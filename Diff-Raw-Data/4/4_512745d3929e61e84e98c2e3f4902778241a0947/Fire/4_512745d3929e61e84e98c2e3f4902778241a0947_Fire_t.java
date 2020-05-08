 package com.anwpteuz.bomberman;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.Random;
 
 public class Fire extends GridObject implements Updateable {
 	
 	private int range;
 	private Direction direction;
 	private int millisLifetime = 1000;
 	private int millisLived;
 	private int millisSpreadTime = 200;
 	private boolean isParent;
 	private Color color = (new Random().nextInt(2) == 0) ? Color.RED : Color.YELLOW;
 	
 	// Images
 	private ArrayList<Image> images = new ArrayList<Image>();
 	private Image currentImage;
 	
 	/**
 	 * 
 	 * @param g The Game
 	 * @param dirX -1 0 1 x-wise
 	 * @param dirY -1 0 1 y-wise
 	 * @param range The number of Fires left to place in direction given
 	 */
 	public Fire(Game g, Direction dir, int range) {
 		super(g);
 		
 		this.range  = range;
 		this.direction = dir;
 		
 		// Add images
 		images.add(AssetsManager.getInstance().loadImage("fire_1"));
 		images.add(AssetsManager.getInstance().loadImage("fire_2"));
 		images.add(AssetsManager.getInstance().loadImage("fire_3"));
 		images.add(AssetsManager.getInstance().loadImage("fire_4"));
 		images.add(AssetsManager.getInstance().loadImage("fire_5"));
 		
 		// Set first image
 		currentImage = images.get(0);
 	}
 	
 	private void placeFireChild() {
 		Tile nextTile = getGame().getGrid().nextTile(this.getTile(), this.direction);
 		if(nextTile != null) {
 			GridObjectFactory.addFire(nextTile.getX(), nextTile.getY(), direction, range-1);
 		}
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		g.drawImage(currentImage, getTile().getX()*Grid.CELL_SIZE, getTile().getY()*Grid.CELL_SIZE, Grid.CELL_SIZE, Grid.CELL_SIZE, null);
 	}
 
 	@Override
 	public void update() {
 		millisLived += Game.targetTime;
 		
 		/**
 		 * Run placeFireChild only if all the following applies:
 		 * 1. Range isn't reached
 		 * 2. This ain't yet a parent
 		 * 3. It has lived long enough to spread
 		 */
 		if(range > 0 && !isParent && millisLived >= millisSpreadTime) { 
 			placeFireChild();
 			isParent = true;
 		}
 		
 		if(millisLived >= millisLifetime)
 			this.getTile().remove(this);
 		
 		float time = (millisLived / (float)millisLifetime);
		int imageIndex = (int)(time * images.size() * 2);
 		if(time > 0.5f) imageIndex = images.size() - imageIndex;
		if(imageIndex < 0) imageIndex = 0;
 		currentImage = images.get(imageIndex);
 	}
 }
