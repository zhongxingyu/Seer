 package gui;
 
 import image.ImageLoader;
 import image.Light;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import map.Location;
 import map.Map;
 import map.Tile;
 import model.Model;
 
 public class MapPanel extends JPanel{
 	private Map map;
 	private Model model;
 	
 	private ArrayList<Light> lights;
 	
 	public MapPanel(Model model, Map m){
 		setPreferredSize(new Dimension(Tile.TILE_WIDTH*Map.MAP_WIDTH, Tile.TILE_HEIGHT* Map.MAP_HEIGHT));
<<<<<<< HEAD
 		lights = new ArrayList<Light>();
=======
		//lights = new ArrayList<Light>();
>>>>>>> 5a77da17fb1b818f946347921294a605a0293c8e
 		//lights.add(new Light(new Location(300,300), "spotlight2.png", 150,150));
 		this.map = m;
 		this.model = model;
 		setFocusable(true);
 
 		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageLoader().getImage("cursor.png"), new Point(0, 0), "crosshair");
 		super.setCursor(cursor);
 		
 	}
 	
 	public Map getMap(){
 		return map;
 	}
 	
 	public void paintComponent(Graphics g){
 		Graphics2D g2d = (Graphics2D) g;
 		BufferedImage offscreen = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
 		Graphics2D buffer = (Graphics2D) offscreen.getGraphics();
 		buffer.setColor(Color.black);
 		buffer.fillRect(0, 0, getWidth(), getHeight());
 		
 		map.draw(buffer);
 		
 		
 		model.draw(buffer);
 		
 		//for(Light l: lights){
 		//	l.draw(offscreen);
 		//}
 
 		if (ItemPanel.currentButton != null) {
 			int left = (YoloMouse.mouseX / Tile.TILE_WIDTH) * Tile.TILE_WIDTH;
 			int top = (YoloMouse.mouseY / Tile.TILE_HEIGHT) * Tile.TILE_HEIGHT;
 			
 			buffer.drawImage(ItemPanel.getImage(), left, top, 64, 64, null);
 			Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),
 					ItemPanel.currentButton);
 			super.setCursor(cursor);
 		}else{
 			Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageLoader().getImage("cursor.png"), new Point(0, 0), "crosshair");
 
 			super.setCursor(cursor);
 		}
 		
 		g2d.drawImage(offscreen, 0, 0, this);
 
 	}
 }
