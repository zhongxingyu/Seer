 package edu.victone.scrabblah.logic.player;
 
 import edu.victone.scrabblah.logic.common.Tile;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 4:15 PM
  */
 
 public class TileRack implements Iterable<Tile> {
     static final int MAXSIZE = 7;
    private static Random random = new Random(System.nanoTime());;
 
     private List<Tile> rack;
 
     public TileRack() {
         rack = new ArrayList<Tile>(MAXSIZE);
     }
 
     public void shuffleRack() {
         Collections.shuffle(rack, random);
     }
 
     public boolean addTile(Tile t) {
         if (size() < MAXSIZE) {
             rack.add(t);
             return true;
         }
         return false;
     }
 
     public void addTiles(ArrayList<Tile> tiles) {
         if (size() + tiles.size() > 7) {
             throw new IllegalStateException();
         } else {
             for (Tile t : tiles) {
                 addTile(t);
             }
         }
     }
 
     public boolean removeTile(Tile t) {
         for (Tile tile : rack) {
             if (tile.getCharacter().equals(t.getCharacter())) {
                 rack.remove(tile);
                 return true;
             }
         }
         return false;
     }
 
     public void dumpRack() { // don't invoke this
         while (rack.size() > 0) {
             System.out.print(rack.remove(0) + " - ");
         }
     }
 
     public int size() {
         return rack.size();
     }
 
     public boolean contains(Tile t) {
         for (Tile tile : rack) {
             if (tile.getCharacter().equals(t.getCharacter())) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public Iterator<Tile> iterator() {
         return rack.iterator();
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("Tile Rack: ");
 
         for (Tile t : rack) {
             sb.append(t + " ");
         }
         return sb.toString();
     }
 }
