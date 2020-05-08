 /*******************************************************************************
  *
  * Copyright (c) 2011 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *    Nikita Levyankov
  *
  *******************************************************************************/
 
 package hudson.model;
 
 /**
  * Mock class for FreeStyleProject
  * <p/>
  * Date: 9/27/11
  *
  * @author Nikita Levyankov
  */
 public class FreeStyleProjectMock extends FreeStyleProject {
 
     public FreeStyleProjectMock(String name) {
         super((ItemGroup) null, name);
         setAllowSave(false);
     }
 
     @Override
     protected void updateTransientActions() {
     }
 
     /**
      * For the unit tests only. Sets cascadingProject for the job.
      *
      * @param cascadingProject parent job
      */
     public void setCascadingProject(FreeStyleProject cascadingProject) {
         this.cascadingProject = cascadingProject;
         this.cascadingProjectName = cascadingProject != null ? cascadingProject.getName() : null;
     }
 }
