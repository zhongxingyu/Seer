 package framework;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.border.EtchedBorder;
 
 import org.jbox2d.common.Vec2;
 
 import utility.TextFieldWithPlaceHolder;
 
 import components.BlockShape;
 
 import exceptions.ElementExistsException;
 import exceptions.ElementNotExistException;
 
 @SuppressWarnings("serial")
 public class EditShapeWindowSidePanel extends JPanel{
 
 	final EditShapeWindow editShapeWindow;
 	final EditShapeWindowBuildPanel buildPanel;
 
 	public static final int SIDE_PANEL_WIDTH = EditShapeWindowBuildPanel.SHAPE_WIN_SIZE/2;
 	public static final int SIDE_PANEL_HEIGHT = EditShapeWindowBuildPanel.SHAPE_WIN_SIZE;
 
 	private TextFieldWithPlaceHolder shapeNameField;
 	private JComboBox<Vec2> resolution;
 	private int currentResolutionSelection;
 	private JButton colorButton;
 	private JButton clearButton;
 	private static JButton saveButton;
 	private static JButton closeButton;
 	private BlockShape shape;
 
 	public EditShapeWindowSidePanel(EditShapeWindow editShapeWindow, EditShapeWindowBuildPanel buildPanel, BlockShape shape){
 		this.editShapeWindow = editShapeWindow;
 		this.buildPanel = buildPanel;
 		this.shape = shape.clone();
 
 		this.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH,SIDE_PANEL_HEIGHT-5));
 		initComponents();
 		addListeners();
 
 	}
 
 	private void initComponents(){
 		setLayout(null);
 
 		JPanel controlPanel = new JPanel();
 		controlPanel.setLayout(null);
 		controlPanel.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.LOWERED),
 				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
 		controlPanel.setBounds(5,5,190,385);
 
 		JLabel nameLabel = new JLabel("Shape Name:");
 		nameLabel.setBounds(10, 5, 170, 25);
 		controlPanel.add(nameLabel);
 
 		// Shape name field
 		shapeNameField = new TextFieldWithPlaceHolder(shape.getShapeName());
 		shapeNameField.setBounds(10,30, 170, 25);
 		controlPanel.add(shapeNameField);
 
 		JLabel resolutionLabel = new JLabel("Grid resolution:");
 		resolutionLabel.setBounds(10,55, 170, 25);
 		controlPanel.add(resolutionLabel);
 
 		resolution = new JComboBox<Vec2>(this.getComboModel());
 		resolution.setMaximumRowCount(30);
 		currentResolutionSelection = resolution.getSelectedIndex();
 		resolution.setBounds(10,80, 170, 25);
 		resolution.setRenderer(new ListCellRenderer<Vec2>(){
 			JLabel resoLabel = null;	
 			@Override
 			public Component getListCellRendererComponent(
 					JList<? extends Vec2> list, Vec2 value, int index,
 					boolean isSelected, boolean cellHasFocus) {
 				if (resoLabel == null) {
 					resoLabel = new JLabel();
 					resoLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 1, 0));
 				}
 				resoLabel.setText((int)value.x +"x" + (int)value.y);
 				if (isSelected) {
 					resoLabel.setBackground(list.getSelectionBackground());
 					resoLabel.setForeground(list.getSelectionForeground());
 				} else {
 					resoLabel.setBackground(list.getBackground());
 					resoLabel.setForeground(list.getForeground());
 				}
 				return resoLabel;
 			}
 		});
 		controlPanel.add(resolution);
 
 		JLabel colorLabel = new JLabel("Choose color:");
 		colorLabel.setBounds(10,115, 145, 25);
 		controlPanel.add(colorLabel);
 
 		colorButton = new JButton();
 		colorButton.setBackground(NewShapeWindowBuildPanel.DEFAULT_PAINT_COLOR);
 		buildPanel.setPaintColor(colorButton.getBackground());
 		colorButton.setBounds(155,115, 25, 25);
 		controlPanel.add(colorButton);
 
 		clearButton = new JButton("Clear");
 		clearButton.setBounds(115,325,65,25);
 		controlPanel.add(clearButton);
 
 		saveButton = new JButton("Save");
 		saveButton.setBounds(50,350, 65, 25);
 		controlPanel.add(saveButton);
 
 		closeButton = new JButton("Close");
 		closeButton.setBounds(115,350, 65, 25);
 		controlPanel.add(closeButton);
 
 		add(controlPanel);
 	}
 
 	private ComboBoxModel<Vec2> getComboModel() {
 		DefaultComboBoxModel<Vec2> combo = new DefaultComboBoxModel<Vec2>();
 		combo.addElement(new Vec2(3,3));
 		combo.addElement(new Vec2(4,4));
 		combo.addElement(new Vec2(5,5));
 		combo.addElement(new Vec2(6,6));
 		combo.addElement(new Vec2(7,7));
 		combo.addElement(new Vec2(8,8));
 		combo.addElement(new Vec2(9,9));
 		combo.addElement(new Vec2(10,10));
 		combo.addElement(new Vec2(20,20));
 		combo.addElement(new Vec2(40,40));
 		return combo;
 	}
 
 	private void addListeners() {
 		colorButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Color newColor = JColorChooser.showDialog(
 						editShapeWindow,
 						"Choose Paint Color",
 						colorButton.getBackground());
 
 				if (newColor != null){
 					colorButton.setBackground(newColor);
 					buildPanel.setPaintColor(newColor);
 				}
 			}
 		});
 
 		resolution.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Message(Y/N)
 				// 		 Yes - change the resolution(create new buildShape)
 				//		 No - Stay here
 				if(buildPanel.getGridResolution().equals((Vec2)resolution.getSelectedItem())){
 					return;
 				}
 				resolution.setPopupVisible(false);
 				int n = JOptionPane.showConfirmDialog(
 						editShapeWindow, "All the painted shape will be gone. Are you sure to change the grid Resolution?",
 						"Warning",
 						JOptionPane.YES_NO_OPTION);
 
 				if(n == JOptionPane.YES_OPTION){
 					buildPanel.setGridResolution((Vec2)resolution.getSelectedItem());
 					currentResolutionSelection = resolution.getSelectedIndex();			// update the buffer
 					buildPanel.setIsDirty(false);																		// set isDirty
 				}
 				else if(n == JOptionPane.NO_OPTION){
 					resolution.setSelectedIndex(currentResolutionSelection);
 				}
 			}
 		});
 
 		saveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//save button can only be pressed when the board is set to dirty
 				//but checking if the buildPanel is dirty is always a safe practice
 				buildPanel.updateIsDirty();
 				String shapeName  =getNameFieldText();
 				if(!shapeName.equals(buildPanel.getPaintedShape().getShapeName())){
 					buildPanel.setIsDirty(true);
 				}
 				if(buildPanel.getIsDirty()){
 					boolean success = true;
 					int n = JOptionPane.showConfirmDialog(
 							editShapeWindow, "The shape has been modified. Are you sure to save it?",
 							"Confirm",
 							JOptionPane.OK_CANCEL_OPTION);
 					if(n == JOptionPane.OK_OPTION){		
 						//Save the BlockShape
 						//If the name is empty:
 						if(shapeName.equals("")){
 							success = false;
 							JOptionPane.showMessageDialog(
 									editShapeWindow, "Please enter a shape name.",
 									"Empty Name",
 									JOptionPane.ERROR_MESSAGE);
 						}
 
 						if(!shapeName.equals(buildPanel.getPaintedShape().getShapeName()) &&
 								editShapeWindow.model.checkIfShapeExists(shapeName)){
 							success = false;
 							JOptionPane.showMessageDialog(
 									editShapeWindow, "There exists a shape with the same shape name.\nPlease enter another one.",
 									"Duplicate Name",
 									JOptionPane.ERROR_MESSAGE);
 						}
 						
						//if the shape is cleared, don't save
						if(buildPanel.getPaintedShape().getShape().isEmpty()){
							success = false;
							JOptionPane.showMessageDialog(
									editShapeWindow, "The shape cannot be empty!",
									"Error",
									JOptionPane.ERROR_MESSAGE);
						}
						
 						if(success){
 							try {
 								//1. delete the old shape
 								editShapeWindow.model.removeShapeFromGame(shape);
 								
 								//2. set the new name to painted shape
 								buildPanel.getPaintedShape().setShapeName(shapeName);
 
 								//3. save the painted shape
 								editShapeWindow.model.attachShapeToGame(buildPanel.getPaintedShape());
 								editShapeWindow.getParentPanel().updateComboBox();
 								JOptionPane.showMessageDialog(
 										editShapeWindow, "The new block shape named as: "+shapeName+" has been saved!",
 										"Save successful",
 										JOptionPane.INFORMATION_MESSAGE);
 								editShapeWindow.dispose();
 								
 							} catch (ElementNotExistException e1) {
 								// This is triggered by editShapeWindow.model.removeShapeFromGame(shape);
 								// Not very likely to run here
 								e1.printStackTrace();					
 							} catch (ElementExistsException e1) {
 								// This is triggered by editShapeWindow.model.attachShapeToGame(shape);
 								// Not very likely to run here
 								e1.printStackTrace();
 							}
 						}
 					}
 					else if(n == JOptionPane.CANCEL_OPTION){
 						//Do nothing, stay in this EditShapeWindow
 					}
 				}else{
 					System.out.println("No modifications to be saved");
 				}
 
 			}
 		});
 
 		closeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Close button is similar to WindowClosing event handler
 				buildPanel.updateIsDirty();
 				String shapeName  =getNameFieldText();
 				if(!shapeName.equals(buildPanel.getPaintedShape().getShapeName())){
 					buildPanel.setIsDirty(true);
 				}
 				if(buildPanel.getIsDirty()){
 					boolean success = true;
 					int n = JOptionPane.showConfirmDialog(
 							editShapeWindow, "The shape has been modified. Do you want to save it?",
 							"Unsaved changes",
 							JOptionPane.YES_NO_CANCEL_OPTION);
 					if(n == JOptionPane.YES_OPTION){		
 						//Save the BlockShape
 						//If the name is empty:
 						if(shapeName.equals("")){
 							success = false;
 							JOptionPane.showMessageDialog(
 									editShapeWindow, "Please enter a shape name.",
 									"Empty Name",
 									JOptionPane.ERROR_MESSAGE);
 						}
 
 						if(!shapeName.equals(buildPanel.getPaintedShape().getShapeName()) &&
 								editShapeWindow.model.checkIfShapeExists(shapeName)){
 							success = false;
 							JOptionPane.showMessageDialog(
 									editShapeWindow, "There exists a shape with the same shape name.\nPlease enter another one.",
 									"Duplicate Name",
 									JOptionPane.ERROR_MESSAGE);
 						}
 						
						//if the shape is cleared, don't save
						if(buildPanel.getPaintedShape().getShape().isEmpty()){
							success = false;
							JOptionPane.showMessageDialog(
									editShapeWindow, "The shape cannot be empty!",
									"Error",
									JOptionPane.ERROR_MESSAGE);
						}
						
 						if(success){
 							try {
 								//1. delete the old shape
 								editShapeWindow.model.removeShapeFromGame(shape);
 								
 								//2. set the new name to painted shape
 								buildPanel.getPaintedShape().setShapeName(shapeName);
 
 								//3. save the painted shape
 								editShapeWindow.model.attachShapeToGame(buildPanel.getPaintedShape());
 								editShapeWindow.getParentPanel().updateComboBox();
 								JOptionPane.showMessageDialog(
 										editShapeWindow, "The new block shape named as: "+shapeName+" has been saved!",
 										"Save successful",
 										JOptionPane.INFORMATION_MESSAGE);
 								editShapeWindow.dispose();
 								
 							} catch (ElementNotExistException e1) {
 								// This is triggered by editShapeWindow.model.removeShapeFromGame(shape);
 								// Not very likely to run here
 								e1.printStackTrace();					
 							} catch (ElementExistsException e1) {
 								// This is triggered by editShapeWindow.model.attachShapeToGame(shape);
 								// Not very likely to run here
 								e1.printStackTrace();
 							}
 						}
 					}
 					else if(n == JOptionPane.NO_OPTION){
 						editShapeWindow.dispose();
 					}
 					else if(n == JOptionPane.CANCEL_OPTION){
 						//Do nothing, stay in this EditShapeWindow
 					}
 				}else{
 					editShapeWindow.dispose();
 				}
 			}
 		});
 
 		clearButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Message(Y/N)
 				// 	Yes - clear all
 				//	No - do nothing
 				int n = JOptionPane.showConfirmDialog(
 						editShapeWindow, "The shape has been modified. Are you sure to clear it?",
 						"Confirm",
 						JOptionPane.OK_CANCEL_OPTION);
 				if(n == JOptionPane.OK_OPTION){
 					buildPanel.clearPaintedShape();
 				}
 				else if(n == JOptionPane.CANCEL_OPTION){
 					//Do nothing
 				}
 
 			}
 		});
 
 	}
 
 	public static void enableSaveButton(){
 		saveButton.setEnabled(true);
 	}
 
 	public static void disableSaveButton(){
 		saveButton.setEnabled(false);
 	}
 
 	public String getNameFieldText(){
 		String shapeName = shapeNameField.getText();
 		if(shapeName == null){
 			return "";
 		}else{
 			shapeName = shapeName.trim();
 		}
 		return shapeName;
 	}
 
 }
