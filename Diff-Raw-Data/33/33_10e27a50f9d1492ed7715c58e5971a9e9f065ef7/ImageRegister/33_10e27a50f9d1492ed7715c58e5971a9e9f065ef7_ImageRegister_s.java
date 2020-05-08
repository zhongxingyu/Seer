 package spaceshooters.gfx;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.imageout.ImageIOWriter;
 
 import spaceshooters.main.Spaceshooters;
 import spaceshooters.util.ErrorFrame;
 import spaceshooters.util.Logger;
 import spaceshooters.util.config.Configuration;
 import spaceshooters.util.config.Options;
 
 public class ImageRegister {
 	
 	private static final int TEXTURE_SIZE = 45;
 	public static final String ENTITIES = getGraphicsFolder() + "entities/";
 	public static final String EFFECTS = getGraphicsFolder() + "effects/";
 	
 	private static HashMap<String, Integer[]> entityImageMap;
 	private static HashMap<String, Integer[]> effectImageMap;
 	private static Image stitchedEntities;
 	private static Image stitchedEffects;
 	private static boolean inited = false;
 	
 	public static void registerImages() {
 		registerImages(false);
 	}
 	
 	public static void registerImages(boolean debug) {
 		try {
 			if (!inited) {
 				// Stitch entities.
 				int x = 0, y = 0;
 				int entityWidth = TEXTURE_SIZE * 6;
 				int entityHeight = TEXTURE_SIZE;
 				stitchedEntities = Image.createOffscreenImage(entityWidth, entityHeight);
 				entityImageMap = new HashMap<>();
 				
 				for (File f : new File(ENTITIES).listFiles()) {
 					Graphics entityG = stitchedEntities.getGraphics();
 					Image i = new Image(f.getAbsolutePath());
 					entityG.drawImage(i, x, y);
 					entityG.flush();
 					entityImageMap.put(f.getName(), new Integer[] { x, y });
 					
 					if (debug) {
 						Logger.info("Drawn " + f.getName() + " at " + x + ", " + y);
 					}
 					
 					x += x >= entityWidth ? 0 : TEXTURE_SIZE;
 					if (x >= entityWidth) {
 						y += TEXTURE_SIZE;
 						x = 0;
 					}
 					
 					if (y >= entityHeight) {
 						if (debug) {
 							Logger.info("Should resize...");
 						}
 						
 						entityHeight += TEXTURE_SIZE;
 						stitchedEntities = resizeImage(stitchedEntities, entityWidth, entityHeight);
 						
 						if (debug) {
 							Logger.info("Image stitched_entities.png has been succesfully resized!");
 						}
 					}
 				}
 				
 				// If debug mode then save the image.
 				if (debug) {
 					if (stitchedEntities != null) {
 						new ImageIOWriter().saveImage(stitchedEntities, "PNG", new FileOutputStream(Spaceshooters.getPath() + "stitched_entities.png"), true);
 					}
 				}
 				
 				// Stitch effects.
 				int x1 = 0, y1 = 0;
 				int effectWidth = TEXTURE_SIZE * 6;
 				int effectHeight = TEXTURE_SIZE;
 				stitchedEffects = Image.createOffscreenImage(effectWidth, effectHeight);
 				effectImageMap = new HashMap<>();
 				
 				for (File f : new File(EFFECTS).listFiles()) {
 					Graphics effectG = stitchedEffects.getGraphics();
 					Image i = new Image(f.getAbsolutePath());
 					effectG.drawImage(i, x1, y1);
 					effectG.flush();
 					effectImageMap.put(f.getName(), new Integer[] { x1, y1 });
 					
 					x1 += x1 >= effectWidth ? 0 : TEXTURE_SIZE;
 					if (x1 >= effectWidth) {
 						y1 += TEXTURE_SIZE;
 						x1 = 0;
 					}
 					
 					if (y1 >= effectHeight) {
 						if (debug) {
 							Logger.info("Should resize...");
 						}
 						
 						effectHeight += TEXTURE_SIZE;
 						stitchedEffects = resizeImage(stitchedEffects, effectWidth, effectHeight);
 						
 						if (debug) {
 							Logger.info("Image stitched_effects.png has been succesfully resized!");
 						}
 					}
 					
 				}
 				
 				// If debug mode then save the image.
 				if (debug) {
 					if (stitchedEffects != null) {
 						new ImageIOWriter().saveImage(stitchedEffects, "PNG", new FileOutputStream(Spaceshooters.getPath() + "stitched_effects.png"), true);
 					}
 				}
 				
 				inited = true;
 			} else {
 				Logger.error("Textures have been already stitched!");
 			}
 		} catch (SlickException | IOException e) {
 			new ErrorFrame("Unable to stitch the textures!", true);
 		}
 	}
 	
 	/**
 	 * Gets an entity image from the stitched image.
 	 * 
 	 * @param imageName
 	 *            The name of the image to get.
 	 * @return The texture of the entity.
 	 */
 	public static Image getEntity(String imageName) {
 		Integer[] coords = entityImageMap.get(imageName);
 		return stitchedEntities.getSubImage(coords[0], coords[1], 45, 45);
 	}
 	
 	/**
 	 * Gets an effect image from the stitched image.
 	 * 
 	 * @param imageName
 	 *            The name of the image to get.
 	 * @return The texture of the effect.
 	 */
 	public static Image getEffect(String imageName) {
 		Integer[] coords = effectImageMap.get(imageName);
 		return stitchedEffects.getSubImage(coords[0], coords[1], 45, 45);
 	}
 	
 	/**
 	 * Useful method for resizing an image.
 	 * 
 	 * @param image
 	 *            The image to resize.
 	 * @param width
 	 *            The new width.
 	 * @param height
 	 *            the new height.
 	 * @return Resized image.
 	 * @throws SlickException
 	 */
 	public static Image resizeImage(Image image, int width, int height) throws SlickException {
 		Image img = Image.createOffscreenImage(width, height);
 		Graphics graphics = img.getGraphics();
 		graphics.drawImage(image, 0, 0);
 		graphics.flush();
 		return img;
 	}
 	
 	private static String getGraphicsFolder() {
 		return Configuration.getConfiguration().getBoolean(Options.OLD_GRAPHICS) ? "res/gfx/org/" : "res/gfx/new/";
 	}
 }
