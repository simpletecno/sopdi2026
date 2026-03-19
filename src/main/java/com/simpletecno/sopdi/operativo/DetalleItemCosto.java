package com.simpletecno.sopdi.operativo;

import java.util.Date;

public class DetalleItemCosto {
    int Id;
    String noCuenta;
    String descripcion;
    double precio;
    double cantidad;
    double total;
    int idProject;
    int idProveedor;
    String idcc;
    int idArea;
    int lote;
    String moneda;
    int idEmpresa;
    String empresa;
    String idTarea;
    String razonOC;
    String unidad;
    String tipo;
    java.util.Date fechaIngreso;
    int noc;
    String idex;
    String codItemProveedor;
    String idVisita;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getNoCuenta() {
        return noCuenta;
    }

    public void setNoCuenta(String noCuenta) {
        this.noCuenta = noCuenta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getIdcc() {
        return idcc;
    }

    public void setIdcc(String idcc) {
        this.idcc = idcc;
    }

    public int getIdArea() {
        return idArea;
    }

    public void setIdArea(int idArea) {
        this.idArea = idArea;
    }

    public int getLote() {
        return lote;
    }

    public void setLote(int lote) {
        this.lote = lote;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(String idTarea) {
        this.idTarea = idTarea;
    }

    public String getRazonOC() {
        return razonOC;
    }

    public void setRazonOC(String razonOC) {
        this.razonOC = razonOC;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public int getNoc() {
        return noc;
    }

    public void setNoc(int noc) {
        this.noc = noc;
    }

    public String getIdex() {
        return idex;
    }

    public void setIdex(String idex) {
        this.idex = idex;
    }

    public String getCodItemProveedor() {
        return codItemProveedor;
    }

    public void setCodItemProveedor(String codItemProveedor) {
        this.codItemProveedor = codItemProveedor;
    }

    public String getIdVisita() {
        return idVisita;
    }

    public void setIdVisita(String idVisita) {
        this.idVisita = idVisita;
    }

    @Override
    public String toString() {
        return "DetalleItemCosto{" +
                "Id=" + Id +
                ", noCuenta='" + noCuenta + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", cantidad=" + cantidad +
                ", total=" + total +
                ", idProject=" + idProject +
                ", idProveedor=" + idProveedor +
                ", idcc='" + idcc + '\'' +
                ", idArea=" + idArea +
                ", lote=" + lote +
                ", moneda='" + moneda + '\'' +
                ", idEmpresa=" + idEmpresa +
                ", empresa='" + empresa + '\'' +
                ", idTarea='" + idTarea + '\'' +
                ", razonOC='" + razonOC + '\'' +
                ", unidad='" + unidad + '\'' +
                ", tipo='" + tipo + '\'' +
                ", fechaIngreso=" + fechaIngreso +
                ", noc=" + noc +
                ", idex='" + idex + '\'' +
                ", codItemProveedor='" + codItemProveedor + '\'' +
                ", idVisita='" + idVisita + '\'' +
                '}';
    }
}