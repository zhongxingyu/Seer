 package org.qooxdoo.demo.widgetbrowser;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.oneandone.qxwebdriver.By;
 import org.oneandone.qxwebdriver.ui.Selectable;
 import org.oneandone.qxwebdriver.ui.Widget;
 import org.oneandone.qxwebdriver.ui.table.Table;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 
 public class TableIT extends Common {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		Common.setUpBeforeClass();
 		selectTab("Table");
 	}
 	
 	public Table table;
 	
 	@Before
 	public void setUp() {
 		table = (Table) tabPage.findWidget(By.qxh("*/qx.ui.table.Table"));
 	}
 
 	@Test
 	public void scrollToRow() {
 		// select rows by index
 		WebElement row = table.scrollToRow(23);		
 		WebElement firstCell = row.findElement(By.xpath("div[contains(@class, 'qooxdoo-table-cell')]"));
 		Assert.assertEquals("23", firstCell.getText());
 		
 		row = table.scrollToRow(3);
 		firstCell = row.findElement(By.xpath("div[contains(@class, 'qooxdoo-table-cell')]"));
 		Assert.assertEquals("3", firstCell.getText());
 	}
 
 	@Test
 	public void getCellByText() {
 		// ctrl-click two rows and verify the selection ranges
 		Actions builder = new Actions(driver.getWebDriver());
 		builder.keyDown(Keys.CONTROL)
 			.click(table.getCellByText("26"))
 			.click(table.getCellByText("32"))
 			.keyUp(Keys.CONTROL)
 			.perform();
 		
 		List<HashMap> selectedRanges = table.getSelectedRanges();
 		Assert.assertEquals(2, selectedRanges.size());
 		
 		HashMap<String, Long> range0 = selectedRanges.get(0);
 		Assert.assertEquals(26, (int) (long) range0.get("minIndex"));
 		Assert.assertEquals(26, (int) (long) range0.get("maxIndex"));
 		
 		HashMap<String, Long> range1 = selectedRanges.get(1);
 		Assert.assertEquals(32, (int) (long) range1.get("minIndex"));
 		Assert.assertEquals(32, (int) (long) range1.get("maxIndex"));
 	}
 	
 	@Test
 	public void editCell() {
 		String cellXpath = "div[contains(@class, 'qooxdoo-table-cell') and position() = 3]";
 		String newText = "Hello WebDriver!";
 		
 		WebElement row = table.scrollToRow(12);
 		WebElement dateCell = row.findElement(By.xpath(cellXpath));
 		Actions builder = new Actions(driver.getWebDriver());
 		builder.doubleClick(dateCell).perform();
 		WebElement editor = table.getCellEditor();
 		editor.sendKeys(newText);
 		editor.sendKeys(Keys.RETURN);
 		
 		row = table.scrollToRow(12);
 		dateCell = row.findElement(By.xpath(cellXpath));
 		Assert.assertEquals(newText,  dateCell.getText());
 	}
 
 	@Test
 	public void columnMenu() {
 		// use the column menu to hide a column
 		List<String> headerLabels = table.getHeaderLabels();
 		Assert.assertArrayEquals(new String[] { "ID", "A number", "A date", "Boolean" },
 				headerLabels.toArray(new String[headerLabels.size()]));
 		
 		Selectable colMenuButton = (Selectable) table.getColumnMenuButton();
 		colMenuButton.selectItem("A number");
 		
 		headerLabels = table.getHeaderLabels();
 		Assert.assertArrayEquals(new String[] { "ID", "A date", "Boolean" },
 				headerLabels.toArray(new String[headerLabels.size()]));
 		colMenuButton.selectItem("A number");
 	}
 
 	@Test
 	public void sortByColumn() {
 		// click column headers to set the table's sorting order
 		Widget idColumnHeader = table.getHeaderCell("ID");
 		String sortIcon = (String) idColumnHeader.getPropertyValue("sortIcon");
 		Assert.assertNull(sortIcon);
 		idColumnHeader.click();
 		sortIcon = (String) idColumnHeader.getPropertyValue("sortIcon");
 		Assert.assertTrue(sortIcon.contains("ascending"));
 		idColumnHeader.click();
 		sortIcon = (String) idColumnHeader.getPropertyValue("sortIcon");
 		Assert.assertTrue(sortIcon.contains("descending"));
 		// back to default sorting
 		idColumnHeader.click();
 	}
 	
 }
