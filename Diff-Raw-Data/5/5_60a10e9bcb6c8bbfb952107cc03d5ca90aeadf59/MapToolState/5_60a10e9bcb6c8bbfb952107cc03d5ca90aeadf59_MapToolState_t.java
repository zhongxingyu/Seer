 package DD.GUI;
 
 import java.awt.Font;
 
 
 import org.lwjgl.Sys;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import DD.Character.DDCharacter;
 import DD.Character.CharacterSheet.Monster.Goblin;
 import DD.MapTool.*;
 import DD.SlickTools.Component;
 import DD.SlickTools.DDImage;
 import DD.SlickTools.RenderComponent;
 
 import org.newdawn.slick.*;
 
 public class MapToolState extends BasicGameState {
 	
 	private int stateID = 0;
 	private MapTool maptool = null;
 	private String mousePos;
 	private int x1, x2, y1, y2;	//coordinates for last two mouse clicks on map
 	private Objects object = null;	//object used to place Objects onto the map
 	private boolean clicked = true;
 	static Input mouse = new Input(650);
 	int posX;
 	int posY;
 	
 	//Images
 	private Image spriteSheet = null;
 	private Image makeSelection = null;
 	private Image removeSelection = null;
 	private Image placeOnMap = null;
 	private Image removeFromMap = null;
 	private Image goblinButton = null;
 	private Image wallButton = null;
 	private Image saveMap = null;
 	private Image loadMap = null;
 	private Image loadWorld = null;
 	private Image clearSelection = null;
 	private Image grass = null;
 	private TextField loadMapText = null;
 	private TextField loadWorldText = null;
 	UnicodeFont font = null;
 	
 	private boolean hotKeyActive = true;
 	
 	public MapToolState(int stateID) {
 		this.stateID = stateID;
 	}
 	
 	@Override
 	public void enter(GameContainer gc , StateBasedGame sbg) throws SlickException{
 		loadMapText = new TextField(gc, font, 945, 610, 180, 25);
 		loadMapText.setText("map0 - map24");
 		loadWorldText = new TextField(gc, font, 945, 570, 180, 25);
 		loadWorldText.setText("Unimplemented"); 
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb)
 			throws SlickException {
 		//don't show fps in top left corner
 		gc.setShowFPS(false);
 		
 		//Initialize Images
 		spriteSheet = new Image("Images/Test/DungeonCrawl_ProjectUtumnoTileset.png");
 		makeSelection = new Image("Images/MapTool/MakeSelection.png");
 		removeSelection = new Image("Images/MapTool/RemoveSelection.png");
 		placeOnMap = new Image("Images/MapTool/PlaceOnMap.png");
 		removeFromMap = new Image("Images/MapTool/RemoveFromMap.png");
 		goblinButton = new Image("Images/MapTool/Goblin.png");
 		wallButton = new Image("Images/MapTool/Wall.png");
 		saveMap = new Image("Images/MapTool/SaveButton.png");
 		loadMap = new Image("Images/MapTool/LoadMap.png");
 		loadWorld = new Image("Images/MapTool/LoadWorld.png");
 		clearSelection = new Image("Images/MapTool/ClearSelection.png");
 		grass = new Image("Images/MapTool/grassButton.png");
 		
 		font  = new UnicodeFont(new Font("Arial" , Font.PLAIN , 16));
 		font.getEffects().add(new ColorEffect(java.awt.Color.white));
 		font.loadGlyphs();
 		
 	
 		maptool = new MapTool();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
 		//Render Map
     	RenderComponent renderComponent = null;
     	
 //    	for(int i = 0; i < maptool.getCurrentMap().mapSize; i++) {
 //    		for(int j = 0; j < maptool.getCurrentMap().mapSize; j++) {
 //    			Objects[] list = new Objects[maptool.getCurrentMap().objectsStack[i][j].size()];
 //    			System.arraycopy(maptool.getCurrentMap().objectsStack[i][j].toArray(), 0, list, 0, maptool.getCurrentMap().objectsStack[i][j].size());
 //    			for(int k = list.length; k > 0; k--) {
 //    				Component component = (Component)list[k-1];
 //    				if (RenderComponent.class.isInstance(component))
 //    				{
 //    					renderComponent = (RenderComponent) component;
 //    					renderComponent.render(gc, sb, g);
 //    				}
 //    			}
 //    		}
 //    	}
     	maptool.getCurrentMap().render(gc, sb, g);
     	
     	g.drawString("MAP TOOL", 0, 0);
 		
     	//draw buttons to the screen
     	makeSelection.draw(625, 0);
     	removeSelection.draw(825, 0);
     	clearSelection.draw(1050, 0);
     	placeOnMap.draw(625, 40);
     	removeFromMap.draw(805, 40);
     	g.drawString("Available To Place", 625, 80);
     	goblinButton.draw(625, 100);
     	wallButton.draw(735, 100);
     	saveMap.draw(630, 610);
     	loadMap.draw(780, 610);
     	loadWorld.draw(780,570);
     	grass.draw(820,100);
     	g.setFont(font);
     	if(loadMapText!=null) (loadMapText).render(gc,g);
     	if(loadWorldText!=null) (loadWorldText).render(gc,g);
     	g.drawString("BACK", 1130, 615);
     	g.drawString(mousePos, 900, 0);
     	
     	g.drawString("Hot Key List:", 975, 175);
     	g.drawString("Make Selection (w)", 975, 200);
     	g.drawString("Remove Selection (e)", 975, 225);
     	g.drawString("Clear Selection (r)", 975, 250);
     	g.drawString("Place Selection on Map (s)", 975, 275);
     	g.drawString("Remove from Map (d)", 975, 300);
     	g.drawString("Goblin (1)", 975, 325);
     	g.drawString("Wall (2)", 975, 350);
     	g.drawString("Grass (3)", 975, 375);
     	
     	String instructions = "";
     	instructions += "Map Tool Instructions: \n";
     	instructions += "1.) Click on two squares and click 'Make\n" + 
     					"Selection' to select squares\n";
     	instructions += "2.) You can click 'Remove Selection'\n" +
     					"to deselect the squares. You\n" +
     					"can also click 'Clear Selection'\n" +
     					"to remove all selection boxes\n";
     	instructions += "3.) With the squares selected, click on\n" +
     					"one of the objects in the 'Available\n" +
     					"To Place' list\n";
     	instructions += "4.) Click on 'Place On Map' to place\n" +
     					"the selected object on the map\n";
     	instructions += "5.) To remove objects from the map,\n" +
     					"repeat step 1 and click 'Remove\n" +
     					"From Map' to remove the object\n";
     	
     	g.drawString(instructions, 625, 175);
     	
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
 		font.loadGlyphs();
 		posX = mouse.getMouseX();
     	posY = mouse.getMouseY();
 		mousePos = "Mouse position: " + posX + " " + posY;
 		
 		clickMap(gc);
 		clickedTextBox(gc);
 		
 		
 		///Update Map
     	RenderComponent renderComponent = null;
     	for(int i = 0; i < maptool.getCurrentMap().mapSize; i++) {
     		for(int j = 0; j < maptool.getCurrentMap().mapSize; j++) {
     			Objects[] list = new Objects[maptool.getCurrentMap().objectsStack[i][j].size()];
     			System.arraycopy(maptool.getCurrentMap().objectsStack[i][j].toArray(), 0, list, 0, maptool.getCurrentMap().objectsStack[i][j].size());
     			for(int k = 0; k < list.length; k++) {
     				Component component = (Component)list[k];
     				if (RenderComponent.class.isInstance(component))
     				{
     					renderComponent = (RenderComponent) component;
     					renderComponent.update(gc, sb, delta);
     				}
     			}
     		}
     	}
 		
 		//update buttons on screen
 		backButton(gc, sb);
     	makeSelectionButton(gc);
     	removeSelectionButton(gc);
     	clearSelectionButton(gc);
     	placeOnMapButton(gc);
     	removeFromMapButton(gc);
     	goblinButton(gc);
     	wallButton(gc);
     	saveMapButton(gc);
     	loadMapButton(gc);
     	loadWorldButton(gc);
     	grassButton(gc);
     	
     	
     	//hotkeys
     	makeSelectionHotKey(gc);
     	makeRemoveHotKey(gc);
     	makeClearListHotKey(gc);
     	makePlaceOnMapHotKey(gc);
     	makeRemoveOnMapHotKey(gc);
     	goblinHotKey(gc);
     	wallHotKey(gc);
     	grassHotKey(gc);
     	
     	
 	}
 	
 	public void clickedTextBox(GameContainer gc){
 		if((posX > 940 && posX < 1125) && (posY > 565 && posY < 634)) {
 			if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
 	    		hotKeyActive = false;
 	    		System.out.println("hotKeyActive: "  +hotKeyActive);
     		}
 		}
 	}
 	
 	
 	//Clicking on map
 	public void clickMap(GameContainer gc) {
 		//NOTE THIS NEEDS TO BE THE FIRST THING IN UPDATE METHOD!!!
     	if((posX > 0 && posX < 620) && (posY > 40 && posY < 640)) {
     		//you are inside map area
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			hotKeyActive = true;
 	    		getMapCoord();
     		}
     	}
 	}
 	
 	
 	
 	//hotkey methods
 	public void makeSelectionHotKey(GameContainer gc) throws SlickException{
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_W))
         {
 			if(hotKeyActive){
 				maptool.getSelectedList().massAddSelectedList(x1, y1, x2, y2);
 				//System.out.println("selection hotkey");
 			}
         }
 	} 
 	public void makeRemoveHotKey(GameContainer gc) throws SlickException{
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_E))
         {
 			if(hotKeyActive){
 				maptool.getSelectedList().massRemoveSelectedList(x1, y1, x2, y2);
 				System.out.println("remove hotkey");
 			}
         }
 	}
 	public void makeClearListHotKey(GameContainer gc) throws SlickException{
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_R))
         {
 			if(hotKeyActive){
 				maptool.getSelectedList().clearSelectedList();
 				System.out.println("clearList hotkey");
 			}
         }
 	}
 	public void makePlaceOnMapHotKey(GameContainer gc) throws SlickException{
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_S))
         {
 			if(hotKeyActive){
 				maptool.getSelectedList().placeSelectedListOnMap(object);
 				System.out.println("placeOnMap HotKey");
 			}
         }
 	}
 	public void makeRemoveOnMapHotKey(GameContainer gc) throws SlickException{
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_D))
         {
 			if(hotKeyActive){
 				maptool.getSelectedList().removeSelectedListOnMap();
 				System.out.println("removeOnMap HotKey");
 			}
         }
 	}
 	
 	public void goblinHotKey(GameContainer gc) throws SlickException{
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_1))
         {
 			if(hotKeyActive){
 				DDCharacter goblinChar = new DDCharacter(stateID++);
     			goblinChar.setCharacterSheet(new Goblin());
     			goblinChar.setCharacterID(stateID++);
     			CharacterObjects goblinObj = new CharacterObjects("Goblin", goblinChar.getImage(), 0, 0, maptool.getCurrentMap(), goblinChar);
     			object = goblinObj;	//set object to the goblin object to place into the map 
     			System.out.println("chose goblin");
 				System.out.println("goblin hotkey");
 			}
         }
 	}
 	public void wallHotKey(GameContainer gc) throws SlickException{
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_2))
         {
 			if(hotKeyActive){
 				Wall wall = new Wall("Wall", maptool.getCurrentMap());
     			object = wall;
 				System.out.println("wall hotkey");
 			}
         }
 	}
 	public void grassHotKey(GameContainer gc) throws SlickException{
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_3))
         {
 			if(hotKeyActive){
 				Grass grass = new Grass("grass", new DDImage(1470, 577));
     			object = grass;
     			
 				System.out.println("grass hotkey");
 			}
         }
 	}
 	
 	
 	
 	//Make Selection Button
 	public void makeSelectionButton(GameContainer gc) throws SlickException {
     	if((posX > 625 && posX < 625 + makeSelection.getWidth()) && (posY > 0 && posY < makeSelection.getHeight())) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().massAddSelectedList(x1, y1, x2, y2);
     			System.out.println("selection button");
     		}
     	}
 	}
 	
 	//Remove selection Button
 	public void removeSelectionButton(GameContainer gc) throws SlickException {
     	if((posX > 825 && posX < 825 + removeSelection.getWidth()) && (posY > 0 && posY < removeSelection.getHeight())) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().massRemoveSelectedList(x1, y1, x2, y2);
     			System.out.println("remove selection button");
     		}
     	}
 	}
 	
 	public void clearSelectionButton(GameContainer gc) throws SlickException {
 		if((posX > 1050 && posX < 1050 + clearSelection.getWidth()) && (posY > 0 && posY < clearSelection.getHeight())) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().clearSelectedList();
     			System.out.println("clear selection button");
     		}
     	}
 	}
 	
 
 	//Place on map button
 	public void placeOnMapButton(GameContainer gc) throws SlickException {
 		if((posX > 625 && posX < 625 + placeOnMap.getWidth()) && (posY > 40 && posY < (40 + placeOnMap.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			if(object == null) {
     				
     			}
     			else {
 	    			maptool.getSelectedList().placeSelectedListOnMap(object);
 	    			System.out.println("place on map button");
     			}
     		}
     	}
 	}
 	
 	//Remove from map button
 	public void removeFromMapButton(GameContainer gc) throws SlickException {
 		if((posX > 805 && posX < 805 + removeFromMap.getWidth()) && (posY > 40 && posY < (40 + removeFromMap.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().removeSelectedListOnMap();
     			System.out.println("remove from map button");
     		}
     	}
 	}
 	
 	public void goblinButton(GameContainer gc) throws SlickException {
 		if((posX > 625 && posX < 625 + goblinButton.getWidth()) && (posY > 100 && posY < (100 + goblinButton.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			//Create a new goblin character
     			DDCharacter goblinChar = new DDCharacter(stateID++);
     			goblinChar.setCharacterSheet(new Goblin());
     			goblinChar.setCharacterID(stateID++);
     			CharacterObjects goblinObj = new CharacterObjects("Goblin", goblinChar.getImage(), 0, 0, maptool.getCurrentMap(), goblinChar);
     			object = goblinObj;	//set object to the goblin object to place into the map 
     			System.out.println("chose goblin");
     		}
     	}
 	}
 	
 	public void wallButton(GameContainer gc) throws SlickException {
 		if((posX > 735 && posX < 735 + wallButton.getWidth()) && (posY > 100 && posY < (100 + wallButton.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			Wall wall = new Wall("Wall", maptool.getCurrentMap());
     			object = wall;
     			System.out.println("chose wall");
     		}
     	}
 	}
 	public void grassButton(GameContainer gc) throws SlickException {
 		if((posX > 820 && posX < 820 + grass.getWidth()) && (posY > 100 && posY < (100 + grass.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			Grass grass = new Grass("grass", new DDImage(1470, 577));
     			object = grass;
     			
 				System.out.println("grass button");
     		}
     	}
 	}
 	
 	public void saveMapButton(GameContainer gc) throws SlickException {
 		if((posX > 630 && posX < 630 + saveMap.getWidth()) && (posY > 610 && posY < (610 + saveMap.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().clearSelectedList();
     			maptool.getWorld().writeMe();
     			System.out.println("save map");
     		}
     	}
 	}
 	
 	public void loadMapButton(GameContainer gc) throws SlickException {
 		if((posX > 780 && posX < 780 + loadMap.getWidth()) && (posY > 610 && posY < (610 + loadMap.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			for (int i = 0; i < maptool.world.getWorldSize(); i++) {
 					for (int j = 0; j < maptool.world.getWorldSize(); j++) {
 						System.out.println("iteration j:"+ j +" input: "+loadMapText.getText().trim()+ " mapname: "+ maptool.world.getMap(i, j).getName());
 						if((loadMapText.getText().trim()).equals(maptool.world.getMap(i, j).getName())){
 					
 							maptool.setCurrentMap(i, j);
 							maptool.selectedList.clearSelectedList();
 							maptool.selectedList.setOwner(maptool.getCurrentMap());
 						}
 					}
 				}
     			System.out.println(maptool.getCurrentMap());
 //    			World world = maptool.loadWorld("world", true);
 //    			//System.out.println(world.toString());
 //    			
 //    			System.out.println(world.getMap(0, 0).getObjectAtLocation(0, 0).image);
 //    			maptool.world = world;
 //    			maptool.setCurrentMap(0, 0);
 //    			System.out.println("hi");
 //    			maptool.selectedList.setOwner(maptool.getCurrentMap()) ;
 //    			try {
 //					maptool.selectedList.clearSelectedList();
 //				} 
 //    			catch (SlickException e) {
 //					e.printStackTrace();
 //				}
     			
     		}
     	}
 	}
 	
 	public void loadWorldButton(GameContainer gc) {
 		if((posX > 780 && posX < 780 + loadWorld.getWidth()) && (posY > 570 && posY < (570 + loadWorld.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			
     			
     			World world = maptool.loadWorld("world", true);
     			//System.out.println(world.toString());
     			
     			System.out.println(world.getMap(0, 0).getObjectAtLocation(0, 0).image);
     			maptool.world = world;
     			maptool.setCurrentMap(0, 0);
     			System.out.println("hi");
     			maptool.selectedList.setOwner(maptool.getCurrentMap()) ;
     			try {
 					maptool.selectedList.clearSelectedList();
 				} 
     			catch (SlickException e) {
 					e.printStackTrace();
 				}
     			
     		}
     	}
 	}
 	
 	//Back Button
 	public void backButton(GameContainer gc, StateBasedGame sb) {
     	if((posX > 1130 && posX < 1170) && (posY > 615 && posY < 630))
 		{
 			if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 			{
 				//go back to main menu
 				sb.enterState(0);
 				loadMapText.setLocation(2000, 2000);
 				loadWorldText.setLocation(2000, 2000);
 			}
 		}
 	}
 	
 	public void getMapCoord() {
 		if(clicked) {
 			x1 = (int)(posX / 30.85);
			y1 = (int)(posY / 30.5) - 1;
 			System.out.println("x1: " + x1 + " y1: " + y1);
 			clicked = false;
 		}
 		else if(!clicked) {
 			x2 = (int)(posX / 30.85);
			y2 = (int)(posY / 30.5) - 1;
 			System.out.println("x2: " + x2 + " y2: " + y2);
 			clicked = true;
 		}
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
