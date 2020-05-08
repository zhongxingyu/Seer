 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import static controller.LoginController.login;
 import javax.swing.JOptionPane;
 import model.service.ServiceFactory;
 import java.awt.Desktop;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import javax.persistence.EntityNotFoundException;
 import model.dao.exceptions.DataBaseException;
 
 /**
  *
  * @author Felipe
  */
 public class ControlDeVersion {
 
     private static final String VERSIONACTUAL = "1.2.0";
     private static final String URLULTIMAVERSION = "http://javergarav.wix.com/ggsi2013#!entregas/c1pna";
     private static String ultimaVersion;
 
     public static void actualizado() {
         try {
             ultimaVersion = ServiceFactory.getInstance().getVersionService()
                     .getVersion().getVersion();
         } catch (EntityNotFoundException ex) {
             JOptionPane.showMessageDialog(login, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
         } catch (DataBaseException ex) {
            JOptionPane.showMessageDialog(login, ex.getMessage() + "\n\n"
                    + "Para acceder a la aplicación debe estar conectado a internet y a una red que no utilice un servidor proxy.\n\n"
                    + "Por favor conectese a internet usando una red que no utilice proxy y vuelva a intentarlo.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
         }
         if (!ultimaVersion.equals(VERSIONACTUAL)) {
             int opcion = JOptionPane.showOptionDialog(login, "Existe una nueva versión disponible (" + ultimaVersion + ") debe actualizar el programa para continuar con su ejecución, version actual: " + VERSIONACTUAL, "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Descargar", "Cancelar"}, "Descargar");
             switch (opcion) {
                 case JOptionPane.OK_OPTION:
                     try {
                         Desktop.getDesktop().browse(new URI(URLULTIMAVERSION));
                    } catch (IOException | URISyntaxException ex) {
                         JOptionPane.showMessageDialog(login, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                     }
                     System.exit(0);
                     break;
                 case JOptionPane.CANCEL_OPTION:
                     System.exit(0);
                     break;
                 default:
                     System.exit(0);
             }
         }
     }
 }
