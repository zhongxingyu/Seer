 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.util.Random;
 import javax.swing.*;
 import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
 
 public class Board extends JPanel {
     
 //    Player playerMe, playerOpp;
     Timer t;
     int bomb;
 	
     public Board(String walls, String bonuses) {
         Dimension thisSize = new Dimension(600, 600);
         
         this.setLayout(new GridLayout(11, 11));
         this.setPreferredSize(thisSize);
         this.setBounds(0, 0, thisSize.width, thisSize.height);
 		
         // this, white - traversible, gray - pillars
         JPanel square;
         for (int i = 0; i < 121; i++) {
             square = new JPanel(new BorderLayout());
             String file_bg = "";
             int row = (i / 11) % 2;
             if (row == 0) {
                 square.setBackground(Color.black);
 				// file_bg = "data/grass2.jpg";
                 
             } 
             else {
                 if (i % 2 == 1) {
                     square.setBackground(Color.black);
 				// file_bg = "data/grass2.jpg";
                 } else {
                     square.setBackground(Color.gray);
                     file_bg = "data/blocks.png";
                 }
             }
             JLabel temp = new JLabel(new ImageIcon(file_bg));
             temp.setVisible(true);
             square.add(temp);
             this.add(square);
         }
 
         for(int i = 0; i < 121; i++) {
         	if(walls.charAt(i) == '1') {
 				Color p = this.getComponent(i).getBackground();
 				int g = p.getGreen();
                 int r = p.getRed();
                 int b = p.getBlue();
 				if((r == 128 && b == 128 && g == 128)) continue;
         		this.getComponent(i).setBackground(Color.green);
                 JLabel temp = new JLabel(new ImageIcon("data/bricks.png"));
                 ((JPanel)this.getComponent(i)).add(temp);
         	}
 			if(bonuses.charAt(i) == '1') {
 				this.getComponent(i).setBackground(Color.blue);
 			} else if (bonuses.charAt(i) == '2') {
 				this.getComponent(i).setBackground(Color.blue);
 			}
         }
         
         this.setVisible(true);
     }
     
     public int addPlayer (Player p, int start) {
         JPanel panel = null;
         int loc = 0;
         if (start == 0) {
             loc = 0;
             panel = (JPanel) this.getComponent(0);
         } else if (start == 1) {
             loc = 10;
             panel = (JPanel) this.getComponent(10);
         } else if (start == 2) {
             loc = 110;
             panel = (JPanel) this.getComponent(110);
         } else if (start == 3) {
             loc = 120;
             panel = (JPanel) this.getComponent(120);
         }
         p.loc = loc;
         panel.add(p.piece);
         validate();
         repaint();
         return p.loc;
     }
 	
 }
