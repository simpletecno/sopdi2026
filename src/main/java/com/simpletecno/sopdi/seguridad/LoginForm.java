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
                "Bienvenido a SOPDI");
    
    public LoginForm() {
        setSizeFull();

        Component loginForm = buildLoginForm();
        addComponent(loginForm);
        setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);
        setResponsive(true);
/**

        Notification notification = new Notification(
                "Welcome to Dashboard Demo");
        notification
                .setDescription("<span>This application is not real, it only demonstrates an application built with the <a href=\"https://vaadin.com\">Vaadin framework</a>.</span> <span>No username or password is required, just click the <b>Sign In</b> button to continue.</span>");
        notification.setHtmlContentAllowed(true);
        notification.setStyleName("tray dark small closable login-help");
        notification.setPosition(Position.BOTTOM_CENTER);
        notification.setDelayMsec(20000);
        notification.show(Page.getCurrent());
**/
        
        notification
                .setDescription("<span>Administración operativa</span> <span>Ingrese el usuario y contraseña, luego haga click en el botón Entrar</span>");
        notification.setHtmlContentAllowed(true);
        notification.setStyleName("tray dark small closable login-help");
        notification.setPosition(Position.BOTTOM_CENTER);
        notification.setDelayMsec(5000);
        notification.show(Page.getCurrent());

    }

    private Component buildLoginForm() {
        loginPanel.setSizeUndefined();
        loginPanel.setSpacing(true);
        loginPanel.setImmediate(true);
        Responsive.makeResponsive(loginPanel);
        loginPanel.addStyleName("login-panel");

        loginPanel.addComponent(buildLabels());
        loginPanel.addComponent(buildFields());
        
        Label instructionsLbl = new Label("Desarrollado por Nisa, S.A");
        instructionsLbl.setContentMode(ContentMode.HTML);
        instructionsLbl.setSizeUndefined();
        instructionsLbl.addStyleName(ValoTheme.LABEL_H4);
        instructionsLbl.addStyleName(ValoTheme.LABEL_COLORED);
//        loginPanel.addComponent(new CheckBox("Recordarme", false));
        loginPanel.addComponent(instructionsLbl);
        
        return loginPanel;
    }

    private Component buildFields() {
        fields.setSpacing(true);
        fields.addStyleName("fields");

        farmName.setIcon(FontAwesome.BUILDING);
        farmName.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        farmName.addListener(new TextChangeListener() {
            @Override
            public void textChange(TextChangeEvent event) {
        
//System.out.println("\nfarmName->ComponentCount=" + loginPanel.getComponentCount());

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
        password.setWidth("200px");

        signIn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        signIn.setClickShortcut(KeyCode.ENTER);       

//        fields.addComponents(farmName, userName, password, signIn);
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

        Label title = new Label("Sistema Operativo SOPDI");
        title.setSizeUndefined();
        title.addStyleName(ValoTheme.LABEL_H3);
        title.addStyleName(ValoTheme.LABEL_LIGHT);
        labels.addComponent(title);
        return labels;
    } 
}
