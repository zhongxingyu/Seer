 package de.se.tinf11b3.breakdown.client.collision;
 
 import java.util.ArrayList;
 
 import gwt.g2d.client.math.Circle;
 import gwt.g2d.client.math.Rectangle;
 import gwt.g2d.client.math.Vector2;
 import de.se.tinf11b3.breakdown.client.gameobjects.Ball;
 import de.se.tinf11b3.breakdown.client.gameobjects.Block;
 import de.se.tinf11b3.breakdown.client.gameobjects.Paddle;
 import de.se.tinf11b3.breakdown.client.steuerung.DirectionVector;
 import de.se.tinf11b3.breakdown.client.ui.Widget_GUI_Interface;
 import de.se.tinf11b3.breakdown.client.vector.VectorOperations;
 
 public class Kollisionserkennung {
 
 	/**
 	 * Überprüft, ob eine Kollision mit einem Block stattgefunden hat
 	 * @param y_direction 
 	 * @param x_direction 
 	 * 
 	 * @param app
 	 */
 	public static Blockkollision checkBlockCollision(ArrayList<Block> bloecke, Ball ball, int x_direction, int y_direction, Widget_GUI_Interface app) {
 		boolean hit=false;
 		
 		for(int i = 0; i < bloecke.size(); i++) {
 			Block tmp = bloecke.get(i);
 			Vector2 position = new Vector2(tmp.getX(), tmp.getY());
 			double width = tmp.getSize().getX();
 			double height = tmp.getSize().getY();
 
 			Rectangle rec = new Rectangle(position, width, height);
 			Circle circ = new Circle(ball.getX(), ball.getY(), ball.getRadius());
 
 			CollisionResult result = RectangleCircleKollision(rec, circ);
 			hit = result.isCollided(); 
 			
 			if(hit) {
 				
 				if(tmp.getHitcount() == 0){
 						bloecke.remove(tmp);
 				}
 				else{
 						tmp.setHitcount(tmp.getHitcount()-1);
 				}
 				
 				
 				switch(result.getSeite()) {
 					case OBEN:
 						return new Blockkollision(new DirectionVector(x_direction, -Math.abs(y_direction), Kollisionsseite.OBEN), bloecke, hit);
 					case UNTEN:
 						return new Blockkollision(new DirectionVector(x_direction, Math.abs(y_direction), Kollisionsseite.UNTEN), bloecke, hit);
 					case LINKS:
 						return new Blockkollision(new DirectionVector(-Math.abs(x_direction), y_direction, Kollisionsseite.LINKS), bloecke, hit);
 					case RECHTS:
 						return new Blockkollision(new DirectionVector(Math.abs(x_direction), y_direction, Kollisionsseite.RECHTS), bloecke, hit);
 					default:
 						break;
 				}
 				
 				
 				return new Blockkollision(new DirectionVector(x_direction, y_direction*(-1), Kollisionsseite.OBEN), bloecke, hit);
 				
 			}
 		}
 		
 		return new Blockkollision(new DirectionVector(x_direction, y_direction, Kollisionsseite.NOKOLLISION), bloecke, false);		
 	}
 	
 	
 	private void makeDirectionNegative(int direction) {
 		
 		
 	}
 	
 	
 	
 	
 
 	// Gibt zurück, ob eine Kollision stattfand und wenn ja, wo, und wie lang
 	// der (minimale, quadratische) Abstand zum Rechteck ist.
 	public static CollisionResult RectangleCircleKollision(Rectangle rect, Circle circle) {
 		double x = rect.getX();
 		double y = rect.getY();
 		double h = rect.getHeight();
 		double w = rect.getWidth();
 
 		Vector2 p1 = new Vector2(x, y);
 		Vector2 p2 = new Vector2(x + w, y);
 		Vector2 p3 = new Vector2(x + w, y + h);
 		Vector2 p4 = new Vector2(x, y + h);
 
 		double minDistSq = 80000;
 		Vector2 basePoint = new Vector2(0, 0);
 		Kollisionsseite seite = Kollisionsseite.NOKOLLISION;
 
 		
 		// Seiten durchgehen, Schleife kann (bzw muss, je nachdem wie Rect
 		// aussieht) entrollt werden
 		Vector2 base = PointLineDist(circle.getCenter(), p1, p2);
 		if(VectorOperations.sqr_length(VectorOperations.vec_minus_vec(circle.getCenter(), base)) < minDistSq) {
 			// Kürzerer Abstand, neu zuweisen.
 			minDistSq = VectorOperations.sqr_length(VectorOperations.vec_minus_vec(circle.getCenter(), base));
 			basePoint = base;
 			seite = Kollisionsseite.OBEN;
 		}
 
 		base = PointLineDist(circle.getCenter(), p2, p3);
 		if(VectorOperations.sqr_length(VectorOperations.vec_minus_vec(circle.getCenter(), base)) < minDistSq) {
 			// Kürzerer Abstand, neu zuweisen.
 			minDistSq = VectorOperations.sqr_length(VectorOperations.vec_minus_vec(circle.getCenter(), base));
 			basePoint = base;
 			seite = Kollisionsseite.RECHTS;
 		}
 
 		base = PointLineDist(circle.getCenter(), p3, p4);
 		if(VectorOperations.sqr_length(VectorOperations.vec_minus_vec(circle.getCenter(), base)) < minDistSq) {
 			// Kürzerer Abstand, neu zuweisen.
 			minDistSq = VectorOperations.sqr_length(VectorOperations.vec_minus_vec(circle.getCenter(), base));
 			basePoint = base;
 			seite = Kollisionsseite.UNTEN;
 		}
 		base = PointLineDist(circle.getCenter(), p4, p1);
 		if(VectorOperations.sqr_length(VectorOperations.vec_minus_vec(circle.getCenter(), base)) < minDistSq) {
 			// Kürzerer Abstand, neu zuweisen.
 			minDistSq = VectorOperations.sqr_length(VectorOperations.vec_minus_vec(circle.getCenter(), base));
 			basePoint = base;
 			seite = Kollisionsseite.LINKS;
 		}
 
 		CollisionResult result = new CollisionResult(minDistSq < circle.getRadius()
 				* circle.getRadius(), basePoint, minDistSq, seite);
 
 		return result;
 	}
 
 	public static Vector2 PointLineDist(Vector2 point, Vector2 linestart, Vector2 lineend) {
 		Vector2 a = new Vector2(lineend.getX() - linestart.getX(), lineend.getY()
 				- linestart.getY());
 		Vector2 b = new Vector2(point.getX() - linestart.getX(), point.getY()
 				- linestart.getY());
 		double t = VectorOperations.dot(a, b)
 				/ (VectorOperations.sqr_length(a));
 
 		if(t < 0)
 			t = 0;
 		if(t > 1)
 			t = 1;
 		return VectorOperations.vec_plus_vec(linestart, VectorOperations.vec_mal_scalar(a, t));
 	}
 
 	public static DirectionVector checkPaddleCollision(Paddle paddle, Ball ball, ArrayList<Block> bloecke, int x_direction, int y_direction, Widget_GUI_Interface app) {
 
 		Vector2 position = new Vector2(paddle.getX() - paddle.getSize() / 2, paddle.getY());
 		double width = paddle.getSize();
 		double height = 10;
 
 		Rectangle rec = new Rectangle(position, width, height);
 		Circle circ = new Circle(ball.getX(), ball.getY(), ball.getRadius());
 		boolean hit = RectangleCircleKollision(rec, circ).isCollided();
 		Vector2 collisionVec = RectangleCircleKollision(rec, circ).getCollisionPoint();
 
 		// Reached Paddle
 		if(hit) {
 
 			 
 			
 			double deltaX = (paddle.getSize() / 2) / 6;
 
 			double bereich_anfang = paddle.getX();
 			double bereich_ende;
 
 			char bereich = 'r';
 			
 			//Ball rechts oder links?
 			if(ball.getX() >= paddle.getX()) {
 				bereich = 'r';
 			}
 			else {
 				bereich = 'l';
 			}
 			
 			
 			for(int i = 1; i < 7; i++) {
 
 				// app.pushToServer("Bereich["+i+"].Anfang="+bereich_anfang);
 				// app.pushToServer("Bereich["+i+"].Ende="+bereich_ende);
 
 				// TODO RECHTS ODER LINKS?
 				if(bereich == 'r') {
 					bereich_ende = bereich_anfang + deltaX;
 				}
 				else {
 					bereich_ende = bereich_anfang - deltaX;
 				}
 
 				// Kollision in Bereich
 				if(Kollisionserkennung.punktInBereich(ball.getX(), bereich_anfang, bereich_ende)) {
 //					app.pushToServer("Bereich " + i+" "+bereich);
 					switch(i) {
 						case 1:
 							
 							x_direction = 1;
 							y_direction = -7;
 							
 							break;
 						case 2:
 							
 							x_direction = 3;
 							y_direction = -6;
 							
 							break;
 						case 3:
 							
 							x_direction = 4;
 							y_direction = -6;
 							
 							break;
 						case 4:
 							
 							x_direction = 5;
 							y_direction = -5;
 							
 							break;
 						case 5:
 							
 							x_direction = 6;
 							y_direction = -3;
 							
 							break;
 						case 6:
 							
 							x_direction = 6;
 							y_direction = -5;
 							
 							break;
 						default:
 							break;
 					}
 
 					break;
 				}
 
 				
 				// Anfang verschieben
 				if(bereich == 'r') {
 					bereich_anfang = bereich_anfang += deltaX;
 				}
 				else {
 					bereich_anfang = bereich_anfang -= deltaX;
 				}
 			}
 
 			if(bereich == 'l'){
 				x_direction *= -1;
 			}
 			
 //			app.pushToServer("HIT PADDLE");
 			// app.pushToServer("Paddle.X= " + paddle.getX());
 			// app.pushToServer("Paddle.Y= " + paddle.getY());
 			// app.pushToServer("Ball.X= " + ball.getX());
 			// app.pushToServer("Ball.Y= " + ball.getY());
 
 		}
 
 		// TODO IMPLEMENT
 		return new DirectionVector(x_direction, y_direction,Kollisionsseite.NOKOLLISION);
 
 	}
 
 	/**
 	 * Liefert True, falls der übergebene Punkt innerhalb des Wertebereichs
 	 * liegt sonst False
 	 * 
 	 * @param x
 	 * @param bereich_anfang
 	 * @return
 	 */
 	public static boolean punktInBereich(int x, double bereich_anfang, double bereich_ende) {
 
 		// Falls negativ
 		if(bereich_anfang > bereich_ende) {
 			double tmp = bereich_anfang;
 			bereich_anfang = bereich_ende;
 			bereich_ende = tmp;
 		}
 
 		if(x >= bereich_anfang && x <= bereich_ende) {
 			return true;
 		}
 		else {
 			return false;
 		}
 
 	}
 
 	public static DirectionVector checkFrameCollision(Ball ball, int x_direction, int y_direction, Widget_GUI_Interface app) {
 		// Check Y-Rand Collision
		if((ball.getY() <= 15)) {
 			y_direction *= -1;
 		}
 		
		if(ball.getY() >= 480){
 			app.erniedrigeLeben();
 		}
 		
 
 		// Check X-Rand Collision
 		if((ball.getX() <= 0) || (ball.getX() >= 495)) {
 			x_direction *= -1;
 		}
 
 		return new DirectionVector(x_direction, y_direction,Kollisionsseite.NOKOLLISION);
 	}
 
 }
