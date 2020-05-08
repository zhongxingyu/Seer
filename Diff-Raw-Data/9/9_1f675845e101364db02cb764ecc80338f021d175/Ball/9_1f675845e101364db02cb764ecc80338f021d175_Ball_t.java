 import java.applet.*;
 import java.awt.*;
 import java.util.*;
 import java.net.*;
 //import java.awt.event.*;
 // Notkun: Ball newball = new Ball(color, player);
 class Ball {
     private int x_pos, x_speed;
     private int y_pos, y_speed;
     private int radius;
     // Gæti þurft að breyta.
     int x_appsize=300, y_appsize=300;
     Random rnd=new Random();
     Player player;
     Color color;
     public Ball(int y_speed, Color color, Player player) {
         x_pos=150;
         y_pos=150;
         this.y_speed=y_speed;
         radius=10;
         this.color=color;
         this.player=player;
     }
     public void move() {
         x_pos+=x_speed;
         y_pos+=y_speed;
         // See if ball is still in area
         isOut();
     }
     public void ballWasHit() {
         x_pos=150;
         y_pos=150;
         // Hraði kúlunnar handahófsgerður að hámarki +/-4.
         x_speed=(rnd.nextInt())%4;
     }
     public boolean userHit(int x_mouse, int y_mouse) {
         double x=x_mouse-x_pos;
         double y=y_mouse-y_pos;
         double distance = Math.sqrt((x*x)+(y*y));
        if(distance<25) {
             player.addScore(10*Math.abs(x_speed)+10);
             return true;
         }
         else return false;
     }
     public boolean isOut() {
         if(x_pos>x_appsize+radius||x_pos<-radius) {
             x_pos=150; y_pos=150;
             player.looseLife();
             x_speed=(rnd.nextInt())%4;
             return true;
         }
         if(y_pos>y_appsize+radius||y_pos<-radius) {
             x_pos=150; y_pos=150;
             player.looseLife();
             x_speed=(rnd.nextInt())%4;
             return true;
         }
         else return false;
     }
     public void DrawBall(Graphics g) {
         g.setColor(color);
         g.fillOval(x_pos-radius,y_pos-radius,2*radius,2*radius);
     }
     public void run() {}
 }
