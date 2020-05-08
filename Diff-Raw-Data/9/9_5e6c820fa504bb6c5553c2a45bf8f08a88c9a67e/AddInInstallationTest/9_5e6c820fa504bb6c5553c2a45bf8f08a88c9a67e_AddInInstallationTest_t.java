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
 
 import org.apache.maven.it.Verifier;
 import org.apache.maven.it.util.ResourceExtractor;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 
 public class AddInInstallationTest
     extends AbstractNPandayIntegrationTestCase
 {
     public AddInInstallationTest()
     {
         super( "[1.0.2,)" );
     }
 
     public void testAddInInstallation()
         throws Exception
     {
         // TODO: these tests are still useful even if we remove the vsinstaller plugin - in that case, we need an
         //       alternate way to obtain the visualstudio.addin artifact from the repository
 
         List<String> artifacts;
         String groupId;
         if ( checkNPandayVersion( createVersionRange( "(,1.4.0)" ), version ) ) 
         {
             groupId = "npanday.plugin";
             artifacts = Arrays.asList(
                 "npanday.model:NPanday.Model.Pom",
                 "npanday.plugin:NPanday.Plugin.Msbuild",
                 "npanday.visualstudio:NPanday.VisualStudio.Addin"
             );
         }
         else
         {
             groupId = "org.apache.npanday.plugins";
             artifacts = Arrays.asList(
                 "org.apache.npanday:NPanday.Model.Pom",
                 "org.apache.npanday.plugins:NPanday.Plugin.Msbuild",
                 "org.apache.npanday.visualstudio:NPanday.VisualStudio.Addin"
             );
         }
 
         File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/AddInInstallationTest" );
         File vsinstaller = new File( testDir, "vsinstaller" );
         testDir.mkdirs();
         Verifier verifier = getVerifier( testDir );
         verifier.setAutoclean( false );
         verifier.getCliOptions().add( "-DinstallationLocation=" + vsinstaller.getAbsolutePath() );
         verifier.executeGoal( groupId + ":maven-vsinstaller-plugin:" + version.toString() + ":install" );
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
 
        File vsinstallerBin = new File( vsinstaller, "bin" );
         if ( checkNPandayVersion( createVersionRange( "[1.5.0,)" ), version ) )
         {
             File assembly = new File( verifier.getArtifactPath( "org.apache.logging", "log4net", "1.2.11", "dll",
                                                                 "net-2.0" ) );
             assertTrue( "Check " + assembly.getAbsolutePath() + " exists", assembly.exists() );
             
            assembly = new File( vsinstallerBin, "log4net.dll" );
             assertTrue( "Check " + assembly.getAbsolutePath() + " exists", assembly.exists() );
         }
 
         // check correct framework version of libraries in the local repository
         for ( String artifact : artifacts )
         {
             String[] parts = artifact.split( ":" );
             String classifier = parts[1].equals( "NPanday.Model.Pom" ) ?  "4b435f4d76e2f0e6" : null;
             File assembly = new File( verifier.getArtifactPath( parts[0], parts[1], version.toString(), "dll",
                                                                 classifier ) );
             assertTrue( "Check " + assembly + " exists", assembly.exists() );
 
             assertAssemblyFrameworkVersion( assembly, "[2.0.50727]" );
 
            assembly = new File( vsinstallerBin, parts[1] + ".dll" );
             assertTrue( "Check " + assembly.getAbsolutePath() + " exists", assembly.exists() );
         }
     }
 }
