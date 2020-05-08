 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
 * $Revision: 1.78 $
 * $Date: 2007-08-14 19:20:56 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
  */
 package org.concord.otrunk.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Hashtable;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 
 import org.concord.applesupport.AppleApplicationAdapter;
 import org.concord.applesupport.AppleApplicationUtil;
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeListener;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.view.OTFrameManager;
 import org.concord.framework.otrunk.view.OTJComponentServiceFactory;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.framework.otrunk.view.OTViewContainerListener;
 import org.concord.framework.otrunk.view.OTViewContext;
 import org.concord.framework.otrunk.view.OTViewFactory;
 import org.concord.framework.text.UserMessageHandler;
 import org.concord.framework.util.SimpleTreeNode;
 import org.concord.otrunk.OTMLToXHTMLConverter;
 import org.concord.otrunk.OTObjectServiceImpl;
 import org.concord.otrunk.OTStateRoot;
 import org.concord.otrunk.OTSystem;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.xml.Exporter;
 import org.concord.otrunk.xml.ExporterJDOM;
 import org.concord.otrunk.xml.XMLDatabase;
 import org.concord.swing.CustomDialog;
 import org.concord.swing.MostRecentFileDialog;
 import org.concord.swing.StreamRecord;
 import org.concord.swing.StreamRecordView;
 import org.concord.swing.util.Util;
 import org.concord.view.SimpleTreeModel;
 import org.concord.view.SwingUserMessageHandler;
 
 /**
  * OTViewer Class name and description
  * 
  * Date created: Dec 14, 2004
  * 
  * @author scott
  *         <p>
  * 
  */
 public class OTViewer extends JFrame
     implements TreeSelectionListener, OTViewContainerListener,
     AppleApplicationAdapter
 {
 	/**
 	 * first version of this class
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public final static String TITLE_PROP = "otrunk.view.frame_title";
 
 	public final static String HIDE_TREE_PROP = "otrunk.view.hide_tree";
 
 	public final static String HTTP_PUT = "PUT";
 
 	public final static String HTTP_POST = "POST";
 
 	private static OTrunkImpl otrunk;
 
 	private static OTViewFactory otViewFactory;
 
 	protected int userMode = 0;
 
 	OTUserObject currentUser = null;
 
 	URL currentURL = null;
 
 	String baseFrameTitle = "OTrunk Viewer";
 
 	OTViewContainerPanel bodyPanel;
 
 	OTFrameManagerImpl frameManager;
 
 	JTree folderTreeArea;
 
 	SimpleTreeModel folderTreeModel;
 
 	JTree dataTreeArea;
 
 	SimpleTreeModel dataTreeModel;
 
 	JSplitPane splitPane;
 
 	JFrame consoleFrame;
 
 	// Temp, to close the window
 	AbstractAction exitAction;
 
 	AbstractAction saveAsAction;
 
 	JMenuBar menuBar;
 
 	XMLDatabase xmlDB;
 
 	XMLDatabase userDataDB;
 
 	File currentAuthoredFile = null;
 
 	File currentUserFile = null;
 
 	Hashtable otContainers = new Hashtable();
 
 	String startupMessage = "";
 
 	boolean justStarted = false;
 
 	boolean showTree = false;
 
 	URL remoteURL;
 
 	private AbstractAction saveUserDataAsAction;
 
 	private AbstractAction saveUserDataAction;
 
 	private AbstractAction debugAction;
 
 	private AbstractAction showConsoleAction;
 
 	private AbstractAction newUserDataAction;
 
 	private AbstractAction loadUserDataAction;
 
 	private AbstractAction loadAction;
 
 	private AbstractAction reloadWindowAction;
 
 	private AbstractAction saveAction;
 
 	private AbstractAction saveRemoteAsAction;
 
 	private AbstractAction exportImageAction;
 
 	private AbstractAction exportHiResImageAction;
 
 	private AbstractAction exportToHtmlAction;
 
 	private JDialog commDialog;
 
 	/**
 	 * This is true if the user was asked about saving user data after they
 	 * initiated a close of the current view.
 	 */
 	private boolean askedAboutSavingUserData = false;
 
 	/**
 	 * This is true if the user was asked about saving the user data, and said
 	 * yes
 	 */
 	private boolean needToSaveUserData = false;
 
 	private boolean useScrollPane;
 
 	private OTChangeListener systemChangeListener;
 
 	private OTSystem userSystem;
 
 	public static void setOTViewFactory(OTViewFactory factory)
 	{
 		otViewFactory = factory;
 	}
 
 	public OTViewer(boolean showTree)
 	{
 		super();
 
 		this.showTree = true;
 
 		AppleApplicationUtil.registerWithMacOSX(this);
 
 		try {
 			// this overrides the default base frame title
 			String title = System.getProperty(TITLE_PROP, null);
 			if (title != null) {
 				baseFrameTitle = title;
 			}
 		} catch (Throwable t) {
 			// do nothing, just use the default title
 		}
 
 		setTitle(baseFrameTitle);
 
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e)
 			{
 				exitAction.actionPerformed(null);
 			}
 		});
 
 		consoleFrame = new JFrame("Console");
 		StreamRecord record = new StreamRecord(10000);
 		StreamRecordView view = new StreamRecordView(record);
 		consoleFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		System
 		        .setOut((PrintStream) view.addOutputStream(System.out,
 		                "Console"));
 		System.setErr((PrintStream) view
 		        .addOutputStream(System.err, System.out));
 
 		consoleFrame.getContentPane().add(view);
 		consoleFrame.setSize(800, 600);
 
 		commDialog = new JDialog(this, true);
 	}
 
 	public void setUserMode(int mode)
 	{
 		userMode = mode;
 	}
 
 	public void updateTreePane()
 	{
 		Dimension minimumSize = new Dimension(100, 50);
 		folderTreeArea = new JTree(folderTreeModel);
 
 		// we are just disabling this however if we want to
 		// use this tree for authoring, or for managing student
 		// created objects this will need to be some form of option
 		folderTreeArea.setEditable(false);
 		folderTreeArea.addTreeSelectionListener(this);
 
 		JComponent leftComponent = null;
 
 		JScrollPane folderTreeScrollPane = new JScrollPane(folderTreeArea);
 
 		if (System.getProperty(OTViewerHelper.DEBUG_PROP, "").equals("true")) {
 			// ViewFactory.getComponent(root);
 
 			dataTreeArea = new JTree(dataTreeModel);
 			dataTreeArea.setEditable(false);
 			dataTreeArea.addTreeSelectionListener(this);
 
 			JScrollPane dataTreeScrollPane = new JScrollPane(dataTreeArea);
 
 			JTabbedPane tabbedPane = new JTabbedPane();
 			tabbedPane.add("Folders", folderTreeScrollPane);
 			tabbedPane.add("Resources", dataTreeScrollPane);
 
 			// Provide minimum sizes for the two components in the split pane
 			folderTreeScrollPane.setMinimumSize(minimumSize);
 			dataTreeScrollPane.setMinimumSize(minimumSize);
 			tabbedPane.setMinimumSize(minimumSize);
 
 			leftComponent = tabbedPane;
 		} else {
 			leftComponent = folderTreeScrollPane;
 		}
 
 		if (splitPane == null) {
 			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 			        leftComponent, bodyPanel);
 		} else {
 			splitPane.setLeftComponent(leftComponent);
 		}
 
 		splitPane.setOneTouchExpandable(true);
 		splitPane.setDividerLocation(200);
 
 	}
 
 	public void initArgs(String[] args)
 	{
 		if (args.length > 0) {
 			String urlStr = null;
 
 			if (args[0].equals("-f")) {
 				if (args.length > 1) {
 					File inFile = new File(args[1]);
 					currentAuthoredFile = inFile;
 					try {
 						URL url = inFile.toURL();
 						urlStr = url.toString();
 					} catch (Exception e) {
 						e.printStackTrace();
 						urlStr = null;
 					}
 				}
 			} else if (args[0].equals("-r")) {
 				if (args.length > 1) {
 					ClassLoader cl = OTViewer.class.getClassLoader();
 					URL url = cl.getResource(args[1]);
 					urlStr = url.toString();
 				}
 			} else {
 				urlStr = args[0];
 			}
 
 			initWithWizard(urlStr);
 		} else {
 			initWithWizard(null);
 		}
 
 	}
 
 	public void init(String url)
 	{
 		updateRemoteURL(url);
 
 		createActions();
 
 		updateMenuBar();
 
 		setJMenuBar(menuBar);
 
 		frameManager = new OTFrameManagerImpl();
 		bodyPanel = new OTViewContainerPanel(frameManager);
 
 		bodyPanel.addViewContainerListener(this);
 
 		if (showTree) {
 
 			dataTreeModel = new SimpleTreeModel();
 
 			folderTreeModel = new SimpleTreeModel();
 
 			updateTreePane();
 
 			getContentPane().add(splitPane);
 		} else {
 			getContentPane().add(bodyPanel);
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run()
 			{
 
 				Dimension screenSize = Toolkit.getDefaultToolkit()
 				        .getScreenSize();
 				if (screenSize.width < 1000 || screenSize.height < 700) {
 					setVisible(true);
 					int state = getExtendedState();
 
 					// Set the maximized bits
 					state |= Frame.MAXIMIZED_BOTH;
 
 					// Maximize the frame
 					setExtendedState(state);
 				} else {
 					int cornerX = 100;
 					int cornerY = 100;
 					int sizeX = 725;
 					int sizeY = 500;
 
 					// OTViewService viewService = otViewFactory.
 
 					setBounds(cornerX, cornerY, cornerX + sizeX, cornerY
 					        + sizeY);
 					setVisible(true);
 				}
 
 			}
 		});
 
 		if (url == null) {
 			return;
 		}
 
 		try {
 	        initializeURL(new URL(url));
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (Exception e) {
 			// FIXME: this should popup a dialog
 			System.err.println("Can't load url");
 			e.printStackTrace();
 			return;
 		}
 	}
 
 	public void initializeURL(URL url) throws Exception
 	{
 		loadURL(url);
 
 		OTMainFrame mainFrame = (OTMainFrame) otrunk
 		.getService(OTMainFrame.class);
 
 		if (!mainFrame.getShowLeftPanel()) {
 			splitPane.getLeftComponent().setVisible(false);
 		}
 
 		useScrollPane = true;
 		if (mainFrame.getFrame() != null) {
 			if (mainFrame.getFrame().isResourceSet("width")
 					&& mainFrame.getFrame().isResourceSet("height")) {
 				int cornerX = 100;
 				int cornerY = 100;
 				int sizeX = mainFrame.getFrame().getWidth();
 				int sizeY = mainFrame.getFrame().getHeight();
 
 				setBounds(cornerX, cornerY, cornerX + sizeX, cornerY
 						+ sizeY);
 				repaint();
 			}
 			useScrollPane = mainFrame.getFrame().getUseScrollPane();
 		}
 
 		bodyPanel.setUseScrollPane(useScrollPane);
 
 		setupBodyPanel();
 	}
 	
 	public void initWithWizard(String url)
 	{
 		justStarted = true;
 
 		init(url);
 
 		if (userMode == OTViewerHelper.SINGLE_USER_MODE) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run()
 				{
 					instructionPanel();
 				}
 			});
 		}
 	}
 
 	public void loadUserDataFile(File file)
 	{
 		currentUserFile = file;
 		try {
 			loadUserDataURL(file.toURL(), file.getName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void loadUserDataURL(URL url, String name)
 	    throws Exception
 	{
 		XMLDatabase db = new XMLDatabase(url);
 		db.loadObjects();
 		loadUserDataDb(db, name);
 	}
 
 	public void loadUserDataDb(XMLDatabase db, String name)
 	    throws Exception
 	{
 		userDataDB = db;
 		currentUser = otrunk.registerUserDataDatabase(userDataDB, name);
 
 		reloadWindow();
 	}
 
 	public void reloadOverlays()
 	{
 		try {
 			otrunk.reloadOverlays(currentUser, userDataDB);
 			reloadWindow();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void loadFile(File file)
 	{
 		currentAuthoredFile = file;
 		try {
 			initializeURL(currentAuthoredFile.toURL());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void loadURL(URL url)
 	    throws Exception
 	{
 		try {
 			xmlDB = new XMLDatabase(url, System.err);
 			if (Boolean.getBoolean(OTViewerHelper.AUTHOR_PROP)) {
 				xmlDB.setTrackResourceInfo(true);
 			}
 			xmlDB.loadObjects();
 		} catch (org.jdom.input.JDOMParseException e) {
 			String xmlWarningTitle = "XML Decoding error";
 			String xmlWarningMessage = "There appears to a problem parsing the XML of this document. \n"
 			        + "Please show this error message to one of the workshop leaders. \n\n"
 			        + e.getMessage();
 			JOptionPane.showMessageDialog(null, xmlWarningMessage,
 			        xmlWarningTitle, JOptionPane.ERROR_MESSAGE);
 			throw e;
 		}
 
 		otrunk = new OTrunkImpl(xmlDB,
 		        new Object[] { new SwingUserMessageHandler(this) },
 		        new Class[] { UserMessageHandler.class });
 
 		OTViewFactory myViewFactory = (OTViewFactory) otrunk
 		        .getService(OTViewFactory.class);
 
 		if (myViewFactory != null) {
 			otViewFactory = myViewFactory;
 		}
 
 		OTViewContext factoryContext = otViewFactory.getViewContext();
 		factoryContext.addViewService(OTrunk.class, otrunk);
 		factoryContext.addViewService(OTFrameManager.class, frameManager);
 		factoryContext.addViewService(OTJComponentServiceFactory.class,
 		        new OTJComponentServiceFactoryImpl());
 
 		currentURL = url;
 	}
 
 	// This method was refactored out of loadURL
 	private void setupBodyPanel()
 	    throws Exception
 	{
 		bodyPanel.setTopLevelContainer(true);
 
 		bodyPanel.setOTViewFactory(otViewFactory);
 
 		// set the current mode from the viewservice to the main bodyPanel
 		bodyPanel.setViewMode(otViewFactory.getDefaultMode());
 
 		// set the viewFactory of the frame manager
 		frameManager.setViewFactory(otViewFactory);
 
 		xmlDB.setDirty(false);
 
 		reloadWindow();
 	}
 
 	private void reloadWindow()
 	    throws Exception
 	{
 		OTObject root = null;
 		boolean overrideShowTree = false;
 
 		switch (userMode) {
 		case OTViewerHelper.NO_USER_MODE:
 			root = otrunk.getRoot();
 			break;
 		case OTViewerHelper.SINGLE_USER_MODE:
 			if (userDataDB == null) {
 				// FIXME This is an error
 				// the newAnonUserData should have been called before this
 				// method is
 				// called
 				// no user file has been started yet
 				overrideShowTree = true;
 
 				root = null;
 			} else {
 				OTObject otRoot = otrunk.getRoot();
 				root = otrunk.getUserRuntimeObject(otRoot, currentUser);
 
 				OTObject realRoot = otrunk.getRealRoot();
 				if (realRoot instanceof OTSystem) {
 
 					OTSystem localUserSystem = (OTSystem) otrunk
 					        .getUserRuntimeObject(realRoot, currentUser);
 
 					// FIXME there should be a better way than this because we
 					// have to handle
 					// multiple users.
 					if (localUserSystem != userSystem) {
 						userSystem = localUserSystem;
 
 						systemChangeListener = new OTChangeListener() {
 
 							public void stateChanged(OTChangeEvent e)
 							{
 								if ("overlays".equals(e.getProperty())) {
 									reloadOverlays();
 								}
 							}
 						};
 
 						userSystem.addOTChangeListener(systemChangeListener);
 					}
 				}
 			}
 		}
 
 		if (showTree && !overrideShowTree) {
 			OTDataObject rootDataObject = xmlDB.getRoot();
 			dataTreeModel.setRoot(new OTDataObjectNode("root", rootDataObject,
 			        otrunk));
 
 			folderTreeModel.setRoot(new OTFolderNode(root));
 		}
 
 		bodyPanel.setCurrentObject(root);
 
 		if (showTree && !overrideShowTree) {
 			folderTreeModel
 			        .fireTreeStructureChanged((SimpleTreeNode) folderTreeModel
 			                .getRoot());
 			dataTreeModel
 			        .fireTreeStructureChanged((SimpleTreeNode) dataTreeModel
 			                .getRoot());
 		}
 
 		Frame frame = (Frame) SwingUtilities.getRoot(this);
 
 		switch (userMode) {
 		case OTViewerHelper.NO_USER_MODE:
 			if (remoteURL != null) {
 				frame.setTitle(baseFrameTitle + ": " + remoteURL.toString());
 			} else {
 				frame.setTitle(baseFrameTitle + ": " + currentURL.toString());
 			}
 			break;
 		case OTViewerHelper.SINGLE_USER_MODE:
 			if (currentUserFile != null) {
 				frame.setTitle(baseFrameTitle + ": "
 				        + currentUserFile.toString());
 			} else if (System.getProperty(TITLE_PROP, null) != null) {
 				frame.setTitle(baseFrameTitle);
 			} else if (userDataDB != null) {
 				frame.setTitle(baseFrameTitle + ": Untitled");
 			} else {
 				frame.setTitle(baseFrameTitle);
 			}
 			break;
 		}
 
 		saveUserDataAction.setEnabled(userDataDB != null);
 		saveUserDataAsAction.setEnabled(userDataDB != null);
 	}
 
 	public void reload()
 	    throws Exception
 	{
 		initializeURL(currentURL);
 	}
 
 	public OTDatabase getUserDataDb()
 	{
 		return userDataDB;
 	}
 
 	/**
 	 * You should call reloadWindow after calling this method to make sure the
 	 * display reflects this change
 	 * 
 	 * @param userObject
 	 */
 	public void setCurrentUser(OTUserObject userObject)
 	{
 		currentUser = userObject;
 	}
 
 	public static void main(String[] args)
 	{
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 
 		OTViewer viewer = new OTViewer(!Boolean.getBoolean(HIDE_TREE_PROP));
 
 		if (Boolean.getBoolean(OTViewerHelper.SINGLE_USER_PROP)) {
 			viewer.setUserMode(OTViewerHelper.SINGLE_USER_MODE);
 		} else if (Boolean.getBoolean(OTViewerHelper.NO_USER_PROP)) {
 			viewer.setUserMode(OTViewerHelper.NO_USER_MODE);
 		}
 
 		viewer.initArgs(args);
 	}
 
 	class ExitAction extends AbstractAction
 	{
 		/**
 		 * nothing to serialize here.
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public ExitAction()
 		{
 			super("Exit");
 		}
 
 		public void actionPerformed(ActionEvent e)
 		{
 			// If this suceeds then the VM will exit so
 			// the window will get disposed
 			exit();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.otrunk.view.OTViewContainerListener#currentObjectChanged(org.concord.framework.otrunk.view.OTViewContainer)
 	 */
 	public void currentObjectChanged(OTViewContainer container)
 	{
 		final OTViewContainer myContainer = container;
 
 		// TODO Auto-generated method stub
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run()
 			{
 				OTObject currentObject = myContainer.getCurrentObject();
 				if (folderTreeArea != null) {
 					OTFolderNode node = (OTFolderNode) folderTreeArea
 					        .getLastSelectedPathComponent();
 					if (node == null)
 						return;
 					if (node.getPfObject() != currentObject) {
 						folderTreeArea.setSelectionPath(null);
 					}
 				}
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
 	 */
 	public void valueChanged(TreeSelectionEvent event)
 	{
 		if (event.getSource() == folderTreeArea) {
 			OTFolderNode node = (OTFolderNode) folderTreeArea
 			        .getLastSelectedPathComponent();
 
 			if (node == null)
 				return;
 
 			OTObject pfObject = node.getPfObject();
 
 			bodyPanel.setCurrentObject(pfObject);
 
 			if (splitPane.getRightComponent() != bodyPanel) {
 				splitPane.setRightComponent(bodyPanel);
 			}
 		} else if (event.getSource() == dataTreeArea) {
 			SimpleTreeNode node = (SimpleTreeNode) dataTreeArea
 			        .getLastSelectedPathComponent();
 			Object resourceValue = null;
 			if (node != null) {
 				resourceValue = node.getObject();
 				if (resourceValue == null) {
 					resourceValue = "null resource value";
 				}
 			} else {
 				resourceValue = "no selected data object";
 			}
 
 			JComponent nodeView = null;
 			if (resourceValue instanceof OTDataObject) {
 				nodeView = new OTDataObjectView((OTDataObject) resourceValue);
 			} else {
 				nodeView = new JTextArea(resourceValue.toString());
 			}
 			JScrollPane scrollPane = new JScrollPane(nodeView);
 
 			splitPane.setRightComponent(scrollPane);
 		}
 	}
 
 	private void updateRemoteURL(String defaultURL)
 	{
 		String remote = System
 		        .getProperty(OTViewerHelper.REMOTE_URL_PROP, null);
 
 		try {
 			if (remote == null) {
 				if (defaultURL.startsWith("http:")) {
 					remoteURL = new URL(defaultURL);
 				}
 			} else {
 				remoteURL = new URL(remote);
 			}
 		} catch (Exception e) {
 			remoteURL = null;
 			System.err.println("Remote URL is invalid.");
 			e.printStackTrace();
 		}
 	}
 
 	public void remoteSaveData(String method)
 	    throws Exception
 	{
 		HttpURLConnection urlConn;
 		DataOutputStream urlDataOut;
 		BufferedReader urlDataIn;
 
 		// If method isn't "POST" or "PUT", throw an exception
 		if (!(method.compareTo(OTViewer.HTTP_POST) == 0 || method
 		        .compareTo(OTViewer.HTTP_PUT) == 0)) {
 			throw new Exception("Invalid HTTP Request method for data saving");
 		}
 
 		urlConn = (HttpURLConnection) remoteURL.openConnection();
 		urlConn.setDoInput(true);
 		urlConn.setDoOutput(true);
 		urlConn.setUseCaches(false);
 		urlConn.setRequestMethod(method);
 		urlConn.setRequestProperty("Content-Type", "application/xml");
 
 		// Send POST output.
 		urlDataOut = new DataOutputStream(urlConn.getOutputStream());
 		Exporter.export(urlDataOut, xmlDB.getRoot(), xmlDB);
 		urlDataOut.flush();
 		urlDataOut.close();
 
 		// Get response data.
 		urlDataIn = new BufferedReader(new InputStreamReader(
 		        new DataInputStream(urlConn.getInputStream())));
 		String str;
 		String response = "";
 		while (null != ((str = urlDataIn.readLine()))) {
 			response += str + "\n";
 		}
 		urlDataIn.close();
 		// Need to trap non-HTTP 200/300 responses and throw an exception (if an
 		// exception isn't thrown already) and capture the exceptions upstream
 		int code = urlConn.getResponseCode();
 		if (code >= 400) {
 			throw new Exception("HTTP Response: "
 			        + urlConn.getResponseMessage() + "\n\n" + response);
 		}
 		urlConn.disconnect();
 		xmlDB.setDirty(false);
 		setTitle(remoteURL.toString());
 	}
 
 	public void createActions()
 	{
 		newUserDataAction = new AbstractAction("New") {
 
 			/**
 			 * nothing to serialize here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 			 */
 			public void actionPerformed(ActionEvent arg0)
 			{
 				createNewUser();
 			}
 
 		};
 
 		loadUserDataAction = new AbstractAction("Open...") {
 			/**
 			 * nothing to serialize here. Just the parent class.
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 			 */
 			public void actionPerformed(ActionEvent arg0)
 			{
 				openUserData();
 			}
 
 		};
 
 		exportToHtmlAction = new AbstractAction("Export to html...") {
 			/**
 			 * nothing to serialize here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent arg0)
 			{
 				File fileToSave = getReportFile();
 				OTMLToXHTMLConverter otxc = new OTMLToXHTMLConverter(
 				        otViewFactory, bodyPanel.getViewContainer());
 				otxc.setXHTMLParams(fileToSave, 800, 600);
 
 				(new Thread(otxc)).start();
 			}
 		};
 		// Isn't it enabled by default?
 		exportToHtmlAction.setEnabled(true);
 
 		saveUserDataAction = new AbstractAction("Save") {
 
 			/**
 			 * Nothing to serialize here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 			 */
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if (currentUserFile == null || !currentUserFile.exists()) {
 					saveUserDataAsAction.actionPerformed(arg0);
 					return;
 				}
 
 				if (currentUserFile.exists()) {
 					try {
 						Exporter.export(currentUserFile, userDataDB.getRoot(),
 						        userDataDB);
 						userDataDB.setDirty(false);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 
 		saveUserDataAsAction = new AbstractAction("Save As...") {
 
 			/**
 			 * nothing to serizile here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 			 */
 			public void actionPerformed(ActionEvent arg0)
 			{
 				Frame frame = (Frame) SwingUtilities.getRoot(OTViewer.this);
 
 				MostRecentFileDialog mrfd = new MostRecentFileDialog(
 				        "org.concord.otviewer.saveotml");
 				mrfd.setFilenameFilter("otml");
 
 				if (currentUserFile != null) {
 					mrfd.setCurrentDirectory(currentUserFile.getParentFile());
 					mrfd.setSelectedFile(currentUserFile);
 				}
 
 				int retval = mrfd.showSaveDialog(frame);
 
 				File file = null;
 				if (retval == MostRecentFileDialog.APPROVE_OPTION) {
 					file = mrfd.getSelectedFile();
 
 					String fileName = file.getPath();
 					currentUserFile = file;
 
 					if (!fileName.toLowerCase().endsWith(".otml")) {
 						currentUserFile = new File(currentUserFile
 						        .getAbsolutePath()
 						        + ".otml");
 					}
 
 					try {
 						Exporter.export(currentUserFile, userDataDB.getRoot(),
 						        userDataDB);
 						userDataDB.setDirty(false);
 						setTitle(baseFrameTitle + ": "
 						        + currentUserFile.toString());
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 
 					frame.setTitle(fileName);
 
 				}
 			}
 
 		};
 
 		loadAction = new AbstractAction("Open Authored Content...") {
 
 			/**
 			 * nothing to serizile here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 			 */
 			public void actionPerformed(ActionEvent arg0)
 			{
 				Frame frame = (Frame) SwingUtilities.getRoot(OTViewer.this);
 
 				MostRecentFileDialog mrfd = new MostRecentFileDialog(
 				        "org.concord.otviewer.openotml");
 				mrfd.setFilenameFilter("otml");
 
 				int retval = mrfd.showOpenDialog(frame);
 
 				File file = null;
 				if (retval == MostRecentFileDialog.APPROVE_OPTION) {
 					file = mrfd.getSelectedFile();
 				}
 
 				if (file != null && file.exists()) {
 					System.out.println("load file name: " + file);
 					loadFile(file);
 					
 					exportToHtmlAction.setEnabled(true);
 				}
 			}
 
 		};
 
 		saveAction = new AbstractAction("Save Authored Content...") {
 
 			/**
 			 * nothing to serizile here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 			 */
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if (remoteURL != null) {
 					try {
 						if (Boolean
 						        .getBoolean(OTViewerHelper.REST_ENABLED_PROP)) {
 							try {
 								remoteSaveData(OTViewer.HTTP_PUT);
 							} catch (Exception e) {
 								remoteSaveData(OTViewer.HTTP_POST);
 							}
 						} else {
 							remoteSaveData(OTViewer.HTTP_POST);
 						}
 					} catch (Exception e) {
 						JOptionPane
 						        .showMessageDialog(
 						                (Frame) SwingUtilities
 						                        .getRoot(OTViewer.this),
 						                "There was an error saving. Check your URL and try again.",
 						                "Error Saving",
 						                JOptionPane.ERROR_MESSAGE);
 						e.printStackTrace();
 					}
 				} else {
 					if (currentAuthoredFile == null) {
 						saveAsAction.actionPerformed(arg0);
 						return;
 					}
 
 					if (checkForReplace(currentAuthoredFile)) {
 						try {
 							ExporterJDOM.export(currentAuthoredFile, xmlDB
 							        .getRoot(), xmlDB);
 							xmlDB.setDirty(false);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				} // end if (remoteUrl == null)
 			}
 		};
 
 		saveAsAction = new AbstractAction("Save Authored Content As...") {
 
 			/**
 			 * nothing to serizile here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 			 */
 			public void actionPerformed(ActionEvent arg0)
 			{
 				Frame frame = (Frame) SwingUtilities.getRoot(OTViewer.this);
 
 				MostRecentFileDialog mrfd = new MostRecentFileDialog(
 				        "org.concord.otviewer.saveotml");
 				mrfd.setFilenameFilter("otml");
 
 				if (currentAuthoredFile != null) {
 					mrfd.setCurrentDirectory(currentAuthoredFile
 					        .getParentFile());
 					mrfd.setSelectedFile(currentAuthoredFile);
 				}
 
 				int retval = mrfd.showSaveDialog(frame);
 
 				File file = null;
 				if (retval == MostRecentFileDialog.APPROVE_OPTION) {
 					file = mrfd.getSelectedFile();
 
 					String fileName = file.getPath();
 
 					if (!fileName.toLowerCase().endsWith(".otml")) {
 						file = new File(file.getAbsolutePath() + ".otml");
 					}
 
 					if (checkForReplace(file)) {
 						try {
 							ExporterJDOM.export(file, xmlDB.getRoot(), xmlDB);
 							currentAuthoredFile = file;
 							xmlDB.setDirty(false);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 
 					frame.setTitle(fileName);
 					remoteURL = null;
 					updateMenuBar();
 
 				}
 			}
 		};
 
 		saveRemoteAsAction = new AbstractAction("Save Remotely As...") {
 
 			/**
 			 * nothing to serizile here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 			 */
 			public void actionPerformed(ActionEvent arg0)
 			{
 				// Pop up a dialog asking for a URL
 				// Post the otml to the url
 				JPanel panel = new JPanel();
 				panel.setBorder(new EmptyBorder(10, 10, 10, 10));
 				panel.setLayout(new BorderLayout());
 
 				JLabel prompt = new JLabel(
 				        "Please enter the URL to which you would like to save:");
 				prompt.setBorder(new EmptyBorder(0, 0, 10, 0));
 				JTextField textField = new JTextField();
 				if (remoteURL == null) {
 					textField.setText("http://");
 				} else {
 					textField.setText(remoteURL.toString());
 				}
 
 				JPanel checkboxPanel = new JPanel();
 				JCheckBox restCheckbox = new JCheckBox("REST Enabled?");
 				restCheckbox.setSelected(Boolean
 				        .getBoolean(OTViewerHelper.REST_ENABLED_PROP));
 				checkboxPanel.setBorder(new EmptyBorder(5, 5, 0, 0));
 				checkboxPanel.add(restCheckbox);
 
 				panel.add(prompt, BorderLayout.NORTH);
 				panel.add(textField, BorderLayout.CENTER);
 				panel.add(checkboxPanel, BorderLayout.SOUTH);
 
 				int returnVal = CustomDialog.showOKCancelDialog(
 				        (Frame) SwingUtilities.getRoot(OTViewer.this), // parent
 				        panel, // custom content
 				        "Save URL", // title
 				        false, // resizeable
 				        true // modal
 				        );
 
 				if (returnVal == 0) {
 					try {
 						remoteURL = new URL(textField.getText());
 						System.setProperty(OTViewerHelper.REST_ENABLED_PROP,
 						        Boolean.toString(restCheckbox.isSelected()));
 						remoteSaveData(OTViewer.HTTP_POST);
 						updateMenuBar();
 					} catch (Exception e) {
 						System.err.println("Bad URL. Not saving.");
 						JOptionPane
 						        .showMessageDialog(
 						                (Frame) SwingUtilities
 						                        .getRoot(OTViewer.this),
 						                "There was an error saving. Check your URL and try again.",
 						                "Error Saving",
 						                JOptionPane.ERROR_MESSAGE);
 						e.printStackTrace();
 					}
 				} else {
 					// CANCELLED
 				}
 			}
 		};
 
 		exportImageAction = new AbstractAction("Export Image...") {
 
 			/**
 			 * nothing to serialize here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e)
 			{
 				// this introduces a dependency on concord Swing project
 				// instead there needs to be a way to added these actions
 				// through
 				// the xml
 				Component currentComp = bodyPanel.getCurrentComponent();
 				Util.makeScreenShot(currentComp);
 			}
 		};
 
 		exportHiResImageAction = new AbstractAction("Export Hi Res Image...") {
 			/**
 			 * nothing to serizile here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e)
 			{
 				Component currentComp = bodyPanel.getCurrentComponent();
 				Util.makeScreenShot(currentComp, 2, 2);
 			}
 		};
 
 		debugAction = new AbstractAction("Debug Mode") {
 
 			/**
 			 * nothing to serizile here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e)
 			{
 				Object source = e.getSource();
 				if (((JCheckBoxMenuItem) source).isSelected()) {
 					System.setProperty(OTViewerHelper.DEBUG_PROP, "true");
 				} else {
 					System.setProperty(OTViewerHelper.DEBUG_PROP, "false");
 				}
 
 				try {
 					reloadWindow();
 				} catch (Exception exp) {
 					exp.printStackTrace();
 				}
 
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run()
 					{
 						updateMenuBar();
 					}
 				});
 				exportToHtmlAction.setEnabled(true);
 			}
 		};
 
 		showConsoleAction = new AbstractAction("Show Console") {
 
 			/**
 			 * nothing to serialize here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e)
 			{
 				if (consoleFrame != null) {
 					consoleFrame.setVisible(true);
 				}
 			}
 		};
 
 		reloadWindowAction = new AbstractAction("Reload window") {
 
 			/**
 			 * nothing to serialize here
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e)
 			{
 				try {
 					reload();
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 		};
 
 		exitAction = new ExitAction();
 
 	}
 
 	/**
 	 * @return Returns the menuBar.
 	 */
 	public JMenuBar updateMenuBar()
 	{
 		// ///////////////////////////////////////////////
 		JMenu fileMenu = null;
 		if (menuBar == null) {
 			menuBar = new JMenuBar();
 			fileMenu = new JMenu("File");
 			menuBar.add(fileMenu);
 		} else {
 			fileMenu = menuBar.getMenu(0);
 			fileMenu.removeAll();
 		}
 
 		if (Boolean.getBoolean(OTViewerHelper.AUTHOR_PROP)) {
 			userMode = OTViewerHelper.NO_USER_MODE;
 		}
 
 		if (userMode == OTViewerHelper.SINGLE_USER_MODE) {
 			fileMenu.setEnabled(!justStarted);
 
 			fileMenu.add(newUserDataAction);
 
 			fileMenu.add(loadUserDataAction);
 
 			fileMenu.add(saveUserDataAction);
 
 			fileMenu.add(saveUserDataAsAction);
 		}
 
 		if (Boolean.getBoolean(OTViewerHelper.DEBUG_PROP)) {
 			if (userMode == OTViewerHelper.SINGLE_USER_MODE) {
 				loadAction.putValue(Action.NAME, "Open Authored Content...");
 				saveAction.putValue(Action.NAME, "Save Authored Content");
 				saveAsAction.putValue(Action.NAME,
 				        "Save Authored Content As...");
 				saveRemoteAsAction.putValue(Action.NAME,
 				        "Save Authored Content Remotely As...");
 			} else {
 				loadAction.putValue(Action.NAME, "Open...");
 				saveAction.putValue(Action.NAME, "Save");
 				saveAsAction.putValue(Action.NAME, "Save As...");
 				saveRemoteAsAction.putValue(Action.NAME, "Save Remotely As...");
 			}
 			fileMenu.add(loadAction);
 			fileMenu.add(saveAction);
 			fileMenu.add(saveAsAction);
 			fileMenu.add(saveRemoteAsAction);
 		}
 
 		if (Boolean.getBoolean(OTViewerHelper.AUTHOR_PROP)
 		        && !Boolean.getBoolean(OTViewerHelper.DEBUG_PROP)) {
 			if (userMode == OTViewerHelper.SINGLE_USER_MODE) {
 				loadAction.putValue(Action.NAME, "Open Authored Content...");
 				saveAction.putValue(Action.NAME, "Save Authored Content");
 				saveAsAction.putValue(Action.NAME,
 				        "Save Authored Content As...");
 			} else {
 				loadAction.putValue(Action.NAME, "Open...");
 				saveAction.putValue(Action.NAME, "Save");
 				saveAsAction.putValue(Action.NAME, "Save As...");
 			}
 			fileMenu.add(loadAction);
 			fileMenu.add(saveAction);
 			fileMenu.add(saveAsAction);
 		}
 
 		if (Boolean.getBoolean("otrunk.view.export_image")) {
 			fileMenu.add(exportImageAction);
 
 			fileMenu.add(exportHiResImageAction);
 		}
 
 		fileMenu.add(exportToHtmlAction);
 
 		fileMenu.add(showConsoleAction);
 
 		if (Boolean.getBoolean(OTViewerHelper.AUTHOR_PROP)
 		        || Boolean.getBoolean(OTViewerHelper.DEBUG_PROP)) {
 			fileMenu.add(reloadWindowAction);
 		}
 
 		JCheckBoxMenuItem debugItem = new JCheckBoxMenuItem(debugAction);
 		debugItem.setSelected(Boolean.getBoolean(OTViewerHelper.DEBUG_PROP));
 		fileMenu.add(debugItem);
 
 		fileMenu.add(exitAction);
 
 		return menuBar;
 	}
 
 	boolean checkForReplace(File file)
 	{
 		if (file == null){
 			return false;
 		}
 		
 		if (!file.exists()) {
 			return true; // File doesn't exist, so go ahead and save
 		}
 		
 		if (currentAuthoredFile != null && file.compareTo(currentAuthoredFile) == 0){
 			return true; // we're already authoring this file, so no need to
 							// prompt
 		}
 		
 		final Object[] options = { "Yes", "No" };
 		return javax.swing.JOptionPane.showOptionDialog(null, "The file '"
 		        + file.getName() + "' already exists.  "
 		        + "Replace existing file?", "Warning",
 		        javax.swing.JOptionPane.YES_NO_OPTION,
 		        javax.swing.JOptionPane.WARNING_MESSAGE, null, options,
 		        options[1]) == javax.swing.JOptionPane.YES_OPTION;
 
 	}
 
 	/**
 	 * Checks if the user has unsaved work. If they do then it prompts them to
 	 * confirm what they are doing. If they cancel then it returns false.
 	 * 
 	 * @return
 	 */
 	public boolean checkForUnsavedUserData()
 	{
 		if (currentUser != null && userDataDB != null) {
 			if (userDataDB.isDirty()) {
 				// show dialog message telling them they haven't
 				// saved their work
 				// FIXME
 				String options[] = { "Don't Save", "Cancel", "Save" };
 				askedAboutSavingUserData = true;
 				int chosenOption = JOptionPane.showOptionDialog(this,
 				        "Save Changes?", "Save Changes?",
 				        JOptionPane.DEFAULT_OPTION,
 				        JOptionPane.WARNING_MESSAGE, null, options, options[2]);
 				switch (chosenOption) {
 				case 0:
 					System.err.println("Not saving work");
 					break;
 				case 1:
 					System.err.println("Canceling close");
 					return false;
 				case 2:
 					System.err.println("Set needToSaveUserData true");
 					needToSaveUserData = true;
 					break;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Checks if the user has unsaved authored data. If they do then it prompts
 	 * them to confirm what they are doing. If they cancel then it returns
 	 * false.
 	 * 
 	 * @return
 	 */
 	public boolean checkForUnsavedAuthorData()
 	{
 		if (xmlDB != null) {
 			if (xmlDB.isDirty()) {
 				// show dialog message telling them they haven't
 				// saved their work
 				// FIXME
 				String options[] = { "Don't Save", "Cancel", "Save" };
 				int chosenOption = JOptionPane.showOptionDialog(this,
 				        "Save Changes?", "Save Changes?",
 				        JOptionPane.DEFAULT_OPTION,
 				        JOptionPane.WARNING_MESSAGE, null, options, options[2]);
 				switch (chosenOption) {
 				case 0:
 					System.err.println("Not saving authored data");
 					break;
 				case 1:
 					System.err.println("Canceling close");
 					return false;
 				case 2:
 					System.err.println("Saving authored data");
 					saveAction.actionPerformed(null);
 					break;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * This does not check for unsaved user data
 	 * 
 	 */
 	public void newAnonUserData()
 	{
 		// call some new method for creating a new un-saved user state
 		// this should set the currentUserFile to null, so the save check
 		// prompts
 		// for a file name
 		try {
 			// need to make a brand new stateDB
 			userDataDB = new XMLDatabase();
 			// System.out.println("otrunk: " + otrunk + " userDatabase: " +
 			// userDataDB);
 			OTObjectService objService = otrunk.createObjectService(userDataDB);
 
 			OTStateRoot stateRoot = (OTStateRoot) objService
 			        .createObject(OTStateRoot.class);
 			userDataDB.setRoot(stateRoot.getGlobalId());
 			stateRoot.setFormatVersionString("1.0");
 
 			OTUserObject userObject = OTViewerHelper.createUser(
 			        "anon_single_user", objService);
 
 			otrunk.initUserObjectService((OTObjectServiceImpl) objService,
 			        userObject, stateRoot);
 
 			userDataDB.setDirty(false);
 
 			currentUserFile = null;
 
 			setCurrentUser(userObject);
 
 			reloadWindow();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public boolean exit()
 	{
 		try {
 			if (!checkForUnsavedUserData()) {
 				// the user canceled the operation
 				return false;
 			}
 
 			if (Boolean.getBoolean(OTViewerHelper.AUTHOR_PROP)
 			        && !checkForUnsavedAuthorData()) {
 				// the user canceled the operation
 				return false;
 			}
 
 			// FIXME there is a problem with this logic. If the user saved data
 			// just before closing
 			// checkForUnsavedUserData will not see any unsaved data. But if
 			// some view creates
 			// data in the viewClosed method then that data will not get saved
 			// here.
 			// I think the key to solving this is to seperate the
 			// automatic/logging data from the
 			// user visible data. And then make a rule that saving data in the
 			// viewClosed method
 			// is not allowed.
 			bodyPanel.setCurrentObject(null);
 
 			conditionalSaveUserData();
 
 			if (otrunk != null)
 				otrunk.close();
 		} catch (Exception exp) {
 			exp.printStackTrace();
 			// exit anyhow
 		}
 		System.exit(0);
 		return true;
 	}
 
 	protected void conditionalSaveUserData()
 	{
 		if (!askedAboutSavingUserData) {
 			checkForUnsavedUserData();
 		}
 
 		if (needToSaveUserData) {
 			saveUserDataAction.actionPerformed(null);
 
 		} else {
 			System.err.println("Not saving work before closing.");
 		}
 
 		// Reset these back to false, so if the user is switching to a new
 		// user or loading a new file we are in a clean state, for that file or
 		// user
 		askedAboutSavingUserData = false;
 		needToSaveUserData = false;
 	}
 
 	public File getReportFile()
 	{
 		Frame frame = (Frame) SwingUtilities.getRoot(OTViewer.this);
 
 		MostRecentFileDialog mrfd = new MostRecentFileDialog(
 		        "org.concord.otviewer.saveotml");
 		mrfd.setFilenameFilter("html");
 
 		if (currentUserFile != null) {
 			mrfd.setCurrentDirectory(currentUserFile.getParentFile());
 		}
 
 		int retval = mrfd.showSaveDialog(frame);
 
 		File file = null;
 		if (retval == MostRecentFileDialog.APPROVE_OPTION) {
 			file = mrfd.getSelectedFile();
 
 			String fileName = file.getPath();
 
 			if (!fileName.toLowerCase().endsWith(".html")) {
 				file = new File(file.getAbsolutePath() + ".html");
 			}
 
 			return file;
 
 		}
 
 		return null;
 	}
 
 	public void createNewUser()
 	{
 		if (!checkForUnsavedUserData()) {
 			// the user canceled the operation
 			return;
 		}
 
 		// This ensures viewClosed is called
 		bodyPanel.setCurrentObject(null);
 		conditionalSaveUserData();
 
 		// call some new method for creating a new un-saved user state
 		// this should set the currentUserFile to null, so the save check
 		// prompts
 		// for a file name
 		newAnonUserData();
 		exportToHtmlAction.setEnabled(true);
 	}
 
 	public void openUserData()
 	{
 		if (!checkForUnsavedUserData()) {
 			// the user canceled the operation
 			return;
 		}
 
 		// FIXME Calling the method below would insure the view is closed, and
 		// that any data that is
 		// is modified in that view closed operation will get saved, however if
 		// the user
 		// cancels the open dialog then we would be left in an unknown
 		// state. The current view would be closed which they would want to see
 		// again.
 		// bodyPanel.setCurrentObject(null);
 
 		conditionalSaveUserData();
 
 		Frame frame = (Frame) SwingUtilities.getRoot(OTViewer.this);
 
 		MostRecentFileDialog mrfd = new MostRecentFileDialog(
 		        "org.concord.otviewer.openotml");
 		mrfd.setFilenameFilter("otml");
 
 		int retval = mrfd.showOpenDialog(frame);
 
 		File file = null;
 		if (retval == MostRecentFileDialog.APPROVE_OPTION) {
 			file = mrfd.getSelectedFile();
 		}
 
 		if (file != null && file.exists()) {
 			loadUserDataFile(file);
 			exportToHtmlAction.setEnabled(true);
 		}
 	}
 
 	public void instructionPanel()
 	{
 		commDialog.setResizable(false);
 
 		JPanel panel = new JPanel();
 		panel.setBackground(Color.WHITE);
 		panel.setLayout(null);
 
 		JLabel lNew = new JLabel(
 		        "Click the \"New\" button to create a new portfolio:");
 		JLabel lOpen = new JLabel(
 		        "Click the \"Open\" button to open a saved portfolio:");
 		JButton bNew = new JButton("New");
 		JButton bOpen = new JButton("Open");
 
 		panel.add(lNew);
 		panel.add(lOpen);
 		panel.add(bNew);
 		panel.add(bOpen);
 
 		lNew.setLocation(50, 100);
 		lOpen.setLocation(50, 150);
 		bNew.setLocation(400, 100);
 		bOpen.setLocation(400, 150);
 
 		lNew.setSize(340, 30);
 		lOpen.setSize(340, 30);
 		bNew.setSize(70, 25);
 		bOpen.setSize(70, 25);
 
 		bNew.setOpaque(false);
 		bOpen.setOpaque(false);
 
 		bNew.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				commDialog.setVisible(false);
 				createNewUser();
 			}
 		});
 
 		bOpen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				commDialog.setVisible(false);
 				openUserData();
 			}
 		});
 
 		commDialog.getContentPane().add(panel);
 		commDialog.setBounds(200, 200, 500, 300);
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run()
 			{
 				commDialog.setVisible(true);
 				justStarted = false;
 				updateMenuBar();
 			}
 		});
 	}
 
 	public OTViewContainerPanel getViewContainerPanel()
 	{
 		return this.bodyPanel;
 	}
 
 	public void setExitAction(AbstractAction exitAction)
 	{
 		this.exitAction = exitAction;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.applesupport.AppleApplicationAdapter#about()
 	 */
 	public void about()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.applesupport.AppleApplicationAdapter#preferences()
 	 */
 	public void preferences()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.applesupport.AppleApplicationAdapter#quit()
 	 */
 	public void quit()
 	{
 		exitAction.actionPerformed(null);
 	}
 } // @jve:decl-index=0:visual-constraint="10,10"
 
 class HtmlFileFilter extends javax.swing.filechooser.FileFilter
 {
 	public boolean accept(File f)
 	{
 		if (f == null)
 			return false;
 		if (f.isDirectory())
 			return true;
 
 		return (f.getName().toLowerCase().endsWith(".html"));
 	}
 
 	public String getDescription()
 	{
 		return "HTML files";
 	}
 
 } // @jve:decl-index=0:visual-constraint="10,10"
