package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class CuentasContablesForm extends Window {

    public String idNomenclaturaEdit = "0";

    String queryString = "";

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;

    MarginInfo marginInfo;

    FormLayout mainForm;

    NumberField id1Txt;
    NumberField id2Txt;
    NumberField id3Txt;
    NumberField id4Txt;
    NumberField id5Txt;
    NumberField noCuentaTxt;

    TextField reporteTxt;
    TextField n1Txt;
    TextField n2Txt;
    TextField n3Txt;
    TextField n4Txt;
    TextField n5Txt;
    CheckBox filtrarIngresoDocumentoChk;
    CheckBox filtrarFormularioLiquidacionChk;
    ComboBox tipoCbx;
    TextField codigoCCTxt;

    Button guardarBtn;
    Button salirBtn;

    public CuentasContablesForm() {        
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setModal(true);

        marginInfo = new MarginInfo(true, true, false, true);

        mainForm = new FormLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);    

        reporteTxt = new TextField("Reporte :");
        reporteTxt.setWidth("15em");
        reporteTxt.setMaxLength(128);

        id1Txt = new NumberField("ID1 :");
        id1Txt.setWidth("8em");
        id1Txt.setMaxValue(99);

        n1Txt = new TextField("N1 :");
        n1Txt.setWidth("8em");
        n1Txt.setMaxLength(128);

        id2Txt = new NumberField("ID2 :");
        id2Txt.setWidth("8em");
        id2Txt.setMaxValue(99);

        n2Txt = new TextField("N2 :");
        n2Txt.setWidth("10em");
        n2Txt.setMaxLength(128);       

        id3Txt = new NumberField("ID3 :");
        id3Txt.setWidth("8em");
        id3Txt.setMaxValue(99);

        n3Txt = new TextField("N3 :");
        n3Txt.setWidth("10em");
        n3Txt.setMaxLength(128);

        id4Txt = new NumberField("ID4 :");
        id4Txt.setWidth("8em");
        id4Txt.setMaxValue(99);       

        n4Txt = new TextField("N4 :");
        n4Txt.setWidth("10em");       

        id5Txt = new NumberField("ID5 :");      
        id5Txt.setWidth("8em");

        noCuentaTxt = new NumberField("No.Cuenta :");
        noCuentaTxt.setWidth("8em");
        noCuentaTxt.setGroupingUsed(false);
        noCuentaTxt.setMaxValue(999999999);

        n5Txt = new TextField("N5 :");
        n5Txt.setWidth("25em");

        filtrarIngresoDocumentoChk = new CheckBox("Filtrar ingreso documentos :");
        filtrarIngresoDocumentoChk.setValue(false);

        filtrarFormularioLiquidacionChk = new CheckBox("Filtrar formulario liquidaciones :");
        filtrarFormularioLiquidacionChk.setValue(false);

        tipoCbx = new ComboBox("Tipo :");
        tipoCbx.setNewItemsAllowed(false);
        tipoCbx.setNullSelectionAllowed(false);
        tipoCbx.setInvalidAllowed(false);
        tipoCbx.addItem("N/A");
        tipoCbx.addItem("COMPRA");
        tipoCbx.addItem("VENTA");
        tipoCbx.addItem("SERVICIO");
        tipoCbx.select("N/A");

        codigoCCTxt = new TextField("CodigoCC :");
        codigoCCTxt.setWidth("25em");

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertarCuentaContable();
            }
        });

        mainForm.addComponents(reporteTxt,id1Txt,n1Txt);
        mainForm.addComponents(id2Txt,n2Txt,id3Txt,n3Txt);
        mainForm.addComponents(id4Txt,n4Txt,id5Txt,noCuentaTxt,n5Txt);
        mainForm.addComponents(filtrarIngresoDocumentoChk,filtrarFormularioLiquidacionChk,tipoCbx,codigoCCTxt);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(salirBtn,guardarBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(buttonsLayout);
        mainForm.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        Label titleLbl = new Label("Nomenclatura de cuentas");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();
        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(mainForm);
        contentLayout.setComponentAlignment(mainForm, Alignment.TOP_CENTER);

        setContent(contentLayout);

    }

    public void llenarCampos() {
        try {

            queryString = " SELECT *";
            queryString += " FROM contabilidad_nomenclatura";
            queryString += " WHERE IdNomenclatura = " + idNomenclaturaEdit;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                reporteTxt.setValue(rsRecords.getString("Reporte"));
                id1Txt.setValue(rsRecords.getString("ID1"));
                n1Txt.setValue(rsRecords.getString("N1"));
                id2Txt.setValue(rsRecords.getString("ID2"));
                n2Txt.setValue(rsRecords.getString("N2"));
                id3Txt.setValue(rsRecords.getString("ID3"));
                n3Txt.setValue(rsRecords.getString("N3"));
                id4Txt.setValue(rsRecords.getString("ID4"));
                n4Txt.setValue(rsRecords.getString("N4"));
                id5Txt.setValue(rsRecords.getString("ID5"));
                noCuentaTxt.setValue(rsRecords.getString("NoCuenta"));
                n5Txt.setValue(rsRecords.getString("N5"));
                filtrarIngresoDocumentoChk.setValue(rsRecords.getString("FiltrarIngresoDocumentos").equals("S"));
                filtrarFormularioLiquidacionChk.setValue(rsRecords.getString("FiltrarFormularioLiquidacion").equals("S"));
                tipoCbx.select(rsRecords.getString("Tipo"));
                codigoCCTxt.setValue(rsRecords.getString("CodigoCC"));
            }
        } catch (SQLException ex) {
            System.out.println("Error al llenar Campos " + ex);
            ex.printStackTrace();
        }
    }

    public void insertarCuentaContable() {
        try {

            if (idNomenclaturaEdit.equals("0")) {
                queryString = "INSERT INTO contabilidad_nomenclatura ";
                queryString += "(Reporte, ID1,N1,ID2,N2,ID3,N3,ID4,N4,ID5,NoCuenta,N5,";
                queryString += "FiltrarIngresoDocumentos, FiltrarFormularioLiquidacion, Tipo, CodigoCC)";
                queryString += " VALUES (";
                queryString += " '" + reporteTxt.getValue() + "'";
                queryString += "," + id1Txt.getValue();
                queryString += ",'" + n1Txt.getValue() + "'";
                queryString += "," + id2Txt.getValue();
                queryString += ",'" + n2Txt.getValue() + "'";
                queryString += "," + id3Txt.getValue();
                queryString += ",'" + n3Txt.getValue() + "'";
                queryString += "," + id4Txt.getValue();
                queryString += ",'" + n4Txt.getValue() + "'";
                queryString += "," + id5Txt.getValue();
                queryString += "," + noCuentaTxt.getValue();
                queryString += ",'" + n5Txt.getValue() + "'";
                queryString += ",'" + (filtrarIngresoDocumentoChk.getValue() ? "S" : "N") + "'";
                queryString += ",'" + (filtrarFormularioLiquidacionChk.getValue() ? "S" : "N") + "'";
                queryString += ",'" + tipoCbx.getValue() + "'";
                queryString += ",'" + codigoCCTxt.getValue() + "'";
                queryString += ")";
            } else {
                queryString = "UPDATE contabilidad_nomenclatura SET ";
                queryString += " Reporte ='" + reporteTxt.getValue() + "'";
                queryString += ",ID1 =" + id1Txt.getValue();
                queryString += ",N1  ='" + n1Txt.getValue() + "'";
                queryString += ",ID2 =" + id2Txt.getValue();
                queryString += ",N2  ='" + n2Txt.getValue() + "'";
                queryString += ",ID3 =" + id3Txt.getValue();
                queryString += ",N3  ='" + n3Txt.getValue() + "'";
                queryString += ",ID4 =" + id4Txt.getValue();
                queryString += ",N4  ='" + n4Txt.getValue() + "'";
                queryString += ",ID5 =" + id5Txt.getValue();
                queryString += ",NoCuenta =" + noCuentaTxt.getValue();
                queryString += ",N5 ='" + n5Txt.getValue() + "'";
                queryString += ",FiltrarIngresoDocumentos ='" + (filtrarIngresoDocumentoChk.getValue() ? "S" : "N") + "'";
                queryString += ",FiltrarFormularioLiquidacion ='" + (filtrarFormularioLiquidacionChk.getValue() ? "S" : "N") + "'";
                queryString += ",Tipo ='" + tipoCbx.getValue() + "'";
                queryString += ",CodigoCC ='" + codigoCCTxt.getValue() + "'";
                queryString += " WHERE IdNomenclatura = " + idNomenclaturaEdit;
            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((CuentasContablesView) (mainUI.getNavigator().getCurrentView())).llenarTablaCuentas();

            Notification notif = new Notification("REGISTRO AGREGADO o MODIFICADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

        } catch (SQLException ex) {
            System.out.println("Error al insertar o editar " + queryString);
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            ex.printStackTrace();
        }
    }

    public void llenarCamposInsert(String idNomenclaturaNuevo) {

        queryString = " SELECT *";
        queryString += " FROM contabilidad_nomenclatura";
        queryString += " WHERE IdNomenclatura = " + idNomenclaturaNuevo;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                reporteTxt.setValue(rsRecords.getString("Reporte"));
                id1Txt.setValue(rsRecords.getString("ID1"));
                n1Txt.setValue(rsRecords.getString("N1"));
                id2Txt.setValue(rsRecords.getString("ID2"));
                n2Txt.setValue(rsRecords.getString("N2"));
                id3Txt.setValue(rsRecords.getString("ID3"));
                n3Txt.setValue(rsRecords.getString("N3"));
                id4Txt.setValue(rsRecords.getString("ID4"));
                n4Txt.setValue(rsRecords.getString("N4"));
                id5Txt.setValue(rsRecords.getString("ID5"));
                noCuentaTxt.setValue(rsRecords.getString("NoCuenta"));
                n5Txt.setValue(rsRecords.getString("N5"));
                filtrarIngresoDocumentoChk.setValue((rsRecords.getString("FiltrarIngresoDocumentos").trim().equals("S")));
                filtrarFormularioLiquidacionChk.setValue((rsRecords.getString("FiltrarFormularioLiquidacion").trim().equals("S")));
                tipoCbx.setValue(rsRecords.getString("Tipo"));
                codigoCCTxt.setValue(rsRecords.getString("CodigoCC"));
            }
        } catch (Exception ex) {
            System.out.println("Error al llenar Campos para un nuevo registro" + ex);
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            ex.printStackTrace();
        }

    }

}
