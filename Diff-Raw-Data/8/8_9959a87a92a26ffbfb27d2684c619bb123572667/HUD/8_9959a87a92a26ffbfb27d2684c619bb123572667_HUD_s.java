 package com.jpii.navalbattle.game.gui;
 
 import java.awt.Graphics2D;
 
 import com.jpii.navalbattle.game.entity.MoveableEntity;
 import com.jpii.navalbattle.game.entity.PortEntity;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.GridHelper;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.grid.Tile;
 import com.jpii.navalbattle.pavo.gui.NewWindowManager;
 import com.jpii.navalbattle.pavo.gui.controls.PWindow;
 import com.jpii.navalbattle.turn.DamageCalculator;
 import com.jpii.navalbattle.turn.TurnManager;
 
 public class HUD extends PWindow{
 	
 	TurnManager tm;
 	Entity display;
 	MoveableEntity move;
 	boolean pinned = true;
 	RightHud right;
 	MidHud mid;
 	LeftHud left;
 
 	 public HUD(NewWindowManager parent,TurnManager tm,int x, int y, int width, int height){
 		super(parent, x, y, width, height);
 		this.tm = tm;
 		System.out.println(tm);
 		setToolTip("This is the HUD.");
 		right = new RightHud(this,width,height);
 		mid = new MidHud(this,tm,parent);
 		left = new LeftHud(height);
 		setTitleVisiblity(false);
 		setVisible(false);
 		update();
 ////		int ui1 = ui1 = Game.Settings.rand.nextInt();
 ////		int ui2 = ui2 = ui1 + 5;
 ////		int ui3 = ui3 = (ui2 & 2) + ui1;
 ////		int ui4 = ui4 = ui3 >> 2;
 ////		int ui5 = ui5 = ui4 + 2;
 ////		int ui6 = ui6 = ui5 / 2;
 ////		int ui7 = ui7 = ui6 + 5;
 ////		int ui8 = ui8 = ui7 * -1;
 //		System.out.println("ui8"+ui8);
 	 }
 	 
 	 
 	
 	public void paint(Graphics2D g) {
 		super.paint(g);
 		g.fillRect(0,0,getWidth(),getHeight());
 		if(right!=null&&mid!=null&&left!=null){
 			right.draw(g);
 			mid.draw(g);
 			left.draw(g);
 		}
 		
 		if(tm!=null)
 			if(tm.getTurn()!=null)
 				if(tm.getTurn().getPlayer()!=null)
 					g.drawString(""+tm.getTurn().getPlayer().name,(width/3)+25,25);
 	}
 	
 	public void setEntity(Entity e){
 		display = e;
 		if(display!=null&&display.getHandle()%10 == 1)
 			move = (MoveableEntity)display;
 		else
 			move = null;
 		right.setEntity(e,move);
 		mid.setEntity(e,move);
 		update();
 	}
 	
 	public void update(){
 		right.update();
 		mid.update();
 		if(display != null){
 			setVisible(true);
 			if(move!=null){
 				if(move.isMovableTileBeingShown()){
 					move.toggleMovable();
 					move.toggleMovable();
 				}
 				if(move.isPrimaryTileBeingShown()){
 					move.togglePrimaryRange();
 					move.togglePrimaryRange();
 				}
 				if(move.isSecondaryTileBeingShown()){
 					move.toggleSecondaryRange();
 					move.toggleSecondaryRange();
 				}					
 			}
 		}
 		else if(pinned)
 			setVisible(true);
 		else
 			setVisible(false);
 		repaint();
 	}
 	
 	public boolean isShowingMove(){
 		boolean flag = false;
 		if(display!=null)
 			if(move!=null)
 				flag = move.isMovableTileBeingShown();
 		return flag;
 	}
 	
 	public boolean hudClick(int x, int y, boolean leftclick){
 		return moveShip(x,y,leftclick) || attackGuns(x,y,leftclick) || attackMissile(x,y,leftclick);
 	}
 	
 	private boolean moveShip(int x, int y, boolean leftclick){	
 		if(!isShowingMove())
 			return false;		
 		
 		if(!tm.getTurn().canmoveEntity(move))
 			return false;
 
 		int startr = move.getLocation().getRow();
 		int startc = move.getLocation().getCol();
 		if(leftclick && GridHelper.canMoveTo(move.getManager(), move, move.getCurrentOrientation(), y, x,move.getWidth())){
 			if(move.isMovableTileBeingShown()){
 				move.toggleMovable();
 			}
 			move.moveTo(new Location(y,x));
 			System.out.println("[chat] Moving ship from ("+startr+","+startc+") to ("+move.getLocation().getRow()+","+move.getLocation().getCol()+")");
 			int rowchange = Math.abs(startr - (move.getLocation().getRow())); 
 			int colchange = Math.abs(startc - (move.getLocation().getCol()));
 			if(rowchange>=colchange)
 				move.addMovement(rowchange);
 			else
 				move.addMovement(colchange);
 			update();
 			return true;
 		}
 		else if(GridHelper.canMoveTo(move.getManager(), move, move.getOppositeOrientation(), y, x,move.getWidth())){
 			if(move.isMovableTileBeingShown()){
 				move.toggleMovable();
 			}
 			move.moveTo(new Location(y,x),move.getOppositeOrientation());
 			System.out.println("[chat] Moving ship from ("+startr+","+startc+") to ("+move.getLocation().getRow()+","+move.getLocation().getCol()+")");
 			int rowchange = Math.abs(startr - (move.getLocation().getRow())); 
 			int colchange = Math.abs(startc - (move.getLocation().getCol()));
 			if(rowchange>=colchange)
 				move.addMovement(rowchange);
 			else
 				move.addMovement(colchange);
 			update();
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean attackGuns(int x, int y, boolean leftclick){
 		System.out.println("guns");
 		if(move==null || !move.isPrimaryTileBeingShown())
 			return false;	
 		if(!tm.getTurn().canFireGuns(move))
 			return false;
 		int startr = move.getLocation().getRow();
 		int startc = move.getLocation().getCol();
 		Tile<Entity> temp = move.getManager().getTile(y,x);
 		MoveableEntity there = null;
 		Entity e = null;
 		if(temp!=null){
 			e = temp.getEntity();
 			if(e.getHandle()%10 == 1){
 			there = (MoveableEntity)e;
 				if(tm.getTurn().getPlayer().myEntity(there)){
					System.out.println("[chat] You can;t attack your own team");
 					return false;
 				}
 			}
 		}
 		if(leftclick && GridHelper.canAttackPrimaryTo(move.getManager(), move, y, x)){
 			if(move.isPrimaryTileBeingShown()){
 				move.togglePrimaryRange();
 			}
 			System.out.println("[chat] Gunning ship from ("+startr+","+startc+") to ("+y+","+x+")");
 			if(there!=null)
 				DamageCalculator.doPrimaryDamage(move, there);
 			if(e.getHandle()==2){
 				PortEntity attacked = (PortEntity)e;
 				DamageCalculator.doPrimaryDamage(move, attacked);
 			}
 			update();
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean attackMissile(int x, int y, boolean leftclick){
 		System.out.println("missile");
 		if(move==null || !move.isSecondaryTileBeingShown())
 			return false;
 		
 		if(!tm.getTurn().canFireMissiles(move))
 			return false;
 
 		int startr = move.getLocation().getRow();
 		int startc = move.getLocation().getCol();
 		Tile<Entity> temp = move.getManager().getTile(y,x);
 		MoveableEntity there = null;
 		Entity e = null;
 		if(temp!=null){
 			e = temp.getEntity();
 			if(e.getHandle()%10 == 1){
 			there = (MoveableEntity)e;
 				if(tm.getTurn().getPlayer().myEntity(there)){
 					System.out.println("[chat] You can;t attack your own team");
 					return false;
 				}
 			}
 		}
 		if(leftclick && GridHelper.canAttackSecondaryTo(move.getManager(), move, y, x)){
 			if(move.isSecondaryTileBeingShown()){
 				move.toggleSecondaryRange();
 			}
 			System.out.println("[chat] Tomahawk ship from ("+startr+","+startc+") to ("+y+","+x+")");
 			if(there!=null)
 				DamageCalculator.doSecondaryDamage(move, there);
 			if(e.getHandle()==2){
 				PortEntity attacked = (PortEntity)e;
 				DamageCalculator.doSecondaryDamage(move, attacked);
 			}
 			update();
 			return true;
 		}
 		return false;
 	}
 	
 	public void togglePinable(){
 		pinned = !pinned;
 		update();
 	}
 	
 	public MidHud getMid(){
 		return mid;
 	}
 	
 }
