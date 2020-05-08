 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 
 
 @SuppressWarnings("serial")
 public class GUIPartRobot implements Serializable
 {
 	public PartRobot partRobot;
 	/** Set the startPos of this Movement to the robot's end point destination */
 	public Movement movement; 
 	
 	private Movement baseMove, armMove, handMove;
	private final int baseStartX = 400;
 	private final int baseStartY = 330;
 	
 	
 	public GUIPartRobot(PartRobot partRobot)
 	{
 		this.partRobot = partRobot;
 		movement = new Movement(new Point2D.Double(baseStartX, baseStartY), Math.PI );
 		baseMove = new Movement(new Point2D.Double(baseStartX, baseStartY), Math.PI );
 		armMove = new Movement(new Point2D.Double(0,0), Math.PI );
 		handMove = new Movement(new Point2D.Double(0,0), Math.PI );
 	}
 
 	public void draw(Graphics2D g, long currentTime)
 	{
 		doCalculations(currentTime);
 		
 		Painter.draw(g, Painter.ImageEnum.ROBOT_BASE, 75, -1, currentTime, baseMove, true);
		Painter.draw(g, Painter.ImageEnum.ROBOT_ARM_1, 400, -1, currentTime, armMove, true);
 		Painter.draw(g, Painter.ImageEnum.PART_ROBOT_HAND, 150, -1, currentTime, handMove, true);
 		
 	}
 	
 	private void doCalculations(long currentTime)
 	{
 		Point2D.Double target = new Point2D.Double(movement.getStartPos().x - baseStartX, movement.getStartPos().y - baseStartY);
 		
		double theta = Math.PI;
 		if (target.x != 0 || target.y != 0)
 			theta = Math.asin(target.x/(Math.sqrt(Math.pow(target.x, 2) + Math.pow(target.y, 2))));
 		
 		if (target.y > 0 && target.x < 0)
 			theta -= Math.PI/2;
 		
 		if (armMove.getEndRot() != theta)
 			armMove = Movement.fromSpeed(armMove.calcPos(currentTime), armMove.calcRot(currentTime), currentTime, armMove.calcPos(currentTime), theta, Math.PI/4);
 		
 		armMove.slaveTranslation(baseMove, 0, 0, currentTime);
 		handMove.slaveTranslation(armMove, 180*Math.sin(armMove.calcRot(currentTime)), -180*Math.cos(armMove.calcRot(currentTime)), currentTime);
 		handMove.slaveRotation(armMove, 0, currentTime);
 		
 	}
 	
 	public boolean arrived(long currentTime)
 	{
 		return (armMove.arrived(currentTime) && baseMove.arrived(currentTime) && handMove.arrived(currentTime));
 	}
 	
 	
 	public void park()
 	{
 		this.movement = new Movement(new Point2D.Double(baseStartX, baseStartY), 0);
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
