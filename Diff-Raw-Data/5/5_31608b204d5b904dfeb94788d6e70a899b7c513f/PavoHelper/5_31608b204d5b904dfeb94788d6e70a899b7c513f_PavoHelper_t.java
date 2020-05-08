 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.pavo;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RadialGradientPaint;
 import java.awt.RenderingHints;
 import java.awt.MultipleGradientPaint.CycleMethod;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.io.PavoImage;
 
 /**
  * 
  * @author maximusvladimir
  *
  */
 public class PavoHelper {
 	private static SystemSpeed calcs;
 	public static PavoImage createInnerShadow(int width, int height) {
 		PavoImage b = new PavoImage(width,height,BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2 = (Graphics2D)b.getGraphics();
 		Point2D center = new Point2D.Float(width/2, height/2);
         float radius = width;
         Point2D focus = new Point2D.Float(width/2, height/2);
         float[] dist = {0.0f,0.3f, 1.0f};
         Color[] colors = {new Color(0,0,0,0),new Color(0,0,0,0), new Color(0,0,0,255)};
         RadialGradientPaint p = new RadialGradientPaint(center, radius, focus, dist, colors, CycleMethod.NO_CYCLE);
         g2.setPaint(p);
         g2.fillRect(0, 0,width, height);
         return b;
 	}
 	public static SystemSpeed getCalculatedSystemSpeed() {
 		if (calcs == null) {
 			long a = System.nanoTime();
 			a = System.nanoTime();
 			a = System.nanoTime();
 			a = System.nanoTime();
 			a = System.nanoTime();
 			long sb = 0;
 			for (int b = 0; b < 10000; b++) {
 				sb = Double.doubleToLongBits(Math.sqrt(b >> 2) * Math.log(b));
 			}
 			a = System.nanoTime() - a;
 			//System.out.println("sb"+sb+"time:"+a);
 			if (a < 2000000)
 				calcs = SystemSpeed.WHALE;
 			else if (a < 2500000)
 				calcs = SystemSpeed.CHEETAH;
 			else if (a < 3100000)
 				calcs = SystemSpeed.HARE;
 			else if (a < 3500000)
 				calcs = SystemSpeed.CREEPER;
 			else
 				calcs = SystemSpeed.TURTLE;
 			if (calcs == SystemSpeed.TURTLE) {
 				a = System.nanoTime();
 				a = System.nanoTime();
 				a = System.nanoTime();
 				a = System.nanoTime();
 				sb = 0;
 				for (int b = 0; b < 10000; b++) {
 					sb = Double.doubleToLongBits(Math.sqrt(b >> 2) * Math.log(b));
 				}
 				a = System.nanoTime() - a;
 				System.out.println("Speed time:"+a);
 				if (a < 2000000)
 					calcs = SystemSpeed.WHALE;
 				else if (a < 2500000)
 					calcs = SystemSpeed.CHEETAH;
 				else if (a < 3100000)
 					calcs = SystemSpeed.HARE;
 				else if (a < 3500000)
 					calcs = SystemSpeed.CREEPER;
 				else
 					calcs = SystemSpeed.TURTLE;
 			}
 		}
 		return calcs;
 	}
 	
 	public static boolean isChunkVisibleOnScreen(World w, Chunk c) {
 		if (w == null || c == null)
 			return false;
 		int sx = Math.abs(w.getScreenX());
 		int sy = Math.abs(w.getScreenY());
 		int px = c.getX() * 100;
 		int py = c.getZ() * 100;
 		if (px-sx+100 >= 0 && py-sy+100 >= 0 && px-sx <= Game.Settings.currentWidth && py-sy <= Game.Settings.currentHeight){
 			return true;
 		}
 		else
 			return false;
 	}
 	public static Chunk convertGridLocationToChunk(World w, Location l) {
 		if (l == null || w == null)
 			return null;
 		int x = l.getCol()/2;
 		int y = l.getRow()/2;
 		if (x < 0 || y < 0 || x > PavoHelper.getGameWidth(w.getWorldSize()) || y > PavoHelper.getGameHeight(w.getWorldSize()))
 			return null;
 		return w.getChunk(x, y);
 	}
 	public static int getGameWidth(WorldSize ws) {
 		if (ws == WorldSize.WORLD_HUGE)
 			return 86;
 		else if (ws == WorldSize.WORLD_LARGE)
 			return 64;
 		else if (ws == WorldSize.WORLD_MEDIUM)
 			return 32;
 		else if (ws == WorldSize.WORLD_SMALL)
 			return 16;
 		else 
 			return 4;
 	}
 	public static int getGameHeight(WorldSize ws) {
 		if (ws == WorldSize.WORLD_HUGE)
 			return 86;
 		else if (ws == WorldSize.WORLD_LARGE)
 			return 64;
 		else if (ws == WorldSize.WORLD_MEDIUM)
 			return 32;
 		else if (ws == WorldSize.WORLD_SMALL)
 			return 16;
 		else 
 			return 4;
 	}
 	public static Point convertWorldToWorldSpace(World w, Point wp) {
 		return wp;
 	}
 	public static Point convertWorldSpaceToScreen(World w, Point wsp) {
 		return new Point(wsp.x - w.getScreenX(), wsp.y - w.getScreenY());
 	}
 	public static Point convertScreenToWorldSpace(World w, Point wsp) {
 		return new Point(wsp.x + w.getScreenX(), wsp.y + w.getScreenY());
 	}
 	public static Location convertWorldSpaceToGridLocation(Point p) {
 		return new Location(p.y/50,p.x/50);
 	}
 	public static Color generateNewLeafColor() {
 		byte pointer = (byte)Game.Settings.rand.nextInt(0, 5);
 		switch (pointer) {
 			case 0:
 				return Game.Settings.rand.nextColor(244,214,38,15);
 			case 1:
 				return Game.Settings.rand.nextColor(236,89,47,15);
 			case 2:
 				return Game.Settings.rand.nextColor(135,185,98,15);
 			case 3:
 				return Game.Settings.rand.nextColor(240,244,89,15);
 			case 4:
 				return Game.Settings.rand.nextColor(135,185,98,15);
 		}
 		return Game.Settings.rand.nextColor(135,185,98,15); 
 	}
 	public static Color generateLeafMod(Color base) {
 		Color c = Game.Settings.rand.nextColor(base.getRed(),base.getGreen(),base.getBlue(),8);
 		return new Color(c.getRed(),c.getGreen(),c.getBlue(),125);
 	}
 	/**
 	 * Linear interpolation.
 	 * @param num0
 	 * @param num1
 	 * @param amount
 	 * @return
 	 */
 	public static double Lerp(int num0, int num1, double amount)
     {
     	return num0 + (amount*(num1-num0));
     }
 	/**
 	 * Linear interpolation.
 	 * @param color0
 	 * @param color1
 	 * @param amount
 	 * @return
 	 */
     public static Color Lerp(Color color0, Color color1, double amount)
     {
 	    int r = (int)Lerp(color0.getRed(), color1.getRed(), amount);
 	    int g = (int)Lerp(color0.getGreen(), color1.getGreen(), amount);
 	    int b = (int)Lerp(color0.getBlue(), color1.getBlue(), amount);
 	    int a = (int)Lerp(color0.getAlpha(), color1.getAlpha(), amount);
 	    if (r > 255)
 	    	r = 255;
 	    if (r < 0)
 	    	r = 0;
 	    if (g > 255)
 	    	g = 255;
 	    if (g < 0)
 	    	g = 0;
 	    if (b > 255)
 	    	b = 255;
 	    if (b < 0)
 	    	b = 0;
 	    if (a > 255)
 	    	a = 255;
 	    if (a < 0)
 	    	a = 0;
 	    return new Color(r,g,b,a);
     }
 	public static boolean isEntityVisibleOnScreen(World w, Entity ent) {
 		if (w == null || ent == null || ent.getLocation() == null)
 			return false;
 		int sx = w.getScreenX();
 		int sy = w.getScreenY();
 		int px = ent.getLocation().getCol() * 50;
 		int py = ent.getLocation().getRow() * 50;
 		if (px >= sx && py >= sy && px <= sx + Game.Settings.currentWidth && py <= sy + Game.Settings.currentHeight)
 			return true;
 		else
 			return false;
 		
 	}
	public static Graphics2D createGraphics(PavoImage b) {
 		if (b == null)
 			return null;
 		
 		Graphics2D g = (Graphics2D) b.getGraphics();
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		return g;
 	}
 	
 	public static Graphics2D createGraphics(BufferedImage b) {
 		if (b == null)
 			return null;
 		
 		Graphics2D g = (Graphics2D) b.getGraphics();
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		return g;
	}
 
 	public static final BufferedImage OneByOnePixel = create1x1Pixel();
 	public static BufferedImage create1x1Pixel() {
 		BufferedImage b = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
 		Graphics g = b.getGraphics();
 		g.setColor(Color.white);
 		g.fillRect(0,0,2,2);
 		g.dispose();
 		return b;
 	}
 	public static BufferedImage imgUtilAdjustImageTransparency(BufferedImage src, float opacity) {
 		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
 		BufferedImage cpy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = (Graphics2D)cpy.getGraphics();
 	    g.setComposite(ac);
 	    g.drawImage(src,0,0,null);
 	    g.dispose();
 	    return cpy;
 	}
 	
 	public static BufferedImage imgUtilFastCopy(BufferedImage src) {
 		if (src == null)
 			return null;
 		BufferedImage b = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
 		b.setData(src.getRaster());
 		return b;
 	}
 	
 	public static BufferedImage imgUtilOutline(BufferedImage src, Color c) {
 		if (src == null)
 			return null;
 		BufferedImage b = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
 		Graphics2D g = (Graphics2D)b.getGraphics();
 		g.setColor(c);
 		g.drawRect(1,1,src.getWidth()-1, src.getHeight()-1);
 		g.drawImage(src,0,0,null);
 		g.dispose();
 		return b;
 	}
 	
 	public static BufferedImage imgUtilFastCrop(BufferedImage src, int x, int y, int width, int height) {
 		if (src == null)
 			return null;
 		if (x == 0 && y == 0 && width == src.getWidth() && height == src.getHeight())
 			return imgUtilFastCopy(src);
 		else {
 			BufferedImage b = new BufferedImage(width, height, src.getType());
 			Graphics g = b.getGraphics();
 			g.drawImage(src,-x,-y,null);
 			g.dispose();
 			return b;
 		}
 	}
 	public static BufferedImage imgUtilMinimizeNoAlpha(BufferedImage src) {
 		if (src == null)
 			return null;
 		BufferedImage b = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
 		Graphics2D g = (Graphics2D)b.getGraphics();
 		g.drawImage(src,0,0,null);
 		g.dispose();
 		return b;
 	}
 }
