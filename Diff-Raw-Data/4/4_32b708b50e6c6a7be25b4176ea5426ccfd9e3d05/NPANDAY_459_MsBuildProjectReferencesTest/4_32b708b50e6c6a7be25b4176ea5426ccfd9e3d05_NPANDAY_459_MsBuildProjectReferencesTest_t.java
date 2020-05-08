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
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.maven.it.Verifier;
 import org.apache.maven.it.util.ResourceExtractor;
 
 import java.io.File;
 
 public class NPANDAY_459_MsBuildProjectReferencesTest
     extends AbstractNPandayIntegrationTestCase
 {
     public NPANDAY_459_MsBuildProjectReferencesTest()
     {
         super( "[1.4.1-incubating,)" );
     }
 
     public void testMsBuildWithProjectReferences()
         throws Exception
     {
         File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/NPANDAY_459_MSBuildProjectReferences" );
         Verifier verifier = getVerifier( testDir );
         // TODO: would be better to ensure each IT has unique IDs for required test artifacts in a better namespace for deleting
         verifier.deleteArtifacts( "test" );
 
        // Can only run up until package, because currently "install" deletes
        // the bin directory (though perhaps shouldn't)
        verifier.executeGoal( "package" );
         verifier.assertFileNotPresent(
             new File( testDir, "ClassLibrary1/.references/test/test-snapshot-1.0-SNAPSHOT/test-snapshot.dll" ).getAbsolutePath() );
         verifier.assertFilePresent(
             new File( testDir, "ConsoleApplication1/.references/test/test-snapshot-1.0-SNAPSHOT/test-snapshot.dll" ).getAbsolutePath() );
         verifier.assertFilePresent( new File( testDir, "ConsoleApplication1/bin/Debug/ConsoleApplication1.exe" ).getAbsolutePath() );
         verifier.assertFilePresent( new File( testDir, "ClassLibrary1/bin/Debug/ClassLibrary1.dll" ).getAbsolutePath() );
         verifier.assertFilePresent( new File( testDir, "ConsoleApplication1/bin/Debug/ClassLibrary1.dll" ).getAbsolutePath() );
         // TODO: need to properly support transitive dependencies in the projects that copy files
         //verifier.assertFilePresent( new File( testDir, "ConsoleApplication1/bin/Debug/test-snapshot.dll" ).getAbsolutePath() );
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
     }
 }
