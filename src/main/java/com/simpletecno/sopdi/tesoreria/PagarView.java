package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.contabilidad.MostrarPartidaContable;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import javax.mail.MessagingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class PagarView extends VerticalLayout implements View {

    ComboBox empresaCbx;
    String empresa;

    UI mainUI;

    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public IndexedContainer documentosContainer = new IndexedContainer();
    Grid documentosGrid;
    Button refreshBtn;
    
    DateField fechaDt;

    static final String ID_AUTO_PROPERTY = "ID AUTO.";
    static final String FECHA_PROPERTY = "Fecha";
    static final String TIPO_PROPERTY = "Tipo";
    static final String ID_PROVEEDOR_PROPERTY = "Id Proveedor";
    static final String NOMBRE_PROVEEDOR_PROPERTY = "Proveedor";
    static final String VALOR_PROPERTY = "Valor";
    static final String CODIGOCC_PROPERTY = "CODIGOCC";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String ORIGEN_PROPERTY = "Origen";
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo Partida";
    static final String CUENTA_LIQUIDAR_PROPERTY = "CUENTA LIQUIDAR";

    public static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    public PagarView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);
        // mainUI.setTheme("myTheme");

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);

        llenarComboEmpresa();

        Label titleLbl = new Label("PAGAR");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");
        
        fechaDt = new DateField("Seleccione una fecha de pago:");
        fechaDt.setDateFormat("dd/MM/yyyy");
        //Date primerDia = Utileria.getPrimerDiaDelMes();
        //fechaDt.setValue(primerDia);
        fechaDt.setWidth("14em");
        fechaDt.addValueChangeListener((event) -> {
            llenarTablaAutorizaciones();
        });
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);
        titleLayout.addComponents(empresaCbx, titleLbl,  fechaDt);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.BOTTOM_CENTER);
        titleLayout.setComponentAlignment(fechaDt, Alignment.BOTTOM_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        empresa = String.valueOf(empresaCbx.getValue());

        crearGridDocumentos();

        refreshBtn = new Button("Refrescar");
        refreshBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        refreshBtn.setIcon(FontAwesome.REFRESH);
        refreshBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarTablaAutorizaciones();
            }
        });

        addComponent(refreshBtn);
        setComponentAlignment(refreshBtn, Alignment.BOTTOM_CENTER);

        HorizontalLayout instruccionesLayout = new HorizontalLayout();
        instruccionesLayout.setMargin(true);
        instruccionesLayout.setSpacing(true);

        Label instruccionesLbl = new Label(
                "SELECCIONE UNA FECHA DE PAGO, DESPUES UN REGISTRO PARA QUE EL SISTEMA PRESENTE EL FORMULARIO DEL PAGO\n" +
                        "<ul>"+
                        "  <li><b>SI EL PAGO NO AMERITA CHEQUE, VALOR  = 0.00</b></li>"+
                        "  <li>EL SISTEMA CREA LA PARTIDA AUTOMATICAMENTE\n"+
                        "  LIQUIDANDO EL DOCUMENTO Y LOS ANTICIPOS RELACIONADOS..</li>"+
                        "</ul> "+
                        "DESPUES DE HACER EL PAGO, PUEDE VERIFICAR EL LIBRO DIARIO PARA COMPROBACION.",
                ContentMode.HTML);
        instruccionesLbl.addStyleName(ValoTheme.LABEL_COLORED);
        instruccionesLayout.addComponent(instruccionesLbl);

        addComponent(instruccionesLayout);
        setComponentAlignment(instruccionesLayout, Alignment.BOTTOM_CENTER);

        llenarTablaAutorizaciones();
    }

    public void llenarComboEmpresa() {
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

    public void crearGridDocumentos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("96%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        documentosContainer.addContainerProperty(ID_AUTO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(TIPO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(NOMBRE_PROVEEDOR_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(VALOR_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(ORIGEN_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, "");
        documentosContainer.addContainerProperty(CUENTA_LIQUIDAR_PROPERTY, String.class, "");

        documentosGrid = new Grid("Autorizado para pagar. ", documentosContainer);

        documentosGrid.setImmediate(true);
        documentosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        documentosGrid.setDescription("Seleccione un registro.");
        documentosGrid.setHeightMode(HeightMode.ROW);
        documentosGrid.setHeightByRows(10);
//        documentosGrid.setHeight("100%");
        documentosGrid.setWidth("100%");
        documentosGrid.setResponsive(true);
        documentosGrid.setEditorBuffered(false);
        documentosGrid.setEditorEnabled(false);
        documentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        documentosGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (documentosGrid.getSelectedRows() != null) {
                    if (documentosGrid.getSelectedRow() != null) {
                        if(fechaDt.getValue() != null){
                            validarFormulario();
                        }else{
                            Notification.show("PARA REALIZAR UN PAGO DEBE SELECCIONAR UNA FECHA DE PAGO", Notification.Type.WARNING_MESSAGE);
                            fechaDt.focus();
                        }
                    }
                }
            }
        });

        documentosGrid.getColumn(ID_AUTO_PROPERTY).setHidable(true).setHidden(true);

        reportLayout.addComponent(documentosGrid);
        reportLayout.setComponentAlignment(documentosGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.BOTTOM_CENTER);
    }

    public void insertarPartidaLiquidarDocumento() {
        // insertar partida  proveedoresLoclaes y anticiposAProveedores

        String codigoCC = String.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGOCC_PROPERTY).getValue()));
        String codigoPartida = String.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()));
        String idProveedorCliente = String.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue()));
        String nombreProveedor = String.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), NOMBRE_PROVEEDOR_PROPERTY).getValue()));
        String moneda = String.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(),MONEDA_PROPERTY).getValue()));

        double montoPagado = 0.00;
        double montoPagadoQ = 0.00;

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()); //yyy/mm/yyyy
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartidaPago = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "3";

        queryString = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + codigoPartidaPago + "%'";
        queryString += " order by codigoPartida desc ";

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

//System.out.println("ultimo encontrado " + ultimoEncontado);
                codigoPartidaPago += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartidaPago += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        String descripcion = "PAGO DE DOCUMENTO CON ANTICIPOS";

        // query para obtener el  o los anticipos relacionados, solo para hacer un ciclo while de la cantidad de anticipos...
        queryString = " SELECT autorizacion_pago.* ";
        queryString += ",contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento";
        queryString += ",contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales,contabilidad_partida.TipoCambio ";
        queryString += " FROM autorizacion_pago";
        queryString += " INNER JOIN contabilidad_partida ON contabilidad_partida.CodigoCC = autorizacion_pago.CodigoCCRelacionado";
        queryString += " WHERE autorizacion_pago.idProveedor = " + idProveedorCliente;
        queryString += " AND autorizacion_pago.IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND autorizacion_pago.CodigoCC = '" + codigoCC + "'";
        queryString += " AND autorizacion_pago.CodigoCCRelacionado <> ''";
        queryString += " AND contabilidad_partida.idProveedor = " + idProveedorCliente;
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaCbx.getValue();
//        queryString += " AND contabilidad_partida.IdNomenclatura In (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAbastos() + ")"; //SOLO LA LINEA DE FACTURA O ABASTO
        queryString += " AND contabilidad_partida.IdNomenclatura = " +  ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
        queryString += " AND contabilidad_partida.Debe > 0"; //JA 2023-04-20

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR ANTICIPOS PARA LIQUIDAR DOCUMENTO : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            //por cada anticipo relacionado

            if (rsRecords.next()) {

                descripcion += " " + nombreProveedor + " " + rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento");

                queryString  = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
                queryString += " TipoDocumento, Fecha, idProveedor, NITProveedor, ";
                queryString += " NombreProveedor, NombreCheque, MontoDocumento, SerieDocumento, NumeroDocumento, ";
                queryString += " IdNomenclatura, MonedaDocumento, Debe, Haber,";
                queryString += " DebeQuetzales, HaberQuetzales, TipoCambio,";
                queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
                queryString += " Values ";

                do { // PUEDEN HABER VARIOS ANTICIPOS PARA PAGAR ESTE DOCUMENTO

                    queryString += " (";
                    queryString += String.valueOf(empresaCbx.getValue());
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartidaPago + "'";
                    queryString += ",'" + rsRecords.getString("CodigoCCRelacionado") + "'";
                    queryString += ",'PAGO DOCUMENTO'";
                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                    queryString += "," + idProveedorCliente;
                    queryString += ",''"; //nit proveedor
                    queryString += ",'" + nombreProveedor + "'";
                    queryString += ",''"; // no hay documento
                    queryString += ", (SELECT A.MontoDocumento FROM contabilidad_partida A Where A.CodigoPartida = '" + codigoPartida + "' And A.IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAbastos() + ") LIMIT 1) ";
                    queryString += ",''"; //serie documento
                    queryString += ",'0'";
                    queryString += ", " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
                    queryString += ",'" + moneda + "'";
                    queryString += ",0.00"; //debe
                    queryString += "," + rsRecords.getString("Monto"); //monto autorizado rebajar al anticipo
                    queryString += ",0.00"; //debeQ
                    queryString += "," + (rsRecords.getDouble("Monto") * rsRecords.getDouble("TipoCambio")); //tipo de cambio del anticipo
                    queryString += ",1.0";
                    queryString += ",'" + descripcion +  "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += "),";

                    montoPagado += rsRecords.getDouble("Monto");
                    montoPagadoQ += (rsRecords.getDouble("Monto") * rsRecords.getDouble("TipoCambio"));

                } while (rsRecords.next()); //FIN DE ANTICIPOS

                //INSERTANDO LINEA DE FACTURA O ABASTO
                queryString += " (";
                queryString += String.valueOf(empresaCbx.getValue());
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartidaPago + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += ",'PAGO DOCUMENTO'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + idProveedorCliente;
                queryString += ",''"; //nit proveedor
                queryString += ",'" + nombreProveedor + "'";
                queryString += ",''"; // no hay documento
                queryString += ", (SELECT A.MontoDocumento FROM contabilidad_partida A Where A.CodigoPartida = '" + codigoPartida + "' And A.IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAbastos() + ") LIMIT 1) ";
                queryString += ",''"; //serie documento
                queryString += ",'0'";

//                queryString += ",76"; // proveedores locales

                if(codigoCC.equals(String.valueOf(empresaCbx.getValue()) + "20210401000")) {
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAbastos(); // abastos
                }
                else {
                    queryString += ",(SELECT A.IdNomenclatura FROM contabilidad_partida A Where A.CodigoPartida = '" + codigoPartida + "' AND A.IdNomenclatura In (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAbastos() + ") LIMIT 1) ";
                }

                queryString += ",'" + moneda + "'";
//                queryString += ", (SELECT A.Haber FROM contabilidad_partida A Where A.CodigoPartida = '" + codigoPartida + "' And A.IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAbastos() + ") LIMIT 1) ";
                queryString += ", " + montoPagado;
                queryString += ",0.00"; // haber
//                queryString += ", (SELECT A.HaberQuetzales FROM contabilidad_partida A Where A.CodigoPartida = '" + codigoPartida + "' And A.IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAbastos() + ") LIMIT 1) ";
                queryString += ", " + montoPagadoQ;
                queryString += ",0.00"; //haberq
                queryString += ",1.0";
                queryString += ",'" + descripcion + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ")";

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Query Liquidar Documento Anticipo : " + queryString);

                stQuery.executeUpdate(queryString);

                queryString = " Update contabilidad_partida Set ";
                queryString += " MontoAutorizadoPagar = 0.00";
                queryString += ", MontoAplicarAnticipo = 0.00";
                queryString += ",Estatus = 'PAGADO'";
                queryString += " ,Referencia = '" + codigoPartidaPago + "'";//codigo de la partida del CHEQUE
                queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";  //CODIGO DE LA PARTIDA DEL DOCUMENTO

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Query Liquidar Documento Anticipo : " + queryString);

                stQuery.executeUpdate(queryString);

                //diferencial cambiariario

                if(moneda.equals("DOLARES")) {
                    queryString = " SELECT SUM(DebeQuetzales) TotalDebeQ, SUM(HaberQuetzales) TotalHaberQ FROM contabilidad_partida ";
                    queryString += " WHERE codigoPartida = '" + codigoPartidaPago + "'";

                    rsRecords = stQuery.executeQuery(queryString);

                    if (rsRecords.next()) { //  encontrado
                        if (rsRecords.getDouble("TotalDebeQ") != rsRecords.getDouble("TotalHaberQ")) { //si hay diferencial cambiario
                            queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
                            queryString += " TipoDocumento, Fecha, idProveedor, NITProveedor, ";
                            queryString += " NombreProveedor, NombreCheque, MontoDocumento, SerieDocumento, NumeroDocumento, ";
                            queryString += " IdNomenclatura, MonedaDocumento, Debe, Haber,";
                            queryString += " DebeQuetzales, HaberQuetzales, TipoCambio,";
                            queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
                            queryString += " Values ";

                            queryString += " (";
                            queryString += String.valueOf(empresaCbx.getValue());
                            queryString += ",'INGRESADO'";
                            queryString += ",'" + codigoPartidaPago + "'";
                            queryString += ",''";
                            queryString += ",'PAGO DOCUMENTO'";
                            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                            queryString += "," + idProveedorCliente;
                            queryString += ",''"; //nit proveedor
                            queryString += ",'" + nombreProveedor + "'";
                            queryString += ",''"; // no hay documento
                            queryString += ", (SELECT A.MontoDocumento FROM contabilidad_partida A Where A.CodigoPartida = '" + codigoPartida + "' And A.IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAbastos() + ") LIMIT 1) ";
                            queryString += ",''"; //serie documento
                            queryString += ",'0'";
                            queryString += ", " + ((SopdiUI) mainUI).cuentasContablesDefault.getDiferencialCambiario();
                            queryString += ",'" + moneda + "'";
                            if (rsRecords.getDouble("TotalDebeQ") > rsRecords.getDouble("TotalHaberQ")) {
                                queryString += ",0.00"; //debe
                                queryString += ",0.00"; //haber
                                queryString += ",0.00"; //debe Q
                                queryString += "," + (rsRecords.getDouble("TotalDebeQ") - rsRecords.getDouble("TotalHaberQ"));
                            } else {
                                queryString += ",0.00"; //debe
                                queryString += ",0.00"; //haber
                                queryString += "," + (rsRecords.getDouble("TotalHaberQ") - rsRecords.getDouble("TotalDebeQ"));
                                queryString += ",0.00"; //haber Q
                            }
                            queryString += ",1.0";
                            queryString += ",'" + descripcion + "'";
                            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                            queryString += ",current_timestamp";
                            queryString += ")";

                            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Query Liquidar Documento Anticipo DIFERENCIAL CAMBIARIO  : " + queryString);

                            stQuery.executeUpdate(queryString);
                        }
                    }
                } //ENDIF DOLARES
//                Notification.show("DOCUMENTO LIQUIDADO EXITOSAMENTE", Notification.Type.HUMANIZED_MESSAGE);
            } // END DOCUMENTOS (UN SOLO DOCUMENTO)
            else {
                Notification.show("ERROR : NO SE ENCONTRARON DOCUMENTOS PARA PAGAR ", Notification.Type.ERROR_MESSAGE);
                return;
            }

            // borrar autorización

            queryString = " DELETE FROM autorizacion_pago";
            queryString += " WHERE CodigoCC = '" + documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGOCC_PROPERTY).getValue() + "'";

            stQuery.executeUpdate(queryString);

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            documentosContainer.removeItem(documentosGrid.getSelectedRow());

            Notification notif = new Notification("DOCUMENTO LLIQUIDADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            MostrarPartidaContable mostrarPartidaContable =
                    new MostrarPartidaContable(codigoPartidaPago, nombreProveedor, descripcion, codigoCC);
            mainUI.addWindow(mostrarPartidaContable);
            mostrarPartidaContable.center();

        } catch (Exception ex) {
            System.out.println("Error al PROCESAR PAGO CON ANTICIPOS de autorizacion_pago :" + ex);
            ex.printStackTrace();

            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex1) {
                Logger.getLogger(PagarView.class.getName()).log(Level.SEVERE, null, ex1);
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "ERROR EN Query Liquidar Documento Anticipo : " + queryString);

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(PagarView.class.getName()).log(Level.SEVERE, null, ex2);
            }

        }

    }

    public void validarFormulario() {

        if (documentosGrid.getSelectedRow() != null) {
            String ORIGEN = String.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), ORIGEN_PROPERTY).getValue()));
            String idAutorizacion = String.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), ID_AUTO_PROPERTY).getValue()));
            String idProveedorCliente = String.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue()));
            String cuentaLiquidar = String.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), CUENTA_LIQUIDAR_PROPERTY).getValue()));
            Double monto = Double.valueOf(String.valueOf(documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), VALOR_PROPERTY).getValue()).replaceAll(",", ""));
            Date fechaPago = fechaDt.getValue();

            System.out.println("ORIGEN : " + ORIGEN);

            if (ORIGEN.equals(AutorizacionesPagoView.TRASLADO_EMP_REL)) {
                PagoAnticipoEmpresaRelForm empresaRelacionada = new PagoAnticipoEmpresaRelForm(idAutorizacion, fechaPago);
                mainUI.addWindow(empresaRelacionada);
                empresaRelacionada.center();

            } else if (ORIGEN.equals(AutorizacionesPagoView.ANTICIPO_PROVEEDOR)) {
                PagoAnticipoProveedorForm pagoAnticipoProveedorForm = new PagoAnticipoProveedorForm(AutorizacionesPagoView.ANTICIPO_PROVEEDOR, fechaPago);
                mainUI.addWindow(pagoAnticipoProveedorForm);
                pagoAnticipoProveedorForm.center();

            } else if (ORIGEN.equals(AutorizacionesPagoView.ANTICIPO_HONORARIOS)) {
                PagoAnticipoProveedorForm pagoAnticipoProveedorForm = new PagoAnticipoProveedorForm(AutorizacionesPagoView.ANTICIPO_HONORARIOS, fechaPago);
                mainUI.addWindow(pagoAnticipoProveedorForm);
                pagoAnticipoProveedorForm.center();

            } else if (ORIGEN.equals(AutorizacionesPagoView.ANTICIPO_SUELDOS)) {
                PagoAnticipoProveedorForm pagoAnticipoProveedorForm = new PagoAnticipoProveedorForm(AutorizacionesPagoView.ANTICIPO_SUELDOS, fechaPago);
                mainUI.addWindow(pagoAnticipoProveedorForm);
                pagoAnticipoProveedorForm.center();

            } else if (ORIGEN.equals(AutorizacionesPagoView.DEVOLUCION_CLIENTE)) {
                String perfil = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfile();

//20231011                if (perfil.equals("ADMINISTRADOR")) {
                    PagoDevolucionEngancheForm pagoDevolucionEngancheForm = new PagoDevolucionEngancheForm(idProveedorCliente, fechaPago);
                    mainUI.addWindow(pagoDevolucionEngancheForm);
                    pagoDevolucionEngancheForm.center();
//                }
//                else {
//                    Notification.show("SOLAMENTE EL ADMNISTRADOR TIENE PERMISO PARA ESTA ACCION...", Notification.Type.WARNING_MESSAGE);
//                }

                if(perfil.equals("TODOS")) {
                    //
                }

            } else if (ORIGEN.equals(AutorizacionesPagoView.VENTA_MONEDA)) {
                PagoCompraMonedaForm pagoCompraMonedaForm = new PagoCompraMonedaForm(idAutorizacion, fechaPago);
                mainUI.addWindow(pagoCompraMonedaForm);
                pagoCompraMonedaForm.center();

            } else if (ORIGEN.equals(AutorizacionesPagoView.PAGO_DOCUMENTO)) {
                if(monto.doubleValue() == 0.00) {
                    insertarPartidaLiquidarDocumento();
                }
                else {
                    PagoFacturaProveedorForm pagoFactura = new PagoFacturaProveedorForm(idProveedorCliente, fechaPago);
                    mainUI.addWindow(pagoFactura);
                    pagoFactura.center();
                }
            } else if (ORIGEN.equals(AutorizacionesPagoView.PAGO_LIQUIDACION)) {
                PagoLiquidacionForm pagoLiquidacion = new PagoLiquidacionForm(idProveedorCliente, fechaPago);
                mainUI.addWindow(pagoLiquidacion);
                pagoLiquidacion.center();
            } else if (ORIGEN.equals(AutorizacionesPagoView.PAGO_PLANILLA)) {
                PagoPlanillaForm pagoPlanilla = new PagoPlanillaForm(idProveedorCliente, fechaPago);
                mainUI.addWindow(pagoPlanilla);
                pagoPlanilla.center();
            } else if (ORIGEN.equals(AutorizacionesPagoView.PAGO_PRESTAMO)) {
                PagoPrestamoForm pagoPrestamoForm = new PagoPrestamoForm(idAutorizacion,cuentaLiquidar, fechaPago);
                mainUI.addWindow(pagoPrestamoForm);
                pagoPrestamoForm.center();
            }
        }
    }

    public void llenarTablaAutorizaciones() {

        documentosContainer.removeAllItems();

        queryString = " SELECT autorizacion_pago.*, proveedor.Nombre";
        queryString += " FROM autorizacion_pago ";
        queryString += " INNER JOIN  proveedor ON autorizacion_pago.idProveedor = proveedor.idProveedor";
        queryString += " AND autorizacion_pago.IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " AND (autorizacion_pago.CodigoCCRelacionado = '' || autorizacion_pago.CodigoCCRelacionado = '0')";
        queryString += " GROUP BY CodigoCC, idProveedor";
        queryString += " Order By autorizacion_pago.idProveedor, autorizacion_pago.Fecha ASC";

//System.out.println("Query llenarAutorizacion" + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) { //  encontrado
                do {
                    Object itemId = documentosContainer.addItem();

                    documentosContainer.getContainerProperty(itemId, ID_AUTO_PROPERTY).setValue(rsRecords.getString("IdAutorizacion"));
                    documentosContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(rsRecords.getString("Fecha"));
                    documentosContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("TipoAutorizacion"));
                    documentosContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("idProveedor"));
                    documentosContainer.getContainerProperty(itemId, NOMBRE_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    documentosContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    documentosContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Monto")));
                    documentosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                    documentosContainer.getContainerProperty(itemId, ORIGEN_PROPERTY).setValue(rsRecords.getString("TipoAutorizacion"));
                    documentosContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    documentosContainer.getContainerProperty(itemId, CUENTA_LIQUIDAR_PROPERTY).setValue(rsRecords.getString("CuentaContableLiquidar"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al leer registros de autorizaciones de pago : " + ex);
            ex.printStackTrace();

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(PagarView.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Pagar");
    }
}