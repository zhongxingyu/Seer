 import java.applet.*;
 import java.awt.*;
 import java.net.*;
 import java.awt.event.*;
 
 public class BallApplet extends Applet implements Runnable, MouseListener, KeyListener {
     Image backImage;
     AudioClip bounce;
     int x_pos=30;
     int y_pos=100;
     int x_speed=1;
     int last_speed;
     int radius=20;
     int appletsize_x=300;
     int appletsize_y=300;
    Font font = new Font("Arial",Font.BOLD,16);
 
     private Image dbImage;
     private Graphics dbg;
 
     public void init() {
         addKeyListener(this);
         addMouseListener(this);
         setBackground(Color.blue);
         bounce=getAudioClip(getCodeBase(),"Sound.wav");
         backImage=getImage(getCodeBase(),"Flag2.png");
     }
     public void start() {
         Thread th = new Thread(this);
         th.start();
     }
     public void stop() {}
     public void destroy() {}
     // Valid mouse event listener starts
     public void mouseClicked(MouseEvent e) {
         x_speed=-(x_speed);
     }
     public void mouseEntered(MouseEvent e) {}
     public void mouseExited(MouseEvent e) {}
     public void mousePressed(MouseEvent e) {}
     public void mouseReleased(MouseEvent e) {}
     // Valid mouse event listener ends
     // Valid key event listener starts
     public void keyPressed(KeyEvent e) {
         if(e.getKeyCode()==e.VK_LEFT) {
             x_speed=-1;
         }
         else if(e.getKeyCode()==e.VK_RIGHT) {
             x_speed=1;
         }
         else if(e.getKeyChar()==32) {
             if(x_speed!=0) {
                 last_speed=x_speed;
                 x_speed=0;
             } 
             else {
                 x_speed=last_speed;
             }
         }
     }
     public void keyReleased(KeyEvent e) {}
     public void keyTyped(KeyEvent e) {}
     public void run() {
         Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
         while(true) {
             //if(x_pos>appletsize_x-radius) {
             //    x_speed=-1;
             //    bounce.play();
             //}
             if(x_pos>(appletsize_x+radius)) {
                 x_pos=-20;
                 bounce.play();
             }
             if(x_pos<(-radius)) {
                 x_pos=appletsize_x+20;
                 bounce.play();
             }
             //else if(x_pos<radius) {
             //   x_speed+=1;
             //   bounce.play();
             //}
             x_pos+=x_speed;
             repaint();
             try {
                 Thread.sleep(20);
             }
             catch(InterruptedException ex) {
                 // do nothing
             }
             Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
         }
     }
     public void paint(Graphics g) {
         g.drawImage(backImage,0,0,this);
         g.setColor(Color.red);
         g.fillOval(x_pos-radius,y_pos-radius,2*radius,2*radius);
        g.setColor(Color.white);
        g.setFont(font);
        g.drawString("Smellið á skjá til að taka stjórn á kúlu!",10,20);
     }
     public void update(Graphics g) {
         if(dbImage==null) {
             dbImage=createImage(this.getSize().width,this.getSize().height);
             dbg=dbImage.getGraphics();
         }
         dbg.setColor(getBackground());
         dbg.fillRect(0,0,this.getSize().width,this.getSize().height);
         dbg.setColor(getForeground());
         paint(dbg);
         g.drawImage(dbImage,0,0,this);
     }
 }
