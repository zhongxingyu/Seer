 /*
  * Copyright 2005 The Apache Software Foundation Licensed under the Apache
  * License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * 
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.lenya.htmlunit.defaultpub.site;
 
 import org.apache.lenya.htmlunit.LenyaTestCase;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class CreateManyTest extends LenyaTestCase {
 
     protected int maxChildren;
     protected int nofDocuments;
     protected int docCounter = 0;
 
     /*
      * (non-Javadoc)
      * 
      * @see junit.framework.TestCase#setUp()
      */
     protected void setUp() throws Exception {
         this.testName = "Create Many Documents";
         super.setUp();
         this.maxChildren = this.config.getInt("lenya.tests.sitemanagement.createMany.maxChildren");
         this.nofDocuments = this.config.getInt("lenya.tests.sitemanagement.createMany.nofDocuments");
     }
 
     /**
      * Attempts to create a number of XHTML documents.
      * 
      * @throws Exception
      */
     public void testCreateManyDocuments() throws Exception {
         loginAsDefaultUser();
        int nofDocuments = 10;
 
         String startDocID = this.config.getString("lenya.tests.general.startDocID");
 
        createDocumentsRec(startDocID, nofDocuments);
 
         logout();
     }
 
     /** Recursively creates documents
      * @param parentDocID the parent document
      * @param nofDocuments the number of documents to be created
      * @throws Exception
      */
     protected void createDocumentsRec(String parentDocID, int nofDocuments)
             throws Exception {
         if (nofDocuments <= 0) return;
 
         int nofDocsOnThisLevel = maxChildren;
         if (nofDocuments < nofDocsOnThisLevel) nofDocsOnThisLevel = nofDocuments; 
             
         nofDocuments -= nofDocsOnThisLevel;
         int nofSubDocs = (int)Math.ceil((double)nofDocuments / (double)maxChildren);
 
         for (int i = 0; i < nofDocsOnThisLevel; i++) {
             String nodeID = createTestNodeID();
             createNewDocument(parentDocID, nodeID, "htmlunit many test "+(++docCounter), "xhtml");
             createDocumentsRec(parentDocID + "/" + nodeID, nofSubDocs);
             nofDocuments -= nofSubDocs;
             if (nofDocuments < nofSubDocs) nofSubDocs = nofDocuments;
         }
     }
 
     public static Test suite() {
         return new TestSuite(CreateManyTest.class);
     }
 
 }
