 /**
 * @author Alexandre Vienne
 * @version 0.1
 * PlayPan est le composant de l'ihm représentant le plateau de jeu, il y dessine
 * les différents composant du jeu.
 */
 
 package brickapp.ihm;
 
 import javax.swing.JPanel;
import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.util.List;
 
 import brickapp.items.Ball;
 import brickapp.items.Bar;
 import brickapp.items.Brick;
 
 public class PlayPan extends JPanel implements MouseMotionListener {
 	private List<Brick> brickList;
     private Ball ball;
     private Bar bar;
 	/**
 	* Methode privée qui dessine les briques
 	*/
 	private void paintBrick(Graphics g){
 		for (Brick b : this.brickList){
 			g.fillRect(b.getPosition().getx(),b.getPosition().gety(),Brick.WIDTH,Brick.HEIGHT);
 		}
         g.fillOval(ball.getPosition().getx(), ball.getPosition().gety(), ball.getRayon(), ball.getRayon());
         g.fillRoundRect(bar.getPosition().getx(),bar.getPosition().gety(),Brick.WIDTH,Brick.HEIGHT/2,10, 10);
 	}
 
 
 	/**
 	* Instancie le playground
 	* @param bl liste des briques du niveau
 	*/
 	public PlayPan(List<Brick> bl,Ball b, Bar bar){
 		brickList = bl;
         ball=b;
         this.bar=bar;
         addMouseMotionListener(this);
 	}
 
 	/**
 	* Dessine le playground (appelé à chaque événement sur l'ihm)
 	*/
 	public void paintComponent(Graphics g){
 		paintField(g);
 	}
 
 	
 	/**
 	* Dessine les briques à l'écran
 	*/
 	public void paintField(Graphics g){
 		paintBrick(g);
 	}
 
     public void mouseDragged(MouseEvent mouseEvent) {
 
     }
 
     public void mouseMoved(MouseEvent mouseEvent) {
         System.out.println(mouseEvent.getX());
         this.bar.setBarPosition(mouseEvent.getX());
         this.repaint();
 
 
     }
 }
