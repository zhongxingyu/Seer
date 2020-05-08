 package com.philiptorchinsky.TimeAppe.tests;
 
 import android.R;
 import android.app.Instrumentation;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.ViewAsserts;
 import android.widget.ListView;
 import com.philiptorchinsky.TimeAppe.Item;
 import com.philiptorchinsky.TimeAppe.MainActivity;
 //import org.junit.Before;
 //import org.junit.Test;
 
 // tests
 
 public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
 
     public MainActivity activity;
     public int mActivePosition = 0;
     ListView mListView;
     Item cItem;
     String newState;
     private Instrumentation mInstrumentation;
 
     public MainActivityTest() {
         super("com.philiptorchinsky.TimeAppe", MainActivity.class);
     }
 
 //    @Before
     public void setUp() throws Exception {
 
         super.setUp();
         activity = getActivity();
 
         mListView = (ListView) activity.getListView().findViewById(R.id.list);
         mInstrumentation = getInstrumentation();
     }
 
 //    @Test
 
     public void testControlsCreated() {
 // is there an activity started?
         assertNotNull(activity);
     }
 
     public void testControlsVisible() {
 // is there a list on screen?
         ViewAsserts.assertOnScreen(mListView.getRootView(),(ListView) activity.findViewById(R.id.list));
     }
 
 
     public void testOnListItemClick() throws Exception {
 
 // check whether a click on an item changes its status, as it should be
 // status can be "active" or "inactive"
 
        int count;
         count = mListView.getAdapter().getCount();
         System.out.println(count);
         if (count != 0) {
             cItem = (Item) mListView.getAdapter().getItem(mActivePosition);
             String currentState = cItem.getStatus();
 
             activity.runOnUiThread(new Runnable() {
                 public void run() {
                     mListView.performItemClick(
                             mListView.getAdapter().getView(mActivePosition, null, null),
                             mActivePosition,
                             mActivePosition);
 
                 }
             });
 
             mInstrumentation.waitForIdleSync();
             newState = cItem.getStatus();
 
             assertFalse("had to change status", newState.contentEquals(currentState));
         }
     }
 }
 
