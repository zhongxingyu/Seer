 package com.jumpandrun;
 
 import java.util.Iterator;
 import java.util.List;
 
 import net.beadsproject.beads.analysis.featureextractors.Power;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.Filter;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.WorldManifold;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.utils.Array;
 
 public class GameInstance {
 
 	final static float MAX_VELOCITY = 20f;	
 	final static float GRAVITY = -160f;
 	
 	public static GameInstance instance;
 	
 	Map map;
 	Background background;
 	
 	public Array<MovingPlatform> platforms = new Array<MovingPlatform>();
 	public Array<Block> blocks = new Array<Block>();
 	public Array<Block> blankBlocks = new Array<Block>();
 	public Array<Enemy> enemies = new Array<Enemy>();
 	public Array<Bullet> bullets = new Array<Bullet>();
 	public Array<PowerUp> powerUps = new Array<PowerUp>();
 	public World world  = new World(new Vector2(0, GRAVITY), true);
 	public Player player;	
 
 	float stillTime = 0;
 	long lastGroundTime = 0;
 	
 	MovingPlatform groundedPlatform = null;
 
 	Filter groupCollideFilter = new Filter();
 	Filter groupNonCollideFilter = new Filter();
 	
 	Filter playerCollideFilter = new Filter();
	static Filter enemyCollideFilter = new Filter();
 	
 	Body box;
 	Block block;
 
 	Fixture playerPhysicsFixture;
 	Fixture playerSensorFixture;
 	
 	public static GameInstance getInstance() {
 		if (instance == null) {
 			instance = new GameInstance();
 		}
 		return instance;
 	}
 	
 	public void resetGame() {
 		platforms.clear();
 		blocks.clear();
 		enemies.clear();
 		bullets.clear();
 		blankBlocks.clear();
 		powerUps.clear();
 		world.dispose();
 		
		groupCollideFilter.groupIndex = 1;
 		groupCollideFilter.categoryBits = 0x0001;
 		groupNonCollideFilter.groupIndex = -1;
 		
 		playerCollideFilter.categoryBits = 0x0002;
 		
		enemyCollideFilter.categoryBits = 0x0002;
		enemyCollideFilter.maskBits = 0x0004;
		enemyCollideFilter.groupIndex = -1;
 		
 		
 		world  = new World(new Vector2(0, GRAVITY), true);
 		map = new Map();
 		background = new Background();
 	}
 	
 	public Body createBox(BodyType type, float width, float height, float density) {
 		BodyDef def = new BodyDef();
 		def.type = type;
 		Body box = GameInstance.getInstance().world.createBody(def);
  
 		PolygonShape poly = new PolygonShape();
 		poly.setAsBox(width, height);
 		Fixture fix = box.createFixture(poly, density);
 		fix.setFilterData(groupCollideFilter);
 		poly.dispose();
  
 		return box;
 	}	
  
 	public Body createEdge(BodyType type, float x1, float y1, float x2, float y2, float density) {
 		BodyDef def = new BodyDef();
 		def.type = type;
 		Body box = world.createBody(def);
 		
 		PolygonShape poly = new PolygonShape();		
 		poly.setAsEdge(new Vector2(0, 0), new Vector2(x2 - x1, y2 - y1));
 		Fixture fix = box.createFixture(poly, density);
 		fix.setFilterData(groupCollideFilter);
 		box.setTransform(x1, y1, 0);
 		poly.dispose();
  
 		return box;
 	}
  
 	public Body createCircle(BodyType type, float radius, float density) {
 		BodyDef def = new BodyDef();
 		def.type = type;
 		Body box = world.createBody(def);
  
 		CircleShape poly = new CircleShape();
 		poly.setRadius(radius);
 		Fixture fix = box.createFixture(poly, density);
 		fix.setFilterData(groupCollideFilter);
 		poly.dispose();
  
 		return box;
 	}	
 	
 	public void addEnemySpawner(float x, float y) {
 		EnemySpawner enemySpawner = new EnemySpawner(x, y);
 		box = createBox(BodyType.StaticBody, 1, 1, 3);
 		box.setTransform(enemySpawner.position.x , enemySpawner.position.y, 0);
 		
 		enemySpawner.body = box;
 		blocks.add(enemySpawner);
 		enemySpawner.body.setUserData(enemySpawner);		
 	}
 	
 	public void addJumpBlock(float x, float y) {
 		JumpBlock block = new JumpBlock(x,y);
 		box = createBox(BodyType.StaticBody, 1, 1, 3);
 		box.setTransform(block.position.x , block.position.y, 0);
 		
 		block.body = box;
 		blocks.add(block);
 		block.body.setUserData(block);
 	}
 	
 	public void addBlock(float x, float y) {
 		Block block = new Block(x,y);
 		box = createBox(BodyType.StaticBody, 1, 1, 3);
 		box.setTransform(block.position.x , block.position.y, 0);
 		
 		block.body = box;
 		block.body.setUserData(block);
 		blocks.add(block);
 	}
 	
 	public void addBlankBlock(float x, float y) {
 		Block block = new Block(x,y);
 		blankBlocks.add(block);
 	}
 	
 	public void addEnemy() {
 		if(enemies.size <= 5) {
 		for(Block block:blocks) {
 			if(block instanceof EnemySpawner) {
 				float size =1;
 				if(MathUtils.randomBoolean()) {
 					size = 1;
 				} else {
 					size = 1.7f;
 				}			
 				
 				Enemy enemy = new Enemy(block.position.x, block.position.y-1.5f, size);				
 				if(Math.random() >= 0.5) {
 					enemy.direction.x = -enemy.direction.x;
 				}
 				Body box = createCircle(BodyType.DynamicBody, size,1);		
 				box.setBullet(true);		 
 				box.setTransform(block.position.x, block.position.y-1.5f, 0);
 				
				box.getFixtureList().get(0).setFilterData(playerCollideFilter);
 				
 				enemy.body = box;
 				box.setFixedRotation(true);
 				enemy.body.setUserData(enemy);
 				enemies.add(enemy);
 			}
 			}
 		}
 	}
 	
 	public void addPowerUp(float x, float y) {
 		if(powerUps.size == 0) {
 			PowerUp powerUp = new PowerUp(x,y);
 			Body box = createCircle(BodyType.DynamicBody, 1, 1);
 			box.setTransform(powerUp.position.x, powerUp.position.y, 0);
 	
			box.getFixtureList().get(0).setFilterData(enemyCollideFilter);
 			
 			powerUp.body = box;
 			box.setFixedRotation(true);
 			powerUp.body.setUserData(powerUp);
 			powerUps.add(powerUp);
 //			powerUp.body.applyLinearImpulse(0, 710, powerUp.position.x, powerUp.position.y);
 		}
 	}
 	
 	public void addBullet() {
 		if(player.lastshot < player.shotlimit)
 			return;
 		player.lastshot = 0;
 		Bullet b = new Bullet(player.position.x, player.position.y-1.5f);
 		
 		Body box = createBox(BodyType.DynamicBody, 0.2f, 0.2f, 1);
 		
 		box.setBullet(true);		 
 		box.setTransform(player.position.x+player.xdir, player.position.y, 0);
 		Vector2 dir = new Vector2(player.xdir, 0);
 		dir = dir.nor();
 		dir.mul(b.speed);
 		box.setLinearVelocity(dir);
 		
 //		enemy.enemyPhysicsFixture = enemyPhysicsFixture;
 		b.body = box;
 		b.body.setUserData(b);
 		bullets.add(b);
 	}
  
 	public void createPlayer(float x, float y) {
 		BodyDef def = new BodyDef();
 		def.type = BodyType.DynamicBody;
 		Body box = world.createBody(def);
 		 
 		PolygonShape poly = new PolygonShape();		
 		poly.setAsBox(0.1f, 0.1f);
 		playerPhysicsFixture = box.createFixture(poly, 0);	
 		playerPhysicsFixture.setFilterData(playerCollideFilter);
 		poly.dispose();		
 		
 		
 		CircleShape circle = new CircleShape();		
 		circle.setRadius(1f);
 		circle.setPosition(new Vector2(0, -0.6f));
 		playerSensorFixture = box.createFixture(circle, 1);		
 		playerSensorFixture.setFilterData(playerCollideFilter);
 		circle.dispose();		
  
 		box.setBullet(true);
  
 		box.setFixedRotation(true);
 		box.setTransform(x, y, 0);
 		
 		player = new Player(x, y);
 		player.body = box;
 		player.body.setUserData(player);
 	}
 	
 	public boolean isPlayerGrounded() {				
 		groundedPlatform = null;
 		List<Contact> contactList = world.getContactList();
 		for(int i = 0; i < contactList.size(); i++) {
 			Contact contact = contactList.get(i);
 			if(contact.isTouching() && (contact.getFixtureA() == playerSensorFixture ||
 			   contact.getFixtureB() == playerSensorFixture)) {				
  
 				Vector2 pos = player.position.tmp();
 				WorldManifold manifold = contact.getWorldManifold();
 				boolean below = true;
 				for(int j = 0; j < manifold.getNumberOfContactPoints(); j++) {
 					below &= (manifold.getPoints()[j].y < pos.y - 1.5f);
 				}
  
 				if(below) {
 					if(contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals("p")) {
 						groundedPlatform = (MovingPlatform)contact.getFixtureA().getBody().getUserData();							
 					}
  
 					if(contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals("p")) {
 						groundedPlatform = (MovingPlatform)contact.getFixtureB().getBody().getUserData();
 					}											
 					return true;			
 				}
  
 				return false;
 			}
 		}
 		return false;
 	}
 	public void flagBullets() {				
 		//List<Contact> contactList = world.getContactList();
 		/*Iterator<Body> it = world.getBodies();
 		while(it.hasNext()) {
 			Body b = it.next();
 			if(b.getUserData() instanceof Bullet) {
 				if(((Bullet)b.getUserData()).age > 100) {
 					((Bullet)b.getUserData()).kill = true;
 					
 				}
 			}
 		}*/
 		
 		List<Contact> contactList = world.getContactList();
 		for(int i = 0; i < contactList.size(); i++) {
 			Contact contact = contactList.get(i);
 			if(contact.isTouching()) {
 				Object a = contact.getFixtureA().getBody().getUserData();
 				Object b = contact.getFixtureB().getBody().getUserData();
 				
 				if(a instanceof Bullet  && b instanceof Block) {
 					((Bullet)a).kill = true;
 					((Block)b).highlightAnimate = 0.3f;
 				} else if(b instanceof Bullet && a instanceof Block) {
 					((Bullet)b).kill = true;
 					((Block)a).highlightAnimate = 0.3f;
 				}
 				
 				
 				if(a instanceof Bullet  && b instanceof Enemy) {
 					((Bullet)a).kill = true;
 					((Enemy)b).hit(((Bullet)a).damage);
 				} else if(b instanceof Bullet && a instanceof Enemy) {
 					((Bullet)b).kill = true;
 					((Enemy)a).hit(((Bullet)b).damage);
 				}
 				if(a instanceof Bullet && !(b instanceof Bullet) && !(b instanceof Player)) {
 					((Bullet)a).kill = true;
 				} else if(b instanceof Bullet && !(a instanceof Bullet) && !(a instanceof Player)) {
 					((Bullet)b).kill = true;
 				}
 			}
 		}
 
 	}
 	
 	public void removeKilled() {
 		//Bullets
 		{
 		boolean found = false;
 		do {
 			found = false;
 			for(int e = 0; e < bullets.size; e++) {
 				if(bullets.get(e).kill) {
 					found = true;
 					world.destroyBody(bullets.get(e).body);
 					bullets.removeIndex(e);
 					break;
 				}
 			}
 		}while(found);
 		}
 		
 		//powerups
 		{
 		boolean found = false;
 		do {
 			found = false;
 			for(int e = 0; e < powerUps.size; e++) {
 				if(powerUps.get(e).kill) {
 					found = true;
 					world.destroyBody(powerUps.get(e).body);
 					powerUps.removeIndex(e);
 					break;
 				}
 			}
 		}while(found);
 		}
 		
 		//Enemies
 		{
 		boolean found = false;
 		do {
 			found = false;
 			for(int e = 0; e < enemies.size; e++) {
 				if((!enemies.get(e).alive || enemies.get(e).health <= 0) && enemies.get(e).dyingAnimate == 1) {
 					found = true;
 					world.destroyBody(enemies.get(e).body);
 					enemies.removeIndex(e);
 					break;
 				}
 			}
 		}while(found);
 		}
 	}
 	
 	public void physicStuff(float delta) {
 
 		
 		if(player.alive == false) {
 			resetGame();
 		}
 		
 		// le step...			
 		world.step(delta, 50, 50);	
 		
 		Vector2 vel = player.body.getLinearVelocity();
 		Vector2 pos = player.position.tmp();		
 		boolean grounded = isPlayerGrounded();
 		/*if(grounded) {
 			lastGroundTime = System.nanoTime();
 		} else {
 			if(System.nanoTime() - lastGroundTime < 100000000) {
 				grounded = true;
 			}
 		}*/
  
 		// cap max velocity on x		
 		if(Math.abs(vel.x) > MAX_VELOCITY) {			
 			vel.x = Math.signum(vel.x) * MAX_VELOCITY;
 			player.body.setLinearVelocity(vel.x, vel.y);
 		}
 		
 		if(Gdx.input.isKeyPressed(Keys.SPACE)) {
 			addBullet();
 		}
 		
 		// calculate stilltime & damp
 		if(!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D)) {			
 			stillTime += Gdx.graphics.getDeltaTime();
 			player.body.setLinearVelocity(vel.x * 0.9f, vel.y);
 		}
 		else { 
 			stillTime = 0;
 		}			
  
 		// disable friction while jumping
 		if(!grounded) {			
 			playerPhysicsFixture.setFriction(0f);
 			playerSensorFixture.setFriction(0f);			
 		} else {
 			/*if(!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D) && stillTime > 0.2) {
 				playerPhysicsFixture.setFriction(100f);
 				playerSensorFixture.setFriction(100f);
 			}
 			else {
 				playerPhysicsFixture.setFriction(0.2f);
 				playerSensorFixture.setFriction(0.2f);
 			}*/
  
 			if(groundedPlatform != null && groundedPlatform.dist == 0) {
 				player.body.applyLinearImpulse(0, -20, pos.x, pos.y);				
 			}
 		}		
  
 		// apply left impulse, but only if max velocity is not reached yet
 		if(Gdx.input.isKeyPressed(Keys.A) && vel.x > -MAX_VELOCITY) {
 			player.body.applyLinearImpulse(-3.4f, 0, pos.x, pos.y);
 			player.xdir = -1;
 		}
  
 		// apply right impulse, but only if max velocity is not reached yet
 		if(Gdx.input.isKeyPressed(Keys.D) && vel.x < MAX_VELOCITY) {
 			player.body.applyLinearImpulse(3.4f, 0, pos.x, pos.y);
 			player.xdir = 1;
 		}
  
 		// jump, but only when grounded
 		if(player.jump) {			
 			
 			//player.jump = false;
 			if(grounded) {
 				player.body.setLinearVelocity(vel.x, 0);			
 				player.body.setTransform(pos.x, pos.y + 0.01f, 0);
 				player.body.applyLinearImpulse(0, 80, pos.x, pos.y);	
 				lastGroundTime = System.nanoTime();
 			} else if(System.nanoTime() - lastGroundTime < 150000000) {
 				player.body.applyLinearImpulse(0, 710*delta, pos.x, pos.y);
 				//player.body.setLinearVelocity(vel.x, vel.y+1*(100000000-(System.nanoTime() - lastGroundTime))/100000000f);
 			}
 		}				
  
 		// update platforms
 		for(int i = 0; i < platforms.size; i++) {
 			MovingPlatform platform = platforms.get(i);
 			platform.update(Math.max(1/30.0f, Gdx.graphics.getDeltaTime()));
 		}
 		
 		//update jump plattforms
 		for(int i = 0; i < blocks.size; i++) {
 			Block block = blocks.get(i);
 			if(block instanceof JumpBlock) {
 				if(((JumpBlock) block).jumpAnim > 0) {
 					((JumpBlock) block).jumpAnim = Math.max(0, ((JumpBlock) block).jumpAnim - Gdx.graphics.getDeltaTime()*2);
 				} 
 			}			
 		}
 		
 		//outOfBounds
 		if(player.position.y < -50) {
 			player.alive = false;
 		}
 		
 		//update enemies
 		for(int i = 0; i < enemies.size; i++) {
 			Enemy enemy = enemies.get(i);
 			enemy.move();
 			enemy.update(delta);
 			enemy.body.setAwake(true);
 			
 			//outOfBounds
 			if(enemy.position.y < -50) {
 				enemy.alive = false;
 			}
 		}
 		//update bullets
 		for(int i = 0; i < bullets.size; i++) {
 			Bullet bullet = bullets.get(i);
 			bullet.update(delta);	
 			bullet.body.setAwake(true);
 			
 			//outOfBounds
 			if(bullet.position.y < -50) {
 				bullet.kill = true;
 			}
 		}
 		//update powerUps
 		for(int i = 0; i < powerUps.size; i++) {
 			PowerUp powerUp = powerUps.get(i);
 			powerUp.update();
 			if(powerUp.show) {
 				powerUp.depth += delta;
 			}else {
 				powerUp.depth -= delta*5f;
 			}
 			if(powerUp.depth > 1 && powerUp.show) {
 				powerUp.depth -= delta;
 			}
 			if(powerUp.depth < 0 && !powerUp.show) {
 				powerUp.kill = true;
 			}
 			
 			//outOfBounds
 			if(powerUp.position.y < -50) {
 				powerUp.kill = true;
 			}
 		}
 
 		
 		flagBullets();
 		removeKilled();
 		
 		player.lastshot+= delta;
  
 		player.update();
 		player.body.setAwake(true);
 		
 		//check player/enemy collision
 		checkPlayerEnemyCollision();
 		
 		//animate background
 		
 					
 	}
 	
 	public void activateJumpBlocks() {
 		List<Contact> contactList = world.getContactList();
 		for(int i = 0; i < contactList.size(); i++) {
 			Contact contact = contactList.get(i);
 			if(contact.isTouching()) {
 				if (contact.getFixtureA().getBody().getUserData() instanceof JumpBlock) {
 					if (contact.getFixtureB().getBody().getUserData() instanceof Enemy || contact.getFixtureB().getBody().getUserData() instanceof Player) {
 						if (contact.getFixtureA().getBody().getPosition().y < contact.getFixtureB().getBody().getPosition().y - 1) {
 							contact.getFixtureB().getBody().applyLinearImpulse(0, 85, player.position.x, player.position.y);
 							// ((JumpBlock)contact.getFixtureA().getBody().getUserData()).jump();
 						}
 					}
 				}
 				if (contact.getFixtureB().getBody().getUserData() instanceof JumpBlock) {
 					if (contact.getFixtureA().getBody().getUserData() instanceof Enemy || contact.getFixtureA().getBody().getUserData() instanceof Player) {
 						if (contact.getFixtureA().getBody().getPosition().y - 1 > contact.getFixtureB().getBody().getPosition().y) {
 							contact.getFixtureA().getBody().applyLinearImpulse(0, 85, player.position.x, player.position.y);
 							// ((JumpBlock)contact.getFixtureB().getBody().getUserData()).jump();
 						}
 					}
 				}
 			}
 		}
 		
 		for(int i = 0; i < blocks.size; i++) {
 			Block block = blocks.get(i);
 			if(block instanceof JumpBlock) {
 				((JumpBlock) block).jump();
 			}			
 		}
 	}
 	
 	
 	public void checkPlayerEnemyCollision() {
 		List<Contact> contactList = world.getContactList();
 		for(int i = 0; i < contactList.size(); i++) {
 			Contact contact = contactList.get(i);
 			if(contact.isTouching()) {
 				if(contact.getFixtureA().getBody().getUserData() instanceof Player  && contact.getFixtureB().getBody().getUserData() instanceof Enemy) {
 					if(((Enemy) contact.getFixtureB().getBody().getUserData()).alive) {
 						player.alive = false;
 					}
 				}
 				if(contact.getFixtureB().getBody().getUserData() instanceof Player && contact.getFixtureA().getBody().getUserData() instanceof Enemy) {
 					if(((Enemy) contact.getFixtureA().getBody().getUserData()).alive) {
 						player.alive = false;
 					}
 				}
 				
 				if(contact.getFixtureA().getBody().getUserData() instanceof Player  && contact.getFixtureB().getBody().getUserData() instanceof PowerUp) {
 					((PowerUp) contact.getFixtureB().getBody().getUserData()).show = false;
 				}
 				if(contact.getFixtureB().getBody().getUserData() instanceof Player && contact.getFixtureA().getBody().getUserData() instanceof PowerUp) {
 					((PowerUp) contact.getFixtureA().getBody().getUserData()).show = false;
 				}
 				
 				if(contact.getFixtureA().getBody().getUserData() instanceof Player) {
 					if(contact.getFixtureB().getBody().getUserData() instanceof Block) {
 //						if(((Block) contact.getFixtureB().getBody().getUserData()).highlightAnimate >= 0) {
 							((Block) contact.getFixtureB().getBody().getUserData()).highlightAnimate = 0.5f;
 //						}
 					}
 				}
 				if(contact.getFixtureB().getBody().getUserData() instanceof Player) {
 					if(contact.getFixtureA().getBody().getUserData() instanceof Block) {
 //						if(((Block) contact.getFixtureA().getBody().getUserData()).highlightAnimate >= 0) {
 							((Block) contact.getFixtureA().getBody().getUserData()).highlightAnimate = 0.5f;
 //						}
 					}
 				}
 			}
 		}
 		
 	}
 }
