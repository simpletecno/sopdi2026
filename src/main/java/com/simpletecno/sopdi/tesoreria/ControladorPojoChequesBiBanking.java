package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/* pojo */
public class ControladorPojoChequesBiBanking {
      
    EnvironmentVars path = new EnvironmentVars();        
    
    ArrayList<PojoChequesBiBanking> lista = new ArrayList<PojoChequesBiBanking>();

    public void crearArchivo(String filePath, ArrayList<PojoChequesBiBanking> lista) {
        FileWriter flwriter = null;
        try {
            
            flwriter = new FileWriter(filePath);            
            BufferedWriter bfwriter = new BufferedWriter(flwriter);

            for (int i = 0; i < lista.size(); i++) {

                bfwriter.write(lista.get(i).getNumeroDocumento() + "," + lista.get(i).getFecha() + "," + lista.get(i).getHaber()
                        + "," + lista.get(i).getNoCuenta() + "," + lista.get(i).getNombreCheque() + "\r\n");
            }
            
            bfwriter.close();
            
            System.out.println("Archivo creado satisfactoriamente..");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (flwriter != null) {
                try {
                    flwriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
