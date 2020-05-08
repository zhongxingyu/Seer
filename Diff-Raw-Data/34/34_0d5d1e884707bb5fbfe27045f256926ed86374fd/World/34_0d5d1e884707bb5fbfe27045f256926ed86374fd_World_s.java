 package doom;
 class World { 
 	private GameField level;
 	private Player gamer;
 	private String canvas = "##########################################################################################" +
 				"#------$---------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------t(r)-----------------------------------------------------------------------------#" +
 				"#-------###+(r)###--------------------------------------------$-----------------------------#" +
 				"#-------#-----#--------------------------------------------------------------------------#" +
 				"#-------#######--------------------------------------------------------------------------#" +
 				"#------------------------------$---------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#------------------------$---------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#-########################################################################################" +
 				"#----------------------------------------------------------------------------------------#" +
 				"########################################################################################-#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"#----------------------------------------------------------------------------------------#" +
 				"##########################################################################################";
 	
 	//constructor	
 	public World() {
 		level = new GameField(canvas, 90, 30);
 		this.gamer = new Player(1, 1);
 	}
 
 	//if the user is pressing a valid key -> rturn true
 	public boolean onKeyPressed(String key) {
 		if ((key.equals("w")) || (key.equals("a")) || (key.equals("s")) || (key.equals("d"))) {
 			return true;
 		} else {return false;}
 	}
 
 	//checking the destination GameTile
 	public boolean moveCheck (int x, int y) {
 		boolean check = true;
 		String tileString = level.getTileString(x, y);
 		
 		if (tileString.equals("#")) {check = false;}
                 if (tileString.equals("+")) {check = false;}
 
 		return check;
 	}
 
 	//main-move-function
 	public void move (String key) {
 		int posX = this.gamer.getX();
 		int posY = this.gamer.getY();
 		if (onKeyPressed(key)) {
 			switch (key.charAt(0)) {
 				case 'w' : if (moveCheck(posX-1, posY)) {this.gamer.moveUp();}
 					break;
 				case 'a' : if (moveCheck(posX, posY-1)) {this.gamer.moveLeft();}
 					break;
 				case 's' : if (moveCheck(posX+1, posY)) {this.gamer.moveDown();}
 					break;
 				case 'd' : if (moveCheck(posX, posY+1)) {this.gamer.moveRight();}
 					break;
 			}
                         gamer.setPlayerWater();
                 }               
 	}
 
 	//main-print-function
 	public void draw (Gui mainWindow){
 		Events event = new Events();
                 //get current position of the player
                 int x = this.gamer.getX();
 		int y = this.gamer.getY();
                 //get current waterstatus
                 int water = gamer.getPlayerWater();
                 //get current gameTile
                 String eventIndex = this.level.getTileIndex(x, y);             
              
                 
 		//get the event-code
 		String eventCombinedIndex = event.eventManager(mainWindow, eventIndex);
                 
                 //delete the event-GameTile
                 if (event.delEvent(eventIndex)) {
                     level.tileConv(x, y);
                 }
                
                //set player-atributes
                 gamer.setAtributes(eventCombinedIndex);
                 
                 //edit the waterBar
                 mainWindow.setWaterBar(water);
                 //get current balance
                 int money = gamer.getPlayerMoney();
                 //edit the MoneyCounter
                 mainWindow.setMoneyCounter(money);
                 
                             
                 
                 //convert the GameField into a string
 		String sub1 = level.toString();
                 //integrate the player into the GameField
 		String sub2 = gamer.draw(sub1, level.getWidth());
                               
                 
                 
                 //convert the GameField-string into a '2D'-string
                 String outString = "";
 		for(int i=0; i<(level.getHeight()); i++) {
 			outString = outString + ( sub2.substring(((level.getWidth())*i), ((level.getWidth())*(i+1))) ) + "<br/>";
 		}
                 
                 //draw-function
                 mainWindow.setMainLabel(outString);               
 	}
 }
 
