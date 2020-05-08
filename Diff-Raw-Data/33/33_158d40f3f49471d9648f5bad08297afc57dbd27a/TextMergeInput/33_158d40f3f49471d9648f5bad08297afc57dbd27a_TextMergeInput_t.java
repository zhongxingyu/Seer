 /*
  * Copyright 1999 - 2013 Herb Bowie
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.powersurgepub.psdatalib.textmerge;
 
   import com.powersurgepub.psdatalib.psdata.*;
   import com.powersurgepub.psdatalib.pslist.*;
   import com.powersurgepub.psdatalib.script.*;
   import com.powersurgepub.psdatalib.tabdelim.*;
   import com.powersurgepub.psdatalib.textmerge.input.*;
   import com.powersurgepub.psdatalib.ui.*;
   import com.powersurgepub.psfiles.*;
   import com.powersurgepub.psutils.*;
   import java.awt.*;
   import java.awt.event.*;
   import java.io.*;
   import java.net.*;
   import java.util.*;
   import java.util.zip.*;
   import javax.swing.*;
   import javax.swing.border.*;
 
 /**
  The Text Merge module for reading input files. 
 
  @author Herb Bowie
  */
 public class TextMergeInput {
   
   private   static  final String  MIME_TYPE = "mimetype";
   
   // Maximum value for normalization Type
   private		static	final int			NORMALTYPE_MAX = 1;
   
   private     PSList              psList = null;
   private     DataRecList         dataRecList = null;
   
   private     TextMergeController textMergeController = null;
   private     TextMergeScript     textMergeScript = null;
   
   private     File                appFolder;
   private     URL                 pageURL;
   
   private     JTabbedPane         tabs = null;
   private     JMenuBar            menus = null;
   
   private     boolean             quietMode = true;
   private     boolean             tabSet = false;
   private     boolean             menuSet = false;
   
   // Input Modules
   private     ArrayList<TextMergeInputModule> inputModules = new ArrayList();
   
   private     TextMergeInputModule  inputModule;
   
   private     TextMergeInputTDF     inTDF = new TextMergeInputTDF();
   private     TextMergeInputDirEntry inDir = new TextMergeInputDirEntry();
   private     TextMergeInputHTML    inHTML = new TextMergeInputHTML();
   private     TextMergeInputXML     inXML = new TextMergeInputXML();
   private     TextMergeInputExcel   inExcel = new TextMergeInputExcel();
   private     TextMergeInputTunes   inTunes = new TextMergeInputTunes();
   private     TextMergeInputClub    inClub = new TextMergeInputClub();
   // private     TextMergeInputOutline inOutline = new TextMergeInputOutline();
   private     TextMergeInputGrid    inGrid = new TextMergeInputGrid();
   // private     TextMergeInputReturnedMail inMail = new TextMergeInputReturnedMail();
   private     TextMergeInputYojimbo inYojimbo = new TextMergeInputYojimbo();
   // private     TextMergeInputYAML    inYAML = new TextMergeInputYAML();
   private     TextMergeInputMetaMarkdown inMarkdown = new TextMergeInputMetaMarkdown();
   
   private     int                   inputModuleIndex = 0;
   
   private     boolean               inputModuleFound = false;
   
   // Menu Objects
   private			JMenuItem						fileOpen;
   private     JMenuItem           fileEpub;
   
   // Fields used for the User Interface
   private     Border              raisedBevel;
   private     Border              etched;
   private     GridBagger          gb = new GridBagger();
   
   // Input Panel objects
   private     JPanel              inputPanel;
   
   // Open Input Button
   private     JButton             openDataButton;
   
   // Input Type Drop Down List
   private    JLabel               inputTypeLabel;
   
   private     JComboBox           inputTypeBox  = new JComboBox ();
   
   // Data Dictionary Check Box
   private    JLabel               inputDictionaryLabel;
   private    JCheckBox            inputDictionaryCkBox;
   
   // Merge Radio Buttons
   private    JLabel               inputMergeLabel;
   private		 ButtonGroup					inputMergeGroup;
   private 	 JRadioButton					inputMergeNoButton;
   private    JRadioButton         inputMergeButton;
   private		 JRadioButton				  inputMergeSameColumnsButton;
   
   // Directory Depth
   private    JLabel               inputDirMaxDepthLabel;
   private    JTextField           inputDirMaxDepthValue;
   private    JButton              inputDirMaxDepthUpButton;
   private    JButton              inputDirMaxDepthDownButton;
   
   // Normalization Type Drop Down List
   private    JLabel               inputNormalLabel;
   public    static final String  	INPUT_NORMAL0 = 		"None";
   public    static final String  	INPUT_NORMAL1 = 		"Boeing Docs";
   
   private     String[]            inputNormalTypes    = {
                                     INPUT_NORMAL0,
                                     INPUT_NORMAL1};
   private     JComboBox           inputNormalBox  = new JComboBox (inputNormalTypes);
     
   // Text Area (filler)
   private     JTextArea           inputText;
   
   private     String              possibleFileName = "";
   
   // File chosen as input Tab-Delimited File.
   private     File                chosenFile = null;
   
   private     DataSource          dataSource = null;
   
   private     String              fileName = "";
   
   private     String              fileNameToDisplay = "";
   
   private     String              tabName = "";
   
   private     FileName            tabFileName;
   
   private     URL                 tabURL;
   
   private			int									dirMaxDepth = 1;
   
   // Normalization Fields
   private			boolean							normalization = false;
   private static final String     NORMALIZATION_KEY = "normalization";
   private			int									normalType = 0;
   private     String							normalTypeValue = "No Normalization";
   private     DataSource          normalizer;
   
   // Data Dictionary Fields
 	private			TabDelimFile				dictFile;
 	private     DataDictionary      dataDict;
   private			boolean							usingDictionary = false;
   private     String              usingDictionaryValue = "No";
   public static final String      DICTIONARY_EXT = "dic";
   
   // Merge Fields
   private			int							 		merge = 0;
   private			String							mergeValue = "No";
   
   private     Logger              log = Logger.getShared();
   
   // Debugging Instance
   private			Debug								debug = new Debug (false);
   
   private			String							inputObject = "";
   
   // Epub files
   private     File                epubFolder;
   private     File                epubFile;
     
   public TextMergeInput (
       PSList psList, 
       TextMergeController textMergeController, 
       TextMergeScript textMergeScript) {
     
     this.psList = psList;
     
     setListOptions();
     
     this.textMergeController = textMergeController;
     this.textMergeScript = textMergeScript;
     
     appFolder = Home.getShared().getAppFolder();
     try {
       pageURL = appFolder.toURI().toURL(); 
     } catch (MalformedURLException e) {
       Trouble.getShared().report ("Trouble forming pageURL from " + appFolder.toString(), 
           "URL Problem");
     }
     
     // Initialization related to input modules
     int insertAt = 0;
     insertAt = addInputModule(inTDF, insertAt);
     insertAt = addInputModule(inClub, insertAt);
     insertAt = addInputModule(inExcel, insertAt);
     insertAt = addInputModule(inDir, insertAt);
     insertAt = addInputModule(inGrid, insertAt);
     insertAt = addInputModule(inHTML, insertAt);
     insertAt = addInputModule(inTunes, insertAt);
     insertAt = addInputModule(inMarkdown, insertAt);
     // insertAt = addInputModule(inOutline, insertAt);
     // insertAt = addInputModule(inMail, insertAt);
     insertAt = addInputModule(inXML, insertAt);
     // insertAt = addInputModule(inYAML, insertAt);
     insertAt = addInputModule(inYojimbo, insertAt);
     inputTypeBox.setSelectedIndex (0);
     
     // Normalization init
     String normalProperty = UserPrefs.getShared().getPref (NORMALIZATION_KEY);
     normalization = Boolean.valueOf(normalProperty).booleanValue();
     if (! normalization) {
       File boeing = new File (Home.getShared().getAppFolder(), "boeing.txt");
       if (boeing.exists()) {
         normalization = true;
       } else {

       }
     }
     
     // Open file if it was passed as a parameter
     possibleFileName = System.getProperty ("tabfile", "");
     if ((possibleFileName != null) && (! possibleFileName.equals (""))) {
       fileName = possibleFileName;
       try {
         tabURL = new URL (pageURL, fileName);
         openURL();
       } catch (MalformedURLException e) {
         // Shouldn't happen
       }
     } else {
       openEmpty();
     }
   }
 
   public void setPSList (PSList psList) {
     this.psList = psList;
     setListOptions();
   }
   
   private void setListOptions() {
     if (psList instanceof DataRecList) {
       dataRecList = (DataRecList)psList;
     } else {
       dataRecList = null;
       throw new IllegalArgumentException("List must be a DataRecList");
     }
   }
   
   public void setMenus(JMenuBar menus) {
     quietMode = false;
     menuSet = true;
     
     this.menus = menus;
     
     JMenu fileMenu = null;
     boolean fileMenuFound = false;
     int i = 0;
     while (i < menus.getMenuCount() && (! fileMenuFound)) {
       Component menuElement = menus.getComponent(i);
       if (menuElement instanceof JMenu) {
         fileMenu = (JMenu)menuElement;
         if (fileMenu.getText().equals("File")) {
           fileMenuFound = true;
         }
       }
     }
     
     // Equivalent Menu Item for File Open
     if (fileMenuFound) {
       fileOpen = new JMenuItem ("Open...");
       fileOpen.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_O,
           Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       fileMenu.add (fileOpen);
       fileOpen.addActionListener (new ActionListener() 
         {
           public void actionPerformed (ActionEvent event) {
             tabs.setSelectedComponent (inputPanel);
             openInputFile();		    
           } // end ActionPerformed method
         } // end action listener
       );
       
       // Menu Item for EPub
       fileEpub = new JMenuItem ("Create EPub...");
       fileMenu.add (fileEpub);
       fileEpub.addActionListener (new ActionListener()
         {
           public void actionPerformed (ActionEvent event) {
             chooseEpubFiles();
           } // end ActionPerformed method
         } // end action listener
       );
     }
     
   }
   
   public void setTabs(JTabbedPane tabs) {
     quietMode = false;
     tabSet = true;
     
     this.tabs = tabs;
     
     // Create common interface components
     raisedBevel = BorderFactory.createRaisedBevelBorder();
     etched      = BorderFactory.createEtchedBorder();
     
 		inputPanel = new JPanel();
     
     // Button to Specify the Input Source and Open it
     openDataButton = new JButton ("Open Input");
     openDataButton.setToolTipText
       ("Specify the Data Source to be Input");
     openDataButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           openInputFile();		    
         } // end ActionPerformed method
 		  } // end action listener
 		);
         
 		// Combo box for input type
     inputTypeLabel = new JLabel ("Type of Data Source", JLabel.LEFT);
     inputTypeLabel.setBorder (etched);
     
 		inputTypeBox.setEditable (false);
 		inputTypeBox.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
 		      JComboBox cb = (JComboBox)event.getSource();
 		      String inType = (String)cb.getSelectedItem();
           initInputModules();
           
           inputModuleIndex = 0;
           inputModuleFound = false;
           while (inputModuleIndex < inputModules.size() && (! inputModuleFound)) {
             inputModule = inputModules.get(inputModuleIndex);
             inputModuleFound = inputModule.setInputType(inType);
             if (! inputModuleFound) {
               inputModuleIndex++;
             }
           }
 		    } // end ActionPerformed method
 		  } // end action listener for input type combo box
 		); 
     
     // Create Check Box for Data Dictionary Input
     inputDictionaryLabel = new JLabel ("Data Dictionary Input", JLabel.LEFT);
     inputDictionaryLabel.setBorder (etched);
     
     inputDictionaryCkBox = new JCheckBox ("Open Companion Dictionary?");
     inputDictionaryCkBox.setSelected (false);
     inputDictionaryCkBox.addItemListener (new ItemListener()
 		  {
 		    public void itemStateChanged (ItemEvent event) {
           usingDictionary 
               = (event.getStateChange() != ItemEvent.DESELECTED);
           setDictionaryImplications();
 		    } // end itemStateChanged method
 		  } // end action listener
 		);
     
     // Create Radio Buttons for File Merge
     inputMergeLabel = new JLabel ("Merge into Existing Data", JLabel.LEFT);
     inputMergeLabel.setBorder (etched);
 
     inputMergeGroup = new ButtonGroup();
   
     inputMergeNoButton = new JRadioButton ("No Merge");
     inputMergeNoButton.setActionCommand ("NO");
     inputMergeNoButton.setSelected (true);
     merge = 0;
     inputMergeGroup.add (inputMergeNoButton);
     inputMergeNoButton.addActionListener  (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           merge = 0;
           setMergeImplications();
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
     inputMergeButton = new JRadioButton ("Merge New Data with Old");
     inputMergeButton.setActionCommand ("MERGE");
     inputMergeGroup.add (inputMergeButton);
     inputMergeButton.addActionListener  (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           merge = 1;
           setMergeImplications();
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
 		
     inputMergeSameColumnsButton = new JRadioButton ("Merge with Same Columns");
     inputMergeSameColumnsButton.setActionCommand ("SAME");
     inputMergeGroup.add (inputMergeSameColumnsButton);
     inputMergeSameColumnsButton.addActionListener  (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           merge = 2;
           setMergeImplications();
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
     // create directory depth fields
     inputDirMaxDepthLabel = new JLabel ("Maximum Directory Depth", JLabel.LEFT);
     inputDirMaxDepthLabel.setBorder (etched);
     
     inputDirMaxDepthValue = new JTextField (String.valueOf(dirMaxDepth));
     inputDirMaxDepthValue.setEditable (false);
     inputDirMaxDepthValue.setHorizontalAlignment (JTextField.RIGHT);
     
     inputDirMaxDepthUpButton = new JButton ("Increment (+)");
     inputDirMaxDepthUpButton.setBorder (raisedBevel);
     inputDirMaxDepthUpButton.setToolTipText
       ("Increase Level of Sub-Directory Explosion");
     inputDirMaxDepthUpButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           dirMaxDepth++;
           inputDirMaxDepthValue.setText (String.valueOf(dirMaxDepth));
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
     inputDirMaxDepthDownButton = new JButton ("Decrement (-)");
     inputDirMaxDepthDownButton.setBorder (raisedBevel);
     inputDirMaxDepthDownButton.setToolTipText
       ("Decrease Level of Sub-Directory Explosion");
     inputDirMaxDepthDownButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           if (dirMaxDepth > 1) {
             dirMaxDepth--;
           }
           inputDirMaxDepthValue.setText (String.valueOf(dirMaxDepth));
 		    } // end ActionPerformed method
 		  } // end action listener
 		);
     
 		// Combo box for Normalization type
     inputNormalLabel = new JLabel ("Data Normalization", JLabel.LEFT);
     inputNormalLabel.setBorder (etched);
     
 		normalType = 0;
     if (normalization) {
       inputNormalBox.setSelectedIndex (0);
       inputNormalBox.setEditable (false);
       inputNormalBox.addActionListener (new ActionListener()
         {
           public void actionPerformed (ActionEvent event) {
             JComboBox cb = (JComboBox)event.getSource();
             String inType = (String)cb.getSelectedItem();
             normalType = 0;
             if (inType.equals (INPUT_NORMAL1)) {
               normalType = 1;
             }
             setNormalTypeImplications();
           } // end ActionPerformed method
         } // end action listener for input type combo box
       ); 
     }
         
     // Bottom of Screen
 		inputText = new JTextArea("");
 		inputText.setLineWrap (true);
 		inputText.setEditable (false);
 		inputText.setWrapStyleWord (true);
 		inputText.setVisible (true);
 
     // Finish up the Input Pane
     setMergeImplications();
     setNormalTypeImplications();
     
 		gb.startLayout (inputPanel, 3, 9);
 		gb.setByRows (false);
 		gb.setAllInsets (2);
 		gb.setDefaultRowWeight (0.0);
 		
 		// Column 0
     gb.setBottomInset(6);
     gb.add (openDataButton);
     
     gb.setTopInset(6);
     gb.setBottomInset(2);
     gb.add (inputTypeLabel);
     gb.setAllInsets(2);
     gb.add (inputTypeBox);
     
     gb.setRow(5);
     gb.setTopInset(6);
     gb.add (inputDirMaxDepthLabel);
     gb.setAllInsets(2);
     gb.add (inputDirMaxDepthValue);
     gb.add (inputDirMaxDepthUpButton);
     gb.add (inputDirMaxDepthDownButton);
     
     // Column 1
     gb.nextColumn();
     gb.setRow(1);
     gb.setTopInset(6);
     gb.add (inputDictionaryLabel);
     gb.setAllInsets(2);
     gb.add (inputDictionaryCkBox);
     
     if (normalization) {
       gb.setRow(5);
       gb.setTopInset(6);
       gb.setBottomInset(2);
       gb.add (inputNormalLabel);
       gb.setAllInsets(2);
       gb.add (inputNormalBox);
     }
     
     // Column 2
     gb.nextColumn();
     gb.setRow(1);
     gb.setTopInset(6);
     gb.add (inputMergeLabel);
     gb.setAllInsets(2);
     gb.setTopInset(5);
     gb.add (inputMergeNoButton);
     gb.add (inputMergeButton);
     gb.add (inputMergeSameColumnsButton);
     
     // Bottom of Panel
     gb.setRow(9);
     gb.setColumn(0);
     gb.setWidth (3);
     gb.setRowWeight (1.0);
     gb.add (inputText);
 
 		tabs.addTab ("Input", inputPanel);
   }
   
   /**
    Select the tab for this panel. 
   */
   public void selectTab() {
     if (tabs != null) {
       tabs.setSelectedComponent (inputPanel);
     }
   }
   
   /**
      Play one recorded action in the Input module.
    */
   public void playScript (
       String  inActionAction,
       String  inActionModifier,
       String  inActionObject,
       String  inActionValue,
       int     inActionValueAsInt,
       boolean inActionValueValidInt) {
     
     if (inActionAction.equals (ScriptConstants.EPUB_IN_ACTION)) {
       epubFolder = new File (inActionValue);
     }
     else
     if (inActionAction.equals (ScriptConstants.EPUB_OUT_ACTION)) {
       epubFile = new File (inActionValue);
       createEpub();
     }
     else
     if (inActionAction.equals (ScriptConstants.SET_ACTION)) {
       if (inActionObject.equals (ScriptConstants.DIR_DEPTH_OBJECT)) {
         if (inActionValueValidInt) {
           if (inActionValueAsInt > 0) {
             dirMaxDepth = inActionValueAsInt;
           } else {
             Logger.getShared().recordEvent (LogEvent.MEDIUM, 
               inActionValue + " is not a valid value for an Open Directory Depth",
               true);
           }
         } else {
           Logger.getShared().recordEvent (LogEvent.MEDIUM, 
             inActionValue + " is not a valid integer for an Open Directory Depth Value",
             true);
         }
       }
       else
       if (inActionObject.equals (ScriptConstants.NORMAL_OBJECT)) {
         if (inActionValueValidInt) {
           if (inActionValueAsInt >= 0
               && inActionValueAsInt <= NORMALTYPE_MAX) {
             normalType = inActionValueAsInt;
             setNormalTypeImplications();
           } else {
             Logger.getShared().recordEvent (LogEvent.MEDIUM, 
               inActionValue + " is not a valid value for a Normalization Type Value",
               true);
           }
         } else {
           Logger.getShared().recordEvent (LogEvent.MEDIUM, 
             inActionValue + " is not a valid integer for a Normalization Type Value",
             true);
         }
       } else {
         Logger.getShared().recordEvent (LogEvent.MEDIUM, 
           inActionObject + " is not a valid Scripting Object for an Open Set Action",
           true);
       }
     } 
     else
     if (inActionAction.equals (ScriptConstants.OPEN_ACTION)) {
       
       merge = 0;
       if (inActionObject.equals (ScriptConstants.MERGE_OBJECT)) {
         merge = 1;
         setMergeImplications();
       } else
       if (inActionObject.equals (ScriptConstants.MERGE_SAME_OBJECT)) {
         merge = 2;
         setMergeImplications();
       }
 
       inputModuleIndex = 0;
       inputModuleFound = false;
       while (inputModuleIndex < inputModules.size() && (! inputModuleFound)) {
         inputModule = inputModules.get(inputModuleIndex);
         inputModuleFound = inputModule.setInputTypeByModifier(inActionModifier);
         if (! inputModuleFound) {
           inputModuleIndex++;
         }
       }
           
       if (inActionModifier.equals (ScriptConstants.URL_MODIFIER)) {
         try {
           tabURL = new URL (Home.getShared().getPageURL(), inActionValue);
         } catch (MalformedURLException e) {
           tabURL = null;
         }
         if (tabURL == null) {
           Logger.getShared().recordEvent (LogEvent.MEDIUM, 
             inActionValue + " is not a valid " + inActionModifier + " for an Open Action",
             true);
         }
         else {
           fileName = inActionValue;
           openURL();
           // openDataName.setText (fileNameToDisplay);
         } // end file existence selector
       } // end if URL modifier
       else
       if (inputModuleFound) {
         chosenFile = new File (inActionValue);
         if (chosenFile == null) {
           Logger.getShared().recordEvent (LogEvent.MEDIUM, 
             inActionValue + " is not a valid " + inActionModifier + " for an Open Action",
             true);
         }
         else {
           openFileOrDirectory();
           // openDataName.setText (fileNameToDisplay);
         } // end file existence selector
       } // end file or directory
       else {
         Logger.getShared().recordEvent (LogEvent.MEDIUM, 
           inActionModifier + " is not a valid Scripting Modifier for an Open Action",
           true);
       } // end Action Modifier selector
     } // end valid action
     else {
       Logger.getShared().recordEvent (LogEvent.MEDIUM, 
         inActionAction + " is not a valid Scripting Action for the Open Module",
         true);
     } // end Action selector
   } // end playInputModule method
   
   /**
    Add another input module to the list. 
   
    @param anotherInputModule Another PSTextMerge input module to be made
                              available. 
   */
   private int addInputModule (
       TextMergeInputModule anotherInputModule,
       int insertAt) {
     int k = insertAt;
     inputModules.add(anotherInputModule);
     anotherInputModule.setInputType(0);
     for (int j = 1; j <= anotherInputModule.getInputTypeMax(); j++) {
       inputTypeBox.insertItemAt
           (anotherInputModule.getInputTypeLabel(j), k);
       k++;
     }
     return k;
   }
   
   private void initInputModules() {
     for (int i = 0; i < inputModules.size(); i++) {
       inputModules.get(i).setInputType(0);
     }
   }
   
   /**
      Open the tab-delimited data file as an empty data set.
    */
  private void openEmpty () {
     fileNameToDisplay = "No Input File";
     tabName = "";
     dataDict = new DataDictionary();
     dataDict.setLog (log);
     dataRecList.initialize();
    
     initDataSets();
     this.textMergeController.setListAvailable(false);
   }
   
   /**
      Open the input file.
    */
   private void openInputFile() {
     JFileChooser fileChooser = new JFileChooser();
     fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
     if (textMergeScript.hasCurrentDirectory()) {
       fileChooser.setCurrentDirectory (textMergeScript.getCurrentDirectory());
     } 
     int fileChooserReturn 
       = fileChooser.showOpenDialog (openDataButton);
     if (fileChooserReturn 
       == JFileChooser.APPROVE_OPTION) {
       chosenFile = fileChooser.getSelectedFile();
       openFileOrDirectory();
     }
   }
   
   /**
    Open the passed file or directory as an input file. 
   
    @param inFile The file or directory to be opened. 
   */
   public void openFileOrDirectory (File inFile) {
     chosenFile = inFile;
     openFileOrDirectory();
   }
   
   /** 
      Decides whether to open the data source as a file or as a directory.
    */
   private void openFileOrDirectory() {
     textMergeScript.recordScriptAction (
         ScriptConstants.INPUT_MODULE, 
         ScriptConstants.SET_ACTION,
         ScriptConstants.NO_MODIFIER, 
         ScriptConstants.NORMAL_OBJECT, 
         String.valueOf (normalType));
     log.recordEvent (LogEvent.NORMAL,
         "Rows before open: "
             + String.valueOf(psList.totalSize()),
         false);
     if (merge == 0) {
       dataDict = new DataDictionary();
       dataDict.setLog (log);
     }
     
     FileName chosenFileName = new FileName (chosenFile);
     
     if (chosenFileName.getExt().trim().equals (inXML.getPreferredExtension())
         && inXML.getInputType() < 1 && inTunes.getInputType() < 1) {
       inXML.setInputType(2);
       inputModuleFound = true;
       inputModule = inXML;
     }
     else
     if (chosenFileName.getExt().trim().equals (inExcel.getPreferredExtension())
         && inExcel.getInputType() == 0) {
       inExcel.setInputType(1);
       inputModuleFound = true;
       inputModule = inExcel;
     }
     /*
     else
     if (chosenFileName.getExt().trim().equals (inYAML.getPreferredExtension())
         && inYAML.getInputType() == 0) {
       inYAML.setInputType(1);
       inputModuleFound = true;
       inputModule = inYAML;
     } */
     
     if (! inputModuleFound) {
       inTDF.setInputType(1);
       inputModuleFound = true;
       inputModule = inTDF;
     }
     
     if (inputModuleFound) {
       fileNameToDisplay = chosenFile.getName();
       tabName = chosenFile.getAbsolutePath();
       if (chosenFile.isDirectory()) {
         setCurrentDirectoryFromDir (chosenFile);
         TextMergeDirectoryReader dirReader 
             = new TextMergeDirectoryReader (chosenFile);
         dirReader.setInputModule(inputModule);
         dirReader.setMaxDepth(dirMaxDepth);
         textMergeScript.recordScriptAction (
             ScriptConstants.INPUT_MODULE, 
             ScriptConstants.SET_ACTION,
             ScriptConstants.NO_MODIFIER, 
             ScriptConstants.DIR_DEPTH_OBJECT, 
             String.valueOf (dirMaxDepth));
         dataSource = dirReader;
       } else {
         setCurrentDirectoryFromFile (chosenFile);
         textMergeScript.setNormalizerPath(textMergeScript.getCurrentDirectory().getPath());
         openDict();
         dataSource = inputModule.getDataSource(chosenFile);
       }
       dataSource.setDebug (debug);
       openData();
       textMergeScript.recordScriptAction (
           ScriptConstants.INPUT_MODULE, 
           ScriptConstants.OPEN_ACTION, 
           inputModule.getInputTypeModifier(), 
           inputObject, 
           chosenFile.getAbsolutePath());
     }
     
     if (dataRecList != null) {
       log.recordEvent (LogEvent.NORMAL,
         "Rows loaded:      "
             + String.valueOf(dataRecList.getRecordsLoaded()),
         false);
     }
     log.recordEvent (LogEvent.NORMAL,
         "Rows after open:  "
             + String.valueOf(psList.totalSize()),
         false);
   } // end openFileOrDirectory method
   
   /**
      Open dictionary file, if requested.
    */
   private void openDict () {
     if (usingDictionary) {
       tabFileName = new FileName (tabName);
       dictFile = 
         new TabDelimFile (textMergeScript.getCurrentDirectory(),
           tabFileName.replaceExt(DICTIONARY_EXT));
       try {
         dataDict.load (dictFile);
       } catch (IOException e) {
         log.recordEvent (LogEvent.MEDIUM, 
             "Problem Reading Input Dictionary",
             false);
       } // end of catch
     } // end if using dictionary
   }
   
   /**
      Open the tab-delimited data file as a URL on the Web.
    */
   private void openURL () {
     textMergeScript.recordScriptAction (
         ScriptConstants.INPUT_MODULE, 
         ScriptConstants.SET_ACTION,
         ScriptConstants.NO_MODIFIER, 
         ScriptConstants.NORMAL_OBJECT, 
         String.valueOf (normalType));
     if (merge == 0) {
       dataDict = new DataDictionary();
       dataDict.setLog (log);
     }
     tabName = tabURL.toString();
     fileNameToDisplay = fileName;
     dataSource = new TabDelimFile (tabURL);
     dataSource.setDebug (debug);
     openData();
     textMergeScript.recordScriptAction (
         ScriptConstants.INPUT_MODULE, 
         ScriptConstants.OPEN_ACTION, 
         ScriptConstants.URL_MODIFIER,
         inputObject, tabURL.toString());
   }
   
   /**
      Opens the input data source (whether a file or a directory).
    */
   private void openData () { 
 
     dataSource.setLog (log);
     DataSource original = dataSource;
     boolean openOK = false;
     if (normalType > 0) {
       if (normalType == 1) {
         try {
           BoeingDocsNormalizer docs = new BoeingDocsNormalizer (original);
           docs.setDataParent (textMergeScript.getNormalizerPath());
           dataSource = docs;
           log.recordEvent (LogEvent.NORMAL, 
             "BoeingDocsNormalizer successfully constructed",
             false);
         } catch (IOException e) {
           log.recordEvent (LogEvent.MAJOR, 
             "I/O Error in Data Normalization routine",
             false);
         }
       } // end if Boeing docs normalizer
       dataSource.setLog (log);
     } //end if noralization type specified
       
     try {
       if (merge == 1) {
         dataRecList.merge (dataSource);
       }
       else
       if (merge == 2) {
         dataRecList.mergeSame (dataSource);
       }
       else {
         dataRecList.load(dataDict, dataSource, log);
       }
       dataDict.setLog (log);
       dataRecList.setSource(new FileSpec(chosenFile));
 
       initDataSets();
       
       /*
       if (openOutputDataButton != null) {
         openOutputDataButton.setEnabled (true);
         fileSave.setEnabled (true);
       }
       */
       log.recordEvent (LogEvent.NORMAL, 
           "Data Source named "
               + fileNameToDisplay
               + " was opened successfully",
           false);
       openOK = true;
     } catch (IOException e) {
       if (quietMode) {
         log.recordEvent (LogEvent.MEDIUM, 
             "Data Source named "
                 + fileNameToDisplay
                 + " could not be opened successfully" 
                 + "\n       Data Source = "
                 + original.toString()
                 + "\n       I/O Error = "
                 + e.toString(),
           true);
       } else {
         log.recordEvent (LogEvent.MEDIUM, 
             "Data Source named "
                 + fileNameToDisplay
                 + " could not be opened successfully"
                 + "\n       Data Source = "
                 + original.toString()
                 + "\n       I/O Error = "
                 + e.toString(),
           true);
         if (! quietMode) {
           JOptionPane.showMessageDialog (tabs, 
               "Data Source named "
                   + fileNameToDisplay
                   + " could not be opened successfully",
               "Data Source Error",
               JOptionPane.ERROR_MESSAGE);
         }
       }
       openOK = false;
       dataRecList.newListLoaded();
     } // end try block
     
     textMergeController.setListAvailable(openOK);
 
   } // openData method
   
   private void chooseEpubFiles () {
 
     // Let the user select the folder containing the contents of the book
     JFileChooser folderChooser = new JFileChooser();
     folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
     folderChooser.setDialogTitle("Open Folder containing EPub Contents");
     if (textMergeScript.hasCurrentDirectory()) {
       folderChooser.setCurrentDirectory (textMergeScript.getCurrentDirectory());
     }
     int folderChooserReturn = folderChooser.showOpenDialog (tabs);
     if (folderChooserReturn == JFileChooser.APPROVE_OPTION) {
 
       // Let the user specify the name and location of the output epub file
       epubFolder = folderChooser.getSelectedFile();
       JFileChooser fileChooser = new JFileChooser();
       fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
       fileChooser.setCurrentDirectory(epubFolder.getParentFile());
       String epubFileName;
       if (epubFolder.getName().equalsIgnoreCase("epub")) {
         epubFileName = epubFolder.getParentFile().getName() + ".epub";
       } else {
         epubFileName = epubFolder.getName() + ".epub";
       }
       fileChooser.setSelectedFile(
             new File(epubFolder.getParentFile(), epubFileName));
       fileChooser.setDialogTitle("Specify the output EPub File");
       int fileChooserReturn = fileChooser.showSaveDialog (tabs);
       if (fileChooserReturn == JFileChooser.APPROVE_OPTION) {
 
         // Copy the folder contents into the output zip file
         epubFile = fileChooser.getSelectedFile();
         createEpub();
       } // end if user specified an output file
     } // end if user specified an input folder
   } // end method chooseEpubFiles
   
   /**
      Create the EPub file.
    */
   private void createEpub() {
         
     try {
       FileOutputStream epubStream = new FileOutputStream(epubFile);
       ZipOutputStream epub = new ZipOutputStream(
           new BufferedOutputStream(epubStream));
       // The Mime Type must be the first entry
       addEpubEntry (epub, epubFolder, new File (epubFolder, MIME_TYPE),
           ZipOutputStream.DEFLATED);
       addEpubDirectory (epub, epubFolder, epubFolder);
       epub.close();
       textMergeScript.recordScriptAction (
           ScriptConstants.INPUT_MODULE, 
           ScriptConstants.EPUB_IN_ACTION, 
           ScriptConstants.NO_MODIFIER,
           inputObject, epubFolder.toString());
       textMergeScript.recordScriptAction (
           ScriptConstants.INPUT_MODULE, 
           ScriptConstants.EPUB_OUT_ACTION, 
           inTDF.getInputTypeModifier(1),
           inputObject, epubFile.toString());
       log.recordEvent (LogEvent.NORMAL,
         "Successfully created EPub file " + epubFile.toString(),
         false);
     } catch (IOException e) {
       log.recordEvent (LogEvent.MEDIUM,
         "Unable to create EPub file " + epubFile.toString()
         + " due to I/O Exception " + e.toString(),
         false);
       Trouble.getShared().report
         ("I/O error creating an EPub from " + epubFolder.toString(),
         "EPub Problem");
     }
   } // end method createEpub
   
   /**
    Add the contents of the specified folder, including the contents
    of sub-folders, to the specified zip output stream.
 
    @param zipOut     The zip output stream to receive the output.
    @param topFolder  The top folder being zipped.
    @param folder     The specific folder to be zipped.
    @throws java.io.IOException
    */
   private void addEpubDirectory(
       ZipOutputStream zipOut, 
       File topFolder, 
       File folder)
         throws java.io.IOException {
     
     String filesAndFolders[] = folder.list();
     for (int i = 0; i < filesAndFolders.length; i++) {
       String nextFileOrFolderName = filesAndFolders[i];
       File nextFileOrFolder = new File (folder, nextFileOrFolderName);
       if (nextFileOrFolderName.equalsIgnoreCase (MIME_TYPE)) {
         // skip this, since we should have already added it as the first entry
       }
       else
       if (nextFileOrFolderName.startsWith(".")) {
         // skip this, since we don't want hidden system files
       }
       else
       if (nextFileOrFolder.isDirectory()) {
         addEpubDirectory (zipOut, topFolder, nextFileOrFolder);
       }
       else
       if (nextFileOrFolder.isFile()) {
         addEpubEntry (zipOut, topFolder, nextFileOrFolder,
             ZipOutputStream.DEFLATED);
       }
     } // end for each file or folder in the directory
   } // end method addEpubDirectory
 
   /**
    Add another entry to the specified zip output stream.
 
    @param zipOut     The zip output stream to receive the output.
    @param topFolder  The top folder being zipped.
    @param file       The specific file to be added.
    @throws java.io.IOException
    */
   private void addEpubEntry(
       ZipOutputStream zipOut,
       File topFolder,
       File file,
       int method)
         throws java.io.IOException {
     FileInputStream inStream = new FileInputStream(file);
     String topFolderPath = topFolder.getPath();
     String filePath = file.getPath();
     String zipPath = filePath.substring(topFolderPath.length() + 1);
     ZipEntry entry = new ZipEntry (zipPath);
     entry.setMethod(method);
     zipOut.putNextEntry(entry);
     int bytesRead;
     byte[] buffer = new byte[4096];
     while((bytesRead = inStream.read(buffer)) != -1) {
       zipOut.write(buffer, 0, bytesRead);
     }
     inStream.close();
   }
   
   private void setCurrentDirectoryFromFile (File inFile) {
     textMergeScript.setCurrentDirectory(new File (inFile.getParent()));
   }
   
   private void setCurrentDirectoryFromDir (File inFile) {
     textMergeScript.setCurrentDirectory(inFile);
   }
   
   /**
      Sets other values related to the merge option.
    */
   private void setMergeImplications () {
     if ((merge > 0) && (! textMergeController.isListAvailable())) {
       merge = 0;
     }
     if (merge == 1) {
       mergeValue = "Yes";
       inputObject = ScriptConstants.MERGE_OBJECT;
     }
     else 
     if (merge == 2) {
       mergeValue = "Same";
       inputObject = ScriptConstants.MERGE_SAME_OBJECT;
     } else {
       mergeValue = "No";
       inputObject = ScriptConstants.NO_OBJECT;
     }
   }
   
   /**
      Depending on we are using a data dictionary, sets other
      appropriate values.
    */
   private void setDictionaryImplications() {
     if (usingDictionary) {
       usingDictionaryValue = "Yes";
     } else {
       usingDictionaryValue = "No";
     }
     textMergeScript.recordScriptAction 
      (ScriptConstants.OUTPUT_MODULE, 
       ScriptConstants.SET_ACTION, 
       ScriptConstants.NO_MODIFIER, 
       ScriptConstants.USING_DICTIONARY_OBJECT, 
       String.valueOf(usingDictionary));
   }
   
   /**
      Sets appropriate input data normalization values.
    */
   private void setNormalTypeImplications() {
     if (normalType == 0) {
       normalTypeValue = "No Normalization";
     } 
     else 
     if (normalType == 1) {
       normalTypeValue = "Boeing Docs";
     }
   }
   
   private void initDataSets () {
     psList.setComparator(new PSDefaultComparator());
     psList.setInputFilter(null);
     /*
     dataTable = new DataTable (filteredDataSet);
     numberOfFields = recDef.getNumberOfFields();
     if (tabTableBuilt) {
       tabTable.setModel (dataTable);
       tabNameLabel.setText (fileNameToDisplay);
       setColumnWidths();
     }
     if (sortTabBuilt) {
       loadSortFields();
     }
     if (filterTabBuilt) {
       loadFilterFields();
     }
     */
   }
   
   public String getFileNameToDisplay() {
     return fileNameToDisplay;
   }
   
 }
