 package de.bwv_aachen.dijkstra.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import de.bwv_aachen.dijkstra.controller.Controller;
 import de.bwv_aachen.dijkstra.model.Airport;
 
 public class ConnectionVisualization extends View {
 
     private static final long serialVersionUID = 1527659470763038523L;
     
     private BufferedImage airportPicture;
     private final int    panelWidth   = 660;
     private final int    panelHeight  = 660;
     //private final double nodeRadius   = 22D;
     //private final double nodeDiameter = nodeRadius * 2;
     private int picCenterX;
     private int picCenterY;
     
     private final double circleRadius = 200D;
     
     public ConnectionVisualization(Controller c) {
         super("ConnectionVisualization",c);
         try {
             //Let the controller do this?
             airportPicture = ImageIO.read(new File("res/airport-icon_2.gif"));
         }
         catch (IOException e) {
             //TODO
             System.out.println("Bild nicht gefunden");
         }
         
         picCenterX = airportPicture.getWidth()/2;
         picCenterY = airportPicture.getHeight()/2;
     }
     
     class VisualizationPanel extends JPanel {
         private static final long serialVersionUID = -3979908130673351633L;
 
         @Override
         public void paintComponent( Graphics g ) {
             //TODO: Replace with a Panel with an ImageIcon and a JLabel per Airport ???
             
             super.paintComponent( g ); // call superclass's paintComponent 
             
             setSize(panelWidth,panelHeight);
             Graphics2D g2d = ( Graphics2D ) g; // cast g to Graphics2D
            
            //Parameters for g2d
             g2d.setPaint( Color.BLACK);
            g2d.setFont(new Font("sans-serif", Font.BOLD, 14));
             
             Object[] airports = controller.getModel().getAirportList().values().toArray();
             
             int numOfNodes = airports.length;
             if (numOfNodes == 0)
                     return;
             
             final int  centerX          = panelWidth/2;
             final int  centerY          = panelHeight/2;
             
             final double angle          = Math.toRadians( 360D / numOfNodes );
 
             for (int i = 0; i < numOfNodes; i++) {
                 double alpha = angle * i;
 
                 double x = centerX + Math.sin(alpha) * circleRadius;
                 double y = centerY - Math.cos(alpha) * circleRadius;
                 
                 //g2d.draw( new Ellipse2D.Double( x-nodeRadius , y-nodeRadius, nodeDiameter, nodeDiameter ) );
                 
                 g2d.drawImage(airportPicture, (int)(x-picCenterX), (int)(y-picCenterY), null);
                 g2d.drawString(airports[i].toString(), (int)(x), (int)(y));
             }       
         }
     }
 
     @Override
     public void draw() {
         this.getContentPane().add(new VisualizationPanel());
         this.pack();
         this.setSize(panelWidth,panelHeight);
         this.setVisible(true);
     }
 
 }
