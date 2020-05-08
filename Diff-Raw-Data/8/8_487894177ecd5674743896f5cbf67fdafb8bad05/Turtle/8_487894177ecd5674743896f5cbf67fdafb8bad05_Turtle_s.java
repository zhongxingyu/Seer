 /*
  * This file is part of the Turtle project
  *
  * (c) 2011 Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  */
 
 package turtle.entity;
 
 import java.awt.Point;
 
 import turtle.behavior.turtle.TurtleBehaviorInterface;
 
 public class Turtle
 {
     /**
      * La position du joueur sur le terrain
      */
     protected Point mPosition;
 
     /**
      * Le comportement du joueur
      */
     protected TurtleBehaviorInterface mBehavior;
 
    public Turtle()
     {
     }
 
     public Point getPosition()
     {
         return mPosition;
     }
 
     public void setPosition(Point position)
     {
         mPosition = position;
     }
 
     public void setPosition(int x, int y)
     {
         mPosition = new Point(x, y);
     }
 
     public TurtleBehaviorInterface getBehavior()
     {
         return mBehavior;
     }
 
     public void setBehavior(TurtleBehaviorInterface behavior)
     {
         mBehavior = behavior;
     }
 }
