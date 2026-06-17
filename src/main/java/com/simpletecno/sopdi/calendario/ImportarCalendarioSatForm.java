package com.simpletecno.sopdi.calendario;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ventana modal que importa los vencimientos de un Calendario Mensual Tributario
 * de la SAT (PDF) y los crea como eventos de todo el día en el calendario del
 * usuario actual (tabla usuario_evento).
 *
 * <p>El PDF de la SAT se genera en orientación horizontal (rotado 90°): las
 * columnas se distinguen por la coordenada Y de cada fragmento de texto y las
 * líneas por la coordenada X. El parser auto-calibra los límites de columna a
 * partir de los datos, sin depender de posiciones fijas.</p>
 *
 * @author Jose Aguirre
 */
public class ImportarCalendarioSatForm extends Window {

    private static final long serialVersionUID = 1L;

    private static final String LUGAR = "Declaraguate / Agencia Virtual SAT";
    private static final String COLOR = "color3"; // Rojo: resaltar vencimientos

    private final Runnable onImported;
    private final UI mainUI;

    private final List<EventoSat> eventos = new ArrayList<EventoSat>();

    private Table previewTable;
    private Label resumenLbl;
    private Button importarBtn;
    private MultiFileUpload upload;

    public ImportarCalendarioSatForm(Runnable onImported) {
        this.onImported = onImported;
        this.mainUI = UI.getCurrent();

        setCaption("Importar Calendario Tributario SAT");
        setModal(true);
        setWidth("760px");
        setHeight("80%");
        center();

        construir();
    }

    private void construir() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(new MarginInfo(true, true, true, true));
        layout.setSpacing(true);
        layout.setSizeFull();

        Label infoLbl = new Label("Suba el PDF del Calendario Mensual Tributario de la SAT. "
                + "Se leerán los vencimientos y se crearán como eventos de todo el día en su calendario.");
        infoLbl.setWidth("100%");

        previewTable = new Table();
        previewTable.setSizeFull();
        previewTable.addContainerProperty("Fecha", String.class, "");
        previewTable.addContainerProperty("Impuesto", String.class, "");
        previewTable.addContainerProperty("Formulario", String.class, "");
        previewTable.setColumnExpandRatio("Impuesto", 1);
        previewTable.setColumnWidth("Fecha", 90);
        previewTable.setColumnWidth("Formulario", 130);

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

        importarBtn = new Button("Importar a mi calendario", FontAwesome.CALENDAR_PLUS_O);
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
            tmp = File.createTempFile("sat_cal_", ".pdf");
            byte[] buffer = new byte[Math.max(stream.available(), 8192)];
            try (OutputStream out = new FileOutputStream(tmp)) {
                int n;
                while ((n = stream.read(buffer)) > 0) {
                    out.write(buffer, 0, n);
                }
            }

            eventos.clear();
            eventos.addAll(parsearPdf(tmp));

            previewTable.removeAllItems();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            int i = 1;
            for (EventoSat ev : eventos) {
                previewTable.addItem(new Object[]{
                        sdf.format(ev.fecha), ev.titulo, ev.formulario}, i++);
            }

            if (eventos.isEmpty()) {
                resumenLbl.setValue("No se encontraron vencimientos en el PDF. ¿Es un Calendario Tributario SAT?");
                importarBtn.setEnabled(false);
                Notification.show("No se reconocieron vencimientos en el archivo.",
                        Notification.Type.WARNING_MESSAGE);
            } else {
                resumenLbl.setValue("Se detectaron " + eventos.size() + " vencimientos. Revise y presione Importar.");
                importarBtn.setEnabled(true);
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportarCalendarioSatForm.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            Notification.show("Error al leer el PDF: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        } finally {
            try { if (stream != null) stream.close(); } catch (Exception ignored) { }
            if (tmp != null) {
                tmp.delete();
            }
        }
    }

    private void confirmarImportacion() {
        if (eventos.isEmpty()) {
            Notification.show("No hay vencimientos para importar.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        ConfirmDialog.show(UI.getCurrent(), "Confirme:",
                "¿Crear " + eventos.size() + " eventos en su calendario?", "SI", "NO",
                dialog -> {
                    if (dialog.isConfirmed()) {
                        guardar();
                    }
                });
    }

    /** Inserta los eventos para el usuario actual, evitando duplicados. */
    private void guardar() {
        String idUsuario = ((SopdiUI) mainUI).sessionInformation.getStrUserId();

        String insert = "INSERT INTO usuario_evento "
                + " (IdUsuario, Titulo, Descripcion, Lugar, FechaInicio, FechaFin, TodoElDia, Color, Estatus) "
                + " VALUES (?,?,?,?,?,?,1,?, 'ACTIVO')";

        SimpleDateFormat dia = new SimpleDateFormat("yyyy-MM-dd");
        int insertados = 0;
        int omitidos = 0;

        try {
            Set<String> existentes = cargarExistentes(idUsuario);

            try (PreparedStatement ps = ((SopdiUI) mainUI).databaseProvider
                    .getCurrentConnection().prepareStatement(insert)) {

                for (EventoSat ev : eventos) {
                    String clave = dia.format(ev.fecha) + "|" + ev.titulo;
                    if (existentes.contains(clave)) {
                        omitidos++;
                        continue;
                    }

                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTime(ev.fecha);
                    c.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    c.set(java.util.Calendar.MINUTE, 0);
                    c.set(java.util.Calendar.SECOND, 0);
                    c.set(java.util.Calendar.MILLISECOND, 0);
                    Timestamp inicio = new Timestamp(c.getTimeInMillis());
                    c.set(java.util.Calendar.HOUR_OF_DAY, 23);
                    c.set(java.util.Calendar.MINUTE, 59);
                    c.set(java.util.Calendar.SECOND, 59);
                    Timestamp fin = new Timestamp(c.getTimeInMillis());

                    ps.setInt(1, Integer.parseInt(idUsuario));
                    ps.setString(2, ev.titulo);
                    ps.setString(3, ev.descripcion);
                    ps.setString(4, LUGAR);
                    ps.setTimestamp(5, inicio);
                    ps.setTimestamp(6, fin);
                    ps.setString(7, COLOR);
                    ps.executeUpdate();

                    existentes.add(clave);
                    insertados++;
                }
            }

            Notification.show("Importación exitosa: " + insertados + " eventos creados"
                    + (omitidos > 0 ? ", " + omitidos + " ya existían." : "."),
                    Notification.Type.HUMANIZED_MESSAGE);

            if (onImported != null) {
                onImported.run();
            }
            close();

        } catch (Exception ex) {
            Logger.getLogger(ImportarCalendarioSatForm.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            Notification.show("Error al guardar los eventos: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    /** Devuelve claves "fecha|titulo" de los eventos SAT ya cargados para el usuario. */
    private Set<String> cargarExistentes(String idUsuario) {
        Set<String> set = new HashSet<String>();
        String q = "SELECT Titulo, DATE_FORMAT(FechaInicio,'%Y-%m-%d') AS d FROM usuario_evento "
                + " WHERE IdUsuario = " + idUsuario + " AND Estatus = 'ACTIVO'";
        try (Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                set.add(rs.getString("d") + "|" + rs.getString("Titulo"));
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportarCalendarioSatForm.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return set;
    }

    // ------------------------------------------------------------------
    //  Parser del PDF (iText 5)
    // ------------------------------------------------------------------

    /** Un vencimiento leído del PDF. */
    static class EventoSat {
        Date fecha;
        String titulo;       // Nombre del impuesto
        String descripcion;  // Concepto (+ formulario)
        String formulario;   // No. de formulario Declaraguate
    }

    /** Fragmento de texto con su posición. */
    private static class Chunk {
        final float x;
        final float y;
        final String texto;
        Chunk(float x, float y, String texto) { this.x = x; this.y = y; this.texto = texto; }
    }

    /** Recolecta los fragmentos de texto de una página con sus coordenadas. */
    private static class ColeccionChunks implements RenderListener {
        final List<Chunk> chunks = new ArrayList<Chunk>();
        @Override public void beginTextBlock() { }
        @Override public void endTextBlock() { }
        @Override public void renderImage(ImageRenderInfo renderInfo) { }
        @Override public void renderText(TextRenderInfo info) {
            String t = info.getText();
            if (t == null || t.trim().isEmpty()) {
                return;
            }
            com.itextpdf.text.pdf.parser.Vector p = info.getBaseline().getStartPoint();
            chunks.add(new Chunk(p.get(0), p.get(1), t));
        }
    }

    static List<EventoSat> parsearPdf(File pdf) throws Exception {
        List<EventoSat> resultado = new ArrayList<EventoSat>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);

        PdfReader reader = new PdfReader(pdf.getAbsolutePath());
        try {
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            Comparator<Chunk> porX = new Comparator<Chunk>() {
                @Override public int compare(Chunk a, Chunk b) { return Float.compare(a.x, b.x); }
            };

            for (int pg = 1; pg <= reader.getNumberOfPages(); pg++) {
                ColeccionChunks lis = new ColeccionChunks();
                parser.processContent(pg, lis);

                // Anclas = fragmentos que son exactamente una fecha dd/MM/yyyy.
                List<Float> anclas = new ArrayList<Float>();
                for (Chunk c : lis.chunks) {
                    if (c.texto.trim().matches("\\d{2}/\\d{2}/\\d{4}")) {
                        anclas.add(c.x);
                    }
                }
                if (anclas.isEmpty()) {
                    continue;
                }
                Collections.sort(anclas);
                float minAncla = anclas.get(0);

                // Detectar columnas agrupando la coordenada Y (cada columna comparte
                // el borde izquierdo, por lo que su Y es prácticamente constante).
                TreeSet<Float> ys = new TreeSet<Float>();
                for (Chunk c : lis.chunks) {
                    if (c.x >= minAncla - 1) {
                        ys.add((float) Math.round(c.y));
                    }
                }
                List<Float> clusters = new ArrayList<Float>();
                Float prev = null;
                for (Float y : ys) {
                    if (prev == null || y - prev > 30) {
                        clusters.add(y);
                    }
                    prev = y;
                }
                if (clusters.size() < 3) {
                    continue; // no parece la tabla esperada
                }
                float yFecha = clusters.get(0);
                float yNombre = clusters.get(1);
                float yConcepto = clusters.get(2);
                float yDecl = clusters.get(clusters.size() - 1);
                float limFechaNombre = (yFecha + yNombre) / 2;
                float limNombreConcepto = (yNombre + yConcepto) / 2;
                float limConceptoDecl = (yConcepto + yDecl) / 2;

                for (int i = 0; i < anclas.size(); i++) {
                    float aX = anclas.get(i);
                    float nX = (i + 1 < anclas.size()) ? anclas.get(i + 1) : Float.MAX_VALUE;

                    String fecha = "";
                    List<Chunk> nombre = new ArrayList<Chunk>();
                    List<Chunk> concepto = new ArrayList<Chunk>();
                    List<Chunk> decl = new ArrayList<Chunk>();

                    for (Chunk c : lis.chunks) {
                        if (c.x < aX - 1 || c.x >= nX - 1) {
                            continue;
                        }
                        if (c.y < limFechaNombre) {
                            if (c.texto.trim().matches("\\d{2}/\\d{2}/\\d{4}")) {
                                fecha = c.texto.trim();
                            }
                        } else if (c.y < limNombreConcepto) {
                            nombre.add(c);
                        } else if (c.y < limConceptoDecl) {
                            concepto.add(c);
                        } else {
                            decl.add(c);
                        }
                    }
                    if (fecha.isEmpty()) {
                        continue;
                    }
                    Collections.sort(nombre, porX);
                    Collections.sort(concepto, porX);
                    Collections.sort(decl, porX);

                    EventoSat ev = new EventoSat();
                    ev.fecha = sdf.parse(fecha);
                    ev.titulo = limpiarTitulo(unir(nombre));
                    ev.formulario = limpiarFormulario(unir(decl));
                    String conceptoTxt = unir(concepto);
                    ev.descripcion = conceptoTxt
                            + (ev.formulario.isEmpty() ? "" : " Formulario Declaraguate: " + ev.formulario);
                    resultado.add(ev);
                }
            }
        } finally {
            reader.close();
        }
        return resultado;
    }

    private static String unir(List<Chunk> chunks) {
        StringBuilder sb = new StringBuilder();
        for (Chunk c : chunks) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(c.texto.trim());
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    private static String limpiarTitulo(String s) {
        if (s.length() > 255) {
            s = s.substring(0, 255);
        }
        return s;
    }

    /** Quita el ruido de la columna "Asiste Web" (-----) y deja solo el formulario. */
    private static String limpiarFormulario(String s) {
        return s.replace("-----", "").replaceAll("\\s+", " ").trim();
    }
}
