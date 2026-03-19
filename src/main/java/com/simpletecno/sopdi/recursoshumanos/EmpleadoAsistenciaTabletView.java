package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
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
public class EmpleadoAsistenciaTabletView extends VerticalLayout implements View {
    DateField fechaDt;

    public static final String IDBITACORA_PROPERTY = "Id";
    public static final String IDEMPLEADO_PROPERTY = "IdEmpleado";
    public static final String EMPLEADO_PROPERTY = "Nombre";
    public static final String ESTATUS_PROPERTY = "Estatus";
    public static final String RAZON_PROPERTY = "Razón";
    public static final String IDEX_PROPERTY = "IDEX";

    public IndexedContainer planbBitacoraContainer = new IndexedContainer();
    Grid planBitacoraGrid;
    public IndexedContainer planBitacoraIDEXContainer = new IndexedContainer(); //SOLO PARA EL REPORTE DE ASISTENCIA CON IDEX

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    VerticalLayout mainLayout = new VerticalLayout();

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public EmpleadoAsistenciaTabletView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " ASISTENCIA DIARIA DE TRABAJO TABLET");
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

        fechaDt = new DateField("Fecha de TRABAJO:");
        fechaDt.setValue(new Date());
        fechaDt.addStyleName(ValoTheme.DATEFIELD_LARGE);
        fechaDt.setSizeFull();
        fechaDt.setDateFormat("dd-MMM-yyyy");
        fechaDt.setEnabled(true);
        fechaDt.addValueChangeListener((event) -> {
            fillPlanBitacoraGrid();
        });
        fechaDt.setShowISOWeekNumbers(true);
        fechaDt.setVisible(false);

        HorizontalLayout fechaLayout = new HorizontalLayout();
        fechaLayout.setSpacing(true);
        fechaLayout.addComponent(fechaDt);
        fechaLayout.setComponentAlignment(fechaDt, Alignment.BOTTOM_CENTER);

        detalleLayout.addComponents(fechaLayout, idexYEmpleadosLayout, botonesLayout);

        planbBitacoraContainer.addContainerProperty(IDBITACORA_PROPERTY, String.class, null);
        planbBitacoraContainer.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, null);
        planbBitacoraContainer.addContainerProperty(EMPLEADO_PROPERTY, String.class, null);
        planbBitacoraContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, "PRESENTE");
        planbBitacoraContainer.addContainerProperty(RAZON_PROPERTY, String.class, "");
        planbBitacoraContainer.addContainerProperty(IDEX_PROPERTY, String.class, null);

        planBitacoraIDEXContainer.addContainerProperty(IDBITACORA_PROPERTY, String.class, null);
        planBitacoraIDEXContainer.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, null);
        planBitacoraIDEXContainer.addContainerProperty(EMPLEADO_PROPERTY, String.class, null);
        planBitacoraIDEXContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, "PRESENTE");
        planBitacoraIDEXContainer.addContainerProperty(RAZON_PROPERTY, String.class, null);
        planBitacoraIDEXContainer.addContainerProperty(IDEX_PROPERTY, String.class, null);

        planBitacoraGrid = new Grid("ASISTENCIA BITACORA DE HOY : " + Utileria.getFechaDDMMYYYY(new Date()), planbBitacoraContainer);
        planBitacoraGrid.setImmediate(true);
        planBitacoraGrid.setSelectionMode(Grid.SelectionMode.NONE);
        planBitacoraGrid.setHeightMode(HeightMode.ROW);
        planBitacoraGrid.setHeightByRows(15);
        planBitacoraGrid.setWidth("100%");
        planBitacoraGrid.setResponsive(true);
        planBitacoraGrid.setSizeFull();
        planBitacoraGrid.addItemClickListener((event) -> {
            if (event != null) {
                if(event.isDoubleClick()) {

                    if(   String.valueOf(planbBitacoraContainer.getContainerProperty(event.getItemId(), IDEX_PROPERTY).getValue()).toUpperCase(). contains("IGSS")
                       || String.valueOf(planbBitacoraContainer.getContainerProperty(event.getItemId(), IDEX_PROPERTY).getValue()).toUpperCase().contains("SUSPENDIDO")
                       || String.valueOf(planbBitacoraContainer.getContainerProperty(event.getItemId(), IDEX_PROPERTY).getValue()).toUpperCase().contains("ENFERMEDAD")) {
                        Notification.show("Este empleado está suspendido, indicarle que debe presentarse en Administración de la e mpresa.");
                        return;
                    }

                    try {
                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                        queryString = "UPDATE empleado_asistencia SET ";
                        queryString += " Idex = '" + String.valueOf(planbBitacoraContainer.getContainerProperty(event.getItemId(), IDEX_PROPERTY).getValue()) + "'";
                        queryString += ",Estatus = 'PRESENTE'";
                        queryString += " WHERE Id = " + String.valueOf(planbBitacoraContainer.getContainerProperty(event.getItemId(), IDBITACORA_PROPERTY).getValue());

                        stQuery.executeUpdate(queryString);

                        queryString = "UPDATE proveedor_empresa SET ";
                        queryString += " EstatusTrabajo = 'PRESENTE'";
                        queryString += ",Razon = ''";
                        queryString += " WHERE IdProveedor = " + String.valueOf(planbBitacoraContainer.getContainerProperty(event.getItemId(), IDEMPLEADO_PROPERTY).getValue());

                        stQuery.executeUpdate(queryString);

                        planbBitacoraContainer.removeItem(event.getItemId());

                    } catch (Exception ex) {
                        System.out.println("Error al actualizar bitacora de empleados : " + ex);
                        ex.printStackTrace();
                        Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    }
                }
            }
        });

        planBitacoraGrid.addStyleName("largegrid");

        planBitacoraGrid.getColumn(IDBITACORA_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);
        planBitacoraGrid.getColumn(IDEMPLEADO_PROPERTY).setExpandRatio(1);
        planBitacoraGrid.getColumn(EMPLEADO_PROPERTY).setExpandRatio(3);
        planBitacoraGrid.getColumn(ESTATUS_PROPERTY).setExpandRatio(2);
        planBitacoraGrid.getColumn(RAZON_PROPERTY).setExpandRatio(3);
        planBitacoraGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);

        idexYEmpleadosLayout.addComponent(planBitacoraGrid);

        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);

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

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(true);
        comboBox.setInvalidAllowed(true);
        comboBox.setNewItemsAllowed(true);
        comboBox.clear();
        comboBox.setWidth("6em");

        try {

            queryString = " SELECT *";
            queryString += " FROM razon_ausencia";

            rsRecords = stQuery.executeQuery(queryString);

            comboBox.addItem("");

            if(!rsRecords.next()) { //NO EXISTE BITACORA PARA HOY, HAY QUE CREARLA

                comboBox.addItem(rsRecords.getString("Razon"));
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla RAZONES DE AUSENCIA : " + ex);
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }


        return comboBox;
    }

    public void fillPlanBitacoraGrid() {
        planbBitacoraContainer.removeAllItems();
        planbBitacoraContainer.removeAllItems();

        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(fechaDt.getValue());

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = " SELECT BITA.*, PROV.IdProveedor, PROV.Nombre NombreEmpleado, PROV.Cargo, PROV.TipoAsignacion";
            queryString += " FROM empleado_asistencia BITA";
            queryString += " INNER JOIN proveedor_empresa PROV ON PROV.IdProveedor = BITA.IdEmpleado ";
            queryString += " WHERE BITA.Fecha = '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += " AND   PROV.Inhabilitado = 0";
            queryString += " AND   PROV.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND   PROV.FechaIngreso <= '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += " AND   BITA.Estatus = 'AUSENTE'";
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
                    queryString = getIdexActivo(rsRecords.getString("IdEmpleado"));
                    planbBitacoraContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(queryString.split("-")[0]);
                    planbBitacoraContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    planbBitacoraContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));
                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla BITACORA DE EMPLEADOS DEL GRUPO : " + ex);
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void crearBitacora() {
        queryString = "INSERT INTO empleado_asistencia (IdEmpleado, Cargo, Fecha, Estatus, Razon, EsDefinitiva, CreadoFechaYHora, CreadoIdUsuario)";
        queryString += " SELECT IdProveedor, Cargo, '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "', 'AUSENTE', Razon, 0, current_timestamp, " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += " FROM proveedor_proveedor ";
        queryString += " WHERE EsPlanilla = 1 ";
        queryString += " AND   Inhabilitado = 0";
        queryString += " AND   EstatusTrabajo <> 'DE BAJA'";
        queryString += " AND   IdEmpresa = " + empresaId;
        queryString += " AND   ISNULL(FechaIngreso) = 0 ";
        queryString += " AND   ISNULL(FechaEgreso) = 1 ";

//System.out.println("query INSERTBITACORA=" + queryString);

        try {
            stQuery.executeUpdate(queryString);
            fillPlanBitacoraGrid(); //recursivo
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
            queryString += " AND   PROV.EsPlanilla = 1";
            queryString += " AND   PROV.Inhabilitado = 0";
            queryString += " AND   PROV.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND   PROV.FechaIngreso <= '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += " AND   BITA.Estatus = 'AUSENTE'";
            queryString += " ORDER BY PROV.Nombre";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    Object itemId = planBitacoraIDEXContainer.addItem();
                    planBitacoraIDEXContainer.getContainerProperty(itemId, IDBITACORA_PROPERTY).setValue(rsRecords.getString("Id"));
                    planBitacoraIDEXContainer.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdEmpleado"));
                    planBitacoraIDEXContainer.getContainerProperty(itemId, EMPLEADO_PROPERTY).setValue(rsRecords.getString("NombreEmpleado"));
//                    planBitacoraIDEXContainer.getContainerProperty(itemId, GRUPONOMBRE_PROPERTY).setValue(rsRecords.getString("GrupoTrabajo"));
                    planBitacoraIDEXContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    planBitacoraIDEXContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));
                    planBitacoraIDEXContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(getIdexActivo(rsRecords.getString("IdEmpleado")));
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
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String queryString = "SELECT PTI.IDEX, PTA.Descripcion, ";
            queryString += "(CASE";
            queryString += " WHEN ISNULL(FechaInicioSegunSupervisor) = 0 THEN FechaInicioSegunSupervisor";
            queryString += " WHEN ISNULL(FechaInicioSegunMaestro) = 0 THEN FechaInicioSegunMaestro";
            queryString += " WHEN ISNULL(FechaInicioSegunJefe) = 0 THEN FechaInicioSegunJefe";
            queryString += " END ) As FechaInicioReal, ";
            queryString += " (CASE ";
            queryString += " WHEN ISNULL(FechaFinSegunSupervisor) = 0 THEN FechaFinSegunSupervisor ";
            queryString += " WHEN ISNULL(FechaFinSegunMaestro) = 0 THEN FechaFinSegunMaestro ";
            queryString += " WHEN ISNULL(FechaFinSegunJefe) = 0 THEN FechaFinSegunJefe";
            queryString += " END ) As FechaFinReal";
            queryString += " FROM plan_trabajo_idex PTI ";
            queryString += " INNER JOIN plan_trabajo_idex_rh PTIR ON PTIR.IdPlanTrabajoIdex = PTI.Id ";
            queryString += " INNER JOIN project_tarea PTA ON PTA.Idex = PTI.Idex ";
            queryString += " INNER JOIN project PJ ON PJ.Id = PTA.IdProject AND PJ.Estatus = 'ACTIVO' ";
            queryString += " WHERE PTIR.IDEmpleado = " + idEmpleado;
            queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "' >= PTA.FechaInicio";

System.out.println("queryBITACORA=" + queryString);

            rsRecords1 = stQuery1.executeQuery(queryString);

            if(rsRecords1.next()) {  //el empleado tiene tarea (idex) asiganda y con fecha asistencia >= fecha planeada
                do {
                    //conocer si la tarea ya ha iniciado
                    if (rsRecords1.getObject("FechaInicioReal") != null) { //fecha inicio real
                        //conocer si la fecha de asistencia es igual o está despues de la fecha de inicio real
                        if (fechaDt.getValue().equals(rsRecords1.getDate("FechaInicioReal")) || fechaDt.getValue().after(rsRecords1.getDate("FechaInicioReal"))) {
                            //conocer si la fecha de asistencia está antes o es igual a la fecha final
                            if (rsRecords1.getObject("FechaFinReal") != null) { //fecha fin real
                                if (fechaDt.getValue().equals(rsRecords1.getDate("FechaFinReal")) || fechaDt.getValue().before(rsRecords1.getDate("FechaFinReal"))) {
                                    idexActivo = rsRecords1.getString("Idex") + "-" + rsRecords1.getString("Descripcion");
                                    break;
                                }
                            } else {
                                idexActivo = rsRecords1.getString("Idex") + "-" + rsRecords1.getString("Descripcion");
                                break;
                            }
                        }
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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - ASISTENCIA DIARIA TRABAJO");
    }
}
