package com.simpletecno.sopdi.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta del endpoint pagoyfactura")
public class PagoYFacturaResponse {

    @Schema(
        description = "Código de resultado: **0** = ocurrió un error (ver campo `mensaje`), **1** = resultado exitoso.",
        example = "1",
        allowableValues = {"0", "1"}
    )
    private Integer codigo;

    @Schema(
        description = "PDF de la factura FEL certificada por INFILE, codificado en Base64. " +
                      "Decodificar el string con Base64 para obtener el archivo PDF. " +
                      "Solo presente cuando `codigo` = 1.",
        example = "JVBERi0xLjQKJeLjz9MKMSAwIG9iago..."
    )
    private String pdfBase64;

    @Schema(description = "UUID de autorización SAT asignado por INFILE al DTE certificado", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String uuid;

    @Schema(description = "Serie del DTE certificado", example = "A")
    private String serie;

    @Schema(description = "Número correlativo del DTE certificado", example = "482")
    private Long numero;

    @Schema(description = "Mensaje descriptivo del resultado", example = "Factura certificada exitosamente")
    private String mensaje;

    public Integer getCodigo() { return codigo; }
    public void setCodigo(Integer codigo) { this.codigo = codigo; }

    public String getPdfBase64() { return pdfBase64; }
    public void setPdfBase64(String pdfBase64) { this.pdfBase64 = pdfBase64; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public Long getNumero() { return numero; }
    public void setNumero(Long numero) { this.numero = numero; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
