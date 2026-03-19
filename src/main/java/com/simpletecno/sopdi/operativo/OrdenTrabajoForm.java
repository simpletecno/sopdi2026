/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class OrdenTrabajoForm extends Window {

    VerticalLayout mainLayout;
    MarginInfo  marginInfo;
    
    FormLayout ordenTrabajoFormLayout;

    Button saveBtn;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    TextField numeroOrdenTxt;
    DateField fechaDt;
    ComboBox tipoCbx;
    CheckBox incluyeMaterialesChk;
    ComboBox idexCbx;
    TextField etapaIdexTxt;
    ComboBox ordenCambioCbx;
    ComboBox solicitadoPorCbx;
    ComboBox autorizadoPorCbx;
    TextArea instruccionesTxt;
    CheckBox seAdjuntaPlanosChk;

    UI mainUI;

    int idOrdenTrabajo;

    public OrdenTrabajoForm(int idOrdenTrabajo) {
        this.idOrdenTrabajo = idOrdenTrabajo;
        this.mainUI = UI.getCurrent();
       
        setResponsive(true);
        setCaption("Ficha de Orden de Trabajo  : " + idOrdenTrabajo);
        setWidth("60%");
        setHeight("75%");
        
        marginInfo = new MarginInfo(true,true,false,true);
        
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        createFormLayout();

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveProject();
            }
        });                

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_LEFT);
        
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);        
        
        setContent(mainLayout);

        if(idOrdenTrabajo > 0) {
            fillData();
        }
    }
    
    private void createFormLayout() {
        VerticalLayout tab1Layout = new VerticalLayout();
        tab1Layout.setSpacing(true);
        tab1Layout.setMargin(true);
        tab1Layout.setWidth("100%");
        
        ordenTrabajoFormLayout  = new FormLayout();
        ordenTrabajoFormLayout.setMargin(marginInfo);
        ordenTrabajoFormLayout.setSpacing(true);
//        ordenTrabajoFormLayout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        ordenTrabajoFormLayout.setWidth("60%");

        numeroOrdenTxt = new TextField("Número : ");
        numeroOrdenTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        numeroOrdenTxt.setValue(String.valueOf(idOrdenTrabajo));
        numeroOrdenTxt.setReadOnly(true);
//        numeroOrdenTxt.setSizeFull();

        fechaDt = new DateField("Fecha :");
        fechaDt.setValue(new java.util.Date());
        fechaDt.addStyleName(ValoTheme.DATEFIELD_LARGE);
//        fechaDt.setSizeFull();
        fechaDt.setDateFormat("dd-MMM-yyyy");
        fechaDt.setEnabled(true);

        tipoCbx = new ComboBox("Tipo : ");
        tipoCbx.setSizeFull();
        tipoCbx.setNewItemsAllowed(false);
        tipoCbx.setInvalidAllowed(false);
        tipoCbx.addItem("TRABAJO");
        tipoCbx.addItem("FABRICACION");
        tipoCbx.select("TRABAJO");

        incluyeMaterialesChk = new CheckBox("Incluye materiales :");

        idexCbx = new ComboBox("IDEX");
        idexCbx.setInputPrompt("IDEX :");
        idexCbx.setDescription("IDEX");
        idexCbx.setSizeFull();
//        idexCbx.setTextInputAllowed(false);
        idexCbx.setNewItemsAllowed(false);
        idexCbx.setNullSelectionAllowed(true);
        idexCbx.setFilteringMode(FilteringMode.CONTAINS);
        idexCbx.addContainerProperty("IdCentroCosto", String.class, "");
        idexCbx.addContainerProperty("FechaInicio", String.class, "");
        idexCbx.addContainerProperty("FechaFin", String.class, "");
        idexCbx.addContainerProperty("DiasDuracion", String.class, "");

        fillIdexCbx();

        etapaIdexTxt = new TextField("Etapa Idex : ");
        etapaIdexTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        etapaIdexTxt.setValue("1/1");

        ordenCambioCbx = new ComboBox();
        ordenCambioCbx.setInputPrompt("Orden de cambio :");
        ordenCambioCbx.setDescription("Orden de cambio");
//        ordenCambioCbx.setWidth("100%");
        ordenCambioCbx.setTextInputAllowed(false);
        ordenCambioCbx.setNewItemsAllowed(false);
        ordenCambioCbx.setNullSelectionAllowed(true);

        solicitadoPorCbx = createComboPersona("Solicitado por :");

        autorizadoPorCbx = createComboPersona("Autorizado por :");

        instruccionesTxt = new TextArea("Instrucción:");
        instruccionesTxt.setWidth("35em");
        instruccionesTxt.setHeight("10em");
        instruccionesTxt.setMaxLength(1024);
        instruccionesTxt.setDescription("Ingrese hasta 1024 caracteres...");

        seAdjuntaPlanosChk = new CheckBox("Se adjunta planos :");

        ordenTrabajoFormLayout.addComponent(numeroOrdenTxt);
        ordenTrabajoFormLayout.addComponent(fechaDt);
        ordenTrabajoFormLayout.addComponent(tipoCbx);
        ordenTrabajoFormLayout.addComponent(incluyeMaterialesChk);
        ordenTrabajoFormLayout.addComponent(idexCbx);
        ordenTrabajoFormLayout.addComponent(etapaIdexTxt);
        ordenTrabajoFormLayout.addComponent(ordenCambioCbx);
        ordenTrabajoFormLayout.addComponent(solicitadoPorCbx);
        ordenTrabajoFormLayout.addComponent(autorizadoPorCbx);
        ordenTrabajoFormLayout.addComponent(instruccionesTxt);
        ordenTrabajoFormLayout.addComponent(seAdjuntaPlanosChk);

        tab1Layout.addComponent(ordenTrabajoFormLayout);
        tab1Layout.setComponentAlignment(ordenTrabajoFormLayout, Alignment.MIDDLE_CENTER);
        
        numeroOrdenTxt.focus();

        mainLayout.addComponent(tab1Layout);
        mainLayout.setComponentAlignment(tab1Layout, Alignment.TOP_CENTER);
        
    }
    
    public void fillData() {
        String queryString =  "Select * ";
        queryString += " From  orden_trabajo ";
        queryString += " Where Id = " + idOrdenTrabajo;

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                numeroOrdenTxt.setReadOnly(false);
                numeroOrdenTxt.setValue(rsRecords.getString("NumeroOrdenTrabajo"));
                numeroOrdenTxt.setReadOnly(true);
                fechaDt.setValue(rsRecords.getDate("Fecha"));
                tipoCbx.select(rsRecords.getString("Tipo"));
                incluyeMaterialesChk.setValue(rsRecords.getString("IncluyeMateriales").equals("SI"));
                idexCbx.select(rsRecords.getString("Idex"));
                etapaIdexTxt.setValue(rsRecords.getString("EtapaIdex"));
                ordenCambioCbx.select(rsRecords.getString("IdOrdenCambio"));
                solicitadoPorCbx.select(rsRecords.getString("SolicitadoPor"));
                autorizadoPorCbx.select(rsRecords.getString("AutorizadoPor"));
                instruccionesTxt.setValue(rsRecords.getString("Instrucciones"));
                seAdjuntaPlanosChk.setValue(rsRecords.getString("SeAdjuntanPlanos").equals("SI"));
             }
        } 
        catch (Exception ex) {
            Logger.getLogger(OrdenTrabajoForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de orden de trabajo : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de orden de trabajo..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    void fillIdexCbx() {

        if(idexCbx == null) {
            return;
        }

        String queryString = "SELECT PROT.IdCentroCosto, PROT.IDEX, PROT.Descripcion, PROT.FechaInicio, ";
        queryString += " PROT.FechaFin, PROT.DiasDuracion, PROT.Estatus";
        queryString += " FROM project PRO ";
        queryString += " INNER JOIN project_tarea PROT ON PROT.IdProject = PRO.IdProject";
        queryString += " WHERE PRO.Estatus = 'ACTIVO'";
//        queryString += " AND PROT.IDEX NOT IN (SELECT IDEX FROM grupo_trabajo_plan GTP WHERE GTP.IdGrupoTrabajo = " + idGrupoTrabajo + " AND GTP.FechaFinalizaAsignacion IS NULL)";

System.out.println(queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            if (rsRecords.next()) {
                do {
                    idexCbx.addItem(rsRecords.getString("Idex"));
                    idexCbx.setItemCaption(rsRecords.getString("Idex"), rsRecords.getString("IdCentroCosto") + " " + rsRecords.getString("Descripcion"));
                    idexCbx.getContainerProperty(rsRecords.getString("Idex"), "FechaInicio").setValue(rsRecords.getString("FechaInicio"));
                    idexCbx.getContainerProperty(rsRecords.getString("Idex"), "FechaFin").setValue(rsRecords.getString("FechaFin"));
                    idexCbx.getContainerProperty(rsRecords.getString("Idex"), "DiasDuracion").setValue(rsRecords.getString("DiasDuracion"));
                } while (rsRecords.next());
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE IDEX : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private ComboBox createComboPersona(String texto) {

        ComboBox comboBox = new ComboBox(texto);
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setNewItemsAllowed(true);
        comboBox.setSizeFull();
        comboBox.clear();

        comboBox.addItem("");

        String queryString = "SELECT * FROM proveedor ";
        queryString += " WHERE EsComite = 1";
        queryString += " AND   IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                comboBox.addItem(rsRecords.getString("IdProveedor"));
                comboBox.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PARTICIPANTES DE REUNIONES", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL INTENTAR BUSCAR CATALOGO DE PROVEEDORES (PARTICIPANTES DE REUNIONES)", ex1);
            ex1.printStackTrace();
        }

        return comboBox;
    }

    private void saveProject() {
        
        if(idexCbx.getValue() == null) {
            Notification.show("Por favor, ingrese el IDEX!", Notification.Type.WARNING_MESSAGE);
            idexCbx.focus();
            return;
        }

        String queryString;
        
        if(idOrdenTrabajo == 0) {
            queryString =  "INSERT INTO orden_trabajo ";
            queryString += "(IdEmpresa, IdProyecto, NumeroOrdenTrabajo, Fecha, IdCC, Idex, EtapaIdex, Tipo, IcluyeMatariales, ";
            queryString += " IdOrdenCambio, SolicitadoPor, AutorizadoPor, Instrucciones, SeAdjuntanPlanos, CreadoFechaYHora, CreadoUsuario)";
            queryString += " VALUES (";
            queryString +=  ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
            queryString += ",'"  + numeroOrdenTxt.getValue() + "'";
            queryString += ",'"  + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'"  + idexCbx.getContainerProperty(idexCbx.getValue(), "IdCentroCosto").getValue() + "'";
            queryString += ",'"  + idexCbx.getValue() + "'";
            queryString += ",'"  + etapaIdexTxt.getValue() + "'";
            queryString += ",'"  + tipoCbx.getValue() + "'";
            queryString += ",'"  + (incluyeMaterialesChk.getValue() ? "SI" : "NO") + "'";
            queryString += ", "  + ordenCambioCbx.getValue();
            queryString += ", "  + solicitadoPorCbx.getValue();
            queryString += ", "  + autorizadoPorCbx.getValue();
            queryString += ",'"  + instruccionesTxt.getValue() + "'";
            queryString += ",'"  + (seAdjuntaPlanosChk.getValue() ? "SI" : "NO") + "'";
            queryString += ",current_timestamp";
            queryString += ","   + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ")";
        }
        else {
            queryString =  "UPDATE orden_trabajo SET ";
            queryString += " Fecha = '"  + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",IdCentroCosto = '"  + idexCbx.getContainerProperty(idexCbx.getValue(), "IdCentroCosto").getValue() + "'";
            queryString += ",Idex = '"  + idexCbx.getValue() + "'";
            queryString += ",EtapaIdex = '"  + etapaIdexTxt.getValue() + "'";
            queryString += ",Tipo = '"  + tipoCbx.getValue() + "'";
            queryString += ",IncluyeMateriales = '"  + (incluyeMaterialesChk.getValue() ? "SI" : "NO") + "'";
            queryString += ",IdOrdenCambio = "  + ordenCambioCbx.getValue();
            queryString += ",SolicitadoPor = "  + solicitadoPorCbx.getValue();
            queryString += ",AutorizadoPor = "  + autorizadoPorCbx.getValue();
            queryString += ",Instrucciones = '"  + instruccionesTxt.getValue() + "'";
            queryString += ",SeAdjuntanPlanos = '"  + (seAdjuntaPlanosChk.getValue() ? "SI" : "NO") + "'";
            queryString += " WHERE Id = " + idOrdenTrabajo;
        }
System.out.println("queryString="+queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            close();

            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("OrdenTrabajoView")) {
                ((OrdenTrabajoView)(mainUI.getNavigator().getCurrentView())).llenarTablaOrdenTrabajo();
            }

        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar bodega : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }    

}