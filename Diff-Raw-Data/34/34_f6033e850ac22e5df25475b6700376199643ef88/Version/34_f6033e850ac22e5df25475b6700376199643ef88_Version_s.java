 package org.vpac.grisu.client.view.swing.template.panels;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.border.TitledBorder;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.TemplateTagConstants;
 import org.vpac.grisu.client.model.template.nodes.TemplateNode;
 import org.vpac.grisu.client.model.template.nodes.TemplateNodeEvent;
 import org.vpac.grisu.model.FqanEvent;
 import org.vpac.grisu.model.FqanListener;
 import org.vpac.grisu.model.GrisuRegistry;
 import org.vpac.grisu.model.info.UserApplicationInformation;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 public class Version extends JPanel implements TemplateNodePanel,
 		ActionListener, ValueListener, FqanListener {
 
 	private static final long serialVersionUID = -4614286850190629566L;
 
 	static final Logger myLogger = Logger.getLogger(Version.class.getName());
 
 	public static final String DEFAULT_VERSION_NOT_AVAILABLE_STRING = "Default version not available.";
 	
 
 	public static final String ANY_MODE_TEMPLATETAG_KEY = "useAny";
 	public static final String DEFAULT_MODE_TEMPLATETAG_KEY = "useDefault";
 	public static final String EXACT_MODE_TEMPLATETAG_KEY = "useExact";
 
 	public static final String ANY_MODE_STRING = "Any";
 	public static final String DEFAULT_MODE_STRING = "Default";
 	public static final String EXACT_MODE_STRING = "Exact";
 	
 	// Version
 	public final static int DEFAULT_VERSION_MODE = 0;
 	public final static int ANY_VERSION_MODE = 1;
 	public final static int EXACT_VERSION_MODE = 2;
 
 	public static final String STARTUP_MODE_KEY = "startMode";
 
 	private JComboBox versionComboBox;
 	private DefaultComboBoxModel versionModel = new DefaultComboBoxModel();
 	private JRadioButton anyRadioButton;
 	private JRadioButton defaultRadioButton;
 	private JRadioButton exactRadioButton;
 
 	private TemplateNode templateNode;
 	private GrisuRegistry registry = null;
 	private UserApplicationInformation infoObject = null;
 //	private UserProperties esv = GrisuRegistry.getDefault().getUserProperties();
 	private String applicationName = null;
 	private boolean useAny = true;
 	private boolean useDefault = true;
 	private String defaultVersion;
 	private boolean useExact = true;
 
 	private String startMode = null;
 	private short currentMode = -1;
 	
 	private ButtonGroup modeGroup = new ButtonGroup();
 	
 	private SubmissionLocation submissionLocationPanel = null;
 	
 	
 	private boolean versionLocked = false;
 	private String lastVersion;
 	
 	
 	/**
 	 * Create the panel
 	 */
 	public Version() {
 		super();
 		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		setBorder(new TitledBorder(null, "Version",
 				TitledBorder.DEFAULT_JUSTIFICATION,
 				TitledBorder.DEFAULT_POSITION, null, null));
 
 		//
 	}
 
 	public void setTemplateNode(TemplateNode node)
 			throws TemplateNodePanelException {
 
 		this.templateNode = node;
 		this.templateNode.setTemplateNodeValueSetter(this);
 		
 		this.applicationName = this.templateNode.getTemplate().getApplicationName();
 		defaultVersion = node.getDefaultValue();
 		
 		registry = GrisuRegistry.getDefault(node.getTemplate().getEnvironmentManager().getServiceInterface());
 		
 		registry.getUserEnvironmentManager().addFqanListener(this);
 		
 		registry.getHistoryManager().setMaxNumberOfEntries(TemplateTagConstants.getGlobalLastVersionModeKey(applicationName), 1);
 		registry.getHistoryManager().setMaxNumberOfEntries(TemplateTagConstants.getGlobalLastVersionKey(applicationName), 1);
 		infoObject = registry.getUserApplicationInformation(applicationName);
 				
 		try {
 			lastVersion = registry.getHistoryManager().getEntries(TemplateTagConstants.getGlobalLastVersionKey(infoObject.getApplicationName())).get(0);
 		} catch (Exception e) {
 			lastVersion = null;
 		}
 
 		this.useAny = node.getOtherProperties().containsKey(
 				ANY_MODE_TEMPLATETAG_KEY);
 		this.useDefault = node.getOtherProperties().containsKey(
 				DEFAULT_MODE_TEMPLATETAG_KEY);
 		this.useExact = node.getOtherProperties().containsKey(
 				EXACT_MODE_TEMPLATETAG_KEY);
 
 		if (node.getOtherProperties().containsKey(STARTUP_MODE_KEY)) {
 			startMode = node.getOtherProperty(STARTUP_MODE_KEY);
 			if (ANY_MODE_STRING.equals(startMode)) {
 				useAny = true;
 			} else if (EXACT_MODE_STRING.equals(startMode)) {
 				useExact = true;
 			} else if (DEFAULT_MODE_STRING.equals(startMode)) {
 				
				if (defaultVersion == null || "".equals(defaultVersion)) {
 					useDefault = false;
 					myLogger
 							.warn("Not using default mode because no default version value is specified in template.");
 				} else {
 					lastVersion = defaultVersion;
 					useDefault = true;
 				}
 			}
 		} else {
 			try {
 				startMode = registry.getHistoryManager().getEntries(TemplateTagConstants.getGlobalLastVersionModeKey(infoObject.getApplicationName())).get(0);
 
 				if ( ANY_MODE_STRING.equals(startMode) && ! useAny ) {
 					startMode = null;
 				} else if ( DEFAULT_MODE_STRING.equals(startMode) && ! useDefault ) {
 					startMode = null;
 				} else if ( EXACT_MODE_STRING.equals(startMode) ) {
 					startMode = null;
 				}
 				
 			} catch (Exception e) {
 				myLogger.debug("Can't get last used parameter for version mode.");
 			}
 
 			if ( startMode == null ) {
 			
 				if (useAny) {
 					startMode = ANY_MODE_STRING;
 				} else if (useExact) {
 					startMode = EXACT_MODE_STRING;
 				} else if (useDefault) {
 					startMode = DEFAULT_MODE_STRING;
 				} else {
 					useAny = true;
 					startMode = ANY_MODE_STRING;
 				}
 				
 			}
 		}
 		
 		
 		if ( DEFAULT_MODE_STRING.equals(startMode) ) {
 			currentMode = DEFAULT_VERSION_MODE;
 		} else if ( EXACT_MODE_STRING.equals(startMode) ) {
 			currentMode = EXACT_VERSION_MODE;
 		} else {
 			currentMode = ANY_VERSION_MODE;
 		}
 
 		if (useAny) {
 			add(getAnyRadioButton());
 		}
 		if (useDefault) {
 			add(getDefaultRadioButton());
 		}
 		if (useExact) {
 			add(getExactRadioButton());
 		}
 
 		// add combobox as last item
 		add(getVersionComboBox());
 
 		// this might be slightly dodgy. But it should always work if a SubmissionLocation template tag is present.
 		getSubmissionLocationPanel();
 
 
 		fillVersions(lastVersion);
 		
 		switch (currentMode) {
 		case ANY_VERSION_MODE:
 			getAnyRadioButton().doClick();
 			break;
 		case DEFAULT_VERSION_MODE:
 			getDefaultRadioButton().doClick();
 			break;
 		case EXACT_VERSION_MODE:
 			getExactRadioButton().doClick();
 			break;
 		}
 		
 				
 
 	}
 	
	private void fillVersions(String prefferedVersion) {
 		
 		versionLocked = true;
 
 		versionModel.removeAllElements();
 		for ( String version : infoObject.getAllAvailableVersionsForFqan(registry.getUserEnvironmentManager().getCurrentFqan()) ) {
 			versionModel.addElement(version);
 		}
 
		if ( versionModel.getIndexOf(prefferedVersion) >= 0 ) {
			versionModel.setSelectedItem(prefferedVersion);
 		} else {
 			if ( versionModel.getSize() > 0 ) {
 				versionModel.setSelectedItem(versionModel.getElementAt(0));
 			}
 		}
 		
 		versionLocked = false;
 
 		
 	}
 	
 	private SubmissionLocation getSubmissionLocationPanel() {
 		if ( submissionLocationPanel == null ) {
 			try {
 				
 				// try to find a templateNodevalueSetter that is a SubmissionLocationPanel
 				for ( TemplateNode node : this.templateNode.getTemplate().getTemplateNodes().values() ) {
 					if ( node.getTemplateNodeValueSetter() instanceof SubmissionLocation ) {
 						submissionLocationPanel = (SubmissionLocation)node.getTemplateNodeValueSetter();
 						break;
 					}
 				
 				}
 			} catch (Exception e) {
 				myLogger.warn("Could not get submissionLocationPanel yet...");
 				submissionLocationPanel = null;
 				return null;
 			}
 			if ( submissionLocationPanel != null ) {
 				submissionLocationPanel.addValueListener(this);
 				// remove value listener just in case it's already there...
 				removeValueListener(submissionLocationPanel);
 				addValueListener(submissionLocationPanel);
 			}
 		}
 		return submissionLocationPanel;
 	}
 	
 	public String getCurrentValue() {
 		return getExternalSetValue();
 	}
 
 	private void switchMode(String mode) {
 
 		if (ANY_MODE_STRING.equals(mode)) {
 			switchMode(ANY_VERSION_MODE);
 			return;
 		} else if (DEFAULT_MODE_STRING.equals(mode)) {
 			switchMode(DEFAULT_VERSION_MODE);
 			return;
 		} else if (EXACT_MODE_STRING.equals(mode)) {
 			switchMode(EXACT_VERSION_MODE);
 			return;
 		}
 
 		myLogger.error("Mode not supported: " + mode);
 	}
 
 	private void switchMode(int mode) {
 
 		switch (mode) {
 		case ANY_VERSION_MODE:
 			switchToAnyVersionMode();
 			registry.getHistoryManager().addHistoryEntry(TemplateTagConstants.getGlobalLastVersionModeKey(infoObject.getApplicationName()), ANY_MODE_STRING, new Date());
 			fireVersionChanged(Constants.NO_VERSION_INDICATOR_STRING);
 			break;
 		case DEFAULT_VERSION_MODE:
 			switchToDefaultVersionMode();
 			registry.getHistoryManager().addHistoryEntry(TemplateTagConstants.getGlobalLastVersionModeKey(infoObject.getApplicationName()), DEFAULT_MODE_STRING, new Date());
 			fireVersionChanged((String)(versionModel.getSelectedItem()));
 			break;
 		case EXACT_VERSION_MODE:
 			switchToExactVersionMode();
 			registry.getHistoryManager().addHistoryEntry(TemplateTagConstants.getGlobalLastVersionModeKey(infoObject.getApplicationName()), EXACT_MODE_STRING, new Date());
 			fireVersionChanged((String)(versionModel.getSelectedItem()));
 			break;
 		default:
 			myLogger
 					.error("Can't switch to mode: " + mode + ". Not supported.");
 		}
 		
 	}
 
 	private void switchToExactVersionMode() {
 
 		this.currentMode = EXACT_VERSION_MODE;
 		
 		getVersionComboBox().setEnabled(true);
 		
 		if ( DEFAULT_VERSION_NOT_AVAILABLE_STRING.equals(versionModel.getSelectedItem()) ) {
 			try {
 				String temp = (String)versionModel.getElementAt(0);
 				versionModel.setSelectedItem(temp);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		versionModel.removeElement(DEFAULT_VERSION_NOT_AVAILABLE_STRING);
 
 	}
 
 	private void switchToDefaultVersionMode() {
 		
 		this.currentMode = DEFAULT_VERSION_MODE;
 		
 		Set<String> temp = infoObject.getAvailableSubmissionLocationsForVersionAndFqan(defaultVersion, registry.getUserEnvironmentManager().getCurrentFqan());
 		
 		try {
 		if ( temp.size() > 0 ) {
 			getVersionComboBox().setEnabled(true);
 			versionModel.setSelectedItem(defaultVersion);
 		} else {
 			versionModel.addElement(DEFAULT_VERSION_NOT_AVAILABLE_STRING);
 			versionModel.setSelectedItem(DEFAULT_VERSION_NOT_AVAILABLE_STRING);
 		}
 		} finally {
 		
 			getVersionComboBox().setEnabled(false);
 		}
 
 	}
 
 	private void switchToAnyVersionMode() {
 
 		this.currentMode = ANY_VERSION_MODE;
 		getVersionComboBox().setEnabled(false);
 
 		if ( DEFAULT_VERSION_NOT_AVAILABLE_STRING.equals(versionModel.getSelectedItem()) ) {
 			try {
 				String temp = (String)versionModel.getElementAt(0);
 				versionModel.setSelectedItem(temp);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		versionModel.removeElement(DEFAULT_VERSION_NOT_AVAILABLE_STRING);
 	}
 
 	public void templateNodeUpdated(TemplateNodeEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public String getExternalSetValue() {
 
 		if ( getAnyRadioButton().isSelected() ) {
 			return Constants.NO_VERSION_INDICATOR_STRING;
 		} else {
 			return (String)(versionModel.getSelectedItem());
 		}
 		
 	}
 
 	public void setExternalSetValue(String version) {
 		
 		myLogger.warn("Not supported yet.");
 		throw new RuntimeException("Setting value not supported yet.");
 //		if ( infoObject.getAllAvailableVersionsForFqan(GrisuRegistry.getDefault().getEnvironmentSnapshotValues().getCurrentFqan()).contains(version) ) {
 //			//TODO set mode
 //			versionModel.setSelectedItem(version);
 //		}
 	}
 	
 
 	public void valueChanged(TemplateNodePanel panel, String newValue) {
 
 		if ( infoObject != null ) {
 		// submissionlocation has changed (only relevant if ANY-MODE is selected. Otherwise version won't change
 		if ( this.currentMode == ANY_VERSION_MODE ) {
 			versionModel.setSelectedItem(chooseBestVersion(newValue));
 		}
 		}
 		
 	}
 	
 	private String chooseBestVersion(String subLoc) {
 		// this would be a job for the metascheduler
 		Set<String> temp = infoObject.getAvailableVersions(subLoc);
 		
 		if ( temp != null && temp.size() > 0 ) {
 			return temp.iterator().next();
 		} else {
 			myLogger.error("No version found for this submissionLocation. This shouldn't happen.");
 			return null;
 		}
 	}
 	
 	public int getMode() {
 		return currentMode;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JRadioButton getExactRadioButton() {
 		if (exactRadioButton == null) {
 			exactRadioButton = new JRadioButton();
 			exactRadioButton.setText("Exact");
 			modeGroup.add(exactRadioButton);
 			exactRadioButton.setActionCommand(EXACT_MODE_STRING);
 			exactRadioButton.addActionListener(this);
 		}
 		return exactRadioButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JRadioButton getDefaultRadioButton() {
 		if (defaultRadioButton == null) {
 			defaultRadioButton = new JRadioButton();
 			defaultRadioButton.setText("Default");
 			modeGroup.add(defaultRadioButton);
 			defaultRadioButton.setActionCommand(DEFAULT_MODE_STRING);
 			defaultRadioButton.addActionListener(this);
 		}
 		return defaultRadioButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JRadioButton getAnyRadioButton() {
 		if (anyRadioButton == null) {
 			anyRadioButton = new JRadioButton();
 			anyRadioButton.setText("Any");
 			modeGroup.add(anyRadioButton);
 			anyRadioButton.setActionCommand(ANY_MODE_STRING);
 			anyRadioButton.addActionListener(this);
 		}
 		return anyRadioButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JComboBox getVersionComboBox() {
 		if (versionComboBox == null) {
 			versionComboBox = new JComboBox(versionModel);
 			versionComboBox.addItemListener(new ItemListener() {
 				public void itemStateChanged(final ItemEvent e) {
 					if ( !versionLocked ) {
 					if ( e.getStateChange() == ItemEvent.SELECTED ) {
 
 						if ( currentMode != ANY_VERSION_MODE ) {
 							String temp = (String)(versionModel.getSelectedItem());
 							fireVersionChanged(temp);
 						}
 						registry.getHistoryManager().addHistoryEntry(TemplateTagConstants.getGlobalLastVersionKey(infoObject.getApplicationName()), (String)(versionModel.getSelectedItem()));
 
 					}						
 					}
 				}
 			});
 			versionComboBox.setMaximumSize(new Dimension(300, 24));
 			versionComboBox.setEditable(false);
 		}
 		return versionComboBox;
 	}
 
 	public JPanel getTemplateNodePanel() {
 		return this;
 	}
 
 	public void reset() {
 		// TODO Auto-generated method stub
 
 	}
 
 	// event stuff
 	// ========================================================
 
 	private Vector<ValueListener> valueChangedListeners;
 
 	private void fireVersionChanged(String newValue) {
 		
 		// just in case the listener wasn't added yet...
 		getSubmissionLocationPanel();
 
 		myLogger.debug("Fire value changed event from Version: new value: " + newValue);
 		// if we have no mountPointsListeners, do nothing...
 		if (valueChangedListeners != null && !valueChangedListeners.isEmpty()) {
 
 			// make a copy of the listener list in case
 			// anyone adds/removes mountPointsListeners
 			Vector<ValueListener> valueChangedTargets;
 			synchronized (this) {
 				valueChangedTargets = (Vector<ValueListener>) valueChangedListeners
 						.clone();
 			}
 
 			// walk through the listener list and
 			// call the gridproxychanged method in each
 			Enumeration<ValueListener> e = valueChangedTargets.elements();
 			while (e.hasMoreElements()) {
 				ValueListener valueChanged_l = (ValueListener) e.nextElement();
 				valueChanged_l.valueChanged(this, newValue);
 			}
 		}
 	}
 
 	// register a listener
 	synchronized public void addValueListener(ValueListener l) {
 		if (valueChangedListeners == null)
 			valueChangedListeners = new Vector<ValueListener>();
 		valueChangedListeners.addElement(l);
 	}
 
 	// remove a listener
 	synchronized public void removeValueListener(ValueListener l) {
 		if (valueChangedListeners == null) {
 			valueChangedListeners = new Vector<ValueListener>();
 		}
 		valueChangedListeners.removeElement(l);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		switchMode(e.getActionCommand());
 	}
 
 	public void fqansChanged(FqanEvent event) {
 
 		String currentVersion = (String)versionModel.getSelectedItem();
 		
 		fillVersions(currentVersion);
 		fireVersionChanged((String)(versionModel.getSelectedItem()));
 	}
 
 
 }
