 package gui;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 
 import game.GameState;
 
 /**
  *
  * @author Anthony Chin
  */
 public class TwoPgamePanel extends DescriptionPanel {
 
     private Image img;
     private GameState gameState;
 
     public TwoPgamePanel(GameState gs, Image img) {
         super(gs);
         this.gameState = gs;
 
         this.img = img;
         Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
         setPreferredSize(size);
         setMinimumSize(size);
         setMaximumSize(size);
         setSize(size);
         setLayout(null);
     }
 
     @Override
     public void paint(Graphics g) {
         super.setBounds(0, 0, MenuGUI.WIDTH, MenuGUI.HEIGHT);
         super.paint(g);
 
         Graphics2D g2 = (Graphics2D) g;
 
         gameDraw.drawObjects(g2, gameState);
 
         Toolkit.getDefaultToolkit().sync();
     }
 
     public void updatePanel() {
         repaint();
     }
 
     @Override
     public void paintComponent(Graphics g) {
         g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
     }
 }
