 package org.firepick.firebom.part;
 /*
     Copyright (C) 2013 Karl Lew <karl@firepick.org>. All rights reserved.
     DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
     
     This file is part of FirePick Software.
     
     FirePick Software is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     FirePick Software is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with FirePick Software.  If not, see <http://www.gnu.org/licenses/>.
     
     For more information about FirePick Software visit http://firepick.org
  */
 
 import junit.framework.Assert;
 
 import java.net.URL;
 import java.util.List;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.junit.Assert.assertEquals;
 
 public class PartTester {
     private URL url;
     private Part part;
 
     public PartTester(PartFactory partFactory, String url) throws Exception {
         this.url = new URL(url);
         part = partFactory.createPart(this.url);
         part.refreshAll();
         assert (part.isFresh());
     }
 
     public PartTester testId(String id) {
         assertEquals("part id", id, part.getId());
         return this;
     }
 
     public PartTester testVendor(String value) {
         Assert.assertEquals("vendor name", value, part.getVendor());
         return this;
     }
 
     public PartTester testUnitCost(double value) {
         assertEquals("part unit cost", value, part.getUnitCost(), .005d);
         return this;
     }
 
     public PartTester testPackageCost(double value, double tolerance) {
         assertEquals("part package cost", value, part.getPackageCost(), tolerance);
         return this;
     }
 
     public PartTester testSourceCost(double value) {
         assertEquals(value, part.getSourcePartUsage().getCost(), .005d);
         return this;
     }
 
    public PartTester testSourcePackageUnits(double value) {
        assertEquals("source package units", value, part.getSourcePartUsage().getQuantity(), .5d);
        return this;
    }

     public PartTester testPackageUnits(double value) {
         assertEquals("part package units", value, part.getPackageUnits(), 0.005d);
         return this;
     }
 
     public PartTester testProject(String value) {
         assertEquals("project name for part", value, part.getProject());
         return this;
     }
 
     public PartTester testTitle(String value) {
         assertEquals("part title", value, part.getTitle());
         return this;
     }
 
     public PartTester testRequiredParts(int value) {
         List<PartUsage> partUsages = part.getRequiredParts();
         assertNotNull(partUsages);
         assertEquals("number of required parts", value, partUsages.size());
         return this;
     }
 
     public PartTester testRequiredPart(int index, String partId, double quantity, double unitCost) {
         List<PartUsage> partUsages = part.getRequiredParts();
         PartUsage partUsage = partUsages.get(index);
         assertEquals(partId, partUsage.getPart().getId());
         assertEquals(partId + " quantity", quantity, partUsage.getQuantity(), 0);
         assertEquals(partId + " unit cost", unitCost, partUsage.getPart().getUnitCost(), 0.5d);
         return this;
     }
 
     public Part getPart() {
         return part;
     }
 
 }
