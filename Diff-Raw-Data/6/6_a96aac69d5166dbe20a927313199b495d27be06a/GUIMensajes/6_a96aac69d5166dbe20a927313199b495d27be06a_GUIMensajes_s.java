 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.fsaravia.utilities;
 
 import com.ib.HBCore.exceptions.ValidationException;
 import com.ib.mailing.reportes.ReportarErrores;
 import java.awt.Component;
 import java.awt.Container;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author Fede
  */
 public class GUIMensajes extends JOptionPane {
 
     private static void mostrarError(Component padre, String error) {
         showMessageDialog(padre, error, "Error", JOptionPane.ERROR_MESSAGE);
     }
 
     private static void mostrarError(Component padre, Throwable error) {
         mostrarError(padre, error.getLocalizedMessage());
     }
 //
 //    private static void mostrarError(Component padreI, Throwable error, boolean reportar, String remitente) {
 //        boolean enviar = true;
 //        if (error instanceof ValidationException) {
 //            enviar = false;
 //        }
 //        if (error.getCause() != null && error.getCause() instanceof ValidationException) {
 //            enviar = false;
 //        }
 //        if (reportar && enviar) {
 //            int op = showConfirmDialog(padre, error.getLocalizedMessage() + "\n\n¿Desea reportar este error?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
 //            if (op == JOptionPane.YES_OPTION) {
 //                //padre
 //                ReportarErrores report = new ReportarErrores(null, error);
 //                report.setLocationRelativeTo(padre);
 //                report.setVisible(true);
 //            }
 //        } else {
 //            mostrarError(padre, error);
 //        }
 //    }
 
     public static void mostrarErrorReportar(Component padre, Throwable error) {
         boolean enviar = true;
         if (error instanceof ValidationException) {
             enviar = false;
         }
         if (error.getCause() != null && error.getCause() instanceof ValidationException) {
             enviar = false;
         }
         if (enviar) {
             Logger.getLogger(padre.getClass().getName()).log(Level.SEVERE, null, error);
             int op = showConfirmDialog(padre, error.getLocalizedMessage() + "\n\n¿Desea reportar este error?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
             if (op == JOptionPane.YES_OPTION) {
                 //padre
                 ReportarErrores report = new ReportarErrores(null, error);
                 report.setLocationRelativeTo(padre);
                 report.setVisible(true);
             }
         } else {
            mostrarError(padre, error);
         }
     }
 
     public static void mostrarMensaje(Component padre, String mensaje) {
         showMessageDialog(padre, mensaje, "Mensaje", JOptionPane.INFORMATION_MESSAGE);
     }
 
     public static boolean mostrarPregunta(Component padre, String pregunta) {
         int op = showConfirmDialog(padre, pregunta, "Pregunta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
         return op == JOptionPane.YES_OPTION;
     }
 
     public static JDialog mostrarMensaje(Component padre, String mensaje, String titulo) {
         JFrame ventana = null;
         try {
             ventana = (JFrame) padre;
             if (ventana == null) {
                 ventana = (JFrame) ((Container) padre).getParent();
             }
         } catch (ClassCastException cce) {
             ventana = null;
         } finally {
             Dialogo d = new Dialogo(ventana, false, titulo, mensaje);
             d.setLocationRelativeTo(padre);
             return d;
         }
     }
 }
