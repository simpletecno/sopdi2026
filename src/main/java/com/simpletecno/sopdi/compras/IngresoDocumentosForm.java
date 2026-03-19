package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.contabilidad.*;
import com.vaadin.data.Item;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.vaadin.ui.NumberField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.mail.MessagingException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author user
 */
public class IngresoDocumentosForm extends Window {

    MultiFileUpload singleUpload;

    ComboBox porContabilizarCbx = new ComboBox("Por contabilizar");
    BigDecimal totalDebe;
    VerticalLayout mainLayout;

    public ComboBox tipoDocumentoCbx;
    public DateField fechaDt;
    TextField serieTxt;
    TextField numeroTxt;
    TextField nitProveedotTxt;
    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox ordenCompraCbx;
    ComboBox centroCostoCbx;
    ComboBox cuentaContableHaberCbx;
    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    ComboBox cuentaContable5Cbx;
    ComboBox cuentaContable6Cbx;
    ComboBox cuentaContable7Cbx;
    ComboBox cuentaContable8Cbx;
    ComboBox cuentaContable9Cbx;
    ComboBox cuentaContable10Cbx;

    ComboBox tipoCbx;
    NumberField exentoTxt;
    Label baseLbl = new Label();
    Label ivaLbl = new Label();

    public NumberField montoTxt;
    NumberField tasaCambioTxt;
    NumberField haber1Txt;
    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;
    NumberField debe5Txt;
    NumberField debe6Txt;
    NumberField debe7Txt;
    NumberField debe8Txt;
    NumberField debe9Txt;
    NumberField debe10Txt;

    TextField centroCosto1Txt;
    TextField centroCosto2Txt;
    TextField centroCosto3Txt;
    TextField centroCosto4Txt;
    TextField centroCosto5Txt;
    TextField centroCosto6Txt;
    TextField centroCosto7Txt;
    TextField centroCosto8Txt;
    TextField centroCosto9Txt;
    TextField centroCosto10Txt;

    Button grabarBtn;
    Button saldosBtn;

    CheckBox hacerRetencionIsrChk;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords1;
    String queryString;
    String variableTemp = "";
    static PreparedStatement stPreparedQuery;

    static final String NIT_PROPERTY = "NIT";
    static final String GRUPO_PROPERTY = "GRUPO";
    static final String NOMBRESINCODIGO_PROPERTY = "NombreSinCodigo";

    String FILENAME = "/Users/joseaguirre/Download/81848889-FACT-328AC577-1399343837.xml";
    File xmlFile;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public IngresoDocumentosForm() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("98%");
        setHeight("98%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setWidth("100%");
        
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

                xmlFile = targetFile;

                findAndProcessXmlFile();

                //   cargarBtn.setEnabled(true);
            } catch (java.io.IOException fIoEx) {
                fIoEx.printStackTrace();
                Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                return;
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler, window, false);
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Abrir archivo XML", "");

        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.xml')");

        List<String> acceptedMimeTypes = new ArrayList();
        acceptedMimeTypes.add("application/xml");

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " INGRESO DE DOCUMENTOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setWidth("1005");
        titleLbl.addStyleName("h2_custom");
        
        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);
        
        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        mainLayout.addComponent(crearComponentes());

    }

    private void llenarComboPorContabilizar() {
        porContabilizarCbx.removeAllItems();

        queryString = " SELECT *";
        queryString += " FROM documentos_fel_sat ";
        queryString += " WHERE Estatus = 'ACTIVA' ";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND Contabilizada = 'N'";
        queryString += " AND UPPER(Accion) = 'PROVEEDOR'";

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if(rsRecords1.next()) {
/**
 IdEmpresa,FechaEmision, NumeroAutorizacion, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor,
 CodigoEstablecimiento, NombreEstablecimiento,IdReceptor, NombreReceptor, NitCertificador, NombreCertificador,
 Moneda, Monto, Estatus, IVA,IDP, TurismoHospedaje,  TurismoPasajes, TimbrePrensa, Bomberos, BebidasAlcoholicas,
 Tabaco, Cemento, BebidasNoAlcoholicas, TarifaPortuaria, FechaCertificacion, Costo
 **/
                do {
                    porContabilizarCbx.addItem(rsRecords1.getString("Id"));
                    porContabilizarCbx.setItemCaption(rsRecords1.getString("Id"), rsRecords1.getString("NombreProveedor") + " " + rsRecords1.getString("Serie") + " " + rsRecords1.getString("Numero"));

                } while(rsRecords1.next());

            }
        } catch (Exception ex1) {
            new Notification("Error al intentar leer registros de tabla documentos_fel_sat.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
        }

    }

    private void llenarFormularioPorContabilizar() {

        tipoDocumentoCbx.setEnabled(true);
        centroCostoCbx.setEnabled(true);
        fechaDt.setEnabled(true);
        proveedorCbx.setEnabled(true);
        serieTxt.setEnabled(true);
        numeroTxt.setEnabled(true);
        montoTxt.setEnabled(true);
        monedaCbx.setEnabled(true);

        tipoDocumentoCbx.select("FACTURA");
        centroCostoCbx.select(null);
        serieTxt.setValue("");
        numeroTxt.setValue("");
        montoTxt.setValue(0.00);
        nitProveedotTxt.setValue("");
        proveedorCbx.select(null);

        if(porContabilizarCbx.getValue() == null) {
            return;
        }

        queryString = " SELECT *";
        queryString += " FROM documentos_fel_sat ";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND Id = " + porContabilizarCbx.getValue();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if(rsRecords1.next()) {
/**
 IdEmpresa,FechaEmision, NumeroAutorizacion, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor,
 CodigoEstablecimiento, NombreEstablecimiento,IdReceptor, NombreReceptor, NitCertificador, NombreCertificador,
 Moneda, Monto, Estatus, IVA,IDP, TurismoHospedaje,  TurismoPasajes, TimbrePrensa, Bomberos, BebidasAlcoholicas,
 Tabaco, Cemento, BebidasNoAlcoholicas, TarifaPortuaria, FechaCertificacion, Costo
 **/
                tipoDocumentoCbx.select(rsRecords1.getString("TipoDte"));
                centroCostoCbx.select(rsRecords1.getString("IdCentroCosto"));
                fechaDt.setValue(rsRecords1.getDate("FechaEmision"));
                proveedorCbx.select(rsRecords1.getString("IdProveedor"));
                serieTxt.setValue(rsRecords1.getString("Serie"));
                numeroTxt.setValue(rsRecords1.getString("Numero"));
                montoTxt.setValue(rsRecords1.getDouble("Monto"));
                monedaCbx.select(rsRecords1.getString("Moneda"));

                tipoDocumentoCbx.setEnabled(false);
                centroCostoCbx.setEnabled(false);
                fechaDt.setEnabled(false);
                proveedorCbx.setEnabled(false);
                nitProveedotTxt.setEnabled(false);
                serieTxt.setEnabled(false);
                numeroTxt.setEnabled(false);
                montoTxt.setEnabled(false);
                monedaCbx.setEnabled(false);

                baseLbl.setValue("BASE = " + Utileria.numberFormatMoney.format((rsRecords1.getDouble("Monto") - rsRecords1.getDouble("Iva"))));
                ivaLbl.setValue("IVA = " + Utileria.numberFormatMoney.format(rsRecords1.getDouble("Iva")));

                haber1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
                debe1Txt.setValue(Double.valueOf(Utileria.numberFormatEntero.format(
                        ((rsRecords1.getDouble("Monto") - rsRecords1.getDouble("Iva")) - exentoTxt.getDoubleValueDoNotThrow()))));

                if(rsRecords1.getDouble("Iva") > 0.00) {
                    cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar());
                    debe2Txt.setValue(Utileria.numberFormatEntero.format(rsRecords1.getDouble("Iva")));
                }

                double otrosImpuestos = rsRecords1.getDouble("IDP") + rsRecords1.getDouble("TurismoHospedaje");
                otrosImpuestos += rsRecords1.getDouble("TurismoPasajes") + rsRecords1.getDouble("TimbrePrensa");
                otrosImpuestos += rsRecords1.getDouble("Bomberos") + rsRecords1.getDouble("TasaMunicipal");
                otrosImpuestos += rsRecords1.getDouble("BebidasAlcoholicas");
                otrosImpuestos += rsRecords1.getDouble("Tabaco")  + rsRecords1.getDouble("Cemento");
                otrosImpuestos += rsRecords1.getDouble("BebidasNoAlcoholicas")  + rsRecords1.getDouble("TarifaPortuaria");
                if(otrosImpuestos  > 0.00) {
                    cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getOtrosArbitrios());
                    debe3Txt.setValue(Utileria.numberFormatEntero.format(otrosImpuestos));
                }

                queryString = " SELECT *";
                queryString += " FROM contabilidad_partida ";
                queryString += " WHERE IdEmpresa = " + empresaId;
                queryString += " AND   TipoDocumento = 'FACTURA VENTA'";
                queryString += " AND   Estatus <> 'ANULADO'";
                queryString += " AND   IdCentroCosto = " + centroCostoCbx.getValue();
//                queryString += " AND   Fecha < '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";

Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                rsRecords1 = stQuery1.executeQuery(queryString);

                if (rsRecords1.next()) {
                    if(rsRecords.getObject("Fecha") != null) {
                        if (fechaDt.getValue().before(rsRecords1.getDate("Fecha"))) {
                            cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCompras());
                        } else {
                            cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProvisionCompras());
                        }
                    }
                }
                else {
                    cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCompras());
                }

            }
        } catch (Exception ex1) {
            new Notification("Error al intentar leer registros de tabla documentos_fel_sat.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
        }

    }
    private void findAndProcessXmlFile() {

        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(xmlFile);

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
            System.out.println("------");

            if (!doc.getDocumentElement().getNodeName().equals("dte:GTDocumento")) {
                Notification.show("EL ARCHIVO XML NO ES UN DOCUMENTO TRIBUTARIO ELECTRONICO (FEL) SAT.", Notification.Type.ERROR_MESSAGE);
                return;
            }

            Element mainElement = doc.getDocumentElement();
            Node satNodeElement = mainElement.getChildNodes().item(0);
            Node dteNodeElement = satNodeElement.getFirstChild();
            Node emisionNodeElement = dteNodeElement.getFirstChild();
            Node generalesNodeElement = emisionNodeElement.getFirstChild();
            Node emisorNodeElement = emisionNodeElement.getChildNodes().item(1);
            Node totalNodeElement = emisionNodeElement.getChildNodes().item(5);
            Node certificacionNodeElement = dteNodeElement.getChildNodes().item(1);

            if (certificacionNodeElement == null) {
                Notification.show("Documento XML FEL, no está CERTIFICADO!!", Notification.Type.WARNING_MESSAGE);
                return;
            }

            fechaDt.setValue(new SimpleDateFormat("yyyy-MM-dd").parse(generalesNodeElement.getAttributes().getNamedItem("FechaHoraEmision").getNodeValue()));
            nitProveedotTxt.setValue(emisorNodeElement.getAttributes().getNamedItem("NITEmisor").getNodeValue());

            if (generalesNodeElement.getAttributes().getNamedItem("CodigoMoneda").getNodeValue().equals("GTQ")) {
                monedaCbx.select("QUETZALES");
            } else {
                monedaCbx.select("DOLARES");
            }
            if (!generalesNodeElement.getAttributes().getNamedItem("Tipo").getNodeValue().equals("FACT")) {
                Notification.show("Este documento no es una FACTURA, solo se permite cargar facturas..", Notification.Type.ERROR_MESSAGE);
                return;
            }

            serieTxt.setValue(certificacionNodeElement.getChildNodes().item(2).getAttributes().getNamedItem("Serie").getNodeValue());
            numeroTxt.setValue(certificacionNodeElement.getChildNodes().item(2).getAttributes().getNamedItem("Numero").getNodeValue());

            montoTxt.setValue(Double.valueOf(totalNodeElement.getLastChild().getTextContent()));

            cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar());

            debe2Txt.setValue(
                    Double.valueOf(
                            totalNodeElement.getFirstChild().getFirstChild().getAttributes().getNamedItem("TotalMontoImpuesto").getNodeValue()
                    ));

        } catch (Exception exp) {
            Notification.show("Error al intentar leer XML FEL..." + exp.getMessage(), Notification.Type.ERROR_MESSAGE);
            exp.printStackTrace();
        }

    }

    public HorizontalLayout crearComponentes() {

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addStyleName("rcorners2");
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(false);
        horizontalLayout.setWidth("100%");

        VerticalLayout leftVerticalLayout = new VerticalLayout();
        leftVerticalLayout.setWidth("100%");
        leftVerticalLayout.addStyleName("rcorners3");
        leftVerticalLayout.setSpacing(true);
        leftVerticalLayout.setMargin(false);

        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.setWidth("100%");
        rightVerticalLayout.addStyleName("rcorners3");
        rightVerticalLayout.setSpacing(true);
        rightVerticalLayout.setMargin(false);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_RIGHT);

        porContabilizarCbx.setWidth("100%");
        porContabilizarCbx.setNullSelectionAllowed(true);
        porContabilizarCbx.setInvalidAllowed(false);
        porContabilizarCbx.setNewItemsAllowed(false);
        porContabilizarCbx.addValueChangeListener(e -> {
            llenarFormularioPorContabilizar();
        });

        llenarComboPorContabilizar();

        ordenCompraCbx = new ComboBox();
        ordenCompraCbx.setInputPrompt("# Orden de compra");
        ordenCompraCbx.setDescription("# Orde de compra");
        ordenCompraCbx.setWidth("100%");
        llenarComboOrdenCompra();

        centroCostoCbx = new ComboBox();
        centroCostoCbx.setInputPrompt("Centro de costo");
        centroCostoCbx.setDescription("Centro de costo");
        centroCostoCbx.setWidth("100%");
        centroCostoCbx.setTextInputAllowed(false);
        centroCostoCbx.setNewItemsAllowed(false);
        centroCostoCbx.setNullSelectionAllowed(true);
        centroCostoCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            verificarCuentaContableAplicar();

            centroCosto1Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto2Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto3Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto4Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto5Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto6Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto7Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto8Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto9Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto10Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));

        });

        llenarComboCentroCosto();

        tipoDocumentoCbx = new ComboBox();
        tipoDocumentoCbx.setTextInputAllowed(false);
        tipoDocumentoCbx.setNewItemsAllowed(false);
        tipoDocumentoCbx.setNullSelectionAllowed(false);
        tipoDocumentoCbx.setInputPrompt("Tipo de documento");
        tipoDocumentoCbx.setDescription("Tipo de documento");
        tipoDocumentoCbx.setWidth("100%");
        tipoDocumentoCbx.addItem("FACTURA");
        tipoDocumentoCbx.addItem("RECIBO");
        tipoDocumentoCbx.addItem("RECIBO CONTABLE");
        tipoDocumentoCbx.addItem("RECIBO CONTABLE IGSS");
        tipoDocumentoCbx.addItem("RECIBO CORRIENTE");
        tipoDocumentoCbx.addItem("FORMULARIO IVA");
        tipoDocumentoCbx.addItem("FORMULARIO ISR");
        tipoDocumentoCbx.addItem("FORMULARIO ISR RETENIDO");
        tipoDocumentoCbx.addItem("FORMULARIO ISR OPCIONAL MENSUAL");
        tipoDocumentoCbx.addItem("FORMULARIO ISO");
        tipoDocumentoCbx.addItem("FORMULARIO RECTIFICACION");
        tipoDocumentoCbx.select("FACTURA");
        tipoDocumentoCbx.addValueChangeListener(event -> {
            saldosBtn.setVisible(false);
            if (String.valueOf(event.getProperty().getValue()).equals("FORMULARIO IVA")
                    || String.valueOf(event.getProperty().getValue()).equals("FORMULARIO ISR")
                    || String.valueOf(event.getProperty().getValue()).equals("FORMULARIO ISR RETENIDO")
                    || String.valueOf(event.getProperty().getValue()).equals("FORMULARIO ISR OPCIONAL MENSUAL")
                    || String.valueOf(event.getProperty().getValue()).equals("FORMULARIO ISO")
                    || String.valueOf(event.getProperty().getValue()).equals("RECIBO CONTABLE IGSS")
                    || String.valueOf(event.getProperty().getValue()).equals("RECIBO CONTABLE")
                    || String.valueOf(event.getProperty().getValue()).equals("FORMULARIO RECTIFICACION")) {
                tipoCbx.setVisible(false);
                cuentaContableHaberCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones());
                saldosBtn.setVisible(true);

                if (String.valueOf(event.getProperty().getValue()).equals("FORMULARIO IVA") && montoTxt.getDoubleValueDoNotThrow() == 0.00) {
                    IvaPorDeclararForm impuestosIva
                            = new IvaPorDeclararForm();
                    UI.getCurrent().addWindow(impuestosIva);
                    impuestosIva.center();
                    close();
                } else if (String.valueOf(event.getProperty().getValue()).equals("FORMULARIO ISR RETENIDO") && montoTxt.getDoubleValueDoNotThrow() == 0.00) {
                    IsrRetenidoPorDeclararForm isrRetenidoPorDeclararForm
                            = new IsrRetenidoPorDeclararForm();
                    UI.getCurrent().addWindow(isrRetenidoPorDeclararForm);
                    isrRetenidoPorDeclararForm.center();
                    close();
                } else if (String.valueOf(event.getProperty().getValue()).equals("RECIBO CONTABLE IGSS") && montoTxt.getDoubleValueDoNotThrow() == 0.00) {
                    IgssPorDeclararForm impuestosIgss
                            = new IgssPorDeclararForm();
                    UI.getCurrent().addWindow(impuestosIgss);
                    impuestosIgss.center();
                    close();
                } else if (String.valueOf(event.getProperty().getValue()).equals("FORMULARIO ISR OPCIONAL MENSUAL") && montoTxt.getDoubleValueDoNotThrow() == 0.00) {
                    IsrOpcionalMensualForm isrOpcionalMensualForm
                            = new IsrOpcionalMensualForm();
                    UI.getCurrent().addWindow(isrOpcionalMensualForm);
                    isrOpcionalMensualForm.center();
                    close();
                }

            } else {
                tipoCbx.setVisible(true);
                tipoCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());
                cuentaContableHaberCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());
            }
        });

        serieTxt = new TextField();
        serieTxt.setWidth("100%");
        serieTxt.addStyleName("mayusculas");
        serieTxt.setInputPrompt("Serie del documento");
        serieTxt.setDescription("Serie del documento");

        numeroTxt = new TextField();
        numeroTxt.setWidth("100%");
        numeroTxt.setInputPrompt("Número del documento");
        numeroTxt.setDescription("Número del documento");

        proveedorCbx = new ComboBox();
        proveedorCbx.setWidth("100%");
        proveedorCbx.setInputPrompt("Proveedor");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.addContainerProperty(GRUPO_PROPERTY, String.class, "");
        proveedorCbx.addContainerProperty(NOMBRESINCODIGO_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            verificarProveedor();
        });

        nitProveedotTxt = new TextField();
        nitProveedotTxt.setWidth("100%");
        nitProveedotTxt.setInputPrompt("Nit del proveedor");
        nitProveedotTxt.setDescription("Nit del proveedor");
        nitProveedotTxt.addValueChangeListener(event
                -> buscarProveedorPorNit()
        );

        fechaDt = new DateField();
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());
        fechaDt.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            verificarCuentaContableAplicar();
        });

        montoTxt = new NumberField();
        montoTxt.setInputPrompt("Monto del documento");
        montoTxt.setDescription("Monto del documento");
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
        montoTxt.addValueChangeListener(event -> {
            calcularBaseIva();
        });

        monedaCbx = new ComboBox();
        monedaCbx.setWidth("100%");
        monedaCbx.setInputPrompt("Moneda");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);
        monedaCbx.addValueChangeListener(evet -> {
            if (monedaCbx.getValue() != null) {
                if (monedaCbx.getValue().equals("DOLARES")) {
                    tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));
                } else {
                    tasaCambioTxt.setValue(1.00);
                }
            } else {
                tasaCambioTxt.setValue(1.00);
            }
        });

        tasaCambioTxt = new NumberField();
        tasaCambioTxt.setInputPrompt("Tasa de cambio");
        tasaCambioTxt.setDescription("Tasa de cambio");
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
        tasaCambioTxt.setWidth("100%");
        tasaCambioTxt.setValue(1.00);
        tasaCambioTxt.setNegativeAllowed(false);

        hacerRetencionIsrChk = new CheckBox("Retener IRS");
        hacerRetencionIsrChk.addStyleName(ValoTheme.CHECKBOX_LARGE);
        hacerRetencionIsrChk.setValue(false);

        leftVerticalLayout.addComponent(porContabilizarCbx);

        HorizontalLayout centroCostoOrdenComprasLayout = new HorizontalLayout();
        centroCostoOrdenComprasLayout.setSpacing(true);
        centroCostoOrdenComprasLayout.setMargin(false);
        centroCostoOrdenComprasLayout.setSizeFull();
        centroCostoOrdenComprasLayout.addComponents(centroCostoCbx, ordenCompraCbx);
//        centroCostoOrdenComprasLayout.setExpandRatio(centroCostoCbx, );

        leftVerticalLayout.addComponent(centroCostoOrdenComprasLayout);

        // TODO : quitar esta condicion ...
        if(empresaId.equals(11)) {
            centroCostoOrdenComprasLayout.setVisible(false);
        }

        HorizontalLayout tipoDocumentoFechaLayout = new HorizontalLayout();
        tipoDocumentoFechaLayout.setSpacing(true);
        tipoDocumentoFechaLayout.setMargin(false);
        tipoDocumentoFechaLayout.setSizeFull();
        tipoDocumentoFechaLayout.addComponents(tipoDocumentoCbx, fechaDt);

        leftVerticalLayout.addComponent(tipoDocumentoFechaLayout);

        HorizontalLayout documentoLayout = new HorizontalLayout();
        documentoLayout.setSpacing(true);
        documentoLayout.setMargin(false);
        documentoLayout.setSizeFull();
        documentoLayout.addComponents(serieTxt, numeroTxt);

        leftVerticalLayout.addComponent(documentoLayout);

        HorizontalLayout proveedorLayout = new HorizontalLayout();
        proveedorLayout.setSpacing(true);
        proveedorLayout.setMargin(false);
        proveedorLayout.setSizeFull();
        proveedorLayout.addComponents(proveedorCbx, nitProveedotTxt);
        proveedorLayout.setExpandRatio(proveedorCbx, 3.0f);
        proveedorLayout.setExpandRatio(nitProveedotTxt, 1.0f);

        leftVerticalLayout.addComponent(proveedorLayout);

        HorizontalLayout montoLayout = new HorizontalLayout();
        montoLayout.setSpacing(true);
        montoLayout.setMargin(false);
        montoLayout.setSizeFull();
        montoLayout.addComponents(montoTxt, monedaCbx, tasaCambioTxt,hacerRetencionIsrChk);
        montoLayout.setExpandRatio(montoTxt, 2.0f);
        montoLayout.setExpandRatio(monedaCbx, 3.0f);
        montoLayout.setExpandRatio(tasaCambioTxt, 1.0f);
        montoLayout.setExpandRatio(hacerRetencionIsrChk, 2.0f);
        montoLayout.setComponentAlignment(hacerRetencionIsrChk, Alignment.BOTTOM_RIGHT);

        leftVerticalLayout.addComponent(montoLayout);

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

        HorizontalLayout layoutHorizontal10 = new HorizontalLayout();
        layoutHorizontal10.setResponsive(true);
        layoutHorizontal10.setSpacing(true);

        HorizontalLayout layoutHorizontal11 = new HorizontalLayout();
        layoutHorizontal11.setResponsive(true);
        layoutHorizontal11.setSpacing(true);

        HorizontalLayout layoutHorizontal12 = new HorizontalLayout();
        layoutHorizontal12.setResponsive(true);
        layoutHorizontal12.setSpacing(true);

        HorizontalLayout layoutHorizontal13 = new HorizontalLayout();
        layoutHorizontal13.setResponsive(true);
        layoutHorizontal13.setSpacing(true);

        HorizontalLayout layoutHorizontal14 = new HorizontalLayout();
        layoutHorizontal14.setResponsive(true);
        layoutHorizontal14.setSpacing(true);

        tipoCbx = new ComboBox("Compra/Abasto :");
        tipoCbx.setInvalidAllowed(false);
        tipoCbx.setNullSelectionAllowed(false);
        tipoCbx.setNewItemsAllowed(false);
        tipoCbx.addContainerProperty("CODIGOCC", String.class, "");
        tipoCbx.addItem(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());
        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos() != null) {
            tipoCbx.addItem(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos());
        }
        tipoCbx.setItemCaption(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores(), "COMPRAS");
        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos() != null) {
            tipoCbx.setItemCaption(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos(), "ABASTO");
        }
        tipoCbx.getContainerProperty(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores(), "CODIGOCC").setValue("");
        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos() != null) {
            tipoCbx.getContainerProperty(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos(), "CODIGOCC").setValue(empresaId + "202104010000");
        }
        tipoCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());
        tipoCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            cuentaContableHaberCbx.select(String.valueOf(event.getProperty().getValue()));
        });

        saldosBtn = new Button("IMPUESTOS/IGGS");
        saldosBtn.setIcon(FontAwesome.SEARCH);
        saldosBtn.setVisible(false);
        saldosBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                ImpuestosYOtros impuestos
//                        = new ImpuestosYOtros();
//                UI.getCurrent().addWindow(impuestos);
//                impuestos.center();
            }
        });

        HorizontalLayout haberLayout = new HorizontalLayout();
        haberLayout.addComponents(tipoCbx, saldosBtn);
        haberLayout.setComponentAlignment(tipoCbx, Alignment.MIDDLE_CENTER);
        haberLayout.setComponentAlignment(saldosBtn, Alignment.MIDDLE_CENTER);

        exentoTxt = new NumberField("Monto Exento: ");
        exentoTxt.setDecimalAllowed(true);
        exentoTxt.setDecimalPrecision(2);
        exentoTxt.setMinimumFractionDigits(2);
        exentoTxt.setDecimalSeparator('.');
        exentoTxt.setDecimalSeparatorAlwaysShown(true);
        exentoTxt.setValue(0d);
        exentoTxt.setGroupingUsed(true);
        exentoTxt.setGroupingSeparator(',');
        exentoTxt.setGroupingSize(3);
        exentoTxt.setImmediate(true);
        exentoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        exentoTxt.setWidth("100%");
        exentoTxt.addValueChangeListener(event -> {
            calcularBaseIva();
        });
        exentoTxt.setReadOnly(true);

        baseLbl.addStyleName(ValoTheme.LABEL_SUCCESS);
        baseLbl.setValue("BASE = 0.00");
        ivaLbl.addStyleName(ValoTheme.LABEL_SUCCESS);
        ivaLbl.setValue("IVA = 0.00");

        HorizontalLayout layoutHorizontalIVA = new HorizontalLayout();
        layoutHorizontalIVA.setResponsive(true);
        layoutHorizontalIVA.setSpacing(true);
        layoutHorizontalIVA.addComponents(exentoTxt, baseLbl, ivaLbl);
        layoutHorizontalIVA.setComponentAlignment(exentoTxt, Alignment.MIDDLE_CENTER);
        layoutHorizontalIVA.setComponentAlignment(baseLbl, Alignment.BOTTOM_CENTER);
        layoutHorizontalIVA.setComponentAlignment(ivaLbl, Alignment.BOTTOM_CENTER);

        cuentaContableHaberCbx = new ComboBox("");
        cuentaContableHaberCbx.setWidth("29em");
        cuentaContableHaberCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableHaberCbx.setInvalidAllowed(false);
        cuentaContableHaberCbx.setNewItemsAllowed(false);

        cuentaContable1Cbx = new ComboBox("Cuentas contables DEBE : ");
        cuentaContable1Cbx.setWidth("29em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("29em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("29em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable3Cbx.setInvalidAllowed(false);
        cuentaContable3Cbx.setNewItemsAllowed(false);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("29em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable4Cbx.setInvalidAllowed(false);
        cuentaContable4Cbx.setNewItemsAllowed(false);

        cuentaContable5Cbx = new ComboBox();
        cuentaContable5Cbx.setWidth("29em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable5Cbx.setInvalidAllowed(false);
        cuentaContable5Cbx.setNewItemsAllowed(false);

        cuentaContable6Cbx = new ComboBox();
        cuentaContable6Cbx.setWidth("29em");
        cuentaContable6Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable6Cbx.setInvalidAllowed(false);
        cuentaContable6Cbx.setNewItemsAllowed(false);

        cuentaContable7Cbx = new ComboBox();
        cuentaContable7Cbx.setWidth("29em");
        cuentaContable7Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable7Cbx.setInvalidAllowed(false);
        cuentaContable7Cbx.setNewItemsAllowed(false);

        cuentaContable8Cbx = new ComboBox();
        cuentaContable8Cbx.setWidth("29em");
        cuentaContable8Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable8Cbx.setInvalidAllowed(false);
        cuentaContable8Cbx.setNewItemsAllowed(false);

        cuentaContable9Cbx = new ComboBox();
        cuentaContable9Cbx.setWidth("29em");
        cuentaContable9Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable9Cbx.setInvalidAllowed(false);
        cuentaContable9Cbx.setNewItemsAllowed(false);

        cuentaContable10Cbx = new ComboBox();
        cuentaContable10Cbx.setWidth("29em");
        cuentaContable10Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable10Cbx.setInvalidAllowed(false);
        cuentaContable10Cbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();
        llenarComboProveedor();

        haber1Txt = new NumberField("Haber : ");
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
        haber1Txt.setWidth("7em");
        haber1Txt.setValue(0.00);

        debe1Txt = new NumberField("Debe : ");
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
        debe1Txt.setWidth("7em");
        debe1Txt.setValue(0.00);
        debe1Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

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
        debe2Txt.setWidth("7em");
        debe2Txt.setValue(0.00);
        debe2Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

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
        debe3Txt.setWidth("7em");
        debe3Txt.setValue(0.00);
        debe3Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

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
        debe4Txt.setWidth("7em");
        debe4Txt.setValue(0.00);
        debe4Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

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
        debe5Txt.setWidth("7em");
        debe5Txt.setValue(0.00);
        debe5Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

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
        debe6Txt.setWidth("7em");
        debe6Txt.setValue(0.00);
        debe6Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

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
        debe7Txt.setWidth("7em");
        debe7Txt.setValue(0.00);
        debe7Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

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
        debe8Txt.setWidth("7em");
        debe8Txt.setValue(0.00);
        debe8Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

        debe9Txt = new NumberField();
        debe9Txt.setDecimalAllowed(true);
        debe9Txt.setDecimalPrecision(2);
        debe9Txt.setMinimumFractionDigits(2);
        debe9Txt.setDecimalSeparator('.');
        debe9Txt.setDecimalSeparatorAlwaysShown(true);
        debe9Txt.setValue(0d);
        debe9Txt.setGroupingUsed(true);
        debe9Txt.setGroupingSeparator(',');
        debe9Txt.setGroupingSize(3);
        debe9Txt.setImmediate(true);
        debe9Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe9Txt.setWidth("7em");
        debe9Txt.setValue(0.00);
        debe9Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

        debe10Txt = new NumberField();
        debe10Txt.setDecimalAllowed(true);
        debe10Txt.setDecimalPrecision(2);
        debe10Txt.setMinimumFractionDigits(2);
        debe10Txt.setDecimalSeparator('.');
        debe10Txt.setDecimalSeparatorAlwaysShown(true);
        debe10Txt.setValue(0d);
        debe10Txt.setGroupingUsed(true);
        debe10Txt.setGroupingSeparator(',');
        debe10Txt.setGroupingSize(3);
        debe10Txt.setImmediate(true);
        debe10Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe10Txt.setWidth("7em");
        debe10Txt.setValue(0.00);
        debe10Txt.addValueChangeListener(event -> {
            generarTotalDebe();
        });

        centroCosto1Txt = new NumberField("Centro costo");
        centroCosto2Txt = new NumberField();
        centroCosto3Txt = new NumberField();
        centroCosto4Txt = new NumberField();
        centroCosto5Txt = new NumberField();
        centroCosto6Txt = new NumberField();
        centroCosto7Txt = new NumberField();
        centroCosto8Txt = new NumberField();
        centroCosto9Txt = new NumberField();
        centroCosto10Txt = new NumberField();
//        centroCosto1Txt.addValidator(Validator.createStringLengthValidator("El centro de costo debe tener 6 caracteres", 6, 6, false));

        grabarBtn = new Button("Grabar");
        grabarBtn.setIcon(FontAwesome.SAVE);
        grabarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertPartidas();
            }
        });

        layoutHorizontal1.addComponent(cuentaContableHaberCbx);
        layoutHorizontal1.addComponent(haber1Txt);

        layoutHorizontal2.addComponent(cuentaContable1Cbx);
        layoutHorizontal2.addComponent(debe1Txt);
        layoutHorizontal2.addComponent(centroCosto1Txt);

        layoutHorizontal3.addComponent(cuentaContable2Cbx);
        layoutHorizontal3.addComponent(debe2Txt);
        layoutHorizontal3.addComponent(centroCosto2Txt);

        layoutHorizontal4.addComponent(cuentaContable3Cbx);
        layoutHorizontal4.addComponent(debe3Txt);
        layoutHorizontal4.addComponent(centroCosto3Txt);

        layoutHorizontal5.addComponent(cuentaContable4Cbx);
        layoutHorizontal5.addComponent(debe4Txt);
        layoutHorizontal5.addComponent(centroCosto4Txt);

        layoutHorizontal6.addComponent(cuentaContable5Cbx);
        layoutHorizontal6.addComponent(debe5Txt);
        layoutHorizontal6.addComponent(centroCosto5Txt);

        layoutHorizontal7.addComponent(cuentaContable6Cbx);
        layoutHorizontal7.addComponent(debe6Txt);
        layoutHorizontal7.addComponent(centroCosto6Txt);

        layoutHorizontal8.addComponent(cuentaContable7Cbx);
        layoutHorizontal8.addComponent(debe7Txt);
        layoutHorizontal8.addComponent(centroCosto7Txt);

        layoutHorizontal9.addComponent(cuentaContable8Cbx);
        layoutHorizontal9.addComponent(debe8Txt);
        layoutHorizontal9.addComponent(centroCosto8Txt);

        layoutHorizontal10.addComponent(cuentaContable9Cbx);
        layoutHorizontal10.addComponent(debe9Txt);
        layoutHorizontal10.addComponent(centroCosto9Txt);

        layoutHorizontal11.addComponent(cuentaContable10Cbx);
        layoutHorizontal11.addComponent(debe10Txt);
        layoutHorizontal11.addComponent(centroCosto10Txt);

        layoutHorizontal12.addComponents(grabarBtn);
        layoutHorizontal12.setComponentAlignment(grabarBtn, Alignment.BOTTOM_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontalIVA);
        rightVerticalLayout.setComponentAlignment(layoutHorizontalIVA, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal1);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal2);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal3);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal4);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal4, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal5);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal5, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal6);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal6, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal7);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal7, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal8);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal8, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal9);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal9, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal10);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal10, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal11);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal11, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal12);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal12, Alignment.MIDDLE_CENTER);

//        rightVerticalLayout.addComponent(layoutHorizontal13);
//        rightVerticalLayout.setComponentAlignment(layoutHorizontal13, Alignment.MIDDLE_CENTER);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);

        tipoCbx.setVisible(true);
        tipoCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());
        cuentaContableHaberCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());

        return horizontalLayout;
    }

    private void calcularBaseIva() {

        if (exentoTxt == null) {
            return;
        }
        if (montoTxt.getDoubleValueDoNotThrow() <= 0) {
            return;
        }

        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().toUpperCase().equals("EXENTA")) {
            exentoTxt.setReadOnly(false);
            exentoTxt.setValue(montoTxt.getDoubleValueDoNotThrow());
            exentoTxt.setReadOnly(true);
        }

        if(porContabilizarCbx.getValue() == null) {
            baseLbl.setValue("BASE = " + Utileria.numberFormatMoney.format((montoTxt.getDoubleValueDoNotThrow() - exentoTxt.getDoubleValueDoNotThrow()) / 1.12));
            ivaLbl.setValue("IVA = " + Utileria.numberFormatMoney.format(((montoTxt.getDoubleValueDoNotThrow() - exentoTxt.getDoubleValueDoNotThrow()) / 1.12) * .12));

            haber1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
            if (((montoTxt.getDoubleValueDoNotThrow() - exentoTxt.getDoubleValueDoNotThrow()) / 1.12) > 0) {
                debe1Txt.setValue(Double.valueOf(Utileria.numberFormatEntero.format((montoTxt.getDoubleValueDoNotThrow() - exentoTxt.getDoubleValueDoNotThrow()) / 1.12)));
            } else {
                debe1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
            }

            if (((montoTxt.getDoubleValueDoNotThrow() - exentoTxt.getDoubleValueDoNotThrow()) / 1.12) > 0) {
                cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar());
                debe2Txt.setValue(Utileria.numberFormatEntero.format(((montoTxt.getDoubleValueDoNotThrow() - exentoTxt.getDoubleValueDoNotThrow()) / 1.12) * .12));
            } else {
                cuentaContable2Cbx.unselect(cuentaContable2Cbx.getValue());
                debe2Txt.setValue(0.0d);
            }
        }
    }

    public void llenarComboOrdenCompra() {
        queryString = " SELECT * FROM orden_compra ";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                ordenCompraCbx.addItem(rsRecords.getString("Id"));
                ordenCompraCbx.setItemCaption(rsRecords.getString("Id"), rsRecords.getString("NOC"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar la tabla de orden de compra " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void verificarProveedor() {
        if (nitProveedotTxt == null) {
            return;
        }
        if (proveedorCbx == null) {
            return;
        }
        if (proveedorCbx.getValue() == null) {
            return;
        }

        try {
            Integer.valueOf(String.valueOf(proveedorCbx.getValue()));
        } catch (Exception strE) {
            return;
        }

        nitProveedotTxt.setValue("");

        nitProveedotTxt.setValue(String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NIT_PROPERTY).getValue()));

        if (String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(GRUPO_PROPERTY).getValue()).equals("Instituciones")) {
            tipoCbx.setVisible(false);
            cuentaContableHaberCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones());
        } else {
            tipoCbx.setVisible(true);
            tipoCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());
            cuentaContableHaberCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());
        }
    }

    public void llenarComboProveedor() {
        queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsProveedor = 1";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {

                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(GRUPO_PROPERTY).setValue(rsRecords.getString("GRUPO"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NOMBRESINCODIGO_PROPERTY).setValue(rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCentroCosto() {

        queryString = " SELECT * FROM centro_costo";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND Inhabilitado = 0";
        queryString += " AND IdEmpresa = " + empresaId;;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    centroCostoCbx.addItem(rsRecords.getString("IdCentroCosto"));
//                    centroCostoCbx.setItemCaption(rsRecords.getString("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto") + " " + rsRecords.getString("Grupo"));
                    centroCostoCbx.setItemCaption(rsRecords.getString("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto"));
                } while (rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar combo centro costo: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE FiltrarIngresoDocumentos = 'S'";
        queryString += " AND Estatus = 'HABILITADA'";
        queryString += " AND UPPER(N1) <> 'INGRESOS' ";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                cuentaContableHaberCbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContableHaberCbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

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

                cuentaContable9Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable9Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable10Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable10Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

            }
            if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCompras() != null) {
                cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCompras());
            }
            else {
                cuentaContable1Cbx.select(null);
            }
        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertPartidas() {
/**
        try {
            Utileria utileria = new Utileria();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            Date fechaInicial = dateFormat.parse(utileria.getFecha());
            Date fechaFinal = dateFormat.parse(utileria.getFechaYYYYMMDD(fechaDt.getValue()));

            int dias = (int) ((fechaInicial.getTime() - fechaFinal.getTime()) / 86400000);

            if (dias > 30) {

                if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken().isEmpty()) {
                    ValidarTokenForm validarTokenForm = new ValidarTokenForm(false);
                    UI.getCurrent().addWindow(validarTokenForm);
                    validarTokenForm.center();
                    return;
                } else {
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
 **/
        if (tipoDocumentoCbx.getValue() == null) {
            Notification.show("Por favor ingrese el tipo de documento.", Notification.Type.WARNING_MESSAGE);
            tipoDocumentoCbx.focus();
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

        if (proveedorCbx.getValue() == null) {
            Notification.show("Por favor ingrese el proveedor.", Notification.Type.WARNING_MESSAGE);
            proveedorCbx.focus();
            return;
        }
        if (nitProveedotTxt.isEmpty()) {
            Notification.show("Por favor ingrese el Nit del proveedor.", Notification.Type.WARNING_MESSAGE);
            nitProveedotTxt.focus();
            return;
        }
        if (monedaCbx.getValue() == null) {
            Notification.show("Por favor ingrese el tipo de moneda.", Notification.Type.WARNING_MESSAGE);
            monedaCbx.focus();
            return;
        }

        if (this.montoTxt.getDoubleValueDoNotThrow() == 0) {
            Notification.show("Por favor ingrese el monto de la factura.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return;
        }
/// Validando montos antes de ingresarlos 
        totalDebe = new BigDecimal(0);
        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe1Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe2Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe3Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe4Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe5Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe6Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe7Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe8Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe9Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe10Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);

        if (totalDebe.doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
            Notification notif = new Notification("EL MONTO DEL DEBE Y EL HABER NO COINCIDEN!. MONTO DEL DEBE : " + totalDebe.doubleValue() + " MONTO DEL HABER : " + montoTxt.getDoubleValueDoNotThrow(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            return;
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartida = empresaId + año + mes + dia + "1";

        queryString = " SELECT codigoPartida FROM contabilidad_partida ";
        queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
        queryString += " ORDER BY codigoPartida DESC ";

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

        queryString = " SELECT * FROM contabilidad_partida";
        queryString += " WHERE SerieDocumento  = '" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += " AND NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " AND IdProveedor     =  " + String.valueOf(proveedorCbx.getValue());
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND TipoDocumento = '" + String.valueOf(tipoDocumentoCbx.getValue()) + "'";
        queryString += " AND MonedaDocumento = '" + monedaCbx.getValue() + "'";

//        System.out.println("\n\nQuery=" + queryString + "\n\n");

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

        queryString = " INSERT INTO proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha, ";
        queryString += " TipoDocumento, SerieDocumento, NumeroDocumento, MonedaDocumento, ";
        queryString += " Monto, MontoQuetzales, TipoCambio, ";
        queryString += " IdUsuarioAutorizoPago,CreadoFechayHora,CreadoUsuario)";
        queryString += " VALUES (";
        queryString += empresaId;
        queryString += "," + String.valueOf(proveedorCbx.getValue());
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
        queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += ", " + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += ", " + tasaCambioTxt.getValue();
//        System.out.println("obtener el get value " + tasaCambioTxt.getValue());
//        System.out.println("obtener el get double" + tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
        } catch (Exception ex1) {
            System.out.println("Error al insertar proveedor cuentaCorriente : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        /// Ingreso del haber
        queryString = " INSERT INTO contabilidad_partida (IdEmpresa, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, ";
        queryString += " Debe, Haber, DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Estatus, ";
        queryString += " IdLiquidador, Descripcion, Referencia,";
        queryString += " CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre, IdOrdenCompra,";
        queryString += " IdCentroCosto, CodigoCentroCosto ";
        queryString += ")";
        queryString += " VALUES ";
        queryString += " (";
        queryString += empresaId;
        queryString += ",'" + codigoPartida + "'";
        if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
            queryString += ",'" + codigoPartida + "'"; //codigoCC
        } else {
            queryString += ",'" + tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue() + "'"; //codigoCC
        }
        queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";        
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + nitProveedotTxt.getValue() + "'";
        queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
        queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += "," + cuentaContableHaberCbx.getValue();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
        queryString += ",0.00"; //DEBE
        queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow()); //HABER
        queryString += ",0.00"; //DEBE Q.
        queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow()); //SALDO
        if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'INGRESADO'";
        } else { //ABASTOS
            queryString += ",0.00";
            queryString += ",'PAGADO'";
        }
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
        if (hacerRetencionIsrChk.getValue().equals(true)) {
            queryString += ",'SI'";
        } else {
            queryString += ",'NO'";
        }
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ",null";
        queryString += ",null";
        queryString += ",0";
        queryString += ",null";
        queryString += "," + ordenCompraCbx.getValue();
        queryString += ", " + centroCostoCbx.getValue();
        queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
        queryString += ")";

//// Primer ingreso debe
        if (cuentaContable1Cbx.getValue() != null && debe1Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable1Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto1Txt.getValue() + "'";
            queryString += ")";
        }

//// segundo  ingreso
        if (cuentaContable2Cbx.getValue() != null && debe2Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable2Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto2Txt.getValue() + "'";
            queryString += ")";
        }

//// Tercer ingreso
        if (cuentaContable3Cbx.getValue() != null && debe3Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'"; //codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable3Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto3Txt.getValue() + "'";
            queryString += ")";
        }
//// cuarto ingreso
        if (cuentaContable4Cbx.getValue() != null && debe4Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable4Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto4Txt.getValue() + "'";
            queryString += ")";
        }

//// quinto ingreso
        if (cuentaContable5Cbx.getValue() != null && debe5Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable5Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto5Txt.getValue() + "'";
            queryString += ")";
        }

//// sexto ingreso
        if (cuentaContable6Cbx.getValue() != null && debe6Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable6Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto6Txt.getValue() + "'";
            queryString += ")";
        }

//// septimo ingreso
        if (cuentaContable7Cbx.getValue() != null && debe7Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable7Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto7Txt.getValue() + "'";
            queryString += ")";
        }

//// octavo ingreso
        if (cuentaContable8Cbx.getValue() != null && debe8Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable8Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto8Txt.getValue() + "'";
            queryString += ")";
        }

//// noveno ingreso
        if (cuentaContable9Cbx.getValue() != null && debe9Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable9Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe9Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe9Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto9Txt.getValue() + "'";
            queryString += ")";
        }

//// decimo ingreso
        if (cuentaContable10Cbx.getValue() != null && debe10Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedotTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable10Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += "," + String.valueOf(debe10Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += ",0.00"; //HABER
            queryString += "," + String.valueOf(debe10Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q.
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if (String.valueOf(tipoCbx.getContainerProperty(tipoCbx.getValue(), "CODIGOCC").getValue()).trim().isEmpty()) {
                queryString += ",'INGRESADO'";
            } else { //ABASTOS
                queryString += ",'PAGADO'";
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (hacerRetencionIsrChk.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",null";
            queryString += ",0";
            queryString += ",null";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto10Txt.getValue() + "'";
            queryString += ")";
        }

        if (cuentaContable1Cbx.getValue() != null) { // hay una cuenta seleccioada

            if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada

                if (cuentaContable2Cbx.getValue().equals(cuentaContable1Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (cuentaContable3Cbx.getValue() != null) {
                    if (cuentaContable1Cbx.getValue().equals(cuentaContable3Cbx.getValue())
                            || cuentaContable2Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                        Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                        return;
                    }

                    if (cuentaContable4Cbx.getValue() != null) {
                        if (cuentaContable4Cbx.getValue().equals(cuentaContable1Cbx.getValue())
                                || cuentaContable4Cbx.getValue().equals(cuentaContable3Cbx.getValue())
                                || cuentaContable4Cbx.getValue().equals(cuentaContable2Cbx.getValue())) {
                            return;
                        }
                    }
                }

                if (cuentaContable4Cbx.getValue() != null) {
                    if (cuentaContable4Cbx.getValue().equals(cuentaContable1Cbx.getValue())
                            || cuentaContable4Cbx.getValue().equals(cuentaContable3Cbx.getValue())
                            || cuentaContable4Cbx.getValue().equals(cuentaContable2Cbx.getValue())) {
                        return;
                    }
                }
            }
        }
        if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada

            if (cuentaContable1Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable1Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable3Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable4Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
        }
        if (cuentaContable3Cbx.getValue() != null) {

            if (cuentaContable1Cbx.getValue() != null) {
                if (cuentaContable3Cbx.getValue().equals(cuentaContable1Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable2Cbx.getValue() != null) {
                if (cuentaContable2Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable4Cbx.getValue() != null) {
                if (cuentaContable4Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
        }
        if (cuentaContable4Cbx.getValue() != null) {

            if (cuentaContable1Cbx.getValue() != null) {
                if (cuentaContable1Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable3Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable3Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            if(porContabilizarCbx.getValue() != null) {
                queryString  = " UPDATE documentos_fel_sat ";
                queryString += " SET Contabilizada = 'S'";
                queryString += " WHERE IdEmpresa = " + empresaId;
                queryString += " AND Id = " + porContabilizarCbx.getValue();

                stQuery.executeUpdate(queryString);
            }

            Notification notif = new Notification("REGISTRO AGREGADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            ((IngresoDocumentosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(empresaId, 0);

            close();

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL INSERTAR DOCUMENTO", ex1);

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

    private void buscarProveedorPorNit() {
        if(nitProveedotTxt.getValue().trim().isEmpty()) {
            return;
        }
        if(nitProveedotTxt.getValue().equals("0")) {
            return;
        }
        if(nitProveedotTxt.getValue().equals("Sin registro")) {
            return;
        }

        for (Iterator<?> i = proveedorCbx.getItemIds().iterator(); i.hasNext();) {
            String id = (String) i.next();
            Item item = proveedorCbx.getItem(id);

            if (nitProveedotTxt.getValue().equals(String.valueOf(item.getItemProperty(NIT_PROPERTY).getValue()).trim())) {
                proveedorCbx.select(id);
                break;
            }
        }
    }

    private void generarTotalDebe() {

        totalDebe = new BigDecimal(0);
        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe1Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe2Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe3Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe4Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe5Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe6Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);

        debe1Txt.setCaption("DEBE : " + totalDebe);

    }

    public void verificarCuentaContableAplicar() {

        if(cuentaContable1Cbx == null) {
            return;
        }

        if(centroCostoCbx.getValue() == null) {
            return;
        }

        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCompras());

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaId; // la venta es de la misma empresa ????
        queryString += " AND IdCentroCosto = " + centroCostoCbx.getValue();
        queryString += " AND Estatus <> 'ANULADO'";
        queryString += " AND TipoDocumento = 'FACTURA VENTA'";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                if(rsRecords.getObject("Fecha") != null) {
                    if (fechaDt.getValue().before(rsRecords.getDate("Fecha"))) {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCompras());
                    } else {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProvisionCompras());
                    }
                }
            }
            else {
                cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCompras());
            }
        } catch (Exception ex1) {
            Notification.show("Error al leer documentos venta del centro de costo : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    /*
    
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
     */
}
