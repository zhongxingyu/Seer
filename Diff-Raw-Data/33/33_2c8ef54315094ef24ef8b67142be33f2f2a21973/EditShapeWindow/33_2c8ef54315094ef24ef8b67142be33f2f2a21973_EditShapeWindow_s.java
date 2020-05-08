 package framework;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 
 import components.BlockShape;
 
 import exceptions.ElementExistsException;
 import exceptions.ElementNotExistException;
 
 /**
  * This is the edit shape window
  * 
  * On start up, the window should display:
  * 1. The block shape name to be edited
  * 2. The shape with correct resolution
  * 3. The colorButton to be set to the default paint color
  *  
  * The following actions will make the window dirty:
  * 1. Change the name
  * 2. Change the grid resolution(everything is wiped)- need to give them a warning
  * 3. Change the paint(add or delete)
  * 
  * The following actions will trigger alert message
  * 1. Saving when it's dirty
  * 2. Closing window when it's dirty
  * 3. Changing the grid resolution(doesn't matter if it's dirty or not) 
  * 4. Clearing the build panel(doesn't matter if it's dirty or not) 
  * 
  * */
 @SuppressWarnings("serial")
 public class EditShapeWindow extends JDialog {
 
 	final GameModel model;
 	final GameSidePanel parent;
 	private EditShapeWindowBuildPanel buildPanel;
 	private EditShapeWindowSidePanel sidePanel;
 	
 	public EditShapeWindow(GameFrame frame,GameModel model, GameSidePanel parentPanel, final BlockShape shape){
 		super(frame,true);
 		
 		this.model = model;
 		this.parent = parentPanel;
 		
 		setLayout(new BorderLayout());
 
 		buildPanel = new EditShapeWindowBuildPanel(shape);
 		buildPanel.setBounds(0,0,EditShapeWindowBuildPanel.SHAPE_WIN_SIZE,NewShapeWindowBuildPanel.SHAPE_WIN_SIZE);
 		this.add((Component) buildPanel, "Center");
 		
 		sidePanel = new EditShapeWindowSidePanel(this,buildPanel, shape);
 		sidePanel.setBounds(NewShapeWindowBuildPanel.SHAPE_WIN_SIZE,0,
 				NewShapeWindowSidePanel.SIDE_PANEL_WIDTH,NewShapeWindowSidePanel.SIDE_PANEL_HEIGHT);
 		this.add(new JScrollPane(sidePanel), "East");
 		
 		this.setTitle("Edit Shape");
 		this.setResizable(false);
 		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
 		this.pack();
 		
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent we) {
 				buildPanel.updateIsDirty();
 				String shapeName  =sidePanel.getNameFieldText();
 				if(!shapeName.equals(buildPanel.getPaintedShape().getShapeName())){
 					buildPanel.setIsDirty(true);
 				}
 				if(buildPanel.getIsDirty()){
 					boolean success = true;
 					int n = JOptionPane.showConfirmDialog(
 							EditShapeWindow.this, "The shape has been modified. Do you want to save it?",
 							"Unsaved changes",
 							JOptionPane.YES_NO_CANCEL_OPTION);
 					if(n == JOptionPane.YES_OPTION){		
 						//Save the BlockShape
 						//If the name is empty:
 						if(shapeName.equals("")){
 							success = false;
 							JOptionPane.showMessageDialog(
 									EditShapeWindow.this, "Please enter a shape name.",
 									"Empty Name",
 									JOptionPane.ERROR_MESSAGE);
 						}
 						
 						if(!shapeName.equals(buildPanel.getPaintedShape().getShapeName()) &&
 								EditShapeWindow.this.model.checkIfShapeExists(shapeName)){
 							success = false;
 							JOptionPane.showMessageDialog(
 									EditShapeWindow.this, "There exists a shape with the same shape name.\nPlease enter another one.",
 									"Duplicate Name",
 									JOptionPane.ERROR_MESSAGE);
 						}
 
 						if(success){
 							try {
 								//1. delete the old shape
 								EditShapeWindow.this.model.removeShapeFromGame(shape);
 								
 								//2. set the new name to painted shape
 								buildPanel.getPaintedShape().setShapeName(shapeName);
 
 								//3. save the painted shape
 								EditShapeWindow.this.model.attachShapeToGame(buildPanel.getPaintedShape());
 								parent.updateComboBox();
 								JOptionPane.showMessageDialog(
 										EditShapeWindow.this, "The new block shape named as: "+shapeName+" has been saved!",
 										"Save successful",
 										JOptionPane.INFORMATION_MESSAGE);
 								dispose();
 							} catch (ElementExistsException e) {
 								success = false;
 								JOptionPane.showMessageDialog(
 										EditShapeWindow.this, "There exists a shape with the same shape name.\nPlease enter another one.",
 										"Duplicate Name",
 										JOptionPane.ERROR_MESSAGE);
 							} catch (ElementNotExistException e) {
 								// This is triggered by EditShapeWindow.this.model.removeShapeFromGame(shape);
 								// Not very likely to run here
 								e.printStackTrace();
 							}
 						}
 
 					}
 					else if(n == JOptionPane.NO_OPTION){
 						dispose();
 					}
 					else if(n == JOptionPane.CANCEL_OPTION){
 						//Do nothing, stay in this NewShapeWindow
 					}
 				}else{
 					dispose();
 				}
 
 			}
 		});
 		
 	}
 	
 	public GameSidePanel getParentPanel(){
 		return this.parent;
 	}
 }
