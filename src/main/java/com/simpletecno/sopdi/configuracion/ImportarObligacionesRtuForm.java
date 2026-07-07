package com.simpletecno.sopdi.configuracion;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ventana modal que lee las <b>Obligaciones fiscales</b> de una Constancia de
 * Inscripción y Actualización al Registro Tributario Unificado (RTU) de la SAT
 * de Guatemala (PDF) y las registra para una empresa contable en la tabla
 * {@code contabilidad_empresa_obligacion}.
 *
 * <p>El PDF del RTU es vertical y el texto se extrae carácter por carácter. El
 * parser reconstruye la tabla de "Obligaciones" que aparece bajo cada afiliación
 * (IVA, ISR, ISO): detecta el título de la afiliación y el encabezado de la
 * tabla (No. / Frecuencia de pago / Nombre Obligación / Código Formulario),
 * auto-calibra los límites de columna a partir del encabezado y agrupa el
 * contenido en filas usando el número de fila como ancla. El impuesto se arrastra
 * entre páginas para soportar tablas que continúan en la página siguiente.</p>
 *
 * @author Jose Aguirre
 */
public class ImportarObligacionesRtuForm extends Window {

    private static final long serialVersionUID = 1L;

    private final String idEmpresa;
    private final Runnable onImported;
    private final UI mainUI;

    private final List<Obligacion> obligaciones = new ArrayList<Obligacion>();

    private Table previewTable;
    private Label resumenLbl;
    private Button importarBtn;
    private MultiFileUpload upload;

    public ImportarObligacionesRtuForm(String idEmpresa, String nombreEmpresa, Runnable onImported) {
        this.idEmpresa = idEmpresa;
        this.onImported = onImported;
        this.mainUI = UI.getCurrent();

        setCaption("Cargar Obligaciones fiscales (RTU SAT)"
                + (nombreEmpresa != null && !nombreEmpresa.trim().isEmpty() ? " - " + nombreEmpresa : ""));
        setModal(true);
        setWidth("820px");
        setHeight("80%");
        center();

        construir();
    }

    private void construir() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(new MarginInfo(true, true, true, true));
        layout.setSpacing(true);
        layout.setSizeFull();

        Label infoLbl = new Label("Suba el PDF de la Constancia RTU de la SAT. Se leerán las "
                + "obligaciones fiscales (IVA, ISR, ISO) y se registrarán para la empresa. "
                + "Al importar se reemplazan las obligaciones registradas anteriormente.");
        infoLbl.setWidth("100%");

        previewTable = new Table();
        previewTable.setSizeFull();
        previewTable.addContainerProperty("Impuesto", String.class, "");
        previewTable.addContainerProperty("No.", String.class, "");
        previewTable.addContainerProperty("Frecuencia", String.class, "");
        previewTable.addContainerProperty("Obligación", String.class, "");
        previewTable.addContainerProperty("Formulario", String.class, "");
        previewTable.setColumnWidth("Impuesto", 70);
        previewTable.setColumnWidth("No.", 40);
        previewTable.setColumnWidth("Frecuencia", 90);
        previewTable.setColumnExpandRatio("Obligación", 1);
        previewTable.setColumnExpandRatio("Formulario", 2);

        resumenLbl = new Label();
        resumenLbl.addStyleName(ValoTheme.LABEL_BOLD);

        UploadFinishedHandler handler = (InputStream stream, String fileName, String mimeType, long length) -> {
            procesarArchivo(stream, fileName);
        };
        upload = new MultiFileUpload(handler, new UploadStateWindow(), false);
        upload.setIcon(FontAwesome.FILE_PDF_O);
        upload.getSmartUpload().setUploadButtonCaptions("Seleccionar PDF", "");
        // Sugerir solo PDF en el selector de archivos del navegador.
        JavaScript.getCurrent().execute(
                "var fu=document.getElementsByClassName('gwt-FileUpload');"
                + "if(fu.length){fu[fu.length-1].setAttribute('accept','.pdf');}");

        importarBtn = new Button("Registrar obligaciones", FontAwesome.SAVE);
        importarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        importarBtn.setEnabled(false);
        importarBtn.addClickListener(e -> confirmarImportacion());

        Button cerrarBtn = new Button("Cerrar", FontAwesome.CLOSE);
        cerrarBtn.addClickListener(e -> close());

        HorizontalLayout barra = new HorizontalLayout(upload, importarBtn, cerrarBtn);
        barra.setSpacing(true);
        barra.setComponentAlignment(upload, Alignment.MIDDLE_LEFT);
        barra.setComponentAlignment(importarBtn, Alignment.MIDDLE_LEFT);
        barra.setComponentAlignment(cerrarBtn, Alignment.MIDDLE_LEFT);

        layout.addComponents(infoLbl, barra, resumenLbl, previewTable);
        layout.setExpandRatio(previewTable, 1);

        setContent(layout);
    }

    /** Guarda el stream subido a un archivo temporal y lo parsea. */
    private void procesarArchivo(InputStream stream, String fileName) {
        File tmp = null;
        try {
            tmp = File.createTempFile("sat_rtu_", ".pdf");
            byte[] buffer = new byte[Math.max(stream.available(), 8192)];
            try (OutputStream out = new FileOutputStream(tmp)) {
                int n;
                while ((n = stream.read(buffer)) > 0) {
                    out.write(buffer, 0, n);
                }
            }

            obligaciones.clear();
            obligaciones.addAll(parsearRtu(tmp));

            previewTable.removeAllItems();
            int i = 1;
            for (Obligacion o : obligaciones) {
                previewTable.addItem(new Object[]{
                        o.impuesto, o.numero, o.frecuencia, o.nombre, o.formulario}, i++);
            }

            if (obligaciones.isEmpty()) {
                resumenLbl.setValue("No se encontraron obligaciones en el PDF. ¿Es una Constancia RTU de la SAT?");
                importarBtn.setEnabled(false);
                Notification.show("No se reconocieron obligaciones en el archivo.",
                        Notification.Type.WARNING_MESSAGE);
            } else {
                resumenLbl.setValue("Se detectaron " + obligaciones.size()
                        + " obligaciones. Revise y presione Registrar obligaciones.");
                importarBtn.setEnabled(true);
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportarObligacionesRtuForm.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            Notification.show("Error al leer el PDF: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        } finally {
            try { if (stream != null) stream.close(); } catch (Exception ignored) { }
            if (tmp != null) {
                tmp.delete();
            }
        }
    }

    private void confirmarImportacion() {
        if (obligaciones.isEmpty()) {
            Notification.show("No hay obligaciones para registrar.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        ConfirmDialog.show(UI.getCurrent(), "Confirme:",
                "¿Registrar " + obligaciones.size() + " obligaciones para la empresa? "
                + "Se reemplazarán las obligaciones registradas anteriormente.", "SI", "NO",
                dialog -> {
                    if (dialog.isConfirmed()) {
                        guardar();
                    }
                });
    }

    /** Reemplaza las obligaciones de la empresa por las leídas del PDF. */
    private void guardar() {
        String insert = "INSERT INTO contabilidad_empresa_obligacion "
                + " (IdEmpresa, Impuesto, Numero, Frecuencia, NombreObligacion, CodigoFormulario, Estatus) "
                + " VALUES (?,?,?,?,?,?, 'ACTIVO')";
        try {
            Connection conn = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection();
            crearTablaSiNoExiste(conn);

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM contabilidad_empresa_obligacion WHERE IdEmpresa = " + idEmpresa);
            }

            int insertados = 0;
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                for (Obligacion o : obligaciones) {
                    ps.setInt(1, Integer.parseInt(idEmpresa));
                    ps.setString(2, o.impuesto);
                    if (o.numero != null && o.numero.matches("\\d+")) {
                        ps.setInt(3, Integer.parseInt(o.numero));
                    } else {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    }
                    ps.setString(4, o.frecuencia);
                    ps.setString(5, o.nombre);
                    ps.setString(6, o.formulario);
                    ps.executeUpdate();
                    insertados++;
                }
            }

            Notification.show("Obligaciones registradas: " + insertados + ".",
                    Notification.Type.HUMANIZED_MESSAGE);

            if (onImported != null) {
                onImported.run();
            }
            close();

        } catch (Exception ex) {
            Logger.getLogger(ImportarObligacionesRtuForm.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            Notification.show("Error al registrar las obligaciones: " + ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
        }
    }

    /** Crea la tabla de obligaciones si aún no existe. */
    static void crearTablaSiNoExiste(Connection conn) throws Exception {
        String ddl = "CREATE TABLE IF NOT EXISTS contabilidad_empresa_obligacion ("
                + " IdObligacion INT NOT NULL AUTO_INCREMENT,"
                + " IdEmpresa INT NOT NULL,"
                + " Impuesto VARCHAR(20) NULL,"
                + " Numero INT NULL,"
                + " Frecuencia VARCHAR(40) NULL,"
                + " NombreObligacion VARCHAR(255) NULL,"
                + " CodigoFormulario VARCHAR(600) NULL,"
                + " Estatus VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',"
                + " FechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + " PRIMARY KEY (IdObligacion),"
                + " KEY idx_ce_obligacion_empresa (IdEmpresa)"
                + " )";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(ddl);
        }
    }

    // ------------------------------------------------------------------
    //  Parser del RTU (iText 5)
    // ------------------------------------------------------------------

    private static final float LINE_TOL = 2.5f; // tolerancia de Y para agrupar en líneas
    private static final float PAD = 5f;        // margen para límites de columna
    private static final float HDR_TOL = 9f;    // rango de Y del bloque de encabezado
    private static final float REGION_END = 30f;// piso de página cuando no hay corte de sección

    /** Una obligación fiscal leída del RTU. */
    static class Obligacion {
        String impuesto;    // IVA / ISR / ISO
        String numero;      // No. dentro de la afiliación
        String frecuencia;  // Frecuencia de pago
        String nombre;      // Nombre Obligación
        String formulario;  // Código Formulario
    }

    /** Un carácter con su posición. */
    private static class Ch {
        final float x;
        final float y;
        final String t;
        Ch(float x, float y, String t) { this.x = x; this.y = y; this.t = t; }
    }

    private static final Comparator<Ch> POR_X = new Comparator<Ch>() {
        @Override public int compare(Ch a, Ch b) { return Float.compare(a.x, b.x); }
    };

    /** Recolecta los caracteres de una página con sus coordenadas. */
    private static class ColeccionChars implements RenderListener {
        final List<Ch> chars = new ArrayList<Ch>();
        @Override public void beginTextBlock() { }
        @Override public void endTextBlock() { }
        @Override public void renderImage(ImageRenderInfo renderInfo) { }
        @Override public void renderText(TextRenderInfo info) {
            String t = info.getText();
            if (t == null || t.isEmpty()) {
                return;
            }
            com.itextpdf.text.pdf.parser.Vector p = info.getBaseline().getStartPoint();
            chars.add(new Ch(p.get(0), p.get(1), t));
        }
    }

    static List<Obligacion> parsearRtu(File pdf) throws Exception {
        List<Obligacion> resultado = new ArrayList<Obligacion>();
        PdfReader reader = new PdfReader(pdf.getAbsolutePath());
        try {
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            String impuestoActual = null; // se arrastra entre páginas

            for (int pg = 1; pg <= reader.getNumberOfPages(); pg++) {
                ColeccionChars col = new ColeccionChars();
                parser.processContent(pg, col);
                List<Ch> chars = col.chars;
                if (chars.isEmpty()) {
                    continue;
                }
                List<List<Ch>> lineas = agruparLineas(chars);

                // Marcadores en orden vertical: títulos de afiliación y encabezados de tabla.
                List<Object[]> markers = new ArrayList<Object[]>(); // {y, "T"|"H", impuesto|cols}
                for (List<Ch> ln : lineas) {
                    float y = ln.get(0).y;
                    String txt = concat(ln);
                    String up = txt.toUpperCase();
                    String imp = null;
                    if (up.contains("(IVA)")) {
                        imp = "IVA";
                    } else if (up.contains("(ISR)")) {
                        imp = "ISR";
                    } else if (up.contains("(ISO)")) {
                        imp = "ISO";
                    }
                    if (imp != null) {
                        markers.add(new Object[]{y, "T", imp});
                        continue;
                    }
                    if (txt.contains("Nombre") && (txt.contains("Código") || txt.contains("Codigo"))) {
                        float[] cols = computeCols(chars, y);
                        if (cols != null) {
                            markers.add(new Object[]{y, "H", cols});
                        }
                    }
                }
                Collections.sort(markers, new Comparator<Object[]>() {
                    @Override public int compare(Object[] a, Object[] b) {
                        return Float.compare((Float) b[0], (Float) a[0]);
                    }
                });

                for (Object[] m : markers) {
                    if ("T".equals(m[1])) {
                        impuestoActual = (String) m[2];
                    } else {
                        float[] cols = (float[]) m[2];
                        float xNo = cols[0], xFrec = cols[1], xNombre = cols[2], xCodigo = cols[3], headerBottomY = cols[4];
                        float regionBottom = computeRegionBottom(lineas, headerBottomY, xNo);
                        resultado.addAll(parseRows(chars, xFrec, xNombre, xCodigo,
                                headerBottomY, regionBottom, impuestoActual));
                    }
                }
            }
        } finally {
            reader.close();
        }
        return resultado;
    }

    /**
     * Deriva los bordes de columna del bloque de encabezado ubicado alrededor de
     * {@code yHmain}. Devuelve {xNo, xFrec, xNombre, xCodigo, headerBottomY} o
     * {@code null} si no reconoce el encabezado.
     */
    private static float[] computeCols(List<Ch> chars, float yHmain) {
        List<Ch> block = new ArrayList<Ch>();
        for (Ch c : chars) {
            if (Math.abs(c.y - yHmain) <= HDR_TOL) {
                block.add(c);
            }
        }
        List<List<Ch>> blines = agruparLineas(block);
        float xNo = Float.NaN, xFrec = Float.NaN, xNombre = Float.NaN, xCodigo = Float.NaN, minY = Float.MAX_VALUE;
        for (List<Ch> ln : blines) {
            minY = Math.min(minY, ln.get(0).y);
            List<Ch> s = new ArrayList<Ch>(ln);
            Collections.sort(s, POR_X);
            float[] xs = new float[s.size()];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.size(); i++) {
                xs[i] = s.get(i).x;
                sb.append(s.get(i).t);
            }
            String str = sb.toString();
            int i;
            if (Float.isNaN(xNo) && (i = str.indexOf("No")) >= 0) {
                xNo = xs[i];
            }
            if (Float.isNaN(xFrec) && (i = str.indexOf("Frec")) >= 0) {
                xFrec = xs[i];
            }
            if (Float.isNaN(xNombre) && (i = str.indexOf("Nombre")) >= 0) {
                xNombre = xs[i];
            }
            if (Float.isNaN(xCodigo)) {
                i = str.indexOf("Código");
                if (i < 0) {
                    i = str.indexOf("Codigo");
                }
                if (i >= 0) {
                    xCodigo = xs[i];
                }
            }
        }
        if (Float.isNaN(xFrec) || Float.isNaN(xNombre) || Float.isNaN(xCodigo)) {
            return null;
        }
        if (Float.isNaN(xNo)) {
            xNo = xFrec - 20;
        }
        return new float[]{xNo, xFrec, xNombre, xCodigo, minY};
    }

    /**
     * La tabla de obligaciones termina en la primera línea (debajo del encabezado)
     * cuyo contenido empieza a la izquierda de la columna "No." (título de sección,
     * otro apartado o pie de página).
     */
    private static float computeRegionBottom(List<List<Ch>> lineas, float headerBottomY, float xNo) {
        for (List<Ch> ln : lineas) {
            float y = ln.get(0).y;
            if (y >= headerBottomY - 0.1f) {
                continue; // sólo debajo del encabezado
            }
            float minX = Float.MAX_VALUE;
            for (Ch c : ln) {
                minX = Math.min(minX, c.x);
            }
            if (minX < xNo - 3f) {
                return y;
            }
        }
        return REGION_END;
    }

    /** Reconstruye las filas de una tabla de obligaciones entre el encabezado y el fin de sección. */
    private static List<Obligacion> parseRows(List<Ch> chars, float xFrec, float xNombre, float xCodigo,
                                              float headerBottomY, float regionBottom, String impuesto) {
        List<Obligacion> res = new ArrayList<Obligacion>();
        List<Ch> region = new ArrayList<Ch>();
        for (Ch c : chars) {
            if (c.y < headerBottomY - 0.1f && c.y > regionBottom + 0.1f) {
                region.add(c);
            }
        }
        if (region.isEmpty()) {
            return res;
        }

        // Anclas de fila: líneas con un número en la columna "No.".
        List<List<Ch>> rlines = agruparLineas(region);
        List<Float> anchorY = new ArrayList<Float>();
        List<String> nums = new ArrayList<String>();
        for (List<Ch> ln : rlines) {
            List<Ch> s = new ArrayList<Ch>();
            for (Ch c : ln) {
                if (c.x < xFrec - PAD) {
                    s.add(c);
                }
            }
            Collections.sort(s, POR_X);
            StringBuilder sb = new StringBuilder();
            for (Ch c : s) {
                sb.append(c.t);
            }
            String n = sb.toString().trim();
            if (n.matches("\\d{1,2}")) {
                anchorY.add(ln.get(0).y);
                nums.add(n);
            }
        }
        if (anchorY.isEmpty()) {
            return res;
        }

        for (int i = 0; i < anchorY.size(); i++) {
            float top = (i == 0) ? headerBottomY : (anchorY.get(i - 1) + anchorY.get(i)) / 2f;
            float bot = (i == anchorY.size() - 1) ? regionBottom : (anchorY.get(i) + anchorY.get(i + 1)) / 2f;
            List<Ch> cell = new ArrayList<Ch>();
            for (Ch c : region) {
                if (c.y < top && c.y > bot) {
                    cell.add(c);
                }
            }
            Obligacion o = new Obligacion();
            o.impuesto = impuesto;
            o.numero = nums.get(i);
            o.frecuencia = reconstruir(cell, xFrec - PAD, xNombre - PAD);
            o.nombre = reconstruir(cell, xNombre - PAD, xCodigo - PAD);
            o.formulario = reconstruir(cell, xCodigo - PAD, Float.MAX_VALUE);
            res.add(o);
        }
        return res;
    }

    /** Une los caracteres de una banda de X (una columna) respetando saltos de línea. */
    private static String reconstruir(List<Ch> chars, float xLo, float xHi) {
        List<Ch> band = new ArrayList<Ch>();
        for (Ch c : chars) {
            if (c.x >= xLo && c.x < xHi) {
                band.add(c);
            }
        }
        if (band.isEmpty()) {
            return "";
        }
        List<List<Ch>> ls = agruparLineas(band);
        StringBuilder sb = new StringBuilder();
        for (List<Ch> ln : ls) {
            List<Ch> s = new ArrayList<Ch>(ln);
            Collections.sort(s, POR_X);
            if (sb.length() > 0) {
                sb.append(" ");
            }
            for (Ch c : s) {
                sb.append(c.t);
            }
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    /** Agrupa caracteres en líneas por su coordenada Y (de arriba hacia abajo). */
    private static List<List<Ch>> agruparLineas(List<Ch> chars) {
        List<Ch> sorted = new ArrayList<Ch>(chars);
        Collections.sort(sorted, new Comparator<Ch>() {
            @Override public int compare(Ch a, Ch b) {
                int d = Float.compare(b.y, a.y);
                return d != 0 ? d : Float.compare(a.x, b.x);
            }
        });
        List<List<Ch>> lines = new ArrayList<List<Ch>>();
        List<Ch> cur = new ArrayList<Ch>();
        float cy = Float.NaN;
        for (Ch c : sorted) {
            if (Float.isNaN(cy) || Math.abs(c.y - cy) <= LINE_TOL) {
                cur.add(c);
                if (Float.isNaN(cy)) {
                    cy = c.y;
                }
            } else {
                lines.add(cur);
                cur = new ArrayList<Ch>();
                cur.add(c);
                cy = c.y;
            }
        }
        if (!cur.isEmpty()) {
            lines.add(cur);
        }
        return lines;
    }

    private static String concat(List<Ch> ln) {
        List<Ch> s = new ArrayList<Ch>(ln);
        Collections.sort(s, POR_X);
        StringBuilder sb = new StringBuilder();
        for (Ch c : s) {
            sb.append(c.t);
        }
        return sb.toString();
    }
}
