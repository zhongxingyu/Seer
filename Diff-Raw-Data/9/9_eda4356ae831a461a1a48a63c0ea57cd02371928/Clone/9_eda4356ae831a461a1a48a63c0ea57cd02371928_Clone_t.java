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
 package org.sonar.duplications.algorithm;
 
 public class Clone {
   private String firstResourceId;
   private String secondResourceId;
 
   private int firstUnitStart;
   private int secondUnitStart;
 
   private int firstLineStart;
   private int secondLineStart;
 
   private int firstLineEnd;
   private int secondLineEnd;
 
   //clone length in units (not lines)
   private int cloneLength;
 
   public Clone() {
   }
 
   public Clone(String firstFile, int firstUnitStart, int firstLineStart, int firstLineEnd,
                String secondFile, int secondUnitStart, int secondLineStart, int secondLineEnd,
                int cloneLength) {
     this.firstResourceId = firstFile;
     this.firstUnitStart = firstUnitStart;
     this.firstLineStart = firstLineStart;
     this.firstLineEnd = firstLineEnd;
     this.secondResourceId = secondFile;
     this.secondUnitStart = secondUnitStart;
     this.secondLineStart = secondLineStart;
     this.secondLineEnd = secondLineEnd;
     this.cloneLength = cloneLength;
   }
 
   public String getFirstResourceId() {
     return firstResourceId;
   }
 
   public void setFirstResourceId(String firstResourceId) {
     this.firstResourceId = firstResourceId;
   }
 
   public String getSecondResourceId() {
     return secondResourceId;
   }
 
   public void setSecondResourceId(String secondResourceId) {
     this.secondResourceId = secondResourceId;
   }
 
   public int getFirstUnitStart() {
     return firstUnitStart;
   }
 
   public void setFirstUnitStart(int firstUnitStart) {
     this.firstUnitStart = firstUnitStart;
   }
 
   public int getSecondUnitStart() {
     return secondUnitStart;
   }
 
   public void setSecondUnitStart(int secondUnitStart) {
     this.secondUnitStart = secondUnitStart;
   }
 
   public int getFirstLineStart() {
     return firstLineStart;
   }
 
   public void setFirstLineStart(int firstLineStart) {
     this.firstLineStart = firstLineStart;
   }
 
   public int getSecondLineStart() {
     return secondLineStart;
   }
 
   public void setSecondLineStart(int secondLineStart) {
     this.secondLineStart = secondLineStart;
   }
 
   public int getFirstLineEnd() {
     return firstLineEnd;
   }
 
   public void setFirstLineEnd(int firstLineEnd) {
     this.firstLineEnd = firstLineEnd;
   }
 
   public int getSecondLineEnd() {
     return secondLineEnd;
   }
 
   public void setSecondLineEnd(int secondLineEnd) {
     this.secondLineEnd = secondLineEnd;
   }
 
   public int getCloneLength() {
     return cloneLength;
   }
 
   public void setCloneLength(int cloneLength) {
     this.cloneLength = cloneLength;
   }
 
   @Override
   public boolean equals(Object object) {
     if (object instanceof Clone) {
       Clone another = (Clone) object;
       return another.firstResourceId.equals(firstResourceId)
           && another.firstUnitStart == firstUnitStart
           && another.firstLineStart == firstLineStart
           && another.firstLineEnd == firstLineEnd
           && another.secondResourceId.equals(secondResourceId)
           && another.secondUnitStart == secondUnitStart
           && another.secondLineStart == secondLineStart
           && another.secondLineEnd == secondLineEnd
           && another.cloneLength == cloneLength;
     }
     return false;
   }
 
   @Override
   public int hashCode() {
     return firstResourceId.hashCode() + secondResourceId.hashCode() +
         firstUnitStart + secondUnitStart + firstLineStart +
        firstLineEnd + secondLineStart + secondLineEnd + 413 * cloneLength;
   }
 
   @Override
   public String toString() {
     return "'" + firstResourceId + "':[" + firstUnitStart + "|" +
         firstLineStart + "-" + firstLineEnd + "] - '" +
         secondResourceId + "':[" + secondUnitStart + "|" +
         secondLineStart + "-" + secondLineEnd + "] " + cloneLength;
   }
 }
