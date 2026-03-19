package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author user
 */
public class LibroMayorView extends VerticalLayout implements View {

    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String FECHA_PROPERTY = "Fecha";
    static final String DOCTO_PROPERTY = "Docto";
    static final String TIPO_PROPERTY = "Tipo";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String CODIGO_PARTIDA_PROPERTY = "Partida";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String SALDO_PROPERTY = "Saldo";

    Grid libroMayorGrid;

    public IndexedContainer libroMayorcontainer = new IndexedContainer();

    String fileexport;
    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords1;
    Statement stQuery2;
    ResultSet rsRecords2;

    Button consultarBtn;
    Button exportExcelBtn;
    ComboBox anioCbx;
    ListSelect mesCbx;
    ComboBox cuentaContableCbx;

    NumberField folioTxt;

    static DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public LibroMayorView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);
        setHeightUndefined();

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " LIBRO MAYOR");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaLibroMayor();
    }

    public void crearTablaLibroMayor() {
        VerticalLayout layoutTablaLibroMayor = new VerticalLayout();
        layoutTablaLibroMayor.setWidth("100%");
        layoutTablaLibroMayor.setSpacing(true);
        layoutTablaLibroMayor.addStyleName("rcorners3");

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setMargin(new MarginInfo(false, true, false, true));
        
        HorizontalLayout layoutButtons = new HorizontalLayout();               
        layoutButtons.setMargin(false);
        layoutButtons.setSpacing(true);

        anioCbx = new ComboBox("Año:");
        anioCbx.setWidth("80px");
        Calendar todayCal = Calendar.getInstance();
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR)-5));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR)-4));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR)-3));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR)-2));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR)-1));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR)));
        anioCbx.select(String.valueOf(todayCal.get(Calendar.YEAR)));

        mesCbx = new ListSelect("Mes:");
                
        mesCbx.addItem("01");
        mesCbx.setItemCaption("01","Enero");
        mesCbx.addItem("02");
        mesCbx.setItemCaption("02","Febrero");
        mesCbx.addItem("03");
        mesCbx.setItemCaption("03","Marzo");
        mesCbx.addItem("04");
        mesCbx.setItemCaption("04","Abril");
        mesCbx.addItem("05");
        mesCbx.setItemCaption("05","Mayo");
        mesCbx.addItem("06");
        mesCbx.setItemCaption("06","Junio");
        mesCbx.addItem("07");
        mesCbx.setItemCaption("07","Julio");
        mesCbx.addItem("08");
        mesCbx.setItemCaption("08","Agosto");
        mesCbx.addItem("09");
        mesCbx.setItemCaption("09","Septiembre");
        mesCbx.addItem("10");
        mesCbx.setItemCaption("10","Octubre");
        mesCbx.addItem("11");
        mesCbx.setItemCaption("11","Noviembre");
        mesCbx.addItem("12");
        mesCbx.setItemCaption("12","Diciembre");
//        mesCbx.setWidth("100px");
        mesCbx.setMultiSelect(true);
        mesCbx.setRows(5);
        
        cuentaContableCbx = new ComboBox("Filtro por cuenta contable  : ");
        cuentaContableCbx.setWidth("27em");
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.addContainerProperty( "IdNomenclatura", String.class, "");
        llenarComboCuentaContable();

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarGridLibroMayor();
            }
        });

        libroMayorcontainer.addContainerProperty(CUENTA_PROPERTY, String.class, "");
        libroMayorcontainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        libroMayorcontainer.addContainerProperty(FECHA_PROPERTY, String.class, "");
        libroMayorcontainer.addContainerProperty(DOCTO_PROPERTY, String.class, "");
        libroMayorcontainer.addContainerProperty(TIPO_PROPERTY, String.class, "");
        libroMayorcontainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, "");
        libroMayorcontainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, "");
        libroMayorcontainer.addContainerProperty(DEBE_PROPERTY, String.class, "");
        libroMayorcontainer.addContainerProperty(HABER_PROPERTY, String.class, "");
        libroMayorcontainer.addContainerProperty(SALDO_PROPERTY, String.class, "");

        libroMayorGrid = new Grid(libroMayorcontainer);
        libroMayorGrid.setImmediate(true);
        libroMayorGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        libroMayorGrid.setHeightMode(HeightMode.ROW);
        libroMayorGrid.setHeightByRows(15);
        libroMayorGrid.setWidth("100%");
        libroMayorGrid.setResponsive(true);
        libroMayorGrid.setEditorBuffered(false);

        libroMayorGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        libroMayorGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(2);
        libroMayorGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
        libroMayorGrid.getColumn(FECHA_PROPERTY).setExpandRatio(2);
        libroMayorGrid.getColumn(DOCTO_PROPERTY).setExpandRatio(2);
        libroMayorGrid.getColumn(TIPO_PROPERTY).setExpandRatio(2);
        libroMayorGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(2);
        libroMayorGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setExpandRatio(2);
        libroMayorGrid.getColumn(DEBE_PROPERTY).setExpandRatio(1);
        libroMayorGrid.getColumn(HABER_PROPERTY).setExpandRatio(1);
        libroMayorGrid.getColumn(SALDO_PROPERTY).setExpandRatio(1);

        libroMayorGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            String codigoPartida = String.valueOf(libroMayorcontainer.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue());
            String descripcion   = String.valueOf(libroMayorcontainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue());
            String nombre        = String.valueOf(libroMayorcontainer.getContainerProperty(e.getItemId(), PROVEEDOR_PROPERTY).getValue());
            String tipo          = "";//String.valueOf(libroMayorcontainer.getContainerProperty(e.getItemId(), "").getValue());
            String documento     = String.valueOf(libroMayorcontainer.getContainerProperty(e.getItemId(), DOCTO_PROPERTY).getValue());

            MostrarPartidaContable mostrarPartidaContable
                    = new MostrarPartidaContable(
                            codigoPartida,
                            descripcion,
                            nombre,
                            tipo + " " + documento                           
                    );
            mainUI.addWindow(mostrarPartidaContable);
            mostrarPartidaContable.center();
        }));

        folioTxt = new NumberField("Folio Inicial :");
        folioTxt.setDecimalAllowed(false);
        folioTxt.setDecimalPrecision(0);
        folioTxt.setMinimumFractionDigits(0);
        folioTxt.setDecimalSeparatorAlwaysShown(false);
        folioTxt.setValue(1d);
        folioTxt.setGroupingUsed(true);
        folioTxt.setGroupingSize(0);
        folioTxt.setImmediate(true);
        folioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        folioTxt.setWidth("8em");
        folioTxt.setValidationVisible(false);

        Button generarPDF = new Button("Generar PDF");
        generarPDF.setIcon(FontAwesome.PAPER_PLANE);
        generarPDF.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarPDF.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (libroMayorcontainer.size() > 0) {
                    Collection collMes = (Collection)mesCbx.getValue();
                    Iterator iterMes = collMes.iterator();
                    String meses = "";
                    while(iterMes.hasNext()) {
                        if(iterMes.hasNext()) {
                            meses += iterMes.next();
                        }
                        if(iterMes.hasNext()) {
                            meses += ",";
                        }
                    }
                    
                    LibroMayorPDF libroMayorPdf
                            = new LibroMayorPDF(
                                    empresaId,
                                    empresaNombre,
                                    ((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId(),
                                    libroMayorcontainer,
                                    String.valueOf(anioCbx.getValue()),
                                    meses,
                                    folioTxt.getValue()
                            );
                    mainUI.addWindow(libroMayorPdf);
                    libroMayorPdf.center();

                } else {
                    Notification.show("La vista no contiene registros disponibles.. ", Notification.Type.WARNING_MESSAGE);
                }
            }
        });
               
        exportExcelBtn = new Button("Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(libroMayorGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(libroMayorGrid);
                    ExcelExport excelExport = new ExcelExport (tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);               
                    fileexport = "LibroMayor_" + empresaNombre.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + ".xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();                    
                }
            }
        });

        filterLayout.addComponents(anioCbx, mesCbx, cuentaContableCbx, consultarBtn);
        filterLayout.setComponentAlignment(anioCbx, Alignment.MIDDLE_LEFT);
        filterLayout.setComponentAlignment(mesCbx, Alignment.MIDDLE_LEFT);
        filterLayout.setComponentAlignment(cuentaContableCbx, Alignment.MIDDLE_LEFT);
        filterLayout.setComponentAlignment(consultarBtn, Alignment.MIDDLE_LEFT);

        layoutTablaLibroMayor.addComponent(filterLayout);
        layoutTablaLibroMayor.setComponentAlignment(filterLayout, Alignment.MIDDLE_CENTER);

        layoutTablaLibroMayor.addComponent(libroMayorGrid);

        layoutButtons.addComponent(folioTxt);
        layoutButtons.setComponentAlignment(folioTxt, Alignment.MIDDLE_LEFT);
        layoutButtons.addComponent(generarPDF);
        layoutButtons.setComponentAlignment(generarPDF, Alignment.BOTTOM_CENTER);
        layoutButtons.addComponent(exportExcelBtn);
        layoutButtons.setComponentAlignment(exportExcelBtn, Alignment.BOTTOM_CENTER);
        
        layoutTablaLibroMayor.addComponent(layoutButtons);
        layoutTablaLibroMayor.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);
        
        addComponent(layoutTablaLibroMayor);
        setComponentAlignment(layoutTablaLibroMayor, Alignment.MIDDLE_CENTER);        
        
    }

    public void llenarGridLibroMayor() {

        if (libroMayorcontainer == null) {
            return;
        }

        libroMayorcontainer.removeAllItems();

        Object itemId;

        String queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus = 'HABILITADA'";
        if (cuentaContableCbx.getValue() != null) {
            queryString += " AND IdNomenclatura = " + String.valueOf(cuentaContableCbx.getValue());
        }
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY ID1";

System.out.println("query con substring " + queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                Collection collMes = (Collection)mesCbx.getValue();
                                 
                Iterator iterMes = collMes.iterator();
                
                if(iterMes.hasNext() == false) {
                    Notification.show("POR FAVOR ELIJA UNO O VARIOS MESES", Notification.Type.HUMANIZED_MESSAGE);
                    return;
                }
                
                do {  // cuentas contables
                    
                   iterMes = collMes.iterator();
                   
                    while(iterMes.hasNext()) {  // meses

                        String mesCierre = String.valueOf(iterMes.next());
                        String anioMesCierre = String.valueOf(anioCbx.getValue()) + mesCierre;

                        Calendar c = Calendar.getInstance();
                        
                        if(Integer.valueOf(mesCierre) == 1) {
                            c.set(
                                    Integer.valueOf(String.valueOf(anioCbx.getValue()))-1, 
                                    11, 
                                    1
                            ); //diciembre del anio pasado
                        }
                        else {
                            c.set(
                                    Integer.valueOf(String.valueOf(anioCbx.getValue())), 
                                    (Integer.valueOf(mesCierre)-1)-1, 
                                    1
                            ); // mes anterior, mismo anio                            
                        }
//System.out.println("calendar 2 = " + c.get(Calendar.YEAR) + " " + c.get(Calendar.SHORT));

                        BigDecimal saldoAnterior = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        if(Integer.valueOf(mesCierre) > 1) {  //ENERO, entonces traer el saldo inicial del año Partida Apertura
                            saldoAnterior = saldoAnterior.add(getSaldoCuentaPartidaApertura(String.valueOf(anioCbx.getValue()), rsRecords.getString("IdNomenclatura")));
                        }
                        else { // FEBRERO en adelante, entonces traer el saldo final del mes anterior...
                            saldoAnterior = saldoAnterior.add(getSaldoCuentaMesAnterior(c, rsRecords.getString("IdNomenclatura")));
                        }

                        itemId = libroMayorcontainer.addItem();

                        libroMayorcontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                        libroMayorcontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));
                        libroMayorcontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(String.valueOf( mesCbx.getItemCaption(mesCierre).toUpperCase() + " " + anioCbx.getItemCaption(anioCbx.getValue())));
                        libroMayorcontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("S. ANTERIOR : ");
                        if(   rsRecords.getString("NoCuenta").startsWith("1")
                           || rsRecords.getString("NoCuenta").startsWith("5")
                           || rsRecords.getString("NoCuenta").startsWith("6")) {

                            libroMayorcontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(saldoAnterior));
                            libroMayorcontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("0.00");
                        }
                        else {
                            libroMayorcontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("0.00");
                            libroMayorcontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(saldoAnterior));
                        }

                        queryString = " SELECT contabilidad_partida.IdPartida, contabilidad_partida.Descripcion, ";
                        queryString += " contabilidad_partida.CodigoPartida,  contabilidad_nomenclatura_empresa.NoCuenta,";
                        queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales,";
                        queryString += " contabilidad_nomenclatura_empresa.N5, contabilidad_partida.Fecha, ";
                        queryString += " contabilidad_partida.IdProveedor, contabilidad_partida.NombreProveedor,";
                        queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
                        queryString += " contabilidad_partida.TipoDocumento ";
                        queryString += " FROM contabilidad_partida, contabilidad_nomenclatura_empresa";
                        queryString += " WHERE contabilidad_partida.IdEmpresa  = " + empresaId;
                        queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                        queryString += " AND contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura";
                        queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";
                        queryString += " AND Extract(YEAR_MONTH From contabilidad_partida.Fecha) = " + anioMesCierre;
                        queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
                        queryString += " ORDER BY contabilidad_partida.Fecha";

//System.out.println("QUERY LIBRO MAYOR = " + queryString);

                        rsRecords1 = stQuery1.executeQuery(queryString);

                        if (rsRecords1.next()) { //  encontrado el detalle contable
                            
                            BigDecimal totalSaldoAnterior = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal totalSaldoFinal = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP); 

                            do { //por cada movimiento contable
                                itemId = libroMayorcontainer.addItem();

                                totalDebe  = totalDebe.add(new BigDecimal(rsRecords1.getDouble("DebeQuetzales")));
                                totalHaber = totalHaber.add(new BigDecimal(rsRecords1.getDouble("HaberQuetzales")));

                                libroMayorcontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("");
                                libroMayorcontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords1.getString("CodigoPartida"));
                                libroMayorcontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords1.getString("DESCRIPCION"));
                                libroMayorcontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords1.getDate("Fecha")));
                                libroMayorcontainer.getContainerProperty(itemId, DOCTO_PROPERTY).setValue(rsRecords1.getString("SerieDocumento") + " " + rsRecords1.getString("NumeroDocumento"));
                                libroMayorcontainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords1.getString("SerieDocumento") + " " + rsRecords1.getString("TipoDocumento"));
                                if(rsRecords1.getString("NombreProveedor") == null) {
                                    libroMayorcontainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords1.getString("Descripcion"));
                                }
                                else if(rsRecords1.getString("NombreProveedor").trim().isEmpty()) {
                                    libroMayorcontainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords1.getString("Descripcion"));
                                }
                                else {
                                    libroMayorcontainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords1.getString("NombreProveedor"));
                                }
                                libroMayorcontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("DebeQuetzales")));
                                libroMayorcontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("HaberQuetzales")));

                                if(   rsRecords.getString("NoCuenta").startsWith("1")
                                   || rsRecords.getString("NoCuenta").startsWith("5")
                                   || rsRecords.getString("NoCuenta").startsWith("6")) { //activo, egreso, costo
                                    saldoAnterior = saldoAnterior.add(new BigDecimal(rsRecords1.getDouble("DebeQuetzales") - rsRecords1.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                }
                                else { //resto
                                    saldoAnterior = saldoAnterior.subtract(new BigDecimal(rsRecords1.getDouble("HaberQuetzales") - rsRecords1.getDouble("DebeQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                }
                                libroMayorcontainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(numberFormat.format(saldoAnterior));

                            } while (rsRecords1.next()); // end movimiento contable

                            itemId = libroMayorcontainer.addItem();

                            libroMayorcontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(" ");
                            libroMayorcontainer.getContainerProperty(itemId, DOCTO_PROPERTY).setValue(" ");
                            libroMayorcontainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(" ");
                            libroMayorcontainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(" ");
                            libroMayorcontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(" ");
                            libroMayorcontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(" ");
                            libroMayorcontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(" ");
                            libroMayorcontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("TOTALES");
                            libroMayorcontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(totalDebe));
                            libroMayorcontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(totalHaber));
                            libroMayorcontainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(numberFormat.format(saldoAnterior));

                        } //end if encontro movimiento contable

                        itemId = libroMayorcontainer.addItem();  //como linea separadora
                        libroMayorcontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("--------");
                        libroMayorcontainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue("--------");
                        libroMayorcontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("--------");
                        libroMayorcontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("--------");
                        libroMayorcontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("--------");
                        libroMayorcontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("--------");
                        libroMayorcontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("--------");
                        libroMayorcontainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue("--------");
                        
                    } // end while (iterMes.hasNext())
                } while (rsRecords.next());

            } else {
                Notification.show("La consulta no tiene resultados por el momento", Notification.Type.WARNING_MESSAGE);
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en LibroMayorView:" + ex);
            Notification.show("ERROR AL CONSULTAR EL LIBRO MAYOR, CLICK AQUI PARA CONTINUAR...", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        String queryString = "";
        queryString += " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY N5";

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            while (rsRecords2.next()) { //  encontrado
                cuentaContableCbx.addItem(rsRecords2.getString("IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords2.getString("IdNomenclatura"), rsRecords2.getString("NoCuenta") + " " + rsRecords2.getString("N5"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public BigDecimal getSaldoCuentaMesAnterior(Calendar c, String idNomenclatura) throws SQLException {
        BigDecimal saldo = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                       
        String queryString  = " SELECT IfNull(SaldoFinal, 0) SaldoMesAnterior ";
        queryString += " FROM contabilidad_balance_saldo";
        queryString += " WHERE IdNomenclatura = " + idNomenclatura;
        queryString += " AND  IdEmpresa = " + empresaId;
        queryString += " AND  AnioMesCierre = " + String.valueOf(c.get(Calendar.YEAR)) + String.format("%02d", c.get(Calendar.MONTH)+1) ;
        
//System.out.println("queryMesAnterior = " + queryString);

        rsRecords2 = stQuery2.executeQuery(queryString);

        if(rsRecords2.next()) {
            saldo = rsRecords2.getBigDecimal("SaldoMesAnterior").setScale(2, BigDecimal.ROUND_HALF_UP);
        }
//System.out.println("saldoMesAnterior = " + saldo);        
        return saldo;
    }

    public BigDecimal getSaldoCuentaPartidaApertura(String anio, String idNomenclatura) throws SQLException {
        BigDecimal saldo = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

        String queryString  = "SELECT DebeQuetzales, HaberQuetzales ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdNomenclatura = " + idNomenclatura;
        queryString += " AND  IdEmpresa = " + empresaId;
        queryString += " AND  TipoDocumento = 'PARTIDA APERTURA'";
        queryString += " AND  Fecha = '" + anio + "-01-01'";

System.out.println("querySaldoPartidApertura = " + queryString);

        rsRecords2 = stQuery2.executeQuery(queryString);

        if(rsRecords2.next()) {
            if (rsRecords2.getDouble("DebeQuetzales") > 0) {
                saldo = rsRecords2.getBigDecimal("DebeQuetzales").setScale(2, BigDecimal.ROUND_HALF_UP);
            } else {
                saldo = rsRecords2.getBigDecimal("HaberQuetzales").setScale(2, BigDecimal.ROUND_HALF_UP);
            }
//System.out.println("saldoMesAnterior = " + saldo);
        }
        return saldo;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Libro Mayor");
    }
}
