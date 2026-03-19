/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class InspectionTaskOCForm extends Window {

    final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000; //Milisegundos al día
    VerticalLayout mainLayout;
    MarginInfo marginInfo;

    FormLayout ocForm;

    Button saveBtn;
    Button salirBtn;

    Statement stQuery = null;
    ResultSet rsRecords = null;
    PreparedStatement stPreparedQuery;

    ComboBox tipoCbx;
    TextField idProjectTxt;
    TextField centroCostoTxt;
    ComboBox idexCbx;
    ComboBox noCuentaCbx;
    TextField descripcionTxt;
    ComboBox areaCbx;
    TextField loteTxt;
    TextField unidadTxt;
    NumberField cantidadTxt;
    NumberField precioTxt;
    NumberField totalTxt;
    ComboBox monedaCbx;
    ComboBox proveedorCbx;

    UI mainUI;

    IndexedContainer indexedContainer;
    Object itemObject;
    Grid grid;
    Object itemObjectDIC;
    String idOC;
    String idDIC;
    String idVisitaInspeccionTareaOcDetalle;
    String projectNumero;
    String projectId;
    boolean esNuevo;
    String idcc;
    boolean esNuevoDesdeSeleccionado;
    String lote;

    static DecimalFormat numberFormat = new DecimalFormat("0.00");

    public InspectionTaskOCForm(
            Object itemObject,
            IndexedContainer indexedContainer,
            Grid grid,
            Object itemObjectDIC,
            String idOC, // id de la orden de cambio
            String idDIC, // id DetalleItemsCostos
            String idVisitaInspeccionTareaOcDetalle, // id de nueva linea de OC para insertar en DetalleItemsCostos para modificar
            String projectNumero,
            String projectId,
            boolean esNuevo,
            String idcc,
            boolean esNuevoDesdeSeleccionado,
            String lote
    ) {
        this.itemObject = itemObject;
        this.indexedContainer = indexedContainer;
        this.grid = grid;
        this.itemObjectDIC = itemObjectDIC;
        this.idOC = idOC;
        this.idDIC = idDIC;
        this.idVisitaInspeccionTareaOcDetalle =idVisitaInspeccionTareaOcDetalle;
        this.projectNumero = projectNumero;
        this.projectId = projectId;
        this.esNuevo = esNuevo;
        this.idcc = idcc;
        this.esNuevoDesdeSeleccionado = esNuevoDesdeSeleccionado;
        this.lote = lote;

        this.mainUI = UI.getCurrent();

        setWidth("50%");

        setCaption(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Datos para detalle item costo");
        Page.getCurrent().setTitle(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Datos para detalle item costo");

        marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        ocForm = new FormLayout();
        ocForm.setWidth("30em");

        tipoCbx = new ComboBox("Tipo : ");
        tipoCbx.addItem("INTINI");
        tipoCbx.addItem("DOCA");
        tipoCbx.select("DOCA");
        tipoCbx.setNewItemsAllowed(false);
        tipoCbx.setInvalidAllowed(false);
        tipoCbx.setNullSelectionAllowed(false);
        tipoCbx.setWidth("20em");
        tipoCbx.setReadOnly(true);

        idProjectTxt = new TextField("Project : ");
        idProjectTxt.setWidth("20em");
        idProjectTxt.setValue(projectNumero);
        idProjectTxt.setData(projectId);
        idProjectTxt.setValidationVisible(false);
        idProjectTxt.addValidator(new IntegerRangeValidator("El dato debe ser un número.", 1, 100));
        idProjectTxt.setReadOnly(true);

        if(projectNumero.equals("0") || projectNumero.isEmpty() || projectId.equals("0")) {
            setCurrectActiveProject();
        }

        centroCostoTxt = new TextField("Centro costo : ");
        centroCostoTxt.setWidth("20em");
        centroCostoTxt.setValue(idcc);
        centroCostoTxt.setReadOnly(true);

        idexCbx = new ComboBox("IDEX : ");
        idexCbx.setWidth("20em");
        idexCbx.setFilteringMode(FilteringMode.CONTAINS);
        if(esNuevoDesdeSeleccionado) {
            idexCbx.setValue(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.IDEX_PROPERTY).getValue()));
            idexCbx.select(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.IDEX_PROPERTY).getValue()));
        }
        llenarComboIdex();
        if(esNuevoDesdeSeleccionado) {
            idexCbx.setValue(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.IDEX_PROPERTY).getValue()));
        }

        noCuentaCbx = new ComboBox("No. Cuenta :");
        noCuentaCbx.setWidth("20em");
        noCuentaCbx.setInvalidAllowed(false);
        noCuentaCbx.setNewItemsAllowed(false);
        noCuentaCbx.setFilteringMode(FilteringMode.CONTAINS);
        noCuentaCbx.addContainerProperty("desc", String.class, "");
        llenarComboCuentas();
        if(esNuevoDesdeSeleccionado) {
            noCuentaCbx.setValue(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.CUENTA_PROPERTY).getValue()));
        }
        noCuentaCbx.addValueChangeListener(event ->
           {
               descripcionTxt.setValue(String.valueOf(noCuentaCbx.getContainerProperty(noCuentaCbx.getValue(), "desc").getValue()));
           }
        );

        descripcionTxt = new TextField("Descripción :");
        descripcionTxt.setWidth("20em");
        if(esNuevoDesdeSeleccionado) {
            descripcionTxt.setValue(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.DESCRIPCION_PROPERTY).getValue()));
        }

        areaCbx = new ComboBox("Area : ");
        areaCbx.setWidth("20em");
        areaCbx.setFilteringMode(FilteringMode.CONTAINS);
        areaCbx.setNewItemsAllowed(false);
        areaCbx.setInvalidAllowed(false);
        areaCbx.setNullSelectionAllowed(false);
        llenarComboArea();

        loteTxt =  new TextField("Lote :");
        loteTxt.setWidth("20em");
        if(esNuevoDesdeSeleccionado) {
            loteTxt.setValue(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.LOTE_PROPERTY).getValue()));
        }
        else {
            loteTxt.setValue(lote);
        }
        loteTxt.setReadOnly(true);

        unidadTxt = new TextField("Unidad :");
        unidadTxt.setWidth("20em");
        if(esNuevoDesdeSeleccionado) {
            unidadTxt.setValue(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.UNIDAD_PROPERTY).getValue()));
        }

        cantidadTxt = new NumberField("Cantidad : ");
        cantidadTxt.setWidth("20em");
        cantidadTxt.setIcon(FontAwesome.ARROW_RIGHT);
        cantidadTxt.setRequired(true);
        cantidadTxt.setDecimalAllowed(true);
        cantidadTxt.setDecimalPrecision(2);
        cantidadTxt.setMinimumFractionDigits(2);
        cantidadTxt.setDecimalSeparator('.');
        cantidadTxt.setDecimalSeparatorAlwaysShown(true);
//        cantidadTxt.setValue(cantidad);
        cantidadTxt.setGroupingUsed(true);
        cantidadTxt.setGroupingSeparator(',');
        cantidadTxt.setGroupingSize(3);
        cantidadTxt.setImmediate(true);
        cantidadTxt.setNegativeAllowed(true);
        cantidadTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        cantidadTxt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    if (!String.valueOf(event.getProperty().getValue()).trim().isEmpty()) {
                        if(cantidadTxt != null && totalTxt != null) {
                            totalTxt.setEnabled(true);
//                            totalTxt.setValue(Utileria.round(cantidadTxt.getDoubleValueDoNotThrow() * precioTxt.getDoubleValueDoNotThrow()));
                            totalTxt.setValue(Double.parseDouble(numberFormat.format((cantidadTxt.getDoubleValueDoNotThrow() * precioTxt.getDoubleValueDoNotThrow()))));
                            totalTxt.setEnabled(false);
                        }
                    }
                }
            }
        });
        if(esNuevoDesdeSeleccionado) {
            cantidadTxt.setValue(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.CANTIDAD_PROPERTY).getValue()));
        }

        precioTxt = new NumberField("Precio : ");
        precioTxt.setIcon(FontAwesome.MONEY);
        precioTxt.setWidth("20em");
        precioTxt.setRequired(true);
        precioTxt.setDecimalAllowed(true);
        precioTxt.setDecimalPrecision(2);
        precioTxt.setMinimumFractionDigits(2);
        precioTxt.setDecimalSeparator('.');
        precioTxt.setDecimalSeparatorAlwaysShown(true);
//        precioTxt.setValue(precio);
        precioTxt.setGroupingUsed(true);
        precioTxt.setGroupingSeparator(',');
        precioTxt.setGroupingSize(3);
        precioTxt.setImmediate(true);
        precioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        precioTxt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    if (!String.valueOf(event.getProperty().getValue()).trim().isEmpty()) {
                        if(cantidadTxt != null && totalTxt != null) {
                            totalTxt.setEnabled(true);
//                            totalTxt.setValue(cantidadTxt.getDoubleValueDoNotThrow() * precioTxt.getDoubleValueDoNotThrow());
                            totalTxt.setValue(Double.parseDouble(numberFormat.format((cantidadTxt.getDoubleValueDoNotThrow() * precioTxt.getDoubleValueDoNotThrow()))));
                            totalTxt.setEnabled(false);
                        }
                    }
                }
            }
        });
        if(esNuevoDesdeSeleccionado) {
            precioTxt.setValue(Double.valueOf(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.ULT_PRECIO_PROPERTY).getValue()).replaceAll(",", "")));
        }

        totalTxt = new NumberField("Total : ");
        totalTxt.setWidth("20em");
        totalTxt.setIcon(FontAwesome.CHECK);
        totalTxt.setRequired(true);
        totalTxt.setGroupingUsed(true);
        totalTxt.setGroupingSeparator(',');
        totalTxt.setGroupingSize(3);
        totalTxt.setDecimalAllowed(true);
        totalTxt.setDecimalPrecision(2);
        totalTxt.setMinimumFractionDigits(2);
        totalTxt.setImmediate(true);
        totalTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        totalTxt.setValue(Utileria.round(cantidadTxt.getDoubleValueDoNotThrow() * precioTxt.getDoubleValueDoNotThrow()));
        totalTxt.setEnabled(false);

        monedaCbx = new ComboBox("Moneda : ");
        monedaCbx.setWidth("20em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setNewItemsAllowed(false);
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNullSelectionAllowed(false);
        if(esNuevoDesdeSeleccionado) {
            monedaCbx.select(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.MONEDA_PROPERTY).getValue()));
        }

        proveedorCbx = new ComboBox("Proveedor : ");
        proveedorCbx.setWidth("20em");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        llenarComboProveedor();
        if(esNuevoDesdeSeleccionado) {
            proveedorCbx.select(String.valueOf(indexedContainer.getContainerProperty(itemObjectDIC, InspectionTaskOCWindow.PROVEEDOR_PROPERTY).getValue()).split(" ")[0]);
        }

        ocForm.addComponent(tipoCbx);
        ocForm.addComponent(idProjectTxt);
        ocForm.addComponent(centroCostoTxt);
        ocForm.addComponent(idexCbx);
        ocForm.addComponent(noCuentaCbx);
        ocForm.addComponent(descripcionTxt);
        ocForm.addComponent(areaCbx);
        ocForm.addComponent(loteTxt);
        ocForm.addComponent(monedaCbx);
        ocForm.addComponent(unidadTxt);
        ocForm.addComponent(cantidadTxt);
        ocForm.addComponent(precioTxt);
        ocForm.addComponent(totalTxt);
        ocForm.addComponent(proveedorCbx);

        mainLayout.addComponent(ocForm);
        mainLayout.setComponentAlignment(ocForm, Alignment.MIDDLE_CENTER);

        saveBtn = new Button("Guardar");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addClickListener((Button.ClickListener)
                event -> {
                    event.getButton().setEnabled(false);
                    saveOC();
                });

        salirBtn = new Button("Salir");
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        setContent(mainLayout);

        fillData();
    }

    private void setCurrectActiveProject() {
        String queryString = "SELECT Id, Numero FROM project WHERE Estatus = 'ACTIVO'";
        queryString += " AND Id IN (SELECT IdProject FROM project_tarea WHERE IdCentroCosto = '" + idcc + "')";

        Logger .getLogger(InspectionTaskOCForm.class.getName()).log(Level.INFO, "QuerySetCurrectActiveProject()="+queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                idProjectTxt.setReadOnly(false);
                idProjectTxt.setValue(rsRecords.getString("Numero"));
                idProjectTxt.setData(rsRecords.getString("Id"));
                projectId = rsRecords.getString("Id");
                idProjectTxt.setReadOnly(true);
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PROYECTO ACTIVO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR PROYECTO ACTIVO: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboIdex() {

        idexCbx.removeAllItems();

        String queryString = "SELECT pt.IDEX, pt.Descripcion ";
        queryString += " FROM project_tarea pt";
        queryString += " INNER JOIN project p ON p.Id  = pt.IdProject";
        queryString += " WHERE  p.Estatus  = 'ACTIVO'";
        queryString += " AND pt.IdCentroCosto = '" + idcc + "'";

        Logger.getLogger(InspectionTaskOCForm.class.getName()).log(Level.INFO, "QueryLlenarComboIDex()="+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            idexCbx.addItem("<<ELIJA>>" );

            while (rsRecords.next()) { //  encontrado
                idexCbx.addItem(rsRecords.getString("Idex"));
                idexCbx.setItemCaption(rsRecords.getString("Idex"), rsRecords.getString("Idex") + " " + rsRecords.getString("Descripcion"));
            }
            idexCbx.select("<<ELIJA>>");
        }
        catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PRIMERA FECHA DE PROJECTS", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR PRIMERA FECHA DE PROJECTS: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboCuentas() {

        String queryString = "SELECT * ";
        queryString += " FROM centro_costo_cuenta ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            noCuentaCbx.addItem("0");
            noCuentaCbx.setItemCaption("0", "<<ELIJA>>");

            while (rsRecords.next()) { //  encontrado
                noCuentaCbx.addItem(rsRecords.getString("CodigoCuentaCentroCosto"));
                noCuentaCbx.setItemCaption(rsRecords.getString("CodigoCuentaCentroCosto"), rsRecords.getString("CodigoCuentaCentroCosto") + " " + rsRecords.getString("Descripcion"));
                noCuentaCbx.getContainerProperty(rsRecords.getString("CodigoCuentaCentroCosto"), "desc").setValue(rsRecords.getString("Descripcion"));
            }

            noCuentaCbx.select("0");
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CUENTAS DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboProveedor() {

        proveedorCbx.removeAllItems();

        String queryString = " SELECT *";
        queryString += " FROM proveedor_empresa";
        queryString += " WHERE Inhabilitado = '0'";
        queryString += " AND (EsProveedor = '1' Or EsRelacionada = '1')" ;
        queryString += " AND (IdEmpresa = '" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    proveedorCbx.addItem(rsRecords.getString("IdProveedor"));
                    proveedorCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("IdProveedor") + " " + rsRecords.getString("Nombre"));
                } while (rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboArea() {

        areaCbx.removeAllItems();

        String queryString = " SELECT * FROM area ORDER BY IdArea";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    areaCbx.addItem(rsRecords.getString("IdArea"));
                    areaCbx.setItemCaption(rsRecords.getString("IdArea"), rsRecords.getString("IdArea") + " " + rsRecords.getString("Descripcion"));

                } while (rsRecords.next());
            }
        } catch (Exception ex1) {
            System.out.println("Error al combo areas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void fillData() {
        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            String queryString = "";

//System.out.println("0 " + this.idVisitaInspeccionTareaOcDetalle.trim().equals("0"));

            if(this.idVisitaInspeccionTareaOcDetalle.trim().equals("0") == false) { // para actualizar oc detalle
                queryString = "SELECT OCD.*, CCC.Descripcion, CC.Lote LoteCC ";
                queryString += " FROM visita_inspeccion_tarea_oc_detalle OCD";
                queryString += " INNER JOIN centro_costo_cuenta CCC ON CCC.CodigoCuentaCentroCosto = OCD.NoCuenta";
                queryString += " INNER JOIN centro_costo CC ON CC.IdCentroCosto = OCD.IdCC";
                queryString += " WHERE OCD.IdVisitaInspeccionTareaOCDetalle = " + idVisitaInspeccionTareaOcDetalle;

//System.out.println("1 " + queryString);

                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado
                    idProjectTxt.setReadOnly(false);
                    idProjectTxt.setValue(rsRecords.getString("IdProject"));
                    idProjectTxt.setReadOnly(true);
                    centroCostoTxt.setReadOnly(false);
                    centroCostoTxt.setValue(rsRecords.getString("IdCC"));
                    centroCostoTxt.setReadOnly(true);
                    idexCbx.setReadOnly(false);
                    idexCbx.setValue(rsRecords.getString("Idex"));
                    noCuentaCbx.setValue(rsRecords.getString("NoCuenta"));
                    descripcionTxt.setValue(rsRecords.getString("Descripcion"));
                    loteTxt.setValue(rsRecords.getString("LoteCC"));
                    areaCbx.select(rsRecords.getString("IdArea"));
                    unidadTxt.setValue(rsRecords.getString("Unidad"));
                    cantidadTxt.setValue(Math.round(rsRecords.getDouble("Cantidad") * 100.0) / 100.0);
                    precioTxt.setValue(rsRecords.getDouble("Precio"));
                    totalTxt.setValue(rsRecords.getDouble("Total"));
                    monedaCbx.select(rsRecords.getString("Moneda"));
                    proveedorCbx.select(rsRecords.getString("IdProveedor"));
                }
            }
            else { //buscar por detalle item costos
                queryString = "SELECT DIC.*, CCC.Descripcion, CC.Lote LoteCC ";
                queryString += " FROM DetalleItemsCostos DIC";
                queryString += " INNER JOIN centro_costo_cuenta CCC ON CCC.CodigoCuentaCentroCosto = DIC.NoCuenta";
                queryString += " INNER JOIN centro_costo CC ON CC.IdCentroCosto = DIC.IdCC";
                queryString += " WHERE DIC.Id = " + idDIC;

System.out.println("2 " + queryString);

                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado
                    idProjectTxt.setReadOnly(false);
                    idProjectTxt.setValue(rsRecords.getString("IdProject"));
                    idProjectTxt.setReadOnly(true);
                    centroCostoTxt.setReadOnly(false);
                    centroCostoTxt.setValue(rsRecords.getString("IdCC"));
                    centroCostoTxt.setReadOnly(true);
                    idexCbx.setValue(rsRecords.getString("Idex"));
                    noCuentaCbx.select(rsRecords.getString("NoCuenta"));
                    noCuentaCbx.setValue(rsRecords.getString("NoCuenta"));
                    descripcionTxt.setValue(rsRecords.getString("Descripcion"));
                    loteTxt.setReadOnly(false);
                    loteTxt.setValue(rsRecords.getString("LoteCC"));
                    loteTxt.setReadOnly(true);
                    areaCbx.select(rsRecords.getString("IdArea"));
                    unidadTxt.setValue(rsRecords.getString("Unidad"));
                    cantidadTxt.setValue(rsRecords.getDouble("Cantidad"));
                    precioTxt.setValue(rsRecords.getDouble("Precio"));
                    totalTxt.setValue(rsRecords.getDouble("Total"));
                    monedaCbx.select(rsRecords.getString("Moneda"));
                    proveedorCbx.select(rsRecords.getString("IdProveedor"));
                }
            }

            tipoCbx.focus();
//            tipoCbx.select("INTINI");

            if(esNuevo) {
                tipoCbx.setReadOnly(true);
                idProjectTxt.setReadOnly(true);
                centroCostoTxt.setReadOnly(true);
            }
            else {
                idexCbx.setReadOnly(true);
                noCuentaCbx.setReadOnly(true);
                descripcionTxt.setReadOnly(true);
                loteTxt.setReadOnly(true);
//                areaCbx.setReadOnly(true);
                unidadTxt.setReadOnly(true);
                monedaCbx.setReadOnly(true);
                proveedorCbx.setReadOnly(true);
                cantidadTxt.focus();
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error BUSCAR DIC.",  ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void saveOC() {

        String queryString = "";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (idVisitaInspeccionTareaOcDetalle.equals("0")) {

                if (idexCbx.getValue() == null || idexCbx.getValue().equals("<<ELIJA>>")) {
                    Notification.show("Por favor elija un IDEX!", Notification.Type.ERROR_MESSAGE);
                    idexCbx.focus();
                    saveBtn.setEnabled(true);
                    return;
                }

                if (cantidadTxt.getDoubleValueDoNotThrow() == 0) {
                    Notification.show("Por favor ingrese una cantidad positiva o negativa!", Notification.Type.ERROR_MESSAGE);
                    cantidadTxt.focus();
                    saveBtn.setEnabled(true);
                    return;
                }
                if (precioTxt.getDoubleValueDoNotThrow() == 0) {
                    Notification.show("Por favor ingrese un precio mayor a 0.00 !", Notification.Type.ERROR_MESSAGE);
                    precioTxt.focus();
                    saveBtn.setEnabled(true);
                    return;
                }
                if (areaCbx.getValue() == null) {
                    Notification.show("Por favor ingrese el area !", Notification.Type.ERROR_MESSAGE);
                    areaCbx.focus();
                    saveBtn.setEnabled(true);
                    return;
                }

                queryString = "INSERT INTO visita_inspeccion_tarea_oc_detalle ";
                queryString += " (idVisitaInspeccionTareaOC, Tipo, IdProject, IdCC, Idex, NoCuenta, IdArea, Lote,";
                queryString += " Cantidad, Precio, Total, Moneda, IdEmpresa, IdProveedor) ";
                queryString += " VALUES (";
                queryString += "  " + idOC;
                queryString += ",'" + tipoCbx.getValue() + "'";
                if(esNuevoDesdeSeleccionado) {
                    queryString += "," + projectNumero;
                }
                else {
                    queryString += "," + idProjectTxt.getValue();
                }
                queryString += ",'" + centroCostoTxt.getValue() + "'";
                queryString += ",'" + idexCbx.getValue() + "'";
                queryString += ",'" + noCuentaCbx.getValue() + "'";
                queryString += ", " + areaCbx.getValue();
                queryString += ",'" + loteTxt.getValue() + "'";
                queryString += ", " + cantidadTxt.getDoubleValueDoNotThrow();
                queryString += ", " + precioTxt.getDoubleValueDoNotThrow();
                queryString += ", " + (cantidadTxt.getDoubleValueDoNotThrow() * precioTxt.getDoubleValueDoNotThrow());
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                queryString += ", " + proveedorCbx.getValue();
                queryString += ")";
            }
            else {
                if (totalTxt.getDoubleValueDoNotThrow() != 0.00) {
                    queryString = "UPDATE visita_inspeccion_tarea_oc_detalle SET ";
                    queryString += " Cantidad = " + cantidadTxt.getDoubleValueDoNotThrow();
                    queryString += ",Precio   = " + precioTxt.getDoubleValueDoNotThrow();
                    queryString += ",Total    = " + (cantidadTxt.getDoubleValueDoNotThrow() * precioTxt.getDoubleValueDoNotThrow());
                    queryString += " WHERE idVisitaInspeccionTareaOcDetalle  = " + idVisitaInspeccionTareaOcDetalle;
                } else {
                    queryString = "DELETE FROM visita_inspeccion_tarea_oc_detalle";
                    queryString += " WHERE idVisitaInspeccionTareaOcDetalle  = " + idVisitaInspeccionTareaOcDetalle;
                }
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString TAREA OC = " + queryString);

            if(idVisitaInspeccionTareaOcDetalle.equals("0")) {
                stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                stPreparedQuery.executeUpdate();
                rsRecords = stPreparedQuery.getGeneratedKeys();

                rsRecords.next();

                idVisitaInspeccionTareaOcDetalle = rsRecords.getString(1);
            }
            else {
                stQuery.executeUpdate(queryString);
            }

            if(cantidadTxt.getDoubleValueDoNotThrow() == 0) {
                precioTxt.setValue(0.00);
                totalTxt.setValue(0.00);
            }

            if(!esNuevo) {
                indexedContainer.getContainerProperty(itemObject, InspectionTaskOCWindow.OC_CANTIDAD_PROPERTY).setValue(cantidadTxt.getValue());
                indexedContainer.getContainerProperty(itemObject, InspectionTaskOCWindow.OC_PRECIO_PROPERTY).setValue(precioTxt.getValue());
                indexedContainer.getContainerProperty(itemObject, InspectionTaskOCWindow.OC_TOTAL_PROPERTY).setValue(totalTxt.getValue());
                indexedContainer.getContainerProperty(itemObject, InspectionTaskOCWindow.CODIGOOC_PROPERTY).setValue(idVisitaInspeccionTareaOcDetalle);

                if (totalTxt.getDoubleValueDoNotThrow() == 0.00) {
                    grid.deselect(itemObject);
                } else {
                    grid.select(itemObject);
                }
            }

/////            ((SopdiUI) UI.getCurrent()).getTasaCambioDelDia(Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()));

            // Actualizar la orden de cambio

            queryString = "UPDATE visita_inspeccion_tarea_oc SET ";
            queryString += " Total = (SELECT SUM(Total) FROM visita_inspeccion_tarea_oc_detalle WHERE Moneda = 'QUETZALES' AND IdVisitaInspeccionTareaOC  = " + idOC + ")";
            queryString += ",TotalDolares = IFNULL((SELECT SUM(Total) FROM visita_inspeccion_tarea_oc_detalle WHERE Moneda = 'DOLARES' AND IdVisitaInspeccionTareaOC  = " + idOC + "), 0)";
            queryString += " WHERE IdVisitaInspeccionTareaOC  = " + idOC;

            stQuery.executeUpdate(queryString);

            java.util.Date fechaDt = new java.util.Date();

            // Obtener contabilidad_tasa_cambio del día de la orden de cambio
            queryString = "SELECT Tasa FROM contabilidad_tasa_cambio WHERE Fecha = '" + Utileria.getFechaYYYYMMDD_1(fechaDt) + "'";

            double tasaCambio = 1.00;

            stQuery.execute(queryString);
            rsRecords = stQuery.getResultSet();

            if (rsRecords.next()) {
                tasaCambio = rsRecords.getDouble("Tasa");
            }
            else {
                // Obtener contabilidad_tasa_cambio del día de la orden de cambio
                queryString = "SELECT Tasa FROM contabilidad_tasa_cambio WHERE Fecha = '" + Utileria.getFechaYYYYMMDD_1(Utileria.getYesterday(fechaDt)) + "'";
                stQuery.execute(queryString);
                rsRecords = stQuery.getResultSet();

                if (rsRecords.next()) {
                    tasaCambio = rsRecords.getDouble("Tasa");
                }
                else {
                    Notification.show("No se encontró tasa de cambio del día de hoy ni de ayer, se usará 1.00", Notification.Type.WARNING_MESSAGE);
                }
            }

            System.out.println("tasaCambio=" + tasaCambio);

            // Actualizar el total en dolares
            queryString = "UPDATE visita_inspeccion_tarea_oc SET TotalDolares = IFNULL((TotalDolares + (Total / " + tasaCambio + ")),0)";
            queryString += " WHERE IdVisitaInspeccionTareaOC  = " + idOC;

            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

        } catch (Exception ex) {
            Notification.show("Error al actualizar tarea con OC : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL REGISTRAR O MODIFICAR TAREA CON OC. ", ex);
        }

        close();
    }
}
