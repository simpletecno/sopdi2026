package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * @author user
 */
public class OrdenTrabajoView extends VerticalLayout implements View {

    Grid ordenTrabajoGrid;
    public IndexedContainer ordenTrabajoContainer = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String NOT_PROPERTY = "Número";
    static final String FECHA_PROPERTY = "Fecha";
    static final String EMPRESA_PROPERTY = "Empresa";
    static final String PROYECTO_PROPERTY = "Proyecto";
    static final String CENTRO_COSTO_PROPERTY = "Centro costo";
    static final String IDEX_PROPERTY = "IDEX";
    static final String CREADOFECHAHORA_PROPERTY = "Creada Fecha";
    static final String CREADOUSUARIO_PROPERTY = "Usuario";
    
    UI mainUI;
    Statement stQuery, stQuery2;
    ResultSet rsRecords, rsRecords2;
    String queryString;

    DateField inicioDt;
    DateField finDt;
    Button consultarBtn;

    ComboBox empresaCbx;

    public OrdenTrabajoView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);

        Label titleLbl = new Label("ORDENES DE TRABAJO");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        crearButtonEmpresa();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setMargin(new MarginInfo(false, true, false, false));
        titleLayout.setWidth("100%");

        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaOrdenTrabajo();
    }

    public void crearTablaOrdenTrabajo() {

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.addStyleName("rcorners3");
        contentLayout.setWidth("100%");
        contentLayout.setResponsive(true);
        contentLayout.setSpacing(true);

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setSpacing(true);

        inicioDt = new DateField("Desde:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("10em");

        finDt = new DateField("Hasta:");
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
                 llenarTablaOrdenTrabajo();
            }
        });

        Button newBtn = new Button("Nueva orden");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newBtn.setDescription("Agregar nueva orden de trabajo.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                OrdenTrabajoForm ordenForm = new OrdenTrabajoForm(0);
                UI.getCurrent().addWindow(ordenForm);
                ordenForm.center();
            }
        });

        ordenTrabajoContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        ordenTrabajoContainer.addContainerProperty(NOT_PROPERTY, String.class, null);
        ordenTrabajoContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        ordenTrabajoContainer.addContainerProperty(EMPRESA_PROPERTY, String.class, null);
        ordenTrabajoContainer.addContainerProperty(PROYECTO_PROPERTY, String.class, null);
        ordenTrabajoContainer.addContainerProperty(CENTRO_COSTO_PROPERTY, String.class, null);
        ordenTrabajoContainer.addContainerProperty(IDEX_PROPERTY, String.class, null);
        ordenTrabajoContainer.addContainerProperty(CREADOFECHAHORA_PROPERTY, String.class, null);
        ordenTrabajoContainer.addContainerProperty(CREADOUSUARIO_PROPERTY, String.class, null);

        ordenTrabajoGrid = new Grid("Listado de ordenes de trabajo", ordenTrabajoContainer);
        ordenTrabajoGrid.setWidth("100%");
        ordenTrabajoGrid.setImmediate(true);
        ordenTrabajoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ordenTrabajoGrid.setDescription("Seleccione un registro.");
        ordenTrabajoGrid.setHeightMode(HeightMode.ROW);
        ordenTrabajoGrid.setHeightByRows(15);
        ordenTrabajoGrid.setResponsive(true);
        ordenTrabajoGrid.setEditorBuffered(false);
        ordenTrabajoGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        ordenTrabajoGrid.getColumn(EMPRESA_PROPERTY).setHidable(true);
        ordenTrabajoGrid.getColumn(PROYECTO_PROPERTY).setHidable(true);
        ordenTrabajoGrid.getColumn(CREADOFECHAHORA_PROPERTY).setHidable(true);
        ordenTrabajoGrid.getColumn(CREADOUSUARIO_PROPERTY).setHidable(true);

        filtrosLayout.addComponent(inicioDt);
        filtrosLayout.addComponent(finDt);
        filtrosLayout.addComponent(consultarBtn);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(newBtn);
        filtrosLayout.setComponentAlignment(newBtn, Alignment.BOTTOM_CENTER);

        contentLayout.addComponent(filtrosLayout);
        contentLayout.setComponentAlignment(filtrosLayout, Alignment.MIDDLE_CENTER);

        contentLayout.addComponent(ordenTrabajoGrid);
        contentLayout.setComponentAlignment(ordenTrabajoGrid, Alignment.MIDDLE_CENTER);

        Button editBtn = new Button("Editar orden");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setDescription("Editar orden de trabajo.");
        editBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (ordenTrabajoGrid.getSelectedRow() != null) {
                    OrdenTrabajoForm ordenForm =
                            new OrdenTrabajoForm(
                                    Integer.valueOf(String.valueOf(ordenTrabajoContainer.getContainerProperty(ordenTrabajoGrid.getSelectedRow(),ID_PROPERTY).getValue()))
                            );
                    UI.getCurrent().addWindow(ordenForm);
                    ordenForm.center();
                } else {
                    Notification notif = new Notification("Por favor seleccione una orden de trab ajo para poder editarla..", Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        botonesLayout.addComponent(editBtn);
        botonesLayout.setComponentAlignment(editBtn, Alignment.BOTTOM_CENTER);

        contentLayout.addComponent(botonesLayout);
        contentLayout.setComponentAlignment(botonesLayout, Alignment.BOTTOM_CENTER);
        addComponent(contentLayout);
        setComponentAlignment(contentLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarTablaOrdenTrabajo() {

        ordenTrabajoContainer.removeAllItems();

        try {

            if (inicioDt.getValue().before(finDt.getValue()) == true) {

                queryString = " SELECT OT.*, EMP.Empresa as EmpresaNombre, ";
                queryString += " PTA.Descripcion, usuario.Nombre as NombreUsuario ";
                queryString += " FROM orden_trabajo OT";
                queryString += " INNER JOIN contabilidad_empresa EMP ON EMP.IdEmpresa = OT.IdEmpresa";
                queryString += " INNER JOIN project_tarea PTA ON PTA.Idex = OT.Idex ";
                queryString += " INNER JOIN project PJ ON PJ.Numero = PTA.Numero AND PJ.Estatus = 'ACTIVO'";
                queryString += " INNER JOIN usuario ON usuario.IdUsuario = OT.CreadoUsuario ";
                queryString += " WHERE OT.Fecha between " + "'" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
                queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
                queryString += " AND OT.IdEmpresa =" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
System.out.println(queryString);
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado    
                    do {
                        Object itemId = ordenTrabajoContainer.addItem();
                        ordenTrabajoContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                        ordenTrabajoContainer.getContainerProperty(itemId, NOT_PROPERTY).setValue(rsRecords.getString("NumeroOrdenTrabajo"));
                        ordenTrabajoContainer.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("EmpresaNombre"));
                        ordenTrabajoContainer.getContainerProperty(itemId, PROYECTO_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
                        ordenTrabajoContainer.getContainerProperty(itemId, CENTRO_COSTO_PROPERTY).setValue(rsRecords.getString("IDCC"));
                        ordenTrabajoContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex") + " " + rsRecords.getString("Descripcion"));
                        ordenTrabajoContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        ordenTrabajoContainer.getContainerProperty(itemId, CREADOFECHAHORA_PROPERTY).setValue(rsRecords.getString("CreadoFechaYHora"));
                        ordenTrabajoContainer.getContainerProperty(itemId, CREADOUSUARIO_PROPERTY).setValue(rsRecords.getString("NombreUsuario"));

                    } while (rsRecords.next());

                    ordenTrabajoGrid.select(ordenTrabajoContainer.firstItemId());
                }

            } else {
                Notification.show("La fecha hasta no puede contener un valor menor a la fecha de inicio.", Notification.Type.WARNING_MESSAGE);
                inicioDt.focus();
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla oden de compra " + ex);
            ex.printStackTrace();
        }
    }

    public void crearButtonEmpresa() {

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);

        queryString = " SELECT * FROM contabilidad_empresa";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Orden de Compra");
    }
}
