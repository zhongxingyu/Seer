 package com.lifecity.felux;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.*;
 import android.util.Log;
 import android.view.WindowManager;
 import android.hardware.usb.UsbAccessory;
 import android.hardware.usb.UsbManager;
 import com.lifecity.felux.items.Item;
 import com.lifecity.felux.lights.DmxColorLight;
 import com.lifecity.felux.lights.DmxGroupLight;
 import com.lifecity.felux.lights.DmxLight;
 import com.lifecity.felux.lights.Light;
 import com.lifecity.felux.scenes.DelayScene;
 import com.lifecity.felux.scenes.LightScene;
 import com.lifecity.felux.scenes.MidiScene;
 import com.lifecity.felux.scenes.Scene;
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * An activity representing a list of Lights. This activity
  * has different presentations for handset and tablet-size devices. On
  * handsets, the activity presents a list of items, which when touched,
  * item details. On tablets, the activity presents the list of items and
  * item details side-by-side using two vertical panes.
  * <p>
  * The activity makes heavy use of fragments. The list of items is a
  * {@link SceneListFragment} and the item details
  * (if present) is a {@link SceneDetailFragment}.
  * <p>
  * This activity also implements the required
  * to listen for item selections.
  */
 public class MainActivity extends FragmentActivity implements ItemListCallbacks<Item> {
     /*
     public static final String ACTIVE_TAB = "active_tab";
     public static final String ACTIVE_LIGHT = "active_light";
     public static final String ACTIVE_SCENE = "active_scene";
     */
     private static final String ACTION_USB_PERMISSION = "com.lifecity.felux.USB_PERMISSION";
     private static final String TAG = "MainActivity";
     private FragmentManager fragmentManager;
     private FeluxManager feluxManager;
     private FtdiUartFileDescriptor uartFileDescriptor;
     private SimpleHdlcOutputStreamWriter feluxWriter;
     private UsbAccessory accessory;
 
     private static Map<String, String> itemToDetailFragment = new LinkedHashMap<String, String>();
 
     static {
         itemToDetailFragment.put(LightScene.class.getCanonicalName(), LightSceneDetailFragment.class.getCanonicalName());
         itemToDetailFragment.put(DelayScene.class.getCanonicalName(), SceneDetailFragment.class.getCanonicalName());
         itemToDetailFragment.put(MidiScene.class.getCanonicalName(), MidiSceneDetailFragment.class.getCanonicalName());
         itemToDetailFragment.put(DmxLight.class.getCanonicalName(), LightDetailFragment.class.getCanonicalName());
         itemToDetailFragment.put(DmxGroupLight.class.getCanonicalName(), GroupLightDetailFragment.class.getCanonicalName());
         itemToDetailFragment.put(DmxColorLight.class.getCanonicalName(), ColorLightDetailFragment.class.getCanonicalName());
     }
 
     /**
      * Whether or not the activity is in two-pane mode, i.e. running on a tablet
      * device.
      */
     private boolean mTwoPane;
 
     private final BroadcastReceiver usbReceiver = new BroadcastReceiver()
     {
         @Override
         public void onReceive(Context context, Intent intent)
         {
             String action = intent.getAction();
             if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
             {
                 Log.d(TAG, "accessory detached");
                 finish();
             }
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         feluxManager = new FeluxManager(getPreferences(0));
         setContentView(R.layout.main_activity);
         registerReceiver(usbReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
 
         getWindow().addFlags(
                 WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                 WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                 WindowManager.LayoutParams.FLAG_FULLSCREEN
         );
 
         fragmentManager = getSupportFragmentManager();
 
         //ActionBar actionBar = getActionBar();
         //actionBar.setDisplayOptions(actionBar.getDisplayOptions() & ~ActionBar.DISPLAY_SHOW_TITLE);
         final ActionBar actionBar = getActionBar();
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
         if (findViewById(R.id.fragment_secondary) != null) {
             // The detail container view will be present only in the
             // large-screen layouts (res/values-large and
             // res/values-sw600dp). If this view is present, then the
             // activity should be in two-pane mode.
             mTwoPane = true;
 
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
             actionBar.addTab(actionBar
                     .newTab()
                     .setText(R.string.title_scene_list)
                     .setTag(SceneListFragment.class.getCanonicalName())
                     .setTabListener(
                             new TabListener(this)
                     ));
 
             actionBar.addTab(actionBar
                     .newTab()
                     .setText(R.string.title_light_list)
                     .setTag(LightListFragment.class.getCanonicalName())
                     .setTabListener(
                             new TabListener(this)
                     ));
 
             /*
             if (savedInstanceState != null) {
             }
             */
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unregisterReceiver(usbReceiver);
         closeUsbAccessory();
     }
 
     private UsbAccessory getAccessory(UsbManager manager) {
         manager = (UsbManager)getSystemService(Context.USB_SERVICE);
         UsbAccessory[] accessories = manager.getAccessoryList();
         return accessories == null ? null : accessories[0];
 
         /* TODO: Loop through all accessories */
     }
 
     private void openUsbAccessory() {
         UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
         accessory = getAccessory(manager);
         String accessoryString = accessory == null ? "null" : accessory.toString();
 
         if (accessory == null) {
             /*
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage("Please connect a felux light board")
                     .setCancelable(false)
                     .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             finish();
                         }
                     })
                     .show();
              */
         } else {
             try {
                 uartFileDescriptor = new FtdiUartFileDescriptor(manager.openAccessory(accessory));
                 FtdiUartFileDescriptor.FtdiUartOutputStream ftdiOutputStream = new FtdiUartFileDescriptor.FtdiUartOutputStream(uartFileDescriptor);
                 if (getIntent().getParcelableExtra(UsbManager.EXTRA_ACCESSORY) != null) {
                     Log.d(TAG, "setConfig");
                     ftdiOutputStream.setConfig(115200,
                             FtdiUartFileDescriptor.DATA_BITS_8,
                             FtdiUartFileDescriptor.STOP_BITS_1,
                             FtdiUartFileDescriptor.PARITY_NONE,
                             FtdiUartFileDescriptor.FLOW_CONTROL_NONE);
                 }
                 Log.d(TAG, "write");
                 feluxWriter = new SimpleHdlcOutputStreamWriter(ftdiOutputStream);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         feluxManager.setFeluxWriter(feluxWriter);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         closeUsbAccessory();
     }
 
     private void closeUsbAccessory() {
         try {
             if (feluxManager != null) {
                 feluxManager.close();
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         feluxWriter = null;
         uartFileDescriptor = null;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         openUsbAccessory();
         feluxManager.open();
 
         List<Light> lights = feluxManager.getLights();
         if (lights.size() == 0) {
             lights.add(new DmxColorLight("Screen", 1, Color.RED));
             lights.add(new DmxColorLight("Side", 5, Color.BLUE));
             lights.add(new DmxColorLight("Ceiling", 9, Color.GREEN));
             lights.add(new DmxGroupLight("Stage", 13, 16));
         }
 
         List<Scene> scenes = feluxManager.getScenes();
         if (scenes.size() == 0) {
             LightScene lightScene = new LightScene("Scene 1");
             lightScene.addLight((Light)lights.get(0).copy());
             lightScene.addLight((Light)lights.get(1).copy());
             lightScene.addLight((Light)lights.get(2).copy());
             scenes.add(lightScene);
         }
 
         ItemListFragment listFragment = (ItemListFragment)fragmentManager.findFragmentByTag(getActionBar().getSelectedTab().getTag().toString());
         if (listFragment != null) {
             listFragment.onItemsLoaded(feluxManager);
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         //outState.putInt(ACTIVE_TAB, getActionBar().getSelectedNavigationIndex());
         super.onSaveInstanceState(outState);
     }
 
     @SuppressWarnings("unchecked")
     private ItemDetailFragment getItemDetailFragment(Item item) {
         FragmentManager fm = fragmentManager;
         FragmentTransaction ft = fm.beginTransaction();
         ItemDetailFragment itemDetailFragment = null;
         String tag = null;
 
         ItemDetailFragment oldDetailFragment = (ItemDetailFragment) fm.findFragmentById(R.id.fragment_secondary);
 
         if (item instanceof Item) {
             tag = itemToDetailFragment.get(item.getClass().getCanonicalName());
             if (tag == null) {
                 throw new IllegalStateException("Invalid item");
             }
         } else if (item == null) {
             if (oldDetailFragment != null) {
                 ft.detach(oldDetailFragment);
                 ft.commit();
             }
             return oldDetailFragment;
         }
 
         if (tag != null && !tag.isEmpty()) {
             itemDetailFragment = (ItemDetailFragment) fm.findFragmentByTag(tag);
             if (itemDetailFragment != oldDetailFragment) {
                 ft.detach(oldDetailFragment);
             }
             if (itemDetailFragment == null) {
                 try {
                     ItemListFragment listFragment = (ItemListFragment)fm.findFragmentByTag(getActionBar().getSelectedTab().getTag().toString());
                     itemDetailFragment = (ItemDetailFragment)Class.forName(tag).newInstance();
                     itemDetailFragment.setDetailCallbacks(listFragment);
                     itemDetailFragment.setFeluxManager(feluxManager);
                     //ft.add(R.id.fragment_secondary, itemDetailFragment, tag);
                     ft.replace(R.id.fragment_secondary, itemDetailFragment, tag);
                 } catch (Exception ex) {
                     throw new IllegalStateException("Invalid item");
                 }
             } else {
                 if (itemDetailFragment.isDetached() || (itemDetailFragment != oldDetailFragment)) {
                     ft.attach(itemDetailFragment);
                 }
             }
 
             if (itemDetailFragment == null) {
                 throw new IllegalStateException("Invalid item");
             }
         } else {
             throw new IllegalStateException("Invalid item");
         }
 
         ft.commit();
 
         return itemDetailFragment;
     }
     /**
      * Callback method from {@link ItemListCallbacks}
      * indicating that the item with the given index was selected.
      */
     @Override
     @SuppressWarnings("unchecked")
     public void onItemSelected(int position, Item item) {
         ItemDetailFragment detailFragment = getItemDetailFragment(item);
         if (detailFragment != null) {
             detailFragment.setItem(item);
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void onItemAdded(Item item) {
         ItemDetailFragment detailFragment = getItemDetailFragment(item);
         if (detailFragment != null) {
            getItemDetailFragment(item).onItemAdded(item);
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void onItemUpdated(Item item) {
         ItemDetailFragment detailFragment = getItemDetailFragment(item);
         if (item instanceof Light) {
             feluxManager.saveLights();
         } else if (item instanceof Scene) {
             feluxManager.saveScenes();
         }
         if (detailFragment != null) {
            getItemDetailFragment(item).onItemUpdated(item);
         }
     }
 
     private class TabListener implements ActionBar.TabListener {
         private FragmentActivity activity;
 
         public TabListener(FragmentActivity activity) {
             this.activity = activity;
         }
         public void onTabSelected(ActionBar.Tab tab,
                                   android.app.FragmentTransaction unused) {
             FragmentManager fm = activity.getSupportFragmentManager();
             FragmentTransaction ft = fm.beginTransaction();
             String listTag = tab.getTag().toString();
             ItemListFragment listFragment = (ItemListFragment)fm.findFragmentByTag(listTag);
 
             if (listFragment instanceof ItemListFragment) {
                 ft.attach(listFragment);
             } else if (listFragment == null) {
                 try {
                     listFragment = (ItemListFragment)Class.forName(listTag).newInstance();
                     listFragment.setFeluxManager(feluxManager);
                     //ft.add(R.id.fragment_primary, listFragment, listTag);
                     ft.replace(R.id.fragment_primary, listFragment, listTag);
                 } catch (Exception ex) {
                     throw new IllegalStateException("Invalid tab");
                 }
             } else {
                 throw new IllegalStateException("Invalid tab");
             }
 
             ft.commit();
         }
 
         public void onTabUnselected(ActionBar.Tab tab,
                                     android.app.FragmentTransaction unused) {
             FragmentManager fm = activity.getSupportFragmentManager();
             FragmentTransaction ft = fm.beginTransaction();
             String listTag = tab.getTag().toString();
             ItemListFragment listFragment = (ItemListFragment)fm.findFragmentByTag(listTag);
 
             if (listFragment instanceof ItemListFragment) {
                 ft.detach(listFragment);
             } else {
                 throw new IllegalStateException("Invalid tab");
             }
 
             ft.commit();
         }
 
         public void onTabReselected(ActionBar.Tab tab,
                                     android.app.FragmentTransaction unused) {
         }
     }
 }
