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
 package org.richfaces.tests.metamer.ftest.richAccordion;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardHttp;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.css.CssProperty;
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
import org.jboss.test.selenium.waiting.conditions.IsDisplayed;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/richAccordion/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichAccordion extends AbstractMetamerTest {
 
     private JQueryLocator accordion = pjq("div[id$=accordion]");
     private JQueryLocator[] itemHeaders = {pjq("div[id$=item1:header]"), pjq("div[id$=item2:header]"),
         pjq("div[id$=item3:header]"), pjq("div[id$=item4:header]"), pjq("div[id$=item5:header]")};
     private JQueryLocator[] itemContents = {pjq("div[id$=item1:content]"), pjq("div[id$=item2:content]"),
         pjq("div[id$=item3:content]"), pjq("div[id$=item4:content]"), pjq("div[id$=item5:content]")};
     private JQueryLocator[] activeHeaders = {pjq("div.rf-ac-itm-hdr-act:eq(0)"), pjq("div.rf-ac-itm-hdr-act:eq(1)"),
         pjq("div.rf-ac-itm-hdr-act:eq(2)"), pjq("div.rf-ac-itm-hdr-act:eq(3)"), pjq("div.rf-ac-itm-hdr-act:eq(4)")};
     private JQueryLocator[] inactiveHeaders = {pjq("div.rf-ac-itm-hdr-inact:eq(0)"),
         pjq("div.rf-ac-itm-hdr-inact:eq(1)"), pjq("div.rf-ac-itm-hdr-inact:eq(2)"),
         pjq("div.rf-ac-itm-hdr-inact:eq(3)"), pjq("div.rf-ac-itm-hdr-inact:eq(4)")};
     private JQueryLocator[] disabledHeaders = {pjq("div.rf-ac-itm-hdr-dis:eq(0)"), pjq("div.rf-ac-itm-hdr-dis:eq(1)"),
         pjq("div.rf-ac-itm-hdr-dis:eq(2)"), pjq("div.rf-ac-itm-hdr-dis:eq(3)"), pjq("div.rf-ac-itm-hdr-dis:eq(4)")};
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richAccordion/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         boolean accordionDisplayed = selenium.isDisplayed(accordion);
         assertTrue(accordionDisplayed, "Accordion is not present on the page.");
 
         for (int i = 0; i < 5; i++) {
             accordionDisplayed = selenium.isDisplayed(itemHeaders[i]);
             assertTrue(accordionDisplayed, "Item" + (i + 1) + "'s header should be visible.");
         }
 
         accordionDisplayed = selenium.isDisplayed(itemContents[0]);
         assertTrue(accordionDisplayed, "Content of item1 should be visible.");
 
         for (int i = 1; i < 5; i++) {
             accordionDisplayed = selenium.isDisplayed(itemContents[i]);
             assertFalse(accordionDisplayed, "Item" + (i + 1) + "'s content should not be visible.");
         }
     }
 
     @Test
     public void testSwitchTypeNull() {
         for (int i = 2; i >= 0; i--) {
             final int index = i;
             guardXhr(selenium).click(itemHeaders[index]);
             waitGui.failWith("Item " + index + " is not displayed.").until(isDisplayed.locator(itemContents[index]));
         }
     }
 
     @Test
     public void testSwitchTypeAjax() {
         JQueryLocator selectOption = pjq("input[type=radio][id$=switchTypeInput:0]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         testSwitchTypeNull();
     }
 
     @Test
     public void testSwitchTypeClient() {
         JQueryLocator selectOption = pjq("input[type=radio][id$=switchTypeInput:1]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         for (int i = 2; i >= 0; i--) {
             final int index = i;
             guardNoRequest(selenium).click(itemHeaders[index]);
             waitGui.failWith("Item " + index + " is not displayed.").until(isDisplayed.locator(itemContents[index]));
         }
     }
 
     @Test
     public void testSwitchTypeServer() {
         JQueryLocator selectOption = pjq("input[type=radio][id$=switchTypeInput:3]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         for (int i = 2; i >= 0; i--) {
             final int index = i;
             guardHttp(selenium).click(itemHeaders[index]);
             waitGui.failWith("Item " + index + " is not displayed.").until(isDisplayed.locator(itemContents[index]));
         }
     }
 
     @Test
     public void testBypassUpdates() {
         JQueryLocator input = pjq("input[type=radio][name$=bypassUpdatesInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         selenium.click(itemHeaders[2]);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(itemContents[2]));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
                 PhaseId.RENDER_RESPONSE);
     }
 
     @Test
     public void testCycledSwitching() {
         String accordionId = selenium.getEval(new JavaScript("window.testedComponentId"));
         String result = null;
 
         // RichFaces.$('form:accordion').nextItem('item4') will be null
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + accordionId + "').nextItem('item4')"));
         assertEquals(result, "null", "Result of function nextItem('item4')");
 
         // RichFaces.$('form:accordion').prevItem('item1') will be null
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + accordionId + "').prevItem('item1')"));
         assertEquals(result, "null", "Result of function prevItem('item1')");
 
         JQueryLocator input = pjq("input[type=radio][name$=cycledSwitchingInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         // RichFaces.$('form:accordion').nextItem('item5') will be item1
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + accordionId + "').nextItem('item5')"));
         assertEquals(result, "item1", "Result of function nextItem('item5')");
 
         // RichFaces.$('form:accordion').prevItem('item1') will be item5
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + accordionId + "').prevItem('item1')"));
         assertEquals(result, "item5", "Result of function prevItem('item1')");
     }
 
     @Test
     public void testDir() {
         testDir(accordion);
     }
 
     @Test
     public void testHeight() {
         JQueryLocator input = pjq("input[type=text][id$=heightInput]");
         AttributeLocator<?> attribute = accordion.getAttribute(new Attribute("style"));
 
         // height = null
         assertFalse(selenium.isAttributePresent(attribute), "Attribute style should not be present.");
 
         // height = 300px
         selenium.type(input, "300px");
         selenium.waitForPageToLoad(TIMEOUT);
 
         assertTrue(selenium.isAttributePresent(attribute), "Attribute style should be present.");
         String value = selenium.getStyle(accordion, CssProperty.HEIGHT);
         assertEquals(value, "300px", "Attribute width");
     }
 
     @Test
     public void testImmediate() {
         JQueryLocator input = pjq("input[type=radio][name$=immediateInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         selenium.click(itemHeaders[2]);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(itemContents[2]));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.RENDER_RESPONSE);
     }
 
     @Test
     public void testItemContentClass() {
         testStyleClass(itemContents[2], "itemContentClass");
     }
 
     @Test
     public void testItemHeaderClass() {
         testStyleClass(itemHeaders[2], "itemHeaderClass");
     }
 
     @Test
     public void testItemHeaderClassActive() {
         selenium.type(pjq("input[id$=itemHeaderClassActiveInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         for (JQueryLocator loc : activeHeaders) {
             assertTrue(selenium.belongsClass(loc, "metamer-ftest-class"), "itemHeaderClassActive does not work");
         }
 
         for (JQueryLocator loc : inactiveHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "itemHeaderClassActive does not work");
         }
 
         for (JQueryLocator loc : disabledHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "itemHeaderClassActive does not work");
         }
     }
 
     @Test
     public void testItemHeaderClassDisabled() {
         selenium.type(pjq("input[id$=itemHeaderClassDisabledInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         for (JQueryLocator loc : activeHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "itemHeaderClassDisabled does not work");
         }
 
         for (JQueryLocator loc : inactiveHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "itemHeaderClassDisabled does not work");
         }
 
         for (JQueryLocator loc : disabledHeaders) {
             assertTrue(selenium.belongsClass(loc, "metamer-ftest-class"), "itemHeaderClassDisabled does not work");
         }
     }
 
     @Test
     public void testItemHeaderClassInactive() {
         selenium.type(pjq("input[id$=itemHeaderClassInactiveInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         for (JQueryLocator loc : activeHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "itemHeaderClassInactive does not work");
         }
 
         for (JQueryLocator loc : inactiveHeaders) {
             assertTrue(selenium.belongsClass(loc, "metamer-ftest-class"), "itemHeaderClassInactive does not work");
         }
 
         for (JQueryLocator loc : disabledHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "itemHeaderClassInactive does not work");
         }
     }
 
     @Test
     public void testItemchangeEvents() {
         JQueryLocator time = jq("span[id$=requestTime]");
 
         selenium.type(pjq("input[type=text][id$=onbeforeitemchangeInput]"), "metamerEvents += \"beforeitemchange \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=onitemchangeInput]"), "metamerEvents += \"itemchange \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
         String time1Value = selenium.getText(time);
 
         guardXhr(selenium).click(itemHeaders[2]);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "beforeitemchange", "Attribute onbeforeitemchange doesn't work");
         assertEquals(events[1], "itemchange", "Attribute onbeforeitemchange doesn't work");
     }
 
     @Test
     public void testLang() {
         testLang(accordion);
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, accordion);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, accordion);
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, accordion);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, accordion);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, accordion);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, accordion);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, accordion);
     }
 
     @Test
     public void testRendered() {
         JQueryLocator input = pjq("input[type=radio][name$=renderedInput][value=false]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(accordion), "Accordion should not be rendered when rendered=false.");
     }
 
     @Test
     public void testStyle() {
         testStyle(accordion, "style");
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(accordion, "styleClass");
     }
 
     @Test
     public void testTitle() {
         testTitle(accordion);
     }
 
     @Test
     public void testWidth() {
         JQueryLocator input = pjq("input[type=text][id$=widthInput]");
         AttributeLocator<?> attribute = accordion.getAttribute(new Attribute("style"));
 
         // width = null
         assertFalse(selenium.isAttributePresent(attribute), "Attribute style should not be present.");
 
         // width = 50%
         selenium.type(input, "50%");
         selenium.waitForPageToLoad(TIMEOUT);
 
         assertTrue(selenium.isAttributePresent(attribute), "Attribute style should be present.");
         String value = selenium.getStyle(accordion, CssProperty.WIDTH);
         assertEquals(value, "50%", "Attribute width");
     }
 }
