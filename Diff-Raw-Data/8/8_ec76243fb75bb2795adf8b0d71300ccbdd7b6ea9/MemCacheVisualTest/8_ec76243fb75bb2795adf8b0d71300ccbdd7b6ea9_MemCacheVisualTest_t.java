 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.albite.test;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import javax.swing.JFrame;
 import org.albite.cache.CacheException;
 import org.albite.cache.VisualMemCache;
 import org.albite.cache.visual.Rect;
 import org.albite.test.CharCacheTest.CharData;
 import org.albite.test.CharCacheTest.CharKey;
 
 /**
  *
  * @author Albus Dumbledore
  */
 public class MemCacheVisualTest extends JFrame {
 
     private static final String FILENAME = "alice.txt";
     private static final Dimension DIMENSIONS = new Dimension(720, 540);
 
     VisualMemCache cache;
 
     public MemCacheVisualTest(final Dimension dimensions)
     {
         super("Visual Cache");
 
         Rect subCacheDimensions = new Rect(300, 80, 100, 400);
        VisualMemCache subCache = new VisualMemCache(null, 220, 256, 500, 1000, subCacheDimensions, "Back Cache (256)", this);
 
         Rect cacheDimensions = new Rect(50, 80, 100, 400);
        cache = new VisualMemCache(subCache, 70, 100, 70, 100, cacheDimensions, "Main Cache (100)", this);
 
         WindowAdapter window =
                 new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }
         };
 
         addWindowListener(window);
         pack();
         setSize(dimensions);
         setVisible(true);
     }
 
     @Override
     public void paint(Graphics g) {
         final int width = getWidth();
         final int height = getHeight();
         final int cacheY = cache.dimensions.y;
        final int cacheHeight = cache.dimensions.height + 1;
 
         g.setColor(Color.WHITE);
         g.fillRect(0, cacheY, width, cacheHeight);
 
         cache.paint(g);
 
         g.setColor(Color.DARK_GRAY);
         g.fillRect(0, 0, width, cacheY);
         g.fillRect(0, cacheY + cacheHeight, width, height - cacheHeight);
 
         cache.paintSecondLayer(g);
     }
 
     private void run() throws IOException, CacheException {
         String line;
 
         BufferedReader r =
                 new BufferedReader(
                 new InputStreamReader(
                 getClass().getResourceAsStream(FILENAME)));
 
         while ((line = r.readLine())  != null) {
             for (int i = 0; i < line.length(); i++) {
                 char c = line.charAt(i);
                 CharKey key = new CharKey(c);
                 CharData data = (CharData) cache.get(key);
 
                 if (data == null) {
                     data = new CharData(c);
                     cache.put(key, data);
                 }
             }
         }
 
         repaint();
     }
 
     public static void main(String s[]) throws IOException, CacheException {
         MemCacheVisualTest visual = new MemCacheVisualTest(DIMENSIONS);
         visual.run();
     }
 }
