 package com.game.rania.model;
 
 import java.util.Vector;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.FrameBuffer;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.RaniaGame;
 import com.game.rania.model.element.HUDDynamicObject;
 import com.game.rania.model.element.Object;
 import com.game.rania.model.element.RegionID;
 
 public class Radar extends HUDDynamicObject{
 	
 	private Vector<Object> objects = new Vector<Object>();
 	private Vector2 posObject = new Vector2();
 	private Vector2 scaleObject = new Vector2();
 	private Color   colorObject = new Color();
 	private Player  player = null;
 	private float   radarWidth, radarHeight;
 	private TextureRegion sensorRegion, objRegion;
 
 	public Radar(Player player, float x, float y, float width, float height) {
 		super(RegionID.RADAR, x, y);
 		this.player = player;
 		this.radarWidth = width;
 		this.radarHeight = height;
 		sensorRegion = RaniaGame.mView.getTextureRegion(RegionID.RADAR_SENSOR);
 		objRegion = RaniaGame.mView.getTextureRegion(RegionID.RADAR_OBJECT);
 		initFrameBuffer();
 	}
 	
 	public void addObject(Object object){
 		objects.add(object);
 	}
 	
 	public void removeObject(Object object){
 		objects.remove(object);
 	}
 	
 	public void removeObject(int num){
 		objects.remove(num);
 	}
 	
 	private float speedSensor = 150.0f;
 	private float deltaSensor = 0.0f;
 	private float alpha = 0.0f;
 	
 	@Override 
 	public void update(float deltaTime){
 		deltaSensor += deltaTime * speedSensor;
 		float widthRadar = getWidth();
 		if (deltaSensor > widthRadar)
 			deltaSensor -= widthRadar;
 	}
 
 	private FrameBuffer frameBuffer = null;
 	private TextureRegion regionBuffer = null;
 	private SpriteBatch spriteBuffer = null;
 	private int width, height;
 	
 	private void initFrameBuffer(){
 		width = region.getRegionWidth();
 		height = region.getRegionHeight();
 		frameBuffer = new FrameBuffer(Format.RGBA4444, width, height, false);
 		regionBuffer = new TextureRegion(frameBuffer.getColorBufferTexture());
 		regionBuffer.flip(false, true);
 		spriteBuffer = new SpriteBatch();
 		spriteBuffer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, width, height));
 	}
 	
 	@Override
 	public boolean draw(SpriteBatch sprite){
 		if (!visible || player == null || region == null)
 			return false;
 
 		sprite.end();
 
 		frameBuffer.begin();
 		spriteBuffer.begin();
 
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		spriteBuffer.setColor(color);
 		drawRegion(spriteBuffer, region, width * 0.5f, height * 0.5f, angle, 1, 1);
 
 		if (objRegion != null) {
 			for (Object object : objects) {
 				posObject.set(object.position);
 				posObject.sub(player.position);
 				posObject.mul(width / radarWidth, height / radarHeight);
 				posObject.add(width * 0.5f, height * 0.5f);
 	
 				scaleObject.set(object.scale.x, object.scale.y);
 				scaleObject.mul(object.region.getRegionWidth() * region.getRegionWidth() / radarWidth / objRegion.getRegionWidth(),
 								object.region.getRegionHeight() * region.getRegionHeight() / radarHeight / objRegion.getRegionHeight());
 	
 				String objectClass = object.getClass().getSimpleName();
 				if (objectClass.compareTo("User") == 0){
 					colorObject.set(1, 0, 0, 1);
 				}
 				else if (objectClass.compareTo("PlanetSprite") == 0){
 					colorObject.set(1, 0, 1, 1);
 					//draw orbit
 					//PlanetSprite pl = (PlanetSprite)object;
 					//spriteBuffer.setColor(colorObject);
 					//drawRegion(spriteBuffer, orbitRegion, 0, 0, 0, 
 							//pl.planet.orbit * region.getRegionWidth()/ radarWidth / orbitRegion.getRegionWidth(),
 							//pl.planet.orbit * region.getRegionHeight() / radarHeight / orbitRegion.getRegionHeight());
 				}
				else if (objectClass.compareTo("Star") == 0){
					colorObject.set(1, 1, 1, 1);
				}
 				
 				alpha = (deltaSensor - posObject.x) / width;
 				if (alpha < 0)
 					alpha += 1.0f;
 				colorObject.a = 1.0f - 0.5f * alpha;
 				
 				spriteBuffer.setColor(colorObject);
 				drawRegion(spriteBuffer, objRegion, posObject, object.angle, scaleObject);
 			}
 		}
 		
 		if (sensorRegion != null) {
 			spriteBuffer.setColor(color);
 			drawRegion(spriteBuffer, sensorRegion, deltaSensor, height * 0.5f, angle, 1, height * 0.98f / sensorRegion.getRegionHeight());
 		}
 
 		spriteBuffer.end();
 		frameBuffer.end();
 		
 		sprite.begin();
 		drawRegion(sprite, regionBuffer, position.x, position.y, 0, scale.x, scale.y);
 		
 		return true;
 	}
 	
 }
