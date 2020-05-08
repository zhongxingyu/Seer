 /*
  * GUIActionListener.java
  *
  * Created on 2. Oktober 2006, 11:02
  *
  * Copyright (C) 2007
  * German Research Center for Artificial Intelligence (DFKI GmbH) Saarbruecken
  * Hochschule fuer Technik und Wirtschaft (HTW) des Saarlandes
  * Developed by Oliver Fourman, Ingo Zinnikus, Matthias Klusch
  *
  * The code is free for non-commercial use only.
  * You can redistribute it and/or modify it under the terms
  * of the Mozilla Public License version 1.1  as
  * published by the Mozilla Foundation at
  * http://www.mozilla.org/MPL/MPL-1.1.txt
  */
 
 package de.dfki.dmas.owls2wsdl.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.WindowConstants;
 import javax.swing.filechooser.FileFilter;
 
 import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;
 import de.dfki.dmas.owls2wsdl.core.AbstractDatatype;
 import de.dfki.dmas.owls2wsdl.core.AbstractDatatypeKB;
 import de.dfki.dmas.owls2wsdl.core.AbstractDatatypeLoadingThread;
 import de.dfki.dmas.owls2wsdl.core.AbstractService;
 import de.dfki.dmas.owls2wsdl.core.AbstractServiceParameter;
 import de.dfki.dmas.owls2wsdl.core.Project;
 import de.dfki.dmas.owls2wsdl.core.ProjectLoadingThread;
 import de.dfki.dmas.owls2wsdl.gui.models.AbstractDatatypeListModel;
 import de.dfki.dmas.owls2wsdl.gui.models.ServiceListModel;
 import de.dfki.dmas.owls2wsdl.utils.HandbookEnvBuilder;
 import de.dfki.dmas.owls2wsdl.utils.TranslationUtils;
 import de.dfki.dmas.owls2wsdl.utils.WSDL2JavaWrapper;
 
 /**
  * 
  * @author Oliver Fourman
  */
 public class GUIActionListener implements ActionListener, WindowListener {
 
 	OWLS2WSDLGui mainWindowRef;
 
 	//
 	// CONSTANTS THAT DESCRIBE RUNTIME ACTIONS
 	//
 	public final static String LOAD_OWLS_FILES = "load and parse owl-s files";
 	public final static String LOAD_OWLS_URL = "load and parse owl file at URL";
 
 	public final static String IMPORT_ONTOLOGY_OWL_FILE = "parse ontology file";
 	public final static String IMPORT_ONTOLOGY_URL = "parse ontology file at URL";
 
 	public final static String LOAD_TYPE_INFORMATION = "load persistent (castor) datatype information .xml to project";
 	public final static String SAVE_TYPE_INFORMATION = "save persistent datatype information .xml";
 
 	public final static String LOAD_SERVICE_INFORMATION = "load persistent service information";
 	public final static String SAVE_SERVICE_INFORMATION = "save persistent service information";
 
 	// Edit
 	public final static String REMOVE_SELECTED_SERVICES = "remove selected service from project";
 	public final static String REMOVE_ALL_SERVICES = "remove services from the opened project";
 	public final static String REMOVE_SELECTED_DATATYPE = "remove selected datatype from project";
 	public final static String REMOVE_ALL_DATATYPES = "remove datatypes from opened project";
 	public final static String REMOVE_UNREFERENCED_DATATYPES = "remove unreferenced datatypes from opened project";
 	public final static String SELECT_ALL_SERVICES = "select all services for export function";
 
 	public final static String CHECK_OWLS_PARAM = "(re-)check if abstract datatypes for parameters are available in KB";
 	public final static String GENERATE_XSD = "generate xsd for selected datatype";
 	public final static String GENERATE_WSDL = "generate wsdl for selected service";
 	public final static String GENERATE_OWLS = "generate owl-s for selected service";
 
 	public final static String NEW_PROJECT = "clean service and datatype information in application";
 	public final static String LOAD_PROJECT = "load persistent project information";
 	public final static String SAVE_PROJECT = "save persistent project information";
 	public final static String SET_PROJECT_NAME = "set new project name";
 
 	public final static String SAVE_OUTPUT_PANEL_SEL = "to save selected generated xsd, wsdl or owls code in output panel";
 	public final static String SAVE_OUTPUT_PANEL = "to save all generated xsd, wsdl and owls code in output panel";
 
 	// View
 	public final static String SHOW_PROJECTDETAILS = "switch to project detail view";
 	public final static String SHOW_SERVICEDETAILS = "switch to service detail view";
 	public final static String SHOW_DATATYPEDETAILS = "switch to datatype detail view";
 
 	public final static String UPDATE_OVERVIEWS = "update datatype and service overview list";
 
 	public final static String SHOW_WSDL2OWLS_CONVERTER = "WSDL2OWL-S Converter, do some reengineering, generate wsdl and then owls";
 	public final static String SHOW_CONFIGURATIONFRAME = "Opens configuration frame.";
 	public final static String SHOW_WSDL_EXPORTFRAME = "Opens new frame for batch wsdl export processing.";
 
 	// Extras
 	public final static String DETERMINE_SERVICE_DEPENDENCY_TYPES = "project service dependencies";
 
 	public final static String WSDL2JAVA = "use Axis to generate Java sourcecode";
 
 	public final static String SHOW_HELP_CONTENTS = "show help dialog";
 	public final static String SHOW_ABOUT = "show about dialog";
 	public final static String CLOSE = "close application";
 
 	final URL resExitIcon32x32URL = GUIActionListener.class
 			.getResource("/images/32x32/actions/system-log-out.png");
 	final Icon iconExit32x32 = new ImageIcon(resExitIcon32x32URL);
 
 	/** Creates a new instance of GUIActionListener */
 	public GUIActionListener(OWLS2WSDLGui frame) {
 		this.mainWindowRef = frame;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		System.out.println("[.] GUIActionListener [command] "
 				+ e.getActionCommand());
 		if (e.getActionCommand().equals(REMOVE_SELECTED_SERVICES)) {
 			if (this.mainWindowRef.getMainPane().serviceList.isSelectionEmpty()) {
 				JOptionPane.showMessageDialog(this.mainWindowRef,
 						"Nothing selected.\nNo services removed from project.",
 						"Error removing services", JOptionPane.WARNING_MESSAGE);
 			} else {
 				RuntimeModel.getInstance().unsetSelectedService();
 
 				int[] selectedIndices = this.mainWindowRef.getMainPane().serviceList
 						.getSelectedIndices();
 				Vector<AbstractService> aserviceList = new Vector<AbstractService>();
 
 				for (int i = 0; i < selectedIndices.length; i++) {
 					AbstractService aservice = this.mainWindowRef.getMainPane().serviceListModel
 							.getElementAt(selectedIndices[i]);
 					System.out.println("IDX: " + selectedIndices[i]
 							+ " Service: " + aservice.getID() + " ("
 							+ System.identityHashCode(aservice) + ")");
 					aserviceList.add(aservice);
 				}
 
 				for (Iterator<AbstractService> it = aserviceList.iterator(); it
 						.hasNext();) {
 					RuntimeModel.getInstance().getProject()
 							.getAbstractServiceCollection()
 							.removeAbstractService(it.next());
 				}
 
 				this.getMainWindowRef().getMainPane().serviceList
 						.clearSelection();
 
 				String statusMsg = "service removed";
 				if (selectedIndices.length > 1) {
 					statusMsg = "services removed";
 				}
 
 				// this.updateServiceList(); // obsolete, using Observable
 				// pattern
 				RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 						RuntimeModel.SERVICE_MODEL_CHANGED, statusMsg);
 			}
 		} else if (e.getActionCommand().equals(REMOVE_ALL_SERVICES)) {
 			RuntimeModel.getInstance().unsetSelectedService();
 			RuntimeModel.getInstance().getProject()
 					.getAbstractServiceCollection().removeAllElements();
 			RuntimeModel.getInstance().getProject()
 					.determineAllDependecyTypes();
 			((ServiceListModel) this.mainWindowRef.getMainPane().serviceList
 					.getModel()).syncWithProject(RuntimeModel.getInstance()
 					.getProject());
 			this.mainWindowRef.getMainPane().serviceList.revalidate();
 			this.mainWindowRef.getMainPane().serviceList.updateUI();
 			RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 					RuntimeModel.SERVICE_MODEL_CHANGED, "all services removed");
 		} else if (e.getActionCommand().equals(REMOVE_SELECTED_DATATYPE)) {
 			if (this.mainWindowRef.getMainPane().datatypeList
 					.isSelectionEmpty()) {
 				JOptionPane.showMessageDialog(this.mainWindowRef,
 						"Nothing selected.\nNo datatype removed from project.",
 						"Error removing datatype", JOptionPane.WARNING_MESSAGE);
 			} else {
 				int selectedIndex = this.mainWindowRef.getMainPane().datatypeList
 						.getSelectedIndex();
 				AbstractDatatype atype = ((AbstractDatatypeListModel) this.mainWindowRef
 						.getMainPane().datatypeList.getModel())
 						.getElementAt(selectedIndex);
 				System.out.println("[i] remove " + selectedIndex + ": "
 						+ atype.getUrl());
 				RuntimeModel.getInstance().unsetSelectedDatatype();
 				AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData()
 						.removeRegisteredDatatype(atype.getUrl());
 
 				// this.mainWindowRef.getMainPane().datatypeList.clearSelection();
 				// \-> Exception in thread "AWT-EventQueue-0"
 				// java.lang.ArrayIndexOutOfBoundsException: -1
 
 				RuntimeModel.getInstance().getProject()
 						.determineAllDependecyTypes();
 				this.updateDatatypeList();
 
 				// if(!this.mainWindowRef.getMainPane().datatypeList.isSelectionEmpty())
 				// {
 				// int idx =
 				// this.mainWindowRef.getMainPane().datatypeList.getSelectedIndex();
 				// if(idx >=0 && idx <
 				// this.mainWindowRef.getMainPane().datatypeList.getModel().getSize())
 				// {
 				// RuntimeModel.getInstance().setSelectedDatatype(
 				// ((AbstractDatatypeListModel)this.mainWindowRef.getMainPane().datatypeList.getModel()).getAbstractDatatypeAt(idx)
 				// );
 				// }
 				// }
 
 				RuntimeModel.getInstance()
 						.setStatusAndNotify("RUNTIME",
 								RuntimeModel.DATATYPE_MODEL_CHANGED,
 								"datatype removed");
 			}
 		} else if (e.getActionCommand().equals(REMOVE_ALL_DATATYPES)) {
 			RuntimeModel.getInstance().unsetSelectedDatatype();
 			AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData()
 					.removeAllDatatypes();
 			RuntimeModel.getInstance().getProject()
 					.determineAllDependecyTypes();
 			this.updateDatatypeList();
 			RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 					RuntimeModel.DATATYPE_MODEL_CHANGED,
 					"all datatypes removed");
 		} else if (e.getActionCommand().equals(REMOVE_UNREFERENCED_DATATYPES)) {
 			RuntimeModel.getInstance().unsetSelectedDatatype();
 			RuntimeModel.getInstance().getProject()
 					.removeUnreferencedDatatypes();
 			this.updateDatatypeList();
 			RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 					RuntimeModel.DATATYPE_MODEL_CHANGED,
 					"unreferenced datatypes removed");
 		} else if (e.getActionCommand().equals(SELECT_ALL_SERVICES)) {
 			this.mainWindowRef.getMainPane().serviceList.addSelectionInterval(
 					0, this.mainWindowRef.getMainPane().serviceList.getModel()
 							.getSize() - 1);
 		} else if (e.getActionCommand().equals(SHOW_WSDL_EXPORTFRAME)) {
 			if (RuntimeModel.getInstance().getProject() == null) {
 				JOptionPane.showMessageDialog(this.mainWindowRef,
 						SHOW_WSDL_EXPORTFRAME
 								+ "\nError: No Project. No services loaded.",
 						"Export WSDL/OWL-S Error", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			ExportWSDLFrame exportFrame = new ExportWSDLFrame();
 			exportFrame.update(this.mainWindowRef.getMainPane()
 					.getSelectedServicesList());
 			exportFrame.setModal(true);
 			exportFrame.setVisible(true);
 			exportFrame
 					.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		} else if (e.getActionCommand().equals(SHOW_CONFIGURATIONFRAME)) {
 			if (RuntimeModel.getInstance().getProject() != null) {
 				//
 				// CONFIGURATION FRAME
 				//
 				ConfigurationFrame configFrame = new ConfigurationFrame(
 						this.mainWindowRef);
 				configFrame.setModal(true);
 				configFrame.updateContentsFromProjectData();
 				configFrame.setTitle("Configuration for Project "
 						+ RuntimeModel.getInstance().getProject()
 								.getProjectName());
 				configFrame.setVisible(true);
 			} else {
 				ConfigurationFrame configFrame = new ConfigurationFrame(
 						this.mainWindowRef);
 				configFrame.deactivateProjectProperties();
 				configFrame.setModal(true);
 				configFrame.setTitle("Configuration");
 				configFrame.setVisible(true);
 
 				// JOptionPane.showMessageDialog(
 				// this.mainWindowRef,
 				// "Error: No Project.\nFailed to open Configuration frame.",
 				// "Configuration Error",
 				// JOptionPane.ERROR_MESSAGE);
 			}
 		} else if (e.getActionCommand().equals(LOAD_SERVICE_INFORMATION)) {
 			System.out.println("[.] process " + LOAD_SERVICE_INFORMATION);
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("Select XML file.");
 			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			fc.setMultiSelectionEnabled(false);
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(".xml")
 							|| f.isDirectory();
 				}
 
 				public String getDescription() {
 					return "xml file (*.xml)";
 				}
 			});
 			if (OWLS2WSDLSettings.getInstance().containsKey(
 					"PERSISTENT_SERVICE_DIR")) {
 				fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance()
 						.getProperty("PERSISTENT_SERVICE_DIR")));
 			}
 
 			int option = fc.showOpenDialog(this.mainWindowRef);
 			if (option == JFileChooser.APPROVE_OPTION) {
 				File f = fc.getSelectedFile();
 				RuntimeModel.getInstance().getProject()
 						.getAbstractServiceCollection()
 						.importServiceCollection(f);
 				this.mainWindowRef.getMainPane().serviceListModel
 						.syncWithProject(RuntimeModel.getInstance()
 								.getProject());
 				this.mainWindowRef.getMainPane().serviceList.updateUI();
 				RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 						RuntimeModel.SERVICE_MODEL_CHANGED,
 						"Imported File: " + f.getName());
 			}
 			OWLS2WSDLSettings.getInstance().setProperty(
 					"PERSISTENT_SERVICE_DIR",
 					fc.getCurrentDirectory().getAbsolutePath());
 			this.mainWindowRef.getMainPane().setServiceInformationFrame(0);
 			this.mainWindowRef.getMainPane().tabbedPaneLeft.setSelectedIndex(0);
 			this.mainWindowRef.getMainPane().showWSDL2OWLSConverter(false);
 		} else if (e.getActionCommand().equals(LOAD_TYPE_INFORMATION)) {
 			RuntimeModel.getInstance().unsetSelectedDatatype();
 
 			System.out.println("[.] process " + LOAD_TYPE_INFORMATION);
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("Select XML file.");
 			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			fc.setMultiSelectionEnabled(false);
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(".xml")
 							|| f.isDirectory();
 				}
 
 				public String getDescription() {
 					return "xml file (*.xml)";
 				}
 			});
 
 			if (OWLS2WSDLSettings.getInstance().containsKey(
 					"PERSISTENT_DATATYPE_DIR")) {
 				fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance()
 						.getProperty("PERSISTENT_DATATYPE_DIR")));
 				System.out.println("OPEN DIR: "
 						+ fc.getCurrentDirectory().getPath());
 			}
 
 			int option = fc.showOpenDialog(this.mainWindowRef);
 			if (option == JFileChooser.APPROVE_OPTION) {
 				File f = fc.getSelectedFile();
 				// AbstractDatatypeMapper.getInstance().importAbstractDatatypeKB(f);
 				AbstractDatatypeLoadingThread t = new AbstractDatatypeLoadingThread(
 						f);
 				t.setGUIActionListenerRef(this);
 				t.start();
 			}
 			// System.out.println("Current Directory for XML File: "+fc.getCurrentDirectory().getAbsolutePath());
 			OWLS2WSDLSettings.getInstance().setProperty(
 					"PERSISTENT_DATATYPE_DIR",
 					fc.getCurrentDirectory().getAbsolutePath());
 
 			this.mainWindowRef.getMainPane().setServiceInformationFrame(1);
 			this.mainWindowRef.getMainPane().tabbedPaneLeft.setSelectedIndex(1);
 			this.mainWindowRef.getMainPane().showWSDL2OWLSConverter(false);
 		} else if (e.getActionCommand().equals(SAVE_SERVICE_INFORMATION)) {
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("SAVE Service information in XML file.");
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(".xml")
 							|| f.isDirectory();
 				}
 
 				public String getDescription() {
 					return "xml file (*.xml)";
 				}
 			});
 			if (OWLS2WSDLSettings.getInstance().containsKey(
 					"PERSISTENT_SERVICE_DIR")) {
 				fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance()
 						.getProperty("PERSISTENT_SERVICE_DIR")));
 			}
 			int option = fc.showSaveDialog(this.mainWindowRef);
 			if (option == JFileChooser.APPROVE_OPTION) {
 				File f = fc.getSelectedFile();
 				if (!f.getAbsolutePath().endsWith(".xml")) {
 					String filepath = f.getAbsolutePath();
 					f = new File(filepath + ".xml");
 				}
 				System.out.println("Current Directory for XML File: "
 						+ f.getAbsolutePath());
 				try {
 					FileOutputStream ausgabeStream = new FileOutputStream(f);
 					RuntimeModel
 							.getInstance()
 							.getProject()
 							.getAbstractServiceCollection()
 							.marshallCollectedServicesAsXml(ausgabeStream, true);
 				} catch (java.io.FileNotFoundException fnfException) {
 					System.out.println("FileNotFoundException: "
 							+ fnfException.toString());
 				}
 			}
 			OWLS2WSDLSettings.getInstance().setProperty(
 					"PERSISTENT_SERVICE_DIR",
 					fc.getCurrentDirectory().getAbsolutePath());
 		} else if (e.getActionCommand().equals(SAVE_TYPE_INFORMATION)) {
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("SAVE Datatype information in XML file.");
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(".xml")
 							|| f.isDirectory();
 				}
 
 				public String getDescription() {
 					return "xml file (*.xml)";
 				}
 			});
 			if (OWLS2WSDLSettings.getInstance().containsKey(
 					"PERSISTENT_DATATYPE_DIR")) {
 				fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance()
 						.getProperty("PERSISTENT_DATATYPE_DIR")));
 			}
 			int option = fc.showSaveDialog(this.mainWindowRef);
 			if (option == JFileChooser.APPROVE_OPTION) {
 				File f = fc.getSelectedFile();
 				if (!f.getAbsolutePath().endsWith(".xml")) {
 					String filepath = f.getAbsolutePath();
 					f = new File(filepath + ".xml");
 				}
 				System.out.println("Current Directory for XML File: "
 						+ f.getAbsolutePath());
 				try {
 					FileOutputStream ausgabeStream = new FileOutputStream(f);
 					AbstractDatatypeKB.getInstance().marshallAsXML(
 							ausgabeStream, true);
 				} catch (java.io.FileNotFoundException fnfException) {
 					System.out.println("FileNotFoundException: "
 							+ fnfException.toString());
 				}
 			}
 			OWLS2WSDLSettings.getInstance().setProperty(
 					"PERSISTENT_DATATYPE_DIR",
 					fc.getCurrentDirectory().getAbsolutePath());
 		} else if (e.getActionCommand().equals(SAVE_PROJECT)) {
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("SAVE Project (Service + Datatype information)");
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(".xml")
 							|| f.isDirectory();
 				}
 
 				public String getDescription() {
 					return "xml file (*.xml)";
 				}
 			});
 			if (OWLS2WSDLSettings.getInstance().containsKey("PROJECT_DIR")) {
 				fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance()
 						.getProperty("PROJECT_DIR")));
 			}
 			int option = fc.showSaveDialog(this.mainWindowRef);
 			if (option == JFileChooser.APPROVE_OPTION) {
 				File f = fc.getSelectedFile();
 				if (!f.getAbsolutePath().endsWith(".xml")) {
 					String filepath = f.getAbsolutePath();
 					f = new File(filepath + ".xml");
 				}
 				System.out.println("Current Directory for XML File: "
 						+ f.getAbsolutePath());
 				try {
 					RuntimeModel.getInstance().getProject().save(f);
 				} catch (java.io.FileNotFoundException fnfException) {
 					System.out.println("FileNotFoundException: "
 							+ fnfException.toString());
 				}
 			}
 			OWLS2WSDLSettings.getInstance().setProperty("PROJECT_DIR",
 					fc.getCurrentDirectory().getAbsolutePath());
 		} else if (e.getActionCommand().equals(NEW_PROJECT)) {
 			String name = JOptionPane.showInputDialog(this.mainWindowRef,
 					ResourceManager.getString("project.name.new"),
 					"Project Name", JOptionPane.QUESTION_MESSAGE);
 			System.out.println("INPUT: " + name);
 			if (name != null && !name.equals("")) {
 				// if(RuntimeModel.getInstance().getProject() != null) {
 				// RuntimeModel.getInstance().getProject().getAbstractServiceCollection().removeAllElements();
 				// }
 
 				// new information
 				this.mainWindowRef.setTitle("OWLS2WSDL Tool - " + name);
 				Project p = new Project(name);
 				RuntimeModel.getInstance().setProject(p);
 
 				// clear old runtime and gui information
 				this.mainWindowRef.getMainPane().serviceList.clearSelection();
 				this.mainWindowRef.getMainPane().datatypeList.clearSelection();
 
 				if (RuntimeModel.getInstance().getSelectedService() != null) {
 					RuntimeModel.getInstance().unsetSelectedService();
 				}
 				if (RuntimeModel.getInstance().getSelectedDatatype() != null) {
 					RuntimeModel.getInstance().unsetSelectedDatatype();
 				}
 
 				AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData()
 						.removeAllDatatypes();
 
 				// Project muss angelegt sein, ansonsten Probleme bei
 				// getServiceDependencyTypes()
 				RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 						RuntimeModel.DATATYPE_MODEL_CHANGED,
 						"all types removed");
 				RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 						RuntimeModel.SERVICE_MODEL_CHANGED,
 						"all services removed");
 
 				this.updateDatatypeList();
 				// this.updateServiceList(); // obsolete, using Observable
 				// pattern
 				this.mainWindowRef.getMainPane().activatePanel();
 			} else {
 				JOptionPane.showMessageDialog(this.mainWindowRef,
 						ResourceManager.getString("project.name.cancel"),
 						"Attention!", JOptionPane.INFORMATION_MESSAGE);
 			}
 		} else if (e.getActionCommand().equals(SET_PROJECT_NAME)) {
 			String name = JOptionPane.showInputDialog(this.mainWindowRef,
 					ResourceManager.getString("project.name.change"),
 					"Project Name", JOptionPane.QUESTION_MESSAGE);
 			System.out.println("INPUT: " + name);
 			if (name != null && !name.equals("")) {
 				this.mainWindowRef.setTitle("OWLS2WSDL Tool - " + name);
 				RuntimeModel.getInstance().getProject().setProjectName(name);
 			} else {
 				JOptionPane.showMessageDialog(this.mainWindowRef,
 						ResourceManager.getString("project.name.cancel"),
 						"Attention!", JOptionPane.INFORMATION_MESSAGE);
 			}
 		} else if (e.getActionCommand().equals(LOAD_PROJECT)) {
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("Load Project. Select XML file.");
 			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			fc.setMultiSelectionEnabled(false);
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(".xml")
 							|| f.isDirectory();
 				}
 
 				public String getDescription() {
 					return "xml file (*.xml)";
 				}
 			});
 
 			if (OWLS2WSDLSettings.getInstance().containsKey("PROJECT_DIR")) {
 				fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance()
 						.getProperty("PROJECT_DIR")));
 			}
 
 			int option = fc.showOpenDialog(this.mainWindowRef);
 			if (option == JFileChooser.APPROVE_OPTION) {
 				File f = fc.getSelectedFile();
 				System.out.println("STARTING THREAD: " + f.toString());
 				ProjectLoadingThread t = new ProjectLoadingThread(f);
 				t.setGUIActionListenerRef(this);
 				t.start();
 			}
 			System.out.println("Current Directory for XML File: "
 					+ fc.getCurrentDirectory().getAbsolutePath());
 			OWLS2WSDLSettings.getInstance().setProperty("PROJECT_DIR",
 					fc.getCurrentDirectory().getAbsolutePath());
 
 			if (!this.mainWindowRef.getMainPane().panelActivationFlag) {
 				this.mainWindowRef.getMainPane().activatePanel();
 			}
 		} else if (e.getActionCommand().equals(LOAD_OWLS_URL)) {
 			// java.awt.EventQueue.invokeLater(new Runnable() {
 			// public void run() {
 			// }
 			// });
 
 			OpenServiceUrlDialog dl = new OpenServiceUrlDialog(mainWindowRef,
 					"Enter URL of Semantic Web Service", true);
 			dl.setVisible(true);
 			String uri = dl.getReturnVal();
 			dl.dispose();
 
 			if (uri != null) {
 				System.out.println("VAL: " + uri);
 				Project project = RuntimeModel.getInstance().getProject();
 				try {
 					project.importServices(uri);
 					RuntimeModel.getInstance().getProject()
 							.determineAllDependecyTypes();
 					((ServiceListModel) this.mainWindowRef.getMainPane().serviceList
 							.getModel()).syncWithProject(project);
 					RuntimeModel.getInstance().setRuntimeAndNotify(
 							RuntimeModel.SERVICE_MODEL_CHANGED, "URI: " + uri);
 				} catch (Exception exception) {
 					// exception.printStackTrace();
 				}
 			}
 		} else if (e.getActionCommand().equals(LOAD_OWLS_FILES)) {
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("Select one or more OWL-S service definitions");
 			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 			fc.setMultiSelectionEnabled(true);
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(".owls")
 							|| f.getName().toLowerCase().endsWith(".owl")
 							|| f.isDirectory();
 				}
 
 				public String getDescription() {
 					return "OWL-S description (*.owls,*.owl)";
 				}
 			});
 
 			if (OWLS2WSDLSettings.getInstance().containsKey("SERVICE_DIR")) {
 				fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance()
 						.getProperty("SERVICE_DIR")));
 			}
 			int option = fc.showOpenDialog(this.mainWindowRef);
 			if (option == JFileChooser.APPROVE_OPTION) {
 				Project project = RuntimeModel.getInstance().getProject();
 				project.importServices(fc.getSelectedFiles());
 				RuntimeModel.getInstance().getProject()
 						.determineAllDependecyTypes();
 
 				((ServiceListModel) this.mainWindowRef.getMainPane().serviceList
 						.getModel()).syncWithProject(project);
 				RuntimeModel.getInstance().setRuntimeAndNotify(
 						RuntimeModel.SERVICE_MODEL_CHANGED,
 						"Service directory: "
 								+ fc.getCurrentDirectory().getName());
 			}
 			System.out.println("Current Directory: "
 					+ fc.getCurrentDirectory().getAbsolutePath());
 			OWLS2WSDLSettings.getInstance().setProperty("SERVICE_DIR",
 					fc.getCurrentDirectory().getAbsolutePath());
 			OWLS2WSDLSettings.getInstance().addValue("recent_service_dirs",
 					fc.getCurrentDirectory().getAbsolutePath());
 		} else if (e.getActionCommand().equals(IMPORT_ONTOLOGY_URL)) {
 			OpenOntologyUrlDialog dl = new OpenOntologyUrlDialog(mainWindowRef,
 					"Enter URL of Ontology", true);
 			dl.setVisible(true);
 			String url = dl.getReturnVal();
 			dl.dispose();
 
 			if (url != null) {
 				System.out.println("URL -VAL: " + url);
 				Project project = RuntimeModel.getInstance().getProject();
 				try {
 					project.importDatatypes(url);
 					RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 							RuntimeModel.DATATYPE_MODEL_CHANGED,
 							url + " parsed.");
 					this.mainWindowRef.getMainPane().datatypeListModel
 							.updateModel();
 					this.mainWindowRef.getMainPane().datatypeList.updateUI();
 				} catch (Exception exception) {
 					exception.printStackTrace();
 				}
 			}
 		} else if (e.getActionCommand().equals(IMPORT_ONTOLOGY_OWL_FILE)) {
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("Select a definition (OWL file)");
 			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			fc.setMultiSelectionEnabled(false);
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(".owl")
 							|| f.isDirectory();
 				}
 
 				public String getDescription() {
 					return "ontology file (*.owl)";
 				}
 			});
 
 			if (OWLS2WSDLSettings.getInstance().containsKey("ONTOLOGY_DIR")) {
 				fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance()
 						.getProperty("ONTOLOGY_DIR")));
 			}
 			// if(RuntimeModel.getInstance().hasStatus("CURRENT_ONTOLOGY_DIR"))
 			// {
 			// fc.setCurrentDirectory(new
 			// File(RuntimeModel.getInstance().getStatus("CURRENT_ONTOLOGY_DIR")));
 			// }
 
 			int option = fc.showOpenDialog(this.mainWindowRef);
 			if (option == JFileChooser.APPROVE_OPTION) {
 				File f = fc.getSelectedFile();
 				System.out.println("LOADED /URI: " + f.toURI());
 
 				RuntimeModel.getInstance().project.importDatatypes(f);
 				RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 						RuntimeModel.DATATYPE_MODEL_CHANGED,
 						f.getName() + " parsed.");
 				this.mainWindowRef.getMainPane().datatypeListModel
 						.updateModel();
 				this.mainWindowRef.getMainPane().datatypeList.updateUI();
 				this.mainWindowRef.validate();
 			}
 			System.out.println("Current Directory: "
 					+ fc.getCurrentDirectory().getAbsolutePath());
 			// RuntimeModel.getInstance().setStatus("CURRENT_ONTOLOGY_DIR",
 			// fc.getCurrentDirectory().getAbsolutePath());
 			OWLS2WSDLSettings.getInstance().setProperty("ONTOLOGY_DIR",
 					fc.getCurrentDirectory().getAbsolutePath());
 		} else if (e.getActionCommand().equals(SHOW_SERVICEDETAILS)) {
 			this.mainWindowRef.getMainPane().setServiceInformationFrame(0);
 			this.mainWindowRef.getMainPane().showWSDL2OWLSConverter(false);
 		} else if (e.getActionCommand().equals(SHOW_DATATYPEDETAILS)) {
 			this.mainWindowRef.getMainPane().setServiceInformationFrame(1);
 			this.mainWindowRef.getMainPane().showWSDL2OWLSConverter(false);
 		} else if (e.getActionCommand().equals(SHOW_PROJECTDETAILS)) {
 			if (RuntimeModel.getInstance().getProject() != null) {
 				this.mainWindowRef.getMainPane().setServiceInformationFrame(2);
 				this.mainWindowRef.getMainPane().showWSDL2OWLSConverter(false);
 			} else {
 				JOptionPane.showMessageDialog(this.mainWindowRef,
 						"No project loaded.", "Show Project Details Error",
 						JOptionPane.ERROR_MESSAGE);
 			}
 		} else if (e.getActionCommand().equals(SHOW_WSDL2OWLS_CONVERTER)) {
 			this.mainWindowRef.getMainPane().showWSDL2OWLSConverter(true);
 		} else if (e.getActionCommand().equals(SHOW_HELP_CONTENTS)) {
 			showHelpContents();
 		} else if (e.getActionCommand().equals(SHOW_ABOUT)) {
 			showAbout();
 		} else if (e.getActionCommand().equals(CLOSE)) {
 			close();
 		} else if (e.getActionCommand().equals(GENERATE_XSD)) {
 			if (TranslationUtils.isValidNCName(RuntimeModel.getInstance()
 					.getSelectedDatatype().getUrl())) {
 				this.mainWindowRef.outputPanel.addOutput(RuntimeModel
 						.getInstance().getSelectedDatatype());
 			} else {
 				JOptionPane.showMessageDialog(this.mainWindowRef, RuntimeModel
 						.getInstance().getSelectedDatatype().getLocalName()
 						+ " is not a valid NCName.", "Error XSD generation",
 						JOptionPane.WARNING_MESSAGE);
 			}
 		} else if (e.getActionCommand().equals(CHECK_OWLS_PARAM)) {
 			this.checkServiceParameterTypes();
 		} else if (e.getActionCommand().equals(GENERATE_WSDL)) {
 			this.mainWindowRef.outputPanel.addOutput(RuntimeModel.getInstance()
 					.getSelectedService());
 		} else if (e.getActionCommand().equals(GENERATE_OWLS)) {
 			this.mainWindowRef.outputPanel.addOWLSOutput(RuntimeModel
 					.getInstance().getSelectedService());
 		} else if (e.getActionCommand().equals(
 				DETERMINE_SERVICE_DEPENDENCY_TYPES)) {
 			RuntimeModel.getInstance().getProject()
 					.determineAllDependecyTypes();
 			RuntimeModel.getInstance().setRuntimeAndNotify(
 					RuntimeModel.DATATYPE_MODEL_CHANGED);
 		} else if (e.getActionCommand().equals(UPDATE_OVERVIEWS)) {
 			this.updateDatatypeList();
 			// this.updateServiceList();
 			RuntimeModel.getInstance().getProject()
 					.determineAllDependecyTypes();
 			RuntimeModel.getInstance().setRuntimeAndNotify(
 					RuntimeModel.SERVICE_MODEL_CHANGED);
 		} else if (e.getActionCommand().equals(SAVE_OUTPUT_PANEL_SEL)) {
 			File outputdir = this
 					.showDirectoryChooser(JFileChooser.SAVE_DIALOG);
 			if (outputdir != null)
 				this.mainWindowRef.getMainPane().outputPanel
 						.saveSelectedTab(outputdir);
 		} else if (e.getActionCommand().equals(SAVE_OUTPUT_PANEL)) {
 			File outputdir = this
 					.showDirectoryChooser(JFileChooser.SAVE_DIALOG);
 			if (outputdir != null)
 				this.mainWindowRef.getMainPane().outputPanel
 						.saveAllTabs(outputdir);
 		} else if (e.getActionCommand().equals(WSDL2JAVA)) {
 			if (RuntimeModel.getInstance().getSelectedService() != null) {
 				if (RuntimeModel.getInstance().hasStatus(
 						RuntimeModel.MULTIPLE_SERVICES_SELECTED)) {
 					JOptionPane.showMessageDialog(this.mainWindowRef,
 							RuntimeModel.MULTIPLE_SERVICES_SELECTED,
 							"WSDL2Java", JOptionPane.WARNING_MESSAGE);
 				} else {
 					// int cid = JOptionPane.showConfirmDialog(
 					// this.mainWindowRef,
 					// "Create stubs for service: "+RuntimeModel.getInstance().getSelectedService().getName(),
 					// "WSDL2Java",
 					// JOptionPane.YES_NO_OPTION);
 
 					String ret = (String) JOptionPane
 							.showInputDialog(this.mainWindowRef,
 									"Choose your way to generate stubs for service: "
 											+ RuntimeModel.getInstance()
 													.getSelectedService()
 													.getName(), "WSDL2Java",
 									JOptionPane.QUESTION_MESSAGE, null,
 									WSDL2JavaWrapper.genops,
 									WSDL2JavaWrapper.genops[0]);
 
 					System.out
 							.println("[GUIActionListener] Gen Dialog: " + ret);
 					WSDL2JavaWrapper w = new WSDL2JavaWrapper(ret,
 							this.mainWindowRef);
 					new Thread(w).start();
 				}
 			} else {
 				JOptionPane.showMessageDialog(this.mainWindowRef,
 						"No Service selected!", "WSDL2Java",
 						JOptionPane.WARNING_MESSAGE);
 			}
 		}
 	}
 
 	// private File showFileChooser(int DIALOG_TYPE) {
 	// File file = null;
 	// JFileChooser fc = new JFileChooser();
 	// fc.setDialogType(DIALOG_TYPE);
 	// fc.setFileFilter( new FileFilter() {
 	// public boolean accept( File f ) {
 	// return f.isDirectory() || f.getName().toLowerCase().endsWith( ".xml" );
 	// }
 	// public String getDescription() {
 	// return "Texte";
 	// }
 	// } );
 	//
 	// int state = -1;
 	// if(DIALOG_TYPE == JFileChooser.SAVE_DIALOG) {
 	// state = fc.showSaveDialog(null);
 	// }
 	// else {
 	// state = fc.showOpenDialog(null);
 	// }
 	//
 	// if ( state == JFileChooser.APPROVE_OPTION ) {
 	// file = fc.getSelectedFile();
 	// System.out.println( file.getAbsolutePath() );
 	// }
 	// else
 	// System.out.println( "Auswahl abgebrochen" );
 	// return file;
 	// }
 
 	private File showDirectoryChooser(int DIALOG_TYPE) {
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogType(DIALOG_TYPE);
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		fc.setMultiSelectionEnabled(false);
 		fc.setFileFilter(new FileFilter() {
 			public boolean accept(File f) {
 				return f.isDirectory();
 			}
 
 			public String getDescription() {
 				return "directory";
 			}
 		});
 
 		int returnVal = -1;
 		if (DIALOG_TYPE == JFileChooser.SAVE_DIALOG) {
 			returnVal = fc.showSaveDialog(null);
 		} else {
 			returnVal = fc.showOpenDialog(null);
 		}
 
 		if (returnVal != JFileChooser.APPROVE_OPTION)
 			return null;
 
 		File selectedDirectory = fc.getSelectedFile();
 
 		if (!selectedDirectory.exists()) {
 			JOptionPane.showMessageDialog(null,
 					selectedDirectory.getAbsolutePath() + "does not exist!",
 					"Error", JOptionPane.ERROR_MESSAGE);
 		}
 		return selectedDirectory;
 	}
 
 	// private final class OpenFileActionListener implements ActionListener {
 	// public void actionPerformed(ActionEvent e) {
 	// new JFileChooser().showOpenDialog(OWLS2WSDLGui.this);
 	// }
 	// }
 
 	private void showHelpContents() {
 		// http://owls2wsdl.googlecode.com/svn/trunk/handbook/index.html
 		String url = ResourceManager.getString("application.handbook.url");
 		String handbookPicList = ResourceManager
 				.getString("application.handbook.pics");
 		String path2Icon = ResourceManager.getString("application.icon");
 		try {
 			HandbookEnvBuilder heb = new HandbookEnvBuilder(url,
 					handbookPicList);
 			// As soon I can render html pages in svn or the handbook is online
 			// somewhere
 			// the HanbbookEnvBuilder class is obsolete!
 			System.out.println("[GUIActionListener] open handbook in "
 					+ heb.getHandbookIndexURL().toString());
 			HandbookFrame helpWindow = new HandbookFrame(heb
 					.getHandbookIndexURL().toString(), path2Icon);
 			helpWindow.setVisible(true);
 		} catch (MalformedURLException murle) {
 			System.err
 					.println("[GUIActionListener] showHelpContents, MalformedURLException: "
 							+ murle.getMessage());
 
 		} catch (IOException ioe) {
 			System.err
 					.println("[GUIActionListener] showHelpContents, IOException: "
 							+ ioe.getMessage());
 		} catch (Exception e) {
 			JOptionPane
 					.showMessageDialog(mainWindowRef,
 							"Problems in synchronizing handbook from\n"
 									+ "URL: " + url, "OWLS2WSDL HANDBOOK",
 							JOptionPane.INFORMATION_MESSAGE);
 			e.printStackTrace();
 		}
 	}
 
 	private void showAbout() {
 
 		// JOptionPane.showMessageDialog(
 		// mainWindowRef,
 		// ResourceManager.getString("application.title")
 		// + ", Release: "+ResourceManager.getString("application.release")
 		// + "\n\n"
 		// + ResourceManager.getString("application.copyright")
 		// + "\n\n",
 		// "About",
 		// JOptionPane.INFORMATION_MESSAGE);
 
 		SplashScreen.splash(mainWindowRef,
 				ResourceManager.getString("application.release"));
 		SplashScreen.hideSplash();
 	}
 
 	/** Programm beenden + confirm question */
 	private void close() {
 		JOptionPane optionPane = new JOptionPane("<html><center><b>"
 				+ ResourceManager.getString("closing.message")
 				+ "</b></center></html>", JOptionPane.WARNING_MESSAGE,
 				JOptionPane.YES_NO_OPTION, iconExit32x32, new String[] {
 						ResourceManager.getString("closing.yes"),
 						ResourceManager.getString("closing.no") });
 
 		JDialog dialog = optionPane.createDialog(mainWindowRef,
 				ResourceManager.getString("closing.title"));
 		dialog.setVisible(true);
 
 		System.out.println("optionPane: " + optionPane.getValue().toString());
 
 		if (optionPane.getValue().equals(
 				ResourceManager.getString("closing.yes"))) {
 			OWLS2WSDLSettings.getInstance().savePropertyFile();
 			dialog.dispose();
 			System.exit(0);
 		} else {
 			dialog.dispose();
 		}
 
 	}
 
 	// =========================================================================
 	// core functionality
 	private void checkServiceParameterTypes() {
 		for (Iterator<AbstractServiceParameter> it = RuntimeModel.getInstance()
 				.getSelectedService().getAllInputParameter().iterator(); it
 				.hasNext();) {
 			AbstractServiceParameter param = it.next();
 			if (AbstractDatatypeKB.getInstance().data.containsKey(param
 					.getUri())) {
 				System.out.println("[ok] " + param.getUri());
 			} else {
 				System.out.println("[er] " + param.getUri());
 				if (param.getUri().equals(
 						"http://www.w3.org/2001/XMLSchema#string")) {
 					System.out.println("[ok] " + param.getUri());
 				}
 			}
 		}
 		for (Iterator<AbstractServiceParameter> it = RuntimeModel.getInstance()
 				.getSelectedService().getAllOutputParameter().iterator(); it
 				.hasNext();) {
 			AbstractServiceParameter param = it.next();
 			if (AbstractDatatypeKB.getInstance().data.containsKey(param
 					.getUri())) {
 				System.out.println("[ok] " + param.getUri());
 			} else {
 				System.out.println("[er] " + param.getUri());
 			}
 		}
 	}
 
 	public void updateDatatypeList() {
 		System.out.println("[GUIListener] updateDatatypeList");
 		((AbstractDatatypeListModel) this.mainWindowRef.getMainPane().datatypeList
 				.getModel()).updateModel();
 		this.mainWindowRef.getMainPane().datatypeList.setVisible(true);
 		this.mainWindowRef.getMainPane().datatypeList.revalidate();
 		this.mainWindowRef.getMainPane().datatypeList.updateUI();
 		this.mainWindowRef.repaint();
 	}
 
 	// public void updateServiceList() {
 	// //((ServiceListModel)this.mainWindowRef.getMainPane().serviceList.getModel()).syncWithProject(p);
 	// this.mainWindowRef.getMainPane().serviceListModel.updateModel();
 	// this.mainWindowRef.getMainPane().serviceList.revalidate();
 	// this.mainWindowRef.getMainPane().serviceList.updateUI();
 	// RuntimeModel.getInstance().setStatusAndNotify("RUNTIME",
 	// RuntimeModel.SERVICE_MODEL_CHANGED);
 	// }
 
 	public OWLS2WSDLGui getMainWindowRef() {
 		return this.mainWindowRef;
 	}
 
 	public void showProjectLoadedMessage(String projectName) {
 		JOptionPane.showMessageDialog(this.mainWindowRef, "Project "
 				+ projectName + " loaded.", "ProjectManager",
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	// =========================================================================
 	// WINDOW LISTENER
 
 	public void windowOpened(WindowEvent windowEvent) {
 	}
 
 	public void windowIconified(WindowEvent windowEvent) {
 	}
 
 	public void windowDeiconified(WindowEvent windowEvent) {
 	}
 
 	public void windowDeactivated(WindowEvent windowEvent) {
 	}
 
 	public void windowClosing(WindowEvent windowEvent) {
 		this.close();
 	}
 
 	public void windowClosed(WindowEvent windowEvent) {
 		this.close();
 	}
 
 	public void windowActivated(WindowEvent windowEvent) {
 	}
 
 }
