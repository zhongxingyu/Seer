 package stkl.spectropolarisclient;
 
 import java.io.DataInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 public class Model {
 	private ArrayList<GameCharacter> d_characters;
 	private ArrayList<Bullet> d_bullets;
 	private int d_numOfBullets;
 	private Player d_player;
 	
 	// tileMap with blocks (true means block, false means noblock)
 	private boolean[][] d_tileMap;
 	private final int d_tileSize = 10;
 	private Paint d_blockPaint;
 	
 	private final int d_mapWidth = 800;
 	private final int d_mapHeight = 768;
 	
 	// pickups
 	private ArrayList<HealthPickup> d_health;
 	private int d_lastNumOfHealthPickups;
 	
 	private ArrayList<AmmoPickup> d_ammo;
 	private int d_lastNumOfAmmoPickups;
 	
 	private GameActivity d_context;
 	private float d_scale;
 	
 	private Point d_motionOrigin;
 	private float d_motionControlX;
 	private float d_motionControlY;
 	private Point d_shootOrigin;
 	private float d_shootControlX;
 	private float d_shootControlY;	
 	
 	private Paint d_borderPaint;
 	
 	public Model(GameActivity context, int color) {
 		Paint playerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		playerPaint.setColor(color);
 		d_player = new Player(10, 10, playerPaint);
 		d_characters = new ArrayList<GameCharacter>();
 		
 		d_bullets = new ArrayList<Bullet>();
 		d_numOfBullets = 0;
 		
 		d_tileMap = new boolean[d_mapHeight / d_tileSize][d_mapWidth / d_tileSize];
 		d_blockPaint = new Paint();
 		
 		// pickups
 		d_health = new ArrayList<HealthPickup>();
 		d_lastNumOfHealthPickups = 0;
 		
 		d_ammo = new ArrayList<AmmoPickup>();
 		d_lastNumOfAmmoPickups = 0;
 		
 		//d_blocks = new ArrayList<Block>();
 		d_motionOrigin = new Point(-1, -1);
 		d_shootOrigin = new Point(-1, -1);
 		
 		d_borderPaint = new Paint();
 		d_borderPaint.setStyle(Paint.Style.STROKE);
 		
 		d_context = context;
 		
 		try {		
 			InputStream is = context.getResources().openRawResource(R.raw.map);
 			DataInputStream file = new DataInputStream(is);
 			
 			//int numOfBlocks = file.readInt();
 			
 			//for(int idx = 0; idx != numOfBlocks; ++idx) {
 			//	int x = file.readInt();
 			//	int y = file.readInt();
 			//	int width = file.readInt();
 			//	int height = file.readInt();
 				
 			//	if(width > 0 && height > 0 && x + width < d_mapWidth && y + height < d_mapHeight)
 			//		addBlock(x, y, width, height);
 			//}
 			
 			for(int y = 0; y != d_mapHeight / d_tileSize; ++y) {
 				for(int x = 0; x != d_mapWidth / d_tileSize; ++x) {
 					d_tileMap[y][x] = file.readBoolean();
 				}
 			}
 			
 			file.close();
 			
 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		}
 		
 		float numOfPixels = 500;
 		float minScreenDimension = Math.min(d_context.centerHorizontal() * 2, d_context.centerVertical() * 2);
 		
 		d_scale = minScreenDimension / numOfPixels;
 	}
 	
 	public void addBlock(int x, int y, int width, int height) {
 		// add blocks to the effected part of the map
 		for(int yIdx = y / d_tileSize; yIdx != (height + y) / d_tileSize; ++yIdx) {
 			for(int xIdx = x / d_tileSize; xIdx != (width + x) / d_tileSize; ++xIdx)
 				d_tileMap[yIdx][xIdx] = true;
 		}
 	}
 	
 	public void setMotionOrigin(float x, float y) {
 		d_motionOrigin.set((int)x, (int)y);
 	}
 
 	public void setShootOrigin(float x, float y) {
 		d_shootOrigin.set((int)x, (int)y);
 	}
 	
 	public void setMotionControls(float controlX, float controlY) {
 		d_motionControlX = controlX;
 		d_motionControlY = controlY;
 	}
 	
 	public void setShootControls(float controlX, float controlY) {
 		d_shootControlX = controlX;
 		d_shootControlY = controlY;
 	}
 	
 	public void addGameCharacter(GameCharacter character) {
 		d_characters.add(character);
 	}
 	
 	public Bullet addBullet() {
 		synchronized(d_bullets) {
 			if(d_numOfBullets == d_bullets.size()) {
 				Bullet newBullet = new Bullet();
 				d_bullets.add(newBullet);
 				++d_numOfBullets;
 				return newBullet;
 			}
 			else // d_numOfBullets < d_bullets.size()
 			{
 				++d_numOfBullets;
 				return d_bullets.get(d_numOfBullets - 1);
 			}
 		}
 	}
 	
 
 	
 	public void step() {
 		synchronized(d_player) {
 			d_player.update(d_motionControlX, d_motionControlY, d_shootControlX, d_shootControlY);
 			d_player.step();
 			
 			for(GameCharacter character : d_characters)
 				character.step();
 		}
 		
 		System.err.println(d_numOfBullets);
 		
 		for(int index = 0; index != d_numOfBullets; ++index) {
 			Bullet bullet = d_bullets.get(index);
 			if(bullet.step())
 			{
 				removeBullet(bullet);
 				--index;
 			}
 		}
 	}
 
 	public void removeBullet(Bullet bullet) {
 		synchronized(d_bullets) {
 			--d_numOfBullets;
 			bullet.instantiate(d_bullets.get(d_numOfBullets));
 			d_bullets.get(d_numOfBullets).destroy();
 		}
 	}
 
 	public void draw(Canvas canvas) {
 		canvas.save();
 		
 		canvas.scale(6 * d_scale, 6 * d_scale, d_context.centerHorizontal(), d_context.centerVertical());
 		
 		
 		
 		canvas.translate(-d_player.xOffset() + d_context.centerHorizontal(),
 				 -d_player.yOffset() + d_context.centerVertical());
 				
		// draw pickups
 		synchronized(d_health) {
 			for(HealthPickup pickup : d_health)
 				pickup.draw(canvas);
 		}
 		
 		synchronized(d_ammo) {
 			for(AmmoPickup pickup : d_ammo)
 				pickup.draw(canvas);
 		}
 		
		// draw bullets
 		synchronized(d_bullets) {
 			for(Bullet bullet : d_bullets)
 				bullet.draw(canvas);
 		}
 		
 		canvas.translate(d_player.xOffset() - d_context.centerHorizontal(),
 				 d_player.yOffset() - d_context.centerVertical());
 		
		// draw player
 		d_player.draw(canvas, d_context.centerHorizontal(), d_context.centerVertical());
 		
 		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		
 
 		canvas.translate(-d_player.xOffset() + d_context.centerHorizontal(),
 				 -d_player.yOffset() + d_context.centerVertical());
 		
		// draw enemies (and other players)
 		synchronized(d_characters) {
 			for(GameCharacter character : d_characters)
 				character.draw(canvas);
 		}
 		
		// draw map
 		for(int y = 0; y != d_mapHeight / d_tileSize; ++y) {
 			for(int x = 0; x != d_mapWidth / d_tileSize; ++x) {
 				if(d_tileMap[y][x] == true)
 					canvas.drawRect(x * d_tileSize, y * d_tileSize, 
 							x * d_tileSize + d_tileSize, y * d_tileSize + d_tileSize, 
 							d_blockPaint);
 			}
 		}
	
 		canvas.drawRect(0, 0, d_mapWidth, d_mapHeight, d_borderPaint);
 		
 		canvas.translate(d_player.xOffset() - d_context.centerHorizontal(),
 				 d_player.yOffset() - d_context.centerVertical());
 		
 		paint.setColor(Color.GRAY);
		canvas.drawRect(d_context.centerHorizontal() - 50, d_context.centerVertical() + 30, 
				d_context.centerHorizontal() + 50, d_context.centerVertical() + 35, paint);
 		paint.setColor(Color.RED);
		canvas.drawRect(d_context.centerHorizontal() - 50, d_context.centerVertical() + 30, 
				d_context.centerHorizontal() - 50 + d_player.health(), d_context.centerVertical() + 35, paint);
 		
 		paint.setColor(Color.GRAY);
		canvas.drawRect(d_context.centerHorizontal() - 50, d_context.centerVertical() + 36, 
				d_context.centerHorizontal() + 50, d_context.centerVertical() + 41, paint);
 		paint.setColor(Color.YELLOW);
		canvas.drawRect(d_context.centerHorizontal() - 50, d_context.centerVertical() + 36, 
				d_context.centerHorizontal() - 50 + d_player.ammo(), d_context.centerVertical() + 41, paint);
 		
 		paint.setColor(Color.CYAN);
 		//paint.setTextSize(20);
 		//canvas.drawText("Motion Controls: " + d_motionControlX + ", " + d_motionControlY, 10, 20, paint);
 		//canvas.drawText("Shoot Controls: " + d_shootControlX + ", " + d_shootControlY, 10, 40, paint);
 		
 		canvas.restore();
 		
 		if(!d_motionOrigin.equals(-1, -1)) {
 			canvas.drawCircle(d_motionOrigin.x, d_motionOrigin.y, 5, paint);
 		}
 		if(!d_shootOrigin.equals(-1, -1)) {
 			canvas.drawCircle(d_shootOrigin.x, d_shootOrigin.y, 5, paint);
 		}
 	}
 
 	public boolean collision(float potentialX, float potentialY, float d_radius) {
 		if(potentialX < d_radius || potentialX + d_radius > d_mapWidth 
 				|| potentialY < d_radius || potentialY + d_radius > d_mapHeight) {
 			return true;
 		}
 				
 		int startX = ((int) (potentialX - d_radius)) / d_tileSize;
 		int startY = ((int) (potentialY - d_radius)) / d_tileSize;
 		int endX = Math.min(1 + ((int) (potentialX + d_radius)) / d_tileSize, d_mapWidth / d_tileSize);
 		int endY = Math.min(1 + ((int) (potentialY + d_radius)) / d_tileSize, d_mapHeight / d_tileSize);
 		
 		for(int yIdx = startY; yIdx != endY; ++yIdx) {
 			for(int xIdx = startX; xIdx != endX; ++xIdx) {
 				if(d_tileMap[yIdx][xIdx] == true)
 					return true;
 			}
 		}
 		
 		
 		//for(Block block : d_blocks)
 		//{
 			//if(block.collision(potentialX, potentialY, radius))
 			//	return true;
 		//}	
 		return false;
 	}
 	
 	public Point collision(float cX1, float cY1, float cX2, float cY2) {
 		boolean xLast = true;
 		int x1 = (int) (cX1 / d_tileSize);
 		int y1 = (int) (cY1 / d_tileSize);
 		int x2 = (int) (cX2 / d_tileSize);
 		int y2 = (int) (cY2 / d_tileSize);
 		
 		int dx = Math.abs(x2-x1);
 		int dy = Math.abs(y2-y1);
 		int sx = x1 < x2 ? 1 : -1;
 		int sy = y1 < y2 ? 1 : -1;
 		
 		int error = dx - dy;
 		
 		while(true) {
 			if(d_tileMap[y1][x1]) {
 				if(xLast) {
 					return new Point(x1 * d_tileSize, (int)(Math.abs((cX1 - (x1 * d_tileSize)) / (dx * d_tileSize)) * dy * sy * d_tileSize + cY1));
 				} else {
 					return new Point((int)(Math.abs((cY1 - (y1 * d_tileSize)) / (dy * d_tileSize)) * dx * sx * d_tileSize + cX1), y1 * d_tileSize);
 				}
 			}
 			
 			if(x1 == x2 && y1 == y2)
 				return null;
 			
 			int e2 = 2*error;
 			
 			if(e2 > -dy) {
 				error = error - dy;
 				x1 = x1 + sx;
 				xLast = true;
 			}
 			
 			if(e2 < dx) {
 				error = error + dx;
 				y1 = y1 + sy;
 				xLast = false;
 			}
 			
 			// check if points are outside of map range
 			if(x1 < 0 || y1 < 0 || x1 >= d_mapWidth / d_tileSize || y1 >= d_mapHeight / d_tileSize)
 				return null;
 		}
 	}
 	
 	public void receive(DataInputStream in, int numOfCharacters, int numOfBullets, int numOfHealthPickups, int numOfAmmoPickups) throws Exception {
 		if(d_player.id() == -1)
 			return;
 				
 		//ByteBuffer buffer = ByteBuffer.allocate(24);
 		
 		synchronized(d_characters) {
 		
 			for(int idx = 0; idx != numOfCharacters; ++idx) {
 				//in.readFully(buffer.array(), 0, 24);
 				
 				//assert numOfBytes == 24 || numOfBytes < 0;
 				
 				float x = in.readFloat();
 				float y = in.readFloat();
 				float direction = in.readFloat();
 				float speed = in.readFloat();
 				int color = in.readInt();
 				int id = in.readInt();
 				
 				//buffer.clear();
 				
 				synchronized(d_player) {
 				
 					if(id == d_player.id()) {
 						--idx;
 						--numOfCharacters;
 						continue;
 					}
 				
 				}
 				
 				if(idx < d_characters.size())
 					d_characters.get(idx).instantiate(x, y, direction, speed, color, id);
 				else {
 					GameCharacter character = new GameCharacter(x, y, direction, speed, color, id);
 					d_characters.add(character);
 				}
 			}
 			
 			if(numOfCharacters < d_characters.size()) {
 				for(int idx = numOfCharacters; idx != d_characters.size(); ++idx)
 					d_characters.get(idx).instantiate(0, 0, 0, 0, 0, -1);
 			}
 		
 		}
 		
 		synchronized(d_bullets) {
 			
 			for(int idx = 0; idx != numOfBullets; ++idx) {
 				//in.readFully(buffer.array(), 0, Bullet.sendSize());
 				
 				//assert numOfBytes == Bullet.sendSize() || numOfBytes < 0;
 				
 				addBullet().instantiate(in);
 				
 				//buffer.clear();
 			}
 		}
 		
 		synchronized(d_health) {
 			
 			// Check if a health pickup has been picked up
 			if(numOfHealthPickups < d_lastNumOfHealthPickups) {
 				synchronized(d_player) {
 					d_player.changeHealth(25);
 				}
 			}
 			
 			d_lastNumOfHealthPickups = numOfHealthPickups;
 			
 			for(int idx = 0; idx != numOfHealthPickups; ++idx) {
 				//in.readFully(buffer.array(), 0, 8);
 				
 				//assert numOfBytes == 8 || numOfBytes < 0;
 				
 				int x = in.readInt();
 				int y = in.readInt();
 				
 				//buffer.clear();
 				
 				if(idx < d_health.size())
 					d_health.get(idx).instantiate(x, y);
 				else {
 					System.err.println("Alloc");
 					HealthPickup pickup = new HealthPickup(x, y);
 					d_health.add(pickup);
 				}
 			}
 			
 			if(numOfHealthPickups < d_health.size()) {
 				for(int idx = numOfHealthPickups; idx != d_health.size(); ++idx)
 					d_health.get(idx).instantiate(-1, -1);
 			}
 		}
 		
 		synchronized(d_ammo) {
 			
 			// Check if a ammo pickup has been picked up
 			if(numOfAmmoPickups < d_lastNumOfAmmoPickups) {
 				synchronized(d_player) {
 					d_player.incAmmo(50);
 				}
 			}
 			
 			d_lastNumOfAmmoPickups = numOfAmmoPickups;
 			
 			for(int idx = 0; idx != numOfAmmoPickups; ++idx) {
 				int x = in.readInt();
 				int y = in.readInt();
 				
 				if(idx < d_ammo.size())
 					d_ammo.get(idx).instantiate(x, y);
 				else {
 					System.err.println("Alloc");
 					AmmoPickup pickup = new AmmoPickup(x, y);
 					d_ammo.add(pickup);
 				}
 			}
 			
 			if(numOfAmmoPickups < d_ammo.size()) {
 				for(int idx = numOfAmmoPickups; idx != d_ammo.size(); ++idx)
 					d_ammo.get(idx).instantiate(-1, -1);
 			}
 		}
 		
 
 	}
 
 	public Player player() {
 		
 		return d_player;
 	}
 }
