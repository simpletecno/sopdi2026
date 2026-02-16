/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.operativo.ProjectTaskView;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class CostCenterForm extends Window {
    
    VerticalLayout mainLayout;
    MarginInfo  marginInfo;
    
    FormLayout centroCostoForm;
    
    Button saveBtn;
    Button salirBtn;
    
    Statement stQuery = null;
    ResultSet rsRecords = null;
        
    TextField idCentroCostoTxt;
    ComboBox grupoCbx;
    TextField codigoTxt;
    TextField loteTxt;
    ComboBox estiloCbx;
    ComboBox maestroObrasCbx;
    TwinColSelect maestroSelect;
    ComboBox supervisorCbx;
    TwinColSelect supervisorSelect;
    ComboBox cuentaContableCbx;
    ComboBox cuentaContableProvisionCbx;
    ComboBox cuentaContableGarantiasCbx;
    CheckBox inhabilidatoChb;

    UI mainUI;
    
    public CostCenterForm() {
        this.mainUI = UI.getCurrent();
        
        setWidth("60%");
       
        setCaption(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Ficha de centro de costo");
        Page.getCurrent().setTitle(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Ficha de centro de costo");
        
        marginInfo = new MarginInfo(true,true,true,true);
                
        mainLayout = new VerticalLayout();                
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        
        centroCostoForm  = new FormLayout();

        idCentroCostoTxt = new TextField("Id Centro Costo : ");
        idCentroCostoTxt.setWidth("8em");
        idCentroCostoTxt.setReadOnly(true);
        idCentroCostoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
             
        grupoCbx = new ComboBox("Grupo : ");
        grupoCbx.setWidth("15em");
        grupoCbx.setInvalidAllowed(true);
        grupoCbx.setNewItemsAllowed(true);        
        grupoCbx.setFilteringMode(FilteringMode.CONTAINS);        

        fillComboGrupo();

        codigoTxt = new TextField("Código : ");
        codigoTxt.setWidth("15em");
        codigoTxt.setRequired(true);
        codigoTxt.setRequiredError("POR FAVOR INGRESE EL CODIGO DEL CENTRO DE COSTO");

        loteTxt = new TextField("Lote : ");
        loteTxt.setWidth("15em");
        loteTxt.setRequired(true);
        loteTxt.setRequiredError("POR FAVOR INGRESE EL LOTE DEL CENTRO DE COSTO");

        estiloCbx = new ComboBox("Estilo : ");
        estiloCbx.setWidth("15em");
        estiloCbx.setRequired(true);
        estiloCbx.setRequiredError("POR FAVOR INGRESE EL LOTE DEL ESTILO");

        fillComboEstilo();

        maestroSelect = createComboPersona("Maestro de obras :", "MAESTRO");
        supervisorSelect = createComboPersona("Supervisor :", "SUPERVISOR");

        cuentaContableCbx = new ComboBox("Cuenta Contable");
        cuentaContableCbx.setInputPrompt("Cuenta Contable");
        cuentaContableCbx.setWidth("100%");
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);

        llenarComboCuentaContable(cuentaContableCbx);

        cuentaContableProvisionCbx = new ComboBox("Cuenta Contable Provisión");
        cuentaContableProvisionCbx.setInputPrompt("Cuenta Contable Provisión");
        cuentaContableProvisionCbx.setWidth("100%");
        cuentaContableProvisionCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableProvisionCbx.setInvalidAllowed(false);
        cuentaContableProvisionCbx.setNewItemsAllowed(false);

        llenarComboCuentaContable(cuentaContableProvisionCbx);

        cuentaContableGarantiasCbx = new ComboBox("Cuenta Contable Garantías");
        cuentaContableGarantiasCbx.setInputPrompt("Cuenta Contable Garantías");
        cuentaContableGarantiasCbx.setWidth("100%");
        cuentaContableGarantiasCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableGarantiasCbx.setInvalidAllowed(false);
        cuentaContableGarantiasCbx.setNewItemsAllowed(false);

        llenarComboCuentaContable(cuentaContableGarantiasCbx);

        inhabilidatoChb = new CheckBox("Inhabilitado");

        centroCostoForm.addComponent(idCentroCostoTxt);
        centroCostoForm.addComponent(grupoCbx);
        centroCostoForm.addComponent(codigoTxt);
        centroCostoForm.addComponent(loteTxt);
        centroCostoForm.addComponent(estiloCbx);
        centroCostoForm.addComponent(maestroSelect);
        centroCostoForm.addComponent(supervisorSelect);
        centroCostoForm.addComponent(inhabilidatoChb);
        centroCostoForm.addComponent(cuentaContableCbx);
        centroCostoForm.addComponent(cuentaContableProvisionCbx);
        centroCostoForm.addComponent(cuentaContableGarantiasCbx);
        centroCostoForm.setMargin(marginInfo);

        mainLayout.addComponent(centroCostoForm);

        saveBtn = new Button("Guardar");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveCentroCosto();
                saveEncargados();
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT); 
        salirBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                close();
            }
        });        
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        
        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
                
        setContent(mainLayout);
    }    

    void fillComboGrupo() {
        String queryString = "SELECT DISTINCT Grupo ";
        queryString += " FROM centro_costo ";
 
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);
                                    
            while(rsRecords.next()) { //  encontrado                
                grupoCbx.addItem(rsRecords.getString("Grupo"));
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE GRUPOS DE CENTRO DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void fillComboEstilo() {
        String queryString = "Select Distinct Codigo, Nombre ";
        queryString += " From planos_estilo ";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery (queryString);

            while(rsRecords.next()) { //  encontrado
                estiloCbx.addItem(rsRecords.getString("Codigo"));
                estiloCbx.setItemCaption(rsRecords.getString(
                        "Codigo"),
                        rsRecords.getString("Codigo") + " - " + rsRecords.getString("Nombre")
                );
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE ESTILOS DE CENTRO DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private TwinColSelect createComboPersona(String texto, String cargo) {

        TwinColSelect select = new TwinColSelect();
        select.setCaption(texto);

        select.setLeftColumnCaption("Opciones :");
        select.setRightColumnCaption("Seleccionados :");
        select.setImmediate(true);
        select.setNullSelectionAllowed(false);
        select.setNewItemsAllowed(false);
        select.clear();

        String queryString = "SELECT * FROM proveedor_empresa ";
        queryString += " WHERE EsPlanilla = 1";
        queryString += " AND   Cargo Like '" + cargo + "%'";
        queryString += " AND   idEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY Nombre";

        int i = 0;
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) {
                do {
                    String id = rsRecords.getString("IdProveedor");
                    String nombre = rsRecords.getString("Nombre");

                    select.addItem(id);
                    select.setItemCaption(id, nombre);
                    i++;
                } while (rsRecords.next());
            } else {
                select.addItem(0);
                select.setItemCaption(0, "No hay " + cargo + " disponibles");
                select.setEnabled(false);
                i++;
            }

            select.setHeight((i * 5 + 1) + "em");

            // 👇 este es el cambio clave
            select.setWidth("520px");

        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PARTICIPANTES DE REUNIONES", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                    "ERROR AL INTENTAR BUSCAR CATALOGO DE PROVEEDORES (PARTICIPANTES DE REUNIONES)", ex1);
            ex1.printStackTrace();
        }

        return select;
    }


    private void llenarComboCuentaContable(ComboBox combo) {

        String queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " AND   IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY N5";

        try {
            combo.addItem("0");
            combo.setItemCaption("0", "SELECCIONE");

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  // encontrado

                combo.addItem(rsRecords.getString("IdNomenclatura"));
                combo.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
            }

//            cuentaContableCbx.setValue(((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getCompras());
            combo.setValue("0");

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void fillData() {
        
        if(idCentroCostoTxt.getValue().compareTo("0") == 0) {
            codigoTxt.focus();
            return;
        }
        
        String queryString = "";
        
        queryString =  "SELECT * ";
        queryString += " FROM  centro_costo ";
        queryString += " WHERE IdCentroCosto = " + idCentroCostoTxt.getValue();

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                grupoCbx.select(rsRecords.getString("Grupo"));
                codigoTxt.setValue(rsRecords.getString("CodigoCentroCosto"));
                codigoTxt.focus();
                loteTxt.setValue(rsRecords.getString("Lote"));
                estiloCbx.select(rsRecords.getString("CodigoEstilo"));
                maestroSelect.select(rsRecords.getString("IdMaestroObras"));
                supervisorSelect.select(rsRecords.getString("IdSupervisor"));
                inhabilidatoChb.setValue(rsRecords.getString("Inhabilitado").equals("1"));
                cuentaContableCbx.setValue(rsRecords.getString("IdNomenclatura"));
                cuentaContableProvisionCbx.setValue(rsRecords.getString("IdNomenclaturaProvision"));
                cuentaContableGarantiasCbx.setValue(rsRecords.getString("IdNomenclaturaGarantias"));
            }
            else {
                Notification.show("Error, no se encotró registro de centro de costo!", Notification.Type.ERROR_MESSAGE);
                close();
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de centros de costo : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de centros de costo..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    private void saveCentroCosto() {
        
        if(codigoTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el código de centro de costo!", Notification.Type.ERROR_MESSAGE);
            codigoTxt.focus();
            return;
        }
        if (cuentaContableCbx.getValue() == null) {
            Notification.show("POR FAVOR SELECCIONE UNA CUENTA CONTABLE.", Notification.Type.WARNING_MESSAGE);
            cuentaContableCbx.focus();
            return;
        }
        if (cuentaContableCbx.getValue() == "0") {
            Notification.show("POR FAVOR SELECCIONE UNA CUENTA CONTABLE.", Notification.Type.WARNING_MESSAGE);
            cuentaContableCbx.focus();
            return;
        }
        if (cuentaContableProvisionCbx.getValue() == null) {
            Notification.show("POR FAVOR SELECCIONE UNA CUENTA CONTABLE PROVISION.", Notification.Type.WARNING_MESSAGE);
            cuentaContableProvisionCbx.focus();
            return;
        }
        if (cuentaContableGarantiasCbx.getValue() == "0") {
            Notification.show("POR FAVOR SELECCIONE UNA CUENTA CONTABLE GARANTIAS.", Notification.Type.WARNING_MESSAGE);
            cuentaContableGarantiasCbx.focus();
            return;
        }

        String queryString;
        
        if(idCentroCostoTxt.getValue().compareTo("0") == 0) {
            queryString =  "INSERT INTO centro_costo (IdEmpresa, IdProyecto, CodigoCentroCosto, Lote, Grupo, CodigoEstilo, Inhabilitado ";
            queryString += ", IdNomenclatura, IdNomenclaturaProvision, IdNomenclaturaGarantias )";
            queryString += " VALUES (";
            queryString += "  " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " ," + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
            queryString += ",'" + codigoTxt.getValue()      + "'";
            queryString += ",'" + loteTxt.getValue()      + "'";
            queryString += ",'" + grupoCbx.getValue() + "'";
            queryString += "," + estiloCbx.getValue();
            queryString += ","  + (inhabilidatoChb.getValue() ? 1 : 0);
            queryString += ","  + (cuentaContableCbx.getValue() != null ? cuentaContableCbx.getValue() : "0");
            queryString += ","  + (cuentaContableProvisionCbx.getValue() != null ? cuentaContableProvisionCbx.getValue() : "0");
            queryString += ","  + (cuentaContableGarantiasCbx.getValue() != null ? cuentaContableGarantiasCbx.getValue() : "0");
            queryString += ")";
        }
        else {
            queryString =  "UPDATE centro_costo SET ";
            queryString += " CodigoCentroCosto = '" + codigoTxt.getValue()      + "'";
            queryString += ",Lote = '" + loteTxt.getValue()      + "'";
            queryString += ",Grupo = '" + grupoCbx.getValue() + "'";
            queryString += ",CodigoEstilo = " + estiloCbx.getValue();
            queryString += ",Inhabilitado = "  + (inhabilidatoChb.getValue() ? 1 : 0);
            queryString += ",IdNomenclatura = "  + (cuentaContableCbx.getValue() != null ? cuentaContableCbx.getValue() : "0");
            queryString += ",IdNomenclaturaProvision = "  + (cuentaContableProvisionCbx.getValue() != null ? cuentaContableProvisionCbx.getValue() : "0");
            queryString += ",IdNomenclaturaGarantias = "  + (cuentaContableGarantiasCbx.getValue() != null ? cuentaContableGarantiasCbx.getValue() : "0");
            queryString += " WHERE IdCentroCosto = " + idCentroCostoTxt.getValue();
        }
//System.out.println("queryString="+queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            stQuery.executeUpdate(queryString);
               
            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
            ((CostCenterView)(mainUI.getNavigator().getCurrentView())).fillEncargadosGrid(idCentroCostoTxt.getValue());
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar el centro de costo : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        
        close();
    }

    private void saveEncargados() {

        if(codigoTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el código de centro de costo!", Notification.Type.ERROR_MESSAGE);
            codigoTxt.focus();
            return;
        }

        String queryString;
        Set<Object> seleccionados;

        if(idCentroCostoTxt.getValue().compareTo("0") != 0) {

            queryString = "UPDATE centro_costo_encargado SET ";
            queryString += " Eliminado = 1";
            queryString += ",FechaEliminado = current_timestamp";
            queryString += " WHERE CodigoCentroCosto = " + codigoTxt.getValue() + "; ";

            try {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
                ((CostCenterView)(mainUI.getNavigator().getCurrentView())).fillEncargadosGrid(idCentroCostoTxt.getValue());
            }
            catch(Exception ex)
            {
                Notification.show("Error al actualizar el centro de costo : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

        if(maestroSelect.getValue() == null && supervisorSelect.getValue() == null) {
            return;
        }
        else {

            queryString = "Insert Into centro_costo_encargado (CodigoCentroCosto, IdProveedor, Nombre, Tipo) ";
            queryString += " Values ";

            seleccionados = (Set<Object>) maestroSelect.getValue();

            String idproveedor = "";

            for (Object item : seleccionados) { // Agregar cada Maestro
                idproveedor = (String) item;
                queryString += " (" + codigoTxt.getValue();
                queryString += "," + idproveedor;
                queryString += ", '" + maestroSelect.getItemCaption(item) + "'";
                queryString += "," + "'MAESTRO DE OBRAS'";
                queryString += "), ";
            }

            seleccionados = (Set<Object>) supervisorSelect.getValue();

            String idproveedorSup = "";

            for (Object item : seleccionados) { // Agregar cada Supervisor
                idproveedorSup = (String) item;
                queryString += " (" + codigoTxt.getValue();
                queryString += ", " + idproveedorSup;
                queryString += ", '" + supervisorSelect.getItemCaption(item) + "'";
                queryString += ", " + "'SUPERVISOR'";
                queryString += "), ";
            }

            queryString = queryString.substring(0, queryString.length() - 2);

System.out.println("queryString="+queryString);

            try {
                if(idproveedor != "" || idproveedorSup != ""){
                    stQuery.executeUpdate(queryString);
                }

            } catch (Exception ex) {
                Notification.show("Error al actualizar el centro de costo : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                ex.printStackTrace();
                close();
            }
        }
        Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
        ((CostCenterView) (mainUI.getNavigator().getCurrentView())).fillEncargadosGrid(idCentroCostoTxt.getValue());

        close();
    }

}