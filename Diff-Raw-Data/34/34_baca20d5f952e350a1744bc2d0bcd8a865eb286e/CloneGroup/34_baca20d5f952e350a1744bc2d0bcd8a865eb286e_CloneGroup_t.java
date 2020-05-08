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
 
 public class CloneGroup {
 
   private final List<ClonePart> parts;
 
   private ClonePart originPart;
 
   private int cloneUnitLength;
 
  private boolean sorted = true;

   /**
    * Cache for hash code.
    */
   private int hash;
 
   public CloneGroup() {
     this(new ArrayList<ClonePart>());
   }
 
   /**
    * TODO Godin: Hack - this code stores a reference to an externally mutable object into the internal representation of the object.
    * However allows creation with initial capacity and what's more important - avoid invocations of method {@link #addPart(ClonePart)}, which performs sorting.
    */
   public CloneGroup(List<ClonePart> parts) {
     this.parts = parts;
   }
 
   public CloneGroup(int cloneUnitLength) {
     this();
     this.cloneUnitLength = cloneUnitLength;
   }
 
   public void setOriginPart(ClonePart originPart) {
     this.originPart = originPart;
   }
 
   public ClonePart getOriginPart() {
     return originPart;
   }
 
   public CloneGroup addPart(ClonePart part) {
     parts.add(part);
    sorted = false;
     return this;
   }
 
   public List<ClonePart> getCloneParts() {
    if (!sorted) {
      Collections.sort(parts, null);
    }
     return Collections.unmodifiableList(parts);
   }
 
   /**
    * @return clone length measured in units (not in lines)
    */
   public int getCloneUnitLength() {
     return cloneUnitLength;
   }
 
   /**
   * @param cloneUnitLength clone length in units (not in lines)
    */
   public void setCloneUnitLength(int cloneUnitLength) {
     this.cloneUnitLength = cloneUnitLength;
   }
 
 
   public boolean containsIn(CloneGroup second) {
     if (!this.getOriginPart().getResourceId().equals(second.getOriginPart().getResourceId())) {
       return false;
     }
 
     for (ClonePart firstPart : this.getCloneParts()) {
       boolean found = false;
 
       for (ClonePart secondPart : second.getCloneParts()) {
         int firstUnitEnd = firstPart.getUnitStart() + this.getCloneUnitLength();
         int secondUnitEnd = secondPart.getUnitStart() + second.getCloneUnitLength();
 
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
     if (object instanceof CloneGroup) {
       CloneGroup another = (CloneGroup) object;
 
      if (another.cloneUnitLength != cloneUnitLength ||
          getCloneParts().size() != another.getCloneParts().size()) {
         return false;
       }
 
       boolean result = true;
      for (int i = 0; i < getCloneParts().size(); i++) {
        result &= another.getCloneParts().get(i).equals(getCloneParts().get(i));
       }
 
      result &= another.getOriginPart().equals(getOriginPart());
 
       return result;
     }
     return false;
   }
 
   @Override
   public int hashCode() {
     int h = hash;
     if (h == 0 && cloneUnitLength != 0) {
      for (ClonePart part : getCloneParts()) {
         h = 31 * h + part.hashCode();
       }
      h = 31 * h + getOriginPart().hashCode();
       h = 31 * h + cloneUnitLength;
       hash = h;
     }
     return h;
   }
 
   @Override
   public String toString() {
     StringBuilder builder = new StringBuilder();
    for (ClonePart part : getCloneParts()) {
       builder.append(part).append(" - ");
     }
     builder.append(cloneUnitLength);
     return builder.toString();
   }
 }
