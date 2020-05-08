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
 package net.rptools.maptool.client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Transparency;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JComponent;
 
 import net.rptools.clientserver.hessian.client.ClientConnection;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.drawing.DrawnElement;
 import net.rptools.maptool.server.MapToolServer;
 import net.rptools.maptool.util.ImageManager;
 
 
 /**
  */
 public class ZoneRenderer extends JComponent implements DropTargetListener, MouseWheelListener {
     private static final long serialVersionUID = 3832897780066104884L;
 
     private Zone              zone;
 
     private BufferedImage     image;
 
     private int               offsetX;
     private int               offsetY;
 
     private int               width;
     private int               height;
 
     private boolean           showGrid;
     private Color             gridColor = new Color (150, 150, 150);
 
     private int               scaleIndex  = 3;
     private float[]           scaleArray  = new float[] { .25F, .50F, .75F, 1F, 1.25F, 1.5F, 1.75F, 2F, 4F};
 
     private List<ZoneOverlay> overlayList = new ArrayList<ZoneOverlay>();
     private Map<Rectangle, Token> tokenBoundsMap = new HashMap<Rectangle, Token>();
     private Set<Token> selectedTokenSet = new HashSet<Token>();
 
     private BufferedImage drawableOverlay;
     
 	private boolean isMouseWheelEnabled = true;
     
     // This is a workaround to identify when the zone has had a new
     // drawnelement added.  Not super fond of this.  Rethink it later
     private int drawnElementCount = -1;
     
     public ZoneRenderer(Zone zone) {
         if (zone == null) { throw new IllegalArgumentException("Zone cannot be null"); }
 
         this.zone = zone;
 
         // DnD
         new DropTarget(this, this);
 
         // Default wheel action
         addMouseWheelListener(this);
         
         // Get focus when clicked in
         addMouseListener(new MouseAdapter(){
 			public void mousePressed(MouseEvent e) {
 				requestFocus();
 			}
         });
     }
 
     public Zone getZone() {
     	return zone;
     }
     
     public void addOverlay(ZoneOverlay overlay) {
         overlayList.add(overlay);
     }
 
     public void removeOverlay(ZoneOverlay overlay) {
         overlayList.remove(overlay);
     }
 
     public void toggleGrid() {
         showGrid = !showGrid;
         
         repaint();
     }
     
     public void setGridVisible(boolean visible) {
         showGrid = visible;
 
         repaint();
     }
 
     public void setMouseWheelEnabled(boolean enabled) {
     	isMouseWheelEnabled = enabled;
     }
     
     /* (non-Javadoc)
 	 * @see javax.swing.JComponent#isRequestFocusEnabled()
 	 */
 	public boolean isRequestFocusEnabled() {
 		return true;
 	}
     
     public void moveGridBy(int dx, int dy) {
 
     	int gridOffsetX = zone.getGridOffsetX();
     	int gridOffsetY = zone.getGridOffsetY();
     	int gridSize = zone.getGridSize();
     	
         gridOffsetX += dx;
         gridOffsetY += dy;
 
         gridOffsetX %= gridSize;
         gridOffsetY %= gridSize;
 
         if (gridOffsetY > 0) {
             gridOffsetY = gridOffsetY - gridSize;
         }
         
         if (gridOffsetX > 0) {
             gridOffsetX = gridOffsetX - gridSize;
         }
 
         zone.setGridOffsetX(gridOffsetX);
         zone.setGridOffsetY(gridOffsetY);
         
         repaint();
     }
 
     public void adjustGridSize(int delta) {
         zone.setGridSize(Math.max(0, zone.getGridSize() + delta));
 
         repaint();
     }
 
     public void moveViewBy(int dx, int dy) {
 
         offsetX += dx;
         offsetY += dy;
 
         repaint();
     }
 
     public void zoomIn(int x, int y) {
         zoomTo(x, y, scaleIndex - 1);
     }
 
     public void zoomOut(int x, int y) {
         zoomTo(x, y, scaleIndex + 1);
     }
 
     private void zoomTo(int x, int y, int index) {
 
         index = Math.max(index, 0);
         index = Math.min(index, scaleArray.length - 1);
 
         double oldScale = scaleArray[scaleIndex];
         double newScale = scaleArray[index];
 
         scaleIndex = index;
 
         // Keep the current pixel centered
         x -= offsetX;
         y -= offsetY;
 
         int newX = (int) ((x * newScale) / oldScale);
         int newY = (int) ((y * newScale) / oldScale);
 
         offsetX -= newX - x;
         offsetY -= newY - y;
 
         repaint();
     }
     
     public BufferedImage getDrawableOverlay() {
     	
     	if (image == null) { return null; }
     	if (zone == null) { return null; }
     	
     	List<DrawnElement> drawnElements = zone.getDrawnElements();
     	if (drawableOverlay != null && drawnElements.size() == drawnElementCount) {
     		return drawableOverlay;
     	}
     	
     	if (drawableOverlay == null) {
     		drawableOverlay = getGraphicsConfiguration().createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.BITMASK);
     	}
     	
 		Graphics2D g = null;
 		try {
 			g = (Graphics2D) drawableOverlay.getGraphics();
 			
 			for (DrawnElement drawnElement : drawnElements) {
 				
 				drawnElement.getDrawable().draw(g, drawnElement.getPen());
 			}
 			
 		} finally {
 			if (g != null) {
 				g.dispose();
 			}
 		}
 
     	// Don't redraw until the drawables change
     	drawnElementCount = drawnElements.size();
     	
     	return drawableOverlay;
     }
 
     public BufferedImage getBackgroundImage() {
         
         if (image != null) { return image; }
         if (zone == null) { return null; }
         
         Asset asset = MapToolClient.getCampaign().getAsset(zone.getAssetID());
         BufferedImage backgroundImage = null;
         if (asset == null) {
 
         	// TODO: abstract this into the client
         	if (MapToolClient.isConnected()) {
         		ClientConnection conn = MapToolClient.getInstance().getConnection();
         		
                 conn.callMethod(MapToolClient.COMMANDS.getAsset.name(), zone.getAssetID());
         	}
         	
             // TODO: Show a placeholder
             return null;
         } 
 
         image = ImageManager.getImage(asset);
         backgroundImage = image;
         
         width = backgroundImage.getWidth(this);
         height = backgroundImage.getHeight(this);
         
         return backgroundImage;
     }
     
     public void paintComponent(Graphics g) {
 
         Dimension size = getSize();
         g.setColor(Color.black);
         g.fillRect(0, 0, size.width, size.height);
 
         if (zone == null) { return; }
 
         Image background = getBackgroundImage();
         if (background == null) {
             return;
         }
 
     	renderBackBuffer(g);
         BufferedImage drawableLayer = getDrawableOverlay();
         if (drawableOverlay != null) {
             float scale = scaleArray[scaleIndex];
             int w = (int)(width * scale);
             int h = (int)(height * scale);
 
             g.drawImage(drawableLayer, offsetX, offsetY, w, h, this);
         }
         
         renderTokens(g);
 
         for (ZoneOverlay overlay : overlayList) {
             overlay.paintOverlay(this, (Graphics2D) g);
         }
     }
     
     private void renderBackBuffer(Graphics g) {
 
     	Dimension size = getSize();
     	
         // Scale
         float scale = scaleArray[scaleIndex];
         int w = (int)(width * scale);
         int h = (int)(height * scale);
 
         float gridSize = zone.getGridSize() * scale;
 
         if (size.width > w) {
             offsetX = (size.width - w) / 2;
         }
 
         if (size.height > h) {
             offsetY = (size.height - h) / 2;
         }
 
         // Border
         if (offsetX > 0) {
         	g.setColor(Color.black);
         	g.fillRect(0, 0, offsetX, size.height);
         }
         if (offsetY > 0) {
         	g.setColor(Color.black);
         	g.fillRect(0, 0, size.width, offsetY);
         }
         if (w + offsetX < size.width) {
         	g.setColor(Color.black);
         	g.fillRect(w + offsetX, 0, size.width, size.height);
         }
         if (h + offsetY < size.height) {
         	g.setColor(Color.black);
         	g.fillRect(0, h + offsetY, size.width, size.height);
         }
         
         // Map
         g.drawImage(image, offsetX, offsetY, w, h, this);
 
         // Render grid
         if (showGrid) {
         	g.setColor(gridColor);
 
             int x = offsetX + (int) (zone.getGridOffsetX() * scaleArray[scaleIndex]);
             int y = offsetY + (int) (zone.getGridOffsetY() * scaleArray[scaleIndex]);
 
             for (int row = 0; row < h + gridSize; row += gridSize) {
                 
                 int theY = Math.min(offsetY + h, Math.max(row + y, offsetY));
                 int theX = Math.max(x, offsetX);
                 
             	g.drawLine(theX, theY, theX + w, theY);
             }
 
             for (int col = 0; col < w + gridSize; col += gridSize) {
                 
                 int theX = Math.min(offsetX + w, Math.max(x + col, offsetX));
                 int theY = Math.max(y, offsetY);
 
                 g.drawLine(theX, theY, theX, theY + h);
             }
         }
         
 
     }
     
     protected void renderTokens(Graphics g) {
 
         int gridSize = zone.getGridSize();
         int gridOffsetX = zone.getGridOffsetX();
         int gridOffsetY = zone.getGridOffsetY();
 
         tokenBoundsMap.clear();
         for (Token token : zone.getTokens()) {
 
             // OPTIMIZE:
             Asset asset = MapToolClient.getCampaign().getAsset(token.getAssetID());
             if (asset == null) {
                 // TODO: this should be abstracted into the client better
                 if (MapToolClient.isConnected()) {
                     System.err.println("Getting asset2");
                     MapToolClient.getInstance().getConnection().callMethod(MapToolServer.COMMANDS.getAsset.name(), token.getAssetID());
                 }
                 continue;
             }
             
             Image image = ImageManager.getImage(asset);
             float scale = scaleArray[scaleIndex];
 
             int width = (int)(gridSize * scale) - 1;
             int height = (int)(gridSize * scale) - 1;
             
             int x = (int)((token.getX() * gridSize) * scale + offsetX) + (int) (gridOffsetX * scaleArray[scaleIndex]) + 1;
             int y = (int)((token.getY() * gridSize) * scale + offsetY) + (int) (gridOffsetY * scaleArray[scaleIndex]) + 1;
             
             g.drawImage(image, x, y, width, height, this);
 
             // OPTIMIZE: don't create new Rectangles each time
             Rectangle bounds = new Rectangle();
             bounds.setBounds(x, y, width, height);
             tokenBoundsMap.put(bounds, token);
 
             // Selected ?
             if (selectedTokenSet.contains(token)) {
                 g.setColor(Color.blue);
                 g.drawRect(x, y, width, height);
                 g.drawRect(x - 1, y - 1, width + 2, height + 2);
             }
         }
     }
 
     public Set<Token> getSelectedTokenSet() {
     	return selectedTokenSet;
     }
     
     public void selectToken(Token token) {
     	selectedTokenSet.add(token);
     	
     	repaint();
     }
     
     public void clearSelectedTokens() {
     	selectedTokenSet.clear();
     	
     	repaint();
     }
     
 	/**
 	 * Returns the token at screen location x, y (not cell location). To get
 	 * the token at a cell location, use getGameMap() and use that.
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public Token getTokenAt (int x, int y) {
 		
 		for (Rectangle rect : tokenBoundsMap.keySet()) {
 			if (rect.contains(x, y)) {
 				return tokenBoundsMap.get(rect);
 			}
 		}
 		
 		return null;
 	}
 
 	public Point convertScreenToZone(int x, int y) {
 		
         double scale = scaleArray[scaleIndex];
         
         // Translate
         x -= offsetX;
         y -= offsetY;
         
         // Scale
         x = (int)(x / scale);
         y = (int)(y / scale);
         
         // Bounds
         // TODO: Decide whether to allow "off zone" coordinates
         if (x < 0) { x = 0; }
         if (x >= x + width * scale) { x = (int)(width * scale) - 1; }
         
         if (y < 0) { y = 0; }
         if (y >= y + height * scale) { y = (int)(height * scale) - 1; }
         
         return new Point(x,y);
 	}
 	
     /**
      * Since the map can be scaled, this is a convenience method to find out
      * what cell is at this location
      * @param x
      * @param y
      * @return
      */
     public Point getCellAt (int x, int y) {
 
         double scale = scaleArray[scaleIndex];
         
         if (x >= offsetX 
                 && x < offsetX + image.getWidth(this) * scale 
                 && y >= offsetY && y < offsetY + image.getHeight(this) * scale) {
             
             // Translate
            x -= offsetX;
            y -= offsetY;
             
             // Scale
             x = (int)(x / (zone.getGridSize() * scale));
             y = (int)(y / (zone.getGridSize() * scale));
             
             return new Point(x, y);
         }
         
         return null;
     }
 
     public double getScale() {
     	return scaleArray[scaleIndex];
     }
     
     ////
     // DROP TARGET LISTENER
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
      */
     public void dragEnter(DropTargetDragEvent dtde) {
         // TODO Auto-generated method stub
 
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
      */
     public void dragExit(DropTargetEvent dte) {
         // TODO Auto-generated method stub
 
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
      */
     public void dragOver(DropTargetDragEvent dtde) {
         // TODO Auto-generated method stub
 
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
      */
     public void drop(DropTargetDropEvent dtde) {
         // TODO Auto-generated method stub
         Transferable transferable = dtde.getTransferable();
         if (!transferable.isDataFlavorSupported(TransferableAsset.dataFlavor) &&
         		!transferable.isDataFlavorSupported(TransferableAssetReference.dataFlavor)) {
             dtde.dropComplete(false);
             return;
         }
 
         try {
         	// TODO: This section needs to be consolidated with ZoneSelectionPanel.drop()
         	Asset asset = null;
         	if (transferable.isDataFlavorSupported(TransferableAsset.dataFlavor)) {
         		
         		// Add it to the system
         		asset = (Asset) transferable.getTransferData(TransferableAsset.dataFlavor);
         		MapToolClient.getCampaign().putAsset(asset);
                 if (MapToolClient.isConnected()) {
                 	
                 	// TODO: abstract this
                     ClientConnection conn = MapToolClient.getInstance().getConnection();
                     
                     conn.callMethod(MapToolClient.COMMANDS.putAsset.name(), asset);
                 }
         		
         	} else {
         		
         		asset = MapToolClient.getCampaign().getAsset((GUID) transferable.getTransferData(TransferableAssetReference.dataFlavor));
         	}
 
             Point p = dtde.getLocation();
             p = getCellAt((int)p.getX(), (int)p.getY());
             int x = (int)p.getX();
             int y = (int)p.getY();
 
             Token token = new Token(asset.getId());
             token.setX(x);
             token.setY(y);
 
             zone.putToken(token);
 
             // TODO: abstract this better
             if (MapToolClient.isConnected()) {
             	ClientConnection conn = MapToolClient.getInstance().getConnection();
             	
             	conn.callMethod(MapToolClient.COMMANDS.putToken.name(), zone.getId(), token);
             }
             
             repaint();
 
         } catch (IOException ioe) {
             ioe.printStackTrace();
         } catch (UnsupportedFlavorException ufe) {
             ufe.printStackTrace();
         }
 
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
      */
     public void dropActionChanged(DropTargetDragEvent dtde) {
         // TODO Auto-generated method stub
 
     }
     
 	////
 	// Mouse Wheel
 	public void mouseWheelMoved(MouseWheelEvent e) {
 
 		if (!isMouseWheelEnabled) {
 			return;
 		}
 		
 		if (e.getWheelRotation() > 0) {
 			
 			zoomOut(e.getX(), e.getY());
 		} else {
 			
 			zoomIn(e.getX(), e.getY());
 		}
 		
 	}	
 	
 }
