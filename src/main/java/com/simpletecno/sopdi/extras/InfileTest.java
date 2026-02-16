package com.simpletecno.sopdi.extras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.extras.infile.*;
import com.vaadin.ui.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class InfileTest extends Window {
    VerticalLayout mainLayout;

    Button crearBtn;
    Button anularBtn;

    UI mainUI;
    Statement stQuery = null;
    String queryString = "";

    ResultSet rsRecords1 = null;
    ResultSet rsRecords2 = null;

    public InfileTest() {
        mainUI = UI.getCurrent();

        setCaption("Ventana Miscelánea");
        center();
        setModal(true);
        setResizable(false);
        setWidth("400px");
        setHeight("300px");

        // Layout principal
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.setSpacing(true);
        setContent(content);

        // Área de texto grande
        TextArea textArea = new TextArea("Descripción:");
        textArea.setWidth("100%");
        textArea.setHeight("150px");
        content.addComponent(textArea);

        // Layout horizontal para los botones
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        Button btnAceptar = new Button("Aceptar");
        btnAceptar.addClickListener(event -> {
            textArea.setValue("");
            textArea.setValue(crear());
            Notification.show("Texto ingresado: " + textArea.getValue());
        });

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.addClickListener(event -> close());

        buttonLayout.addComponents(btnAceptar, btnCancelar);
        content.addComponent(buttonLayout);
        content.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);
    }


    public String crear(){
        String apiUrl = "https://certificador.feel.com.gt/fel/procesounificado/transaccion/v2/xml";
        String xmlPath = "C:\\Users\\josea\\Documents\\Visionara\\sopdi\\src\\main\\java\\com\\simpletecno\\sopdi\\extras\\FACTURA.xml"; // reemplazar con el XML correcto
        Emisor emsior = new Emisor();
        try {
            // Cargar el XML desde archivo (puede ser construido dinámicamente también)
            //String xmlContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(xmlPath)));

            queryString = " SELECT * FROM contabilidad_empresa c ";
            queryString += "WHERE c.IdEmpresa = 210";

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords1 = stQuery.executeQuery(queryString);

                if (rsRecords1.next()) { //  encontrado
                    emsior = new Emisor(
                            rsRecords1.getString("NIT"),
                            rsRecords1.getString("Empresa"),
                            rsRecords1.getString("CorreoContacto"),
                            new Direccion(
                                    rsRecords1.getString("Direccion"),
                                    Utileria.obtenerCodigoPostal(rsRecords1.getString("Direccion")),
                                    "Guatemala",
                                    "Guatemala",
                                    "GT"
                            ),
                            "GEN",
                            "1",
                            rsRecords1.getString("NombreCorto"),
                            rsRecords1.getString("InfileUsuarioApi"),
                            rsRecords1.getString("InfileLlaveApi"),
                            rsRecords1.getString("InfileUsuarioFirma"),
                            rsRecords1.getString("InfileLlaveFirma"),
                            rsRecords1.getString("TipoPersoneria")
                    );
                }

            } catch (Exception ex1) {
                System.out.println("Error al buscar empresa" + ex1.getMessage());
                ex1.printStackTrace();
                return "Error al buscar el emisor: " + ex1.getMessage();
            }

            InfileClient infileUitilities = new InfileClient(emsior);

            // Generar el XML usando InfileClient
            Receptor receptor = new Receptor(
                    "76365204",
                    "Jaime Alvizures",
                    "leyoalvizures4456@gmail.com",
                    new Direccion(
                            "6a. Avenida 1-20 zona 1",
                            "01001",
                            "Guatemala",
                            "Guatemala",
                            "GT"
            ));

            queryString = "SELECT * ";
            queryString += "FROM producto_venta_empresa ";
            queryString += "WHERE IdEmpresa = " + rsRecords1.getInt("IdEmpresa") + " ";
            queryString += "ORDER BY CorrelativoProducto";

            List<Producto> productos = new ArrayList<>();
            Map<Integer, Integer> frases = new HashMap<>(); //<Frase, Escenario>

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords1 = stQuery.executeQuery(queryString);

                if (rsRecords1.next()) { //  encontrado

                    queryString =  "SELECT * ";
                    queryString += "FROM producto_venta_frases ea ";
                    queryString += "WHERE CorrelativoProducto = ? "; // <-- se le asigna en el setString()

                    Producto p = new Producto(
                                    rsRecords1.getString("NombreProducto"),
                                    120.00,
                                    1,
                                    "Comentario 1",
                                    rsRecords1.getString("InfileTipo"));

                    try {

                        PreparedStatement pstQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
                        pstQuery.setInt(1, rsRecords1.getInt("CorrelativoProducto"));
                        rsRecords2 = pstQuery.executeQuery();
                        while (rsRecords2.next()){
                            frases.put(
                                    rsRecords2.getInt("Frase"),
                                    rsRecords2.getInt("Escenario"));
                        }

                        p.setFrases(frases);
                        productos.add(p);
                    }
                    catch (Exception ex) {
                        System.out.println("Error al buscar productos: " + ex.getMessage());
                        ex.printStackTrace();
                        return "Error al buscar los productos: " + ex.getMessage();
                    }
                }

            } catch (Exception ex1) {
                System.out.println("Error al buscar  empresa" + ex1.getMessage());
                ex1.printStackTrace();
                return "Error al buscar el emisor: " + ex1.getMessage();
            }

            boolean response = infileUitilities.generarDocumentoBase(
                    receptor,
                    "---------------",
                    productos,
                    "FACT",
                    "",
                    new Date(),
                    "GTQ",
                    1.00
            );

            String respuesta = response + "\n" + infileUitilities.getXmlCertificado(true);
            return respuesta;

        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }
}