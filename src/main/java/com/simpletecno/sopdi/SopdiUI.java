package com.simpletecno.sopdi;

import com.simpletecno.sopdi.configuracion.CuentasContablesDefault;
import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.seguridad.*;
import com.simpletecno.sopdi.configuracion.ProveedoresInstitucionales;
import com.simpletecno.sopdi.seguridad.LoginForm;
import com.simpletecno.sopdi.utilerias.FontAwesomeUtil;
import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.utilerias.TasaCambioForm;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.vaadin.annotations.*;

import javax.servlet.annotation.WebServlet;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.xml.ws.BindingProvider;

import org.vaadin.dialogs.ConfirmDialog;
import com.vaadin.shared.Position;


/* Comando para genera el web service
 * java <= v8
 * sudo ./wsimport -keep -d banguat "https://banguat.gob.gt/variables/ws/TipoCambio.asmx?wsdl" */
import banguat.*;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of a html page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@Theme("tests-valo-facebook")
@Title("SOPDI")
//@StyleSheet("valo-theme-ui.css")
@PreserveOnRefresh
public class SopdiUI extends UI implements Button.ClickListener {

    String empresaProyecto = "";
    public HorizontalLayout top;

    private boolean testMode = false;
    String loginMsg;
    com.simpletecno.sopdi.seguridad.LoginForm loginForm;
    Statement stQuery = null;
    ResultSet rsRecords = null;

    Tree treeMainMenu = new Tree();

    public MenuBar userSettings;
    private MenuBar.MenuItem userSettingsItem;

    public MyDatabaseProvider databaseProvider;
    public SessionInformation sessionInformation;
    public CuentasContablesDefault cuentasContablesDefault;
    public ProveedoresInstitucionales proveedoresInstitucionales;

    public String tipoCambioDolar = "0.00";
    public EnvironmentVars enviromentsVars = new EnvironmentVars();
    InfoVariable result;

    private MenuLayout appLayout = new MenuLayout();

    ComponentContainer viewDisplay = appLayout.getContentContainer();
    CssLayout mainMenuLayout = new CssLayout();
    private CssLayout menuItemsLayout = new CssLayout();
    {
        mainMenuLayout.setId("testMenu");
    }
    private Navigator navigator;
    private LinkedHashMap<String, String> menuItems = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> orderMenuItems = new LinkedHashMap<String, String>();
    private final LinkedHashMap<String, FontAwesome> iconItems = new LinkedHashMap<String, FontAwesome>();

    public ThemeResource projectLogo = new ThemeResource("img/logo_nisa.png");
    public Embedded projectCover = new Embedded(null, projectLogo);

    public CssLayout getMenuItemsLayout() {
        return menuItemsLayout;
    }

    public LinkedHashMap<String, String> getMenuItems() {
        return menuItems;
    }

    public MenuLayout getAppLayout() {
        return appLayout;
    }

    public MenuBar.MenuItem getUserSettingsItem() {
        return userSettingsItem;
    }

    @WebServlet(urlPatterns = "/*", name = "SopdiUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SopdiUI.class, productionMode = true)
    public static class SopdiUIServlet extends VaadinServlet {

        @Override
        protected void servletInitialized() throws ServletException {
            super.servletInitialized();
            getService().addSessionInitListener(
                    new MySessionInitListener());
        }
    }

    public String currentViewName;

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        setSizeFull();

        getPage().setTitle("SOPDI v 4.0.0");

        setLocale(Locale.US);

        addDetachListener(new DetachListener() {

            @Override
            public void detach(DetachEvent event) {
                logOff();
            }
        });

        //FontAwesome.BUILDING_O

        if (vaadinRequest.getParameter("test") != null) {
            testMode = true;

            if (browserCantRenderFontsConsistently()) {
                getPage().getStyles().add(
                        ".v-app.v-app.v-app {font-family: Sans-Serif;}");
            }
        }
        if (getPage().getWebBrowser().isIE()
                && getPage().getWebBrowser().getBrowserMajorVersion() == 9) {
            mainMenuLayout.setWidth("320px");
        }
        if (!testMode) {
            Responsive.makeResponsive(this);
        }

        if (vaadinRequest.getParameter("test") != null) {
            testMode = true;

            if (browserCantRenderFontsConsistently()) {
                getPage().getStyles().add(
                        ".v-app.v-app.v-app {font-family: Sans-Serif;}");
            }
        }
        addStyleName(ValoTheme.UI_WITH_MENU);

        treeMainMenu.addValueChangeListener(event -> { // Java 8
            if (event.getProperty() != null &&
                    event.getProperty().getValue() != null) {
                if(treeMainMenu.getParent(event.getProperty().getValue()) != null) {
                    getNavigator().navigateTo(event.getProperty().getValue().toString());
                }
            }
        });

        getMenuItemsLayout().addComponent(treeMainMenu);

        buildLoginView();
    }

    private boolean browserCantRenderFontsConsistently() {
        // PhantomJS renders font correctly about 50% of the time, so
        // disable it to have consistent screenshots
        // https://github.com/ariya/phantomjs/issues/10592

        // IE8 also has randomness in its font rendering...
        return getPage().getWebBrowser().getBrowserApplication()
                .contains("PhantomJS")
                || (getPage().getWebBrowser().isIE() && getPage()
                .getWebBrowser().getBrowserMajorVersion() <= 9);
    }

    static boolean isTestMode() {
        return ((SopdiUI) getCurrent()).testMode;
    }

    public void buildLoginView() {
        getUI().getPage().setLocation("");
        loginForm = new LoginForm();
        setContent(loginForm);
        addStyleName("loginview");
        loginForm.userName.focus();
    }

    public boolean validateUser(String userName, String passWord) {

        try {

            if (!connectToDB()) {
                return false;
            }
            stQuery = databaseProvider.getCurrentConnection().createStatement();

            String queryString;

            queryString = "SELECT Usr.*, Prv.Codigo CodigoProveedor,  ";
            queryString += "Emp.Nombre EmpresaNombre, Emp.Estatus EmpresaEstatus, Emp.Logo ";
            queryString += " FROM       usuario Usr ";
            queryString += " INNER JOIN empresa Emp On Emp.IdEmpresa = Usr.IdEmpresa ";// And Upper(Emp.Alias) = '" + loginView.farmName.getValue().trim().toUpperCase() + "'";
            queryString += " LEFT  JOIN proveedor_empresa Prv On Usr.IdUsuario = Prv.IdUsuario";
            queryString += " WHERE Upper(Usr.Usuario)  = '" + userName.toUpperCase() + "'";
//            queryString += " AND Prv.IdEmpresa = " + sessionInformation.getStrCompanyId() + " ";
            if (databaseProvider.getUsedDBDataSource().equals("MYSQL")) {
                queryString += " AND  Usr.Clave    = Sha1('" + passWord + "')";
            } else {
                queryString += " AND  Usr.Clave    = SUBSTRING(master.dbo.fn_varbintohexstr(HASHBYTES('SHA1', '" + passWord + "')),3,40)";
            }

//System.out.println("\nLogin="+queryString);

            stQuery = databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) { //  encontrado

                if (rsRecords.getString("Estatus").toUpperCase().compareTo("INACTIVO") == 0) {
                    Notification.show("Usuario tiene estatus INACTIVO, por favor consulte a su administrador!", Notification.Type.HUMANIZED_MESSAGE);
                    loginMsg = "Usuario INACTIVO!";
                    return false;
                }
                if (rsRecords.getString("EmpresaEstatus").toUpperCase().compareTo("INACTIVA") == 0) {
                    Notification.show("Su empresa tiene estatus INACTIVA, por favor consulte a su administrador!", Notification.Type.HUMANIZED_MESSAGE);
                    loginMsg = "Empresa INACTIVA!";
                    return false;
                }

//                loginLayout.removeAllComponents();
                sessionInformation = new SessionInformation();

                sessionInformation.setStrUserId(rsRecords.getString("IdUsuario"));
                sessionInformation.setStrUserName(rsRecords.getString("Usuario"));
                sessionInformation.setStrUserFullName(rsRecords.getString("Nombre"));
                sessionInformation.setStrUserProfile(rsRecords.getString("Perfil"));
                sessionInformation.setStrUserProfileName(rsRecords.getString("Perfil"));
//                sessionInformation.setStrCompanyId(rsRecords.getString("IdEmpresa"));
//                sessionInformation.setStrCompanyName(rsRecords.getString("EmpresaNombre"));
                sessionInformation.setStrAliasName(loginForm.farmName.getValue().trim().toUpperCase());
                sessionInformation.setStrLastLogin(rsRecords.getString("UltimoLogin"));
                sessionInformation.setStrUserSpecialCode(rsRecords.getString("CodigoEspecial"));
//                if(rsRecords.getObject("GrupoTrabajo") != null) {
//                    sessionInformation.setStrGupoTrabajo(rsRecords.getString("GrupoTrabajo"));
//                }
                if(rsRecords.getObject("CodigoProveedor") != null) {
                    sessionInformation.setStrIdProveedor(rsRecords.getString("CodigoProveedor"));
                }

// System.out.println(rsRecords.getString("IdProveedor"));

//                if(rsRecords.getObject("CodigoProveedor") != null) {
//                    sessionInformation.setStrAccountingCompanyId(rsRecords.getString("IdEmpresaContable"));
//                    sessionInformation.setStrAccountingCompanyName(rsRecords.getString("NombreEmpresaContable"));
//                }

//System.out.println("idproveedor= " + sessionInformation.getStrIdProveedor());

                sessionInformation.setPhotoStreamResource(null);

                getPage().setTitle("Sopdi / " + sessionInformation.getStrCompanyName());

                final byte[] docBytes = rsRecords.getBytes("Logo");

                if (docBytes != null) {
                    sessionInformation.setPhotoStreamResource(new StreamResource(
                            new StreamResource.StreamSource() {
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(docBytes);
                                }
                            }, rsRecords.getString("IdUsuario")
                    ));
                }

                if (databaseProvider.getUsedDBDataSource().equals("MYSQL")) {
                    rsRecords = stQuery.executeQuery("SELECT UUID() AS SESION_UNICA");
                } else { // otro motor de base de datos
                    rsRecords = stQuery.executeQuery("SELECT UUID() AS SESION_UNICA");
                }
                rsRecords.next();

                String sessionUnica = rsRecords.getString("SESION_UNICA");
                sessionInformation.setStrSessionId(sessionUnica);

                if (sessionInformation.getStrLastLogin() != null) {
                    stQuery.executeUpdate("Update usuario set UltimoLogin = current_timestamp Where IdUsuario = " + sessionInformation.getStrUserId());
                }

                actualizarTasaCambio();

            } else {
                Notification.show("Usuario incorrecto o contraseña incorrecta, intente de nuevo!", Notification.Type.WARNING_MESSAGE);
                loginMsg = "Usuario incorrecto o contraseña incorrecta. <span>Intente de nuevo o registrese.</span>";
                return false;
            }
        }
        catch(Exception ex1) {
            Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex1);
            System.out.println("Error : " + ex1.getMessage());
            Notification.show("Error al intentar acceder al sistema SOPDI..!", Notification.TYPE_ERROR_MESSAGE);
            ex1.printStackTrace();

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error al consultar base de datos (login)..! " + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }

            return false;
        }

        return true;
    }

    public void actualizarTasaCambio() {

        try {

            String queryString = " SELECT * ";
            queryString += " FROM contabilidad_tasa_cambio";
            queryString += " WHERE Fecha = current_date";

//System.out.println("query contabilidad_tasa_cambio = " + queryString);

            try {
                stQuery = databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (!rsRecords.next()) {
                    TipoCambio service = new TipoCambio() ;
                    TipoCambioSoap port = service.getTipoCambioSoap();

                    ((BindingProvider) port).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, 5000); // Timeout in millis
                    ((BindingProvider) port).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, 3000); // Timeout in millis
                    System.setProperty("com.sun.xml.internal.ws.connect.timeout ", "5000");
                    System.setProperty("com.sun.xml.internal.ws.request.timeout", "3000");

                    result = port.tipoCambioDia();

                    tipoCambioDolar = String.valueOf(result.getCambioDolar().getVarDolar().get(0).getReferencia());

                    sessionInformation.setFltlExchangeRate(result.getCambioDolar().getVarDolar().get(0).getReferencia());

                    queryString = " INSERT INTO contabilidad_tasa_cambio (Fecha, Tasa, CreadoUsuario, CreadoFechaYHora) Values ( ";
                    queryString += " current_date()";
                    queryString += "," + tipoCambioDolar;
                    queryString += "," + sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ")";

                    stQuery.executeUpdate(queryString);
                }
                else {
                    tipoCambioDolar = rsRecords.getString("Tasa");
                    sessionInformation.setFltlExchangeRate(rsRecords.getFloat("Tasa"));
                }

            } catch (Exception ex1) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, "Error al leer tabla contabilidad_tasa_cambio :  " + ex1.getMessage());
                ex1.printStackTrace();
            }

        } catch (Exception tcEx) {
            Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, "ERROR AL INTENTAR OBTENER LA TASA DE CAMBIO: " + tcEx.getMessage());
        }
    }

    public double getTasaCambioDelDia(String fecha) {
        double tipoCambio = 1.00;

        String queryString = " SELECT * ";
        queryString += " FROM contabilidad_tasa_cambio";
        queryString += " WHERE Fecha = '" + fecha + "'";

//System.out.println(queryString);

        try {
            stQuery = databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                tipoCambio = rsRecords.getDouble("Tasa");
            }
        } catch (Exception ex1) {
            Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, "Error al leer tabla contabilidad_tasa_cambio :  " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return tipoCambio;
    }

    public boolean connectToDB() {
        try {

            if (databaseProvider == null) {
                databaseProvider = new MyDatabaseProvider();
                databaseProvider.getNewConnection();
            }

            if (databaseProvider.getCurrentConnection() == null) {

                databaseProvider = null;

                Notification.show("PROBLEMA AL CONECTARSE A BASE DE DATOS, POR FAVOR CONTACTE A TECNOLOGIA DE INFORMACION!!!", Notification.Type.ERROR_MESSAGE);

                String emailRecipients[] = {"alerta@simpletecno.com"};

                MyEmailMessanger eMail = new MyEmailMessanger();

                try {
                    eMail.postMail(emailRecipients, "Error SOPDI ", "NO HAY CONEXION A BASE DE DATOS");
                } catch (Exception ex) {
                    Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, "SIN CONEXION A BASE DE DATOS", ex);
                }

                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("PROBLEMA AL CONECTARSE A BASE DE DATOS, POR FAVOR CONTACTE AL DESARROLLADOR.", Notification.Type.ERROR_MESSAGE);

            return false;
        }

        Logger.getLogger(SopdiUI.class.getName()).log(Level.INFO, "CONECTADO A BASE DE DATOS OK!!!");

        return true;
    }

    public void buildMainView() {

        removeStyleName("loginview");

        if (getPage().getWebBrowser().isIE()
                && getPage().getWebBrowser().getBrowserMajorVersion() == 9) {
            mainMenuLayout.setWidth("320px");
        }

        if (!testMode) {
            Responsive.makeResponsive(this);
        }

        getPage().setTitle("SOPDI 4.0");
        addStyleName(ValoTheme.UI_WITH_MENU);

        navigator = new Navigator(this, viewDisplay);

        String queryString = " SELECT * ";
        queryString += " FROM usuario_permisos UP";
        queryString += " INNER JOIN opcion_menu OM ON OM.IdOpcionMenu = UP.IdOpcionMenu";
        queryString += " WHERE UP.IdUsuario = " + sessionInformation.getStrUserId();
        queryString += " AND OM.Estatus = 'ACTIVO'";
        queryString += " ORDER BY OM.Orden ASC";

//System.out.println(queryString);

        try {
            stQuery = databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    try {
                        Class<?> navClass = Class.forName(rsRecords.getString("Clase"));
                        getNavigator().addView(rsRecords.getString("Opcion"), (Class<? extends View>) navClass);
                    } catch (Exception ignored) {
                    }
                } while (rsRecords.next());
            } else {
                Notification.show("Usuario NO TIENE PERMISOS!", Notification.Type.WARNING_MESSAGE);
                loginMsg = "Usuario NO TIENE PERMISOS!</span>";
            }

        } catch (Exception ex1) {
            Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, "Error al leer tabla usuario_permisos :  " + ex1.getMessage());
            ex1.printStackTrace();
        }

        getNavigator().setErrorView(ErrorView.class);
        getNavigator().setErrorView(AccessDeniedView.class);

        String f = Page.getCurrent().getUriFragment();
        if (f == null || f.equals("")) {
            navigator.navigateTo("dashboardGerencial");
        }

        getNavigator().addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {

                for (Iterator<Component> it = getMenuItemsLayout().iterator(); it
                        .hasNext();) {
                    it.next().removeStyleName("selected");
                }
                currentViewName = event.getViewName();
//System.out.println("currentViewName=" + currentViewName) ;
                /**
                 * for (Entry<String, String> item : menuItems.entrySet()) { if
                 * (event.getViewName().equals(item.getKey())) { for
                 * (Iterator<Component> it = menuItemsLayout .iterator();
                 * it.hasNext();) { Component c = it.next(); if (c.getCaption()
                 * != null && c.getCaption() .startsWith(item.getValue())) {
                 * c.addStyleName("selected"); break; } } break; } }
                 *
                 */
                for (final Entry<String, String> item : getMenuItems().entrySet()) {
                    if (event.getViewName().equals(item.getKey())) {
                        for (final Iterator<Component> it = getMenuItemsLayout()
                                .iterator(); it.hasNext();) {
                            final Component c = it.next();
                            if (c.getCaption() != null
                                    && c.getCaption().startsWith(
                                            item.getValue())) {
                                break;
                            }
                        }
                        break;
                    }
                }

                mainMenuLayout.removeStyleName("valo-menu-visible");
            }
        });

        setContent(appLayout);
        appLayout.setWidth("100%");
        appLayout.addMenu(buildMenu());

//        fillCuentasContablesPorDefault();

    }

    CssLayout buildMenu() {

        String queryString = " SELECT * ";
        queryString += " FROM usuario_permisos UP";
        queryString += " INNER JOIN opcion_menu OM ON OM.IdOpcionMenu = UP.IdOpcionMenu";
        queryString += " WHERE UP.IdUsuario = " + sessionInformation.getStrUserId();
        queryString += " AND OM.Estatus = 'ACTIVO'";
        queryString += " ORDER BY OM.Orden Asc";

// System.out.println(queryString);

        try {
            stQuery = databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    try {
                        getMenuItems().put(rsRecords.getString("Opcion"), rsRecords.getString("Descripcion"));
                        iconItems.put(rsRecords.getString("Opcion"), FontAwesomeUtil.fromName(rsRecords.getString("Icono")));
                        orderMenuItems.put(rsRecords.getString("Opcion"), rsRecords.getString("Orden"));
                    }
                    catch (Exception ignored)
                    {
                    }
                } while(rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al intntentar leer tabla contabilidad_empresa_cierre :  " + ex1.getMessage());
            ex1.printStackTrace();
        }

        top = new HorizontalLayout();
        top.setWidth("100%");
        top.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        top.addStyleName(ValoTheme.MENU_TITLE);
        mainMenuLayout.addComponent(top);

        Button showMenu = new Button("Menu", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (mainMenuLayout.getStyleName().contains("valo-menu-visible")) {
                    mainMenuLayout.removeStyleName("valo-menu-visible");
                } else {
                    mainMenuLayout.addStyleName("valo-menu-visible");
                }
            }
        });

        showMenu.addStyleName(ValoTheme.BUTTON_PRIMARY);
        showMenu.addStyleName(ValoTheme.BUTTON_SMALL);
        showMenu.addStyleName("valo-menu-toggle");
        showMenu.setIcon(FontAwesome.LIST);
        mainMenuLayout.addComponent(showMenu);

        empresaProyecto = "<strong>" + sessionInformation.getStrAccountingCompanyName() + "<br></strong><strong>" + sessionInformation.getStrProjectName() + "</strong></br>";

        Label title = new Label(empresaProyecto,
                ContentMode.HTML);
        title.setSizeUndefined();
        top.addComponent(title);
        top.setExpandRatio(title, 1);

        MenuBar settings = new MenuBar();
        settings.addStyleName("user-menu");
        // Define a common menu command for all the menu items.
        MenuBar.Command mycommand = selectedItem -> {
            if (selectedItem.getId() == 3) { // preferencias
                UserPreferences userPreferences = new UserPreferences();
                getUI().addWindow(userPreferences);
            }
            if (selectedItem.getId() == 4) { // cambiar clave
                ChangePassword changePassword = new ChangePassword(sessionInformation.getStrUserId(), sessionInformation.getStrUserName(), sessionInformation.getStrLastLogin());
                getUI().addWindow(changePassword);
                changePassword.center();
                changePassword.txtPasswordActual.focus();
            }
            if (selectedItem.getId() == 6) { // TIPO DE CAMBIO

                if(   sessionInformation.getStrUserProfileName().contains("SUPERVISOR")
                   || sessionInformation.getStrUserProfileName().contains("MAESTRO")
                   || sessionInformation.getStrUserProfileName().contains("JEFE")
                ) {
                    Notification.show("OPERACION NO PERMITIDA PARA USUARIO.", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                try {
                    if(tipoCambioDolar.equals("0.00") || tipoCambioDolar.trim().isEmpty()) {
                        TasaCambioForm tasaCambioForm = new TasaCambioForm(tipoCambioDolar);
                        getUI().addWindow(tasaCambioForm);
                        tasaCambioForm.center();
                    }
                    else {
                        selectedItem.setText("Tipo de cambio = " + tipoCambioDolar);

                        Notification notif = new Notification("El tipo de cambio de hoy es : " + tipoCambioDolar,
                                Notification.Type.HUMANIZED_MESSAGE);
                        notif.setDelayMsec(1500);
                        notif.setPosition(Position.MIDDLE_CENTER);
                        notif.setIcon(FontAwesome.MONEY);

                        // Show it in the page
                        notif.show(Page.getCurrent());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Notification.show("Error al conectar al API de Banguat para Tipo de Cambio.", Notification.Type.ERROR_MESSAGE);
                }
            }
            if (selectedItem.getId() == 8) { // ELEGIR EMPRESA
                if(   sessionInformation.getStrUserProfileName().contains("SUPERVISOR")
                        || sessionInformation.getStrUserProfileName().contains("MAESTRO")
                        || sessionInformation.getStrUserProfileName().contains("JEFE")
                ) {
                    Notification.show("OPERACION NO PERMITIDA PARA USUARIO.", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                SelectEmpresaContable selectEmpresa = new SelectEmpresaContable();
                UI.getCurrent().addWindow(selectEmpresa);
                selectEmpresa.center();
            }
            if (selectedItem.getId() == 10) {
                getNavigator().navigateTo("ErrorView");
/*
                String basePath = VaadinService.getCurrent()
                        .getBaseDirectory().getAbsolutePath();
                String filePath = basePath + "/ManualUsuario/Manual.html";

                FileResource resource = new FileResource(new File(filePath));
                Window window = new Window();
                window.setWidth(90, Unit.PERCENTAGE);
                window.setHeight(95, Unit.PERCENTAGE);
                window.setModal(true);
                window.center();
//                    BrowserFrame pdf = new BrowserFrame("test", new ExternalResource(fileResource));
                BrowserFrame pdf = new BrowserFrame("Sopdi -- HELP", new ExternalResource("http://65.111.164.179:8080/sopdihelp/"));
                pdf.setSizeFull();
                window.setContent(pdf);
                getUI().addWindow(window);
*/
            }
            if (selectedItem.getId() == 12) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de salir del sistema?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            logOff();
                        }
                    }
                });
            }
        };

        settings.addStyleName("menu-logo-empresa");
        settings.setAutoOpen(true);

        userSettingsItem = settings.addItem(sessionInformation.getStrUserFullName(),
                sessionInformation.getPhotoStreamResource(),
                null);
        userSettingsItem.addItem("Preferencias", FontAwesome.HEART, mycommand).setDescription("Preferencias del usuario.");
        userSettingsItem.addItem("Cambiar clave", FontAwesome.USER_SECRET, mycommand).setDescription("Cambio de contraseña/clave.");
        userSettingsItem.addSeparator();
        userSettingsItem.addItem("Tipo de cambio", FontAwesome.DOLLAR, mycommand).setDescription("Tipo de cambio del dollar"); //6
        userSettingsItem.addSeparator();
        userSettingsItem.addItem("Elegir empresa", FontAwesome.CC, mycommand).setDescription("Elegir empresa"); //8
        userSettingsItem.addSeparator();
        userSettingsItem.addItem("Ayuda", FontAwesome.BOOK, mycommand).setDescription("Ayuda del sistema."); //12
        userSettingsItem.addSeparator();
        userSettingsItem.addItem("Salir", FontAwesome.SIGN_OUT, mycommand).setDescription("Salir (logout) del sistema."); //10
        mainMenuLayout.addComponent(settings);

        getMenuItemsLayout().setPrimaryStyleName("valo-menuitems");
        mainMenuLayout.addComponent(getMenuItemsLayout());

        Label label = null;
        int count = -1;
        String parent = "";

        for (final Entry<String, String> item : getMenuItems().entrySet()) {

            if(orderMenuItems.get(item.getKey()).trim().length() == 1) {
                label = new Label(item.getValue(), ContentMode.HTML);
                label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
                label.addStyleName(ValoTheme.LABEL_H4);
                label.setSizeUndefined();
                //getMenuItemsLayout().addComponent(label);
                treeMainMenu.addItem(item.getKey());
                treeMainMenu.setItemCaption(item.getKey(), item.getValue());
                treeMainMenu.setItemIcon(item.getKey(), iconItems.get(item.getKey()));
                parent = item.getKey();
           }
            else {

//                Button b = new Button(item.getValue(), new Button.ClickListener() {
//                    @Override
//                    public void buttonClick(Button.ClickEvent event) {
//                        if (event.getButton().getCaption().contains("Impuestos")) {
//                            ImpuestosYOtros impuestosYOtros = new ImpuestosYOtros();
//                            UI.getCurrent().addWindow(impuestosYOtros);
//                            impuestosYOtros.center();
//                        } else if (event.getButton().getCaption().contains("Cierre mensual")) {
//                            CierreMensual cierreMensual = new CierreMensual();
//                            UI.getCurrent().addWindow(cierreMensual);
//                            cierreMensual.center();
//                        } else {
//                            getNavigator().navigateTo(item.getKey());
//                        }
//
//                    }
//                });
//                b.setHtmlContentAllowed(true);
//                b.setPrimaryStyleName(ValoTheme.MENU_ITEM);
//                b.setIcon(iconItems.get(item.getKey()));
                //getMenuItemsLayout().addComponent(b);
                treeMainMenu.addItem(item.getKey());
                treeMainMenu.setItemCaption(item.getKey(), item.getValue());
                treeMainMenu.setChildrenAllowed(item.getKey(), false);
                treeMainMenu.setParent(item.getKey(), parent);
                treeMainMenu.setItemIcon(item.getKey(), iconItems.get(item.getKey()));
            }
            /**
             * if (item.getKey().equals("disponibles")) {
             * b.setCaption(b.getCaption() + " <span class=\"valo-menu-badge\">"
             * + getTotalAvailable() + "</span>"); }
             *
             */
//            if (count == 2) {
//                b.setCaption(b.getCaption()
//                        + " <span class=\"valo-menu-badge\">123</span>");
//            }
            count++;
        }

        return mainMenuLayout;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {

        if (event.getButton().getId().compareTo("LOGIN") == 0) {
            if (validateUser(loginForm.userName.getValue(), loginForm.password.getValue())) {
                if (sessionInformation.getStrUserProfile().compareTo("SUPER USUARIO") != 0) {
                    if (sessionInformation.getStrLastLogin() == null) {
                        getUI().addWindow(new ChangePassword(sessionInformation.getStrUserId(), sessionInformation.getStrUserName(), sessionInformation.getStrLastLogin()));
                    }

                    SelectEmpresaContable selectEmpresa = new SelectEmpresaContable();
                    UI.getCurrent().addWindow(selectEmpresa);
                    selectEmpresa.center();

                } else {
                    buildMainView();
                }
            } else {

                if (loginForm.loginPanel.getComponentCount() > 3) {
                    // Remove the previous error message
                    loginForm.loginPanel.removeComponent(loginForm.loginPanel.getComponent(3));
                }
                // Add new error message
                Label error = new Label(loginMsg, ContentMode.HTML);
                error.addStyleName("error");
                error.setSizeUndefined();
                error.addStyleName("light");
                // Add animation
                error.addStyleName("v-animate-reveal");
                loginForm.loginPanel.addComponent(error);
                loginForm.userName.focus();
            }
        } else {
            Notification.show(event.getButton().getCaption() + " NO DISPONIBLE EN ESTA VERSION!!", Notification.Type.TRAY_NOTIFICATION);
        }
    }

    public void logOff() {

        try {
            if (databaseProvider != null) {
                databaseProvider.getCurrentConnection().close();
            }
            databaseProvider = null;
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
        if (getNavigator() != null) {
            getNavigator().destroy();
        }
        if (VaadinService.getCurrentRequest() != null) {
            VaadinService.getCurrentRequest().getWrappedSession().invalidate();
            VaadinSession.getCurrent().close();
        }

        getSession().close();

        getUI().getPage().setLocation("");

        setContent(null);
    }

    /* Valida si el mes contable del documento a ingresar  esta abierto contablemente
    * @param idEmpresa String id de a empresa
    * @param mesDocumento  String fecha del documento
    * @return falso o verdadero
     */
    public boolean esMesCerrado(String idEmpresa, String mesDocumento) {
        boolean isClosed = false;

        String queryString = " SELECT * FROM contabilidad_empresa_cierre";
        queryString += " WHERE IdEmpresa = " + idEmpresa;
        queryString += " AND Mes = '" + mesDocumento.substring(0, 4) + mesDocumento.substring(5, 7) + "'";
        queryString += " AND Estatus = 'CERRADO'";

        //System.out.println(queryString);
        try {
            stQuery = databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                isClosed = true;
            }

        } catch (Exception ex1) {
            System.out.println("Error al intntentar leer tabla contabilidad_empresa_cierre : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return isClosed;
    }

    /* Valida si el mes contable del documento a ingresar  es el primer mes abierto (no cerrado) contablemente
    * @param idEmpresa String id de a empresa
    * @param mesDocumento  String fecha del documento
    * @return falso o verdadero
     */
    public boolean esPrimerMesAbierto(String idEmpresa, String mesDocumento) {
        boolean isCorrect = false;

        return true;

//        String queryString = " SELECT Mes from contabilidad_empresa_cierre";
//        queryString += " where IdEmpresa = " + idEmpresa;
//        queryString += " And Estatus = 'ABIERTO'";
//        queryString += " Limit 1";
//
//        //System.out.println(queryString);
//
//        try {
//            stQuery = databaseProvider.getCurrentConnection().createStatement();
//            rsRecords = stQuery.executeQuery(queryString);
//
//            if(rsRecords.next()) {
//                if(rsRecords.getString("Mes").equals(mesDocumento)) {
//                    isCorrect = true;
//                }
//            }
//
//        } catch (Exception ex1) { System.out.println("Error al intentar leer tabla contabilidad_empresa_cierre : " + ex1.getMessage());
//            ex1.printStackTrace();
//        }
//        return isCorrect;
    }

    /* Retorna el primer mes abierto a operaciones contablemente
    * @param idEmpresa String id de la empresa
    * @return String
     */
    public String primerMesAbierto(String idEmpresa) {
        String mes = "";

        String queryString = " SELECT Mes FROM contabilidad_empresa_cierre";
        queryString += " WHERE IdEmpresa = " + idEmpresa;
        queryString += " AND Estatus = 'ABIERTO'";
        queryString += " LIMIT 1";

//System.out.println(queryString);
        try {
            stQuery = databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                mes = rsRecords.getString("Mes");
            }

        } catch (Exception ex1) {
            System.out.println("Error al intntentar leer tabla contabilidad_empresa_cierre :  " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return mes;
    }

    public void fillCuentasContablesPorDefault() {
        String queryString = " SELECT * ";
        queryString += " FROM cuentas_contables_default";
        queryString += " WHERE IdEmpresa = " + sessionInformation.getStrAccountingCompanyId();

        cuentasContablesDefault = new CuentasContablesDefault();

        try {
            stQuery = databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                cuentasContablesDefault.setProveedores(rsRecords.getString("Proveedores"));
                cuentasContablesDefault.setClientes(rsRecords.getString("Clientes"));
                cuentasContablesDefault.setInstituciones(rsRecords.getString("Instituciones"));
                cuentasContablesDefault.setBancosMonedaLocal(rsRecords.getString("BancosMonedaLocal"));
                cuentasContablesDefault.setBancosMonedaExtranjera(rsRecords.getString("BancosMonedaExtranjera"));
                cuentasContablesDefault.setAnticiposProveedor(rsRecords.getString("AnticiposProveedor"));
                cuentasContablesDefault.setAnticiposClientes(rsRecords.getString("AnticiposClientes"));
                cuentasContablesDefault.setLiquidacionesCajaChicha(rsRecords.getString("LiquidacionesCajaChicha"));
                cuentasContablesDefault.setCompras(rsRecords.getString("Compras"));
                cuentasContablesDefault.setVentas(rsRecords.getString("Ventas"));
                cuentasContablesDefault.setAbastos(rsRecords.getString("Abastos"));
                cuentasContablesDefault.setEnganches(rsRecords.getString("Enganches"));
                cuentasContablesDefault.setIvaPorCobrar(rsRecords.getString("IvaPorCobrar"));
                cuentasContablesDefault.setIvaPorPagar(rsRecords.getString("IvaPorPagar"));
                cuentasContablesDefault.setEfectivoEnTransito(rsRecords.getString("EfectivoEnTransito"));
                cuentasContablesDefault.setDiferencialCambiario(rsRecords.getString("DiferencialCambiario"));
                cuentasContablesDefault.setPrestamos(rsRecords.getString("Prestamos"));
                cuentasContablesDefault.setInteresesPrestamo(rsRecords.getString("InteresesPrestamo"));
                cuentasContablesDefault.setInteresesDevengados(rsRecords.getString("InteresesDevengados"));
                cuentasContablesDefault.setAnticiposHonorarios(rsRecords.getString("AnticipoHonorarios"));
                cuentasContablesDefault.setAnticiposSueldos(rsRecords.getString("AnticipoSueldos"));
                cuentasContablesDefault.setIsrGasto(rsRecords.getString("IsrGasto"));
                cuentasContablesDefault.setIsrPorPagar(rsRecords.getString("IsrPorPagar"));
                cuentasContablesDefault.setIsrRetenidoPorPagar(rsRecords.getString("IsrRetenidoPorPagar"));
                cuentasContablesDefault.setIsrOpcionalMensualPorPagar(rsRecords.getString("IsrOpcionalMensualPorPagar"));
                cuentasContablesDefault.setSueldosPorPagar(rsRecords.getString("SueldosPorPagar"));
                cuentasContablesDefault.setRedondeo(rsRecords.getString("Redondeo"));
                cuentasContablesDefault.setMultasYRectificaciones(rsRecords.getString("MultasYRectificaciones"));
                cuentasContablesDefault.setCuotaPatronalIgssPorPagar(rsRecords.getString("CuotaPatronalIgssPorPagar"));
                cuentasContablesDefault.setCuotaLaboralIgssPorPagar(rsRecords.getString("CuotaLaboralIgssPorPagar"));
                cuentasContablesDefault.setCuotaPatronalIgss(rsRecords.getString("CuotaPatronalIgss"));
                cuentasContablesDefault.setOtrosArbitrios(rsRecords.getString("OtrosArbitrios"));
                cuentasContablesDefault.setProvisionCompras(rsRecords.getString("ProvisionCompras"));
                cuentasContablesDefault.setServiciosBancos(rsRecords.getString("ServiciosBancos"));
                cuentasContablesDefault.setChequesDevueltos(rsRecords.getString("ChequesDevueltos"));
                cuentasContablesDefault.setPerdidasGananciasEjercicioAnterior(rsRecords.getString("PerdidasGananciasEjercicioAnterior"));
                cuentasContablesDefault.setSueldoOrdinario(rsRecords.getString("SueldoOrdinario"));
                cuentasContablesDefault.setSueldoExtraordinario(rsRecords.getString("SueldoExtraordinario"));
                cuentasContablesDefault.setBonificacionDCTO07_2001(rsRecords.getString("Bono37_2001"));
                cuentasContablesDefault.setBonificacionDCTO78_89(rsRecords.getString("Bono78_89"));
                cuentasContablesDefault.setAguinaldo(rsRecords.getString("Aguinaldo"));
                cuentasContablesDefault.setBono14(rsRecords.getString("Bono14"));
                cuentasContablesDefault.setProvisionAguinaldo(rsRecords.getString("ProvisionAguinaldo"));
                cuentasContablesDefault.setProvisionBono14(rsRecords.getString("ProvisionBono14"));
                cuentasContablesDefault.setIndemnizacion(rsRecords.getString("Indemnizacion"));
                cuentasContablesDefault.setVacaciones(rsRecords.getString("Vacaciones"));
                cuentasContablesDefault.setAcreedoresCortoPlazo(rsRecords.getString("AcreedoresCortoPlazo"));
                cuentasContablesDefault.setChequesTesoreria(rsRecords.getString("ChequesTesoreria"));
                cuentasContablesDefault.setIvaRetenidoPorPagar(rsRecords.getString("IvaRetenidoPorPagar"));
                cuentasContablesDefault.setTituloAccion(rsRecords.getString("TituloAccion"));
                cuentasContablesDefault.setTituloAccion2(rsRecords.getString("TituloAccion2"));
            }
        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,"Error al leer tabla cuentas_contables_default:  " + ex1.getMessage());
            Notification.show("ERROR AL OBTENER CUENTAS CONTABLES DEFAULT : " + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }
    }

    public void fillProveedoresInstitucionales() {
        String queryString = " SELECT * ";
        queryString += " FROM proveedor_institucionales";
        queryString += " WHERE IdEmpresa = " + sessionInformation.getStrAccountingCompanyId();

        proveedoresInstitucionales = new ProveedoresInstitucionales();

        try {
            stQuery = databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                proveedoresInstitucionales.setSat(rsRecords.getString("SAT"));
                proveedoresInstitucionales.setIgss(rsRecords.getString("IGSS"));
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO,"Proveedores Institucionales| sat: " + proveedoresInstitucionales.getSat() + ", : " + proveedoresInstitucionales.getIgss());

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,"Error al leer tabla proveedor_uso_recurrente:  " + ex1.getMessage());
            Notification.show("ERROR AL OBTENER PROVEEDORES INSTITUCIONALES : " + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }
    }

    /**
     * @return the navigator
     */
    public Navigator getNavigator() {
        return navigator;
    }
}
