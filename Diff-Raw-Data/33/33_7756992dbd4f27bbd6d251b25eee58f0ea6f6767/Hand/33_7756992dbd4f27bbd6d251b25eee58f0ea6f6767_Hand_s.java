 package net.qmat.qmhh.models;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Vector;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Transform;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.FixtureDef;
 
 import net.qmat.qmhh.Main;
 import net.qmat.qmhh.controllers.Controllers;
 import net.qmat.qmhh.models.creatures.CreatureBase;
 import net.qmat.qmhh.utils.CPoint2;
 import net.qmat.qmhh.utils.PPoint2;
 
 import processing.opengl.*;
 import codeanticode.glgraphics.*;
 
 public class Hand extends ProcessingObject {
 
 	private float x = 0.0f;
 	private float y = 0.0f;
 	private boolean rebuildBeamP;
 	private Beam beam;
 	private boolean markedForRemovalP = false;
 	private boolean chargingP = true;
 	private Long startTime;
 	private Double maxHandSize = 30.0;
 	private ArrayList<CreatureBase> beamCreatures;
 
 	private float indexOffset;
 	private float cycle = 2000.0f;
 	
 	private static Long beamGrowTime = 1L * 1000000000L;
 	
 	private static GLTexture srcTex, bloomMask;
 	private static GLTexture tex0, tex2, tex4, tex8, tex16;
 	private static GLTextureFilter extractBloom, blur, blend4;
 	private static GLGraphicsOffScreen glg1;
 
 	public Hand(float x, float y) {
 		startTime = System.nanoTime();
 		beamCreatures = new ArrayList<CreatureBase>();
 		updatePosition(x, y);
 		rebuildBeamP = true;
 		indexOffset = p.random(1.0f);
 	}
 	
 	private void initGL() {
 		glg1 = new GLGraphicsOffScreen(p, 175, 175);
 	    extractBloom = new GLTextureFilter(p, "ExtractBloom.xml");
 	    blur = new GLTextureFilter(p, "Blur.xml");
 	    blend4 = new GLTextureFilter(p, "Blend4.xml");  
 	    int w = glg1.width;
 	    int h = glg1.height;
 	    
 	    // Initializing bloom mask and blur textures.
 	    bloomMask = new GLTexture(p, w, h, GLTexture.FLOAT);
 	    tex0 = new GLTexture(p, w, h, GLTexture.FLOAT);
 	    tex2 = new GLTexture(p, w / 2, h / 2, GLTexture.FLOAT);
 	    tex4 = new GLTexture(p, w / 4, h / 4, GLTexture.FLOAT);
 	    tex8 = new GLTexture(p, w / 8, h / 8, GLTexture.FLOAT);
 	    tex16 = new GLTexture(p, w / 16, h / 16, GLTexture.FLOAT);
 	}
 
 	public int nrBeamCreatures() {
 		return beamCreatures.size();
 	}
 	
 	public boolean hasReachedCenterP() {
 		return beam != null && beam.reachedCenterP;
 	}
 
 	public void addCreature(CreatureBase creature) {
 		if(nrBeamCreatures() == 0)
 			Controllers.getSoundController().beamBlocked();
		if(!beamCreatures.contains(creature))
			beamCreatures.add(creature);
 		System.out.println(nrBeamCreatures());
 	}
 
 	public void removeCreature(CreatureBase creature) {
		if(beamCreatures.contains(creature))
			beamCreatures.remove(creature);
 		if(nrBeamCreatures() == 0)
 			Controllers.getSoundController().beamUnblocked();
 		System.out.println(nrBeamCreatures());
 	}
 
 	public void updatePosition(float x, float y) {
 		this.x = x;
 		this.y = y;
 		rebuildBeamP = true;
 	}
 
 	public CPoint2 getCPosition() {
 		return new CPoint2(x, y);
 	}
 
 	private void rebuildBeam() {
 		if(beam != null) beam.rebuildShape();
 		else beam = new Beam(this);
 		rebuildBeamP = false;
 	}
 
 
 	public void markForRemoval() {
 		markedForRemovalP = true;
 	}
 
 	public boolean isMarkedForRemoval() {
 		return markedForRemovalP;
 	}
 
 	public void draw() {
 		if(!markedForRemovalP) {
 			if(bloomMask == null) initGL();
 			// are we still charging?
 			Long now = System.nanoTime();
 			Double chargeIndex = (now - startTime) / HandsModel.chargeTimeNano;
 			Double handSize;
 			if(chargeIndex < 1.0) {
 				chargingP = true;
 				rebuildBeamP = false;
 			} else {
 				chargingP = false;
 				rebuildBeamP = true;
 			}
 			
 			// rebuild and draw the beam
 			if(rebuildBeamP) rebuildBeam();
 			if(beam != null) beam.draw();
 
 			// draw hand		
 			glg1.beginDraw();
 			glg1.background(0);
 			//glg1.fill(200);
 			//glg1.stroke(255);
 			glg1.fill(237, 212, 69, 150);
 			glg1.stroke(234, 187, 31);
 			glg1.pushMatrix();
 			glg1.translate(glg1.width/2.0f,glg1.height/2.0f);
 			glg1.beginShape();
 			
 			int steps = 51;
 			int now2 = p.millis();
 			float index = (now2 % cycle) / cycle + indexOffset;
 			
 			for(int i=0; i<steps+1; i++) {
 				float angle = (Main.TWO_PI) / steps * i;
 				float length = 60.0f;
 				if(i % 2 == 0)
 					length += 1.0 + Main.sin((index+i*0.09f) * Main.TWO_PI) * 20.0f;
 				float o = Main.sin(angle) * length;
 				float a = Main.cos(angle) * length;
 				//curveVertex(o, a);
 				glg1.vertex(o, a);
 			}
 			glg1.endShape(Main.CLOSE);
 			glg1.popMatrix();
 			glg1.endDraw();
 			
 			// Extracting the bright regions from input texture.
 			srcTex = glg1.getTexture();
 		    extractBloom.setParameterValue("bright_threshold", 0.01f);
 		    extractBloom.apply(srcTex, tex0);
 		  
 		    // Downsampling with blur.
 		    tex0.filter(blur, tex2);
 		    tex2.filter(blur, tex4);    
 		    tex4.filter(blur, tex8);    
 		    tex8.filter(blur, tex16);     
 		    
 		    // Blending downsampled textures.
 		    blend4.apply(new GLTexture[]{tex2, tex4, tex8, tex16}, new GLTexture[]{bloomMask});
 			
 			p.pushMatrix();
 			p.translate(x, y);
 			p.image(bloomMask, 0, 0, srcTex.width, srcTex.height);
 			p.popMatrix();
 		}
 	}
 
 	public void destroy() {
 		if(beam != null) beam.destroy();
 	}
 
 	public class Beam extends ProcessingObject {
 
 		private Body body;
 		public Hand hand;
 		private boolean reachedCenterP = false;
 		private Long beamStartTimestamp;
 		private PPoint2[] corners; 
 		// in milliseconds
 		private float rippleCycle = 2000f;
 
 		Beam(Hand hand) {
 			beamStartTimestamp = System.nanoTime();
 			this.hand = hand;
 			makeBody();
 		}
 
 		private void rebuildShape() {
 			// update size of the body
 			Fixture f = body.getFixtureList();
 			while(f != null) {
 				PolygonShape sd = (PolygonShape)f.m_shape;
 				Vec2[] vs = getShapeVertices();
 				sd.set(vs, 4);
 				f = f.getNext();
 			}
 		}
 
 		private void makeBody() {
 			FixtureDef fd = createFixture();
 			BodyDef bd = new BodyDef();
			bd.type = BodyType.STATIC;
 			// set position to be in between the center and the hand position
 			//bd.position.set(box2d.coordPixelsToWorld(new Vec2((hand.x + Main.centerX)*0.5f, (hand.y + Main.centerY)*0.5f)));
 			// or set the position of the static body to the middle
 			bd.position.set(box2d.coordPixelsToWorld(new Vec2(Main.centerX, Main.centerY)));
 			body = box2d.createBody(bd);
 			body.createFixture(fd);
 			body.setUserData(this);
 		}
 
 		private FixtureDef createFixture() {
 			PolygonShape sd = new PolygonShape();
 			Vec2[] vs = getShapeVertices();
 			sd.set(vs, 4);
 			FixtureDef fd = new FixtureDef();
 			fd.shape = sd;
 			fd.isSensor = true;
 			return fd;
 		}
 
 		private Vec2[] getShapeVertices() {
 			PPoint2 handPos = new CPoint2(x, y).toPPoint2();
 			// TODO: calculate the actual angleOffset from the hand size
 			float angleOffset = Main.TWO_PI / 92.0f;
 			float stopRadius = 0.0f;
 			// have to decide the radius myself..
 			Long now = System.nanoTime();
 			Double beamGrowthIndex = (now - beamStartTimestamp) / beamGrowTime.doubleValue();
 			if(beamGrowthIndex >= 1.0) 
 				reachedCenterP = true;
 			if(reachedCenterP) {
 				stopRadius = getGreatestRadius();
 			} else {
 				stopRadius = (1.0f - beamGrowthIndex.floatValue()) * Main.outerRingInnerRadius; 
 			}
 			corners = new PPoint2[4];
 			corners[0] = new PPoint2(handPos.r, handPos.t - angleOffset);
 			corners[1] = new PPoint2(handPos.r, handPos.t + angleOffset);
 			corners[2] = new PPoint2(stopRadius, handPos.t + angleOffset);
 			corners[3] = new PPoint2(stopRadius, handPos.t - angleOffset);
 			Vec2 vs[] = new Vec2[4];
 			vs[0] = box2d.coordPixelsToWorld(corners[0].toVec2());
 			vs[1] = box2d.coordPixelsToWorld(corners[1].toVec2());
 			vs[2] = box2d.coordPixelsToWorld(corners[2].toVec2());
 			vs[3] = box2d.coordPixelsToWorld(corners[3].toVec2());
 			return vs;
 		}
 
 		public void destroy() {
 			box2d.destroyBody(body);
 		}
 
 		public float getGreatestRadius() {
 			float r = 10.0f;
 			for(CreatureBase c : hand.beamCreatures) {
 				float cr = c.getPPosition().r + c.getRadius();
 				if(cr > r) r = cr;
 			}
 			return r;
 		}
 		
 		private void getBeamCoordinates(CPoint2 p1, CPoint2 p2, Vector<CPoint2> cs, float reverse) {
 			float stepSize = 20.0f;
 			float length = Main.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
 			int steps = (int)(length / stepSize);
 			float angle = Main.atan2(p2.y-p1.y, p2.x-p1.x);
 			float cycleIndex = (p.millis() % rippleCycle) / rippleCycle; 
 			for(int step=0; step<steps; step++) {
 				float ds = stepSize * step;
 				float dc = (1.0f + Main.sin((cycleIndex + reverse*step*0.3f) * Main.TWO_PI)) / 2.0f * 8.0f + 3.0f;
 				float dx = ds * Main.cos(angle) + dc * Main.cos(Main.PI/2+angle);
 				float dy = ds * Main.sin(angle) + dc * Main.sin(Main.PI/2+angle);
 				cs.add(new CPoint2(p1.x + dx, p1.y + dy));
 			}
 		}
 
 		public void draw() {
 			//System.out.println("creatures: " + hand.beamCreatures.size());
 			if(corners != null) {
 				
 				// side 1
 				Vector<CPoint2> cs1 = new Vector<CPoint2>();
 				CPoint2 p1 = corners[1].toCPoint2();
 				CPoint2 p2 = corners[2].toCPoint2();
 				getBeamCoordinates(p1, p2, cs1, -1.0f);
 				// side 2
 				Vector<CPoint2> cs2 = new Vector<CPoint2>();
 				p1 = corners[3].toCPoint2();
 				p2 = corners[0].toCPoint2();
 				getBeamCoordinates(p1, p2, cs2, 1.0f);
 				if(cs1.size() > 1 && cs2.size() > 1) {
 					if(hand.nrBeamCreatures() <= 0) {
 						PPoint2 middle1 = cs1.lastElement().toPPoint2();
 						PPoint2 middle2 = cs2.firstElement().toPPoint2();
 						// add tip
 						float avgAngle = (middle2.t+middle2.t) / 2.0f;
 						float avgRadius;
 						if(middle2.r > 20f) {
 							avgRadius = (middle1.r+middle2.r) / 2.0f * 0.7f;
 						} else {
 							avgRadius = 0.0f;
 						}
 						cs1.add(new PPoint2(avgRadius, avgAngle).toCPoint2());
 					}
 					cs1.addAll(cs2);
 					
 					// draw beam
 					p.fill(237, 212, 69, 100);
 					p.noStroke();
 					p.beginShape();
 					for(CPoint2 cpos : cs1) {
 						p.curveVertex(cpos.x, cpos.y);
 					}
 					p.endShape(Main.CLOSE);
 				}
 			}
 			
 			/*
 			p.beginShape();
 			for(Fixture f=body.getFixtureList(); f!=null; f=f.getNext()) {
 				PolygonShape shape = (PolygonShape) f.getShape();
 				Transform transform = body.getTransform();
 				for(int i=0; i<shape.getVertexCount(); i++) {
 					Vec2 pos = shape.getVertex(i);
 					Vec2 v2 = box2d.coordWorldToPixels(Transform.mul(transform, pos));
 					p.vertex(v2.x, v2.y);
 				}
 			}
 			p.endShape(Main.CLOSE);
 			*/
 		}
 
 	}
 
 }
