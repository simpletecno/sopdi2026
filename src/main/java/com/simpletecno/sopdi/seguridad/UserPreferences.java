/*
 * Ventana para editar el perfil del usuario.
 * Lee y actualiza la fotografía del usuario (campo Fotografia de la tabla usuario).
 *
 * @author Jose Aguirre
*/

package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class UserPreferences extends Window implements Button.ClickListener
{
    private static Statement stQuery;
    private static PreparedStatement stPreparedQuery;
    private static ResultSet rsRecords;

    Button acceptBtn;
    Button exitBtn;

    Upload uploader;
    Image personPhoto;

    ImageUploader receiver = new ImageUploader();

    public UserPreferences() {

        setSizeFull();
        setCaption("Preferencias de usuario");

        acceptBtn = new Button("Aceptar");
        acceptBtn.setIcon(FontAwesome.CHECK);
        acceptBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        acceptBtn.addListener(this);
        acceptBtn.setClickShortcut(KeyCode.ENTER);

        exitBtn = new Button("Salir");
        exitBtn.setIcon(FontAwesome.EJECT);
        exitBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        exitBtn.addListener(this);
        exitBtn.setClickShortcut(KeyCode.ESCAPE);

        ThemeResource resource = new ThemeResource("img/profilepicture.jpg");

        personPhoto = new Image("", resource);
        personPhoto.setImmediate(true);
        personPhoto.setWidth("150px");
        personPhoto.setHeight("150px");

        uploader = new Upload("Fotografía de usuario", receiver);
        uploader.setButtonCaption("Cargar");
        uploader.addSucceededListener(receiver);

        MarginInfo marginInfo = new MarginInfo(true, true, true, true);
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(marginInfo);
        layout.setSpacing(true);

        HorizontalLayout pictureLayout = new HorizontalLayout();
        pictureLayout.setMargin(marginInfo);
        pictureLayout.setSpacing(true);
        pictureLayout.addComponent(personPhoto);
        pictureLayout.addComponent(uploader);
        pictureLayout.setComponentAlignment(personPhoto, Alignment.MIDDLE_LEFT);
        pictureLayout.setComponentAlignment(uploader, Alignment.MIDDLE_RIGHT);

        layout.addComponent(pictureLayout);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addComponent(exitBtn);
        footer.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        footer.addComponent(acceptBtn);
        footer.setComponentAlignment(acceptBtn, Alignment.BOTTOM_CENTER);
        layout.addComponent(footer);
        layout.setComponentAlignment(footer, Alignment.BOTTOM_CENTER);

        setContent(layout);

        setWidth("520px");
        setHeight("400px");

        setPositionX(500);
        setPositionY(100);
        setModal(true);
        setResizable(false);

        fillData();
    }

    /**
     * Lee la fotografía del usuario actual desde el campo Fotografia de la tabla
     * usuario y la muestra. Si no hay fotografía guardada, se conserva la imagen
     * por defecto del tema.
     */
    void fillData() {

        String queryString = "SELECT Fotografia FROM usuario ";
        queryString += " WHERE IdUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                final byte[] imageBytes = rsRecords.getBytes("Fotografia");
                if (imageBytes != null && imageBytes.length > 0) {
                    personPhoto.setSource(new StreamResource(
                            () -> new ByteArrayInputStream(imageBytes),
                            "user_photo_" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId()
                                    + "_" + System.currentTimeMillis() + ".jpg"));
                }
            }
        } catch (Exception ex1) {
            Logger.getLogger(UserPreferences.class.getName()).log(Level.SEVERE, ex1.getMessage());
            Notification.show("Error al leer la fotografía del usuario: " + ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }
    }

    @Override
    public void buttonClick(ClickEvent event) {
        final Button source = event.getButton();

        if (source == acceptBtn) {

            if (receiver.fis == null || receiver.file == null) {
                Notification.show("Seleccione una fotografía para cargar.",
                        Notification.Type.WARNING_MESSAGE);
                return;
            }

            try {
                // Lee los bytes del archivo subido.
                final byte[] thisArray = new byte[(int) receiver.fis.getChannel().size()];
                receiver.fis.read(thisArray, 0, thisArray.length);
                ByteArrayInputStream inputStream1 = new ByteArrayInputStream(thisArray);

                // Actualiza el campo Fotografia de la tabla usuario.
                String queryString = "UPDATE usuario SET Fotografia = ? ";
                queryString += " WHERE IdUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();

                stPreparedQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString);
                stPreparedQuery.setBinaryStream(1, inputStream1, inputStream1.available());
                stPreparedQuery.execute();

                // Refresca la foto en sesión y el avatar del menú de usuario (header).
                StreamResource photo = new StreamResource(
                        () -> new ByteArrayInputStream(thisArray),
                        "user_photo_" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId()
                                + "_" + System.currentTimeMillis() + ".jpg");
                ((SopdiUI) UI.getCurrent()).sessionInformation.setPhotoStreamResource(photo);
                if (((SopdiUI) UI.getCurrent()).getUserSettingsItem() != null) {
                    ((SopdiUI) UI.getCurrent()).getUserSettingsItem().setIcon(photo);
                }

                Notification.show("Fotografía actualizada!", Notification.Type.HUMANIZED_MESSAGE);

                // Limpia el archivo temporal de staging.
                try {
                    receiver.file.delete();
                } catch (Exception ignored) {
                }

                close();

            } catch (Exception ex1) {
                Logger.getLogger(UserPreferences.class.getName()).log(Level.SEVERE, ex1.getMessage());
                Notification.show("ERROR AL ACTUALIZAR LA FOTOGRAFÍA: " + ex1.getMessage(),
                        Notification.Type.ERROR_MESSAGE);
                ex1.printStackTrace();
            }
        }

        if (source == exitBtn) {
            try {
                if (receiver.fis != null && receiver.file != null) {
                    receiver.file.delete();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.close();
        }
    }

    // Receiver que guarda la subida en un archivo temporal y listener de subida exitosa.
    class ImageUploader implements Receiver, SucceededListener {
        public File file;
        public FileOutputStream fos = null; // Stream para escribir
        public FileInputStream fis = null;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            try {
                String userId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
                String basePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
                        + "/WEB-INF/FOTOS_USUARIO/" + userId;
                new File(basePath).mkdirs();
                file = new File(basePath + "/" + filename);
                fos = new FileOutputStream(file);
            } catch (final java.io.FileNotFoundException e) {
                new Notification("El archivo no se puede abrir o leer",
                        e.getMessage(),
                        Notification.Type.ERROR_MESSAGE)
                        .show(Page.getCurrent());
                return null;
            }
            return fos; // Stream de salida donde escribir
        }

        @Override
        public void uploadSucceeded(SucceededEvent event) {
            Notification.show("Archivo cargado con éxito!", Notification.Type.TRAY_NOTIFICATION);
            personPhoto.setSource(new FileResource(file));
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UserPreferences.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
