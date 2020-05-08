 package net.fourbytes.shadow;
 
 import java.util.Vector;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 public abstract class Mob extends Entity {
 	
 	public float SPEED = 0.065f;
 	public float JUMPH = 0.25f;
 	public int JUMPHAI = 1;
 	public boolean facingLeft = true;
 	public boolean standing = false;
 	int subframe = 0;
 	public int frame = 0;
 	public int maxframe = 4;
 	public int animSpeed = 4;
 	public int animSpeedInAir = 4;
 	public int canJump = 0;
 	public int maxJump = 1;
 	public Vector2 spawnpos;
 	public boolean invertedImage = false;
 	
 	public boolean canAIClimb = true;
 	public boolean canAIWall = true;
 	public boolean canAITurn = false;
 	public boolean canAIJump = false;
 	public int turnTick = 0;
 	
 	public float objgravityOG = 1f;
 	public float objgravityIA = 1f;
 	public float objgravityAI = 0.1f;
 	
 	public Mob(Vector2 position, Layer layer) {
 		super(position, layer);
 		spawnpos = new Vector2(position);
 	}
 	
 	@Override
 	public void dead() {
 		health = MAXHEALTH;
 		pos.set(spawnpos);
 		movement.set(0, 0);
 	}
 	
 	boolean invoid = false;
 	
 	@Override
 	public void tick() {
 		if (!standing) {
 			if (facingLeft) {
 				movement.add(-SPEED, 0f);
 				imgupdate = true;
 				subframe++;
 			}
 			if (!facingLeft) {
 				movement.add(SPEED, 0f);
 				imgupdate = true;
 				subframe++;
 			}
 			//movement.add((float)Math.random()/12f-1f/24f, 0f);
 			//movement.add(0f, (float)Math.random()/16f-1f/32f);
 			movement.add(0f, -0.0075f);
 		}
 		int animSpeed_ = animSpeed;
 		if (canJump != maxJump) {
 			animSpeed_ = animSpeedInAir;
 		}
 		if (subframe >= animSpeed_) {
 			frame++;
 			imgupdate = true;
 			subframe = 0;
 		}
 		if (frame >= maxframe) {
 			frame = 0;
 			imgupdate = true;
 		}
 		
 		if ((layer != null && layer.level != null && layer.level.hasvoid) || movement.y > 5f) {
 			if (pos.y > layer.level.tiledh && !invoid) {
 				health = 0f;
 				invoid = true;
 			} else {
 				invoid = false;
 			}
 		}
 		
 		objgravity = objgravityOG;
 		if (canJump != maxJump && movement.y > 0f) {
 			objgravity = objgravityIA;
 		}
 		objgravity += (float)(Math.random()*(objgravityAI*2))-objgravityAI;
 		
 		//Messy AI block detection
 		
 		boolean avoidedWall = false;
 		boolean climbed = false;
 		boolean turned = false;
 		
 		if ((canAIWall || canAIClimb) && canJump > 0) {
 			boolean collides = false;
 			int minx = 0;
 			int maxx = 0;
 			if (facingLeft) {
 				minx = -1;
 			} else {
 				maxx = 2;
 			}
 			Rectangle er = calcCollide();
 			er.x += minx;
 			er.width -= minx;
 			er.width += maxx;
 			er.height -= er.height/4;
 			for (int x = minx; x <= maxx; x++) {
 				for (int y = -1; y <= 0; y++) {
					Array<Block> blocks = layer.get(Coord.get((int)(pos.x + x), (int)(pos.y + y)));
 					if (blocks != null) {
 						for (Block b : blocks) {
 							if (b == null) continue;
 							if (!b.solid) continue;
 							
 							or.set((int)b.pos.x, (int)b.pos.y, 1f, 1f);
 							if (MathHelper.overlaps(or, er)) {
 								//b.highlight();
 								collides = true;
 								break;
 							}
 						}
 					}
 				}
 			}
 			if (collides) {
 				avoidedWall = true;
 			}
 		}
 		
 		if (canAIClimb && avoidedWall) {
 			if (canJump != 0) {
 				boolean collides = false;
 				int minx = 0;
 				int maxx = 0;
 				if (facingLeft) {
 					minx = -1;
 				} else {
 					maxx = 2;
 				}
 				for (int x = minx; x <= maxx; x++) {
 					for (int y = -JUMPHAI-1; y <= -2; y++) {
						Array<Block> blocks = layer.get(Coord.get(pos.x + x, pos.y + y));
 						if (blocks != null) {
 							for (Block b : blocks) {
 								if (b == null) continue;
 								if (!b.solid) continue;
 								//b.highlight();
 								collides = true;
 								break;
 							}
 						}
 					}
 				}
 				if (!collides) {
 					climbed = true;
 				}
 			}
 		}
 		
 		if (canAIWall && avoidedWall && !climbed && canJump > 0) {
 			facingLeft = !facingLeft;
 			turned = true;
 		}
 		
 		if (canAIClimb && climbed && canJump > 0) {
 			Sounds.getSound("jump").play(Sounds.calcVolume(pos), Sounds.calcPitch(1f, 0.3f), 0f);
 			movement.add(0, -movement.y - JUMPH);
 			canJump--;
 		}
 		
 		//Random turning / jumping
 		
 		if (canAIJump && !climbed && canJump > 0) {
 			if ((int)(Math.random()*64) == 0) {
 				Sounds.getSound("jump").play(Sounds.calcVolume(pos), Sounds.calcPitch(1f, 0.3f), 0f);
 				movement.add(0, -movement.y - JUMPH);
 				canJump--;
 			}
 		}
 		
 		if (canAITurn && !turned && turnTick >= 24 && canJump == maxJump) {
 			if ((int)(Math.random()*32) == 0) {
 				facingLeft = !facingLeft;
 			}
 			turnTick = 0;
 		}
 		turnTick++;
 		
 		super.tick();
 		
 		/*
 		if (invertedImage?!facingLeft:facingLeft) {
 			renderoffs.width = -rec.width*2;
 			renderoffs.x = rec.width;
 		} else {
 			renderoffs.width = 0;
 			renderoffs.x = 0;
 		}
 		*/
 	}
 	
 }
