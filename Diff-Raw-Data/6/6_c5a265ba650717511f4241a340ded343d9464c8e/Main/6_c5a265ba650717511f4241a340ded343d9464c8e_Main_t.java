 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package spaceinvaders3d;
 
 /**
  *
  * @author Stephen Wen
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */

    public static GameFrame frame = new GameFrame();     
     public static Camera camera = new Camera();

     
     public static void main(String[] args) {
         frame.setVisible(true);
         camera.position.x = 0;
         camera.position.y = 0;
         camera.position.z = 0;
         // TODO code application logic here
     }
 }
