 /**
  * 
  *    This module represents an engine for the load testing framework
  *    Copyright (C) 2008  Imran M Yousuf (imran@smartitengineering.com)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.smartitengineering.loadtest.engine.result;
 
 import com.smartitengineering.domain.PersistentDTO;
 import com.smartitengineering.loadtest.engine.TestCase;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 
 /**
  *
  * @author imyousuf
  */
 @XmlAccessorType(XmlAccessType.PROPERTY)
 public class TestCaseInstanceResult
     extends PersistentDTO<TestCaseInstanceResult> {
 
     private Date startTime;
     private Date endTime;
     private int instanceNumber;
     private String endState;
     private int testCaseResultId;
     private Set<KeyedInformation> otherInfomations;
 
     public boolean isValid() {
        if (otherInfomations != null) {
            for (KeyedInformation keyedInformation : otherInfomations) {
                if (!keyedInformation.isValid()) {
                    return false;
                }
            }
        }
         if (startTime != null && endTime != null && endState != null &&
             instanceNumber > 0) {
             return true;
         }
         return false;
     }
 
     @Override
     public Object clone() {
         TestCaseInstanceResult instanceResult = new TestCaseInstanceResult();
         super.clone(instanceResult);
         instanceResult.setEndState(endState);
         if (endTime != null) {
             instanceResult.setEndTime(new Date(endTime.getTime()));
         }
         if (startTime != null) {
             instanceResult.setStartTime(new Date(startTime.getTime()));
         }
         instanceResult.setInstanceNumber(instanceNumber);
         if (otherInfomations != null) {
             Set<KeyedInformation> keyedInformations =
                 new HashSet<KeyedInformation>(otherInfomations.size());
             for (KeyedInformation keyedInformation : otherInfomations) {
                 keyedInformations.add(
                     (KeyedInformation) keyedInformation.clone());
             }
             instanceResult.setOtherInfomations(keyedInformations);
         }
         instanceResult.setTestCaseResultId(testCaseResultId);
         return instanceResult;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (super.equals(obj) && obj instanceof TestCaseInstanceResult) {
             TestCaseInstanceResult resultObj = (TestCaseInstanceResult) obj;
             if (testCaseResultId == resultObj.testCaseResultId &&
                 instanceNumber == resultObj.instanceNumber) {
                 return true;
             }
         }
         return false;
     }
 
     protected String getEndState() {
         return endState;
     }
 
     protected void setEndState(String endState) {
         this.endState = endState;
     }
 
     public TestCase.State getEndTestCaseState() {
         return TestCase.State.valueOf(getEndState());
     }
 
     public void setEndTestCaseState(TestCase.State testCaseState) {
         if (testCaseState != null) {
             setEndState(testCaseState.name());
         }
     }
 
     public Date getEndTime() {
         return endTime;
     }
 
     public void setEndTime(Date endTime) {
         this.endTime = endTime;
     }
 
     public int getInstanceNumber() {
         return instanceNumber;
     }
 
     public void setInstanceNumber(int instanceNumber) {
         this.instanceNumber = instanceNumber;
     }
 
     public Date getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Date startTime) {
         this.startTime = startTime;
     }
 
     public Set<KeyedInformation> getOtherInfomations() {
         return otherInfomations;
     }
 
     public void setOtherInfomations(Set<KeyedInformation> otherInfomations) {
         this.otherInfomations = otherInfomations;
     }
 
     public int getTestCaseResultId() {
         return testCaseResultId;
     }
 
     public void setTestCaseResultId(int testCaseResultId) {
         this.testCaseResultId = testCaseResultId;
     }
 }
