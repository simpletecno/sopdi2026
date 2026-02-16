package com.simpletecno.sopdi.extras.infile;

import com.simpletecno.sopdi.utilerias.Utileria;

public class Receptor extends Individuo {

    public Receptor() {}

    public Receptor(String nit, String nombre, String correo, Direccion direccion) {
        this.nit = nit;
        this.nombre = nombre;
        this.setCorreo(correo);
        this.direccion = direccion;
    }

    public Receptor(String nit, String nombre, String correo, String direccion) {
        this.nit = nit;
        this.nombre = nombre;
        this. correo = (correo == null || correo.isEmpty() || correo.toUpperCase().equals("NULL")) ? "" : correo;
        this.direccion = new Direccion(
                direccion,
                Utileria.obtenerCodigoPostal(direccion),
                "Gutemala",
                "Guatemala",
                "GT"
        );
    }



    @Override
    public String getTipo() {
        return "Receptor";
    }
}
