package com.simpletecno.sopdi.api.resource;

import com.simpletecno.sopdi.api.model.PagoYFactura;
import com.simpletecno.sopdi.api.model.PagoYFacturaResponse;
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

@Path("/pagoyfactura")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pago y Factura", description = "Contabilizar pago y factura certificada para un cliente")
@SecurityRequirement(name = "ApiKeyAuth")
public class PagoYFacturaResource {

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
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"El cuerpo de la solicitud es requerido\"}").build();
        }
        if (pago.getIdEmpresa() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"El campo IdEmpresa es obligatorio\"}").build();
        }
        if (pago.getIdProveedor() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"El campo IdProveedor es obligatorio\"}").build();
        }
        if (pago.getMonto() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"El campo Monto es obligatorio\"}").build();
        }
        if (pago.getFecha() == null || pago.getFecha().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"El campo Fecha es obligatorio\"}").build();
        }

        // Procesamiento pendiente — lógica se implementará en una fase posterior
        return Response.ok(pago).build();
    }
}
