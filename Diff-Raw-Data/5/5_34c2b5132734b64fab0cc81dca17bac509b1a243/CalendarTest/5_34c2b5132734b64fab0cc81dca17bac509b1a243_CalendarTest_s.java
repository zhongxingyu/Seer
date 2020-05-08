 /**
  * License Agreement.
  *
  *  JBoss RichFaces - Ajax4jsf Component Library
  *
  * Copyright (C) 2007  Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.richfaces.testng;
 
 import static org.ajax4jsf.bean.CalendarTestBean.DATE_FORMAT;
 import static org.ajax4jsf.bean.CalendarTestBean.DEFAULT_DATE;
 import static org.ajax4jsf.bean.CalendarTestBean.REQUIRED_MESSAGE;
 import static org.ajax4jsf.bean.CalendarTestBean.currentDateChangeListener;
 import static org.ajax4jsf.bean.CalendarTestBean.getDayInMay;
 import static org.ajax4jsf.bean.CalendarTestBean.valueChangeListener;
 
 import java.text.DateFormatSymbols;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.ajax4jsf.javascript.JSLiteral;
 import org.ajax4jsf.template.Template;
 import org.ajax4jsf.util.DateUtils;
 import org.richfaces.AutoTester;
 import org.richfaces.SeleniumTestBase;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.thoughtworks.selenium.SeleniumException;
 
 public class CalendarTest extends SeleniumTestBase {
 
     static final String RESET_METHOD = "#{calendarBean.reset}";
     
     static final String RESET_METHOD2 = "#{calendarBean.reset2}";
     
     static final String RESET_METHOD3 = "#{calendarBean.reset3}";
     
     static final String INIT_SHOW_ATTRIBUTES_TEST = "#{calendarBean.initShowAttributesTest}";
 
     static final String INIT_REQUIRED_TEST = "#{calendarBean.initRequiredTest}";
 
     static final String FORM_ID = "_form:";
 
     static final String SHOW_ATTRIBURES_TEST_URL = "pages/calendar/showAttributesTest.xhtml";
 
     static final String TODAY_CONTROL_AND_BOUNDARY_DATES_MODES_TEST_URL = "pages/calendar/todayControlAndBoundaryDatesModesTest.xhtml";
 
     static final String BUTTON_RELATED_TEST_URL = "pages/calendar/buttonRelatedAttributesTest.xhtml";
 
     static final String LAYOUT_TESTS_URL = "pages/calendar/layoutTests.xhtml";
     
     static final String IMMEDIATE_TEST_URL = "pages/calendar/testImmediate.xhtml";
     
     static final String EVENTS_TEST_URL = "pages/calendar/testEventsAttributes.xhtml";
     
     static final String STYLES_AND_CLASSES_TEST_URL = "pages/calendar/styleAndClassesTest.xhtml";
 
     static final String JOINTPOINT_DIRECTION_TEST_URL = "pages/calendar/jointPointAndDirectionAttributesTest.xhtml";
 
     static final String RESET_TIME_ON_DATE_SELECTION_TEST_URL = "pages/calendar/resetTimeOnDateSelectTest.xhtml";
 
     static final String FACETS_TEST_URL = "pages/calendar/facetsTest.xhtml";
 
     static final String CONTROLS_FORM_ID = "_controls:";
 
     static final String availableDayCellClass = "rich-calendar-cell-size rich-calendar-cell rich-calendar-btn";
     
     static final String disabledDayCellClass = "rich-calendar-cell-size rich-calendar-cell rich-calendar-boundary-dates";
     
     static final List<String> WEEK_DAYS_RU = new ArrayList<String>();
     static {
     	WEEK_DAYS_RU.add("\u041F\u043D");
     	WEEK_DAYS_RU.add("\u0412\u0442");
     	WEEK_DAYS_RU.add("\u0421\u0440");
     	WEEK_DAYS_RU.add("\u0427\u0442");
     	WEEK_DAYS_RU.add("\u041F\u0442");
     	WEEK_DAYS_RU.add("\u0421\u0431");
     	WEEK_DAYS_RU.add("\u0412\u0441");  	
     }
     
     
     static final DateFormatSymbols symbolsUS = new DateFormatSymbols(Locale.US); 
     
     static final DateFormatSymbols symbolsRU = new DateFormatSymbols(new Locale("ru"));
 
     String calendarId;
 
     String calendarHeaderId;
     
     String calendarFooterId;
 
     String weekDaysBarId;
 
     String ajaxSubmitId;
 
     String serverSubmitId;
 
     String statusId;
 
     String resetActionId;
     
     String resetAction2Id;
 
     String testClientModeId;
 
     String setupActionId;
 
     String ajaxSetupActionId;
 
     String dateSelectionXpath;
 
     String timeSelectionXpath;
 
     String timeSelectionXpathMinusDiv;
 
     String timeHoursSelectionId;
     
     String timeMinutesSelectionId;
     
     String timeSelectionOkButtonId;
     
     String timeSelectionCancelButtonId;
 
     String selectedDateId;
 
     String currentDateId;
 
     String isPopupId;
 
     String datePatternId;
 
     String timeZoneId;
 
     String localeId;
 
     String currentDateHeaderXpath;
 
     String popupButtonId;
 
     String inputDateId;
 
     String showApplyButtonId;
 
     String showHeaderId;
 
     String showFooterId;
 
     String showInputId;
 
     String showWeekDaysBarId;
 
     String showWeeksBarId;
 
     String showPopupId;
 
     String todayControlModeId;
 
     String todayControlXpath;
     
     String applyButtonXpath;
     
     String cleanButtonXPath;
     
     String closeHeaderXpath;
 
     String calendarMessageId;
 
     String firstWeekDayId;
 
     String isDisabledId;
 
     String defaultTimeId;
 
     String resetTimeOnDateSelectId;
 
     String jointPointId;
 
     String directionId;
     
     String enableManualInputId;
 
     String boundaryDatesModeId;
    
     String optionalHeaderFacetId;
 
     String optionalFooterFacetId;
     
     String headerFacetId;
 
     String footerFacetId;
 
     void initIds(String parentId) {
         calendarId = parentId + FORM_ID + "calendar";
         calendarHeaderId = calendarId + "Header";
         calendarFooterId = calendarId + "Footer";
         dateSelectionXpath = "//td[@id='"+calendarFooterId+"']/table/tbody/tr/td[1]";
         timeSelectionXpathMinusDiv = "//td[@id='"+calendarFooterId+"']/table/tbody/tr/td[3]";
         timeSelectionXpath = "//td[@id='"+calendarFooterId+"']/table/tbody/tr/td[3]/div";
         ajaxSubmitId = parentId + FORM_ID + "ajaxSubmit";
         serverSubmitId = parentId + FORM_ID + "serverSubmit";
         statusId = parentId + FORM_ID + "status";
         resetActionId = parentId + CONTROLS_FORM_ID + "resetAction";
         resetAction2Id = parentId + CONTROLS_FORM_ID + "resetAction2";
         testClientModeId = parentId + CONTROLS_FORM_ID + "testClientMode";
         setupActionId = parentId + CONTROLS_FORM_ID + "setup";
         ajaxSetupActionId = parentId + CONTROLS_FORM_ID + "ajaxSetup";
         timeHoursSelectionId = calendarId + "TimeHours";
         timeMinutesSelectionId = calendarId + "TimeMinutes";
         timeSelectionOkButtonId = calendarId + "TimeEditorButtonOk";
         timeSelectionCancelButtonId = calendarId + "TimeEditorButtonCancel";
         selectedDateId = parentId + CONTROLS_FORM_ID + "selectedDate";
         currentDateId = parentId + CONTROLS_FORM_ID + "currentDate";
         isPopupId  = parentId + CONTROLS_FORM_ID + "isPopup";
         datePatternId = parentId + CONTROLS_FORM_ID + "datePattern";
         timeZoneId = parentId + CONTROLS_FORM_ID + "timeZone";
         localeId = parentId + CONTROLS_FORM_ID + "locale";
         currentDateHeaderXpath = "//td[@id='"+calendarHeaderId+"']/table/tbody/tr/td[3]/div";
         closeHeaderXpath = "//td[@id='"+calendarHeaderId+"']/table/tbody/tr/td[last()]/div";
         popupButtonId = calendarId + "PopupButton";
         inputDateId = calendarId + "InputDate";
         showApplyButtonId = parentId + FORM_ID + "showApplyButton";
         showHeaderId = parentId + FORM_ID + "showHeader";
         showFooterId = parentId + FORM_ID + "showFooter";
         showInputId = parentId + FORM_ID + "showInput";
         showWeekDaysBarId = parentId + FORM_ID + "showWeekDaysBar";
         showWeeksBarId = parentId + FORM_ID + "showWeeksBar";
         showPopupId = parentId + FORM_ID + "showPopup";
         weekDaysBarId = calendarId + "WeekDay";
         todayControlModeId = parentId + FORM_ID + "todayControlMode";
         todayControlXpath = "//td[@id='" + calendarFooterId + "']/table/tbody/tr/td[5]";
         applyButtonXpath = "//td[@id='" + calendarFooterId + "']/table/tbody/tr/td[6]";
         cleanButtonXPath = "//td[@id='"+calendarFooterId+"']/table/tbody/tr/td[2]";
         calendarMessageId = parentId + FORM_ID + "calendarMsg";
         firstWeekDayId = parentId + FORM_ID + "firstWeekDay";
         isDisabledId = parentId + FORM_ID + "isDisabled";
         defaultTimeId = parentId + CONTROLS_FORM_ID + "defaultTime";
         resetTimeOnDateSelectId = parentId + CONTROLS_FORM_ID + "resetTimeOnDateSelect";
         jointPointId = parentId + FORM_ID + "jointPoint";
         directionId = parentId + FORM_ID + "direction";
         enableManualInputId = parentId + FORM_ID + "enableManualInput";
         boundaryDatesModeId = parentId + FORM_ID + "boundaryDatesMode";
         optionalHeaderFacetId = calendarId + "HeaderOptional";
         optionalFooterFacetId = calendarId + "FooterOptional";
         headerFacetId = calendarId + "Header";
         footerFacetId = calendarId + "Footer";
     }
 
     String getStatus() {
         return getTextById(statusId);
     }
 
     void assertListeners(String... listener) {
         String status = getStatus();
         String s = status;
         String sum = "";
         for (String l : listener) {
             if (status.indexOf(l) == -1) {
                 Assert.fail(l + " has been skipped");
             } else {
                 s = s.replace(l, "");
             }
             sum += l;
         }
         if (s.length() > 0) {
             Assert.fail("The following listener were called but shouldn't: " + s);
         }
         if (!status.equals(sum)) {
             Assert.fail("Order of listeners call is incorrect. Should be: " + sum + ". But was : " + status);
         }
     }
 
     void changeDate() {
         String weekNumId = calendarId + "WeekNum2";
         selenium.click("//tr[@id='" + weekNumId + "']/td[starts-with(@class, '" + availableDayCellClass + "')]");
     }
     
     void changeCurrentDate(boolean wait4ajax) {
         selenium.click("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td/div");
         if (wait4ajax) {
             waitForAjaxCompletion();
         }
     }
     
     Calendar changeCurrentDate(Calendar c, boolean wait4ajax) {
     	selenium.click("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td/div");
         if (wait4ajax) {
             waitForAjaxCompletion();
         }
         c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
         if (getCalendarDate().indexOf(c.get(Calendar.YEAR)) == -1) {
         	Assert.fail("Date is incorrect");
         }
         return c;
     }
 
     void reset() {
         clickCommandAndWait(resetActionId);
     }
 
     void switchToClientMode() {
         clickCommandAndWait(testClientModeId);
     }
     
     void showTimeSelectionWindow() {
     	selenium.click(timeSelectionXpath);
     }
     
     String getCalendarDate() {
     	return selenium.getText(currentDateHeaderXpath);
     }
     
     Calendar nextMonth(Calendar c) {
     	String date = getCalendarDate();
     	selenium.click("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][3]/div");
     	Assert.assertFalse(getCalendarDate().equals(date), "Current date has not been changed after next month clicked");
     	c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);
     	c.set(Calendar.DATE, 1);
     	return c;
     }
     
     Calendar previousMonth(Calendar c) {
     	String date = getCalendarDate();
     	selenium.click("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][2]/div");
     	Assert.assertFalse(getCalendarDate().equals(date), "Current date has not been changed after previous month clicked");
     	c.set(Calendar.MONTH, c.get(Calendar.MONTH) - 1);
     	c.set(Calendar.DATE, 1);
     	return c;
     }
     
     void _testModelDataAppliedToClient(Calendar c) {
     	 String currentDate = selenium.getText(dateSelectionXpath);
          selenium.click("//table[@id='"+calendarId+"']/tbody/tr/td[text() = '13']");
          
          if (!selenium.getText(dateSelectionXpath).equals(currentDate)) {
          	Assert.fail("Enabled attribute of data model was not applied for client data. 13th day should disabled and should force date change after click.");
          }
          
          int currentMonth = c.get(Calendar.MONTH);
          
          String cellDay7StyleClass = selenium.getAttribute("//table[@id='"+calendarId+"']/tbody/tr/td[text() = '7']/@class");
          String cellDay7ModelClass = "styleClass"+currentMonth+"7";
          if (cellDay7StyleClass == null || cellDay7StyleClass.indexOf(cellDay7ModelClass) == -1) {
          	Assert.fail("Style class was not applied from data model to cell days. Style class for 7th day should contain [" + cellDay7ModelClass + "]. But has only + [" + cellDay7StyleClass +"]");
          }
          
          String cellDay5StyleClass = selenium.getAttribute("//table[@id='"+calendarId+"']/tbody/tr/td[text() = '5']/@class");
          String cellDay5ModelClass = "styleClass"+currentMonth+"5";
          if (cellDay5StyleClass == null || cellDay5StyleClass.indexOf(cellDay5ModelClass) == -1) {
          	Assert.fail("Style class was not applied from data model to cell days. Style class for 5th day should contain [" + cellDay5ModelClass + "]. But has only + [" + cellDay5StyleClass +"]");
          }
     }
     
     void testInternatialization(String prefix) {
     	String label = selenium.getText(todayControlXpath);
     	String expectedLabel = ("Today" + prefix);
     	if (!expectedLabel.equals(label)) {
     		Assert.fail("Internatialization was not applied. Expected label for 'Today' button: ["+expectedLabel+"]. But was ["+label+"]");
     	}
     	
     	label = selenium.getText(cleanButtonXPath);
     	expectedLabel = ("Clean" + prefix);
     	if (!expectedLabel.equals(label)) {
     		Assert.fail("Internatialization was not applied. Expected label for 'Clean' button: ["+expectedLabel+"]. But was ["+label+"]");
     	}
     	
     	label = selenium.getText(timeSelectionOkButtonId);
     	expectedLabel = ("OK" + prefix);
     	if (!expectedLabel.equals(label)) {
     		Assert.fail("Internatialization was not applied. Expected label for 'OK' button: ["+expectedLabel+"]. But was ["+label+"]");
     	}
     	
     	label = selenium.getText(timeSelectionCancelButtonId);
     	expectedLabel = ("Cancel" + prefix);
     	if (!expectedLabel.equals(label)) {
     		Assert.fail("Internatialization was not applied. Expected label for 'Cancel' button: ["+expectedLabel+"]. But was ["+label+"]");
     	}
     	
     	label = selenium.getText(closeHeaderXpath);
     	expectedLabel = ("Close" + prefix);
     	if (!expectedLabel.equals(label)) {
     		Assert.fail("Internatialization was not applied. Expected label for 'Close' button: ["+expectedLabel+"]. But was ["+label+"]");
     	}
 
 
     	
     }
     
     // Checks months label with ru locale
     private void testWeekDays() {
     	String weekDaysLabelsId = calendarId + "WeekDay";
     	int l = selenium.getXpathCount("//tr[@id='"+weekDaysLabelsId+"']/td").intValue();
     	List<String> weekDays = new ArrayList<String>(); 
     	for (int i = 1; i <= l; i++) {
     		weekDays.add(selenium.getText("//tr[@id='"+weekDaysLabelsId+"']/td["+i+"]"));
     	}
     	
     	for (String weekday : WEEK_DAYS_RU) {
     		if (!weekDays.contains(weekday)) {
     			Assert.fail("Internationalization failed. Weekdays should contain ["+weekday+"] in case of RU locale. But was: " + weekDays.toString());    			
     		}
     	}
     }
     
     
     // Check current date str
     private void checkCurrentDate(String date, String message) {
     	if (message == null) {
     		message = "";
     	}
     	Calendar c = Calendar.getInstance();
     	if (!date.contains(String.valueOf(c.get(Calendar.YEAR)))) {
     		Assert.fail(message + "Current date is invalid. Date string ["+date+"] does not contain current year");
     	}
     	if (!date.contains(symbolsUS.getShortMonths()[c.get(Calendar.MONTH)])) {
     		Assert.fail(message + "Current date is invalid. Date string ["+date+"] does not contain current month");
     	}
 
     	if (!date.contains(String.valueOf(c.get(Calendar.DATE)))) {
     		Assert.fail(message + "Current date is invalid. Date string ["+date+"] does not contain current day");
     	}
 
     	if (!date.contains(symbolsUS.getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)])) {
     		Assert.fail(message + "Current date is invalid. Date string ["+date+"] does not contain current day of week");
     	}
 
     	
     }
     
     private void checkCurrentDate(Calendar c) {
     	String headerDate = getCalendarDate();
     	String expected = symbolsUS.getMonths()[c.get(Calendar.MONTH)];//   c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
     	if (!headerDate.contains(expected)) {
     		Assert.fail("Calendar displays invalid date. It should contain ["+expected+"]. But was ["+headerDate+"]");
     	}
     	
     	expected = String.valueOf(c.get(Calendar.YEAR));
     	if (!headerDate.contains(expected)) {
     		Assert.fail("Calendar displays invalid date. It should contain ["+expected+"]. But was ["+headerDate+"]");
     	}
     }
     
     @Test
     public void testStylesAndClasses(Template template) {
     	renderPage(STYLES_AND_CLASSES_TEST_URL, template, RESET_METHOD);
     	initIds(getParentId());
     	
     	showPopup();
     	
     	AssertTextEquals(popupButtonId, "Click", "Text for popup button has not been applied");
     	assertStyleAttributeContains(inputDateId, "color: green", "Style for input has bot been applied");
     	assertClassNames(inputDateId, new String [] {"inputClass"}, "Css class for input has not been applied", true);
     	
     	assertClassNames(calendarId, new String [] {"rich-calendar-exterior","rich-calendar-popup","styleClass"}, "Css classes for component were rendered incorrectly", true);
     	assertStyleAttributeContains(calendarId, "font-weight: bold", "Style was not applied for component");
     	
     	assertClassNames(calendarHeaderId, new String [] { "rich-calendar-header" }, "", true);
     }
 
     @Test
     public void testEventsAttributes(Template template) {
     	renderPage(EVENTS_TEST_URL, template, RESET_METHOD);
     	initIds(getParentId());
     	List<String> events = new ArrayList<String>();
     	
     	showPopup();
     	
 
     	events.add("onexpand");
     	events.add("ondateselect");
     	events.add("oncollapse");
     	
     	changeDate();
     	assertEvents(events);
     	
     	events.add("ondatemouseover");   	
     	events.add("ondatemouseout");
     	
     	String weekNumId = calendarId + "WeekNum2";
     	selenium.mouseOver("//tr[@id='" + weekNumId + "']/td[starts-with(@class, '" + availableDayCellClass + "')]");
     	selenium.mouseOut("//tr[@id='" + weekNumId + "']/td[starts-with(@class, '" + availableDayCellClass + "')]");
     	assertEvents(events);
     	
     	events.add("oncurrentdateselect");
     	
     	changeCurrentDate(false);
     	assertEvents(events);
     	
     	type(inputDateId, "a");
     	assertEvent("oninputchange");
     	assertEvent("oninputkeydown");
     	assertEvent("oninputkeypress");
     	assertEvent("oninputkeyup");
     	
     	clickById(inputDateId);
     	assertEvent("oninputclick");
     	
     }
     
     @Test // Test is the same as for internationalization
     public void testLocaleAttribute(Template template) {
     	renderPage(template, RESET_METHOD);
     	initIds(getParentId());
     	
     	// Failed due to bug https://jira.jboss.org/jira/browse/RF-5330
 
      	setPopup(true);
     	String changeLocaleButtonId = getParentId() + CONTROLS_FORM_ID + "testLocale";
     	clickCommandAndWait(changeLocaleButtonId);
     	
     	showPopup();
     	showTimeSelectionWindow();
     	
     	testInternatialization("_ru");
     	testWeekDays();
     	
     }
     
     @Test
     public void testImmediate(Template template) {
     	renderPage(IMMEDIATE_TEST_URL, template, RESET_METHOD);
     	initIds(getParentId());
     	
     	String hMessageId = getParentId() + FORM_ID + "message";
     	String commandId = getParentId() + FORM_ID + "submit";
     	clickCommandAndWait(commandId);
     	
     	AssertNotPresent(hMessageId, "Immediate attribute does not work. h:message for required input should be absent, because validation for calendar should be failed before on APPLY_REQUEST phase.");
     	
     }
 
     @Test
     public void testAjaxSingle(Template template) {
     	AutoTester tester = getAutoTester(this);
     	tester.renderPage(template, RESET_METHOD);
     	calendarId = tester.getClientId(AutoTester.COMPONENT_ID, template);
     	calendarHeaderId = calendarId + "Header";
     	
     	tester.testAjaxSingle(false);
     }
     
     @Test
     public void testProcessAttribute(Template template) {
     	AutoTester tester = getAutoTester(this);
     	tester.renderPage(template, RESET_METHOD);
     	calendarId = tester.getClientId(AutoTester.COMPONENT_ID, template);
     	calendarHeaderId = calendarId + "Header";
     	
     	tester.testProcessAttribute();
     }
 
    // Erroneous test case. 
    /* @Test
     public void testNULLModel(Template template) {
     	renderPage(template, RESET_METHOD);
     	initIds(getParentId());
     	
     	String commandId = getParentId() + CONTROLS_FORM_ID + "testNullModel";
     	clickCommandAndWait(commandId);
 
     	changeCurrentDate(false);
         pause(1000, calendarId);
    		Assert.assertTrue(runScript("window.done").equals(Boolean.FALSE.toString().toLowerCase()), "Change of current date should not force ajax request in case of NULL dat model.");
        
     	
     }*/
 
     //https://jira.jboss.org/jira/browse/RF-5379
     @Test(groups=FAILURES_GROUP)
     public void testJSAPI(Template template) {
     	renderPage(template, RESET_METHOD);
     	initIds(getParentId());
     	
     	switchToClientMode();
     	
     	Calendar c =  Calendar.getInstance();
     	
     	// Test getSelectedDate
     	String date = invokeFromComponent(calendarId, "getSelectedDate", null);
     	checkCurrentDate(date, "'getSelectedDate' JS API function does not work");
     	checkCurrentDate(c);
     	
     	//Test 'getCurrentYear'
     	String year = invokeFromComponent(calendarId, "getCurrentYear", null);
     	String expected = String.valueOf(c.get(Calendar.YEAR));
     	if (!year.equals(expected)) {
     		Assert.fail("'getCurrentYear' JS API function does not. Expected year: ["+expected+"]. But was ["+year+"]");
     	}
     	
     	//Test 'getCurrentMonth'
     	String month = invokeFromComponent(calendarId, "getCurrentMonth", null);
     	expected = String.valueOf(c.get(Calendar.MONTH));
     	if (!month.equals(expected)) {
     		Assert.fail("'getCurrentMonth' JS API function does not. Expected month: ["+expected+"]. But was ["+month+"]");
     	}
     	
    	
     	//Test 'nextMonth'
     	c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);
     	invokeFromComponent(calendarId, "nextMonth", null);
     	month = invokeFromComponent(calendarId, "getCurrentMonth", null);
     	expected = String.valueOf(c.get(Calendar.MONTH));
     	if (!month.equals(expected)) {
     		Assert.fail("'nextMonth' JS API function does not. Expected month after 'nextMonth' method is ["+expected+"]. But was ["+month+"]");
     	}
     	checkCurrentDate(c);
     	
     	//Test 'nextYear'
     	c = Calendar.getInstance();
     	c.set(c.get(Calendar.YEAR) + 1, c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE));
     	invokeFromComponent(calendarId, "nextYear", null);
     	year = invokeFromComponent(calendarId, "getCurrentYear", null);
     	expected = String.valueOf(c.get(Calendar.YEAR));
     	if (!year.equals(expected)) {
     		Assert.fail("'nextYear' JS API function does not. Expected year after 'nextYear' method is ["+expected+"]. But was ["+year+"]");
     	}
     	checkCurrentDate(c);
     	
     	
     	//Test 'selectDate(date)'
     	c = Calendar.getInstance();
     	invokeFromComponent(calendarId, "selectDate", new JSLiteral("new Date()"));
     	date = invokeFromComponent(calendarId, "getSelectedDate", null);
     	checkCurrentDate(date, "'selectDate' JS API function does not work. Date should be swtiched to current date. ");
     	checkCurrentDate(c);
     	
     	//Test 'prevMonth()'
     	c = Calendar.getInstance();
     	c.set(Calendar.MONTH, c.get(Calendar.MONTH) - 1);
     	invokeFromComponent(calendarId, "prevMonth", null);
     	month = invokeFromComponent(calendarId, "getCurrentMonth", null);
     	expected = String.valueOf(c.get(Calendar.MONTH));
     	if (!month.equals(expected)) {
     		Assert.fail("'prevMonth' JS API function does not. Expected month after 'prevMonth' method is ["+expected+"]. But was ["+month+"]");
     	}
     	checkCurrentDate(c);
     	
     	
     	//Test 'prevYear'
     	c = Calendar.getInstance();
     	c.set(c.get(Calendar.YEAR) - 1, c.get(Calendar.MONTH) - 1, c.get(Calendar.DATE));
     	invokeFromComponent(calendarId, "prevYear", null);
     	year = invokeFromComponent(calendarId, "getCurrentYear", null);
     	expected = String.valueOf(c.get(Calendar.YEAR));
     	if (!year.equals(expected)) {
     		Assert.fail("'prevYear' JS API function does not. Expected year after 'prevYear' method is ["+expected+"]. But was ["+year+"]");
     	}
     	checkCurrentDate(c);
     	
     	//Test 'today'
     	c = Calendar.getInstance();
     	invokeFromComponent(calendarId, "today", null);
     	date = invokeFromComponent(calendarId, "getSelectedDate", null);
     	checkCurrentDate(date, "'today' JS API function does not work. Date should be swtiched to current date");
     	checkCurrentDate(c);
     	
     	
     	//Test 'getData' https://jira.jboss.org/jira/browse/RF-4806
     	c = Calendar.getInstance();
     	String data = invokeFromComponent(calendarId, "getData",  new JSLiteral("new Date()"));
     	String expectedData = "data" + c.get(Calendar.DATE);
     	if (!data.equals("data" + c.get(Calendar.DATE))) {
     		Assert.fail("'getData' JS API function does not work. Expected data for current date is ["+expectedData+"]. But was ["+data+"]");
     	}
     	
     	//Test 'resetSelectedDate'
     	c = Calendar.getInstance();
     	invokeFromComponent(calendarId, "resetSelectedDate", null);
     	date = selenium.getText(dateSelectionXpath);
     	if (!"".equals(date)) {
     		Assert.fail("'resetSelectedDate' JS API function does not work. Selected date should not be displayed on component footer. But was ["+date+"]");
     	}
     	date = invokeFromComponent(calendarId, "getSelectedDate", null);
     	if (!"null".equals(date)) {
     		Assert.fail("'resetSelectedDate' JS API function does not work. Selected date was not reset. Is was ["+date+"]");
     	}
     	checkCurrentDate(c);
     	
     	
     	//Test doCollapse() 
     	setPopup(true);
     	setup();
     	showPopup();  // Show popup
     	invokeFromComponent(calendarId, "doCollapse", null);  // Hide popup
     	AssertNotVisible(calendarId, "'doCollapse' does not work. Calendar popup has not been hidden");
     	
     	// Test doExpand 
     	invokeFromComponent(calendarId, "doExpand", null); // Show popup
     	AssertVisible(calendarId, "'doExpand' does not work. Calendar popup has not been shown");
     	
     	// Test doSwitch
     	invokeFromComponent(calendarId, "doSwitch", null);  // Hide popup
     	AssertNotVisible(calendarId, "'doSwitch' does not work. Calendar popup has not been hidden");
     	invokeFromComponent(calendarId, "doSwitch", null);  // Show popup
     	AssertVisible(calendarId, "'doSwitch' does not work. Calendar popup has not been shown");
     	
     	
     }
 
     @Test
     public void testRenderedAttribute(Template template) {
     	AutoTester tester = getAutoTester(this);
     	tester.renderPage(template, RESET_METHOD);
     	tester.testRendered();
     }
 
     @Test
     public void testInternationalization(Template template) {
     	renderPage(template, RESET_METHOD);
     	initIds(getParentId());
         setPopup(true);
     	String changeLocaleButtonId = getParentId() + CONTROLS_FORM_ID + "testLocale";
     	clickCommandAndWait(changeLocaleButtonId);
     	
     	showPopup();
     	showTimeSelectionWindow();
     	
     	testInternatialization("_ru");
     	testWeekDays();
        
     }
 
     @Test
     public void testLimitToListAttribute(Template template) {
     	AutoTester tester = getAutoTester(this);
     	tester.renderPage(template, RESET_METHOD);
     	calendarId = tester.getClientId(AutoTester.COMPONENT_ID, template);
     	calendarHeaderId = calendarId + "Header";
     	tester.testLimitToList();
     }
 
     @Test
     public void testReRenderAttribute(Template template) {
     	AutoTester tester = getAutoTester(this);
     	tester.renderPage(template, RESET_METHOD);
     	calendarId = tester.getClientId(AutoTester.COMPONENT_ID, template);
     	calendarHeaderId = calendarId + "Header";
     	currentDateHeaderXpath = "//td[@id='"+calendarHeaderId+"']/table/tbody/tr/td[3]/div";
     	tester.testReRender();
     }
     
     @Test
     public void testConverterAttribute(Template template) {
     	AutoTester tester = getAutoTester(this);
     	tester.renderPage(template, RESET_METHOD);
     	calendarId = tester.getClientId(AutoTester.COMPONENT_ID, template);
     	tester.testConverterAttribute();
     }
     
     @Test
     public void testValidatorAndValidatorMessageAttributes(Template template) {
     	AutoTester tester = getAutoTester(this);
     	tester.renderPage(template, RESET_METHOD);
     	calendarId = tester.getClientId(AutoTester.COMPONENT_ID, template);
     	tester.testValidatorAndValidatorMessageAttributes(false);
     }
 
     @Test
     public void testClientMode(Template template) {
     	renderPage(template, RESET_METHOD);
         initIds(getParentId());
         switchToClientMode();
         
         Calendar c = Calendar.getInstance();
         c = previousMonth(c);
         pause(1000, calendarId);
 		Assert.assertTrue(runScript("window.done").equals(Boolean.FALSE.toString().toLowerCase()), "Change of current date should not force ajax request in client mode.");
         
         try {
 			_testModelDataAppliedToClient(c);
 			c = nextMonth(c);
 			_testModelDataAppliedToClient(c);
 			c = nextMonth(c);
 			_testModelDataAppliedToClient(c);
 		} catch (AssertionError e) {
 			Assert.fail("Calendar does not preload data in client mode for interval defined by 'preloadDateRangeBegin' & 'preloadDateRangeEnd'. It should preload data for last, current & next month. " + e);
 		}
         
 		boolean error = false;
 		c = nextMonth(c);
 		try {
 			_testModelDataAppliedToClient(c);
 		}catch (AssertionError e) {
 			error = true;
 		}
 		
 		if (!error) {
 			Assert.fail("Calendar pre-loaded data for interval that not defined by 'preloadDateRangeBegin' & 'preloadDateRangeEnd'");
 		}
 		
     }
     
     //https://jira.jboss.org/jira/browse/RF-6475
     @Test(groups=FAILURES_GROUP)
     public void testDataModelAttribute(Template template) {
     	renderPage(template, RESET_METHOD);
         initIds(getParentId());
         
         Calendar c = Calendar.getInstance();
         
         _testModelDataAppliedToClient(c);
         
         try {
         	c = changeCurrentDate(c, true);
         }catch (SeleniumException exception) {
 			Assert.fail("Celendar in ajax mode does not request for the next portion of data after current date has been changed");
 		}
         
         _testModelDataAppliedToClient(c);
       
     }
     
     @Test
     public void testTimeSelection(Template template) {
     	renderPage(template, RESET_METHOD);
         initIds(getParentId());
        
         
         // Test time selection appearing
         showTimeSelectionWindow();
         AssertVisible(calendarId + "TimeEditorLayout", "Time selection dialog was not opened");
         AssertVisible(timeHoursSelectionId, "'Hour' input was not displayed");
         AssertVisible(timeMinutesSelectionId, "'Minutes' input was not displayed");
         AssertVisible(timeSelectionOkButtonId, "Time selection 'OK' button was not displayed");
         AssertVisible(timeSelectionCancelButtonId, "Time selection 'Cancel' button was not displayed");
         
         // Test time selection applying
         setValueById(timeHoursSelectionId, "10");
         setValueById(timeMinutesSelectionId, "00");
         clickById(timeSelectionOkButtonId);
         AssertNotVisible(calendarId + "TimeEditorLayout", "Time selection dialog was not hidden after 'Ok' button clicked");
         
         String time = selenium.getText(timeSelectionXpath);
         if (!"10:00".equals(time)) {
         	Assert.fail("Time selected has not been applied. Expected [10:00]. But was + [" + time+ "]");
         }
         
         // Test time cancel button 
         showTimeSelectionWindow();
         
         setValueById(timeHoursSelectionId, "18");
         setValueById(timeMinutesSelectionId, "50");
         clickById(timeSelectionCancelButtonId);
         AssertNotVisible(calendarId + "TimeEditorLayout", "Time selection dialog was not hidden after 'Cancel' button clicked");
         
         time = selenium.getText(timeSelectionXpath);
         if (!"10:00".equals(time)) {
         	Assert.fail("Time applied after cancel button clicked. Expected [10:00]. But was + [" + time+ "]");
         }
        
         // Test time applying in server side
     
         clickCommandAndWait(serverSubmitId);
         time = selenium.getText(timeSelectionXpath);
         if (!"10:00".equals(time)) {
         	Assert.fail("Time was not applied on server side. Expected [10:00]. But was + [" + time+ "]");
         }
         
     }
 
     @Test(groups=FAILURES_GROUP)
     //https://jira.jboss.org/jira/browse/RF-5209
     public void testListenersInAjaxMode(Template template) {
         renderPage(template, RESET_METHOD);
         initIds(getParentId());
 
         changeDate();
 
         clickAjaxCommandAndWait(ajaxSubmitId);
         assertListeners(valueChangeListener);
 
         reset();
 
         changeCurrentDate(true);
         changeDate();
         clickAjaxCommandAndWait(ajaxSubmitId);
         assertListeners(valueChangeListener, currentDateChangeListener);
 
     }
 
     @Test(groups=FAILURES_GROUP)
     //https://jira.jboss.org/jira/browse/RF-5209
     public void testListenersInClientMode(Template template) {
         renderPage(template, RESET_METHOD);
         initIds(getParentId());
         switchToClientMode();
 
         changeDate();
 
         clickCommandAndWait(serverSubmitId);
         assertListeners(valueChangeListener);
 
         reset();
         switchToClientMode();
 
         changeCurrentDate(false);
         changeDate();
         clickCommandAndWait(serverSubmitId);
         assertListeners(valueChangeListener, currentDateChangeListener);
 
     }
 
     @Test
     public void testCalendarComponent(Template template) {
         renderPage(template, RESET_METHOD2);	
 
         String containerId = getParentId() + "_form:";
         String calendarOpenedId = containerId + "calendar";
         String calendarCollapsedId = calendarOpenedId + "Popup";
         String calendarInputDate = calendarOpenedId + "InputDate";
         String calendarPopupButton = calendarCollapsedId + "Button";
         String outputPanel = containerId + "outputPanel";
 
         Assert.assertFalse(isVisible(calendarOpenedId), "Calendar window should NOT be visible on the component!");
 
         writeStatus("Mouse click on calendar InputDate field");
         clickById(calendarInputDate);
 
         Assert.assertTrue(isVisible(calendarOpenedId), "Calendar window should be visible on the component!");
 
         writeStatus("Mouse click outside calendar");
         clickById(outputPanel);
 
         Assert.assertFalse(isVisible(calendarOpenedId), "Calendar window should NOT be visible on the component!");
 
         writeStatus("Mouse click on calendar popup button");
         clickById(calendarPopupButton);
 
         Assert.assertTrue(isVisible(calendarOpenedId), "Calendar window should be visible on the component!");
     }
 
     @Test
     public void testSelectDateComponent(Template template) {
         renderPage(template, RESET_METHOD2);
         initIds(getParentId());
         Date newSelectedDate = getDayInMay(15);
 
         String containerId = getParentId() + "_form:";
         String calendarOpenedId = containerId + "calendar";
         String calendarCollapsedId = calendarOpenedId + "Popup";
         String calendarPopupButton = calendarCollapsedId + "Button";
         
 
         Assert.assertFalse(isVisible(calendarOpenedId), "Calendar window should NOT be visible on the component!");
         writeStatus("Mouse click on calendar popup button");
         clickById(calendarPopupButton);
         Assert.assertTrue(isVisible(calendarOpenedId), "Calendar window should be visible on the component!");
 
         String inputDateString = getValueById(selectedDateId);
         Date readDate = null;
         try {
             readDate = DATE_FORMAT.parse(inputDateString);
         } catch (ParseException parseException) {
             // skip exception
         }
         Assert.assertEquals(readDate, DEFAULT_DATE, "Default date representation is wrong!");
 
         // click on 15th of May
         String newSelectedDateId = calendarOpenedId + "DayCell18";
         clickById(newSelectedDateId);
         
         clickCommandAndWait(serverSubmitId);
 
         inputDateString = getValueById(selectedDateId);
         try {
             readDate = DATE_FORMAT.parse(inputDateString);
         } catch (ParseException parseException) {
             // skip exception
         }
         Assert.assertEquals(readDate, newSelectedDate, "Date representation after selecting 15.May.2008 is wrong!");
 
         Assert.assertFalse(isVisible(calendarOpenedId), "Calendar window should NOT be visible on the component!");
     }
 
     @Test
     public void testValueAndCurrentDateOfCalendarInCaseOfPopupTrue(Template template) {
         renderPage(template, RESET_METHOD3);
         initIds(getParentId());
         String expectedSelectedDate = "03/03/2007 11:00";
         String expectedCurrentDate = "04/04/2008 13:00";
         setPopup(true);
 
         writeStatus("Check that in popup mode currentDate attribute is ignored in all cases");
 
         //1. value != null + curr_date != null
         setValueById(selectedDateId, expectedSelectedDate);
         setValueById(currentDateId, expectedCurrentDate);
         setup();
 
         showPopup();
         writeStatus("Check calendar popup is shown up");
         Assert.assertTrue(isVisible(calendarId), "Calendar popup is not visible");
 
         writeStatus("Check selected date");
         AssertValueEquals(inputDateId, expectedSelectedDate, "Calendar shows wrong date");
 
         writeStatus("Check current month and year. Remind! Current date has to be ignored. Value date is used instead");
         String currentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(currentDate, "March, 2007", "Calendar shows wrong current date");
 
         //2. value == null + curr_date != null
         setValueById(selectedDateId, "");
         setValueById(currentDateId, expectedCurrentDate);
         setup();
 
         showPopup();
         writeStatus("Check selected date is empty");
         AssertValueEquals(inputDateId, "", "Calendar value must be empty");
 
         writeStatus("Check current month and year. Remind! Current date has to be ignored as before. Value date is empty, present date is used");
         currentDate = selenium.getText(currentDateHeaderXpath);
 
         Locale locale = new Locale(selenium.getText(localeId));
         Date presentTime = Calendar.getInstance(locale).getTime();
         String month = DateUtils.month(presentTime, locale);
         int year = DateUtils.year(presentTime);
         String month_year = month + ", " + year;
 
         Assert.assertEquals(currentDate, month_year, "Calendar shows wrong current date");
     }
 
     @Test
     public void testValueAndCurrentDateOfCalendarWithPopupFalse(Template template) {
         renderPage(template, RESET_METHOD);
         initIds(getParentId());
         String expectedSelectedDate = "03/03/2007 11:00";
         String expectedCurrentDate = "04/04/2008 13:00";
         setPopup(false);
 
         //1. value != null + curr_date != null
         writeStatus("Check whether the component is present and up to the mark if value and currentDate are defined");
         setValueById(selectedDateId, expectedSelectedDate);
         setValueById(currentDateId, expectedCurrentDate);
         setup();
 
         writeStatus("Check calendar panel has been rendered");
         Assert.assertTrue(isVisible(calendarId), "Calendar panel is not visible");
 
         writeStatus("Check selected date");
         String date = selenium.getText(dateSelectionXpath);
         String time = selenium.getText(timeSelectionXpath);
         String date_time = date + " " + time;
         Assert.assertEquals(date_time, expectedSelectedDate, "Calendar shows wrong date");
 
         writeStatus("Check current month and year");
         String currentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(currentDate, "April, 2008", "Calendar shows wrong current date");
 
         //2. value != null + curr_date == null
         writeStatus("Check whether the component is present and up to the mark if value is given but currentDate is not");
 
         setValueById(selectedDateId, expectedSelectedDate);
         setValueById(currentDateId, "");
         setup();
 
         writeStatus("Check selected date");
         date = selenium.getText(dateSelectionXpath);
         time = selenium.getText(timeSelectionXpath);
         date_time = date + " " + time;
         Assert.assertEquals(date_time, expectedSelectedDate, "Calendar shows wrong date");
 
         writeStatus("Check current month and year. Current date is not specified. Value date will be used instead");
         currentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(currentDate, "March, 2007", "Calendar shows wrong current date");
 
         //3. value == null + curr_date == null
         writeStatus("Check whether the component is present and up to the mark if value and currentDate are not defined");
         setValueById(selectedDateId, "");
         setValueById(currentDateId, "");
         setup();
 
         writeStatus("Selected date is null. Selected value panel is not visible");
         Assert.assertFalse(isVisible(dateSelectionXpath), "Footer with selected date has to be invisible");
         Assert.assertFalse(isVisible(timeSelectionXpathMinusDiv), "Footer with selected date has to be invisible");
 
         writeStatus("Check current month and year. Current date and value are not defined. Present (do not mix with current time 8))");
         currentDate = selenium.getText(currentDateHeaderXpath);
 
         Locale locale = new Locale(selenium.getText(localeId));
         Date presentTime = Calendar.getInstance(locale).getTime();
         String month = DateUtils.month(presentTime, locale);
         int year = DateUtils.year(presentTime);
         String month_year = month + ", " + year;
 
         Assert.assertEquals(currentDate, month_year, "Calendar shows wrong current date");
     }
 
     @Test
     public void testDatePatternNonPopupMode(Template template) {
         renderPage(template, RESET_METHOD2);
         initIds(getParentId());
         String originalPattern = "MM/dd/yyyy HH:mm";
         writeStatus("Check 'datePattern' attribute in non-popup mode");
 
         String selectedDate = "03/03/2007 11:00";
         String expectedSelectedDate = "03/2007";
         String expectedPattern = "MM/yyyy";
 
         setValueById(selectedDateId, selectedDate);
         setValueById(currentDateId, "");
         setValueById(datePatternId, expectedPattern);
         setPopup(false);
         setup();
 
         try {
             writeStatus("Check displayed date. It should be in 'MM/yyyy' format - time part is not visible at all");
             String date = selenium.getText(dateSelectionXpath);
             Assert.assertEquals(date, expectedSelectedDate, "Calendar shows date in wrong format");
             Assert.assertFalse(isVisible(timeSelectionXpathMinusDiv), "Time part has to be invisible");
         } catch (AssertionError ae) {
             Assert.fail(ae.getMessage());
         } finally {
             setValueById(datePatternId, originalPattern);
             setup();
         }
     }
 
     @Test
     public void testDatePatternPopupMode(Template template) {
         renderPage(template, RESET_METHOD2);
         initIds(getParentId());
         showPopup();
 
         String originalPattern = "MM/dd/yyyy HH:mm";
         writeStatus("Check 'datePattern' attribute in popup mode");
 
         String selectedDate = "03/03/2007 11:00";
         String expectedSelectedDate = "03-03-2007";
         String expectedPattern = "dd-MM-yyyy";
 
         setValueById(selectedDateId, selectedDate);
         setValueById(currentDateId, "");
         setValueById(datePatternId, expectedPattern);
 
         setup();
         showPopup();
         
         try {
             writeStatus("Check calendar popup is shown up and date in the input field is in proper format");
             AssertValueEquals(calendarId + "InputDate", expectedSelectedDate, "Calendar input field shows wrong date");
 
             writeStatus("Check displayed date again. It as before should be in 'MM/yyyy' format - time part is not visible at all");
             String date = selenium.getText(dateSelectionXpath);
             Assert.assertEquals(date, expectedSelectedDate, "Calendar shows date in wrong format");
             Assert.assertFalse(isVisible(timeSelectionXpathMinusDiv), "Time part has to be invisible");
         } catch (AssertionError ae) {
             Assert.fail(ae.getMessage());
         } finally {
             setValueById(datePatternId, originalPattern);
             setup();
         }
     }
 
     @Test
     public void testShowHeaderAttribute(Template template) {
         renderPage(SHOW_ATTRIBURES_TEST_URL, template, INIT_SHOW_ATTRIBUTES_TEST);
         initIds(getParentId());
         writeStatus("Check 'showHeader' attribute");
 
         writeStatus("Non-popup mode");
         check(showPopupId, false);
 
         AssertPresent(calendarHeaderId, "Header is not present");
         check(showHeaderId, false);
         AssertNotPresent(calendarHeaderId, "Header is present");
 
         writeStatus("Popup mode");
         check(showPopupId, true);
 
         showPopup();
 
         AssertNotPresent(calendarHeaderId, "Header is present");
         check(showHeaderId, true);
         AssertPresent(calendarHeaderId, "Header is not present");
     }
 
     @Test
     public void testShowFooterAttribute(Template template) {
         renderPage(SHOW_ATTRIBURES_TEST_URL, template, INIT_SHOW_ATTRIBUTES_TEST);
         initIds(getParentId());
         writeStatus("Check 'showFooter' attribute");
 
         writeStatus("Non-popup mode");
         check(showPopupId, false);
 
         AssertPresent(calendarFooterId, "Footer is not present");
         check(showFooterId, false);
         AssertNotPresent(calendarFooterId, "Footer is present");
 
         writeStatus("Popup mode");
         check(showPopupId, true);
 
         showPopup();
 
         AssertNotPresent(calendarFooterId, "Footer is present");
         check(showFooterId, true);
         AssertPresent(calendarFooterId, "Footer is not present");
     }
 
     @Test
     public void testShowWeekDaysBar(Template template) {
         renderPage(SHOW_ATTRIBURES_TEST_URL, template, INIT_SHOW_ATTRIBUTES_TEST);
         initIds(getParentId());
         writeStatus("Check 'showWeekDaysBar' attribute");
 
         writeStatus("Non-popup mode");
         check(showPopupId, false);
 
         AssertPresent(weekDaysBarId, "Week days bar is not present");
         check(showWeekDaysBarId, false);
         AssertNotPresent(weekDaysBarId, "Week days bar is present");
 
         writeStatus("Popup mode");
         check(showPopupId, true);
 
         showPopup();
 
         AssertNotPresent(weekDaysBarId, "Week days bar is present");
         check(showWeekDaysBarId, true);
         AssertPresent(weekDaysBarId, "Week days bar is not present");
     }
 
     @Test
     public void testShowWeeksBar(Template template) {
         renderPage(SHOW_ATTRIBURES_TEST_URL, template, INIT_SHOW_ATTRIBUTES_TEST);
         initIds(getParentId());
         String weeksBarId = calendarId + "WeekNumCell1";
         writeStatus("Check 'showWeeksBar' attribute");
 
         writeStatus("Non-popup mode");
         check(showPopupId, false);
 
         AssertPresent(weeksBarId, "Weeks bar is not present");
         check(showWeeksBarId, false);
         AssertNotPresent(weeksBarId, "Weeks bar is present");
 
         writeStatus("Popup mode");
         check(showPopupId, true);
 
         showPopup();
 
         AssertNotPresent(weeksBarId, "Weeks bar is present");
         check(showWeeksBarId, true);
         AssertPresent(weeksBarId, "Weeks bar is not present");
     }
 
     @Test
     public void testShowInput(Template template) {
         renderPage(SHOW_ATTRIBURES_TEST_URL, template, INIT_SHOW_ATTRIBUTES_TEST);
         initIds(getParentId());
         writeStatus("Check 'showInput' attribute");
 
         writeStatus("This attribute makes sense in popup mode only");
         check(showPopupId, true);
 
         AssertVisible(inputDateId, "Input is not visible");
         check(showInputId, false);
         AssertNotVisible(inputDateId, "Input is visible");
     }
 
     @Test
     public void testShowApplyButton(Template template) {
         renderPage(SHOW_ATTRIBURES_TEST_URL, template, INIT_SHOW_ATTRIBUTES_TEST);
         initIds(getParentId());
         String applyButtonXpath = "//td[@id='" + calendarFooterId + "']/table/tbody/tr/td[6]";
         writeStatus("Check 'showApplyButton' attribute");
 
         writeStatus("This attribute makes sense in popup mode only");
         check(showPopupId, true);
 
         showPopup();
 
         Assert.assertTrue(isVisible(applyButtonXpath), "Apply button is not visible");
         check(showApplyButtonId, false);
         Assert.assertFalse(isPresent(applyButtonXpath), "Apply button is visible");
     }
 
     @Test
     public void testFirstWeekDay(Template template) {
         renderPage(SHOW_ATTRIBURES_TEST_URL, template, INIT_SHOW_ATTRIBUTES_TEST);
         initIds(getParentId());
 
         String firstWeekDayCellId = calendarId + "WeekDayCell0";
         Locale locale = new Locale(selenium.getText(getParentId() + FORM_ID + "locale"));
         DateFormatSymbols symbols = new DateFormatSymbols(locale);
         Calendar cal = Calendar.getInstance(locale);
 
         writeStatus("Check 'firstWeekDay' attribute");
         writeStatus("Set first firstWeekDay to 5 day. 'FirstWeekDay' ranges from 0 to 6 for instance it is Friday in USA and Saturday (samedi) in France");
         selenium.type(firstWeekDayId, "5");
 
         int weekday = Integer.parseInt(selenium.getValue(firstWeekDayId));
         String weekDayShortName = symbols.getShortWeekdays()[weekday + cal.getFirstDayOfWeek()];
 
         writeStatus("Non-popup mode");
         check(showPopupId, false);
         AssertTextEquals(firstWeekDayCellId, weekDayShortName, "It looks as if 'firstWeekDay' attribute doesn't work");
 
         writeStatus("Popup mode");
         check(showPopupId, true);
         showPopup();
 
         AssertTextEquals(firstWeekDayCellId, weekDayShortName, "It looks as if 'firstWeekDay' attribute doesn't work");
     }
 
    @Test
     public void testDisabledAttribute(Template template) {
         renderPage(SHOW_ATTRIBURES_TEST_URL, template, INIT_SHOW_ATTRIBUTES_TEST);
         initIds(getParentId());
 
         writeStatus("Check 'disabled' attribute");
         check(isDisabledId, true);
 
         writeStatus("Popup mode");
         check(showPopupId, true);
 
         writeStatus("Check date input is disabled");
         //Assert.assertFalse(selenium.isEditable(inputDateId), "Date input is not disabled");
         //Assert.assertTrue(Boolean.parseBoolean(selenium.getAttribute(inputDateId + "@disabled")), "Date input is not disabled");
         if(selenium.getXpathCount("//*[@id='" + inputDateId + "' and @disabled='true']").intValue() == 0) {
             Assert.fail("Date input is not disabled");
         }
         writeStatus("button does not trigger popup calendar");
         showPopup();
         Assert.assertFalse(isVisible(calendarId), "Calendar popup should not be shown up");
         writeStatus("and decorated with disabled icon");
         testIcon(popupButtonId, "Disabled");
 
         writeStatus("Non-popup mode");
         check(showPopupId, false);
         writeStatus("All stuff is disabled");
         Assert.assertFalse(isPresent("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][1]/div"), "Previous year button has NOT to be present");
         Assert.assertFalse(isPresent("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][2]/div"), "Previous month button has NOT to be present");
         Assert.assertFalse(isPresent("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][3]/div"), "Next month button has NOT to be present");
         Assert.assertFalse(isPresent("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][4]/div"), "Next year button has NOT to be present");
 
         String footerText = selenium.getText("//td[@id='" + calendarFooterId + "']/table/tbody/tr");
         Assert.assertFalse(footerText.matches(".*Today.*"), "Today button should NOT be present");
         Assert.assertFalse(footerText.matches(".*Clean.*"), "Clean button should NOT be present");
 
         String expectedHTML = getHTMLById(calendarId);
         writeStatus("Convulsively click all over the calendar. The component has not to be changed it is disabled, remember?!");
         clickById(calendarId + "DayCell6");
         String actualHTML = getHTMLById(calendarId);
         Assert.assertEquals(actualHTML, expectedHTML);
         clickById(calendarId + "DayCell17");
         actualHTML = getHTMLById(calendarId);
         Assert.assertEquals(actualHTML, expectedHTML);
 
         clickById(calendarId + "DayCell26");
         actualHTML = getHTMLById(calendarId);
         Assert.assertEquals(actualHTML, expectedHTML);
     }
 
     @Test
     public void testEnableManualInputAttribute(Template template) {
         renderPage(SHOW_ATTRIBURES_TEST_URL, template, INIT_SHOW_ATTRIBUTES_TEST);
         initIds(getParentId());
 
         writeStatus("Check 'enableManualInput' attribute");
         check(showPopupId, true);
 
         writeStatus("Set 'enableManualInput' to false. Date input has to be read only. Check it");
         check(enableManualInputId, false);
         Assert.assertFalse(selenium.isEditable(inputDateId), "Date input has to be read only");
 
         writeStatus("Set 'enableManualInput' to true. Date input has to become editable. Check it");
         check(enableManualInputId, true);
         Assert.assertTrue(selenium.isEditable(inputDateId), "Date input has to be editable");
     }
 
     @Test
     public void testTodayControlMode(Template template) {
         renderPage(TODAY_CONTROL_AND_BOUNDARY_DATES_MODES_TEST_URL, template, RESET_METHOD2);
         initIds(getParentId());
         
         String expectedPattern = "MM/dd/yyyy";
         setValueById(getParentId() + FORM_ID + "datePattern", expectedPattern);
         clickCommandAndWait(getParentId() + FORM_ID + "setup");
         
         Locale locale = new Locale(selenium.getText(getParentId() + FORM_ID + "locale"));
         Date presentTime = Calendar.getInstance(locale).getTime();
         String month = DateUtils.month(presentTime, locale);
         int year = DateUtils.year(presentTime);
         String expectedCurrentMonthYearHeader = month + ", " + year;
         String expectedTodaySelectedDate = new SimpleDateFormat(expectedPattern).format(new Date());
 
         writeStatus("Check 'todayControlMode' attribute");
 
         //select mode
         writeStatus("Check 'select' mode (default). Control 'Today' should scroll calendar to current date and set selected date to today");
         selenium.select(todayControlModeId, "select");
         writeStatus("Non-popup mode");
         check(showPopupId, false);
 
         selenium.click(todayControlXpath + "/div");
         String actualSelectedDate = selenium.getValue(inputDateId);
         String actualCurrentDate = selenium.getText(currentDateHeaderXpath);
         
        
         
         writeStatus("Check selected date and current date set to now");
         //TODO: time ain't set to current time ... is it true? find out!
         Assert.assertEquals(actualSelectedDate, expectedTodaySelectedDate);
         Assert.assertEquals(actualCurrentDate, expectedCurrentMonthYearHeader);
 
         writeStatus("Popup mode");
         check(showPopupId, true);
         showPopup();
         writeStatus("Check calendar popup is shown up");
         Assert.assertTrue(isVisible(calendarId), "Calendar popup is not visible");
 
         selenium.click(todayControlXpath + "/div");
         Assert.assertEquals(actualSelectedDate, expectedTodaySelectedDate);
         Assert.assertEquals(actualCurrentDate, expectedCurrentMonthYearHeader);
 
         //scroll mode
         writeStatus("Check 'scroll' mode. Control 'Today' should just scroll calendar to current date not changing selected date");
         selenium.select(todayControlModeId, "scroll");
         writeStatus("Non-popup mode");
         check(showPopupId, false);
 
         String previuosSelectedDate = selenium.getValue(inputDateId);
         selenium.click(todayControlXpath + "/div");
         actualSelectedDate = selenium.getValue(inputDateId);
         actualCurrentDate = selenium.getText(currentDateHeaderXpath);
 
         writeStatus("Check selected date is not affected, current date set to now (month, year)");
         Assert.assertEquals(actualSelectedDate, previuosSelectedDate);
         Assert.assertEquals(actualCurrentDate, expectedCurrentMonthYearHeader);
 
         writeStatus("Popup mode");
         check(showPopupId, true);
         showPopup();
         writeStatus("Check calendar popup is shown up");
         Assert.assertTrue(isVisible(calendarId), "Calendar popup is not visible");
 
         previuosSelectedDate = selenium.getValue(inputDateId);
         selenium.click(todayControlXpath + "/div");
         actualSelectedDate = selenium.getValue(inputDateId);
         actualCurrentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(actualSelectedDate, previuosSelectedDate);
         Assert.assertEquals(actualCurrentDate, expectedCurrentMonthYearHeader);
 
         //hidden mode
         writeStatus("Check 'hidden' mode. Control 'Today' has to be hidden");
         selenium.select(todayControlModeId, "hidden");
         writeStatus("Non-popup mode");
         check(showPopupId, false);
         Assert.assertFalse(isVisible(todayControlXpath), "Control 'Today' has to be hidden");
 
         writeStatus("Popup mode");
         check(showPopupId, true);
         showPopup();
         writeStatus("Check calendar popup is shown up");
         Assert.assertTrue(isVisible(calendarId), "Calendar popup is not visible");
         Assert.assertFalse(isVisible(todayControlXpath), "Control 'Today' has to be hidden");
     }
 
     @Test
     public void testBoundaryDatesMode(Template template) {
         renderPage(TODAY_CONTROL_AND_BOUNDARY_DATES_MODES_TEST_URL, template, RESET_METHOD2);
         initIds(getParentId());
         showPopup();
         
         String selectedDateId = getParentId() + FORM_ID + "selectedDate";
         String currentDateId = getParentId() + FORM_ID + "currentDate";
         String startDate = "02/21/2007 12:00";
         String firstMarCellId = calendarId + "DayCell32";
         String thirtyFirstJanCellId = calendarId + "DayCell3";
         selenium.focus(currentDateId);
         setValueById(currentDateId, startDate);
         selenium.fireEvent(currentDateId, "blur");
         selenium.focus(selectedDateId);
         setValueById(selectedDateId, startDate);
         selenium.fireEvent(selectedDateId, "blur");
 
         writeStatus("Check 'boundaryDatesMode' attribute");
 
         writeStatus("Check 'inactive' (default) mode");
         selenium.select(boundaryDatesModeId, "inactive");
         showPopup();
 
         String expectedSelectedDate = selenium.getValue(inputDateId);
         String expectedCurrentDate = selenium.getText(currentDateHeaderXpath);
 
         writeStatus("Click boundary cells. Nothing has to be changed");
         clickById(firstMarCellId);
         String actualSelectedDate = selenium.getValue(inputDateId);
         String actualCurrentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(actualCurrentDate, expectedCurrentDate, "Nothing has to be changed");
         Assert.assertEquals(actualSelectedDate, expectedSelectedDate, "Nothing has to be changed");
 
         clickById(thirtyFirstJanCellId);
         actualSelectedDate = selenium.getValue(inputDateId);
         actualCurrentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(actualCurrentDate, expectedCurrentDate, "Nothing has to be changed");
         Assert.assertEquals(actualSelectedDate, expectedSelectedDate, "Nothing has to be changed");
 
         writeStatus("Check 'scroll' mode");
         selenium.select(boundaryDatesModeId, "scroll");
         showPopup();
         writeStatus("Click a day from next month, the month has to be scrolled to the next month, but selected date stays untouched");
         clickById(firstMarCellId);
         actualSelectedDate = selenium.getValue(inputDateId);
         actualCurrentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(actualCurrentDate, "March, 2007", "Month has to be switched to the next one");
         Assert.assertEquals(actualSelectedDate, expectedSelectedDate, "Selected date has not to be changed");
         writeStatus("Move back");
         selenium.click("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][2]/div");
         writeStatus("Click a day from previous month, the month has to be scrolled to the previous month, selected date stays untouched as before");
         clickById(thirtyFirstJanCellId);
         actualSelectedDate = selenium.getValue(inputDateId);
         actualCurrentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(actualCurrentDate, "January, 2007", "Month has to be switched to the previous one");
         Assert.assertEquals(actualSelectedDate, expectedSelectedDate, "Selected date has not to be changed");
         writeStatus("Move back");
         selenium.click("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][3]/div");
 
         writeStatus("Check 'select' mode");
         selenium.select(boundaryDatesModeId, "select");
         showPopup();
         writeStatus("Click a day from next month: 1) the month has to be scrolled to the next month 2) date set to selected date (1st of March)");
         clickById(firstMarCellId);
         actualSelectedDate = selenium.getValue(inputDateId);
         actualCurrentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(actualCurrentDate, "March, 2007", "Month has to be switched to the next one");
         Assert.assertEquals(actualSelectedDate, "03/01/2007 12:00", "Selected date has to be changed");
         writeStatus("Move back");
         selenium.click("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][2]/div");
         writeStatus("Click a day from previous month: 1) the month has to be scrolled to the previous month 2) date set to selected date (31st of January)");
         clickById(thirtyFirstJanCellId);
         actualSelectedDate = selenium.getValue(inputDateId);
         actualCurrentDate = selenium.getText(currentDateHeaderXpath);
         Assert.assertEquals(actualCurrentDate, "January, 2007", "Month has to be switched to the previous one");
         Assert.assertEquals(actualSelectedDate, "01/31/2007 12:00", "Selected date has to be changed");
         writeStatus("Move back");
         selenium.click("//td[@id='" + calendarHeaderId + "']/table/tbody/tr/td[@class='rich-calendar-tool'][3]/div");
     }
 
     @Test
     public void testButtonRelatedAttributes(Template template) {
         renderPage(BUTTON_RELATED_TEST_URL, template, null);
         writeStatus("Check button-related attributes");
 
         String calendar = getParentId() + FORM_ID + "calendar";
         String enabledIconPopupBtnId = calendar + "EnabledIconPopupButton";
         String disabledIconPopupBtnId = calendar + "DisabledIconPopupButton";
         String labeledPopupBtnId = calendar + "LabeledPopupButton";
 
         writeStatus("Check enabled popup button are rendered with a proper icon");
         testIcon(enabledIconPopupBtnId, "icon_enabled");
         isRenderedAs(enabledIconPopupBtnId, "img");
 
         writeStatus("Check disabled popup button are rendered with a proper icon");
         testIcon(disabledIconPopupBtnId, "icon_disabled");
         isRenderedAs(disabledIconPopupBtnId, "img");
 
         writeStatus("Check popup button is rendered as a labeled button element");
         isRenderedAs(labeledPopupBtnId, "button");
         AssertTextEquals(labeledPopupBtnId, "Button");
     }
 
     @Test
     public void testRequiredAndRequiredMessageAttributes(Template template) {
         renderPage(template, INIT_REQUIRED_TEST);
         initIds(getParentId());
 
         String validDate = new SimpleDateFormat(selenium.getValue(datePatternId)).format(new Date());
 
         writeStatus("Check required & requiredMessage attributes");
 
         writeStatus("Initially calendar message has to be empty. Check it");
         AssertTextEquals(calendarMessageId, "", "there is already not empty message here! This is no good!");
 
         writeStatus("Check the attributes themselves. In any case 'Date cannot be empty' error message has to come up. Track it");
         writeStatus("Check non-popup mode");
 
         clickAjaxCommandAndWait(ajaxSubmitId);
         AssertTextEquals(calendarMessageId, REQUIRED_MESSAGE, "Wrong message is shown up");
         ajaxSetup();
         clickCommandAndWait(serverSubmitId);
         AssertTextEquals(calendarMessageId, REQUIRED_MESSAGE, "Wrong message is shown up");
 
         writeStatus("Check popup mode");
         setPopup(true);
         ajaxSetup();
 
         clickAjaxCommandAndWait(ajaxSubmitId);
         AssertTextEquals(calendarMessageId, REQUIRED_MESSAGE, "Wrong message is shown up");
         ajaxSetup();
         clickCommandAndWait(serverSubmitId);
         AssertTextEquals(calendarMessageId, REQUIRED_MESSAGE, "Wrong message is shown up");
 
         writeStatus("Fix date. Error message has to escape");
         setValueById(selectedDateId, validDate);
         ajaxSetup();
 
         clickAjaxCommandAndWait(ajaxSubmitId);
         AssertTextEquals(calendarMessageId, "", "Message has to be empty");
         ajaxSetup();
 
         clickCommandAndWait(serverSubmitId);
         AssertTextEquals(calendarMessageId, "", "Message has to be empty");
         ajaxSetup();
     }
 
     @Test
     public void testLabelAttribute(Template template) {
         renderPage(template, RESET_METHOD);
         initIds(getParentId());
 
         String label = "Calendar";
         writeStatus("Check 'label' attribute");
 
         writeStatus("Set calendar input to something low-recalling date. Error message shown up has to be peppered with given label (Calendar)");
         setValueById(inputDateId, "imnotdatetrustme");
         clickAjaxCommandAndWait(ajaxSubmitId);
         String msg = selenium.getText(calendarMessageId);
         Assert.assertTrue(msg.matches(".*" + label + ".*"), "Error message does not contain defined label: Calendar");
     }
 
     @Test
     public void testInputSizeAttribute(Template template) {
         renderPage(LAYOUT_TESTS_URL, template, null);
 
         inputDateId = getParentId() + "form:calendarInputDate";
         showPopupId = getParentId() + "form:showPopup";
 
         writeStatus("Check 'inputSize' attribute");
         check(showPopupId, true);
 
         String size = selenium.getAttribute(inputDateId + "@size");
         Assert.assertEquals(size, "15", "Input size attribute is not applied");
     }
 
     @Test
     public void testCellWidthAndCellHeightAttributes(Template template) {
         renderPage(LAYOUT_TESTS_URL, template, null);
 
         //In IE 6 the class selector doesn’t work if the class name starts with an underscore or a hyphen
         //I was reluctant to rename form name
         calendarId = getParentId() + "form:calendar";
         showPopupId = getParentId() + "form:showPopup";
         popupButtonId = calendarId + "PopupButton";
         String dayCell5 = calendarId + "DayCell5";
 
         int expectedCellWidth = 31;
         int expectedCellHeight = 33;
         int precision = 1;
 
         writeStatus("Check 'cellWidth' and 'cellHeight' attributes.");
 
         writeStatus("Non-popup mode");
         check(showPopupId, false);
 
         writeStatus("Check size of arbitrary cell. Width must be 31px, height must be 33px");
 
         int height = selenium.getElementHeight(dayCell5).intValue();
         int width = selenium.getElementWidth(dayCell5).intValue();
 
         Assert.assertTrue((Math.abs(height - expectedCellHeight) <= precision), height + "==" + expectedCellHeight);
         Assert.assertTrue((Math.abs(width - expectedCellWidth) <= precision), width + "==" + expectedCellWidth);
 
         writeStatus("Check the same for popup mode");
         check(showPopupId, true);
         showPopup();
 
         height = selenium.getElementHeight(dayCell5).intValue();
         width = selenium.getElementWidth(dayCell5).intValue();
 
         Assert.assertTrue((Math.abs(height - expectedCellHeight) <= precision), height + "==" + expectedCellHeight);
         Assert.assertTrue((Math.abs(width - expectedCellWidth) <= precision), width + "==" + expectedCellWidth);
     }
 
     @Test
     public void testFacets(Template template) {
         renderPage(FACETS_TEST_URL, template, null);
         initIds(getParentId());
         writeStatus("Check facets of the component: 'header', 'footer', 'optionalHeader', 'optionalFooter', 'weekNumber' and 'weekDay'");
 
         AssertTextEquals(optionalHeaderFacetId, "optionalHeader", "Optional header facet is not rendered to client");
         AssertTextEquals(optionalFooterFacetId, "optionalFooter", "Optional footer facet is not rendered to client");
 
         writeStatus("Check header facet");
         String headerFacetText = selenium.getText(headerFacetId);
         Assert.assertTrue(headerFacetText.matches(".*Header.*"), "Header facet is not rendered to client");
         Assert.assertTrue(headerFacetText.matches(".*Today.*"), "Header facet is not rendered to client");
 
         writeStatus("Check footer facet");
         AssertPresent(footerFacetId, "Footer facet is not rendered");
         AssertTextEquals("//*[@id='" + footerFacetId + "']/table/tbody/tr/td[1]", "Footer");
 
         writeStatus("Check '{previousMonthControl}' element works");
         AssertTextEquals("//*[@id='" + footerFacetId + "']/table/tbody/tr/td[2]", "<");
         String beforeHTML = getHTMLById(calendarId);
         selenium.click("//*[@id='" + footerFacetId + "']/table/tbody/tr/td[2]/div");
         String afterHTML = getHTMLById(calendarId);
         Assert.assertFalse(afterHTML.equals(beforeHTML), "It looks as if previous month control does not work");
         
         writeStatus("Check '{nextMonthControl}' element works");
         AssertTextEquals("//*[@id='" + footerFacetId + "']/table/tbody/tr/td[4]", ">");
         beforeHTML = getHTMLById(calendarId);
         selenium.click("//*[@id='" + footerFacetId + "']/table/tbody/tr/td[4]/div");
         afterHTML = getHTMLById(calendarId);
         Assert.assertFalse(afterHTML.equals(beforeHTML), "It looks as if next month control does not work");
 
         writeStatus("Check 'weekNumber' facet");
         String weekNumberCellText = selenium.getText(calendarId + "WeekNumCell1");
         Assert.assertTrue(weekNumberCellText.matches(".*WN:.*"), "Week number facet is not rendered to client");
         
         writeStatus("Check 'weekDay' facet");
         String weekDayCellText = selenium.getText(calendarId + "WeekDayCell1");
         Assert.assertTrue(weekDayCellText.matches(".*WD:.*"), "Week day facet is not rendered to client");
     }
 
     @Test
     public void testResetTimeOnDateSelect(Template template) {
         renderPage(RESET_TIME_ON_DATE_SELECTION_TEST_URL, template, RESET_METHOD);
         initIds(getParentId());
 
         String expectedDefaultTime = "05/05/2007 13:13";
         String notEmptySelectedDate = "04/04/2006 10:10";
 
         writeStatus("Check 'defaultTime' and 'resetTimeOnDateSelect' attributes");
 
         writeStatus("Check time of selected date replaced by defaultTime in case of 'resetTimeOnDateSelect' attribute being true");
         setValueById(defaultTimeId, expectedDefaultTime);
         setValueById(selectedDateId, notEmptySelectedDate);
         checkWithoutClick(resetTimeOnDateSelectId, true);
         ajaxSetup();
 
         selenium.click(todayControlXpath + "/div");
         String actualSelectedDate = selenium.getValue(inputDateId);
         //current date time with time replaced by default time
         String expectedReplacedTodaySelectedDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date()) + " 13:13";
         Assert.assertEquals(actualSelectedDate, expectedReplacedTodaySelectedDate);
 
         writeStatus("Check time of selected date replaced by defaultTime if value (selected date) is empty");
         setValueById(selectedDateId, "");
         checkWithoutClick(resetTimeOnDateSelectId, false);
         ajaxSetup();
 
         selenium.click(todayControlXpath + "/div");
         actualSelectedDate = selenium.getValue(inputDateId);
         expectedReplacedTodaySelectedDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date()) + " 13:13";
         Assert.assertEquals(actualSelectedDate, expectedReplacedTodaySelectedDate);
 
         writeStatus("if value (selected date) is not empty and resetTimeOnDateSelect' attribute is not true then nothing happens");
         setValueById(selectedDateId, notEmptySelectedDate);
         checkWithoutClick(resetTimeOnDateSelectId, false);
         ajaxSetup();
 
         selenium.click(todayControlXpath + "/div");
         actualSelectedDate = selenium.getValue(inputDateId);
         String expectedTodaySelectedDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date()) + " 10:10";
         //side effect of https://jira.jboss.org/jira/browse/RF-5221
         Assert.assertEquals(actualSelectedDate, expectedTodaySelectedDate);
     }
 
     @Test
     public void testJointPointAndDirectionAttributes(Template template) {
         renderPage(JOINTPOINT_DIRECTION_TEST_URL, template, null);
         initIds(getParentId());
 
         String topLeftPointLeft = "$('" + inputDateId + "').cumulativeOffset().left";
         String topLeftPointTop = "$('" + inputDateId + "').cumulativeOffset().top";
 
         String bottomLeftPointLeft = topLeftPointLeft;
         String bottomLeftPointTop = "($('" + inputDateId + "').cumulativeOffset().top + $('" + inputDateId + "').getHeight())";
 
         String topRightPointLeft = "($('" + popupButtonId + "').cumulativeOffset().left + $('" + popupButtonId + "').getWidth())";
        String topRightPointTop =  "$('" + popupButtonId + "').cumulativeOffset().top";
 
         String bottomRightPointLeft = topRightPointLeft;
        String bottomRightPointTop =  "($('" + popupButtonId + "').cumulativeOffset().top + $('" + popupButtonId + "').getHeight())";
 
         testJointPoint("top-left", topLeftPointLeft, topLeftPointTop);
         testJointPoint("top-right", topRightPointLeft, topRightPointTop);
         testJointPoint("bottom-left", bottomLeftPointLeft, bottomLeftPointTop);
         testJointPoint("bottom-right", bottomRightPointLeft, bottomRightPointTop);
     }
 
     private void testJointPoint(String jointPointValue, String jointPointLeft, String jointPointTop) {
         String calendarLeft = "$('" + calendarId + "').cumulativeOffset().left";
         String calendarTop = "$('" + calendarId + "').cumulativeOffset().top";
 
         writeStatus("Check [" + jointPointValue + "] joint point with all directions");
         selenium.select(jointPointId, jointPointValue);
 
         //top-left direction
         selenium.select(directionId, "top-left");
         showPopup();       
         assertTrue(calendarLeft, "<", jointPointLeft);
         assertTrue(calendarTop, "<", jointPointTop);
 
         //top-right direction
         selenium.select(directionId, "top-right");
         showPopup();
         assertTrue(calendarLeft, "==", jointPointLeft);
         assertTrue(calendarTop, "<", jointPointTop);
 
         //bottom-right direction
         selenium.select(directionId, "bottom-right");
         showPopup();
         assertTrue(calendarLeft, "==", jointPointLeft);
         assertTrue(calendarTop, "==", jointPointTop);
 
         //bottom-left direction
         selenium.select(directionId, "bottom-left");
         showPopup();
         assertTrue(calendarLeft, "<", jointPointLeft);
         assertTrue(calendarTop, "==", jointPointTop);
 
         //waiteForCondition("$('" + popupButtonId + "').getStyle('display') != 'none'", 1000);
     }
 
     private void assertTrue(String calendarPoint, String operation, String jointPoint) {
         calendarPoint = calendarPoint.replaceAll("\\$", WINDOW_JS_RESOLVER + "\\$");
         jointPoint = jointPoint.replaceAll("\\$", WINDOW_JS_RESOLVER + "\\$");
         String expression;
         if ("==".equals(operation)) {
             expression = "Math.abs(" + calendarPoint + " - " + jointPoint + ") <= 2";
         } else {
             expression = calendarPoint + " " + operation + " " + jointPoint;
         }
         Assert.assertTrue(Boolean.parseBoolean(selenium.getEval(expression)), selenium.getEval(calendarPoint) + operation + selenium.getEval(jointPoint));
     }
 
     private void setPopup(boolean isPopup) {
         runScript("$('" + isPopupId + "').checked=" + isPopup);
     }
 
     private void checkWithoutClick(String id, boolean isChecked) {
         runScript("$('" + id + "').checked=" + isChecked);
     }
 
     private boolean check(String checkBoxId, boolean isChecked) {
         boolean prevValue = Boolean.parseBoolean(runScript("$('" + checkBoxId + "').checked"));
         if (prevValue != isChecked) {
             clickAjaxCommandAndWait(checkBoxId);
         }
         return prevValue;
     }
 
     private void setup() {
         clickCommandAndWait(setupActionId);
     }
 
     private void ajaxSetup() {
         clickAjaxCommandAndWait(ajaxSetupActionId);
     }
 
     private void showPopup() {
         writeStatus("Show popup");
         clickById(popupButtonId);
     }
 
 /*    private void showPopupAndWait() {
         writeStatus("Show popup");
         clickById(popupButtonId);
         waiteForCondition("$('" + calendarId + "').style.display != 'none'", 1000);
     }*/
 
     /**
      * Test an icon.
      *
      * @param location location of image representing icon to be tested
      * @param iconName substring that icon uri has to contain
      */
     private void testIcon(String location, String iconSubstring) {
         String iconSrc = selenium.getAttribute(location + "@src");
         if (null == iconSrc || !iconSrc.matches(".*" + iconSubstring + ".*")) {
             Assert.fail("It looks as if the icon is not proper. Uri of icon is being tested must contain [" + iconSubstring + "]");
         }
     }
 
     private void isRenderedAs(String id, String tagName) {
         if (selenium.getXpathCount("//" + tagName + "[@id='" + id + "']").intValue() != 1) {
             Assert.fail("Dom element with id[" + id + "] is not rendered as [" + tagName + "]");
         }
     }
 
     public String getTestUrl() {
         return "pages/calendar/calendarTest.xhtml";
     }
 
     @Override
     public String getAutoTestUrl() {
         return "pages/calendar/calendarAutoTest.xhtml";
     }
 
     @Override
     public void changeValue() {
         changeDate();
     }
 
     @Override
     public void sendAjax() {
        changeCurrentDate(true);
     }
 }
