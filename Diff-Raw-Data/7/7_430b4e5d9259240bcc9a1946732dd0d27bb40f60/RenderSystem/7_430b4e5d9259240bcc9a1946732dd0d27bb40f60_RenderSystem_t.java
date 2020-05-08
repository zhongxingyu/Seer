 package CEF;
 
 import logic.Engine;
 import static logic.level.Level.FOV;
 import CEF.Components.*;
 import CEF.Groups.*;
 
 
 import com.artemis.Aspect;
 import com.artemis.Component;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.EntitySystem;
 import com.artemis.annotations.Mapper;
 import com.artemis.managers.GroupManager;
 import com.artemis.managers.TagManager;
 import com.artemis.utils.Bag;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * System that renders the field
  * @author nhydock
  */
 public class RenderSystem extends EntitySystem {
 
 	public RenderSystem() {
 		super(Aspect.getAspectForAll(Renderable.class));
 	}
 
 	@Mapper ComponentMapper<Position> pmap;
 	@Mapper ComponentMapper<Angle> amap;
 	@Mapper ComponentMapper<Scrollable> smap;
 	@Mapper ComponentMapper<Renderable> rmap;
 	
 	@Mapper ComponentMapper<Player> playermap;
 	@Mapper ComponentMapper<Enemy> enemymap;
 
 	//system has its own drawing components
 	Matrix4 camera;
 
 	public boolean warning;
 	
 	
 	@Override
 	protected void initialize()
 	{
 		camera = new Matrix4();
 		camera.setToOrtho2D(0, 0, 240, 320);
 		camera.translate(FOV[0], FOV[1], 0);
 	}
 	
 	@Override
 	protected boolean checkProcessing() {
 		return true;
 	}
 
 	@Override
 	protected void processEntities(ImmutableBag<Entity> entities) {
 		for (int i = 0; i < entities.size(); i++)
 		{
 			Entity e = entities.get(i);
 			Renderable r = rmap.get(e);
 			
 			Angle a = amap.getSafe(e);
 			if (a != null)
 				r.sprite.setRotation(a.degrees);
 			Position p = pmap.getSafe(e);
 			if (p != null)
 				r.sprite.setPosition(p.location.x+p.offset.x, p.location.y+p.offset.y);
 			
 			//scroll sprite's texture if renderable has scrollable as well
 			Scrollable s = smap.getSafe(e);
 			if (s != null)
 			{
 				s.update(world.delta);
 				r.sprite.setRegion(s.u1, s.v1, s.u2, s.v2);
 			}
 		}
 	}
 	
 	public boolean inView(Vector2 loc)
 	{
 		return loc.x >= FOV[0] && loc.x <= FOV[2] && loc.y >= FOV[1] && loc.y <= FOV[3];
 	}
 
 	
 	public void draw(SpriteBatch batch)
 	{
 		this.process();
 		
 		//temp variable name holders
 		Renderable r;
 		Entity e;
 		
 		GroupManager gm = this.world.getManager(GroupManager.class);
 		TagManager tm = this.world.getManager(TagManager.class);
 		
 		ImmutableBag<Entity> bag;
 		
 		//we group the different parts to maximize efficiency
 		
 		batch.setProjectionMatrix(camera);
		bag = this.world.getManager(GroupManager.class).getEntities("Field");
		batch.begin();
 		{
			//render the field
 			for (int i = 0; i < bag.size(); i++)
 			{
 				e = bag.get(i);
 				r = rmap.get(e);
 				r.sprite.draw(batch);
 			}
 		}
 		batch.end();
 		
 		batch.begin();
 		{
 			//draw warning banners
 			//Display banners;
 			Ammo a = (Ammo)Engine.player.getComponent(Ammo.CType);
 			Health h = (Health)Engine.player.getComponent(Health.CType);
 			bag = gm.getEntities("Banner");
 			ImmutableBag<Entity> bannerBag;
 			Bag<Entity> bannerBag2;
 			
 			if (h.isLow())
 			{
 				bannerBag = gm.getEntities("BannerA");
 				bannerBag2 = (Bag<Entity>)gm.getEntities("BannerB");
 			}
 			else if (a.isLow())
 			{
 				bannerBag = gm.getEntities("BannerB");
 				bannerBag2 = (Bag<Entity>)gm.getEntities("BannerA");
 			}
 			else
 			{
 				bannerBag = gm.getEntities("BannerC");
 				bannerBag2 = new Bag<Entity>();
 				bannerBag2.addAll(gm.getEntities("BannerA"));
 				bannerBag2.addAll(gm.getEntities("BannerB"));
 			}
 			
 			for (int i = bag.size()-1; i >= 0; i--)
 			{
 				e = bag.get(i);
 				Position p = pmap.getSafe(e);
 				r = rmap.get(e);
 				
 				if (bannerBag.contains(e))
 				{
 					//right side banner
 					if (p.offset.x < 0)
 					{
 						p.location.x = Math.max(190, p.location.x - 32 * world.delta);
 					}
 					else
 					{
 						p.location.x = Math.min(0, p.location.x + 32 * world.delta);
 					}
 				}
 				else if (bannerBag2.contains(e))
 				{
 					//right side banner
 					if (p.offset.x < 0)
 					{
 						p.location.x = Math.min(190-p.offset.x, p.location.x + 32 * world.delta);
 					}
 					else
 					{
 						p.location.x = Math.max(-12, p.location.x - 32 * world.delta);
 					}
 				}
 				
 				r.sprite.draw(batch);
 			}
 		}
 		batch.end();
 		
 		//render the player
 		batch.begin();
 		{
 			e = tm.getEntity("PlayerShadow");
 			r = rmap.get(e);
 			r.sprite.draw(batch);
 			e = Engine.player;
 			r = rmap.get(e);
 			r.sprite.draw(batch);
 		}
 		batch.end();
 
 		bag = gm.getEntities(CEF.Groups.Bullet.TYPE);
 		batch.begin();
 		{
 			//render enemy bullets
 			for (int i = 0; i < bag.size(); i++)
 			{
 				e = bag.get(i);
 				Enemy p = enemymap.getSafe(e);
 				if (p != null)
 				{
 					r = (Renderable)e.getComponent(Renderable.CType);
 					r.sprite.draw(batch);
 				}
 			}
 		}
 		batch.end();
 		
 		bag = gm.getEntities(CEF.Groups.Enemy.TYPE);
 		batch.begin();
 		{
 			//render enemies
 			for (int i = 0; i < bag.size(); i++)
 			{
 				e = bag.get(i);
 				r = (Renderable)e.getComponent(Renderable.CType);
 				r.sprite.draw(batch);
 			}
 		}
 		batch.end();
 		
 		bag = gm.getEntities(CEF.Groups.Bullet.TYPE);
 		batch.begin();
 		{
 			//render player bullets
 			for (int i = 0; i < bag.size(); i++)
 			{
 				e = bag.get(i);
 				Player p = playermap.getSafe(e);
 				if (p != null)
 				{
 					r = (Renderable)e.getComponent(Renderable.CType);
 					r.sprite.draw(batch);
 				}
 			}
 		}
 		batch.end();
 		
 		bag = gm.getEntities("Dead");
 		batch.begin();
 		{
 			//render explosions
 			for (int i = 0; i < bag.size(); i++)
 			{
 				e = bag.get(i);
 				r = (Renderable)e.getComponent(Renderable.CType);
 				r.sprite.draw(batch);
 				Animation a = (Animation)e.getComponent(Animation.CType);
 				if (a.done)
 					e.deleteFromWorld();
 			}
 		}
 		batch.end();
 		
 		batch.begin();
 		//draw game over banner
 		if (Engine.GameOver)
 		{
 			r = (Renderable)tm.getEntity("GameOver").getComponent(Renderable.CType);
 			r.sprite.draw(batch);
 		}
 		else if (warning)
 		{
 			r = (Renderable)tm.getEntity("Warning").getComponent(Renderable.CType);
 			r.sprite.draw(batch);
 		}
 		batch.end();
 	}
 	
 }
