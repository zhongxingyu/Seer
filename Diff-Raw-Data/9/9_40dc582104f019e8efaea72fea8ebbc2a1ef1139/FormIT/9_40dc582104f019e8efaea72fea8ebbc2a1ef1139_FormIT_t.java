 package org.oneandone.qxwebdriver.widgetbrowser;
 
 import static org.junit.Assert.*;
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.oneandone.qxwebdriver.By;
 import org.oneandone.qxwebdriver.ui.Selectable;
 import org.oneandone.qxwebdriver.ui.Widget;
 
 public class FormIT extends Common {
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		Common.setUpBeforeClass();
 		selectTab("Form");
 	}
 	
 	@Test
 	public void textField() {
 		String text = "Hello TextField";
 		By textFieldLocator = By.qxh("*/qx.ui.form.TextField");
 		Widget textField = tabPage.findWidget(textFieldLocator);
 		textField.sendKeys(text);
 		String value = (String) textField.getPropertyValue("value");
 		assertEquals(text, value);
 	}
 	
 	@Test
 	public void passwordField() {
 		String text = "Hello PasswordField";
 		By passwordFieldLocator = By.qxh("*/qx.ui.form.PasswordField");
 		Widget passwordField = tabPage.findWidget(passwordFieldLocator);
 		passwordField.sendKeys(text);
 		String value = (String) passwordField.getPropertyValue("value");
 		assertEquals(text, value);
 	}
 	
 	@Test
 	public void textArea() {
 		String text = "Hello\nTextArea";
 		By textAreaLocator = By.qxh("*/qx.ui.form.TextArea");
 		Widget textArea = tabPage.findWidget(textAreaLocator);
 		textArea.sendKeys(text);
 		String value = (String) textArea.getPropertyValue("value");
		
		Pattern regex = Pattern.compile("Hello.*?TextArea", Pattern.DOTALL);
		Matcher regexMatcher = regex.matcher(value);
		assertTrue(regexMatcher.matches());
 	}
 	
 	@Test
 	public void comboBox() {
 		String item = "Item 4";
 		By comboBoxLocator = By.qxh("*/qx.ui.form.ComboBox");
 		// The selectItem clicks the ComboBox, then clicks an item in the popup list 
 		Selectable comboBox = (Selectable) tabPage.findWidget(comboBoxLocator);
 		comboBox.selectItem(item);
 		String value = (String) comboBox.getPropertyValue("value");
 		assertEquals(item, value);
 		
 		String text = "Hello ComboBox";
 		// clear is delegated to the TextField child control when using the
 		// Selectable interface
 		comboBox.clear();
 		comboBox.sendKeys(text);
 		value = (String) comboBox.getPropertyValue("value");
 		assertEquals(text, value);
 	}
 	
 	@Test
 	public void virtualComboBox() throws InterruptedException {
 		String item = "Item 14";
 		By comboBoxLocator = By.qxh("*/qx.ui.form.VirtualComboBox");
 		// The selectItem clicks the ComboBox, then clicks an item in the popup list 
 		Selectable comboBox = (Selectable) tabPage.findWidget(comboBoxLocator);
 		comboBox.selectItem(item);
 		String value = (String) comboBox.getPropertyValue("value");
 		assertEquals(item, value);
 		
 		String text = "Hello VirtualComboBox";
 		// clear is delegated to the TextField child control when using the
 		// Selectable interface
 		comboBox.clear();
 		comboBox.sendKeys(text);
 		// The value won't be updated until the box loses focus
 		tabPage.click();
 		String typedValue = (String) comboBox.getPropertyValue("value");
 		assertEquals(text, typedValue);
 	}
 	
 	@Test
 	public void dateField() {
 		Widget dateField = tabPage.findWidget(By.qxh("*/qx.ui.form.DateField"));
 		// No Selectable implementation for DateField yet, so we use the childControls directly
 		dateField.getChildControl("button").click();
 		Widget list = dateField.waitForChildControl("list", 2);
 		list.getChildControl("last-year-button").click();
 		list.getChildControl("next-month-button").click();
 		list.findWidget(By.qxh("*/[@value=^12$]")).click();
 		
 		String value = (String) dateField.getPropertyValueAsJson("value");
 		assertTrue(value.matches("\"?\\d{4}-\\d{2}-\\d{2}.*"));
 	}
 	
 	@Test
 	public void selectBox() {
 		String item = "Item 4";
 		Selectable selectBox = (Selectable) tabPage.findWidget(By.qxh("*/qx.ui.form.SelectBox"));
 		selectBox.selectItem(item);
 		java.util.List<Widget> selection = selectBox.getWidgetListFromProperty("selection");
 		assertEquals(1, selection.size());
 		Widget selected = selection.get(0);
 		String selectedLabel = (String) selected.getPropertyValue("label");
 		assertEquals(item, selectedLabel);
 	}
 	
 	@Test
 	public void virtualSelectBox() {
 		String item = "Item 19";
 		Selectable vSelectBox = (Selectable) tabPage.findWidget(By.qxh("*/qx.ui.form.VirtualSelectBox"));
 		vSelectBox.selectItem(item);
 		java.util.List<String> selection = (java.util.List<String>) vSelectBox.getPropertyValue("selection");
 		assertEquals(1, selection.size());
 		String selected = selection.get(0);
 		assertEquals(item, selected);
 	}
 	
 	@Test
 	public void list() {
 		String item = "Item 5";
 		Selectable list = (Selectable) tabPage.findWidget(By.qxh("*/qx.ui.form.List"));
 		list.selectItem(item);
 		java.util.List<Widget> selection = list.getWidgetListFromProperty("selection");
 		assertEquals(1, selection.size());
 		Widget selected = selection.get(0);
 		String selectedLabel = (String) selected.getPropertyValue("label");
 		assertEquals(item, selectedLabel);
 	}
 	
 	@Test
 	public void radioButtonGroup() {
 		By by = By.qxh("*/[@label=RadioButton 2]");
 		Widget radioButton = tabPage.findWidget(by);
 		radioButton.click();
 		assertTrue(radioButton.isSelected());
 	}
 	
 	@Test
 	public void buttons() {
 		Widget toggleButton = tabPage.findWidget(By.qxh("*/qx.ui.form.ToggleButton"));
 		assertFalse(toggleButton.isSelected());
 		toggleButton.click();
 		assertTrue(toggleButton.isSelected());
 		
 		Selectable menuButton = (Selectable) tabPage.findWidget(By.qxh("*/qx.ui.form.MenuButton"));
 		menuButton.selectItem("Button2");
 	}
 }
