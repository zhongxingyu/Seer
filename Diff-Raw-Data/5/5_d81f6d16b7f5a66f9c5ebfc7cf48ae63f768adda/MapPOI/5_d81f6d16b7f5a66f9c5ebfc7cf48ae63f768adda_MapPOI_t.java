 /* gvSIG Mini. A free mobile phone viewer of free maps.
  *
  * Copyright (C) 2011 Prodevelop.
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
  *   2011.
  *   author Alberto Romeu aromeu@prodevelop.es
  */
 
 package es.prodevelop.gvsig.mini.activities;
 
 import java.util.ArrayList;
 
 import android.R.color;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.AbstractAction;
 
 import es.prodevelop.android.spatialindex.poi.POICategories;
 import es.prodevelop.android.spatialindex.quadtree.provide.perst.PerstOsmPOIProvider;
 import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
 import es.prodevelop.gvsig.mini.R;
 import es.prodevelop.gvsig.mini.geom.Point;
 import es.prodevelop.gvsig.mini.offline.reader.OfflineMapsReader;
 import es.prodevelop.gvsig.mini.search.POIProviderChangedListener;
 import es.prodevelop.gvsig.mini.search.POIProviderManager;
 import es.prodevelop.gvsig.mini.search.activities.SearchExpandableActivity;
 import es.prodevelop.gvsig.mini.tasks.poi.InvokeIntents;
 import es.prodevelop.gvsig.mini.views.overlay.BookmarkOverlay;
 import es.prodevelop.gvsig.mini.views.overlay.CategoriesListView;
 import es.prodevelop.gvsig.mini.views.overlay.CategoriesListView.CheckBoxBulletAdapter;
 import es.prodevelop.gvsig.mini.views.overlay.PerstClusterPOIOverlay;
 import es.prodevelop.gvsig.mini.views.overlay.ResultSearchOverlay;
 import es.prodevelop.gvsig.mini.views.overlay.SlidingDrawer2;
 import es.prodevelop.gvsig.mini.views.overlay.SlidingDrawer2.OnDrawerCloseListener;
 import es.prodevelop.gvsig.mini.views.overlay.SlidingDrawer2.OnDrawerOpenListener;
 import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
 import es.prodevelop.tilecache.layers.Layers;
 
 public class MapPOI extends Map implements POIProviderChangedListener {
 
 	ProgressBar loadingResults;
 	public boolean isPOISlideShown = false;
 	PerstClusterPOIOverlay p;
 	private SlidingDrawer2 sliding;
 
 	public final static int SEARCH_MENU = 0;
 	public final static int ADV_SEARCH_MENU = 1;
 	public final static int MY_LOC_MENU = 2;
 	public final static int FAV_MENU = 3;
 	public final static int NEAR_LOC_MENU = 4;
 	public final static int SETTINGS_MENU = 6;
 	public final static int MAPS_MENU = 5;
 	public final static int NAV_MENU = 7;
 	public final static int LICENSE_MENU = 8;
 	public final static int ABOUT_MENU = 9;
 
 	MenuItem locationItem;
 	MenuItem searchItem;
 	MenuItem advSearchItem;
 	MenuItem settingsItem;
 	MenuItem nagItem;
 	MenuItem favItem;
 	MenuItem nearLocItem;
 
 	private boolean poiProviderEnabled = false;
 
 	/**
 	 * Instantiates the UI: TileRaster, ZoomControls, SlideBar in a
 	 * RelativeLayout
 	 * 
 	 * @param savedInstanceState
 	 */
 	public void loadUI(Bundle savedInstanceState) {
 		try {
 			super.loadUI(savedInstanceState);
 			osmap.setPoiProviderFailListener(this);
 			final LayoutInflater factory = LayoutInflater.from(this);
 			sliding = (SlidingDrawer2) factory.inflate(R.layout.slide, null);
 			sliding.setOnDrawerOpenListener(new OnDrawerOpenListener() {
 
 				@Override
 				public void onDrawerOpened() {
 					try {
 						((Button) sliding.getHandle())
 								.setBackgroundResource(R.drawable.slide_down_icon);
 						isPOISlideShown = true;
 						osmap.pauseDraw();
 						if (MapPOI.this.c != null)
 							c.setVisibility(View.GONE);
 						z.setVisibility(View.INVISIBLE);
 						if (s.getVisibility() == View.VISIBLE) {
 							wasScaleBarVisible = true;
 						} else {
 							wasScaleBarVisible = false;
 						}
 						s.setVisibility(View.INVISIBLE);
 					} catch (Exception e) {
 						Log.e("", "onDrawerOpened Error");
 					}
 				}
 			});
 
 			sliding.setOnDrawerCloseListener(new OnDrawerCloseListener() {
 
 				@Override
 				public void onDrawerClosed() {
 					try {
 						((Button) sliding.getHandle())
 								.setBackgroundResource(R.drawable.slide_up_icon);
 						isPOISlideShown = false;
 						z.setVisibility(View.VISIBLE);
 						if (wasScaleBarVisible)
 							s.setVisibility(View.VISIBLE);
 						PerstClusterPOIOverlay poiOverlay = (PerstClusterPOIOverlay) osmap
 								.getOverlay(PerstClusterPOIOverlay.DEFAULT_NAME);
 						if (poiOverlay != null)
 							poiOverlay.setCategories(POICategories.selected);
 
 						if (!POICategories.bookmarkSelected)
 							osmap.removeOverlay(BookmarkOverlay.DEFAULT_NAME);
 						else
 							osmap.addOverlay(new BookmarkOverlay(MapPOI.this,
 									osmap, BookmarkOverlay.DEFAULT_NAME));
 
 						osmap.getOverlay(ResultSearchOverlay.DEFAULT_NAME)
 								.setVisible(POICategories.resultSearchSelected);
 
 						osmap.resumeDraw();
 
 						// Map.this.osmap.poiOverlay.onTouchEvent(null, null);
 					} catch (Exception e) {
 						// Log.e("", e.getMessage());
 					}
 				}
 			});
 
 			final RelativeLayout.LayoutParams sParams = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.FILL_PARENT,
 					RelativeLayout.LayoutParams.FILL_PARENT);
 
 			loadingResults = new ProgressBar(this);
 			loadingResults.setIndeterminate(true);
 			loadingResults.setBackgroundColor(color.background_dark);
 			loadingResults.setVisibility(View.INVISIBLE);
 			final RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.WRAP_CONTENT,
 					RelativeLayout.LayoutParams.WRAP_CONTENT);
 			lParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 			lParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 			//
 			rl.addView(sliding, sParams);
 			rl.addView(loadingResults, lParams);
 
 			/* Creating the main Overlay */
 			{
 
 				try {
 					if (p == null)
 						p = new PerstClusterPOIOverlay(this, osmap,
 								PerstClusterPOIOverlay.DEFAULT_NAME, true);
 					this.osmap.addOverlay(p);
 
 				} catch (Exception e) {
 					Log.e("", e.getMessage());
 				}
 
 				this.osmap.addOverlay(new ResultSearchOverlay(this, osmap,
 						ResultSearchOverlay.DEFAULT_NAME));
 			}
 
 		} catch (Exception e) {
 		} catch (OutOfMemoryError ou) {
 			System.gc();
 		}
 	}
 
 	public void processActionSearch(Intent i) {
 		if (Intent.ACTION_SEARCH.equals(i.getAction())) {
 			String q = i.getStringExtra(SearchManager.QUERY);
 			if (q == null)
 				q = i.getDataString();
 			final String query = q;
 			Thread t = new Thread(new Runnable() {
 				public void run() {
 					try {
 						handler.sendEmptyMessage(Map.SHOW_LOADING);
 						if (query != null && query.length() > 0) {
 							final ResultSearchOverlay overlay = (ResultSearchOverlay) osmap
 									.getOverlay(ResultSearchOverlay.DEFAULT_NAME);
 
 							ArrayList list = ((PerstOsmPOIProvider) POIProviderManager
 									.getInstance().getPOIProvider())
 									.fullTextSearch(query);
 							if (list != null && list.size() > 0) {
 								overlay.onSearchResults(query, list);
 								overlay.setVisible(true);
 								POICategories.resultSearchSelected = true;
 								Message msg = Message.obtain();
 								msg.what = Map.SHOW_TOAST;
 								msg.obj = String.format(
 										MapPOI.this.getResources().getString(
 												R.string.num_results_found),
 										list.size());
 								MapPOI.this.handler.sendMessage(msg);
 								CategoriesListView c = (CategoriesListView) sliding
 										.findViewById(R.id.categories_list_view);
 								CheckBoxBulletAdapter chk = ((CheckBoxBulletAdapter) c
 										.getAdapter());
 								chk.selected[1] = true;
 							} else {
 								overlay.setVisible(false);
 								POICategories.resultSearchSelected = false;
 								Message msg = Message.obtain();
 								msg.what = Map.SHOW_TOAST;
 								msg.obj = MapPOI.this.getResources().getString(
 										R.string.no_results);
 								MapPOI.this.handler.sendMessage(msg);
 							}
 						} else {
 							Message msg = Message.obtain();
 							msg.what = Map.SHOW_TOAST;
 							msg.obj = MapPOI.this.getResources().getString(
 									R.string.fill_text);
 							MapPOI.this.handler.sendMessage(msg);
 						}
 					} catch (Exception e) {
 						Log.e("", "processActionSearch error");
 					} finally {
 						handler.sendEmptyMessage(Map.HIDE_LOADING);
 					}
 				}
 			});
 			t.start();
 			// Intent newIntent = new Intent(this, ResultSearchActivity.class);
 			// newIntent.putExtra(SearchActivity.HIDE_AUTOTEXTVIEW, true);
 			// newIntent.putExtra(ResultSearchActivity.QUERY, query.toString());
 			// fillSearchCenter(newIntent);
 			// startActivity(newIntent);
 			// return;
 		}
 	}
 
 	public void setLoadingVisible(boolean visible) {
 		if (loadingResults != null)
 			if (visible)
 				loadingResults.setVisibility(View.VISIBLE);
 			else
 				loadingResults.setVisibility(View.INVISIBLE);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		try {
 			if (keyCode == KeyEvent.KEYCODE_BACK) {
 				if (this.sliding.isOpened()) {
 					this.sliding.close();
 					return true;
 				}
 
 				if (super.onKeyDown(keyCode, event))
 					return true;
 			}
 			return super.onKeyDown(keyCode, event);
 		} catch (Exception e) {
 			return false;
 		}
 		// return false;
 
 	}
 
 	public boolean isPOISlideShown() {
 		return isPOISlideShown;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		boolean result = true;
 		try {
 			switch (item.getItemId()) {
 			case SEARCH_MENU:
 				try {
 					onSearchRequested();
 				} catch (Exception e) {
 
 				}
 				break;
 			case ADV_SEARCH_MENU:
 				try {
 					Intent mainIntent = new Intent(this,
 							SearchExpandableActivity.class);
 					// Point center = this.osmap.getMRendererInfo().getCenter();
 					fillSearchCenter(mainIntent);
 					this.startActivity(mainIntent);
 				} catch (Exception e) {
 					// log.log(Level.SEVERE, "OpenWebsite: ", e);
 				}
 				break;
 			case MY_LOC_MENU:
 				try {
 					if (this.mMyLocationOverlay.mLocation == null
 							|| (this.mMyLocationOverlay.mLocation
 									.getLatitudeE6() == 0 && this.mMyLocationOverlay.mLocation
 									.getLongitudeE6() == 0)) {
 						Toast.makeText(this, R.string.Map_24, Toast.LENGTH_LONG)
 								.show();
 						return true;
 					}
 					this.osmap
 							.adjustViewToAccuracyIfNavigationMode(this.mMyLocationOverlay.mLocation.acc);
 					this.osmap
 							.setMapCenterFromLonLat(
 									this.mMyLocationOverlay.mLocation
 											.getLongitudeE6() / 1E6,
 									this.mMyLocationOverlay.mLocation
 											.getLatitudeE6() / 1E6);
 
 				} catch (Exception e) {
 
 				}
 				break;
 			case FAV_MENU:
 				try {
 					Point center = mMyLocationOverlay.getLocationLonLat();
 					double[] lonlat = ConversionCoords.reproject(center.getX(),
 							center.getY(), CRSFactory.getCRS("EPSG:4326"),
 							CRSFactory.getCRS("EPSG:900913"));
 					InvokeIntents.launchListBookmarks(this, lonlat);
 				} catch (Exception e) {
 
 				}
 				break;
 			case NEAR_LOC_MENU:
 				if (this.mMyLocationOverlay.mLocation == null
 						|| (this.mMyLocationOverlay.mLocation.getLatitudeE6() == 0 && this.mMyLocationOverlay.mLocation
 								.getLongitudeE6() == 0)) {
 					Toast.makeText(this, R.string.Map_24, Toast.LENGTH_LONG)
 							.show();
 				} else {
 					Point p = new Point(
 							this.mMyLocationOverlay.mLocation.getLongitudeE6() / 1E6,
 							this.mMyLocationOverlay.mLocation.getLatitudeE6() / 1E6);
 					InvokeIntents.findPOISNear(this, p.toShortString(6));
 				}
 				break;
 			case SETTINGS_MENU:
 				Intent i = new Intent(this, SettingsActivity.class);
 				startActivity(i);
 				break;
 			case MAPS_MENU:
 				try {
 					viewLayers();
 				} catch (Exception e) {
 
 				}
 				break;
 			case NAV_MENU:
 				try {
 					recenterOnGPS = !recenterOnGPS;
 
 					if (locationItem != null)
 						locationItem.setEnabled(!recenterOnGPS);
 					if (searchItem != null)
 						searchItem.setEnabled(!recenterOnGPS);
 					if (advSearchItem != null)
 						advSearchItem.setEnabled(!recenterOnGPS);
 					if (settingsItem != null)
 						settingsItem.setEnabled(!recenterOnGPS);
 
 					// myGPSButton.setEnabled(!recenterOnGPS);
 
 					if (!recenterOnGPS) {
 						z.setVisibility(View.VISIBLE);
 						nagItem.setIcon(R.drawable.menu_navigation);
 
 					} else {
 						z.setVisibility(View.INVISIBLE);
 						nagItem.setIcon(R.drawable.menu_navigation_off);
 					}
 
 					//
 					if (!navigation) {
 
 						this.initializeSensor(this, true);
 						this.showNavigationModeAlert();
 					} else {
 						try {
 							if (Settings.getInstance().getBooleanValue(
 									getText(R.string.settings_key_orientation)
 											.toString()))
 								this.stopSensor(this);
 						} catch (Exception e) {
 
 						}
 						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 						navigation = false;
 						osmap.setKeepScreenOn(false);
 					}
 
 					//
 					// osmap.switchPanMode();
 					// if (osmap.isPanMode()) {
 					// item.setIcon(R.drawable.mv_rectangle).setTitle(
 					// R.string.Map_6);
 					// } else {
 					// item.setIcon(R.drawable.mv_pan).setTitle(R.string.Map_7);
 					// }
 				} catch (Exception e) {
 					// log.log(Level.SEVERE, "switchPanMode: ", e);
 				}
 				break;
 			case LICENSE_MENU:
 				try {
 					showLicense();
 				} catch (Exception e) {
 
 				}
 				break;
 			case ABOUT_MENU:
 				try {
 					showAboutDialog();
 				} catch (Exception e) {
 
 				}
 				break;
 			default:
 				break;
 			}
 		} catch (Exception e) {
 
 		}
 		return result;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		try {
 			if (favItem != null)
 				favItem.setEnabled(poiProviderEnabled);
 
 			if (nearLocItem != null)
 				nearLocItem.setEnabled(poiProviderEnabled);
 
 			if (searchItem != null)
 				searchItem.setEnabled(poiProviderEnabled);
 
 			if (advSearchItem != null)
 				advSearchItem.setEnabled(poiProviderEnabled);
 			return super.onPrepareOptionsMenu(menu);
 		} catch (Exception e) {
 			Log.e("", "Error on prepare menu");
 			return false;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu pMenu) {
 		try {
 
 			// searchItem = pMenu.add(0, SEARCH_MENU, SEARCH_MENU,
 			// R.string.alert_dialog_text_search).setIcon(
 			// R.drawable.menu00);
 
 			advSearchItem = pMenu.add(0, ADV_SEARCH_MENU, ADV_SEARCH_MENU,
 					R.string.alert_dialog_text_search).setIcon(
 					R.drawable.menu00);
 
 			locationItem = pMenu.add(0, MY_LOC_MENU, MY_LOC_MENU,
 					R.string.Map_4).setIcon(R.drawable.menu_location);
 
 			favItem = pMenu.add(0, FAV_MENU, FAV_MENU, R.string.bookmark_title)
 					.setIcon(R.drawable.bookmark_38);// .setEnabled(connection);
 
 			nearLocItem = pMenu.add(0, NEAR_LOC_MENU, NEAR_LOC_MENU,
 					R.string.nearest_places).setIcon(R.drawable.poi_near);
 
 			settingsItem = pMenu.add(0, SETTINGS_MENU, SETTINGS_MENU,
 					R.string.Map_31).setIcon(
 					android.R.drawable.ic_menu_preferences);
 
 			pMenu.add(0, MAPS_MENU, MAPS_MENU, R.string.Map_5).setIcon(
 					R.drawable.menu02);
 
 			nagItem = pMenu.add(0, NAV_MENU, NAV_MENU, R.string.Map_Navigator)
 					.setIcon(R.drawable.menu_navigation).setEnabled(connection);
 
 			pMenu.add(0, LICENSE_MENU, LICENSE_MENU, R.string.Map_29);
 
 			pMenu.add(0, ABOUT_MENU, ABOUT_MENU, R.string.Map_28);
 		} catch (Exception e) {
 			// log.log(Level.SEVERE, "onCreateOptionsMenu: ", e);
 		}
 		return true;
 	}
 
 	/**
 	 * Starts the LayersActivity with the MapState.gvTilesPath file
 	 */
 	public void viewLayers() {
 		try {
 			// log.log(Level.FINE, "Launching load layers activity");
 			Intent intent = new Intent(this, POILayersActivity.class);
 
 			intent.putExtra("loadLayers", true);
 			intent.putExtra("gvtiles", mapState.gvTilesPath);
 
 			startActivityForResult(intent, 1);
 		} catch (Exception e) {
 			// log.log(Level.SEVERE, "viewLayers: ", e);
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent i) {
 		try {
 			OfflineMapsReader reader = new OfflineMapsReader();
 
 			ArrayList<String> offmaps = reader.readOfflineMaps();
 
 			final int size = offmaps.size();
 
 			for (int j = 0; j < size; j++) {
 				Layers.getInstance().addLayer(offmaps.get(j));
 			}
 			Layers.getInstance().persist();
 
 			if (i == null) {
 				Log.d("Map", "intent is null");
 				return;
 			}
 
 			super.onNewIntent(i);
 		} catch (Exception e) {
 			Log.e("", "error no New Intent");
 		}
 	}
 
 	@Override
 	public void onPOIProviderFail() {
 		sliding.setVisibility(View.INVISIBLE);
 		poiProviderEnabled = false;
 		// instantiateActionBar();
 	}
 
 	@Override
 	public void onNewPOIProvider() {
 		sliding.setVisibility(View.VISIBLE);
 		poiProviderEnabled = true;
 		// instantiateActionBar();
 	}
 
 	public void addLayersActivityAction() {
 		try {
 			// if (poiProviderEnabled)
 			getActionbar().addAction(new POISlideAction());
 			super.addLayersActivityAction();
 		} catch (Exception e) {
 			if (e != null && e.getMessage() != null) {
 				Log.e("", e.getMessage());
 			}
 		}
 	}
 
 	public void addSearchAction() {
 		// if (poiProviderEnabled)
 		getActionbar().addAction(new SearchAction());
 	}
 
 	// public void instantiateActionBar() {
 	// try {
 	// ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
 	//
 	// actionBar.setTitle(R.string.action_bar_title);
 	// setActionbar(actionBar);
 	//
 	// addMyLocationAction();
 	// addLayersActivityAction();
 	// addSearchAction();
 	// } catch (Exception e) {
 	// if (e != null && e.getMessage() != null) {
 	// Log.e("", e.getMessage());
 	// }
 	// }
 	// }
 
 	private class POISlideAction extends AbstractAction {
 
 		public POISlideAction() {
 			super(R.drawable.gd_action_bar_locate);
 		}
 
 		@Override
 		public void performAction(View view) {
 			try {
 				if (poiProviderEnabled)
					if (MapPOI.this.isPOISlideShown)
						MapPOI.this.sliding.close();
					else
						MapPOI.this.sliding.open();
 				else
 					Toast.makeText(MapPOI.this, R.string.no_poi_database,
 							Toast.LENGTH_LONG).show();
 			} catch (Exception e) {
 				if (e != null && e.getMessage() != null) {
 					Log.e("", e.getMessage());
 				}
 			}
 		}
 	}
 
 	private class SearchAction extends AbstractAction {
 
 		public SearchAction() {
 			super(R.drawable.gd_action_bar_search);
 		}
 
 		@Override
 		public void performAction(View view) {
 			try {
 				if (poiProviderEnabled) {
 					Intent mainIntent = new Intent(MapPOI.this,
 							SearchExpandableActivity.class);
 					// Point center = this.osmap.getMRendererInfo().getCenter();
 					fillSearchCenter(mainIntent);
 					MapPOI.this.startActivity(mainIntent);
 				} else {
 					Toast.makeText(MapPOI.this, R.string.no_poi_database,
 							Toast.LENGTH_LONG).show();
 				}
 
 			} catch (Exception e) {
 				if (e != null && e.getMessage() != null) {
 					Log.e("", e.getMessage());
 				}
 			}
 		}
 	}
 }
