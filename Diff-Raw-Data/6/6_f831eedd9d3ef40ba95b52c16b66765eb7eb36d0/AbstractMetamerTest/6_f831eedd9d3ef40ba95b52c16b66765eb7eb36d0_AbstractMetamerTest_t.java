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
 package org.richfaces.tests.metamer.ftest;
 
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.AbstractTestCase;
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.locator.ElementLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.jboss.test.selenium.waiting.EventFiredCondition;
 import org.richfaces.tests.metamer.Phase;
 import org.richfaces.tests.metamer.TemplatesList;
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
 import org.richfaces.tests.metamer.ftest.annotations.Templates;
 import org.testng.SkipException;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 /**
  * Abstract test case used as a basis for majority of test cases.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public abstract class AbstractMetamerTest extends AbstractTestCase {
 
     /**
      * timeout in miliseconds
      */
     public static final long TIMEOUT = 5000;
     @Inject
     @Templates({"plain", "richDataTable1,redDiv", "richDataTable2,redDiv", "a4jRepeat1", "a4jRepeat2", "hDataTable1",
         "hDataTable2", "uiRepeat1", "uiRepeat2"})
     private TemplatesList template;
 
     /**
      * Returns the url to test page to be opened by Selenium
      * 
      * @return absolute url to the test page to be opened by Selenium
      */
     public abstract URL getTestUrl();
 
     /**
      * Opens the tested page. If templates is not empty nor null, it appends url parameter with templates.
      * 
      * @param templates
      *            templates that will be used for test, e.g. "red_div"
      */
     @BeforeMethod(alwaysRun = true)
     public void loadPage(Object[] templates) {
         if (selenium == null) {
             new SkipException("selenium isn't initialized");
         }
         selenium.open(buildUrl(getTestUrl() + "?templates=" + template.toString()));
         selenium.waitForPageToLoad(TIMEOUT);
     }
 
     /**
      * Invalidates session by clicking on a button on tested page.
      */
     @AfterMethod(alwaysRun = true)
     public void invalidateSession() {
         selenium.deleteAllVisibleCookies();
     }
 
     /**
      * Forces the current thread sleep for given time.
      * 
      * @param millis
      *            number of miliseconds for which the thread will sleep
      */
     @Deprecated
     protected void waitFor(long millis) {
         try {
             Thread.sleep(millis);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Factory method for creating instances of class JQueryLocator which locates the element using <a
      * href="http://api.jquery.com/category/selectors/">JQuery Selector</a> syntax. It adds "div.content " in front of
      * each selector.
      * 
      * @param jquerySelector
      *            the jquery selector
      * @return the j query locator
      * @see JQueryLocator
      */
     public static JQueryLocator pjq(String jquerySelector) {
         return new JQueryLocator("div.content " + jquerySelector);
     }
 
     /**
      * A helper method for testing javascripts events. It sets alert('testedevent') to the input field for given event
      * and fires the event. Then it checks the message in the alert dialog.
      * 
      * @param event
      *            JavaScript event to be tested
      * @param element
      *            locator of tested element
      */
     protected void testFireEvent(Event event, ElementLocator<?> element) {
         testFireEvent(event, element, event.getEventName());
     }
 
     /**
      * A helper method for testing javascripts events. It sets alert('testedevent') to the input field for given event
      * and fires the event. Then it checks the message in the alert dialog.
      *
      * @param event
      *            JavaScript event to be tested
      * @param element
      *            locator of tested element
      * @param attributeName
      *            name of the attribute that should be set
      */
     protected void testFireEvent(Event event, ElementLocator<?> element, String attributeName) {
         ElementLocator<?> eventInput = pjq("input[id$=on" + attributeName + "Input]");
         String value = "metamerEvents += \"" + event.getEventName() + " \"";
 
         selenium.type(eventInput, value);
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.fireEvent(element, event);
 
         waitGui.failWith(event.getEventName() + " attribute did not change correctly").until(
                 new EventFiredCondition(event));
     }
 
     /**
      * A helper method for testing attribute "style". It sets "background-color: yellow; font-size: 1.5em;" to the input
      * field and checks that it was changed on the page.
      * 
      * @param element
      *            locator of tested element
      */
     protected void testStyle(ElementLocator<?> element) {
         ElementLocator<?> styleInput = pjq("input[id$=styleInput]");
         final String value = "background-color: yellow; font-size: 1.5em;";
 
         selenium.type(styleInput, value);
         selenium.waitForPageToLoad();
 
         AttributeLocator<?> styleAttr = element.getAttribute(Attribute.STYLE);
         assertTrue(selenium.getAttribute(styleAttr).contains(value), "Attribute style should contain \"" + value + "\"");
     }
 
     /**
      * A helper method for testing attribute "class". It sets "metamer-ftest-class" to the input field and checks that
      * it was changed on the page.
      * 
      * @param element
      *            locator of tested element
      * @param attribute
      *            name of the attribute that will be set (e.g. styleClass, headerClass, itemContentClass
      */
     protected void testStyleClass(ElementLocator<?> element, String attribute) {
         ElementLocator<?> classInput = pjq("input[id$=" + attribute + "Input]");
         final String value = "metamer-ftest-class";
 
         selenium.type(classInput, value);
         selenium.waitForPageToLoad();
 
         assertTrue(selenium.belongsClass(element, value), attribute + " does not work");
     }
 
     /**
     * A helper method for testing attribute "dir". It tries null, ltr and rtl.
      *
      * @param element
      *            locator of tested element
      */
     protected void testDir(ElementLocator<?> element) {
         JQueryLocator ltrInput = pjq("input[type=radio][name$=dirInput][value=ltr]");
         JQueryLocator rtlInput = pjq("input[type=radio][name$=dirInput][value=rtl]");
         AttributeLocator<?> dirAttribute = element.getAttribute(new Attribute("dir"));
 
         // dir = null
         assertFalse(selenium.isAttributePresent(dirAttribute), "Attribute dir should not be present.");
 
         // dir = ltr
         selenium.click(ltrInput);
         selenium.waitForPageToLoad();
         assertTrue(selenium.isAttributePresent(dirAttribute), "Attribute dir should be present.");
         String value = selenium.getAttribute(dirAttribute);
         assertEquals(value, "ltr", "Attribute dir");
 
         // dir = rtl
         selenium.click(rtlInput);
         selenium.waitForPageToLoad();
         assertTrue(selenium.isAttributePresent(dirAttribute), "Attribute dir should be present.");
         value = selenium.getAttribute(dirAttribute);
         assertEquals(value, "rtl", "Attribute dir");
     }
 
     /**
      * Hides header, footer and inputs for attributes.
      */
     protected void hideControls() {
         selenium.getEval(new JavaScript("window.hideControls()"));
     }
 
     /**
      * Shows header, footer and inputs for attributes.
      */
     protected void showControls() {
         selenium.getEval(new JavaScript("window.showControls()"));
     }
 
     /**
      * Verifies that only given phases were executed. It uses the list of phases in the header of the page.
      * @param phases phases that are expected to have been executed
      */
     protected void assertPhases(PhaseId... phases) {
         JQueryLocator phasesItems = jq("div#phasesPanel li");
         int count = selenium.getCount(phasesItems);
 
         String phase;
         int phaseNumber = 1;
 
         for (int i = 0; i < count; i++) {
             phase = selenium.getText(jq("div#phasesPanel li:eq(" + i + ")"));
             // check that it is really name of a phase
             if (!phase.startsWith("* ")) {
                 assertEquals(phase, new Phase(phases[phaseNumber - 1]).toString(), "Phase nr. " + phaseNumber);
                 phaseNumber++;
             }
         }
     }
 }
