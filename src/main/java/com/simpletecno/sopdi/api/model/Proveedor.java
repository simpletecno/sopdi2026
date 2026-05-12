package com.simpletecno.sopdi.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Proveedor {

    private Long id;
    private String codigo;
    private String codigoAnterior;
    private String nit;
    private String tipoPersona;
    private String regimen;
    private String genero;
    private String nombre;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String apellidoCasada;
    private String nacionalidad;
    private String dpi;
    private String direccion;
    private String telefono;
    private String telefonoEmergencia;
    private String email;
    private boolean esProveedor;
    private boolean esCliente;
    private boolean esBanco;
    private boolean esAgenteRetenedorISR;
    private boolean esAgenteRetenedorIVA;
    private boolean esInstitucionFiscal;
    private boolean esInstitucionSeguroSocial;
    private boolean esSujetoRetencionDefinitivaISR;
    private boolean inhabilitado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getCodigoAnterior() { return codigoAnterior; }
    public void setCodigoAnterior(String codigoAnterior) { this.codigoAnterior = codigoAnterior; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getTipoPersona() { return tipoPersona; }
    public void setTipoPersona(String tipoPersona) { this.tipoPersona = tipoPersona; }

    public String getRegimen() { return regimen; }
    public void setRegimen(String regimen) { this.regimen = regimen; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public String getApellidoCasada() { return apellidoCasada; }
    public void setApellidoCasada(String apellidoCasada) { this.apellidoCasada = apellidoCasada; }

    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

    public String getDpi() { return dpi; }
    public void setDpi(String dpi) { this.dpi = dpi; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getTelefonoEmergencia() { return telefonoEmergencia; }
    public void setTelefonoEmergencia(String telefonoEmergencia) { this.telefonoEmergencia = telefonoEmergencia; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEsProveedor() { return esProveedor; }
    public void setEsProveedor(boolean esProveedor) { this.esProveedor = esProveedor; }

    public boolean isEsCliente() { return esCliente; }
    public void setEsCliente(boolean esCliente) { this.esCliente = esCliente; }

    public boolean isEsBanco() { return esBanco; }
    public void setEsBanco(boolean esBanco) { this.esBanco = esBanco; }

    public boolean isEsAgenteRetenedorISR() { return esAgenteRetenedorISR; }
    public void setEsAgenteRetenedorISR(boolean esAgenteRetenedorISR) { this.esAgenteRetenedorISR = esAgenteRetenedorISR; }

    public boolean isEsAgenteRetenedorIVA() { return esAgenteRetenedorIVA; }
    public void setEsAgenteRetenedorIVA(boolean esAgenteRetenedorIVA) { this.esAgenteRetenedorIVA = esAgenteRetenedorIVA; }

    public boolean isEsInstitucionFiscal() { return esInstitucionFiscal; }
    public void setEsInstitucionFiscal(boolean esInstitucionFiscal) { this.esInstitucionFiscal = esInstitucionFiscal; }

    public boolean isEsInstitucionSeguroSocial() { return esInstitucionSeguroSocial; }
    public void setEsInstitucionSeguroSocial(boolean esInstitucionSeguroSocial) { this.esInstitucionSeguroSocial = esInstitucionSeguroSocial; }

    public boolean isEsSujetoRetencionDefinitivaISR() { return esSujetoRetencionDefinitivaISR; }
    public void setEsSujetoRetencionDefinitivaISR(boolean esSujetoRetencionDefinitivaISR) { this.esSujetoRetencionDefinitivaISR = esSujetoRetencionDefinitivaISR; }

    public boolean isInhabilitado() { return inhabilitado; }
    public void setInhabilitado(boolean inhabilitado) { this.inhabilitado = inhabilitado; }
}