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
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.Transparency;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import net.rptools.clientserver.hessian.client.ClientConnection;
 import net.rptools.maptool.client.swing.SwingUtil;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.util.FileUtil;
 
 
 /**
  */
 public class ZoneSelectionPanel extends JPanel implements DropTargetListener  {
 
     private static final int MAX_THUMB_WIDTH = 70;
     private static final int PADDING = 5;
     
     private Map<Rectangle, ZoneRenderer> boundsMap;
     private BufferedImage backBuffer;
     private int lastZoneCount = -1;
     
     public ZoneSelectionPanel() {
      
         boundsMap = new HashMap<Rectangle, ZoneRenderer>();
         setOpaque(false);
         
         // DnD
         new DropTarget(this, this);
 
         addMouseListener(new MouseAdapter(){
            
             /* (non-Javadoc)
              * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
              */
             public void mouseClicked(MouseEvent e) {
                 
                 ZoneRenderer renderer = getRendererAt(e.getX(), e.getY()); 
                 if (renderer != null) {
                     
                     MapToolClient.setCurrentZoneRenderer(renderer);
                 }
             }
             
         });
     }
     
     /* (non-Javadoc)
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      */
     protected void paintComponent(Graphics g) {
         
         Dimension mySize = getSize();
         List<ZoneRenderer> rendererList = MapToolClient.getZoneRenderers();
 
         if (backBuffer == null || rendererList.size() != lastZoneCount ||
         		backBuffer.getWidth() != mySize.width ||
         		backBuffer.getHeight() != mySize.height) {
         	
         	lastZoneCount = rendererList.size();
        	System.out.println ("Rendering zone selector: " + lastZoneCount);
 	        if (backBuffer == null) {
 	        	backBuffer = getGraphicsConfiguration().createCompatibleImage(mySize.width, mySize.height, Transparency.TRANSLUCENT);
 	        }
 	        
 	        Graphics backG = null;
 	        try {
 	        	backG = backBuffer.getGraphics();
 	        	
 		        // Background
 	        	backG.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
 	        	backG.fillRect(0, 0, mySize.width, mySize.height);
 	        	backG.setColor(Color.black);
 	        	backG.drawRect(1, 1, mySize.width-2, mySize.height-2);
 	        	backG.setColor(Color.white);
 	        	backG.drawRect(0, 0, mySize.width-2, mySize.height-2);
 		        
 		        
 		        boundsMap.clear();
 		        int x = PADDING;
 		        for (ZoneRenderer renderer : rendererList) {
 		            
 		            // TODO: This is a naive solution.  In the future, actually render the zone
 		            BufferedImage img = renderer.getBackgroundImage();
 		            if (img == null) {
		            	// Force a redraw later
		            	lastZoneCount = -1;
 		                continue;
 		            }
 		            
 		            int width = img.getWidth();
 		            int height = img.getHeight();
 		
 		            int targetHeight = mySize.height - PADDING - PADDING;
 		            
 		            width = (int)(width * (targetHeight / (double)height));
 		            height = targetHeight;
 		
 		            // TODO: handle "still too wide" case
 		            
 		            backG.drawImage(img, x, PADDING, width, height, this);
 		            backG.setColor(Color.black);
 		            backG.drawRect(x, PADDING, width, height);
 		            
 		            boundsMap.put(new Rectangle(x, PADDING, width, height), renderer);
 		            
 		            x += width + PADDING;
 		        } 
 		        
 	        } finally {
 	        	if (backG != null) {
 	        		backG.dispose();
 	        	}
 	        }
         }
         
     	g.drawImage(backBuffer, 0, 0, this);
     }
 
     public ZoneRenderer getRendererAt(int x, int y) {
         
         for (Rectangle rect : boundsMap.keySet()) {
             if (rect.contains(x, y)) {
                 return boundsMap.get(rect);
             }
         }
         return null;
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
         Transferable transferable = dtde.getTransferable();
 
         // TODO: Consolidate all of this crap.
         try {
 	        // EXISTING ASSET
 	        if (transferable.isDataFlavorSupported(TransferableAsset.dataFlavor) ||
 	        		transferable.isDataFlavorSupported(TransferableAssetReference.dataFlavor)) {
 	
 	        	dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
 
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
 	        	
 	            MapToolClient.addZone(asset.getId());
 	            dtde.dropComplete(true);
 	            return;
 	        
 	        }
 	        
 	        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
 	        	
 	        	dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
 	        	List<File> list = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
 	        	
 	        	if (list.size() == 0) {
 	        		return;
 	        	}
 	        	
 	        	// For some reason, firefox does not actually write out the temporary file designated in
 	        	// this list until list line is called.  So it has to stay ABOVE the loadFile() call
 	        	// It also requires just a moment to copy from internal system whatever into the file
 	            dtde.dropComplete(true);
 	            try {
 	            	Thread.sleep(1000);
 	            } catch (Exception e) {
 	            	e.printStackTrace();
 	            }
 
 	            // We only support using one at a time for now
         		Asset asset = new Asset(FileUtil.loadFile(list.get(0)));
         		MapToolClient.getCampaign().putAsset(asset);
                 if (MapToolClient.isConnected()) {
                 	
                 	// TODO: abstract this
                     ClientConnection conn = MapToolClient.getInstance().getConnection();
                     
                     conn.callMethod(MapToolClient.COMMANDS.putAsset.name(), asset);
                 }
 
 	            MapToolClient.addZone(asset.getId());
 	            return;
 	        }	        	
             dtde.dropComplete(false);
 	
         } catch (IOException ioe) {
             ioe.printStackTrace();
         } catch (UnsupportedFlavorException ufe) {
             ufe.printStackTrace();
         } catch (Exception e) {
         	e.printStackTrace();
         }
 
         repaint();
 
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
      */
     public void dropActionChanged(DropTargetDragEvent dtde) {
         // TODO Auto-generated method stub
 
     }
 }
