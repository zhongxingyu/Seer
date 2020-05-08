 /*
 * Board class
 * This class is considered the intermedate of the whole program.
 * It connects the actors with the other actors on the board.
 * this makes for low coupling.
 */
 package karel;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 
  /**
  *
  * @author Gonzo
  */
 public class Board extends JPanel {
 
     private final int LEFT_COLLISION = 1;
     private final int RIGHT_COLLISION = 2;
     private final int TOP_COLLISION = 3;
     private final int BOTTOM_COLLISION = 4;
     private final int SPACE = 30;
     private final int OFFSET = 0;
     private final int TURN_LEFT=-1;
     private final int TURN_RIGHT=1;
     
     private ArrayList walls = new ArrayList();
     private ArrayList gems = new ArrayList();
     private Player karel;
     private Home home;
     private boolean completed;
     private boolean crashed;
     private String level;
     private String fileName;
     private boolean manualMode;
     private int boardHeight = 0;
     private int boardWidth = 0;
     private int theme;
     
     
     /**
      * Constructor
      * @param file File name of the level
      * @param th Theme of the actor
      */
     public Board(String file, int th) {
         completed = false;
         manualMode = true;
         crashed = false;
         theme = th;
         setFocusable(true);
         setFileName(file);
         loadLevel();
         initWorld();
         
     }
     
     public String getFileName(){
         return fileName;
     }
     
     public int getBoardWidth() {
         return boardWidth;
     }
 
     public int getBoardHeight() {
         return boardHeight;
     }
        
     /**
      * Sets the file name of the level to be loaded
      * @param fileName File name of the level  
      */
     public void setFileName(String fileName){
         this.fileName = fileName;
     }
     
     /**
      * Reset the board to it's initial state
      * @param ManualRestart True if user selects key to restart level
      */
     public void restartLevel(boolean ManualRestart){
         
         //empty ArrayList of created actors
         walls.clear();
         gems.clear();
         
         //recreate and initialize all actors that will be on the board
         initWorld();
         
         //reset completed variable
         if(completed){
             completed = false;
         }
         
         
         if(!ManualRestart){
             //karal crashed so need to display this is a bad thing somewho
         }
     }
     
     /**
      * Reads in level data from from file and stores in string called level
      */
     public void loadLevel(){
        String tempString;
        
         try{
             BufferedReader br = new BufferedReader(new FileReader(fileName));
             while ((tempString = br.readLine()) != null) {
                 level += (tempString + "\n");
                 //System.out.println(tempString);
 
             }
             
         }catch(IOException e){
             loadLevel();
         }
     }
         
     
     /**
      * Create and initialize all actor that will be a part of the board
      * Blank spaces aren't considered actors thus aren't stored
      * For easy printing to the console actors and blank space are saved to a 2d character array 
      */
     public void initWorld(){ 
         
         Wall wall;
         Gems gem;
         
         
         int x = OFFSET;
         int y = OFFSET;
 
         for (int i = 0; i < level.length(); i++) {
             char item = level.charAt(i);
             
             //if new line adjust coordinates 
             if (item == '\n') {
                 y+=SPACE;
                 if(boardWidth <x){
                     boardWidth = x;
                 }
                 x = OFFSET;
             } 
             
             //creates wall and adds to list of walls on the board
             else if (item == '#') {
                 wall = new Wall(x, y, theme);
                 walls.add(wall);
                 x+=SPACE;
             } 
             //creates gems and adds to list of gems
             else if (item == 'G') {
                 gem = new Gems(x, y, theme);
                 gems.add(gem);
                 x+=SPACE;
             }
             //create home
             else if (item == 'H') {
                 home = new Home(x, y, theme);
                 x+=SPACE;
             }
             //create the player
             else if (item == '@') {
                 karel = new Player(x, y, theme);
                 x+=SPACE;
             } 
             //add blanks to the world array for easy printing to the console
             else if (item == ' ') {
                 x+=SPACE;
             }   
             
             boardHeight = y;
         }
   
     }
     
     /**
      * Builds world(updates world array) according to actor's stored coordinates
      */
     public void buildWorld(Graphics g){
         ArrayList world = new ArrayList();
         world.addAll(walls);
         world.addAll(gems);
         world.add(home);
         world.add(karel);
         
         g.setColor(new Color(127, 127, 127));
         if (theme == 1 || theme == 4 || theme == 5 || theme == 6)
         {
             g.setColor(new Color(54,139,67));
         }
         g.fillRect(0, 0, this.getWidth(), this.getHeight());
         
         g.setColor(new Color(0,0,0));
         for(int i=0;i<(boardWidth/30);i++){
             g.drawLine(i*30, 0, i*30,boardHeight);
         }
 
         for(int i=0;i<(boardHeight/30);i++){
             g.drawLine(0,i*30,boardWidth,i*30);
         }
 
         for (int i = 0; i < world.size(); i++) {
             Actor item = (Actor) world.get(i);
             
             if((item instanceof Gems)){
                 Gems tempGem = (Gems) world.get(i);
                 
                 if((tempGem.Gempickup() == false) && (tempGem.getOnHome() == false)){
                     g.drawImage(item.getImage(), item.x() + 2, item.y() + 2, this);
                 }
             }
             else if((item instanceof Player)){
                
                 g.drawImage(item.getImage(), item.x() + 2, item.y() + 2, this);
             }
             
             else if((item instanceof Home)){
                
                 g.drawImage(item.getImage(), item.x() + 2, item.y() + 2, this);
             }
             
             else{
                 g.drawImage(item.getImage(), item.x(), item.y(), this);
                 
             }
            
         }//end for
 
             if (completed) {
                 ImageIcon iia = new ImageIcon("skins/others/complete.jpg");
                 Image mage = iia.getImage();
                 g.setColor(new Color(128, 128, 255));
                 g.fillRect(0, 0, this.getWidth(), this.getHeight());
                 g.drawImage(mage, 0, 0, this);   
             }
             
              if(crashed){
                 ImageIcon iia = new ImageIcon("skins/others/error.png");
                 Image mage = iia.getImage();
                 //System.out.println("WIN");
                 g.setColor(new Color(128, 128, 255));
                 g.fillRect(0, 0, this.getWidth(), this.getHeight());
                 //g.drawString("Completed", 25, 20);
                 g.drawImage(mage, 0, 0, this);
                 crashed = false;
             }
        
     }
     
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         buildWorld(g);
     }
     
     
     /**
      * Checks if the actor being passed to it will crash into a wall when moved
      * @param actor Actor being checked for collision 
      * @param type Numeric representation of the type of collision to test
      * @return True if a crash will happen
      */
     private boolean checkWallCollision(Actor actor, int type) {
 
         if (type == LEFT_COLLISION) {
 
             for (int i = 0; i < walls.size(); i++) {
                 Wall wall = (Wall) walls.get(i);
                 if (actor.isLeftCollision(wall)) {
                     return true;
                 }
             }
             return false;
 
         } else if (type == RIGHT_COLLISION) {
 
             for (int i = 0; i < walls.size(); i++) {
                 Wall wall = (Wall) walls.get(i);
                 if (actor.isRightCollision(wall)) {
                     return true;
                 }
             }
             return false;
 
         } else if (type == TOP_COLLISION) {
 
             for (int i = 0; i < walls.size(); i++) {
                 Wall wall = (Wall) walls.get(i);
                 if (actor.isTopCollision(wall)) {
                     return true;
                 }
             }
             return false;
 
         } else if (type == BOTTOM_COLLISION) {
 
             for (int i = 0; i < walls.size(); i++) {
                 Wall wall = (Wall) walls.get(i);
                 if (actor.isBottomCollision(wall)) {
                     return true;
                 }
             }
             return false;
         }
         return false;
     }
     
     /**
      * Interprets the key pressed when in manual mode 
      * @param key Character key entered on the keyboard
      */
      public void keyPressed(char key) {
             
          //dont do anything if the level is complete 
             if (completed) {
                 return;
             }
             
             //turn face of player 90 degrees to the left
             if (key == 'a') { 
                 
                 karel.SetDir(TURN_LEFT);
             } 
             //turn face of player 90 degrees to the right
             else if (key == 'd') {
                 
                 karel.SetDir(TURN_RIGHT);
 
             } 
             
             //checks if player can move to next spot and if allowed moved player to next spot
             else if (key == 'w') {
                 karelGO();
             } 
             
             //if player wants to set down a gem
             else if (key == 's') {  
                 karelPUT();
             } 
             
             //if the player wants to pick up a gem
             else if(key == 'e'){
                 karelGET();
             } 
             
             //restart the level
             else if (key == 'r') {
                 restartLevel(true);
             }
 
             repaint();
            
             
             
         }
         
      
           /**
      * Adjusts carried gem positions to the position of the player
      * @param x How many spaces on the x axis to move 
      * @param y How many spaces on the y axis to move
      */
      private void movePickedUpGems(int x, int y){
          for(int i=0; i<gems.size(); i++){
              Gems gem = (Gems) gems.get(i);
              if(gem.Gempickup() == true){
                  gem.move(x, y);
              }
          }
      }
      
      /**
       * Sets completed variable to true is all gems have been moved to home
       */
      public void isCompleted(){
         if(karel.ReturnGemCount() == gems.size() && 
           (karel.x() == home.x() && karel.y() == home.y())
           ){
              completed = true;
          }
          else{
              completed = false;
          }
      }
      
      /**
       * Returns mode the board is in
       * @return True if board is in manual mode
       */
      public boolean isManualMode(){
          return manualMode;
      }
      
      /**
       * Returns state the game is in
       * @return True if all gems have been picked up and place on home
       */
      public boolean checkCompleted(){
          return completed;
      }
      
      /**
       * Move player forward if move is accepted
       */
      public void karelGO(){
          int temp=0;
                 temp=karel.ReturnDirection();    //player's current direction
                 
                 switch(temp)
                 {
                     case 1://trying to move north
                         if (checkWallCollision(karel, TOP_COLLISION)) {
                             crashed=true;
                             restartLevel(false);
                             return;
                         }
                         else{  
                             karel.move(0,-SPACE);
                             movePickedUpGems(0, -SPACE);
                             isCompleted();
                             break;
                         }
                         
                     case 2://trying to move east
                         if(checkWallCollision(karel, RIGHT_COLLISION)) {
                             crashed=true;
                             restartLevel(false);
                             return;
                         }
                         else{
                             karel.move(SPACE,0);
                             movePickedUpGems(SPACE, 0);
                             isCompleted();
                             break;
                        }
                         
                     case 3://trying to move south
                         if (checkWallCollision(karel, BOTTOM_COLLISION)) {
                             crashed=true;
                             restartLevel(false);
                             return;
                         }
                         else{
                             karel.move(0,SPACE);
                             movePickedUpGems(0, SPACE);
                             isCompleted();
                             break;
                        }
                     case 4://trying to move west
                         if (checkWallCollision(karel, LEFT_COLLISION)) {
                             crashed=true;
                             restartLevel(false);
                             return;
                         }
                         else{
                             karel.move(-SPACE,0);
                             movePickedUpGems(-SPACE, 0);
                             isCompleted();
                             break;
                         }
                     default:
                         break;
                         
                 }//end switch
      }
      
      /**
       * Places gem if player is carrying one
       */
      public void karelPUT(){
          int x, y;
 
                 //players current position
                 x = karel.x();
                 y = karel.y();
                 
                 //find a gem that is being carried
                 //if no gem is being carried nothing happens
                 for(int i=0; i<gems.size(); i++){
                     Gems gem = (Gems) gems.get(i);
                     
                     //if there is a gem to put down updates all associated data necessary
                     if((gem.x()== x) && (gem.y()==y)){
                         if(gem.Gempickup() == true){
                             gem.Setpickup(false);
                             karel.DecGems();
                             
                             if((gem.x() == home.x()) && (gem.y() == home.y())){
                                 gem.setOnHome(true);
                                 home.CountInc();
                             }//end inner most if
                             break;
                         }//end middle if
                     }//end outer if  
                 }//end for
               
               //test to see if the level has been completed
               isCompleted();
      }
      
      /**
       * Grab gem if spot has a gem
       */
      public void karelGET(){
          int x, y;
 
                 x = karel.x();
                 y = karel.y();
                 
                 //find a gem that is on the current location of the player
                 //if no gem is on the location nothing happens
                 for(int i=0; i<gems.size(); i++){     
                     Gems gem = (Gems) gems.get(i);
                     
                     //if there is a gem to pick up updates all associated data necessary
                     if((gem.x()== x) && (gem.y()==y)){
                         if(gem.Gempickup() == false){
                             gem.Setpickup(true);
                             karel.IncrementGems();
                             
                             if((gem.x() == home.x()) && (gem.y() == home.y())){
                                 gem.setOnHome(false);
                                 home.CountDec();
                             }//end inner most if
                             break;
                         }//end middle if       
                     }//end outer if  
                 }//end for
      }
      
      /**
       * Checks if there is a wall directly in front of the player
       * @return True if the is a wall directly in front of the player
       */
      public boolean wallSensor(){
          
                 int temp=karel.ReturnDirection();    //player's current direction
                 
                 switch(temp)
                 {
                     case 1://trying to move north
                         if (checkWallCollision(karel, TOP_COLLISION)) {
                             return true;
                         }
                         else{  
                             return false;
                         }
                         
                     case 2://trying to move east
                         if(checkWallCollision(karel, RIGHT_COLLISION)) {
                             return true;
                         }
                         else{
                             return false;
                        }
                         
                     case 3://trying to move south
                         if (checkWallCollision(karel, BOTTOM_COLLISION)) {
                             return true;
                         }
                         else{
                             return false;
                        }
                     case 4://trying to move west
                         if (checkWallCollision(karel, LEFT_COLLISION)) {
                             return true;
                         }
                         else{
                             return false;
                         }
                     default:
                         return true;
                         
                 }//end switch
      }
      
      /**
       * Checks if the player is on a gem
       * @return True if the player is on a space with a gem
       */
      public boolean gemSensor(){
          int x, y;
          boolean state = false;
 
                 x = karel.x();
                 y = karel.y();
                 
                 //find a gem that is on the current location of the player
                 //if no gem is on the location nothing happens
                 for(int i=0; i<gems.size(); i++){     
                     Gems gem = (Gems) gems.get(i);
                     
                     //if there is a gem to pick up updates all associated data necessary
                     if((gem.x()== x) && (gem.y()==y)){
                         if(gem.Gempickup() == false){
                             state = true;
                             
                         }//end middle if       
                     }//end outer if  
                 }//end for
                 
                 return state;
      }
      
      public boolean emptySensor(){
          if(karel.ReturnGemCount() == 0){
              return true;
          }
          else{
              return false;
          }
      }
      
      public boolean northSensor(){
          if(karel.ReturnDirection() == 1){
              return true;
          }
          else{
              return false;
          }
      }
      
      public boolean homeSensor(){
          if( (karel.x() == home.x()) && (karel.y() == home.y())){
              return true;
          }
          else{
              return false;
          }
      }
      
      public boolean ReturnCrashState(){
          return crashed;
      }
      
      public void SetCrashedState(boolean state){
          crashed = state;
      }
      
          
      public String PlayerInfo(){
          String tempString = new String();
          int n = 0;
          
          //number of gems
             n=karel.ReturnGemCount();
             tempString = "Number of Gems on Karel: " + n + "\n";
             
 
             //direction the player is facing
             n=karel.ReturnDirection();
             tempString = tempString.concat("Direction facing: ");
 
             switch(n)
             {
                 case 1:
                     tempString = tempString.concat("North\n");
                     break;
 
                 case 2:
                     tempString = tempString.concat("East\n");
                     break;
 
                 case 3:
                     tempString = tempString.concat("South\n");
                     break;
 
                 case 4:
                     tempString = tempString.concat("West\n");
                     break;
             }//end switch
 
             //number of gems on home 
             n=home.CountGet();
             tempString = tempString.concat("Number of Gems on Home: " + n + "\n");
             
             return tempString;
      }
      
      public void setManualMode(boolean bool){
          manualMode = bool;
      }
      
      public void setTheme(int thm){
         theme = thm;
         
         for (int i = 0; i < walls.size(); i++) {
             Wall item = (Wall) walls.get(i);
             item.changeTheme(theme);
         }
         
         for (int i = 0; i < gems.size(); i++) {
             Gems item = (Gems) gems.get(i);
             item.changeTheme(theme);
         }
         
         home.changeTheme(theme);
         karel.changeTheme(theme);
              
      }
      
 
         
         
 }
