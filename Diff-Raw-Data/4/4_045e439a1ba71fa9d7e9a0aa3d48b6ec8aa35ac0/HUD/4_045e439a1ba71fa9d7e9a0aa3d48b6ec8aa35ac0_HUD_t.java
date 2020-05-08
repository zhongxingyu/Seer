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
 import com.jpii.navalbattle.game.turn.DamageCalculator;
 import com.jpii.navalbattle.game.turn.TurnManager;
 import com.jpii.navalbattle.gui.MainMenuWindow;
 
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
 		setToolTip("This is the HUD.");
 		right = new RightHud(this,width,height);
 		mid = new MidHud(this,tm,parent);
 		left = new LeftHud(height);
 		setTitleVisiblity(false);
 		setVisible(false);
 		update();
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
 					move.toggleMoveable();
 					move.toggleMoveable();
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
 				move.toggleMoveable();
 			}
			move.moveTo(new Location(y,x));
//			move.animatedMoveTo(new Location(y,x), 0.45f);
 			System.out.println("[chat] Moving ship from ("+startr+","+startc+") to ("+move.getLocation().getRow()+","+move.getLocation().getCol()+")");
 			int rowchange = Math.abs(startr - (move.getLocation().getRow())); 
 			int colchange = Math.abs(startc - (move.getLocation().getCol()));
 			if(rowchange>=colchange)
 				move.addMovement(rowchange);
 			else
 				move.addMovement(colchange);
 			easterEgg16(y,x);
 			update();
 			return true;
 		}
 		else if(GridHelper.canMoveTo(move.getManager(), move, move.getOppositeOrientation(), y, x,move.getWidth())){
 			if(move.isMovableTileBeingShown()){
 				move.toggleMoveable();
 			}
 			move.moveTo(new Location(y,x),move.getOppositeOrientation());
 			System.out.println("[chat] Moving ship from ("+startr+","+startc+") to ("+move.getLocation().getRow()+","+move.getLocation().getCol()+")");
 			int rowchange = Math.abs(startr - (move.getLocation().getRow())); 
 			int colchange = Math.abs(startc - (move.getLocation().getCol()));
 			if(rowchange>=colchange)
 				move.addMovement(rowchange);
 			else
 				move.addMovement(colchange);
 			easterEgg16(y,x);
 			update();
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean attackGuns(int x, int y, boolean leftclick){
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
 					System.out.println("[chat] You can't attack your own team");
 					return false;
 				}
 			}
 		}
 		if(leftclick && GridHelper.canAttackPrimaryTo(move.getManager(), move, y, x)){
 			if(move.isPrimaryTileBeingShown()){
 				move.togglePrimaryRange();
 			}
 			System.out.println("[chat] Gunning ship from ("+startr+","+startc+") to ("+y+","+x+")");
 			if(there!=null){
 				DamageCalculator.doPrimaryDamage(move, there);
 			}
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
 	
 	private void easterEgg16(int r, int c){
 		if(r == 1 && c == 16){
 			if(MainMenuWindow.spg.getStageManager().getStageNumber()>10){
 				if(isPrime(MainMenuWindow.spg.getStageManager().getStageNumber())){
 					if(move.getHandle()==31){
 						System.out.println("The time has come!");
 						MainMenuWindow.spg.getStageManager().easterEgg16();
 					}
 				}
 			}
 		}
 	}
 	
 	private boolean isPrime(int ask){
 		if(ask==1)
 			return false;
 		boolean[] tests = new boolean[ask];
 		for(int index = 0; index<ask; index++){
 			tests[index]=true;
 		}
 		
 		int size = 2;
 		while(size<ask){
 			for(int index = 1; index<=ask; index++){
 				if(index!=size)
 					if(index%size==0)
 						tests[index-1]=false;
 			}
 			size++;
 		}
 		return tests[ask-1];
 	}
 	
 }
