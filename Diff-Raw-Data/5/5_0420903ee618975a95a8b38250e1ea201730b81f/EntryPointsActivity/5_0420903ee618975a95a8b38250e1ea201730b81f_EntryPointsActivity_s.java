 /**
  * 
  */
 package fr.utc.nf33.ins;
 
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.widget.ArrayAdapter;
 import fr.utc.nf33.ins.location.Building;
 import fr.utc.nf33.ins.location.CloseBuildingsService;
 import fr.utc.nf33.ins.location.CloseBuildingsService.LocalBinder;
 import fr.utc.nf33.ins.location.LocationHelper;
 import fr.utc.nf33.ins.location.LocationIntent;
 import fr.utc.nf33.ins.location.SnrService;
 
 /**
  * 
  * @author
  * 
  */
 public final class EntryPointsActivity extends ListActivity {
   //
   private ServiceConnection mCloseBuildingsConnection;
   //
   private CloseBuildingsService mCloseBuildingsService;
   //
   private BroadcastReceiver mNewCloseBuildingsReceiver;
   //
   private BroadcastReceiver mNewSnrReceiver;
   //
   private ServiceConnection mSnrConnection;
 
   @Override
   protected final void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.activity_entry_points);
   }
 
   @Override
   protected final void onStart() {
     super.onStart();
 
     // Connect to the SNR Service.
     Intent snrIntent = new Intent(this, SnrService.class);
     mSnrConnection = new ServiceConnection() {
       @Override
       public final void onServiceConnected(ComponentName name, IBinder service) {
 
       }
 
       @Override
       public final void onServiceDisconnected(ComponentName name) {
 
       }
     };
     bindService(snrIntent, mSnrConnection, Context.BIND_AUTO_CREATE);
 
     // Connect to the Close Buildings Service.
     Intent closeBuildingsIntent = new Intent(this, CloseBuildingsService.class);
     mCloseBuildingsConnection = new ServiceConnection() {
       @Override
       public final void onServiceConnected(ComponentName name, IBinder service) {
         mCloseBuildingsService = ((LocalBinder) service).getService();
 
         List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
         if (buildings == null) return;
         setListAdapter(new ArrayAdapter<Building>(EntryPointsActivity.this,
            R.id.entry_points_list_item_text, buildings));
       }
 
       @Override
       public final void onServiceDisconnected(ComponentName name) {
 
       }
     };
     bindService(closeBuildingsIntent, mCloseBuildingsConnection, Context.BIND_AUTO_CREATE);
 
     // Register receivers.
     LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
 
     mNewCloseBuildingsReceiver = new BroadcastReceiver() {
       @Override
       public final void onReceive(Context context, Intent intent) {
         List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
         if (buildings == null) return;
         setListAdapter(new ArrayAdapter<Building>(EntryPointsActivity.this,
            R.id.entry_points_list_item_text, buildings));
       }
     };
     lbm.registerReceiver(mNewCloseBuildingsReceiver,
         LocationIntent.NewCloseBuildings.newIntentFilter());
 
     mNewSnrReceiver = new BroadcastReceiver() {
       @Override
       public final void onReceive(Context context, Intent intent) {
         float snr = intent.getFloatExtra(LocationIntent.NewSnr.EXTRA_SNR, 0);
         List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
         if (LocationHelper.shouldGoIndoor(snr, buildings))
           startActivity(new Intent(EntryPointsActivity.this, IndoorActivity.class));
       }
     };
     lbm.registerReceiver(mNewSnrReceiver, LocationIntent.NewSnr.newIntentFilter());
   }
 
   @Override
   protected final void onStop() {
     super.onStop();
 
     // Unregister receivers.
     LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
     lbm.unregisterReceiver(mNewCloseBuildingsReceiver);
     mNewCloseBuildingsReceiver = null;
     lbm.unregisterReceiver(mNewSnrReceiver);
     mNewSnrReceiver = null;
 
     // Disconnect from the Close Buildings Service.
     unbindService(mCloseBuildingsConnection);
     mCloseBuildingsConnection = null;
     mCloseBuildingsService = null;
 
     // Disconnect from the SNR Service.
     unbindService(mSnrConnection);
     mSnrConnection = null;
   }
 }
