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
 package org.richfaces.tests.metamer.ftest.richTogglePanel;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardHttp;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.net.URL;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.jboss.test.selenium.waiting.EventFiredCondition;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/richTogglePanel/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichTogglePanel extends AbstractMetamerTest {
 
     private JQueryLocator panel = pjq("div[id$=richTogglePanel]");
     private JQueryLocator item1 = pjq("div[id$=item1]");
     private JQueryLocator item2 = pjq("div[id$=item2]");
     private JQueryLocator item3 = pjq("div[id$=item3]");
     // toggle controls
     private JQueryLocator tc1 = pjq("a[id$=tcLink1]");
     private JQueryLocator tc2 = pjq("a[id$=tcLink2]");
     private JQueryLocator tc3 = pjq("a[id$=tcLink3]");
     private JQueryLocator tcFirst = pjq("a[id$=tcFirst]");
     private JQueryLocator tcPrev = pjq("a[id$=tcPrev]");
     private JQueryLocator tcNext = pjq("a[id$=tcNext]");
     private JQueryLocator tcLast = pjq("a[id$=tcLast]");
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richTogglePanel/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         assertTrue(selenium.isElementPresent(panel), "Toggle panel is not present on the page.");
         assertTrue(selenium.isVisible(panel), "Toggle panel is not visible.");
         assertTrue(selenium.isElementPresent(item1), "Item 1 is not present on the page.");
         assertTrue(selenium.isVisible(item1), "Item 1 is not visible.");
         assertTrue(selenium.isElementPresent(item2), "Item 2 is not present on the page.");
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
         assertTrue(selenium.isElementPresent(item3), "Item 3 is not present on the page.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
     }
 
     @Test
     public void testFirstLastPrevNextSwitchNull() {
         guardXhr(selenium).click(tcNext);
         waitGui.failWith("Next item (2) is not displayed.").until(isDisplayed.locator(item2));
         assertFalse(selenium.isVisible(item1), "Item 1 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
 
         guardXhr(selenium).click(tcPrev);
         waitGui.failWith("Previous item (1) is not displayed.").until(isDisplayed.locator(item1));
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
 
         guardXhr(selenium).click(tcLast);
         waitGui.failWith("Last item (3) is not displayed.").until(isDisplayed.locator(item3));
         assertFalse(selenium.isVisible(item1), "Item 1 should not be visible.");
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
 
         guardXhr(selenium).click(tcFirst);
         waitGui.failWith("First item (1) is not displayed.").until(isDisplayed.locator(item1));
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
     }
 
     @Test
     public void testFirstLastPrevNextSwitchAjax() {
         selenium.click(pjq("input[name$=switchTypeInput][value=ajax]"));
         selenium.waitForPageToLoad();
 
         testFirstLastPrevNextSwitchNull();
     }
 
     @Test
     public void testFirstLastPrevNextSwitchClient() {
         selenium.click(pjq("input[name$=switchTypeInput][value=client]"));
         selenium.waitForPageToLoad();
 
         guardNoRequest(selenium).click(tcNext);
         waitGui.failWith("Next item (2) is not displayed.").until(isDisplayed.locator(item2));
         assertFalse(selenium.isVisible(item1), "Item 1 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
 
         guardNoRequest(selenium).click(tcPrev);
         waitGui.failWith("Previous item (1) is not displayed.").until(isDisplayed.locator(item1));
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
 
         guardNoRequest(selenium).click(tcLast);
         waitGui.failWith("Last item (3) is not displayed.").until(isDisplayed.locator(item3));
         assertFalse(selenium.isVisible(item1), "Item 1 should not be visible.");
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
 
         guardNoRequest(selenium).click(tcFirst);
         waitGui.failWith("First item (1) is not displayed.").until(isDisplayed.locator(item1));
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10040")
     public void testFirstLastPrevNextSwitchServer() {
         selenium.click(pjq("input[name$=switchTypeInput][value=server]"));
         selenium.waitForPageToLoad();
 
         guardHttp(selenium).click(tcNext);
         assertTrue(selenium.isVisible(item2), "Next item (2) should be visible.");
         assertFalse(selenium.isDisplayed(item1), "Item 1 should not be displayed.");
         assertFalse(selenium.isDisplayed(item3), "Item 3 should not be displayed.");
 
         guardHttp(selenium).click(tcPrev);
         assertTrue(selenium.isVisible(item1), "Previous item (2) should be visible.");
         assertFalse(selenium.isDisplayed(item2), "Item 2 should not be displayed.");
         assertFalse(selenium.isDisplayed(item3), "Item 3 should not be displayed.");
 
         guardHttp(selenium).click(tcLast);
         assertTrue(selenium.isVisible(item3), "Last item (3) should be visible.");
         assertFalse(selenium.isDisplayed(item1), "Item 1 should not be displayed.");
         assertFalse(selenium.isDisplayed(item2), "Item 2 should not be displayed.");
 
         guardHttp(selenium).click(tcFirst);
         assertTrue(selenium.isVisible(item1), "First item (1) should be visible.");
         assertFalse(selenium.isDisplayed(item2), "Item 2 should not be displayed.");
         assertFalse(selenium.isDisplayed(item3), "Item 3 should not be displayed.");
     }
 
     @Test
     public void testActiveItem() {
         selenium.type(pjq("input[type=text][id$=activeItemInput]"), "item3");
         selenium.waitForPageToLoad();
 
         boolean displayed = selenium.isDisplayed(panel);
         assertTrue(displayed, "Toggle panel is not present on the page.");
 
         displayed = selenium.isDisplayed(item1);
         assertFalse(displayed, "Content of item1 should not be visible.");
 
         displayed = selenium.isDisplayed(item2);
         assertFalse(displayed, "Content of item2 should not be visible.");
 
         displayed = selenium.isDisplayed(item3);
         assertTrue(displayed, "Content of item3 should be visible.");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10054")
     public void testBypassUpdates() {
         selenium.click(pjq("input[type=radio][name$=bypassUpdatesInput][value=true]"));
         selenium.waitForPageToLoad();
 
         selenium.click(tc3);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(item3));
 
         phaseInfo.assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
                 PhaseId.RENDER_RESPONSE);

        String listenerOutput = selenium.getText(jq("div#phasesPanel li:eq(3)"));
        assertEquals(listenerOutput, "* item changed item1 -> item3", "Item change listener's output");
     }
 
     @Test
     public void testCycledSwitching() {
         selenium.click(pjq("input[type=radio][name$=cycledSwitchingInput][value=true]"));
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(tcPrev);
         waitGui.failWith("Previous item (3) is not displayed.").until(isDisplayed.locator(item3));
         guardXhr(selenium).click(tcNext);
         waitGui.failWith("Next item (1) is not displayed.").until(isDisplayed.locator(item1));
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10061")
     public void testData() {
         selenium.type(pjq("input[type=text][id$=dataInput]"), "RichFaces 4");
         selenium.waitForPageToLoad();
 
         selenium.type(pjq("input[type=text][id$=oncompleteInput]"), "data = event.data");
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(tc3);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(item3));
 
         String data = selenium.getEval(new JavaScript("window.data"));
         assertEquals(data, "RichFaces 4", "Data sent with ajax request");
     }
 
     @Test
     public void testDir() {
         super.testDir(panel);
     }
 
     @Test
     public void testExecute() {
         selenium.type(pjq("input[type=text][id$=executeInput]"), "@this executeChecker");
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(tc3);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(item3));
 
         JQueryLocator logItems = jq("ul.phases-list li:eq({0})");
         for (int i = 0; i < 6; i++) {
             if ("* executeChecker".equals(selenium.getText(logItems.format(i)))) {
                 return;
             }
         }
 
         fail("Attribute execute does not work");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10054")
     public void testImmediate() {
         selenium.click(pjq("input[type=radio][name$=immediateInput][value=true]"));
         selenium.waitForPageToLoad();
 
         selenium.click(tc3);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(item3));
 
         phaseInfo.assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.RENDER_RESPONSE);

        String listenerOutput = selenium.getText(jq("div#phasesPanel li:eq(2)"));
        assertEquals(listenerOutput, "* item changed: item1 -> item3", "Item change listener's output");
     }
 
     @Test
     public void testItemChangeListener() {
         selenium.click(tc3);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(item3));
 
         phaseInfo.assertListener(PhaseId.INVOKE_APPLICATION, "item changed: item1 -> item3");
     }
 
     @Test
     public void testLang() {
         testLang(panel);
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9881")
     public void testLimitRender() {
         selenium.click(pjq("input[type=radio][name$=limitRenderInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String timeValue = selenium.getText(time);
 
         guardXhr(selenium).click(tc2);
         waitGui.failWith("Item 2 is not displayed.").until(isDisplayed.locator(item2));
 
         String newTimeValue = selenium.getText(time);
         assertEquals(newTimeValue, timeValue, "Panel with ajaxRendered=true should not be rerendered.");
     }
 
     @Test
     public void testOnbeforeitemchange() {
         selenium.type(pjq("input[id$=onbeforeitemchangeInput]"), "metamerEvents += \"onbeforeitemchange \"");
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(tc2);
         waitGui.failWith("Item 2 is not displayed.").until(isDisplayed.locator(item2));
 
         waitGui.failWith("onbeforeitemchange attribute does not work correctly").until(new EventFiredCondition(new Event("beforeitemchange")));
     }
 
     @Test
     public void testAjaxEvents() {
         selenium.type(pjq("input[type=text][id$=onbeginInput]"), "metamerEvents += \"begin \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=onbeforedomupdateInput]"), "metamerEvents += \"beforedomupdate \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=oncompleteInput]"), "metamerEvents += \"complete \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).click(tc2);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events.length, 3, "3 events should be fired.");
         assertEquals(events[0], "begin", "Attribute onbegin doesn't work");
         assertEquals(events[1], "beforedomupdate", "Attribute onbeforedomupdate doesn't work");
         assertEquals(events[2], "complete", "Attribute oncomplete doesn't work");
     }
 
     @Test
     public void testItemchangeEvents() {
         selenium.type(pjq("input[type=text][id$=onbeforeitemchangeInput]"), "metamerEvents += \"beforeitemchange \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=onitemchangeInput]"), "metamerEvents += \"itemchange \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
         String time1Value = selenium.getText(time);
 
         guardXhr(selenium).click(tc2);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "beforeitemchange", "Attribute onbeforeitemchange doesn't work");
         assertEquals(events[1], "itemchange", "Attribute onbeforeitemchange doesn't work");
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, panel);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, panel);
     }
 
     @Test
     public void testOnitemchange() {
         selenium.type(pjq("input[id$=onitemchangeInput]"), "metamerEvents += \"onitemchange \"");
         selenium.waitForPageToLoad(TIMEOUT);
 
         guardXhr(selenium).click(tc2);
         waitGui.failWith("Item 2 is not displayed.").until(isDisplayed.locator(item2));
 
         waitGui.failWith("onitemchange attribute does not work correctly").until(new EventFiredCondition(new Event("itemchange")));
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, panel);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, panel);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, panel);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, panel);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, panel);
     }
 
     @Test
     public void testRender() {
         selenium.type(pjq("input[type=text][id$=renderInput]"), "renderChecker");
         selenium.waitForPageToLoad();
 
         String renderCheckerTime = selenium.getText(renderChecker);
         guardXhr(selenium).click(tc2);
         waitGui.failWith("Attribute render doesn't work").waitForChange(renderCheckerTime, retrieveText.locator(renderChecker));
     }
 
     @Test
     public void testRendered() {
         selenium.click(pjq("input[type=radio][name$=renderedInput][value=false]"));
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(panel), "Toggle panel should not be rendered when rendered=false.");
     }
 
     @Test
     public void testStatus() {
         selenium.type(pjq("input[type=text][id$=statusInput]"), "statusChecker");
         selenium.waitForPageToLoad();
 
         String statusCheckerTime = selenium.getText(statusChecker);
         guardXhr(selenium).click(tc2);
         waitGui.failWith("Attribute status doesn't work").waitForChange(statusCheckerTime, retrieveText.locator(statusChecker));
     }
 
     @Test
     public void testStyle() {
         testStyle(panel, "style");
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(panel, "styleClass");
     }
 
     @Test
     public void testSwitchTypeNull() {
         guardXhr(selenium).click(tc3);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(item3));
         assertFalse(selenium.isVisible(item1), "Item 1 should not be visible.");
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
 
         guardXhr(selenium).click(tc2);
         waitGui.failWith("Item 2 is not displayed.").until(isDisplayed.locator(item2));
         assertFalse(selenium.isVisible(item1), "Item 1 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
 
         guardXhr(selenium).click(tc1);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(item1));
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
     }
 
     @Test
     public void testSwitchTypeAjax() {
         selenium.click(pjq("input[name$=switchTypeInput][value=ajax]"));
         selenium.waitForPageToLoad();
 
         testSwitchTypeNull();
     }
 
     @Test
     public void testSwitchTypeClient() {
         selenium.click(pjq("input[name$=switchTypeInput][value=client]"));
         selenium.waitForPageToLoad();
 
         guardNoRequest(selenium).click(tc3);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(item3));
         assertFalse(selenium.isVisible(item1), "Item 1 should not be visible.");
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
 
         guardNoRequest(selenium).click(tc2);
         waitGui.failWith("Item 2 is not displayed.").until(isDisplayed.locator(item2));
         assertFalse(selenium.isVisible(item1), "Item 1 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
 
         guardNoRequest(selenium).click(tc1);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(item1));
         assertFalse(selenium.isVisible(item2), "Item 2 should not be visible.");
         assertFalse(selenium.isVisible(item3), "Item 3 should not be visible.");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10040")
     public void testSwitchTypeServer() {
         selenium.click(pjq("input[name$=switchTypeInput][value=server]"));
         selenium.waitForPageToLoad();
 
         guardHttp(selenium).click(tc3);
         assertTrue(selenium.isVisible(item3), "Item 3 should be visible.");
         assertFalse(selenium.isDisplayed(item1), "Item 1 should not be displayed.");
         assertFalse(selenium.isDisplayed(item2), "Item 2 should not be displayed.");
 
         guardHttp(selenium).click(tc2);
         assertTrue(selenium.isVisible(item2), "Item 2 should be visible.");
         assertFalse(selenium.isDisplayed(item1), "Item 1 should not be displayed.");
         assertFalse(selenium.isDisplayed(item3), "Item 3 should not be displayed.");
 
         guardHttp(selenium).click(tc1);
         assertTrue(selenium.isVisible(item1), "Item 1 should be visible.");
         assertFalse(selenium.isDisplayed(item2), "Item 2 should not be displayed.");
         assertFalse(selenium.isDisplayed(item3), "Item 3 should not be displayed.");
     }
 
     @Test
     public void testTitle() {
         testTitle(panel);
     }
 }
