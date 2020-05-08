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
 package org.richfaces.tests.metamer.ftest.abstractions;
 
 import static org.testng.Assert.assertEquals;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public abstract class DataTableFacetsTest extends AbstractDataTableTest {
 
     private static final String SAMPLE_STRING = "Abc123!@#ĚščСам";
     private static final String EMPTY_STRING = "";
 
     public void testNoDataInstantChange() {
         attributes.setShowData(false);
         facets.setNoData(SAMPLE_STRING);
         assertEquals(selenium.getText(model.getNoData()), SAMPLE_STRING);
     }
 
     public void testNoDataEmpty() {
         attributes.setShowData(false);
         facets.setNoData(EMPTY_STRING);
         assertEquals(selenium.getText(model.getNoData()), EMPTY_STRING);
     }
 
     public void testNoDataLabelWithEmptyNoDataFacet() {
         attributes.setShowData(false);
         facets.setNoData(EMPTY_STRING);
         attributes.setNoDataLabel(SAMPLE_STRING);
        assertEquals(selenium.getText(model.getNoData()), SAMPLE_STRING);
     }
 
     public void testHeaderInstantChange() {
         facets.setHeader(SAMPLE_STRING);
         assertEquals(selenium.getText(model.getHeader()), SAMPLE_STRING);
     }
 
     public void testHeaderEmpty() {
         facets.setHeader(EMPTY_STRING);
 
         if (selenium.isElementPresent(model.getHeader())) {
             assertEquals(selenium.getText(model.getHeader()), EMPTY_STRING);
         } else {
             facets.setHeader(SAMPLE_STRING);
             assertEquals(selenium.getText(model.getHeader()), SAMPLE_STRING);
         }
     }
 
     public void testStateHeaderInstantChange() {
         facets.setStateHeader(SAMPLE_STRING);
         assertEquals(selenium.getText(model.getColumnHeader(COLUMN_STATE)), SAMPLE_STRING);
     }
 
     public void testStateHeaderEmpty() {
         facets.setStateHeader(EMPTY_STRING);
         assertEquals(selenium.getText(model.getColumnHeader(COLUMN_STATE)), EMPTY_STRING);
     }
 
     public void testStateFooterInstantChange() {
         facets.setStateFooter(SAMPLE_STRING);
         assertEquals(selenium.getText(model.getColumnFooter(COLUMN_STATE)), SAMPLE_STRING);
     }
 
     public void testStateFooterEmpty() {
         facets.setStateFooter(EMPTY_STRING);
         assertEquals(selenium.getText(model.getColumnFooter(COLUMN_STATE)), EMPTY_STRING);
     }
 
     public void testCapitalHeaderInstantChange() {
         facets.setCapitalHeader(SAMPLE_STRING);
         assertEquals(selenium.getText(model.getColumnHeader(COLUMN_CAPITAL)), SAMPLE_STRING);
     }
 
     public void testCapitalHeaderEmpty() {
         facets.setCapitalHeader(EMPTY_STRING);
         assertEquals(selenium.getText(model.getColumnHeader(COLUMN_CAPITAL)), EMPTY_STRING);
     }
 
     public void testCapitalFooterInstantChange() {
         facets.setCapitalFooter(SAMPLE_STRING);
         assertEquals(selenium.getText(model.getColumnFooter(COLUMN_CAPITAL)), SAMPLE_STRING);
     }
 
     public void testCapitalFooterEmpty() {
         facets.setCapitalFooter(EMPTY_STRING);
         assertEquals(selenium.getText(model.getColumnFooter(COLUMN_CAPITAL)), EMPTY_STRING);
     }
 }
