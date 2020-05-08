 package org.racenet.racesow;
 
 import java.util.List;
 import java.util.Random;
 
 import org.racenet.framework.AndroidAudio;
 import org.racenet.framework.AndroidSound;
 import org.racenet.framework.AnimatedBlock;
 import org.racenet.framework.Camera2;
 import org.racenet.framework.FifoPool;
 import org.racenet.framework.FifoPool.PoolObjectFactory;
 import org.racenet.framework.GLGame;
 import org.racenet.framework.GameObject;
 import org.racenet.framework.Polygon;
 import org.racenet.framework.TexturedBlock;
 import org.racenet.framework.TexturedShape;
 import org.racenet.framework.Vector2;
 
 import android.util.Log;
 
 class Player extends AnimatedBlock {
 	
 	public final Vector2 velocity = new Vector2();
 	public final Vector2 accel = new Vector2();
 	
 	public static final short ANIM_RUN = 0;
 	public static final short ANIM_JUMP = 1;
 	public static final short ANIM_WALLJUMP = 2;
 	public static final short ANIM_BURN = 3;
 	public static final short ANIM_INVISIBLE = 4;
 	public static final short ANIM_ROCKET_RUN = 5;
 	public static final short ANIM_ROCKET_JUMP = 6;
 	public static final short ANIM_ROCKET_WALLJUMP = 7;
 	public static final short ANIM_PLASMA_RUN = 8;
 	public static final short ANIM_PLASMA_JUMP = 9;
 	public static final short ANIM_PLASMA_WALLJUMP = 10;
 	public static final short ANIM_DROWN = 11;
 	
 	public static final short SOUND_JUMP1 = 0;
 	public static final short SOUND_JUMP2 = 1;
 	public static final short SOUND_WJ1 = 2;
 	public static final short SOUND_WJ2 = 3;
 	public static final short SOUND_DIE = 4;
 	public static final short SOUND_PICKUP = 5;
 	public static final short SOUND_ROCKET = 6;
 	public static final short SOUND_PLASMA = 7;
 	private AndroidSound sounds[] = new AndroidSound[8];
 	
 	private static final float FIRERATE_ROCKET = 1.75f;
 	private static final float FIRERATE_PLASMA = 0.04f;
 	private float lastShot = 0;
 	
 	private boolean onFloor = false;
 	private float lastWallJumped = 0;
 	private float distanceOnJump = -1;
 	private boolean distanceRemembered = false;
 	public float virtualSpeed = 0;
 	private float startSpeed = 450;
 	private boolean enableAnimation = false;
 	private boolean isDead = false;
 	private float animDuration = 0;
 	private TexturedBlock attachedItem;
 	private Camera2 camera;
 	private Random rGen;
 	private float volume = 0.1f;
 	private String model = "male";
 	private Map map;
 	private FifoPool<TexturedBlock> plasmaPool;
 	private FifoPool<TexturedBlock> rocketPool;
 	
 	private int frames = 0;
 	
 	public <T> Player(final GLGame game, Map map, Camera2 camera, float x, float y) {
 		
 		super(game, new Vector2(x,y), new Vector2(x + 3.4f, y), new Vector2(x + 3.4f, y + 6.5f), new Vector2(x, y + 6.5f));
 		
 		this.rGen = new Random();
 		this.camera = camera;
 		this.map = map;
 		
 		AndroidAudio audio = (AndroidAudio)game.getAudio();
 		this.sounds[SOUND_JUMP1] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/jump_1.ogg");
 		this.sounds[SOUND_JUMP2] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/jump_2.ogg");
 		this.sounds[SOUND_WJ1] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/wj_1.ogg");
 		this.sounds[SOUND_WJ2] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/wj_2.ogg");
 		this.sounds[SOUND_DIE] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/death.ogg");
 		this.sounds[SOUND_PICKUP] = (AndroidSound)audio.newSound("sounds/weapon_pickup.ogg");
 		this.sounds[SOUND_ROCKET] = (AndroidSound)audio.newSound("sounds/rocket_explosion.ogg");
 		this.sounds[SOUND_PLASMA] = (AndroidSound)audio.newSound("sounds/plasmagun.ogg");
 		
 		this.loadAnimations();
 		this.setupVertices();
 		
 		this.plasmaPool = new FifoPool<TexturedBlock>(new PoolObjectFactory<TexturedBlock>() {
 			
 			public TexturedBlock createObject() {
 				
 				return new TexturedBlock(
 						game,
 						"decals/plasma_hit.png",
 						GameObject.FUNC_NONE,
 						-1,
 						-1,
 						new Vector2(-1, 0),
 						new Vector2(2, 0)
 						);
 			}
 		}, 10);
 		
 		this.rocketPool = new FifoPool<TexturedBlock>(new PoolObjectFactory<TexturedBlock>() {
 	        	
             public TexturedBlock createObject() {
             	
             	return new TexturedBlock(
         				game,
         				"decals/rocket_hit.png",
         				GameObject.FUNC_NONE,
         				-1,
         				-1,
         				new Vector2(-3, 0),
         				new Vector2(5, 0)
         			);
             }
         }, 1);
 	}
 	
 	public void loadAnimations() {
 		
 		String[][] animations = new String[12][];
 		
 		animations[ANIM_RUN] = new String[] {
 			"player/" + this.model + "/default.png"
 		};
 		
 		animations[ANIM_JUMP] = new String[] {
 			"player/" + this.model + "/jump_f1.png",
 			"player/" + this.model + "/jump_f2.png",
 			"player/" + this.model + "/jump_f1.png"
 		};
 		
 		animations[ANIM_ROCKET_RUN] = new String[] {
 			"player/" + this.model + "/rocket_run.png"
 		};
 		
 		animations[ANIM_ROCKET_JUMP] = new String[] {
 			"player/" + this.model + "/rocket_jump_f1.png",
 			"player/" + this.model + "/rocket_jump_f2.png",
 			"player/" + this.model + "/rocket_jump_f1.png"
 		};
 		
 		animations[ANIM_ROCKET_WALLJUMP] = new String[] {
 			"player/" + this.model + "/rocket_walljump_f1.png",
 			"player/" + this.model + "/rocket_walljump_f2.png",
 			"player/" + this.model + "/rocket_walljump_f1.png"
 		};
 		
 		animations[ANIM_PLASMA_RUN] = new String[] {
 			"player/" + this.model + "/plasma_run.png"
 		};
 		
 		animations[ANIM_PLASMA_JUMP] = new String[] {
 			"player/" + this.model + "/plasma_jump_f1.png",
 			"player/" + this.model + "/plasma_jump_f2.png",
 			"player/" + this.model + "/plasma_jump_f1.png"
 		};
 		
 		animations[ANIM_PLASMA_WALLJUMP] = new String[] {
 			"player/" + this.model + "/plasma_walljump_f1.png",
 			"player/" + this.model + "/plasma_walljump_f2.png",
 			"player/" + this.model + "/plasma_walljump_f1.png"
 		};
 		
 		animations[ANIM_WALLJUMP] = new String[] {
 			"player/" + this.model + "/walljump_f1.png",
 			"player/" + this.model + "/walljump_f2.png",
 			"player/" + this.model + "/walljump_f1.png"
 		};
 		
 		animations[ANIM_BURN] = new String[] {
 			"player/" + this.model + "/burn_f1.png",
 			"player/" + this.model + "/burn_f2.png",
 			"player/" + this.model + "/burn_f3.png",
 			"player/" + this.model + "/burn_f4.png"
 		};
 		
 		animations[ANIM_DROWN] = new String[] {
 			"player/" + this.model + "/drown_f1.png",
 			"player/" + this.model + "/drown_f2.png",
 			"player/" + this.model + "/drown_f3.png",
 			"player/" + this.model + "/drown_f4.png"
 		};
 		
 		animations[ANIM_INVISIBLE] = new String[] {
 			"player/" + this.model + "/invisible.png"
 		};
 	
 		this.setAnimations(animations);
 	}
 	
 	public void jump(float jumpPressedTime) {
 		
 		if (this.isDead) return;
 		
 		if (!this.distanceRemembered && this.velocity.y < 0) {
 			
 			TexturedShape ground = this.map.getGround(this);
 			if (ground != null) {
 				
 				this.distanceOnJump = Math.max(0.32f,
 						this.getPosition().y - (ground.getPosition().y + ground.getHeightAt(this.getPosition().x)));
 				this.distanceRemembered = true;
 			}
 		}
 		
 		if (this.onFloor) {
 		
 			this.onFloor = false;
 			
 			int jumpSound = this.rGen.nextInt(SOUND_JUMP2 - SOUND_JUMP1 + 1) + SOUND_JUMP1;
 			this.sounds[jumpSound].play(this.volume);
 			
 			if (this.virtualSpeed == 0) {
 				
 				this.virtualSpeed = this.startSpeed;
 			}
 			
 			if (this.distanceOnJump > 0) {
 				
 				float boost = (30000 / (this.virtualSpeed / 2) / this.distanceOnJump);
 				this.virtualSpeed += boost;
 			}
 			
 			this.velocity.add(0, 20);
 			this.distanceRemembered = false;
 			this.distanceOnJump = -1;
 			
 			
 			if (this.attachedItem != null) {
 				
 				switch (this.attachedItem.func) {
 				
 					case GameObject.ITEM_ROCKET:
 						this.activeAnimId = Player.ANIM_ROCKET_JUMP;
 						break;
 					
 					case GameObject.ITEM_PLASMA:
 						this.activeAnimId = Player.ANIM_PLASMA_JUMP;
 						break;
 						
 					default:
 						this.activeAnimId = Player.ANIM_JUMP;
 						break;
 				}
 				
 			} else {
 			
 				this.activeAnimId = Player.ANIM_JUMP;
 			}
 			
 			this.enableAnimation = true;
 			this.animDuration = 0.3f;
 		
 		} else {
 			
 			if (jumpPressedTime == 0 && System.nanoTime() / 1000000000.0f > this.lastWallJumped + 1.5f) {
 				
 				List<GameObject> colliders = this.map.getPotentialWallColliders(this);
 				int length = colliders.size();
 				for (int i = 0; i < length; i++) {
 				
 					GameObject part = colliders.get(i);
 					CollisionInfo info = this.intersect(part);
 					if (info.collided) {
 						
 						int wjSound = this.rGen.nextInt(SOUND_WJ2 - SOUND_WJ1 + 1) + SOUND_WJ1;
 
 						this.sounds[wjSound].play(this.volume);
 						
 						this.velocity.set(this.velocity.x + 5, 17);
 						this.lastWallJumped = System.nanoTime() / 1000000000.0f;
 						if (this.attachedItem != null) {
 							
 							switch (this.attachedItem.func) {
 							
 								case GameObject.ITEM_ROCKET:
 									this.activeAnimId = Player.ANIM_ROCKET_WALLJUMP;
 									break;
 								
 								case GameObject.ITEM_PLASMA:
 									this.activeAnimId = Player.ANIM_PLASMA_WALLJUMP;
 									break;
 									
 								default:
 									this.activeAnimId = Player.ANIM_WALLJUMP;
 									break;
 							}
 							
 						} else {
 						
 							this.activeAnimId = Player.ANIM_WALLJUMP;
 						}
 						this.enableAnimation = true;
 						this.animDuration = 0.3f;
 					}
 				}
 			}
 		}
 	}
 	
 	public void shoot(float shootPressedTime) {
 		
 		if (this.attachedItem == null || this.isDead) return;
 		
 		float currentTime = System.nanoTime() / 1000000000.0f;
 		switch (this.attachedItem.func) {
 		
 			case GameObject.ITEM_ROCKET:
 				boolean hitWall = false;
 				if (currentTime >= this.lastShot + FIRERATE_ROCKET) {
 					
 					List<GameObject> colliders = this.map.getPotentialWallColliders(this);
 					int length = colliders.size();
 					for (int i = 0; i < length; i++) {
 					
 						GameObject part = colliders.get(i);
 						CollisionInfo info = this.intersect(part);
 						if (info.collided) {
 					
 							hitWall = true;
 							float impactX = this.getPosition().x;
 							float impactY = this.getPosition().y;
 							this.velocity.set(this.velocity.x, this.velocity.y < 0 ? 30 : this.velocity.y + 20);
 							this.virtualSpeed += 200;
 							
 							this.sounds[SOUND_ROCKET].play(this.volume * 1.5f);
							TexturedBlock decal = this.rocketPool.newObject();
							decal.setPosition(new Vector2(impactX, impactY));
							map.addDecal(decal, 0.25f);
 							
 							this.lastShot = currentTime;
 							break;
 						}
 					}
 					
 					if (!hitWall) {
 						
 						TexturedShape ground = map.getGround(this);
 						if (ground != null) {
 							
 							float impactY = ground.getPosition().y + ground.getHeightAt(this.getPosition().x) - 4;
 							float distance = this.getPosition().y - impactY;
 							
 							if (ground.func == GameObject.FUNC_NONE) {
 								
 								this.addToPosition(0, 1);
 								this.velocity.add(0, 100 / distance);
 								this.onFloor = false;
 							}
 							
 							this.sounds[SOUND_ROCKET].play(this.volume * 1.5f);
							TexturedBlock decal = this.rocketPool.newObject();
							decal.setPosition(new Vector2(this.getPosition().x, impactY));
							map.addDecal(decal, 0.25f);
 						}
 						
 						this.lastShot = currentTime;
 					}
 				}
 				break;
 				
 			case GameObject.ITEM_PLASMA:
 				if (currentTime >= this.lastShot + FIRERATE_PLASMA) {
 					
 					List<GameObject> colliders = this.map.getPotentialWallColliders(this);
 					int length = colliders.size();
 					for (int i = 0; i < length; i++) {
 					
 						GameObject part = colliders.get(i);
 						CollisionInfo info = this.intersect(part);
 						if (info.collided) {
 					
 							float impactX = this.getPosition().x;
 							float impactY = this.getPosition().y + 1;
 							this.velocity.add(0, 2.5f);
 							this.virtualSpeed += 15;
 							
 							this.sounds[SOUND_PLASMA].play(this.volume * 1.2f);
 							TexturedBlock decal = (TexturedBlock)this.plasmaPool.newObject();
 							decal.setPosition(new Vector2(impactX, impactY));
 							map.addDecal(decal, 0.25f);
 							
 							this.lastShot = currentTime;
 							break;
 						}
 					}
 					
 					this.lastShot = currentTime;
 				}
 				break;
 		}
 	}
 	
 	public void move(Vector2 gravity, float deltaTime, boolean pressingJump) {
 		
 		if (++frames < 3) return; // workaround
 		
 		if (this.enableAnimation) {
 			
 			this.animTime += deltaTime;
 			if (this.animTime > this.animDuration) {
 				
 				this.enableAnimation = false;
 				this.animTime = 0;
 				
 				if (this.activeAnimId == Player.ANIM_BURN || this.activeAnimId == Player.ANIM_DROWN) {
 					
 					this.activeAnimId = Player.ANIM_INVISIBLE;
 				
 				} else {
 				
 					if (this.attachedItem != null) {
 						
 						switch (this.attachedItem.func) {
 						
 							case GameObject.ITEM_ROCKET:
 								this.activeAnimId = Player.ANIM_ROCKET_RUN;
 								break;
 							
 							case GameObject.ITEM_PLASMA:
 								this.activeAnimId = Player.ANIM_PLASMA_RUN;
 								break;
 								
 							default:
 								this.activeAnimId = Player.ANIM_RUN;
 								break;
 						}
 						
 					} else {
 					
 						this.activeAnimId = Player.ANIM_RUN;
 					}
 				}
 			}
 		}
 		
 		if (this.isDead) return;
 		
 		List<GameObject> colliders = this.map.getPotentialFuncColliders(this);
 		int length = colliders.size();
 		for (int i = 0; i < length; i++) {
 		
 			GameObject part = colliders.get(i);
 			CollisionInfo info = this.intersect(part);
 			if (info.collided) {
 			
 				switch (part.func) {
 				
 					case GameObject.FUNC_START_TIMER:
 						this.map.startTimer();
 						break;
 						
 					case GameObject.FUNC_STOP_TIMER:
 						this.map.stopTimer();
 						break;
 				}
 			}
 		}
 		
 		length = this.map.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			TexturedShape item = this.map.items.get(i);
 			float playerX = this.getPosition().x;
 			float itemX = item.getPosition().x;
 			if (playerX >= itemX && playerX <= itemX + item.width) {
 				
 				String texture = "";
 				switch (item.func) {
 				
 					case GameObject.ITEM_ROCKET:
 						texture = "items/rocket.png";
 						this.activeAnimId = ANIM_ROCKET_RUN;
 						break;
 					
 					case GameObject.ITEM_PLASMA:
 						texture = "items/plasma.png";
 						this.activeAnimId = ANIM_PLASMA_RUN;
 						break;
 				}
 				
 				TexturedBlock hudItem = new TexturedBlock(
 					this.game,
 					texture,
 					item.func,
 					-1,
 					-1,
 					new Vector2(-(this.camera.frustumWidth / 2) + 1, -(this.camera.frustumHeight / 2) + 1),
 					new Vector2(-(this.camera.frustumWidth / 2) + 10, -(this.camera.frustumHeight / 2) + 10)
 				);
 				
 				camera.addHud(hudItem);
 				this.map.items.remove(item);
 				this.map.pickedUpItems.add(item);
 				if (this.attachedItem != null) {
 					
 					synchronized (this) {
 						
 						this.camera.removeHud(this.attachedItem);
 						this.attachedItem.texture.dispose();
 					}
 				}
 				
 				this.lastShot = 0;
 				this.attachedItem = hudItem;
 				this.sounds[SOUND_PICKUP].play(this.volume);
 				break;
 			}
 		}
 		
 		if (!this.onFloor) {
 			
 			/*
 			boolean straightUp = false;
 			if (this.velocity.x == 0 && this.velocity.y > 0) {
 				
 				straightUp = true;
 			}
 			*/
 			
 			this.velocity.add(gravity.x * deltaTime, gravity.y * deltaTime);
 			
 			/*
 			if (straightUp && this.velocity.y <= 0) {
 				
 				straightUp = false;
 				this.virtualSpeed += 30;
 			}
 			*/
 			
 			this.addToPosition(this.velocity.x * deltaTime, this.velocity.y * deltaTime);
 			
 			colliders = this.map.getPotentialGroundColliders(this);
 			length = colliders.size();
 			for (int i = 0; i < length; i++) {
 				
 				GameObject ground = colliders.get(i);
 				CollisionInfo info = this.intersect(ground);
 				if (info.collided) {
 
 					switch (ground.func) {
 					
 						case GameObject.FUNC_LAVA:
 							this.activeAnimId = Player.ANIM_BURN;
 							this.enableAnimation = true;
 							this.animDuration = 0.4f;						
 							this.die();
 							return;
 							
 						case GameObject.FUNC_WATER:
 							this.activeAnimId = Player.ANIM_DROWN;
 							this.enableAnimation = true;
 							this.animDuration = 0.4f;						
 							this.die();
 							return;
 					}
 
 					// ground
 					if (info.type == Polygon.TOP) {
 					
 						this.setPosition(new Vector2(this.getPosition().x, this.getPosition().y + info.distance));
 						this.velocity.set(this.velocity.x, 0);
 						this.onFloor = true;
 					
 					// wall
 					} else if (info.type == Polygon.LEFT) {
 					
 						this.setPosition(new Vector2(this.getPosition().x - info.distance, this.getPosition().y));
 						this.velocity.set(0, this.velocity.y);
 						this.virtualSpeed = 0;
 					
 					// ramp up
 					} else if (info.type == Polygon.RAMPUP) {
 						
 						this.setPosition(new Vector2(this.getPosition().x, this.getPosition().y - info.distance));
 						if (pressingJump && this.virtualSpeed >= 900) {
 							
 							float m = (ground.vertices[2].y - ground.vertices[0].y) / (ground.vertices[2].x - ground.vertices[0].x);
 							this.velocity.set(this.velocity.x, this.velocity.x * m);
 							
 						} else {
 						
 							this.velocity.set(this.velocity.x, 0);
 							this.onFloor = true;
 						}
 						
 					// ramp down
 					} else if (info.type == Polygon.RAMPDOWN) {
 						
 						float m = (ground.vertices[0].y - ground.vertices[2].y) / (ground.vertices[0].x - ground.vertices[2].x);
 						
 						Log.d("DEBUG", "m " + String.valueOf(new Float(m)));
 						
 						this.velocity.set(this.velocity.x, 0);
 						this.virtualSpeed += (-m + 1) * (-m + 1) * (-m + 1) * 128;
 						this.onFloor = true;
 					}
 
 					break;
 				}
 			}
 			
 		} else {
 			
 			if (this.virtualSpeed > 0) {
 				
 				this.virtualSpeed = Math.max(0, this.virtualSpeed - 10000 * deltaTime);
 			}
 		}
 		
 		this.velocity.set(this.virtualSpeed / 23, this.velocity.y);
 	}
 	
 	public void die() {
 		
 		this.virtualSpeed = 0;
 		this.sounds[SOUND_DIE].play(this.volume);
 		this.isDead = true;
 	}
 	
 	public void reset(float x, float y) {
 		
 		this.isDead = false;
 		this.activeAnimId = ANIM_RUN;
 		this.virtualSpeed = 0;
 		this.setPosition(new Vector2(x, y));
 		this.velocity.set(0, 0);
 		
 		if (this.attachedItem != null) {
 			
 			synchronized (this) {
 				
 				this.camera.removeHud(this.attachedItem);
 				this.attachedItem.texture.dispose();
 				this.attachedItem = null;
 			}
 		}
 	}
 }
