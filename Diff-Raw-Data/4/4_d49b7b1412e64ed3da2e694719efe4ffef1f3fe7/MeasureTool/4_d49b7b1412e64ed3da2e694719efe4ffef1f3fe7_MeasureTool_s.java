 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.tool;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.ui.zone.ZoneOverlay;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.walker.ZoneWalker;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.Path;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.util.GraphicsUtil;
 
 
 /**
  */
 public class MeasureTool extends DefaultTool implements ZoneOverlay {
 
 	private ZoneWalker walker;
 	private Path<ZonePoint> gridlessPath;
 	
 	public MeasureTool () {
         try {
             setIcon(new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/Tool_Measure.gif")));
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
     
     @Override
     public String getTooltip() {
         return "Measure the distance along a path";
     }
 
     @Override
     public String getInstructions() {
     	return "tool.measure.instructions";
     }
     
     public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
         
         if (walker == null && gridlessPath == null) {
             return;
         }
 
         if (walker != null) {
 	        renderer.renderPath(g, walker.getPath(), 1, 1);
 	
 	        ScreenPoint sp = walker.getLastPoint().convertToScreen(renderer);
 	        
 	        int y = sp.y - 10;
 	        int x = sp.x + (int)(renderer.getScaledGridSize()/2);
 	        GraphicsUtil.drawBoxedString(g, Integer.toString(walker.getDistance()), x, y);
         } else {
         	Object oldAA = SwingUtil.useAntiAliasing(g);
             g.setColor(Color.black);
             ScreenPoint lastPoint = null;
             for (ZonePoint zp : gridlessPath.getCellPath()) {
             	if (lastPoint == null) {
             		lastPoint = ScreenPoint.fromZonePoint(renderer, zp);
             		continue;
             	}
                 ScreenPoint nextPoint = ScreenPoint.fromZonePoint(renderer, zp.x, zp.y);
                 g.drawLine(lastPoint.x, lastPoint.y , nextPoint.x, nextPoint.y);
                 lastPoint = nextPoint;
             }
             
             // distance
     		double c = 0;
     		ZonePoint lastZP = null;
             for (ZonePoint zp : gridlessPath.getCellPath()) {
 
             	if (lastZP == null) {
             		lastZP = zp;
             		continue;
             	}
             	
             	int a = lastZP.x - zp.x;
             	int b = lastZP.y - zp.y;
 
             	c += Math.sqrt(a*a + b*b)/renderer.getZone().getUnitsPerCell();
             	
             	lastZP = zp;
             }
             
 //    		int a = lastPoint.x - (set.offsetX + token.getX());
 //    		int b = lastPoint.y - (set.offsetY + token.getY());
 //
 //            c +=  Math.sqrt(a*a + b*b)/zone.getUnitsPerCell();
             
     		String distance = String.format("%.1f", c);
	        GraphicsUtil.drawBoxedString(g, distance, lastZP.x, lastZP.y - 20);
            
             
             SwingUtil.restoreAntiAliasing(g, oldAA);
         }
     }
     
     @Override
     protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
     	super.installKeystrokes(actionMap);
     	
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if (walker == null && gridlessPath == null) {
 					return;
 				}
 				
 				// Waypoint
 				if (walker != null) {
 			        CellPoint cp = renderer.getZone().getGrid().convert(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
 			        walker.toggleWaypoint(cp);
 				} else {
 					gridlessPath.addWayPoint(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
 					gridlessPath.addPathCell(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
 				}
 			}
 		});
     }
     
     ////
     // MOUSE LISTENER
 	@Override
 	public void mousePressed(java.awt.event.MouseEvent e){
 
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
         
         
 		if (SwingUtilities.isLeftMouseButton(e)) {
 			if (renderer.getZone().getGrid().getCapabilities().isPathingSupported()) {
 		        CellPoint cellPoint = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
 				walker = renderer.getZone().getGrid().createZoneWalker();
 	            walker.addWaypoints(cellPoint, cellPoint);
 			} else {
 				gridlessPath = new Path<ZonePoint>();
 				gridlessPath.addPathCell(new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer));
 				
 				// Add a second one that will be replaced as the mouse moves around the screen
 				gridlessPath.addPathCell(new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer));
 			}
 			
             renderer.repaint();
             return;
 		} 
         
         super.mousePressed(e);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 
         ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 
 		if (SwingUtilities.isLeftMouseButton(e)) {
 		
 			walker = null;
 			gridlessPath = null;
 			
 			renderer.repaint();
             return;
 		}
         
         super.mouseReleased(e);
 	}
 	
     ////
     // MOUSE MOTION LISTENER
 	@Override
     public void mouseDragged(MouseEvent e){
 
         if (SwingUtilities.isRightMouseButton(e)) {
             super.mouseDragged(e);
             return;
         }
 
         ZoneRenderer renderer = (ZoneRenderer) e.getSource();
         if (renderer.getZone().getGrid().getCapabilities().isPathingSupported()) {
 	        CellPoint cellPoint = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
 	        walker.replaceLastWaypoint(cellPoint);
         }  else {
         	gridlessPath.replaceLastPoint(new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer));
         }
         
         renderer.repaint();
     }
 }
