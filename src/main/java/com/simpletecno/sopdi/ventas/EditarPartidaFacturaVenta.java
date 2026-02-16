package com.simpletecno.sopdi.ventas;

import static com.simpletecno.sopdi.ventas.FacturaVentaForm.NOMBRESINCODIGO_PROPERTY;
import static com.simpletecno.sopdi.compras.IngresoDocumentosPDF.stPreparedQuery;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;

/**
 * @author user
 */
public class EditarPartidaFacturaVenta extends Window {

    VerticalLayout mainLayout;

    TextField serieTxt;
    TextField numeroTxt;
    TextField nitProveedotTxt;

    ComboBox empresaCbx;
    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox tipoVentaCbx;
    ComboBox tipoVenta1Cbx;
    ComboBox tipoVenta2Cbx;
    ComboBox tipoVenta3Cbx;
    ComboBox tipoVenta4Cbx;
    ComboBox tipoVenta5Cbx;
    ComboBox tipoVenta6Cbx;
    ComboBox tipoVenta7Cbx;
    ComboBox tipoVenta8Cbx;
    ComboBox cuentaContableCbx;
    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    ComboBox cuentaContable5Cbx;
    ComboBox cuentaContable6Cbx;
    ComboBox cuentaContable7Cbx;
    ComboBox cuentaContable8Cbx;

    DateField fechaDt;

    NumberField montoTxt;
    NumberField tasaCambioTxt;

    NumberField haberTxt;
    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;
    NumberField haber4Txt;
    NumberField haber5Txt;
    NumberField haber6Txt;
    NumberField haber7Txt;
    NumberField haber8Txt;

    NumberField debeTxt;
    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;
    NumberField debe5Txt;
    NumberField debe6Txt;
    NumberField debe7Txt;
    NumberField debe8Txt;

    String nombreArchvivo, tipoArchivo;
    Long pesoArchivo;
    StreamResource documentStreamResource;

    Button grabarBtn, cerrarFacturasBtn;

    CheckBox checkbox1;

    String variableTemp = "";

    static final String NIT_PROPERTY = "Eliminar";

    UI mainUI;
    Statement stQuery, stQuery2;
    ResultSet rsRecords, rsRecords2;

    String queryString;
    BigDecimal totalDebe, totalHaber, totalMonto;

    String codigoPartidaEdit, estatusPartidaEdit, descripcionEdit;
    String tipoDocumento;

    public EditarPartidaFacturaVenta(String codigoPartida, String descripcion, String tipoDocumento) {
        this.codigoPartidaEdit = codigoPartida;
        this.descripcionEdit = descripcion;
        this.tipoDocumento = tipoDocumento;
        this.setResponsive(true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("96%");
        setHeight("80%");

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("90%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();
        empresaCbx.select(empresaCbx.getItemIds().iterator().next());

        Label titleLbl = new Label("EDITAR DOCUMENTO VENTA");
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.addStyleName("h3_custom");

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);
        crearDocumento();
        crearTablaDocumentos();
    }

    public void crearDocumento() {

        HorizontalLayout camposDocumento = new HorizontalLayout();
        camposDocumento.addStyleName("rcorners3");
        camposDocumento.setSpacing(true);

        serieTxt = new TextField("Serie : ");
        serieTxt.setWidth("5em");

        numeroTxt = new TextField("Número : ");
        numeroTxt.setWidth("8em");

        proveedorCbx = new ComboBox("Proveedores : ");
        proveedorCbx.setWidth("20em");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.addContainerProperty(NOMBRESINCODIGO_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
//        llenarComboProveedor();

        nitProveedotTxt = new TextField("Nit : ");
        nitProveedotTxt.setWidth("6em");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("8em");
        fechaDt.setValue(new java.util.Date());

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
        montoTxt.setWidth("7em");

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.setWidth("8em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.addValueChangeListener((event) -> {
            if(monedaCbx.getValue() != null){
                if(monedaCbx.getValue().equals("DOLARES")){                                       
                   tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));                   
                }else{
                    tasaCambioTxt.setValue(1.00);
                }
            }else{
                 tasaCambioTxt.setValue(1.00);
            }
        });
        
        tasaCambioTxt = new NumberField("Tipo de Cambio : ");
        tasaCambioTxt.setDecimalAllowed(true);
        tasaCambioTxt.setDecimalPrecision(5);
        tasaCambioTxt.setMinimumFractionDigits(5);
        tasaCambioTxt.setDecimalSeparator('.');
        tasaCambioTxt.setDecimalSeparatorAlwaysShown(true);
        tasaCambioTxt.setGroupingUsed(true);
        tasaCambioTxt.setGroupingSeparator(',');
        tasaCambioTxt.setGroupingSize(3);
        tasaCambioTxt.setImmediate(true);
        tasaCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tasaCambioTxt.setWidth("4em");
        tasaCambioTxt.setValue(1.00);

        checkbox1 = new CheckBox("Hacer retencion IRS");
        checkbox1.addStyleName(ValoTheme.CHECKBOX_LARGE);
        checkbox1.setValue(false);

        camposDocumento.addComponent(serieTxt);
        camposDocumento.addComponent(numeroTxt);
        camposDocumento.addComponent(nitProveedotTxt);
        camposDocumento.addComponent(proveedorCbx);
        camposDocumento.addComponent(fechaDt);
        camposDocumento.addComponent(montoTxt);
        camposDocumento.addComponent(monedaCbx);
        camposDocumento.addComponent(tasaCambioTxt);
        if (tipoDocumento.equals("FACTURA VENTA")) {
            camposDocumento.addComponent(checkbox1);
            camposDocumento.setComponentAlignment(checkbox1, Alignment.MIDDLE_RIGHT);
        }

        mainLayout.addComponent(camposDocumento);
        mainLayout.setComponentAlignment(camposDocumento, Alignment.MIDDLE_CENTER);

    }

    public void buscarArchivo() {
        try {

            queryString = "Select * ";
            queryString += " From contabilidad_partida";
            queryString += " Where CodigoPartida = " + codigoPartidaEdit;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (!rsRecords.next()) { //  no encontrado  
                Notification.show("Documento scan no disponible para visualizar!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            if (rsRecords.getObject("Archivo") == null) {
                Notification.show("Documento scan no disponible para visualizar!");

                return;
            }

            Window window = new Window();
            window.setResizable(true);
            window.setWidth("95%");
            window.setHeight("95%");

            final byte docBytes[] = rsRecords.getBytes("Archivo");
            final String fileName = rsRecords.getString("ArchivoNombre");
            documentStreamResource = null;

            if (docBytes != null) {
                documentStreamResource = new StreamResource(
                        new StreamResource.StreamSource() {
                    public InputStream getStream() {
                        return new ByteArrayInputStream(docBytes);
                    }
                }, fileName
                );
            }
            documentStreamResource.setMIMEType(rsRecords.getString("ArchivoTipo"));
            documentStreamResource.setFilename(rsRecords.getString("ArchivoNombre"));
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

            System.out.println("Tipo de Archivo : " + rsRecords.getString("ArchivoTipo"));
            System.out.println("Archivo Nombre :" + rsRecords.getString("ArchivoNombre"));
            System.out.println("Tamanio :" + rsRecords.getString("ArchivoPeso"));

            tipoArchivo = rsRecords.getString("ArchivoTipo");
            nombreArchvivo = rsRecords.getString("ArchivoNombre");
            pesoArchivo = rsRecords.getLong("ArchivoPeso");

        } catch (Exception ex) {
            System.out.println("Error al intentar buscar el archivo de la partida " + ex);
            ex.printStackTrace();
        }
    }

    public void llenarCampos() {

        estatusPartidaEdit = "INGRESADO";

        queryString = " SELECT * from contabilidad_partida";
        queryString += " where CodigoPartida = '" + codigoPartidaEdit + "'";

        try {
            int contador = 0;
            double montoFactura = 0.00;

            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            while (rsRecords2.next()) { //  encontrado                

                if (contador == 0) {
                    estatusPartidaEdit = rsRecords2.getString("Estatus");
                    empresaCbx.select(rsRecords2.getString("IdEmpresa"));
                    serieTxt.setValue(rsRecords2.getString("SerieDocumento"));
                    numeroTxt.setValue(rsRecords2.getString("NumeroDocumento"));
                    nitProveedotTxt.setValue(rsRecords2.getString("NITProveedor"));
                    proveedorCbx.select(rsRecords2.getString("IdProveedor"));
                    fechaDt.setValue(rsRecords2.getDate("Fecha"));
                    monedaCbx.setValue(rsRecords2.getString("MonedaDocumento"));
                    tasaCambioTxt.setValue(rsRecords2.getDouble("TipoCambio"));
                    cuentaContableCbx.select(rsRecords2.getString("IdNomenclatura"));
                    debeTxt.setValue(rsRecords2.getDouble("Debe"));
                    tipoVentaCbx.select(rsRecords2.getString("TipoVenta"));

                    if (rsRecords2.getString("Referencia") == null) {
                        checkbox1.setValue(false);
                    } else if (rsRecords2.getString("Referencia").equals("NO")) {
                        checkbox1.setValue(false);
                    } else if (rsRecords2.getString("Referencia").equals("SI")) {
                        checkbox1.setValue(true);
                    }

                }
                if (contador == 1) {
                    cuentaContable1Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe1Txt.setValue(rsRecords2.getDouble("Debe"));
                    haber1Txt.setValue(rsRecords2.getDouble("Haber"));
                    tipoVenta1Cbx.select(rsRecords2.getString("TipoVenta"));
                }
                if (contador == 2) {
                    cuentaContable2Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe2Txt.setValue(rsRecords2.getDouble("Debe"));
                    haber2Txt.setValue(rsRecords2.getDouble("Haber"));
                    tipoVenta2Cbx.setEnabled(true);
                    tipoVenta2Cbx.select(rsRecords2.getString("TipoVenta"));
                }
                if (contador == 3) {
                    cuentaContable3Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe3Txt.setValue(rsRecords2.getDouble("Debe"));
                    haber3Txt.setValue(rsRecords2.getDouble("Haber"));
                    tipoVenta3Cbx.setEnabled(true);
                    tipoVenta3Cbx.select(rsRecords2.getString("TipoVenta"));
                }
                if (contador == 4) {
                    cuentaContable4Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe4Txt.setValue(rsRecords2.getDouble("Debe"));
                    haber4Txt.setValue(rsRecords2.getDouble("Haber"));
                    tipoVenta4Cbx.setEnabled(true);
                    tipoVenta4Cbx.select(rsRecords2.getString("TipoVenta"));
                }
                if (contador == 5) {
                    cuentaContable5Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe5Txt.setValue(rsRecords2.getDouble("Debe"));
                    haber5Txt.setValue(rsRecords2.getDouble("Haber"));
                    tipoVenta5Cbx.setEnabled(true);
                    tipoVenta5Cbx.select(rsRecords2.getString("TipoVenta"));
                }
                if (contador == 6) {
                    cuentaContable6Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe6Txt.setValue(rsRecords2.getDouble("Debe"));
                    haber6Txt.setValue(rsRecords2.getDouble("Haber"));
                    tipoVenta6Cbx.setEnabled(true);
                    tipoVenta6Cbx.select(rsRecords2.getString("TipoVenta"));
                }
                if (contador == 7) {
                    cuentaContable7Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe7Txt.setValue(rsRecords2.getDouble("Debe"));
                    haber7Txt.setValue(rsRecords2.getDouble("Haber"));
                    tipoVenta7Cbx.setEnabled(true);
                    tipoVenta7Cbx.select(rsRecords2.getString("TipoVenta"));
                }
                if (contador == 8) {
                    cuentaContable8Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe8Txt.setValue(rsRecords2.getDouble("Debe"));
                    haber8Txt.setValue(rsRecords2.getDouble("Haber"));
                    tipoVenta8Cbx.setEnabled(true);
                    tipoVenta8Cbx.select(rsRecords2.getString("TipoVenta"));
                }

                contador = contador + 1;
                montoFactura += rsRecords2.getDouble("Haber");

            }
            fechaDt.setReadOnly(true);
            montoTxt.setValue(montoFactura);
            empresaCbx.setReadOnly(false);
            serieTxt.setReadOnly(false);
            numeroTxt.setReadOnly(false);
            nitProveedotTxt.setReadOnly(false);
            proveedorCbx.setReadOnly(false);
            fechaDt.setReadOnly(false);
            montoTxt.setReadOnly(false);
            monedaCbx.setReadOnly(false);
            tasaCambioTxt.setReadOnly(false);
            cuentaContableCbx.setReadOnly(true);
            haber4Txt.setReadOnly(false);
            debeTxt.setReadOnly(false);
            haberTxt.setReadOnly(false);
            debe1Txt.setReadOnly(false);
            debe2Txt.setReadOnly(false);
            debe3Txt.setReadOnly(false);
            debe4Txt.setReadOnly(false);
            debe5Txt.setReadOnly(false);
            debe6Txt.setReadOnly(false);
            debe7Txt.setReadOnly(false);
            debe8Txt.setReadOnly(false);

            //   cuentaContableCbx.setReadOnly(true);
        } catch (Exception ex1) {
            System.out.println("Error al listar datos de la partida : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearTablaDocumentos() {

        VerticalLayout contenedorLayout = new VerticalLayout();
        contenedorLayout.setResponsive(true);
        contenedorLayout.addStyleName("rcorners3");
        contenedorLayout.setSpacing(true);

        HorizontalLayout layoutHorizontal = new HorizontalLayout();
        layoutHorizontal.setResponsive(true);
        layoutHorizontal.setSpacing(true);

        HorizontalLayout layoutHorizontal1 = new HorizontalLayout();
        layoutHorizontal1.setResponsive(true);
        layoutHorizontal1.setSpacing(true);

        HorizontalLayout layoutHorizontal2 = new HorizontalLayout();
        layoutHorizontal2.setResponsive(true);
        layoutHorizontal2.setSpacing(true);

        HorizontalLayout layoutHorizontal3 = new HorizontalLayout();
        layoutHorizontal3.setResponsive(true);
        layoutHorizontal3.setSpacing(true);

        HorizontalLayout layoutHorizontal4 = new HorizontalLayout();
        layoutHorizontal4.setResponsive(true);
        layoutHorizontal4.setSpacing(true);

        HorizontalLayout layoutHorizontal5 = new HorizontalLayout();
        layoutHorizontal5.setResponsive(true);
        layoutHorizontal5.setSpacing(true);

        HorizontalLayout layoutHorizontal6 = new HorizontalLayout();
        layoutHorizontal6.setResponsive(true);
        layoutHorizontal6.setSpacing(true);

        HorizontalLayout layoutHorizontal7 = new HorizontalLayout();
        layoutHorizontal7.setResponsive(true);
        layoutHorizontal7.setSpacing(true);

        HorizontalLayout layoutHorizontal8 = new HorizontalLayout();
        layoutHorizontal8.setResponsive(true);
        layoutHorizontal8.setSpacing(true);

        HorizontalLayout layoutHorizontal9 = new HorizontalLayout();
        layoutHorizontal9.setResponsive(true);
        layoutHorizontal9.setSpacing(true);

        tipoVentaCbx = new ComboBox("Tipo de venta");
        tipoVentaCbx.setWidth("9em");
        tipoVentaCbx.addItem("PRODUCTO");
        tipoVentaCbx.addItem("SERVICIO");
        tipoVentaCbx.select("PRODUCTO");

        tipoVenta1Cbx = new ComboBox();
        tipoVenta1Cbx.setWidth("9em");
        tipoVenta1Cbx.addItem("PRODUCTO");
        tipoVenta1Cbx.addItem("SERVICIO");
        tipoVenta1Cbx.select("PRODUCTO");
        tipoVenta1Cbx.setEnabled(false);

        tipoVenta2Cbx = new ComboBox();
        tipoVenta2Cbx.setWidth("9em");
        tipoVenta2Cbx.addItem("PRODUCTO");
        tipoVenta2Cbx.addItem("SERVICIO");
        tipoVenta2Cbx.select("PRODUCTO");
        tipoVenta2Cbx.setEnabled(false);

        tipoVenta3Cbx = new ComboBox();
        tipoVenta3Cbx.setWidth("9em");
        tipoVenta3Cbx.addItem("PRODUCTO");
        tipoVenta3Cbx.addItem("SERVICIO");
        tipoVenta3Cbx.select("PRODUCTO");
        tipoVenta3Cbx.setEnabled(false);

        tipoVenta4Cbx = new ComboBox();
        tipoVenta4Cbx.setWidth("9em");
        tipoVenta4Cbx.addItem("PRODUCTO");
        tipoVenta4Cbx.addItem("SERVICIO");
        tipoVenta4Cbx.select("PRODUCTO");
        tipoVenta4Cbx.setEnabled(false);

        tipoVenta5Cbx = new ComboBox();
        tipoVenta5Cbx.setWidth("9em");
        tipoVenta5Cbx.addItem("PRODUCTO");
        tipoVenta5Cbx.addItem("SERVICIO");
        tipoVenta5Cbx.select("PRODUCTO");
        tipoVenta5Cbx.setEnabled(false);

        tipoVenta6Cbx = new ComboBox();
        tipoVenta6Cbx.setWidth("9em");
        tipoVenta6Cbx.addItem("PRODUCTO");
        tipoVenta6Cbx.addItem("SERVICIO");
        tipoVenta6Cbx.select("PRODUCTO");
        tipoVenta6Cbx.setEnabled(false);

        tipoVenta7Cbx = new ComboBox();
        tipoVenta7Cbx.setWidth("9em");
        tipoVenta7Cbx.addItem("PRODUCTO");
        tipoVenta7Cbx.addItem("SERVICIO");
        tipoVenta7Cbx.select("PRODUCTO");
        tipoVenta7Cbx.setEnabled(false);

        tipoVenta8Cbx = new ComboBox();
        tipoVenta8Cbx.setWidth("9em");
        tipoVenta8Cbx.addItem("PRODUCTO");
        tipoVenta8Cbx.addItem("SERVICIO");
        tipoVenta8Cbx.select("PRODUCTO");
        tipoVenta8Cbx.setEnabled(false);

        cuentaContableCbx = new ComboBox("Cuenta contable");
        cuentaContableCbx.setWidth("30em");
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable1Cbx = new ComboBox();
        cuentaContable1Cbx.setWidth("30em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("30em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("30em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("30em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable5Cbx = new ComboBox();
        cuentaContable5Cbx.setWidth("30em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable6Cbx = new ComboBox();
        cuentaContable6Cbx.setWidth("30em");
        cuentaContable6Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable7Cbx = new ComboBox();
        cuentaContable7Cbx.setWidth("30em");
        cuentaContable7Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable8Cbx = new ComboBox();
        cuentaContable8Cbx.setWidth("30em");
        cuentaContable8Cbx.setFilteringMode(FilteringMode.CONTAINS);

        llenarComboCuentaContable();

        haberTxt = new NumberField("HABER : ");
        haberTxt.setDecimalAllowed(true);
        haberTxt.setDecimalPrecision(2);
        haberTxt.setMinimumFractionDigits(2);
        haberTxt.setDecimalSeparator('.');
        haberTxt.setDecimalSeparatorAlwaysShown(true);
        haberTxt.setValue(0d);
        haberTxt.setGroupingUsed(true);
        haberTxt.setGroupingSeparator(',');
        haberTxt.setGroupingSize(3);
        haberTxt.setImmediate(true);
        haberTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haberTxt.setWidth("8em");
        haberTxt.setValue(0.00);

        haber1Txt = new NumberField();
        haber1Txt.setDecimalAllowed(true);
        haber1Txt.setDecimalPrecision(2);
        haber1Txt.setMinimumFractionDigits(2);
        haber1Txt.setDecimalSeparator('.');
        haber1Txt.setDecimalSeparatorAlwaysShown(true);
        haber1Txt.setValue(0d);
        haber1Txt.setGroupingUsed(true);
        haber1Txt.setGroupingSeparator(',');
        haber1Txt.setGroupingSize(3);
        haber1Txt.setImmediate(true);
        haber1Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber1Txt.setWidth("8em");
        haber1Txt.setValue(0.00);

        haber2Txt = new NumberField();
        haber2Txt.setDecimalAllowed(true);
        haber2Txt.setDecimalPrecision(2);
        haber2Txt.setMinimumFractionDigits(2);
        haber2Txt.setDecimalSeparator('.');
        haber2Txt.setDecimalSeparatorAlwaysShown(true);
        haber2Txt.setValue(0d);
        haber2Txt.setGroupingUsed(true);
        haber2Txt.setGroupingSeparator(',');
        haber2Txt.setGroupingSize(3);
        haber2Txt.setImmediate(true);
        haber2Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber2Txt.setWidth("8em");
        haber2Txt.setValue(0.00);

        haber3Txt = new NumberField();
        haber3Txt.setDecimalAllowed(true);
        haber3Txt.setDecimalPrecision(2);
        haber3Txt.setMinimumFractionDigits(2);
        haber3Txt.setDecimalSeparator('.');
        haber3Txt.setDecimalSeparatorAlwaysShown(true);
        haber3Txt.setValue(0d);
        haber3Txt.setGroupingUsed(true);
        haber3Txt.setGroupingSeparator(',');
        haber3Txt.setGroupingSize(3);
        haber3Txt.setImmediate(true);
        haber3Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber3Txt.setWidth("8em");
        haber3Txt.setValue(0.00);

        haber4Txt = new NumberField();
        haber4Txt.setDecimalAllowed(true);
        haber4Txt.setDecimalPrecision(2);
        haber4Txt.setMinimumFractionDigits(2);
        haber4Txt.setDecimalSeparator('.');
        haber4Txt.setDecimalSeparatorAlwaysShown(true);
        haber4Txt.setValue(0d);
        haber4Txt.setGroupingUsed(true);
        haber4Txt.setGroupingSeparator(',');
        haber4Txt.setGroupingSize(3);
        haber4Txt.setImmediate(true);
        haber4Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber4Txt.setWidth("8em");
        haber4Txt.setValue(0.00);

        haber5Txt = new NumberField();
        haber5Txt.setDecimalAllowed(true);
        haber5Txt.setDecimalPrecision(2);
        haber5Txt.setMinimumFractionDigits(2);
        haber5Txt.setDecimalSeparator('.');
        haber5Txt.setDecimalSeparatorAlwaysShown(true);
        haber5Txt.setValue(0d);
        haber5Txt.setGroupingUsed(true);
        haber5Txt.setGroupingSeparator(',');
        haber5Txt.setGroupingSize(3);
        haber5Txt.setImmediate(true);
        haber5Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber5Txt.setWidth("8em");
        haber5Txt.setValue(0.00);

        haber6Txt = new NumberField();
        haber6Txt.setDecimalAllowed(true);
        haber6Txt.setDecimalPrecision(2);
        haber6Txt.setMinimumFractionDigits(2);
        haber6Txt.setDecimalSeparator('.');
        haber6Txt.setDecimalSeparatorAlwaysShown(true);
        haber6Txt.setValue(0d);
        haber6Txt.setGroupingUsed(true);
        haber6Txt.setGroupingSeparator(',');
        haber6Txt.setGroupingSize(3);
        haber6Txt.setImmediate(true);
        haber6Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber6Txt.setWidth("8em");
        haber6Txt.setValue(0.00);

        haber7Txt = new NumberField();
        haber7Txt.setDecimalAllowed(true);
        haber7Txt.setDecimalPrecision(2);
        haber7Txt.setMinimumFractionDigits(2);
        haber7Txt.setDecimalSeparator('.');
        haber7Txt.setDecimalSeparatorAlwaysShown(true);
        haber7Txt.setValue(0d);
        haber7Txt.setGroupingUsed(true);
        haber7Txt.setGroupingSeparator(',');
        haber7Txt.setGroupingSize(3);
        haber7Txt.setImmediate(true);
        haber7Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber7Txt.setWidth("8em");
        haber7Txt.setValue(0.00);

        haber8Txt = new NumberField();
        haber8Txt.setDecimalAllowed(true);
        haber8Txt.setDecimalPrecision(2);
        haber8Txt.setMinimumFractionDigits(2);
        haber8Txt.setDecimalSeparator('.');
        haber8Txt.setDecimalSeparatorAlwaysShown(true);
        haber8Txt.setValue(0d);
        haber8Txt.setGroupingUsed(true);
        haber8Txt.setGroupingSeparator(',');
        haber8Txt.setGroupingSize(3);
        haber8Txt.setImmediate(true);
        haber8Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber8Txt.setWidth("8em");
        haber8Txt.setValue(0.00);

        debeTxt = new NumberField("DEBE :");
        debeTxt.setDecimalAllowed(true);
        debeTxt.setDecimalPrecision(2);
        debeTxt.setMinimumFractionDigits(2);
        debeTxt.setDecimalSeparator('.');
        debeTxt.setDecimalSeparatorAlwaysShown(true);
        debeTxt.setValue(0d);
        debeTxt.setGroupingUsed(true);
        debeTxt.setGroupingSeparator(',');
        debeTxt.setGroupingSize(3);
        debeTxt.setImmediate(true);
        debeTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debeTxt.setWidth("8em");
        debeTxt.setValue(0.00);

        debe1Txt = new NumberField();
        debe1Txt.setDecimalAllowed(true);
        debe1Txt.setDecimalPrecision(2);
        debe1Txt.setMinimumFractionDigits(2);
        debe1Txt.setDecimalSeparator('.');
        debe1Txt.setDecimalSeparatorAlwaysShown(true);
        debe1Txt.setValue(0d);
        debe1Txt.setGroupingUsed(true);
        debe1Txt.setGroupingSeparator(',');
        debe1Txt.setGroupingSize(3);
        debe1Txt.setImmediate(true);
        debe1Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe1Txt.setWidth("8em");
        debe1Txt.setValue(0.00);

        debe2Txt = new NumberField();
        debe2Txt.setDecimalAllowed(true);
        debe2Txt.setDecimalPrecision(2);
        debe2Txt.setMinimumFractionDigits(2);
        debe2Txt.setDecimalSeparator('.');
        debe2Txt.setDecimalSeparatorAlwaysShown(true);
        debe2Txt.setValue(0d);
        debe2Txt.setGroupingUsed(true);
        debe2Txt.setGroupingSeparator(',');
        debe2Txt.setGroupingSize(3);
        debe2Txt.setImmediate(true);
        debe2Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe2Txt.setWidth("8em");
        debe2Txt.setValue(0.00);

        debe3Txt = new NumberField();
        debe3Txt.setDecimalAllowed(true);
        debe3Txt.setDecimalPrecision(2);
        debe3Txt.setMinimumFractionDigits(2);
        debe3Txt.setDecimalSeparator('.');
        debe3Txt.setDecimalSeparatorAlwaysShown(true);
        debe3Txt.setValue(0d);
        debe3Txt.setGroupingUsed(true);
        debe3Txt.setGroupingSeparator(',');
        debe3Txt.setGroupingSize(3);
        debe3Txt.setImmediate(true);
        debe3Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe3Txt.setWidth("8em");
        debe3Txt.setValue(0.00);

        debe4Txt = new NumberField();
        debe4Txt.setDecimalAllowed(true);
        debe4Txt.setDecimalPrecision(2);
        debe4Txt.setMinimumFractionDigits(2);
        debe4Txt.setDecimalSeparator('.');
        debe4Txt.setDecimalSeparatorAlwaysShown(true);
        debe4Txt.setValue(0d);
        debe4Txt.setGroupingUsed(true);
        debe4Txt.setGroupingSeparator(',');
        debe4Txt.setGroupingSize(3);
        debe4Txt.setImmediate(true);
        debe4Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe4Txt.setWidth("8em");
        debe4Txt.setValue(0.00);

        debe5Txt = new NumberField();
        debe5Txt.setDecimalAllowed(true);
        debe5Txt.setDecimalPrecision(2);
        debe5Txt.setMinimumFractionDigits(2);
        debe5Txt.setDecimalSeparator('.');
        debe5Txt.setDecimalSeparatorAlwaysShown(true);
        debe5Txt.setValue(0d);
        debe5Txt.setGroupingUsed(true);
        debe5Txt.setGroupingSeparator(',');
        debe5Txt.setGroupingSize(3);
        debe5Txt.setImmediate(true);
        debe5Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe5Txt.setWidth("8em");
        debe5Txt.setValue(0.00);

        debe6Txt = new NumberField();
        debe6Txt.setDecimalAllowed(true);
        debe6Txt.setDecimalPrecision(2);
        debe6Txt.setMinimumFractionDigits(2);
        debe6Txt.setDecimalSeparator('.');
        debe6Txt.setDecimalSeparatorAlwaysShown(true);
        debe6Txt.setValue(0d);
        debe6Txt.setGroupingUsed(true);
        debe6Txt.setGroupingSeparator(',');
        debe6Txt.setGroupingSize(3);
        debe6Txt.setImmediate(true);
        debe6Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe6Txt.setWidth("8em");
        debe6Txt.setValue(0.00);

        debe7Txt = new NumberField();
        debe7Txt.setDecimalAllowed(true);
        debe7Txt.setDecimalPrecision(2);
        debe7Txt.setMinimumFractionDigits(2);
        debe7Txt.setDecimalSeparator('.');
        debe7Txt.setDecimalSeparatorAlwaysShown(true);
        debe7Txt.setValue(0d);
        debe7Txt.setGroupingUsed(true);
        debe7Txt.setGroupingSeparator(',');
        debe7Txt.setGroupingSize(3);
        debe7Txt.setImmediate(true);
        debe7Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe7Txt.setWidth("8em");
        debe7Txt.setValue(0.00);

        debe8Txt = new NumberField();
        debe8Txt.setDecimalAllowed(true);
        debe8Txt.setDecimalPrecision(2);
        debe8Txt.setMinimumFractionDigits(2);
        debe8Txt.setDecimalSeparator('.');
        debe8Txt.setDecimalSeparatorAlwaysShown(true);
        debe8Txt.setValue(0d);
        debe8Txt.setGroupingUsed(true);
        debe8Txt.setGroupingSeparator(',');
        debe8Txt.setGroupingSize(3);
        debe8Txt.setImmediate(true);
        debe8Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe8Txt.setWidth("8em");
        debe8Txt.setValue(0.00);

        grabarBtn = new Button("Grabar");
        grabarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarBtn.setIcon(FontAwesome.SAVE);
        grabarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertTablaFactura();
            }
        });

        cerrarFacturasBtn = new Button("Cerrar factura");
        cerrarFacturasBtn.setIcon(FontAwesome.CHECK);
        cerrarFacturasBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                //comprobarBalance();
            }
        });

        layoutHorizontal.addComponent(cuentaContableCbx);
        layoutHorizontal.addComponent(debeTxt);
        layoutHorizontal.addComponent(haberTxt);
        layoutHorizontal.addComponent(tipoVentaCbx);

        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);
        layoutHorizontal1.addComponent(haber1Txt);
        layoutHorizontal1.addComponent(tipoVenta1Cbx);

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);
        layoutHorizontal2.addComponent(tipoVenta2Cbx);

        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);
        layoutHorizontal3.addComponent(haber3Txt);
        layoutHorizontal3.addComponent(tipoVenta3Cbx);

        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);
        layoutHorizontal4.addComponent(haber4Txt);
        layoutHorizontal4.addComponent(tipoVenta4Cbx);

        layoutHorizontal5.addComponent(cuentaContable5Cbx);
        layoutHorizontal5.addComponent(debe5Txt);
        layoutHorizontal5.addComponent(haber5Txt);
        layoutHorizontal5.addComponent(tipoVenta5Cbx);

        layoutHorizontal6.addComponent(cuentaContable6Cbx);
        layoutHorizontal6.addComponent(debe6Txt);
        layoutHorizontal6.addComponent(haber6Txt);
        layoutHorizontal6.addComponent(tipoVenta6Cbx);

        layoutHorizontal7.addComponent(cuentaContable7Cbx);
        layoutHorizontal7.addComponent(debe7Txt);
        layoutHorizontal7.addComponent(haber7Txt);
        layoutHorizontal7.addComponent(tipoVenta7Cbx);

        layoutHorizontal8.addComponent(cuentaContable8Cbx);
        layoutHorizontal8.addComponent(debe8Txt);
        layoutHorizontal8.addComponent(haber8Txt);
        layoutHorizontal8.addComponent(tipoVenta8Cbx);

        layoutHorizontal9.addComponent(grabarBtn);
        layoutHorizontal9.setComponentAlignment(grabarBtn, Alignment.BOTTOM_CENTER);

        contenedorLayout.addComponent(layoutHorizontal);
        contenedorLayout.setComponentAlignment(layoutHorizontal, Alignment.MIDDLE_CENTER);

        contenedorLayout.addComponent(layoutHorizontal1);
        contenedorLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        contenedorLayout.addComponent(layoutHorizontal2);
        contenedorLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        contenedorLayout.addComponent(layoutHorizontal3);
        contenedorLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_CENTER);

        contenedorLayout.addComponent(layoutHorizontal4);
        contenedorLayout.setComponentAlignment(layoutHorizontal4, Alignment.MIDDLE_CENTER);

        contenedorLayout.addComponent(layoutHorizontal5);
        contenedorLayout.setComponentAlignment(layoutHorizontal5, Alignment.MIDDLE_CENTER);

        contenedorLayout.addComponent(layoutHorizontal6);
        contenedorLayout.setComponentAlignment(layoutHorizontal6, Alignment.MIDDLE_CENTER);

        contenedorLayout.addComponent(layoutHorizontal7);
        contenedorLayout.setComponentAlignment(layoutHorizontal7, Alignment.MIDDLE_CENTER);

        contenedorLayout.addComponent(layoutHorizontal8);
        contenedorLayout.setComponentAlignment(layoutHorizontal8, Alignment.MIDDLE_CENTER);

        contenedorLayout.addComponent(layoutHorizontal9);
        contenedorLayout.setComponentAlignment(layoutHorizontal9, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(contenedorLayout);
        mainLayout.setComponentAlignment(contenedorLayout, Alignment.MIDDLE_CENTER);

    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT * from contabilidad_nomenclatura";
        queryString += " where FiltrarIngresoDocumentos = 'S'";
        queryString += " and Estatus = 'HABILITADA'";
        queryString += " and IdNomenclatura <> " +  ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar(); // solicitado por Shwaony 1/9/2021
        queryString += " Order By N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado          

                cuentaContableCbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable1Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable1Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable2Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable2Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable3Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable3Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable4Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable4Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable5Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable5Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable6Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable6Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable7Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable7Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable8Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable8Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), "(" + rsRecords.getString("IdEmpresa") + ") " + rsRecords.getString("Empresa"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertTablaFactura() {

        totalMonto = new BigDecimal(montoTxt.getDoubleValueDoNotThrow())
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe = new BigDecimal(debeTxt.getDoubleValueDoNotThrow()
                + debe1Txt.getDoubleValueDoNotThrow() + debe2Txt.getDoubleValueDoNotThrow()
                + debe3Txt.getDoubleValueDoNotThrow() + debe4Txt.getDoubleValueDoNotThrow()
                + debe5Txt.getDoubleValueDoNotThrow() + debe6Txt.getDoubleValueDoNotThrow()
                + debe7Txt.getDoubleValueDoNotThrow() + debe8Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = new BigDecimal(haberTxt.getDoubleValueDoNotThrow()
                + haber1Txt.getDoubleValueDoNotThrow() + haber2Txt.getDoubleValueDoNotThrow()
                + haber3Txt.getDoubleValueDoNotThrow() + haber4Txt.getDoubleValueDoNotThrow()
                + haber5Txt.getDoubleValueDoNotThrow() + haber6Txt.getDoubleValueDoNotThrow()
                + haber7Txt.getDoubleValueDoNotThrow() + haber8Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalMonto.setScale(2, BigDecimal.ROUND_HALF_UP);

        try {
            Utileria utileria = new Utileria();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            Date fechaInicial = dateFormat.parse(utileria.getFecha());
            Date fechaFinal = dateFormat.parse(utileria.getFechaYYYYMMDD(fechaDt.getValue()));

            int dias = (int) ((fechaInicial.getTime() - fechaFinal.getTime()) / 86400000);

            System.out.println("Hay " + dias + " dias de diferencia");

            if (dias > 30) {

                if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken().isEmpty()) {
                    ValidarTokenForm validarTokenForm = new ValidarTokenForm(false);
                    UI.getCurrent().addWindow(validarTokenForm);
                    validarTokenForm.center();
                    return;
                } else {
                    System.out.println("ver si esta entrado ");
                    variableTemp = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken();
                    ((SopdiUI) UI.getCurrent()).sessionInformation.setStrUserToken("");
                }

            }

        } catch (Exception e) {
            System.out.println("Error validar fechas " + e);
            e.printStackTrace();

        }

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresaCbx.getValue())), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }

        if (totalDebe.doubleValue() != totalHaber.doubleValue()) {
            System.out.println("Debe =" + totalDebe.doubleValue() + "  haber=" + totalHaber);
            Notification.show("La partida es descuadrada, por favor revisar"
                    + " Debe = " + totalDebe.doubleValue() + "  Haber = " + totalHaber, Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (cuentaContable1Cbx.getValue() == null && debe1Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable2Cbx.getValue() == null && debe2Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable3Cbx.getValue() == null && debe3Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable4Cbx.getValue() == null && debe4Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable5Cbx.getValue() == null && debe5Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable6Cbx.getValue() == null && debe6Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable7Cbx.getValue() == null && debe7Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable8Cbx.getValue() == null && debe8Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }

        if (cuentaContable1Cbx.getValue() == null && haber1Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable2Cbx.getValue() == null && haber2Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable3Cbx.getValue() == null && haber3Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable4Cbx.getValue() == null && haber4Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable5Cbx.getValue() == null && haber5Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable6Cbx.getValue() == null && haber6Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable7Cbx.getValue() == null && haber8Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable8Cbx.getValue() == null && haber8Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }

        queryString = " DELETE from contabilidad_partida ";
        queryString += " where CodigoPartida = '" + codigoPartidaEdit + "'";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
        } catch (SQLException ex) {
            System.out.println("Error al intentar eliminar registros " + ex);
            ex.printStackTrace();
        }

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
        queryString += " TipoDocumento, TipoVenta, Fecha, IdProveedor, NITProveedor, NombreProveedor, Referencia,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion,";
        queryString += " CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre)";
        queryString += " Values ";

        queryString += "(";
        queryString += empresaCbx.getValue();
        queryString += ",'" + estatusPartidaEdit + "'";
        queryString += ",'" + codigoPartidaEdit + "'";
        queryString += ",'" + codigoPartidaEdit + "'";
        queryString += ", '" + tipoDocumento + "'";
        queryString += ",'" + String.valueOf(tipoVentaCbx.getValue()) + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + nitProveedotTxt.getValue() + "'";
        queryString += ",'" + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
        if (checkbox1.getValue().equals(true)) {
            queryString += ",'SI'";
        } else {
            queryString += ",'NO'";
        }
        queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += "," + cuentaContableCbx.getValue();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + String.valueOf(debeTxt.getDoubleValueDoNotThrow()); //DEBE
        queryString += "," + String.valueOf(haberTxt.getDoubleValueDoNotThrow()); //HABER
        queryString += "," + String.valueOf(debeTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(haberTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q.
        queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
        queryString += ",'" + tipoDocumento + " " + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        if (nombreArchvivo == null && tipoArchivo == null) {
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";

        } else {
            queryString += ",?";
            queryString += ",'" + tipoArchivo + "'";
            queryString += "," + pesoArchivo;
            queryString += ",'" + nombreArchvivo + "'";

        }

        queryString += ")";

        //// INGRESO DEL HABER
        if ((cuentaContable1Cbx.getValue() != null && debe1Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable1Cbx.getValue() != null && haber1Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ", '" + tipoDocumento + "'";
            queryString += ",'" + String.valueOf(tipoVenta1Cbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable1Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());//DEBE Q.
            queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + tipoDocumento + " " + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            if (nombreArchvivo == null && tipoArchivo == null) {
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";

            } else {
                queryString += ",?";
                queryString += ",'" + tipoArchivo + "'";
                queryString += "," + pesoArchivo;
                queryString += ",'" + nombreArchvivo + "'";

            }
            queryString += ")";
        }

//// segundo  ingreso
        if ((cuentaContable2Cbx.getValue() != null && debe2Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable2Cbx.getValue() != null && haber2Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ", '" + tipoDocumento + "'";
            queryString += ",'" + String.valueOf(tipoVenta2Cbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable2Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());//DEBE Q.
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + tipoDocumento + " " + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            if (nombreArchvivo == null && tipoArchivo == null) {
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";

            } else {
                queryString += ",?";
                queryString += ",'" + tipoArchivo + "'";
                queryString += "," + pesoArchivo;
                queryString += ",'" + nombreArchvivo + "'";

            }
            queryString += ")";
        }

//// tercer ingreso
        if ((cuentaContable3Cbx.getValue() != null && debe3Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable3Cbx.getValue() != null && haber3Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ", '" + tipoDocumento + "'";
            queryString += ",'" + String.valueOf(tipoVenta3Cbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable3Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());//DEBE Q.
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + tipoDocumento + " " + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            if (nombreArchvivo == null && tipoArchivo == null) {
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";

            } else {
                queryString += ",?";
                queryString += ",'" + tipoArchivo + "'";
                queryString += "," + pesoArchivo;
                queryString += ",'" + nombreArchvivo + "'";

            }
            queryString += ")";
        }

//// cuarto ingreso
        if ((cuentaContable4Cbx.getValue() != null && debe4Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable4Cbx.getValue() != null && haber4Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ", '" + tipoDocumento + "'";
            queryString += ",'" + String.valueOf(tipoVenta4Cbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable4Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());//DEBE Q.
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + tipoDocumento + " " + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            if (nombreArchvivo == null && tipoArchivo == null) {
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";

            } else {
                queryString += ",?";
                queryString += ",'" + tipoArchivo + "'";
                queryString += "," + pesoArchivo;
                queryString += ",'" + nombreArchvivo + "'";

            }
            queryString += ")";
        }
//// quinto ingreso
        if ((cuentaContable5Cbx.getValue() != null && debe5Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable5Cbx.getValue() != null && haber5Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ", '" + tipoDocumento + "'";
            queryString += ",'" + String.valueOf(tipoVenta5Cbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable5Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());//DEBE Q.
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + tipoDocumento + " " + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            if (nombreArchvivo == null && tipoArchivo == null) {
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";

            } else {
                queryString += ",?";
                queryString += ",'" + tipoArchivo + "'";
                queryString += "," + pesoArchivo;
                queryString += ",'" + nombreArchvivo + "'";

            }
            queryString += ")";
        }
//// sexto ingreso
        if ((cuentaContable6Cbx.getValue() != null && debe6Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable6Cbx.getValue() != null && haber6Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ", '" + tipoDocumento + "'";
            queryString += ",'" + String.valueOf(tipoVenta6Cbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable6Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());//DEBE Q.
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + tipoDocumento + " " + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            if (nombreArchvivo == null && tipoArchivo == null) {
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";

            } else {
                queryString += ",?";
                queryString += ",'" + tipoArchivo + "'";
                queryString += "," + pesoArchivo;
                queryString += ",'" + nombreArchvivo + "'";

            }
            queryString += ")";
        }
//// septimo ingreso
        if ((cuentaContable7Cbx.getValue() != null && debe7Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable7Cbx.getValue() != null && haber7Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ", '" + tipoDocumento + "'";
            queryString += ",'" + String.valueOf(tipoVenta7Cbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable7Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());//DEBE Q.
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + tipoDocumento + " " + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            if (nombreArchvivo == null && tipoArchivo == null) {
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";

            } else {
                queryString += ",?";
                queryString += ",'" + tipoArchivo + "'";
                queryString += "," + pesoArchivo;
                queryString += ",'" + nombreArchvivo + "'";

            }
            queryString += ")";
        }
//// octavo ingreso
        if ((cuentaContable8Cbx.getValue() != null && debe8Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable8Cbx.getValue() != null && haber8Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ",'" + codigoPartidaEdit + "'";
            queryString += ", '" + tipoDocumento + "'";
            queryString += ",'" + String.valueOf(tipoVenta8Cbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable8Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());//DEBE Q.
            queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + tipoDocumento + " " + String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            if (nombreArchvivo == null && tipoArchivo == null) {
                queryString += ",null";
                queryString += ",null";
                queryString += ",0";
                queryString += ",null";

            } else {
                queryString += ",?";
                queryString += ",'" + tipoArchivo + "'";
                queryString += "," + pesoArchivo;
                queryString += ",'" + nombreArchvivo + "'";

            }
            queryString += ")";
        }

        System.out.println(queryString);

        try {
            if (nombreArchvivo == null && tipoArchivo == null) {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);
            } else {
                stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
                stPreparedQuery.setBinaryStream(1, documentStreamResource.getStream().getStream(), documentStreamResource.getStream().getStream().available());
                stPreparedQuery.executeUpdate();
            }
            
            if(!variableTemp.isEmpty()){
                cambiarEstatusToken(codigoPartidaEdit);
            }

            Notification notif = new Notification("REGISTRO AGREGADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta(String.valueOf(empresaCbx.getValue()));
            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar facturas  : " + ex1.getMessage());
            ex1.printStackTrace();

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
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

    public void llenarComboProveedor() {
        queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsCliente = 1";
        queryString += " Order By Nombre";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NOMBRESINCODIGO_PROPERTY).setValue(rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar de clientes (tabla proveedores) " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }
    
    public void cambiarEstatusToken(String codigoPartida){
        
        try {
            queryString = "UPDATE token SET ";
            queryString += " IdUsuario = " +((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ", Usuario = '" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName() + "'";
            queryString += ", FechaUsado = current_timestamp";
            queryString += ", CodigoPartida = '" + codigoPartida +"'";
            queryString += ", Estatus = 'UTILIZADO'";
            queryString += " Where Codigo = '" + variableTemp +"'";
            
            variableTemp = "";

            stQuery.executeUpdate(queryString);
            
        } catch (Exception e) {
            System.out.println("Error al intentar cambiar estatus token : " + e);
            e.printStackTrace();
        }                
        
    }
}
