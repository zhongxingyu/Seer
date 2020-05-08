 package com.group5.diceroller;
 
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class DiceSet
     implements Iterable<Dice> {
 
     Set<Dice> dice;
     int id;
     String name;
     int modifier;
 
     public static final int NOT_SAVED = -1;
 
     /**
     * Constructs a DiceSet with the given id, name, and modifier.
      */
     public DiceSet(int id, String name, int modifier) {
         this.name = name;
         this.modifier = modifier;
         dice = new TreeSet<Dice>();
     }
 
     /**
     * Constructs a DiceSet with the given id and name. The Modifier is
      * defaulted to 0.
      */
     public DiceSet(int id, String name) {
         this(id, name, 0);
     }
 
     /**
      * Loads all the dice sets in the local database into a list.
      * TODO implement. Current implementation for testing only.
      * @return A list of dice sets from the database
      */
     public static List<DiceSet> LoadAllFromDB() {
         ArrayList<DiceSet> ret = new ArrayList<DiceSet>();
            // Not sure if this is the correct way to get the number of rows in the database but I think it is,
            // which I am using as the number of dice sets we have minus 1 for the header row.
         int numSets = 9;//diceSet.getRowCount (); 
         
         for (int i=0; i<numSets; i++)
             ret.add(new DiceSet(i, "Set " + i));
         DiceSet s = new DiceSet(numSets, "");
         ret.add(s);
         return ret;
     }
 
     /**
      * Returns a standard dice notation string describing the dice in this set.
      * For example: "1D6, 2D4, 3D8". If a set has no dice, this returns the
      * empty string.
      * TODO implement.
      * @return A description of this dice set.
      */
     public String description() {
         /* ArrayList<DiceSet> ret = new ArrayList<DiceSet>(); */
         /* Scanner DiceScanner = new scanner (DiceSet); */
         /* String desc = ""; */
         /*                          */
         /* // I am using this assuming the database has diceset name, number of that dice, number of sides of that dice, and repeating that till there are no more left. */
         /*  while (DiceSet.hasnextint()) */
         /* { */
         /*     desc = desc + DiceScanner.nextInt; */
         /*     desc = desc + "D" */
         /*     desc = desc + DiceScanner.nextInt; */
         /*     if DiceSet.hasnextint() */
         /* 	desc = desc + ", " */
         /* } */
         /*  */
         /* return desc;    						    // changes the hardcoded answer to desc which is a string that should have all of the dice info in it. */
         return "description";
                                                                 
     }
 
     /**
      * Returns the user-defined name for this Dice Set. If the user did not
      * define a name, this should return the empty string.
      * @return The user-defined name of this dice set.
      */
     public String name() {
         return name;
     }
 
     /**
      * Saves this set to the database. If the ID is DiceSet.NOT_SAVED, then it
      * creates a new set and updates this set's id to represent the newly-saved
      * id.
      * TODO implement:Padraic
      */
     public void save() {
     }
 
     /**
      * Adds a dice to this DiceSet. If the dice to add has the same face count
      * as another Dice in the set, its count is added into the count of the
      * Dice in the set.
      * TODO implement.
      * @param dice The dice to add.
      * @return True if the dice was added to the set, false if another dice
      *   with the same number of faces was already in the set.
      */
     public boolean add(Dice dice) {
         return false;
     }
 
     /**
      * Removes a dice from the set which has the given number of faces.
      * TODO implement.
      * @param faces The number of faces on the Dice to be removed.
      * @return True if a Dice was removed from the set, false otherwise.
      */
     public boolean remove(int faces) {
         return false;
     }
 
     /**
      * Checks if two dice sets contain the same dice. 
      * TODO implement.
      * @return True if for every Dice of a given face and count in one DiceSet,
      *   a dice with the same face and count is in the other.
      */
     public boolean equals(DiceSet other) {
         return false;
     }
 
     /**
      * Returns an itrator over the dice in this set.
      * TODO implement. current implementation for testing
      */
     public Iterator<Dice> iterator() {
         return dice.iterator();
     }
 
     /**
      * Returns the number of dice in this set.
      * TODO implement
      */
     public int getCount() {
         return 0;
     }
 
     /**
      * Returns the sum of the dice in this set.
      * TODO implement
      */
     public int sum() {
         return 0;
     }
 
     /**
      * Returns human-readable label for this set.
      * @return The name if it is available, the description if not.
      */
     public String label() {
         String label = name();
         if (label.equals(""))
             label = description();
         return label;
     }
 }
