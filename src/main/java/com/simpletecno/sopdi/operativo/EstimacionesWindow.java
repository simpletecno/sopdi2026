/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Grid.MultiSelectionModel;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class EstimacionesWindow extends Window {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;
    public Statement stQuery1 = null;
    public ResultSet rsRecords1 = null;

    VerticalLayout mainLayout;

    //header
    protected static final String CODIGO_PROPERTY        = "Id";
    protected static final String CCOSTO_PROPERTY        = "CCosto";
    protected static final String PROJECT_PROPERTY       = "Project";
    protected static final String IDEX_PROPERTY          = "IDEX";
    protected static final String MONEDA_PROPERTY        = "Moneda";
    protected static final String TOTAL_PROPERTY         = "Total";
    
    //detalle DetalleItemCostos
    protected static final String CUENTA_PROPERTY        = "Cuenta";
    protected static final String DESCRIPCION_PROPERTY   = "Descripción";
    protected static final String CANTIDAD_PROPERTY      = "Cantidad";
    protected static final String PRECIO_PROPERTY        = "Precio";
    protected static final String PROVISION_PROPERTY     = "Provision";
    protected static final String TOTALEST_PROPERTY      = "TotalEst";
    protected static final String LOTE_PROPERTY          = "Lote";
    protected static final String TOTALSF_PROPERTY       = "TSF";
    protected static final String PROVISIONSF_PROPERTY   = "PSF";
    protected static final String TOTALESTSF_PROPERTY    = "TESF";

    Label estimacionIdLbl;
    Label titleLbl;
    
    Utileria utileria = new Utileria();
    MarginInfo marginInfo;

    public IndexedContainer fromContainer = new IndexedContainer();
    public IndexedContainer toContainer = new IndexedContainer();
    Grid fromGrid, toGrid;
    MultiSelectionModel selectionFrom, selectionTo;
    FooterRow fromGridFooter, toGridFooter;

    ComboBox proveedorCbx;

    Button searchBtn;
    Button moveBtn;
    Button moveBackBtn;
    Button saveBtn;
    Button verificadaBtn;
    Button aprobadaBtn;
    Button facturaBtn;
    Button exitBtn;

    static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    static DecimalFormat moneyFormat = new DecimalFormat("#,###,##0.00");
    
    String estimacionId = "0";
    String empresa = "";
    String empresaNombre = "";
    String proveedorId = "0";
    String proveedorNombre = "";
    String estatus = "NUEVA";
          
    UI mainUI;
    
    public EstimacionesWindow(String empresa) {
        this.mainUI = UI.getCurrent();
        this.empresa = empresa;
        
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        marginInfo = new MarginInfo(true, true, false, true);

        proveedorCbx = new ComboBox("Proveedor");
        proveedorCbx.setWidth("22em");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.addValueChangeListener(event -> {
            if(estimacionIdLbl != null) {
                estimacionIdLbl.setValue(getNextEstimacion());
            }
        });

        llenarComboProveedor();

        estimacionIdLbl = new Label(estimacionId);
        estimacionIdLbl.addStyleName(ValoTheme.LABEL_SUCCESS);
  //      estimacionIdLbl.setSizeUndefined();
        estimacionIdLbl.addStyleName("h1_custom");
        
        titleLbl = new Label("Detalle estimación " + estatus);
        if(estimacionId.equals("0")) { // nueva estimacion
            estimacionIdLbl.setValue(getNextEstimacion());
        }        
        
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h3_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
//        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl, estimacionIdLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);
        titleLayout.setComponentAlignment(estimacionIdLbl, Alignment.TOP_LEFT);

        searchBtn = new Button("IDEXs");
        searchBtn.setDescription("Consultar IDEXs");
        searchBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        searchBtn.setIcon(FontAwesome.SEARCH);
        searchBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillTable();
            }
        });

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setWidth("100%");
        filterLayout.setMargin(new MarginInfo(false, true, false, true));
        
        filterLayout.addComponents(proveedorCbx);
        filterLayout.setComponentAlignment(proveedorCbx, Alignment.TOP_CENTER);
        filterLayout.addComponents(searchBtn);
        filterLayout.setComponentAlignment(searchBtn, Alignment.BOTTOM_CENTER);

        titleLayout.addComponents(filterLayout);
        titleLayout.setComponentAlignment(filterLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        setContent(mainLayout);
        
        createGrids();
        createButtons();
        
        if(!estatus.equals("NUEVA")) {
            fillTable();
        }

    }

    private void llenarComboProveedor() {
        String queryString  = " SELECT * from proveedor ";
        queryString += " WHERE EsProveedor = 1";
        queryString += " AND Inhabilitado = 0";

        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("PROVEEDOR")) {
            queryString += " And IdProveedor = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserSpecialCode();
        }
        queryString += " Order By Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {              
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
            }
            
            proveedorCbx.select(proveedorCbx.getItemIds().iterator().next());

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
                
    }

    private void createGrids() {

        Responsive.makeResponsive(this);
        
//        setWidth("85%");
//        setHeight("98%");

        fromContainer.addContainerProperty(CODIGO_PROPERTY,         Integer.class, null);
        fromContainer.addContainerProperty(CCOSTO_PROPERTY,         String.class, null);
        fromContainer.addContainerProperty(IDEX_PROPERTY,           String.class, null);
        fromContainer.addContainerProperty(PROJECT_PROPERTY,        String.class, null);
        fromContainer.addContainerProperty(MONEDA_PROPERTY,         String.class, null);
        fromContainer.addContainerProperty(TOTALEST_PROPERTY,       String.class, null);
        fromContainer.addContainerProperty(TOTALESTSF_PROPERTY,     Double.class, null);
        
        toContainer.addContainerProperty(CODIGO_PROPERTY,         Integer.class, null);
        toContainer.addContainerProperty(CCOSTO_PROPERTY,         String.class, null);
        toContainer.addContainerProperty(IDEX_PROPERTY,           String.class, null);
        toContainer.addContainerProperty(PROJECT_PROPERTY,        String.class, null);
        toContainer.addContainerProperty(MONEDA_PROPERTY,         String.class, null);
        toContainer.addContainerProperty(TOTALEST_PROPERTY,       String.class, null);
        toContainer.addContainerProperty(TOTALESTSF_PROPERTY,     Double.class, null);

        fromGrid = new Grid("Por elegir", fromContainer);
        toGrid = new Grid("Electos", toContainer);

        if(!estatus.equals("PAGADA") && !estatus.equals("APROBADA")) {  //estimacion pendiente de pago, se pueden seleccionar IDEX
            fromGrid.setSelectionMode(Grid.SelectionMode.MULTI);
            fromGrid.setDescription("Seleccione uno o varios registros.");
            selectionFrom = (MultiSelectionModel) fromGrid.getSelectionModel();

            fromGrid.addSelectionListener(selectionEvent -> { // Java 8
                setTotal();
            });

            toGrid.setSelectionMode(Grid.SelectionMode.MULTI);
            toGrid.setDescription("Seleccione uno o varios registros.");
            selectionTo = (MultiSelectionModel) toGrid.getSelectionModel();

            toGrid.addSelectionListener(selectionEvent -> { // Java 8
                setTotal();
            });
        }
        
        fromGrid.setHeightMode(HeightMode.ROW);
        fromGrid.setHeightByRows(5);
        fromGrid.setWidth("60%");
        fromGrid.setResponsive(true);
        fromGrid.setEditorBuffered(false);

        toGrid.setHeightMode(HeightMode.ROW);
        toGrid.setHeightByRows(5);
        toGrid.setWidth("60%");
        toGrid.setResponsive(true);
        toGrid.setEditorBuffered(false);

        moveBtn = new Button("Agregar los IDEX seleccionados");
        moveBtn.setIcon(FontAwesome.PLUS);
//        saveBtn.setWidth(175, Sizeable.UNITS_PIXELS);
        moveBtn.setDescription("Agregar los IDEX seleccionados");
        moveBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                moveItems();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(moveBtn);
        buttonsLayout.setComponentAlignment(moveBtn, Alignment.MIDDLE_CENTER);

        mainLayout.addComponents(fromGrid, buttonsLayout, toGrid);
        mainLayout.setComponentAlignment(fromGrid, Alignment.MIDDLE_CENTER);
        mainLayout.setComponentAlignment(toGrid, Alignment.MIDDLE_CENTER);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_CENTER);

        fromGrid.getColumn(CODIGO_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        fromGrid.getColumn(CCOSTO_PROPERTY).setExpandRatio(1).setWidth(90).setHidable(true);
        fromGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1).setWidth(90).setHidable(true);
        fromGrid.getColumn(PROJECT_PROPERTY).setExpandRatio(1).setWidth(90).setHidable(true);
        fromGrid.getColumn(MONEDA_PROPERTY).setExpandRatio(1).setWidth(100);
        fromGrid.getColumn(TOTALEST_PROPERTY).setExpandRatio(1).setWidth(110);
        fromGrid.getColumn(TOTALESTSF_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        
        fromGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if(TOTALEST_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROJECT_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            }
            else {
                return null;
            }

        });
        
        
        toGrid.getColumn(CODIGO_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        toGrid.getColumn(CCOSTO_PROPERTY).setExpandRatio(1).setWidth(90).setHidable(true);
        toGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1).setWidth(90).setHidable(true);
        toGrid.getColumn(PROJECT_PROPERTY).setExpandRatio(1).setWidth(90).setHidable(true);
        toGrid.getColumn(MONEDA_PROPERTY).setExpandRatio(1).setWidth(100);
        toGrid.getColumn(TOTALEST_PROPERTY).setExpandRatio(1).setWidth(110);
        toGrid.getColumn(TOTALESTSF_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        
        toGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if(TOTALEST_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROJECT_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            }
            else {
                return null;
            }

        });

        toGrid.setDescription("Doble click para ver detalle de cuentas del IDEX");
        toGrid.addListener((ItemClickEvent.ItemClickListener) (ItemClickEvent event) -> {
            toGrid.select(event.getItemId());
            if (event.isDoubleClick()) {
                if (selectionTo.getSelectedRows() != null) {
                    EstimacionesDetalleWindow estimacionesDetalleWindow = 
                            new EstimacionesDetalleWindow(
                                    estimacionId,
                                    empresa,
                                    empresaNombre, 
                                    String.valueOf(proveedorCbx.getValue()),
                                    proveedorCbx.getItemCaption(proveedorCbx.getValue()),
                                    String.valueOf(toContainer.getContainerProperty(event.getItemId(), CCOSTO_PROPERTY).getValue()),
                                    String.valueOf(toContainer.getContainerProperty(event.getItemId(), IDEX_PROPERTY).getValue())
                            );
                    mainUI.addWindow(estimacionesDetalleWindow);
                    estimacionesDetalleWindow.center();
                }
            }
        });
            
        
        HeaderRow filterRow = fromGrid.appendHeaderRow();

        HeaderCell cellIdex = filterRow.getCell(IDEX_PROPERTY);

        TextField filterFieldIdex = new TextField();
        filterFieldIdex.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldIdex.setInputPrompt("Filtrar");
        filterFieldIdex.setColumns(5);

        filterFieldIdex.addTextChangeListener(change -> {
            fromContainer.removeContainerFilters(IDEX_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                fromContainer.addContainerFilter(
                        new SimpleStringFilter(IDEX_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cellIdex.setComponent(filterFieldIdex);

        HeaderCell cellProject = filterRow.getCell(PROJECT_PROPERTY);

        TextField filterFieldProject = new TextField();
        filterFieldProject.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldProject.setInputPrompt("Filtrar");
        filterFieldProject.setColumns(5);

        filterFieldProject.addTextChangeListener(change -> {
            fromContainer.removeContainerFilters(PROJECT_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                fromContainer.addContainerFilter(
                        new SimpleStringFilter(PROJECT_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cellProject.setComponent(filterFieldProject);

        HeaderCell cell0 = filterRow.getCell(CCOSTO_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(5);

        filterField0.addTextChangeListener(change -> {
            fromContainer.removeContainerFilters(CCOSTO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                fromContainer.addContainerFilter(
                        new SimpleStringFilter(CCOSTO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell0.setComponent(filterField0);
         
        fromGridFooter = fromGrid.appendFooterRow();
        fromGridFooter.getCell(CCOSTO_PROPERTY).setText("Totales");
        fromGridFooter.getCell(CCOSTO_PROPERTY).setStyleName("rightalign");
        fromGridFooter.getCell(TOTALEST_PROPERTY).setText("0.00");
        fromGridFooter.getCell(TOTALEST_PROPERTY).setStyleName("rightalign");

        fromGrid.setFooterVisible(true);

        toGridFooter = toGrid.appendFooterRow();
        toGridFooter.getCell(CCOSTO_PROPERTY).setText("Totales");
        toGridFooter.getCell(CCOSTO_PROPERTY).setStyleName("rightalign");
        toGridFooter.getCell(TOTALEST_PROPERTY).setText("0.00");
        toGridFooter.getCell(TOTALEST_PROPERTY).setStyleName("rightalign");

        toGrid.setFooterVisible(true);
    }

    private void createButtons() {

        moveBackBtn = new Button("Quitar");
        moveBackBtn.setIcon(FontAwesome.MINUS);
//        moveBackBtn.setWidth(175, Sizeable.UNITS_PIXELS);
        moveBackBtn.setDescription("Quitar los IDEX seleccionados");
        moveBackBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                moveItemsBack();
            }
        });

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE);
//        saveBtn.setWidth(175, Sizeable.UNITS_PIXELS);
        saveBtn.setDescription("Guardar cambios");
        saveBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                saveRecords(true);
            }
        });

        verificadaBtn = new Button("Verificada");
        verificadaBtn.setIcon(FontAwesome.CHECK);
//        verificadaBtn.setWidth(175, Sizeable.UNITS_PIXELS);
        verificadaBtn.setDescription("Cambiar estatus a VERIFICADA por supervisor");
        verificadaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                changeStatus("VERIFICADA");
            }
        });

        aprobadaBtn = new Button("Aprobada");
        aprobadaBtn.setIcon(FontAwesome.CHECK_CIRCLE);
//        aprobadaBtn.setWidth(175, Sizeable.UNITS_PIXELS);
        aprobadaBtn.setDescription("Cambiar estatus a APROBADA por gerencia");
        aprobadaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                changeStatus("APROBADA");
//                Notification.show("NO DISPONIBLE EN ESTA VERSION", Notification.Type.HUMANIZED_MESSAGE);
            }
        });

        facturaBtn = new Button("Factura");
        facturaBtn.setIcon(FontAwesome.CHECK_CIRCLE);
//        facturaBtn.setWidth(175, Sizeable.UNITS_PIXELS);
        facturaBtn.setDescription("DATOS DE LA FACTURA CON QUE SE PAGARA LA ESTIMACION");
        facturaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                EstimacionesFacturaWindow estimacionesFacturaWindow = 
                        new EstimacionesFacturaWindow(
                                estimacionId,
                                empresa, // idempresa
                                empresaNombre, 
                                proveedorId,
                                proveedorNombre                                
                        );
                mainUI.addWindow(estimacionesFacturaWindow);
                estimacionesFacturaWindow.center();
                estimacionesFacturaWindow.serieTxt.focus();
                close();                
            }
        });

//System.out.println("\n Estatus = " + estatus);

        if(estatus.equals("NUEVA") || estatus.equals("PAGADA")) {
            verificadaBtn.setEnabled(false);
            aprobadaBtn.setEnabled(false);
            facturaBtn.setEnabled(false);
            if(estatus.equals("PAGADA")) {
                saveBtn.setEnabled(false);
            }            
        }
        if(estatus.equals("VERIFICADA")) {
            saveBtn.setEnabled(false);
            verificadaBtn.setEnabled(false);
            facturaBtn.setEnabled(false);
        }
        if(estatus.equals("APROBADA")) {
            saveBtn.setEnabled(false);
            verificadaBtn.setEnabled(false);
            aprobadaBtn.setEnabled(false);
        }

        exitBtn = new Button("Salir");
        exitBtn.setIcon(FontAwesome.EDIT);
//        exitBtn.setWidth(130, Sizeable.UNITS_PIXELS);
        exitBtn.setDescription("Salir");
        exitBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de SALIR?",
                    "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            close();
                        }
                    }
                });                       
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(moveBackBtn);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.addComponent(verificadaBtn);
        buttonsLayout.addComponent(aprobadaBtn);
        buttonsLayout.addComponent(facturaBtn);
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(moveBackBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(verificadaBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(aprobadaBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(facturaBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    private String getNextEstimacion() {
        String estimacion = "1";
                
        String queryString;
        
        queryString =  "Select Max(CorrelativoEstimacion) UltimaEstimacion ";
        queryString += " From  estimacion ";
        queryString += " Where IdEmpresa = " + this.empresa;
        queryString += " And   IdProveedor = " + String.valueOf(proveedorCbx.getValue());
        
//System.out.println("\n\n"+queryString);

        try {
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                if(rsRecords.getObject("UltimaEstimacion") != null) {
                    estimacion = String.valueOf(rsRecords.getInt("UltimaEstimacion") + 1);
                }
            }
        }
        catch (Exception ex) {
            Logger.getLogger(EstimacionesWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al obtener valor de siguiente estimacion : " + ex.getMessage());
            Notification.show("Error al obtener valor de siguiente estimacion..!", Notification.Type.ERROR_MESSAGE);
        }
        
        return estimacion;
        
    }
    
    private void fillTable() {

        if(selectionFrom == null) {
            return;
        }
        
        if(fromContainer == null) {
            return;
        }
        
        selectionFrom.deselectAll();
        selectionTo.deselectAll();

        fromContainer.removeAllItems();
        toContainer.removeAllItems();

        fromGridFooter.getCell(TOTALEST_PROPERTY).setText("0.00");
        toGridFooter.getCell(TOTALEST_PROPERTY).setText("0.00");

        if(estimacionId.equals("0")) {
            estimacionIdLbl.setValue(getNextEstimacion());
        }
        
        String queryString;
       
        queryString =  "Select DetalleItemsCostos.IdCC, DetalleItemsCostos.IDEX, DetalleItemsCostos.IdProject, ";
        queryString += " DetalleItemsCostos.Moneda, SUM(DetalleItemsCostos.Total) TotalEstimacion ";
        queryString += " From  DetalleItemsCostos ";
        queryString += " Inner Join project On project.Numero = DetalleItemsCostos.IdProject And project.Estatus = 'ACTIVO'";
        queryString += " Where DetalleItemsCostos.IdEmpresa = " + this.empresa;
        queryString += " And   DetalleItemsCostos.IdProveedor = " + String.valueOf(proveedorCbx.getValue());
        queryString += " And   DetalleItemsCostos.Tipo In ('1', '2')";
        queryString += " Group By DetalleItemsCostos.IdCC, DetalleItemsCostos.IDEX, DetalleItemsCostos.IdProject, DetalleItemsCostos.Moneda";

System.out.println("\nQueryEstimacionDetalle="+queryString);

        try {
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                double saldo = 0.00;
                double provision = 0.00;
                double totalest = 0.00;
                
                do {
                                        
                    saldo = rsRecords.getDouble("TotalEstimacion");
                    
                    queryString = " Select IfNull(Sum(Det.Monto),0) TotalCobrado, ";
                    queryString += " IfNull(Sum(Det.Provision),0) TotalProvision, (IfNull(Sum(Det.Monto + Det.Provision),0)) Total ";
                    queryString += " From estimacion_detalle Det";
                    queryString += " Inner Join estimacion Est On Est.EstimacionId = Det.EstimacionId";
                    queryString += " Where Est.IdProveedor = " + String.valueOf(proveedorCbx.getValue());
                    queryString += " And   Est.IdEmpresa   = " + empresa;
                    queryString += " And   Det.IdCentroCosto = " + rsRecords.getString("IdCC");
                    queryString += " And   Det.IDEX        = " + rsRecords.getString("IDEX");
                    queryString += " And   Det.Project     = " + rsRecords.getString("IdProject");

System.out.println("\nQuerySaldoEstimacion="+queryString);

                    rsRecords1 = stQuery1.executeQuery (queryString);

                    if(rsRecords1.next()) { //  encontrado
                        saldo = saldo - rsRecords1.getDouble("Total");
                    }

                    if(estatus.equals("NUEVA") && (saldo <= 0)) {
                        continue;
                    }

                    Object itemId = fromContainer.addItem();

                    fromContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(fromContainer.size()+1);
                    fromContainer.getContainerProperty(itemId, CCOSTO_PROPERTY).setValue(rsRecords.getString("IdCC"));
                    fromContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));
                    fromContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(rsRecords.getString("IdProject"));
                    fromContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                    fromContainer.getContainerProperty(itemId, TOTALEST_PROPERTY).setValue(moneyFormat.format(saldo));
                    fromContainer.getContainerProperty(itemId, TOTALESTSF_PROPERTY).setValue(saldo);

//                    if((estatus.equals("ABIERTA") || estatus.equals("CONFIRMADA")) && (rsRecords1.getObject("Estimacion") != null)) {
//                        selection.select(itemId);
//                    }

                } while(rsRecords.next());

                setTotal();
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(EstimacionesWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }

    }

    private void setTotal() {
        fromGridFooter.getCell(TOTALEST_PROPERTY).setText("0.00");
        fromGridFooter.getCell(CCOSTO_PROPERTY).setText("0 IDEXs");

        if(fromContainer.size() == 0) {
            return;
        }
        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal provision = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalest = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        for (Object itemId: fromContainer.getItemIds()) {
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                        String.valueOf(fromContainer.getContainerProperty(itemId, TOTALESTSF_PROPERTY).getValue())
                )
            ));
        }
        fromGridFooter.getCell(TOTALEST_PROPERTY).setText(moneyFormat.format(total));
        fromGridFooter.getCell(CCOSTO_PROPERTY).setText(String.valueOf(fromContainer.size()) + " IDEXs");
    }
    
    private void setTotalTo() {
        toGridFooter.getCell(TOTALEST_PROPERTY).setText("0.00");
        toGridFooter.getCell(CCOSTO_PROPERTY).setText("0 IDEXs");

        if(toContainer.size() == 0) {
            return;
        }
        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal provision = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalest = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        for (Object itemId: toContainer.getItemIds()) {
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                        String.valueOf(toContainer.getContainerProperty(itemId, TOTALESTSF_PROPERTY).getValue())
                )
            ));
        }
        toGridFooter.getCell(TOTALEST_PROPERTY).setText(moneyFormat.format(total));
        toGridFooter.getCell(CCOSTO_PROPERTY).setText(String.valueOf(fromContainer.size()) + " IDEXs");
    }

    private void saveRecords(boolean displayOkMessage) {
             
        String queryString;
/***
        try {
            if(!estimacionId.equals("0")) { // editar estimacion            
                queryString = "Delete From ";
                queryString += " DocumentosContablesAplicados";
                queryString += " Where Estimacion  = " + estimacionId;
                queryString += " And   IdEmpresa = " + this.empresa;
                queryString += " And   IdProveedor = " + String.valueOf(proveedorCbx.getValue());

                stQuery.executeUpdate(queryString);
            }

            for (Object itemId: selection.getSelectedRows()) {
                queryString = "INSERT INTO DocumentosContablesAplicados (";
                queryString += " TipoDoc, Serie, NoDocumento, Fecha, IdProveedor, Proveedor, NoCuenta, Descripcion,";
                queryString += " IdProject, IdCC, Empresa, IdEmpresa, Total, Moneda, Unidades, Precio, Provision,";
                queryString += " TotEst, Estimacion, EstimacionEstatus, EstimacionFecha, Tasa, TotalQ, FechaIngreso,";
                queryString += " Noc, Lote, IdexAnterior, Rebajado, NoRefCi, Idex, IdPartida )";
                queryString += " VALUES (";
                queryString += " 'Factura'";  //tipodoc
                queryString += ",''"; //serie
                queryString += ",''";// nodocumento
                queryString += ",null"; // fecha de la factura
                queryString += ",'" + String.valueOf(proveedorCbx.getValue()) + "'";
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(fromContainer.getContainerProperty(itemId, CUENTA_PROPERTY).getValue()) + "'"; //nocuenta
                queryString += ",'" + String.valueOf(fromContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).getValue()) + "'"; //nocuenta
                queryString += ","  + String.valueOf(fromContainer.getContainerProperty(itemId, PROJECT_PROPERTY).getValue());
                queryString += ","  + String.valueOf(fromContainer.getContainerProperty(itemId, CCOSTO_PROPERTY).getValue());
                queryString += ",'" + empresaNombre + "'";
                queryString += ","  + empresa;
                queryString += ","  + String.valueOf(fromContainer.getContainerProperty(itemId, TOTALSF_PROPERTY).getValue()); //total
                queryString += ",'" + String.valueOf(fromContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue()) + "'"; //moneda
                queryString += ","  + String.valueOf(fromContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).getValue()).replaceAll(",","");
                queryString += ","  + String.valueOf(fromContainer.getContainerProperty(itemId, PRECIO_PROPERTY).getValue()).replaceAll(",","");
                queryString += ","  + String.valueOf(fromContainer.getContainerProperty(itemId, PRECIO_PROPERTY).getValue()).replaceAll(",","");//provesion
                queryString += ","  +  String.valueOf(fromContainer.getContainerProperty(itemId, TOTALSF_PROPERTY).getValue()); //totest
                queryString += ","  + estimacionIdLbl.getValue();
                queryString += ",'ABIERTA'"; //estimacionestatus
                queryString += ",current_date"; //estimacionfecha
                queryString += ",'1.0000'"; //tasa
                queryString += ",0.00"; //totalq
                queryString += ",current_date"; //fechaingreso
                queryString += ",0"; // noc
                queryString += "," + String.valueOf(fromContainer.getContainerProperty(itemId, LOTE_PROPERTY).getValue()); //lote
                queryString += ",0"; //idexanterior
                queryString += ",null";// rebajado
                queryString += ",null";//norefci
                queryString += "," +  String.valueOf(fromContainer.getContainerProperty(itemId, IDEX_PROPERTY).getValue()); //idex
                queryString += ",0)"; //idpartida

                stQuery.executeUpdate(queryString);

            } // end for                    
            
            if(displayOkMessage) {
                Notification.show("ESTIMACION CREADA CON EXITO!", Notification.Type.HUMANIZED_MESSAGE);
                close();
            }
        }
        catch (Exception ex) {
            Logger.getLogger(EstimacionesWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al actualizar registros de DocumentosContablesAplicados : " + ex.getMessage());
            Notification.show("Error al intentar actualizar registros de DocumentosContablesAplicados..!", Notification.Type.ERROR_MESSAGE);
        }
***/        
    }   

    private void moveItems() {        

        for (Object itemId: selectionFrom.getSelectedRows()) {
            toContainer.addItem(itemId);
            toContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(toContainer.size()+1);
            toContainer.getContainerProperty(itemId, CCOSTO_PROPERTY).setValue(fromContainer.getContainerProperty(itemId, CCOSTO_PROPERTY).getValue());
            toContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(fromContainer.getContainerProperty(itemId, IDEX_PROPERTY).getValue());
            toContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(fromContainer.getContainerProperty(itemId, PROJECT_PROPERTY).getValue());
            toContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(fromContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue());
            toContainer.getContainerProperty(itemId, TOTALEST_PROPERTY).setValue(fromContainer.getContainerProperty(itemId, TOTALEST_PROPERTY).getValue());
            toContainer.getContainerProperty(itemId, TOTALESTSF_PROPERTY).setValue(fromContainer.getContainerProperty(itemId, TOTALESTSF_PROPERTY).getValue());
                        
        }
        setTotalTo();
    }
    
    private void moveItemsBack() {
        for (Object itemId: selectionTo.getSelectedRows()) {
            toContainer.removeItem(itemId);            
        }
        setTotalTo();
        
    }

    private void changeStatus(String estatus) {
     
        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de cambiar a estatus : " + estatus.toUpperCase() + " ?",
                "SI", "NO", new ConfirmDialog.Listener() {

            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    String queryString;

                    try {
                        queryString = "Update DocumentosContablesAplicados ";
                        queryString += " Set EstimacionEstatus = '" + estatus.toUpperCase() + "'";
                        queryString += " Where Estimacion  = " + estimacionId;
                        queryString += " And   IdEmpresa = " + empresa;
                        queryString += " And   IdProveedor = " + String.valueOf(proveedorCbx.getValue());

                        stQuery.executeUpdate(queryString);

                        if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("EstimacionesView")) {

                            ((EstimacionesView) (mainUI.getNavigator().getCurrentView())).container.getContainerProperty(
                                    ((EstimacionesView) (mainUI.getNavigator().getCurrentView())).estimacionesGrid.getSelectedRow(), EstimacionesView.ESTATUS_PROPERTY).setValue(estatus.toUpperCase());
                        }

                        Notification.show("ESTIMACION " + estatus.toUpperCase() + " CON EXITO!", Notification.Type.HUMANIZED_MESSAGE);

                        //TODO : generar documento PDF

                        EstimacionPDF estimacionPDF
                                = new EstimacionPDF(
                                        empresa,
                                        empresaNombre,
                                        getEmpresaNit(),
                                        estimacionId,
                                        estatus,
                                        proveedorId,
                                        proveedorNombre,
                                        fromContainer,
                                        selectionTo,
                                        fromGridFooter
                                );
                        mainUI.addWindow(estimacionPDF);
                        estimacionPDF.center();

                        //TODO : enviar correo, verificar si es necesario o no...
                        
                        close();
                    }
                    catch (Exception ex) {
                        Logger.getLogger(EstimacionesWindow.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("Error al actualizar registros de DocumentosContablesAplicados : " + ex.getMessage());
                        Notification.show("Error al intentar actualizar registros de DocumentosContablesAplicados..!", Notification.Type.ERROR_MESSAGE);
                    }

                }
            }
        });
        
    }   
    
    public String getEmpresaNit() {
        String strNit = "N/A";

        String queryString = " SELECT Nit from contabilidad_empresa ";
        queryString += " Where IdEmpresa = " + empresa;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) {
                strNit = rsRecords1.getString("Nit");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar NIT de empresa: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return strNit;
    }
    
}
