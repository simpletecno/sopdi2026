package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class EmpresasContablesForm extends Window {

    public String idEmpresaEdit = "0";

    UI mainUI;
    Statement stQuery = null;
    static PreparedStatement stPreparedQuery;
    ResultSet rsRecords = null;
    String queryString = "";

    MarginInfo marginInfo;

    FormLayout mainForm;

    NumberField idEmpresaTxt;

    TextField nombreTxt;
    TextField nombreCortoTxt;
    TextField nitTxt;
    NumberField ultimaLiquidacionTxt;
    CheckBox recibeEnganchesCheck;
    TextField usuarioFELTxt;
    TextField claveFELTxt;
    TextField tokenFELTxt;
    ComboBox regimenCbx;
    TextField codigoProductoExcelFELTxt;

    Button guardarBtn;
    Button salirBtn;

    MultiFileUpload singleUpload;
    Image logoImage;
    public File file;
    StreamResource logoStreamResource = null;

    public EmpresasContablesForm() {        
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setModal(true);

        marginInfo = new MarginInfo(true, true, false, true);

        mainForm = new FormLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);    

        idEmpresaTxt = new NumberField("Empresa : ");
        idEmpresaTxt.setWidth("8em");
        idEmpresaTxt.setMaxValue(999);
        idEmpresaTxt.setReadOnly((!idEmpresaEdit.equals("0")));

        nombreTxt = new TextField("Nombre :");
        nombreTxt.setWidth("15em");
        nombreTxt.setMaxLength(128);
       
        nombreCortoTxt = new TextField("Nombre corto:");
        nombreCortoTxt.setWidth("15em");
        nombreCortoTxt.setMaxLength(128);

        nitTxt = new TextField("NIT :");
        nitTxt.setWidth("8em");
        nitTxt.setMaxLength(128);

        ultimaLiquidacionTxt = new NumberField("Ultima Liquidación :");
        ultimaLiquidacionTxt.setWidth("8em");
        ultimaLiquidacionTxt.setDecimalAllowed(false);
        ultimaLiquidacionTxt.setDecimalPrecision(0);
        ultimaLiquidacionTxt.setMaxValue(9999);

        recibeEnganchesCheck = new CheckBox("Recibe enganches : ");
        recibeEnganchesCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        recibeEnganchesCheck.setValue(false);

        usuarioFELTxt = new TextField("FEL Usuario:");
        usuarioFELTxt.setWidth("15em");
        usuarioFELTxt.setMaxLength(128);

        claveFELTxt = new TextField("FEL Clave:");
        claveFELTxt.setWidth("15em");
        claveFELTxt.setMaxLength(128);

        tokenFELTxt = new TextField("FEL Token:");
        tokenFELTxt.setWidth("15em");
        tokenFELTxt.setMaxLength(128);

        regimenCbx = new ComboBox("REGIMEN : ");
        regimenCbx.setWidth("25em");
        regimenCbx.setInvalidAllowed(false);
        regimenCbx.setNewItemsAllowed(false);
        regimenCbx.setTextInputAllowed(false);
        regimenCbx.setNullSelectionAllowed(false);
        regimenCbx.addItem("Sobre las Utilidades de Actividades Lucrativas");
        regimenCbx.addItem("Opcional Simplificado sobre Ingresos de Actividades Lucrativas");
        regimenCbx.select("Sobre las Utilidades de Actividades Lucrativas");

        codigoProductoExcelFELTxt = new TextField("Código FEL producto EXENTO :");
        codigoProductoExcelFELTxt.setWidth("10em");
        codigoProductoExcelFELTxt.setMaxLength(32);

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
                insertarEmpresaContable();
            }
        });

        mainForm.addComponent(idEmpresaTxt);
        mainForm.addComponent(nombreTxt);
        mainForm.addComponent(nombreCortoTxt);
        mainForm.addComponent(nitTxt);
        mainForm.addComponent(ultimaLiquidacionTxt);
        mainForm.addComponent(recibeEnganchesCheck);
        mainForm.addComponent(usuarioFELTxt);
        mainForm.addComponent(claveFELTxt);
        mainForm.addComponent(tokenFELTxt);
        mainForm.addComponent(regimenCbx);
        mainForm.addComponent(codigoProductoExcelFELTxt);

        HorizontalLayout logoImgLayout = new HorizontalLayout();
        logoImgLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        logoImgLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        logoImage = new Image();
        logoImage.setImmediate(true);
        logoImage.setWidth("100px");
        logoImage.setHeight("100px");
        logoImage.setIcon(FontAwesome.IMAGE);
//        logoImage.addStyleName("menu-logo-empresa");

        logoImgLayout.addComponent(logoImage);

        UploadFinishedHandler handler;
        handler = new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                File targetFile;

                try {

                    System.out.println("\nfileName="+fileName);
                    System.out.println("length="+stream.available());
                    System.out.println("mimeType="+mimeType);

                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);
                    String filePath = VaadinService.getCurrent()
                            .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId() + "/";

                    new File(filePath).mkdirs();

                    fileName = filePath + fileName;
                    targetFile = new File(fileName);
                    OutputStream outStream = new FileOutputStream(targetFile);
                    outStream.write(buffer);
                    outStream.close();
                    stream.close();

                    System.out.println("\ntargetFile = " + fileName);

                    logoStreamResource = null;

                    if(buffer != null ) {
                        logoStreamResource = new StreamResource(
                                new StreamResource.StreamSource() {
                                    public InputStream getStream() {
                                        return new ByteArrayInputStream(buffer);
                                    }
                                },idEmpresaTxt.getValue()
                        );
                    }
                    logoImage.setSource(logoStreamResource);
                    file = targetFile;
                }
                catch( java.io.IOException fIoEx) {
                    fIoEx.printStackTrace();
                    Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler,window, false);
//        singleUpload.setCaption("Cargar archivo");
//        singleUpload2.setRootDirectory(newDoc.getAbsolutePath());
//        singleUpload.setPanelCaption("Buscar");
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar logo del empresa", "");

        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.jpeg')");

/**
 List<String> acceptedMimeTypes = new ArrayList();
 acceptedMimeTypes.add("application/octet-stream");
 acceptedMimeTypes.add("application/ms-project");
 acceptedMimeTypes.add("application/vnd.ms-project");
 acceptedMimeTypes.add("application/msproj");
 acceptedMimeTypes.add("application/msproject");
 acceptedMimeTypes.add("application/x-msproject");
 acceptedMimeTypes.add("application/x-ms-project");
 acceptedMimeTypes.add("application/x-dos_ms_project");
 acceptedMimeTypes.add("application/mpp");
 acceptedMimeTypes.add("zz-application/zz-winassoc-mpp");
 //        singleUpload.setAcceptedMimeTypes(acceptedMimeTypes);
 **/

        logoImgLayout.addComponent(singleUpload);

        mainForm.addComponent(logoImgLayout);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(salirBtn,guardarBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(buttonsLayout);
        mainForm.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);

        Label titleLbl = new Label("Empresa contable.");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

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
            queryString += " FROM contabilidad_empresa";
            queryString += " WHERE IdEmpresa = " + idEmpresaEdit;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                idEmpresaTxt.setReadOnly(false);
                idEmpresaTxt.setValue(rsRecords.getString("IdEmpresa"));
                idEmpresaTxt.setReadOnly(true);
                nombreTxt.setValue(rsRecords.getString("Empresa"));
                nombreCortoTxt.setValue(rsRecords.getString("NombreCorto"));
                nitTxt.setValue(rsRecords.getString("Nit"));
                ultimaLiquidacionTxt.setValue(rsRecords.getString("IdUltimaLiquidacion"));
                if(rsRecords.getString("RecibeEnganches").equals("1")){
                    recibeEnganchesCheck.setValue(true);
                }
                usuarioFELTxt.setValue(rsRecords.getString("UsuarioFEL"));
                claveFELTxt.setValue(rsRecords.getString("ClaveFEL"));
                tokenFELTxt.setValue(rsRecords.getString("TokenFEL"));
                regimenCbx.select(rsRecords.getString("Regimen"));
                codigoProductoExcelFELTxt.setValue(rsRecords.getString("CodigoProductoExentoFel"));

                final byte docBytes[] = rsRecords.getBytes("Logo");
                StreamResource logoStreamResource = null;

                if(docBytes != null ) {
                    logoStreamResource = new StreamResource(
                            new StreamResource.StreamSource() {
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(docBytes);
                                }
                            },idEmpresaTxt.getValue()
                    );
                }
                logoImage.setSource(logoStreamResource);

            }
        } catch (SQLException ex) {
            System.out.println("Error al llenar Campos " + ex);
            ex.printStackTrace();
        }
    }

    public void insertarEmpresaContable() {
        try {

            if(nombreTxt.getValue().trim().isEmpty()) {
                Notification.show("Ingrese el nombre de la empresa.", Notification.Type.WARNING_MESSAGE);
                nombreTxt.focus();
                return;
            }
            if(nombreCortoTxt.getValue().trim().isEmpty()) {
                Notification.show("Ingrese el nombre corto de la empresa.", Notification.Type.WARNING_MESSAGE);
                nombreCortoTxt.focus();
                return;
            }
            if(nitTxt.getValue().trim().isEmpty()) {
                Notification.show("Ingrese el NIT de la empresa.", Notification.Type.WARNING_MESSAGE);
                nitTxt.focus();
                return;
            }
            if (idEmpresaEdit.equals("0")) {
                queryString = "INSERT INTO contabilidad_empresa (IdEmpresa, Empresa, NombreCorto, Nit, " +
                        "IdUltimaLiquidacion, RecibeEnganches, UsuarioFEL, ClaveFEL, UsuarioToken, " +
                        "Regimen, CodigoProductoExentoFel, Logo)";
                queryString += " VALUES (";
                queryString += " " + idEmpresaTxt.getValue();
                queryString += ",'" + nombreTxt.getValue() + "'";
                queryString += ",'" + nombreCortoTxt.getValue() + "'";
                queryString += "," + nitTxt.getValue();
                queryString += "," + ultimaLiquidacionTxt.getValue();
                if (recibeEnganchesCheck.getValue() == true){
                    queryString += ", 1";
                }else{
                    queryString += ", 0";
                }
                queryString += ", '" + usuarioFELTxt.getValue() + "'";
                queryString += ", '" + claveFELTxt.getValue() + "'";
                queryString += ", '" + tokenFELTxt.getValue() + "'";
                queryString += ", '" + regimenCbx.getValue() + "'";
                queryString += ", '" + codigoProductoExcelFELTxt.getValue() + "'";
                queryString += ",?";
                queryString += ")";
            } else {
                queryString = "UPDATE contabilidad_empresa SET ";
                queryString += " Empresa = '" + nombreTxt.getValue() + "'";
                queryString += ",NombreCorto = '" + nombreCortoTxt.getValue() + "'";
                queryString += ",Nit = '" + nitTxt.getValue() + "'";
                queryString += ",IdUltimaLiquidacion  = " + ultimaLiquidacionTxt.getValue();
                if (recibeEnganchesCheck.getValue() == true){
                    queryString += ", RecibeEnganches = 1";
                }else{
                    queryString += ", RecibeEnganches = 0";
                }
                queryString += ", UsuarioFEL = '" + usuarioFELTxt.getValue() + "'";
                queryString += ", ClaveFEL = '" + claveFELTxt.getValue() + "'";
                queryString += ", TokenFEL = '" + tokenFELTxt.getValue() + "'";
                queryString += ", Regimen = '" + regimenCbx.getValue() + "'";
                queryString += ", CodigoProductoExentoFel = '" + codigoProductoExcelFELTxt.getValue() + "'";
                queryString += ",Logo = ?";
                queryString += " WHERE IdEmpresa = " + idEmpresaEdit;
            }

            stPreparedQuery  = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString);

            if(logoStreamResource != null) {

                stPreparedQuery.setBinaryStream(1, logoStreamResource.getStream().getStream(), logoStreamResource.getStream().getStream().available());
//                    receiver.file.delete();
            }
            else {
                stPreparedQuery.setBinaryStream(1, null, 0);
            }

            stPreparedQuery.executeUpdate();

            ((EmpresasContablesView) (mainUI.getNavigator().getCurrentView())).llenarTablaEmpresas();
            
            close();

        } catch (Exception ex) {
            Notification.show("ERROR AL CREAR O EDITAR EMPRESA CONTABLE : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            System.out.println("Error al insertar o editar " + queryString);
            ex.printStackTrace();
        }
    }
}
