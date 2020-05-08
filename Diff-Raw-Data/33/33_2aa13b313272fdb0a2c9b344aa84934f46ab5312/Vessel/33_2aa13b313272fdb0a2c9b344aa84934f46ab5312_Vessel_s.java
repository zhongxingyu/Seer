 package org.skullforge.asteroidpush.arena.entities;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Mat22;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.common.MathUtils;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.skullforge.asteroidpush.arena.Entity;
 import org.skullforge.asteroidpush.arena.SignalTracker;
 import org.skullforge.asteroidpush.arena.Viewport;
 
 public class Vessel implements Entity {
 
    public Vessel(Image image) throws SlickException {
       vesselImage = image;
       body = null;
    }
 
    @Override
    public void render(Viewport view) {
       if (body != null) {
          view.showImage(vesselImage, body.getPosition(), body.getAngle(), size);
       }
    }
 
    @Override
    public void update(int delta, SignalTracker signals) {
      // Do nothing
       Vec2 thrust = new Vec2(500000.0f, 0.0f);
       thrust = Mat22.createRotationalTransform(body.getAngle()).mul(thrust);
       
       if (signals.getForwardThrust() > 0.0f) {
          body.applyForce(thrust, body.getPosition());
       }
       if (signals.getAnticlockwiseThrust() > 0.0f) {
          body.applyTorque(-500000.0f);
       }
       if (signals.getClockwiseThrust() > 0.0f) {
          body.applyTorque(500000.0f);
       }
    }
 
    @Override
    public void spawn(World world, Vec2 position) {
       body = world.createBody(GetBodyDef(position));
       body.createFixture(GetFixureDef());
    }
 
    @Override
    public Vec2 getPosition() {
       if (body != null) {
          return body.getPosition();
       } else {
          return new Vec2(0.0f, 0.0f);
       }
    }
 
    private BodyDef GetBodyDef(Vec2 position) {
       BodyDef def = new BodyDef();
       def.type = BodyType.DYNAMIC;
       def.position.set(position);
       def.angle = MathUtils.PI * -0.7f;
       def.angularDamping = 0.5f;
       return def;
    }
 
    private FixtureDef GetFixureDef() {
       PolygonShape shape = new PolygonShape();
       shape.setAsBox(size.x / 2.0f, size.y / 2.0f);
       FixtureDef def = new FixtureDef();
       def.density = 1000.0f;
       def.shape = shape;
       def.friction = 0.3f;
       return def;
    }
 
    final Vec2 size = new Vec2(4.0f, 4.0f);
    private Image vesselImage;
    private Body body;
 }
