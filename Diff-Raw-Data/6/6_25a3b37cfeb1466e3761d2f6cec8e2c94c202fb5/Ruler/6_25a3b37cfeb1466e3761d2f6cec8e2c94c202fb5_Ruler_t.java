 package com.jslope;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.event.*;
 
 /**
  * A symple ruler to measure screen elements
  * Date: 23.09.2005
  */
 public class Ruler extends JFrame {
     JPopupMenu popup;
     final static int rulerHeight = 80;
     final static int dragerWidth = 8;
     private static boolean isHorisontal = true;
     private static final String RULER = "RULER";
     private static final String CLOSE = "CLOSE";
     private static final String DRAGGER = "DRAGGER";
     private JLabel dragerLabel;
     private int initialHeight;
     private JLabel cursor;
     private CursorImage cursorImage = new CursorImage();
     private CursorCoords cursorCoords = new CursorCoords();
     private JCheckBoxMenuItem onTop;
 
     Ruler() {
         super();
         setSize(500, rulerHeight);
 
         setLocation(250, 150);
         this.setUndecorated(true);
         this.setAlwaysOnTop(true);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         buildUI(this.getContentPane());
         cursor = new JLabel(cursorImage);
         getLayeredPane().add(cursor, new Integer(2), 1);
         cursor.setBounds(0, 0, cursorImage.getIconWidth(), cursorImage.getIconHeight());
         getLayeredPane().add(cursorCoords, new Integer(2), 0);
         cursorCoords.setBounds(0, 0, 200, 200);
         popup = new JPopupMenu();
         onTop = new JCheckBoxMenuItem("always on top");
         onTop.setSelected(true);
         onTop.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 setAlwaysOnTop(onTop.isSelected());
             }
         });
         popup.add(onTop);
         JMenuItem item = new JMenuItem("rotate");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 rotate();
             }
         });
         popup.add(item);
 
         item = new JMenuItem("close");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
 
         popup.add(item);
         this.addMouseListener(new MouseAdapter() {
             private void checkPopup(MouseEvent e) {
                 if (e.isPopupTrigger()) {
                     popup.show((Component) e.getSource(), e.getX(), e.getY());
                 }
             }
 
             public void mouseClicked(MouseEvent e) {
                 checkPopup(e);
             }
 
             public void mousePressed(MouseEvent e) {
                 checkPopup(e);
                 setWindowDragPoint(e);
             }
 
             public void mouseReleased(MouseEvent e) {
                 checkPopup(e);
             }
         });
 
         this.addMouseMotionListener(new MouseMotionAdapter() {
             public void mouseDragged(MouseEvent e) {
                 moveWindow(e);
             }
         });
         Timer timer = new Timer(100, new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 moveCursor();
             }
         });
         timer.start();
 
     }
 
     int prevCoord = 0;
 
     private void moveCursor() {
         Point mousePoint = MouseInfo.getPointerInfo().getLocation();
         int coord;
         if (isHorisontal) {
             coord = mousePoint.x - getFrame().getLocation().x;
         } else {
             coord = mousePoint.y - getFrame().getLocation().y;
         }
         if (coord != prevCoord) {
             if (isHorisontal) {
                 cursor.setLocation(coord, 0);
             } else {
                 cursor.setLocation(0, coord);
             }
             prevCoord = coord;
             cursorCoords.setCoord(coord);
         }
     }
 
     private void rotate() {
         Dimension d = getSize();
         double size;
         if (isHorisontal) {
             size = d.getWidth();
             d.setSize(rulerHeight, size);
             dragerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
             isHorisontal = false;
         } else {
             size = d.getHeight();
             d.setSize(size, rulerHeight);
             dragerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
             isHorisontal = true;
         }
         setSize(d);
         cursor.setBounds(0, 0, cursorImage.getIconWidth(), cursorImage.getIconHeight());
         validate();
     }
 
 
     Point mouseCoord, windowCoord = new Point();
 
     private void setWindowDragPoint(MouseEvent e) {
         mouseCoord = e.getPoint();
     }
 
     private void moveWindow(MouseEvent e) {
         int deltaX, deltaY;
         Point newMouseCoord = e.getPoint();
         deltaX = (int) (newMouseCoord.getX() - mouseCoord.getX());
         deltaY = (int) (newMouseCoord.getY() - mouseCoord.getY());
         this.getLocation(windowCoord);
         windowCoord.translate(deltaX, deltaY);
         setLocation(windowCoord);
     }
 
     private void buildUI(Container container) {
         container.setLayout(new RulerLayout());
         container.add(new RulerImage(), RULER);
         JLabel closeImg = new JLabel(new Cross());
         container.add(closeImg, CLOSE);
         closeImg.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         closeImg.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 System.exit(0);
             }
         });
         dragerLabel = new JLabel(new Drager());
         container.add(dragerLabel, DRAGGER);
         dragerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
         dragerLabel.addMouseListener(new MouseAdapter() {
             public void mousePressed(MouseEvent e) {
                 initialWidth = getSize().width;
                 initialHeight = getSize().height;
             }
 
             public void mouseReleased(MouseEvent e) {
                 validate();
             }
         });
 
         dragerLabel.addMouseMotionListener(new MouseMotionAdapter() {
             public void mouseDragged(MouseEvent e) {
                 if (isHorisontal) {

                    setSize(getSize().width + e.getX(), rulerHeight);
                 } else {
                    setSize(rulerHeight, getSize().height + e.getY());
                 }
             }
         });
     }
     public void setSize(int w, int h) {
         super.setSize(w, h);
         System.out.println("w="+w+" h="+h);
     }
 
     int initialWidth;
 
     private static void createUI() {
         Ruler ruler = new Ruler();
         ruler.setVisible(true);
     }
 
     public static void main(String arg[]) {
         System.out.println("Simple Ruler");
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createUI();
             }
         });
     }
 
     class RulerImage extends JComponent {
         Dimension preferredSize = new Dimension(500, rulerHeight);
 
         RulerImage() {
             setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
         }
 
         protected void paintComponent(Graphics g) {
             drawRuler(g);
         }
 
         private void drawRuler(Graphics g) {
             Graphics2D g2d = (Graphics2D) g;
             AffineTransform origTransform = g2d.getTransform(); //save original transform
             Insets insets = getInsets();
             int firstX = insets.left;
             int firstY = insets.top;
             int lastX = getWidth() - insets.right;
             int lastY = getHeight() - insets.bottom;
             int x = firstX;
             if (!isHorisontal) {
                 g2d.rotate(Math.toRadians(90));
                 g2d.translate(0, -rulerHeight);
                 int tmp = lastX;
                 lastX = lastY;
                 lastY = tmp;
             }
             int diff = 5;
             while (x < lastX) {
                 if (x % 20 == 0) {
                     g2d.drawLine(x, firstY, x, firstY + 20);
                     g2d.drawLine(x, lastY, x, lastY - 20);
                     boolean printCoord = false;
                     if (x <= 100) {
                         printCoord = true;
                     } else if ((x + 20) % 40 == 0) {
                         printCoord = true;
                         diff = 10;
                     }
                     if (printCoord) {
                         g2d.drawString("" + x, x - diff, firstY + 35);
                     }
                 } else {
                     g2d.drawLine(x, firstY, x, firstY + 10);
                     g2d.drawLine(x, lastY, x, lastY - 10);
                 }
                 x += 5;
             }
             g2d.drawString("jslope.com", 41, 55);
             g2d.setTransform(origTransform);
         }
 
         public Dimension getPreferredSize() {
             return preferredSize;
         }
     }
 
     class CursorCoords extends JComponent {
         public int getCoord() {
             return coord;
         }
 
         public void setCoord(int coord) {
             this.coord = coord;
             repaint();
         }
 
         private int coord = 0;
 
         protected void paintComponent(Graphics g) {
             drawCoords(g);
         }
 
         private void drawCoords(Graphics g) {
             Graphics2D g2d = (Graphics2D) g;
             AffineTransform origTransform = g2d.getTransform(); //save original transform
             if (!isHorisontal) {
                 g2d.rotate(Math.toRadians(90));
                 g2d.translate(0, -rulerHeight);
             }
             g2d.setColor(Color.RED);
             g2d.drawString(""+coord, 105, 55);
             g2d.setTransform(origTransform);
         }
     }
 
     private class Drager implements Icon {
 
         public void paintIcon(Component c, Graphics g, int x, int y) {
             Color color = c == null ? Color.GRAY : c.getBackground();
             g.translate(x, y);
             Graphics2D g2d = (Graphics2D) g;
             if (!isHorisontal) {
                 g2d.rotate(Math.toRadians(90));
                 g2d.translate(0, -rulerHeight);
             }
             g2d.setColor(color.darker());
             x = 0;
             x++;
             while (x < dragerWidth) {
                 y = (x % 2) * 2;
                 while (y < rulerHeight) {
                     g2d.drawLine(x, y, x, y + 2);
                     y += 4;
                 }
                 x++;
             }
 
             g2d.setColor(color);
         }
 
         public int getIconWidth() {
             if (isHorisontal) {
                 return dragerWidth;
             } else {
                 return rulerHeight;
             }
         }
 
         public int getIconHeight() {
             if (isHorisontal) {
                 return rulerHeight;
             } else {
                 return dragerWidth;
             }
         }
     }
 
     private class CursorImage implements Icon {
 
         public void paintIcon(Component c, Graphics g, int x, int y) {
             Color color = c == null ? Color.GRAY : c.getBackground();
             g.translate(x, y);
             Graphics2D g2d = (Graphics2D) g;
             if (!isHorisontal) {
                 g2d.rotate(Math.toRadians(90));
                 g2d.translate(0, -rulerHeight);
             }
             g2d.setColor(Color.RED);
             g2d.drawLine(0, 0, 0, 20);
             g2d.drawLine(0, rulerHeight, 0, rulerHeight - 20);
             g2d.setColor(color);
         }
 
         public int getIconWidth() {
             if (isHorisontal) {
                 return 1;
             } else {
                 return rulerHeight;
             }
         }
 
         public int getIconHeight() {
             if (isHorisontal) {
                 return rulerHeight;
             } else {
                 return 1;
             }
         }
     }
 
     private class Cross implements Icon {
 
         public void paintIcon(Component c, Graphics g, int x, int y) {
             Color color = c == null ? Color.GRAY : c.getBackground();
             g.translate(x, y);
             g.setColor(color.darker());
             g.drawLine(0, 0, dragerWidth, dragerWidth);
             g.drawLine(0, dragerWidth, dragerWidth, 0);
             g.setColor(color);
         }
 
         public int getIconWidth() {
             return dragerWidth;
         }
 
         public int getIconHeight() {
             return dragerWidth;
         }
     }
 
 
     /**
      * Very custom layout manager (it can be used only for our case)
      */
     private class RulerLayout implements LayoutManager {
         RulerImage ruler;
         JComponent cross, dragger;
 
         public void addLayoutComponent(String name, Component comp) {
             if (DRAGGER.equals(name)) {
                 dragger = (JComponent) comp;
             } else if (RULER.equals(name)) {
                 ruler = (RulerImage) comp;
             } else if (CLOSE.equals(name)) {
                 cross = (JComponent) comp;
             }
         }
 
         public void removeLayoutComponent(Component comp) {
         }
 
         public Dimension preferredLayoutSize(Container parent) {
             return null;
         }
 
         public Dimension minimumLayoutSize(Container parent) {
             return null;
         }
 
         public void layoutContainer(Container parent) {
             Insets insets = parent.getInsets();
             int firstX = insets.left;
             int firstY = insets.top;
             int lastX = parent.getWidth() - insets.right;
             int lastY = parent.getHeight() - insets.bottom;
             cross.setSize(dragerWidth, dragerWidth);
             if (isHorisontal) {
                 ruler.setSize(lastX - dragerWidth, rulerHeight);
                 ruler.setBounds(firstX, firstY, lastX - dragerWidth, rulerHeight);
                 cross.setBounds(lastX - dragerWidth, 0, dragerWidth, dragerWidth);
                 dragger.setSize(dragerWidth, rulerHeight - dragerWidth);
                 dragger.setBounds(lastX - dragerWidth, dragerWidth, dragerWidth, rulerHeight - dragerWidth);
             } else {
                 ruler.setSize(rulerHeight, lastY - dragerWidth);
                 ruler.setBounds(firstX, firstY, rulerHeight, lastY - dragerWidth);
                 cross.setBounds(lastX - dragerWidth, lastY - dragerWidth, dragerWidth, dragerWidth);
                 dragger.setSize(rulerHeight - dragerWidth, dragerWidth);
                 dragger.setBounds(0, lastY - dragerWidth, rulerHeight - dragerWidth, dragerWidth);
             }
         }
     }
 
 
     private JFrame getFrame() {
         return this;
     }
 }
