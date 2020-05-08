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
 package org.richfaces.tests.metamer.ftest.model;
 
 import static org.richfaces.tests.metamer.ftest.AbstractMetamerTest.pjq;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.javascript.JQueryScript.jqScript;
 import static org.jboss.test.selenium.javascript.JQueryScript.jqObject;
 import static org.jboss.test.selenium.waiting.WaitFactory.*;
 
 import java.awt.event.KeyEvent;
 
 import org.jboss.test.selenium.RequestTypeModelGuard.Model;
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.framework.AjaxSelenium;
 import org.jboss.test.selenium.framework.AjaxSeleniumProxy;
 import org.jboss.test.selenium.locator.JQueryLocator;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class Autocomplete implements Model {
 
 	private static String KEY_ENTER = "13";
 	private static String KEY_UP = "38";
 	private static String KEY_DOWN = "40";
 
 	AjaxSelenium selenium = AjaxSeleniumProxy.getInstance();
 
 	JQueryLocator input = pjq("input.rf-au-inp[id$=autocompleteInput]");
 
 	JQueryLocator items = jq("div.rf-au-lst-cord[id$=autocompleteList] div[id$=autocompleteItems]");
	JQueryLocator selection = items.getDescendant(jq("div.rf-au-itm-sel"));
	JQueryLocator option = jq("div.rf-au-itm");
 	JQueryLocator labeledOption = jq("div.rf-au-opt:contains('{0}')");
 
 	public void typeKeys(String keys) {
 		for (int i = 0; i < keys.length(); i++) {
 			final String key = String.valueOf(keys.charAt(i));
 			selenium.focus(input);
 			selenium.keyPress(input, key);
 			selenium.fireEvent(input, Event.KEYDOWN);
 		}
 	}
 
 	public void confirmByKeys() {
 		pressEnter();
 	}
 
 	public void selectByKeys(String label) {
 		int labeledIndex = getLabeledOptionIndex(label);
 		while (getSelectedOptionIndex() < labeledIndex) {
 			pressDown();
 		}
 		while (getSelectedOptionIndex() > labeledIndex) {
 			pressUp();
 		}
 	}
 
 	public boolean isLabeledOptionAvailable(String label) {
 		return selenium.isElementPresent(getLabeledOption(label));
 	}
 
 	public int getLabeledOptionIndex(String label) {
 		String index = selenium.getEval(jqScript(getLabeledOption(label),
 				"index()"));
 		return Integer.valueOf(index);
 	}
 
 	public int getSelectedOptionIndex() {
 		JavaScript script = jqScript(option, "index({0})").parametrize(
 				jqObject(selection));
 		String index = selenium.getEval(script);
 		return Integer.valueOf(index);
 	}
 
 	public String getSelectedOptionText() {
 		return selenium.getText(selection);
 	}
 
 	public String getInputText() {
 		return selenium.getValue(input);
 	}
 
 	private JQueryLocator getLabeledOption(String label) {
 		return labeledOption.format(label);
 	}
 
 	public void pressBackspace() {
 		selenium.keyPressNative(String.valueOf(KeyEvent.VK_BACK_SPACE));
 	}
 
 	public void pressUp() {
 		selenium.keyDown(input, KEY_UP);
 	}
 
 	public void pressDown() {
 		selenium.keyDown(input, KEY_DOWN);
 	}
 
 	public void pressEnter() {
 		selenium.keyDown(input, KEY_ENTER);
 	}
 
 	public void pressLeft() {
 		selenium.keyPressNative(String.valueOf(KeyEvent.VK_LEFT));
 	}
 
 	public void pressRight() {
 		selenium.keyPressNative(String.valueOf(KeyEvent.VK_RIGHT));
 	}
 
 	public void pressDelete() {
 		selenium.keyPressNative(String.valueOf(KeyEvent.VK_DELETE));
 	}
 
 	public void textSelectionLeft(int size) {
 		selenium.keyDownNative(String.valueOf(KeyEvent.VK_SHIFT));
 		for (int i = 0; i < size; i++) {
 			selenium.keyPressNative(String.valueOf(KeyEvent.VK_LEFT));
 		}
 		selenium.keyUpNative(String.valueOf(KeyEvent.VK_SHIFT));
 	}
 
 	public void textSelectionRight(int size) {
 		selenium.keyDownNative(String.valueOf(KeyEvent.VK_SHIFT));
 		for (int i = 0; i < size; i++) {
 			selenium.keyPressNative(String.valueOf(KeyEvent.VK_RIGHT));
 		}
 		selenium.keyUpNative(String.valueOf(KeyEvent.VK_SHIFT));
 	}
 
 	public void textSelectAll() {
 		selenium.keyDownNative(String.valueOf(KeyEvent.VK_CONTROL));
 		selenium.keyPressNative(String.valueOf(KeyEvent.VK_A));
 		selenium.keyDownNative(String.valueOf(KeyEvent.VK_CONTROL));
 	}
 
 	public boolean isCompletionVisible() {
 		if (!selenium.isElementPresent(option)) {
 			return false;
 		}
 		return selenium.isVisible(option);
 	}
 
 	public void waitForCompletionVisible() {
 		waitGui.until(elementPresent.locator(option));
 	}
 }
