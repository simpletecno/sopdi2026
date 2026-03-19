/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class ErrorView extends VerticalLayout implements View {

    public ErrorView() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.setSizeFull();
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        Label icon = new Label("🚫");
        icon.setStyleName(ValoTheme.LABEL_HUGE);

        Label title = new Label("Acceso Denegado o Funcionalidad No Disponible");
        title.addStyleName(ValoTheme.LABEL_H2);
        title.addStyleName(ValoTheme.LABEL_COLORED);

        Label detail = new Label("Esta opción no está disponible en esta versión del sistema.");
        detail.addStyleName(ValoTheme.LABEL_LARGE);

        layout.addComponents(icon, title, detail);

        addComponent(layout);
        setComponentAlignment(layout, Alignment.MIDDLE_CENTER);

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("SOPDI - NO DISPONIBLE");
    }

}
