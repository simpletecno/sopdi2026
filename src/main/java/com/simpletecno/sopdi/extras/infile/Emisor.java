package com.simpletecno.sopdi.extras.infile;

public class Emisor extends Individuo {
    private String afiliacionIVA;
    private String codigoEstablecimiento;
    private String nombreComercial;
    private String usuarioApi;
    private String llaveApi;
    private String usuarioFirma;
    private String llaveFirma;
    private String TipoPersoneria;

    public Emisor() {}

    public Emisor(String nit,
                  String nombre,
                  String correo,
                  Direccion direccion,
                  String afiliacionIVA,
                  String codigoEstablecimiento,
                  String nombreComercial,
                  String usuarioApi,
                  String llaveApi,
                  String usuarioFirma,
                  String llaveFirma,
                  String TipoPersoneria) {
        super(nit, nombre, correo, direccion);
        this.afiliacionIVA = afiliacionIVA;
        this.codigoEstablecimiento = codigoEstablecimiento;
        this.nombreComercial = nombreComercial;
        this.usuarioApi = usuarioApi;
        this.llaveApi = llaveApi;
        this.usuarioFirma = usuarioFirma;
        this.llaveFirma = llaveFirma;
        this.TipoPersoneria = TipoPersoneria;
    }

    public String getAfiliacionIVA() { return afiliacionIVA; }
    public void setAfiliacionIVA(String afiliacionIVA) { this.afiliacionIVA = afiliacionIVA; }

    public String getCodigoEstablecimiento() { return codigoEstablecimiento; }
    public void setCodigoEstablecimiento(String codigoEstablecimiento) { this.codigoEstablecimiento = codigoEstablecimiento; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getUsuarioApi() { return usuarioApi; }
    public void setUsuarioApi(String usuarioApi) { this.usuarioApi = usuarioApi; }

    public String getLlaveApi() { return llaveApi; }
    public void setLlaveApi(String llaveApi) { this.llaveApi = llaveApi; }

    public String getUsuarioFirma() { return usuarioFirma; }
    public void setUsuarioFirma(String usuarioFirma) { this.usuarioFirma = usuarioFirma; }

    public String getLlaveFirma() { return llaveFirma; }
    public void setLlaveFirma(String llaveFirma) { this.llaveFirma = llaveFirma; }

    public String getTipoPersoneria() { return TipoPersoneria; }
    public void setTipoPersoneria(String TipoPersoneria) { this.TipoPersoneria = TipoPersoneria; }

    @Override
    public String getTipo() {
        return "Emisor";
    }
}
