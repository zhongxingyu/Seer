 package org.richfaces.tests.metamer.ftest.model;
 
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.locator.reference.ReferencedLocator.ref;
 
 import org.jboss.test.selenium.framework.AjaxSelenium;
 import org.jboss.test.selenium.framework.AjaxSeleniumProxy;
 import org.jboss.test.selenium.locator.ExtendedLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.jboss.test.selenium.locator.reference.ReferencedLocator;
 
 public class CollapsibleSubTable extends AbstractModel<JQueryLocator> {
 
     AjaxSelenium selenium = AjaxSeleniumProxy.getInstance();
 
     JQueryLocator subtableRow = jq("tr[class*=rf-cst-][class*=-r]");
     JQueryLocator subtableCell = jq("td.rf-cst-c");
 
     ReferencedLocator<JQueryLocator> noData = ref(root, "> tr.rf-cst-nd > td.rf-cst-nd-c");
     ReferencedLocator<JQueryLocator> header = ref(root, "> tr.rf-cst-hdr > td.rf-cst-hdr-c");
     ReferencedLocator<JQueryLocator> footer = ref(root, "> tr.rf-cst-ftr > td.rf-cst-ftr-c");
 
     JQueryLocator visible = jq("{0}:visible");
 
     public CollapsibleSubTable(JQueryLocator root) {
         super(root);
     }
     
     public JQueryLocator getAnyRow() {
         return root.getLocator().getChild(subtableRow);
     }
     
     public JQueryLocator getAnyCellInColumn(int column) {
         return root.getLocator().getChild(subtableRow).getChild(subtableCell).getNthChildElement(column);
     }
 
     public Iterable<JQueryLocator> getRows() {
         return root.getLocator().getChildren(subtableRow);
     }
 
     public JQueryLocator getRow(int rowIndex) {
         return root.getLocator().getChild(subtableRow).getNthOccurence(rowIndex);
     }
 
     public int getRowCount() {
         return selenium.getCount(root.getLocator().getChild(subtableRow));
     }
 
     public boolean hasVisibleRows() {
         JQueryLocator locator = root.getLocator().getChild(subtableRow);
         locator = visible.format(locator.getRawLocator());
         return selenium.isElementPresent(locator);
     }
 
     public JQueryLocator getCell(int column, int row) {
         return getRow(row).getChild(subtableCell).getNthOccurence(column);
     }
 
     public ExtendedLocator<JQueryLocator> getNoData() {
         return noData;
     }
 
     public boolean isNoData() {
        return selenium.isElementPresent(noData) && selenium.isVisible(noData);
     }
     
     public boolean isVisible() {
         return selenium.isElementPresent(root.getLocator()) && selenium.isVisible(root.getLocator());
     }
 
     public ExtendedLocator<JQueryLocator> getHeader() {
         return header;
     }
 
     public ExtendedLocator<JQueryLocator> getFooter() {
         return footer;
     }
 }
