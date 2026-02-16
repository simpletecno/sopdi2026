/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SeguimientoHandler;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class InspectionsTaskTrackView extends VerticalLayout implements View {

    public Statement stQuery = null, stQuery1 = null;
    public ResultSet rsRecords = null, rsRecords1 = null;
    static PreparedStatement stPreparedQuery;

    public IndexedContainer taskContainer = new IndexedContainer();
    Grid inspectionsTaskTrackGrid;

    static final String ID_PROPERTY = "Id";
    static final String CODIGO_TAREA_PROPERTY = "Código";
    static final String FECHA_PROPERTY = "Fecha";
    static final String LOTE_PROPERTY = "Lote";
    static final String IDCC_PROPERTY = "IDCC";
    static final String CLIENTE_PROPERTY = "Cliente ";
    static final String INSTRUCCION_PROPERTY = "Descripción ";
    static final String RESPONSABLE_PROPERTY = "Responsable";
    static final String EJECUTOR_PROPERTY = "Ejecutor";
    static final String GARANTIA_PROPERTY = "Garantia";
    static final String PRESUPUESTO_PROPERTY = "Presupuesto";
    static final String MONTO_PRESUPUESTO_PROPERTY = "Total Presupuesto";
    static final String MONTO_OC_PROPERTY = "Total OC";
    static final String MONTO_DOLARES_OC_PROPERTY = "Total OC $";
    static final String AUTORIZADO_TIPO_PROPERTY = "Autoriza";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String ARCHIVADO_PROPERTY = "Archivado";
    static final String ULTIMO_ESTATUS_PROPERTY = "Ultimo estatus";

    FooterRow footer;

    MarginInfo marginInfo;

    TabSheet tabSheet;
    public IndexedContainer notasContainer = new IndexedContainer();
    Grid notasGrid;
    public IndexedContainer ocContainer = new IndexedContainer();
    Grid ocGrid;

    static final String OBSERVACION_PROPERTY = "Observacion";
    static final String FECHA_HORA_PROPERTY = "Fecha y hora";
    static final String USUARIO_PROPERTY = "Usuario";

    static final String IDOC_PROPERTY = "IDOC";
    static final String CODIGOOC_PROPERTY = "CodigoOC";
    static final String TOTAL_OC_PROPERTY = "Total";

    public static Locale locale = new Locale("ES", "GT");
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    UI mainUI;

    String queryString = "";

    OptionGroup ordenCambioOg = new OptionGroup();
    CheckBox archivadoChbx = new CheckBox("Archivados");

    public InspectionsTaskTrackView() {
        this.mainUI = UI.getCurrent();

//        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);
        setHeightUndefined();

        marginInfo = new MarginInfo(true, true, true, false);

        Label titleLbl = new Label("Seguimiento de tareas");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

        Button refreshBtn = new Button("Refrescar");
        refreshBtn.setIcon(FontAwesome.REFRESH);
        refreshBtn.setDescription("Refrescar listado");
        refreshBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillInspectionsTaskGrid();
            }
        });

        ordenCambioOg.setStyleName("horizontal");
        ordenCambioOg.addItems("Todas", "Sin Orden de cambio", "Con Orden de cambio");
        ordenCambioOg.select("Todas");
        ordenCambioOg.addValueChangeListener( e -> {
            fillInspectionsTaskGrid();
        });

//        archivadoChbx.addStyleName(ValoTheme.CHECKBOX_LARGE);
        archivadoChbx.addValueChangeListener(e -> {
            fillInspectionsTaskGrid();
        });

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);
        titleLayout.addComponents(ordenCambioOg, archivadoChbx);
        titleLayout.setComponentAlignment(ordenCambioOg, Alignment.MIDDLE_RIGHT);
        titleLayout.setComponentAlignment(archivadoChbx, Alignment.MIDDLE_RIGHT);
        titleLayout.addComponent(refreshBtn);
        titleLayout.setComponentAlignment(refreshBtn, Alignment.MIDDLE_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        tabSheet = new TabSheet();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.setWidth("100%");

        createInspectionsTaskGrid();

        createTablaNotasSeguimiento();

        createTablaOrdenesCambio();

        createButtons();

        fillInspectionsTaskGrid();

        addComponent(tabSheet);

    }

    private void createInspectionsTaskGrid() {
        HorizontalLayout ocLayout = new HorizontalLayout();
        ocLayout.setWidth("100%");
        ocLayout.addStyleName("rcorners3");
        ocLayout.setResponsive(true);
        ocLayout.setHeightUndefined();
        ocLayout.setMargin(new MarginInfo(true, false, true, false));

        taskContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(CODIGO_TAREA_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(LOTE_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(IDCC_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(CLIENTE_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(INSTRUCCION_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(RESPONSABLE_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(EJECUTOR_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(GARANTIA_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(PRESUPUESTO_PROPERTY, String.class, "0.00");
        taskContainer.addContainerProperty(MONTO_PRESUPUESTO_PROPERTY, String.class, "000");
        taskContainer.addContainerProperty(MONTO_OC_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(MONTO_DOLARES_OC_PROPERTY, String.class, "0.00");
        taskContainer.addContainerProperty(AUTORIZADO_TIPO_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(ARCHIVADO_PROPERTY, String.class, null);
        taskContainer.addContainerProperty(ULTIMO_ESTATUS_PROPERTY, String.class, null);
//        taskContainer.addContainerProperty(CODIGOOC_PROPERTY, String.class, "0");

        inspectionsTaskTrackGrid = new Grid("", taskContainer);
        inspectionsTaskTrackGrid.setImmediate(true);
        inspectionsTaskTrackGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        inspectionsTaskTrackGrid.setDescription("Seleccione un registro.");
//        inspectionsTaskTrackGrid.setHeightMode(HeightMode.ROW);
//        inspectionsTaskTrackGrid.setHeightByRows(10);
        inspectionsTaskTrackGrid.setWidth("100%");
        inspectionsTaskTrackGrid.setResponsive(true);
//        inspectionsTaskTrackGrid.setSizeFull();

        inspectionsTaskTrackGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (inspectionsTaskTrackGrid.getSelectedRow() != null) {
                    llenarNotasGrid();
                    fillOcGrid();
                }
            }
        });

        /*
        inspectionsTaskTrackGrid.addShortcutListener(new ShortcutListener("TAB",
                ShortcutAction.KeyCode.TAB, null) {

            @Override
            public void handleAction(Object sender, Object target) {

                Object targetParent = ((AbstractComponent) target).getParent();
                if ((targetParent != null) && (targetParent instanceof Grid)) {

                    Grid targetGrid = (Grid) targetParent;

                    if (targetGrid.isEditorActive()) {
                        try {
                            if (inspectionsTaskTrackGrid.getEditedItemId() != null) {

                                String id = String.valueOf(container.getContainerProperty(inspectionsTaskTrackGrid.getEditedItemId(), ID_PROPERTY).getValue());
                                String estatus = String.valueOf(container.getContainerProperty(inspectionsTaskTrackGrid.getEditedItemId(), ESTATUS_PROPERTY).getValue()) + "'";

                                queryString = "UPDATE  visita_inspeccion_tarea SET ";
                                queryString += "Estatus = '" + estatus + "'";
                                //queryString += ",FechaUltimoEstatus = ' " + Utileria.getFechaDDMMYYYY_HHMM_2(new java.util.Date()) + "'";
                                queryString += " WHERE IdVisitaInspeccionTarea = " + id;

                                System.out.println("entra a esta funcion");

                                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                stQuery.executeUpdate(queryString);

                                Notification.show("Registro modificado!!!", Notification.Type.WARNING_MESSAGE);

                                inspectionsTaskTrackGrid.saveEditor();
                                inspectionsTaskTrackGrid.cancelEditor();

                                System.out.println("Id " + id);
                                System.out.println("Estatus" + estatus);
                            }

                            Notification.show("Registro modificado!!!", Notification.Type.WARNING_MESSAGE);

                        } catch (CommitException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (SQLException ex) {
                            Logger.getLogger(InspectionsTaskTrackView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

        });
         */
        inspectionsTaskTrackGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        inspectionsTaskTrackGrid.getColumn(FECHA_PROPERTY).setMaximumWidth(110).setHidable(true).setHidden(true);

        inspectionsTaskTrackGrid.getColumn(CODIGO_TAREA_PROPERTY).setMaximumWidth(140);
        inspectionsTaskTrackGrid.getColumn(LOTE_PROPERTY).setMaximumWidth(60);
        inspectionsTaskTrackGrid.getColumn(IDCC_PROPERTY).setMaximumWidth(80);
        inspectionsTaskTrackGrid.getColumn(CLIENTE_PROPERTY).setMaximumWidth(150);
        inspectionsTaskTrackGrid.getColumn(INSTRUCCION_PROPERTY).setMaximumWidth(230);
        inspectionsTaskTrackGrid.getColumn(RESPONSABLE_PROPERTY).setMaximumWidth(130);
        inspectionsTaskTrackGrid.getColumn(EJECUTOR_PROPERTY).setMaximumWidth(100);
        inspectionsTaskTrackGrid.getColumn(GARANTIA_PROPERTY).setMaximumWidth(85);
        inspectionsTaskTrackGrid.getColumn(PRESUPUESTO_PROPERTY).setMaximumWidth(85);
        inspectionsTaskTrackGrid.getColumn(ESTATUS_PROPERTY).setMaximumWidth(100);
        inspectionsTaskTrackGrid.getColumn(ULTIMO_ESTATUS_PROPERTY).setMaximumWidth(150);
        inspectionsTaskTrackGrid.getColumn(MONTO_PRESUPUESTO_PROPERTY).setMaximumWidth(100);
        inspectionsTaskTrackGrid.getColumn(MONTO_OC_PROPERTY).setMaximumWidth(100);
        inspectionsTaskTrackGrid.getColumn(MONTO_DOLARES_OC_PROPERTY).setMaximumWidth(100);
        inspectionsTaskTrackGrid.getColumn(AUTORIZADO_TIPO_PROPERTY).setMaximumWidth(130);
//        inspectionsTaskTrackGrid.getColumn(CODIGOOC_PROPERTY).setHidable(true).setHidden(true);

        inspectionsTaskTrackGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (CODIGO_TAREA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (GARANTIA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (ARCHIVADO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (PRESUPUESTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (MONTO_PRESUPUESTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (MONTO_DOLARES_OC_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        HeaderRow filterRow = inspectionsTaskTrackGrid.appendHeaderRow();
        HeaderCell cellA = filterRow.getCell(CODIGO_TAREA_PROPERTY);
        TextField filterFieldA = new TextField();
        filterFieldA.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldA.setInputPrompt("Filtrar");
        filterFieldA.setColumns(8);
        filterFieldA.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(CODIGO_TAREA_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(CODIGO_TAREA_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cellA.setComponent(filterFieldA);

        HeaderCell cell = filterRow.getCell(FECHA_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);
        filterField.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(FECHA_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(FECHA_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell1 = filterRow.getCell(INSTRUCCION_PROPERTY);
        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(15);
        filterField1.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(INSTRUCCION_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(INSTRUCCION_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cell1.setComponent(filterField1);

        HeaderCell cellRes = filterRow.getCell(RESPONSABLE_PROPERTY);
        TextField filterRes = new TextField();
        filterRes.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterRes.setInputPrompt("Filtrar");
        filterRes.setColumns(15);
        filterRes.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(RESPONSABLE_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(RESPONSABLE_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cellRes.setComponent(filterRes);

        HeaderCell cellEjecutor = filterRow.getCell(EJECUTOR_PROPERTY);
        TextField filterEjecutor = new TextField();
        filterEjecutor.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterEjecutor.setInputPrompt("Filtrar");
        filterEjecutor.setColumns(15);
        filterEjecutor.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(EJECUTOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(EJECUTOR_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cellEjecutor.setComponent(filterEjecutor);

        HeaderCell cellG = filterRow.getCell(GARANTIA_PROPERTY);
        TextField filterFieldG = new TextField();
        filterFieldG.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldG.setInputPrompt("Filtrar");
        filterFieldG.setColumns(5);
        filterFieldG.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(GARANTIA_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(GARANTIA_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cellG.setComponent(filterFieldG);

        HeaderCell cell3 = filterRow.getCell(PRESUPUESTO_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(5);
        filterField3.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(PRESUPUESTO_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(PRESUPUESTO_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cell3.setComponent(filterField3);

        HeaderCell cell4 = filterRow.getCell(AUTORIZADO_TIPO_PROPERTY);
        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(8);
        filterField4.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(AUTORIZADO_TIPO_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(AUTORIZADO_TIPO_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cell4.setComponent(filterField4);

        HeaderCell cellLote = filterRow.getCell(LOTE_PROPERTY);
        TextField filterFieldLote = new TextField();
        filterFieldLote.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldLote.setInputPrompt("Filtrar");
        filterFieldLote.setColumns(4);
        filterFieldLote.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(LOTE_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(LOTE_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cellLote.setComponent(filterFieldLote);

        HeaderCell cell5 = filterRow.getCell(IDCC_PROPERTY);
        TextField filterField5 = new TextField();
        filterField5.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField5.setInputPrompt("Filtrar");
        filterField5.setColumns(6);
        filterField5.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(IDCC_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(IDCC_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cell5.setComponent(filterField5);

        HeaderCell cell6 = filterRow.getCell(CLIENTE_PROPERTY);
        TextField filterField6 = new TextField();
        filterField6.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField6.setInputPrompt("Filtrar");
        filterField6.setColumns(8);
        filterField6.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(CLIENTE_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(CLIENTE_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cell6.setComponent(filterField6);

        HeaderCell cell7 = filterRow.getCell(ESTATUS_PROPERTY);
        TextField filterField7 = new TextField();
        filterField7.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField7.setInputPrompt("Filtrar");
        filterField7.setColumns(8);
        filterField7.addTextChangeListener(change -> {
            taskContainer.removeContainerFilters(ESTATUS_PROPERTY);
            if (!change.getText().isEmpty()) {
                taskContainer.addContainerFilter(
                        new SimpleStringFilter(ESTATUS_PROPERTY,
                                change.getText(), true, false));
                footer.getCell(INSTRUCCION_PROPERTY).setText(String.valueOf(taskContainer.size()) + " TAREAS");
            }
        });
        cell7.setComponent(filterField7);

        footer = inspectionsTaskTrackGrid.appendFooterRow();
        footer.getCell(INSTRUCCION_PROPERTY).setText("0 TAREAS");
        footer.getCell(INSTRUCCION_PROPERTY).setStyleName("rightalign");

        ocLayout.addComponent(inspectionsTaskTrackGrid);
        ocLayout.setComponentAlignment(inspectionsTaskTrackGrid, Alignment.TOP_CENTER);

        addComponent(ocLayout);
        setComponentAlignment(ocLayout, Alignment.TOP_CENTER);

    }

    public boolean exportToExcel() {
        if (inspectionsTaskTrackGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(inspectionsTaskTrackGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
// produccion            fileexport = (empresa + "_" + empresaLbl.getValue().substring(5, empresaLbl.getValue().length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_INTEGRACION_CAMBIOS.xlsx").replaceAll(" ", "").replaceAll(",", "");
            fileexport = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "_" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_TAREAS.xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    public void createTablaNotasSeguimiento() {

//        VerticalLayout ocLayout = new VerticalLayout();
//        ocLayout.setWidth("100%");
//        ocLayout.addStyleName("rcorners3");
//        ocLayout.setHeight(UI.getCurrent().getHeight(), Sizeable.UNITS_PIXELS);
//        ocLayout.setMargin(new MarginInfo(true, false, true, false));

        notasContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        notasContainer.addContainerProperty(OBSERVACION_PROPERTY, String.class, null);
        notasContainer.addContainerProperty(FECHA_HORA_PROPERTY, String.class, null);
        notasContainer.addContainerProperty(USUARIO_PROPERTY, String.class, null);

        notasGrid = new Grid("NOTAS O SEGUIMIENTO ", notasContainer);
        notasGrid.setImmediate(true);
        notasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        notasGrid.setDescription("Seleccione un registro.");
        notasGrid.setHeightByRows(5);
        notasGrid.setHeightMode(HeightMode.ROW);
        notasGrid.setWidth("100%");
        notasGrid.setEditorBuffered(false);
        notasGrid.setEditorEnabled(false);

        notasGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);

//        ocLayout.addComponent(notasGrid);
//        ocLayout.setComponentAlignment(notasGrid, Alignment.BOTTOM_CENTER);

        tabSheet.addTab(notasGrid, "Seguimiento", FontAwesome.ENVELOPE_SQUARE);

    }

    public void createTablaOrdenesCambio() {

//        VerticalLayout ocLayout = new VerticalLayout();
//        ocLayout.setWidth("100%");
//        ocLayout.addStyleName("rcorners3");
//        ocLayout.setHeight(UI.getCurrent().getHeight(), Sizeable.UNITS_PIXELS);
//        ocLayout.setMargin(new MarginInfo(true, false, true, false));

        ocContainer.addContainerProperty(IDOC_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(CODIGOOC_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(FECHA_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(USUARIO_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(TOTAL_OC_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, "");

        ocGrid = new Grid("ORDENES DE CAMBIO ", ocContainer);
        ocGrid.setWidth("100%");
        ocGrid.setImmediate(true);
        ocGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ocGrid.setEditorBuffered(false);
        ocGrid.setEditorEnabled(false);
        ocGrid.setDescription("Doble CLICK aqui para abrir Orden De Cambio.");
        ocGrid.setHeightByRows(5);
        ocGrid.setHeightMode(HeightMode.ROW);
        ocGrid.addItemClickListener(
                event -> {
                    if (event.isDoubleClick()) {
                        InspectionTaskOCWindow inspectionTaskOCWindow = new InspectionTaskOCWindow(
                                String.valueOf(ocContainer.getContainerProperty(event.getItemId(), IDOC_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CODIGO_TAREA_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), INSTRUCCION_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), AUTORIZADO_TIPO_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), IDCC_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CLIENTE_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), LOTE_PROPERTY).getValue())
                        );
                        UI.getCurrent().addWindow(inspectionTaskOCWindow);
                        inspectionTaskOCWindow.center();

                    }
                }
        );
        ocGrid.setRowStyleGenerator(line -> {
            String valor = String.valueOf(line.getItem().getItemProperty(ESTATUS_PROPERTY).getValue());
            if(!valor.trim().isEmpty()) {
                if (String.valueOf(line.getItem().getItemProperty(ESTATUS_PROPERTY).getValue()).equals("FINALIZADA") ) {
                    return "red";
                }
                else {
                    return "green";
                }
            }
            return null;
        });

//        ocLayout.addComponent(ocGrid);
//        ocLayout.setComponentAlignment(ocGrid, Alignment.BOTTOM_CENTER);

//        Button actualizarBtn = new Button("Actualizar");
//        actualizarBtn.setIcon(FontAwesome.REFRESH);
//        actualizarBtn.setDescription("Actualizar listado de OC");
//        actualizarBtn.addListener(new Button.ClickListener() {
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                fillOcGrid();
//            }
//        });
//
//        VerticalLayout botonActualizarLayout = new VerticalLayout();
//        botonActualizarLayout.setWidth("5%");
//        botonActualizarLayout.setHeight("100%");
//        botonActualizarLayout.setSpacing(true);
//        botonActualizarLayout.setMargin(new MarginInfo(false, true, false, false));
//        botonActualizarLayout.addComponent(actualizarBtn);
//        botonActualizarLayout.setComponentAlignment(actualizarBtn, Alignment.MIDDLE_RIGHT);
//
//        ocLayout.addComponent(botonActualizarLayout);
//        ocLayout.setComponentAlignment(botonActualizarLayout, Alignment.MIDDLE_RIGHT);

        tabSheet.addTab(ocGrid, "ORDENES DE CAMBIO", FontAwesome.EXCHANGE);

    }

    private void createButtons() {

        Button budgetBtn = new Button("Presupuesto");
        budgetBtn.setIcon(FontAwesome.CALCULATOR);
        budgetBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        budgetBtn.setDescription("Presupuesto PRELIMINAR");
        budgetBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (inspectionsTaskTrackGrid.getSelectedRow() != null) {
                    if (String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), PRESUPUESTO_PROPERTY).getValue()).equals("SI")) {
                        InspectionTaskBudgetWindow inspectionTaskBudgetWindow = new InspectionTaskBudgetWindow(
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CODIGO_TAREA_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), INSTRUCCION_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), AUTORIZADO_TIPO_PROPERTY).getValue())
                        );
                        UI.getCurrent().addWindow(inspectionTaskBudgetWindow);
                        inspectionTaskBudgetWindow.center();
                    } else {
                        Notification.show("Esta tarea no necesita presupuesto!", Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show("Por favor seleccione un registro!", Notification.Type.WARNING_MESSAGE);
                }

            }
        });

        Button changeOrderBtn = new Button("Orden de cambio");
        changeOrderBtn.setIcon(FontAwesome.EXCHANGE);
        changeOrderBtn.setDescription("Orden de cambio");
        changeOrderBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (inspectionsTaskTrackGrid.getSelectedRow() != null) {
//                    if(String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CODIGOOC_PROPERTY).getValue()).equals("0")) {
                        InspectionTaskOCWindow inspectionTaskOCWindow = new InspectionTaskOCWindow(
                                "0",
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CODIGO_TAREA_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), INSTRUCCION_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), AUTORIZADO_TIPO_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), IDCC_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CLIENTE_PROPERTY).getValue()),
                                String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), LOTE_PROPERTY).getValue())
                        );
                        UI.getCurrent().addWindow(inspectionTaskOCWindow);
                        inspectionTaskOCWindow.center();
//                    }
//                    else {
//                        Notification.show("POR FAVOR ELEGIR UNA TAREA QUE NO TENGA OC.", Notification.Type.WARNING_MESSAGE);
//                    }
                } else {
                    Notification.show("DEBE ELEJIR UN REGISRO!", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        Button imagenBtn = new Button("Img/Fotos");
        imagenBtn.setIcon(FontAwesome.PICTURE_O);
        imagenBtn.setWidth(110, Sizeable.UNITS_PIXELS);
        imagenBtn.setDescription("Imagenes y fotos");
        imagenBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (inspectionsTaskTrackGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    InspectionTaskImageWindow inspectionTaskImageWindow
                            = new InspectionTaskImageWindow(
                                    String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                                    String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CODIGO_TAREA_PROPERTY).getValue()),
                                    String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), INSTRUCCION_PROPERTY).getValue()),
                                    false
                            );
                    UI.getCurrent().addWindow(inspectionTaskImageWindow);
                    inspectionTaskImageWindow.center();
                }
            }
        });

        Button notesBtn = new Button("Seguimiento");
        notesBtn.setIcon(FontAwesome.BARS);
        notesBtn.setWidth(110, Sizeable.UNITS_PIXELS);
        notesBtn.setDescription("Notas de seguimiento u observaciones");
        notesBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (inspectionsTaskTrackGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    SeguimientoHandler seguimientoHandler = new SeguimientoHandler();
                    seguimientoHandler.fillTrackTable(
                            String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                            String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CODIGO_TAREA_PROPERTY).getValue()) + " "
                            + String.valueOf(taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), INSTRUCCION_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(seguimientoHandler);
                }
            }
        });

        Button archivarBtn = new Button("Archivar");
        archivarBtn.setIcon(FontAwesome.ARCHIVE);
        archivarBtn.setWidth(110, Sizeable.UNITS_PIXELS);
        archivarBtn.setDescription("Archivar tarea");
        archivarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        archivarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (inspectionsTaskTrackGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    queryString = "UPDATE  visita_inspeccion_tarea SET ";
                    queryString += "Archivado = 1";
                    queryString += " WHERE IdVisitaInspeccionTarea = " + taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), ID_PROPERTY).getValue();

                    System.out.println("Tarea archivada.." + queryString);

                    try {
                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);

                        Notification.show("Tarea arvhivada exitosamente!!!", Notification.Type.WARNING_MESSAGE);
                        taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), ARCHIVADO_PROPERTY).setValue("SI");

                    } catch (Exception ex) {
                        Logger.getLogger(InspectionsTaskTrackView.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("Error al intentar archivar tarea : " + ex.getMessage());
                        Notification.show("Error al intentar archivar tarea..!", Notification.Type.ERROR_MESSAGE);
                    }

                }
            }
        });

        Button exportPlanillaBtn = new Button("Exportar plantilla a Excel");
        exportPlanillaBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportPlanillaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        exportPlanillaBtn.setDescription("Exportar a Excel");
        exportPlanillaBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (taskContainer.size() == 0) {
                Notification notif = new Notification("No hay planilla.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
            else {
                exportToExcel();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(new MarginInfo(true, false, false, false));

        buttonsLayout.addComponent(budgetBtn);
        buttonsLayout.addComponent(changeOrderBtn);
        buttonsLayout.addComponent(imagenBtn);
        buttonsLayout.addComponent(notesBtn);
        buttonsLayout.addComponent(archivarBtn);
        buttonsLayout.addComponent(exportPlanillaBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void fillInspectionsTaskGrid() {

        if (taskContainer == null) {
            return;
        }
        taskContainer.removeAllContainerFilters();

        taskContainer.removeAllItems();
        notasContainer.removeAllItems();

        footer.getCell(INSTRUCCION_PROPERTY).setText("0 TAREAS");

        queryString = "SELECT VisI.IdVisitaInspeccion, VisI.CodigoVisita, VisI.FechaYHoraInicio FechaVisita,";
        queryString += " Tare.*, Usr.Nombre NombreAutorizadoPor, Cl.Nombre, Cc.Lote ";
        queryString += " FROM visita_inspeccion_tarea Tare ";
        queryString += " INNER JOIN visita_inspeccion VisI ON VisI.IdVisitaInspeccion = Tare.IdVisitaInspeccion";
        queryString += " LEFT  JOIN usuario Usr ON Usr.IdUsuario = Tare.AutorizadoPor";
        queryString += " LEFT  JOIN proveedor_empresa Cl ON Cl.IdProveedor = VisI.IdCliente";
//        queryString += " LEFT  JOIN proveedor Cl ON Cl.IdProveedor = VisI.IdCliente";
        queryString += " LEFT  JOIN centro_costo Cc ON Cc.CodigoCentroCosto = VisI.IdCentroCosto";
        queryString += " WHERE VisI.IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND Tare.EsTarea = 'SI'";
        queryString += " AND Tare.RechazadoFecha IS NULL";
        queryString += " AND VisI.FechaYHoraInicio >= '2022-01-01 00:00:00'";
        //"Sin Orden de cambio", "Con Orden de cambio", "Todas"
        if(String.valueOf(ordenCambioOg.getValue()).equals("Sin Orden de cambio")) {
            queryString += " AND Tare.IdVisitaInspeccionTarea NOT IN (SELECT IdVisitaInspeccionTarea FROM visita_inspeccion_tarea_oc)";
        }
        else if(String.valueOf(ordenCambioOg.getValue()).equals("Con Orden de cambio")) {
            queryString += " AND Tare.IdVisitaInspeccionTarea IN (SELECT IdVisitaInspeccionTarea FROM visita_inspeccion_tarea_oc)";
        }
        if(archivadoChbx.getValue()) {
            queryString += " AND Tare.Archivado = 1";
        } else {
            queryString += " AND Tare.Archivado = 0";
        }
        queryString += " AND VisI.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND Cc.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND Cl.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {

                    String montoPresupuesto = "0.00";

                    queryString = "SELECT SUM(Total) TotalPresupuesto ";
                    queryString += " FROM visita_inspeccion_tarea_presupuesto ";
                    queryString += " WHERE IdVisitaInspeccionTarea = " + rsRecords.getString("IdVisitaInspeccionTarea");

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) { //  encontrado
                        montoPresupuesto = numberFormat.format(rsRecords1.getDouble("TotalPresupuesto"));
                    }

                    Object itemId = taskContainer.addItem();

                    taskContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdVisitaInspeccionTarea"));
                    taskContainer.getContainerProperty(itemId, CODIGO_TAREA_PROPERTY).setValue(rsRecords.getString("CodigoTarea"));
                    taskContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaVisita")));
                    taskContainer.getContainerProperty(itemId, LOTE_PROPERTY).setValue(rsRecords.getString("Lote"));
                    taskContainer.getContainerProperty(itemId, IDCC_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));
                    taskContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    taskContainer.getContainerProperty(itemId, INSTRUCCION_PROPERTY).setValue(rsRecords.getString("Instruccion"));
                    taskContainer.getContainerProperty(itemId, RESPONSABLE_PROPERTY).setValue(rsRecords.getString("Responsable"));
                    taskContainer.getContainerProperty(itemId, EJECUTOR_PROPERTY).setValue(rsRecords.getString("Ejecutor"));
                    taskContainer.getContainerProperty(itemId, GARANTIA_PROPERTY).setValue(rsRecords.getString("Garantia"));
                    taskContainer.getContainerProperty(itemId, PRESUPUESTO_PROPERTY).setValue(rsRecords.getString("Presupuesto"));
                    taskContainer.getContainerProperty(itemId, AUTORIZADO_TIPO_PROPERTY).setValue(rsRecords.getString("AutorizadoTipo"));

                    taskContainer.getContainerProperty(itemId, MONTO_PRESUPUESTO_PROPERTY).setValue(montoPresupuesto);
                    taskContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    taskContainer.getContainerProperty(itemId, ARCHIVADO_PROPERTY).setValue(rsRecords.getString("Archivado").equals("0") ? "NO" : "SI");

                    if (rsRecords.getObject("FechaUltimoEstatus") == null) {
                        taskContainer.getContainerProperty(itemId, ULTIMO_ESTATUS_PROPERTY).setValue("");
                    } else {
                        taskContainer.getContainerProperty(itemId, ULTIMO_ESTATUS_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaUltimoEstatus")));
                    }
//                    taskContainer.getContainerProperty(itemId, CODIGOOC_PROPERTY).setValue(rsRecords.getString("CodigoOC"));

                } while (rsRecords.next());

                footer.getCell(INSTRUCCION_PROPERTY).setText(taskContainer.size() + " TAREAS");

            }
        } catch (Exception ex) {
            Logger.getLogger(InspectionsTaskTrackView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de TAREAS por visita de inspección : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de TAREAS por visitas de inspección..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void llenarNotasGrid() {

        notasContainer.removeAllItems();

        queryString = "SELECT Seg.*, Usr.Nombre UsuarioNombre";
        queryString += " FROM  visita_inspeccion_tarea_seguimiento Seg";
        queryString += " INNER JOIN usuario Usr ON Usr.IdUsuario = Seg.IdUsuario";
        queryString += " WHERE Seg.IdVisitaInspeccionTarea =" + taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), ID_PROPERTY).getValue();
        queryString += " ORDER BY Seg.FechaYHora";

        notasGrid.setDescription("NOTAS O SEGUIMIENTO DE LA TAREA : " + taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CODIGO_TAREA_PROPERTY).getValue());

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    Object itemId = notasContainer.addItem();

                    notasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdSeguimiento"));
                    notasContainer.getContainerProperty(itemId, OBSERVACION_PROPERTY).setValue(rsRecords.getString("Observacion"));
                    notasContainer.getContainerProperty(itemId, FECHA_HORA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY_HHMM_2(rsRecords.getTimestamp("FechaYHora")));
                    notasContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecords.getString("UsuarioNombre"));

                } while (rsRecords.next());
            }

        } catch (Exception e) {
            System.out.println("Error al intentar llenar tabla Notas " + e);
            e.printStackTrace();
        }
    }

    public void fillOcGrid() {

        if(ocGrid == null) {
            return;
        }
        ocContainer.removeAllItems();
//        ocIdexGrid.select(null);

        ocGrid.setDescription("ORDENES DE CAMBIO DE LA TAREA : " + taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), CODIGO_TAREA_PROPERTY).getValue());

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String queryString =  "SELECT *, usuario.Nombre as NombreUsuario";
            queryString += " FROM  visita_inspeccion_tarea_oc";
            queryString += " INNER JOIN usuario ON usuario.IdUsuario = visita_inspeccion_tarea_oc.CreadoUsuario";
            queryString += " WHERE IdVisitaInspeccionTarea = " + taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), ID_PROPERTY).getValue();

            System.out.println(queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                double totalOC = 0.00;
                double totalOCDolares = 0.00;

                Object itemId = 0;

                do {
                    itemId = ocContainer.addItem();

                    ocContainer.getContainerProperty(itemId, IDOC_PROPERTY).setValue(rsRecords.getString("IdVisitaInspeccionTareaOC"));
                    ocContainer.getContainerProperty(itemId, CODIGOOC_PROPERTY).setValue(rsRecords.getString("CodigoOC"));
                    ocContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(rsRecords.getString("Fecha"));
                    ocContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecords.getString("NombreUsuario"));
                    ocContainer.getContainerProperty(itemId, TOTAL_OC_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Total")));
                    ocContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));

                    totalOC += rsRecords.getDouble("Total");
                    totalOCDolares += rsRecords.getDouble("TotalDolares");

                } while (rsRecords.next());

                taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), MONTO_OC_PROPERTY).setValue(numberFormat.format(totalOC));
                taskContainer.getContainerProperty(inspectionsTaskTrackGrid.getSelectedRow(), MONTO_DOLARES_OC_PROPERTY).setValue(numberFormat.format(totalOCDolares));

            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla OC.",  ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Seguimiento de tareas");
    }
}
