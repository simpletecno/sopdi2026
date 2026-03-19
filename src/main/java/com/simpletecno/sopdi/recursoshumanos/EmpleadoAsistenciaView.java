package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
@SuppressWarnings("ALL")
public class EmpleadoAsistenciaView extends VerticalLayout implements View {
    DateField fechaDt;

    public static final String IDBITACORA_PROPERTY = "Id";
    public static final String IDEMPLEADO_PROPERTY = "IdEmpleado";
    public static final String EMPLEADO_PROPERTY = "Nombre";
    public static final String CARGO_PROPERTY = "Cargo";
    public static final String IDEX_PROPERTY = "IDEX";
    public static final String DESCRIPCION_PROPERTY = "Descripción";
    public static final String HORASEXTRA_PROPERTY = "Hrs Extra";
    public static final String HORASEXTRAII_PROPERTY = "Hrs ExtraII";
    public static final String EVENTO_PROPERTY = "Eventos";
    public static final String DIASVACACIONES_PROPERTY = "Dias vacaciones";
    public static final String ESTATUS_PROPERTY = "Estatus";
    public static final String RAZON_PROPERTY = "Razón";

    public IndexedContainer planbBitacoraContainer = new IndexedContainer();
    Grid planBitacoraGrid;
    public IndexedContainer planBitacoraIDEXContainer = new IndexedContainer(); //SOLO PARA EL REPORTE DE ASISTENCIA CON IDEX

    ComboBox razonAusenciaCbx;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    VerticalLayout mainLayout = new VerticalLayout();

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public EmpleadoAsistenciaView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " ASISTENCIA DIARIA PLAN DE TRABAJO");
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

        fillPlanBitacoraGrid();
        fillPlanBitacoraIDEXGrid();

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
        fechaDt.setValue(new Date());
        fechaDt.addStyleName(ValoTheme.DATEFIELD_LARGE);
        fechaDt.setSizeFull();
        fechaDt.setDateFormat("dd-MMM-yyyy");
        fechaDt.setEnabled(true);
        fechaDt.addValueChangeListener((event) -> {
            fillPlanBitacoraGrid();
        });
        fechaDt.setShowISOWeekNumbers(true);

        HorizontalLayout fechaLayout = new HorizontalLayout();
        fechaLayout.setSpacing(true);
        fechaLayout.addComponent(fechaDt);
        fechaLayout.setComponentAlignment(fechaDt, Alignment.BOTTOM_CENTER);

        detalleLayout.addComponents(fechaLayout, idexYEmpleadosLayout, botonesLayout);

        planbBitacoraContainer.addContainerProperty(IDBITACORA_PROPERTY, String.class, null);
        planbBitacoraContainer.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, null);
        planbBitacoraContainer.addContainerProperty(EMPLEADO_PROPERTY, String.class, null);
        planbBitacoraContainer.addContainerProperty(CARGO_PROPERTY, String.class, null);
        planbBitacoraContainer.addContainerProperty(IDEX_PROPERTY, String.class, "0");
        planbBitacoraContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        planbBitacoraContainer.addContainerProperty(HORASEXTRA_PROPERTY, String.class, "0");
        planbBitacoraContainer.addContainerProperty(HORASEXTRAII_PROPERTY, String.class, "0");
        planbBitacoraContainer.addContainerProperty(EVENTO_PROPERTY, String.class, "0");
        planbBitacoraContainer.addContainerProperty(DIASVACACIONES_PROPERTY, String.class, "0");
        planbBitacoraContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, "PRESENTE");
        planbBitacoraContainer.addContainerProperty(RAZON_PROPERTY, String.class, "");

        planBitacoraIDEXContainer.addContainerProperty(IDBITACORA_PROPERTY, String.class, null);
        planBitacoraIDEXContainer.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, null);
        planBitacoraIDEXContainer.addContainerProperty(EMPLEADO_PROPERTY, String.class, null);
        planBitacoraIDEXContainer.addContainerProperty(HORASEXTRA_PROPERTY, String.class, "0");
        planBitacoraIDEXContainer.addContainerProperty(HORASEXTRAII_PROPERTY, String.class, "0");
        planBitacoraIDEXContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        planBitacoraIDEXContainer.addContainerProperty(RAZON_PROPERTY, String.class, null);
        planBitacoraIDEXContainer.addContainerProperty(IDEX_PROPERTY, String.class, null);

        planBitacoraGrid = new Grid("BITACORA DE HOY : " + Utileria.getFechaDDMMYYYY(new Date()), planbBitacoraContainer);
        planBitacoraGrid.setImmediate(true);
        planBitacoraGrid.setSelectionMode(Grid.SelectionMode.NONE);
        planBitacoraGrid.setHeightMode(HeightMode.ROW);
        planBitacoraGrid.setHeightByRows(15);
        planBitacoraGrid.setWidth("100%");
        planBitacoraGrid.setResponsive(true);
        planBitacoraGrid.setEditorBuffered(false);
        planBitacoraGrid.setSizeFull();
        planBitacoraGrid.setEditorEnabled(true);
        planBitacoraGrid.getColumn(ESTATUS_PROPERTY).setEditorField(getComboState());
        planBitacoraGrid.getColumn(RAZON_PROPERTY).setEditorField(getEditTextRazon());
        planBitacoraGrid.getColumn(HORASEXTRA_PROPERTY).setEditorField(getEditTextHoras());
        planBitacoraGrid.getColumn(HORASEXTRAII_PROPERTY).setEditorField(getEditTextHoras());
        planBitacoraGrid.getColumn(EVENTO_PROPERTY).setEditorField(getEditTextEventos());
        planBitacoraGrid.getColumn(DIASVACACIONES_PROPERTY).setEditorField(getEditTextDiasVacaciones());
        planBitacoraGrid.addItemClickListener((event) -> {
            if (event != null) {
                planBitacoraGrid.editItem(event.getItemId());
            }
        });

        planBitacoraGrid.getColumn(IDBITACORA_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);
        planBitacoraGrid.getColumn(IDEMPLEADO_PROPERTY).setExpandRatio(1);
        planBitacoraGrid.getColumn(EMPLEADO_PROPERTY).setExpandRatio(3);
        planBitacoraGrid.getColumn(HORASEXTRA_PROPERTY).setExpandRatio(1);
        planBitacoraGrid.getColumn(HORASEXTRAII_PROPERTY).setExpandRatio(1);
        planBitacoraGrid.getColumn(EVENTO_PROPERTY).setExpandRatio(1);
        planBitacoraGrid.getColumn(DIASVACACIONES_PROPERTY).setExpandRatio(1);
        planBitacoraGrid.getColumn(ESTATUS_PROPERTY).setExpandRatio(2);
        planBitacoraGrid.getColumn(RAZON_PROPERTY).setExpandRatio(3);

        idexYEmpleadosLayout.addComponent(planBitacoraGrid);

        Button actualizarBtn = new Button("Actualizar");
        actualizarBtn.setIcon(FontAwesome.EDIT);
        actualizarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        actualizarBtn.setDescription("ACTUALIZAR empleado");
        actualizarBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            try {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                for(Object objectItem : planbBitacoraContainer.getItemIds()) {

                    if(String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, ESTATUS_PROPERTY).getValue()).equals("PRESENTE") & !String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, RAZON_PROPERTY).getValue()).trim().isEmpty() ) {
                        Notification.show("El estatus de asistencia del empleado : " + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, EMPLEADO_PROPERTY).getValue()) + " es PRESENTE, debe primero cambiarlo a AUSENTE para elgir razón de ausencia.", Notification.Type.WARNING_MESSAGE);
                        return;
                    }

                    queryString = "UPDATE empleado_asistencia SET ";
                    queryString += " Idex = '" + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, IDEX_PROPERTY).getValue()) + "'";
                    queryString += ",Estatus = '" + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, ESTATUS_PROPERTY).getValue()) + "'";
                    queryString += ",Razon   = '" + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, RAZON_PROPERTY).getValue()) + "'";
                    queryString += ",EsDefinitiva = 1";
                    queryString += ",EsDescuento = 1";
                    queryString += ",HorasExtra = " + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, HORASEXTRA_PROPERTY).getValue());
                    queryString += ",HorasExtraDoble = " + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, HORASEXTRAII_PROPERTY).getValue());
                    queryString += ",Eventos = " + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, EVENTO_PROPERTY).getValue());
                    queryString += ",DiasVacaciones = " + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, DIASVACACIONES_PROPERTY).getValue());
                    queryString += " WHERE Id = " + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, IDBITACORA_PROPERTY).getValue());

                    stQuery.executeUpdate(queryString);

                    queryString = "UPDATE proveedor_empresa SET ";
                    queryString += " EstatusTrabajo = '" + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, ESTATUS_PROPERTY).getValue()) + "'";
                    queryString += ",Razon   = '" + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, RAZON_PROPERTY).getValue()) + "'";
                    if(String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, ESTATUS_PROPERTY).getValue()).equals("DE BAJA")){
                        queryString += ",FechaEgreso = '" + Utileria.getFechaYYYYMMDD_1(new Date()) + "' ";
                    }else {
                        queryString += ",FechaEgreso = NULL ";
                    }
                    queryString += " WHERE IdProveedor = " + String.valueOf(planbBitacoraContainer.getContainerProperty(objectItem, IDEMPLEADO_PROPERTY).getValue());

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

        Button printAsistenciaBtn = new Button("Imprimir ASISTENCIA (PDF)");
        printAsistenciaBtn.setIcon(FontAwesome.PRINT);
        printAsistenciaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        printAsistenciaBtn.setDescription("Imprimir ASISTENCIA en PDF");
        printAsistenciaBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (planbBitacoraContainer.size() == 0) {
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
                                planbBitacoraContainer
                        );
                mainUI.addWindow(empleadoAsistenciaPDF);
                //
            }
        });

        Button printAsistenciaIdexBtn = new Button("Imprimir ASISTENCIA + IDEX (PDF)");
        printAsistenciaIdexBtn.setIcon(FontAwesome.PRINT);
        printAsistenciaIdexBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        printAsistenciaIdexBtn.setDescription("Imprimir ASISTENCIA + IDEX en PDF");
        printAsistenciaIdexBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (planBitacoraIDEXContainer.size() == 0) {
                Notification notif = new Notification("No hay bitacora o asistencia.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
            else {
                EmpleadoAsistenciaIdexPDF empleadoAsistenciaIdexPDF =
                        new EmpleadoAsistenciaIdexPDF(
                                Utileria.getFechaDDMMYYYY(fechaDt.getValue()),
                                planBitacoraIDEXContainer
                        );
                mainUI.addWindow(empleadoAsistenciaIdexPDF);
                //
            }
        });

        Button exportAsistenciaBtn = new Button("Exportar ASISTENCIA a Excel");
        exportAsistenciaBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportAsistenciaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        exportAsistenciaBtn.setDescription("Exportar a Excel");
        exportAsistenciaBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (planbBitacoraContainer.size() == 0) {
                Notification notif = new Notification("No hay bitacora o asistencia.",
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

        botonesLayout.addComponent(actualizarBtn);
        botonesLayout.setComponentAlignment(actualizarBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(printAsistenciaBtn);
        botonesLayout.setComponentAlignment(printAsistenciaBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(printAsistenciaIdexBtn);
        botonesLayout.setComponentAlignment(printAsistenciaIdexBtn, Alignment.BOTTOM_LEFT);
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

        comboBox.addItem("PRESENTE");
        comboBox.addItem("AUSENTE");
        comboBox.addItem("DE BAJA");
        comboBox.select("PRESENTE");

        return comboBox;
    }

    private Field<?> getEditTextRazon() {

        razonAusenciaCbx.select("");

        return razonAusenciaCbx;
    }

    private Field<?> getEditTextHoras() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(true);
        comboBox.setInvalidAllowed(true);
        comboBox.setNewItemsAllowed(true);
        comboBox.clear();
        comboBox.setWidth("6em");

        comboBox.addItem("0");
        comboBox.addItem("1");
        comboBox.addItem("2");
        comboBox.addItem("3");
        comboBox.addItem("4");
        comboBox.addItem("5");
        comboBox.addItem("6");
        comboBox.addItem("7");
        comboBox.addItem("8");
        comboBox.select("0");

        return comboBox;
    }

    private Field<?> getEditTextEventos() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(true);
        comboBox.setInvalidAllowed(true);
        comboBox.setNewItemsAllowed(true);
        comboBox.clear();
        comboBox.setWidth("6em");

        comboBox.addItem("0");
        comboBox.addItem("1");
        comboBox.addItem("2");
        comboBox.addItem("3");
        comboBox.select("0");

        return comboBox;
    }

    private Field<?> getEditTextDiasVacaciones() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(true);
        comboBox.setInvalidAllowed(true);
        comboBox.setNewItemsAllowed(true);
        comboBox.clear();
        comboBox.setWidth("6em");

        comboBox.addItem("0");
        comboBox.addItem(".5");
        comboBox.addItem("1");
        comboBox.addItem("2");
        comboBox.addItem("3");
        comboBox.addItem("4");
        comboBox.addItem("5");
        comboBox.addItem("6");
        comboBox.addItem("7");
        comboBox.addItem("8");
        comboBox.addItem("9");
        comboBox.addItem("10");
        comboBox.addItem("11");
        comboBox.addItem("12");
        comboBox.addItem("13");
        comboBox.addItem("14");
        comboBox.addItem("15");
        comboBox.addItem("16");
        comboBox.addItem("17");
        comboBox.addItem("18");
        comboBox.addItem("19");
        comboBox.addItem("20");
        comboBox.select("0");

        return comboBox;
    }
    public void fillPlanBitacoraGrid() {
        planbBitacoraContainer.removeAllItems();

        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(fechaDt.getValue());

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = " SELECT BITA.*, PROV.IdProveedor, PROV.Nombre NombreEmpleado, ";
            queryString += " PROV.Cargo, PROV.TipoAsignacion, PROV.FechaIngreso";
            queryString += " FROM empleado_asistencia BITA";
            queryString += " INNER JOIN proveedor_empresa PROV ON PROV.IdProveedor = BITA.IdEmpleado ";
            queryString += " WHERE BITA.Fecha = '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += " AND   PROV.Inhabilitado = 0";
            queryString += " AND   PROV.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " ORDER BY PROV.Nombre";

//System.out.println("queryBITACORA=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);

//System.out.println("DAY_OF_WEEK=" + today.get(java.util.GregorianCalendar.DAY_OF_WEEK));

            if(!rsRecords.next()) { //NO EXISTE BITACORA PARA HOY, HAY QUE CREARLA

                if (today.get(java.util.GregorianCalendar.DAY_OF_WEEK) == 1) { //DIA DOMINGO

                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "HOY ES DIA DOMINGO, CONFIRME SI DESEA CREAR ASISTENCIA PARA ESTE DIA ?",
                            "SI", "NO", new ConfirmDialog.Listener() {

                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        crearBitacora();
                                        fillPlanBitacoraGrid(); //recursivo
                                    }
                                }
                            }
                    );
                }
                else {
                    crearBitacora();
                }
            }
            else {
                do {
                    Object itemId = planbBitacoraContainer.addItem();
                    planbBitacoraContainer.getContainerProperty(itemId, IDBITACORA_PROPERTY).setValue(rsRecords.getString("Id"));
                    planbBitacoraContainer.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdEmpleado"));
                    planbBitacoraContainer.getContainerProperty(itemId, EMPLEADO_PROPERTY).setValue(rsRecords.getString("NombreEmpleado"));
                    planbBitacoraContainer.getContainerProperty(itemId, CARGO_PROPERTY).setValue(rsRecords.getString("Cargo"));
                    if( !((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                        queryString = getIdexActivo(rsRecords.getString("IdEmpleado"));
                        planbBitacoraContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(queryString.split("-")[0]);
                        planbBitacoraContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(queryString.split("-")[1]);
                    }
                    planbBitacoraContainer.getContainerProperty(itemId, HORASEXTRA_PROPERTY).setValue(rsRecords.getString("HorasExtra"));
                    planbBitacoraContainer.getContainerProperty(itemId, HORASEXTRAII_PROPERTY).setValue(rsRecords.getString("HorasExtraDoble"));
                    planbBitacoraContainer.getContainerProperty(itemId, EVENTO_PROPERTY).setValue(rsRecords.getString("Eventos"));
                    planbBitacoraContainer.getContainerProperty(itemId, DIASVACACIONES_PROPERTY).setValue(rsRecords.getString("DiasVacaciones"));
                    planbBitacoraContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    planbBitacoraContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));
                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla BITACORA DE EMPLEADOS : " + ex);
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void crearBitacora() {
        queryString = "Insert Into empleado_asistencia (IdEmpleado, Cargo, Fecha, Estatus, Razon, EsDefinitiva, CreadoFechaYHora, CreadoIdUsuario)";
        queryString += " SELECT IdProveedor, Cargo, '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "', EstatusTrabajo, Razon, 0, current_timestamp, " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += " FROM proveedor_empresa ";
        queryString += " WHERE EsPlanilla = 1 ";
        queryString += " AND   Inhabilitado = 0";
        queryString += " AND   EstatusTrabajo <> 'DE BAJA'";
        queryString += " AND   ISNULL(FechaIngreso) = 0 ";
        queryString += " AND   ISNULL(FechaEgreso) = 1 ";
        queryString += " AND   FechaIngreso <= '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += " AND   IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

//System.out.println("queryINSERTBITACORA=" + queryString);

        try {
            stQuery.executeUpdate(queryString);

            fillPlanBitacoraGrid();
        }
        catch(SQLException sqlE) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla BITACORA DE EMPLEADOS DEL GRUPO.");
            Notification.show("ERROR DE BASE DE DATOS " + sqlE.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public void fillPlanBitacoraIDEXGrid() {
        planBitacoraIDEXContainer.removeAllItems();
        planBitacoraIDEXContainer.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = " SELECT BITA.*, PROV.IdProveedor, PROV.Nombre NombreEmpleado, PROV.TipoAsignacion";
            queryString += " FROM empleado_asistencia BITA ";
            queryString += " INNER JOIN proveedor_empresa PROV ON PROV.IdProveedor = BITA.IdEmpleado ";
            queryString += " WHERE BITA.Fecha = '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += " AND   PROV.Inhabilitado = 0";
            queryString += " AND   PROV.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND   PROV.FechaIngreso <= '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += " ORDER BY PROV.Nombre";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    Object itemId = planBitacoraIDEXContainer.addItem();
                    planBitacoraIDEXContainer.getContainerProperty(itemId, IDBITACORA_PROPERTY).setValue(rsRecords.getString("Id"));
                    planBitacoraIDEXContainer.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdEmpleado"));
                    planBitacoraIDEXContainer.getContainerProperty(itemId, EMPLEADO_PROPERTY).setValue(rsRecords.getString("NombreEmpleado"));
                    planBitacoraIDEXContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    planBitacoraIDEXContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));
                    if( !((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                        planBitacoraIDEXContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(getIdexActivo(rsRecords.getString("IdEmpleado")));
                    }
                    } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla BITACORA DE EMPLEADOS DEL GRUPO + IDEX : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private String getIdexActivo(String idEmpleado) {
        String idexActivo = "0-0";

        try {
            String queryString = "SELECT PTI.IDEX, PTA.Descripcion, PTI.FechaInicioSegunJefe, PTI.FechaFinSegunJefe ";
            queryString += " FROM plan_trabajo_idex PTI ";
            queryString += " INNER JOIN plan_trabajo_idex_rh PTIR ON PTIR.IdPlanTrabajoIdex = PTI.Id ";
            queryString += " INNER JOIN project_tarea PTA ON PTA.Idex = PTI.Idex ";
            queryString += " INNER JOIN project PJ ON PJ.Id = PTA.IdProject AND PJ.Estatus = 'ACTIVO' ";
            queryString += " WHERE PTIR.IDEmpleado = " + idEmpleado;
            queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "' >= PTI.FechaInicioSegunJefe";

//            System.out.println("queryBITACORA=" + queryString);

            rsRecords1 = stQuery1.executeQuery(queryString);

            if(rsRecords1.next()) {  //el empleado tiene tarea (idex) asiganda y con fecha asistencia >= fecha planeada
                do {
                    //conocer si la tarea ya ha iniciado
                    if (rsRecords1.getObject("FechaFinSegunJefe") != null) { //fecha fin real
                        if (fechaDt.getValue().equals(rsRecords1.getDate("FechaFinSegunJefe")) || fechaDt.getValue().before(rsRecords1.getDate("FechaFinSegunJefe"))) {
                            idexActivo = rsRecords1.getString("Idex") + "-" + rsRecords1.getString("Descripcion");
                            break;
                        }
                    }
                    else {
                        idexActivo = rsRecords1.getString("Idex") + "-" + rsRecords1.getString("Descripcion");
                        break;
                    }
                } while(rsRecords1.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al obtener IDEX ACTIVO del grupo de trabajo: " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }

        return idexActivo;
    }

    public boolean exportToExcel() {
        if (planBitacoraGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(planBitacoraGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
            fileexport = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "_" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_ASISTENCIA.xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - ASISTENCIA DIARIA TRABAJO");
    }
}
