package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class PlanoForm extends Window {

    String idPlano;
    String codigo;
    String version;

    UI mainUI;
    Statement stQuery = null;
    Statement stQuery1 = null;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords = null;
    ResultSet rsRecords1 = null;
    String queryString = "";

    VerticalLayout mainForm;

    ComboBox projectCbx;
    ComboBox centroCostoCbx;
    ComboBox categoriaCbx;
    ComboBox estiloCbx;
    ComboBox nombreCbx;
    ComboBox tipoCbx;
    ComboBox nivelCbx;
    TextField archivoTxt;
    TextField descripcionTxt;
    TextField codigoTxt;

    MultiFileUpload singleUpload;
    EnvironmentVars environmentsVars;
    StreamResource pdfStreamResource = null;

    Button guardarBtn;
    Button salirBtn;

    boolean archivoCargado = false;

    public PlanoForm(String idPlano) {
        this.mainUI = UI.getCurrent();
        this.idPlano = idPlano;
        setWidth("50%");
        setHeight("70%");
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

        Label titleLbl;

        if (this.idPlano.trim().isEmpty()) {
            titleLbl = new Label("NUEVO PLANO");
            titleLbl.setSizeUndefined();
            titleLbl.addStyleName(Runo.LABEL_H2);
        } else {
            titleLbl = new Label("EDITAR PLANO");
            titleLbl.setSizeUndefined();
            titleLbl.addStyleName(Runo.LABEL_H2);
        }

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        crearComponents();

        setContent(mainForm);

    }

    public void crearComponents() {

        HorizontalLayout layotsHorizontal = new HorizontalLayout();
        layotsHorizontal.setSpacing(true);
        layotsHorizontal.setWidth("100%");

        FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(true);
        formLayout.setWidth("100%");
        formLayout.setResponsive(true);

        projectCbx = new ComboBox("Project : ");
        projectCbx.setSizeFull();
        projectCbx.addContainerProperty("numero", String.class, "");
        projectCbx.setFilteringMode(FilteringMode.CONTAINS);
        projectCbx.setInvalidAllowed(false);
        projectCbx.setNullSelectionAllowed(false);
        projectCbx.setNewItemsAllowed(false);
        projectCbx.addValueChangeListener(event -> {
            llenarComboCentroCosto();
        });
        llenarComboProject();

        centroCostoCbx = new ComboBox("Centro de Costo : ");
        centroCostoCbx.setSizeFull();
        centroCostoCbx.setImmediate(true);
        centroCostoCbx.setNullSelectionAllowed(false);
        centroCostoCbx.setNewItemsAllowed(false);
        centroCostoCbx.setInvalidAllowed(false);
        centroCostoCbx.addContainerProperty("CodigoEstilo", String.class, "");
        centroCostoCbx.addValueChangeListener(event -> {
            llenarComboEstilo();
            setCodigo();
        });
        llenarComboCentroCosto();

        estiloCbx = new ComboBox("Estilo : ");
        estiloCbx.setSizeFull();
        estiloCbx.setImmediate(true);
        estiloCbx.setNullSelectionAllowed(false);
        estiloCbx.setInvalidAllowed(false);
        estiloCbx.setNewItemsAllowed(false);
        estiloCbx.addContainerProperty("id", String.class, "");
        estiloCbx.addValueChangeListener(event -> {
            if (centroCostoCbx.getValue() != null) {
                setCodigo();
            }
        });

        categoriaCbx = new ComboBox("Categoría : ");
        categoriaCbx.setSizeFull();
        categoriaCbx.setImmediate(true);
        categoriaCbx.setNullSelectionAllowed(false);
        categoriaCbx.setInvalidAllowed(false);
        categoriaCbx.setNewItemsAllowed(false);
        categoriaCbx.addContainerProperty("id", String.class, "");
        categoriaCbx.addValueChangeListener(event -> {
            llenarComboNombre();
            setCodigo();
        });
        llenarComboCategoria();

        nombreCbx = new ComboBox("Nombre : ");
        nombreCbx.setSizeFull();
        nombreCbx.setImmediate(true);
        nombreCbx.setNewItemsAllowed(false);
        nombreCbx.setInvalidAllowed(false);
        nombreCbx.setNullSelectionAllowed(false);
        nombreCbx.addContainerProperty("id", String.class, "");
        nombreCbx.addValueChangeListener(event -> {
            setCodigo();
        });

        tipoCbx = new ComboBox("Tipo : ");
        tipoCbx.setSizeFull();
        tipoCbx.setImmediate(true);
        tipoCbx.setNewItemsAllowed(false);
        tipoCbx.setInvalidAllowed(false);
        tipoCbx.setNullSelectionAllowed(false);
        tipoCbx.addContainerProperty("id", String.class, "");
        tipoCbx.addValueChangeListener(event -> {
            setCodigo();
        });
        llenarComboTipo();

        nivelCbx = new ComboBox("Nivel : ");
        nivelCbx.setSizeFull();
        nivelCbx.setImmediate(true);
        nivelCbx.setNullSelectionAllowed(false);
        nivelCbx.setNewItemsAllowed(false);
        nivelCbx.setInvalidAllowed(false);
        nivelCbx.addItem("01");
        nivelCbx.addItem("02");
        nivelCbx.addItem("03");
        nivelCbx.addItem("04");
        nivelCbx.addItem("05");
        nivelCbx.addItem("06");
        nivelCbx.addItem("07");
        nivelCbx.addItem("08");
        nivelCbx.addItem("09");
        nivelCbx.addItem("10");
        nivelCbx.addItem("11");
        nivelCbx.addItem("12");
        nivelCbx.addItem("13");
        nivelCbx.addItem("14");
        nivelCbx.addItem("15");
        nivelCbx.addItem("16");
        nivelCbx.addItem("17");
        nivelCbx.addItem("18");
        nivelCbx.addItem("19");
        nivelCbx.addItem("20");
        nivelCbx.addValueChangeListener(event -> {
            setCodigo();
        });

        archivoTxt =  new TextField();
        archivoTxt.setValue("...");
        archivoTxt.setSizeFull();
        archivoTxt.setReadOnly(true);

        descripcionTxt =  new TextField("Descripción : ");
        descripcionTxt.setSizeFull();

        codigoTxt =  new TextField("Código : ");
        codigoTxt.setSizeFull();
        codigoTxt.setReadOnly(true);

//        formLayout.addComponent(projectCbx);
        formLayout.addComponent(centroCostoCbx);
        formLayout.addComponent(estiloCbx);
        formLayout.addComponent(categoriaCbx);
        formLayout.addComponent(nombreCbx);
        formLayout.addComponent(tipoCbx);
        formLayout.addComponent(nivelCbx);
        formLayout.addComponent(descripcionTxt);
        formLayout.addComponent(codigoTxt);
        formLayout.addComponent(archivoTxt);

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertarPlano();
            }
        });

        UploadStateWindow window = new UploadStateWindow();

        UploadFinishedHandler handler;
        handler = (stream, fileName, mimeType, length) -> {

//            if (projectCbx.getValue() == null) {
//                Notification.show("Por favor ingrese el project.", Notification.Type.WARNING_MESSAGE);
//                projectCbx.focus();
//                return;
//            }
            if (centroCostoCbx.getValue() == null) {
                Notification.show("Por favor ingrese el centro de costo.", Notification.Type.WARNING_MESSAGE);
                centroCostoCbx.focus();
                return;
            }
            if (estiloCbx.getValue() == null) {
                Notification.show("Por favor seleccione el estilo.", Notification.Type.WARNING_MESSAGE);
                estiloCbx.focus();
                return;
            }
            if (categoriaCbx.getValue() == null) {
                Notification.show("Por favor seleccione la categoría.", Notification.Type.WARNING_MESSAGE);
                categoriaCbx.focus();
                return;
            }
            if (nombreCbx.getValue() == null) {
                Notification.show("Por favor seleccione el nombre.", Notification.Type.WARNING_MESSAGE);
                nombreCbx.focus();
                return;
            }
            if (tipoCbx.getValue() == null) {
                Notification.show("Por favor seleccione el tipo.", Notification.Type.WARNING_MESSAGE);
                tipoCbx.focus();
                return;
            }

            File targetFile;

            try {
                if (mimeType.contains("png") || mimeType.contains("jpeg") || mimeType.contains("jpg") || mimeType.contains("pdf")) {
                    System.out.println("\nfileName=" + fileName);
                    System.out.println("length=" + stream.available());
                    System.out.println("mimeType=" + mimeType);
                    long fileSize = stream.available();
                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);

                    String filePath = environmentsVars.getDtePath() + "planos/" + ((SopdiUI) mainUI).sessionInformation.getStrProjectId() + "/";

                    new File(filePath).mkdirs();

                    fileName = filePath;
//                    fileName += String.valueOf(projectCbx.getContainerProperty(projectCbx.getValue(), "numero").getValue()).trim().length() < 2 ? ("0"+projectCbx.getContainerProperty(projectCbx.getValue(), "numero").getValue()) : String.valueOf(projectCbx.getContainerProperty(projectCbx.getValue(), "numero").getValue());
                    fileName += codigo;
                    fileName += mimeType.contains("png") ? ".png" : ".pdf";

                    new File(filePath).mkdirs();

                    targetFile = new File(fileName);

                    archivoTxt.setReadOnly(false);
                    archivoTxt.setValue(targetFile.getName());
                    archivoTxt.setReadOnly(true);

                    System.out.println("\ntargetFile = " + fileName);
                    OutputStream outStream = new FileOutputStream(targetFile);
                    outStream.write(buffer);
                    outStream.close();

                    stream.close();

// /Users/joseaguirre/temp/planos/1/1005_1.pdf
// /Users/joseaguirre/temp/planos/1/1005_1.pdf

                    pdfStreamResource = null;

                    if (buffer != null) {
                        pdfStreamResource = new StreamResource(
                                new StreamResource.StreamSource() {
                                    public InputStream getStream() {
                                        return new ByteArrayInputStream(buffer);
                                    }
                                }, String.valueOf(System.currentTimeMillis())
                        );
                    }

                    recibirParametrosImagen(targetFile.getName(), mimeType, fileSize);

                    Notification.show("Archivo cargado con exito!!..", Notification.Type.TRAY_NOTIFICATION);
                    archivoCargado = true;

                    window.close();
                } else {
                    archivoCargado = false;
                    Notification notif = new Notification("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PDF'",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                    return;
                }
            } catch (IOException fIoEx) {
                fIoEx.printStackTrace();
                Notification.show("Error al cargar el archivo PDF!", Notification.Type.ERROR_MESSAGE);
                return;
            }
        };

        singleUpload = new MultiFileUpload(handler, window, false);
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar archivo", "");

//        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
//        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.PDF')");

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setResponsive(true);
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("80%");

        buttonsLayout.addComponents(salirBtn, singleUpload, guardarBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_RIGHT);

        layotsHorizontal.addComponent(formLayout);
        layotsHorizontal.setComponentAlignment(formLayout, Alignment.MIDDLE_CENTER);

        mainForm.addComponent(layotsHorizontal);
        mainForm.setComponentAlignment(layotsHorizontal, Alignment.TOP_CENTER);

        mainForm.addComponent(buttonsLayout);
        mainForm.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_CENTER);
    }

    private void llenarComboProject() {
        String queryString = "";

        queryString = "SELECT *";
        queryString += " FROM  project";
        queryString += " WHERE Estatus = 'ACTIVO'";
        queryString += " ORDER BY Numero";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    Utileria utileria = new Utileria();

                    projectCbx.addItem(rsRecords.getInt("Id"));
                    projectCbx.setItemCaption(rsRecords.getInt("Id"),
                            rsRecords.getString("Numero")
                                    + " " + rsRecords.getString("Descripcion")
                                    + " " + rsRecords.getString("CreadoFecha"));
                    projectCbx.getContainerProperty(rsRecords.getInt("Id"), "numero").setValue(rsRecords.getString("Numero"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de projects : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de projects..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void llenarComboCentroCosto() {

        queryString = "SELECT * ";
        queryString += " FROM centro_costo ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND Inhabilitado = 0";
        queryString += " GROUP BY IdCentroCosto";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            centroCostoCbx.removeAllItems();

            while (rsRecords.next()) { //  encontrado
                centroCostoCbx.addItem(rsRecords.getString("CodigoCentroCosto"));
                centroCostoCbx.getContainerProperty(rsRecords.getString("CodigoCentroCosto"), "CodigoEstilo").setValue(rsRecords.getString("CodigoEstilo"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboEstilo() {

        if(centroCostoCbx.getValue() == null) {
            return;
        }

        queryString = "SELECT * ";
        queryString += " FROM planos_estilo ";
        queryString += " WHERE Codigo = " + centroCostoCbx.getContainerProperty(centroCostoCbx.getValue(), "CodigoEstilo").getValue();
        queryString += " ORDER BY Codigo";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            estiloCbx.removeAllItems();

            while (rsRecords.next()) { //  encontrado
                estiloCbx.addItem(rsRecords.getString("Codigo"));
                estiloCbx.setItemCaption(rsRecords.getString("Codigo"),
                        rsRecords.getString("Nombre")
                                + " m2 = " + rsRecords.getString("M2")
                                + " Inclinación = " + rsRecords.getString("Inclinacion")
                );
                estiloCbx.getContainerProperty(rsRecords.getString("Codigo"), "id").setValue(rsRecords.getString("Codigo"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR ESTILOS DE PLANOS", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE ESTILOS DE PLANOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboCategoria() {

        queryString = "SELECT * ";
        queryString += " FROM planos_categoria ";
        queryString += " ORDER BY Codigo";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            categoriaCbx.removeAllItems();

            while (rsRecords.next()) { //  encontrado
                categoriaCbx.addItem(rsRecords.getString("Codigo"));
                categoriaCbx.setItemCaption(rsRecords.getString("Codigo"), rsRecords.getString("Nombre"));
                categoriaCbx.getContainerProperty(rsRecords.getString("Codigo"), "id").setValue(rsRecords.getString("Id"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CATEGORIAS DE PLANOS", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CATEGORIAS DE PLANOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboNombre() {

        if(categoriaCbx.getValue() == null) {
            return;
        }

        queryString = "SELECT * ";
        queryString += " FROM planos_nombre ";
        queryString += " WHERE IdCategoria = " + categoriaCbx.getContainerProperty(categoriaCbx.getValue(), "id").getValue();
        queryString += " ORDER BY Codigo";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            nombreCbx.removeAllItems();

            while (rsRecords.next()) { //  encontrado
                nombreCbx.addItem(rsRecords.getString("Codigo"));
                nombreCbx.setItemCaption(rsRecords.getString("Codigo"), rsRecords.getString("Nombre"));
                nombreCbx.getContainerProperty(rsRecords.getString("Codigo"), "id").setValue(rsRecords.getString("Id"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR NOMBRES DE PLANOS", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE NOMBRES DE PLANOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboTipo() {

        queryString = "SELECT * ";
        queryString += " FROM planos_tipo ";
        queryString += " ORDER BY Codigo";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            tipoCbx.removeAllItems();

            while (rsRecords.next()) { //  encontrado
                tipoCbx.addItem(rsRecords.getString("Codigo"));
                tipoCbx.setItemCaption(rsRecords.getString("Codigo"), rsRecords.getString("Nombre"));
                tipoCbx.getContainerProperty(rsRecords.getString("Codigo"), "id").setValue(rsRecords.getString("Id"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR TIPOS DE PLANOS", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE TIPOS DE PLANOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        String parametro1 = fileName;
        String parametro2 = mimeType;
        long parametro3 = peso;
    }

    public void insertarPlano() {

        try {

//            if (projectCbx.getValue() == null) {
//                Notification.show("Por favor ingrese el project.", Notification.Type.WARNING_MESSAGE);
//                projectCbx.focus();
//                return;
//            }
            if (centroCostoCbx.getValue() == null) {
                Notification.show("Por favor ingrese el centro de costo.", Notification.Type.WARNING_MESSAGE);
                centroCostoCbx.focus();
                return;
            }
            if (estiloCbx.getValue() == null) {
                Notification.show("Por favor seleccione el estilo.", Notification.Type.WARNING_MESSAGE);
                estiloCbx.focus();
                return;
            }
            if (categoriaCbx.getValue() == null) {
                Notification.show("Por favor seleccione la categoría.", Notification.Type.WARNING_MESSAGE);
                categoriaCbx.focus();
                return;
            }
            if (nombreCbx.getValue() == null) {
                Notification.show("Por favor seleccione el nombre.", Notification.Type.WARNING_MESSAGE);
                nombreCbx.focus();
                return;
            }
            if (tipoCbx.getValue() == null) {
                Notification.show("Por favor seleccione el tipo.", Notification.Type.WARNING_MESSAGE);
                tipoCbx.focus();
                return;
            }
            if(archivoCargado == false) {
                Notification.show("Por favor cargue el archivo!.", Notification.Type.WARNING_MESSAGE);
                return;
            }

            if (idPlano.trim().isEmpty()) { /// NUEVO REGISTRO

                queryString = "INSERT INTO planos ";
                queryString += "(IdProyecto, IdCentroCosto, CodigoEstilo, IdCategoria, IdNombre, IdTipo, Nivel, Version, Descripcion, CodigoPlano, CreadoFechaYHora, CreadoUsuario)";
                queryString += " VALUES ";
                queryString += "(" + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
                queryString += ",'" + centroCostoCbx.getValue() + "'";
                queryString += "," + estiloCbx.getContainerProperty(estiloCbx.getValue(), "id").getValue();
                queryString += "," + categoriaCbx.getContainerProperty(categoriaCbx.getValue(), "id").getValue();
                queryString += "," + nombreCbx.getContainerProperty(nombreCbx.getValue(), "id").getValue();
                queryString += "," + tipoCbx.getContainerProperty(tipoCbx.getValue(), "id").getValue();
                queryString += "," + nivelCbx.getValue();
                queryString += "," + version;
                queryString += ",'" + descripcionTxt.getValue() + "'";
                queryString += ",'" + codigo + "'";
                queryString += ",current_timestamp";
                queryString += ","   + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ")";

                stPreparedQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                stPreparedQuery.executeUpdate();
                rsRecords = stPreparedQuery.getGeneratedKeys();
                rsRecords.next();

                idPlano = rsRecords.getString(1);

                queryString = "UPDATE planos SET ";
                queryString += " EsUltimaVersion = 'NO'";
                queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
                queryString += " AND IdCentroCosto = '" + centroCostoCbx.getValue() + "'";
                queryString += " AND CodigoEstilo = " + estiloCbx.getContainerProperty(estiloCbx.getValue(), "id").getValue();
                queryString += " AND IdCategoria = " + categoriaCbx.getContainerProperty(categoriaCbx.getValue(), "id").getValue();
                queryString += " AND IdNombre = " + nombreCbx.getContainerProperty(nombreCbx.getValue(), "id").getValue();
                queryString += " AND IdTipo = " + tipoCbx.getContainerProperty(tipoCbx.getValue(), "id").getValue();
                queryString += " AND Nivel = " + nivelCbx.getValue();
                queryString += " AND Id < " + idPlano;

                stQuery.executeUpdate(queryString);

                singleUpload.setEnabled(true);

            } else {

                queryString = "UPDATE planos SET ";
                queryString += "IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
                queryString += ",IdCentroCosto = '" + centroCostoCbx.getValue() + "'";
                queryString += ",CodigoEstilo = " + estiloCbx.getContainerProperty(estiloCbx.getValue(), "id").getValue();
                queryString += ",IdCategoria = " + categoriaCbx.getContainerProperty(categoriaCbx.getValue(), "id").getValue();
                queryString += ",IdNombre = " + nombreCbx.getContainerProperty(nombreCbx.getValue(), "id").getValue();
                queryString += ",IdTipo = " + tipoCbx.getContainerProperty(tipoCbx.getValue(), "id").getValue();
                queryString += ",Nivel = " + nivelCbx.getValue();
                queryString += ",Version = " + version;
                queryString += ",Descripcion = '" + descripcionTxt.getValue() + "'";
                queryString += ",CodigoPlano = '" + archivoTxt.getValue() + "'";
                queryString += " Where Id = " + idPlano;

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                queryString = "UPDATE planos SET ";
                queryString += " EsUltimaVersion = 'NO'";
                queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
                queryString += " AND IdCentroCosto = '" + centroCostoCbx.getValue() + "'";
                queryString += " AND CodigoEstilo = " + estiloCbx.getContainerProperty(estiloCbx.getValue(), "id").getValue();
                queryString += " AND IdCategoria = " + categoriaCbx.getContainerProperty(categoriaCbx.getValue(), "id").getValue();
                queryString += " AND IdNombre = " + nombreCbx.getContainerProperty(nombreCbx.getValue(), "id").getValue();
                queryString += " AND IdTipo = " + tipoCbx.getContainerProperty(tipoCbx.getValue(), "id").getValue();
                queryString += " AND Nivel = " + nivelCbx.getValue();
                queryString += " AND Id < " + idPlano;

                stQuery.executeUpdate(queryString);

                singleUpload.setEnabled(true);
            }

//            ((PlanosView) (mainUI.getNavigator().getCurrentView())).llenarTablaPlanos();

            Notification notif = new Notification("REGISTRO AGREGADO o MODIFICADO EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            close();

        } catch (SQLException ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error en base datos al intentar hacer update o insert en tabla planos : " + ex);
            ex.printStackTrace();
        }
    }

    private void setCodigo() {

        codigo = "";    // ORDEN:   CodigoEstilo + Categoria + Nombre + Tipo + Nivel + CentroCosto
                        // Ejemplo:      111          11
        version = "";

        if(estiloCbx.getValue() != null) {
            codigo = String.valueOf(estiloCbx.getValue());
            if(categoriaCbx.getValue() != null) {
                codigo += categoriaCbx.getValue();
                if (nombreCbx.getValue() != null) {
                    codigo += nombreCbx.getValue();//codigo de nombre
                    if (tipoCbx.getValue() != null) {
                        codigo += tipoCbx.getValue();
                        if (nivelCbx.getValue() != null) {
                            codigo += nivelCbx.getValue();
                            if (centroCostoCbx.getValue() != null) {
                                codigo += centroCostoCbx.getValue();

                                queryString = "Select IFNULL(Max(CodigoPlano), " + codigo + "00) CodigoPlanoMax ";
                                queryString += " From planos ";
                                queryString += " Where CodigoPlano like '" + codigo + "%'";

                                try {
                                    stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                    rsRecords1 = stQuery1.executeQuery(queryString);

                                    if (rsRecords1.next()) {
                                        //01234567890123456
                                        //11170101101102202

                                        String ultimoEncontado = rsRecords1.getString("CodigoPlanoMax").substring(14, 16);

                                        version = String.format("%02d", (Integer.valueOf(ultimoEncontado) + 1));

                                    } else {
                                        version += "01";
                                    }
                                } catch (Exception ex1) {
                                    Notification.show("ERROR DEL SISTEMA AL BUSCAR VERSIONES DE PLANOS", Notification.Type.ERROR_MESSAGE);
                                    System.out.println("ERROR AL INTENTAR BUSCAR VERSIONES CATALOGO PLANOS : " + ex1.getMessage());
                                    ex1.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }

        codigo += version;
        codigoTxt.setReadOnly(false);
        codigoTxt.setValue(codigo);
        codigoTxt.setReadOnly(true);
    }
}
