package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author user
 */
public class AsistenciaCambioRazonView extends VerticalLayout implements View {
    DateField fechaDt;

    public static final String IDBITACORA_PROPERTY = "Id";
    public static final String IDEMPLEADO_PROPERTY = "IdEmpleado";
    public static final String EMPLEADO_PROPERTY = "Nombre";
    public static final String GRUPONOMBRE_PROPERTY = "Grupo";
    public static final String ESTATUS_PROPERTY = "Estatus";
    public static final String RAZON_PROPERTY = "Razón";

    public IndexedContainer groupBitacoraContainer = new IndexedContainer();
    Grid groupBitacoraGrid;

    ComboBox razonAusenciaCbx;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    VerticalLayout mainLayout = new VerticalLayout();

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AsistenciaCambioRazonView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " ASISTENCIA DIARIA DE TRABAJO");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createComboRazonAusencia();

        createDetailsGrid();

        fillgroupBitacoraGrid();

    }

    public void createDetailsGrid() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.addStyleName("rcorners2");
        detalleLayout.setSpacing(true);

        HorizontalLayout idexYEmpleadosLayout = new HorizontalLayout();
        idexYEmpleadosLayout.setWidth("100%");
        idexYEmpleadosLayout.addStyleName("rcorners3");
        idexYEmpleadosLayout.setSpacing(true);

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        fechaDt = new DateField("Fecha de plan:");
        fechaDt.setValue(new java.util.Date());
        fechaDt.addStyleName(ValoTheme.DATEFIELD_LARGE);
        fechaDt.setSizeFull();
        fechaDt.setDateFormat("dd-MMM-yyyy");
        fechaDt.setEnabled(true);
        fechaDt.addValueChangeListener((event) -> {
            fillgroupBitacoraGrid();
        });

        HorizontalLayout fechaLayout = new HorizontalLayout();
        fechaLayout.setSpacing(true);
        fechaLayout.addComponent(fechaDt);
        fechaLayout.setComponentAlignment(fechaDt, Alignment.BOTTOM_CENTER);

        detalleLayout.addComponents(fechaLayout, idexYEmpleadosLayout, botonesLayout);

        groupBitacoraContainer.addContainerProperty(IDBITACORA_PROPERTY, String.class, null);
        groupBitacoraContainer.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, null);
        groupBitacoraContainer.addContainerProperty(EMPLEADO_PROPERTY, String.class, null);
        groupBitacoraContainer.addContainerProperty(GRUPONOMBRE_PROPERTY, String.class, null);
        groupBitacoraContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        groupBitacoraContainer.addContainerProperty(RAZON_PROPERTY, String.class, null);

        groupBitacoraGrid = new Grid("BITACORA DEL  : " + Utileria.getFechaDDMMYYYY(new java.util.Date()), groupBitacoraContainer);
        groupBitacoraGrid.setImmediate(true);
        groupBitacoraGrid.setSelectionMode(Grid.SelectionMode.NONE);
        groupBitacoraGrid.setHeightMode(HeightMode.ROW);
        groupBitacoraGrid.setHeightByRows(15);
        groupBitacoraGrid.setWidth("100%");
        groupBitacoraGrid.setResponsive(true);
        groupBitacoraGrid.setEditorBuffered(false);
        groupBitacoraGrid.setSizeFull();
        groupBitacoraGrid.setEditorEnabled(true);
        groupBitacoraGrid.getColumn(ESTATUS_PROPERTY).setEditorField(getComboState());
        groupBitacoraGrid.getColumn(RAZON_PROPERTY).setEditorField(getEditTextRazon());
        groupBitacoraGrid.addItemClickListener((event) -> {
            if (event != null) {
                groupBitacoraGrid.editItem(event.getItemId());
            }
        });

        groupBitacoraGrid.getColumn(IDBITACORA_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);
        groupBitacoraGrid.getColumn(IDEMPLEADO_PROPERTY).setExpandRatio(1);
        groupBitacoraGrid.getColumn(EMPLEADO_PROPERTY).setExpandRatio(3);
        groupBitacoraGrid.getColumn(GRUPONOMBRE_PROPERTY).setExpandRatio(1);
        groupBitacoraGrid.getColumn(ESTATUS_PROPERTY).setExpandRatio(2);
        groupBitacoraGrid.getColumn(RAZON_PROPERTY).setExpandRatio(3);

        idexYEmpleadosLayout.addComponent(groupBitacoraGrid);

        Button actualizarBtn = new Button("Actualizar");
        actualizarBtn.setIcon(FontAwesome.EDIT);
        actualizarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        actualizarBtn.setDescription("ACTUALIZAR empleado al grupo");
        actualizarBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            try {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                for(Object objectItem : groupBitacoraContainer.getItemIds()) {

                    if(String.valueOf(groupBitacoraContainer.getContainerProperty(objectItem, ESTATUS_PROPERTY).getValue()).equals("PRESENTE") & !String.valueOf(groupBitacoraContainer.getContainerProperty(objectItem, RAZON_PROPERTY).getValue()).trim().isEmpty() ) {
                        Notification.show("El estatus de asistencia del empleado : " + String.valueOf(groupBitacoraContainer.getContainerProperty(objectItem, EMPLEADO_PROPERTY).getValue()) + " es PRESENTE, debe primero cambiarlo a AUSENTE para elgir razón de ausencia.", Notification.Type.WARNING_MESSAGE);
                        return;
                    }

                    queryString = "UPDATE empleado_asistencia SET ";
                    queryString += " Razon   = '" + String.valueOf(groupBitacoraContainer.getContainerProperty(objectItem, RAZON_PROPERTY).getValue()) + "'";
                    queryString += ",EsDefinitiva = 1";
                    queryString += " WHERE Id = " + String.valueOf(groupBitacoraContainer.getContainerProperty(objectItem, IDBITACORA_PROPERTY).getValue());

                    stQuery.executeUpdate(queryString);

                    queryString = "UPDATE proveedor_empresa SET ";
                    queryString += " Razon   = '" + String.valueOf(groupBitacoraContainer.getContainerProperty(objectItem, RAZON_PROPERTY).getValue()) + "'";
                    queryString += " WHERE IdProveedor = " + String.valueOf(groupBitacoraContainer.getContainerProperty(objectItem, IDEMPLEADO_PROPERTY).getValue());

                    stQuery.executeUpdate(queryString);

                }

                Notification notif = new Notification("ASISTENCIA ACTUALIZADA EXITOSAMENTE.",
                        Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.CHECK);
                notif.show(Page.getCurrent());

            } catch (Exception ex) {
                System.out.println("Error al actualizar bitacora de empleados : " + ex);
                ex.printStackTrace();
                Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        });

        Button printAsistenciaBtn = new Button("Imprimir AUSENCIAS");
        printAsistenciaBtn.setIcon(FontAwesome.PRINT);
        printAsistenciaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        printAsistenciaBtn.setDescription("Imprimir PLAN");
        printAsistenciaBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (groupBitacoraContainer.size() == 0) {
                Notification notif = new Notification("No hay bitacora o asistencia.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
            else {
                EmpleadoAsistenciaPDF empleadoAsistenciaPDF =
                        new EmpleadoAsistenciaPDF(
                                Utileria.getFechaDDMMYYYY(fechaDt.getValue()),
                                groupBitacoraContainer
                        );
                mainUI.addWindow(empleadoAsistenciaPDF);
                //
            }
        });

        botonesLayout.addComponent(actualizarBtn);
        botonesLayout.setComponentAlignment(actualizarBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(printAsistenciaBtn);
        botonesLayout.setComponentAlignment(printAsistenciaBtn, Alignment.BOTTOM_LEFT);
        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);

    }

    private void createComboRazonAusencia() {
        razonAusenciaCbx= new ComboBox();
        razonAusenciaCbx.setImmediate(true);
        razonAusenciaCbx.setNullSelectionAllowed(false);
        razonAusenciaCbx.setTextInputAllowed(true);
        razonAusenciaCbx.setInvalidAllowed(true);
        razonAusenciaCbx.setNewItemsAllowed(true);
        razonAusenciaCbx.clear();
        razonAusenciaCbx.setWidth("6em");

        razonAusenciaCbx.addItem("");

        String queryString = "";
        queryString += " SELECT * FROM razon_ausencia";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                razonAusenciaCbx.addItem(rsRecords.getString("Razon"));
            }
        } catch (Exception ex1) {
            System.out.println("Error al combo razones de ausencia: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private Field<?> getComboState() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(false);
        comboBox.setInvalidAllowed(false);
        comboBox.setNewItemsAllowed(false);
        comboBox.clear();
        comboBox.setWidth("15em");

        comboBox.addItem("AUSENTE");
        comboBox.select("AUSENTE");

        return comboBox;
    }

    private Field<?> getEditTextRazon() {

        razonAusenciaCbx.select("");

        return razonAusenciaCbx;
    }

    public void fillgroupBitacoraGrid() {
        groupBitacoraContainer.removeAllItems();
        groupBitacoraContainer.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = " SELECT BITA.*, PROV.IdProveedor, PROV.Nombre NombreEmpleado, PROV.GrupoTrabajo, PROV.TipoAsignacion";
            queryString += " FROM empleado_asistencia BITA";
            queryString += " INNER JOIN proveedor_empresa PROV ON PROV.IdProveedor = BITA.IdEmpleado AND PROV.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " WHERE BITA.Fecha = '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += " AND   PROV.Inhabilitado = 0";
            queryString += " AND   PROV.EsPlanilla = 1";
            queryString += " AND   PROV.IdEmpresa = " + empresaId;
            queryString += " AND   BITA.Estatus = 'AUSENTE'";
            queryString += " ORDER BY PROV.Nombre";

            rsRecords = stQuery.executeQuery(queryString);

            if(!rsRecords.next()) { //NO EXISTE BITACORA PARA HOY, HAY QUE CREARLA
                Notification.show("NO HAY BITACORA DE ASISTENCIA DE ESTE DIA.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            else {
                do {
                    Object itemId = groupBitacoraContainer.addItem();
                    groupBitacoraContainer.getContainerProperty(itemId, IDBITACORA_PROPERTY).setValue(rsRecords.getString("Id"));
                    groupBitacoraContainer.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdEmpleado"));
                    groupBitacoraContainer.getContainerProperty(itemId, EMPLEADO_PROPERTY).setValue(rsRecords.getString("NombreEmpleado"));
                    groupBitacoraContainer.getContainerProperty(itemId, GRUPONOMBRE_PROPERTY).setValue(rsRecords.getString("GrupoTrabajo"));
                    groupBitacoraContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    groupBitacoraContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));
                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla BITACORA DE EMPLEADOS DEL GRUPO : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - CAMBIAR RAZON INASISTENCIA");
    }
}
