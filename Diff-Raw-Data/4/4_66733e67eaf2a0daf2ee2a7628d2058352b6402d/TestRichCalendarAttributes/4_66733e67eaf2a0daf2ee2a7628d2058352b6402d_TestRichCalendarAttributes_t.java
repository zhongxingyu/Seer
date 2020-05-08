 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
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
 package org.richfaces.tests.metamer.ftest.richCalendar;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.waiting.EventFiredCondition;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for attributes of a calendar on page faces/components/richCalendar/simple.xhtml.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichCalendarAttributes extends AbstractCalendarTest {
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richCalendar/simple.xhtml");
     }
 
     @Test
     public void testBoundaryDatesModeNull() {
         selenium.click(input);
 
         String month = selenium.getText(monthLabel);
         guardNoRequest(selenium).click(cellWeekDay.format(6, 6));
         String newMonth = selenium.getText(monthLabel);
         assertEquals(newMonth, month, "Month should not change.");
 
         // the most top-left column might be 1st day of month
        while (selenium.getText(cellWeekDay.format(1, 1)).equals("1")) {
             selenium.click(prevMonthButton);
         }
 
        month = selenium.getText(monthLabel);
         guardNoRequest(selenium).click(cellWeekDay.format(1, 1));
         newMonth = selenium.getText(monthLabel);
         assertEquals(newMonth, month, "Month should not change.");
     }
 
     @Test
     public void testBoundaryDatesModeInactive() {
         selenium.click(pjq("input[name$=boundaryDatesModeInput][value=inactive]"));
         selenium.waitForPageToLoad();
 
         testBoundaryDatesModeNull();
     }
 
     @Test
     public void testBoundaryDatesModeScroll() {
         selenium.click(pjq("input[name$=boundaryDatesModeInput][value=scroll]"));
         selenium.waitForPageToLoad();
         selenium.click(input);
 
         String thisMonth = selenium.getText(monthLabel);
         // November, 2010 -> November
         thisMonth = thisMonth.substring(0, thisMonth.indexOf(","));
         guardNoRequest(selenium).click(cellWeekDay.format(6, 6));
         String newMonth = selenium.getText(monthLabel);
         newMonth = newMonth.substring(0, newMonth.indexOf(","));
         assertEquals(Month.valueOf(newMonth), Month.valueOf(thisMonth).next(), "Month did not change correctly.");
 
         assertNoDateSelected();
 
         // the most top-left column might be 1st day of month
         while (selenium.getText(cellWeekDay.format(1, 0)).equals("1")) {
             selenium.click(prevMonthButton);
         }
 
         thisMonth = selenium.getText(monthLabel);
         // November, 2010 -> November
         thisMonth = thisMonth.substring(0, thisMonth.indexOf(","));
         guardNoRequest(selenium).click(cellWeekDay.format(1, 1));
         newMonth = selenium.getText(monthLabel);
         newMonth = newMonth.substring(0, newMonth.indexOf(","));
 
         assertEquals(Month.valueOf(newMonth), Month.valueOf(thisMonth).previous(), "Month did not change correctly.");
 
         assertNoDateSelected();
     }
 
     @Test
     public void testBoundaryDatesModeSelect() {
         selenium.click(pjq("input[name$=boundaryDatesModeInput][value=select]"));
         selenium.waitForPageToLoad();
         selenium.click(input);
 
         String thisMonth = selenium.getText(monthLabel);
         String selectedDate = selenium.getText(cellWeekDay.format(6, 6));
         // November, 2010 -> November
         thisMonth = thisMonth.substring(0, thisMonth.indexOf(","));
         guardNoRequest(selenium).click(cellWeekDay.format(6, 6));
         String newMonth = selenium.getText(monthLabel);
         newMonth = newMonth.substring(0, newMonth.indexOf(","));
         assertEquals(Month.valueOf(newMonth), Month.valueOf(thisMonth).next(), "Month did not change correctly.");
 
         assertSelected(selectedDate);
 
         // the most top-left column might be 1st day of month
         while (selenium.getText(cellWeekDay.format(1, 0)).equals("1")) {
             selenium.click(prevMonthButton);
         }
 
         thisMonth = selenium.getText(monthLabel);
         selectedDate = selenium.getText(cellWeekDay.format(1, 1));
         // November, 2010 -> November
         thisMonth = thisMonth.substring(0, thisMonth.indexOf(","));
         guardNoRequest(selenium).click(cellWeekDay.format(1, 1));
         newMonth = selenium.getText(monthLabel);
         newMonth = newMonth.substring(0, newMonth.indexOf(","));
 
         assertEquals(Month.valueOf(newMonth), Month.valueOf(thisMonth).previous(), "Month did not change correctly.");
 
         assertSelected(selectedDate);
     }
 
     @Test
     public void testButtonClass() {
         testStyleClass(image, "buttonClass");
     }
 
     @Test
     public void testButtonClassLabel() {
         selenium.type(pjq("input[type=text][id$=buttonLabelInput]"), "label");
         selenium.waitForPageToLoad();
 
         testStyleClass(button, "buttonClass");
     }
 
     @Test
     public void testButtonClassIcon() {
         selenium.click(pjq("td:has(label:contains(heart)) > input[name$=buttonIconInput]"));
         selenium.waitForPageToLoad();
 
         testStyleClass(image, "buttonClass");
     }
 
     @Test
     public void testButtonIcon() {
         selenium.click(pjq("td:has(label:contains(star)) > input[name$=buttonIconInput]"));
         selenium.waitForPageToLoad();
 
         AttributeLocator attr = image.getAttribute(Attribute.SRC);
         String src = selenium.getAttribute(attr);
         assertTrue(src.contains("star.png"), "Calendar's icon was not updated.");
 
         selenium.click(pjq("td:has(label:contains(null)) > input[name$=buttonIconInput]"));
         selenium.waitForPageToLoad();
 
         src = selenium.getAttribute(attr);
         assertTrue(src.contains("calendarIcon.png"), "Calendar's icon was not updated.");
     }
 
     @Test
     public void testButtonIconDisabled() {
         selenium.click(pjq("input[name$=disabledInput][value=true]"));
         selenium.waitForPageToLoad();
 
         selenium.click(pjq("td:has(label:contains(heart)) > input[name$=buttonIconDisabledInput]"));
         selenium.waitForPageToLoad();
 
         AttributeLocator attr = image.getAttribute(Attribute.SRC);
         String src = selenium.getAttribute(attr);
         assertTrue(src.contains("heart.png"), "Calendar's icon was not updated.");
 
         selenium.click(pjq("td:has(label:contains(null)) > input[name$=buttonIconDisabledInput]"));
         selenium.waitForPageToLoad();
 
         src = selenium.getAttribute(attr);
         assertTrue(src.contains("disabledCalendarIcon.png"), "Calendar's icon was not updated.");
     }
 
     @Test
     public void testButtonLabel() {
         selenium.type(pjq("input[type=text][id$=buttonLabelInput]"), "label");
         selenium.waitForPageToLoad();
 
         assertTrue(selenium.isDisplayed(button), "Button should be displayed.");
         assertEquals(selenium.getText(button), "label", "Label of the button.");
         if (selenium.isElementPresent(image)) {
             assertFalse(selenium.isDisplayed(image), "Image should not be displayed.");
         }
 
         selenium.click(pjq("td:has(label:contains(star)) > input[name$=buttonIconInput]"));
         selenium.waitForPageToLoad();
 
         if (selenium.isElementPresent(image)) {
             assertFalse(selenium.isDisplayed(image), "Image should not be displayed.");
         }
     }
 
     @Test
     public void testDatePattern() {
         selenium.type(pjq("input[type=text][id$=datePatternInput]"), "hh:mm:ss a MMMM d, yyyy");
         selenium.waitForPageToLoad();
 
         selenium.click(input);
 
         selenium.click(cellDay.format(6));
         String day = selenium.getText(cellDay.format(6));
         String month = selenium.getText(monthLabel);
 
         String selectedDate = null;
         try {
             Date date = new SimpleDateFormat("d MMMM, yyyy hh:mm a").parse(day + " " + month + " 12:00 PM");
             selectedDate = new SimpleDateFormat("hh:mm:ss a MMMM d, yyyy").format(date);
         } catch (ParseException ex) {
             fail(ex.getMessage());
         }
 
         selenium.click(applyButton);
         assertFalse(selenium.isDisplayed(popup), "Popup should not be displayed.");
 
         String inputDate = selenium.getValue(input);
         assertEquals(inputDate, selectedDate, "Input doesn't contain selected date.");
     }
 
     @Test
     public void testDayClassFunction() {
         selenium.click(pjq("input[name$=dayClassFunctionInput][value=yellowTuesdays]"));
         selenium.waitForPageToLoad();
 
         selenium.click(input);
 
         for (int i = 2; i < 42; i += 7) {
             if (!selenium.belongsClass(cellDay.format(i), "rf-ca-boundary-dates")) {
                 assertTrue(selenium.belongsClass(cellDay.format(i), "yellowDay"), "Cell nr. " + i + " should be yellow.");
             }
         }
 
         selenium.click(pjq("input[name$=dayClassFunctionInput][value=]"));
         selenium.waitForPageToLoad();
 
         selenium.click(input);
 
         for (int i = 0; i < 42; i++) {
             assertFalse(selenium.belongsClass(cellDay.format(i), "yellowDay"), "Cell nr. " + i + " should not be yellow.");
         }
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9837")
     public void testDefaultTime() {
         selenium.type(pjq("input[type=text][id$=defaultTimeInput]"), "21:24");
         selenium.waitForPageToLoad();
 
         selenium.click(input);
         selenium.click(cellWeekDay.format(3, 3));
 
         boolean displayed = selenium.isDisplayed(timeButton);
         assertTrue(displayed, "Time button should be visible.");
         String buttonText = selenium.getText(timeButton);
         assertEquals(buttonText, "21:24", "Default time");
     }
 
     @Test
     public void testDisabled() {
         selenium.click(pjq("input[name$=disabledInput][value=true]"));
         selenium.waitForPageToLoad();
 
         AttributeLocator disabledAttr = input.getAttribute(new Attribute("disabled"));
         assertTrue(selenium.isAttributePresent(disabledAttr), "Disabled attribute of input should be defined.");
         assertEquals(selenium.getAttribute(disabledAttr), "disabled", "Input should be disabled.");
 
         selenium.click(input);
         assertFalse(selenium.isDisplayed(popup), "Popup should not be displayed.");
 
         selenium.click(image);
         assertFalse(selenium.isDisplayed(popup), "Popup should not be displayed.");
     }
 
     @Test
     public void testEnableManualInput() {
         AttributeLocator readonlyAttr = input.getAttribute(new Attribute("readonly"));
         assertTrue(selenium.isAttributePresent(readonlyAttr), "Readonly attribute of input should be defined.");
         assertEquals(selenium.getAttribute(readonlyAttr), "readonly", "Input should be read-only.");
 
         selenium.click(pjq("input[name$=enableManualInputInput][value=true]"));
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isAttributePresent(readonlyAttr), "Readonly attribute of input should not be defined.");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9646")
     public void testFirstWeekDay() {
         selenium.type(pjq("input[type=text][id$=firstWeekDayInput]"), "6");
         selenium.waitForPageToLoad();
 
         selenium.click(input);
 
         String[] labels = {"", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"};
 
         for (int i = 0; i < 8; i++) {
             String label = selenium.getText(weekDayLabel.format(i));
             assertEquals(label, labels[i], "Week day label " + i);
         }
 
         // wrong input - throws a server-side exception
         // selenium.type(pjq("input[type=text][id$=firstWeekDayInput]"), "9");
         // selenium.waitForPageToLoad();
         //
         // selenium.click(input);
         //
         // labels = new String[]{"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
         //
         // for (int i = 0; i < 8; i++) {
         //     String label = selenium.getText(weekDayLabel.format(i));
         //     assertEquals(label, labels[i], "Week day label " + i);
         // }
     }
 
     @Test
     public void testInputClass() {
         testStyleClass(input, "inputClass");
     }
 
     @Test
     public void testInputSize() {
         selenium.type(pjq("input[type=text][id$=inputSizeInput]"), "30");
         selenium.waitForPageToLoad();
 
         AttributeLocator sizeAttr = input.getAttribute(Attribute.SIZE);
         assertTrue(selenium.isAttributePresent(sizeAttr), "Size attribute of input should be defined.");
         assertEquals(selenium.getAttribute(sizeAttr), "30", "Input should be disabled.");
     }
 
     @Test
     public void testInputStyle() {
         testStyle(input, "inputStyle");
     }
 
     @Test
     public void testLocale() {
         selenium.type(pjq("input[type=text][id$=localeInput]"), "ru");
         selenium.waitForPageToLoad();
 
         selenium.click(input);
 
         String[] labels = {"", "Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб"};
 
         for (int i = 0; i < 8; i++) {
             String label = selenium.getText(weekDayLabel.format(i));
             assertEquals(label, labels[i], "Week day label " + i);
         }
 
         selenium.click(cellDay.format(6));
         String day = selenium.getText(cellDay.format(6));
         String month = selenium.getText(monthLabel);
 
         String selectedDate = null;
         try {
             Date date = new SimpleDateFormat("d MMMM, yyyy hh:mm", new Locale("ru")).parse(day + " " + month + " 12:00");
             selectedDate = new SimpleDateFormat("MMM d, yyyy hh:mm", new Locale("ru")).format(date);
         } catch (ParseException ex) {
             fail(ex.getMessage());
         }
 
         selenium.click(applyButton);
         String inputDate = selenium.getValue(input);
         assertEquals(inputDate, selectedDate, "Input doesn't contain selected date.");
     }
 
     @Test
     public void testOninputblur() {
         testFireEvent(Event.BLUR, input, "inputblur");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9602")
     public void testOninputchange() {
         selenium.click(pjq("input[name$=enableManualInputInput][value=true]"));
         selenium.waitForPageToLoad();
 
         selenium.type(pjq("input[id$=oninputchangeInput]"), "metamerEvents += \"inputchange \"");
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.type(input, "Dec 23, 2010 19:27");
 
         waitGui.failWith("Attribute oninputchange does not work correctly").until(
                 new EventFiredCondition(new Event("inputchange")));
     }
 
     @Test
     public void testOninputclick() {
         testFireEvent(Event.CLICK, input, "inputclick");
     }
 
     @Test
     public void testOninputdblclick() {
         testFireEvent(Event.DBLCLICK, input, "inputdblclick");
     }
 
     @Test
     public void testOninputfocus() {
         testFireEvent(Event.FOCUS, input, "inputfocus");
     }
 
     @Test
     public void testOninputkeydown() {
         testFireEvent(Event.KEYDOWN, input, "inputkeydown");
     }
 
     @Test
     public void testOninputkeypress() {
         testFireEvent(Event.KEYPRESS, input, "inputkeypress");
     }
 
     @Test
     public void testOninputkeyup() {
         testFireEvent(Event.KEYUP, input, "inputkeyup");
     }
 
     @Test
     public void testOninputmousedown() {
         testFireEvent(Event.MOUSEDOWN, input, "inputmousedown");
     }
 
     @Test
     public void testOninputmousemove() {
         testFireEvent(Event.MOUSEMOVE, input, "inputmousemove");
     }
 
     @Test
     public void testOninputmouseout() {
         testFireEvent(Event.MOUSEOUT, input, "inputmouseout");
     }
 
     @Test
     public void testOninputmouseover() {
         testFireEvent(Event.MOUSEOVER, input, "inputmouseover");
     }
 
     @Test
     public void testOninputmouseup() {
         testFireEvent(Event.MOUSEUP, input, "inputmouseup");
     }
 
     @Test
     public void testOninputselect() {
         testFireEvent(Event.SELECT, input, "inputselect");
     }
 
     @Test
     public void testPopup() {
         selenium.click(pjq("input[name$=popupInput][value=false]"));
         selenium.waitForPageToLoad();
 
         boolean displayed = selenium.isDisplayed(calendar);
         assertTrue(displayed, "Calendar is not present on the page.");
 
         if (selenium.isElementPresent(input)) {
             displayed = selenium.isDisplayed(input);
             assertFalse(displayed, "Calendar's input should not be visible.");
         }
 
         if (selenium.isElementPresent(image)) {
             displayed = selenium.isDisplayed(image);
             assertFalse(displayed, "Calendar's image should not be visible.");
         }
 
         displayed = selenium.isDisplayed(popup);
         assertTrue(displayed, "Popup should be visible.");
 
         displayed = selenium.isElementPresent(button);
         assertFalse(displayed, "Calendar's button should not be visible.");
     }
 
     @Test
     public void testRendered() {
         selenium.click(pjq("input[type=radio][name$=renderedInput][value=false]"));
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(calendar), "Panel should not be rendered when rendered=false.");
     }
 
     @Test
     public void testShowApplyButton() {
         selenium.click(pjq("input[type=radio][name$=showApplyButtonInput][value=false]"));
         selenium.waitForPageToLoad();
 
         selenium.click(input);
         if (selenium.isElementPresent(applyButton)) {
             assertFalse(selenium.isDisplayed(applyButton), "Apply button should not be displayed.");
         }
 
         guardXhr(selenium).click(cellDay.format(6));
         String day = selenium.getText(cellDay.format(6));
         String month = selenium.getText(monthLabel);
 
         String selectedDate = null;
         try {
             Date date = new SimpleDateFormat("d MMMM, yyyy hh:mm").parse(day + " " + month + " 12:00");
             selectedDate = new SimpleDateFormat("MMM d, yyyy hh:mm").format(date);
         } catch (ParseException ex) {
             fail(ex.getMessage());
         }
 
         assertFalse(selenium.isDisplayed(popup), "Popup should not be displayed.");
 
         String inputDate = selenium.getValue(input);
         assertEquals(inputDate, selectedDate, "Input doesn't contain selected date.");
     }
 
     @Test
     public void testShowFooter() {
         selenium.click(pjq("input[type=radio][name$=showFooterInput][value=false]"));
         selenium.waitForPageToLoad();
 
         selenium.click(input);
         boolean displayed = true;
 
         if (selenium.isElementPresent(todayButton)) {
             displayed = selenium.isDisplayed(todayButton);
             assertFalse(displayed, "Today button should not be visible.");
         }
 
         if (selenium.isElementPresent(applyButton)) {
             displayed = selenium.isDisplayed(applyButton);
             assertFalse(displayed, "Apply button should not be visible.");
         }
 
         displayed = selenium.isElementPresent(cleanButton);
         assertFalse(displayed, "Clean button should not be visible.");
 
         displayed = selenium.isElementPresent(timeButton);
         assertFalse(displayed, "Time button should not be visible.");
 
         selenium.click(cellWeekDay.format(3, 3));
 
         if (selenium.isElementPresent(cleanButton)) {
             displayed = selenium.isDisplayed(cleanButton);
             assertFalse(displayed, "Clean button should not be visible.");
         }
 
         if (selenium.isElementPresent(timeButton)) {
             displayed = selenium.isDisplayed(timeButton);
             assertFalse(displayed, "Time button should not be visible.");
         }
     }
 
     @Test
     public void testShowHeader() {
         selenium.click(pjq("input[type=radio][name$=showHeaderInput][value=false]"));
         selenium.waitForPageToLoad();
 
         selenium.click(input);
         boolean displayed = true;
 
         if (selenium.isElementPresent(prevYearButton)) {
             displayed = selenium.isDisplayed(prevYearButton);
             assertFalse(displayed, "Previous year button should not be visible.");
         }
 
         if (selenium.isElementPresent(prevMonthButton)) {
             displayed = selenium.isDisplayed(prevMonthButton);
             assertFalse(displayed, "Previous month button should not be visible.");
         }
 
         if (selenium.isElementPresent(nextMonthButton)) {
             displayed = selenium.isDisplayed(nextMonthButton);
             assertFalse(displayed, "Next month button should not be visible.");
         }
 
         if (selenium.isElementPresent(nextYearButton)) {
             displayed = selenium.isDisplayed(nextYearButton);
             assertFalse(displayed, "Next year button should not be visible.");
         }
 
         if (selenium.isElementPresent(closeButton)) {
             displayed = selenium.isDisplayed(closeButton);
             assertFalse(displayed, "Close button should not be visible.");
         }
 
         if (selenium.isElementPresent(monthLabel)) {
             displayed = selenium.isDisplayed(monthLabel);
             assertFalse(displayed, "Month label should not be visible.");
         }
     }
 
     @Test
     public void testShowInput() {
         selenium.click(pjq("input[type=radio][name$=showInputInput][value=false]"));
         selenium.waitForPageToLoad();
 
         if (selenium.isElementPresent(input)) {
             boolean displayed = selenium.isDisplayed(input);
             assertFalse(displayed, "Input should not be visible.");
         }
     }
 
     @Test
     public void testShowWeekDaysBar() {
         selenium.click(pjq("input[type=radio][name$=showWeekDaysBarInput][value=false]"));
         selenium.waitForPageToLoad();
 
         for (int i = 0; i < 8; i++) {
             if (selenium.isElementPresent(weekDayLabel.format(i))) {
                 boolean displayed = selenium.isDisplayed(weekDayLabel.format(i));
                 assertFalse(displayed, "Bar with week days should not be visible.");
             }
         }
     }
 
     @Test
     public void testShowWeeksBar() {
         selenium.click(pjq("input[type=radio][name$=showWeeksBarInput][value=false]"));
         selenium.waitForPageToLoad();
 
         for (int i = 0; i < 6; i++) {
             if (selenium.isElementPresent(week.format(i))) {
                 boolean displayed = selenium.isDisplayed(week.format(i));
                 assertFalse(displayed, "Bar with week numbers should not be visible.");
             }
         }
     }
 
     @Test
     public void testValueChangeListener() {
         String time1Value = selenium.getText(time);
         selenium.click(input);
         selenium.click(cellDay.format(6));
         guardXhr(selenium).click(applyButton);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
 
         String selectedDate1 = selenium.getValue(input);
         String selectedDate2 = selenium.getText(output);
         String listenerOutput = selenium.getText(jq("ul.phases-list li:eq(3)"));
 
         assertEquals(selectedDate1, selectedDate2, "Output and calendar's input should be the same.");
         assertEquals(listenerOutput, "* value changed: null -> " + selectedDate1);
     }
 
     /**
      * Checks that no date in the open month is selected.
      */
     private void assertNoDateSelected() {
         for (int i = 0; i < 42; i++) {
             assertFalse(selenium.belongsClass(cellDay.format(i), "rf-ca-sel"), "Cell nr. " + i + " should not be selected.");
         }
     }
 
     /**
      * Checks that no date in the open month is selected except of one passed as argument.
      * @param exceptOfDate date that should be selected (e.g. "13")
      */
     private void assertSelected(String exceptOfDate) {
         int lowerBoundary = 0;
         int upperBoundary = 42;
 
         if (Integer.parseInt(exceptOfDate) < 15) {
             upperBoundary = 21;
         } else {
             lowerBoundary = 21;
         }
 
         // check 3 lines of cells that contain selected date
         for (int i = lowerBoundary; i < upperBoundary; i++) {
             if (exceptOfDate.equals(selenium.getText(cellDay.format(i)))) {
                 assertTrue(selenium.belongsClass(cellDay.format(i), "rf-ca-sel"), "Cell nr. " + i + " should not be selected.");
             } else {
                 assertFalse(selenium.belongsClass(cellDay.format(i), "rf-ca-sel"), "Cell nr. " + i + " should not be selected.");
             }
         }
 
         lowerBoundary = lowerBoundary == 0 ? 21 : 0;
         upperBoundary = upperBoundary == 21 ? 42 : 21;
 
         // check other 3 lines of cells
         for (int i = lowerBoundary; i < upperBoundary; i++) {
             assertFalse(selenium.belongsClass(cellDay.format(i), "rf-ca-sel"), "Cell nr. " + i + " should not be selected.");
         }
     }
 }
