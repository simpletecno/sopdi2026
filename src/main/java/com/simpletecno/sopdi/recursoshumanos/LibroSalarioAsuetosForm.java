package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class LibroSalarioAsuetosForm extends Window {
    int anio;

    UI mainUI;

    VerticalLayout mainLayout;
    HorizontalLayout headerLayout;
    HorizontalLayout footerLayout;

    Label empresalbl;
    Label anioLbl;

    Button guardarBtn;

    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    List<Asueto> asuetoList = new ArrayList<>();

    public LibroSalarioAsuetosForm(int anio){
        this.anio = anio;

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("65%");
        setHeight("80%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setMargin(false);
        mainLayout.setWidth("100%");

        crearHeader();

        fillWindow();

        crearFooter();

        setContent(mainLayout);
    }

    private void crearHeader(){
        headerLayout = new HorizontalLayout();
        headerLayout.setSpacing(true);
        headerLayout.setResponsive(true);
        headerLayout.setMargin(false);
        headerLayout.setWidth("95%");

        empresalbl = new Label(((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanySmallName());
        empresalbl.addStyleName(ValoTheme.LABEL_H2);
        empresalbl.setResponsive(true);
        empresalbl.setWidth("50%");
        empresalbl.setContentMode(ContentMode.HTML);

        anioLbl = new Label(anio+"");
        anioLbl.addStyleName(ValoTheme.LABEL_H2);
        anioLbl.setResponsive(true);
        anioLbl.setWidth("50%");
        anioLbl.setContentMode(ContentMode.HTML);

        headerLayout.addComponents(empresalbl, anioLbl);
        headerLayout.setComponentAlignment(empresalbl, Alignment.MIDDLE_CENTER);
        headerLayout.setComponentAlignment(anioLbl, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(headerLayout);
        mainLayout.setComponentAlignment(headerLayout, Alignment.TOP_CENTER);
    }

    private void crearFooter(){
        footerLayout = new HorizontalLayout();
        footerLayout.setSpacing(true);
        footerLayout.setResponsive(true);
        footerLayout.setMargin(true);
        footerLayout.setWidth("95%");

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.FILE_PDF_O);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);

        guardarBtn.addClickListener(clickEvent -> {
            actualizarFechas();
        });

        footerLayout.addComponents(guardarBtn);
        footerLayout.setComponentAlignment(guardarBtn, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(footerLayout);
        mainLayout.setComponentAlignment(footerLayout, Alignment.BOTTOM_CENTER);
    }

    private DateField creatEditor (String nombre, Date fecha){
        HorizontalLayout editorLayout = new HorizontalLayout();
        editorLayout.setSpacing(true);
        editorLayout.setResponsive(true);
        editorLayout.setMargin(false);
        editorLayout.setWidth("80%");
        editorLayout.addStyleName("rcorners3");

        Label nombreLbl = new Label(nombre + ": ");

        DateField fechaOriginal = new DateField("Fecha Original");
        fechaOriginal.setWidth("100%");
        fechaOriginal.setDateFormat("dd/MM/yyyy");
        fechaOriginal.setValue(new java.util.Date());
        fechaOriginal.setValue(fecha);
        fechaOriginal.setEnabled(false);

        DateField fechaNueva = new DateField("Fecha Nueva");
        fechaNueva.setWidth("100%");
        fechaNueva.setDateFormat("dd/MM/yyyy");
        fechaNueva.setValue(new java.util.Date());
        fechaNueva.setValue(fecha);

        Label cambioLbl = new Label("");
        cambioLbl.setWidth("50%");

        fechaNueva.addValueChangeListener(valueChangeEvent -> {
            if(fechaNueva.getValue().equals(fechaOriginal.getValue())){
                cambioLbl.setValue("");
            } else {
                cambioLbl.setValue("Cambio");
            }
        });

        editorLayout.addComponents(nombreLbl, fechaOriginal, fechaNueva, cambioLbl);
        editorLayout.setComponentAlignment(nombreLbl, Alignment.MIDDLE_LEFT);
        editorLayout.setComponentAlignment(fechaOriginal, Alignment.MIDDLE_CENTER);
        editorLayout.setComponentAlignment(fechaOriginal, Alignment.MIDDLE_CENTER);
        editorLayout.setComponentAlignment(cambioLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(editorLayout);
        mainLayout.setComponentAlignment(editorLayout, Alignment.MIDDLE_CENTER);

        return fechaNueva;
    }

    private void actualizarFechas(){
        for (Asueto asueto : asuetoList){
            String fecha = Utileria.getFechaYYYYMMDD_1(asueto.fecha.getValue());
            if (!fecha.substring(0, 4).equals(anio + "")){
                Notification.show("Error, Asueto \"" + asueto.descripcion + "\"\ntiene año " + fecha.substring(0, 4) + " tiene que ser " + anio, Notification.Type.WARNING_MESSAGE);
                break;
            }
            queryString =  "UPDATE asueto_fecha SET ";
            queryString += "Fecha = '" + fecha + "', ";
            queryString += "Anio = " + fecha.substring(0, 4) + ", ";
            queryString += "Mes = " + fecha.substring(5, 7) + ", ";
            queryString += "Dia = " + fecha.substring(8, 10) + " ";
            queryString += "WHERE Id = " + asueto.id;

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);
            } catch (Exception ex1) {
                System.out.println("Error al Actualizar Asueto" + ex1.getMessage());
                ex1.printStackTrace();
            }
        }

        Notification.show("Modificaciones Exitosas", Notification.Type.HUMANIZED_MESSAGE);
        close();
    }

    private void fillWindow(){
        queryString = "SELECT af.Fecha, ae.Descripcion, af.Id ";
        queryString += "FROM asueto_empresa ae ";
        queryString += "INNER JOIN asueto_fecha af ON af.IdAsueto = ae.Id AND af.Anio = " + anio + " ";
        queryString += "WHERE ae.IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
        queryString += "ORDER BY af.Mes";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()){
                do{
                    DateField dateField = creatEditor(rsRecords.getString("Descripcion"), rsRecords.getDate("Fecha"));
                    asuetoList.add(new Asueto(rsRecords.getInt("Id"), dateField, rsRecords.getString("Descripcion")));
                }while ((rsRecords.next()));

            }
        } catch (Exception ex1) {
            System.out.println("Error al listar Años " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    static class Asueto{
        public int id;
        public DateField fecha;
        public String descripcion;

        public Asueto(int id, DateField fecha, String descripcion){
            this.id = id;
            this.fecha = fecha;
            this.descripcion = descripcion;
        }
    }
}
