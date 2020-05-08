 
 package hhu.propra_2013.gruppe_13;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 
 public class CoreRoom {
 
	//Geerbtes <- gelogen!!
 //	private Figure 		figure;
 	
 	//Sachen die jeder Raum weiß
 	private ArrayList<CoreGameObjects> content; //die alte innere Array list, enthält alle Objekte im Raum
 	private boolean 	isBossRoom, isShop;
 	private boolean 	hasTopNeighbour, hasBottomNeighbour, hasLeftNeighbour, hasRightNeighbour;
 	private boolean 	isFinished;//for later use in the map
 	
 	//Variablen zur Erzeugung
 	private int 		element , column, line , dest;
 	private int 		randomNumber; //speichert die Zufallsvariablevariable in der Raumerzeugung, je nachdem ob es ein  bossraum ist oder nicht
 	private String 		type;
 	private int 		stage; //Nummer des Levels, für den NPC
 	private int			mode;
 	
 	// Stream and Reader for reading data from file
 	private InputStream roomStream 	= null;
 	private Reader roomReader		= null;
 	
 	// Constructor
 	CoreRoom(int inStage, String inBoss, boolean inTopNeighbour, boolean inBottomNeighbour, boolean inLeftNeighbour, boolean inRightNeighbour, int inMode){
 //		figure 				= inFigure;
 		content 			= new ArrayList<CoreGameObjects>();
 		stage 				= inStage;
 		mode				= inMode;
 		
 		//ja, ich hasse mich auch schon für solche langen namen
 		hasTopNeighbour 	= inTopNeighbour;  
 		hasBottomNeighbour 	= inBottomNeighbour;
 		hasLeftNeighbour 	= inLeftNeighbour;
 		hasRightNeighbour 	= inRightNeighbour;
 	//	System.out.println("links "+hasLeftNeighbour+" rechts "+hasRightNeighbour+" oben "+hasTopNeighbour+" unten "+hasBottomNeighbour);
 	}
 	
 	void setType(String inType) {
 		type = inType;
 		return;
 	}
 	
 	
 	//liest den Inhalt des Raums aus einer Datei ein
 	public void buildRoom(){
 		
 		//Konstanten anpassen sobald es mehr Räume gibt!!! TODO: Automatisieren
 		//festlegen welche Raumliste der Builder durchgeht
 		if (type == "Raum"){
 			randomNumber =(int)(13*Math.random());
 		}
 		else if (type == "BossRaum"){
 			randomNumber =(int)(3*Math.random());	
 			isBossRoom = true;
 			System.out.println("yay, like a baus");
 		}
 		else if (type == "Shop"){
 			randomNumber = 0;
 			isShop = true;
 		}
 		else if (type == "Start"){
 			randomNumber = 0;
 		}
 		
 	
 		
 		// First read all Walls into the ArrayList, that way Walls will  be drawn in  the background
 		try {
 			roomStream = new FileInputStream("Level/"+type+randomNumber+".txt");
 			roomReader = new InputStreamReader (roomStream);
 			
 			element = 0;
 			column = 0; 
 			line = 0;	
 			dest = 0;
 			
 			while ((element = roomReader.read()) != -1){ //Goes trough the whole raumX.txt, and spawns Objects at their Positions
 				switch (element) { 	//ASCII: W=87 D=68 E=69
 				case 'W':			//In order of probability
 					content.add(new MISCWall(column-1+0.5, line-1+0.5, 1, 1, 1)); 	//-1 because the top left corner seems to have
 					break;															//the coordinates 1:1
 				}
 				
 				column++; //sets column up for the next cycle of the switch-case
 				
 				if (column==25){ //since we use 24 chars per line, the 25th should trigger the next line
 					column = 0;
 					line++;
 				}
 			}
 		}
 		catch (IOException e) {
 			System.out.println("File not found, system exiting.");
 			System.exit(1);
 		} finally {
 			try {
 				roomReader.close();
 			} catch (IOException e) {
 			}
 			try {
 				roomStream.close();
 			} catch (IOException e) {
 			}
 		}
 		
 		// Put all other stuff into the ArrayList
 		try {
 			System.out.println("Walls added");
 			roomStream = new FileInputStream("Level/"+type+randomNumber+".txt");
 			roomReader = new InputStreamReader (roomStream);
 			
 			element = 0;
 			column = 0; 
 			line = 0;	
 			dest = 0;
 			
 			while ((element = roomReader.read()) != -1){ //Goes trough the whole raumX.txt, and spawns Objects at their Positions
 				
 				switch (element) {
 				
 				case 'E':
 					content.add(new EnemyRanged(column-1+0.5, line-1+0.5, 1, 1, Enemy.ENEMY_FIRE_SHOOTING, stage, mode));
 					break;
 
 				case 'D': //looks where the door is, then sets destination accordingly
 					//I have no clue why this works
 					if (line == 0 && hasTopNeighbour)		{dest = 0; content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, dest));} //Door is on the upper edge of the field, door should lead up
 					if (line == 14 && hasBottomNeighbour)	{dest = 2; content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, dest));} //Door is on the bottom edge of the field, door should lead down
 					if (column==23 && hasRightNeighbour)	{dest = 1; content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, dest));} //Door is on the right edge of the field, door should lead right
 					if (column==0 && hasLeftNeighbour)		{dest = 3; content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, dest));} //Door is on the left edge of the field, door should lead left
 					 //creating door with correct destination
 					break;	
 						
 				case 'I':
 					int randItem;
 					randItem = (int)(4*Math.random());
 					
 					switch(randItem){
 					
 					case 0 :
 						content.add(new ItemCupACoffee(column-1+0.5, line-1+0.5, 1, 1, 1));
 						break;
 						
 					case 1 :
 						content.add(new ItemChocolateBar(column-1+0.5, line-1+0.5, 1, 1, 1));
 						break;
 						
 					case 2 :
 						content.add(new ItemArmor(column-1+0.5, line-1+0.5, 1, 1, 1));
 						break;
 					case 3:
 						content.add(new ItemImproveWeapon(column-1+0.5, line-1+0.5, 1, 1, 1));
 						break;
 					}
 					
 					break;
 				
 				case 'S':
 					//content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, 4));//'Door' leads to the next floor
 					break;
 					
 				case 'R': 
 					content.add(new ItemResurrect(column-1+0.5, line-1+0.5,1,1));
 					break;
 					
 				case 'G':
 					content.add(new ItemMoney (column-1+0.5, line-1+0.5,1,1));
 					break;
 					
 				case 'N':
 					content.add(new MISCNPC (column-1+0.5, line-1,1,1, "this is a stub",stage));
 					break;
 				case 'F':
 					content.add(new EnemyMelee(column-1+0.5, line-1+0.5, 1, 1, Enemy.ENEMY_FIGURE_RUN, stage, mode));
 
 					break;
 				case 'B':
 
 					content.add(new EnemyBossMelee(column-1+0.5, line-1+0.5, 1 , 1 , Enemy.ENEMY_FIGURE_RUN, stage, this, mode));
 					break;				
 				}
 				column++; //sets column up for the next cycle of the switch-case
 					
 				if (column==25){ //since we use 24 chars per line, the 25th should trigger the next line
 					column = 0;
 					line++;
 				}
 			}
 			
 		} catch (IOException e) {
 			System.out.println("File \"Level/"+type+randomNumber+".txt\" not found, system exiting.");
 			System.exit(1);
 		} finally {
 			try {
 				roomReader.close();
 			} catch (IOException e) {
 			}
 			try {
 				roomStream.close();
 			} catch (IOException e) {
 			}
 		}
 		
 	}
 	
 	//Getter
 	//***********************************************************************************************************
 	boolean getFinished(){
 		return isFinished;
 	}
 	
 	boolean getBossRoom(){
 		return isBossRoom;
 	}
 	
 	boolean getShop(){
 		return isShop;
 	}
 
 	ArrayList<CoreGameObjects> getContent() {
 		return content;
 	}
 	
 	boolean getTopNeighbour(){
 		return hasTopNeighbour;
 	}
 	
 	boolean getBottomNeighbour(){
 		return hasTopNeighbour;
 	}
 	
 	boolean getLeftNeighbour(){
 		return hasLeftNeighbour;
 	}
 	
 	boolean getRightNeighbour(){
 		return hasLeftNeighbour;
 	}
 	//***********************************************************************************************************
 	//Setter
 	//***********************************************************************************************************
 	
 	void setFinished(boolean inFinished){
 		isFinished = inFinished;
 		return;
 	}
 	
 	void setTopNeighbour(boolean inTopNeighbour){
 		hasTopNeighbour = inTopNeighbour;
 		return;
 	}
 	
 	void setBottomNeighbour(boolean inBottomNeighbour){
 		hasBottomNeighbour = inBottomNeighbour;
 		return;
 	}
 	
 	void setLeftNeighbour(boolean inLeftNeighbour){
 		hasLeftNeighbour = inLeftNeighbour;
 		return;
 	}
 	
 	void setRightNeighbour(boolean inRightNeighbour){
 		hasRightNeighbour = inRightNeighbour;
 		return;
 	}
 }
