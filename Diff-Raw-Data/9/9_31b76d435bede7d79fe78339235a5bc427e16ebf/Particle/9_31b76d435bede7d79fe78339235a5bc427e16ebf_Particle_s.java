 package propinquity;
 
 import org.jbox2d.collision.shapes.CircleDef;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 
 import processing.core.*;
 
 public class Particle {
 
 	/**
 	 * The minimum amount of time a player must have their glove in the sweet
 	 * spot to get a single point.
 	 */
 	public static final float SPAWN_DELAY = 0.5f;
 
 	public Vec2 position;
 
 	private Body body;
 	private CircleDef shape;
 
 	private float scale;
 	private PGraphics texture;
 	private Color color;
 
 	private Propinquity parent;
 
 	public Particle(Propinquity parent, Vec2 position, PGraphics texture, Color color) {
 
 		this.parent = parent;
 		this.position = position;
 		this.texture = texture;
 		this.color = color;
 
 		scale = 0.5f;
 
 		shape = new CircleDef();
		shape.radius = parent.box2d.scalarPixelsToWorld((texture.width - 10) * scale / 2f);
 		shape.density = 1.0f;
 		shape.friction = 0.01f;
 		shape.restitution = 0.3f;
 
 		BodyDef bd = new BodyDef();
 		bd.position.set(parent.box2d.coordPixelsToWorld(position));
 
 		body = parent.box2d.createBody(bd);
 		body.createShape(shape);
 		body.setMassFromShapes();
 	}
 
 	public void kill() {
 		parent.box2d.destroyBody(body);
 	}
 
 	public Body getBody() {
 		return body;
 	}
 	
 	public void update() {
 
 	}
 
 	public void draw() {
 
 		position = parent.box2d.getBodyPixelCoord(body);
 
 		parent.pushMatrix();
 		parent.translate(position.x, position.y);
 		parent.scale(scale * texture.width / 2f);
 		parent.beginShape();
 		parent.texture(texture);
 		parent.tint(color.toInt(parent));
 		parent.vertex(-1, -1, 0, 0, 0);
 		parent.vertex(1, -1, 0, 1, 0);
 		parent.vertex(1, 1, 0, 1, 1);
 		parent.vertex(-1, 1, 0, 0, 1);
 		parent.noTint();
 		parent.endShape();
 		parent.popMatrix();
 	}
 }
