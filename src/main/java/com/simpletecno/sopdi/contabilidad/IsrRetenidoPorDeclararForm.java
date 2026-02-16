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
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IsrRetenidoPorDeclararForm extends Window {

    UI mainUI;

    ComboBox empresaCbx;

    DateField finDt;

    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;

    String queryString;

    VerticalLayout mainLayout;

    Grid isrRetenidoDetailGrid;
    Grid isrFooterGrid;

    Grid.FooterRow isrRetenidoFooter;

    public IndexedContainer isrHeaderContainer = new IndexedContainer();
    public IndexedContainer isrRetenidoContainer = new IndexedContainer();

    static final String CODIGO_PROPERTY = "CODIGOCC";
    static final String NO_DOCUMENTO_PROPERTY = "DOCUMENTO";
    static final String TIPO_DOCUMENTO_PROPERTY = "TIPO_DOCUMENTO";
    static final String PROVEEDOR_PROPERTY = "PROVEEDOR";
    static final String FECHA_PROPERTY = "FECHA";
    static final String MONTO_PROPERTY = "MONTO_ISR";
    static final String MONTOSF_PROPERTY = "MontoSF";

    TextField serieTxt;
    TextField numeroTxt;
    DateField fechaFormularioDt;
    NumberField montoTxt;
    NumberField multaTxt;
    Button crearPartidaBtn;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("######0.00");

    double totalISRPorPagar = 0.00;

    public IsrRetenidoPorDeclararForm() {
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

        finDt = new DateField("FECHAS DE FACTURAS AL : ");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("15em");
        finDt.addValueChangeListener(event -> {
                    fillGrids();
                }
        );

        Label titleLbl = new Label("ISR RETENIDO POR DECLARAR");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(finDt);
        layoutTitle.setComponentAlignment(finDt, Alignment.MIDDLE_CENTER);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        createGridDetails();
        createGridisrFooter();

        fillGrids();

    }

    public void llenarComboEmpresa() {

        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado
                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));
            }

            rsRecords1.first();

            empresaCbx.select(rsRecords1.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al llenar combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void createGridDetails() {

        HorizontalLayout facturasYPartidasLayout = new HorizontalLayout();
        facturasYPartidasLayout.setWidth("100%");
        facturasYPartidasLayout.addStyleName("rcorners3");
        facturasYPartidasLayout.setSpacing(true);

        isrRetenidoContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        isrRetenidoContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        isrRetenidoContainer.addContainerProperty(NO_DOCUMENTO_PROPERTY, String.class, null);
        isrRetenidoContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        isrRetenidoContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        isrRetenidoContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        isrRetenidoContainer.addContainerProperty(MONTOSF_PROPERTY, String.class, null);

        isrRetenidoDetailGrid = new Grid("ISR RETENIDO", isrRetenidoContainer);
        isrRetenidoDetailGrid.setWidth("100%");
        isrRetenidoDetailGrid.setImmediate(true);
        isrRetenidoDetailGrid.setHeightMode(HeightMode.ROW);
        isrRetenidoDetailGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        isrRetenidoDetailGrid.setHeightByRows(10);
        isrRetenidoDetailGrid.setResponsive(true);
        isrRetenidoDetailGrid.setEditorBuffered(false);
        isrRetenidoDetailGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (isrRetenidoDetailGrid.getSelectedRows() != null) {
                    setTotalisr();
                }
            }
        });

        isrRetenidoDetailGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        isrRetenidoDetailGrid.getColumn(MONTOSF_PROPERTY).setHidable(true).setHidden(true);
//        isrRetenidoDetailGrid.getColumn(CODIGO_PROPERTY).setHidable(true).setHidden(true);

        isrRetenidoFooter = isrRetenidoDetailGrid.appendFooterRow();
        isrRetenidoFooter.getCell(PROVEEDOR_PROPERTY).setText("Total : ");
        isrRetenidoFooter.getCell(PROVEEDOR_PROPERTY).setStyleName("rightalign");
        isrRetenidoFooter.getCell(MONTO_PROPERTY).setText("0.00");
        isrRetenidoFooter.getCell(MONTO_PROPERTY).setStyleName("rightalign");

        facturasYPartidasLayout.addComponent(isrRetenidoDetailGrid);

        mainLayout.addComponent(facturasYPartidasLayout);
        mainLayout.setComponentAlignment(facturasYPartidasLayout, Alignment.MIDDLE_CENTER);

    }

    public void createGridisrFooter() {

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setSpacing(true);
        footerLayout.setWidth(("100%"));
        footerLayout.addStyleName("rcorners2");

        fechaFormularioDt = new DateField("Fecha formulario :");
        fechaFormularioDt.setDateFormat("dd/MM/yyyy");
        fechaFormularioDt.setValue(new Date());
        fechaFormularioDt.setWidth("8em");

        serieTxt = new TextField("Serie formulario : ");
        serieTxt.setWidth("8em");
        serieTxt.addStyleName("mayusculas");
        serieTxt.setValue("1331");

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
//        montoTxt.addValueChangeListener(event -> {
//            if (cuentaContable1Cbx != null) {
//                verificarProveedor();
//            }
//        });

        crearPartidaBtn = new Button("REGISTRAR FORMULARIO");
        crearPartidaBtn.setIcon(FontAwesome.SAVE);
        crearPartidaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        crearPartidaBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de registrar el Formulario ISR con esta fecha : " + Utileria.getFechaDDMMYYYY(fechaFormularioDt.getValue()) + " ?",
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

        isrHeaderContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);

        isrFooterGrid = new Grid("ISR RETENIDO", isrHeaderContainer);
        isrFooterGrid.setSizeUndefined();
        isrFooterGrid.setImmediate(true);
        isrFooterGrid.setHeightMode(HeightMode.ROW);
        isrFooterGrid.setHeightByRows(4);
        isrFooterGrid.setResponsive(true);
        isrFooterGrid.setEditorBuffered(false);
        isrFooterGrid.setWidth("100%");

        isrFooterGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        footerLayout.addComponent(isrFooterGrid);
        footerLayout.setComponentAlignment(isrFooterGrid, Alignment.MIDDLE_LEFT);

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

        footerLayout.addComponent(formularioForm);
        footerLayout.setComponentAlignment(formularioForm, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(footerLayout);
        mainLayout.setComponentAlignment(footerLayout, Alignment.TOP_CENTER);
    }

    public void fillGrids() {
        fillDetailsGrid();
    }

    public void fillDetailsGrid() {
        isrRetenidoContainer.removeAllItems();
        isrFooterGrid.getContainerDataSource().removeAllItems();

        isrRetenidoFooter.getCell(MONTO_PROPERTY).setText("0.00");
        isrRetenidoFooter.getCell(MONTO_PROPERTY).setText("0.00");

        totalISRPorPagar = 0.00;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT CodigoCC, IdNomenclatura, Fecha, SerieDocumento, NumeroDocumento, NombreProveedor, TipoDocumento ";
            queryString += "FROM contabilidad_partida " + " ";
            queryString += "WHERE  IdEmpresa = " + empresaCbx.getValue() + " ";
            queryString += "AND Extract(YEAR_MONTH FROM Fecha) >= 202001 ";
            queryString += "AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue())  + "' ";
            queryString += "AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIsrRetenidoPorPagar() + " ";
            queryString += "AND HABER > 0 "; // solo las lineas cuenta por PAGAR
//            queryString += " AND TipoDocumento IN ('CONSTANCIA ISR COMPRA')";
            queryString += "AND Estatus <> 'ANULADO' ";
            queryString += "AND PagadoIVA = 'NO' ";
            queryString += "ORDER BY Fecha ";

//System.out.println(queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                do {

                    queryString = "SELECT ";
                    queryString += "SUM(DEBE) TOTALDEBE, SUM(DEBEQuetzales) TOTALDEBEQ, SUM(HABER) TOTALHABER, SUM(HABERQuetzales) TOTALHABERQ ";
                    queryString += "FROM contabilidad_partida ";
                    queryString += "WHERE IdEmpresa = " + empresaCbx.getValue() + " ";
                    queryString += "AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue())  + "' ";
                    queryString += "AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "' ";
                    queryString += "AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura") + " ";
                    queryString += "AND Estatus <> 'ANULADO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        double isrSaldo = (rsRecords1.getDouble("TOTALHABERQ") - rsRecords1.getDouble("TOTALDEBEQ"));
                        if (isrSaldo > 0.00) {

                            Object itemId = isrRetenidoContainer.addItem();
                            isrRetenidoContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                            isrRetenidoContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            isrRetenidoContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                            isrRetenidoContainer.getContainerProperty(itemId, NO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                            isrRetenidoContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                            isrRetenidoContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(isrSaldo));
                            isrRetenidoContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(numberFormat2.format(isrSaldo));
                            isrRetenidoDetailGrid.select(itemId);
                            totalISRPorPagar += isrSaldo;
                        }
                    }
                } while (rsRecords.next()) ;
            }
            isrRetenidoFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalISRPorPagar));

            Object itemId = isrHeaderContainer.addItem();
            isrHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalISRPorPagar));

            itemId = isrHeaderContainer.addItem();
            isrHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("---------------");

            serieTxt.setReadOnly(false);
            serieTxt.setValue("1331");
            numeroTxt.setReadOnly(false);
            numeroTxt.setValue("");
            montoTxt.setReadOnly(false);
            montoTxt.setValue(Double.valueOf(numberFormat2.format(totalISRPorPagar)).doubleValue());
            fechaFormularioDt.setReadOnly(false);
            crearPartidaBtn.setVisible(true);

        } catch (Exception ex) {
            System.out.println("Error al listar tabla facturas: " + ex);
            ex.printStackTrace();
        }
    }

    private void setTotalisr() {
        double totalISRPorPagar = 0.00;
        for(Object itemId : isrRetenidoContainer.getItemIds()) {
            if(isrRetenidoDetailGrid.isSelected(itemId)) {
                totalISRPorPagar += Double.valueOf(isrRetenidoContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).getValue().toString());
            }
        }
        isrRetenidoFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(totalISRPorPagar));

        isrHeaderContainer.removeAllItems();

        Object itemId = isrHeaderContainer.addItem();
        isrHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(totalISRPorPagar));

        itemId = isrHeaderContainer.addItem();
        isrHeaderContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("---------------");

        montoTxt.setReadOnly(false);
        montoTxt.setValue(Double.valueOf(numberFormat2.format(totalISRPorPagar)).doubleValue());

    }

    public void insertPartidas() {
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
        queryString += " And TipoDocumento = 'FORMULARIO ISR RETENIDO'";
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

        queryString = " Insert Into proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha, ";
        queryString += " TipoDocumento, SerieDocumento, NumeroDocumento, MonedaDocumento, ";
        queryString += " Monto, MontoQuetzales, TipoCambio, ";
        queryString += " IdUsuarioAutorizoPago,CreadoFechayHora,CreadoUsuario)";
        queryString += " Values(";
        queryString += empresaCbx.getValue();
        queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); //SAT
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
        queryString += ",'FORMULARIO ISR RETENIDO'";
        queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += ",'QUETZALES'";
        queryString += "," + totalISRPorPagar; //monto
        queryString += "," + totalISRPorPagar; //monto quetzales
        queryString += ", 1.0"; // tipo cambiio
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new Date()) + "'";
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
        } catch (Exception ex1) {
            System.out.println("Error al insertar proveedor cuentaCorriente : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        /// Ingreso del haber o el debe
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
        queryString += ",'FORMULARIO ISR RETENIDO'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
        queryString += ",0";
        queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat();  //SAT
        queryString += ",'1669394-9'"; //SAT
        queryString += ",'Superintendencia de Administracion Tributaria'";
        queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones();
        queryString += ",'QUETZALES'";
//        queryString += "," + isrDiferencia; // monto del documento
        queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
        queryString += ",0"; //debe
        queryString += "," +  montoTxt.getDoubleValueDoNotThrow(); //HABER
        queryString += ",0"; //debe Q
        queryString += "," +  montoTxt.getDoubleValueDoNotThrow(); //HABERQ
        queryString += ",1.0"; //tipo cambio
        queryString += "," +  montoTxt.getDoubleValueDoNotThrow(); //HABER
        queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // SAT idliquidador
        queryString += ",'FORMULARIO ISR RETENIDO " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
        queryString += ",'NO'"; //REFRENCIA NO ISR
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ",null";
        queryString += ",null";
        queryString += ",0";
        queryString += ",null";
        queryString += ")";

        //CICLO DE ISR X PAGAR
        for (Object itemId : isrRetenidoContainer.getItemIds()) {
            if(isrRetenidoDetailGrid.isSelected(itemId)) {
                queryString += ",(";
                queryString += empresaCbx.getValue();
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + isrRetenidoContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "'"; //codigoCC
                queryString += ",'FORMULARIO ISR RETENIDO'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
                queryString += ",0";
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); //sat
                queryString += ",'1669394-9'"; //sat
                queryString += ",'Superintendencia de Administracion Tributaria'";
                queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIsrRetenidoPorPagar();
                queryString += ",'QUETZALES'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
                queryString += "," + isrRetenidoContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "");
                queryString += ",0.00"; // HABER
                queryString += "," + isrRetenidoContainer.getContainerProperty(itemId, MONTO_PROPERTY).getValue().toString().replaceAll(",", "");
                queryString += ",0.00"; // HABER Q
                queryString += ",1.00"; // tipo cambio
                queryString += ",0.00"; // saldo
                queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat();
                queryString += ",'FORMULARIO ISR RETENIDO " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
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

        // multa
        if(multaTxt.getDoubleValueDoNotThrow() > 0) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'FORMULARIO ISR RETENIDO'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaFormularioDt.getValue()) + "'";
            queryString += ",0";
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat();  //sat
            queryString += ",'1669394-9'"; //sat
            queryString += ",'Superintendencia de Administracion Tributaria'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getMultasYRectificaciones();
            queryString += ",'QUETZALES'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // monto del documento
            queryString += "," + multaTxt.getDoubleValueDoNotThrow(); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + multaTxt.getDoubleValueDoNotThrow(); // DEBE
            queryString += ",0.00"; //HABER Q
            queryString += ",1.0"; //tipo cambio
            queryString += ",0"; //SALDO
            queryString += "," + ((SopdiUI)mainUI).proveedoresInstitucionales.getSat(); // idliquidador
            queryString += ",'FORMULARIO ISR RETENIDO " + serieTxt.getValue() + " " + numeroTxt.getValue() + " Superintendencia de Administracion Tributaria'";
            queryString += ",'NO'"; //REFRENCIA NO ISR
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += ")";
        }

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            if (multaTxt.getDoubleValueDoNotThrow() > 0 && multaTxt.getValue() != null) {

                queryString = " UPDATE contabilidad_partida ";
                queryString += " Set MontoDocumento = " + (montoTxt.getDoubleValueDoNotThrow() + multaTxt.getDoubleValueDoNotThrow());
                queryString += " , Haber = " + (montoTxt.getDoubleValueDoNotThrow() + multaTxt.getDoubleValueDoNotThrow());
                queryString += " , HaberQuetzales = " + (montoTxt.getDoubleValueDoNotThrow() + multaTxt.getDoubleValueDoNotThrow());
                queryString += " WHERE codigoPartida = '" + codigoPartida + "'";
                queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones();
                queryString += " AND IdEmpresa = " + empresaCbx.getValue();

                stQuery.executeUpdate(queryString);
            }

            if(isrRetenidoContainer.size() > 0) {

                queryString = " UPDATE contabilidad_partida Set PagadoIVA = 'SI' WHERE CODIGOCC IN ( ";
                for (Object itemId : isrRetenidoContainer.getItemIds()) {
                    if (isrRetenidoDetailGrid.isSelected(itemId)) {
                        queryString += "'" + isrRetenidoContainer.getContainerProperty(itemId, CODIGO_PROPERTY).getValue().toString() + "',";
                    }
                }
                queryString = queryString.substring(0, queryString.length() - 1) + ")";
                queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIsrRetenidoPorPagar();
                queryString += " AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue())  + "'";
                stQuery.executeUpdate(queryString);
            }

            Notification notif = new Notification("FORMULARIO DEL ISR REGISTRADO EXITOSAMENTE!",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(2000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            ((IngresoDocumentosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(String.valueOf(empresaCbx.getValue()), 0);

            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar FORMULARIO DEL ISR RETENIDO  : " + ex1.getMessage());
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
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }

        }
    }
}
