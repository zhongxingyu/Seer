 package karel;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.*;
 import java.awt.Color;
 
 
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
     private int w = 18;
     private int h = 14;
     private home Home;
     JTextArea lines;
     JTextArea jta;
     int Speed = 5;
     
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
             
              a = new Area(x, y);
                 areas.add(a);
 
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
     
     public void choiceMade(String choice)
     {    
         //Get karels current direction
         char direction = karel.GetDirection();
         switch (choice)
         {
             case "go":
                 switch(direction)
                 {
                     case '^':
                         handleMove(0,-SPACE);
                         break;
                     case 'v':
                         handleMove(0, SPACE);
                         break;
                     case '>':
                         handleMove(SPACE,0);
                         break;
                     case '<':
                         handleMove(-SPACE,0);
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
                     //pick up the gem
                     gems.remove(karel.isGemCollision(karel.GetX(), karel.GetY(), gems));
                     karel.addGem();
                 }
                 break;
             case "put":
                 //pick up the gem
                 if(karel.getGemCount() > 0)
                 {
                     karel.removeGem();
                     Gem gem = new Gem(karel.GetX(),karel.GetY());
                     gems.add(gem);
                 }
                 break;
             case "manual":
                 break;
         }
         karel.addStep();
         this.repaint();
     }
     
     public void worldDeleter()
     {//delete the gems, walls, areas arrayLists
         gems.clear();
         walls.clear();
         areas.clear();
     }
      
     public void handleMove(int x, int y)
     {
         //Get where karel wants to move
         int newX = x + karel.GetX();
         int newY = y + karel.GetY();
 
         if (karel.isHomeCollision(karel.GetX(),karel.GetY(),Home))
         {
             //if karel is home and all gems are taken, move and end game
             if(gems.isEmpty())
             {
                 karel.move(x,y);
                 isRunning = false;
                 infoBox("You have won!", "Congratulations!");
             }
         }
         if (karel.isWallCollision(newX, newY, walls))
         {
             //collided with wall - do not move karel
         }
         else
         {
             //move karel
             karel.move(x, y);
         }
     }              
             
         public int doScript(int line_count, int scope, List<String> user_input)
         { // Runs a user defined list of commands. Used recursively.
           // line_count is how far into the file we are
           // scope is the level of nested commands
           // user_input is the string array containing the file
             
             int max_line_count = user_input.size(); // Size of the file
             
             while (line_count < max_line_count) 
             { 
                 String current_line = user_input.get(line_count); // Gets the line we're
                                                                  // dealing with
                 String tempstr = new String(); // Used for swapping strings
                 String conditional = new String(); // Holds the condition
                                                    // to be checked.
                 int repeat_num = 0; //The number of times to repeat. Initialized
                                     //to a valid value for error checking
                 int next_line = 0; //Keeps the next line when dealing with scope
                 final int throw_error = max_line_count + 1; // Error return value
                 
                 if (current_line.isEmpty())
                 {
                     line_count++;
                     continue;
                 }
                 
                 // Checking for valid scope
                 if (scope > 0) 
                 {
                     int i;
                     for (i = 0; i < scope; i++)
                     {
                         if (!(current_line.startsWith("\t")))
                         {
                             return line_count; // Returning due to out of scope
                         }
                         
                         else
                         {
                             current_line = current_line.substring(1); // Removing the tab
                         }
                     }
                     if (current_line.startsWith("\t"))
                     {
                         ++line_count;
                         continue;
                     }
                 }
                 current_line = current_line.trim();
                 
                 /* Parsing the current line for recognizable Syntax */
                 if (current_line.matches("^repeat [0-9]{1,}$"))
                 {
                     tempstr = current_line.substring(7); // Grabbing the number
                     repeat_num = Integer.valueOf(tempstr);
                     tempstr = current_line.substring(0, 6); // Grabbing the repeat
                     current_line = tempstr;
                 }
                 
                 if(current_line.matches("^if (not )?(gem|home|wall)$"))
                 {
                     conditional = current_line.substring(3); // Grabbing condition
                     tempstr = current_line.substring(0, 2); // Grabbing if
                     current_line = tempstr;
                 }
                 
                 if (current_line.matches("^while (not )?(gem|home|wall)$"))
                 {
                     conditional = current_line.substring(6); // Grabbing condition
                     tempstr = current_line.substring(0, 5); // Grabbing while
                     current_line = tempstr;
                 }
                 /* End Parsing */                
                 switch (current_line)
                 { // Controls the logic for each valid command
                   // If input is something unexpected, it halts execution and
                   // prints an appropriate error
                   // Any time an error is encountered, max_line_count + 1 is
                   // returned, signaling to end execution
                   // Note: Since line_count is post-incremented, all uses of
                   // next_line are reduced by 1 to account for the post increment
                     case "left" :                       
                     case "right":                        
                     case "go"   : 
                     case "put"  :
                     case "get"  :
                            try 
                            {
                                Thread.currentThread().sleep(2050 - (Speed * 200));
                            }
                            catch(Exception e){}; 
                            choiceMade(current_line);
                            break;
                     case "repeat":  
                             // Checking if the repeat integer is out of range 
                             if ((repeat_num < 1) || (repeat_num > 999))
                             { 
                                 infoBox("Repeat value not "
                                         + "in valid range (1-999) "
                                         + "on line " + (line_count + 1), "ERROR");
                                 return throw_error;
                             }
                         
                             for (int i = 0; i < repeat_num; i++)
                             {
                                 next_line = doScript((line_count + 1), 
                                                     (scope + 1), user_input);
                                 
                                 // If an error was returned
                                 if (next_line > max_line_count)
                                 { 
                                     return throw_error;
                                 }
                                 
                             }
                             line_count = next_line - 1;
                             break; 
                             // End "Repeat" case
                         
                     case "if"   :
                             // Checking if the conditional is blank
                             if(conditional.isEmpty())
                             { 
                                 infoBox("Expected condition"
                                         + " after If on line " 
                                         +  (line_count + 1), "ERROR");;
                                 return throw_error;
                             }                           
                             
                             if (handleCondition(conditional))
                             { // Successful If case
                                 next_line = doScript((line_count + 1), 
                                                     (scope + 1), user_input);
                             }
                             
                             else
                             { // Successful Else case
                                 // Finding the accompanying Else statement
                                 tempstr = "else";
                                 
                                 // Forming tempstr based on our scope
                                 for (int i = 0; i < scope; i++)
                                 {
                                     tempstr = "\t" + tempstr;
                                 }
                                 int else_line = line_count + 1;//Line the Else is on
                                 
                                 // While the next line isn't our Else
                                 while (! (user_input.get(else_line).matches(tempstr)))
                                 {
                                     else_line++;
                                     
                                     // If we can't find an accompanying Else
                                     if (else_line >= max_line_count)
                                     {
                                         break;
                                     }
                                 }
                                 if (else_line >= max_line_count)
                                 {
                                     ++line_count;
                                     continue;
                                 }
                                 // End check for accompanying Else
                                
                                 next_line = doScript((else_line + 1), 
                                                     (scope + 1), user_input);
                              }
                             
                             line_count = next_line - 1;
                             break;
                     
                     case "else" : // Only falls in this after a successful 'If'   
                                   // This code is used to skip the unnecessary
                                   // Else and all statements within it
                             tempstr = "\t";
                             
                             // As long as the line exceeds our scope
                             do
                             {
                                 ++line_count;
                                 
                                 // If we've reached the end of the file
                                 if (line_count >= max_line_count)
                                 { 
                                     return line_count;
                                 }
                              } while (user_input.get(line_count).startsWith(tempstr, scope));
                              line_count -= 1;
                             break;
                             // End "If-Else" case
                         
                     case "while" :
                             // Checking if the conditional is blank
                             if(conditional.isEmpty())
                             { 
                                 infoBox("Expected condition"
                                         + " on line " 
                                         +  (line_count + 1), "ERROR");
                                 return throw_error;
                             }
                             int while_line = line_count;
                             while (handleCondition(conditional))
                             {
                                 next_line = doScript((while_line + 1), 
                                                      (scope + 1), user_input);                                                               
                                 // If an error was returned in this loop
                                 if (next_line > max_line_count)
                                 {
                                     return throw_error;
                                 }
                                 line_count = next_line - 1;
                             }
                             break;
                              // End "While" case
                         
                     default: 
                             infoBox("Unrecognized syntax\n" 
                                     + current_line 
                                     + "\nOn line " + (line_count + 1), "ERROR");
                             return throw_error;
                 }
                 ++line_count;
             }
             return line_count;
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
         }
 
 }
