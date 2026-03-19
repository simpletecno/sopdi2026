/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;  
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class CostCenterView extends VerticalLayout implements View {
    
    public Statement stQuery = null;
    public ResultSet rsRecords = null;

    // --  Tabala Centro Costos
    static final String ID_PROPERTY_CENTRO_COSTO    = "Id";
    static final String CENTRO_COSTO_PROPERTY       = "Centro costo";
    static final String DESCRIPCION_PROPERTY        = "Descripción";
    static final String LOTE_PROPERTY               = "Lote";
    static final String INHABILITADO_PROPERTY       = "Inhabilitado";
    
    // --  Tabala Encargados
    static final String ID_PROPERTY_ENCARGADO       = "Id";
    static final String CODGIO_PROVEEDOR_PROPERTY   = "Id Proveedor";
    static final String NOMBRE_PROPERTY             = "Nombre";
    static final String TIPO_PROPERTY               = "Tipo";

    MarginInfo  marginInfo;
     
    public IndexedContainer container_centro_costo = new IndexedContainer();
    public IndexedContainer container_encargados = new IndexedContainer();
    Grid costCenterGrid;
    Grid costCenterEncargadosGrid;
                                        
    UI mainUI;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public CostCenterView() {
        this.mainUI = UI.getCurrent();

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);
        
        marginInfo = new MarginInfo(true,true,false,true); 

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " Centros de costo");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl,  Alignment.TOP_LEFT);

        Button refreshBtn = new Button("Actualizar");
        refreshBtn.setIcon(FontAwesome.REFRESH);
        refreshBtn.setWidth(140,Sizeable.UNITS_PIXELS);
        refreshBtn.setDescription("Actualizar datos");
        refreshBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                fillCostCenterGrid();
            }
        });
        titleLayout.addComponent(refreshBtn);
        titleLayout.setComponentAlignment(refreshBtn,  Alignment.TOP_RIGHT);
        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createCostCenterGrid();
        createEncargados();
        createButtons();
        
        fillCostCenterGrid();
        
    }

    private void createCostCenterGrid() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container_centro_costo.addContainerProperty(ID_PROPERTY_CENTRO_COSTO , String.class, "");
        container_centro_costo.addContainerProperty(CENTRO_COSTO_PROPERTY,  String.class, "");
        container_centro_costo.addContainerProperty(DESCRIPCION_PROPERTY,         String.class, "");
        container_centro_costo.addContainerProperty(LOTE_PROPERTY,         String.class, "");
        container_centro_costo.addContainerProperty(INHABILITADO_PROPERTY,  String.class, "");

        costCenterGrid = new Grid("Listado de centros de costo", container_centro_costo);
        
        costCenterGrid.setImmediate(true);
        costCenterGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        costCenterGrid.setDescription("Seleccione un registro.");
        costCenterGrid.setHeightMode(HeightMode.ROW);
        costCenterGrid.setHeightByRows(8);
        costCenterGrid.setWidth("100%");
        costCenterGrid.setResponsive(true);
        costCenterGrid.setEditorBuffered(false);

        reportLayout.addComponent(costCenterGrid);
        reportLayout.setComponentAlignment(costCenterGrid, Alignment.MIDDLE_LEFT);

        costCenterGrid.getColumn(ID_PROPERTY_CENTRO_COSTO ).setHidable(true).setHidden(true);                
//        costCenterGrid.getColumn(ID_VISITA_PROPERTY).setHidable(true).setHidden(true);                

        //costCenterGrid.getColumn(ID_PROPERTY_CENTRO_COSTO ).setMaximumWidth(10);
        //costCenterGrid.getColumn(CENTRO_COSTO_PROPERTY).setMaximumWidth(200);
        //costCenterGrid.getColumn(DESCRIPCION_PROPERTY).setMaximumWidth(500);
                        
        HeaderRow filterRow = costCenterGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(CENTRO_COSTO_PROPERTY);
        
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");            
        filterField.setColumns(15);
            
        filterField.addTextChangeListener(change -> {
            container_centro_costo.removeContainerFilters(CENTRO_COSTO_PROPERTY);
                
            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                container_centro_costo.addContainerFilter(
                    new SimpleStringFilter(CENTRO_COSTO_PROPERTY,
                        change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell1 = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(20);

        filterField1.addTextChangeListener(change -> {
            container_centro_costo.removeContainerFilters(DESCRIPCION_PROPERTY);

            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                container_centro_costo.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField1);

        HeaderCell cellLote = filterRow.getCell(LOTE_PROPERTY);

        TextField filterFieldLote = new TextField();
        filterFieldLote.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldLote.setInputPrompt("Filtrar");
        filterFieldLote.setColumns(10);

        filterFieldLote.addTextChangeListener(change -> {
            container_centro_costo.removeContainerFilters(LOTE_PROPERTY);

            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                container_centro_costo.addContainerFilter(
                        new SimpleStringFilter(LOTE_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cellLote.setComponent(filterFieldLote);

        costCenterGrid.addSelectionListener((SelectionEvent.SelectionListener) event -> {
            if (costCenterGrid.getSelectedRow() != null) {

                fillEncargadosGrid((String) container_centro_costo.getContainerProperty(costCenterGrid.getSelectedRow(), CENTRO_COSTO_PROPERTY).getValue());
            }
        });

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createEncargados() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);
                
        container_encargados.addContainerProperty(ID_PROPERTY_ENCARGADO, String.class, "");
        container_encargados.addContainerProperty(CODGIO_PROVEEDOR_PROPERTY, String.class, "");
        container_encargados.addContainerProperty(NOMBRE_PROPERTY, String.class, "");
        container_encargados.addContainerProperty(TIPO_PROPERTY,  String.class, "");

        costCenterEncargadosGrid = new Grid("Encargados Centro Costo: ", container_encargados);

        costCenterEncargadosGrid.setImmediate(true);
        costCenterEncargadosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        costCenterEncargadosGrid.setDescription("Encargados Centro Costo");
        costCenterEncargadosGrid.setHeightMode(HeightMode.ROW);
        costCenterEncargadosGrid.setHeightByRows(4);
        costCenterEncargadosGrid.setWidth("100%");
        costCenterEncargadosGrid.setResponsive(true);
        costCenterEncargadosGrid.setEditorBuffered(false);

        reportLayout.addComponent(costCenterEncargadosGrid);
        reportLayout.setComponentAlignment(costCenterEncargadosGrid, Alignment.MIDDLE_RIGHT);

        costCenterGrid.getColumn(ID_PROPERTY_ENCARGADO).setHidable(true).setHidden(true);

        HeaderRow filterRow = costCenterEncargadosGrid.appendHeaderRow();

        HeaderCell cell1 = filterRow.getCell(CODGIO_PROVEEDOR_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(15);

        filterField1.addTextChangeListener(change -> {
            container_encargados.removeContainerFilters(CODGIO_PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                container_encargados.addContainerFilter(
                        new SimpleStringFilter(CODGIO_PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField1);

        HeaderCell cell2 = filterRow.getCell(NOMBRE_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(20);

        filterField2.addTextChangeListener(change -> {
            container_encargados.removeContainerFilters(NOMBRE_PROPERTY);

            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                container_encargados.addContainerFilter(
                        new SimpleStringFilter(NOMBRE_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);


        HeaderCell cell3 = filterRow.getCell(TIPO_PROPERTY);

        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(20);

        filterField3.addTextChangeListener(change -> {
            container_encargados.removeContainerFilters(TIPO_PROPERTY);

            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                container_encargados.addContainerFilter(
                        new SimpleStringFilter(TIPO_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell3.setComponent(filterField3);


        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        Button newBtn    = new Button("Agregar");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140,Sizeable.UNITS_PIXELS);
//        newBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        newBtn.setDescription("Agregar centro de costo");
        newBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                CostCenterForm costCenterForm = 
                        new CostCenterForm();
                costCenterForm.idCentroCostoTxt.setReadOnly(false);
                costCenterForm.idCentroCostoTxt.setValue("0");                
                costCenterForm.idCentroCostoTxt.setReadOnly(true);
                UI.getCurrent().addWindow(costCenterForm);
                costCenterForm.setData(this);
                costCenterForm.center();
            }
        });

        Button editBtn    = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(140,Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(costCenterGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);                    
                }
                else {
                    CostCenterForm costCenterForm = 
                            new CostCenterForm();
                    costCenterForm.idCentroCostoTxt.setReadOnly(false);
                    costCenterForm.idCentroCostoTxt.setValue(String.valueOf(container_centro_costo.getContainerProperty(costCenterGrid.getSelectedRow(), ID_PROPERTY_CENTRO_COSTO ).getValue()));
                    costCenterForm.idCentroCostoTxt.setReadOnly(true);
                    UI.getCurrent().addWindow(costCenterForm);
                    costCenterForm.setData(this);
                    costCenterForm.fillData();
                    costCenterForm.center();
                }
            }
        });

        Button deleteBtn    = new Button("Eliminar");
        deleteBtn.setIcon(FontAwesome.TRASH);
//        deleteBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        deleteBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        deleteBtn.setDescription("Eliminar el registro.");
//        deleteBtn.addStyleName(ValoTheme.BUTTON_QUIET);
        deleteBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(costCenterGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);                    
                }
                else {                    
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                Notification.show("NO DISPONIBLE EN ESTA VERSION", Notification.Type.WARNING_MESSAGE);                    
                            }
                        }
                    });
                }
            }
        });
                
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(deleteBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);                
    }

    public void fillCostCenterGrid() {

        if(container_centro_costo == null) {
            return;
        }
        
        container_centro_costo.removeAllItems();

        String queryString = "";
        
        queryString = "SELECT *";
        queryString += " FROM centro_costo";
        queryString += " WHERE IdProyecto = " + ((SopdiUI)mainUI).sessionInformation.getStrProjectId();
        queryString += " AND IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId();

System.out.println("\nQuery CentrosCosto=" + queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                                
                do {

                    Object itemId = container_centro_costo.addItem();
                    
                    container_centro_costo.getContainerProperty(itemId, ID_PROPERTY_CENTRO_COSTO ).setValue(rsRecords.getString("IdCentroCosto"));
                    container_centro_costo.getContainerProperty(itemId, CENTRO_COSTO_PROPERTY).setValue(rsRecords.getString("CodigoCentroCosto"));
                    container_centro_costo.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Grupo"));
                    container_centro_costo.getContainerProperty(itemId, LOTE_PROPERTY).setValue(rsRecords.getString("Lote"));
                    container_centro_costo.getContainerProperty(itemId, INHABILITADO_PROPERTY).setValue(rsRecords.getString("Inhabilitado").equals("1") ? "SI" : "NO");

                }while(rsRecords.next());

                rsRecords.last();
                //costCenterGrid.select(container_centro_costo.firstItemId());
            }


        } 
        catch (Exception ex) {
            Logger.getLogger(CostCenterView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de CENTROS DE COSTO : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de CENTROS DE COSTO..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void fillEncargadosGrid(String centroCosto) {

        costCenterEncargadosGrid.setCaption("Encargados Centro Costo: " + centroCosto);

        if(container_centro_costo == null) {
            return;
        }

        container_encargados.removeAllItems();

        String queryString = "";

        queryString = "SELECT *";
        queryString += " FROM centro_costo_encargado";
        queryString += " WHERE CodigoCentroCosto = " + centroCosto;
        queryString += " AND Eliminado = 0";

//System.out.println("\n\n"+queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                do {

                    Object itemId = container_encargados.addItem();
                    container_encargados.getContainerProperty(itemId, ID_PROPERTY_ENCARGADO).setValue(rsRecords.getString("ID"));
                    container_encargados.getContainerProperty(itemId, CODGIO_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    container_encargados.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    container_encargados.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("Tipo"));

                }while(rsRecords.next());

                rsRecords.last();
                //costCenterEncargadosGrid.select(container_centro_costo.firstItemId());
            }
        }
        catch (Exception ex) {
            Logger.getLogger(CostCenterView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de CENTROS DE COSTO : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de CENTROS DE COSTO..!", Notification.Type.ERROR_MESSAGE);
        }
    }
        
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub
        Page.getCurrent().setTitle("Sopdi - Centros de costo");
    }
}