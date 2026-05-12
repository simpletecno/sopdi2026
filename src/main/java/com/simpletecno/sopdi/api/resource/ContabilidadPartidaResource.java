package com.simpletecno.sopdi.api.resource;

import com.simpletecno.sopdi.api.DbHelper;
import com.simpletecno.sopdi.api.model.ContabilidadPartida;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Path("/contabilidad-partidas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Contabilidad Partidas", description = "CRUD de la tabla contabilidad_partida")
@SecurityRequirement(name = "ApiKeyAuth")
public class ContabilidadPartidaResource {

    // ─── GET /contabilidad-partidas ──────────────────────────────────────────

    @GET
    @Operation(
        summary = "Listar partidas contables",
        description = "Retorna partidas con filtros opcionales por empresa, estatus y rango de fechas.",
        parameters = {
            @Parameter(name = "idEmpresa",   in = ParameterIn.QUERY, description = "ID de la empresa"),
            @Parameter(name = "estatus",     in = ParameterIn.QUERY, description = "Estatus: INGRESADO, REVISADO, ANULADO"),
            @Parameter(name = "fechaDesde",  in = ParameterIn.QUERY, description = "Fecha inicio (YYYY-MM-DD)"),
            @Parameter(name = "fechaHasta",  in = ParameterIn.QUERY, description = "Fecha fin   (YYYY-MM-DD)"),
            @Parameter(name = "codigoCC",    in = ParameterIn.QUERY, description = "Código de cuenta corriente"),
            @Parameter(name = "pagina",      in = ParameterIn.QUERY, schema = @Schema(defaultValue = "1")),
            @Parameter(name = "tamano",      in = ParameterIn.QUERY, schema = @Schema(defaultValue = "50"))
        },
        responses = {
            @ApiResponse(responseCode = "200",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContabilidadPartida.class))))
        }
    )
    public Response listar(
            @QueryParam("idEmpresa")  Long   idEmpresa,
            @QueryParam("estatus")    String estatus,
            @QueryParam("fechaDesde") String fechaDesde,
            @QueryParam("fechaHasta") String fechaHasta,
            @QueryParam("codigoCC")   String codigoCC,
            @QueryParam("pagina")  @DefaultValue("1")  int pagina,
            @QueryParam("tamano")  @DefaultValue("50") int tamano) {

        if (pagina < 1) pagina = 1;
        if (tamano < 1 || tamano > 500) tamano = 50;
        int offset = (pagina - 1) * tamano;

        StringBuilder sql = new StringBuilder("SELECT * FROM contabilidad_partida WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (idEmpresa != null) {
            sql.append(" AND IdEmpresa = ?");
            params.add(idEmpresa);
        }
        if (estatus != null && !estatus.isBlank()) {
            sql.append(" AND Estatus = ?");
            params.add(estatus.toUpperCase());
        }
        if (fechaDesde != null && !fechaDesde.isBlank()) {
            sql.append(" AND Fecha >= ?");
            params.add(fechaDesde);
        }
        if (fechaHasta != null && !fechaHasta.isBlank()) {
            sql.append(" AND Fecha <= ?");
            params.add(fechaHasta);
        }
        if (codigoCC != null && !codigoCC.isBlank()) {
            sql.append(" AND CodigoCC = ?");
            params.add(codigoCC);
        }
        sql.append(" ORDER BY Fecha DESC, IdPartida DESC LIMIT ? OFFSET ?");
        params.add(tamano);
        params.add(offset);

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object val = params.get(i);
                if (val instanceof Long)    ps.setLong(i + 1, (Long) val);
                else if (val instanceof Integer) ps.setInt(i + 1, (Integer) val);
                else ps.setString(i + 1, val.toString());
            }

            List<ContabilidadPartida> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
            return Response.ok(lista).build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    // ─── GET /contabilidad-partidas/{id} ────────────────────────────────────

    @GET
    @Path("/{id}")
    @Operation(
        summary = "Obtener partida por ID",
        responses = {
            @ApiResponse(responseCode = "200",
                content = @Content(schema = @Schema(implementation = ContabilidadPartida.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada")
        }
    )
    public Response getById(@PathParam("id") long id) {
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM contabilidad_partida WHERE IdPartida = ?")) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Response.ok(mapRow(rs)).build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Partida no encontrada\"}").build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    // ─── POST /contabilidad-partidas ─────────────────────────────────────────

    @POST
    @Operation(
        summary = "Crear partida contable",
        requestBody = @RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = ContabilidadPartida.class))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
        }
    )
    public Response crear(ContabilidadPartida cp) {
        if (cp == null || cp.getCodigoPartida() == null || cp.getCodigoPartida().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"El campo codigoPartida es obligatorio\"}").build();
        }

        String sql = "INSERT INTO contabilidad_partida " +
                "(CodigoPartida, Fecha, SerieDocumento, NumeroDocumento, TipoDocumento, " +
                "MonedaDocumento, Debe, Haber, DebeQuetzales, HaberQuetzales, CodigoCC, " +
                "IdNomenclatura, IdEmpresa, NombreProveedor, Estatus, IdLiquidacion, " +
                "Referencia, MontoDocumento, MontoAutorizadoPagar, MontoAplicarAnticipo) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindInsert(ps, cp);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cp.setIdPartida(keys.getLong(1));
            }
            return Response.status(Response.Status.CREATED).entity(cp).build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    // ─── PUT /contabilidad-partidas/{id} ────────────────────────────────────

    @PUT
    @Path("/{id}")
    @Operation(
        summary = "Actualizar partida contable",
        description = "Solo se permite actualizar el Estatus y los montos de autorización/anticipo.",
        requestBody = @RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = ContabilidadPartida.class))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Actualizada"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
        }
    )
    public Response actualizar(@PathParam("id") long id, ContabilidadPartida cp) {
        String sql = "UPDATE contabilidad_partida SET " +
                "Estatus = ?, MontoAutorizadoPagar = ?, MontoAplicarAnticipo = ?, " +
                "Referencia = ?, NombreProveedor = ?, Debe = ?, Haber = ?, " +
                "DebeQuetzales = ?, HaberQuetzales = ? WHERE IdPartida = ?";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nvl(cp.getEstatus()));
            ps.setDouble(2, dbl(cp.getMontoAutorizadoPagar()));
            ps.setDouble(3, dbl(cp.getMontoAplicarAnticipo()));
            ps.setString(4, nvl(cp.getReferencia()));
            ps.setString(5, nvl(cp.getNombreProveedor()));
            ps.setDouble(6, dbl(cp.getDebe()));
            ps.setDouble(7, dbl(cp.getHaber()));
            ps.setDouble(8, dbl(cp.getDebeQuetzales()));
            ps.setDouble(9, dbl(cp.getHaberQuetzales()));
            ps.setLong(10, id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Partida no encontrada\"}").build();
            }
            cp.setIdPartida(id);
            return Response.ok(cp).build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ContabilidadPartida mapRow(ResultSet rs) throws SQLException {
        ContabilidadPartida cp = new ContabilidadPartida();
        cp.setIdPartida(rs.getLong("IdPartida"));
        cp.setCodigoPartida(rs.getString("CodigoPartida"));

        Date fecha = rs.getDate("Fecha");
        if (fecha != null) cp.setFecha(fecha.toString());

        cp.setSerieDocumento(rs.getString("SerieDocumento"));
        cp.setNumeroDocumento(rs.getString("NumeroDocumento"));
        cp.setTipoDocumento(rs.getString("TipoDocumento"));
        cp.setMonedaDocumento(rs.getString("MonedaDocumento"));
        cp.setDebe(rs.getDouble("Debe"));
        cp.setHaber(rs.getDouble("Haber"));
        cp.setDebeQuetzales(rs.getDouble("DebeQuetzales"));
        cp.setHaberQuetzales(rs.getDouble("HaberQuetzales"));
        cp.setCodigoCC(rs.getString("CodigoCC"));
        cp.setIdNomenclatura(rs.getLong("IdNomenclatura"));
        cp.setIdEmpresa(rs.getLong("IdEmpresa"));
        cp.setNombreProveedor(rs.getString("NombreProveedor"));
        cp.setEstatus(rs.getString("Estatus"));
        cp.setIdLiquidacion(rs.getLong("IdLiquidacion"));
        cp.setReferencia(rs.getString("Referencia"));
        cp.setMontoDocumento(rs.getDouble("MontoDocumento"));
        cp.setMontoAutorizadoPagar(rs.getDouble("MontoAutorizadoPagar"));
        cp.setMontoAplicarAnticipo(rs.getDouble("MontoAplicarAnticipo"));
        cp.setArchivoNombre(rs.getString("ArchivoNombre"));
        cp.setArchivoTipo(rs.getString("ArchivoTipo"));
        return cp;
    }

    private void bindInsert(PreparedStatement ps, ContabilidadPartida cp) throws SQLException {
        ps.setString(1, nvl(cp.getCodigoPartida()));
        ps.setString(2, cp.getFecha() != null ? cp.getFecha() : null);
        ps.setString(3, nvl(cp.getSerieDocumento()));
        ps.setString(4, nvl(cp.getNumeroDocumento()));
        ps.setString(5, nvl(cp.getTipoDocumento()));
        ps.setString(6, nvl(cp.getMonedaDocumento()));
        ps.setDouble(7, dbl(cp.getDebe()));
        ps.setDouble(8, dbl(cp.getHaber()));
        ps.setDouble(9, dbl(cp.getDebeQuetzales()));
        ps.setDouble(10, dbl(cp.getHaberQuetzales()));
        ps.setString(11, nvl(cp.getCodigoCC()));
        if (cp.getIdNomenclatura() != null) ps.setLong(12, cp.getIdNomenclatura()); else ps.setNull(12, Types.BIGINT);
        if (cp.getIdEmpresa()      != null) ps.setLong(13, cp.getIdEmpresa());      else ps.setNull(13, Types.BIGINT);
        ps.setString(14, nvl(cp.getNombreProveedor()));
        ps.setString(15, cp.getEstatus() != null ? cp.getEstatus() : "INGRESADO");
        ps.setLong(16, cp.getIdLiquidacion() != null ? cp.getIdLiquidacion() : 0L);
        ps.setString(17, nvl(cp.getReferencia()));
        ps.setDouble(18, dbl(cp.getMontoDocumento()));
        ps.setDouble(19, dbl(cp.getMontoAutorizadoPagar()));
        ps.setDouble(20, dbl(cp.getMontoAplicarAnticipo()));
    }

    private String nvl(String s)  { return s == null ? "" : s; }
    private double dbl(Double d)  { return d == null ? 0.0 : d; }

    private Response serverError(Exception e) {
        e.printStackTrace();
        return Response.serverError()
                .entity("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}")
                .build();
    }
}