 package entity;
 
 import sound.SoundEffect;
 import networking.NetworkObjectType;
 import gfx.ResourceManager;
 import baseGame.Rendering.RGBImage;
 import baseGame.Rendering.Renderer;
 import level.BasicLevel;
 
 public class Bullet extends Projectile {
 	
 	int explosionRadius = 10;
 	int explosionPower = 40;
 	
 	public Bullet(double x, double y, BasicLevel level, double angle) {
 		super(x, y, 2, 2, level, 1, angle);
 		this.maxSpeed = 15;
 		this.frictionConstant = 0.000005;
 		this.angle = angle;
 		this.dx = dx * maxSpeed;
 		this.dy = dy * maxSpeed;
 		
 	}
 
 	@Override
 	public void explode() {
 		
 		level.getTerrain().addExplosion((int) (x - explosionRadius), (int) (y - explosionRadius), explosionRadius);
 		level.addEntity(new Explosion(x, y, explosionRadius + 2, level, explosionPower));
 //		if(!SoundEffect.THUD.isRunning())
 			SoundEffect.THUD.play();
 		
 	}
 
 	public boolean intersectsTerrain() {
 		for (FloatingPoint point : hitbox) {
 			if (level.getTerrain().hitTestpoint((int) (x + point.getX()), (int) (y + point.getY()))){
 				while(level.getTerrain().hitTestpoint((int) (x + point.getX()), (int) (y + point.getY()))){
 					setLocation(x-dx/4, y-dy/4);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean handleIntersections() {
 		for (int i = 0; i < level.getPlayers().size(); i++) {
 			if (intersectsEntity(level.getPlayers().get(i)))
 				return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void tick(double dt){
 		super.tick(dt);
 		if(intersectsTerrain() || handleIntersections()){
 			explode();
 			super.remove();
 		}
 	}
 	
 	@Override
 	public void remove(){
 		
 		super.remove();
 		explode();
 	}
 	@Override
 	public void render(Renderer renderer) {
 		RGBImage img = ResourceManager.BULLET;
 		renderer.DrawImage(img, -1, (int) (x - 1), (int) (y - 1), img.getWidth(), img.getHeight());
 	}
 
 	@Override
 	public void initNetworkValues() {
 		// TODO Auto-generated method stub
 		setNetworkObjectType(NetworkObjectType.BULLET);
 		
 	}
 
 	
 
 }
