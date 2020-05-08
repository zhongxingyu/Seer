 /* Acid - Provides a Java cell API to display fancy cell boxes.
  * Copyright (C) 2013  Miguel Gonzalez
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
  */
 
 package de.myreality.acidsnake.world;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import de.myreality.acidsnake.core.Player;
 import de.myreality.acidsnake.core.SimplePlayer;
 import de.myreality.acidsnake.core.SimpleSnake;
 import de.myreality.acidsnake.core.Snake;
 import de.myreality.acidsnake.util.Accelerator;
 import de.myreality.acidsnake.util.WorldBinder;
 
 /**
  * World class which handles the entire game
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 1.0
  * @version 1.0
  */
 public class SimpleWorld implements World {
 	
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	
 	private Snake snake;
 	
 	private Player player;
 	
 	private Set<WorldListener> listeners;
 	
 	private int width, height;
 	
 	WorldEntity[][] area;
 	
 	private Set<WorldEntity> entities;
 	
 	private Map<WorldEntityType, Set<WorldEntity> > types;
 	
 	private WorldBinder binder;
 	
 	private Accelerator snakeAccelerator;
 	
 	private boolean paused;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	
 	public SimpleWorld(int width, int height) {
 		this.width = width;
 		this.height = height;
 		listeners = new HashSet<WorldListener>();
 		entities = new HashSet<WorldEntity>();
 		area = new WorldEntity[width][height];
 		binder = new WorldBinder(this);
 		types = new HashMap<WorldEntityType, Set<WorldEntity> >();
 	}
 
 	// ===========================================================
 	// Getters and Setters
 	// ===========================================================
 
 	// ===========================================================
 	// Methods from Superclass
 	// ===========================================================
 	
 	@Override
 	public Snake getSnake() {
 		return snake;
 	}
 
 	@Override
 	public void reset() {
 		
 		Set<WorldEntity> copy = new HashSet<WorldEntity>(entities);
 		
 		for (WorldEntity entity : copy) {
 			removeEntity(entity);
 		}
 		
 		build();
 	}
 
 	@Override
 	public Player getPlayer() {
 		return player;
 	}
 
 	@Override
 	public boolean hasEntity(int indexX, int indexY) {
 		return getEntity(indexX, indexY) != null;
 	}
 
 	@Override
 	public WorldEntity getEntity(int indexX, int indexY) {
 		indexX = binder.bindIndexX(indexX);
 		indexY = binder.bindIndexY(indexY);
 		return area[indexX][indexY];
 	}
 
 	@Override
 	public boolean putEntity(int indexX, int indexY, WorldEntity entity) {
 		
 		indexY = binder.bindIndexY(indexY);
 			
 		WorldEntity old = getEntity(indexX, indexY);
 		boolean oldExists = old != null;
 		boolean isTheSame = oldExists && old.equals(entity);
 		
 		if (oldExists && !isTheSame) {
 			removeEntity(old);
 		} 
 		
 		if (!oldExists || !isTheSame) {
 
 			removeEntity(entity);
 			
 			for (WorldListener listener : listeners) {
 				listener.onPut(indexX, indexY, entity, this);
 			}
 			
 			entities.add(entity);
 			area[indexX][indexY] = entity;
 			
 			Set<WorldEntity> targets = types.get(entity.getType());
 			
 			if (targets == null) {
 				targets = new HashSet<WorldEntity>();
 				types.put(entity.getType(), targets);
 			}
 			
 			targets.add(entity);
 			
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 
 	@Override
 	public void removeEntity(WorldEntity entity) {
 		if (entity != null && entities.contains(entity)) {
 			
 			int indexX = entity.getIndexX(), indexY = entity.getIndexY();
 			
 			indexX = binder.bindIndexX(indexX);
 			indexY = binder.bindIndexY(indexY);
 			
 			for (WorldListener listener : listeners) {
 				listener.onRemove(indexX, indexY, entity, this);
 			}
 			
 			entities.remove(entity);
 			
 			Set<WorldEntity> targets = types.get(entity.getType());
 			targets.remove(entity);			
 			if (targets.isEmpty()) {
 				types.remove(targets);
 			}
 
 			if (validIndex(indexX, indexY)) {
 				area[indexX][indexY] = null;
 			}
 		}
 	}
 
 	@Override
 	public void addListener(WorldListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 
 	@Override
 	public void build() {
 		player = new SimplePlayer();
 		snake = new SimpleSnake(5, 5, this);	
 		snakeAccelerator = new Accelerator(snake);
 		
 		for (WorldEntityType entityType : WorldEntityType.values()) {
 			snake.addListener(entityType);
 		}
 		
 		snake.build();
 	}
 
 	@Override
 	public int getWidth() {
 		return width;
 	}
 
 	@Override
 	public int getHeight() {
 		return height;
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	
 	private boolean validIndex(int indexX, int indexY) {
 		return indexX >= 0 && indexX < getWidth() &&
 			   indexY >= 0 && indexY < getHeight();
 	}
 
 	@Override
 	public void update(float delta) {
		snakeAccelerator.setSpeedRate(14 + (player.getLevel() / 3));
 		snakeAccelerator.update(delta);
 	}
 
 	@Override
 	public int getEntityCount(WorldEntityType type) {
 		Set<WorldEntity> targets = types.get(type);
 		
 		return targets != null ? targets.size() : 0;
 	}
 
 	@Override
 	public boolean hasEntity(WorldEntity entity) {
 		return getEntityCount(entity.getType()) > 0;
 	}
 
 	@Override
 	public Set<WorldEntity> getEntitiesOfType(WorldEntityType type) {
 		return types.get(type) != null ? types.get(type) : new HashSet<WorldEntity>();
 	}
 
 	@Override
 	public void setPaused(boolean paused) {
 		snakeAccelerator.setPaused(paused);
 		player.setPaused(paused);
 		this.paused = paused;
 		
 		for (WorldListener listener : listeners) {
 			listener.onPaused(paused);
 		}
 	}
 	
 	@Override
 	public boolean isPaused() {
 		return paused;
 	}
 
 	// ===========================================================
 	// Inner classes
 	// ===========================================================
 }
