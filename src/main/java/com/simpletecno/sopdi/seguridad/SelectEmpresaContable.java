package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.configuracion.ProjectSelectionForm;
import com.simpletecno.sopdi.contabilidad.EmpresaCuentasEquivalentesHelper;
import com.simpletecno.sopdi.extras.infile.Direccion;
import com.simpletecno.sopdi.extras.infile.Emisor;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.*;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class SelectEmpresaContable extends Window {

    VerticalLayout mainLayout = new VerticalLayout();

    static final String LOGO_PROPERTY = "Logo";
    static final String ID_PROPERTY = "Id";
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String NOMBRE_CORTO_PROPERTY = "Nombre corto";
    static final String NIT_PROPERTY = "NIT";
    static final String REGIMEN_PROPERTY = "REGIMEN";
    static final String ULTIMA_LIQUI_PROPERTY = "Ultima Liq.";

    MarginInfo marginInfo;

    Table empresasTable = new Table("Empresas");
    Table proyectosTable = new Table("Proyectos");

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    Button selectBtn;
    Button exitBtn;

    public SelectEmpresaContable() {
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setWidth("50%");
        setHeight("70%");

        Label titleLbl = new Label("Empresas contables");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(false);
        titleLayout.setSpacing(true);

        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        Responsive.makeResponsive(this);

        createTablaEmpresas();
        llenarTablaEmpresas();

        crearTableProyectos();
        llenarTablaProyectos();

        crearBotones();

        setContent(mainLayout);

        setSizeUndefined();
    }

    public void createTablaEmpresas() {
        HorizontalLayout empresasLayout = new HorizontalLayout();
        empresasLayout.setWidth("100%");
        empresasLayout.addStyleName("rcorners2");
        empresasLayout.setResponsive(true);
        empresasLayout.setMargin(true);

        empresasTable.setImmediate(true);
        empresasTable.setSelectable(true);

        empresasLayout.addComponent(empresasTable);
        empresasLayout.setComponentAlignment(empresasTable, Alignment.MIDDLE_CENTER);

        empresasTable.setWidth("100%");
        empresasTable.setPageLength(5);

        empresasTable.addContainerProperty(LOGO_PROPERTY,  Image.class, null);
        empresasTable.addContainerProperty(ID_PROPERTY,    String.class, null);
        empresasTable.addContainerProperty(NOMBRE_PROPERTY,    String.class, null);
        empresasTable.addContainerProperty(NOMBRE_CORTO_PROPERTY,    String.class, null);
        empresasTable.addContainerProperty(NIT_PROPERTY, String.class, "");
        empresasTable.addContainerProperty(REGIMEN_PROPERTY, String.class, "");
        empresasTable.addContainerProperty(ULTIMA_LIQUI_PROPERTY, String.class, "");

        empresasTable.setColumnAlignments(new Table.Align[] {
                Table.Align.CENTER, Table.Align.CENTER,Table.Align.LEFT, Table.Align.LEFT,
                Table.Align.LEFT,   Table.Align.LEFT,  Table.Align.CENTER
        });

        mainLayout.addComponent(empresasLayout);
        mainLayout.setComponentAlignment(empresasLayout, Alignment.TOP_CENTER);

    }

    public void llenarTablaEmpresas() {

        Image empresaLogo;

        String queryString = "SELECT * ";
        queryString += "FROM contabilidad_empresa CE ";
        queryString += "INNER JOIN usuario_permisos_empresa UPE ON UPE.IdEmpresa = CE.IdEmpresa AND UPE.IdUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId() + " ";
        queryString += "WHERE CE.IdEmpresa > 0 ";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) { //  encontrado     

                int primeraEmpresa = rsRecords.getInt("IdEmpresa");

                do {
                    final byte docBytes[] = rsRecords.getBytes("Logo");
                    StreamResource logoStreamResource = null;

                    if(docBytes != null ) {
                        logoStreamResource = new StreamResource(
                                new StreamResource.StreamSource() {
                                    public InputStream getStream() {
                                        return new ByteArrayInputStream(docBytes);
                                    }
                                },rsRecords.getString("IdEmpresa")
                        );
                    }

                    empresaLogo = new Image(null,logoStreamResource);
                    empresaLogo.setImmediate(true);
                    empresaLogo.setWidth("35px");
                    empresaLogo.setHeight("35px");

                    empresasTable.addItem(new Object[]{
                            empresaLogo,
                            rsRecords.getString("IdEmpresa"),
                            rsRecords.getString("Empresa"),
                            rsRecords.getString("NombreCorto"),
                            rsRecords.getString("Nit"),
                            rsRecords.getString("Regimen"),
                            rsRecords.getString("IdUltimaLiquidacion")
                    }, rsRecords.getInt("IdEmpresa"));
                    
                } while (rsRecords.next());

                if(empresasTable.size() == 1) { // una unica empresa se selecciona por default
                    empresasTable.select(primeraEmpresa);
                }
            }
            else {
                selectBtn.setEnabled(false);
            }
        } catch (Exception ex) {
            System.out.println("Error al buscar tabla empresas contables :" + ex);
            ex.printStackTrace();
            Notification.show("ERROR AL BUSCAR EMPRESAS CONTABLES.", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void crearTableProyectos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners4");

//        proyectosTable.addStyleName(ValoTheme.TABLE_SMALL);
//        proyectosTable.addStyleName(ValoTheme.TABLE_COMPACT);
//        proyectosTable.addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        proyectosTable.setImmediate(true);
        proyectosTable.setSelectable(true);

        reportLayout.addComponent(proyectosTable);
        reportLayout.setComponentAlignment(proyectosTable, Alignment.MIDDLE_CENTER);

        proyectosTable.setWidth("100%");
        proyectosTable.setPageLength(5);

//        proyectosTable.addContainerProperty(CODIGO_PROPERTY,    String.class, null);
        proyectosTable.addContainerProperty(LOGO_PROPERTY,      Image.class, null);
        proyectosTable.addContainerProperty(NOMBRE_PROPERTY,    String.class, null);

        proyectosTable.setColumnAlignments(new Table.Align[] {
                Table.Align.CENTER, Table.Align.LEFT
        });

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.BOTTOM_CENTER);

    }

    public void llenarTablaProyectos() {

        String queryString = "Select Pro.*";
        queryString += " From  proyecto_usuario ProUsr";
        queryString += " Inner Join proyecto Pro On Pro.IdProyecto = ProUsr.IdProyecto";
        queryString += " Where ProUsr.IdUsuario = " + ((SopdiUI)mainUI).sessionInformation.getStrUserId();
//        queryString += " And Pro.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId();
//        queryString += " Order By Pro.Nombre";

//System.out.println("\n\nQuery="+queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                int primerRegistro = rsRecords.getInt("IdProyecto");

                Image proyectoLogo;

                do {
                    final byte docBytes[] = rsRecords.getBytes("Logo");
                    StreamResource logoStreamResource = null;

                    if(docBytes != null ) {
                        logoStreamResource = new StreamResource(
                                new StreamResource.StreamSource() {
                                    @Override
                                    public InputStream getStream() {
                                        return new ByteArrayInputStream(docBytes);
                                    }
                                },rsRecords.getString("IdProyecto")
                        );
                    }

                    proyectoLogo = new Image(null, logoStreamResource);
                    proyectoLogo.setImmediate(true);
                    proyectoLogo.setWidth("40px");
                    proyectoLogo.setHeight("40px");

                    proyectosTable.addItem(new Object[] {
                            //                      rsRecords.getString("IdProyecto"),
                            proyectoLogo,
                            rsRecords.getString("Nombre")
                    }, rsRecords.getInt("IdProyecto"));

                }while(rsRecords.next());

                if(proyectosTable.size() == 1) { // un unico proyecto se selecciona por default
                    proyectosTable.select(primerRegistro);
                }
            }
            else {
                selectBtn.setEnabled(false);
            }
        }
        catch (Exception ex) {
            Logger.getLogger(ProjectSelectionForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de proyectos : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de proyectos..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void crearBotones() {
        selectBtn = new Button("Aceptar");
        selectBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        selectBtn.setIcon(FontAwesome.CHECK);
        selectBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(empresasTable.getValue() != null && proyectosTable.getValue() != null) {
                        setEmpresaYProyecto();
                }
                else {
                    Notification.show("Por favor seleccione una empresa y un proyecto.", Notification.Type.WARNING_MESSAGE);
                }
            }

        });

        exitBtn    = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        exitBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                ((SopdiUI) mainUI).logOff();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(selectBtn);
        buttonsLayout.setComponentAlignment(selectBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

    }

    private void setEmpresaYProyecto() {

        queryString = "SELECT * FROM proyecto WHERE IdProyecto = " + proyectosTable.getValue();

        try {
            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            ((SopdiUI)mainUI).sessionInformation.setStrProjectId(String.valueOf(proyectosTable.getValue()));
            ((SopdiUI)mainUI).sessionInformation.setStrProjectName(String.valueOf(proyectosTable.getContainerProperty(proyectosTable.getValue(), NOMBRE_PROPERTY).getValue()));

            byte[] imageBytes = rsRecords.getBytes("Logo");

            if (imageBytes != null) {
                ((SopdiUI) mainUI).sessionInformation.setProjectStreamResource(new StreamResource(
                        new StreamResource.StreamSource() {
                            public InputStream getStream() {
                                return new ByteArrayInputStream(imageBytes);
                            }
                        }, ((SopdiUI) mainUI).sessionInformation.getStrProjectId()
                ));
            }
            else {
                ((SopdiUI) mainUI).sessionInformation.setProjectStreamResource(null);
            }

            queryString = "SELECT * FROM contabilidad_empresa WHERE idEmpresa = " + empresasTable.getValue();

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyId(rsRecords.getString("IdEmpresa"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyId(rsRecords.getString("IdEmpresa"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyName(rsRecords.getString("Empresa"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanySmallName(rsRecords.getString("NombreCorto"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyTaxId(rsRecords.getString("Nit"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyRegimen(rsRecords.getString("Regimen"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyFelUser(rsRecords.getString("UsuarioFEL"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyFelPass(rsRecords.getString("ClaveFEL"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyFelToken(rsRecords.getString("TokenFEL"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyDirection(rsRecords.getString("Direccion"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyBillingDirection(rsRecords.getString("DireccionFactura"));
            ((SopdiUI) mainUI).sessionInformation.setStrAccountingCompanyFelCodProdExento(rsRecords.getString("CodigoProductoExentoFel"));

//System.out.println("AccCommpanyId=" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + " AccCompanyName=" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName());

            Emisor emisor = new Emisor(
                    rsRecords.getString("Nit"),
                    rsRecords.getString("Empresa"),
                    String.valueOf(rsRecords.getString("CorreoContacto")),
                    new Direccion(
                            rsRecords.getString("DireccionFactura"),
                            rsRecords.getString("CodigoPostal"),
                            rsRecords.getString("NombreDepartamento"),
                            rsRecords.getString("NombreMunicipio"),
                            rsRecords.getString("Pais")
                    ),
                    rsRecords.getString("AfiliacionIVA"),
                    "1",
                    rsRecords.getString("NombreCorto"),
                    rsRecords.getString("InfileUsuarioApi"),
                    rsRecords.getString("InfileLlaveApi"),
                    rsRecords.getString("InfileUsuarioFirma"),
                    rsRecords.getString("InfileLlaveFirma"),
                    rsRecords.getString("TipoPersoneria")
            );
            ((SopdiUI) mainUI).sessionInformation.setInfileEmisor(emisor);

            for (Iterator<Component> it = ((SopdiUI) mainUI).getMenuItemsLayout().iterator(); it.hasNext();) {
                final Component c = it.next();
//System.out.println(c.getClass().getName());
                if ("com.vaadin.ui.Label".equals(c.getClass().getName())) {
                    final Label label = (Label) c;
                    if (label.getValue().contains("Contabilidad")) {
                        label.setValue("Contabilidad : " + rsRecords.getString("NombreCorto"));
                        break;
                    }
                }
            }

            byte[] imageBytes1 = rsRecords.getBytes("Logo");

            if (imageBytes1 != null) {
                ((SopdiUI) mainUI).sessionInformation.setPhotoStreamResource(new StreamResource(
                        new StreamResource.StreamSource() {
                            public InputStream getStream() {
                                return new ByteArrayInputStream(imageBytes1);
                            }
                        }, ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId()
                ));
            }
            else {
                ((SopdiUI) mainUI).sessionInformation.setPhotoStreamResource(null);
            }

        }
        catch (Exception ex) {
            Notification.show("ERROR AL BUSCAR DATOS DE EMPRESA : " + ex.getMessage(), Notification.Type.WARNING_MESSAGE);
            ex.printStackTrace();
        }

        ((SopdiUI) mainUI).fillCuentasContablesPorDefault();

        ((SopdiUI) mainUI).fillProveedoresInstitucionales();
        generarEmpresaCuentasEquivalentes(((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId());

        if(((SopdiUI) mainUI).getMenuItems().isEmpty()) {
            ((SopdiUI) mainUI).buildMainView();
        }
        else {
            mainUI.setContent(((SopdiUI) mainUI).getRoot());
            ((SopdiUI) mainUI).getUserSettingsItem().setIcon(((SopdiUI) UI.getCurrent()).sessionInformation.getPhotoStreamResource());
        }

//System.out.println(((SopdiUI) mainUI).getNavigator().getCurrentView().getClass().getName());
        if (((SopdiUI) mainUI).currentViewName != null) {
            ((SopdiUI) mainUI).getNavigator().navigateTo(((SopdiUI) mainUI).currentViewName);
        }

        String empresaProyecto = "<strong>" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName() + "<br></strong><strong>" + ((SopdiUI) mainUI).sessionInformation.getStrProjectName() + "</strong></br>";

        ((Label)((SopdiUI) mainUI).top.getComponent(0)).setValue(empresaProyecto);

        close();

    }

    private void generarEmpresaCuentasEquivalentes(String idEmpresa) {
        String queryString = "SELECT * ";
        queryString += "FROM empresa_cuenta_equivalente ";
        queryString += "WHERE IdEmpresa = " + idEmpresa + " ";
        queryString += "ORDER BY IdEmpresa, IdNomenclatura, IdEmpresa_1, IdNomenclatura_1";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                List<Object[]> relaciones = new java.util.ArrayList<>();
                do {
                    Object nom = rsRecords.getObject("IdNomenclatura");
                    Object emp1 = rsRecords.getObject("IdProveedor_1");
                    Object nom1 = rsRecords.getObject("IdNomenclatura_1");
                    relaciones.add(new Object[]{nom, emp1, nom1});
                }
                while (rsRecords.next());
                EmpresaCuentasEquivalentesHelper helper = new EmpresaCuentasEquivalentesHelper(relaciones);
                System.out.println(helper);
                ((SopdiUI) mainUI).sessionInformation.setEmpresaCuentasEquivalentesHelper(helper);
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla empresas empresa_cuenta_equivalente :" + ex);
            ex.printStackTrace();
            Notification.show("ERROR AL SELECCIONAR EMPRESAS CONTABLES EQUIVALENTES.", Notification.Type.ERROR_MESSAGE);
        }
    }
}
