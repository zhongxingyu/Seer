 /*
  * This file is part of the Turtle project
  *
  * (c) 2011 Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  */
 
 package turtle.gui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JFrame;
 
 import turtle.Kernel;
 import turtle.entity.Game;
 
 /**
  * Fentre principale (ou se droule le jeu de foot)
  *
  * @author Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  * @since 1.0
  */
 public class GameWindow extends AbstractWindow
 {
     protected FieldPannel mFieldPannel;
 
     public GameWindow(Kernel kernel, Game game)
     {
         super(kernel, game);
 
         initialize();
 
         // Window informations
         setTitle("Game");
 
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLocationRelativeTo(null);
         setResizable(false);
     }
 
     /**
      * Cration de la fentre et de ses composants
      */
     protected void initialize()
     {
         setLayout(new BorderLayout());
 
         mFieldPannel = new FieldPannel(mGame.getField());
 
        add(mFieldPannel, BorderLayout.CENTER);
        pack();
     }
 
     @Override
     public void updateView(Object arg)
     {
     }
 }
