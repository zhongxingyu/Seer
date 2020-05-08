 /**********************************************************************/
 /* Copyright 2013 KRV                                                 */
 /*                                                                    */
 /* Licensed under the Apache License, Version 2.0 (the "License");    */
 /* you may not use this file except in compliance with the License.   */
 /* You may obtain a copy of the License at                            */
 /*                                                                    */
 /*  http://www.apache.org/licenses/LICENSE-2.0                        */
 /*                                                                    */
 /* Unless required by applicable law or agreed to in writing,         */
 /* software distributed under the License is distributed on an        */
 /* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,       */
 /* either express or implied.                                         */
 /* See the License for the specific language governing permissions    */
 /* and limitations under the License.                                 */
 /**********************************************************************/
 package players;
 
 // Default libraries
 import java.util.ArrayList;
 
 // Libraries
 import gui.GUI;
 import arena.Robot;
 import arena.World;
 import parameters.*;
 
 /**
  * Create a general player, used to
  * identify the Team of all the scenarios
  * avaiable in the map.
  * @see arena.Robot
  * @see scenario.Scenario
  *
  * @author Renato Cordeiro Ferreira
  */
 public class Player
 {
     /*
     ////////////////////////////////////////////////////////////////////
     -------------------------------------------------------------------
                                 CLASS' DATA
     -------------------------------------------------------------------
     \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     */
     
     // Counter with the amount of
     // players created (to ID's)
     private static int total = -1;
     
     /**
      * Default player: Mother Nature
      */
     public static Player Nature = new Player(null);
     
     /*
     ////////////////////////////////////////////////////////////////////
     -------------------------------------------------------------------
                                OBJECT'S DATA
     -------------------------------------------------------------------
     \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     */
     
     // Internal variables
     private int    ID;
     private Base   base;
     private String color;
     private int[]  basePos;
     
     // Player's armies
     public ArrayList<Robot> armies = new ArrayList<Robot>();
     
     // Player's view of the game
     public GUI GUI = null;
     
     /*
     ////////////////////////////////////////////////////////////////////
     -------------------------------------------------------------------
                                 CONSTRUCTORS
     -------------------------------------------------------------------
     \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     */
     
     /**
      * Default constructor.
      * Creates the ID, color and the base
      * for a given player.
      * @param base Player's base
      */
     public Player(Base base)
     {
         this.ID    = ++total;
         this.base  = base;
         this.color = setColor(this.ID);
         
         // Sets the base as his
         if(base != null) base.own(this);
     }
     
     /*
     ////////////////////////////////////////////////////////////////////
     -------------------------------------------------------------------
                                    INFO
     -------------------------------------------------------------------
     \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     */
     
     /** 
      * Getter for the player's color
      * @return String representing player's color
      */
     public String color() { return this.color; }
     
     /**
      * Player identification 
      * @return Player name (if any) or ID
      */
     public String toString()
     {
         return ("Player " + this.ID);
     }
     
     /*
     ////////////////////////////////////////////////////////////////////
     -------------------------------------------------------------------
                                   ARMIES
     -------------------------------------------------------------------
     \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     */
     
     /**
      * Add a new robot on player's army.
      * @param  name       Name of the new robot
      * @param  pathToProg Robot assembly program
      */
    public void insertArmy(String name, String pathToProg)
     {
         Debugger.say("[", this.toString(), "]", " Adding robot ", name);
         Robot r = World.insertArmy(this, name, pathToProg);
         this.armies.add(r);
 
         Debugger.say("[PLAYER] Armies:");
         for(Robot rob: this.armies) Debugger.say(rob.toString());
     }
     
     /**
      * Takes out the robot from the 
      * player's army and the arena.
      * @param  deadRobot Robot to be 
      *         removed from the list
      */
     public void removeArmy(Robot deadRobot)
     {
         this.armies.remove (deadRobot);
     }
     
     /*
     ////////////////////////////////////////////////////////////////////
     -------------------------------------------------------------------
                              GETTERS AND SETTERS
     -------------------------------------------------------------------
     \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     */
     
     /**
      * Sets player's GUI.
      * @param GUI  Player's own visualization
      *             of what is happening on the
      *             game
      */
     public void setGUI(GUI GUI) { this.GUI = GUI; }
     
     /** 
      * Getter for player's robots
      * @return Array with player's robots
      */
     public ArrayList<Robot> getRobots() { return this.armies; }
     
     /** 
      * Getter for the player's ID.
      * @return Integer with player ID
      */
     public int getID() { return this.ID; }
     
     /** 
      * Returns the player's base.
      * @return Player's base
      */
     public Base getBase() { return this.base; }
     
     /*
     ////////////////////////////////////////////////////////////////////
     -------------------------------------------------------------------
                                AUXILIAR METHODS
     -------------------------------------------------------------------
     \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     */
     
     /** 
      * Auxiliar method to define a new 
      * player's color.
      * @param  plID Player's ID.
      * @return String representing 
      *         player's color
      */
     final private String setColor(int plID)
     {
         switch(plID)
         {
             case 0:  return "";
             case 1:  return "BLACK";
             case 2:  return "RED";
             case 3:  return "GREEN";
             case 4:  return "YELLOW";
             default: return "";
         }
     }
 }
