 package org.apache.maven.plugin.coreit;
 
 /*
  * Copyright 2001-2004 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.maven.plugin.AbstractPlugin;
 import org.apache.maven.plugin.PluginExecutionRequest;
 import org.apache.maven.plugin.PluginExecutionResponse;
 
 import java.io.File;
 import java.io.FileWriter;
 
 /**
  * @goal it0013
  *
  * @description touches a test file
  *
  * @parameter
  *  name="outputDirectory"
  *  type="String"
  *  required="true"
  *  validator=""
  *  expression="#project.build.directory"
  *  description=""
  */
 public class CoreIt0013Mojo
     extends AbstractPlugin
 {
     private static final int DELETE_RETRY_SLEEP_MILLIS = 10;
 
     public void execute( PluginExecutionRequest request, PluginExecutionResponse response )
         throws Exception
     {
         String outputDirectory = (String) request.getParameter( "outputDirectory" );
 
         File f = new File( outputDirectory );
         
         if ( !f.exists() )
         {
             f.mkdirs();
         }
         
         File touch = new File( f, "it0013-verify" );
         
         FileWriter w = new FileWriter( touch );
         
         w.write( "it0013-verify" );
         
         w.close();                
     }
 }
