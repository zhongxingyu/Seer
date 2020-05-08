 package com.insightfullogic.release;
 
 /*
  Copyright 2013 John Oliver
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 import org.apache.maven.artifact.versioning.ArtifactVersion;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * @goal release-versions
  */
 public class MoveToReleaseMojo extends AbstractVersionModMojo {
 
 	/**
 	 * Weather or not to update versions held in properties too. Careful with
 	 * this, it will aggressively update to the most recent version available
 	 * for that dependency.
 	 * 
 	 * @parameter property="rv.processProperties" default-value="false"
 	 */
 	public Boolean processProperties = false;
 
 	public void execute() throws MojoExecutionException {
 		if(processProperties) {
 			bumpProperties();
 		}
 
 		executeStandardUpdateGoals();
 
 		ArtifactVersion artifactVersion = new Version(project.getVersion());
 		Version newVersion = new Version(artifactVersion.getMajorVersion(),
 				artifactVersion.getMinorVersion(),
 				artifactVersion.getIncrementalVersion(),
 				artifactVersion.getBuildNumber(), null);
 		writeVersion(newVersion);
 	
 	}
 
 }
