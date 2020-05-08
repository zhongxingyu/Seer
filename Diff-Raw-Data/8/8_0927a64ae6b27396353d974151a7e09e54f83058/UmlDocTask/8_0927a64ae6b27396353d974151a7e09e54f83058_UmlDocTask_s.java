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
 import java.io.IOException;
 
 import org.apache.maven.jxr.util.DotTask.DotNotPresentInPathBuildException;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 
 /**
  * <a href="http://ant.apache.org/">Ant</a> task to generate UML diagram.
  * <br/>
  * <b>Note</b>: <a href="http://www.graphviz.org/">Graphviz</a> program should be in the path.
  *
  * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
  * @version $Id$
  */
 public class UmlDocTask
     extends Task
 {
     /** Java source directory */
     private File srcDir;
 
     /** Output file of the diagram*/
     private File out;
 
     /** Specify verbose information */
     private boolean verbose;
 
     /** Terminate Ant build */
     private boolean failOnError;
 
     /**
      * Set the Java source directory.
      *
      * @param d Path to the directory.
      */
     public void setSrcDir( File d )
     {
         this.srcDir = d;
     }
 
     /**
      * Set the destination file.
      *
      * @param f Path to the generated UML file.
      */
     public void setOut( File f )
     {
         this.out = f;
     }
 
     /**
      * Setter for the verbose
      *
      * @param verbose the verbose to set
      */
     public void setVerbose( boolean verbose )
     {
         this.verbose = verbose;
     }
 
     /**
      * Set fail on an error.
      *
      * @param b true to fail on an error.
      */
     public void setFailonerror( boolean b )
     {
         this.failOnError = b;
     }
 
     /** {@inheritDoc} */
     public void init()
         throws BuildException
     {
         super.init();
     }
 
     /** {@inheritDoc} */
     public String getTaskName()
     {
         return "umldoc";
     }
 
     /** {@inheritDoc} */
     public String getDescription()
     {
         return "Generate UML documentation";
     }
 
     /** {@inheritDoc} */
     public void execute()
         throws BuildException
     {
         try
         {
             GenerateUMLDoc generator = new GenerateUMLDoc( getSrcDir(), getOut() );
            generator.setVerbose( true );
             generator.generateUML();
         }
         catch ( IllegalArgumentException e )
         {
             if ( !failOnError )
             {
                 throw new BuildException( "IllegalArgumentException: " + e.getMessage(), e, getLocation() );
             }
 
             log( "IllegalArgumentException: " + e.getMessage(), Project.MSG_ERR );
         }
         catch ( IOException e )
         {
             if ( !failOnError )
             {
                 throw new BuildException( "IOException: " + e.getMessage(), e, getLocation() );
             }
 
             log( "IOException: " + e.getMessage(), Project.MSG_ERR );
         }
         catch ( DotNotPresentInPathBuildException e )
         {
             log( "Dot is not present in the path: " + e.getMessage(), Project.MSG_ERR );
         }
         catch ( BuildException e )
         {
             e.printStackTrace();
             if ( !failOnError )
             {
                 throw new BuildException( "RuntimeException: " + e.getMessage(), e, getLocation() );
             }
 
             log( e.getMessage(), Project.MSG_ERR );
         }
     }
 
     // ----------------------------------------------------------------------
     // Private
     // ----------------------------------------------------------------------
 
     /**
      * @return the source dir.
      */
     private File getSrcDir()
     {
         return this.srcDir;
     }
 
     /**
      * @return the output file.
      */
     private File getOut()
     {
         return this.out;
     }
 }
