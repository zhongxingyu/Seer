 package org.sankozi.rogueland.gui;
 
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import org.apache.log4j.Logger;
 import org.sankozi.rogueland.control.Game;
 import org.sankozi.rogueland.model.Actor;
 import org.sankozi.rogueland.model.Direction;
 import org.sankozi.rogueland.model.Player;
 import org.sankozi.rogueland.model.Tile;
 
 /**
  * TilePainter that uses font symbols for representing various tiles
  * @author sankozi
  */
 public class FontPainter implements TilePainter{
     private final static Logger LOG = Logger.getLogger(FontPainter.class);
     
     private Font font;
     private FontMetrics metrics;
 
     private int tileHeight;
     private int tileWidth;
 
     private final ConcurrentMap<String,PainterOptions> optionsCache = new ConcurrentHashMap<>();
 	private final Map<Direction, Character> directionChars = new EnumMap<>(Direction.class);
 
     {
 		directionChars.put(Direction.NW, '\\');
 		directionChars.put(Direction.N, '|');
 		directionChars.put(Direction.NE, '/');
 		
 		directionChars.put(Direction.W, '-');
 		directionChars.put(Direction.E, '-');
 
 		directionChars.put(Direction.SW, '/');
 		directionChars.put(Direction.S, '|');
 		directionChars.put(Direction.SE, '\\');
         try {
             font = new Font("Monospaced", Font.BOLD, 30);
         } catch (Exception ex) {
             LOG.error(ex.getMessage(), ex);
         }
     }
 
     @Override
     public Rectangle getPixelLocation(Game game, int width, int height, Point location) {
 		//pixel coordinates
 		width = width - width % tileWidth;
 		height = height - height % tileHeight;
 		
 		//tile coordinates
 		int screenWidth = width / tileWidth;
 		int screenHeight = height / tileHeight;
 		
         return new Rectangle(
 				(screenWidth  / 2) * tileWidth,
 				(screenHeight / 2) * tileHeight,
 				tileWidth, tileHeight);
     }
 
     private static class PainterOptions{
         String character;
         Color color;
     }
 
     private void drawActor(Graphics g, Actor actor, int x, int y){
         PainterOptions po = optionsCache.get(actor.getName());
         if(po == null){
             po = new PainterOptions();
             if(actor.getName().contains("player")){
                 po.character = "@";
                 po.color = Color.WHITE;
             } else {
                 po.character = "@";
                 po.color = Color.RED;
             }
             optionsCache.put(actor.getName(), po);
         }
         g.setColor(po.color);
         g.drawString(po.character, x, y);
     }
 
 	private void drawSword(Game game, Graphics g, int startingPixelX, int startingX, int startingPixelY, int startingY) {
 		g.setColor(Color.WHITE);
 		Player p = game.getPlayer();
 		Point location = p.getLocation();
 		Direction dir = p.getWeaponDirection();
 		g.drawString(directionChars.get(dir).toString(), 
 				startingPixelX + tileWidth * (location.x - startingX + dir.dx), 
 				startingPixelY + tileHeight * (location.y - startingY + dir.dy));
 //		LOG.info("paint end");
 	}
 
     private void initMetrics(Graphics g){
         metrics = g.getFontMetrics(font);
         tileHeight = metrics.getHeight() - 3;
         tileWidth = metrics.charWidth('#') - 3;
     }
 
     @Override
     public void paint(Game game, Graphics g, int width, int height) {
 		if(!game.isInitialized()){
 			g.setColor(Color.BLACK);
 			g.fillRect(0,0, width, height);
 			return;
 		}
 //		LOG.info("paint start : size ->" + width + " " + height);
 		Tile[][] tiles = game.getLevel().getTiles();
 
 		if(metrics == null){
         	initMetrics(g);
 		}
 
 		//pixel coordinates
 		width = width - width % tileWidth;
 		height = height - height % tileHeight;
 		int startingPixelX = 0;
 		int startingPixelY = metrics.getAscent();
 
 		//tile coordinates
 		int screenWidth = width / tileWidth;
 		int screenHeight = height / tileHeight;
 		Point playerLocation = game.getPlayer().getLocation();
 		int startingX = playerLocation.x - screenWidth  / 2;
 		if(startingX < 0){
 			startingPixelX -= startingX * tileWidth;
 			screenWidth += startingX;
 			startingX = 0;
 		}
 		int startingY = playerLocation.y - screenHeight / 2;
 		if(startingY < 0){
 			startingPixelY -= startingY * tileHeight;
 			screenHeight += startingY;
 			startingY = 0;
 		}
 		Rectangle rect = new Rectangle(startingX, startingY, screenWidth, screenHeight);
 
         g.setFont(font);
         g.setColor(Color.BLACK);
 		//clean part with black
         g.fillRect(0, 0, width, height);
 
 		//fill with symbols
         int y = startingPixelY;
         for(int iy = rect.y; iy < rect.height; ++iy){
 			int x = startingPixelX;
 			for(int ix = rect.x; ix < rect.width; ++ix){
 				Actor actor = tiles[ix][iy].actor;
 				if(actor != null){
 					drawActor(g, actor,x,y);
 				} else {
 					drawField(g,tiles[ix][iy], x, y);
 				}
 				x += tileWidth;
 			}
             y += tileHeight;
         }
 		drawSword(game, g, startingPixelX, startingX, startingPixelY, startingY);
 //		LOG.info("paint end");
     }
 
     private void drawField(Graphics g, Tile tile, int x, int y) {
         switch (tile.type) {
             case FLOOR:
                 g.setColor(Color.GRAY);
                 g.drawString(".", x, y);
                 break;
             case GRASS:
                 g.setColor(Color.GREEN);
                 g.drawString("\"", x, y);
                 break;
             case WALL:
                 g.setColor(Color.GRAY);
                 g.drawString("#", x, y);
                 break;
         }
     }
 }
