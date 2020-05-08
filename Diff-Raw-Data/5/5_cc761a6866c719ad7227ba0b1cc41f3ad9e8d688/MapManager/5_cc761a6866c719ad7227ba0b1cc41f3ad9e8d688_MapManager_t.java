 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.dt.custom.map;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 import eu.trentorise.smartcampus.dt.DTParamsHelper;
 import eu.trentorise.smartcampus.dt.R;
 import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
 import eu.trentorise.smartcampus.dt.fragments.home.HomeFragment;
 import eu.trentorise.smartcampus.dt.model.BaseDTObject;
 
 public class MapManager {
 
 	public static int ZOOM_DEFAULT = 15;
 	private static MapView mapView;
 	private static GeoPoint DEFAULT_POINT = new GeoPoint((int) (46.0696727540531 * 1E6), (int) (11.1212700605392 * 1E6));
 
 	public static void iniWithParam() {
 		int zoom = DTParamsHelper.getZoomLevelMap();
 		if (zoom != 0) {
			ZOOM_DEFAULT = zoom;
 		}
 		List<Double> centerMap = DTParamsHelper.getCenterMap();
 		if (centerMap != null) {
 			Double latitute = centerMap.get(0);
 			Double longitude = centerMap.get(1);
			DEFAULT_POINT = new GeoPoint((int) (latitute * 1E6),(int) (longitude *1E6));
 		}
 	}
 
 	public static GeoPoint trento() {
 		return DEFAULT_POINT;
 	}
 
 	public static MapView getMapView() {
 		return mapView;
 	}
 
 	public static void setMapView(MapView mapView) {
 		MapManager.mapView = mapView;
 		MapManager.mapView.setClickable(true);
 		MapManager.mapView.setBuiltInZoomControls(true);
 
 	}
 
 	public static GeoPoint requestMyLocation(Context ctx) {
 		// LocationManager locationManager =
 		// (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
 		// if (locationManager == null) return null;
 		// Location last = null;
 		// try {
 		// last =
 		// locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		// } catch (Exception e) {}
 		// if (last == null) {
 		// try {
 		// last =
 		// locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		// } catch (Exception e) {
 		// return null;
 		// }
 		// }
 		// if (last != null) {
 		// return new GeoPoint((int)(last.getLatitude()* 1E6),
 		// (int)(last.getLongitude()* 1E6));
 		// }
 		// return null;
 		return DTHelper.getLocationHelper().getLocation();
 	}
 
 	public static OverlayItem createMe(GeoPoint point, Context mContext) {
 		OverlayItem overlayitem = new OverlayItem(point, "Me", "My location");
 		Drawable drawable = mContext.getResources().getDrawable(R.drawable.me);
 		drawable.setBounds(-drawable.getIntrinsicWidth() / 2, -drawable.getIntrinsicHeight(),
 				drawable.getIntrinsicWidth() / 2, 0);
 		overlayitem.setMarker(drawable);
 		return overlayitem;
 	}
 
 	public static double[] computeCenter(List<BaseDTObject> list) {
 		double[] ll = null, rr = null;
 		if (list != null) {
 			for (BaseDTObject o : list) {
 				if (o.getLocation() == null)
 					continue;
 				if (ll == null) {
 					ll = o.getLocation().clone();
 					rr = o.getLocation().clone();
 				} else {
 					ll[0] = Math.min(ll[0], o.getLocation()[0]);
 					ll[1] = Math.max(ll[1], o.getLocation()[1]);
 
 					rr[0] = Math.max(rr[0], o.getLocation()[0]);
 					rr[1] = Math.min(rr[1], o.getLocation()[1]);
 				}
 			}
 		}
 		if (ll != null) {
 			return new double[] { (ll[0] + rr[0]) / 2, (ll[1] + rr[1]) / 2 };
 		}
 		return null;
 	}
 
 	public static void switchToMapView(ArrayList<BaseDTObject> list, Fragment src) {
 		FragmentTransaction fragmentTransaction = src.getActivity().getSupportFragmentManager().beginTransaction();
 		HomeFragment fragment = new HomeFragment();
 		Bundle args = new Bundle();
 		args.putSerializable(HomeFragment.ARG_OBJECTS, list);
 		fragment.setArguments(args);
 		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 		// fragmentTransaction.detach(src);
 		fragmentTransaction.replace(android.R.id.content, fragment, src.getTag());
 		fragmentTransaction.addToBackStack(fragment.getTag());
 		fragmentTransaction.commit();
 
 	}
 
 	public static void switchToMapView(String category, Fragment src) {
 		FragmentTransaction fragmentTransaction = src.getActivity().getSupportFragmentManager().beginTransaction();
 		HomeFragment fragment = new HomeFragment();
 		Bundle args = new Bundle();
 		args.putString(HomeFragment.ARG_POI_CATEGORY, category);
 		fragment.setArguments(args);
 		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 		// fragmentTransaction.detach(src);
 		fragmentTransaction.replace(android.R.id.content, fragment, src.getTag());
 		fragmentTransaction.addToBackStack(fragment.getTag());
 		fragmentTransaction.commit();
 
 	}
 
 	public static void fitMap(List<BaseDTObject> list, MapView mapView) {
 		double[] ll = null, rr = null;
 		if (list != null) {
 			for (BaseDTObject o : list) {
 				if (o.getLocation() == null)
 					continue;
 				if (ll == null) {
 					ll = o.getLocation().clone();
 					rr = o.getLocation().clone();
 				} else {
 					ll[0] = Math.min(ll[0], o.getLocation()[0]);
 					ll[1] = Math.max(ll[1], o.getLocation()[1]);
 
 					rr[0] = Math.max(rr[0], o.getLocation()[0]);
 					rr[1] = Math.min(rr[1], o.getLocation()[1]);
 				}
 			}
 		}
 		fit(mapView, ll, rr, list != null && list.size() > 1);
 	}
 
 	public static void fitMapWithOverlays(List<OverlayItem> list, MapView mapView) {
 		double[] ll = null, rr = null;
 		if (list != null) {
 			for (OverlayItem o : list) {
 				double[] location = new double[] { o.getPoint().getLatitudeE6() / 1E6,
 						o.getPoint().getLongitudeE6() / 1E6 };
 				if (o.getPoint() == null)
 					continue;
 				if (ll == null) {
 					ll = location;
 					rr = location.clone();
 				} else {
 					ll[0] = Math.min(ll[0], location[0]);
 					ll[1] = Math.max(ll[1], location[1]);
 
 					rr[0] = Math.max(rr[0], location[0]);
 					rr[1] = Math.min(rr[1], location[1]);
 				}
 			}
 		}
 		fit(mapView, ll, rr, list != null && list.size() > 1);
 	}
 
 	private static void fit(MapView mapView, double[] ll, double[] rr, boolean zoomIn) {
 		if (ll != null) {
 			int maxLat = (int) (ll[0] * 1E6);
 			int minLat = (int) (rr[0] * 1E6);
 			int maxLon = (int) (ll[1] * 1E6);
 			int minLon = (int) (rr[1] * 1E6);
 			mapView.getController().zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));
 			if (!zoomIn && mapView.getZoomLevel() > ZOOM_DEFAULT) {
 				mapView.getController().setZoom(ZOOM_DEFAULT);
 			}
 			mapView.getController().animateTo(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
 		}
 	}
 
 }
