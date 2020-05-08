 package ui.isometric;
 
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import serialization.Serializer;
 import serialization.Tree;
 import serialization.util.Serializers;
 import util.Direction;
 import util.Resources;
 
 import client.model.GameThing;
 import client.model.LevelLocation;
 import client.model.Location;
 import data.Database;
 
 /**
  * 
  * A class for getting the correct information needed for an IsoCanvas to display from data provided by a GamModel
  * 
  * @author melby
  *
  */
 public class IsoRendererLibrary {
 	public static final String RENDERER_ISOMETRIC = "renderer.isometric";
 	public static final String RENDERER_ISOMETRIC_LEVEL = "level";
 	
 	private static Map<String, Map<Direction, BufferedImage>> renderers = null;
 	
 	private static class ImageType {
 		private Type type;
 		private String imageName;
 		
 		private static final String NAME = "name";
 		private static final String TYPE = "type";
 		
 		protected enum Type {
 			IMAGE1,
 			IMAGE2,
 			IMAGE4;
 		}
 		
 		protected static class Serializer implements serialization.Serializer<ImageType> {
 			@Override
 			public ImageType read(Tree in) {
				serialization.Serializer<Map<String, String>> deserializer = new Serializers.Map<String, String>(Serializers.string, Serializers.string);
 				Map<String, String> store = deserializer.read(in);
 				
 				return new ImageType(store.get(NAME), Type.valueOf(store.get(TYPE)));
 			}
 
 			@Override
 			public Tree write(ImageType in) {
 				HashMap<String, String> store = new HashMap<String, String>();
 				store.put(NAME, in.imageName);
 				store.put(TYPE, in.type.name());
 				
				serialization.Serializer<Map<String, String>> serializer = new Serializers.Map<String, String>(Serializers.string, Serializers.string);
 				
 				return serializer.write(store);
 			}
 		}
 		
 		public ImageType(String image, Type type) {
 			this.imageName = image;
 			this.type = type;
 		}
 		
 		public Map<Direction, BufferedImage> load() {
 			switch(type) {
 				case IMAGE1:
 					return loadImage1(imageName);
 				case IMAGE2:
 					return loadImage2(imageName);
 				case IMAGE4:
 					return loadImage4(imageName);
 				default:
 					throw new RuntimeException("Unknown ImageType.Type encountered: " + type);
 			}
 		}
 	}
 
 	/**
 	 * Get the renderers, if null create them
 	 * @return
 	 */
 	private static Map<String, Map<Direction, BufferedImage>> renderers() {
 		synchronized(IsoRendererLibrary.class) {
 			if(renderers == null) {
 				loadImages();
 			}
 		}
 		
 		return renderers;
 	}
 
 	/**
 	 * Load all the images from disk into our internal data structures
 	 */
 	private static void loadImages() {
 		synchronized(IsoRendererLibrary.class) {
 			if(renderers == null) {
 				renderers = new HashMap<String, Map<Direction, BufferedImage>>();
 				
 				
				Serializer<Map<String, ImageType>> deserializer = new Serializers.Map<String, ImageType>(Serializers.string, new ImageType.Serializer());
 				Map<String, ImageType> types = deserializer.read(Database.xmlToTree(Resources.loadTextResource("/resources/isotiles/resources.xml")));
 				
 				for(String key : types.keySet()) {
 					renderers.put(key, types.get(key).load());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Create the data structure needed for an image by having the same image for all 4 directions.
 	 * Note, assumes images are png and adds the extension automatically
 	 * @param resourceName
 	 * @return
 	 */
 	private static Map<Direction, BufferedImage> loadImage1(String resourceName) {
 		Map<Direction, BufferedImage> map = new HashMap<Direction, BufferedImage>();
 		
 		BufferedImage image = Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+".png");
 		map.put(Direction.NORTH, image);
 		map.put(Direction.EAST, image);
 		map.put(Direction.WEST, image);
 		map.put(Direction.SOUTH, image);
 		
 		return map;
 	}
 	
 	/**
 	 * Create the data structure for an image by loading the images suffixed _n _e _s _w into the appropriate places
 	 * Note, assumes images are png and adds the extension automatically
 	 * @param resourceName
 	 * @return
 	 */
 	private static Map<Direction, BufferedImage> loadImage4(String resourceName) {
 		Map<Direction, BufferedImage> map = new HashMap<Direction, BufferedImage>();
 		
 		map.put(Direction.NORTH, Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_n.png"));
 		map.put(Direction.EAST, Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_e.png"));
 		map.put(Direction.WEST, Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_w.png"));
 		map.put(Direction.SOUTH, Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_s.png"));
 		
 		return map;
 	}
 	
 	/**
 	 * Create the data structure for an image by loading the images suffixed _ns _ew into the appropriate places
 	 * Note, assumes images are png and adds the extension automatically
 	 * @param resourceName
 	 * @return
 	 */
 	private static Map<Direction, BufferedImage> loadImage2(String resourceName) {
 		Map<Direction, BufferedImage> map = new HashMap<Direction, BufferedImage>();
 		
 		BufferedImage ns = Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_ns.png");
 		BufferedImage ew = Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_ew.png");
 		
 		map.put(Direction.NORTH, ns);
 		map.put(Direction.EAST, ew);
 		map.put(Direction.WEST, ns);
 		map.put(Direction.SOUTH, ew);
 		
 		return map;
 	}
 	
 	/**
 	 * Get the appropriate image for a given renderer name
 	 * @param renderer
 	 * @param viewDirection
 	 * @return
 	 */
 	public static BufferedImage imageForRendererName(String rendererName, Direction viewDirection) {
 		Map<Direction, BufferedImage> renderer = renderers().get(rendererName);
 		if(renderer != null) {
 			return renderer.get(viewDirection);
 		}
 		else {
 			return null;
 		}
 	}
 
 	/**
 	 * Get the level a IsoImage should be displayed at from the user arguments stored by a GameModel
 	 * @param userArguments
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static int levelFromArguments(Map<String, Object> userArguments) {				
 		Object tmp = userArguments.get(RENDERER_ISOMETRIC);;
 		
 		if(tmp != null && tmp instanceof Map) {
 			Map<String, Object> iso = (Map<String, Object>)tmp;
 			tmp = iso.get(RENDERER_ISOMETRIC_LEVEL);
 			
 			if(tmp instanceof Number) {
 				Number i = (Number)tmp;
 				return i.intValue();
 			}
 		}
 		
 		return IsoSquare.FLOOR;
 	}
 
 	/**
 	 * Get a new IsoImage representing the given GameThing on a given square
 	 * @param square
 	 * @param thing
 	 * @param viewDirection
 	 * @return
 	 */
 	public static IsoImage newImageFromGameThing(IsoSquare square, GameThing thing, Direction viewDirection) {
 		IsoImage tmp = null;
 		
 		Location l = thing.location();
 		if(l instanceof LevelLocation) {
 			tmp = new IsoImage(imageForRendererName(thing.renderer(), ((LevelLocation)l).direction().compose(viewDirection)), square);
 			tmp.setGameThing(thing);
 		}
 		
 		return tmp;
 	}
 
 	/**
 	 * Get all the renderers supported
 	 * @return
 	 */
 	public static Set<String> allRendererNames() {
 		return renderers().keySet();
 	}
 }
