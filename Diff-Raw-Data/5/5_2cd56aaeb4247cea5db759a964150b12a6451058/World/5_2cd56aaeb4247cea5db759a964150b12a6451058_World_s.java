 package karel;
 
 import java.io.File;
 import java.lang.Exception;
 import java.io.IOException;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.*;
 import java.awt.Color;
 import javax.sound.sampled.*;
 
 /**
  * Karel the Robot
  * Current version has 1 predefined map
  * @author Heather,Noel,Sam,Amber,Josh,MacsR4Luzrs
  */
 
 public class World extends JPanel
 {
     private final int OFFSET = 0;
     private final int SPACE = 31;
     private ArrayList walls = new ArrayList();//walls in world
     private ArrayList gems = new ArrayList(); //gems in world
     private ArrayList areas = new ArrayList(); //floors
     private boolean isRunning = true; //game ending bool
     //Wall home = new Wall(0,0); // home space
     protected Player karel; //object for karel 
     private int w = 19;
     private int h = 14;
     private home Home;
     private Gem tempGem;
     private int Speed = 5; // Execution speed for script
     private String stepThrough = ""; // String for keeping track of execution
                                     // steps in doScript
     
     //Map
     private String level =
               "###################\n"
             + "#                 #\n"
             + "#                 #\n"
             + "#                 #\n"
             + "#                 #\n"
             + "#                 #\n"
             + "#        $        #\n"
             + "#       $#$       #\n"
             + "#      $###$      #\n"
             + "#     $#####$     #\n"
             + "#    $#######$    #\n"
             + "#   $#########$   #\n"
             + "# ^ ###########  @#\n"
             + "###################\n";
     
     //Constructor - Set up world
     public World()
     {
         initWorld();
         setFocusable(true);
     }
     
     public int getBoardWidth() 
     {
         return this.w;
     }
 
     public int getBoardHeight() 
     {
         return this.h;
     }
     
     public int getPlayerGem()
     {
         return karel.getGemCount();
     }
 
     public int getStepCount()
     {
         return karel.getSteps();
     }
     
     public void setLevelString(String newLevel)
     {
         level = newLevel;
     }
     
     //Reads the map and adds all objects and their coordinates to arraylists
     public final void initWorld()
     {
         //create wall and gem objects
         Wall wall;
         Gem gem;
         Area a;
         
         //variables used to keep track of coordinates during loop
         int x = 0;
         int y = 0;
         
         for (int i = 0; i < level.length(); i++)
         {
             //Grab the item in string at i
             char item = level.charAt(i); 
             if (item != '\n')
             {    
                 a = new Area(x, y);
                  areas.add(a);
             }
             //Adjust X,Y value based on what character is at i
             //and create an item in the array list if needed
             if (item == '\n') 
             {
                 y += SPACE;
                 if (this.w < x) 
                 {
                     this.w = x;
                 }
               x = OFFSET;
             } 
             else if (item == '#') 
             {
                 wall = new Wall(x, y);
                 walls.add(wall);
                 x += SPACE;
             }
             else if (item == '$') 
             {
                 gem = new Gem(x,y);
                 gems.add(gem);
                 x += SPACE;
             } 
             else if (item == '.') 
             {
                 a = new Area(x, y);
                 areas.add(a);
                 x += SPACE;
             } else if (item == '^') 
             {
                 karel = new Player(x,y);
                 x += SPACE;
             } 
             else if (item == ' ') 
             {
                 x += SPACE;
             }
             else if (item == '@')
             {
                 Home = new home(x,y);
                 x += SPACE;
             }
 
             //home_square = y;
         }
         
    }
     
         public void buildWorld(Graphics g) {
 
         g.setColor(new Color(250, 240, 170));
         g.fillRect(0, 0, this.getWidth(), this.getHeight());
 
         ArrayList world = new ArrayList();
         world.addAll(walls);
         world.addAll(areas);
         world.addAll(gems);
         world.add(Home);
         world.add(karel);
 
         
         for (int i = 0; i < areas.size(); i++)
         {
             Entity item = (Entity) areas.get(i);
             g.drawImage(item.getImage(), item.GetX(), item.GetY(), this);
         }
 
         for (int i = 0; i < world.size(); i++) {
 
             Entity item = (Entity) world.get(i);
 
             if ((item instanceof Player)|| (item instanceof Gem) || (item instanceof home)) 
             {
                 g.drawImage(item.getImage(), item.GetX(), item.GetY(), this);
             } 
             else if(item instanceof Wall) 
             {
                 g.drawImage(item.getImage(), item.GetX(), item.GetY(), this);
             }
             
     /*        if (completed) {
                 g.setColor(new Color(0, 0, 0));
                 g.drawString("Completed", 25, 20);
             }
 */
         }
     }
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         buildWorld(g);
     }
     
     public boolean choiceMade(String choice)
     {    
         boolean error = false;
         //Get karels current direction
         char direction = karel.GetDirection();
         switch (choice)
         {
             case "go":
                 switch(direction)
                 {
                     case '^':
                         error = handleMove(0,-SPACE);
                         break;
                     case 'v':
                         error = handleMove(0, SPACE);
                         break;
                     case '>':
                         error = handleMove(SPACE,0);
                         break;
                     case '<':
                         error = handleMove(-SPACE,0);
                         break;
                 }
                 break;
             case "left":
                 switch(direction)
                 {
                     case '^':
                         karel.SetDirection('<');
                         karel.ChangeImage("left");
                         break;
                     case 'v':
                         karel.SetDirection('>');
                         karel.ChangeImage("right");
                         break;
                     case '>':
                         karel.SetDirection('^');
                         karel.ChangeImage("up");
                         break;
                     case '<':
                         karel.SetDirection('v');
                         karel.ChangeImage("down");
                         break;
                 }
                 break;
             case "right":
                 switch(direction)
                 {
                     case '^':
                         karel.SetDirection('>');
                         karel.ChangeImage("right");
                         break;
                     case 'v':
                         karel.SetDirection('<');
                         karel.ChangeImage("left");
                         break;
                     case '>':
                         karel.SetDirection('v');
                         karel.ChangeImage("down");
                         break;
                     case '<':
                         karel.SetDirection('^');
                         karel.ChangeImage("up");
                         break;
                 }
                 break;
             case "get":
                 //if the gem is on the same space as karel
                 if (karel.isGemCollision(karel.GetX(), karel.GetY(), gems) != -1)
                 {
                     //get the array location of this gem
                     int gemLocation = karel.isGemCollision(karel.GetX(), karel.GetY(), gems);
                     tempGem = (Gem) gems.get(gemLocation); 
                    
                     //put in karels bag
                     karel.addGem(tempGem);
                     
                     //remove from world
                     gems.remove(gemLocation);
                 }
                 break;
             case "put":
                 //drop gem from gem bag
                 if(karel.getGemCount() > 0)
                 {
                     tempGem = karel.removeGem();
                     tempGem.SetX(karel.GetX());
                     tempGem.SetY(karel.GetY());
                     gems.add(tempGem);
                 }
                 break;
             case "manual":
                 break;
         }
         karel.addStep();
         this.repaint();
         return error;
     }
     
     public void worldDeleter()
     {//delete the gems, walls, areas arrayLists
         gems.clear();
         walls.clear();
         areas.clear();
     }
      
     public boolean handleMove(int x, int y)
     {
         //Get where karel wants to move
         int newX = x + karel.GetX();
         int newY = y + karel.GetY();
         
         if (karel.isWallCollision(newX, newY, walls))
         {
             //collided with wall - do not move karel
             return true; // returning an error
         }
         else
         {
             //move karel
             karel.move(x, y);
         }
         repaint();
         if (karel.isHomeCollision(karel.GetX(),karel.GetY(),Home))
         {
             //if karel is home and all gems are taken, move and end game
             if(gems.isEmpty())
             {
                 isRunning = false;
                 playVictoryMusic();
                 infoBox("You have won!", "Congratulations!");
                 worldDeleter();
                 initWorld();
             }
         }
         return false; // no error
     }              
             
         public int doScript(int lineCount, int scope, List<String> userInput)
         { // Runs a user defined list of commands. Used recursively.
           // lineCount is how far into the file we are
           // scope is the level of nested commands
           // userInput is the string array containing the file
             
             int maxLineCount = userInput.size(); // Size of the file  
            
             while (lineCount < maxLineCount) 
             { 
                 String currentLine = userInput.get(lineCount); // Gets the line we're
                                                                  // dealing with
                 String tempstr = new String(); // Used for swapping strings
                 String conditional = new String(); // Holds the condition
                                                    // to be checked.
                 int repeatNum = 0; //The number of times to repeat. Initialized
                                     //to a valid value for error checking
                 int nextLine = 0; //Keeps the next line when dealing with scope
                 int commentIndex = currentLine.indexOf('#'); // Finding comments
                 final int throwError = maxLineCount + 1; // Error return value
                 
                 // If the comment symbol is in the string
                 if (commentIndex != -1)
                 {
                     currentLine = currentLine.substring(0, commentIndex);
                 }
                 
                if (currentLine.isEmpty())
                 {
                     lineCount++;
                     continue;
                 }
                 
                 // Checking for valid scope
                 if (scope > 0) 
                 {
                     int i;
                     for (i = 0; i < scope; i++)
                     {
                         if (!(currentLine.startsWith("\t")))
                         {
                             return lineCount; // Returning due to out of scope
                         }
                         
                         else
                         {
                             currentLine = currentLine.substring(1); // Removing the tab
                         }
                     }                    
                 }
                 // if the current line is out of scope
                 if (currentLine.startsWith("\t"))
                 {
                     ++lineCount;
                     continue;
                 }
                 currentLine = currentLine.trim();
                 
                 /* Parsing the current line for recognizable Syntax */
                 if (currentLine.matches("^repeat [0-9]{1,}$"))
                 {
                     tempstr = currentLine.substring(7); // Grabbing the number
                     repeatNum = Integer.valueOf(tempstr);
                     tempstr = currentLine.substring(0, 6); // Grabbing the repeat
                     currentLine = tempstr;
                 }
                 
                 if(currentLine.matches("^if (not )?(gem|home|wall)$"))
                 {
                     conditional = currentLine.substring(3); // Grabbing condition
                     tempstr = currentLine.substring(0, 2); // Grabbing if
                     currentLine = tempstr;
                 }
                 
                 if (currentLine.matches("^while (not )?(gem|home|wall)$"))
                 {
                     conditional = currentLine.substring(6); // Grabbing condition
                     tempstr = currentLine.substring(0, 5); // Grabbing while
                     currentLine = tempstr;
                 }
                 /* End Parsing */                
                 switch (currentLine)
                 { // Controls the logic for each valid command
                   // If input is something unexpected, it halts execution and
                   // prints an appropriate error
                   // Any time an error is encountered, maxLineCount + 1 is
                   // returned, signaling to end execution
                   // Note: Since lineCount is post-incremented, all uses of
                   // nextLine are reduced by 1 to account for the post increment
                     case "left" :                       
                     case "right":                        
                     case "go"   : 
                     case "put"  :
                     case "get"  :
                            try 
                            {
                                Thread.currentThread().sleep((3000/Speed) - 250);
                            }
                            catch(Exception e){}; 
                            stepThrough = stepThrough + "Line: " 
                                        + (lineCount + 1)
                                        + "      Executing command "
                                        + currentLine + "\n";
                            boolean error = choiceMade(currentLine);
                            if (error)
                            {
                                infoBox("Karel has crashed into a wall!", "ERROR");
                                stepThrough = stepThrough + "ERROR\n";
                                return throwError;                              
                            }
                            break;
                     case "repeat":  
                             // Checking if the repeat integer is out of range 
                             if ((repeatNum < 1) || (repeatNum > 999))
                             { 
                                 infoBox("Repeat value not "
                                         + "in valid range (1-999) "
                                         + "on line " + (lineCount + 1), "ERROR");
                                 stepThrough = stepThrough + "ERROR\n";
                                 return throwError;
                             }
                         
                             for (int i = 0; i < repeatNum; i++)
                             {
                                 stepThrough = stepThrough + "Line: " 
                                             + (lineCount + 1)
                                             + "      Repeat number "
                                             + (i + 1) + "\n";
                                 nextLine = doScript((lineCount + 1), 
                                                     (scope + 1), userInput);
                                 
                                 // If an error was returned
                                 if (nextLine > maxLineCount)
                                 { 
                                     return throwError;
                                 }
                                 
                             }
                             lineCount = nextLine - 1;
                             break; 
                             // End "Repeat" case
                         
                     case "if"   :
                             // Checking if the conditional is blank
                             if(conditional.isEmpty())
                             { 
                                 infoBox("Expected condition"
                                         + " after If on line " 
                                         +  (lineCount + 1), "ERROR");
                                 stepThrough = stepThrough + "ERROR\n";
                                 return throwError;
                             }                           
                             
                             if (handleCondition(conditional))
                             { // Successful If case
                                 stepThrough = stepThrough + "Line: "
                                             + (lineCount + 1) + "      If " 
                                             + conditional + " is true\n";
                                 nextLine = doScript((lineCount + 1), 
                                                     (scope + 1), userInput);
                             }
                             
                             else
                             { // Successful Else case
                                 stepThrough = stepThrough + "Line: " 
                                             + (lineCount + 1)
                                             + "      If " + conditional 
                                             + " is false\n";
                                 // Finding the accompanying Else statement
                                 tempstr = "else";
                                 
                                 // Forming tempstr based on our scope
                                 for (int i = 0; i < scope; i++)
                                 {
                                     tempstr = "\t" + tempstr;
                                 }
                                 int else_line = lineCount + 1;//Line the Else is on
                                 
                                 // While the next line isn't our Else
                                 while (! (userInput.get(else_line).matches(tempstr)))
                                 {
                                     else_line++;
                                     
                                     // If we can't find an accompanying Else
                                     if (else_line >= maxLineCount)
                                     {
                                         break;
                                     }
                                 }
                                 if (else_line >= maxLineCount)
                                 {
                                     ++lineCount;
                                     continue;
                                 }
                                 // End check for accompanying Else
                                
                                 nextLine = doScript((else_line + 1), 
                                                     (scope + 1), userInput);
                              }
                             
                             lineCount = nextLine - 1;
                             break;
                     
                     case "else" : // Only falls in this after a successful 'If'   
                                   // This code is used to skip the unnecessary
                                   // Else and all statements within it
                             tempstr = "\t";
                             
                             // As long as the line exceeds our scope
                             do
                             {
                                 ++lineCount;
                                 
                                 // If we've reached the end of the file
                                 if (lineCount >= maxLineCount)
                                 { 
                                     return lineCount;
                                 }
                              } while (userInput.get(lineCount).startsWith(tempstr, scope));
                              lineCount -= 1;
                             break;
                             // End "If-Else" case
                         
                     case "while" :
                             // Checking if the conditional is blank
                             if(conditional.isEmpty())
                             { 
                                 infoBox("Expected condition"
                                         + " on line " 
                                         +  (lineCount + 1), "ERROR");
                                 stepThrough = stepThrough + "ERROR\n";
                                 return throwError;
                             }
                             int while_line = lineCount;
                             while (handleCondition(conditional))
                             {
                                 stepThrough = stepThrough + "Line: " 
                                             + (while_line + 1)
                                             + "      While " + conditional
                                             + " is true in While\n" ;
                                 nextLine = doScript((while_line + 1), 
                                                      (scope + 1), userInput);                                                               
                                 // If an error was returned in this loop
                                 if (nextLine > maxLineCount)
                                 {
                                     return throwError;
                                 }
                                 lineCount = nextLine - 1;
                             }
                             stepThrough = stepThrough + "Line: " 
                                         + (while_line + 1)
                                         + "      While " + conditional 
                                         + " is false\n";
                             break;
                              // End "While" case
                         
                     default: 
                             infoBox("Unrecognized syntax\n" 
                                     + currentLine 
                                     + "\nOn line " + (lineCount + 1), "ERROR");
                             stepThrough = stepThrough + "ERROR\n";
                             return throwError;
                 }
                 ++lineCount;
             }
             if (scope == 0)
             {
                 stepThrough = stepThrough + "Script Complete!";
             }
             return lineCount;
         }
         
         // Function to check if a conditional is true or false
         public boolean handleCondition(String conditional)
         {
             char direction = karel.GetDirection();
             int x = 0;
             int y = 0;
             
             // Getting the correct x and y values to use
             switch(direction)
             {
                     case '^':
                         x = 0;
                         y = -SPACE;
                         break;
                     case 'v':
                         x = 0;
                         y = SPACE;
                         break;
                     case '>':
                         x = SPACE;
                         y = 0;
                         break;
                     case '<':
                         x = -SPACE;
                         y = 0;
                         break;
             }
             
             int newX = x + karel.GetX(); // Getting x of next space
             int newY = y + karel.GetY(); // Getting y of next space
             x = karel.GetX(); // Current space x
             y = karel.GetY(); // Current space Y
             
             switch (conditional)
             {
                 case "not gem" :
                         if ( (karel.isGemCollision(x, y, gems)) == -1)
                         { return true; }
                         
                         else
                         { return false; }
                         
                 case "gem"  :
                         if ( (karel.isGemCollision(x, y, gems)) != -1)
                         { return true; }
                        
                         else
                         { return false; }
                         
                 case "not wall":
                         if (!karel.isWallCollision(newX, newY, walls))
                         { return true; }
                         
                         else
                         { return false; }
                 case "wall" :
                         if (karel.isWallCollision(newX, newY, walls))
                         { return true; }
                         
                         else
                         { return false; }
                 case "not home":
                         if (!karel.isHomeCollision(x, y, Home))
                         { return true; }
                         
                         else
                         { return false; }
                 case "home" :
                         if (karel.isHomeCollision(x, y, Home))
                         { return true; }
                         
                         else
                         { return false; }
             }
            
             return false; // Should never get here
         }
         public static void infoBox(String infoMessage, String location)
         {
             JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + location, JOptionPane.INFORMATION_MESSAGE);
         }
         public void setSpeed(int newSpeed)
         {
             Speed = newSpeed;
         }
         
         public void setThemes(String newTheme)
         {
             for(int loop = 0; loop<gems.size(); loop++)
             {
                 Gem temp = (Gem) gems.get(loop);
                 temp.setNewImage(newTheme+"Gem.png");
             }
             
             karel.updateGemsInBag(newTheme);
             
             for(int loop = 0; loop<karel.getGemCount(); loop++)
             {
                 Gem temp = (Gem) gems.get(loop);
                 temp.setNewImage(newTheme+"Gem.png");
             }            
             
             for(int loop = 0; loop<walls.size(); loop++)
             {
                 Wall temp = (Wall) walls.get(loop);
                 temp.setNewImage(newTheme+"Wall.png");
             }
             
             for(int loop = 0; loop<areas.size(); loop++)
             {
                 Area temp = (Area) areas.get(loop);
                 temp.setNewImage(newTheme+"Area.png");
             }
             
             Home.setNewImage(newTheme+"Home.png");
             karel.setNewTheme(newTheme);
             karel.repaintNewTheme();
             //the player setnewtheme only happens when the player changes direction..
             //need to reset the image outside of the change direction...
         }
         // Function to reset the stepthrough string
         public void resetStepThrough()
         {
             stepThrough = "";
         }
         
         public String getStepThrough()
         {
             return stepThrough;
         }
         
         public void playVictoryMusic()
         {
             try 
             {
                     AudioInputStream audio = AudioSystem.getAudioInputStream(new File("src/karel/sounds/victory.wav"));
                     Clip clip = AudioSystem.getClip();
                     clip.open(audio);
                     clip.start();
             }
             
             catch(Exception e) {};
         }
 }
