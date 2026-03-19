/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class ProjectInfoView extends VerticalLayout implements View { 
    

    public  int idProyecto = 0;
    
    FormLayout proyectoForm;

    Statement stQuery = null;
    ResultSet rsRecords = null;
        
    TextField idProyectoTxt;
    Link sitioWebLnk;
    TextField nombreTxt;
    ComboBox paisCbx;
    ComboBox  monedaCbx;
    TextField ubicacionTxt;
    TextField responsableTxt;
    TextField telefonoTxt;
    PopupDateField fechaInicioDt;
    PopupDateField fechaFinDt;
    ComboBox unidadMedidaCbx;
    NumberField montoReservaTxt;
    NumberField porcentajeEngancheTxt;
    NumberField cuotaMantenimientoTxt;
    NumberField cantidadUnidadesCotizacionTxt;

    RichTextArea caracteristicasTxt;
    Table amenidadesTable;
    TextField amenidadTxt;    
    
    Image logoImage;

    UI mainUI;
    
    public ProjectInfoView() {
        this.mainUI = UI.getCurrent();
       
        setResponsive(true);
        setWidth("100%");

        addComponent(buildTab1());
        addComponent(buildTab2());
        addComponent(buildTab3());
        
        fillAmenidadesTable();
    }
    
    private Component buildTab1() {
        VerticalLayout tab1Layout = new VerticalLayout();
        tab1Layout.setSpacing(false);
        tab1Layout.setMargin(false);
        tab1Layout.setWidth("100%");

        logoImage = new Image();
        
        HorizontalLayout pictureLayout = new HorizontalLayout();
        pictureLayout.setWidth("100%");
        pictureLayout.addComponent(logoImage);
        pictureLayout.setComponentAlignment(logoImage, Alignment.MIDDLE_CENTER);
        pictureLayout.setMargin(false);

        tab1Layout.addComponent(pictureLayout);
                
        proyectoForm  = new FormLayout();
        proyectoForm.setMargin(false);
        proyectoForm.setSpacing(false);
        proyectoForm.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        proyectoForm.setWidth("60%");

        tab1Layout.addComponent(proyectoForm);
        tab1Layout.setComponentAlignment(proyectoForm, Alignment.MIDDLE_CENTER);
        
        idProyectoTxt = new TextField("Id Proyecto : ");
//        idProyectoTxt.setWidth("8em");
        idProyectoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        idProyectoTxt.setReadOnly(true);

        nombreTxt = new TextField("Nombre : ");
        nombreTxt.setWidth("100%");

        sitioWebLnk = new Link("http://sitioweb.com", new ExternalResource("http://google.com"));
        sitioWebLnk.setTargetName("_blank");
        
        paisCbx = new ComboBox("Pais : ");
//        paisCbx.setWidth("10em");
        
        llenarComboPais();

        ubicacionTxt = new TextField("Ubicación : ");
        ubicacionTxt.setWidth("100%");

        responsableTxt = new TextField("Responsable : ");
//        responsableTxt.setWidth("10em");

        telefonoTxt = new TextField("Teléfono : ");
//        telefonoTxt.setWidth("10em");

        fechaInicioDt = new PopupDateField("Fecha inicio : ");      
        fechaInicioDt.setValue(new java.util.Date());        
        fechaInicioDt.setResolution(Resolution.DAY);
//        fechaInicioDt.setWidth("125px");
        fechaInicioDt.setDateFormat("dd/MM/yyyy");

        fechaFinDt = new PopupDateField("Fecha fin : ");        
        fechaFinDt.setValue(new java.util.Date());        
        fechaFinDt.setResolution(Resolution.DAY);
//        fechaFinDt.setWidth("125px");
        fechaFinDt.setDateFormat("dd/MM/yyyy");

        monedaCbx = new ComboBox("Moneda : ");
        monedaCbx.addItem("USD");
        monedaCbx.addItem("GTQ");
        monedaCbx.select("USD");

        unidadMedidaCbx = new ComboBox("Unidad de medida : ");
        unidadMedidaCbx.addItem("VARAS");
        unidadMedidaCbx.addItem("METROS");
        unidadMedidaCbx.select("VARAS");
        
        montoReservaTxt = new NumberField("Monto reserva : ");
        montoReservaTxt.setWidth("10em");
        montoReservaTxt.setDecimalPrecision(2);
        montoReservaTxt.setDecimalSeparator('.');
        montoReservaTxt.setValue(0d);
        montoReservaTxt.setGroupingUsed(true);
        montoReservaTxt.setGroupingSeparator(',');
        montoReservaTxt.setImmediate(true);
        montoReservaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        porcentajeEngancheTxt = new NumberField("% Enganche : ");
        porcentajeEngancheTxt.setWidth("10em");
        porcentajeEngancheTxt.setDecimalPrecision(0);
        porcentajeEngancheTxt.setDecimalSeparator('.');
        porcentajeEngancheTxt.setValue(20d);
        porcentajeEngancheTxt.setGroupingUsed(false);
        porcentajeEngancheTxt.setGroupingSeparator(',');
        porcentajeEngancheTxt.setImmediate(true);
        porcentajeEngancheTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        
        cuotaMantenimientoTxt = new NumberField("Cuota mantenimiento : ");
        cuotaMantenimientoTxt.setDecimalPrecision(2);
        cuotaMantenimientoTxt.setDecimalSeparator('.');
        cuotaMantenimientoTxt.setValue(0d);
        cuotaMantenimientoTxt.setGroupingUsed(true);
        cuotaMantenimientoTxt.setGroupingSeparator(',');
        cuotaMantenimientoTxt.setImmediate(true);
        cuotaMantenimientoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        cantidadUnidadesCotizacionTxt = new NumberField("Cantidad unidades cotización : ");
        cantidadUnidadesCotizacionTxt.setDecimalPrecision(0);
        cantidadUnidadesCotizacionTxt.setDecimalSeparator('.');
        cantidadUnidadesCotizacionTxt.setValue(1d);
        cantidadUnidadesCotizacionTxt.setGroupingUsed(true);
        cantidadUnidadesCotizacionTxt.setGroupingSeparator(',');
        cantidadUnidadesCotizacionTxt.setImmediate(true);
        cantidadUnidadesCotizacionTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        proyectoForm.addComponent(idProyectoTxt);
        proyectoForm.addComponent(nombreTxt);
        proyectoForm.addComponent(sitioWebLnk);
        proyectoForm.addComponent(paisCbx);
        proyectoForm.addComponent(monedaCbx);
        proyectoForm.addComponent(ubicacionTxt);
        proyectoForm.addComponent(responsableTxt);
        proyectoForm.addComponent(telefonoTxt);
        proyectoForm.addComponent(fechaInicioDt);
        proyectoForm.addComponent(fechaFinDt);
        proyectoForm.addComponent(unidadMedidaCbx);
        proyectoForm.addComponent(montoReservaTxt);
        proyectoForm.addComponent(porcentajeEngancheTxt);
        proyectoForm.addComponent(cuotaMantenimientoTxt);
        proyectoForm.addComponent(cantidadUnidadesCotizacionTxt);
        
        return tab1Layout;
    }
    
    private Component buildTab2() {

        VerticalLayout tab1Layout = new VerticalLayout();
        tab1Layout.setWidth("100%");
        tab1Layout.setMargin(false);
        tab1Layout.setSpacing(false);
        
        Label titleLbl = new Label("Caracteristicas");
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.addStyleName(ValoTheme.LABEL_LIGHT);
        
        tab1Layout.addComponent(titleLbl);
        tab1Layout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);
        
        caracteristicasTxt = new RichTextArea("Características del proyecto");
        caracteristicasTxt.setWidth("600px");
        caracteristicasTxt.setHeight("300px");
        caracteristicasTxt.setImmediate(true);
        caracteristicasTxt.addStyleName(ValoTheme.TEXTAREA_LARGE);
       
        tab1Layout.addComponent(caracteristicasTxt);
        tab1Layout.setComponentAlignment(caracteristicasTxt, Alignment.TOP_CENTER);
                                       
        return tab1Layout;
        
    }    

    private Component buildTab3() {

        VerticalLayout tab1Layout = new VerticalLayout();
        tab1Layout.setWidth("100%");
        tab1Layout.setMargin(false);
        tab1Layout.setSpacing(false);
        
        Label titleLbl = new Label("Amenidades");
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.addStyleName(ValoTheme.LABEL_LIGHT);
        
        tab1Layout.addComponent(titleLbl);
        tab1Layout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        amenidadTxt = new TextField("Amenidad :");
//        amenidadTxt.setWidth("50%");

        amenidadesTable = new Table("Lista de amenidades de proyecto");
        amenidadesTable.setSelectable(false);
        amenidadesTable.setEditable(false);
        amenidadesTable.setWidth("30%");
        amenidadesTable.setPageLength(5);
        amenidadesTable.addStyleName(ValoTheme.TABLE_NO_HEADER);
                
        amenidadesTable.addContainerProperty("Amenidad",  String.class, null);
                
        amenidadesTable.setColumnAlignments(Table.Align.LEFT);

        tab1Layout.addComponent(amenidadesTable);
        tab1Layout.setComponentAlignment(amenidadesTable, Alignment.TOP_CENTER);
                        
        return tab1Layout;
        
    }    

    void llenarComboPais() {
        String queryString = "Select * ";
        queryString += " From pais ";
 
        paisCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery (queryString);
                        
            while(rsRecords.next()) { //  encontrado                
                paisCbx.addItem(rsRecords.getInt("IdPais"));
                paisCbx.setItemCaption(rsRecords.getInt("IdPais"), rsRecords.getString("Nombre"));
            }
            
            if(rsRecords.first()) {
                paisCbx.select(rsRecords.getString("IdPais"));
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE PAIS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void fillData() {
        String queryString = "";
        
        queryString =  "SELECT * ";
        queryString += " FROM  proyecto ";
        queryString += " WHERE IdProyecto = " + idProyectoTxt.getValue();

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                nombreTxt.setValue(rsRecords.getString("Nombre"));
                sitioWebLnk.setResource(new ExternalResource(rsRecords.getString("SitioWeb")));
                paisCbx.select(rsRecords.getInt("IdPais"));
                monedaCbx.select(rsRecords.getString("Moneda"));
                ubicacionTxt.setValue(rsRecords.getString("Ubicacion"));
                responsableTxt.setValue(rsRecords.getString("Responsable"));
                telefonoTxt.setValue(rsRecords.getString("TelefonoResponsable"));
                sitioWebLnk.setCaption(rsRecords.getString("SitioWeb"));
                unidadMedidaCbx.select(rsRecords.getString("UnidadMedida"));
                montoReservaTxt.setValue(rsRecords.getDouble("MontoReserva"));
                porcentajeEngancheTxt.setValue(rsRecords.getDouble("PorcentajeEnganche"));
                fechaInicioDt.setValue(rsRecords.getDate("FechaInicio"));
                fechaFinDt.setValue(rsRecords.getDate("FechaFin"));
                cuotaMantenimientoTxt.setValue(rsRecords.getDouble("CuotaMantenimiento"));
                cantidadUnidadesCotizacionTxt.setValue(rsRecords.getDouble("CantidadUnidadesCotizacion"));
                
                caracteristicasTxt.setValue(rsRecords.getString("Caracteristica"));
                
                final byte docBytes[] = rsRecords.getBytes("Logo");
                StreamResource logoStreamResource = null;

                if(docBytes != null ) {
                    logoStreamResource = new StreamResource(
                        new StreamResource.StreamSource() {
                            public InputStream getStream() {
                                return new ByteArrayInputStream(docBytes);
                            }
                        },rsRecords.getString("IdProyecto")
                    );
                }
                logoImage.setSource(logoStreamResource);
                
                proyectoForm.setReadOnly(true);
                caracteristicasTxt.setReadOnly(true);
                
             }
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectInfoView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de proyectos : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de proyectos..!", Notification.Type.ERROR_MESSAGE);
        } 
        
        fillAmenidadesTable();
    }

    void fillAmenidadesTable() {
        String queryString = "SELECT * ";
        queryString += " FROM proyecto_amenidad ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId();
 
        amenidadesTable.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);
                        
            while(rsRecords.next()) { //  encontrado                
                amenidadesTable.addItem(new Object[] {rsRecords.getString("Descripcion")}, rsRecords.getInt("IdProyectoAmenidad"));
            }            
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE AMENIDADES : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub
        Page.getCurrent().setTitle("Sopdi - Información del proyecto");
        idProyecto = Integer.valueOf(((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId());
        idProyectoTxt.setReadOnly(false);
        idProyectoTxt.setValue(((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId());
        idProyectoTxt.setReadOnly(true);
        fillData();
    }
}