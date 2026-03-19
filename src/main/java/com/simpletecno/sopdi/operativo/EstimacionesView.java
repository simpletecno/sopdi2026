/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
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
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class EstimacionesView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;
    public Statement stQuery1 = null;
    public ResultSet rsRecords1 = null;

    static final String ID_PROPERTY = "Id";
    static final String ESTIMACION_PROPERTY = "Estimación";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String MONTO_PROPERTY = "Monto";
    static final String PROVISION_PROPERTY = "Provisión";
    static final String TOTAL_PROPERTY = "Total";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String MONTOSF_PROPERTY = "MSF";
    static final String PROVISIONSF_PROPERTY = "PSF";
    static final String TOTALSF_PROPERTY = "TSF";
    static final String PARTIDA_PROPERTY = "Partida";

    Utileria utileria = new Utileria();
    MarginInfo marginInfo;

    Button nextBtn;
    Button consultarBtn;
    Button prevBtn;
    List<String> empresaLst;
    ListIterator<String> listIterator;
    Label empresaLbl;
    String empresa;

    public IndexedContainer container = new IndexedContainer();
    Grid estimacionesGrid;
    FooterRow footer;

    Button newBtn;
    Button editBtn;
    Button deleteBtn;
    DateField inicioDt;
    DateField finDt;

    static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    static DecimalFormat moneyFormat = new DecimalFormat("#,###,##0.00");
    
    UI mainUI;

    public EstimacionesView() {
        this.mainUI = UI.getCurrent();

        Responsive.makeResponsive(this);
//        setMargin(true);
        setSpacing(true);

        marginInfo = new MarginInfo(true, true, false, true);

        Label titleLbl = new Label("Estimaciones");
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        addComponents(titleLbl);
        setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        empresaLbl = new Label("");
        empresaLbl.setWidth("310px");
        empresaLbl.addStyleName(ValoTheme.LABEL_H3);

        crearButtonEmpresa();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSizeUndefined();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.addStyleName("rcorners4");
        titleLayout.addComponents(empresaLbl, prevBtn, nextBtn);
        titleLayout.setComponentAlignment(prevBtn, Alignment.MIDDLE_LEFT);
        titleLayout.setComponentAlignment(nextBtn, Alignment.MIDDLE_LEFT);

        inicioDt = new DateField("DEL:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelAnio();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("8em");

        finDt = new DateField("AL:");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("8em");

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillTable();
            }
        });

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setWidth("100%");
        filterLayout.setMargin(new MarginInfo(false, true, false, true));
        
        filterLayout.addComponents(inicioDt);
        filterLayout.setComponentAlignment(inicioDt, Alignment.TOP_CENTER);
        filterLayout.addComponents(finDt);
        filterLayout.setComponentAlignment(finDt, Alignment.TOP_CENTER);
        filterLayout.addComponents(consultarBtn);
        filterLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);

        titleLayout.addComponents(filterLayout);
        titleLayout.setComponentAlignment(filterLayout, Alignment.TOP_LEFT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createInspectionsGrid();
        createButtons();

        String empresaString = String.valueOf(listIterator.next());
        empresaLbl.setValue(empresaString);

        empresa = empresaString.substring(1, 3);

        fillTable();

    }

    public void crearButtonEmpresa() {

        final int EPREV = 0;
        final int ENEXT = 1;

        empresaLst = new ArrayList<String>();

        llenarComboEmpresa();

        listIterator = empresaLst.listIterator();

        prevBtn = new Button("Anterior");
        prevBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        prevBtn.addStyleName(ValoTheme.BUTTON_LARGE);
        prevBtn.addStyleName("flechas");
        prevBtn.setIcon(FontAwesome.ARROW_LEFT);
        prevBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (listIterator.hasPrevious()) {
                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);
                    empresaLbl.setValue(listIterator.previous());
                    empresa = empresaLbl.getValue().substring(1, 3);

                    fillTable();
                } else {
                    prevBtn.setEnabled(false);
                }
            }
        });

        nextBtn = new Button("Siguiente");
        nextBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        nextBtn.addStyleName(ValoTheme.BUTTON_LARGE);
        nextBtn.addStyleName("flechas");
        nextBtn.setIcon(FontAwesome.ARROW_RIGHT);
        nextBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (listIterator.hasNext()) {
                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);

                    empresaLbl.setValue(listIterator.next());
                    empresa = empresaLbl.getValue().substring(1, 3);
                    fillTable();
                } else {
                    nextBtn.setEnabled(false);
                }
            }
        });

        String empresaString = String.valueOf(empresaLst.iterator().next());
        empresaLbl.setValue(empresaString);

        empresa = empresaString.substring(1, 3);

    }

    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                empresaLst.add("(" + rsRecords1.getString("IdEmpresa") + ") " + rsRecords1.getString("Empresa"));
            }
            rsRecords1.first();
            empresaLbl.setValue("(" + rsRecords1.getString("IdEmpresa") + ") " + rsRecords1.getString("Empresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void createInspectionsGrid() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(false);

        container.addContainerProperty(ID_PROPERTY,          String.class, null);
        container.addContainerProperty(ESTIMACION_PROPERTY,  String.class, null);
        container.addContainerProperty(PROVEEDOR_PROPERTY,   String.class, null);
        container.addContainerProperty(FECHA_PROPERTY,       String.class, null);
        container.addContainerProperty(MONTO_PROPERTY,       String.class, null);
        container.addContainerProperty(PROVISION_PROPERTY,   String.class, null);
        container.addContainerProperty(TOTAL_PROPERTY,       String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY,      String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY,     String.class, null);
        container.addContainerProperty(PARTIDA_PROPERTY,     String.class, null);
        container.addContainerProperty(MONTOSF_PROPERTY,     Double.class, null);
        container.addContainerProperty(PROVISIONSF_PROPERTY, Double.class, null);
        container.addContainerProperty(TOTALSF_PROPERTY,     Double.class, null);

        estimacionesGrid = new Grid("", container);

        estimacionesGrid.setImmediate(true);
        estimacionesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        estimacionesGrid.setDescription("Seleccione un registro.");
        estimacionesGrid.setHeightMode(HeightMode.ROW);
        estimacionesGrid.setHeightByRows(10);
        estimacionesGrid.setWidth("100%");
        estimacionesGrid.setResponsive(true);
        estimacionesGrid.setEditorBuffered(false);

        reportLayout.addComponent(estimacionesGrid);
        reportLayout.setComponentAlignment(estimacionesGrid, Alignment.MIDDLE_CENTER);

        estimacionesGrid.getColumn(ID_PROPERTY).setMaximumWidth(90).setHidable(true).setHidden(true);
        estimacionesGrid.getColumn(ESTIMACION_PROPERTY).setMaximumWidth(90).setHidable(true);
        estimacionesGrid.getColumn(PROVEEDOR_PROPERTY).setMaximumWidth(300);
        estimacionesGrid.getColumn(FECHA_PROPERTY).setMaximumWidth(120);
        estimacionesGrid.getColumn(MONTO_PROPERTY).setMaximumWidth(120);
        estimacionesGrid.getColumn(PROVISION_PROPERTY).setMaximumWidth(120);
        estimacionesGrid.getColumn(TOTAL_PROPERTY).setMaximumWidth(120);
        estimacionesGrid.getColumn(MONEDA_PROPERTY).setMaximumWidth(100);
        estimacionesGrid.getColumn(ESTATUS_PROPERTY).setMaximumWidth(120);
        estimacionesGrid.getColumn(PARTIDA_PROPERTY).setMaximumWidth(100);
        estimacionesGrid.getColumn(MONTOSF_PROPERTY).setMaximumWidth(10).setHidable(true).setHidden(true);
        estimacionesGrid.getColumn(PROVISIONSF_PROPERTY).setMaximumWidth(10).setHidable(true).setHidden(true);
        estimacionesGrid.getColumn(TOTALSF_PROPERTY).setMaximumWidth(10).setHidable(true).setHidden(true);

        estimacionesGrid.getColumn(PARTIDA_PROPERTY).setRenderer(new ButtonRenderer(e
                -> VerImagen(e)
        ));
        
        estimacionesGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROVISION_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TOTAL_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        HeaderRow filterRow = estimacionesGrid.appendHeaderRow();

        HeaderCell cell0 = filterRow.getCell(ESTIMACION_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(5);

        filterField0.addTextChangeListener(change -> {
            container.removeContainerFilters(ESTIMACION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(ESTIMACION_PROPERTY,
                                change.getText(), true, false));
            }
            footer.getCell(ESTIMACION_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            setTotal();
        });
        cell0.setComponent(filterField0);

        HeaderCell cellA = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterFieldA = new TextField();
        filterFieldA.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldA.setInputPrompt("Filtrar");
        filterFieldA.setColumns(15);

        filterFieldA.addTextChangeListener(change -> {
            container.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
            footer.getCell(PROVEEDOR_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            setTotal();
        });
        cellA.setComponent(filterFieldA);

        HeaderCell cell = filterRow.getCell(ESTATUS_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(ESTATUS_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(ESTATUS_PROPERTY,
                                change.getText(), true, false));
            }
            footer.getCell(PROVEEDOR_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            setTotal();
        });
        cell.setComponent(filterField);

        HeaderCell cell2 = filterRow.getCell(MONEDA_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(8);

        filterField2.addTextChangeListener(change -> {
            container.removeContainerFilters(MONEDA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(MONEDA_PROPERTY,
                                change.getText(), true, false));
            }
            footer.getCell(PROVEEDOR_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            setTotal();
        });
        cell2.setComponent(filterField2);

        HeaderCell cell3 = filterRow.getCell(PARTIDA_PROPERTY);

        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(10);

        filterField3.addTextChangeListener(change -> {
            container.removeContainerFilters(PARTIDA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(PARTIDA_PROPERTY,
                                change.getText(), true, false));
            }
            footer.getCell(PROVEEDOR_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            setTotal();
        });
        cell3.setComponent(filterField3);

        footer = estimacionesGrid.appendFooterRow();
        footer.getCell(PROVEEDOR_PROPERTY).setText("0 REGISTROS");
        footer.getCell(PROVEEDOR_PROPERTY).setStyleName("rightalign");
        footer.getCell(MONTO_PROPERTY).setText("0.00");
        footer.getCell(MONTO_PROPERTY).setStyleName("rightalign");
        footer.getCell(PROVISION_PROPERTY).setText("0.00");
        footer.getCell(PROVISION_PROPERTY).setStyleName("rightalign");
        footer.getCell(TOTAL_PROPERTY).setText("0.00");
        footer.getCell(TOTAL_PROPERTY).setStyleName("rightalign");

        estimacionesGrid.setFooterVisible(true);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        newBtn = new Button("Nueva estimación");
        newBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        newBtn.setWidth(175, Sizeable.UNITS_PIXELS);
        newBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newBtn.setDescription("Registrar nueva estimación");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                EstimacionesWindow estimacionesWindow = 
                        new EstimacionesWindow(
                                empresa
                        );
                mainUI.addWindow(estimacionesWindow);
                estimacionesWindow.center();
            }
        });

        editBtn = new Button("Detalle");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(130, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Ver o actualizar detalle de la estimación");
        editBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (estimacionesGrid.getSelectedRow() != null) {
/**                    
                    EstimacionesWindow estimacionesWindow = 
                            new EstimacionesWindow(
                                    String.valueOf(estimacionesGrid.getContainerDataSource().getItem(estimacionesGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()),
                                    empresa
                            );
                    mainUI.addWindow(estimacionesWindow);
                    estimacionesWindow.center();
**/               
                } else {
                    Notification.show("DEBE ELEJIR UN REGISRO!", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        deleteBtn = new Button("Eliminar");
        deleteBtn.setIcon(FontAwesome.TRASH);
        deleteBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        deleteBtn.setDescription("Eliminar esta estimación.");
        deleteBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().equals("ADMINISTRADOR")) {
                    if (estimacionesGrid.getSelectedRow() == null) {
                        Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                    } else {
                        if(String.valueOf(estimacionesGrid.getContainerDataSource().getItem(estimacionesGrid.getSelectedRow()).getItemProperty(EstimacionesView.ESTATUS_PROPERTY).getValue()).equals("PAGADA")) {
                            Notification.show("NO SE PUEDE ELIMINAR UNA ESTIMACION YA PAGADA.", Notification.Type.ERROR_MESSAGE);
                            return;
                        }

                        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro?",
                                "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    deleteEstimacion();
                                }
                            }
                        });
                    }
                } else {
                    Notification.show("Usuario no tiene permiso para esta acción!", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(deleteBtn);
        buttonsLayout.setComponentAlignment(newBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(editBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(deleteBtn, Alignment.BOTTOM_RIGHT);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void fillTable() {

        if (container == null) {
            return;
        }

        container.removeAllItems();

        footer.getCell(PROVEEDOR_PROPERTY).setText("0 REGISTROS");
        footer.getCell(MONTO_PROPERTY).setText("0.00");

        if(inicioDt.getValue().after(finDt.getValue())) {
            Notification.show("La fecha inicial no puede ser mayor a la fecha final, revise!", Notification.Type.WARNING_MESSAGE);
            inicioDt.focus();
            return;
        }
        
        String queryString ;

        queryString = "Select Est.*, Prov.Nombre";
        queryString += " From  estimacion Est ";
        queryString += " Inner Join proveedor Prov On Prov.IdProveedor = Est.IdProveedor";
        queryString += " WHERE Est.Fecha >= '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
        queryString += " AND   Est.Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue())    + "'";
        queryString += " AND   Est.IdEmpresa = " + empresa;

System.out.println("\nQueryEstimacion="+queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalProvision = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalEst = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
                
                do {
                    Object itemId = container.addItem();                    

                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("EstimacionId"));
                    container.getContainerProperty(itemId, ESTIMACION_PROPERTY).setValue(rsRecords.getString("CorrelativoEstimacion"));
                    container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("Nombre") );
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(df.format(rsRecords.getDate("Fecha")));
                    container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                    container.getContainerProperty(itemId, MONTO_PROPERTY).setValue(moneyFormat.format(rsRecords.getDouble("Monto")));
                    container.getContainerProperty(itemId, PROVISION_PROPERTY).setValue(moneyFormat.format(rsRecords.getDouble("Provision")));
                    container.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(moneyFormat.format(rsRecords.getDouble("TotalEstimacion")));
                    container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    if (rsRecords.getString("Estatus").equals("FACTURADA")) {
                        container.getContainerProperty(itemId, PARTIDA_PROPERTY).setValue(rsRecords.getString("DocumentoSerie") + " " + rsRecords.getString("DOCUMENTO"));
                    } else {
                        container.getContainerProperty(itemId, PARTIDA_PROPERTY).setValue("");

                    }
                    container.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(rsRecords.getDouble("Monto"));
                    container.getContainerProperty(itemId, PROVISIONSF_PROPERTY).setValue(rsRecords.getDouble("Provision"));
                    container.getContainerProperty(itemId, TOTALSF_PROPERTY).setValue(rsRecords.getDouble("TotalEstimacion"));
                    total = total.add(new BigDecimal(rsRecords.getDouble("Monto")).setScale(2, BigDecimal.ROUND_HALF_UP));
                    totalProvision = totalProvision.add(new BigDecimal(rsRecords.getDouble("Provision")).setScale(2, BigDecimal.ROUND_HALF_UP));
                    totalEst       = totalEst.add(new BigDecimal(rsRecords.getDouble("TotalEstimacion")).setScale(2, BigDecimal.ROUND_HALF_UP));
                    
                } while (rsRecords.next());

                rsRecords.last();
                footer.getCell(PROVEEDOR_PROPERTY).setText(String.valueOf(rsRecords.getRow()) + " REGISTROS");
                footer.getCell(MONTO_PROPERTY).setText(moneyFormat.format(total));
                footer.getCell(PROVISION_PROPERTY).setText(moneyFormat.format(totalProvision));
                footer.getCell(TOTAL_PROPERTY).setText(moneyFormat.format(totalEst));
                
                estimacionesGrid.select(container.firstItemId());
                
            }
        } catch (Exception ex) {
            Logger.getLogger(EstimacionesView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de estimaciones : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de estimaciones..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void setTotal() {
        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        for (Object rid: estimacionesGrid.getContainerDataSource()
                     .getItemIds()) {
                     total = total.add(new BigDecimal(
                             Double.parseDouble(
                                     String.valueOf(container.getContainerProperty(rid, MONTOSF_PROPERTY).getValue())
                             )));
        }
        footer.getCell(MONTO_PROPERTY).setText(moneyFormat.format(total));
    }
        

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub
        Page.getCurrent().setTitle("Sopdi - Estimaciones");
    }

    private void deleteEstimacion() {

        String queryString = "";

        queryString = "Delete ";
        queryString += " From  estimacion";
        queryString += " Where IdEstimacion = " + String.valueOf(estimacionesGrid.getContainerDataSource().getItem(estimacionesGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue());

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("Estimación elliminada exitosamente!", Notification.Type.TRAY_NOTIFICATION);

            fillTable();
        } catch (Exception ex) {
            Logger.getLogger(EstimacionesView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al ELIINAR registros : " + ex.getMessage());
            Notification.show("Error al ELIMINAR registros...!", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void VerImagen(ClickableRenderer.RendererClickEvent e) {

        String idPartidaContable = String.valueOf(container.getContainerProperty(e.getItemId(), ID_PROPERTY).getValue());

        estimacionesGrid.select(e.getItemId());

        String queryString;

        try {

            queryString = "Select * ";
            queryString += " From contabilidad_partida";
            queryString += " Where IdPartidaContableDocumento = " + idPartidaContable;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (!rsRecords.next()) { //  no encontrado  
                Notification.show("Documento scan no disponible para visualizar!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            if (rsRecords.getObject("Archivo") == null) {
                Notification.show("Documento scan no disponible para visualizar!");

                return;
            }

            Window window = new Window();
            window.setResizable(true);
            window.setWidth("95%");
            window.setHeight("95%");

            final byte docBytes[] = rsRecords.getBytes("Archivo");
            final String fileName = rsRecords.getString("ArchivoNombre");
            StreamResource documentStreamResource = null;

            if (docBytes != null) {
                documentStreamResource = new StreamResource(
                        new StreamResource.StreamSource() {
                    public InputStream getStream() {
                        return new ByteArrayInputStream(docBytes);
                    }
                }, fileName
                );
            }
            documentStreamResource.setMIMEType(rsRecords.getString("ArchivoTipo"));
            documentStreamResource.setFilename(rsRecords.getString("ArchivoNombre"));
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

            System.out.println("Tipo de Archivo : " + rsRecords.getString("ArchivoTipo"));
            System.out.println("Archivo Nombre :" + rsRecords.getString("ArchivoNombre"));
            System.out.println("Tamanio :" + rsRecords.getString("ArchivoPeso"));

            if (rsRecords.getString("ArchivoTipo").contains("pdf")) {

                BrowserFrame browserFrame = new BrowserFrame();
                browserFrame.setSizeFull();

                browserFrame.setSource(documentStreamResource);
                window.setContent(browserFrame);

            } else {
                window.setWidth("90%");
                window.setHeight("90%");
                VerticalLayout imageLayout = new VerticalLayout();
                imageLayout.setSizeFull();

                Image imageViewComponent = new Image(null, documentStreamResource);
                imageViewComponent.setImmediate(true);
                imageViewComponent.setSizeFull();
                imageViewComponent.addStyleName("my-img-button");
                imageViewComponent.setData(idPartidaContable);

                imageLayout.addComponent(imageViewComponent);
                imageLayout.setComponentAlignment(imageViewComponent, Alignment.MIDDLE_CENTER);

                Panel imagePanel = new Panel();
                imagePanel.setResponsive(true);
                imagePanel.setContent(imageLayout);

                window.setContent(imagePanel);
            }

            UI.getCurrent().addWindow(window);
            window.center();

        } catch (Exception allEx) {
            Notification.show("Error al intentar mostrar el archivo.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }
}
