 
 
 package es.igosoftware.experimental.vectorial;
 
 import es.igosoftware.euclid.bounding.GAxisAlignedRectangle;
 import es.igosoftware.euclid.experimental.vectorial.rendering.GPolygon2DRenderer;
 import es.igosoftware.euclid.experimental.vectorial.rendering.GRenderingAttributes;
 import es.igosoftware.euclid.projection.GProjection;
 import es.igosoftware.euclid.shape.IPolygon2D;
 import es.igosoftware.euclid.vector.GVector2D;
 import es.igosoftware.euclid.vector.IVector2;
 import es.igosoftware.globe.IGlobeApplication;
 import es.igosoftware.globe.IGlobeLayer;
 import es.igosoftware.globe.actions.ILayerAction;
 import es.igosoftware.globe.attributes.GBooleanLayerAttribute;
 import es.igosoftware.globe.attributes.GColorLayerAttribute;
 import es.igosoftware.globe.attributes.GFloatLayerAttribute;
 import es.igosoftware.globe.attributes.GGroupAttribute;
 import es.igosoftware.globe.attributes.ILayerAttribute;
 import es.igosoftware.util.GCollections;
 import es.igosoftware.util.GPair;
 import es.igosoftware.util.GUtils;
 import es.igosoftware.util.IPredicate;
 import es.igosoftware.util.ITransformer;
 import es.igosoftware.util.LRUCache;
 import es.igosoftware.util.LRUCache.Entry;
 import es.igosoftware.utils.GGlobeStateKeyCache;
 import es.igosoftware.utils.GWWUtils;
 import gov.nasa.worldwind.View;
 import gov.nasa.worldwind.avlist.AVKey;
 import gov.nasa.worldwind.geom.Box;
 import gov.nasa.worldwind.geom.Frustum;
 import gov.nasa.worldwind.geom.Sector;
 import gov.nasa.worldwind.geom.Vec4;
 import gov.nasa.worldwind.globes.Globe;
 import gov.nasa.worldwind.layers.AbstractLayer;
 import gov.nasa.worldwind.render.DrawContext;
 import gov.nasa.worldwind.render.SurfaceImage;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.awt.image.RasterFormatException;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.FutureTask;
 
 import javax.imageio.ImageIO;
 import javax.swing.Icon;
 
 
 public class GPolygon2DLayer
          extends
             AbstractLayer
          implements
             IGlobeLayer {
 
 
    private static final int    TIMEOUT_FOR_CACHED_RESULTS     = 200;
 
    private static final String RENDERING_CACHE_DIRECTORY_NAME = "rendering-cache";
    private static final File   RENDERING_CACHE_DIRECTORY      = new File(RENDERING_CACHE_DIRECTORY_NAME);
 
 
    private static final int    BYTES_PER_PIXEL                = 4 /* rgba */
                                                               * 4 /* 4 bytes x integer*/;
 
 
    private static final class RenderingKey {
       private final GPolygon2DLayer       _layer;
       private final GAxisAlignedRectangle _tileBounds;
       //      private final Sector                _tileSector;
       private final GRenderingAttributes  _renderingAttributes;
       private final String                _id;
 
 
       private RenderingKey(final GPolygon2DLayer layer,
                            final GAxisAlignedRectangle tileSectorBounds,
                            //                           final Sector tileSector,
                            final GRenderingAttributes renderingAttributes,
                            final String id) {
          _layer = layer;
          _tileBounds = tileSectorBounds;
          //         _tileSector = tileSector;
          _renderingAttributes = renderingAttributes;
          _id = id;
       }
 
 
       @Override
       public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + ((_renderingAttributes == null) ? 0 : _renderingAttributes.hashCode());
          result = prime * result + ((_layer == null) ? 0 : _layer.hashCode());
          result = prime * result + ((_tileBounds == null) ? 0 : _tileBounds.hashCode());
          return result;
       }
 
 
       @Override
       public boolean equals(final Object obj) {
          if (this == obj) {
             return true;
          }
          if (obj == null) {
             return false;
          }
          if (getClass() != obj.getClass()) {
             return false;
          }
          final RenderingKey other = (RenderingKey) obj;
          if (_renderingAttributes == null) {
             if (other._renderingAttributes != null) {
                return false;
             }
          }
          else if (!_renderingAttributes.equals(other._renderingAttributes)) {
             return false;
          }
          if (_layer == null) {
             if (other._layer != null) {
                return false;
             }
          }
          else if (!_layer.equals(other._layer)) {
             return false;
          }
          if (_tileBounds == null) {
             if (other._tileBounds != null) {
                return false;
             }
          }
          else if (!_tileBounds.equals(other._tileBounds)) {
             return false;
          }
          return true;
       }
 
 
       @Override
       public String toString() {
          return "RenderingKey [layer=" + _layer + ", tileBounds=" + _tileBounds + ", renderingAttributes=" + _renderingAttributes
                 + "]";
       }
 
 
       private String uniqueName() {
          return _layer.uniqueName() + _id + _renderingAttributes.uniqueName();
       }
    }
 
 
    private static final LRUCache<RenderingKey, Future<BufferedImage>, RuntimeException> IMAGES_CACHE;
 
 
    private static final LinkedList<RendererFutureTask>                                  RENDERING_TASKS = new LinkedList<RendererFutureTask>();
 
    private static class RendererFutureTask
             extends
                FutureTask<BufferedImage>
             implements
                Comparable<RendererFutureTask> {
 
       private final double          _priority;
       //      private final Sector    _tileSector;
       private GPolygon2DLayer       _layer;
       private GAxisAlignedRectangle _tileBounds;
 
 
       private RendererFutureTask(final RenderingKey key,
                                  final double priority) {
          super(new Callable<BufferedImage>() {
 
             private BufferedImage getImageFromDiskCache() {
                final File file = new File(RENDERING_CACHE_DIRECTORY, key.uniqueName() + ".png");
 
                if (!file.exists()) {
                   return null;
                }
 
                try {
                   return ImageIO.read(file);
                }
                catch (final IOException e) {
                }
 
                return null;
             }
 
 
             @Override
             public BufferedImage call() throws Exception {
 
                final BufferedImage imageFromCache = getImageFromDiskCache();
                if (imageFromCache != null) {
                   // System.out.println("disk cache hit");
                   key._layer.redraw();
                   return imageFromCache;
                }
 
 
                final GPolygon2DLayer layer = key._layer;
                final GPolygon2DRenderer renderer = layer._renderer;
                final BufferedImage renderedImage = renderer.render(key._tileBounds, key._renderingAttributes);
                layer.redraw();
 
                saveImageIntoDiskoCache(renderedImage);
 
                return renderedImage;
             }
 
 
             private void saveImageIntoDiskoCache(final BufferedImage renderedImage) {
                final Thread worker = new Thread() {
                   @Override
                   public void run() {
                      saveImageIntoDiskCache(key, renderedImage);
                   }
                };
                worker.setDaemon(true);
                worker.setPriority(Thread.MIN_PRIORITY);
                worker.start();
             }
          });
 
          _priority = priority;
          //         _tileSector = key._tileSector;
          _tileBounds = key._tileBounds;
          _layer = key._layer;
       }
 
 
       @Override
       public int compareTo(final RendererFutureTask that) {
          return Double.compare(that._priority, _priority);
       }
 
    }
 
 
    private static class RenderingWorker
             extends
                Thread {
 
       private static int _workerID = 0;
 
 
       private RenderingWorker() {
          super("Rendering Worker #" + _workerID++);
          setDaemon(true);
          setPriority(Thread.MIN_PRIORITY);
       }
 
 
       @Override
       public void run() {
          while (true) {
             final RendererFutureTask task = findTask();
 
             if (task == null) {
                GUtils.delay(500);
             }
             else {
                task.run();
             }
          }
       }
 
    }
 
 
    private static RendererFutureTask findTask() {
       synchronized (RENDERING_TASKS) {
          if (RENDERING_TASKS.isEmpty()) {
             return null;
          }
 
          final RendererFutureTask selectedTask = findBestTaskForCurrentTiles();
          //         if (selectedTask == null) {
          //            selectedTask = findBestTask();
          //         }
 
          if (selectedTask != null) {
             RENDERING_TASKS.remove(selectedTask);
             return selectedTask;
          }
       }
 
       return null;
    }
 
 
    //   private static RendererFutureTask findBestTask() {
    //      double biggestPriority = Double.NEGATIVE_INFINITY;
    //      RendererFutureTask selectedTask = null;
    //
    //      for (final RendererFutureTask task : RENDERING_TASKS) {
    //         final double currentPriority = task._priority;
    //         if (currentPriority > biggestPriority) {
    //            biggestPriority = currentPriority;
    //            selectedTask = task;
    //         }
    //      }
    //
    //      return selectedTask;
    //   }
 
 
    private static RendererFutureTask findBestTaskForCurrentTiles() {
       double biggestPriority = Double.NEGATIVE_INFINITY;
       RendererFutureTask selectedTask = null;
 
       for (final RendererFutureTask task : RENDERING_TASKS) {
          final List<Tile> currentTiles = task._layer._currentTiles;
          synchronized (currentTiles) {
             for (final Tile currentTile : currentTiles) {
                if (task._tileBounds.equals(currentTile._tileBounds)) {
                   final double currentPriority = task._priority;
                   if (currentPriority > biggestPriority) {
                      biggestPriority = currentPriority;
                      selectedTask = task;
                   }
                }
             }
          }
       }
 
       return selectedTask;
    }
 
 
    private String uniqueName() {
       return _uniqueName;
    }
 
 
    private static void saveImageIntoDiskCache(final RenderingKey key,
                                               final BufferedImage image) {
       try {
          if (image != null) {
             final String fileName = key.uniqueName() + ".png";
             //            final File tempFile = new File(RENDERING_CACHE_DIRECTORY, fileName + ".PART");
             //            final File tempFile = new File(RENDERING_CACHE_DIRECTORY, fileName + ".PART");
 
             final File tempFile = File.createTempFile("temp", "png", RENDERING_CACHE_DIRECTORY);
 
             ImageIO.write(image, "png", tempFile);
             //            final ByteBuffer buffer = DDSCompressor.compressImage(image);
             //            WWIO.saveBuffer(buffer, tempFile);
 
             tempFile.renameTo(new File(RENDERING_CACHE_DIRECTORY, fileName));
          }
       }
       catch (final IOException e) {
          e.printStackTrace();
       }
    }
 
 
    static {
       //      try {
       //         GIOUtils.assureEmptyDirectory(RENDERING_CACHE_DIRECTORY_NAME, false);
       //      }
       //      catch (final IOException e) {
       //         e.printStackTrace();
       //      }
       if (!RENDERING_CACHE_DIRECTORY.exists()) {
          RENDERING_CACHE_DIRECTORY.mkdirs();
       }
 
 
       final int numberOfThreads = Math.max(Runtime.getRuntime().availableProcessors() / 4, 1);
       //      final int numberOfThreads = 1;
       for (int i = 0; i < numberOfThreads; i++) {
          new RenderingWorker().start();
       }
 
       final LRUCache.SizePolicy<RenderingKey, Future<BufferedImage>, RuntimeException> sizePolicy;
       sizePolicy = new LRUCache.SizePolicy<GPolygon2DLayer.RenderingKey, Future<BufferedImage>, RuntimeException>() {
          final private long _maxImageCacheSizeInBytes = Runtime.getRuntime().maxMemory() / 3;
 
 
          @Override
          public boolean isOversized(final List<Entry<RenderingKey, Future<BufferedImage>, RuntimeException>> entries) {
             long totalBytes = 0;
 
             for (final Entry<RenderingKey, Future<BufferedImage>, RuntimeException> entry : entries) {
                final GRenderingAttributes renderingAttributes = entry.getKey()._renderingAttributes;
                totalBytes += renderingAttributes._textureWidth * renderingAttributes._textureHeight * BYTES_PER_PIXEL;
             }
 
             return (totalBytes > _maxImageCacheSizeInBytes);
          }
       };
 
 
       final LRUCache.ValueVisitor<RenderingKey, Future<BufferedImage>, RuntimeException> removedListener;
       removedListener = new LRUCache.ValueVisitor<RenderingKey, Future<BufferedImage>, RuntimeException>() {
          @Override
          public void visit(final RenderingKey key,
                            final Future<BufferedImage> value,
                            final RuntimeException exception) {
             //            saveImage(key, value);
             //            System.out.println("Removed " + key);
          }
       };
 
 
       final LRUCache.ValueFactory<RenderingKey, Future<BufferedImage>, RuntimeException> factory = new LRUCache.ValueFactory<RenderingKey, Future<BufferedImage>, RuntimeException>() {
          @Override
          public Future<BufferedImage> create(final RenderingKey key) {
             final double priority = key._tileBounds.area();
             final RendererFutureTask future = new RendererFutureTask(key, priority);
             synchronized (RENDERING_TASKS) {
                RENDERING_TASKS.add(future);
             }
             return future;
 
             // return _renderer.render(key._tileSectorBounds, key._attributes);
          }
       };
 
       IMAGES_CACHE = new LRUCache<RenderingKey, Future<BufferedImage>, RuntimeException>(sizePolicy, factory, removedListener, 0);
    }
 
 
    private final class Tile {
 
       private final Tile                  _parent;
 
       //      private final Sector                _tileSector;
       private final GAxisAlignedRectangle _tileBounds;
       private final IVector2<?>           _tileBoundsExtent;
 
       private final String                _id;
 
       private SurfaceImage                _surfaceImage;
 
       private BufferedImage               _ancestorContribution;
 
 
       private Tile(final Tile parent,
                    final int positionInParent,
                    final GAxisAlignedRectangle tileBounds) {
 
          _parent = parent;
          //         _level = (parent == null) ? 0 : parent._level + 1;
          if (parent == null) {
             _id = Integer.toHexString(positionInParent);
          }
          else {
             _id = parent._id + Integer.toHexString(positionInParent);
          }
 
          _tileBounds = tileBounds;
          _tileBoundsExtent = _tileBounds.getExtent();
 
          //         _tileSector = GWWUtils.toSector(tileBounds, GProjection.EPSG_4326);
       }
 
 
       private Box getBox(final DrawContext dc) {
          // return Sector.computeBoundingBox(globe, verticalExaggeration, _sector);
 
          return BOX_CACHE.get(dc, _tileBounds);
       }
 
 
       private double computeProjectedPixels(final DrawContext dc) {
          //         final LatLon[] tileSectorCorners = _tileSector.getCorners();
 
          final List<IVector2<?>> tileSectorCorners = _tileBounds.getVertices();
 
          final Vec4 firstProjected = GWWUtils.getScreenPoint(dc, tileSectorCorners.get(0));
          double minX = firstProjected.x;
          double maxX = firstProjected.x;
          double minY = firstProjected.y;
          double maxY = firstProjected.y;
 
          for (int i = 1; i < tileSectorCorners.size(); i++) {
             final Vec4 projected = GWWUtils.getScreenPoint(dc, tileSectorCorners.get(i));
 
             if (projected.x < minX) {
                minX = projected.x;
             }
             if (projected.y < minY) {
                minY = projected.y;
             }
             if (projected.x > maxX) {
                maxX = projected.x;
             }
             if (projected.y > maxY) {
                maxY = projected.y;
             }
          }
 
 
          // calculate the area of the rectangle
          final double width = maxX - minX;
          final double height = maxY - minY;
          final double area = Math.abs(width) * Math.abs(height);
          return area;
       }
 
 
       private boolean needToSplit(final DrawContext dc) {
          return computeProjectedPixels(dc) > (_attributes._textureWidth * _attributes._textureHeight);
       }
 
 
       private Tile[] slit() {
          //         final Globe globe = dc.getGlobe();
 
          //         final GAxisAlignedRectangle[] sectors = _tileBounds.subdivideAtCenter();
          //
          //         final Tile[] subTiles = new Tile[4];
          //         subTiles[0] = new Tile(this, globe, GWWUtils.toSector(sectors[0], _projection));
          //         subTiles[1] = new Tile(this, globe, GWWUtils.toSector(sectors[1], _projection));
          //         subTiles[2] = new Tile(this, globe, GWWUtils.toSector(sectors[2], _projection));
          //         subTiles[3] = new Tile(this, globe, GWWUtils.toSector(sectors[3], _projection));
 
          final GAxisAlignedRectangle[] sectors = _tileBounds.subdivideAtCenter();
 
          final Tile[] subTiles = new Tile[4];
          subTiles[0] = new Tile(this, 0, sectors[0]);
          subTiles[1] = new Tile(this, 1, sectors[1]);
          subTiles[2] = new Tile(this, 2, sectors[2]);
          subTiles[3] = new Tile(this, 3, sectors[3]);
 
          return subTiles;
       }
 
 
       private void preRender(final DrawContext dc) {
          if ((_surfaceImage == null) || (_ancestorContribution != null)) {
             final Future<BufferedImage> renderedImageFuture = IMAGES_CACHE.get(createRenderingKey());
 
             if (renderedImageFuture.isDone()) {
                try {
                   setImageToSurfaceImage(renderedImageFuture.get());
                   _ancestorContribution = null;
                }
                catch (final InterruptedException e) {
                }
                catch (final ExecutionException e) {
                }
             }
          }
 
 
          if ((_surfaceImage == null) && (_ancestorContribution == null)) {
             _ancestorContribution = calculateAncestorContribution();
             setImageToSurfaceImage(_ancestorContribution);
          }
 
 
          if (_surfaceImage != null) {
             _surfaceImage.preRender(dc);
          }
 
       }
 
 
       private void setImageToSurfaceImage(final BufferedImage image) {
          if (image == null) {
             return;
          }
 
          final Sector tileSector = GWWUtils.toSector(_tileBounds, GProjection.EPSG_4326);
          if (_surfaceImage == null) {
             _surfaceImage = new SurfaceImage(image, tileSector);
          }
          else {
             _surfaceImage.setImageSource(image, tileSector);
          }
       }
 
 
       private BufferedImage calculateAncestorContribution() {
          final GPair<Tile, BufferedImage> ancestorAndImage = findNearestAncestorWithImage();
 
          if (ancestorAndImage == null) {
             return null;
          }
 
          final BufferedImage ancestorImage = ancestorAndImage._second;
          if (ancestorImage == null) {
             return null;
          }
 
          final Tile ancestor = ancestorAndImage._first;
          ancestor.moveUpInCache();
 
          final IVector2<?> scale = _tileBoundsExtent.div(ancestor._tileBoundsExtent);
 
          final GVector2D textureExtent = new GVector2D(_attributes._textureWidth, _attributes._textureHeight);
 
          final IVector2<?> topLeft = _tileBounds._lower.sub(ancestor._tileBounds._lower).scale(scale).div(_tileBoundsExtent).scale(
                   textureExtent);
 
          final IVector2<?> widthAndHeight = textureExtent.scale(scale);
 
          final int width = (int) widthAndHeight.x();
          final int height = (int) widthAndHeight.y();
          final int x = (int) topLeft.x();
          final int y = (int) -(topLeft.y() + height - _attributes._textureHeight); // flip y
 
          try {
             final BufferedImage subimage = ancestorImage.getSubimage(x, y, width, height);
             return markImageAsWorkInProgress(subimage);
          }
          catch (final RasterFormatException e) {
          }
 
 
          return null;
       }
 
 
       //      private final Image workingIcon = GGlobeApplication.instance().getImage("working.png");
 
 
       private BufferedImage markImageAsWorkInProgress(final BufferedImage image) {
          final int TODO_flag_the_image_as_work_in_progress;
 
          return image;
          //         if (image == null) {
          //            return null;
          //         }
          //
          //         final int width = _attributes._textureWidth;
          //         final int height = _attributes._textureHeight;
          //
          //         final BufferedImage result = new BufferedImage(width, height,
          //                  image.getColorModel().hasAlpha() ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
          //         final Graphics2D g2d = result.createGraphics();
          //
          //         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          //         g2d.drawImage(image, 0, 0, width, height, null);
          //
          //         if (workingIcon != null) {
          //            //            final int iconWidth = workingIcon.getWidth(null);
          //            //            final int iconHeight = workingIcon.getHeight(null);
          //            //g2d.drawImage(workingIcon, (width - iconWidth) / 2, (height - iconHeight) / 2, iconWidth, iconHeight, null);
          //            g2d.drawImage(workingIcon, 0, 0, width, height, null);
          //         }
          //
          //         g2d.dispose();
          //
          //         return result;
       }
 
 
       private RenderingKey createRenderingKey() {
          return new RenderingKey(GPolygon2DLayer.this, _tileBounds, _attributes, _id);
       }
 
 
       private GPair<Tile, BufferedImage> findNearestAncestorWithImage() {
          Tile ancestor = _parent;
          while (ancestor != null) {
             final RenderingKey ancestorKey = ancestor.createRenderingKey();
             final Future<BufferedImage> futureImage = IMAGES_CACHE.getValueOrNull(ancestorKey);
             if ((futureImage != null) && futureImage.isDone()) {
                try {
                   return new GPair<Tile, BufferedImage>(ancestor, futureImage.get());
                }
                catch (final InterruptedException e) {
                }
                catch (final ExecutionException e) {
                }
             }
 
             ancestor = ancestor._parent;
          }
 
          return null;
       }
 
 
       private void render(final DrawContext dc) {
          if (_surfaceImage != null) {
             _surfaceImage.render(dc);
          }
       }
 
 
       //      private void renderExtent(final DrawContext dc) {
       //         GWWUtils.renderSector(dc, _tileSector, 1, 1, 0);
       //      }
 
 
       private void moveUpInCache() {
          IMAGES_CACHE.get(createRenderingKey());
          //         IMAGES_CACHE.moveUp(createRenderingKey());
       }
    }
 
 
    private static final GGlobeStateKeyCache<GAxisAlignedRectangle, Box> BOX_CACHE;
 
    static {
       BOX_CACHE = new GGlobeStateKeyCache<GAxisAlignedRectangle, Box>(
                new GGlobeStateKeyCache.Factory<GAxisAlignedRectangle, Box>() {
                   @Override
                   public Box create(final DrawContext dc,
                                     final GAxisAlignedRectangle bounds) {
                      final Globe globe = dc.getView().getGlobe();
                      final double verticalExaggeration = dc.getVerticalExaggeration();
 
                      final Sector sector = GWWUtils.toSector(bounds, GProjection.EPSG_4326);
 
                      return Sector.computeBoundingBox(globe, verticalExaggeration, sector);
                   }
                });
    }
 
 
    //   private final GProjection                             _projection;
 
    private final Sector                                                 _polygonsSector;
    //   private final LatLon[]                                _sectorCorners;
 
 
    private final GPolygon2DRenderer                                     _renderer;
    private GRenderingAttributes                                         _attributes;
 
    //   private Globe                                         _lastGlobe;
    private List<Tile>                                                   _topTiles;
    private final List<Tile>                                             _currentTiles               = new ArrayList<Tile>();
 
    //   private boolean                                                      _showExtents                = false;
 
    private int                                                          _fillColorAlpha             = 127;
    private int                                                          _borderColorAlpha           = 255;
 
    private View                                                         _lastView;
 
 
    //   private Sector                                        _lastVisibleSector;
 
 
    private final String                                                 _resourceName;
    //   private final long                                    _lastComputedProjectedPixelsTime = -1;
    //   private float                                         _lastComputedProjectedPixels;
    private long                                                         _lastCurrentTilesCalculated = -1;
 
    private boolean                                                      _debugRendering             = false;
 
    private final String                                                 _uniqueName;
 
    private final GAxisAlignedRectangle                                  _polygonsBounds;
 
 
    public GPolygon2DLayer(final String resourceName,
                           final String uniqueName,
                           final List<IPolygon2D<?>> polygons) {
       _resourceName = resourceName;
       _uniqueName = uniqueName;
 
       _polygonsBounds = GAxisAlignedRectangle.minimumBoundingRectangle(polygons);
 
       _polygonsSector = GWWUtils.toSector(_polygonsBounds, GProjection.EPSG_4326);
       //      _sectorCorners = _sector.getCorners();
 
       _renderer = new GPolygon2DRenderer(polygons);
 
       _attributes = createRenderingAttributes();
    }
 
 
    @Override
    public String getName() {
       //      return "Vectorial: " + _resourceName;
       return _resourceName;
    }
 
 
    private GRenderingAttributes createRenderingAttributes() {
       final boolean renderLODIgnores = true;
       final float borderWidth = 1f;
       final Color fillColor = createColor(new Color(1, 1, 0), _fillColorAlpha);
       final Color borderColor = createColor(Color.WHITE, _borderColorAlpha);
       final double lodMinSize = 5;
       final boolean debugLODRendering = _debugRendering;
       final int textureWidth = 256;
       final int textureHeight = 256;
       final boolean renderBounds = _debugRendering;
 
       return new GRenderingAttributes(renderLODIgnores, borderWidth, fillColor, borderColor, lodMinSize, debugLODRendering,
                textureWidth, textureHeight, renderBounds);
    }
 
 
    private static List<GAxisAlignedRectangle> createTopLevelSectors(final GAxisAlignedRectangle polygonsSector) {
 
       //      final int latitudeSubdivisions = 2;
       //      final int longitudeSubdivisions = 4;
       //      final List<Sector> allTopLevelSectors = GWWUtils.createTopLevelSectors(latitudeSubdivisions, longitudeSubdivisions);
 
       final List<GAxisAlignedRectangle> allTopLevelSectors = createTopLevelSectors();
 
       final List<GAxisAlignedRectangle> intersectingSectors = GCollections.select(allTopLevelSectors,
                new IPredicate<GAxisAlignedRectangle>() {
                   @Override
                   public boolean evaluate(final GAxisAlignedRectangle sector) {
                      return sector.touches(polygonsSector);
                   }
                });
 
       return GCollections.collect(intersectingSectors, new ITransformer<GAxisAlignedRectangle, GAxisAlignedRectangle>() {
          @Override
          public GAxisAlignedRectangle transform(final GAxisAlignedRectangle sector) {
             return tryToReduce(sector);
          }
 
 
          private GAxisAlignedRectangle tryToReduce(final GAxisAlignedRectangle sector) {
             if (polygonsSector.isFullInside(sector)) {
                final GAxisAlignedRectangle[] subdivisions = sector.subdivideAtCenter();
 
                GAxisAlignedRectangle lastTouchedSubdivision = null;
                for (final GAxisAlignedRectangle subdivision : subdivisions) {
                   if (subdivision.touches(polygonsSector)) {
                      if (lastTouchedSubdivision != null) {
                         return sector;
                      }
                      lastTouchedSubdivision = subdivision;
                   }
                }
 
                return tryToReduce(lastTouchedSubdivision);
             }
 
             return sector;
          }
       });
    }
 
 
    @Override
    public Icon getIcon(final IGlobeApplication application) {
       return application.getIcon("vectorial.png");
    }
 
 
    @Override
    public Sector getExtent() {
       return _polygonsSector;
    }
 
 
    @Override
    public GProjection getProjection() {
       return GProjection.EPSG_4326;
    }
 
 
    @Override
    public void setProjection(final GProjection proj) {
       throw new RuntimeException("Operation not supported!");
    }
 
 
    @Override
    public void redraw() {
       // fire event to force a redraw
       //      firePropertyChange(AVKey.LAYER, null, this);
       if (_lastView != null) {
          _lastView.firePropertyChange(AVKey.VIEW, null, _lastView);
       }
    }
 
 
    @Override
    public List<ILayerAttribute<?>> getLayerAttributes(final IGlobeApplication application) {
       final List<ILayerAttribute<?>> result = new ArrayList<ILayerAttribute<?>>();
 
       addBorderAttributes(result);
 
       addFillAttributes(result);
 
       addAdvancedAttributes(result);
 
       return result;
    }
 
 
    private void addAdvancedAttributes(final List<ILayerAttribute<?>> result) {
       final ILayerAttribute<?> debugRendering = new GBooleanLayerAttribute("Debug Rendering", "DebugRendering") {
          @Override
          public boolean isVisible() {
             return true;
          }
 
 
          @Override
          public Boolean get() {
             return isDebugRendering();
          }
 
 
          @Override
          public void set(final Boolean value) {
             setDebugRendering(value);
          }
       };
 
       result.add(new GGroupAttribute("Advanced", debugRendering));
    }
 
 
    private void addFillAttributes(final List<ILayerAttribute<?>> result) {
       final ILayerAttribute<?> fillColor = new GColorLayerAttribute("Color", "FillColor") {
          @Override
          public boolean isVisible() {
             return true;
          }
 
 
          @Override
          public Color get() {
             return getFillColor();
          }
 
 
          @Override
          public void set(final Color value) {
             setFillColor(value);
          }
       };
 
       final ILayerAttribute<?> fillAlpha = new GFloatLayerAttribute("Alpha", "FillColorAlpha", 0, 255,
                GFloatLayerAttribute.WidgetType.SPINNER, 1) {
          @Override
          public boolean isVisible() {
             return true;
          }
 
 
          @Override
          public Float get() {
             return Float.valueOf(getFillColorAlpha());
          }
 
 
          @Override
          public void set(final Float value) {
             setFillColorAlpha(Math.round(value));
          }
       };
 
       result.add(new GGroupAttribute("Fill", fillColor, fillAlpha));
    }
 
 
    private void addBorderAttributes(final List<ILayerAttribute<?>> result) {
       final ILayerAttribute<?> borderWidth = new GFloatLayerAttribute("Width", "BorderWidth", 0, 5,
                GFloatLayerAttribute.WidgetType.SPINNER, 0.25f) {
          @Override
          public boolean isVisible() {
             return true;
          }
 
 
          @Override
          public Float get() {
             return getBorderWidth();
          }
 
 
          @Override
          public void set(final Float value) {
             setBorderWidth(value);
          }
       };
 
       final ILayerAttribute<?> borderColor = new GColorLayerAttribute("Color", "BorderColor") {
          @Override
          public boolean isVisible() {
             return true;
          }
 
 
          @Override
          public Color get() {
             return getBorderColor();
          }
 
 
          @Override
          public void set(final Color value) {
             setBorderColor(value);
          }
       };
 
       final ILayerAttribute<?> borderAlpha = new GFloatLayerAttribute("Alpha", "BorderColorAlpha", 0, 255,
                GFloatLayerAttribute.WidgetType.SPINNER, 1) {
          @Override
          public boolean isVisible() {
             return true;
          }
 
 
          @Override
          public Float get() {
             return Float.valueOf(getBorderColorAlpha());
          }
 
 
          @Override
          public void set(final Float value) {
             setBorderColorAlpha(Math.round(value));
          }
       };
 
       result.add(new GGroupAttribute("Border", borderWidth, borderColor, borderAlpha));
    }
 
 
    public int getFillColorAlpha() {
       return _fillColorAlpha;
    }
 
 
    public int getBorderColorAlpha() {
       return _borderColorAlpha;
    }
 
 
    public void setBorderColorAlpha(final int newValue) {
       final Color newColor = createColor(getBorderColor(), newValue);
 
       if (_attributes._borderColor.equals(newColor)) {
          return;
       }
 
       _borderColorAlpha = newValue;
 
       final Color oldValue = _attributes._borderColor;
 
       _attributes = new GRenderingAttributes(_attributes._renderLODIgnores, _attributes._borderWidth, _attributes._fillColor,
                newColor, _attributes._lodMinSize, _attributes._debugLODRendering, _attributes._textureWidth,
                _attributes._textureHeight, _attributes._renderBounds);
 
       clearCache();
 
       firePropertyChange("BorderColor", oldValue, newValue);
       firePropertyChange("BorderColorAlpha", oldValue, newValue);
    }
 
 
    public void setFillColorAlpha(final int newValue) {
       final Color newColor = createColor(getFillColor(), newValue);
 
       if (_attributes._fillColor.equals(newColor)) {
          return;
       }
 
       _fillColorAlpha = newValue;
 
       final Color oldValue = _attributes._fillColor;
 
       _attributes = new GRenderingAttributes(_attributes._renderLODIgnores, _attributes._borderWidth, newColor,
                _attributes._borderColor, _attributes._lodMinSize, _attributes._debugLODRendering, _attributes._textureWidth,
                _attributes._textureHeight, _attributes._renderBounds);
 
       clearCache();
 
       firePropertyChange("FillColor", oldValue, newValue);
       firePropertyChange("FillColorAlpha", oldValue, newValue);
    }
 
 
    private Color getBorderColor() {
       return _attributes._borderColor;
    }
 
 
    private Color getFillColor() {
       return _attributes._fillColor;
    }
 
 
    private static Color createColor(final Color color,
                                     final int alpha) {
       return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
 
 
    public void setFillColor(final Color newOpaqueColor) {
       final Color newValue = createColor(newOpaqueColor, _fillColorAlpha);
 
       if (_attributes._fillColor.equals(newValue)) {
          return;
       }
 
       final Color oldValue = _attributes._fillColor;
 
       _attributes = new GRenderingAttributes(_attributes._renderLODIgnores, _attributes._borderWidth, newValue,
                _attributes._borderColor, _attributes._lodMinSize, _attributes._debugLODRendering, _attributes._textureWidth,
                _attributes._textureHeight, _attributes._renderBounds);
 
       clearCache();
 
       firePropertyChange("FillColor", oldValue, newValue);
    }
 
 
    public void setBorderColor(final Color newOpaqueColor) {
       final Color newValue = createColor(newOpaqueColor, _borderColorAlpha);
 
       if (_attributes._borderColor.equals(newValue)) {
          return;
       }
 
       final Color oldValue = _attributes._borderColor;
 
       _attributes = new GRenderingAttributes(_attributes._renderLODIgnores, _attributes._borderWidth, _attributes._fillColor,
                newValue, _attributes._lodMinSize, _attributes._debugLODRendering, _attributes._textureWidth,
                _attributes._textureHeight, _attributes._renderBounds);
 
       clearCache();
 
       firePropertyChange("BorderColor", oldValue, newValue);
    }
 
 
    public float getBorderWidth() {
       return _attributes._borderWidth;
    }
 
 
    public void setBorderWidth(final float newValue) {
       if (_attributes._borderWidth == newValue) {
          return;
       }
 
       final float oldValue = _attributes._borderWidth;
 
       _attributes = new GRenderingAttributes(_attributes._renderLODIgnores, newValue, _attributes._fillColor,
                _attributes._borderColor, _attributes._lodMinSize, _attributes._debugLODRendering, _attributes._textureWidth,
                _attributes._textureHeight, _attributes._renderBounds);
 
       clearCache();
 
       firePropertyChange("BorderWidth", oldValue, newValue);
    }
 
 
    private void clearCache() {
       //      _topTiles = null;
      if (_topTiles != null) {
         for (final Tile topTile : _topTiles) {
            topTile._surfaceImage = null;
         }
       }
 
       IMAGES_CACHE.clear(new LRUCache.ValuePredicate<GPolygon2DLayer.RenderingKey, Future<BufferedImage>, RuntimeException>() {
          @Override
          public boolean evaluate(final RenderingKey key,
                                  final Future<BufferedImage> value,
                                  final RuntimeException exception) {
             return (key._layer == GPolygon2DLayer.this);
          }
       });
 
       redraw();
    }
 
 
    @Override
    public void doDefaultAction(final IGlobeApplication application) {
       application.zoomToSector(getExtent());
    }
 
 
    @Override
    public List<ILayerAction> getLayerActions(final IGlobeApplication application) {
       return null;
    }
 
 
    private Box getBox(final DrawContext dc) {
       // return Sector.computeBoundingBox(globe, verticalExaggeration, _sector);
 
       return BOX_CACHE.get(dc, _polygonsBounds);
    }
 
 
    //   public void setShowExtents(final boolean showExtents) {
    //      _showExtents = showExtents;
    //      redraw();
    //   }
 
 
    //   public boolean isShowExtents() {
    //      return _showExtents;
    //   }
 
 
    //   private float computeProjectedPixels(final DrawContext dc) {
    //      final long now = System.currentTimeMillis();
    //
    //      // cache the result for TIMEOUT_FOR_CACHED_RESULTS ms
    //      if ((_lastComputedProjectedPixelsTime > 0) || ((now - _lastComputedProjectedPixelsTime) <= TIMEOUT_FOR_CACHED_RESULTS)) {
    //         return _lastComputedProjectedPixels;
    //      }
    //
    //      final Vec4 firstProjected = GWWUtils.getScreenPoint(dc, _sectorCorners[0]);
    //      double minX = firstProjected.x;
    //      double maxX = firstProjected.x;
    //      double minY = firstProjected.y;
    //      double maxY = firstProjected.y;
    //
    //      for (int i = 1; i < _sectorCorners.length; i++) {
    //         final Vec4 projected = GWWUtils.getScreenPoint(dc, _sectorCorners[i]);
    //
    //         if (projected.x < minX) {
    //            minX = projected.x;
    //         }
    //         if (projected.y < minY) {
    //            minY = projected.y;
    //         }
    //         if (projected.x > maxX) {
    //            maxX = projected.x;
    //         }
    //         if (projected.y > maxY) {
    //            maxY = projected.y;
    //         }
    //      }
    //
    //
    //      // calculate the area of the rectangle
    //      final double width = maxX - minX;
    //      final double height = maxY - minY;
    //      final double area = width * height;
    //      final float result = (float) area;
    //
    //      _lastComputedProjectedPixelsTime = now;
    //      _lastComputedProjectedPixels = result;
    //
    //      return result;
    //   }
 
 
    private void selectVisibleTiles(final DrawContext dc,
                                    final Tile tile,
                                    final Frustum frustum,
                                    final int currentLevel) {
       if (!tile._tileBounds.touches(_polygonsBounds)) {
          return;
       }
 
       if (!tile.getBox(dc).intersects(frustum)) {
          return;
       }
 
       final int maxLevel = 22;
       if ((currentLevel < maxLevel - 1) && tile.needToSplit(dc)) {
          final Tile[] children = tile.slit();
          for (final Tile child : children) {
             selectVisibleTiles(dc, child, frustum, currentLevel + 1);
          }
          return;
       }
       _currentTiles.add(tile);
    }
 
 
    private void calculateCurrentTiles(final DrawContext dc) {
       if (_topTiles == null) {
          final List<GAxisAlignedRectangle> topLevelSectors = createTopLevelSectors(_polygonsBounds);
 
          _topTiles = new ArrayList<Tile>(topLevelSectors.size());
          for (int i = 0; i < topLevelSectors.size(); i++) {
             final GAxisAlignedRectangle topLevelSector = topLevelSectors.get(i);
             _topTiles.add(new Tile(null, i, topLevelSector));
          }
       }
 
       final long now = System.currentTimeMillis();
 
       // cache the result for TIMEOUT_FOR_CACHED_RESULTS ms
       if ((_lastCurrentTilesCalculated > 0) && ((now - _lastCurrentTilesCalculated) <= TIMEOUT_FOR_CACHED_RESULTS)) {
          return;
       }
 
       final Frustum frustum = dc.getView().getFrustumInModelCoordinates();
       synchronized (_currentTiles) {
          _currentTiles.clear();
          for (final Tile tile : _topTiles) {
             selectVisibleTiles(dc, tile, frustum, 0);
          }
       }
 
       _lastCurrentTilesCalculated = now;
    }
 
 
    @Override
    public boolean isLayerInView(final DrawContext dc) {
       final Box extent = getBox(dc);
       final Frustum frustum = dc.getView().getFrustumInModelCoordinates();
       if (!extent.intersects(frustum)) {
          return false;
       }
 
       //      final boolean bigEnough = (computeProjectedPixels(dc) >= 25);
       //      return bigEnough;
       return true;
    }
 
 
    @Override
    protected final void doPreRender(final DrawContext dc) {
       _lastView = dc.getView();
 
       //      _lastVisibleSector = dc.getVisibleSector();
 
       calculateCurrentTiles(dc);
 
       for (final Tile tile : _currentTiles) {
          tile.preRender(dc);
       }
    }
 
 
    @Override
    protected final void doRender(final DrawContext dc) {
       // already done in isLayerInView();
       //      final boolean bigEnough = (computeProjectedPixels(dc) >= 25);
       //      if (!bigEnough) {
       //         return;
       //      }
 
       //      if (_showExtents) {
       //         renderExtents(dc);
       //      }
 
       for (final Tile tile : _currentTiles) {
          tile.render(dc);
       }
 
       if (_topTiles != null) {
          for (final Tile topTile : _topTiles) {
             topTile.moveUpInCache();
          }
       }
    }
 
 
    //   private void renderExtents(final DrawContext dc) {
    //      //      getBox(dc).render(dc);
    //
    //      final GL gl = dc.getGL();
    //      GWWUtils.pushOffset(gl);
    //
    //      for (final Tile tile : _currentTiles) {
    //         tile.renderExtent(dc);
    //      }
    //
    //      GWWUtils.popOffset(gl);
    //   }
 
 
    public boolean isDebugRendering() {
       return _debugRendering;
    }
 
 
    public void setDebugRendering(final boolean newValue) {
       if (newValue == _debugRendering) {
          return;
       }
 
       _debugRendering = newValue;
 
       _attributes = new GRenderingAttributes(_attributes._renderLODIgnores, _attributes._borderWidth, _attributes._fillColor,
                _attributes._borderColor, _attributes._lodMinSize, newValue, _attributes._textureWidth,
                _attributes._textureHeight, newValue);
 
       clearCache();
 
       firePropertyChange("DebugRendering", !newValue, newValue);
    }
 
 
    private static GAxisAlignedRectangle[] createWordQuadrants() {
       final GAxisAlignedRectangle wholeWorld = new GAxisAlignedRectangle(new GVector2D(-Math.PI, -Math.PI / 2), new GVector2D(
                Math.PI, Math.PI / 2));
 
 
       final GAxisAlignedRectangle[] hemispheres = wholeWorld.subdividedByY();
       final GAxisAlignedRectangle south = hemispheres[0];
       final GAxisAlignedRectangle north = hemispheres[1];
 
 
       final GAxisAlignedRectangle[] northDivisions = north.subdividedByX();
       final GAxisAlignedRectangle northWest = northDivisions[0];
       final GAxisAlignedRectangle northEast = northDivisions[1];
 
 
       final GAxisAlignedRectangle[] southDivisions = south.subdividedByX();
       final GAxisAlignedRectangle southWest = southDivisions[0];
       final GAxisAlignedRectangle southEast = southDivisions[1];
 
 
       return new GAxisAlignedRectangle[] { northWest, northEast, southWest, southEast };
    }
 
 
    private static List<GAxisAlignedRectangle> createTopLevelSectors() {
       final GAxisAlignedRectangle[] wordQuadrants = createWordQuadrants();
 
       final GAxisAlignedRectangle[] northWestDivisions = wordQuadrants[0].subdividedByX();
       final GAxisAlignedRectangle[] northEastDivisions = wordQuadrants[1].subdividedByX();
       final GAxisAlignedRectangle[] southWestDivisions = wordQuadrants[2].subdividedByX();
       final GAxisAlignedRectangle[] southEastDivisions = wordQuadrants[3].subdividedByX();
 
       return Arrays.asList( //
                northWestDivisions[0], northWestDivisions[1], //
                northEastDivisions[0], northEastDivisions[1], //
                southWestDivisions[0], southWestDivisions[1], //
                southEastDivisions[0], southEastDivisions[1]);
    }
 
 }
