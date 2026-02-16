/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
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
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class VisitasView extends VerticalLayout implements View {

    Statement stQuery = null;
    ResultSet rsRecords = null;
    Statement stQuery1 = null;
    ResultSet rsRecords1 = null;
    PreparedStatement stPreparedQuery = null;

    MultiFileUpload singleUpload;
    Image logoImage;
    public File file;
    StreamResource logoStreamResource = null;
    String parametro1, parametro2;
    Long parametro3;
    long fileSize;

    // Tabbla Visitas y Reuniones
    static final String ID_PROPERTY = "Id";
    static final String CODIGO_VISITA_PROPERTY = "Visita";
    static final String FECHAVISITA_PROPERTY = "Fecha";
    static final String MEDIO_PROPERTY = "Medio";
    static final String MOTIVO_PROPERTY = "Motivo";
    static final String VISITAS_PROPERTY = "Visitas";
    static final String CLIENTE_PROPERTY = "Cliente";
    static final String REFERENCIA_PROPERTY = "Referencia";
    static final String ARCHIVO_PROPERTY = "Documento";

    // Tabla Participantes
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String DPI_PROPERTY = "DPI";
    static final String EMAIL_PROPERTY = "Email";

    // Tabla Agenda
    static final String ID_AGENDA_PROPERTY = "Id";
    static final String PUNTO_AGENDA_PROPERTY = "Punto de Agenda";
    static final String RESOLUCION_PROPERTY = "Resolucion";

    // Tabla Tareas
    static final String RUBRO_PROPERTY = "Rubro";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String RESPONSABLE_PROPERTY = "Responsable";
    static final String EJECUTOR_PROPERTY = "Ejecutor";
    static final String GARANTIA_PROPERTY = "Garantia";
    static final String ES_TAREA_PROPERTY = "EsTarea";
    static final String PRESUPUESTO_PROPERTY = "Presupuesto";
    static final String AUTORIZA_PROPERTY = "Autoriza";
    static final String DIAS_HABILES_PROPERTY = "Dias Habiles";
    static final String EQUIPO_DIBUJO_PROPERTY = "Eq.Dibujo";
    static final String VISIBLE_PROPERTY = "Visible";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String FECHA_ESTATUS_PROPERTY = "Fecha Estatus";
    static final String CODIGO_TAREA_PROPERTY = "CTarea";
    static final String FOTOS_PROPERTY = "Fotos";
    static final String ELIMINAR_PROPERTY = "Eliminar";
    
    // Compratido
    static final String CENTRO_COSTO_PROPERTY = "Centro costo"; //<-- Compartido VIsitas y Tareas
    static final String CORRELATIVO_PROPERTY = "#"; // <-- Compartida Participantes, Agenda y Tareas

    VerticalLayout mainLayout;
    TabSheet tabSheet;

    EnvironmentVars enviromentsVars;
    Utileria utileria = new Utileria();
    MarginInfo marginInfo;

    public IndexedContainer visitasContainer = new IndexedContainer();
    Grid visitasGrid;
    FooterRow footer;

    IndexedContainer agendaYResolucionesContainer = new IndexedContainer();
    IndexedContainer participantesContainer = new IndexedContainer();
    IndexedContainer tareasContainer = new IndexedContainer();
    IndexedContainer notasContainer = new IndexedContainer();

    Grid agendaYResolucionesGrid;
    Grid participantesGrid;
    Grid tareasGrid;
    Grid notasGrid;

    List<Integer> agendaList = new ArrayList<>();
    List<Integer> participanteList = new ArrayList<>();
    List<Object> tareaList = new ArrayList<>();

    TextField idVisitaInspeccionTxt;
    TextField codigoVisitaInspeccionTxt;
    TextField referenciaTxt;
    TextField lugarTxt;
    TextArea observacionesTxt;
    DateField fechaYHoraInicioDt;
    DateField fechaYHoraFinDt;
    ComboBox medioCbx;
    ComboBox motivoCbx;
    ComboBox visitasCbx;
    ComboBox clienteCbx;
    ComboBox centroCostoCbx;

    DateField inicioDt;
    DateField finDt;
    Button consultarBtn;

    Button newBtn;
    Button saveBtn;
    Button reportePdfBtn;
    Button reporteInternoPdfBtn;
    Button printPdfBudgetBtn;

    UI mainUI;

    public VisitasView() {
        this.mainUI = UI.getCurrent();

        enviromentsVars = new EnvironmentVars();

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);

        marginInfo = new MarginInfo(true, true, false, true);

        Label titleLbl = new Label("Visitas y reuniones del proyecto : " + ((SopdiUI)UI.getCurrent()).sessionInformation.getStrProjectName() + "<br>Empesa : " + ((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyName() + "</br>");
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");
        titleLbl.setContentMode(ContentMode.HTML);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        inicioDt = new DateField("DEL:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelAnio();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("10em");

        finDt = new DateField("AL:");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("10em");

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillVisitasTable();
            }
        });

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setMargin(new MarginInfo(false, true, false, true));

        filterLayout.addComponents(inicioDt);
        filterLayout.setComponentAlignment(inicioDt, Alignment.TOP_CENTER);
        filterLayout.addComponents(finDt);
        filterLayout.setComponentAlignment(finDt, Alignment.TOP_CENTER);
        filterLayout.addComponents(consultarBtn);
        filterLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);

        titleLayout.addComponents(filterLayout);
        titleLayout.setComponentAlignment(filterLayout, Alignment.TOP_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createVisitasGrid();
        createVisitasTabSheet();
        createButtons();

        fillVisitasTable();

    }

    private void createVisitasGrid() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(false);

        visitasContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        visitasContainer.addContainerProperty(CODIGO_VISITA_PROPERTY, String.class, null);
        visitasContainer.addContainerProperty(FECHAVISITA_PROPERTY, String.class, null);
        visitasContainer.addContainerProperty(MEDIO_PROPERTY, String.class, "");
        visitasContainer.addContainerProperty(MOTIVO_PROPERTY, String.class, "");
        visitasContainer.addContainerProperty(VISITAS_PROPERTY, String.class, "");
        visitasContainer.addContainerProperty(CLIENTE_PROPERTY, String.class, "");
        visitasContainer.addContainerProperty(CENTRO_COSTO_PROPERTY, String.class, "");
        visitasContainer.addContainerProperty(REFERENCIA_PROPERTY, String.class, "");
        visitasContainer.addContainerProperty(ARCHIVO_PROPERTY, String.class, "");

        visitasGrid = new Grid("Visitas y reuniones", visitasContainer);

        visitasGrid.setImmediate(true);
        visitasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        visitasGrid.setDescription("Seleccione un registro.");
        visitasGrid.setHeightMode(HeightMode.ROW);
        visitasGrid.setHeightByRows(5);
        visitasGrid.setWidth("100%");
        visitasGrid.setResponsive(true);
        visitasGrid.setEditorBuffered(false);

        reportLayout.addComponent(visitasGrid);
        reportLayout.setComponentAlignment(visitasGrid, Alignment.MIDDLE_CENTER);

        visitasGrid.getColumn(ID_PROPERTY).setMaximumWidth(90).setHidable(true).setHidden(true);
        visitasGrid.getColumn(CODIGO_VISITA_PROPERTY).setMaximumWidth(130);
        visitasGrid.getColumn(FECHAVISITA_PROPERTY).setMaximumWidth(120);
        visitasGrid.getColumn(CLIENTE_PROPERTY).setMaximumWidth(200);
        visitasGrid.getColumn(MEDIO_PROPERTY).setMaximumWidth(150);
        visitasGrid.getColumn(MOTIVO_PROPERTY).setMaximumWidth(200);
        visitasGrid.getColumn(VISITAS_PROPERTY).setMaximumWidth(150);

        visitasGrid.getColumn(CENTRO_COSTO_PROPERTY).setHidden(true);
        visitasGrid.getColumn(REFERENCIA_PROPERTY).setMaximumWidth(120);

        visitasGrid.getColumn(ARCHIVO_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            if (visitasContainer.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue().equals("Cargar archivo")) {
                String codigoVisita = String.valueOf(visitasContainer.getContainerProperty(e.getItemId(), CODIGO_VISITA_PROPERTY).getValue());
                visitasGrid.select(e.getItemId());

                CargarDocumentoVisita cargarArchivo
                        = new CargarDocumentoVisita(e.getItemId(), codigoVisita);
                UI.getCurrent().addWindow(cargarArchivo);
                cargarArchivo.center();

            } else {
                VerCambiarDocumento(e);
            }
        }));

        visitasGrid.addListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {
                visitasGrid.select(event.getItemId());
                if (visitasGrid.getSelectedRow() != null) {
                    fillVisitaData();
//                        InspectionForm newInspectionForm = new InspectionForm(
//                                String.valueOf(visitasGrid.getContainerDataSource().getItem(visitasGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()),
//                                String.valueOf(visitasGrid.getContainerDataSource().getItem(visitasGrid.getSelectedRow()).getItemProperty(CODIGO_VISITA_PROPERTY).getValue()));
//                        mainUI.addWindow(newInspectionForm);
//                        newInspectionForm.center();
//                        newInspectionForm.fillData();
//                        newInspectionForm.motivoCbx.focus();
                }
            }
        });

        HeaderRow filterRow = visitasGrid.appendHeaderRow();

        HeaderCell cellA = filterRow.getCell(CODIGO_VISITA_PROPERTY);

        TextField filterFieldA = new TextField();
        filterFieldA.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldA.setInputPrompt("Filtrar");
        filterFieldA.setColumns(8);

        filterFieldA.addTextChangeListener(change -> {
            visitasContainer.removeContainerFilters(CODIGO_VISITA_PROPERTY);

            if (!change.getText().isEmpty()) {
                visitasContainer.addContainerFilter(
                        new SimpleStringFilter(CODIGO_VISITA_PROPERTY,
                                change.getText(), true, false));
//                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(visitasContainer.size()) + " REGISTROS");
                visitasGrid.setCaption(String.valueOf(visitasContainer.size()) + " VISITAS");
            }
        });
        cellA.setComponent(filterFieldA);

        HeaderCell cell = filterRow.getCell(FECHAVISITA_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            visitasContainer.removeContainerFilters(FECHAVISITA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                visitasContainer.addContainerFilter(
                        new SimpleStringFilter(FECHAVISITA_PROPERTY,
                                change.getText(), true, true));
//                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(visitasContainer.size()) + " REGISTROS");
                visitasGrid.setCaption(String.valueOf(visitasContainer.size()) + " VISITAS");
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell1 = filterRow.getCell(CLIENTE_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(15);

        filterField1.addTextChangeListener(change -> {
            visitasContainer.removeContainerFilters(CLIENTE_PROPERTY);

            if (!change.getText().isEmpty()) {
                visitasContainer.addContainerFilter(
                        new SimpleStringFilter(CLIENTE_PROPERTY,
                                change.getText(), true, false));
//                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(visitasContainer.size()) + " REGISTROS");
                visitasGrid.setCaption(String.valueOf(visitasContainer.size()) + " VISITAS");
            }
        });
        cell1.setComponent(filterField1);

        HeaderCell cell2 = filterRow.getCell(REFERENCIA_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(10);

        filterField2.addTextChangeListener(change -> {
            visitasContainer.removeContainerFilters(REFERENCIA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                visitasContainer.addContainerFilter(
                        new SimpleStringFilter(REFERENCIA_PROPERTY,
                                change.getText(), true, false));
//                footer.getCell(REFERENCIA_PROPERTY).setText(String.valueOf(visitasContainer.size()) + " REGISTROS");
                visitasGrid.setCaption(String.valueOf(visitasContainer.size()) + " VISITAS");
            }
        });
        cell2.setComponent(filterField2);

        HeaderCell cell3 = filterRow.getCell(MOTIVO_PROPERTY);

        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(15);

        filterField3.addTextChangeListener(change -> {
            visitasContainer.removeContainerFilters(MOTIVO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                visitasContainer.addContainerFilter(
                        new SimpleStringFilter(MOTIVO_PROPERTY,
                                change.getText(), true, false));
//                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(visitasContainer.size()) + " REGISTROS");
                visitasGrid.setCaption(String.valueOf(visitasContainer.size()) + " VISITAS");
            }
        });
        cell3.setComponent(filterField3);

        HeaderCell cell4 = filterRow.getCell(MEDIO_PROPERTY);

        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(15);

        filterField4.addTextChangeListener(change -> {
            visitasContainer.removeContainerFilters(MEDIO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                visitasContainer.addContainerFilter(
                        new SimpleStringFilter(MEDIO_PROPERTY,
                                change.getText(), true, false));
//                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(visitasContainer.size()) + " REGISTROS");
                visitasGrid.setCaption(String.valueOf(visitasContainer.size()) + " VISITAS");
            }
        });
        cell4.setComponent(filterField4);

        HeaderCell cell5 = filterRow.getCell(VISITAS_PROPERTY);

        TextField filterField5 = new TextField();
        filterField5.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField5.setInputPrompt("Filtrar");
        filterField5.setColumns(15);

        filterField5.addTextChangeListener(change -> {
            visitasContainer.removeContainerFilters(VISITAS_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                visitasContainer.addContainerFilter(
                        new SimpleStringFilter(VISITAS_PROPERTY,
                                change.getText(), true, false));
//                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(visitasContainer.size()) + " REGISTROS");
                visitasGrid.setCaption(String.valueOf(visitasContainer.size()) + " VISITAS");

            }
        });
        cell5.setComponent(filterField5);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createVisitasTabSheet() {
        tabSheet = new TabSheet();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        tabSheet.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);
        tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {

                // Find the tabsheet
                TabSheet tabsheet = event.getTabSheet();

                if (!tabSheet.getSelectedTab().getClass().getName().equals("com.vaadin.ui.Label")) {
                    Layout tab = (Layout) tabsheet.getSelectedTab();

                    // Get the tab caption from the tab object
                    String caption = tabsheet.getTab(tab).getCaption();

//                    System.out.println("\nTab Caption = " + caption);

                    if (caption.contains("Historial")) {
                        //
                    }

                    if (caption.toUpperCase().contains("ANTICIPO") || caption.toUpperCase().contains("MONEDA")) {
                        //
                    }
                }
            }
        });

        addTabDatos();
        createTabAgendaYResoluciones();
        crateTabParticipantes();
        addTabTareas();

        addComponent(tabSheet);
    }

    private void addTabDatos() {

        HorizontalLayout layoutDatosVisita = new HorizontalLayout();
        layoutDatosVisita.setSpacing(true);
        layoutDatosVisita.setMargin(true);
        layoutDatosVisita.setWidth(("100%"));

        FormLayout formLayout = new FormLayout();
        formLayout.setMargin(true);
        formLayout.setWidth("50%");
        formLayout.setMargin(false);

        FormLayout formLayout2 = new FormLayout();
        formLayout2.setMargin(true);
        formLayout2.setWidth("50%");
        formLayout2.setMargin(false);

        layoutDatosVisita.addComponents(formLayout, formLayout2);
        layoutDatosVisita.setComponentAlignment(formLayout, Alignment.TOP_LEFT);
        layoutDatosVisita.setComponentAlignment(formLayout2, Alignment.TOP_LEFT);

        idVisitaInspeccionTxt = new TextField("Id visita : ");
        idVisitaInspeccionTxt.setValue("0");
        idVisitaInspeccionTxt.setReadOnly(true);
        idVisitaInspeccionTxt.setVisible(false);

        codigoVisitaInspeccionTxt = new TextField("Código : ");
        codigoVisitaInspeccionTxt.setWidth("5em");
        codigoVisitaInspeccionTxt.setReadOnly(true);

        fechaYHoraInicioDt = new DateField("Fecha y hora inicio :");
        fechaYHoraInicioDt.setValue(new java.util.Date());
        fechaYHoraInicioDt.setResolution(Resolution.MINUTE);
        fechaYHoraInicioDt.setDateFormat("dd-MM-yyyy hh:mm");
        fechaYHoraInicioDt.setWidth("20em");

        fechaYHoraFinDt = new DateField("Fecha y hora fin:");
        fechaYHoraFinDt.setValue(new java.util.Date());
        fechaYHoraFinDt.setResolution(Resolution.MINUTE);
        fechaYHoraFinDt.setDateFormat("dd-MM-yyyy hh:mm");
        fechaYHoraFinDt.setWidth("20em");

        medioCbx = new ComboBox("Medio :");
        medioCbx.addContainerProperty("Codigo", String.class, "");
        medioCbx.setInvalidAllowed(false);
        medioCbx.setNewItemsAllowed(false);
        medioCbx.setWidth("20em");
        medioCbx.setPageLength(15);
        medioCbx.addItem("<<ELIJA>>").getItemProperty("Codigo").setValue("0");
        medioCbx.addItem("Reunión").getItemProperty("Codigo").setValue("1");
        medioCbx.addItem("Visita").getItemProperty("Codigo").setValue("2");
        medioCbx.addItem("Correo").getItemProperty("Codigo").setValue("3");
        medioCbx.addItem("Llamada").getItemProperty("Codigo").setValue("4");

        motivoCbx = new ComboBox("Motivo :");
        motivoCbx.addContainerProperty("Codigo", String.class, "");
        motivoCbx.setInvalidAllowed(false);
        motivoCbx.setNewItemsAllowed(false);
        motivoCbx.setWidth("20em");
        motivoCbx.setPageLength(15);
        motivoCbx.addItem("<<ELIJA>>").getItemProperty("Codigo").setValue("0");
        motivoCbx.addItem("Diaria").getItemProperty("Codigo").setValue("1");
        motivoCbx.addItem("Residente").getItemProperty("Codigo").setValue("2");
        motivoCbx.addItem("Reunión de Obra").getItemProperty("Codigo").setValue("3");
        motivoCbx.addItem("Reunión Administrativa").getItemProperty("Codigo").setValue("4");
        motivoCbx.addItem("Reunión Ordinaria").getItemProperty("Codigo").setValue("5");
        motivoCbx.addItem("Comité Técnico").getItemProperty("Codigo").setValue("6");
        motivoCbx.addItem("Operativa").getItemProperty("Codigo").setValue("7");
        motivoCbx.addItem("Consejo Administración").getItemProperty("Codigo").setValue("8");
        motivoCbx.addItem("Cliente").getItemProperty("Codigo").setValue("9");
        motivoCbx.addItem("Cierre Centro Costo").getItemProperty("Codigo").setValue("10");

        visitasCbx = new ComboBox("Visitas :");
        visitasCbx.addContainerProperty("Codigo", String.class, "");
        visitasCbx.setInvalidAllowed(false);
        visitasCbx.setNewItemsAllowed(false);
        visitasCbx.setWidth("20em");
        visitasCbx.setPageLength(15);
        visitasCbx.addItem("<<ELIJA>>").getItemProperty("Codigo").setValue("0");
        visitasCbx.addItem("Equipo de Trabajo").getItemProperty("Codigo").setValue("1");
        visitasCbx.addItem("Gerente").getItemProperty("Codigo").setValue("2");
        visitasCbx.addItem("Director").getItemProperty("Codigo").setValue("3");
        visitasCbx.addItem("Accionista").getItemProperty("Codigo").setValue("4");
        visitasCbx.addItem("Especialista").getItemProperty("Codigo").setValue("5");
        visitasCbx.addItem("Supervisor Contraparte").getItemProperty("Codigo").setValue("6");
        visitasCbx.addItem("Cliente").getItemProperty("Codigo").setValue("7");
        visitasCbx.addItem("Gobierno").getItemProperty("Codigo").setValue("8");
        visitasCbx.addItem("Colaborades").getItemProperty("Codigo").setValue("9");

        centroCostoCbx = new ComboBox("Centro costo :");
        centroCostoCbx.setWidth("20em");
        centroCostoCbx.addContainerProperty("idCentroCosto", String.class, null);
        fillComboCentroCosto();

        clienteCbx = new ComboBox("Cliente :");
        clienteCbx.setWidth("25em");
        //clienteCbx.setRequired(true);
        //clienteCbx.setRequiredError("POR FAVOR ELIJA AL CLIENTE.");
        clienteCbx.setInvalidAllowed(false);
        clienteCbx.setNewItemsAllowed(false);
        clienteCbx.setFilteringMode(FilteringMode.CONTAINS);
        clienteCbx.addContainerProperty("idCentroCosto", String.class, null);
        fillComboCliente();

        clienteCbx.addValueChangeListener(event -> {
            if (centroCostoCbx != null) {
                centroCostoCbx.setEnabled(true);
            }
            if (clienteCbx.getValue() != null) {
                centroCostoCbx.select("0");
//System.out.println("clienteCbx=" + clienteCbx.getValue() + " " + clienteCbx.getContainerProperty(clienteCbx.getValue(), "CodigoCentroCosto").getValue());
                for(Object itemObject :  centroCostoCbx.getItemIds()) {
                    if (centroCostoCbx.getItem(itemObject) != null) {
                        if (String.valueOf(itemObject).equals(String.valueOf(clienteCbx.getContainerProperty(clienteCbx.getValue(), "idCentroCosto").getValue()))) {
                            centroCostoCbx.select(itemObject);
                        }
                    } else {
                        //centroCostoCbx.select(0);
                    }
                }//endfor
                participantesContainer.removeAllItems();
                participanteList.clear();
                Object itemId = participantesContainer.addItem();

                participantesContainer.getContainerProperty(itemId, CORRELATIVO_PROPERTY).setValue(1);
                participantesContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(clienteCbx.getItemCaption(clienteCbx.getValue()));
                participantesContainer.getContainerProperty(itemId, DPI_PROPERTY).setValue("");
                participantesContainer.getContainerProperty(itemId, EMAIL_PROPERTY).setValue("");
                participanteList.add(1);
                addGridParticipantes(false);

            }
        });

        referenciaTxt = new TextField("Referencia : ");
        referenciaTxt.setWidth("25em");

        lugarTxt = new TextField("Lugar : ");
        lugarTxt.setWidth("25em");

        observacionesTxt = new TextArea("Obsevaciónes : ");
        observacionesTxt.setWidth("25em");
        observacionesTxt.setHeight("5em");

        formLayout.addComponent(idVisitaInspeccionTxt);
        formLayout.addComponent(fechaYHoraInicioDt);
        formLayout.addComponent(fechaYHoraFinDt);
        formLayout.addComponent(medioCbx);
        formLayout.addComponent(motivoCbx);
        formLayout.addComponent(visitasCbx);
        formLayout2.addComponent(clienteCbx);
        formLayout2.addComponent(centroCostoCbx);
        formLayout2.addComponent(referenciaTxt);
        formLayout2.addComponent(lugarTxt);
        formLayout2.addComponent(observacionesTxt);

        TabSheet.Tab newTab = tabSheet.addTab(layoutDatosVisita, "Datos");
        newTab.setIcon(FontAwesome.CLIPBOARD);
        newTab.setId("1");
    }

    private void createTabAgendaYResoluciones() {

        agendaYResolucionesContainer.addContainerProperty(CORRELATIVO_PROPERTY, Integer.class, null);
        agendaYResolucionesContainer.addContainerProperty(ID_AGENDA_PROPERTY, Integer.class, 0);
        agendaYResolucionesContainer.addContainerProperty(PUNTO_AGENDA_PROPERTY, String.class, "");
        agendaYResolucionesContainer.addContainerProperty(RESOLUCION_PROPERTY, String.class, "");

        agendaYResolucionesGrid = new Grid(agendaYResolucionesContainer);
        agendaYResolucionesGrid.setHeightMode(HeightMode.ROW);
        agendaYResolucionesGrid.setHeightByRows(7);
        agendaYResolucionesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        agendaYResolucionesGrid.setWidth("100%");
        agendaYResolucionesGrid.setDescription("DOBLE Click aqui para editar.");

        agendaYResolucionesGrid.getColumn(ID_AGENDA_PROPERTY).setExpandRatio(1);
        agendaYResolucionesGrid.getColumn(ID_AGENDA_PROPERTY).setMaximumWidth(50);
        agendaYResolucionesGrid.getColumn(CORRELATIVO_PROPERTY).setMaximumWidth(50);
        agendaYResolucionesGrid.getColumn(CORRELATIVO_PROPERTY).setExpandRatio(1);
        agendaYResolucionesGrid.getColumn(PUNTO_AGENDA_PROPERTY).setExpandRatio(3);
        agendaYResolucionesGrid.getColumn(RESOLUCION_PROPERTY).setExpandRatio(6);

        agendaYResolucionesGrid.getColumn(ID_AGENDA_PROPERTY).setHidable(true);
        agendaYResolucionesGrid.getColumn(ID_AGENDA_PROPERTY).setHidden(true);

        agendaYResolucionesGrid.addItemClickListener((event) -> {
            if (event != null) {
                if (event.isDoubleClick()) {
                    if (event.getItemId() == null) {
                        return;
                    }
                    if(agendaList.isEmpty()) validarYGuardar(false);
                    InspectionTextWindow inspectionTextWindow
                            = new InspectionTextWindow(
                                idVisitaInspeccionTxt.getValue(),
                                codigoVisitaInspeccionTxt.getValue(),
                                Integer.valueOf(String.valueOf(event.getItem().getItemProperty(CORRELATIVO_PROPERTY).getValue())),
                                event.getItemId(),
                                Integer.valueOf(String.valueOf(event.getItem().getItemProperty(ID_AGENDA_PROPERTY).getValue()))
                            );
                    inspectionTextWindow.puntoAgendaTxt.setValue(String.valueOf(event.getItem().getItemProperty(PUNTO_AGENDA_PROPERTY).getValue()).trim());
                    inspectionTextWindow.resolucionTxt.setValue(String.valueOf(event.getItem().getItemProperty(RESOLUCION_PROPERTY).getValue()).trim());
                    mainUI.addWindow(inspectionTextWindow);
                    inspectionTextWindow.center();
                    inspectionTextWindow.puntoAgendaTxt.focus();
                }
            }

        });

        addGridAgenda(true);

        HorizontalLayout resolucionesLayout = new HorizontalLayout();
        resolucionesLayout.setWidth("100%");
        resolucionesLayout.addComponent(agendaYResolucionesGrid);
        resolucionesLayout.setComponentAlignment(agendaYResolucionesGrid, Alignment.MIDDLE_CENTER);

        TabSheet.Tab newTab = tabSheet.addTab(resolucionesLayout, "Agenda");
        newTab.setIcon(FontAwesome.CLIPBOARD);
        newTab.setId("2");
    }

    public void addGridAgenda(boolean reset) {
        if (reset){
            agendaYResolucionesContainer.removeAllItems();
            agendaList.clear();
        }
        Object itemId = agendaYResolucionesContainer.addItem();
        agendaYResolucionesContainer.getContainerProperty(itemId, CORRELATIVO_PROPERTY).setValue(agendaList.size()+1);
        agendaYResolucionesContainer.getContainerProperty(itemId, PUNTO_AGENDA_PROPERTY).setValue("");
        agendaYResolucionesContainer.getContainerProperty(itemId, RESOLUCION_PROPERTY).setValue("");
    }

    private void crateTabParticipantes() {

        participantesContainer.addContainerProperty(CORRELATIVO_PROPERTY, Integer.class, null);
        participantesContainer.addContainerProperty(NOMBRE_PROPERTY, String.class, "");
        participantesContainer.addContainerProperty(DPI_PROPERTY, String.class, "");
        participantesContainer.addContainerProperty(EMAIL_PROPERTY, String.class, "");

        participantesGrid = new Grid(participantesContainer);
        participantesGrid.setEditorBuffered(false);
        participantesGrid.setHeightMode(HeightMode.ROW);
        participantesGrid.setHeightByRows(7);
        participantesGrid.setEditorEnabled(true);
        participantesGrid.setSelectionMode(Grid.SelectionMode.NONE);
        participantesGrid.setWidth("100%");
        participantesGrid.setDescription("DOBLE Click aqui para editar lista de participantes.");        
        participantesGrid.addItemClickListener((event) -> {
            
            participantesGrid.getColumn(NOMBRE_PROPERTY).setEditorField(getComboDatosParticipante());
            if (event != null) {
                if(participanteList.isEmpty()) validarYGuardar(false);
                participantesGrid.editItem(event.getItemId());
                participantesGrid.getColumn(NOMBRE_PROPERTY).setEditorField(getComboDatosParticipante());
                participantesGrid.cancelEditor();
                participantesGrid.getColumn(NOMBRE_PROPERTY).setEditorField(getComboDatosParticipante());
            }

        });

        participantesGrid.getColumn(CORRELATIVO_PROPERTY).setMaximumWidth(50);
        participantesGrid.getColumn(CORRELATIVO_PROPERTY).setExpandRatio(1);
        participantesGrid.getColumn(NOMBRE_PROPERTY).setExpandRatio(5);
        participantesGrid.getColumn(DPI_PROPERTY).setExpandRatio(3);
        participantesGrid.getColumn(EMAIL_PROPERTY).setExpandRatio(3);

        addGridParticipantes(true);

        HorizontalLayout participantesLayout = new HorizontalLayout();
        participantesLayout.setWidth("100%");
//        participantesLayout.setMargin(new MarginInfo(true, true, false, true));
        participantesLayout.setSpacing(true);
//        participantesLayout.addStyleName("rcorners2");
        participantesLayout.addComponent(participantesGrid);
        participantesLayout.setComponentAlignment(participantesGrid, Alignment.MIDDLE_CENTER);

        TabSheet.Tab newTab = tabSheet.addTab(participantesLayout, "Participantes");
        newTab.setIcon(FontAwesome.USERS);
        newTab.setId("3");
    }

    private void addGridParticipantes(boolean reset) {
        if (reset) {
            participantesContainer.removeAllItems();
            participanteList.clear();
        }
        Object itemId = participantesContainer.addItem();
        participantesContainer.getContainerProperty(itemId, CORRELATIVO_PROPERTY).setValue(participanteList.size()+1);
        participantesContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue("");
        participantesContainer.getContainerProperty(itemId, DPI_PROPERTY).setValue("");
        participantesContainer.getContainerProperty(itemId, EMAIL_PROPERTY).setValue("");
    }

    private void addTabTareas() {

        tareasContainer.addContainerProperty(ID_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(CORRELATIVO_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(RUBRO_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(RESPONSABLE_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(EJECUTOR_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(GARANTIA_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(ES_TAREA_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(PRESUPUESTO_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(CENTRO_COSTO_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(AUTORIZA_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(DIAS_HABILES_PROPERTY, Integer.class, "");
        tareasContainer.addContainerProperty(EQUIPO_DIBUJO_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(VISIBLE_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(FECHA_ESTATUS_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(CODIGO_TAREA_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(FOTOS_PROPERTY, String.class, "");
        tareasContainer.addContainerProperty(ELIMINAR_PROPERTY, String.class, "ELIMINAR");

        tareasGrid = new Grid(tareasContainer);
        tareasGrid.setHeightMode(HeightMode.ROW);
        tareasGrid.setHeightByRows(7);
        tareasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        tareasGrid.setWidth("100%");
        tareasGrid.getColumn(CODIGO_TAREA_PROPERTY).setHidden(true);
        tareasGrid.setDescription("DOBLE Click aqui para editar.");

        tareasGrid.getColumn(ID_PROPERTY).setHidden(true);
        tareasGrid.getColumn(CORRELATIVO_PROPERTY).setMaximumWidth(50);
        tareasGrid.getColumn(RUBRO_PROPERTY).setMaximumWidth(250);
        tareasGrid.getColumn(DESCRIPCION_PROPERTY).setMaximumWidth(250);
        tareasGrid.getColumn(RESPONSABLE_PROPERTY).setMaximumWidth(150);
        tareasGrid.getColumn(EJECUTOR_PROPERTY).setMaximumWidth(150);
        tareasGrid.getColumn(ES_TAREA_PROPERTY).setMaximumWidth(75);
        tareasGrid.getColumn(GARANTIA_PROPERTY).setMaximumWidth(75);
        tareasGrid.getColumn(PRESUPUESTO_PROPERTY).setMaximumWidth(75);
        tareasGrid.getColumn(CENTRO_COSTO_PROPERTY).setMaximumWidth(90);
        tareasGrid.getColumn(AUTORIZA_PROPERTY).setMaximumWidth(150);

        tareasGrid.addItemClickListener((event) -> {
            if (event != null) {
                if (event.isDoubleClick()) {
                    if (event.getItemId() != null) {
                        if(tareaList.isEmpty()) validarYGuardar(false);
                        int puntosAgenda = 0;

                        puntosAgenda = agendaYResolucionesContainer.getItemIds().stream().filter((objectItem) -> (!String.valueOf(agendaYResolucionesContainer.getContainerProperty(objectItem, "Punto de Agenda").getValue()).trim().isEmpty())).map((_item) -> 1).reduce(puntosAgenda, Integer::sum);

                        if (puntosAgenda == 0) {
                            Notification.show("Por favor, escriba los puntos de agenda.", Notification.Type.WARNING_MESSAGE);
                            tabSheet.setSelectedTab(1);
                            return;
                        }
                        InspectionTaskForm inspectionTaskForm = new InspectionTaskForm(
                                idVisitaInspeccionTxt.getValue(),
                                String.valueOf(tareasContainer.getContainerProperty(event.getItemId(), CODIGO_TAREA_PROPERTY).getValue()),
                                codigoVisitaInspeccionTxt.getValue(),
                                String.valueOf(centroCostoCbx.getValue()),
                                String.valueOf(centroCostoCbx.getValue()).equals("No Aplica")
                        );
                        inspectionTaskForm.center();
                        mainUI.addWindow(inspectionTaskForm);
                        inspectionTaskForm.fillData();
                    }
                }
            }

        });

        tareasGrid.getColumn(PRESUPUESTO_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            if (tareasContainer.getContainerProperty(e.getItemId(), PRESUPUESTO_PROPERTY).getValue().equals("SI")) {

                tareasGrid.select(e.getItemId());

                InspectionTaskBudgetWindow inspectionTaskBudgetWindow = new InspectionTaskBudgetWindow(
                        String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), CODIGO_TAREA_PROPERTY).getValue()),
                        String.valueOf(tabSheet.getTab(0).getCaption() + tareasContainer.getContainerProperty(e.getItemId(), CORRELATIVO_PROPERTY).getValue()),
                        String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue()),
                        String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), AUTORIZA_PROPERTY).getValue())
                );
                UI.getCurrent().addWindow(inspectionTaskBudgetWindow);
                inspectionTaskBudgetWindow.center();
            }
        }));

        tareasGrid.getColumn(ESTATUS_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            if (String.valueOf(visitasContainer.getContainerProperty(visitasGrid.getSelectedRow(), ARCHIVO_PROPERTY).getValue()).equals("Cargar archivo")) {
                Notification.show("No se permite cambiar de estatus si no ha cargado el documento firmado a la tarea.",
                        Notification.Type.ERROR_MESSAGE);
                return;
            }

            tareasGrid.select(e.getItemId());

            validarYGuardar(false);
            int puntosAgenda = 0;

            puntosAgenda = agendaYResolucionesContainer.getItemIds().stream().filter((objectItem) -> (!String.valueOf(agendaYResolucionesContainer.getContainerProperty(objectItem, "Punto de Agenda").getValue()).trim().isEmpty())).map((_item) -> 1).reduce(puntosAgenda, Integer::sum);

            if (puntosAgenda == 0) {
                Notification.show("Por favor, escriba los puntos de agenda.", Notification.Type.WARNING_MESSAGE);
                tabSheet.setSelectedTab(1);
                return;
            }
            InspectionTaskForm inspectionTaskForm = new InspectionTaskForm(
                    idVisitaInspeccionTxt.getValue(),
                    String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), CODIGO_TAREA_PROPERTY).getValue()),
                    codigoVisitaInspeccionTxt.getValue(),
                    String.valueOf(centroCostoCbx.getValue()),
                    String.valueOf(centroCostoCbx.getValue()).equals("No Aplica")
            );
            inspectionTaskForm.center();
            mainUI.addWindow(inspectionTaskForm);
            inspectionTaskForm.fillData();
        }));

        tareasGrid.getColumn(FOTOS_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            tareasGrid.select(e.getItemId());
            InspectionTaskImageWindow inspectionTaskImageWindow
                    = new InspectionTaskImageWindow(
                            String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), CODIGO_TAREA_PROPERTY).getValue()),
                            tabSheet.getTab(0).getCaption() + String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), CODIGO_TAREA_PROPERTY).getValue()),
                            String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue()),
                            true
                    );
            UI.getCurrent().addWindow(inspectionTaskImageWindow);
            inspectionTaskImageWindow.center();
        }));

        tareasGrid.getColumn(ELIMINAR_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            if(String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), ID_PROPERTY).getValue()).equals("")) {
                return;
            }
            tareasGrid.select(e.getItemId());
            boolean tienePresupuesto = false;

            try {
                rsRecords = stQuery.executeQuery("SELECT IdVisitaInspeccionTareaPresupuesto FROM visita_inspeccion_tarea_presupuesto WHERE IdVisitaInspeccionTarea = " + String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), ID_PROPERTY).getValue()));

                tienePresupuesto = rsRecords.next();

                if (tienePresupuesto) {
                    Notification.show("No se permite eliminar la tarea si ya tiene presupuesto asignado.",
                            Notification.Type.ERROR_MESSAGE);
                }
                else { // eliminar tarea
                    stQuery.executeUpdate("DELETE FROM visita_inspeccion_tarea WHERE IdVisitaInspeccionTarea = " + String.valueOf(tareasContainer.getContainerProperty(e.getItemId(), ID_PROPERTY).getValue()));
                    tareasContainer.removeItem(e.getItemId());
                    Notification.show("Tarea eliminada.", Notification.Type.TRAY_NOTIFICATION);
                }
            } catch (SQLException ex) {
                Logger.getLogger(VisitasView.class.getName()).log(Level.SEVERE, null, ex);
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                //                    throw new RuntimeException(ex);
            }
        }));

        addGridTareas(true);

        HorizontalLayout tareasLayout = new HorizontalLayout();
        tareasLayout.setWidth("100%");
//        tareasLayout.setMargin(new MarginInfo(true, true, false, true));
        tareasLayout.setSpacing(true);
//        resolucionesLayout.addStyleName("rcorners2");
        tareasLayout.addComponent(tareasGrid);
        tareasLayout.setComponentAlignment(tareasGrid, Alignment.MIDDLE_CENTER);

        TabSheet.Tab newTab = tabSheet.addTab(tareasLayout, "Tareas");
        newTab.setIcon(FontAwesome.CHECK_CIRCLE_O);
        newTab.setId("4");
    }

    private void addGridTareas(boolean reset) {
        if(reset){
            tareasContainer.removeAllItems();
            tareaList.clear();
        }

        Object item = tareasContainer.addItem();

        tareasContainer.getContainerProperty(item, ID_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, CORRELATIVO_PROPERTY).setValue(String.format("%02d", tareaList.size()+1));
        tareasContainer.getContainerProperty(item, RUBRO_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, DESCRIPCION_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, RESPONSABLE_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, EJECUTOR_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, GARANTIA_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, ES_TAREA_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, PRESUPUESTO_PROPERTY).setValue("NO");
        tareasContainer.getContainerProperty(item, AUTORIZA_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, DIAS_HABILES_PROPERTY).setValue(0);
        tareasContainer.getContainerProperty(item, EQUIPO_DIBUJO_PROPERTY).setValue("NO");
        tareasContainer.getContainerProperty(item, ESTATUS_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, FECHA_ESTATUS_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, CODIGO_TAREA_PROPERTY).setValue("");
        tareasContainer.getContainerProperty(item, FOTOS_PROPERTY).setValue("Fotos");
        tareasContainer.getContainerProperty(item, ELIMINAR_PROPERTY).setValue("Eliminar");
    }

    private void createButtons() {

        newBtn = new Button("Nueva visita o reunión");
        newBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        newBtn.setWidth(190, Sizeable.UNITS_PIXELS);
//        newBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newBtn.setDescription("Registrar nueva visita o reunión");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                addGridAgenda(true);
                addGridParticipantes(true);
                addGridTareas(true);

                tabSheet.getTab(0).setCaption("***NUEVA***");
                idVisitaInspeccionTxt.setReadOnly(false);
                idVisitaInspeccionTxt.setValue("0");
                idVisitaInspeccionTxt.setReadOnly(false);
                codigoVisitaInspeccionTxt.setReadOnly(false);
                codigoVisitaInspeccionTxt.setValue("");
                codigoVisitaInspeccionTxt.setReadOnly(true);
                fechaYHoraInicioDt.setValue(new java.util.Date());
                fechaYHoraFinDt.setValue(new java.util.Date());
                medioCbx.select("<<ELIJA>>");
                motivoCbx.select("<<ELIJA>>");
                visitasCbx.select("<<ELIJA>>");
                clienteCbx.select("<<ELIJA>>");
                clienteCbx.select("0");

                centroCostoCbx.select("0");
                referenciaTxt.setValue("");
                lugarTxt.setValue("Proyecto");
                observacionesTxt.setValue("");
                
                visitasGrid.select(null);                

                medioCbx.focus();

            }
        });

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        saveBtn.setDescription("Actualizar datos de la visita o reunión");
        saveBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                validarYGuardar(true);
            }
        });

        reportePdfBtn = new Button("Visualizar reporte");
        reportePdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        reportePdfBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (visitasGrid.getSelectedRow() != null) {

                    ReporteVisitasReunionesPDF reporteVisitasReunionesPDF
                            = new ReporteVisitasReunionesPDF(
                                    idVisitaInspeccionTxt.getValue(),
                                    tabSheet.getTab(0).getCaption(),
                                    Utileria.getFechaYYYYMMDD_1(fechaYHoraFinDt.getValue()),
                                    Utileria.getFechaYYYYMMDD_1(fechaYHoraFinDt.getValue()),
                                    String.valueOf(motivoCbx.getValue()),
                                    String.valueOf(medioCbx.getValue()),
                                    String.valueOf(centroCostoCbx.getValue()),
                                    String.valueOf(clienteCbx.getValue()),
                                    String.valueOf(referenciaTxt.getValue()),
                                    String.valueOf(lugarTxt.getValue()),
                                    agendaYResolucionesContainer, participantesContainer);
                    UI.getCurrent().addWindow(reporteVisitasReunionesPDF);

                } else {
                    Notification.show("Por favor seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                }
            }

        });

        reporteInternoPdfBtn = new Button("Visualizar reporte interno");
        reporteInternoPdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        reporteInternoPdfBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (visitasGrid.getSelectedRow() != null) {

                    ReporteVisitasReunionesInternoPDF reporteVisitasReunionesInternoPDF
                            = new ReporteVisitasReunionesInternoPDF(
                            idVisitaInspeccionTxt.getValue(),
                            tabSheet.getTab(0).getCaption(),
                            Utileria.getFechaYYYYMMDD_1(fechaYHoraFinDt.getValue()),
                            Utileria.getFechaYYYYMMDD_1(fechaYHoraFinDt.getValue()),
                            String.valueOf(motivoCbx.getValue()),
                            String.valueOf(medioCbx.getValue()),
                            String.valueOf(centroCostoCbx.getValue()),
                            String.valueOf(clienteCbx.getValue()),
                            String.valueOf(referenciaTxt.getValue()),
                            String.valueOf(lugarTxt.getValue()),
                            agendaYResolucionesContainer, participantesContainer);
                    UI.getCurrent().addWindow(reporteVisitasReunionesInternoPDF);

                } else {
                    Notification.show("Por favor seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                }
            }

        });

        printPdfBudgetBtn = new Button("Presupuesto PDF");
        printPdfBudgetBtn.setIcon(FontAwesome.FILE_PDF_O);
//        printPdfBudgetBtn.setWidth(140, Sizeable.UNITS_PIXELS);
//        printPdfBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        printPdfBudgetBtn.setDescription("Imprimir reporte PDF de presupuesto");
        printPdfBudgetBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                InspectionBudgetReportPDF inspectionBudgetReportPDF
                        = new InspectionBudgetReportPDF(idVisitaInspeccionTxt.getValue());
                mainUI.addWindow(inspectionBudgetReportPDF);
                inspectionBudgetReportPDF.center();
                //inspectionBudgetReportPDF.
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(newBtn, saveBtn, reportePdfBtn, reporteInternoPdfBtn, printPdfBudgetBtn);
        buttonsLayout.setComponentAlignment(newBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(reportePdfBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(reporteInternoPdfBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout.setComponentAlignment(printPdfBudgetBtn, Alignment.BOTTOM_RIGHT);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void fillVisitasTable() {

        if (visitasContainer == null) {
            return;
        }

        idVisitaInspeccionTxt.setReadOnly(false);
        idVisitaInspeccionTxt.setValue("");
        fechaYHoraInicioDt.setValue(new java.util.Date());
        fechaYHoraFinDt.setValue(new java.util.Date());
        referenciaTxt.setValue("");
        lugarTxt.setValue("");
        observacionesTxt.setValue("");

        visitasContainer.removeAllItems();
        addGridAgenda(true);
        addGridParticipantes(true);
        addGridTareas(true);

//        footer.getCell(CLIENTE_PROPERTY).setText("0 REGISTROS");
        visitasGrid.setCaption(" 0 Visitas y reuniones");

        if (inicioDt.getValue().after(finDt.getValue())) {
            Notification.show("La fecha inicial no puede ser mayor a la fecha final, revise!", Notification.Type.WARNING_MESSAGE);
            inicioDt.focus();
            return;
        }

        String queryString;

        queryString = "SELECT Vis.*, Cli.Nombre ClienteNombre ";
        queryString += " FROM visita_inspeccion Vis ";
        queryString += " LEFT JOIN proveedor Cli ON Cli.IdProveedor = Vis.IdCliente";
        queryString += " WHERE Vis.IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND Vis.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND FechaYHoraInicio >= '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + " 00:00:00'";
        queryString += " AND FechaYHoraInicio <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + " 23:59:59'";
        queryString += " AND Vis.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY Vis.FechaYHoraInicio DESC";

System.out.println("\n\n"+queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

                do {
                    Object itemId = visitasContainer.addItem();

                    visitasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdVisitaInspeccion"));
                    visitasContainer.getContainerProperty(itemId, CODIGO_VISITA_PROPERTY).setValue(rsRecords.getString("CodigoVisita"));
                    visitasContainer.getContainerProperty(itemId, FECHAVISITA_PROPERTY).setValue(df.format(rsRecords.getDate("FechaYHoraInicio")));
                    visitasContainer.getContainerProperty(itemId, MEDIO_PROPERTY).setValue(rsRecords.getString("Medio"));
                    visitasContainer.getContainerProperty(itemId, MOTIVO_PROPERTY).setValue(rsRecords.getString("Motivo"));
                    visitasContainer.getContainerProperty(itemId, VISITAS_PROPERTY).setValue(rsRecords.getString("Visitas"));
                    if (rsRecords.getObject("ClienteNombre") != null) {
                        visitasContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(rsRecords.getString("ClienteNombre"));
                    } else {
                        visitasContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue("");
                    }
                    visitasContainer.getContainerProperty(itemId, CENTRO_COSTO_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));
                    visitasContainer.getContainerProperty(itemId, REFERENCIA_PROPERTY).setValue(rsRecords.getString("Referencia"));

                    if (rsRecords.getString("ArchivoNombre").trim().isEmpty()) {
                        visitasContainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue("Cargar archivo");
                    } else {
                        visitasContainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue(rsRecords.getString("ArchivoNombre"));
                    }

                } while (rsRecords.next());

                //rsRecords.last();
                visitasGrid.setCaption(visitasContainer.size() + " Visitas y reunions");
                //visitasGrid.select(visitasContainer.firstItemId());
                // fillVisitaData();
            }
        } catch (Exception ex) {
            Logger.getLogger(VisitasView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de visita/reunión : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de visita/reunión..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub
        Page.getCurrent().setTitle("Sopdi - Visitas/Reuniones");
    }

    private void validarYGuardar(boolean desplegarMsg) {

        if (String.valueOf(motivoCbx.getValue()).equals("Visita de cliente")
                || String.valueOf(motivoCbx.getValue()).equals("Reunión con residente")) {
            if (String.valueOf(clienteCbx.getValue()).equals("0")) {
                Notification.show("Por favor, elija al cliente.", Notification.Type.WARNING_MESSAGE);
                tabSheet.setSelectedTab(tabSheet.getTab(0));
                clienteCbx.focus();
                return;
            }
        }

        if (String.valueOf(medioCbx.getValue()).equals("<<ELIJA>>")) {
            Notification.show("Por favor, elija UNA OPCION DEL COMBO MEDIO.", Notification.Type.WARNING_MESSAGE);
            tabSheet.setSelectedTab(tabSheet.getTab(0));
            medioCbx.focus();
            return;

        }
        if (String.valueOf(motivoCbx.getValue()).equals("<<ELIJA>>")) {
            Notification.show("Por favor, elija UNA OPCION DEL COMBO MOTIVO.", Notification.Type.WARNING_MESSAGE);
            tabSheet.setSelectedTab(tabSheet.getTab(0));
            motivoCbx.focus();
            return;

        }

        String queryString, codigoVisita = "";

        try {

            if (idVisitaInspeccionTxt.getValue().equals("0")) {

                codigoVisita = String.format("%02d", Integer.valueOf(((SopdiUI) mainUI).sessionInformation.getStrProjectId()));
                codigoVisita += String.format("%02d", Integer.valueOf(String.valueOf(medioCbx.getItem(medioCbx.getValue()).getItemProperty("Codigo").getValue())));
                codigoVisita += String.format("%02d", Integer.valueOf(String.valueOf(motivoCbx.getItem(motivoCbx.getValue()).getItemProperty("Codigo").getValue())));
                SimpleDateFormat df = new SimpleDateFormat("ddMMyy");
                codigoVisita += df.format(fechaYHoraInicioDt.getValue());

                queryString = "SELECT CodigoVisita";
                queryString += " FROM  visita_inspeccion ";
                queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
                queryString += " AND   CodigoVisita Like '" + codigoVisita + "%'";
                queryString += " ORDER BY CodigoVisita DESC";
                queryString += " LIMIT 1";

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado
                    codigoVisita = rsRecords.getString("CodigoVisita");

                    codigoVisita = codigoVisita.substring(0, 12) + String.format("%02d", Integer.valueOf(codigoVisita.substring(12, 14)) + 1);
                } else {
                    codigoVisita += "01";
                }

                codigoVisitaInspeccionTxt.setReadOnly(false);
                codigoVisitaInspeccionTxt.setValue(codigoVisita);

                tabSheet.getTab(0).setCaption(codigoVisita);

                queryString = "Insert Into visita_inspeccion ";
                queryString += "(IdProyecto, IdEmpresa, CodigoVisita, FechaYHoraInicio, FechaYHoraFin, ";
                queryString += " Medio, Motivo, Visitas, IdCliente, IdCentroCosto, Referencia, ";
                queryString += " CreadoUsuario, CreadoFechaYHora, Lugar, Observaciones) ";
                queryString += " Values (";
                queryString += "  " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId();
                queryString += ","  + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                queryString += ",'" + codigoVisita + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDDHHMMSS(fechaYHoraInicioDt.getValue()) + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDDHHMMSS(fechaYHoraFinDt.getValue()) + "'";
                queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(motivoCbx.getValue()) + "'";

                if (visitasCbx.getValue().equals("<<ELIJA>>") || visitasCbx.getValue().equals("")
                        || visitasCbx.getValue() == null) {
                    queryString += ",'Gerente'";
                } else {
                    queryString += ",'" + String.valueOf(visitasCbx.getValue()) + "'";
                }

                if (clienteCbx.getValue() == null || clienteCbx.getValue().equals("")) {
                    queryString += ", 0";
                } else {
                    queryString += "," + String.valueOf(clienteCbx.getValue());
                }

                if (centroCostoCbx.getValue().equals("<<ELIJA>>") || centroCostoCbx.getValue().equals("")
                        || centroCostoCbx.getValue() == null) {
                    queryString += ", '0'";
                } else {
                    queryString += ",'" + String.valueOf(centroCostoCbx.getValue()) + "'";
                }

                queryString += ",'" + referenciaTxt.getValue() + "'";
                queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId(); //creado usuario
                queryString += ",current_timestamp"; // creado fechayhora
                queryString += ",'" + lugarTxt.getValue() + "'";
                queryString += ",'" + observacionesTxt.getValue() + "'";
                queryString += ")";
            } else {
                if(String.valueOf(idVisitaInspeccionTxt.getValue()).trim().isEmpty()) {
                    return;
                }
                queryString = "Update visita_inspeccion Set";
                queryString += " IdCliente = " + String.valueOf(clienteCbx.getValue());
                queryString += ",FechaYHoraInicio = '" + Utileria.getFechaYYYYMMDDHHMMSS(fechaYHoraInicioDt.getValue()) + "'";
                queryString += ",FechaYHoraFin = '" + Utileria.getFechaYYYYMMDDHHMMSS(fechaYHoraFinDt.getValue()) + "'";
                queryString += ",Medio = '" + String.valueOf(medioCbx.getValue()) + "'";
                queryString += ",Motivo = '" + String.valueOf(motivoCbx.getValue()) + "'";
                queryString += ",Visitas = '" + String.valueOf(visitasCbx.getValue()) + "'";
                queryString += ",IdCentroCosto = '" + String.valueOf(centroCostoCbx.getValue()) + "'";
                queryString += ",Referencia = '" + referenciaTxt.getValue() + "'";
                queryString += ",Lugar = '" + lugarTxt.getValue() + "'";
                queryString += ",Observaciones = '" + observacionesTxt.getValue() + "'";
                queryString += " Where IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();
            }

            System.out.println("\nQUERY=" + queryString + "\n");

            if (idVisitaInspeccionTxt.getValue().equals("0")) { //nuevo
                stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                stPreparedQuery.executeUpdate();
                rsRecords = stPreparedQuery.getGeneratedKeys();

                rsRecords.next();

                idVisitaInspeccionTxt.setReadOnly(false);
                idVisitaInspeccionTxt.setValue(rsRecords.getString(1));
                idVisitaInspeccionTxt.setReadOnly(true);

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm");

                Object itemId = visitasContainer.addItem();

                visitasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(idVisitaInspeccionTxt.getValue());
                visitasContainer.getContainerProperty(itemId, CODIGO_VISITA_PROPERTY).setValue(codigoVisita);
                visitasContainer.getContainerProperty(itemId, FECHAVISITA_PROPERTY).setValue(df.format(fechaYHoraInicioDt.getValue()));
                visitasContainer.getContainerProperty(itemId, MEDIO_PROPERTY).setValue(medioCbx.getValue());
                visitasContainer.getContainerProperty(itemId, MOTIVO_PROPERTY).setValue(motivoCbx.getValue());
                if (visitasCbx.getValue().equals("<<ELIJA>>")) {
                    visitasContainer.getContainerProperty(itemId, VISITAS_PROPERTY).setValue("Gerente");
                } else {
                    visitasContainer.getContainerProperty(itemId, VISITAS_PROPERTY).setValue(visitasCbx.getValue());
                }

                visitasContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(clienteCbx.getValue());
                visitasContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(clienteCbx.getValue());
                visitasContainer.getContainerProperty(itemId, CENTRO_COSTO_PROPERTY).setValue(centroCostoCbx.getValue());
                visitasContainer.getContainerProperty(itemId, REFERENCIA_PROPERTY).setValue(referenciaTxt.getValue());
                visitasContainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue("Cargar archivo");

//                footer.getCell(CLIENTE_PROPERTY).setText(String.valueOf(visitasContainer.size()) + " REGISTROS");
                visitasGrid.setCaption(String.valueOf(visitasContainer.size()) + " Visitas y reuniones");

                if (desplegarMsg) {
                    Notification.show("Registro insertado!!!", Notification.Type.WARNING_MESSAGE);
                }

            } else {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                if (desplegarMsg) {
                    Notification.show("Registro modificado!!!", Notification.Type.WARNING_MESSAGE);
                }
            }

            guardarAgenda();
            guardarParticipantes();

//            ((VisitasView) (mainUI.getNavigator().getCurrentView())).fillVisitasTable();
        } catch (Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR GUARDAR LOS CAMBIOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    private void guardarAgenda(){
        String queryString;

        if(agendaYResolucionesContainer.getItemIds().size() > 1) {
            try {
                queryString = "DELETE FROM visita_inspeccion_agenda WHERE IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);
            } catch (Exception ex1) {
                Notification.show("ERROR EN AGENDA", Notification.Type.ERROR_MESSAGE);
                System.out.println("ERROR AL INTENTAR GUARDAR LOS CAMBIOS : " + ex1.getMessage());
                ex1.printStackTrace();
            }

            queryString = "INSERT INTO visita_inspeccion_agenda (";
            queryString += "IdVisitaInspeccion, IdEmpresa, CodigoVisita, NumeroAgenda, PuntoAgenda, Resolucion, CreadoUsuario, CreadoFechayHora) Values ";

            int posicion = 1;
            for (Object item : agendaYResolucionesContainer.getItemIds()) {
                if(!agendaYResolucionesContainer.getContainerProperty(item, PUNTO_AGENDA_PROPERTY).getValue().equals("")) {
                    queryString += "(" + idVisitaInspeccionTxt.getValue() + ", ";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + ", ";
                    queryString += "'" + codigoVisitaInspeccionTxt.getValue() + "', ";
                    queryString += posicion + ", ";
                    queryString += "'" + agendaYResolucionesContainer.getContainerProperty(item, PUNTO_AGENDA_PROPERTY).getValue() + "', ";
                    queryString += "'" + agendaYResolucionesContainer.getContainerProperty(item, RESOLUCION_PROPERTY).getValue() + "', ";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrUserId() + ", ";
                    queryString += "current_timestamp), ";

                    posicion++;
                }
            }

            try {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString.substring(0, (queryString.length() - 2)));
            } catch (Exception ex1) {
                Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
                System.out.println("ERROR AL INTENTAR GUARDAR LOS CAMBIOS : " + ex1.getMessage());
                ex1.printStackTrace();
            }
        }
    }

    private void guardarParticipantes(){
        String queryString;

        if(participantesContainer.getItemIds().size() > 1) {
            try {
                queryString = "DELETE FROM visita_inspeccion_participante WHERE IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);
            } catch (Exception ex1) {
                Notification.show("ERROR EN PARTICIPANTE", Notification.Type.ERROR_MESSAGE);
                System.out.println("ERROR AL INTENTAR GUARDAR LOS CAMBIOS : " + ex1.getMessage());
                ex1.printStackTrace();
            }

            queryString = "INSERT INTO visita_inspeccion_participante (";
            queryString += "IdVisitaInspeccion, CodigoVisita, IdEmpresa, Nombre, DPI, Email, Rol, CreadoUsuario, CreadoFechaYHora) Values ";

            for (Object item : participantesContainer.getItemIds()) {
                if(!participantesContainer.getContainerProperty(item, NOMBRE_PROPERTY).getValue().equals("")) {
                    queryString += "(" + idVisitaInspeccionTxt.getValue() + ", ";
                    queryString += "'" + codigoVisitaInspeccionTxt.getValue() + "', ";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + ", ";
                    queryString += "'" + participantesContainer.getContainerProperty(item, NOMBRE_PROPERTY).getValue() + "', ";
                    queryString += "'" + participantesContainer.getContainerProperty(item, DPI_PROPERTY).getValue() + "', ";
                    queryString += "'" + participantesContainer.getContainerProperty(item, EMAIL_PROPERTY).getValue() + "', ";
                    queryString += "(SELECT Producto FROM proveedor WHERE Nombre = '" + participantesContainer.getContainerProperty(item, NOMBRE_PROPERTY).getValue() + "'), ";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrUserId() + ", ";
                    queryString += "current_timestamp), ";
                }
            }

            try {

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString.substring(0, (queryString.length() - 2)));
            } catch (Exception ex1) {
                Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
                System.out.println("ERROR AL INTENTAR GUARDAR LOS CAMBIOS : " + ex1.getMessage());
                ex1.printStackTrace();
            }
        }
    }

    private void deleteInspeccion() {

        String queryString;

        queryString = "Delete ";
        queryString += " From  visita_inspeccion_tarea_seguimiento ";
        queryString += " Where IdVisitaInspeccionTarea In (Select A.IdVisitaInspeccionTarea From visita_inspeccion_tarea A Where A.IdVisitaInspeccion = " + String.valueOf(visitasGrid.getContainerDataSource().getItem(visitasGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()) + ")";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            queryString = "Delete ";
            queryString += " From  visita_inspeccion_tarea_imagen ";
            queryString += " Where IdVisitaInspeccionTarea In (Select A.IdVisitaInspeccionTarea From visita_inspeccion_tarea A Where A.IdVisitaInspeccion = " + String.valueOf(visitasGrid.getContainerDataSource().getItem(visitasGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()) + ")";

            stQuery.executeUpdate(queryString);

            queryString = "Delete ";
            queryString += " From  visita_inspeccion_tarea_presupuesto ";
            queryString += " Where IdVisitaInspeccionTarea In (Select A.IdVisitaInspeccionTarea From visita_inspeccion_tarea A Where A.IdVisitaInspeccion = " + String.valueOf(visitasGrid.getContainerDataSource().getItem(visitasGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()) + ")";

            stQuery.executeUpdate(queryString);

            queryString = "Delete ";
            queryString += " From  visita_inspeccion_tarea ";
            queryString += " Where IdVisitaInspeccion = " + String.valueOf(visitasGrid.getContainerDataSource().getItem(visitasGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue());

            stQuery.executeUpdate(queryString);

            queryString = "Delete ";
            queryString += " From  visita_inspeccion ";
            queryString += " Where IdVisitaInspeccion = " + String.valueOf(visitasGrid.getContainerDataSource().getItem(visitasGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue());

            stQuery.executeUpdate(queryString);

            Notification.show("Operación exitosa!", Notification.Type.TRAY_NOTIFICATION);

            fillVisitasTable();
        } catch (Exception ex) {
            Logger.getLogger(VisitasView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al ELIINAR registros : " + ex.getMessage());
            Notification.show("Error al ELIMINAR registros...!", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void VerCambiarDocumento(ClickableRenderer.RendererClickEvent e) {
        Object selectedObject = e.getItemId();
        String idVisita = String.valueOf(visitasContainer.getContainerProperty(e.getItemId(), ID_PROPERTY).getValue());
        String codigoVisita = String.valueOf(visitasContainer.getContainerProperty(e.getItemId(), CODIGO_VISITA_PROPERTY).getValue());
        String archivoNombre = String.valueOf(visitasContainer.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue());

        visitasGrid.select(e.getItemId());

        try {

            String filePath = enviromentsVars.getDtePath();

            final byte docBytes[] = Files.readAllBytes(new File(filePath + archivoNombre).toPath());
            final String fileName = filePath + archivoNombre;

            if (docBytes == null) {
                Notification.show("Documento no disponible para visualizar!");

                return;
            }

            Window window = new Window();
            window.setResizable(true);
            window.setWidth("95%");
            window.setHeight("95%");

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
            documentStreamResource.setMIMEType("application/pdf");
            documentStreamResource.setFilename(archivoNombre);
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

//            System.out.println("Tipo de Archivo : " + rsRecords.getString("ArchivoTipo"));
//            System.out.println("Archivo Nombre :" + rsRecords.getString("ArchivoNombre"));
//            System.out.println("Tamanio :" + rsRecords.getString("ArchivoPeso"));
            window.setWidth("98%");
            window.setHeight("98%");

            VerticalLayout pdfLayout = new VerticalLayout();
            pdfLayout.setSizeFull();
            pdfLayout.setSpacing(true);

            BrowserFrame browserFrame = new BrowserFrame();
            browserFrame.setSizeFull();
            browserFrame.setSource(documentStreamResource);

            pdfLayout.addComponent(browserFrame);

            UploadFinishedHandler handler;
            handler = new UploadFinishedHandler() {
                @Override
                public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                    File targetFile;

                    try {
                        if (!mimeType.contains("pdf")) {
                            return;
                        }
                        System.out.println("\nfileName=" + fileName);
                        System.out.println("length=" + stream.available());
                        System.out.println("mimeType=" + mimeType);

                        fileSize = stream.available();
                        byte[] buffer = new byte[stream.available()];
                        stream.read(buffer);

//                                String filePath = VaadinService.getCurrent()
//                                        .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "/";
                        String filePath = enviromentsVars.getDtePath();

                        new File(filePath).mkdirs();

                        fileName = codigoVisita + fileName.substring(fileName.length() - 4, fileName.length());

                        new File(filePath + filePath).mkdirs();

                        targetFile = new File(filePath + fileName);
                        OutputStream outStream = new FileOutputStream(targetFile);
                        outStream.write(buffer);
                        outStream.close();

                        stream.close();

                        System.out.println("\ntargetFile = " + filePath + fileName);

                        logoStreamResource = null;

                        if (buffer != null) {
                            logoStreamResource = new StreamResource(
                                    new StreamResource.StreamSource() {
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(buffer);
                                }
                            }, String.valueOf(System.currentTimeMillis())
                            );
                        }

                        recibirParametrosImagen(targetFile.getName(), mimeType, fileSize);

                        file = targetFile;

                        Notification.show("Archivo cargado con exito!!..", Notification.Type.TRAY_NOTIFICATION);

                        guardarDocumentoVisita(selectedObject, idVisita, fileName);
                        window.close();
                    } catch (java.io.IOException fIoEx) {
                        fIoEx.printStackTrace();
                        Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                        return;
                    }
                }
            };

            UploadStateWindow window2 = new UploadStateWindow();

            singleUpload = new MultiFileUpload(handler, window2, false);
            singleUpload.setIcon(FontAwesome.UPLOAD);
            singleUpload.setImmediate(true);
            singleUpload.getSmartUpload().setUploadButtonCaptions("Cambiar archivo", "");

            JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.PDF')");

            pdfLayout.addComponent(singleUpload);

            window.setContent(pdfLayout);

            pdfLayout.setExpandRatio(browserFrame, 2);

            archivoNombre = "";
            UI.getCurrent().addWindow(window);
            window.center();

        } catch (Exception allEx) {
            Notification.show("Error al intentar mostrar el archivo.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }

    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        parametro1 = fileName;
        parametro2 = mimeType;
        parametro3 = peso;
    }

    public void guardarDocumentoVisita(Object selectedObject, String idVisita, String fileName) {
        try {

            String queryString = " Update visita_inspeccion set  ";
            queryString += "  ArchivoNombre ='" + fileName + "'";
            queryString += ", ArchivoTipo ='" + parametro2 + "'";
            queryString += ", ArchivoPeso = " + parametro3;
            queryString += " where IdVisita = " + idVisita;

            PreparedStatement stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.executeUpdate();

            visitasContainer.getContainerProperty(selectedObject, ARCHIVO_PROPERTY).setValue(fileName);

        } catch (Exception ex) {
            System.out.println("Error al intentar insertar Imagen" + ex);
        }
    }

    private void fillComboCliente() {

        if (clienteCbx == null) {
            return;
        }

        String queryString = "SELECT * ";
        queryString += " FROM proveedor ";
        queryString += " WHERE N0 IN (1, 2, 3) "; // <- Camnbio de condicional | escliente = 1 | o cotra condicional
        queryString += " AND Inhabilitado = 0";
        queryString += " ORDER BY Nombre";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            clienteCbx.removeAllItems();
            clienteCbx.addItem("0");
            clienteCbx.setItemCaption(0, "<<ELIJA>>");
            clienteCbx.select("0");

            while (rsRecords.next()) { //  encontrado 

//System.out.println("Cliente=" + rsRecords.getString("Nombre") + " CentroCosto=" + rsRecords.getString("IdProveedor").substring(1, 5));
                clienteCbx.addItem(rsRecords.getString("IdProveedor"));
                clienteCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "idCentroCosto").setValue(rsRecords.getString("IdProveedor").substring(2, 6));
                clienteCbx.setItemCaption(rsRecords.getString("IdProveedor"), "(" + rsRecords.getString("IdProveedor").substring(2, 6) + ") " + rsRecords.getString("Nombre"));
//System.out.println("PropertyCentroCosto=" + clienteCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "CodigoCentroCosto").getValue());
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CLIENTES", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO CLIENTES : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void fillComboCentroCosto() {

        if (centroCostoCbx == null) {
            return;
        }

        String queryString = "Select * ";
        queryString += " From centro_costo ";
        queryString += " Where IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " And Inhabilitado = 0";

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            centroCostoCbx.removeAllItems();
            centroCostoCbx.clear();
            centroCostoCbx.addItem("0");
            centroCostoCbx.setItemCaption("0", "No Aplica");
            // centroCostoCbx.select(0);

            while (rsRecords1.next()) { //  encontrado
//System.out.println("CodigoCentroCosto:"+rsRecords1.getString("CodigoCentroCosto"));
                centroCostoCbx.addItem(rsRecords1.getString("CodigoCentroCosto"));
                centroCostoCbx.getContainerProperty(rsRecords1.getString("CodigoCentroCosto"), "idCentroCosto").setValue(rsRecords1.getString("IdCentroCosto"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private Field<?> getComboDatosParticipante() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setNewItemsAllowed(true);
        comboBox.clear();
        comboBox.addContainerProperty(DPI_PROPERTY, String.class, "");
        comboBox.addContainerProperty(EMAIL_PROPERTY, String.class, "");

        comboBox.addItem("");
        
        System.out.println("Lo que trae el combo motivo es : " + motivoCbx.getValue());
        
        String queryString = "Select * ";
        queryString += "From proveedor ";
                
        switch (String.valueOf(motivoCbx.getValue())) {
            case "Diaria":
            case "Comité Técnico":
            case "Reunión de Obra":
            case "Reunión Administrativa":
            case "Reunión Ordinaria":
            case "Operativa":
                queryString += "WHERE (EsComite = 1 ";
                break;
            case "Residente":
                queryString += "WHERE (EsComite = 1 OR EsCliente = 1 ";
                break;
            case "Consejo Administración":
                queryString += "WHERE (EsComite = 1 OR Grupo = 'Comites' ";
                break;
            case "Cliente":
                queryString += "Where (EsComite = 1 ";
                break;
            default:
                queryString += " ";
                break;
        }
        queryString += "OR IdProveedor = " + clienteCbx.getValue() + ") ";
        queryString += "Order By Nombre";

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                comboBox.addItem(rsRecords1.getString("Nombre"));
                comboBox.getItem(rsRecords1.getString("Nombre")).getItemProperty(DPI_PROPERTY).setValue(rsRecords1.getString("DPI"));
                comboBox.getItem(rsRecords1.getString("Nombre")).getItemProperty(EMAIL_PROPERTY).setValue(rsRecords1.getString("Email"));
            }

        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PARTICIPANTES DE REUNIONES", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE PROVEEDORES (PARTICIPANTES DE REUNIONES) : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        comboBox.addValueChangeListener(event -> {
            if (event != null) {
                if (event.getProperty().getValue() != null) {

                    if (participantesGrid != null) {

                        if (comboBox.getContainerDataSource().getContainerProperty(event.getProperty().getValue(), DPI_PROPERTY) != null) {
                            if (String.valueOf(comboBox.getContainerProperty(event.getProperty().getValue(), DPI_PROPERTY).getValue()) != null) {
                                participantesContainer.getContainerProperty(participantesGrid.getEditedItemId(), DPI_PROPERTY).setValue(String.valueOf(comboBox.getContainerProperty(event.getProperty().getValue(), DPI_PROPERTY).getValue()));
                                participantesContainer.getContainerProperty(participantesGrid.getEditedItemId(), EMAIL_PROPERTY).setValue(String.valueOf(comboBox.getContainerProperty(event.getProperty().getValue(), EMAIL_PROPERTY).getValue()));
                                if (!participanteList.contains((Integer) participantesContainer.getContainerProperty(participantesGrid.getEditedItemId(), CORRELATIVO_PROPERTY).getValue()) && !comboBox.getValue().equals(""))
                                {
                                    participanteList.add((Integer) participantesContainer.getContainerProperty(participantesGrid.getEditedItemId(), CORRELATIVO_PROPERTY).getValue());
                                    addGridParticipantes(false);
                                }
                            }
                        }

                    }

                }
            }
        });

        return comboBox;
    }

    public void fillVisitaData() {

        idVisitaInspeccionTxt.setReadOnly(false);
        idVisitaInspeccionTxt.setValue(String.valueOf(visitasGrid.getContainerDataSource().getItem(visitasGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()));
        idVisitaInspeccionTxt.setReadOnly(true);

        String queryString = "Select * ";
        queryString += " From  visita_inspeccion ";
        queryString += " Where IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                codigoVisitaInspeccionTxt.setReadOnly(false);
                codigoVisitaInspeccionTxt.setValue(rsRecords.getString("CodigoVisita"));
                codigoVisitaInspeccionTxt.setReadOnly(true);
                fechaYHoraInicioDt.setValue(rsRecords.getDate("FechaYHoraInicio"));
                fechaYHoraFinDt.setValue(rsRecords.getDate("FechaYHorafin"));
                medioCbx.select(rsRecords.getString("Medio"));
                motivoCbx.select(rsRecords.getString("Motivo"));

                if (rsRecords.getObject("Visitas") == null || rsRecords.getObject("Visitas").equals("0")
                        || rsRecords.getObject("Visitas").equals("")) {
                    visitasCbx.clear();
                } else {
                    visitasCbx.select(rsRecords.getString("Visitas"));
                }
                if (rsRecords.getObject("IdCliente") == null || rsRecords.getObject("IdCliente").equals("0")
                        || rsRecords.getObject("IdCliente").equals("")) {
                    clienteCbx.select("0");
                } else {
                    clienteCbx.select(rsRecords.getString("IdCliente"));
                }
                if (rsRecords.getObject("IdCentroCosto") == null || rsRecords.getObject("IdCentroCosto").equals("0")
                        || rsRecords.getObject("IdCentroCosto").equals("")) {
                    centroCostoCbx.select("0");
                } else {
                    centroCostoCbx.select(rsRecords.getString("IdCentroCosto"));
                }

                referenciaTxt.setValue(rsRecords.getString("Referencia"));
                lugarTxt.setValue(rsRecords.getString("Lugar"));
                observacionesTxt.setValue(rsRecords.getString("Observaciones"));

                tabSheet.getTab(0).setCaption(rsRecords.getString("CodigoVisita"));

                fillAgenda();
                fillParticipante();

                fillInspectionTaskGrid();

                motivoCbx.focus();

            } else {
                Notification.show("Error, no se encotró registro!", Notification.Type.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros : " + ex.getMessage());
            Notification.show("Error al intentar leer registros..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void fillAgenda(){
        agendaList.clear();

        String queryString = "SELECT via.* ";
        queryString += "FROM  visita_inspeccion vi ";
        queryString += "INNER JOIN visita_inspeccion_agenda via ON vi.IdVisitaInspeccion = via.IdVisitaInspeccion ";
        queryString += "WHERE vi.IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue() + " ";
        queryString += "ORDER BY via.NumeroAgenda, via.IdAgenda";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            agendaYResolucionesContainer.removeAllItems();
            if(rsRecords.next()){
                do {

                    Object item = agendaYResolucionesContainer.addItem();
                    agendaYResolucionesContainer.getContainerProperty(item, ID_AGENDA_PROPERTY).setValue(rsRecords.getInt("IdAgenda"));
                    agendaYResolucionesContainer.getContainerProperty(item, CORRELATIVO_PROPERTY).setValue(agendaList.size()+1);
                    agendaYResolucionesContainer.getContainerProperty(item, PUNTO_AGENDA_PROPERTY).setValue(rsRecords.getString("PuntoAgenda"));
                    agendaYResolucionesContainer.getContainerProperty(item, RESOLUCION_PROPERTY).setValue(rsRecords.getString("Resolucion"));

                    agendaList.add(agendaList.size());
                }while (rsRecords.next());
            }

            addGridAgenda(false);

        } catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de Angemda : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de Angemda..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void fillParticipante(){
        String queryString = "SELECT * ";
        queryString += "FROM visita_inspeccion_participante ";
        queryString += "WHERE IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            participantesContainer.removeAllItems();
            if(rsRecords.next()){
                do {
                    Object item = participantesContainer.addItem();
                    participantesContainer.getContainerProperty(item, CORRELATIVO_PROPERTY).setValue(participanteList.size());
                    participantesContainer.getContainerProperty(item, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    participantesContainer.getContainerProperty(item, DPI_PROPERTY).setValue(rsRecords.getString("DPI"));
                    participantesContainer.getContainerProperty(item, EMAIL_PROPERTY).setValue(rsRecords.getString("Email"));

                    participanteList.add(participanteList.size());
                }while (rsRecords.next());
            }
            addGridParticipantes(false);

        } catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de Participantes : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de Participantes..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void fillInspectionTaskGrid() {
        tareasContainer.removeAllItems();
        tareaList.clear();

        String queryString = "Select *";
        queryString += " From visita_inspeccion_tarea";
        queryString += " Where IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado


                do {

                    Object item = tareasContainer.addItem();
                    //0123456789012345
                    //XXYYZZDDMMAACCTT
                    tareasContainer.getContainerProperty(item, ID_PROPERTY).setValue(rsRecords.getString("IdVisitaInspeccionTarea"));
                    tareasContainer.getContainerProperty(item, CODIGO_TAREA_PROPERTY).setValue(rsRecords.getString("IdVisitaInspeccionTarea"));
                    tareasContainer.getContainerProperty(item, CORRELATIVO_PROPERTY).setValue(rsRecords.getString("CodigoTarea").substring(14, 16));
                    tareasContainer.getContainerProperty(item, RUBRO_PROPERTY).setValue(rsRecords.getString("Rubro"));
                    tareasContainer.getContainerProperty(item, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    tareasContainer.getContainerProperty(item, RESPONSABLE_PROPERTY).setValue(rsRecords.getString("Responsable"));
                    tareasContainer.getContainerProperty(item, EJECUTOR_PROPERTY).setValue(rsRecords.getString("Ejecutor"));
                    tareasContainer.getContainerProperty(item, GARANTIA_PROPERTY).setValue(rsRecords.getString("Garantia"));
                    tareasContainer.getContainerProperty(item, ES_TAREA_PROPERTY).setValue(rsRecords.getString("EsTarea"));
                    tareasContainer.getContainerProperty(item, PRESUPUESTO_PROPERTY).setValue(rsRecords.getString("Presupuesto"));
                    tareasContainer.getContainerProperty(item, CENTRO_COSTO_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));
                    tareasContainer.getContainerProperty(item, AUTORIZA_PROPERTY).setValue(rsRecords.getString("AutorizadoTipo"));
                    tareasContainer.getContainerProperty(item, DIAS_HABILES_PROPERTY).setValue(rsRecords.getInt("DiasHabiles"));
                    tareasContainer.getContainerProperty(item, EQUIPO_DIBUJO_PROPERTY).setValue(rsRecords.getString("EquipoDibujo"));
                    tareasContainer.getContainerProperty(item, VISIBLE_PROPERTY).setValue(rsRecords.getString("VisibleParaCliente"));
                    tareasContainer.getContainerProperty(item, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    if (rsRecords.getObject("FechaUltimoEstatus") == null) {
                        tareasContainer.getContainerProperty(item, FECHA_ESTATUS_PROPERTY).setValue("");
                    } else {
                        tareasContainer.getContainerProperty(item, FECHA_ESTATUS_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaUltimoEstatus")));
                    }

                    tareasContainer.getContainerProperty(item, FOTOS_PROPERTY).setValue("Fotos");

                    tareaList.add(item);

                } while (rsRecords.next());

            }

            addGridTareas(false);
        } catch (Exception ex) {
            Logger.getLogger(InspectionTasksWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al leer registros de tareas visita  : " + ex.getMessage());
            Notification.show("Error al leer registros de tareas visitas..!", Notification.Type.ERROR_MESSAGE);
        }
    }

}
