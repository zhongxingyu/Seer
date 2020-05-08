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
 package org.richfaces.tests.metamer.ftest.richAccordionItem;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardHttp;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.locator.option.OptionLocatorFactory.optionLabel;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 import org.jboss.test.selenium.css.CssProperty;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page faces/components/richAccordionItem/simple.xhtml.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichAccordionItem extends AbstractMetamerTest {
 
     private JQueryLocator accordion = pjq("div[id$=accordion]");
     private JQueryLocator item1 = pjq("div[id$=item1].rf-ac-itm");
     private JQueryLocator[] itemHeaders = {pjq("div[id$=item1:header]"), pjq("div[id$=item2:header]"),
         pjq("div[id$=item3:header]"), pjq("div[id$=item4:header]"), pjq("div[id$=item5:header]")};
     private JQueryLocator[] itemContents = {pjq("div[id$=item1:content]"), pjq("div[id$=item2:content]"),
         pjq("div[id$=item3:content]"), pjq("div[id$=item4:content]"), pjq("div[id$=item5:content]")};
     private JQueryLocator[] activeHeaders = {pjq("div[id$=item1:header] div.rf-ac-itm-lbl-act"),
         pjq("div[id$=item2:header] div.rf-ac-itm-lbl-act"), pjq("div[id$=item3:header] div.rf-ac-itm-lbl-act"),
         pjq("div[id$=item4:header] div.rf-ac-itm-lbl-act"), pjq("div[id$=item5:header] div.rf-ac-itm-lbl-act")};
     private JQueryLocator[] inactiveHeaders = {pjq("div[id$=item1:header] div.rf-ac-itm-lbl-inact"),
         pjq("div[id$=item2:header] div.rf-ac-itm-lbl-inact"), pjq("div[id$=item3:header] div.rf-ac-itm-lbl-inact"),
         pjq("div[id$=item4:header] div.rf-ac-itm-lbl-inact"), pjq("div[id$=item5:header] div.rf-ac-itm-lbl-inact")};
     private JQueryLocator[] disabledHeaders = {pjq("div[id$=item1:header] div.rf-ac-itm-lbl-dis"),
         pjq("div[id$=item2:header] div.rf-ac-itm-lbl-dis"), pjq("div[id$=item3:header] div.rf-ac-itm-lbl-dis"),
         pjq("div[id$=item4:header] div.rf-ac-itm-lbl-dis"), pjq("div[id$=item5:header] div.rf-ac-itm-lbl-dis")};
     private JQueryLocator leftIcon = pjq("div[id$=item{0}] td.rf-ac-itm-ico");
     private JQueryLocator rightIcon = pjq("div[id$=item{0}] td.rf-ac-itm-exp-ico");
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richAccordionItem/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         boolean accordionDisplayed = selenium.isDisplayed(accordion);
         assertTrue(accordionDisplayed, "Accordion is not present on the page.");
 
         accordionDisplayed = selenium.isDisplayed(itemHeaders[2]);
         assertTrue(accordionDisplayed, "Item3's header should be visible.");
 
 
         accordionDisplayed = selenium.isDisplayed(itemContents[2]);
         assertTrue(accordionDisplayed, "Content of item3 should be visible.");
 
         accordionDisplayed = selenium.isDisplayed(itemContents[0]);
         assertFalse(accordionDisplayed, "Item1's content should not be visible.");
     }
 
     @Test
     public void testContentClass() {
         testStyleClass(itemContents[0], "contentClass");
     }
 
     @Test
     public void testDir() {
         testDir(item1);
     }
 
     @Test
     public void testDisabled() {
         selenium.click(pjq("input[type=radio][name$=disabledInput][value=true]"));
         selenium.waitForPageToLoad();
 
         guardNoRequest(selenium).click(itemHeaders[0]);
         boolean accordionDisplayed = selenium.isDisplayed(itemContents[0]);
         assertFalse(accordionDisplayed, "Item1's content should not be visible.");
     }
 
     @Test
     public void testHeader() {
         selenium.type(pjq("input[id$=headerInput]"), "new header");
         selenium.waitForPageToLoad();
 
         String header = selenium.getText(activeHeaders[0]);
         assertEquals(header, "new header", "Header of item1 did not change.");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10297")
     public void testHeaderActiveClass() {
         testStyleClass(activeHeaders[0], "headerActiveClass");
         assertFalse(selenium.belongsClass(activeHeaders[1], "metamer-ftest-class"), "headerActiveClass should be set only on first item");
         assertFalse(selenium.belongsClass(activeHeaders[2], "metamer-ftest-class"), "headerActiveClass should be set only on first item");
         assertFalse(selenium.belongsClass(activeHeaders[3], "metamer-ftest-class"), "headerActiveClass should be set only on first item");
     }
 
     @Test
     public void testHeaderClass() {
         testStyleClass(itemHeaders[0], "headerClass");
         assertFalse(selenium.belongsClass(itemHeaders[1], "metamer-ftest-class"), "headerClass should be set only on first item");
         assertFalse(selenium.belongsClass(itemHeaders[2], "metamer-ftest-class"), "headerClass should be set only on first item");
         assertFalse(selenium.belongsClass(itemHeaders[3], "metamer-ftest-class"), "headerClass should be set only on first item");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10297")
     public void testHeaderDisabledClass() {
         selenium.click(pjq("input[type=radio][name$=disabledInput][value=true]"));
         selenium.waitForPageToLoad();
 
         testStyleClass(disabledHeaders[0], "headerDisabledClass");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10297")
     public void testHeaderInactiveClass() {
         testStyleClass(inactiveHeaders[0], "headerInactiveClass");
         assertFalse(selenium.belongsClass(inactiveHeaders[1], "metamer-ftest-class"), "headerInactiveClass should be set only on first item");
         assertFalse(selenium.belongsClass(inactiveHeaders[2], "metamer-ftest-class"), "headerInactiveClass should be set only on first item");
         assertFalse(selenium.belongsClass(inactiveHeaders[3], "metamer-ftest-class"), "headerInactiveClass should be set only on first item");
     }
 
     @Test
     public void testHeaderStyle() {
         testStyle(itemHeaders[0], "headerStyle");
     }
 
     @Test
     public void testLang() {
         testLang(item1);
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-10488")
     public void testLeftActiveIcon() {
         JQueryLocator icon = leftIcon.format(1).getDescendant(jq("div.rf-ac-itm-ico-act"));
         JQueryLocator input = pjq("select[id$=leftActiveIconInput]");
         JQueryLocator image = leftIcon.format(1).getChild(jq("img"));
 
         // icon=null
         for (int i = 1; i < 6; i++) {
             assertFalse(selenium.isElementPresent(leftIcon.format(i)), "Left icon of item" + i + " should not be present on the page.");
         }
 
         guardXhr(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
 
         verifyStandardIcons(input, icon, image, "");
     }
 
     @Test
     public void testLeftDisabledIcon() {
         JQueryLocator icon = leftIcon.format(1).getDescendant(jq("div.rf-ac-itm-ico-inact"));
         JQueryLocator input = pjq("select[id$=leftDisabledIconInput]");
         JQueryLocator image = leftIcon.format(1).getChild(jq("img"));
 
         // icon=null
         for (int i = 1; i < 6; i++) {
             assertFalse(selenium.isElementPresent(leftIcon.format(i)), "Left icon of item" + i + " should not be present on the page.");
         }
 
         selenium.click(pjq("input[type=radio][name$=disabledInput][value=true]"));
         selenium.waitForPageToLoad();
 
         verifyStandardIcons(input, icon, image, "-dis");
     }
 
     @Test
     public void testLeftInactiveIcon() {
         JQueryLocator icon = leftIcon.format(1).getDescendant(jq("div.rf-ac-itm-ico-inact"));
         JQueryLocator input = pjq("select[id$=leftInactiveIconInput]");
         JQueryLocator image = leftIcon.format(1).getChild(jq("img"));
 
         // icon=null
         for (int i = 1; i < 6; i++) {
             assertFalse(selenium.isElementPresent(leftIcon.format(i)), "Left icon of item" + i + " should not be present on the page.");
         }
 
         verifyStandardIcons(input, icon, image, "");
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-10488")
     public void testName() {
         selenium.type(pjq("input[id$=nameInput]"), "new name");
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(pjq("input[type=submit][name$=switchButtonCustom]"));
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, item1);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, item1);
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-9821 https://issues.jboss.org/browse/RF-10488")
     public void testOnenter() {
         testFireEvent(Event.CLICK, itemHeaders[0], "enter");
     }
 
     @Test
     public void testOnheaderclick() {
         testFireEvent(Event.CLICK, itemHeaders[0], "headerclick");
     }
 
     @Test
     public void testOnheaderdblclick() {
         testFireEvent(Event.DBLCLICK, itemHeaders[0], "headerdblclick");
     }
 
     @Test
     public void testOnheadermousedown() {
         testFireEvent(Event.MOUSEDOWN, itemHeaders[0], "headermousedown");
     }
 
     @Test
     public void testOnheadermousemove() {
         testFireEvent(Event.MOUSEMOVE, itemHeaders[0], "headermousemove");
     }
 
     @Test
     public void testOnheadermouseup() {
         testFireEvent(Event.MOUSEUP, itemHeaders[0], "headermouseup");
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-9821 https://issues.jboss.org/browse/RF-10488")
     public void testOnleave() {
         selenium.type(pjq("input[type=text][id$=onleaveInput]"), "metamerEvents += \"leave \"");
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
         String time1Value = selenium.getText(time);
 
         guardXhr(selenium).click(itemHeaders[1]);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "leave", "Attribute onleave doesn't work");
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, item1);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, item1);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, item1);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, item1);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, item1);
     }
 
     @Test
     public void testRendered() {
         selenium.click(pjq("input[type=radio][name$=renderedInput][value=false]"));
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(item1), "Item1 should not be rendered when rendered=false.");
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-10488")
     public void testRightActiveIcon() {
         JQueryLocator icon = rightIcon.format(1).getDescendant(jq("div.rf-ac-itm-ico-act"));
         JQueryLocator input = pjq("select[id$=rightActiveIconInput]");
         JQueryLocator image = rightIcon.format(1).getChild(jq("img"));
 
         // icon=null
         for (int i = 1; i < 6; i++) {
             assertFalse(selenium.isElementPresent(rightIcon.format(i)), "Right icon of item" + i + " should not be present on the page.");
         }
 
         guardXhr(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
 
         verifyStandardIcons(input, icon, image, "");
     }
 
     @Test
     public void testRightDisabledIcon() {
         JQueryLocator icon = rightIcon.format(1).getDescendant(jq("div.rf-ac-itm-ico-inact"));
         JQueryLocator input = pjq("select[id$=rightDisabledIconInput]");
         JQueryLocator image = rightIcon.format(1).getChild(jq("img"));
 
         // icon=null
         for (int i = 1; i < 6; i++) {
             assertFalse(selenium.isElementPresent(rightIcon.format(i)), "Right icon of item" + i + " should not be present on the page.");
         }
 
         selenium.click(pjq("input[type=radio][name$=disabledInput][value=true]"));
         selenium.waitForPageToLoad();
 
         verifyStandardIcons(input, icon, image, "-dis");
     }
 
     @Test
     public void testRightInactiveIcon() {
         JQueryLocator icon = rightIcon.format(1).getDescendant(jq("div.rf-ac-itm-ico-inact"));
         JQueryLocator input = pjq("select[id$=rightInactiveIconInput]");
         JQueryLocator image = rightIcon.format(1).getChild(jq("img"));
 
         // icon=null
         for (int i = 1; i < 6; i++) {
             assertFalse(selenium.isElementPresent(rightIcon.format(i)), "Right icon of item" + i + " should not be present on the page.");
         }
 
         verifyStandardIcons(input, icon, image, "");
     }
 
     @Test
     public void testStyle() {
         testStyle(item1, "style");
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(item1, "styleClass");
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-10488")
     public void testSwitchTypeNull() {
         guardXhr(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testSwitchTypeAjax() {
         selenium.click(pjq("input[type=radio][name$=switchTypeInput][value=ajax]"));
         selenium.waitForPageToLoad();
 
         testSwitchTypeNull();
     }
 
     @Test
     public void testSwitchTypeClient() {
         selenium.click(pjq("input[type=radio][name$=switchTypeInput][value=client]"));
         selenium.waitForPageToLoad();
 
         guardNoRequest(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testSwitchTypeServer() {
         selenium.click(pjq("input[type=radio][name$=switchTypeInput][value=server]"));
         selenium.waitForPageToLoad();
 
         guardHttp(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testTitle() {
         testTitle(item1);
     }
 
     private void verifyStandardIcons(JQueryLocator input, JQueryLocator icon, JQueryLocator image, String classSuffix) {
         String imageNameSuffix = "";
         if (classSuffix.contains("dis")) {
             imageNameSuffix = "Disabled";
         }
 
         selenium.select(input, optionLabel("chevronDown"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-chevron-down-hdr" + classSuffix), "Div should have set class rf-ico-chevron-down" + classSuffix + ".");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("ChevronDown" + imageNameSuffix + ".png"), "Icon should contain a chevron down.");
 
         selenium.select(input, optionLabel("chevronUp"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-chevron-up-hdr" + classSuffix), "Div should have set class rf-ico-chevron-up" + classSuffix + ".");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("ChevronUp" + imageNameSuffix + ".png"), "Icon should contain a chevron up.");
 
         selenium.select(input, optionLabel("disc"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-disc-hdr" + classSuffix), "Div should have set class rf-ico-disc" + classSuffix + ".");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("Disc" + imageNameSuffix + ".png"), "Icon should contain a disc.");
 
         for (int i = 2; i < 6; i++) {
             assertFalse(selenium.isElementPresent(leftIcon.format(i)), "Left icon of item" + i + " should not be present on the page.");
         }
 
         selenium.select(input, optionLabel("grid"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-grid-hdr" + classSuffix), "Div should have set class rf-ico-grid" + classSuffix + ".");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("Grid" + imageNameSuffix + ".png"), "Icon should contain a grid.");
 
         selenium.select(input, optionLabel("triangle"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-triangle-hdr" + classSuffix), "Div should have set class rf-ico-triangle" + classSuffix + ".");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("Triangle" + imageNameSuffix + ".png"), "Icon should contain a triangle.");
 
         selenium.select(input, optionLabel("triangleDown"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-triangle-down-hdr" + classSuffix), "Div should have set class rf-ico-triangle-down" + classSuffix + ".");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("TriangleDown" + imageNameSuffix + ".png"), "Icon should contain a triangle down.");
 
         selenium.select(input, optionLabel("triangleUp"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-triangle-up-hdr" + classSuffix), "Div should have set class rf-ico-triangle-up" + classSuffix + ".");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("TriangleUp" + imageNameSuffix + ".png"), "Icon should contain a triangle up.");
 
         selenium.select(input, optionLabel("none"));
         selenium.waitForPageToLoad();
         assertFalse(selenium.isElementPresent(icon), "Icon should not be present when icon=none.");
 
         selenium.select(input, optionLabel("star"));
         selenium.waitForPageToLoad();
         assertFalse(selenium.isElementPresent(icon), "Icon's div should not be present when icon=star.");
         assertTrue(selenium.isElementPresent(image), "Icon's image should be rendered.");
         assertTrue(selenium.getAttribute(image.getAttribute(Attribute.SRC)).contains("star.png"), "Icon's src attribute should contain star.png.");
 
         selenium.select(input, optionLabel("nonexisting"));
         selenium.waitForPageToLoad();
         assertFalse(selenium.isElementPresent(icon), "Icon's div should not be present when icon=nonexisting.");
         assertTrue(selenium.isElementPresent(image), "Icon's image should be rendered.");
         assertTrue(selenium.getAttribute(image.getAttribute(Attribute.SRC)).contains("nonexisting"), "Icon's src attribute should contain nonexisting.");
     }
 }
