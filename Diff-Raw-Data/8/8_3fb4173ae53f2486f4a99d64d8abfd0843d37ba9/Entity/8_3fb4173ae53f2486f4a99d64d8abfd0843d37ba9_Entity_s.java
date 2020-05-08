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
 
 package com.jpii.navalbattle.pavo.grid;
 
 import java.awt.Point;
 import java.io.Serializable;
 
 
 import com.jpii.navalbattle.pavo.*;
 
 public class Entity implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private Location location = Location.Unknown;
 	private String tag = "";
 	public long lastUpdate = 0;
 	private int width;
 	private byte teamColor = -1;
 	private EntityManager manager;
 	private GridedEntityTileOrientation id;
 	private byte ORIENTATION_BUFFER_POSITION = GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT;
 	public String imgLocation;
 	private static int ENTITYMASTERRECORDSYSTEMPLEASEDONOTTOUCHTHIS = 0;
 	protected byte handle = -1;
 	protected boolean startpos = false;
 	protected boolean disposed = false;
 	
 	public Entity(EntityManager em) {
 		manager = em;
 		init();
 	}
 	
 	public byte getCurrentOrientation() {
 		return ORIENTATION_BUFFER_POSITION;
 	}
 	
 	public byte getOppositeOrientation(){
 		if(getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT)
 			return GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM;
 		else
 			return GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT;
 	}
 	
 	public void setTeamColor(byte team) {
 		if (teamColor != team) {
 			teamColor = team;
 			setId(id);
 		}
 	}
 	
 	public byte getTeamColor() {
 		return teamColor;
 	}
 	
 	public int getCurrentId() {
 		return id.memCall(ORIENTATION_BUFFER_POSITION)[0];
 	}
 	
 	public String getTag() {
 		return tag;
 	}
 	
 	public void setTag(String tag) {
 		this.tag = tag;
 	}
 	
 	public Entity(EntityManager em,Location loc, GridedEntityTileOrientation id,byte orientation) {
 		manager = em;
 		location = loc;
 		//teamColor = Game.Settings.rand.nextByte();
 		ORIENTATION_BUFFER_POSITION = orientation;
 		try {
 			moveTo(loc,true);
 		}
 		catch (Throwable throwable) {
 			
 		}
 		manager.addEntity(this);
 		init();
 		if(getCurrentOrientation()==GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM){
 			location = new Location(loc.getRow()-(getWidth()-1),loc.getCol());
 			startpos = true;
 		}
 		setId(id);
 	}
 	/**
 	 * Initializes the entity. This should never be called. If inheriting <code>Entity></code>, this method should probably be overriden.
 	 */
 	public void init() {
 		setWidth(1);
 		setTag("entity-"+ENTITYMASTERRECORDSYSTEMPLEASEDONOTTOUCHTHIS++);
 	}
 	
 	/*
 	 * Actions:
 	 */
 	
 	/**
 	 * Rotates the entity.
 	 * @param akamai The rotation to apply to the entity. (e.g. Location.HALF_CIRCLE).
 	 */
 	public void rotateTo(byte akamai) { //akamai equals rotate to
 		if (akamai == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM){//ORIENTATION_BUFFER_POSITION equals getCurrentOrientation()
 				for (int h = 0; h < getHeight(); h++) { //sets the entity to null
 					manager.setTile(location.getRow()+h-3,location.getCol(), null);
 				}
 				ORIENTATION_BUFFER_POSITION = akamai; //makes the current position the position rotating to
 				for (int w = 0; w < getWidth(); w++) { //places the entity in the correct position
 					Tile<Entity> t = new Tile<Entity>(this,location.getRow(),location.getCol());
 					t.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],w));
 					manager.setTile(location.getRow(),location.getCol()+w, t);
 				}
 				manager.getWorld().forceRender();
 			}
 		}
 		else if (akamai == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) { //akamai equals rotate to
 			if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {//ORIENTATION_BUFFER_POSITION equals getCurrentOrientation()
 				for (int w = 0; w < getWidth(); w++) {  //sets the entity to null
 					manager.setTile(location.getRow(),location.getCol()+w, null);
 				}
 				ORIENTATION_BUFFER_POSITION = akamai;//makes the current position the position rotating to
 				for (int h = 0; h < getHeight(); h++) {//places the entity in the correct position
 					Tile<Entity> t = new Tile<Entity>(this,location.getRow(),location.getCol());
 					t.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],h));
 					manager.setTile(location.getRow()+h-3,location.getCol(), t);
 				}
 				manager.getWorld().forceRender();
 			}
 		}
 	}
 	
 	public Tile<?>[] getTiles() {
 		Tile<?>[] tiles = new Tile[getWidth()];
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT)
 			for (int w = 0; w < getWidth(); w++) {
 				tiles[w] = manager.getTile(location.getRow(), location.getCol()+w);
 			}
 		else if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM)
 			for (int w = 0; w < getWidth(); w++) {
 				tiles[w] = manager.getTile(location.getRow()+w-getHeight()+1, location.getCol());
 			}
 		return tiles;
 	}
 	
 	//public void onTeamColorBeingDrawn(Area a) {
 	//	a.add(new Area(new Rectangle2D.Float(0,0,80,50)));
 	//}
 	
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param r The row to move the entity to.
 	 * @param c The column to move the entity to.
 	 */
 	public void moveTo(int r, int c) {
 		moveTo(new Location(r,c));
 	}
 	
 	/**
 	 * Sets the generic id of the entity. This shouldn't have to be called by the client.
 	 * @param id The identifier to set the entity to.
 	 */
 	public void setId(GridedEntityTileOrientation id) {
 		this.id = id;
 		if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int w = 0; w < getWidth(); w++) {
 				Tile<Entity> t22 = new Tile<Entity>(this,location.getRow(),location.getCol()+w);
 				int[] vbws = id.memCall(ORIENTATION_BUFFER_POSITION);
 				int vbw = vbws[0];
 				t22.setId(new Id(vbw,w));
 				manager.setTile(location.getRow(),location.getCol()+w, t22);
 			}
 		}
 		else if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int h = 0; h < getWidth(); h++) {
 				Tile<Entity> t22 = new Tile<Entity>(this,location.getRow()-(getWidth()-1)+h,location.getCol());
 				t22.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],h));
 				manager.setTile(location.getRow()-(getWidth()-1)+h,location.getCol(), t22);
 			}
 		}
 		manager.getWorld().forceRender();
 	}
 	
 	public boolean canDoRotation(byte desiredRotation) {
 		return true;
 	}
 	
 	public boolean canDoMoveTo(Location desiredLocation) {
 		return true;
 	}
 	
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param loc The location to move the entity to.
 	 */
 	public void moveTo(Location loc) {
 		moveTo(loc,true);
 	}
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param r The row to move the entity to.
 	 * @param c The column to move the entity to.
 	 * @param override Should current entities at that location be overridden?
 	 * @return A value indicating if the operation was successful.
 	 */
 	public boolean moveTo(int r, int c, boolean override) {
 		return moveTo(new Location(r,c),override);
 	}
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param loc The location to move the entity to.
 	 * @param override Should current entities at that location be overridden?
 	 * @return A value indicating if the operation was successful.
 	 */
 	public boolean moveTo(Location loc, boolean override) {
 		if (loc == null)
 			return false;
 		if (loc == Location.Unknown) {
 			hideEntity();
 			return true;
 		}
 		isHide = false;
 		Tile<Entity> t = manager.getTile(loc);
 		if (t != null && ((t.getSuperId() != 0 || t.getEntity() != null) && !override)){
 			return false;
 		}
 		if (getWidth() + loc.getCol() + 1 >= PavoHelper.getGameWidth(manager.getWorld().getWorldSize())*2 ||
 				getHeight() + loc.getRow() + 1 >= PavoHelper.getGameHeight(manager.getWorld().getWorldSize())*2){
 			return false;
 		}
 		if (loc.getRow() < 0 || loc.getCol() < 0){
 			return false;
 		}
 		if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int w = 0; w < getWidth(); w++) {
 				manager.setTile(location.getRow(),location.getCol()+w, null);
 			}
 			for (int w = 0; w < getWidth(); w++) {
 				Tile<Entity> t22 = new Tile<Entity>(this,loc.getRow(),loc.getCol()+w);
 				t22.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],w));
 				manager.setTile(loc.getRow(),loc.getCol()+w, t22);
 			}
 			manager.getWorld().forceRender();
 		}
 		else if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int h = 0; h < getHeight(); h++) {
 				manager.setTile(location.getRow()+h-getHeight()+1,location.getCol(),null);
 			}
 			for (int h = 0; h < getHeight(); h++) {
 				Tile<Entity> t22 = new Tile<Entity>(this,loc.getRow()+h-getHeight()+1,loc.getCol());
 				t22.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],h));
 				manager.setTile(loc.getRow()+h-getHeight()+1,loc.getCol(), t22);
 			}
 			manager.getWorld().forceRender();
 		}
 		Location swap2 = getLocation();
 		setLocation(loc);
 		manager.getWorld().forceRender();
 		onMove(swap2);
 		return true;
 	}
 	Location originality = Location.Unknown;
 	public Location destiny = null;
 	
 	public Point currentLocation = null;
 	public boolean readyForMove = false;
 	
 	public Location getOriginalLocation() {
 		return originality;
 	}
 	
 	public void animatedMoveTo(Location loc, float speed) {
 		
 		readyForMove = false;
 		if (ORIENTATION_BUFFER_POSITION != GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT)
 			return;
 		
 		originality = getLocation();
 		setLocation(loc);
 		destiny = loc;
 		
 		getManager().getWorld().setMotionEntity(this);
 
 		readyForMove = true;
 	}
 	
 	public boolean moveTo(Location loc, byte position){
 		if (loc == null)
 			return false;
 		if (loc == Location.Unknown) {
 			hideEntity();
 			return true;
 		}
 		if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int w = 0; w < getWidth(); w++) {
 				manager.setTile(location.getRow(),location.getCol()+w, null);
 				//(byte)0x2f1d
 			}
 		}
 		else if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int h = 0; h < getHeight(); h++) {
 				manager.setTile(location.getRow()+h-getHeight()+1,location.getCol(),null);
 			}
 		}
		ORIENTATION_BUFFER_POSITION = getOppositeOrientation();
 		
 		isHide = false;
 		
 		if (getWidth() + loc.getCol() + 1 >= PavoHelper.getGameWidth(manager.getWorld().getWorldSize())*2 ||
 				getHeight() + loc.getRow() + 1 >= PavoHelper.getGameHeight(manager.getWorld().getWorldSize())*2){
 			return false;
 		}
 		if (loc.getRow() < 0 || loc.getCol() < 0){
 			return false;
 		}
 		if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int w = 0; w < getWidth(); w++) {
 				Tile<Entity> t22 = new Tile<Entity>(this,loc.getRow(),loc.getCol()+w);
 				t22.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],w));
 				manager.setTile(loc.getRow(),loc.getCol()+w, t22);
 			}
 			manager.getWorld().forceRender();
 		}
 		else if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int h = 0; h < getHeight(); h++) {
 				Tile<Entity> t22 = new Tile<Entity>(this,loc.getRow()+h-getHeight()+1,loc.getCol());
 				t22.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],h));
 				manager.setTile(loc.getRow()+h-getHeight()+1,loc.getCol(), t22);
 			}
 			manager.getWorld().forceRender();
 		}
 		Location swap2 = getLocation();
 		setLocation(loc);
 		manager.getWorld().forceRender();
 		onMove(swap2);
 		return true;
 	}
 	
 	public void rotateNext() {
 		byte gb = getCurrentOrientation();
 		if (gb == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT)
 			gb = GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM;
 		else
 			gb = GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT;
 		rotateTo(gb);
 	}
 	
 	boolean isHide = false;
 	
 	public boolean isHidden() {
 		return isHide;
 	}
 
 	public void hideEntity() {
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			isHide = true;
 			int row = getLocation().getRow();
 			int col = getLocation().getCol();
 			for (int c = 0; c < getWidth(); c++) {
 				manager.setTile(new Location(row,col+c),null);
 			}
 			manager.getWorld().forceRender();
 		}
 	}
 	
 	/**
 	 * Gets rid of the entity.
 	 */
 	public void truncate() {
 		if (getLocation() == null || getLocation() == Location.Unknown)
 			return;
 		int row = getLocation().getRow();
 		int col = getLocation().getCol();		
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int c = 0; c < width; c++) {
 				Tile<Entity> t=null;
 				manager.setTile(new Location(row,col+c), t);
 			}
 		}
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int c = 0; c < width; c++) {
 				Tile<Entity> t=null;
 				manager.setTile(new Location(row+c,col), t);
 			}
 		}
 	}
 	
 	/**
 	 * Same as <code>truncate()</code>.
 	 * @return true
 	 */
 	public boolean dispose() {
 		truncate();
 		getManager().removeEntity(this);
 		disposed = true;
 		return true;
 	}
 	
 	/*
 	 * Attributes:
 	 */
 	
 	public void setLocation(Location loc) {
 		location = loc;
 	}
 	
 	public EntityManager getManager(){
 		return manager;
 	}
 	
 	/**
 	 * Gets the upper left tile of the Entity's location.
 	 * @return The location. Could be "Unknown", if the Entity is not in the Grid.
 	 */
 	public Location getLocation() {
 		return location;
 	}
 	
 	public final void setWidth(int width) {
 		this.width = width;
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return width;
 	}
 	
 	/*
 	 * Events:
 	 */
 	
 	/**
 	 * Occurs when the mouse moves over the entity.
 	 * @param x The local x location.
 	 * @param y The local y location.
 	 */
 	public void onMouseMove(int x, int y) {
 	}
 	
 	public void onMove(Location original) {
 		
 	}
 	
 	public void onMouseDown(int x, int y, boolean leftClick) {
 		
 	}
 	
 	public void onHit(Entity attackingEntity) {
 		
 	}
 	
 	public void onAttack(Entity entityBeingAttacked) {
 		
 	}
 	
 	public void onUpdate(long tickTime) {
 		
 	}
 	
 	public byte getHandle(){
 		return handle;
 	}
 }
