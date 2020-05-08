 package main.pieces;
 
 import main.globals.Globals;
 import main.position.Position;
 
import java.util.ArrayList;

 /**
  * @author Alex Telon
  */
 public class Piece implements IPiece {
 
     @Override
     public Position getPosition() {
         return null;
     }
 
     @Override
     public boolean tryMove(Position newPosition) {
         return false;
     }
 
     @Override
     public void remove() {
 
     }
 
     @Override
     public int getValue() {
         return -100;
     }
 
     @Override
     public Globals.Side getSide() {
         return null;
     }

    @Override
    public ArrayList<Position> returnPossiblePositions() {
        return null;
    }
 }
