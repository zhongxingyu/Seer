 package org.apache.maven.jxr.java.doc;
 
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
 
 import java.io.File;
 
 import org.apache.tools.ant.Project;
 
 import junit.framework.TestCase;
 
 /**
  * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
  * @version $Id$
  */
 public class UmlDocTaskTest
     extends TestCase
 {
     /**
      * Call UMLdoc task
      *
      * @throws Exception if any.
      */
     public void testDefaultExecute()
         throws Exception
     {
         final String basedir = new File( "" ).getAbsolutePath();
 
         File out = new File( basedir, "target/unit/umldoc-default/uml.svg" );
         File srcDir = new File( basedir, "src/test/resources/javasrc" );
 
         Project antProject = new Project();
         antProject.setBasedir( basedir );
 
         UmlDocTask task = new UmlDocTask();
         task.setProject( antProject );
         task.setSrcDir( srcDir );
         task.setOut( out );
        task.setVerbose( true );
         task.execute();
 
         // Generated files
         assertTrue( out.exists() );
         assertTrue( out.length() > 0 );
     }
 }
