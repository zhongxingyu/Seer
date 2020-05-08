 package gui;
 
 import image.ImageLoader;
 import image.Light;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import map.Location;
 import map.Map;
 import map.Tile;
 import model.Model;
 
 public class MapPanel extends JPanel{
 	private Map map;
 	private Model model;
 	private double tickCount = 0;
 	private ArrayList<Light> lights;
 	public static boolean fullYolo = false;
 	private int yoloStage = 0;
 	
 	public MapPanel(Model model, Map m){
 		lights = new ArrayList<Light>();
 		//lights.add(new Light(new Location(300,300), "light_color.png", 300,300));
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
 		
 		if (YoloRiot.lost) {
 			g2d.drawImage( new ImageLoader().getImage("StartScreen.png"), 0, 0, 400, 400, null);
 		}
 		
 		
 		BufferedImage offscreen = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
 		Graphics2D buffer = (Graphics2D) offscreen.getGraphics();
		/*
 		if (Model.model.yolomode){
 			
 			if (tickCount > 30){
 				
 				buffer.rotate(10);
 				
 			}
 			else if(tickCount > 90){
 				
 				
 			}
 			else if(tickCount > 120){
 				buffer.rotate(-10);
 				
 			}
 			else if (tickCount > 150){
 				
 				
 			}
 			
		}*/
 		buffer.setColor(Color.black);
 		buffer.fillRect(0, 0, getWidth(), getHeight());
 		
 
 		tickCount++;
 		map.draw(buffer);
 		
 		
 		model.draw(buffer);
 
 		//for(Light l: lights){
 	//		l.draw(offscreen);
 	//	}
 
 
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
 		
 		if(Model.model.yolomode){
 			//TODO yolo drawing
 			//Light.drawFilter(offscreen, -50,-50,-50);
 		}
 		
 		g2d.drawImage(offscreen, 0, 0, this);
 
 	}
 }
