package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmpresaCuentaEquivalenteView extends VerticalLayout implements View {

    public static final String ARROW_PROPERTY = "";
    public static final String INHABILITADA_PROPERTY = "inhabilitada";

    public enum Col {
        ID_EMPRESA("IdEmpresa"),
        ID_PROVEEDOR("IdProveedor"),
        NOMBRE_EMPRESA("NombreEmpresa"),
        ID_NOMENCLATURA("IdNomenclatura"),
        N5("N5"),
        ID_EMPRESA_1("IdEmpresa_1"),
        ID_PROVEEDOR_1("IdProveedor_1"),
        NOMBRE_EMPRESA_1("NombreEmpresa_1"),
        ID_NOMENCLATURA_1("IdNomenclatura_1"),
        N5_1("N5_1");

        private final String id;
        Col(String id) { this.id = id; }
        public String id() { return id; }
    }

    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public IndexedContainer container = new IndexedContainer();
    Grid cuentasEquivalentesGrid;

    UI mainUI;
    Button inhabilitarBtn;
    Button exportExcelBtn;

    public EmpresaCuentaEquivalenteView() {

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);
        this.mainUI = UI.getCurrent();

        Label titleLbl = new Label("Cuentas Equivalentes por Empresa");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createTablaCuentasContables();
        llenarTablaCuentas();

        createButtons();

    }

    public void createTablaCuentasContables() {
        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        String centeredIcon = "<center>"
                + com.vaadin.server.FontAwesome.ARROW_RIGHT.getHtml()
                + "</center>";

        container.addContainerProperty(Col.ID_EMPRESA.id(), Integer.class, null);
        container.addContainerProperty(Col.ID_PROVEEDOR.id(), Integer.class, null);
        container.addContainerProperty(Col.NOMBRE_EMPRESA.id(), String.class, null);
        container.addContainerProperty(Col.ID_NOMENCLATURA.id(), Integer.class, null);
        container.addContainerProperty(Col.N5.id(), String.class, null);
        container.addContainerProperty(ARROW_PROPERTY, String.class, centeredIcon);
        container.addContainerProperty(Col.ID_EMPRESA_1.id(), Integer.class, null);
        container.addContainerProperty(Col.ID_PROVEEDOR_1.id(), Integer.class, null);
        container.addContainerProperty(Col.NOMBRE_EMPRESA_1.id(), String.class, null);
        container.addContainerProperty(Col.ID_NOMENCLATURA_1.id(), Integer.class, null);
        container.addContainerProperty(Col.N5_1.id(), String.class, null);
        container.addContainerProperty(INHABILITADA_PROPERTY, Boolean.class, null);

        cuentasEquivalentesGrid = new Grid("Listado de equivalencias", container);
        cuentasEquivalentesGrid.setImmediate(true);
        cuentasEquivalentesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        cuentasEquivalentesGrid.setDescription("Seleccione un registro.");
        cuentasEquivalentesGrid.setHeightMode(HeightMode.ROW);
        cuentasEquivalentesGrid.setHeightByRows(10);
        cuentasEquivalentesGrid.setWidth("100%");
        cuentasEquivalentesGrid.setResponsive(true);
        cuentasEquivalentesGrid.setEditorBuffered(false);

        cuentasEquivalentesGrid.getColumn(ARROW_PROPERTY).setRenderer(new com.vaadin.ui.renderers.HtmlRenderer());
        cuentasEquivalentesGrid.getColumn(ARROW_PROPERTY).setWidth(40);
        cuentasEquivalentesGrid.getColumn(Col.NOMBRE_EMPRESA.id()).setExpandRatio(1);
        cuentasEquivalentesGrid.getColumn(Col.N5.id()).setExpandRatio(4);
        cuentasEquivalentesGrid.getColumn(Col.NOMBRE_EMPRESA_1.id()).setExpandRatio(1);
        cuentasEquivalentesGrid.getColumn(Col.N5_1.id()).setExpandRatio(4);

        cuentasEquivalentesGrid.getColumn(Col.ID_EMPRESA.id()).setHidden(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_EMPRESA.id()).setHidable(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_PROVEEDOR.id()).setHidden(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_PROVEEDOR.id()).setHidable(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_NOMENCLATURA.id()).setHidden(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_NOMENCLATURA.id()).setHidable(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_EMPRESA_1.id()).setHidden(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_EMPRESA_1.id()).setHidable(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_PROVEEDOR_1.id()).setHidden(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_PROVEEDOR_1.id()).setHidable(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_NOMENCLATURA_1.id()).setHidden(true);
        cuentasEquivalentesGrid.getColumn(Col.ID_NOMENCLATURA_1.id()).setHidable(true);
        cuentasEquivalentesGrid.getColumn(INHABILITADA_PROPERTY).setHidden(true);

        cuentasEquivalentesGrid.addItemClickListener((event) -> {
            if (event != null) {
                if (event.isDoubleClick()) {
                    if(cuentasEquivalentesGrid.getSelectedRow() == null) {
                        return;
                    }
                    Object itemId = event.getItemId();

                    EmpresaCuentaEquivalenteResumen empresaCuentaEquivalenteResumen = new EmpresaCuentaEquivalenteResumen(
                            (Integer) container.getContainerProperty(itemId, Col.ID_EMPRESA.id()).getValue(),
                            (Integer) container.getContainerProperty(itemId, Col.ID_PROVEEDOR.id()).getValue(),
                            (Integer) container.getContainerProperty(itemId, Col.ID_NOMENCLATURA.id()).getValue(),
                            (String) container.getContainerProperty(itemId, Col.N5.id()).getValue(),
                            (Integer) container.getContainerProperty(itemId, Col.ID_EMPRESA_1.id()).getValue(),
                            (Integer) container.getContainerProperty(itemId, Col.ID_PROVEEDOR_1.id()).getValue(),
                            (Integer) container.getContainerProperty(itemId, Col.ID_NOMENCLATURA_1.id()).getValue(),
                            (String) container.getContainerProperty(itemId, Col.N5_1.id()).getValue()
                    );

                    empresaCuentaEquivalenteResumen.setModal(true);
                    UI.getCurrent().addWindow(empresaCuentaEquivalenteResumen);
                    empresaCuentaEquivalenteResumen.center();

                }
            }
        });

        cuentasEquivalentesGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (cuentasEquivalentesGrid.getSelectedRow() != null) {

                    if ((boolean) cuentasEquivalentesGrid.getContainerDataSource().getItem(cuentasEquivalentesGrid.getSelectedRow()).getItemProperty(INHABILITADA_PROPERTY).getValue()) {
                        inhabilitarBtn.setCaption("Inhabilitar");
                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_DOWN);
                    } else {
                        inhabilitarBtn.setCaption("Habilitar");
                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_UP);
                    }
                }
            }
        });

        Grid.HeaderRow filterRow = cuentasEquivalentesGrid.appendHeaderRow();
        for (Col col : Col.values()) {
            if (col != null) {
                Utileria.addTextFilter(filterRow, col.id(), container, 0);
            }
        }

        reportLayout.addComponent(cuentasEquivalentesGrid);
        reportLayout.setComponentAlignment(cuentasEquivalentesGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            try {
                if (cuentasEquivalentesGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    EmpresaCuentaEquivalenteForm cuentasForm = new EmpresaCuentaEquivalenteForm();
                    cuentasForm.idempresa = String.valueOf(container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), Col.ID_EMPRESA.id()).getValue());
                    cuentasForm.idempresa_1 = String.valueOf(container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), Col.ID_EMPRESA_1.id()).getValue());
                    cuentasForm.idnomenclatura = String.valueOf(container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), Col.ID_NOMENCLATURA.id()).getValue());
                    cuentasForm.idnomenclatura_1 = String.valueOf(container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), Col.ID_NOMENCLATURA_1.id()).getValue());
                    cuentasForm.seleccionarCampos();
                    UI.getCurrent().addWindow(cuentasForm);
                }
                cuentasEquivalentesGrid.select(null);
            } catch (Exception ex) {
                System.out.println("Error en el boton editar cuenta" + ex);
            }
        });

        Button newBtn = new Button("Nueva");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nueva cuenta contable.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    if (cuentasEquivalentesGrid.getSelectedRow() == null) {
                        EmpresaCuentaEquivalenteForm cuentasForm = new EmpresaCuentaEquivalenteForm();
                        cuentasForm.center();
                        UI.getCurrent().addWindow(cuentasForm);
                    } else if (cuentasEquivalentesGrid.getSelectedRow() != null){
                        EmpresaCuentaEquivalenteForm cuentasForm = new EmpresaCuentaEquivalenteForm();
                        cuentasForm.idempresa = String.valueOf(container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), Col.ID_EMPRESA.id()).getValue());
                        cuentasForm.idnomenclatura = String.valueOf(container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), Col.ID_NOMENCLATURA.id()).getValue());
                        cuentasForm.seleccionarCampos();
                        cuentasForm.center();
                        UI.getCurrent().addWindow(cuentasForm);
                    }

                } catch (Exception ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error en el boton nueva cuenta" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setDescription("Eliminar cuenta.");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (cuentasEquivalentesGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    queryString = " select * from contabilidad_partida";
                    queryString += " where IdNomenclatura = " + String.valueOf(container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), Col.ID_EMPRESA.id()).getValue());

                    try {

                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords = stQuery.executeQuery(queryString);

                        if (rsRecords.next()) {
                            Notification.show("La cuenta seleccionada contiene movimientos en partidas no se puede eliminar.", Notification.Type.ERROR_MESSAGE);
                        } else {

                            queryString = " delete from contabilidad_nomenclatura";
                            queryString += " where IdNomenclatura = " + String.valueOf(container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), Col.ID_EMPRESA.id()).getValue());
                            ;

                            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                            stQuery.executeUpdate(queryString);

                            Notification.show("Cuenta eliminada con exito!", Notification.Type.HUMANIZED_MESSAGE);

                            llenarTablaCuentas();

                        }
                    } catch (SQLException ex) {
                        System.out.println("Error al buscar registros en contabilidad_partida" + ex);
                    }

                }
            }
        });
        inhabilitarBtn = new Button("Inhabilitar");
        inhabilitarBtn.setIcon(FontAwesome.HAND_O_DOWN);
        inhabilitarBtn.setDescription("Eliminar cuenta.");
        inhabilitarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (cuentasEquivalentesGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    queryString = " UPDATE contabilidad_nomenclatura";
                    if (inhabilitarBtn.getCaption().equals("Habilitar")) {
                        queryString += " SET Estatus = 'HABILITADA'";
                        container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), INHABILITADA_PROPERTY).setValue("HABILITADA");
                        inhabilitarBtn.setCaption("Inhabilitar");
                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_DOWN);
                    } else {
                        queryString += " SET Estatus = 'INHABILITADA'";
                        container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), INHABILITADA_PROPERTY).setValue("INHABILITADA");
                        inhabilitarBtn.setCaption("Habilitar");
                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_UP);
                    }

                    queryString += " WHERE IdNomenclatura = " + String.valueOf(container.getContainerProperty(cuentasEquivalentesGrid.getSelectedRow(), Col.ID_EMPRESA.id()).getValue());

                    try {
                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);
                    } catch (SQLException ex) {
                        System.out.println("Error a Inabilitar cuenta contable " + ex);
                        ex.printStackTrace();
                    }
                }
            }
        });

        exportExcelBtn    = new Button("Exportar a Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
//        exportExcelBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_LINK);
        exportExcelBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if (cuentasEquivalentesGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(cuentasEquivalentesGrid);
                    ExcelExport excelExport = new ExcelExport(tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);
                    String fileexport;
                    fileexport = "NomenclaturaContable_.xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(eliminarBtn);
        buttonsLayout.addComponent(inhabilitarBtn);
        buttonsLayout.addComponent(exportExcelBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarTablaCuentas() {
        container.removeAllItems();

        queryString = "SELECT ce.IdEmpresa AS IdEmpresa, ce.NombreCorto AS NombreEmpresa,\n";
        queryString += "cn.IdNomenclatura AS IdNomenclatura, cn.N5 AS N5, cn.ID1, \n";
        queryString += "ce_1.IdEmpresa AS IdEmpresa_1, ce_1.NombreCorto AS NombreEmpresa_1,\n";
        queryString += "cn_1.IdNomenclatura AS IdNomenclatura_1, cn_1.N5 AS N5_1, ece.*\n";
        queryString += "FROM empresa_cuenta_equivalente ece \n";
        queryString += "INNER JOIN contabilidad_nomenclatura cn ON ece.Idnomenclatura = cn.IdNomenclatura  \n";
        queryString += "INNER JOIN contabilidad_empresa ce ON ce.IdEmpresa = ece.IdEmpresa \n";
        queryString += "INNER JOIN contabilidad_nomenclatura cn_1 ON ece.IdNomenclatura_1 = cn_1.IdNomenclatura  \n";
        queryString += "INNER JOIN contabilidad_empresa ce_1 ON ce_1.IdEmpresa = ece.IdEmpresa_1 \n";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, Col.ID_EMPRESA.id()).setValue(rsRecords.getInt("IdEmpresa"));
                    container.getContainerProperty(itemId, Col.ID_PROVEEDOR.id()).setValue(rsRecords.getInt("IdProveedor"));
                    container.getContainerProperty(itemId, Col.NOMBRE_EMPRESA.id()).setValue(rsRecords.getString("NombreEmpresa"));
                    container.getContainerProperty(itemId, Col.ID_NOMENCLATURA.id()).setValue(rsRecords.getInt("IdNomenclatura"));
                    container.getContainerProperty(itemId, Col.N5.id()).setValue(rsRecords.getString("N5"));
                    if (rsRecords.getInt("ID1") == 2){ // Si es pasivo en cambiar direccion de la flecha
                        String arrowHtml = com.vaadin.server.FontAwesome.ARROW_LEFT.getHtml();
                        arrowHtml = arrowHtml.replace(
                                "<span",
                                "<span style='color:red; font-family: FontAwesome;' "
                        );
                        container.getContainerProperty(itemId, ARROW_PROPERTY)
                                .setValue("<center>" + arrowHtml + "</center>");
                    }
                    container.getContainerProperty(itemId, Col.ID_EMPRESA_1.id()).setValue(rsRecords.getInt("IdEmpresa_1"));
                    container.getContainerProperty(itemId, Col.ID_PROVEEDOR_1.id()).setValue(rsRecords.getInt("IdProveedor_1"));
                    container.getContainerProperty(itemId, Col.NOMBRE_EMPRESA_1.id()).setValue(rsRecords.getString("NombreEmpresa_1"));
                    container.getContainerProperty(itemId, Col.ID_NOMENCLATURA_1.id()).setValue(rsRecords.getInt("IdNomenclatura_1"));
                    container.getContainerProperty(itemId, Col.N5_1.id()).setValue(rsRecords.getString("N5_1"));
                    container.getContainerProperty(itemId, INHABILITADA_PROPERTY).setValue(rsRecords.getBoolean("Inhabilitado"));


                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla nomenclatura contable en CuentasContablesView:" + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Cuentas contables");
    }

}
