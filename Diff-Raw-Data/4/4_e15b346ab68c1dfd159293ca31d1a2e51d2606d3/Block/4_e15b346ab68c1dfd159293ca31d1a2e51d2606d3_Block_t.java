 package com.feup.sfs.block;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import com.feup.sfs.factory.Factory;
 
 public class Block {
 
 	private Factory factory;
 	private int type;
 	private int nextType;
 	
 	private boolean moveLeft;
 	private boolean moveTop;
 	private boolean moveBottom;
 	private boolean moveRight;
 	
 	private double centerX;
 	private double centerY;
 	private double oldCenterX;
 	private double oldCenterY;
 	
 	private long duration;
 	private long currentWork = 0;
 	
 	public Block(Factory factory, int type, double centerX, double centerY) {
 		this.factory = factory;
 		this.type = type;
 		this.setNextType(-1);
 		this.setCenterX(centerX);
 		this.setCenterY(centerY);
 	}
 
 	public void setFactory(Factory factory) {
 		this.factory = factory;
 	}
 
 	public Factory getFactory() {
 		return factory;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 
 	public int getType() {
 		return type;
 	}
 	
 	public void resetMovements(){
 		moveBottom = false;
 		moveLeft = false;
 		moveRight = false;
 		moveTop = false;
 	}
 	
 	public void doStep(){
 		oldCenterX = getCenterX();
 		oldCenterY = getCenterY();
 		if (moveLeft) setCenterX(getCenterX() - getFactory().getConveyorSpeed()*getFactory().getSimulationTime()/1000);
 		if (moveRight) setCenterX(getCenterX() + getFactory().getConveyorSpeed()*getFactory().getSimulationTime()/1000);
 		if (moveTop) setCenterY(getCenterY() - getFactory().getConveyorSpeed()*getFactory().getSimulationTime()/1000);
 		if (moveBottom) setCenterY(getCenterY() + getFactory().getConveyorSpeed()*getFactory().getSimulationTime()/1000);
 	}
 	
 	public void undoStep(){
 		setCenterX(oldCenterX);
 		setCenterY(oldCenterY);
 	}
 
 	private Color getColor(int id) {
 		BlockType type = BlockType.getBlockType(id);
 		if (type == null) return Color.black;
 		return type.getColor();
 	}
 	
 	public void paint(Graphics g) {
 		g.setColor(getColor(type));
 		g.fillOval(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
 		if (nextType != -1) {
 			g.setColor(Color.red);
 			g.drawOval(getBounds().x - 1, getBounds().y - 1, getBounds().width + 2, getBounds().height + 2);
 		}
 		
 		if (nextType != -1) {
 			g.setColor(getColor(nextType));
 			int angle = Math.min((int) (360*((double)(duration - currentWork)/duration)), 360);
 			g.fillArc(getBounds().x, getBounds().y, getBounds().width, getBounds().height, 0, angle);
 		}
 		
 		g.setColor(Color.black);
 		g.drawOval(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
 		
 		
 	}
 
 	public Rectangle getBounds() {
 		double blockSize = factory.getBlockSize();
 		double pixelSize = factory.getPixelSize();
 		int x = (int) ((getCenterX() - blockSize/2)/pixelSize);
 		int y = (int) ((getCenterY() - blockSize/2)/pixelSize);
 		int w = (int) (blockSize/pixelSize);
 		int h = (int) (blockSize/pixelSize);
 		return new Rectangle(x,y,w,h);
 	}
 
 	public void setMoveLeft(boolean b) {
 		moveLeft = b;
 	}
 	public void setMoveRight(boolean b) {
 		moveRight = b;
 	}
 	public void setMoveTop(boolean b) {
 		moveTop = b;
 	}
 	public void setMoveBottom(boolean b) {
 		moveBottom = b;
 	}
 
 	public double getDistanceTo(double x, double y) {
 		double xpart = getCenterX() - x; xpart *= xpart; 
 		double ypart = getCenterY() - y; ypart *= ypart; 
 		return Math.sqrt(xpart + ypart);
 	}
 
 	public void setCenterX(double centerX) {
 		this.centerX = centerX;
 	}
 
 	public double getCenterX() {
 		return centerX;
 	}
 
 	public void setCenterY(double centerY) {
 		this.centerY = centerY;
 	}
 
 	public double getCenterY() {
 		return centerY;
 	}
 
 	public void setNextType(int nextType) {
 		this.nextType = nextType;
 	}
 
 	public int getNextType() {
 		return nextType;
 	}
 
 	public void doWork(int tool, long step) {
 		if (type == 0) return;
 		int transformation = getFactory().getTransformation(type, tool);
 		duration = getFactory().getTransformationTime(type, tool);
 		if (nextType!=-1 && nextType != transformation) {type = 0; nextType = -1;}
 		else if (nextType==-1) nextType = transformation;
 		if (nextType == transformation) currentWork += step;
 		if (Math.abs(currentWork) > duration + duration / 10) {type = 0; nextType = -1;}
 	}
 	
 	public void stopWork(){
 		if (type == 0) {nextType = -1; return;}
		if (Math.abs(currentWork - duration) < duration / 10) {type = nextType; nextType = -1; currentWork = 0;}
 		else if (currentWork > duration + duration / 10) {type = 0; nextType = -1; currentWork = 0;}
 	}
 }
