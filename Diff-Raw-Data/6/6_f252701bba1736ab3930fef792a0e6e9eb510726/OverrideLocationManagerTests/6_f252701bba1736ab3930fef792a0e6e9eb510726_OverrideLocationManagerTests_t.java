 package android.override.tests;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.ILocationManager;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.OverrideLocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 public class OverrideLocationManagerTests extends AndroidTestCase {
 
   private static final String TEST_PROVIDER = "test";
   private OverrideLocationManager mManager;
   private LocationManager mLocationManager;
 
   @Override
   protected void tearDown() throws Exception {
     super.tearDown();
     mLocationManager.setTestProviderEnabled(TEST_PROVIDER, false);
     mLocationManager.removeTestProvider(TEST_PROVIDER);
   }
 
   @Override
   protected void setUp() throws Exception {
     super.setUp();
     final Context context = getContext();
     final IBinder binder = ServiceManager.getService(Context.LOCATION_SERVICE);
     final ILocationManager service = ILocationManager.Stub.asInterface(binder);
 
     mManager = new OverrideLocationManager(context, service);
 
     mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
 
     if (mLocationManager.getProvider(TEST_PROVIDER) != null) {
       mLocationManager.removeTestProvider(TEST_PROVIDER);
     }
     mLocationManager.addTestProvider(
         TEST_PROVIDER, false, false, false, false, true, true, true,
         Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
     mLocationManager.setTestProviderEnabled(TEST_PROVIDER, true);
 
 
   }
 
   private void setTestLocation(double lat, double lon) {
     Location location = new Location(TEST_PROVIDER);
     location.setLatitude(lat);
     location.setLongitude(lon);
     location.setTime(System.currentTimeMillis());
     mLocationManager.setTestProviderLocation(TEST_PROVIDER, location);
   }
 
 
   public void testAttachListener() {
 
     LocationListener listener = new LocationListener() {
       @Override
       public void onLocationChanged(Location location) {
       }
 
       @Override
       public void onStatusChanged(String s, int i, Bundle bundle) {
       }
 
       @Override
       public void onProviderEnabled(String s) {
       }
 
       @Override
       public void onProviderDisabled(String s) {
       }
 
     };
     assertFalse(mManager.checkListenerRegistered(listener));
 
     mManager.requestLocationUpdates(TEST_PROVIDER, 0, 0, listener);
     assertTrue(mManager.checkListenerRegistered(listener));
 
     mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
     assertTrue(mManager.checkListenerRegistered(listener));
 
     mManager.removeUpdates(listener);
     assertFalse(mManager.checkListenerRegistered(listener));
   }
 
   public void testReleaseCommand() throws InterruptedException {
     Bundle command_release = new Bundle();
 
     command_release.putInt("COMMAND", OverrideLocationManager.COMMAND_RELEASE);
     command_release.putString("PACKAGE", getContext().getPackageName());
 
     try {
       mManager.getCommandListener().onCommand(command_release);
     } catch (RemoteException ex) {
       Log.d("TEST", "RemoteException");
     }
 
    assertEquals(mManager.getCommandState(), OverrideLocationManager.COMMAND_RELEASE);
 
   }
 }
