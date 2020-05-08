 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.ui.button.*;
 import ca.couchware.wezzle2d.ui.group.Group;
 import ca.couchware.wezzle2d.util.*;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * The menu manager is used to manage the menues in Wezzle.  In particular, it
  * helps to link Buttons to Groups.
  * 
  * @author cdmckay
  */
 public class GroupManager 
 {
     /**
      * The game over class.
      */
     final public static int CLASS_GAME_OVER = 1;   
         
     /**
      * The pause class.
      */
     final public static int CLASS_PAUSE = 2;
     
     /**
      * The options class.
      */
     final public static int CLASS_OPTIONS = 3;        
         
     /**
      * The bottom layer.
      */
     final public static int LAYER_BOTTOM = 1;
     
     /**
      * The middle layer.
      */
     final public static int LAYER_MIDDLE = 2;
     
     /**
      * The top layer.
      */
     final public static int LAYER_TOP = 3;
     
     /**
      * The list of groups currently being shown.
      */
     protected LinkedList<Entry> entryList;
           
     /**
      * A reference to the layer manager.
      */
     protected LayerManager layerMan;
     
     /**
      * A reference to the piece manager.
      */
     protected PieceManager pieceMan;
     
     /**
      * The constructor.
      */
     public GroupManager(LayerManager layerMan, PieceManager pieceMan)
     {
         entryList = new LinkedList<Entry>();        
         
         this.layerMan = layerMan;
         this.pieceMan = pieceMan;
     }     
     
     public void showGroup(BooleanButton button, Group showGroup, 
             int classNum, int layerNum)
     {
         // Remove all groups that aren't part of the passed class.
         // Hide all existing members of the passed clas.
         for (Iterator<Entry> it = entryList.iterator(); it.hasNext(); )            
         {
             // The entry we are looking at.
             Entry e = it.next();                   
             
             if (e.getLayerNum() == layerNum)
             {
                 if (e.getClassNum() == classNum)
                     e.getGroup().setVisible(false);
                 else
                 {
                     deactivateEntry(e);
                     it.remove();
                 }
             }
                 
         }
        
         // Add the group on top.
         entryList.addFirst(new Entry(showGroup, button, classNum, layerNum)); 
         
         // Make the group visible.
         showGroup.setVisible(true);
         showGroup.setActivated(true);
         
         // Make the button activated.
         if (button != null)
             button.setActivated(true);
         
         layerMan.hide(Game.LAYER_TILE);
         layerMan.hide(Game.LAYER_EFFECT);
         
         Util.handleMessage("Groups open: " + entryList.size(), 
                 Thread.currentThread());
     }    
     
     /**
      * This method makes an entry's group invisible, deactivates it, 
      * resets it's buttons, and deactivates the button that called it.
      * 
      * @param entry
      */
     protected void deactivateEntry(Entry entry)
     {
         entry.getGroup().setVisible(false);
         entry.getGroup().setActivated(false);
         entry.getGroup().clearChanged();
 
         if (entry.getButton() != null)
             entry.getButton().setActivated(false);
     }
     
     public void hideGroup(int classNum, int layerNum)
     {
         // Go through the entry list, removing all entries with the
         // passed class name.
         for (Iterator<Entry> it = entryList.iterator(); it.hasNext(); )            
         {
             // The entry we are looking at.
             Entry e = it.next();
             
             if (e.getLayerNum() == layerNum && e.getClassNum() == classNum)
             {
                 // Deactivate the entry.
                 deactivateEntry(e);     
                 
                 // Remove it.
                 it.remove();
             }
         }
                     
         // Make the top of the list visible.
         if (entryList.isEmpty() == true)
         {
             pieceMan.clearMouseButtons();
             layerMan.show(Game.LAYER_TILE);
             layerMan.show(Game.LAYER_EFFECT);
         }
         else
             entryList.getFirst().getGroup().setVisible(true);
         
         Util.handleMessage("Groups open: " + entryList.size(), 
                 Thread.currentThread());
     }   
     
     public void hideGroup(Group group)
     {
         // Remove the group.
         for (Iterator<Entry> it = entryList.iterator(); it.hasNext(); )            
         {
             // The entry we are looking at.
             Entry e = it.next();
             
             if (e.getGroup() == group)
             {
                 deactivateEntry(e);
                 it.remove();
                 break;
             }
         }
                 
         // Make the top of the list visible.
         if (entryList.isEmpty() == true)
         {
             pieceMan.clearMouseButtons();
             layerMan.show(Game.LAYER_TILE);
             layerMan.show(Game.LAYER_EFFECT);
         }
         else
             entryList.getFirst().getGroup().setVisible(true);
         
         Util.handleMessage("Groups open: " + entryList.size(), 
                 Thread.currentThread());
     }
     
     public boolean isActivated()
     {
         // If the group list is not empty, then at least one group must be 
         // showing.
         return !entryList.isEmpty();
     }  
 
     /**
      * This is an inner class represented an entry in 
      * the the group linked list.
      */
     protected class Entry
     {               
         /**
          * The group associated with this entry.
          */
         final protected Group group;
         
         /**
          * The button that was used to open this group.
          */
         final protected BooleanButton button;
         
         /**
          * The class of the group.  This is used to hide or show many groups
          * at once.
          */        
         final protected int classNum;
         
         /**
          * The layer that the group is on.  This is mainly used to keep
          * the game over screen open under a bunch of menues.
          */
         final protected int layerNum;
         
         /**
          * The constructor.
          */
         public Entry(Group group, BooleanButton button, 
                 int classNum, int layerNum)
         {
             // Set the references.
             this.group = group;
             this.button = button;
             this.classNum = classNum;
             this.layerNum = layerNum;
         }
 
         public Group getGroup()
         {
             return group;
         }    
         
         public BooleanButton getButton()
         {
             return button;
         }
 
         public int getClassNum()
         {
             return classNum;
         }
 
         public int getLayerNum()
         {
             return layerNum;
         }                                   
     }
     
 }
