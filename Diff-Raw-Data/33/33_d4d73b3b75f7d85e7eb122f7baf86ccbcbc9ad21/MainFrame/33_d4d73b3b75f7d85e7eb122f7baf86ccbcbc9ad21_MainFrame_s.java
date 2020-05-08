 /**
  * Floor Plan Marker Project
  * Copyright (C) 2013  Vy Thuy Nguyen
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  * 
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
  * Boston, MA  02110-1301, USA.
  */
 
 package gui.view;
 
 import entity.*;
 import gui.util.FileChooserWindow;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.*;
 import java.io.IOException;
 import javax.persistence.EntityManager;
 import javax.swing.*;
 import util.DatabaseService;
 import util.FileService;
 import util.ImageFileFilter;
 
 /**
  * @author              Vy Thuy Nguyen
  * @version             1.0 Jan 9, 2013
  * Last modified:       
  */
 public class MainFrame extends JFrame 
 {
     //Dialog mgs
     private static final String closingMsg = "Do you want to save the current work before exiting?\n\n" 
                                              + "'Yes' to save and exit.\n"
                                              + "'No' to exit without saving.\n"   
                                              + "'Cancel' to go back.";
     
     //File Menu
     private JMenuItem newItem = new JMenuItem("New Floor Plan...");
     private JMenuItem saveAllPointSetsToFileItem = new JMenuItem("Save All Point Sets to File");
     private JMenuItem saveToDBItem = new JMenuItem("Save to Database");
     private JMenuItem configureItem = new JMenuItem("Configure...");
     //private JMenuItem saveBothItem = new JMenuItem("Save to Both File and Database");
     private JMenuItem exportItem = new JMenuItem("Export Marked Floor Plan...");
     private JMenuItem exportWithDeadCellItem = new JMenuItem("Export Floor Plan with Dead Cells...");
     private JMenuItem showExistItem = new JMenuItem("Show Saved Floor Plans... ");
     boolean ignoreRatioChangeWarning;
     
     //Help Menu
     private JMenuItem aboutItem = new JMenuItem("About");
     private JMenuItem reportItem = new JMenuItem("Report Issue");
     private JFrame floorPlanChooserFr = null;
     
     //Current cursor pos
     private JTextField currentPos = new JTextField("");
     
     //Ratio of graph
     private int ratio = 2; //default value
     
     //Actual width of the floor plan in real life
     private int actualW = 1;
     
     //Actual height of the floor plan in real life
     private int actualH = 1;    
     
     //Main content
     TabbedPanel mainContent;
     
     public MainFrame()      
     {  
         //Init this JFrame
         initFrame();
         
         //Set up menu
         setupMenu();
         
         //Init Main panel
         mainContent = new TabbedPanel(this);
         
         this.add(mainContent, BorderLayout.CENTER);
         this.updateImageCanvasSize();
         
         JPanel statusPanel = new JPanel();
         statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         currentPos.setEditable(false);   
         currentPos.setPreferredSize(new Dimension(this.getWidth(), 16));
         currentPos.setBorder(null);
         statusPanel.add(currentPos);
         this.add(statusPanel, BorderLayout.SOUTH);
         
         //Resize listener
         getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener(){
             @Override
             public void ancestorMoved(HierarchyEvent e) {
                // System.out.println(e);              
             }
             
             @Override
             public void ancestorResized(HierarchyEvent e) {
                 updateImageCanvasSize();                 
             }           
         });    
         
         pack();
         
         ignoreRatioChangeWarning = true;
         configureItem.doClick();
     }
  
     private void initFrame()
     {
         JFrame.setDefaultLookAndFeelDecorated(false);
         this.setVisible(true);
         this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         this.setSize(880, 700);
         this.setLocationRelativeTo(null);
         this.setTitle("Floor Plan Viewer");
         this.setLayout(new BorderLayout());
         
         this.addWindowListener(new WindowListener(){
 
             @Override
             public void windowOpened(WindowEvent e){}
 
             @Override
             public void windowClosing(WindowEvent e)
             {
                 int option = JOptionPane.showConfirmDialog(null, closingMsg, 
                                                                  "Exiting?", 
                                                                  JOptionPane.YES_NO_CANCEL_OPTION);
                 switch (option)
                 {
                     case JOptionPane.YES_OPTION:
                         saveCurrentWorkToDB();  //fall thru intentionally
                         
                     case JOptionPane.NO_OPTION:
                         MainFrame.this.dispose();
                         System.exit(option);
                         break;
                         
                     default: //do nothing
                         break;
                 }
                 
             }
 
             @Override
             public void windowClosed(WindowEvent e){}
 
             @Override
             public void windowIconified(WindowEvent e){}
 
             @Override
             public void windowDeiconified(WindowEvent e){}
 
             @Override
             public void windowActivated(WindowEvent e){}
 
             @Override
             public void windowDeactivated(WindowEvent e){}
         });
     }
     
     public void newFloorPlanItemClicked()
     {
         newItem.setSelected(true);
     }
     
     private void setupMenu()
     {
         JMenuBar menuBar = new JMenuBar();
         
         //File Menu
         JMenu fileMenu = new JMenu("File");
         
         //New item
         newItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {                    
                 //Bring up floor plan chooser popup window
                 if (floorPlanChooserFr == null)
                     floorPlanChooserFr = new FileChooserWindow(mainContent.getPointMarkingPn().getUI(), new ImageFileFilter());
                 
                 floorPlanChooserFr.setVisible(true);
             }
         });
         newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
         fileMenu.add(newItem);
         fileMenu.addSeparator();
         
         //Show existing item
         showExistItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
         showExistItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 mainContent.getPointMarkingPn().getUI().showFloorPlanListPopup();
             }
         });
         fileMenu.add(showExistItem);
         fileMenu.addSeparator();
         
         //Save All point sets item
         saveAllPointSetsToFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
         saveAllPointSetsToFileItem.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 FloorPlan fp = mainContent.getPointMarkingPn().getUI().getCurrentFloorPlan();
                 if (fp != null)
                 {
                     try
                     {
                         FileService.saveAllPointSetsToFile(fp);
                     }
                     catch (IOException exc)
                     {
                         JOptionPane.showMessageDialog(null, 
                                                       "Error writing to file", 
                                                       "ERROR", 
                                                       JOptionPane.ERROR_MESSAGE);
                     }
                 }
                 else
                 {
                     JOptionPane.showMessageDialog(null, 
                                                   "Please select a floor  plan and create a point set first.", 
                                                   "ERROR", 
                                                   JOptionPane.ERROR_MESSAGE);
                 }
             }
         });
         fileMenu.add(saveAllPointSetsToFileItem);
         
         //Save Floor plan to DB
         saveToDBItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
         saveToDBItem.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 saveCurrentWorkToDB();                
             }
         });
         fileMenu.add(saveToDBItem);
         fileMenu.addSeparator();
         
         //Export floor plan to image file
         exportItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (mainContent.getPointMarkingPn().getUI().hasImage())
                 {
                     mainContent.getPointMarkingPn().getUI().exportImage();
                 }
                 else
                 {
                     JOptionPane.showMessageDialog(null,
                                                   "Please load a floor plan and mark it first", 
                                                   "No Image to Export",
                                                   JOptionPane.ERROR_MESSAGE);
                 }
             }
         
         });
         exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
         fileMenu.add(exportItem);
         
         //Export floor plan with dead cells
         exportWithDeadCellItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (mainContent.getPointMarkingPn().getUI().hasImage())
                 {
                     try
                     {
                         FileService.exportFloorPlanWithDeadCells(mainContent.pointMarkingPn.getUI().getCurrentFloorPlan());
                         JOptionPane.showMessageDialog(null, "Successfully export image");
                     }
                     catch (IOException ex)
                     {
                         JOptionPane.showMessageDialog(null,
                                                   "Error while exported image.", 
                                                   "ERROR",
                                                   JOptionPane.ERROR_MESSAGE);
                     }
                 }
                 else
                 {
                     JOptionPane.showMessageDialog(null,
                                                   "Please load a floor plan and mark it first", 
                                                   "No Image to Export",
                                                   JOptionPane.ERROR_MESSAGE);
                 }
             }
         });
         fileMenu.add(exportWithDeadCellItem);
         fileMenu.addSeparator();
         
         //Set ratio, width, height
         configureItem.addActionListener(new ActionListener(){
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 int result; 
                 if (MainFrame.this.ignoreRatioChangeWarning)
                 {
                     result = JOptionPane.YES_OPTION;
                     MainFrame.this.ignoreRatioChangeWarning = false;
                 }
                 else
                 {
                     result = JOptionPane.showConfirmDialog(null, 
                                                 "Warning: changing ratio may result in unexpected behavior and you would lose all your work on the current floor plan.\nDo you want to continue?",
                                                 "Are you sure?",
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.WARNING_MESSAGE);
                 }
                 
                 if (result == JOptionPane.YES_OPTION)
                 {
                     //Proceed
                     String strVal = "";
                     int irat = ratio, iW = actualW, iH = actualH;
                     boolean isValid = false;
                     while (!isValid)
                     {
                         try
                         {
                             //ratio
                             strVal = JOptionPane.showInputDialog(null, 
                                                         "Enter new value x for the ratio.\nx means the width and height of a cell would be x% the width and height of the floor plan, respectively.\nA reasonable value is between 2 and 5.", 
                                                         "New Ratio", 
                                                         JOptionPane.INFORMATION_MESSAGE);
                             irat = Integer.parseInt(strVal);
                             
                             //width
                             strVal = JOptionPane.showInputDialog(null, 
                                                         "Enter the actual width of the floor plan in real life", 
                                                         "Actual Width", 
                                                         JOptionPane.INFORMATION_MESSAGE);
                             iW = Integer.parseInt(strVal);
                             
                             //height
                             strVal = JOptionPane.showInputDialog(null, 
                                                         "Enter the actual height of the floor plan in real life", 
                                                         "Actual Height", 
                                                         JOptionPane.INFORMATION_MESSAGE);
                             iH = Integer.parseInt(strVal);
                             
                             isValid = true;
                         }
                         catch (Exception exc)
                         {
                            // exc.printStackTrace();
                             isValid = false;
                         }
                     }
                     
                     ratio = irat;
                     actualW = iW;
                     actualH = iH;
                     
                     //update current work if applicable
                     if (MainFrame.this.hasFloorPlan())
                     {
                         MainFrame.this.updateFloorPlanConfig();
                     }
                 }
                 else
                 {
                     //Does nothing.
                 }
             }
 
         
         });
         fileMenu.add(configureItem);
         menuBar.add(fileMenu); 
         //end File Menu
         
         //Help Menu
 //        JMenu helpMenu = new JMenu("Help");
 //        helpMenu.add(reportItem);
 //        helpMenu.add(aboutItem);
 //        menuBar.add(helpMenu);
 //        
         this.setJMenuBar(menuBar);    
     }
     
     private void saveCurrentWorkToDB()
     {
         FloorPlan fp = mainContent.getPointMarkingPn().getUI().getCurrentFloorPlan();
         if (fp != null)
         {
             EntityManager em = DatabaseService.getEntityManager();
             em.getTransaction().begin();
 
             //Persist pointSets
             for (PointSet ps: fp.getPointSets())
             {
                 //Persist points
                 for (Point p : ps.getPoints())
                     em.persist(p);
 
                 em.persist(ps);
             }
 
             //Persist deadCells and afp
             AnnotFloorPlan afp = fp.getAnnotFloorPlan();
             for (DeadPoint dp : afp.getDeadPoints())
             {
                 dp.setAnnotFloorPlan(afp);
                 em.persist(dp);
             }
             
             em.persist(afp);
             em.persist(fp);
             em.getTransaction().commit();
             
             DatabaseService.closeConnection();
             JOptionPane.showMessageDialog(null, "Successfully Saved To Database");
         }
     }
     
     /**
      * Update floor plan area based on current window's size
      */
     private void updateImageCanvasSize()
     {
         mainContent.updateImagePanelScrollPaneSize(this.getWidth() - 350, this.getHeight() - 152);
     }
 
     void setCurrentPositionlabel(String str)
     {
         currentPos.setText(str);
     }
 
     public FloorPlan getCurrentFloorPlan()
     {
         return mainContent.getPointMarkingPn().getUI().getCurrentFloorPlan();
     }
 
     public int getRatio()
     {
         return ratio;
     }
     
     public int getActualW()
     {
         return actualW;
     }
     
     public int getActualH()
     {
         return actualH;
     }
 
     private boolean hasFloorPlan()
     {
         return this.mainContent.pointMarkingPn.getUI().getCurrentFloorPlan() != null;
     }
 
     /**
      * Update ratio, actual width, actual height of floor plan.
      */
     private void updateFloorPlanConfig()
     {
        this.mainContent.pointMarkingPn.getUI().getCurrentFloorPlan().getAnnotFloorPlan().updateConfig(ratio, actualW, actualH);
        
     }
     
     private class MenuListener implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             if (e.getSource() == newItem)
             {                       
                 //Bring up floor plan chooser popup window
                 if (floorPlanChooserFr == null)
                     floorPlanChooserFr = new FileChooserWindow(mainContent.getPointMarkingPn().getUI(), new ImageFileFilter());
                 
                 floorPlanChooserFr.setVisible(true);
             }            
         }        
     }
 }
