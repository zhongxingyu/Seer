 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
 import com.javadocking.DockingManager;
 import com.javadocking.dock.FloatDock;
 import com.javadocking.dock.Position;
 import com.javadocking.dock.SplitDock;
 import com.javadocking.dock.TabDock;
 import com.javadocking.dockable.DefaultDockable;
 import com.javadocking.dockable.Dockable;
 import com.javadocking.dockable.DockingMode;
 import com.javadocking.model.FloatDockModel;
 import graphview.GraphNode;
 import graphview.GraphScene;
 import graphview.GraphUtils;
 import graphview.layouts.HierarchicalLayoutK;
 import java.awt.*;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.activation.DataHandler;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.plaf.nimbus.NimbusLookAndFeel;
 import parser.parserXML;
 
 /**
  *
  * @author Kirill
  */
 public class MainFrame extends javax.swing.JFrame {
     
     private class TransferableImage implements Transferable 
     {
 
         Image i;
 
         public TransferableImage( Image i ) {
             this.i = i;
         }
 
         public Object getTransferData( DataFlavor flavor )
         throws UnsupportedFlavorException, IOException {
             if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
                 return i;
             }
             else {
                 throw new UnsupportedFlavorException( flavor );
             }
         }
 
         public DataFlavor[] getTransferDataFlavors() {
             DataFlavor[] flavors = new DataFlavor[ 1 ];
             flavors[ 0 ] = DataFlavor.imageFlavor;
             return flavors;
         }
 
         public boolean isDataFlavorSupported( DataFlavor flavor ) {
             DataFlavor[] flavors = getTransferDataFlavors();
             for ( int i = 0; i < flavors.length; i++ ) {
                 if ( flavor.equals( flavors[ i ] ) ) {
                     return true;
                 }
             }
 
             return false;
         }
     }
 
     
     ArrayList<String> openedDoc=new ArrayList<String>();
     File fileProperties = new File("res/properties/openedFiles.xml");
     parserXML myParser=new parserXML(openedDoc);
     
     GraphScene scene=null;
     MainPanel mainPanel;
     Properties properties;
     
     /**
      * Creates new form MainFrame
      */
     public MainFrame() throws IOException {
         initComponents();
         openProperties();
         initUI();        
     }
     
     public void initUI()
     {
         scene=new GraphScene();
         mainPanel=new MainPanel(this,scene);
         add(mainPanel);
         
         properties=new Properties(this, true);
         
         JPanel mainPanel1=mainPanel;//new JPanel();
         mainPanel1.setBackground(Color.yellow);
  
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(mainPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(mainPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         this.setLocation((screenSize.width - this.getWidth()) / 2, (screenSize.height - this.getHeight()) / 2);
     };
     
     public void openProperties() throws IOException
     {     
         jMenu7.removeAll();
         if(!fileProperties.exists())
         {
             fileProperties.createNewFile();
         }       
         else
         {
             BufferedReader propertiesText = new BufferedReader (new FileReader(fileProperties));
             String temp="";
             ArrayList<String> tempData=new ArrayList<String>();
             while((temp = propertiesText.readLine()) != null)
             {
                 tempData.add(temp);
             }
             openedDoc=myParser.openedFilesPars(tempData);
             
             for(int i=0;i<openedDoc.size();i++)
             {
                 JMenuItem jMenuItemNew = new javax.swing.JMenuItem();
                 jMenuItemNew.setText(openedDoc.get(i));
                 jMenuItemNew.addActionListener(new java.awt.event.ActionListener()
                 {
                     public void actionPerformed(java.awt.event.ActionEvent evt) 
                     {
                         File file = new File(evt.getActionCommand());      
                         if(file.exists())
                         {
                             scene.loadDot(file.getPath());
 
                             mainPanel.structure.updateTables();
 
                             if(openedDoc.size()!=0)
                             {
                                 while(openedDoc.contains(file.getPath()))
                                 {
                                     openedDoc.remove(file.getPath());
                                 }
                                 if(openedDoc.size()>4)
                                 {
                                     for(int i=openedDoc.size()-1;i>0;i--)
                                     {
                                         openedDoc.set(i, openedDoc.get(i-1));
                                     }
                                     openedDoc.set(0, file.getPath());
                                 }
                                 else
                                 {
                                     openedDoc.add(null);
                                     for(int i=openedDoc.size()-1;i>0;i--)
                                     {
                                         openedDoc.set(i, openedDoc.get(i-1));
                                     }
                                     openedDoc.set(0, file.getPath());
                                 }
 
                             }
                             else
                             {
                                 openedDoc.add(file.getPath());
                             }
 
                             PrintWriter propertiesText;
                             try {
                                 propertiesText = new PrintWriter(fileProperties.getAbsoluteFile());
                                 propertiesText.print(myParser.openedFilesToWrite(openedDoc));
                                 propertiesText.flush();                                
                             } catch (FileNotFoundException ex) {
                                 Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                             }   
 
                             try {
                                 openProperties();
                             } catch (IOException ex) {
                                 Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                         else
                         {
                             JOptionPane.showMessageDialog(mainPanel,"File not exists!");
                         }
                             
                     }
                 });                
                 jMenu7.add(jMenuItemNew);
 
             }
         }
         
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenuItemFileOpen = new javax.swing.JMenuItem();
         jMenu7 = new javax.swing.JMenu();
         jMenu2 = new javax.swing.JMenu();
         jMenuItemSaveToImage = new javax.swing.JMenuItem();
         jMenuItemCopyToClipboard = new javax.swing.JMenuItem();
         jSeparator3 = new javax.swing.JPopupMenu.Separator();
         jMenuItem1 = new javax.swing.JMenuItem();
         jMenu3 = new javax.swing.JMenu();
         jMenu4 = new javax.swing.JMenu();
         jMenuItemNimbusSkin = new javax.swing.JMenuItem();
         jMenuItemMetalSkin = new javax.swing.JMenuItem();
         jMenuItemMotifSkin = new javax.swing.JMenuItem();
         jMenuItemWindowsSkin = new javax.swing.JMenuItem();
         jMenuItemWindowsClas = new javax.swing.JMenuItem();
         jSeparator4 = new javax.swing.JPopupMenu.Separator();
         jMenuItem2 = new javax.swing.JMenuItem();
         jMenuItem3 = new javax.swing.JMenuItem();
         jMenuItem4 = new javax.swing.JMenuItem();
         jMenu5 = new javax.swing.JMenu();
         jMenuItem5 = new javax.swing.JMenuItem();
         jMenuItem10 = new javax.swing.JMenuItem();
         jMenuItem11 = new javax.swing.JMenuItem();
         jMenuItemTestLayout = new javax.swing.JMenuItem();
         jMenuHighlight = new javax.swing.JMenu();
         jMenuItemHClusters = new javax.swing.JMenuItem();
         jMenuItemHCycles = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JPopupMenu.Separator();
         jMenuItemHSelCluster = new javax.swing.JMenuItem();
         jMenuItemHSelCycle = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JPopupMenu.Separator();
         jMenuItemHClear = new javax.swing.JMenuItem();
         jMenu6 = new javax.swing.JMenu();
         jMenuItem6 = new javax.swing.JMenuItem();
         jMenuItem7 = new javax.swing.JMenuItem();
         jMenuItem8 = new javax.swing.JMenuItem();
         jMenuItem9 = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("GraphBuilder");
         addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentResized(java.awt.event.ComponentEvent evt) {
                 formComponentResized(evt);
             }
         });
 
         jMenu1.setText("File");
 
         jMenuItemFileOpen.setText("Open...");
         jMenuItemFileOpen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemFileOpenActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuItemFileOpen);
 
         jMenu7.setText("Recent Documents");
         jMenu1.add(jMenu7);
 
         jMenuBar1.add(jMenu1);
 
         jMenu2.setText("Edit");
 
         jMenuItemSaveToImage.setText("Save to image...");
         jMenuItemSaveToImage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemSaveToImageActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuItemSaveToImage);
 
         jMenuItemCopyToClipboard.setText("Copy to clipboard");
         jMenuItemCopyToClipboard.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemCopyToClipboardActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuItemCopyToClipboard);
         jMenu2.add(jSeparator3);
 
         jMenuItem1.setText("Properties");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuItem1);
 
         jMenuBar1.add(jMenu2);
 
         jMenu3.setText("View");
 
         jMenu4.setText("Skins");
 
         jMenuItemNimbusSkin.setText("Nimbus");
         jMenuItemNimbusSkin.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemNimbusSkinActionPerformed(evt);
             }
         });
         jMenu4.add(jMenuItemNimbusSkin);
 
         jMenuItemMetalSkin.setText("Metal");
         jMenuItemMetalSkin.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemMetalSkinActionPerformed(evt);
             }
         });
         jMenu4.add(jMenuItemMetalSkin);
 
         jMenuItemMotifSkin.setText("CDE/Motif");
         jMenuItemMotifSkin.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemMotifSkinActionPerformed(evt);
             }
         });
         jMenu4.add(jMenuItemMotifSkin);
 
         jMenuItemWindowsSkin.setText("Windows");
         jMenuItemWindowsSkin.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemWindowsSkinActionPerformed(evt);
             }
         });
         jMenu4.add(jMenuItemWindowsSkin);
 
         jMenuItemWindowsClas.setText("Windows Classic");
         jMenuItemWindowsClas.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemWindowsClasActionPerformed(evt);
             }
         });
         jMenu4.add(jMenuItemWindowsClas);
 
         jMenu3.add(jMenu4);
         jMenu3.add(jSeparator4);
 
         jMenuItem2.setText("Zoom in");
         jMenu3.add(jMenuItem2);
 
         jMenuItem3.setText("Zoom out");
         jMenu3.add(jMenuItem3);
 
         jMenuItem4.setText("Fit");
         jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem4ActionPerformed(evt);
             }
         });
         jMenu3.add(jMenuItem4);
 
         jMenuBar1.add(jMenu3);
 
         jMenu5.setText("Layout");
 
         jMenuItem5.setText("Simple");
         jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem5ActionPerformed(evt);
             }
         });
         jMenu5.add(jMenuItem5);
 
         jMenuItem10.setText("Ierarchy_beta");
         jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem10ActionPerformed(evt);
             }
         });
         jMenu5.add(jMenuItem10);
 
         jMenuItem11.setText("Radial");
         jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem11ActionPerformed(evt);
             }
         });
         jMenu5.add(jMenuItem11);
 
        jMenuItemTestLayout.setText("Test layout");
         jMenuItemTestLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemTestLayoutActionPerformed(evt);
             }
         });
         jMenu5.add(jMenuItemTestLayout);
 
         jMenuBar1.add(jMenu5);
 
         jMenuHighlight.setText("Highlight");
 
         jMenuItemHClusters.setText("Highlight clusters");
         jMenuItemHClusters.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemHClustersActionPerformed(evt);
             }
         });
         jMenuHighlight.add(jMenuItemHClusters);
 
         jMenuItemHCycles.setText("Highlight cycles");
         jMenuItemHCycles.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemHCyclesActionPerformed(evt);
             }
         });
         jMenuHighlight.add(jMenuItemHCycles);
         jMenuHighlight.add(jSeparator1);
 
         jMenuItemHSelCluster.setText("Highlight selected cluster");
         jMenuItemHSelCluster.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemHSelClusterActionPerformed(evt);
             }
         });
         jMenuHighlight.add(jMenuItemHSelCluster);
 
         jMenuItemHSelCycle.setText("Highlight selected cycle");
         jMenuItemHSelCycle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemHSelCycleActionPerformed(evt);
             }
         });
         jMenuHighlight.add(jMenuItemHSelCycle);
         jMenuHighlight.add(jSeparator2);
 
         jMenuItemHClear.setText("Clear");
         jMenuItemHClear.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemHClearActionPerformed(evt);
             }
         });
         jMenuHighlight.add(jMenuItemHClear);
 
         jMenuBar1.add(jMenuHighlight);
 
         jMenu6.setText("Windows");
 
         jMenuItem6.setText("Show/hide scene");
         jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem6ActionPerformed(evt);
             }
         });
         jMenu6.add(jMenuItem6);
 
         jMenuItem7.setText("Object Properties");
         jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem7ActionPerformed(evt);
             }
         });
         jMenu6.add(jMenuItem7);
 
         jMenuItem8.setText("Overview");
         jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem8ActionPerformed(evt);
             }
         });
         jMenu6.add(jMenuItem8);
 
         jMenuItem9.setText("Structure view");
         jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem9ActionPerformed(evt);
             }
         });
         jMenu6.add(jMenuItem9);
 
         jMenuBar1.add(jMenu6);
 
         setJMenuBar(jMenuBar1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 1198, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 721, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
         //mainPanel.setSize(getWidth()-20, getHeight()-65);
     }//GEN-LAST:event_formComponentResized
 
     private void jMenuItemNimbusSkinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNimbusSkinActionPerformed
         setSkin("Nimbus");
         SwingUtilities.updateComponentTreeUI(this);
     }//GEN-LAST:event_jMenuItemNimbusSkinActionPerformed
 
     private void jMenuItemMetalSkinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMetalSkinActionPerformed
         setSkin("Metal");
         SwingUtilities.updateComponentTreeUI(this);
     }//GEN-LAST:event_jMenuItemMetalSkinActionPerformed
 
     private void jMenuItemMotifSkinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMotifSkinActionPerformed
         setSkin("CDE/Motif");
         SwingUtilities.updateComponentTreeUI(this);
     }//GEN-LAST:event_jMenuItemMotifSkinActionPerformed
 
     private void jMenuItemWindowsSkinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemWindowsSkinActionPerformed
         setSkin("Windows");
         SwingUtilities.updateComponentTreeUI(this);
     }//GEN-LAST:event_jMenuItemWindowsSkinActionPerformed
 
     private void jMenuItemWindowsClasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemWindowsClasActionPerformed
         setSkin("Windows Classic");
         SwingUtilities.updateComponentTreeUI(this);
     }//GEN-LAST:event_jMenuItemWindowsClasActionPerformed
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         //Properties
         properties.setLocation(
                 this.getLocationOnScreen().x+
                 this.getWidth()/2-
                 properties.getWidth()/2,
                 this.getLocationOnScreen().y+
                 this.getHeight()/2-
                 properties.getHeight()/2 
                 );
         properties.setVisible(true);
       
     }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     private void jMenuItemFileOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFileOpenActionPerformed
         final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
         int returnVal = fc.showOpenDialog(this);
 
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             
             if(!scene.loadDot(file.getPath())) {
                 JOptionPane.showMessageDialog(this, "Load failed!");
             }
             else
             {
                 mainPanel.structure.updateTables();
 
                 if(openedDoc.size()!=0)
                 {
                     while(openedDoc.contains(file.getPath()))
                     {
                         openedDoc.remove(file.getPath());
                     }
                     if(openedDoc.size()>4)
                     {
                         for(int i=openedDoc.size()-1;i>0;i--)
                         {
                             openedDoc.set(i, openedDoc.get(i-1));
                         }
                         openedDoc.set(0, file.getPath());
                     }
                     else
                     {
                         openedDoc.add(null);
                         for(int i=openedDoc.size()-1;i>0;i--)
                         {
                             openedDoc.set(i, openedDoc.get(i-1));
                         }
                         openedDoc.set(0, file.getPath());
                     }
 
                 }
                 else
                 {
                     openedDoc.add(file.getPath());
                 }
                 
                  PrintWriter propertiesText;
                  try {
                      propertiesText = new PrintWriter(fileProperties.getAbsoluteFile());
                      propertiesText.print(myParser.openedFilesToWrite(openedDoc));
                      propertiesText.flush();                     
                  } catch (FileNotFoundException ex) {
                      Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                 }   
                  
                  try {
                         openProperties();
                     } catch (IOException ex) {
                         Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
             
             
         }        
     }//GEN-LAST:event_jMenuItemFileOpenActionPerformed
 
     private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
         // show/hide scene
         mainPanel.hideDock("Scene");
         
     }//GEN-LAST:event_jMenuItem6ActionPerformed
 
     private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
         // Object Properties
         mainPanel.hideDock("ObjectProperties");
         
     }//GEN-LAST:event_jMenuItem7ActionPerformed
 
     private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
         // Overview
         mainPanel.hideDock("Overview");
         
     }//GEN-LAST:event_jMenuItem8ActionPerformed
 
     private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
         // Structure
         mainPanel.hideDock("Structure");
         
     }//GEN-LAST:event_jMenuItem9ActionPerformed
 
     private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
         scene.applySimpleLayout();
     }//GEN-LAST:event_jMenuItem5ActionPerformed
 
     private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
         scene.fitScene();
     }//GEN-LAST:event_jMenuItem4ActionPerformed
 
     private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
         scene.applyTestLayout();
     }//GEN-LAST:event_jMenuItem10ActionPerformed
 
     private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
         scene.applyRadialLayout();
     }//GEN-LAST:event_jMenuItem11ActionPerformed
 
     public void highlightSelected(ArrayList<ArrayList<GraphNode>> clusters)
     {
         scene.clearHighlight();
         Color c=null;
         for(int i=0;i<clusters.size();i++)
         {
             c=GraphUtils.nextColor(c);
             for(int j=0;j<clusters.get(i).size();j++)
             {
                 clusters.get(i).get(j).getAspect().addHighlight(c);
             };
         };
         scene.updateScene();
     };
     
     private void jMenuItemHClustersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHClustersActionPerformed
         ArrayList<ArrayList<GraphNode>> clusters=GraphUtils.findClusters(scene,null);
         highlightSelected(clusters);
         scene.updateScene();
     }//GEN-LAST:event_jMenuItemHClustersActionPerformed
 
     private void jMenuItemHCyclesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHCyclesActionPerformed
         ArrayList<ArrayList<GraphNode>> clusters=GraphUtils.findCycles(scene,null);
         highlightSelected(clusters);
         scene.updateScene();
     }//GEN-LAST:event_jMenuItemHCyclesActionPerformed
 
     private void jMenuItemHSelClusterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHSelClusterActionPerformed
         ArrayList<ArrayList<GraphNode>> clusters=GraphUtils.findClusters(scene,scene.getSelectedNodes());
         highlightSelected(clusters);
         scene.updateScene();
     }//GEN-LAST:event_jMenuItemHSelClusterActionPerformed
 
     private void jMenuItemHSelCycleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHSelCycleActionPerformed
         ArrayList<ArrayList<GraphNode>> clusters=GraphUtils.findCycles(scene,scene.getSelectedNodes());
         highlightSelected(clusters);
         scene.updateScene();
     }//GEN-LAST:event_jMenuItemHSelCycleActionPerformed
 
     private void jMenuItemHClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHClearActionPerformed
         scene.clearHighlight();
     }//GEN-LAST:event_jMenuItemHClearActionPerformed
 
     private void jMenuItemSaveToImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveToImageActionPerformed
         BufferedImage img=scene.drawToImage(0, 0);
         
         final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
         
         fc.setFileFilter(new FileNameExtensionFilter("JPEG file", "jpg", "jpeg"));
         fc.addChoosableFileFilter(new FileNameExtensionFilter("PNG file", "png"));
         fc.addChoosableFileFilter(new FileNameExtensionFilter("BMP file", "bmp"));
         fc.addChoosableFileFilter(new FileNameExtensionFilter("GIF file", "gif"));
         fc.setSelectedFile(new File("unnamed"));
             
         int returnVal = fc.showSaveDialog(this);
         
         if (returnVal == JFileChooser.APPROVE_OPTION) 
         {
             String str=fc.getFileFilter().getDescription();
             String path=fc.getSelectedFile().getPath();
             String ff="";
             if(str.equals("JPEG file")) ff="jpg";
             else if(str.equals("PNG file")) ff="png";
             else if(str.equals("BMP file")) ff="bmp";
             else if(str.equals("GIF file")) ff="gif";
             
             if(!path.endsWith(ff)) path+="."+ff;
             File file=new File(path);
             
             if(file.exists())
             {
                 int t=JOptionPane.showConfirmDialog(this, String.format("Overwrite %s?", file.getName()),"",JOptionPane.YES_NO_OPTION);
                 if(t==JOptionPane.CANCEL_OPTION) return;
             };
             
             try {
                 ImageIO.write(img, ff, file);
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(this, "Save FAILED!");
                 Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
             }
         };
     }//GEN-LAST:event_jMenuItemSaveToImageActionPerformed
 
     private void jMenuItemCopyToClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyToClipboardActionPerformed
         BufferedImage img=scene.drawToImage(0, 0);
         TransferableImage trans = new TransferableImage( img );
         
         Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
         c.setContents( trans, null );
     }//GEN-LAST:event_jMenuItemCopyToClipboardActionPerformed
 
     private void jMenuItemTestLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTestLayoutActionPerformed
         HierarchicalLayoutK layout=new HierarchicalLayoutK();
         scene.applyLayout(layout);
     }//GEN-LAST:event_jMenuItemTestLayoutActionPerformed
 
     public static void setSkin(String str)
     {
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if (str.equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
     };
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
 
         setSkin("Windows");
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try { 
                     new MainFrame().setVisible(true);
                 } catch (IOException ex) {
                     Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
         
         
         
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenu jMenu3;
     private javax.swing.JMenu jMenu4;
     private javax.swing.JMenu jMenu5;
     private javax.swing.JMenu jMenu6;
     private javax.swing.JMenu jMenu7;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenu jMenuHighlight;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem10;
     private javax.swing.JMenuItem jMenuItem11;
     private javax.swing.JMenuItem jMenuItem2;
     private javax.swing.JMenuItem jMenuItem3;
     private javax.swing.JMenuItem jMenuItem4;
     private javax.swing.JMenuItem jMenuItem5;
     private javax.swing.JMenuItem jMenuItem6;
     private javax.swing.JMenuItem jMenuItem7;
     private javax.swing.JMenuItem jMenuItem8;
     private javax.swing.JMenuItem jMenuItem9;
     private javax.swing.JMenuItem jMenuItemCopyToClipboard;
     private javax.swing.JMenuItem jMenuItemFileOpen;
     private javax.swing.JMenuItem jMenuItemHClear;
     private javax.swing.JMenuItem jMenuItemHClusters;
     private javax.swing.JMenuItem jMenuItemHCycles;
     private javax.swing.JMenuItem jMenuItemHSelCluster;
     private javax.swing.JMenuItem jMenuItemHSelCycle;
     private javax.swing.JMenuItem jMenuItemMetalSkin;
     private javax.swing.JMenuItem jMenuItemMotifSkin;
     private javax.swing.JMenuItem jMenuItemNimbusSkin;
     private javax.swing.JMenuItem jMenuItemSaveToImage;
     private javax.swing.JMenuItem jMenuItemTestLayout;
     private javax.swing.JMenuItem jMenuItemWindowsClas;
     private javax.swing.JMenuItem jMenuItemWindowsSkin;
     private javax.swing.JPopupMenu.Separator jSeparator1;
     private javax.swing.JPopupMenu.Separator jSeparator2;
     private javax.swing.JPopupMenu.Separator jSeparator3;
     private javax.swing.JPopupMenu.Separator jSeparator4;
     // End of variables declaration//GEN-END:variables
 }
