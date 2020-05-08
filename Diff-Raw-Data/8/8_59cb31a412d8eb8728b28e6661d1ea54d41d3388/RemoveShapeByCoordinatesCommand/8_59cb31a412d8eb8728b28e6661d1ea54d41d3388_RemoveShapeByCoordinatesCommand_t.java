 package edu.pucsp.cgpi.core.CommandTools.Undoablecommands;
 
 import edu.pucsp.cgpi.Drawableshapes.DrawableShape;
 import edu.pucsp.cgpi.core.CommandTools.CommandTarget;
 
 public class RemoveShapeByCoordinatesCommand  implements UndoableCommand {
 
	private CommandTarget target = null;
 	private double x,y;
	private DrawableShape deletedShape = null;
 
 	public RemoveShapeByCoordinatesCommand(double x, double y) {
 		super();
 		this.x = x;
 		this.y = y;
 	}
 
 	@Override
 	public void execute() 
 	{
 		deletedShape = target.getShapeByCoordinates(x,y);
 		if(target!= null) target.removeShape(deletedShape);
 	}
 
 	@Override
 	public void undo() 
 	{
		if(target!= null && deletedShape != null) target.addShape(deletedShape);
 	}
 
 	@Override
 	public void setTarget(CommandTarget t) {
 		target = t;
 	}
 
 }
