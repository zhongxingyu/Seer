 package org.geekygoblin.nedetlesmaki.game.utils;
 
 import org.geekygoblin.nedetlesmaki.game.components.Position;
 
 /**
  *
  * @author natir
 */
 public class PosOperation {
     
    static Position sum(Position p1, Position p2) {
	return Position(p1.getX()+p2.getX(), p1.getY()+p2.getY());
     }
 
    static Position deduction(Position p1, Position p2) {
	return Position(p1.getX()-p2.getX(), p1.getY()-p2.getY());	
     }
 }
