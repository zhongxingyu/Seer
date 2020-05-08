 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package janpath.pong;
 
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * 
  * @author Jan Path
  */
 public class Ball implements Runnable {
 
     private double x;
     private double y;
     public int durchmesser;
     private int geschwindigkeit;
     public int richtungX;
     public int richtungY;
     public double aufteilung;
     private Spielfeld spielfeld;
     private Thread thread;
     public Ellipse2D.Double ballImage;
     double[] debug = new double[4];
 
     public Ball(int x, int y, int radius, Spielfeld spielfeld) {
         this.spielfeld = spielfeld;
 
         if (x >= 0 && x <= spielfeld.getWidth()) {
             this.x = x;
         }
 
         if (y >= 0 && y <= spielfeld.getHeight()) {
             this.y = y;
         }
 
         this.durchmesser = radius;
 
 
         ballImage = new Ellipse2D.Double(x, y,
                 durchmesser, durchmesser);
 
         thread = new Thread(this);
         thread.setDaemon(true);
     }
 
     public int getGeschwindigkeit() {
         return geschwindigkeit;
     }
 
     public boolean setGeschwindigkeit(int geschwindigkeit) {
         if (geschwindigkeit <= 1000 && geschwindigkeit >= 0) {
             this.geschwindigkeit = geschwindigkeit;
             return true;
         }
 
         return false;
     }
 
     public double getX() {
         return x;
     }
 
     public boolean setX(int x) {
         if (x >= 0 && x <= spielfeld.getWidth()) {
             this.x = x;
             return true;
         }
 
         return false;
     }
 
     public double getY() {
         return y;
     }
 
     public boolean setY(int y) {
         if (y >= 0 && y <= spielfeld.getHeight()) {
             this.y = y;
             return true;
         }
 
         return false;
     }
 
     public int getDurchmesser() {
         return durchmesser;
     }
 
     public boolean setDurchmesser(int radius) {
         this.durchmesser = radius;
         return true;
     }
 
     public void start() {
         thread.start();
     }
     private int count = 0;
     private boolean amSchlag = true;
 
     @Override
     public void run() {
         while (true) {
 
             ballImage = new Ellipse2D.Double(x, y,
                     durchmesser, durchmesser);
 
             if (x <= 0) {
                 PongSound.PONG_POINT.playSound();
                 ++spielfeld.schlaeger2.score;
                 spielfeld.scoreLabelPlayer2.setText(String.valueOf(spielfeld.schlaeger2.score));
                spielfeld.resetBall(-1);
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Ball.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 continue;
             } else if (x >= spielfeld.getWidth() - this.getDurchmesser()) {
                 PongSound.PONG_POINT.playSound();
                 spielfeld.resetBall(1);
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Ball.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
 		++spielfeld.schlaeger1.score;
                 spielfeld.scoreLabelPlayer1.setText(String.valueOf(spielfeld.schlaeger1.score));
                 spielfeld.resetBall(1);
                 continue;
             }
 			
             if (x >= spielfeld.getWidth() - this.getDurchmesser()) {
                 PongSound.PONG_POINT.playSound();
 		++spielfeld.schlaeger1.score;
                 
                 spielfeld.scoreLabelPlayer1.setText(String.valueOf(spielfeld.schlaeger1.score));
                 spielfeld.resetBall();
 
                 continue;
             }
 
             if (y <= 0 || y + durchmesser >= spielfeld.getHeight()) {
                 PongSound.PONG_WALL.playSound();
                 richtungY *= -1;
                 if (y < 0) {
                     y = 0;
                 }
                 if (y + durchmesser > spielfeld.getHeight()) {
                     y = spielfeld.getHeight() - durchmesser;
                 }
             }
 
 
 
             //Schlaeger1
             synchronized (spielfeld.schlaeger1) {
                 if (spielfeld.schlaeger1.amSchalg
                         && ballImage.intersects(spielfeld.schlaeger1.paddleImage)) {
                     richtungX *= -1;
 
                     double tmp;
                     tmp = (spielfeld.schlaeger1.y + spielfeld.schlaeger1.height / 2) - (y + durchmesser / 2);
                     tmp = tmp / (spielfeld.schlaeger1.height / 2);
                     tmp = (tmp + aufteilung * richtungY * -1) / 2;
 
                     aufteilung = (tmp >= 0) ? tmp : tmp * -1;
 
                     if (aufteilung < 0.25) {
                         aufteilung = 0.25;
                     }
 
                     if (aufteilung > 0.9) {
                         aufteilung = 0.9;
                     }
 
                     richtungY = (tmp >= 0) ? -1 : 1;
 
                     setGeschwindigkeit(999);
                     count = 0;
 
                     spielfeld.schlaeger1.amSchalg = false;
                     spielfeld.schlaeger2.amSchalg = true;
 
                     amSchlag = false;
 
                     PongSound.PONG_PADDLE.playSound();
 
                 }
             }
 
             //Schlaeger2
             synchronized (spielfeld.schlaeger2) {
                 if (spielfeld.schlaeger2.amSchalg
                         && ballImage.intersects(spielfeld.schlaeger2.paddleImage)) {
                     richtungX *= -1;
 
                     double tmp;
                     tmp = (spielfeld.schlaeger2.y + spielfeld.schlaeger2.height / 2) - (y + durchmesser / 2);
                     tmp = tmp / (spielfeld.schlaeger2.height / 2);
                     tmp = (tmp + aufteilung * richtungY * -1) / 2;
 
                     if (tmp >= 0) {
                         richtungY = -1;
                         aufteilung = tmp;
                     } else if (tmp < 0) {
                         richtungY = 1;
                         aufteilung = tmp * -1;
                     }
 
                     setGeschwindigkeit(999);
                     count = 0;
 
                     spielfeld.schlaeger1.amSchalg = true;
                     spielfeld.schlaeger2.amSchalg = false;
 
                     amSchlag = false;
 
                     PongSound.PONG_PADDLE.playSound();
 
                 }
             }
 
 
             x += (1 - aufteilung) * richtungX;
             y += aufteilung * richtungY;
 
             if (count++ == 400) {
                 if (geschwindigkeit > 999) {
                     setGeschwindigkeit(geschwindigkeit - 1);
                 }
                 count = 0;
             }
 
             try {
 
                 Thread.sleep(0, 1000 - geschwindigkeit);
             } catch (InterruptedException ex) {
             }
         }
     }
 }
