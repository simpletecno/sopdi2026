package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class LoginForm extends VerticalLayout {

    public final VerticalLayout loginPanel = new VerticalLayout();
    public final HorizontalLayout fields = new HorizontalLayout();
    public final TextField farmName = new TextField("SOPDI");
    public final TextField userName = new TextField("Usuario");
    public final PasswordField password = new PasswordField("Contraseña");
    public final Button signIn = new Button("Entrar");

    public final Notification notification = new Notification(
                "Bienvenido a SOPDI 4.0");

    public LoginForm() {
        setSizeFull();

        Component loginForm = buildLoginForm();
        addComponent(loginForm);
        setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);

        setResponsive(true);

        notification
                .setDescription("<span>Administración operativa</span> <span>Ingrese el usuario y contraseña, luego haga click en el botón Entrar</span>");
        notification.setHtmlContentAllowed(true);
        notification.setStyleName("tray dark small closable login-help");
        notification.setPosition(Position.BOTTOM_CENTER);
        notification.setDelayMsec(5000);
        notification.show(Page.getCurrent());

        injectPageEnhancements();
    }

    private Component buildLoginForm() {
        loginPanel.setSizeUndefined();
        loginPanel.setSpacing(true);
        loginPanel.setImmediate(true);
        Responsive.makeResponsive(loginPanel);
        loginPanel.addStyleName("login-panel");

        loginPanel.addComponent(buildLogo());
        loginPanel.addComponent(buildLabels());
        loginPanel.addComponent(buildFields());

        Label instructionsLbl = new Label("Desarrollado por Visionara, S.A");
        instructionsLbl.setContentMode(ContentMode.HTML);
        instructionsLbl.setSizeUndefined();
        instructionsLbl.addStyleName(ValoTheme.LABEL_H4);
        instructionsLbl.addStyleName(ValoTheme.LABEL_COLORED);
        loginPanel.addComponent(instructionsLbl);

        return loginPanel;
    }

    private Component buildLogo() {
        String svg =
            "<div style='text-align:center; padding:6px 0;'>" +
            "<svg xmlns='http://www.w3.org/2000/svg' width='90' height='90' viewBox='0 0 90 90'>" +
            "  <rect width='90' height='90' rx='14' fill='#1A237E'/>" +
            "  <polygon points='45,8 80,28 10,28' fill='#3949AB'/>" +
            "  <rect x='10' y='26' width='70' height='5' rx='1' fill='#5C6BC0'/>" +
            "  <rect x='17' y='31' width='8' height='27' rx='2' fill='#9FA8DA'/>" +
            "  <rect x='31' y='31' width='8' height='27' rx='2' fill='#9FA8DA'/>" +
            "  <rect x='51' y='31' width='8' height='27' rx='2' fill='#9FA8DA'/>" +
            "  <rect x='65' y='31' width='8' height='27' rx='2' fill='#9FA8DA'/>" +
            "  <rect x='10' y='58' width='70' height='5' rx='1' fill='#5C6BC0'/>" +
            "  <rect x='10' y='63' width='70' height='8' rx='3' fill='#3949AB'/>" +
            "  <circle cx='45' cy='20' r='10' fill='#FFD54F' stroke='#FF8F00' stroke-width='1.5'/>" +
            "  <text x='45' y='25' fill='#4E342E' font-family='Arial,sans-serif' font-size='13' text-anchor='middle' font-weight='bold'>$</text>" +
            "  <text x='45' y='82' fill='#C5CAE9' font-family='Arial,sans-serif' font-size='8' text-anchor='middle' letter-spacing='2'>SOPDI</text>" +
            "</svg>" +
            "</div>";
        Label logo = new Label(svg, ContentMode.HTML);
        logo.setSizeUndefined();
        return logo;
    }

    private Component buildFields() {
        fields.setSpacing(true);
        fields.addStyleName("fields");

        farmName.setIcon(FontAwesome.BUILDING);
        farmName.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        farmName.addListener(new TextChangeListener() {
            @Override
            public void textChange(TextChangeEvent event) {
                if (loginPanel.getComponentCount() == 4) {
                    loginPanel.removeComponent(loginPanel.getComponent(3));
                }
            }
        });

        userName.setIcon(FontAwesome.USER);
        userName.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        userName.addListener(new TextChangeListener() {
            @Override
            public void textChange(TextChangeEvent event) {
                if (loginPanel.getComponentCount() == 4) {
                    loginPanel.removeComponent(loginPanel.getComponent(3));
                }
            }
        });

        userName.setValue("");
        userName.setInputPrompt("Ingrese su usuario");
        userName.setWidth("200px");
        userName.focus();

        password.setIcon(FontAwesome.LOCK);
        password.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        password.addListener(new TextChangeListener() {
            @Override
            public void textChange(TextChangeEvent event) {
                if (loginPanel.getComponentCount() == 4) {
                    loginPanel.removeComponent(loginPanel.getComponent(3));
                }
            }
        });
        password.setValue("");
        password.setInputPrompt("Ingrese su contraseña");
        password.setWidth("200px");

        signIn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        signIn.setClickShortcut(KeyCode.ENTER);

        fields.addComponents(userName, password, signIn);
        fields.setComponentAlignment(signIn, Alignment.BOTTOM_LEFT);

        signIn.setId("LOGIN");
        signIn.addClickListener(((SopdiUI) UI.getCurrent()));

        return fields;
    }

    private Component buildLabels() {
        CssLayout labels = new CssLayout();
        labels.addStyleName("labels");

        Label welcome = new Label("Bienvenido");
        welcome.setSizeUndefined();
        welcome.addStyleName(ValoTheme.LABEL_H4);
        welcome.addStyleName(ValoTheme.LABEL_COLORED);
        labels.addComponent(welcome);

        Label title = new Label("Sistema ERP SOPDI");
        title.setSizeUndefined();
        title.addStyleName(ValoTheme.LABEL_H3);
        title.addStyleName(ValoTheme.LABEL_LIGHT);
        labels.addComponent(title);

        return labels;
    }

    private void injectPageEnhancements() {
        Page.getCurrent().getJavaScript().execute(
            // Asegura que Inter esté disponible si el @import SCSS no carga (ej. primera carga sin caché)
            "(function() {" +
            "  if (!document.getElementById('sopdi-inter-font')) {" +
            "    var link = document.createElement('link');" +
            "    link.id   = 'sopdi-inter-font';" +
            "    link.rel  = 'stylesheet';" +
            "    link.href = 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap';" +
            "    document.head.appendChild(link);" +
            "  }" +
            "})();" +

            // --- Browser info: reintenta hasta que document.body esté listo ---
            "(function tryInject(attempts) {" +
            "  if (!document.body) { if (attempts > 0) window.setTimeout(function(){ tryInject(attempts-1); }, 200); return; }" +
            "  if (document.getElementById('sopdi-browser-badge')) return;" +
            "  var ua   = navigator.userAgent;" +
            "  var name = 'Desconocido';" +
            "  if      (/Edg\\//.test(ua))              name = 'Microsoft Edge';" +
            "  else if (/OPR\\//.test(ua))              name = 'Opera';" +
            "  else if (/Chrome\\//.test(ua))           name = 'Google Chrome';" +
            "  else if (/Firefox\\//.test(ua))          name = 'Mozilla Firefox';" +
            "  else if (/Safari\\//.test(ua))           name = 'Apple Safari';" +
            "  else if (/Trident\\/|MSIE/.test(ua))     name = 'Internet Explorer';" +
            "  var d = document.createElement('div');" +
            "  d.id = 'sopdi-browser-badge';" +
            "  d.style.cssText = 'position:fixed;bottom:6px;left:10px;font-size:10px;" +
            "    font-family:Inter,sans-serif;color:#aaa;z-index:9999;pointer-events:none;';" +
            "  d.innerHTML = '&#9656; Navegador: <b>' + name + '</b>';" +
            "  document.body.appendChild(d);" +
            "})(10);"
        );
    }
}