package com.simpletecno.sopdi;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

public class AccessDeniedView extends VerticalLayout implements View {

    private static final String HOME_VIEW = "/sopdi"; // pon "dashboard" o tu home real si quieres

    public AccessDeniedView() {
        buildUI();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // sin lógica extra; fijo
    }

    private void buildUI() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);

        // Contenedor FULL para centrar
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setMargin(true);
        wrapper.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        // “Card” central
        VerticalLayout card = new VerticalLayout();
        card.setWidth("560px");
        card.setSpacing(true);
        card.setMargin(true);
        card.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        // Look corporativo (Valo)
        card.addStyleName(ValoTheme.LAYOUT_CARD);
        card.addStyleName(ValoTheme.LAYOUT_WELL);

        // Icono grande (truco: Button icon-only)
        Button icon = new Button();
        icon.setEnabled(false);
        icon.setIcon(FontAwesome.LOCK);
        icon.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        icon.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        icon.setWidthUndefined();
        icon.setHeightUndefined();
        icon.addStyleName("access-icon"); // opcional si luego quieres CSS

        Label title = new Label("Acceso denegado");
        title.addStyleName(ValoTheme.LABEL_H2);
        title.addStyleName(ValoTheme.LABEL_BOLD);
        title.addStyleName(ValoTheme.LABEL_FAILURE);

        Label message = new Label("No tiene permisos para acceder a esta opción.");
        message.addStyleName(ValoTheme.LABEL_LARGE);

        Label hint = new Label("Si cree que es un error, contacte al administrador del sistema.");
        hint.addStyleName(ValoTheme.LABEL_SMALL);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button home = new Button("Ir a Inicio");
        home.setIcon(FontAwesome.HOME);
        home.addStyleName(ValoTheme.BUTTON_PRIMARY);
        home.addClickListener(e -> navigateTo(HOME_VIEW));

        Button request = new Button("Solicitar acceso");
        request.setIcon(FontAwesome.ENVELOPE);
        request.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        request.addClickListener(e -> Notification.show(
                "Solicitud registrada",
                "Puede enviar esto a soporte o registrar un ticket.",
                Notification.Type.TRAY_NOTIFICATION));

        actions.addComponents(home, request);

        card.addComponents(icon, title, message, hint, actions);

        // Centrado real (clave)
        wrapper.addComponent(card);
        wrapper.setExpandRatio(card, 1f);
        wrapper.setComponentAlignment(card, Alignment.MIDDLE_CENTER);

        addComponent(wrapper);
    }

    private void navigateTo(String viewName) {
        if (UI.getCurrent() != null && UI.getCurrent().getNavigator() != null) {
            UI.getCurrent().getNavigator().navigateTo(viewName != null ? viewName : "");
        }
    }
}