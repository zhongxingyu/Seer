 /*
  * Copyright (C) 2012 Gyver
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.gyver.matrixmover;
 
 import com.gyver.matrixmover.core.Controller;
 import com.gyver.matrixmover.core.ExecutionTimerTask;
 import com.gyver.matrixmover.gui.Frame;
 import com.gyver.matrixmover.output.NullDevice;
 import com.gyver.matrixmover.output.Output;
 import com.gyver.matrixmover.output.OutputDeviceEnum;
 import com.gyver.matrixmover.properties.PropertiesHelper;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import net.sf.nimrod.NimRODLookAndFeel;
 import net.sf.nimrod.NimRODTheme;
 
 /**
  * Class MatrixMover starting the MatrixMover Programm.
  * 
  * Code-parts copied from http://github.com/neophob/PixelController
  * 
  * @author Gyver
  */
 
 public class MatrixMover {
 
     /** The log. */
     private static final Logger LOG = Logger.getLogger(MatrixMover.class.getName());
     /** The Constant CONFIG_FILENAME. */
     private static final String CONFIG_FILENAME = "data/config.properties";
     /** The Constant LAF_THEME. */
     private static final String LAF_THEME = "data/matrixmover.theme";
     /** The output. */
     private Output output;
     
     private static boolean guiReady = false;
 
     /** 
      * Prepare MatrixMover to start
      */
     private void setup() {
         
         LOG.log(Level.INFO, "MatrixMover Setup START");
 
         Properties config = new Properties();
         try {
             InputStream is = new FileInputStream(CONFIG_FILENAME);
             config.load(is);
             LOG.log(Level.INFO, "Config loaded, {0} entries", config.size());
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "Failed to load Config", e);
             throw new IllegalArgumentException("Configuration error!", e);
         }
 
         final PropertiesHelper ph = new PropertiesHelper(config);
 
         OutputDeviceEnum outputDeviceEnum = ph.getOutputDevice();
 
         try {
             switch (outputDeviceEnum) {
                 case NULL:
                     this.output = new NullDevice(ph);
                     break;
                 default:
                     throw new IllegalArgumentException("Unable to initialize unknown output device: " + outputDeviceEnum);
             }
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "Unable to initialize output device: " + outputDeviceEnum, e);
         }
 
         LOG.log(Level.INFO, "Starting Core");
         final Controller controller = Controller.getControllerInstance();
         controller.initController(ph);
 
         LOG.log(Level.FINER, "Loading NimROD LAF");
         try {
             NimRODTheme nt = new NimRODTheme(LAF_THEME);
 
             NimRODLookAndFeel NimRODLF = new NimRODLookAndFeel();
             NimRODLookAndFeel.setCurrentTheme(nt);
             UIManager.setLookAndFeel(NimRODLF);
         } catch (UnsupportedLookAndFeelException ex) {
             Logger.getLogger(MatrixMover.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         LOG.log(Level.INFO, "Starting Gui");
         
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 Frame guiFrame = Frame.getFrameInstance();
                 guiFrame.initFrame(ph, controller);
                 guiFrame.setSize(1000, 600);
                 guiFrame.centerWindow();
                 guiFrame.setVisible(true);
                 MatrixMover.guiReady(true);
             }
         });
 
         LOG.log(Level.INFO, "Starting timer with {0} FPS", ph.getFps());
         Timer fpsTimer = new Timer();
         long millisecondsDelay = 1000 / ph.getFps();
         
         //wait for gui to fully initialize
         while(!guiReady){
             try {
                 Thread.sleep(100);
             } catch (InterruptedException ex) {
                 Logger.getLogger(MatrixMover.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
         fpsTimer.scheduleAtFixedRate(new ExecutionTimerTask(controller), 1, millisecondsDelay);
         
        LOG.log(Level.INFO, "MatrixMover Setup END");
 
     }
     
     private static void guiReady(boolean ready){
         MatrixMover.guiReady = ready;
     }
 
     public static void main(String[] args) {
         MatrixMover matmove = new MatrixMover();
         matmove.setup();
     }
 }
