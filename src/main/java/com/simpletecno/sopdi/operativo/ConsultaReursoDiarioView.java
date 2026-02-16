package com.simpletecno.sopdi.operativo;

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
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class ConsultaReursoDiarioView extends VerticalLayout implements View {
    DateField inicioDt;
    DateField finDt;

    static final String FECHA_PROPERTY = "Fecha";
    static final String RH1_PROPERTY = "RH1 Project";
    static final String RH2_PROPERTY = "RH2 Project";
    static final String RH1_C_PROPERTY = "RH1 Contratado";
    static final String RH2_C_PROPERTY = "RH2 Contratado";
    static final String RH1_D_PROPERTY = "Diferencia RH1";
    static final String RH2_D_PROPERTY = "Diferencia RH2";

    public IndexedContainer recursoDiarioContainer = new IndexedContainer();
    Grid recursoDiarioGrid;
    Grid.FooterRow footerRow;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords1;
    Statement stQuery2;
    ResultSet rsRecords2;
    String queryString;

    public ConsultaReursoDiarioView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(true);
        setSpacing(true);

        Label titleLbl = new Label("DISPONIBILIDAD DE RH1 Y RH2");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createDetails();

        fillRecursoDiarioGrid();

    }

    public void createDetails() {

        HorizontalLayout filtersLayout = new HorizontalLayout();
        filtersLayout.setWidth("100%");
        filtersLayout.addStyleName("rcorners2");
        filtersLayout.setSpacing(true);

        HorizontalLayout rhLayout = new HorizontalLayout();
        rhLayout.setWidth("100%");
        rhLayout.addStyleName("rcorners3");
        rhLayout.setSpacing(true);

        inicioDt = new DateField("FECHA DEL :");
        inicioDt.setValue(new java.util.Date());
        inicioDt.setDateFormat("dd/MMM/yyyy");
        inicioDt.setWidth("10em");
        inicioDt.addValueChangeListener(event -> {
            if (inicioDt.getValue().after(finDt.getValue())) {
                Notification.show("Fecha inicial es mayor que fecha final, revise!!", Notification.Type.WARNING_MESSAGE);
                return;
            }
//            fillIdexGrid();
        });

        Instant today = inicioDt.getValue().toInstant();
        Instant next15Days = today.plus(15, ChronoUnit.DAYS);
        finDt = new DateField("FECHA DE INICIO AL :");
        finDt.setValue(new java.util.Date());
        finDt.setValue(java.util.Date.from(next15Days));
        finDt.setDateFormat("dd/MMM/yyyy");
        finDt.setWidth("10em");
        finDt.addValueChangeListener(event -> {
            if (finDt.getValue().before(inicioDt.getValue())) {
                Notification.show("Fecha final es menor que fecha inicial, revise!!", Notification.Type.WARNING_MESSAGE);
                return;
            }
//            fillIdexGrid();
        });

        Button buscarBtn = new Button("Buscar");
        buscarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        buscarBtn.setIcon(FontAwesome.SEARCH);
        buscarBtn.addClickListener((event) -> {
            fillRecursoDiarioGrid();
        });

        filtersLayout.setSpacing(true);
        filtersLayout.setWidth("100%");
        filtersLayout.addComponents(inicioDt,finDt, buscarBtn);
        filtersLayout.setComponentAlignment(buscarBtn, Alignment.BOTTOM_CENTER);

        addComponents(filtersLayout, rhLayout);

        recursoDiarioContainer.addContainerProperty(FECHA_PROPERTY, String.class, "0");
        recursoDiarioContainer.addContainerProperty(RH1_PROPERTY, Integer.class, 0);
        recursoDiarioContainer.addContainerProperty(RH2_PROPERTY, Integer.class, 0);
        recursoDiarioContainer.addContainerProperty(RH1_C_PROPERTY, Integer.class, 0);
        recursoDiarioContainer.addContainerProperty(RH2_C_PROPERTY, Integer.class, 0);
        recursoDiarioContainer.addContainerProperty(RH1_D_PROPERTY, Integer.class, 0);
        recursoDiarioContainer.addContainerProperty(RH2_D_PROPERTY, Integer.class, 0);

        recursoDiarioGrid = new Grid(recursoDiarioContainer);
        recursoDiarioGrid.setImmediate(true);
        recursoDiarioGrid.setSelectionMode(Grid.SelectionMode.NONE);
        recursoDiarioGrid.setHeightMode(HeightMode.ROW);
        recursoDiarioGrid.setHeightByRows(15);
//        recursoDiarioGrid.setWidth("100%");
        recursoDiarioGrid.setResponsive(true);
        recursoDiarioGrid.setEditorBuffered(false);
        recursoDiarioGrid.setSizeFull();

        recursoDiarioGrid.getColumn(FECHA_PROPERTY).setExpandRatio(1);
        recursoDiarioGrid.getColumn(RH1_PROPERTY).setExpandRatio(1);
        recursoDiarioGrid.getColumn(RH2_PROPERTY).setExpandRatio(1);
        recursoDiarioGrid.getColumn(RH1_C_PROPERTY).setExpandRatio(1);
        recursoDiarioGrid.getColumn(RH1_C_PROPERTY).setExpandRatio(1);
        recursoDiarioGrid.getColumn(RH1_D_PROPERTY).setExpandRatio(1);
        recursoDiarioGrid.getColumn(RH1_D_PROPERTY).setExpandRatio(1);

        recursoDiarioGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (FECHA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if(RH1_PROPERTY.equals(cellReference.getPropertyId())) {
                if(Integer.valueOf(String.valueOf(cellReference.getValue())).intValue() < 0) {
                    return "centeralignred";
                }
                else {
                    return "centeralign";
                }
            } else if(RH2_PROPERTY.equals(cellReference.getPropertyId())) {
                if(Integer.valueOf(String.valueOf(cellReference.getValue())).intValue() < 0) {
                    return "centeralignred";
                }
                else {
                    return "centeralign";
                }
            } else if(RH1_C_PROPERTY.equals(cellReference.getPropertyId())) {
                if(Integer.valueOf(String.valueOf(cellReference.getValue())).intValue() < 0) {
                    return "centeralignred";
                }
                else {
                    return "centeralign";
                }
            } else if(RH2_C_PROPERTY.equals(cellReference.getPropertyId())) {
                if(Integer.valueOf(String.valueOf(cellReference.getValue())).intValue() < 0) {
                    return "centeralignred";
                }
                else {
                    return "centeralign";
                }
            } else if(RH1_D_PROPERTY.equals(cellReference.getPropertyId())) {
                if(Integer.valueOf(String.valueOf(cellReference.getValue())).intValue() < 0) {
                    return "centeralignred";
                }
                else {
                    return "centeralign";
                }
            } else if(RH2_D_PROPERTY.equals(cellReference.getPropertyId())) {
                if(Integer.valueOf(String.valueOf(cellReference.getValue())).intValue() < 0) {
                    return "centeralignred";
                }
                else {
                    return "centeralign";
                }
            } else {
                return null;
            }

        });

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        Button printPDFBtn = new Button("Imprimir (PDF)");
        printPDFBtn.setIcon(FontAwesome.FILE_PDF_O);
        printPDFBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        printPDFBtn.setDescription("Imprimir en PDF");
        printPDFBtn.addClickListener(e -> {
            if (recursoDiarioContainer.size() == 0) {
                Notification notif = new Notification("No hay datos.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
            else {
                ConsultaRecursoDiarioPDF consultaRecursoDiarioPDF =
                        new ConsultaRecursoDiarioPDF(
                                Utileria.getFechaDDMMYYYY(inicioDt.getValue()),
                                Utileria.getFechaDDMMYYYY(finDt.getValue()),
                                footerRow.getCell(RH2_C_PROPERTY).getText(),
                                recursoDiarioContainer
                        );
                mainUI.addWindow(consultaRecursoDiarioPDF);

            }
        });

        Button exportExcelBtn = new Button("Exportar ASISTENCIA a Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        exportExcelBtn.setDescription("Exportar a Excel");
        exportExcelBtn.addClickListener(e -> {
            if (recursoDiarioContainer.size() == 0) {
                Notification notif = new Notification("No hay datos.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
            else {
                exportToExcel();
            }
        });

        botonesLayout.addComponents(printPDFBtn, exportExcelBtn);
        addComponent(botonesLayout);
        setComponentAlignment(botonesLayout, Alignment.BOTTOM_CENTER);

        footerRow = recursoDiarioGrid.appendFooterRow();
        footerRow.getCell(RH2_PROPERTY).setText("Total Contratado");
        footerRow.join(RH1_C_PROPERTY, RH2_C_PROPERTY).setStyleName("centeralign");
        footerRow.getCell(RH2_C_PROPERTY).setText("0");

        rhLayout.addComponent(recursoDiarioGrid);
    }

    public void fillRecursoDiarioGrid() {
        recursoDiarioContainer.removeAllItems();

        if (inicioDt.getValue().after(finDt.getValue())) {
            Notification.show("Fecha inicial es mayor que fecha final, revise!!", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (finDt.getValue().before(inicioDt.getValue())) {
            Notification.show("Fecha final es menor que fecha inicial, revise!!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        long daysDiff = 0;

        try {
            java.util.GregorianCalendar dateBefore = new java.util.GregorianCalendar();
            dateBefore.setTime(inicioDt.getValue());

            java.util.GregorianCalendar dateAfter = new java.util.GregorianCalendar();
            dateAfter.setTime(finDt.getValue());

            long dateBeforeInMs = dateBefore.getTimeInMillis();
            long dateAfterInMs = dateAfter.getTimeInMillis();

            long timeDiff = Math.abs(dateAfterInMs - dateBeforeInMs);

            daysDiff = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

        } catch(Exception e){
            e.printStackTrace();
            Notification.show("ERROR : al calcular los dias : " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
            return;
        }
        footerRow.getCell(RH2_C_PROPERTY).setText("0");

        Instant esteDia = inicioDt.getValue().toInstant();

        int RH1_C = getRhContratado("RH1");
        int RH2_C = getRhContratado("RH2");

        footerRow.getCell(RH2_C_PROPERTY).setText(String.valueOf(RH1_C + RH2_C));

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            recursoDiarioGrid.setCaption("RECURSO DIARIO REQUERIDO Y CONTRATADO");

            for(int dia = 1; dia <= daysDiff+1; dia++) {  //POR CADA DIA

                Object itemId;

                int RH1=0, RH2=0;
                itemId = recursoDiarioContainer.addItem();

                recursoDiarioContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaYYYYMMDD_1(java.util.Date.from(esteDia)));
                recursoDiarioContainer.getContainerProperty(itemId, RH1_PROPERTY).setValue(0);
                recursoDiarioContainer.getContainerProperty(itemId, RH2_PROPERTY).setValue(0);

                recursoDiarioContainer.getContainerProperty(itemId, RH1_C_PROPERTY).setValue(RH1_C);
                recursoDiarioContainer.getContainerProperty(itemId, RH2_C_PROPERTY).setValue(RH2_C);

                recursoDiarioContainer.getContainerProperty(itemId, RH1_D_PROPERTY).setValue(RH1_C);
                recursoDiarioContainer.getContainerProperty(itemId, RH2_D_PROPERTY).setValue(RH2_C);

                queryString = "SELECT SUM(PTA.RH1) TotRH1, SUM(PTA.RH2) TotRH2 ";
                queryString += " FROM  project_tarea PTA";
                queryString += " INNER JOIN project PRJ ON PRJ.Id = PTA.IdProject";
                queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(java.util.Date.from(esteDia)) + "'";
                queryString += " BETWEEN PTA.FechaInicio AND PTA.FechaFin ";
                queryString += " AND UPPER(PRJ.Estatus) = 'ACTIVO'";
                queryString += " HAVING TotRH1 > 0";

//System.out.println("queryRECURSODIARIO=" + queryString);

                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado
                    RH1 = rsRecords.getInt("TotRH1");
                    RH2 = rsRecords.getInt("TotRH2");

                    recursoDiarioContainer.getContainerProperty(itemId, RH1_PROPERTY).setValue(RH1);
                    recursoDiarioContainer.getContainerProperty(itemId, RH2_PROPERTY).setValue(RH2);

                    recursoDiarioContainer.getContainerProperty(itemId, RH1_D_PROPERTY).setValue((RH1_C - RH1));
                    recursoDiarioContainer.getContainerProperty(itemId, RH2_D_PROPERTY).setValue((RH2_C - RH2));

                }
                esteDia = esteDia.plus(1, ChronoUnit.DAYS);
            }// end for

        } catch (Exception ex) {
            System.out.println("Error al listar tabla RECURSO DIARIO  : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public int getRhContratado(String cargo) {

        String queryString;

        //2. Leer los registros de tabla proveedor donde cargo = RH1 o RH2 (done)
        queryString = "SELECT COUNT(*) RHCONTRATADO";
        queryString += " FROM proveedor";
        queryString += " WHERE EsPlanilla = 1";
        queryString += " AND Cargo = '" + cargo + "'";
        queryString += " AND EstatusTrabajo NOT IN ('DE BAJA', 'AUSENTE')";
        queryString += " AND Inhabilitado  = 0";
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

        int cantidad = 0;

        try {

            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { // POR CADA EMPLEADO RH1 o RH2
                cantidad = rsRecords1.getInt("RHCONTRATADO");
            }
        } catch (Exception ex) {
            Logger.getLogger(ConsultaReursoDiarioView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de empleados : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de RH..!", Notification.Type.ERROR_MESSAGE);
        }
        return cantidad;
    }

    public boolean exportToExcel() {
        if (recursoDiarioGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(recursoDiarioGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
// produccion            fileexport = (empresa + "_" + empresaLbl.getValue().substring(5, empresaLbl.getValue().length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_INTEGRACION_CAMBIOS.xlsx").replaceAll(" ", "").replaceAll(",", "");
            fileexport = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "_" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_DISPONIBILIDAD_RH.xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - DISPONIBILIDAD DE RH");
    }
}
