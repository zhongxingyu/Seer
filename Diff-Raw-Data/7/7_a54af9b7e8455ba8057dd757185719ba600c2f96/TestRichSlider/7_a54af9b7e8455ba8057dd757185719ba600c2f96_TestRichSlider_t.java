 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010-2011, Red Hat, Inc. and individual contributors
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
 package org.richfaces.tests.metamer.ftest.richInputNumberSlider;
 
 import java.text.ParseException;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.option.OptionLocatorFactory.optionLabel;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.css.CssProperty;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.geometry.Point;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.locator.ElementLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/richInputNumberSlider/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichSlider extends AbstractSliderTest {
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richInputNumberSlider/simple.xhtml");
     }
 
     @Test
     @Use(field = "number", value = "correctNumbers")
     @Override
     public void testTypeIntoInputCorrect() {
         super.testTypeIntoInputCorrect();
     }
 
     @Test
     @Use(field = "number", value = "smallNumbers")
     @Override
     public void testTypeIntoInputSmall() {
         super.testTypeIntoInputSmall();
     }
 
     @Test
     @Use(field = "number", value = "bigNumbers")
     @Override
     public void testTypeIntoInputBig() {
         super.testTypeIntoInputBig();
     }
 
     @Test
     @Use(field = "number", value = "decimalNumbers")
     @Override
     public void testTypeIntoInputDecimal() {
         super.testTypeIntoInputDecimal();
     }
 
     @Test
     @Override
     public void testTypeIntoInputNotNumber() {
         super.testTypeIntoInputNotNumber();
     }
 
     @Test
     @Override
     public void testClickLeft() {
         super.testClickLeft();
     }
 
     @Test
     @Override
     public void testClickRight() {
         super.testClickRight();
     }
 
     @Test
     @Override
     public void testClick() {
         super.testClick();
     }
 
     @Test
     public void testInit() {
         assertTrue(selenium.isDisplayed(slider), "Slider is not present on the page.");
         assertTrue(selenium.isDisplayed(input), "Slider's input is not present on the page.");
         assertFalse(selenium.isElementPresent(left), "Slider's left arrow should not be present on the page.");
         assertFalse(selenium.isElementPresent(right), "Slider's right arrow should not be present on the page.");
         assertTrue(selenium.isDisplayed(minBoundary), "Slider's min value is not present on the page.");
         assertTrue(selenium.isDisplayed(maxBoundary), "Slider's max value is not present on the page.");
         assertTrue(selenium.isDisplayed(track), "Slider's track is not present on the page.");
         assertTrue(selenium.isDisplayed(handle), "Slider's handle is not present on the page.");
         assertFalse(selenium.isElementPresent(tooltip), "Slider's tooltip should not be present on the page.");
     }
 
     @Test
     public void testAccesskey() {
         testHtmlAttribute(input, "accesskey", "x");
     }
 
     @Test
     public void testDecreaseClass() {
         selenium.click(pjq("input[type=radio][name$=showArrowsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         testStyleClass(left, "decreaseClass");
     }
 
     @Test
     public void testDecreaseSelectedClass() {
         selenium.click(pjq("input[type=radio][name$=showArrowsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         final String value = "metamer-ftest-class";
         selenium.type(pjq("input[id$=decreaseSelectedClassInput]"), value);
         selenium.waitForPageToLoad();
 
         selenium.mouseDown(left);
         assertTrue(selenium.belongsClass(left, value), "decreaseSelectedClass does not work");
     }
 
     @Test
     @Use(field = "delay", ints = {800, 1250, 3700})
     public void testDelay() {
         selenium.type(pjq("input[type=text][id$=delayInput]"), delay.toString());
         selenium.waitForPageToLoad();
         selenium.click(pjq("input[type=radio][name$=showArrowsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         try {
             verifyDelay(left, delay);
             verifyDelay(right, delay);
         } catch (ParseException ex) {
             fail(ex.getMessage());
         }
     }
 
     @Test
     public void testDisabled() {
         JQueryLocator selectOption = pjq("input[type=radio][name$=disabledInput][value=true]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         AttributeLocator disabledAttribute = input.getAttribute(new Attribute("disabled"));
         assertEquals(selenium.getAttribute(disabledAttribute), "disabled", "Input should be disabled.");
 
         assertFalse(selenium.isElementPresent(handle), "Handle should not be present on the page.");
 
         JQueryLocator handleDisabled = pjq("span.rf-insl-hnd-dis");
         assertTrue(selenium.isElementPresent(handleDisabled), "An disabled handle should be displayed.");
         assertTrue(selenium.isVisible(handleDisabled), "An disabled handle should be displayed.");
     }
 
     @Test
     public void testEnableManualInput() {
         JQueryLocator selectOption = pjq("input[type=radio][name$=enableManualInputInput][value=false]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         AttributeLocator readonlyAttribute = input.getAttribute(new Attribute("readonly"));
         assertEquals(selenium.getAttribute(readonlyAttribute), "readonly", "Input should be read-only.");
 
         testClick();
     }
 
     @Test
     public void testHandleClass() {
         testStyleClass(handle, "handleClass");
     }
 
     @Test
     public void testHandleSelectedClass() {
         final String value = "metamer-ftest-class";
         selenium.type(pjq("input[id$=handleSelectedClassInput]"), value);
         selenium.waitForPageToLoad();
 
         selenium.mouseDown(handle);
         assertTrue(selenium.belongsClass(handle, value), "handleSelectedClass does not work");
     }
 
     @Test
     public void testImmediate() {
         selenium.click(pjq("input[type=radio][name$=immediateInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).mouseDownAt(track, new Point(0, 0));
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         phaseInfo.assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
                 PhaseId.UPDATE_MODEL_VALUES, PhaseId.INVOKE_APPLICATION, PhaseId.RENDER_RESPONSE);
         phaseInfo.assertListener(PhaseId.APPLY_REQUEST_VALUES, "value changed: 2 -> -10");
     }
 
     @Test
     public void testIncreaseClass() {
         selenium.click(pjq("input[type=radio][name$=showArrowsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         testStyleClass(right, "increaseClass");
     }
 
     @Test
     public void testIncreaseSelectedClass() {
         selenium.click(pjq("input[type=radio][name$=showArrowsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         final String value = "metamer-ftest-class";
         selenium.type(pjq("input[id$=increaseSelectedClassInput]"), value);
         selenium.waitForPageToLoad();
 
         selenium.mouseDown(right);
         assertTrue(selenium.belongsClass(right, value), "increaseSelectedClass does not work");
     }
 
     @Test
     public void testInputClass() {
         testStyleClass(input, "inputClass");
     }
 
     @Test
     public void testInputPosition() {
         JQueryLocator br = pjq("span[id$=slider] br");
 
         selenium.select(pjq("select[id$=inputPositionInput]"), optionLabel("bottom"));
         selenium.waitForPageToLoad();
         int inputPosition = selenium.getElementPositionTop(input);
         int trackPosition = selenium.getElementPositionTop(track);
         assertTrue(trackPosition < inputPosition, "Track should be above input on the page.");
         assertTrue(selenium.isElementPresent(br), "Track and input should not be on the same line.");
 
         selenium.select(pjq("select[id$=inputPositionInput]"), optionLabel("top"));
         selenium.waitForPageToLoad();
         inputPosition = selenium.getElementPositionTop(input);
         trackPosition = selenium.getElementPositionTop(track);
         assertTrue(trackPosition > inputPosition, "Track should be below input on the page.");
         assertTrue(selenium.isElementPresent(br), "Track and input should not be on the same line.");
 
         selenium.select(pjq("select[id$=inputPositionInput]"), optionLabel("right"));
         selenium.waitForPageToLoad();
         inputPosition = selenium.getElementPositionLeft(input);
         trackPosition = selenium.getElementPositionLeft(track);
         assertTrue(trackPosition < inputPosition, "Track should be on the left of input on the page.");
         assertFalse(selenium.isElementPresent(br), "Track and input should be on the same line.");
 
         selenium.select(pjq("select[id$=inputPositionInput]"), optionLabel("left"));
         selenium.waitForPageToLoad();
         inputPosition = selenium.getElementPositionLeft(input);
         trackPosition = selenium.getElementPositionLeft(track);
         assertTrue(trackPosition > inputPosition, "Track should be on the right of input on the page.");
         assertFalse(selenium.isElementPresent(br), "Track and input should be on the same line.");
 
         selenium.select(pjq("select[id$=inputPositionInput]"), optionLabel("null"));
         selenium.waitForPageToLoad();
         inputPosition = selenium.getElementPositionLeft(input);
         trackPosition = selenium.getElementPositionLeft(track);
         assertTrue(trackPosition < inputPosition, "Track should be on the left of input on the page.");
         assertFalse(selenium.isElementPresent(br), "Track and input should be on the same line.");
     }
 
     @Test
     public void testInputSize() {
         JQueryLocator selectOption = pjq("input[type=text][id$=inputSizeInput]");
 
         selenium.type(selectOption, "2");
         selenium.waitForPageToLoad();
         AttributeLocator sizeAttribute = input.getAttribute(new Attribute("size"));
         assertEquals(selenium.getAttribute(sizeAttribute), "2", "Input's size attribute.");
 
         selenium.type(selectOption, "40");
         selenium.waitForPageToLoad();
         assertEquals(selenium.getAttribute(sizeAttribute), "40", "Input's size attribute.");
     }
 
     @Test
     public void testMaxValueType() {
         JQueryLocator selectOption = pjq("input[type=text][id$=maxValueInput]");
         selenium.type(selectOption, "13");
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "11");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "11", "Output was not updated.");
 
         reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "13");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "13", "Output was not updated.");
     }
 
     @Test
     public void testMaxValueClick() {
         selenium.type(pjq("input[type=text][id$=maxValueInput]"), "20");
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         reqTime = selenium.getText(time);
         guardXhr(selenium).mouseDownAt(track, new Point(170, 0));
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "17", "Output was not updated.");
         int margin = selenium.getElementPositionLeft(handle) - selenium.getElementPositionLeft(track);
         assertTrue(margin >= 167 && margin <= 173, "Left margin of handle should be between 167 and 173 (was " + margin + ").");
 
         reqTime = selenium.getText(time);
         guardXhr(selenium).mouseDownAt(track, new Point(195, 0));
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "20", "Output was not updated.");
         margin = selenium.getElementPositionLeft(handle) - selenium.getElementPositionLeft(track);
         assertTrue(margin >= 192 && margin <= 198, "Left margin of handle should be between 192 and 198 (was " + margin + ").");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9860")
     public void testMaxlength() {
         selenium.type(pjq("input[type=text][id$=maxlengthInput]"), "5");
         selenium.waitForPageToLoad();
 
         AttributeLocator attr = input.getAttribute(Attribute.MAXLENGTH);
         assertEquals(selenium.getAttribute(attr), "5", "Attribute maxlength of input.");
 
         selenium.type(pjq("input[type=text][id$=maxlengthInput]"), "");
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isAttributePresent(attr), "Attribute maxlength should not be present.");
     }
 
     @Test
     public void testMinValueType() {
         JQueryLocator selectOption = pjq("input[type=text][id$=minValueInput]");
         selenium.type(selectOption, "-13");
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "-11");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "-11", "Output was not updated.");
 
         reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "-13");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "-13", "Output was not updated.");
     }
 
     @Test
     public void testMinValueClick() {
         selenium.type(pjq("input[type=text][id$=minValueInput]"), "-20");
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         reqTime = selenium.getText(time);
         guardXhr(selenium).mouseDownAt(track, new Point(28, 0));
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "-16", "Output was not updated.");
         int margin = selenium.getElementPositionLeft(handle) - selenium.getElementPositionLeft(track);
         assertTrue(margin >= 25 && margin <= 31, "Left margin of handle should be between 25 and 31 (was " + margin + ").");
 
         reqTime = selenium.getText(time);
         guardXhr(selenium).mouseDownAt(track, new Point(0, 0));
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "-20", "Output was not updated.");
         margin = selenium.getElementPositionLeft(handle) - selenium.getElementPositionLeft(track);
         assertTrue(margin <= 3, "Left margin of handle should be between 0 and 3 (was " + margin + ").");
     }
 
     @Test
     public void testOnchangeType() {
         String value = "metamerEvents += \"change \"";
         selenium.type(pjq("input[id$=onchangeInput]"), value);
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "4");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "change", "Attribute onchange doesn't work");
         assertEquals(events.length, 1, "Only one event should be fired");
     }
 
     @Test
     public void testOnchangeClick() {
         selenium.click(pjq("input[type=radio][name$=showArrowsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String value = "metamerEvents += \"change \"";
         selenium.type(pjq("input[id$=onchangeInput]"), value);
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
 
         // click on track, left arrow and right arrow
         selenium.mouseDownAt(track, new Point(30, 0));
         selenium.mouseUpAt(track, new Point(30, 0));
         selenium.runScript(clickRight);
         selenium.runScript(clickLeft);
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
         assertEquals(events[0], "change", "Attribute onchange doesn't work");
         assertEquals(events[1], "change", "Attribute onchange doesn't work.");
         assertEquals(events[1], "change", "Attribute onchange doesn't work.");
         assertEquals(events.length, 3, "Three events should be fired.");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9947")
     public void testRendered() {
         selenium.click(pjq("input[type=radio][name$=renderedInput][value=false]"));
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(slider), "Slider should not be rendered when rendered=false.");
     }
 
     @Test
     public void testShowArrows() {
         selenium.click(pjq("input[type=radio][name$=showArrowsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         assertTrue(selenium.isElementPresent(left), "Left arrow should be present on the page.");
         assertTrue(selenium.isVisible(left), "Left arrow should be visible.");
         assertTrue(selenium.isElementPresent(right), "Right arrow should be present on the page.");
         assertTrue(selenium.isVisible(right), "Right arrow should be visible.");
     }
 
     @Test
     public void testShowBoundaryValues() {
         selenium.click(pjq("input[type=radio][name$=showBoundaryValuesInput][value=false]"));
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(minBoundary), "Boundary values should not be present on the page.");
         assertFalse(selenium.isElementPresent(maxBoundary), "Boundary values should not be present on the page.");
     }
 
     @Test
     public void testShowInput() {
         selenium.click(pjq("input[type=radio][name$=showInputInput][value=false]"));
         selenium.waitForPageToLoad();
 
         if (selenium.isElementPresent(input)) {
             assertFalse(selenium.isVisible(input), "Input should not be visible on the page.");
         }
     }
 
     @Test
     public void testShowTooltip() {
         selenium.click(pjq("input[type=radio][name$=showTooltipInput][value=true]"));
         selenium.waitForPageToLoad();
 
         assertTrue(selenium.isElementPresent(tooltip), "Tooltip should be present on the page.");
         assertFalse(selenium.isVisible(tooltip), "Tooltip should not be visible.");
 
         selenium.mouseDownAt(track, new Point(0, 0));
         assertTrue(selenium.isVisible(tooltip), "Tooltip should be visible.");
         assertEquals(selenium.getText(tooltip), "-10", "Value of tooltip.");
 
         selenium.mouseUpAt(track, new Point(0, 0));
         assertFalse(selenium.isVisible(tooltip), "Tooltip should not be visible.");
     }
 
     @Test
     public void testStep() {
         selenium.click(pjq("input[type=radio][name$=showArrowsInput][value=true]"));
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=delayInput]"), "100");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[id$=stepInput]"), "7");
         selenium.waitForPageToLoad();
 
         clickArrow(right, 1);
         assertEquals(selenium.getText(output), "9", "Wrong output");
 
         clickArrow(right, 1);
         assertEquals(selenium.getText(output), "10", "Wrong output");
 
         clickArrow(left, 1);
         assertEquals(selenium.getText(output), "3", "Wrong output");
 
         clickArrow(left, 1);
         assertEquals(selenium.getText(output), "-4", "Wrong output");
 
         clickArrow(left, 1);
         assertEquals(selenium.getText(output), "-10", "Wrong output");
 
         clickArrow(right, 1);
         assertEquals(selenium.getText(output), "-3", "Wrong output");
     }
 
     @Test
     public void testStyle() {
         testStyle(slider, "style");
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(slider, "styleClass");
     }
 
     @Test
     public void testTabindex() {
         selenium.type(pjq("input[id$=tabindexInput]"), "55");
         selenium.waitForPageToLoad(TIMEOUT);
 
         AttributeLocator attr = track.getAttribute(new Attribute("tabindex"));
         assertTrue(selenium.isAttributePresent(attr), "Attribute tabindex of track is not present.");
         assertEquals(selenium.getAttribute(attr), "55", "Attribute tabindex of track.");
 
         attr = input.getAttribute(new Attribute("tabindex"));
         assertTrue(selenium.isAttributePresent(attr), "Attribute tabindex of input is not present.");
         assertEquals(selenium.getAttribute(attr), "55", "Attribute tabindex of input.");
     }
 
     @Test
     public void testTooltipClass() {
         selenium.click(pjq("input[type=radio][name$=showTooltipInput][value=true]"));
         selenium.waitForPageToLoad();
 
         testStyleClass(tooltip, "tooltipClass");
     }
 
     @Test
     public void testTrackClass() {
         testStyleClass(track, "trackClass");
     }
 
     @Test
     @Use(field = "number", value = "correctNumbers")
     public void testValueCorrect() {
         selenium.type(pjq("input[id$=valueInput]"), number);
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(output), number, "Output was not updated.");
         assertEquals(selenium.getValue(input), number, "Input was not updated.");
     }
 
     @Test
     @Use(field = "number", value = "smallNumbers")
     public void testValueSmall() {
         selenium.type(pjq("input[id$=valueInput]"), number);
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(output), number, "Output was not updated.");
         assertEquals(selenium.getValue(input), "-10", "Input was not updated.");
     }
 
     @Test
     @Use(field = "number", value = "bigNumbers")
     public void testValueBig() {
         selenium.type(pjq("input[id$=valueInput]"), number);
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(output), number, "Output was not updated.");
         assertEquals(selenium.getValue(input), "10", "Input was not updated.");
     }
 
     /**
      * Clicks on slider's arrow.
      * @param arrow slider's left or right arrow locator
      * @param clicks how many times should it be clicked
      */
     private void clickArrow(ElementLocator<?> arrow, int clicks) {
         String reqTime = null;
 
         for (int i = 0; i < clicks; i++) {
             reqTime = selenium.getText(time);
             guardXhr(selenium).runScript(new JavaScript("jQuery(\"" + arrow.getRawLocator() + "\").mousedown().mouseup()"));
 
             waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         }
     }
 
     /**
      * Clicks on slider's arrow and verifies delay.
      * @param arrow slider's left or right arrow locator
      * @param delay awaited delay between ajax requests
      */
     private void verifyDelay(JQueryLocator arrow, int delay) throws ParseException {
         SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS");
         long delta = (long) (delay * 0.5);
         Set<String> timesSet = new TreeSet<String>();
 
         selenium.mouseDown(arrow);
 
         for (int i = 0; i < 14; i++) {
             timesSet.add(selenium.getText(time));
             waitFor(delta);
         }
 
         selenium.mouseUp(arrow);
 
         Date[] timesArray = new Date[timesSet.size()];
         List<String> timesList = new ArrayList<String>(timesSet);
 
         for (int i = 1; i < timesList.size(); i++) {
             timesArray[i] = sdf.parse(timesList.get(i));
         }
 
         delta = (long) (delay * 0.5);
         long average = countAverage(timesArray);
         assertTrue(Math.abs(average - delay) < delta, "Average delay " + average + " is too far from set value (" + delay + ")");
     }
 
     private long countAverage(Date[] times) {
         long total = 0L;
         for (int i = 1; i < times.length - 1; i++) {
             total += (times[i].getTime() - times[i + 1].getTime());
         }
 
         return Math.abs(total / (times.length - 2));
     }
 }
