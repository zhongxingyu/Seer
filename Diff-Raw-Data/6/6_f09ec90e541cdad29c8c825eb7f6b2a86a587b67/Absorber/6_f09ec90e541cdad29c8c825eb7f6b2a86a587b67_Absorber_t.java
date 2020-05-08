 package model;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 import model.physics.Geometry;
 import model.physics.LineSegment;
 
 public class Absorber extends Observable implements iGizmo {
 
 	protected Point point;
 	protected double row, column;
 	protected double  cellWidth, cellHeight;
 	protected List<LineSegment> lineSegments;
 	protected List<iBall> capturedBalls;
 	protected boolean active;
 	protected String identifier;
 	
 	public Absorber( String identifier, Point p, double row, double column, double width, double height) {
 		this.point 			= p;
 		this.row 			= row;
 		this.column 		= column;
 		this.identifier 	= identifier;
 		this.cellWidth		= width;
 		this.cellHeight		= height;
 		this.active 		= false;
 		
 		this.lineSegments = new ArrayList<LineSegment>();
 		this.capturedBalls = new ArrayList<iBall>();
 		
 		this.fillLineSegments();
 	}
 
 	@Override
 	public String getIdentifier() {
 		return this.identifier;
 	}
 
 	@Override
 	public Point getLocation() {
 		return this.point;
 	}
 
 	@Override
 	public void setLocation(Point p) {
 		this.point = p;
 	}
 
 	@Override
 	public double getRowWidth() {
 		return this.row;
 	}
 
 	@Override
 	public void setRowWidth(double w) {
 		this.row = w;
 	}
 
 	@Override
 	public double getColumnHeight() {
 		return this.column;
 	}
 
 	@Override
 	public void setColumnHeight(double h) {
 		this.column = h;
 	}
 
 	@Override
 	public double getRotation() {
 		//unneeded
 		return 0;
 	}
 
 	@Override
 	public void setRotation(double r) {
 		//unneeded
 	}
 	
 	public void releaseBall()
 	{
 		this.active = true;
 	}
 
 	@Override
 	public void move() {
 		if(this.active){
 			if((!this.capturedBalls.isEmpty())){
 				this.capturedBalls.get(0).setCaptured(false);
 				this.capturedBalls.remove(0);
 			}
 		}
		this.active = false;
 	}
 
 	@Override
 	public double getCellWidth() {
 		// TODO Auto-generated method stub
 		return this.cellWidth;
 	}
 
 	@Override
 	public void setCellWidth(double w) {
 		this.cellWidth = w;
 	}
 
 	@Override
 	public double getCellHeight() {
 		// TODO Auto-generated method stub
 		return this.cellHeight;
 	}
 
 	@Override
 	public void setCellHeight(double h) {
 		this.cellHeight = h;
 	}
 
 	private void fillLineSegments()
 	{
 		double topLX = (this.point.x * this.cellWidth) - (this.cellWidth / 2);
 		double topLY = (this.point.y * this.cellHeight) - (this.cellHeight / 2);
 		
 		double topRX = (this.point.x * this.cellWidth) + (this.row * this.cellWidth)- (this.cellWidth / 2);
 		double topRY = topLY;
 		
 		double bottomLX = topLX;
 		double bottomLY = (this.point.y * this.cellHeight) + (this.column * this.cellHeight) - (this.cellHeight / 2);
 		
 		double bottomRX = topRX;
 		double bottomRY = bottomLY;
 		
 		this.lineSegments.add(new LineSegment(topLX, topLY, topRX, topRY));
 		
 		this.lineSegments.add(new LineSegment(bottomLX, bottomLY, bottomRX, bottomRY));
 		
 		this.lineSegments.add(new LineSegment(topLX, topLY, bottomLX, bottomLY));
 		
 		this.lineSegments.add(new LineSegment(topRX, topRY, bottomRX, bottomRY));
 		
 		
 	}
 	
 	@Override
 	public double timeUntilCollision(iBall ball) {
 		double min = Double.POSITIVE_INFINITY;
 		double newMin;
 		
 		for(LineSegment l : this.lineSegments){
 			newMin = Geometry.timeUntilWallCollision(l, ball.returnBounds(), ball.getVelocity());
 			if(newMin < min)min = newMin;
 		}
 		
 		return min;
 	}
 
 	@Override
 	public void collide(iBall ball) {
 		double min = Double.POSITIVE_INFINITY;
 		double newMin;
 		LineSegment closestLine = null;
 		
 		for(LineSegment l : this.lineSegments){
 			newMin = Geometry.timeUntilWallCollision(l, ball.returnBounds(), ball.getVelocity());
 			if(newMin < min){
 				min = newMin;
 				closestLine = l;
 			}
 		}
 		
 		if(closestLine != null)ball.setVelocity(Geometry.reflectWall(closestLine, ball.getVelocity()));
 	}
 	
 	public void captureBall(iBall ball)
 	{
 		this.capturedBalls.add(ball);
 	}
 
 }
