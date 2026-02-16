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
public class LibroInventarioView extends VerticalLayout implements View {

    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String FECHA_PROPERTY = "Fecha";
    static final String DOCTO_PROPERTY = "Docto";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String CODIGO_PARTIDA_PROPERTY = "Partida";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String SALDO_PROPERTY = "Saldo";

    Utileria utileria = new Utileria();

    Grid libroInventarioGrid;
    Grid.FooterRow footerLibroInventario;

    public IndexedContainer libroInventarioContainer = new IndexedContainer();

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
    ComboBox empresaCbx;
    String empresa;
    ComboBox anioCbx;
    ListSelect mesCbx;
    ComboBox cuentaContableCbx;
    NumberField folioTxt;

    static DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    public LibroInventarioView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);
        setHeightUndefined();

        Label titleLbl = new Label("LIBRO INVENTARIO");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
//        titleLbl.addStyleName("h2_custom");

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);        

        llenarComboEmpresa();
        
        empresaCbx.addValueChangeListener(event -> {
            empresa = String.valueOf(event.getProperty().getValue());            
            llenarGridLibroMayor(empresa);
        });

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaLibroMayor();

        empresa = String.valueOf(empresaCbx.getValue());

//        llenarGridLibroMayor(empresa);

    }

    public void crearTablaLibroMayor() {
        VerticalLayout layoutTablaLibroMayor = new VerticalLayout();
        layoutTablaLibroMayor.setWidth("100%");
        layoutTablaLibroMayor.setSpacing(true);
//        layoutTablaLibroMayor.setHeightUndefined();
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
        llenarComboCuentaContable();

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarGridLibroMayor(empresa);
            }
        });

        libroInventarioContainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        libroInventarioContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        libroInventarioContainer.addContainerProperty(DOCTO_PROPERTY, String.class, null);
        libroInventarioContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        libroInventarioContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        libroInventarioContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        libroInventarioContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        libroInventarioContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        libroInventarioContainer.addContainerProperty(SALDO_PROPERTY, String.class, null);

        libroInventarioGrid = new Grid(libroInventarioContainer);
        libroInventarioGrid.setImmediate(true);
        libroInventarioGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        libroInventarioGrid.setHeightMode(HeightMode.ROW);
        libroInventarioGrid.setHeightByRows(15);
        libroInventarioGrid.setWidth("100%");
        libroInventarioGrid.setResponsive(true);
        libroInventarioGrid.setEditorBuffered(false);

        libroInventarioGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

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

        libroInventarioGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        libroInventarioGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
        libroInventarioGrid.getColumn(FECHA_PROPERTY).setExpandRatio(2);
        libroInventarioGrid.getColumn(DOCTO_PROPERTY).setExpandRatio(2);
        libroInventarioGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(2);
        libroInventarioGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setExpandRatio(2);
        libroInventarioGrid.getColumn(DEBE_PROPERTY).setExpandRatio(1);
        libroInventarioGrid.getColumn(HABER_PROPERTY).setExpandRatio(1);
        libroInventarioGrid.getColumn(SALDO_PROPERTY).setExpandRatio(1);

        libroInventarioGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            String codigoPartida = String.valueOf(libroInventarioContainer.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue());
            String descripcion   = String.valueOf(libroInventarioContainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue());
            String nombre        = String.valueOf(libroInventarioContainer.getContainerProperty(e.getItemId(), PROVEEDOR_PROPERTY).getValue());
            String tipo          = "";//String.valueOf(libroInventarioContainer.getContainerProperty(e.getItemId(), "").getValue());
            String documento     = String.valueOf(libroInventarioContainer.getContainerProperty(e.getItemId(), DOCTO_PROPERTY).getValue());

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
        
        Button generarPDF = new Button("Generar PDF");
        generarPDF.setIcon(FontAwesome.PAPER_PLANE);
        generarPDF.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarPDF.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (libroInventarioContainer.size() > 0) {
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
                                    empresa,
                                    empresaCbx.getItemCaption(empresaCbx.getValue()),
                                    getEmpresaNit(),
                                    libroInventarioContainer,
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
                if(libroInventarioGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(libroInventarioGrid);
                    ExcelExport excelExport = new ExcelExport (tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);               
                    fileexport = "LibroInventario_" + empresaCbx.getItemCaption(empresaCbx.getValue()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + ".xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();                    
                }
            }
        });

        filterLayout.addComponents(anioCbx, mesCbx, cuentaContableCbx, consultarBtn);
        filterLayout.setComponentAlignment(anioCbx, Alignment.MIDDLE_LEFT);
        filterLayout.setComponentAlignment(mesCbx, Alignment.MIDDLE_LEFT);
        filterLayout.setComponentAlignment(cuentaContableCbx, Alignment.TOP_RIGHT);
        filterLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_RIGHT);

        layoutTablaLibroMayor.addComponent(filterLayout);
        layoutTablaLibroMayor.setComponentAlignment(filterLayout, Alignment.MIDDLE_CENTER);

        layoutTablaLibroMayor.addComponent(libroInventarioGrid);

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

        layoutButtons.addComponent(folioTxt);
        layoutButtons.setComponentAlignment(folioTxt, Alignment.MIDDLE_LEFT);
        layoutButtons.addComponent(generarPDF);
        layoutButtons.setComponentAlignment(generarPDF, Alignment.MIDDLE_CENTER);
        layoutButtons.addComponent(exportExcelBtn);
        layoutButtons.setComponentAlignment(exportExcelBtn, Alignment.MIDDLE_CENTER);
        
        layoutTablaLibroMayor.addComponent(layoutButtons);
        layoutTablaLibroMayor.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);
        
        addComponent(layoutTablaLibroMayor);
        setComponentAlignment(layoutTablaLibroMayor, Alignment.MIDDLE_CENTER);        
        
    }

    public void llenarGridLibroMayor(String empresa) {

        if (libroInventarioContainer == null) {
            return;
        }

        libroInventarioContainer.removeAllItems();

        Object itemId;

        String queryString = " SELECT * from contabilidad_nomenclatura";
        queryString += " where Estatus = 'HABILITADA'";
        queryString += " and N3 = 'Inventario'";
        if (cuentaContableCbx.getValue() != null) {
            queryString += " And N5 = '" + cuentaContableCbx.getItemCaption(cuentaContableCbx.getValue())
                    .substring(9, cuentaContableCbx.getItemCaption(cuentaContableCbx.getValue()).length()) + "'";
        }
        queryString += " Order By ID1";

//System.out.println("query con substring " + queryString);

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
                        if(Integer.valueOf(mesCierre) > 1) {
                            saldoAnterior = saldoAnterior.add(getSaldoCuentaMesAnterior(c, rsRecords.getString("IdNomenclatura")));
                        }

                        itemId = libroInventarioContainer.addItem();

                        libroInventarioContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                        libroInventarioContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));
                        libroInventarioContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(String.valueOf( mesCbx.getItemCaption(mesCierre).toUpperCase() + " " + anioCbx.getItemCaption(anioCbx.getValue())));
                        libroInventarioContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("S. ANTERIOR : ");
                        if(   rsRecords.getString("NoCuenta").startsWith("1")
                           || rsRecords.getString("NoCuenta").startsWith("5")
                           || rsRecords.getString("NoCuenta").startsWith("6")) {

                            libroInventarioContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(saldoAnterior));
                        }
                        else {                        
                            libroInventarioContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(saldoAnterior));
                        }

                        queryString = " select contabilidad_partida.IdPartida, contabilidad_partida.Descripcion, ";
                        queryString += " contabilidad_partida.CodigoPartida,  contabilidad_nomenclatura.NoCuenta,";
                        queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales,";
                        queryString += " contabilidad_nomenclatura.N5, contabilidad_partida.Fecha, ";
                        queryString += " contabilidad_partida.IdProveedor, contabilidad_partida.NombreProveedor,";
                        queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento ";
                        queryString += " From contabilidad_partida, contabilidad_nomenclatura";
                        queryString += " Where contabilidad_partida.IdEmpresa  = " + empresa;
                        queryString += " And contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                        queryString += " And contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
                        queryString += " And contabilidad_partida.Estatus <> 'ANULADO'";
                        queryString += " And Extract(YEAR_MONTH From contabilidad_partida.Fecha) = " + anioMesCierre;
                        queryString += " Order By contabilidad_partida.CodigoPartida, contabilidad_partida.Debe Desc";

//System.out.println("QUERY LIBRO MAYOR = " + queryString);

                        rsRecords1 = stQuery1.executeQuery(queryString);

                        if (rsRecords1.next()) { //  encontrado el detalle contable
                            
                            BigDecimal totalSaldoAnterior = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal totalSaldoFinal = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP); 

                            do { //por cada movimiento contable
                                itemId = libroInventarioContainer.addItem();

                                totalDebe  = totalDebe.add(new BigDecimal(rsRecords1.getDouble("DebeQuetzales")));
                                totalHaber = totalHaber.add(new BigDecimal(rsRecords1.getDouble("HaberQuetzales")));

                                libroInventarioContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("");
                                libroInventarioContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords1.getString("CodigoPartida"));
                                libroInventarioContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords1.getString("DESCRIPCION"));
                                libroInventarioContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords1.getDate("Fecha")));
                                libroInventarioContainer.getContainerProperty(itemId, DOCTO_PROPERTY).setValue(rsRecords1.getString("SerieDocumento") + " " + rsRecords1.getString("NumeroDocumento"));
                                libroInventarioContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords1.getString("NombreProveedor"));
                                libroInventarioContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("DebeQuetzales")));
                                libroInventarioContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("HaberQuetzales")));

                                if(   rsRecords.getString("NoCuenta").startsWith("1")
                                   || rsRecords.getString("NoCuenta").startsWith("5")
                                   || rsRecords.getString("NoCuenta").startsWith("6")) {
                                    saldoAnterior = saldoAnterior.add(new BigDecimal(rsRecords1.getDouble("DebeQuetzales") - rsRecords1.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
    //                                totalSaldoFinal = totalSaldoFinal.add(saldoAnterior).setScale(2, BigDecimal.ROUND_HALF_UP);
    //                                totalSaldoFinal = totalSaldoFinal.add(new BigDecimal(rsRecords1.getDouble("DebeQuetzales") - rsRecords1.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);;
                                }
                                else {
                                    saldoAnterior = saldoAnterior.subtract(new BigDecimal(rsRecords1.getDouble("DebeQuetzales") - rsRecords1.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
    //                                totalSaldoFinal = totalSaldoFinal.add(saldoAnterior).setScale(2, BigDecimal.ROUND_HALF_UP);
    //                                totalSaldoFinal = totalSaldoFinal.subtract(new BigDecimal(rsRecords1.getDouble("DebeQuetzales") - rsRecords1.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);;
                                }
                                libroInventarioContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(numberFormat.format(saldoAnterior));

                            } while (rsRecords1.next()); // end movimiento contable

                            itemId = libroInventarioContainer.addItem();

                            libroInventarioContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(" ");
                            libroInventarioContainer.getContainerProperty(itemId, DOCTO_PROPERTY).setValue(" ");
                            libroInventarioContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(" ");
                            libroInventarioContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(" ");
                            libroInventarioContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(" ");
                            libroInventarioContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("Totales Exactos");
                            libroInventarioContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(totalDebe));
                            libroInventarioContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(totalHaber));
                            libroInventarioContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(numberFormat.format(saldoAnterior));

                        } //end if encontro movimiento contable

                        itemId = libroInventarioContainer.addItem();  //como linea separadora
                        libroInventarioContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("--------");
                        libroInventarioContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("--------");
                        libroInventarioContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("--------");
                        libroInventarioContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("--------");
                        libroInventarioContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("--------");
                        libroInventarioContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("--------");
                        libroInventarioContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue("--------");
                        
                    } // end while (iterMes.hasNext())
                } while (rsRecords.next());

            } else {
                Notification.show("La consulta no tiene resultados por el momento", Notification.Type.WARNING_MESSAGE);
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en LibroInventarioView:" + ex);
            Notification.show("ERROR AL CONSULTAR EL LIBRO MAYOR, CLICK AQUI PARA CONTINUAR...", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        String queryString = "";
        queryString += " SELECT * from contabilidad_nomenclatura";
        queryString += " where Estatus = 'HABILITADA'";
        queryString += " Order By N5";

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

    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));
            }
            rsRecords1.first();
            
            empresaCbx.select(rsRecords1.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public String getEmpresaNit() {
        String strNit = "N/A";

        String queryString = " SELECT Nit from contabilidad_empresa ";
        queryString += " Where IdEmpresa = " + empresa;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) {
                strNit = rsRecords1.getString("Nit");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar NIT de empresa: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return strNit;
    }

    public BigDecimal getSaldoCuentaMesAnterior(Calendar c, String idNomenclatura) throws SQLException {
        BigDecimal saldo = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                       
        String queryString  = " Select IfNull(SaldoFinal, 0) SaldoMesAnterior ";
        queryString += " From contabilidad_balance_saldo";
        queryString += " Where IdNomenclatura = " + idNomenclatura;
        queryString += " And  IdEmpresa = " + empresa;
        queryString += " And  AnioMesCierre = " + String.valueOf(c.get(Calendar.YEAR)) + String.format("%02d", c.get(Calendar.MONTH)+1) ;
        
//System.out.println("queryMesAnterior = " + queryString);

        rsRecords2 = stQuery2.executeQuery(queryString);

        if(rsRecords2.next()) {
            saldo = rsRecords2.getBigDecimal("SaldoMesAnterior").setScale(2, BigDecimal.ROUND_HALF_UP);
        }
//System.out.println("saldoMesAnterior = " + saldo);        
        return saldo;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Libro Mayor");
    }
}
