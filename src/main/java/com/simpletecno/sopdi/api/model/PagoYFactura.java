package com.simpletecno.sopdi.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Contabiliazar pago y factura venta certificada")
public class PagoYFactura {

    @Schema(description = "ID de la empresa", example = "101", required = true)
    private Long idEmpresa;

    @Schema(description = "ID del cliente", example = "4523", required = true)
    private Long idProveedor;

    @Schema(description = "NIT del cliente", example = "12345678-9")
    private String nit;

    @Schema(description = "Número de comprobante", example = "COMP-2026-000482")
    private String numeroComprobante;

    @Schema(description = "Monto total del pago", example = "4500.00", required = true)
    private Double monto;

    @Schema(description = "Fecha del pago (YYYY-MM-DD)", example = "2026-09-03", required = true)
    private String fecha;

    @Schema(description = "Lista de cuotas que componen el pago")
    private List<Cuota> cuotas;

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public Long getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Long idEmpresa) { this.idEmpresa = idEmpresa; }

    public Long getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getNumeroComprobante() { return numeroComprobante; }
    public void setNumeroComprobante(String numeroComprobante) { this.numeroComprobante = numeroComprobante; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public List<Cuota> getCuotas() { return cuotas; }
    public void setCuotas(List<Cuota> cuotas) { this.cuotas = cuotas; }

    // ─── Clase interna Cuota ─────────────────────────────────────────────────

    @Schema(description = "Cuota individual dentro de un pago")
    public static class Cuota {

        @Schema(description = "ID de la cuota", example = "8801")
        private Long idCuota;

        @Schema(description = "Número de cuota dentro del pago", example = "1")
        private Integer numeroCuota;

        @Schema(description = "Descripción o concepto de la cuota", example = "Cuota 1 de 3 - Plan mensual Komclick")
        private String concepto;

        @Schema(description = "Monto de la cuota", example = "1500.00")
        private Double monto;

        @Schema(description = "Código de nomenclatura contable", example = "5111-01")
        private String idNomenclatura;

        public Long getIdCuota() { return idCuota; }
        public void setIdCuota(Long idCuota) { this.idCuota = idCuota; }

        public Integer getNumeroCuota() { return numeroCuota; }
        public void setNumeroCuota(Integer numeroCuota) { this.numeroCuota = numeroCuota; }

        public String getConcepto() { return concepto; }
        public void setConcepto(String concepto) { this.concepto = concepto; }

        public Double getMonto() { return monto; }
        public void setMonto(Double monto) { this.monto = monto; }

        public String getIdNomenclatura() { return idNomenclatura; }
        public void setIdNomenclatura(String idNomenclatura) { this.idNomenclatura = idNomenclatura; }
    }
}
