 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.view;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.vecmath.Vector2d;
 
 import org.andengine.opengl.texture.TextureManager;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 import ultraextreme.model.entity.IEntity;
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.model.util.ObjectName;
 
 /**
  * Class in charge of creating new GameObjectSprites
  * 
  * @author Johan Gronvall
  * 
  */
 public class SpriteFactory {
 
 	private Map<ObjectName, ITextureRegion> textureMap = new HashMap<ObjectName, ITextureRegion>();
 
 	private Map<ObjectName, Vector2d> offsetMap = new HashMap<ObjectName, Vector2d>();
 
 	/**
 	 * The items' textures.
 	 */
 	private Map<ObjectName, ITextureRegion> itemTextures = new HashMap<ObjectName, ITextureRegion>();
 
 	// TODO PMD: Perhaps 'screenDimension' could be replaced by a local
 	// variable.
 	private Dimension screenDimension; // TODO implement scaling in this class?
 	// TODO PMD: Avoid unused private fields such as 'MODEL_DIMENSION'.
 	private static final Dimension MODEL_DIMENSION = Constants
 			.getLevelDimension();
 
 	/**
 	 * The item bar's texture.
 	 */
 	private ITextureRegion itemBarTexture;
 
 	private static SpriteFactory instance;
 
 	/**
 	 * Creates a spriteFactory OBS: should be called during a loadResources
 	 * because this constructor might get heavy
 	 */
 	private SpriteFactory(final SimpleBaseGameActivity activity) {
 		screenDimension = new Dimension(activity.getResources()
 				.getDisplayMetrics().widthPixels, activity.getResources()
 				.getDisplayMetrics().heightPixels);
 		GameObjectSprite.setScreenDimension(screenDimension);
 		textureMap = new HashMap<ObjectName, ITextureRegion>();
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		final TextureManager textureManager = activity.getTextureManager();
 		BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(
 				textureManager, 1024, 1024,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		// init enemies bullets and the player
 		final TextureRegion playerShip = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity,
 						"ship_blue_42px.png", 0, 0);
 		putProperties(ObjectName.PLAYERSHIP, playerShip, new Vector2d(16.5, 13));
 
 		final TextureRegion playerBullet = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity,
 						"laserGreen.png", 33, 0);
 		putProperties(ObjectName.BASIC_BULLET, playerBullet, new Vector2d(4.5, 16.5));
 
 		final TextureRegion basicEnemy = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity,
 						"evil_ship.png", 0, 43);
 		putProperties(ObjectName.BASIC_ENEMYSHIP, basicEnemy, new Vector2d(27, 40));
 
 		// init pickupables
 		final TextureRegion basicWeapon = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity,
 						"cannon.png", 56, 51);
 		putProperties(ObjectName.BASIC_WEAPON, basicWeapon, new Vector2d(15, 15));
 		
 		final TextureRegion spinningSpreadWeapon = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity,
 						"spin.png", 87, 51);
 		putProperties(ObjectName.SPINNING_SPREAD_WEAPON, spinningSpreadWeapon, new Vector2d(15, 15));
 
 		// Init the item bar texture
 		itemBarTexture = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity, "itembar.png", 80, 0);
 
 		// Init the item textures for items in the itembar
 		itemTextures.put(ObjectName.BASIC_WEAPON, basicWeapon); // Test only
 		itemTextures.put(ObjectName.SPINNING_SPREAD_WEAPON, spinningSpreadWeapon); // Test only
		itemTextures.put(ObjectName.BASIC_SPREAD_WEAPON, spinningSpreadWeapon); // Test only
 
 		// What is this for?(I think it needs to be called to init the atlas, we
 		// will never know.. gramlich 2012)
 		textureManager.loadTexture(textureAtlas);
 	}
 
 	private void putProperties(ObjectName objectName,
 			TextureRegion texture, Vector2d textureOffset) {
 		textureMap.put(objectName, texture);
 		offsetMap.put(objectName, textureOffset);
 	}
 
 	public static void initialize(SimpleBaseGameActivity activity) {
 		instance = new SpriteFactory(activity);
 	}
 
 	public static SpriteFactory getInstance() {
 		if (instance == null) {
 			throw new IllegalStateException(
 					"SpriteFactory must have been initialized before it is used");
 		}
 		return instance;
 	}
 
 	/**
 	 * Creates and returns a sprite of the specified type
 	 * 
 	 * @param entity
 	 *            The entity which this sprite is to follow
 	 * @param vbom
 	 *            the VertexBufferOBjectManager
 	 * @param objectName
 	 *            what kind of sprite (picture) is desired
 	 * @return a new GameOBjectSprite
 	 */
 	public GameObjectSprite getNewSprite(final IEntity entity,
 			final VertexBufferObjectManager vbom) {
 		ObjectName objName = entity.getObjectName();
 		return new GameObjectSprite(entity, vbom, textureMap.get(objName), offsetMap.get(objName));
 	}
 
 	/**
 	 * 
 	 * @return The texture of the item bar.
 	 */
 	public ITextureRegion getItemBarTexture() {
 		return itemBarTexture;
 	}
 
 	/**
 	 * 
 	 * @param item
 	 *            The item you want an image of.
 	 * @return An texture of an item that you want to show in the item bar.
 	 */
 	public ITextureRegion getItemTexture(ObjectName item) {
 		return itemTextures.get(item);
 	}
 }
