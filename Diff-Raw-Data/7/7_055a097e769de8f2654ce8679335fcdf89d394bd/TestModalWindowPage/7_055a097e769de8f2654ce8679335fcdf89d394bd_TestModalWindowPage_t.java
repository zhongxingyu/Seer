 package edu.hawaii.ihale.frontend.page;
 
 import static org.junit.Assert.assertEquals;
 import java.util.Date;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.PageableListView;
 import org.apache.wicket.util.tester.WicketTester;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.api.ApiDictionary.SystemStatusMessageType;
 import edu.hawaii.ihale.api.repository.impl.Repository;
 import edu.hawaii.ihale.frontend.SolarDecathlonApplication;
 import edu.hawaii.ihale.frontend.page.dashboard.Dashboard;
 import edu.hawaii.ihale.frontend.page.messages.MessagesListener;
 
 /**
  * JUnit testing for ErrorWindow page.
  * 
  * @author Anthony Kinsey
  * @author Kurt Teichman
  */
 public class TestModalWindowPage {
 
   static MessagesListener msgListener;
   static SolarDecathlonApplication sda;
   // Start up the WicketTester and check that the page renders.
   WicketTester tester = new WicketTester(sda);
 
   /**
    * Loads items into lists.
    * 
    * @throws Exception e.
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
     sda = new SolarDecathlonApplication();
     msgListener = SolarDecathlonApplication.getMessagesListener();
 
     // Get Current Time and set backoff variables.
     long currentTime = new Date().getTime();
     long min = 60 * 1000;
     long hour = min * 60;
     long day = hour * 24;
 
     // Test Message
     String msg = "Test Message";
 
     // Populate lists
     msgListener
         .messageAdded(currentTime, IHaleSystem.AQUAPONICS, SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - min, IHaleSystem.AQUAPONICS,
         SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - hour, IHaleSystem.AQUAPONICS,
         SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - day, IHaleSystem.AQUAPONICS,
         SystemStatusMessageType.INFO, msg);
 
     msgListener.messageAdded(currentTime, IHaleSystem.HVAC, SystemStatusMessageType.INFO, msg);
     msgListener
         .messageAdded(currentTime - min, IHaleSystem.HVAC, SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - hour, IHaleSystem.HVAC, SystemStatusMessageType.INFO,
         msg);
     msgListener
         .messageAdded(currentTime - day, IHaleSystem.HVAC, SystemStatusMessageType.INFO, msg);
 
     msgListener.messageAdded(currentTime, IHaleSystem.LIGHTING, SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - min, IHaleSystem.LIGHTING, SystemStatusMessageType.INFO,
         msg);
     msgListener.messageAdded(currentTime - hour, IHaleSystem.LIGHTING,
         SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - day, IHaleSystem.LIGHTING, SystemStatusMessageType.INFO,
         msg);
 
     msgListener.messageAdded(currentTime, IHaleSystem.ELECTRIC, SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - min, IHaleSystem.ELECTRIC, SystemStatusMessageType.INFO,
         msg);
     msgListener.messageAdded(currentTime - hour, IHaleSystem.ELECTRIC,
         SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - day, IHaleSystem.ELECTRIC, SystemStatusMessageType.INFO,
         msg);
 
     msgListener.messageAdded(currentTime, IHaleSystem.PHOTOVOLTAIC, SystemStatusMessageType.INFO,
         msg);
     msgListener.messageAdded(currentTime - min, IHaleSystem.PHOTOVOLTAIC,
         SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - hour, IHaleSystem.PHOTOVOLTAIC,
         SystemStatusMessageType.INFO, msg);
     msgListener.messageAdded(currentTime - day, IHaleSystem.PHOTOVOLTAIC,
         SystemStatusMessageType.INFO, msg);
   }
 
   /**
    * Performs JUnit tests on the Dashboard page.
    */
   @SuppressWarnings("unchecked")
   @Test
   public void testPage() {
 
     tester.startPage(Dashboard.class);
     tester.assertRenderedPage(Dashboard.class);
 
     // Check all components on page
     tester.assertComponent("logo", Image.class);
     tester.assertComponent("printer", Image.class);
     tester.assertComponent("help", Image.class);
     tester.assertComponent("refresh", Image.class);
     tester.assertComponent("TableViewImage", Image.class);
     tester.assertComponent("TableEditImage", Image.class);
     tester.assertComponent("TableDeleteImage", Image.class);
     tester.assertComponent("title", Label.class);
     tester.assertComponent("DashboardItem", WebMarkupContainer.class);
     tester.assertComponent("DashboardItem:DashboardPageLinkTab", Link.class);
     tester.assertComponent("EnergyItem", WebMarkupContainer.class);
     tester.assertComponent("EnergyItem:EnergyPageLinkTab", Link.class);
     tester.assertComponent("AquaponicsItem", WebMarkupContainer.class);
     tester.assertComponent("AquaponicsItem:AquaponicsPageLinkTab", Link.class);
     tester.assertComponent("LightingItem", WebMarkupContainer.class);
     tester.assertComponent("LightingItem:LightingPageLinkTab", Link.class);
     tester.assertComponent("HvacItem", WebMarkupContainer.class);
     tester.assertComponent("HvacItem:HvacPageLinkTab", Link.class);
     tester.assertComponent("HelpItem", WebMarkupContainer.class);
     tester.assertComponent("HelpItem:HelpPageLinkTab", Link.class);
     tester.assertComponent("DashboardPageLink", Link.class);
     tester.assertComponent("EnergyPageLink", Link.class);
     tester.assertComponent("AquaponicsPageLink", Link.class);
     tester.assertComponent("LightingPageLink", Link.class);
     tester.assertComponent("HvacPageLink", Link.class);
     tester.assertComponent("HelpPageLink", Link.class);
     tester.assertComponent("modalwindow", SelectModalWindow.class);
     tester.assertComponent("Calendar", Label.class);
     tester.assertComponent("CurrentWeatherHeader", Label.class);
     tester.assertComponent("OutsideTemperatureHeader", Label.class);
     tester.assertComponent("InsideTemperatureHeader", Label.class);
     tester.assertComponent("CurrentWeatherCondition", Label.class);
     tester.assertComponent("CurrentWeatherImage", Image.class);
     tester.assertComponent("WeatherForecastImage1", Image.class);
     tester.assertComponent("WeatherForecastImage2", Image.class);
     tester.assertComponent("WeatherForecastImage3", Image.class);
     tester.assertComponent("WeatherForecastImage4", Image.class);
     tester.assertComponent("weatherForecastDayLabel1", Label.class);
     tester.assertComponent("weatherForecastDayLabel2", Label.class);
     tester.assertComponent("weatherForecastDayLabel3", Label.class);
     tester.assertComponent("weatherForecastDayLabel4", Label.class);
     tester.assertComponent("consprodD", WebMarkupContainer.class);
     tester.assertComponent("consprodW", WebMarkupContainer.class);
     tester.assertComponent("consprodM", WebMarkupContainer.class);
     tester.assertComponent("DayUsage2", Label.class);
     tester.assertComponent("WeekUsage2", Label.class);
     tester.assertComponent("MonthUsage2", Label.class);
     tester.assertComponent("DayPriceConverter", Label.class);
     tester.assertComponent("WeekPriceConverter", Label.class);
     tester.assertComponent("MonthPriceConverter", Label.class);
     tester.assertComponent("DayDiv", WebMarkupContainerWithAssociatedMarkup.class);
     tester.assertComponent("DayDiv:DayUsage", Label.class);
     tester.assertComponent("WeekDiv", WebMarkupContainerWithAssociatedMarkup.class);
     tester.assertComponent("WeekDiv:WeekUsage", Label.class);
     tester.assertComponent("MonthDiv", WebMarkupContainerWithAssociatedMarkup.class);
     tester.assertComponent("MonthDiv:MonthUsage", Label.class);
     tester.assertComponent("Cities", DropDownChoice.class);
     tester.assertComponent("InsideTemperature", Label.class);
     tester.assertComponent("OutsideTemperature", Label.class);
     tester.assertComponent("Time", Label.class);
     tester.assertComponent("SystemLogContainer", WebMarkupContainer.class);
     tester.assertComponent("SystemLogContainer:StatusMessages", PageableListView.class);
     tester.assertComponent("modalwindow", SelectModalWindow.class);
 
     Repository repository = new Repository();
    repository.store(IHaleSystem.AQUAPONICS, IHaleState.PH, 101020L, 1.0);
     
    repository.store(IHaleSystem.AQUAPONICS, IHaleState.CIRCULATION, 101020L, 1.0);
     // Check the dropdown box
     DropDownChoice<String> countryDropDownChoice =
         (DropDownChoice<String>) tester.getComponentFromLastRenderedPage("Cities");
     assertEquals("Check that the dropdown has correct amount of selections.", countryDropDownChoice
         .getChoices().size(), 2);
 
     // The following line is useful for seeing what's on the page.
     tester.debugComponentTrees();
 
   } // End testPage
   
   /**
    * Test status messages log.
    */
   public void testStatusMessages() {
     // TO DO
     int x = 0;
     System.out.println(x);
   } // End testStatusMessages
 
 }
