 /*
  * Sonar, open source software quality management tool.
  * Written (W) 2011 Andrew Tereskin
  * Copyright (C) 2008-2011 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.duplications.index;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class Clone {
 
   private final List<ClonePart> parts = new ArrayList<ClonePart>();
 
   private ClonePart originPart;
 
   private int cloneUnitLength;
 
   private int hash;
 
   public Clone(String resourceId1, int unitIndex1, int lineStart1, int lineEnd1,
                String resourceId2, int unitIndex2, int lineStart2, int lineEnd2,
                int cloneUnitLength) {
     addPart(new ClonePart(resourceId1, unitIndex1, lineStart1, lineEnd1));
     addPart(new ClonePart(resourceId2, unitIndex2, lineStart2, lineEnd2));
 
     this.cloneUnitLength = cloneUnitLength;
   }
 
   public Clone(int cloneUnitLength) {
     this.cloneUnitLength = cloneUnitLength;
   }
 
   public void setOriginPart(ClonePart originPart) {
     this.originPart = originPart;
   }
 
   public ClonePart getOriginPart() {
     return originPart;
   }
 
   public Clone addPart(ClonePart part) {
     parts.add(part);
     Collections.sort(parts, null);
     return this;
   }
 
   public List<ClonePart> getCloneParts() {
     return Collections.unmodifiableList(parts);
   }
 
   /**
    * @return clone length measured in units (not in lines)
    */
   public int getCloneUnitLength() {
     return cloneUnitLength;
   }
 
   /**
    * @param cloneUnitLength clone length measured in units (not in lines)
    */
   public void setCloneUnitLength(int cloneUnitLength) {
     this.cloneUnitLength = cloneUnitLength;
   }
 
   /**
    * Checks if first <tt>Clone</tt> is contained in second <tt>Clone</tt>. Clone A is contained in another
    * Clone B if every ClonePart pA from A has ClonePart pB in B which satisfy the conditions
    * pA.resourceId == pB.resourceId and pA.unitStart >= pB.unitStart and pA.unitEnd <= pb.unitEnd
    *
    * @param other Clone where current Clone should be contained
    * @return
    */
   public boolean containsIn(Clone other) {
     if (!getOriginPart().getResourceId().equals(other.getOriginPart().getResourceId())) {
       return false;
     }
     for (int i = 0; i < getCloneParts().size(); i++) {
       ClonePart firstPart = getCloneParts().get(i);
       int firstUnitEnd = firstPart.getUnitStart() + getCloneUnitLength();
       boolean found = false;
 
       for (int j = 0; j < other.getCloneParts().size(); j++) {
         ClonePart secondPart = other.getCloneParts().get(j);
         int secondUnitEnd = secondPart.getUnitStart() + other.getCloneUnitLength();
         if (firstPart.getResourceId().equals(secondPart.getResourceId()) &&
             firstPart.getUnitStart() >= secondPart.getUnitStart() &&
             firstUnitEnd <= secondUnitEnd) {
           found = true;
           break;
         }
       }
       if (!found) {
         return false;
       }
     }
     return true;
   }
 
   @Override
   public boolean equals(Object object) {
     if (object instanceof Clone) {
       Clone another = (Clone) object;
 
       if (another.cloneUnitLength != cloneUnitLength
           || parts.size() != another.parts.size())
         return false;
 
       boolean result = true;
       for (int i = 0; i < parts.size(); i++) {
         result &= another.parts.get(i).equals(parts.get(i));
       }
 
       result &= another.originPart.equals(originPart);
 
       return result;
     }
     return false;
   }
 
   @Override
   public int hashCode() {
     int h = hash;
     if (h == 0 && cloneUnitLength != 0) {
       for (ClonePart part : parts) {
         h = 31 * h + part.hashCode();
       }
       h = 31 * h + originPart.hashCode();
       h = 31 * h + cloneUnitLength;
       hash = h;
     }
     return h;
   }
 
   @Override
   public String toString() {
    StringBuilder builder = new StringBuilder();
     for (ClonePart part : parts) {
      builder.append(part).append(" - ");
     }
    builder.append(cloneUnitLength);
    return builder.toString();
   }
 }
