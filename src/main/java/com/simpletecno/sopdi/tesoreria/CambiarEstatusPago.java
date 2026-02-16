/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author user
 */
public class CambiarEstatusPago extends Window {

    String queryString;
    FormLayout mainForm;
    ComboBox estatusCbx;
    TextField numeroTxt;

    Button guardarBtn;
    Statement stQuery, stQuery1,stQuery2 ;
    ResultSet rsRecords, rsRecords1, rsRecords2;

    UI mainUI;

    IndexedContainer container;
    Object itemId;
    String codigoPartida;
    String monto;

    public CambiarEstatusPago(
            IndexedContainer container,
            Object itemId,
            String codigoPartida,
            String monto) {

        this.container = container;
        this.itemId = itemId;
        this.codigoPartida = codigoPartida;
        this.mainUI = UI.getCurrent();
        setResponsive(true);

        System.out.println("-->CambiarEstatusPago() --> codigoPartida=" + codigoPartida);

        MarginInfo marginInfo = new MarginInfo(false, true, true, true);

        mainForm = new FormLayout();
        mainForm.setMargin(marginInfo);
        mainForm.setSpacing(true);

        numeroTxt = new TextField("# Documento : ");
        numeroTxt.setValue(String.valueOf(container.getItem(itemId).getItemProperty(HistorialPagoView.DOCUMENTO_PROPERTY).getValue()));
        numeroTxt.setWidth("8em");

        estatusCbx = new ComboBox("Estatus :");
        estatusCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setWidth("14em");
        estatusCbx.addItem("CONCILIADO");
        estatusCbx.addItem("INGRESADO");
        estatusCbx.addItem("ANULADO");
        estatusCbx.select(String.valueOf(container.getItem(itemId).getItemProperty(HistorialPagoView.ESTATUS_PROPERTY).getValue()));

        guardarBtn = new Button("Cambiar datos");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CAMBIAR los datos de este cheque?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    cambiarEstatus();
                                }
                            }
                        }
                );
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

        mainForm.addComponent(estatusCbx);
        mainForm.addComponent(numeroTxt);
        mainForm.addComponent(buttonsLayout);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);

        Label titleLbl = new Label("Cambio de estatus/documento");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(mainForm);
        contentLayout.setComponentAlignment(mainForm, Alignment.TOP_CENTER);

        setContent(contentLayout);

    }

    public void cambiarEstatus() {
        try {

            queryString = " Update contabilidad_partida Set ";
            queryString += " Estatus = '" + String.valueOf(estatusCbx.getValue()) + "'";
            queryString += ",NumeroDocumento = '" + numeroTxt.getValue() + "'";
            queryString += " Where CodigoPartida = '" + codigoPartida + "'";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

System.out.println("Cambiar estatus Y NUMERO del cheque=" + queryString);

            container.getItem(itemId).getItemProperty(HistorialPagoView.ESTATUS_PROPERTY).setValue(String.valueOf(estatusCbx.getValue()));
            container.getItem(itemId).getItemProperty(HistorialPagoView.DOCUMENTO_PROPERTY).setValue(numeroTxt.getValue());

        } catch (SQLException ex) {
            System.out.println("error" + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
