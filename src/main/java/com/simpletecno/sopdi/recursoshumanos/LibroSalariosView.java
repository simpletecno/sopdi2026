package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.*;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.io.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class LibroSalariosView extends VerticalLayout implements View {

    UI mainUI;
    Utileria utileria;

    String filePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/projectfiles/";
    String fileName;

    final FileDownloader[] fileDownloader = new FileDownloader[3];

    // Enum del contendio del Libro de Salarios para facilitar la iteracion por mes tanto del excel como de la tabla
    public enum LibroSalarioProperties {
        ID_PLANILLA_PROPERTY("ID"),
        NO_PAGO_PROPERTY("Cheque"),
        FECHA_PROPERTY("Fecha"),
        SALARIO_BASE_PROPERTY("Salario en Quetzales"),
        DIAS_TRABAJODOS_PROPERTY("Dias Trabajados"),
        HORAS_ORDINARIAS_PROPERTY("Horas Ordinarias"),
        HORAS_EXTRAORDINARIAS_PROPERTY("Horas Extraordinarias"),
        SALARIO_ORDINARIO_PROPERTY("Salario Ordinario"),
        SALARIO_EXTRAORDINARIO_PROPERTY("Salario Extraordinario"),
        OTROS_SALARIOS_PROPERTY("Otros Salarios"),
        SALARIO_SEPTIMOS_ASUETOS_PROPERTY("Séptimos y Asuetos"),
        VACACIONES_PROPERTY("Vacaciones"),
        SALARIO_TOTAL_PROPERTY("SALARIO TOTAL"),
        CUOTA_LABORAL_IGSS_PROPERTY("Cuota Laboral IGSS"),
        DESCUENTO_ISR_PROPERTY("Descuentos ISR"),
        OTRAS_DEDUCCIONES_PROPERTY("Otras Deducciones"),
        SALARIO_DESCUENTO_PROPERTY("Total"),
        BONOS_PROPERTY("Bono 14 / Aguinaldo"),
        BONO_37_2001_PROPERTY("Decreto 37-2001"),
        DEVOLUCIONES_PROPERTY("Devoluciones"),
        SALARIO_LIQUIDO_PROPERTY("Salario Liquido");

        private final String descripcion;

        LibroSalarioProperties(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public IndexedContainer libroSalarioContainer = new IndexedContainer();

    HorizontalLayout opcionLayout;
    VerticalLayout reportLayout;
    HorizontalLayout layoutGrid;
    HorizontalLayout buttonExcelLayout;

    Grid libroSalarioGrid;

    ComboBox anioCbx;
    ComboBox proveedorCbx;

    Label nombreProveedorLbl;

    Button siguienteBtn;
    Button excelBtn;
    Button anteriorBtn;

    Button changeDateBtn;

    Statement stQuery;
    ResultSet rsRecords1;
    String queryString1;
    String queryString2;

    Double[] asuetoArray = new Double[12];
    List<String> proveedorList;

    FileInputStream fileInputStream;

    HSSFWorkbook workbook;

    HSSFSheet sheet;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public LibroSalariosView(){
        this.utileria = new Utileria();
        this.mainUI = UI.getCurrent();

        setWidth("100%");
        setMargin(false);
        setSpacing(true);
        setResponsive(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " Libro de Salarios");
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            titleLbl.addStyleName(ValoTheme.LABEL_H2);
        }
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        opcionLayout = new HorizontalLayout();
        opcionLayout.setSpacing(true);
        opcionLayout.setResponsive(true);
        opcionLayout.setMargin(false);
        opcionLayout.setWidth("95%");

        reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("95%");
        reportLayout.setHeightUndefined();
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);
        reportLayout.setMargin(false);

        layoutGrid = new HorizontalLayout();
        layoutGrid.setWidth("100%");
        layoutGrid.setHeightUndefined();
        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(false);

        buttonExcelLayout = new HorizontalLayout();
        buttonExcelLayout.setWidth("85%");
        buttonExcelLayout.setHeightUndefined();
        buttonExcelLayout.setSpacing(false);
        buttonExcelLayout.setMargin(false);

        addComponent(titleLayout);
        addComponents(opcionLayout, reportLayout, buttonExcelLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        setComponentAlignment(opcionLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
        setComponentAlignment(buttonExcelLayout, Alignment.TOP_CENTER);

        crearLibroSalarioGrid();

        crearOpciones();

        setFechaBaseDeDatos();

        crearBottonExcel();

        descargarLibroSalarioExcel();
    }

    public void crearLibroSalarioGrid(){

        // Agregar las columnas del Enum
        for (LibroSalarioProperties propiedad : LibroSalarioProperties.values()){
            libroSalarioContainer.addContainerProperty(propiedad.getDescripcion(), String.class, "");
        }

        libroSalarioGrid = new Grid("Libro de Salarios", libroSalarioContainer);
        libroSalarioGrid.setWidth("95%");
        libroSalarioGrid.setImmediate(true);
        libroSalarioGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        libroSalarioGrid.setDescription("Seleccione un registro.");
        libroSalarioGrid.setHeightMode(HeightMode.ROW);
        libroSalarioGrid.setHeightByRows(14);
        libroSalarioGrid.setResponsive(true);
        libroSalarioGrid.setResponsive(true);
        libroSalarioGrid.setEditorBuffered(false);
        libroSalarioGrid.setSizeFull();

        libroSalarioGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (LibroSalarioProperties.ID_PLANILLA_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "centeralign";
            }else if (LibroSalarioProperties.FECHA_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (LibroSalarioProperties.NO_PAGO_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (LibroSalarioProperties.SALARIO_BASE_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.DIAS_TRABAJODOS_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "leftalign";
            } else if (LibroSalarioProperties.HORAS_ORDINARIAS_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "leftalign";
            } else if (LibroSalarioProperties.HORAS_EXTRAORDINARIAS_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "leftalign";
            } else if (LibroSalarioProperties.SALARIO_ORDINARIO_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.SALARIO_EXTRAORDINARIO_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.OTROS_SALARIOS_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.SALARIO_SEPTIMOS_ASUETOS_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.VACACIONES_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.SALARIO_TOTAL_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.CUOTA_LABORAL_IGSS_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.DESCUENTO_ISR_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.OTRAS_DEDUCCIONES_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.SALARIO_DESCUENTO_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.BONOS_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.BONO_37_2001_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.DEVOLUCIONES_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (LibroSalarioProperties.SALARIO_LIQUIDO_PROPERTY.getDescripcion().equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        layoutGrid.addComponent(libroSalarioGrid);
        layoutGrid.setComponentAlignment(libroSalarioGrid, Alignment.BOTTOM_CENTER);
        reportLayout.addComponent(layoutGrid);

    }

    public void crearOpciones(){

        nombreProveedorLbl = new Label("Nombre");
        proveedorCbx = new ComboBox("Empleados");
        anioCbx = new ComboBox("Año");
        changeDateBtn = new Button("Cambiar Asuetos");

        anioCbx.setTextInputAllowed(false);
        anioCbx.setNewItemsAllowed(false);
        anioCbx.setNullSelectionAllowed(false);
        anioCbx.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if(proveedorCbx != null) {
                    if (!anioCbx.isEmpty() && !proveedorCbx.isEmpty()){
                        llenarLibrosalario();
                        if(excelBtn != null) {
                            descargarLibroSalarioExcel();
                        }
                    }
                    llenarProveedores();
                }
            }
        });

        int anio_inico = 0;
        int anio_fin = 0;
        queryString1 =  "SELECT YEAR(MIN(FechaInicio)) AS Anio ";
        queryString1 += "FROM planilla_encabezado ";
        queryString1 += "WHERE IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery.executeQuery(queryString1);

            if(rsRecords1.next()) anio_inico = rsRecords1.getInt("Anio");
        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores/Empleados " + ex1.getMessage());
            ex1.printStackTrace();
        }
        anio_fin = Utileria.getTomorrow().getYear() + 1900;
        while(anio_inico <= anio_fin && anio_inico != 0) {
            anioCbx.addItem(anio_inico);
            anio_inico++;
        }
        anioCbx.select(anio_fin);

        proveedorCbx.setTextInputAllowed(true);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addContainerProperty("Nombre", String.class, "");
        proveedorCbx.addContainerProperty("IGSS", String.class, "");
        proveedorCbx.addContainerProperty("Cargo", String.class, "");
        proveedorCbx.addContainerProperty("Nit", String.class, "");
        proveedorCbx.addContainerProperty("Edad", String.class, "");
        proveedorCbx.addContainerProperty("DPI", String.class, "");
        proveedorCbx.addContainerProperty("Genero", String.class, "");
        proveedorCbx.addContainerProperty("Nacionalidad", String.class, "");
        proveedorCbx.addContainerProperty("Inicio", String.class, "");
        proveedorCbx.addContainerProperty("Final", String.class, "");
        proveedorCbx.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (!proveedorCbx.isEmpty()) {
                    nombreProveedorLbl.setValue(String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "Nombre").getValue()));
                    if (anioCbx != null) {
                        if (!anioCbx.isEmpty() && !proveedorCbx.isEmpty()) {
                            llenarLibrosalario();
                            if (excelBtn != null) {
                                descargarLibroSalarioExcel();
                            }
                        }
                    }
                    if (anteriorBtn != null && !proveedorList.isEmpty()) {
                        if (proveedorList.indexOf((String) proveedorCbx.getValue()) == 0) {
                            anteriorBtn.setEnabled(false);
                        } else {
                            anteriorBtn.setEnabled(true);
                        }
                    }

                    if (siguienteBtn != null && !proveedorList.isEmpty()) {
                        if (proveedorList.indexOf((String) proveedorCbx.getValue()) == (proveedorList.size() - 1)) {
                            siguienteBtn.setEnabled(false);
                        } else {
                            siguienteBtn.setEnabled(true);
                        }
                    }
                }
            }
        });

        changeDateBtn.addClickListener(clickEvent -> {
            LibroSalarioAsuetosForm cambioAsuetos = new LibroSalarioAsuetosForm((Integer)anioCbx.getValue());
            UI.getCurrent().addWindow(cambioAsuetos);
            cambioAsuetos.center();
        });

        opcionLayout.addComponents(proveedorCbx, nombreProveedorLbl, anioCbx, changeDateBtn);
        opcionLayout.setComponentAlignment(proveedorCbx, Alignment.MIDDLE_CENTER);
        opcionLayout.setComponentAlignment(nombreProveedorLbl, Alignment.BOTTOM_CENTER);
        opcionLayout.setComponentAlignment(anioCbx, Alignment.MIDDLE_CENTER);
        opcionLayout.setComponentAlignment(changeDateBtn, Alignment.BOTTOM_CENTER);

        llenarProveedores();
    }

    public void crearBottonExcel(){

        anteriorBtn = new Button("Anterior");
        anteriorBtn.setDescription("Exportar a Excel el Libro de Salarios");
        anteriorBtn.setWidth(130, Sizeable.UNITS_PIXELS);
        anteriorBtn.addClickListener(clickEvent -> {
            anterior();
        });
        anteriorBtn.setEnabled(false);

        excelBtn = new Button("Excel");
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.setDescription("Exportar a Excel el Libro de Salarios");
        excelBtn.setWidth(130, Sizeable.UNITS_PIXELS);

        siguienteBtn = new Button("Siguiente");
        siguienteBtn.setDescription("Exportar a Excel el Libro de Salarios");
        siguienteBtn.setWidth(130, Sizeable.UNITS_PIXELS);
        siguienteBtn.addClickListener(clickEvent -> {
            siguiente();
        });


        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.addComponent(anteriorBtn);
        layout.addComponent(excelBtn);
        layout.addComponent(siguienteBtn);
        layout.setComponentAlignment(siguienteBtn, Alignment.BOTTOM_RIGHT);

        buttonExcelLayout.addComponent(layout);
        buttonExcelLayout.setComponentAlignment(layout, Alignment.BOTTOM_RIGHT);
    }

    public void llenarProveedores(){
        String id;
        if(!proveedorCbx.isEmpty()) proveedorCbx.removeAllItems();

        queryString1 =  "SELECT p.IDProveedor, p.Nombre, p.Cargo, p.Nit, (" + anioCbx.getValue() + " - YEAR(p.FechaNacimiento)) AS Edad, ";
        queryString1 +=        "p.DPI, p.AfiliacionIGSS, p.Genero, p.Nacionalidad, p.FechaIngreso, p.FechaEgreso, ec.Descripcion ";
        queryString1 += "FROM proveedor_empresa p ";
        queryString1 += "INNER JOIN empleado_cargo ec on p.Cargo = ec.Cargo ";
        queryString1 += "AND ec.IdEmpresa = " + empresaId;
        queryString1 += "AND p.IdEmpresa = " + empresaId;
        queryString1 += "WHERE  YEAR(FechaEgreso) >= " + anioCbx.getValue() + " ";
        queryString1 += "OR ISNULL(FechaEgreso) ";
        queryString1 += "AND EsPlanilla = 1 ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery.executeQuery(queryString1);
            String primero;
            if(rsRecords1.next()) {
                primero = rsRecords1.getString("IdProveedor");
                do{
                    id = rsRecords1.getString("IdProveedor");
                    proveedorCbx.addItem(id);
                    proveedorCbx.getContainerProperty(id, "Nombre").setValue(rsRecords1.getString("Nombre"));
                    proveedorCbx.getContainerProperty(id, "IGSS").setValue(rsRecords1.getString("AfiliacionIGSS"));
                    proveedorCbx.getContainerProperty(id, "Cargo").setValue(rsRecords1.getString("Descripcion"));
                    proveedorCbx.getContainerProperty(id, "Edad").setValue(rsRecords1.getString("Edad"));
                    proveedorCbx.getContainerProperty(id, "Nit").setValue(rsRecords1.getString("Nit"));
                    proveedorCbx.getContainerProperty(id, "DPI").setValue(rsRecords1.getString("DPI"));
                    proveedorCbx.getContainerProperty(id, "Genero").setValue(rsRecords1.getString("Genero"));
                    proveedorCbx.getContainerProperty(id, "Nacionalidad").setValue(rsRecords1.getString("Nacionalidad"));
                    proveedorCbx.getContainerProperty(id, "Inicio").setValue(Utileria.getFechaDDMMYYYY(rsRecords1.getDate("FechaIngreso")));
                    if(rsRecords1.getDate("FechaEgreso") != null) {
                        proveedorCbx.getContainerProperty(id, "Final").setValue(Utileria.getFechaDDMMYYYY(rsRecords1.getDate("FechaEgreso")));
                    }
                }while(rsRecords1.next());
                proveedorCbx.select(primero);
                proveedorList =  new ArrayList<>((List<String>) proveedorCbx.getItemIds());
            }
        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores/Empleados " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarLibrosalario(){
        boolean fin = false; // Asegurarme de romper el for

        getSeptimosAsuetos();
        libroSalarioContainer.removeAllItems();

        queryString1 = "SELECT MONTH(pe.FechaFin) as Mes, pe.Descripcion, pe.Tipo, pd.*, p.FechaEgreso ";
        queryString1 += "FROM planilla_encabezado pe ";
        queryString1 += "INNER JOIN planilla_detalle pd ON pe.id = pd.IdPlanilla ";
        queryString1 += "AND pe.FechaInicio >= '" + anioCbx.getValue() + "-01-01' ";
        queryString1 += "AND pe.FechaInicio < '" + (((int)anioCbx.getValue()) + 1) + "-01-01' ";
        queryString1 += "INNER JOIN proveedor_empresa p ON p.IDProveedor = pd.IdEmpleado ";
        queryString1 += "AND IdEmpleado = " + proveedorCbx.getValue() +  " ";
        queryString1 += "AND p.IdEmpresa = " + empresaId + " ";
        queryString1 += "WHERE pe.Tipo IN ('Salario', 'Liquidacion') OR pe.Tipo LIKE '%Provi%' ";
        queryString1 += "ORDER BY Mes";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery.executeQuery(queryString1);

            Object itemId;

            int diasPeriodo;

            double diasTrabajados;
            Double diasAsuetoSeptimos;

            double horasTrabajadas;

            double horasExtraTrabajadas;

            String fechaPeriodo;
            double sueldoBase;
            double otrasDeducciones;
            double otrosSalarios;

            double salarioTotal;
            double salarioDescuento; // Salario total - Descuento toal

            if(rsRecords1.next()) {
                // Rotar por los 12 meses
                for (int mes = 0; mes < 12 && !fin; mes++) {

                    while (rsRecords1.getInt("Mes") == (mes + 1)) {
                        itemId = libroSalarioContainer.addItem();

                        Date date = new Date(((Integer) anioCbx.getValue() - 1900), mes, 1);

                        diasPeriodo = utileria.getUltimoDiaDelMes(date) - 1;

                        diasAsuetoSeptimos = asuetoArray[mes];

                        diasTrabajados = diasPeriodo - diasAsuetoSeptimos;
                        horasTrabajadas = diasTrabajados * 8;

                        horasExtraTrabajadas = (rsRecords1.getDouble("HorasExtra") + rsRecords1.getDouble("HorasExtraII"));

                        if(rsRecords1.getString("Tipo").equals("Salario")){
                            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MMM-yyyy", new Locale("es", "ES"));
                            fechaPeriodo = formatoFecha.format(new Date(((Integer) anioCbx.getValue() - 1900), mes, diasPeriodo));
                            fechaPeriodo = fechaPeriodo.substring(0, 6) + fechaPeriodo.substring(7, 12);

                            sueldoBase = rsRecords1.getDouble("SueldoBase");
                            otrasDeducciones = sueldoBase - rsRecords1.getDouble("SalarioDevengado");
                            otrosSalarios = rsRecords1.getDouble("Bonificacion2") + rsRecords1.getDouble("Bonificacion3");

                        }else if (rsRecords1.getString("Tipo").contains("Liqui")) {
                            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MMM-yyyy", new Locale("es", "ES"));
                            fechaPeriodo = formatoFecha.format(rsRecords1.getDate("FechaEgreso"));
                            fechaPeriodo = fechaPeriodo.substring(0, 6) + fechaPeriodo.substring(7, 12);

                            sueldoBase = rsRecords1.getDouble("SueldoBase");
                            otrasDeducciones = sueldoBase - rsRecords1.getDouble("SalarioDevengado");
                            otrosSalarios = rsRecords1.getDouble("Bonificacion2") + rsRecords1.getDouble("Bonificacion3");
                        }else{
                            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MMM-yyyy", new Locale("es", "ES"));
                            // Si es aguinaldo
                            if(rsRecords1.getString("Descripcion").toUpperCase().contains("AGUIN") && mes == 10){
                                mes++;
                            }

                            fechaPeriodo = formatoFecha.format(new Date(((Integer) anioCbx.getValue() - 1900), mes, 14));
                            fechaPeriodo = fechaPeriodo.substring(0, 6) + fechaPeriodo.substring(7, 12);

                            sueldoBase = 0d;
                            otrasDeducciones = 0d;
                            otrosSalarios = 0d;

                            diasTrabajados = 0d;
                            horasTrabajadas = 0d;
                            horasExtraTrabajadas = 0d;
                        }

                        salarioTotal = sueldoBase + rsRecords1.getDouble("SalarioExtraordinario") + otrosSalarios + rsRecords1.getDouble("Vacaciones");
                        salarioDescuento = rsRecords1.getDouble("Descuento1") + rsRecords1.getDouble("Descuento3") + otrasDeducciones;

                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.ID_PLANILLA_PROPERTY.getDescripcion()).setValue(rsRecords1.getString("IdPlanilla"));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.FECHA_PROPERTY.getDescripcion()).setValue(fechaPeriodo);
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.NO_PAGO_PROPERTY.getDescripcion()).setValue(rsRecords1.getString("Cheque"));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.SALARIO_BASE_PROPERTY.getDescripcion()).setValue(Utileria.format(sueldoBase));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.DIAS_TRABAJODOS_PROPERTY.getDescripcion()).setValue(Utileria.format(diasTrabajados));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.HORAS_ORDINARIAS_PROPERTY.getDescripcion()).setValue(Utileria.format(horasTrabajadas));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.HORAS_EXTRAORDINARIAS_PROPERTY.getDescripcion()).setValue(Utileria.format(horasExtraTrabajadas));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.SALARIO_ORDINARIO_PROPERTY.getDescripcion()).setValue(Utileria.format((sueldoBase * (1-diasAsuetoSeptimos/30))));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.SALARIO_EXTRAORDINARIO_PROPERTY.getDescripcion()).setValue(Utileria.format(rsRecords1.getDouble("SalarioExtraordinario")));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.OTROS_SALARIOS_PROPERTY.getDescripcion()).setValue(Utileria.format(otrosSalarios));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.SALARIO_SEPTIMOS_ASUETOS_PROPERTY.getDescripcion()).setValue(Utileria.format((sueldoBase * (diasAsuetoSeptimos/30))));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.VACACIONES_PROPERTY.getDescripcion()).setValue(Utileria.format(rsRecords1.getDouble("Vacaciones")));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.SALARIO_TOTAL_PROPERTY.getDescripcion()).setValue(Utileria.format(salarioTotal));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.CUOTA_LABORAL_IGSS_PROPERTY.getDescripcion()).setValue(Utileria.format(rsRecords1.getDouble("Descuento1")));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.DESCUENTO_ISR_PROPERTY.getDescripcion()).setValue(Utileria.format(rsRecords1.getDouble("Descuento3")));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.OTRAS_DEDUCCIONES_PROPERTY.getDescripcion()).setValue(Utileria.format(otrasDeducciones));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.SALARIO_DESCUENTO_PROPERTY.getDescripcion()).setValue(Utileria.format(salarioTotal - salarioDescuento));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.BONOS_PROPERTY.getDescripcion()).setValue(Utileria.format(rsRecords1.getDouble("Bonificacion4") + rsRecords1.getDouble("Bonificacion5")));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.BONO_37_2001_PROPERTY.getDescripcion()).setValue(Utileria.format(rsRecords1.getDouble("Bonificacion1")));
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.DEVOLUCIONES_PROPERTY.getDescripcion()).setValue("0.00");
                        libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.SALARIO_LIQUIDO_PROPERTY.getDescripcion()).setValue(Utileria.format(rsRecords1.getDouble("LiquidoRecibir") + rsRecords1.getDouble("Descuento4")));

                        if (!rsRecords1.next()){
                            fin = true; // Asegurarme de romper el for
                            break; // Si no hay mas salir
                        }
                    }
                }
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar Combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    // Devuelde el nuemro de Domingos(Septimos) y Asuetosw
    public void getSeptimosAsuetos(){
        queryString1 = "SELECT af.*, ae.Duracion  ";
        queryString1 += "FROM asueto_empresa ae ";
        queryString1 += "INNER JOIN asueto_fecha af ON af.IdAsueto = ae.Id ";
        queryString1 += "AND af.Anio = " + anioCbx.getValue() + " ";
        queryString1 += "WHERE ae.IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
        queryString1 += "ORDER BY af.Mes";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery.executeQuery(queryString1);

            if(rsRecords1.next()){
                // Rotar por los 12 meses
                for (int mes = 0; mes < 12; mes++){
                    Double dias = 0d;
                    // Si hay un año seleccionado
                    if(!anioCbx.isEmpty()) {
                        Date date = new Date(((Integer) anioCbx.getValue() - 1900), mes, 1);
                        //System.out.println(utileria.getUltimoDiaDelMes(date));
                        //System.out.println(((date.getDay() + 6) % 7));
                        // DiaSemana = Lun: 0 | Mar: 1 | . . . | Sab: 5 | Dom: 6
                        dias = (double) ((utileria.getUltimoDiaDelMes(date) + ((date.getDay() + 6) % 7)) / 7); // (DiasMes[28, 31] + DiaSemana[0, 6]) / 7 sin aproximacion

                        while(rsRecords1.getInt("Mes") == (mes+1)){
                            //System.out.println(rsRecords1.getDate("Fecha"));
                            //System.out.println(rsRecords1.getDate("Fecha").getDay());
                            //System.out.println("---");
                            if(rsRecords1.getDate("Fecha").getDay() != 0){
                                dias = dias + rsRecords1.getFloat("Duracion");
                            }
                            if(!rsRecords1.next()) break; // Si no hay mas salir
                        }
                    }

                    asuetoArray[mes] = dias;
                }
            }
        } catch (Exception ex1) {
            System.out.println("Error al listar Años " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    // Insertar en la base de datos las fechas de asueto del año, si y solo si no se encuentra ese año.
    public void setFechaBaseDeDatos(){
        List<Object> anios = new ArrayList<>(anioCbx.getItemIds());

        queryString1 = "SELECT DISTINCT(anio) anio ";
        queryString1 += "FROM asueto_empresa ae ";
        queryString1 += "LEFT JOIN asueto_fecha af ON af.IdAsueto = ae.Id ";
        queryString1 += "AND af.Anio IN " + anioCbx.getItemIds().toString().replace("[", "(").replace("]", ")") + " ";
        queryString1 += "WHERE ae.IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery.executeQuery(queryString1);

            if(rsRecords1.next()){
                do {
                    if(anioCbx.getItemIds().contains(rsRecords1.getInt("anio"))){
                        anios.remove((Object) rsRecords1.getInt("anio"));
                    }
                }while (rsRecords1.next());
            }
        } catch (Exception ex1) {
            System.out.println("Error al listar Años " + ex1.getMessage());
            ex1.printStackTrace();
        }

        for(Object a : anios) {
            queryString2 = "INSERT INTO asueto_fecha (IdAsueto, Fecha, Anio, Mes, Dia)";
            queryString2 += "SELECT ae.id, ";
            queryString2 += "       STR_TO_DATE(CONCAT(" + a + ", '-', ae.MesDefault, '-', ae.DiaDefault), '%Y-%m-%d'), ";
            queryString2 += "       " + a + ", ";
            queryString2 += "       ae.MesDefault, ";
            queryString2 += "       ae.DiaDefault ";
            queryString2 += "FROM asueto_empresa ae ";
            queryString2 += "WHERE ae.IdEmpresa = " + empresaId;

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString2);

            } catch (Exception ex1) {
                System.out.println("Error actualizar asueto_fecha " + ex1.getMessage());
                ex1.printStackTrace();
            }
        }
    }

    private void anterior(){
        int index = proveedorList.indexOf((String) proveedorCbx.getValue());
        String proveedor = proveedorList.get(index - 1);
        proveedorCbx.select(proveedor);
    }

    private void siguiente(){
        int index = proveedorList.indexOf((String) proveedorCbx.getValue());
        String proveedor = proveedorList.get(index + 1);
        proveedorCbx.select(proveedor);
    }

    private void descargarLibroSalarioExcel() {
        if(fileDownloader[0] != null){
            excelBtn.removeExtension(fileDownloader[0]);
            excelBtn.removeClickListener((Button.ClickListener) excelBtn.getListeners(Button.ClickEvent.class).toArray()[0]);
        }
        fileName = "libro_salario_" + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanySmallName() + "_" + anioCbx.getValue() + "_" + proveedorCbx.getValue() + ".xls";

        // Crear la instancia de OnDemandStreamResource
        OnDemandStreamResource onDemandStreamResource = new OnDemandStreamResource();

        // Crear el StreamResource usando el StreamSource on-demand
        StreamResource resource = new StreamResource(onDemandStreamResource, filePath + "planilla_libro_salario.xls");
        resource.setMIMEType("application/vnd.ms-excel");

        // Asociar el recurso al FileDownloader una vez
        fileDownloader[0] = new FileDownloader(resource);
        fileDownloader[0].extend(excelBtn);

        // Listener para generar y actualizar el contenido al hacer clic
        excelBtn.addClickListener(event -> {
            fileName = "libro_salario_" + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanySmallName() + "_" + anioCbx.getValue() + "_" + proveedorCbx.getValue() + ".xls";
            onDemandStreamResource.generateExcelFile(); // Generar nuevo contenido
            resource.setFilename(fileName); // Actualizar nombre del archivo
            Notification.show("Archivo listo para descargar", Notification.Type.HUMANIZED_MESSAGE);
        });
    }

    private HSSFWorkbook generarLibroSalario(){
        String plantillaName = "";
        File plantllaTarget;

        try {
            new File(filePath).mkdirs();

            plantillaName = filePath + "plantilla_libro_salario.xls";

            plantllaTarget = new File(plantillaName);

            fileInputStream = new FileInputStream(plantllaTarget);

            workbook = new HSSFWorkbook(fileInputStream);

            sheet = workbook.getSheetAt(0);

            System.out.println("Libro Salario Proveedor: " + proveedorCbx.getValue());
        }
        catch(IOException ioE) {
            Notification.show("Error al intentar leer el archivo : " + plantillaName);
            ioE.printStackTrace();
            return new HSSFWorkbook();
        }


        CellStyle styleTitulo = workbook.createCellStyle();
        Font font1 = workbook.createFont();
        font1.setBold(true); // Negrita
        font1.setFontHeightInPoints((short) 14); // Tamaño de la fuente
        font1.setFontName("Times New Roman"); // Tipo de fuente
        styleTitulo.setFont(font1);
        styleTitulo.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle styleFolio = workbook.createCellStyle();
        Font font2 = workbook.createFont();
        font2.setBold(true); // Negrita
        font2.setFontHeightInPoints((short) 14); // Tamaño de la fuente
        font2.setFontName("Helvetica Narrow"); // Tipo de fuente
        styleFolio.setFont(font2);
        styleFolio.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle styleEmpleado1 = workbook.createCellStyle();
        Font font3 = workbook.createFont();
        font3.setFontHeightInPoints((short) 10); // Tamaño de la fuente
        font3.setFontName("Helvetica Narrow"); // Tipo de fuente
        styleEmpleado1.setFont(font3);
        styleEmpleado1.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle styleEmpleado2 = workbook.createCellStyle();
        Font font4 = workbook.createFont();
        font4.setFontHeightInPoints((short) 8); // Tamaño de la fuente
        font4.setFontName("Helvetica Narrow"); // Tipo de fuente
        styleEmpleado2.setFont(font4);
        styleEmpleado2.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle styleDatos = workbook.createCellStyle();
        Font font5 = workbook.createFont();
        font5.setFontHeightInPoints((short) 11); // Tamaño de la fuente
        font5.setFontName("Calibri"); // Tipo de fuente
        styleDatos.setFont(font5);
        styleDatos.setAlignment(CellStyle.ALIGN_CENTER);

        Cell cell = null;

        cell = sheet.getRow(4).createCell(0);
        cell.setCellValue(((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyName());
        cell.setCellStyle(styleTitulo);

        cell = sheet.getRow(5).createCell(0);
        cell.setCellValue(((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId());
        cell.setCellStyle(styleTitulo);

        cell = sheet.getRow(10).createCell(0);
        cell.setCellValue(((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "Nombre").getValue()));
        cell.setCellStyle(styleEmpleado1);

        cell = sheet.getRow(13).createCell(0);
        cell.setCellValue(((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "IGSS").getValue()));
        cell.setCellStyle(styleEmpleado1);

        cell = sheet.getRow(10).createCell(7);
        cell.setCellValue(((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "Edad").getValue()));
        cell.setCellStyle(styleEmpleado2);

        cell = sheet.getRow(13).createCell(6);
        cell.setCellValue(((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "DPI").getValue()));
        cell.setCellStyle(styleEmpleado2);

        cell = sheet.getRow(10).createCell(10);
        cell.setCellValue(((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "Genero").getValue()));
        cell.setCellStyle(styleEmpleado2);

        cell = sheet.getRow(10).createCell(13);
        cell.setCellValue(((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "Nacionalidad").getValue()));
        cell.setCellStyle(styleEmpleado2);

        cell = sheet.getRow(13).createCell(13);
        cell.setCellValue(((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "Inicio").getValue()));
        cell.setCellStyle(styleEmpleado2);

        cell = sheet.getRow(10).createCell(19);
        cell.setCellValue(((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "Cargo").getValue()));
        cell.setCellStyle(styleEmpleado2);

        cell = sheet.getRow(13).createCell(19);
        cell.setCellValue(((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "Final").getValue()));
        cell.setCellStyle(styleEmpleado2);

        int columna = 0;
        int fila = 19;

        // Por cada entrada (Pago/Salario)
        for (Object itemId : libroSalarioContainer.getItemIds()) {
            // Por cada columna
            for(LibroSalarioProperties propiedad : LibroSalarioProperties.values()){
                if(columna != 0){ // Saltar ID
                    if (sheet.getRow(fila) == null) {
                        sheet.createRow(fila);
                    }
                    String valor = (String) libroSalarioContainer.getContainerProperty(itemId, propiedad.getDescripcion()).getValue();
                    cell = sheet.getRow(fila).createCell(columna - 1);
                    if (valor.contains("-")){// Para la Fecha
                        cell.setCellValue(valor);
                    }else if(valor.isEmpty()){
                        cell.setCellValue(valor); // Si no hay nada
                    }else{
                        cell.setCellValue(Double.parseDouble(valor));
                    }
                    cell.setCellStyle(styleDatos);
                }
                columna++;
            }
            cell = sheet.getRow(fila).createCell(columna - 1);
            String valor = (String) libroSalarioContainer.getContainerProperty(itemId, LibroSalarioProperties.NO_PAGO_PROPERTY.getDescripcion()).getValue();
            if(valor.isEmpty()){
                cell.setCellValue(valor); // Si no hay nada
            }else{
                cell.setCellValue(Double.parseDouble(valor));
            }
            cell.setCellStyle(styleDatos);
            columna = 0;
            fila++;
        }
        return  workbook;
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        Page.getCurrent().setTitle("Libro de Salario");
    }

    public class OnDemandStreamResource implements StreamResource.StreamSource {
        private ByteArrayOutputStream outputStream;

        public void generateExcelFile() {
            try {
                // Crear el archivo Excel en memoria
                HSSFWorkbook workbook = generarLibroSalario(); // Método que genera el contenido del archivo
                File fileTarget = new File(filePath + fileName);
                generarLibroSalario();
                workbook.write(fileTarget);

                // Escribir el archivo Excel en un ByteArrayOutputStream
                outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public InputStream getStream() {
            if (outputStream == null) {
                return null;
            }
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }
}
