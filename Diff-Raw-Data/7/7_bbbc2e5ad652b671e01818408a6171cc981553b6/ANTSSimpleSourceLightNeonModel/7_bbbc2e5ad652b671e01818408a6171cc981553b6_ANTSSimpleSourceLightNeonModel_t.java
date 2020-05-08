 package models.sourceLight;
 
 import interfaces.ANTSIModel;
 
 import java.awt.Color;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 
 import models.ANTSAbstractModel;
 
 import basics.ANTSFactory;
 import basics.ANTSDevelopment.ANTSStream;
 
 public class ANTSSimpleSourceLightNeonModel extends ANTSAbstractModel implements ANTSIModel 
 {
 	private AffineTransform matrix;
 	private AffineTransform rotation;
 	private double length;
 	private double height;
 	private double angle;
 	
 	private boolean on;
 	private Color color;
 	
 	private int ticksBetweenTwoRays = 10;
 	private int tickCounter;
 	
 	private double rotationStep = 0.5;
 	
 	private double velocityRay = 1;
 	
 	//RayProperties:
 	
 	private int numberOfRaysPerLength = 1;
 	
 	public ANTSSimpleSourceLightNeonModel(double posX, double posY, double length, Color color, boolean isMouseListener, ANTSFactory factory)
 	{
 		super(factory);
 		
 		this.matrix = new AffineTransform();
 		this.rotation = new AffineTransform();
 		
 		this.matrix.setToTranslation(posX, posY);
 		
 		this.length = length;
 		this.height = 50;		//TODO
 		this.angle = 0;
 		
 		this.on = false;		//TODO
 		this.color = color;
 		
 		this.tickCounter = 0;
 		
 		this.isDragged = false;
 		this.isMouseListener = isMouseListener;
 		
 		this.updateCenter();
 	}
 	
 	///////////
 	//Getters//
 	///////////
 
 	private double getDistanceBetweenTwoRays()
 	{
 		return ((double) this.length)/((double) this.numberOfRaysPerLength);
 	}
 	
 	public int getNumberOfRays()
 	{
 		return this.numberOfRaysPerLength;
 	}
 	
 	public boolean isDragged()
 	{
 		return this.isDragged;
 	}
 	
 	public double getPosX()
 	{
 		return this.matrix.getTranslateX();
 	}
 
 	public double getPosY()
 	{
 		return this.matrix.getTranslateY();
 	}
 	
 	public double getLength()
 	{
 		return this.length;
 	}
 	
 	public double getHeight()
 	{
 		return this.height;
 	}
 	
 	public AffineTransform getMatrix()
 	{
 		return this.matrix;
 	}
 	
 	public Color getColor()
 	{
 		return this.color;
 	}
 	
 	public boolean isOn()
 	{
 		return this.on;
 	}
 	
 	public String toString()
 	{
 		return "Model: X: " + this.matrix.getTranslateX() + " Y: " + this.matrix.getTranslateY() + " Length : " +this.length + " COLOR: " + this.color;
 	}
 	
 	///////////
 	//Setters//
 	///////////
 	
 	public void setPosition(int x, int y)
 	{
 		this.matrix.setToTranslation(x, y);
 		this.updateCenter();
 	}
 	
 	public void rotate(int direction)
 	{
 		this.angle+=direction*this.rotationStep;
 		
 		this.rotation = new AffineTransform();
 		
 		this.rotation.rotate(Math.toRadians(direction*this.rotationStep),length/2,height/2);
 		
 		this.matrix.concatenate(this.rotation);
 		
 		ANTSStream.printDebug("angle" + this.angle);
 	}
 	
 	///////////
 	//Special//
 	///////////
 	
 	public void switchLight()
 	{
 		this.on = !this.on;
 	}
 	
 	@Override
 	public void update()
 	{
 		if(this.canSendRay())
 		{
 			this.createRays();
 		}
 	}
 	
 	private boolean canSendRay()
 	{
 		if(this.on)
 		{
 			if(this.tickCounter==0)
 			{
 				this.tickCounter++;
 				return true;
 			}
 			else if(this.tickCounter<this.ticksBetweenTwoRays)
 			{
 				this.tickCounter++;
 			}
 			else
 			{
 				this.tickCounter = 0;
 			}
 			
 			return false;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	private void createRays()
 	{
 		double tmpPosX = 0;
 		
 		for(int numberRay = 0; numberRay<this.getNumberOfRays(); numberRay++)
 		{
 			Point2D.Double pointUp = new Point2D.Double(tmpPosX, 0);
 			Point2D.Double pointDown = new Point2D.Double(tmpPosX, this.height);
 			
 			this.matrix.transform(pointUp, pointUp);
 			this.matrix.transform(pointDown, pointDown);
 			
 			double[] posUp = {pointUp.getX(),pointUp.getY()};
 			double[] posDown = {pointDown.getX(),pointDown.getY()};
 			
 			this.factory.createSimpleRayLight(posUp, this.velocityRay, this.angle-90, this.color);
 			this.factory.createSimpleRayLight(posDown, this.velocityRay, this.angle+90 , this.color);
 			
 			tmpPosX += this.getDistanceBetweenTwoRays();
 		}
 	}
 	
 	private void updateCenter() 
 	{
 		this.center[0] = this.matrix.getTranslateX()+this.length/2;
 		this.center[1] = this.matrix.getTranslateY()+this.height/2;
 		
		this.rotation = new AffineTransform();
		
		this.rotation.rotate(Math.toRadians(this.angle),length/2,height/2);
		
		this.matrix.concatenate(this.rotation);
		
 	}
 }
