 package controller;
 
 import java.awt.Rectangle;
 import java.util.Observable;
 
 import model.Ball;
 import model.Board;
 import model.gizmos.AbsorberGizmo;
 import model.gizmos.AcceleratorGizmo;
 import model.gizmos.CircleBumper;
 import model.gizmos.GateGizmo;
 import model.gizmos.IGizmo;
 import model.gizmos.LeftFlipper;
 import model.gizmos.MultiballGizmo;
 import model.gizmos.PortalGizmo;
 import model.gizmos.RightFlipper;
 import model.gizmos.SquareBumper;
 import model.gizmos.TriangleBumper;
 import controller.GizmoballViewModel.UpdateReason;
 
 public class DesignModeViewModel extends Observable {
 	
 	private Board board;
 	private DesignCommand currentCommand;
 	private Rectangle positionBox;
 	private boolean positionValid, variableSize, selecting;
 	private int startX, startY;
 	private IGizmo selectedGizmo;
 	private TriggerHandler triggerHandler;
 	private String statusMessage;
 	
 	/**
 	 * This enum represents the current design tool selected by the
 	 * user, it is coupled with an associated status message. This
 	 * message is displayed in a status bar while in design mode. 
 	 */
 	public enum DesignCommand {
 		
 		None,
 		AddCircleBumper("Click to place a circle bumper.", true),
 		AddSquareBumper("Click to place a square bumper.", true),
 		AddTriangleBumper("Click to place a triangle bumper.", true),
 		AddLeftFlipper("Click to place a left flipper.", 2, 2),
 		AddRightFlipper("Click to place a right flipper.", 2, 2),
 		AddAbsorber("Click and drag to place an absorber gizmo.", true),
 		AddBall("Click to place a ball.", true),
 		DeleteGizmo("Click on a gizmo or ball to delete.", false),
 		RotateGizmo("Click on a gizmo to rotate it clockwise by 90 degrees.", false),
 		MoveGizmo("Click on a gizmo and drag it to a new locaton.", false),
 		ConnectKeyUp("Click gizmo to trigger.", false),
 		ConnectKeyDown("Click gizmo to trigger.", false),
 		ConnectGizmo("Click source gizmo.", false),
 		AddAcceleratorGizmo("Click to place an accelerator gizmo.", true),
 		AddPortalGizmo("Click to place a portal gizmo.", true),
 		AddMultiballGizmo("Click to place a multiball gizmo.", true),
 		AddGateGizmo("Click to place a gate gizmo.", true);
 		
 		private DesignCommand() {
 			
 			this.statusMessage = "";
 			this.addGizmoCommand = false;
 			this.positionBox = null;
 		}
 		
 		private DesignCommand(String statusMessage, boolean addGizmoCommand){
 			
 			this.statusMessage = statusMessage;
 			this.addGizmoCommand = addGizmoCommand;
 			this.positionBox = new Rectangle(0, 0, 1, 1);
 		}
 		
 		private DesignCommand(String statusMessage, int positionBoxWidth, int positionBoxHeight) {
 
 			this.statusMessage = statusMessage;
 			this.addGizmoCommand = true;
 			this.positionBox = new Rectangle(0, 0, positionBoxWidth, positionBoxHeight);
 		}
 		
 		public String statusMessage;
 		public boolean addGizmoCommand;
 		public Rectangle positionBox; 
 	}
 	
 	/**
 	 * Constructor.
 	 */
 	public DesignModeViewModel(Board board, TriggerHandler triggerHandler) {
 		
 		this.board = board;
 		this.triggerHandler = triggerHandler;
 		currentCommand = DesignCommand.None;
 	}
 	
 	/**
 	 * Gets the currently selected command.
 	 */
 	public DesignCommand getCurrentCommand() {
 		
 		return currentCommand;
 	}
 	
 	/**
 	 * Sets the current command and notifies observers of the change.
 	 */
 	public void setCurrentCommand(DesignCommand value) {
 		
 		currentCommand = value;
 		positionBox = currentCommand.positionBox;		
 		setStatusMessage(currentCommand.statusMessage);
 		selectedGizmo = null;
 		
 		this.setChanged();
 		this.notifyObservers(UpdateReason.SelectedToolChanged);
 	}
 	
 	/**
 	 * When plcaing an absorber or moving a gizmo, mouse dragging
 	 * is required. This stores the initial mouse position.
 	 * 
 	 * @param x - mouse-x location.
 	 * @param y - mouse-y location. 
 	 */
 	public void beginSelectAt(int x, int y) {
 		
 		if (positionValid == false)
 			return;
 		
 		switch (currentCommand) {
 		
 			case AddAbsorber:
 				positionBox.setLocation(x, y);
 				variableSize = true;
 				break;
 				
 			case MoveGizmo:
 				selectedGizmo = board.getGizmoAt(x, y);
 				
 				if (selectedGizmo != null) {
 					positionBox = new Rectangle(selectedGizmo.getX(), selectedGizmo.getY(), selectedGizmo.getWidth(), selectedGizmo.getHeight() );
 				}
 				break;
 		}
 		
 		startX = x;
 		startY = y;
 		selecting = true;
 	}
 	
 	/**
 	 * When the user has finished selecting on the grid a command 
 	 * has been completed. This method gets passed the mouse location
 	 * which is used for pinpointing where a commands action is to 
 	 * take effect. e.g. placing a CircleBumped, the width/height of
 	 * an absorber, etc. 
 	 * 
 	 * @param x - mouse-x location.
 	 * @param y - mouse-y location. 
 	 */
 	public void endSelectAt(int x, int y) {
 		
 		selecting = false;
 		
 		if (positionValid == false)
 			return;
 		
 		switch (currentCommand) {
 		
 			case AddAbsorber:
 				board.addGizmo(new AbsorberGizmo(positionBox.x, positionBox.y, 
 						positionBox.x + positionBox.width, positionBox.y + positionBox.height));
 				
 				positionBox.setLocation(x, y);
 				positionBox.setSize(1, 1);
 				variableSize = false;
 				positionValid = false;
 				break;
 				
 			case AddCircleBumper:
 				board.addGizmo(new CircleBumper(x, y));
 				positionValid = false;
 				break;
 				
 			case AddSquareBumper:
 				board.addGizmo(new SquareBumper(x, y));
 				positionValid = false;
 				break;
 				
 			case AddTriangleBumper:
 				board.addGizmo(new TriangleBumper(x, y, 0));
 				positionValid = false;
 				break;
 				
 			case AddLeftFlipper:
 				board.addGizmo(new LeftFlipper(x, y));
 				positionValid = false;
 				break;
 				
 			case AddRightFlipper:
 				board.addGizmo(new RightFlipper(x, y));
 				positionValid = false;
 				break;
 				
 			case AddAcceleratorGizmo:
 				board.addGizmo(new AcceleratorGizmo(x, y));
 				positionValid = false;
 				break;
 				
 			case AddPortalGizmo:
 				board.addGizmo(new PortalGizmo(x, y));
 				positionValid = false;
 				break;
 				
 			case AddMultiballGizmo:
 				board.addGizmo(new MultiballGizmo(x, y, board));
 				positionValid = false;
 				break;
 				
 			case AddGateGizmo:
 				board.addGizmo(new GateGizmo(x, y));
 				positionValid = false;
 				break;
 				
 			case AddBall:
 				board.addBall(new Ball(x + 0.5, y + 0.5, 0.25, 1));
 				positionValid = false;
 				break;
 				
 			case MoveGizmo:
 				if (selectedGizmo != null) {
 					selectedGizmo.move(x, y);
 					selectedGizmo = null;
 				}
 				break;
 				
 			case RotateGizmo:
 				selectedGizmo = board.getGizmoAt(x, y);
 				
 				if (selectedGizmo != null) {
					if (selectedGizmo.canRotate())
						selectedGizmo.rotate();
					
 					selectedGizmo = null;
 				}
 				break;
 				
 			case DeleteGizmo:
 				Ball ball = board.getBallAt(x, y);
 				
 				if (ball != null) {
 					
 					board.getBalls().remove(ball);
 				}else {
 					
 					selectedGizmo = board.getGizmoAt(x, y);
 					
 					if (selectedGizmo != null) {
 						board.getGizmos().remove(selectedGizmo);
 						selectedGizmo = null;
 						positionValid = false;
 					}
 				}
 				break;
 				
 			case ConnectKeyDown:
 			case ConnectKeyUp:
 				selectedGizmo = board.getGizmoAt(x, y);
 				
 				if (selectedGizmo != null) {
 					setStatusMessage("Press trigger key.");
 				}
 				break;
 				
 			case ConnectGizmo:
 				if (selectedGizmo != null) {
 					IGizmo targetGizmo = board.getGizmoAt(x, y);
 					
 					if (targetGizmo != null) {
 						selectedGizmo.connect(targetGizmo);
 						setStatusMessage("Connected.");
 						selectedGizmo = null;
 					}
 				}else {
 					selectedGizmo = board.getGizmoAt(x, y);
 					
 					if (selectedGizmo != null) {
 						setStatusMessage("Select target gizmo.");
 					}
 				}
 				break;
 				
 			default:
 				return;
 		}
 		
 		this.setChanged();
 		this.notifyObservers(UpdateReason.BoardChanged);
 	}
 	
 	/**
 	 * When the mouse is moving around the grid, the position (validation)
 	 * box needs to be updated. This method updates the position-box position 
 	 * as well as the validity of it's location (changing the colour to red or 
 	 * green as appropriate depending on the command).
 	 * 
 	 * @param x - mouse-x location.
 	 * @param y - mouse-y location. 
 	 */
 	public void moveTo(int x, int y) {
 		
 		if (positionBox != null) {
 			if (currentCommand.addGizmoCommand) {
 				if (variableSize == false) {
 					//move the box to the new position
 					positionBox.setLocation(x, y);
 				}else {
 					//resize the box from the start to the new position
 					positionBox.setLocation(Math.min(x, startX), Math.min(y, startY));
 					positionBox.setSize(Math.abs(x - startX) + 1, Math.abs(y - startY) + 1);
 				}
 				
 				positionValid = validLocation();
 			}else {
 				if (selecting) {
 					positionBox.setLocation(x, y);
 					positionValid = validLocation();
 				}else {
 					IGizmo gizmo = board.getGizmoAt(x, y);
 					
 					if (gizmo != null) {
 						positionBox.setLocation(gizmo.getX(), gizmo.getY());
 						positionBox.setSize(gizmo.getWidth(), gizmo.getHeight());
 						positionValid = true;
 					}else {
 						Ball ball = board.getBallAt(x, y);
 						
 						if (ball != null && currentCommand == DesignCommand.DeleteGizmo) {
 							positionBox.setLocation(x, y);
 							positionBox.setSize(1, 1);
 							positionValid = true;
 						}else {
 							positionBox.setLocation(x, y);
 							positionBox.setSize(1, 1);
 							positionValid = false;
 						}
 					}
 				}
 			}
 			
 			this.setChanged();
 			this.notifyObservers(UpdateReason.BoardChanged);
 		}
 	}
 	
 	
 	public Rectangle getPositionBox() {
 		
 		return positionBox;
 	}
 	
 	
 	public boolean getPositionValid() {
 		
 		return positionValid;
 	}
 	
 	
 	public String getStatusMessage() {
 		
 		return statusMessage;
 	}
 	
 	protected void setStatusMessage(String message) {
 		
 		this.statusMessage = message;
 		this.setChanged();
 		this.notifyObservers(UpdateReason.StatusChanged);
 	}
 	
 	/**
 	 * This registers keystrokes in the system for linking gizmos
 	 * actions to keys so the user can interact with them during
 	 * run mode.
 	 * 
 	 * @param keycode - the keycode of the selected key to link to. 
 	 */
 	public void keyPressed(int keycode) {
 		
 		switch (currentCommand) {
 		
 			case ConnectKeyDown:
 				if (selectedGizmo != null){
 					triggerHandler.addLinkDown(keycode, selectedGizmo);
 					setStatusMessage("Connected.");
 					selectedGizmo = null;
 				}
 				
 				break;
 				
 			case ConnectKeyUp:
 				if (selectedGizmo != null) {
 					triggerHandler.addLinkUp(keycode, selectedGizmo);
 					setStatusMessage("Connected.");
 					selectedGizmo = null;
 				}
 				
 				break;
 		}
 		
 		setStatusMessage("Key connected.");
 		selectedGizmo = null;
 	}
 	
 	
 	/**
 	 * Check the validity of a gizmo/ball placement.
 	 */
 	private boolean validLocation() {
 		int x = positionBox.x;
 		int y = positionBox.y;
 		int w = positionBox.width;
 		int h = positionBox.height;
 		
 		if (x + w > board.getWidth() || y + h > board.getHeight()) {
 			return false;
 		}
 		for (int i = 0; i < w; i++) {
 			for (int j = 0; j < h; j++) {
 				int xx = x + i;
 				int yy = y + j;
 				IGizmo bMG = board.getGizmoAt(xx, yy);
 				Ball bMB = board.getBallAt(xx, yy);
 				if (bMG != null && !bMG.equals(selectedGizmo)) {
 					return false;
 				} else if (bMB != null) {
 					return false;
 				}
 			}
 		}
 		
 		return true;
 	}
 }
