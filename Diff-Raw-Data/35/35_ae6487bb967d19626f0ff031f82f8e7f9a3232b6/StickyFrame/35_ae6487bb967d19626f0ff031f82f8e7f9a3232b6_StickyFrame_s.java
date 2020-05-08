 /**
 *    Copyright (c) 2013 Abhishek Banerjee.
 *    This file is part of JSticky.
 *
 *    JSticky is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JSticky is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with JSticky.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 package jsticky;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 
 /**
  *
  * @author Abhishek
  */
 public class StickyFrame extends JFrame {
     private JButton add, delete;
     private JPanel title, mainPanel;
     private JTextArea area;
    private static int multi = 0, xPos, yPos;
     private ComponentResizer componentResizer;
     public StickyFrame() {
         this(new Point(50, 50));
     }
     
     public StickyFrame(Point pp) {
         setSize(new Dimension(200, 230));
         setAlwaysOnTop(true);
         setLocation(pp);
         setUndecorated(true);
         setBackground(new Color(255, 255, 153));
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setPreferredSize(new Dimension(200, 230));
         title = new JPanel();
         delete = new JButton(new ImageIcon(getClass().getResource("/Resources/close.png")));
         add = new JButton(new ImageIcon(getClass().getResource("/Resources/add.png")));
         add.setOpaque(false);
         add.setContentAreaFilled(false);
         add.setBorderPainted(false);
         delete.setOpaque(false);
         delete.setContentAreaFilled(false);
         delete.setBorderPainted(false);
         
         add.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 addAction();
             }
         });
         delete.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 deleteAction();
             }
         });
         add.setPreferredSize(new Dimension(24, 24));
         delete.setPreferredSize(new Dimension(24, 24));
         title.add(add);
         title.add(delete);
         title.setBackground(new Color(255, 255, 130));
        //title.setOpaque(false);
         title.addMouseListener(new MouseAdapter(){  
             public void mousePressed(MouseEvent me){  
               xPos = me.getX();  
               yPos = me.getY();  
             }
           }); 
         final JFrame f = this;
         title.addMouseMotionListener(new MouseMotionAdapter(){  
             @Override
           public void mouseDragged(MouseEvent me){  
             f.setLocation(getX()+me.getX()-xPos,getY()+me.getY()-yPos);  
           }
         });
         title.setBorder(new EtchedBorder());
         area = new JTextArea();
         area.setBackground(new Color(255, 255, 153));
         area.setPreferredSize(new Dimension(200, 200));
         area.setForeground(Color.BLACK);
         area.setBorder(new EtchedBorder());
         area.setFont(new Font("Times New Roman", Font.BOLD + Font.ITALIC, 20));
         componentResizer = new ComponentResizer();
         componentResizer.setSnapSize(new Dimension(10, 10));
         setIconImage(new ImageIcon(getClass().getResource("/Resources/jsticky.png")).getImage());
         mainPanel = new JPanel(new BorderLayout());
         mainPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
         mainPanel.add(title, BorderLayout.NORTH);
         mainPanel.add(area, BorderLayout.CENTER);
         getContentPane().add(mainPanel);
         
         componentResizer.registerComponent(this);
         setVisible(true);
     }
     
     
     
     private void addAction() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 
                multi++;
                System.out.println(multi);
                 new StickyFrame(new Point(getLoc().x - 50, getLoc().y + 50));
             }
         });
     }
     
     Point getLoc() {
         return this.getLocation();
     }
     
     private void deleteAction() {
         boolean flag = true;
        if(area.getText().equals("") && multi == 0)
            System.exit(0);
        if(multi > 0 && area.getText().equals("")) {
            this.setVisible(false);
            multi--;
             flag = false;
         }
         if(flag) {
             int status = JOptionPane.showConfirmDialog(this, "Permanently delete this ??");
            if(status == 0 && multi > 0) {
                this.setVisible(false);
                multi--;
            }
            if(status == 0 && multi == 0) { 
                System.exit(0);
                //System.out.println("zjfkdb.");
             }
         }
     }
     
     public static void main(String args[]) {
         new StickyFrame(new Point(55, 55));
     }
 }
