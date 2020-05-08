 package org.sankozi.rogueland.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import org.apache.log4j.Logger;
 import org.sankozi.rogueland.model.Actor;
 import org.sankozi.rogueland.model.Tile;
 
 /**
  *
  * @author sankozi
  */
 public class FontPainter implements TilePainter{
     private final static Logger LOG = Logger.getLogger(FontPainter.class);
     
     private Font font;
     private FontMetrics metrics;
 
     private final ConcurrentMap<String,PainterOptions> optionsCache = new ConcurrentHashMap<String, PainterOptions>();
 
     {
         try {
             font = new Font("Monospaced", Font.BOLD, 30);
         } catch (Exception ex) {
             LOG.error(ex.getMessage(), ex);
         }
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
 
     @Override
     public void paint(Rectangle rect, Tile[][] tiles, Graphics g) {
         LOG.trace("font painter!");
         metrics = g.getFontMetrics(font);
         g.setFont(font);
         int dy = metrics.getHeight() - 3;
         int dx = metrics.charWidth('#') - 3;
         g.setColor(Color.BLACK);
 
        int y = rect.y * dy;

        g.fillRect(rect.x * dx, y, rect.width * dx,  rect.height * dy);
 
         for(int iy = rect.y; iy < rect.height; ++iy){
             int x = rect.x * dx;
             for(int ix = rect.x; ix < rect.width; ++ix){
                 Actor actor = tiles[ix][iy].actor;
                 if(actor != null){
 //                    LOG.info("tile = " + tiles[ix][iy].player);
                     drawActor(g, actor,x,y);
                 } else {
                     switch(tiles[ix][iy].type){
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
                 x += dx;
             }
             y += dy;
         }
         
     }
 
 }
