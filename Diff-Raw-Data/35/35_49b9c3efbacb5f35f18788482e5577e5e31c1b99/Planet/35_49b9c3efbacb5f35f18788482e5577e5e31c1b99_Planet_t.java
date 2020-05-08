 package com.game.rania.model;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.RaniaGame;
 import com.game.rania.controller.Controllers;
 import com.game.rania.model.element.Object;
 import com.game.rania.model.element.RegionID;
 
 public class Planet extends Object{
 
 	public int id				= -1;
 	public int type      		= -1;
	public float speed			=  0;
 	public int orbit			=  0;
 	public int radius			=  0;
 	public int atmosphere		= -1;
	public int domain			= -1;
 	public String name    	    = "";
 	public int  idLocation   	= -1;
 	public Star star 			= null;
 	private Texture cloudTexture = null;

	private static final float radianSec = MathUtils.degreesToRadians / 3600.0f; 
 	private float time = 0.0f;
 	private float dt = 0.0f;
 	private final float cloudSpeedX = 0.005f;
 	private final float cloudSpeedY = 0.002f;
	private Vector2 cloudSpeed = new Vector2();
 
 	public Planet(int id, String name, int type, int radius, int atmosphere, int speed, int orbit, int idLocation, int Domain) {
 		super(RegionID.fromInt(RegionID.PLANET_0.ordinal() + type), 0, 0);
 		cloudTexture = RaniaGame.mView.getTexture(RegionID.CLOUDS);
 		this.id = id;
 		this.name = name;
 		this.type = type;
 		this.radius = radius;
 		this.atmosphere = atmosphere;
		this.speed = speed * radianSec / 100.0f;
 		this.orbit = orbit;
 		this.idLocation = idLocation;
 		this.domain = Domain;
 		this.star = Controllers.locController.getLocation(idLocation).star;
 		zIndex = Indexes.planets;
 		updatePosition();
 		shader = Controllers.shaderManager.getShader("data/shaders/main.vert", "data/shaders/planet.frag");
 	}
 	
 	public void updatePosition(){
		int serverTime = Controllers.netController.getServerTime();
		time = serverTime % (2.0f * MathUtils.PI / speed);
 		calcPosition(time);
 		if (region != null)
 			scale.set(2.0f * radius / region.getRegionWidth(), 2.0f * radius / region.getRegionHeight());
 	}
 	@Override
 	public void update(float deltaTime){
 		dt += deltaTime;
		cloudSpeed.set(cloudSpeedX * dt, cloudSpeedY * dt);
 		calcPosition(time + dt);
 	}
 	
 	@Override
 	public boolean draw(SpriteBatch sprite){
 		if (star == null)
 			return false;
 		if (currentShader == shader && shader != null){
 			shader.setUniformf("uvMin", new Vector2(region.getU(), region.getV()));
 			shader.setUniformf("uvMax", new Vector2(region.getU2(), region.getV2()));
 		}
 		boolean ret =  super.draw(sprite);
 		sprite.flush();
 		return ret;
 	}
 
 	@Override
 	public boolean setShader(SpriteBatch sprite){
 		if (!super.setShader(sprite))
 			return false;
 
 		cloudTexture.bind(1);
 		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
 		shader.setUniformi("u_texture2", 1);
		shader.setUniformf("v_speed", cloudSpeed);
 
 		return true;
 	}
 
 	private void calcPosition(float currentTime) {
 		if (star == null)
 			return;
		float calcAngle = speed * currentTime;
		position.set((float)Math.cos(calcAngle), 
					 (float)Math.sin(calcAngle));
 		position.mul(orbit);
 		position.add(star.position);
		angle = MathUtils.radiansToDegrees * calcAngle + 45.0f;
 	}
 }
