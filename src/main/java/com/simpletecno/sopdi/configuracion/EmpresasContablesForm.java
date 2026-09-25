package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class EmpresasContablesForm extends Window {

    public String idEmpresaEdit = "0";

    UI mainUI;
    Statement stQuery = null;
    static PreparedStatement stPreparedQuery;
    ResultSet rsRecords = null;
    String queryString = "";

    MarginInfo marginInfo;

    FormLayout mainForm;
    FormLayout felForm;

    NumberField idEmpresaTxt;

    TextField nombreTxt;
    TextField nombreCortoTxt;
    TextField nitTxt;
    NumberField ultimaLiquidacionTxt;
    CheckBox recibeEnganchesCheck;
    TextField usuarioFELTxt;
    TextField claveFELTxt;
    TextField tokenFELTxt;
    ComboBox regimenCbx;
    ComboBox idProveedorCbx;
    TextField codigoProductoExcelFELTxt;

    // ── Parámetros generales (montos y porcentajes) ──────────────────────────
    NumberField porcentajeIvaTxt;
    NumberField montoMaximoFacturaCfTxt;
    NumberField montoInicialRetencionIsrTxt;
    NumberField montoMaximoBaseIsrPrimerTxt;
    NumberField primerPorcentajeIsrTxt;
    NumberField montoInicialBaseIsrSegundoTxt;
    NumberField segundoPorcentajeIsrTxt;

    Button guardarBtn;
    Button salirBtn;

    Button cargarRtuBtn;
    Table obligacionesTable;

    MultiFileUpload singleUpload;
    Image logoImage;
    public File file;
    StreamResource logoStreamResource = null;

    public EmpresasContablesForm() {
        this.mainUI = UI.getCurrent();
        asegurarColumnasParametros();
        setResponsive(true);
        setModal(true);
        setWidth("90%");
        setHeight("95%");
        center();

        marginInfo = new MarginInfo(true, true, false, true);

        mainForm = new FormLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);    

        idEmpresaTxt = new NumberField("Empresa : ");
        idEmpresaTxt.setWidth("8em");
        idEmpresaTxt.setMaxValue(999);
        idEmpresaTxt.setReadOnly((!idEmpresaEdit.equals("0")));

        nombreTxt = new TextField("Nombre :");
        nombreTxt.setWidth("15em");
        nombreTxt.setMaxLength(128);
       
        nombreCortoTxt = new TextField("Nombre corto:");
        nombreCortoTxt.setWidth("15em");
        nombreCortoTxt.setMaxLength(128);

        nitTxt = new TextField("NIT :");
        nitTxt.setWidth("8em");
        nitTxt.setMaxLength(128);

        ultimaLiquidacionTxt = new NumberField("Ultima Liquidación :");
        ultimaLiquidacionTxt.setWidth("8em");
        ultimaLiquidacionTxt.setDecimalAllowed(false);
        ultimaLiquidacionTxt.setDecimalPrecision(0);
        ultimaLiquidacionTxt.setMaxValue(9999);

        recibeEnganchesCheck = new CheckBox("Recibe enganches : ");
        recibeEnganchesCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        recibeEnganchesCheck.setValue(false);

        usuarioFELTxt = new TextField("FEL Usuario:");
        usuarioFELTxt.setWidth("15em");
        usuarioFELTxt.setMaxLength(128);

        claveFELTxt = new TextField("FEL Clave:");
        claveFELTxt.setWidth("15em");
        claveFELTxt.setMaxLength(128);

        tokenFELTxt = new TextField("FEL Token:");
        tokenFELTxt.setWidth("15em");
        tokenFELTxt.setMaxLength(128);

        regimenCbx = new ComboBox("REGIMEN : ");
        regimenCbx.setWidth("25em");
        regimenCbx.setInvalidAllowed(false);
        regimenCbx.setNewItemsAllowed(false);
        regimenCbx.setTextInputAllowed(false);
        regimenCbx.setNullSelectionAllowed(false);
        regimenCbx.addItem("Sobre las Utilidades de Actividades Lucrativas");
        regimenCbx.addItem("Opcional Simplificado sobre Ingresos de Actividades Lucrativas");
        regimenCbx.select("Sobre las Utilidades de Actividades Lucrativas");

        idProveedorCbx = new ComboBox("Proveedor de la empresa :");
        idProveedorCbx.setWidth("25em");
        idProveedorCbx.setFilteringMode(com.vaadin.shared.ui.combobox.FilteringMode.CONTAINS);
        idProveedorCbx.setNullSelectionAllowed(true);
        idProveedorCbx.setInvalidAllowed(false);
        idProveedorCbx.setNewItemsAllowed(false);
        idProveedorCbx.setDescription("IdProveedor que corresponde a esta empresa en proveedor_empresa");
        llenarComboProveedorEmpresa();

        codigoProductoExcelFELTxt = new TextField("Código FEL producto EXENTO :");
        codigoProductoExcelFELTxt.setWidth("10em");
        codigoProductoExcelFELTxt.setMaxLength(32);

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        guardarBtn = new Button("Guardar");
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertarEmpresaContable();
            }
        });

        // Datos generales -> mainForm
        mainForm.addComponent(idEmpresaTxt);
        mainForm.addComponent(nombreTxt);
        mainForm.addComponent(nombreCortoTxt);
        mainForm.addComponent(nitTxt);
        mainForm.addComponent(ultimaLiquidacionTxt);
        mainForm.addComponent(regimenCbx);
        mainForm.addComponent(idProveedorCbx);
        mainForm.addComponent(recibeEnganchesCheck);

        // Facturación electrónica (FEL) -> felForm
        felForm = new FormLayout();
        felForm.setWidth("100%");
        felForm.setMargin(true);
        felForm.setSpacing(true);
        felForm.addComponent(usuarioFELTxt);
        felForm.addComponent(claveFELTxt);
        felForm.addComponent(tokenFELTxt);
        felForm.addComponent(codigoProductoExcelFELTxt);

        HorizontalLayout logoImgLayout = new HorizontalLayout();
        logoImgLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        logoImgLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        logoImage = new Image();
        logoImage.setImmediate(true);
        logoImage.setWidth("100px");
        logoImage.setHeight("100px");
        logoImage.setIcon(FontAwesome.IMAGE);
//        logoImage.addStyleName("menu-logo-empresa");

        logoImgLayout.addComponent(logoImage);

        UploadFinishedHandler handler;
        handler = new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                File targetFile;

                try {

                    System.out.println("\nfileName="+fileName);
                    System.out.println("length="+stream.available());
                    System.out.println("mimeType="+mimeType);

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

                    logoStreamResource = null;

                    if(buffer != null ) {
                        logoStreamResource = new StreamResource(
                                new StreamResource.StreamSource() {
                                    public InputStream getStream() {
                                        return new ByteArrayInputStream(buffer);
                                    }
                                },idEmpresaTxt.getValue()
                        );
                    }
                    logoImage.setSource(logoStreamResource);
                    file = targetFile;
                }
                catch( java.io.IOException fIoEx) {
                    fIoEx.printStackTrace();
                    Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                }
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler,window, false);
//        singleUpload.setCaption("Cargar archivo");
//        singleUpload2.setRootDirectory(newDoc.getAbsolutePath());
//        singleUpload.setPanelCaption("Buscar");
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar logo del empresa", "");

        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.jpeg')");

/**
 List<String> acceptedMimeTypes = new ArrayList();
 acceptedMimeTypes.add("application/octet-stream");
 acceptedMimeTypes.add("application/ms-project");
 acceptedMimeTypes.add("application/vnd.ms-project");
 acceptedMimeTypes.add("application/msproj");
 acceptedMimeTypes.add("application/msproject");
 acceptedMimeTypes.add("application/x-msproject");
 acceptedMimeTypes.add("application/x-ms-project");
 acceptedMimeTypes.add("application/x-dos_ms_project");
 acceptedMimeTypes.add("application/mpp");
 acceptedMimeTypes.add("zz-application/zz-winassoc-mpp");
 //        singleUpload.setAcceptedMimeTypes(acceptedMimeTypes);
 **/

        logoImgLayout.addComponent(singleUpload);

        // ================= TAB 1: Datos generales =================
        Label logoLbl = new Label("Logo");
        logoLbl.addStyleName(ValoTheme.LABEL_H4);
        logoLbl.addStyleName(ValoTheme.LABEL_BOLD);

        VerticalLayout datosTabLayout = new VerticalLayout();
        datosTabLayout.setWidth("100%");
        datosTabLayout.setMargin(true);
        datosTabLayout.setSpacing(true);
        datosTabLayout.addComponent(mainForm);
        datosTabLayout.addComponent(logoLbl);
        datosTabLayout.addComponent(logoImgLayout);

        Panel datosTabPanel = new Panel();
        datosTabPanel.setSizeFull();
        datosTabPanel.setContent(datosTabLayout);

        // ================= TAB 2: Facturación electrónica (FEL) =================
        VerticalLayout felTabLayout = new VerticalLayout();
        felTabLayout.setWidth("100%");
        felTabLayout.setMargin(true);
        felTabLayout.setSpacing(true);
        felTabLayout.addComponent(felForm);

        Panel felTabPanel = new Panel();
        felTabPanel.setSizeFull();
        felTabPanel.setContent(felTabLayout);

        // ================= TAB 3: Obligaciones fiscales =================
        Label obligTitleLbl = new Label("Obligaciones fiscales según la Constancia RTU de la SAT");
        obligTitleLbl.addStyleName(ValoTheme.LABEL_H4);
        obligTitleLbl.addStyleName(ValoTheme.LABEL_BOLD);

        cargarRtuBtn = new Button("Cargar RTU (PDF)", FontAwesome.FILE_PDF_O);
        cargarRtuBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        cargarRtuBtn.setDescription("Leer las obligaciones fiscales desde la Constancia RTU de la SAT (PDF)");
        cargarRtuBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (idEmpresaEdit.equals("0")) {
                    Notification.show("Guarde primero la empresa para poder cargar sus obligaciones.",
                            Notification.Type.WARNING_MESSAGE);
                    return;
                }
                UI.getCurrent().addWindow(new ImportarObligacionesRtuForm(
                        idEmpresaEdit, nombreTxt.getValue(),
                        EmpresasContablesForm.this::cargarObligaciones));
            }
        });

        obligacionesTable = new Table();
        obligacionesTable.setSizeFull();
        obligacionesTable.addContainerProperty("Impuesto", String.class, "");
        obligacionesTable.addContainerProperty("No.", String.class, "");
        obligacionesTable.addContainerProperty("Frecuencia", String.class, "");
        obligacionesTable.addContainerProperty("Obligación", String.class, "");
        obligacionesTable.addContainerProperty("Formulario", String.class, "");
        obligacionesTable.setColumnWidth("Impuesto", 55);
        obligacionesTable.setColumnWidth("No.", 35);
        obligacionesTable.setColumnWidth("Frecuencia", 90);
        obligacionesTable.setColumnExpandRatio("Obligación", 1);
        obligacionesTable.setColumnExpandRatio("Formulario", 2);

        HorizontalLayout obligToolbar = new HorizontalLayout(cargarRtuBtn);
        obligToolbar.setSpacing(true);

        VerticalLayout obligTabLayout = new VerticalLayout();
        obligTabLayout.setSizeFull();
        obligTabLayout.setMargin(true);
        obligTabLayout.setSpacing(true);
        obligTabLayout.addComponent(obligTitleLbl);
        obligTabLayout.addComponent(obligToolbar);
        obligTabLayout.addComponent(obligacionesTable);
        obligTabLayout.setExpandRatio(obligacionesTable, 1);

        // ================= TabSheet =================
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(datosTabPanel, "Datos generales", FontAwesome.BUILDING);
        tabSheet.addTab(construirParametrosTab(), "Parámetros generales", FontAwesome.PERCENT);
        tabSheet.addTab(felTabPanel, "Facturación electrónica (FEL)", FontAwesome.FILE_CODE_O);
        tabSheet.addTab(obligTabLayout, "Obligaciones fiscales", FontAwesome.FILE_TEXT_O);

        // La administración de API Keys solo es visible para el perfil ADMINISTRADOR.
        if ("ADMINISTRADOR".equals(((SopdiUI) mainUI).sessionInformation.getStrUserProfile())) {
            tabSheet.addTab(construirApiKeysTab(), "API Keys", FontAwesome.KEY);
        }

        // ----- Botones (siempre visibles al pie) -----
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(salirBtn, guardarBtn);

        Label titleLbl = new Label("Empresa contable");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeFull();
        contentLayout.setMargin(true);
        contentLayout.setSpacing(true);
        contentLayout.addComponent(titleLbl);
        contentLayout.addComponent(tabSheet);
        contentLayout.addComponent(buttonsLayout);
        contentLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);
        contentLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
        contentLayout.setExpandRatio(tabSheet, 1);

        setContent(contentLayout);

    }

    /**
     * Pestaña para administrar las API Keys de esta empresa. Solo se agrega para
     * usuarios con perfil ADMINISTRADOR. Las claves son por empresa; la ventana de
     * administración se abre con la empresa actual como alcance.
     */
    private VerticalLayout construirApiKeysTab() {
        Label apiTitleLbl = new Label("API Keys para el consumo del API REST de esta empresa");
        apiTitleLbl.addStyleName(ValoTheme.LABEL_H4);
        apiTitleLbl.addStyleName(ValoTheme.LABEL_BOLD);

        Label apiInfoLbl = new Label("Cada clave se envía en el header X-API-Key. "
                + "Puede crearlas, activarlas/desactivarlas y asignarles fecha de vencimiento o dejarlas permanentes.");

        Button adminApiKeysBtn = new Button("Administrar API Keys", FontAwesome.KEY);
        adminApiKeysBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        adminApiKeysBtn.setDescription("Crear, activar/desactivar y eliminar API Keys de esta empresa.");
        adminApiKeysBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (idEmpresaEdit.equals("0")) {
                    Notification.show("Guarde primero la empresa para poder administrar sus API Keys.",
                            Notification.Type.WARNING_MESSAGE);
                    return;
                }
                UI.getCurrent().addWindow(
                        new com.simpletecno.sopdi.api.ApiKeysForm(idEmpresaEdit, nombreTxt.getValue()));
            }
        });

        VerticalLayout apiTabLayout = new VerticalLayout();
        apiTabLayout.setSizeFull();
        apiTabLayout.setMargin(true);
        apiTabLayout.setSpacing(true);
        apiTabLayout.addComponent(apiTitleLbl);
        apiTabLayout.addComponent(apiInfoLbl);
        apiTabLayout.addComponent(adminApiKeysBtn);
        return apiTabLayout;
    }

    /**
     * Pestaña de parámetros generales de la empresa (montos y porcentajes usados
     * en cálculos fiscales: IVA, factura CF, retención de ISR por tramos).
     *
     * Diseñada para crecer: para agregar un parámetro nuevo basta con declarar el
     * NumberField, agregar la columna en {@link #asegurarColumnasParametros()} y
     * leerlo/guardarlo en {@link #llenarCampos()} e {@link #insertarEmpresaContable()}.
     */
    private Panel construirParametrosTab() {
        porcentajeIvaTxt            = crearCampoDecimal("Porcentaje del IVA (%) :");
        montoMaximoFacturaCfTxt     = crearCampoDecimal("Monto máximo factura CF :");
        montoInicialRetencionIsrTxt = crearCampoDecimal("Monto inicial para retener ISR :");
        montoMaximoBaseIsrPrimerTxt = crearCampoDecimal("Monto máximo base ISR (1er %) :");
        primerPorcentajeIsrTxt      = crearCampoDecimal("Primer porcentaje ISR (%) :");
        montoInicialBaseIsrSegundoTxt = crearCampoDecimal("Monto inicial base ISR (2do %) :");
        segundoPorcentajeIsrTxt     = crearCampoDecimal("Segundo porcentaje ISR (%) :");

        Label titleLbl = new Label("Parámetros generales (montos y porcentajes)");
        titleLbl.addStyleName(ValoTheme.LABEL_H4);
        titleLbl.addStyleName(ValoTheme.LABEL_BOLD);

        FormLayout paramForm = new FormLayout();
        paramForm.setWidth("100%");
        paramForm.setMargin(true);
        paramForm.setSpacing(true);
        paramForm.addComponent(porcentajeIvaTxt);
        paramForm.addComponent(montoMaximoFacturaCfTxt);
        paramForm.addComponent(montoInicialRetencionIsrTxt);
        paramForm.addComponent(montoMaximoBaseIsrPrimerTxt);
        paramForm.addComponent(primerPorcentajeIsrTxt);
        paramForm.addComponent(montoInicialBaseIsrSegundoTxt);
        paramForm.addComponent(segundoPorcentajeIsrTxt);

        VerticalLayout paramTabLayout = new VerticalLayout();
        paramTabLayout.setWidth("100%");
        paramTabLayout.setMargin(true);
        paramTabLayout.setSpacing(true);
        paramTabLayout.addComponent(titleLbl);
        paramTabLayout.addComponent(paramForm);

        Panel paramTabPanel = new Panel();
        paramTabPanel.setSizeFull();
        paramTabPanel.setContent(paramTabLayout);
        return paramTabPanel;
    }

    /** Valor numérico de un NumberField apto para SQL; 0 si está vacío/nulo. */
    private String num(NumberField field) {
        return field.getValue() == null ? "0" : String.valueOf(field.getValue());
    }

    /** Crea un NumberField configurado para montos/porcentajes con 2 decimales. */
    private NumberField crearCampoDecimal(String caption) {
        NumberField nf = new NumberField(caption);
        nf.setWidth("10em");
        nf.setDecimalAllowed(true);
        nf.setDecimalPrecision(2);
        nf.setMinimumFractionDigits(2);
        nf.setDecimalSeparator('.');
        nf.setDecimalSeparatorAlwaysShown(true);
        nf.setGroupingUsed(true);
        nf.setGroupingSeparator(',');
        nf.setGroupingSize(3);
        nf.setValue(0d);
        nf.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        return nf;
    }

    /**
     * Agrega a contabilidad_empresa las columnas de parámetros generales que aún
     * no existan. MySQL/MariaDB no soporta "ADD COLUMN IF NOT EXISTS" en todas
     * las versiones, por lo que se consulta information_schema antes de cada ALTER.
     *
     * Para agregar un parámetro nuevo, basta con añadir una fila a este arreglo.
     */
    private void llenarComboProveedorEmpresa() {
        idProveedorCbx.removeAllItems();
        String empresa = idEmpresaEdit.equals("0")
                ? ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId()
                : idEmpresaEdit;
        try {
            Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT IDProveedor, Nombre FROM proveedor_empresa"
                    + " WHERE IdEmpresa = " + empresa
                    + " ORDER BY Nombre");
            while (rs.next()) {
                String id = rs.getString("IDProveedor");
                idProveedorCbx.addItem(id);
                idProveedorCbx.setItemCaption(id, id + " – " + rs.getString("Nombre"));
            }
            rs.close();
        } catch (Exception ex) {
            Logger.getLogger(EmpresasContablesForm.class.getName()).log(Level.WARNING,
                    "Error al cargar proveedores para IdProveedor", ex);
        }
    }

    private void asegurarColumnasParametros() {
        String[][] columnas = {
                {"PorcentajeIva",                        "DECIMAL(6,2)  NULL DEFAULT 0"},
                {"MontoMaximoFacturaCf",                 "DECIMAL(14,2) NULL DEFAULT 0"},
                {"MontoInicialRetencionIsr",             "DECIMAL(14,2) NULL DEFAULT 0"},
                {"MontoMaximoBaseIsrPrimerPorcentaje",   "DECIMAL(14,2) NULL DEFAULT 0"},
                {"PrimerPorcentajeIsr",                  "DECIMAL(6,2)  NULL DEFAULT 0"},
                {"MontoInicialBaseIsrSegundoPorcentaje", "DECIMAL(14,2) NULL DEFAULT 0"},
                {"SegundoPorcentajeIsr",                 "DECIMAL(6,2)  NULL DEFAULT 0"},
                {"IdProveedor",                          "VARCHAR(20)   NULL DEFAULT NULL"},
        };
        for (String[] col : columnas) {
            String existeSql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                    + " WHERE TABLE_SCHEMA = DATABASE() "
                    + " AND TABLE_NAME = 'contabilidad_empresa' "
                    + " AND COLUMN_NAME = '" + col[0] + "'";
            try {
                Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                ResultSet rs = st.executeQuery(existeSql);
                boolean existe = rs.next() && rs.getInt(1) > 0;
                rs.close();
                if (!existe) {
                    st.executeUpdate("ALTER TABLE contabilidad_empresa ADD COLUMN " + col[0] + " " + col[1]);
                }
            } catch (Exception ex) {
                Logger.getLogger(EmpresasContablesForm.class.getName()).log(Level.WARNING,
                        "No se pudo asegurar la columna contabilidad_empresa." + col[0] + ": {0}", ex.getMessage());
            }
        }
    }

    public void llenarCampos() {
        try {

            queryString = " SELECT *";
            queryString += " FROM contabilidad_empresa";
            queryString += " WHERE IdEmpresa = " + idEmpresaEdit;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                idEmpresaTxt.setReadOnly(false);
                idEmpresaTxt.setValue(rsRecords.getString("IdEmpresa"));
                idEmpresaTxt.setReadOnly(true);
                nombreTxt.setValue(rsRecords.getString("Empresa"));
                nombreCortoTxt.setValue(rsRecords.getString("NombreCorto"));
                nitTxt.setValue(rsRecords.getString("Nit"));
                ultimaLiquidacionTxt.setValue(rsRecords.getString("IdUltimaLiquidacion"));
                if(rsRecords.getString("RecibeEnganches").equals("1")){
                    recibeEnganchesCheck.setValue(true);
                }
                usuarioFELTxt.setValue(rsRecords.getString("UsuarioFEL"));
                claveFELTxt.setValue(rsRecords.getString("ClaveFEL"));
                tokenFELTxt.setValue(rsRecords.getString("TokenFEL"));
                regimenCbx.select(rsRecords.getString("Regimen"));
                idProveedorCbx.select(rsRecords.getString("IdProveedor"));
                codigoProductoExcelFELTxt.setValue(rsRecords.getString("CodigoProductoExentoFel"));

                // Parámetros generales (montos y porcentajes)
                porcentajeIvaTxt.setValue(rsRecords.getDouble("PorcentajeIva"));
                montoMaximoFacturaCfTxt.setValue(rsRecords.getDouble("MontoMaximoFacturaCf"));
                montoInicialRetencionIsrTxt.setValue(rsRecords.getDouble("MontoInicialRetencionIsr"));
                montoMaximoBaseIsrPrimerTxt.setValue(rsRecords.getDouble("MontoMaximoBaseIsrPrimerPorcentaje"));
                primerPorcentajeIsrTxt.setValue(rsRecords.getDouble("PrimerPorcentajeIsr"));
                montoInicialBaseIsrSegundoTxt.setValue(rsRecords.getDouble("MontoInicialBaseIsrSegundoPorcentaje"));
                segundoPorcentajeIsrTxt.setValue(rsRecords.getDouble("SegundoPorcentajeIsr"));

                final byte[] docBytes = rsRecords.getBytes("Logo");
                StreamResource logoStreamResource = null;

                if(docBytes != null ) {
                    logoStreamResource = new StreamResource(
                            new StreamResource.StreamSource() {
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(docBytes);
                                }
                            },idEmpresaTxt.getValue()
                    );
                }
                logoImage.setSource(logoStreamResource);

            }

            cargarObligaciones();

        } catch (SQLException ex) {
            System.out.println("Error al llenar Campos " + ex);
            ex.printStackTrace();
        }
    }

    /** Carga en la tabla las obligaciones fiscales registradas para la empresa en edición. */
    public void cargarObligaciones() {
        if (obligacionesTable == null) {
            return;
        }
        obligacionesTable.removeAllItems();
        if (idEmpresaEdit.equals("0")) {
            return;
        }
        try {
            ImportarObligacionesRtuForm.crearTablaSiNoExiste(
                    ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection());

            String query = " SELECT Impuesto, Numero, Frecuencia, NombreObligacion, CodigoFormulario"
                    + " FROM contabilidad_empresa_obligacion"
                    + " WHERE IdEmpresa = " + idEmpresaEdit + " AND Estatus = 'ACTIVO'"
                    + " ORDER BY Impuesto, Numero";

            Statement st = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = st.executeQuery(query);
            int i = 1;
            while (rs.next()) {
                obligacionesTable.addItem(new Object[]{
                        rs.getString("Impuesto"),
                        rs.getString("Numero"),
                        rs.getString("Frecuencia"),
                        rs.getString("NombreObligacion"),
                        rs.getString("CodigoFormulario")}, i++);
            }
        } catch (Exception ex) {
            Logger.getLogger(EmpresasContablesForm.class.getName()).log(Level.WARNING,
                    "Error al cargar obligaciones: {0}", ex.getMessage());
        }
    }

    public void insertarEmpresaContable() {
        try {

            if(nombreTxt.getValue().trim().isEmpty()) {
                Notification.show("Ingrese el nombre de la empresa.", Notification.Type.WARNING_MESSAGE);
                nombreTxt.focus();
                return;
            }
            if(nombreCortoTxt.getValue().trim().isEmpty()) {
                Notification.show("Ingrese el nombre corto de la empresa.", Notification.Type.WARNING_MESSAGE);
                nombreCortoTxt.focus();
                return;
            }
            if(nitTxt.getValue().trim().isEmpty()) {
                Notification.show("Ingrese el NIT de la empresa.", Notification.Type.WARNING_MESSAGE);
                nitTxt.focus();
                return;
            }
            if (idEmpresaEdit.equals("0")) {
                queryString = "INSERT INTO contabilidad_empresa (IdEmpresa, Empresa, NombreCorto, Nit, " +
                        "IdUltimaLiquidacion, RecibeEnganches, UsuarioFEL, ClaveFEL, UsuarioToken, " +
                        "Regimen, IdProveedor, CodigoProductoExentoFel, " +
                        "PorcentajeIva, MontoMaximoFacturaCf, MontoInicialRetencionIsr, " +
                        "MontoMaximoBaseIsrPrimerPorcentaje, PrimerPorcentajeIsr, " +
                        "MontoInicialBaseIsrSegundoPorcentaje, SegundoPorcentajeIsr, Logo)";
                queryString += " VALUES (";
                queryString += " " + idEmpresaTxt.getValue();
                queryString += ",'" + nombreTxt.getValue() + "'";
                queryString += ",'" + nombreCortoTxt.getValue() + "'";
                queryString += "," + nitTxt.getValue();
                queryString += "," + ultimaLiquidacionTxt.getValue();
                if (recibeEnganchesCheck.getValue() == true){
                    queryString += ", 1";
                }else{
                    queryString += ", 0";
                }
                queryString += ", '" + usuarioFELTxt.getValue() + "'";
                queryString += ", '" + claveFELTxt.getValue() + "'";
                queryString += ", '" + tokenFELTxt.getValue() + "'";
                queryString += ", '" + regimenCbx.getValue() + "'";
                queryString += ", " + (idProveedorCbx.getValue() == null ? "NULL" : "'" + idProveedorCbx.getValue() + "'");
                queryString += ", '" + codigoProductoExcelFELTxt.getValue() + "'";
                queryString += ", " + num(porcentajeIvaTxt);
                queryString += ", " + num(montoMaximoFacturaCfTxt);
                queryString += ", " + num(montoInicialRetencionIsrTxt);
                queryString += ", " + num(montoMaximoBaseIsrPrimerTxt);
                queryString += ", " + num(primerPorcentajeIsrTxt);
                queryString += ", " + num(montoInicialBaseIsrSegundoTxt);
                queryString += ", " + num(segundoPorcentajeIsrTxt);
                queryString += ",?";
                queryString += ")";
            } else {
                queryString = "UPDATE contabilidad_empresa SET ";
                queryString += " Empresa = '" + nombreTxt.getValue() + "'";
                queryString += ",NombreCorto = '" + nombreCortoTxt.getValue() + "'";
                queryString += ",Nit = '" + nitTxt.getValue() + "'";
                queryString += ",IdUltimaLiquidacion  = " + ultimaLiquidacionTxt.getValue();
                if (recibeEnganchesCheck.getValue() == true){
                    queryString += ", RecibeEnganches = 1";
                }else{
                    queryString += ", RecibeEnganches = 0";
                }
                queryString += ", UsuarioFEL = '" + usuarioFELTxt.getValue() + "'";
                queryString += ", ClaveFEL = '" + claveFELTxt.getValue() + "'";
                queryString += ", TokenFEL = '" + tokenFELTxt.getValue() + "'";
                queryString += ", Regimen = '" + regimenCbx.getValue() + "'";
                queryString += ", IdProveedor = " + (idProveedorCbx.getValue() == null ? "NULL" : "'" + idProveedorCbx.getValue() + "'");
                queryString += ", CodigoProductoExentoFel = '" + codigoProductoExcelFELTxt.getValue() + "'";
                queryString += ", PorcentajeIva = " + num(porcentajeIvaTxt);
                queryString += ", MontoMaximoFacturaCf = " + num(montoMaximoFacturaCfTxt);
                queryString += ", MontoInicialRetencionIsr = " + num(montoInicialRetencionIsrTxt);
                queryString += ", MontoMaximoBaseIsrPrimerPorcentaje = " + num(montoMaximoBaseIsrPrimerTxt);
                queryString += ", PrimerPorcentajeIsr = " + num(primerPorcentajeIsrTxt);
                queryString += ", MontoInicialBaseIsrSegundoPorcentaje = " + num(montoInicialBaseIsrSegundoTxt);
                queryString += ", SegundoPorcentajeIsr = " + num(segundoPorcentajeIsrTxt);
                queryString += ", Logo = ?";
                queryString += " WHERE IdEmpresa = " + idEmpresaEdit;
            }
Logger.getLogger(EmpresasContablesForm.class.getName()).log(Level.INFO, "queryString={0}", queryString);
            stPreparedQuery  = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString);

            if(logoStreamResource != null) {

                stPreparedQuery.setBinaryStream(1, logoStreamResource.getStream().getStream(), logoStreamResource.getStream().getStream().available());
//                    receiver.file.delete();
            }
            else {
                stPreparedQuery.setBinaryStream(1, null, 0);
            }

            stPreparedQuery.executeUpdate();

            ((EmpresasContablesView) (mainUI.getNavigator().getCurrentView())).llenarTablaEmpresas();
            
            close();

        } catch (Exception ex) {
            Notification.show("ERROR AL CREAR O EDITAR EMPRESA CONTABLE : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            System.out.println("Error al insertar o editar " + queryString);
            ex.printStackTrace();
        }
    }
}
