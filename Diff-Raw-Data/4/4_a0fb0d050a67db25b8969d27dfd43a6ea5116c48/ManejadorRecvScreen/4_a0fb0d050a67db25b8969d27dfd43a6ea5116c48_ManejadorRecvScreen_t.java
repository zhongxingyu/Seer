 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */

//Bug - Varios monitores - No determina bien la posición de las ventanas al crearse
//ni el tamaño de la pantalla compartida desde el alumno

 package Threads.ClaseAlumno;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImagingOpException;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import netcomp.ConexionAlumno;
 import netcomp.GUI.VtnClaseAlumno;
 import org.imgscalr.Scalr;
 
 /**
  *
  * @author zerg
  */
 public class ManejadorRecvScreen implements Runnable {
 
     public static BufferedImage image;
     public static Image imagePrint;
     public static JPanel panel;
     VtnClaseAlumno parent;
     ConexionAlumno conexion;
     public static InetAddress ip;
     public static int puerto;
     public static boolean corriendo;
     public static boolean closedFromGui;
     public static int ancho;
     public static int alto;
     JFrame frame;
 
     public ManejadorRecvScreen(InetAddress laIp, int elPuerto, VtnClaseAlumno parent) {
         ip = laIp;
         puerto = elPuerto;
         this.parent = parent;
         conexion = parent.getConexionAlumno();
     }
 
     @Override
     public void run() {
         corriendo = true;
         closedFromGui = false;
         conexion.setCompartePantalla(true);
         frame = crearVentana();
         byte[] data;
         try {
             Socket objSocket = new Socket(ip, puerto);
             InputStream objInput = objSocket.getInputStream();
             while (corriendo == true) {
                 Thread.sleep(300);
                 data = leerImagen(objInput);
                 if (data != null) {
                     BufferedImage tmpImage = ImageIO.read(new ByteArrayInputStream(data));
                     if (tmpImage != null) {
                         mostrarImagen(tmpImage, frame);
                     }
                 }
                 //Reviso memoria para evitar OutOfMemoryError: Java heap space
                 long minRunningMemory = (1024 * 1024);
                 Runtime runtime = Runtime.getRuntime();
                 if (runtime.freeMemory() < minRunningMemory) {
                     System.gc();
                 }
                 data = null;
             }
         } catch (InterruptedException ex) {
             frame.dispose();
             kill();
             corriendo = false;
         } catch (IOException e1) {
             e1.printStackTrace();
             corriendo = false;
             kill();
         }
     }
 
     public JFrame crearVentana() throws HeadlessException {
         JFrame elFrame = new JFrame("Pantalla de la Clase");
         ancho = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.8);
         alto = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.8);
         elFrame.setSize(ancho, alto);
         elFrame.add(panel = new JPanel() {
             @Override
             public void paintComponent(Graphics graphics) {
                 super.paintComponents(graphics);
                 if (imagePrint != null) {
                     graphics.drawImage(imagePrint, 0, 0, this);
                 } else {
                     graphics.setColor(Color.black);
                     graphics.fillRect(0, 0, ancho, alto);
                 }
             }
         });
         elFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         elFrame.addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 corriendo = false;
                 frame.dispose();
                 closedFromGui = true;
                 conexion.stopCompartirPantalla();
             }
         });
         elFrame.setVisible(true);
         return elFrame;
     }
 
     public void mostrarImagen(BufferedImage tmpImage, JFrame frame) throws ImagingOpException, IllegalArgumentException {
         imagePrint = Scalr.resize(tmpImage, ancho);
         frame.setSize(imagePrint.getWidth(frame) + 15, imagePrint.getHeight(frame) + 30);
         panel.repaint();
     }
 
     public byte[] leerImagen(InputStream objInput) {
         try {
             int size = (objInput.read() & 0xFF) << 24 | (objInput.read() & 0xFF) << 16 | (objInput.read() & 0xFF) << 8 | (objInput.read() & 0xFF);
             if (size > 0) {
                 byte[] data = new byte[size];
                 int total = 0;
                 while (total != size) {
                     total += objInput.read(data, total, size - total);
                 }
                 return data;
             } else {
                 return null;
             }
         } catch (IOException ex) {
             Logger.getLogger(ManejadorRecvScreen.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
 
     public void kill() {
         frame.dispose();
         conexion.setCompartePantalla(false);
         corriendo = false;
         if (!closedFromGui) {
             JOptionPane.showMessageDialog(new JFrame(), "Se ha perdido la conexión.");
         }
     }
 
     public void killSelf() {
         frame.dispose();
         conexion.setCompartePantalla(false);
         corriendo = false;
     }
 }
