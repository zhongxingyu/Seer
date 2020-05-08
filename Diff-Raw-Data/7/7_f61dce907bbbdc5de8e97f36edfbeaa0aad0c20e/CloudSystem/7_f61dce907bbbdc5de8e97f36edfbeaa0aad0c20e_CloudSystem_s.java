 package io.github.ldears.ld26.render;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.utils.Array;
 
 import java.util.Random;
 
 /**
  * @author dector
  */
 public class CloudSystem {
 
 	private static final int MAX_CLOUDS = 5;
 	private static final int MIN_CLOUD_Y = Renderer.SCREEN_HEIGHT / 7;
	private static final int CLOUD_ROAD_HEIGHT = (Renderer.SCREEN_HEIGHT - MIN_CLOUD_Y) / MAX_CLOUDS;
 	private static final int MIN_VELOCITY = 1 * Renderer.TILE_SIZE;
 	private static final int MAX_VELOCITY = 4 * Renderer.TILE_SIZE;
 
 	private ResLoader resLoader;
 	private Array<Integer> idsArray;
 	private Cloud[] clouds;
 
 	private int cloudsCount;
 
 	private int worldWidth;
 	private int worldHeight;
 
 	private Random rnd;
 
 	public CloudSystem(ResLoader resLoader, int worldWidth, int worldHeight) {
 		this.resLoader = resLoader;
 		this.worldWidth = worldWidth;
 		this.worldHeight = worldHeight;
 
 		clouds = new Cloud[MAX_CLOUDS];
 
 		idsArray = new Array<Integer>(MAX_CLOUDS);
 		for (int i = 0; i < MAX_CLOUDS; i++) {
 			idsArray.add(i);
 		}
 
 		rnd = new Random();
 	}
 
 	public void update(float dt) {
 		if (cloudsCount < MAX_CLOUDS) {
 			// Create Cloud
 			Array<TextureAtlas.AtlasRegion> textures = resLoader.cloudsTexture;
 
 			int newId = idsArray.removeIndex(rnd.nextInt(idsArray.size));
 
 			Cloud c = new Cloud(newId,
 					rnd.nextInt(textures.size),
 					worldWidth,
 					rnd.nextInt(Math.abs(CLOUD_ROAD_HEIGHT - 64)) + MIN_CLOUD_Y + newId * CLOUD_ROAD_HEIGHT,
 					rnd.nextInt(MAX_VELOCITY - MIN_VELOCITY) + MIN_VELOCITY);
 
 			c.width = textures.get(c.textureIndex).getRegionWidth();
 
 			clouds[c.id] = c;
 			cloudsCount++;
 		}
 
 		// Update clouds
 		for (Cloud c : clouds) {
 			if (c != null) {
 				c.x -= c.velocity * dt;
 
 				// Out of range
 				if (c.x + c.width < 0) {
 					idsArray.add(c.id);
 
 					clouds[c.id] = null;
 					cloudsCount--;
 				}
 			}
 		}
 	}
 
 	public void render(SpriteBatch batch) {
 		for (Cloud cloud : clouds) {
 			if (cloud != null) {
 				TextureAtlas.AtlasRegion reg = resLoader.cloudsTexture.get(cloud.textureIndex);
 				batch.draw(reg, cloud.x, cloud.y);
 			}
 		}
 	}
 
 	private static class Cloud {
 		final int textureIndex;
 		final int velocity;
 
 		float x;
 		final float y;
 
 		int width;
 
 		final int id;
 
 		private Cloud(int id, int textureIndex, int x, int y, int velocity) {
 			this.id = id;
 			this.textureIndex = textureIndex;
 			this.x = x;
 			this.y = y;
 			this.velocity = velocity;
 		}
 	}
 }
