/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class ProveedorEmpresaForm extends Window {

    VerticalLayout mainLayout;
    MarginInfo marginInfo;

    Button saveBtn;
    Button salirBtn;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    TextField codigoTxt = new TextField("Código");
    TextField nitTxt = new TextField("NIT");
    ComboBox tipoPersonaCbx = new ComboBox("Tipo persona");
    ComboBox generoCbx = new ComboBox("Género");
    TextField nombreTxt = new TextField("Nombre");
    TextField primerNombreTxt = new TextField("Primer Nombre");
    TextField segundoNombreTxt = new TextField("Segundo Nombre");
    TextField primerApellidoTxt = new TextField("Primer Apellido");
    TextField segundoApellidoTxt = new TextField("Segundo Apellido");
    TextField apellidoDeCasadaTxt = new TextField("Apellido de Casada");
    TextField nacionalidadTxt = new TextField("Nacionalidad");
    TextField dpiTxt = new TextField("DPI");
    ComboBox regimenCbx = new ComboBox("Regimen fiscal");

    TextField direccionTxt = new TextField("Direccion");
    TextField telefonoTxt = new TextField("Telefono");
    TextField telefonoEmergenciaTxt = new TextField("Telefono Emergencia");
    TextField emailTxt = new TextField("Email");

    CheckBox esProveedorCheck = new CheckBox("Es Proveedor");
    CheckBox esClienteCheck = new CheckBox("Es Cliente");
    CheckBox esInstitucionFiscalCheck = new CheckBox("Es Institucion Fiscal");
    CheckBox esInstitucionSeguroSocialCheck = new CheckBox("Es Institucion Seguro Social");
    CheckBox esAgenteRetenedorISRCheck = new CheckBox("Es Agente Retenedor ISR");
    CheckBox esAgenteRetenedorIVACheck = new CheckBox("Es Agente Retenedor IVA");
    CheckBox esSujetoARetencionDefinitivaISRCheck = new CheckBox("Es Sujeto ARetencion ISR");
    CheckBox esBancoCheck = new CheckBox("Es Banco");

    CheckBox esLiquidadorCheck = new CheckBox("Es Liquidador");
    CheckBox esComiteCheck = new CheckBox("Es Comite");
    CheckBox esPlanillaCheck = new CheckBox("Es Planilla");
    CheckBox esJefeCheck = new CheckBox("Es Jefe");
    CheckBox esContactoObraCheck = new CheckBox("Es Contacto Obra");
    CheckBox esVisitaResponsableCheck = new CheckBox("Es Visita Responsable");
    CheckBox esAutorizadoPagarCheck = new CheckBox("Es Autorizado Pagar");
    CheckBox esAbastosCheck = new CheckBox("Es Abastos");
    ComboBox cargoCbx = new ComboBox("Cargo");
    ComboBox usuarioCbx = new ComboBox("Usuario");

    ComboBox inabilitadoCbk = new ComboBox("Inhabilidato");

    UI mainUI;
    Label captionLbl;

    public String idProveedor = "0";

    public ProveedorEmpresaForm(String idProveedor) {
        this.idProveedor = idProveedor;
        this.mainUI = UI.getCurrent();

        setWidth("70%");
        setHeightUndefined();

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(true, true, false, true));
        mainLayout.setSpacing(true);
        mainLayout.addStyleName(ValoTheme.LAYOUT_CARD);

        setContent(mainLayout);

        setCaption("<H2>MIS PROVEEDORES!!</H2>");
        setCaptionAsHtml(true);

        marginInfo = new MarginInfo(true, true, true, true);

        captionLbl = new Label("Mantenimiento de Proveedores/Clientes/Otros");
        captionLbl.addStyleName("h3");
        captionLbl.setHeightUndefined();

//        mainLayout.addComponent(captionLbl);
//        mainLayout.setComponentAlignment(captionLbl, Alignment.TOP_CENTER);

        crearFormulario();

        fillData();
    }

    private void crearFormulario() {

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("100%");
        formLayout.setMargin(false);
        formLayout.setSpacing(true);

        codigoTxt.setWidth("10em");
        codigoTxt.setReadOnly(true);

        nitTxt = new TextField("NIT : ");
        nitTxt.setWidth("10em");
        nitTxt.setRequired(true);

        tipoPersonaCbx.addItem("INDIVIDUAL");
        tipoPersonaCbx.addItem("JURIDICA");
        tipoPersonaCbx.addItem("EXTRANJERO");
        tipoPersonaCbx.select("JURIDICA");
        tipoPersonaCbx.setTextInputAllowed(false);
        tipoPersonaCbx.setInvalidAllowed(false);
        tipoPersonaCbx.setNewItemsAllowed(false);
        tipoPersonaCbx.setNullSelectionAllowed(false);

        generoCbx.addItem("NO APLICA");
        generoCbx.addItem("MASCULINO");
        generoCbx.addItem("FEMININO");
        generoCbx.select("NO APLICA");
        generoCbx.setTextInputAllowed(false);
        generoCbx.setInvalidAllowed(false);
        generoCbx.setNewItemsAllowed(false);
        generoCbx.setNullSelectionAllowed(false);

        nombreTxt.setWidth("95%");
        primerNombreTxt.setWidth("95%");
        segundoNombreTxt.setWidth("95%");
        primerApellidoTxt.setWidth("95%");
        segundoApellidoTxt.setWidth("95%");
        apellidoDeCasadaTxt.setWidth("95%");

        nacionalidadTxt.setWidth("95%");
        dpiTxt.setWidth("95%");
        regimenCbx.setWidth("95%");
        regimenCbx.setInvalidAllowed(false);
        regimenCbx.setNewItemsAllowed(false);
        regimenCbx.setTextInputAllowed(false);
        regimenCbx.setNullSelectionAllowed(false);
        regimenCbx.addItem("Pequeño Contribuyente");
        regimenCbx.addItem("Opcional Simplificado");
        regimenCbx.addItem("Sobre las Utilidades de Actividades Lucrativas");
        regimenCbx.addItem("Exento");
        regimenCbx.select("Opcional Simplificado");

        direccionTxt.setWidth("95%");
        telefonoTxt.setWidth("95%");
        telefonoEmergenciaTxt.setWidth("95%");
        emailTxt.setWidth("95%");
        emailTxt.addValidator(new RegexpValidator(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
                "El email no es válido"
        ));

        cargoCbx.setWidth("95%");
        cargoCbx.addItem("NO APLICA");
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
        cargoCbx.select("NO APLICA");
        cargoCbx.setInvalidAllowed(false);
        cargoCbx.setNewItemsAllowed(false);
        cargoCbx.setTextInputAllowed(false);
        cargoCbx.setNullSelectionAllowed(false);

        usuarioCbx = new ComboBox("Usuario : ");
        usuarioCbx.setWidth("20em");
        usuarioCbx.setInvalidAllowed(false);
        usuarioCbx.setNewItemsAllowed(false);
        usuarioCbx.setTextInputAllowed(false);
        usuarioCbx.setNullSelectionAllowed(false);
        llenarComboUsuario();

        esProveedorCheck.setValue(false);
        esClienteCheck.setValue(false);
        esSujetoARetencionDefinitivaISRCheck.setValue(false);
        esInstitucionFiscalCheck.setValue(false);
        esInstitucionSeguroSocialCheck.setValue(false);
        esAgenteRetenedorISRCheck.setValue(false);
        esAgenteRetenedorIVACheck.setValue(false);
        esBancoCheck.setValue(false);

        inabilitadoCbk.addItem("NO");
        inabilitadoCbk.addItem("SI");
        inabilitadoCbk.select("NO");
        inabilitadoCbk.setInvalidAllowed(false);
        inabilitadoCbk.setNewItemsAllowed(false);
        inabilitadoCbk.setTextInputAllowed(false);
        inabilitadoCbk.setNullSelectionAllowed(false);

        formLayout.addComponents(codigoTxt, nitTxt, tipoPersonaCbx, generoCbx);
        formLayout.addComponents(nombreTxt, primerNombreTxt, segundoNombreTxt, primerApellidoTxt, segundoApellidoTxt, apellidoDeCasadaTxt);
        formLayout.addComponents(nacionalidadTxt, dpiTxt, regimenCbx);
        formLayout.addComponents(direccionTxt, telefonoTxt, telefonoEmergenciaTxt, emailTxt);

        GridLayout layoutEs = new GridLayout(4, 5);
        layoutEs.setSpacing(true);
        layoutEs.setMargin(false);
        layoutEs.setWidth("100%");
        layoutEs.setHeight("100%");
        layoutEs.addStyleName("rcorners3");

        layoutEs.addComponents(esProveedorCheck, esClienteCheck, esSujetoARetencionDefinitivaISRCheck);
        layoutEs.addComponents(esInstitucionFiscalCheck, esInstitucionSeguroSocialCheck);
        layoutEs.addComponents(esAgenteRetenedorISRCheck, esAgenteRetenedorIVACheck);
        layoutEs.addComponents(esBancoCheck, esLiquidadorCheck, esComiteCheck, esPlanillaCheck);
        layoutEs.addComponents(esJefeCheck, esContactoObraCheck, esVisitaResponsableCheck, esAutorizadoPagarCheck);
        layoutEs.addComponents(esAbastosCheck, cargoCbx, usuarioCbx, inabilitadoCbk);

        mainLayout.addComponent(formLayout);
        mainLayout.setComponentAlignment(formLayout, Alignment.TOP_CENTER);
        mainLayout.addComponent(layoutEs);
        mainLayout.setComponentAlignment(layoutEs, Alignment.TOP_CENTER);

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
    }

    public void fillData() {

        codigoTxt.focus();

        if(Objects.equals(idProveedor, "0")) {
            captionLbl.setValue("NUEVO REGISTRO");
        }
        else {
            String queryString = "";

            queryString = "SELECT * ";
            queryString += " FROM proveedor_empresa ";
            queryString += " WHERE Codigo = '" + idProveedor + "'";

            try {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado

                    codigoTxt.setReadOnly(false);
                    codigoTxt.setValue(rsRecords.getString("Codigo"));
                    codigoTxt.setReadOnly(true);
//                    codigoAnteriorTxt.setValue(rsRecords.getString("CodigoAnterior"));
                    nitTxt.setValue(rsRecords.getString("NIT"));
                    tipoPersonaCbx.setValue(rsRecords.getString("TipoPersona"));
                    regimenCbx.select(rsRecords.getString("REGIMEN"));
                    generoCbx.setValue(rsRecords.getString("Genero"));
                    nombreTxt.setValue(rsRecords.getString("Nombre"));
                    primerNombreTxt.setValue(rsRecords.getString("PrimerNombre"));
                    segundoNombreTxt.setValue(rsRecords.getString("SegundoNombre"));
                    primerApellidoTxt.setValue(rsRecords.getString("PrimerApellido"));
                    segundoApellidoTxt.setValue(rsRecords.getString("SegundoApellido"));
                    apellidoDeCasadaTxt.setValue(rsRecords.getString("ApellidoCasada"));
                    nacionalidadTxt.setValue(rsRecords.getString("Nacionalidad"));
                    dpiTxt.setValue(rsRecords.getString("dpi"));
                    direccionTxt.setValue(rsRecords.getString("Direccion"));
                    telefonoTxt.setValue(rsRecords.getString("Telefono"));
                    telefonoEmergenciaTxt.setValue(rsRecords.getString("TelefonoEmergencia"));
                    emailTxt.setValue(rsRecords.getString("Email"));
                    cargoCbx.setValue(rsRecords.getString("Cargo"));
                    usuarioCbx.setValue(rsRecords.getString("IdUsuario"));

                    esProveedorCheck.setValue(rsRecords.getString("EsProveedor").equals("1"));
                    esClienteCheck.setValue(rsRecords.getString("EsCliente").equals("1"));
                    esBancoCheck.setValue(rsRecords.getString("EsBanco").equals("1"));
                    esAgenteRetenedorISRCheck.setValue(rsRecords.getString("EsAgenteRetenedorISR").equals("1"));
                    esAgenteRetenedorIVACheck.setValue(rsRecords.getString("EsAgenteRetenedorIVA").equals("1"));
                    esInstitucionFiscalCheck.setValue(rsRecords.getString("EsInstitucionFiscal").equals("1"));
                    esInstitucionSeguroSocialCheck.setValue(rsRecords.getString("EsInstitucionSeguroSocial").equals("1"));

                    esLiquidadorCheck.setValue(rsRecords.getString("EsLiquidador").equals("1"));
                    esComiteCheck.setValue(rsRecords.getString("EsComite").equals("1"));
                    esJefeCheck.setValue(rsRecords.getString("EsJefe").equals("1"));
                    esPlanillaCheck.setValue(rsRecords.getString("EsPlanilla").equals("1"));
                    esContactoObraCheck.setValue(rsRecords.getString("EsContactoObra").equals("1"));
                    esVisitaResponsableCheck.setValue(rsRecords.getString("EsVisitaResponsable").equals("1"));
                    esAutorizadoPagarCheck.setValue(rsRecords.getString("EsAutorizadoPagar").equals("1"));
                    esAbastosCheck.setValue(rsRecords.getString("EsAbastos").equals("1"));

                    inabilitadoCbk.setValue(rsRecords.getString("Inhabilitado").equals("0") ? "NO" : "SI");

                    captionLbl.setValue("EDITANDO EL REGISTRO DE : " + nombreTxt.getValue());
                }
            } catch (Exception ex) {
                Logger.getLogger(ProveedorEmpresaForm.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error al intentar leer registros : " + ex.getMessage());
                Notification.show("Error al intentar leer registros..!", Notification.Type.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void saveProveedor() {

        if (nombreTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el nombre!", Notification.Type.ERROR_MESSAGE);
            nombreTxt.focus();
            return;
        }
        if (nitTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el NIT!", Notification.Type.ERROR_MESSAGE);
            nitTxt.focus();
            return;
        }
        if (direccionTxt.getValue().trim().isEmpty() || direccionTxt.getValue() == null) {
            Notification.show("Error, falta la dirección!", Notification.Type.ERROR_MESSAGE);
            direccionTxt.focus();
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

        if (Objects.equals(idProveedor, "0")) {
            queryString = "INSERT INTO proveedor (Codigo, Nit, TipoPersona, Regimen, ";
            queryString += " Genero, Nombre, PrimerNombre, SegundoNombre, PrimerApellido, SegundoApellido, ApellidoDeCasada,  ";
            queryString += " Nacionalidad, Dpi, Direccion, Telefono, TelefonoEmergencia, Email, ";
            queryString +=  "EsProveedor, EsCliente, EsBanco, EsAgenteRetenedorISR, EsAgenteRetenedorIVA, ";
            queryString += " EsInstitucionFiscal, EsInstitucionSeguroSocial, EsAbEsSujetoRetencionDefinitivaISR, ";
            queryString += " EsLiquidador, EsComite, EsJefe, EsPlanilla, EsContactoObra, EsVisitaResponsable, ";
            queryString += " EsAutorizadoPagar, EsAbastos, Cargo, Usuario, Inhabilitado)";
            queryString += " VALUES (";
            queryString += "'" + codigoTxt.getValue() + "'";
            queryString += ",'" + nitTxt.getValue() + "'";
            queryString += ",'" + tipoPersonaCbx.getValue() + "'";
            queryString += ",'" + regimenCbx.getValue() + "'";
            queryString += ",'" + generoCbx.getValue() + "'";
            queryString += ",'" + nombreTxt.getValue() + "'";
            queryString += ",'" + primerNombreTxt.getValue() + "'";
            queryString += ",'" + segundoNombreTxt.getValue() + "'";
            queryString += ",'" + primerApellidoTxt.getValue() + "'";
            queryString += ",'" + segundoApellidoTxt.getValue() + "'";
            queryString += ",'" + apellidoDeCasadaTxt.getValue() + "'";
            queryString += ",'" + nacionalidadTxt.getValue() + "'";
            queryString += ",'" + dpiTxt.getValue() + "'";
            queryString += ",'" + direccionTxt.getValue() + "'";
            queryString += ",'" + telefonoTxt.getValue() + "'";
            queryString += ",'" + telefonoEmergenciaTxt.getValue() + "'";
            queryString += ",'" + emailTxt.getValue() + "'";
            queryString += ", " + (esProveedorCheck.getValue() ? "1" : "0");
            queryString += ", " + (esClienteCheck.getValue() ? "1" : "0");
            queryString += ", " + (esBancoCheck.getValue() ? "1" : "0");
            queryString += ", " + (esAgenteRetenedorISRCheck.getValue() ? "1" : "0");
            queryString += ", " + (esAgenteRetenedorIVACheck.getValue() ? "1" : "0");
            queryString += ", " + (esInstitucionFiscalCheck.getValue() ? "1" : "0");
            queryString += ", " + (esInstitucionSeguroSocialCheck.getValue() ? "1" : "0");
            queryString += ", " + (esSujetoARetencionDefinitivaISRCheck.getValue() ? "1" : "0");
            queryString += ", " + (esLiquidadorCheck.getValue() ? "1" : "0");
            queryString += ", " + (esComiteCheck.getValue() ? "1" : "0");
            queryString += ", " + (esJefeCheck.getValue() ? "1" : "0");
            queryString += ", " + (esPlanillaCheck.getValue() ? "1" : "0");
            queryString += ", " + (esContactoObraCheck.getValue() ? "1" : "0");
            queryString += ", " + (esVisitaResponsableCheck.getValue() ? "1" : "0");
            queryString += ", " + (esAbastosCheck.getValue() ? "1" : "0");
            queryString += ",'" + cargoCbx.getValue() + "'";
            queryString += ", " + usuarioCbx.getValue();
            queryString += ", " + (Objects.equals(inabilitadoCbk.getValue(), "NO") ? "0" : "1");
            queryString += ")";
        } else {
            queryString = "UPDATE proveedor SET ";
            queryString += " Nit = '" + nitTxt.getValue() + "'";
            queryString += ",TipoPersona = '" + tipoPersonaCbx.getValue() + "'";
            queryString += ",Regimen = '" + regimenCbx.getValue() + "'";
            queryString += ",Genero = '" + generoCbx.getValue() + "'";
            queryString += ",Nombre = '" + nombreTxt.getValue() + "'";
            queryString += ",PrimerNombre = '" + primerNombreTxt.getValue() + "'";
            queryString += ",SegundoNombre = '" + segundoNombreTxt.getValue() + "'";
            queryString += ",PrimerApellido ='" + primerApellidoTxt.getValue() + "'";
            queryString += ",SegundoApellido = '" + segundoApellidoTxt.getValue() + "'";
            queryString += ",ApellidoDeCasada = '" + apellidoDeCasadaTxt.getValue() + "'";
            queryString += ",Nacionalidad = '" + nacionalidadTxt.getValue() + "'";
            queryString += ",Dpi = '" + dpiTxt.getValue() + "'";
            queryString += ",Direccion = '" + direccionTxt.getValue() + "'";
            queryString += ",Telefono = '" + telefonoTxt.getValue() + "'";
            queryString += ",TelefonoEmergencia = '" + telefonoEmergenciaTxt.getValue() + "'";
            queryString += ",Email = '" + emailTxt.getValue() + "'";
            queryString += ",EsProveedor = " + (esProveedorCheck.getValue() ? "1" : "0");
            queryString += ",EsCliente =  " + (esClienteCheck.getValue() ? "1" : "0");
            queryString += ",EsBanco =  " + (esBancoCheck.getValue() ? "1" : "0");
            queryString += ",EsAgenteRetenedorIsr = " + (esAgenteRetenedorISRCheck.getValue() ? "1" : "0");
            queryString += ",EsAgenteRetenedorIva = " + (esAgenteRetenedorIVACheck.getValue() ? "1" : "0");
            queryString += ",EsInstitucionFiscal = " + (esInstitucionFiscalCheck.getValue() ? "1" : "0");
            queryString += ",EsInstitucionSeguroSocial = " + (esInstitucionSeguroSocialCheck.getValue() ? "1" : "0");
            queryString += ",EsLiquidador = " + (esLiquidadorCheck.getValue() ? "1" : "0");
            queryString += ",EsComite = " + (esComiteCheck.getValue() ? "1" : "0");
            queryString += ",EsJefe =  " + (esJefeCheck.getValue() ? "1" : "0");
            queryString += ",EsPlanilla =  " + (esPlanillaCheck.getValue() ? "1" : "0");
            queryString += ",EsContactoObra =  " + (esContactoObraCheck.getValue() ? "1" : "0");
            queryString += ",EsVisitaResponsable = " + (esVisitaResponsableCheck.getValue() ? "1" : "0");
            queryString += ",EsAbastos = " + (esAbastosCheck.getValue() ? "1" : "0");
            queryString += ",Cargo = '" + cargoCbx.getValue() + "'";
            queryString += ",IdUsuario = " + usuarioCbx.getValue();
            queryString += ",InHabilitado " + (Objects.equals(inabilitadoCbk.getValue(), "NO") ? "0" : "1");
            queryString += " WHERE Id = " + idProveedor;

        }

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            ((ProveedorView) (mainUI.getNavigator().getCurrentView())).fillProveedorTable();

        }
        catch(Exception exc99) {
            Notification.show("Error al actualizar registro : " + exc99.getMessage(), Notification.Type.ERROR_MESSAGE);
            exc99.printStackTrace();
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
