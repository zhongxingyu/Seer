 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2009, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 
 package org.jboss.richfaces.integrationTest.calendar;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.util.Calendar;
 import java.util.Locale;
 
 import org.jboss.richfaces.integrationTest.AbstractSeleniumRichfacesTestCase;
 import org.jboss.test.selenium.waiting.Condition;
 import org.jboss.test.selenium.waiting.Wait;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Test case that tests the calendar component. It tests the "Usage" tab.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class CalendarTestCase extends AbstractSeleniumRichfacesTestCase {
 
     // components on the page
     private final String LOC_CALENDAR_HEADER = getLoc("CALENDAR_HEADER");
     private final String LOC_DATE_INPUT = getLoc("DATE_INPUT");
     private final String LOC_DATE_BUTTON = getLoc("DATE_BUTTON");
     private final String LOC_INPUTS = getLoc("INPUTS");
     private final String LOC_POPUP_MODE_CHECKBOX = getLoc("POPUP_MODE_CHECKBOX");
     private final String LOC_APPLY_BUTTON_CHECKBOX = getLoc("APPLY_BUTTON_CHECKBOX");
     private final String LOC_LOCALE_SELECT = getLoc("LOCALE_SELECT");
     private final String LOC_DATE_PATTERN_SELECT = getLoc("DATE_PATTERN_SELECT");
 
     // components in the calendar
     private final String LOC_CALENDAR = getLoc("CALENDAR");
     private final String LOC_DOUBLE_LEFT_ARROW = getLoc("DOUBLE_LEFT_ARROW");
     private final String LOC_LEFT_ARROW = getLoc("LEFT_ARROW");
     private final String LOC_MONTH_YEAR_LABEL = getLoc("MONTH_YEAR_LABEL");
     private final String LOC_RIGHT_ARROW = getLoc("RIGHT_ARROW");
     private final String LOC_DOUBLE_RIGHT_ARROW = getLoc("DOUBLE_RIGHT_ARROW");
     private final String LOC_CLOSE_BUTTON = getLoc("CLOSE_BUTTON");
     private final String LOC_CLEAN_BUTTON = getLoc("CLEAN_BUTTON");
     private final String LOC_TODAY_BUTTON = getLoc("TODAY_BUTTON");
     private final String LOC_APPLY_BUTTON = getLoc("APPLY_BUTTON");
     private final String LOC_RANDOM_DAY = getLoc("RANDOM_DAY");
 
     private enum Month {
 
         JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;
 
         public static Month previous(Month month) {
             return month == Month.JANUARY ? Month.DECEMBER : Month.values()[month.ordinal() - 1];
         }
 
         public static Month following(Month month) {
             return month == Month.DECEMBER ? Month.JANUARY : Month.values()[month.ordinal() + 1];
         }
     };
 
     /**
      * Tests basic functionality. It checks that the calendar is not visible at
      * the beginning, then it clicks into input field, and checks the visibility
      * of the calendar. At the end, it clicks into input field again and checks
      * the visibility of component. The component should be invisible at the
      * beginning, then visible, and it should hide on second click.
      */
     @Test
     public void testClickIntoDateInput() {
         assertFalse(isDisplayed(LOC_CALENDAR), "The calendar component should not be visible at the beginning.");
 
         selenium.click(LOC_DATE_INPUT);
         assertTrue(isDisplayed(LOC_CALENDAR), "The calendar component should be visible after clicking to date input.");
 
         selenium.click(LOC_DATE_INPUT);
         assertFalse(isDisplayed(LOC_CALENDAR),
                 "The calendar component should not be visible after second click to date input.");
     }
 
     /**
      * Tests basic functionality. It checks that the calendar is not visible at
      * the beginning, then it clicks on button next to date input field, and
      * checks the visibility of the calendar. At the end, it clicks on the
      * button again and checks the visibility of component. The component should
      * be invisible at the beginning, then visible, and it should hide on second
      * click.
      */
     @Test
     public void testClickOnDateButton() {
         assertFalse(isDisplayed(LOC_CALENDAR), "The calendar component should not be visible at the beginning.");
 
         selenium.click(LOC_DATE_BUTTON);
         assertTrue(isDisplayed(LOC_CALENDAR),
                 "The calendar component should be visible after clicking on calendar button.");
 
         selenium.click(LOC_DATE_BUTTON);
         assertFalse(isDisplayed(LOC_CALENDAR),
                 "The calendar component should not be visible after second click on calendar button.");
     }
 
     /**
      * Tests navigation in calendar. At first it click left double arrow to
      * change year and checks that the year changed (e.g. 2009 -> 2008). Then it
      * clicks the simple left arrow to change month and verifies that the month
      * changed (e.g. May -> April). In the next step, it click the "Today"
      * button. Then it performs the same steps for right arrow and double right
      * arrow. In the end it clicks on the "Close" button and verify that the
      * calendar is not visible.
      */
     @Test
     public void testNavigation() {
         selenium.click(LOC_DATE_BUTTON);
 
         // LOC_MONTH_YEAR_LABEL contains e.g. "August, 2009"
         Month month = Month.valueOf(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[0].trim().toUpperCase());
         int year = Integer.parseInt(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[1].trim());
 
         selenium.click(LOC_DOUBLE_LEFT_ARROW);
         int newYear = Integer.parseInt(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[1].trim());
         assertEquals(newYear, year - 1,
                 "Calendar should show the previous year after clicking on the left double arrow.");
 
         selenium.click(LOC_LEFT_ARROW);
         Month newMonth = Month.valueOf(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[0].trim().toUpperCase());
         assertEquals(newMonth, Month.previous(month),
                 "Calendar should show the previous month after clicking on the left arrow.");
 
         selenium.click(LOC_TODAY_BUTTON);
         newMonth = Month.valueOf(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[0].trim().toUpperCase());
         assertEquals(newMonth, month, "Calendar should show today's month after clicking on \"today\".");
         newYear = Integer.parseInt(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[1].trim());
         assertEquals(newYear, year, "Calendar should show today's month after clicking on \"today\".");
 
         // TODO check also the day
 
         selenium.click(LOC_DOUBLE_RIGHT_ARROW);
         newYear = Integer.parseInt(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[1].trim());
         assertEquals(newYear, year + 1,
                 "Calendar should show the following year after clicking on the right double arrow.");
 
         selenium.click(LOC_RIGHT_ARROW);
         newMonth = Month.valueOf(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[0].trim().toUpperCase());
         assertEquals(newMonth, Month.following(month),
                 "Calendar should show the following month after clicking on the right arrow.");
 
         selenium.click(LOC_CLOSE_BUTTON);
         assertFalse(isDisplayed(LOC_CALENDAR),
                 "The calendar component should not be visible after second click on calendar button.");
     }
 
     /**
      * Tests choosing a date in calendar and cleaning selection. At first, it
      * selects a day on 4th line and 2nd column, clicks "Apply" and verifies
      * that input field changed. Then it clicks "Clean" followed by "Apply" and
      * checks that the input field is empty.
      */
     @Test
     public void testChooseDateAndClean() {
         selenium.click(LOC_DATE_BUTTON);
         Month month = Month.valueOf(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[0].trim().toUpperCase());
         int year = Integer.parseInt(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[1].trim());
 
         String date = selenium.getText(LOC_RANDOM_DAY);
         assertFalse(belongsClass("rich-calendar-select", LOC_RANDOM_DAY), format(
                 "The date {0} should not be selected.", date));
 
         selenium.click(LOC_RANDOM_DAY);
         selenium.click(LOC_APPLY_BUTTON);
 
         date = selenium.getText(LOC_RANDOM_DAY);
         assertTrue(belongsClass("rich-calendar-select", LOC_RANDOM_DAY), format("The date {0} should be selected.",
                 date));
 
         selenium.click(LOC_APPLY_BUTTON);
         String dateTime = selenium.getValue(LOC_DATE_INPUT);
         StringBuilder expected = new StringBuilder(date);
         expected.append("/");
         expected.append(month.ordinal() + 1);
         expected.append("/");
         expected.append(String.format("%02d", year % 100));
         expected.append(" 12:00");
         assertEquals(dateTime, expected.toString(), "Date and time should appear in input field.");
 
         selenium.click(LOC_DATE_BUTTON);
         selenium.click(LOC_CLEAN_BUTTON);
         selenium.click(LOC_APPLY_BUTTON);
         dateTime = selenium.getText(LOC_DATE_INPUT);
         assertEquals(dateTime, "", "Date input field should be empty.");
     }
 
     /**
      * Tests the "Popup Mode" checkbox. It unchecks the checkbox and waits for
      * calendar to change. Then it checks that both calendar's input field and
      * button are invisible.
      */
     @Test
     public void testPopupModeCheckbox() {
         String attr = selenium.getAttribute(LOC_CALENDAR + "@style");
         selenium.click(LOC_POPUP_MODE_CHECKBOX);
         waitForAttributeChanges(LOC_CALENDAR + "@style", attr);
 
         assertFalse(isDisplayed(LOC_DATE_INPUT), "Date input field should be hidden.");
         assertFalse(isDisplayed(LOC_INPUTS), "Calendar's button should be hidden.");
     }
 
     /**
      * Tests the "Apply Button" checkbox. It selects a day from the calendar,
      * checks that cell's class attribute has changed to "rich-calendar-select",
      * and checks that the date and time appeared in the input field
      * immediately.
      */
     @Test
     public void testApplyButtonCheckbox() {
         selenium.click(LOC_APPLY_BUTTON_CHECKBOX);
         waitFor(1000);
         selenium.click(LOC_DATE_INPUT);
 
         // LOC_MONTH_YEAR_LABEL contains e.g. "August, 2009"
         Month month = Month.valueOf(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[0].trim().toUpperCase());
         int year = Integer.parseInt(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[1].trim());
 
         String date = selenium.getText(LOC_RANDOM_DAY);
         selenium.click(LOC_RANDOM_DAY);
         Wait.timeout(15000).failWith(format("The date {0} should be selected.", date)).until(new Condition() {
             public boolean isTrue() {
                 return belongsClass("rich-calendar-select", LOC_RANDOM_DAY);
             }
         });
 
         String dateTime = selenium.getValue(LOC_DATE_INPUT);
         StringBuilder expected = new StringBuilder(date);
         expected.append("/");
         expected.append(month.ordinal() + 1);
         expected.append("/");
         expected.append(String.format("%02d", year % 100));
         expected.append(" 12:00");
         assertEquals(dateTime, expected.toString(), "Date and time should appear in input field.");
     }
 
     /**
      * Tests localization of calendar. It selects German locale from the select
      * on the right, checks the name of the month, and the name of the second
      * day in week. In the end it changes the locale back to US English.
      */
     @Test
     public void testLocale() {
         // choose German localization
         selenium.click(LOC_LOCALE_SELECT + "/td[2]/input");
 
         waitFor(1000);
         selenium.click(LOC_DATE_BUTTON);
         selenium.click(LOC_RANDOM_DAY);
 
        String month1 = Month.values()[Calendar.getInstance(new Locale("de")).get(Calendar.MONTH)].toString();
         String month2 = selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[0].trim();
 
         assertTrue(month1.equalsIgnoreCase(month2), "The name of the month is not localized.");
 
         String text = selenium.getText(LOC_CALENDAR + "/tbody/tr[2]/td[3]");
         assertTrue(!"Mon".equalsIgnoreCase(text), "The name of the second day is not localized.");
 
         // TODO find out whether the following strings should be translated
         // text = selenium.getText(cleanButton);
         // assertTrue(!"Clean".equalsIgnoreCase(text),
         // "The \"Clean\" button is not localized.");
         //		
         // text = selenium.getText(todayButton);
         // assertTrue(!"Today".equalsIgnoreCase(text),
         // "The \"Today\" button is not localized.");
         //		
         // text = selenium.getText(applyButton);
         // assertTrue(!"Apply".equalsIgnoreCase(text),
         // "The \"Apply\" button is not localized.");
         //		
         // selenium.click(timeInput);
         //		
         // text = selenium.getText(okButton);
         // assertTrue(!"OK".equalsIgnoreCase(text),
         // "The \"OK\" button is not localized.");
         //		
         // text = selenium.getText(cancelButton);
         // assertTrue(!"Cancel".equalsIgnoreCase(text),
         // "The \"Cancel\" button is not localized.");
     }
 
     /**
      * Tests date patterns. It selects the second pattern (dd/M/yy hh:mm a),
      * clicks a day in calendar, and clicks "Apply". Then it verifies that the
      * date and time in the input field match the selected pattern.
      */
     @Test
     public void testDataPatternSelect() {
         selenium.select(LOC_DATE_PATTERN_SELECT, "index=1");
 
         waitFor(1000);
         selenium.click(LOC_DATE_BUTTON);
         selenium.click(LOC_RANDOM_DAY);
         selenium.click(LOC_APPLY_BUTTON);
 
         // LOC_MONTH_YEAR_LABEL contains e.g. "August, 2009"
         Month month = Month.valueOf(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[0].trim().toUpperCase());
         int year = Integer.parseInt(selenium.getText(LOC_MONTH_YEAR_LABEL).split(",")[1].trim());
 
         String date = selenium.getText(LOC_RANDOM_DAY);
 
         String dateTime = selenium.getValue(LOC_DATE_INPUT);
         StringBuilder expected = new StringBuilder(date);
         expected.append("/");
         expected.append(month.ordinal() + 1);
         expected.append("/");
         expected.append(String.format("%02d", year % 100));
         expected.append(" 12:00 PM");
         assertEquals(dateTime, expected.toString(), "Date and time does not have right format.");
     }
 
     /**
      * Tests the "View Source". It checks that the source code is not visible,
      * clicks on the link, and checks 8 lines of source code.
      */
     @Test
     public void testPageSource() {
         String[] strings = new String[] {
                 "<ui:composition xmlns=\"http://www.w3.org/1999/xhtml\"",
                 ".rich-calendar-tool-btn{",
                 "<a4j:outputPanel id=\"calendar\" layout=\"block\">",
                 "<rich:calendar value=\"#{calendarBean.selectedDate}\"",
                 "locale=\"#{calendarBean.locale}\"",
                 "popup=\"#{calendarBean.popup}\"",
                 "datePattern=\"#{calendarBean.pattern}\"",
                 "showApplyButton=\"#{calendarBean.showApply}\" cellWidth=\"24px\" cellHeight=\"22px\" style=\"width:200px\"/>", };
 
         abstractTestSource(1, "View Source", strings);
     }
 
     /**
      * Loads the page containing the calendar component.
      */
     @SuppressWarnings("unused")
     @BeforeMethod
     private void loadPage() {
         openComponent("Calendar");
         scrollIntoView(LOC_CALENDAR_HEADER, true);
     }
 
 }
