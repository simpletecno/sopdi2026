/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;


import com.simpletecno.sopdi.SeguimientoHandler;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.*;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.*;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class ProjectTaskView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public Statement stQuery1 = null;
    public ResultSet rsRecords = null;
    public ResultSet rsRecords1 = null;

    MarginInfo marginInfo;

    IndexedContainer projectsContainer;
    Grid projectsGrid;
    Button uploadProjectBtn;
    Button downloadProjectBtn;
    Button exportExcelProjectBtn;
    Button integracionBtn;
    Button idexRecursosBtn;
    Button notasBtn;
    Button compararBtn;

    TextField codigoTareaTxt;
    TextField descripcionTxt;
    Button findBtn;
    Table tareasTable;

    ProgressBar progressBar;

    public static Locale locale = new Locale("ES", "GT");
//    static DecimalFormat numberFormat = new DecimalFormat("#,###,###.##");

    UI mainUI;

    public ProjectTaskView() {
        this.mainUI = UI.getCurrent();

        setSpacing(true);
//        setCaption("Projects y tareas");
        Page.getCurrent().setTitle("Projects y tareas");

//        marginInfo = new MarginInfo(true,true,true,true);
        marginInfo = new MarginInfo(true, false, false, false);

        createProjectsLayout();
        createTaskLayout();

        fillProjects();
    }

    private void createProjectsLayout() {
        VerticalLayout projectsLayout = new VerticalLayout();
        projectsLayout.setWidth("95%");
        projectsLayout.addStyleName("rcorners3");
        projectsLayout.setResponsive(true);
        projectsLayout.setMargin(true);

        projectsContainer = new IndexedContainer();

        projectsContainer.addContainerProperty("idProject", String.class, null);
        projectsContainer.addContainerProperty("numero", String.class, null);
        projectsContainer.addContainerProperty("estatus", String.class, null);
        projectsContainer.addContainerProperty("descripción", String.class, null);
        projectsContainer.addContainerProperty("fecha", String.class, null);
        projectsContainer.addContainerProperty("etiqueta", String.class, null);
        projectsContainer.addContainerProperty("idVisita", String.class, null);
        projectsContainer.addContainerProperty("archivo", String.class, null);

        projectsGrid = new Grid("Projects. ", projectsContainer);

        projectsGrid.setImmediate(true);
        projectsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        projectsGrid.setDescription("Seleccione un registro.");
        projectsGrid.setHeightMode(HeightMode.ROW);
        projectsGrid.setHeightByRows(10);
        projectsGrid.setWidth("100%");
        projectsGrid.setResponsive(true);
        projectsGrid.setEditorBuffered(false);
        projectsGrid.setEditorEnabled(false);
        projectsGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event
            ) {
                if (projectsGrid.getSelectedRow() != null) {
                    fillTasks();
                }
            }
        });

        projectsGrid.getColumn("descripción").setWidth(300);
        projectsGrid.getColumn("archivo").setHidable(true).setHidden(true);
        projectsGrid.getColumn("idProject").setHidable(true).setHidden(true);

        projectsLayout.addComponent(projectsGrid);

        uploadProjectBtn = new Button("Cargar nuevo");
        uploadProjectBtn.setIcon(FontAwesome.UPLOAD);
        uploadProjectBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        uploadProjectBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                CargarProjectTareas cargarProject = new CargarProjectTareas();
                mainUI.addWindow(cargarProject);
                cargarProject.center();
            }
        });

//        SimpleFileDownloader downloader = new SimpleFileDownloader();
//        addExtension(downloader);

        downloadProjectBtn = new Button("Descargar");
        downloadProjectBtn.setIcon(FontAwesome.DOWNLOAD);
        downloadProjectBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        downloadProjectBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (projectsContainer.size() > 0) {
                    if (projectsGrid.getSelectedRow() != null) {
                        try {
                            // Open the file for writing.

//System.out.println(String.valueOf(projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(),ARCHIVO_PROPERTY).getValue()));

                            File downloadFile = new File(String.valueOf(projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(),"archivo").getValue()));
//System.out.println("toPath = " + downloadFile.toPath().toString());

//System.out.println("existe = " + Files.exists(downloadFile.toPath()));

                            final byte docBytes[] = Files.readAllBytes(downloadFile.toPath());

                            final StreamResource resource = new StreamResource(() -> {
                                return new ByteArrayInputStream(docBytes);
//                            }, String.valueOf(projectsContainer.getContainerProperty(DESCRIPCION_PROPERTY, projectsGrid.getSelectedRow()).getValue()));
                            }, downloadFile.getName());

                            resource.setMIMEType("application/mpp");

                            BrowserFrame downBrowser = new BrowserFrame();
                            downBrowser.setSizeFull();
                            downBrowser.setSource(resource);

                            Window downloadW = new Window();
                            downloadW.setWidth("500");
                            downloadW.setHeight("120");

                            VerticalLayout downLayout = new VerticalLayout();
                            downLayout.setSpacing(true);
                            downLayout.setMargin(true);
                            downLayout.addStyleName("rcorners1");

                            Button closeBtn = new Button("Documento descargado !, click aqui para cerrar.");
                            closeBtn.setIcon(FontAwesome.CLOSE);
                            closeBtn.setWidth(350, Sizeable.UNITS_PIXELS);
                            closeBtn.addClickListener(new Button.ClickListener() {
                                @Override
                                public void buttonClick(Button.ClickEvent event) {
                                    downLayout.removeAllComponents();
                                    downloadW.setContent(null);
                                    downloadW.detach();
                                    ((SopdiUI) mainUI).removeWindow(downloadW);
                                }
                            });

                            downLayout.addComponent(closeBtn);
                            downLayout.setComponentAlignment(closeBtn, Alignment.TOP_CENTER);
                            downLayout.addComponent(downBrowser);

                            downloadW.setContent(downLayout);
                            downloadW.center();

                            ((SopdiUI) mainUI).addWindow(downloadW);
                            /***
                             final StreamResource resource = new StreamResource(() -> {
                             return new ByteArrayInputStream(docBytes);
                             //                            }, String.valueOf(projectsContainer.getContainerProperty(DESCRIPCION_PROPERTY, projectsGrid.getSelectedRow()).getValue()));
                             }, "testdownload.txt");

                             downloader.setFileDownloadResource(resource);
                             downloader.download();
                             ***/
                        } catch (final Exception e) {
                            new Notification("Could not open file",
                                    e.getMessage(),
                                    Notification.Type.ERROR_MESSAGE)
                                    .show(Page.getCurrent());
                            e.printStackTrace();
                            return;
                        }
                    }
                    else {
                        Notification.show("Por favor seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show("Por favor seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        exportExcelProjectBtn = new Button("Exportar Excel");
        exportExcelProjectBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelProjectBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        exportExcelProjectBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (projectsContainer.size() > 0) {
                    if (projectsGrid.getSelectedRow() != null) {
                        exportToExcel(tareasTable);
                    }
                    else {
                        Notification.show("Por favor seleccione el project correspondiente.", Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show("Por favor seleccione el project correspondiente.", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        integracionBtn = new Button("Integración");
        integracionBtn.setIcon(FontAwesome.BARS);
        integracionBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        integracionBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (projectsContainer.size() > 0) {
                    if (projectsGrid.getSelectedRow() != null) {
                         IntegracionItemCostos integracionItemCostos
                                 = new IntegracionItemCostos(
                                         String.valueOf(projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(),"numero").getValue()));
                         mainUI.addWindow(integracionItemCostos);
                        integracionItemCostos.center();
                    } else {
                        Notification.show("Por favor seleccione el project correspondiente.", Notification.Type.WARNING_MESSAGE);
                    }
                }
            }
        });

        idexRecursosBtn = new Button("IDEX Recursos");
        idexRecursosBtn.setIcon(FontAwesome.USERS);
        idexRecursosBtn.setWidth(130, Sizeable.UNITS_PIXELS);
        idexRecursosBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (projectsContainer.size() > 0) {
                    if (projectsGrid.getSelectedRow() != null) {
//                        IdexRecursoTrabajoWindow idexRecursoTrabajoViewdexRecursoTrabajoView
//                                = new IdexRecursoTrabajoWindow(
//                                String.valueOf(projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(),"numero").getValue()),
//                                String.valueOf(projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(),"descripción").getValue())
//                        );
//                        mainUI.addWindow(idexRecursoTrabajoViewdexRecursoTrabajoView);
//                        idexRecursoTrabajoViewdexRecursoTrabajoView.center();
                    } else {
                        Notification.show("Por favor seleccione el project correspondiente.", Notification.Type.WARNING_MESSAGE);
                    }
                }
            }
        });

        progressBar = new ProgressBar();
        progressBar.setValue(0.01f);
        progressBar.setImmediate(true);
//        progressBar.setSizeFull();
        progressBar.setVisible(false);

        notasBtn = new Button("Notas");
        notasBtn.setIcon(FontAwesome.NEWSPAPER_O);
        notasBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        notasBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (tareasTable.size() > 0) {
                    if (tareasTable.getValue() != null) {
                        SeguimientoHandler seguimientoHandler = new SeguimientoHandler();
                        seguimientoHandler.fillTrackTable(String.valueOf(tareasTable.getValue()), String.valueOf(tareasTable.getContainerProperty(tareasTable.getValue(), "descripción").getValue()));
                        UI.getCurrent().addWindow(seguimientoHandler);
                        seguimientoHandler.center();
                    } else {
                        Notification.show("Por favor seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                    }
                }
            }
        });

        compararBtn = new Button("Generar estadística");
        compararBtn.setIcon(FontAwesome.GEARS);
        compararBtn.setWidth(150, Sizeable.UNITS_PIXELS);
        compararBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (projectsContainer.size() > 0) {
//                    ProjectComparacion rojectComparacion = new ProjectComparacion();
//                    UI.getCurrent().addWindow(rojectComparacion);
//                    rojectComparacion.center();
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);

        buttonsLayout.addComponent(uploadProjectBtn);
        buttonsLayout.addComponent(downloadProjectBtn);
        buttonsLayout.addComponent(exportExcelProjectBtn);
        buttonsLayout.addComponent(integracionBtn);
        buttonsLayout.addComponent(idexRecursosBtn);
        buttonsLayout.addComponent(progressBar);
//        buttonsLayout.addComponent(notasBtn);
//        buttonsLayout.addComponent(compararBtn);

        projectsLayout.addComponent(buttonsLayout);
        projectsLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);

        addComponent(projectsLayout);
        setComponentAlignment(projectsLayout, Alignment.TOP_CENTER);
    }

    private void createTaskLayout() {

        VerticalLayout tasksLayout = new VerticalLayout();
        tasksLayout.addStyleName("rcorners3");
        tasksLayout.setResponsive(true);
        tasksLayout.setMargin(true);

        codigoTareaTxt = new TextField("IDEX :");
        codigoTareaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        codigoTareaTxt.setWidth("80%");
//        codigoTareaTxt.addValueChangeListener((Property.ValueChangeEvent event) -> {
        codigoTareaTxt.addTextChangeListener(new FieldEvents.TextChangeListener() {
                SimpleStringFilter filter = null;

                public void textChange(FieldEvents.TextChangeEvent event) {
                    fillTasks();
//                    descripcionTxt.setValue("");
//
//                    Container.Filterable f = (Container.Filterable)
//                            tareasTable.getContainerDataSource();
//
//                    // Remove old filter
//                    if (filter != null)
//                        f.removeContainerFilter(filter);
//
//                    // Set new filter for the "Name" column
//                    filter = new SimpleStringFilter("IDEX", event.getText(),
//                            true, false);
//                    f.addContainerFilter(filter);
                }
        });

        descripcionTxt = new TextField("Descripción/Nombre de tarea :");
        descripcionTxt.setWidth("100%");
        descripcionTxt.addValueChangeListener((Property.ValueChangeEvent event) -> {
            codigoTareaTxt.setValue("");
        });

        // END-EXAMPLE: component.select.optiongroup.basic
        findBtn = new Button("Buscar");
        findBtn.setWidth("80%");
        findBtn.setIcon(FontAwesome.BINOCULARS);
        findBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillTasks();
            }
        });

        Label h1 = new Label("Tareas");
        h1.addStyleName("h2");

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setMargin(false);
        filterLayout.setWidth("50%");
        filterLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        filterLayout.addStyleName("rcorners3");

//        filterLayout.addComponent(h1);
        filterLayout.addComponent(codigoTareaTxt);
        filterLayout.addComponent(descripcionTxt);
        filterLayout.addComponent(findBtn);
        filterLayout.setComponentAlignment(findBtn, Alignment.BOTTOM_RIGHT);

        tasksLayout.addComponent(filterLayout);
        tasksLayout.setComponentAlignment(filterLayout, Alignment.TOP_CENTER);

        tasksLayout.addComponent(createtareasTable());

        addComponent(tasksLayout);
        setComponentAlignment(tasksLayout, Alignment.TOP_CENTER);

    }

    private Table createtareasTable() {

        tareasTable = new Table("Listado de Tareas ");
        tareasTable.addStyleName(ValoTheme.TABLE_COMPACT);
        tareasTable.setSizeFull();
        tareasTable.setImmediate(true);
        tareasTable.setSelectable(true);

        tareasTable.addContainerProperty("Id", Double.class, 0);
        tareasTable.addContainerProperty("IDEX", String.class, "");
        tareasTable.addContainerProperty("IdProject", Double.class, null);
        tareasTable.addContainerProperty("IDCC", Double.class, null);
        tareasTable.addContainerProperty("Nombre", String.class, null);
        tareasTable.addContainerProperty("Duración", Double.class, null);
        tareasTable.addContainerProperty("Comienzo", Date.class, null);
        tareasTable.addContainerProperty("Fin", Date.class, null); // 10
        tareasTable.addContainerProperty("Predecesoras", String.class, null);
        tareasTable.addContainerProperty("Sucesoras", String.class, null);
        tareasTable.addContainerProperty("Comienzo_real", Date.class, null);
        tareasTable.addContainerProperty("Fin_real", Date.class, null);
        tareasTable.addContainerProperty("Duración_real", Double.class, 0);
        tareasTable.addContainerProperty("Duración_restante", Double.class, 0);
        tareasTable.addContainerProperty("RH1", Double.class, 0.00);
        tareasTable.addContainerProperty("RH2", Double.class, 0.00);
        tareasTable.addContainerProperty("Comienzo_de_linea_base1", Date.class, null);
        tareasTable.addContainerProperty("Fin_de_linea_base1", Date.class, null); ///20
        tareasTable.addContainerProperty("Duración_de_linea_base1", Double.class, 0);
        tareasTable.addContainerProperty("EDT", String.class, "");
        tareasTable.addContainerProperty("Nivel_de_esquema", Double.class, 0);
        tareasTable.addContainerProperty("Hito", Boolean.class, false);
        tareasTable.addContainerProperty("Resumen", Date.class, null);
        tareasTable.addContainerProperty("Tareas_críticas", Boolean.class, false);
        tareasTable.addContainerProperty("Activo", Boolean.class, false);
        tareasTable.addContainerProperty("Modo_de_tarea", Double.class, 0);
        tareasTable.addContainerProperty("Tipo_de_restricción", Double.class, 0);
        tareasTable.addContainerProperty("Fecha_de_restricción", Date.class, null); //30
        tareasTable.addContainerProperty("Notas", String.class, "");
        tareasTable.addContainerProperty("Contacto", String.class, "");
        tareasTable.addContainerProperty("ContratoNeto", Double.class, 0.00);
        tareasTable.addContainerProperty("CambiosContrato", Double.class, 0.00);
        tareasTable.addContainerProperty("Costo_Real_(Real)", Double.class, 0.00);
        tareasTable.addContainerProperty("Presupuesto_Nacsa", Double.class, 0.00);
        tareasTable.addContainerProperty("CostoReal_ManoObra", Double.class, 0.00);
        tareasTable.addContainerProperty("CostoReal_Materiales", Double.class, 0.00);
        tareasTable.addContainerProperty("CostoReal_Nacsa", Double.class, 0.00);
        tareasTable.addContainerProperty("Estimacion", Boolean.class, false); //40
        tareasTable.addContainerProperty("Ejecutada", Boolean.class, false);
        tareasTable.addContainerProperty("Crítica", Boolean.class, false);
        tareasTable.addContainerProperty("DLI", Double.class, 0.00);
        tareasTable.addContainerProperty("NoEst", Double.class, 0.00);
        tareasTable.addContainerProperty("NoPrograma", Double.class, 0.00);
        tareasTable.addContainerProperty("RH3", Double.class, 0.00);
        tareasTable.addContainerProperty("RH4", Double.class, 0.00);
        tareasTable.addContainerProperty("RH5", Double.class, 0.00);
        tareasTable.addContainerProperty("RH6", Double.class, 0.00);
        tareasTable.addContainerProperty("CodigoEstilo", String.class, ""); //50
        tareasTable.addContainerProperty("NombreVentas", String.class, "");
        tareasTable.addContainerProperty("NombreTecnico", String.class, "");
        tareasTable.addContainerProperty("IdNivel", String.class, "");
        tareasTable.addContainerProperty("CodPlanos", String.class, "");
        tareasTable.addContainerProperty("Proveedor", Double.class, 0.00); //55

        return tareasTable;
    }

    public void fillProjects() {

        projectsContainer.removeAllItems();

        String queryString = "";

        queryString = "Select *";
        queryString += " From  project";
        queryString += " Where IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " Order By Numero";

//System.out.println("\n\n"+queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    Utileria utileria = new Utileria();

                    Object itemId = projectsContainer.addItem();

                    projectsContainer.getContainerProperty(itemId, "idProject").setValue(rsRecords.getString("Id"));
                    projectsContainer.getContainerProperty(itemId, "numero").setValue(rsRecords.getString("Numero"));
                    projectsContainer.getContainerProperty(itemId, "estatus").setValue(rsRecords.getString("Estatus"));
                    projectsContainer.getContainerProperty(itemId, "descripción").setValue(rsRecords.getString("Descripcion"));
                    projectsContainer.getContainerProperty(itemId, "fecha").setValue(rsRecords.getString("CreadoFecha"));
                    projectsContainer.getContainerProperty(itemId, "etiqueta").setValue(rsRecords.getString("Etiqueta"));
                    projectsContainer.getContainerProperty(itemId, "idVisita").setValue(rsRecords.getString("IdVisita"));
                    projectsContainer.getContainerProperty(itemId, "archivo").setValue(rsRecords.getString("ArchivoNombre"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de projects : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de projects..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    public void fillTasks() {

        if(projectsContainer.size() == 0) {
            return;
        }
        if(projectsGrid.getSelectedRow() == null) {
            return;
        }

        downloadProjectBtn.setEnabled(false);
        uploadProjectBtn.setEnabled(false);
        integracionBtn.setEnabled(false);

        tareasTable.removeAllItems();
        tareasTable.clear();

        tareasTable.setCaption("Listado de tareas de project : " + projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(), "idProject").getValue() + " ...ESPERE...");

        tareasTable.setColumnFooter("Nombre", "0");

        String queryString = "";

        queryString = "Select *";
        queryString += " From  project_tarea";
        queryString += " Where IdProject = " + String.valueOf(projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(), "idProject").getValue());
        if (codigoTareaTxt.getValue().trim().isEmpty() == false) {
            queryString += " And IDEX = '" + codigoTareaTxt.getValue().trim() + "'";
        }
        if (descripcionTxt.getValue().trim().isEmpty() == false) {
            queryString += " And Descripcion Like '%" + descripcionTxt.getValue().trim() + "%'";
        }

//System.out.println("\n\nQueryProyectTask="+queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                Utileria utileria = new Utileria();

                int primerRegistro = rsRecords.getInt("IdTareaProject");

                progressBar.setVisible(true);

                float progreessCount = 1.0f;

                do {
                    progressBar.setValue(progreessCount++);
//System.out.println("Registro No . " + rsRecords.getInt("IdTareaProject") + " " + rsRecords.getString("Descripcion"));
                    tareasTable.addItem(new Object[]{
                            rsRecords.getDouble("IdTareaProject"),
                            rsRecords.getString("Idex"),
                            rsRecords.getDouble("Numero"),
                            rsRecords.getDouble("IdCentroCosto"),
                            rsRecords.getString("Descripcion"),
                            rsRecords.getDouble("DiasDuracion"),
                            rsRecords.getDate("FechaInicio"),
                            rsRecords.getDate("FechaFin"), //10
                            rsRecords.getString("IdPredecesores"),
                            rsRecords.getString("IdSucesores"),
                            rsRecords.getDate("FechaRealInicio"),
                            rsRecords.getDate("FechaRealFin"),
                            rsRecords.getDouble("DuracionReal"),
                            rsRecords.getDouble("DuracionRestante"),
                            rsRecords.getDouble("RH1"),
                            rsRecords.getDouble("RH2"),
                            rsRecords.getDate("ComienzoLineaBase1"),
                            rsRecords.getDate("FinLineaBase1"), //20
                            rsRecords.getDouble("DuracionLineaBase1"),
                            rsRecords.getString("EDT"),
                            rsRecords.getDouble("NivelEsquema"),
                            (rsRecords.getString("Hito").equals("SI") ? true : false),
                            rsRecords.getDate("Resumen"),
                            (rsRecords.getString("TareasCriticas").equals("SI") ? true : false),
                            (rsRecords.getString("Activo").equals("SI") ? true : false),
                            rsRecords.getDouble("ModoTarea"),
                            rsRecords.getDouble("TipoRestriccion"),
                            rsRecords.getDate("FechaRestriccion"), //30
//                            rsRecords.getString("Notas"),
                            "NOTAS...",
                            rsRecords.getString("Contacto"),
                            rsRecords.getDouble("ContratoNeto"),
                            rsRecords.getDouble("CambiosContrato"),
                            rsRecords.getDouble("CostoRealReal"),
                            rsRecords.getDouble("PresupuestoNacsa"),
                            rsRecords.getDouble("CostoRealManoObra"),
                            rsRecords.getDouble("CostoRealMateriales"),
                            rsRecords.getDouble("CostoRealNacsa"),
                            (rsRecords.getString("Estimacion").equals("SI") ? true : false), //40
                            (rsRecords.getString("Ejecutada").equals("SI") ? true : false),
                            (rsRecords.getString("Critica").equals("SI") ? true : false),
                            rsRecords.getDouble("DLI"),
                            rsRecords.getDouble("NoEst"),
                            rsRecords.getDouble("NoPrograma"),
                            rsRecords.getDouble("RH3"),
                            rsRecords.getDouble("RH4"),
                            rsRecords.getDouble("RH5"),
                            rsRecords.getDouble("RH6"),
                            rsRecords.getString("CodigoEstilo"), //50
                            rsRecords.getString("NombreVentas"),
                            rsRecords.getString("NombreTecnico"),
                            rsRecords.getString("IdNivel"),
                            rsRecords.getString("CodPlanos"),
                            rsRecords.getDouble("IdProveedor")
                    }, rsRecords.getInt("IdTareaProject"));

//                    tareasTable.setValue(rsRecords.getInt("IdTarea"));
//                    if (rsRecords.getInt("IdTareaPadre") > 0) {
//                        tareasTable.setParent(rsRecords.getInt("IdTareaProject"), rsRecords.getInt("IdTareaPadre"));
//                        tareasTable.setCollapsed(rsRecords.getInt("IdTareaPadre"), false);
//                    }
//
                } while (rsRecords.next());

                progressBar.setVisible(false);
                tareasTable.select(primerRegistro);
            }
            tareasTable.setColumnFooter("Nombre", String.valueOf(tareasTable.size()));
        } catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de tareas : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de tareas..!", Notification.Type.ERROR_MESSAGE);
        }

        tareasTable.setCaption("Listado de tareas de project : " + projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(), "idProject").getValue());

        downloadProjectBtn.setEnabled(true);
        uploadProjectBtn.setEnabled(true);
        integracionBtn.setEnabled(true);

    }

    private String getBudget(String IDEX) {

        String queryString = "";

        queryString =  "Select Moneda, Idex, SUM(Total) TotalTotal ";
        queryString += " From  DetalleItemsCostos ";
        queryString += " Where IdProject = " + String.valueOf(projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(), "numero").getValue());;
//        queryString += " And CAST(Idex AS SIGNED)= " + String.valueOf(projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(), NUMERO_PROPERTY).getValue()) + IDEX;
        queryString += " And Idex = '" + IDEX + "'";
        queryString += " And Tipo In ('INTINI', 'DOCA')";
        queryString += " Group By Moneda, Idex";

//        System.out.println("\n"+queryString);

        try {

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery (queryString);

            queryString = "0.00";

            if(rsRecords1.next()) { //  encontrado

                if(rsRecords1.getString("Moneda").toUpperCase().trim().equals("QUETZALES")) {
                    queryString = "Q." +rsRecords1.getString("TotalTotal");
                }
                else {
                    queryString = "$." + rsRecords1.getString("TotalTotal");
                }

            }
        }
        catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }

        return queryString;
    }

    public boolean exportToExcel(Table tableToExport) {
        ExcelExport excelExport;

        excelExport = new ExcelExport(tableToExport);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName("PROJECT_" + projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(), "numero").getValue() + ".xls");

//        String mainTitle = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName() + " - TAREAS AL: " + new Utileria().getFechaYYYYMMDD_1(new Date());
//
//        excelExport.setReportTitle(mainTitle);

        excelExport.export();

        return true;

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

    void setTableTitle(String tableTitle) {
        if (tareasTable != null) {
            tareasTable.setCaption(tableTitle);
            tareasTable.setDescription(tableTitle);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub

    }

}

/***
    File downloadFile = new File(String.valueOf(projectsContainer.getContainerProperty(projectsGrid.getSelectedRow(),"archivo").getValue()));


    final byte docBytes[] = Files.readAllBytes(downloadFile.toPath());

    final StreamResource resource = new StreamResource(() -> {
        return new ByteArrayInputStream(docBytes);
//                            }, String.valueOf(projectsContainer.getContainerProperty(DESCRIPCION_PROPERTY, projectsGrid.getSelectedRow()).getValue()));
    }, downloadFile.getName());

                            resource.setMIMEType("application/mpp");

                                    BrowserFrame downBrowser = new BrowserFrame();
                                    downBrowser.setSizeFull();
                                    downBrowser.setSource(resource);

                                    Window downloadW = new Window();
                                    downloadW.setWidth("500");
                                    downloadW.setHeight("120");

                                    VerticalLayout downLayout = new VerticalLayout();
                                    downLayout.setSpacing(true);
                                    downLayout.setMargin(true);
                                    downLayout.addStyleName("rcorners1");

                                    Button closeBtn = new Button("Documento descargado !, click aqui para cerrar.");
                                    closeBtn.setIcon(FontAwesome.CLOSE);
                                    closeBtn.setWidth(350, Sizeable.UNITS_PIXELS);
                                    closeBtn.addClickListener(new Button.ClickListener() {
@Override
public void buttonClick(Button.ClickEvent event) {
        downLayout.removeAllComponents();
        downloadW.setContent(null);
        downloadW.detach();
        ((SopdiUI) mainUI).removeWindow(downloadW);
        }
        });

        downLayout.addComponent(closeBtn);
        downLayout.setComponentAlignment(closeBtn, Alignment.TOP_CENTER);
        downLayout.addComponent(downBrowser);

        downloadW.setContent(downLayout);
        downloadW.center();

        ((SopdiUI) mainUI).addWindow(downloadW);
        /***
         final StreamResource resource = new StreamResource(() -> {
         return new ByteArrayInputStream(docBytes);
         //                            }, String.valueOf(projectsContainer.getContainerProperty(DESCRIPCION_PROPERTY, projectsGrid.getSelectedRow()).getValue()));
         }, "testdownload.txt");

         downloader.setFileDownloadResource(resource);
         downloader.download();
         *** /
        } catch (final Exception e) {
        new Notification("Could not open file",
        e.getMessage(),
        Notification.Type.ERROR_MESSAGE)
        .show(Page.getCurrent());
        e.printStackTrace();
        return;
        }
 ****/
