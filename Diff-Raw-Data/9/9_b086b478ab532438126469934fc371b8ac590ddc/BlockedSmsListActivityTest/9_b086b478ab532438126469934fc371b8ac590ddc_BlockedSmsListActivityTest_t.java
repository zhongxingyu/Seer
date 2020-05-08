 package ezhun.smsb.activity;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import ezhun.smsb.R;
 import ezhun.smsb.storage.MessageProviderHelper;
 import ezhun.smsb.storage.TestMessageProvider;
 
 public class BlockedSmsListActivityTest extends ActivityInstrumentationTestCase2<BlockedSmsListActivity> {
 	private BlockedSmsListActivity mActivity;
 	private ListView mList;
 	private SmsPojoArrayAdapter mAdapter;
 	private static final int ADAPTER_COUNT = 10;
 
 	public BlockedSmsListActivityTest(){
 		super("ezhun.smsb.activity.BlockedSmsListActivity", BlockedSmsListActivity.class);
 	}
 
 	public BlockedSmsListActivityTest(String pkg, Class activityClass) {
 		super(pkg, activityClass);
 	}
 
 	public BlockedSmsListActivityTest(Class activityClass) {
 		super(activityClass);
 	}
 
 	public void setUp() throws Exception {
 		MessageProviderHelper.setMessageProvider(new TestMessageProvider());
 		super.setUp();
 		setActivityInitialTouchMode(false);
 		mActivity = getActivity();
 		mList = (ListView)mActivity.findViewById(R.id.messagesListView);
 		mAdapter = (SmsPojoArrayAdapter) mList.getAdapter();
 	}
 
 	public void testPreConditions() {
 		assertTrue(mAdapter != null);
 		assertEquals(ADAPTER_COUNT, mAdapter.getCount());
 	}
 
 	public void testUndoButtonIsInvisibleAtStartup(){
 		View v = mActivity.findViewById(R.id.buttonLayout);
 		assertEquals(View.GONE, v.getVisibility());
 	}
 
 	public void test_pressing_DeleteAll_deletes_all_items(){
 		pressDeleteAllMenuItem();
 		assertEquals(0, mAdapter.getCount());
 	}
 
 	public void test_undo_button_is_visible_after_pressing_DeleteAll(){
 		pressDeleteAllMenuItem();
 		View v = mActivity.findViewById(R.id.buttonLayout);
 		assertEquals(View.VISIBLE, v.getVisibility());
 	}
 
 	public void test_undo_button_has_correct_text_after_pressing_DeleteAll(){
 		pressDeleteAllMenuItem();
 		Button b = (Button)mActivity.findViewById(R.id.undoButton);
 		assertEquals("10 messages were deleted. (Undo)", b.getText());
 	}
 
 	public void test_pressing_undo_button_after_deleting_all_returns_all_items_back(){
 		int originalCount = mAdapter.getCount();
 		pressDeleteAllMenuItem();
 		pressUndoButton();
 		assertEquals(originalCount, mAdapter.getCount());
 	}
 
 	private void pressUndoButton() {
		final Button b = (Button)mActivity.findViewById(R.id.undoButton);
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				b.performClick();
			}
		});
		getInstrumentation().waitForIdleSync();
 	}
 
 	private void pressDeleteAllMenuItem() {
 		sendKeys(KeyEvent.KEYCODE_MENU); // showing menu
 		sendKeys(KeyEvent.KEYCODE_DPAD_LEFT); // selecting first menu item (delete all)
 		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER); // pressing selected item
 	}
 }
