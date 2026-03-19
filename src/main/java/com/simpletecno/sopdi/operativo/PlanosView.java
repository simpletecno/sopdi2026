package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.*;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.*;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlanosView extends VerticalLayout implements View {

    static final String ID_PROPERTY = "Id";
    static final String CENTROCOSTO_PROPERTY = "CentroCosto";
    static final String ESTILO_PROPERTY = "Estilo";
    static final String CATEGORIA_PROPERTY = "Categoria";
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String TIPO_PROPERTY = "Tipo";
    static final String NIVEL_PROPERTY = "Nivel";
    static final String VERSION_PROPERTY = "Versión";
    static final String CODIGO_PROPERTY = "Codigo Plano";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String CREADOPOR_PROPERTY = "Cargado por";

    Button consultarBtn;

    public IndexedContainer container = new IndexedContainer();
    Grid planosGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    EnvironmentVars environmentsVars;

    public PlanosView() {
        this.mainUI = UI.getCurrent();

        environmentsVars = new EnvironmentVars();

        Label titleLbl = new Label("Planos");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarTablaPlanos();
            }
        });

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(new MarginInfo(false, true, false, true));
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl, consultarBtn);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);
        titleLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);

        createTablaPlanos();
        llenarTablaPlanos();
        createButtons();

    }

    public void createTablaPlanos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

//        CODIGODEPLANO=aabbbbccddee
//                      aa=project
//                      bbbb=centrocosto
//                      cc=categoria
//                      dd=subcategoria
//                      ee=version
        container.addContainerProperty(ID_PROPERTY, String.class, "");
        container.addContainerProperty(CENTROCOSTO_PROPERTY, String.class, "");
        container.addContainerProperty(ESTILO_PROPERTY, String.class, "");
        container.addContainerProperty(CATEGORIA_PROPERTY, String.class, "");
        container.addContainerProperty(NOMBRE_PROPERTY, String.class, "");
        container.addContainerProperty(TIPO_PROPERTY, String.class, "");
        container.addContainerProperty(NIVEL_PROPERTY, String.class, "");
        container.addContainerProperty(VERSION_PROPERTY, String.class, "");
        container.addContainerProperty(CODIGO_PROPERTY, String.class, "");
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        container.addContainerProperty(CREADOPOR_PROPERTY, String.class, "");

        planosGrid = new Grid("Listado de planos", container);
        planosGrid.setImmediate(true);
        planosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        planosGrid.setDescription("Seleccione un registro.");
        planosGrid.setHeightMode(HeightMode.ROW);
        planosGrid.setHeightByRows(15);
        planosGrid.setWidth("100%");
        planosGrid.setResponsive(true);
        planosGrid.setEditorBuffered(false);

        planosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);

        planosGrid.getColumn(ID_PROPERTY).setRenderer(new ButtonRenderer(e -> {
                VerImagen();
            }
        ));

        Grid.HeaderRow filterRow = planosGrid.appendHeaderRow();

        Grid.HeaderCell cell = filterRow.getCell(CENTROCOSTO_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);
        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(CENTROCOSTO_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(CENTROCOSTO_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(CATEGORIA_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(10);

        filterField2.addTextChangeListener(change -> {
            container.removeContainerFilters(CATEGORIA_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(CATEGORIA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        Grid.HeaderCell cell3 = filterRow.getCell(ESTILO_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(10);

        filterField3.addTextChangeListener(change -> {
            container.removeContainerFilters(ESTILO_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(ESTILO_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell3.setComponent(filterField3);
        
        Grid.HeaderCell cell4 = filterRow.getCell(DESCRIPCION_PROPERTY);
        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(10);

        filterField4.addTextChangeListener(change -> {
            container.removeContainerFilters(DESCRIPCION_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell4.setComponent(filterField4);
        
        reportLayout.addComponent(planosGrid);
        reportLayout.setComponentAlignment(planosGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            try {
                if (planosGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    PlanoForm planoForm = new PlanoForm(String.valueOf(container.getContainerProperty(planosGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(planoForm);
                    planoForm.center();
                }

            } catch (Exception ex) {
                System.out.println("Error en el boton editar" + ex);
                ex.printStackTrace();
            }
        });

        Button newBtn = new Button("Nuevo");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nuevo plano.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                PlanoForm planoForm = new PlanoForm("");
                UI.getCurrent().addWindow(planoForm);
                planoForm.center();
            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        eliminarBtn.setDescription("Eliminar plano.");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (planosGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el plano?",
                            "SI", "NO", new ConfirmDialog.Listener() {

                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {

                                try {
                                    queryString = " DELETE FROM planos ";
                                    queryString += " WHERE Id = " + String.valueOf(container.getContainerProperty(planosGrid.getSelectedRow(), ID_PROPERTY).getValue());

                                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                    stQuery.executeUpdate(queryString);

                                    Notification.show("Plano eliminado exitosamente!", Notification.Type.HUMANIZED_MESSAGE);

                                    llenarTablaPlanos();
                                } catch (SQLException ex) {
                                    Logger.getLogger(PlanosView.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        }
                    });

                }
            }
        });

        Button viewBtn = new Button("Visualizar");
        viewBtn.setIcon(FontAwesome.FILE_PDF_O);
        viewBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        viewBtn.setDescription("Visualizar pdf de plano.");
        viewBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String filePath = environmentsVars.getDtePath() + "planos/" + ((SopdiUI) mainUI).sessionInformation.getStrProjectId() + "/" + String.valueOf(container.getContainerProperty(planosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()) + ".pdf";
                PlanoShowPDF planoShowPdf = new PlanoShowPDF(filePath);
                mainUI.addWindow(planoShowPdf);
                planoShowPdf.center();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
//        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(viewBtn);
        buttonsLayout.addComponent(eliminarBtn);
        buttonsLayout.setComponentAlignment(eliminarBtn, Alignment.BOTTOM_LEFT);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarTablaPlanos() {

        container.removeAllItems();

        queryString = "SELECT PLA.*, EST.Nombre NombreEstilo, CAT.Nombre NombreCategoria, NOM.Nombre NombreNombre,";
        queryString += " TIP.Nombre NombreTipo, EST.M2, EST.Inclinacion, USR.Nombre NombreUsuario ";
        queryString += " FROM planos PLA";
        queryString += " INNER JOIN planos_estilo EST ON EST.Codigo = PLA.CodigoEstilo";
        queryString += " INNER JOIN planos_categoria CAT ON CAT.Id = PLA.IdCategoria";
        queryString += " INNER JOIN planos_nombre NOM ON NOM.Id  = PLA.IdNombre";
        queryString += " INNER JOIN planos_tipo TIP ON TIP.Id = PLA.IdTipo";
        queryString += " INNER JOIN usuario USR ON USR.IdUsuario = PLA.CreadoUsuario";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado

                Object itemId = container.addItem();

                container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                container.getContainerProperty(itemId, CENTROCOSTO_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));
                container.getContainerProperty(itemId, ESTILO_PROPERTY).setValue(rsRecords.getString("NombreEstilo"));
                container.getContainerProperty(itemId, CATEGORIA_PROPERTY).setValue(rsRecords.getString("NombreCategoria"));
                container.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("NombreNombre"));
                container.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("NombreTipo"));
                container.getContainerProperty(itemId, NIVEL_PROPERTY).setValue(rsRecords.getString("Nivel"));
                container.getContainerProperty(itemId, VERSION_PROPERTY).setValue(rsRecords.getString("Version"));
                container.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoPlano"));
                container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                container.getContainerProperty(itemId, CREADOPOR_PROPERTY).setValue(rsRecords.getString("NombreUsuario"));

            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla de planos :" + ex);
            ex.printStackTrace();
        }
    }

    public void VerImagen() {

        if(planosGrid.getSelectedRow() == null) {
            Notification.show("POR FAVOR SELECCIONE UN PLANO." , Notification.Type.WARNING_MESSAGE);
            return;
        }

        Object selectedObject = planosGrid.getSelectedRow();
        String idPlano = String.valueOf(container.getContainerProperty(selectedObject, ID_PROPERTY).getValue());

        try {

            String filePath = environmentsVars.getDtePath();

            String fileName = filePath + "planos/" + ((SopdiUI) mainUI).sessionInformation.getStrProjectId() + "/" + idPlano;
            final byte docBytes[] = Files.readAllBytes(new File(fileName).toPath());

            if (docBytes == null) {
                Notification.show("Documento PDF no disponible para visualizar!");
                return;
            }
            Window window = new Window();
            window.setResizable(true);
            window.setWidth("50%");
            window.setHeight("50%");
            window.center();

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
            documentStreamResource.setMIMEType("PDF");
            documentStreamResource.setFilename(fileName);
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

//            System.out.println("Tipo de Archivo : " + rsRecords.getString("ArchivoTipo"));
//            System.out.println("Archivo Nombre :" + rsRecords.getString("ArchivoNombre"));
//            System.out.println("Tamanio :" + rsRecords.getString("ArchivoPeso"));
            window.setWidth("98%");
            window.setHeight("98%");

            BrowserFrame browserFrame = new BrowserFrame();
            browserFrame.setSizeFull();
            browserFrame.setSource(documentStreamResource);

            window.setContent(browserFrame);

            UI.getCurrent().addWindow(window);
            window.center();

        } catch (Exception allEx) {
            Notification.show("No existe archivo PDF,  o no se puede leer el archivo.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }
    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        String parametro1 = fileName;
        String parametro2 = mimeType;
        long parametro3 = peso;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Planos");
    }
}