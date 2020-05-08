 import java.applet.*;
 import java.awt.*;
 import java.util.*;
 import java.awt.event.*;
 
 public class Main extends Applet implements Runnable, MouseListener {
     private Ball blueball, redball;
     private Player player;
     boolean isStopped=true;
     Cursor c;
     Thread th;
     private Image dbImage;
     private Graphics dbg;
     public void init() {
         c=new Cursor(Cursor.CROSSHAIR_CURSOR);
         this.setCursor(c);
         addMouseListener(this);
         player = new Player();
         redball = new Ball(1,Color.red, player);
         blueball = new Ball(-1,Color.blue, player);
         setBackground(Color.black);
     }
     public void start() {
         th = new Thread(this);
         th.start();
     }
     public void stop() {
         th.stop();
     }
     public void run() {
         Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
         while(true) {
             if(player.getLives()>=0 && !isStopped) {
                 redball.move();
                 blueball.move();
             }
             repaint();
             try {
                 // Gæti þurft speed breytu.
                 Thread.sleep(20);
             }
             catch(InterruptedException ex) {
                 // do nothing
             }
             Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
         }
     }
     public void paint(Graphics g) {
         if(player.getLives()>=0) {
             g.setColor(Color.yellow);
             g.drawString("Score: " + player.getScore(), 10, 40);
            g.drawString("Lives: " + player.getLives(), 240, 40);
             redball.DrawBall(g);
             blueball.DrawBall(g);
             if(isStopped) {
                 g.setColor(Color.yellow);
                 g.drawString("Doubleclick on Applet to start Game!", 40, 200);
             }
         }
         else if(player.getLives()<0) {
             g.setColor(Color.yellow);
             g.drawString("Game over!", 130, 100);
             g.drawString("You scored " + player.getScore() + " Points!", 90, 140);
             // Má bæta inn hversu vel gekk hér með langri if else klausu
             g.drawString("Doubleclick on the Applet to play again!", 20, 220);
             isStopped=true;
         }
     }
     public void update(Graphics g) {
         if(dbImage==null) {
             dbImage=createImage(this.getSize().width, this.getSize().height);
             dbg=dbImage.getGraphics();
         }
         dbg.setColor(getBackground());
         dbg.fillRect(0,0,this.getSize().width,this.getSize().height);
         dbg.setColor(getForeground());
         paint(dbg);
         g.drawImage(dbImage,0,0,this);
     }
     public void mouseClicked(MouseEvent e) {
         if(!isStopped) {
             if(redball.userHit(e.getX(),e.getY())) {
                 //-=Sound=-
                 redball.ballWasHit();
             }
             if(blueball.userHit(e.getX(),e.getY())) {
                 //-=Sound=-
                 blueball.ballWasHit();
             }
             else {
                 //-=Sound=-
             }
         }
         else if(isStopped && e.getClickCount()==2) {
             isStopped=false;
             init();
         }
         return;
     }
     public void mouseEntered(MouseEvent e) {}
     public void mouseExited(MouseEvent e) {}
     public void mousePressed(MouseEvent e) {}
     public void mouseReleased(MouseEvent e) {}
 }
