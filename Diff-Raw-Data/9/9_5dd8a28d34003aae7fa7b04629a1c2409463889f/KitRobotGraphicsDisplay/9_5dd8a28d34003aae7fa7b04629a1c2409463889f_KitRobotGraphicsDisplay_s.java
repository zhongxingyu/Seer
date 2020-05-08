 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 
 public class KitRobotGraphicsDisplay  extends DeviceGraphicsDisplay {
 	//double  x,y;
 	
 	//Rectangle2D.Double rectangle;
 	Rectangle2D.Double rectangle1;
 	JLabel imageLabel;
 	//Positions
 	public enum Position{conveyorPosition,inspectionPosition,location1Position,location2Position;}
 	Position position;	
 	
 	//Commands  
 	public enum Command{moveToConveyor,moveToInspectionStand,moveToLocation1,moveToLocation2};
 	
 	Command moveToInitialPosition;
 	Command moveToFinalPosition;
 	
 	ImageIcon image;
 	boolean initialJob;
 	boolean finalJob;
 	boolean jobIsDone;
 	
 	int degreeStep;
 	int currentDegree;
 	int finalDegree;
 	
 	int rotationAxisX;
 	int rotationAxisY;
 	
 	int kitRobotPositionX;
 	int kitRobotPositionY;
 	AffineTransform trans;
 	//Kit kit=new Kit();
 	Client client;
 	Location location;
 	ArrayList<KitGraphicsDisplay> kits=new ArrayList<KitGraphicsDisplay>();
 	KitGraphicsDisplay currentKit =new KitGraphicsDisplay(client, new Location(0,0)); 
 	
 	public KitRobotGraphicsDisplay(Client cli,  Location loc){
 		
 		//super();
 		location = loc;
 		client = cli;
 		
 		moveToInitialPosition=Command.moveToConveyor;
 		moveToFinalPosition=Command.moveToConveyor;
 		position=Position.conveyorPosition;
 		initialJob=false;
 		finalJob=false;
 		jobIsDone=true;
 		degreeStep=1;
 		currentDegree=0;
 		finalDegree=0;
 		
 		trans= new AffineTransform();
 		
 		//image =new ImageIcon(this.getClass().getResource("/resource/Square.jpg"));
 		rotationAxisX=25;
 		rotationAxisY=25;
		kitRobotPositionX=135;
 		kitRobotPositionY=215;
 		
 		trans.translate(kitRobotPositionX,kitRobotPositionY);		
 		rectangle1 = new Rectangle2D.Double(0,0,600,400);
 		
 	}
 	
 	public void changeKit(KitGraphicsDisplay k){
 		kits.add(k);
 	}
 	
 	public void removeKit(KitGraphicsDisplay k){
 		kits.remove(k);
 	}
 	
 	
 	
 	public void placeKitOnStand(int i){
 		if(i==1)
 		{
 			ConveyorToLocation1();
 		}
 		else if(i==2)
 		{
 			ConveyorToLocation2();
 		}
 	}	
 	
 	public void ConveyorToLocation1(){
 		jobIsDone=false;
 		initialJob=true;
 		moveToInitialPosition=Command.moveToConveyor;
 		moveToFinalPosition=Command.moveToLocation1;
 		position=Position.conveyorPosition;
 		moveToInitial();
 	}
 	
 	public void ConveyorToLocation2(){
 		jobIsDone=false;
 		initialJob=true;
 		moveToInitialPosition=Command.moveToConveyor;
 		moveToFinalPosition=Command.moveToLocation2;
 		moveToInitial();
 	}
 	public void Location1ToInspectionStand(){
 		jobIsDone=false;
 		initialJob=true;
 		moveToInitialPosition=Command.moveToLocation1;
 		moveToFinalPosition=Command.moveToInspectionStand;
 		moveToInitial();
 	}
 	
 	public void Location2ToInspectionStand(){
 		jobIsDone=false;
 		initialJob=true;
 		moveToInitialPosition=Command.moveToLocation2;
 		moveToFinalPosition=Command.moveToInspectionStand;
 		moveToInitial();
 	}
 	public void InspectionStandToConveyor(){
 		jobIsDone=false;
 		initialJob=true;
 		moveToInitialPosition=Command.moveToInspectionStand;
 		moveToFinalPosition=Command.moveToConveyor;
 		moveToInitial();
 	}
 	
 	public void Location1ToLocation2(){
 		jobIsDone=false;
 		initialJob=true;
 		moveToInitialPosition=Command.moveToLocation1;
 		moveToFinalPosition=Command.moveToLocation2;
 		
 		moveToInitial();
 	}
 
 	
 	
 	public void moveToInitial(){
 			if(position.equals(Position.conveyorPosition))
 			{
 				if(moveToInitialPosition.equals(Command.moveToInspectionStand))
 				{
 					finalDegree=90;
 					position=Position.inspectionPosition;
 				}
 				else if(moveToInitialPosition.equals(Command.moveToLocation1))
 				{
 					finalDegree=180;
 					position=Position.location1Position;
 				}
 				else if(moveToInitialPosition.equals(Command.moveToLocation2))
 				{
 					finalDegree=270;
 					position=Position.location2Position;
 				}
 				else
 				{
 					finalDegree=0;
 				}
 			}
 			else if(position.equals(Position.inspectionPosition))
 			{
 				if(moveToInitialPosition.equals(Command.moveToLocation1))
 				{
 					finalDegree=90;
 					position=Position.location1Position;
 				}
 				else if(moveToInitialPosition.equals(Command.moveToLocation2))
 				{
 					finalDegree=180;
 					position=Position.location2Position;
 				}
 				else if(moveToInitialPosition.equals(Command.moveToConveyor))
 				{
 					finalDegree=270;
 					position=Position.conveyorPosition;
 				}
 				else
 				{
 					finalDegree=0;
 				}
 			}
 			else if(position.equals(Position.location1Position))
 			{
 				if(moveToInitialPosition.equals(Command.moveToLocation2))
 				{
 					finalDegree=90;
 					position=Position.location2Position;
 				}
 				else if(moveToInitialPosition.equals(Command.moveToConveyor))
 				{
 					finalDegree=180;
 					position=Position.conveyorPosition;
 				}
 				else if(moveToInitialPosition.equals(Command.moveToInspectionStand))
 				{
 					finalDegree=270;
 					position=Position.inspectionPosition;
 				}
 				else
 				{
 					finalDegree=0;
 				}
 			}
 			else if(position.equals(Position.location2Position))
 			{
 				if(moveToInitialPosition.equals(Command.moveToConveyor))
 				{
 					finalDegree=90;
 					position=Position.conveyorPosition;
 				}
 				else if(moveToInitialPosition.equals(Command.moveToInspectionStand))
 				{
 					finalDegree=180;
 					position=Position.inspectionPosition;
 				}
 				else if(moveToInitialPosition.equals(Command.moveToLocation1))
 				{
 					finalDegree=270;
 					position=Position.location1Position;
 				}
 				else
 				{
 					finalDegree=0;
 				}
 			}
 	}
 	
 	public void moveToFinal(){
 		if(position.equals(Position.conveyorPosition))
 		{
 			if(moveToFinalPosition.equals(Command.moveToInspectionStand))
 			{
 				finalDegree=90;
 				position=Position.inspectionPosition;
 			}
 			else if(moveToFinalPosition.equals(Command.moveToLocation1))
 			{
 				finalDegree=180;
 				position=Position.location1Position;
 			}
 			else if(moveToFinalPosition.equals(Command.moveToLocation2))
 			{
 				finalDegree=270;
 				position=Position.location2Position;
 			}
 		}
 		else if(position.equals(Position.inspectionPosition))
 		{
 			if(moveToFinalPosition.equals(Command.moveToLocation1))
 			{
 				finalDegree=90;
 				position=Position.location1Position;
 			}
 			else if(moveToFinalPosition.equals(Command.moveToLocation2))
 			{
 				finalDegree=180;
 				position=Position.location2Position;
 			}
 			else if(moveToFinalPosition.equals(Command.moveToConveyor))
 			{
 				finalDegree=270;
 				position=Position.conveyorPosition;
 			}
 			
 		}
 		else if(position.equals(Position.location1Position))
 		{
 			if(moveToFinalPosition.equals(Command.moveToLocation2))
 			{
 				finalDegree=90;
 				position=Position.location2Position;
 			}
 			else if(moveToFinalPosition.equals(Command.moveToConveyor))
 			{
 				finalDegree=180;
 				position=Position.conveyorPosition;
 			}
 			else if(moveToFinalPosition.equals(Command.moveToInspectionStand))
 			{
 				finalDegree=270;
 				position=Position.inspectionPosition;
 			}
 		}
 		else if(position.equals(Position.location2Position))
 		{
 			if(moveToFinalPosition.equals(Command.moveToConveyor))
 			{
 				finalDegree=90;
 				position=Position.conveyorPosition;
 			}
 			else if(moveToFinalPosition.equals(Command.moveToInspectionStand))
 			{
 				finalDegree=180;
 				position=Position.inspectionPosition;
 			}
 			else if(moveToFinalPosition.equals(Command.moveToLocation1))
 			{
 				finalDegree=270;
 				position=Position.location1Position;
 			}
 		}
 		currentKit.setFinalDegree(finalDegree);
 	}
 	
 	public void checkDegrees(){
 		if(currentDegree==finalDegree)
 		{
 			if(initialJob)
 			{
 				initialJob=false;
 				finalJob=true;
 				currentDegree=0;
 				System.out.println("Passed through initial job");
 				moveToFinal();
 			}
 			else if(finalJob)
 			{
 				finalJob=false;
 				jobIsDone=true;
 				currentDegree=0;
 			}
 		}
 	}
 	
 
 	public void receiveData(Request req){
 		String command = req.getCommand();
 		String target = req.getTarget();
 		Object obj = req.getData();
 		if(target.equals(Constants.KIT_ROBOT_TARGET))
 		{
 			if(command.equals("moveKitToStand1"))
 			{	
 				KitGraphicsDisplay kit=new KitGraphicsDisplay(client, new Location(0,0));
 				kit.setPosition(3);
 				currentKit=kit;
 				kits.add(kit);
 				ConveyorToLocation1();
 			}
 			else if(command.equals("moveit"))
 			{
 				System.out.println("goes through moveit command");
 				for(int i=0; i<kits.size(); i++)
 				{
 					if(kits.get(i).getPosition()==3)
 					{
 						
 						currentKit=kits.get(i);
 						kits.get(i).setPosition(4);
 					}
 				}
 				
 				Location1ToLocation2();
 			}
 			else if(command.equals("moveKitToStand2"))
 			{
 				KitGraphicsDisplay kit=new KitGraphicsDisplay(client, new Location(0,0));
 				kit.setPosition(4);
 				currentKit=kit;
 				kits.add(kit);
 				ConveyorToLocation2();
 			}
 			else if(command.equals("moveKitInLocation1ToInspection"))
 			{
 				for(int i=0; i<kits.size(); i++)
 				{
 					if(kits.get(i).getPosition()==3)
 					{
 						currentKit=kits.get(i);
 						kits.get(i).setPosition(2);
 					}
 				}
 				Location1ToInspectionStand();
 			}
 			else if(command.equals("moveKitInLocation2ToInspection"))
 			{
 				for(int i=0; i<kits.size(); i++)
 				{
 					if(kits.get(i).getPosition()==4)
 					{
 						currentKit=kits.get(i);
 						kits.get(i).setPosition(2);
 					}
 				}
 				Location2ToInspectionStand();
 			}
 			
 		}
 		
 	}
 	
 	
 	public void doJob()
 	{
 		if(!jobIsDone)
 		{
 			trans.rotate(Math.toRadians(degreeStep),rotationAxisX,rotationAxisY);
 			currentDegree++;
 			System.out.println("currentDegree: "+ currentDegree);
 		}
 	}
 	
 	public void rotateKit(){
 		
 	}
 	
 	public void draw(JComponent c, Graphics2D g)
 	{
 		checkDegrees();
 		doJob();
 		
 		for(int i=0;i<kits.size(); i++)
 		{
 			kits.get(i).drawRotate(c,g);;
 		}
 		
 		//Image image=Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resource/Square.jpg"));
 		g.drawImage(Constants.KIT_ROBOT_IMAGE, trans,null);
 	
 	}
 	
 	
 	/*
 	public void draw(JComponent c, Graphics2D g) {
 		g.drawImage(kitImage, kitLocation.getX(), kitLocation.getY(), c);
 		
 	}
 	*/
 	/*
 	public void paint(Graphics g){
 	
 		//Graphics2D g2=(Graphics2D)g;
 		//g2.setColor(Color.yellow);
 		//g2.fill(rectangle1);
 		//trans.rotate(Math.toRadians(1),-50,-50);
 		
 		//trans = g2.getTransform();
 		//g2.rotate(Math.toRadians(rotation), imageX, imageY);
 		//image.paintIcon(this, g2, imageX, imageY); 
 		//trans.rotate(.01, 225, 125);
 		//trans.rotate(Math.toRadians(1),rotationAxisX,rotationAxisY);
 		Image image=Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resource/Square.jpg"));
 		g2.drawImage(image, trans,null);
 		kit.paint(g2);
 		g2.finalize();
 	}
 	*/
 	/*
 	public static void main(String args[]){
 		JFrame frame = new JFrame("RotateImage");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(600,400);
 		KitRobotGraphicsDisplay rke= new KitRobotGraphicsDisplay();
 		frame.setContentPane(rke);
 		frame.setVisible(true);
 		rke.ConveyorToLocation1();//added this command
 		new javax.swing.Timer(100,rke).start();
 		
 	}
 	*/
 	
 	@Override
 	public void setLocation(Location newLocation) {
 		location=newLocation;
 		// TODO Auto-generated method stub
 	}
 	
 	
 }
