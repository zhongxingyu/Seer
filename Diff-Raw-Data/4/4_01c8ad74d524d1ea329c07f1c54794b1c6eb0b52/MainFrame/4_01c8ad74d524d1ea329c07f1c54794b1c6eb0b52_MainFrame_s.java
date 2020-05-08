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
 import gov.nih.nci.ncicb.cadsr.loader.parser.ParserException;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.*;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.*;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.*;
 import gov.nih.nci.ncicb.cadsr.loader.validator.*;
 
 
 import java.awt.Component;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.Image;
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
 
 import org.apache.log4j.Logger;
 import java.lang.reflect.Method;
 import javax.swing.JOptionPane;
 
 
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
   //private JMenuItem exportErrorsMenuItem = new JMenuItem("Export");
   private JMenuItem exitMenuItem = new JMenuItem("Exit");
 
   private JMenu editMenu = new JMenu("Edit");
   private JMenuItem findMenuItem = new JMenuItem("Find");
   private JMenuItem prefMenuItem = new JMenuItem("Preferences");
 
   private JMenu elementMenu = new JMenu("Element");
   private JMenuItem applyMenuItem = new JMenuItem("Apply");
   private JMenuItem applyToAllMenuItem = new JMenuItem("Apply to All");
 
 
   private JMenu runMenu = new JMenu("Run");
   private JMenuItem validateMenuItem = new JMenuItem("Validate");
   //private JMenuItem uploadMenuItem = new JMenuItem("Upload");
   private JMenuItem defaultsMenuItem = new JMenuItem("Defaults");
   private JMenuItem validateConceptsMenuItem = new JMenuItem("Validate Concepts");
 
   private JMenu helpMenu = new JMenu("Help");
   private JMenuItem aboutMenuItem = new JMenuItem("About");
   private JMenuItem indexMenuItem = new JMenuItem("SIW on GForge");
 
   private JMenuItem semanticConnectorMenuItem = new JMenuItem("Semantic Connector");
 
   private JSplitPane jSplitPane1 = new JSplitPane();
   private JSplitPane jSplitPane2 = new JSplitPane();
   private JTabbedPane jTabbedPane1 = new JTabbedPane();
   private CloseableTabbedPane viewTabbedPane = new CloseableTabbedPane();
   private JPanel jPanel1 = new JPanel();
 
   private NavigationPanel navigationPanel;
   private ErrorPanel errorPanel = null;
   private JPanel logPanel;
 
   private MainFrame _this = this;
 
   private JLabel infoLabel = new JLabel(" ");
 
   private Map<String, UMLElementViewPanel> viewPanels = new HashMap();
   private AssociationViewPanel associationViewPanel = null;
   private ValueDomainViewPanel vdViewPanel = null;
 
   private ReviewTracker reviewTracker = ReviewTracker.getInstance();
 
   private RunMode runMode = null;
 
   private String saveFilename = "";
 
   private ElementWriter xmiWriter = null;
 
   private static Logger logger = Logger.getLogger(MainFrame.class);
 
   public MainFrame()
   {
   }
 
   public void init() {
     UserSelections selections = UserSelections.getInstance();
     
     runMode = (RunMode)(selections.getProperty("MODE"));
     saveFilename = (String)selections.getProperty("FILENAME");
 
     jbInit();
   }
 
   public void exit() {
     if(!ChangeTracker.getInstance().isEmpty()) {
     int result = JOptionPane.showConfirmDialog((JFrame) null, "Would you like to save your file before quitting?");
     switch(result) { 
     case JOptionPane.YES_OPTION: 
       saveMenuItem.doClick();
       break;
     case JOptionPane.NO_OPTION:
       break;
     
     case JOptionPane.CANCEL_OPTION:
       return;    
     }
     System.exit(0);
     }
     else 
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
     } else if(evt.getPropertyName().equals("EXPORT_ERRORS")) {
       infoLabel.setText("Export Errors Complete");
     } else if(evt.getPropertyName().equals("EXPORT_ERRORS_FAILED")) {
       infoLabel.setText("Export Errors Failed !");
     }
     
   }
 
   private void jbInit() {
     this.getContentPane().setLayout(new BorderLayout());
     this.setSize(new Dimension(830, 650));
     this.setJMenuBar(mainMenuBar);
     
     this.setIconImage(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("siw-logo3_2.gif")).getImage());
 
     UserSelections selections = UserSelections.getInstance();
     String fileName = new File((String)selections.getProperty("FILENAME")).getName();
     this.setTitle("Semantic Integration Workbench - " + fileName);
 
     jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
     jSplitPane1.setDividerLocation(160);
     jSplitPane2.setDividerLocation(400);
 
     jSplitPane1.setOneTouchExpandable(true);
     jSplitPane2.setOneTouchExpandable(true);
 
     fileMenu.add(saveMenuItem);
     fileMenu.add(saveAsMenuItem);
     fileMenu.addSeparator();
     fileMenu.add(findMenuItem);
     //fileMenu.add(exportErrorsMenuItem);
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
       //runMenu.add(uploadMenuItem); 
       //uploadMenuItem.setEnabled(false);
       //runMenu.addSeparator();
       runMenu.add(defaultsMenuItem);
       runMenu.add(validateConceptsMenuItem);
       mainMenuBar.add(runMenu);
     }
 
     helpMenu.add(indexMenuItem);
     helpMenu.addSeparator();
     helpMenu.add(aboutMenuItem);
     mainMenuBar.add(helpMenu);
 
     errorPanel = new ErrorPanel(TreeBuilder.getInstance().getRootNode());
 
     errorPanel.addPropertyChangeListener(this);
 
     jTabbedPane1.addTab("Errors", errorPanel);
 
     Icon closeIcon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("close-tab.gif"));
     viewTabbedPane.setCloseIcons(closeIcon, closeIcon, closeIcon);
     viewTabbedPane.addCloseableTabbedPaneListener(this);
 
     jTabbedPane1.addTab("Log", logPanel);
     jSplitPane2.add(jTabbedPane1, JSplitPane.BOTTOM);
     jSplitPane2.add(viewTabbedPane, JSplitPane.TOP);
     jSplitPane1.add(jSplitPane2, JSplitPane.RIGHT);
     
     navigationPanel = new NavigationPanel();
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
         pd.updatePreferences();
         UIUtil.putToCenter(pd);
         pd.setVisible(true);
       }
     });
     
     saveMenuItem.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         xmiWriter.setOutput(saveFilename);
 
         try {
           xmiWriter.write(ElementsLists.getInstance());
           
           infoLabel.setText("File Saved");
         } catch (ParserException e){
           JOptionPane.showMessageDialog(_this, "There was an error saving your File. Please contact support.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
           infoLabel.setText("Save Failed!!");
           logger.error(e);
           e.printStackTrace();
         } // end of try-catch
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
 	    String fileExtension = "xmi";
 
 	    if(runMode.equals(RunMode.Curator)) 
 	      fileExtension = "csv";
             else if(runMode.equals(RunMode.Reviewer)) 
 	      fileExtension = "xmi";
 
 	    if(!filePath.endsWith(fileExtension))
 	      filePath = filePath + "." + fileExtension;
 
             UserPreferences.getInstance().setRecentDir(filePath);
             xmiWriter.setOutput(filePath);
             saveFilename = filePath;
             try {
               xmiWriter.write(ElementsLists.getInstance());
               infoLabel.setText("File Saved");
             } catch (ParserException e){
               JOptionPane.showMessageDialog(_this, "There was an error saving your File. Please contact support.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
               infoLabel.setText("Save Failed!!");
             } // end of try-catch
           }
       }
     });
 
 /*
     exportErrorsMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           JOptionPane.showMessageDialog(_this, "Sorry, Not Implemented Yet", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
         } 
       });
 */
     validateMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           ValidationItems.getInstance().clear();
           Validator validator = new UMLValidator();
           validator.validate();
 
           ElementsLists elements = ElementsLists.getInstance();
 
           TreeBuilder tb = TreeBuilder.getInstance();
           tb.init();
           tb.buildTree(elements);
 
           errorPanel.update(tb.getRootNode());
 
 //           JOptionPane.showMessageDialog(_this, "Sorry, Not Implemented Yet", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
         } 
       });
       
     validateConceptsMenuItem.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         int n = JOptionPane.showConfirmDialog(_this, 
           "This process may take some time. Would you like to continue? ",
           "Validate Concepts", JOptionPane.YES_NO_OPTION);
         if(n == JOptionPane.YES_OPTION) {
           ValidateConceptsDialog vcd = new ValidateConceptsDialog(_this);
           vcd.addSearchListener(navigationPanel);
           vcd.setVisible(true);
         }
       }
     });
 /*
     uploadMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           JOptionPane.showMessageDialog(_this, "Sorry, Not Implemented Yet", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
         } 
       });
 */
     applyMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
           UMLElementViewPanel viewPanel =
             (UMLElementViewPanel)viewTabbedPane
             .getSelectedComponent();
           
           try {
             viewPanel.apply(false);
           } catch (ApplyException e){
             infoLabel.setText("Changes were not applied!");
           } // end of try-catch
           
         }
       });
     applyToAllMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
           UMLElementViewPanel viewPanel =
             (UMLElementViewPanel)viewTabbedPane
             .getSelectedComponent();
           
           try {
             viewPanel.apply(true);
           } catch (ApplyException e){
             infoLabel.setText("Changes were not applied!");
           } // end of try-catch
           
         }
       });
 
     aboutMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
           new AboutPanel();
         }
       });
       
       indexMenuItem.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent evt) {
             String errMsg = "Error attempting to launch web browser";
             String osName = System.getProperty("os.name");
             String url = "http://gforge.nci.nih.gov/projects/siw/";
             try {
               if (osName.startsWith("Mac OS")) {
                 Class fileMgr = Class.forName("com.apple.eio.FileManager");
                 Method openURL = fileMgr.getDeclaredMethod("openURL",
                                                            new Class[] {String.class});
                 openURL.invoke(null, new Object[] {url});
               }
               else if (osName.startsWith("Windows"))
                 Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
               else { //assume Unix or Linux
                 String[] browsers = {
                   "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                 String browser = null;
                 for (int count = 0; count < browsers.length && browser == null; count++)
                   if (Runtime.getRuntime().exec(
                                                 new String[] {"which", browsers[count]}).waitFor() == 0)
                     browser = browsers[count];
                 if (browser == null)
                   throw new Exception("Could not find web browser");
                 else
                   Runtime.getRuntime().exec(new String[] {browser, url});
               }
             } catch (Exception e) {
               JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
             }
           }
         });      
   }
 
   public void viewChanged(ViewChangeEvent event) {
     if(event.getType() == ViewChangeEvent.VIEW_CONCEPTS
         || event.getType() == ViewChangeEvent.VIEW_VALUE_MEANING) {
       UMLNode node = (UMLNode)event.getViewObject();
 
       // If concept is already showing, just bring it up front
       if(viewPanels.containsKey(node.getFullPath())) {
         UMLElementViewPanel pa = viewPanels.get(node.getFullPath());
         viewTabbedPane.setSelectedComponent(pa);
         return;
       }
 
 
       if((event.getInNewTab() == true) || (viewPanels.size() == 0)
           || viewTabbedPane.getSelectedComponent() instanceof AssociationViewPanel
           || viewTabbedPane.getSelectedComponent() instanceof ValueDomainViewPanel) {
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
       else if(event.getType() == ViewChangeEvent.VIEW_VALUE_DOMAIN) {
       UMLNode node = (UMLNode)event.getViewObject();
       
       if(vdViewPanel == null) {
         vdViewPanel = new ValueDomainViewPanel((ValueDomain)node.getUserObject());
         viewTabbedPane.addTab("ValueDomain", vdViewPanel);
         vdViewPanel.setName("ValueDomain");
         infoLabel.setText("ValueDomain");
       }
       else
         vdViewPanel.update((ValueDomain)node.getUserObject());
         
       viewTabbedPane.setSelectedComponent(vdViewPanel);
   }
   }
 
   public boolean closeTab(int index) {
 
     Component c = viewTabbedPane.getComponentAt(index);
     if(c.equals(associationViewPanel))
       associationViewPanel = null;
     if(c.equals(vdViewPanel))
       vdViewPanel = null;
     viewPanels.remove(c.getName());
 
     return true;
   }
 
   public void setXmiWriter(ElementWriter writer) {
     this.xmiWriter = writer;
   }
  
   public void setLogTab(JPanel panel) {
     this.logPanel = panel;
   }
   
 }
