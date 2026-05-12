package com.simpletecno.sopdi.api.resource;

import com.simpletecno.sopdi.api.DbHelper;
import com.simpletecno.sopdi.api.model.Proveedor;
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

@Path("/proveedores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Proveedores", description = "CRUD de la tabla proveedor")
@SecurityRequirement(name = "ApiKeyAuth")
public class ProveedorResource {

    // ─── GET /proveedores ────────────────────────────────────────────────────

    @GET
    @Operation(
        summary = "Listar proveedores",
        description = "Retorna todos los proveedores. Filtrado opcional por nombre o NIT.",
        parameters = {
            @Parameter(name = "buscar",  in = ParameterIn.QUERY, description = "Texto libre en Nombre o NIT"),
            @Parameter(name = "pagina",  in = ParameterIn.QUERY, description = "Número de página (desde 1)", schema = @Schema(defaultValue = "1")),
            @Parameter(name = "tamano",  in = ParameterIn.QUERY, description = "Registros por página",        schema = @Schema(defaultValue = "50"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de proveedores",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Proveedor.class))))
        }
    )
    public Response listar(
            @QueryParam("buscar") String buscar,
            @QueryParam("pagina") @DefaultValue("1")  int pagina,
            @QueryParam("tamano") @DefaultValue("50") int tamano) {

        if (pagina < 1) pagina = 1;
        if (tamano < 1 || tamano > 500) tamano = 50;
        int offset = (pagina - 1) * tamano;

        String sql = "SELECT * FROM proveedor";
        if (buscar != null && !buscar.isBlank()) {
            sql += " WHERE Nombre LIKE ? OR Nit LIKE ?";
        }
        sql += " ORDER BY Nombre LIMIT ? OFFSET ?";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            if (buscar != null && !buscar.isBlank()) {
                String like = "%" + buscar + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            ps.setInt(idx++, tamano);
            ps.setInt(idx,   offset);

            List<Proveedor> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
            return Response.ok(lista).build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    // ─── GET /proveedores/{id} ───────────────────────────────────────────────

    @GET
    @Path("/{id}")
    @Operation(
        summary = "Obtener proveedor por ID",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Proveedor.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
        }
    )
    public Response getById(@PathParam("id") long id) {
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM proveedor WHERE Id = ?")) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Response.ok(mapRow(rs)).build();
                }
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Proveedor no encontrado\"}").build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    // ─── POST /proveedores ───────────────────────────────────────────────────

    @POST
    @Operation(
        summary = "Crear proveedor",
        requestBody = @RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = Proveedor.class))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Creado",
                content = @Content(schema = @Schema(implementation = Proveedor.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
        }
    )
    public Response crear(Proveedor p) {
        if (p == null || p.getNombre() == null || p.getNombre().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"El campo nombre es obligatorio\"}").build();
        }

        String sql = "INSERT INTO proveedor " +
                "(Codigo, CodigoAnterior, Nit, TipoPersona, Regimen, Genero, Nombre, " +
                "PrimerNombre, SegundoNombre, PrimerApellido, SegundoApellido, ApellidoCasada, " +
                "Nacionalidad, Dpi, Direccion, Telefono, TelefonoEmergencia, Email, " +
                "EsProveedor, EsCliente, EsBanco, EsAgenteRetenedorISR, EsAgenteRetenedorIVA, " +
                "EsInstitucionFiscal, EsInstitucionSeguroSocial, EsAbEsSujetoRetencionDefinitivaISR, Inhabilitado) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindInsert(ps, p);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getLong(1));
                }
            }
            return Response.status(Response.Status.CREATED).entity(p).build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    // ─── PUT /proveedores/{id} ───────────────────────────────────────────────

    @PUT
    @Path("/{id}")
    @Operation(
        summary = "Actualizar proveedor",
        requestBody = @RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = Proveedor.class))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
        }
    )
    public Response actualizar(@PathParam("id") long id, Proveedor p) {
        String sql = "UPDATE proveedor SET " +
                "CodigoAnterior=?, Nit=?, TipoPersona=?, Regimen=?, Genero=?, Nombre=?, " +
                "PrimerNombre=?, SegundoNombre=?, PrimerApellido=?, SegundoApellido=?, " +
                "ApellidoCasada=?, Nacionalidad=?, Dpi=?, Direccion=?, Telefono=?, " +
                "TelefonoEmergencia=?, Email=?, EsProveedor=?, EsCliente=?, EsBanco=?, " +
                "EsAgenteRetenedorIsr=?, EsAgenteRetenedorIva=?, EsInstitucionFiscal=?, " +
                "EsInstitucionSeguroSocial=?, InHabilitado=? WHERE Id=?";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindUpdate(ps, p, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Proveedor no encontrado\"}").build();
            }
            p.setId(id);
            return Response.ok(p).build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    // ─── DELETE /proveedores/{id} ────────────────────────────────────────────

    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "Eliminar proveedor",
        responses = {
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
        }
    )
    public Response eliminar(@PathParam("id") long id) {
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM proveedor WHERE Id = ?")) {

            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Proveedor no encontrado\"}").build();
            }
            return Response.noContent().build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Proveedor mapRow(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getLong("Id"));
        p.setCodigo(rs.getString("Codigo"));
        p.setCodigoAnterior(rs.getString("CodigoAnterior"));
        p.setNit(rs.getString("Nit"));
        p.setTipoPersona(rs.getString("TipoPersona"));
        p.setRegimen(rs.getString("Regimen"));
        p.setGenero(rs.getString("Genero"));
        p.setNombre(rs.getString("Nombre"));
        p.setPrimerNombre(rs.getString("PrimerNombre"));
        p.setSegundoNombre(rs.getString("SegundoNombre"));
        p.setPrimerApellido(rs.getString("PrimerApellido"));
        p.setSegundoApellido(rs.getString("SegundoApellido"));
        p.setApellidoCasada(rs.getString("ApellidoCasada"));
        p.setNacionalidad(rs.getString("Nacionalidad"));
        p.setDpi(rs.getString("Dpi"));
        p.setDireccion(rs.getString("Direccion"));
        p.setTelefono(rs.getString("Telefono"));
        p.setTelefonoEmergencia(rs.getString("TelefonoEmergencia"));
        p.setEmail(rs.getString("Email"));
        p.setEsProveedor(rs.getInt("EsProveedor") == 1);
        p.setEsCliente(rs.getInt("EsCliente") == 1);
        p.setEsBanco(rs.getInt("EsBanco") == 1);
        p.setEsAgenteRetenedorISR(rs.getInt("EsAgenteRetenedorISR") == 1);
        p.setEsAgenteRetenedorIVA(rs.getInt("EsAgenteRetenedorIVA") == 1);
        p.setEsInstitucionFiscal(rs.getInt("EsInstitucionFiscal") == 1);
        p.setEsInstitucionSeguroSocial(rs.getInt("EsInstitucionSeguroSocial") == 1);
        p.setInhabilitado(rs.getInt("Inhabilitado") == 1);
        return p;
    }

    private void bindInsert(PreparedStatement ps, Proveedor p) throws SQLException {
        ps.setString(1,  nvl(p.getCodigo()));
        ps.setString(2,  nvl(p.getCodigoAnterior()));
        ps.setString(3,  nvl(p.getNit()));
        ps.setString(4,  nvl(p.getTipoPersona()));
        ps.setString(5,  nvl(p.getRegimen()));
        ps.setString(6,  nvl(p.getGenero()));
        ps.setString(7,  nvl(p.getNombre()));
        ps.setString(8,  nvl(p.getPrimerNombre()));
        ps.setString(9,  nvl(p.getSegundoNombre()));
        ps.setString(10, nvl(p.getPrimerApellido()));
        ps.setString(11, nvl(p.getSegundoApellido()));
        ps.setString(12, nvl(p.getApellidoCasada()));
        ps.setString(13, nvl(p.getNacionalidad()));
        ps.setString(14, nvl(p.getDpi()));
        ps.setString(15, nvl(p.getDireccion()));
        ps.setString(16, nvl(p.getTelefono()));
        ps.setString(17, nvl(p.getTelefonoEmergencia()));
        ps.setString(18, nvl(p.getEmail()));
        ps.setInt(19, p.isEsProveedor() ? 1 : 0);
        ps.setInt(20, p.isEsCliente() ? 1 : 0);
        ps.setInt(21, p.isEsBanco() ? 1 : 0);
        ps.setInt(22, p.isEsAgenteRetenedorISR() ? 1 : 0);
        ps.setInt(23, p.isEsAgenteRetenedorIVA() ? 1 : 0);
        ps.setInt(24, p.isEsInstitucionFiscal() ? 1 : 0);
        ps.setInt(25, p.isEsInstitucionSeguroSocial() ? 1 : 0);
        ps.setInt(26, p.isEsSujetoRetencionDefinitivaISR() ? 1 : 0);
        ps.setInt(27, p.isInhabilitado() ? 1 : 0);
    }

    private void bindUpdate(PreparedStatement ps, Proveedor p, long id) throws SQLException {
        ps.setString(1,  nvl(p.getCodigoAnterior()));
        ps.setString(2,  nvl(p.getNit()));
        ps.setString(3,  nvl(p.getTipoPersona()));
        ps.setString(4,  nvl(p.getRegimen()));
        ps.setString(5,  nvl(p.getGenero()));
        ps.setString(6,  nvl(p.getNombre()));
        ps.setString(7,  nvl(p.getPrimerNombre()));
        ps.setString(8,  nvl(p.getSegundoNombre()));
        ps.setString(9,  nvl(p.getPrimerApellido()));
        ps.setString(10, nvl(p.getSegundoApellido()));
        ps.setString(11, nvl(p.getApellidoCasada()));
        ps.setString(12, nvl(p.getNacionalidad()));
        ps.setString(13, nvl(p.getDpi()));
        ps.setString(14, nvl(p.getDireccion()));
        ps.setString(15, nvl(p.getTelefono()));
        ps.setString(16, nvl(p.getTelefonoEmergencia()));
        ps.setString(17, nvl(p.getEmail()));
        ps.setInt(18, p.isEsProveedor() ? 1 : 0);
        ps.setInt(19, p.isEsCliente() ? 1 : 0);
        ps.setInt(20, p.isEsBanco() ? 1 : 0);
        ps.setInt(21, p.isEsAgenteRetenedorISR() ? 1 : 0);
        ps.setInt(22, p.isEsAgenteRetenedorIVA() ? 1 : 0);
        ps.setInt(23, p.isEsInstitucionFiscal() ? 1 : 0);
        ps.setInt(24, p.isEsInstitucionSeguroSocial() ? 1 : 0);
        ps.setInt(25, p.isInhabilitado() ? 1 : 0);
        ps.setLong(26, id);
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private Response serverError(Exception e) {
        e.printStackTrace();
        return Response.serverError()
                .entity("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}")
                .build();
    }
}