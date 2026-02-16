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
        Label huge = new Label("No disponible en esta versión!!!");
        
//        huge.setWidth("100%");
        huge.addStyleName(ValoTheme.LABEL_HUGE);
        huge.addStyleName(ValoTheme.LABEL_COLORED);
        huge.setSizeUndefined();
        addComponent(huge);
        setComponentAlignment(huge, Alignment.MIDDLE_CENTER);

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("SOPDI - NO DISPONIBLE");
    }

}
