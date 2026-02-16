package com.simpletecno.sopdi.extras.infile;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public abstract class Individuo {
    protected String nit;
    protected String nombre;
    protected String correo;
    protected Direccion direccion;

    private static final Pattern EMAIL_RX = Pattern.compile(
            "^(?=.{1,254}$)(?=.{1,64}@)[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+" +
                    "(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
                    "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+" +
                    "[A-Za-z]{2,}$"
    );

    public Individuo() {}

    public Individuo(String nit, String nombre, String correo, Direccion direccion) {
        this.nit = nit;
        this.nombre = nombre;
        this.correo = correo;
        this.direccion = direccion;
    }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) {
        if (correo == null || correo.isEmpty() || "NULL".equalsIgnoreCase(correo)) {
            this.correo = "";
            return;
        }
        if (!EMAIL_RX.matcher(correo).matches()) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Correo no aceptado: '{}'", correo);
            this.correo = "";
        } else {
            this.correo = correo;
        }
    }

    public Direccion getDireccion() { return direccion; }
    public void setDireccion(Direccion direccion) { this.direccion = direccion; }

    public abstract String getTipo(); // Método abstracto obligatorio en hijos
}
