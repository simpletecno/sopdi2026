/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.extras.custom.ToggleSwitch;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class ProveedorForm_old extends Window {

    VerticalLayout mainLayout;
    HorizontalLayout formsLayout;
    MarginInfo marginInfo;

    public int idProveedor = 0;

    String idProveedorAnterior = "";

    HorizontalLayout proveedorForm0;
    FormLayout proveedorForm1;
    FormLayout proveedorForm2;
    GridLayout proveedorForm3;

    Button saveBtn;
    Button salirBtn;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    ToggleSwitch esInabilitadoCheck;
    TextField idTxt;
    TextField n0Txt;
    TextField grupo0Txt;
    DateField fechaDt;
    TextField n1Txt;
    TextField grupoTxt;
    TextField n2Txt;
    TextField tipoTxt;
    TextField n3Txt;
    TextField numeroTxt;
    TextField n4Txt;
    TextField idProveedorTxt;

    TextField proveedorTxt;
    TextField productoTxt;
    TextField direccionTxt;
    TextField nitTxt;
    ComboBox regimenCbx;
    ComboBox grupoTrabajoCbx;
    ComboBox estatusTrabajoCbx;
    TextField razonTxt;

    TextField anticipoLoteTxt;
    TextField provisionTxt;
    TextField diasAnticipoTxt;
    TextField diasCreditoTxt;
    TextField anticipoUnidadTxt;
    TextField diaProvisionTxt;
    TextField emailTxt;
    ComboBox empresaRelacionadaCbx;
    ComboBox cuentaAnticiposLiquidarCbx;
    ComboBox cuentaAcreedoresCbx;
    CheckBox esProveedorCheck;
    CheckBox esClienteCheck;
    CheckBox esLiquidadorCheck;
    CheckBox esComiteCheck;
    CheckBox esPlanillaCheck;
    CheckBox esRelacionadaCheck;
    CheckBox esBancoCheck;
    CheckBox esAgenteRetenedorISRCheck;
    CheckBox esAgenteRetenedorIVACheck;
    CheckBox esJefeCheck;
    CheckBox esContactoObraCheck;
    CheckBox esVisitaResponsable;
    CheckBox esAutorizadoPagarCheck;
    CheckBox esAbastosCheck;
    ComboBox cargoCbx;
    ComboBox usuarioCbx;

    UI mainUI;
    Label captionLbl;

    public ProveedorForm_old() {
        this.mainUI = UI.getCurrent();

        setWidth("95%");
        setHeight("95%");       // idem para la altura

        setCaption("PROVEEDORES/CLIENTES/OTROS");

        marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(true, true, false, true));
        mainLayout.setSpacing(true);

        captionLbl = new Label("Mantenimiento de Proveedores/Clientes/Otros");
        captionLbl.addStyleName("h3");
        captionLbl.setHeightUndefined();

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(false);
        filterLayout.setMargin(false);
        filterLayout.addComponent(captionLbl);
        filterLayout.addStyleName("rcorners3");

        mainLayout.addComponent(filterLayout);
        mainLayout.setComponentAlignment(filterLayout, Alignment.TOP_CENTER);

        formsLayout = new HorizontalLayout();
        formsLayout.setWidth("100%");
        formsLayout.setMargin(false);
        formsLayout.setSpacing(true);

        proveedorForm0 = new HorizontalLayout();
        proveedorForm1 = new FormLayout();
        proveedorForm2 = new FormLayout();

        esInabilitadoCheck = new ToggleSwitch( "Estado :", "Inhabilidato", "Habilitado");
        esInabilitadoCheck.setValue(false);

        idTxt = new TextField("Id: ");
        idTxt.setWidth("10em");

        n0Txt = new TextField("N0 : ");
        n0Txt.setWidth("2em");
        n0Txt.setStyleName("segmented-input");
        n0Txt.setMaxLength(1);

        grupo0Txt = new TextField("Grupo 0 : ");
        grupo0Txt.setWidth("10em");

        n1Txt = new TextField("N1 : ");
        n1Txt.setWidth("2em");
        n1Txt.setStyleName("segmented-input");
        n1Txt.setMaxLength(1);

        grupoTxt = new TextField("Grupo : ");
        grupoTxt.setWidth("10em");

        n2Txt = new TextField("N2 : ");
        n2Txt.setWidth("2em");
        n2Txt.setStyleName("segmented-input");
        n2Txt.setMaxLength(1);

        tipoTxt = new TextField("Tipo :");
        tipoTxt.setWidth("10em");

        n3Txt = new TextField("N3 : ");
        n3Txt.setWidth("2em");
        n3Txt.setStyleName("segmented-input");
        n3Txt.setMaxLength(1);

        numeroTxt = new TextField("Numero :");
        numeroTxt.setWidth("10em");

        n4Txt = new TextField("N4 : ");
        n4Txt.setWidth("3em");
        n4Txt.setStyleName("segmented-input");
        n4Txt.setMaxLength(2);

        idProveedorTxt = new TextField("Codigo Proveedor : ");
        idProveedorTxt.setStyleName("segmented-input");
        idProveedorTxt.setReadOnly(false);
        idProveedorTxt.setWidth("10em");
        idProveedorTxt.setMaxLength(6);

        proveedorTxt = new TextField("Nombre : ");
        proveedorTxt.setWidth("95%");

        direccionTxt = new TextField("Dirección : ");
        direccionTxt.setWidth("95%");
        productoTxt = new TextField("Producto : ");
        productoTxt.setWidth("95%");

        fechaDt = new DateField("Fecha Ingreso : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("8em");
        fechaDt.setValue(new java.util.Date());

        nitTxt = new TextField("NIT : ");
        nitTxt.setWidth("10em");

        regimenCbx = new ComboBox("REGIMEN : ");
        regimenCbx.setWidth("95%");
        regimenCbx.setInvalidAllowed(false);
        regimenCbx.setNewItemsAllowed(false);
        regimenCbx.setTextInputAllowed(false);
        regimenCbx.setNullSelectionAllowed(false);
        regimenCbx.addItem("NORMAL");
        regimenCbx.addItem("PEQUEÑO CONTRIBUYENTE");
        regimenCbx.addItem("SUJETO A RETENCION ISR");
        regimenCbx.addItem("SUJETO A RETENCION IVA");
        regimenCbx.addItem("INSTITUCION");
        regimenCbx.select("NORMAL");

        grupoTrabajoCbx = new ComboBox("Grupo de Trabajo : ");
        grupoTrabajoCbx.setWidth("20em");
        grupoTrabajoCbx.setInvalidAllowed(false);
        grupoTrabajoCbx.setNewItemsAllowed(false);
        grupoTrabajoCbx.setTextInputAllowed(false);
        grupoTrabajoCbx.setNullSelectionAllowed(false);
        llenarComboGrupoTrabajo();
        
        estatusTrabajoCbx = new ComboBox("Estatus Trabajo : ");
        estatusTrabajoCbx.setWidth("20em");
        estatusTrabajoCbx.setInvalidAllowed(false);
        estatusTrabajoCbx.setNewItemsAllowed(false);
        estatusTrabajoCbx.setTextInputAllowed(false);
        estatusTrabajoCbx.setNullSelectionAllowed(false);
        estatusTrabajoCbx.addItem("PRESENTE");
        estatusTrabajoCbx.addItem("AUSENTE");
        estatusTrabajoCbx.addItem("INACTIVO");
        estatusTrabajoCbx.addItem("DE BAJA");
        estatusTrabajoCbx.select("PRESENTE");

        razonTxt = new TextField("Razón : ");
        razonTxt.setWidth("95%");

        anticipoLoteTxt = new TextField("% Anticipo lote : ");
        anticipoLoteTxt.setWidth("10em");

        provisionTxt = new TextField("% Provisión : ");
        provisionTxt.setWidth("10em");

        diasAnticipoTxt = new TextField("Días anticipo : ");
        diasAnticipoTxt.setWidth("10em");

        diasCreditoTxt = new TextField("Días crédito : ");
        diasCreditoTxt.setWidth("10em");

        anticipoUnidadTxt = new TextField("% Anticipo unidad : ");
        anticipoUnidadTxt.setWidth("10em");

        diaProvisionTxt = new TextField("Dias provisión : ");
        diaProvisionTxt.setWidth("10em");

        emailTxt = new TextField("Email : ");
        emailTxt.setWidth("95%");
        emailTxt.addValidator(new RegexpValidator(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
                "El email no es válido"
        ));


        cargoCbx = new ComboBox("Cargo : ");
        cargoCbx.setWidth("95%");
        cargoCbx.addItem("DIRECTOR");
        cargoCbx.addItem("GERENTE GENERAL");
        cargoCbx.addItem("GERENTE ADMINISTRATIVO");
        cargoCbx.addItem("GERENTE OPERACIONES");
        cargoCbx.addItem("GERENTE FINANCIERO");
        cargoCbx.addItem("SECRETARIA");
        cargoCbx.addItem("RECEPCIONISTA");
        cargoCbx.addItem("ASISTENTE");
        cargoCbx.addItem("CONTADOR");
        cargoCbx.addItem("AUXLIAR CONTABLE");
        cargoCbx.addItem("ASESOR VENTAS");
        cargoCbx.addItem("SUPERVISOR VENTAS");
        cargoCbx.addItem("SUPERVISOR OBRAs");
        cargoCbx.addItem("SUPERVISOR ARQUITECTURA");
        cargoCbx.addItem("CONTROLLER");
        cargoCbx.addItem("DIBUJANTE");
        cargoCbx.addItem("PROGRAMADOR");
        cargoCbx.addItem("MAESTRO OBRAs");
        cargoCbx.addItem("BODEGUERO");
        cargoCbx.addItem("CAPORAL");
        cargoCbx.addItem("PLOMERO");
        cargoCbx.addItem("ELECTRICISTA");
        cargoCbx.addItem("INSTALADOR PISO");
        cargoCbx.addItem("RH1");
        cargoCbx.addItem("RH2");
        cargoCbx.addItem("CONSERJE");
        cargoCbx.addItem("JARDINERO");

        esProveedorCheck = new CheckBox("ES PROVEEDOR");
//        esProveedorCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esProveedorCheck.setValue(false);

        esClienteCheck = new CheckBox("ES CLIENTE");
//        esClienteCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esClienteCheck.setValue(false);

        esLiquidadorCheck = new CheckBox("ES LIQUIDADOR");
//        esLiquidadorCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esLiquidadorCheck.setValue(false);

        esComiteCheck = new CheckBox("ES COMITE");
//        esComiteCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esComiteCheck.setValue(false);

        esPlanillaCheck = new CheckBox("ES PLANILLA");
//        esPlanillaCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esPlanillaCheck.setValue(false);

        esRelacionadaCheck = new CheckBox("ES RELACIONADA");
//        esRelacionadaCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esRelacionadaCheck.setValue(false);

        esBancoCheck = new CheckBox("ES BANCO");
//        esBancoCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esBancoCheck.setValue(false);

        esAgenteRetenedorISRCheck = new CheckBox("ES RETENEDOR ISR");
//        esAgenteRetenedorISRCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esAgenteRetenedorISRCheck.setValue(false);

        esAgenteRetenedorIVACheck = new CheckBox("ES RETENEDOR IVA");
//        esAgenteRetenedorIVACheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esAgenteRetenedorIVACheck.setValue(false);

        esJefeCheck = new CheckBox("ES JEFE");
//        esJefeCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esJefeCheck.setValue(false);

        esContactoObraCheck = new CheckBox("ES CONTACTO EN OBRA");
//        esContactoObraCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        esContactoObraCheck.setValue(false);

        esVisitaResponsable = new CheckBox("ES RESPONSABLES DE VISITAS");
        esVisitaResponsable.setValue(false);

        esAutorizadoPagarCheck = new CheckBox("ES AUTORIZADO PAGAR");
        esAutorizadoPagarCheck.setValue(false);
        esAbastosCheck = new CheckBox("ES ABASTOS");
        esAbastosCheck.setValue(false);

        empresaRelacionadaCbx = new ComboBox("EMPRESA :");
        empresaRelacionadaCbx.setWidth("95%");
        empresaRelacionadaCbx.setInvalidAllowed(false);
        empresaRelacionadaCbx.setNewItemsAllowed(false);
        empresaRelacionadaCbx.setTextInputAllowed(false);
        empresaRelacionadaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        cuentaAnticiposLiquidarCbx = new ComboBox("CTA ANTICIPOS X LIQUIDAR :");
        cuentaAnticiposLiquidarCbx.setWidth("95%");
        cuentaAnticiposLiquidarCbx.setInvalidAllowed(false);
        cuentaAnticiposLiquidarCbx.setNewItemsAllowed(false);
        cuentaAnticiposLiquidarCbx.setTextInputAllowed(false);
        cuentaAnticiposLiquidarCbx.setNullSelectionAllowed(false);
        llenarComboCuentaContable(cuentaAnticiposLiquidarCbx,"ANTICIPOS INTERNOS");

        cuentaAcreedoresCbx = new ComboBox("CTA ACRREDORES :");
        cuentaAcreedoresCbx.setWidth("95%");
        cuentaAcreedoresCbx.setInvalidAllowed(false);
        cuentaAcreedoresCbx.setNewItemsAllowed(false);
        cuentaAcreedoresCbx.setTextInputAllowed(false);
        cuentaAcreedoresCbx.setNullSelectionAllowed(false);
        llenarComboCuentaContable(cuentaAcreedoresCbx,"ACREEDORES");

        usuarioCbx = new ComboBox("Usuario : ");
        usuarioCbx.setWidth("20em");
        usuarioCbx.setInvalidAllowed(false);
        usuarioCbx.setNewItemsAllowed(false);
        usuarioCbx.setTextInputAllowed(false);
        usuarioCbx.setNullSelectionAllowed(false);
        llenarComboUsuario();

        HorizontalLayout idFormLayout = new HorizontalLayout();
        HorizontalLayout idProveedorFormLayout = new HorizontalLayout();
        HorizontalLayout idProveedorLayout = new HorizontalLayout();

        proveedorForm0.addComponents(idFormLayout, idProveedorFormLayout, idProveedorLayout);
        proveedorForm0.setSpacing(true);
        proveedorForm0.setComponentAlignment(idFormLayout, Alignment.TOP_CENTER);
        proveedorForm0.setComponentAlignment(idProveedorFormLayout, Alignment.TOP_CENTER);
        proveedorForm0.setComponentAlignment(idProveedorLayout, Alignment.TOP_CENTER);

        idFormLayout.addStyleName("rcorners3");
        idFormLayout.setSpacing(true);
        idFormLayout.addComponent(esInabilitadoCheck);
        idFormLayout.addComponent(idTxt);

        idProveedorFormLayout.addStyleName("rcorners3");
        idProveedorFormLayout.setSpacing(true);
        idProveedorFormLayout.addComponent(n0Txt);
        idProveedorFormLayout.addComponent(grupo0Txt);
        idProveedorFormLayout.addComponent(n1Txt);
        idProveedorFormLayout.addComponent(grupoTxt);
        idProveedorFormLayout.addComponent(n2Txt);
        idProveedorFormLayout.addComponent(tipoTxt);
        idProveedorFormLayout.addComponent(n3Txt);
        idProveedorFormLayout.addComponent(numeroTxt);
        idProveedorFormLayout.addComponent(n4Txt);

        idProveedorLayout.addStyleName("rcorners3");
        idProveedorLayout.setSpacing(true);
        idProveedorLayout.addComponent(idProveedorTxt);

        proveedorForm1.addStyleName("rcorners3");
        proveedorForm1.addComponent(proveedorTxt);
        proveedorForm1.addComponent(nitTxt);
        proveedorForm1.addComponent(direccionTxt);
        proveedorForm1.addComponent(regimenCbx);
        proveedorForm1.addComponent(emailTxt);
        proveedorForm1.addComponent(fechaDt);
        proveedorForm1.addComponent(empresaRelacionadaCbx);
        proveedorForm1.addComponent(cuentaAnticiposLiquidarCbx);
        proveedorForm1.addComponent(cuentaAcreedoresCbx);
        proveedorForm1.addComponent(usuarioCbx);
        proveedorForm1.addComponent(new Label(""));

        proveedorForm2.addStyleName("rcorners3");
        proveedorForm2.addComponent(grupoTrabajoCbx);
        proveedorForm2.addComponent(estatusTrabajoCbx);
        proveedorForm2.addComponent(razonTxt);
        proveedorForm2.addComponent(cargoCbx);
        proveedorForm2.addComponent(productoTxt);
        proveedorForm2.addComponent(anticipoLoteTxt);
        proveedorForm2.addComponent(provisionTxt);
        proveedorForm2.addComponent(diasAnticipoTxt);
        proveedorForm2.addComponent(diasCreditoTxt);
        proveedorForm2.addComponent(anticipoUnidadTxt);
        proveedorForm2.addComponent(diaProvisionTxt);

        proveedorForm3 = new GridLayout(6, 3);
        proveedorForm3.setSizeFull();
        proveedorForm3.addStyleName("rcorners3");
        proveedorForm3.setSpacing(true);
        proveedorForm3.addComponents( esProveedorCheck,esClienteCheck, esLiquidadorCheck,
                esComiteCheck, esPlanillaCheck,esRelacionadaCheck,
                esAgenteRetenedorISRCheck, esAgenteRetenedorIVACheck,
                esJefeCheck, esContactoObraCheck,
                esBancoCheck, esVisitaResponsable, esAutorizadoPagarCheck, esAbastosCheck);

        mainLayout.addComponent(proveedorForm0);
        mainLayout.setComponentAlignment(proveedorForm0, Alignment.TOP_CENTER);

        formsLayout.addComponent(proveedorForm1);
        formsLayout.setComponentAlignment(proveedorForm1, Alignment.TOP_LEFT);

        formsLayout.addComponent(proveedorForm2);
        formsLayout.setComponentAlignment(proveedorForm2, Alignment.TOP_RIGHT);

        mainLayout.addComponent(formsLayout);

        mainLayout.addComponent(proveedorForm3);
        mainLayout.setComponentAlignment(proveedorForm3, Alignment.MIDDLE_CENTER);


        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                saveProveedor();
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);

        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        setContent(mainLayout);
    }

    public void fillData() {
        String queryString = "";

        queryString = "Select * ";
        queryString += " From  proveedor ";
        queryString += " Where Id = " + idTxt.getValue();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                idProveedorAnterior = rsRecords.getString("IdProveedor");

                n0Txt.setValue(rsRecords.getString("N0"));
                grupo0Txt.setValue(rsRecords.getString("Grupo0"));
                n1Txt.setValue(rsRecords.getString("N1"));
                grupoTxt.setValue(rsRecords.getString("Grupo"));
                n2Txt.setValue(rsRecords.getString("N2"));
                tipoTxt.setValue(rsRecords.getString("Tipo"));
                n3Txt.setValue(rsRecords.getString("N3"));
                numeroTxt.setValue(rsRecords.getString("Numero"));
                n4Txt.setValue(rsRecords.getString("N4"));
                idProveedorTxt.setValue(rsRecords.getString("IDProveedor"));
                proveedorTxt.setValue(rsRecords.getString("Nombre"));
                productoTxt.setValue(rsRecords.getString("Producto"));
                direccionTxt.setValue(rsRecords.getString("Direccion"));
                nitTxt.setValue(rsRecords.getString("NIT"));
                regimenCbx.select(rsRecords.getString("REGIMEN"));
                grupoTrabajoCbx.select(rsRecords.getString("GrupoTrabajo"));
                estatusTrabajoCbx.select(rsRecords.getString("EstatusTrabajo"));
                razonTxt.setValue(rsRecords.getString("Razon"));
                anticipoLoteTxt.setValue(rsRecords.getString("AnticipoLote"));
                provisionTxt.setValue(rsRecords.getString("Provision"));
                diasAnticipoTxt.setValue(rsRecords.getString("DiasAnticipo"));
                diasCreditoTxt.setValue(rsRecords.getString("DiasCredito"));
                anticipoUnidadTxt.setValue(rsRecords.getString("AnticipoUnidad"));
                diaProvisionTxt.setValue(rsRecords.getString("DiaProvision"));
                emailTxt.setValue(rsRecords.getString("Email"));
                empresaRelacionadaCbx.select(rsRecords.getString("IdEmpresa"));
                cuentaAnticiposLiquidarCbx.select(rsRecords.getString("CuentaAnticiposLiquidar"));
                cuentaAcreedoresCbx.select(rsRecords.getString("CuentaAcreedores"));

                if(rsRecords.getString("Inhabilitado").equals("0")){
                    esInabilitadoCheck.setValue(true);
                }
                if(rsRecords.getString("EsProveedor").equals("1")){
                    esProveedorCheck.setValue(true);
                }
                if(rsRecords.getString("EsCliente").equals("1")){
                    esClienteCheck.setValue(true);
                }
                if(rsRecords.getString("EsLiquidador").equals("1")){
                    esLiquidadorCheck.setValue(true);
                }
                if(rsRecords.getString("EsComite").equals("1")){
                    esComiteCheck.setValue(true);
                }
                if(rsRecords.getString("EsPlanilla").equals("1")){
                    esPlanillaCheck.setValue(true);
                }
                if(rsRecords.getString("EsRelacionada").equals("1")){
                    esRelacionadaCheck.setValue(true);
                }
                if(rsRecords.getString("EsBanco").equals("1")){
                    esBancoCheck.setValue(true);
                }
                if(rsRecords.getString("EsAgenteRetenedorISR").equals("1")){
                    esAgenteRetenedorISRCheck.setValue(true);
                }
                if(rsRecords.getString("EsAgenteRetenedorIVA").equals("1")){
                    esAgenteRetenedorIVACheck.setValue(true);
                }
                esJefeCheck.setValue(rsRecords.getString("EsJefe").equals("1"));
                esContactoObraCheck.setValue(rsRecords.getString("EsContactoObra").equals("1"));
                esVisitaResponsable.setValue(rsRecords.getString("EsVisitaResponsable").equals("1"));
                esAutorizadoPagarCheck.setValue(rsRecords.getString("EsAutorizadoPagar").equals("1"));
                esAbastosCheck.setValue(rsRecords.getString("EsAbastos").equals("1"));

                cargoCbx.setValue(rsRecords.getString("Cargo"));
                usuarioCbx.setValue(rsRecords.getString("IdUsuario"));

            }
        } catch (Exception ex) {
            Logger.getLogger(ProveedorForm_old.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros : " + ex.getMessage());
            Notification.show("Error al intentar leer registros..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        if(idProveedor == -1) {
            captionLbl.setValue("NUEVO REGISTRO");
        }
        else {
            captionLbl.setValue("EDITANDO EL REGISTRO DE : " + proveedorTxt.getValue());
        }
    }

    private void saveProveedor() {

        if (proveedorTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el nombre!", Notification.Type.ERROR_MESSAGE);
            proveedorTxt.focus();
            return;
        }
        if (productoTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el producto!", Notification.Type.ERROR_MESSAGE);
            productoTxt.focus();
            return;
        }
        if (direccionTxt.getValue().trim().isEmpty() || direccionTxt.getValue() == null) {
            Notification.show("Error, falta la dirección!", Notification.Type.ERROR_MESSAGE);
            direccionTxt.focus();
            return;
        }
        if (idProveedorTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el código!", Notification.Type.ERROR_MESSAGE);
            idProveedorTxt.focus();
            return;
        }
        if (proveedorTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el nombre!", Notification.Type.ERROR_MESSAGE);
            proveedorTxt.focus();
            return;
        }
        if (String.valueOf(tipoTxt.getValue()).trim().isEmpty()) {
            Notification.show("Error, falta el tipo!", Notification.Type.ERROR_MESSAGE);
            tipoTxt.focus();
            return;
        }
        if (fechaDt.getValue() == null) {
            Notification.show("Error, falta la fecha de inicio de relaciones!", Notification.Type.ERROR_MESSAGE);
            fechaDt.focus();
            return;
        }

        String emailValue = emailTxt.getValue().trim();
        if (!emailValue.isEmpty()) {
            try {
                emailTxt.validate();
            } catch (Exception e) {
                Notification.show("Error, el email no es válido!", Notification.Type.ERROR_MESSAGE);
                emailTxt.focus();
                return;
            }
        }

        /**
         * if(contactoTxt.getValue().trim().isEmpty()) {
         * Notification.show("Error, falta el contacto!",
         * Notification.Type.ERROR_MESSAGE); contactoTxt.focus(); return; }
         * if(direccionTxt.getValue().trim().isEmpty()) {
         * Notification.show("Error, falta la dirección!",
         * Notification.Type.ERROR_MESSAGE); direccionTxt.focus(); return; }
         * if(telefono1Txt.getValue().trim().isEmpty()) {
         * Notification.show("Error, falta el teléfono!",
         * Notification.Type.ERROR_MESSAGE); telefono1Txt.focus(); return; }
         *
         */
        String queryString = "";
        String queryString2 = "";

        if (idProveedor <= 0) {
            queryString = "Insert Into proveedor (N0, Grupo0, N1, Grupo, N2,Tipo, N3, Numero, N4, IDProveedor, ";
            queryString += " Nombre, Producto, Direccion, NIT, Regimen, GrupoTrabajo, EstatusTrabajo, Razon, ";
            queryString += " AnticipoLote, Provision, DiasAnticipo, ";
            queryString += " DiasCredito, AnticipoUnidad, DiaProvision, Email, ";
            queryString += " IdEmpresa, CuentaAnticiposLiquidar, CuentaAcreedores, ";
            queryString +=  " Inhabilitado, EsProveedor, EsCliente, ";
            queryString += " EsLiquidador, EsComite, EsPlanilla, EsRelacionada, EsBanco, ";
            queryString += " EsAgenteRetenedorISR,  EsAgenteRetenedorIVA, EsJefe, EsContactoObra, ";
            queryString += " EsVisitaResponsable, EsAutorizadoPagar, EsAbastos, Cargo, IdUsuario)";
            queryString += " Values (";
            queryString += n0Txt.getValue();
            queryString += ",'" + grupo0Txt.getValue() + "'";
            queryString += "," + n1Txt.getValue();
            queryString += ",'" + grupoTxt.getValue() + "'";
            queryString += "," + n2Txt.getValue();
            queryString += ",'" + tipoTxt.getValue() + "'";
            queryString += "," + n3Txt.getValue();
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + n4Txt.getValue();
            queryString += "," + idProveedorTxt.getValue();
            queryString += ",'" + proveedorTxt.getValue() + "'";
            queryString += ",'" + productoTxt.getValue() + "'";
            queryString += ",'" + direccionTxt.getValue() + "'";
            queryString += ",'" + nitTxt.getValue() + "'";
            queryString += ",'" + regimenCbx.getValue() + "'";
            queryString += ",'" + grupoTrabajoCbx.getValue() + "'";
            queryString += ",'" + estatusTrabajoCbx.getValue() + "'";
            queryString += ",'" + razonTxt.getValue() + "'";
            queryString += ", " + anticipoLoteTxt.getValue();
            queryString += ", " + provisionTxt.getValue();
            queryString += "," + diasAnticipoTxt.getValue();
            queryString += "," + diasCreditoTxt.getValue();
            queryString += "," + anticipoUnidadTxt.getValue();
            queryString += ", " + diaProvisionTxt.getValue();
            queryString += ",'" + emailTxt.getValue() + "'";
            queryString += ", " + String.valueOf(empresaRelacionadaCbx.getValue());
            queryString += ",'" + String.valueOf(cuentaAnticiposLiquidarCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(cuentaAcreedoresCbx.getValue()) + "'";

            queryString += ", " + (!esInabilitadoCheck.getValue() ? "1" : "0");
            queryString += ", " + (esProveedorCheck.getValue() ? "1" : "0");
            queryString += ", " + (esClienteCheck.getValue() ? "1" : "0");
            queryString += ", " + (esLiquidadorCheck.getValue() ? "1" : "0");
            queryString += ", " + (esComiteCheck.getValue() ? "1" : "0");
            queryString += ", " + (esPlanillaCheck.getValue() ? "1" : "0");
            queryString += ", " + (esRelacionadaCheck.getValue() ? "1" : "0");
            queryString += ", " + (esBancoCheck.getValue() ? "1" : "0");
            queryString += ", " + (esAgenteRetenedorISRCheck.getValue() ? "1" : "0");
            queryString += ", " + (esAgenteRetenedorIVACheck.getValue() ? "1" : "0");
            queryString += ", " + (esJefeCheck.getValue() ? "1" : "0");
            queryString += ", " + (esContactoObraCheck.getValue() ? "1" : "0");
            queryString += ", " + (esVisitaResponsable.getValue() ? "1" : "0");
            queryString += ", " + (esAutorizadoPagarCheck.getValue() ? "1" : "0");
            queryString += ", " + (esAbastosCheck.getValue() ? "1" : "0");

            queryString += ", '" + cargoCbx.getValue() + "'";
            queryString += "," + usuarioCbx.getValue();
            queryString += ")";
        } else {
            queryString = "Update proveedor Set ";
            queryString += " N0 = " + n0Txt.getValue();
            queryString += ",Grupo0 = '" + grupo0Txt.getValue() + "'";
            queryString += ",N1 = " + n1Txt.getValue();
            queryString += ",Grupo = '" + grupoTxt.getValue() + "'";
            queryString += ",N2 = " + n2Txt.getValue();
            queryString += ",Tipo = '" + tipoTxt.getValue() + "'";
            queryString += ",N3 = " + n3Txt.getValue();
            queryString += ",Numero = '" + numeroTxt.getValue() + "'";
            queryString += ",N4 = " + n4Txt.getValue();
            queryString += ",IDProveedor = " + idProveedorTxt.getValue();
            queryString += ",Nombre = '" + proveedorTxt.getValue() + "'";
            queryString += ",Producto = '" + productoTxt.getValue() + "'";
            queryString += ",Direccion = '" + direccionTxt.getValue() + "'";
            queryString += ",NIT = '" + nitTxt.getValue() + "'";
            queryString += ",Regimen = '" + regimenCbx.getValue() + "'";
            queryString += ",GrupoTrabajo = '" + grupoTrabajoCbx.getValue() + "'";
            queryString += ",EstatusTrabajo = '" + estatusTrabajoCbx.getValue() + "'";
            queryString += ",Razon = '" + razonTxt.getValue() + "'";
            queryString += ",AnticipoLote = " + anticipoLoteTxt.getValue();
            queryString += ",Provision = " + provisionTxt.getValue();
            queryString += ",DiasAnticipo = " + diasAnticipoTxt.getValue();
            queryString += ",DiasCredito = " + diasCreditoTxt.getValue();
            queryString += ",AnticipoUnidad = " + anticipoUnidadTxt.getValue();
            queryString += ",DiaProvision = " + diaProvisionTxt.getValue();
            queryString += ",Email = '" + emailTxt.getValue() + "'";
            queryString += ",IdEmpresa = " + String.valueOf(empresaRelacionadaCbx.getValue());
            queryString += ",CuentaAnticiposLiquidar = '" + String.valueOf(cuentaAnticiposLiquidarCbx.getValue()) + "'";
            queryString += ",CuentaAcreedores = '" + String.valueOf(cuentaAcreedoresCbx.getValue()) + "'";

            queryString += ",Inhabilitado = " + (!esInabilitadoCheck.getValue() ? "1" : "0");
            queryString += ",EsProveedor = " + (esProveedorCheck.getValue() ? "1" : "0");
            queryString += ",EsCliente = " + (esClienteCheck.getValue() ? "1" : "0");
            queryString += ",EsLiquidador = " + (esLiquidadorCheck.getValue() ? "1" : "0");
            queryString += ",EsComite = " + (esComiteCheck.getValue() ? "1" : "0");
            queryString += ",EsPlanilla = " + (esPlanillaCheck.getValue() ? "1" : "0");
            queryString += ",EsRelacionada = " + (esRelacionadaCheck.getValue() ? "1" : "0");
            queryString += ",EsBanco = " + (esBancoCheck.getValue() ? "1" : "0");
            queryString += ",EsAgenteRetenedorISR = " + (esAgenteRetenedorISRCheck.getValue() ? "1" : "0");
            queryString += ",EsAgenteRetenedorIVA = " + (esAgenteRetenedorIVACheck.getValue() ? "1" : "0");
            queryString += ",EsJefe = " + (esJefeCheck.getValue() ? "1" : "0");
            queryString += ",EsContactoObra = " + (esContactoObraCheck.getValue() ? "1" : "0");
            queryString += ",EsVisitaResponsable = " + (esVisitaResponsable.getValue() ? "1" : "0");
            queryString += ",EsAutorizadoPagar = " + (esAutorizadoPagarCheck.getValue() ? "1" : "0");
            queryString += ",EsAbastos = " + (esAbastosCheck.getValue() ? "1" : "0");

            queryString += ", Cargo = '" + cargoCbx.getValue() + "'";
            queryString += ", IdUsuario = " + usuarioCbx.getValue() ;
            queryString += " Where Id = " + idTxt.getValue();

        }

//System.out.println("proveedor queryString = " + queryString);

        Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
        close();

        Object selectedItem = ((ProveedorView) (mainUI.getNavigator().getCurrentView())).proveedorGrid.getSelectedRow();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((ProveedorView) (mainUI.getNavigator().getCurrentView())).fillProveedorTable();

//            if (idProveedor > 0) {  no funciona  da  error...porque lo ejecuta cuando el grid esta vacio...
//                ((ProveedorView) (mainUI.getNavigator().getCurrentView())).proveedorGrid.select(selectedItem);
//            }
        }
        catch(Exception exc99) {
            Notification.show("Error al actualizar registro : " + exc99.getMessage(), Notification.Type.ERROR_MESSAGE);
            exc99.printStackTrace();
        }
    }

    public void llenarComboEmpresa() {
        String queryString = "";
        queryString += " SELECT * from contabilidad_empresa";
//        queryString += " Where IdEmpresa <> " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        empresaRelacionadaCbx.addItem("0");
        empresaRelacionadaCbx.setItemCaption("0", "<<no aplica>>");
        empresaRelacionadaCbx.select("0");

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                empresaRelacionadaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaRelacionadaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa") + " (" + rsRecords.getString("IdEmpresa") + ")");
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCuentaContable(ComboBox comboBox, String N4) {

        String queryString = "";
        queryString += " SELECT * from contabilidad_nomenclatura";
        queryString += " where N4 = '" + N4 + "'";
        queryString += " and Estatus = 'HABILITADA'";
        queryString += " Order By N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            comboBox.addItem("0");
            comboBox.setItemCaption("0", "<<no aplica>>");

            while (rsRecords.next()) { //  encontrado
                comboBox.addItem(rsRecords.getString("NoCuenta"));
                comboBox.setItemCaption(rsRecords.getString("NoCuenta"),  rsRecords.getString("N5") + " (" +  rsRecords.getString("NoCuenta") + ")");
            }
            if(comboBox.size() > 0) {
                comboBox.select(comboBox.getItemIds().iterator().next());
            }
        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboGrupoTrabajo() {

        String queryString = "";
        queryString += " SELECT * FROM grupo_trabajo";
        queryString += " WHERE Estatus = 'ACTIVO'";
        queryString += " ORDER BY Nombre";

        grupoTrabajoCbx.addItem("");

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                grupoTrabajoCbx.addItem(rsRecords.getString("Nombre"));
            }
            if(grupoTrabajoCbx.size() > 0) {
                grupoTrabajoCbx.select(grupoTrabajoCbx.getItemIds().iterator().next());
            }
        } catch (Exception ex1) {
            System.out.println("Error al combo grupos de trabajo: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboUsuario() {

        String queryString = "";
        queryString += " SELECT * FROM usuario";
        queryString += " WHERE Estatus = 'ACTIVO'";
//        queryString += " AND PERFIL NOT IN ('Super Usuario', 'Administrador', 'Financiero', 'Contador', 'Auditoria')";
        queryString += " ORDER BY Nombre";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            usuarioCbx.addItem("0");
            usuarioCbx.setItemCaption("0",  "SIN USUARIO");

            while (rsRecords.next()) { //  encontrado
                usuarioCbx.addItem(rsRecords.getString("IdUsuario"));
                usuarioCbx.setItemCaption(rsRecords.getString("IdUsuario"),  rsRecords.getString("Nombre"));
            }
            usuarioCbx.select("0");
        } catch (Exception ex1) {
            System.out.println("Error al combo usuarios: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        usuarioCbx.select("0");
    }
}
