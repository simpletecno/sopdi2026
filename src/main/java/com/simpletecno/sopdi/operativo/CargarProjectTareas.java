/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.sun.istack.logging.Logger;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

//import net.sf.mpxj.Duration;
//import net.sf.mpxj.ProjectFile;
//import net.sf.mpxj.Relation;
//import net.sf.mpxj.Task;
//import net.sf.mpxj.reader.ProjectReader;
//import net.sf.mpxj.reader.ProjectReaderUtility;
import org.vaadin.ui.NumberField;
import org.vaadin.dialogs.ConfirmDialog;

import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Relation;
import net.sf.mpxj.Task;
import net.sf.mpxj.reader.ProjectReader;
import net.sf.mpxj.reader.ProjectReaderUtility;
/**
 *
 * @author joseaguirre
 */
public class CargarProjectTareas extends Window {

    VerticalLayout mainLayout;

    Statement stQuery = null;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords = null;

    MarginInfo marginInfo;

    ComboBox projectCbx;
    TextField numeroTxt;
    TextField idVisitaTxt;
    TextField etiquetaTxt;

    MultiFileUpload singleUpload;

    Label archivoLbl = new Label("");

    Button salirBtn;
    Button cargarBtn;

    public File file;

    TreeTable tareasTable;

    public static Locale locale = new Locale("ES", "GT");
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("##,###");

    int linea = 0;

    UI mainUI;

    public CargarProjectTareas() {

        setWidth("85%");
//        setHeight("70%");

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        setContent(mainLayout);

        this.mainUI = UI.getCurrent();

        setCaption(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Importar archivo MS PROJECT de tareas del proyecto : " + ((SopdiUI) mainUI).sessionInformation.getStrProjectName());
        Page.getCurrent().setTitle(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Importar tareas");

        marginInfo = new MarginInfo(true, true, true, true);

        HorizontalLayout encabezadoLayout;
        encabezadoLayout = new HorizontalLayout();
        encabezadoLayout.setWidth("95%");
//        encabezadoLayout.setHeight("5%");
        encabezadoLayout.setMargin(marginInfo);
        encabezadoLayout.setSpacing(true);
        encabezadoLayout.addStyleName("rcorners3");

        projectCbx = new ComboBox("Project de : ");
        projectCbx.addItem("Urbanización");
        projectCbx.addItem("Casas 1");
        projectCbx.addItem("Casas 2");
        projectCbx.addItem("Casas 3");
        projectCbx.addItem("Casas 4");
        projectCbx.addItem("Casas 5");
        projectCbx.addItem("Casas 6");
        projectCbx.addItem("Casas 7");
        projectCbx.addItem("Casas 8");
        projectCbx.addItem("Casas 9");
        projectCbx.addItem("Casas 10");
        projectCbx.addItem("Villas 1");
        projectCbx.addItem("Villas 2");
        projectCbx.addItem("Villas 3");
        projectCbx.addItem("Villas 4");
        projectCbx.addItem("Villas 5");
        projectCbx.addItem("Apartamentos 1");
        projectCbx.addItem("Apartamentos 2");
        projectCbx.addItem("Apartamentos 3");
        projectCbx.addItem("Apartamentos 4");
        projectCbx.addItem("Apartamentos 5");
        projectCbx.addItem("Casa club 1");
        projectCbx.addItem("Casa club 2");
        projectCbx.addItem("Casa club 3");
        projectCbx.addItem("Casa club 4");
        projectCbx.addItem("Casa club 5");
        projectCbx.addItem("Extras 1");
        projectCbx.addItem("Extras 2");
        projectCbx.addItem("Extras 3");
        projectCbx.addItem("Extras 4");
        projectCbx.addItem("Extras 5");
        projectCbx.select("Casas 1");

        numeroTxt = new TextField("Número : ");
        numeroTxt.setValue("0");
        numeroTxt.setImmediate(true);
        numeroTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        numeroTxt.setWidth("8em");

        idVisitaTxt = new NumberField("Id Vista : ");
        idVisitaTxt.setValue("0");
        idVisitaTxt.setImmediate(true);
        idVisitaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        idVisitaTxt.setWidth("8em");

        etiquetaTxt = new TextField("Etiqueta : ");
        etiquetaTxt.setValue("");
        etiquetaTxt.setWidth("8em");
        etiquetaTxt.addStyleName("v-textfield-uppercase");

        UploadFinishedHandler handler;

        handler = new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                File targetFile;

                try {

                    System.out.println("\nfileName=" + fileName);
                    fileName = fileName.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("ó", "o").replaceAll("é", "").replaceAll("-", "");
                    System.out.println("\nfileName=" + fileName);
                    System.out.println("length=" + stream.available());
                    System.out.println("mimeType=" + mimeType);

                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);
                    String filePath = VaadinService.getCurrent()
                            .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrProjectId() + "/";

                    new File(filePath).mkdirs();

                    fileName = filePath + fileName;

                    targetFile = new File(fileName);
                    OutputStream outStream = new FileOutputStream(targetFile);
                    outStream.write(buffer);
                    outStream.close();
                    stream.close();

                    System.out.println("\ntargetFile = " + fileName);

                    cargarTareas(targetFile);
                    file = targetFile;
                    cargarBtn.setEnabled(true);

                    archivoLbl.setValue(fileName);

                } catch (java.io.IOException fIoEx) {
                    fIoEx.printStackTrace();
                    Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }

        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler, window, false);
//        singleUpload.setCaption("Cargar archivo");
//        singleUpload2.setRootDirectory(newDoc.getAbsolutePath());
//        singleUpload.setPanelCaption("Buscar");
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Buscar archivo", "");

        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.mpp')");

        List<String> acceptedMimeTypes = new ArrayList();
        acceptedMimeTypes.add("application/octet-stream");
        acceptedMimeTypes.add("application/ms-project");
        acceptedMimeTypes.add("application/vnd.ms-project");
        acceptedMimeTypes.add("application/msproj");
        acceptedMimeTypes.add("application/msproject");
        acceptedMimeTypes.add("application/x-msproject");
        acceptedMimeTypes.add("application/x-ms-project");
        acceptedMimeTypes.add("application/x-dos_ms_project");
        acceptedMimeTypes.add("application/mpp");
        acceptedMimeTypes.add("zz-application/zz-winassoc-mpp");
//        singleUpload.setAcceptedMimeTypes(acceptedMimeTypes);

//        encabezadoLayout.addComponent(h1);
        encabezadoLayout.addComponents(projectCbx, numeroTxt, idVisitaTxt, etiquetaTxt, singleUpload);
        encabezadoLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(encabezadoLayout);
        mainLayout.setComponentAlignment(encabezadoLayout, Alignment.TOP_CENTER);

        archivoLbl.setSizeFull();
        mainLayout.addComponent(archivoLbl);
        mainLayout.setComponentAlignment(archivoLbl, Alignment.TOP_CENTER);

        VerticalLayout tareasLayout;
        tareasLayout = new VerticalLayout();
//        tareasLayout.setSizeUndefined();
        tareasLayout.setWidth("95%");
//        tareasLayout.setHeight("95%");
        tareasLayout.setMargin(marginInfo);
        tareasLayout.setSpacing(true);
        tareasLayout.addStyleName("rcorners3");

        tareasTable = new TreeTable();
        tareasTable.setWidth("100%");
        tareasTable.addContainerProperty("TID", String.class, "");
        tareasTable.addContainerProperty("IDEX", String.class, "");
        tareasTable.addContainerProperty("Descripción", String.class, "");

        tareasTable.addContainerProperty("Inicio", String.class, "");
        tareasTable.addContainerProperty("Fin", String.class, "");
        tareasTable.addContainerProperty("Duración", String.class, "");

        tareasTable.addContainerProperty("Nivel", String.class, "");
        tareasTable.addContainerProperty("Nivel código", String.class, "");
        tareasTable.addContainerProperty("Predecesores", String.class, "");

        tareasTable.addContainerProperty("Sucesores", String.class, "");
        tareasTable.addContainerProperty("RH1", String.class, "");
        tareasTable.addContainerProperty("RH2", String.class, "");

        tareasTable.addContainerProperty("RH3", String.class, "");
        tareasTable.addContainerProperty("RH4", String.class, "");
        tareasTable.addContainerProperty("FechaRealInicio", String.class, "");

        tareasTable.addContainerProperty("FechaRealFin", String.class, "");

        tareasTable.setColumnAlignments(new Table.Align[]{
                Table.Align.CENTER, Table.Align.LEFT, Table.Align.LEFT,
                Table.Align.LEFT, Table.Align.LEFT, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.LEFT,
                Table.Align.LEFT
        });

        tareasTable.setColumnWidth("TID", 50);
        tareasTable.setColumnWidth("IDEX", 50);
        tareasTable.setColumnWidth("Descripción", 300);
        tareasTable.setColumnWidth("Inicio", 60);
        tareasTable.setColumnWidth("Fin", 60);
        tareasTable.setColumnWidth("Duracion", 50);
        tareasTable.setColumnWidth("Nivel", 50);
        tareasTable.setColumnWidth("Nivel código", 50);
        tareasTable.setColumnWidth("Predecesores", 50);
        tareasTable.setColumnWidth("Sucesores", 50);

/**
 tareasTable.setFooterVisible(true);
 tareasTable.setColumnFooter("Total", "0.00");
 tareasTable.setSelectable(true);
 **/
        tareasLayout.addComponent(tareasTable);
        tareasLayout.setComponentAlignment(tareasTable, Alignment.MIDDLE_CENTER);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);

        salirBtn = new Button("SALIR");
        salirBtn.setIcon(FontAwesome.CLOSE);
        salirBtn.setWidth(150, Sizeable.UNITS_PIXELS);
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
//                tareasTable.removeAllItems();
//                tareasTable.clear();
            }
        });

        cargarBtn = new Button("CARGAR PROJECT");
        cargarBtn.setIcon(FontAwesome.ARROW_CIRCLE_O_UP);
        cargarBtn.setWidth(170, Sizeable.UNITS_PIXELS);
        cargarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (tareasTable.size() > 0) {
                    validarYGuardar();
                }
            }
        });
        cargarBtn.setEnabled(false);

        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(cargarBtn);
        buttonsLayout.setComponentAlignment(cargarBtn, Alignment.BOTTOM_CENTER);

        tareasLayout.addComponent(buttonsLayout);
        tareasLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(tareasLayout);
        mainLayout.setComponentAlignment(tareasLayout, Alignment.MIDDLE_CENTER);
    }

    void validarYGuardar() {

        if (tareasTable.size() == 0) {
            Notification.show("No exiten tareas para cargar, revise!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("POR FAVOR INGRESE UN NUMERO DE PROJECT.", Notification.Type.WARNING_MESSAGE);
            numeroTxt.focus();
            return;
        } else {
            try {
                if (Integer.valueOf(numeroTxt.getValue()) == 0) {
                    Notification.show("EL NUMERO DE PROJECT DEBE SER MAYOR A 0.", Notification.Type.ERROR_MESSAGE);
                    numeroTxt.focus();
                    return;
                }
            } catch (Exception xx1) {
                Notification.show("ID DE VISITA CON VALOR NO VALIDO.  TIENE QUE SER UN NUMERO.", Notification.Type.ERROR_MESSAGE);
                idVisitaTxt.focus();
                return;
            }
        }

        if (idVisitaTxt.getValue().trim().isEmpty()) {
            idVisitaTxt.setValue("0");
        } else {
            try {
                Integer.valueOf(idVisitaTxt.getValue());
            } catch (Exception xx1) {
                Notification.show("ID DE VISITA CON VALOR NO VALIDO.  TIENE QUE SER UN NUMERO.", Notification.Type.ERROR_MESSAGE);
                idVisitaTxt.focus();
                return;
            }
        }

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CARGAR este project a base de datos?",
                "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            String queryString;
                            getContent().setEnabled(false);

                            try {
                                queryString = "Select Numero";
                                queryString += " From  project";
                                queryString += " Where IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
                                queryString += " And Numero = " + numeroTxt.getValue();

                                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                rsRecords = stQuery.executeQuery(queryString);

                                if (rsRecords.next()) { //  encontrado

//                                    queryString = "Select Max(Numero) UltimoNumero";
//                                    queryString += " From  project";
//                                    queryString += " Where IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
//
//                                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
//                                    rsRecords = stQuery.executeQuery(queryString);
//                                    rsRecords.next();
//
                                    Notification.show("YA EXISTE UN \"PROJECT\" CON ESTE NUMERO PARA ESTE PROYECTO.", Notification.Type.WARNING_MESSAGE);

                                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Se procederá a poner INACTIVO el project anterior que tiene el mismo numero, está de acuerdo ? \n\n\n Esta ventana se cerrará cuando haya terminado el proceso...",
                                            "SI", "NO", new ConfirmDialog.Listener() {

                                                public void onClose(ConfirmDialog dialog) {
                                                    if (dialog.isConfirmed()) {
                                                        try {
                                                            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);
                                                            String queryString = "UPDATE project SET Estatus = 'INACTIVO' WHERE Numero = " + numeroTxt.getValue() + " AND Estatus = 'ACTIVO'";
                                                            stQuery.executeUpdate(queryString);
                                                            guardar();
                                                        } catch (Exception exe1) {
                                                            Logger.getLogger(this.getClass()).log(Level.SEVERE, "ERROR AL INTENTAR ACTUALIZAR PROJECT", exe1);
                                                            try {
                                                                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                                                            }
                                                            catch(java.sql.SQLException sqlE) {
                                                                //
                                                            }
                                                        }
                                                    } else {
                                                        Notification.show("CARGA DE ARCHIVO CANCELADA POR USUARIO", Notification.Type.WARNING_MESSAGE);
                                                    }
                                                }
                                            });

                                    numeroTxt.focus();
                                }
                                else {
                                    guardar();
                                }
                            } catch (Exception ex1) {
                                System.out.println("Error al insertar registros de tareas en base de datos..Transaccion abortada..!");
                                Notification.show("Error al insertar registro de tareas en base de datos..Transaccion abortada..!", Notification.Type.ERROR_MESSAGE);
                                ex1.printStackTrace();
                            }
                        } else {
                            Notification.show("CARGA DE ARCHIVO CANCELADA POR USUARIO", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                });
    }

    private void guardar() {
        String queryString ="";
        try {
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);

            queryString = "Insert Into project (IdEmpresa, IdProyecto, Numero, Fase, Descripcion,";
            queryString += " Etiqueta, IdVisita, CreadoUsuario, CreadoFecha, ArchivoNombre) ";
            queryString += " Values ( ";
            queryString += " " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
            queryString += "," + numeroTxt.getValue();
            queryString += ",'" + String.valueOf(projectCbx.getValue()).replaceAll("'", "\'") + "'";
            queryString += ",'" + file.getName() + "'";
            queryString += ",'" + etiquetaTxt.getValue().toUpperCase() + "'";
            queryString += ",'" + idVisitaTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",'" + file.getAbsolutePath() + "'";
            queryString += ")";
//System.out.println(queryString);
            stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stPreparedQuery.executeUpdate();
            rsRecords = stPreparedQuery.getGeneratedKeys();

            rsRecords.next();

            int idProject = rsRecords.getInt(1);

//            queryString = "update project set Estatus = 'ULTIMO' Where Numero = " + (Integer.valueOf(numeroTxt.getValue()) - 1);
//            stQuery.executeUpdate(queryString);

            ProjectReader reader = ProjectReaderUtility.getProjectReader(file.getAbsolutePath());
            ProjectFile mpx = reader.read(file.getAbsolutePath());

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            for (Task task : mpx.getAllTasks()) {
                Date date = task.getStart();
                String startDate = date != null ? df.format(date) : "0000-00-00";

                date = task.getFinish();
                String finishDate = date != null ? df.format(date) : "0000-00-00";

                date = task.getActualStart();
                String actualStartDate = date != null ? df.format(date) : "0000-00-00";

                date = task.getActualFinish();
                String actualFinishDate = date != null ? df.format(date) : "0000-00-00";

                Duration dur = task.getDuration();
                String duration = dur != null ? dur.toString() : "0";

                if (dur != null) {
                    duration = String.valueOf(Double.valueOf(duration.replace("d", "")).intValue());
                }

                dur = task.getActualDuration();
                String actualDuration = dur != null ? dur.toString() : "0";

                if (dur != null) {
                    actualDuration = String.valueOf(Double.valueOf(actualDuration.replace("d", "")).intValue());
                }

                dur = task.getRemainingDuration();
                String remainingDuration = dur != null ? dur.toString() : "0";

                if (dur != null) {
                    remainingDuration = String.valueOf(Double.valueOf(remainingDuration.replace("d", "")).intValue());
                }

                List<Relation> predecesoresList = task.getPredecessors();

                String predecesores = "";
                if (!predecesoresList.isEmpty()) {
                    for (Relation relationItem : predecesoresList) {
                        predecesores += relationItem.getTargetTask().getID() + ",";
                    }
                    predecesores = predecesores.substring(1, predecesores.length() - 1);
                }

                List<Relation> sucesoresList = task.getSuccessors();

                String sucesores = "";
                if (!sucesoresList.isEmpty()) {
                    for (Relation relationItem : sucesoresList) {
                        sucesores += relationItem.getTargetTask().getID() + ",";
                    }
                    sucesores = sucesores.substring(1, sucesores.length() - 1);
                }

                date = task.getBaselineStart();
                String baseLineStartDate = date != null ? df.format(date) : "0000-00-00";

                date = task.getBaselineFinish();
                String baseLineFinishDate = date != null ? df.format(date) : "0000-00-00";

                dur = task.getBaselineDuration();
                String baseLineDuration = dur != null ? dur.toString() : "0";

                if (dur != null) {
                    baseLineDuration = String.valueOf(Double.valueOf(baseLineDuration.replace("d", "")).intValue());
                }
                date = task.getResume();
                String resumeDate = date != null ? df.format(date) : "0000-00-00";

                date = task.getConstraintDate();
                String constractionDate = date != null ? df.format(date) : "0000-00-00";

//System.out.println("predecesores="+predecesores + " sucesures="+sucesores);

                queryString = "Insert  Into project_tarea (";
                queryString += " IdProject, Numero, IdTareaProject, IdTareaPadre, ";
                queryString += " IdentificadorExclusivo, IdexAnt, IDEX, IdCentroCosto, Descripcion, DiasDuracion, ";
                queryString += " FechaInicio, FechaFin, IdPredecesores, IdSucesores, FechaRealInicio, FechaRealFin, Id,";
                queryString += " DuracionReal, DuracionRestante, RH1, RH2, ComienzoLineaBase1, FinLineaBase1, DuracionLineaBase1, ";
                queryString += " EDT, NivelEsquema, Hito, Resumen, TareasCriticas, Activo, ModoTarea, TipoRestriccion, FechaRestriccion, ";
                queryString += " Notas, Contacto, ContratoNeto, CambiosContrato, CostoRealReal, PresupuestoNacsa, ";
                queryString += " CostoRealManoObra, CostoRealMateriales, CostoRealNacsa, Estimacion, Ejecutada, ";
                queryString += " Critica, DLI, NoEst, NoPrograma, RH3, RH4, RH5, RH6, CodigoEstilo, ";
                queryString += " NombreVentas, NombreTecnico, IdNivel, CodPlanos, IdProveedor ";
                queryString += ")";
                queryString += " Values ( ";
                queryString += " " + idProject;
                queryString += "," + numeroTxt.getValue();
                queryString += "," + task.getID();
                if (task.getParentTask() != null) {
                    queryString += "," + String.valueOf(task.getParentTask().getID());
                } else {
                    queryString += ",0";
                }
                queryString += "," + task.getUniqueID(); // Identifador Exclusivo
                queryString += "," + task.getNumber(1); // IdexAnt
                queryString += ",'" + task.getText(10) + "'"; // IDEX
                queryString += ",'" + task.getNumber(3).intValue() + "'"; //IDCENTROCOSTO
                queryString += ",'" + task.getName().replaceAll("'", "''") + "'";
                queryString += "," + duration;
                queryString += ",'" + startDate + "'";
                queryString += ",'" + finishDate + "'";
                queryString += ",'" + predecesores + "'";
                queryString += ",'" + sucesores + "'";
                queryString += ",'" + actualStartDate + "'";
                queryString += ",'" + actualFinishDate + "'";
                queryString += "," + actualDuration;
                queryString += "," + task.getID();
                queryString += "," + remainingDuration;
                queryString += "," + task.getNumber(15).intValue(); //RH1
                queryString += "," + task.getNumber(16).intValue(); //RH2
                queryString += ",'" + baseLineStartDate + "'";
                queryString += ",'" + baseLineFinishDate + "'";
                queryString += "," + baseLineDuration;
                queryString += ",'WBS" + task.getWBS() + "'"; // EDT
                queryString += "," + task.getOutlineLevel();
                queryString += ",'" + (task.getMilestone() ? "SI" : "NO") + "'";
                queryString += ",'" + resumeDate + "'";
                queryString += ",'" + (task.getCritical() ? "SI" : "NO") + "'";
                queryString += ",'" + (task.getActive() ? "SI" : "NO") + "'";
                queryString += "," + task.getTaskMode().getValue();
                queryString += "," + task.getConstraintType().getValue();
                queryString += ",'" + constractionDate + "'";
                queryString += ",'" + task.getNotes() + "'";
                queryString += ",'" + task.getContact() + "'";
                queryString += "," + task.getCost(1);
                queryString += "," + task.getCost(2);
                queryString += "," + task.getCost(3);
                queryString += "," + task.getCost(7);
                queryString += "," + task.getCost(8);
                queryString += "," + task.getCost(9);
                queryString += "," + task.getCost(10);
                queryString += ",'" + (task.getEnterpriseFlag(2) ? "SI" : "NO") + "'"; // marca 2
                queryString += ",'" + (task.getEnterpriseFlag(1) ? "SI" : "NO") + "'"; // marca 1
                queryString += ",'" + (task.getEnterpriseFlag(3) ? "SI" : "NO") + "'"; // marca 3
                queryString += "," + task.getNumber(2).intValue(); //DLI
                queryString += "," + task.getNumber(4).intValue(); //NoEst
                queryString += "," + task.getNumber(6).intValue(); //NoPrograma
                queryString += "," + task.getNumber(17).intValue(); //RH3
                queryString += "," + task.getNumber(18).intValue(); //RH4
                queryString += "," + task.getNumber(19).intValue(); //RH5
                queryString += "," + task.getNumber(20).intValue(); //RH6
                queryString += ",'" + task.getText(1) + "'"; // CODIGOESTILO
                queryString += ",'" + task.getText(2) + "'"; // NombreVentas
                queryString += ",'" + task.getText(3) + "'"; // NombreTECNICO
                queryString += ",'" + task.getText(4) + "'"; // IDNIVEL
                queryString += ",'" + task.getText(5) + "'"; // CODPLANOS
                queryString += "," + task.getNumber(14).intValue();// IDPROVEEDOR
                queryString += ")";

                //System.out.println("query=" + queryString);

                stQuery.executeUpdate(queryString);
            } //end for

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

            ((ProjectTaskView) (mainUI.getNavigator().getCurrentView())).fillProjects();

            Notification notif = new Notification("ARCHIVO CARGADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            cargarBtn.setEnabled(false);

            tareasTable.removeAllItems();
        }
        catch(Exception ex1) {
Logger.getLogger(this.getClass()).log(Level.SEVERE,queryString);
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
            }
            catch(java.sql.SQLException sqlE) {
                //
            }
            System.out.println("Error al insertar registros de tareas en base de datos..Transaccion abortada..!");
            Notification.show("Error al insertar registro de tareas en base de datos..Transaccion abortada..!", Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();

            String emailRecipients[] = {"alerta@simpletecno.com"};

            MyEmailMessanger eMail = new MyEmailMessanger();

            try {
                eMail.postMail(emailRecipients, "Error SOPDI Carga de Tareas Project", "ERROR AL INSERTAR REGISTROS : " + ex1.getMessage());
            } catch (Exception ex) {
//                    Logger.getLogger(MyUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }//endcatch//endcatch

//                    file.delete();
    }

    private void cargarTareas(File file) {

        tareasTable.removeAllItems();
        tareasTable.clear();

        try {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            ProjectReader reader = ProjectReaderUtility.getProjectReader(file.getAbsolutePath());
            ProjectFile mpx = reader.read(file.getAbsolutePath());

            for (Task task : mpx.getAllTasks()) {
                Date date = task.getStart();
                String startDate = date != null ? df.format(date) : "(no start date supplied)";

                date = task.getFinish();
                String finishDate = date != null ? df.format(date) : "(no finish date supplied)";

                date = task.getActualStart();
                String actualStartDate = date != null ? df.format(date) : "0000-00-00";

                date = task.getActualFinish();
                String actualFinishDate = date != null ? df.format(date) : "0000-00-00";

                Duration dur = task.getDuration();
                String duration = dur != null ? dur.toString() : "(no duration supplied)";
                
                if(dur != null) {
                    duration = String.valueOf(Double.valueOf(duration.replace("d", "")).intValue());
                }

                List<Relation> predecesoresList = task.getPredecessors();

                String predecesores = "";
                if(!predecesoresList.isEmpty()){
                    for(Relation relationItem: predecesoresList) {
                        predecesores += relationItem.getTargetTask().getID() + ",";
                    }
                    predecesores = predecesores.substring(1, predecesores.length()-1);
                }

                List<Relation> sucesoresList = task.getSuccessors();

                String sucesores = "";
                if(!sucesoresList.isEmpty()) {
                    for (Relation relationItem : sucesoresList) {
                        sucesores += relationItem.getTargetTask().getID() + ",";
                    }
                    sucesores = sucesores.substring(1, sucesores.length()-1);
		        }
                    
                String idTareaPadre  = "0";
                if(task.getParentTask() != null) {
                    idTareaPadre = String.valueOf(task.getParentTask().getID());
                }

//                Logger.getLogger(this.getClass()).log(Level.INFO, "numero : "+ task.getNumber(15));
//                Logger.getLogger(this.getClass()).log(Level.INFO, "numero : "+ task.getNumber(16));
//                Logger.getLogger(this.getClass()).log(Level.INFO, "numero : "+ task.getNumber(17));
//                Logger.getLogger(this.getClass()).log(Level.INFO, "numero : "+ task.getNumber(18));

                tareasTable.addItem(new Object[] {
                        String.valueOf(task.getID()),
                        String.valueOf(task.getFieldByAlias("IDEX")),
                        task.getName(),
                        startDate,
                        finishDate,
                        duration,
                        String.valueOf(task.getOutlineLevel()),
                        task.getOutlineNumber(),
                        predecesores,
                        sucesores,
                        String.valueOf(task.getNumber(15)),
                        String.valueOf(task.getNumber(16)),
                        String.valueOf(task.getNumber(17)),
                        String.valueOf(task.getNumber(18)),
                        actualStartDate,
                        actualFinishDate
                }, task.getID());
                
                if(task.getParentTask() != null) {
                    tareasTable.setParent(task.getID(), task.getParentTask().getID());
                    tareasTable.setCollapsed(task.getParentTask().getID(), false);
                }
    //            System.out.println("Task: " + task.getName() + " ID=" + task.getID() + " Unique ID=" + task.getUniqueID() + " (Start Date=" + startDate + " Finish Date=" + finishDate + " Duration=" + duration + " Actual Duration=" + actualDuration + " Baseline Duration=" + baselineDuration + " Cost=" + task.getCost() + " Outline Level=" + task.getOutlineLevel() + " Outline Number=" + task.getOutlineNumber() + " Recurring=" + task.getRecurring() + ")");
//                System.out.println("Task: " + task.getName() + " ID=" + task.getID() + " Unique ID=" + task.getUniqueID() + " (Start Date=" + startDate + " Finish Date=" + finishDate + " Duration=" + duration + " Actual Duration=" + actualDuration + " Baseline Duration=" + baselineDuration + " Cost=" + task.getCost() + " Outline Level=" + task.getOutlineLevel() + " Outline Number=" + task.getOutlineNumber() + " Recurring=" + task.getRecurring() + " Number(1)=" + task.getNumber(1)  + " Number(2)=" + task.getNumber(2) + ")");
            } //endfor
//            file.delete();
        }
        catch(Exception ex1) {
            new Notification("Error al intentar cargar las tareas del archivo.",
            ex1.getMessage(),
            Notification.Type.ERROR_MESSAGE)
            .show(Page.getCurrent());
            ex1.printStackTrace();
        }
    }

}
