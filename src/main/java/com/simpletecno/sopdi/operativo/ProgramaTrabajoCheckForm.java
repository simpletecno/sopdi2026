package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.simpletecno.sopdi.recursoshumanos.PlanRHShow;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.*;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author user
 */
public class ProgramaTrabajoCheckForm extends Window {

    EnvironmentVars environmentsVars;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;
    String queryString = "";

    VerticalLayout mainForm;

    TextField centroCostoTxt;
    TextField idexTxt;
    TextField descripcionTxt;
    TextField fechaInicioTxt;
    TextField fechaFinTxt;
    TextArea  instruccionesTxtA;
    TextField fechaInicioRealTxt;
    TextField fechaFinRealTxt;

    Button inicioBtn;
    Button finBtn;

    Button aceptarInicioBtn;
    Button aceptarFinBtn;

    Button idexSupervisarBtn;
    Button validarTokenBtn;
    Button calificarBtn;

    Button salirBtn;

    boolean esSupervisor;

    String idPlanTrabajoIdex;
    String centroCosto;
    String idex;
    String descripcion;
    String fechaInicio;
    String fechaFin;
    String fechaInicioReal;
    String fechaFinReal;
    String instrucciones;
    String codigoEstilo;
    String idNivel;
    String codigoPlanos;

    public ProgramaTrabajoCheckForm(
            String idPlanTrabajoIdex,
            String centroCosto,
            String idex,
            String descripcion,
            String fechaInicio,
            String fechaFin,
            String fechaInicioReal,
            String fechaFinReal,
            String instrucciones,
            String codigoPlanos,
            String codigoEstilo,
            String idNivel
            ) {
        this.mainUI = UI.getCurrent();
        this.idPlanTrabajoIdex = idPlanTrabajoIdex;
        this.centroCosto = centroCosto;
        this.idex = idex;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaInicioReal = fechaInicioReal;
        this.fechaFinReal = fechaFinReal;
        this.instrucciones = instrucciones;
        this.codigoPlanos =  codigoPlanos;
        this.codigoEstilo = codigoEstilo;
        this.idNivel = idNivel;

        setSizeFull();
        setResponsive(true);
        setModal(true);

        environmentsVars = new EnvironmentVars();

        mainForm = new VerticalLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");

        Label titleLbl = new Label("DETALLE DE TAREA");
            titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        esSupervisor = ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("SUPERVISOR");

        crearComponents();

        setContent(mainForm);

        llenarDatosTarea();
    }

    public void crearComponents() {

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        horizontalLayout.setWidth("100%");

        FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(true);
        formLayout.setWidth("100%");
        formLayout.setResponsive(true);

        horizontalLayout.addComponent(formLayout);
        horizontalLayout.setComponentAlignment(formLayout, Alignment.MIDDLE_CENTER);

        centroCostoTxt = new TextField("Centro Costo");
        centroCostoTxt.setValue(centroCosto);
        centroCostoTxt.setWidth("100%");
        centroCostoTxt.setReadOnly(true);

        idexTxt = new TextField("Idex");
        idexTxt.setValue(idex);
        idexTxt.setWidth("100%");
        idexTxt.setReadOnly(true);

        descripcionTxt = new TextField("Descripción");
        descripcionTxt.setWidth("100%");
        descripcionTxt.setValue(descripcion);
        descripcionTxt.setReadOnly(true);

        fechaInicioTxt = new TextField("Inicio");
        fechaInicioTxt.setWidth("100%");
        fechaInicioTxt.setValue(fechaInicio);
        fechaInicioTxt.setReadOnly(true);

        fechaFinTxt = new TextField("Fin");
        fechaFinTxt.setWidth("100%");
        fechaFinTxt.setValue(fechaFin);
        fechaFinTxt.setReadOnly(true);

        instruccionesTxtA = new TextArea("Instrucciones");
        instruccionesTxtA.setWidth("100%");
        instruccionesTxtA.setHeight("7em");
        instruccionesTxtA.setValue(instrucciones);
        instruccionesTxtA.setReadOnly(true);

        fechaInicioRealTxt = new TextField("Inicio Real");
        fechaInicioRealTxt.setWidth("100%");
        fechaInicioRealTxt.setReadOnly(true);

        fechaFinRealTxt = new TextField("Fin Real");
        fechaFinRealTxt.setWidth("100%");
        fechaFinRealTxt.setReadOnly(true);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidth("100%");

        formLayout.addComponents(centroCostoTxt, idexTxt, descripcionTxt, fechaInicioTxt, fechaFinTxt);
        formLayout.addComponent(instruccionesTxtA);
        formLayout.addComponents(fechaInicioRealTxt, fechaFinRealTxt, buttonsLayout);

        inicioBtn = new Button("Inicio");
        inicioBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        inicioBtn.setIcon(FontAwesome.PLAY);
        inicioBtn.setImmediate(true);
        inicioBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                if(verificacionBloqueada()) {
                    Notification.show("La verificación de tareas está bloqueada!", Notification.Type.WARNING_MESSAGE);
                    return;
                }

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de INICIAR?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    String queryString;

                                    queryString = "UPDATE project_tarea, project ";
                                    queryString += " SET project_tarea.FechaRealInicio = current_date";
                                    queryString += ",project_tarea.Estatus = 'EN EJECUCION'";
                                    queryString += " WHERE project_tarea.Idex = '" + idex + "'";
                                    queryString += " AND project_tarea.IdProject = project.Id";
                                    queryString += " AND project.Estatus = 'ACTIVO'";

                                    //System.out.println("queryString="+queryString);

                                    try {
                                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                        stQuery.executeUpdate(queryString);

                                        queryString = "UPDATE plan_trabajo_idex ";
                                        queryString += " SET FechaInicioSegunJefe = '" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
                                        queryString += " WHERE Id = " + idPlanTrabajoIdex;

                                        stQuery.executeUpdate(queryString);

                                        ((ProgramaTrabajoCheckView) (mainUI.getNavigator().getCurrentView())).idexContainer.getContainerProperty(((ProgramaTrabajoCheckView) (mainUI.getNavigator().getCurrentView())).idexGrid.getSelectedRow(), ProgramaTrabajoCheckView.FECHAINICIOREAL_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(new java.util.Date()));

                                        Notification.show("TAREA INICIADA!", Notification.Type.HUMANIZED_MESSAGE);

                                        close();
                                    } catch (Exception ex) {
                                        Notification.show("Error al actualizar fechas del IDEX de este PROGRAMA de trabajo : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        });
        inicioBtn.setVisible(false);

        finBtn = new Button("Fin");
        finBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        finBtn.setIcon(FontAwesome.STOP);
        finBtn.setImmediate(true);
        finBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                if(verificacionBloqueada()) {
                    Notification.show("La verificación de tareas está bloqueada!", Notification.Type.WARNING_MESSAGE);
                    return;
                }

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de FINALIZAR?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    String queryString;

                                    queryString = "UPDATE project_tarea, project ";
                                    queryString += " SET project_tarea.FechaRealFin = current_date";
                                    queryString += ",project_tarea.Estatus = 'EJECUTADA'";
                                    queryString += " WHERE project_tarea.Idex = '" + idex + "'";
                                    queryString += " AND project_tarea.IdProject = project.Id ";
                                    queryString += " AND project.Estatus = 'ACTIVO'";

                                    //System.out.println("queryString="+queryString);

                                    try {
                                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                        stQuery.executeUpdate(queryString);

                                        queryString = "UPDATE plan_trabajo_idex ";
                                        queryString += " SET FechaFinSegunJefe = '" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
                                        queryString += " WHERE Id = " + idPlanTrabajoIdex;

                                        stQuery.executeUpdate(queryString);

                                        ((ProgramaTrabajoCheckView) (mainUI.getNavigator().getCurrentView())).fillIdexGrid();

                                        Notification.show("TAREA EJECUTADA!", Notification.Type.HUMANIZED_MESSAGE);

                                        ProgramaTrabajoCalificarForm planTrabajoCalificarForm = new ProgramaTrabajoCalificarForm(
                                                "PERSONAL",
                                                ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("JEFE"),
                                                ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("MAESTRO"),
                                                esSupervisor,
                                                idPlanTrabajoIdex,
                                                centroCosto,
                                                idex,
                                                descripcion
                                        );
                                        planTrabajoCalificarForm.center();
                                        planTrabajoCalificarForm.setModal(true);
                                        planTrabajoCalificarForm.setCaption("CALIFICACIONES");
                                        UI.getCurrent().addWindow(planTrabajoCalificarForm);

                                        close();

                                    } catch (Exception ex) {
                                        Notification.show("Error al actualizar fechas del IDEX de este grupo de trabajo : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        });
        finBtn.setVisible(false);

        aceptarInicioBtn = new Button("Aceptar Inicio");
        aceptarInicioBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        aceptarInicioBtn.setIcon(FontAwesome.CHECK);
        aceptarInicioBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                if(verificacionBloqueada()) {
                    Notification.show("La verificación de tareas está bloqueada!", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                ProgramaTrabajoFechasForm planTrabajoFechasForm =
                        new ProgramaTrabajoFechasForm(
                                idPlanTrabajoIdex,
                                centroCosto,
                                idex, descripcion,
                                fechaInicioRealTxt.getValue(),
                                fechaFinRealTxt.getValue(),
                                esSupervisor,
                                true
                        );
                planTrabajoFechasForm.center();
                planTrabajoFechasForm.setModal(true);
                UI.getCurrent().addWindow(planTrabajoFechasForm);

                close();
            }

        });
        aceptarInicioBtn.setVisible(false);

        aceptarFinBtn = new Button("Aceptar Fin");
        aceptarFinBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        aceptarFinBtn.setIcon(FontAwesome.CHECK);
        aceptarFinBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                if(verificacionBloqueada()) {
                    Notification.show("La verificación de tareas está bloqueada!", Notification.Type.WARNING_MESSAGE);
                    return;
                }

                ProgramaTrabajoFechasForm planTrabajoFechasFormFIN =
                        new ProgramaTrabajoFechasForm(
                                idPlanTrabajoIdex,
                                centroCosto,
                                idex,
                                descripcion,
                                fechaInicioRealTxt.getValue(),
                                fechaFinRealTxt.getValue(),
                                esSupervisor,
                                false
                        );
                planTrabajoFechasFormFIN.center();
                planTrabajoFechasFormFIN.setModal(true);
                UI.getCurrent(). addWindow(planTrabajoFechasFormFIN);

                close();

            }
        });
        aceptarFinBtn.setVisible(false);

        idexSupervisarBtn = new Button("Supervisar");
        idexSupervisarBtn.setIcon(FontAwesome.EYE);
        idexSupervisarBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        idexSupervisarBtn.setDescription("SELECCIONE UNA TAREA PARA SUPERVISAR");
        idexSupervisarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ProgramaTrabajoCalificarForm planTrabajoCalificarForm = new ProgramaTrabajoCalificarForm(
                        "SUPERVISAR",
                        ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("JEFE"),
                        ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("MAESTRO"),
                        esSupervisor,
                        idPlanTrabajoIdex,
                        centroCosto,
                        idex,
                        descripcion
                );
                planTrabajoCalificarForm.center();
                planTrabajoCalificarForm.setModal(true);
                planTrabajoCalificarForm.setCaption("SUPERVISIONES");
                UI.getCurrent().addWindow(planTrabajoCalificarForm);

                close();
            }
        });
        idexSupervisarBtn.setVisible(false);

        validarTokenBtn = new Button("Validar TOKEN");
        validarTokenBtn.setIcon(FontAwesome.EYE);
        validarTokenBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        validarTokenBtn.setDescription("SELECCIONE UNA TAREA PARA SUPERVISAR");
        validarTokenBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ValidarTokenForm validarTokenForm = new ValidarTokenForm(true);
                validarTokenForm.setOrigen(idPlanTrabajoIdex + " IDCC=" + centroCosto + " IDEX=" + idex);
                UI.getCurrent().addWindow(validarTokenForm);
                validarTokenForm.center();
                close();
            }
        });
        validarTokenBtn.setVisible(false);

        calificarBtn = new Button("Calificar");
        calificarBtn.setIcon(FontAwesome.CHECK_CIRCLE);
        calificarBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        calificarBtn.setDescription("SELECCIONE UNA TAREA PARA CALIFICAR");
        calificarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ProgramaTrabajoCalificarForm planTrabajoCalificarForm = new ProgramaTrabajoCalificarForm(
                        "CALIDAD",
                        ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("JEFE"),
                        ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("MAESTRO"),
                        esSupervisor,
                        idPlanTrabajoIdex,
                        centroCosto,
                        idex,
                        descripcion
                );
                planTrabajoCalificarForm.center();
                planTrabajoCalificarForm.setModal(true);
                planTrabajoCalificarForm.setCaption("CALIFICACIONES");
                UI.getCurrent().addWindow(planTrabajoCalificarForm);

                close();
            }
        });
        calificarBtn.setVisible(false);

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        HorizontalLayout buttonsLayout2 = new HorizontalLayout();
        buttonsLayout2.setResponsive(true);
        buttonsLayout2.setSpacing(true);
        buttonsLayout2.setWidth("100%");

        buttonsLayout2.addComponents(inicioBtn, finBtn, aceptarInicioBtn, aceptarFinBtn, idexSupervisarBtn, validarTokenBtn, calificarBtn, salirBtn);
        buttonsLayout2.setComponentAlignment(inicioBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout2.setComponentAlignment(finBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout2.setComponentAlignment(aceptarInicioBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout2.setComponentAlignment(aceptarFinBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout2.setComponentAlignment(validarTokenBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout2.setComponentAlignment(calificarBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout2.setComponentAlignment(salirBtn, Alignment.BOTTOM_RIGHT);

        mainForm.addComponent(horizontalLayout);
        mainForm.setComponentAlignment(horizontalLayout, Alignment.TOP_CENTER);

        mainForm.addComponent(buttonsLayout2);
        mainForm.setComponentAlignment(buttonsLayout2, Alignment.MIDDLE_CENTER);
    }

    private boolean verificacionBloqueada() {
        boolean bloqueada = false;

        queryString = "SELECT VerificacionBloqueada ";
        queryString += " FROM proyecto ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI)UI.getCurrent()).sessionInformation.getStrProjectId();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                bloqueada = (rsRecords.getInt("VerificacionBloqueada") == 1 ? true : false);
            }
        }
        catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PROYECTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR PROYECTO: " + ex1.getMessage());
            ex1.printStackTrace();
        }
        return bloqueada;
    }

    private void llenarDatosTarea() {
        aceptarInicioBtn.setVisible(false);
        aceptarFinBtn.setVisible(false);

        fechaInicioRealTxt.setReadOnly(false);
        fechaInicioRealTxt.setValue(fechaInicioReal);
        fechaInicioRealTxt.setReadOnly(true);

        fechaFinRealTxt.setReadOnly(false);
        fechaFinRealTxt.setValue(fechaFinReal);
        fechaFinRealTxt.setReadOnly(true);


        queryString = "SELECT * ";
        queryString += " FROM plan_trabajo_idex ";
        queryString += " WHERE Id = " + idPlanTrabajoIdex;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                if (((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("JEFE")) {
                    if (rsRecords.getObject("FechaInicioSegunJefe") != null) {
                        if (rsRecords.getObject("FechaFinSegunJefe") == null) {
                            finBtn.setVisible(true);
                        }
                    } else {
                            inicioBtn.setVisible(true);
                    }
                    validarTokenBtn.setVisible(true);
                } else if (((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("MAESTRO")){ // es maestro o supervisor o administrador,  no hace falta ya el flujo de quien aceptar el inicio o fin...
                    if (rsRecords.getObject("FechaInicioSegunJefe") != null) {
                        idexSupervisarBtn.setVisible(true);
                    }
                    if (rsRecords.getObject("FechaInicioSegunMaestro") == null) {
                        aceptarInicioBtn.setVisible(true);  //habilitado para ambos
                    }
                    if (rsRecords.getObject("FechaFinSegunJefe") != null) {
                        if (rsRecords.getObject("FechaInicioSegunMaestro") != null) {
                            if (rsRecords.getObject("FechaFinSegunMaestro") == null) {
                                aceptarFinBtn.setVisible(true); //habilitado para ambos,  al aceptar  se modifican ambos campos, los del maestro y los del supervisor....
                            }
                            else {
                                idexSupervisarBtn.setVisible(false); //ya se ha aceptado la fecha fin,  ya no procede supervisar
                                activarBotonCalificarCalidadYRH();
                            }
                        }
                    }
                } else if(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("SUPERVISOR")){
                    if (rsRecords.getObject("FechaInicioSegunJefe") != null) {
                        idexSupervisarBtn.setVisible(true);
                    }
                    if (rsRecords.getObject("FechaInicioSegunSupervisor") == null) {
                        aceptarInicioBtn.setVisible(true);  //habilitado para ambos
                    }
                    if (rsRecords.getObject("FechaFinSegunJefe") != null) {
                        if (rsRecords.getObject("FechaInicioSegunSupervisor") != null) {
                            if (rsRecords.getObject("FechaFinSegunSupervisor") == null) {
                                aceptarFinBtn.setVisible(true); //habilitado para ambos,  al aceptar  se modifican ambos campos, los del maestro y los del supervisor....
                            }
                            else {
                                idexSupervisarBtn.setVisible(false); //ya se ha aceptado la fecha fin,  ya no procede supervisar
                                activarBotonCalificarCalidadYRH();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PROGRAMA DE TRABAJO (IDEX) CHECK", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR PROGRAMA DE TRABAJO (IDEX) CHECK : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        createPDFLinks();

        createRHGrid();
    }

    private void activarBotonCalificarCalidadYRH() {
        calificarBtn.setVisible(false);

        queryString = "SELECT * ";
        queryString += " FROM plan_trabajo_idex_ca ";
        queryString += " WHERE IdPlanTrabajoIdex = " + idPlanTrabajoIdex;
        queryString += " AND IdUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
//System.out.println("\nquery encontrar calificar calidad = " + queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            calificarBtn.setVisible(!rsRecords.next());
        }
        catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CALIFICACION DE TRABAJO (IDEX) CHECK", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CALIFICACION DE TRABAJO (IDEX) CHECK : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void createPDFLinks() {

        VerticalLayout planoLayout = new VerticalLayout();
        planoLayout.setWidth("100%");
        planoLayout.setHeight("100%");
        planoLayout.addStyleName("rcorners3");

        Label h1 = new Label("Planos");
        h1.addStyleName("h2");

        planoLayout.addComponent(h1);

        EnvironmentVars environmentsVars = new EnvironmentVars();

        String filePath = "";
        String findCode = "";

        try {

            filePath = environmentsVars.getDtePath() + "planos/" + ((SopdiUI) mainUI).sessionInformation.getStrProjectId() + "/";
            List<File> filesInFolder = Files.walk(Paths.get(filePath))
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".pdf"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
System.out.println("codigoPlanos=" + codigoPlanos);
            String[] listacodigo = codigoPlanos.split(";");
            for(String codPlano : listacodigo) {

                findCode = "";

                queryString = "SELECT Pla.CodigoPlano, Nom.Nombre ";
                queryString += " FROM planos Pla";
                queryString += " INNER JOIN planos_nombre Nom ON Nom.Id = Pla.IdNombre";
                queryString += " INNER JOIN planos_estilo Est ON Est.Codigo = Pla.CodigoEstilo";
                queryString += " WHERE Pla.IdCentroCosto = '" + centroCosto + "'";
                queryString += " AND Est.Codigo = '" + codigoEstilo + "'";
                queryString += " AND Pla.CodigoPlano LIKE '%" + codPlano + "%'";
                queryString += " AND Pla.EsUltimaVersion = 'SI'";
System.out.println("queryStringPlanos=" + queryString);
                try {
                    rsRecords = stQuery.executeQuery(queryString);
                    if(rsRecords.next()) {
                        findCode = rsRecords.getString("CodigoPlano") + ".pdf";
                    }
System.out.println("findCode="+findCode);
                }
                catch (Exception ex1) {
                    Notification.show("ERROR DEL SISTEMA AL BUSCAR PLANOS DE TRABAJO (IDEX) CHECK", Notification.Type.ERROR_MESSAGE);
                    System.out.println("ERROR AL INTENTAR BUSCAR PLANOS DE TRABAJO (IDEX) CHECK : " + ex1.getMessage());
                    ex1.printStackTrace();
                    return;
                }

                for(File pdfFile : filesInFolder) {
                    System.out.println("fileName = " + pdfFile.getName() + " " + pdfFile.getAbsolutePath());
                    System.out.println("pdfFile.getName().matches(findCode)=" + pdfFile.getName().matches(findCode));

                    if (pdfFile.getName().equals(findCode)) {
                        Button viewPdfBtn = new Button(rsRecords.getString("Nombre"));
                        viewPdfBtn.addStyleName(ValoTheme.BUTTON_LINK);
                        viewPdfBtn.addClickListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                PlanoShowPDF planoShowPdf = new PlanoShowPDF(pdfFile.getAbsolutePath());
                                mainUI.addWindow(planoShowPdf);
                                planoShowPdf.center();
                            }
                        });
                        planoLayout.addComponent(viewPdfBtn);
                    }
                } //endfor filesInfolder
            } //endfor codiosPalnos[;]

            mainForm.addComponent(planoLayout);

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de pdf planos: " + ex.getMessage());
            Notification.show("Error al intentar leer registros de pdf de planos..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void createRHGrid() {

        IndexedContainer rhContainer = new IndexedContainer();
        rhContainer.addContainerProperty("id", String.class, "");
        rhContainer.addContainerProperty("nombre", String.class, "");
        rhContainer.addContainerProperty("cargo", String.class, "");
        rhContainer.addContainerProperty("esJefe", String.class, "");

        Grid rhGrid = new Grid("", rhContainer);
        rhGrid.setWidth("100%");
        rhGrid.setImmediate(true);
        rhGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
//        rhGrid.setDescription("Seleccione un registro.");
        rhGrid.setHeightMode(HeightMode.ROW);
        rhGrid.setHeightByRows(15);
        rhGrid.setResponsive(true);
        rhGrid.setEditorBuffered(false);
        rhGrid.setSizeFull();

        rhGrid.getColumn("id").setExpandRatio(1);
        rhGrid.getColumn("nombre").setExpandRatio(4);
        rhGrid.getColumn("cargo").setExpandRatio(1);
        rhGrid.getColumn("esJefe").setExpandRatio(1);

        mainForm.addComponent(rhGrid);
        mainForm.setComponentAlignment(rhGrid, Alignment.MIDDLE_CENTER);

        String queryString;

        queryString = "SELECT *";
        queryString += " FROM plan_trabajo_idex_rh ";
        queryString += " INNER JOIN proveedor ON proveedor.IdProveedor = plan_trabajo_idex_rh.IdEmpleado";
        queryString += " WHERE plan_trabajo_idex_rh.IdPlanTrabajoIdex = " + idPlanTrabajoIdex;
        queryString += " AND proveedor.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY proveedor.Nombre";

        int rh1 = 0, rh2 = 0;

        try {

            Statement stQuery;
            ResultSet rsRecords;

            stQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // POR CADA EMPLEADO RH1 Y RH2
                do {
                    if (rsRecords.getString("Cargo").equals("RH1")) {
                        rh1++;
                    }
                    if (rsRecords.getString("Cargo").equals("RH2")) {
                        rh2++;
                    }

                    Object itemId;
                    itemId = rhContainer.addItem();

                    rhContainer.getContainerProperty(itemId, "id").setValue(rsRecords.getString("IdEmpleado"));
                    rhContainer.getContainerProperty(itemId, "nombre").setValue(rsRecords.getString("Nombre"));
                    rhContainer.getContainerProperty(itemId, "cargo").setValue(rsRecords.getString("Cargo"));
                    rhContainer.getContainerProperty(itemId, "esJefe").setValue(rsRecords.getString("EsJefe"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(PlanRHShow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de empleados : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de RH..!", Notification.Type.ERROR_MESSAGE);
        }
    }

}
