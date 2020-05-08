 package view;
 
 import hierarchicalcontrol.Behavior;
 import hierarchicalcontrol.BehavioralControl;
 import hierarchicalcontrol.Connection;
 import hierarchicalcontrol.Connection.TYPES;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.JPanel;
 
 /**
  *
  * @author sjoerdlagarde
  */
 public class StructurePanel extends JPanel implements Observer {
     private static final long serialVersionUID = 1L;
     
     private BehavioralControl behaviors;
     private ArrayList<Behavior> layers;
     
     private final int windowWidth = 500;
     private final int windowHeight = 400;
     
     private final int layerHeight = 50;
     private final int layerWidth = 150;
     private final int layerSpacing = 25;
     private final int layerStartY = layerHeight+layerSpacing;
     private int xLayer;         // x-pos of layers
     
     public StructurePanel(BehavioralControl behaviors) {
         this.setLayout(new GridLayout(1, 1));
         this.setPreferredSize(new Dimension(windowWidth, windowHeight));
         this.setBackground(Color.white);
         
         this.behaviors = behaviors;
     }
     
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         drawLegend(g);
         
         g.translate(20, 75);
         layers = behaviors.getLayers();
         drawLayers(g);
         drawConnections(g);
     }
     
     /**
      * Draws the layers of the hierarchical system
      * @param g The graphics context
      */
     private void drawLayers(Graphics g) {
         int bottomY = (layers.size()-1)*layerStartY+layerHeight;
         
         // Draw input links
         g.drawString("Inputs", 5, bottomY-10);
         g.drawLine(0, bottomY, 75, bottomY);
         g.drawLine(75, bottomY, 75, 0+layerHeight/2);
         xLayer = 100;
         
         // Draw layers
         for ( int i=layers.size()-1, j=0; i>=0; i--, j++ ) {
             Behavior behavior = layers.get(i);
             g.drawLine(75, j*layerStartY+layerHeight/2, xLayer, j*layerStartY+layerHeight/2);
             g.drawRect(xLayer, j*layerStartY, layerWidth, layerHeight);
             if ( behavior.getOutput() != null && !behavior.isInhibited() )      // Draw active layers
                 g.drawRect(xLayer+1, j*layerStartY+1, layerWidth-2, layerHeight-2); 
             g.drawString(layers.get(i).getId(), xLayer+20, j*layerStartY+30);
         }
     }
     
     /**
      * Draws the connections of the hierarchical system
      * @param g The graphics context
      */
     private void drawConnections(Graphics g) {
         int bottomY = (layers.size()-1)*layerStartY+layerHeight;        
         
         // Draw output link from level 0
         g.drawString("Ouput", 400, bottomY-10);
         int xOutput = xLayer+layerWidth+125;        // Line at same height as 'Inputs'
         g.drawLine(xLayer+layerWidth, bottomY-layerHeight/2, xOutput, bottomY-layerHeight/2);
         g.drawLine(xOutput, bottomY-layerHeight/2, xOutput, bottomY);
         g.drawLine(xOutput, bottomY, 450, bottomY);
         
         // Draw connections
         int xLayerEnd = xLayer+layerWidth;      // Defines where to start drawing horizontal output lines
         int[] numberConnections = new int[layers.size()]; 
         for ( int x=0; x<numberConnections.length; x++ )
             numberConnections[x] = 0;
         
         // Loop over all layers except for layer 0
         for ( int i=layers.size()-1, j=0; i>0; i--, j++ ) {
             ArrayList<Connection> connections = layers.get(i).getConnections();
             int offset = j*15;
             
             // Draw horizontal output line. Length of the line is based on 2 factors: 
             //  1) number of connections going out of this layer
             //  2) number of connections coming into this layer
             int sizeX = xLayerEnd+25*connections.size()+offset;
             if ( numberConnections[i]>connections.size() )  // Drawing last connection 
                 sizeX = xLayerEnd+25*numberConnections[i]+offset;
            g.drawLine(xLayerEnd, j*layerStartY+layerHeight/2, sizeX, j*layerStartY+layerHeight/2);     
            int diffX = (sizeX-xLayerEnd)/connections.size();   // Calculates how far apart connections should be drawn
             
             // Draw actual connections
             for ( int k=connections.size()-1; k>=0; k-- ) {  
                 Connection connection = connections.get(k);
                 int toLayer = getLayer(connection.getConnectedTo());
                 numberConnections[toLayer] = ( numberConnections[toLayer] < k+1 ) ? k+1 : numberConnections[toLayer];
 
                 // Connections
                 int posX = xLayerEnd+diffX*(k+1);
                 int posFromY = j*layerStartY+layerHeight/2;
                 int posToY = (layers.size()-1-toLayer)*layerStartY+layerHeight/2;
                 if ( k==connections.size()-1 )      // Account for rounding errors
                     posX = sizeX;
                 g.drawLine(posX, posFromY, posX, posToY); 
  
                 // Type of connections
                 if ( TYPES.INHIBIT.equals(connections.get(k).getType()) ) 
                     g.fillOval(posX-3, posToY-3, 5, 5);
                 else if ( TYPES.OVERRIDE.equals(connections.get(k).getType()) ) {
                     g.setColor(Color.white);
                     g.fillOval(posX-3, posToY-3, 5, 5);
                     g.setColor(Color.black);
                     g.drawOval(posX-3, posToY-3, 5, 5);
                 } else if ( TYPES.MONITOR.equals(connections.get(k).getType()) ) {
                     g.drawLine(posX, posFromY, posX-3, posFromY+3);
                     g.drawLine(posX, posFromY, posX+3, posFromY+3);
                 }
             }
         }
     }
     
     /**
      * Draws a legend
      * @param g The graphics context
      */
     private void drawLegend(Graphics g) {
         g.fillOval(15, windowHeight-27, 5, 5);
         g.drawString("Inhibit", 30, windowHeight-20);
         
         g.drawOval(95, windowHeight-27, 5, 5);
         g.drawString("Override", 110, windowHeight-20);
         
         g.drawLine(187, windowHeight-20, 187, windowHeight-30);
         g.drawLine(187, windowHeight-30, 184, windowHeight-27);
         g.drawLine(187, windowHeight-30, 190, windowHeight-27);
         g.drawString("Monitor", 200, windowHeight-20);
     }
     
     private int getLayer(Behavior behavior) {
         for ( int i=0; i<layers.size(); i++ ) {
             if ( layers.get(i).getId().equals(behavior.getId()) )
                 return i;
         }
          
         return 0;
     }
 
     @Override
     public void update(Observable o, Object arg) {
         repaint();
     }
     
 }
