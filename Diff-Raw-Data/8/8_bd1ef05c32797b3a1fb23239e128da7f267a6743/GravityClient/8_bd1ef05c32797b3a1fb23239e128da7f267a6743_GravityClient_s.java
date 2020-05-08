 package com.evanreidland.e.gravity;
 
 import com.evanreidland.e.Game;
 import com.evanreidland.e.Resource;
 import com.evanreidland.e.Vector3;
 import com.evanreidland.e.engine;
 import com.evanreidland.e.roll;
 import com.evanreidland.e.client.EApplet;
 import com.evanreidland.e.client.EApplication;
 import com.evanreidland.e.client.GameClient;
 import com.evanreidland.e.client.control.input;
 import com.evanreidland.e.client.control.key;
 import com.evanreidland.e.client.ent.Planet;
 import com.evanreidland.e.client.ent.Ship;
 import com.evanreidland.e.ent.Entity;
 import com.evanreidland.e.ent.ents;
 import com.evanreidland.e.graphics.Model;
 import com.evanreidland.e.graphics.Sprite;
 import com.evanreidland.e.graphics.font;
 import com.evanreidland.e.graphics.generate;
 import com.evanreidland.e.graphics.graphics;
 import com.evanreidland.e.graphics.Model.ModelType;
 
 public class GravityClient extends GameClient {
 	Resource font1;
 	
 	Model shipModel;
 	
 	Sprite planetSprite;
 	
 	Resource skybox;
 	
 	Ship ship;
 	Planet planet;
 	
 	long nextShot;
 	
 	double viewHeight = 0.01f;
 	
 	public void drawRing(Vector3 origin, double rad, int numPoints) {
 		Vector3 lp = origin.plus(Vector3.fromAngle2d(0).multipliedBy(rad));
 		for ( int i = 1; i <= numPoints; i++ ) {
 			Vector3 np = origin.plus(Vector3.fromAngle2d((i/(double)numPoints)*engine.Pi2).multipliedBy(rad));
 			graphics.drawLine(lp, np, 1, 1, 1, 1, 0.5f);
 			lp.setAs(np);
 		}
 	}
 	
 	public void onUpdate() {
 		double speed = getDelta();
 		if ( input.getKeyState(key.KEY_1) && Game.getTime() >= nextShot ) {
 			Entity ent = ents.Create("missile");
 			ent.pos = ship.pos.plus(ship.angle.getForward().multipliedBy(0.01f));
 			ent.vel = ship.vel.plus(ship.angle.getForward().multipliedBy(5));
 			nextShot = Game.getTime() + 100;
 		}
 		if ( input.getKeyState(key.KEY_9) ) {
 			viewHeight += speed;
 		}
 		if ( input.getKeyState(key.KEY_8) ) {
 			viewHeight -= speed;
 		}
 		if ( viewHeight < 0.01f ) viewHeight = 0.01f;
 		if ( viewHeight > 1 ) viewHeight = 1;
 		if ( input.getKeyState(key.KEY_SHIFT) ) {
 			speed *= 10;
 			if ( input.getKeyState(key.KEY_UP) ) {
 				ship.vel.add(ship.angle.getForward().multipliedBy(speed));
 			}
 			if ( input.getKeyState(key.KEY_DOWN) ) {
 				ship.vel.add(ship.angle.getForward().multipliedBy(-speed));
 			}
 			if ( input.getKeyState(key.KEY_LEFT) ) {
 				ship.vel.add(ship.angle.getRight().multipliedBy(-speed));
 			}
 			if ( input.getKeyState(key.KEY_RIGHT) ) {
 				ship.vel.add(ship.angle.getRight().multipliedBy(speed));
 			}
 		} else {
			
 			if ( input.getKeyState(key.KEY_CONTROL) ) {
 				speed *= 10;
 				if ( input.getKeyState(key.KEY_UP) ) {
 					ship.vel.add(ship.angle.getUp().multipliedBy(speed));
 				}
 				if ( input.getKeyState(key.KEY_DOWN) ) {
 					ship.vel.add(ship.angle.getUp().multipliedBy(-speed));
 				}
 				if ( input.getKeyState(key.KEY_LEFT) ) {
 					ship.vel.add(ship.angle.getRight().multipliedBy(-speed));
 				}
 				if ( input.getKeyState(key.KEY_RIGHT) ) {
 					ship.vel.add(ship.angle.getRight().multipliedBy(speed));
 				}
 				
 				if ( input.getKeyState(key.KEY_C) ) {
 					ship.vel.setAs(0, 0, 0);
 					ship.angleVel.setAs(0, 0, 0);
 				}
 			} else {
 				if ( ship.angle.x < engine.Pi ) {
 					speed = -speed;
 				}
 				if ( input.getKeyState(key.KEY_LEFT) ) {
 					ship.angleVel.z -= speed;//Math.cos(ship.angle.y)*speed;
 				}
 				if ( input.getKeyState(key.KEY_RIGHT) ) {
 					ship.angleVel.z += speed;//Math.cos(ship.angle.y)*speed;
 				}
 				
 				if ( input.getKeyState(key.KEY_UP) ) {
 					ship.angleVel.x += Math.abs(speed);
 				}
 				if ( input.getKeyState(key.KEY_DOWN) ) {
 					ship.angleVel.x -= Math.abs(speed);
 				}
 			}
 		}
 		
 		if ( input.getKeyState(key.KEY_SPACE) ) {
 			ship.angle.setAs(planet.pos.minus(ship.pos).getAngle());
 			ship.angleVel.setAs(planet.pos.minus(ship.pos.plus(ship.vel)).getAngle().minus(planet.pos.minus(ship.pos).getAngle()));
 		}
 		
 		ents.list.simulateGravity(getDelta());
 		ents.list.onThink();
 		
 		graphics.camera.angle.setAs(ship.angle);//planet.pos.minus(ship.pos).getAngle());
 		graphics.camera.pos.setAs(ship.pos.plus(graphics.camera.getForward().multipliedBy(-viewHeight*2.5f)).plus(graphics.camera.getUp().multipliedBy(viewHeight)));
 	}
 
 	public void onRender() {
 		ents.list.onRender();
 	
 		graphics.unbindTexture();
 		for ( int i = 1; i < 10; i++ ) {			
 			drawRing(planet.pos, i*100, i*100);
 		}
 		
 		for ( int i = 0; i < 50; i++ ) {
 			graphics.drawLine(planet.pos, planet.pos.plus(Vector3.fromAngle2d((i/50f)*engine.Pi2).multipliedBy(900)), 1, 1, 1, 0, 0.5f);
 		}
 		
 		graphics.drawSkybox(skybox, graphics.camera.farDist - 1);
 	}
 
 	public void onRenderHUD() {
 		font.Render2d(font1, "Pos: " + ship.pos.toRoundedString(), graphics.camera.bottomLeft().plus(0, 16, 0), 16, false);
 		font.Render2d(font1, "Ang: " + ship.angle.clipAngle().toRoundedString(), graphics.camera.bottomLeft().plus(0, 32, 0), 16, false);
 		font.Render2d(font1, "Delta: " + Game.getDelta(), graphics.camera.bottomLeft().plus(0, 48, 0), 16, false);
 		
 		double s = 10;
 		graphics.unbindTexture();
 		graphics.drawLine(new Vector3(-s, 0, 0), new Vector3(0, -s, 0), 2, 1, 1, 1, 0.5f);
 		graphics.drawLine(new Vector3(0, -s, 0), new Vector3(s, 0, 0), 2, 1, 1, 1, 0.5f);
 		graphics.drawLine(new Vector3(s, 0, 0), new Vector3(0, s, 0), 2, 1, 1, 1, 0.5f);
 		graphics.drawLine(new Vector3(0, s, 0), new Vector3(-s, 0, 0), 2, 1, 1, 1, 0.5f);
 	}
 	
 	public void registerEntities() {
 		ents.Register("missile", TestInterceptor.class);
 	}
 	
 	public void createEntities() {
 		ship = (Ship)ents.Create("ship");
 		planet = (Planet)ents.Create("planet");
 		
 		ship.model = shipModel;
 		ship.mass = 0.0001f;
 		ship.bStatic = false;
 		ship.pos = new Vector3(200, 0, 0);
 		
 		nextShot = 0;
 		
 		planet.mass = 100;
 		planet.radius = 100;
 		planet.sprite = planetSprite;
 		planet.bStatic = true;
 		
 		ship.vel = new Vector3(0, ship.getOrbitalVelocity(ship.pos.x, planet.mass), 0);
 		
 		double num = 10;
 		
 		for ( double i = 0; i < num; i++ ) {
 			Planet ent = (Planet)ents.Create("planet");
 			
 			ent.sprite = new Sprite(0, 0, engine.loadTexture("planet2.png"));
 			ent.mass = 10;
 			ent.radius = 5;
 			
 			double rad = (i + 1)*100;
 		
 			ent.bStatic = false;
 			
 			double angle = (i/num)*engine.Pi2;
 			
 			ent.vel = new Vector3((double)Math.cos(angle + engine.Pi_2), (double)Math.sin(angle + engine.Pi_2), 0)
 			.multipliedBy(ent.getOrbitalVelocity(rad, planet.mass));
 			
 			ent.pos.setAs(planet.pos.plus(Vector3.fromAngle2d(angle).multipliedBy(rad)));
 			
 			Planet moon = (Planet)ents.Create("planet");
 			moon.bStatic = false;
 			moon.sprite = new Sprite(0, 0, engine.loadTexture("moon1.png"));
 			moon.radius = ent.radius/3;
 			moon.mass = moon.radius/ent.radius;
 			moon.pos = ent.pos.plus(Vector3.fromAngle2d(roll.randomDouble(0, engine.Pi2)).multipliedBy(ent.radius + roll.randomDouble(1, 5)));
 			moon.vel = ent.vel.plus(ent.pos.minus(moon.pos).getAngle().getRight().multipliedBy(moon.getOrbitalVelocity(ent)));
 		}
 		
 		ship.angle = planet.pos.minus(ship.pos).getAngle();
 	}
 	
 	public void loadGraphics() {
 		planetSprite = new Sprite(32, 32, engine.loadTexture("planet1.png"));
 		
 		generate.setModelType(ModelType.RenderList);
 		shipModel = generate.Cube(new Vector3(0, 0, 0), new Vector3(0.01f, 0.01f, 0.01f), new Vector3());
 		shipModel.tex = engine.loadTexture("shiptest1.png");
 		
 		font.buildFont("Courier New", 32, true, false);
 		font1 = engine.loadFont("Courier Newx32");
 		
 		skybox = engine.loadTexture("skybox1.png");
 	}
 	
 	public void loadSound() {
 		//I'm going to have something here eventually.
 	}
 	
 	public void onInit() {
 		super.onInit();
 		
 		registerEntities();
 		loadGraphics();
 		loadSound();
 		createEntities();
 		
 		graphics.camera.farDist = 10000000;
 	}
 	
 	public static void main(String[] args) {
 		EApplication app = new EApplication(new EApplet());
 		engine.game = new GravityClient();
 		app.runApplet("Gravity Alpha v0.1");
 	}
 }
