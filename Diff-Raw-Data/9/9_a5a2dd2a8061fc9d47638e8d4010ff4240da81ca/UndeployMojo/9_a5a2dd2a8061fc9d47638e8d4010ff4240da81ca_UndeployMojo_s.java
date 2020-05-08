 /*
  * Copyright 2005 Jeff Genender.
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
 package org.codehaus.mojo.jboss;
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Undeploys a directory or file from JBoss via JMX
  *
  * @author <a href="mailto:jgenender@apache.org">Jeff Genender</a>
  * @goal undeploy
  * @description Maven 2 JBoss plugin
  */
 public class UndeployMojo extends AbstractDeployerMojo {
 
     /**
      * The undeployment URL
      *
     * @parameter expression="/jmx-console/HtmlAdaptor?action=invokeOpByName&amp;name=jboss.system:service%3DMainDeployer&amp;methodName=undeploy&amp;argType=java.net.URL&amp;arg0="
      * @required
      */
     protected String undeployUrlPath;
 
     public void execute() throws MojoExecutionException {
         getLog().info("Undeploying " + fileName + " from JBoss.");
         String url = "http://" + hostName + ":" + port + undeployUrlPath + "file:" + fileName;
         doURL(url);
     }
 }
