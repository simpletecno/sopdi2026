package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;

public class ValidarTokenForm extends Window {

    VerticalLayout mainLayout;

    TextField tokenTxt;
    Button grabarBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    boolean supervisar;
    private String origen;

    public ValidarTokenForm(boolean supervisar) {

        this.mainUI = UI.getCurrent();
        this.supervisar = supervisar;

        setResponsive(true);
        setWidth("50%");
        setHeight("42%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setMargin(new MarginInfo(false,true, false, true));

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(true, true,false,true));
        layoutTitle.setWidth("100%");

        Label titleLbl = new Label("INGRESE EL TOKEN PARA PODER CONTINUAR");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        HorizontalLayout layoutToken = new HorizontalLayout();
        layoutToken.setSpacing(true);
        layoutToken.setMargin(new MarginInfo(false, true, true, true));
        //layoutToken.setWidth("100%");

        tokenTxt = new TextField("Token : ");
        tokenTxt.setStyleName(ValoTheme.TEXTFIELD_HUGE);        

        grabarBtn = new Button("Validar");
        grabarBtn.setIcon(FontAwesome.CHECK_CIRCLE);
        grabarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarBtn.setStyleName(ValoTheme.BUTTON_HUGE);
        grabarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                buscarToken();
            }
        });

        Label instruccionesLbl;

        if(!supervisar) {
            instruccionesLbl = new Label(
                    "<b>INGRESE EL TOKEN PROPORCIONADO POR EL GERENTE PARA PODER INGRESAR EL DOCUMENTO MAYOR A 30 DIAS.</br>\n" +
                            "<ul>" +
                            "  <li><b>POR FAVOR TOMAR EN CUENTA QUE EL TOKEN DEBE ESCRIBIRSE CON FORMATO 0000-0000.</b></li>" +
                            "</ul> ",
                    ContentMode.HTML);
        }
        else {
            instruccionesLbl = new Label(
                    "<b>INGRESE EL TOKEN PROPORCIONADO POR EL SUPERVISOR DE OBRA.</br>\n" +
                            "<ul>" +
                            "  <li><b>POR FAVOR TOMAR EN CUENTA QUE EL TOKEN DEBE ESCRIBIRSE CON FORMATO 0000-0000.</b></li>" +
                            "</ul> ",
                    ContentMode.HTML);
        }
        instruccionesLbl.addStyleName(ValoTheme.LABEL_COLORED);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        layoutToken.addComponents(tokenTxt, grabarBtn);
        layoutToken.setComponentAlignment(tokenTxt, Alignment.TOP_CENTER);
        layoutToken.setComponentAlignment(grabarBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(layoutTitle);
        mainLayout.addComponent(layoutToken);
        mainLayout.addComponent(instruccionesLbl);
        mainLayout.setComponentAlignment(layoutToken, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(instruccionesLbl, Alignment.BOTTOM_CENTER);       

        setContent(mainLayout);
    }

    public void buscarToken() {

        try {          
            
            String codigo = tokenTxt.getValue().replace("'",""); // ES PARA QUITAR LAS COMILLAS PARA REDUCIR ERRORES
            
            queryString = "SELECT *,HOUR(TIMEDIFF(FechaCreado, CURRENT_TIMESTAMP)) as difference FROM token";
            queryString += " WHERE Codigo = '" + codigo + "'";
            queryString += " AND Estatus = 'DISPONIBLE'";           

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                if (rsRecords.getInt("difference") <= 24) {
                    ((SopdiUI) UI.getCurrent()).sessionInformation.setStrUserToken(tokenTxt.getValue());
                    
                    Notification.show("TOKEN VALIDADO CON EXITO! ", Notification.Type.HUMANIZED_MESSAGE);

                    if(supervisar) {
                        queryString = "UPDATE token SET ";
                        queryString += " IdUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
                        queryString += ", Usuario = '" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName() + "'";
                        queryString += ", FechaUsado = current_timestamp";
                        queryString += ", CodigoPartida = '" + getOrigen() + "'";
                        queryString += ", Estatus = 'UTILIZADO'";
                        queryString += " Where Codigo = '" + codigo + "'";

                        stQuery.executeUpdate(queryString);
                    }
                    close();
                }else{
                    Notification notif = new Notification("ESTE TOKEN YA HA CADUCADO.",
                    Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1000);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }                
            } else {
                Notification notif = new Notification("ESTE TOKEN NO ES VALIDO POR FAVOR CONTACTE CON EL GERENTE.",
                Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1000);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }
}
