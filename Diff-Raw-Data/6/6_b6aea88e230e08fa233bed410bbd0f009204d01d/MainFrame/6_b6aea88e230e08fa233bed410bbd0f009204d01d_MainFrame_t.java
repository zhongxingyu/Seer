 /*
  * Copyright 2000-2005 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import gov.nih.nci.ncicb.cadsr.loader.*;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewEvent;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewListener;
 import gov.nih.nci.ncicb.cadsr.loader.parser.ElementWriter;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.*;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.*;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.*;
 import gov.nih.nci.ncicb.cadsr.loader.validator.*;
 
 
 import java.awt.Component;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 
 import java.io.File;
 import javax.swing.*;
 
 import java.util.*;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 
 /**
  * The main Frame containing other frames
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class MainFrame extends JFrame 
   implements ViewChangeListener, CloseableTabbedPaneListener,
              PropertyChangeListener
 {
 
   private JMenuBar mainMenuBar = new JMenuBar();
   private JMenu fileMenu = new JMenu("File");
   private JMenuItem saveMenuItem = new JMenuItem("Save");
   private JMenuItem saveAsMenuItem = new JMenuItem("Save As");
   private JMenuItem exportErrorsMenuItem = new JMenuItem("Export");
   private JMenuItem exitMenuItem = new JMenuItem("Exit");
 
   private JMenu editMenu = new JMenu("Edit");
   private JMenuItem findMenuItem = new JMenuItem("Find");
   private JMenuItem prefMenuItem = new JMenuItem("Preferences");
 
   private JMenu elementMenu = new JMenu("Element");
   private JMenuItem applyMenuItem = new JMenuItem("Apply");
   private JMenuItem applyToAllMenuItem = new JMenuItem("Apply to All");
 
 
   private JMenu runMenu = new JMenu("Run");
   private JMenuItem validateMenuItem = new JMenuItem("Validate");
   private JMenuItem uploadMenuItem = new JMenuItem("Upload");
   private JMenuItem defaultsMenuItem = new JMenuItem("Defaults");
 
   private JMenu helpMenu = new JMenu("Help");
   private JMenuItem aboutMenuItem = new JMenuItem("About");
   private JMenuItem indexMenuItem = new JMenuItem("Index");
 
   private JMenuItem semanticConnectorMenuItem = new JMenuItem("Semantic Connector");
 
   private JSplitPane jSplitPane1 = new JSplitPane();
   private JSplitPane jSplitPane2 = new JSplitPane();
   private JTabbedPane jTabbedPane1 = new JTabbedPane();
   private CloseableTabbedPane viewTabbedPane = new CloseableTabbedPane();
   private JPanel jPanel1 = new JPanel();
 
   private NavigationPanel navigationPanel = new NavigationPanel();
   private ErrorPanel errorPanel = null;
 
   private MainFrame _this = this;
 
   private JLabel infoLabel = new JLabel(" ");
 
   private Map<String, UMLElementViewPanel> viewPanels = new HashMap();
   private AssociationViewPanel associationViewPanel = null;
 
   private ReviewTracker reviewTracker = ReviewTracker.getInstance();
 
   private RunMode runMode = null;
 
   private String saveFilename = "";
 
   public MainFrame()
   {
     try
     {
       UserSelections selections = UserSelections.getInstance();
 
       runMode = (RunMode)(selections.getProperty("MODE"));
       saveFilename = (String)selections.getProperty("FILENAME");
 
       jbInit();
 
     }
     catch(Exception e)
     {
       e.printStackTrace();
     }
 
   }
 
   public void exit() {
     System.exit(0);
   }
 
   public void propertyChange(PropertyChangeEvent evt) {
     if(evt.getPropertyName().equals("APPLY")) {
       applyMenuItem.setEnabled((Boolean)evt.getNewValue());
       applyToAllMenuItem.setEnabled((Boolean)evt.getNewValue());
       
       if((Boolean)evt.getNewValue() == true)
         infoLabel.setText("Unsaved Changes");
       else
         infoLabel.setText("Changes Applied");
     }
   }
 
   private void jbInit() throws Exception
   {
     this.getContentPane().setLayout(new BorderLayout());
     this.setSize(new Dimension(830, 650));
     this.setJMenuBar(mainMenuBar);
     
     UserSelections selections = UserSelections.getInstance();
     String fileName = new File((String)selections.getProperty("FILENAME")).getName();
     this.setTitle("Semantic Integration Workbench - " + fileName);
 
     jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
     jSplitPane1.setDividerLocation(160);
     jSplitPane2.setDividerLocation(400);
 
     fileMenu.add(saveMenuItem);
     fileMenu.add(saveAsMenuItem);
     fileMenu.addSeparator();
     fileMenu.add(findMenuItem);
     fileMenu.add(exportErrorsMenuItem);
     fileMenu.addSeparator();
     fileMenu.add(exitMenuItem);
     mainMenuBar.add(fileMenu);
 
     editMenu.add(findMenuItem);
     editMenu.add(prefMenuItem);
     mainMenuBar.add(editMenu);
     
 
     applyMenuItem.setEnabled(false);
     applyToAllMenuItem.setEnabled(false);
     elementMenu.add(applyMenuItem);
     elementMenu.add(applyToAllMenuItem);
     mainMenuBar.add(elementMenu);
 
 //     runMenu.add(validateMenuItem);
     if(runMode.equals(RunMode.Reviewer)) {
       runMenu.add(uploadMenuItem); 
       uploadMenuItem.setEnabled(false);
       runMenu.addSeparator();
       runMenu.add(defaultsMenuItem);
       mainMenuBar.add(runMenu);
     }
 
     helpMenu.add(indexMenuItem);
     helpMenu.addSeparator();
     helpMenu.add(aboutMenuItem);
     mainMenuBar.add(helpMenu);
 
     errorPanel = new ErrorPanel(TreeBuilder.getInstance().getRootNode());
 
     jTabbedPane1.addTab("Errors", errorPanel);
 
     Icon closeIcon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("close-tab.gif"));
     viewTabbedPane.setCloseIcons(closeIcon, closeIcon, closeIcon);
     viewTabbedPane.addCloseableTabbedPaneListener(this);
 
     jTabbedPane1.addTab("Log", new JPanel());
     jSplitPane2.add(jTabbedPane1, JSplitPane.BOTTOM);
     jSplitPane2.add(viewTabbedPane, JSplitPane.TOP);
     jSplitPane1.add(jSplitPane2, JSplitPane.RIGHT);
     
     jSplitPane1.add(navigationPanel, JSplitPane.LEFT);
     
     navigationPanel.addViewChangeListener(this);
 
     this.getContentPane().add(jSplitPane1, BorderLayout.CENTER);
     this.getContentPane().add(infoLabel, BorderLayout.SOUTH);
     
     exitMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           _this.exit();
         }
     });  
 
     defaultsMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           UmlDefaultsPanel dp =  new UmlDefaultsPanel(_this);
           dp.show();
           UIUtil.putToCenter(dp);
         }
     });
     
     findMenuItem.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         SearchDialog sd = new SearchDialog(_this);
         UIUtil.putToCenter(sd);
         sd.addSearchListener(navigationPanel);
         sd.setVisible(true);
 
       }
     });
     
     findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,    
       Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     
     final PreferenceDialog pd = new PreferenceDialog(_this);
     prefMenuItem.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         UIUtil.putToCenter(pd);
         pd.setVisible(true);
       }
     });
     
     saveMenuItem.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
 //         if(runMode.equals(RunMode.Reviewer)) {
 //           JOptionPane.showMessageDialog(_this, "Sorry, Not Implemented Yet", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
 //           return;
 //         } 
 
         ElementWriter writer = BeansAccessor.getWriter();
         writer.setOutput(saveFilename);
         writer.write(ElementsLists.getInstance());
         
         infoLabel.setText("File Saved");
       }
     });
     
     saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,    
       Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     
     saveAsMenuItem.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         String saveDir = UserPreferences.getInstance().getRecentDir();
         JFileChooser chooser = new JFileChooser(saveDir);
         javax.swing.filechooser.FileFilter filter = 
             new javax.swing.filechooser.FileFilter() {
               String fileExtension = null;
               {
                 if(runMode.equals(RunMode.Curator)) 
                   fileExtension = "csv";
                 else if(runMode.equals(RunMode.Reviewer)) 
                   fileExtension = "xmi";
               }
 
               public boolean accept(File f) {
                 if (f.isDirectory()) {
                   return true;
                 }                
                 return f.getName().endsWith("." + fileExtension);
               }
               public String getDescription() {
                 return fileExtension.toUpperCase() + " Files";
               }
             };
             
         chooser.setFileFilter(filter);
         int returnVal = chooser.showSaveDialog(null);
           if(returnVal == JFileChooser.APPROVE_OPTION) {
             String filePath = chooser.getSelectedFile().getAbsolutePath();
             UserPreferences.getInstance().setRecentDir(filePath);
             ElementWriter writer = BeansAccessor.getWriter();
             writer.setOutput(filePath);
             saveFilename = filePath;
             writer.write(ElementsLists.getInstance());
             infoLabel.setText("File Saved");
           }
       }
     });
 
 
     exportErrorsMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           JOptionPane.showMessageDialog(_this, "Sorry, Not Implemented Yet", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
         } 
       });
 
     validateMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           ValidationItems.getInstance().clear();
           Validator validator = new UMLValidator();
           validator.validate();
 
           errorPanel.update(TreeBuilder.getInstance().getRootNode());
 
           JOptionPane.showMessageDialog(_this, "Sorry, Not Implemented Yet", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
         } 
       });
 
     uploadMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           JOptionPane.showMessageDialog(_this, "Sorry, Not Implemented Yet", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
         } 
       });
 
     applyMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
           UMLElementViewPanel viewPanel =
             (UMLElementViewPanel)viewTabbedPane
             .getSelectedComponent();
           
           viewPanel.apply(false);
           
         }
       });
     applyToAllMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
           UMLElementViewPanel viewPanel =
             (UMLElementViewPanel)viewTabbedPane
             .getSelectedComponent();
           
           viewPanel.apply(true);
           
         }
       });
 
     aboutMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
           new AboutPanel();
         }
       });
   }
 
   public void viewChanged(ViewChangeEvent event) {
     if(event.getType() == ViewChangeEvent.VIEW_CONCEPTS) {
       UMLNode node = (UMLNode)event.getViewObject();
 
       // If concept is already showing, just bring it up front
       if(viewPanels.containsKey(node.getFullPath())) {
         UMLElementViewPanel pa = viewPanels.get(node.getFullPath());
         viewTabbedPane.setSelectedComponent(pa);
         return;
       }
 
 
      if((event.getInNewTab() == true) || (viewPanels.size() == 0)
          || viewTabbedPane.getSelectedComponent() instanceof AssociationViewPanel) {
         UMLElementViewPanel viewPanel = new UMLElementViewPanel(node);
         
         viewPanel.addPropertyChangeListener(this);
         viewPanel.addReviewListener(navigationPanel);
         viewPanel.addReviewListener(reviewTracker);
         viewPanel.addElementChangeListener(ChangeTracker.getInstance());
         viewPanel.addNavigationListener(navigationPanel);
         navigationPanel.addNavigationListener(viewPanel);
 
         String tabTitle = node.getDisplay();;
         if(node instanceof AttributeNode) 
           tabTitle = node.getParent().getDisplay() 
             + "." + tabTitle;
 
         viewTabbedPane.addTab(tabTitle, viewPanel);
         viewTabbedPane.setSelectedComponent(viewPanel);
 
         viewPanel.setName(node.getFullPath());
         viewPanels.put(viewPanel.getName(), viewPanel);
         infoLabel.setText(tabTitle);
       } else {
         UMLElementViewPanel viewPanel = (UMLElementViewPanel)
           viewTabbedPane.getSelectedComponent();
         viewPanels.remove(viewPanel.getName());
             
         String tabTitle = node.getDisplay();;
         if(node instanceof AttributeNode) 
           tabTitle = node.getParent().getDisplay() 
             + "." + tabTitle;
         viewTabbedPane.setTitleAt(viewTabbedPane.getSelectedIndex(), tabTitle);
 
         infoLabel.setText(tabTitle);
 
         viewPanel.setName(node.getFullPath());
         viewPanel.updateNode(node);
         viewPanels.put(viewPanel.getName(), viewPanel);
       }
 
     } else if(event.getType() == ViewChangeEvent.VIEW_ASSOCIATION) {
       UMLNode node = (UMLNode)event.getViewObject();
 
       if(associationViewPanel == null) {
         associationViewPanel = new AssociationViewPanel((ObjectClassRelationship)node.getUserObject());
         viewTabbedPane.addTab("Association", associationViewPanel);
         associationViewPanel.setName("Association");
         infoLabel.setText("Association");
       } else
         associationViewPanel.update((ObjectClassRelationship)node.getUserObject());
 
       viewTabbedPane.setSelectedComponent(associationViewPanel);
 
     }
    
   }
 
   public boolean closeTab(int index) {
 
     Component c = viewTabbedPane.getComponentAt(index);
     if(c.equals(associationViewPanel))
       associationViewPanel = null;
     viewPanels.remove(c.getName());
 
     return true;
   }
   
 }
