 /* gvSIG Mini. A free mobile phone viewer of free maps.
  *
  * Copyright (C) 2009 Prodevelop.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *   Prodevelop, S.L.
  *   Pza. Don Juan de Villarrasa, 14 - 5
  *   46001 Valencia
  *   Spain
  *
  *   +34 963 510 612
  *   +34 963 510 968
  *   prode@prodevelop.es
  *   http://www.prodevelop.es
  *
  *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Peque�a y
  *   Mediana Empresa de la Comunidad Valenciana) &
  *   European Union FEDER funds.
  *   
  *   2009.
  *   author Rub�n Blanco rblanco@prodevelop.es
  *
  *
  * Original version of the code made by Nicolas Gramlich.
  * No header license code found on the original source file.
  * 
  * Original source code downloaded from http://code.google.com/p/osmdroid/
  * package org.andnav.osm.views.overlay;
  * package org.andnav.osm.views;
  * 
  * License stated in that website is 
  * http://www.gnu.org/licenses/lgpl.html
  * As no specific LGPL version was specified, LGPL license version is LGPL v2.1
  * is assumed.
  *  
  * 
  */
 
 package es.prodevelop.gvsig.mini.views.overlay;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnDoubleTapListener;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Interpolator;
 import android.view.animation.LinearInterpolator;
 import android.widget.Scroller;
 import android.widget.Toast;
 import es.prodevelop.android.spatialindex.poi.POICategories;
 import es.prodevelop.android.spatialindex.quadtree.provide.BookmarkProviderListener;
 import es.prodevelop.android.spatialindex.quadtree.provide.FullTextSearchListener;
 import es.prodevelop.android.spatialindex.quadtree.provide.perst.PerstBookmarkProvider;
 import es.prodevelop.android.spatialindex.quadtree.provide.perst.PerstOsmPOIClusterProvider;
 import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
 import es.prodevelop.gvsig.mini.R;
 import es.prodevelop.gvsig.mini.activities.Map;
 import es.prodevelop.gvsig.mini.activities.OSSettingsUpdater;
 import es.prodevelop.gvsig.mini.activities.Settings;
 import es.prodevelop.gvsig.mini.common.CompatManager;
 import es.prodevelop.gvsig.mini.common.IBitmap;
 import es.prodevelop.gvsig.mini.common.IContext;
 import es.prodevelop.gvsig.mini.common.android.BitmapAndroid;
 import es.prodevelop.gvsig.mini.common.android.HandlerAndroid;
 import es.prodevelop.gvsig.mini.common.impl.Tile;
 import es.prodevelop.gvsig.mini.exceptions.BaseException;
 import es.prodevelop.gvsig.mini.geom.Extent;
 import es.prodevelop.gvsig.mini.geom.Feature;
 import es.prodevelop.gvsig.mini.geom.Pixel;
 import es.prodevelop.gvsig.mini.geom.Point;
 import es.prodevelop.gvsig.mini.geom.android.GPSPoint;
 import es.prodevelop.gvsig.mini.map.ExtentChangedListener;
 import es.prodevelop.gvsig.mini.map.GeoUtils;
 import es.prodevelop.gvsig.mini.map.LayerChangedListener;
 import es.prodevelop.gvsig.mini.map.LoadCallbackHandler;
 import es.prodevelop.gvsig.mini.map.MultiTouchController;
 import es.prodevelop.gvsig.mini.map.MultiTouchController.MultiTouchObjectCanvas;
 import es.prodevelop.gvsig.mini.map.MultiTouchController.PointInfo;
 import es.prodevelop.gvsig.mini.map.MultiTouchController.PositionAndScale;
 import es.prodevelop.gvsig.mini.map.ViewPort;
 import es.prodevelop.gvsig.mini.namefinder.NamedMultiPoint;
 import es.prodevelop.gvsig.mini.projection.TileConversor;
 import es.prodevelop.gvsig.mini.search.POIProviderChangedListener;
 import es.prodevelop.gvsig.mini.search.POIProviderManager;
 import es.prodevelop.gvsig.mini.util.ResourceLoader;
 import es.prodevelop.gvsig.mini.util.Utils;
 import es.prodevelop.gvsig.mini.utiles.Cancellable;
 import es.prodevelop.gvsig.mini.utiles.WorkQueue;
 import es.prodevelop.gvsig.mini.yours.Route;
 import es.prodevelop.gvsig.mini.yours.RouteManager;
 import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
 import es.prodevelop.tilecache.layers.Layers;
 import es.prodevelop.tilecache.provider.Downloader;
 import es.prodevelop.tilecache.provider.TileProvider;
 import es.prodevelop.tilecache.provider.android.AndroidTileProvider;
 import es.prodevelop.tilecache.provider.filesystem.impl.TileFilesystemProvider;
 import es.prodevelop.tilecache.provider.filesystem.strategy.ITileFileSystemStrategy;
 import es.prodevelop.tilecache.provider.filesystem.strategy.impl.FileSystemStrategyManager;
 import es.prodevelop.tilecache.renderer.MapRenderer;
 import es.prodevelop.tilecache.renderer.OSMMercatorRenderer;
 import es.prodevelop.tilecache.renderer.wms.OSRenderer;
 import es.prodevelop.tilecache.renderer.wms.WMSRenderer;
 import es.prodevelop.tilecache.util.Tags;
 import es.prodevelop.tilecache.util.Utilities;
 
 /**
  * The main view to show tiles. This view is contained in a RelativeLayout in
  * the Map Activity.
  * 
  * This class contains a MapRenderer that knows how to convert the current pixel
  * coordinates to coordinates and get the URL string of the tile server
  * 
  * The TileProvider manages the logic of download/load from server/filesystem
  * the tiles
  * 
  * @author aromeu
  * @author rblanco
  * 
  */
 public class TileRaster extends SurfaceView implements GeoUtils,
 		OnClickListener, OnLongClickListener, LayerChangedListener,
 		MultiTouchObjectCanvas<Object>, SurfaceHolder.Callback {
 
 	private ArrayList extentChangedListeners = new ArrayList();
 
 	private POIProviderChangedListener poiProviderFailListener;
 
 	public void setPoiProviderFailListener(
 			POIProviderChangedListener poiProviderFailListener) {
 		this.poiProviderFailListener = poiProviderFailListener;
 	}
 
 	Point scrollingCenter;
 	private boolean isScrolling = false;
 	private boolean invalidateLongPress = false;
 	private static final int UPDATE_ZOOM_CONTROLS = 147;
 	private static final int ANIMATION_FINISHED_NEED_ZOOM = 148;
 	private TileRasterThread surfaceThread;
 
 	Cancellable cancellable = Utilities.getNewCancellable();
 	public AcetateOverlay acetate;
 	boolean panMode = true;
 
 	private float xOff = 0.0f, yOff = 0.0f, relativeScale = 1.0f;
 
 	private PointInfo currTouchPoint;
 
 	private MultiTouchController<Object> multiTouchController;
 
 	private boolean zoomflag = false;
 	public int mBearing = 0;
 
 	private MapRenderer mRendererInfo;
 	private final static Logger log = Logger.getLogger(TileRaster.class
 			.getName());
 
 	public MapRenderer getMRendererInfo() {
 		return mRendererInfo;
 	}
 
 	protected TileProvider mTileProvider;
 
 	protected final GestureDetector mGestureDetector = new GestureDetector(
 			new OpenStreetMapViewGestureDetectorListener());
 
 	protected final List<MapOverlay> mOverlays = new ArrayList<MapOverlay>();
 
 	private int previousRotation = 999;
 	public int pixelX;
 	public int pixelY;
 	int mTouchDownX;
 	int mTouchDownY;
 	public static int mTouchMapOffsetX;
 	public static int mTouchMapOffsetY;
 	public static int lastTouchMapOffsetX;
 	public static int lastTouchMapOffsetY;
 	String datalog = null;
 	Map map;
 	int centerPixelX = 0;
 	int centerPixelY = 0;
 	AndroidGeometryDrawer geomDrawer;
 	public static int mapWidth = 0;
 	public static int mapHeight = 0;
 	public static boolean CLEAR_ROUTE = false;
 	public static boolean CLEAR_OFF_POIS = false;
 	private IContext androidContext;
 	private MotionEvent lastTouchEvent;
 	protected Feature selectedFeature = null;
 
 	Canvas bufferCanvas = new Canvas();
 	Bitmap bufferBitmap;
 
 	private Canvas zoomCanvas = new Canvas();
 	private Bitmap zoomBitmap;
 
 	Scaler mScaler;
 
 	private boolean scale = false;
 
 	private SurfaceHolder holder;
 	private boolean hasSurface;
 	private SimpleInvalidationHandler simpleInvalidationHandler = new SimpleInvalidationHandler();
 	private Scroller mScroller;
 
 	/**
 	 * The Constructor.
 	 * 
 	 * @param context
 	 *            The context (Usually the Map activity)
 	 * @param aRendererInfo
 	 *            A MapRenderer
 	 * @param width
 	 *            The width of the view in pixels
 	 * @param height
 	 *            The height of the view in pixels
 	 */
 	public TileRaster(final Context context, final IContext androidContext,
 			final MapRenderer aRendererInfo, int width, int height) {
 		super(context);
 		try {
 			CompatManager.getInstance().getRegisteredLogHandler()
 					.configureLogger(log);
 		} catch (BaseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			init();
 			this.mScroller = new Scroller(context);
 			this.androidContext = androidContext;
 			this.map = (Map) context;
 			multiTouchController = new MultiTouchController<Object>(this,
 					getResources(), false);
 			log.setLevel(Utils.LOG_LEVEL);
 			// log.setClientID(this.toString());
 			this.mScaler = new Scaler(context, new LinearInterpolator());
 			this.instantiateTileProviderfromSettings();
 			this.setRenderer(aRendererInfo);
 			geomDrawer = new AndroidGeometryDrawer(this, context);
 			acetate = new AcetateOverlay(context, this,
 					AcetateOverlay.DEFAULT_NAME);
 			this.mCurrentAnimationRunner = new LinearAnimationRunner(0, 0,
 					false, false);
 			this.setFocusable(true);
 			this.setClickable(true);
 			this.setLongClickable(true);
 			this.setOnClickListener(this);
 			this.setOnLongClickListener(this);
 			this.initializeCanvas(width, height);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onCreate:", e);
 		} catch (OutOfMemoryError e) {
 			System.gc();
 			log.log(Level.SEVERE, "onCreate:", e);
 		}
 	}
 
 	public boolean containsOverlay(String name) {
 		final List<MapOverlay> overlays = this.mOverlays;
 
 		final int size = overlays.size();
 
 		MapOverlay overlay;
 		for (int i = 0; i < size; i++) {
 			overlay = overlays.get(i);
 			if (overlay.getName().compareTo(name) == 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void addOverlay(MapOverlay overlay) {
 		if (containsOverlay(overlay.getName())) {
 			overlay.onLayerChanged("");
 		} else {
 			if (overlay instanceof ExpandedClusterOverlay) {
 				this.mOverlays.add(0, overlay);
 			} else {
 				this.mOverlays.add(overlay);
 			}
 
 			extentChangedListeners.add(overlay);
 		}
 	}
 
 	public void removeOverlay(String name) {
 		final List<MapOverlay> overlays = this.mOverlays;
 
 		List<MapOverlay> copyOverlays = new ArrayList();
 
 		int size = overlays.size();
 
 		for (int i = 0; i < size; i++) {
 			copyOverlays.add(mOverlays.get(i));
 		}
 
 		Iterator<MapOverlay> it = copyOverlays.iterator();
 
 		MapOverlay overlay;
 
 		while (it.hasNext()) {
 			try {
 				overlay = it.next();
 				if (overlay.getName().compareTo(name) == 0) {
 					if (overlays.remove(overlay))
 						Log.d("", "Overlay removed: " + overlay.getName());
 
 					extentChangedListeners.remove(overlay);
 					overlay.destroy();
 					overlay = null;
 					acetate.setPopupVisibility(View.INVISIBLE);
 					break;
 				}
 			} catch (Exception e) {
 				Log.e("", "Error on RemoveOverlay: " + name);
 			}
 		}
 
 		// copyOverlays = null;
 	}
 
 	public MapOverlay getOverlay(String name) {
 		final List<MapOverlay> overlays = this.mOverlays;
 
 		final int size = overlays.size();
 
 		MapOverlay overlay;
 		for (int i = 0; i < size; i++) {
 			overlay = overlays.get(i);
 			if (overlay.getName().compareTo(name) == 0) {
 				return overlay;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Sets the center of the Map from a GPSPoint and postinvalidates the view
 	 * 
 	 * @param aCenter
 	 *            A GPSPoint
 	 */
 	public void setMapCenter(final GPSPoint aCenter) {
 		try {
 			this.setMapCenterFromLonLat(aCenter.getLongitudeE6() / 1E6,
 					aCenter.getLatitudeE6() / 1E6);
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "setMapCenter:", e);
 		}
 	}
 
 	/**
 	 * Checks if the max extent of the MapRenderer contains the xy, sets the
 	 * center and postinvalidates the view. The coordinates x,y need to be in
 	 * the same SRS as the MapRenderer
 	 * 
 	 * @param x
 	 *            the x coordinate
 	 * @param y
 	 *            the y coordinate
 	 */
 	public void setMapCenter(final double x, final double y) {
 		try {
 			final MapRenderer renderer = this.getMRendererInfo();
 			if (renderer.getExtent().contains(x, y)) {
 				renderer.setCenter(x, y);
 				this.notifyExtentChangedListeners(renderer.getCurrentExtent(),
 						this.getZoomLevel(), renderer.getCurrentRes());
 				resumeDraw();
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "setMapCenter:", e);
 		}
 	}
 
 	public void notifyExtentChangedListeners(Extent extent, int zoomLevel,
 			double resolution) {
 		final ArrayList list = this.extentChangedListeners;
 		final int length = list.size();
 		for (int i = 0; i < length; i++) {
 			((ExtentChangedListener) list.get(i)).onExtentChanged(extent,
 					zoomLevel, resolution);
 		}
 	}
 
 	/**
 	 * Sets the current MapRenderer of the view and centers the view on the
 	 * center of the max extent of the MapRenderer
 	 * 
 	 * @param aRenderer
 	 *            A MapRenderer
 	 */
 	public void setRenderer(final MapRenderer aRenderer) {
 		try {
 			log.log(Level.FINE, "set MapRenderer: " + aRenderer.toString());
 			this.mRendererInfo = aRenderer;
 			this.map.vp = new ViewPort();
 			this.mRendererInfo.setCenter(aRenderer.getExtent().getCenter()
 					.getX(), aRenderer.getExtent().getCenter().getY());
 			this.map.vp.resolutions = aRenderer.resolutions;
 			this.map.vp.setDist1Pixel(this.map.vp.resolutions[0]);
 			this.map.vp.origin = new es.prodevelop.gvsig.mini.geom.Point(
 					aRenderer.getOriginX(), aRenderer.getOriginY());
 			return;
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "setRenderer:", e);
 		}
 	}
 
 	/**
 	 * Sets a zoom level that fits a resolution. This method is useful for
 	 * zoomRectangle, zooming to a route, to a multipoint, etc.
 	 * 
 	 * @param resolution
 	 *            The resolution in the units of the projection of the
 	 *            MapRenderer
 	 * @see CRSFactory
 	 */
 	public void setZoomLevelFromResolution(double resolution) {
 		try {
 			final double[] resolutions = this.getMRendererInfo().resolutions;
 			final int size = resolutions.length;
 
 			double current = 0;
 			double moreAccurate = -1;
 			int zoomLevel = -1;
 			for (int i = 0; i < size; i++) {
 				current = resolutions[i];
 				double diff = current - resolution;
 				if (diff > 0) {
 					double moreAccurateDiff = current - moreAccurate;
 					if (diff < moreAccurateDiff) {
 						moreAccurateDiff = diff;
 						zoomLevel = i;
 					}
 				}
 			}
 			if (zoomLevel != -1) {
 				this.setZoomLevel(zoomLevel);
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "setZoomLevelFromResolution", e);
 		}
 	}
 
 	/**
 	 * Sets the zoom level, updates the zoom controls and postInvalidates the
 	 * view
 	 * 
 	 * @param aZoomLevel
 	 *            The zoom level
 	 */
 	public void setZoomLevel(final int aZoomLevel) {
 		final MapRenderer renderer = this.mRendererInfo;
 		int zoomLevel = aZoomLevel;
 		if (aZoomLevel < renderer.getZoomMinLevel()) {
 			zoomLevel = renderer.getZoomMinLevel();
 		} else if (aZoomLevel > renderer.getZOOM_MAXLEVEL()) {
 			zoomLevel = renderer.getZOOM_MAXLEVEL();
 		}
 
 		tempZoomLevel = zoomLevel;
 		// Extent viewExtent =
 		// this.map.vp.calculateExtent(this.getMRendererInfo()
 		// .getCenter(), this.getMRendererInfo().resolutions[zoomLevel],
 		// mapWidth, mapHeight);
 		// Extent maxExtent = this.getMRendererInfo().getExtent();
 		//
 		// double viewExtentWidth = viewExtent.getWidth();
 		// double viewExtentHeight = viewExtent.getHeight();
 		//
 		// double maxWidth = maxExtent.getWidth();
 		// double maxHeight = maxExtent.getHeight();
 		// if (maxWidth < viewExtentWidth || maxHeight < viewExtentHeight) {
 		// if (zoomLevel != this.getMRendererInfo().getZOOM_MAXLEVEL()) {
 		// if (zoomLevel != this.getMRendererInfo().getZoomLevel() - 1) {
 		// this.setZoomLevel(zoomLevel + 1);
 		// map.z.setIsZoomOutEnabled(false);
 		// } else {
 		// map.z.setIsZoomOutEnabled(false);
 		// }
 		// return;
 		// }
 		// }
 
 		// if (!maxExtent.contains(viewExtent)) {
 		// this.zoomIn();
 		// } else {
 
 		try {
 			renderer.setZoomLevel(zoomLevel);
 			this.notifyExtentChangedListeners(renderer.getCurrentExtent(),
 					zoomLevel, renderer.getCurrentRes());
 			map.vp.setDist1Pixel(map.vp.resolutions[zoomLevel]);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "setZoomLevel:", e);
 
 		}
 		scale = true;
 		// refresh = false;
 		// }
 
 		this.simpleInvalidationHandler
 				.sendEmptyMessage(TileRaster.UPDATE_ZOOM_CONTROLS);
 
 		// try {
 		// // t.cancel();
 		// // t.purge();
 		// } catch (Exception e) {
 		//
 		// } finally {
 		// try {
 		// ZoomRefreshTask z = new ZoomRefreshTask();
 		// Timer t = new Timer();
 		// t.schedule(z, 5000);
 		// } catch (Exception e) {
 		//
 		// }
 		// }
 	}
 
 	/**
 	 * Sets the zoom level and applies a LinearInterpolation Scaler to the view
 	 * 
 	 * @param aZoomLevel
 	 *            The zoom level
 	 * @param several
 	 *            If changes several zooms level applies the Scaler
 	 */
 	public void setZoomLevel(final int aZoomLevel, boolean several) {
 		try {
 			int levels = aZoomLevel - this.getMRendererInfo().getZoomLevel();
 			tempZoomLevel = aZoomLevel;
 			if (several) {
 				// Zoom in
 				if (levels > 0) {
 					if (mScaler.isFinished()) {
 						mScaler.startScale(1.0f, (float) (levels * 2.0f),
 								Scaler.DURATION_SHORT);
 						postInvalidate();
 					} else {
 						mScaler.extendDuration(Scaler.DURATION_SHORT);
 						mScaler.setFinalScale(mScaler.getFinalScale() * 2.0f);
 					}
 				} else {
 					levels = Math.abs(1 / levels);
 					if (mScaler.isFinished()) {
 						mScaler.startScale(1.0f, (float) (levels / 2.0f),
 								Scaler.DURATION_SHORT);
 						postInvalidate();
 					} else {
 						mScaler.extendDuration(Scaler.DURATION_SHORT);
 						mScaler.setFinalScale(mScaler.getFinalScale() * 0.5f);
 					}
 				}
 			} else {
 				this.setZoomLevel(aZoomLevel);
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "setZoomLevel", e);
 		}
 	}
 
 	void onScalingFinished() {
 		// frontCanvas.drawBitmap(bufferBitmap, 0, 0, normalPaint);
 		setZoomLevel(tempZoomLevel);
 	}
 
 	private void computeScale() {
 		if (mScaler.computeScale()) {
 			if (mScaler.isFinished())
 				onScalingFinished();
 			else
 				resumeDraw();
 		}
 	}
 
 	private int tempZoomLevel = 0;
 
 	/**
 	 * 
 	 * @return The final zoom level after the Scale interpolation ends
 	 */
 	public int getTempZoomLevel() {
 		return tempZoomLevel;
 	}
 
 	/**
 	 * Increase the zoom of the view and applies the Scaler
 	 */
 	public void zoomIn() {
 		try {
 			this.mTileProvider.clearPendingQueue();
 			tempZoomLevel += 1;
 			// this.setZoomLevel(this.getMRendererInfo().getZoomLevel() + 1);
 
 			if (mScaler.isFinished()) {
 				mScaler.startScale(1.0f, 2.0f, Scaler.DURATION_SHORT);
 				postInvalidate();
 			} else {
 				mScaler.extendDuration(Scaler.DURATION_SHORT);
 				mScaler.setFinalScale(mScaler.getFinalScale() * 2.0f);
 			}
 
 			// Matrix m = new Matrix();
 			// m.postScale(2, 2);
 			// Bitmap resizedBitmap = Bitmap.createBitmap(bufferBitmap, 0, 0,
 			// this.getWidth(), this.getHeight(), m, true);
 			// bufferBitmap = resizedBitmap;
 			//
 			// zoomed = true;
 			// mapGraphics.drawImage(zoomImage.scaled(480, 640), -120, -160);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "zoomIn:", e);
 		}
 	}
 
 	/**
 	 * Decreases the zoom level of the view applying a Scaler
 	 */
 	public void zoomOut() {
 		try {
 			this.mTileProvider.clearPendingQueue();
 			if (this.getMRendererInfo().getZoomLevel() != 0) {
 				this.tempZoomLevel -= 1;
 				// this.setZoomLevel(this.getMRendererInfo().getZoomLevel() -
 				// 1);
 				if (mScaler.isFinished()) {
 					mScaler.startScale(1.0f, 0.5f, Scaler.DURATION_SHORT);
 					postInvalidate();
 				} else {
 					mScaler.extendDuration(Scaler.DURATION_SHORT);
 					mScaler.setFinalScale(mScaler.getFinalScale() * 0.5f);
 				}
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "zoomOut:", e);
 		}
 	}
 
 	/**
 	 * 
 	 * @return The current zoom level of the view
 	 */
 	public int getZoomLevel() {
 		return this.getMRendererInfo().getZoomLevel();
 	}
 
 	public int i = 0;
 
 	public boolean onLongPress(MotionEvent e) {
 
 		if (!panMode || map.navigation
 				|| (currTouchPoint != null && currTouchPoint.isDown())
 				|| invalidateLongPress) {
 			log.log(Level.FINE,
 					"longpress on pan mode or navigation does not work");
 			return false;
 		}
 
 		try {
 			if (TileRaster.this.mTouchMapOffsetX < ResourceLoader.MIN_PAN
 					&& TileRaster.this.mTouchMapOffsetY < ResourceLoader.MIN_PAN
 					&& TileRaster.this.mTouchMapOffsetX > -ResourceLoader.MIN_PAN
 					&& TileRaster.this.mTouchMapOffsetY > -ResourceLoader.MIN_PAN) {
 
 				double[] coords = TileRaster.this.getMRendererInfo()
 						.fromPixels(
 								new int[] { (int) e.getX(), (int) e.getY() });
 
 				for (MapOverlay osmvo : this.mOverlays)
 					if (osmvo.onLongPress(e, this)) {
 						map.setOverlayContext(osmvo.getItemContext());
 						map.showOverlayContext();
 						this.animateTo(coords[0], coords[1]);
 						return true;
 					}
 
 				this.animateTo(coords[0], coords[1]);
 				map.showContext(map.getItemContext());
 
 				pixelX = (int) e.getX();
 				pixelY = (int) e.getY();
 			}
 		} catch (Exception ex) {
 			log.log(Level.SEVERE, "onLongPress:", ex);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		try {
 			for (MapOverlay osmvo : this.mOverlays)
 				if (osmvo.onKeyDown(keyCode, event, this))
 					return true;
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onkeydown:", e);
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		try {
 			for (MapOverlay osmvo : this.mOverlays)
 				if (osmvo.onKeyUp(keyCode, event, this))
 					return true;
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onkeyUp:", e);
 		}
 
 		return super.onKeyUp(keyCode, event);
 	}
 
 	@Override
 	public boolean onTrackballEvent(MotionEvent event) {
 		try {
 			int x = (int) Math.ceil(event.getX() * 10);
 			int y = (int) Math.ceil(event.getY() * 10);
 			double[] coords = TileRaster.this.getMRendererInfo()
 					.fromPixels(
 							new int[] { (int) centerPixelX + x,
 									(int) centerPixelY + y });
 			System.out.println("x, y: " + coords[0] + ", " + coords[1]);
 			this.setMapCenter(coords[0], coords[1]);
 			for (MapOverlay osmvo : this.mOverlays)
 				osmvo.onTrackballEvent(event, this);
 			// return true;
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onTrackBallEvent", e);
 		}
 		return super.onTrackballEvent(event);
 	}
 
 	@Override
 	public boolean onTouchEvent(final MotionEvent event) {
 		try {
 			final int size = this.mOverlays.size();
 			int i = 0;
 			MapOverlay overlay;
 
 			resumeDraw();
 			if (map.isPOISlideShown())
 				return true;
 			// System.out.println("onTouchEvent");
 			// Log.d("", event.getX() + ", " + event.getY());
 			this.lastTouchEvent = MotionEvent.obtain(event);
 			if (event.getAction() == MotionEvent.ACTION_UP) {
 				lastTouchEventProcessed = false;
 			}
 			if (!map.navigation) {
 				for (i = 0; i < size; i++) {
 					overlay = this.mOverlays.get(i);
 					overlay.onTouchEvent(event, this);
 				}
 				// return true;
 
 				this.mGestureDetector.onTouchEvent(event);
 
 				if (!multiTouchController.onTouchEvent(event)) {
 					// System.out.println("onTouchEvent acetate");
 					// if (acetate.isFirstTouch()) {
 					// System.out.println("first touch");
 					// acetate.onTouchEvent(event);
 					// } else
 					// synchronized (holder) {
 					// System.out.println("not first touch");
 					acetate.onTouchEvent(event);
 					// }
 				}
 			}
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onTouchEvent:", e);
 		}
 
 		return super.onTouchEvent(event);
 
 	}
 
 	protected void processMultiTouchEvent() {
 		if (lastTouchEvent != null
 				&& this.lastTouchEvent.getAction() == MotionEvent.ACTION_UP
 				&& this.currTouchPoint != null && !lastTouchEventProcessed) {
 			int multiTouchZoom = this.getZoomLevel();
 			double scale = this.mScaler.mCurrScale;
 			if (scale > 1) {
 				multiTouchZoom += Math.ceil(scale - 1);
 			} else {
 				scale *= 0.1;
 				double decimals = scale - Math.ceil(scale);
 				if (decimals > 0.5)
 					multiTouchZoom -= Math.floor(scale);
 				else
 					multiTouchZoom -= Math.ceil(scale);
 
 			}
 
 			this.setZoomLevel(multiTouchZoom);
 			mScaler.forceFinished(true);
 			this.currTouchPoint = null;
 			this.lastTouchEventProcessed = true;
 		}
 	}
 
 	private boolean lastTouchEventProcessed = false;
 
 	@Override
 	public void onDraw(final Canvas c) {
 
 		try {
 			// System.out.println(getScrollX() +", " + getScrollY());
 			ViewPort.mapHeight = mapHeight;
 			ViewPort.mapWidth = mapWidth;
 			final MapRenderer renderer = this.getMRendererInfo();
 			boolean someTileNull = false;
 			boolean areAllValid = true;
 
 			final double originX = renderer.getOriginX();
 			final double originY = renderer.getOriginY();
 
 			// c.drawColor(0, PorterDuff.Mode.CLEAR);
 			if (!mScaler.isFinished()
 					|| (this.currTouchPoint != null && this.currTouchPoint
 							.isDown())) {
 				c.drawRect(0, 0, mapWidth, mapHeight, Paints.whitePaint);
 				Matrix m = c.getMatrix();
 				// Log.d("", "Scale: " + mScaler.mCurrScale);
 				m.preScale(mScaler.mCurrScale, mScaler.mCurrScale,
 						mapWidth / 2, mapHeight / 2);
 
 				c.setMatrix(m);
 				c.drawBitmap(bufferBitmap, ViewPort.mTouchMapOffsetX,
 						ViewPort.mTouchMapOffsetY, Paints.normalPaint);
 				// System.out.println("scalling");
 				// return;
 			} else {
 
 				processMultiTouchEvent();
 
 				if (map.navigation) {
 					// TileRaster.this.rotatePaint.setAntiAlias(true);
 					Paints.normalPaint = Paints.rotatePaint;
 					int rotationToDraw = -mBearing
 							- map.getmMyLocationOverlay()
 									.getOffsetOrientation();
 					if (Math.abs(Math.abs(rotationToDraw)
 							- Math.abs(previousRotation)) < Utils.MIN_ROTATION) {
 						rotationToDraw = previousRotation;
 					} else {
 						previousRotation = rotationToDraw;
 					}
 					c.rotate(rotationToDraw, mapWidth / 2, mapHeight / 2);
 				} else {
 					Paints.normalPaint = Paints.mPaintR;
 				}
 
 				final long startMs = System.currentTimeMillis();
 
 				final int zoomLevel = renderer.getZoomLevel();
 				final double resolution = renderer.resolutions[zoomLevel];
 				final int viewWidth = this.mapWidth;
 				final int viewHeight = this.mapHeight;
 				// this.centerPixelX = this.getWidth() / 2;
 				// this.centerPixelY = this.getHeight() / 2;
 				final int tileSizePx = renderer.getMAPTILE_SIZEPX();
 
 				int[] centerMapTileCoords = renderer.getMapTileFromCenter();
 
 				final int additionalTilesNeededToLeftOfCenter;
 				final int additionalTilesNeededToRightOfCenter;
 				final int additionalTilesNeededToTopOfCenter;
 				final int additionalTilesNeededToBottomOfCenter;
 
 				Pixel upperLeftCornerOfCenterMapTile = renderer
 						.getUpperLeftCornerOfCenterMapTileInScreen(null);
 
 				final int centerMapTileScreenLeft = upperLeftCornerOfCenterMapTile
 						.getX();
 				final int centerMapTileScreenTop = upperLeftCornerOfCenterMapTile
 						.getY();
 
 				final int centerMapTileScreenRight = centerMapTileScreenLeft
 						+ tileSizePx;
 				final int centerMapTileScreenBottom = centerMapTileScreenTop
 						+ tileSizePx;
 				if (!map.navigation) {
 
 					additionalTilesNeededToLeftOfCenter = (int) Math
 							.ceil((float) centerMapTileScreenLeft / tileSizePx);
 					additionalTilesNeededToRightOfCenter = (int) Math
 							.ceil((float) (viewWidth - centerMapTileScreenRight)
 									/ tileSizePx);
 					additionalTilesNeededToTopOfCenter = (int) Math
 							.ceil((float) centerMapTileScreenTop / tileSizePx);
 					additionalTilesNeededToBottomOfCenter = (int) Math
 							.ceil((float) (viewHeight - centerMapTileScreenBottom)
 									/ tileSizePx);
 				} else {
 
 					additionalTilesNeededToLeftOfCenter = (int) Math
 							.ceil((float) (viewWidth + centerMapTileScreenLeft)
 									/ tileSizePx);
 					additionalTilesNeededToRightOfCenter = (int) Math
 							.ceil((float) (Utils.ROTATE_BUFFER_SIZE * viewWidth - centerMapTileScreenRight)
 									/ tileSizePx);
 					additionalTilesNeededToTopOfCenter = (int) Math
 							.ceil((float) (viewHeight + centerMapTileScreenTop)
 									/ tileSizePx);
 					additionalTilesNeededToBottomOfCenter = (int) Math
 							.ceil((float) (Utils.ROTATE_BUFFER_SIZE
 									* viewHeight - centerMapTileScreenBottom)
 									/ tileSizePx);
 
 				}
 
 				final int[] mapTileCoords = new int[] {
 						centerMapTileCoords[MAPTILE_LATITUDE_INDEX],
 						centerMapTileCoords[MAPTILE_LONGITUDE_INDEX] };
 
 				final int size = (additionalTilesNeededToBottomOfCenter
 						+ additionalTilesNeededToTopOfCenter + 1)
 						* (additionalTilesNeededToRightOfCenter
 								+ additionalTilesNeededToLeftOfCenter + 1);
 				Tile[] tiles = new Tile[size];
 				int cont = 0;
 
 				final Extent maxExtent = renderer.getExtent();
 				final Extent viewExtent = map.vp.calculateExtent(mapWidth,
 						mapHeight, renderer.getCenter());
 				// this.getMTileProvider().setViewExtent(viewExtent);
 
 				final String layerName = this.getMRendererInfo().getNAME();
 
 				boolean process = true;
 
 				for (int y = -additionalTilesNeededToTopOfCenter; y <= additionalTilesNeededToBottomOfCenter; y++) {
 					for (int x = -additionalTilesNeededToLeftOfCenter; x <= additionalTilesNeededToRightOfCenter; x++) {
 						process = true;
 						if (viewExtent.intersect(maxExtent)) {
 							final int tileLeft = this.mTouchMapOffsetX
 									+ centerMapTileScreenLeft
 									+ (x * tileSizePx);
 							final int tileTop = this.mTouchMapOffsetY
 									+ centerMapTileScreenTop + (y * tileSizePx);
 
 							double[] coords = new double[] {
 									maxExtent.getLefBottomCoordinate().getX(),
 									maxExtent.getLefBottomCoordinate().getY() };
 
 							final int[] leftBottom = renderer.toPixels(coords);
 							coords = new double[] {
 									maxExtent.getRightTopCoordinate().getX(),
 									maxExtent.getRightTopCoordinate().getY() };
 							final int[] rightTop = renderer.toPixels(coords);
 
 							final Rect r = new Rect();
 							r.bottom = leftBottom[1];
 							r.left = leftBottom[0];
 							r.right = rightTop[0];
 							r.top = rightTop[1];
 
 							process = r.intersects(tileLeft, tileTop, tileLeft
 									+ tileSizePx / 2, tileTop + tileSizePx / 2);
 
 						}
 
 						if (process) {
 							mapTileCoords[MAPTILE_LATITUDE_INDEX] = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
 									+ this.mRendererInfo.isTMS() * y;
 
 							mapTileCoords[MAPTILE_LONGITUDE_INDEX] = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
 									+ x;
 
 							final String tileURLString = this.mRendererInfo
 									.getTileURLString(mapTileCoords, zoomLevel);
 
 							final int[] tile = new int[] { mapTileCoords[0],
 									mapTileCoords[1] };
 							final int tileLeft = this.mTouchMapOffsetX
 									+ centerMapTileScreenLeft
 									+ (x * tileSizePx);
 							final int tileTop = this.mTouchMapOffsetY
 									+ centerMapTileScreenTop + (y * tileSizePx);
 
 							// final Extent ext =
 							// TileConversor.tileMeterBounds(tile[0], tile[1],
 							// resolution, -originX, -originY);
 							// System.out.println("Tile: " + tile[0] + ", " +
 							// tile[1] + "Extent: " + ext.toString());
 							final Tile t = new Tile(tileURLString, tile,
 									new Pixel(tileLeft, tileTop), zoomLevel,
 									layerName, this.cancellable, null);
 							if (cont < tiles.length)
 								tiles[cont] = t;
 
 							if (DEBUGMODE) {
 								c.drawLine(tileLeft, tileTop, tileLeft
 										+ tileSizePx, tileTop, Paints.mPaintR);
 								c.drawLine(tileLeft, tileTop, tileLeft, tileTop
 										+ tileSizePx, Paints.mPaintR);
 							}
 						}
 						cont++;
 					}
 				}
 
 				this.sortTiles(tiles);
 				final int length = tiles.length;
 				Tile temp;
 				IBitmap currentMapTile;
 				c.drawRect(0, 0, mapWidth, mapHeight, Paints.whitePaint);
 				// bufferCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
 				for (int j = 0; j < length; j++) {
 					temp = tiles[j];
 					if (temp != null) {
 						currentMapTile = this.mTileProvider.getMapTile(temp,
 								cancellable, null);
 						if (currentMapTile != null) {
 							if (areAllValid
 									&& !((BitmapAndroid) currentMapTile).isValid) {
 								areAllValid = false;
 							}
 							TileRaster.lastTouchMapOffsetX = 0;
 							TileRaster.lastTouchMapOffsetY = 0;
 							if (j == 0)
 								bufferCanvas.drawRect(0, 0, mapWidth,
 										mapHeight, Paints.whitePaint);
 
 							// /*****/
 							// Shader gradientShader = new LinearGradient(0, 0,
 							// mapWidth, mapHeight,
 							// Color.argb(0, 0, 0, 0), Color.argb(255, 0,
 							// 0, 0), TileMode.CLAMP);
 							//
 							// Shader bitmapShader = new BitmapShader(
 							// (Bitmap) currentMapTile.getBitmap(),
 							// TileMode.CLAMP, TileMode.CLAMP);
 							//
 							// Shader composeShader = new ComposeShader(
 							// bitmapShader, gradientShader,
 							// new PorterDuffXfermode(Mode.DST_OUT));
 							// Paint mPaint = new Paint();
 							// mPaint.setShader(composeShader);
 							// /******/
 							c.drawBitmap((Bitmap) currentMapTile.getBitmap(),
 									temp.distanceFromCenter.getX(),
 									temp.distanceFromCenter.getY(),
 									Paints.normalPaint);
 
 							bufferCanvas.drawBitmap(
 									(Bitmap) currentMapTile.getBitmap(),
 									temp.distanceFromCenter.getX(),
 									temp.distanceFromCenter.getY(),
 									Paints.normalPaint);
 							TileRaster.lastTouchMapOffsetX = 0;
 							TileRaster.lastTouchMapOffsetY = 0;
 							// System.out.println("paint tile");
 
 							// temp.destroy();
 							temp = null;
 						} else {
 							// System.out.println("tile null");
 							areAllValid = false;
 							someTileNull = true;
 
 							c.drawText("Loading", TileRaster.mapWidth / 2,
 									TileRaster.mapHeight - 35, Paints.mPaint);
 							// c.drawBitmap(bufferBitmap,
 							// TileRaster.lastTouchMapOffsetX,
 							// TileRaster.lastTouchMapOffsetY,
 							// Paints.normalPaint);
 						}
 					}
 				}
 				tiles = null;
 			}
 
 			if (!someTileNull) {
 				TileRaster.lastTouchMapOffsetX = 0;
 				TileRaster.lastTouchMapOffsetY = 0;
 				this.mTileProvider.setLastZoomLevelRequested(this
 						.getZoomLevel());
 			}
 
 			final ArrayList<MapOverlay> overlays = (ArrayList<MapOverlay>) this.mOverlays;
 			final int numLayers = overlays.size();
 
 			MapOverlay o;
 			for (int i = 0; i < numLayers; i++) {
 				try {
 					o = overlays.get(i);
 					o.onManagedDraw(c, this);
 				} catch (Exception e) {
 					// Concurrent modifications can occur
 				}
 			}
 
 			acetate.onManagedDraw(c, this);
 
 			// c.drawBitmap(bufferBitmap, 0, 0, Paints.mPaintR);
 			if (currTouchPoint == null)
 				computeScale();
 
 			if (areAllValid && !scale)
 				pauseDraw();
 			else
 				resumeDraw();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onDraw", e);
 		}
 
 	}
 
 	/**
 	 * Sorts the tiles from the center of the screen to outside to load the
 	 * tiles in spiral.
 	 * 
 	 * @param array2
 	 *            the array of tiles
 	 */
 	private void sortTiles(final Tile[] array2) {
 		try {
 			final Pixel center = new Pixel(centerPixelX, centerPixelY);
 			double e1, e2;
 			final int length = array2.length;
 			Tile t;
 			Tile t1;
 			final Pixel temp = new Pixel((this.getMRendererInfo()
 					.getMAPTILE_SIZEPX() / 2), (this.getMRendererInfo()
 					.getMAPTILE_SIZEPX() / 2));
 			for (int pass = 1; pass < length; pass++) {
 				for (int element = 0; element < length - 1; element++) {
 					t = array2[element];
 					t1 = array2[element + 1];
 					if (t != null && t1 != null) {
 						e1 = (t.distanceFromCenter.add(temp)).distance(center);
 						e2 = (t1.distanceFromCenter.add(temp)).distance(center);
 						if (e1 > e2) {
 							swap(array2, element, element + 1);
 						}
 					}
 				}
 			}
 		} catch (final Exception e) {
 			log.log(Level.SEVERE, "sortTiles:", e);
 		}
 	}
 
 	private void swap(final Tile[] array3, final int first, final int second) {
 		try {
 			final Tile hold = array3[first];
 
 			array3[first] = array3[second];
 			array3[second] = hold;
 		} catch (final Exception e) {
 			log.log(Level.SEVERE, "swap", e);
 		}
 	}
 
 	private class SimpleInvalidationHandler extends Handler {
 
 		@Override
 		public void handleMessage(final Message msg) {
 			try {
 				switch (msg.what) {
 				case Downloader.MAPTILEDOWNLOADER_SUCCESS_ID:
 					// if (!Utils.isSDMounted()) {
 					// TileRaster.this.invalidate();
 					// TileRaster.this.mTileProvider.getMFSTileProvider().getPendingQueue().remove(((TileEvent)msg.obj).getTile().getTileString());
 					// }
 					break;
 				case Downloader.MAPTILEDOWNLOADER_FAIL_ID:
 					// bufferCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
 					break;
 				// case Downloader.REMOVE_CACHE_URL:
 				// // if (!Utils.isSDMounted()) {
 				// TileRaster.this.getMTileProvider().getDownloader().
 				// // }
 				// break;
 				case TileFilesystemProvider.MAPTILEFSLOADER_FAIL_ID:
 					break;
 				case TileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
 					// TileRaster.this.invalidate();
 					// TileRaster.this.mTileProvider.getMFSTileProvider().getPendingQueue().remove(((TileEvent)msg.obj).getTile().getTileString());
 					break;
 				case TileRaster.UPDATE_ZOOM_CONTROLS:
 					map.updateSlider();
 					map.updateZoomControl();
 					cleanZoomRectangle();
 					TileRaster.this.postInvalidate();
 					break;
 				case TileRaster.ANIMATION_FINISHED_NEED_ZOOM:
 					TileRaster.this.zoomIn();
 					break;
 				}
 
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "SimpleInvalidationHandler", e);
 			} finally {
 				resumeDraw();
 			}
 		}
 	}
 
 	private class OpenStreetMapViewGestureDetectorListener implements
 			OnGestureListener, OnDoubleTapListener {
 
 		@Override
 		public boolean onDown(MotionEvent e) {
 			if (TileRaster.this.map.isPOISlideShown())
 				return true;
 			if (!TileRaster.this.getScroller().isFinished()) {
 				TileRaster.this.getScroller().forceFinished(true);
 				invalidateLongPress = true;
 			} else {
 				invalidateLongPress = false;
 			}
 			isScrolling = false;
 
 			return false;
 		}
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			if (TileRaster.this.map.isPOISlideShown())
 				return true;
 			// System.out.println("onFling");
 			startScrolling();
 			final int worldSize = getWorldSizePx();
 			mScroller.fling(getScrollX(), getScrollY(),
 					(int) (-velocityX * 2 / 5), (int) (-velocityY * 2 / 5),
 					-worldSize, worldSize, -worldSize, worldSize);
 			return true;
 		}
 
 		@Override
 		public void onLongPress(MotionEvent e) {
 			try {
 				if (TileRaster.this.map.isPOISlideShown())
 					return;
 				TileRaster.this.onLongPress(e);
 			} catch (Exception est) {
 				log.log(Level.SEVERE, "", est);
 			}
 		}
 
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			// System.out.println("onScroll");
 			scrollBy((int) distanceX, (int) distanceY);
 			return true;
 		}
 
 		@Override
 		public void onShowPress(MotionEvent e) {
 		}
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			try {
 				if (TileRaster.this.map.isPOISlideShown())
 					return true;
 				log.log(Level.FINE, "double tap");
 				double[] coords = TileRaster.this.getMRendererInfo()
 						.fromPixels(
 								new int[] { (int) e.getX(), (int) e.getY() });
 				TileRaster.this.animateTo(coords[0], coords[1], true);
 				// TileRaster.this.zoomIn();
 			} catch (Exception ex) {
 				log.log(Level.SEVERE, "doubletap", ex);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onDoubleTapEvent(MotionEvent arg0) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			if (TileRaster.this.map.isPOISlideShown())
 				return true;
 			if (invalidateLongPress)
 				return false;
 
 			try {
 				if (acetate.onSingleTapUp(e, TileRaster.this))
 					return true;
 
 				try {
 					for (MapOverlay osmvo : TileRaster.this.mOverlays)
 						try {
 							if (osmvo.onSingleTapUp(e, TileRaster.this))
 								return true;
 						} catch (Exception ignore) {
 
 						}
 				} catch (Exception ignore) {
 
 				}
 
				if (acetate.getPopupVisibility() == View.VISIBLE)
					acetate.setPopupVisibility(View.INVISIBLE);
				else
					map.switchSlideBar();
 			} catch (Exception ex) {
 				log.log(Level.SEVERE, "singletapconfirmed", ex);
 
 			}
 			return false;
 		}
 
 		@Override
 		public boolean onSingleTapUp(MotionEvent arg0) {
 			return false;
 
 		}
 	}
 
 	@Override
 	public void onClick(View view) {
 		try {
 			// boolean handled =
 			// this.onSingleTapUpConfirmed(MotionEvent.obtain(0, 0, 0,
 			// centerPixelX, centerPixelY, 0));
 
 			// if (!handled) {
 			// this.zoomIn();
 			// }
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	@Override
 	public boolean onLongClick(View view) {
 		try {
 			// boolean handled = this.onLongPress(MotionEvent.obtain(0, 0, 0,
 			// centerPixelX, centerPixelY, 0));
 
 			// if (!handled) {
 			// zoomOut();
 			// }
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 		return true;
 	}
 
 	// public void instantiateTileProvider() {
 	// Handler lh = new LoadCallbackHandler(new SimpleInvalidationHandler());
 	// this.mTileProvider = new TileProvider(this.androidContext,
 	// new HandlerAndroid(lh), mapWidth, mapHeight, 256,
 	// R.drawable.maptile_loading, TileProvider.MODE_ONLINE,
 	// new QuadKeyFileSystemStrategy(".tile.gvSIG"));
 	// }
 
 	public void instantiateTileProviderfromSettings() {
 		Handler lh = new LoadCallbackHandler(simpleInvalidationHandler);
 		int mode = TileProvider.MODE_ONLINE;
 		String tileName = "tile.gvSIG";
 		String dot = ".";
 		String strategy = ITileFileSystemStrategy.FLATX;
 		ITileFileSystemStrategy t = FileSystemStrategyManager.getInstance()
 				.getStrategyByName(strategy);
 
 		boolean offline = false;
 		try {
 			offline = Settings.getInstance().getBooleanValue(
 					map.getText(R.string.settings_key_offline_maps).toString());
 
 			if (offline)
 				mode = TileProvider.MODE_OFFLINE;
 			else
 				mode = Settings.getInstance()
 						.getIntValue(
 								map.getText(R.string.settings_key_list_mode)
 										.toString());
 		} catch (NoSuchFieldError e) {
 
 		}
 
 		try {
 			tileName = Settings.getInstance().getStringValue(
 					map.getText(R.string.settings_key_tile_name).toString());
 		} catch (NoSuchFieldError e) {
 
 		}
 
 		try {
 			strategy = Settings.getInstance()
 					.getStringValue(
 							map.getText(R.string.settings_key_list_strategy)
 									.toString());
 		} catch (NoSuchFieldError e) {
 
 		}
 
 		String tileSuffix = dot + tileName;
 		t = FileSystemStrategyManager.getInstance().getStrategyByName(strategy);
 		t.setTileNameSuffix(tileSuffix);
 
 		this.mTileProvider = new AndroidTileProvider(this.androidContext,
 				new HandlerAndroid(lh), mapWidth, mapHeight, 256, mode, t);
 	}
 
 	@Override
 	public void onLayerChanged(String layerName) {
 		try {
 			clearBufferCanvas();
 			log.log(Level.FINE, "on layer changed");
 			this.clearCache();
 
 			MapRenderer previous = this.getMRendererInfo();
 			MapRenderer renderer = Layers.getInstance().getRenderer(layerName);
 
 			if (renderer instanceof OSRenderer)
 				OSSettingsUpdater.synchronizeRendererWithSettings(
 						(OSRenderer) renderer, map);
 
 			Tags.DEFAULT_TILE_SIZE = renderer.getMAPTILE_SIZEPX();
 			es.prodevelop.gvsig.mini.utiles.Tags.DEFAULT_TILE_SIZE = renderer
 					.getMAPTILE_SIZEPX();
 			TileConversor.pixelsPerTile = renderer.getMAPTILE_SIZEPX();
 
 			log.log(Level.FINE, "from: " + previous.toString());
 			log.log(Level.FINE, "to: " + renderer.toString());
 
 			if (renderer instanceof WMSRenderer && !map.navigation) {
 				log.log(Level.FINE, "Decreasing size of memory cache");
 				Utils.ROTATE_BUFFER_SIZE = 1;
 			} else {
 				Utils.ROTATE_BUFFER_SIZE = 1;
 			}
 
 			mTileProvider.destroy();
 
 			Utils.BUFFER_SIZE = 2;
 			instantiateTileProviderfromSettings();
 			Extent previousExtent = map.vp.calculateExtent(mapWidth, mapHeight,
 					previous.getCenter());
 			if (renderer != null)
 				this.setRenderer(renderer);
 
 			try {
 				final Route route = RouteManager.getInstance()
 						.getRegisteredRoute();
 				if (route != null)
 					renderer.reprojectGeometryCoordinates(route.getRoute()
 							.getFeatureAt(0).getGeometry(), previous.getSRS());
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "reprojecting route:", e);
 			}
 
 			try {
 				final NamedMultiPoint nm = map.nameds;
 				if (nm != null)
 					renderer.reprojectGeometryCoordinates(nm, previous.getSRS());
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "reprojecting namefinder:", e);
 			}
 
 			double[] newCenter = previous.transformCenter(renderer.getSRS());
 			boolean contains = renderer.getExtent().contains(newCenter);
 
 			double[] minXY = ConversionCoords.reproject(
 					previousExtent.getMinX(), previousExtent.getMinY(),
 					CRSFactory.getCRS(previous.getSRS()),
 					CRSFactory.getCRS(renderer.getSRS()));
 			double[] maxXY = ConversionCoords.reproject(
 					previousExtent.getMaxX(), previousExtent.getMaxY(),
 					CRSFactory.getCRS(previous.getSRS()),
 					CRSFactory.getCRS(renderer.getSRS()));
 			Extent currentExtent = new Extent(minXY[0], minXY[1], maxXY[0],
 					maxXY[1]);
 			if (renderer.isOffline()) {
 
 				renderer.centerOnBBox();
 				this.setMapCenter(renderer.getCenter().getX(), renderer
 						.getCenter().getY());
 				this.zoomToExtent(renderer.getOfflineExtent(), true);
 				Settings.getInstance()
 						.updateStringSharedPreference(
 								map.getText(R.string.settings_key_list_strategy)
 										.toString(),
 								ITileFileSystemStrategy.FLATX, map);
 				Settings.getInstance().updateBooleanSharedPreference(
 						map.getText(R.string.settings_key_offline_maps)
 								.toString(), new Boolean(true), map);
 				instantiateTileProviderfromSettings();
 				Toast.makeText(
 						map,
 						String.format(map.getText(R.string.load_offline)
 								.toString(), renderer.getOfflineLayerName()),
 						Toast.LENGTH_LONG).show();
 			} else {
 				if (contains) {
 					this.setMapCenter(newCenter[0], newCenter[1]);
 					if (previous instanceof OSMMercatorRenderer
 							&& renderer instanceof OSMMercatorRenderer) {
 						setZoomLevel(previous.getZoomLevel(), false);
 					} else {
 						this.zoomToExtent(currentExtent, true);
 					}
 				} else {
 					renderer.centerOnBBox();
 					this.setMapCenter(renderer.getCenter().getX(), renderer
 							.getCenter().getY());
 					// Point p = renderer.getExtent().getCenter();
 					// this.setMapCenter(p.getX(), p.getY());
 				}
 			}
 
 			// this.setZoomLevel(previous.getZoomLevel());
 
 			// if (mapWidth != 0 && mapHeight != 0) {
 			// Extent previousExtent = map.vp.calculateExtent(mapWidth,
 			// mapHeight,
 			// previous.getCenter());
 			//
 			// Point leftBottom = previousExtent.getLefBottomCoordinate();
 			// Point rigthTop = previousExtent.getRightTopCoordinate();
 			//
 			// double[] leftB = ConversionCoords.reproject(leftBottom.getX(),
 			// leftBottom.getY(), CRSFactory.getCRS(previous.getSRS()),
 			// CRSFactory.getCRS(renderer.getSRS()));
 			// double[] rightT = ConversionCoords.reproject(rigthTop.getX(),
 			// rigthTop.getY(), CRSFactory.getCRS(previous.getSRS()),
 			// CRSFactory.getCRS(renderer.getSRS()));
 			//
 			// Extent currentExtent = new Extent(leftB[0], leftB[1], rightT[0],
 			// rightT[1]);
 			// int zoom = this.findZoomFitExtent(currentExtent);
 			// this.setZoomLevel(zoom);
 			// }
 
 			Log.d("TileRaster", "onLayerChanged " + renderer.getFullNAME());
 			map.persist();
 			PerstClusterPOIOverlay poiOverlay = (PerstClusterPOIOverlay) getOverlay(PerstClusterPOIOverlay.DEFAULT_NAME);
 			if (poiOverlay != null && renderer.isOffline()) {
 				/*
 				 * get the previous provider and bookmark provider, close the
 				 * database, free memory
 				 * 
 				 * register the new provider if the database not exists, show a
 				 * message and disable POI options and slidingdrawer if exists
 				 * register also the bookmark provider
 				 */
 				try {
 					PerstOsmPOIClusterProvider provider = POIProviderManager
 							.getInstance().getPOIProvider();
 					provider.getHelper().closeDatabase();
 					PerstBookmarkProvider bookmarkProvider = (PerstBookmarkProvider) POIProviderManager
 							.getInstance().getBookmarkProvider();
 					bookmarkProvider.getHelper().closeDatabase();
 				} catch (BaseException e) {
 					Log.e("", e.getMessage());
 				} catch (Exception e) {
 					Log.e("", e.getMessage());
 				}
 
 				try {
 					String name = renderer.getOfflineLayerName();
 					String[] parts = name.split("_");
 					if (parts.length == 3) {
 						name = parts[0].toLowerCase();
 					}
 					PerstOsmPOIClusterProvider provider = new PerstOsmPOIClusterProvider(
 							Environment.getExternalStorageDirectory()
 									+ File.separator + Utils.APP_DIR
 									+ File.separator + Utils.POIS_DIR
 									+ File.separator + name + File.separator
 									+ name + ".db", getMRendererInfo()
 									.getZOOM_MAXLEVEL()
 									- getMRendererInfo().getZoomMinLevel(),
 							poiOverlay, getMRendererInfo().getZOOM_MAXLEVEL());
 
 					POIProviderManager.getInstance().registerPOIProvider(
 							provider);
 
 					POIProviderManager
 							.getInstance()
 							.getPOIProvider()
 							.loadCategories(POICategories.CATEGORIES,
 									poiOverlay);
 
 					provider.setCurrentZoomLevel(getZoomLevel());
 
 					POIProviderManager
 							.getInstance()
 							.getBookmarkProvider()
 							.setQuadtreeProviderListener(
 									(BookmarkProviderListener) getOverlay(BookmarkOverlay.DEFAULT_NAME));
 					POIProviderManager
 							.getInstance()
 							.getPOIProvider()
 							.setListener(
 									(FullTextSearchListener) getOverlay(ResultSearchOverlay.DEFAULT_NAME));
 				} catch (Exception e) {
 					Log.e("", "error registering database");
 				}
 
 			} else {
 
 			}
 
 			try {
 				PerstOsmPOIClusterProvider provider = POIProviderManager
 						.getInstance().getPOIProvider();
 				if (provider != null) {
 					if (this.poiProviderFailListener != null) {
 						poiProviderFailListener.onNewPOIProvider();
 					}
 				} else {
 					if (this.poiProviderFailListener != null) {
 						poiProviderFailListener.onPOIProviderFail();
 					}
 				}
 			} catch (BaseException e) {
 				if (this.poiProviderFailListener != null) {
 					poiProviderFailListener.onPOIProviderFail();
 				}
 			}
 
 			// new LoadClusterIndexAsyncTask(this.map,
 			// this.poiOverlay.getPoiProvider())
 			// .execute(POICategories.CATEGORIES);
 
 			final List<MapOverlay> overlays = this.mOverlays;
 
 			final int length = overlays.size();
 
 			LayerChangedListener overlay;
 			for (int i = 0; i < length; i++) {
 				try {
 					overlay = overlays.get(i);
 					if (overlay instanceof ExpandedClusterOverlay)
 						this.removeOverlay(((ExpandedClusterOverlay) overlay)
 								.getName());
 					else
 						overlay.onLayerChanged(layerName);
 				} catch (Exception ignore) {
 
 				}
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onlayerchanged:", e);
 		} finally {
 			map.updateSlider();
 		}
 	}
 
 	/**
 	 * 
 	 * @return The center of the map expressed in SRS:4326
 	 */
 	public double[] getCenterLonLat() {
 		double[] res = null;
 		try {
 			final MapRenderer renderer = this.mRendererInfo;
 			res = ConversionCoords.reproject(renderer.getCenter().getX(),
 					renderer.getCenter().getY(),
 					CRSFactory.getCRS(renderer.getSRS()),
 					CRSFactory.getCRS("EPSG:4326"));
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "getCenterLonLat:", e);
 		}
 		return res;
 	}
 
 	public void removeExpanded(String cat) {
 		final List<MapOverlay> overlays = this.mOverlays;
 
 		List<MapOverlay> copyOverlays = new ArrayList();
 
 		int size = overlays.size();
 
 		for (int i = 0; i < size; i++) {
 			copyOverlays.add(mOverlays.get(i));
 		}
 
 		MapOverlay overlay;
 
 		Iterator<MapOverlay> it = copyOverlays.iterator();
 
 		while (it.hasNext()) {
 			overlay = it.next();
 			if (overlay instanceof ExpandedClusterOverlay) {
 				if (((ExpandedClusterOverlay) overlay).isRemovable(cat)) {
 					// overlays.remove(overlay);
 					overlay.setVisible(false);
 					this.removeOverlay(overlay.getName());
 					// size--;
 				}
 			}
 		}
 
 		// copyOverlays = null;
 	}
 
 	public boolean removeExpanded() {
 		try {
 			final List<MapOverlay> overlays = this.mOverlays;
 
 			List<MapOverlay> copyOverlays = new ArrayList();
 
 			int size = overlays.size();
 
 			for (int i = 0; i < size; i++) {
 				copyOverlays.add(mOverlays.get(i));
 			}
 
 			MapOverlay overlay;
 
 			Iterator<MapOverlay> it = copyOverlays.iterator();
 
 			while (it.hasNext()) {
 				overlay = it.next();
 				if (overlay instanceof ExpandedClusterOverlay) {
 					if (((ExpandedClusterOverlay) overlay).isRemovable()) {
 						// overlays.remove(overlay);
 						overlay.setVisible(false);
 						this.removeOverlay(overlay.getName());
 						return true;
 						// size--;
 					}
 				}
 			}
 			return false;
 
 		} catch (Exception e) {
 			Log.e("", "Error removingExpanded");
 			return false;
 		}
 	}
 
 	/**
 	 * Sets the center of the map from a longitude, latitude. Also makes the
 	 * coordinates conversion to the SRS of the MapRenderer
 	 * 
 	 * @param lon
 	 *            The longitude
 	 * @param lat
 	 *            The latitude
 	 */
 	public void setMapCenterFromLonLat(double lon, double lat) {
 		try {
 			if (lon == 0.0 && lat == 0.0)
 				return;
 			double[] coords = ConversionCoords.reproject(lon, lat,
 					CRSFactory.getCRS("EPSG:4326"),
 					CRSFactory.getCRS(this.mRendererInfo.getSRS()));
 			this.setMapCenter(coords[0], coords[1]);
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "setMapCenterFromLonLat", e);
 		}
 	}
 
 	/**
 	 * Zooms the view to fit an Extent
 	 * 
 	 * @param extent
 	 *            The extent to fit
 	 * @param layerChanged
 	 *            If true then the Scaler is not applied when zooming
 	 * @param currentZoomLevel
 	 *            The zoom level to get the resolution from
 	 */
 	public void zoomToExtent(final Extent extent, boolean layerChanged,
 			int currentZoomLevel) {
 		zoomToSpan(extent.getWidth(), extent.getHeight(), layerChanged,
 				currentZoomLevel, false, null);
 	}
 
 	/**
 	 * Zooms the view to fit an Extent
 	 * 
 	 * @param extent
 	 *            The extent to fit
 	 * @param layerChanged
 	 *            If true then the Scaler is not applied when zooming
 	 */
 	public void zoomToExtent(final Extent extent, boolean layerChanged) {
 		zoomToExtent(extent, layerChanged, false, null);
 	}
 
 	/**
 	 * Zooms the view to fit an Extent
 	 * 
 	 * @param extent
 	 *            The extent to fit
 	 * @param layerChanged
 	 *            If true then the Scaler is not applied when zooming
 	 * @param forceZoomMore
 	 *            If true adds one level to the zoom calculated to fit the
 	 *            extent
 	 * @param animateTo
 	 *            If is not null, animates to this point
 	 */
 	public void zoomToExtent(final Extent extent, boolean layerChanged,
 			boolean forceZoomMore, Point animateToCenter) {
 		zoomToSpan(extent.getWidth(), extent.getHeight(), layerChanged,
 				this.getZoomLevel(), forceZoomMore, animateToCenter);
 	}
 
 	/**
 	 * Zooms the view to a width, height
 	 * 
 	 * @param width
 	 *            The width to fit the zoom
 	 * @param height
 	 *            The height to fit the zoom
 	 * @param layerChanged
 	 *            If true then the Scaler is not applied when zooming
 	 * @param currentZoomLevel
 	 *            The zoom level to get the resolution from
 	 * @param forceZoomMore
 	 *            If true adds one level to the zoom calculated to fit the
 	 *            extent
 	 * @param animateTo
 	 *            If is not null, animates to this point
 	 */
 	public void zoomToSpan(final double width, final double height,
 			boolean layerChanged, int currentZoomLevel, boolean forceZoomMore,
 			Point animateToCenter) {
 		if (width <= 0 || height <= 0) {
 			return;
 		}
 
 		int zoomLevel = getZoomLevelFitsExtent(width, height, currentZoomLevel);
 		if (zoomLevel != -1) {
 			if (!layerChanged && zoomLevel <= this.getZoomLevel())
 				zoomLevel++;
 			if (forceZoomMore)
 				zoomLevel++;
 			if (animateToCenter != null)
 				animateTo(animateToCenter.getX(), animateToCenter.getY());
 			setZoomLevel(zoomLevel, !layerChanged);
 		}
 	}
 
 	/**
 	 * Zooms the view to a width, height
 	 * 
 	 * @param width
 	 *            The width to fit the zoom
 	 * @param height
 	 *            The height to fit the zoom
 	 * @param layerChanged
 	 *            If true then the Scaler is not applied when zooming
 	 */
 	public void zoomToSpan(final double width, final double height,
 			boolean layerChanged) {
 		if (width <= 0 || height <= 0) {
 			return;
 		}
 
 		int zoomLevel = getZoomLevelFitsExtent(width, height);
 		if (zoomLevel != -1) {
 			if (!layerChanged && zoomLevel <= this.getZoomLevel())
 				zoomLevel++;
 			setZoomLevel(zoomLevel, !layerChanged);
 		}
 	}
 
 	/**
 	 * Calculates a zoom level that fits an extent
 	 * 
 	 * @param width
 	 *            The width of the extent
 	 * @param height
 	 *            The height of the extent
 	 * @param currentZoomLevel
 	 *            the current zoom level to get resolution from
 	 * @return The zoom level that fits the extent
 	 */
 	public int getZoomLevelFitsExtent(final double width, final double height,
 			final int currentZoomLevel) {
 		final Extent mapExtent = ViewPort.calculateExtent(this
 				.getMRendererInfo().getCenter(),
 				this.getMRendererInfo().resolutions[currentZoomLevel],
 				mapWidth, mapHeight);
 		final int curZoomLevel = currentZoomLevel;
 
 		final double curWidth = mapExtent.getWidth();
 		final double curHeight = mapExtent.getHeight();
 
 		final double diffNeededX = width / curWidth;
 		final double diffNeededY = height / curHeight;
 
 		if (Double.isInfinite(diffNeededX) || Double.isInfinite(diffNeededY)) {
 			return -1;
 		}
 
 		final double diffNeeded = Math.max(diffNeededX, diffNeededY);
 
 		if (diffNeeded > 1) {
 			return curZoomLevel - Utils.getNextSquareNumberAbove(diffNeeded);
 		} else if (diffNeeded < 0.5) {
 			return curZoomLevel
 					+ Utils.getNextSquareNumberAbove(1 / diffNeeded) - 1;
 		} else {
 			return curZoomLevel;
 		}
 	}
 
 	/**
 	 * Calculates a zoom level that fits an extent
 	 * 
 	 * @param width
 	 *            The width of the extent
 	 * @param height
 	 *            The height of the extent
 	 * @return The zoom level that fits the extent
 	 */
 	public int getZoomLevelFitsExtent(final double width, final double height) {
 		return this.getZoomLevelFitsExtent(width, height, getZoomLevel());
 	}
 
 	// @Override
 	// public void postInvalidate() {
 	// // if (refresh)
 	// // super.postInvalidate();
 	// }
 	//
 	// @Override
 	// public void invalidate() {
 	// // if (refresh)
 	// // super.invalidate();
 	// }
 
 	// public boolean refresh = true;
 	//
 	// private class ZoomRefreshTask extends TimerTask {
 	//
 	// @Override
 	// public void run() {
 	// refresh = true;
 	// postInvalidate();
 	// try {
 	// this.finalize();
 	// } catch (Throwable e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	// }
 	//
 	// }
 
 	AnimationRunner mCurrentAnimationRunner;
 
 	/**
 	 * Applies a LinearAnimation to center the view to a xy coordinates
 	 * 
 	 * @param x
 	 *            The x coordinate
 	 * @param y
 	 *            The y coordinate
 	 */
 	public void animateTo(final double x, final double y) {
 		// this.stopAnimation(false);
 		// log.log(Level.FINE, "animate TO");
 		this.animateTo(x, y, false);
 	}
 
 	/**
 	 * Applies a LinearAnimation to center the view to a xy coordinates
 	 * 
 	 * @param x
 	 *            The x coordinate
 	 * @param y
 	 *            The y coordinate
 	 * @param zoomChanged
 	 *            if true, when the animation ends applies a zoomIn
 	 */
 	public void animateTo(final double x, final double y,
 			final boolean zoomChanged) {
 		// this.stopAnimation(false);
 		// log.log(Level.FINE, "animate TO");
 		// final Scroller aScroller = getScroller();
 		// int xy[] = this.getMRendererInfo().toPixels(new double[] { x, y });
 		// startScrolling();
 		// aScroller.startScroll(getScrollX(), getScrollY(),
 		// (int) (xy[0] - this.centerPixelX),
 		// (int) (xy[1] - this.centerPixelY), 1000);
 		mCurrentAnimationRunner = new LinearAnimationRunner(x, y, true,
 				zoomChanged);
 		WorkQueue.getExclusiveInstance().execute(mCurrentAnimationRunner);
 	}
 
 	/**
 	 * Stops a running animation.
 	 * 
 	 * @param jumpToTarget
 	 */
 	public void stopAnimation(final boolean jumpToTarget) {
 		final AnimationRunner currentAnimationRunner = this.mCurrentAnimationRunner;
 
 		if (currentAnimationRunner != null && !currentAnimationRunner.isDone()) {
 			// currentAnimationRunner.interrupt();
 			if (jumpToTarget) {
 				this.setMapCenter(currentAnimationRunner.mTargetX,
 						currentAnimationRunner.mTargetY);
 			}
 		}
 	}
 
 	// protected Object lock = new Object();
 
 	private abstract class AnimationRunner implements Runnable {
 
 		protected final int mSmoothness, mDuration;
 		protected final double mTargetX, mTargetY;
 		protected boolean mDone = false;
 
 		protected final int mStepDuration;
 
 		protected final double mPanTotalX, mPanTotalY;
 
 		public AnimationRunner(final double aTargetX, final double aTargetY) {
 			this(aTargetX, aTargetY, 10, 1000);
 		}
 
 		public abstract void setTarget(final double x, final double y);
 
 		public AnimationRunner(final double aTargetX, final double aTargetY,
 				final int aSmoothness, final int aDuration) {
 			this.mTargetX = aTargetX;
 			this.mTargetY = aTargetY;
 			this.mSmoothness = aSmoothness;
 			this.mDuration = aDuration;
 			this.mStepDuration = aDuration / aSmoothness;
 
 			Point center = TileRaster.this.getMRendererInfo().getCenter();
 
 			final double mapCenterX = center.getX();
 			final double mapCenterY = center.getY();
 
 			this.mPanTotalX = (mapCenterX - aTargetX);
 			this.mPanTotalY = (mapCenterY - aTargetY);
 		}
 
 		@Override
 		public void run() {
 			onRunAnimation();
 			this.mDone = true;
 		}
 
 		public boolean isDone() {
 			return this.mDone;
 		}
 
 		public abstract void onRunAnimation();
 	}
 
 	private class LinearAnimationRunner extends AnimationRunner {
 
 		protected double mPanPerStepX, mPanPerStepY;
 		private boolean zoomChanged = false;
 
 		public LinearAnimationRunner(final double aTargetX,
 				final double aTargetY, final boolean animate,
 				final boolean zoomChanged) {
 			this(aTargetX, aTargetY, 10, 500, animate);
 			this.zoomChanged = zoomChanged;
 			// start();
 		}
 
 		public LinearAnimationRunner(final double aTargetX,
 				final double aTargetY, final int aSmoothness,
 				final int aDuration, final boolean animate) {
 			super(aTargetX, aTargetY, aSmoothness, aDuration);
 			if (animate)
 				setTarget(aTargetX, aTargetY, aSmoothness);
 		}
 
 		public void setTarget(final double x, final double y,
 				final int aSmoothness) {
 			Point center = TileRaster.this.getMRendererInfo().getCenter();
 			final double mapCenterX = center.getX();
 			final double mapCenterY = center.getY();
 
 			this.mPanPerStepX = (mapCenterX - x) / aSmoothness;
 			this.mPanPerStepY = (mapCenterY - y) / aSmoothness;
 
 			// this.setName("LinearAnimationRunner");
 		}
 
 		public void setTarget(final double x, final double y) {
 			setTarget(x, y, 10);
 		}
 
 		@Override
 		public void onRunAnimation() {
 			final double panPerStepX = this.mPanPerStepX;
 			final double panPerStepY = this.mPanPerStepY;
 			final int stepDuration = this.mStepDuration;
 			try {
 				// while (!mDone) {
 				// synchronized (lock) {
 				// try {
 				// lock.wait();
 				// } catch (InterruptedException ignored) {
 				//
 				// }
 				// }
 
 				double newMapCenterX;
 				double newMapCenterY;
 
 				Point center;
 
 				for (int i = this.mSmoothness; i > 0; i--) {
 					center = TileRaster.this.getMRendererInfo().getCenter();
 
 					newMapCenterX = center.getX() - panPerStepX;
 					newMapCenterY = center.getY() - panPerStepY;
 					setMapCenter(newMapCenterX, newMapCenterY);
 
 					Thread.sleep(stepDuration);
 				}
 				setMapCenter(super.mTargetX, super.mTargetY);
 				// }
 			} catch (final Exception e) {
 				// this.interrupt();
 			} finally {
 				if (zoomChanged)
 					TileRaster.this.simpleInvalidationHandler
 							.sendEmptyMessage(TileRaster.ANIMATION_FINISHED_NEED_ZOOM);
 			}
 		}
 	}
 
 	/**
 	 * Switch from panMode to rectangleMode and viceversa
 	 */
 	public void switchPanMode() {
 		panMode = !panMode;
 	}
 
 	/**
 	 * Calculates the size of the rectangle to mark the zoom level to apply when
 	 * zooming in, and tells the AcetateOverlay to draw it
 	 * 
 	 * @param zoom
 	 */
 	public void drawZoomRectangle(int zoom) {
 		try {
 			double res = this.getMRendererInfo().resolutions[zoom];
 			final Point center = this.getMRendererInfo().getCenter();
 			Extent extent = map.vp.calculateExtent(center, res, mapWidth,
 					mapHeight);
 			Extent currentExtent = map.vp.calculateExtent(mapWidth, mapHeight,
 					center);
 			double[] minXY = new double[] { extent.getMinX(), extent.getMinY() };
 			double[] maxXY = new double[] { extent.getMaxX(), extent.getMaxY() };
 			int[] leftBottom = map.vp.fromMapPoint(minXY,
 					currentExtent.getMinX(), currentExtent.getMaxY());
 			int[] rightTop = map.vp.fromMapPoint(maxXY,
 					currentExtent.getMinX(), currentExtent.getMaxY());
 			Rect r = new Rect();
 			r.set(leftBottom[0], rightTop[1], rightTop[0], leftBottom[1]);
 			if (leftBottom[0] <= 0 || leftBottom[1] >= mapHeight
 					|| rightTop[1] <= 0 || rightTop[0] >= mapWidth)
 				return;
 			acetate.drawZoomRectangle(r);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "drawZoomRectangle", e);
 		}
 	}
 
 	/**
 	 * Cleans the ZoomRectangle of the AcetateOverlay
 	 */
 	public void cleanZoomRectangle() {
 		try {
 			acetate.cleanZoomRectangle();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "cleanZoomRectangle", e);
 		}
 	}
 
 	@Override
 	public void invalidate() {
 		resumeDraw();
 		super.invalidate();
 	}
 
 	@Override
 	public void postInvalidate() {
 		resumeDraw();
 		super.postInvalidate();
 	}
 
 	public void adjustViewToAccuracyIfNavigationMode(final float accuracy) {
 		try {
 			final MapRenderer renderer = this.getMRendererInfo();
 			Point center = renderer.center;
 			double[] xy = ConversionCoords.reproject(center.getX(),
 					center.getY(), CRSFactory.getCRS(renderer.getSRS()),
 					CRSFactory.getCRS("EPSG:900913"));
 			double minX = xy[0] - accuracy * 1;
 			double maxX = xy[0] + accuracy * 1;
 			double minY = xy[1] - accuracy * 1;
 			double maxY = xy[1] + accuracy * 1;
 
 			double[] minXY = ConversionCoords.reproject(minX, minY,
 					CRSFactory.getCRS("EPSG:900913"),
 					CRSFactory.getCRS(renderer.getSRS()));
 			double[] maxXY = ConversionCoords.reproject(maxX, maxY,
 					CRSFactory.getCRS("EPSG:900913"),
 					CRSFactory.getCRS(renderer.getSRS()));
 
 			this.zoomToExtent(
 					new Extent(minXY[0], minXY[1], maxXY[0], maxXY[1]), true);
 		} catch (Exception ignore) {
 
 		}
 	}
 
 	/**
 	 * Sets the Feature selected by the user when onLongPress and animates the
 	 * view to it
 	 * 
 	 * @param f
 	 *            The Feature selected
 	 */
 	public void setSelectedFeature(Feature f) {
 		try {
 			this.selectedFeature = f;
 			// if (f != null) {
 			// Point geom = (Point) f.getGeometry();
 			// log.log(Level.FINE, "selectedFeature: " + geom.toString());
 			// this.animateTo(geom.getX(), geom.getY());
 			// }
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "setSelectedFeature", e);
 		}
 	}
 
 	/**
 	 * 
 	 * @return True if PanMode is active, False if zoomRectangle is active
 	 */
 	public boolean isPanMode() {
 		return panMode;
 	}
 
 	class Scaler {
 
 		public static final int DURATION_SHORT = 500;
 		public static final int DURATION_LONG = 1000;
 
 		private float mStartScale;
 		private float mFinalScale;
 		private float mCurrScale;
 
 		private long mStartTime;
 		private int mDuration;
 		private float mDurationReciprocal;
 		private float mDeltaScale;
 		private boolean mFinished;
 		private Interpolator mInterpolator;
 
 		/**
 		 * Create a Scaler with the specified interpolator.
 		 */
 		public Scaler(Context context, Interpolator interpolator) {
 			mFinished = true;
 			mInterpolator = interpolator;
 		}
 
 		/**
 		 * 
 		 * Returns whether the scaler has finished scaling.
 		 * 
 		 * @return True if the scaler has finished scaling, false otherwise.
 		 */
 		public final boolean isFinished() {
 			return mFinished;
 		}
 
 		/**
 		 * Force the finished field to a particular value.
 		 * 
 		 * @param finished
 		 *            The new finished value.
 		 */
 		public final void forceFinished(boolean finished) {
 			mFinished = finished;
 		}
 
 		/**
 		 * Returns how long the scale event will take, in milliseconds.
 		 * 
 		 * @return The duration of the scale in milliseconds.
 		 */
 		public final int getDuration() {
 			return mDuration;
 		}
 
 		/**
 		 * Returns the current scale factor.
 		 * 
 		 * @return The new scale factor.
 		 */
 		public final float getCurrScale() {
 			return mCurrScale;
 		}
 
 		/**
 		 * Returns the start scale factor.
 		 * 
 		 * @return The start scale factor.
 		 */
 		public final float getStartScale() {
 			return mStartScale;
 		}
 
 		/**
 		 * Returns where the scale will end.
 		 * 
 		 * @return The final scale factor.
 		 */
 		public final float getFinalScale() {
 			return mFinalScale;
 		}
 
 		/**
 		 * Sets the final scale for this scaler.
 		 * 
 		 * @param newScale
 		 *            The new scale factor.
 		 */
 		public void setFinalScale(float newScale) {
 			mFinalScale = newScale;
 			mDeltaScale = mFinalScale - mStartScale;
 			mFinished = false;
 		}
 
 		/**
 		 * Call this when you want to know the new scale. If it returns true,
 		 * the animation is not yet finished.
 		 */
 		public boolean computeScale() {
 			resumeDraw();
 			if (mFinished) {
 				Paints.mPaintR = new Paint();
 				mCurrScale = 1.0f;
 				return false;
 			}
 
 			int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
 
 			if (timePassed < mDuration) {
 				float x = (float) timePassed * mDurationReciprocal;
 
 				x = mInterpolator.getInterpolation(x);
 
 				mCurrScale = mStartScale + x * mDeltaScale;
 				if (mCurrScale == mFinalScale)
 					mFinished = true;
 
 			} else {
 				mCurrScale = mFinalScale;
 				mFinished = true;
 			}
 			return true;
 		}
 
 		/**
 		 * Start scaling by providing the starting scale and the final scale.
 		 * 
 		 * @param startX
 		 *            Starting horizontal scroll offset in pixels. Positive
 		 *            numbers will scroll the content to the left.
 		 * @param startY
 		 *            Starting vertical scroll offset in pixels. Positive
 		 *            numbers will scroll the content up.
 		 * @param dx
 		 *            Horizontal distance to travel. Positive numbers will
 		 *            scroll the content to the left.
 		 * @param dy
 		 *            Vertical distance to travel. Positive numbers will scroll
 		 *            the content up.
 		 * @param duration
 		 *            Duration of the scroll in milliseconds.
 		 */
 		public void startScale(float startScale, float finalScale, int duration) {
 			Paints.mPaintR.setFlags(Paint.FILTER_BITMAP_FLAG);
 
 			mFinished = false;
 			mDuration = duration;
 			mStartTime = AnimationUtils.currentAnimationTimeMillis();
 			mStartScale = startScale;
 			mFinalScale = finalScale;
 			mDeltaScale = finalScale - startScale;
 			mDurationReciprocal = 1.0f / (float) mDuration;
 			// bufferCanvas.drawBitmap(frontBitmap,
 			// TileRaster.this.mTouchMapOffsetX,
 			// TileRaster.this.mTouchMapOffsetY, rotatePaint);
 		}
 
 		/**
 		 * Extend the scale animation. This allows a running animation to scale
 		 * further and longer, when used with {@link #setFinalScale(float)}.
 		 * 
 		 * @param extend
 		 *            Additional time to scale in milliseconds.
 		 * @see #setFinalScale(float)
 		 */
 		public void extendDuration(int extend) {
 			Paints.mPaintR.setFlags(Paint.FILTER_BITMAP_FLAG);
 
 			int passed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
 			mDuration = passed + extend;
 			mDurationReciprocal = 1.0f / (float) mDuration;
 			mFinished = false;
 		}
 	}
 
 	/**
 	 * 
 	 * @return The TileProvider
 	 */
 	public TileProvider getMTileProvider() {
 		return mTileProvider;
 	}
 
 	public void addExtentChangedListener(ExtentChangedListener listener) {
 		if (listener != null)
 			this.extentChangedListeners.add(listener);
 	}
 
 	public void removeExtentChangedListener(ExtentChangedListener listener) {
 		if (listener != null)
 			this.extentChangedListeners.remove(listener);
 	}
 
 	/**
 	 * Clears the pending queues of the TileProvider
 	 */
 	public void clearCache() {
 		try {
 			log.log(Level.FINE, "clearCache");
 			this.mTileProvider.clearPendingQueue();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "clearCache", e);
 		}
 	}
 
 	/**
 	 * Frees memory
 	 */
 	public void destroy() {
 		try {
 			log.log(Level.FINE, "destroy tileraster");
 			acetate.destroy();
 			mTileProvider.destroy();
 
 			for (MapOverlay m : mOverlays) {
 				m.destroy();
 			}
 
 			geomDrawer = null;
 			// this.mCurrentAnimationRunner.interrupt();
 			// this.mCurrentAnimationRunner = null;
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "destroy", e);
 		}
 	}
 
 	// --------------------------------------------------------------------------
 	// -----------------
 
 	@Override
 	public Object getDraggableObjectAtPoint(PointInfo pt) {
 		// Just return something non-null, we don't need to keep track of which
 		// specific object is being dragged
 		return this;
 	}
 
 	@Override
 	public void getPositionAndScale(Object obj,
 			PositionAndScale objPosAndScaleOut) {
 		// We start at 0.0f each time the drag position is replaced, because we
 		// just want the relative drag distance
 		objPosAndScaleOut.set(xOff, yOff, relativeScale);
 	}
 
 	@Override
 	public void selectObject(Object obj, PointInfo pt) {
 
 	}
 
 	@Override
 	public boolean setPositionAndScale(Object obj, PositionAndScale update,
 			PointInfo touchPoint) {
 		try {
 			if (!touchPoint.isMultiTouch()) {
 				// currTouchPoint = null;
 			} else {
 				if (touchPoint.getMultiTouchDiameter() != 0)
 					currTouchPoint = touchPoint;
 				if (this.currTouchPoint.isDown()) {
 					mScaler.mCurrScale = update.getScale();
 					// this.zoomToExtent(e, false);
 				}
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 
 		}
 
 		return true;
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		if (this.surfaceThread != null)
 			this.surfaceThread.onWindowResize(width, height);
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		hasSurface = true;
 		startST();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		hasSurface = false;
 		destroyST();
 	}
 
 	private void clearBufferCanvas() {
 		if (this.bufferCanvas != null) {
 			bufferCanvas.drawRect(0, 0, mapWidth, mapHeight, Paints.whitePaint);
 		}
 
 		if (this.zoomCanvas != null) {
 			zoomCanvas.drawRect(0, 0, mapWidth, mapHeight, Paints.whitePaint);
 		}
 	}
 
 	public void initializeCanvas(int width, int height) throws BaseException {
 		mapWidth = width;
 		mapHeight = height;
 
 		if (bufferBitmap != null) {
 			bufferBitmap.recycle();
 			bufferBitmap = null;
 		}
 
 		if (bufferBitmap == null) {
 			bufferBitmap = Bitmap.createBitmap(mapWidth, mapHeight,
 					Bitmap.Config.RGB_565);
 			bufferCanvas.setBitmap(bufferBitmap);
 		}
 
 		if (zoomBitmap != null) {
 			zoomBitmap.recycle();
 			zoomBitmap = null;
 		}
 
 		if (zoomBitmap == null) {
 			zoomBitmap = Bitmap.createBitmap(mapWidth, mapHeight,
 					Bitmap.Config.RGB_565);
 			zoomCanvas.setBitmap(zoomBitmap);
 		}
 
 		ViewPort.mapHeight = height;
 		ViewPort.mapWidth = width;
 		this.centerPixelX = width / 2;
 		this.centerPixelY = height / 2;
 
 		acetate.setPopupMaxSize(width, height);
 	}
 
 	boolean scaleCanvasForZoom() {
 		try {
 			if (scale) {
 				zoomCanvas.drawBitmap((Bitmap) bufferBitmap, 0, 0,
 						Paints.normalPaint);
 				bufferCanvas.drawRect(0, 0, mapWidth, mapHeight,
 						Paints.whitePaint);
 
 				if (mScaler.mFinalScale > 0) {
 					if (mScaler.mFinalScale < 1) {
 						final int scaledWidth = (int) (mapWidth * mScaler.mFinalScale);
 						final int scaledHeight = (int) (mapHeight * mScaler.mFinalScale);
 						// if width and height are lower than 0, the VM will
 						// crash
 						if (scaledWidth > 0 && scaledHeight > 0) {
 							final Bitmap scaledBitmap = Bitmap
 									.createScaledBitmap(zoomBitmap,
 											scaledWidth, scaledHeight, true);
 							bufferCanvas.drawBitmap(scaledBitmap,
 									(mapWidth - (int) (scaledWidth)) / 2,
 									(mapHeight - (int) (scaledHeight)) / 2,
 									Paints.mPaint);
 						}
 					} else {
 						final float inverseScale = 1 / mScaler.mFinalScale;
 						final int x = (mapWidth - (int) ((mapWidth * inverseScale))) / 2;
 						final int y = (mapHeight - (int) ((mapHeight * inverseScale))) / 2;
 						final int width = (int) (mapWidth * inverseScale);
 						final int height = (int) (mapHeight * inverseScale);
 						final Bitmap cropBitmap = Bitmap.createBitmap(
 								zoomBitmap, x, y, width, height);
 						final Bitmap scaledBitmap = Bitmap.createScaledBitmap(
 								cropBitmap, (int) (mapWidth),
 								(int) (mapHeight), true);
 						bufferCanvas.drawBitmap(scaledBitmap, 0, 0,
 								Paints.mPaint);
 					}
 				}
 				return true;
 			}
 		} catch (Exception ignore) {
 		} catch (OutOfMemoryError out) {
 
 		} finally {
 			scale = false;
 		}
 		return false;
 	}
 
 	private void init() {
 		holder = getHolder();
 		holder.addCallback(this);
 		hasSurface = false;
 	}
 
 	/**
 	 * Starts the surfaceThread
 	 */
 	public void startST() {
 		if (this.surfaceThread == null) {
 			this.surfaceThread = new TileRasterThread(holder, this);
 
 			if (hasSurface) {
 				surfaceThread.start();
 			}
 		}
 	}
 
 	/**
 	 * Destroys the surfaceThread
 	 */
 	public void destroyST() {
 		if (this.surfaceThread != null) {
 			resumeDraw();
 			surfaceThread.requestExitAndWait();
 			this.surfaceThread = null;
 		}
 	}
 
 	public Scroller getScroller() {
 		return mScroller;
 	}
 
 	@Override
 	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
 		if ((oldl == 0 && oldt == 0) || !isScrolling)
 			return;
 		resetTouchOffset();
 		// System.out.println(l + ", " + t + ", " + oldl + ", " + oldt);
 		int px = l - oldl;
 		int py = t - oldt;
 
 		// System.out.println("px, py: " + px / 2 + ", " + py / 2);
 
 		int newCenterPx = this.centerPixelX + px;
 		int newCenterPy = this.centerPixelY + py;
 		// System.out.println("npx, npy: " + newCenterPx + ", " + newCenterPy);
 
 		double[] newCenter = ViewPort.toMapPoint(new int[] { newCenterPx,
 				newCenterPy }, mapWidth, mapHeight, scrollingCenter,
 				this.getMRendererInfo().resolutions[this.getZoomLevel()]);
 
 		synchronized (holder) {
 			scrollingCenter.setCoordinates(newCenter);
 			this.setMapCenter(scrollingCenter.getX(), scrollingCenter.getY());
 		}
 
 		// System.out.println("wpx; wpy: " + newCenter[0] + ", " +
 		// newCenter[1]);
 	}
 
 	private void startScrolling() {
 		isScrolling = true;
 		this.scrollingCenter = this.getMRendererInfo().getCenter();
 	}
 
 	private void resetTouchOffset() {
 		TileRaster.mTouchMapOffsetX = 0;
 		TileRaster.mTouchMapOffsetY = 0;
 		ViewPort.mTouchMapOffsetX = 0;
 		ViewPort.mTouchMapOffsetY = 0;
 	}
 
 	@Override
 	public void computeScroll() {
 		// System.out.println("computeScroll");
 		if (mScroller.computeScrollOffset()) {
 			if (mScroller.isFinished()) {
 				setZoomLevel(tempZoomLevel);
 				isScrolling = false;
 			} else
 				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
 			postInvalidate(); // Keep on drawing until the animation has
 			// finished.
 		}
 	}
 
 	int getPixelZoomLevel() {
 		double extentWidth = this.getMRendererInfo().getExtent().getWidth();
 		double extentHeight = this.getMRendererInfo().getExtent().getHeight();
 
 		double size = Math.max(extentWidth, extentHeight);
 
 		return (int) (size * this.getMRendererInfo().getMAPTILE_SIZEPX());
 	}
 
 	int getWorldSizePx() {
 		return Integer.MAX_VALUE;
 	}
 
 	@Override
 	public void scrollTo(int x, int y) {
 		// System.out.println("scrollTo");
 		final int worldSize = getWorldSizePx();
 		x %= worldSize;
 		y %= worldSize;
 		super.scrollTo(x, y);
 	}
 
 	public void pauseDraw() {
 		if (this.surfaceThread != null)
 			this.surfaceThread.pause = true;
 	}
 
 	public void resumeDraw() {
 		if (this.surfaceThread != null)
 			this.surfaceThread.pause = false;
 	}
 }
 
 class TileRasterThread extends Thread {
 	private SurfaceHolder surfaceHolder;
 	private TileRaster view;
 	private boolean done = false;
 	public boolean pause = false;
 
 	public TileRasterThread(SurfaceHolder surfaceHolder, TileRaster view) {
 		this.surfaceHolder = surfaceHolder;
 		this.view = view;
 	}
 
 	// public void setRunning(boolean run) {
 	// this.run = run;
 	// }
 
 	public SurfaceHolder getSurfaceHolder() {
 		return surfaceHolder;
 	}
 
 	@Override
 	public void run() {
 		Canvas c;
 		while (!done) {
 			while (pause && view.mScaler.isFinished()) {
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			c = null;
 			try {
 				c = surfaceHolder.lockCanvas();
 				// if (this.view.acetate.isFirstTouch())
 				// synchronized (surfaceHolder) {
 				// this.view.onDraw(c);
 				// this.view.scaleCanvasForZoom();
 				// }
 				// else {
 				this.view.onDraw(c);
 				this.view.scaleCanvasForZoom();
 				// }
 			} finally {
 				if (c != null) {
 					surfaceHolder.unlockCanvasAndPost(c);
 				}
 			}
 		}
 	}
 
 	public void requestExitAndWait() {
 		done = true;
 		try {
 			join();
 		} catch (InterruptedException ex) {
 
 		}
 	}
 
 	public void onWindowResize(int w, int h) {
 		view.resumeDraw();
 	}
 }
