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
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 public class IntegrationTestSuite
     extends TestCase
 {
     public static Test suite()
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
         // suite.addTestSuite( NPandayIT0033VBSourceWithCSharpSourceTest.class ); // issue #11732
         // suite.addTestSuite( NPandayIT0002NetModuleDependencyTest.class ); // issue #11729
         // suite.addTestSuite( NPandayIT0003NetModuleTransitiveDependencyTest.class ); // issue #11729
 
        suite.addTestSuite( NPANDAY_459_MsBuildProjectReferencesTest.class );
        suite.addTestSuite( NPANDAY_465_AspxDisablePrecompilationTest.class );
         suite.addTestSuite( NPANDAY_488_MSDeployPackageSimpleWebApp.class );
         suite.addTestSuite( NPANDAY_96_GlobalAsaxPrecompiledTest.class );
         suite.addTestSuite( NPANDAY_377_WithCustomNPandaySettingsFileTest.class );
         suite.addTestSuite( NPANDAY_377_WithCustomNPandaySettingsDirectoryTest.class );
         suite.addTestSuite( NPANDAY_329_VS2010WcfProjectSupportTest.class );
         suite.addTestSuite( NPANDAY_328_VS2010WpfProjectSupportTest.class );
         suite.addTestSuite( NPANDAY_330_VS2010MvcProjectSupportTest.class );
         suite.addTestSuite( NPANDAY_288_Net40SupportTest.class );
         suite.addTestSuite( NPANDAY_302_SnapshotUpdatesTest.class );
         suite.addTestSuite( NPANDAY_292_CompilerParamForOptioninferTest.class );
         suite.addTestSuite( NPANDAY_140_ConflictingExtensionsTest.class );
         suite.addTestSuite( NPANDAY_268_TransitiveDependenciesTest.class );
         suite.addTestSuite( NPANDAY_262_ResolvingMixedVersionsTest.class );
         suite.addTestSuite( NPANDAY_196_MvcSupportTest.class );
         suite.addTestSuite( NPANDAY_245_WpfGeneratedResourcesHandlingTest.class );
         suite.addTestSuite( NPANDAY_198_MissingGroupOrVersionTest.class );
         suite.addTestSuite( NPANDAY_208_MsBuildCopyReferencesTest.class );
         suite.addTestSuite( NPANDAY_202_MsBuildErrorHandlingTest.class );
         suite.addTestSuite( NPANDAY_121_ResGenWithErrorInFileNameTest.class );
 
         suite.addTestSuite( NPandayIT0014WithResourceFileTest.class );
         suite.addTestSuite( NPandayIT0013WebAppInstallTest.class );
         suite.addTestSuite( NPandayIT0012VBWebAppTest.class );
         suite.addTestSuite( NPandayIT0011SnapshotResolutionTest.class );
         suite.addTestSuite( NPandayIT0041Net35Test.class );
         suite.addTestSuite( NPandayIT0040IntraProjectDependencyTest.class );
         suite.addTestSuite( NPandayIT0039ConsoleApplicationTest.class );
         suite.addTestSuite( NPandayIT0038CompilerWithArgsTest.class );
         suite.addTestSuite( NPandayIT0037ClassLibWithWebRefInstallTest.class );
         suite.addTestSuite( NPandayIT0036InstalledArtifactsVerificationTest.class );
         suite.addTestSuite( NPandayIT0035VBRootNamespaceTest.class );
         suite.addTestSuite( NPandayIT0032CompileExclusionsTest.class );
         suite.addTestSuite( NPandayIT0029RemoteRepoTest.class );
         suite.addTestSuite( NPandayIT0028RemoteSnapshotRepoTest.class );
         suite.addTestSuite( NPandayIT0022StrongNameKeyAddedToAssemblyTest.class );
         suite.addTestSuite( NPandayIT0020EmbeddedResourcesTest.class );
         suite.addTestSuite( NPandayIT0010VBCompilationTest.class );
         suite.addTestSuite( NPandayIT0007XSDVerificationTest.class );
         suite.addTestSuite( NPandayIT0006StockingHandlersTest.class );
         suite.addTestSuite( NPandayIT0004NUnitTestVerificationTest.class );
         suite.addTestSuite( NPandayIT0001CompilerVerificationTest.class );
 
         return suite;
     }
 }
