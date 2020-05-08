 package simulator.viewport;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Stroke;
 import java.awt.geom.Arc2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import mapping.MapGraph;
 import mapping.Tile;
 import simulator.pilot.AbstractPilot;
 import simulator.pilot.PilotInterface;
 
 @SuppressWarnings("serial")
 public class UnitViewPort extends DummyViewPort {
 
     private final Set<AbstractPilot> pilots;
     private List<Point> pathCoordinates;
     private final Arc2D sonarArc = new Arc2D.Double();
 
     public UnitViewPort(final Set<AbstractPilot> pilotSet, Color[] teamColors) {
         super(pilotSet, teamColors);
         pilots = new HashSet<AbstractPilot>(pilotSet);
         resetPath();
     }
 
     private void addPathPoint(final double x, final double y) {
         final Point point = new Point();
         point.setLocation(x, y);
         pathCoordinates.add(point);
         repaint();
     }
 
     public void resetPath() {
         pathCoordinates = new ArrayList<Point>();
         for (final PilotInterface pilot : pilots)
             addPathPoint(pilot.getPosition().getX(), pilot.getPosition().getY());
     }
     
     @Override
     protected void paintComponent(final Graphics graph) {
         super.paintComponent(graph);
         paintPathComponent(graph);
         paintBeamComponent(graph);
         paintExploreQueue(graph);
     }
     
     private void paintPathComponent(final Graphics graph) {
         for (final PilotInterface pilot : pilots)
             if (pilot.getPosition().getX() != pathCoordinates.get(pathCoordinates.size() - 1).getX()
                     || pilot.getPosition().getY() != pathCoordinates.get(pathCoordinates.size() - 1).getY())
                 addPathPoint(pilot.getPosition().getX(), pilot.getPosition().getY());
         final Graphics2D g2 = ((Graphics2D) graph);
         g2.setColor(Color.RED);
         final Stroke originalStroke = g2.getStroke();
         g2.setStroke(new BasicStroke(3));
         for (int i = 0; i < pathCoordinates.size() - 1; i++)
             g2.draw(new Line2D.Double(pathCoordinates.get(i), pathCoordinates.get(i + 1)));
         g2.setStroke(originalStroke);
     }
 
     private void paintBeamComponent(final Graphics graph) {
         for (AbstractPilot pilot : pilots) {
        	double correctedUSDistance = pilot.getUltraSensorValue() - 5.5;
             sonarArc.setArc(pilot.getPosition().getX() - correctedUSDistance, pilot.getPosition().getY() - correctedUSDistance,
             		2 * correctedUSDistance, 2 * correctedUSDistance, 360 - pilot.getAngle() - 15, 30, Arc2D.PIE);
             ((Graphics2D) graph).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
             if (pilot.getUltraSensorValue() > 200 || pilot.getUltraSensorValue() < 20)
                 graph.setColor(new Color(133, 211, 249)); //Light blue when not trusted (< 20 || > 200)
             else
                 graph.setColor(new Color(12, 24, 244)); //Dark blue when trusted (>= 20 && <= 200)
             ((Graphics2D) graph).fill(sonarArc);
         }
     }
 
     private void paintExploreQueue(final Graphics graph) {
     	try {
         	((Graphics2D) graph).setColor(Color.ORANGE);
         	for (MapGraph mapGraph : getAllMapGraphs())
                 for (Tile tile : mapGraph.getTiles()) {
                 	Rectangle2D checkHighlight = new Rectangle2D.Double(tile.getPosition().getX() * 40, tile.getPosition().getY() * 40,
                 														(int) getSizeTile(), (int) getSizeTile());
                 	((Graphics2D) graph).fill(checkHighlight);
                 }
     	} catch(java.util.ConcurrentModificationException e) {
     		paintExploreQueue(graph);
     	}
     }
 }
