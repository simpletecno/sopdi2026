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

/**
 *
 * @author user
 */
@SuppressWarnings("ALL")
public class GrupoTrabajoRepPlanillaView extends VerticalLayout implements View {
    DateField fechaDt;

    public static final String IDEMPLEADO_PROPERTY = "IdEmpleado";
    public static final String EMPLEADO_PROPERTY = "Nombre";
    public static final String CARGO_PROPERTY = "Cargo";
    public static final String GRUPONOMBRE_PROPERTY = "Grupos";
    public static final String IDEX_PROPERTY = "IDEX";
    public static final String ESTATUS_PROPERTY = "Estatus";
    public static final String DIAS_PROPERTY = "DIAS";
    public static final String HORAS_EXTRA_PROPERTY = "Horas Extra";
    public static final String HORAS_EXTRAII_PROPERTY = "Horas Extra II";

    public IndexedContainer groupBitacoraRepContainer = new IndexedContainer();
    Grid groupBitacoraRepGrid;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    VerticalLayout mainLayout = new VerticalLayout();

    public GrupoTrabajoRepPlanillaView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label("REPORTE PARA PAGO DE PLANILLA");
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

        fillgroupBitacoraRepGrid();
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

        fechaDt = new DateField("Año y mes:");
        fechaDt.setValue(new java.util.Date());
        fechaDt.addStyleName(ValoTheme.DATEFIELD_LARGE);
        fechaDt.setWidth("15em");
        fechaDt.setDateFormat("MMM-yyyy");
        fechaDt.setEnabled(true);
        fechaDt.setResolution(Resolution.MONTH);
        fechaDt.addValueChangeListener((event) -> {
            fillgroupBitacoraRepGrid();
        });

        Button consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillgroupBitacoraRepGrid();
            }
        });

        HorizontalLayout fechaLayout = new HorizontalLayout();
        fechaLayout.setSpacing(true);
        fechaLayout.addComponents(fechaDt, consultarBtn);
        fechaLayout.setComponentAlignment(fechaDt, Alignment.BOTTOM_CENTER);
        fechaLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);

        detalleLayout.addComponents(fechaLayout, idexYEmpleadosLayout, botonesLayout);

        groupBitacoraRepContainer.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, null);
        groupBitacoraRepContainer.addContainerProperty(EMPLEADO_PROPERTY, String.class, null);
        groupBitacoraRepContainer.addContainerProperty(CARGO_PROPERTY, String.class, null);
        groupBitacoraRepContainer.addContainerProperty(GRUPONOMBRE_PROPERTY, String.class, null);
        groupBitacoraRepContainer.addContainerProperty(IDEX_PROPERTY, String.class, null);
        groupBitacoraRepContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        groupBitacoraRepContainer.addContainerProperty(DIAS_PROPERTY, String.class, null);
        groupBitacoraRepContainer.addContainerProperty(HORAS_EXTRA_PROPERTY, String.class, null);
        groupBitacoraRepContainer.addContainerProperty(HORAS_EXTRAII_PROPERTY, String.class, null);

        groupBitacoraRepGrid = new Grid("REPORTE PARA PLANILLA", groupBitacoraRepContainer);
        groupBitacoraRepGrid.setImmediate(true);
        groupBitacoraRepGrid.setSelectionMode(Grid.SelectionMode.NONE);
        groupBitacoraRepGrid.setHeightMode(HeightMode.ROW);
        groupBitacoraRepGrid.setHeightByRows(15);
        groupBitacoraRepGrid.setWidth("100%");
        groupBitacoraRepGrid.setResponsive(true);
        groupBitacoraRepGrid.setEditorBuffered(false);
        groupBitacoraRepGrid.setSizeFull();

        groupBitacoraRepGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (  DIAS_PROPERTY.equals(cellReference.getPropertyId())
                || HORAS_EXTRA_PROPERTY.equals(cellReference.getPropertyId())
                || HORAS_EXTRAII_PROPERTY.equals(cellReference.getPropertyId())
            ) {
                return "centeralign";
            } else {
                return null;
            }
        });

//        groupBitacoraRepGrid.getColumn(IDEMPLEADO_PROPERTY).setExpandRatio(1);
//        groupBitacoraRepGrid.getColumn(EMPLEADO_PROPERTY).setExpandRatio(3);
//        groupBitacoraRepGrid.getColumn(GRUPONOMBRE_PROPERTY).setExpandRatio(1);

        idexYEmpleadosLayout.addComponent(groupBitacoraRepGrid);

        Button printAsistenciaBtn = new Button("Imprimir ASISTENCIA (PDF)");
        printAsistenciaBtn.setIcon(FontAwesome.PRINT);
        printAsistenciaBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        printAsistenciaBtn.setDescription("Imprimir ASISTENCIA en PDF");
        printAsistenciaBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (groupBitacoraRepContainer.size() == 0) {
                Notification notif = new Notification("No hay bitacora o asistencia.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
            else {
                GrupoTrabajoRepPlanillaPDF grupoTrabajoRepPlanillaPDF =
                        new GrupoTrabajoRepPlanillaPDF(
                                Utileria.getFechaYYYYMM(fechaDt.getValue()),
                                groupBitacoraRepContainer
                        );
                mainUI.addWindow(grupoTrabajoRepPlanillaPDF);
                //
            }
        });

        botonesLayout.addComponent(printAsistenciaBtn);
        botonesLayout.setComponentAlignment(printAsistenciaBtn, Alignment.BOTTOM_CENTER);
        detalleLayout.setComponentAlignment(botonesLayout, Alignment.BOTTOM_CENTER);
        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);

    }

    public void fillgroupBitacoraRepGrid() {
        groupBitacoraRepContainer.removeAllItems();
        groupBitacoraRepContainer.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = " SELECT Count(*) Dias, BITA.Estatus, SUM(BITA.HorasExtra) TotHorasExtra, SUM(BITA.HorasExtraDoble) TotHorasExtraDoble, ";
            queryString += " BITA.Idex, PROV.IdProveedor, PROV.Nombre NombreEmpleado, PROV.GrupoTrabajo, PROV.Cargo";
            queryString += " FROM grupo_trabajo_bitacora BITA";
            queryString += " INNER JOIN proveedor PROV ON PROV.IdProveedor = BITA.IdEmpleado ";
            queryString += " WHERE Extract(YEAR_MONTH FROM BITA.Fecha) = " + Utileria.getFechaYYYYMM(fechaDt.getValue());
//            queryString += " AND   BITA.ESTATUS <> 'PRESENTE'";
//            queryString += " AND   PROV.GrupoTrabajo <> ''";
            queryString += " AND   PROV.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += " GROUP BY BITA.Idex, BITA.Estatus, PROV.IdProveedor, PROV.GrupoTrabajo, PROV.Cargo";
            queryString += " ORDER BY PROV.GrupoTrabajo, PROV.Nombre  ";

System.out.println("queryBITACORAREPPLANILLA=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                Object itemId;
                do {
                    itemId = groupBitacoraRepContainer.addItem();
                    groupBitacoraRepContainer.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    groupBitacoraRepContainer.getContainerProperty(itemId, EMPLEADO_PROPERTY).setValue(rsRecords.getString("NombreEmpleado"));
                    groupBitacoraRepContainer.getContainerProperty(itemId, GRUPONOMBRE_PROPERTY).setValue(rsRecords.getString("GrupoTrabajo"));
                    groupBitacoraRepContainer.getContainerProperty(itemId, CARGO_PROPERTY).setValue(rsRecords.getString("Cargo"));
                    groupBitacoraRepContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));
                    groupBitacoraRepContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    groupBitacoraRepContainer.getContainerProperty(itemId, DIAS_PROPERTY).setValue(rsRecords.getString("Dias"));
                    groupBitacoraRepContainer.getContainerProperty(itemId, HORAS_EXTRA_PROPERTY).setValue(rsRecords.getString("TotHorasExtra"));
                    groupBitacoraRepContainer.getContainerProperty(itemId, HORAS_EXTRAII_PROPERTY).setValue(rsRecords.getString("TotHorasExtraDoble"));
                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla BITACORA DE EMPLEADOS DEL GRUPO : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public boolean exportToExcel() {
        if (groupBitacoraRepGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(groupBitacoraRepGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
// produccion            fileexport = (empresa + "_" + empresaLbl.getValue().substring(5, empresaLbl.getValue().length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_INTEGRACION_CAMBIOS.xlsx").replaceAll(" ", "").replaceAll(",", "");
            fileexport = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "_" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_ASISTENCIA.xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - REPORTE PLANILLA DE TRABAJO");
    }
}
