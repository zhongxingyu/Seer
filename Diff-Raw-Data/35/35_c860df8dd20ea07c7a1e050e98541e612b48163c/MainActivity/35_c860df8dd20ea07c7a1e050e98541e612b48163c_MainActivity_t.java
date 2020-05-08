 package com.sysiq.android.callmanager.ui;
 
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.provider.ContactsContract;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 import com.sysiq.android.callmanager.CMApplication;
 import com.sysiq.android.callmanager.CMUtils;
 import com.sysiq.android.callmanager.R;
 import com.sysiq.android.callmanager.model.entity.CMItem;
 import com.sysiq.android.callmanager.model.entity.CMList;
 import com.sysiq.android.callmanager.service.ICMService;
 import com.sysiq.android.callmanager.ui.fragments.CMItemsFragment;
 import com.sysiq.android.callmanager.ui.fragments.CMListsFragment;
 import com.sysiq.android.callmanager.ui.fragments.CreateListDialogFragment;
 
 import java.util.List;
 
 public class MainActivity extends FragmentActivity implements CMListsFragment.CMListsCallback, CMItemsFragment.CMItemCallback {
     public static final String TAG = "com.sysiq.android.callmanager.ui.MainActivity";
     private static final int PICK_CONTACT = 10000;
     private CMListsFragment cmListsFragment;
     private CMItemsFragment cmItemsFragment;
     private Button createListBtn;
     private ICMService icmService;
     private ServiceConnection connection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName name, IBinder iBinder) {
             icmService = ICMService.Stub.asInterface(iBinder);
         }
 
         @Override
         public void onServiceDisconnected(ComponentName name) {
             icmService = null;
         }
     };
     private View.OnClickListener createListListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
             Fragment prev = getSupportFragmentManager().findFragmentByTag("create_list_dialog");
             if (null != prev) transaction.remove(prev);
             transaction.addToBackStack(null);
 
             CreateListDialogFragment dialogFragment = new CreateListDialogFragment();
             dialogFragment.show(transaction, "create_list_dialog");
         }
     };
     private Button addContactBtn;
     private View.OnClickListener addContactListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             if (getApp().getCMList(getApp().getSelectedListId()) == null) {
                 Toast.makeText(MainActivity.this, R.string.select_group, Toast.LENGTH_LONG);
                 return;
             }
             Intent intent = new Intent(Intent.ACTION_PICK);
             intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
             startActivityForResult(intent, PICK_CONTACT);
         }
     };
 
     public CMApplication getApp() {
         return (CMApplication) getApplication();
     }
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         bindService(new Intent("com.sysiq.intent.action.CM_SERVICE"), connection, BIND_AUTO_CREATE);
 
         Log.d(TAG, "onCreate");
 
         if (Configuration.ORIENTATION_PORTRAIT == getApp().getOrientation()) {
             Log.d(TAG, "PORTRAIT");
             setContentView(R.layout.main_portrait);
 
             createListBtn = (Button) findViewById(R.id.new_list);
             createListBtn.setOnClickListener(createListListener);
 
             Log.d(TAG, "create CMListsFragment");
             Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.cm_lists_fragment);
             if (null != fragment) cmListsFragment = (CMListsFragment) fragment;
         } else {
             Log.d(TAG, "LANDSCAPE");
             setContentView(R.layout.main_landscape);
 
             createListBtn = (Button) findViewById(R.id.new_list);
             createListBtn.setOnClickListener(createListListener);
 
             addContactBtn = (Button) findViewById(R.id.add_item);
             addContactBtn.setOnClickListener(addContactListener);
 
             Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.cm_lists_fragment);
             if (null != fragment) cmListsFragment = (CMListsFragment) fragment;
 
             fragment = getSupportFragmentManager().findFragmentById(R.id.cm_list_items_fragment);
             if (null != fragment) cmItemsFragment = (CMItemsFragment) fragment;
         }
     }
 
     @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
             case PICK_CONTACT:
                 if (RESULT_OK == resultCode) {
                     Uri result = data.getData();
                     String id = result.getLastPathSegment();
                     List<CMItem> cmItems = CMUtils.getContactInfo(id, getContentResolver());
                     for (CMItem cmItem : cmItems) {
                         getApp().addCMItem(cmItem);
                     }
                     cmItemsFragment.clearData();
                     cmItemsFragment.addItems(getApp().getCMList(getApp().getSelectedListId()).getCmItems());
                 }
                 break;
         }
     }
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();
         finish();
     }
 
     // =============== CMListsFragment.CMListsCallback ===============
     @Override
     public void onListSelected(long id) {
         Log.d(TAG, "onListSelected(" + id + ")");
         getApp().setSelectedListId(id);
         setTitle(getString(R.string.app_name) + ": " + getApp().getCMList(id).getName());
         if (Configuration.ORIENTATION_PORTRAIT == getApp().getOrientation()) {
             Intent itemsActivity = new Intent(this, CMItemsActivity.class);
             startActivity(itemsActivity);
         } else {
             cmItemsFragment.clearData();
             cmItemsFragment.addItems(getApp().getCMList(id).getCmItems());
             cmItemsFragment.notifyDataChanged();
         }
     }
 
     @Override
     public void onListCreate(String listName, CMList.ListType listType) {
         CMList cmList = new CMList();
         cmList.setType(listType);
         cmList.setName(listName);
         cmList.setEnabled(false);
 
         getApp().addCMList(cmList);
 
         cmListsFragment.addCMLIst(cmList);
 
         getApp().setSelectedListId(cmList.getId());
         if (null != cmItemsFragment) {
             cmItemsFragment.clearData();
             if (null != cmList.getCmItems())
                 cmItemsFragment.addItems(cmList.getCmItems());
         }
     }
 
     @Override
     public void onListEnabled(long id, boolean value) {
         getApp().listEnable(id, value);
         getApp().setSelectedListId(id);
         CMList cmList = getApp().getCMList(id);
         if (null != cmItemsFragment) {
             cmItemsFragment.clearData();
             if (null != cmList.getCmItems())
                 cmItemsFragment.addItems(cmList.getCmItems());
         }
         try {
             if (null != icmService)
                 icmService.reloadData(id);
         } catch (RemoteException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void onListDelete(long id) {
         getApp().deleteCMList(id);
         if (null != cmItemsFragment) {
             cmItemsFragment.clearData();
             cmItemsFragment.notifyDataChanged();
         }
         try {
             if (null != icmService)
                 icmService.reloadData(id);
         } catch (RemoteException e) {
             e.printStackTrace();
         }
     }
 
     // =========== CMItemsFragment.CMItemCallback ===========
 
     @Override
     public void onItemSelected(long itemId) {
         getApp().setSelectedItemId(itemId);
     }
 
     @Override
     public void onItemCreate(int contactId, String name, String phone) {
         CMItem cmItem = new CMItem(name, contactId, phone);
         getApp().addCMItem(cmItem);
     }
 
     @Override
     public void onItemDelete(long itemId) {
         getApp().deleteCMItem(itemId);
     }
 }
