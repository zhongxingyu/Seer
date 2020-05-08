 package de.schelklingen2008.canasta.client.view;
 
 import java.awt.Color;
 import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
 
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.samskivert.swing.ShapeIcon;
 
 import de.schelklingen2008.canasta.client.controller.Controller;
 import de.schelklingen2008.canasta.client.controller.GameChangeListener;
 import de.schelklingen2008.canasta.client.model.GameContext;
 import de.schelklingen2008.canasta.model.GameModel;
 import de.schelklingen2008.canasta.model.Player;
 
 /**
  * Displays a list of players and turn change information in a turn-based game.
  */
 public class TurnPanel extends JPanel implements GameChangeListener
 {
 
     private static final Polygon   TRIANGLE  = new Polygon(new int[] { 0, 12, 0 }, new int[] { 0, 6, 12 }, 3);
    private static final Ellipse2D CIRCLE    = new Ellipse2D.Float(0, 0, 12, 12);
     private static final ShapeIcon ICON_TURN = new ShapeIcon(TRIANGLE, Color.YELLOW, null);
     private Controller             controller;
 
     public TurnPanel(Controller controller)
     {
         this.controller = controller;
         controller.addChangeListener(this);
     }
 
     public void gameChanged()
     {
         removeAll();
 
         if (getGameModel() == null) return;
 
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
         for (Player player : getGameModel().getPlayers())
         {
             String name = player.getName();
             JLabel label = new JLabel(name + "       " + player.getTotalScore());
             if (controller.getGameContext().getGameModel().getTurnHolder() == 0) label.setIcon(ICON_TURN);
             add(label);
 
         }
 
         revalidate();
         repaint();
     }
 
     private GameContext getGameContext()
     {
         return controller.getGameContext();
     }
 
     private GameModel getGameModel()
     {
         return getGameContext().getGameModel();
     }
 }
