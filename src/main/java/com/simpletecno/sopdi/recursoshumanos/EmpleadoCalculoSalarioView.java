package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.*;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
@SuppressWarnings("ALL")
public class EmpleadoCalculoSalarioView extends VerticalLayout implements View {
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
    public static final String TOTAL_PROVISIONADO_PLANILLA = "Total provisioado";
    public static final String FECHA_INICIALSF_PLANILLA = "FISF";
    public static final String FECHA_FINALSF_PLANILLA = "FFSF";

    public static final String IDEMPLEADO = "IdEmpleado";
    public static final String EMPLEADO = "Nombre";
    public static final String CARGO = "Cargo";

    public static final String SALARIO_BASE = "Salario base";
    public static final String DIAS_LABORADOS = "Dias laborados";
    public static final String LICENCIAS = "Licencias";
    public static final String SALARIO_DEVENGADO = "Devengado";
    public static final String HORAS_EXTRA = "Hrs Extra";
    public static final String HORAS_EXTRAII = "Hrs ExtraII";
    public static final String EXTRAORDINARIO = "Extraordinario";
    public static final String BONO1 = "37-2001";
    public static final String BONO2 = "78-89";
    public static final String BONO3 = "Otros Ingresos";
    public static final String BONO4 = "AGUI / B14";
    public static final String BONO5 = "EVENTOS";
    public static final String COMISION = "Comisión";
    public static final String INGRESOS = "Tot.Ingresos";
    public static final String DESCUENTO1 = "CuotLabIgss";
    public static final String DESCUENTO2 = "Descto1";
    public static final String DESCUENTO3 = "ISR";
    public static final String DESCUENTO4 = "Anticipo Anterior";
    public static final String DESCUENTO5 = "Descto5";
    public static final String DESCUENTOS = "Tot.Desctos.";
    public static final String LIQUIDO = "Liquido";
    public static final String PROVISION1 = "CuotPatIgss";
    public static final String PROVISION2 = "ProvBono14";
    public static final String PROVISION3 = "ProvAguinaldo";
    public static final String PROVISION4 = "Provi4";
    public static final String PROVISION5 = "Provi5";
    public static final String PROVISIONADO = "Tot.Provi.";
    public static final String CODIGOPARTIDA = "CodigoPartida";
    public static final String CHEQUE = "Cheque";

    public IndexedContainer planillaContainer = new IndexedContainer();
    Grid planillaGrid;

    public IndexedContainer planillaDetalleContainer = new IndexedContainer();
    Grid planillaDetalleGrid;

    private void itemClick(ItemClickEvent event) {
        if (event != null) {
            if (event.isDoubleClick()) {
                if (planillaDetalleGrid.getSelectedRows() == null) {
                    return;
                }
                EmpleadoPlanillaExtraForm empleadoPlanillaExtraForm =
                        new EmpleadoPlanillaExtraForm(
                                event.getItemId(),
                                String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue()),
                                String.valueOf(planillaDetalleContainer.getContainerProperty(event.getItemId(), IDEMPLEADO).getValue())
                                        + " " + String.valueOf(planillaDetalleContainer.getContainerProperty(event.getItemId(), EMPLEADO).getValue()),
                                String.valueOf(planillaDetalleContainer.getContainerProperty(event.getItemId(), IDEMPLEADO).getValue()),
                                0
                        );
                empleadoPlanillaExtraForm.setModal(true);
                UI.getCurrent().addWindow(empleadoPlanillaExtraForm);
                empleadoPlanillaExtraForm.center();
                empleadoPlanillaExtraForm.montoTxt.focus();
            }
        }
    }

    private static enum TIPO_Provision {
        BONO14,
        AGUINALDO
    }

    Button nuevaPlanillaBtn = new Button("Nueva");
    Button editarPlanillaBtn = new Button("Editar");
    Button eliminarPlanillaBtn = new Button("Eliminar");
    Button guardarPlanillaBtn = new Button("Guardar");
    Button cancelarPlanillaBtn = new Button("Cancelar");
    Button calcularPlanillaBtn = new Button("Calcular");

    Button generarPlanillaBtn = new Button("Generar");
    Button excluirEmpleadosBtn = new Button("Excluir empleados");

    NumberField correlativoInicialChequeTxt = new NumberField();
    DateField fechaChequeDt = new DateField();

    DateField fechaInicioDt = new DateField("Fecha inicial :");
    DateField fechaFinDt = new DateField("Fecha final :");
    ComboBox tipoPlanillaCbx = new ComboBox("Tipo :");
    TextField descripcionPlanillaTxt = new TextField("Descripción:");
    CheckBox aguinaldoChb = new CheckBox("+ Aguinaldo?");
    CheckBox bono14Chb = new CheckBox("+ Bono 14?");

    UI mainUI;
    Statement stQuery, stQuery1;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords, rsRecords1, rsRecords2;
    String queryString;
    boolean nuevaPlanilla;

    MultiFileUpload singleUpload;
    public File planillaFile;
//    public XSSFWorkbook workbook;
    public HSSFWorkbook workbook;
//    public XSSFSheet sheet, sheet2;
    public HSSFSheet sheet;
    private FileInputStream fileInputStream;

    DecimalFormat df = new DecimalFormat("######0.00");
    double montoBaseRetenerISR = 0.00;
    double factorHoraExtra = 0.00;
    double factorHoraExtraDoble = 0.00;
    int factorDiasMes = 30;
    double factorDiasAnio = 365;;
    double porcentajeCuotaLaboralIGSS = 4.83;
    double porcentajeCuotaPatronalIGSS = 12.67;
    double valorAnticipo1a5Dias = 0.00;
    double valorAnticipo1a10Dias = 0.00;
    double valorAnticipo1a15Dias = 0.00;
    double valorEvento = 0.00;

    String idProveedor = "";

    long daysDiff = 0;
    java.util.GregorianCalendar dateBefore; //tambien fecha de ingreso
    java.util.GregorianCalendar dateAfter; //tambien fecha fin planilla

    int totalEmpleados = 0;
    double totalPagado = 0.00;
    double totalProvisionado = 0.00;

    Button generarPlanillaIgssBtn;

    VerticalLayout mainLayout = new VerticalLayout();

    public EmpleadoCalculoSalarioView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label("CALCULO DE PLANILLA");
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
        planillaContainer.addContainerProperty(TOTAL_PROVISIONADO_PLANILLA, Double.class, 0);
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
            } else if (TOTAL_PROVISIONADO_PLANILLA.equals(cellReference.getPropertyId())) {
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
                    excluirEmpleadosBtn.setEnabled(true);
                    correlativoInicialChequeTxt.setVisible(true);
                    fechaChequeDt.setVisible(true);
                    generarPlanillaIgssBtn.setEnabled(false);

                    if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ESTATUS_PLANILLA).getValue()).equals("GENERADA")) {
                        correlativoInicialChequeTxt.setVisible(false);
                        fechaChequeDt.setVisible(false);
                        editarPlanillaBtn.setEnabled(false);
                        eliminarPlanillaBtn.setEnabled(false);
                        calcularPlanillaBtn.setEnabled(false);
                        generarPlanillaBtn.setEnabled(false);
                        excluirEmpleadosBtn.setEnabled(false);
                        generarPlanillaIgssBtn.setEnabled(true);
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

                    aguinaldoChb.setValue(rsRecords.getString("IncluyeAguinaldo").equals("S"));
                    bono14Chb.setValue(rsRecords.getString("IncluyeBono14").equals("S"));

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
//        correlativoInicialChequeTxt.setValue(0);
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
        tipoPlanillaCbx.addItem("Anticipo");
        tipoPlanillaCbx.addItem("Salario");
        tipoPlanillaCbx.addItem("Anticipo + provisión");
        tipoPlanillaCbx.addItem("Salario + provisión");
        tipoPlanillaCbx.addItem("Solo provisión");
        tipoPlanillaCbx.select("Anticipo");
        tipoPlanillaCbx.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if(   String.valueOf(tipoPlanillaCbx.getValue()).contains("Anticipo")
                   || String.valueOf(tipoPlanillaCbx.getValue()).contains("Solo provisión") ) {
                    fechaFinDt.setValue(new java.util.Date().from(Utileria.getUltimoDiaDelMes().toInstant().minus(16,ChronoUnit.DAYS)));
                }
                else {
                    fechaFinDt.setValue(Utileria.getUltimoDiaDelMes());
                }
            }
        });

        fechaInicioDt.setWidth("100%");
        fechaInicioDt.setDateFormat("dd/MMM/yyyy");
        fechaInicioDt.setValue(Utileria.getPrimerDiaDelMes());
        fechaFinDt.setWidth("100%");
        fechaFinDt.setDateFormat("dd/MMM/yyyy");
        fechaFinDt.setValue((new java.util.Date().from(Utileria.getUltimoDiaDelMes().toInstant().minus(16,ChronoUnit.DAYS))));

        descripcionPlanillaTxt.setWidth("100%");
        descripcionPlanillaTxt.setMaxLength(128);
        descripcionPlanillaTxt.setDescription("Nombre descriptivo de la planilla");

//        estatusPlanillaCbx.setTextInputAllowed(false);
//        estatusPlanillaCbx.setNewItemsAllowed(false);
//        estatusPlanillaCbx.setNullSelectionAllowed(false);
//        estatusPlanillaCbx.addItem("EN PROCESO");
//        estatusPlanillaCbx.addItem("AUTORIZADA");
//        estatusPlanillaCbx.select("EN PROCESO");
//        estatusPlanillaCbx.setReadOnly(true);

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

        bono14Chb.setValue(false);
        aguinaldoChb.setValue(false);
        bono14Chb.setVisible(false);
        aguinaldoChb.setVisible(false);
        aguinaldoChb.setEnabled(false);
        bono14Chb.setEnabled(false);

        fechaFinDt.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }

            bono14Chb.setValue(false);
            aguinaldoChb.setValue(false);
            bono14Chb.setVisible(false);
            aguinaldoChb.setVisible(false);
            aguinaldoChb.setEnabled(false);
            bono14Chb.setEnabled(false);

            if(    !String.valueOf(tipoPlanillaCbx.getValue()).toUpperCase().contains("PROVIS")) {
                return;
            }
            java.util.GregorianCalendar today = new java.util.GregorianCalendar();
            today.setTime(fechaFinDt.getValue());
            if((today.get(java.util.GregorianCalendar.MONTH) + 1) == 7) { // JULIO bono 14
                bono14Chb.setValue(true);
                aguinaldoChb.setValue(false);
                bono14Chb.setVisible(true);
                aguinaldoChb.setVisible(false);
            }
            else if((today.get(java.util.GregorianCalendar.MONTH) + 1) == 12) { // DICIEMBRE aguinaldo
                bono14Chb.setValue(false);
                aguinaldoChb.setValue(true);
                bono14Chb.setVisible(false);
                aguinaldoChb.setVisible(true);
            }
        });

        planillaDatosLayout.addComponents(tipoPlanillaCbx, fechaInicioDt, fechaFinDt, descripcionPlanillaTxt, aguinaldoChb, bono14Chb, guardarPlanillaBtn, cancelarPlanillaBtn);
        planillaDatosLayout.setComponentAlignment(tipoPlanillaCbx, Alignment.BOTTOM_LEFT);
        planillaDatosLayout.setComponentAlignment(fechaInicioDt, Alignment.BOTTOM_LEFT);
        planillaDatosLayout.setComponentAlignment(fechaFinDt, Alignment.BOTTOM_LEFT);
        planillaDatosLayout.setComponentAlignment(descripcionPlanillaTxt, Alignment.BOTTOM_CENTER);
        planillaDatosLayout.setComponentAlignment(bono14Chb, Alignment.BOTTOM_RIGHT);
        planillaDatosLayout.setComponentAlignment(aguinaldoChb, Alignment.BOTTOM_RIGHT);
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
        planillaDetalleContainer.addContainerProperty(SALARIO_BASE, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DIAS_LABORADOS, Integer.class, 0);
        planillaDetalleContainer.addContainerProperty(LICENCIAS, Integer.class, 0);
        planillaDetalleContainer.addContainerProperty(SALARIO_DEVENGADO, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(HORAS_EXTRA, Integer.class, 0);
        planillaDetalleContainer.addContainerProperty(HORAS_EXTRAII, Integer.class, 0);
        planillaDetalleContainer.addContainerProperty(EXTRAORDINARIO, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO1, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO2, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO3, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO4, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(BONO5, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(COMISION, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(INGRESOS, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTO1, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTO2, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTO3, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTO4, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTO5, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(DESCUENTOS, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(LIQUIDO, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(PROVISION1, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(PROVISION2, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(PROVISION3, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(PROVISION4, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(PROVISION5, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(PROVISIONADO, Double.class, 0.00);
        planillaDetalleContainer.addContainerProperty(CODIGOPARTIDA, String.class, "");
        planillaDetalleContainer.addContainerProperty(CHEQUE, String.class, "");

        planillaDetalleGrid = new Grid("EMPLEADOS DE PLANILLA DE : " + tipoPlanillaCbx.getValue(), planillaDetalleContainer);
        planillaDetalleGrid.setImmediate(true);
        planillaDetalleGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        planillaDetalleGrid.setHeightMode(HeightMode.ROW);
        planillaDetalleGrid.setHeightByRows(10);
        planillaDetalleGrid.setSizeFull();

        planillaDetalleGrid.getColumn(ID_PLANILLA).setExpandRatio(1).setHidden(true).setHidable(true);
        planillaDetalleGrid.getColumn(IDEMPLEADO).setExpandRatio(1);
        planillaDetalleGrid.getColumn(EMPLEADO).setExpandRatio(4);
        planillaDetalleGrid.getColumn(DIAS_LABORADOS).setExpandRatio(1);
        planillaDetalleGrid.getColumn(LICENCIAS).setExpandRatio(1);
        planillaDetalleGrid.getColumn(SALARIO_BASE).setExpandRatio(1);
        planillaDetalleGrid.getColumn(HORAS_EXTRA).setExpandRatio(1);
        planillaDetalleGrid.getColumn(HORAS_EXTRAII).setExpandRatio(1);
        planillaDetalleGrid.getColumn(EXTRAORDINARIO).setExpandRatio(1);
        planillaDetalleGrid.getColumn(SALARIO_DEVENGADO).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO1).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO2).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO3).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO4).setExpandRatio(1);
        planillaDetalleGrid.getColumn(BONO5).setExpandRatio(1);
        planillaDetalleGrid.getColumn(COMISION).setExpandRatio(1).setHidable(true).setHidden(true);
        planillaDetalleGrid.getColumn(INGRESOS).setExpandRatio(2);
        planillaDetalleGrid.getColumn(DESCUENTO1).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DESCUENTO2).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DESCUENTO3).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DESCUENTO4).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DESCUENTO5).setExpandRatio(1);
        planillaDetalleGrid.getColumn(DESCUENTOS).setExpandRatio(2);
        planillaDetalleGrid.getColumn(LIQUIDO).setExpandRatio(2);
        planillaDetalleGrid.getColumn(PROVISION1).setExpandRatio(1);
        planillaDetalleGrid.getColumn(PROVISION2).setExpandRatio(1);
        planillaDetalleGrid.getColumn(PROVISION3).setExpandRatio(1);
        planillaDetalleGrid.getColumn(PROVISION4).setExpandRatio(1);
        planillaDetalleGrid.getColumn(PROVISION5).setExpandRatio(1);
        planillaDetalleGrid.getColumn(PROVISIONADO).setExpandRatio(2);
        planillaDetalleGrid.getColumn(CODIGOPARTIDA).setExpandRatio(1);
        planillaDetalleGrid.getColumn(CHEQUE).setExpandRatio(1);

        planillaDetalleGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DIAS_LABORADOS.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (LICENCIAS.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (HORAS_EXTRA.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (SALARIO_BASE.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALARIO_DEVENGADO.equals(cellReference.getPropertyId())) {
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
            } else if (COMISION.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (INGRESOS.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DESCUENTO1.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DESCUENTO2.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DESCUENTO3.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DESCUENTO4.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DESCUENTO5.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LIQUIDO.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROVISION1.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROVISION2.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROVISION3.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROVISION4.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROVISION5.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROVISIONADO.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        planillaDetalleGrid.addItemClickListener(this::itemClick);

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

        excluirEmpleadosBtn.setIcon(FontAwesome.MINUS_CIRCLE);
        excluirEmpleadosBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        excluirEmpleadosBtn.setDescription("Excluir empleados de esta planilla.");
        excluirEmpleadosBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (planillaDetalleContainer.size() == 0) {
                Notification notif = new Notification("No hay planilla.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
            else {
                for (Object idEmpleado : planillaDetalleContainer. getItemIds()) {
                    if(planillaDetalleGrid.isSelected(idEmpleado)) {
                        queryString = "DELETE FROM planilla_detalle ";
                        queryString += " WHERE IdPlanilla = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();
                        queryString += " AND IdEmpleado = " + planillaDetalleContainer.getContainerProperty(idEmpleado, IDEMPLEADO).getValue();

                        try {

                            stQuery.executeUpdate(queryString);

                        } catch (SQLException e) {
                            Notification notif = new Notification("Error al excluir empleados. " + e.getMessage(),
                                    Notification.Type.ERROR_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());
                            return;
                        }
                    }
                }//endfor
                for (Object idEmpleado : planillaDetalleGrid.getSelectedRows()) {
                    planillaDetalleContainer.removeItem(idEmpleado);
                }//endfor

                Notification.show("Empleados excluidos de la planilla.", Notification.Type.TRAY_NOTIFICATION);
            }
        });

        generarPlanillaIgssBtn = new Button("Plantilla IGSS");
        generarPlanillaIgssBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        generarPlanillaIgssBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        generarPlanillaIgssBtn.setDescription("Exportar a Excel la planilla IGSS");
        generarPlanillaIgssBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (planillaDetalleContainer.size() == 0) {
                Notification notif = new Notification("No hay planilla.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
            else {
                PlanillaIGSSGenerator planillaIGSSGenerator = new PlanillaIGSSGenerator((String)planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue());
                UI.getCurrent().addWindow(planillaIGSSGenerator);
                planillaIGSSGenerator.center();
                //generarPlanillaIgss();
            }
        });

        UploadFinishedHandler handler;
        handler = (InputStream stream, String fileName, String mimeType, long length) -> {
            File targetFile;

            try {

                System.out.println("\nfileName=" + fileName);
                System.out.println("length=" + stream.available());
                System.out.println("mimeType=" + mimeType);

                byte[] buffer = new byte[stream.available()];
                stream.read(buffer);
                String filePath = VaadinService.getCurrent()
                        .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId() + "/";

                new File(filePath).mkdirs();

                fileName = filePath + fileName;
                targetFile = new File(fileName);
                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);
                outStream.close();
                stream.close();

                System.out.println("\ntargetFile = " + fileName);

                cargarArchivo(targetFile);

                planillaFile = targetFile;

                //   cargarBtn.setEnabled(true);
            } catch (java.io.IOException fIoEx) {
                fIoEx.printStackTrace();
                Notification.show("Error al cargar el archivo FEL SAR adjunto!", Notification.Type.ERROR_MESSAGE);
                return;
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler, window, false);
//        singleUpload.setCaption("Cargar archivo");
//        singleUpload2.setRootDirectory(newDoc.getAbsolutePath());
//        singleUpload.setPanelCaption("Buscar");
//        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar archivo (Excel xls)", "");

        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.xls')");
 //       JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.xlsx')");

        List<String> acceptedMimeTypes = new ArrayList();
//        acceptedMimeTypes.add("application/octet-stream");
        acceptedMimeTypes.add("application/ovnd.ms-excel");
        acceptedMimeTypes.add("application/msexcel");
        acceptedMimeTypes.add("application/x-msexcel");
        acceptedMimeTypes.add("application/x-ms-excel");
        acceptedMimeTypes.add("application/x-excel");
        acceptedMimeTypes.add("application/x-dos_ms_excel");
        acceptedMimeTypes.add("application/xls");
        acceptedMimeTypes.add("application/x-xls");
        //       singleUpload.setAcceptedMimeTypes(acceptedMimeTypes);

        botonesLayout.addComponent(excluirEmpleadosBtn);
        botonesLayout.setComponentAlignment(excluirEmpleadosBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(exportPlanillaBtn);
        botonesLayout.setComponentAlignment(exportPlanillaBtn, Alignment.BOTTOM_RIGHT);
        botonesLayout.addComponent(singleUpload);
        botonesLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_RIGHT);
        botonesLayout.addComponent(generarPlanillaIgssBtn);
        botonesLayout.setComponentAlignment(generarPlanillaIgssBtn, Alignment.BOTTOM_RIGHT);
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
            queryString += ",'" + (aguinaldoChb.getValue() ? "S" : "N") + "'";
            queryString += ",'" + (bono14Chb.getValue() ? "S" : "N") + "'";
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

        boolean aplicaAnticipo;

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
            long diasPlanilla = 1 + TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            java.util.GregorianCalendar dateOfJournal = new java.util.GregorianCalendar();
            dateOfJournal.setTime(rsRecords.getDate("FechaInicio"));

            //1 parametros de planilla
            queryString = "SELECT *";
            queryString += " FROM planilla_parametros";
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

            rsRecords = stQuery.executeQuery(queryString);

            if(!rsRecords.next()) {
                Notification.show("ERROR,  no se encontraron parámetros de configuraciones de planilla de esta empresa!", Notification.Type.ERROR_MESSAGE);
                return;
            }

            montoBaseRetenerISR = rsRecords.getDouble("MontoBaseRetenerISR");
            factorHoraExtra = rsRecords.getDouble("FactorHoraExtra");
            factorHoraExtraDoble = rsRecords.getDouble("FactorHoraExtraII");
            factorDiasMes = rsRecords.getInt("FactorDiasMes");
            factorDiasAnio = rsRecords.getInt("FactorDiasAnio");
            porcentajeCuotaLaboralIGSS = rsRecords.getDouble("PorcentajeCuotaLaboralIgss");
            porcentajeCuotaPatronalIGSS = rsRecords.getDouble("PorcentajeCuotaPatronalIgss");
            valorAnticipo1a5Dias = rsRecords.getDouble("ValorAnticipo1a5Dias");
            valorAnticipo1a10Dias = rsRecords.getDouble("ValorAnticipo1a10Dias");
            valorAnticipo1a15Dias = rsRecords.getDouble("ValorAnticipo1a15Dias");
            valorEvento = rsRecords.getDouble("ValorEvento");

            totalPagado = 0.00;
            totalProvisionado = 0.00;

            // 2 : query por cada empleado activo
            queryString = "SELECT *";
            queryString += " FROM proveedor_empresa";
            queryString += " WHERE EsPlanilla = 1";
            queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND EstatusTrabajo NOT IN ('DE BAJA')";
            queryString += " AND ISNULL(FechaIngreso) = 0";
            queryString += " AND ISNULL(FechaEgreso) = 1";
//            if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).equals("Anticipo")) {
            if(String.valueOf(tipoPlanillaCbx.getValue()).equals("Anticipo")) {
                queryString += " AND AplicaAnticipoSalario = 1";
            }
            queryString += " ORDER BY IdProveedor";

//System.out.println("queryString=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {

                java.util.Date fechaFinPlanilla;

                int diasLaborados = 0;

                int diasAusenciaSinPago = 0;
                int horasExtra;
                int horasExtraDoble;
                int eventos;
                double salarioBase;
                double ordinario;
                double extraOrdinario;
                double totalIngresos;
                double totalEgresos;
                double liquidoRecibir;
                double bonificaciones[] = new double[5];
                double descuentos[] = new double[5];

                do {
                    aplicaAnticipo = (rsRecords.getInt("AplicaAnticipoSalario") == 1);

                    idProveedor = rsRecords.getString("IdProveedor");

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
                    salarioBase = 0.00;
                    ordinario = 0.00;
                    extraOrdinario = 0.00;
                    totalIngresos = 0.00;
                    totalEgresos = 0.00;
                    liquidoRecibir = 0.00;
                    bonificaciones[0] = 0.00;bonificaciones[1] = 0.00;bonificaciones[2] = 0.00;bonificaciones[3] = 0.00;bonificaciones[4] = 0.00;
                    descuentos[0] = 0.00;descuentos[1] = 0.00;descuentos[2] = 0.00;descuentos[3] = 0.00;descuentos[4] = 0.00;

//                    if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).equals("Anticipo")) {

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
                        horasExtra = rsRecords1.getInt("TOTALHORASEXTRAS");
                        horasExtraDoble = rsRecords1.getInt("TOTALHORASEXTRASDOBLE");
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

                    // 4 : query de bitacora para contar dias AUSENTE que se descuentan en el rango de fechas (lo que importan son las ausencias que son objeto de descuento)
                    queryString = " SELECT count(*) AS TOTALDIASAUSENTEDESCUENTO";
                    queryString += " FROM empleado_asistencia ea";
                    queryString += " INNER JOIN razon_ausencia ra ON ra.Razon = ea.Razon";
                    queryString += " WHERE ea.IdEmpleado = " + idProveedor;
                    queryString += " AND ea.Fecha  Between '" + Utileria.getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
                    queryString += "               And '" + Utileria.getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";
                    queryString += " AND Upper(ea.Estatus) <> 'PRESENTE'";
                    queryString += " AND ra.EsDescuentoEnPlanilla = 1";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(rsRecords1.next()) {
                        diasAusenciaSinPago = rsRecords1.getInt("TOTALDIASAUSENTEDESCUENTO");
                    }

                    try {
                        dateBefore = new java.util.GregorianCalendar();
                        dateBefore.setTime(rsRecords.getDate("FechaIngreso"));

                        dateAfter = new java.util.GregorianCalendar();
                        dateAfter.setTime(fechaFinDt.getValue());
                        if(aguinaldoChb.getValue()) {
                            dateAfter.setTime(fechaInicioDt.getValue());
                            dateAfter.add(GregorianCalendar.DATE, -1);
                        }
                        if(bono14Chb.getValue()) {
                            dateAfter.setTime(fechaFinDt.getValue());
                            dateAfter.add(GregorianCalendar.DATE, -1);
                        }

                        long dateBeforeInMs = dateBefore.getTimeInMillis();
                        long dateAfterInMs = dateAfter.getTimeInMillis();

                        long timeDiff = Math.abs(dateAfterInMs - dateBeforeInMs);

                        daysDiff = 1 + TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

                    } catch(Exception e){
                        e.printStackTrace();
                        Notification.show("ERROR : al calcular los dias : " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
                        return;
                    }

//System.out.println("daysDiff = " + daysDiff);
                    // 5 : query para obtener valor del salario y bonificaciones

                    queryString = " SELECT *";
                    queryString += " FROM empleado_salario";
                    queryString += " WHERE IdEmpleado = " + idProveedor;

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(rsRecords1.next()) {
                        int idx = 0;
                        do {

                            if (rsRecords1.getInt("EsOrdinario") == 1) {
                                if(rsRecords1.getDouble("Valor") > 2000) {
                                    salarioBase = rsRecords1.getDouble("Valor");
                                }
                                ordinario = rsRecords1.getDouble("Valor");
                            }
                            else {
                                if(    !String.valueOf(tipoPlanillaCbx.getValue()).contains("Anticipo")
                                    && !String.valueOf(tipoPlanillaCbx.getValue()).contains("Solo")) {
                                    if(rsRecords1.getString("IdNomenclatura").equals(((SopdiUI)mainUI).cuentasContablesDefault.getBonificacionDCTO07_2001())){
                                        bonificaciones[0] = rsRecords1.getDouble("Valor");
                                    }
                                    if(rsRecords1.getString("IdNomenclatura").equals(((SopdiUI)mainUI).cuentasContablesDefault.getBonificacionDCTO78_89())){
                                        bonificaciones[1] = rsRecords1.getDouble("Valor");
                                    }
                                }
                            }
                        } while(rsRecords1.next());
                    }

                    // 6 : calculo de salario devengado (salario / 365) * dias_laborados
                    if(String.valueOf(tipoPlanillaCbx.getValue()).contains("Anticipo")) {
                        if(daysDiff <= 5) {
                            liquidoRecibir = valorAnticipo1a5Dias;
                        }
                        else if(daysDiff <= 10) {
                            liquidoRecibir = valorAnticipo1a10Dias;
                        } else liquidoRecibir = valorAnticipo1a15Dias;

                        diasLaborados = 15;
                        diasLaborados-= diasAusenciaSinPago;

                        if(diasLaborados <= 0) {
                            liquidoRecibir = 0;
                        }

                        if(aplicaAnticipo) {
                            bonificaciones[2] = liquidoRecibir; //EL ANTICIPO
                        }
                        else {
                            liquidoRecibir = 0.00;
                        }

                        if(aplicaAnticipo) {
                            bonificaciones[2] = liquidoRecibir; //EL ANTICIPO
                        }
                        else {
                            liquidoRecibir = 0.00;
                        }

                        //BONO14 O AGUINALDO
                        if(bono14Chb.getValue()) {
                            bonificaciones[3] = getProvision(idProveedor, TIPO_Provision.BONO14, salarioBase);
                        }
                        if(aguinaldoChb.getValue()) {
                            bonificaciones[3] = getProvision(idProveedor, TIPO_Provision.AGUINALDO, salarioBase);
                        }

                        liquidoRecibir += bonificaciones[3];


                    }
                    else { //planilla salario

                        if(daysDiff < diasPlanilla) {
                            diasLaborados = (int)daysDiff;
                        }

                        if(diasLaborados < 0) {
                            diasLaborados = 0;
                        }
                        if(String.valueOf(tipoPlanillaCbx.getValue()).equals("Solo provisión")) {
                            //BONO14 O AGUINALDO
                            if(bono14Chb.getValue()) {
                                bonificaciones[3] = getProvision(idProveedor, TIPO_Provision.BONO14, salarioBase);
                            }
                            if(aguinaldoChb.getValue()) {
                                bonificaciones[3] = getProvision(idProveedor, TIPO_Provision.AGUINALDO, salarioBase);
                            }

                            liquidoRecibir = bonificaciones[3];
                        }
                        else {

                            //diasLaborados ya viene con factorDiasMes

                            if(dateOfJournal.get(GregorianCalendar.MONTH) == 1) { // febrero
                                diasLaborados = dateOfJournal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
                            }
                            diasLaborados -= diasAusenciaSinPago;

                            //System.out.println("dateOfJournal.get(GregorianCalendar.MONTH)=" + dateOfJournal.get(GregorianCalendar.MONTH) + " diasLaborados=" + diasLaborados + " diasPlanilla=" + diasPlanilla);

                            if(diasLaborados < 0) {
                                diasLaborados = 0;
                            }

                            if(diasAusenciaSinPago == 0) {
                                diasLaborados = getDiasTrabajados();
                            }

                            bonificaciones[0] = (Utileria.round(bonificaciones[0] * ((double)diasLaborados / factorDiasMes)));
                            bonificaciones[1] = (Utileria.round(bonificaciones[1] * ((double)diasLaborados / factorDiasMes)));
                            //                        bonificaciones[2] = LA ESPECIAL CARGADA POR EXCEL
                            //                        bonificaciones[3] = SOLO EN ANTICIPO Y ES LA Provision BONO14 O AGUINALDO

                            // ordinario = (ordinario / factorDiasMes) * diasLaborados;  PARA HORAS EXTRA ES EL BASE SIN DESCONTAR DIAS
                            // 7 : calculo de extraordinario (horas_extra * valor_horas_extr
                            extraOrdinario = ((Utileria.round((ordinario / factorDiasMes) / 8) * factorHoraExtra) * horasExtra);
                            extraOrdinario += ((Utileria.round((ordinario / factorDiasMes) / 8) * factorHoraExtraDoble) * horasExtraDoble);

                            ordinario = Utileria.round((ordinario / factorDiasMes) * diasLaborados); //DEVENGADO

                            totalIngresos = Utileria.round((ordinario + extraOrdinario));
                            totalIngresos += Utileria.round((bonificaciones[0] + bonificaciones[1] + bonificaciones[2] + bonificaciones[3] + bonificaciones[4]));

                            if (!String.valueOf(tipoPlanillaCbx.getValue()).contains("Solo")) {
                                descuentos[0] = Utileria.round((ordinario + extraOrdinario) * (porcentajeCuotaLaboralIGSS / 100));

                                if (((totalIngresos - descuentos[0]) - montoBaseRetenerISR) > 0) { //ingresos - cuotalaboraligss
                                    descuentos[2] = Utileria.round((((totalIngresos - descuentos[0]) - montoBaseRetenerISR) * 0.05)); //isr
                                }

                                descuentos[3] = anticipoPrevio(fechaInicioDt.getValue());

                                totalEgresos = (descuentos[0] + descuentos[1] + descuentos[2] + descuentos[3] + descuentos[4]);
                            }

                            liquidoRecibir = (totalIngresos - totalEgresos);
                        }

                    }

                    bonificaciones[4] = eventos * valorEvento; //BONO5

                    //System.out.println("Empleado=[" + idProveedor + "] liquidoRecibir=[" + liquidoRecibir + "]");

                    Object itemId = planillaDetalleContainer.addItem();
                    // planillaContainer.getContainerProperty(itemId, IDPLANILLA).setValue(rsRecords.getString("Id"));
                    planillaDetalleContainer.getContainerProperty(itemId, IDEMPLEADO).setValue(idProveedor);
                    planillaDetalleContainer.getContainerProperty(itemId, EMPLEADO).setValue(rsRecords.getString("Nombre"));
                    planillaDetalleContainer.getContainerProperty(itemId, CARGO).setValue(rsRecords.getString("Cargo"));
                    planillaDetalleContainer.getContainerProperty(itemId, SALARIO_BASE).setValue(salarioBase);
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_LABORADOS).setValue(diasLaborados);
                    planillaDetalleContainer.getContainerProperty(itemId, LICENCIAS).setValue(diasAusenciaSinPago);
                    // INGRESOS
                    if(   String.valueOf(tipoPlanillaCbx.getValue()).contains("Anticipo")
                       || String.valueOf(tipoPlanillaCbx.getValue()).contains("Solo")) {
                        planillaDetalleContainer.getContainerProperty(itemId, SALARIO_DEVENGADO).setValue(0.00);
                    }
                    else {
                        planillaDetalleContainer.getContainerProperty(itemId, SALARIO_DEVENGADO).setValue(ordinario);
                    }
                    planillaDetalleContainer.getContainerProperty(itemId, HORAS_EXTRA).setValue(horasExtra);
                    planillaDetalleContainer.getContainerProperty(itemId, HORAS_EXTRAII).setValue(horasExtraDoble);
                    planillaDetalleContainer.getContainerProperty(itemId, EXTRAORDINARIO).setValue(extraOrdinario);
                    planillaDetalleContainer.getContainerProperty(itemId, BONO1).setValue(Utileria.round(bonificaciones[0])); // BONIFICACION DCTO 37-2001
                    planillaDetalleContainer.getContainerProperty(itemId, BONO2).setValue(Utileria.round(bonificaciones[1])); // BONIFICACION DCTO 78-89
                    planillaDetalleContainer.getContainerProperty(itemId, BONO3).setValue(Utileria.round(bonificaciones[2])); // CARGADO DESDE EXCEL...
                    planillaDetalleContainer.getContainerProperty(itemId, BONO4).setValue(Utileria.round(bonificaciones[3]));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO5).setValue(Utileria.round(bonificaciones[4]));
                    planillaDetalleContainer.getContainerProperty(itemId, COMISION).setValue(0.00);// ??
                    planillaDetalleContainer.getContainerProperty(itemId, INGRESOS).setValue(Utileria.round(totalIngresos));
                    // EGRESOS
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO1).setValue(Utileria.round(descuentos[0])); // CUOTA LABORAL IGSS
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO2).setValue(Utileria.round(descuentos[1]));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO3).setValue(Utileria.round(descuentos[2]));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO4).setValue(Utileria.round(descuentos[3]));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO5).setValue(Utileria.round(descuentos[4]));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTOS).setValue(Utileria.round(totalEgresos));

//                    planillaDetalleContainer.getContainerProperty(itemId, LIQUIDO).setValue(liquidoRecibir);
                    planillaDetalleContainer.getContainerProperty(itemId, LIQUIDO).setValue(Utileria.round(liquidoRecibir));
//                    planillaDetalleContainer.getContainerProperty(itemId, LIQUIDO).setValue(new BigDecimal(liquidoRecibir).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    // PROVISIONES
                    if (    String.valueOf(tipoPlanillaCbx.getValue()).contains("Anticipo")
                         || String.valueOf(tipoPlanillaCbx.getValue()).contains("Solo")) {
                        planillaDetalleContainer.getContainerProperty(itemId, PROVISION1).setValue(0.00); // CUOTA PATRONAL IGSS
                        planillaDetalleContainer.getContainerProperty(itemId, PROVISION2).setValue(0.00); //AGINALDO
                        planillaDetalleContainer.getContainerProperty(itemId, PROVISION3).setValue(0.00); //BONO14
                    }
                    else {
                        planillaDetalleContainer.getContainerProperty(itemId, PROVISION1).setValue(Utileria.round((ordinario + extraOrdinario) * (porcentajeCuotaPatronalIGSS / 100))); //CUOTA PATRONAL IGSS
                        planillaDetalleContainer.getContainerProperty(itemId, PROVISION2).setValue(Utileria.round((ordinario) / 12)); //AGINALDO
                        planillaDetalleContainer.getContainerProperty(itemId, PROVISION3).setValue(Utileria.round((ordinario) / 12)); //BONO14
                    }
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION4).setValue(0.00);
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION5).setValue(0.00);
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISIONADO).setValue(0.00);

                    totalEmpleados++;

                    totalPagado+= liquidoRecibir;
                    totalProvisionado+= (double)planillaDetalleContainer.getContainerProperty(itemId, PROVISION2).getValue();
                    totalProvisionado+= (double)planillaDetalleContainer.getContainerProperty(itemId, PROVISION3).getValue();
                    totalProvisionado+= (double)planillaDetalleContainer.getContainerProperty(itemId, PROVISION4).getValue();
                    totalProvisionado+= (double)planillaDetalleContainer.getContainerProperty(itemId, PROVISION5).getValue();

                } while (rsRecords.next());

                planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_EMPLEADOS_PLANILLA).setValue(totalEmpleados);
                planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_PAGADO_PLANILLA).setValue(totalPagado);
                planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_PROVISIONADO_PLANILLA).setValue(totalProvisionado);

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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "INICIO - CALCULANDO PLANILLA EMPLEADOS");

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

            java.util.GregorianCalendar dateOfJournal = new java.util.GregorianCalendar();
            dateOfJournal.setTime(rsRecords.getDate("FechaInicio"));

            //1 parametros de planilla
            queryString = "SELECT *";
            queryString += " FROM planilla_parametros";
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

            rsRecords = stQuery.executeQuery(queryString);

            if(!rsRecords.next()) {
                Notification.show("ERROR,  no se encontraron parámetros de configuraciones de planilla de esta empresa!", Notification.Type.ERROR_MESSAGE);
                return;
            }

            montoBaseRetenerISR = rsRecords.getDouble("MontoBaseRetenerISR");
            factorHoraExtra = rsRecords.getDouble("FactorHoraExtra");
            factorHoraExtraDoble = rsRecords.getDouble("FactorHoraExtraII");
            factorDiasMes = rsRecords.getInt("FactorDiasMes");
            factorDiasAnio = rsRecords.getInt("FactorDiasAnio");
            porcentajeCuotaLaboralIGSS = rsRecords.getDouble("PorcentajeCuotaLaboralIgss");
            porcentajeCuotaPatronalIGSS = rsRecords.getDouble("PorcentajeCuotaPatronalIgss");
            valorAnticipo1a5Dias = rsRecords.getDouble("ValorAnticipo1a5Dias");
            valorAnticipo1a10Dias = rsRecords.getDouble("ValorAnticipo1a10Dias");
            valorAnticipo1a15Dias = rsRecords.getDouble("ValorAnticipo1a15Dias");
            valorEvento = rsRecords.getDouble("ValorEvento");

            totalPagado = 0.00;
            totalProvisionado = 0.00;

            boolean aplicaAnticipo;

            java.util.Date fechaFinPlanilla;
            int diasLaborados = 0;
            int diasAusenciaSinPago;
            int horasExtra;
            int horasExtraDoble;
            int eventos;
            double salarioBase;
            double ordinario;
            double extraOrdinario;
            double totalIngresos;
            double totalEgresos;
            double liquidoRecibir;
            double bonificaciones[] = new double[5];
            double descuentos[] = new double[5];
            Item item;

            for (Object itemObject : planillaDetalleContainer.getItemIds()) {

                item = planillaDetalleContainer.getItem(itemObject);

                idProveedor =  String.valueOf(item.getItemProperty(IDEMPLEADO).getValue());

                // 2 : query por cada empleado activo
                queryString = "SELECT *";
                queryString += " FROM proveedor";
                queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                queryString += " AND IdProveedor = " + idProveedor;
                if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).equals("Anticipo")) {
                    queryString += " AND AplicaAnticipoSalario = 1";
                }

                rsRecords = stQuery.executeQuery(queryString);

                if(rsRecords.next()) {

                    aplicaAnticipo = (rsRecords.getInt("AplicaAnticipoSalario") == 1);

//System.out.println("Empleado = " + rsRecords.getString("IdProveedor") + " " + rsRecords.getString("Nombre"));
                    // 2.1 : query por cada empleado buscar si ya está en otra planilla del aniomes(añomes)...del mismo tipo de planilla..
                    queryString = "SELECT pla.*";
                    queryString += " FROM planilla_encabezado pla ";
                    queryString += " INNER JOIN planilla_detalle plade ON plade.IdPlanilla = pla.Id";
                    queryString += " WHERE plade.IdEmpleado = " + idProveedor;
                    queryString += " AND pla.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += " AND pla.AnioMes = " + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), MES_PLANILLA).getValue());
                    queryString += " AND pla.Tipo = '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()) + "'";
                    queryString += " AND pla.Estatus <> 'EN PROCESO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(rsRecords1.next()) {

                        continue;
                    }

                    fechaFinPlanilla = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_FINALSF_PLANILLA).getValue()));

                    diasLaborados = getDiasTrabajados();
                    /*
                    if(dateOfJournal.get(GregorianCalendar.MONTH) == 1) { // febrero
                        diasLaborados = dateOfJournal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
                    }
                    */

                    if(idProveedor.equals("922294")) {
                        System.out.println("----------------- O K ---------------");
                    }

                    diasAusenciaSinPago = 0;
                    horasExtra = 0;
                    horasExtraDoble = 0;
                    eventos = 0;
                    salarioBase = 0.00;
                    ordinario = 0.00;
                    extraOrdinario = 0.00;
                    totalIngresos = 0.00;
                    totalEgresos = 0.00;
                    liquidoRecibir = 0.00;
                    bonificaciones[0] = (double)item.getItemProperty(BONO1).getValue();
                    bonificaciones[1] = (double)item.getItemProperty(BONO2).getValue();
                    bonificaciones[2] = (double)item.getItemProperty(BONO3).getValue();
                    bonificaciones[3] = (double)item.getItemProperty(BONO4).getValue();
                    bonificaciones[4] = (double)item.getItemProperty(BONO5).getValue();

                    descuentos[0] = Utileria.round((double)item.getItemProperty(DESCUENTO1).getValue());
                    descuentos[1] = Utileria.round((double)item.getItemProperty(DESCUENTO2).getValue());
                    descuentos[2] = Utileria.round((double)item.getItemProperty(DESCUENTO3).getValue());
                    descuentos[3] = Utileria.round((double)item.getItemProperty(DESCUENTO4).getValue());
                    descuentos[4] = Utileria.round((double)item.getItemProperty(DESCUENTO5).getValue());

//                    if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).equals("Anticipo")) {

                    // 3 : query de bitacora para contar las horas extra
                    queryString = " SELECT IFNULL(SUM(HorasExtra), 0) TOTALHORASEXTRAS, IFNULL(SUM(HorasExtraDoble), 0) TOTALHORASEXTRASDOBLE";
                    queryString += " FROM empleado_asistencia";
                    queryString += " WHERE IdEmpleado = " + idProveedor;
                    queryString += " AND Fecha Between '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_INICIALSF_PLANILLA).getValue()) + "'";
                    queryString += "               And '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_FINALSF_PLANILLA).getValue())   + "'";

                    rsRecords1 = stQuery1.executeQuery(queryString);
//System.out.println("calcular query horas extra : " + queryString);

                    if(rsRecords1.next()) {
                        horasExtra = rsRecords1.getInt("TOTALHORASEXTRAS");
                        horasExtraDoble = rsRecords1.getInt("TOTALHORASEXTRASDOBLE");
                    }

                    // 3.1 : query de bitacora para contar eventos
                    queryString = " SELECT IFNULL(SUM(Eventos), 0) TOTALEVENTOS";
                    queryString += " FROM empleado_asistencia";
                    queryString += " WHERE IdEmpleado = " + idProveedor;
                    queryString += " AND Fecha Between '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_INICIALSF_PLANILLA).getValue()) + "'";
                    queryString += "               And '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_FINALSF_PLANILLA).getValue())   + "'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(rsRecords1.next()) {
                        eventos = rsRecords1.getInt("TOTALEVENTOS");
                    }

                    // 4 : query de bitacora para contar dias AUSENTE que se descuentan en el rango de fechas (lo que importan son las ausencias que son objeto de descuento)
                    queryString = " SELECT count(*) AS TOTALDIASAUSENTEDESCUENTO";
                    queryString += " FROM empleado_asistencia ea";
                    queryString += " INNER JOIN razon_ausencia ra ON ra.Razon = ea.Razon";
                    queryString += " WHERE ea.IdEmpleado = " + idProveedor;
                    queryString += " AND ea.Fecha Between '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_INICIALSF_PLANILLA).getValue()) + "'";
                    queryString += "                  AND '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), FECHA_FINALSF_PLANILLA).getValue()) + "'";
                    queryString += " AND Upper(ea.Estatus) <> 'PRESENTE'";
                    queryString += " AND ra.EsDescuentoEnPlanilla = 1";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(rsRecords1.next()) {
                        diasAusenciaSinPago = rsRecords1.getInt("TOTALDIASAUSENTEDESCUENTO");
                    }

                    // 5 : query para obtener valor del salario y bonificaciones
                    queryString = " SELECT *";
                    queryString += " FROM empleado_salario";
                    queryString += " WHERE IdEmpleado = " + rsRecords.getString("IdProveedor");

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(rsRecords1.next()) {
                        do {

                            if (rsRecords1.getInt("EsOrdinario") == 1) {
                                if(rsRecords1.getDouble("Valor") > 2000) {
                                    salarioBase = rsRecords1.getDouble("Valor");
                                }
                                ordinario = rsRecords1.getDouble("Valor");
                            }
                            else {
                                if(    !String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Anticipo")
                                    && !String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Solo")) {
                                    if(rsRecords1.getString("IdNomenclatura").equals(((SopdiUI)mainUI).cuentasContablesDefault.getBonificacionDCTO07_2001())){
                                        bonificaciones[0] = rsRecords1.getDouble("Valor");
                                    }
                                    if(rsRecords1.getString("IdNomenclatura").equals(((SopdiUI)mainUI).cuentasContablesDefault.getBonificacionDCTO78_89())){
                                        bonificaciones[1] = rsRecords1.getDouble("Valor");
                                    }
                                }
                            }
                        } while(rsRecords1.next());
                    }

                    try {
                        dateBefore = new java.util.GregorianCalendar();
                        dateBefore.setTime(rsRecords.getDate("FechaIngreso"));

                        dateAfter = new java.util.GregorianCalendar();
                        dateAfter.setTime(fechaFinPlanilla);
                        if(aguinaldoChb.getValue()) {
                            dateAfter.setTime(fechaInicioDt.getValue());
                            dateAfter.add(GregorianCalendar.DATE, -1);
                        }
                        if(bono14Chb.getValue()) {
                            dateAfter.setTime(fechaFinDt.getValue());
                            dateAfter.add(GregorianCalendar.DATE, -1);
                        }

                        long dateBeforeInMs = dateBefore.getTimeInMillis();
                        long dateAfterInMs = dateAfter.getTimeInMillis();
                                              // 31/10/2023    23/01/2023
                        long timeDiff = Math.abs(dateAfterInMs - dateBeforeInMs);

                        daysDiff = 1 + TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
//if(rsRecords.getString("IdProveedor").equals("97405"))  {
//    System.out.println("fechaIngreso= " + Utileria.getFechaYYYYMMDD_1(rsRecords.getDate("FechaIngreso")));
//    System.out.println("fechaFinPlanilla = " + Utileria.getFechaYYYYMMDD_1(fechaFinPlanilla));
//    System.out.println("97405 = daysDiff = " + daysDiff);
//}

//System.out.println("daysDiff = " + daysDiff);
                    } catch(Exception e){
                        e.printStackTrace();
                        Notification.show("ERROR : al calcular los dias : " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
                        return;
                    }

                    // 6 : calculo de salario devengado (salario / 365) * dias_laborados
                    if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Anticipo")) {

                        if(daysDiff <= 5) {
                            liquidoRecibir = valorAnticipo1a5Dias;
                        }
                        else if(daysDiff <= 10) {
                            liquidoRecibir = valorAnticipo1a10Dias;
                        } else liquidoRecibir = valorAnticipo1a15Dias;

                        diasLaborados = 15;
                        diasLaborados-= diasAusenciaSinPago;

                        if(diasLaborados <= 0) {
                            liquidoRecibir = 0;
                        }

                        if(aplicaAnticipo) {
                            bonificaciones[2] = liquidoRecibir; //EL ANTICIPO
                        }
                        else {
                            liquidoRecibir = 0.00;
                        }

                        //Provision BONO14 O AGUINALDO
                        if(bono14Chb.getValue()) {
                            bonificaciones[3] = getProvision(idProveedor, TIPO_Provision.BONO14,salarioBase);
                        }
                        if(aguinaldoChb.getValue()) {
                            bonificaciones[3] = getProvision(idProveedor, TIPO_Provision.AGUINALDO,salarioBase);
                        }

                        liquidoRecibir+= bonificaciones[3];

                    }
                    else {

                        if(diasLaborados < 0) {
                            diasLaborados = 0;
                        }

                        if(String.valueOf(tipoPlanillaCbx.getValue()).toUpperCase().contains("PROVIS")) {
                            //BONO14 O AGUINALDO
                            if(bono14Chb.getValue()) {
                                bonificaciones[3] = getProvision(idProveedor, TIPO_Provision.BONO14, salarioBase);
                            }
                            if(aguinaldoChb.getValue()) {
                                bonificaciones[3] = getProvision(idProveedor, TIPO_Provision.AGUINALDO, salarioBase);
                            }

                            liquidoRecibir = bonificaciones[3];
                        }
                        else {

                            //diasLaborados ya viene con factorDiasMes
                            // En el que el mes sea febrero y no hay trabajado nada, pagarle nada
                            if((dateOfJournal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) - diasAusenciaSinPago) == 0) {
                                diasLaborados = 0;
                            } else {
                                diasLaborados -= diasAusenciaSinPago;
                            }


                            //System.out.println("dateOfJournal.get(GregorianCalendar.MONTH)=" + dateOfJournal.get(GregorianCalendar.MONTH) + " diasLaborados=" + diasLaborados + " diasPlanilla=" + diasPlanilla);

                            if(diasLaborados < 0) {
                                diasLaborados = 0;
                            }

                            if((diasAusenciaSinPago == 0) && (daysDiff == 0)) {
                                diasLaborados = getDiasTrabajados();
                            }

                            //ordinario = (ordinario / factorDiasMes) * diasLaborados; HORAS EXTRA SE CALCULA POR SALARIOBASES
                            // 7 : calculo de extraordinario (horas_extra * valor_horas_extra)
                            extraOrdinario = Utileria.round((((ordinario / factorDiasMes) / 8) * factorHoraExtra) * horasExtra);
                            extraOrdinario += Utileria.round((((ordinario / factorDiasMes) / 8) * factorHoraExtraDoble) * horasExtraDoble);

                            ordinario = Utileria.round(((ordinario / factorDiasMes) * diasLaborados)); //DEVENGADO

                            bonificaciones[0] = (Utileria.round(bonificaciones[0] * ((double)diasLaborados / factorDiasMes)));
                            bonificaciones[1] = (Utileria.round(bonificaciones[1] * ((double)diasLaborados / factorDiasMes)));
                            //                        bonificaciones[2] = ((bonificaciones[2] / factorDiasMes) * diasLaborados); se respeta lo ingresado como bonificación especial no sujeta a dias laborados
                            //                        bonificaciones[2] = LA ESPECIAL CARGADA POR EXCEL

                            bonificaciones[4] = eventos * valorEvento; //BONO5

                            totalIngresos = Utileria.round((ordinario + extraOrdinario));
                            totalIngresos += Utileria.round((bonificaciones[0] + bonificaciones[1] + bonificaciones[2] + bonificaciones[3] + bonificaciones[4]));

                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "CALCULANDO PLANILLA EMPLEADOS: " + idProveedor + " " + totalIngresos);

                            if(!String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Solo")) {
                                descuentos[0] = Utileria.round(((ordinario + extraOrdinario) * (porcentajeCuotaLaboralIGSS / 100)));

                                if (((totalIngresos - descuentos[0]) - montoBaseRetenerISR) > 0) { //ingresos - cuotalaboraligss
                                    descuentos[2] = Utileria.round(((totalIngresos - descuentos[0]) - montoBaseRetenerISR) * 0.05); //isr
                                }

                            }

                            totalEgresos += (descuentos[0] + descuentos[1] + descuentos[2] + descuentos[3] + descuentos[4]);

                            liquidoRecibir = (totalIngresos - totalEgresos);
                        }

                    }

                    // Proposito Visual unicamente, los calculos se hacen sobre 30 dias.
                    if(dateOfJournal.get(GregorianCalendar.MONTH) == 1 && diasLaborados > 29) { // febrero
                        diasLaborados = dateOfJournal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) - diasAusenciaSinPago;
                    }

                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "CALCULANDO PLANILLA EMPLEADOS Bonos: b1 " + bonificaciones[0] + " |b2 " + bonificaciones[1] + " |b3 " +  bonificaciones[2] + " |b4 " +  bonificaciones[3] + " |b5 " +  bonificaciones[4] + " |O " + ordinario + " |E "  +  extraOrdinario);

                    // planillaContainer.getContainerProperty(itemId, IDPLANILLA).setValue(rsRecords.getString("Id"));
                    item.getItemProperty(IDEMPLEADO).setValue(idProveedor);
                    item.getItemProperty(EMPLEADO).setValue(rsRecords.getString("Nombre"));
                    item.getItemProperty(CARGO).setValue(rsRecords.getString("Cargo"));
                    item.getItemProperty(SALARIO_BASE).setValue(salarioBase);
                    item.getItemProperty(DIAS_LABORADOS).setValue(diasLaborados);
                    item.getItemProperty(LICENCIAS).setValue(diasAusenciaSinPago);
                    // INGRESOS
                    if(    String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Anticipo")
                        || String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Solo")) {
                        item.getItemProperty(SALARIO_DEVENGADO).setValue(0.00);
                    }
                    else {
                        item.getItemProperty(SALARIO_DEVENGADO).setValue(ordinario);
                    }
                    item.getItemProperty(HORAS_EXTRA).setValue(horasExtra);
                    item.getItemProperty(HORAS_EXTRAII).setValue(horasExtraDoble);
                    item.getItemProperty(EXTRAORDINARIO).setValue(extraOrdinario);
                    item.getItemProperty(BONO1).setValue(Utileria.round(bonificaciones[0])); // BONIFICACION DCTO 37-2001
                    item.getItemProperty(BONO2).setValue(Utileria.round(bonificaciones[1])); // BONIFICACION DCTO 78-89
                    item.getItemProperty(BONO3).setValue(Utileria.round(bonificaciones[2])); // CARGADO DESDE EXCEL...
                    item.getItemProperty(BONO4).setValue(Utileria.round(bonificaciones[3]));
                    item.getItemProperty(BONO5).setValue(Utileria.round(bonificaciones[4]));
                    item.getItemProperty(COMISION).setValue(0.00); //??
                    item.getItemProperty(INGRESOS).setValue(Utileria.round(totalIngresos));
                    // EGRESOS
                    item.getItemProperty(DESCUENTO1).setValue(Utileria.round(descuentos[0])); //CUOTA LABORAL IGSS
                    item.getItemProperty(DESCUENTO2).setValue(Utileria.round(descuentos[1]));
                    item.getItemProperty(DESCUENTO3).setValue(Utileria.round(descuentos[2])); //ISR
                    item.getItemProperty(DESCUENTO4).setValue(Utileria.round(descuentos[3]));
                    item.getItemProperty(DESCUENTO5).setValue(Utileria.round(descuentos[4]));
                    item.getItemProperty(DESCUENTOS).setValue(Utileria.round(totalEgresos));
//                    item.getItemProperty(LIQUIDO).setValue(liquidoRecibir);
                    item.getItemProperty(LIQUIDO).setValue(Utileria.round(liquidoRecibir));
//                    item.getItemProperty(LIQUIDO).setValue(new BigDecimal(liquidoRecibir).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    // PROVISIONES
                    if(   String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Anticipo")
                       || String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Solo")) {
                        item.getItemProperty(PROVISION1).setValue(0.00); //CUOTA PATRONAL IGSS
                        item.getItemProperty(PROVISION2).setValue(0.00); //AGUINALDO
                        item.getItemProperty(PROVISION3).setValue(0.00); //BONO14
                    }
                    else {
                        item.getItemProperty(PROVISION1).setValue(Utileria.round((ordinario + extraOrdinario) * (porcentajeCuotaPatronalIGSS / 100))); //CUOTA PATRONAL IGSS
                        item.getItemProperty(PROVISION2).setValue(Utileria.round((ordinario) / 12)); //AGUINALDO
                        item.getItemProperty(PROVISION3).setValue(Utileria.round((ordinario) / 12)); //BONO14
                    }
                    item.getItemProperty(PROVISION4).setValue(0.00);
                    item.getItemProperty(PROVISION5).setValue(0.00);
                    item.getItemProperty(PROVISIONADO).setValue(
                            (double)item.getItemProperty(PROVISION1).getValue()
                            + (double)item.getItemProperty(PROVISION2).getValue()
                            + (double)item.getItemProperty(PROVISION3).getValue()
                            + (double)item.getItemProperty(PROVISION4).getValue()
                            + (double)item.getItemProperty(PROVISION5).getValue()
                            );
                    totalPagado+= liquidoRecibir;
                    totalProvisionado+= (double)item.getItemProperty(PROVISION2).getValue();
                    totalProvisionado+= (double)item.getItemProperty(PROVISION2).getValue();
                    totalProvisionado+= (double)item.getItemProperty(PROVISION2).getValue();
                    totalProvisionado+= (double)item.getItemProperty(PROVISION2).getValue();

                } // no encontro al proveedor/empleado
            } //endfor

            planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_EMPLEADOS_PLANILLA).setValue(planillaDetalleContainer.size());
            planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_PAGADO_PLANILLA).setValue(totalPagado);
            planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TOTAL_PROVISIONADO_PLANILLA).setValue(totalProvisionado);

            planillaDetalleGrid.setCaption("Planilla de " + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()) + " " + planillaDetalleContainer.size() + " empleados...");

            guardarPlanilla(
                    Integer.valueOf(
                            String.valueOf(
                                    planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue())));

            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Fin - CALCULANDO PLANILLA EMPLEADOS");

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla CALCULO DE PLANILLA EMPLEADOS : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }


    public void fillPlanillaGrid(int nuevaPlanillaId)
    {
        planillaContainer.removeAllItems();
        planillaDetalleContainer.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT *";
            queryString += " FROM planilla_encabezado";
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
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
                    planillaContainer.getContainerProperty(itemId, TOTAL_PROVISIONADO_PLANILLA).setValue(rsRecords.getDouble("TotalProvisionado"));
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
                //colocar valores de layout de encabezado de planilla para poder modificar, si fuera necesario.
                tipoPlanillaCbx.select(rsRecords.getString("Tipo"));
                tipoPlanillaCbx.setValue(rsRecords.getString("Tipo"));

                fechaInicioDt.setValue(rsRecords.getDate("FechaInicio"));
                fechaFinDt.setValue(rsRecords.getDate("FechaFin"));
                descripcionPlanillaTxt.setValue(rsRecords.getString("Descripcion"));
                aguinaldoChb.setValue(rsRecords.getString("IncluyeAguinaldo").equals("S"));
                bono14Chb.setValue(rsRecords.getString("IncluyeBono14").equals("S"));
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
                    planillaDetalleContainer.getContainerProperty(itemId, SALARIO_BASE).setValue(rsRecords.getDouble("SueldoBase"));
                    planillaDetalleContainer.getContainerProperty(itemId, DIAS_LABORADOS).setValue(rsRecords.getInt("DiasLaborados"));
                    planillaDetalleContainer.getContainerProperty(itemId, LICENCIAS).setValue(rsRecords.getInt("Licencia"));
                    // INGRESOS
                    planillaDetalleContainer.getContainerProperty(itemId, SALARIO_DEVENGADO).setValue(rsRecords.getDouble("SalarioDevengado"));
                    planillaDetalleContainer.getContainerProperty(itemId, HORAS_EXTRA).setValue(rsRecords.getInt("HorasExtra"));
                    planillaDetalleContainer.getContainerProperty(itemId, HORAS_EXTRAII).setValue(rsRecords.getInt("HorasExtraII"));
                    planillaDetalleContainer.getContainerProperty(itemId, EXTRAORDINARIO).setValue(rsRecords.getDouble("SalarioExtraordinario"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO1).setValue(rsRecords.getDouble("Bonificacion1"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO2).setValue(rsRecords.getDouble("Bonificacion2"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO3).setValue(rsRecords.getDouble("Bonificacion3"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO4).setValue(rsRecords.getDouble("Bonificacion4"));
                    planillaDetalleContainer.getContainerProperty(itemId, BONO5).setValue(rsRecords.getDouble("Bonificacion5"));
                    planillaDetalleContainer.getContainerProperty(itemId, COMISION).setValue(rsRecords.getDouble("Comision"));
                    planillaDetalleContainer.getContainerProperty(itemId, INGRESOS).setValue(rsRecords.getDouble("TotalIngresos"));
                    // EGRESOS
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO1).setValue(rsRecords.getDouble("Descuento1"));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO2).setValue(rsRecords.getDouble("Descuento2"));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO3).setValue(rsRecords.getDouble("Descuento3"));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO4).setValue(rsRecords.getDouble("Descuento4"));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTO5).setValue(rsRecords.getDouble("Descuento5"));
                    planillaDetalleContainer.getContainerProperty(itemId, DESCUENTOS).setValue(rsRecords.getDouble("TotalDescuentos"));
                    planillaDetalleContainer.getContainerProperty(itemId, LIQUIDO).setValue(rsRecords.getDouble("LiquidoRecibir"));
                    // PROVISIONES
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION1).setValue(rsRecords.getDouble("Provision1"));
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION2).setValue(rsRecords.getDouble("Provision2"));
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION3).setValue(rsRecords.getDouble("Provision3"));
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION4).setValue(rsRecords.getDouble("Provision4"));
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISION5).setValue(rsRecords.getDouble("Provision5"));
                    planillaDetalleContainer.getContainerProperty(itemId, PROVISIONADO).setValue(rsRecords.getDouble("TotalProvisionado"));
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
/***
        try {
            queryString = "SELECT plade.LiquidoRecibir";
            queryString += " FROM planilla_encabezado pla ";
            queryString += " INNER JOIN planilla_detalle plade ON plade.IdPlanilla = pla.Id";
            queryString += " WHERE plade.IdEmpleado = " + rsRecords.getString("IdProveedor");
            queryString += " AND pla.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND pla.AnioMes = " + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), MES_PLANILLA).getValue());
            queryString += " AND pla.Tipo = 'Anticipo'";
            queryString += " AND pla.Estatus <> 'EN PROCESO'";

            rsRecords1 = stQuery1.executeQuery(queryString);

            if(rsRecords1.next()) {
                montoAnticipo = rsRecords1.getDouble("LiquidoRecibir");
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al buscar anticipo de planilla de empleado : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
***/
        try {
            queryString = "SELECT Debe";
            queryString += " FROM contabilidad_partida ";
            queryString += " WHERE IdProveedor = " + rsRecords.getString("IdProveedor");
            queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND Extract(YEAR_MONTH FROM Fecha) = " + new Utileria().getFechaYYYYMM(fechaPlanilla);
            queryString += " AND TipoDocumento = 'CHEQUE'";
            queryString += " AND IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposSueldos();
//if(rsRecords.getString("IdProveedor") == "97405") {
//    System.out.println("\n\n405 = " + queryString + "\n\n");
//}
            rsRecords1 = stQuery1.executeQuery(queryString);

            if(rsRecords1.next()) {
                montoAnticipo = rsRecords1.getDouble("Debe");
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

        String empleado_error = "";

        try {

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            queryString = "UPDATE planilla_encabezado SET";
            queryString += "  Tipo = '" + tipoPlanillaCbx.getValue() + "'";
            queryString += ", FechaInicio = '" + Utileria.getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
            queryString += ", FechaFin = '" + Utileria.getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";
            queryString += ", AnioMes = " + new Utileria().getFechaYYYYMM(fechaInicioDt.getValue());
            queryString += ", Descripcion = '" + descripcionPlanillaTxt.getValue() + "'";
            queryString += ", IncluyeAguinaldo = '" + (aguinaldoChb.getValue() ? "S" : "N") + "'";
            queryString += ", IncluyeBono14 = '" + (bono14Chb.getValue() ? "S" : "N") + "'";
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND Id = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

            stQuery.executeUpdate(queryString);

            queryString = "DELETE FROM planilla_detalle ";
            queryString += " WHERE IdPlanilla = " + planillaId;

            stQuery.executeUpdate(queryString);

            for (Object itemObject : planillaDetalleContainer.getItemIds()) {

                Item item = planillaDetalleContainer.getItem(itemObject);

                empleado_error = (String)item.getItemProperty(IDEMPLEADO).getValue();

                queryString = "  INSERT INTO planilla_detalle (IdPlanilla, IdEmpleado, SueldoBase, DiasLaborados, Licencia, SalarioDevengado, ";
                queryString += " HorasExtra, HorasExtraII, SalarioExtraordinario, Bonificacion1, Bonificacion2, Bonificacion3,";
                queryString += " Bonificacion4, Bonificacion5, Comision, TotalIngresos, Descuento1, Descuento2, Descuento3, ";
                queryString += " Descuento4, Descuento5, TotalDescuentos, LiquidoRecibir, Provision1, Provision2, ";
                queryString += " Provision3, Provision4, Provision5, TotalProvisionado";
                queryString += ")";
                queryString += " VALUES ";
                queryString += "(";
                queryString +=       planillaId;
                queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                queryString += "," + item.getItemProperty(SALARIO_BASE).getValue();
                queryString += "," + item.getItemProperty(DIAS_LABORADOS).getValue();
                queryString += "," + item.getItemProperty(LICENCIAS).getValue();
                queryString += "," + item.getItemProperty(SALARIO_DEVENGADO).getValue();
                queryString += "," + item.getItemProperty(HORAS_EXTRA).getValue();
                queryString += "," + item.getItemProperty(HORAS_EXTRAII).getValue();
                queryString += "," + item.getItemProperty(EXTRAORDINARIO).getValue();
                queryString += "," + item.getItemProperty(BONO1).getValue();
                queryString += "," + item.getItemProperty(BONO2).getValue();
                queryString += "," + item.getItemProperty(BONO3).getValue();
                queryString += "," + item.getItemProperty(BONO4).getValue();
                queryString += "," + item.getItemProperty(BONO5).getValue();
                queryString += "," + item.getItemProperty(COMISION).getValue();
                queryString += "," + item.getItemProperty(INGRESOS).getValue();
                queryString += "," + item.getItemProperty(DESCUENTO1).getValue();
                queryString += "," + item.getItemProperty(DESCUENTO2).getValue();
                queryString += "," + item.getItemProperty(DESCUENTO3).getValue();
                queryString += "," + item.getItemProperty(DESCUENTO4).getValue();
                queryString += "," + item.getItemProperty(DESCUENTO5).getValue();
                queryString += "," + item.getItemProperty(DESCUENTOS).getValue();
                queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                queryString += "," + item.getItemProperty(PROVISION1).getValue();
                queryString += "," + item.getItemProperty(PROVISION2).getValue();
                queryString += "," + item.getItemProperty(PROVISION3).getValue();
                queryString += "," + item.getItemProperty(PROVISION4).getValue();
                queryString += "," + item.getItemProperty(PROVISION5).getValue();
                queryString += "," + item.getItemProperty(PROVISIONADO).getValue();
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al guardar tabla PLANILLAS : " + ex + " | Empleado: " + empleado_error);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void generarPlanilla() {

        if (String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Anticipo")) {
            generarPlanillaAnticipo();
        } else if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).contains("Salario")) {
            generarPlanillaMensual();
        } else if(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), TIPO_PLANILLA).getValue()).equals("Solo provisión")) {
            generarPlanillaSoloProvision();
        }
    }

    private void generarPlanillaAnticipo() {
        try {

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

            String codigoPartida, codigoCC;
            int correlativoCheque = (int)correlativoInicialChequeTxt.getDoubleValueDoNotThrow();

            long diffInMillies = Math.abs(rsRecords.getDate("FechaInicio").getTime() - rsRecords.getDate("FechaFin").getTime());
            long diasPlanilla = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

            double totalPagado = 0.00;

            String fecha = Utileria.getFechaYYYYMMDD_1(fechaChequeDt.getValue());
            String ultimoEncontado;
            String dia = fecha.substring(8, 10);
            String mes = fecha.substring(5, 7);
            String año = fecha.substring(0, 4);

            codigoPartida = rsRecords.getString("IdEmpresa") + año + mes + dia + "3";

            queryString = " SELECT codigoPartida FROM contabilidad_partida ";
            queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
            queryString += " ORDER BY codigoPartida DESC ";

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado
                ultimoEncontado = rsRecords1.getString("CodigoPartida").substring(12, 15);
                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

            double totalProvisioado = 0.00;

            queryString = "";

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            for (Object itemObject : planillaDetalleContainer.getItemIds()) {

                Item item = planillaDetalleContainer.getItem(itemObject);

                if((double)item.getItemProperty(LIQUIDO).getValue() == 0.00) {
                    continue;
                }

//2026-03-04                codigoCC = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "10000000" + item.getItemProperty(IDEMPLEADO).getValue().toString().substring(2);
                //2210000000343
                codigoCC = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "100000" + item.getItemProperty(IDEMPLEADO).getValue().toString().substring(0);
                //Si no se paga nada, saltar Empleado
                if((double)item.getItemProperty(LIQUIDO).getValue() <= 0) continue;

                queryString  = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
                queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor, NombreCheque,";
                queryString += " MontoDocumento, SerieDocumento, NumeroDocumento, TipoDOCA, NoDOCA, IdNomenclatura, MonedaDocumento, Debe, Haber,";
                queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,";
                queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora, IdCentroCosto, CodigoCentroCosto, SueldoOrdinario)";
                queryString += " VALUES ";

                if((double)item.getItemProperty(BONO4).getValue() > 0.00) { //AGUINALDO O BONO 14

                    //la fecha de las partidas de planilla por pagar (sueldos por pagar) debe ser 1 dia antes de la fecha de cheque...
                    String fechaProvision = Utileria.getFechaYYYYMMDD_1(new java.util.Date().from(fechaChequeDt.getValue().toInstant().minus(1, ChronoUnit.DAYS)));
                    dia = fecha.substring(8, 10);
                    mes = fecha.substring(5, 7);
                    año = fecha.substring(0, 4);
                    String codigoPartidaProvision = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + año + mes + dia + "8";

                    String queryString1 = " SELECT codigoPartida FROM contabilidad_partida ";
                    queryString1 += " WHERE codigoPartida LIKE '" + codigoPartidaProvision + "%'";
                    queryString1 += " ORDER BY codigoPartida DESC ";

                    stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords1 = stQuery1.executeQuery(queryString1);

                    if (rsRecords.next()) { //  encontrado
//                01234567890123
//                22202311308000
//                12345678901234
                        ultimoEncontado = rsRecords1.getString("CodigoPartida").substring(12, 15);


                        codigoPartidaProvision += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

                    } else {
                        codigoPartidaProvision += "001";
                    }
//                22202311301000
                    System.out.println("FECHA PARTIDA PLANILLA AGUINALDO/BONO14 = " + fechaProvision + " CodigoPartidaProvision = " + codigoPartidaProvision );

//2026-03-04                    codigoCC = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "10000000" + item.getItemProperty(IDEMPLEADO).getValue().toString().substring(2);
                    //2210000000343
                    codigoCC = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "100000" + item.getItemProperty(IDEMPLEADO).getValue().toString().substring(0);
                    queryString += " (";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartidaProvision + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fechaProvision + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(BONO4).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getProvisionAguinaldo(); //PROVISION AGUINALDO
                    queryString += ",'QUETZALES'";
                    queryString += "," + item.getItemProperty(BONO4).getValue(); // DEBE
                    queryString += ",0.00"; // HABER
                    queryString += "," + item.getItemProperty(BONO4).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(BONO4).getValue();
                    queryString += ",'PLANILLA AGUINALDO " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";

                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartidaProvision + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'PLANILLA'";
                    queryString += ",'" + fechaProvision + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(BONO4).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(item.getItemProperty(IDEMPLEADO).getValue()) + String.format("%03d", Integer.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()))) + "'";  //numero documento
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getSueldosPorPagar();
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + item.getItemProperty(BONO4).getValue();
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + item.getItemProperty(BONO4).getValue();
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(BONO4).getValue();
                    queryString += ",'PLANILLA AGUINALDO " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                //CHEQUE BANCOS
                queryString += " (";
                queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += ",'CHEQUE'";
                queryString += ",'" + fecha + "'";
                queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                queryString += ",''";//nitproveedor
                queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                queryString += ",'" + String.valueOf(correlativoCheque)+ "'"; //numero
                queryString += ",''"; //tipodoca
                queryString += ",''"; //doca
                queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal(); //BANCOS
                queryString += ",'QUETZALES'";
                queryString += ",0.00"; // DEBE
                queryString += "," + item.getItemProperty(LIQUIDO).getValue(); // HABER
                queryString += ",0.00"; //DEBE Q.
                queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                queryString += ",1.0";
                queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                if((double)item.getItemProperty(BONO4).getValue() > 0.00) { //ANTICIPO + AGUINALDO O BONO 14
                    queryString += ",'ANTICIPO QUINCENA + AGUINALDO " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                }
                else {
                    queryString += ",'ANTICIPO QUINCENA " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                }
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",0";
                queryString += ",'0'";
                queryString += ", " + null + ")";

                if((double)item.getItemProperty(BONO4).getValue() > 0.00) { //ANTICIPO + AGUINALDO O BONO 14
                    //ANTICIPO
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'CHEQUE'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(correlativoCheque)+ "'"; //numero
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposSueldos();
                    queryString += ",'QUETZALES'";
                    queryString += "," + item.getItemProperty(BONO3).getValue(); //anticipo
                    queryString += ",0.00"; //HABER
                    queryString += "," + item.getItemProperty(BONO3).getValue(); //anticio
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'ANTICIPO QUINCENA + AGUINALDO " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";

                    //AGUINALDO O BONO14
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'CHEQUE'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(correlativoCheque)+ "'"; //numero
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getSueldosPorPagar();//AGUINALDO O BONO14
                    queryString += ",'QUETZALES'";
                    queryString += "," + item.getItemProperty(BONO4).getValue(); //AGUINALDO O BONO14
                    queryString += ",0.00"; //HABER
                    queryString += "," + item.getItemProperty(BONO4).getValue(); //AGUINALDO O BONO14
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'ANTICIPO QUINCENA + AGUINALDO " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";

                }
                else { //SOLO ANTICIPO
                    queryString += ",(";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'CHEQUE'";
                    queryString += ",'" + fecha + "'";
                    queryString += "," + item.getItemProperty(IDEMPLEADO).getValue();
                    queryString += ",''";//nitproveedor
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CORRELATIVO_PLANILLA).getValue()) + "'";  //serie documento
                    queryString += ",'" + String.valueOf(correlativoCheque)+ "'"; //numero
                    queryString += ",''"; //tipodoca
                    queryString += ",''"; //doca
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposSueldos();
                    queryString += ",'QUETZALES'";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",0.00"; //HABER Q.
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'ANTICIPO QUINCENA " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY INSERT ANTICIPO QUINCENA : " + queryString);

                stQuery1.executeUpdate(queryString);

                queryString = "UPDATE planilla_detalle";
                queryString += " SET CodigoPartida = '" + codigoPartida + "'";
                queryString += ",    Cheque = '" + String.valueOf(correlativoCheque) + "'";
                queryString += " WHERE IdPlanilla = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();
                queryString += " AND   IdEmpleado = " + item.getItemProperty(IDEMPLEADO).getValue();

                stQuery1.executeUpdate(queryString);

                ultimoEncontado = codigoPartida.substring(12, 15);

                codigoPartida = codigoPartida.substring(0,12) + String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

                totalPagado+= (double)item.getItemProperty(LIQUIDO).getValue();

                correlativoCheque++;
            } //endfor

            queryString = "UPDATE planilla_encabezado";
            queryString += " SET Estatus = 'GENERADA'";
            queryString += ", TotalEmpleados = " + planillaDetalleContainer.size();
            queryString += ", TotalPagado= " + totalPagado;
//            queryString += ", TotalProvisionado= " + totalProvisioado;  es un anticipo
            queryString += ", AutorizadoFechaYhora = current_timestamp";
            queryString += ", AutorizadoUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND Id = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();

            stQuery.executeUpdate(queryString);

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ESTATUS_PLANILLA).setValue("GENERADA");

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

    private void generarPlanillaMensual() {
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
            double totalProvisionado = 0.00;

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
//                01234567890123
//                22202311308000
                    ultimoEncontado = rsRecords.getString("CodigoPartida").substring(12, 15);


                    codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

                } else {
                    codigoPartida += "001";
                }
//                22202311301000
                System.out.println("FECHA PARTIDA PLANILLA = " + fecha + " CodigoPartida = " + codigoPartida );

                Item item = planillaDetalleContainer.getItem(itemObject);
//2026-03-04                codigoCC = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "10000000" + item.getItemProperty(IDEMPLEADO).getValue().toString().substring(2);
                //2210000000343
                codigoCC = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "100000" + item.getItemProperty(IDEMPLEADO).getValue().toString().substring(0);
//************************************************ DEBE *************************************//

                //Si no se paga nada, saltar Empleado
                if((double)item.getItemProperty(LIQUIDO).getValue() <= 0) continue;

                //ORDINARIO
                queryString = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
                queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor, NombreCheque,";
                queryString += " MontoDocumento, SerieDocumento, NumeroDocumento, TipoDOCA, NoDOCA, IdNomenclatura, MonedaDocumento, Debe, Haber,";
                queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,";
                queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora, IdCentroCosto, CodigoCentroCosto, SueldoOrdinario)";
                queryString += " VALUES ";
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
                queryString += "," + ((Double.valueOf((double)item.getItemProperty(SALARIO_DEVENGADO).getValue())) - (Double.valueOf((double)item.getItemProperty(DESCUENTO2).getValue())));
                queryString += ",'0.00'";
                queryString += "," + ((Double.valueOf((double)item.getItemProperty(SALARIO_DEVENGADO).getValue())) - (Double.valueOf((double)item.getItemProperty(DESCUENTO2).getValue())));
                queryString += ",0.00"; //DEBE Q.
                queryString += ",1.0";
                queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",0";
                queryString += ",'0'";
                queryString += ", " + item.getItemProperty(SALARIO_BASE).getValue() + ")";

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
                if(((double)item.getItemProperty(BONO2).getValue()
                        + (double)item.getItemProperty(BONO3).getValue()
                        + (double)item.getItemProperty(BONO4).getValue()
                        + (double)item.getItemProperty(BONO5).getValue()
                ) > 0
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
                    queryString +=  "," + ((SopdiUI) mainUI).cuentasContablesDefault.getBonificacionDCTO78_89(); // BONO DCTO 78-89
                    queryString += ",'QUETZALES'";
                    queryString += "," + ((double)item.getItemProperty(BONO2).getValue()
                            + (double)item.getItemProperty(BONO3).getValue()
                            + (double)item.getItemProperty(BONO4).getValue()
                            + (double)item.getItemProperty(BONO5).getValue()
                    );
                    queryString += ",0.00"; //HABER
                    queryString += "," + ((double)item.getItemProperty(BONO2).getValue()
                            + (double)item.getItemProperty(BONO3).getValue()
                            + (double)item.getItemProperty(BONO4).getValue()
                            + (double)item.getItemProperty(BONO5).getValue()
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

                //CUOTA PATRONAL IGSS gasto
                if((double)item.getItemProperty(PROVISION1).getValue() > 0 ) {
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
                    queryString += "," + ((SopdiUI)mainUI).cuentasContablesDefault.getCuotaPatronalIgss(); // GASTO --> CUOTA PATRONAL IGSS
                    queryString += ",'QUETZALES'";
                    queryString += "," + (double)item.getItemProperty(PROVISION1).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(PROVISION1).getValue();
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
                if((double)item.getItemProperty(PROVISION2).getValue() > 0 ) {
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
                    queryString += "," + (double)item.getItemProperty(PROVISION2).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(PROVISION2).getValue();
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
                if((double)item.getItemProperty(PROVISION3).getValue() > 0 ) {
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
                    queryString += "," + (double)item.getItemProperty(PROVISION3).getValue();
                    queryString += ",0.00"; //HABER
                    queryString += "," + (double)item.getItemProperty(PROVISION3).getValue();
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
                    queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaLaboralIgssPorPagar(); // CUOTA LABORAL IGSS POR PAGAR
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
                if((double)item.getItemProperty(PROVISION1).getValue() > 0 ) {
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
                    queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaPatronalIgssPorPagar(); // CUOTA PATRONAL IGSS POR PAGAR
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + (double)item.getItemProperty(PROVISION1).getValue();
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + (double)item.getItemProperty(PROVISION1).getValue();
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
                    queryString += "," + ((SopdiUI)mainUI).cuentasContablesDefault.getSueldosPorPagar(); //SUELDOS POR PAGAR
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
                    queryString += "," + ((SopdiUI)mainUI).cuentasContablesDefault.getIsrRetenidoPorPagar(); //RETENCIONES ISR POR PAGAR
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
                }

                //ANTICIPO SUELDO
                if((double)item.getItemProperty(DESCUENTO4).getValue() > 0) {
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
                    queryString += ","  + ((SopdiUI)mainUI).cuentasContablesDefault.getAnticiposSueldos(); //ANTICIPOS DE SUELDOS
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + item.getItemProperty(DESCUENTO4).getValue(); //HABER
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + item.getItemProperty(DESCUENTO4).getValue(); //HABER Q
                    queryString += ",1.0";
                    queryString += "," + item.getItemProperty(LIQUIDO).getValue();
                    queryString += ",'PLANILLA SUELDO POR PAGAR " + String.valueOf(item.getItemProperty(EMPLEADO).getValue()) + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ",0";
                    queryString += ",'0'";
                    queryString += ", " + null + ")";
                }
                //PROVISION AGUINALDO
                if((double)item.getItemProperty(PROVISION2).getValue() > 0) {
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
                    queryString += ","  + ((SopdiUI)mainUI).cuentasContablesDefault.getProvisionAguinaldo(); //PROVISION AGUINALDO
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + item.getItemProperty(PROVISION2).getValue();
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + item.getItemProperty(PROVISION2).getValue();
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
                if((double)item.getItemProperty(PROVISION3).getValue() > 0) {
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
                    queryString += "," + ((SopdiUI)mainUI).cuentasContablesDefault.getProvisionBono14(); //PROVISION BONO 14
                    queryString += ",'QUETZALES'";
                    queryString += ",0.00"; //DEBE
                    queryString += "," + item.getItemProperty(PROVISION3).getValue();
                    queryString += ",0.00"; //DEBE Q.
                    queryString += "," + item.getItemProperty(PROVISION3).getValue();
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

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY INSERT PLANILLA SALARIO : " + queryString);

                totalProvisionado+= (double)item.getItemProperty(PROVISIONADO).getValue();

                correlativoCheque++;

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

    private void generarPlanillaSoloProvision() {
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
            double totalProvisionado = 0.00;

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
//                01234567890123
//                22202311308000
                        ultimoEncontado = rsRecords.getString("CodigoPartida").substring(12, 15);

                    codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

                } else {
                    codigoPartida += "001";
                }
//                22202311301000
                System.out.println("FECHA PARTIDA PLANILLA = " + fecha + " CodigoPartida = " + codigoPartida );

                Item item = planillaDetalleContainer.getItem(itemObject);
                //26-03-04 codigoCC = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "10000000" + item.getItemProperty(IDEMPLEADO).getValue().toString().substring(2);
                //2210000000343
                codigoCC = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "100000" + item.getItemProperty(IDEMPLEADO).getValue().toString().substring(0);
                queryString = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
                queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor, NombreCheque,";
                queryString += " MontoDocumento, SerieDocumento, NumeroDocumento, TipoDOCA, NoDOCA, IdNomenclatura, MonedaDocumento, Debe, Haber,";
                queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,";
                queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora, IdCentroCosto, CodigoCentroCosto, SueldoOrdinario)";
                queryString += " VALUES ";
/************************************************ DEBE *************************************/
                //AGUINALDO O BONO14
                if((double)item.getItemProperty(BONO4).getValue() > 0 ) {
                    queryString += "(";
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
                    if(aguinaldoChb.getValue()) {
                        queryString += "," + ((SopdiUI)mainUI).cuentasContablesDefault.getProvisionAguinaldo(); // AGUINALDO
                    }
                    else {
                        queryString += "," + ((SopdiUI)mainUI).cuentasContablesDefault.getProvisionBono14(); // BONO 14
                    }
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

/************************************************ HABER *************************************/

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
                    queryString += "," + ((SopdiUI)mainUI).cuentasContablesDefault.getSueldosPorPagar(); //SUELDOS POR PAGAR
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

                if((double)item.getItemProperty(LIQUIDO).getValue() > 0 && !((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {

/************************************************ CHEQUE *************************************/
                    if ((double) item.getItemProperty(LIQUIDO).getValue() > 0 && !((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
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

                        totalPagado += (double) item.getItemProperty(LIQUIDO).getValue();
                    }//SI HAY MONTO LIQUIDO > 0
                }
                System.out.println("queryProvision="+queryString);

                if((double)item.getItemProperty(LIQUIDO).getValue() > 0) {
                    stQuery.executeUpdate(queryString);

                    queryString = "UPDATE planilla_detalle";
                    queryString += " SET CodigoPartida = '" + codigoPartida + "'";
                    queryString += ",    Cheque = '" + String.valueOf(correlativoCheque) + "'";
                    queryString += " WHERE IdPlanilla = " + planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PLANILLA).getValue();
                    queryString += " AND   IdEmpleado = " + item.getItemProperty(IDEMPLEADO).getValue();

                    stQuery.executeUpdate(queryString);

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY INSERT PLANILLA SALARIO : " + queryString);

                    totalProvisionado += (double) item.getItemProperty(PROVISIONADO).getValue();

                    correlativoCheque++;
                }
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

    private void cargarArchivo(File planillaFile) {

        singleUpload.setEnabled(false);
        try {

            fileInputStream = new FileInputStream(planillaFile);

            workbook = new HSSFWorkbook(fileInputStream);

            sheet = workbook.getSheetAt(0);

            System.out.println("\n Total lineas en archivo=" + sheet.getLastRowNum());
            System.out.println("...INICIO...");

            Object itemId; int recordCount = 0;;

            for (int linea = 1; linea <= sheet.getLastRowNum(); linea++) {

//                try {
//                    sheet.getRow(linea).getCell(0).getNumericCellValue();
//                }
//                catch(Exception exNull) {
//                    System.out.println("OUT...");
//                    break;
//                }

                double  bono =  sheet.getRow(linea).getCell(12).getNumericCellValue();

//System.out.println("bono=" + bono);

                Item itemOject = planillaDetalleContainer.getItem(linea);

                itemOject.getItemProperty(BONO3).setValue(bono);

                recordCount++;

            } //endfor

            System.out.println("...FIN...");

            Notification.show("Operación exitosa! " + recordCount + " registros cargados.", Notification.Type.HUMANIZED_MESSAGE);

        } catch (Exception ex1) {
            new Notification("Error al intentar cargar archivo EXCEL.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                //
            }
        }
        singleUpload.setEnabled(true);
    }

    private double redondear(double value) {
        return Math.floor(value * 100) / 100;
        //Math.floor(60984.1)=60984.0
        //Math.floor(-497.99)=-498.0
        //Math.floor(0)=0.0
//       return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
//       return new BigDecimal(value).setScale(2, RoundingMode.HALF_DOWN);
//       return new BigDecimal(value).setScale(2, RoundingMode.HALF_INCLUSIVE);
//       return new BigDecimal(value).setScale(2, RoundingMode.DOWN);
//       return new BigDecimal(value).setScale(2, RoundingMode.UP);
        //Summary of Rounding Operations Under Different Rounding Modes
        //Result of rounding input to one digit with the given rounding mode
   //Number	   UP	DOWN	CEILING	FLOOR	HALF_UP	HALF_DOWN	HALF_EVEN	UNNECESSARY
        //5.5	6	5	6	5	6	5	6	throw ArithmeticException
        //2.5	3	2	3	2	3	2	2	throw ArithmeticException
        //1.6	2	1	2	1	2	2	2	throw ArithmeticException
        //1.1	2	1	2	1	1	1	1	throw ArithmeticException
        //1.0	1	1	1	1	1	1	1	1
        //-1.0	-1	-1	-1	-1	-1	-1	-1	-1
        //-1.1	-2	-1	-1	-2	-1	-1	-1	throw ArithmeticException
        //-1.6	-2	-1	-1	-2	-2	-2	-2	throw ArithmeticException
        //-2.5	-3	-2	-2	-3	-3	-2	-2	throw ArithmeticException
        //-5.5	-6	-5	-5	-6	-6	-5	-6	throw ArithmeticException
//        return Double.valueOf(df.format(value)); //DecimalFormat df = new DecimalFormat("######0.00");
    }

    private int getDiasTrabajados(){
        long diffInMillies = 0;
        int diasTrabajados = 0; // 0: Dias Totales (Inicio A Fin) | 1: Dias Sin paga (Mes)
        int diasLicenciaSinPago = 0;
        Date fechaEgreso;
        Date fechaIngreso;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

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

        java.util.GregorianCalendar dateOfJournal = new java.util.GregorianCalendar();
        dateOfJournal.setTime(fechaIngreso);

        // ----------------------------- Dias Totales -------------------------------
        if(fechaIngreso.getMonth() == Utileria.getUltimoDiaDelMes().getMonth() && fechaIngreso.getYear() == Utileria.getUltimoDiaDelMes().getYear()){
            diffInMillies = Math.abs(fechaIngreso.getTime() - Utileria.getUltimoDiaDelMes().getTime());
            diasTrabajados = (int)TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
        }else{
            diasTrabajados = factorDiasMes;
        }
        if(dateOfJournal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) == diasTrabajados) {
            diasTrabajados = factorDiasMes;
        }
        if(dateOfJournal.get(GregorianCalendar.MONTH) == 1 && diasTrabajados < factorDiasMes) {
            diasTrabajados += factorDiasMes - dateOfJournal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        }


        if(diasTrabajados > factorDiasMes) diasTrabajados = factorDiasMes;


        return diasTrabajados;
    }

    private double getProvision(String idProveedor, TIPO_Provision tipoProvision, double salarioBase) {
        int diasAusenciaSinPago = 0;
        double promedio = 0.00;
        double montoProvision = 0.00; // El salario del mes actual
        String fechaInicioSt, fechaFinalSt;
        java.util.Date fechaInicioProv = new java.util.Date();

        if(tipoProvision.equals(TIPO_Provision.AGUINALDO)){
            fechaInicioProv = new java.util.Date((dateAfter.get(1) - 1901), 11, 1);
        }
        if(tipoProvision.equals(TIPO_Provision.BONO14)){
            fechaInicioProv = new java.util.Date((dateAfter.get(1) - 1901), 6, 1);
        }

        fechaInicioSt = Utileria.getFechaYYYYMMDD_1(fechaInicioProv);
        fechaFinalSt = Utileria.getFechaYYYYMMDD_1(dateAfter.getTime()); //fecha fin planilla , para Provisiones deveria ser el 15 Julio  o el 15 Diciembre

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            //0 leer datos de planilla
            queryString = "SELECT *";
            queryString += " FROM proveedor_empresa";
            queryString += " WHERE IdProveedor = " + idProveedor;

            rsRecords2 = stQuery.executeQuery(queryString);

            if(!rsRecords2.next()) {
                Notification.show("ERROR,  no se encontró Empleado!", Notification.Type.ERROR_MESSAGE);
                return montoProvision;
            }

            Date fechaIngreso = rsRecords2.getDate("FechaIngreso");

            // El caso en el que entre y salga en el mismo mes
            if (fechaIngreso != null && dateAfter != null ) {
                if (dateAfter.get(java.util.Calendar.MONTH) == fechaIngreso.getMonth()
                    && dateAfter.get(java.util.Calendar.YEAR) == fechaIngreso.getYear()) {

                    long diffInMillies = Math.abs(dateAfter.getTimeInMillis() - fechaIngreso.getTime());
                    long dias = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
                    // 12 mese 30 dias por mes
                    montoProvision = Utileria.round((salarioBase / (30)) * dias);

                    return montoProvision;
                }
            }

            queryString = "SELECT Fecha, SueldoOrdinario, IdNomenclatura ";
            queryString += "FROM contabilidad_partida ";
            queryString += "WHERE IdProveedor = " + idProveedor + " ";
            queryString += "AND Fecha Between '" + fechaInicioSt + "' AND '" + Utileria.getFechaYYYYMMDD_1(dateAfter.getTime()) + "' ";
            queryString += "AND Upper(Estatus) <> 'ANULADO' ";
            queryString += "AND TipoDocumento = 'Planilla' ";
            queryString += "AND IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
            queryString += "AND IdNomenclatura = " + ((SopdiUI)mainUI).cuentasContablesDefault.getSueldoOrdinario() + " ";
            queryString += "ORDER BY Fecha";


            rsRecords1 = stQuery1.executeQuery(queryString);
            Date fecha = null;
            Double monto = 0d;
            int mesesLaborados = 0;
            long diasLaborados = (long)factorDiasAnio;

            if(rsRecords1.next()){
                do{
                    mesesLaborados++;
                    if(rsRecords1.getDate("Fecha").getMonth() == fechaIngreso.getMonth() && rsRecords1.getDate("Fecha").getYear() == fechaIngreso.getYear()){
                        long diffInMillies = Math.abs(fechaInicioProv.getTime() - fechaIngreso.getTime()) ;
                        diasLaborados = (long)(factorDiasAnio - TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS));
                    }
                    monto += rsRecords1.getDouble("SueldoOrdinario");
                }while (rsRecords1.next());
                double promedioSalario = Utileria.round((monto/mesesLaborados));
                montoProvision = Utileria.round((promedioSalario / (factorDiasAnio)) * diasLaborados);
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla CALCULO DE PLANILLA EMPLEADOS Provision : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            return 0.00;
        }

        return montoProvision;
    }

    @Getter
    @Setter
    private class Empleado {
        private String afilicionIgss = "NO EXISTE REGISTRO";
        private String primerNombre = "NO EXISTE REGISTRO";
        private String segundoNombre = "";
        private String primerApellido = "";
        private String segundoApellido = "";
        private String casadaApellido = "";
        private String fechaIngreso = "";
        private String codigoCentroTrabajo = "";
        private String nit = "";
        private String codigoOcupacion = "";
        private String condicionLaboral = "";
        private String tipoSalario = "";
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - CALCULO DE PLANILLA");
    }
}
