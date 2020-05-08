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
 
 package com.jpii.navalbattle.pavo.gui;
 
 import java.awt.event.*;
 import java.awt.image.*;
 import java.awt.*;
 
 import maximusvladimir.dagen.Rand;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.ProceduralLayeredMapGenerator;
 import com.jpii.navalbattle.pavo.PavoHelper;
 import com.jpii.navalbattle.pavo.Renderable;
 import com.jpii.navalbattle.pavo.World;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.EntityManager;
 import com.jpii.navalbattle.pavo.io.PavoImage;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.renderer.RenderConstants;
 
 public class OmniMap extends Renderable {
 	private static final long serialVersionUID = 1L;
 	int mx,my;
 	World w;
 	PavoImage terrain;
 	public OmniMap(World w) {
 		super();
 		this.w = w;
 		setSize(100,100);
 		buffer = (new PavoImage(getWidth(), getHeight(),BufferedImage.TYPE_INT_RGB));
 		terrain = (new PavoImage(getWidth(), getHeight(),BufferedImage.TYPE_INT_RGB));
 		writeBuffer();
 	}
 	public void mouseMoved(MouseEvent me) {
 		mx = me.getX();
 		my = me.getY();
 		
 		render();
 	}
 	public boolean mouseDown(MouseEvent me) {
 		int ax = me.getX() - (Game.Settings.currentWidth - 158);
 		int ay = me.getY() - 40;
 		if (ax > 100 || ay > 100 || ax < 0 || ay < 0)
 			return false;
 		int sw = (int)(((150 * Game.Settings.currentWidth)/(PavoHelper.getGameWidth(w.getWorldSize())*100))/3.33333333333f);
 		int sh = (int)(((150 * Game.Settings.currentHeight)/(PavoHelper.getGameHeight(w.getWorldSize())*100))/3.333333333333f);
 
 		if (!w.getGame().isAClient())
 			w.getGame().getSelfServer().send("bounds:"+sw+","+sh);
 		else
 			w.getGame().getSelfClient().send("bounds:"+sw+","+sh);
 		w.setLoc(
 				((-(ax-sw)*PavoHelper.getGameWidth(w.getWorldSize()))),
 				((-(ay-sh)*PavoHelper.getGameHeight(w.getWorldSize())))
 				);
 		render();
 		w.forceRender();
 		return true;
 	}
 	public boolean mouseDragged(MouseEvent me) {
 		return mouseDown(me);
 	}
 	public void writeBuffer() {
 		Graphics2D g = PavoHelper.createGraphics(terrain);
 		Rand rand = new Rand(Game.Settings.seed);
 		for (int x = 0; x < 100/3; x++) {
 			for (int y = 0; y < 100/3; y++) {
 				int strx = x * PavoHelper.getGameWidth(w.getWorldSize());
 				int stry = y * PavoHelper.getGameHeight(w.getWorldSize());
 				float frsh = ProceduralLayeredMapGenerator.getPoint(strx,stry);
 				float lsy = frsh;
 				if (lsy < 0.4) {
 					int rgs = Helper.colorSnap((int)(lsy*102));
 					g.setColor(new Color(63+rand.nextInt(-7,7),60+rand.nextInt(-7,7),rand.nextInt(90, 100)+rgs));
 				}
 				else if (lsy < 0.55) {
 					Color base1 = PavoHelper.Lerp(RenderConstants.GEN_SAND_COLOR,new Color(52,79,13),((lsy-0.4)/0.15));
 					base1 = Helper.randomise(base1, 8, rand, false);
 					g.setColor(base1);
 				}
 				else{
 					Color base1 = PavoHelper.Lerp(new Color(52,79,13),new Color(100,92,40),((lsy-0.55)/0.45));
 					base1 = Helper.randomise(base1, 8, rand, false);
 					g.setColor(base1);
 				}
 				g.fillRect(x*3,y*3,4,4);
 			}
 		}
 		g.dispose();
 	}
 	int mpx = 0,mpy=0;
 	public void setMultiplayer(int xx, int zz) {
 		mpx = xx;
 		mpy = zz;
 		render();
 	}
 	public void render() {
 		Graphics2D g = PavoHelper.createGraphics(getBuffer());
 		g.drawImage(terrain, 0,0,null);
 		int rwx = (int)(-w.getScreenX()*100)/(PavoHelper.getGameWidth(w.getWorldSize()) * 100);
 		int rwy = (int)(-w.getScreenY()*100)/(PavoHelper.getGameHeight(w.getWorldSize()) * 100);
 		int sw = (int)((150 * Game.Settings.currentWidth)/(PavoHelper.getGameWidth(w.getWorldSize())*100));
 		int sh = (int)((150 * Game.Settings.currentHeight)/(PavoHelper.getGameHeight(w.getWorldSize())*100));
 		int rwmx = (int) (Math.abs(mpx-(Game.Settings.currentWidth/2)) * 33.333333 / (PavoHelper.getGameWidth(w.getWorldSize()) * 100))*3;
 		int rwmy = (int) (Math.abs(mpy-(Game.Settings.currentHeight/2)) * 33.333333 / (PavoHelper.getGameHeight(w.getWorldSize()) * 100))*3;
 
 		g.setColor(Color.red);
 		g.drawRect(rwx-1,rwy-1,sw/2,sh/2);
 		if (w.getGame().isConnectedToClientOrServer()) {
 			g.setColor(Color.yellow);
 			g.drawRect(rwmx-1,rwmy-1,sw/2,sh/2);
 		}
 		EntityManager eme = w.getEntityManager();
 		g.setColor(Color.cyan);
 		for (int c = 0; c < eme.getTotalEntities(); c++) {
 			Entity ent = eme.getEntity(c);
 			int rdx = (int)(((ent.getLocation().getCol()*50)*100)/(PavoHelper.getGameWidth(w.getWorldSize()) * 100));
 			int rdy = (int)(((ent.getLocation().getRow()*50)*100)/(PavoHelper.getGameHeight(w.getWorldSize()) * 100));
 			g.drawLine(rdx,rdy,rdx,rdy);
 		}
 		
 		g.setColor(new Color(100, 78, 47));
         g.drawRect(1, 1, width - 3, 100 - 3);
         g.setColor(new Color(74, 30, 3));
         g.drawRect(0, 0, width - 1, 100 - 1);
         g.dispose();
 	}
 }
