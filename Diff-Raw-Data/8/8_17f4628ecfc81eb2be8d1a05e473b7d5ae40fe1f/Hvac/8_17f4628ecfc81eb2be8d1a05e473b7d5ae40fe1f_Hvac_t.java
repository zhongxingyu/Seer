 package edu.hawaii.systemh.frontend.page.hvac;
 
 //import java.util.ArrayList;
 //import java.util.List;
 //import java.util.ArrayList;
 //import java.util.List;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import org.apache.wicket.Component;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
 import org.apache.wicket.behavior.AbstractBehavior;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.PageableListView;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.util.time.Duration;
 import com.codecommit.wicket.AbstractChartData;
 import com.codecommit.wicket.Chart;
 import com.codecommit.wicket.ChartAxis;
 import com.codecommit.wicket.ChartAxisType;
 import com.codecommit.wicket.ChartDataEncoding;
 import com.codecommit.wicket.ChartGrid;
 import com.codecommit.wicket.ChartProvider;
 import com.codecommit.wicket.ChartType;
 import com.codecommit.wicket.IChartData;
 import com.codecommit.wicket.MarkerType;
 import com.codecommit.wicket.ShapeMarker;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleCommandType;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.api.repository.SystemStatusMessage;
 import edu.hawaii.ihale.api.repository.TimestampIntegerPair;
 import edu.hawaii.systemh.frontend.SolarDecathlonApplication;
 import edu.hawaii.systemh.frontend.SolarDecathlonSession;
 import edu.hawaii.systemh.frontend.page.Header;
 import edu.hawaii.systemh.frontend.page.help.Help;
 
 /**
  * The temperature(Hvac) page.
  * 
  * @author Noah Woodden
  * @author Kevin Leong
  * @author Anthony Kinsey
  * @author Kylan Hughes
  * @author Chuan Lun Hung
  */
 public class Hvac extends Header {
 
   private boolean DEBUG = false;
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
 
   // desired room temperature range
   private static final long LOW_ROOM_TEMP_BOTTOM = 18L;
   private static final long LOW_ROOM_TEMP_TOP = 18L;
   private static final long HIGH_ROOM_TEMP_BOTTOM = 30L;
   private static final long HIGH_ROOM_TEMP_TOP = 32L;
 
   // for validating user's input for setTemp
   // don't want them perform duplicate doCommand with the same temperature.
   private int desiredTemp = SolarDecathlonApplication.getHvac().getTempCommand();
   private int setTemp = SolarDecathlonApplication.getHvac().getTemp();
 
   // feedback to user after they setTemp, failed or successful
   private Label feedback;
   // textfield for setTemp
   private TextField<String> airTemp;
 
   // values (attributes) for the on off hvac button
   private String buttonLabel = "Activate HVAC";
   private String buttonClass = "green-button right";
   private String buttonColor = "background-color:green";
 
   // the on off message to the right of the button.
   private Label hvacState = new Label("hvacState", "<font color=\"red\">OFF</font>");
   // to keep track of the state of hvac button
   private boolean hvacOn = false;
 
   /**
    * The temperature(Hvac) page.
    * 
    * @throws Exception the Exception
    */
   public Hvac() throws Exception {
 
     ((SolarDecathlonSession) getSession()).getHeaderSession().setActiveTab(4);
 
     // Messages
     // Add messages as a list view to each page
 
     // Get all messages applicable to this page
     List<SystemStatusMessage> msgs =
         SolarDecathlonApplication.getMessages().getMessages(IHaleSystem.HVAC);
 
     // Create wrapper container for pageable list view
     WebMarkupContainer systemLog = new WebMarkupContainer("HVACSystemLogContainer");
     systemLog.setOutputMarkupId(true);
 
     // Help button link
     Link<String> helpLink = new Link<String>("helpLink") {
       private static final long serialVersionUID = 1L;
 
       public void onClick() {
         ((SolarDecathlonSession) getSession()).getHelpSession().setCurrentTab(5);
         setResponsePage(Help.class);
       }
     };
 
     // Help Image
     helpLink
         .add(new Image("helpHvac", new ResourceReference(Header.class, "images/icons/help.png")));
 
     add(helpLink);
 
     // Create Listview
     PageableListView<SystemStatusMessage> listView =
         new PageableListView<SystemStatusMessage>("HVACStatusMessages", msgs, 10) {
 
           private static final long serialVersionUID = 1L;
 
           @Override
           protected void populateItem(ListItem<SystemStatusMessage> item) {
 
             SystemStatusMessage msg = item.getModelObject();
 
             // If only the empty message is in the list, then
             // display "No Messages"
             if (msg.getType() == null) {
               item.add(new Label("HVACMessageType", "-"));
               item.add(new Label("HVACTimestamp", "-"));
               item.add(new Label("HVACMessageContent", "No Messages"));
             }
             // Populate data
             else {
               item.add(new Label("HVACTimestamp", new Date(msg.getTimestamp()).toString()));
               item.add(new Label("HVACMessageType", msg.getType().toString()));
               item.add(new Label("HVACMessageContent", msg.getMessage()));
             }
           }
         };
 
     systemLog.add(listView);
     systemLog.add(new AjaxPagingNavigator("paginatorHVAC", listView));
     // Update log every 5 seconds.
     systemLog.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)) {
       private static final long serialVersionUID = 1L;
     });
     systemLog.setVersioned(false);
     add(systemLog);
 
     // End messages section
 
     // model for inside temperature label
     Model<String> insideTempModel = new Model<String>() {
 
       private static final long serialVersionUID = 1L;
 
       /**
        * Override the getObject for dynamic programming and change the text color according to the
        * temperature value.
        */
       @Override
       public String getObject() {
         long value = SolarDecathlonApplication.getHvac().getTemp();
         String original = value + "&deg;C";
         String closeTag = "</font>";
         if ((value <= LOW_ROOM_TEMP_TOP && value >= LOW_ROOM_TEMP_BOTTOM)
             || (value <= HIGH_ROOM_TEMP_TOP && value >= HIGH_ROOM_TEMP_BOTTOM)) {
           original = "<font color=\"#FF9900\">" + original + closeTag;
         }
         else if (value < LOW_ROOM_TEMP_BOTTOM || value > HIGH_ROOM_TEMP_TOP) {
           original = "<font color=\"red\">" + original + closeTag;
         }
         else {
           original = "<font color=\"green\">" + original + closeTag;
         }
         return original;
       }
     };
     Label insideTemperature = new Label("InsideTemperature", insideTempModel);
     insideTemperature.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)) {
       private static final long serialVersionUID = 1L;
     });
 
     // model for outside temperature label
     Model<String> outsideModel = new Model<String>() {
       private static final long serialVersionUID = 1L;
 
       @Override
       public String getObject() {
         return String.valueOf(currentWeather.getTempC() + "&deg;C");
       }
     };
     Label outsideTemperature = new Label("OutsideTemperature", outsideModel);
 
     // clear feedback each time the page is refreshed.
     feedback = new Label("Feedback", "&nbsp;");
 
     // the on off button for hvac
     Link<String> onOffButton = new Link<String>("button") {
       private static final long serialVersionUID = 1L;
 
       @Override
       /**
        * Turn the Hvac on or off, change button color and label.
        */
       public void onClick() {
 
         // change hvac state and button attributes.
         hvacOn = !hvacOn;
         if (hvacOn) {
           setButtonLabel("Deactivate HVAC");
           setButtonClass("red-button right");
           setButtonColor("background-color:red");
           hvacState.setDefaultModelObject("<font color=\"green\">ON</font>");
 
         }
         else {
           setButtonLabel("Activate HVAC");
           setButtonClass("green-button right");
           setButtonColor("background-color:green");
           hvacState.setDefaultModelObject("<font color=\"red\">OFF</font>");
         }
       }
 
     };
     // add the button value. e.g. Activate HVAC / Deactivate HVAC
     onOffButton.add(new Label("buttonLabel", new PropertyModel<String>(this, "buttonLabel"))
         .setEscapeModelStrings(false));
 
     // add some coponent tags to the button.
     onOffButton.add(new AbstractBehavior() {
 
       // support serialization
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         tag.put("class", buttonClass);
         tag.put("style", buttonColor);
       }
     });
 
     // add button to the page.
     add(onOffButton);
 
     hvacState.setEscapeModelStrings(false);
     // add hvac state label to the page.
     add(hvacState);
 
     // set label for inside temp
     // insideTemperature = new Label("InsideTemperature", insideTempLabel);
     insideTemperature.setEscapeModelStrings(false);
     // set label for outside temp
     outsideTemperature.setEscapeModelStrings(false);
     // add labels to the page
     add(insideTemperature);
     add(outsideTemperature);
 
     Form<String> form = new Form<String>("form");
 
     // Add the control for the air temp slider
     airTemp = new TextField<String>("airTemperature", new Model<String>(String.valueOf(setTemp)));
     airTemp.setEscapeModelStrings(false);
 
     // Added for jquery control.
     airTemp.setMarkupId(airTemp.getId());
     airTemp.add(new AjaxFormComponentUpdatingBehavior("onchange") {
 
       /**
        * Serial ID.
        */
       private static final long serialVersionUID = 1L;
 
       /**
        * Updates the model when the value is changed on screen.
        */
       @Override
       protected void onUpdate(AjaxRequestTarget target) {
         setTemp = Integer.valueOf(airTemp.getValue());
         if (DEBUG) {
           System.out.println("onUpdate setTemp: " + setTemp);
         }
       }
     });
 
     // airTemp.setOutputMarkupId(true);
     form.add(airTemp);
     form.add(new AjaxButton("SubmitTemp") {
 
       // support serializable
       private static final long serialVersionUID = 1L;
 
       /** Provide user feeback after they set a new desired temperature */
       @Override
       protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
         if (setTemp == desiredTemp) {
           feedback.setDefaultModelObject("<font color=\"#FF9900\">Same: " + "(" + desiredTemp
               + "&deg;C)</font>");
           // target.addComponent(textField);
           target.addComponent(feedback);
           return;
         }
         else {
           desiredTemp = setTemp;
 
           IHaleSystem system = IHaleSystem.HVAC;
           IHaleCommandType command = IHaleCommandType.SET_TEMPERATURE;
           Integer newTemperature = setTemp;
           SolarDecathlonApplication.getBackend().doCommand(system, null, command, newTemperature);
 
           feedback.setDefaultModelObject("<font color=\"green\">" + "Success: (" + desiredTemp
               + "&deg;C)</font>");
         }
         // target.addComponent(textField);
         target.addComponent(feedback);
       }
     });
 
     form.setOutputMarkupId(true);
     feedback.setEscapeModelStrings(false);
     feedback.setOutputMarkupId(true);
     form.add(feedback);
     add(form);
 
     addDayGraph();
     addWeekGraph();
     addMonthGraph();
 
   }
 
   /**
    * Add day graph to the page.
    */
   private void addDayGraph() {
     IChartData data = new AbstractChartData(ChartDataEncoding.TEXT, 100) {
 
       private static final long serialVersionUID = 1L;
 
       public double[][] getData() {
 
         Calendar current = Calendar.getInstance();
         long lastTwentyFour = 24 * 60 * 60 * 1000L;
         long time = (new Date()).getTime();
         long mHBegin =
             current.get(Calendar.MINUTE) * 60000 + current.get(Calendar.SECOND) * 1000
                 + current.get(Calendar.MILLISECOND);
         long twoHours = 2 * 60 * 60 * 1000L;
         List<TimestampIntegerPair> temperatureList =
             SolarDecathlonApplication.getRepository()
                 .getHvacTemperatureSince(time - lastTwentyFour);
 
         double[] data = new double[13];
         int pointsFound = 0;
         long totalValue = 0;
 
         for (int i = 12; i >= 0; i--) {
           for (int j = temperatureList.size() - 1; j >= 0; j--) {
 
             if ((temperatureList.get(j).getTimestamp() < ((time - mHBegin) - (twoHours * (i - 1))))
                 && (temperatureList.get(j).getTimestamp() > ((time - mHBegin) - (twoHours * i)))) {
               totalValue += temperatureList.get(j).getValue();
               pointsFound++;
             }
           }
           if (pointsFound > 0) {
             data[12 - i] = ((totalValue / pointsFound) - 5) * 2.856;
           }
           else {
             data[12 - i] = 0;
           }
 
           totalValue = 0;
           pointsFound = 0;
         }
 
         return new double[][] { {}, data };
       }
     };
 
    ChartProvider provider = new ChartProvider(new Dimension(450, 275), ChartType.LINE_XY, data);
     provider.setColors(new Color[] { Color.BLUE });
     provider.setTitle("Inside Temperature (&#176;C / Hour)");
     ChartAxis axisX = new ChartAxis(ChartAxisType.BOTTOM);
     axisX.setLabels(generateXAxis(0));
 
     provider.addAxis(axisX);
     ChartAxis axisY = new ChartAxis(ChartAxisType.LEFT);
     axisY.setLabels(new String[] { "5", "10", "15", "20", "25", "30", "35", "40" });
     provider.addAxis(axisY);
 
     provider.addShapeMarker(new ShapeMarker(MarkerType.DIAMOND, Color.RED, 0, -1, 5));
     provider.setGrid(new ChartGrid(0, 100 / 11));
 
     add(new Chart("tempD", provider));
   }
 
   /**
    * Add week graph to the page.
    */
   private void addWeekGraph() {
     IChartData data = new AbstractChartData(ChartDataEncoding.TEXT, 100) {
 
       private static final long serialVersionUID = 1L;
 
       public double[][] getData() {
 
         double[] data = new double[7];
         Calendar current = Calendar.getInstance();
         long mInADay = 24 * 3600000;
         long time = (new Date()).getTime();
         long mWeekAgo =
             (6 * mInADay) + current.get(Calendar.HOUR_OF_DAY) * 3600000L
                 + current.get(Calendar.MINUTE) * 60000L + current.get(Calendar.SECOND) * 1000L
                 + current.get(Calendar.MILLISECOND);
         long mToday =
             current.get(Calendar.HOUR_OF_DAY) * 3600000L + current.get(Calendar.MINUTE) * 60000L
                 + current.get(Calendar.SECOND) * 1000L + current.get(Calendar.MILLISECOND);
 
         List<TimestampIntegerPair> temperatureList =
             SolarDecathlonApplication.getRepository().getHvacTemperatureSince(time - mWeekAgo);
 
         long totalValue = 0;
         int pointsFound = 0;
         for (int i = 6; i >= 0; i--) {
           for (int j = 0; j < temperatureList.size(); j++) {
 
             if ((temperatureList.get(j).getTimestamp() < ((time - mToday) - (mInADay * (i - 1))))
                 && (temperatureList.get(j).getTimestamp() > ((time - mToday) - (mInADay * i)))) {
               totalValue += temperatureList.get(j).getValue();
               pointsFound++;
             }
 
           }
           if (pointsFound > 0) {
             data[6 - i] = ((totalValue / pointsFound) - 5) * 2.856;
           }
           else {
             data[6 - i] = 0;
           }
 
           totalValue = 0;
           pointsFound = 0;
         }
 
         return new double[][] { {}, data };
       }
     };
 
    ChartProvider provider = new ChartProvider(new Dimension(450, 275), ChartType.LINE_XY, data);
     provider.setColors(new Color[] { Color.BLUE });
     provider.setTitle("Inside Temperature (&#176;C / Hour)");
     ChartAxis axisX = new ChartAxis(ChartAxisType.BOTTOM);
     axisX.setLabels(generateXAxis(1));
     provider.addAxis(axisX);
     ChartAxis axisY = new ChartAxis(ChartAxisType.LEFT);
     axisY.setLabels(new String[] { "5", "10", "15", "20", "25", "30", "35", "40" });
     provider.addAxis(axisY);
 
     provider.addShapeMarker(new ShapeMarker(MarkerType.DIAMOND, Color.RED, 0, -1, 5));
     provider.setGrid(new ChartGrid(0, 100 / 11));
 
     add(new Chart("tempW", provider));
   }
 
   /**
    * Add month graph to the page.
    */
   private void addMonthGraph() {
     IChartData data = new AbstractChartData(ChartDataEncoding.TEXT, 100) {
 
       private static final long serialVersionUID = 1L;
 
       public double[][] getData() {
 
         double[] data = new double[7];
         Calendar current = Calendar.getInstance();
         long mFive = 5 * 24 * 60 * 60 * 1000L;
         long mInADay = 24 * 3600000;
         long time = (new Date()).getTime();
         long mMonthAgo =
             (30 * mInADay) + current.get(Calendar.HOUR_OF_DAY) * 3600000L
                 + current.get(Calendar.MINUTE) * 60000L + current.get(Calendar.SECOND) * 1000L
                 + current.get(Calendar.MILLISECOND);
         long mToday =
             current.get(Calendar.HOUR_OF_DAY) * 3600000L + current.get(Calendar.MINUTE) * 60000L
                 + current.get(Calendar.SECOND) * 1000L + current.get(Calendar.MILLISECOND);
 
         List<TimestampIntegerPair> temperatureList =
             SolarDecathlonApplication.getRepository().getHvacTemperatureSince(time - mMonthAgo);
 
         long totalValue = 0;
         int pointsFound = 0;
         for (int i = 6; i >= 0; i--) {
           for (int j = 0; j < temperatureList.size(); j++) {
 
             if ((temperatureList.get(j).getTimestamp() < ((time - mToday) - (mFive * (i - 1))))
                 && (temperatureList.get(j).getTimestamp() > ((time - mToday) - (mFive * i)))) {
               totalValue += temperatureList.get(j).getValue();
               pointsFound++;
             }
 
           }
           if (pointsFound > 0) {
             data[6 - i] = ((totalValue / pointsFound) - 5) * 2.856;
           }
           else {
             data[6 - i] = 0;
           }
 
           totalValue = 0;
           pointsFound = 0;
         }
 
         return new double[][] { {}, data };
       }
     };
 
    ChartProvider provider = new ChartProvider(new Dimension(450, 275), ChartType.LINE_XY, data);
     provider.setColors(new Color[] { Color.BLUE });
     provider.setTitle("Inside Temperature (&#176;C / Hour)");
     ChartAxis axisX = new ChartAxis(ChartAxisType.BOTTOM);
     axisX.setLabels(generateXAxis(2));
     provider.addAxis(axisX);
     ChartAxis axisY = new ChartAxis(ChartAxisType.LEFT);
     axisY.setLabels(new String[] { "5", "10", "15", "20", "25", "30", "35", "40" });
     provider.addAxis(axisY);
 
     provider.addShapeMarker(new ShapeMarker(MarkerType.DIAMOND, Color.RED, 0, -1, 5));
     provider.setGrid(new ChartGrid(0, 100 / 11));
 
     add(new Chart("tempM", provider));
   }
 
   /**
    * Generate and return the X axis.
    * 
    * @param type 0: day graph 1: week graph 2: month graph
    * @return A string array.
    */
   private String[] generateXAxis(int type) {
     // String[] axisDayLabels = new String[13];
     // String[] axisWeekLabels = new String[7];
     // String[] axisMonthLabels = new String[7];
     Calendar calendar = Calendar.getInstance();
 
     int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
     int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
     int currentMonth = calendar.get(Calendar.MONTH);
     int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
 
     if (type == 0) {
       String[] axisDayLabels = new String[13];
       for (int i = 0; i <= 12; i++) {
 
         currentHour %= 12;
         if (currentHour == 0) {
           currentHour = 12;
         }
         axisDayLabels[i] = currentHour + "H";
         currentHour += 2;
       }
       return axisDayLabels;
     }
 
     if (type == 1) {
       String[] axisWeekLabels = new String[7];
       String[] daysOfWeek = { "Sat", "Sun", "Mon", "Tues", "Wed", "Thurs", "Fri" };
       // x-axis is the past 7 days starting from current day.
       for (int i = 0; i <= 5; i++) {
         axisWeekLabels[i] = daysOfWeek[(currentDay + i + 1) % 7];
       }
       axisWeekLabels[6] = daysOfWeek[currentDay % 7];
 
       return axisWeekLabels;
     }
     else {
       String[] axisMonthLabels = new String[7];
       int[] months = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
 
       for (int i = 0; i <= 5; i++) {
         if ((currentDayOfMonth - ((6 - i) * 5)) <= 0) {
           axisMonthLabels[i] =
               String.valueOf(months[currentMonth - 1] - ((6 - i) * 5 - currentDayOfMonth));
         }
         else {
           axisMonthLabels[i] = String.valueOf(currentDayOfMonth - ((6 - i) * 5));
         }
       }
       axisMonthLabels[6] = String.valueOf(currentDayOfMonth);
 
       return axisMonthLabels;
     }
   }
 
   /**
    * Set the button label.
    * 
    * @param label The label.
    */
   public void setButtonLabel(String label) {
     this.buttonLabel = label;
   }
 
   /**
    * Get the button label. For PropertyModel to access.
    * 
    * @return The label.
    */
   public String getButtonLabel() {
     return this.buttonLabel;
   }
 
   /**
    * Set the button class attribute.
    * 
    * @param newClass The new class attribute.
    */
   public void setButtonClass(String newClass) {
     this.buttonClass = newClass;
   }
 
   /**
    * Set the button background color.
    * 
    * @param newColor The new color.
    */
   public void setButtonColor(String newColor) {
     this.buttonColor = newColor;
   }
 
 }
