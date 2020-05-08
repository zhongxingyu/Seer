 package beothorn.labs.core;
 
 import static playn.core.PlayN.assets;
 import static playn.core.PlayN.graphics;
 import static playn.core.PlayN.log;
 import static playn.core.PlayN.pointer;
 
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 
 import playn.core.Image;
 import playn.core.ImageLayer;
 import playn.core.Pointer;
 import playn.core.ResourceCallback;
 import beothorn.fingerball.ImageToBody;
 import beothorn.fingerball.MetersToPixels;
 import beothorn.fingerball.PhysicBody;
 import beothorn.fingerball.PhysicBodyImpl;
 import beothorn.fingerball.PointMeters;
 import beothorn.fingerball.PointPixels;
 
 public class Ball {
 
 	public static String IMAGE = "images/soccerBall.png";
	private static final float BALL_RADIUS = 0.2f;
 	
 	private float radius;
 	private ImageToBody imageToBody;
 
 	public Ball(World world,final MetersToPixels metersToPixels, final float x, final float y) {
 
 		final PhysicBody ballBody = createBallBody(world, x, y); 
 		ImageLayer ballImage= createBallImage(world);
 		
 		imageToBody = new ImageToBody(ballImage, ballBody, metersToPixels);
 		
 		pointer().setListener(new Pointer.Adapter() {
 			@Override
 			public void onPointerStart(Pointer.Event event) {
 				PointPixels clickPosition = new PointPixels((int)event.x(), (int)event.y());
 				PointMeters pixelsToMeters = metersToPixels.pixelsToMeters(clickPosition);
 				
 				float deltaX = imageToBody.getPhisicalX()- pixelsToMeters.x;
 				Vec2 impulse = new Vec2(deltaX, imageToBody.getPhisicalY() - pixelsToMeters.y);
 				impulse.normalize();
				Vec2 impulseForce = impulse.mul(0.02f);
 				ballBody.applyLinearImpulse(impulseForce);
 				ballBody.applyAngularImpulse(impulseForce.x*0.1f);
 			}
 		});
 	}
 
 	private ImageLayer createBallImage(World world) {
 		final ImageLayer ballImage;
 		Image image = assets().getImage(IMAGE);
 		ballImage = graphics().createImageLayer(image);
 		image.addCallback(new ResourceCallback<Image>() {
 			@Override
 			public void done(Image image) {
 				setOriginAtCenter(image);
 				graphics().rootLayer().add(ballImage);
 			}
 
 			private void setOriginAtCenter(Image image) {
 				radius = image.width()/2f;
 				ballImage.setOrigin(radius, radius);
 			}
 
 			@Override
 			public void error(Throwable err) {
 				log().error("Error loading image!", err);
 			}
 		});
 		return ballImage;
 	}
 
 	private PhysicBody createBallBody(World world,float x, float y) {
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position = new Vec2(0, 0);
 		Body body = world.createBody(bodyDef);
 
 		CircleShape circleShape = new CircleShape();
 		circleShape.m_radius = BALL_RADIUS;
 		circleShape.m_p.set(0, 0);
 		
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = circleShape;
 		fixtureDef.density = 0.4f;
 		fixtureDef.friction = 0.1f;
 		fixtureDef.restitution = 0.5f;
 		
 		body.createFixture(fixtureDef);
 		body.setLinearDamping(0.3f);
 		body.setTransform(new Vec2(x, y), 0);
 		return new PhysicBodyImpl(body);
 	}
 
 	public void update() {
 		imageToBody.update();
 	}
 }
