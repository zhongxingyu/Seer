 package nl.napauleon.sabber.test;
 
 import nl.napauleon.sabber.R;
 
 public class DownloadingFragmentTest extends RobotiumTest {
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		enterMockSettings();
 	}
 
 	public void testDownloadingMock() throws Exception {
 		solo.clickOnText(solo.getString(R.string.downloading));
 		assertTrue(solo.searchText("Rookie.Blue"));
 	}
 	
 	public void testChangeCategory() throws Exception {
 		
 		solo.clickOnText(solo.getString(R.string.downloading));
 		solo.clickInList(0);
 		solo.clickOnText(solo.getString(R.string.option_change_category));
 		solo.clickOnText("films");
 		assertTrue(solo.searchText("Rookie.Blue"));
 	}
 
 	public void testHistoryMock() throws Exception {
 		
 		solo.clickOnText(solo.getString(R.string.history));
 		assertTrue(solo.searchText("Anne Rice"));
 	}
 }
