package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.contabilidad.MostrarPartidaContable;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import org.vaadin.dialogs.ConfirmDialog;

public class TokenView extends VerticalLayout implements View {

    UI mainUI;

    ComboBox empresaCbx;

    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public IndexedContainer tokenContainer = new IndexedContainer();
    Grid tokenGrid;

    static final String ID_PROPERTY = "ID.";
    static final String CODIGO_PROPERTY = "Código";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String ID_USUARIO_PROPERTY = "Id Usuario";
    static final String USUARIO_PROPERTY = "Usado por ";
    static final String FECHA_USADO_PROPERTY = "Usado";
    static final String FECHA_CREADO_PROPERTY = "Creado";
    static final String HORA_CREADO_PROPERTY = "Hora Creado";
    static final String CODIGO_PARTIDA_PROPERTY = "No. Partida";

    Utileria utileria;

    public TokenView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);

        utileria = new Utileria();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);

        llenarComboEmpresa();

        Label titleLbl = new Label("TOKENS DE AUTORIZACIÓN");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.BOTTOM_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearGridTokens();
        llenarGridTokens();

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }
            rsRecords.first();

            empresaCbx.select(rsRecords.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearGridTokens() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("96%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        tokenContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        tokenContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        tokenContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        tokenContainer.addContainerProperty(FECHA_CREADO_PROPERTY, String.class, null);
        tokenContainer.addContainerProperty(HORA_CREADO_PROPERTY, String.class, null);
        tokenContainer.addContainerProperty(ID_USUARIO_PROPERTY, String.class, null);
        tokenContainer.addContainerProperty(USUARIO_PROPERTY, String.class, null);
        tokenContainer.addContainerProperty(FECHA_USADO_PROPERTY, String.class, null);
        tokenContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);

        tokenGrid = new Grid("Listado de Tokens generados. ", tokenContainer);
        tokenGrid.setImmediate(true);
        tokenGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        tokenGrid.setDescription("Seleccione un registro.");
        tokenGrid.setHeightMode(HeightMode.ROW);
        tokenGrid.setHeightByRows(10);
        tokenGrid.setWidth("100%");
        tokenGrid.setResponsive(true);
        tokenGrid.setEditorBuffered(false);
        tokenGrid.setEditorEnabled(false);

        tokenGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            String codigoPartida = String.valueOf(tokenContainer.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue());
            MostrarPartidaContable mostrarPartidaContable
                    = new MostrarPartidaContable(
                            codigoPartida,
                            "",
                            "",
                            "" + " " + ""
                    );
            mainUI.addWindow(mostrarPartidaContable);
            mostrarPartidaContable.center();
        }));

        tokenGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (tokenGrid.getSelectedRows() != null) {
                    if (tokenGrid.getSelectedRow() != null) {

                    }
                }
            }
        });

        tokenGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        tokenGrid.getColumn(ID_USUARIO_PROPERTY).setHidable(true).setHidden(true);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setResponsive(true);
        buttonLayout.setSpacing(true);
        buttonLayout.setSpacing(true);

        Button nuevoToken = new Button("Nuevo Token");
        nuevoToken.setIcon(FontAwesome.PLUS);
        nuevoToken.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                TokenForm tokenForm = new TokenForm();
                UI.getCurrent().addWindow(tokenForm);
                tokenForm.center();
            }
        });

        Button eliminarToken = new Button("Eliminar Token");
        eliminarToken.setIcon(FontAwesome.TRASH);
        eliminarToken.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (tokenGrid.getSelectedRow() != null) {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de Eliminar el Token seleccionado?",
                            "SI", "NO", new ConfirmDialog.Listener() {

                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                try {
                                    eliminarToken();
                                } catch (Exception ex) {
                                    System.out.println("Error al intentar eliminar " + ex);
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });

                } else {
                    Notification.show("Por favor seleccione un Token para poder eliminarlo.", queryString, Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        buttonLayout.addComponents(nuevoToken, eliminarToken);

        reportLayout.addComponent(tokenGrid);
        reportLayout.setComponentAlignment(tokenGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.BOTTOM_CENTER);

        addComponent(buttonLayout);
        setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);

    }

    public void llenarGridTokens() {

        tokenContainer.removeAllItems();

        queryString = " select *, HOUR(TIMEDIFF(FechaCreado, CURRENT_TIMESTAMP))     as difference ";
        queryString += " from token";
        //queryString += " Order By IdToken ";

        System.out.println("Query token" + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado 2220210909102
                do {
                    Object itemId = tokenContainer.addItem();

                    tokenContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdToken"));
                    tokenContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("Codigo"));
                    tokenContainer.getContainerProperty(itemId, FECHA_CREADO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaCreado")));
                    String horaCreado = rsRecords.getString("FechaCreado").substring(11, rsRecords.getString("FechaCreado").length());
                    tokenContainer.getContainerProperty(itemId, HORA_CREADO_PROPERTY).setValue(horaCreado);

                    if (rsRecords.getString("Estatus").equals("DISPONIBLE")) {
                        if (!rsRecords.getString("difference").equals("0")) {
                            System.out.println("Token vencido ");
                            tokenContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue("VENCIDO");
                        } else {
                            tokenContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                        }
                    } else {
                        tokenContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    }

                    if (rsRecords.getObject("FechaUsado") == null) {
                        tokenContainer.getContainerProperty(itemId, ID_USUARIO_PROPERTY).setValue("NO HA SIDO USADO");
                        tokenContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue("NO HA SIDO USADO");
                        tokenContainer.getContainerProperty(itemId, FECHA_USADO_PROPERTY).setValue("NO HA SIDO USADO");
                        tokenContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("NO HA SIDO USADO");
                    } else {
                        tokenContainer.getContainerProperty(itemId, ID_USUARIO_PROPERTY).setValue(rsRecords.getString("IdUsuario"));
                        tokenContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecords.getString("Usuario"));
                        tokenContainer.getContainerProperty(itemId, FECHA_USADO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaUsado")));
                        tokenContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    }

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al consultar la tabla token en base de datos " + ex);
            ex.printStackTrace();
        }
    }

    public void eliminarToken() {

        try {
            queryString = " delete from token";
            queryString += " where IdToken = " + tokenContainer.getContainerProperty(tokenGrid.getSelectedRow(), ID_PROPERTY).getValue();

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            llenarGridTokens();
            Notification.show("Registro eliminado exitosamente!.", Notification.Type.TRAY_NOTIFICATION);

        } catch (Exception e) {
            System.out.println("Error al intetnar eliminar el token de acceso " + e);
            e.printStackTrace();
        }

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Token");
    }

}
