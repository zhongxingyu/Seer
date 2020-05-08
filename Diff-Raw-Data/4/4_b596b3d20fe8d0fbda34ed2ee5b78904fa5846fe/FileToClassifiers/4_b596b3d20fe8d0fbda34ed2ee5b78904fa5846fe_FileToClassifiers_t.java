 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.attachqars;
 
 /**
  * Enum of files to attach.
  * @author Mirko Friedenhagen
  */
 public enum FileToClassifiers {
     /** Checkstyle. */
     Checkstyle("checkstyle-result.xml", "checkstyle-report"), 
     /** PMD. */
     Pmd("pmd.xml", "pmd-report"),
     /** CPD. */
     Cpd("cpd.xml", "cpd-report"),
     /** Findbugs. */
     Findbugs("findbugsXml.xml", "findbugs-report"),
     /** Jacoco. */
    Jacoco("site/jacoco/jacoco.xml", "jacoco-report"),
    /** Jacoco integration test results. */
    JacocoIt("site/jacoco-it/jacoco.xml", "jacoco-it-report");
 
     /**
      * Filename relative to target.
      */
     private final String fileName;
 
     /**
      * Classifier for attachment.
      */
     private final String classifier;
 
     /**
      * @param fileName to attach.
      * @param classifier of attachment.
      */
     private FileToClassifiers(String fileName, String classifier) {
         this.fileName = fileName;
         this.classifier = classifier;
     }
 
     /**
      * @return the fileName
      */
     String getFileName() {
         return fileName;
     }
 
     /**
      * @return the classifier
      */
     String getClassifier() {
         return classifier;
     }
 
 }
