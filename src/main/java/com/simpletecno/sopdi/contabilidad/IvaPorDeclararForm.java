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

public class IvaPorDeclararForm extends Window {

    UI mainUI;

    DateField finDt;
    Button buscarBtn;
    CheckBox incluirResumenChk;

    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords, rsRecords1, rsRecords2;

    String queryString;

    VerticalLayout mainLayout;

    Grid ivaComprasDetailGrid;
    Grid ivaVentasDetailGrid;
    Grid ivaFooterGrid;

    Grid.FooterRow ivaComprasFooter;
    Grid.FooterRow ivaVentasFooter;

    public IndexedContainer ivaHeaderContainer = new IndexedContainer();
    public IndexedContainer ivaComprasContainer = new IndexedContainer();
    public IndexedContainer ivaVentasContainer = new IndexedContainer();

    static final String CODIGO_PROPERTY = "CodigoCC";
    static final String TIPO_DOCUMENTO_PROPERTY = "Tipo Documento";
    static final String NO_DOCUMENTO_PROPERTY = "Documento";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String CLIENTE_PROPERTY = "Cliente";
    static final String FECHA_PROPERTY = "Fecha";
    static final String MONTO_PROPERTY = "MONTO";
    static final String MONTOSF_PROPERTY = "MontoSF";
    static final String CODIGOPARTIDA_PROPERTY = "CodigoPartida";

    static final String TIPOIVA_PROPERTY = "RUBRO";

    boolean ivaCredito = false;

    Label pagarSiNoLbl;
    TextField serieTxt;
    TextField numeroTxt;
    DateField fechaFormularioDt;
    NumberField montoTxt;
    NumberField multaTxt;
    Button crearPartidaBtn;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("######0.00");

    double ivaDiferencia = 0.00;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public IvaPorDeclararForm() {
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

        finDt = new DateField("FECHAS DE FACTURAS AL : ");
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
                fillGrids();
            }
        });

        incluirResumenChk = new CheckBox("Incluir Resumen");
        incluirResumenChk.addValueChangeListener((event) -> {
            setTotalIva();
        });

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " IVA POR DECLARAR");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(finDt);
        layoutTitle.setComponentAlignment(finDt, Alignment.MIDDLE_CENTER);
        layoutTitle.addComponent(buscarBtn);
        layoutTitle.setComponentAlignment(buscarBtn, Alignment.MIDDLE_CENTER);
        layoutTitle.addComponent(incluirResumenChk);
        layoutTitle.setComponentAlignment(incluirResumenChk, Alignment.MIDDLE_CENTER);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        createGridDetails();
        createGridIvaFooter();

//        fillGrids();
    }

    public void createGridDetails() {

        HorizontalLayout facturasYPartidasLayout = new HorizontalLayout();
        facturasYPartidasLayout.setWidth("100%");
        facturasYPartidasLayout.addStyleName("rcorners3");
        facturasYPartidasLayout.setSpacing(true);

        ivaComprasContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        ivaComprasContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        ivaComprasContainer.addContainerProperty(NO_DOCUMENTO_PROPERTY, String.class, null);
        ivaComprasContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        ivaComprasContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        ivaComprasContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        ivaComprasContainer.addContainerProperty(MONTOSF_PROPERTY, String.class, null);
        ivaComprasContainer.addContainerProperty(CODIGOPARTIDA_PROPERTY, String.class, null);

        ivaComprasDetailGrid = new Grid("IVA POR COBRAR", ivaComprasContainer);
        ivaComprasDetailGrid.setWidth("100%");
        ivaComprasDetailGrid.setImmediate(true);
        ivaComprasDetailGrid.setHeightMode(HeightMode.ROW);
        ivaComprasDetailGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        ivaComprasDetailGrid.setHeightByRows(10);
        ivaComprasDetailGrid.setResponsive(true);
        ivaComprasDetailGrid.setEditorBuffered(false);
        ivaComprasDetailGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (ivaComprasDetailGrid.getSelectedRows() != null) {
                    setTotalIva();
                }
            }
        });

        ivaComprasDetailGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        ivaComprasDetailGrid.getColumn(MONTOSF_PROPERTY).setHidable(true).setHidden(true);
        ivaComprasDetailGrid.getColumn(CODIGO_PROPERTY).setHidable(true).setHidden(true);

        ivaComprasFooter = ivaComprasDetailGrid.appendFooterRow();
        ivaComprasFooter.getCell(PROVEEDOR_PROPERTY).setText("Total : ");
        ivaComprasFooter.getCell(PROVEEDOR_PROPERTY).setStyleName("rightalign");
        ivaComprasFooter.getCell(MONTO_PROPERTY).setText("0.00");
        ivaComprasFooter.getCell(MONTO_PROPERTY).setStyleName("rightalign");

        ivaVentasContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        ivaVentasContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        ivaVentasContainer.addContainerProperty(NO_DOCUMENTO_PROPERTY, String.class, null);
        ivaVentasContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        ivaVentasContainer.addContainerProperty(CLIENTE_PROPERTY, String.class, null);
        ivaVentasContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        ivaVentasContainer.addContainerProperty(MONTOSF_PROPERTY, String.class, null);
        ivaVentasContainer.addContainerProperty(CODIGOPARTIDA_PROPERTY, String.class, null);

        ivaVentasDetailGrid = new Grid("IVA POR PAGAR", ivaVentasContainer);
        ivaVentasDetailGrid.setWidth("100%");
        ivaVentasDetailGrid.setImmediate(true);
        ivaVentasDetailGrid.setHeightMode(HeightMode.ROW);
        ivaVentasDetailGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        ivaVentasDetailGrid.setHeightByRows(10);
        ivaVentasDetailGrid.setResponsive(true);
        ivaVentasDetailGrid.setEditorBuffered(false);
        ivaVentasDetailGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (ivaVentasDetailGrid.getSelectedRows() != null) {
                    setTotalIva();
                }
            }
        });

        ivaVentasDetailGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        ivaVentasDetailGrid.getColumn(CODIGO_PROPERTY).setHidable(true).setHidden(true);
        ivaVentasDetailGrid.getColumn(MONTOSF_PROPERTY).setHidable(true).setHidden(true);

        ivaVentasFooter = ivaVentasDetailGrid.appendFooterRow();
        ivaVentasFooter.getCell(CLIENTE_PROPERTY).setText("Total : ");
        ivaVentasFooter.getCell(CLIENTE_PROPERTY).setStyleName("rightalign");
        ivaVentasFooter.getCell(MONTO_PROPERTY).setText("0.00");
        ivaVentasFooter.getCell(MONTO_PROPERTY).setStyleName("rightalign");

        facturasYPartidasLayout.addComponent(ivaComprasDetailGrid);
        facturasYPartidasLayout.addComponent(ivaVentasDetailGrid);

        mainLayout.addComponent(facturasYPartidasLayout);
        mainLayout.setComponentAlignment(facturasYPartidasLayout, Alignment.MIDDLE_CENTER);

    }

    public void createGridIvaFooter() {

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setSpacing(true);
        footerLayout.setWidth(("100%"));
        footerLayout.addStyleName("rcorners2");

        fechaFormularioDt = new DateField("Fecha formulario :");
        fechaFormularioDt.setDateFormat("dd/MM/yyyy");
        fechaFormularioDt.setValue(new java.util.Date());
        fechaFormularioDt.setWidth("8em");

        serieTxt = new TextField("Serie formulario : ");
        serieTxt.setWidth("8em");
        serieTxt.addStyleName("mayusculas");
        serieTxt.setValue("2237");

        numeroTxt = new TextField("Número formulario : ");
        numeroTxt.setWidth("8em");

        montoTxt = new NumberField("Monto formulario : ");
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
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de registrar el Formulario IVA con esta fecha : " + Utileria.getFechaDDMMYYYY(fechaFormularioDt.getValue()) + " ?",
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

        ivaHeaderContainer.addContainerProperty(TIPOIVA_PROPERTY, String.class, null);
        ivaHeaderContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);

        ivaFooterGrid = new Grid("IVA POR DECLARAR", ivaHeaderContainer);
        ivaFooterGrid.setSizeUndefined();
        ivaFooterGrid.setImmediate(true);
        ivaFooterGrid.setHeightMode(HeightMode.ROW);
        ivaFooterGrid.setHeightByRows(4);
        ivaFooterGrid.setResponsive(true);
        ivaFooterGrid.setEditorBuffered(false);
        ivaFooterGrid.setWidth("100%");

        ivaFooterGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        footerLayout.addComponent(ivaFooterGrid);
        footerLayout.setComponentAlignment(ivaFooterGrid, Alignment.MIDDLE_LEFT);

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

        pagarSiNoLbl = new Label("POR FAVOR LLENAR DATOS DEL FORMULARIO");
        pagarSiNoLbl.setSizeFull();
        pagarSiNoLbl.addStyleName(ValoTheme.LABEL_COLORED);

        footerLayout.addComponent(pagarSiNoLbl);
        footerLayout.addComponent(formularioForm);
        footerLayout.setComponentAlignment(formularioForm, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(footerLayout);
        mainLayout.setComponentAlignment(footerLayout, Alignment.TOP_CENTER);
    }

    public void fillGrids() {
        fillDetailsGrid();
    }

    public void fillDetailsGrid() {
        ivaComprasContainer.removeAllItems();
        ivaVentasContainer.removeAllItems();
        ivaFooterGrid.getContainerDataSource().removeAllItems();

        ivaComprasFooter.getCell(MONTO_PROPERTY).setText("0.00");
        ivaVentasFooter.getCell(MONTO_PROPERTY).setText("0.00");
        ivaComprasFooter.getCell(MONTO_PROPERTY).setText("0.00");
        ivaVentasFooter.getCell(MONTO_PROPERTY).setText("0.00");

        double totalIvaPorCobrar = 0.00, totalIvaPorPagar = 0.00;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            //buscar el ultimo formulario....para obtener el IVA REMANENTE, que debe estar en 7 DEBE.
            queryString = "SELECT CodigoCC, TipoDocumento, Fecha, SerieDocumento, NumeroDocumento, NombreProveedor, ";
            queryString += "DebeQuetzales, HaberQuetzales, CodigoPartida ";
            queryString += " FROM contabilidad_partida";
            queryString += " WHERE  IdEmpresa = " + empresaId;
            queryString += " AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
            queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar();  //iva por cobrar
            queryString += " AND DEBE > 0"; // solo las lineas cuenta por cobrar
            queryString += " AND TipoDocumento = 'FORMULARIO IVA'";
            queryString += " AND PagadoIva = 'NO'";
            queryString += " AND UPPER(Estatus) <> 'ANULADO'";
            queryString += " AND CodigoCC = CodigoPartida";  //para que solamente traiga el remanente y no alguna nota de credito...

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                Object itemId = ivaComprasContainer.addItem();
                ivaComprasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                ivaComprasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                ivaComprasContainer.getContainerProperty(itemId, NO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                ivaComprasContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                ivaComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                ivaComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("DebeQuetzales")));
                ivaComprasContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(numberFormat2.format(rsRecords.getDouble("DebeQuetzales")));
                ivaComprasContainer.getContainerProperty(itemId, CODIGOPARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));

                totalIvaPorCobrar += rsRecords.getDouble("DebeQuetzales");

                ivaComprasDetailGrid.select(itemId);
            }

            queryString = "SELECT CodigoCC, TipoDocumento, IdNomenclatura, Fecha, SerieDocumento, NumeroDocumento, ";
            queryString += " DebeQuetzales, HaberQuetzales, NombreProveedor, CodigoPartida ";
            queryString += " FROM contabilidad_partida";
            queryString += " WHERE  IdEmpresa = " + empresaId;
            queryString += " AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
            queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar();  //iva por cobrar
            queryString += " AND TipoDocumento IN ('FACTURA', 'NOTA DE CREDITO COMPRA')";
            queryString += " AND PagadoIVa = 'NO'";
            queryString += " AND UPPER(Estatus) <> 'ANULADO'";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                do {

                    double ivaSaldo = (rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"));

                    Object itemId = ivaComprasContainer.addItem();
                    ivaComprasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    ivaComprasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    ivaComprasContainer.getContainerProperty(itemId, NO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                    ivaComprasContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    ivaComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    ivaComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(ivaSaldo));
                    ivaComprasContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(numberFormat2.format(ivaSaldo));
                    ivaComprasContainer.getContainerProperty(itemId, CODIGOPARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    ivaComprasDetailGrid.select(itemId);
                    totalIvaPorCobrar += ivaSaldo;
                } while (rsRecords.next());

                ivaComprasFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalIvaPorCobrar));
            }

            queryString = "SELECT CodigoCC, TipoDocumento, IdNomenclatura, Fecha, SerieDocumento, NumeroDocumento, ";
            queryString += " Prov.Nombre ProveedorNombre, DebeQuetzales, HaberQuetzales, CodigoPartida ";
            queryString += " FROM contabilidad_partida ";
            queryString += " INNER JOIN proveedor_empresa Prov On Prov.IdProveedor = contabilidad_partida.IdProveedor";
            queryString += " WHERE  contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
            queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar(); // iva por pagar
            queryString += " AND TipoDocumento IN ('FACTURA VENTA', 'NOTA DE CREDITO COMPRA', 'CONSTANCIA RETENCION IVA')";
            queryString += " AND PagadoIVa = 'NO'";
            queryString += " AND Prv.IdEmpresa = " + empresaId;
            queryString += " AND UPPER(Estatus) <> 'ANULADO'";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    double ivaSaldo = (rsRecords.getDouble("HaberQuetzales") - rsRecords.getDouble("DebeQuetzales"));

                    Object itemId = ivaVentasContainer.addItem();
                    ivaVentasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    ivaVentasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    ivaVentasContainer.getContainerProperty(itemId, NO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                    ivaVentasContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    ivaVentasContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(rsRecords.getString("ProveedorNombre"));
                    ivaVentasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(ivaSaldo));
                    ivaVentasContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(numberFormat2.format(ivaSaldo));
                    ivaVentasContainer.getContainerProperty(itemId, CODIGOPARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    ivaVentasDetailGrid.select(itemId);

                    totalIvaPorPagar += ivaSaldo;
                } while (rsRecords.next());

                ivaVentasFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalIvaPorPagar));
            }

            ivaDiferencia = totalIvaPorCobrar - totalIvaPorPagar;

            setTotalIva();

            serieTxt.setReadOnly(false);
            serieTxt.setValue("2237");
            numeroTxt.setReadOnly(false);
            numeroTxt.setValue("");
            montoTxt.setReadOnly(false);
            montoTxt.setValue(Double.valueOf(numberFormat2.format(ivaDiferencia)).doubleValue());
            fechaFormularioDt.setReadOnly(false);
            crearPartidaBtn.setVisible(true);
            pagarSiNoLbl.setValue("POR FAVOR LLENAR DATOS DEL FORMULARIO");

        } catch (Exception ex) {
            System.out.println("Error al listar tabla facturas: " + ex);
            ex.printStackTrace();
        }
    }

    private void setTotalIva() {
        double totalIvaPorCobrar = 0.00, totalIvaPorPagar = 0.00;
        for (Object itemId : ivaComprasContainer.getItemIds()) {
            if (ivaComprasDetailGrid.isSelected(itemId)) {
                totalIvaPorCobrar += Double.valueOf(ivaComprasContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).getValue().toString());
            }
        }
        for (Object itemId : ivaVentasContainer.getItemIds()) {
            if (ivaVentasDetailGrid.isSelected(itemId)) {
                totalIvaPorPagar += Double.valueOf(ivaVentasContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).getValue().toString());
            }
        }
        ivaComprasFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalIvaPorCobrar));
        ivaVentasFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalIvaPorPagar));

        ivaDiferencia = totalIvaPorCobrar - totalIvaPorPagar;

        ivaHeaderContainer.removeAllItems();

        Object itemId = ivaHeaderContainer.addItem();
        ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("IVA COMPRAS");
        ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalIvaPorCobrar));

        itemId = ivaHeaderContainer.addItem();
        ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("IVA VENTAS");
        ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalIvaPorPagar));

        itemId = ivaHeaderContainer.addItem();
        ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("---------------");
        ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("---------------");

        itemId = ivaHeaderContainer.addItem();
        if (ivaDiferencia > 0.00) {  // no hay que pagar
            ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("IVA CREDITO");
            ivaCredito = true;
        } else {  //hay que pagar
            ivaDiferencia = ivaDiferencia * -1;
            ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("IVA DEBITO");
            ivaCredito = false;
        }
        ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(ivaDiferencia));

        montoTxt.setReadOnly(false);
        montoTxt.setValue(Double.valueOf(numberFormat2.format(ivaDiferencia)).doubleValue());

        if (incluirResumenChk.getValue()) {
            try {

                BigDecimal totalPeqCont = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalNoAfecto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalCompra = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalServicio = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalIva = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

                stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                for (Object ivaItemId : ivaComprasContainer.getItemIds()) {

                    if (ivaComprasDetailGrid.isSelected(ivaItemId)) {
                        queryString = " SELECT contabilidad_partida.TipoDocumento, contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
                        queryString += " contabilidad_partida.CodigoPartida,  contabilidad_nomenclatura_empresa.NoCuenta,";
                        queryString += " contabilidad_partida.NitProveedor, contabilidad_partida.NombreProveedor, ";
                        queryString += " IFNULL(proveedor.Regimen, 'SINREGIMEN') PROV_REGIMEN,";
                        queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales,";
                        queryString += " contabilidad_partida.Fecha, contabilidad_nomenclatura_empresa.IdNomenclatura, contabilidad_nomenclatura_empresa.Tipo ";
                        queryString += " FROM contabilidad_partida ";
                        queryString += " INNER JOIN contabilidad_nomenclatura_empresa ON contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura ";
                        queryString += " LEFT JOIN proveedor_empresa ON proveedor_empresa.IdProveedor = contabilidad_partida.IdProveedor";
                        queryString += " WHERE contabilidad_partida.IdEmpresa = " + empresaId;
                        queryString += " AND contabilidad_partida.CodigoPartida = '" + ivaComprasContainer.getContainerProperty(ivaItemId, CODIGOPARTIDA_PROPERTY).getValue().toString() + "'";
                        queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;
                        queryString += " ANd contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;

                        rsRecords2 = stQuery2.executeQuery(queryString);

                        if (rsRecords2.next()) { //  encontrado
                            String codigoPartidaAnterior = "";
                            String regimen = "";

                            do {

                                regimen = rsRecords2.getString("PROV_REGIMEN");

                                switch (rsRecords2.getString("Tipo").toUpperCase()) {
                                    case "NO AFECTO":
                                        totalNoAfecto = totalNoAfecto.add(new BigDecimal((rsRecords2.getDouble("DebeQuetzales") - rsRecords2.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        break;
                                    case "COMPRA":
                                    case "VENTA":
                                        if (regimen.equals("PEQUEÑO CONTRIBUYENTE")) {
                                            totalPeqCont = totalPeqCont.add(new BigDecimal((rsRecords2.getDouble("DebeQuetzales") - rsRecords2.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        } else {
                                            totalCompra = totalCompra.add(new BigDecimal((rsRecords2.getDouble("DebeQuetzales") - rsRecords2.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        }
                                        break;
                                    case "SERVICIO":
                                        if (regimen.equals("PEQUEÑO CONTRIBUYENTE")) {
                                            totalPeqCont = totalPeqCont.add(new BigDecimal((rsRecords2.getDouble("DebeQuetzales") - rsRecords2.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        } else {
                                            totalServicio = totalServicio.add(new BigDecimal((rsRecords2.getDouble("DebeQuetzales") - rsRecords2.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        }
                                        break;
                                    case "IVA":
                                        totalIva = totalIva.add(new BigDecimal((rsRecords2.getDouble("DebeQuetzales") - rsRecords2.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        break;
                                    default:
                                        break;
                                }
                            } while (rsRecords2.next());
                        }//endif record found
                    }
                } //end for

                itemId = ivaHeaderContainer.addItem();
                ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("---------------");
                ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("---------------");
                itemId = ivaHeaderContainer.addItem();
                ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR COBRAR --> PEQ CONT");
                ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalPeqCont));
                itemId = ivaHeaderContainer.addItem();
                ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR COBRAR --> NO AFECTO");
                ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalNoAfecto));
                itemId = ivaHeaderContainer.addItem();
                ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR COBRAR --> COMPRA");
                ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalCompra));
                itemId = ivaHeaderContainer.addItem();
                ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR COBRAR --> SERVICIO");
                ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalServicio));
                itemId = ivaHeaderContainer.addItem();
                ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR COBRAR --> IMPORTACION");
                ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("0.00");

                if (ivaVentasContainer.size() > 0) {

                    //TODO :QUERY Y CICLO
                    totalPeqCont = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                    totalNoAfecto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                    totalCompra = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                    totalServicio = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

                    itemId = ivaHeaderContainer.addItem();
                    ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("---------------");
                    ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("---------------");
                    itemId = ivaHeaderContainer.addItem();
                    ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR PAGAR --> PEQ CONT");
                    ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalPeqCont));
                    itemId = ivaHeaderContainer.addItem();
                    ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR PAGAR --> NO AFECTO");
                    ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalNoAfecto));
                    itemId = ivaHeaderContainer.addItem();
                    ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR PAGAR --> COMPRA");
                    ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalCompra));
                    itemId = ivaHeaderContainer.addItem();
                    ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR PAGAR --> SERVICIO");
                    ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalServicio));
                    itemId = ivaHeaderContainer.addItem();
                    ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("POR PAGAR --> IMPORTACION");
                    ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("0.00");
                    itemId = ivaHeaderContainer.addItem();
                    ivaHeaderContainer.getContainerProperty(itemId, TIPOIVA_PROPERTY).setValue("---------------");
                    ivaHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("---------------");
                }

            } catch (Exception ex) {
                System.out.println("Error al listar TABLA DE IVAS:" + ex);
                ex.printStackTrace();
            }
        }//if checkbox
    }

    public void insertPartidas() {
        if (((SopdiUI) UI.getCurrent()).esMesCerrado(empresaId, Utileria.getFechaYYYYMMDD_1(new java.util.Date()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(empresaId, Utileria.getFechaYYYYMMDD_1(new java.util.Date()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(empresaId), Notification.Type.WARNING_MESSAGE);
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

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartida = empresaId + año + mes + dia + "1";

        queryString = " SELECT codigoPartida FROM contabilidad_partida ";
        queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
        queryString += " ORDER BY codigoPartida DESC ";

        try {
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);
        } catch (java.sql.SQLException sqlE) {
            //
        }

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
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                //
            }
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " SELECT * FROM contabilidad_partida";
        queryString += " WHERE SerieDocumento  = '" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += " AND NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND TipoDocumento = 'FORMULARIO IVA'";
        queryString += " AND MonedaDocumento = 'QUETZALES'";

        try {
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                try {
                    ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                    ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
                } catch (java.sql.SQLException sqlE) {
                    //
                }
                Notification.show("Este documento ya fué ingresado, revise.!.", Notification.Type.WARNING_MESSAGE);
                numeroTxt.focus();
                return;
            }
        } catch (Exception ex1) {
            System.out.println("Error al buscar documento : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " INSERT INTO proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha, ";
        queryString += " TipoDocumento, SerieDocumento, NumeroDocumento, MonedaDocumento, ";
        queryString += " Monto, MontoQuetzales, TipoCambio, ";
        queryString += " IdUsuarioAutorizoPago,CreadoFechayHora,CreadoUsuario)";
        queryString += " VALUES(";
        queryString += empresaId;
        queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); //SAT
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
        queryString += ",'FORMULARIO IVA'";
        queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += ",'QUETZALES'";
        queryString += "," + ivaDiferencia; //monto
        queryString += "," + ivaDiferencia; //monto quetzales
        queryString += ", 1.0"; // tipo cambiio
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
        } catch (Exception ex1) {
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                //
            }
            System.out.println("Error al insertar proveedor cuentaCorriente : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        /// Ingreso del haber o el debe
        queryString = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, Fecha, IdOrdenCompra, IdProveedor, NITProveedor, NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, IdLiquidador, Descripcion, Referencia,";
        queryString += " CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre)";
        queryString += " VALUES ";
        queryString += " (";
        queryString += empresaId;
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoPartida + "'"; //codigoCC
        queryString += ",'FORMULARIO IVA'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
        queryString += ",0";
        queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat();  //SAT
        queryString += ",'1669394-9'"; //SAT
        queryString += ",'Superintendencia de Administracion Tributaria'";
        queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        if (ivaCredito) {
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar(); // instituciones x pagar IVA REMANENTE
        } else {
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones(); // instituciones x pagar
        }
        queryString += ",'QUETZALES'";
        queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
        if (ivaCredito) {  //iva credito,no hay que pagar
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //DEBE
            queryString += ",0"; //HABER, iva credito, no se paga
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //DEBE Q
            queryString += ",0"; //HABERQ, iva credito, no se paga
        } else { //hay que pagar
            queryString += ",0"; //debe
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //HABER
            queryString += ",0"; //debe Q
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //HABERQ
        }
        queryString += ",1.0"; //tipo cambio
        if (ivaCredito) {  //iva credito
            queryString += ",0"; //SALDO
        } else {
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //SALDO
        }
        queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // SAT idliquidador
        queryString += ",'FORMULARIO IVA " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
        queryString += ",'NO'"; //REFRENCIA NO ISR
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ",null";
        queryString += ",null";
        queryString += ",0";
        queryString += ",null";
        queryString += ")";

        //CICLO DE IVAS X COBRAR
        for (Object itemId : ivaComprasContainer.getItemIds()) {
            if (ivaComprasDetailGrid.isSelected(itemId)) {
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + ivaComprasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "'"; //codigoCC
                queryString += ",'FORMULARIO IVA'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
                queryString += ",0";
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); //sat
                queryString += ",'1669394-9'"; //sat
                queryString += ",'Superintendencia de Administracion Tributaria'";
                queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += ", " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar(); //iva x cobrar
                queryString += ",'QUETZALES'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
                if (!ivaComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().contains("-")) {
                    queryString += ",0.00"; // DEBE
                    queryString += "," + ivaComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "");
                    queryString += ",0.00"; // DEBE Q
                    queryString += "," + ivaComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "");

                } else {
                    queryString += "," + ivaComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "").replaceAll("-", "");
                    queryString += ",0.00"; // HABER
                    queryString += "," + ivaComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "").replaceAll("-", "");
                    queryString += ",0.00"; // HABER Q
                }
                queryString += ",1.00"; // tipo cambio
                queryString += ",0.00"; // saldo
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); //SAT
                queryString += ",'FORMULARIO IVA " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
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

        //CICLO DE IVAS X PAGAR
        for (Object itemId : ivaVentasContainer.getItemIds()) {
            if (ivaVentasDetailGrid.isSelected(itemId)) {
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + ivaVentasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "'"; //codigoCC
                queryString += ",'FORMULARIO IVA'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
                queryString += ",0";
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); //SAT
                queryString += ",'1669394-9'";
                queryString += ",'Superintendencia de Administracion Tributaria'";
                queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar(); // iva x pagar
                queryString += ",'QUETZALES'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
                if (!ivaVentasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().contains("-")) {
                    queryString += "," + ivaVentasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "");
                    queryString += ",0.00"; // HABER
                    queryString += "," + ivaVentasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "");
                    queryString += ",0.00"; // HABER Q
                } else {
                    queryString += ",0.00"; // DEBE
                    queryString += "," + ivaVentasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "").replaceAll("-", "");
                    queryString += ",0.00"; // DEBE Q
                    queryString += "," + ivaVentasContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "").replaceAll("-", "");
                }
                queryString += ",1.00"; // tipo cambio
                queryString += ",0.00"; // saldo
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); //SAT
                queryString += ",'FORMULARIO IVA " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
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

//        //REGULARIZAR EL IVA
//        if(ivaCredito) {  //iva credito
//            queryString += ",(";
//            queryString += empresaCbx.getValue();
//            queryString += ",'INGRESADO'";
//            queryString += ",'" + codigoPartida + "'";
//            queryString += ",'" + codigoPartida + "'"; //codigoCC
//            queryString += ",'FORMULARIO IVA'";
//            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
//            queryString += ",0";
//            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat();
//            queryString += ",'1669394-9'";
//            queryString += ",'Superintendencia de Administracion Tributaria'";
//            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
//            queryString += ",'" + numeroTxt.getValue().trim() + "'";
//            queryString += ",7"; // iva x cobrar
//            queryString += ",'QUETZALES'";
//            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
//            queryString += "," + ivaDiferencia;
//            queryString += ",0.00"; // HABER
//            queryString += "," + ivaDiferencia;
//            queryString += ",0.00"; // HABER Q
//            queryString += ",1.00"; // tipo cambio
//            queryString += ",0.00"; // saldo
//            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat();
//            queryString += ",'FORMULARIO IVA " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
//            queryString += ",'NO'"; //REFRENCIA NO ISR
//            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
//            queryString += ",current_timestamp";
//            queryString += ",null";
//            queryString += ",null";
//            queryString += ",0";
//            queryString += ",null";
//            queryString += ")";
//        }
//// aqui tiene que ir el insert de multas 
        // redondeo
        if (montoTxt.getDoubleValueDoNotThrow() > 0 && (montoTxt.getDoubleValueDoNotThrow() != ivaDiferencia)) {
            queryString += ",(";
            queryString += empresaNombre;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'FORMULARIO IVA'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
            queryString += ",0";
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat();  //sat
            queryString += ",'1669394-9'"; //sat
            queryString += ",'Superintendencia de Administracion Tributaria'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getRedondeo(); // redeondeo
            queryString += ",'QUETZALES'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
            if (ivaCredito) {  //iva credito,no hay que pagar, instituciones en el DEBE
                if (ivaDiferencia - montoTxt.getDoubleValueDoNotThrow() > 0) { //diferencia positiva
                    queryString += "," + (ivaDiferencia - montoTxt.getDoubleValueDoNotThrow()); // DEBE monto del redondeo
                    queryString += ",0.00"; //HAber
                    queryString += "," + (ivaDiferencia - montoTxt.getDoubleValueDoNotThrow()); /// DEBE Q
                    queryString += ",0.00"; //HABER Q
                } else {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + ((ivaDiferencia - montoTxt.getDoubleValueDoNotThrow()) * -1); // HABER monto del redondeo
                    queryString += ",0.00"; //DEBE
                    queryString += "," + ((ivaDiferencia - montoTxt.getDoubleValueDoNotThrow()) * -1); // HABER monto del redondeo
                }
            } else { //iva debito, si hay que pagar, instituciones en el HABER
                if (ivaDiferencia - montoTxt.getDoubleValueDoNotThrow() > 0) { //diferencia positiva
                    queryString += ",0.00"; //DEBE
                    queryString += "," + (ivaDiferencia - montoTxt.getDoubleValueDoNotThrow()); // HABER monto del redondeo
                    queryString += ",0.00"; //DEBE Q
                    queryString += "," + (ivaDiferencia - montoTxt.getDoubleValueDoNotThrow()); // HABER Q monto del redondeo
                } else {
                    queryString += "," + ((ivaDiferencia - montoTxt.getDoubleValueDoNotThrow()) * -1); // DEBE monto del redondeo
                    queryString += ",0.00"; //HABER 
                    queryString += "," + ((ivaDiferencia - montoTxt.getDoubleValueDoNotThrow()) * -1); // DEBE monto del redondeo
                    queryString += ",0.00"; //HABER Q
                }
            }
            queryString += ",1.0"; //tipo cambio
            queryString += ",0"; //SALDO
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // idliquidador
            queryString += ",'FORMULARIO IVA " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
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

            queryString += " ,(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'FORMULARIO IVA'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
            queryString += ",0";
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat();  //SAT
            queryString += ",'1669394-9'"; //SAT
            queryString += ",'Superintendencia de Administracion Tributaria'";
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
            queryString += ",'FORMULARIO IVA MULTA" + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
            queryString += ",'NO'"; //REFRENCIA NO ISR
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += ")";

            if (ivaCredito) {
                queryString += " ,(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoPartida + "'"; //codigoCC
                queryString += ",'FORMULARIO IVA'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
                queryString += ",0";
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); //SAT
                queryString += ",'1669394-9'"; //SAT
                queryString += ",'Superintendencia de Administracion Tributaria'";
                queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones(); // multas y rectificaciones
                queryString += ",'QUETZALES'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento                
                queryString += ",0"; //DEBE, 
                queryString += "," + multaTxt.getDoubleValueDoNotThrow(); //HABER
                queryString += ",0";//DEBE
                queryString += "," + multaTxt.getDoubleValueDoNotThrow();//HABER
                queryString += ",1.0";
                queryString += ",0"; //SALDO
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // SAT idliquidador
                queryString += ",'FORMULARIO IVA MULTA" + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
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

        System.out.println("query insert IVA POR DECLARAR " + queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            if (multaTxt.getDoubleValueDoNotThrow() > 0 && multaTxt.getValue() != null) {

                if(!ivaCredito){
                    
                    queryString = " UPDATE contabilidad_partida ";
                    queryString += " SET MontoDocumento = " + (montoTxt.getDoubleValueDoNotThrow() + multaTxt.getDoubleValueDoNotThrow());
                    queryString += " , Haber = " + (montoTxt.getDoubleValueDoNotThrow() + multaTxt.getDoubleValueDoNotThrow());
                    queryString += " , HaberQuetzales = " + (montoTxt.getDoubleValueDoNotThrow() + multaTxt.getDoubleValueDoNotThrow());
                    queryString += " WHERE codigoPartida = '" + codigoPartida + "'";
                    queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones();
                    queryString += " AND IdEmpresa = " + empresaId;
                    
                    stQuery.executeUpdate(queryString);
                }
            }

            if (ivaComprasContainer.size() > 0) {
                //CICLO DE IVAS X COBRAR
                queryString = " UPDATE contabilidad_partida Set PagadoIva = 'SI' WHERE CODIGOCC IN (";
                for (Object itemId : ivaComprasContainer.getItemIds()) {
                    if (ivaComprasDetailGrid.isSelected(itemId)) {
                        queryString += "'" + ivaComprasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "',";
                    }
                }
                queryString  = queryString.substring(0, queryString.length() - 1) + ")";
                queryString += " AND TIPODOCUMENTO IN (";
                for (Object itemId : ivaComprasContainer.getItemIds()) {
                    if (ivaComprasDetailGrid.isSelected(itemId)) {
                        queryString += "'" + ivaComprasContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).getValue().toString() + "',";
                    }
                }
                queryString  = queryString.substring(0, queryString.length() - 1) + ")";
                queryString += " AND IdNomenclatura =  " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar();
                Logger.getLogger(this.getClass().getName()).log(Level.INFO,"UPDATE PAGADOIVA POR COBRAR = SI QUERY : " + queryString);
                stQuery.executeUpdate(queryString);

            }

            if (ivaVentasContainer.size() > 0) {
                //CICLO DE IVAS X PAGAR
                queryString = " UPDATE contabilidad_partida Set PagadoIva = 'SI' WHERE CODIGOCC IN (";
                for (Object itemId : ivaVentasContainer.getItemIds()) {
                    if (ivaVentasDetailGrid.isSelected(itemId)) {
                        queryString += "'" + ivaVentasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "',";
                    }
                }
                queryString = queryString.substring(0, queryString.length() - 1) + ")";
                queryString += " AND TIPODOCUMENTO IN ( ";
                for (Object itemId : ivaVentasContainer.getItemIds()) {
                    if (ivaVentasDetailGrid.isSelected(itemId)) {
                        queryString += "'" + ivaVentasContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).getValue().toString() + "',";
                    }
                }
                queryString  = queryString.substring(0, queryString.length() - 1) + ")";
                queryString += " AND IdNomenclatura =  " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar();
                Logger.getLogger(this.getClass().getName()).log(Level.INFO,"UPDATE PAGADOIVA POR PAGAR = SI QUERY : " + queryString);
                stQuery.executeUpdate(queryString);
            }

            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                //
            }
            Notification notif = new Notification("FORMULARIO DEL IVA REGISTRADO EXITOSAMENTE!",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            ((IngresoDocumentosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(empresaId, 0);

            close();

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,"Error al insertar FORMULARIO DEL IVA  : " + ex1.getMessage());
            ex1.printStackTrace();

            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                //
            }
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
