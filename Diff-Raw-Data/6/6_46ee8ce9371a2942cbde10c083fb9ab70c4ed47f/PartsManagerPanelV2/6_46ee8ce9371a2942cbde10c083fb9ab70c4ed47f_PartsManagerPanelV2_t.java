 package manager.panel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 
 import manager.PartsManager;
 import manager.util.OverlayPanel;
 import manager.util.WhiteLabel;
 import Utils.Constants;
 import factory.PartType;
 
 public class PartsManagerPanelV2 extends JPanel{
 	public static final Border PADDING = BorderFactory.createEmptyBorder(20, 20, 20, 20);
 	public static final Border FIELD_PADDING = BorderFactory.createEmptyBorder(5, 5, 5, 5);
 	public static final Border MEDIUM_PADDING = BorderFactory.createEmptyBorder(10, 10, 10, 10);
 	public static final Border BOTTOM_PADDING = BorderFactory.createEmptyBorder(0, 0, 20, 0);
 	public static final Border TOP_PADDING = BorderFactory.createEmptyBorder(20, 0, 5, 0);
 	public static final Border VERTICAL_PADDING = BorderFactory.createEmptyBorder(10, 0, 10, 0);
 	
 	JPanel panels;
 	OverlayPanel leftPanel;
	JScrollPane jsp;
 	PartsListPanel rightPanel;
 	
 	WhiteLabel leftTitle;
 	JTextField nameField;
 	JTextField numField;
 	JTextArea descField;
 	JButton submitButton;
 	
 	PartsManager manager;
 	
 	boolean isEditing;
 	boolean isDeleting;
 	
 	ArrayList<PartType> partTypes = new ArrayList<PartType>();
 	
 	public PartsManagerPanelV2(PartsManager mngr) {
 		manager = mngr;
 		partTypes = (ArrayList<PartType>) Constants.DEFAULT_PARTTYPES.clone();
 		
 		setLayout(new BorderLayout());
 		setBorder(PADDING);
 		
 		JLabel title = new WhiteLabel("Parts Manager");
 		title.setFont(new Font("Arial", Font.BOLD, 30));
 		title.setBorder(VERTICAL_PADDING);
 		add(title, BorderLayout.NORTH);
 		
 		panels = new JPanel(new GridLayout(1,2));
 		panels.setOpaque(false);
 		panels.setVisible(true);
 		add(panels);
 		
 		leftPanel = new OverlayPanel();
 		leftPanel.setVisible(true);
 		panels.add(leftPanel);
 		
 		rightPanel = new PartsListPanel(new PartsListPanel.PartsListPanelHandler() {
 			@Override
 			public void editPart(PartType pt) {
 				startEditing(pt);
 			}
 			@Override
 			public void deletePart(PartType pt) {
 				startDeleting(pt);
 			}
 		});
 		rightPanel.setVisible(true);
 		rightPanel.setBackground(new Color(0, 0, 0, 30));
 		
		jsp = new JScrollPane(rightPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		jsp.setOpaque(false);
 		jsp.getViewport().setOpaque(false);
 		panels.add(jsp);
 
 		setUpLeftPanel();
 	}
 
 	public void setUpLeftPanel() {
 		leftPanel.removeAll();
 		
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
 		leftPanel.setAlignmentX(LEFT_ALIGNMENT);
 		leftPanel.setBorder(PADDING);
 		
 		leftTitle = new WhiteLabel("Create a New Part");
 		leftTitle.setFont(new Font("Arial", Font.PLAIN, 20));
 		leftTitle.setLabelSize(300, 40);
 		leftTitle.setAlignmentX(0);
 		leftPanel.add(leftTitle);
 		
 		JPanel namePanel = new JPanel();
 		namePanel.setBorder(TOP_PADDING); 
 		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
 		namePanel.setOpaque(false);
 		namePanel.setVisible(true);
 		namePanel.setAlignmentX(0);
 		leftPanel.add(namePanel);
 		
 			WhiteLabel nameLabel = new WhiteLabel("Name");
 			nameLabel.setLabelSize(100, 25);
 			namePanel.add(nameLabel);
 			
 			nameField = new JTextField("name");
 			
 			nameField.setMaximumSize(new Dimension(200, 25));
 			nameField.setBorder(FIELD_PADDING);
 			namePanel.add(nameField);
 		
 		JPanel numPanel = new JPanel();
 		numPanel.setBorder(TOP_PADDING); 
 		numPanel.setLayout(new BoxLayout(numPanel, BoxLayout.LINE_AXIS));
 		numPanel.setOpaque(false);
 		numPanel.setVisible(true);
 		numPanel.setAlignmentX(0);
 		leftPanel.add(numPanel);
 		
 			WhiteLabel numLabel = new WhiteLabel("Part no.");
 			numLabel.setLabelSize(100, 25);
 			numPanel.add(numLabel);
 			
 			numField = new JTextField("23");
 			numField.setMaximumSize(new Dimension(200, 25));
 			numField.setBorder(FIELD_PADDING);
 			numPanel.add(numField);
 		
 		JPanel descPanel = new JPanel();
 		descPanel.setBorder(TOP_PADDING); 
 		descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.LINE_AXIS));
 		descPanel.setOpaque(false);
 		descPanel.setVisible(true);
 		descPanel.setAlignmentX(0);
 		leftPanel.add(descPanel);
 		
 			WhiteLabel descLabel = new WhiteLabel("Description");
 			descLabel.setLabelSize(100, 25);
 			descPanel.add(descLabel);
 			
 			descField = new JTextArea("Description...");
 			descField.setMinimumSize(new Dimension(200, 100));
 			descField.setMaximumSize(new Dimension(200, 100));
 			descField.setPreferredSize(new Dimension(200, 100));
 			descField.setBorder(FIELD_PADDING);
 			descPanel.add(descField);
 		
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setBorder(TOP_PADDING); 
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
 		buttonPanel.setOpaque(false);
 		buttonPanel.setVisible(true);
 		buttonPanel.setAlignmentX(0);
 		leftPanel.add(buttonPanel);
 		
 			if(isEditing || isDeleting) {
 				JButton cancelButton = new JButton("Cancel");
 				cancelButton.setMinimumSize(new Dimension (100, 25));
 				cancelButton.setMaximumSize(new Dimension (100, 25));
 				cancelButton.setPreferredSize(new Dimension (100, 25));
 				cancelButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent ae) {
 						restoreLeftPanel();
 					}
 				});
 				buttonPanel.add(cancelButton);
 			} else {
 				WhiteLabel fakeLabel = new WhiteLabel("");
 				fakeLabel.setLabelSize(100, 25);
 				buttonPanel.add(fakeLabel);
 			}
 			
 			submitButton = new JButton("Submit >");
 			submitButton.setMinimumSize(new Dimension (200, 25));
 			submitButton.setMaximumSize(new Dimension (200, 25));
 			submitButton.setPreferredSize(new Dimension (200, 25));
 			submitButton.setAlignmentX(0);
 			buttonPanel.add(submitButton);
 			
 			removeAllActionListener(submitButton);
 			submitButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					manager.createPart(new PartType(
 							nameField.getText(),
 							Integer.parseInt(numField.getText()),
 							descField.getText()
 					));
 					restoreLeftPanel();
 				}
 			});
 	}
 	
 	public void updatePartTypes(ArrayList<PartType> pt) {
 		rightPanel.updatePartTypes(pt);
		jsp.validate();
 	}
 	
 	public void startEditing(final PartType pt) {
 		isEditing = true;
 		isDeleting = false;
 		setUpLeftPanel();
 		
 		leftTitle.setText("Editing a Part");
 		nameField.setText(pt.getName());
 		numField.setText(String.valueOf(pt.getPartNum()));
 		descField.setText(pt.getDescription());
 		submitButton.setText("Edit >");
 		
 		removeAllActionListener(submitButton);
 		submitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Edit name: " + nameField.getText());
 				pt.setName(nameField.getText());
 				pt.setPartNum(Integer.parseInt(numField.getText()));
 				pt.setDescription(descField.getText());
 				
 				manager.editPart(pt);
 				restoreLeftPanel();
 			}
 		});
 	}
 	
 	public void startDeleting(final PartType pt) {
 		isEditing = false;
 		isDeleting = true;
 		setUpLeftPanel();
 		
 		leftTitle.setText("Deleting a Part");
 		nameField.setEnabled(false);
 		numField.setEnabled(false);
 		descField.setEnabled(false);
 		submitButton.setText("Delete >");
 		
 		removeAllActionListener(submitButton);
 		submitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				manager.deletePart(pt);
 				restoreLeftPanel();
 			}
 		});
 	}
 	
 	public void restoreLeftPanel() {
 		isEditing = false;
 		isDeleting = false;
 		setUpLeftPanel();
 		
 		rightPanel.restoreColors();
 		
 		validate();
 	}
 	
 	@Override
 	public void paintComponent(Graphics gg) {
 		Graphics2D g = (Graphics2D) gg;
 		g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);
 	}
 	
 	/**
 	 * Removes all action listeners from a button. 
 	 * TODO consider moving this out, or starting own JButton subclass.
 	 */
 	public void removeAllActionListener(JButton button) {
 		for(ActionListener al : button.getActionListeners()) {
 			button.removeActionListener(al);
 		}
 	}
 }
