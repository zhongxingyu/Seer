 package com.secondhand.view.resource;
 
 import java.util.EnumMap;
 import java.util.Map;
 
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 
 import com.secondhand.model.resource.PlanetType;
 import com.secondhand.model.resource.PowerUpType;
 import com.secondhand.view.loader.TextureRegionLoader;
 
 
 public class TextureRegions {
 
 	private static final String TEXTURE_BASEPATH = "gfx/";
 	
 
 	public TextureRegion playerSprite;
 	public TextureRegion enemySprite;
 	
 	public TextureRegion obstacleTexture;
 	public Map<PlanetType, TextureRegion> planetTextures;
 	
 	public Map<PowerUpType, TextureRegion> powerUpTextures;
 
 	public TextureRegion starsTexture;
 	
 	private static TextureRegions instance;
 	
 	public static TextureRegions getInstance() {
 		if(instance == null) {
 			instance = new TextureRegions();
 		}
 		return instance;
 	}
 	
 	public TextureRegion getPlanetTexture(final PlanetType planetType) {
 		return planetTextures.get(planetType);
 	}
 	
 	public TextureRegion getPowerUpTexture(final PowerUpType powerUpType) {
 		return powerUpTextures.get(powerUpType);
 	}
 	
 	public void load() {	
 		planetTextures = new EnumMap<PlanetType, TextureRegion>(PlanetType.class);
 		
 		for (final PlanetType planetType : PlanetType.values()) {
 			final TextureRegion planetTexture = TextureRegionLoader.getInstance().loadTextureRegion(TEXTURE_BASEPATH+planetType.getPath(), 
 					planetType.getWidth(), planetType.getHeight(),
 					TextureOptions.REPEATING_BILINEAR);
 			planetTextures.put(planetType, planetTexture);
 		}
 		
 		
 		powerUpTextures = new EnumMap<PowerUpType, TextureRegion>(PowerUpType.class);
 		
 		for (final PowerUpType powerUpType : PowerUpType.values()) {
 			final TextureRegion planetTexture = TextureRegionLoader.getInstance().loadTextureRegion(TEXTURE_BASEPATH+powerUpType.getPath(), 64, 64,
 					TextureOptions.REPEATING_BILINEAR);
 			powerUpTextures.put(powerUpType, planetTexture);
 		}
 		
 		
 		this.obstacleTexture = TextureRegionLoader.getInstance().loadTextureRegion(TEXTURE_BASEPATH+"obstacle.png", 256, 256,
 				TextureOptions.REPEATING_BILINEAR);
 	
 		this.starsTexture = TextureRegionLoader.getInstance().loadTextureRegion(TEXTURE_BASEPATH+"stars.png", 256, 256,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		this.playerSprite = TextureRegionLoader.getInstance().loadTextureRegion(TEXTURE_BASEPATH+"player.png", 256, 256,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		this.enemySprite = TextureRegionLoader.getInstance().loadTextureRegion(TEXTURE_BASEPATH+"enemy.png", 256, 256,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		
 	}	
 }
