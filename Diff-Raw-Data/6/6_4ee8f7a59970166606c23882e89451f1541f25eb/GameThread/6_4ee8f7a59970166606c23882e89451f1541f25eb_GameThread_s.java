 package server;
 
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 
import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 class GameThread extends Thread{
 	private ServerGame game;
 	private volatile boolean running;
 	
 	World physicsWorld;
 	private ContactListener contactListener;
 	
 	static final float BOX_STEP=1f/10f;
     static final int BOX_VELOCITY_ITERATIONS=6;  
     static final int BOX_POSITION_ITERATIONS=2;  
     static final float WORLD_TO_BOX=0.01f;  
     static final float BOX_WORLD_TO=100.0f; 
     
 	static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
 	
 	public GameThread(ServerGame game){
 		this.game = game;
 		this.running = true;
 		this.setName(game.getName());
 		
 		physicsWorld = new World(new Vector2(0, 0), true);
 		contactListener = createContactListener();
 		physicsWorld.setContactListener(contactListener);
 		
 		BodyDef bodyDef = new BodyDef();
         bodyDef.type = BodyType.StaticBody; 
         bodyDef.position.set(0,0);  
 		Body WorldCollisionBody = physicsWorld.createBody(bodyDef);
 		
		TiledMap map = TiledLoader.createMap(Gdx.files.internal("data/BattleShipsCollision.tmx"));
 		TiledObjectGroup objectGroup = map.objectGroups.get(0);
 		for (TiledObject current : objectGroup.objects) {
 			
 			PolygonShape staticRectangle = new PolygonShape();
 			staticRectangle.setAsBox(current.width/2*WORLD_TO_BOX, current.height/2*WORLD_TO_BOX,
 					new Vector2((current.x-1024 +current.width/2)*WORLD_TO_BOX,
 							(-current.y+1024 -current.height/2)*WORLD_TO_BOX), 0);
 	        FixtureDef fixtureDef = new FixtureDef();  
 	        fixtureDef.shape = staticRectangle;  
 	        fixtureDef.density = 1.0f;  
 	        fixtureDef.friction = 0.0f;  
 	        fixtureDef.restitution = 0.0f;
 	        WorldCollisionBody.createFixture(fixtureDef);  
 	    }
 		
 		objectGroup = map.objectGroups.get(1);
 		for (TiledObject current : objectGroup.objects) {
 			
 			if(current.y>1024)
 				new Tower("blue",current.x-1024+16,-current.y+1024, this);
 			else
 				new Tower("red",current.x-1024+16,-current.y+1024, this);
 	    }
 	}
 	
 	public void run(){
 		System.out.println("game " + game.getName() + " started");
 		while(running){
 			try {
 				physicsWorld.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS); 
 				
 				// TODO Fixed step accumulator needed
 				
 				while(physicsWorld.getBodies().hasNext())
 				{
 					if(((Unit)physicsWorld.getBodies().next().getUserData())!=null)
 						((Unit)physicsWorld.getBodies().next().getUserData()).onUpdate(100);	
 				}
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		System.out.println("game " + game.getName() + " stopped");
 	}
 	
 	public void stopGame(){
 		this.running = false;
 	}
 	
 	   private ContactListener createContactListener()
 	    {
 	        return new ContactListener()
 	        {
 	            @Override
 	            public void beginContact(Contact contact)
 	            {
 	                final Fixture x1 = contact.getFixtureA();
 	                final Fixture x2 = contact.getFixtureB();
 	                if(x2.getUserData() instanceof Unit && x1.getUserData() instanceof Weapon)
 	                {
 	                	if(!((Weapon)x1.getUserData()).Owner.team.equals(((Unit)(x2.getUserData())).team))
 	                		((Weapon)x1.getUserData()).Enemies.add(((Unit)(x2.getUserData())));
 	                }
 	                if(x1.getUserData() instanceof Unit && x2.getUserData() instanceof Weapon)
 	                {
 	                	if(!((Weapon)x2.getUserData()).Owner.team.equals(((Unit)(x1.getUserData())).team))
 	                		((Weapon)x2.getUserData()).Enemies.add(((Unit)(x1.getUserData())));
 	                }        
 	            }
 	 
 	            @Override
 	            public void endContact(Contact contact)
 	            {
 	                final Fixture x1 = contact.getFixtureA();
 	                final Fixture x2 = contact.getFixtureB();
 	                
 	                if(x2.getUserData() instanceof Unit && x1.getUserData() instanceof Weapon)
 	                {
 	                	if(!((Weapon)x1.getUserData()).Owner.team.equals(((Unit)(x2.getUserData())).team))
 	                		((Weapon)x1.getUserData()).Enemies.remove(((Unit)(x2.getUserData())));
 	                }
 	                if(x1.getUserData() instanceof Unit && x2.getUserData() instanceof Weapon)
 	                {
 	                	if(!((Weapon)x2.getUserData()).Owner.team.equals(((Unit)(x1.getUserData())).team))
 	                		((Weapon)x2.getUserData()).Enemies.remove(((Unit)(x1.getUserData())));
 	                }  
 	            }  
 	 
 	            @Override
 	            public void preSolve(Contact contact, Manifold oldManifold)
 	            {
 	                   
 	            }
 	 
 	            @Override
 	            public void postSolve(Contact contact, ContactImpulse impulse)
 	            {
 	                   
 	            }
 	        };
 	    } 
 	
 }
