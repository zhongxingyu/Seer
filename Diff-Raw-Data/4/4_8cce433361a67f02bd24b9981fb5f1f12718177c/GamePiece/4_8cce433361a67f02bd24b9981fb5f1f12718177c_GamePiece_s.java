 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package connectfour;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import javax.swing.ImageIcon;
 
 /**
  *
  * @author zesty
  */
 public class GamePiece {
    private Image red = new ImageIcon("/home/zesty/Repos/four-in-a-row/images/redpiece.gif").getImage().getScaledInstance(SIZE, SIZE, Image.SCALE_DEFAULT);
    private Image black = new ImageIcon("/home/zesty/Repos/four-in-a-row/images/blackpiece.gif").getImage().getScaledInstance(SIZE, SIZE, Image.SCALE_DEFAULT);
     private Point point;
     private byte player;
     private byte color;
     
     public static final int SIZE = 100;
     public static final byte RED = 0;
     public static final byte BLACK = 1;
     public static final byte PLAYER1 = 1;
     public static final byte PLAYER2 = 2;
     
     /***********************************************
      * Need to figure out a way to keep someone from 
      * inputing a value larger than 1. Maybe throw 
      * exception or use enum to prevent erroneous values
      ***********************************************/
     public GamePiece(byte _player, byte _color){
         player = _player;
         color = _color;
         point = new Point(0,0);
     }
     
     public int getX(){
         return point.x;
     }
     
     public int getY(){
         return point.y;
     }
     
     public void setX(int _x){
         point.x = _x;
     }
     
     public void setY(int _y){
         point.y = _y;
     }
     
     public Image getImage(){
         if(color == RED)
             return red;
         else
             return black;
     }
     
     public byte getPlayer(){
         return player;
     }
     
     public int getColumn(){
         if(point.x == 0)
             return 0;
         return (point.x/100);
     }
     
     public void draw(Graphics g){
         if(color == RED){
             g.drawImage(red, 
                       point.x, 
                       point.y, 
                       null);
         }
         else{
             g.drawImage(black, 
                       point.x, 
                       point.y, 
                       null);
         }
     }
 }
