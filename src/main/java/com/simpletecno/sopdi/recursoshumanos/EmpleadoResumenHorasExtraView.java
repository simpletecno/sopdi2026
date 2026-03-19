package com.simpletecno.sopdi.recursoshumanos;

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
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
@SuppressWarnings("ALL")
public class EmpleadoResumenHorasExtraView extends VerticalLayout implements View {
    DateField fechaDt;

    public static final String IDBITACORA_PROPERTY = "Id";
    public static final String IDEMPLEADO_PROPERTY = "IdEmpleado";
    public static final String EMPLEADO_PROPERTY = "Nombre";
    public static final String CARGO_PROPERTY = "Cargo";
    public static final String LICENCIA_PROPERTY = "Licencia";
    public static final String SUSPENCION_PROPERTY = "SUSPENCION";
    public static final String HORASEXTRA_PROPERTY = "Hrs Extra";
    public static final String HORASEXTRAII_PROPERTY = "Hrs ExtraII";
    public static final String EVENTO_PROPERTY = "Eventos";
    public static final String DIASVACACIONES_PROPERTY = "Dias vacaciones";

    public IndexedContainer resumenContainer = new IndexedContainer();
    Grid resumenGrid;
    Grid.FooterRow footerRow;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    VerticalLayout mainLayout = new VerticalLayout();

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public EmpleadoResumenHorasExtraView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " RESUMEN HORAS EXTRA Y VACACIONES");
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

        createDetailsGrid();
        fillresumenGrid();

    }

    public void createDetailsGrid() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.addStyleName("rcorners2");
        detalleLayout.setSpacing(true);

        HorizontalLayout idexYEmpleadosLayout = new HorizontalLayout();
        idexYEmpleadosLayout.setWidth("100%");
        idexYEmpleadosLayout.addStyleName("rcorners3");
        idexYEmpleadosLayout.setSpacing(true);

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        fechaDt = new DateField("MES:");
        fechaDt.setValue(new Date());
        fechaDt.addStyleName(ValoTheme.DATEFIELD_LARGE);
        fechaDt.setWidth("100%");
        fechaDt.setDateFormat("MMM-yyyy");
        fechaDt.setEnabled(true);
        fechaDt.addValueChangeListener((event) -> {
            fillresumenGrid();
        });
        fechaDt.setResolution(Resolution.MONTH);
        fechaDt.setShowISOWeekNumbers(true);

        HorizontalLayout fechaLayout = new HorizontalLayout();
        fechaLayout.setSpacing(true);
        fechaLayout.addComponent(fechaDt);
        fechaLayout.setWidth("100%");
        fechaLayout.setComponentAlignment(fechaDt, Alignment.BOTTOM_CENTER);

        detalleLayout.addComponents(fechaLayout, idexYEmpleadosLayout, botonesLayout);

        resumenContainer.addContainerProperty(IDBITACORA_PROPERTY, String.class, null);
        resumenContainer.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, null);
        resumenContainer.addContainerProperty(EMPLEADO_PROPERTY, String.class, null);
        resumenContainer.addContainerProperty(CARGO_PROPERTY, String.class, null);
        resumenContainer.addContainerProperty(LICENCIA_PROPERTY, String.class, "0");
        resumenContainer.addContainerProperty(SUSPENCION_PROPERTY, String.class, "0");
        resumenContainer.addContainerProperty(HORASEXTRA_PROPERTY, String.class, "0");
        resumenContainer.addContainerProperty(HORASEXTRAII_PROPERTY, String.class, "0");
        resumenContainer.addContainerProperty(EVENTO_PROPERTY, String.class, "0");
        resumenContainer.addContainerProperty(DIASVACACIONES_PROPERTY, String.class, "0");

        resumenGrid = new Grid("RESUMEN DEL MES : " + Utileria.getFechaYYYYMM(fechaDt.getValue()), resumenContainer);
        resumenGrid.setImmediate(true);
        resumenGrid.setSelectionMode(Grid.SelectionMode.NONE);
        resumenGrid.setHeightMode(HeightMode.ROW);
        resumenGrid.setHeightByRows(15);
        resumenGrid.setWidth("100%");
        resumenGrid.setResponsive(true);
        resumenGrid.setSizeFull();

        resumenGrid.getColumn(IDBITACORA_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);
        resumenGrid.getColumn(IDEMPLEADO_PROPERTY).setExpandRatio(1);
        resumenGrid.getColumn(EMPLEADO_PROPERTY).setExpandRatio(3);
        resumenGrid.getColumn(CARGO_PROPERTY).setExpandRatio(2);
        resumenGrid.getColumn(LICENCIA_PROPERTY).setExpandRatio(1);
        resumenGrid.getColumn(SUSPENCION_PROPERTY).setExpandRatio(1);
        resumenGrid.getColumn(HORASEXTRA_PROPERTY).setExpandRatio(1);
        resumenGrid.getColumn(HORASEXTRAII_PROPERTY).setExpandRatio(1);
        resumenGrid.getColumn(EVENTO_PROPERTY).setExpandRatio(1);
        resumenGrid.getColumn(DIASVACACIONES_PROPERTY).setExpandRatio(1);

        resumenGrid.setCellStyleGenerator(
                (Grid.CellReference cellReference) -> {

                    if (HORASEXTRA_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else if (LICENCIA_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else if (SUSPENCION_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else if (HORASEXTRAII_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else if (EVENTO_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else if (DIASVACACIONES_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else {
                        return null;
                    }

                }
        );
        idexYEmpleadosLayout.addComponent(resumenGrid);

        Button exportAsistenciaBtn = new Button("Exportar RESUMEN a Excel");
        exportAsistenciaBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportAsistenciaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        exportAsistenciaBtn.setDescription("Exportar a Excel");
        exportAsistenciaBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (resumenContainer.size() == 0) {
                Notification notif = new Notification("No hay registros o asistencia.",
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

        botonesLayout.addComponent(exportAsistenciaBtn);
        botonesLayout.setComponentAlignment(exportAsistenciaBtn, Alignment.BOTTOM_LEFT);
        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);

        footerRow = resumenGrid.appendFooterRow();
        footerRow.getCell(EMPLEADO_PROPERTY).setText("Total");
        footerRow.getCell(LICENCIA_PROPERTY).setText("0.00");
        footerRow.getCell(SUSPENCION_PROPERTY).setText("0.00");
        footerRow.getCell(HORASEXTRA_PROPERTY).setText("0.00");
        footerRow.getCell(HORASEXTRAII_PROPERTY).setText("0.00");
        footerRow.getCell(EVENTO_PROPERTY).setText("0.00");
        footerRow.getCell(DIASVACACIONES_PROPERTY).setText("0.00");

        footerRow.getCell(LICENCIA_PROPERTY).setStyleName("centeralign");
        footerRow.getCell(SUSPENCION_PROPERTY).setStyleName("centeralign");
        footerRow.getCell(HORASEXTRA_PROPERTY).setStyleName("centeralign");
        footerRow.getCell(HORASEXTRAII_PROPERTY).setStyleName("centeralign");
        footerRow.getCell(EVENTO_PROPERTY).setStyleName("centeralign");
        footerRow.getCell(DIASVACACIONES_PROPERTY).setStyleName("centeralign");
    }
    public void fillresumenGrid() {
        resumenContainer.removeAllItems();
        resumenContainer.removeAllItems();

        double totalLicencia = 0.00;
        double totalSuspecion = 0.00;
        double totalHorasExtra = 0.00;
        double totalHorasExtraII = 0.00;
        double totalEventos = 0.00;
        double totalVacaciones = 0.00;

        java.util.GregorianCalendar today = new java.util.GregorianCalendar();
        today.setTime(fechaDt.getValue());

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT BITA.IdEmpleado, PROV.Nombre NombreEmpleado, PROV.Cargo, ";
            queryString += "SUM(IFNULL(BITA.HorasExtra, 0)) TOTHORASEXTRA, SUM(IFNULL(BITA.HorasExtraDoble,0)) TOTHORASEXTRADOBLE, ";
            queryString += "SUM(IFNULL(BITA.Eventos, 0)) TOTEVENTOS, SUM(IFNULL(BITA.DiasVacaciones,0)) TOTVACACIONES, ";
            queryString += "SUM(IF(BITA.Razon = 'Licencia', 1, 0)) TOTLICENCIA, ";
            queryString += "SUM(IF(BITA.Razon = 'Suspensión IGSS', 1, 0)) TOTSUSPENCION ";
            queryString += "FROM empleado_asistencia BITA ";
            queryString += "INNER JOIN proveedor_empresa PROV ON PROV.IdProveedor = BITA.IdEmpleado ";
            queryString += "WHERE Extract(YEAR_MONTH FROM BITA.Fecha) = " + Utileria.getFechaYYYYMM(fechaDt.getValue()) + " ";
            queryString += "AND PROV.Inhabilitado = 0 ";
            queryString += "AND PROV.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
            queryString += "GROUP BY BITA.IdEmpleado, PROV.Nombre, PROV.Cargo ";

System.out.println("queryRESUMENHORASEXTRA=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);

//System.out.println("DAY_OF_WEEK=" + today.get(java.util.GregorianCalendar.DAY_OF_WEEK));

            if(rsRecords.next()) { //NO EXISTE BITACORA PARA HOY, HAY QUE CREARLA
                Object itemId;
                do {
                    if((rsRecords.getDouble("TOTHORASEXTRA") + rsRecords.getDouble("TOTHORASEXTRADOBLE")
                        + rsRecords.getDouble("TOTEVENTOS") + rsRecords.getDouble("TOTVACACIONES")
                        + rsRecords.getDouble("TOTLICENCIA") + rsRecords.getDouble("TOTSUSPENCION")) > 0) {

                        itemId = resumenContainer.addItem();
                        resumenContainer.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdEmpleado"));
                        resumenContainer.getContainerProperty(itemId, EMPLEADO_PROPERTY).setValue(rsRecords.getString("NombreEmpleado"));
                        resumenContainer.getContainerProperty(itemId, CARGO_PROPERTY).setValue(rsRecords.getString("Cargo"));
                        resumenContainer.getContainerProperty(itemId, LICENCIA_PROPERTY).setValue(Utileria.formatSimple(rsRecords.getDouble("TOTLICENCIA")));
                        resumenContainer.getContainerProperty(itemId, SUSPENCION_PROPERTY).setValue(Utileria.formatSimple(rsRecords.getDouble("TOTSUSPENCION")));
                        resumenContainer.getContainerProperty(itemId, HORASEXTRA_PROPERTY).setValue(rsRecords.getString("TOTHORASEXTRA"));
                        resumenContainer.getContainerProperty(itemId, HORASEXTRAII_PROPERTY).setValue(rsRecords.getString("TOTHORASEXTRADOBLE"));
                        resumenContainer.getContainerProperty(itemId, EVENTO_PROPERTY).setValue(Utileria.formatSimple(rsRecords.getDouble("TOTEVENTOS")));
                        resumenContainer.getContainerProperty(itemId, DIASVACACIONES_PROPERTY).setValue(rsRecords.getString("TOTVACACIONES"));

                        totalLicencia += rsRecords.getDouble("TOTLICENCIA");
                        totalSuspecion += rsRecords.getDouble("TOTSUSPENCION");
                        totalHorasExtra += rsRecords.getDouble("TOTHORASEXTRA");
                        totalHorasExtraII += rsRecords.getDouble("TOTHORASEXTRADOBLE");
                        totalEventos += rsRecords.getDouble("TOTEVENTOS");
                        totalVacaciones += rsRecords.getDouble("TOTVACACIONES");
                    }

                } while (rsRecords.next());

                footerRow.getCell(LICENCIA_PROPERTY).setText(String.valueOf(totalLicencia));
                footerRow.getCell(SUSPENCION_PROPERTY).setText(String.valueOf(totalSuspecion));
                footerRow.getCell(HORASEXTRA_PROPERTY).setText(String.valueOf(totalHorasExtra));
                footerRow.getCell(HORASEXTRAII_PROPERTY).setText(String.valueOf(totalHorasExtraII));
                footerRow.getCell(EVENTO_PROPERTY).setText(String.valueOf(totalEventos));
                footerRow.getCell(DIASVACACIONES_PROPERTY).setText(String.valueOf(totalVacaciones));

            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla BITACORA DE EMPLEADOS : " + ex);
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    public boolean exportToExcel() {
        if (resumenGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(resumenGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;

            fileexport = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "_" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_ASISTENCIA.xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - RESUMEN HORAS EXTRA Y VACACIONES");
    }
}
