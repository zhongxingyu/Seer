 package controller;
 
 import java.awt.Rectangle;
 import java.util.Observable;
 
 import model.Ball;
 import model.Board;
 import model.gizmos.AbsorberGizmo;
 import model.gizmos.CircleBumper;
 import model.gizmos.IGizmo;
 import model.gizmos.LeftFlipper;
 import model.gizmos.RightFlipper;
 import model.gizmos.SquareBumper;
 import model.gizmos.TriangleBumper;
 import controller.GizmoballViewModel.UpdateReason;
 
 public class DesignModeViewModel extends Observable
 {
 	private Board board;
 	private DesignCommand currentCommand;
 	private Rectangle positionBox;
 	private boolean positionValid, variableSize, selecting;
 	private int startX, startY;
 	private IGizmo selectedGizmo;
 	private TriggerHandler triggerHandler;
 	private String statusMessage;
 	
 	public enum DesignCommand
 	{
 		None(""),
 		AddCircleBumper("Click to place a circle bumper."),
 		AddSquareBumper("Click to place a square bumper."),
 		AddTriangleBumper("Click to place a triangle bumper."),
 		AddLeftFlipper("Click to place a left flipper."),
 		AddRightFlipper("Click to place a right flipper."),
 		AddAbsorber("Click to place an absorber."),
 		AddBall("Click to place a ball."),
 		DeleteGizmo("Click on a gizmo or ball to delete."),
 		RotateGizmo("Click on a gizmo to rotate it clockwise by 90 degrees."),
 		MoveGizmo("Click on a gizmo and drag it to a new locaton."),
 		ConnectKeyUp("Click gizmo to trigger."),
 		ConnectKeyDown("Click gizmo to trigger."),
 		ConnectGizmo("Click source gizmo.");
 		
 		private DesignCommand(String statusMessage)
 		{
 			this.statusMessage = statusMessage;
 		}
 		
 		public String getStatusMessage()
 		{
 			return statusMessage;
 		}
 		
 		private String statusMessage;
 	}
 	
 	/**
 	 * Constructor.
 	 */
 	public DesignModeViewModel(Board board, TriggerHandler triggerHandler)
 	{
 		this.board = board;
 		this.triggerHandler = triggerHandler;
 		currentCommand = DesignCommand.None;
 	}
 	
 	/**
 	 * Gets the currently selected command.
 	 */
 	public DesignCommand getCurrentCommand()
 	{
 		return currentCommand;
 	}
 	
 	/**
 	 * Sets the current command and notifies observers of the change.
 	 */
 	public void setCurrentCommand(DesignCommand value)
 	{
 		currentCommand = value;
 		
 		switch (currentCommand)
 		{
 			case AddSquareBumper: 
 			case AddCircleBumper:
 			case AddTriangleBumper:
 			case AddAbsorber:
 			case AddBall:
 			case MoveGizmo:
 			case RotateGizmo:
 			case DeleteGizmo:
 			case ConnectGizmo:
 			case ConnectKeyDown:
 			case ConnectKeyUp:
 				positionBox = new Rectangle(0, 0, 1, 1);
 				break;
 				
 			case AddLeftFlipper:
 			case AddRightFlipper:
 				positionBox = new Rectangle(0, 0, 2, 2);
 				break;
 				
 			default:
 				positionBox = null;
 				break;
 		}
 		
 		setStatusMessage(currentCommand.getStatusMessage());
 		this.setChanged();
 		this.notifyObservers(UpdateReason.SelectedToolChanged);
 	}
 	
 	
 	public void beginSelectAt(int x, int y)
 	{
 		if (positionValid == false)
 			return;
 		
 		switch (currentCommand)
 		{
 			case AddAbsorber:
 				positionBox.setLocation(x, y);
 				variableSize = true;
 				break;
 				
 			case MoveGizmo:
 				selectedGizmo = board.getGizmoAt(x, y);
 				
 				if (selectedGizmo != null)
 				{
					positionBox = new Rectangle(selectedGizmo.getX(), selectedGizmo.getY(), selectedGizmo.getWidth(), selectedGizmo.getHeight() );
 				}
 				break;
 		}
 		
 		startX = x;
 		startY = y;
 		selecting = true;
 	}
 	
 	
 	public void endSelectAt(int x, int y)
 	{
 		selecting = false;
 		
 		if (positionValid == false)
 			return;
 		
 		switch (currentCommand)
 		{
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
 				
 			case AddBall:
 				board.addBall(new Ball(x + 0.5, y + 0.5, 0.25, 1));
 				positionValid = false;
 				break;
 				
 			case MoveGizmo:
 				if (selectedGizmo != null)
 				{
 					selectedGizmo.move(x, y);
 					selectedGizmo = null;
 					positionBox = null;
 				}
 				break;
 				
 			case RotateGizmo:
 				selectedGizmo = board.getGizmoAt(x, y);
 				
 				if (selectedGizmo != null)
 				{
 					selectedGizmo.rotate();
 					selectedGizmo = null;
 				}
 				break;
 				
 			case DeleteGizmo:
 				selectedGizmo = board.getGizmoAt(x, y);
 				
 				if (selectedGizmo != null)
 				{
 					board.getGizmos().remove(selectedGizmo);
 					selectedGizmo = null;
 					positionValid = false;
 				}
 				else
 				{
 					Ball ball = board.getBallAt(x, y);
 					
 					if (ball != null)
 					{
 						board.getBalls().remove(ball);
 					}
 				}
 				break;
 				
 			case ConnectKeyDown:
 			case ConnectKeyUp:
 				selectedGizmo = board.getGizmoAt(x, y);
 				
 				if (selectedGizmo != null)
 				{
 					setStatusMessage("Press trigger key.");
 				}
 				break;
 				
 			case ConnectGizmo:
 				if (selectedGizmo != null)
 				{
 					IGizmo targetGizmo = board.getGizmoAt(x, y);
 					
 					if (targetGizmo != null)
 					{
 						selectedGizmo.connect(targetGizmo);
 						setStatusMessage("Connected.");
 						selectedGizmo = null;
 					}
 				}
 				else
 				{
 					selectedGizmo = board.getGizmoAt(x, y);
 					
 					if (selectedGizmo != null)
 					{
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
 	
 	
 	public void moveTo(int x, int y)
 	{
 		if (positionBox != null)
 		{
 			switch (currentCommand)
 			{
 				case AddCircleBumper:
 				case AddSquareBumper:
 				case AddTriangleBumper:
 				case AddLeftFlipper:
 				case AddRightFlipper:
 				case AddAbsorber:
 				case AddBall:
 					if (variableSize == false)
 					{
 						//move the box to the new position
 						positionBox.setLocation(x, y);
 					}
 					else
 					{
 						//resize the box from the start to the new position
 						positionBox.setLocation(Math.min(x, startX), Math.min(y, startY));
 						positionBox.setSize(Math.abs(x - startX) + 1, Math.abs(y - startY) + 1);
 					}
 					
 					positionValid = validLocation();
 					break;
 					
 				default:					
 					if (selecting)
 					{
 						positionBox.setLocation(x, y);
 						positionValid = validLocation();
 					}
 					else 
 					{
 						IGizmo gizmo = board.getGizmoAt(x, y);
 						
 						if (gizmo != null)
 						{
 							positionBox.setLocation(gizmo.getX(), gizmo.getY());
							positionBox.setSize(gizmo.getWidth(), gizmo.getHeight());
 							positionValid = true;
 						}
 						else
 						{
 							Ball ball = board.getBallAt(x, y);
 							
 							if (ball != null)
 							{
 								positionBox.setLocation(x, y);
 								positionBox.setSize(1, 1);
 								positionValid = true;
 							}
 							else
 							{
 								positionBox.setLocation(x, y);
 								positionBox.setSize(1, 1);
 								positionValid = false;
 							}
 						}
 					}
 					break;
 			}
 			
 			this.setChanged();
 			this.notifyObservers(UpdateReason.BoardChanged);
 		}
 	}
 	
 	
 	public Rectangle getPositionBox()
 	{
 		return positionBox;
 	}
 	
 	
 	public boolean getPositionValid()
 	{
 		return positionValid;
 	}
 	
 	
 	public String getStatusMessage()
 	{
 		return statusMessage;
 	}
 	
 	protected void setStatusMessage(String message)
 	{
 		this.statusMessage = message;
 		this.setChanged();
 		this.notifyObservers(UpdateReason.StatusChanged);
 	}
 	
 	
 	public void keyPressed(int keycode)
 	{
 		switch (currentCommand)
 		{
 			case ConnectKeyDown:
 				if (selectedGizmo != null)
 				{
 					triggerHandler.addLinkDown(keycode, selectedGizmo);
 					setStatusMessage("Connected.");
 				}
 				
 				break;
 				
 			case ConnectKeyUp:
 				if (selectedGizmo != null)
 				{
 					triggerHandler.addLinkUp(keycode, selectedGizmo);
 					setStatusMessage("Connected.");
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
