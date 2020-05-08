 package com.battleships.base;
 
 
 import java.util.LinkedList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.battleships.network.Common;
 
 public abstract class Unit extends Actor {
 
 	String team;
 	public static String lastSpawnTeam;
     short Health = 1000;
     Sprite baseSprite;
 	Sprite colorSprite;
 	short MaxHealth = 1000;
 	int moveSpeed = 50;
 	int goldworth = 50;
 	Body CollisionBody;
 	
 	Vector2 DesiredVelocity = new Vector2(0,0);
 	
	Weapon gun = null;
 	Visor visor;
 	protected int VisibleEnemiesCount = 0;
 	protected LinkedList<Unit> VisibleEnemies = new LinkedList<Unit>(); //used by allies
 	Actor icon;
 	protected Vector2 CurrentPosition;
 	static BodyPool bodyPool = new BodyPool();
 	
 	Unit(String Team, float InitialX, float InitialY)
 	{
 		team = Team;
 				
 		baseSprite = new Sprite();
 		baseSprite.setRegion(Resources.BaseTextureRegion[0]);
 		
 		baseSprite.setSize(64, 64);
 		baseSprite.setOrigin(baseSprite.getWidth()/2, baseSprite.getHeight()/2);
 		baseSprite.setPosition(-baseSprite.getWidth()/2, -baseSprite.getHeight()/2);
 		
 		colorSprite = new Sprite();
 		colorSprite.setRegion(Resources.ColorTextureRegion[0]);
 		
 		colorSprite.setSize(64, 64);
 		colorSprite.setOrigin(colorSprite.getWidth()/2, colorSprite.getHeight()/2);
 		colorSprite.setPosition(-colorSprite.getWidth()/2, -colorSprite.getHeight()/2);
 		
 		if(team == "red")
 			colorSprite.setColor(Color.RED);
 		else
 			colorSprite.setColor(Color.BLUE);
 
 		createBody(InitialX,InitialY);
 		GameScreen.gameStage.addActor(this);
 		
 		icon = new Actor(){
 			float scale = GameScreen.miniMap.getWidth()/2048;
 	        public void draw (SpriteBatch batch, float parentAlpha) {
 	    		if(team != GameScreen.LocalPlayerTeam && VisibleEnemiesCount<=0 && !(Unit.this instanceof Tower) || Health <= 0 )
 	    			return;
 	    		
         		batch.setColor(colorSprite.getColor());
 	            batch.draw(Resources.iconTexture,
 	            		GameScreen.miniMap.getX() + GameScreen.miniMap.getWidth()/2 + Unit.this.getX()*scale -2,
 	            		GameScreen.miniMap.getY() + GameScreen.miniMap.getHeight()/2 + Unit.this.getY()*scale -2,4,4);
 	            batch.setColor(Color.WHITE);
 	        }
 		};
 		GameScreen.hudStage.addActor(icon);
 	}
 	
 	public static Unit createNewUnit(UnitData data) {
 		String team = "";
 		if(data.position.y > 0) {
 			team = "red";
 		} else {
 			team = "blue";
 		}
 		Unit unit;
 		switch(data.type) {
 			case Common.PLAYER_SHIP : {
 				unit = new PlayerShip(team, data.position.x*GameScreen.BOX_WORLD_TO, data.position.y*GameScreen.BOX_WORLD_TO, data.slot);
 				break;
 			}
 			case Common.CRUISER : {
 				unit = new Cruiser(team, data.position.x*GameScreen.BOX_WORLD_TO, data.position.y*GameScreen.BOX_WORLD_TO);
 				break;
 			}
 			case Common.TOWER : {
 				unit = new Tower(team, data.position.x*GameScreen.BOX_WORLD_TO, data.position.y*GameScreen.BOX_WORLD_TO);
 				break;
 			}
 			default : {
 				unit = null;
 				break;
 			}
 		}
 		return unit;
 	}
 	
 	void updateUnitData(UnitData data)
 	{
 		CollisionBody.setTransform(data.position, 0);
 		Health = data.health;
 	}
 	
 	void createBody(float initialX, float initialY)
 	{ 
 		CollisionBody = bodyPool.obtain();
     	CollisionBody.setType(BodyType.DynamicBody);
 		CollisionBody.setTransform(initialX*GameScreen.WORLD_TO_BOX,initialY*GameScreen.WORLD_TO_BOX, 0);
 		
 		CircleShape dynamicCircle = new CircleShape();  
         dynamicCircle.setRadius(16f*GameScreen.WORLD_TO_BOX);  
         FixtureDef fixtureDef = new FixtureDef();  
         fixtureDef.shape = dynamicCircle;  
         fixtureDef.density = 1.0f;  
         fixtureDef.friction = 0.0f;  
         fixtureDef.restitution = 0.0f;
         CollisionBody.createFixture(fixtureDef);  
         CollisionBody.getFixtureList().get(0).setUserData(this);
         
         CurrentPosition = new Vector2(0,0);
         this.setPosition(initialX, initialY);
 	}  
 	
 	public void draw (SpriteBatch batch, float parentAlpha) {
 		onUpdate(Gdx.graphics.getDeltaTime());
 		updateVelocity();
 		
 		CurrentPosition.x = this.getX()*GameScreen.WORLD_TO_BOX;
 		CurrentPosition.y = this.getY()*GameScreen.WORLD_TO_BOX;
 		
 		CurrentPosition.lerp(CollisionBody.getPosition(),Gdx.graphics.getDeltaTime()/(GameScreen.BOX_STEP-GameScreen.box_accu+Gdx.graphics.getDeltaTime()));
 		this.setPosition(CurrentPosition.x*GameScreen.BOX_WORLD_TO, CurrentPosition.y*GameScreen.BOX_WORLD_TO);
 		
 		if(team != GameScreen.LocalPlayerTeam && VisibleEnemiesCount<=0)
 			return;
 		
 		float widthScaled = baseSprite.getWidth()*baseSprite.getScaleX();
 		
         batch.draw(baseSprite, getX()-widthScaled/2,getY()-widthScaled/2,widthScaled,widthScaled);
         batch.setColor(colorSprite.getColor());
         batch.draw(colorSprite, getX()-widthScaled/2,getY()-widthScaled/2,widthScaled,widthScaled);
         batch.setColor(Color.WHITE);
         batch.draw(Resources.HealthbarTextureRegion[0], 
         		getX()-widthScaled/4,getY()+widthScaled/4,widthScaled/2,4);
 
         batch.draw(Resources.HealthbarTextureRegion[1], 
         		getX()-widthScaled/4+1,getY()+widthScaled/4,widthScaled/2*((float)Health/(float)MaxHealth),4);
 	}
 	
 	void onUpdate(float delta)
 	{
     	if(Health<=0)
     	{
     		Health=0;
     		Destroy();
     		return;
     	}
 		gun.onUpdate(delta);
 	}
 	
     void TakeDamage(int Damage, Unit Instigator)
     {
     	if(GameScreen.test_mode)
     		Health -= Damage;
     }
 	
 	void Destroy()
 	{
 		GameScreen.hudStage.getRoot().removeActor(icon);
     	this.getParent().removeActor(this);
     	int fixtures = CollisionBody.getFixtureList().size();
 		for(int i=0; i<fixtures; i++)
 		{
 			CollisionBody.destroyFixture(CollisionBody.getFixtureList().get(0));
 		}
 		CollisionBody.setUserData(null);
 		bodyPool.free(CollisionBody);
     	if(team.equals(GameScreen.LocalPlayerTeam))
 		{
 			for(Unit current : VisibleEnemies)
 			{
 				current.VisibleEnemiesCount--;
 			}
 		}
 	}
     
     void setDesiredVelocity(float X, float Y)
     {
     	DesiredVelocity.x = X;
     	DesiredVelocity.y = Y;
     }
     
     void updateVelocity()
     {
     	float velocity = DesiredVelocity.len();
     	if(velocity > 0)
     	{
     		CollisionBody.setLinearVelocity(DesiredVelocity.x*moveSpeed/velocity*GameScreen.WORLD_TO_BOX, DesiredVelocity.y*moveSpeed/velocity*GameScreen.WORLD_TO_BOX);
         	setVisualRotation(DesiredVelocity.x, DesiredVelocity.y);
     	}
     	else
     		CollisionBody.setLinearVelocity(0, 0);
     }
     
     void setVisualRotation(float X, float Y)
     {
     	int Angle = (int) Math.toDegrees(-Math.atan2(X+0.0001f, -Y));
     	if(Angle < 0) 
     		Angle += 360;
 
     		Angle = Angle/10;
     		if(Angle >17)
     		{
     			Angle = 36 - Angle;
     			Angle *= 0.833333f;
     			baseSprite.setRegion(Resources.BaseTextureRegion[Angle]);
     			colorSprite.setRegion(Resources.ColorTextureRegion[Angle]);
     			baseSprite.flip(true, false);
     			colorSprite.flip(true, false);
     		}
     		else
     		{
     			Angle *= 0.833333f;
     			baseSprite.setRegion(Resources.BaseTextureRegion[Angle]);
     			colorSprite.setRegion(Resources.ColorTextureRegion[Angle]);
     			baseSprite.flip(false, false);
     			colorSprite.flip(false, false);
     		}
     } 
 }
