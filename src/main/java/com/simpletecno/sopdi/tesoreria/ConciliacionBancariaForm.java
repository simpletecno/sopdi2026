package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.sun.istack.logging.Logger;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

public class ConciliacionBancariaForm extends Window {

    VerticalLayout mainLayout;

    Grid egresosGrid;
    public IndexedContainer egresosContainer = new IndexedContainer();
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo partida";
    static final String FECHA_PROPERTY = "Fecha";
    static final String FECHA2_PROPERTY = "Fecha";
    static final String TIPO_DOCUMENTO_PROPERTY = "Tipo Documento";
    static final String DOCTO_PROPERTY = "# Doc";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String NOMBRE_CHEQUE_PROPERTY = "Nombre";
    static final String EGRESO_PROPERTY = "Egreso";
    Grid.FooterRow egresoFooter;

    Grid ingresosGrid;
    public IndexedContainer ingresosContainer = new IndexedContainer();
    static final String INGRESO_PROPERTY = "Ingreso";
    Grid.FooterRow ingresoFooter;

    UI mainUI;
    PreparedStatement stPreparedQuery = null;
    Statement stQuery, stQuery2;
    ResultSet rsRecords, rsRecords2;
    String queryString;

    ComboBox cuentaContableCbx;
    PopupDateField mesDt;
    TextField conciliacionTxt;
    NumberField saldoMesAnteriorTxt;
    NumberField saldoFinalBancoTxt;

    NumberField saldoInicialContableTxt, ingresosContablesTxt, egresosContablesTxt, saldoFinalContableTxt;
    NumberField saldoFinalCuentaBancoTxt, egresosTransitoTxt, saldoFinalNetoTxt, validadorTxt;

    Button guardarBtn;
    Button finalizarBtn;

    String idConciliacion = "";

    String idRegistroCuentaBancos = "";

    static DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    Utileria utilidadesFecha = new Utileria();

    BigDecimal ingresosMes = new BigDecimal(0.00).setScale(2, RoundingMode.FLOOR);
    BigDecimal egresosMes = new BigDecimal(0.00).setScale(2, RoundingMode.FLOOR);

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public ConciliacionBancariaForm(String idConciliacion) {
        this.idConciliacion = idConciliacion;
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setWidth("98%");
        setHeight("100%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(new MarginInfo(false, true, false, true));

        setContent(mainLayout);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " LIBRO CONCILIACIÓN BANCARIA DE " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName().toUpperCase());
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearFormularioConciliacion();

        if (!idConciliacion.trim().isEmpty()) { ///EDITAR
            llenarDatosConciliacion();
            llenarGridsConciliacion();
        }

    }

    public void crearFormularioConciliacion() {

        VerticalLayout repotLayout = new VerticalLayout();
        repotLayout.setWidth("100%");
        repotLayout.addStyleName("rcorners3");
        repotLayout.setMargin(new MarginInfo(false, true, false, true));

        HorizontalLayout contentFormularios = new HorizontalLayout();
        contentFormularios.setWidth("100%");
        contentFormularios.setSpacing(true);

        FormLayout formularioLeft = new FormLayout();
        formularioLeft.setWidth("50%");

        FormLayout formularioRight = new FormLayout();
        formularioRight.setWidth("50%");

        cuentaContableCbx = new ComboBox("SELECCIONE LA CUENTA CONTABLE : ");
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.setWidth("25em");
        cuentaContableCbx.setResponsive(true);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();

        mesDt = new PopupDateField("SELECCIONE EL MES : ");
        mesDt.setWidth("22em");
        mesDt.setValue(new java.util.Date());
        mesDt.setResolution(Resolution.MONTH);
        mesDt.setDateFormat("MM/yyyy");

        Button buscarBtn = new Button("Buscar");
        buscarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        buscarBtn.setIcon(FontAwesome.SEARCH);
        buscarBtn.addClickListener((event) -> {
            if (cuentaContableCbx.getValue() != null && mesDt.getValue() != null) {
                if (!saldoFinalBancoTxt.getValue().trim().isEmpty() || saldoFinalBancoTxt.getDoubleValueDoNotThrow() == 0.00) {
                    buscarIdCuentasBancos();
                    buscarSaldoInicialContable();
                    llenarGridsConciliacion();
                } else {
                    Notification notif = new Notification("Por favor ingrese el saldo final del banco.", Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });
        if (!idConciliacion.trim().isEmpty()) {
            buscarBtn.setEnabled(false);
        } else {
            buscarBtn.setEnabled(true);
        }

        conciliacionTxt = new TextField("CORRELATIVO DE CONCILIACIÓN :");
        conciliacionTxt.setWidth("22em");
        conciliacionTxt.setReadOnly(true);
        conciliacionTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        saldoMesAnteriorTxt = new NumberField("SALDO AL MES ANTERIOR : ");
        saldoMesAnteriorTxt.setDecimalAllowed(true);
        saldoMesAnteriorTxt.setDecimalPrecision(2);
        saldoMesAnteriorTxt.setMinimumFractionDigits(2);
        saldoMesAnteriorTxt.setDecimalSeparator('.');
        saldoMesAnteriorTxt.setDecimalSeparatorAlwaysShown(true);
        saldoMesAnteriorTxt.setValue(0d);
        saldoMesAnteriorTxt.setGroupingUsed(true);
        saldoMesAnteriorTxt.setGroupingSeparator(',');
        saldoMesAnteriorTxt.setGroupingSize(3);
        saldoMesAnteriorTxt.setImmediate(true);
        saldoMesAnteriorTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        saldoMesAnteriorTxt.setWidth("22em");
        saldoMesAnteriorTxt.setValue(0.00);
        saldoMesAnteriorTxt.setReadOnly(true);

        saldoFinalBancoTxt = new NumberField("INGRESE EL SALDO FINAL DEL BANCO : ");
        saldoFinalBancoTxt.setDecimalAllowed(true);
        saldoFinalBancoTxt.setDecimalPrecision(2);
        saldoFinalBancoTxt.setMinimumFractionDigits(2);
        saldoFinalBancoTxt.setDecimalSeparator('.');
        saldoFinalBancoTxt.setDecimalSeparatorAlwaysShown(true);
        saldoFinalBancoTxt.setValue(0d);
        saldoFinalBancoTxt.setGroupingUsed(true);
        saldoFinalBancoTxt.setGroupingSeparator(',');
        saldoFinalBancoTxt.setGroupingSize(3);
        saldoFinalBancoTxt.setImmediate(true);
        saldoFinalBancoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        saldoFinalBancoTxt.setWidth("22em");
        saldoFinalBancoTxt.setValue(0.00);
        saldoFinalBancoTxt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                saldoFinalCuentaBancoTxt.setReadOnly(false);
                saldoFinalCuentaBancoTxt.setValue(saldoFinalBancoTxt.getValue());
                saldoFinalCuentaBancoTxt.setReadOnly(true);
            }
        });

        formularioLeft.addComponents(cuentaContableCbx, conciliacionTxt, saldoFinalBancoTxt);
        formularioRight.addComponents(mesDt, saldoMesAnteriorTxt, buscarBtn);

        contentFormularios.addComponents(formularioLeft, formularioRight);

        repotLayout.addComponent(contentFormularios);
        repotLayout.setComponentAlignment(contentFormularios, Alignment.TOP_CENTER);

        egresosContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        egresosContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        egresosContainer.addContainerProperty(FECHA2_PROPERTY, String.class, null);
        egresosContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        egresosContainer.addContainerProperty(DOCTO_PROPERTY, String.class, null);
        egresosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        egresosContainer.addContainerProperty(NOMBRE_CHEQUE_PROPERTY, String.class, null);
        egresosContainer.addContainerProperty(EGRESO_PROPERTY, String.class, null);

        egresosGrid = new Grid("EGRESOS", egresosContainer);
        egresosGrid.setWidth("100%");
        egresosGrid.setResponsive(true);
        egresosGrid.setImmediate(true);
        egresosGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        egresosGrid.setDescription("Seleccione uno o varios registros.");
        egresosGrid.setHeightMode(HeightMode.ROW);
        egresosGrid.setHeightByRows(5);
        egresosGrid.setResponsive(true);
        egresosGrid.setEditorBuffered(false);

        egresosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (EGRESO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        egresosGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true);
        egresosGrid.getColumn(FECHA2_PROPERTY).setHidable(false).setHidden(true);
        egresosGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);;

        egresoFooter = egresosGrid.appendFooterRow();
        egresoFooter.getCell(NOMBRE_CHEQUE_PROPERTY).setText("Totales");
        egresoFooter.getCell(EGRESO_PROPERTY).setText("0.00");

        egresoFooter.getCell(NOMBRE_CHEQUE_PROPERTY).setStyleName("rightalign");
        egresoFooter.getCell(EGRESO_PROPERTY).setStyleName("rightalign");

        ingresosContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        ingresosContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        ingresosContainer.addContainerProperty(FECHA2_PROPERTY, String.class, null);
        ingresosContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        ingresosContainer.addContainerProperty(DOCTO_PROPERTY, String.class, null);
        ingresosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        ingresosContainer.addContainerProperty(NOMBRE_CHEQUE_PROPERTY, String.class, null);
        ingresosContainer.addContainerProperty(INGRESO_PROPERTY, String.class, null);

        ingresosGrid = new Grid("INGRESOS", ingresosContainer);
        ingresosGrid.setWidth("100%");
        ingresosGrid.setResponsive(true);
        ingresosGrid.setImmediate(true);
        ingresosGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        ingresosGrid.setDescription("Seleccione uno o varios registros.");
        ingresosGrid.setHeightMode(HeightMode.ROW);
        ingresosGrid.setHeightByRows(5);
        ingresosGrid.setResponsive(true);
        ingresosGrid.setEditorBuffered(false);

        ingresosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (INGRESO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        ingresosGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        ingresosGrid.getColumn(FECHA2_PROPERTY).setHidable(false).setHidden(true);
        ingresosGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(false).setHidden(true);

        ingresoFooter = ingresosGrid.appendFooterRow();
        ingresoFooter.getCell(NOMBRE_CHEQUE_PROPERTY).setText("Totales");
        ingresoFooter.getCell(INGRESO_PROPERTY).setText("0.00");

        ingresoFooter.getCell(NOMBRE_CHEQUE_PROPERTY).setStyleName("rightalign");
        ingresoFooter.getCell(INGRESO_PROPERTY).setStyleName("rightalign");

        HorizontalLayout gridLayout = new HorizontalLayout();
        gridLayout.setWidth("100%");
        gridLayout.setSpacing(true);

        gridLayout.addComponents(egresosGrid, ingresosGrid);
        gridLayout.setComponentAlignment(egresosGrid, Alignment.TOP_CENTER);
        gridLayout.setComponentAlignment(ingresosGrid, Alignment.TOP_CENTER);

        repotLayout.addComponent(gridLayout);
        repotLayout.setComponentAlignment(gridLayout, Alignment.TOP_CENTER);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        buttonLayout.setMargin(new MarginInfo(false, true, false, true));

        FormLayout formulario2Left = new FormLayout();
        formularioLeft.setSpacing(true);

        FormLayout formulario2Right = new FormLayout();
        formulario2Right.setSpacing(true);

        saldoInicialContableTxt = new NumberField("Saldo Inicial Contable: ");
        saldoInicialContableTxt.setDecimalAllowed(true);
        saldoInicialContableTxt.setDecimalPrecision(2);
        saldoInicialContableTxt.setMinimumFractionDigits(2);
        saldoInicialContableTxt.setDecimalSeparator('.');
        saldoInicialContableTxt.setDecimalSeparatorAlwaysShown(true);
        saldoInicialContableTxt.setValue(0d);
        saldoInicialContableTxt.setGroupingUsed(true);
        saldoInicialContableTxt.setGroupingSeparator(',');
        saldoInicialContableTxt.setGroupingSize(3);
        saldoInicialContableTxt.setImmediate(true);
        saldoInicialContableTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        saldoInicialContableTxt.setWidth("8em");
        saldoInicialContableTxt.setValue(0.00);

        ingresosContablesTxt = new NumberField("Ingresos contables: ");
        ingresosContablesTxt.setDecimalAllowed(true);
        ingresosContablesTxt.setDecimalPrecision(2);
        ingresosContablesTxt.setMinimumFractionDigits(2);
        ingresosContablesTxt.setDecimalSeparator('.');
        ingresosContablesTxt.setDecimalSeparatorAlwaysShown(true);
        ingresosContablesTxt.setValue(0d);
        ingresosContablesTxt.setGroupingUsed(true);
        ingresosContablesTxt.setGroupingSeparator(',');
        ingresosContablesTxt.setGroupingSize(3);
        ingresosContablesTxt.setImmediate(true);
        ingresosContablesTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        ingresosContablesTxt.setWidth("8em");
        ingresosContablesTxt.setValue(0.00);

        egresosContablesTxt = new NumberField("Egresos Contables: ");
        egresosContablesTxt.setDecimalAllowed(true);
        egresosContablesTxt.setDecimalPrecision(2);
        egresosContablesTxt.setMinimumFractionDigits(2);
        egresosContablesTxt.setDecimalSeparator('.');
        egresosContablesTxt.setDecimalSeparatorAlwaysShown(true);
        egresosContablesTxt.setValue(0d);
        egresosContablesTxt.setGroupingUsed(true);
        egresosContablesTxt.setGroupingSeparator(',');
        egresosContablesTxt.setGroupingSize(3);
        egresosContablesTxt.setImmediate(true);
        egresosContablesTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        egresosContablesTxt.setWidth("8em");
        egresosContablesTxt.setValue(0.00);

        saldoFinalContableTxt = new NumberField("Saldo Final Contable: ");
        saldoFinalContableTxt.setDecimalAllowed(true);
        saldoFinalContableTxt.setDecimalPrecision(2);
        saldoFinalContableTxt.setMinimumFractionDigits(2);
        saldoFinalContableTxt.setDecimalSeparator('.');
        saldoFinalContableTxt.setDecimalSeparatorAlwaysShown(true);
        saldoFinalContableTxt.setValue(0d);
        saldoFinalContableTxt.setGroupingUsed(true);
        saldoFinalContableTxt.setGroupingSeparator(',');
        saldoFinalContableTxt.setGroupingSize(3);
        saldoFinalContableTxt.setImmediate(true);
        saldoFinalContableTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        saldoFinalContableTxt.setWidth("8em");
        saldoFinalContableTxt.setValue(0.00);

        saldoFinalCuentaBancoTxt = new NumberField("Saldo Final Banco: ");
        saldoFinalCuentaBancoTxt.setDecimalAllowed(true);
        saldoFinalCuentaBancoTxt.setDecimalPrecision(2);
        saldoFinalCuentaBancoTxt.setMinimumFractionDigits(2);
        saldoFinalCuentaBancoTxt.setDecimalSeparator('.');
        saldoFinalCuentaBancoTxt.setDecimalSeparatorAlwaysShown(true);
        saldoFinalCuentaBancoTxt.setValue(0d);
        saldoFinalCuentaBancoTxt.setGroupingUsed(true);
        saldoFinalCuentaBancoTxt.setGroupingSeparator(',');
        saldoFinalCuentaBancoTxt.setGroupingSize(3);
        saldoFinalCuentaBancoTxt.setImmediate(true);
        saldoFinalCuentaBancoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        saldoFinalCuentaBancoTxt.setWidth("8em");
        saldoFinalCuentaBancoTxt.setValue(0.00);

        egresosTransitoTxt = new NumberField("Egresos en transito: ");
        egresosTransitoTxt.setDecimalAllowed(true);
        egresosTransitoTxt.setDecimalPrecision(2);
        egresosTransitoTxt.setMinimumFractionDigits(2);
        egresosTransitoTxt.setDecimalSeparator('.');
        egresosTransitoTxt.setDecimalSeparatorAlwaysShown(true);
        egresosTransitoTxt.setValue(0d);
        egresosTransitoTxt.setGroupingUsed(true);
        egresosTransitoTxt.setGroupingSeparator(',');
        egresosTransitoTxt.setGroupingSize(3);
        egresosTransitoTxt.setImmediate(true);
        egresosTransitoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        egresosTransitoTxt.setWidth("8em");
        egresosTransitoTxt.setValue(0.00);

        saldoFinalNetoTxt = new NumberField("Saldo Final Neto: ");
        saldoFinalNetoTxt.setDecimalAllowed(true);
        saldoFinalNetoTxt.setDecimalPrecision(2);
        saldoFinalNetoTxt.setMinimumFractionDigits(2);
        saldoFinalNetoTxt.setDecimalSeparator('.');
        saldoFinalNetoTxt.setDecimalSeparatorAlwaysShown(true);
        saldoFinalNetoTxt.setValue(0d);
        saldoFinalNetoTxt.setGroupingUsed(true);
        saldoFinalNetoTxt.setGroupingSeparator(',');
        saldoFinalNetoTxt.setGroupingSize(3);
        saldoFinalNetoTxt.setImmediate(true);
        saldoFinalNetoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        saldoFinalNetoTxt.setWidth("8em");
        saldoFinalNetoTxt.setValue(0.00);

        validadorTxt = new NumberField("Validador: ");
        validadorTxt.setDecimalAllowed(true);
        validadorTxt.setDecimalPrecision(2);
        validadorTxt.setMinimumFractionDigits(2);
        validadorTxt.setDecimalSeparator('.');
        validadorTxt.setDecimalSeparatorAlwaysShown(true);
        validadorTxt.setValue(0d);
        validadorTxt.setGroupingUsed(true);
        validadorTxt.setGroupingSeparator(',');
        validadorTxt.setGroupingSize(3);
        validadorTxt.setImmediate(true);
        validadorTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        validadorTxt.setWidth("8em");
        validadorTxt.setValue(0.00);

        guardarBtn = new Button("Guardar");
        guardarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.setWidth("8em");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener((event) -> {
            if (egresosContainer.size() > 0 || ingresosContainer.size() > 0) {
                guardarConciliacion();
                finalizarBtn.setVisible(true);
            }
        });

        finalizarBtn = new Button("Finalizar");
        finalizarBtn.setWidth("8em");
        finalizarBtn.setIcon(FontAwesome.CHECK);
        finalizarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        if (idConciliacion.trim().isEmpty()) {
            finalizarBtn.setVisible(false);
        }
        finalizarBtn.addClickListener((event) -> {
            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de Finalizar la conciliación?",
                    "SI", "NO", new ConfirmDialog.Listener() {

                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {                        
                        finalizarConciliacion();
                    }
                }
            }
            );
        });

        formulario2Left.addComponents(saldoInicialContableTxt, ingresosContablesTxt, egresosContablesTxt, saldoFinalContableTxt, guardarBtn);
        formulario2Right.addComponents(saldoFinalCuentaBancoTxt, egresosTransitoTxt, saldoFinalNetoTxt, validadorTxt, finalizarBtn);

        buttonLayout.addComponents(formulario2Left, formulario2Right);
        buttonLayout.setComponentAlignment(formulario2Left, Alignment.TOP_CENTER);
        buttonLayout.setComponentAlignment(formulario2Right, Alignment.TOP_CENTER);

        repotLayout.addComponent(buttonLayout);
        repotLayout.setComponentAlignment(buttonLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(repotLayout);
        mainLayout.setComponentAlignment(repotLayout, Alignment.TOP_CENTER);
    }

    public void llenarDatosConciliacion() {

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_conciliacion_bancaria ";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND IdConciliacionBancaria = '" + idConciliacion + "'";

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado

                cuentaContableCbx.setReadOnly(false);
                cuentaContableCbx.select(rsRecords2.getString("IdNomenclatura"));
                cuentaContableCbx.setReadOnly(true);

                conciliacionTxt.setReadOnly(false);
                conciliacionTxt.setValue(rsRecords2.getString("Correlativo"));
                conciliacionTxt.setReadOnly(true);

                String fecha = Utileria.getFormatoDDMMYYYY(rsRecords2.getString("AnioMes") + "01");
                Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);

                mesDt.setReadOnly(false);
                mesDt.setValue(date1);
                mesDt.setReadOnly(true);

                saldoMesAnteriorTxt.setReadOnly(false);
                saldoMesAnteriorTxt.setValue(rsRecords2.getDouble("SaldoInicialContable"));
                saldoMesAnteriorTxt.setReadOnly(true);
                
                saldoFinalBancoTxt.setValue(rsRecords2.getDouble("SaldoFinalBanco"));

                saldoInicialContableTxt.setReadOnly(false);
                saldoInicialContableTxt.setValue(rsRecords2.getDouble("SaldoInicialContable"));
                saldoInicialContableTxt.setReadOnly(true);
                
                ingresosContablesTxt.setReadOnly(false);
                ingresosContablesTxt.setValue(rsRecords2.getDouble("TotalIngresos"));
                ingresosContablesTxt.setReadOnly(true);
                
                egresosContablesTxt.setReadOnly(false);
                egresosContablesTxt.setValue(rsRecords2.getDouble("TotalEgresos"));
                egresosContablesTxt.setReadOnly(true);
                
                saldoFinalContableTxt.setReadOnly(false);
                saldoFinalContableTxt.setValue(rsRecords2.getDouble("SaldoFinalContable"));
                saldoFinalContableTxt.setReadOnly(true);
                
                saldoFinalCuentaBancoTxt.setReadOnly(false);
                saldoFinalCuentaBancoTxt.setValue(rsRecords2.getDouble("SaldoFinalBanco"));
                saldoFinalCuentaBancoTxt.setReadOnly(true);
                                
                egresosTransitoTxt.setReadOnly(false);
                egresosTransitoTxt.setValue(rsRecords2.getDouble("EgresosNoConciliado_Monto"));
                egresosTransitoTxt.setReadOnly(true);
                
                saldoFinalNetoTxt.setReadOnly(false);
                saldoFinalNetoTxt.setValue(saldoFinalCuentaBancoTxt.getDoubleValueDoNotThrow() - egresosTransitoTxt.getDoubleValueDoNotThrow());
                saldoFinalNetoTxt.setReadOnly(true);
                
                validadorTxt.setReadOnly(false);
                validadorTxt.setValue(String.valueOf(saldoFinalContableTxt.getDoubleValueDoNotThrow()-saldoFinalNetoTxt.getDoubleValueDoNotThrow()));
                validadorTxt.setReadOnly(true);
                
                idRegistroCuentaBancos =rsRecords2.getString("IdCuentaBanco");
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar datos de la conciliacion: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void buscarSaldoInicialContable() {

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_cuentas_bancos as CB ";
        queryString += " INNER JOIN contabilidad_conciliacion_bancaria AS CCB on CB.IdCuentaBanco = CCB.IdCuentaBanco ";
        queryString += " WHERE CB.IdCuentaBanco = " + idRegistroCuentaBancos;
        queryString += " AND AnioMes < '" + Utileria.getFechaYYYYMM(mesDt.getValue()).replaceAll("/", "").replaceAll("-", "") + "'";
        queryString += " ORDER BY AnioMes DESC";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            saldoMesAnteriorTxt.setReadOnly(false);
            if (rsRecords.next()) {
                saldoInicialContableTxt.setValue(rsRecords.getDouble("saldoFinalContable"));
                saldoMesAnteriorTxt.setValue(rsRecords.getDouble("saldoFinalContable"));
            } else {
                saldoInicialContableTxt.setValue(0.00);
                saldoMesAnteriorTxt.setValue(0.00);
            }
            saldoMesAnteriorTxt.setReadOnly(true);

        } catch (Exception ex1) {
            System.out.println("Error al buscar el saldo incial osea saldo final del mes anterior " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT CB.*, CN.N5 ";
        queryString += " FROM contabilidad_cuentas_bancos CB ";
        queryString += " INNER JOIN contabilidad_nomenclatura_empresa AS CN ON CB.IdNomenclatura = CN.IdNomenclatura ";
        queryString += " WHERE CB.IdEmpresa = " + empresaId;
        queryString += " AND CN.IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                cuentaContableCbx.addItem(rsRecords.getString("CB.IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords.getString("CB.IdNomenclatura"), rsRecords.getString("CB.IdNomenclatura") + " " + rsRecords.getString("CN.N5") + " " + rsRecords.getString("CB.NoCuenta"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar Combo cuentas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void buscarIdCuentasBancos() {

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_cuentas_bancos";
        queryString += " WHERE IdNomenclatura =  " + cuentaContableCbx.getValue();
        queryString += " AND IdEmpresa =  " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                idRegistroCuentaBancos = rsRecords.getString("IdCuentaBanco");
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar Combo cuentas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarGridsConciliacion() {

        egresoFooter.getCell(EGRESO_PROPERTY).setText("0.00");
        ingresoFooter.getCell(INGRESO_PROPERTY).setText("0.00");

        ingresosContainer.removeAllItems();
        egresosContainer.removeAllItems();

        BigDecimal totalIngresos = new BigDecimal(0.00).setScale(2, RoundingMode.FLOOR);
        BigDecimal totalEgresos = new BigDecimal(0.00).setScale(2, RoundingMode.FLOOR);
                
        ingresosMes = new BigDecimal(0);
        egresosMes = new BigDecimal(0);
        
        try {

            queryString = " SELECT * FROM contabilidad_conciliacion_bancaria";
            queryString += " WHERE AnioMes = " + Utileria.getFechaYYYYMM(mesDt.getValue()).replaceAll("/", "");
            queryString += " AND IdEmpresa =" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
            queryString += " AND IdNomenclatura =" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) {
                Notification notif = new Notification("TOME EN CUENTA QUE EN ESTE RANGO DE FECHAS YA HAY UNA CONCILIACIÓN.", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(2000);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }

            queryString = "SELECT * FROM contabilidad_partida";
            queryString += " WHERE IdNomenclatura = " + cuentaContableCbx.getValue();
            queryString += " AND Fecha between ";
            queryString += " '2020-01-01'";
            queryString += " AND '" + utilidadesFecha.getMesUltimoDia(mesDt.getValue()).replaceAll("/", "-") + "'";
            queryString += " AND Estatus <> 'ANULADO'";
            queryString += " AND TRIM(TipoDocumento) NOT IN ('PARTIDA CIERRE','PARTIDA APERTURA', 'TRANSACCION ESPECIAL', ";
            queryString += "                           'PARTIDA AJUSTE', 'PARTIDA AJUSTE1', 'PARTIDA AJUSTE TEMPORAL')";
            queryString += " AND IdEmpresa =" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
            if (!idConciliacion.trim().isEmpty()) {
                queryString += " AND (IdConciliacion = 0 OR IdConciliacion = " + idConciliacion + ")";
            } else {
                queryString += " AND IdConciliacion = 0";
            }
            queryString += " ORDER BY Fecha";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado         

                do {

                    if (rsRecords.getDouble("Debe") > 0.00) {/// SON INGRESOS {                                                

                        Object itemId = ingresosContainer.addItem();
                        ingresosContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        ingresosContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        ingresosContainer.getContainerProperty(itemId, FECHA2_PROPERTY).setValue(Utileria.getFechaYYYYMMDD_1(rsRecords.getDate("Fecha")));
                        ingresosContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                        ingresosContainer.getContainerProperty(itemId, DOCTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                        ingresosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                        ingresosContainer.getContainerProperty(itemId, NOMBRE_CHEQUE_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                        ingresosContainer.getContainerProperty(itemId, INGRESO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Debe")));

                        if (idConciliacion.trim().isEmpty()) {
                            ingresosGrid.select(itemId);
                        } else {
                            if (!rsRecords.getString("IdConciliacion").equals("0")) {
                                ingresosGrid.select(itemId);
                            }
                        }

                        if (Utileria.getFechaMM(rsRecords.getDate("Fecha")).equals(Utileria.getFechaMM(mesDt.getValue()))) {
                            ingresosMes = ingresosMes.add(new BigDecimal(rsRecords.getDouble("Debe")));
                        }

                        totalIngresos = totalIngresos.add(new BigDecimal(rsRecords.getDouble("Debe")));

                    } else if (rsRecords.getDouble("Haber") > 0.00) { // son Egresos

                        Object itemId = egresosContainer.addItem();
                        egresosContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        egresosContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        egresosContainer.getContainerProperty(itemId, FECHA2_PROPERTY).setValue(Utileria.getFechaYYYYMMDD_1(rsRecords.getDate("Fecha")));
                        egresosContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                        egresosContainer.getContainerProperty(itemId, DOCTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                        egresosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                        egresosContainer.getContainerProperty(itemId, NOMBRE_CHEQUE_PROPERTY).setValue(rsRecords.getString("NombreCheque"));
                        egresosContainer.getContainerProperty(itemId, EGRESO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Haber")));

                        if (!rsRecords.getString("IdConciliacion").equals("0")) {
                            egresosGrid.select(itemId);
                        }
                        if (Utileria.getFechaMM(rsRecords.getDate("Fecha")).equals(Utileria.getFechaMM(mesDt.getValue()))) {
                            egresosMes = egresosMes.add(new BigDecimal(rsRecords.getDouble("Haber")));
                        }
                        totalEgresos = totalEgresos.add(new BigDecimal(rsRecords.getDouble("Haber")));
                        Logger.getLogger(this.getClass()).log(Level.INFO, "FECHA = " + rsRecords.getDate("Fecha") + " MONTO=" + rsRecords.getDouble("HABER") + " TOTALEGRESOS = " + totalEgresos.doubleValue());
                    }

                } while (rsRecords.next());

                /** agregue esto porque no se comprende lo de egreses del mes **/
                //ingresosMes = totalIngresos;
                //egresosMes  = totalEgresos;

                conciliacionTxt.setReadOnly(false);
                conciliacionTxt.setValue(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + String.valueOf(cuentaContableCbx.getValue()) + Utileria.getFechaYYYYMM(mesDt.getValue()));
                conciliacionTxt.setReadOnly(true);

                ingresoFooter.getCell(INGRESO_PROPERTY).setText(numberFormat.format(totalIngresos.doubleValue()));
                egresoFooter.getCell(EGRESO_PROPERTY).setText(numberFormat.format(totalEgresos.doubleValue()));

                ingresosContablesTxt.setReadOnly(false);
                ingresosContablesTxt.setValue(ingresosMes.doubleValue());
                ingresosContablesTxt.setReadOnly(true);
                
                egresosContablesTxt.setReadOnly(false);
                egresosContablesTxt.setValue(egresosMes.doubleValue());
                egresosContablesTxt.setReadOnly(true);

                saldoFinalContableTxt.setReadOnly(false);
                double saldoFinal = (saldoInicialContableTxt.getDoubleValueDoNotThrow() + ingresosContablesTxt.getDoubleValueDoNotThrow())
                        - egresosContablesTxt.getDoubleValueDoNotThrow();
                
                saldoFinalContableTxt.setValue(Utileria.numberFormatEntero.format(saldoFinal));
                
                saldoFinalContableTxt.setReadOnly(true);

                saldoFinalCuentaBancoTxt.setReadOnly(false);
                saldoFinalCuentaBancoTxt.setValue(saldoFinalBancoTxt.getDoubleValueDoNotThrow());
                saldoFinalCuentaBancoTxt.setReadOnly(true);

            }
        } catch (Exception ex) {
            System.out.println("Error al llenar grids :" + ex);
            ex.printStackTrace();
        }
    }

    public void guardarConciliacion() {

        try {

            BigDecimal totalMontoEgresosNoConciliado = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalMontoIngreosNoConciliado = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            for (Object itemId : egresosContainer.getItemIds()) {
                Item item = egresosContainer.getItem(itemId);

                if (!egresosGrid.isSelected(itemId)) {
                    totalMontoEgresosNoConciliado = totalMontoEgresosNoConciliado.add(new BigDecimal(String.valueOf(item.getItemProperty(EGRESO_PROPERTY).getValue()).replaceAll(",", "")));
                }
            }

            for (Object itemId : ingresosContainer.getItemIds()) {
                Item item = ingresosContainer.getItem(itemId);

                if (!ingresosGrid.isSelected(itemId)) {
                    totalMontoIngreosNoConciliado = totalMontoIngreosNoConciliado.add(new BigDecimal(String.valueOf(item.getItemProperty(INGRESO_PROPERTY).getValue()).replaceAll(",", "")));
                }
            }

            if (idConciliacion.trim().isEmpty()) {

                queryString = "INSERT INTO contabilidad_conciliacion_bancaria(IdCuentaBanco, IdEmpresa, IdNomenclatura,  AnioMes, SaldoInicialContable, ";
                queryString += " SaldoFinalContable, EgresosNoConciliado_Monto, IngresosNoConciliado_Monto,  ";
                queryString += " Correlativo, TotalIngresos, TotalEgresos, SaldoFinalBanco, CreadoUsuario , CreadoFechaYHora, Estatus)";
                queryString += " VALUES ";
                queryString += "(";
                queryString += "" + idRegistroCuentaBancos;
                queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                queryString += "," + cuentaContableCbx.getValue();
                queryString += ",'" + Utileria.getFechaYYYYMM(mesDt.getValue()).replaceAll("/", "") + "'";
                queryString += "," + saldoInicialContableTxt.getDoubleValueDoNotThrow();
                queryString += "," + saldoFinalContableTxt.getDoubleValueDoNotThrow();
                queryString += "," + totalMontoEgresosNoConciliado.doubleValue();
                queryString += "," + totalMontoIngreosNoConciliado.doubleValue();                
                queryString += ",'" + conciliacionTxt.getValue() + "'";
                queryString += "," + ingresosMes.doubleValue();
                queryString += "," + egresosMes.doubleValue();
                queryString += "," + saldoFinalBancoTxt.getDoubleValueDoNotThrow();
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",'GUARDADA'";
                queryString += ")";

                stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                stPreparedQuery.executeUpdate();
                rsRecords = stPreparedQuery.getGeneratedKeys();

                rsRecords.next();

                idConciliacion = String.valueOf(rsRecords.getInt(1));

            } else {

                queryString = "  DELETE FROM conciliacion_bancaria_noconciliados";
                queryString += " WHERE IdConciliacion =" + idConciliacion;
                queryString += " AND IdEmpresa =" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

                stQuery.executeUpdate(queryString);

                queryString = "  UPDATE contabilidad_conciliacion_bancaria set ";
                queryString += " SaldoInicialContable = " + saldoInicialContableTxt.getValue();
                queryString += ", SaldoFinalContable = " + saldoFinalContableTxt.getValue();
                queryString += ", EgresosNoConciliado_Monto =" + totalMontoEgresosNoConciliado.doubleValue();
                queryString += ", IngresosNoConciliado_Monto =" + totalMontoIngreosNoConciliado.doubleValue();                
                queryString += ", TotalIngresos = " + ingresosMes.doubleValue();
                queryString += ", TotalEgresos = " + egresosMes.doubleValue();
                queryString += ", TotalIngresos = " + ingresosMes.doubleValue();
                queryString += ", TotalEgresos = " + egresosMes.doubleValue();
                queryString += ", SaldoFinalBanco = " + saldoFinalBancoTxt.getDoubleValueDoNotThrow();
                queryString += ", CreadoUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ", CreadoFechaYHora = current_timestamp";
                queryString += " Where Correlativo = '" + conciliacionTxt.getValue() + "'";

                stQuery.executeUpdate(queryString);
            }

            for (Object itemId : egresosContainer.getItemIds()) {
                Item item = egresosContainer.getItem(itemId);

                queryString = " UPDATE  contabilidad_partida ";
                if (egresosGrid.isSelected(itemId)) {
                    queryString += " SET IdConciliacion = " + idConciliacion;
                } else {
                    queryString += " SET IdConciliacion = 0 ";
                }
                queryString += " WHERE CodigoPartida = '" + String.valueOf(item.getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue()) + "'";
                queryString += " AND IdNomenclatura = " + cuentaContableCbx.getValue();

                stQuery.executeUpdate(queryString);

                if (!egresosGrid.isSelected(itemId)) { ///INSERTAR EN LA TABLA RESPALDO PARA NO CONCILIADOS POR MES

                    queryString = "INSERT INTO conciliacion_bancaria_noconciliados(IdCuentaBanco, IdConciliacion, IdEmpresa, IdNomenclatura,  AnioMes, CodigoPartida, NumeroDocumento, ";
                    queryString += "  Fecha, Descripcion, Monto, Tipo)";
                    queryString += " VALUES ";
                    queryString += "(";
                    queryString += "" + idRegistroCuentaBancos;
                    queryString += "," + idConciliacion;
                    queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                    queryString += "," + cuentaContableCbx.getValue();
                    queryString += ",'" + Utileria.getFechaYYYYMM(mesDt.getValue()).replaceAll("/", "") + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(DOCTO_PROPERTY).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(FECHA2_PROPERTY).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(NOMBRE_CHEQUE_PROPERTY).getValue()) + "'";
                    queryString += "," + String.valueOf(item.getItemProperty(EGRESO_PROPERTY).getValue()).replaceAll(",", "");
                    queryString += ",'EGRESOS'";
                    queryString += ")";
                    
                    stQuery.executeUpdate(queryString);
                }

            }

            for (Object itemId : ingresosContainer.getItemIds()) {
                Item item = ingresosContainer.getItem(itemId);

                queryString = " UPDATE  contabilidad_partida ";
                if (ingresosGrid.isSelected(itemId)) {
                    queryString += " SET IdConciliacion = " + idConciliacion;
                } else {
                    queryString += " SET IdConciliacion = 0 ";
                }
                queryString += " WHERE CodigoPartida = '" + String.valueOf(item.getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue()) + "'";
                queryString += " AND IdNomenclatura = " + cuentaContableCbx.getValue();

                stQuery.executeUpdate(queryString);

                if (!ingresosGrid.isSelected(itemId)) { ///INSERTAR EN LA TABLA RESPALDO PARA NO CONCILIADOS POR MES

                    queryString = "INSERT Into conciliacion_bancaria_noconciliados(IdCuentaBanco, IdConciliacion, IdEmpresa, IdNomenclatura,  AnioMes, CodigoPartida, NumeroDocumento, ";
                    queryString += "  Fecha, Descripcion, Monto, Tipo)";
                    queryString += " VALUES ";
                    queryString += "(";
                    queryString += "" + idRegistroCuentaBancos;
                    queryString += "," + idConciliacion;
                    queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                    queryString += "," + cuentaContableCbx.getValue();
                    queryString += ",'" + Utileria.getFechaYYYYMM(mesDt.getValue()).replaceAll("/", "") + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(DOCTO_PROPERTY).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(FECHA2_PROPERTY).getValue()) + "'";
                    queryString += ",'" + String.valueOf(item.getItemProperty(NOMBRE_CHEQUE_PROPERTY).getValue()) + "'";
                    queryString += "," + String.valueOf(item.getItemProperty(INGRESO_PROPERTY).getValue()).replaceAll(",", "");
                    queryString += ",'INGRESO'";
                    queryString += ")";

                    stQuery.executeUpdate(queryString);
                }

            }

            egresosTransitoTxt.setReadOnly(false);
            egresosTransitoTxt.setValue(totalMontoEgresosNoConciliado.doubleValue());
            egresosTransitoTxt.setReadOnly(true);
            
            saldoFinalNetoTxt.setReadOnly(false);
            saldoFinalNetoTxt.setValue(saldoFinalCuentaBancoTxt.getDoubleValueDoNotThrow() - egresosTransitoTxt.getDoubleValueDoNotThrow());
            saldoFinalNetoTxt.setReadOnly(true);

            validadorTxt.setReadOnly(false);
            validadorTxt.setValue(saldoFinalContableTxt.getDoubleValueDoNotThrow() - saldoFinalNetoTxt.getDoubleValueDoNotThrow());
            validadorTxt.setReadOnly(true);

            Notification notif = new Notification("CONCILIACIÓN GUARDADA CON EXITO!.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.SAVE);
            notif.show(Page.getCurrent());

            ((ConciliacionBancariaView) (mainUI.getNavigator().getCurrentView())).llenarGridConciliacion();

        } catch (Exception ex1) {
            System.out.println("Error al actualizar conciliacion : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public void finalizarConciliacion() {

        try {

            queryString = " UPDATE contabilidad_conciliacion_bancaria ";
            queryString += " SET Estatus = 'FINALIZADA'";
            queryString += " WHERE Correlativo = '" + conciliacionTxt.getValue() + "'";
            queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ConciliacionPDF conciliacionPDF = new ConciliacionPDF(idConciliacion, getEmpresaNit(), Utileria.getFechaMMYYYY(mesDt.getValue()), getNombreBanco());
            UI.getCurrent().addWindow(conciliacionPDF);
            conciliacionPDF.center();

            close();

        } catch (Exception e) {
            System.out.println("Error al actualizar estado de la conciliacion bancaria " + e);
            e.printStackTrace();
        }
    }

    public String getEmpresaNit() {
        String strNit = "N/A";

        queryString = " SELECT Nit FROM contabilidad_empresa ";
        queryString += " WHERE IdEmpresa = " + empresaId;

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) {
                strNit = rsRecords2.getString("Nit");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar NIT de empresa: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return strNit;
    }

    public String getNombreBanco() {
        String nombreBanco = "";

        queryString = " SELECT *, CCB.IdProveedor,CCB.NoCuenta, proveedor_empresa.Nombre";
        queryString += " FROM contabilidad_conciliacion_bancaria AS CB";
        queryString += " INNER JOIN contabilidad_cuentas_bancos AS CCB on CB.IdCuentaBanco = CCB.IdCuentaBanco";
        queryString += " INNER JOIN proveedor_empresa on CCB.IdProveedor = proveedor_empresa.IDProveedor";
        queryString += " WHERE CB.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;
        queryString += " AND CB.AnioMes = '" + Utileria.getFechaYYYYMM(mesDt.getValue()).replaceAll("/", "") + "'";
        queryString += " AND CB.IdConciliacionBancaria = " + idConciliacion;

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) {
                nombreBanco = rsRecords2.getString("proveedor.Nombre") + rsRecords2.getString("CCB.NoCuenta");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el nombre del banco(PROVEEDOR): " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return nombreBanco;
    }
}
