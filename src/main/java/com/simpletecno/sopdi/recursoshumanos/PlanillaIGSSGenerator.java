package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlanillaIGSSGenerator extends Window {


    String JURAMENTO = "juramento";

    Statement stQuery;
    PreparedStatement pstQuery;
    ResultSet rsRecords;
    String queryString;

    VerticalLayout mainForm;
    HorizontalLayout versionccx;
    HorizontalLayout botonesLayout;

    UI mainUI;

    ComboBox iggsVersiocbx;
    Button generarbtn;
    Button downloadBtn;

    String idPlanilla;

    int codigoDelCentro;
    int identificadorTipoPlanilla;
    String tiempoContrato;

    String encabezado = "";
    String cuerpo = "";
    String licencias = "";
    String suspendidos = "";
    String footer = "";

    List<String> empleados = new ArrayList<>();

    Date fechaIncio;
    Date fechaFin;


    public PlanillaIGSSGenerator(String idPlanilla){
        encabezado = "";
        cuerpo = "";
        licencias = "";
        suspendidos = "";
        footer = "";

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("35%");

        mainForm = new VerticalLayout();
        mainForm.setSpacing(true);
        mainForm.setResponsive(true);
        mainForm.setMargin(false);
        mainForm.setWidth("100%");

        versionccx = new HorizontalLayout();
        versionccx.setWidth("90%");
        botonesLayout = new HorizontalLayout();
        botonesLayout.setWidth("90%");

        iggsVersiocbx = new ComboBox("Version IGSS Planilla");
        iggsVersiocbx.setWidth("55%");
        iggsVersiocbx.setFilteringMode(FilteringMode.CONTAINS);
        iggsVersiocbx.addContainerProperty(JURAMENTO, String.class, "JURAMENTO");

        versionccx.addComponent(iggsVersiocbx);
        versionccx.setComponentAlignment(iggsVersiocbx, Alignment.TOP_LEFT);

        queryString = "SELECT FechaInicio, FechaFin ";
        queryString += "FROM planilla_encabezado ";
        queryString += "WHERE id = " + idPlanilla;
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
                fechaIncio = rsRecords.getDate("FechaInicio");
                fechaFin = rsRecords.getDate("FechaFin");
            }
        }catch (Exception ex){
            System.out.println("ERROR AL TRAER EL PLANILLA" + ex);
            ex.printStackTrace();
            Notification.show("ERROR AL TRAER EL PLANILLA", Notification.Type.ERROR_MESSAGE);
        }

        generarbtn = new Button("Generar");
        generarbtn.setIcon(FontAwesome.SAVE);
        generarbtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        generarbtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                encabezado = "";
                cuerpo = "";
                licencias = "";
                suspendidos = "";
                footer = "";

                generarHeader();
                generarCuerpo();
                generarAusencias();
                generarCola();
                generarArchivo();
            }
        });

        downloadBtn = new Button("Descargar Planilla");
        downloadBtn.setEnabled(false);

        botonesLayout.addComponents(generarbtn, downloadBtn);
        botonesLayout.setComponentAlignment(generarbtn, Alignment.MIDDLE_LEFT);
        botonesLayout.setComponentAlignment(downloadBtn, Alignment.MIDDLE_RIGHT);

        mainForm.addComponents(versionccx, botonesLayout);
        mainForm.setComponentAlignment(versionccx, Alignment.TOP_CENTER);
        mainForm.setComponentAlignment(botonesLayout, Alignment.BOTTOM_CENTER);

        this.mainUI = UI.getCurrent();

        this.idPlanilla = idPlanilla;
        setContent(mainForm);
        llenarCombox();
    }

    private void llenarCombox(){
        queryString = "SELECT * FROM igss_version_planilla";
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery.executeQuery(queryString);
            String select = "";
            if(rsRecords.next()){
                select = rsRecords.getString("Version");
                do {
                    iggsVersiocbx.addItem(rsRecords.getString("Version"));
                    iggsVersiocbx.setItemCaption(rsRecords.getString("Version"), rsRecords.getString("Version"));
                    iggsVersiocbx.getItem(rsRecords.getString("Version")).getItemProperty(JURAMENTO).setValue(rsRecords.getString("Juramento"));
                } while(rsRecords.next());
            }
            iggsVersiocbx.select(select);
        }catch (Exception ex){
            ex.printStackTrace();
            System.out.println("ERROR AL TRAER EL VERSIONES DE PLANILLA IGSS "+ ex);
        }
    }

    private void generarHeader(){

        queryString = "SELECT * FROM contabilidad_empresa WHERE IdEmpresa =" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()){
                identificadorTipoPlanilla = rsRecords.getInt("IdentificadorTipoPlanilla");
                codigoDelCentro = rsRecords.getInt("CodigoDelCentro");
                tiempoContrato = rsRecords.getString("TiempoContrato");

                encabezado += iggsVersiocbx.getValue() + "|" + Utileria.getStaticFecha() + "|" + rsRecords.getString("NoPatrono") + "|" + Utileria.getStaticMes(fechaIncio) + "|" + Utileria.getStaticAno(fechaIncio) + "|";
                encabezado += rsRecords.getString("NombrePatrono") + "|" + rsRecords.getString("NIT").replace("-", "") + "|" + rsRecords.getString("CorreoPatrono") + "|" + rsRecords.getString("PruebaOProduccion") + "\n";
                encabezado += "[Centros]\n";
                encabezado += codigoDelCentro + "|" + rsRecords.getString("NombrePatrono") + "|" + rsRecords.getString("Direccion") + "|" + rsRecords.getString("Zona");
                encabezado += "|" + rsRecords.getString("Telefono") + "||" + rsRecords.getString("NombreContacto") + "|" +rsRecords.getString("CorreoContacto");
                encabezado += "|" + rsRecords.getString("Departamento") + "|" + rsRecords.getString("Municipio") + "|" + rsRecords.getString("CodigoActividad") + "|\n";
                encabezado += "[tiposplanilla]\n";
                encabezado += identificadorTipoPlanilla + "|" + rsRecords.getString("DescripcionTipoPlanilla") + "|" + rsRecords.getString("TipoDeAfiliados") + "|";
                encabezado += rsRecords.getString("PeriodoPlanilla") + "|" + rsRecords.getString("TipoPlanillaLiquidacion") + "|" + rsRecords.getString("CodigoActividad");
                encabezado += "|" + rsRecords.getString("ClaseDePlanilla") + "|" + tiempoContrato + "|\n";
                encabezado += "[liquidaciones]\n";
                encabezado += codigoDelCentro + "|" + codigoDelCentro + "|" + Utileria.getStaticFecha(fechaIncio)+ "|" + Utileria.getStaticFecha(fechaFin) + "|" + "O" + "||\n";
            }

        }catch (Exception ex){
            System.out.println("ERROR AL TRAER EL ENCABEZADO DE EMPRESA:" + ex);
            ex.printStackTrace();
            Notification.show("ERROR AL TRAER EL ASISTENCIA DE EMPLEADO", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void generarCuerpo(){

        queryString = "SELECT p.*, (pd.SalarioDevengado + pd.SalarioExtraordinario) as SueldoYExtra, ";
        queryString +=       "(IF(p.FechaIngreso BETWEEN pe_fechas.FechaInicio AND pe_fechas.FechaFin, p.FechaIngreso, NULL)) as IngresoFecha, ";
        queryString +=       "(IF(p.FechaEgreso BETWEEN pe_fechas.FechaInicio AND pe_fechas.FechaFin, p.FechaEgreso, NULL)) as EgresoFecha ";
        queryString += "FROM planilla_detalle pd ";
        queryString += "INNER JOIN proveedor p ON pd.IdEmpleado = p.IDProveedor ";
        queryString += "INNER JOIN planilla_encabezado pe ON pd.IdPlanilla = pe.Id ";
        queryString += "INNER JOIN ( SELECT FechaInicio, FechaFin ";
        queryString +=              "FROM planilla_encabezado ";
        queryString +=              "WHERE id = " + idPlanilla;
        queryString +=            ") pe_fechas ON pe.FechaInicio BETWEEN pe_fechas.FechaInicio AND pe_fechas.FechaFin ";
        queryString += "WHERE pe.Tipo IN ('Salario', 'Liquidacion') ";
        queryString += "AND pe.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
        queryString += "AND pe.Estatus = 'GENERADA' ";
        queryString += "AND pd.idplanilla = " + idPlanilla + " ";
        queryString += "GROUP BY p.IDProveedor";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
                cuerpo += "[empleados]\n";

                do {
                    empleados.add(rsRecords.getString("IdProveedor"));

                    cuerpo += codigoDelCentro + "|" + rsRecords.getString("AfiliacionIGSS") + "|" + rsRecords.getString("PrimerNombre") + "|" + rsRecords.getString("SegundoNombre");
                    cuerpo += "|" + rsRecords.getString("PrimerApellido") + "|" + rsRecords.getString("SegundoApellido") + "|" + rsRecords.getString("ApellidoCasada") + "|";
                    cuerpo += Utileria.format(rsRecords.getDouble("SueldoYExtra")) + "|" + Utileria.getStaticFecha(rsRecords.getDate("IngresoFecha")) + "|" + Utileria.getStaticFecha(rsRecords.getDate("EgresoFecha"));
                    cuerpo += "|1|" + rsRecords.getString("Nit").replace("-", "") + "|" + rsRecords.getString("CodigoOcupacion") + "|" + rsRecords.getString("CondicionLaboral");
                    cuerpo += "|" + "|" + identificadorTipoPlanilla + "||" + tiempoContrato + "|" + Utileria.getStaticUltimoDiaDelMes(fechaIncio) + "|\n";

                } while (rsRecords.next());

                cuerpo.replace(" ", "");
            }
        }catch (Exception ex){
            System.out.println("ERROR AL TRAER EL PLANILLA" + ex);
            ex.printStackTrace();
            Notification.show("ERROR AL TRAER EL PLANILLA", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void generarAusencias(){
        String s = "";
        // Prepared Statment
        queryString =  "SELECT ea.Fecha, ea.Estatus, ea.Razon, p.* ";
        queryString += "FROM empleado_asistencia ea ";
        queryString += "INNER JOIN proveedor p ON p.IDProveedor = ea.IdEmpleado ";
        queryString += "WHERE IdEmpleado = ? "; // <-- se le asigna en el setString()
        queryString += "AND Fecha BETWEEN '" + Utileria.getFechaYYYYMMDD_1(fechaIncio) + "' AND '" + Utileria.getFechaYYYYMMDD_1(fechaFin) + "' ";
        queryString += "ORDER BY ea.Fecha ";

        suspendidos = "[suspendidos]\n";
        licencias = "[licencias]\n";

        try {
            pstQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
        for (String empleado : empleados) {
            pstQuery.setString(1, empleado);
            rsRecords = pstQuery.executeQuery();
            if (rsRecords.next()) {
                String razon = "";
                Date inicio = null;
                Date fin = null;

                // Contenido Genral
                s = identificadorTipoPlanilla + "|" + rsRecords.getString("AfiliacionIGSS") + "|" + rsRecords.getString("PrimerNombre") + "|" + rsRecords.getString("SegundoNombre");
                s += "|" + rsRecords.getString("PrimerApellido") + "|" + rsRecords.getString("SegundoApellido") + "|" + rsRecords.getString("ApellidoCasada") + "|";

                do {
                    if(razon.equals(rsRecords.getString("Razon"))) {
                        fin = rsRecords.getDate("Fecha");
                    }else{
                        if (!razon.equals("")) {

                            if (razon.equals("Suspensión IGSS")){ // Si fue por Suspencion
                                suspendidos += s;
                                suspendidos += Utileria.getStaticFecha(inicio) + "|" + Utileria.getStaticFecha(fin) + "|\n";
                            }else if(razon.equals("Licencia")){ // SI fue por Licencia
                                licencias += s;
                                licencias += Utileria.getStaticFecha(inicio) + "|" + Utileria.getStaticFecha(fin) + "|\n";
                            }

                        }
                        inicio = rsRecords.getDate("Fecha"); // Inicio del conteo del nuevo bloque
                        fin = rsRecords.getDate("Fecha");
                        razon = rsRecords.getString("Razon");// Razon del nuevo bloque
                    }

                    if(rsRecords.getString("Estatus").equals("DE BAJA")) break;

                } while (rsRecords.next());

                // Si termino el Mes y aun sigue con esa razon de ausencia
                if (razon.equals("Suspensión IGSS")){// Si fue por Suspencion
                    suspendidos += s;
                    suspendidos += Utileria.getStaticFecha(inicio) + "|" + Utileria.getStaticFecha(fin) + "|\n";
                }else if(razon.equals("Licencia")){ // SI fue por Licencia
                    licencias += s;
                    licencias += Utileria.getStaticFecha(inicio) + "|" + Utileria.getStaticFecha(fin) + "|\n";
                }
            }
        }
        }catch (Exception ex){
            System.out.println("ERROR AL TRAER EL ASISTENCIA DE EMPLEADO" + ex);
            ex.printStackTrace();
            Notification.show("ERROR AL TRAER EL ASISTENCIA DE EMPLEADO", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void generarCola(){
        footer = "[juramento]\n";
        footer += iggsVersiocbx.getContainerProperty(iggsVersiocbx.getValue(), JURAMENTO).getValue()+ "\n";
        footer += "[finplanilla]";
    }

    private void generarArchivo(){
        String filePath = VaadinService.getCurrent()
                .getBaseDirectory().getAbsolutePath() + "/projectfiles/";
        String fileName = filePath + "planilla_igss_" + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanySmallName().replaceAll(" ", "_").replaceAll(",", "_") +
                          "_" + Utileria.getStaticMes(fechaIncio) + "_" + Utileria.getStaticAno(fechaIncio) + ".txt";
        File fileTarget = new File(fileName);

        String fileContent = encabezado + cuerpo + suspendidos + licencias + footer;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileTarget))) {
            writer.write(fileContent);
        } catch (IOException e) {
            Notification.show("Error al crear el archivo: " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }

        System.out.println(fileContent);

        final byte[] content = fileContent.getBytes(StandardCharsets.UTF_8);

        final StreamResource resource = new StreamResource(() -> {
            return new ByteArrayInputStream(content);
        }, fileTarget.getName());

        FileDownloader fileDownloader = new FileDownloader(resource);
        fileDownloader.extend(downloadBtn);

        downloadBtn.setEnabled(true);


    }



}
