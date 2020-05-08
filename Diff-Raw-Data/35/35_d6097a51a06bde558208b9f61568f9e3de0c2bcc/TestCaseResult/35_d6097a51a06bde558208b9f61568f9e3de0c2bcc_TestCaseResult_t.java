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
 import java.util.HashSet;
 import java.util.Set;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 
 /**
  *
  * @author imyousuf
  */
 @XmlAccessorType(XmlAccessType.PROPERTY)
 public class TestCaseResult
     extends PersistentDTO<TestCaseResult> {
 
     private String name;
     private String instanceFactoryClassName;
     private String stepDelayConfiguration;
     private int stepSize;
     private int stepCount;
     private int testResultId;
     private Set<TestProperty> testProperties;
     private Set<TestCaseInstanceResult> testCaseInstanceResults;
     private Set<KeyedInformation> otherInfomations;
 
     public boolean isValid() {
         if (name != null && instanceFactoryClassName != null && stepSize >= 1 &&
             stepCount >= 1 && testCaseInstanceResults != null &&
             testCaseInstanceResults.size() > 0) {
            for (TestCaseInstanceResult instanceResult : testCaseInstanceResults) {
                if (!instanceResult.isValid()) {
                    return false;
                }
                if (otherInfomations != null) {
                    for (KeyedInformation keyedInformation : otherInfomations) {
                        if (!keyedInformation.isValid()) {
                            return false;
                        }
                    }
                }
                if (testProperties != null) {
                    for (TestProperty testProperty : testProperties) {
                        if (!testProperty.isValid()) {
                            return false;
                        }
                    }
                }
            }
             return true;
         }
         return false;
     }
 
     @Override
     public Object clone() {
         TestCaseResult caseResult = new TestCaseResult();
         super.clone(caseResult);
         caseResult.setInstanceFactoryClassName(instanceFactoryClassName);
         caseResult.setName(name);
         if (otherInfomations != null) {
             Set<KeyedInformation> otherInfomationsClone =
                 new HashSet<KeyedInformation>(otherInfomations.size());
             for (KeyedInformation otherInformation : otherInfomations) {
                 final KeyedInformation otherInformationClone =
                     (KeyedInformation) otherInformation.clone();
                 otherInfomationsClone.add(otherInformationClone);
             }
             caseResult.setOtherInfomations(otherInfomationsClone);
         }
         if (testProperties != null) {
             Set<TestProperty> testPropertiesClone = new HashSet<TestProperty>(
                 testProperties.size());
             for (TestProperty testProperty : testProperties) {
                 final TestProperty testPropertyClone =
                     (TestProperty) testProperty.clone();
                 testPropertiesClone.add(testPropertyClone);
             }
             caseResult.setTestProperties(testPropertiesClone);
         }
         if (testCaseInstanceResults != null) {
             Set<TestCaseInstanceResult> testCaseInstanceResultsClone =
                 new HashSet<TestCaseInstanceResult>(
                 testCaseInstanceResults.size());
             for (TestCaseInstanceResult testCaseInstanceResult : testCaseInstanceResults) {
                 final TestCaseInstanceResult testCaseInstanceResultClone =
                     (TestCaseInstanceResult) testCaseInstanceResult.clone();
                 testCaseInstanceResultsClone.add(testCaseInstanceResultClone);
             }
             caseResult.setTestCaseInstanceResults(testCaseInstanceResultsClone);
         }
         caseResult.setTestResultId(testResultId);
        caseResult.setStepCount(stepCount);
        caseResult.setStepSize(stepSize);
        caseResult.setStepDelayConfiguration(stepDelayConfiguration);
         return caseResult;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (super.equals(obj) && obj instanceof TestCaseResult) {
             TestCaseResult resultObj = (TestCaseResult) obj;
             if (name != null && resultObj.name != null &&
                 name.equals(resultObj.name)) {
                 return true;
             }
         }
         return false;
     }
 
     public String getInstanceFactoryClassName() {
         return instanceFactoryClassName;
     }
 
     public void setInstanceFactoryClassName(String instanceFactoryClassName) {
         this.instanceFactoryClassName = instanceFactoryClassName;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public int getStepCount() {
         return stepCount;
     }
 
     public void setStepCount(int stepCount) {
         this.stepCount = stepCount;
     }
 
     public String getStepDelayConfiguration() {
         return stepDelayConfiguration;
     }
 
     public void setStepDelayConfiguration(String stepDelayConfiguration) {
         this.stepDelayConfiguration = stepDelayConfiguration;
     }
 
     public int getStepSize() {
         return stepSize;
     }
 
     public void setStepSize(int stepSize) {
         this.stepSize = stepSize;
     }
 
     public Set<TestCaseInstanceResult> getTestCaseInstanceResults() {
         return testCaseInstanceResults;
     }
 
     public void setTestCaseInstanceResults(
         Set<TestCaseInstanceResult> testCaseInstanceResults) {
         this.testCaseInstanceResults = testCaseInstanceResults;
     }
 
     public Set<TestProperty> getTestProperties() {
         return testProperties;
     }
 
     public void setTestProperties(Set<TestProperty> testProperties) {
         this.testProperties = testProperties;
     }
 
     public Set<KeyedInformation> getOtherInfomations() {
         return otherInfomations;
     }
 
     public void setOtherInfomations(Set<KeyedInformation> otherInfomations) {
         this.otherInfomations = otherInfomations;
     }
 
     public int getTestResultId() {
         return testResultId;
     }
 
     public void setTestResultId(int testResultId) {
         this.testResultId = testResultId;
     }
 }
