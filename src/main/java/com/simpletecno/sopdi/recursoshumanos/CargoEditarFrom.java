package com.simpletecno.sopdi.recursoshumanos;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;


public class CargoEditarFrom extends Window {

    static String CARGO_PROPERTY = "Cargo / Puesto";
    static String DESCRIPCION_PROPERTY = "Descripcion";

    FormLayout mainForm;
    TextField cargoTxt;
    TextArea descripcionTxt;
    Button guardarBtn;

    String cargo;
    String descripcion;
    Boolean cambiarNombre;
    Object item;


    UI mainUI;

    public CargoEditarFrom(String cargo, String descripcion, Object item, Boolean cambiarNombre) {
        this.cargo = cargo;
        this.descripcion = descripcion;
        this.item = item;
        this.cambiarNombre = cambiarNombre;
        this.mainUI = UI.getCurrent();
        setResponsive(true);

        mainForm = new FormLayout();
        MarginInfo marginInfo = new MarginInfo(false,true,true,true);
        mainForm.setMargin(marginInfo);
        mainForm.setSpacing(true);

        cargoTxt = new TextField("Nombre : ");
        cargoTxt.setWidth("20em");
        cargoTxt.setValue(cargo);
        cargoTxt.setReadOnly(cambiarNombre);

        descripcionTxt = new TextArea("Descripcion:");
        descripcionTxt.setWidth("50em");
        descripcionTxt.setHeight("10em");
        descripcionTxt.setMaxLength(1024);
        descripcionTxt.setDescription("Ingrese hasta 1024 caracteres...");
        descripcionTxt.setValue(descripcion);

        guardarBtn = new Button("Modificar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                guardar();
            }
        });

        Button salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(guardarBtn);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(cargoTxt);
        mainForm.addComponent(descripcionTxt);
        mainForm.addComponent(buttonsLayout);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);

        Label titleLbl = new Label("Cargo o Puesto");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        HorizontalLayout titleLayout2 = new HorizontalLayout();
        titleLayout2.setWidth("100%");
        titleLayout2.setMargin(new MarginInfo(false,false,true, false));

        contentLayout.addComponent(titleLayout2);
        contentLayout.setComponentAlignment(titleLayout2, Alignment.TOP_CENTER);

        contentLayout.addComponent(mainForm);
        contentLayout.setComponentAlignment(mainForm, Alignment.TOP_CENTER);

        setContent(contentLayout);

    }


    public void guardar() {
        if(!cambiarNombre) {
            ((CargosPuestosView) (mainUI.getNavigator().getCurrentView())).addEmpyCargo(false);
        }
        ((CargosPuestosView) (mainUI.getNavigator().getCurrentView())).cargoContainer.getContainerProperty(item, CARGO_PROPERTY).setValue(cargoTxt.getValue());
        ((CargosPuestosView) (mainUI.getNavigator().getCurrentView())).cargoContainer.getContainerProperty(item, DESCRIPCION_PROPERTY).setValue(descripcionTxt.getValue());

        close();

    }

}
