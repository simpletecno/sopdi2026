package com.simpletecno.sopdi.extras.infile;

import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.ui.Notification;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.json.JSONArray;


public class InfileClient {
    final static String URL = "https://certificador.feel.com.gt/fel/procesounificado/transaccion/v2/xml";
    final static String PDF_URL = "https://report.feel.com.gt/ingfacereport/ingfacereport_documento?uuid=";
    public final static int EXENTOIVA_FRASE = 4;
    public final static int ESCENARIO_FUNDACION = 10;

    private Emisor emisor;
    Map<Integer, Integer> frases = null;


    final BigDecimal IVA_RATE     = new BigDecimal("0.12");
    final BigDecimal IVA_DIVISOR  = BigDecimal.ONE.add(IVA_RATE); // 1.12
    final int MONEY_SCALE = 2;
    final RoundingMode RM = RoundingMode.HALF_UP;

    // USO EXCLUSIVO DE CIVA
    BigDecimal montoExencionIva;
    // USO EXCLUSIVO DE CIVA

    private String respuesta;
    private JSONObject jsonRespuesta;  // variable global para guardar la respuesta

    HttpURLConnection conn = null;


    public InfileClient(Emisor emisor) {
        this.emisor = emisor;
    }

    public boolean generarDocumentoBase(Receptor receptor, String identificador, List<Producto> productos, String tipoDocumento,
                                        String adenda, Date FechaEmision, String Moneda, Double TipoCambio) {
        if(tipoDocumento.equals("RDON") || tipoDocumento.equals("FACT")) {
            return generarDocumento(receptor, identificador, productos, tipoDocumento, adenda, "", "", "",
                                    FechaEmision, FechaEmision, Moneda, TipoCambio, "");
        }else {
            // Si el tipo de documento no es soportado, retornar false
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error: Tipo de documento no soportado. NO ES BASE (FACT o RDON)");
            return false;
        }
    }

    public boolean generarDocumentoDependiente(Receptor receptor, String identificador, List<Producto> productos,String tipoDocumento,
                                               String adenda, String UUID, String serie, String numero, Date FechaEmision,
                                               Date fechaDependienteEmision, String Moneda, Double TipoCambio, String razon) {
        if (tipoDocumento.equals("NCRE")){
            return generarDocumento(receptor, identificador, productos, tipoDocumento, adenda, UUID, serie, numero,
                                    FechaEmision, fechaDependienteEmision, Moneda, TipoCambio, razon);
        }else {  // Si el tipo de documento no es soportado, retornar false
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error: Tipo de documento no soportado. NO ES DEPENDIENTE (NCRE)");
            return false;
        }
    }

    public boolean generarDocumentoCIVA(Receptor receptor, String identificador, List<Producto> productos, BigDecimal exento,
                                        String tipoDocumento, String adenda, String UUID, String serie, String numero,
                                        Date FechaEmision, Date fechaDependienteEmision, String Moneda, Double TipoCambio, String razon) {
        if (tipoDocumento.equals("CIVA")){
            montoExencionIva = exento;
            return generarDocumento(receptor, identificador, productos, tipoDocumento, adenda, UUID, serie, numero,
                    FechaEmision, fechaDependienteEmision, Moneda, TipoCambio, razon);
        }else {  // Si el tipo de documento no es soportado, retornar false
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error: Tipo de documento no soportado. NO ES DEPENDIENTE (CIVA)");
            return false;
        }
    }

    private boolean generarDocumento(Receptor receptor, String identificador, List<Producto> productos, String tipoDocumento, String adenda, String UUID,
                                     String serie, String numero, Date FechaEmision, Date fechaDependienteEmision, String Moneda, Double TipoCambio, String razon) {
        try {


            if (productos == null || productos.isEmpty()) {
                System.out.println("Error: Debe incluir al menos un producto.");
                return false;
            }

            boolean todosIguales = productos.stream()
                    .map(Producto::getFrases)
                    .map(m -> new TreeMap<>(m)) // normaliza orden y equals/hashCode
                    .distinct()
                    .limit(2)
                    .count() <= 1;

            if (!todosIguales) {
                System.out.println("Error: Todos los productos deben tener las mismas frases.");
                return false;
            }else {
                frases = productos.get(0).getFrases();
            }
            URL url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            // Configurar headers
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/xml");
            conn.setRequestProperty("UsuarioFirma", emisor.getUsuarioFirma());
            conn.setRequestProperty("LlaveFirma", emisor.getLlaveFirma());
            conn.setRequestProperty("UsuarioApi", emisor.getUsuarioApi());
            conn.setRequestProperty("LlaveApi", emisor.getLlaveApi());
            conn.setRequestProperty("identificador", identificador);

            conn.setDoOutput(true);

            // Generar XML
            String xmlContent = generarXML(
                    receptor,
                    tipoDocumento,
                    adenda,
                    UUID,
                    serie,
                    numero,
                    FechaEmision,
                    fechaDependienteEmision,
                    Moneda,
                    TipoCambio,
                    productos,
                    razon
            );


            System.out.println("XML generado:\n" + xmlContent);

            // Enviar XML
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = xmlContent.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Leer respuesta
            int responseCode = conn.getResponseCode();
            InputStream responseStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Mostrar respuesta
            System.out.println("Respuesta del servidor:");
            System.out.println(response);

            respuesta = response.toString();

            jsonRespuesta = new JSONObject(respuesta);

            return this.getResultado();

        } catch (Exception ex) {
            System.out.println("Error en el boton nuevo" + ex);
            ex.printStackTrace();
            return false;
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }


    private String generarXML(Receptor receptor, String tipoDocumento, String adenda, String UUID, String serie, String numero,
                              Date fechaEmision, Date fechaDependienteEmision, String moneda, Double tipoCambio, List<Producto> productos, String razon) {
//-------------------------------- Encabezado del XML --------------------------
        String fechaHoraEmisionStr = Utileria.getFechaDDMMYYYY_HHMM_SS(fechaEmision);
        String fechaHoraDependienteStr = Utileria.getFechaYYYYMMDD_1(fechaDependienteEmision);
        StringBuilder xml = new StringBuilder();

        boolean tieneAdenda = adenda != null && !adenda.isEmpty();
        boolean tieneTipoCambio = tipoCambio != null && tipoCambio > 1;

        xml.append("<dte:GTDocumento xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:dte=\"http://www.sat.gob.gt/dte/fel/0.2.0\" ");
        xml.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" Version=\"0.1\" xsi:schemaLocation=\"http://www.sat.gob.gt/dte/fel/0.2.0\">\n");
        xml.append("<dte:SAT ClaseDocumento=\"dte\">\n");
        xml.append("<dte:DTE ID=\"DatosCertificados\">\n");
        xml.append("<dte:DatosEmision ID=\"DatosEmision\">\n");
        xml.append("<dte:DatosGenerales CodigoMoneda=\"").append(moneda).append("\" FechaHoraEmision=\"").append(fechaHoraEmisionStr);
        switch (tipoDocumento) {
            case "RDON":
                xml.append("\" Tipo=\"").append(tipoDocumento).append("\" TipoPersoneria=\"").append(emisor.getTipoPersoneria()).append("\"\n");
                break;
            case "FACT":
            case "CIVA":
            case "NCRE":
                xml.append("\" Tipo=\"").append(tipoDocumento).append("\"");
                break;
            default:
                System.out.println("Error en el boton nuevo");
                xml.append("\" Tipo=\"").append("ERROR").append("\"");
        }
        xml.append("></dte:DatosGenerales>");
        xml.append("<dte:Emisor AfiliacionIVA=\"").append(emisor.getAfiliacionIVA()).append("\" CodigoEstablecimiento=\"1\" ");
        xml.append( "CorreoEmisor=\"").append(emisor.getCorreo()).append("\" NITEmisor=\"").append(emisor.getNit());
        xml.append("\" NombreComercial=\"").append(emisor.getNombreComercial()).append("\" NombreEmisor=\"").append(emisor.getNombre()).append("\">\n");
        xml.append("<dte:DireccionEmisor>\n");
        xml.append("<dte:Direccion>").append(emisor.getDireccion().getDireccion()).append("</dte:Direccion>\n");
        xml.append("<dte:CodigoPostal>").append(emisor.getDireccion().getCodigoPostal()).append("</dte:CodigoPostal>\n");
        xml.append("<dte:Municipio>").append(emisor.getDireccion().getMunicipio()).append("</dte:Municipio>\n");
        xml.append("<dte:Departamento>").append(emisor.getDireccion().getDepartamento()).append("</dte:Departamento>\n");
        xml.append("<dte:Pais>").append(emisor.getDireccion().getPais()).append("</dte:Pais>\n");
        xml.append("</dte:DireccionEmisor>\n");
        xml.append("</dte:Emisor>\n");

        xml.append("<dte:Receptor CorreoReceptor=\"").append(receptor.getCorreo()).append("\" IDReceptor=\"").append(receptor.getNit());
        xml.append("\" NombreReceptor=\"").append(receptor.getNombre()).append("\">\n");
        xml.append("<dte:DireccionReceptor>\n");
        xml.append("<dte:Direccion>").append(receptor.getDireccion().getDireccion()).append("</dte:Direccion>\n");
        xml.append("<dte:CodigoPostal>").append(receptor.getDireccion().getCodigoPostal()).append("</dte:CodigoPostal>\n");
        xml.append("<dte:Municipio>").append(receptor.getDireccion().getMunicipio()).append("</dte:Municipio>\n");
        xml.append("<dte:Departamento>").append(receptor.getDireccion().getDepartamento()).append("</dte:Departamento>\n");
        xml.append("<dte:Pais>").append(receptor.getDireccion().getPais()).append("</dte:Pais>\n");
        xml.append("</dte:DireccionReceptor>\n");
        xml.append("</dte:Receptor>\n");
//-------------------------------- Frases --------------------------------
        if(!tipoDocumento.equals("CIVA")) {
            xml.append("<dte:Frases>\n");
            if (frases != null) {
                for (Map.Entry<Integer, Integer> entry : frases.entrySet()) {
                    int frase = entry.getKey();
                    int escenario = entry.getValue();
                    if(frase == EXENTOIVA_FRASE && tipoDocumento.equals("NCRE")) {
                        xml.append(""); // No agregar la frase de exento de IVA en notas de crédito
                    }else {
                        xml.append("  <dte:Frase CodigoEscenario=\"").append(escenario).append("\" TipoFrase=\"").append(frase).append("\"/>\n");
                    }
                }
            }
            xml.append("</dte:Frases>\n");
        }
//-------------------------------- Generación de Items --------------------------------
        int linea = 1;
        BigDecimal total = new BigDecimal("0.00");
        BigDecimal totalIVA = new BigDecimal("0.00");
        xml.append("<dte:Items>\n");

        for (Producto p : productos) {
            BigDecimal precioProducto = p.getMonto(); // asumir scale consistente
            BigDecimal cantidad       = BigDecimal.valueOf(p.getCantidad());
            BigDecimal totalProducto  = precioProducto.multiply(cantidad);

// gravable e impuesto
            BigDecimal gravableProducto;
            BigDecimal impuestoProducto;

            if (p.tieneFrase(EXENTOIVA_FRASE)) {
                if (tipoDocumento.equals("FACT") || tipoDocumento.equals("NCRE")) {
                    // Exento: todo el total es gravable=total y el impuesto es 0
                    gravableProducto  = totalProducto.setScale(MONEY_SCALE, RM);
                    impuestoProducto  = BigDecimal.ZERO.setScale(MONEY_SCALE, RM);
                } else {
                    gravableProducto  = BigDecimal.ZERO.setScale(MONEY_SCALE, RM);
                    impuestoProducto  = BigDecimal.ZERO.setScale(MONEY_SCALE, RM);
                }
            } else {
                // NO exento: factoriza IVA desde el total
                gravableProducto = totalProducto.divide(IVA_DIVISOR, MONEY_SCALE, RM);
                impuestoProducto = totalProducto.subtract(gravableProducto).setScale(MONEY_SCALE, RM);
            }

            total = total.add(totalProducto);
            totalIVA = totalIVA.add(impuestoProducto.multiply(BigDecimal.valueOf(p.getCantidad())));

            xml.append("    <dte:Item BienOServicio=\"").append(p.getBienOServicio()).append("\" NumeroLinea=\"").append(linea++).append("\">\n");
            xml.append("      <dte:Cantidad>").append(p.getCantidad()).append("</dte:Cantidad>\n");
            xml.append("      <dte:UnidadMedida>UND</dte:UnidadMedida>\n");
            xml.append("      <dte:Descripcion>").append(p.getNombre()).append(" ").append(p.getComentario()).append("</dte:Descripcion>\n");
            xml.append("      <dte:PrecioUnitario>").append(String.format("%.2f", precioProducto)).append("</dte:PrecioUnitario>\n");
            xml.append("      <dte:Precio>").append(String.format("%.2f", totalProducto)).append("</dte:Precio>\n");
            xml.append("      <dte:Descuento>0.00</dte:Descuento>\n");

            if (!tipoDocumento.equals("RDON")) {
                xml.append("      <dte:Impuestos>\n");
                xml.append("        <dte:Impuesto>\n");
                xml.append("          <dte:NombreCorto>IVA</dte:NombreCorto>\n");
                if(p.tieneFrase(EXENTOIVA_FRASE)) {
                    xml.append("          <dte:CodigoUnidadGravable>2</dte:CodigoUnidadGravable>\n");
                } else {
                    xml.append("          <dte:CodigoUnidadGravable>1</dte:CodigoUnidadGravable>\n");
                }
                xml.append("          <dte:MontoGravable>").append(String.format("%.2f", gravableProducto)).append("</dte:MontoGravable>\n");
                xml.append("          <dte:MontoImpuesto>").append(String.format("%.2f", impuestoProducto)).append("</dte:MontoImpuesto>\n");
                xml.append("        </dte:Impuesto>\n");
                xml.append("      </dte:Impuestos>\n");
            }

            xml.append("      <dte:Total>").append(String.format("%.2f", totalProducto)).append("</dte:Total>\n");
            xml.append("    </dte:Item>\n");
        }

        xml.append("</dte:Items>\n");
        xml.append("    <dte:Totales>\n");

        if (!tipoDocumento.equals("RDON")) {
            xml.append("      <dte:TotalImpuestos>\n");
            xml.append("        <dte:TotalImpuesto NombreCorto=\"IVA\" TotalMontoImpuesto=\"").append(String.format("%.2f", totalIVA)).append("\"></dte:TotalImpuesto>\n");
            xml.append("      </dte:TotalImpuestos>\n");
        }
        xml.append("       <dte:GranTotal>").append(String.format("%.2f", total)).append("</dte:GranTotal>\n");
        xml.append("    </dte:Totales>\n");

//-------------------------------- Complementos y Referencias -----------------------------
        switch (tipoDocumento) {
            case "CIVA":
                xml.append("<dte:Complementos>\n");
                xml.append("  <dte:Complemento IDComplemento=\"ReferenciasConstancia\" NombreComplemento=\"ReferenciasConstancia\" URIComplemento=\"http://www.sat.gob.gt/face2/ComplementoReferenciaConstancia/0.1.0\">\n");
                xml.append("    <crc:ReferenciasConstancia xmlns:crc=\"http://www.sat.gob.gt/face2/ComplementoReferenciaConstancia/0.1.0\" ");
                xml.append("FechaEmisionDocumentoOrigen=\"").append(fechaHoraDependienteStr).append("\" MontoIVAExento=\"").append(Utileria.format(montoExencionIva)).append("\" ");
                xml.append("NumeroAutorizacionDocumentoOrigen=\"").append(UUID).append("\" Version=\"1\" xsi:schemaLocation=\"http://www.sat.gob.gt/dte/fel/ComplementoReferenciaConstancia/0.1.0\"/>\n");
                xml.append("  </dte:Complemento>\n");
                xml.append("</dte:Complementos>\n");
                break;
            case "NCRE":
                xml.append("<dte:Complementos>\n");
                xml.append("  <dte:Complemento IDComplemento=\"Notas\" NombreComplemento=\"Notas\" URIComplemento=\"http://www.sat.gob.gt/fel/notas.xsd\">\n");
                xml.append("    <cno:ReferenciasNota xmlns:cno=\"http://www.sat.gob.gt/face2/ComplementoReferenciaNota/0.1.0\" ");
                xml.append("FechaEmisionDocumentoOrigen=\"").append(fechaHoraDependienteStr);
                xml.append("\" MotivoAjuste=\"").append(esc(razon)).append("\" NumeroAutorizacionDocumentoOrigen=\"").append(UUID);
                xml.append("\" NumeroDocumentoOrigen=\"").append(numero).append("\" SerieDocumentoOrigen=\"").append(serie);
                xml.append("\" Version=\"0.0\" xsi:schemaLocation=\"http://www.sat.gob.gt/face2/ComplementoReferenciaNota/0.1.0 C:\\Users\\User\\Desktop\\FEL\\Esquemas\\GT_Complemento_Referencia_Nota-0.1.0.xsd\"/>\n");
                xml.append("  </dte:Complemento>\n");
                xml.append("</dte:Complementos>\n");
                break;
            default:
                // No hay complementos para otros tipos de documentos
                break;
        }

        xml.append("</dte:DatosEmision>\n");
        xml.append("</dte:DTE>\n");


        if (tieneAdenda || tieneTipoCambio) {
            xml.append("<dte:Adenda>\n");
            xml.append("  <Codigo_cliente>").append(receptor.getNombre()).append("</Codigo_cliente>\n");
            if (tieneAdenda) {
                xml.append("  <Observaciones>").append(esc(adenda)).append("</Observaciones>\n");
            }
            if (tieneTipoCambio) {
                xml.append("  <TipoCambio>").append(String.format("%.4f", tipoCambio)).append("</TipoCambio>\n");
            }
            xml.append("</dte:Adenda>\n");
        }

        xml.append("</dte:SAT>\n");
        xml.append("</dte:GTDocumento>\n");

        return xml.toString();
    }


    public boolean generarAnulacion(String numeroDocumentoAAnular, String idReceptor, String identificador,
                                   String motivoAnulacion, Date fechaDocumentoBase, Date fechaHoraAnulacion) {
        try {
            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/xml");
            conn.setRequestProperty("UsuarioFirma", emisor.getUsuarioFirma());
            conn.setRequestProperty("LlaveFirma", emisor.getLlaveFirma());
            conn.setRequestProperty("UsuarioApi", emisor.getUsuarioApi());
            conn.setRequestProperty("LlaveApi", emisor.getLlaveApi());
            conn.setRequestProperty("identificador", identificador);

            String fechaHoraBaseStr = Utileria.getFechaDDMMYYYY_HHMM_SS(fechaDocumentoBase);
            String fechaHoraAnularStr = Utileria.getFechaDDMMYYYY_HHMM_SS(fechaHoraAnulacion);
            StringBuilder xml = new StringBuilder();
            xml.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<dte:GTAnulacionDocumento xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" ");
            xml.append("xmlns:dte=\"http://www.sat.gob.gt/dte/fel/0.1.0\" ");
            xml.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" Version=\"0.1\" ");
            xml.append("xsi:schemaLocation=\"http://www.sat.gob.gt/dte/fel/0.1.0 GT_AnulacionDocumento-0.1.0.xsd\">\n");
            xml.append("  <dte:SAT>\n");
            xml.append("    <dte:AnulacionDTE ID=\"DatosCertificados\">\n");
            xml.append("      <dte:DatosGenerales FechaEmisionDocumentoAnular=\"").append(fechaHoraBaseStr).append("\"\n");
            xml.append("                          FechaHoraAnulacion=\"").append(fechaHoraAnularStr).append("\"\n");
            xml.append("                          ID=\"DatosAnulacion\"\n");
            xml.append("                          IDReceptor=\"").append(idReceptor).append("\"\n");
            xml.append("                          MotivoAnulacion=\"").append(motivoAnulacion).append("\"\n");
            xml.append("                          NITEmisor=\"").append(emisor.getNit()).append("\"\n");
            xml.append("                          NumeroDocumentoAAnular=\"").append(numeroDocumentoAAnular).append("\" />\n");
            xml.append("    </dte:AnulacionDTE>\n");
            xml.append("  </dte:SAT>\n");
            xml.append("</dte:GTAnulacionDocumento>");


            System.out.println("XML generado:\n" + xml);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = xml.toString().getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            InputStream responseStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            respuesta = response.toString();
            jsonRespuesta = new JSONObject(respuesta);

            // Mostrar respuesta
            System.out.println("Respuesta del servidor:");
            System.out.println(response);
            System.out.println("Response Code: " + responseCode);

            return this.getResultado();

        } catch (Exception ex) {
            System.out.println("Error enviando la anulación: " + ex);
            ex.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    public File obtenerDTEPdf(String savePath) {
        byte[] buffer = new byte[1024];
        int bytesRead;

        try {
            URL pdfFileUrl = new URL(PDF_URL + getUUID());
            File pdfFile = new File(savePath);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Conectando a: " + pdfFileUrl);

            URLConnection urlConn = pdfFileUrl.openConnection();

            if (!"application/pdf".equalsIgnoreCase(urlConn.getContentType())) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "La respuesta no es un PDF válido.");
                Notification.show("No se pudo obtener el PDF de la factura.", Notification.Type.ERROR_MESSAGE);
                return null;
            }

            try  {

                System.out.println("Descargando PDF desde: " + pdfFileUrl);
                System.out.println("Guardando PDF en: " + pdfFile.getAbsolutePath());

                if(!pdfFile.getParentFile().exists()) {
                    pdfFile.getParentFile().mkdirs(); // Crea el directorio si no existe
                }

                InputStream inputStream = urlConn.getInputStream();
                FileOutputStream fos = new FileOutputStream(pdfFile);

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "PDF descargado correctamente en: " + savePath);

            } catch (ConnectException ce) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error de conexión al obtener el PDF: " + ce.getMessage());
                Notification.show("Error de conexión al obtener el PDF.", Notification.Type.ERROR_MESSAGE);
            }
            return pdfFile;
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Fallo al obtener el PDF: " + ex.getMessage());
            Notification.show("Error inesperado al obtener el PDF.", Notification.Type.ERROR_MESSAGE);
        }
        return null;
    }

    public static File obtenerDTEPdf(String UUID, String savePath) {
        byte[] buffer = new byte[1024];
        int bytesRead;

        try {
            URL pdfFileUrl = new URL(PDF_URL + UUID);
            File pdfFile = new File(savePath);

            Logger.getLogger(InfileClient.class.getName()).log(Level.INFO, "Conectando a: " + pdfFileUrl);

            URLConnection urlConn = pdfFileUrl.openConnection();

            if (!"application/pdf".equalsIgnoreCase(urlConn.getContentType())) {
                Logger.getLogger(InfileClient.class.getName()).log(Level.SEVERE, "La respuesta no es un PDF válido.");
                Notification.show("No se pudo obtener el PDF de la factura.", Notification.Type.ERROR_MESSAGE);
                return null;
            }

            try  {

                System.out.println("Descargando PDF desde: " + pdfFileUrl);
                System.out.println("Guardando PDF en: " + pdfFile.getAbsolutePath());

                if(!pdfFile.getParentFile().exists()) {
                    pdfFile.getParentFile().mkdirs(); // Crea el directorio si no existe
                }

                InputStream inputStream = urlConn.getInputStream();
                FileOutputStream fos = new FileOutputStream(pdfFile);

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }

                Logger.getLogger(InfileClient.class.getName()).log(Level.INFO, "PDF descargado correctamente en: " + savePath);

            } catch (ConnectException ce) {
                Logger.getLogger(InfileClient.class.getName()).log(Level.SEVERE, "Error de conexión al obtener el PDF: " + ce.getMessage());
                Notification.show("Error de conexión al obtener el PDF.", Notification.Type.ERROR_MESSAGE);
            }
            return pdfFile;
        } catch (Exception ex) {
            Logger.getLogger(InfileClient.class.getName()).log(Level.SEVERE, "Fallo al obtener el PDF: " + ex.getMessage());
            Notification.show("Error inesperado al obtener el PDF.", Notification.Type.ERROR_MESSAGE);
        }
        return null;
    }

    public String getRespuesta(){
        return respuesta;
    }

    public boolean getResultado() {
        return jsonRespuesta != null && jsonRespuesta.optBoolean("resultado", false);
    }

    public Date getFechaHoraCertificacion() {
        if (jsonRespuesta != null) {
            String fechaCompleta = jsonRespuesta.optString("fecha", "");
            if (!fechaCompleta.isEmpty()) {
                try {
                    OffsetDateTime odt = OffsetDateTime.parse(fechaCompleta);
                    Instant instant = odt.toInstant();
                    return Date.from(instant); // Devuelve java.util.Date
                } catch (Exception e) {
                    System.out.println("Error al parsear la fecha: " + e.getMessage());
                }
            }
        }
        return null;
    }


    public String getOrigen() {
        return jsonRespuesta != null ? jsonRespuesta.optString("origen", "") : "";
    }

    public String getDescripcion() {
        return jsonRespuesta != null ? jsonRespuesta.optString("descripcion", "") : "";
    }

    public String getSaldo() {
        if (jsonRespuesta != null && jsonRespuesta.has("control_emision")) {
            JSONObject control = jsonRespuesta.getJSONObject("control_emision");
            return control.optString("Saldo", "");
        }
        return "";
    }

    public String getCreditos() {
        if (jsonRespuesta != null && jsonRespuesta.has("control_emision")) {
            JSONObject control = jsonRespuesta.getJSONObject("control_emision");
            return control.optString("Creditos", "");
        }
        return "";
    }



    public List<String> getDescripcionAlertasInfile() {
        List<String> alertas = new ArrayList<>();
        if (jsonRespuesta != null && jsonRespuesta.has("descripcion_alertas_infile")) {
            JSONArray arr = jsonRespuesta.getJSONArray("descripcion_alertas_infile");
            for (int i = 0; i < arr.length(); i++) {
                alertas.add(arr.optString(i));
            }
        }
        return alertas;
    }

    public boolean getAlertasSAT() {
        return jsonRespuesta != null && jsonRespuesta.optBoolean("alertas_sat", false);
    }

    public boolean getAlertasInfile() {
        return jsonRespuesta != null && jsonRespuesta.optBoolean("alertas_infile", false);
    }


    public List<String> getDescripcionAlertasSAT() {
        List<String> alertas = new ArrayList<>();
        if (jsonRespuesta != null && jsonRespuesta.has("descripcion_alertas_sat")) {
            JSONArray arr = jsonRespuesta.getJSONArray("descripcion_alertas_sat");
            for (int i = 0; i < arr.length(); i++) {
                alertas.add(arr.optString(i));
            }
        }
        return alertas;
    }

    public int getCantidadErrores() {
        return jsonRespuesta != null ? jsonRespuesta.optInt("cantidad_errores", 0) : 0;
    }

    public String getInformacionAdicional() {
        return jsonRespuesta != null ? jsonRespuesta.optString("informacion_adicional", "") : "";
    }

    public String getUUID() {
        return jsonRespuesta != null ? jsonRespuesta.optString("uuid", "") : "";
    }

    public String getSerie() {
        return jsonRespuesta != null ? jsonRespuesta.optString("serie", "").replaceAll("\\*", "") : "";
    }

    public long getNumero() {
        return jsonRespuesta != null ? jsonRespuesta.optLong("numero", 0) : 0;
    }

    public String getXmlCertificado(boolean decodificado) {
        if (jsonRespuesta != null && jsonRespuesta.has("xml_certificado")) {
            String base64 = jsonRespuesta.optString("xml_certificado", "");
            if (decodificado) {
                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(base64);
                    return new String(decodedBytes, StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error al decodificar el XML certificado: " + e.getMessage());
                    return ""; // Retorna vacío en caso de error
                }
            } else {
                return base64;
            }
        }
        return "";
    }


    public List<String> getDescripcionErrores() {
        List<String> errores = new ArrayList<>();

        if (jsonRespuesta != null && jsonRespuesta.has("descripcion_errores")) {
            JSONArray erroresArray = jsonRespuesta.getJSONArray("descripcion_errores");

            for (int i = 0; i < erroresArray.length(); i++) {
                JSONObject errorObj = erroresArray.getJSONObject(i);

                String fuente = errorObj.optString("fuente", "N/A");
                String categoria = errorObj.optString("categoria", "N/A");
                String mensaje = errorObj.optString("mensaje_error", "Sin mensaje");

                errores.add("-> [" + fuente + "] " + categoria + ": " + mensaje);
            }
        }

        return errores;
    }

    private static String esc(String s){
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;");
    }
}
