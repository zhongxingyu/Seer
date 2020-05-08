 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.manager;
 
 import ca.couchware.wezzle2d.*;
 import ca.couchware.wezzle2d.event.CollisionEvent;
 import ca.couchware.wezzle2d.event.ICollisionListener;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.tile.Tile;
 import ca.couchware.wezzle2d.tile.TileType;
 import ca.couchware.wezzle2d.util.IXMLizable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import org.jdom.Element;
 
 /**
  * A class to manage achievements.
  * The manager holds an arraylist of achievements not yet achieved
  * and a list of achievements which have already been achieved
  * 
  * every iteration of the game, the manager passes the gamestate into all of
  * the achievements which check to see if they have been achieved. The 
  * achievments have an evaluate(gamestate) function which returns a boolean.
  * if the achievement has been achieved, the achievement is moved from the
  * arraylist into the achieved array list and displayed to the screen.
  *
  * @author Kevin
  */
 public class AchievementManager implements ICollisionListener
 {
 
     /** A flag that is set to true if an achievement has been completed. */
     private boolean achievementCompleted = false;
     
     /** The unachieved achievements. */
     private List<Achievement> incompleteList;
     
     /** The achieved achievements. */
     private List<Achievement> completeList;
     
     /** The master list */
     private List<Achievement> masterList;
     
     /** The current date */
     private Date date;
         
     /**
      * The constructor.
      */
     private AchievementManager()
     {
         this.incompleteList = new ArrayList<Achievement>();
         this.completeList   = new ArrayList<Achievement>();
         this.masterList     = new ArrayList<Achievement>();
         this.importAchievements();
         
         if(date == null)
         {
             date = Calendar.getInstance().getTime();
             
         }
     }
     
     // Public API.
     public static AchievementManager newInstance()
     {
         return new AchievementManager();
     }
     
     /**
      * Add an achievement to the manager.
      * @param achieve The achievement.
      * 
      * Note: this method adds to the master list as well. Be careful 
      * that you are not adding to the list twice.
      */
     public void add(Achievement achievement)
     {
         this.incompleteList.add(achievement);
         this.masterList.add(achievement);
     }
     
     /**
      * Evaluate each achievement. 
      * If the achievement is completed transfer from the incomplete to 
      * the completed lists.
      * 
      * @param game The state of the game.
      * @return True if an achievement was completed, false otherwise.
      */
     public boolean evaluate(Game game)
     {
         boolean achieved = false;
         
         for (Iterator<Achievement> it = incompleteList.iterator(); it.hasNext(); )
         {
             Achievement a = it.next();
             
             if (a.evaluate(game) == true)
             {
                 // set the date.
                 setCompleted(a);
                 this.completeList.add(a);
                 it.remove();
                 achieved = true;
                 this.achievementCompleted = true;
             }
         }
         
         return achieved;
     }
 
     /**
      * Returns true if one or more achievement has recently been completed.
      * 
      * @return
      */
     public boolean isAchievementCompleted()
     {
         return achievementCompleted;
     }
 
     /**
      * Clears the achievement completed flag.
      * 
      * @param achievementCompleted
      */
     public void clearAchievementCompleted()
     {
         this.achievementCompleted = false;
     }        
     
     /**
      * Report the completed descriptions to the console.
      */
     public void reportCompleted()
     {
         // Clear the achievement completed flag.
         this.achievementCompleted = false;
         
         for (int i = 0; i < completeList.size(); i++)
             LogManager.recordMessage(completeList.get(i).toString(),
                     "AcheivementManager#reportCompleted");
     }     
     
     /**
      * Listens for collision events.
      * 
      * @param e
      */
     public void collisionOccured(CollisionEvent e)
     {
         List<Tile> collisionList =  e.getCollisionList();
 
         StringBuffer buffer = new StringBuffer();
 
         for (Tile t : collisionList)
         {
            buffer.append(t.getType().toString() + " -> ");
         }
 
         buffer.append("END");
 
         LogManager.recordMessage(buffer.toString());
         
         for (Iterator<Achievement> it = incompleteList.iterator(); it.hasNext(); )
         {
             Achievement a = it.next();
 
             if (a.evaluateCollision(collisionList) == true)
             {
                this.completeList.add(a);
                 it.remove();
                 this.achievementCompleted = true;
             }
         } // end for   
     }
     
     private void importAchievements()
     {
         SettingsManager settingsMan = SettingsManager.get();
         
         // Get the list from the settings manager. 
         List list = (List) settingsMan.getObject(Key.USER_ACHIEVEMENT);
         
         for (Object object : list)     
         {
             Achievement achieve = (Achievement) object;
             
             if (achieve.getDateCompleted() != null)
                 this.completeList.add(achieve);
             else
                 this.incompleteList.add(achieve);
             
             // Add to master list here so we dont have to rebuild it every 
             // time we click go to achievements potentially.
             this.masterList.add(achieve);
         }
     }
     
     /**
      * Get the master list.
      * @return The master list.
      * 
      * Note: returns an unmodifiable list.
      */
     public List<Achievement> getMasterList()
     {
         return Collections.unmodifiableList(this.masterList);
     }
     
     private void setCompleted(Achievement a)
     {
         assert a.getDateCompleted() == null;
         a.setDate(Calendar.getInstance());
     }
 
  
 }
