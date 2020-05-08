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
 package org.richfaces.tests.metamer.ftest.a4jRepeat;
 
 import static java.lang.Math.max;
 import static java.lang.Math.min;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 
 import java.net.URL;
 
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestSimple extends AbstractMetamerTest {
 
     protected static final int ELEMENTS_TOTAL = 20;
 
     SimpleModel model;
     RepeatAttributes attributes = new RepeatAttributes();
 
     @Inject
     @Use(empty = false)
     Integer first;
 
     @Inject
     @Use(empty = false)
     Integer rows;
 
     int expectedFirst;
     int expectedRows;
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/a4jRepeat/simple.xhtml");
     }
 
     @BeforeMethod(alwaysRun = true)
     public void prepareAttributes() {
         model = new SimpleModel();
 
         if (first != null) {
             attributes.setFirst(first);
         }
         if (rows != null) {
             attributes.setRows(rows);
         }
     }
 
     @Test
     public void testRenderedAttribute() {
         attributes.setRendered(false);
         assertEquals(model.isRendered(), false);
     }
 
     @Test
     @Use(field = "first", ints = { -2, -1, 0, 1, ELEMENTS_TOTAL / 2, ELEMENTS_TOTAL - 1, ELEMENTS_TOTAL,
         ELEMENTS_TOTAL + 1 })
     @IssueTracking({ "https://jira.jboss.org/browse/RF-9372" })
     public void testFirstAttribute() {
         verifyRepeat();
     }
 
     @Test
     @Use(field = "rows", ints = { -2, -1, 0, 1, ELEMENTS_TOTAL / 2, ELEMENTS_TOTAL - 1, ELEMENTS_TOTAL,
         ELEMENTS_TOTAL + 1 })
     @IssueTracking({ "https://jira.jboss.org/browse/RF-9373" })
     public void testRowsAttribute() {
         verifyRepeat();
     }
 
     private void verifyRepeat() {
         countExpectedValues();
         verifyCounts();
         verifyRows();
     }
 
     private void verifyCounts() {
         assertEquals(model.getTotalRowCount(), expectedRows);
         if (expectedRows > 0) {
             assertEquals(model.getIndex(1), expectedFirst);
         }
     }
 
     private void verifyRows() {
         int rowCount = model.getTotalRowCount();
         for (int position = 1; position <= rowCount; position++) {
             assertEquals(model.getBegin(position), expectedFirst, "begin");
             assertEquals(model.getEnd(position), expectedFirst + expectedRows - 1, "end");
             assertEquals(model.getIndex(position), expectedFirst + position - 1, "index");
             assertEquals(model.getCount(position), position, "count");
             assertEquals(model.isFirst(position), position == 1, "first");
             assertEquals(model.isLast(position), position == rowCount, "last");
             assertEquals(model.isEven(position), (position % 2) == 0, "even");
             // TODO fails because of rowCount on page doesn't eqaul to rowCount, but ELEMENTS_TOTAL
             // assertEquals(model.getRowCount(position), rowCount, "rowCount");
         }
     }
 
     private void countExpectedValues() {
         if (first == null || first < 0) {
             expectedFirst = 0;
         } else {
             expectedFirst = first;
         }
 
         if (rows == null || rows < 1 || rows > ELEMENTS_TOTAL) {
             expectedRows = ELEMENTS_TOTAL;
         } else {
             expectedRows = rows;
         }
 
         expectedFirst = minMax(0, expectedFirst, ELEMENTS_TOTAL);
         expectedRows = min(expectedRows, ELEMENTS_TOTAL - expectedFirst);
     }
 
     private int minMax(int min, int value, int max) {
         return max(0, min(max, value));
     }
 }
