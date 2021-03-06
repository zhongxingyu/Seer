 package com.manmoe.example.test;
 
 import com.manmoe.example.model.PopupPage;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.remote.SessionId;
 import org.testng.ITestResult;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import us.monoid.web.AbstractContent;
 import us.monoid.web.Resty;
 
 import java.io.IOException;
 
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 
 /**
  * Tests our Firespotting test example.
  *
  * @author Manuel Möhlmann <mail@manmoe.com>
  */
 public class FirespottingITTest {
 	/**
 	 * Our test object.
 	 */
 	private FirespottingIT firespottingIT;
 
 	private PopupPage popupPage = mock(PopupPage.class);
 
 	/**
 	 *  Method for setting up the test environment.
 	 */
 	@BeforeMethod
 	public void setUp() {
 		this.firespottingIT = spy(new FirespottingIT());
 		this.firespottingIT.popupPage = this.popupPage;
 	}
 
 	/**
 	 * Let's check, if we set up our test environment properly.
 	 */
 	@Test
 	public void testSetUp() {
 		RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
 
 		doReturn(remoteWebDriver).when(firespottingIT).getWebDriver();
 
 		// run test
 		firespottingIT.setUp();
 
 		// check, if all went well
 		assertNotNull(firespottingIT.popupPage);
 	}
 
 	/**
 	 * Check the tear down.
 	 */
 	@Test
 	public void testTearDown() throws IOException {
 		// insert mock to test object
 		firespottingIT.popupPage = this.popupPage;
 
 		// mock rest client
 		firespottingIT.restClient = mock(Resty.class);
 
 		// mock some objects for session key
 		RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
 		SessionId sessionId = mock(SessionId.class);
 		when(popupPage.getDriver()).thenReturn(remoteWebDriver);
 		when(remoteWebDriver.getSessionId()).thenReturn(sessionId);
 		when(sessionId.toString()).thenReturn("72345863");
 
 		// run test method
 		firespottingIT.tearDown();
 
 		// is the method called to tear down correctly?
 		verify(popupPage, atLeastOnce()).tearDown();
 
 		if (System.getenv("SAUCE_USERNAME") != null && System.getenv("SAUCE_ACCESS_KEY") != null && System.getenv("PLATFORM") != null) {
 			// verify rest client actions if environment variables are set
 			// @TODO: add better verification! (no more anyStrings; check the values!)
 			verify(firespottingIT.restClient, atLeastOnce()).authenticate(anyString(), anyString(), anyString().toCharArray());
 			verify(firespottingIT.restClient, atLeastOnce()).withHeader("Content-Type", "application/json");
 			verify(firespottingIT.restClient, atLeastOnce()).json(anyString(), any(AbstractContent.class));
 		}
 	}
 
 	@Test(dataProvider = "reportDataProvider")
 	public void testReport(boolean testSuccess) {
 		ITestResult itResultMock = mock(ITestResult.class);
 		when(itResultMock.isSuccess()).thenReturn(testSuccess);
 		// set initial value of test result
 		firespottingIT.testResult = true;
 
 		// run test method
 		firespottingIT.report(itResultMock);
 
 		assertEquals(firespottingIT.testResult, testSuccess);
 	}
 
 	@DataProvider
 	public Object[][] reportDataProvider() {
 		return new Object[][] {
 			{true},
 			{false}
 		};
 	}
 
 	/**
 	 * We check the isInstalled method.
 	 */
 	@Test
 	public void testIsInstalled() {
 		firespottingIT.popupPage = this.popupPage;
 
 		when(popupPage.getId()).thenReturn("testId");
 
 		firespottingIT.isInstalled();
 	}
 
 	@Test
 	public void testPopupTest() {
 		when(popupPage.getTitle()).thenReturn("Firespotting!");
 
 		firespottingIT.testPopup();
 
 		verify(popupPage, atLeastOnce()).open();
 		verify(popupPage, atLeastOnce()).getTitle();
 	}
 
 	@Test
 	public void testEntryTest() {
 		String linkText = "linkText";
 
 		// mocking for every entry
 		for (int i = 1; i <= FirespottingIT.ENTRY_LIST_LENGTH; i++) {
 			when(popupPage.getEntryTitle(i)).thenReturn(linkText + i);
 		}
 
 		// call test method
 		firespottingIT.testEntry();
 
 		// verifying for every entry
 		for (int i = 1; i <= FirespottingIT.ENTRY_LIST_LENGTH; i++) {
 			verify(popupPage, atLeastOnce()).clickOnEntryLink(linkText + i);
 			verify(popupPage, atLeastOnce()).getBack();
 		}
 	}
 }
