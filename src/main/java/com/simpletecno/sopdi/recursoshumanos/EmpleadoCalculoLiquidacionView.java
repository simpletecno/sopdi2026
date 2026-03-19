package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
@SuppressWarnings("ALL")
public class EmpleadoCalculoLiquidacionView extends VerticalLayout implements View {
    public static final String ID_PLANILLA = "Id";
    public static final String CORRELATIVO_PLANILLA = "Correlativo";
    public static final String TIPO_PLANILLA = "Tipo";
    public static final String FECHA_INICIAL_PLANILLA = "F. Inicial";
    public static final String FECHA_FINAL_PLANILLA = "F. Final";
    public static final String MES_PLANILLA = "Año y mes";
    public static final String DESCRIPCION_PLANILLA = "Descripción";
    public static final String ESTATUS_PLANILLA = "Estatus";
    public static final String TOTAL_EMPLEADOS_PLANILLA = "Total empleados";
    public static final String TOTAL_PAGADO_PLANILLA = "Total pagado";
    public static final String FECHA_INICIALSF_PLANILLA = "FISF";
    public static final String FECHA_FINALSF_PLANILLA = "FFSF";

    public static final String IDEMPLEADO = "IdEmpleado";
    public static final String EMPLEADO = "Nombre";
    public static final String CARGO = "Cargo";

    public static final String SUELDO_BASE = "Sueldo base";
    public static final String DIAS_LABORADOS_MES = "Dias laborados";
    public static final String LICENCIA = "Licencias";
    public static final String DIAS_LABORADOS_TOTALES = "Total Dias Laborados";
    public static final String DIAS_GOZADOS_VACACIONES = "Dias Vacaciones Gozadas";
    public static final String DIAS_VACACIONES_PENDIENTES = "Dias Vacaciones Pendientes";
    public static final String SUELDO_DEVENGADO = "Devengado";
    public static final String HORAS_EXTRA = "Hrs Extra";
    public static final String HORAS_EXTRAII = "Hrs ExtraII";
    public static final String EXTRAORDINARIO = "Extraordinario";
    public static final String BONO1 = "37-2001";
    public static final String BONO2 = "78-89";
    public static final String BONO3 = "EVENTOS";
    public static final String BONO4 = "BONO14";
    public static final String BONO5 = "AGUINALDO";
    public static final String PROVISION_AG_B14 = "Provisión";
    public static final String PROVISION_PATIGSS = "Cuota Pat Iggs";
    public static final String INDEMNIZACION = "Indemnización";
    public static final String VACACIONES = "Vacaciones";
    public static final String INGRESOS = "Tot.Ingresos";
    public static final String DESCUENTO1 = "CuotLabIgss";
    public static final String DESCUENTO2 = "Anticipo";
    //public static final String DESCUENTO3 = "ISR";
    public static final String DESCUENTO4 = "Descto4";
    public static final String DESCUENTO5 = "Descto5";
    public static final String DESCUENTOS = "Tot.Desctos.";
    public static final String LIQUIDO = "Liquido";
    public static final String CODIGOPARTIDA = "CodigoPartida";
    public static final String CHEQUE = "Cheque";

    public IndexedContainer planillaContainer = new IndexedContainer();
    Grid planillaGrid;

    public IndexedContainer planillaDetalleContainer = new IndexedContainer();
    Grid planillaDetalleGrid;

    Button nuevaPlanillaBtn = new Button("Nueva");
    Button editarPlanillaBtn = new Button("Editar");
    Button eliminarPlanillaBtn = new Button("Eliminar");
    Button guardarPlanillaBtn = new Button("Guardar");
    Button cancelarPlanillaBtn = new Button("Cancelar");
    Button calcularPlanillaBtn = new Button("Calcular");
    Button generarPlanillaBtn = new Button("Generar");
    NumberField correlativoInicialChequeTxt = new NumberField();
    DateField fechaChequeDt = new DateField();

    DateField fechaInicioDt = new DateField("Fecha inicial :");
    DateField fechaFinDt = new DateField("Fecha final :");
    ComboBox tipoPlanillaCbx = new ComboBox("Tipo :");
    TextField descripcionPlanillaTxt = new TextField("Descripción:");

    UI mainUI;
    Statement stQuery, stQuery1, stQuery2;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords, rsRecords1, rsRecords2, rsRecords3;
    String queryString;
    boolean nuevaPlanilla;

    DecimalFormat df = new DecimalFormat("######0.00");
    //double montoBaseRetenerISR = 0.00;
    double factorHoraExtra = 0.00;
    double factorHoraExtraDoble = 0.00;
    int factorDiasMes = 30;
    double factorDiasAnio = 365;;
    double porcentajeCuotaLaboralIGSS = 4.83;
    double porcentajeCuotaPatronalIGSS = 12.67;
    double valorEvento = 0.00;

    String idProveedor = "";

    long daysDiff = 0;
    GregorianCalendar dateBefore; //tambien fecha de ingreso
    GregorianCalendar dateAfter; //tambien fecha fin planilla

    int totalEmpleados = 0;
    double totalPagado = 0.00;

    VerticalLayout mainLayout = new VerticalLayout();

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public EmpleadoCalculoLiquidacionView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label("CALCULO DE PLANILLA LIQUIDACIONES");
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

        ComboBox empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("95%");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        empresaCbx.addItem(((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId());
        empresaCbx.setItemCaption(((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId(), ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName());

        empresaCbx.select(((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId());

        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        addComponent(mainLayout);
        setComponentAlignment(mainLayout, Alignment.TOP_CENTER);

        createPlanillasGrid();
        createPlanillaDetalleGrid();

        fillPlanillaGrid(0);
    }

    public void createPlanillasGrid() {
        HorizontalLayout planillasLayout = new HorizontalLayout();
        planillasLayout.setWidth("100%");
        planillasLayout.addStyleName("rcorners3");
        planillasLayout.setSpacing(true);

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        HorizontalLayout planillaDatosLayout = new HorizontalLayout();
        planillaDatosLayout.setWidth("100%");
        planillaDatosLayout.addStyleName("rcorners3");
        planillaDatosLayout.setSpacing(true);
        planillaDatosLayout.setVisible(false);

        mainLayout.addComponents(planillasLayout, botonesLayout, planillaDatosLayout);

        planillaContainer.addContainerProperty(ID_PLANILLA, String.class, "");
        planillaContainer.addContainerProperty(CORRELATIVO_PLANILLA, String.class, "");
        planillaContainer.addContainerProperty(TIPO_PLANILLA, String.class, "");
        planillaContainer.addContainerProperty(FECHA_INICIAL_PLANILLA, String.class, "");
        planillaContainer.addContainerProperty(FECHA_FINAL_PLANILLA, String.class, "");
        planillaContainer.addContainerProperty(MES_PLANILLA, String.class, "");
        planillaContainer.addContainerProperty(DESCRIPCION_PLANILLA, String.class, "");
        planillaContainer.addContainerProperty(ESTATUS_PLANILLA, String.class, "EN PROCESO");
        planillaContainer.addContainerProperty(TOTAL_EMPLEADOS_PLANILLA, Integer.class, 0);
        planillaContainer.addContainerProperty(TOTAL_PAGADO_PLANILLA, Double.class, 0);
        planillaContainer.addContainerProperty(FECHA_INICIALSF_PLANILLA, String.class, "");
        planillaContainer.addContainerProperty(FECHA_FINALSF_PLANILLA, String.class, "");

        planillaGrid = new Grid("PLANILLAS", planillaContainer);
        planillaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        planillaGrid.setHeightMode(HeightMode.ROW);
        planillaGrid.setHeightByRows(7);
        planillaGrid.setSizeFull();

        planillaGrid.getColumn(ID_PLANILLA).setExpandRatio(1).setHidden(true).setHidable(true);

        planillaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (TOTAL_EMPLEADOS_PLANILLA.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (TOTAL_PAGADO_PLANILLA.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        planillaGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event
            ) {
                if (planillaGrid.getSelectedRow() != null) {
                    editarPlanillaBtn.setEnabled(true);
                    eliminarPlanillaBtn.setEnabled(true);
                    calcularPlanillaBtn.setEnabled(true);
                    generarPlanillaBtn.setEnabled(true);
                    correlativoInicialChequeTxt.setVisible(true);
                    fechaChequeDt.setVisible(true);

                    if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ESTATUS_PLANILLA).getValue()).equals("GENERADA")) {
                        correlativoInicialChequeTxt.setVisible(false);
                        fechaChequeDt.setVisible(false);
                        editarPlanillaBtn.setEnabled(false);
                        eliminarPlanillaBtn.setEnabled(false);
                        calcularPlanillaBtn.setEnabled(false);
                        generarPlanillaBtn.setEnabled(false);
                    }

                    fillPlanillaDetalleGrid();
                }
            };
        });

        planillaGrid.getColumn(FECHA_INICIALSF_PLANILLA).setHidable(true).setHidden(true);
        planillaGrid.getColumn(FECHA_FINALSF_PLANILLA).setHidable(true).setHidden(true);

        planillasLayout.addComponent(planillaGrid);

//----------------------------------------BOTONES----------------------------------------
        nuevaPlanillaBtn.setIcon(FontAwesome.PLUS);
        nuevaPlanillaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        nuevaPlanillaBtn.setDescription("Nueva planilla.");
        nuevaPlanillaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                planillaDatosLayout.setVisible(true);
                nuevaPlanilla = true;
            }
        });

        editarPlanillaBtn.setIcon(FontAwesome.EDIT);
        editarPlanillaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        editarPlanillaBtn.setDescription("Editar planilla.");
        editarPlanillaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(planillaGrid.getSelectedRow() == null) {
                    Notification.show("Por favor seleccione una planilla para editar.", Notification.Type.HUMANIZED_MESSAGE);
                    return;
                }
                planillaDatosLayout.setVisible(true);
                nuevaPlanilla = false;

                try {
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                    queryString = "SELECT *";
                    queryString += " FROM planilla_encabezado";
                    queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += " AND Id = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

                    rsRecords = stQuery.executeQuery(queryString);

                    rsRecords.next();

                    fechaInicioDt.setValue(rsRecords.getDate("FechaInicio"));
                    fechaFinDt.setValue(rsRecords.getDate("FechaFin"));
                    tipoPlanillaCbx.select(rsRecords.getString("Tipo"));
                    descripcionPlanillaTxt.setValue(rsRecords.getString("Descripcion"));
//                    estatusPlanillaCbx.setReadOnly(false);
//                    estatusPlanillaCbx.select(rsRecords.getString("Estatus"));
//                    estatusPlanillaCbx.setReadOnly(true);

                } catch (Exception ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla PLANILLAS : " + ex);
                    ex.printStackTrace();
                    Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            }
        });

        eliminarPlanillaBtn.setIcon(FontAwesome.TRASH);
        eliminarPlanillaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        eliminarPlanillaBtn.setDescription("Eliminar planilla.");
        eliminarPlanillaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (planillaGrid.getSelectedRow() == null) {
                    return;
                }

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar la planilla ?",
                        "SI", "NO", new ConfirmDialog.Listener() {
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    try {
                                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                        
                                        queryString = "DELETE ";
                                        queryString += " FROM planilla_detalle";
                                        queryString += " WHERE IdPlanilla = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

                                        stQuery.executeUpdate(queryString);

                                        queryString = "DELETE ";
                                        queryString += " FROM planilla_encabezado";
                                        queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                                        queryString += " AND Id = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

                                        stQuery.executeUpdate(queryString);

                                        fillPlanillaGrid(0);

                                    } catch (Exception ex) {
                                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla PLANILLAS : " + ex);
                                        ex.printStackTrace();
                                        Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                                    }
                                }
                                else {
                                    Notification.show("OPERACION CANCELADA POR USUARIO", Notification.Type.HUMANIZED_MESSAGE);
                                }
                            }
                        });
            }
        });

        calcularPlanillaBtn.setIcon(FontAwesome.CALCULATOR);
        calcularPlanillaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        calcularPlanillaBtn.setDescription("Calcular planilla.");
        calcularPlanillaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CALCULAR la planilla ?",
                        "SI", "NO", new ConfirmDialog.Listener() {
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    calcularPlanilla();
                                }
                                else {
                                    Notification.show("OPERACION CANCELADA POR USUARIO", Notification.Type.HUMANIZED_MESSAGE);
                                }
                            }
                        }
                );
            }
        });

        generarPlanillaBtn.setIcon(FontAwesome.BOOK);
        generarPlanillaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        generarPlanillaBtn.setDescription("Generar (cheques) planilla.");
        generarPlanillaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(correlativoInicialChequeTxt.getDoubleValueDoNotThrow() == 0) {
                    Notification.show("Por favor ingrese el siguiente número de cheque.", Notification.Type.HUMANIZED_MESSAGE);
                    correlativoInicialChequeTxt.focus();
                    return;
                }
                try {
                    if (fechaChequeDt.getValue().before(new SimpleDateFormat("yyyy-mm-dd").parse(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_INICIALSF_PLANILLA).getValue())))) {
                        Notification.show("La fecha de cheques no puede ser menor a la fecha inicial de planilla.", Notification.Type.HUMANIZED_MESSAGE);
                        fechaChequeDt.focus();
                        return;
                    }
                    if (fechaChequeDt.getValue().equals(new SimpleDateFormat("yyyy-mm-dd").parse(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_INICIALSF_PLANILLA).getValue())))) {
                        Notification.show("La fecha de cheques no puede ser igual a la fecha inicial de planilla.", Notification.Type.HUMANIZED_MESSAGE);
                        fechaChequeDt.focus();
                        return;
                    }
                }
                catch(Exception ex11) {
                    Notification.show("LA FECHA DE CHEQUES NO ES CORRECTA, REVISE!!", Notification.Type.ERROR_MESSAGE);
                    fechaChequeDt.focus();
                    return;
                }
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de generar la planilla ?",
                        "SI", "NO", new ConfirmDialog.Listener() {
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    try {
                                        System.out.println("Entrando a generarPlanilla");
                                        generarPlanilla();
                                    } catch (Exception e) {
                                        e.printStackTrace(); // o mostrar una Notification
                                        Notification.show("Error en generarPlanilla(): " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
                                    }
                                } else {
                                    Notification.show("OPERACION CANCELADA POR USUARIO", Notification.Type.HUMANIZED_MESSAGE);
                                }
                            }
                        }
                );
            }
        });

        correlativoInicialChequeTxt.setInputPrompt("Correlativo inicial de cheques");
        correlativoInicialChequeTxt.setDescription("Numero inicial de cheque");
        correlativoInicialChequeTxt.setDecimalAllowed(false);
        correlativoInicialChequeTxt.setDecimalPrecision(0);
        correlativoInicialChequeTxt.setDecimalSeparatorAlwaysShown(false);
        correlativoInicialChequeTxt.setGroupingUsed(true);
        correlativoInicialChequeTxt.setGroupingSeparator(',');
        correlativoInicialChequeTxt.setGroupingSize(3);
        correlativoInicialChequeTxt.setImmediate(true);
        correlativoInicialChequeTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        correlativoInicialChequeTxt.setWidth("100%");

        fechaChequeDt.setDateFormat("dd/MMM/yyyy");
        fechaChequeDt.setDescription("Fecha de cheque");

        botonesLayout.addComponents(nuevaPlanillaBtn, editarPlanillaBtn, eliminarPlanillaBtn, calcularPlanillaBtn, correlativoInicialChequeTxt, fechaChequeDt, generarPlanillaBtn);

//----------------------------------------CAMPOS----------------------------------------

        tipoPlanillaCbx.setWidth("100%");
        tipoPlanillaCbx.setTextInputAllowed(false);
        tipoPlanillaCbx.setNewItemsAllowed(false);
        tipoPlanillaCbx.setNullSelectionAllowed(false);
        tipoPlanillaCbx.addItem("Liquidación");
        tipoPlanillaCbx.select("Liquidación");

        fechaInicioDt.setWidth("100%");
        fechaInicioDt.setDateFormat("dd/MMM/yyyy");
        fechaInicioDt.setValue(Utileria.getPrimerDiaDelMes());
        fechaFinDt.setWidth("100%");
        fechaFinDt.setDateFormat("dd/MMM/yyyy");
        fechaFinDt.setValue((new java.util.Date().from(Utileria.getUltimoDiaDelMes().toInstant().minus(16,ChronoUnit.DAYS))));

        descripcionPlanillaTxt.setWidth("100%");
        descripcionPlanillaTxt.setMaxLength(128);
        descripcionPlanillaTxt.setDescription("Nombre descriptivo de la planilla");

        guardarPlanillaBtn.setIcon(FontAwesome.SAVE);
        guardarPlanillaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        guardarPlanillaBtn.setDescription("Guardar datos planilla.");
        guardarPlanillaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de guardar los datos de planilla ?, tome en cuenta que los datos de la planilla serán eliminados y vueltos a crear.",
                        "SI", "NO", new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            if (fechaInicioDt.getValue() == null) {
                                Notification.show("Por favor seleccione la fecha inicial...", Notification.Type.WARNING_MESSAGE);
                                fechaInicioDt.focus();
                                return;
                            }
                            if (fechaFinDt.getValue() == null) {
                                Notification.show("Por favor seleccione la fecha final...", Notification.Type.WARNING_MESSAGE);
                                fechaFinDt.focus();
                                return;
                            }
                            if (fechaFinDt.getValue().before(fechaInicioDt.getValue())) {
                                Notification.show("La fecha final no puede ser menor a la fecha inicial, revise por favor...", Notification.Type.WARNING_MESSAGE);
                                fechaFinDt.focus();
                                return;
                            }

                            if (descripcionPlanillaTxt.getValue() == null || descripcionPlanillaTxt.getValue().isEmpty()) {
                                Notification.show("Por favor escriba la descripción..", Notification.Type.WARNING_MESSAGE);
                                descripcionPlanillaTxt.focus();
                            }

                            if(nuevaPlanilla) {
                                crearPlanilla();
                            }
                            else {
                                if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ESTATUS_PLANILLA).getValue()).equals("AUTORIAZADA")){
                                    //
                                }
                                guardarPlanilla(
                                        Integer.valueOf(
                                                String.valueOf(
                                                        planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue())));
                            }

                            planillaDatosLayout.setVisible(false);

                        }//if dialog is confirmed
                    } // on close
                }); // confirm dialog
            }
        }); //add listener

        cancelarPlanillaBtn.setIcon(FontAwesome.UNDO);
        cancelarPlanillaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        cancelarPlanillaBtn.setDescription("Cancelar planilla.");
        cancelarPlanillaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                planillaDatosLayout.setVisible(false);
            }
        });

        planillaDatosLayout.addComponents(tipoPlanillaCbx, fechaInicioDt, fechaFinDt, descripcionPlanillaTxt, guardarPlanillaBtn, cancelarPlanillaBtn);
        planillaDatosLayout.setComponentAlignment(tipoPlanillaCbx, Alignment.BOTTOM_LEFT);
        planillaDatosLayout.setComponentAlignment(fechaInicioDt, Alignment.BOTTOM_LEFT);
        planillaDatosLayout.setComponentAlignment(fechaFinDt, Alignment.BOTTOM_LEFT);
        planillaDatosLayout.setComponentAlignment(descripcionPlanillaTxt, Alignment.BOTTOM_CENTER);
        planillaDatosLayout.setComponentAlignment(guardarPlanillaBtn, Alignment.BOTTOM_RIGHT);
        planillaDatosLayout.setComponentAlignment(cancelarPlanillaBtn, Alignment.BOTTOM_RIGHT);

        planillaDatosLayout.setExpandRatio(tipoPlanillaCbx, 2.0f);
        planillaDatosLayout.setExpandRatio(fechaInicioDt, 1.0f);
        planillaDatosLayout.setExpandRatio(fechaFinDt, 1.0f);
        planillaDatosLayout.setExpandRatio(descripcionPlanillaTxt, 3.0f);
    }

    public void createPlanillaDetalleGrid() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.setHeightUndefined();
        detalleLayout.addStyleName("rcorners2");
        detalleLayout.setSpacing(true);

        HorizontalLayout empleadosLayout = new HorizontalLayout();
        empleadosLayout.setWidth("100%");
        empleadosLayout.addStyleName("rcorners3");
        empleadosLayout.setSpacing(true);

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        detalleLayout.addComponents(empleadosLayout, botonesLayout);

        planillaDetalleContainer.addContainerProperty(ID_PLANILLA, String.class, "0");
        planillaDetalleContainer.addContainerProperty(IDEMPLEADO, String.class, "");
        planillaDetalleContainer.addContainerProperty(EMPLEADO, String.class, "");
        planillaDetalleContainer.addContainerProperty(CARGO, String.class, "");
        planillaDetalleContainer.addContainerProperty(SUELDO_BASE, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DIAS_LABORADOS_MES, Integer.class, 0);
        planillaDetalleContainer.addContainerProperty(LICENCIA, Integer.class, 0);
        planillaDetalleContainer.addContainerProperty(DIAS_LABORADOS_TOTALES, Integer.class, 0);
        planillaDetalleContainer.addContainerProperty(DIAS_GOZADOS_VACACIONES, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DIAS_VACACIONES_PENDIENTES, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(SUELDO_DEVENGADO, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(HORAS_EXTRA, Integer.class, 0);
        planillaDetalleContainer.addContainerProperty(HORAS_EXTRAII, Integer.class, 0);
        planillaDetalleContainer.addContainerProperty(EXTRAORDINARIO, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO1, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO2, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO3, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO4, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO5, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(PROVISION_AG_B14, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(PROVISION_PATIGSS, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(INDEMNIZACION, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(VACACIONES, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(INGRESOS, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTO1, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTO2, Double.class, 0.00);
        //planillaDetalleContainer.addContainerProperty(DESCUENTO3, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTO4, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTO5, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTOS, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(LIQUIDO, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(CODIGOPARTIDA, String.class, "");
        planillaDetalleContainer.addContainerProperty(CHEQUE, String.class, "");

        planillaDetalleGrid = new Grid("EMPLEADOS DE PLANILLA DE : " + tipoPlanillaCbx.getValue(), planillaDetalleContainer);
        planillaDetalleGrid.setImmediate(true);
        planillaDetalleGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        planillaDetalleGrid.setHeightMode(HeightMode.ROW);
        planillaDetalleGrid.setHeightByRows(10);
        planillaDetalleGrid.setSizeFull();

        planillaDetalleGrid.getColumn(ID_PLANILLA).setExpandRatio(1).setHidden(true).setHidable(true);
        planillaDetalleGrid.getColumn(IDEMPLEADO).setExpandRatio(1);
        planillaDetalleGrid.getColumn(EMPLEADO).setExpandRatio(4);
        planillaDetalleGrid.getColumn(DIAS_LABORADOS_MES).setExpandRatio(1);
        planillaDetalleGrid.getColumn(LICENCIA).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DIAS_LABORADOS_TOTALES).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DIAS_GOZADOS_VACACIONES).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DIAS_VACACIONES_PENDIENTES).setExpandRatio(1);
        planillaDetalleGrid.getColumn(SUELDO_BASE).setExpandRatio(1);
        planillaDetalleGrid.getColumn(HORAS_EXTRA).setExpandRatio(1);
        planillaDetalleGrid.getColumn(HORAS_EXTRAII).setExpandRatio(1);
        planillaDetalleGrid.getColumn(EXTRAORDINARIO).setExpandRatio(1);
        planillaDetalleGrid.getColumn(SUELDO_DEVENGADO).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO1).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO2).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO3).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO4).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO5).setExpandRatio(1);
        planillaDetalleGrid.getColumn(PROVISION_AG_B14).setExpandRatio(1);
        planillaDetalleGrid.getColumn(PROVISION_PATIGSS).setExpandRatio(1);
        planillaDetalleGrid.getColumn(INDEMNIZACION).setExpandRatio(1);
        planillaDetalleGrid.getColumn(VACACIONES).setExpandRatio(1);
        planillaDetalleGrid.getColumn(INGRESOS).setExpandRatio(2);
        planillaDetalleGrid.getColumn(DESCUENTO1).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DESCUENTO2).setExpandRatio(1);
        //planillaDetalleGrid.getColumn(DESCUENTO3).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DESCUENTO4).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DESCUENTO5).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DESCUENTOS).setExpandRatio(2);
        planillaDetalleGrid.getColumn(LIQUIDO).setExpandRatio(2);
        planillaDetalleGrid.getColumn(CODIGOPARTIDA).setExpandRatio(1);
        planillaDetalleGrid.getColumn(CHEQUE).setExpandRatio(1);

        planillaDetalleGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DIAS_LABORADOS_MES.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (LICENCIA.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (DIAS_LABORADOS_TOTALES.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (DIAS_GOZADOS_VACACIONES.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (DIAS_VACACIONES_PENDIENTES.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (HORAS_EXTRA.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (SUELDO_BASE.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SUELDO_DEVENGADO.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (EXTRAORDINARIO.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (BONO1.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (BONO2.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (BONO3.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (BONO4.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (BONO5.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROVISION_AG_B14.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if(PROVISION_PATIGSS.equals(cellReference.getPropertyId())){
                return "rightalign";
            } else if(INDEMNIZACION.equals(cellReference.getPropertyId())){
                return "rightalign";
            } else if (VACACIONES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (INGRESOS.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DESCUENTO1.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DESCUENTO2.equals(cellReference.getPropertyId())) {
                return "rightalign";
            //} else if (DESCUENTO3.equals(cellReference.getPropertyId())) {
            //    return "rightalign";
            } else if (DESCUENTO4.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DESCUENTO5.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LIQUIDO.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        planillaDetalleGrid.addItemClickListener((event) -> {
            if (event != null) {
                if (event.isDoubleClick()) {
                    if(planillaDetalleGrid.getSelectedRow() == null) {
                        return;
                    }
                    EmpleadoPlanillaExtraForm empleadoPlanillaExtraForm =
                            new EmpleadoPlanillaExtraForm(
                                    event.getItemId(),
                                    String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue()),
                                    String.valueOf(planillaDetalleContainer.getContainerProperty(planillaDetalleGrid.getSelectedRow(), IDEMPLEADO).getValue())
                                            + " " + String.valueOf(planillaDetalleContainer.getContainerProperty(planillaDetalleGrid.getSelectedRow(), EMPLEADO).getValue()),
                                    String.valueOf(planillaDetalleContainer.getContainerProperty(planillaDetalleGrid.getSelectedRow(), IDEMPLEADO).getValue()),
                                    1
                            );
                    empleadoPlanillaExtraForm.setModal(true);
                    UI.getCurrent().addWindow(empleadoPlanillaExtraForm);
                    empleadoPlanillaExtraForm.center();
                    empleadoPlanillaExtraForm.montoTxt.focus();
                }
            }
        });

        empleadosLayout.addComponent(planillaDetalleGrid);

        Button exportPlanillaBtn = new Button("Exportar plantilla a Excel");
        exportPlanillaBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportPlanillaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        exportPlanillaBtn.setDescription("Exportar a Excel");
        exportPlanillaBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (planillaDetalleContainer.size() == 0) {
                Notification notif = new Notification("No hay planilla.",
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

        Label informativoaLbl = new Label("*El dia \"DE BAJA\" cuenta como dia laborado");

        botonesLayout.addComponents(informativoaLbl, exportPlanillaBtn);
        botonesLayout.setComponentAlignment(informativoaLbl, Alignment.TOP_LEFT);
        botonesLayout.setComponentAlignment(exportPlanillaBtn, Alignment.BOTTOM_RIGHT);
        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);
    }

    private void crearPlanilla() {

        int planillId = crearPlanillaEncabezado();

        fillPlanillaGrid(planillId);

        crearPlanillaDetalle(planillId);

    }

    private int crearPlanillaEncabezado() {
        String queryString;

        int planillaId = 0;

        try {

            if(!nuevaPlanilla && planillaDetalleGrid.getSelectedRow() != null) {
                queryString = "DELETE FROM planilla_detalle ";
                queryString += " WHERE IdPlanilla = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

                stQuery.executeUpdate(queryString);

                queryString = "DELETE FROM planilla_encabezado ";
                queryString += " WHERE IdP = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

                stQuery.executeUpdate(queryString);

            }

            queryString = "  INSERT INTO planilla_encabezado (IdEmpresa, Descripcion, FechaInicio, FechaFin, ";
            queryString += " AnioMes, Correlativo, Tipo, CreadoUsuario, CreadoFechaYHora, IncluyeAguinaldo, IncluyeBono14";
            queryString += ")";
            queryString += " VALUES ";
            queryString += "(";
            queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += ",'" + descripcionPlanillaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";
            queryString += "," + new Utileria().getFechaYYYYMM(fechaInicioDt.getValue());
            queryString += ",(SELECT IFNULL((MAX(A.Correlativo) + 1), 1) FROM planilla_encabezado A WHERE A.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + ")";
            queryString += ",'" + tipoPlanillaCbx.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",'S'";
            queryString += ",'S'";
            queryString += ")";

//System.out.println("queryString = " + queryString);

            stPreparedQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stPreparedQuery.executeUpdate();
            rsRecords = stPreparedQuery.getGeneratedKeys();
            rsRecords.next();
            planillaId = rsRecords.getInt(1);

            Notification.show("PLANILLA guardada exitosamente!", Notification.Type.HUMANIZED_MESSAGE);

        } catch (Exception exPlanilla) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,"Error al insertar ENCABEZADO DE PLANILLA : " + exPlanilla.getMessage());
            Notification notif = new Notification("ERROR AL GUARDAR DATOS ENCABEZADO DE PLANILLA!",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());
            exPlanilla.printStackTrace();
        }

        return planillaId;
    }

    private void crearPlanillaDetalle(int planillaId) {
        totalEmpleados = 0;

        planillaDetalleContainer.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            //0 leer datos de planilla
            queryString = "SELECT *";
            queryString += " FROM planilla_encabezado";
            queryString += " WHERE Id = " + planillaId;
            queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

            rsRecords = stQuery.executeQuery(queryString);

            if(!rsRecords.next()) {
                Notification.show("ERROR,  no se encontro planilla!", Notification.Type.ERROR_MESSAGE);
                return;
            }

            long diffInMillies = Math.abs(rsRecords.getDate("FechaInicio").getTime() - rsRecords.getDate("FechaFin").getTime());
            long diasPlanilla = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

            //1 parametros de planilla
            queryString = "SELECT *";
            queryString += " FROM planilla_parametros";
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

            rsRecords = stQuery.executeQuery(queryString);

            if(!rsRecords.next()) {
                Notification.show("ERROR,  no se encontraron parámetros de configuraciones de planilla de esta empresa!", Notification.Type.ERROR_MESSAGE);
                return;
            }

            //montoBaseRetenerISR = rsRecords.getDouble("MontoBaseRetenerISR");
            factorHoraExtra = rsRecords.getDouble("FactorHoraExtra");
            factorHoraExtraDoble = rsRecords.getDouble("FactorHoraExtraII");
            factorDiasMes = rsRecords.getInt("FactorDiasMes");
            factorDiasAnio = rsRecords.getInt("FactorDiasAnio");
            porcentajeCuotaLaboralIGSS = rsRecords.getDouble("PorcentajeCuotaLaboralIgss");
            porcentajeCuotaPatronalIGSS = rsRecords.getDouble("PorcentajeCuotaPatronalIgss");
            valorEvento = rsRecords.getDouble("ValorEvento");

            totalPagado = 0.00;

            // 2 : query por cada empleado activo
            queryString = "SELECT *";
            queryString += " FROM proveedor_empresa";
            queryString += " WHERE EsPlanilla = 1";
            queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND EstatusTrabajo IN ('DE BAJA')";
            queryString += " AND ISNULL(FechaIngreso) = 0";
            queryString += " AND FechaEgreso Between '" + Utileria.getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
            queryString += "               And '" + Utileria.getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";
            queryString += " AND IdPlanillaLiquidacion = 0"; // Que no haya sido Liquidado | Se le asigna IdPlanillaLiquidacion al generar o terminar la planilla
            queryString += " ORDER BY IdProveedor";

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {

                java.util.Date fechaFinPlanilla;
                java.util.Date fechaIngreso;
                java.util.Date fechaEgreso;
                int[] diasLaborados = new int[2]; // 0: Dias Totales (Inicio A Fin) | 1: Dias Totales (Este Mes)
                double diasVacaciones = 0.0;
                int horasExtra;
                int horasExtraDoble;
                int eventos;
                double sueldoBase;
                double ordinario;
                double extraOrdinario;
                double[] vacaciones;
                double totalIngresos;
                double totalEgresos;
                double liquidoRecibir;
                double bonificaciones[] = new double[5]; // 0: 37-2001 | 1: 78-89 | 2: EVENTOS | 3: BONO 14 | 4: Aguinaldo
                double indemnizacion;
                double descuentos[] = new double[5];
                double provision;
                double cuotaPatronalIgss;
                do {

                    idProveedor = rsRecords.getString("IdProveedor");
                    fechaIngreso = rsRecords.getDate("FechaIngreso");
                    fechaEgreso = rsRecords.getDate("FechaEgreso");

                    stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                    // 2.1 : query por cada empleado buscar si ya está en otra planilla del aniomes...del mismo tipo de planilla..
                    queryString = "SELECT pla.*";
                    queryString += " FROM planilla_encabezado pla ";
                    queryString += " INNER JOIN planilla_detalle plade ON plade.IdPlanilla = pla.Id";
                    queryString += " WHERE plade.IdEmpleado = " + idProveedor;
                    queryString += " AND pla.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += " AND pla.AnioMes = " + new Utileria().getFechaHoraSinFormato(fechaInicioDt.getValue());
                    queryString += " AND pla.Tipo = '" + tipoPlanillaCbx.getValue() + "'";
                    queryString += " AND pla.Estatus <> 'EN PROCESO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(rsRecords1.next()) {
System.out.println("Empleado fuera de planilla : " + idProveedor + " " + rsRecords1.getString("Nombre"));
                        continue;
                    }

                    diasLaborados = getDiasTrabajados();
                    horasExtra = 0;
                    horasExtraDoble = 0;
                    eventos = 0;
                    sueldoBase = 0.00;
                    ordinario = 0.00;
                    extraOrdinario = 0.00;
                    vacaciones = new double[3];
                    totalIngresos = 0.00;
                    totalEgresos = 0.00;
                    liquidoRecibir = 0.00;
                    bonificaciones[0] = 0.00;bonificaciones[1] = 0.00;bonificaciones[2] = 0.00;bonificaciones[3] = 0.00;bonificaciones[4] = 0.00;
                    indemnizacion = 0.00;
                    descuentos[0] = 0.00;descuentos[1] = 0.00;descuentos[2] = 0.00;descuentos[3] = 0.00;descuentos[4] = 0.00;

                    // 3 : query de bitacora para contar las horas extra
                    queryString = " SELECT IFNULL(SUM(HorasExtra), 0) TOTALHORASEXTRAS, IFNULL(SUM(HorasExtraDoble), 0) TOTALHORASEXTRASDOBLE";
                    queryString += " FROM empleado_asistencia";
                    queryString += " WHERE IdEmpleado = " + idProveedor;
                    queryString += " AND Fecha Between '" + Utileria.getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
                    queryString += "               And '" + Utileria.getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";

                    rsRecords1 = stQuery1.executeQuery(queryString);
//if(rsRecords.getString("IdProveedor").equals("92310")) {
//    System.out.println(rsRecords.getString("Nombre"));
//}
                    if(rsRecords1.next()) {
                        //horasExtra = rsRecords1.getInt("TOTALHORASEXTRAS");
                        //horasExtraDoble = rsRecords1.getInt("TOTALHORASEXTRASDOBLE");
                        horasExtraDoble = 0;
                        horasExtra = 0;
                    }

                    // 3.1 : query de bitacora para contar los evnetos
                    queryString = " SELECT IFNULL(SUM(Eventos), 0) TOTALEVENTOS";
                    queryString += " FROM empleado_asistencia";
                    queryString += " WHERE IdEmpleado = " + idProveedor;
                    queryString += " AND Fecha Between '" + Utileria.getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
                    queryString += "               And '" + Utileria.getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(rsRecords1.next()) {
                        eventos = rsRecords1.getInt("TOTALEVENTOS");
                    }

                    try {
                        dateBefore = new GregorianCalendar();
                        dateBefore.setTime(fechaIngreso);

                        dateAfter = new GregorianCalendar();
                        dateAfter.setTime(fechaEgreso);

                        dateAfter.setTime(fechaInicioDt.getValue());
                        dateAfter.add(GregorianCalendar.DATE, -1);


                        long dateBeforeInMs = dateBefore.getTimeInMillis();
                        long dateAfterInMs = dateAfter.getTimeInMillis();

                        long timeDiff = Math.abs(dateAfterInMs - dateBeforeInMs);

                        daysDiff = 1 + TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

                    } catch(Exception e){
                        e.printStackTrace();
                        Notification.show("ERROR : al calcular los dias : " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
                        return;
                    }

                    // Sueldo a pagar y bonos mensuales
                    double[] ingresos = getIngresosFijos();
                    ordinario = ingresos[0];
                    sueldoBase = ingresos[1];

                    bonificaciones[0] = ingresos[2];
                    bonificaciones[1] = ingresos[3];


                    // Vacaciones
                    vacaciones = getVacaciones();

                    //BONO14 O AGUINALDO
                    double[] bonos = getBonos_1();
                    bonificaciones[3] = bonos[1];
                    bonificaciones[4] = bonos[0];

                    // ordinario = (ordinario / factorDiasMes) * diasLaborados;  PARA HORAS EXTRA ES EL BASE SIN DESCONTAR DIAS
                    // 7 : calculo de extraordinario (horas_extra * valor_horas_extr
                    extraOrdinario = extraOrdinario = ((Utileria.round((sueldoBase / factorDiasMes) / 8) * factorHoraExtra) * horasExtra);
                    extraOrdinario += ((Utileria.round((sueldoBase / factorDiasMes) / 8) * factorHoraExtraDoble) * horasExtraDoble);

                    // Provision Aguinaldo Bono14
                    provision = Utileria.round((ordinario + extraOrdinario) / 12);

                    // Cuota Patronal Iggs
                    cuotaPatronalIgss = Utileria.round((ordinario + extraOrdinario) * (porcentajeCuotaPatronalIGSS / 100));

                    indemnizacion = getIndemnizacion() * rsRecords.getInt("AplicaIndemnizacion");

                    totalIngresos = Utileria.round((ordinario + extraOrdinario));
                    totalIngresos += Utileria.round((bonificaciones[0] + bonificaciones[1] + bonificaciones[2] + bonificaciones[3] + bonificaciones[4]) + indemnizacion + vacaciones[0]);

                    descuentos[0] = Utileria.round((ordinario + extraOrdinario) * (porcentajeCuotaLaboralIGSS / 100));

                   /* if (((totalIngresos - descuentos[0]) - montoBaseRetenerISR) > 0) { //ingresos - cuotalaboraligss
                        descuentos[2] = Utileria.round((((totalIngresos - descuentos[0]) - montoBaseRetenerISR) * 0.05)); //isr
                    }*/

                    descuentos[1] = anticipoPrevio(fechaInicioDt.getValue());

                    totalEgresos = (descuentos[0] + descuentos[1] + descuentos[2] + descuentos[3] + descuentos[4]);

                    liquidoRecibir = (totalIngresos - totalEgresos);

System.out.println("Empleado=[" + idProveedor + "] liquidoRecibir=[" + liquidoRecibir + "]");

                    Object itemId = planillaDetalleContainer.addItem();
                    // planillaContainer.getContainerProperty(itemId, IDPLANILLA).setValue(rsRecords.getString("Id"));
                    planillaDetalleContainer.getContainerProperty(itemId, IDEMPLEADO).setValue(idProveedor);
                    planillaDetalleContainer.getContainerProperty(itemId, EMPLEADO).setValue(rsRecords.getString("Nombre"));
                    planillaDetalleContainer.getContainerProperty(itemId, CARGO).setValue(rsRecords.getString("Cargo"));
                    planillaDetalleContainer.getContainerProperty(itemId, SUELDO_BASE).setValue(sueldoBase);
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_LABORADOS_MES).setValue(diasLaborados[1]);
                    planillaDetalleContainer.getContainerProperty(itemId, LICENCIA).setValue(diasLaborados[2]);
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_LABORADOS_TOTALES).setValue(diasLaborados[0]);
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_GOZADOS_VACACIONES).setValue(vacaciones[1] + vacaciones[2]);
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_VACACIONES_PENDIENTES).setValue(vacaciones[3]);
                    // INGRESOS
                    planillaDetalleContainer.getContainerProperty(itemId, SUELDO_DEVENGADO).setValue(ordinario);
                    planillaDetalleContainer.getContainerProperty(itemId, HORAS_EXTRA).setValue(horasExtra);
                    planillaDetalleContainer.getContainerProperty(itemId, HORAS_EXTRAII).setValue(horasExtraDoble);
                    planillaDetalleContainer.getContainerProperty(itemId, EXTRAORDINARIO).setValue(extraOrdinario);
                    planillaDetalleContainer.getContainerProperty(itemId, BONO1).setValue(Utileria.round(bonificaciones[0])); // BONIFICACION DCTO 37-2001
                    planillaDetalleContainer.getContainerProperty(itemId, BONO2).setValue(Utileria.round(bonificaciones[1])); // BONIFICACION DCTO 78-89
                    planillaDetalleContainer.getContainerProperty(itemId, BONO3).setValue(Utileria.round(bonificaciones[2])); // BONIFICACION EVENTOS
                    planillaDetalleContainer.getContainerProperty(itemId, BONO4).setValue(Utileria.round(bonificaciones[3])); // BONIFICACION BONO 14
                    planillaDetalleContainer.getContainerProperty(itemId, BONO5).setValue(Utileria.round(bonificaciones[4])); // BONIFICACION AGUINALDO
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION_AG_B14).setValue(Utileria.round(provision)); // PROVISION AGUINALDO Y BONO 14
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION_PATIGSS).setValue(Utileria.round(cuotaPatronalIgss)); // CUOTA PATRONAL IGGS
                    planillaDetalleContainer.getContainerProperty(itemId, INDEMNIZACION).setValue(Utileria.round(indemnizacion));
                    planillaDetalleContainer.getContainerProperty(itemId, VACACIONES).setValue(vacaciones[0]);
                    planillaDetalleContainer.getContainerProperty(itemId, INGRESOS).setValue(Utileria.round(totalIngresos));
                    // EGRESOS
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO1).setValue(Utileria.round(descuentos[0])); // CUOTA LABORAL IGSS
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO2).setValue(Utileria.round(descuentos[1]));
                    //planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO3).setValue(Utileria.round(descuentos[2]));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO4).setValue(Utileria.round(descuentos[3]));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO5).setValue(Utileria.round(descuentos[4]));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTOS).setValue(Utileria.round(totalEgresos));

//                    planillaDetalleContainer.getContainerProperty(itemId, LIQUIDO).setValue(liquidoRecibir);
                    planillaDetalleContainer.getContainerProperty(itemId, LIQUIDO).setValue(Utileria.round(liquidoRecibir));
//                    planillaDetalleContainer.getContainerProperty(itemId, LIQUIDO).setValue(new BigDecimal(liquidoRecibir).setScale(2, RoundingMode.HALF_UP).doubleValue());

                    totalEmpleados++;

                    totalPagado+= liquidoRecibir;

                } while (rsRecords.next());

                planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_EMPLEADOS_PLANILLA).setValue(totalEmpleados);
                planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_PAGADO_PLANILLA).setValue(totalPagado);

                guardarPlanilla(planillaId);
            }
            else {
                Notification.show("NO HAY EMPLEADOS");
            }

            planillaDetalleGrid.setCaption("Planilla de " + tipoPlanillaCbx.getValue() + " " + planillaDetalleContainer.size() + " empleados...");

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla CALCULO DE PLANILLA EMPLEADOS : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void calcularPlanilla() {

        try {
            guardarPlanilla(Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue())));

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            //0 leer datos de planilla
            queryString = "SELECT *";
            queryString += " FROM planilla_encabezado";
            queryString += " WHERE Id = " + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue());
            queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

            rsRecords = stQuery.executeQuery(queryString);

            if(!rsRecords.next()) {
                Notification.show("ERROR,  no se encontro planilla!", Notification.Type.ERROR_MESSAGE);
                return;
            }

            long diffInMillies = Math.abs(rsRecords.getDate("FechaInicio").getTime() - rsRecords.getDate("FechaFin").getTime());
            long diasPlanilla = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

            //1 parametros de planilla
            queryString = "SELECT *";
            queryString += " FROM planilla_parametros";
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

            rsRecords = stQuery.executeQuery(queryString);

            if(!rsRecords.next()) {
                Notification.show("ERROR,  no se encontraron parámetros de configuraciones de planilla de esta empresa!", Notification.Type.ERROR_MESSAGE);
                return;
            }

            //montoBaseRetenerISR = rsRecords.getDouble("MontoBaseRetenerISR");
            factorHoraExtra = rsRecords.getDouble("FactorHoraExtra");
            factorHoraExtraDoble = rsRecords.getDouble("FactorHoraExtraII");
            factorDiasMes = rsRecords.getInt("FactorDiasMes");
            factorDiasAnio = rsRecords.getInt("FactorDiasAnio");
            porcentajeCuotaLaboralIGSS = rsRecords.getDouble("PorcentajeCuotaLaboralIgss");
            porcentajeCuotaPatronalIGSS = rsRecords.getDouble("PorcentajeCuotaPatronalIgss");
            valorEvento = rsRecords.getDouble("ValorEvento");

            totalPagado = 0.00;

            java.util.Date fechaFinPlanilla;
            int[] diasLaborados;
            int diasLicenciaSinPago;
            int horasExtra;
            int horasExtraDoble;
            int eventos;
            double sueldoBase;
            double ordinario;
            double extraOrdinario;
            double totalIngresos;
            double totalEgresos;
            double[] vacaciones = new double[3];
            double liquidoRecibir;
            double bonificaciones[] = new double[5];
            double indemnizacion;
            double descuentos[] = new double[5];
            double provision;
            double cuotaPatronalIgss;

            for (Object itemObject : planillaDetalleContainer.getItemIds()) {

                Item item = planillaDetalleContainer.getItem(itemObject);

                idProveedor =  String.valueOf(item.getItemProperty(IDEMPLEADO).getValue());

                // 2 : query por cada empleado activo ya en esta planilla
                queryString = "SELECT *";
                queryString += " FROM proveedor_empresa";
                queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                queryString += " AND IdProveedor = " + idProveedor;

                rsRecords = stQuery.executeQuery(queryString);

                if(rsRecords.next()) {

//System.out.println("Empleado = " + rsRecords.getString("IdProveedor") + " " + rsRecords.getString("Nombre"));
                    // 2.1 : query por cada empleado buscar si ya está en otra planilla del aniomes...del mismo tipo de planilla..
                    queryString = "SELECT pla.*";
                    queryString += " FROM planilla_encabezado pla ";
                    queryString += " INNER JOIN planilla_detalle plade ON plade.IdPlanilla = pla.Id";
                    queryString += " WHERE plade.IdEmpleado = " + idProveedor;
                    queryString += " AND pla.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += " AND pla.AnioMes = " + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), MES_PLANILLA).getValue());
                    queryString += " AND pla.Tipo = '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()) + "'";
                    queryString += " AND pla.Estatus <> 'EN PROCESO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {
                        continue;
                    }

                    fechaFinPlanilla = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_FINALSF_PLANILLA).getValue()));
                    diasLaborados = getDiasTrabajados();
                    diasLicenciaSinPago = 0;
                    horasExtra = 0;
                    horasExtraDoble = 0;
                    eventos = 0;
                    sueldoBase = 0.00;
                    ordinario = 0.00;
                    extraOrdinario = 0.00;
                    totalIngresos = 0.00;
                    totalEgresos = 0.00;
                    liquidoRecibir = 0.00;
                    vacaciones[0] = (double) item.getItemProperty(VACACIONES).getValue();
                    bonificaciones[0] = (double) item.getItemProperty(BONO1).getValue();
                    bonificaciones[1] = (double) item.getItemProperty(BONO2).getValue();
                    bonificaciones[2] = (double) item.getItemProperty(BONO3).getValue();
                    bonificaciones[3] = (double) item.getItemProperty(BONO4).getValue();
                    bonificaciones[4] = (double) item.getItemProperty(BONO5).getValue();
                    provision = (double) item.getItemProperty(PROVISION_AG_B14).getValue();
                    cuotaPatronalIgss = (double) item.getItemProperty(PROVISION_PATIGSS).getValue();
                    indemnizacion = (double) item.getItemProperty(INDEMNIZACION).getValue();
                    descuentos[0] = Utileria.round((double) item.getItemProperty(DESCUENTO1).getValue());
                    descuentos[1] = Utileria.round((double) item.getItemProperty(DESCUENTO2).getValue());
                    //descuentos[2] = Utileria.round((double) item.getItemProperty(DESCUENTO3).getValue());
                    descuentos[3] = Utileria.round((double) item.getItemProperty(DESCUENTO4).getValue());
                    descuentos[4] = Utileria.round((double) item.getItemProperty(DESCUENTO5).getValue());

//                    if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).equals("Anticipo")) {

                    // 3 : query de bitacora para contar las horas extra
                    queryString = " SELECT IFNULL(SUM(HorasExtra), 0) TOTALHORASEXTRAS, IFNULL(SUM(HorasExtraDoble), 0) TOTALHORASEXTRASDOBLE";
                    queryString += " FROM empleado_asistencia";
                    queryString += " WHERE IdEmpleado = " + idProveedor;
                    queryString += " AND Fecha Between '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_INICIALSF_PLANILLA).getValue()) + "'";
                    queryString += "               And '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_FINALSF_PLANILLA).getValue()) + "'";

                    rsRecords1 = stQuery1.executeQuery(queryString);
//System.out.println("calcular query horas extra : " + queryString);

                    if (rsRecords1.next()) {
                        //horasExtra = rsRecords1.getInt("TOTALHORASEXTRAS");
                        //horasExtraDoble = rsRecords1.getInt("TOTALHORASEXTRASDOBLE");
                        horasExtra = 0;
                        horasExtraDoble = 0;
                    }

                    // 3.1 : query de bitacora para contar eventos
                    queryString = " SELECT IFNULL(SUM(Eventos), 0) TOTALEVENTOS";
                    queryString += " FROM empleado_asistencia";
                    queryString += " WHERE IdEmpleado = " + idProveedor;
                    queryString += " AND Fecha Between '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_INICIALSF_PLANILLA).getValue()) + "'";
                    queryString += "               And '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_FINALSF_PLANILLA).getValue()) + "'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {
                        eventos = rsRecords1.getInt("TOTALEVENTOS");
                    }

                    // 5 : query para obtener valor del sueldo y bonificaciones
                    queryString = " SELECT *";
                    queryString += " FROM empleado_salario";
                    queryString += " WHERE IdEmpleado = " + rsRecords.getString("IdProveedor");

                    rsRecords1 = stQuery1.executeQuery(queryString);


                    try {
                        dateBefore = new GregorianCalendar();
                        dateBefore.setTime(rsRecords.getDate("FechaIngreso"));

                        dateAfter = new GregorianCalendar();
                        dateAfter.setTime(fechaFinPlanilla);

                        dateAfter.setTime(fechaInicioDt.getValue());
                        dateAfter.add(GregorianCalendar.DATE, -1);

                        long dateBeforeInMs = dateBefore.getTimeInMillis();
                        long dateAfterInMs = dateAfter.getTimeInMillis();
                        // 31/10/2023    23/01/2023
                        long timeDiff = Math.abs(dateAfterInMs - dateBeforeInMs);

                        daysDiff = 1 + TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Notification.show("ERROR : al calcular los dias : " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
                        return;
                    }

                    // Sueldo a pagar y bonos mensuales
                    double[] ingresos = getIngresosFijos();
                    ordinario = ingresos[0];
                    sueldoBase = ingresos[1];

                    bonificaciones[0] = ingresos[2];
                    bonificaciones[1] = ingresos[3];


                    // Vacaciones
                    vacaciones = getVacaciones();

                    //BONO14 O AGUINALDO
                    double[] bonos = getBonos_1();
                    bonificaciones[4] = bonos[0]; // Aduinaldo
                    bonificaciones[3] = bonos[1]; // Bono 14

                    // ordinario = (ordinario / factorDiasMes) * diasLaborados;  PARA HORAS EXTRA ES EL BASE SIN DESCONTAR DIAS
                    // 7 : calculo de extraordinario (horas_extra * valor_horas_extr
                    extraOrdinario = ((Utileria.round((sueldoBase / factorDiasMes) / 8) * factorHoraExtra) * horasExtra);
                    extraOrdinario += ((Utileria.round((sueldoBase / factorDiasMes) / 8) * factorHoraExtraDoble) * horasExtraDoble);

                    // Provision Aguinaldo y Bono 14
                    provision = Utileria.round((ordinario + extraOrdinario) / 12);

                    // Couta Patronal IGSS
                    cuotaPatronalIgss = Utileria.round((ordinario + extraOrdinario) * (porcentajeCuotaPatronalIGSS / 100));

                    indemnizacion = getIndemnizacion() * rsRecords.getInt("AplicaIndemnizacion");

                    totalIngresos = Utileria.round((ordinario + extraOrdinario));
                    totalIngresos += Utileria.round(bonificaciones[0] + bonificaciones[1] + bonificaciones[2] + bonificaciones[3] + bonificaciones[4]);
                    totalIngresos += indemnizacion + vacaciones[0];

                    if (!String.valueOf(tipoPlanillaCbx.getValue()).contains("Solo")) {
                        descuentos[0] = Utileria.round((ordinario + extraOrdinario) * (porcentajeCuotaLaboralIGSS / 100));

                        /*if (((totalIngresos - descuentos[0]) - montoBaseRetenerISR) > 0) { //ingresos - cuotalaboraligss
                            descuentos[2] = Utileria.round((((totalIngresos - descuentos[0]) - montoBaseRetenerISR) * 0.05)); //isr
                        }*/

                        descuentos[1] = anticipoPrevio(fechaInicioDt.getValue());

                        totalEgresos = (descuentos[0] + descuentos[1] + descuentos[2] + descuentos[3] + descuentos[4]);
                    }

                    liquidoRecibir = (totalIngresos - totalEgresos);

                    // planillaContainer.getContainerProperty(itemId, IDPLANILLA).setValue(rsRecords.getString("Id"));
                    item.getItemProperty(IDEMPLEADO).setValue(idProveedor);
                    item.getItemProperty(EMPLEADO).setValue(rsRecords.getString("Nombre"));
                    item.getItemProperty(CARGO).setValue(rsRecords.getString("Cargo"));
                    item.getItemProperty(SUELDO_BASE).setValue(sueldoBase);
                    item.getItemProperty(DIAS_LABORADOS_MES).setValue(diasLaborados[1]);
                    item.getItemProperty(LICENCIA).setValue(diasLaborados[2]);
                    item.getItemProperty(DIAS_LABORADOS_TOTALES).setValue(diasLaborados[0]);
                    item.getItemProperty(DIAS_GOZADOS_VACACIONES).setValue(vacaciones[1] + vacaciones[2]);
                    item.getItemProperty(DIAS_VACACIONES_PENDIENTES).setValue(vacaciones[3]);
                    // INGRESOS
                    item.getItemProperty(SUELDO_DEVENGADO).setValue(ordinario);
                    item.getItemProperty(HORAS_EXTRA).setValue(horasExtra);
                    item.getItemProperty(HORAS_EXTRAII).setValue(horasExtraDoble);
                    item.getItemProperty(EXTRAORDINARIO).setValue(extraOrdinario);
                    item.getItemProperty(BONO1).setValue(bonificaciones[0]); // BONIFICACION DCTO 37-2001
                    item.getItemProperty(BONO2).setValue(bonificaciones[1]); // BONIFICACION DCTO 78-89
                    item.getItemProperty(BONO3).setValue(bonificaciones[2]); // BONIFICACION EVENTOS
                    item.getItemProperty(BONO4).setValue(bonificaciones[3]); // BONOFICACION BONO 14
                    item.getItemProperty(BONO5).setValue(bonificaciones[4]); // BONOFICACION AGUINALDO
                    item.getItemProperty(PROVISION_AG_B14).setValue(provision); // PROVISION AGUINALDO Y BONO 14
                    item.getItemProperty(PROVISION_PATIGSS).setValue(cuotaPatronalIgss); // CUOTA PATRONAL IGSS
                    item.getItemProperty(INDEMNIZACION).setValue(Utileria.round(indemnizacion));
                    item.getItemProperty(VACACIONES).setValue(vacaciones[0]); //IngresoVacaciones
                    item.getItemProperty(INGRESOS).setValue(Utileria.round(totalIngresos));
                    // EGRESOS
                    item.getItemProperty(DESCUENTO1).setValue(Utileria.round(descuentos[0])); //CUOTA LABORAL IGSS
                    item.getItemProperty(DESCUENTO2).setValue(Utileria.round(descuentos[1]));
                    //item.getItemProperty(DESCUENTO3).setValue(Utileria.round(descuentos[2])); //ISR
                    item.getItemProperty(DESCUENTO4).setValue(Utileria.round(descuentos[3]));
                    item.getItemProperty(DESCUENTO5).setValue(Utileria.round(descuentos[4]));
                    item.getItemProperty(DESCUENTOS).setValue(Utileria.round(totalEgresos));
//                    item.getItemProperty(LIQUIDO).setValue(liquidoRecibir);
                    item.getItemProperty(LIQUIDO).setValue(Utileria.round(liquidoRecibir));
//                    item.getItemProperty(LIQUIDO).setValue(new BigDecimal(liquidoRecibir).setScale(2, RoundingMode.HALF_UP).doubleValue());

                } // no encontro al proveedor/empleado
            } //endfor

            planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_EMPLEADOS_PLANILLA).setValue(planillaDetalleContainer.size());
            planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_PAGADO_PLANILLA).setValue(totalPagado);

            planillaDetalleGrid.setCaption("Planilla de " + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()) + " " + planillaDetalleContainer.size() + " empleados...");

            guardarPlanilla(
                    Integer.valueOf(
                            String.valueOf(
                                    planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue())));

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla CALCULO DE PLANILLA EMPLEADOS : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public void fillPlanillaGrid(int nuevaPlanillaId) {
        planillaContainer.removeAllItems();
        planillaDetalleContainer.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT *";
            queryString += " FROM planilla_encabezado";
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND   Tipo = 'Liquidación'";
            queryString += " ORDER BY CreadoFechaYHora DESC";

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {

                Object itemId;
                Object itemIdNuevaPlanilla = null;

                stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                do {
                    itemId = planillaContainer.addItem();
                    planillaContainer.getContainerProperty(itemId, ID_PLANILLA).setValue(rsRecords.getString("Id"));
                    planillaContainer.getContainerProperty(itemId, CORRELATIVO_PLANILLA).setValue(rsRecords.getString("Correlativo"));
                    planillaContainer.getContainerProperty(itemId, TIPO_PLANILLA).setValue(rsRecords.getString("Tipo"));
                    planillaContainer.getContainerProperty(itemId, MES_PLANILLA).setValue(rsRecords.getString("AnioMes"));
                    planillaContainer.getContainerProperty(itemId, FECHA_INICIAL_PLANILLA).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicio")));
                    planillaContainer.getContainerProperty(itemId, FECHA_FINAL_PLANILLA).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFin")));
                    planillaContainer.getContainerProperty(itemId, DESCRIPCION_PLANILLA).setValue(rsRecords.getString("Descripcion"));
                    planillaContainer.getContainerProperty(itemId, ESTATUS_PLANILLA).setValue(rsRecords.getString("Estatus"));
                    planillaContainer.getContainerProperty(itemId, TOTAL_EMPLEADOS_PLANILLA).setValue(rsRecords.getInt("TotalEmpleados"));
                    planillaContainer.getContainerProperty(itemId, TOTAL_PAGADO_PLANILLA).setValue(rsRecords.getDouble("TotalPagado"));
                    planillaContainer.getContainerProperty(itemId, FECHA_INICIALSF_PLANILLA).setValue(Utileria.getFechaYYYYMMDD_1(rsRecords.getDate("FechaInicio")));
                    planillaContainer.getContainerProperty(itemId, FECHA_FINALSF_PLANILLA).setValue(Utileria.getFechaYYYYMMDD_1(rsRecords.getDate("FechaFin")));
                    if(rsRecords.getInt("Id") == nuevaPlanillaId) {
                        itemIdNuevaPlanilla = itemId;
                    }
                } while (rsRecords.next());

                if(nuevaPlanillaId > 0) {
                    planillaGrid.select(itemIdNuevaPlanilla);
                }
                else {
                    planillaGrid.select(planillaContainer.firstItemId());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla PLANILLAS : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public void fillPlanillaDetalleGrid() {
        planillaDetalleContainer.removeAllItems();

        if(planillaGrid.getSelectedRow() == null) {
            Notification.show("POR FAVOR SELECCIONE UNA PLANILLA, PARA DESPLEGAR DETALLE", Notification.Type.HUMANIZED_MESSAGE);
            return;
        }

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT *";
            queryString += " FROM planilla_encabezado";
            queryString += " WHERE Id = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
                fechaInicioDt.setValue(rsRecords.getDate("FechaInicio"));
                fechaFinDt.setValue(rsRecords.getDate("FechaFin"));
                descripcionPlanillaTxt.setValue(rsRecords.getString("Descripcion"));
            }

            queryString = "SELECT *, prv.Cargo, prv.Nombre";
            queryString += " FROM planilla_detalle";
            queryString += " INNER JOIN proveedor_empresa prv ON prv.IdProveedor = IdEmpleado";
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND IdPlanilla = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();
            queryString += " ORDER BY prv.IdProveedor";
//System.out.println("query fillplanillaDetalleGrid = " + queryString);
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
                Object itemId;
                do {
                    itemId = planillaDetalleContainer.addItem();
                    planillaDetalleContainer.getContainerProperty(itemId, IDEMPLEADO).setValue(rsRecords.getString("IdEmpleado"));
                    planillaDetalleContainer.getContainerProperty(itemId, EMPLEADO).setValue(rsRecords.getString("Nombre"));
                    planillaDetalleContainer.getContainerProperty(itemId, CARGO).setValue(rsRecords.getString("Cargo"));
                    planillaDetalleContainer.getContainerProperty(itemId, SUELDO_BASE).setValue(rsRecords.getDouble("SueldoBase"));
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_LABORADOS_MES).setValue(rsRecords.getInt("DiasLaborados"));
                    planillaDetalleContainer.getContainerProperty(itemId, LICENCIA).setValue(rsRecords.getInt("Licencia"));
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_LABORADOS_TOTALES).setValue(rsRecords.getInt("DiasTotalesLaborados"));
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_GOZADOS_VACACIONES).setValue(rsRecords.getDouble("VacacionesGozadas"));
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_VACACIONES_PENDIENTES).setValue(rsRecords.getDouble("VacacionesPendientes"));
                    // INGRESOS
                    planillaDetalleContainer.getContainerProperty(itemId, SUELDO_DEVENGADO).setValue(rsRecords.getDouble("SalarioDevengado"));
                    planillaDetalleContainer.getContainerProperty(itemId, HORAS_EXTRA).setValue(rsRecords.getInt("HorasExtra"));
                    planillaDetalleContainer.getContainerProperty(itemId, HORAS_EXTRAII).setValue(rsRecords.getInt("HorasExtraII"));
                    planillaDetalleContainer.getContainerProperty(itemId, EXTRAORDINARIO).setValue(rsRecords.getDouble("SalarioExtraordinario"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO1).setValue(rsRecords.getDouble("Bonificacion1"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO2).setValue(rsRecords.getDouble("Bonificacion2"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO3).setValue(rsRecords.getDouble("Bonificacion3"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO4).setValue(rsRecords.getDouble("Bonificacion4"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO5).setValue(rsRecords.getDouble("Bonificacion5"));
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION_AG_B14).setValue(rsRecords.getDouble("Provision2"));
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION_PATIGSS).setValue(rsRecords.getDouble("Provision1"));
                    planillaDetalleContainer.getContainerProperty(itemId, INDEMNIZACION).setValue(rsRecords.getDouble("Indemnizacion"));
                    planillaDetalleContainer.getContainerProperty(itemId, VACACIONES).setValue(rsRecords.getDouble("Vacaciones"));
                    planillaDetalleContainer.getContainerProperty(itemId, INGRESOS).setValue(rsRecords.getDouble("TotalIngresos"));
                    // EGRESOS
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO1).setValue(rsRecords.getDouble("Descuento1"));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO2).setValue(rsRecords.getDouble("Descuento2"));
                    //planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO3).setValue(rsRecords.getDouble("Descuento3"));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO4).setValue(rsRecords.getDouble("Descuento4"));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO5).setValue(rsRecords.getDouble("Descuento5"));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTOS).setValue(rsRecords.getDouble("TotalDescuentos"));
                    planillaDetalleContainer.getContainerProperty(itemId, LIQUIDO).setValue(rsRecords.getDouble("LiquidoRecibir"));
                    planillaDetalleContainer.getContainerProperty(itemId, CHEQUE).setValue(rsRecords.getString("Cheque"));
                    planillaDetalleContainer.getContainerProperty(itemId, CODIGOPARTIDA).setValue(rsRecords.getString("CodigoPartida"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla CALCULO DE PLANILLA EMPLEADOS : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    //---------------------------------------------------------------------------------------------------------------------
    private double anticipoPrevio(java.util.Date fechaPlanilla) {
        double montoAnticipo = 0.00;
        try {

            queryString = "SELECT pd.Bonificacion3, pd.Descuento4 ";
            queryString += "FROM planilla_encabezado pe ";
            queryString += "INNER JOIN planilla_detalle pd ON pe.id = pd.idplanilla ";
            queryString += "WHERE pd.IdEmpleado = " + rsRecords.getString("IdProveedor") + " ";
            queryString += "AND pe.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
            queryString += "AND Extract(YEAR_MONTH FROM pe.FechaFin) = " + new Utileria().getFechaYYYYMM(fechaPlanilla) + " ";
            queryString += "AND pe.Estatus = 'GENERADA' ";
            queryString += "ORDER BY pe.tipo";

            rsRecords1 = stQuery1.executeQuery(queryString);

            if(rsRecords1.next()) {
                montoAnticipo = rsRecords1.getDouble("Bonificacion3");
            }
            if(rsRecords1.next()) {
                montoAnticipo = montoAnticipo <= rsRecords1.getDouble("Descuento4") ? 0 : montoAnticipo;
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al buscar anticipo de planilla de empleado : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }

        return montoAnticipo;
    }

    //---------------------------------------------------------------------------------------------------------------------
    private void guardarPlanilla(int planillaId) {

        try {

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            queryString = "UPDATE planilla_encabezado SET";
            queryString += "  Tipo = '" + tipoPlanillaCbx.getValue() + "'";
            queryString += ", FechaInicio = '" + Utileria.getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
            queryString += ", FechaFin = '" + Utileria.getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";
            queryString += ", AnioMes = " + new Utileria().getFechaYYYYMM(fechaInicioDt.getValue());
            queryString += ", Descripcion = '" + descripcionPlanillaTxt.getValue() + "'";
            queryString += ", IncluyeAguinaldo = 'S'";
            queryString += ", IncluyeAguinaldo = 'S'";
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND Id = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

            stQuery.executeUpdate(queryString);

            queryString = "DELETE FROM planilla_detalle ";
            queryString += " WHERE IdPlanilla = " + planillaId;

            stQuery.executeUpdate(queryString);

            for (Object itemObject : planillaDetalleContainer.getItemIds()) {

                Item item = planillaDetalleContainer.getItem(itemObject);

                queryString = "  INSERT INTO planilla_detalle (IdPlanilla, IdEmpleado, SueldoBase, DiasLaborados, Licencia, DiasTotalesLaborados, VacacionesGozadas, ";
                queryString += " VacacionesPendientes, SalarioDevengado, HorasExtra, HorasExtraII, SalarioExtraordinario, Bonificacion1, Bonificacion2, Bonificacion3,";
                queryString += " Bonificacion4, Bonificacion5, Provision1, Provision2, Provision3, Vacaciones, TotalIngresos, Descuento1,";
                queryString += " Descuento2, Descuento3, Descuento4, Descuento5, TotalDescuentos, LiquidoRecibir, Indemnizacion ";
                queryString += ")";
                queryString += " VALUES ";
                queryString += "(";
                queryString +=       planillaId;
                queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                queryString += "," + item.getItemProperty(SUELDO_BASE).getValue();
                queryString += "," + item.getItemProperty(DIAS_LABORADOS_MES).getValue();
                queryString += "," + item.getItemProperty(LICENCIA).getValue();
                queryString += "," + item.getItemProperty(DIAS_LABORADOS_TOTALES).getValue();
                queryString += "," + item.getItemProperty(DIAS_GOZADOS_VACACIONES).getValue();
                queryString += "," + item.getItemProperty(DIAS_VACACIONES_PENDIENTES).getValue();
                queryString += "," + item.getItemProperty(SUELDO_DEVENGADO).getValue();
                queryString += "," + item.getItemProperty(HORAS_EXTRA).getValue();
                queryString += "," + item.getItemProperty(HORAS_EXTRAII).getValue();
                queryString += "," + item.getItemProperty(EXTRAORDINARIO).getValue();
                queryString += "," + item.getItemProperty(BONO1).getValue();
                queryString += "," + item.getItemProperty(BONO2).getValue();
                queryString += "," + item.getItemProperty(BONO3).getValue();
                queryString += "," + item.getItemProperty(BONO4).getValue();
                queryString += "," + item.getItemProperty(BONO5).getValue();
                queryString += "," + item.getItemProperty(PROVISION_PATIGSS).getValue();
                queryString += "," + item.getItemProperty(PROVISION_AG_B14).getValue();
                queryString += "," + item.getItemProperty(PROVISION_AG_B14).getValue();
                queryString += "," + item.getItemProperty(VACACIONES).getValue();
                queryString += "," + item.getItemProperty(INGRESOS).getValue();
                queryString += "," + item.getItemProperty(DESCUENTO1).getValue();
                queryString += "," + item.getItemProperty(DESCUENTO2).getValue();
                queryString += "," +  0; //item.getItemProperty(DESCUENTO3).getValue();
                queryString += "," + item.getItemProperty(DESCUENTO4).getValue();
                queryString += "," + item.getItemProperty(DESCUENTO5).getValue();
                queryString += "," + item.getItemProperty(DESCUENTOS).getValue();
                queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                queryString += "," + item.getItemProperty(INDEMNIZACION).getValue();
                queryString += ")";

                stQuery.executeUpdate(queryString);
            } // endfor
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
        } catch (Exception ex) {
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                //
            }
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al guardar tabla PLANILLAS : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void generarPlanilla() {
        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            //0 leer datos de planilla
            queryString = "SELECT *";
            queryString += " FROM planilla_encabezado";
            queryString += " WHERE Id = " + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue());
            queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

            rsRecords = stQuery.executeQuery(queryString);

            if(!rsRecords.next()) {
                Notification.show("ERROR,  no se encontró planilla!", Notification.Type.ERROR_MESSAGE);
                return;
            }

            String codigoPartida, codigoCC;
            int correlativoCheque = (int)correlativoInicialChequeTxt.getDoubleValueDoNotThrow();

            long diffInMillies = Math.abs(rsRecords.getDate("FechaInicio").getTime() - rsRecords.getDate("FechaFin").getTime());
            long diasPlanilla = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

            double totalPagado = 0.00;

            String fecha;
            String ultimoEncontado;
            String dia;
            String mes;
            String año;

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            for (Object itemObject : planillaDetalleContainer.getItemIds()) {

                //la fecha de las partidas de planilla por pagar (sueldos por pagar) debe ser 1 dia antes de la fecha de cheque...
                fecha = Utileria.getFechaYYYYMMDD_1(new java.util.Date().from(fechaChequeDt.getValue().toInstant().minus(1, ChronoUnit.DAYS)));
                dia = fecha.substring(8, 10);
                mes = fecha.substring(5, 7);
                año = fecha.substring(0, 4);
                codigoPartida = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + año + mes + dia + "8";

                queryString = " SELECT codigoPartida FROM contabilidad_partida ";
                queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
                queryString += " ORDER BY codigoPartida DESC ";

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado
                    ultimoEncontado = rsRecords.getString("CodigoPartida").substring(12, 15);

                    codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

                } else {
                    codigoPartida += "001";
                }

                System.out.println("FECHA PARTIDA PLANILLA = " + fecha + " CodigoPartida = " + codigoPartida );

                Item item = planillaDetalleContainer.getItem(itemObject);
                codigoCC = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "10000000" + item.getItemProperty(IDEMPLEADO).getValue().toString().substring(2);
                //2210000000343

                /************************************************ DEBE *************************************/
                //ORDINARIO
                queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
                queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor, NombreCheque,";
                queryString += " MontoDocumento, SerieDocumento, NumeroDocumento, TipoDOCA, NoDOCA, IdNomenclatura, MonedaDocumento, Debe, Haber,";
                queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,";
                queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora, IdCentroCosto, CodigoCentroCosto, SueldoOrdinario)";
                queryString += " Values ";
                queryString += " (";
                queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += ",'PLANILLA'";
                queryString += ",'" + fecha + "'";
                queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                queryString += ",''";//nitproveedor
                queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                queryString += ",''"; //tipodoca
                queryString += ",''"; //doca
                queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getSueldoOrdinario(); // sueldo ordinario
                queryString += ",'QUETZALES'";
                queryString += "," + item.getItemProperty(SUELDO_DEVENGADO).getValue(); //DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + item.getItemProperty(SUELDO_DEVENGADO).getValue(); //DEBE Q
                queryString += ",0.00"; //DEBE Q.
                queryString += ",1.0";
                queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",0";
                queryString += ",'0'";
                queryString += ", " + item.getItemProperty(SUELDO_BASE).getValue() + ")";

                //EXTRAORDINARIO
                if((double)item.getItemProperty(EXTRAORDINARIO).getValue() > 0) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getSueldoExtraordinario(); // EXTRAORDINARIO
                    queryString += ",'QUETZALES'";
                    queryString += "," + item.getItemProperty(EXTRAORDINARIO).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + item.getItemProperty(EXTRAORDINARIO).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }
                //BONO DCTO 37-2001
                if((double)item.getItemProperty(BONO1).getValue() > 0) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getBonificacionDCTO07_2001(); //BONO DCTO 37-2001
                    queryString += ",'QUETZALES'";
                    queryString += "," + item.getItemProperty(BONO1).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + item.getItemProperty(BONO1).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }
                //BONO DCTO 78-89
                if(((double)item.getItemProperty(BONO2).getValue() + (double)item.getItemProperty(BONO3).getValue() ) > 0
                ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getBonificacionDCTO78_89(); // BONO DCTO 78-89
                    queryString += ",'QUETZALES'";
                    queryString += "," + ((double)item.getItemProperty(BONO2).getValue()
                            + (double)item.getItemProperty(BONO3).getValue()
                    );
                    queryString += ",0.00"; //HABER
                    queryString += "," + ((double)item.getItemProperty(BONO2).getValue()
                            + (double)item.getItemProperty(BONO3).getValue()
                    );
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                //AGUINALDO
                if((double)item.getItemProperty(PROVISION_AG_B14).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAguinaldo(); // AGUINALDO
                    queryString += ",'QUETZALES'";
                    queryString += "," + (double)item.getItemProperty(PROVISION_AG_B14).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(PROVISION_AG_B14).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }
                //BONO 14
                if((double)item.getItemProperty(PROVISION_AG_B14).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getBono14(); // BONO 14
                    queryString += ",'QUETZALES'";
                    queryString += "," + (double)item.getItemProperty(PROVISION_AG_B14).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(PROVISION_AG_B14).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                //PROVICION AGUINALDO
                if((double)item.getItemProperty(BONO5).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getProvisionAguinaldo(); //PROVICION AGUINALDO
                    queryString += ",'QUETZALES'";
                    queryString += "," + (double)item.getItemProperty(BONO5).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(BONO5).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }
                //PROVISION BONO 14
                if((double)item.getItemProperty(BONO4).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += ", " + ((SopdiUI) mainUI).cuentasContablesDefault.getProvisionBono14(); // PROVICION BONO 14
                    queryString += ",'QUETZALES'";
                    queryString += "," + (double)item.getItemProperty(BONO4).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(BONO4).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }


                // Vacaciones
                if((double)item.getItemProperty(VACACIONES).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getVacaciones(); // Vacaciones
                    queryString += ",'QUETZALES'";
                    queryString += "," + (double)item.getItemProperty(VACACIONES).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(VACACIONES).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                // Indemnizacion
                if((double)item.getItemProperty(INDEMNIZACION).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIndemnizacion(); // Indemnizacion
                    queryString += ",'QUETZALES'";
                    queryString += "," + (double)item.getItemProperty(INDEMNIZACION).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(INDEMNIZACION).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                // CUOTA PATRONAL IGSS
                if((double)item.getItemProperty(PROVISION_PATIGSS).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getCuotaPatronalIgss(); // Cuota Patronal
                    queryString += ",'QUETZALES'";
                    queryString += "," + (double)item.getItemProperty(PROVISION_PATIGSS).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(PROVISION_PATIGSS).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }
/************************************************ HABER *************************************/

                //CUOTA LABORAL IGSS POR PAGAR
                if((double)item.getItemProperty(DESCUENTO1).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getCuotaLaboralIgssPorPagar();; // CUOTA LABORAL IGSS POR PAGAR
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + (double)item.getItemProperty(DESCUENTO1).getValue();
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + (double)item.getItemProperty(DESCUENTO1).getValue();
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                //CUOTA PATRONAL IGSS POR PAGAR
                if((double)item.getItemProperty(PROVISION_PATIGSS).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getCuotaPatronalIgssPorPagar();; // CUOTA PATRONAL IGSS POR PAGAR
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + (double)item.getItemProperty(PROVISION_PATIGSS).getValue();
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + (double)item.getItemProperty(PROVISION_PATIGSS).getValue();
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                //SUELDOS POR PAGAR
                if((double)item.getItemProperty(LIQUIDO).getValue() > 0) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += ",87"; //SUELDOS POR PAGAR
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue(); //HABER
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue(); //HABER Q
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }
/*
                //RETENCIONES ISR POR PAGAR
                if((double)item.getItemProperty(DESCUENTO3).getValue() > 0) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += ",86"; //RETENCIONES ISR POR PAGAR
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + item.getItemProperty(DESCUENTO3).getValue(); //HABER
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + item.getItemProperty(DESCUENTO3).getValue(); //HABER Q
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }*/

                //ANTICIPO SUELDO
                if((double)item.getItemProperty(DESCUENTO2).getValue() > 0) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += ",16"; //ANTICIPOS DE SUELDOS
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + item.getItemProperty(DESCUENTO2).getValue(); //HABER
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + item.getItemProperty(DESCUENTO2).getValue(); //HABER Q
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                //AGUINALDO
                if((double)item.getItemProperty(PROVISION_AG_B14).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getProvisionAguinaldo(); // PROVISION AGUINALDO
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + (double)item.getItemProperty(PROVISION_AG_B14).getValue();
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + (double)item.getItemProperty(PROVISION_AG_B14).getValue();
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }
                //PROVISION BONO 14
                if((double)item.getItemProperty(PROVISION_AG_B14).getValue() > 0 ) {
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getProvisionBono14(); // PROVISION BONO 14
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + (double)item.getItemProperty(PROVISION_AG_B14).getValue();
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + (double)item.getItemProperty(PROVISION_AG_B14).getValue();
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                if((double)item.getItemProperty(LIQUIDO).getValue() > 0 && !((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {

/************************************************ CHEQUE *************************************/

                    //la fecha de cheques de planilla debe ser 1 dia despues  de la fecha de planilla por pabar (sueldos por pagar)... es decir, la fecha de cheque (campo)
                    String fechaCheque = Utileria.getFechaYYYYMMDD_1(fechaChequeDt.getValue());
                    dia = fechaCheque.substring(8, 10);
                    mes = fechaCheque.substring(5, 7);
                    año = fechaCheque.substring(0, 4);

                    codigoPartida = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + año + mes + dia + "8";

                    String queryString1 = " SELECT codigoPartida FROM contabilidad_partida ";
                    queryString1 += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
                    queryString1 += " ORDER BY codigoPartida DESC ";

                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords = stQuery.executeQuery(queryString1);

                    if (rsRecords.next()) { //  encontrado

                        ultimoEncontado = rsRecords.getString("CodigoPartida").substring(12, 15);

                        codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

                    } else {
                        codigoPartida += "001";
                    }

                    System.out.println("FECHA PARTIDA PLANILLA = " + fecha + " CodigoPartieda = " + codigoPartida);

                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'CHEQUE'";
                    queryString += ",'" + fechaCheque + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(correlativoCheque) + "'"; //numero
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal();
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; // DEBE
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue(); // HABER
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PAGO PLANILLA " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";

                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'CHEQUE'";
                    queryString += ",'" + fechaCheque + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(correlativoCheque) + "'"; //numero
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getSueldosPorPagar();
                    queryString += ",'QUETZALES'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PAGO PLANILLA " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";

                    queryString += ";";

                    totalPagado+= (double)item.getItemProperty(LIQUIDO).getValue();
                }//SI HAY MONTO LIQUIDO > 0

                stQuery.executeUpdate(queryString);

                queryString = "UPDATE planilla_detalle";
                queryString += " SET CodigoPartida = '" + codigoPartida + "'";
                queryString += ",    Cheque = '" + String.valueOf(correlativoCheque) + "'";
                queryString += " WHERE IdPlanilla = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();
                queryString += " AND   IdEmpleado = " + item.getItemProperty(IDEMPLEADO).getValue();

                stQuery.executeUpdate(queryString);

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY INSERT PLANILLA SUELDO : " + queryString);


                correlativoCheque++;

                updateProveedor(String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()), String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue()));

                stQuery.executeUpdate(queryString);

            } //endfor

            queryString = "UPDATE planilla_encabezado";
            queryString += " SET Estatus = 'GENERADA'";
            queryString += ", TotalEmpleados = " + planillaDetalleContainer.size();
            queryString += ", TotalPagado= " + planillaDetalleContainer.size();
            queryString += ", AutorizadoFechaYhora = current_timestamp";
            queryString += ", AutorizadoUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND Id = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

            stQuery.executeUpdate(queryString);

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("PLANILLA GENERADA EXITOSAMENTE, POR FAVOR REVISE CHEQUES A PAGAR. " +
                    " CHEQUE INICIAL=[" + correlativoInicialChequeTxt.getValue() + "] CHEQUE FINAL=[" + (--correlativoCheque) +
                    "] [" + String.valueOf(correlativoCheque - (int)correlativoInicialChequeTxt.getDoubleValueDoNotThrow()) + "] CHEQUES GENERADOS EN TOTAL.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            generarPlanillaBtn.setEnabled(false);
            correlativoInicialChequeTxt.setVisible(false);
            fechaChequeDt.setVisible(false);

        } catch (Exception ex) {
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                //
            }
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al generar PLANILLA contable (cheques) : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void updateProveedor(String idProveedor, String idPlanilla) throws Exception {
        queryString = "UPDATE proveedor_empresa";
        queryString += " SET IdPlanillaLiquidacion = " + idPlanilla;
        queryString += " WHERE IdProveedor = " + idProveedor;

        stQuery.executeUpdate(queryString);
    }

    public boolean exportToExcel() {
        if (planillaGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(planillaDetalleGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
// produccion            fileexport = (empresa + "_" + empresaLbl.getValue().substring(5, empresaLbl.getValue().length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_INTEGRACION_CAMBIOS.xlsx").replaceAll(" ", "").replaceAll(",", "");
            fileexport = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "_" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_PLANILLA_BONO_EXTRA.xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    private double[]    getBonos_1(){
        long diffInMillies_aguinaldo = 0;
        long diffInMillies_bono14 = 0;
        double[] bonos = {0.00, 0.00}; // 0: Aguinaldo | 1: Bono 14


        double suledo_acumulado_bono14 = 0;
        double suledo_acumulado_aguinaldo = 0;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            //0 leer datos de planilla
            queryString = "SELECT *";
            queryString += " FROM proveedor_empresa";
            queryString += " WHERE IdProveedor = " + idProveedor;

            rsRecords2 = stQuery.executeQuery(queryString);

            if(!rsRecords2.next()) {
                Notification.show("ERROR,  no se encontró Empleado!", Notification.Type.ERROR_MESSAGE);
                return bonos;
            }

            // El caso en el que entre y salga en el mismo mes
            if(rsRecords2.getDate("FechaEgreso").getMonth() == rsRecords2.getDate("FechaIngreso").getMonth()
                && rsRecords2.getDate("FechaEgreso").getYear() == rsRecords2.getDate("FechaIngreso").getYear()){

                long diffInMillies = Math.abs(rsRecords2.getDate("FechaEgreso").getTime() - rsRecords2.getDate("FechaIngreso").getTime());
                long dias = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
                // 12 mese 30 dias por mes
                bonos[0] = Utileria.round((getIngresosFijos()[1] / (12 * 30)) * dias);
                bonos[1] = bonos[0];

                return bonos;

            }

            int año_a =  rsRecords2.getDate("FechaEgreso").getYear();
            int año_b = año_a;

            if(rsRecords2.getDate("FechaEgreso").getMonth() < 6){ // Verificamos si usamos el año de egreso o el anterior para el BONO 14
                año_a -= 1;
            }
            Date inicio_bono14 = new Date(año_a, 6, 1);

            if(rsRecords2.getDate("FechaEgreso").getMonth() < 11){ // Verificamos si usamos el año de egreso o el anterior para el AGUINALDO
                año_b -= 1;
            }
            Date inicio_aguinaldo = new Date(año_b, 11, 1);

            queryString = "SELECT Fecha, SueldoOrdinario, IdNomenclatura ";
            queryString += "FROM contabilidad_partida ";
            queryString += "WHERE IdProveedor = " + idProveedor + " ";
            queryString += "AND Fecha Between '" + Utileria.getFechaYYYYMMDD_1(rsRecords2.getDate("FechaIngreso")) + "' AND '" + Utileria.getFechaYYYYMMDD_1(rsRecords2.getDate("FechaEgreso")) + "' ";
            queryString += "AND Upper(Estatus) <> 'ANULADO' ";
            queryString += "AND TipoDocumento = 'Planilla' ";
            queryString += "AND IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
            queryString += "AND IdNomenclatura = " + ((SopdiUI)mainUI).cuentasContablesDefault.getSueldoOrdinario() + " ";
            queryString += "ORDER BY Fecha";


            rsRecords3 = stQuery2.executeQuery(queryString);
            Date fecha = null;
            Double monto = 0d;
            if(rsRecords3.next()){
                do{
                    fecha = rsRecords3.getDate("Fecha");
                    monto = rsRecords3.getDouble("SueldoOrdinario");

                    // Si la ingreso se encuentra de
                    if(fecha.getMonth() == rsRecords2.getDate("FechaIngreso").getMonth() && fecha.getYear() == rsRecords2.getDate("FechaIngreso").getYear()){
                        Date temp = new Date(fecha.getYear(), fecha.getMonth(), Utileria.getStaticUltimoDiaDelMes(fecha));
                        long diffInMillies = Math.abs(temp.getTime() - rsRecords2.getDate("FechaIngreso").getTime()) ;
                        long dias = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

                        monto = Utileria.round((monto / 30) * dias);

                    }

                    if(fecha.getMonth() == rsRecords2.getDate("FechaEgreso").getMonth() && fecha.getYear() == rsRecords2.getDate("FechaEgreso").getYear()){
                        Date temp = new Date(fecha.getYear(), fecha.getMonth(), 1);
                        long diffInMillies = Math.abs(temp.getTime() - rsRecords2.getDate("FechaEgreso").getTime());
                        long dias = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

                        monto = Utileria.round((monto / 30) * dias);

                    }

                    if(inicio_bono14.before(fecha)){
                        suledo_acumulado_bono14 += monto;
                    }
                    if(inicio_aguinaldo.before(fecha)){
                        suledo_acumulado_aguinaldo += monto;
                    }

                }while (rsRecords3.next());

                if(fecha.getMonth()+1 == rsRecords2.getDate("FechaEgreso").getMonth() && fecha.getYear() == rsRecords2.getDate("FechaEgreso").getYear()){
                    Date temp = new Date(fecha.getYear(), fecha.getMonth()+1, 1);
                    long diffInMillies = Math.abs(temp.getTime() - rsRecords2.getDate("FechaEgreso").getTime());
                    long dias = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

                    suledo_acumulado_aguinaldo += Utileria.round((monto / 30) * dias);
                    suledo_acumulado_bono14 += Utileria.round((monto / 30) * dias);

                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println(idProveedor);
        System.out.println("ag: " + suledo_acumulado_bono14 + " | b14: " + suledo_acumulado_aguinaldo);


        bonos[1] = suledo_acumulado_bono14/12;
        bonos[0] = suledo_acumulado_aguinaldo/12;
        return bonos;
    }

    /**
     *
     * @return  Arreglo con los promedios de 6 mneses
     * 0 Prodemdio Pagada, 1 Promedio Extraordinario Pagado, 2 Promedio Sueldo
     */
    private double[] getPromedio6meses(){

        int diasLaborados = factorDiasMes;
        double promedio_pagado = 0.00;
        double promedio_extraordinario = 0.00;
        double promedio_ordinario = 0.00;
        double[] promedio = new double[3]; // 0: Ordinario Pagado | 1: Extraordinario Pagado | 2: Sueldo Ordinario
        long dividido = 6;

        String fechaInicial = Utileria.getFechaYYYYMMDD_1(new java.util.Date().from(dateAfter.getTime().toInstant().minus(factorDiasMes * 6, ChronoUnit.DAYS)));
        String fechaFinal = Utileria.getFechaYYYYMMDD_1(dateAfter.getTime()); //fecha fin planilla

        try {

            Date fechaIngreso = rsRecords.getDate("FechaIngreso");
            Date fechaEgreso = rsRecords.getDate("FechaEgreso");

            long dias = getDaysDiff(fechaIngreso, fechaEgreso); // verifico el numero de meses
            if(  (dias/30) <  6) dividido = dias/30;

            // query obtener el PROMEDIO de sueldo ordinario + extraordinario 6 meses
            queryString = "SELECT IFNULL((SUM(DebeQuetzales) / " + dividido + "), 0) AS PAGADPROMEDIO, ";
            queryString += "IFNULL((SUM(SueldoOrdinario) / " + dividido + "), 0) AS SUELDOPROMEDIO, IdNomenclatura ";
            queryString += "FROM contabilidad_partida ";
            queryString += "WHERE IdProveedor = " + idProveedor + " ";
            queryString += "AND Fecha Between '" + fechaInicial + "' AND '" + fechaFinal + "' ";
            queryString += "AND Upper(Estatus) <> 'ANULADO' ";
            queryString += "AND TipoDocumento = 'Planilla' ";
            queryString += "AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
            queryString += "AND IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getSueldoOrdinario() + ", " + ((SopdiUI) mainUI).cuentasContablesDefault.getSueldoExtraordinario() + ") "; //ordinario y extraordinario solamente
            queryString += "GROUP BY IdNomenclatura";

            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) {
                if (rsRecords1.getInt("IdNomenclatura") != Integer.valueOf(((SopdiUI) mainUI).cuentasContablesDefault.getSueldoOrdinario())) {
                    promedio_extraordinario = rsRecords1.getDouble("PAGADPROMEDIO");
                }else {
                    promedio_pagado = rsRecords1.getDouble("PAGADPROMEDIO");
                    promedio_ordinario += rsRecords1.getDouble("SUELDOPROMEDIO");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla CALCULO DE PLANILLA EMPLEADOS Promedio Sueldo : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            return promedio;
        }

        promedio[0] = Utileria.round(promedio_pagado);
        promedio[1] = Utileria.round(promedio_extraordinario);
        promedio[2] = Utileria.round(promedio_ordinario);

        return  promedio;
    }

    private double getIndemnizacion(){
        double indemnizacion = 0.00;
        double[] promedio = getPromedio6meses();
        long diffInMillies = 0;

        try {

            diffInMillies = Math.abs(rsRecords.getDate("FechaIngreso").getTime() - rsRecords.getDate("FechaEgreso").getTime());

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al traer Fecha de Ingreso y Egereso : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            return indemnizacion;
        }

        long dias_trabajados = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

        indemnizacion = promedio[0] + promedio[1];
        indemnizacion = Utileria.round(indemnizacion*dias_trabajados/365);

        return indemnizacion;
    }

    private double[] getVacaciones(){
        double[] vacaciones = new double[4]; // 0: Monto | 1: Dias Gozados | 2: Correccion | 3: Pendientes
        long diffInMillies = 0;
        double diasVacaciones = 0;

        try {
            Date fechaIngreso = rsRecords.getDate("FechaIngreso");
            Date fechaEgreso = rsRecords.getDate("FechaEgreso");

            // 4.1 : query de bitacora para contar dias VACACIONES que se descuentan en el rango de fechas
            queryString = " SELECT SUM(IFNULL(DiasVacaciones, 0)) AS TOTALDIASVACACIONES";
            queryString += " FROM empleado_asistencia";
            queryString += " WHERE IdEmpleado = " + idProveedor;
            queryString += " AND Fecha  Between '" + Utileria.getFechaYYYYMMDD_1(fechaIngreso) + "'";
            //queryString += " AND Fecha  Between '" + Utileria.getFechaYYYYMMDD_1(inicioConteoVacaciones) + "'";
            queryString += "               And '" + Utileria.getFechaYYYYMMDD_1(fechaEgreso) + "'"; //  Prposito de purebad

            rsRecords1 = stQuery1.executeQuery(queryString);

            if(rsRecords1.next()) {
                diasVacaciones = rsRecords1.getDouble("TOTALDIASVACACIONES");

                vacaciones[1] = diasVacaciones;
                vacaciones[2] = rsRecords.getDouble("DiasVacacionesGozados");

                double promedio = getPromedio6meses()[2];
                double vacacionesAcumuladas = getDiasTrabajados()[0] * rsRecords.getDouble("DiasVacacionesDerecho")/365;
                vacaciones[3] = Utileria.round(vacacionesAcumuladas - diasVacaciones - vacaciones[2]);
                vacaciones[0] = Utileria.round(vacaciones[3] * promedio / factorDiasMes);

                if(vacaciones[0] < 0){
                    vacaciones[0] = 0;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al traer Fecha de Ingreso y Egereso : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            return vacaciones;
        }

        return vacaciones;
    }

    private double[] getIngresosFijos(){
        double[] sueldo = new double[4]; // 0: Sueldo a pagar este mes (el posentaje) | 1: Sueldo Ordinario | 2: 37-2001 | 3: 78-89
        Date fechaEgreso;
        long diffInMillies = 0;
        int diasLicenciaSinPago = 0;

        try {
            queryString = "SELECT es.Valor, IFNULL(cp.Debe, 0) AS Pagado, es.IdNomenclatura  ";
            queryString += "FROM empleado_salario es ";
            queryString += "INNER JOIN proveedor_empresa p ON es.IdEmpleado = p.IDProveedor ";
            queryString += "LEFT JOIN contabilidad_partida cp ON es.IdEmpleado = cp.IdProveedor ";
            queryString += "AND DATE_FORMAT(cp.Fecha, '%Y-%m') = DATE_FORMAT(p.FechaEgreso, '%Y-%m') ";
            queryString += "AND cp.IdNomenclatura = es.IdNomenclatura ";
            queryString += "WHERE es.IdEmpleado = " + idProveedor;

            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords3 = stQuery2.executeQuery(queryString);

            long dias_trabajados = getDiasTrabajados()[1];
            if (dias_trabajados > 30) {
                dias_trabajados = 30;
            }

            if(rsRecords3.next()) {
                do {
                    if(rsRecords3.getString("IdNomenclatura").equals(((SopdiUI)mainUI).cuentasContablesDefault.getSueldoOrdinario())){
                        sueldo[1] = Utileria.round(rsRecords3.getDouble("Valor"));

                        sueldo[0] = Utileria.round(sueldo[1] * dias_trabajados / 30);
                        sueldo[0] = sueldo[0] - Utileria.round(rsRecords3.getDouble("Pagado"));

                    }else if(rsRecords3.getString("IdNomenclatura").equals(((SopdiUI)mainUI).cuentasContablesDefault.getBonificacionDCTO07_2001())){
                        sueldo[2] = Utileria.round(Utileria.round(rsRecords3.getDouble("Valor")) * dias_trabajados / 30);
                        sueldo[2] = sueldo[2] - Utileria.round(rsRecords3.getDouble("Pagado"));

                    }else if(rsRecords3.getString("IdNomenclatura").equals(((SopdiUI)mainUI).cuentasContablesDefault.getBonificacionDCTO78_89())){
                        sueldo[3] = Utileria.round(Utileria.round(rsRecords3.getDouble("Valor")) * dias_trabajados / 30);
                        sueldo[3] = sueldo[3] - Utileria.round(rsRecords3.getDouble("Pagado"));

                    }

                } while (rsRecords3.next());
            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al traer Fecha de Egereso : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);

            return sueldo;
        }

        return sueldo;
    }
    
    private int[] getDiasTrabajados(){
        long diffInMillies = 0;
        int[] diasTrabajados = {0, 0, 0, 0}; // 0: Dias Totales (Inicio A Fin) | 1: Dias Totales (Este Mes)
        int diasLicenciaSinPago = 0;
        Date fechaEgreso;
        Date fechaIngreso;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            //0 leer datos de planilla
            queryString = "SELECT *";
            queryString += " FROM proveedor_empresa";
            queryString += " WHERE IdProveedor = " + idProveedor;

            rsRecords2 = stQuery.executeQuery(queryString);

            if(!rsRecords2.next()) {
                Notification.show("ERROR,  no se encontró Empleado!", Notification.Type.ERROR_MESSAGE);
            }

            fechaEgreso = rsRecords2.getDate("FechaEgreso");
            fechaIngreso = rsRecords2.getDate("FechaIngreso");

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al traer Fecha de Ingreso y Egereso : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            return diasTrabajados;
        }


        try {
            queryString = " SELECT count(*) AS TOTALDIASAUSENTEDESCUENTO";
            queryString += " FROM empleado_asistencia ea";
            queryString += " INNER JOIN razon_ausencia ra ON ra.Razon = ea.Razon";
            queryString += " WHERE ea.IdEmpleado = " + idProveedor;
            queryString += " AND ea.Fecha  Between '" + Utileria.getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
            queryString += "               And '" + Utileria.getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";
            queryString += " AND Upper(ea.razon) IN ('LICENCIA')";
            queryString += " AND ra.EsDescuentoEnPlanilla = 1";

            rsRecords1 = stQuery1.executeQuery(queryString);

            if(rsRecords1.next()) {
                diasLicenciaSinPago = rsRecords1.getInt("TOTALDIASAUSENTEDESCUENTO");
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al traer Fecha de Ingreso y Egereso : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            return diasTrabajados;
        }

        // ----------------------------- Dias Totales -------------------------------
        diffInMillies = Math.abs(fechaIngreso.getTime() - fechaEgreso.getTime());
        diasTrabajados[0] = (int)TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1 ;

        // ---------------------- Dias Totales Mes - Licencia -----------------------
        Date inicioMes = new Date(fechaEgreso.getYear(), fechaEgreso.getMonth(), 1);

        // Si entro y salio el mismo mes dar los dias correctos
        if(fechaIngreso.getMonth() == fechaEgreso.getMonth() && fechaIngreso.getYear() == fechaEgreso.getYear()){
            diasTrabajados[1] = diasTrabajados[0];
        } else{
            diffInMillies = Math.abs(inicioMes.getTime() - fechaEgreso.getTime());
            diasTrabajados[1] = (int)TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
        }

        // Si trabajo mas de factorDiasMes reducir a factorDiasMes
        if (diasTrabajados[1] > factorDiasMes){
            diasTrabajados[1] = factorDiasMes;
        }

        diasTrabajados[1] = diasTrabajados[1] - diasLicenciaSinPago ; // El dia "De Baja" cuenta para la paga

        if(diasTrabajados[1] < 0){
            diasTrabajados[1] = 0;
        }
        // ------------------------------- Licencia ---------------------------------

        diasTrabajados[2] = diasLicenciaSinPago;

        return diasTrabajados;
    }

    private long getDaysDiff(Date dInicio, Date dFinal){
        long diffInMillies = Math.abs(dInicio.getTime() - dFinal.getTime());
        return (int)TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - CALCULO DE PLANILLA");
    }
}
