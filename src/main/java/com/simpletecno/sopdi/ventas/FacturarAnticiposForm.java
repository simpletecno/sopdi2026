package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.tesoreria.PagoDocumentoVentaForm;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.simpletecno.sopdi.ventas.FacturaVentaInfileForm.NIT_PROPERTY;

public class FacturarAnticiposForm extends Window {

    VerticalLayout mainLayout;
    UI mainUI;

    HorizontalLayout layoutTitle;
    Label titleLbl;
    
    Button guardarBtn;
    Button salirBtn;

    DateField fechaDt;
    TextField numeroTxt;
    TextField descripcionTxt;
    ComboBox proveedorCbx;
    NumberField montoTxt;

    ComboBox cuentaContable1Cbx;
    
    public IndexedContainer anticiposContainer = new IndexedContainer();
    Grid anticiposGrid;
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo Partida";
    static final String CODIGO_CC_PROPERTY = "Codigo CC";
    static final String FECHA_DOCUMENTO_PROPERTY = "Fecha";
    static final String NUMERO_DOCUMENTO_PROPERTY = "# Documento";
    static final String MONEDA_DOCUMENTO_PROPERTY = "Moneda";
    static final String MONTO_DOCUMENTO_PROPERTY = "Monto";

    Statement stQuery, stQuery2, stQuery3;
    ResultSet rsRecords, rsRecords2, rsRecords3;
    String queryString, codigoPartida;
    String variableTemp = "";

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();
    
    public FacturarAnticiposForm() {

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("87%");
        setHeight("85%");
        setModal(true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");
        
        titleLbl = new Label(empresaId + " " + empresaNombre + " FACTURAR ANTICIPOS DE CLIENTES");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");
        
        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        mainLayout.addComponent(crearComponentes());

    }
    
    public void llenarProveedor() {
        String prov = "";
        try {

            queryString = " SELECT prov.* ";
            queryString += " FROM proveedor_empresa prov";
            queryString += " WHERE prov.Inhabilitado = 0 ";
            queryString += " AND prov.EsCliente = 1";
            queryString += " AND prov.IdEmpresa = " + empresaId;
            queryString += " ORDER By prov.Nombre";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery3 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    queryString = " SELECT * FROM contabilidad_partida ";
//                    queryString += " WHERE IdNomenclatura IN (" + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal() + "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal() + ")";
                    queryString += " WHERE IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes();
                    queryString += " AND TipoDocumento IN ('DEPOSITO', 'NOTA DE CREDITO') ";
                    queryString += " AND IdProveedor = " + rsRecords.getString("IdProveedor");
                    queryString += " AND IdEmpresa = " + empresaId;
                    
                    rsRecords2 = stQuery2.executeQuery(queryString);

                    if (rsRecords2.next()) {
                        do {

                            queryString = " SELECT  ";
                            queryString += " SUM(HABER - DEBE) as TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) as TOTALSALDOQ ";
                            queryString += " FROM contabilidad_partida";
                            queryString += " WHERE CodigoCC = '" + rsRecords2.getString("CodigoCC") + "'";
                            queryString += " AND IdNomenclatura = " + rsRecords2.getString("IdNomenclatura");
                            queryString += " GROUP BY CodigoCC, IdNomenclatura";
                            queryString += " HAVING TOTALSALDO > 0";
                            
                            rsRecords3 = stQuery3.executeQuery(queryString);

                            if (rsRecords3.next()) {
                                if (!prov.equals(rsRecords.getString("IdProveedor"))) {
                                    proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                                    proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                                    proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                                }
                            }
                            prov = rsRecords.getString("IdProveedor");
                        } while (rsRecords2.next());
                    }

                } while (rsRecords.next());

            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error en busqueda proveedores con saldo " + e);
            e.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " AND Tipo IN ('SERVICIO', 'PRODUCTO', 'VENTA')";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  // encontrado

                cuentaContable1Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable1Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
            }

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public HorizontalLayout crearComponentes() {

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addStyleName("rcorners3");
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);
        horizontalLayout.setWidth("100%");

        VerticalLayout leftVerticalLayout = new VerticalLayout();
        leftVerticalLayout.setWidth("100%");
        leftVerticalLayout.setSpacing(true);
        leftVerticalLayout.setMargin(true);

        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.setWidth("100%");
        rightVerticalLayout.setSpacing(true);
        rightVerticalLayout.setMargin(true);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_RIGHT);

        proveedorCbx = new ComboBox("Cliente");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener((event) -> {
            llenarTablaAnticiposPorFacturar();
        });

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());

        numeroTxt = new TextField("Recibo contable:");
        numeroTxt.setWidth("100%");

        montoTxt = new NumberField("Monto : ");
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
        montoTxt.setWidth("100%");

        descripcionTxt = new TextField("Descripción :");
        descripcionTxt.setWidth("100%");
        descripcionTxt.setVisible(false);

        leftVerticalLayout.addComponent(proveedorCbx);
        leftVerticalLayout.addComponent(fechaDt);
        leftVerticalLayout.addComponent(numeroTxt);
        leftVerticalLayout.addComponent(descripcionTxt);
        leftVerticalLayout.addComponent(montoTxt);

        HorizontalLayout layoutHorizontal1 = new HorizontalLayout();
        layoutHorizontal1.setResponsive(true);
        layoutHorizontal1.setSpacing(true);

        HorizontalLayout layoutHorizontal2 = new HorizontalLayout();
        layoutHorizontal2.setResponsive(true);
        layoutHorizontal2.setSpacing(true);

        anticiposContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(NUMERO_DOCUMENTO_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(FECHA_DOCUMENTO_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(MONEDA_DOCUMENTO_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(MONTO_DOCUMENTO_PROPERTY, String.class, null);

        anticiposGrid = new Grid("", anticiposContainer);
        anticiposGrid.setWidth("100%");
        anticiposGrid.setImmediate(true);
        anticiposGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        anticiposGrid.setDescription("Seleccione uno o varios registros del mismo proveedor.");
        anticiposGrid.setHeightMode(HeightMode.ROW);
        anticiposGrid.setHeightByRows(5);
        anticiposGrid.setResponsive(true);
        anticiposGrid.setEditorBuffered(false);
        anticiposGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (anticiposGrid.getSelectedRows() != null) {

                    Object gridItem;

                    Iterator iter = event.getSelected().iterator();

                    if (iter == null) {
                        limpiarPartida();
                        return;

                    }
                    if (!iter.hasNext()) {
                        limpiarPartida();
                        return;
                    }

                    limpiarPartida();
                    montoTxt.setReadOnly(false);
                    montoTxt.setValue(0.00);

                    Iterator iter2 = anticiposGrid.getSelectedRows().iterator();

                    while (iter2.hasNext()) {
                        gridItem = iter2.next();
                        montoTxt.setValue( (montoTxt.getDoubleValueDoNotThrow() + Double.valueOf(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))));
                    } // END WHILE
                    montoTxt.setReadOnly(true);
                }
            }
        });

        anticiposGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        anticiposGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);

        cuentaContable1Cbx = new ComboBox("CUENTA DEL PRODUCTO O SERVICIO :");
        cuentaContable1Cbx.setWidth("24em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();
        llenarProveedor();

        guardarBtn = new Button("Grabar");
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (validarCamposParaIngresarPagoDocumentos()) {
                    ingresarDocumento();
                }
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.setWidth("7em");
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal2.addComponents(salirBtn, guardarBtn);
        layoutHorizontal2.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontal2.setComponentAlignment(salirBtn, Alignment.BOTTOM_CENTER);

        rightVerticalLayout.addComponent(anticiposGrid);
        rightVerticalLayout.setComponentAlignment(anticiposGrid, Alignment.TOP_CENTER);
        rightVerticalLayout.addComponent(layoutHorizontal1);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal2);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);

        return horizontalLayout;

    }

    public void llenarTablaAnticiposPorFacturar() {

        anticiposContainer.removeAllItems();

        queryString = " SELECT * FROM contabilidad_partida ";
        queryString += " WHERE IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes();
        queryString += " AND TipoDocumento IN ('DEPOSITO', 'NOTA DE CREDITO') ";
        queryString += " And IdProveedor = " + proveedorCbx.getValue();
        queryString += " And IdEmpresa = " + empresaId;

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR ANTICIPOS CIENTE : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    // se busca el saldo real...
                    queryString = " SELECT  ";
                    queryString += " SUM(HABER - DEBE) as TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) as TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " GROUP BY CodigoCC, IdNomenclatura";
                    queryString += " HAVING TOTALSALDO > 0";
// no es necesario                   queryString += " Order by contabilidad_partida.NombreProveedor";

                    stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords2 = stQuery2.executeQuery(queryString);

                    if (rsRecords2.next()) {
                        Object itemId = anticiposContainer.addItem();
                        anticiposContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        anticiposContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                        anticiposContainer.getContainerProperty(itemId, FECHA_DOCUMENTO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        anticiposContainer.getContainerProperty(itemId, NUMERO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento") + " " + rsRecords.getString("SerieDocumento"));
                        anticiposContainer.getContainerProperty(itemId, MONEDA_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                        anticiposContainer.getContainerProperty(itemId, MONTO_DOCUMENTO_PROPERTY).setValue(rsRecords2.getString("TOTALSALDO"));
                    }

                } while (rsRecords.next());
            }
            if (anticiposContainer.size() == 0) {
                guardarBtn.setEnabled(false);
                Notification.show("Este cliente no tiene facturas/recibos contables pendientes de pago.", Notification.Type.ASSISTIVE_NOTIFICATION);
            } else {
                guardarBtn.setEnabled(true);
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error al listar tabla de DOCUMENTOS : " + ex);
            Notification.show("ERROR AL LEER DOCUMENTOS VENTA PARA REGISTRAR PAGOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public boolean validarCamposParaIngresarPagoDocumentos() {

//        boolean error = false;
//
//        totalDebe = new BigDecimal(debe1Txt.getDoubleValueDoNotThrow()
//                + debe2Txt.getDoubleValueDoNotThrow() + debe3Txt.getDoubleValueDoNotThrow()
//                + debe4Txt.getDoubleValueDoNotThrow() + debe5Txt.getDoubleValueDoNotThrow()
//                + debe6Txt.getDoubleValueDoNotThrow() + debe7Txt.getDoubleValueDoNotThrow()
//                + debe8Txt.getDoubleValueDoNotThrow() + debe9Txt.getDoubleValueDoNotThrow()
//                + debe10Txt.getDoubleValueDoNotThrow()
//        ).setScale(2, BigDecimal.ROUND_HALF_UP);
//
//        totalHaber = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
//                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
//                + haber4Txt.getDoubleValueDoNotThrow() + haber5Txt.getDoubleValueDoNotThrow()
//                + haber6Txt.getDoubleValueDoNotThrow() + haber7Txt.getDoubleValueDoNotThrow()
//                + haber8Txt.getDoubleValueDoNotThrow() + haber9Txt.getDoubleValueDoNotThrow()
//                + haber10Txt.getDoubleValueDoNotThrow()
//        ).setScale(2, BigDecimal.ROUND_HALF_UP);
//
//        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
//        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);
//
//        if (totalDebe.doubleValue() != totalHaber.doubleValue()) {
//            System.out.println("Debe =" + totalDebe.doubleValue() + "  haber=" + totalHaber);
//            Notification.show("La partida está descuadrada, por favor revisar"
//                    + " Debe = " + totalDebe.doubleValue() + "  Haber = " + totalHaber, Notification.Type.WARNING_MESSAGE);
//            error = true;
//        }
//
        if (proveedorCbx.getValue() == null) {
            Notification.show("Por favor, seleccione un cliente..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return false;
        }

        if (montoTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor, Ingrese un monto..", Notification.Type.ERROR_MESSAGE);
            montoTxt.focus();
            return false;
        }
        /*
        if (descripcionTxt.getValue().isEmpty()) {
            Notification.show("Por favor, Ingrese una descripción..", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            error = true;
        }
         */

        if (numeroTxt.getValue().isEmpty()) {
            Notification.show("Por favor ingrese un # de RECIBO CONTABLE O FACTURA.", Notification.Type.ERROR_MESSAGE);
            numeroTxt.focus();
            return false;
        }
        return true;
    }

    public void ingresarDocumento() {

        queryString = " SELECT * FROM contabilidad_partida";
        queryString += " WHERE NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " AND IdProveedor     =  " + String.valueOf(proveedorCbx.getValue());
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND TipoDocumento = 'RECIBO CONTABLE'";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Query=" + queryString + "\n\n");

        try {
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                Notification.show("Este documento ya fué ingresado, revise.!.", Notification.Type.WARNING_MESSAGE);
                numeroTxt.focus();
                return;
            }
        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error al buscar documento : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        codigoPartida = empresaId + año + mes + dia + "0";

        queryString = " SELECT codigoPartida FROM contabilidad_partida ";
        queryString += " WHERE codigoPartida like '" + codigoPartida + "%'";
        queryString += " ORDER BY codigoPartida desc ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {   //encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

//                System.out.println("ultimo encontrado " + ultimoEncontado);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
        queryString += " TipoDocumento, Fecha, IdProveedor, NombreProveedor, NumeroDocumento, IdNomenclatura,";
        queryString += " MonedaDocumento, MontoDocumento, Debe, Haber, DebeQuetzales, HaberQuetzales, TipoCambio,";
        queryString += " Saldo, Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values (";
        queryString += empresaId;
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'RECIBO CONTABLE'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes();
        queryString += ",'QUETZALES'";
        queryString += "," + montoTxt.getValue(); // monto documento
        queryString += "," + montoTxt.getValue(); // DEBE
        queryString += ",0.00"; //HABER
        queryString += "," + montoTxt.getValue();
        queryString += ",0.00"; //HABER Q.
        queryString += ",1.0";
        queryString += ",0.00";
        queryString += ",'RECIBO CONTABLE " + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += "),";

        queryString += "(";
        queryString += empresaId;
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",''";
        queryString += ",'RECIBO CONTABLE'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += "," + cuentaContable1Cbx.getValue(); // la cuenta de producto o servicio
        queryString += ",'QUETZALES'";
        queryString += "," + montoTxt.getValue(); // monto documento
        queryString += "," + montoTxt.getValue(); // DEBE
        queryString += ",0.00"; //HABER
        queryString += "," + montoTxt.getValue();
        queryString += ",0.00"; //HABER Q.
        queryString += ",1.0";
        queryString += ",0.00";
        queryString += ",'RECIBO CONTABLE " + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        /***********************************************************************************************************
         ***
         *** SEGUNDA PARTIDA
         ***
        ***********************************************************************************************************/

        ultimoEncontado = codigoPartida.substring(12, 15);

        String codigoCCRecibo = codigoPartida;

        codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

        String queryString2 = "";

        queryString2 = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
        queryString2 += " TipoDocumento, Fecha, IdProveedor, NombreProveedor, NumeroDocumento, IdNomenclatura,";
        queryString2 += " MonedaDocumento, MontoDocumento, Debe, Haber, DebeQuetzales, HaberQuetzales, TipoCambio,";
        queryString2 += " Saldo, Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString2 += " Values (";
        queryString2 += empresaId;
        queryString2 += ",'INGRESADO'";
        queryString2 += ",'" + codigoPartida + "'";
        queryString2 += ",'" + codigoCCRecibo + "'";
        queryString2 += ",'PAGO DOCUMENTO VENTA'";
        queryString2 += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString2 += "," + proveedorCbx.getValue();
        queryString2 += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
        queryString2 += ",'" + numeroTxt.getValue() + "'";
        queryString2 += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes();
        queryString2 += ",'QUETZALES'";
        queryString2 += "," + montoTxt.getValue(); // monto documento
        queryString2 += ",0.00"; //DEBE
        queryString2 += "," + montoTxt.getValue(); // DEBE
        queryString2 += ",0.00"; //DEBE Q.
        queryString2 += "," + montoTxt.getValue(); // DEBE
        queryString2 += ",1.0";
        queryString2 += ",0.00";
        queryString2 += ",'PAGO DOCUMENTO VENTA " + descripcionTxt.getValue() + "'";
        queryString2 += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString2 += ",current_timestamp";
        queryString2 += ")";

        Iterator iter2 = anticiposGrid.getSelectedRows().iterator();

        Object gridItem;

        while (iter2.hasNext()) {
            gridItem = iter2.next();
            queryString2 += ",(";
            queryString2 += empresaId;
            queryString2 += ",'INGRESADO'";
            queryString2 += ",'" + codigoPartida + "'";
            queryString2 += ",'" + String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_CC_PROPERTY).getValue()) + "'";   /// CODIGOCC
            queryString2 += ",'PAGO DOCUMENTO VENTA'";
            queryString2 += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString2 += "," + proveedorCbx.getValue();
            queryString2 += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString2 += ",'" + numeroTxt.getValue() + "'";
            queryString2 += "," + ((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes();
            queryString2 += ",'QUETZALES'";
            queryString2 += "," + montoTxt.getValue(); // monto documento
            queryString2 += "," + String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "");  //HABER
            queryString2 += ",0.00";  //haber
            queryString2 += "," + String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "");  //HABER
            queryString2 += ",0.00";  //haber Q
            queryString2 += ",1.00";
            queryString2 += ",0.00";
            queryString2 += ",'PAGO DOCUMENTO VENTA" + descripcionTxt.getValue() + "'";
            queryString2 += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString2 += ",current_timestamp";
            queryString2 += ")";
        } // END WHILE

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY REGISTRO RECIBO CONTABLE CON ANTICIPOS = " + queryString);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY REGISTRO RECIBO CONTABLE CON ANTICIPOS = " + queryString2);

        try {

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
            stQuery.executeUpdate(queryString2);

            if (!variableTemp.isEmpty()) {
                cambiarEstatusToken(codigoPartida);
            }

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            anticiposGrid.getSelectedRows().clear();
            anticiposGrid.getSelectionModel().reset();
            anticiposContainer.removeAllItems();

            ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta();

            Notification notif = new Notification("ANTICIPOS FACTURADOS (recibo contable) EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            montoTxt.setValue(0.00);

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error al insertar RECIBO CONTABLE POR ANTICIPOS CLIENTE: " + ex1.getMessage());
            ex1.printStackTrace();

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(PagoDocumentoVentaForm.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void limpiarPartida() {
        //montoTxt.setValue(0.00);

        cuentaContable1Cbx.setReadOnly(false);
        cuentaContable1Cbx.clear();
    }

    public void cambiarEstatusToken(String codigoPartida) {

        try {
            queryString = "UPDATE token SET ";
            queryString += " IdUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ", Usuario = '" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName() + "'";
            queryString += ", FechaUsado = current_timestamp";
            queryString += ", CodigoPartida = '" + codigoPartida + "'";
            queryString += ", Estatus = 'UTILIZADO'";
            queryString += " Where Codigo = '" + variableTemp + "'";

            stQuery.executeUpdate(queryString);

            variableTemp = "";

        } catch (Exception e) {
            System.out.println("Error al intentar cambiar estatus token : " + e);
            e.printStackTrace();
        }

    }
}
