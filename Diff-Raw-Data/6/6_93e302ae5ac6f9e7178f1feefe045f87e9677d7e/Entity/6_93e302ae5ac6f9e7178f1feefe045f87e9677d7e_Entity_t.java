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
 
 import com.jpii.navalbattle.game.*;
 import com.jpii.navalbattle.game.entity.BattleShip;
 import com.jpii.navalbattle.pavo.*;
 
 public class Entity {
 	
 	private Location location = Location.Unknown;
 	private String tag;
 	public long lastUpdate = 0;
 	private int width, height;
 	private EntityManager manager;
 	private int id;
 	
 	public Entity(EntityManager em) {
 		manager = em;
 		init();
 	}
 	public Entity(EntityManager em,Location loc, int superId) {
 		manager = em;
 		location = loc;
 		try
 		{
 			moveTo(loc,true);
 		}
 		catch (Throwable throwable) {}
 		manager.addEntity(this);
 		init();
 		setId(superId);
 	}
 	
 	/**
 	 * Initialises the entity. This should never be called. If inheriting <code>Entity></code>, this method should probably be overriden.
 	 */
 	public void init() {
 		setWidth(1);
 		setHeight(1);
 	}
 	
 	/*
 	 * Actions:
 	 */
 	
 	/**
 	 * Rotates the entity.
 	 * @param akamai The rotation to apply to the entity. (e.g. Location.HALF_CIRCLE).
 	 */
 	public void rotateTo(int akamai) {
 		if (akamai == Location.HALF_CIRCLE) {
 			
 		}
 	}
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param r The row to move the entity to.
 	 * @param c The column to move the entity to.
 	 */
 	public void moveTo(int r, int c) {
 		moveTo(new Location(r,c));
 	}
 	/**
 	 * Sets the genericied id of the entity. This shouldn't have to be called by the client.
 	 * @param id The identifier to set the entity to.
 	 */
 	public void setId(int id) {
 		this.id = id;
 		for (int w = 0; w < getWidth(); w++) {
 			for (int h = 0; h < getHeight(); h++) {
 				Tile t = new Tile(this,location.getRow()+h, location.getCol()+w);
 				t.setId(new Id(id,w));
 				manager.setTile(location.getRow()+h, location.getCol()+w, t);
 				//System.out.println("efretgfd");
 			}
 		}
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
 	 * @param override Should current entities at that location be overidden?
 	 * @return A value indicating if the operation was sucessfull.
 	 */
 	public boolean moveTo(int r, int c, boolean override) {
 		return moveTo(new Location(r,c),override);
 	}
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param loc The location to move the entity to.
 	 * @param override Should current entities at that location be overriden?
 	 * @return A value indicating if the operation was sucessfull.
 	 */
 	public boolean moveTo(Location loc, boolean override) {
 		if (loc == null)
 			return false;
 		if (loc == Location.Unknown) {
 			hideEntity();
 			return true;
 		}
 		Tile<Entity> t = manager.getTile(loc);
 		if (t != null && ((t.getSuperId() != 0 || t.getEntity() != null) && override))
 			return false;
 		if (getWidth() + loc.getCol() + 1 >= PavoHelper.getGameWidth(manager.getWorld().getWorldSize())*2 ||
 				getHeight() + loc.getRow() + 1 >= PavoHelper.getGameHeight(manager.getWorld().getWorldSize())*2)
 			return false;
 		if (loc.getRow() < 0 || loc.getCol() < 0)
 			return false;
 		for (int w = 0; w < getWidth(); w++) {
 			for (int h = 0; h < getHeight(); h++) {
				//Tile<Entity> ttmp = (Tile<Entity>)manager.getTile(h+getLocation().getRow(), w+getLocation().getCol());
				Tile gd = new Tile(this,loc.getRow()+h, loc.getCol()+w);
				gd.setId(new Id(id,w));
				manager.setTile(loc.getRow()+h, loc.getCol()+w,gd);
 				//System.out.println("settile for " + w + "," + h);
 			}
 		}
 		for (int w = 0; w < getWidth(); w++) {
 			for (int h = 0; h < getHeight(); h++) {
 				Tile<Entity> ttmp = (Tile<Entity>)manager.getTile(h+getLocation().getRow(), w+getLocation().getCol());
 				manager.setTile(getLocation().getRow()+h, getLocation().getCol()+w,null);
 				//System.out.println("efretgfd");
 			}
 		}
 		//System.out.println("wincall");
 		setLocation(loc);
 		manager.getWorld().forceRender();
 		return true;
 	}
 	
 	private void hideEntity() {
 		for (int w = 0; w < getWidth(); w++) {
 			for (int h = 0; h < getHeight(); h++) {
 				Tile<Entity> ttmp = (Tile<Entity>)manager.getTile(h+getLocation().getRow(), w+getLocation().getCol());
 				manager.setTile(getLocation().getRow()+h, getLocation().getCol()+w,null);
 				//System.out.println("efretgfd");
 			}
 		}
 	}
 	
 	/**
 	 * Gets rid of the entity.
 	 */
 	public void truncate() {
 		if (getLocation() == null || getLocation() == Location.Unknown)
 			return;
 		Tile t = (new Tile(null,getLocation().getRow(),getLocation().getCol()));
 		t.setId(new Id(0,0));
 		manager.setTile(getLocation(), t);
 	}
 	
 	/**
 	 * Same as <code>truncate()</code>.
 	 */
 	public void dispose() {
 		truncate();
 	}
 	
 	/*
 	 * Attributes:
 	 */
 	
 	private void setLocation(Location loc) {
 		location = loc;
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
 	
 	public final void setHeight(int height) {
 		this.height = height;
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return height;
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
 		System.out.println(manager.battleShipId);
 		new BattleShip(manager,location,manager.battleShipId);
 	}
 	
 	public void onHit(Entity attackingEntity) {
 		
 	}
 	
 	public void onAttack(Entity entityBeingAttacked) {
 		
 	}
 	
 	public void onUpdate() {
 		
 	}
 }
