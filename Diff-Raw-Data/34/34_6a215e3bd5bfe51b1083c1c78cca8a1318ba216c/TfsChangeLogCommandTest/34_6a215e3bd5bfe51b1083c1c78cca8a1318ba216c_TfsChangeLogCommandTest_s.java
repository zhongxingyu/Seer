 package org.apache.maven.scm.provider.tfs.command;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import java.io.File;
 
 import org.apache.maven.scm.ScmFile;
 import org.apache.maven.scm.ScmFileStatus;
 import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
 import org.codehaus.plexus.util.cli.Commandline;
 
 public class TfsChangeLogCommandTest
     extends TfsCommandTest
 {
 
     private FileListConsumer consumer;
 
     protected void setUp()
         throws Exception
     {
         super.setUp();
         consumer = new FileListConsumer();
     }
 
     public void testCommandline()
     {
         TfsScmProviderRepository repo = getScmProviderRepository();
         File f = new File( "file" );
         Commandline cmd = new TfsChangeLogCommand().createCommand( repo, getScmFileSet(), f ).command;
         String expected = "tf history -login:user,password -format:detailed " + f.getName();
         assertCommandLine( expected, getWorkingDirectory(), cmd );
     }
 
     public void testCommand()
     {
         consumer.consumeLine( "C:\\temp\\maven\\c8:" );
         consumer.consumeLine( "Replacing .tpattributes" );
         consumer.consumeLine( "Replacing .classpath" );
         consumer.consumeLine( "Replacing .myclasspath" );
         consumer.consumeLine( "Replacing .project" );
         consumer.consumeLine( "" );
         consumer.consumeLine( "C:\\temp\\maven\\c8\\.settings:" );
         consumer.consumeLine( "" );
         consumer.consumeLine( "C:\\temp\\maven\\c8:" );
         consumer.consumeLine( "Replacing .tpignore" );
         consumer.consumeLine( "Replacing about.html" );
         consumer.consumeLine( "" );
         consumer.consumeLine( "C:\\temp\\maven\\c8\\bin:" );
         consumer.consumeLine( "" );
         consumer.consumeLine( "C:\\temp\\maven\\c8:" );
         consumer.consumeLine( "Replacing build.properties" );
         consumer.consumeLine( "Replacing customBuildCallbacks.xml" );
         consumer.consumeLine( "" );
 
         assertNotNull( consumer.getFiles() );
         assertEquals( 11, consumer.getFiles().size() );
        assertTrue( consumer.getFiles().contains( new ScmFile( "C:\\temp\\maven\\c8", ScmFileStatus.CHECKED_OUT ) ) );
        assertTrue( consumer.getFiles().contains(
                                                  new ScmFile( "C:\\temp\\maven\\c8\\build.properties",
                                                               ScmFileStatus.CHECKED_OUT ) ) );
     }
 
     public void testMSCommand()
     {
         consumer.consumeLine( "c:\\temp\\maven:" );
         consumer.consumeLine( "Replacing c10" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "c:\\temp\\maven\\c10:" );
         consumer.consumeLine( "Replacing .classpath" );
         consumer.consumeLine( "Replacing .myclasspath" );
         consumer.consumeLine( "Replacing .project" );
         consumer.consumeLine( "Replacing .settings" );
         consumer.consumeLine( "Replacing .tpattributes" );
         consumer.consumeLine( "Replacing .tpignore" );
         consumer.consumeLine( "Replacing about.html" );
         consumer.consumeLine( "Replacing bin" );
         consumer.consumeLine( "Replacing build.properties" );
 
         assertNotNull( consumer.getFiles() );
         assertEquals( 11, consumer.getFiles().size() );
        assertTrue( consumer.getFiles().contains( new ScmFile( "c:\\temp\\maven", ScmFileStatus.CHECKED_OUT ) ) );
        assertTrue( consumer.getFiles().contains(
                                                  new ScmFile( "c:\\temp\\maven\\c10\\build.properties",
                                                               ScmFileStatus.CHECKED_OUT ) ) );
     }
 
 }
