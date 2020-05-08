 package ca.ualberta.CMPUT301F13T02.chooseyouradventure.test;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import ca.ualberta.CMPUT301F13T02.chooseyouradventure.ControllerApp;
 import ca.ualberta.CMPUT301F13T02.chooseyouradventure.EditStoryActivity;
 import ca.ualberta.CMPUT301F13T02.chooseyouradventure.Page;
 import ca.ualberta.CMPUT301F13T02.chooseyouradventure.Story;
 import ca.ualberta.CMPUT301F13T02.chooseyouradventure.TextTile;
 import ca.ualberta.CMPUT301F13T02.chooseyouradventure.ViewPageActivity;
 import ca.ualberta.CMPUT301F13T02.chooseyouradventure.ViewStoriesActivity;
 import android.app.Activity;
 import android.app.Instrumentation.ActivityMonitor;
 import android.provider.Settings.Secure;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.UiThreadTest;
 import android.widget.Button;
 import android.widget.ListView;
 
 /**
  * Tests for the TestViewStoriesActivity
  */
 
 public class TestViewStoriesActivity extends ActivityInstrumentationTestCase2<ViewStoriesActivity> {
 	
 	private ViewStoriesActivity activity;
 	
 	private Button addNewButton;
 	private ListView listView;
 	private MockHandler handler = new MockHandler();
 	private Story testStory;
 	private Page testPage;
 	
 	public TestViewStoriesActivity() {
 		super(ViewStoriesActivity.class);
 	}
 	
 	protected void setUp() throws Exception{
 		super.setUp();
 		
 		// Get UI elements of this activity
 		activity = getActivity();
 		addNewButton = (Button) activity.findViewById(ca.ualberta.CMPUT301F13T02.chooseyouradventure.R.id.createButton);
 		listView = (ListView) activity.findViewById(ca.ualberta.CMPUT301F13T02.chooseyouradventure.R.id.mainView);
 		
 		// Set up a test story
 		testPage = new Page();
 		testPage.addTile(new TextTile("TEST TEXT"));
 
 		testStory = new Story();
 		testStory.setTitle("Test Story");
 		testStory.addPage(testPage);
 		testStory.setFirstpage(testPage.getId());
 	    testStory.setAuthor(Secure.getString(activity.getBaseContext().getContentResolver(), Secure.ANDROID_ID));
 
 	    //Use the same data for each test
 		activity.setHandler(handler);
 		handler.deleteAllStories();
 		handler.addStory(testStory);
 	}
 	
 	
 	public void testLayout() {
 		assertTrue(addNewButton != null);
 		assertTrue(listView != null);
 	}
 	
 	@UiThreadTest
 	public void testDisplay() {
 
 		activity.refresh();
 		
 		//Make sure that the correct stories are displaying
 		assertEquals(listView.getCount(), 1);
 		assertTrue(listView.getAdapter().getItem(0).equals("Test Story"));
 	}
 	
 	public void testClick() {
 		
 		activity.runOnUiThread(new Runnable() {
 			@Override
             public void run() {
 				activity.refresh();
 				listView.performItemClick(listView.getAdapter().getView(0, null, null),
 										  0, 
 										  listView.getAdapter().getItemId(0));
             }
         });
 		
 		
 		//Wait for the view page activity to start
 	    ActivityMonitor monitor = new ActivityMonitor(ViewPageActivity.class.getName(), null, false);
 	    getInstrumentation().addMonitor(monitor);
 	    Activity nextActivity = monitor.waitForActivityWithTimeout(5 * 1000);
 	    assertNotNull("Activity was not started", nextActivity);
 	    
 	    //Verify state properly set
 	    assertTrue(((ControllerApp)activity.getApplication()).getStory() == testStory);
 	    assertTrue(((ControllerApp)activity.getApplication()).getPage() == testPage);
 	    
 	    nextActivity.finish();
 	}
 	
 	public void testEdit() {
 
	    Solo solo = new Solo(getInstrumentation(), getActivity());
 	    getInstrumentation().waitForIdleSync();
 
 		activity.runOnUiThread(new Runnable() {
 			@Override
             public void run() {
 				activity.refresh();
 				activity.onLongListItemClick(null, 0, 0);
             }
         });
 		
 		//Find pop-up, click edit
 	    assertTrue("Couldn't find dialog", solo.searchText(".*Advanced Story Options"));
 	    solo.clickOnText("Edit");
 	    
 	    //Wait for edit story activity to start
 	    ActivityMonitor monitor = new ActivityMonitor(EditStoryActivity.class.getName(), null, false);
 	    getInstrumentation().addMonitor(monitor);
 	    Activity nextActivity = monitor.waitForActivityWithTimeout(5 * 1000);
 	    assertNotNull("Activity was not started", nextActivity);
 	    
 	    //Verify state properly set
 	    assertTrue(((ControllerApp)activity.getApplication()).getStory() == testStory);
 	    
 	    nextActivity.finish();
 	}
 	
 	public void testAddNew() {
 
	    Solo solo = new Solo(getInstrumentation(), getActivity());
 	    getInstrumentation().waitForIdleSync();
 
 		activity.runOnUiThread(new Runnable() {
 			@Override
             public void run() {
 				addNewButton.performClick();
             }
         });
 		
 		//Find dialog, and create a new story
 	    assertTrue("Couldn't find dialog", solo.searchText("Create New"));
 	    solo.enterText(0, "Test Story -- SOLO");
 	    solo.clickOnButton("Save");
 	    
 	    //Wait for Edit Story Activity to start
 	    ActivityMonitor monitor = new ActivityMonitor(EditStoryActivity.class.getName(), null, false);
 	    getInstrumentation().addMonitor(monitor);
 	    Activity nextActivity = monitor.waitForActivityWithTimeout(5 * 1000);
 	    assertNotNull("Activity was not started", nextActivity);
 	    
 	    //Verify state properly set
 	    assertTrue(((ControllerApp)activity.getApplication()).getStory().getTitle().equals("Test Story -- SOLO"));
 	    
 	    nextActivity.finish();
 	}
 }
