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
 package org.richfaces.tests.metamer.ftest.richExtendedDataTable;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 
 import java.net.URL;
 
 import org.richfaces.tests.metamer.ftest.abstractions.DataTableFacetsTest;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.model.ExtendedDataTable;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestExtendedDataTableFacets extends DataTableFacetsTest {
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richExtendedDataTable/facets.xhtml");
     }
 
     @BeforeClass
     public void setupModel() {
         model = new ExtendedDataTable(pjq("div.rf-edt[id$=richEDT]"));
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RFPL-1193")
     public void testNoDataInstantChange() {
         super.testNoDataInstantChange();
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RFPL-1193")
     public void testNoDataEmpty() {
         super.testNoDataEmpty();
     }
 
     @Test
    @IssueTracking({ "https://issues.jboss.org/browse/RFPL-1193", "https://issues.jboss.org/browse/RF-10627" })
     public void testNoDataLabelWithEmptyNoDataFacet() {
         super.testNoDataLabelWithEmptyNoDataFacet();
     }
 
     @Test
     public void testHeaderInstantChange() {
         super.testHeaderInstantChange();
     }
 
     @Test
     public void testHeaderEmpty() {
         super.testHeaderEmpty();
     }
 
     @Test
     public void testStateHeaderInstantChange() {
         super.testStateHeaderInstantChange();
     }
 
     @Test
     public void testStateHeaderEmpty() {
         super.testStateHeaderEmpty();
     }
 
     @Test
     public void testStateFooterInstantChange() {
         super.testStateFooterInstantChange();
     }
 
     @Test
     public void testStateFooterEmpty() {
         super.testStateFooterEmpty();
     }
 
     @Test
     public void testCapitalHeaderInstantChange() {
         super.testCapitalHeaderInstantChange();
     }
 
     @Test
     public void testCapitalHeaderEmpty() {
         super.testCapitalHeaderEmpty();
     }
 
     @Test
     public void testCapitalFooterInstantChange() {
         super.testCapitalFooterInstantChange();
     }
 
     @Test
     public void testCapitalFooterEmpty() {
         super.testCapitalFooterEmpty();
     }
 }
