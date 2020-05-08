 /**********************************************************************************
  *
  * $URL$
  * $Id$  
  ***********************************************************************************
  *
  * Copyright (c) 2008 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  **********************************************************************************/
 package org.etudes.api.app.melete;
 
 import java.util.ArrayList;
 import java.util.List;
 /**
  * Mallika - 4/20/05 - Added method to delete section
 * Revised by rashmi 4/26 add signature for sort sections
 * Mallika - 4/6/06 - Adding parameters for dirs to insertSection and editSection
 * Mallika - 8/1/06 - Adding method to delete sections
 * Rashmi - 8/10/06 - get max no of sections exisiting in a module
 * Rashmi - 8/22/06 - revised insertsection() and add insertsectionresource()
 * Rashmi - 8/23/06 - add license info methods
  */
 public interface SectionService{
 
 	public Integer insertSection(ModuleObjService module, SectionObjService section) throws Exception;
 	public void editSection(SectionObjService section, MeleteResourceService melResource) throws Exception;
 	public void editSection(SectionObjService section ) throws Exception;
 	public SectionObjService getSection(int sectionId);
 
 	/* this method inserts the association in between section and resource and updates melete resource object*/
 	public void insertSectionResource(SectionObjService section, MeleteResourceService melResource) throws Exception;
 
 	/* this method inserts the association and inserts a new melete resource*/
 	public void insertMeleteResource(SectionObjService section, MeleteResourceService melResource) throws Exception;
 	public void insertResource(MeleteResourceService melResource) throws Exception;
 	public void updateResource(MeleteResourceService melResource) throws Exception;
 	public void deleteResource(MeleteResourceService melResource) throws Exception;
 
 	// used by view pages -- Mallika pages
    public void setSection(SectionObjService sec);
 
    public List getSortSections(ModuleObjService module);
    public String getSectionDisplaySequence(SectionObjService module);
 
    public void deleteSection(SectionObjService sec, String courseId, String userId) throws Exception;
    public void deleteSections(List sectionBeans, String courseId, String userId) throws Exception;
 
    public ArrayList getMeleteLicenses();
    public String[] getCCLicenseURL(boolean reqAttr, boolean allowCmrcl, int allowMod);
    public SectionResourceService getSectionResource(String secResourceId);
    public MeleteResourceService getMeleteResource(String selResourceId);
    public void deassociateSectionResource(SectionObjService section, SectionResourceService secResource) throws Exception;
    public void updateSectionResource(SectionObjService section, SectionResourceService secResource) throws Exception;
    public List findResourceInUse(String selResourceId, String courseId);
   public void deleteResourceInUse(String delResourceId) throws Exception;
    public int cleanUpDeletedSections() throws Exception;
    public SectionObjService getNextSection(String curr_id, String seqXML) throws Exception;
    public SectionObjService getPrevSection(String curr_id, String seqXML) throws Exception;
 }
