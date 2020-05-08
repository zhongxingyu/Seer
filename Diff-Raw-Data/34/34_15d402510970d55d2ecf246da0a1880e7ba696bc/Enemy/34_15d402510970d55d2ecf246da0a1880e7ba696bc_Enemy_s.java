 
 package com.jumpandrun;
 
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.Fixture;
 
 public class Enemy {
 	public Body body;
 	public Fixture enemyPhysicsFixture;
 	
 	public Vector2 direction = new Vector2(9,0);
 	
 	public Vector2 position = new Vector2();
 	public float angle = 0;
 	public boolean alive;
 	public int health;
 	public float hitAnimate = 0;
 	public float dyingAnimate = 0;
 	
 	public float size = 1;
 
 	public Enemy(float x, float y, float size) {
 		position.x = x;
 		position.y = y;
 		this.size = size;
 		alive = true;
 		health = 100;
 	}
 	
 	public void update(float delta) {
 		if(body!=null) {
 			position.x = body.getPosition().x;
 			position.y = body.getPosition().y;
 		}
 		hitAnimate = Math.max(0, hitAnimate - delta*2);
 		if(health <= 0) {
 			alive = false;	
 			if(dyingAnimate==0 && hitAnimate>0) {
 				dyingAnimate-=hitAnimate;
 			}

			body.getFixtureList().get(0).setFilterData(GameInstance.enemyCollideFilter);
 		}
 		if(!alive) {
 			dyingAnimate = Math.min(1, dyingAnimate + delta*3);
 		}		
 	}
 
 	public void move() {
 		
 		if(Math.abs(body.getLinearVelocity().x)<0.05f) {
 			direction.x = -direction.x;
 		}		
 		
 		List<Contact> contactList = GameInstance.getInstance().world.getContactList();
 		for(int i = 0; i < contactList.size(); i++) {
 			Contact contact = contactList.get(i);
 			if(contact.isTouching()) {
 				if(contact.getFixtureA().getBody().getUserData().equals(this)) {
 					if(contact.getFixtureA().getBody().getPosition().y > contact.getFixtureB().getBody().getPosition().y-1) {
 						if(alive) {
 							body.setLinearVelocity(direction.x, body.getLinearVelocity().y);
 						}
 					}
 				}
 				if(contact.getFixtureB().getBody().getUserData().equals(this)) {
 					if(contact.getFixtureA().getBody().getPosition().y-1 < contact.getFixtureB().getBody().getPosition().y) {
 						if(alive) {
 							body.setLinearVelocity(direction.x, body.getLinearVelocity().y);
 						}					
 					} 
 				}
 			} else {
 				body.setLinearVelocity(direction.x*1f, body.getLinearVelocity().y);
 			}
 		}
 		
 		angle = (MathUtils.PI * -position.x) / 2.f;		
 	}
 	
 	public void hit(int damage) {
 		health -= damage;
 		if(health <= 0) {
 			alive = false;
 			body.getFixtureList().get(0).getFilterData().categoryBits = 0x0002;
 			body.getFixtureList().get(0).getFilterData().maskBits = 0x0004;
 		} 
 		if (dyingAnimate==0){
 			hitAnimate = 1;
 		}
 	}
 	
 }
