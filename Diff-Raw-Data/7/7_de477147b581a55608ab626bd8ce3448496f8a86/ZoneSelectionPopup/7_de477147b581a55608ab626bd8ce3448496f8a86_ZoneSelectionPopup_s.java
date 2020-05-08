 package net.rptools.maptool.client.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import java.util.ListIterator;
 
 import javax.swing.JButton;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.util.ImageManager;
 
 public class ZoneSelectionPopup extends JPopupMenu {
 
 	private static final int MAP_SIZE_WIDTH = 120;
 	private static final int MAP_SIZE_HEIGHT = 100;
 	private static final int PADDING = 3;
 	
 	public ZoneSelectionPopup() {
 		
		List<ZoneRenderer> rendererList = MapTool.getFrame().getZoneRenderers();
 		if (!MapTool.getPlayer().isGM()) {
 			for (ListIterator<ZoneRenderer> iter = rendererList.listIterator(); iter.hasNext();) {
 				ZoneRenderer renderer = iter.next();
 				if (!renderer.getZone().isVisible()) {
 					iter.remove();
 				}
 			}
 		}
 		
 		int rows = rendererList.size() >= 3 ? (int)Math.ceil(rendererList.size() / 3.0) : 1;
 		int cols = rendererList.size() >= 3 ? 3 : rendererList.size();
		System.out.println(rows + ", " + cols);
 		setLayout(new GridLayout(rows, cols));
 		
 		for (ZoneRenderer renderer : rendererList) {
 			
 			add(new SelectZoneButton(renderer));
 		}
 	}
 	
 	private class SelectZoneButton extends JButton implements ActionListener{
 		
 		private ZoneRenderer renderer;
 		
 		public SelectZoneButton(ZoneRenderer renderer) {
 			this.renderer = renderer;
 			addActionListener(this);
 		}
 
 		@Override
 		public Dimension getPreferredSize() {
 			FontMetrics fm = getFontMetrics(getFont());
 			return new Dimension(MAP_SIZE_WIDTH + PADDING * 2, MAP_SIZE_HEIGHT + fm.getHeight() + PADDING + PADDING*2);
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			
     		if (MapTool.getFrame().getCurrentZoneRenderer() != renderer) {
         		MapTool.getFrame().setCurrentZoneRenderer(renderer);
         		MapTool.getFrame().refresh();
         		
         		if (AppState.isPlayerViewLinked()) {
                 	ZonePoint zp = new ScreenPoint(renderer.getWidth()/2, renderer.getHeight()/2).convertToZone(renderer);
         			MapTool.serverCommand().enforceZone(renderer.getZone().getId());
         			MapTool.serverCommand().enforceZoneView(renderer.getZone().getId(), zp.x, zp.y, renderer.getScaleIndex());
         		}
     		}
     		
     		ZoneSelectionPopup.this.setVisible(false);
 		}
 
 		@Override
 		protected void paintComponent(Graphics g) {
 
 			Insets insets = getInsets();
 			
 			Dimension size = getSize();
 			g.setColor(renderer == MapTool.getFrame().getCurrentZoneRenderer() ? Color.lightGray : Color.white);
 			g.fillRect(0, 0, size.width, size.height);
 			
 			String name = renderer.getZone().getName();
 			if (name == null || name.length() == 0) {
 				name = "Map";
 			}
 
 			BufferedImage img = renderer.getMiniImage(MAP_SIZE_WIDTH);
 	        if (img == null || img == ImageManager.UNKNOWN_IMAGE) {
 	            img = ImageManager.UNKNOWN_IMAGE;
 	            
 	            // Let's wake up when the image arrives
 	            ImageManager.addObservers(renderer.getZone().getAssetID(), this);
 	        }
 
 	        Dimension imgSize = new Dimension(img.getWidth(), img.getHeight());
 			SwingUtil.constrainTo(imgSize, MAP_SIZE_WIDTH, MAP_SIZE_HEIGHT);
 			g.drawImage(img, (size.width - imgSize.width)/2, PADDING + insets.top, imgSize.width, imgSize.height, this);
 
 			g.setColor(Color.black);
 			int nameWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), name);
 			g.drawString(name, size.width/2 - nameWidth/2, size.height - g.getFontMetrics().getDescent() - insets.bottom);
 		}
 		
 		@Override
 		public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
 			repaint();
 			return super.imageUpdate(img, infoflags, x, y, w, h);
 		}
 	}
 }
