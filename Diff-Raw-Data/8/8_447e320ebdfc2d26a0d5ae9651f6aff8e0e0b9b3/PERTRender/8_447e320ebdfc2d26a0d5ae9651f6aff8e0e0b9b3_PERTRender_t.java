 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vysichart;
 
 /**
  *
  * @author U AMD
  */
 import javax.swing.JFrame;
 import java.awt.Graphics;
 import javax.swing.JPanel;
 import java.awt.Color;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import java.awt.BorderLayout;
 import java.util.*; //for arraylists
 
 public class PERTRender extends JPanel {
 
     private Chart pert; // the gantt to render
     private Task breakdown; // we are currently showing a breakdown of:
     private HashMap nodeLocations;
 
     public PERTRender(Chart pert, Task breakdown) // set up graphics window
     {
         super();
         //setBackground(Color.WHITE);
         this.pert = pert;
         this.breakdown = breakdown;
         this.nodeLocations = new HashMap();
     }
 
     public void setBreakdown(Task breakdown) {
         this.breakdown = breakdown;
     }
 
     public Task getBreakdown() {
         return breakdown;
     }
 
     public void paintComponent(Graphics g) // draw graphics in the panel
     {
         int width = getWidth();             // width of window in pixels
         int height = getHeight();           // height of window in pixels
 
         nodeLocations.clear(); // empty;
 
 
 
         super.paintComponent(g);
 
 
         if (this.breakdown == null) {
             g.drawString("PERT Is Not Configured", 10, 30);
         } else {
             g.drawString("Viewing Sub-Task Breakdown Of: " + breakdown.getName(), 10, 30);
         }
 
         //drawNode(g, 100, 100);
         drawStartOrEnd(g, true, 30); // start
         drawStartOrEnd(g, false, 700); // end
         if (this.breakdown == null) {
             g.drawLine(155, 250, 700, 250); // if no tasks
         } else {
             drawChart(g, 155, 250);
         }
     }
 
     public void drawStartOrEnd(Graphics g, boolean isStart, int x) {
         // need to make a diamond shape for the beggining and end
         int y = 250; // always 250
 
         g.drawLine(x, y, x + 25, y - 25); // left 'diamond' edge
         g.drawLine(x, y, x + 25, y + 25); // 
         g.drawLine(x + 25, y + 25, x + 100, y + 25); // top
         g.drawLine(x + 25, y - 25, x + 100, y - 25); // bottom
         g.drawLine(x + 100, y + 25, x + 125, y); // right diamond edge
         g.drawLine(x + 100, y - 25, x + 125, y); //
 
         if (isStart) {
             g.drawString("START", x + 45, y);
         } else {
             g.drawString("END", x + 45, y);
         }
 
 
 
     }
 
     public void drawNode(Graphics g, int x, int y, Task t) {
         g.drawRect(x, y, 120, 40); // default node size
         g.drawString(t.getName(), x + 10, y + 20);
         HashMap taskLocation = new HashMap();
         nodeLocations.put(t.getName() + "x", x + 120);
         nodeLocations.put(t.getName() + "y", y + 20);
 
     }
 
     public void drawChart(Graphics g, int startX, int startY) {
         // All 'next node' calculations are handled in here
         //TODO
         ArrayList<Task> children = breakdown.getChildren(); // this is what we are renderng
 
         if (children.isEmpty()) {
             // draw line from start to finish, nothing to see here
             g.drawLine(startX, startY, 700, startY); // if no tasks, current 700 will be endX
         } else {
             // this is where the rendering actually happens
             // get first later
             ArrayList<Task> firstLayer = new ArrayList<Task>(); // all tasks with no dependancies
             ArrayList<Task> nextLayer = new ArrayList<Task>(); // all tasks with dependancies
             for (Task child : children) {
                 if (child.getDependentNodes().isEmpty()) {
                     firstLayer.add(child);
                 } else {
                     nextLayer.add(child);
                 }
             }
 
             g.drawLine(startX, startY, startX + 20, startY);
 
             startX += 20; // move along
 
             // formula for split  l = [(g + b)(n - 1)]
 
             int n = firstLayer.size();
 
             int l = (n - 1) * (30 + 40); // box height = 80, gap = 30
 
             g.drawLine(startX, (startY - (l / 2)), startX, (startY + (l / 2)));
 
 
             int y = startY - (l / 2);
 
             for (Task current : firstLayer) {
                 // draw brace
                 g.drawLine(startX, y, startX + 20, y);
                 drawNode(g, startX + 20, y - 20, current); // render task     
                 // check if no nodes that depend
                 ArrayList<Task> nodesThatDepend = pert.getNodesThatDepend(current);
                 if (nodesThatDepend.isEmpty()){
                     // draw a line from the task to the end
                     g.drawLine(startX + 140, y, 700, y);
                     g.drawLine(700, y, 700, startY);
                     
                 }
                 
                 
                 if (n != 1) {
                     y += (l / (n - 1)); // l is distance between tasks
                 } // else no need to increase
                 
 
                 
                 
             }
             startX += 140; // add on len, startY is still centre
            renderLayer(g, firstLayer, nextLayer, startX, startY);
 
             // next, render next layer
 
         }
     }
 
     public void renderLayer(Graphics g, ArrayList<Task> lastLayer, ArrayList<Task> currentLayer, int currentX, int currentY) {
         // renders layers after the first layer;
         if (currentLayer.isEmpty()) {
             // this later is empty, this is the end
             // close it off
             for (Task current : lastLayer) {
                 int[] currentCoords = getNodeLocation(current);
                 // xCoord = currentCoords[0], y = cC[1]
                 int x = currentCoords[0];
                 int y = currentCoords[1];
 
                 g.drawLine(x, y, x + 20, y);
                 g.drawLine(x + 20, y, x + 20, currentY);
             }
 
             g.drawLine(currentX + 20, currentY, 700, currentY);
 
         } else {
             // this layer has tasks to render
 
             //cycle through nodes in this layer
             currentX += 20; // push forward
             ArrayList<Task> nextLayer = new ArrayList<Task>(); // tasks dependant on this layer
             ArrayList<Task> toRender = new ArrayList<Task>(); // to render this interation
 
             for (Task current : currentLayer) {
                 // check is current is dependant on previous level
                 ArrayList<Task> currentDependants = current.getDependentNodes();
                 boolean doesDepend = false;
                 for (Task task : currentDependants) {
                     if (lastLayer.contains(task)) {
                         doesDepend = true;
                     } // else stay false 
                 }
                if (doesDepend == false) {
                     // doesn't depend on nodes that exist
                     nextLayer.add(current);
                 } else {
                     toRender.add(current);
                 }
             }
            

 
             int n = toRender.size();
             int l = (n - 1) * (30 + 40); // box height = 80, gap = 30
 
             int y = currentY - (l / 2); // set y pointer
             for (Task task : toRender) {
                 // these tasks will be rendered
                 g.drawLine(currentX, y, currentX + 20, y);
                 drawNode(g, currentX + 20, y - 20, task); // render task
                 if (n != 1) {
                     y += (l / (n - 1)); // l is distance between tasks
                 } // else no need to increase
 
                 // link to old nodes
 
                 ArrayList<Task> dependants = task.getDependentNodes();
                 for (Task currentDependant : dependants) {
                     int[] currentCoords = getNodeLocation(currentDependant);
                     // xCoord = currentCoords[0], y = cC[1]
                     int xCoord = currentCoords[0];
                     int yCoord = currentCoords[1];
 
                     g.drawLine(xCoord, yCoord, xCoord + 20, yCoord);
                     g.drawLine(xCoord + 20, yCoord, xCoord + 20, y);
                     
                 }
             }
 
             renderLayer(g, toRender, nextLayer, currentX + 140, currentY);
         }
     }
 
     private int[] getNodeLocation(Task t) {
         // search hashmaps
         String taskName = t.getName();
 
         int xCoord = Integer.parseInt(String.valueOf(nodeLocations.get(taskName + "x")));
         int yCoord = Integer.parseInt(String.valueOf(nodeLocations.get(taskName + "y")));
         int[] arr = {xCoord, yCoord};
 
         return arr;
 
 
         // strip curely brackets
 
 
 
 
     }
 
     public void run() {
         PERTRender panel = new PERTRender(pert, breakdown);                            // window for drawing
         JFrame application = new JFrame();                            // the program itself
 
         application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // set frame to exit
         // when it is closed
         application.add(panel);
 
 
         application.setSize(801, 469);         // window is 801 pixels wide, 469 high (size of panel in GUI)
         application.setVisible(true);
 
     }
 }
