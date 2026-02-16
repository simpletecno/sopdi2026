/*
 * Ventana para editar el perfil de usuario
 * ...
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
import com.vaadin.ui.ComboBox;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
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

    final Calendar now = Calendar.getInstance();
    
    ImageUploader receiver = new ImageUploader();        
    
    public UserPreferences() {
        
        setSizeFull();
        setCaption("Edición de perfil");

        acceptBtn    = new Button("Aceptar");
        acceptBtn.setIcon(FontAwesome.CHECK);
        acceptBtn.setWidth(120,Sizeable.UNITS_PIXELS);
        acceptBtn.addListener((Button.ClickListener) this);
        acceptBtn.setClickShortcut(KeyCode.ENTER);
        exitBtn    = new Button("Salir");
        exitBtn.setIcon(FontAwesome.EJECT);
        exitBtn.setWidth(120,Sizeable.UNITS_PIXELS);
        exitBtn.addListener((Button.ClickListener) this);
        exitBtn.setClickShortcut(KeyCode.ESCAPE);

        ThemeResource resource = new ThemeResource("img/profile-pic-300px.jpg");

        personPhoto = new Image("", resource);
        personPhoto.setImmediate(true);
        personPhoto.setWidth("100px");
        personPhoto.setHeight("100px"); 
        personPhoto.addStyleName("user-menu");

        uploader = new Upload("Logo de la empresa", receiver);
        uploader.setButtonCaption("Cargar");
        uploader.addSucceededListener(receiver);   
           
        MarginInfo marginInfo = new MarginInfo(true, true, true, true);
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(marginInfo);
        layout.setSpacing(true);
                        
        HorizontalLayout pictureLayout = new HorizontalLayout();
        pictureLayout.setMargin(marginInfo);
        pictureLayout.addComponent(personPhoto);
        pictureLayout.addComponent(uploader);
        pictureLayout.setComponentAlignment(uploader, Alignment.MIDDLE_CENTER);
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
        setHeight("500px");
        
        setPositionX(500);
        setPositionY(100);
        setModal(true);
        setResizable(false);
        
        fillData();
    }

    void fillData() {
        
        personPhoto.setSource(((SopdiUI) UI.getCurrent()).sessionInformation.getPhotoStreamResource());
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        final Button source = event.getButton();
        
        if(source == acceptBtn) {
       
            if(!datosValidos()) {
                return;
            }

            try {

                String queryString;
                queryString = "update empresa Set ";
                queryString += " Estatus = 'ACTIVA'";
                
                ByteArrayInputStream inputStream1 = null;
                
                if(receiver.fis != null) {
                    
System.out.println("\nfis file size=" + receiver.fis.getChannel().size());

                    final byte []thisArray = new byte[(int)receiver.fis.getChannel().size()];
                    receiver.fis.read(thisArray,0,thisArray.length);
                    inputStream1 = new ByteArrayInputStream(thisArray);                
                    queryString += ",LogoFileName = '" + receiver.file.getName() + "'";
                    queryString += ",Logo = ?";
                    ((SopdiUI)UI.getCurrent()).sessionInformation.setPhotoStreamResource(new StreamResource(
                        new StreamResource.StreamSource() {
                            public InputStream getStream() {
                                return new ByteArrayInputStream(thisArray);
                            }
                        },receiver.file.getName()));
                    ((SopdiUI) UI.getCurrent()).userSettings.getItems().get(0).setIcon(((SopdiUI)UI.getCurrent()).sessionInformation.getPhotoStreamResource());
                }
                
                queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrCompanyId();
                                
                stPreparedQuery  = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString);

                if(receiver.file != null) {
                    stPreparedQuery.setBinaryStream(1, inputStream1, inputStream1.available());
//                    receiver.file.delete(); 
                }
                
System.out.println("preparedQuery="+stPreparedQuery.toString());

                stPreparedQuery.execute();                

                Notification.show("Actualización de perfil exitoso!", Notification.Type.HUMANIZED_MESSAGE);
 
                close();
                    
            }
            catch(Exception ex1) {
                Logger.getLogger(UserPreferences.class.getName()).log(Level.SEVERE, ex1.getMessage() );
                Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE); 
                System.out.println("ERROR AL INTENTAR ACTUALIZAR PERFIL : " + ex1.getMessage());
                ex1.printStackTrace();
            }
            
        }

        if(source == exitBtn) {
            try {
                if(receiver.fis != null) {
                    receiver.file.delete();
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            this.close();
        }
    }

    public final ComboBox createComboYear() {
        final ComboBox comboBox = new ComboBox("Año");
        comboBox.setImmediate(true);

        for(int iyear = now.get(Calendar.YEAR); iyear > 1930; iyear--) {
            comboBox.addItem(iyear);
        }
        
        return comboBox;
    }
    
    public final ComboBox createComboPais() {
        
        final ComboBox comboBox = new ComboBox("Pais");
        comboBox.setWidth("15em");
        comboBox.setImmediate(true);
                
        String queryString = "Select * ";
        queryString += " From cat_pais ";
        queryString += " Order By Nombre";
 
        try {
                       
            stQuery  = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery.executeQuery(queryString);
                        
            while(rsRecords.next()) { //  encontrado                
                comboBox.addItem(rsRecords.getInt("IdPais"));
                comboBox.setItemCaption(rsRecords.getInt("IdPais"), rsRecords.getString("Nombre"));
            }
            comboBox.select(320);
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA");
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE PAISES : " + ex1.getMessage());
            ex1.printStackTrace();
        }
        
        comboBox.setNewItemsAllowed(false);
        
        return comboBox;
    }        
    
    private boolean datosValidos() {
/*
        if(nombreTxt.getValue().isEmpty()) {
            Notification.show("Falta ingresar el nombre");
            nombreTxt.focus();
            return false;
        }
        if(telefonoTxt.getValue().isEmpty()) {
            Notification.show("Falta ingresar el número de teléfono");
            telefonoTxt.focus();
            return false;
        }
        if(String.valueOf(telefonoTxt.getValue()).isEmpty()) {
            Notification.show("Falta ingresar el número de teléfono");
            telefonoTxt.focus();
            return false;
        }
        if(String.valueOf(paisCbx.getValue()).isEmpty()) {
            Notification.show("Falta ingresar el pais");
            paisCbx.focus();
            return false;
        }
*/
        return true;
    }
    
    // Implement both receiver that saves upload in a file and
    // listener for successful upload
    class ImageUploader implements Receiver, SucceededListener {
        public File file;
        public FileOutputStream fos = null; // Stream to write to
        public FileInputStream fis = null;
        @Override
        public OutputStream receiveUpload(String filename,
            String mimeType) {
            // Create upload stream
            try {
                // Open the file for writing.
                
                new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/WEB-INF/LOGOS/" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrCompanyId()).mkdirs();
                file = new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/WEB-INF/LOGOS/" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrCompanyId() + "/" + filename);
System.out.println("\nfile="+file.getAbsolutePath());
                fos = new FileOutputStream(file);
            } catch (final java.io.FileNotFoundException e) {
                new Notification("El archivo se puede abrir o leer",
                e.getMessage(),
                Notification.Type.ERROR_MESSAGE)
                .show(Page.getCurrent());
                return null;
            }
            return fos; // Return the output stream to write to
        }
        @Override
        public void uploadSucceeded(SucceededEvent event) {
            Notification.show("Archivo cargado con exito!", Notification.Type.TRAY_NOTIFICATION);
            personPhoto.setSource(new FileResource(file));
            try {
                fis = new FileInputStream(file);
                //file.delete();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UserPreferences.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };       
}
