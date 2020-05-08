 package fi.spanasenko.android.presenter;
 
 import android.content.BroadcastReceiver;
 import android.content.Intent;
 import android.content.IntentFilter;
 import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
 import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
 import fi.spanasenko.android.ImageGalleryActivity;
 import fi.spanasenko.android.R;
 import fi.spanasenko.android.instagram.InstagramApi;
 import fi.spanasenko.android.instagram.OperationCallback;
 import fi.spanasenko.android.instagram.OperationCallbackBase;
 import fi.spanasenko.android.model.Location;
 import fi.spanasenko.android.utils.LocationBroadcastReceiver;
 import fi.spanasenko.android.utils.Utils;
 import fi.spanasenko.android.view.INearbyLocationsView;
 
 /**
  * NearbyLocationPresenter
  * Implementation of nearby locations presenter interface.
  */
 public class NearbyLocationPresenter extends PresenterBase<INearbyLocationsView> implements INearbyLocationsPresenter,
         LocationBroadcastReceiver.LocationChangedListener {
 
     private InstagramApi mInstagram;
     private BroadcastReceiver mLocationReceiver;
     private LocationInfo mLastKnownLocation;
 
     /**
      * Constructor matching parent.
      * @param view Parent view interface.
      */
     public NearbyLocationPresenter(INearbyLocationsView view) {
         super(view, view.getContext());
 
         mInstagram = InstagramApi.getInstance(getContext());
         mLocationReceiver = new LocationBroadcastReceiver(this);
     }
 
     @Override
     public void loadLocations() {
         getView().showBusyDialog(R.string.wait_locations);
 
         // Asynchronously request locations from Instagram.
         mInstagram.fetchNearbyLocations(getLastKnownLocation().lastLat, getLastKnownLocation().lastLong,
                 new OperationCallback<Location[]>(OperationCallbackBase.DispatchType.MainThread) {
                     @Override
                     protected void onCompleted(Location[] result) {
                         getView().dismissBusyDialog();
                         getView().updateLocations(result);
                     }
 
                     @Override
                     protected void onError(Exception error) {
                         getView().dismissBusyDialog();
                         onError(error);
                     }
                 });
     }
 
     @Override
     public void registerLocationObserver() {
         // Register for location broadcasts.
         getContext().registerReceiver(mLocationReceiver, new IntentFilter(LocationBroadcastReceiver.ACTION));
 
     }
 
     @Override
     public void unregisterLocationObserver() {
         getContext().unregisterReceiver(mLocationReceiver);
     }
 
     @Override
     public void openLocation(Location location) {
         // Show selected location in gallery view which shows available media from selected location.
         Intent showRecentMedia = new Intent(getContext(), ImageGalleryActivity.class);
         showRecentMedia.putExtra(ImageGalleryActivity.EXTRA_LOCATION_ID, location.getId());
         openActivity(showRecentMedia);
     }
 
     @Override
     public LocationInfo getLastKnownLocation() {
         if (mLastKnownLocation == null) {
             mLastKnownLocation = new LocationInfo(getView().getBaseContext());
         }
 
         return mLastKnownLocation;
     }
 
     @Override
     public void onLocationReceived(LocationInfo locationInfo) {
         LocationInfo oldLocation = mLastKnownLocation;
         mLastKnownLocation = locationInfo;
 
        float distance = Utils.calculateDistance(locationInfo.lastLat, locationInfo.lastLong,
                oldLocation.lastLat, oldLocation.lastLong);
         if (locationInfo.lastAccuracy < oldLocation.lastAccuracy
                && distance > LocationLibraryConstants.MINIMUM_DISTANCE) {
             // Only update if location changed reasonably
             loadLocations();
         }
     }
 }
