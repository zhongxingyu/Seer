 package com.jpii.navalbattle.game.gui;
 
 import java.awt.Color;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.gui.WindowManager;
 import com.jpii.navalbattle.pavo.gui.controls.PWindow;
 import com.jpii.navalbattle.util.FileUtils;
 
 public class HUD extends PWindow{
 	
 	GradientPaint gp;
 	int centerx,centery;
 	int imgx,imgy;
 	BufferedImage entityImg;
 	int boxwidth,boxheight,boxx,boxy;
 	String location = "";
 	Entity display;
 	
 	public HUD(WindowManager parent,int x, int y, int width, int height){
 		super(parent, x, y, width, height);
 		gp = new GradientPaint(0,0,new Color(96,116,190),0,height,new Color(0,0,54));
 		setTitleVisiblity(false);
 		setVisible(false);
 		centerx = getWidth()-202;
 		centery = getHeight()/2;
 	}
 	
 	public void paint(Graphics2D g) {
 		super.paint(g);
 		g.setPaint(gp);
 		g.fillRect(0,0,getWidth(),getHeight());
 		drawFrame(g, boxx, boxy, boxwidth, boxheight);
 		g.drawImage(entityImg,boxx+50,boxy+50,null);
 		g.setColor(Color.red);
		if(location==null)
 			System.out.println("this is the problem");
		g.drawString(location, centerx-(getWidth(g,location))/2, centery+100);
 	}
 	
 	public void paintAfter(Graphics2D g){
 		super.paintAfter(g);
 	}
 	
 	public void setEntity(Entity e){		
 		display = e;
 		update();
 	}
 	
 	public void update(){
 		if(display != null){
 			setVisible(true);
 			entityImg = FileUtils.getImage(display.imgLocation);
 			int tempwidth = entityImg.getWidth();
 			int tempheight = entityImg.getHeight();
 			boxx = centerx - (tempwidth/2) - 50;
 			boxy = centery - (tempheight/2) - 50;
 			boxwidth = tempwidth+100;
 			boxheight = tempheight+100;
 			location = ("[X:"+display.getLocation().getCol()+" Y:"+display.getLocation().getRow()+"]");
 		}
 		else{
 			setVisible(false);
 		}
 		repaint();
 	}
 	
 	private void drawFrame(Graphics2D g,int x, int y, int width, int height) { // needs work!
 		g.setColor(new Color(126,105,65));
 		g.fillRect(x,y,width,height);
 		g.setColor(new Color(65,54,33));
 		for (int x22 = 8+x; x22 < (x+width)-8; x22 += 8) {
 			g.drawLine(x22+x,0+y,x+x22+4,y+8);
 		}
 		for (int x22 = 8+x; x22 < (x+width)-8; x22 += 8) {
 			g.drawLine(x22+4+x,y+height-9,x22+x,y+height);
 		}
 		for (int y22 = 8+y; y22 < (y+height)-8; y22 += 8) {
 			g.drawLine(x,y22+y,8+x,y+y22+4);
 		}
 		for (int y22 = 8+y; y22 < (y+height)-8; y22 += 8) {
 			g.drawLine(x+height-9,y+y22+4,x+width,y+y22);
 		}
 		g.setColor(new Color(169,140,86));
 		g.fillRect(8+x,y+8,width-16,height-16);
 		g.setColor(Color.black);
 		g.drawRect(x,y,width-1,height-1);
 		g.drawRect(8+x,8+y,width-16,height-16);
 	}
 	
 	private int getWidth(Graphics2D g,String s){
 		return 4+g.getFontMetrics().stringWidth(s);
 	}
 	
 }
