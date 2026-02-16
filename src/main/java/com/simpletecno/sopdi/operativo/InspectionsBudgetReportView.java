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
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class InspectionsBudgetReportView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;

    public Statement stQuery1 = null;
    public ResultSet rsRecords1 = null;

    static final String ID_PROPERTY = "Id";
    static final String CODIGO_VISITA_PROPERTY = "Visita";
    static final String FECHA_PROPERTY = "Fecha";
    static final String MOTIVO_PROPERTY = "Motivo";
    static final String CLIENTE_PROPERTY = "Cliente";
    static final String SUPERVISOR_PROPERTY = "Supervisor";
    static final String CENTRO_COSTO_PROPERTY = "Centro costo";
    static final String CREADACLIENTE_PROPERTY = "Usuario";
    static final String IDCENTRO_COSTO_PROPERTY = "IdCC";

    Utileria utileria = new Utileria();
    MarginInfo marginInfo;

    public IndexedContainer container = new IndexedContainer();
    Grid inspectionsGrid;
    FooterRow footer;

    Button printPdfBtn;

    UI mainUI;

    public InspectionsBudgetReportView() {
        this.mainUI = UI.getCurrent();

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);

        marginInfo = new MarginInfo(true, true, false, true);

        Label titleLbl = new Label("Visitas de cliente con tareas y presupuesto");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createInspectionsGrid();
        createButtons();

        fillInspectionsTable();

    }

    private void createInspectionsGrid() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("95%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(false);

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(CODIGO_VISITA_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(MOTIVO_PROPERTY, String.class, null);
        container.addContainerProperty(CLIENTE_PROPERTY, String.class, null);
        container.addContainerProperty(SUPERVISOR_PROPERTY, String.class, null);
        container.addContainerProperty(CENTRO_COSTO_PROPERTY, String.class, null);
        container.addContainerProperty(IDCENTRO_COSTO_PROPERTY, String.class, null);

        inspectionsGrid = new Grid("", container);

//        inspectionsGrid.addStyleName("smallgrid");
        inspectionsGrid.setImmediate(true);
        inspectionsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        inspectionsGrid.setDescription("Seleccione un registro.");
        inspectionsGrid.setHeightMode(HeightMode.ROW);
        inspectionsGrid.setHeightByRows(10);
        inspectionsGrid.setWidth("100%");
        inspectionsGrid.setResponsive(true);
        inspectionsGrid.setEditorBuffered(false);

        reportLayout.addComponent(inspectionsGrid);
        reportLayout.setComponentAlignment(inspectionsGrid, Alignment.MIDDLE_CENTER);

        inspectionsGrid.getColumn(ID_PROPERTY).setMaximumWidth(90).setHidable(true).setHidden(true);
        //inspectionsGrid.getColumn(CODIGO_VISITA_PROPERTY).setMaximumWidth(125);
        //inspectionsGrid.getColumn(FECHA_PROPERTY).setMaximumWidth(110);
        //inspectionsGrid.getColumn(CLIENTE_PROPERTY).setMaximumWidth(200);
        //inspectionsGrid.getColumn(SUPERVISOR_PROPERTY).setMaximumWidth(200);
        //inspectionsGrid.getColumn(MOTIVO_PROPERTY).setMaximumWidth(200);

        inspectionsGrid.getColumn(CENTRO_COSTO_PROPERTY).setHidden(true);

        inspectionsGrid.getColumn(IDCENTRO_COSTO_PROPERTY).setHidden(true);
        /**
         * inspectionsGrid.addListener(new ItemClickEvent.ItemClickListener() {
         * public void itemClick(ItemClickEvent event) {
         * inspectionsGrid.select(event.getItemId()); if (event.isDoubleClick())
         * { if(inspectionsGrid.getSelectedRow() != null) { InspectionForm
         * newInspectionForm = new
         * InspectionForm(String.valueOf(inspectionsGrid.getContainerDataSource().getItem(inspectionsGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()));
         * mainUI.addWindow(newInspectionForm); newInspectionForm.center();
         * newInspectionForm.fillData(); newInspectionForm.motivoCbx.focus(); }
         * } } });
         *
         */
        HeaderRow filterRow = inspectionsGrid.appendHeaderRow();

        HeaderCell cellA = filterRow.getCell(CODIGO_VISITA_PROPERTY);

        TextField filterFieldA = new TextField();
        filterFieldA.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldA.setInputPrompt("Filtrar");
        filterFieldA.setColumns(8);

        filterFieldA.addTextChangeListener(change -> {
            container.removeContainerFilters(CODIGO_VISITA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(CODIGO_VISITA_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            }
        });
        cellA.setComponent(filterFieldA);

        HeaderCell cell = filterRow.getCell(FECHA_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(FECHA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(FECHA_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell1 = filterRow.getCell(CLIENTE_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(15);

        filterField1.addTextChangeListener(change -> {
            container.removeContainerFilters(CLIENTE_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(CLIENTE_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            }
        });
        cell1.setComponent(filterField1);

        HeaderCell cell2 = filterRow.getCell(SUPERVISOR_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(10);

        filterField2.addTextChangeListener(change -> {
            container.removeContainerFilters(SUPERVISOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(SUPERVISOR_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            }
        });
        cell2.setComponent(filterField2);

        HeaderCell cell3 = filterRow.getCell(MOTIVO_PROPERTY);

        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(15);

        filterField3.addTextChangeListener(change -> {
            container.removeContainerFilters(SUPERVISOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(MOTIVO_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(container.size()) + " REGISTROS");
            }
        });
        cell3.setComponent(filterField3);

        footer = inspectionsGrid.appendFooterRow();
        footer.getCell(CLIENTE_PROPERTY).setText("0 REGISTROS");
        footer.getCell(CLIENTE_PROPERTY).setStyleName("rightalign");

        inspectionsGrid.setFooterVisible(true);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        printPdfBtn = new Button("Generar PDF");
        printPdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        printPdfBtn.setWidth(130, Sizeable.UNITS_PIXELS);
//        printPdfBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        printPdfBtn.setDescription("Imprimir reporte de presupuesto");
        printPdfBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                InspectionBudgetReportPDF inspectionBudgetReportPDF
                        = new InspectionBudgetReportPDF(String.valueOf(inspectionsGrid.getContainerDataSource().getItem(inspectionsGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()));
                mainUI.addWindow(inspectionBudgetReportPDF);
                inspectionBudgetReportPDF.center();
                //inspectionBudgetReportPDF.
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(printPdfBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void fillInspectionsTable() {

        if (container == null) {
            return;
        }

        container.removeAllItems();

        footer.getCell(CLIENTE_PROPERTY).setText("0 REGISTROS");

        String queryString;

        queryString = "Select Distinct Vis.*, Cli.Nombre ClienteNombre, CC.CodigoCentroCosto";
        queryString += " From visita_inspeccion Vis ";
        queryString += " Inner Join proveedor Cli On Cli.IdProveedor = Vis.IdCliente";
        queryString += " Inner Join visita_inspeccion_tarea Tar On Tar.IdVisitaInspeccion = Vis.IdVisitaInspeccion And Tar.Presupuesto = 'SI'";
        queryString += " Left  Join centro_costo CC On CC.IdCentroCosto = Vis.IdCentroCosto";
        queryString += " Where Vis.IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " And Vis.FechaYHoraInicio >= '2019-01-01 00:00:00'";
        queryString += " And Vis.Motivo In ('Cliente', 'Residente')";
        queryString += " Order By Vis.CodigoVisita Desc";

//System.out.println("\n\n"+queryString);

        try {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                

                do {
                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdVisitaInspeccion"));
                    container.getContainerProperty(itemId, CODIGO_VISITA_PROPERTY).setValue(rsRecords.getString("CodigoVisita"));
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaYHoraInicio")));
                    container.getContainerProperty(itemId, MOTIVO_PROPERTY).setValue(rsRecords.getString("Motivo"));
                    container.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(rsRecords.getString("ClienteNombre"));
                    container.getContainerProperty(itemId, SUPERVISOR_PROPERTY).setValue("otro dato");
                    container.getContainerProperty(itemId, CENTRO_COSTO_PROPERTY).setValue(rsRecords.getString("CodigoCentroCosto"));
                    container.getContainerProperty(itemId, IDCENTRO_COSTO_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));

                } while (rsRecords.next());
                //  rsRecords.last();
               
            }

        } catch (Exception ex) {
            Logger.getLogger(InspectionsBudgetReportView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de visita  : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de visitas...!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub
        Page.getCurrent().setTitle("Sopdi - Reporte de presupuesto de tareas");
    }

}
