package com.simpletecno.sopdi.api.resource;

import com.simpletecno.sopdi.api.DbHelper;
import com.simpletecno.sopdi.api.model.PagoYFactura;
import com.simpletecno.sopdi.api.model.PagoYFacturaResponse;
import com.simpletecno.sopdi.extras.infile.Direccion;
import com.simpletecno.sopdi.extras.infile.Emisor;
import com.simpletecno.sopdi.extras.infile.InfileClient;
import com.simpletecno.sopdi.extras.infile.Producto;
import com.simpletecno.sopdi.extras.infile.Receptor;
import com.simpletecno.sopdi.utilerias.Utileria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/pagoyfactura")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pago y Factura", description = "Contabilizar pago y factura certificada para un cliente")
@SecurityRequirement(name = "ApiKeyAuth")
public class PagoYFacturaResource {

    private static final Logger LOG = Logger.getLogger(PagoYFacturaResource.class.getName());

    @POST
    @Operation(
        summary = "Contabilizar pago y factura venta FEL",
        description = "Recibe los datos del pago de un cliente (cabecera + cuotas) y certifica una " +
                      "Factura Electrónica en Línea (FEL) ante el SAT a través de INFILE. " +
                      "Contabiliza ambos documentos. " +
                      "**Respuesta exitosa:** el campo `pdfBase64` contiene el PDF de la factura " +
                      "certificada codificado en Base64 estándar (RFC 4648). El cliente debe " +
                      "decodificarlo para obtener el archivo PDF (`application/pdf`). " +
                      "Adicionalmente se retornan el `uuid` de autorización SAT, la `serie` y el `numero` del DTE.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = PagoYFactura.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Factura FEL certificada. El campo `pdfBase64` contiene el PDF en Base64.",
                content = @Content(schema = @Schema(implementation = PagoYFacturaResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o incompletos en el request"),
            @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
            @ApiResponse(responseCode = "422", description = "INFILE rechazó la certificación del DTE (ver detalle en el cuerpo)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    public Response pagoYFactura(PagoYFactura pago) {
        if (pago == null) {
            return badRequest("El cuerpo de la solicitud es requerido");
        }
        if (pago.getIdEmpresa() == null) {
            return badRequest("El campo IdEmpresa es obligatorio");
        }
        if (pago.getIdProveedor() == null) {
            return badRequest("El campo IdProveedor es obligatorio");
        }
        if (pago.getMonto() == null || pago.getMonto() <= 0) {
            return badRequest("El campo Monto es obligatorio y debe ser mayor que cero");
        }
        if (pago.getFecha() == null || pago.getFecha().isBlank()) {
            return badRequest("El campo Fecha es obligatorio (formato YYYY-MM-DD)");
        }

        Connection conn = null;
        try {
            conn = DbHelper.getConnection();

            // ── 1. Empresa (emisor + regimen + credenciales FEL) ─────────────
            EmpresaInfo empresa = cargarEmpresa(conn, pago.getIdEmpresa());
            if (empresa == null) {
                return badRequest("Empresa contable no encontrada: " + pago.getIdEmpresa());
            }
            if (empresa.emisor == null) {
                return badRequest("La empresa no tiene credenciales FEL configuradas");
            }

            // ── 2. Cliente ────────────────────────────────────────────────────
            ClienteInfo cliente = cargarCliente(conn, pago.getIdProveedor());
            if (cliente == null) {
                return badRequest("Cliente no encontrado: " + pago.getIdProveedor());
            }

            // ── 3. Cuentas contables default ──────────────────────────────────
            CuentasInfo cuentas = cargarCuentas(conn, empresa.idEmpresa);
            if (cuentas == null || cuentas.clientes == null) {
                return badRequest("Cuentas contables default no configuradas para la empresa");
            }

            // ── 4. Fecha y código de partida ──────────────────────────────────
            Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(pago.getFecha());
            String codigoPartida = Utileria.nextCodigoPartida(conn, empresa.idEmpresa, fecha, 0);

            // ── 5. Productos desde cuotas ─────────────────────────────────────
            List<Producto> productos = construirProductos(conn, pago, empresa);
            if (productos.isEmpty()) {
                return badRequest("Debe incluir al menos una cuota con monto mayor que cero");
            }

            // ── 6. Certificar con INFILE ──────────────────────────────────────
            InfileClient infile = new InfileClient(empresa.emisor);
            Receptor receptor = new Receptor(
                    cliente.nit.replaceAll("-", ""),
                    cliente.nombre,
                    cliente.correo,
                    cliente.direccion);

            boolean certificado = infile.generarDocumentoBase(
                    receptor,
                    codigoPartida,
                    productos,
                    empresa.tipoDocFel,
                    "",
                    fecha,
                    "GTQ",
                    1.0);

            if (!certificado) {
                LOG.warning("INFILE rechazo certificacion. Errores: " + infile.getDescripcionErrores());
                PagoYFacturaResponse err = new PagoYFacturaResponse();
                err.setCodigo(0);
                err.setMensaje("INFILE rechazó la certificación: " + infile.getDescripcion()
                        + " | " + infile.getDescripcionErrores());
                return Response.status(422).entity(err).build();
            }

            String uuid   = infile.getUUID();
            String serie  = infile.getSerie();
            long   numero = infile.getNumero();

            LOG.info("DTE certificado OK. UUID=" + uuid + " Serie=" + serie + " Num=" + numero);

            // ── 7. PDF en memoria → Base64 ────────────────────────────────────
            String pdfBase64 = descargarPdfBase64(uuid);

            // ── 8. proveedor_cuentacorriente ──────────────────────────────────
            insertarCuentaCorriente(conn, pago, empresa, cliente, infile, codigoPartida, fecha);

            // ── 9. contabilidad_partida ───────────────────────────────────────
            insertarPartidas(conn, pago, empresa, cliente, cuentas, infile,
                    codigoPartida, fecha, productos);

            // ── 10. certificado_fel_infile ────────────────────────────────────
            insertarCertificadoFel(conn, infile, codigoPartida, empresa.idEmpresa);

            // ── 11. Respuesta ─────────────────────────────────────────────────
            PagoYFacturaResponse resp = new PagoYFacturaResponse();
            resp.setCodigo(1);
            resp.setUuid(uuid);
            resp.setSerie(serie);
            resp.setNumero(numero);
            resp.setPdfBase64(pdfBase64);
            resp.setMensaje("Factura certificada y contabilizada exitosamente");
            return Response.ok(resp).build();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error en pagoYFactura", e);
            return serverError(e);
        } finally {
            if (conn != null) try { conn.close(); } catch (Exception ignored) {}
        }
    }

    // ─── Carga de datos ───────────────────────────────────────────────────────

    private EmpresaInfo cargarEmpresa(Connection conn, Long idEmpresa) throws Exception {
        String sql = "SELECT * FROM contabilidad_empresa WHERE IdEmpresa = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                EmpresaInfo e = new EmpresaInfo();
                e.idEmpresa = rs.getString("IdEmpresa");
                e.nombre    = rs.getString("Empresa");
                e.nit       = rs.getString("Nit");
                e.regimen   = nvl(rs.getString("Regimen"));
                e.esExenta  = e.regimen.equalsIgnoreCase("EXENTA");
                e.tipoDocFel      = e.esExenta ? "RDON" : "FACT";
                e.tipoDocContable = e.esExenta ? "RECIBO CONTABLE VENTA" : "FACTURA VENTA";

                String felUser = nvl(rs.getString("InfileUsuarioApi"));
                if (felUser.isEmpty()) return e; // sin FEL configurado — emisor null

                e.emisor = new Emisor(
                        e.nit,
                        e.nombre,
                        nvl(rs.getString("CorreoContacto")),
                        new Direccion(
                                nvl(rs.getString("DireccionFactura")),
                                nvl(rs.getString("CodigoPostal")),
                                nvl(rs.getString("NombreDepartamento")),
                                nvl(rs.getString("NombreMunicipio")),
                                nvl(rs.getString("Pais"))
                        ),
                        nvl(rs.getString("AfiliacionIVA")),
                        "1",
                        nvl(rs.getString("NombreCorto")),
                        felUser,
                        nvl(rs.getString("InfileLlaveApi")),
                        nvl(rs.getString("InfileUsuarioFirma")),
                        nvl(rs.getString("InfileLlaveFirma")),
                        nvl(rs.getString("TipoPersoneria"))
                );
                return e;
            }
        }
    }

    private ClienteInfo cargarCliente(Connection conn, Long idProveedor) throws Exception {
        String sql = "SELECT IDProveedor, NIT, Nombre, Email, Direccion " +
                     "FROM proveedor_empresa WHERE IDProveedor = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ClienteInfo c = new ClienteInfo();
                c.idProveedor = rs.getString("IDProveedor");
                c.nit         = nvl(rs.getString("NIT"));
                c.nombre      = nvl(rs.getString("Nombre"));
                c.correo      = nvl(rs.getString("Email"));
                c.direccion   = nvl(rs.getString("Direccion"));
                return c;
            }
        }
    }

    private CuentasInfo cargarCuentas(Connection conn, String idEmpresa) throws Exception {
        String sql = "SELECT Clientes, IvaPorPagar FROM cuentas_contables_default WHERE IdEmpresa = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                CuentasInfo c = new CuentasInfo();
                c.clientes    = rs.getString("Clientes");
                c.ivaPorPagar = rs.getString("IvaPorPagar");
                return c;
            }
        }
    }

    // ─── Productos ────────────────────────────────────────────────────────────

    private List<Producto> construirProductos(Connection conn, PagoYFactura pago, EmpresaInfo empresa) {
        List<Producto> lista = new ArrayList<>();
        List<PagoYFactura.Cuota> cuotas = pago.getCuotas();

        if (cuotas == null || cuotas.isEmpty()) {
            // Línea única con el monto total
            Producto p = new Producto(
                    "Pago",
                    pago.getMonto(),
                    1,
                    nvl(pago.getNumeroComprobante()),
                    "S");
            cargarFrasesProducto(conn, p, null, empresa.idEmpresa);
            lista.add(p);
        } else {
            for (PagoYFactura.Cuota cuota : cuotas) {
                if (cuota.getMonto() == null || cuota.getMonto() <= 0) continue;
                Producto p = new Producto(
                        cuota.getConcepto() != null ? cuota.getConcepto() : "Cuota",
                        cuota.getMonto(),
                        1,
                        "",
                        "S");
                cargarFrasesProducto(conn, p, cuota.getIdNomenclatura(), empresa.idEmpresa);
                lista.add(p);
            }
        }
        return lista;
    }

    /** Busca InfileTipo y frases en producto_venta_empresa para la nomenclatura dada. */
    private void cargarFrasesProducto(Connection conn, Producto p, String idNomenclatura, String idEmpresa) {
        StringBuilder sql = new StringBuilder(
                "SELECT pve.InfileTipo, pvf.Frase, pvf.Escenario " +
                "FROM producto_venta_empresa pve " +
                "LEFT JOIN producto_venta_frases pvf ON pvf.CorrelativoProducto = pve.CorrelativoProducto " +
                "WHERE pve.IdEmpresa = ? AND pve.Especial = 0");
        if (idNomenclatura != null && !idNomenclatura.isBlank()) {
            sql.append(" AND pve.IdNomenclatura = ?");
        }
        sql.append(" LIMIT 50");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, idEmpresa);
            if (idNomenclatura != null && !idNomenclatura.isBlank()) {
                ps.setString(2, idNomenclatura);
            }
            try (ResultSet rs = ps.executeQuery()) {
                boolean tipoSet = false;
                while (rs.next()) {
                    if (!tipoSet) {
                        String tipo = rs.getString("InfileTipo");
                        if (tipo != null && !tipo.isBlank()) p.setBienOServicio(tipo);
                        tipoSet = true;
                    }
                    int frase    = rs.getInt("Frase");
                    int escenario = rs.getInt("Escenario");
                    if (frase > 0) p.agregarFrase(frase, escenario);
                }
            }
        } catch (Exception ex) {
            LOG.warning("No se cargaron frases del producto: " + ex.getMessage());
        }
    }

    // ─── PDF ─────────────────────────────────────────────────────────────────

    private String descargarPdfBase64(String uuid) {
        try {
            URL url = new URL(InfileClient.PDF_URL + uuid);
            URLConnection urlConn = url.openConnection();
            urlConn.setConnectTimeout(15000);
            urlConn.setReadTimeout(30000);

            if (!"application/pdf".equalsIgnoreCase(urlConn.getContentType())) {
                LOG.warning("El servidor no retorno PDF para UUID=" + uuid);
                return null;
            }
            try (InputStream is = urlConn.getInputStream();
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
                return Base64.getEncoder().encodeToString(bos.toByteArray());
            }
        } catch (Exception ex) {
            LOG.warning("No se pudo descargar PDF de INFILE: " + ex.getMessage());
            return null;
        }
    }

    // ─── Inserts DB ───────────────────────────────────────────────────────────

    private void insertarCuentaCorriente(Connection conn, PagoYFactura pago, EmpresaInfo empresa,
            ClienteInfo cliente, InfileClient infile, String codigoPartida, Date fecha) throws Exception {

        String sql = "INSERT INTO proveedor_cuentacorriente " +
                "(IdEmpresa, IdProveedor, Fecha, TipoDocumento, SerieDocumento, NumeroDocumento, " +
                "MonedaDocumento, Monto, MontoQuetzales, TipoCambio, " +
                "IdUsuarioAutorizoPago, CreadoFechayHora, CreadoUsuario) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'QUETZALES', ?, ?, 1, 0, current_timestamp, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empresa.idEmpresa);
            ps.setString(2, cliente.idProveedor);
            ps.setString(3, Utileria.getFechaYYYYMMDD_1(fecha));
            ps.setString(4, empresa.tipoDocContable);
            ps.setString(5, infile.getSerie().toUpperCase());
            ps.setString(6, String.valueOf(infile.getNumero()));
            ps.setDouble(7, pago.getMonto());
            ps.setDouble(8, pago.getMonto());
            ps.executeUpdate();
        }
    }

    private void insertarPartidas(Connection conn, PagoYFactura pago, EmpresaInfo empresa,
            ClienteInfo cliente, CuentasInfo cuentas, InfileClient infile,
            String codigoPartida, Date fecha, List<Producto> productos) throws Exception {

        String fechaStr          = Utileria.getFechaYYYYMMDD_1(fecha);
        String fechaCertStr      = Utileria.getFechaYYYYMMDD_1(infile.getFechaHoraCertificacion());
        String serie             = infile.getSerie().toUpperCase();
        String numero            = String.valueOf(infile.getNumero());
        String uuid              = infile.getUUID();
        double montoTotal        = pago.getMonto();
        String descripcion       = empresa.tipoDocContable + " " + cliente.nombre;
        String tipoVenta         = productos.isEmpty() ? "S" : nvl(productos.get(0).getBienOServicio());
        boolean esExenta         = empresa.esExenta;

        double ivaTotal = 0.0;
        if (!esExenta) {
            ivaTotal = Double.parseDouble(Utileria.format(montoTotal - (montoTotal / 1.12)));
        }

        String colsSinId =
                "IdEmpresa, Estatus, CodigoPartida, CodigoCC, TipoDocumento, TipoVenta, Fecha, " +
                "IdProveedor, NitProveedor, NombreProveedor, SerieDocumento, NumeroDocumento, " +
                "IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber, " +
                "DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion, Referencia, " +
                "IdCentroCosto, CodigoCentroCosto, CreadoUsuario, CreadoFechaYHora, " +
                "Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre, " +
                "UUID, FechaYHoraCertificacion, XmlRequest, XmlResponse, IdProducto";

        StringBuilder sql = new StringBuilder("INSERT INTO contabilidad_partida (")
                .append(colsSinId).append(") VALUES ");

        List<Object[]> filas = new ArrayList<>();

        // ── Línea clientes (DEBE) ─────────────────────────────────────────────
        filas.add(new Object[]{
            empresa.idEmpresa, "INGRESADO", codigoPartida, codigoPartida,
            empresa.tipoDocContable, tipoVenta, fechaStr,
            cliente.idProveedor, cliente.nit, cliente.nombre,
            serie, numero,
            cuentas.clientes, "QUETZALES", montoTotal,
            montoTotal, 0.0,                   // DEBE, HABER
            montoTotal, 0.0,                   // DEBE_Q, HABER_Q
            1.0, montoTotal, descripcion, "NO",
            null, null,                        // IdCentroCosto, CodigoCentroCosto
            0, "current_timestamp",
            "", "application/pdf", 0, "",
            uuid, fechaCertStr, "", "", null   // IdProducto
        });

        // ── Líneas de cuota / ingresos (HABER) ────────────────────────────────
        List<PagoYFactura.Cuota> cuotas = pago.getCuotas();
        for (int i = 0; i < productos.size(); i++) {
            Producto prod   = productos.get(i);
            double montoLinea = prod.getMonto().doubleValue();
            double haberLinea = esExenta
                    ? montoLinea
                    : Double.parseDouble(Utileria.format(montoLinea / 1.12));

            String idNom = "NULL";
            if (cuotas != null && i < cuotas.size() && cuotas.get(i).getIdNomenclatura() != null) {
                idNom = cuotas.get(i).getIdNomenclatura();
            }

            filas.add(new Object[]{
                empresa.idEmpresa, "INGRESADO", codigoPartida, codigoPartida,
                empresa.tipoDocContable, prod.getBienOServicio(), fechaStr,
                cliente.idProveedor, cliente.nit, cliente.nombre,
                serie, numero,
                idNom, "QUETZALES", montoTotal,
                0.0, haberLinea,               // DEBE=0, HABER=neto
                0.0, haberLinea,               // DEBE_Q=0, HABER_Q=neto
                1.0, 0.0, descripcion, "NO",
                null, null,
                0, "current_timestamp",
                "", "application/pdf", 0, "",
                uuid, fechaCertStr, "", "", idNom.equals("NULL") ? null : idNom
            });
        }

        // ── Línea IVA por pagar (HABER) ────────────────────────────────────────
        if (!esExenta && cuentas.ivaPorPagar != null && ivaTotal > 0) {
            filas.add(new Object[]{
                empresa.idEmpresa, "INGRESADO", codigoPartida, codigoPartida,
                empresa.tipoDocContable, tipoVenta, fechaStr,
                cliente.idProveedor, cliente.nit, cliente.nombre,
                serie, numero,
                cuentas.ivaPorPagar, "QUETZALES", montoTotal,
                0.0, ivaTotal,                 // DEBE=0, HABER=IVA
                0.0, ivaTotal,
                1.0, 0.0, descripcion, "NO",
                null, null,
                0, "current_timestamp",
                "", "application/pdf", 0, "",
                uuid, fechaCertStr, "", "", null
            });
        }

        // ── Construir VALUES placeholders ─────────────────────────────────────
        String placeholder = "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        for (int i = 0; i < filas.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append(placeholder);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object[] fila : filas) {
                for (Object val : fila) {
                    if (val == null)                           ps.setNull(idx++, java.sql.Types.VARCHAR);
                    else if ("current_timestamp".equals(val)) ps.setString(idx++, Utileria.getFechaYYYYMMDD_1(new Date()));
                    else if (val instanceof Double)            ps.setDouble(idx++, (Double) val);
                    else if (val instanceof Integer)           ps.setInt(idx++, (Integer) val);
                    else                                       ps.setString(idx++, val.toString());
                }
            }
            ps.executeUpdate();
        }
    }

    private void insertarCertificadoFel(Connection conn, InfileClient infile,
            String codigoPartida, String idEmpresa) throws Exception {

        String sql = "INSERT INTO certificado_fel_infile " +
                "(Fecha, Origen, Descripcion, Saldo, Creditos, AlertasInfile, AlertasSat, " +
                "InformacionAdicional, UUID, Serie, Numero, JsonResponse, CodigoPartida, IdEmpresa, Estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'INGRESADO')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Utileria.getFechaYYYYMMDD_1(infile.getFechaHoraCertificacion()));
            ps.setString(2, infile.getOrigen());
            ps.setString(3, infile.getDescripcion());
            ps.setString(4, infile.getSaldo());
            ps.setString(5, infile.getCreditos());
            ps.setBoolean(6, infile.getAlertasInfile());
            ps.setBoolean(7, infile.getAlertasSAT());
            ps.setString(8, infile.getInformacionAdicional());
            ps.setString(9, infile.getUUID());
            ps.setString(10, infile.getSerie());
            ps.setString(11, String.valueOf(infile.getNumero()));
            ps.setString(12, infile.getRespuesta());
            ps.setString(13, codigoPartida);
            ps.setString(14, idEmpresa);
            ps.executeUpdate();
        }
    }

    // ─── Inner DTOs ───────────────────────────────────────────────────────────

    private static class EmpresaInfo {
        String idEmpresa, nombre, nit, regimen;
        boolean esExenta;
        String tipoDocFel;        // "FACT" | "RDON"
        String tipoDocContable;   // "FACTURA VENTA" | "RECIBO CONTABLE VENTA"
        Emisor emisor;
    }

    private static class ClienteInfo {
        String idProveedor, nit, nombre, correo, direccion;
    }

    private static class CuentasInfo {
        String clientes;
        String ivaPorPagar;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String nvl(String s) { return s == null ? "" : s; }

    private Response badRequest(String msg) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"" + msg + "\"}").build();
    }

    private Response serverError(Exception e) {
        return Response.serverError()
                .entity("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}")
                .build();
    }
}
