 package com.jpii.navalbattle.game.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import com.jpii.navalbattle.game.TurnManager;
 import com.jpii.navalbattle.game.entity.MoveableEntity;
 import com.jpii.navalbattle.game.entity.PortEntity;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.GridHelper;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.gui.NewWindowManager;
 import com.jpii.navalbattle.pavo.gui.controls.PImage;
 import com.jpii.navalbattle.pavo.gui.controls.PWindow;
 import com.jpii.navalbattle.pavo.gui.controls.PButton;
 import com.jpii.navalbattle.pavo.gui.events.PMouseEvent;
 import com.jpii.navalbattle.util.FileUtils;
 
 public class HUD extends PWindow{
 	
 	TurnManager tm;
 	GradientPaint gp;
 	GradientPaint ht;
 	int centerx,centery;
 	int imgx,imgy;
 	BufferedImage entityImg;
 	int boxwidth,boxheight,boxx,boxy;
 	String location = new String("");
 	String health = new String("");
 	String movement = new String("");
 	Entity display;
 	String[] events;
 	boolean pinned = true;
 	boolean attackGuns = false;
 	boolean attackMissiles = false;
 	
 	PImage missile;
 	PImage bullet;
 	PImage move;
 	PImage diplomacy;
 	
 	PButton missileB;
 	PButton bulletB;
 	PButton moveB;
 	PButton diplomacyB;
 	PButton nextMove;
 	
 	 public HUD(NewWindowManager parent,TurnManager tm,int x, int y, int width, int height){
 		super(parent, x, y, width, height);
 		this.tm = tm;
 		gp = new GradientPaint(0,0,new Color(96,116,190),0,height,new Color(0,0,54));
 		ht = new GradientPaint(0,0,new Color(169,140,86),0,height,new Color(69,40,6));
 		setTitleVisiblity(false);
 		setVisible(false);
 		centerx = getWidth()-210;
 		centery = getHeight()/2;
 		events = new String[25];
 		initButtons();
 		update();
 	 }
 	 
 	 private void initButtons(){
 		addControl(missileB = new PButton(this,(getWidth()/2)-90,getHeight()-60,30,30));
 		addControl(bulletB = new PButton(this,(getWidth()/2)-30,getHeight()-60,30,30));
 		addControl(diplomacyB = new PButton(this,(getWidth()/2)+30,getHeight()-60,30,30));
 		addControl(moveB = new PButton(this,(getWidth()/2)+90,getHeight()-60,30,30));
 		addControl(nextMove = new PButton(this,"End Turn",(getWidth()/2)-60,getHeight()-130,150,40));
 		
 		nextMove.setFont(new Font("Arial",0,35));		
 		
 		missile = new PImage(this);
 		bullet = new PImage(this);
 		move = new PImage(this);
 		diplomacy = new PImage(this);
 		
 		missile.setLoc((getWidth()/2)-90,getHeight()-60);
 		bullet.setLoc((getWidth()/2)-30,getHeight()-60);
 		diplomacy.setLoc((getWidth()/2)+30,getHeight()-60);
 		move.setLoc((getWidth()/2)+90,getHeight()-60);
 		
 		missile.setSize(30,30);
 		bullet.setSize(30,30);
 		diplomacy.setSize(30,30);
 		move.setSize(30,30);
 		
 		missile.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Missile.png")));
 		bullet.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Bullet.png")));
 		diplomacy.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Diplomacy.png")));
 		move.setImage(PImage.registerImage(FileUtils.getImage("drawable-game/Buttons/Move.png")));
 		
 		missile.repaint();
 		bullet.repaint();
 		diplomacy.repaint();
 		move.repaint();
 		
 		addControl(missile);
 		addControl(bullet);
 		addControl(diplomacy);
 		addControl(move);
 		
 		moveB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				if(move.isVisible()){
 					if(display!=null && display.getHandle()==1){
 						MoveableEntity display2 = (MoveableEntity)display;
 						display2.toggleMovable();
 					}
 				}
 			}
 		});
 		
 		final TurnManager tm2 = tm;
 		
 		nextMove.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				if(nextMove.isVisible()){
 					tm2.nextTurn();
 					update();
 				}
 			}
 		});
 		
 		bulletB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				if(bullet.isVisible()){
 					if(display!=null && display.getHandle()==1){
 						MoveableEntity display2 = (MoveableEntity)display;
 						display2.toggleAttackRange();
 						attackGuns = true;	
 						attackMissiles = false;	
 					}
 				}
 			}
 		});
 		
 		missileB.addMouseListener(new PMouseEvent(){
 			public void mouseDown(int x, int y, int buttonid) {
 				if(missile.isVisible()){
 					if(display!=null && display.getHandle()==1){
 						MoveableEntity display2 = (MoveableEntity)display;
 						display2.toggleAttackRange();
 						attackMissiles = true;
 						attackGuns = false;	
 					}
 				}
 			}
 		});
 		
 		this.repaint();
 	 }
 	
 	public void paint(Graphics2D g) {
 		super.paint(g);
 		
 		g.setPaint(gp);
 		g.fillRect(0,0,getWidth(),getHeight());
 		
 		drawText(g);
 		drawEntityBox(g);	
 		drawHistoryBox(g);
 	}
 	
 	private void drawText(Graphics2D g){
 		g.setColor(Color.green);
 		Font temp = g.getFont();
 		Font perks = new Font("Arial",0,10);
 		g.setFont(perks);
 		g.drawString("Missile",(getWidth()/2)-90,getHeight()-62);
 		g.drawString("Guns",(getWidth()/2)-28,getHeight()-62);
 		g.drawString("Diplomacy",(getWidth()/2)+18,getHeight()-62);
 		g.drawString("Move",(getWidth()/2)+90,getHeight()-62);
 		if(tm!=null)
 			if(tm.getTurn()!=null)
 				if(tm.getTurn().getPlayer()!=null)
 					g.drawString(""+tm.getTurn().getPlayer().name,(getWidth()/3)+25,25);
 		g.setFont(temp);
 	}
 	
 	private void drawEntityBox(Graphics2D g){
 		drawFrame(g, boxx, boxy, boxwidth, boxheight);
 		if(display!=null){
 			g.drawImage(entityImg,boxx+50,boxy+50,null);
 			if (display.getHandle() == 2) {
 				g.setColor(new Color(169,140,86));
 				g.drawRect(boxx+49,boxy+49,51,51);
 			}
 			g.setColor(Color.red);
 			drawString(g,location, centerx, centery+60);
 			drawString(g,health, centerx, centery-35);
 			drawString(g,movement, centerx, centery+40);
 		}
 	}
 	
 	private void drawHistoryBox(Graphics2D g){
 		g.setPaint(ht);
 		g.fillRoundRect(25,0,375,151,25,25);
 		g.setPaint(Color.black);
 		g.drawRoundRect(25,0,375,151,25,25);
 	}
 	
 	public void setEntity(Entity e){
 		if(display!=null){
 			if(e==null || !display.equals(e)){
 				if(display.getHandle()==1){
 					MoveableEntity display = (MoveableEntity)this.display;
 					if(display.isMovableTileBeingShown()){
 						display.toggleMovable();
 					}
 					if(display.isAttackTileBeingShown()){
 						display.toggleAttackRange();
 					}
 				}
 			}
 		}
 		display = e;
 		update();
 	}
 	
 	public void update(){
 		if(display != null || pinned){
 			setVisible(true);
 			move.setVisible(false);
			moveB.setVisible(false);
 			missile.setVisible(false);
 			bullet.setVisible(false);
		//	diplomacy.setVisible(false);
			missileB.setVisible(false);
			bulletB.setVisible(false);
		//	diplomacyB.setVisible(false);
 			boxx = boxy = boxheight = boxwidth = 0;
 			if(display!=null){
 				if (display.getHandle()==2) {
 					PortEntity display = (PortEntity)this.display;
 					entityImg = display.getIcon();
 				}
 				else
 					entityImg = FileUtils.getImage(display.imgLocation);
 				int tempwidth = entityImg.getWidth();
 				int tempheight = entityImg.getHeight();
 				boxx = centerx - (tempwidth/2) - 50;
 				boxy = centery - (tempheight/2) - 50;
 				boxwidth = tempwidth+100;
 				boxheight = tempheight+100;
 				location = ("[X:"+display.getLocation().getCol()+" Y:"+display.getLocation().getRow()+"]");
 				if(display.getHandle()==1){
 					MoveableEntity display = (MoveableEntity)this.display;
 					if(display.isMovableTileBeingShown()){
 						display.toggleMovable();
 						display.toggleMovable();
 					}
 					if(display.isAttackTileBeingShown()){
 						display.toggleAttackRange();
 						display.toggleAttackRange();
 					}
 					if(display.getMaxMovement()!=display.getMoved()){
 						move.setVisible(true);
 					}
 					moveB.setVisible(true);
 					missile.setVisible(true);
 					bullet.setVisible(true);
 					diplomacy.setVisible(true);
 					missileB.setVisible(true);
 					bulletB.setVisible(true);
 					diplomacyB.setVisible(true);
 					health = ("Health: "+display.getHealth()+"%");
 					movement = ("Movement Left: "+(display.getMaxMovement()-display.getMoved())+" out of "+display.getMaxMovement());
 				}
 			}
 		}
 		else{
 			setVisible(false);
 		}
 		repaint();
 	}
 	
 	private void drawFrame(Graphics2D g,int x, int y, int width, int height) {
 		g.setColor(new Color(126,105,65));
 		g.fillRect(x,y,width,height);
 		g.setColor(new Color(65,54,33));
 		for (int x22 = x+8; x22 < (x+width)-8; x22 += 8) {
 			g.drawLine(x22+x-50,y,x+x22+4-50,y+8);
 		}
 		for (int x22 = x+8; x22 < (x+width)-8; x22 += 8) {
 			g.drawLine(x22+4+x-50,y+height-9,x22+x-50,y+height);
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
 	
 	private void drawString(Graphics2D g,String s,int x, int y){
 		if(s!=null){
 			g.drawString(s, x-(getWidth(g,s))/2, y);
 		}
 	}
 	
 	public boolean isShowingMove(){
 		boolean flag = false;
 		if(display!=null)
 			if(display.getHandle() == 1)
 				flag = ((MoveableEntity) display).isMovableTileBeingShown();
 		return flag;
 	}
 	
 	public boolean isShowingAttack(){
 		boolean flag = false;
 		if(display!=null)
 			if(display.getHandle() == 1)
 				flag = ((MoveableEntity) display).isAttackTileBeingShown();
 		return flag;
 	}
 	
 	public boolean hudClick(int x, int y, boolean leftclick){
 		if(moveShip(x,y,leftclick))
 			return true;
 		if(attackGuns(x,y,leftclick))
 			return true;
 		if(attackMissile(x,y,leftclick))
 			return true;
 		return false;
 	}
 	
 	private boolean moveShip(int x, int y, boolean leftclick){	
 		if(!isShowingMove())
 			return false;
 
 		MoveableEntity display = (MoveableEntity)this.display;			
 		
 		if(!tm.getTurn().canmoveEntity(display))
 			return false;
 
 		int startr = display.getLocation().getRow();
 		int startc = display.getLocation().getCol();
 		if(leftclick && GridHelper.canMoveTo(display.getManager(), display, display.getCurrentOrientation(), y, x,display.getWidth())){
 			if(display.isMovableTileBeingShown()){
 				display.toggleMovable();
 			}
 			display.moveTo(new Location(y,x));
 			addEvent("Moving ship from ("+startr+","+startc+") to ("+display.getLocation().getRow()+","+display.getLocation().getCol()+")");
 			int rowchange = Math.abs(startr - (display.getLocation().getRow())); 
 			int colchange = Math.abs(startc - (display.getLocation().getCol()));
 			if(rowchange>=colchange)
 				display.addMovement(rowchange);
 			else
 				display.addMovement(colchange);
 			update();
 			return true;
 		}
 		else if(GridHelper.canMoveTo(display.getManager(), display, display.getOppositeOrientation(), y, x,display.getWidth())){
 			if(display.isMovableTileBeingShown()){
 				display.toggleMovable();
 			}
 			display.moveTo(new Location(y,x),display.getOppositeOrientation());
 			addEvent("Moving ship from ("+startr+","+startc+") to ("+display.getLocation().getRow()+","+display.getLocation().getCol()+")");
 			int rowchange = Math.abs(startr - (display.getLocation().getRow())); 
 			int colchange = Math.abs(startc - (display.getLocation().getCol()));
 			if(rowchange>=colchange)
 				display.addMovement(rowchange);
 			else
 				display.addMovement(colchange);
 			update();
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean attackGuns(int x, int y, boolean leftclick){
 		if(!attackGuns)
 			return false;
 		
 		if(!isShowingAttack())
 			return false;
 
 		MoveableEntity display = (MoveableEntity)this.display;			
 		
 		if(!tm.getTurn().canFireGuns(display))
 			return false;
 		
 		int startr = display.getLocation().getRow();
 		int startc = display.getLocation().getCol();
 		if(leftclick && GridHelper.canAttackTo(display.getManager(), display, y, x)){
 			if(display.isAttackTileBeingShown()){
 				display.toggleAttackRange();
 			}
 			addEvent("Gunning ship from ("+startr+","+startc+") to ("+display.getLocation().getRow()+","+display.getLocation().getCol()+")");
 			System.out.println("Gunning ship from ("+startr+","+startc+") to ("+display.getLocation().getRow()+","+display.getLocation().getCol()+")");
 			display.useGuns();
 			update();
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean attackMissile(int x, int y, boolean leftclick){
 		if(!attackMissiles)
 			return false;
 		
 		if(!isShowingAttack())
 			return false;
 
 		MoveableEntity display = (MoveableEntity)this.display;			
 		
 		if(!tm.getTurn().canFireMissiles(display))
 			return false;
 
 		int startr = display.getLocation().getRow();
 		int startc = display.getLocation().getCol();
 		if(leftclick && GridHelper.canAttackTo(display.getManager(), display, y, x)){
 			if(display.isAttackTileBeingShown()){
 				display.toggleAttackRange();
 			}
 			addEvent("Tomahawk ship from ("+startr+","+startc+") to ("+display.getLocation().getRow()+","+display.getLocation().getCol()+")");
 			System.out.println("Tomahawk ship from ("+startr+","+startc+") to ("+display.getLocation().getRow()+","+display.getLocation().getCol()+")");
 			display.useMissiles();
 			update();
 			return true;
 		}
 		return false;
 	}
 	
 	public void addEvent(String s){
 		if(events[events.length-1]==null){
 			for(int index=0;index<events.length-1;index++){
 				if(events[index]!=null){
 					events[index]=s;
 					return;
 				}
 			}
 		}
 		else{
 			for(int index=0;index<events.length-2;index++){
 				events[index]=events[index+1];
 			}
 			events[events.length-1]=s;
 		}	
 	}
 	
 	public void togglePinable(){
 		pinned = !pinned;
 		update();
 	}
 	
 }
