/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SeguimientoHandler;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class ChangeOrdersView extends VerticalLayout implements View {
    
    public Statement stQuery = null;
    public ResultSet rsRecords = null;
    
    static final String ID_PROPERTY = "Id";
    static final String FECHA_PROPERTY = "Fecha";
    static final String POR_PROPERTY = "Solicitado por";
    static final String ROL_PROPERTY = "Rol";
    static final String INSTRUCCION_PROPERTY   = "Instrucción";
    static final String CREADAPOR_PROPERTY   = "Creada por";
    static final String FECHA_REGISTRO_PROPERTY = "Fecha registro";
    static final String OPTIONS_PROPERTY = "-";    
    
    Utileria utileria = new Utileria();
    MarginInfo  marginInfo;
     
    IndexedContainer container = new IndexedContainer();
    Grid changeOrdersGrid;
    FooterRow footer;
                
    Button newBtn;
    Button editBtn;
    Button trackingBtn;
    Button deleteBtn;
    
    Button exportExcelBtn;
        
    public static Locale locale = new Locale("ES","GT"); 
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
                
    UI mainUI;
       
    public ChangeOrdersView() {
        this.mainUI = UI.getCurrent();    
        
        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);
        
        marginInfo = new MarginInfo(true,true,false,true); 

        Label titleLbl = new Label("Ordenes de cambio");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");
        
        addComponent(titleLbl);
        
        buildReport();
        
    }
            
    private void buildReport() {

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setMargin(true);
        
        Label viewCaption = new Label("Ordenes de cambio");
        viewCaption.setStyleName(ValoTheme.LABEL_H3);

        verticalLayout.addComponent(viewCaption);        
        verticalLayout.setComponentAlignment(viewCaption, Alignment.MIDDLE_LEFT);
        
        verticalLayout.addComponent(createReport());
        
        newBtn    = new Button("Nueva orden");
        newBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        newBtn.setWidth(170,Sizeable.UNITS_PIXELS);
//        newBtn.addStyleName(ValoTheme.BUTTON_QUIET);
        newBtn.setDescription("Registrar nueva orden de cambio");
        newBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                ChangeOrderForm newChangeOrderForm = new ChangeOrderForm();
                mainUI.addWindow(newChangeOrderForm);
                newChangeOrderForm.center();
                newChangeOrderForm.solicitadoPorTxt.focus();
            }
        });
                
        editBtn    = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(170,Sizeable.UNITS_PIXELS);
//        newBtn.addStyleName(ValoTheme.BUTTON_QUIET);
        editBtn.setDescription("Registrar nueva orden de cambio");
        editBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(changeOrdersGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);                    
                }
                else {
                    editOrdenCambio(changeOrdersGrid.getSelectedRow());
                }
            }
        });

        trackingBtn    = new Button("Notas y seguimiento");
        trackingBtn.setIcon(FontAwesome.BARS);
        trackingBtn.setWidth(170,Sizeable.UNITS_PIXELS);
//        trackingBtn.addStyleName(ValoTheme.BUTTON_QUIET);
        trackingBtn.setDescription("Notas y seguimiento");
        trackingBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(changeOrdersGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);                    
                }
                else {
                    SeguimientoHandler seguimientoHandler = new SeguimientoHandler();
                    seguimientoHandler.fillTrackTable(String.valueOf(changeOrdersGrid.getSelectedRow()), String.valueOf(changeOrdersGrid.getContainerDataSource().getItem(changeOrdersGrid.getSelectedRow()).getItemProperty(POR_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(seguimientoHandler);
                }
            }
        });

        deleteBtn    = new Button("Eliminar");
        deleteBtn.setIcon(FontAwesome.TRASH);
        deleteBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        deleteBtn.setDescription("Eliminar el registro de orden de cambio.");
//        deleteBtn.addStyleName(ValoTheme.BUTTON_QUIET);
        deleteBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(changeOrdersGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);                    
                }
                else {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                deleteChangeOrder();
                            }
                        }
                    });
                }
            }
        });
        
        exportExcelBtn    = new Button("Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setDescription("Exportar los datos a Excel");
        exportExcelBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(changeOrdersGrid.getHeightByRows() > 0) {
                    //
                }
            }
        });        

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(new MarginInfo(true,false,false,false));
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(trackingBtn);
        buttonsLayout.addComponent(deleteBtn);

        verticalLayout.addComponent(buttonsLayout);
        verticalLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
                
        fillChangeOrdersTable();

        addComponent(verticalLayout);
    }
    
    private HorizontalLayout createReport() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(false);

        container.addContainerProperty(ID_PROPERTY,               String.class, null);        
        container.addContainerProperty(FECHA_PROPERTY,            String.class, null);
        container.addContainerProperty(POR_PROPERTY,              String.class, null);
        container.addContainerProperty(ROL_PROPERTY,              String.class, null);
        container.addContainerProperty(INSTRUCCION_PROPERTY,      String.class, null);
        container.addContainerProperty(CREADAPOR_PROPERTY,        String.class, null);
        container.addContainerProperty(FECHA_REGISTRO_PROPERTY,   String.class, null);
//        container.addContainerProperty(OPTIONS_PROPERTY,          String.class, null);
  
        changeOrdersGrid = new Grid("Listado de inspecciones", container);
        
//        changeOrdersGrid.addStyleName("smallgrid");
        changeOrdersGrid.setImmediate(true);
        changeOrdersGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        changeOrdersGrid.setDescription("Seleccione un registro.");
        changeOrdersGrid.setHeightMode(HeightMode.ROW);
        changeOrdersGrid.setHeightByRows(5);
        changeOrdersGrid.setSizeFull();
        changeOrdersGrid.setResponsive(true);
        changeOrdersGrid.setEditorBuffered(false);

        changeOrdersGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);                

//        changeOrdersGrid.getColumn(OPTIONS_PROPERTY).setRenderer(new ButtonRenderer(e ->
//                editOrdenCambio(e.getItemId())));

        changeOrdersGrid.getColumn(FECHA_PROPERTY).setMaximumWidth(110);
        changeOrdersGrid.getColumn(POR_PROPERTY).setMaximumWidth(200);
        changeOrdersGrid.getColumn(ROL_PROPERTY).setMaximumWidth(90);
        changeOrdersGrid.getColumn(INSTRUCCION_PROPERTY).setMaximumWidth(300);
        changeOrdersGrid.getColumn(CREADAPOR_PROPERTY).setMaximumWidth(100);
        changeOrdersGrid.getColumn(FECHA_REGISTRO_PROPERTY).setMaximumWidth(110);
//        changeOrdersGrid.getColumn(OPTIONS_PROPERTY).setMaximumWidth(100);
                        
        changeOrdersGrid.addListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {
                changeOrdersGrid.select(event.getItemId());
                if (event.isDoubleClick()) {
                    if(changeOrdersGrid.getSelectedRow() != null) {
                        editOrdenCambio(changeOrdersGrid.getSelectedRow());
                    }
                }
            }
        });

        HeaderRow filterRow = changeOrdersGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(POR_PROPERTY);
        
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");            
        filterField.setColumns(10);
            
        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(POR_PROPERTY);
                
            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                container.addContainerFilter(
                    new SimpleStringFilter(POR_PROPERTY,
                        change.getText(), true, false));
                footer.getCell(POR_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");                
            }
        });
        cell.setComponent(filterField);      

        HeaderCell cell1 = filterRow.getCell(ROL_PROPERTY);
        
        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");            
        filterField1.setColumns(10);
            
        filterField1.addTextChangeListener(change -> {
            container.removeContainerFilters(ROL_PROPERTY);
                
            // (Re)create the filter if necessary
            if (! change.getText().isEmpty()) {
                container.addContainerFilter(
                    new SimpleStringFilter(POR_PROPERTY,
                        change.getText(), true, false));
                footer.getCell(ROL_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");                
            }
        });
        cell.setComponent(filterField1);

        footer = changeOrdersGrid.appendFooterRow();
        footer.getCell(POR_PROPERTY).setText("0 REGISTROS");
        footer.getCell(POR_PROPERTY).setStyleName("rightalign");
        
        changeOrdersGrid.setFooterVisible(true);
        
        reportLayout.addComponent(changeOrdersGrid);
        reportLayout.setComponentAlignment(changeOrdersGrid, Alignment.TOP_CENTER);

        return reportLayout;
    }

    public void fillChangeOrdersTable() {
        
        if(container == null) {
            return;
        }
        
        container.removeAllItems();

        footer.getCell(POR_PROPERTY).setText("0 REGISTROS");
                
        String queryString = "";
        
        queryString =  "Select OrdC.*, Usr.Nombre UsuarioNombre";
        queryString += " From       orden_cambio OrdC";
        queryString += " Inner Join usuario  Usr On Usr.IdUsuario = OrdC.CreadoUsuario";
        queryString += " Where OrdC.IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();

System.out.println("\n\n"+queryString);

        try {
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                
                do {
                    Object itemId = container.addItem();
                    
                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdOrdenCambio"));
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    container.getContainerProperty(itemId, POR_PROPERTY).setValue(rsRecords.getString("SolicitadoPor"));
                    container.getContainerProperty(itemId, ROL_PROPERTY).setValue(rsRecords.getString("SolicitadoRol"));
                    container.getContainerProperty(itemId, INSTRUCCION_PROPERTY).setValue(rsRecords.getString("Instruccion"));
                    container.getContainerProperty(itemId, CREADAPOR_PROPERTY).setValue(rsRecords.getString("UsuarioNombre"));
                    container.getContainerProperty(itemId, FECHA_REGISTRO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY_HHMM_2(rsRecords.getTimestamp("CreadoFechaYHora")));
//                    container.getContainerProperty(itemId, OPTIONS_PROPERTY).setValue("Editar");

                }while(rsRecords.next());
                
//                rsRecords.last();
                footer.getCell(POR_PROPERTY).setText(String.valueOf(rsRecords.getRow()) + " REGISTROS");
                changeOrdersGrid.select(container.firstItemId());

            }
        } 
        catch (Exception ex) {
            Logger.getLogger(ChangeOrdersView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de ordenes de cambio : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de ordenes de cambio..!", Notification.Type.ERROR_MESSAGE);
        }

    } 
        
    /**
     * This class creates a streamresource. This class implements
     * the StreamSource interface which defines the getStream method.
     */
    public static class ShowExcelFile implements StreamResource.StreamSource {

        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        public ShowExcelFile(File fileToOpen) {
            try {           
               
                FileOutputStream fost = new FileOutputStream(fileToOpen);

            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

        @Override
        public InputStream getStream() {
            // Here we return the pdf contents as a byte-array
            return new ByteArrayInputStream(os.toByteArray());
        }    
    }
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub
        Page.getCurrent().setTitle("Sopdi - Visitas por inspección");
    }

    private void deleteChangeOrder() {        
        
        String queryString = "";

        queryString =  "Delete ";
        queryString += " From  orden_cambio_seguimiento ";
        queryString += " Where IdOrdenCambio = " + String.valueOf(changeOrdersGrid.getContainerDataSource().getItem(changeOrdersGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue());

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            stQuery.executeUpdate(queryString);

            queryString =  "Delete ";
            queryString += " From  orden_cambio ";
            queryString += " Where IdOrdenCambio = " + String.valueOf(changeOrdersGrid.getContainerDataSource().getItem(changeOrdersGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue());

            stQuery.executeUpdate(queryString);

            Notification.show("Operación exitosa!", Notification.Type.TRAY_NOTIFICATION);
            fillChangeOrdersTable();
        } 
        catch (Exception ex) {
            Logger.getLogger(ChangeOrdersView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al ELIINAR registros de ordenes de cambio : " + ex.getMessage());
            Notification.show("Error al ELIMINAR registros de ordenes de cambio..!", Notification.Type.ERROR_MESSAGE);
        } 
    }
    
    private void editOrdenCambio(Object itemId) {
        
        ChangeOrderForm newChangeOrderForm = new ChangeOrderForm();
        mainUI.addWindow(newChangeOrderForm);
        newChangeOrderForm.idChangeOrderTxt.setReadOnly(false);
        newChangeOrderForm.idChangeOrderTxt.setValue(String.valueOf(changeOrdersGrid.getContainerDataSource().getItem(changeOrdersGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()));
        newChangeOrderForm.idChangeOrderTxt.setReadOnly(true);
        newChangeOrderForm.fillData();
        newChangeOrderForm.center();
        newChangeOrderForm.solicitadoPorTxt.focus();
    }
 
}
