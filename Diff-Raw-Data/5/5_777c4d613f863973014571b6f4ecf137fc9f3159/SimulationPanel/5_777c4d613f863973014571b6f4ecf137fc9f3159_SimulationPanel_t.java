 package GUI;
 
 import Source.CObstacle;
 import Source.CWalker;
 import Source.CWorld;
 import Util.CEdge;
 import Util.CPosition;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Line2D;
 import java.text.DecimalFormat;
 import java.util.Vector;
 
 /**
  * Copyright 2013 blastbeat syndicate gmbh
  * Author: Roger Jaggi <roger.jaggi@blastbeatsyndicate.com>
  * Date: 04.10.13
  * Time: 10:30
  */
 public class SimulationPanel extends JPanel {
 
     /**
      * Holds the informaticon if the simulation is running
      */
     protected boolean isRunning = false;
     protected boolean showGraph = true;
 
     protected CWorld oWorld = null;
 
     public SimulationPanel() {
         super();
 
         // We want to positionate our Elements with x und y coordinates
         setLayout(null);
 
         // set the Panel size to the Window size
         this.setBounds(0,0,Application.INSTANCE.getWidth(), Application.INSTANCE.getHeight());
     }
 
 
     /**
      * creates a random world
      */
     public void setupDummyWorld() {
         this.isRunning = false;
 
         this.oWorld = new CWorld();
 
         this.oWorld.addWalker(new CWalker(new CPosition(20, 20), new CPosition(520, 300)));
         //this.oWorld.addWalker(new CWalker(new CPosition(20, 300), new CPosition(520, 20)));
 
         this.oWorld.loadConfig();
 
         this.oWorld.buildGraph();
 
         this.isRunning = true;
 
     }
 
     /**
      * paintComponent is called, every time the window gets drawed by the application
      *
      * @param g contains the reference to the Java2D graphics object
      */
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         doDrawing(g);
     }
 
     /**
      * doDrawing does the actual drawing
      * @param g contains the reference to the Java2D graphics object
      */
     private void doDrawing(Graphics g) {
 
         Graphics2D g2d = (Graphics2D) g;
 
         //Customize the main world
         g2d.setBackground(Color.BLACK);
         g2d.fillRect(getX(), getY(), getWidth(), getHeight());
 
         //Customize the obstacles
         g2d.setColor(Color.WHITE);
 
         if(oWorld == null) {
             g2d.drawString("Passenger Simulation - please load a world or Press F2 for Dummyworld", 200, 50);
         }
         else {
            for(CObstacle obstacle : oWorld.getObstacles()) {
                Vector<CPosition> positions = obstacle.getPositions();
                int[] xCoordinates = new int[positions.size()];
                int[] yCoordinates = new int[positions.size()];
 
                int i = 0;
                for(CPosition position : positions) {
                    xCoordinates[i] = position.getX().intValue();
                    yCoordinates[i] = position.getY().intValue();
                    i += 1;
                }
 
                g2d.fillPolygon(xCoordinates, yCoordinates, positions.size());
            }
 //               Vector<CPosition> positions2 = obstacle.getWaypoints();
 //               int[] xCoordinates2 = new int[positions.size()];
 //               int[] yCoordinates2 = new int[positions.size()];
 //
 //               int j = 0;
 //               for(CPosition position : positions2) {
 //                   xCoordinates2[j] = position.getX().intValue();
 //                   yCoordinates2[j] = position.getY().intValue();
 //                   j += 1;
 //               }
 //
 //               g.setColor(Color.GREEN);
 //               g.drawPolygon(xCoordinates2, yCoordinates2, positions2.size());
 
              if(this.showGraph) {
                  g2d.setColor(Color.GREEN);
                  for(CEdge edge : this.oWorld.getGraph().getEdges()) {
                      g2d.drawLine(edge.getSource().getX().intValue(), edge.getSource().getY().intValue(), edge.getDestination().getX().intValue(), edge.getDestination().getY().intValue());
                      //this.drawArrowLine(g2d, edge.getSource().getX().intValue(), edge.getSource().getY().intValue(), edge.getDestination().getX().intValue(), edge.getDestination().getY().intValue());
                  }
 
 //                 g2d.setColor(Color.RED);
 //                 for(CEdge edge : this.oWorld.getGraph().getTrashEdges()) {
 //                     g2d.drawLine(edge.getSource().getX().intValue(), edge.getSource().getY().intValue(), edge.getDestination().getX().intValue(), edge.getDestination().getY().intValue());
 //                     this.drawArrowLine((Graphics2D)g, edge.getSource().getX().intValue(), edge.getSource().getY().intValue(), edge.getDestination().getX().intValue(), edge.getDestination().getY().intValue());
 //                 }
              }
 
 
             for(CWalker walker : oWorld.getWalkers()) {
                 CPosition position = walker.getPosition();
 
                 g2d.setColor(Color.ORANGE);
                 g2d.fillOval(((Double)(position.getX() - walker.getSize())).intValue(),
                              ((Double)(position.getY() - walker.getSize())).intValue(),
                              ((Double)(walker.getSize() * 2)).intValue(),
                              ((Double)(walker.getSize() * 2)).intValue());
 
 
                 // draw the target as x, because the X marks the point =)
                 int upperleftX = ((Double)(walker.getTarget().getX() - walker.getSize())).intValue();
                 int upperleftY = ((Double)(walker.getTarget().getY() - walker.getSize())).intValue();
                 int width = ((Double)(walker.getSize() * 2)).intValue();
                 int height = ((Double)(walker.getSize() * 2)).intValue();
                 g.drawLine(upperleftX, upperleftY, upperleftX + width, upperleftY + height);
                 g.drawLine(upperleftX + width, upperleftY, upperleftX, upperleftY + height);
 
                 DecimalFormat df = new DecimalFormat("#.00");
 
                 g2d.setColor(Color.CYAN);
                 g.drawString("x" + df.format(walker.getPosition().getX()) + "/y" + df.format(walker.getPosition().getY()), position.getX().intValue() + width + 10, position.getY().intValue() );
 
                if(walker.getDesiredPath().size() > 0 ) {
                    g.drawString("x" + df.format(walker.getDesiredPath().getFirst().getX()) + "/y" + df.format(walker.getDesiredPath().getFirst().getY()), ((Double)(walker.getDesiredPath().getFirst().getX() + 100.0)).intValue(), walker.getDesiredPath().getFirst().getY().intValue());
                }
             }
         }
     }
 
     private void drawArrowLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
 
         AffineTransform tx = new AffineTransform();
         Line2D.Double line = new Line2D.Double(x1,y1,x2,y2);
 
         Polygon arrowHead = new Polygon();
         arrowHead.addPoint( 0,5);
         arrowHead.addPoint( -5, -5);
         arrowHead.addPoint( 5,-5);
 
         tx.setToIdentity();
         double angle = Math.atan2(line.y2-line.y1, line.x2-line.x1);
         tx.translate(line.x2, line.y2);
         tx.rotate((angle-Math.PI/2d));
 
         Graphics2D g = (Graphics2D) g2d.create();
         g.setTransform(tx);
         g.fill(arrowHead);
         g.dispose();
         //g.drawLine(x1,y1,x2,y2);
     }
 
     /**
      * the simulation loop itself
      */
     public void runSimulationLoop() {
         do {
             // redraw window
             this.repaint();
 
             // sleep 5 milliseconds
             try	{
                 java.lang.Thread.sleep(20);
             } catch (InterruptedException e) {
 
             }
 
             // when the simulation is running....
             if(this.isRunning ) {
                   this.oWorld.stepSimulation();
 
             }
         } while (true);
     }
 
     public void runOneStep() {
         this.oWorld.stepSimulation();
     }
 
     public void toggleRunningState() {
         this.isRunning = !this.isRunning;
     }
 
     public void toggleShowGraph() {
         this.showGraph = !this.showGraph;
     }
 }
