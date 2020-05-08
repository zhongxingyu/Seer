 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
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
 
 package org.etudes.component.app.melete;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.content.cover.ContentTypeImageService;
 
 import org.etudes.component.app.melete.MeleteResource;
 import org.etudes.component.app.melete.MeleteUtil;
 import org.hibernate.HibernateException;
 
 import org.etudes.api.app.melete.CourseModuleService;
 import org.etudes.api.app.melete.ModuleDateBeanService;
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.ModuleShdatesService;
 import org.etudes.api.app.melete.MeleteCHService;
 import org.etudes.api.app.melete.SectionObjService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.MeleteExportService;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.sakaiproject.exception.IdUnusedException;
 
 import org.sakaiproject.db.cover.SqlService;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.entity.api.Entity;
 
 import org.sakaiproject.util.Validator;
 
 
 
 /**
  * @author Rashmi
  *
  * This is the class implementing ModuleService interface.
  */
 /*
  * Mallika - 1/17/07 - Adding code to migrate meleteDocs
  * Mallika - 2/21/07 - Adding code to truncate filename
  * Mallika - 5/15/07 - Added code for import from site
  * Mallika - 6/18/07 - fixed the seq issue
  * Mallika - 7/24/07 - Added embed tag processing
  */
 public class ModuleServiceImpl implements ModuleService,Serializable {
 	/** Dependency:  The logging service. */
 	private Log logger = LogFactory.getLog(ModuleServiceImpl.class);
 
 	private ModuleDB moduledb;
 	private List moduleDateBeans = null;
 	private List viewModuleBeans = null;
 	private List modules = null;
 	private Module module = null;
 	private ModuleDateBean mdBean = null;
 	private MeleteUtil meleteUtil= new MeleteUtil();
 
 
 	//constants
 	public final static int NO_CODE = 0;
 	public final static int Copyright_CODE = 1;
 	public final static int PD_CODE = 2;
 	public final static int CC_CODE = 3;
 	public final static int FU_CODE = 4;
 	private MeleteCHService meleteCHService;
 	private SectionService sectionService;
 	 /** Dependency: The melete import export service. */
 	protected MeleteExportService meleteExportService;
 
 
 	public void init()
 	{
 		logger.info(this +".init()");
 	}
 
 	public ModuleServiceImpl(){
 
 		if (moduledb== null) moduledb = getModuledb();
 
 		}
 
 
 
 
 
 	/*
 	 * @see org.foothillglobalaccess.melete.ModuleService#insertProperties(org.foothillglobalaccess.melete.Module, org.foothillglobalaccess.melete.ModuleShdates, int, int)
 	 * creates the course object and calls methods to actually insert a module.
 	 */
 	public void insertProperties(ModuleObjService module, ModuleShdatesService moduleshdates,String userId, String courseId) throws Exception
 	{
 
 	  // module object and moduleshdates are provided by ui pages
 
 		Module module1 = (Module)module;
 		ModuleShdates moduleshdates1 = (ModuleShdates)moduleshdates;
 
 	// insert new module
 		moduledb.addModule(module1, moduleshdates1, userId, courseId);
 		
		moduledb.updateCalendar(module1, moduleshdates1, courseId);	
 
 	}
 
 	
 	public void createSubSection(ModuleObjService module, List secBeans) throws MeleteException
 	{
 		moduledb.createSubSection((Module)module, secBeans);
 	}
 
 	public void bringOneLevelUp(ModuleObjService module, List secBeans) throws MeleteException
 	{
 		moduledb.bringOneLevelUp((Module)module, secBeans);
 	}
 	public void sortModule(ModuleObjService module,String course_id,String Direction) throws MeleteException
 	{
 		moduledb.sortModuleItem((Module)module,course_id, Direction);
 
 	}
 
 	public void sortSectionItem(ModuleObjService module, String section_id,String Direction) throws MeleteException
 	{
 		moduledb.sortSectionItem((Module)module,section_id,Direction);
 	}
 
 	public void copyModule(ModuleObjService module,String courseId,String userId) throws MeleteException
 	{
 		moduledb.copyModule((Module)module, courseId, userId);
 
 	}
 
 	public void moveSections(List sectionBeans,ModuleObjService selectedModule) throws MeleteException
 	{
 	  try{
 		  for (ListIterator<SectionBean> i = sectionBeans.listIterator(); i.hasNext(); )
 		  {
 			  SectionBean moveSectionBean = (SectionBean)i.next();
 			  if(moveSectionBean.getSection().getModuleId() != selectedModule.getModuleId().intValue())
 				  moduledb.moveSection(moveSectionBean.getSection(), (Module)selectedModule);
 		  }
 		}catch (Exception ex)
 		{
 			throw new MeleteException("move_section_fail");
 		}
 	}
 
 	public String printModule(ModuleObjService module) throws MeleteException
 	{
 		try{
 
 		return moduledb.prepareModuleSectionsForPrint((Module)module);
 		}catch (Exception ex)
 		{
 			ex.printStackTrace();
 			throw new MeleteException("print_module_fail");
 		}
 	}
 
 // mallika page stuff
 public List getModuleDateBeans(String userId, String courseId) {
   	if (moduledb == null) moduledb = ModuleDB.getModuleDB();
 
   	try {
   		moduleDateBeans = moduledb.getShownModulesAndDatesForInstructor(userId, courseId);
   	}catch (HibernateException e)
 	{
   		//e.printStackTrace();
   		logger.debug(e.toString());
 	}
   	return moduleDateBeans;
   }
 
 public List getViewModules(String userId, String courseId) {
   	if (moduledb == null) moduledb = ModuleDB.getModuleDB();
 
   	try {
   		viewModuleBeans = moduledb.getViewModulesAndDates(userId, courseId);
   	}catch (HibernateException e)
 	{
   		//e.printStackTrace();
   		logger.debug(e.toString());
 	}
   	return viewModuleBeans;
   }
 
   public void setModuleDateBeans(List moduleDateBeansList) {
     moduleDateBeans = moduleDateBeansList;
   }
 
  public ModuleDateBeanService getModuleDateBean(String userId, String courseId,  int moduleId) {
   	if (moduledb == null) moduledb = ModuleDB.getModuleDB();
 
   	try {
   		mdBean = moduledb.getModuleDateBean(userId, courseId,  moduleId);
   	}catch (HibernateException e)
 	{
   		//e.printStackTrace();
   		logger.debug(e.toString());
 	}
   	return mdBean;
   }
 
  public ModuleDateBeanService getModuleDateBeanBySeq(String userId, String courseId,  int seqNo) {
 	  	if (moduledb == null) moduledb = ModuleDB.getModuleDB();
 
 	  	try {
 	  		mdBean = moduledb.getModuleDateBeanBySeq(userId, courseId,  seqNo);
 	  	}catch (HibernateException e)
 		{
 	  		//e.printStackTrace();
 	  		logger.debug(e.toString());
 		}
 	  	return mdBean;
 	  }
 
   public void setModuleDateBean(ModuleDateBeanService mdBean) {
   	this.mdBean = (ModuleDateBean) mdBean;
   }
 
   public List getModules(String courseId) {
   	try {
   		modules = moduledb.getModules(courseId);
   	}catch (HibernateException e)
 	{
   		//e.printStackTrace();
   		logger.debug(e.toString());
 	}
   	return modules;
   }
 
   public void setModules(List modules) {
     this.modules = modules;
   }
 
 
   /*
    * @see org.foothillglobalaccess.melete.ModuleService#updateProperties(org.foothillglobalaccess.melete.ModuleDateBean)
    * updates the moduleDateBean object
    */
  public void updateProperties(List moduleDateBeans, String courseId)  throws MeleteException
   {
     try{
     moduledb.updateModuleDateBeans(moduleDateBeans);
      }
     catch(Exception ex)
 	{
 		logger.debug("multiple user exception in module business");
 	   throw new MeleteException("edit_module_multiple_users");
 	}
     for (ListIterator i = moduleDateBeans.listIterator(); i.hasNext(); )
  	{
         ModuleDateBean mdbean = (ModuleDateBean) i.next();
         try
         {
           moduledb.updateCalendar((Module)mdbean.getModule(),(ModuleShdates)mdbean.getModuleShdate(), courseId);
         }
         catch (Exception ex)
         {
         	logger.debug("Exception thrown while updating calendar tool tables");
         }
  	}    
   }
 
 
 // end - mallika
  public void deleteModules(List delModules, String courseId, String userId) throws Exception
  {
 	 List cmodList = null;
 	 List<Module> allModules = new ArrayList<Module>(0);
 	  
 	 moduledb.deleteCalendar(delModules, courseId);
 	 try{
 		 allModules = moduledb.getActivenArchiveModules(courseId);
 		 moduledb.deleteModules(delModules, allModules, courseId, userId);
 	 }
 	 catch (Exception ex)
 	 {
 		 throw new MeleteException("delete_module_fail");
 	 }
 	  
 
  }
  
  
 
  /*public void deleteModules(List moduleDateBeans, String courseId, String userId)
   {
 	  List cmodList = null;
 
 	  for (ListIterator i = moduleDateBeans.listIterator(); i.hasNext(); )
       {
 		ModuleDateBean mdbean = (ModuleDateBean)i.next();
 		 try
 		  {
 		    cmodList = moduledb.getCourseModules(courseId);
 
 	      }
 		  catch (HibernateException e)
 		  {
 			//e.printStackTrace();
 			logger.error(e.toString());
 		  }
 		  for (ListIterator j = cmodList.listIterator(); j.hasNext(); )
 	      {
 			  CourseModule cmod = (CourseModule) j.next();
 			  if (cmod.getModuleId().intValue() == mdbean.getCmod().getModule().getModuleId().intValue())
 			  {
 				  try
 					{
 					   moduledb.deleteModule(cmod, userId);
 					}
 				  	catch (Exception ex)
 					{
 
 					}
 				  	break;
 			  }
 	      }
 
       }
   }*/
 
  public void archiveModules(List selModBeans, List moduleDateBeans, String courseId) throws Exception
  {
 	 List cmodList = null;
 	 try
 	 {
 		 moduledb.archiveModules(selModBeans, moduleDateBeans, courseId);
 	 }
 	 catch (HibernateException e)
 	 {
 		 //e.printStackTrace();
 		 logger.debug(e.toString());
 		 throw new MeleteException("archive_fail");
 	 }
 	 catch (Exception ex)
 	 {
 		 throw new MeleteException("archive_fail");
 	 }
  }
 
 /*
  * @see org.foothillglobalaccess.melete.ManageModuleService#getArchiveModules(int, int)
  */
 public List getArchiveModules(String course_id)
 {
 	List archModules=null;
 	try{
 		 archModules = moduledb.getArchivedModules(course_id);
 		}catch(Exception ex)
 		{
 			logger.debug("ManageModulesBusiness --get Archive Modules failed");
 		}
 		return archModules;
 }
 
 
 public ModuleObjService getModule(int moduleId) {
   	try {
   		module = moduledb.getModule(moduleId);
   	}catch (HibernateException e)
 	{
   		//e.printStackTrace();
   		logger.debug(e.toString());
 	}
   	return module;
   }
 
 public void setModule(ModuleObjService mod) {
   	module = (Module)mod;
   }
 
 
 /*
  * @see org.foothillglobalaccess.melete.ManageModuleService#restoreModules(java.util.List, int, int)
  */
 public void restoreModules(List modules, String courseId) throws Exception
 {
 
 	try{
 		 moduledb.restoreModules(modules, courseId);
 		}catch(Exception ex)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.debug("ManageModulesBusiness --restore Modules failed");
 			ex.printStackTrace();
 			}
 			throw new MeleteException(ex.toString());
 		}
 }
 
 
 	public CourseModuleService getCourseModule(int moduleId,  String courseId)
 	throws Exception{
       CourseModule cMod =null;
       try{
         cMod = moduledb.getCourseModule(moduleId,  courseId);
       }catch(Exception ex){
         logger.debug("ManageModulesBusiness --get Archive Modules failed");
        }
      return cMod;
     }
 
 
 
 	 public int getNextSeqNo(String courseId, int currSeqNo)
 	  {
 	  	int nextseq=0;
 
 	  	nextseq=moduledb.getNextSeqNo(courseId, currSeqNo);
 
 	  	return nextseq;
 	  }
 
 	 public int getPrevSeqNo(String courseId, int currSeqNo)
 	  {
 	  	int prevseq=0;
 
 	  	prevseq=moduledb.getPrevSeqNo(courseId, currSeqNo);
 
 	  	return prevseq;
 	  }
 	public org.w3c.dom.Document getSubSectionW3CDOM(String sectionsSeqXML)
 	{
 		SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 		org.w3c.dom.Document subSectionW3CDOM = ssuImpl.getSubSectionW3CDOM(sectionsSeqXML);
 		return subSectionW3CDOM;
 
 	}
 
 
 	/*METHODS USED BY MIGRATEMELETEDOCS BEGIN*/
 	/*When removing this code, also make sure to remove the service associations in components.xml
 	 * that may not be needed anymore
 	 */
 
 	//This method checks to see if we have a new installation of Melete or an upgrade
 	//It queries the MELETE_MODULE table for its count. If it is zero, it assumes a new
 	//installation. After the query, it populates MELETE_MIGRATE_STATUS.
 	//As long as MELETE_MIGRATE_STATUS is not empty, the MELETE_MODULE table will not be
 	//queried
 	public void checkInstallation() throws Exception
 	{
 		Connection dbConnection = null;
 		int start_flag=0,complete_flag=0;
 		boolean migrateTableEmpty = true;
 		boolean moduleTableEmpty = true;
 		int moduleCount = 0;
 
 		try {
 
 			dbConnection = SqlService.borrowConnection();
 	    	Statement stmt = dbConnection.createStatement();
 	    	ResultSet rs = null;
 
             //This check is in place to ensure that the migration process does not start again
 	    	String sql = "select start_flag,complete_flag from melete_migrate_status";
 	    	rs = stmt.executeQuery(sql);
 	    	if (rs != null)
 	    	{
 	    		while (rs.next())
 	    		{
 	    			start_flag = rs.getInt("start_flag");
 	    			complete_flag = rs.getInt("complete_flag");
 	    			migrateTableEmpty = false;
 	    		}
 	    		rs.close();
 	    		stmt.close();
 	    		if (migrateTableEmpty == false) return;
 	    		if (migrateTableEmpty == true)
 	    		{
 	    			sql = "select count(*) as modulecount from melete_module";
 	    			stmt = dbConnection.createStatement();
 	    			rs = stmt.executeQuery(sql);
 	    	    	if (rs != null)
 	    	    	{
 	    	    		while (rs.next())
 	    	    		{
 	    	    			moduleCount = rs.getInt("modulecount");
 	    	    			moduleTableEmpty = false;
 	    	    		}
 	    	    		rs.close();
 	    	    		stmt.close();
 	    	    		//This means it is a new installation
 	    	    		if ((moduleTableEmpty == true)||((moduleTableEmpty == false)&&(moduleCount == 0)))
 	    	    		{
 	    	    			sql = "insert into melete_migrate_status(START_FLAG,COMPLETE_FLAG) values(1,1)";
 	    	    	    	dbConnection.setAutoCommit(true);
 	    	    	    	stmt = dbConnection.createStatement();
 	    	    	    	int insRes = stmt.executeUpdate(sql);
 	    	    	    	stmt.close();
 	    	    		}
 	    	    	}
 	    		}
 	    	}
 		} catch (Exception e) {
 			if (logger.isErrorEnabled()) logger.error(e);
 			throw e;
 		} finally{
 			try{
 				if (dbConnection != null)
 					SqlService.returnConnection(dbConnection);
 			}catch (Exception e1){
 				if (logger.isErrorEnabled()) logger.error(e1);
 				throw e1;
 			}
 		}
 	}
 
 	public int getMigrateStatus() throws Exception
 	{
 		Connection dbConnection = null;
 		int start_flag=0,complete_flag=0;
 
 		try {
 
 			dbConnection = SqlService.borrowConnection();
 	    	Statement stmt = dbConnection.createStatement();
 	    	ResultSet rs = null;
 
             //This check is in place to ensure that the migration process does not start again
 	    	String sql = "select start_flag,complete_flag from melete_migrate_status";
 	    	rs = stmt.executeQuery(sql);
 	    	if (rs != null)
 	    	{
 	    		while (rs.next())
 	    		{
 	    			start_flag = rs.getInt("start_flag");
 	    			complete_flag = rs.getInt("complete_flag");
 	    		}
 	    		rs.close();
 	    		stmt.close();
 
 	    		if ((start_flag == 1)&&(complete_flag == 1))
 	    		{
 	    			return MIGRATE_COMPLETE;
 	    		}
 	    		else
 	    		{
 	    			logger.info("getMigrateStatus - The migrate process has not completed");
 	    			return MIGRATE_INCOMPLETE;
 	    		}
 	    	}
 		} catch (Exception e) {
 			if (logger.isErrorEnabled()) logger.error(e);
 			throw e;
 		} finally{
 			try{
 				if (dbConnection != null)
 					SqlService.returnConnection(dbConnection);
 			}catch (Exception e1){
 				if (logger.isErrorEnabled()) logger.error(e1);
 				throw e1;
 			}
 		}
 		return MIGRATE_INCOMPLETE;
 	}
 
 
 	/*IF YOU ARE USING ORACLE, PLEASE REPLACE THE TWO METHODS BELOW (migrateMeleteDocs and
 	 * processLicenseInformation) WITH THE ORACLE VERSION. THE ORACLE VERSION IS LOCATED
 	 * AT /patch/migrate_oracle.txt
 	 */
 	public int migrateMeleteDocs(String meleteDocsDir) throws Exception
 	{
 		Connection dbConnection = null;
 		int modId;
 		String courseId;
 		byte[] secContentData;
 		String contentEditor;
 		String secResourceName;
 		String secResourceDescription;
 		String newResourceId;
 		String addCollId = null;
 		int start_flag=0,complete_flag=0;
 		boolean processComplete = false;
 
 		try {
 
 			dbConnection = SqlService.borrowConnection();
 	    	Statement stmt = dbConnection.createStatement();
 	    	ResultSet rs = null;
 
             //This check is in place to ensure that the migration process does not start again
 	    	String sql = "select start_flag,complete_flag from melete_migrate_status";
 	    	rs = stmt.executeQuery(sql);
 	    	if (rs != null)
 	    	{
 	    		while (rs.next())
 	    		{
 	    			start_flag = rs.getInt("start_flag");
 	    			complete_flag = rs.getInt("complete_flag");
 	    		}
 	    		rs.close();
 	    		stmt.close();
 
 	    		if ((start_flag == 1)&&(complete_flag != 1))
 	    		{
 	    			logger.info("migrateMeleteDocs - The migrate process has already begun and is not yet complete");
 	    			return MIGRATE_IN_PROCESS;
 	    		}
 	    		if ((start_flag == 1)&&(complete_flag == 1))
 	    		{
 	    			return MIGRATE_COMPLETE;
 	    		}
 	    		if (!((start_flag == 0)&&(complete_flag ==0)))
 	    		{
 	    			logger.error("migrateMeleteDocs - Some other problem in migrate process");
 	    			return MIGRATE_FAILED;
 	    		}
 	    	}
 
 	    	logger.info("Migrate process begins");
 
 	    	//Set start_flag to 1 to show that the migrate process has started
 	    	sql = "insert into melete_migrate_status(START_FLAG) values(1)";
 	    	dbConnection.setAutoCommit(true);
 	    	Statement stmt1 = dbConnection.createStatement();
 	    	int insRes = stmt1.executeUpdate(sql);
 	    	logger.info("MELETE_MIGRATE_STATUS was inserted into "+insRes);
 	    	stmt1.close();
 
 
 	    	stmt = dbConnection.createStatement();
 	    	rs = null;
 
 	    	//Get all modules and their licenses
 	    	sql = "select melete_module_bkup.module_id,melete_module_bkup.license_code,melete_module_bkup.cc_license_url,melete_module_bkup.req_attr,melete_module_bkup.allow_cmrcl,melete_module_bkup.allow_mod,melete_module_bkup.created_by_fname,melete_module_bkup.created_by_lname,melete_module_bkup.creation_date,melete_course_module.course_id from melete_module_bkup,melete_course_module where melete_module_bkup.module_id=melete_course_module.module_id order by melete_course_module.course_id";
 
 			rs = stmt.executeQuery(sql);
 			List modList = null;
 			List colNames = null;
 			if (rs != null){
 			  colNames = new ArrayList();
 			  colNames.add(new String("melete_module_bkup.module_id"));
 			  colNames.add(new String("melete_module_bkup.license_code"));
 			  colNames.add(new String("melete_module_bkup.cc_license_url"));
 			  colNames.add(new String("melete_module_bkup.req_attr"));
 			  colNames.add(new String("melete_module_bkup.allow_cmrcl"));
 			  colNames.add(new String("melete_module_bkup.allow_mod"));
 			  colNames.add(new String("melete_module_bkup.created_by_fname"));
 			  colNames.add(new String("melete_module_bkup.created_by_lname"));
 			  colNames.add(new String("melete_module_bkup.creation_date"));
 			  colNames.add(new String("melete_course_module.course_id"));
 
 			  modList = toList(rs, colNames);
 			}
 
 			rs.close();
 	    	stmt.close();
 
 	    	MeleteResource meleteResource = null;
 	    	//Iterate through each module ordered by course id
 	    	if (modList != null)
 	    	{
 	    		logger.info("NUMBER OF MODULES IN THIS DATABASE IS "+modList.size());
 	    		for (ListIterator i = modList.listIterator(); i.hasNext(); ) {
 	    			Map modMap = (LinkedHashMap)i.next();
 	    		    modId = ((Integer)modMap.get("melete_module_bkup.module_id")).intValue();
 	    		    courseId = (String)modMap.get("melete_course_module.course_id");
 
                     SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
           	        stmt = dbConnection.createStatement();
 
           	        //For each module, get the sections in ascending seq order from the backup table
     	    		sql = "select section_id, content_type, content_path,upload_path,link "
    						 + " from melete_section_bkup where module_id = "
    						 + modId+ " order by seq_no ";
    					rs = stmt.executeQuery(sql);
    					if (rs != null)
    					{
    					 	if (logger.isDebugEnabled()) logger.debug("Processing sections");
    					 	//This loop executes for each section
    	          		    while (rs.next())
    	          		    {
    	          				 meleteResource = null;
    	    	    		    String res_mime_type=getMeleteCHService().MIME_TYPE_EDITOR;
    	    		            boolean encodingFlag = false;
    	    		            secResourceName=null;
    	    		            secContentData = null;
    	    		            secResourceDescription="";
    	    		            String contentType = rs.getString("content_type");
    	    		            int section_id = rs.getInt("section_id");
 
 
    	          				boolean processSection = true;
    	          				//The code below makes sure the processing continues even if there are missing files
    	          			    if (contentType.equals("typeLink"))
 						    {
 							  processSection = true;
 						    }
 						    else
 						    {
 							  if (contentType.equals("typeEditor"))
 							  {
 								//processSection = meleteExportService.checkFileExists(rs.getString("content_path"));
 								processSection = meleteUtil.checkFileExists(rs.getString("content_path"));
 							  }
 							  if (contentType.equals("typeUpload"))
 							  {
 								processSection = meleteUtil.checkFileExists(rs.getString("upload_path"));
 							  }
 						    }
 
    	          			    if (processSection == true)
    	          			    {
 
    	          				//Set license for each resource
    	          				meleteResource = new MeleteResource();
    	          				meleteResource = processLicenseInformation(modMap,meleteResource);
    	          				   //Use the info above to create resources for sections
    	          				if (contentType.equals("typeEditor"))
    	          				{
    	          				  contentEditor = new String(meleteUtil.readFromFile(new File(rs.getString("content_path"))));
    	          				  //replace image path and create image files
    	          				  //Need to get homeDirpath
   							  contentEditor = replaceImagePath(meleteDocsDir,contentEditor,courseId);
                               res_mime_type= getMeleteCHService().MIME_TYPE_EDITOR;
                               secResourceName = "Section_" + String.valueOf(section_id);
    		                      secResourceDescription="compose content";
    		                      secContentData = new byte[contentEditor.length()];
    		                      secContentData = contentEditor.getBytes();
    		                      encodingFlag = true;
    	          				}
 
 
    	  	                    newResourceId = null;
    	  	                    String checkResourceId = null;
    	  	                    //If the section is typeUpload or typeLink, check to see if its already in CH
    	  	                    if ((contentType.equals("typeUpload"))||(contentType.equals("typeLink")))
    	  	                    {
    	  	                      File fi = null;
    	  	                      if (contentType.equals("typeLink"))
 	          				  {
 	          				    secResourceName = rs.getString("link");
 	          				    if ((secResourceName != null)&&(secResourceName.trim().length() != 0))
 	          				    {
 	          				      checkResourceId = Entity.SEPARATOR + "private" + Entity.SEPARATOR + "meleteDocs" +Entity.SEPARATOR+courseId+Entity.SEPARATOR+"uploads"+Entity.SEPARATOR+Validator.escapeResourceName(secResourceName);
 	          				    }
  	          				  }
    	  	                      if (contentType.equals("typeUpload"))
    	  	                      {
    	  	                    	fi = new File(rs.getString("upload_path"));
   	          					String uploadFileName = fi.getName();
   	          				    secResourceName = uploadFileName.substring(uploadFileName.lastIndexOf("/")+1);
   	          				    if ((secResourceName != null)&&(secResourceName.trim().length() != 0))
   	          				    {
   	          				      checkResourceId = Entity.SEPARATOR + "private" + Entity.SEPARATOR + "meleteDocs" +Entity.SEPARATOR+courseId+Entity.SEPARATOR+"uploads"+Entity.SEPARATOR+secResourceName;
   	          				    }
    	  	                      }
 
    	  	                      if ((secResourceName != null)&&(secResourceName.trim().length() != 0))
    	  	                      {
    	  	                    	try
    	  	                    	{
    	  	                    	  getMeleteCHService().checkResource(checkResourceId);
    						 		  newResourceId = checkResourceId;
    						 		  if (logger.isDebugEnabled()) logger.debug("Reusing "+newResourceId);
    						 	      Section sec = (Section) sectionService.getSection(section_id);
   						          meleteResource.setResourceId(newResourceId);
   						          sectionService.insertSectionResource(sec, meleteResource);
   						          ssuImpl.addSection(String.valueOf(section_id));
    	  	                    	}
    	  	                        catch (IdUnusedException ex2)
 						        {
    	  	                          if (contentType.equals("typeLink"))
       	          				  {
    	  	                        	res_mime_type=getMeleteCHService().MIME_TYPE_LINK;
       		                        secContentData = new byte[secResourceName.length()];
       		                        secContentData = secResourceName.getBytes();
       	          				  }
       	          				  if (contentType.equals("typeUpload"))
       	          				  {
       	          				    secContentData = new byte[(int)fi.length()];
       	          				    secContentData = meleteUtil.readFromFile(new File(rs.getString("upload_path")));
       	          				    if (logger.isDebugEnabled()) logger.debug("Secresourcename is "+secResourceName);
       	                            if (logger.isDebugEnabled()) logger.debug("upload section content data " + (int)fi.length());
       	                            String file_mime_type = secResourceName.substring(secResourceName.lastIndexOf(".")+1);
       				                res_mime_type = ContentTypeImageService.getContentType(file_mime_type);
       	          				  }
    	  	                          try
       	  					      {
 
    	  	   	          			    ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(encodingFlag,secResourceName,secResourceDescription);
    	  	   	          		        addCollId = getMeleteCHService().getCollectionId(courseId,contentType,modId);
    	  	   	          			    newResourceId = getMeleteCHService().addResourceItem(secResourceName, res_mime_type,addCollId,secContentData,res );
       	  	            	        if (logger.isDebugEnabled()) logger.debug("Inserting section and resource into Melete tables");
    						            Section sec = (Section) sectionService.getSection(section_id);
    						            meleteResource.setResourceId(newResourceId);
    						            sectionService.insertMeleteResource(sec, meleteResource);
    						            ssuImpl.addSection(String.valueOf(section_id));
       	  					      }
       	  				          catch(Exception e)
       	  					      {
       	  				        	logger.error("ModuleServiceImpl migrateMeleteDocs - error in creating resource for section content");
       	  				        	throw e;
       	          			      }
 						        }
    	  	                        catch(Exception e2)
    						        {
    							      logger.error(e2.toString());
    						        }
    	  	                      } //End if secResourceName != null
    	  	                    }
    	  	                    else
    	  	                    {
    	  				          try
    	  					      {
 
    	   	          			    ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(encodingFlag,secResourceName,secResourceDescription);
    	   	          		        addCollId = getMeleteCHService().getCollectionId(courseId,contentType,modId);
    	   	          			    newResourceId = getMeleteCHService().addResourceItem(secResourceName, res_mime_type,addCollId,secContentData,res );
    	  	            	        meleteResource.setResourceId(newResourceId);
    						        if (logger.isDebugEnabled()) logger.debug("Inserting section and resource into Melete tables");
    						        Section sec = (Section) sectionService.getSection(section_id);
    						        sectionService.insertMeleteResource(sec, meleteResource);
    						        ssuImpl.addSection(String.valueOf(section_id));
    	  					      }
    	  				          catch(Exception e)
    	  					      {
    	  				        	logger.error("ModuleServiceImpl migrateMeleteDocs - error in creating resource for section content");
    	  				        	throw e;
    	          			      }
    	  	                    }
 
    	          			    }//End if processSection = true
    					     }//End while rs.next
    	          	    rs.close();
    	   			    stmt.close();
    	          	    Module module = (Module) getModule(modId);
    	                String seqXml = ssuImpl.storeSubSections();
    	                module.setSeqXml(seqXml);
    	                moduledb.updateModule(module);
 				         }//End if rs!=null
 
 
 	    	logger.info("NUMBER OF MODULES MIGRATED: "+i.nextIndex());
 	    	logger.info("NUMBER OF MODULES REMAINING: "+(modList.size() - i.nextIndex()));
 	    	}//End modlist for loop
 	    	}//End modlist != null
 	    	processComplete = true;
 
 	    	//Upon successful completion, update complete_flag in MELETE_MIGRATE_STATUS
 	    	sql = "update melete_migrate_status set COMPLETE_FLAG=1 where START_FLAG=1";
 	    	dbConnection.setAutoCommit(true);
 	    	Statement stmt2 = dbConnection.createStatement();
 	    	int updRes = stmt2.executeUpdate(sql);
 	    	logger.info("MELETE_MIGRATE_STATUS was updated "+updRes);
 	    	stmt2.close();
 		} catch (Exception e) {
 			if (logger.isErrorEnabled()) logger.error(e);
 			throw e;
 		} finally{
 			try{
 				if (dbConnection != null)
 					SqlService.returnConnection(dbConnection);
 			}catch (Exception e1){
 				if (logger.isErrorEnabled()) logger.error(e1);
 				throw e1;
 			}
 		}
 
 		if (processComplete == true)
 		{
 			return MIGRATE_COMPLETE;
 		}
 		return MIGRATE_FAILED;
 	}
 
 	private MeleteResource processLicenseInformation(Map modMap, MeleteResource meleteSectionResource)
 	{
 		String[] result = new String[2];
 		GregorianCalendar cal = new GregorianCalendar();
 		int licenseCodes = ((Integer)modMap.get("melete_module_bkup.license_code")).intValue();
 		String copyrightOwner = (String)modMap.get("melete_module_bkup.created_by_fname")+" "+(String)modMap.get("melete_module_bkup.created_by_lname");
 		cal.setTime((Date)modMap.get("melete_module_bkup.creation_date"));
 		String copyrightYear = String.valueOf(cal.get(Calendar.YEAR));
 		 if(licenseCodes == CC_CODE)
 		 	{
 		 		meleteSectionResource.setCcLicenseUrl((String)modMap.get("melete_module_bkup.cc_license_url"));
 		 		meleteSectionResource.setLicenseCode(licenseCodes);
 		 		meleteSectionResource.setReqAttr(true);
 		 		meleteSectionResource.setAllowCmrcl(((Boolean)modMap.get("melete_module_bkup.allow_cmrcl")).booleanValue());
 		 		meleteSectionResource.setAllowMod(((Integer)modMap.get("melete_module_bkup.allow_mod")).intValue());
 		 		meleteSectionResource.setCopyrightOwner(copyrightOwner);
 		 		meleteSectionResource.setCopyrightYear(copyrightYear);
 		 	}
 		 else if(licenseCodes == PD_CODE)
 		 {
 
 	 	  	meleteSectionResource.setCcLicenseUrl((String)modMap.get("melete_module_bkup.cc_license_url"));
 	 	  	meleteSectionResource.setLicenseCode(licenseCodes);
 	 	  	meleteSectionResource.setReqAttr(false);
 	 	  	meleteSectionResource.setAllowCmrcl(false);
 	 	  	meleteSectionResource.setAllowMod(0);
 	 	  	meleteSectionResource.setCopyrightOwner(copyrightOwner);
 	 		meleteSectionResource.setCopyrightYear(copyrightYear);
 		 }
 		 else if (licenseCodes == Copyright_CODE)
 		 {
 			meleteSectionResource.setCcLicenseUrl("Copyright (c) " + copyrightOwner+", " + copyrightYear);
 		 	meleteSectionResource.setLicenseCode(licenseCodes);
 		 	meleteSectionResource.setCopyrightOwner(copyrightOwner);
 	 		meleteSectionResource.setCopyrightYear(copyrightYear);
 		 }
 		 else if(licenseCodes == FU_CODE)
 		 {
 			 meleteSectionResource.setCcLicenseUrl("Copyrighted Material - subject to fair use exception");
 		 	meleteSectionResource.setLicenseCode(licenseCodes);
 		 	meleteSectionResource.setCopyrightOwner(copyrightOwner);
 	 		meleteSectionResource.setCopyrightYear(copyrightYear);
 		 }
 		 return meleteSectionResource;
 	}
 	/*END OF METHODS THAT NEED TO BE REPLACED FOR ORACLE VERSION*/
 
 
 	private static final List toList(ResultSet rs, List wantedColumnNames) throws SQLException
     {
         List rows = new ArrayList();
 
         int numWantedColumns = wantedColumnNames.size();
         while (rs.next())
         {
             Map row = new LinkedHashMap();
 
             for (int i = 0; i < numWantedColumns; ++i)
             {
                 String columnName   = (String)wantedColumnNames.get(i);
                 Object value = rs.getObject(columnName);
                 row.put(columnName, value);
             }
 
             rows.add(row);
         }
 
         return rows;
     }
 	  private String replaceImagePath(String meleteDocsDir, String secContent, String courseId) throws Exception
 	    {
 		  StringBuffer strBuf = new StringBuffer();
 			String checkforimgs = secContent;
 			int imgindex = -1;
 
 			String imgSrcPath, imgName, imgLoc, rsrcName;
 			String modifiedSecContent = new String(secContent);
 
 
 	        String replaceStr = null;
 
 			try {
 
 				 int startSrc =0;
 				int endSrc = 0;
 
 				while(checkforimgs !=null) {
 
 					ArrayList embedData = meleteUtil.findEmbedItemPattern(checkforimgs);
 	    			checkforimgs = (String)embedData.get(0);
 	    			if (embedData.size() > 1)
 	    			{
 	    				startSrc = ((Integer)embedData.get(1)).intValue();
 	    				endSrc = ((Integer)embedData.get(2)).intValue();
 	    			}
 	    			if (endSrc <= 0) break;
 
 					imgSrcPath = checkforimgs.substring(startSrc, endSrc);
 					imgName = imgSrcPath.substring(imgSrcPath.lastIndexOf("/")+1);
 
 					if(imgSrcPath.indexOf("meleteDocs") != -1 || imgSrcPath.indexOf("/access/content/group/") != -1)
 					{
 					String newEmbedResourceId = "";
 					if (imgSrcPath.indexOf("meleteDocs") != -1){
 						imgLoc = imgSrcPath.substring(imgSrcPath.indexOf("meleteDocs")+10);
 						if (logger.isDebugEnabled()) logger.debug("imgLoc is "+imgLoc);
                         rsrcName = imgSrcPath.substring(imgSrcPath.lastIndexOf("/")+1);
 
 			            try
  	                    {
 			            	  String checkResourceId = Entity.SEPARATOR + "private" + Entity.SEPARATOR + "meleteDocs" +Entity.SEPARATOR+courseId+Entity.SEPARATOR+"uploads"+Entity.SEPARATOR+rsrcName;
  	                    	  getMeleteCHService().checkResource(checkResourceId);
 					 		  newEmbedResourceId = checkResourceId;
  	                    }
  	                    catch (IdUnusedException ex2)
 				        {
  	                    	 // read data
  	                    	 byte[] data = null;
  	                    	 boolean fileExists = meleteUtil.checkFileExists(meleteDocsDir+imgLoc);
  	                    	 if (fileExists)
  	                    	 {
  				               try{
  				               File re = new File(meleteDocsDir+imgLoc);
 
  				               data = new byte[(int)re.length()];
  				               FileInputStream fis = new FileInputStream(re);
  				               fis.read(data);
  				               fis.close();
 
  				               // add as a resource to uploads collection
  	 				           String file_mime_type = imgLoc.substring(imgLoc.lastIndexOf(".")+1);
  	 				           file_mime_type = ContentTypeImageService.getContentType("file_mime_type");
 
  	 				           ResourcePropertiesEdit res =getMeleteCHService().fillEmbeddedImagesResourceProperties(rsrcName);
  	                           //get collection id where the embedded files will go
  	 					       String UploadCollId = getMeleteCHService().getUploadCollectionId(courseId);
  	 	                       newEmbedResourceId = getMeleteCHService().addResourceItem(rsrcName,file_mime_type,UploadCollId,data,res );
 
  	 	                       //add in melete resource database table also
  	 				           MeleteResource meleteResource = new MeleteResource();
  	 			               meleteResource.setResourceId(newEmbedResourceId);
  	 			               //set default license info to "I have not determined copyright yet" option
  	 			               meleteResource.setLicenseCode(0);
  	 			               sectionService.insertResource(meleteResource);
 
  				               }
  				               catch (Exception e) {
  						     	logger.error(e.toString());
  						       }
                            }
 				        }
  	                    catch(Exception e2)
 					    {
 						    logger.error(e2.toString());
 					    }
 					}
 					else if (imgSrcPath.indexOf("/access/content/group/") != -1){
 						imgLoc = imgSrcPath.substring(imgSrcPath.indexOf("/group"));
 						rsrcName = imgSrcPath.substring(imgSrcPath.lastIndexOf("/")+1);
 
 				        try
 	 	                {
 				        	  String checkResourceId = Entity.SEPARATOR + "private" + Entity.SEPARATOR + "meleteDocs" +Entity.SEPARATOR+courseId+Entity.SEPARATOR+"uploads"+Entity.SEPARATOR+rsrcName;
 	 	                	  getMeleteCHService().checkResource(checkResourceId);
 							  newEmbedResourceId = checkResourceId;
 	 	                }
 	 	                catch (IdUnusedException ex2)
 					    {
 //							 read data
 				             try{
 				               ContentResource cr = getMeleteCHService().getResource(imgLoc);
 				 	  	  	   byte[] data = new byte[cr.getContentLength()];
 				 			   data = cr.getContent();
 
 		  		               // add as a resource to uploads collection
 				               String file_mime_type = imgLoc.substring(imgLoc.lastIndexOf(".")+1);
 				               file_mime_type = ContentTypeImageService.getContentType("file_mime_type");
 
 				               ResourcePropertiesEdit res =getMeleteCHService().fillEmbeddedImagesResourceProperties(rsrcName);
                                //get collection id where the embedded files will go
 				   	           String UploadCollId = getMeleteCHService().getUploadCollectionId(courseId);
 				               newEmbedResourceId = getMeleteCHService().addResourceItem(rsrcName,file_mime_type,UploadCollId,data,res );
 
 				               //add in melete resource database table also
 				               MeleteResource meleteResource = new MeleteResource();
 			            	   meleteResource.setResourceId(newEmbedResourceId);
 			            	   //set default license info to "I have not determined copyright yet" option
 			            	   meleteResource.setLicenseCode(0);
 			            	   sectionService.insertResource(meleteResource);
 
 				         	   // in content editor replace the file found with resource reference url
 				         	   //String replaceStr = getMeleteCHService().getResourceUrl(newEmbedResourceId);
 
 			         	   }
 				           catch (Exception e) {
 					     	logger.error(e.toString());
 					       }					    }
 	 	                catch(Exception e2)
 						{
 						    logger.error(e2.toString());
 						}
 
 	 	               }
 					   ContentResource contResource = null;
 					   if ((newEmbedResourceId != null)&&(newEmbedResourceId.trim().length() > 0))
 					   {
 		         	    try
 		                {
 		                   contResource = getMeleteCHService().getResource(newEmbedResourceId);
 		                   replaceStr = contResource.getUrl();
 		                }
 		                catch (Exception e)
 		                {
 	             	       e.printStackTrace();
 	                     }
 						String patternStr = imgSrcPath;
 						Pattern pattern = Pattern.compile(Pattern.quote(patternStr));
 //						Upon import, embedded media was getting full url without code below
 						if (replaceStr.startsWith(ServerConfigurationService.getServerUrl()))
 						{
 							replaceStr = replaceStr.replace(ServerConfigurationService.getServerUrl(), "");
 						}
 						modifiedSecContent = meleteUtil.replace(modifiedSecContent,patternStr, replaceStr);
 					   }
 
 					}
 					checkforimgs =checkforimgs.substring(endSrc);
 		            startSrc=0; endSrc = 0;
 				}
 			}catch (Exception e) {
 				throw e;
 			}
 
 			return modifiedSecContent;
 
 	    }
 
 
 			/*METHODS USED BY MIGRATEMELETEDOCS END*/
 
 	  /*METHODS USED BY UPDATESEQXML BEGIN*/
 		//This method generates the XML sequence string from the module's sections
 		public boolean updateSeqXml(String courseId) throws Exception
 		{
 			int modId;
 			List  modList = getModules(courseId);
 		    	//Iterate through each course, get all modules for the course
 		    	if (modList != null)
 		    	{
 		    		if (logger.isDebugEnabled()) logger.debug("Number of modules is "+modList.size());
 		    		List secList;
 		    		Module mod = null;
 		    		for (ListIterator i = modList.listIterator(); i.hasNext(); ) {
 		    			mod = (Module)i.next();
 		    		      modId = mod.getModuleId().intValue();
 		    		    secList = null;
 		    		    try{
 		    		        //Get sections for each module;
 		    		    	secList = moduledb.getSections(modId);
 		    			}catch(Exception ex)
 		    			{
 		    				logger.debug("ModuleServiceImpl updateSeqXml - get sections failed");
 		    				throw ex;
 		    			}
 
 	          	    	//Get the meleteDocs content info for each section
 		    			if (secList != null)
 		    			{
 		    			  if (logger.isDebugEnabled()) logger.debug("Number of sections is "+secList.size());
 	                      SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 	                      for (ListIterator j = secList.listIterator(); j.hasNext(); ) {
 	          	    		  Section sec = (Section)j.next();
 	          	    		  ssuImpl.addSection(sec.getSectionId().toString());
 	   	          		  }//End for seclist loop
 	          	          String seqXml = ssuImpl.storeSubSections();
 
 	                      mod.setSeqXml(seqXml);
 	                      moduledb.updateModule(mod);
 		    	       }//End seclist != null
 		    	    }//End modlist for loop
 		    	  }//End modlist != null
 		    	return true;
 		    }
 		 /*METHODS USED BY UPDATESEQXML END*/
 
 		// clean up deleted modules
 		public int cleanUpDeletedModules() throws Exception
 		{
 			int noOfDeleted = moduledb.cleanUpDeletedModules();
 			return noOfDeleted;
 		}
 
 	/**
 	 * @return Returns the moduledb.
 	 */
 	public ModuleDB getModuledb() {
 		return moduledb;
 	}
 	/**
 	 * @param moduledb The moduledb to set.
 	 */
 	public void setModuledb(ModuleDB moduledb) {
 		this.moduledb = moduledb;
 	}
 
 	public MeleteCHService getMeleteCHService() {
 		return meleteCHService;
 	}
 
     public void setMeleteCHService(MeleteCHService meleteCHService) {
 		this.meleteCHService = meleteCHService;
     }
 
     public SectionService getSectionService() {
         return sectionService;
     }
 
     public void setSectionService(SectionService sectionService) {
         this.sectionService = sectionService;
     }
 
 	public List getViewModuleBeans()
 	{
 		return this.viewModuleBeans;
 	}
 
 	public void setViewModuleBeans(List viewModuleBeans)
 	{
 		this.viewModuleBeans = viewModuleBeans;
 	}
 
 
 	/**
 	 * @param meleteExportService
 	 *
 	 */
 //	public void setMeleteExportService(
 //			MeleteExportService meleteExportService) {
 //		this.meleteExportService = meleteExportService;
 //	}
 }
