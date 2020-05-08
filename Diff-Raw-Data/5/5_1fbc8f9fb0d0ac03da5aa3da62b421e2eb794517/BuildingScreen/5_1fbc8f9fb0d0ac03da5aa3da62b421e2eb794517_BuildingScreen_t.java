 package menuscene;
 
 import java.util.*;
 
 import pulpcore.Stage;
 import pulpcore.image.CoreImage;
 import pulpcore.scene.Scene2D;
 import pulpcore.sprite.Button;
 import pulpcore.sprite.Group;
 
 import utility.ResourceHandler;
 import component.Culture;
 import global.GlobalDef;
 import building.*;
 
 public class BuildingScreen extends Scene2D{
 
 	final int width  = 100;
 	
 	CoreImage[] buildTileImg;
 	Button[][] buildBtn = new Button[4][4];
 	Group buildForm;
 	
 	Culture player;
 	int maxNumOfBuilding;
 	int constrctedNum;
 	
 	public void Init(Culture culture, int max)
 	{
 		player = culture;
 		maxNumOfBuilding = max;
 		constrctedNum = 0;
 	}
 	
 	public void load()
 	{
 		buildTileImg = CoreImage.load("/resource/buildTile.jpg").split(12, 4);
 		buildForm = new Group(Stage.getWidth() / 2 - 200, Stage.getHeight() / 2 - 200, 400, 400);
 		
 		for(int row = 0; row < 4; row++)
 			for(int col = 0; col < 4; col++)
 			{
 				int id = row * 4 + col;
 	
 				if( id <  14 )
 				{
 					CoreImage[] img = new CoreImage[]
 						{buildTileImg[row * 12 + col], buildTileImg[row * 12 + col + 4], buildTileImg[row * 12 + col + 8]};
 					buildBtn[row][col] = new Button(img, col * width, row * width);
 					// if player's resource is not enough for this building. disable it
 					if(!EnoughResource(id) && !hasBuild(id))
 						buildBtn[row][col].setImage(img[2]);
 					buildForm.add(buildBtn[row][col]);
 				}
 				
 			}
 			
 		add(buildForm);
 	}
 	
 	@Override
     public void update(int elapsedTime) 
 	{
 		// determine which tile has been selected
 		int ID;
 		for(int row = 0; row < 4; row++)
 			for(int col = 0; col < 4; col++)
 			{
 				ID = row * 4 + col;
 				if(ID < 14)
 				{
 					// drawing
 					if(!EnoughResource(ID))
 						buildBtn[row][col].setImage(buildTileImg[row * 12 + col + 8]);
 					else if(hasBuild(ID))
 					{
 						if(ID != 0)
 							buildBtn[row][col].setImage(buildTileImg[row * 12 + col + 8]);
 						else{ // house
 							if(player.getGameBoard().getNumOfVillager() >= 10)
 								buildBtn[row][col].setImage(buildTileImg[row * 12 + col + 8]);
 						}
 					}
 					
 					// build this one
 					if(buildBtn[row][col].isClicked() && EnoughResource(ID))
 					{
 						// building other than house
 						if(!hasBuild(ID) && ID > 0)
 						{
 							Building newBuild = GlobalDef.getBuildingMap().get(ID);
 							player.getGameBoard().PlaceBuilding(newBuild, ID);
 							constrctedNum++;
 						}else if(ID == 0 && player.getGameBoard().getNumOfVillager() <= 10) // house
 						{
 							Building newBuild = GlobalDef.getBuildingMap().get(ID);
 							player.getGameBoard().PlaceBuilding(newBuild, ID);
 							constrctedNum++;
 						}
 						 
 					}
 					
 				}
 			}
 		
		// determine whether player's resource can not build any building 
 		boolean meet = false;
 		for(int row = 0; row < 4; row++){
 			for(int col = 0; col < 4; col++)
 			{
 				ID = row * 4 + col;
				if(ID < 14 && EnoughResource(ID) && !hasBuild(ID)){
 					meet = true;
 					break;
 				}
 					
 			}
 			
 			if(meet)
 				break;
 		}
 		
 		if(constrctedNum == maxNumOfBuilding || meet == false)
 			Stage.popScene();
 	}
 	
 	// check whether player's resource is qualified for certain building
 	private boolean EnoughResource(int ID)
 	{	
 		Hashtable<GlobalDef.Resources, Integer> cost = GlobalDef.getBuildingMap().get(ID).getCost();
 		return ResourceHandler.isResEnough(player.getGameBoard().getHoldResource(), cost);
 	}
 	
 	// check whether this building has been build
 	private boolean hasBuild(int ID)
 	{
 		return player.getB_build().get(GlobalDef.getBuildingMap().get(ID));
 	}
 	
 }
