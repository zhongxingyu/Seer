 package org.vpac.grisu.client.view.swing.template.panels;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.Vector;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.MatteBorder;
 import javax.swing.border.TitledBorder;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.model.template.nodes.TemplateNode;
 import org.vpac.grisu.client.model.template.nodes.TemplateNodeEvent;
 import org.vpac.historyRepeater.HistoryManager;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 abstract class AbstractInputPanel extends JPanel implements TemplateNodePanel {
 	
 	static final Logger myLogger = Logger.getLogger(AbstractInputPanel.class.getName());
 	
 	private JTextArea textArea;
 	private JScrollPane scrollPane;
 	public static final String COMBOBOX_PANEL = "combobox";
 	public static final String TEXTFIELD_PANEL = "textfield";
 
 	private JPanel inputField;
 	private JLabel requiredLabel;
 	private JButton genericButton;
 	private JLabel errorLabel;
 
 	protected ComponentHolder holder = null;
 	
 	protected boolean useHistory = false;
 	protected boolean useLastInput = false;
 	protected boolean locked = false;
 	
 	FormLayout layout = null;
 
 	protected HistoryManager historyManager = null;
 	String historyManagerKeyForThisNode = null;
 
 	protected TemplateNode templateNode = null;
 	
 	int defaultHeight = 120;
 	int heightDelta = 0;
 
 	/**
 	 * Create the panel
 	 */
 	public AbstractInputPanel() {
 		super();
 		layout = new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("44dlu:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("41dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("5dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("17dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC});
 		setLayout(layout);
 		add(getErrorLabel(), new CellConstraints(2, 4));
 		add(getRequiredLabel(), new CellConstraints(6, 4));
 		//
 	}
 
 	public void setTemplateNode(TemplateNode node)
 			throws TemplateNodePanelException {
 		
 		this.templateNode = node;
 		this.templateNode.setTemplateNodeValueSetter(this);
 		node.addTemplateNodeListener(this);
 		
 		setBorder(new TitledBorder(null, this.templateNode.getTitle(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 
 
 		if ("1".equals(this.templateNode.getMultiplicity())) {
 			getRequiredLabel().setText("*");
 		} else {
 			getRequiredLabel().setText("");
 		}
 		
 		String description = this.templateNode.getDescription();
 		if ( ! this.templateNode.hasProperty(TemplateNode.HIDE_DESCRIPTION) && description != null && !"".equals(description) ) {
 			getTextArea().setText(description);
 			add(getScrollPane(), new CellConstraints(2, 2, 5, 1, CellConstraints.FILL, CellConstraints.FILL));
 		} else {
 			// don't know
 			layout.setRowSpec(1, RowSpec.decode("0dlu"));
 			layout.setRowSpec(2, RowSpec.decode("0dlu"));
 			heightDelta = -40;
 		}
 		
 		historyManager = this.templateNode.getTemplate()
 		.getEnvironmentManager().getHistoryManager();
 		
 		historyManagerKeyForThisNode = this.templateNode.getOtherProperty(TemplateNode.HISTORY_KEY);
 		if ( historyManagerKeyForThisNode == null ) {
 			historyManagerKeyForThisNode = this.templateNode.getName();
 		}
 		
 		if ( this.templateNode.getOtherProperties().containsKey(TemplateNode.LAST_USED_PARAMETER) ) {
 			useLastInput = true;
 		} else {
 			useLastInput = false;
 		}
 		
 		if ( this.templateNode.getOtherProperties().containsKey(TemplateNode.LOCKED_KEY) ) {
 			locked = true;
 		} else {
 			locked = false;
 		}
 		
 		if (this.templateNode.getOtherProperties().containsKey(
 				TemplateNode.USE_HISTORY)) {
 			useHistory = true;
 
 
 			String maxString = this.templateNode
 					.getOtherProperty(TemplateNode.USE_HISTORY);
 			
 			
 			if (!TemplateNode.NON_MAP_PARAMETER.equals(maxString)) {
 				int maxValues = Integer.parseInt(maxString);
 				historyManager.setMaxNumberOfEntries(historyManagerKeyForThisNode, maxValues);
 			}
 
 		} else {
 			useHistory = false;
 		}
 		
 		preparePanel();
 		String buttonText = genericButtonText();
 		initialize(getComponentHolder(), genericButtonText());
 		setupComponent();
 	}
 
 	abstract protected ComponentHolder getComponentHolder();
 	abstract protected void preparePanel();
 	abstract protected void setupComponent();
 	
 	/**
 	 * Specify the text of the button. If you specify null, no button is rendered.
 	 * @return the button text
 	 */
 	abstract protected String genericButtonText();
 	/**
 	 * This can be an empty method if initialized is called with showButton=false.
 	 */
 	abstract protected void buttonPressed();
 
 	/**
 	 * Call this in your constructor after you sorted out which component to
 	 * render.
 	 * 
 	 * @param showButton
 	 *            whether to render a button or not
 	 */
 	private void initialize(ComponentHolder holder, String buttonText) {
 
 //		Component comp = getInputComponent();
 		this.holder = holder;
 		getInputField().add(holder.getComponent(), BorderLayout.CENTER);
 
 		String heightComponentHolderRow = holder.getRowSpec()+"dlu";
 		layout.setRowSpec(6, RowSpec.decode(heightComponentHolderRow));
 		
 		if ( buttonText != null ) {
 			getGenericButton().setText(buttonText);
 			add(getGenericButton(), new CellConstraints(4, 6, 3, 1, CellConstraints.DEFAULT, CellConstraints.TOP));
 			add(getInputField(), new CellConstraints(2, 6,
 					CellConstraints.FILL, CellConstraints.TOP));
 		} else {
 			add(getInputField(), new CellConstraints(2, 6, 5, 1,
 					CellConstraints.FILL, CellConstraints.TOP));
 		}
 
 	}
 
 	protected JComboBox createJComboBox() {
 
 		setPreferredSize(new Dimension(300, defaultHeight+heightDelta));
 
 		if ( this.templateNode == null ) {
 			throw new RuntimeException("AbstractInputPanel not ready yet.");
 		}
 
 		DefaultComboBoxModel comboboxModel = new DefaultComboBoxModel();
 
 		JComboBox combobox = new JComboBox(comboboxModel);
 //		combobox.setRenderer(new MyComboboxListCellRenderer(SwingConstants.TRAILING));
 		if (locked) {
 			combobox.setEditable(false);
 		} else {
 			combobox.setEditable(true);
 		}
 
 		// can't use fillComboBox & setdefaultvalue yet
 		
 		comboboxModel.removeAllElements();
 		for (String prefill : getPrefills())
 			comboboxModel.addElement(prefill);
 
 		
 		String lastUsedString = null;
 		try {
 			lastUsedString = historyManager.getEntries(historyManagerKeyForThisNode+"_"+TemplateNode.LAST_USED_PARAMETER).get(0);
 		} catch (Exception e) {
 		}
 		
 		String defaultValue = null;
 		if ( lastUsedString != null && ! "".equals(lastUsedString) ) {
 			defaultValue = lastUsedString;
 		} else {
 			defaultValue = getDefaultValue();
 		}
 
 		if (defaultValue != null) {
 			combobox.setSelectedItem(defaultValue);
 		} else {
 			combobox.setSelectedItem(null);
 		}
 
 		return combobox;
 	}
 	
 
 	protected void setDefaultValue() {
 		
 		
 		if ( useLastInput ) {
 			String lastUserInput = holder.getExternalSetValue();
 			if ( lastUserInput != null && !"".equals(lastUserInput)) 
 				historyManager.addHistoryEntry(historyManagerKeyForThisNode+"_"+TemplateNode.LAST_USED_PARAMETER, lastUserInput, new Date(), 1);
 		}
 		
 		
 		String defaultValue = getDefaultValue();
 		if (defaultValue != null) {
 
 			holder.setComponentField(defaultValue);
		} else {
			holder.setComponentField(null);
 		}
 	}
 	
 	protected void fillComboBox() {
 		
 		JComboBox combobox = (JComboBox)holder.getComponent();
 		((DefaultComboBoxModel)combobox.getModel()).removeAllElements();
 		for (String prefill : getPrefills())
 			((DefaultComboBoxModel)combobox.getModel()).addElement(prefill);
 		
 	}
 	
 	
 	protected JTextField createJTextField() {
 
 		// the whole panel doesn't need to be that big in this case...
 		setPreferredSize(new Dimension(300, defaultHeight+heightDelta-20));
 
 		
 		if ( this.templateNode == null ) {
 			throw new RuntimeException("AbstractInputPanel not ready yet.");
 		}
 		
 		JTextField textField = new JTextField();
 		
 		if (locked) {
 			textField.setEditable(false);
 		} else {
 			textField.setEditable(true);
 		}
 		
 		// can't use setDefaultValueYet
 		String lastUsedString = null;
 		try {
 			lastUsedString = historyManager.getEntries(historyManagerKeyForThisNode+"_"+TemplateNode.LAST_USED_PARAMETER).get(0);
 		} catch (Exception e) {
 		}
 		String defaultValue = null;
 		if ( lastUsedString != null && ! "".equals(lastUsedString) ) {
 			defaultValue = lastUsedString;
 		} else {
 			defaultValue = getDefaultValue();
 		}
 
 		if (defaultValue != null) {
 			textField.setText(defaultValue);
 		} else {
 			textField.setText(null);
 		}
 		return textField;
 	}
 
 	protected String getDefaultValue() {
 		
 		if ( templateNode.getOtherProperties().containsKey(TemplateNode.LAST_USED_PARAMETER) ) {
 
 			String lastUsedString = null;
 			try {
 				lastUsedString = historyManager.getEntries(historyManagerKeyForThisNode+"_"+TemplateNode.LAST_USED_PARAMETER).get(0);
 			} catch (Exception e) {
 			}
 			if ( lastUsedString != null && ! "".equals(lastUsedString) ) 
 				return lastUsedString;
 			
 		}
 		
 		String defaultValue = templateNode.getDefaultValue();
 
 		if (defaultValue != null && !"".equals(defaultValue)) {
 			return defaultValue;
 		} else {
 			return null;
 		}
 	}
 
 	protected LinkedList<String> getPrefills() {
 
 		LinkedList<String> prefillStrings = new LinkedList<String>();
 
 		if (useHistory
 				&& historyManager != null
 				&& historyManager.getEntries(historyManagerKeyForThisNode)
 						.size() > 0) {
 			for (String entry : historyManager.getEntries(historyManagerKeyForThisNode)) {
 				prefillStrings.addFirst(entry);
 			}
 		}
 
 		if (templateNode.getPrefills() != null) {
 			for (String prefill : templateNode.getPrefills()) {
 				if (prefill != null && !"".equals(prefill))
 					prefillStrings.add(prefill);
 			}
 		}
 
 		return prefillStrings;
 
 	}
 	
 	public String getExternalSetValue() {
 		return holder.getExternalSetValue();
 	}
 	
 	public void setExternalSetValue(String value) {
 		holder.setComponentField(value);
 	}
 	
 	public JPanel getTemplateNodePanel() {
 		return this;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getErrorLabel() {
 		if (errorLabel == null) {
 			errorLabel = new JLabel();
 			errorLabel.setForeground(Color.RED);
 		}
 		return errorLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getGenericButton() {
 		if (genericButton == null) {
 			genericButton = new JButton();
 			genericButton.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					buttonPressed();
 				}
 			});
 			genericButton.setText("genericButton");
 		}
 		return genericButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getRequiredLabel() {
 		if (requiredLabel == null) {
 			requiredLabel = new JLabel();
 		}
 		return requiredLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JPanel getInputField() {
 		if (inputField == null) {
 			inputField = new JPanel();
 			inputField.setLayout(new BorderLayout());
 		}
 		return inputField;
 	}
 	
 	public void templateNodeUpdated(TemplateNodeEvent event) {
 
 			if ( event.getEventType() == TemplateNodeEvent.RESET ) {
 				reset();
 			} else if (event.getEventType() == TemplateNodeEvent.TEMPLATE_PROCESSED_INVALID
 				|| event.getEventType() == TemplateNodeEvent.TEMPLATE_FILLED_INVALID) {
 			String message = event.getMessage();
 			if (message == null)
 				message = TemplateNodeEvent.DEFAULT_PROCESSED_INVALID_MESSAGE;
 			layout.setRowSpec(4, new RowSpec("10dlu"));
 			errorLabel.setText(message);
 			errorLabel.setToolTipText(message);
 			errorLabel.setVisible(true);
 
 			getRequiredLabel().setForeground(Color.RED);
 
 		} else if (event.getEventType() == TemplateNodeEvent.TEMPLATE_PROCESSED_VALID) {
 			errorLabel.setVisible(false);
 			layout.setRowSpec(4, new RowSpec("4dlu"));
 			getRequiredLabel().setForeground(Color.BLACK);
 		} 
 
 	}
 	/**
 	 * @return
 	 */
 	protected JScrollPane getScrollPane() {
 		if (scrollPane == null) {
 			scrollPane = new JScrollPane();
 			scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
 			scrollPane.setBackground(UIManager.getColor("Panel.background"));
 			scrollPane.setViewportView(getTextArea());
 		}
 		return scrollPane;
 	}
 	/**
 	 * @return
 	 */
 	protected JTextArea getTextArea() {
 		if (textArea == null) {
 			textArea = new JTextArea();
 			textArea.setBorder(new EmptyBorder(0, 0, 0, 0));
 			textArea.setOpaque(false);
 			textArea.setMinimumSize(new Dimension(100, 0));
 			textArea.setWrapStyleWord(true);
 			textArea.setBackground(UIManager.getColor("Panel.background"));
 			textArea.setMargin(new Insets(5,7,5,7));
 			textArea.setLineWrap(true);
 			textArea.setEditable(false);
 		}
 		return textArea;
 	}
 	
 	public String toString() {
 		return getName();
 	}
 	
 	// event stuff
 	// ========================================================
 	
 	private Vector<ValueListener> valueChangedListeners;
 
 	private void fireSitePanelEvent(String newValue) {
 		
 		myLogger.debug("Fire value changed event: new value: "+newValue);
 		// if we have no mountPointsListeners, do nothing...
 		if (valueChangedListeners != null && !valueChangedListeners.isEmpty()) {
 
 			// make a copy of the listener list in case
 			// anyone adds/removes mountPointsListeners
 			Vector<ValueListener> valueChangedTargets;
 			synchronized (this) {
 				valueChangedTargets = (Vector<ValueListener>) valueChangedListeners.clone();
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
 	
 
 }
