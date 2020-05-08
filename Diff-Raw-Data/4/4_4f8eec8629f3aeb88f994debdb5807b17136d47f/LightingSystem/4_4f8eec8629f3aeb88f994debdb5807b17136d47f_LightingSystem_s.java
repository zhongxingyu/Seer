 package com.turbonips.troglodytes.systems;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.utils.ImmutableBag;
 import com.turbonips.troglodytes.ResourceManager;
 import com.turbonips.troglodytes.components.Resource;
 import com.turbonips.troglodytes.components.Sliding;
 
 public class LightingSystem extends BaseEntitySystem {
 	private Image light;
 	private Graphics graphics;
 	private GameContainer container;
 	private ComponentMapper<Sliding> slidingMapper;
 	private ComponentMapper<Resource> resourceMapper;
 
 	public LightingSystem(GameContainer container) {
 		// TODO For map based lights we could also use Transform.class
 		this.container = container;
 		graphics = container.getGraphics();
 		ResourceManager resourceManager = ResourceManager.getInstance();
 		light = (Image)resourceManager.getResource("light").getObject();
 	}
 	
 	@Override
 	protected void initialize() {
 		slidingMapper = new ComponentMapper<Sliding>(Sliding.class, world);
 		resourceMapper = new ComponentMapper<Resource>(Resource.class, world);
 	}
 
 	@Override
 	protected void processEntities(ImmutableBag<Entity> entities) {
 		ImmutableBag<Entity> creatures = world.getGroupManager().getEntities("PLAYER");
 		ImmutableBag<Entity> layers = world.getGroupManager().getEntities("LAYER");
 		
 		if (!layers.isEmpty()) {
 			Resource resource = resourceMapper.get(layers.get(0));
 			if (resource != null) {
 				TiledMap tiledMap = (TiledMap)resourceMapper.get(layers.get(0)).getObject();
 				boolean isDark = Boolean.parseBoolean(tiledMap.getMapProperty("Dark", "false"));
 				if (isDark) {
 					for (int i=0; i<creatures.size(); i++) {
 						Entity entity = creatures.get(i);
 						Sliding sliding = slidingMapper.get(entity);
 						int lightSize = 15;
 						float invSize = 1f / lightSize;
 						graphics.clearAlphaMap();
 						//graphics.setColor(new Color(0,0,0,100));
 						//graphics.fillRect(0, 0, container.getWidth(), container.getHeight());
 						graphics.scale(lightSize, lightSize);
 						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
						light.drawCentered((container.getWidth()/2 - sliding.getX() + 16) * invSize, (container.getHeight()/2 - sliding.getY() + 16) * invSize);
 						graphics.scale(invSize, invSize);
 						GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_DST_ALPHA);
 						graphics.setColor(new Color(0,0,0,255));
 						graphics.fillRect(0, 0, container.getWidth(), container.getHeight());
 						graphics.setDrawMode(Graphics.MODE_NORMAL);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	protected boolean checkProcessing() {
 		return true;
 	}
 
 }
