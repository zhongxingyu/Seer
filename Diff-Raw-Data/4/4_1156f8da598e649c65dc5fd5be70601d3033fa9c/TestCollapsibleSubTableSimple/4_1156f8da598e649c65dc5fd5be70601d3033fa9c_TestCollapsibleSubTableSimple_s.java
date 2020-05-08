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
 package org.richfaces.tests.metamer.ftest.richCollapsibleSubTable;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guard;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.net.URL;
 import java.util.List;
 
 import org.jboss.test.selenium.request.RequestType;
 import org.richfaces.ExpandMode;
import org.richfaces.component.UICollapsibleSubTable;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.richfaces.tests.metamer.model.Employee;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestCollapsibleSubTableSimple extends AbstractCollapsibleSubTableTest {
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richCollapsibleSubTable/simple.xhtml");
     }
 
     @Test
     @Use(field = "expandMode", enumeration = true)
     @IssueTracking("https://issues.jboss.org/browse/RF-10181")
     public void testExpandMode() {
         final RequestType requestType = getRequestTypeForExpandMode();
 
         attributes.setExpandMode(expandMode);
 
         assertTrue(subtable.hasVisibleRows());
         assertTrue(secondSubtable.hasVisibleRows());
 
         if (expandMode == ExpandMode.none) {
             guard(selenium, requestType).click(toggler);
             assertEquals(subtable.hasVisibleRows(), ExpandMode.none == expandMode);
             assertTrue(secondSubtable.hasVisibleRows());
         } else {
             for (int i = 0; i < 2; i++) {
                 guard(selenium, requestType).click(toggler);
                 assertFalse(subtable.hasVisibleRows());
                 assertTrue(secondSubtable.hasVisibleRows());
 
                 guard(selenium, requestType).click(toggler);
                 assertTrue(subtable.hasVisibleRows());
                 assertTrue(secondSubtable.hasVisibleRows());
             }
         }
     }
 
     @Test
     @Use(field = "configuration", empty = true)
     public void testExpanded() {
         attributes.setExpanded(false);
 
         assertFalse(configurationMen.subtable.hasVisibleRows());
         assertFalse(configurationWomen.subtable.hasVisibleRows());
 
         selenium.click(configurationMen.toggler);
         assertTrue(configurationMen.subtable.hasVisibleRows());
 
         attributes.setExpanded(true);
 
         assertTrue(configurationMen.subtable.hasVisibleRows());
         assertTrue(configurationWomen.subtable.hasVisibleRows());

        new UICollapsibleSubTable().getCaptionClass();
     }
 
     @Test
     public void testFirst() {
         attributes.setFirst(2);
 
         List<Employee> visibleEmployees = employees.subList(2, subtable.getRowCount());
 
         for (int i = 0; i < visibleEmployees.size(); i++) {
             String name = selenium.getText(subtable.getCell(1, i + 1));
             String title = selenium.getText(subtable.getCell(2, i + 1));
 
             assertEquals(name, visibleEmployees.get(i).getName());
             assertEquals(title, visibleEmployees.get(i).getTitle());
         }
     }
 
     @Test
     @Use(field = "configuration", empty = true)
     public void testRendered() {
         attributes.setRendered(false);
 
         assertFalse(configurationMen.subtable.hasVisibleRows());
         assertFalse(configurationWomen.subtable.hasVisibleRows());
 
         selenium.click(configurationMen.toggler);
         assertFalse(configurationMen.subtable.hasVisibleRows());
 
         attributes.setRendered(true);
 
         assertTrue(configurationMen.subtable.hasVisibleRows());
         assertTrue(configurationWomen.subtable.hasVisibleRows());
     }
 
     @Test
     public void testRows() {
         attributes.setRows(11);
 
         List<Employee> visibleEmployees = employees.subList(0, 11);
 
         assertEquals(subtable.getRowCount(), 11);
 
         for (int i = 0; i < visibleEmployees.size(); i++) {
             String name = selenium.getText(subtable.getCell(1, i + 1));
             String title = selenium.getText(subtable.getCell(2, i + 1));
 
             assertEquals(name, visibleEmployees.get(i).getName());
             assertEquals(title, visibleEmployees.get(i).getTitle());
         }
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10217")
     @Use(field = "configuration", empty = true)
     public void testClasses() {
         // TODO classes are currently not working
         fail();
     }
 }
