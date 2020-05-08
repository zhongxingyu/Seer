 /*******************************************************************************
  * Copyright (c) 2008 The Bioclipse Project and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * www.eclipse.orgepl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
  * 
  * Contributors:
  *     Ola Spjuth - initial API and implementation
  *     
  ******************************************************************************/
 package net.bioclipse.data.sampledata.wizards;
 
 /**
  * Hold a folder object to be installed. Initialized from EP.
  * @author ola
  *
  */
 public class InstallableFolder {
 
     private String name;
     private String description;
     private String location;
     
     //Should this be installed?
     private boolean checked;
 
    public InstallableFolder(String name, String description, String location) {
         this.name = name;
         this.description = description;
         this.location = location;
     }
 
     public boolean isChecked() {
         return checked;
     }
 
     public void setChecked(boolean checked) {
         this.checked = checked;
     }
     
     public String getName() {
         return name;
     }
     public void setName(String name) {
         this.name = name;
     }
     public String getDescription() {
         return description;
     }
     public void setDescription(String description) {
         this.description = description;
     }
     public String getLocation() {
         return location;
     }
     public void setLocation(String location) {
         this.location = location;
     }
     
     
 }
