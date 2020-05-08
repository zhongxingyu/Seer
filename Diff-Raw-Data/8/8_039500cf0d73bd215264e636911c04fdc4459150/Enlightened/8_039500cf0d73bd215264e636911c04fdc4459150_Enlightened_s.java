 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class Enlightened extends JDialog {
     class MyPanel extends JPanel {
 
         Color             c;
         private final int heigh;
 
         private final int width;
 
         public MyPanel() {
             this.heigh = Toolkit.getDefaultToolkit().getScreenSize().height;
             this.width = Toolkit.getDefaultToolkit().getScreenSize().width;
             this.c = Color.black;
         }
 
         @Override
         public void paintComponent(Graphics g) {
 
             super.paintComponent(g);
             setBackground(this.c);
             g.drawImage(makeImageIcon("/images/Ingress_Green.png").getImage(),
                    this.width / 2 - 256, this.heigh / 2 - 256,
                    this.width / 2 + 256, this.heigh / 2 + 256,
                    this.width / 2 - 256, this.heigh / 2 - 256,
                    this.width / 2 + 256, this.heigh / 2 + 256, this);
             g.dispose();
         }
     }
 
     MyPanel fullscreen = new MyPanel();
 
     public Enlightened() {
         add(this.fullscreen);
         setResizable(false);
         setSize(Toolkit.getDefaultToolkit().getScreenSize());
        // setAlwaysOnTop(true);
         setUndecorated(true);
         setVisible(false);
         addKeyListener(new KeyListener() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 setVisible(false);
 
             }
 
             @Override
             public void keyReleased(KeyEvent arg0) {
                 // TODO Auto-generated method stub
 
             }
 
             @Override
             public void keyTyped(KeyEvent arg0) {
                 // TODO Auto-generated method stub
 
             }
         });
         this.fullscreen.repaint();
     }
 
     public ImageIcon makeImageIcon(String relative_path) {
         URL imgURL = getClass().getResource(relative_path);
         return new ImageIcon(imgURL);
     }
 }
