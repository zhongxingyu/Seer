 package org.apache.maven.surefire.its;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the LicenseUni.
  */
 
 import java.io.File;
 import org.apache.maven.surefire.its.fixture.OutputValidator;
 import org.apache.maven.surefire.its.fixture.SurefireJUnit4IntegrationTestCase;
 import org.apache.maven.surefire.its.fixture.TestFile;
 
 import org.junit.Assert;
 import org.junit.Assume;
 import org.junit.Test;
 
 /**
 * Use -Dtest to run a single TestNG test, overriding the suite XML parameter.
  *
 * @author <a href="mailto:dfabulich@apache.org">Dan Fabulich</a>
  */
 public class UnicodeTestNamesIT
     extends SurefireJUnit4IntegrationTestCase
 {
     @Test
     public void checkFileNamesWithUnicode()
     {
         File sourceFile = new File("src/test/resources/unicode-testnames/src/test/java/junit/twoTestCases/而索其情Test.java");
         Assume.assumeTrue( sourceFile.exists() );
         OutputValidator outputValidator =
             unpack( "/unicode-testnames" ).executeTest().assertTestSuiteResults( 2, 0, 0, 0 );
         TestFile surefireReportsFile = outputValidator.getSurefireReportsFile( "junit.twoTestCases.而索其情Test.txt" );
         Assert.assertTrue( surefireReportsFile.exists() );
         //surefireReportsFile .assertContainsText( "junit.twoTestCases.\u800C\u7D22\u5176\u60C5Test.txt" );
     }
 
 }
