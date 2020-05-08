 /*
  * ChibiPaintMod
  *     Copyright (c) 2012-2014 Sergey Semushin
  *     Copyright (c) 2006-2008 Marc Schefer
  *
  *     This file is part of ChibiPaintMod (previously ChibiPaint).
  *
  *     ChibiPaintMod is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     ChibiPaintMod is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with ChibiPaintMod. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package chibipaint;
 
 import chibipaint.controller.CPControllerApplication;
 import chibipaint.engine.CPArtwork;
 import chibipaint.gui.CPLayersPalette;
 import chibipaint.gui.CPMainGUI;
 
 import javax.swing.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.prefs.Preferences;
 
 public class ChibiApp extends JFrame
 {
 
 /**
  *
  */
 private static final long serialVersionUID = 1L;
 private final CPControllerApplication controller;
 private final CPMainGUI mainGUI;
 
 public enum appState
 {
   FREE,
   SAVING,
   LOADING,
   TRANSFORM,
 }
 
 private appState curAppState = appState.FREE;
 
 void transformIsInProgressMsgBox ()
 {
   JOptionPane.showMessageDialog (null, "Please finish or cancel currently ongoing transformation before exiting application", "Transformation is in progress", JOptionPane.INFORMATION_MESSAGE);
 }
 
 // Returns true if it is ok to continue operation
 public boolean confirmDialog ()
 {
   if (this.controller.changed)
     {
       int result = JOptionPane.showConfirmDialog (this,
                                                   "Save the Changes to Image '" +
                                                           (this.controller.getCurrentFile () != null ? this.controller.getCurrentFile ().getName ()
                                                                                                      : "Untitled") + "' Before Closing?", "Existing file",
                                                   JOptionPane.YES_NO_CANCEL_OPTION);
       switch (result)
         {
         case JOptionPane.YES_OPTION:
           return this.controller.save ();
         case JOptionPane.NO_OPTION:
           break;
         case JOptionPane.CLOSED_OPTION:
         case JOptionPane.CANCEL_OPTION:
         default:
           return false;
         }
     }
   return true;
 }
 
 private ChibiApp ()
 {
   super ("ChibiPaintMod");
 
   controller = new CPControllerApplication (this);
 
   controller.setCurrentFile (null);
   controller.setArtwork (new CPArtwork (600, 450));
 
   controller.loadUrgentSettings ();
   mainGUI = new CPMainGUI (controller);
 
   setContentPane (mainGUI.getGUI ());
   setJMenuBar (mainGUI.getMenuBar ());
 
 
   mainGUI.getPaletteManager ().loadPalettesSettings ();
   controller.canvas.loadCanvasSettings ();
   controller.loadControllerSettings ();
 
   final ChibiApp frame = this;
   controller.canvas.initialUpdatesAfterCanvasCreation ();
 
   frame.addWindowListener (new WindowAdapter ()
   {
     @Override
     public void windowClosing (WindowEvent e)
     {
       if (curAppState == appState.TRANSFORM)
         {
           transformIsInProgressMsgBox ();
           return;
         }
       while (curAppState != appState.FREE)
         {
           try
             {
               Thread.sleep (100);
             }
           catch (InterruptedException e1)
             {
               e1.printStackTrace ();
             }
           // Waiting while it would be safe exit (all changes saved)
         }
 
       if (!confirmDialog ())
         return;
 
       SaveWindowSettings (frame);
       mainGUI.getPaletteManager ().savePalettesSettings ();
       controller.canvas.saveCanvasSettings ();
       controller.canvas.killTimers ();
       controller.saveControllerSettings ();
       dispose ();
     }
   });
 }
 
 private static void LoadWindowSettings (JFrame frame)
 {
 
   Preferences userRoot = Preferences.userRoot ();
   Preferences preferences = userRoot.node ("chibipaintmod");
   int s = preferences.getInt ("window_state", -1);
   int h = preferences.getInt ("window_height", -1);
   int w = preferences.getInt ("window_width", -1);
   if (s != -1)
     {
       frame.setExtendedState (s);
     }
   if (h != -1 && w != -1)
     frame.setSize (w, h);
   else
     frame.setSize (800, 600);
 
   frame.validate ();
   frame.setVisible (true);
 }
 
 private static void SaveWindowSettings (JFrame frame)
 {
 
   Preferences userRoot = Preferences.userRoot ();
   Preferences preferences = userRoot.node ("chibipaintmod");
   int s = frame.getExtendedState ();
   preferences.putInt ("window_state", s);
   int h = frame.getHeight ();
   int w = frame.getWidth ();
   preferences.putInt ("window_height", h);
   preferences.putInt ("window_width", w);
 }
 
 private static void createChibiApp ()
 {
   JFrame frame = new ChibiApp ();
   frame.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
   LoadWindowSettings (frame);
 }
 
 public static void main (String[] args)
 {
   javax.swing.SwingUtilities.invokeLater (new Runnable ()
   {
 
     @Override
     public void run ()
     {
       createChibiApp ();
     }
   });
 }
 
 public void recreateEverything (CPArtwork artwork)
 {
   controller.setArtwork (artwork);
   controller.canvas.reinitCanvas (); // Reinit canvas
  CPLayersPalette layersPalette = (CPLayersPalette) mainGUI.getPaletteManager ().getPalettes ().get ("layers");
  layersPalette.addListener ();
  layersPalette.updateScroll ();
  layersPalette.updateArtworkState ();
  layersPalette.layerChange (artwork);
 
   mainGUI.updateRecentFiles (); // TODO: Change this to correct update of recent files
 
 }
 
 public void resetMainMenu ()
 {
   controller.canvas.saveCanvasSettings ();
   mainGUI.recreateMenuBar ();
   controller.canvas.loadCanvasSettings ();
 
   setJMenuBar (mainGUI.getMenuBar ());
 }
 
 public void setAppState (appState value)
 {
   curAppState = value;
   controller.updateTitle ();
 }
 
 public appState getAppState ()
 {
   return curAppState;
 }
 }
