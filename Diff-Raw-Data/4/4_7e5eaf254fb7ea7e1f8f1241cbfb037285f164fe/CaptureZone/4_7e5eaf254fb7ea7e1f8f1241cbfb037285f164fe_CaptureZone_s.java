 package ld27.map;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.swing.UIManager;
 
 import ld27.GamePanel;
 import ld27.player.Animatable;
 import ld27.player.Animation;
 import ld27.util.FileIOHelper;
 
 public class CaptureZone extends Animatable {
 	private static final String root = "/graphics/Platform/";
 	private Rectangle bounds;
 	private final static ArrayList<Point> locations = new ArrayList<Point>();
 	private static final long TEN_SECONDS = 10000;
 	private long countTimeStart = 0;
 	Font digital;
 	boolean countdown = false;
 	DecimalFormat df = new DecimalFormat("0.00");
 	
 	public CaptureZone(){
 		loadAnims();
 		locations.add(new Point(2464,268));
 		locations.add(new Point(2464,652));
 		locations.add(new Point(2464,1292));
 		locations.add(new Point(2464,1804));
 		//Collections.shuffle(locations);
 		bounds = new Rectangle(locations.get(2).x,locations.get(2).y,196,64);
 		try {
 			digital = Font.createFont(Font.TRUETYPE_FONT, FileIOHelper.loadResource("/font/digital-7.ttf"));
 		} catch (FontFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		};
 	}
 	
 	public void loadAnims() {
 		activeAnim = new Animation(root+"Platform0.png",
 								   root+"Platform1.png",
 								   root+"Platform2.png");
 	}
 	
 	public void paint(Graphics g){
 		activeAnim.paint(g, bounds.x+GamePanel.cam.offx, bounds.y+GamePanel.cam.offy);
 		Font old = g.getFont();
 		g.setFont(digital.deriveFont(128f));
 		g.setColor(Color.RED);
 		double time = 10.0;
 		if(isLeftTeamInZone() || isRightTeamInZone()){
 			if(!countdown){
 				countTimeStart = System.currentTimeMillis();
 				countdown = true;
 			}
 			time = 10.0 - ((double)System.currentTimeMillis() - (double)countTimeStart)/1000.0;
 			if(time<=0){
 				time=0.0;
 				GamePanel.endGame(isLeftTeamInZone());
 			}
 		} else {
 			countdown = false;
 			time = 10.0;
 		}
 		String t = df.format(time);
		
		g.drawString(t.substring(0,t.indexOf('.')) + ":" + t.substring(t.indexOf('.')+1,t.length()), bounds.x+GamePanel.cam.offx, bounds.y+GamePanel.cam.offy-30);
 		g.setFont(old);
 	}
 	
 	public boolean isLeftTeamInZone(){
 		boolean left = false;
 		for(int i=0; i<GamePanel.allies.size(); i++){
 			Rectangle pBounds = new Rectangle(GamePanel.allies.get(i).playerX, GamePanel.allies.get(i).playerY,64,64);
 			if(pBounds.intersects(bounds)){
 				left = true;
 				break;
 			}
 		}
 		Rectangle you = new Rectangle(GamePanel.player.playerX, GamePanel.player.playerY,64,64);
 		if(left || bounds.intersects(you)){
 			for(int i=0; i<GamePanel.enemies.size(); i++){
 				Rectangle pBounds = new Rectangle(GamePanel.enemies.get(i).playerX, GamePanel.enemies.get(i).playerY,64,64);
 				if(pBounds.intersects(bounds)){
 					return false;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isRightTeamInZone(){
 		boolean right = false;
 		for(int i=0; i<GamePanel.enemies.size(); i++){
 			Rectangle pBounds = new Rectangle(GamePanel.enemies.get(i).playerX, GamePanel.enemies.get(i).playerY,64,64);
 			if(pBounds.intersects(bounds)){
 				right = true;
 				break;
 			}
 		}
 		Rectangle you = new Rectangle(GamePanel.player.playerX, GamePanel.player.playerY,64,64);
 		if(right){
 			for(int i=0; i<GamePanel.allies.size(); i++){
 				Rectangle pBounds = new Rectangle(GamePanel.allies.get(i).playerX, GamePanel.allies.get(i).playerY,64,64);
 				if(pBounds.intersects(bounds) || bounds.intersects(you)){
 					return false;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 }
