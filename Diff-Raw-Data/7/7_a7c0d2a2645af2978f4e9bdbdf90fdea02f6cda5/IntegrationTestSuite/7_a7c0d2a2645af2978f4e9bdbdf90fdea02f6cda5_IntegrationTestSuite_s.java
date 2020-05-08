 package npanday.its;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
import java.io.PrintStream;

 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 public class IntegrationTestSuite
     extends TestCase
 {
    private static PrintStream out = System.out;

     public static Test suite()
        throws VerificationException
     {
         TestSuite suite = new TestSuite();
 
         /*
          * This must be the first one to ensure the local repository is properly setup.
          */
         suite.addTestSuite( BootstrapTest.class );
 
         /*
          * Add tests in order of newest first.
          * Newer tests are also more likely to fail, so this is
          * a fail fast technique as well.
          */
 
         // Tests that currently don't pass for any Maven version, i.e. the corresponding issue hasn't been resolved yet
         // suite.addTestSuite( NPandayIT0033Test.class ); // issue #11732
         // suite.addTestSuite( NPandayIT0002Test.class ); // issue #11729
         // suite.addTestSuite( NPandayIT0003Test.class ); // issue #11729
 
         suite.addTestSuite( NPandayIT13018TransitiveDependenciesTest.class );
         suite.addTestSuite( NPandayIT12940MixedVersionsTest.class );
         suite.addTestSuite( NPandayIT11480MVCProjectTest.class );
         suite.addTestSuite( NPandayIT12549WpfProjectTest.class );
         suite.addTestSuite( NPandayIT11579MissingGroupOrVersionTest.class );
         suite.addTestSuite( NPandayIT11695MsBuildCopyReferencesTest.class );
         suite.addTestSuite( NPandayIT11637MsBuildErrorHandlingTest.class );
         suite.addTestSuite( NPandayIT9903ResGenWithErrorInFileNameTest.class );
 
         suite.addTestSuite( NPandayITWithResourceFileTest.class );
         suite.addTestSuite( NPandayITWebAppInstallTest.class );
         suite.addTestSuite( NPandayITVBWebAppTest.class );
         suite.addTestSuite( NPandayITSnapshotResolutionTest.class );
         suite.addTestSuite( NPandayITNet35Test.class );
         suite.addTestSuite( NPandayITIntraProjectDependencyTest.class );
         suite.addTestSuite( NPandayITConsoleApplicationTest.class );
         suite.addTestSuite( NPandayITCompilerWithArgsTest.class );
         suite.addTestSuite( NPandayITClassLibWithWebRefInstallTest.class );
         suite.addTestSuite( NPandayIT0036Test.class );
         suite.addTestSuite( NPandayIT0035Test.class );
         suite.addTestSuite( NPandayIT0033Test.class );
         suite.addTestSuite( NPandayIT0032VerifyInstallationTest.class );
         suite.addTestSuite( NPandayIT0029RemoteRepoTest.class );
         suite.addTestSuite( NPandayIT0028RemoteSnapshotRepoTest.class );
         suite.addTestSuite( NPandayIT0022Test.class );
         suite.addTestSuite( NPandayIT0020Test.class );
         suite.addTestSuite( NPandayIT0010Test.class );
         suite.addTestSuite( NPandayIT0007XSDVerificationTest.class );
         suite.addTestSuite( NPandayIT0006StockingHandlersTest.class );
         suite.addTestSuite( NPandayIT0004NUnitTestVerificationTest.class );
         suite.addTestSuite( NPandayIT0003Test.class );
         suite.addTestSuite( NPandayIT0002Test.class );
         suite.addTestSuite( NPandayIT0001VerifyInstallationTest.class );
 
         return suite;
     }
 }
