package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.compras.IngresoDocumentosView;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IgssPorDeclararForm extends Window {

    UI mainUI;

    ComboBox empresaCbx;

    DateField finDt;
    Button buscarBtn;

    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords, rsRecords1, rsRecords2;

    String queryString;

    VerticalLayout mainLayout;

    Grid igssPatronalDetailGrid;
    Grid igssLaboralDetailGrid;

    Grid.FooterRow igssPatronalFooter;
    Grid.FooterRow igssLaboralFooter;

    NumberField patronoalSegunIgss;
    NumberField laboralSegunIgss;

    public IndexedContainer igssHeaderContainer = new IndexedContainer();
    public IndexedContainer igssPatronalContainer = new IndexedContainer();
    public IndexedContainer igssLaboralContainer = new IndexedContainer();

    static final String CODIGO_PROPERTY = "CodigoCC";
    static final String TIPO_DOCUMENTO_PROPERTY = "Tipo Documento";
    static final String NO_DOCUMENTO_PROPERTY = "Documento";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String CLIENTE_PROPERTY = "Cliente";
    static final String FECHA_PROPERTY = "Fecha";
    static final String MONTO_PROPERTY = "MONTO";
    static final String MONTOSF_PROPERTY = "MontoSF";
    static final String CODIGOPARTIDA_PROPERTY = "CodigoPartida";

    TextField serieTxt;
    TextField numeroTxt;
    DateField fechaFormularioDt;
    NumberField montoTxt;
    NumberField multaTxt;
    Button crearPartidaBtn;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("######0.00");

    double totalIgssPatronal;
    double totalIgssLaboral;

    boolean insertandoPartida;

    double laboralSegunIGSS_N = 0;
    double patronalSegunIGSS_N = 0;

    public IgssPorDeclararForm() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("95%");
        setHeight("95%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("90%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        finDt = new DateField("FECHAS DE PLANILLAS AL : ");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("15em");
//        finDt.addValueChangeListener(event -> {
//                    fillGrids();
//                }
//        );

        buscarBtn = new Button("BUSCAR");
        buscarBtn.setIcon(FontAwesome.SAVE);
        buscarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        buscarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillDetailsGrid();
            }
        });
        
        Label titleLbl = new Label("IGSS POR DECLARAR");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(finDt);
        layoutTitle.setComponentAlignment(finDt, Alignment.MIDDLE_CENTER);
        layoutTitle.addComponent(buscarBtn);
        layoutTitle.setComponentAlignment(buscarBtn, Alignment.MIDDLE_CENTER);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        createGridDetails();
        createGridIgssFooter();

    }

    public void llenarComboEmpresa() {
        empresaCbx.addItem(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId());
        empresaCbx.setItemCaption(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId(), ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName());
        empresaCbx.select(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId());
    }

    public void createGridDetails() {

        HorizontalLayout facturasYPartidasLayout = new HorizontalLayout();
        facturasYPartidasLayout.setWidth("100%");
        facturasYPartidasLayout.addStyleName("rcorners3");
        facturasYPartidasLayout.setSpacing(true);
        facturasYPartidasLayout.setMargin(new MarginInfo(false, true, false, true));

        igssPatronalContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        igssPatronalContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        igssPatronalContainer.addContainerProperty(NO_DOCUMENTO_PROPERTY, String.class, null);
        igssPatronalContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        igssPatronalContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        igssPatronalContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        igssPatronalContainer.addContainerProperty(MONTOSF_PROPERTY, String.class, null);
        igssPatronalContainer.addContainerProperty(CODIGOPARTIDA_PROPERTY, String.class, null);

        igssPatronalDetailGrid = new Grid("CUOTA PATRONAL IGSS POR PAGAR", igssPatronalContainer);
        igssPatronalDetailGrid.setWidth("100%");
        igssPatronalDetailGrid.setImmediate(true);
        igssPatronalDetailGrid.setHeightMode(HeightMode.ROW);
        igssPatronalDetailGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        igssPatronalDetailGrid.setHeightByRows(10);
        igssPatronalDetailGrid.setResponsive(true);
        igssPatronalDetailGrid.setEditorBuffered(false);
        igssPatronalDetailGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (igssPatronalDetailGrid.getSelectedRows() != null) {
                    setTotalIgss();
                }
            }
        });

        igssPatronalDetailGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        igssPatronalDetailGrid.getColumn(MONTOSF_PROPERTY).setHidable(true).setHidden(true);
        igssPatronalDetailGrid.getColumn(CODIGO_PROPERTY).setHidable(true).setHidden(true);

        igssPatronalFooter = igssPatronalDetailGrid.appendFooterRow();
        igssPatronalFooter.getCell(PROVEEDOR_PROPERTY).setText("Total : ");
        igssPatronalFooter.getCell(PROVEEDOR_PROPERTY).setStyleName("rightalign");
        igssPatronalFooter.getCell(MONTO_PROPERTY).setText("0.00");
        igssPatronalFooter.getCell(MONTO_PROPERTY).setStyleName("rightalign");

        igssLaboralContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        igssLaboralContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        igssLaboralContainer.addContainerProperty(NO_DOCUMENTO_PROPERTY, String.class, null);
        igssLaboralContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        igssLaboralContainer.addContainerProperty(CLIENTE_PROPERTY, String.class, null);
        igssLaboralContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        igssLaboralContainer.addContainerProperty(MONTOSF_PROPERTY, String.class, null);
        igssLaboralContainer.addContainerProperty(CODIGOPARTIDA_PROPERTY, String.class, null);

        igssLaboralDetailGrid = new Grid("CUOTA LABORAL IGSS POR PAGAR", igssLaboralContainer);
        igssLaboralDetailGrid.setWidth("100%");
        igssLaboralDetailGrid.setImmediate(true);
        igssLaboralDetailGrid.setHeightMode(HeightMode.ROW);
        igssLaboralDetailGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        igssLaboralDetailGrid.setHeightByRows(10);
        igssLaboralDetailGrid.setResponsive(true);
        igssLaboralDetailGrid.setEditorBuffered(false);
        igssLaboralDetailGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (igssLaboralDetailGrid.getSelectedRows() != null) {
                    setTotalIgss();
                }
            }
        });

        igssLaboralDetailGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        igssLaboralDetailGrid.getColumn(CODIGO_PROPERTY).setHidable(true).setHidden(true);
        igssLaboralDetailGrid.getColumn(MONTOSF_PROPERTY).setHidable(true).setHidden(true);

        igssLaboralFooter = igssLaboralDetailGrid.appendFooterRow();
        igssLaboralFooter.getCell(CLIENTE_PROPERTY).setText("Total : ");
        igssLaboralFooter.getCell(CLIENTE_PROPERTY).setStyleName("rightalign");
        igssLaboralFooter.getCell(MONTO_PROPERTY).setText("0.00");
        igssLaboralFooter.getCell(MONTO_PROPERTY).setStyleName("rightalign");

        facturasYPartidasLayout.addComponent(igssPatronalDetailGrid);
        facturasYPartidasLayout.addComponent(igssLaboralDetailGrid);

        mainLayout.addComponent(facturasYPartidasLayout);
        mainLayout.setComponentAlignment(facturasYPartidasLayout, Alignment.MIDDLE_CENTER);

        HorizontalLayout segunIGSSLayout = new HorizontalLayout();
        segunIGSSLayout.setWidth("100%");
        segunIGSSLayout.addStyleName("rcorners3");
        segunIGSSLayout.setSpacing(true);
        segunIGSSLayout.setMargin(new MarginInfo(false, true, false, true));

        patronoalSegunIgss = new NumberField("Cuota Patronal Segun IGSS : ");
        patronoalSegunIgss.setDecimalAllowed(true);
        patronoalSegunIgss.setDecimalPrecision(2);
        patronoalSegunIgss.setMinimumFractionDigits(2);
        patronoalSegunIgss.setDecimalSeparator('.');
        patronoalSegunIgss.setDecimalSeparatorAlwaysShown(true);
        patronoalSegunIgss.setValue(0d);
        patronoalSegunIgss.setGroupingUsed(true);
        patronoalSegunIgss.setGroupingSeparator(',');
        patronoalSegunIgss.setGroupingSize(3);
        patronoalSegunIgss.setImmediate(true);
        patronoalSegunIgss.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        patronoalSegunIgss.setWidth("8em");

        laboralSegunIgss = new NumberField("Cuota Laboral Segun IGSS : ");
        laboralSegunIgss.setDecimalAllowed(true);
        laboralSegunIgss.setDecimalPrecision(2);
        laboralSegunIgss.setMinimumFractionDigits(2);
        laboralSegunIgss.setDecimalSeparator('.');
        laboralSegunIgss.setDecimalSeparatorAlwaysShown(true);
        laboralSegunIgss.setValue(0d);
        laboralSegunIgss.setGroupingUsed(true);
        laboralSegunIgss.setGroupingSeparator(',');
        laboralSegunIgss.setGroupingSize(3);
        laboralSegunIgss.setImmediate(true);
        laboralSegunIgss.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        laboralSegunIgss.setWidth("8em");

        segunIGSSLayout.addComponents(patronoalSegunIgss, laboralSegunIgss);
        segunIGSSLayout.setComponentAlignment(patronoalSegunIgss, Alignment.MIDDLE_CENTER);
        segunIGSSLayout.setComponentAlignment(laboralSegunIgss, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(segunIGSSLayout);
        mainLayout.setComponentAlignment(segunIGSSLayout, Alignment.MIDDLE_CENTER);
    }

    public void createGridIgssFooter() {

        fechaFormularioDt = new DateField("Fecha formulario :");
        fechaFormularioDt.setDateFormat("dd/MM/yyyy");
        fechaFormularioDt.setValue(new Date());
        fechaFormularioDt.setWidth("8em");

        serieTxt = new TextField("Serie formulario : ");
        serieTxt.setWidth("8em");
        serieTxt.addStyleName("mayusculas");
        serieTxt.setValue("2237");

        numeroTxt = new TextField("Número formulario : ");
        numeroTxt.setWidth("8em");

        montoTxt = new NumberField("Monto formulario : ");
        montoTxt.setDescription("Monto del formulario con la multa incluida.");
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setValue(0d);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setWidth("8em");
//        montoTxt.addValueChangeListener(event -> {
//            if (cuentaContable1Cbx != null) {
//                verificarProveedor();
//            }
//        });

        multaTxt = new NumberField("Multa : ");
        multaTxt.setDescription("El monto del formulario ya debe incluir la multa, este campo es solamente para incluirla en la partida contable.");
        multaTxt.setDecimalAllowed(true);
        multaTxt.setDecimalPrecision(2);
        multaTxt.setMinimumFractionDigits(2);
        multaTxt.setDecimalSeparator('.');
        multaTxt.setDecimalSeparatorAlwaysShown(true);
        multaTxt.setValue(0d);
        multaTxt.setGroupingUsed(true);
        multaTxt.setGroupingSeparator(',');
        multaTxt.setGroupingSize(3);
        multaTxt.setImmediate(true);
        multaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        multaTxt.setWidth("8em");

        crearPartidaBtn = new Button("REGISTRAR FORMULARIO");
        crearPartidaBtn.setIcon(FontAwesome.SAVE);
        crearPartidaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        crearPartidaBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de registrar el Formulario IGSS con esta fecha : " + Utileria.getFechaDDMMYYYY(fechaFormularioDt.getValue()) + " ?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            insertPartidas();
                        }
                    }
                }
                );
            }
        });
        
        FormLayout formularioForm = new FormLayout();

        formularioForm.addComponent(fechaFormularioDt);
//        formularioLayout.setComponentAlignment(fechaFormularioDt, Alignment.BOTTOM_LEFT);
        formularioForm.addComponent(serieTxt);
//        formularioLayout.setComponentAlignment(serieTxt, Alignment.BOTTOM_LEFT);
        formularioForm.addComponent(numeroTxt);
//        formularioLayout.setComponentAlignment(numeroTxt, Alignment.BOTTOM_LEFT);
        formularioForm.addComponent(montoTxt);
//        formularioLayout.setComponentAlignment(montoTxt, Alignment.BOTTOM_LEFT);
        formularioForm.addComponent(multaTxt);
//        formularioLayout.setComponentAlignment(multaTxt, Alignment.BOTTOM_LEFT);
        formularioForm.addComponent(crearPartidaBtn);
        formularioForm.setComponentAlignment(crearPartidaBtn, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(formularioForm);
        mainLayout.setComponentAlignment(formularioForm, Alignment.TOP_CENTER);
    }

    public void fillDetailsGrid() {
        igssPatronalContainer.removeAllItems();
        igssLaboralContainer.removeAllItems();

        igssPatronalFooter.getCell(MONTO_PROPERTY).setText("0.00");
        igssLaboralFooter.getCell(MONTO_PROPERTY).setText("0.00");
        igssPatronalFooter.getCell(MONTO_PROPERTY).setText("0.00");
        igssLaboralFooter.getCell(MONTO_PROPERTY).setText("0.00");

        totalIgssPatronal = 0.00; totalIgssLaboral = 0.00;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT CodigoCC, TipoDocumento, IdNomenclatura, Fecha, SerieDocumento, NumeroDocumento, ";
            queryString += " DebeQuetzales, HaberQuetzales, NombreProveedor, CodigoPartida ";
            queryString += " FROM contabilidad_partida";
            queryString += " WHERE  IdEmpresa = " + empresaCbx.getValue();
            queryString += " AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
            queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaPatronalIgssPorPagar();
            queryString += " AND UPPER(TipoDocumento) = 'PLANILLA'";
            queryString += " AND PagadoIVa = 'NO'";
            queryString += " AND UPPER(Estatus) <> 'ANULADO'";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                do {

                    double saldo = (rsRecords.getDouble("HaberQuetzales") - rsRecords.getDouble("DebeQuetzales"));

                    Object itemId = igssPatronalContainer.addItem();
                    igssPatronalContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    igssPatronalContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    igssPatronalContainer.getContainerProperty(itemId, NO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                    igssPatronalContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    igssPatronalContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    igssPatronalContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(saldo));
                    igssPatronalContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(numberFormat2.format(saldo));
                    igssPatronalContainer.getContainerProperty(itemId, CODIGOPARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    igssPatronalDetailGrid.select(itemId);
                    totalIgssPatronal += saldo;
                } while (rsRecords.next());

                igssPatronalFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalIgssPatronal));
            }

            queryString = "SELECT CodigoCC, TipoDocumento, IdNomenclatura, Fecha, SerieDocumento, NumeroDocumento, ";
            queryString += " NombreProveedor, DebeQuetzales, HaberQuetzales, CodigoPartida ";
            queryString += " FROM contabilidad_partida ";
            queryString += " WHERE  contabilidad_partida.IdEmpresa = " + empresaCbx.getValue();
            queryString += " AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
            queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaLaboralIgssPorPagar();
            queryString += " AND UPPER(TipoDocumento) = 'PLANILLA'";
            queryString += " AND PagadoIVa = 'NO'";
            queryString += " AND UPPER(Estatus) <> 'ANULADO'";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    double saldo = (rsRecords.getDouble("HaberQuetzales") - rsRecords.getDouble("DebeQuetzales"));

                    Object itemId = igssLaboralContainer.addItem();
                    igssLaboralContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    igssLaboralContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    igssLaboralContainer.getContainerProperty(itemId, NO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                    igssLaboralContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    igssLaboralContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    igssLaboralContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(saldo));
                    igssLaboralContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(numberFormat2.format(saldo));
                    igssLaboralContainer.getContainerProperty(itemId, CODIGOPARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    igssLaboralDetailGrid.select(itemId);

                    totalIgssLaboral += saldo;
                } while (rsRecords.next());

                igssLaboralFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalIgssLaboral));
            }

            setTotalIgss();

            serieTxt.setReadOnly(false);
            serieTxt.setValue("DR-182-1");
            numeroTxt.setReadOnly(false);
            numeroTxt.setValue("");
            montoTxt.setReadOnly(false);
            montoTxt.setValue(Double.valueOf(numberFormat2.format(totalIgssPatronal + totalIgssLaboral)).doubleValue());
            fechaFormularioDt.setReadOnly(false);
            crearPartidaBtn.setVisible(true);

        } catch (Exception ex) {
            System.out.println("Error al listar tabla igss por pagar: " + ex);
            ex.printStackTrace();
        }
    }
    
    private void setTotalIgss() {
        totalIgssPatronal = 0.00; totalIgssLaboral = 0.00;
        for (Object itemId : igssPatronalContainer.getItemIds()) {
            if (igssPatronalDetailGrid.isSelected(itemId)) {
                totalIgssPatronal += Double.valueOf(igssPatronalContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).getValue().toString());
            }
        }
        totalIgssPatronal = Double.valueOf(String.format("%.2f", totalIgssPatronal));

        for (Object itemId : igssLaboralContainer.getItemIds()) {
            if (igssLaboralDetailGrid.isSelected(itemId)) {
                totalIgssLaboral += Double.valueOf(igssLaboralContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).getValue().toString());
            }
        }
        totalIgssLaboral = Double.valueOf(String.format("%.2f", totalIgssLaboral));

        igssPatronalFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalIgssPatronal));
        igssLaboralFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalIgssLaboral));

        montoTxt.setReadOnly(false);
        montoTxt.setValue(Double.valueOf(numberFormat2.format(totalIgssPatronal + totalIgssLaboral)).doubleValue());

    }

    public void insertPartidas() {

        //-----
        laboralSegunIGSS_N = laboralSegunIgss.getDoubleValueDoNotThrow();
        patronalSegunIGSS_N = patronoalSegunIgss.getDoubleValueDoNotThrow();
        //-----

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(new Date()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(new Date()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresaCbx.getValue())), Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (this.serieTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese la serie del documento.", Notification.Type.WARNING_MESSAGE);
            serieTxt.focus();
            return;
        }
        if (this.numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese el número de documento.", Notification.Type.WARNING_MESSAGE);
            numeroTxt.focus();
            return;
        }

        if(laboralSegunIgss.getDoubleValueDoNotThrow() <= 0) {
            Notification.show("Por favor ingrese el monto de Cuota Laboral Igss según el Igss.", Notification.Type.WARNING_MESSAGE);
            laboralSegunIgss.focus();
            return;
        }
        if(patronoalSegunIgss.getDoubleValueDoNotThrow() <= 0) {
            Notification.show("Por favor ingrese el monto de Cuota Patronal Igss según el Igss.", Notification.Type.WARNING_MESSAGE);
            patronoalSegunIgss.focus();
            return;
        }

        BigDecimal totalIgss = new BigDecimal(0);
        totalIgss.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalIgss = totalIgss.add(new BigDecimal(laboralSegunIgss.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalIgss = totalIgss.add(new BigDecimal(patronoalSegunIgss.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalIgss = totalIgss.add(new BigDecimal(multaTxt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);

        if (totalIgss.doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
            Notification notif = new Notification("EL MONTO DEL FORMULARIO NO CUADRA CON LA SUMAS DE LAS CUOTAS + LA MULTA : ",
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            return;
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartida = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "1";

        queryString = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + codigoPartida + "%'";
        queryString += " order by codigoPartida desc ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " Select * from contabilidad_partida";
        queryString += " Where SerieDocumento  = '" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " And TipoDocumento = 'RECIBO CONTABLE'";
        queryString += " And MonedaDocumento = 'QUETZALES'";

        try {
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                Notification.show("Este documento ya fué ingresado, revise.!.", Notification.Type.WARNING_MESSAGE);
                numeroTxt.focus();
                return;
            }
        } catch (Exception ex1) {
            System.out.println("Error al buscar documento : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        /// Ingreso del haber
        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, Fecha, IdOrdenCompra, IdProveedor, NITProveedor, NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, IdLiquidador, Descripcion, Referencia,";
        queryString += " CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre)";
        queryString += " Values ";
        queryString += " (";
        queryString += empresaCbx.getValue();
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoPartida + "'"; //codigoCC
        queryString += ",'RECIBO CONTABLE'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
        queryString += ",0";
        queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getIgss();  //IGSS
        queryString += ",'2342855'"; //IGSS
        queryString += ",'Instituto Guatemalteco de Seguridad Social'";
        queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones(); // instituciones x pagar
        queryString += ",'QUETZALES'";
        queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
        queryString += ",0"; //debe
        queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //HABER
        queryString += ",0"; //debe Q
        queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //HABERQ
        queryString += ",1.0"; //tipo cambio
        queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //SALDO
        queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // SAT idliquidador
        queryString += ",'RECIBO CONTABLE IGSS " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Instituto Guatemalteco de Seguridad Social'";
        queryString += ",'NO'"; //REFRENCIA NO ISR
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ",null";
        queryString += ",null";
        queryString += ",0";
        queryString += ",null";
        queryString += ")";

        //CICLO DE IGSS PATRONAL POR PAGAR
        for (Object itemId : igssPatronalContainer.getItemIds()) {
            if (igssPatronalDetailGrid.isSelected(itemId)) {
                queryString += ",(";
                queryString += empresaCbx.getValue();
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + igssPatronalContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "'"; //codigoCC
                queryString += ",'RECIBO CONTABLE IGSS'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
                queryString += ",0";
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getIgss();  //IGSS
                queryString += ",'2342855'"; //IGSS
                queryString += ",'Instituto Guatemalteco de Seguridad Social'";
                queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += ", " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaPatronalIgssPorPagar();
                queryString += ",'QUETZALES'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
                queryString += "," + String.valueOf(igssPatronalContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue()).replaceAll(",", "").replaceAll("-", "");
                queryString += ",0.00"; // HABER
                queryString += "," + String.valueOf(igssPatronalContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue()).replaceAll(",", "").replaceAll("-", "");
                queryString += ",0.00"; // HABER Q
                queryString += ",1.00"; // tipo cambio
                queryString += ",0.00"; // saldo
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); //Sat
                queryString += ",'RECIBO CONTABLE IGSS " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Instituto Guatemalteco de Seguridad Social'";
                queryString += ",'NO'"; //REFRENCIA NO ISR
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";
                queryString += ")";
            }
        }

        //CICLO DE IGSS LABORAL
        for (Object itemId : igssLaboralContainer.getItemIds()) {
            if (igssLaboralDetailGrid.isSelected(itemId)) {
                queryString += ",(";
                queryString += empresaCbx.getValue();
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + igssLaboralContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "'"; //codigoCC
                queryString += ",'RECIBO CONTABLE'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
                queryString += ",0";
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getIgss();  //IGSS
                queryString += ",'2342855'"; //IGSS
                queryString += ",'Instituto Guatemalteco de Seguridad Social'";
                queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaLaboralIgssPorPagar();
                queryString += ",'QUETZALES'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
                queryString += "," + String.valueOf(igssLaboralContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue()).replaceAll(",", "").replaceAll("-", "");
                queryString += ",0.00"; // haber
                queryString += "," + String.valueOf(igssLaboralContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue()).replaceAll(",", "").replaceAll("-", "");
                queryString += ",0.00"; // haber Q
                queryString += ",1.00"; // tipo cambio
                queryString += ",0.00"; // saldo
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat();
                queryString += ",'RECIBO CONTABLE IGSS " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Instituto Guatemalteco de Seguridad Social'";
                queryString += ",'NO'"; //REFRENCIA NO ISR
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";
                queryString += ")";
            }
        }

        // ajuste por montos calculados por IGSS PATRONAL
Logger.getLogger(this.getClass().getName()).log(Level.INFO, "totalIgssPatronal - patronoalSegunIgss.getDoubleValueDoNotThrow() = " + (totalIgssPatronal - patronoalSegunIgss.getDoubleValueDoNotThrow()));
        if (totalIgssPatronal - patronoalSegunIgss.getDoubleValueDoNotThrow() < 0) {
Logger.getLogger(this.getClass().getName()).log(Level.INFO, "1...");
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'RECIBO CONTABLE'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
            queryString += ",0";
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getIgss();  //IGSS
            queryString += ",'2342855'"; //IGSS
            queryString += ",'Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaPatronalIgss();
            queryString += ",'QUETZALES'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
            queryString += "," + (patronoalSegunIgss.getDoubleValueDoNotThrow() - totalIgssPatronal);
            queryString += ",0.00"; //HAber
            queryString += "," + (patronoalSegunIgss.getDoubleValueDoNotThrow() - totalIgssPatronal);
            queryString += ",0.00"; //HABER Q
            queryString += ",1.0"; //tipo cambio
            queryString += ",0"; //SALDO
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // idliquidador
            queryString += ",'RECIBO CONTABLE IGSS " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'NO'"; //REFRENCIA NO ISR
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += ")";
        }

        // ajuste por montos calculados por IGSS PATRONAL 2
        if (totalIgssPatronal - patronoalSegunIgss.getDoubleValueDoNotThrow() > 0) {
Logger.getLogger(this.getClass().getName()).log(Level.INFO, "2...");
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'RECIBO CONTABLE'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
            queryString += ",0";
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getIgss();  //IGSS
            queryString += ",'2342855'"; //IGSS
            queryString += ",'Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaPatronalIgss();
            queryString += ",'QUETZALES'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
            queryString += ",0.00"; //Debe
            queryString += "," + (totalIgssPatronal - patronoalSegunIgss.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //Debe Q
            queryString += "," + (totalIgssPatronal - patronoalSegunIgss.getDoubleValueDoNotThrow());
            queryString += ",1.0"; //tipo cambio
            queryString += ",0"; //SALDO
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // idliquidador
            queryString += ",'RECIBO CONTABLE IGSS " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'NO'"; //REFRENCIA NO ISR
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += ")";
        }

        // ajuste por montos calculados por IGSS LABORAL
        if (totalIgssLaboral - laboralSegunIgss.getDoubleValueDoNotThrow() < 0) {
Logger.getLogger(this.getClass().getName()).log(Level.INFO, "3...");
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'RECIBO CONTABLE'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
            queryString += ",0";
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getIgss();  //IGSS
            queryString += ",'2342855'"; //IGSS
            queryString += ",'Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaPatronalIgss();
            queryString += ",'QUETZALES'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
            queryString += "," + (laboralSegunIgss.getDoubleValueDoNotThrow() - totalIgssLaboral);
            queryString += ",0.00"; //HAber
            queryString += "," + (laboralSegunIgss.getDoubleValueDoNotThrow() - totalIgssLaboral );
            queryString += ",0.00"; //HABER Q
            queryString += ",1.0"; //tipo cambio
            queryString += ",0"; //SALDO
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // idliquidador
            queryString += ",'RECIBO CONTABLE IGSS " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'NO'"; //REFRENCIA NO ISR
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += ")";
        }

        // ajuste por montos calculados por IGSS LABORAL 2
        if (totalIgssLaboral - laboralSegunIgss.getDoubleValueDoNotThrow() > 0) {
Logger.getLogger(this.getClass().getName()).log(Level.INFO, "4...");
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'RECIBO CONTABLE'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
            queryString += ",0";
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getIgss();  //IGSS
            queryString += ",'2342855'"; //IGSS
            queryString += ",'Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaPatronalIgss();
            queryString += ",'QUETZALES'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
            queryString += ",0.00"; //Debe
            queryString += "," + (totalIgssLaboral - laboralSegunIgss.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //Debe Q
            queryString += "," + (totalIgssLaboral - laboralSegunIgss.getDoubleValueDoNotThrow());
            queryString += ",1.0"; //tipo cambio
            queryString += ",0"; //SALDO
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // idliquidador
            queryString += ",'RECIBO CONTABLE IGSS " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'NO'"; //REFRENCIA NO ISR
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += ")";
        }

        //multas y rectificaciones
        if (multaTxt.getDoubleValueDoNotThrow() > 0 && multaTxt.getValue() != null) {

Logger.getLogger(this.getClass().getName()).log(Level.INFO, "5...");
            queryString += " ,(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'RECIBO CONTABLE'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
            queryString += ",0";
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getIgss();  //IGSS
            queryString += ",'2342855'"; //IGSS
            queryString += ",'Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getMultasYRectificaciones(); // multas y rectificaciones
            queryString += ",'QUETZALES'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
            queryString += "," + multaTxt.getDoubleValueDoNotThrow(); //DEBE
            queryString += ",0"; //HABER, iva credito, no se paga
            queryString += "," + multaTxt.getDoubleValueDoNotThrow(); //DEBE Q
            queryString += ",0";
            queryString += ",1.0";
            queryString += ",0"; //SALDO
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // SAT idliquidador
            queryString += ",'FORMULARIO IGSS MULTA" + serieTxt.getValue() + " " + numeroTxt.getValue() + " Instituto Guatemalteco de Seguridad Social'";
            queryString += ",'NO'"; //REFRENCIA NO ISR
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += ")";

        }

       Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Query insert IGSS POR DECLARAR " + queryString);

        try {

            if(insertandoPartida) {
                Notification notif = new Notification("RECIBO CONTABLE ESTA REGISTRADO !",
                        Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.CHECK);
                notif.show(Page.getCurrent());
                return;
            }

            insertandoPartida = true;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            insertandoPartida = false;

            if (igssPatronalContainer.size() > 0) {
                //CICLO DE IGSS PATRONAL X PAGAR
                queryString = " UPDATE contabilidad_partida Set PagadoIva = 'SI' WHERE CODIGOCC IN (";
                for (Object itemId : igssPatronalContainer.getItemIds()) {
                    if (igssPatronalDetailGrid.isSelected(itemId)) {
                        queryString += "'" + igssPatronalContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "',";
                    }
                }
                queryString = queryString.substring(0, queryString.length() - 1) + ")";
                queryString += " AND TIPODOCUMENTO IN (";
                for (Object itemId : igssPatronalContainer.getItemIds()) {
                    if (igssPatronalDetailGrid.isSelected(itemId)) {
                        queryString += "'" + igssPatronalContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).getValue().toString() + "',";
                    }
                }
                queryString = queryString.substring(0, queryString.length() - 1) + ")";
                queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaPatronalIgssPorPagar();
                queryString += " AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
                Logger.getLogger(this.getClass().getName()).log(Level.INFO,"UPDATE PAGADOIVA IGSS PATRONAL POR PAGAR = SI QUERY : " + queryString);
                stQuery.executeUpdate(queryString);

            }

            if (igssLaboralContainer.size() > 0) {
                //CICLO DE IGSS LABORAL X PAGAR
                queryString = " UPDATE contabilidad_partida Set PagadoIva = 'SI' WHERE CODIGOCC IN (";
                for (Object itemId : igssLaboralContainer.getItemIds()) {
                    if (igssLaboralDetailGrid.isSelected(itemId)) {
                        queryString += "'" + igssLaboralContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "',";
                    }
                }
                queryString = queryString.substring(0, queryString.length() - 1) + ")";
                queryString += " AND TIPODOCUMENTO IN (";
                for (Object itemId : igssLaboralContainer.getItemIds()) {
                    if (igssLaboralDetailGrid.isSelected(itemId)) {
                        queryString += "'" + igssLaboralContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).getValue().toString() + "',";
                    }
                }
                queryString = queryString.substring(0, queryString.length() - 1) + ")";
                queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCuotaLaboralIgssPorPagar();
                queryString += " AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
                Logger.getLogger(this.getClass().getName()).log(Level.INFO,"UPDATE PAGADOIVA IGSS LABORAL POR PAGAR = SI QUERY : " + queryString);
                stQuery.executeUpdate(queryString);
            }

            Notification notif = new Notification("RECIBO CONTABLE REGISTRADO EXITOSAMENTE!",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            ((IngresoDocumentosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(String.valueOf(empresaCbx.getValue()), 0);

            close();

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,"Error al insertar RECIBO CONTABLE DEL IGSS  : " + ex1.getMessage());
            ex1.printStackTrace();

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage(), ex2);
            }

        }
   }
}
