 package de.dhbw.swe.camping_site_mgt.gui_mgt.map_mgt.map_info.view;
 
 import java.awt.*;
 
 import javax.swing.JPanel;
 
 import de.dhbw.swe.camping_site_mgt.gui_mgt.Gui;
 import de.dhbw.swe.camping_site_mgt.gui_mgt.map_mgt.map.view.MapPanelInterface;
 
 public class ShapePanel extends JPanel {
     /** The serial version UID. */
     private static final long serialVersionUID = 1L;
 
     @Override
     public void paint(final Graphics g) {
 	super.paint(g);
 	if (polygon == null) {
 	    return;
 	}
 	final Graphics2D g2 = (Graphics2D) g;
 
 	g2.setColor(Color.GRAY);
 	if (nature != null) {
 	    g2.setColor(nature);
 	}
 	g2.fill(polygon);
 
 	g2.setColor(Color.BLACK);
 	g2.draw(polygon);
 
 	g2.setColor(Color.BLACK);
 	final Font defaultFont = g2.getFont();
 	g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
 	final Rectangle bounds = polygon.getBounds();
 	final int stringLength = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
 	final int stringHeight = (int) g2.getFontMetrics().getStringBounds(text, g2).getHeight();
 	final int xStart = (bounds.width - stringLength) / 2;
 	final int yStart = (bounds.height + stringHeight) / 2;
 	g2.drawString(text, xStart, yStart);
 	g2.setFont(defaultFont);
     }
 
     public void setLabel(final String text) {
 	this.text = text;
     }
 
     public void setNature(final Color nature) {
 	this.nature = nature;
     }
 
     public void setPolygon(final Polygon polygon) {
 	final int mapInfoComponentWidth = (int) ((1f - MapPanelInterface.MAP_SCREEN_COVERAGE) * Gui.screenSize.width);
 
 	final Rectangle bounds = polygon.getBounds();
 	final int multiplicator = (mapInfoComponentWidth - 16) / bounds.width;
	setPreferredSize(new Dimension(bounds.width * multiplicator + 1,
 		bounds.height * multiplicator + 3));
 	final int[] xPoints = polygon.xpoints;
 	final int[] yPoints = polygon.ypoints;
 	for (int i = 0; i < xPoints.length; i++) {
 	    xPoints[i] = xPoints[i] * multiplicator;
 	    yPoints[i] = yPoints[i] * multiplicator;
 	}
 
 	this.polygon = new Polygon(xPoints, yPoints, xPoints.length);
 	repaint();
     }
 
     /** The {@link Color} of the nature of soil. */
     private Color nature = null;
 
     private Polygon polygon = null;
 
     private String text = "";
 }
