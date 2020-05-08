 /*
  * OM Simulation Tool: This software is a simulation tool for virtual
  * orientated measurement (OM) campaigns following the protocol "6+1" to
  * determine and evaluate the level of radon exposure in buildings.
  * 
  * Copyright (C) 2012 Alexander Schoedon <a.schoedon@student.htw-berlin.de>
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.bfs.radon.omsimulation;
 
 import java.awt.EventQueue;
 import java.awt.Font;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 
 import de.bfs.radon.omsimulation.gui.OMPanelAbout;
 import de.bfs.radon.omsimulation.gui.OMPanelData;
 import de.bfs.radon.omsimulation.gui.OMPanelImport;
 import de.bfs.radon.omsimulation.gui.OMPanelResults;
 import de.bfs.radon.omsimulation.gui.OMPanelSimulation;
 import de.bfs.radon.omsimulation.gui.OMPanelTesting;
 
 /**
  * Public class OMMainFrame, which is the main entry point of this software.
  * Creates and shows the main JFrame for this software tool.
  * 
  * @author A. Schoedon
  * 
  */
 public class OMMainFrame extends JFrame {
 
   /**
    * Unique serial version ID.
    */
   private static final long serialVersionUID = 639348294183394699L;
 
   /**
    * Stores the tool's version string.
    */
  private static final String version = "v0.4.46-beta4";
 
   /**
    * Stores the tabbed pane used for navigating throught the panels.
    */
   protected JTabbedPane tabbedPane;
 
   /**
    * Stores the panel for importing CSV data.
    */
   protected OMPanelImport jpanelImport;
 
   /**
    * Stores the panel for inspecting the imported data.
    */
   protected OMPanelData jpanelData;
 
   /**
    * Stores the panel for running simulations.
    */
   protected OMPanelSimulation jpanelSimulation;
 
   /**
    * Stores the panel for viewing the simulation results.
    */
   protected OMPanelResults jpanelResults;
 
   /**
    * Stores the panel for analyzing the simulation results.
    */
   protected OMPanelTesting jpanelTesting;
 
   /**
    * Stores the panel for the credits.
    */
   protected OMPanelAbout jpanelAbout;
 
   /**
    * Gets the tool's version string.
    */
   public static String getVersion() {
     return version;
   }
 
   /**
    * Launches the application and displays the main frame.
    */
   public static void main(String[] args) {
     EventQueue.invokeLater(new Runnable() {
       public void run() {
         try {
           OMMainFrame frame = new OMMainFrame();
           frame.setVisible(true);
         } catch (Exception e) {
           JOptionPane.showMessageDialog(
               null,
               "Failed to run OM Simulation Tool " + getVersion() + ".\n"
                   + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
       }
     });
   }
 
   /**
    * Creates the main frame of the OM-Simulation tool, sets size and adds tabs
    * and panels. To find out more about the content of the tabs, see the
    * documentation of the single panels.
    */
   public OMMainFrame() {
     setTitle("OM Simulation Tool " + getVersion());
     setResizable(false);
     setBounds(100, 100, 800, 600);
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     getContentPane().setLayout(null);
     tabbedPane = new JTabbedPane(JTabbedPane.TOP);
     tabbedPane.setBounds(10, 11, 772, 551);
     tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
     getContentPane().add(tabbedPane);
     jpanelImport = new OMPanelImport();
     tabbedPane.addTab("Import", null, jpanelImport, null);
     jpanelData = new OMPanelData();
     tabbedPane.addTab("Data", null, jpanelData, null);
     jpanelSimulation = new OMPanelSimulation();
     tabbedPane.addTab("Simulation", null, jpanelSimulation, null);
     jpanelResults = new OMPanelResults();
     tabbedPane.addTab("Results", null, jpanelResults, null);
     jpanelTesting = new OMPanelTesting();
     tabbedPane.addTab("Analyse", null, jpanelTesting, null);
     jpanelAbout = new OMPanelAbout(getVersion());
     tabbedPane.addTab("About", null, jpanelAbout, null);
   }
 }
