package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.configuracion.CuentasContablesView;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author user
 */
public class EmpresaCuentaEquivalenteForm extends Window {

    static final String CUENTA = "Cuenta";

    public String idempresa = null;
    public String idempresa_1 = null;

    public String idnomenclatura = null;
    public String idnomenclatura_1 = null;

    Boolean esNuevo = true;
    String queryString = "";
    int id;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;

    HorizontalLayout mainForm;

    FormLayout formEmisor;
    FormLayout formReceptor;

    TextField idEmpresaTxt;
    ComboBox empresasCbx;
    TextField idNomenclaturaTxt;
    ComboBox nomenclaturasCbx;

    TextField idEmpresa_1Txt;
    ComboBox empresas_1Cbx;
    TextField idNomenclatura_1Txt;
    ComboBox nomenclaturas_1Cbx;

    CheckBox inhabilitadoChk;

    Button guardarBtn;
    Button salirBtn;

    public EmpresaCuentaEquivalenteForm() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setModal(true);

        setWidth("77%");
        setResponsive(true);
        setResizable(true);

        mainForm = new HorizontalLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(new MarginInfo(false, true, true, true));
        mainForm.setSpacing(false);

        createEmisorSection();

        Label arrowLbl = new Label();
        arrowLbl.setIcon(FontAwesome.ARROW_RIGHT);
        arrowLbl.setWidth("100%");
        arrowLbl.setSizeUndefined();
        arrowLbl.setResponsive(true);

        createReceptorSection();

        mainForm.addComponents(formEmisor, arrowLbl, formReceptor);

        mainForm.setExpandRatio(formEmisor, 2);
        mainForm.setExpandRatio(arrowLbl, 0.1f);
        mainForm.setExpandRatio(formReceptor, 2);

        mainForm.setComponentAlignment(formEmisor, Alignment.TOP_CENTER);
        mainForm.setComponentAlignment(arrowLbl, Alignment.MIDDLE_CENTER);
        mainForm.setComponentAlignment(formReceptor, Alignment.TOP_CENTER);

        llenarComboEmpresa();
        llenarComboNomenclatura();

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

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(salirBtn,guardarBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        Label titleLbl = new Label("Nomenclatura de cuentas");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(new MarginInfo(true, true, false, true));
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();
        contentLayout.setWidth("100%");
        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(mainForm);
        contentLayout.setComponentAlignment(mainForm, Alignment.TOP_CENTER);
        contentLayout.addComponent(buttonsLayout);
        contentLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        setContent(contentLayout);

    }

    private void createEmisorSection() {
        formEmisor = new FormLayout();
        formEmisor.setWidth("98%");
        formEmisor.addStyleName("rcorners3");
        formEmisor.setResponsive(true);
        formEmisor.setMargin(true);
        formEmisor.setSpacing(true);
        formEmisor.setCaption("Emisor");

        HorizontalLayout empresaLayout = new HorizontalLayout();
        empresaLayout.setWidth("100%");
        empresaLayout.setSpacing(true);
        empresaLayout.setCaption("Empresa :");

        HorizontalLayout nomenclaturaLayout = new HorizontalLayout();
        nomenclaturaLayout.setWidth("100%");
        nomenclaturaLayout.setSpacing(true);
        nomenclaturaLayout.setCaption("Nomenclatura :");

        idEmpresaTxt = new NumberField();
        idEmpresaTxt.setMaxLength(3);
        idEmpresaTxt.setWidth("100%");
        idEmpresaTxt.setEnabled(false);

        empresasCbx = new ComboBox();
        empresasCbx.setWidth("100%");
        empresasCbx.setNullSelectionAllowed(false);
        empresasCbx.addValueChangeListener(event -> {
            if (event.getProperty().getValue() != null) {
                idEmpresaTxt.setValue(event.getProperty().getValue().toString());
            } else {
                idEmpresaTxt.setValue("");
            }
        });

        idNomenclaturaTxt = new NumberField();
        idNomenclaturaTxt.setMaxLength(6);
        idNomenclaturaTxt.setWidth("100%");
        idNomenclaturaTxt.setEnabled(false);

        nomenclaturasCbx = new ComboBox();
        nomenclaturasCbx.setWidth("100%");
        nomenclaturasCbx.setNullSelectionAllowed(false);
        nomenclaturasCbx.addContainerProperty(CUENTA, Integer.class, null);
        nomenclaturasCbx.addValueChangeListener(event -> {
            if (event.getProperty().getValue() != null) {
                Integer cuenta = (Integer) nomenclaturasCbx.getContainerProperty(event.getProperty().getValue(), CUENTA).getValue();
                idnomenclatura = event.getProperty().getValue().toString();
                idNomenclaturaTxt.setValue(String.valueOf(cuenta));
            } else {
                idNomenclaturaTxt.setValue("");
            }
        });

        empresaLayout.addComponents(idEmpresaTxt, empresasCbx);
        empresaLayout.setExpandRatio(idEmpresaTxt, 0.1f);
        empresaLayout.setExpandRatio(empresasCbx, 0.9f);

        nomenclaturaLayout.addComponents(idNomenclaturaTxt, nomenclaturasCbx);
        nomenclaturaLayout.setExpandRatio(idNomenclaturaTxt, 0.22f);
        nomenclaturaLayout.setExpandRatio(nomenclaturasCbx, 0.78f);

        formEmisor.addComponents(empresaLayout, nomenclaturaLayout);
    }

    private void createReceptorSection() {
        formReceptor = new FormLayout();
        formReceptor.setWidth("98%");
        formReceptor.addStyleName("rcorners3");
        formReceptor.setResponsive(true);
        formReceptor.setMargin(true);
        formReceptor.setSpacing(true);
        formReceptor.setCaption("Receptor");

        HorizontalLayout empresaLayout = new HorizontalLayout();
        empresaLayout.setWidth("100%");
        empresaLayout.setSpacing(true);
        empresaLayout.setCaption("Empresa :");

        HorizontalLayout nomenclaturaLayout = new HorizontalLayout();
        nomenclaturaLayout.setWidth("100%");
        nomenclaturaLayout.setSpacing(true);
        nomenclaturaLayout.setCaption("Nomenclatura :");

        idEmpresa_1Txt = new NumberField();
        idEmpresa_1Txt.setWidth("100%");
        idEmpresa_1Txt.setEnabled(false);

        empresas_1Cbx = new ComboBox();
        empresas_1Cbx.setWidth("100%");
        empresas_1Cbx.setNullSelectionAllowed(false);
        empresas_1Cbx.addValueChangeListener(event -> {
            if (event.getProperty().getValue() != null) {
                idEmpresa_1Txt.setValue(event.getProperty().getValue().toString());
            } else {
                idEmpresa_1Txt.setValue("");
            }
        });

        idNomenclatura_1Txt = new NumberField();
        idNomenclatura_1Txt.setWidth("100%");
        idNomenclatura_1Txt.setEnabled(false);

        nomenclaturas_1Cbx = new ComboBox();
        nomenclaturas_1Cbx.setWidth("100%");
        nomenclaturas_1Cbx.setNullSelectionAllowed(false);
        nomenclaturas_1Cbx.addContainerProperty(CUENTA, Integer.class, null);
        nomenclaturas_1Cbx.addValueChangeListener(event -> {
            if (event.getProperty().getValue() != null) {
                Integer cuenta = (Integer) nomenclaturas_1Cbx.getContainerProperty(event.getProperty().getValue(), CUENTA).getValue();
                idnomenclatura_1 = event.getProperty().getValue().toString();
                idNomenclatura_1Txt.setValue(String.valueOf(cuenta));
            } else {
                idNomenclatura_1Txt.setValue("");
            }
        });

        empresaLayout.addComponents(idEmpresa_1Txt, empresas_1Cbx);
        empresaLayout.setExpandRatio(idEmpresa_1Txt, 0.1f);
        empresaLayout.setExpandRatio(empresas_1Cbx, 0.9f);

        nomenclaturaLayout.addComponents(idNomenclatura_1Txt, nomenclaturas_1Cbx);
        nomenclaturaLayout.setExpandRatio(idNomenclatura_1Txt, 0.22f);
        nomenclaturaLayout.setExpandRatio(nomenclaturas_1Cbx, 0.78f);

        formReceptor.addComponents(empresaLayout, nomenclaturaLayout);

    }


    private void llenarComboEmpresa(){
        queryString = "SELECT * ";
        queryString += "FROM contabilidad_empresa";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    empresasCbx.addItem(rsRecords.getString("IdEmpresa"));
                    empresasCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("NombreCorto"));
                    empresas_1Cbx.addItem(rsRecords.getString("IdEmpresa"));
                    empresas_1Cbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("NombreCorto"));
                } while (rsRecords.next());
            }

            if (idempresa != null) {
                empresasCbx.setValue(idempresa);
            }
            if (idempresa_1 != null) {
                empresas_1Cbx.setValue(idempresa_1);
            }
        } catch (SQLException ex) {
            System.out.println("Error al llenar Campos " + ex);
            ex.printStackTrace();
        }
    }

    private void llenarComboNomenclatura() {
        queryString = "SELECT * ";
        queryString += "FROM contabilidad_nomenclatura";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    String itemId = rsRecords.getString("IdNomenclatura");
                    nomenclaturasCbx.addItem(itemId);
                    nomenclaturasCbx.setItemCaption(itemId, rsRecords.getString("N5"));
                    nomenclaturasCbx.getContainerProperty(itemId, CUENTA).setValue(rsRecords.getInt("NoCuenta"));

                    nomenclaturas_1Cbx.addItem(itemId);
                    nomenclaturas_1Cbx.setItemCaption(itemId, rsRecords.getString("N5"));
                    nomenclaturas_1Cbx.getContainerProperty(itemId, CUENTA).setValue(rsRecords.getInt("NoCuenta"));
                } while (rsRecords.next());
            }

            if (idnomenclatura != null) {
                nomenclaturasCbx.setValue(idnomenclatura);
            }
            if (idnomenclatura_1 != null) {
                nomenclaturas_1Cbx.setValue(idnomenclatura_1);
            }
        } catch (SQLException ex) {
            System.out.println("Error al llenar Campos " + ex);
            ex.printStackTrace();
        }
    }

    public void seleccionarCampos() {
        empresasCbx.select(idempresa);
        nomenclaturasCbx.select(idnomenclatura);
        empresas_1Cbx.select(idempresa_1);
        nomenclaturas_1Cbx.select(idnomenclatura_1);
    }

    public void insertarCuentaContable() {
        try {

            if (esNuevo) {
                queryString = "Insert Into empresa_cuenta_equivalente ";
                queryString += "(IdEmpresa, IdNomenclatura, IdEmpresa_1, IdNomenclatura_1, Inhabilitado)";
                queryString += " Values (";
                queryString += " " + idEmpresaTxt.getValue();
                queryString += "," + idNomenclaturaTxt.getValue();
                queryString += "," + idEmpresa_1Txt.getValue();
                queryString += "," + idNomenclatura_1Txt.getValue();
                queryString += "," + inhabilitadoChk.getValue();
                queryString += ")";
            } else {
                queryString = "Update empresa_cuenta_equivalente Set ";
                queryString += " IdEmpresa =" + idEmpresaTxt.getValue();
                queryString += ",IdNomenclatura ='" + idNomenclaturaTxt.getValue();
                queryString += ",IdEmpresa_1 =" + idEmpresa_1Txt.getValue();
                queryString += ",IdNomenclatura_1  ='" + idNomenclatura_1Txt.getValue();
                queryString += ",Inhabilitado ='" + inhabilitadoChk.getValue();
                queryString += " Where Id = " + id;
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

}
