 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.metatile;
 
 import java.awt.RenderingHints;
 import java.awt.image.RenderedImage;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.media.jai.JAI;
 import javax.media.jai.operator.CropDescriptor;
 
 import org.geoserver.platform.GeoServerExtensions;
 import org.geoserver.platform.ServiceException;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.vfny.geoserver.wms.GetMapProducer;
 import org.vfny.geoserver.wms.RasterMapProducer;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.responses.AbstractGetMapProducer;
 import org.vfny.geoserver.wms.responses.DefaultRasterMapProducer;
 import org.vfny.geoserver.wms.responses.map.metatile.QuickTileCache.MetaTileKey;
 
 /**
  * Wrapping map producer that performs on the fly meta tiling wrapping another
  * map producer. It will first peek inside a tile cache to see if the requested
  * tile has already been computed, if so, it'll encode and return that one,
  * otherwise it'll build a meta tile, split it, and finally encode just the
  * requested tile, putting the others in the tile cache.
  * 
  * @author Andrea Aime - TOPP
  * @author Simone Giannecchini - GeoSolutions
  */
 public final class MetatileMapProducer extends AbstractGetMapProducer implements
 		GetMapProducer {
 	/** A logger for this class. */
 	private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.vfny.geoserver.responses.wms.map.metatile");
 
 	/** Small number for double equality comparison */
 	public static final double EPS = 1E-6;
 
 	private GetMapRequest request;
 
 	private RasterMapProducer delegate;
 
 	private RenderedImage tile;
 
 	private static QuickTileCache tileCache;
 
 	/**
 	 * True if the request has the tiled hint, is 256x256 image, and the raw
 	 * delegate is a raster one
 	 * 
 	 * @param request
 	 * @param delegate
 	 * @return
 	 */
 	public static boolean isRequestTiled(GetMapRequest request,
 			GetMapProducer delegate) {
 		if (!(request.isTiled() && (request.getTilesOrigin() != null)
 				&& (request.getWidth() == 256) && (request.getHeight() == 256) && delegate instanceof RasterMapProducer)) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public MetatileMapProducer(GetMapRequest request, RasterMapProducer delegate) {
 	    if(tileCache == null) {
 	        tileCache = (QuickTileCache) GeoServerExtensions.bean("metaTileCache");
 	    }
 		this.request = request;
 		this.delegate = delegate;
 	}
 
 	public void produceMap() throws WmsException {
 		// get the key that identifies the meta tile. The cache will make sure
 		// two threads asking
 		// for the same tile will get the same key, and thus will synchronize
 		// with each other
 		// (the first eventually builds the meta-tile, the second finds it ready
 		// to be used)
 		QuickTileCache.MetaTileKey key = tileCache.getMetaTileKey(request);
 
 		synchronized (key) {
 			tile = tileCache.getTile(key, request);
 			if (LOGGER.isLoggable(Level.FINER)) {
 				LOGGER.finer("Looked for meta tile " + key.metaTileCoords.x
 						+ ", " + key.metaTileCoords.y + "in cache: "
 						+ ((tile != null) ? "hit!" : "miss"));
 			}
 
 			if (tile == null) {
 				// compute the meta-tile
 				if (LOGGER.isLoggable(Level.FINER)) {
 					LOGGER.finer("Building meta tile " + key.metaTileCoords.x
 							+ ", " + key.metaTileCoords.y);
 				}
 
 				// alter the map definition so that we build a meta-tile instead
 				// of just the tile
 				ReferencedEnvelope origEnv = mapContext.getAreaOfInterest();
 				mapContext.setAreaOfInterest(new ReferencedEnvelope(key
 						.getMetaTileEnvelope(), origEnv
 						.getCoordinateReferenceSystem()));
 				mapContext.setMapWidth(key.getTileSize() * key.getMetaFactor());
 				mapContext
 						.setMapHeight(key.getTileSize() * key.getMetaFactor());
 
 				// generate, split and cache
 				delegate.setMapContext(mapContext);
 				
 				delegate.produceMap();
 				
 
 				RenderedImage metaTile = delegate.getImage();
 				RenderedImage[] tiles = split(key, metaTile, mapContext);
 				tileCache.storeTiles(key, tiles);
 				tile = tileCache.getTile(key, request, tiles);
 			}
 		}
 	}
 
 	// /**
 	// * Splits the tile into a set of tiles, numbered from lower right and
 	// going up so
 	// * that first row is 0,1,2,...,metaTileFactor, and so on.
 	// * In the case of a 3x3 meta-tile, the layout is as follows:
 	// * <pre>
 	// * 6 7 8
 	// * 3 4 5
 	// * 0 1 2
 	// * </pre>
 	// * @param key
 	// * @param metaTile
 	// * @param map
 	// * @return
 	// */
 	// private BufferedImage[] split(MetaTileKey key, BufferedImage metaTile,
 	// WMSMapContext map) {
 	// int metaFactor = key.getMetaFactor();
 	// BufferedImage[] tiles = new BufferedImage[key.getMetaFactor() *
 	// key.getMetaFactor()];
 	// int tileSize = key.getTileSize();
 	//
 	// for (int i = 0; i < metaFactor; i++) {
 	// for (int j = 0; j < metaFactor; j++) {
 	// // TODO: create child writable rasters instead of cloning the images
 	// using
 	// // graphics2d. Should be quite a bit faster and save some memory. Or
 	// else,
 	// // store meta-tiles in the cache directly, and extract children tiles
 	// // on demand (even simpler)
 	// BufferedImage tile;
 	//
 	// // keep the palette if necessary
 	// if (metaTile.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
 	// tile = new BufferedImage(tileSize, tileSize,
 	// BufferedImage.TYPE_BYTE_INDEXED,
 	// (IndexColorModel) metaTile.getColorModel());
 	// } else if (metaTile.getType() == BufferedImage.TYPE_CUSTOM) {
 	// throw new RuntimeException("We don't support custom buffered image
 	// tiling");
 	// } else {
 	// tile = new BufferedImage(tileSize, tileSize, metaTile.getType());
 	// }
 	//
 	// Graphics2D g2d = (Graphics2D) tile.getGraphics();
 	// AffineTransform at = AffineTransform.getTranslateInstance(-j * tileSize,
 	// (-tileSize * (metaFactor - 1)) + (i * tileSize));
 	// setupBackground(g2d, map);
 	// g2d.drawRenderedImage(metaTile, at);
 	// g2d.dispose();
 	// tiles[(i * key.getMetaFactor()) + j] = tile;
 	// }
 	// }
 	//
 	// return tiles;
 	// }
 
 	/**
 	 * Splits the tile into a set of tiles, numbered from lower right and going
 	 * up so that first row is 0,1,2,...,metaTileFactor, and so on. In the case
 	 * of a 3x3 meta-tile, the layout is as follows:
 	 * 
 	 * <pre>
 	 *    6 7 8
 	 *    3 4 5
 	 *    0 1 2
 	 * </pre>
 	 * 
 	 * @param key
 	 * @param metaTile
 	 * @param map
 	 * @return
 	 */
 	private RenderedImage[] split(MetaTileKey key, RenderedImage metaTile,
 			WMSMapContext map) {
 		final int metaFactor = key.getMetaFactor();
 		final RenderedImage[] tiles = new RenderedImage[key.getMetaFactor()
 				* key.getMetaFactor()];
 		final int tileSize = key.getTileSize();
 		final RenderingHints no_cache = new RenderingHints(JAI.KEY_TILE_CACHE,
 				null);
 		
 		for (int i = 0; i < metaFactor; i++) {
 			for (int j = 0; j < metaFactor; j++) {
 				int x = j * tileSize;
 				int y = (tileSize * (metaFactor - 1)) - (i * tileSize);
 
 				tile = CropDescriptor.create(metaTile, new Float(x), new Float(
 						y), new Float(tileSize), new Float(tileSize), no_cache);
 				tiles[(i * key.getMetaFactor()) + j] = tile;
 			}
 		}
 
 		return tiles;
 	}
 
 	/**
 	 * Have the delegate encode the tile
 	 */
 	public void writeTo(OutputStream out) throws ServiceException, IOException {
 		delegate.formatImageOutputStream(tile, out);
 	}
 
 	public void abort() {
 		delegate.abort();
 	}
 
 	public String getContentDisposition() {
 		return delegate.getContentDisposition();
 	}
 
 	public String getContentType() throws IllegalStateException {
 		return delegate.getContentType();
 	}
 }
