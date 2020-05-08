 package fi.spanasenko.android.presenter;
 
 import android.content.Context;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.provider.Settings;
 import fi.spanasenko.android.LocationsMapActivity;
 import fi.spanasenko.android.NearbyLocationsActivity;
 import fi.spanasenko.android.R;
 import fi.spanasenko.android.instagram.InstagramApi;
 import fi.spanasenko.android.instagram.OperationCallback;
 import fi.spanasenko.android.instagram.OperationCallbackBase;
 import fi.spanasenko.android.instagram.VoidOperationCallback;
 import fi.spanasenko.android.utils.UserSettings;
 import fi.spanasenko.android.view.ILoginView;
 
 /**
  * LoginPresenter
  * Implementation of the login presenter.
  */
 public class LoginPresenter extends PresenterBase<ILoginView> implements ILoginPresenter {
 
     private InstagramApi mInstagram;
 
     /**
      * Constructor.
      * @param view Interface to parent view.
      */
     public LoginPresenter(ILoginView view) {
         super(view, view.getContext());
         mInstagram = InstagramApi.getInstance(view.getContext());
     }
 
     @Override
     public void checkAuthorizationAndShowNextView() {
         // Check authorization status and authorize or show next view.
         if (!mInstagram.hasAccessToken()) {
             authorize();
         } else {
             openPreferredView();
         }
     }
 
     @Override
     public void checkGpsStatusAndShowNextView() {
         final LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
 
         if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
             getView().promptUser(R.string.gps_title, R.string.gps_message, android.R.string.yes, android.R.string.no,
                     new OperationCallback<String>() {
                         @Override
                         protected void onCompleted(String result) {
                             if (result.equals(getView().getStringResource(android.R.string.yes))) {
                                 // Show device settings
                                 openActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                             }
 
                             // Decision is made, now we can show next view.
                             openPreferredView();
                         }
 
                         @Override
                         protected void onError(Exception error) {
                             // Anyway show next view!
                             openPreferredView();
                         }
                     });
         } else {
             // No prompt needed, go showing nearby locations screen.
             openPreferredView();
         }
     }
 
     @Override
     public void authorize() {
         getView().showBusyDialog(R.string.wait_access_token);
 
         mInstagram.authorize(getContext(), new VoidOperationCallback(OperationCallbackBase.DispatchType.MainThread) {
             @Override
             protected void onCompleted() {
                 getView().dismissBusyDialog();
 
                 // To avoid bothering user to often this check will be issued once per authorization.
                 checkGpsStatusAndShowNextView();
             }
 
             @Override
             protected void onError(Exception error) {
                 getView().dismissBusyDialog();
 
                 getView().onError(error);
             }
         });
     }
 
     @Override
     public void logout() {
         mInstagram.logout();
     }
 
     /**
      * Opens new activity depending on user preference. It might be List or Map activity with nearby locations.
      */
     private void openPreferredView() {
         // Open user preferred view, by default it's MapView.
         boolean preferMaps = UserSettings.getInstance(getContext()).isMapPrefered();
        openActivity(preferMaps ? LocationsMapActivity.class : NearbyLocationsActivity.class);
 
         // We don't need this view to hang in back stack, so just finish it.
         getView().finish();
     }
 
 }
