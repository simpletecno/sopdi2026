package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

public class ActivosResponsableFirmaWindow extends Window {

    private static final String FIRMA_BASE_DIR = System.getProperty(
            "sopdi.firmasActivos.dir",
            System.getProperty("user.home") + File.separator + "sopdi_firmas_activos"
    );

    private final UI mainUI;
    private final long idActivo;
    private final long idResponsable;
    private final String codigoActivo;
    private final String descripcionActivo;
    private final String responsable;
    private final Date fechaInicio;
    private final String firmaPathActual;
    private final Runnable onGuardarCallback;

    private VerticalLayout visorLayout;
    private File archivoSubidoPendiente;

    public ActivosResponsableFirmaWindow(
            UI mainUI,
            long idActivo,
            long idResponsable,
            String codigoActivo,
            String descripcionActivo,
            String responsable,
            Date fechaInicio,
            String firmaPathActual,
            Runnable onGuardarCallback
    ) {
        super("Firma - " + responsable);

        this.mainUI = mainUI;
        this.idActivo = idActivo;
        this.idResponsable = idResponsable;
        this.codigoActivo = codigoActivo;
        this.descripcionActivo = descripcionActivo;
        this.responsable = responsable;
        this.fechaInicio = fechaInicio;
        this.firmaPathActual = firmaPathActual;
        this.onGuardarCallback = onGuardarCallback;

        setModal(true);
        setWidth("70%");
        setHeight("80%");
        center();

        initLayout();
    }

    private void initLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        visorLayout = new VerticalLayout();
        visorLayout.setSizeFull();
        visorLayout.setSpacing(true);

        if (!isBlank(firmaPathActual)) {
            mostrarArchivoPDF(new File(firmaPathActual));
        } else {
            mostrarMensajeSinPDF();
        }

        Button btnGenerar = new Button("Generar PDF");
        btnGenerar.setIcon(FontAwesome.FILE_PDF_O);
        btnGenerar.setStyleName(ValoTheme.BUTTON_PRIMARY);
        btnGenerar.addClickListener(event -> generarTemplatePDF());

        PdfUploadReceiver receiver = new PdfUploadReceiver();

        Upload upload = new Upload("Subir PDF firmado:", receiver);
        upload.setButtonCaption("Subir PDF");
        upload.addSucceededListener(event -> {
            File uploaded = receiver.getFile();

            if (uploaded == null || !uploaded.exists()) {
                Notification.show("No se pudo recibir el PDF.", Notification.Type.ERROR_MESSAGE);
                return;
            }

            archivoSubidoPendiente = uploaded;
            mostrarArchivoPDF(uploaded);

            Notification.show(
                    "PDF firmado cargado temporalmente. Presione Guardar para asignarlo.",
                    Notification.Type.HUMANIZED_MESSAGE
            );
        });

        upload.addFailedListener(event -> Notification.show(
                "Error al subir PDF.",
                Notification.Type.ERROR_MESSAGE
        ));

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setIcon(FontAwesome.SAVE);
        btnGuardar.setStyleName(ValoTheme.BUTTON_PRIMARY);
        btnGuardar.addClickListener(event -> guardarPDF());

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setIcon(FontAwesome.TIMES);
        btnCerrar.addClickListener(event -> close());

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.addComponents(btnGenerar, upload, btnGuardar, btnCerrar);
        botonesLayout.setComponentAlignment(btnGenerar, Alignment.MIDDLE_LEFT);
        botonesLayout.setComponentAlignment(upload, Alignment.MIDDLE_LEFT);
        botonesLayout.setComponentAlignment(btnGuardar, Alignment.MIDDLE_LEFT);
        botonesLayout.setComponentAlignment(btnCerrar, Alignment.MIDDLE_LEFT);

        mainLayout.addComponents(visorLayout, botonesLayout);
        mainLayout.setExpandRatio(visorLayout, 1);

        setContent(mainLayout);
    }

    private void generarTemplatePDF() {
        try {
            byte[] pdfBytes = crearPDFFirmaBytes();

            File tempTemplate = File.createTempFile(
                    "template_firma_" + idResponsable + "_",
                    ".pdf"
            );

            try (FileOutputStream fos = new FileOutputStream(tempTemplate)) {
                fos.write(pdfBytes);
            }

            // El template generado NO queda pendiente para guardar.
            // El usuario debe firmarlo y subir el PDF firmado.
            archivoSubidoPendiente = null;

            mostrarArchivoPDF(tempTemplate);

            Notification.show(
                    "Template generado. Debe firmarse y luego subirse el PDF firmado.",
                    Notification.Type.HUMANIZED_MESSAGE
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Error al generar PDF: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void guardarPDF() {
        try {
            if (archivoSubidoPendiente == null || !archivoSubidoPendiente.exists()) {
                Notification.show(
                        "Debe subir el PDF firmado antes de guardar.",
                        Notification.Type.WARNING_MESSAGE
                );
                return;
            }

            String pathAnterior = obtenerFirmaPathActualDesdeBD();

            File carpeta = new File(FIRMA_BASE_DIR);
            if (!carpeta.exists() && !carpeta.mkdirs()) {
                throw new Exception("No se pudo crear carpeta de firmas: " + FIRMA_BASE_DIR);
            }

            String nombreArchivo = "activo_" + idActivo +
                    "_responsable_" + idResponsable +
                    "_" + System.currentTimeMillis() +
                    ".pdf";

            File destino = new File(carpeta, nombreArchivo);

            Files.copy(
                    archivoSubidoPendiente.toPath(),
                    destino.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            actualizarFirmaPath(destino.getAbsolutePath());

            borrarArchivoAnterior(pathAnterior, destino);

            archivoSubidoPendiente.delete();

            Notification.show("PDF de firma guardado correctamente.", Notification.Type.HUMANIZED_MESSAGE);

            if (onGuardarCallback != null) {
                onGuardarCallback.run();
            }

            close();

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Error al guardar PDF: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void mostrarMensajeSinPDF() {
        visorLayout.removeAllComponents();

        Label label = new Label("Este responsable no tiene PDF/firma asociado.");
        visorLayout.addComponent(label);
        visorLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
    }

    private void mostrarArchivoPDF(File archivoPDF) {
        visorLayout.removeAllComponents();

        if (archivoPDF == null || !archivoPDF.exists()) {
            mostrarMensajeSinPDF();
            return;
        }

        StreamResource.StreamSource source = () -> {
            try {
                return new FileInputStream(archivoPDF);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        };

        StreamResource resource = new StreamResource(
                source,
                "firma_" + System.currentTimeMillis() + ".pdf"
        );

        resource.setMIMEType("application/pdf");
        resource.setCacheTime(0);

        BrowserFrame browserFrame = new BrowserFrame(null, resource);
        browserFrame.setSizeFull();

        visorLayout.addComponent(browserFrame);
        visorLayout.setExpandRatio(browserFrame, 1);
    }

    private String obtenerFirmaPathActualDesdeBD() throws Exception {
        String sql = "SELECT FirmaFilePath FROM activos_responsable WHERE id = ?";

        PreparedStatement ps = ((SopdiUI) mainUI)
                .databaseProvider
                .getCurrentConnection()
                .prepareStatement(sql);

        ps.setLong(1, idResponsable);

        ResultSet rs = ps.executeQuery();

        String path = null;
        if (rs.next()) {
            path = rs.getString("FirmaFilePath");
        }

        rs.close();
        ps.close();

        return path;
    }

    private void actualizarFirmaPath(String nuevoPath) throws Exception {
        String sql = "UPDATE activos_responsable SET FirmaFilePath = ? WHERE id = ?";

        PreparedStatement ps = ((SopdiUI) mainUI)
                .databaseProvider
                .getCurrentConnection()
                .prepareStatement(sql);

        ps.setString(1, nuevoPath);
        ps.setLong(2, idResponsable);

        ps.executeUpdate();
        ps.close();
    }

    private void borrarArchivoAnterior(String pathAnterior, File nuevoArchivo) {
        if (isBlank(pathAnterior)) {
            return;
        }

        File anterior = new File(pathAnterior);

        if (!anterior.exists()) {
            return;
        }

        if (nuevoArchivo != null && anterior.getAbsolutePath().equals(nuevoArchivo.getAbsolutePath())) {
            return;
        }

        anterior.delete();
    }

    private byte[] crearPDFFirmaBytes() throws Exception {
        if (fechaInicio == null) {
            throw new Exception("La fecha de inicio no puede estar vacía.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.LETTER, 20, 20, 20, 20);
        PdfWriter.getInstance(document, baos);

        document.open();

        Font fontTitle = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD, BaseColor.BLACK);
        Paragraph titlePara = new Paragraph("ASIGNACIÓN DE RESPONSABLE - ACTIVO", fontTitle);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        document.add(titlePara);

        document.add(new Paragraph(" "));

        Font fontNormal = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

        Paragraph nombreActivo = new Paragraph("Activo: " + descripcionActivo, fontNormal);
        nombreActivo.setAlignment(Element.ALIGN_LEFT);
        document.add(nombreActivo);

        Paragraph codActivo = new Paragraph("Código: " + codigoActivo, fontNormal);
        codActivo.setAlignment(Element.ALIGN_LEFT);
        document.add(codActivo);

        document.add(new Paragraph(" "));

        Paragraph responsableParagraph = new Paragraph("Responsable: " + responsable, fontNormal);
        responsableParagraph.setAlignment(Element.ALIGN_LEFT);
        document.add(responsableParagraph);

        document.add(new Paragraph(" "));

        Paragraph fecha = new Paragraph(
                "Fecha de Asignación: " + Utileria.getFechaDDMMYYYY(fechaInicio),
                fontNormal
        );
        fecha.setAlignment(Element.ALIGN_LEFT);
        document.add(fecha);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        Font fontSubtitle = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        Paragraph firmaSection = new Paragraph("Firma del Responsable:", fontSubtitle);
        firmaSection.setAlignment(Element.ALIGN_LEFT);
        document.add(firmaSection);

        document.add(new Paragraph(" "));

        PdfPTable firmaTable = new PdfPTable(1);
        firmaTable.setWidthPercentage(60);

        PdfPCell firmaCell = new PdfPCell(new Paragraph(" "));
        firmaCell.setFixedHeight(60);
        firmaCell.setBorderWidthTop(1);
        firmaCell.setBorderWidthBottom(0);
        firmaCell.setBorderWidthLeft(0);
        firmaCell.setBorderWidthRight(0);
        firmaTable.addCell(firmaCell);

        document.add(firmaTable);

        document.add(new Paragraph(" "));

        PdfPTable nombreTable = new PdfPTable(1);
        nombreTable.setWidthPercentage(60);

        PdfPCell nombreCell = new PdfPCell(new Paragraph("_________________________", fontNormal));
        nombreCell.setBorder(0);
        nombreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nombreTable.addCell(nombreCell);

        PdfPCell labelCell = new PdfPCell(new Paragraph("Nombre y Firma", fontNormal));
        labelCell.setBorder(0);
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nombreTable.addCell(labelCell);

        document.add(nombreTable);

        document.close();

        return baos.toByteArray();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean esPDF(String filename, String mimeType) {
        boolean mimeOk = mimeType != null && mimeType.equalsIgnoreCase("application/pdf");
        boolean extensionOk = filename != null && filename.toLowerCase().endsWith(".pdf");

        return mimeOk || extensionOk;
    }

    private class PdfUploadReceiver implements Upload.Receiver {
        private File file;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            try {
                if (!esPDF(filename, mimeType)) {
                    Notification.show("Solo se permiten archivos PDF", Notification.Type.WARNING_MESSAGE);
                    file = null;
                    return null;
                }

                file = File.createTempFile("firma_upload_", ".pdf");
                return new FileOutputStream(file);

            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Error al recibir archivo: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                file = null;
                return null;
            }
        }

        public File getFile() {
            return file;
        }
    }
}