package com.simpletecno.sopdi.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContabilidadPartida {

    private Long idPartida;
    private String codigoPartida;
    private String fecha;
    private String serieDocumento;
    private String numeroDocumento;
    private String tipoDocumento;
    private String monedaDocumento;
    private Double debe;
    private Double haber;
    private Double debeQuetzales;
    private Double haberQuetzales;
    private String codigoCC;
    private Long idNomenclatura;
    private Long idEmpresa;
    private String nombreProveedor;
    private String estatus;
    private Long idLiquidacion;
    private String referencia;
    private Double montoDocumento;
    private Double montoAutorizadoPagar;
    private Double montoAplicarAnticipo;
    private String archivoNombre;
    private String archivoTipo;

    public Long getIdPartida() { return idPartida; }
    public void setIdPartida(Long idPartida) { this.idPartida = idPartida; }

    public String getCodigoPartida() { return codigoPartida; }
    public void setCodigoPartida(String codigoPartida) { this.codigoPartida = codigoPartida; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getSerieDocumento() { return serieDocumento; }
    public void setSerieDocumento(String serieDocumento) { this.serieDocumento = serieDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getMonedaDocumento() { return monedaDocumento; }
    public void setMonedaDocumento(String monedaDocumento) { this.monedaDocumento = monedaDocumento; }

    public Double getDebe() { return debe; }
    public void setDebe(Double debe) { this.debe = debe; }

    public Double getHaber() { return haber; }
    public void setHaber(Double haber) { this.haber = haber; }

    public Double getDebeQuetzales() { return debeQuetzales; }
    public void setDebeQuetzales(Double debeQuetzales) { this.debeQuetzales = debeQuetzales; }

    public Double getHaberQuetzales() { return haberQuetzales; }
    public void setHaberQuetzales(Double haberQuetzales) { this.haberQuetzales = haberQuetzales; }

    public String getCodigoCC() { return codigoCC; }
    public void setCodigoCC(String codigoCC) { this.codigoCC = codigoCC; }

    public Long getIdNomenclatura() { return idNomenclatura; }
    public void setIdNomenclatura(Long idNomenclatura) { this.idNomenclatura = idNomenclatura; }

    public Long getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Long idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String nombreProveedor) { this.nombreProveedor = nombreProveedor; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public Long getIdLiquidacion() { return idLiquidacion; }
    public void setIdLiquidacion(Long idLiquidacion) { this.idLiquidacion = idLiquidacion; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public Double getMontoDocumento() { return montoDocumento; }
    public void setMontoDocumento(Double montoDocumento) { this.montoDocumento = montoDocumento; }

    public Double getMontoAutorizadoPagar() { return montoAutorizadoPagar; }
    public void setMontoAutorizadoPagar(Double montoAutorizadoPagar) { this.montoAutorizadoPagar = montoAutorizadoPagar; }

    public Double getMontoAplicarAnticipo() { return montoAplicarAnticipo; }
    public void setMontoAplicarAnticipo(Double montoAplicarAnticipo) { this.montoAplicarAnticipo = montoAplicarAnticipo; }

    public String getArchivoNombre() { return archivoNombre; }
    public void setArchivoNombre(String archivoNombre) { this.archivoNombre = archivoNombre; }

    public String getArchivoTipo() { return archivoTipo; }
    public void setArchivoTipo(String archivoTipo) { this.archivoTipo = archivoTipo; }
}