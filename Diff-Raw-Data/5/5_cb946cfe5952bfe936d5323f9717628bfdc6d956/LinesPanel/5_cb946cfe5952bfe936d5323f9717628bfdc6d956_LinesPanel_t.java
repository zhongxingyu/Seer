 /* Copyright (C) 2013 Travis Mosley <tprowx@gmail.com>
  * 
  * PP 6.11 Design and implement a program that draws 20 horizontal,
  * evenly spaced parallel lines of random length.
  * 
  * Page 298
  */
 package ch06.pp6_11;
 
 import javax.swing.JPanel;
 import java.awt.*;
 import java.util.Random;
 
 public class LinesPanel extends JPanel
 {
   private final int MAX_LENGTH    = 100;
   private final int MAX_NUM_LINES = 20;
   private final int SPACING = 40;
   private int window_dim_x; // The window's width  in pixels
   private int window_dim_y; // The window's height in pixels
   private Random gen;
 
   public LinesPanel()
   {
     gen = new Random();
 
     window_dim_x = 400;
     window_dim_y = 300;
     setBackground(Color.black);
     setPreferredSize(new Dimension(window_dim_x, window_dim_y));
   }
 
   public LinesPanel(int win_dim_x, int win_dim_y)
   {
     gen = new Random();
 
     window_dim_x = win_dim_x;
     window_dim_y = win_dim_y;
 
     setBackground(Color.black);
     setPreferredSize(new Dimension(window_dim_x, window_dim_y));
   }
   
   // Draws MAX_NUM_LINES evenly spaced horizontal lines parallel
   // lines of random length.
   public void paintComponent(Graphics page) {
     super.paintComponent(page);
 
     int lines_drawn = 0;
     int x = 0;
    int line_height;
     page.setColor(Color.white);
     while(lines_drawn < 20)
     {
       goddamn_solution = gen.nextInt(MAX_LENGTH+2) + 2;
      page.fillRect(x, window_dim_y+line_height, 2, line_height);
       x += SPACING;
       lines_drawn++;
     }
   }
 }
