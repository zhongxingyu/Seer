 package mainGUI;
 
 import guiElements.CustomAlternativeDialog;
 import guiElements.ExistingAlternativeDialog;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.util.Collection;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.text.JTextComponent;
 
 import dataStructures.AbstractDocument;
 import dataStructures.Alternative;
 import dataStructures.Member;
 import dataStructures.Role;
 import dataStructures.maps.MemberMap;
 import dataStructures.maps.RoleMap;
 
 /**
  * The ViewResultsPane is an UpdatePane which compiles all previous
  * input, queries the PreferenceReasoner back-end, and displays
  * the query results.
  */
 @SuppressWarnings("serial")
 public abstract class ViewResultsPane extends UpdatePane implements ActionListener {
 	public static final int CONSISTENCY = 0;
 	public static final int DOMINANCE = 1;
 	public static final int TOP = 2;
 	public static final int NEXT = 3;
 	
 	protected AbstractDocument document;
 	protected AbstractPaneTurner paneTurner;
 	protected JFrame parentFrame;
 	protected reasoner.PreferenceReasoner reasoner;
 	protected boolean reasonerInitialized;
 	
 	private JPanel dominancePanel;
 	private JButton dominanceButton;
 	private JTextField leftDominanceSet;
 	private JTextField rightDominanceSet;
 	protected JTextField dominanceField;
 	//private AlternativeList alreadyChosen;
 	protected Alternative leftAlternative;
 	protected Alternative rightAlternative;
 	
 	protected JTextField consistencyField;
 	private JButton consistencyButton;
 	
 	private JButton topNextButton;
 	protected JTextArea resultsField;
 	protected int currentResult = 1;
 	
 	//private JPanel justificationPanel;
 	//private JTextArea justificationField;
 
 	JPanel stakeholderPanel;
 	private JComboBox stakeholderBox;
 	private Member curMember;
 	private boolean allMembers;
 	
 	/**
 	 * Create new ViewResultsPane instance
 	 * @param document
 	 * @param parentFrame
 	 */
 	public ViewResultsPane(AbstractDocument document, JFrame parentFrame,
 			AbstractPaneTurner paneTurner) {
 		this.document = document;
 		this.parentFrame = parentFrame;
 		this.paneTurner = paneTurner;
 		this.reasonerInitialized = false;
 		//this.alreadyChosen = new AlternativeList();
 		this.add(initializeGUI());
 		setVisible(true);
 	}
 	
 	/**
 	 * Setup GUI
 	 * @return JPanel
 	 */
 	private JPanel initializeGUI() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 
 		stakeholderPanel = new JPanel();
 		
 		JPanel consistencyPanel = createConsistencyPanel();
 	
 		JPanel resultsPanel = createResultsPanel();
 		
 		dominancePanel = createDominancePanel();
 		
 		//JPanel justificationPanel = createJustificationPanel();
 
 		update();
 		panel.add(stakeholderPanel);
 		panel.add(Box.createRigidArea(new Dimension(5,35)));
 		panel.add(consistencyPanel);
 		panel.add(Box.createRigidArea(new Dimension(5,35)));
 		panel.add(dominancePanel);
 		panel.add(Box.createRigidArea(new Dimension(5,35)));
 		//panel.add(justificationPanel);
 		panel.add(resultsPanel);
 		
 		return panel;
 	}
 	
 	@Override
 	public void update() {
 		
 		RoleMap rm = document.getRoleMap();
 		
 		if (rm.isMultipleStakeholder()) {
 			stakeholderPanel.removeAll();
 			setupStakeholderBox();
 			stakeholderPanel.add(stakeholderBox);
 		} else {
 			// set curMember to default member
 			curMember = rm.get(0).getObject().get(0); 
 			allMembers = false;
 		}
 		
 		if(!paneTurner.isInitializing() && curMember != null && curMember.getPreferenceFilePath() != null) {
 			initReasoner(curMember.getPreferenceFilePath());
 		} else if (!paneTurner.isInitializing()) { // do not display errors if paneTurner is initializing
 			if(curMember == null){
 				displayReasonerInitError("There are no stakeholders in the project.");
 				reasonerInitialized = false;
 			} else {
 				displayReasonerInitError("The current stakeholder has no preference file.");
 				reasonerInitialized = false;
 			}
 		}
 		
 		resetResultFields();
 		currentResult = 1;
 		parentFrame.pack();
 	}
 	
 	protected void displayReasonerInitError(String error) {
 		JOptionPane.showMessageDialog(parentFrame,
			    "The reasoner was not initialized:\n" +
 			    error,
 			    "Error initializing preference reasoner",
 			    JOptionPane.WARNING_MESSAGE);
 	}
 	
 	protected void displayReasonerError(String error) {
 		JOptionPane.showMessageDialog(parentFrame,
			    "An error occured while attempting to run the preference reasoner:\n" +
 			    error,
 			    "Preference Reasoner Error",
 			    JOptionPane.WARNING_MESSAGE);
 	}
 	
 	/**
 	 * Create the consistency panel
 	 * @return JPanel
 	 */
 	private JPanel createConsistencyPanel() {
 		JPanel consistencyPanel = new JPanel();
 		consistencyPanel.setLayout(new BoxLayout(consistencyPanel, BoxLayout.Y_AXIS));
 		
 		JPanel labelButtonPanel = new JPanel();
 		
 		consistencyButton = new JButton("Consistency");
 		consistencyButton.addActionListener(this);
 		consistencyButton.setToolTipText("Verify all preference statements are consistent");
 		
 		labelButtonPanel.add(new JLabel("Check Project for Consistency:"));
 		labelButtonPanel.add(consistencyButton);
 		
 		consistencyField=new JTextField("result");
 		consistencyField.setEditable(false);
 		/*consistencyField.setToolTipText("click to view justification");
 		consistencyField.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				showJustificationPanel(true);	
 			}
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 
 			@Override
 			public void mouseExited(MouseEvent e) {}
 
 			@Override
 			public void mousePressed(MouseEvent e) {}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {}
 		});*/
 		
 		consistencyPanel.add(labelButtonPanel);
 		consistencyPanel.add(Box.createRigidArea(new Dimension(5,5)));
 		consistencyPanel.add(consistencyField);
 		return consistencyPanel;
 	}
 	
 	/**
 	 * Create Dominance Panel
 	 * @return JPanel
 	 */
 	private JPanel createDominancePanel() {
 		JPanel labelButtonPanel = new JPanel();
 		JLabel label = new JLabel("Determine Set Dominance:");
 		dominanceButton = new JButton("Dominance");
 		dominanceButton.addActionListener(this);
 		dominanceButton.setToolTipText("Determine which set is more preferred");
 		
 		labelButtonPanel.add(label);
 		labelButtonPanel.add(dominanceButton);
 		
 		JPanel dominancePanel = new JPanel();
 		dominancePanel.setLayout(new BoxLayout(dominancePanel, BoxLayout.Y_AXIS));
 
 		leftDominanceSet= new JTextField("{}");
 		leftDominanceSet.setEditable(false);
 		leftDominanceSet.addMouseListener(new AlternativeListener(leftDominanceSet, leftAlternative, "left"));
 		leftDominanceSet.setAlignmentX(Component.CENTER_ALIGNMENT);
 		
 		JLabel greaterThan = new JLabel(">");
 		greaterThan.setAlignmentX(Component.CENTER_ALIGNMENT);
 		
 		rightDominanceSet=new JTextField("{}");
 		rightDominanceSet.setEditable(false);
 		rightDominanceSet.addMouseListener(new AlternativeListener(rightDominanceSet, rightAlternative, "right"));
 		rightDominanceSet.setAlignmentX(Component.CENTER_ALIGNMENT);
 		
 		dominanceField=new JTextField("result");
 		dominanceField.setEditable(false);
 		dominanceField.setAlignmentX(Component.CENTER_ALIGNMENT);
 		
 		dominancePanel.add(labelButtonPanel);
 		dominancePanel.add(leftDominanceSet);
 		dominancePanel.add(greaterThan);
 		dominancePanel.add(rightDominanceSet);
 		dominancePanel.add(Box.createRigidArea(new Dimension(5,5)));
 		dominancePanel.add(dominanceField);
 		
 		return dominancePanel;
 	}
 	
 	/**
 	 * Create Results Panel
 	 * @return JPanel
 	 */
 	private JPanel createResultsPanel() {
 		JPanel resultsPanel = new JPanel();
 		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
 		
 		JPanel labelButtonPanel = new JPanel();
 		
 		JLabel label = new JLabel("Retrieve Most Preferred Results:");
 		
 		topNextButton = new JButton("Top");
 		topNextButton.addActionListener(this);
 		topNextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 		topNextButton.setToolTipText("Find most preferred result");
 		
 		labelButtonPanel.add(label);
 		labelButtonPanel.add(topNextButton);
 		
 		JPanel innerPanel = new JPanel();
 		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
 		
 		JTextField resultLabel = new JTextField("Results");
 		resultLabel.setEditable(false);
 		resultLabel.setPreferredSize(new Dimension(75, 50));
 		
 		resultsField = new JTextArea(3,38);
 		resultsField.setEditable(false);
 		resultsField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
 		
 		JScrollPane resultScrollPane = new JScrollPane(resultsField);
 		resultScrollPane.setVerticalScrollBarPolicy(
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		resultScrollPane.setHorizontalScrollBarPolicy(
                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		resultScrollPane.setPreferredSize(new Dimension(400,80));
 		
 		innerPanel.add(resultLabel);
 		innerPanel.add(resultScrollPane);
 		
 		resultsPanel.add(Box.createRigidArea(new Dimension(10, 10)));
 		resultsPanel.add(labelButtonPanel);
 		resultsPanel.add(Box.createRigidArea(new Dimension(10, 10)));
 		resultsPanel.add(innerPanel);
 		resultsPanel.add(Box.createRigidArea(new Dimension(10, 10)));
 		return resultsPanel;
 	}
 	
 	protected void addResult(String resultSet) {
 		String oldText;
 		
 		if(currentResult == 1)
 			oldText = "";
 		else
 			oldText = resultsField.getText();
 		
 		oldText += currentResult++ + ". " + resultSet + "\n";
 		resultsField.setText(oldText);
 	}
 	
 	protected void addEndOfResults() {
 		String oldText;
 		oldText = resultsField.getText();
 		
 		oldText += "--End of Results--";
 		resultsField.setText(oldText);
 	}
 	
 	/*
 	 * create Justification Panel
 	 * @return JPanel
 	 */
 	/*private JPanel createJustificationPanel() {
 		justificationPanel = new JPanel();
 		justificationPanel.setLayout(new BoxLayout(justificationPanel, BoxLayout.Y_AXIS));
 		
 		JPanel innerPanel = new JPanel();
 		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
 		
 		JTextField justificationLabel = new JTextField("Justification");
 		justificationLabel.setEditable(false);
 		justificationLabel.setPreferredSize(new Dimension(75, 50));
 		
 		justificationField = new JTextArea(3,38);
 		justificationField.setEditable(false);
 		justificationField.setPreferredSize(new Dimension(400, 50));
 		justificationField.setText("result justification");
 		justificationField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
 		
 		JButton hide = new JButton("hide justification");
 		hide.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showJustificationPanel(false);
 			}
 		});
 		
 		innerPanel.add(justificationLabel);
 		innerPanel.add(justificationField);
 		
 		justificationPanel.add(Box.createRigidArea(new Dimension(10,10)));
 		justificationPanel.add(innerPanel);
 		justificationPanel.add(Box.createRigidArea(new Dimension(10,5)));
 		justificationPanel.add(hide);
 		justificationPanel.setVisible(false);
 		
 		return justificationPanel;
 	}
 	
 	private void showJustificationPanel(boolean show) {
 		justificationPanel.setVisible(show);
 	}*/
 	
 	/**
 	 * Clear results fields
 	 */
 	private void resetResultFields() {
 		consistencyField.setText("result");
 		dominanceField.setText("result");
 		topNextButton.setText("Top");
 		resultsField.setText("");
 	}
 	
 	/**
 	 * Setup the combobox containing role members
 	 * -- only used in multistakeholder
 	 */
 	private void setupStakeholderBox() {
 		RoleMap rm = document.getRoleMap();
 		Role[] roles = (Role[]) rm.values().toArray(new Role[0]);
 		Vector<Member> allMembers = new Vector<Member>();
 		for(Role role: roles) {
 			MemberMap members = role.getObject();
 			if (members != null) {
 				Collection<Member> roleMembers = members.values();
 				allMembers.addAll(roleMembers);
 
 			}
 		}
 		
 		stakeholderBox = new JComboBox(allMembers);
 		//stakeholderBox.insertItemAt("All Members", 0);
 		stakeholderBox.addActionListener(this);
 		stakeholderBox.invalidate();
 		
 		if(stakeholderBox.getItemAt(0) != null)
 			stakeholderBox.setSelectedIndex(0); // select All Members (for now, first member)
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object source = e.getSource();
 		if (source == consistencyButton) {
 			sendQuery(CONSISTENCY);
 		} else if (source == dominanceButton) {
 			sendQuery(DOMINANCE);
 		} else if (source == topNextButton) {
 			if(topNextButton.getText().contains("Top")){
 				topNextButton.setText("Next");
 				sendQuery(TOP);
 			}else{
 				sendQuery(NEXT);
 			}
 		} else if (source == stakeholderBox) {
 			Object selectedItem = stakeholderBox.getSelectedItem();
 			if (selectedItem instanceof Member) {
 				curMember = (Member) selectedItem;
 				
 				if(curMember.getPreferenceFilePath() != null) {
 					initReasoner(curMember.getPreferenceFilePath());
 				} else {
 					reasoner = null; //reasoner must be set to null so that results are not displayed or previous member
 					reasonerInitialized = false;
 					displayReasonerInitError("The current stakeholder has no preference file.");
 				}
 				
 				allMembers = false;
 			} else if (selectedItem.equals("All Members")) {
 				curMember = null;
 				allMembers = true;
 			}
 			resetResultFields();
 		}
 	}
 	
 	protected abstract void initReasoner(String prefXml);
 	protected abstract void dominance();
 	protected abstract void topNext();
 	protected abstract File xmlToText(File prefXml);
 	protected abstract String getVariableSet(String line);
 	protected abstract Alternative getAlternative(String variableSet);
 	protected abstract void checkConsistency();
 	
 	/**
 	 * Send Query to back end
 	 * @param query type
 	 */
 	private void sendQuery(int type) {
 		switch (type) {
 		case CONSISTENCY:
 			System.out.println("send consistency query");
 			checkConsistency();
 			break;
 			
 		case DOMINANCE:
 			System.out.println("send dominance query");
 			dominance();
 			break;
 		
 		case TOP:
 			topNext();
 			break;
 		
 		case NEXT:
 			topNext();
 			break;
 		
 		default:
 			System.err.println("In ViewResultsPaneCI - sendQuery triggered by invalid type integer");
 			break;
 		}
 		parentFrame.pack();
 	}
 
 	class AlternativeListener implements MouseListener {
 		private JTextComponent field;
 		private Alternative singleAlternative;
 		private String side;
 		public AlternativeListener(JTextComponent field, Alternative singleAlternative, String side) {
 			this.field=field;
 			this.singleAlternative = singleAlternative;
 			this.side = side;
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			if(document.getAlternativeMap().useEntireAlternativeSpace()){
 				CustomAlternativeDialog dialog = new CustomAlternativeDialog(parentFrame,document.getAttributeMap(),singleAlternative);
 				singleAlternative = dialog.getAlternative();
 				field.setText(singleAlternative.toExpandedString(document.getAttributeMap()));
 				field.setToolTipText(singleAlternative.toExpandedString(document.getAttributeMap()));
 			}else{
 				ExistingAlternativeDialog dialog = new ExistingAlternativeDialog(parentFrame,document.getAlternativeMap(),singleAlternative);
 				singleAlternative = dialog.getAlternative();
 				field.setText(singleAlternative.toString());
 				field.setToolTipText(singleAlternative.toExpandedString(document.getAttributeMap()));
 			}
 			
 			if(side.equals("right")) {
 				rightAlternative = singleAlternative;
 			} else {
 				leftAlternative = singleAlternative;
 			}
 			parentFrame.pack();
 		}
 
 		public void mouseEntered(MouseEvent e) {}
 
 		public void mouseExited(MouseEvent e) {}
 
 		public void mousePressed(MouseEvent e) {}
 
 		public void mouseReleased(MouseEvent e) {}
 	}
 }
