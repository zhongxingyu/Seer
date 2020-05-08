 package com.strollimo.android.view;
 
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.*;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapView;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.model.*;
 import com.novoda.imageloader.core.ImageManager;
 import com.novoda.imageloader.core.model.ImageTag;
 import com.novoda.imageloader.core.model.ImageTagFactory;
 import com.strollimo.android.AppGlobals;
 import com.strollimo.android.R;
 import com.strollimo.android.StrollimoApplication;
 import com.strollimo.android.StrollimoPreferences;
 import com.strollimo.android.controller.AccomplishableController;
 import com.strollimo.android.controller.UserService;
 import com.strollimo.android.model.MapPlacesModel;
 import com.strollimo.android.model.Mystery;
 
 public class MapFragment extends Fragment {
     private View mView;
     private GoogleMap mMap;
     private LocationClient mLocationClient;
     private AccomplishableController mAccomplishableController;
     private StrollimoPreferences mPrefs;
     private ImageManager mImageManager;
     private UserService mUserService;
     private boolean firstStart = true;
     private ImageView mPlaceImage;
     private TextView mPlaceTitle;
     private View mRibbonPanel;
     private MapPlacesModel mMapPlacesModel;
     private MapView mMapView;
     private GoogleMap.OnInfoWindowClickListener onInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
         @Override
         public void onInfoWindowClick(Marker marker) {
             Log.i("BB", "on info window selected");
             launchDetailsActivity();
         }
     };
     private GoogleMap.OnMarkerClickListener mOnMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
         @Override
         public boolean onMarkerClick(Marker marker) {
             mMapPlacesModel.onMarkerClick(marker);
             displayRibbon(mMapPlacesModel.getSelectedPlace(), true);
             return false;
         }
     };
     private GoogleMap.OnMapClickListener mOnMapClickListener = new GoogleMap.OnMapClickListener() {
         @Override
         public void onMapClick(LatLng latLng) {
             hideRibbon();
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mPrefs = StrollimoApplication.getService(StrollimoPreferences.class);
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         try {
             MapsInitializer.initialize(getActivity());
         } catch (GooglePlayServicesNotAvailableException ex) {
             Log.e("BB", "Error", ex);
         }
         if (mView == null) {
             firstStart = true;
             mView = inflater.inflate(R.layout.stroll_map_layout, container, false);
             mMapView = (MapView) mView.findViewById(R.id.map);
             mAccomplishableController = ((StrollimoApplication) getActivity().getApplication()).getService(AccomplishableController.class);
             mImageManager = StrollimoApplication.getService(ImageManager.class);
             mUserService = ((StrollimoApplication) getActivity().getApplication()).getService(UserService.class);
             mPlaceImage = (ImageView) mView.findViewById(R.id.place_image);
             mPlaceTitle = (TextView) mView.findViewById(R.id.place_title);
             mRibbonPanel = mView.findViewById(R.id.ribbon_panel);
 
             mRibbonPanel.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     Log.i("BB", "on click");
                     launchDetailsActivity();
                 }
             });
 
             setSwipeToChangePlace(mRibbonPanel);
             mMapView.onCreate(savedInstanceState);
         } else {
             ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
             if (parentViewGroup != null) {
                 parentViewGroup.removeAllViews();
             }
         }
         return mView;
     }
 
     private void setSwipeToChangePlace(View dismissableRibbon) {
         dismissableRibbon.setOnTouchListener(new SwipeDismissTouchListener(
                 dismissableRibbon,
                 null,
                 new SwipeDismissTouchListener.OnDirectionalDismissCallback() {
                     @Override
                     public void onDismiss(View view, Object token, DismissDirectionType dismissDirectionType) {
                         Mystery toMystery;
                         if (dismissDirectionType == DismissDirectionType.RIGHT) {
                             toMystery = mMapPlacesModel.getNextPlaceFor(getActivity(), mMapPlacesModel.getSelectedPlace());
                         } else {
                             toMystery = mMapPlacesModel.getPreviousPlaceFor(getActivity(), mMapPlacesModel.getSelectedPlace());
                         }
                         if (toMystery != null) {
                             Marker marker = mMapPlacesModel.getMarkerForPlace(toMystery);
                             marker.showInfoWindow();
                             mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 250, null);
                             mMapPlacesModel.selectMapPlaceByPlace(toMystery);
                             displayRibbon(mMapPlacesModel.getSelectedPlace(), dismissDirectionType != DismissDirectionType.RIGHT);
                         } else {
                            mMapPlacesModel.getSelectedMarker().hideInfoWindow();
                             mRibbonPanel.setVisibility(View.GONE);
                             mMapPlacesModel.hideSelectedPlace();
                         }
                     }
                 }));
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mMapView.onResume();
         mMapPlacesModel.refreshSelectedMarker();
         // TODO BB: do we need this dialog?
 //        if (mUserService.getFoundPlacesNum() == mAccomplishableController.getMysteriesCount() && mAccomplishableController.getMysteriesCount() != 0) {
 //            new DemoFinishedDialog().show(getActivity().getSupportFragmentManager(), "dialog");
 //        }
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.i("BB", "onStart");
         setUpMapIfNecessary();
 
         setUpLocationClientIfNecessary();
         setUpMapMarkersIfNecessary();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mMapView.onPause();
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.main, menu);
     }
 
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         if (menu == null) {
             return;
         }
         MenuItem addMysteryItem = menu.findItem(R.id.add_mystery);
         if (addMysteryItem != null) {
             addMysteryItem.setVisible(mPrefs.isDebugModeOn());
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.add_mystery) {
             startActivity(new Intent(getActivity(), AddMysteryActivity.class));
             return true;
         } else {
             return super.onOptionsItemSelected(item);
         }
     }
 
     private void setUpMapIfNecessary() {
         if (mMap == null) {
             mMap = mMapView.getMap();
             mMap.setMyLocationEnabled(true);
             mMap.setOnInfoWindowClickListener(onInfoWindowClickListener);
             mMap.setOnMarkerClickListener(mOnMarkerClickListener);
             mMap.setOnMapClickListener(mOnMapClickListener);
             mMap.getUiSettings().setZoomControlsEnabled(false);
             mMap.getUiSettings().setCompassEnabled(false);
         }
     }
 
     public void setUpMapMarkersIfNecessary() {
         if (mMapPlacesModel != null) {
             return;
         }
 
         mMapPlacesModel = new MapPlacesModel(mUserService);
         mMap.clear();
         for (Mystery mystery : mAccomplishableController.getAllMysteries()) {
             addPlaceToMap(mystery);
         }
     }
 
     private void addPlaceToMap(Mystery mystery) {
         BitmapDescriptor bitmapDescriptor;
         if (mUserService.isSecretCaptured(mystery.getId())) {
             bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.pink_flag);
         } else {
             bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.azure_flag);
         }
 
         Marker marker = mMap.addMarker(new MarkerOptions()
                 .position(new LatLng(mystery.getLocation().getLat(), mystery.getLocation().getLng()))
                 .title(mystery.getName()).icon(bitmapDescriptor));
         mMapPlacesModel.add(mystery, marker);
     }
 
     @Override
     public void onStop() {
         mLocationClient.disconnect();
         super.onStop();
     }
 
     private void launchDetailsActivity() {
         Mystery selectedMystery = mMapPlacesModel.getSelectedPlace();
         if (selectedMystery != null) {
             this.startActivity(DetailsActivity.createDetailsIntent(getActivity(), selectedMystery.getId()));
         }
     }
 
     private void setUpLocationClientIfNecessary() {
         if (mLocationClient == null) {
             mLocationClient = new LocationClient(getActivity(), new GooglePlayServicesClient.ConnectionCallbacks() {
                 @Override
                 public void onConnected(Bundle bundle) {
                     if (firstStart) {
                         Location loc = mLocationClient.getLastLocation();
                         Mystery mystery = mAccomplishableController.getFirstMystery();
                         LatLng latLng;
                         if (mystery != null) {
                             latLng = new LatLng(mystery.getLocation().getLat(), mystery.getLocation().getLng());
                         } else if (loc != null) {
                             latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                         } else {
                             latLng = AppGlobals.LONDON_SOMERSET_HOUSE;
                         }
                         CameraPosition pos = CameraPosition.builder().target(latLng).zoom(16f).tilt(45).build();
                         mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
                         firstStart = false;
                     }
                 }
 
                 @Override
                 public void onDisconnected() {
 
                 }
             }, new GooglePlayServicesClient.OnConnectionFailedListener() {
                 @Override
                 public void onConnectionFailed(ConnectionResult connectionResult) {
 
                 }
             }
             );
         }
         if (!mLocationClient.isConnected()) {
             mLocationClient.connect();
         }
     }
 
     private void displayRibbon(Mystery mystery, boolean fromRight) {
         Animation anim = AnimationUtils.loadAnimation(getActivity(), fromRight ? R.anim.slide_in_from_right : R.anim.slide_in_from_left);
         mRibbonPanel.setVisibility(View.VISIBLE);
 
         ImageTagFactory imageTagFactory = ImageTagFactory.newInstance(800, 600, R.drawable.closed);
         imageTagFactory.setAnimation(android.R.anim.fade_in);
         ImageTag tag = imageTagFactory.build(mystery.getImgUrl(), getActivity());
         mPlaceImage.setTag(tag);
         mImageManager.getLoader().load(mPlaceImage);
 
         mPlaceTitle.setText(mystery.getName().toUpperCase());
         mRibbonPanel.startAnimation(anim);
     }
 
     private void hideRibbon() {
         if (mMapPlacesModel.getSelectedPlace() != null) {
             Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_to_left);
             anim.setAnimationListener(new Animation.AnimationListener() {
                 @Override
                 public void onAnimationStart(Animation animation) {
 
                 }
 
                 @Override
                 public void onAnimationEnd(Animation animation) {
                     mRibbonPanel.setVisibility(View.GONE);
                 }
 
                 @Override
                 public void onAnimationRepeat(Animation animation) {
 
                 }
             });
             mRibbonPanel.startAnimation(anim);
         }
         mMapPlacesModel.hideSelectedPlace();
     }
 
 }
