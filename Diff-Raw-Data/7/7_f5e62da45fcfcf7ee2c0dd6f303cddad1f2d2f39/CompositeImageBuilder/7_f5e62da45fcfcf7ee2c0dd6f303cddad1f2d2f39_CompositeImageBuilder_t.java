 package net.nabaal.majiir.realtimerender.image;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 
 
 public class CompositeImageBuilder extends ImageProvider {
 
 	private final ImageProvider source;
 	private final int levels;
 	
 	public CompositeImageBuilder(ImageProvider source, int levels) {
 		this.source = source;
 		this.levels = levels;
 	}
 	
 	@Override
 	public BufferedImage getImage(Coordinate tile) {
 		
 		if (levels < 0) {
 			// TODO: Support.
 			throw new UnsupportedOperationException("Cannot retrieve supertiles. (TODO)");
 		} else {
 			BufferedImage image = source.getImage(tile.zoomOut(levels));
 			if (image == null) {
 				return null;
 			}
 			tile = tile.subCoordinate(levels);
			
			int width = image.getWidth() >> levels;
			int height = image.getHeight() >> levels;
			
 			// TODO: Evaluate repercussions of subimage reference
			return image.getSubimage(tile.getX() * width, tile.getY() * height, width, height);
 		}		
 		
 	}
 
 	@Override
 	public void setImage(Coordinate tile, BufferedImage image) {
 		
 		int width = image.getWidth();
 		int height = image.getHeight();
 		
 		if (levels < 0) {
 			
 			width >>= (levels * -1);
 			height >>= (levels * -1);
 			
 			tile = tile.zoomIn(levels * -1);
 			for (int x = 0; x < (1 << (levels * -1)); x++) {
 				for (int y = 0; y < (1 << (levels * -1)); y++) {
 					Coordinate subtile = new Coordinate(x, y, tile.getLevel());
 					BufferedImage subImg = image.getSubimage(subtile.getX() * width, subtile.getY() * height, width, height);
 					// TODO: Evaluate repercussions of subimage reference
 					source.setImage(tile.plus(subtile), subImg);
 				}
 			}
 		} else {
 			BufferedImage tileImg = source.getImage(tile.zoomOut(levels));
 			if (tileImg == null) {
 				tileImg = createImage(width << levels, height << levels); 
 			}
 			Graphics2D graphics = tileImg.createGraphics();
 			Coordinate subtile = tile.subCoordinate(levels);
 			graphics.drawImage(image, subtile.getX() * width, subtile.getY() * height, null);
 			source.setImage(tile.zoomOut(levels), tileImg);
 		}
 	}
 	
 	// TODO: Eliminate/pull out
 	private static BufferedImage createImage(int width, int height) {
 		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 	}
 
 	@Override
 	public void removeImage(Coordinate tile) {
 		// TODO: Method that actually works
 		//setImage(tile, createImage(userSize));
 	}
 
 	@Override
 	public Set<Coordinate> getTiles() {
 		// TODO
 		Set<Coordinate> tiles = new HashSet<Coordinate>();
 		/*
 		for (Coordinate tile : source.getTiles()) {
 			for (int x = 0; x < (1 << levels); x++) {
 				for (int y = 0; y < (1 << levels); y++) {
 					tiles.add(tile.zoomIn(levels).plus(new Coordinate(x, y, userSize)));
 				}
 			}
 		}
 		*/
 		return tiles;
 	}
 
 }
