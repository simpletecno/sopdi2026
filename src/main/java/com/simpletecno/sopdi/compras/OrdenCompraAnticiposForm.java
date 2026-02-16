package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * @author user
 */
public class OrdenCompraAnticiposForm extends Window {

    VerticalLayout mainLayout;

    Grid ordenCompraGrid;
    public IndexedContainer ordenCompraContainer = new IndexedContainer();
    Grid.FooterRow footerRow;

    static final String ID_PROPERTY = "Id";
    static final String NOC_PROPERTY = "NOC";
    static final String TIPO_PROPERTY = "Tipo";
    static final String EMPRESA_PROPERTY = "Empresa";
    static final String PROYECTO_PROPERTY = "Proyecto";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String TOTAL_PROPERTY = "Total";
    static final String RESPONSABLE_PROPERTY = "Responsable";
//    static final String DIRECCIONENTREGA_PROPERTY = "Direccion";
    static final String ANTICIPO_PROPERTY = "Anticipo";
    static final String RAZON_PROPERTY = "Razon";
    static final String ESTADO_PROPERTY = "Estado";
    static final String CREADOFECHAHORA_PROPERTY = "Creada Fecha";
    static final String CREADOUSUARIO_PROPERTY = "Usuario";
    static final String ANTICIPOSF_PROPERTY = "ASF";
    static final String IDPROVEEDOR_PROPERTY = "IdProveedor";
    static final String PROCESADO_PROPERTY = "Procesado";

    UI mainUI;
    Statement stQuery, stQuery2;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords, rsRecords2;
    String queryString;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    Button salirBtn;
    Button autorizarBtn;

    ComboBox empresaCbx;

    public OrdenCompraAnticiposForm() {
        this.mainUI = UI.getCurrent();
        setWidth("85%");
        setHeight("70%");

        mainLayout = new VerticalLayout();
        mainLayout.setSizeUndefined();
        mainLayout.setWidth("100%");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        setContent(mainLayout);

        Label titleLbl = new Label("ANTICIPOS POR AUTORIZAR -- DESDE ORDENES DE COMPRA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        crearButtonEmpresa();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setMargin(new MarginInfo(false, true, false, false));
        titleLayout.setWidth("100%");

        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaOrdenCompra();

        llenarTablaOrdenCompra();

    }

    public void crearTablaOrdenCompra() {

        ordenCompraContainer.addContainerProperty(PROCESADO_PROPERTY, String.class, "NO");
        ordenCompraContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(NOC_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(TIPO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(EMPRESA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(PROYECTO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(TOTAL_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(RESPONSABLE_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(ANTICIPO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(RAZON_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(ESTADO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(CREADOFECHAHORA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(CREADOUSUARIO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(ANTICIPOSF_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(IDPROVEEDOR_PROPERTY, String.class, null);

        ordenCompraGrid = new Grid("Anticipos de ordenes de compra", ordenCompraContainer);
        ordenCompraGrid.setSizeFull();
        ordenCompraGrid.setImmediate(true);
        ordenCompraGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        ordenCompraGrid.setDescription("Seleccione uno o varios registros.");
        ordenCompraGrid.setHeightMode(HeightMode.ROW);
        ordenCompraGrid.setHeightByRows(10);
        ordenCompraGrid.setResponsive(true);
        ordenCompraGrid.setEditorBuffered(false);
        ordenCompraGrid.setColumnReorderingAllowed(true);
//        ordenCompraGrid.setColumns(ID_PROPERTY, NOC_PROPERTY, TIPO_PROPERTY, EMPRESA_PROPERTY, PROYECTO_PROPERTY, PROVEEDOR_PROPERTY, FECHA_PROPERTY, MONEDA_PROPERTY, TOTAL_PROPERTY, RESPONSABLE_PROPERTY, ANTICIPO_PROPERTY, RAZON_PROPERTY, ESTADO_PROPERTY, PARTIDA_PROPERTY, CREADOFECHAHORA_PROPERTY, CREADOUSUARIO_PROPERTY);
        ordenCompraGrid.setFooterVisible(true);

        ordenCompraGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(EMPRESA_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(PROYECTO_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(CREADOFECHAHORA_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(CREADOUSUARIO_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(ANTICIPOSF_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(IDPROVEEDOR_PROPERTY).setHidable(true).setHidden(true);

        ordenCompraGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (TOTAL_PROPERTY.equals(cellReference.getPropertyId())
                || PROCESADO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (ANTICIPO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });
        ordenCompraGrid.setRowStyleGenerator(line -> {
            String valor = String.valueOf(line.getItem().getItemProperty(PROCESADO_PROPERTY).getValue());
            if(!valor.trim().isEmpty()) {
                if (String.valueOf(line.getItem().getItemProperty(PROCESADO_PROPERTY).getValue()).equals("SI") ) {
                    return "red";
                }
                else {
                    return "green";
                }
            }
            return null;
        });

        ordenCompraGrid.addSelectionListener((SelectionEvent.SelectionListener) event -> {

            footerRow.getCell(TOTAL_PROPERTY).setText("0.00");

            if (ordenCompraGrid.getSelectedRows() != null) {
                Object gridItem;
                Iterator iter = event.getSelected().iterator();

                if (iter == null) {
                    return;
                }
                if (!iter.hasNext()) {
                    return;
                }

                double total = 0.00;

                do {
                    gridItem = iter.next();

                    total = total + Double.parseDouble(String.valueOf(ordenCompraContainer.getContainerProperty(gridItem, ANTICIPOSF_PROPERTY).getValue()));
                } while (iter.hasNext());
                footerRow.getCell(ANTICIPO_PROPERTY).setText(numberFormat.format(total));
            }
        });

        footerRow = ordenCompraGrid.appendFooterRow();
        footerRow.getCell(RESPONSABLE_PROPERTY).setText("Total");
        footerRow.getCell(ANTICIPO_PROPERTY).setText("0.00");

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        autorizarBtn = new Button("Autorizar anticipos");
        autorizarBtn.setIcon(FontAwesome.CHECK);
        //autorizarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        autorizarBtn.setDescription("AUTORIZAR ANTICIPOS.");
        autorizarBtn.addClickListener((Button.ClickListener) event -> ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de AUTORIZAR los anticipos de estas ordenes de compra?",
                "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {

                            if(ordenCompraGrid.getSelectedRows().isEmpty()) {
                                Notification.show("Seleccione al menos un anticipo a autorizar.", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            int contador = 0;

                            try {
                                for (Object itemId : ordenCompraGrid.getSelectedRows()) {
                                    if(String.valueOf(ordenCompraContainer.getContainerProperty(itemId, PROCESADO_PROPERTY).getValue()).equals("NO")) {
                                        queryString = "INSERT INTO autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
                                        queryString += " Fecha, Moneda, Monto, CodigoCC, CuentaContableLiquidar, ";
                                        queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora, IdOrdenCompra)";
                                        queryString += " VALUES ";
                                        queryString += "(";
                                        queryString += "'ANTICIPO A PROVEEDOR'";
                                        queryString += "," + String.valueOf(empresaCbx.getValue());
                                        queryString += "," + ordenCompraContainer.getContainerProperty(itemId, IDPROVEEDOR_PROPERTY).getValue();
                                        queryString += ",current_date";
                                        queryString += ",'" + ordenCompraContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue() + "'";
                                        queryString += "," + ordenCompraContainer.getContainerProperty(itemId, ANTICIPOSF_PROPERTY).getValue();
                                        queryString += ",'" + "TEMP_" + Math.random() * 1000 + "'"; // codigoCC
                                        queryString += ",''"; // cuentacontableliquidar
                                        queryString += ",'ANTICIPO A PROVEEDOR'";
                                        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                                        queryString += ",current_timestamp";
                                        queryString += "," + ordenCompraContainer.getContainerProperty(itemId, ID_PROPERTY).getValue();
                                        queryString += ")";

                                        System.out.println(queryString);

                                        stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                                        stPreparedQuery.executeUpdate();
                                        rsRecords = stPreparedQuery.getGeneratedKeys();

                                        ordenCompraContainer.getContainerProperty(itemId, PROCESADO_PROPERTY).setValue("SI");

                                        contador ++;

                                    }
                                    rsRecords.next();
                                }//endfor

                                ordenCompraGrid.getSelectedRows().forEach(itemId -> {
                                    ordenCompraGrid.deselect(itemId);
                                });

//                                for (Object itemId : ordenCompraGrid.getSelectedRows()) {
//                                    ordenCompraContainer.removeItem(itemId);
//                                }//endfor
//
                                Notification notif = new Notification("[" + contador + "] Anticipos autorizaos exitosamente!.", Notification.Type.TRAY_NOTIFICATION);
                                notif.setDelayMsec(1500);
                                notif.setPosition(Position.MIDDLE_CENTER);
                                notif.setIcon(FontAwesome.CHECK);
                                notif.show(Page.getCurrent());

                            } catch (Exception e) {
                                System.out.println("Error el intentar autorizar anticipos.");
                                e.printStackTrace();
                                Notification.show("Error al intentar autorizar anticipos.", Notification.Type.ERROR_MESSAGE);
                            }
                        } else {
                            Notification.show("Operación cacelada!", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }));

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");

        buttonsLayout.addComponents(salirBtn, autorizarBtn);
        buttonsLayout.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);

        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setSpacing(true);
        gridLayout.setWidth("100%");
        gridLayout.setMargin(true);
        gridLayout.addComponents(ordenCompraGrid, buttonsLayout);
        gridLayout.setComponentAlignment(ordenCompraGrid, Alignment.MIDDLE_CENTER);
        gridLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
        gridLayout.addStyleName("rcorners3");

        mainLayout.addComponent(gridLayout);
        mainLayout.setComponentAlignment(gridLayout, Alignment.TOP_CENTER);

    }

    public void llenarTablaOrdenCompra() {

        ordenCompraContainer.removeAllItems();

        try {

            queryString = " SELECT *, empresa.Nombre as EmpresaNombre, ";
            queryString += " proveedor.Nombre AS ProveedorNombre, tipo_orden_compra.Descripcion As TipoOrdenCompra ";
            queryString += " FROM orden_compra";
            queryString += " LEFT JOIN empresa ON orden_compra.IdEmpresa = empresa.IdEmpresa";
            queryString += " LEFT JOIN proveedor ON orden_compra.IdProveedor = proveedor.IdProveedor";
            queryString += " LEFT JOIN tipo_orden_compra ON orden_compra.IdTipoOrdenCompra = tipo_orden_compra.Id";
            queryString += " WHERE orden_compra.CodigoCCAnticipo = '' AND orden_compra.CodigoCCDocumento = ''";
            queryString += " AND   orden_compra.IdEmpresa =" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    Object itemId = ordenCompraContainer.addItem();
                    ordenCompraContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                    ordenCompraContainer.getContainerProperty(itemId, NOC_PROPERTY).setValue(rsRecords.getString("NOC"));
                    ordenCompraContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("TipoOrdenCompra"));
                    ordenCompraContainer.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("EmpresaNombre"));
//                    ordenCompraContainer.getContainerProperty(itemId, PROYECTO_PROPERTY).setValue(rsRecords.getString("ProjectNom"));
                    ordenCompraContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("ProveedorNombre"));
                    ordenCompraContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    ordenCompraContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                    ordenCompraContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Total")));
                    ordenCompraContainer.getContainerProperty(itemId, RESPONSABLE_PROPERTY).setValue(rsRecords.getString("Responsable"));
                    ordenCompraContainer.getContainerProperty(itemId, ANTICIPO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Anticipo")));
                    ordenCompraContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));
                    ordenCompraContainer.getContainerProperty(itemId, ESTADO_PROPERTY).setValue(rsRecords.getString("Estado"));
                    ordenCompraContainer.getContainerProperty(itemId, CREADOFECHAHORA_PROPERTY).setValue(rsRecords.getString("CreadoFechaYHora"));
                    ordenCompraContainer.getContainerProperty(itemId, CREADOUSUARIO_PROPERTY).setValue(rsRecords.getString("CreadoUsuario"));
                    ordenCompraContainer.getContainerProperty(itemId, ANTICIPOSF_PROPERTY).setValue(rsRecords.getString("Anticipo"));
                    ordenCompraContainer.getContainerProperty(itemId, IDPROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));

                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla anticipos de oden de compra " + ex);
            ex.printStackTrace();
            Notification.show("Error al listar tabla anticipos de orden de compra.", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void crearButtonEmpresa() {

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);

        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }
            rsRecords.first();

            empresaCbx.select(rsRecords.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }
}
