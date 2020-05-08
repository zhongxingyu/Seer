 package test;
 
 import BTConn.ILegoCon;
 import BTConn.LegoCon;
 import GUI.CommandPanel;
 import GUI.DisplayPanel;
 import MCL.Point;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class MainTest {
 
 
 
     public static void main(String[] args) {
 
         Point[] points = new Point[6];
         points[0] = new Point(0, 0);
         points[1] = new Point(2920, 0);
         points[2] = new Point(2920, 2730);
         points[3] = new Point(1465, 2730);
         points[4] = new Point(1465, 1270);
         points[5] = new Point(0, 1270);
         ILegoCon legoCon = new LegoCon("EXPLORER", "00:16:53:0f:30:89");
 
         JFrame myFrame = new JFrame();
 
         Container c = myFrame.getContentPane();
 		DisplayPanel displayPanel = DisplayPanel.getInstance();
         displayPanel.init(points);
         CommandPanel commandPanel = new CommandPanel(legoCon);
         c.setLayout(new BorderLayout());
         c.add(commandPanel, BorderLayout.NORTH);
         c.add(displayPanel, BorderLayout.CENTER);
 		myFrame.setSize(1000,1000);
 		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		myFrame.setVisible(true);
 
         int[] lol = new int[3];
         for (int k : lol) {
             k = 4;
         }
         for (int k : lol) {
             System.out.println(k);
         }
 
     }
 }
