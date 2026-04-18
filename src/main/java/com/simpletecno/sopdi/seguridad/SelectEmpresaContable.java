package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.configuracion.ProjectSelectionForm;
import com.simpletecno.sopdi.contabilidad.EmpresaCuentasEquivalentesHelper;
import com.simpletecno.sopdi.extras.infile.Direccion;
import com.simpletecno.sopdi.extras.infile.Emisor;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.*;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ventana de selección de Empresa Contable y Proyecto.
 * UI modernizada: layout centrado, estilos CSS personalizados.
 *
 * @author user
 */
public class SelectEmpresaContable extends Window {

    // ── Constantes de columnas ───────────────────────────────────────────────
    static final String LOGO_PROPERTY         = "Logo";
    static final String ID_PROPERTY           = "Id";
    static final String NOMBRE_PROPERTY       = "Nombre";
    static final String NOMBRE_CORTO_PROPERTY = "Nombre corto";
    static final String NIT_PROPERTY          = "NIT";
    static final String REGIMEN_PROPERTY      = "REGIMEN";
    static final String ULTIMA_LIQUI_PROPERTY = "Ultima Liq.";

    // ── Componentes principales ──────────────────────────────────────────────
    /** Layout raíz: ocupa todo el Window y centra el panel interior. */
    VerticalLayout rootLayout  = new VerticalLayout();

    /** Panel interior con ancho fijo (70 %) que contiene todo el contenido. */
    VerticalLayout mainLayout  = new VerticalLayout();

    Table empresasTable  = new Table("");
    Table proyectosTable = new Table("");

    UI     mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String    queryString;
    Button    selectBtn;
    Button    exitBtn;

    // ────────────────────────────────────────────────────────────────────────
    public SelectEmpresaContable() {
        this.mainUI = UI.getCurrent();

        setSizeFull();
        setModal(true);
        setResizable(false);
        setDraggable(false);
        // Eliminar la barra de título del Window para una apariencia más limpia
        setCaption(null);

        // ── CSS inyectado una sola vez por sesión ────────────────────────────
        injectStyles();

        // ── rootLayout: wrapper que centra mainLayout ────────────────────────
        rootLayout.setSizeFull();
        rootLayout.setMargin(false);
        rootLayout.setSpacing(false);
        rootLayout.addStyleName("sec-root");

        // ── mainLayout: el "card" central ────────────────────────────────────
        mainLayout.setWidth("680px");        // ancho fijo, se ve bien en cualquier resolución
        mainLayout.setHeightUndefined();     // altura se adapta al contenido
        mainLayout.setMargin(new MarginInfo(true, true, true, true));
        mainLayout.setSpacing(true);
        mainLayout.addStyleName("sec-card");

        // ── Título / encabezado ──────────────────────────────────────────────
        buildHeader();

        // ── Tablas ──────────────────────────────────────────────────────────
        createTablaEmpresas();
        llenarTablaEmpresas();

        crearTableProyectos();
        llenarTablaProyectos();

        // ── Botones ──────────────────────────────────────────────────────────
        crearBotones();

        // ── Ensamblar: centrar mainLayout dentro de rootLayout ───────────────
        rootLayout.addComponent(mainLayout);
        rootLayout.setComponentAlignment(mainLayout, Alignment.MIDDLE_CENTER);

        setContent(rootLayout);
    }

    // ── Estilos CSS ──────────────────────────────────────────────────────────
    private void injectStyles() {
        // Vaadin 7: se usa Page.getCurrent().getStyles() para inyectar CSS global.
        Page.getCurrent().getStyles().add(
                /* Fondo semitransparente sobre el overlay del modal */
                ".sec-root {" +
                        "  background: transparent;" +
                        "}" +

                        /* Card central con sombra y bordes redondeados */
                        ".sec-card {" +
                        "  background: #ffffff;" +
                        "  border-radius: 12px;" +
                        "  box-shadow: 0 8px 40px rgba(0,0,0,0.18);" +
                        "  padding: 32px !important;" +
                        "}" +

                        /* Encabezado */
                        ".sec-header {" +
                        "  border-bottom: 2px solid #1976D2;" +
                        "  padding-bottom: 12px;" +
                        "  margin-bottom: 8px;" +
                        "  width: 100%;" +
                        "}" +

                        /* Ícono decorativo del encabezado */
                        ".sec-icon {" +
                        "  font-size: 32px;" +
                        "  color: #1976D2;" +
                        "  line-height: 1;" +
                        "}" +

                        /* Título principal */
                        ".sec-title {" +
                        "  color: #1565C0;" +
                        "  font-size: 20px !important;" +
                        "  font-weight: 700 !important;" +
                        "  margin: 0 !important;" +
                        "  line-height: 1.2 !important;" +
                        "}" +

                        /* Sub-texto bajo el título */
                        ".sec-subtitle {" +
                        "  color: #607D8B;" +
                        "  font-size: 13px !important;" +
                        "  margin: 2px 0 0 0 !important;" +
                        "}" +

                        /* Etiqueta de sección (encima de cada tabla) */
                        ".sec-section-label {" +
                        "  color: #1976D2;" +
                        "  font-size: 12px !important;" +
                        "  font-weight: 600 !important;" +
                        "  letter-spacing: 0.08em;" +
                        "  text-transform: uppercase;" +
                        "  margin-bottom: 2px !important;" +
                        "}" +

                        /* Tablas: quitar bordes extra de Valo y aplicar estilo propio */
                        ".sec-card .v-table-body {" +
                        "  border: 1px solid #E3E8EF !important;" +
                        "  border-radius: 8px;" +
                        "  overflow: hidden;" +
                        "}" +
                        ".sec-card .v-table-header-wrap {" +
                        "  background: #F0F4F8 !important;" +
                        "  border-bottom: 2px solid #BBDEFB !important;" +
                        "}" +
                        ".sec-card .v-table-row:hover td {" +
                        "  background: #E3F2FD !important;" +
                        "  cursor: pointer;" +
                        "}" +
                        ".sec-card .v-table-row-selected td {" +
                        "  background: #BBDEFB !important;" +
                        "  color: #0D47A1 !important;" +
                        "  font-weight: 600;" +
                        "}" +

                        /* Área de botones */
                        ".sec-btn-area {" +
                        "  border-top: 1px solid #E3E8EF;" +
                        "  padding-top: 16px;" +
                        "  margin-top: 4px;" +
                        "}" +

                        /* Botón Aceptar */
                        ".sec-btn-accept.v-button {" +
                        "  background: linear-gradient(135deg, #1976D2 0%, #1565C0 100%) !important;" +
                        "  color: #ffffff !important;" +
                        "  border: none !important;" +
                        "  border-radius: 6px !important;" +
                        "  font-weight: 600 !important;" +
                        "  padding: 0 28px !important;" +
                        "  height: 38px !important;" +
                        "  box-shadow: 0 2px 8px rgba(25,118,210,0.35) !important;" +
                        "  transition: box-shadow 0.2s;" +
                        "}" +
                        ".sec-btn-accept.v-button:hover {" +
                        "  box-shadow: 0 4px 16px rgba(25,118,210,0.5) !important;" +
                        "}" +

                        /* Botón Salir */
                        ".sec-btn-exit.v-button {" +
                        "  color: #607D8B !important;" +
                        "  border: 1px solid #CFD8DC !important;" +
                        "  border-radius: 6px !important;" +
                        "  background: transparent !important;" +
                        "  height: 38px !important;" +
                        "  padding: 0 20px !important;" +
                        "}" +
                        ".sec-btn-exit.v-button:hover {" +
                        "  background: #F5F5F5 !important;" +
                        "}"
        );
    }

    // ── Encabezado ───────────────────────────────────────────────────────────
    private void buildHeader() {
        String userName = ((SopdiUI) mainUI).sessionInformation.getStrUserFullName();

        // Ícono decorativo (usando un carácter Unicode — no requiere FontAwesome en el label)
        Label iconLbl = new Label("🏢");
        iconLbl.addStyleName("sec-icon");
        iconLbl.setSizeUndefined();

        // Nombre del usuario en el título
        Label titleLbl = new Label("Bienvenido, " + userName);
        titleLbl.addStyleName("sec-title");
        titleLbl.setSizeUndefined();

        Label subtitleLbl = new Label("Selecciona la empresa contable y el proyecto para continuar");
        subtitleLbl.addStyleName("sec-subtitle");
        subtitleLbl.setSizeUndefined();

        // Columna izquierda: ícono
        VerticalLayout iconCol = new VerticalLayout();
        iconCol.setMargin(false);
        iconCol.setSpacing(false);
        iconCol.setWidthUndefined();
        iconCol.addComponent(iconLbl);
        iconCol.setComponentAlignment(iconLbl, Alignment.MIDDLE_CENTER);

        // Columna derecha: título + subtítulo
        VerticalLayout textCol = new VerticalLayout();
        textCol.setMargin(false);
        textCol.setSpacing(false);
        textCol.addComponents(titleLbl, subtitleLbl);

        HorizontalLayout headerHL = new HorizontalLayout();
        headerHL.setSpacing(true);
        headerHL.setMargin(false);
        headerHL.setWidth("100%");
        headerHL.addStyleName("sec-header");
        headerHL.addComponents(iconCol, textCol);
        headerHL.setComponentAlignment(iconCol, Alignment.MIDDLE_LEFT);
        headerHL.setComponentAlignment(textCol, Alignment.MIDDLE_LEFT);
        headerHL.setExpandRatio(textCol, 1f);

        mainLayout.addComponent(headerHL);
        mainLayout.setComponentAlignment(headerHL, Alignment.TOP_CENTER);
    }

    // ── Tabla de Empresas ────────────────────────────────────────────────────
    public void createTablaEmpresas() {
        // Etiqueta de sección
        Label sectionLbl = new Label("Empresa contable");
        sectionLbl.addStyleName("sec-section-label");
        mainLayout.addComponent(sectionLbl);

        empresasTable.setSelectable(true);
        empresasTable.setWidth("100%");
        empresasTable.setHeight("200px");
        empresasTable.setPageLength(5);
        empresasTable.setImmediate(true);

        empresasTable.addContainerProperty(LOGO_PROPERTY,         Image.class,  null);
        empresasTable.addContainerProperty(ID_PROPERTY,           String.class, null);
        empresasTable.addContainerProperty(NOMBRE_PROPERTY,       String.class, null);
        empresasTable.addContainerProperty(NOMBRE_CORTO_PROPERTY, String.class, null);
        empresasTable.addContainerProperty(NIT_PROPERTY,          String.class, "");

        empresasTable.setColumnAlignments(
                Table.Align.CENTER,
                Table.Align.CENTER,
                Table.Align.LEFT,
                Table.Align.LEFT,
                Table.Align.LEFT
        );

        // Anchos de columna sugeridos
        empresasTable.setColumnWidth(LOGO_PROPERTY, 50);
        empresasTable.setColumnWidth(ID_PROPERTY,   50);

        mainLayout.addComponent(empresasTable);
        mainLayout.setComponentAlignment(empresasTable, Alignment.TOP_CENTER);
    }

    public void llenarTablaEmpresas() {
        Image empresaLogo;

        String queryString = "SELECT * ";
        queryString += "FROM contabilidad_empresa CE ";
        queryString += "INNER JOIN usuario_permisos_empresa UPE ON UPE.IdEmpresa = CE.IdEmpresa AND UPE.IdUsuario = "
                + ((SopdiUI) mainUI).sessionInformation.getStrUserId() + " ";
        queryString += "WHERE CE.IdEmpresa > 0 ";

        try {
            stQuery   = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                int primeraEmpresa = rsRecords.getInt("IdEmpresa");

                do {
                    final byte[] docBytes        = rsRecords.getBytes("Logo");
                    StreamResource logoStreamResource = null;

                    if (docBytes != null) {
                        logoStreamResource = new StreamResource(
                                () -> new ByteArrayInputStream(docBytes),
                                rsRecords.getString("IdEmpresa")
                        );
                    }

                    empresaLogo = new Image(null, logoStreamResource);
                    empresaLogo.setImmediate(true);
                    empresaLogo.setWidth("35px");
                    empresaLogo.setHeight("35px");

                    empresasTable.addItem(new Object[]{
                            empresaLogo,
                            rsRecords.getString("IdEmpresa"),
                            rsRecords.getString("Empresa"),
                            rsRecords.getString("NombreCorto"),
                            rsRecords.getString("Nit")
                    }, rsRecords.getInt("IdEmpresa"));

                } while (rsRecords.next());

                if (empresasTable.size() == 1) {
                    empresasTable.select(primeraEmpresa);
                }
            } else {
                selectBtn.setEnabled(false);
            }
        } catch (Exception ex) {
            System.out.println("Error al buscar tabla empresas contables: " + ex);
            ex.printStackTrace();
            Notification.show("ERROR AL BUSCAR EMPRESAS CONTABLES.", Notification.Type.ERROR_MESSAGE);
        }
    }

    // ── Tabla de Proyectos ───────────────────────────────────────────────────
    private void crearTableProyectos() {
        // Separador visual
        Label sep = new Label(" ");
        sep.setHeight("4px");
        mainLayout.addComponent(sep);

        // Etiqueta de sección
        Label sectionLbl = new Label("Proyecto");
        sectionLbl.addStyleName("sec-section-label");
        mainLayout.addComponent(sectionLbl);

        proyectosTable.setSelectable(true);
        proyectosTable.setWidth("100%");
        proyectosTable.setHeight("160px");
        proyectosTable.setPageLength(4);
        proyectosTable.setImmediate(true);

        proyectosTable.addContainerProperty(LOGO_PROPERTY,   Image.class,  null);
        proyectosTable.addContainerProperty(NOMBRE_PROPERTY, String.class, null);

        proyectosTable.setColumnAlignments(new Table.Align[]{
                Table.Align.CENTER,
                Table.Align.LEFT
        });

        proyectosTable.setColumnWidth(LOGO_PROPERTY, 50);

        mainLayout.addComponent(proyectosTable);
        mainLayout.setComponentAlignment(proyectosTable, Alignment.TOP_CENTER);
    }

    public void llenarTablaProyectos() {
        String queryString = "SELECT Pro.*";
        queryString += " FROM  proyecto_usuario ProUsr";
        queryString += " INNER JOIN proyecto Pro ON Pro.IdProyecto = ProUsr.IdProyecto";
        queryString += " WHERE ProUsr.IdUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();

        try {
            stQuery   = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection()
                    .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                int primerRegistro = rsRecords.getInt("IdProyecto");
                Image proyectoLogo;

                do {
                    final byte[] docBytes        = rsRecords.getBytes("Logo");
                    StreamResource logoStreamResource = null;

                    if (docBytes != null) {
                        logoStreamResource = new StreamResource(
                                () -> new ByteArrayInputStream(docBytes),
                                rsRecords.getString("IdProyecto")
                        );
                    }

                    proyectoLogo = new Image(null, logoStreamResource);
                    proyectoLogo.setImmediate(true);
                    proyectoLogo.setWidth("40px");
                    proyectoLogo.setHeight("40px");

                    proyectosTable.addItem(new Object[]{
                            proyectoLogo,
                            rsRecords.getString("Nombre")
                    }, rsRecords.getInt("IdProyecto"));

                } while (rsRecords.next());

                if (proyectosTable.size() == 1) {
                    proyectosTable.select(primerRegistro);
                }
            } else {
                selectBtn.setEnabled(false);
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectSelectionForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de proyectos: " + ex.getMessage());
            Notification.show("Error al intentar leer registros de proyectos..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    // ── Botones ──────────────────────────────────────────────────────────────
    private void crearBotones() {
        // ── Botón Aceptar ────────────────────────────────────────────────────
        selectBtn = new Button("Aceptar");
        selectBtn.setIcon(FontAwesome.CHECK);
        selectBtn.addStyleName("sec-btn-accept");
        selectBtn.addClickListener(event -> {
            if (empresasTable.getValue() != null && proyectosTable.getValue() != null) {
                setEmpresaYProyecto();
            } else {
                Notification.show(
                        "Por favor seleccione una empresa y un proyecto.",
                        Notification.Type.WARNING_MESSAGE
                );
            }
        });

        // ── Botón Salir ──────────────────────────────────────────────────────
        exitBtn = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        exitBtn.addStyleName("sec-btn-exit");
        exitBtn.addClickListener(event -> ((SopdiUI) mainUI).logOff());

        // ── Layout de botones: Salir a la izquierda, Aceptar a la derecha ───
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidth("100%");
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(new MarginInfo(true, false, false, false));
        buttonsLayout.addStyleName("sec-btn-area");

        // Spacer para empujar los botones
        Label spacer = new Label();
        buttonsLayout.addComponents(exitBtn, spacer, selectBtn);
        buttonsLayout.setExpandRatio(spacer, 1f);               // ← centra/empuja
        buttonsLayout.setComponentAlignment(exitBtn,    Alignment.MIDDLE_LEFT);
        buttonsLayout.setComponentAlignment(selectBtn,  Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    // ── Lógica de negocio (sin cambios) ─────────────────────────────────────
    private void setEmpresaYProyecto() {

        queryString = "SELECT * FROM proyecto WHERE IdProyecto = " + proyectosTable.getValue();

        try {
            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            ((SopdiUI) mainUI).sessionInformation.setStrProjectId(
                    String.valueOf(proyectosTable.getValue()));
            ((SopdiUI) mainUI).sessionInformation.setStrProjectName(
                    String.valueOf(proyectosTable.getContainerProperty(
                            proyectosTable.getValue(), NOMBRE_PROPERTY).getValue()));

            byte[] imageBytes = rsRecords.getBytes("Logo");
            if (imageBytes != null) {
                ((SopdiUI) mainUI).sessionInformation.setProjectStreamResource(
                        new StreamResource(
                                () -> new ByteArrayInputStream(imageBytes),
                                ((SopdiUI) mainUI).sessionInformation.getStrProjectId()
                        ));
            } else {
                ((SopdiUI) mainUI).sessionInformation.setProjectStreamResource(null);
            }

            queryString = "SELECT * FROM contabilidad_empresa WHERE idEmpresa = " + empresasTable.getValue();
            rsRecords   = stQuery.executeQuery(queryString);
            rsRecords.next();

            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyId(rsRecords.getString("IdEmpresa"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyName(rsRecords.getString("Empresa"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanySmallName(rsRecords.getString("NombreCorto"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyTaxId(rsRecords.getString("Nit"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyRegimen(rsRecords.getString("Regimen"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyFelUser(rsRecords.getString("UsuarioFEL"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyFelPass(rsRecords.getString("ClaveFEL"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyFelToken(rsRecords.getString("TokenFEL"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyDirection(rsRecords.getString("Direccion"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyBillingDirection(rsRecords.getString("DireccionFactura"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyFelCodProdExento(rsRecords.getString("CodigoProductoExentoFel"));

            Emisor emisor = new Emisor(
                    rsRecords.getString("Nit"),
                    rsRecords.getString("Empresa"),
                    String.valueOf(rsRecords.getString("CorreoContacto")),
                    new Direccion(
                            rsRecords.getString("DireccionFactura"),
                            rsRecords.getString("CodigoPostal"),
                            rsRecords.getString("NombreDepartamento"),
                            rsRecords.getString("NombreMunicipio"),
                            rsRecords.getString("Pais")
                    ),
                    rsRecords.getString("AfiliacionIVA"),
                    "1",
                    rsRecords.getString("NombreCorto"),
                    rsRecords.getString("InfileUsuarioApi"),
                    rsRecords.getString("InfileLlaveApi"),
                    rsRecords.getString("InfileUsuarioFirma"),
                    rsRecords.getString("InfileLlaveFirma"),
                    rsRecords.getString("TipoPersoneria")
            );
            ((SopdiUI) mainUI).sessionInformation.setInfileEmisor(emisor);

            for (Iterator<Component> it = ((SopdiUI) mainUI).getMenuItemsLayout().iterator(); it.hasNext(); ) {
                final Component c = it.next();
                if ("com.vaadin.ui.Label".equals(c.getClass().getName())) {
                    final Label label = (Label) c;
                    if (label.getValue().contains("Contabilidad")) {
                        label.setValue("Contabilidad : " + rsRecords.getString("NombreCorto"));
                        break;
                    }
                }
            }

            byte[] imageBytes1 = rsRecords.getBytes("Logo");
            if (imageBytes1 != null) {
                ((SopdiUI) mainUI).sessionInformation.setPhotoStreamResource(
                        new StreamResource(
                                () -> new ByteArrayInputStream(imageBytes1),
                                ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId()
                        ));
            } else {
                ((SopdiUI) mainUI).sessionInformation.setPhotoStreamResource(null);
            }

        } catch (Exception ex) {
            Notification.show(
                    "ERROR AL BUSCAR DATOS DE EMPRESA : " + ex.getMessage(),
                    Notification.Type.WARNING_MESSAGE
            );
            ex.printStackTrace();
        }

        ((SopdiUI) mainUI).fillCuentasContablesPorDefault();
        ((SopdiUI) mainUI).fillProveedoresInstitucionales();
        generarEmpresaCuentasEquivalentes(
                ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId());

        if (((SopdiUI) mainUI).getMenuItems().isEmpty()) {
            ((SopdiUI) mainUI).buildMainView();
        } else {
            mainUI.setContent(((SopdiUI) mainUI).getAppLayout());
            ((SopdiUI) mainUI).getUserSettingsItem().setIcon(
                    ((SopdiUI) UI.getCurrent()).sessionInformation.getPhotoStreamResource());
        }

        if (((SopdiUI) mainUI).currentViewName != null) {
            ((SopdiUI) mainUI).getNavigator().navigateTo(((SopdiUI) mainUI).currentViewName);
        }

        String empresaProyecto = "<strong>"
                + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName()
                + "<br></strong><strong>"
                + ((SopdiUI) mainUI).sessionInformation.getStrProjectName()
                + "</strong></br>";
        ((Label) ((SopdiUI) mainUI).top.getComponent(0)).setValue(empresaProyecto);

        close();
    }

    private void generarEmpresaCuentasEquivalentes(String idEmpresa) {
        String queryString = "SELECT * ";
        queryString += "FROM empresa_cuenta_equivalente ";
        queryString += "WHERE IdEmpresa = " + idEmpresa + " ";
        queryString += "ORDER BY IdEmpresa, IdNomenclatura, IdEmpresa_1, IdNomenclatura_1";

        try {
            stQuery   = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                List<Object[]> relaciones = new java.util.ArrayList<>();
                do {
                    Object nom  = rsRecords.getObject("IdNomenclatura");
                    Object emp1 = rsRecords.getObject("IdProveedor_1");
                    Object nom1 = rsRecords.getObject("IdNomenclatura_1");
                    relaciones.add(new Object[]{nom, emp1, nom1});
                } while (rsRecords.next());

                EmpresaCuentasEquivalentesHelper helper = new EmpresaCuentasEquivalentesHelper(relaciones);
                System.out.println(helper);
                ((SopdiUI) mainUI).sessionInformation.setEmpresaCuentasEquivalentesHelper(helper);
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla empresas empresa_cuenta_equivalente: " + ex);
            ex.printStackTrace();
            Notification.show(
                    "ERROR AL SELECCIONAR EMPRESAS CONTABLES EQUIVALENTES.",
                    Notification.Type.ERROR_MESSAGE
            );
        }
    }
}