 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.CourseModuleService;
 import org.etudes.api.app.melete.MeleteCHService;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.etudes.api.app.melete.ModuleDateBeanService;
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.ModuleShdatesService;
 import org.etudes.api.app.melete.SectionBeanService;
 import org.etudes.api.app.melete.SectionObjService;
 import org.etudes.api.app.melete.ViewModBeanService;
 import org.etudes.api.app.melete.ViewSecBeanService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.util.api.AccessAdvisor;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.StaleObjectStateException;
 import org.hibernate.Transaction;
 import org.sakaiproject.calendar.api.CalendarEvent;
 import org.sakaiproject.calendar.api.CalendarEventEdit;
 import org.sakaiproject.calendar.api.CalendarService;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.db.cover.SqlService;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.entity.cover.EntityManager;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.time.cover.TimeService;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.cover.UserDirectoryService;
 import org.sakaiproject.util.ResourceLoader;
 
 public class ModuleDB implements Serializable
 {
 
 	private class DelModuleInfo implements Comparable<DelModuleInfo>
 	{
 		String id;
 		int seq;
 
 		DelModuleInfo(String id, int seq)
 		{
 			this.id = id;
 			this.seq = seq;
 		}
 
 		public int compareTo(DelModuleInfo n)
 		{
 			if (this.seq > n.seq) return 1;
 			if (this.seq < n.seq) return -1;
 			if (this.seq == n.seq) return 0;
 			return 0;
 		}
 
 		/**
 		 * @return the id
 		 */
 		public String getId()
 		{
 			return this.id;
 		}
 
 		/**
 		 * @return the seq
 		 */
 		public int getSeq()
 		{
 			return this.seq;
 		}
 	}
 	/**
 	 * Delete directory and all its files
 	 * 
 	 * @param dir
 	 *        File
 	 * @return true if everything was deleted
 	 */
 	public static boolean deleteDir(File dir)
 	{
 		if (dir.isDirectory())
 		{
 			String[] children = dir.list();
 			if (children.length == 0)
 			{
 				dir.delete();
 				return true;
 			}
 			else
 			{
 				for (int i = 0; i < children.length; i++)
 				{
 					boolean success = deleteDir(new File(dir, children[i]));
 					if (!success)
 					{
 						return false;
 					}
 				}
 			}
 		}
 
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 	/**
 	 * @return moduleDb object
 	 */
 	public static ModuleDB getModuleDB()
 	{
 		return new ModuleDB();
 	}
 	/**
 	 * Rename directory with _del appended to name
 	 * 
 	 * @param dir
 	 *        directory to rename
 	 * @return Rename process status
 	 */
 	public static boolean renameDir(File dir)
 	{
 		String del_fname = dir.getAbsolutePath().concat("_del");
 		boolean success = dir.renameTo(new File(del_fname));
 		return success;
 	}
 	private HibernateUtil hibernateUtil;
 	/** Dependency: The logging service. */
 	private Log logger = LogFactory.getLog(ModuleDB.class);
 	private int MAX_IN_CLAUSES = 500;
 
 	private MeleteCHService meleteCHService;
 	private MeleteSecurityService meleteSecurityService;
 
 	private SpecialAccessDB saDB;
 
 	private SectionDB sectionDB;
 
 	private int seqNumber;
 
 	private MeleteUserPreferenceDB userPrefdb;
 
 	private List xmlSecList;
 
 	/** Dependency (optional, self-injected): AccessAdvisor. */
 	protected transient AccessAdvisor accessAdvisor = null;
 
 	public ModuleDB()
 	{
 		if (hibernateUtil == null) getHibernateUtil();
 	}
 
 	/**
 	 * Actually inserts a row with module information. adds a row in module , moduleshdates , course module. if a transaction fails , rollback the whole transaction.
 	 * 
 	 * @param module
 	 *        Module object
 	 * @param moduleshowdates
 	 *        Module dates object
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @throws Exception
 	 */
 	public void addModule(Module module, ModuleShdates moduleshowdates, String userId, String courseId) throws Exception
 	{
 		addModule(module, moduleshowdates, 0, userId, courseId);
 	}
 
 	/**
 	 * 
 	 * @param module
 	 * @param moduleshowdates
 	 * @param seq
 	 * @param userId
 	 * @param courseId
 	 * @throws Exception
 	 */
 	public void addModule(Module module, ModuleShdates moduleshowdates, int seq, String userId, String courseId) throws Exception
 	{
 		/*
 		 * Since Oracle silently transforms "" to nulls, we need to check to see if these non null properties are in fact null.
 		 */
 
 		hibernateUtil.ensureModuleHasNonNulls(module);
 
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 
 			try
 			{
 				module.setCreationDate(new java.util.Date());
 				module.setUserId(userId);
 				module.setModificationDate(new java.util.Date());
 				User user = UserDirectoryService.getUser(userId);
 				module.setCreatedByFname(user.getFirstName());
 				module.setCreatedByLname(user.getLastName());
 				module.setModifiedByFname(user.getFirstName());
 				module.setModifiedByLname(user.getLastName());
 				// assign sequence number
 				if (seq == 0)
 					seq = assignSequenceNumber(session, courseId);
 
 				if (!moduleshowdates.isStartDateValid()) moduleshowdates.setStartDate(null);
 				if (!moduleshowdates.isEndDateValid()) moduleshowdates.setEndDate(null);
 				
 				moduleshowdates.setModule(module);
 
 				tx = session.beginTransaction();
 				// save module
 
 				session.save(module);
 
 				// save module show dates
 				session.save(moduleshowdates);
 
 				// create instance of coursemodules
 				CourseModule coursemodule = new CourseModule();
 				coursemodule.setCourseId(courseId);
 				coursemodule.setModule(module);
 				coursemodule.setSeqNo(seq);
 				coursemodule.setDeleteFlag(false);
 
 				// save course module
 				session.save(coursemodule);
 
 				CourseModule cms = (CourseModule) module.getCoursemodule();
 				if (cms == null)
 				{
 					cms = coursemodule;
 				}
 				module.setCoursemodule(cms);
 
 				session.update(module);
 				session.flush();
 				tx.commit();
 				logger.debug("add module success" + module.getModuleId() + module.getCoursemodule().getCourseId());
 				return;
 
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				// he.printStackTrace();
 				throw he;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception ex)
 		{
 			// Throw application specific error
 			logger.error("error at module db level");
 			throw new MeleteException("add_module_fail");
 		}
 
 	}
 
 	/**
 	 * Apply days difference to all start and end dates
 	 * 
 	 * @param course_id
 	 *        Course id
 	 * @param days_diff
 	 *        Number of days
 	 */
 	public void applyBaseDateTx(String course_id, int days_diff)
 	{
 		if (course_id == null)
 		{
 			throw new IllegalArgumentException("applyBaseDateTx: course_id is null");
 		}
 		if (days_diff == 0)
 		{
 			return;
 		}
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE MELETE_MODULE_SHDATES MSH,MELETE_COURSE_MODULE MCM SET");
 		sql.append(" MSH.START_DATE=DATE_ADD(MSH.START_DATE,INTERVAL ? DAY), MSH.END_DATE=DATE_ADD(MSH.END_DATE,INTERVAL ? DAY)");
 		sql.append(" WHERE MSH.MODULE_ID=MCM.MODULE_ID AND MCM.COURSE_ID =?");
 
 		Object[] fields = new Object[3];
 		int i = 0;
 		fields[i++] = days_diff;
 		fields[i++] = days_diff;
 		fields[i++] = course_id;
 
 		if (!SqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("applyBaseDate: db write failed");
 		}
 	}
 
 	/**
 	 * Archive the selected list of modules and update sequence accordingly
 	 * 
 	 * @param selModBeans
 	 *        Selected modules
 	 * @param moduleDateBeans
 	 *        List of modules
 	 * @param courseId
 	 *        Course id
 	 * @throws Exception
 	 */
 	public void archiveModules(List<? extends ModuleDateBeanService> selModBeans, List<? extends ModuleDateBeanService> moduleDateBeans, String courseId) throws Exception
 	{
 		Transaction tx = null;
 		StringBuffer moduleIds = new StringBuffer();
 		moduleIds.append("(");
 		ModuleDateBean mdbean = null;
 		for (ListIterator<?> i = selModBeans.listIterator(); i.hasNext();)
 		{
 			mdbean = (ModuleDateBean) i.next();
 			moduleIds.append(mdbean.getModule().getModuleId().toString());
 			moduleIds.append(", ");
 		}
 		moduleIds.delete(moduleIds.toString().length() - 2, moduleIds.toString().length());
 		moduleIds.append(")");
 
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			tx = session.beginTransaction();
 
 			Date currentDate = Calendar.getInstance().getTime();
 			String updCourseModuleStr = "update CourseModule cm set cm.seqNo=-1, cm.archvFlag=1,cm.dateArchived=:currentDate where cm.moduleId in "
 					+ moduleIds.toString();
 			int updatedEntities = session.createQuery(updCourseModuleStr).setParameter("currentDate", currentDate).executeUpdate();
 			logger.debug("course module updated " + updatedEntities);
 			String updMshdatesStr = "update ModuleShdates mshdates set mshdates.addtoSchedule=0 where mshdates.moduleId in " + moduleIds.toString();
 			updatedEntities = session.createQuery(updMshdatesStr).executeUpdate();
 			logger.debug("ModuleShdates updated " + updatedEntities);
 
 			moduleDateBeans.removeAll(selModBeans);
 			List<CourseModule> courseModules = new ArrayList<CourseModule>(0);
 			for (ListIterator<?> i = moduleDateBeans.listIterator(); i.hasNext();)
 			{
 				mdbean = (ModuleDateBean) i.next();
 				courseModules.add((CourseModule) mdbean.getCmod());
 			}
 			logger.debug("Updating sequence for all other modules");
 			assignSeqs(session, courseModules);
 			tx.commit();
 		}
 		catch (HibernateException he)
 		{
 			if (tx != null) tx.rollback();
 			logger.error(he.toString());
 			throw he;
 		}
 		catch (Exception e)
 		{
 			if (tx != null) tx.rollback();
 			logger.error(e.toString());
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 				throw he;
 			}
 		}
 		List<Module> modList = new ArrayList<Module>();
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			String queryString = "from Module as mod where mod.moduleId in " + moduleIds.toString();
 			Query query = session.createQuery(queryString);
 			modList = query.list();
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		for (ListIterator<?> i = modList.listIterator(); i.hasNext();)
 		{
 			Module mod = (Module) i.next();
 			updateCalendar(mod, (ModuleShdates) mod.getModuleshdate(), courseId);
 		}
 		logger.debug("Calendar updated");
 	}
 
 	/**
 	 * Bring a section one level up in indentation
 	 * 
 	 * @param module
 	 *        Module
 	 * @param secBeans
 	 *        List of section beans
 	 * @throws MeleteException
 	 */
 	public void bringOneLevelUp(ModuleObjService module, List secBeans) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			String pattern = "\\.";
 			Integer section_id;
 			SectionBeanService secBean = null;
 			try
 			{
 				String queryString = "select module from Module as module where module.moduleId = :moduleId";
 				Module mod = (Module) session.createQuery(queryString).setParameter("moduleId", module.getModuleId()).uniqueResult();
 
 				String sectionsSeqXML = mod.getSeqXml();
 				if (sectionsSeqXML == null) throw new MeleteException("indent_left_fail");
 				SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 				if (secBeans.size() == 1)
 				{
 					secBean = (SectionBeanService) secBeans.get(0);
 					section_id = secBean.getSection().getSectionId();
 					logger.debug("bring up section " + section_id);
 					sectionsSeqXML = SectionUtil.bringOneLevelUp(sectionsSeqXML, section_id.toString());
 				}
 				else
 				{
 					for (ListIterator i = secBeans.listIterator(); i.hasNext();)
 					{
 						secBean = (SectionBeanService) i.next();
 						int occurs = secBean.getDisplaySequence().split(pattern).length - 1;
 						// Only left indent non-top level sections
 						if (occurs > 1)
 						{
 							section_id = secBean.getSection().getSectionId();
 							sectionsSeqXML = SectionUtil.bringOneLevelUp(sectionsSeqXML, section_id.toString());
 						}
 					}
 				}
 
 				mod.setSeqXml(sectionsSeqXML);
 
 				// save object
 				tx = session.beginTransaction();
 				session.saveOrUpdate(mod);
 				tx.commit();
 
 				if (logger.isDebugEnabled()) logger.debug("commiting transaction and left indenting multiple sections");
 			}
 			catch (StaleObjectStateException sose)
 			{
 				logger.error("stale object exception" + sose.toString());
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			catch (MeleteException me)
 			{
 				if (tx != null) tx.rollback();
 				throw me;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception ex)
 		{
 
 			throw new MeleteException("indent_left_fail");
 		}
 	}
 
 	/**
 	 * Check to see if there is author/edit access to course
 	 * 
 	 * @param user_id
 	 *        User id - Not in use
 	 * @param course_id
 	 *        Course id
 	 * @return
 	 */
 	public boolean checkEditAccess(String user_id, String course_id)
 	{
 		try
 		{
 			return meleteSecurityService.allowAuthor(course_id);
 		}
 		catch (Exception e)
 		{
 			return false;
 		}
 	}
 
 	/**
 	 * Clean up deleted modules, physically delete all modules with delete flag = 1
 	 * 
 	 * @return Number of modules deleted
 	 * @throws Exception
 	 */
 	public int cleanUpDeletedModules(String userId) throws Exception
 	{
 		if (!meleteSecurityService.isSuperUser(userId)) throw new MeleteException("admin_allow_cleanup");
 
 		logger.info("clean up process started");
 		int delCount = 0;
 		long totalStart = System.currentTimeMillis();
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			try
 			{
 				// get deleted modules group by course id
 				String queryString = "select cmod.courseId as courseId,cmod.moduleId as moduleId from CourseModule cmod where cmod.deleteFlag = 1 order by cmod.courseId";
 				Query query = session.createQuery(queryString);
 				List res = query.list();
 
 				Map deletedModules = new HashMap<String, ArrayList<Module>>();
 
 				for (Iterator itr = res.listIterator(); itr.hasNext();)
 				{
 					Object pair[] = (Object[]) itr.next();
 					String courseId = (String) pair[0];
 					Integer moduleId = (Integer) pair[1];
 
 					if (deletedModules.containsKey(courseId))
 					{
 						ArrayList delmodules = (ArrayList) deletedModules.get(courseId);
 						delmodules.add(moduleId);
 						deletedModules.put(courseId, delmodules);
 					}
 					else
 					{
 						ArrayList delmodule = new ArrayList();
 						delmodule.add(moduleId);
 						deletedModules.put(courseId, delmodule);
 					}
 				}
 				logger.info("Process deleted modules from " + deletedModules.size() + " sites");
 				delCount = deletedModules.size();
 				int i = 0;
 				// for each course id
 				Set alldelCourses = deletedModules.keySet();
 				for (Iterator iter = alldelCourses.iterator(); iter.hasNext();)
 				{
 					// for that course id get all melete resources from melete_resource
 					long starttime = System.currentTimeMillis();
 					String toDelCourseId = (String) iter.next();
 					logger.info("processing " + i++ + " course with id " + toDelCourseId);
 					List<? extends ModuleObjService> activenArchModules = getActivenArchiveModules(toDelCourseId);
 					List<Integer> delModules = (ArrayList) deletedModules.get(toDelCourseId);
 
 					if (activenArchModules == null || activenArchModules.size() == 0)
 					{
 						if (!session.isOpen()) session = hibernateUtil.currentSession();
 						tx = session.beginTransaction();
 
 						String allModuleIds = "(";
 						for (Integer moduleId : delModules)
 							allModuleIds = allModuleIds.concat(moduleId.toString() + ",");
 						if (allModuleIds.lastIndexOf(",") != -1) allModuleIds = allModuleIds.substring(0, allModuleIds.lastIndexOf(",")) + " )";
 
 						deleteEverything(toDelCourseId, session, allModuleIds);
 						// remove entire collection
 						meleteCHService.removeCollection(toDelCourseId, null);
 						tx.commit();
 						continue;
 					}
 					// parse and list all names which are in use
 					List<String> activeResources = getActiveResourcesFromList(activenArchModules);
 					List<String> allCourseResources = getAllMeleteResourcesOfCourse(toDelCourseId);
 					int allresourcesz = 0;
 					int delresourcesz = 0;
 					if (allCourseResources != null) allresourcesz = allCourseResources.size();
 					// compare the lists and not in use resources are
 					if (!session.isOpen()) session = hibernateUtil.currentSession();
 					tx = session.beginTransaction();
 					if (allCourseResources != null && activeResources != null)
 					{
 						// logger.debug("active resources list sz and all resources" + activeResources.size() + " ; " + allCourseResources.size());
 						allCourseResources.removeAll(activeResources);
 					}
 					// delete modules and module collection and sections marked for delete
 
 					for (Iterator delModuleIter = delModules.listIterator(); delModuleIter.hasNext();)
 					{
 						Integer delModuleId = (Integer) delModuleIter.next();
 						String allSecIds = null;
 						allSecIds = getDelSectionIds(session, delModuleId.intValue());
 
 						String selectResourceStr = "select sr.resource.resourceId from SectionResource sr where sr.section.contentType ='typeEditor' and sr.section in "
 								+ allSecIds;
 						String updSectionResourceStr = "update SectionResource sr set sr.resource = null where sr.section in " + allSecIds;
 						String delSectionResourceStr = "delete SectionResource sr where sr.section in " + allSecIds;
 						// String delSectionStr = "delete Section s where s.moduleId=:moduleId";
 						String delSectionStr = "delete Section s where s.sectionId in " + allSecIds;
 						String delCourseModuleStr = "delete CourseModule cm where cm.moduleId=:moduleId";
 						String delModuleshDatesStr = "delete ModuleShdates msh where msh.moduleId=:moduleId";
 						String delModuleStr = "delete Module m where m.moduleId=:moduleId";
 
 						if (allSecIds != null)
 						{
 							try
 							{
 								List<String> delSectionResources = session.createQuery(selectResourceStr).list();
 								// logger.debug("CHECK SECTION FILES:" + delSectionResources.toString());
 								int deletedEntities = session.createQuery(updSectionResourceStr).executeUpdate();
 								deletedEntities = session.createQuery(delSectionResourceStr).executeUpdate();
 								deletedEntities = session.createQuery(delSectionStr).executeUpdate();
 
 								if (delSectionResources != null && delSectionResources.size() > 0)
 								{
 									sectionDB.deleteResources(session, delSectionResources, false);
 
 								}
 
 							}
 							catch (Exception e)
 							{
 								logger.info("error deleting section and resources " + allSecIds + " for module" + delModuleId);
 								e.printStackTrace();
 								continue;
 							}
 						}
 						// logger.debug("deleting stuff for module" + delModuleId);
 						// int deletedEntities = session.createQuery(delSectionStr).setInteger("moduleId", delModuleId).executeUpdate();
 						int deletedEntities = session.createQuery(delCourseModuleStr).setInteger("moduleId", delModuleId).executeUpdate();
 						deletedEntities = session.createQuery(delModuleshDatesStr).setInteger("moduleId", delModuleId).executeUpdate();
 						deletedEntities = session.createQuery(delModuleStr).setInteger("moduleId", delModuleId).executeUpdate();
 						try
 						{
 							meleteCHService.removeCollection(toDelCourseId, "module_" + delModuleId.toString());
 						}
 						catch (Exception e)
 						{
 							continue;
 						}
 
 					}
 
 					// look for sections just marked for delete
 					String queryString1 = "select sec.sectionId from Section sec where sec.deleteFlag = 1 and sec.moduleId IN (select moduleId from CourseModule cm where cm.courseId=:courseId) order by sec.moduleId";
 					Query query1 = session.createQuery(queryString1);
 					query1.setString("courseId", toDelCourseId);
 					List<Section> sectionsres = query1.list();
 					// logger.debug("found extra sections marked for delete:" + sectionsres.size());
 					if (sectionsres != null && sectionsres.size() > 0)
 					{
 						String allSecIds = "(";
 
 						for (Iterator itr = sectionsres.listIterator(); itr.hasNext();)
 							allSecIds = allSecIds.concat(Integer.toString((Integer) itr.next()) + ",");
 
 						if (allSecIds.lastIndexOf(",") != -1) allSecIds = allSecIds.substring(0, allSecIds.lastIndexOf(",")) + " )";
 
 						String selectResourceStr = "select sr.resource.resourceId from SectionResource sr where sr.section.contentType ='typeEditor' and sr.section in "
 								+ allSecIds;
 						String updSectionResourceStr = "update SectionResource sr set sr.resource = null where sr.section in " + allSecIds;
 						String delSectionResourceStr = "delete SectionResource sr where sr.section in " + allSecIds;
 						String delSectionStr = "delete Section s where s.sectionId in " + allSecIds;
 
 						try
 						{
 							List<String> delSectionResources = session.createQuery(selectResourceStr).list();
 							int deletedEntities = session.createQuery(updSectionResourceStr).executeUpdate();
 							deletedEntities = session.createQuery(delSectionResourceStr).executeUpdate();
 							deletedEntities = session.createQuery(delSectionStr).executeUpdate();
 
 							if (delSectionResources != null && delSectionResources.size() > 0)
 							{
 								sectionDB.deleteResources(session, delSectionResources, true);
 							}
 
 							// logger.debug("sucess remove of deleted sections" + deletedEntities);
 						}
 						catch (Exception e)
 						{
 							e.printStackTrace();
 							logger.info("error deleting extra section and resources " + allSecIds);
 						}
 
 					}
 
 					if ((allCourseResources != null) && (allCourseResources.size() > 0))
 					{
 						sectionDB.deleteResources(session, allCourseResources, true);
 					}
 					// if course collection is empty than delete course collection
 					meleteCHService.removeCourseCollection(toDelCourseId);
 
 					tx.commit();
 					long endtime = System.currentTimeMillis();
 					logger.info("to cleanup course with " + allresourcesz + " resources and del modules " + delModules.size() + " and del resources"
 							+ delresourcesz + ", it took " + (endtime - starttime) + "ms");
 				} // for end
 				long totalend = System.currentTimeMillis();
 				logger.info("to cleanup deleted modules from " + deletedModules.size() + "courses it took " + (totalend - totalStart) + "ms");
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw new MeleteException("cleanup_module_fail");
 		}
 		return delCount;
 	}
 
 	/**
 	 * Create a copy of this module
 	 * 
 	 * @param module
 	 *        Module object
 	 * @param courseId
 	 *        Course id
 	 * @param userId
 	 *        User id
 	 * @throws MeleteException
 	 */
 	public void copyModule(Module module, String courseId, String userId) throws MeleteException
 	{
 		ResourceLoader bundle = new ResourceLoader("melete_license");
 
 		try
 		{
 			// get module and its sections
 			Module copyMod = new Module(module);
 			String firstName = UserDirectoryService.getUser(userId).getFirstName();
 			String lastName = UserDirectoryService.getUser(userId).getLastName();
 
 			DateFormat shortTime = DateFormat.getDateInstance(DateFormat.LONG);
 
 			copyMod.setCreatedByFname(firstName);
 			copyMod.setCreatedByLname(lastName);
 			copyMod.setTitle(copyMod.getTitle() + " (" + bundle.getString("Copied") + " " + shortTime.format(new Date()) + " )");
 			ModuleShdates CopyModuleshowdates = new ModuleShdates((ModuleShdates) module.getModuleshdate());
 
 			// insert copy module with blank seq_xml and sections as null
 			addModule(copyMod, CopyModuleshowdates, userId, courseId);
 
 			String copyModSeqXml = module.getSeqXml();
 			// get sections
 			List<Section> toCopySections = getSections(module.getModuleId().intValue());
 			if (toCopySections != null && toCopySections.size() > 0)
 			{
 				for (Section toCopySection : toCopySections)
 				{
 					// with title as copy of xxx and sectionResource
 					Section copySection = new Section(toCopySection);
 					copySection.setCreatedByFname(firstName);
 					copySection.setCreatedByLname(lastName);
 					copySection.setModifiedByFname(firstName);
 					copySection.setModifiedByLname(lastName);
 					copySection.setModule(copyMod);
 					copySection.setTitle(copySection.getTitle() + " (" + bundle.getString("Copied") + " " + shortTime.format(new Date()) + " )");
 					// insert section
 					Integer copySectionId = sectionDB.addSection(copyMod, copySection, false);
 					copySection.setSectionId(copySectionId);
 					// copySection.setModule(copyMod);
 					if (toCopySection.getContentType() != null && !toCopySection.getContentType().equals("notype"))
 					{
 						// if section content type is composed than create a new copy
 						if (toCopySection.getContentType().equals("typeEditor"))
 						{
 
 							String copyModCollId = meleteCHService.getCollectionId(courseId, "typeEditor", copyMod.getModuleId());
 							String res_mime_type = meleteCHService.MIME_TYPE_EDITOR;
 							ContentResource cr = meleteCHService.getResource(toCopySection.getSectionResource().getResource().getResourceId());
 							byte[] secContentData = cr.getContent();
 
 							boolean encodingFlag = true;
 							String secResourceName = meleteCHService.getTypeEditorSectionName(copySectionId);
 							String secResourceDescription = "compose content";
 
 							ResourcePropertiesEdit res = meleteCHService.fillInSectionResourceProperties(encodingFlag, secResourceName,
 									secResourceDescription);
 							String newResourceId = meleteCHService
 									.addResourceItem(secResourceName, res_mime_type, copyModCollId, secContentData, res);
 
 							MeleteResource copyMelResource = new MeleteResource((MeleteResource) toCopySection.getSectionResource().getResource());
 							copyMelResource.setResourceId(newResourceId);
 							sectionDB.insertMeleteResource(copySection, copyMelResource);
 						}
 						else if (toCopySection.getSectionResource() != null)
 						{
 							// insert section resource with same melete resource
 							MeleteResource copyMr = (MeleteResource) toCopySection.getSectionResource().getResource();
 							if (copyMr != null) sectionDB.insertSectionResource(copySection, copyMr);
 						}
 					}
 					// replace with new copied section
 					copyModSeqXml = copyModSeqXml.replace(toCopySection.getSectionId().toString(), copySectionId.toString());
 				}
 			}
 			// update module seq xml
 			Module copyMod1 = getModule(copyMod.getModuleId());
 			copyMod1.setSeqXml(copyModSeqXml);
 			updateModule(copyMod1);
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 			throw new MeleteException("copy_fail");
 		}
 	}
 
 	/**
 	 * Indent some sections in a module
 	 * 
 	 * @param module
 	 *        Module object
 	 * @param secBeans
 	 *        List of sections to indent
 	 * @throws MeleteException
 	 */
 	public void createSubSection(ModuleObjService module, List secBeans) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			String pattern = "\\.";
 			Integer section_id;
 			SectionBeanService secBean = null;
 			try
 			{
 				//get module object again
 				String queryString = "select module from Module as module where module.moduleId = :moduleId";
 				Module mod = (Module) session.createQuery(queryString).setParameter("moduleId", module.getModuleId()).uniqueResult();
 
 				String sectionsSeqXML = mod.getSeqXml();
 				if (sectionsSeqXML == null) throw new MeleteException("indent_right_fail");
 				SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 
 				for (ListIterator i = secBeans.listIterator(); i.hasNext();)
 				{
 					secBean = (SectionBeanService) i.next();
 					section_id = secBean.getSection().getSectionId();
 					logger.debug("indenting section " + section_id);
 					sectionsSeqXML = SectionUtil.MakeSubSection(sectionsSeqXML, section_id.toString());
 				}
 
 				mod.setSeqXml(sectionsSeqXML);
 
 				// save object
 				tx = session.beginTransaction();
 				session.saveOrUpdate(mod);
 				tx.commit();
 
 				if (logger.isDebugEnabled()) logger.debug("commiting transaction and indenting multiple sections");
 			}
 			catch (StaleObjectStateException sose)
 			{
 				logger.error("stale object exception" + sose.toString());
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			catch (MeleteException me)
 			{
 				if (tx != null) tx.rollback();
 				throw me;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception ex)
 		{
 
 			throw new MeleteException("indent_right_fail");
 		}
 	}
 	
 	/**
 	 * 
 	 * @param module
 	 * @param sectionId
 	 * @throws MeleteException
 	 */
 	public void createSubSection(ModuleObjService module, String sectionId) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			try
 			{
 				//get module object again
 				String queryString = "select module from Module as module where module.moduleId = :moduleId";
 				Module mod = (Module) session.createQuery(queryString).setParameter("moduleId", module.getModuleId()).uniqueResult();
 
 				String sectionsSeqXML = mod.getSeqXml();
 				if (sectionsSeqXML == null) throw new MeleteException("indent_right_fail");
 				SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 
 				sectionsSeqXML = SectionUtil.MakeSubSection(sectionsSeqXML, sectionId);
 				mod.setSeqXml(sectionsSeqXML);
 
 				// save object
 				tx = session.beginTransaction();
 				session.saveOrUpdate(mod);
 				tx.commit();
 
 				if (logger.isDebugEnabled()) logger.debug("commiting transaction and indenting a section");
 			}
 			catch (StaleObjectStateException sose)
 			{
 				logger.error("stale object exception" + sose.toString());
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			catch (MeleteException me)
 			{
 				if (tx != null) tx.rollback();
 				throw me;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception ex)
 		{
 
 			throw new MeleteException("indent_right_fail");
 		}
 	}
 
 	/**
 	 * Create truncated title when string is over 30 chars
 	 * 
 	 * @param modTitle
 	 *        string to truncate
 	 * @return truncated string
 	 */
 	public String createTruncstr(String modTitle)
 	{
 		String truncTitle = null;
 		if (modTitle.length() <= 30) return modTitle.trim();
 		if (modTitle.length() > 30)
 		{
 			truncTitle = modTitle.substring(0, 27);
 			truncTitle = truncTitle.concat("...");
 		}
 		return truncTitle;
 	}
 
 	/**
 	 * Delete modules of a course
 	 * 
 	 * @param delModules
 	 *        Modules to delete
 	 * @param allModules
 	 *        List of all modules
 	 * @param courseId
 	 *        Course id
 	 * @param userId
 	 *        User id not in use
 	 * @throws Exception
 	 */
 	public void deleteModules(List<? extends ModuleObjService> delModules, List<? extends ModuleObjService> allModules, String courseId, String userId)
 			throws Exception
 	{
 		long starttime = System.currentTimeMillis();
 		Transaction tx = null;
 
 		// If not all modules of the course need to be deleted
 		if (delModules.size() != allModules.size())
 		{
 			logger.debug("delete some Modules begin");
 			ArrayList<DelModuleInfo> DelModuleInfoList = new ArrayList<DelModuleInfo>(0);
 			List<String> delResourcesList;
 			try
 			{
 				// Get resources for modules that need to be deleted
 				delResourcesList = getActiveResourcesFromList(delModules);
 				if ((delResourcesList != null) && (delResourcesList.size() > 0))
 				{
 					List<String> allActiveResources = getActiveResourcesFromList(allModules);
 
 					if (allActiveResources != null && delResourcesList != null)
 					{
 						logger.debug("active list and all" + delResourcesList.size() + " ; " + allActiveResources.size());
 						delResourcesList.removeAll(allActiveResources);
 					}
 				}
 
 				// get all module-ids and section_ids
 				// update seq_no for each deleted_module
 				StringBuffer allModuleIds = new StringBuffer("(");
 				StringBuffer allSectionIds = new StringBuffer("(");
 				ArrayList<StringBuffer> allSectionIdsArray = new ArrayList<StringBuffer>();
 				String delModuleIds = null;
 				// String delSectionIds = null;
 				int count = 1;
 				for (Iterator<? extends ModuleObjService> dmIter = delModules.iterator(); dmIter.hasNext();)
 				{
 					Module dm = (Module) dmIter.next();
					allModules.remove(dm);
 					allModuleIds.append(dm.getModuleId().toString() + ",");
 					Map<Integer, SectionObjService> delSections = dm.getSections();
 					if (delSections != null && !delSections.isEmpty())
 					{
 						for (Iterator<Integer> i = delSections.keySet().iterator(); i.hasNext();)
 						{
 							if (count % MAX_IN_CLAUSES == 0)
 							{
 								allSectionIds.append(i.next() + ")");
 								allSectionIdsArray.add(allSectionIds);
 								allSectionIds = new StringBuffer("(");
 							}
 							else
 							{
 								allSectionIds.append(i.next() + ",");
 							}
 							count++;
 						}
 					}
 
 					Map<Integer, SectionObjService>  delDeletedSections = dm.getDeletedSections();
 					if (delDeletedSections != null && !delDeletedSections.isEmpty())
 					{
 						for (Iterator<Integer> i1 = delDeletedSections.keySet().iterator(); i1.hasNext();)
 						{
 							if (count % MAX_IN_CLAUSES == 0)
 							{
 								allSectionIds.append(i1.next() + ")");
 								allSectionIdsArray.add(allSectionIds);
 								allSectionIds = new StringBuffer("(");
 							}
 							else
 							{
 								allSectionIds.append(i1.next() + ",");
 							}
 							count++;
 						}
 					}
 
 					// record seq_no and id
 					DelModuleInfoList.add(new DelModuleInfo(dm.getModuleId().toString(), dm.getCoursemodule().getSeqNo()));
 				}
 
 				if (allModuleIds.lastIndexOf(",") != -1) delModuleIds = allModuleIds.substring(0, allModuleIds.lastIndexOf(",")) + ")";
 
 				// if (allSectionIds.lastIndexOf(",") != -1) delSectionIds = allSectionIds.substring(0, allSectionIds.lastIndexOf(",")) + " )";
 				if (allSectionIds.lastIndexOf(",") != -1)
 				{
 					if (count % MAX_IN_CLAUSES != 0)
 					{
 						allSectionIds.replace(allSectionIds.lastIndexOf(","), allSectionIds.lastIndexOf(",") + 1, ")");
 						allSectionIdsArray.add(allSectionIds);
 					}
 				}
 
 				Session session = hibernateUtil.currentSession();
 				tx = session.beginTransaction();
 
 				String delMeleteResourceStr;
 				int deletedEntities;
 
 				// delete modules and sections
 				String updSectionResourceStr = "update SectionResource sr set sr.resource = null where sr.section.sectionId in ";
 				String delSectionResourceStr = "delete SectionResource sr where sr.section.sectionId in ";
 				String delBookmarksStr = "delete Bookmark bm where bm.sectionId in ";
 				String delSectionTrackStr = "delete SectionTrackView stv where stv.sectionId in ";
 				String delSectionStr = "delete Section s where s.moduleId in " + delModuleIds;
 				String delCourseModuleStr = "delete CourseModule cm where cm.moduleId in " + delModuleIds;
 				String delModuleshDatesStr = "delete ModuleShdates msh where msh.moduleId in " + delModuleIds;
 				String delSpecialAccStr = "delete SpecialAccess sa where sa.moduleId in " + delModuleIds;
 				String delModuleStr = "delete Module m where m.moduleId in " + delModuleIds;
 
 				if (allSectionIdsArray != null)
 				{
 					for (int i = 0; i < allSectionIdsArray.size(); i++)
 					{
 						allSectionIds = allSectionIdsArray.get(i);
 			
 						try
 						{
 							deletedEntities = session.createQuery(updSectionResourceStr + allSectionIds.toString()).executeUpdate();
 							deletedEntities = session.createQuery(delSectionResourceStr + allSectionIds.toString()).executeUpdate();
 							logger.debug("section resource deleted" + deletedEntities);
 							deletedEntities = session.createQuery(delBookmarksStr + allSectionIds.toString()).executeUpdate();
 							logger.debug("Boomkarks deleted " + deletedEntities);
 							deletedEntities = session.createQuery(delSectionTrackStr + allSectionIds.toString()).executeUpdate();
 							logger.debug("Section track records deleted " + deletedEntities);
 							session.flush();
 						}
 						catch (HibernateException he)
 						{
 							logger.error("Error in deleting one of these section resources :" + allSectionIds );
 							logger.error(he.toString());
 							he.printStackTrace();
 							throw he;
 						}
 						catch (Exception e)
 						{
 							logger.error("Error in deleting one of these section resources :" + allSectionIds );
 							logger.error(e.toString());
 							e.printStackTrace();
 							throw e;
 						}
 					
 					}
 				}
 		    	if (delModuleIds != null)
 				{
 					try
 					{
 						deletedEntities = session.createQuery(delSectionStr).executeUpdate();
 						logger.debug("section deleted" + deletedEntities);
 						deletedEntities = session.createQuery(delCourseModuleStr).executeUpdate();
 						logger.debug("course module deleted" + deletedEntities);
 						deletedEntities = session.createQuery(delModuleshDatesStr).executeUpdate();
 						deletedEntities = session.createQuery(delSpecialAccStr).executeUpdate();
 						logger.debug("special access deleted" + deletedEntities);
 						deletedEntities = session.createQuery(delModuleStr).executeUpdate();
 						logger.debug("module deleted" + deletedEntities);
 					}
 					catch (HibernateException he)
 					{
 						logger.error("Error in deleting module :" + delModuleIds);
 						logger.error(he.toString());
 						he.printStackTrace();
 						throw he;
 					}
 					catch (Exception e)
 					{
 						logger.error("Error in deleting module :" + delModuleIds);
 						logger.error(e.toString());
 						e.printStackTrace();
 						throw e;
 					}
 			    }
 
 				// delete module collection
 
 				logger.debug("updating seq_number now");
 				List<CourseModule> courseModules = new ArrayList<CourseModule>(0);
 				for (ListIterator<? extends ModuleObjService> i = allModules.listIterator(); i.hasNext();)
 				{
 					Module mdbean = (Module) i.next();
 					courseModules.add((CourseModule) mdbean.getCoursemodule());
 				}
 				assignSeqs(session, courseModules);
 
 				// delete resources
 				if ((delResourcesList != null) && (delResourcesList.size() > 0))
 				{
 					StringBuffer delResourceIds = new StringBuffer("(");
 					// delete melete resource and from content resource
 					for (Iterator<String> delIter = delResourcesList.listIterator(); delIter.hasNext();)
 					{
 						String delResourceId = (String) delIter.next();
 						if ((delResourceId == null) || (delResourceId.trim().length() == 0))
 						{
 							logger.warn("NULL or empty resource id found in delete process ");
 							continue;
 						}
 						delResourceIds.append("'" + delResourceId + "',");
 					}
 
 					// Ensuring that there are no empty resource ids
 					if ((delResourceIds.length() > 4) && (delResourceIds.lastIndexOf(",") != -1))
 					{
 						delResourceIds = new StringBuffer(delResourceIds.substring(0, delResourceIds.lastIndexOf(",")) + " )");
 						delMeleteResourceStr = "delete MeleteResource mr where mr.resourceId in " + delResourceIds;
 						deletedEntities = session.createQuery(delMeleteResourceStr).executeUpdate();
 						logger.debug("melete resource deleted" + deletedEntities);
 					}
 				}
 				tx.commit();
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			catch (Exception e)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(e.toString());
 				e.printStackTrace();
 				throw e;
 			}
 			finally
 			{
 				try
 				{
 					hibernateUtil.closeSession();
 				}
 				catch (HibernateException he)
 				{
 					logger.error(he.toString());
 					throw he;
 				}
 			}
 
 			logger.debug("Successfully cleared Melete tables");
 			logger.debug("Removing module collections now");
 			Collections.reverse(DelModuleInfoList);
 			for (DelModuleInfo dmi : DelModuleInfoList)
 			{
 				meleteCHService.removeCollection(courseId, "module_" + dmi.getId());
 			}
 
 			logger.debug("Removing upload collection resources");
 			for (Iterator<String> delIter = delResourcesList.listIterator(); delIter.hasNext();)
 			{
 				String delResourceId = (String) delIter.next();
 				if ((delResourceId == null) || (delResourceId.trim().length() == 0))
 				{
 					logger.warn("NULL or empty resource id found in delete process ");
 					continue;
 				}
 				// TypeEditor sections will have been removed already
 				if (delResourceId.startsWith("/private/meleteDocs/" + courseId + "/uploads/"))
 				{
 					meleteCHService.removeResource(delResourceId);
 				}
 			}
 
 			long endtime = System.currentTimeMillis();
 
 			logger.debug("delete some modules ends " + (endtime - starttime));
 		}
 		else
 		{
 			logger.debug("delete all Modules begin");
 			try
 			{
 				Session session = hibernateUtil.currentSession();
 				tx = session.beginTransaction();
 				StringBuffer allModuleIds = new StringBuffer("(");
 				String delModuleIds = null;
 				for (Iterator<? extends ModuleObjService> dmIter = delModules.iterator(); dmIter.hasNext();)
 				{
 					Module dm = (Module) dmIter.next();
 					allModuleIds.append(dm.getModuleId().toString() + ",");
 				}
 				if (allModuleIds.lastIndexOf(",") != -1) delModuleIds = allModuleIds.substring(0, allModuleIds.lastIndexOf(",")) + " )";
 				deleteEverything(courseId, session, delModuleIds);
 				// remove entire collection
 				try
 				{
 					meleteCHService.removeCollection(courseId, null);
 				}
 				catch (Exception removeColl)
 				{
 					// do nothing
 				}
 				tx.commit();
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			catch (Exception e)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(e.toString());
 				e.printStackTrace();
 				throw e;
 			}
 			finally
 			{
 				try
 				{
 					hibernateUtil.closeSession();
 				}
 				catch (HibernateException he)
 				{
 					logger.error(he.toString());
 					throw he;
 				}
 			}
 			long endtime = System.currentTimeMillis();
 
 			logger.debug("delete all modules ends " + (endtime - starttime));
 
 		}
 	}
 
 	/**
 	 * Get all active and archived modules
 	 * 
 	 * @param courseId
 	 *        Course id
 	 * @return List of active and archived modules
 	 * @throws HibernateException
 	 */
 	public List<? extends  ModuleObjService> getActivenArchiveModules(String courseId) throws HibernateException
 	{
 		List<? extends  ModuleObjService> modList = new ArrayList<ModuleObjService>();
 	
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 
 			String queryString = "from Module module where module.coursemodule.courseId = :courseId  and module.coursemodule.deleteFlag = 0 order by module.coursemodule.seqNo";
 
 			Query query = session.createQuery(queryString);
 			query.setParameter("courseId", courseId);
 
 			modList = query.list();
 
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		return modList;
 	}
 
 	/**
 	 * Get archived modules for this course
 	 * 
 	 * @param course_id
 	 *        course id
 	 * @return list of archived modules
 	 */
 	public List< CourseModuleService> getArchivedModules(String course_id)
 	{
 		List<CourseModuleService> archModules = new ArrayList<CourseModuleService>();
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Query q = session.createQuery("select cm from CourseModule cm where cm.courseId =:course_id and cm.archvFlag=1 order by cm.dateArchived");
 			q.setParameter("course_id", course_id);
 
 			archModules = q.list();
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		return archModules;
 	}
 
 	/**
 	 * Get course module object for this module and course
 	 * 
 	 * @param moduleId
 	 *        Module id
 	 * @param courseId
 	 *        Course id
 	 * @return course module object
 	 * @throws HibernateException
 	 */
 	public CourseModule getCourseModule(int moduleId, String courseId) throws HibernateException
 	{
 		CourseModule cmod = null;
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			String queryString = "select cmod from CourseModule as cmod where cmod.module.moduleId = :moduleId  and cmod.courseId = :courseId";
 			Query query = session.createQuery(queryString);
 			query.setParameter("moduleId", new Integer(moduleId));
 			query.setParameter("courseId", courseId);
 			cmod = (CourseModule) query.uniqueResult();
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		return cmod;
 	}
 
 	/**
 	 * Get course modules for this course
 	 * 
 	 * @param courseId
 	 *        Course id
 	 * @return List of course modules
 	 * @throws HibernateException
 	 */
 	public List getCourseModules(String courseId) throws HibernateException
 	{
 		List cmodList = new ArrayList();
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			String queryString = "from CourseModule as cmod where cmod.courseId = :courseId and cmod.deleteFlag = 0";
 			Query query = session.createQuery(queryString);
 			query.setParameter("courseId", courseId);
 			cmodList = query.list();
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		return cmodList;
 	}
 
 	/**
 	 * Get a count of all active(unarchived) modules
 	 * 
 	 * @param courseId
 	 *        Course id
 	 * @return Number of all active(unarchived) modules
 	 */
 	public int getCourseModuleSize(String courseId)
 	{
 		Integer size = new Integer(0);
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			String queryString = "select count(*) from CourseModule as cmod where cmod.courseId = :courseId and cmod.archvFlag = 0 and cmod.deleteFlag = 0";
 			Query query = session.createQuery(queryString);
 			query.setParameter("courseId", courseId);
 			size = (Integer) query.uniqueResult();
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		return size.intValue();
 	}
 
 	/**
 	 * @return Returns the hibernateUtil.
 	 */
 	public HibernateUtil getHibernateUtil()
 	{
 		return hibernateUtil;
 	}
 
 	/**
 	 * Get the minimum date of all modules in the course(may be start or end)
 	 * 
 	 * @param course_id
 	 *        Course id
 	 * @return Minimum(earliest) start or end date of course
 	 */
 	public Date getMinStartDate(String course_id)
 	{
 		Date minStartDate = null;
 		try
 		{
 			List modList = getModules(course_id);
 			Iterator i = modList.iterator();
 			List dateList = new ArrayList();
 
 			// Create a list of just the non-null dates
 			while (i.hasNext())
 			{
 				Module mod = (Module) i.next();
 				if (mod.getModuleshdate() != null)
 				{
 					if (mod.getModuleshdate().getStartDate() != null)
 					{
 						dateList.add(mod.getModuleshdate().getStartDate());
 					}
 					if (mod.getModuleshdate().getEndDate() != null)
 					{
 						dateList.add(mod.getModuleshdate().getEndDate());
 					}
 				}
 			}
 
 			if (dateList != null)
 			{
 				if (dateList.size() > 0)
 				{
 					i = dateList.iterator();
 					if (i.hasNext())
 					{
 						minStartDate = (Date) i.next();
 						while (i.hasNext())
 						{
 							Date modDate = (Date) i.next();
 							if (modDate.before(minStartDate))
 							{
 								minStartDate = modDate;
 							}
 						}
 					}
 				}
 			}
 		}
 		catch (Exception he)
 		{
 			logger.error(he.toString());
 			he.printStackTrace();
 		}
 		return minStartDate;
 	}
 
 	/**
 	 * Get the maximum date of all modules in the course(may be start or end)
 	 * 
 	 * @param course_id
 	 *        Course id
 	 * @return Maximum(latest) start or end date of course
 	 */
 	public Date getMaxStartDate(String course_id)
 	{
 		Date maxStartDate = null;
 		try
 		{
 			List modList = getModules(course_id);
 			Iterator i = modList.iterator();
 			List dateList = new ArrayList();
 
 			// Create a list of just the non-null dates
 			while (i.hasNext())
 			{
 				Module mod = (Module) i.next();
 				if (mod.getModuleshdate() != null)
 				{
 					if (mod.getModuleshdate().getStartDate() != null)
 					{
 						dateList.add(mod.getModuleshdate().getStartDate());
 					}
 					if (mod.getModuleshdate().getEndDate() != null)
 					{
 						dateList.add(mod.getModuleshdate().getEndDate());
 					}
 				}
 			}
 
 			if (dateList != null)
 			{
 				if (dateList.size() > 0)
 				{
 					i = dateList.iterator();
 					if (i.hasNext())
 					{
 						maxStartDate = (Date) i.next();
 						while (i.hasNext())
 						{
 							Date modDate = (Date) i.next();
 							if (modDate.after(maxStartDate))
 							{
 								maxStartDate = modDate;
 							}
 						}
 					}
 				}
 			}
 		}
 		catch (Exception he)
 		{
 			logger.error(he.toString());
 			he.printStackTrace();
 		}
 		return maxStartDate;
 	}
 
 	/**
 	 * Get module from module id
 	 * 
 	 * @param moduleId
 	 *        Module id
 	 * @return module object
 	 * @throws HibernateException
 	 */
 	public Module getModule(int moduleId) throws HibernateException
 	{
 		Module mod = null;
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			String queryString = "select module from Module as module where module.moduleId = :moduleId";
 			mod = (Module) session.createQuery(queryString).setParameter("moduleId", new Integer(moduleId)).uniqueResult();
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		return mod;
 	}
 
 	/**
 	 * Get moduledatebean object
 	 * 
 	 * @param userId
 	 *        User id no longer in use
 	 * @param courseId
 	 *        Course id
 	 * @param moduleId
 	 *        Module id
 	 * @return Moduledatebean object
 	 * @throws HibernateException
 	 */
 	public ModuleDateBean getModuleDateBean(String userId, String courseId, int moduleId) throws HibernateException
 	{
 		List modList = new ArrayList();
 		Module mod = null;
 		ModuleDateBean mdBean = null;
 
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 
 			// String queryString = "from Module module where module.moduleId = :moduleId and module.coursemodule.courseId = :courseId  and module.coursemodule.archvFlag = 0 and module.coursemodule.deleteFlag = 0 order by module.coursemodule.seqNo";
 			String queryString = "from CourseModule cmod where cmod.moduleId = :moduleId and cmod.courseId = :courseId  and cmod.archvFlag = 0 and cmod.deleteFlag = 0 order by cmod.seqNo";
 			Query query = session.createQuery(queryString);
 			query.setParameter("moduleId", new Integer(moduleId));
 			query.setParameter("courseId", courseId);
 
 			modList = query.list();
 			Iterator i = modList.iterator();
 			while (i.hasNext())
 			{
 				CourseModule cmod = (CourseModule) i.next();
 				mdBean = new ModuleDateBean();
 				mod = (Module) cmod.getModule();
 				populateModuleBean(mod, mdBean);
 
 			}
 		}
 		catch (Exception he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		if (mdBean == null) mdBean = new ModuleDateBean();
 		return mdBean;
 	}
 
 	/**
 	 * Get module date bean by sequence
 	 * 
 	 * @param userId
 	 *        User id no longer in user
 	 * @param courseId
 	 *        Course id
 	 * @param seqNo
 	 *        Sequence number
 	 * @return Moduledatebean object
 	 * @throws HibernateException
 	 */
 	public ModuleDateBean getModuleDateBeanBySeq(String userId, String courseId, int seqNo) throws HibernateException
 	{
 		List modList = new ArrayList();
 		Module mod = null;
 		ModuleDateBean mdBean = null;
 
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			String queryString = "from CourseModule cmod where cmod.courseId = :courseId and cmod.seqNo = :seqNo  and cmod.archvFlag = 0 and cmod.deleteFlag = 0 order by cmod.seqNo";
 
 			Query query = session.createQuery(queryString);
 			query.setParameter("seqNo", new Integer(seqNo));
 			query.setParameter("courseId", courseId);
 
 			modList = query.list();
 			Iterator i = modList.iterator();
 			while (i.hasNext())
 			{
 				CourseModule cmod = (CourseModule) i.next();
 				mdBean = new ModuleDateBean();
 				mod = (Module) cmod.getModule();
 				populateModuleBean(mod, mdBean);
 
 			}
 		}
 		catch (Exception he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		if (mdBean == null) mdBean = new ModuleDateBean();
 		return mdBean;
 	}
 
 	/**
 	 * Core method to get modules
 	 * 
 	 * @param courseId
 	 *        course id
 	 * @return list of modules
 	 * @throws HibernateException
 	 */
 	public List<ModuleObjService> getModules(String courseId) throws HibernateException
 	{
 		List<ModuleObjService>  modList = new ArrayList<ModuleObjService>();
 		List<? extends  CourseModuleService> cmodList = new ArrayList<CourseModuleService>();
 
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 
 			String queryString = "select cmod from CourseModule as cmod where courseId = :courseId  and archvFlag = 0 and deleteFlag = 0 order by seqNo";
 
 			Query query = session.createQuery(queryString);
 			query.setParameter("courseId", courseId);
 
 			cmodList = query.list();
 			Iterator<?> i = cmodList.iterator();
 			while (i.hasNext())
 			{
 				CourseModule cmod = (CourseModule) i.next();
 				modList.add(cmod.getModule());
 			}
 
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		return modList;
 	}
 
 	/**
 	 * Get next sequence number (after this module in the course) This method is invoked for instructors and students
 	 * It skips invalid modules.
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @param currSeqNo
 	 *        Current seq no
 	 * @return Next seq no
 	 */
 	public int getNextSeqNo(String userId, String courseId, int currSeqNo)
 	{
 		int nextSeqNo = -1;
 		String queryStr = null;
 		try
 		{
 			if (meleteSecurityService.allowAuthor(courseId))
 			{
 				queryStr = "select cm.seqNo, count(s.moduleId) from CourseModule cm, Section s, ModuleShdates ms where cm.courseId =:courseId and cm.deleteFlag=0 and cm.archvFlag=0 and cm.seqNo > :currSeqNo and cm.moduleId = s.moduleId and cm.moduleId=ms.moduleId and (ms.startDate is null or ms.endDate is null or ms.startDate < ms.endDate) group by cm order by cm.seqNo asc";
 			}
 			if (meleteSecurityService.allowStudent(courseId))
 			{
 				return getStudentNavSeqNo(userId, courseId, currSeqNo, false);
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 		}
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Integer minsequence = 0;
 			Integer sectionCount = 0;
 			Query q = session.createQuery(queryStr);
 			q.setParameter("courseId", courseId);
 			q.setParameter("currSeqNo", currSeqNo);
 			List<Object[]> res = q.list();
 			for(Iterator<Object[]> it=res.iterator();it.hasNext();)
 			{
 				 Object[] row = (Object[]) it.next();
 				 minsequence = (Integer) row[0];
 				 sectionCount = (Integer) row[1];
 				 if (sectionCount > 0) break;
 			}
 			// if no sequence is found then this is the last module
 			if (minsequence == null || minsequence.intValue() <= 0)
 			{
 				return -1;
 			}
 			nextSeqNo = minsequence.intValue();
 
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 			// he.printStackTrace();
 		}
 		finally
 		{
 			hibernateUtil.closeSession();
 		}
 		return nextSeqNo;
 
 	}
 
 	/**
 	 * Get the number of modules completely read by all users
 	 * 
 	 * @param course_id
 	 *        Course id
 	 * @return A map of course id and number of modules read
 	 */
 	public Map<String, Integer> getNumberOfModulesCompletedByUserId(String course_id)
 	{
 		if (course_id == null) return null;
 		Map<String, Integer> allCompletedModules = new HashMap<String, Integer>();
 		Map<String, Set<Integer>> checkModules = new HashMap<String, Set<Integer>>();
 		Session session = hibernateUtil.currentSession();
 		List res = null;
 		try
 		{
 			String queryString = "select secTrack.userId as userId, cmod.moduleId as moduleId from CourseModule cmod,Section sec, SectionTrackView secTrack where cmod.moduleId=sec.moduleId and sec.sectionId = secTrack.sectionId and cmod.archvFlag=0 and cmod.courseId =:courseId order by secTrack.userId";
 			Query query = session.createQuery(queryString);
 			query.setParameter("courseId", course_id);
 			res = query.list();
 		}
 		catch (Exception ex)
 		{
 			logger.debug(ex.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		try
 		{
 			if (res != null)
 			{
 				// collect all distinct read module_ids
 				for (Iterator itr = res.listIterator(); itr.hasNext();)
 				{
 					Object pair[] = (Object[]) itr.next();
 					String userId = (String) pair[0];
 					Integer moduleId = (Integer) pair[1];
 
 					if (moduleId == null || moduleId.intValue() == 0) continue;
 					Set<Integer> mods = new HashSet<Integer>(0);
 					if (checkModules.containsKey(userId)) mods = checkModules.get(userId);
 					mods.add(moduleId);
 					checkModules.put(userId, mods);
 				}
 
 				// if no sections is read in the site return
 				if (checkModules == null || checkModules.size() == 0) return allCompletedModules;
 
 				// count completed modules by the user
 				Iterator<String> i = checkModules.keySet().iterator();
 				for (i = checkModules.keySet().iterator(); i.hasNext();)
 				{
 					String user = i.next();
 					Set<Integer> mods = checkModules.get(user);
 					for (Integer m : mods)
 					{
 						boolean complete = isModuleCompleted(user, m);
 						if (!complete) continue;
 
 						// if complete increment the count
 						Integer count = 0;
 						if (allCompletedModules.containsKey(user)) count = allCompletedModules.get(user);
 						allCompletedModules.put(user, ++count);
 					}
 				} // for end
 				checkModules = null;
 			} // if res is null end
 		}
 		catch (Exception e)
 		{
 			logger.debug("exception at getting viewed modules count " + e.getMessage());
 		}
 
 		return allCompletedModules;
 	}
 
 	/**
 	 * Get the number of sections read by a user in a module
 	 * 
 	 * @param user_id
 	 *        User id
 	 * @param module_id
 	 *        Module id
 	 * @return Number of sections read
 	 */
 	public int getNumberOfSectionsReadFromModule(String user_id, int module_id)
 	{
 		int count = 0;
 		Connection dbConnection = null;
 		try
 		{
 			ResultSet rs = null;
 			dbConnection = SqlService.borrowConnection();
 			String sql = "select sv.section_id,sv.view_date from melete_section_track_view sv,melete_section ms where sv.user_id = ? and sv.section_id = ms.section_id and ms.module_id = ? order by sv.view_date";
 			PreparedStatement pstmt = dbConnection.prepareStatement(sql);
 			pstmt.setString(1, user_id);
 			pstmt.setInt(2, module_id);
 			rs = pstmt.executeQuery();
 			List<Integer> trackSecList = new ArrayList<Integer>();
 
 			if (rs != null)
 			{
 				while (rs.next())
 				{
 					int sectionId = rs.getInt("section_id");
 					trackSecList.add(new Integer(sectionId));
 				}
 			}
 			rs.close();
 			pstmt.close();
 			count = trackSecList.size();
 		}
 		catch (Exception e)
 		{
 			// nothing
 		}
 		finally
 		{
 			try
 			{
 				if (dbConnection != null) SqlService.returnConnection(dbConnection);
 			}
 			catch (Exception e1)
 			{
 				if (logger.isErrorEnabled()) logger.error(e1);
 			}
 		}
 		return count;
 	}
 
 	/**
 	 * Get prev sequence number (before this module in the course) This method is invoked for instructors and students
 	 * It skips invalid modules. 
 	 * 
 	 * @param userId
 	 * @param courseId
 	 * @param currSeqNo
 	 * @return
 	 */
 	public int getPrevSeqNo(String userId, String courseId, int currSeqNo)
 	{
 		int prevSeqNo = -1;
 		String queryStr = null;
 		boolean allowStudent = false;
 		try
 		{
 			if (meleteSecurityService.allowAuthor(courseId))
 			{
 				queryStr = "select cm.seqNo, count(s.moduleId) from CourseModule cm, ModuleShdates ms, Section s where cm.courseId =:courseId and cm.deleteFlag=0 and cm.archvFlag=0 and cm.seqNo < :currSeqNo and cm.moduleId = s.moduleId and cm.moduleId=ms.moduleId and (ms.startDate is null or ms.endDate is null or ms.startDate < ms.endDate) group by cm order by cm.seqNo desc";
 			}
 			if (meleteSecurityService.allowStudent(courseId))
 			{
 				return getStudentNavSeqNo(userId, courseId, currSeqNo, true);
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 		}
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 
 			Integer maxsequence = 0;
 			Integer sectionCount = 0;
 			Query q = session.createQuery(queryStr);
 			q.setParameter("courseId", courseId);
 			q.setParameter("currSeqNo", currSeqNo);
 			List<Object[]> res = q.list();
 			for(Iterator<Object[]> it=res.iterator();it.hasNext();)
 			{
 				 Object[] row = (Object[]) it.next();
 				 maxsequence = (Integer) row[0];
 				 sectionCount = (Integer) row[1];
 				 if (sectionCount > 0) break;
 			}
 
 			// if no sequence is found then there is no module before this one
 			if (maxsequence == null || maxsequence.intValue() <= 0)
 			{
 				return -1;
 			}
 			prevSeqNo = maxsequence.intValue();
 
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 			// he.printStackTrace();
 		}
 		finally
 		{
 			hibernateUtil.closeSession();
 		}
 		return prevSeqNo;
 	}
 
 	/**
 	 * Get list of sections for module
 	 * 
 	 * @param moduleId
 	 *        Module id
 	 * @return list of sections
 	 * @throws HibernateException
 	 */
 	public List<Section> getSections(int moduleId) throws HibernateException
 	{
 		List<Section> sectionsList = new ArrayList<Section>();
 
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 
 			String queryString = "select section from Section as section where section.moduleId = :moduleId and section.deleteFlag = 0";
 			sectionsList = session.createQuery(queryString).setParameter("moduleId", new Integer(moduleId)).list();
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		return sectionsList;
 	}
 
 	/**
 	 * Get moduleshdates object for this module
 	 * 
 	 * @param moduleId
 	 *        Module id
 	 * @return Moduleshdates object
 	 * @throws HibernateException
 	 */
 	public ModuleShdates getShownModuleDates(int moduleId) throws HibernateException
 	{
 		ModuleShdates mDate = null;
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 
 			String queryString = "select moduleshdate from ModuleShdates as moduleshdate where moduleshdate.module.moduleId = :moduleId";
 			mDate = (ModuleShdates) session.createQuery(queryString).setParameter("moduleId", new Integer(moduleId)).uniqueResult();
 
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 		return mDate;
 	}
 
 	/**
 	 * Get list of modules for instructor
 	 * 
 	 * @param userId
 	 *        user id
 	 * @param courseId
 	 *        course id
 	 * @return list of modules
 	 * @throws HibernateException
 	 */
 	public List<ModuleDateBeanService> getShownModulesAndDatesForInstructor(String userId, String courseId) throws HibernateException
 	{
 		List<ModuleDateBeanService> moduleDateBeansList = new ArrayList<ModuleDateBeanService>();
 		List<? extends ModuleObjService> modList = null;
 		List<SpecialAccess> saModList = null;
 		ModuleDateBean mdBean = null;
 		Module mod = null;
 
 		try
 		{			
 			modList = getModules(courseId);
 			saModList = saDB.getSpecialAccessModuleIds(courseId);
 
 			Iterator<?> i = modList.iterator();
 
 			while (i.hasNext())
 			{
 				mdBean = new ModuleDateBean();
 				mod = (Module) i.next();
 
 				populateModuleBean(mod, mdBean);
 
 				mdBean.setSaFlag(false);
 				if (saModList != null)
 				{
 					if (saModList.size() > 0)
 					{
 						if (saModList.contains(mod.getModuleId()))
 						{
 							mdBean.setSaFlag(true);
 						}
 					}
 				}
 
 				moduleDateBeansList.add(mdBean);
 				mod = null;
 			}
 
 		}
 		catch (Exception he)
 		{
 			logger.error(he.toString());
 			he.printStackTrace();
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 
 		return moduleDateBeansList;
 
 	}
 
 	/**
 	 * Get list of modules core method
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @param filtered
 	 *        flag false value means invalid modules need to be picked up
 	 * @return list of modules
 	 * @throws HibernateException
 	 */
 	public List<ViewModBeanService> getViewModules(String userId, String courseId, boolean filtered) throws Exception
 	{
 		Connection dbConnection = null;
 		List<ViewModBeanService> resList = new ArrayList<ViewModBeanService>();
 		Map<Integer, AccessDates> accMap = null;
 		Map<Integer, Map<Integer,Date>> modSecTrackMap = null;
 		Map<Integer,Date> secViewMap = null;
 
 		try
 		{
 			dbConnection = SqlService.borrowConnection();
 			ResultSet rs = null, stvRs = null;
 			String sql,stvSql;
 			// Check the special access table to see if there are any records
 			// for this user in this course, do this only for students
 			accMap = getAccessRecords(userId, courseId, dbConnection);
 
 			// Select all undeleted modules in this course
 			// MAP-32, if filtered don't pick up invalid modules, otherwise pick up all modules
 			//Select information from the section tracking table, use conditions as appropriate with dates for filtered and unfiltered
 			if (filtered)
 			{
 				sql = "select m.module_id,c.seq_no,m.title as modTitle,m.description as modDesc,m.whats_next,m.seq_xml,d.start_date,d.end_date,s.section_id,s.content_type,s.title as secTitle from melete_module m inner join melete_module_shdates d on m.module_id=d.module_id inner join melete_course_module c on m.module_id=c.module_id left outer join melete_section s on m.module_id = s.module_id where c.course_id = ? and c.delete_flag=0 and c.archv_flag=0 and (d.start_date is NULL or d.end_date is NULL or d.start_date < d.end_date) and (s.delete_flag=0 or s.delete_flag is NULL) order by c.seq_no";
 			    stvSql = "select straight_join mcm.module_id,mstv.section_id,mstv.view_date from melete_course_module mcm,melete_module_shdates msh,melete_section ms,melete_section_track_view mstv where  mcm.course_id = ? and mcm.delete_flag = 0 and mcm.archv_flag = 0 and mcm.module_id = msh.module_id and (msh.start_date is NULL or msh.end_date is NULL or msh.start_date < msh.end_date) and mcm.module_id = ms.module_id and (ms.delete_flag=0 or ms.delete_flag is NULL) and mstv.user_id = ? and ms.section_id = mstv.section_id order by mcm.module_id";
 			}
 			else
 			{
 				sql = "select m.module_id,c.seq_no,m.title as modTitle,m.description as modDesc, m.whats_next,m.seq_xml,d.start_date,d.end_date,s.section_id,s.content_type,s.title as secTitle from melete_module m inner join melete_module_shdates d on m.module_id=d.module_id inner join melete_course_module c on m.module_id=c.module_id left outer join melete_section s on m.module_id = s.module_id where c.course_id = ? and c.delete_flag=0 and c.archv_flag=0 and (s.delete_flag=0 or s.delete_flag is NULL) order by c.seq_no";
 				stvSql = "select straight_join mcm.module_id,mstv.section_id,mstv.view_date from melete_course_module mcm,melete_section ms,melete_section_track_view mstv where  mcm.course_id = ? and mcm.delete_flag = 0 and mcm.archv_flag = 0 and mcm.module_id = ms.module_id  and (ms.delete_flag=0 or ms.delete_flag is NULL) and mstv.user_id = ? and ms.section_id = mstv.section_id order by mcm.module_id";
 			}
 			PreparedStatement pstmt = dbConnection.prepareStatement(sql);
 			pstmt.setString(1, courseId);
 			rs = pstmt.executeQuery();
 			PreparedStatement stvPstmt = dbConnection.prepareStatement(stvSql);
 			stvPstmt.setString(1, courseId);
 			stvPstmt.setString(2, userId);
 			stvRs = stvPstmt.executeQuery();
 			ViewSecBean vsBean = null;
 			Map<Integer, ViewSecBean> vsBeanMap = null;
 			int prevModId = 0, prevSeqNo = 0;
 			int moduleId = 0, seqNo;
 			ViewModBean vmBean = null;
 			String seqXml, prevSeqXml = null;
 			
 			//Iterate through Section Tracking result set
 			//Create a modSecTrackMap whose key is the module id and the value is a map of section id and view date
 			if (stvRs != null)
 			{
 				int prevStvModId = 0;
 				Date viewDate = null;
 				if (modSecTrackMap == null) modSecTrackMap = new LinkedHashMap<Integer, Map<Integer,Date>>();
 				while (stvRs.next())
 				{
 					int stvModId = stvRs.getInt("module_id");
 					int stvSecId = stvRs.getInt("section_id");
 					java.sql.Timestamp stvTimestamp = stvRs.getTimestamp("view_date");
 					if (stvTimestamp != null) viewDate = new java.util.Date(stvTimestamp.getTime() + (stvTimestamp.getNanos() / 1000000));
 					//When module id changes in the result set, associate section tracking information with each module id
 					//Then, clear out the section view map to be used for the next module
 					if ((prevStvModId == 0) || (stvModId != prevStvModId))
 					{
 						if (prevStvModId != 0) modSecTrackMap.put(new Integer(prevStvModId),secViewMap);
 						secViewMap = null;
 						secViewMap = new LinkedHashMap<Integer,Date>();
 					}
 					//Populate the section view date map
 					secViewMap.put(new Integer(stvSecId), viewDate);
 					prevStvModId = stvModId;
 				}
 				//The if condition above does not execute for the last module, so take care of it here
 				if (secViewMap != null)
 				{
 					modSecTrackMap.put(new Integer(prevStvModId),secViewMap);
 				}
 				stvRs.close();
 				stvPstmt.close();
 			}
 
 			if (rs != null)
 			{
 				secViewMap = null;
 				//Iterate through each record in the resultset
 				while (rs.next())
 				{
 					moduleId = rs.getInt("module_id");
 					seqNo = rs.getInt("seq_no");
 					seqXml = rs.getString("seq_xml");
 
 					// Associate vsBeans to vmBean
 					// This means its a new module
 					// This executes just once for each module
 					if ((prevModId != 0) && (moduleId != prevModId))
 					{
 						if (modSecTrackMap != null) secViewMap = modSecTrackMap.get(prevModId);
 						associateSections(vsBeanMap, prevSeqXml, secViewMap, prevSeqNo, vmBean);
 						secViewMap = null;
 						vsBeanMap = null;
 					}// End if ((prevModId != 0)&&(moduleId != prevModId))
 
 					// Populate each vsBean and add to vsBeanMap
 					// This executes for each record in the resultset
 					// It builds up the sections and adds them to vsBeanMap
 					int sectionId = rs.getInt("section_id");
 					if (sectionId != 0)
 					{
 						if (vsBeanMap == null) vsBeanMap = new LinkedHashMap<Integer, ViewSecBean>();
 
 						vsBean = new ViewSecBean();
 						vsBean.setSectionId(sectionId);
 						vsBean.setContentType(rs.getString("content_type"));
 						vsBean.setTitle(rs.getString("secTitle"));
 						vsBeanMap.put(new Integer(sectionId), vsBean);
 					}
 
 					// Populate vmBean
 					// This executes just once for each module
 					if ((prevModId == 0) || (moduleId != prevModId))
 					{
 						vmBean = populateVmBean(rs, accMap, courseId);
 	
 						// Add invalid modules if not filtered
 						// If filtered, do not add bad dates and no sections modules (invalid modules)
 						if (filtered && (!vmBean.isDateFlag() || vsBeanMap == null || vsBeanMap.size() <= 0)) continue;
 						resList.add(vmBean);
 						
 					}// end if ((prevModId == 0)||(moduleId != prevModId))
 
 					prevModId = moduleId;
 					prevSeqNo = seqNo;
 					prevSeqXml = seqXml;
 
 				}// End while
 
 				// The last module will not have had its sections added
 				// so we do it here
 				if (modSecTrackMap != null) secViewMap = modSecTrackMap.get(moduleId);
 				associateSections(vsBeanMap, prevSeqXml, secViewMap, prevSeqNo, vmBean);
 				rs.close();
 				pstmt.close();
 			}
 		}
 		catch (Exception e)
 		{
 			if (logger.isErrorEnabled()) logger.error(e);
 			throw e;
 		}
 		finally
 		{
 			try
 			{
 				if (dbConnection != null) SqlService.returnConnection(dbConnection);
 			}
 			catch (Exception e1)
 			{
 				if (logger.isErrorEnabled()) logger.error(e1);
 				throw e1;
 			}
 		}
 
 		return resList;
 	}
 
 	/**
 	 * Associates sections with vmBean and adds tracking info to each section bean
 	 * 
 	 * @param vsBeanMap
 	 *        Map of sections
 	 * @param seqXml
 	 *        Sequence xml
 	 * @param secViewMap
 	 *        Map of section id and view date
 	 * @param seqNo
 	 *        Sequence number
 	 * @param vmBean
 	 *        ViewModBean object
 	 * @throws SQLException
 	 */
 	protected void associateSections(Map vsBeanMap, String seqXml,
 			Map secViewMap, int seqNo, ViewModBean vmBean) throws SQLException {
 		if (vsBeanMap != null) {
 			if (vsBeanMap.size() > 0) {
 				SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 				ssuImpl.traverseDom(seqXml, Integer.toString(seqNo));
 				List xmlSecList = ssuImpl.getXmlSecList();
 				StringBuffer rowClassesBuf = new StringBuffer();
 				// comment the below line. This should be called for instructors
 				// only and not students
 				// xmlSecList = correctSections(vsBeanMap, moduleId,
 				// xmlSecList);
 				List vsBeanList = new ArrayList();
 				processViewSections(vsBeanMap, vsBeanList, xmlSecList,
 						rowClassesBuf);
 				vmBean.setRowClasses(rowClassesBuf.toString());
 				int count = 0;
 				if ((secViewMap != null)&&(secViewMap.size() > 0)) vmBean.setReadDate(getReadDate(secViewMap));
 				else vmBean.setReadDate(null);
 				if ((secViewMap != null)&&(secViewMap.size() > 0))	count = secViewMap.size();
 				else count = 0;
 				if (count == vsBeanMap.size())
 					vmBean.setReadComplete(true);
 				else
 					vmBean.setReadComplete(false);
 				if (count > 0) {
 					for (ListIterator<ViewSecBean> k = vsBeanList
 							.listIterator(); k.hasNext();) {
 						ViewSecBean vsBean = k.next();
 						if (vsBean != null) {
 							vsBean.setViewDate((Date)secViewMap.get(vsBean
 									.getSectionId()));
 						}
 					}
 				}
 				vmBean.setVsBeans(vsBeanList);
 				vmBean.setNoOfSectionsRead(count);
 			}
 		} else {
 			if (vmBean != null) {
 				vmBean.setReadDate(null);
 				vmBean.setNoOfSectionsRead(0);
 				vmBean.setReadComplete(true);
 			}
 		}
 	}
 	
 	/*protected void associateSections(Map vsBeanMap, String seqXml, int moduleId, int seqNo, ViewModBean vmBean, String userId, Connection dbConnection)
 	throws SQLException
 {
 if (vsBeanMap != null)
 {
 	if (vsBeanMap.size() > 0)
 	{
 		SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 		ssuImpl.traverseDom(seqXml, Integer.toString(seqNo));
 		List xmlSecList = ssuImpl.getXmlSecList();
 		StringBuffer rowClassesBuf = new StringBuffer();
 // comment the below line. This should be called for instructors only and not students
 //			xmlSecList = correctSections(vsBeanMap, moduleId, xmlSecList);
 		List vsBeanList = new ArrayList();
 		processViewSections(vsBeanMap, vsBeanList, xmlSecList, rowClassesBuf);
 		vmBean.setRowClasses(rowClassesBuf.toString());
 		Map<Integer, Date> secTrackMap = new HashMap();
 		int count = 0;
 		vmBean.setReadDate(getReadDate(moduleId, vsBeanMap, userId, dbConnection, secTrackMap));
 		count = secTrackMap.size();
 		if(count == vsBeanMap.size())vmBean.setReadComplete(true);
 		else vmBean.setReadComplete(false);
 		if (count > 0)
 		{
 			for (ListIterator<ViewSecBean> k = vsBeanList.listIterator(); k.hasNext();)
 			{
 				ViewSecBean vsBean = k.next();
 				if (vsBean != null)
 				{
 					vsBean.setViewDate(secTrackMap.get(vsBean.getSectionId()));
 				}
 			}
 		}
 		vmBean.setVsBeans(vsBeanList);
 		vmBean.setNoOfSectionsRead(count);
 	}
 }
 else
 {
 	if (vmBean != null)
 	{
 		vmBean.setReadDate(null);
 		vmBean.setNoOfSectionsRead(0);
 		vmBean.setReadComplete(true);
 	}
 }
 }	*/
 
 	/**
 	 * Creates new vmBean, sets its properties and determines its visibility
 	 * 
 	 * @param rs
 	 *        ResultSet Object
 	 * @param accMap
 	 *        Access records
 	 * @param courseId
 	 *        Course Id
 	 * @return Newly created ViewModBean object
 	 * @throws SQLException
 	 */
 	protected ViewModBean populateVmBean(ResultSet rs, Map<Integer, AccessDates> accMap, String courseId) throws SQLException
 	{
 		ViewModBean vmBean = new ViewModBean();
 		int moduleId = rs.getInt("module_id");
 		vmBean.setModuleId(moduleId);
 		int seqNo = rs.getInt("seq_no");
 		vmBean.setSeqNo(seqNo);
 		vmBean.setTitle(rs.getString("modTitle"));
 		vmBean.setDescription(rs.getString("modDesc"));
 		vmBean.setWhatsNext(rs.getString("whats_next"));
 		String seqXml = rs.getString("seq_xml");
 		vmBean.setSeqXml(seqXml);
 
 		// what's next display seq number is number of top level sections + 1
 		SubSectionUtilImpl ssuImpl1 = new SubSectionUtilImpl();
 		int top = ssuImpl1.noOfTopLevelSections(seqXml);
 		top = top + 1;
 		String ns_number = new String(seqNo + ".");
 		ns_number = ns_number.concat(Integer.toString(top));
 		vmBean.setNextStepsNumber(ns_number);
 
 		java.sql.Timestamp startTimestamp = rs.getTimestamp("start_date");
 		java.sql.Timestamp endTimestamp = rs.getTimestamp("end_date");
 		// If special access is set up, use those dates; otherwise,
 		// use module dates
 		if ((accMap != null) && (accMap.size() > 0))
 		{
 			AccessDates ad = (AccessDates) accMap.get(moduleId);
 			if (ad != null)
 			{
 				if (ad.overrideStart) startTimestamp = ad.getAccStartTimestamp();
 				if (ad.overrideEnd) endTimestamp = ad.getAccEndTimestamp();
 			}
 		}
 
 		// Date flag is false for invalid modules
 		if ((startTimestamp != null) && (endTimestamp != null) && (startTimestamp.compareTo(endTimestamp) >= 0))
 			vmBean.setDateFlag(false);
 		else
 			vmBean.setDateFlag(true);
 		if (isVisible(startTimestamp, endTimestamp))
 		{
 			this.accessAdvisor = (AccessAdvisor) ComponentManager.get(AccessAdvisor.class);
 			if ((this.accessAdvisor != null)
 					&& (this.accessAdvisor.denyAccess("sakai.melete", courseId, String.valueOf(moduleId), SessionManager.getCurrentSessionUserId())))
 			{
 				 String blockDetails = this.accessAdvisor.details("sakai.melete", courseId, String.valueOf(moduleId), SessionManager.getCurrentSessionUserId());
 				 if(blockDetails != null) vmBean.setBlockedDetails(" "+blockDetails);
 				 
 				vmBean.setBlockedBy(this.accessAdvisor.message("sakai.melete", courseId, String.valueOf(moduleId), SessionManager
 						.getCurrentSessionUserId()));
 				vmBean.setVisibleFlag(false);
 			}
 			else
 			{
 				vmBean.setVisibleFlag(true);
 			}
 		}
 		else
 		{
 			vmBean.setVisibleFlag(false);
 		}
 
 		if (startTimestamp != null)
 		{
 			vmBean.setStartDate(new java.util.Date(startTimestamp.getTime() + (startTimestamp.getNanos() / 1000000)));
 		}
 		if (endTimestamp != null)
 		{
 			vmBean.setEndDate(new java.util.Date(endTimestamp.getTime() + (endTimestamp.getNanos() / 1000000)));
 		}
 		return vmBean;
 	}
 	
 	/**
 	 * Get list of modules with view status set
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @param filtered
 	 *        flag false value means invalid modules need to be picked up
 	 * @return list of modules
 	 * @throws HibernateException
 	 */
 	public List<ViewModBeanService> getViewModulesAndDates(String userId, String courseId, boolean filtered) throws HibernateException
 	{
 		List<ViewModBeanService> modList = null;
 		Module mod = null;
 
 		try
 		{
 			modList = getViewModules(userId, courseId, filtered);
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 			e.printStackTrace();
 		}
 		return modList;
 
 	}
 
 	/**
 	 * 
 	 * @param userId
 	 * @param courseId
 	 * @param modId
 	 * @return
 	 * @throws Exception
 	 */
 	public ViewModBeanService getViewModBean(String userId, String courseId,
 			int modId) throws Exception {
 		Connection dbConnection = null;
 		ViewModBeanService vmBean = null;
 		try {
 			dbConnection = SqlService.borrowConnection();
 			ResultSet rs = null;
 			String sql;
 
 			sql = "select m.module_id,c.seq_no,m.title as modTitle,m.description as modDesc,m.whats_next,m.seq_xml,d.start_date,d.end_date,s.section_id,s.content_type,s.title as secTitle from melete_module m inner join melete_module_shdates d on m.module_id=d.module_id inner join melete_course_module c on m.module_id=c.module_id left outer join melete_section s on m.module_id = s.module_id where m.module_id = ? and (s.delete_flag=0 or s.delete_flag is NULL)";
 			
 			PreparedStatement pstmt = dbConnection.prepareStatement(sql);
 			pstmt.setInt(1, modId);
 			rs = pstmt.executeQuery();
 			if (rs != null) {
 				vmBean = getViewModBeanRs(rs, dbConnection, userId, courseId);
 				rs.close();
 				pstmt.close();
 			}
 		} catch (Exception e) {
 			if (logger.isErrorEnabled())
 				logger.error(e);
 			throw e;
 		} finally {
 			try {
 				if (dbConnection != null)
 					SqlService.returnConnection(dbConnection);
 			} catch (Exception e1) {
 				if (logger.isErrorEnabled())
 					logger.error(e1);
 				throw e1;
 			}
 		}
 		return vmBean;
 	}
 			
 	/**
 	 * Get ViewModBeanService object by sequence number
 	 * 
 	 * @param userId
 	 *        The user Id
 	 * @param courseId
 	 *        The course id
 	 * @param seqNo
 	 *        The sequence number
 	 * @return ViewModBeanService object
 	 * @throws Exception
 	 */
 	public ViewModBeanService getViewModBeanBySeq(String userId, String courseId, int seqNo) throws Exception
 	{
 		Connection dbConnection = null;
 		ViewModBeanService vmBean = null;
 		Map secViewMap = null;
 		try
 		{
 			dbConnection = SqlService.borrowConnection();
 			ResultSet rs = null, modIdRs = null;
 			String sql;
 
 			sql = "select m.module_id,c.seq_no,m.title as modTitle,m.description as modDesc,m.whats_next,m.seq_xml,d.start_date,d.end_date,s.section_id,s.content_type,s.title as secTitle from melete_module m inner join melete_module_shdates d on m.module_id=d.module_id inner join melete_course_module c on m.module_id=c.module_id left outer join melete_section s on m.module_id = s.module_id where c.course_id = ? and c.seq_no = ? and (s.delete_flag=0 or s.delete_flag is NULL)";
 			PreparedStatement pstmt = dbConnection.prepareStatement(sql);
 			pstmt.setString(1, courseId);
 			pstmt.setInt(2, seqNo);
 			rs = pstmt.executeQuery();
 			if (rs != null)
 			{
 				vmBean = getViewModBeanRs(rs, dbConnection, userId, courseId);
 				rs.close();
 				pstmt.close();
 			}
 		}
 		catch (Exception e)
 		{
 			if (logger.isErrorEnabled()) logger.error(e);
 			throw e;
 		}
 		finally
 		{
 			try
 			{
 				if (dbConnection != null) SqlService.returnConnection(dbConnection);
 			}
 			catch (Exception e1)
 			{
 				if (logger.isErrorEnabled()) logger.error(e1);
 				throw e1;
 			}
 		}
 		return vmBean;
 	}
 	
 	/**
 	 * Returns a map of <section id,view date> for a module for a user
 	 * 
 	 * @param modId
 	 *            The module id
 	 * @param userId
 	 *            The user id
 	 * @param dbConnection
 	 * @return
 	 * @throws SQLException
 	 */
 	private Map getSecViewMap(int modId, String userId, Connection dbConnection)
 			throws SQLException {
 		Map secViewMap = null;
 		String stvSql = "select mstv.section_id,mstv.view_date from melete_course_module mcm,melete_section ms,melete_section_track_view mstv where mcm.module_id = ms.module_id and ms.section_id = mstv.section_id and (ms.delete_flag=0 or ms.delete_flag is NULL) and mcm.module_id = ?  and mstv.user_id = ?";
 		PreparedStatement stvPstmt = dbConnection.prepareStatement(stvSql);
 		stvPstmt.setInt(1, modId);
 		stvPstmt.setString(2, userId);
 		ResultSet stvRs = stvPstmt.executeQuery();
 
 		if (stvRs != null) {
 			Date viewDate = null;
 			secViewMap = new LinkedHashMap<Integer, Date>();
 			while (stvRs.next()) {
 				int stvSecId = stvRs.getInt("section_id");
 				java.sql.Timestamp stvTimestamp = stvRs
 						.getTimestamp("view_date");
 				if (stvTimestamp != null)
 					viewDate = new java.util.Date(stvTimestamp.getTime()
 							+ (stvTimestamp.getNanos() / 1000000));
 				secViewMap.put(new Integer(stvSecId), viewDate);
 			}
 			stvRs.close();
 			stvPstmt.close();
 		}
 		return secViewMap;
 	}
 
 	/**
 	 * Method that does the actual db work, associates sections and returns viewModBean object
 	 * Works with just one module and its sections
 	 * @param rs
 	 *        ResultSet
 	 * @param dbConnection
 	 *        Connection object
 	 * @param userId
 	 *        The user id
 	 * @param courseId
 	 *        The course id
 	 * @return ViewModBean object
 	 * @throws Exception
 	 */
 	protected ViewModBean getViewModBeanRs(ResultSet rs, Connection dbConnection, String userId, String courseId) throws Exception
 	{
 		ViewSecBean vsBean = null;
 		Map<Integer, ViewSecBean> vsBeanMap = null;
 		int prevModId = 0;
 		int moduleId = 0, seqNo = 0;
 		String seqXml = null;
 		Map<Integer, AccessDates> accMap = null;
 		ViewModBean vmBean = null;
 		Map secViewMap = null;
 
 		if (rs != null)
 		{
 			// Check the special access table to see if there are any records
 			// for this user in this course, do this only for students
 			accMap = getAccessRecords(userId, courseId, dbConnection);
 
 			// Iterate through result set
 			while (rs.next())
 			{
 				// Executes just once for entire result set
 				// to populate vmBean
 				if (prevModId == 0)
 				{
 					moduleId = rs.getInt("module_id");
 					seqNo = rs.getInt("seq_no");
 					seqXml = rs.getString("seq_xml");
 					vmBean = populateVmBean(rs, accMap, courseId);
 					prevModId = moduleId;
 				}
 
 				// Executes for each record in result set
 				// Populate each vsBean and add to vsBeanMap
 				int sectionId = rs.getInt("section_id");
 				if (sectionId != 0)
 				{
 					if (vsBeanMap == null) vsBeanMap = new LinkedHashMap<Integer, ViewSecBean>();
 
 					vsBean = new ViewSecBean();
 					vsBean.setSectionId(sectionId);
 					vsBean.setContentType(rs.getString("content_type"));
 					vsBean.setTitle(rs.getString("secTitle"));
 					vsBeanMap.put(new Integer(sectionId), vsBean);
 				}
 			}// End while
 			secViewMap = getSecViewMap(prevModId, userId, dbConnection);
 			// Associates the section map with vmBean
 			associateSections(vsBeanMap, seqXml, secViewMap, seqNo, vmBean);
 		}
 		return vmBean;
 	}
 	
 	/**
 	 * Checks if the module is completely read by the user
 	 * 
 	 * @param user_id
 	 *        User id
 	 * @param module_id
 	 *        Module id
 	 * @return true if module is completed, false otherwise
 	 */
 	public boolean isModuleCompleted(String user_id, int module_id)
 	{
 		Module m = getModule(module_id);
 		if (m == null) return false;
 		Map<Integer, ? extends SectionObjService> sections = m.getSections();
 
 		// if module has no sections then its considered as completed.
 		if (sections == null || sections.size() == 0) return true;
 
 		// # of sections read by user for this module
 		int userViews = getNumberOfSectionsReadFromModule(user_id, module_id);
 
 		// if both numbers are same then user has finished the module
 		if (sections.size() == userViews)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * Move section from one module to another, delete section tracking info when section moves
 	 * 
 	 * @param section
 	 *        Section object
 	 * @param selectedModule
 	 *        Module object
 	 * @throws MeleteException
 	 */
 	public void moveSection(String courseId, Section section, Module selectedModule) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			try
 			{
 				// add section to selected Module
 				String selectedModSeqXml = selectedModule.getSeqXml();
 				SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 				SectionUtil.addSectiontoList(selectedModSeqXml, section.getSectionId().toString());
 				selectedModSeqXml = SectionUtil.storeSubSections();
 				selectedModule.setSeqXml(selectedModSeqXml);
 
 				// delete section association from the previous module
 				Module prev_module = (Module) section.getModule();
 				String prevModSeqXml = prev_module.getSeqXml();
 				prevModSeqXml = SectionUtil.deleteSection(prevModSeqXml, section.getSectionId().toString());
 				prev_module.setSeqXml(prevModSeqXml);
 
 				section.setModule(selectedModule);
 				section.setModuleId(selectedModule.getModuleId().intValue());
 
 				// save object
 				tx = session.beginTransaction();
 				String delSectionTrackStr = "delete SectionTrackView stv where stv.sectionId = ";
 				int deletedEntities = session.createQuery(delSectionTrackStr + section.getSectionId()).executeUpdate();
 				session.saveOrUpdate(section);
 				session.saveOrUpdate(prev_module);
 				session.saveOrUpdate(selectedModule);
 
 				// move section file from content hosting to new location
 				if (section.getContentType() != null && section.getContentType().equals("typeEditor"))
 				{
 					String secContentFile = section.getSectionResource().getResource().getResourceId();
 					String destinationColl = meleteCHService.getCollectionId(courseId, "typeEditor", selectedModule.getModuleId());
 					String newResId = meleteCHService.moveResource(secContentFile, destinationColl);
 					MeleteResource old = (MeleteResource) section.getSectionResource().getResource();
 					MeleteResource newMR = new MeleteResource(old);
 					newMR.setResourceId(newResId);
 					session.save(newMR);
 					SectionResource newSR = (SectionResource) section.getSectionResource();
 					newSR.setResource(newMR);
 					section.setSectionResource(newSR);
 					session.update(newSR);
 					session.saveOrUpdate(section);
 					session.flush();
 					session.delete(old);
 				}
 				tx.commit();
 
 			}
 			catch (StaleObjectStateException sose)
 			{
 				logger.error("stale object exception" + sose.toString());
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			catch (MeleteException me)
 			{
 				if (tx != null) tx.rollback();
 				throw me;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception e)
 		{
 			throw new MeleteException("move_section_fail");
 		}
 	}
 
 	/**
 	 * Create printable view of sections of a module
 	 * 
 	 * @param module
 	 *        module object
 	 * @return Print text
 	 * @throws MeleteException
 	 */
 	public String prepareModuleSectionsForPrint(Module module) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			StringBuffer printText = null;
 			String courseId = module.getCoursemodule().getCourseId();
 			boolean autonumber = false;
 			MeleteSitePreference pref = userPrefdb.getSitePreferences(courseId);
 			if (pref != null) autonumber = pref.isAutonumber();
 			try
 			{
 				if (autonumber)
 				{
 					printText = new StringBuffer("<h3>" + module.getCoursemodule().getSeqNo() + ".  " + module.getTitle() + "</h3>");
 				}
 				else
 				{
 					printText = new StringBuffer("<h3>" + module.getTitle() + "</h3>");
 				}
 				;
 				if (module.getDescription() != null && module.getDescription().length() != 0)
 					printText.append("<p> " + module.getDescription() + "</p>");
 				SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 				ssuImpl.traverseDom(module.getSeqXml(), new Integer(module.getCoursemodule().getSeqNo()).toString());
 				List<SecLevelObj> xmlSecList = ssuImpl.getXmlSecList();
 				Map printSections = module.getSections();
 
 				if (xmlSecList != null)
 				{
 					for (ListIterator<SecLevelObj> k = xmlSecList.listIterator(); k.hasNext();)
 					{
 						SecLevelObj slObj = k.next();
 						if (slObj != null)
 						{
 							Section sec = (Section) printSections.get(new Integer(slObj.getSectionId()));
 
 							if (autonumber)
 							{
 								printText.append("<h4>" + slObj.getDispSeq() + ".  " + sec.getTitle() + "</h4>");
 							}
 							else
 							{
 								printText.append("<h4>" + sec.getTitle() + "</h4> <hr style=\"border-bottom:3px solid #000;\">");
 							}
 							;
 							SectionResource secRes = null;
 							MeleteResource melRes = null;
 							if (sec.getSectionResource() != null)
 							{
 								secRes = sectionDB.getSectionResourcebyId(sec.getSectionId().toString());
 								if(secRes.getResource() != null && secRes.getResource().getResourceId() != null && secRes.getResource().getResourceId().length() != 0)
 								{
 								melRes = sectionDB.getMeleteResource(secRes.getResource().getResourceId());
 								printText.append("<p><i>" + getLicenseInformation(melRes) + "</i></p>");
 								}
 							}
 							if (sec.getInstr() != null && sec.getInstr().length() != 0)
 								printText.append("<p> <i>Instructions:</i> " + sec.getInstr() + "</p>");
 							if (sec.getContentType() == null || sec.getContentType().equals("notype") || secRes == null || melRes == null)
 							{
 								continue;
 							}
 							String resourceId = melRes.getResourceId();
 							ContentResource resource = null;
 							try
 							{
 								resource = meleteCHService.getResource(resourceId);
 							}
 							catch (Exception resEx)
 							{
 								// skip unable to get resource
 								continue;
 							}
 							if (sec.getContentType().equals("typeEditor"))
 							{
 								byte[] data = resource.getContent();
 								if (data != null && data.length != 0) printText.append("<p>" + new String(data) + "</p>");
 							}
 							if (resource != null
 									&& (sec.getContentType().equals("typeLink") || sec.getContentType().equals("typeUpload") || sec.getContentType()
 											.equals("typeLTI")))
 							{
 								String url = resource.getUrl();
 								url = url.replaceAll(" ", "%20");
 								printText.append("<a href=\"" + url + "\" target=\"_blank\">");
 								printText.append(resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
 								printText.append("</a>");
 							}
 
 						}
 					}
 				}
 				// what's next information in the end
 				if (module.getWhatsNext() != null && module.getWhatsNext().length() > 0)
 				{
 					ResourceLoader rl = new ResourceLoader("melete_license");
 					printText.append("<h4>" + rl.getString("Next_steps") + "</h4>");
 					printText.append(module.getWhatsNext());
 				}
 				return printText.toString();
 			}
 			catch (StaleObjectStateException sose)
 			{
 				logger.error("stale object exception" + sose.toString());
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw new MeleteException("print_module_fail");
 		}
 		return null;
 	}
 
 	/**
 	 * Restore modules and set dates to null when restored
 	 * 
 	 * @param restoreModules
 	 *        List of modules to restore
 	 * @param courseId
 	 *        Course id
 	 * @throws MeleteException
 	 */
 	public void restoreModules(List restoreModules, String courseId, String userId) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 
 			try
 			{
 				int startSeqNo = assignSequenceNumber(session, courseId);
 
 				// 1.for each element of list
 				for (int i = 0; i < restoreModules.size(); i++)
 				{
 					// 2.set course module object archv_flag to false, archived_date to null,
 					CourseModule coursemodule = (CourseModule) restoreModules.get(i);
 					Query q = session.createQuery("select cm1 from CourseModule cm1 where cm1.module.moduleId =:moduleId");
 					q.setParameter("moduleId", coursemodule.getModule().getModuleId());
 
 					CourseModule coursemodule1 = (CourseModule) (q.uniqueResult());
 					coursemodule1.setArchvFlag(false);
 					coursemodule1.setDateArchived(null);
 					coursemodule1.setDeleteFlag(false);
 
 					// seq no as max+1
 					coursemodule1.setSeqNo(startSeqNo);
 					startSeqNo++;
 
 					// 3. fetch module_shdate object
 					q = session.createQuery("select msh from ModuleShdates msh where msh.module.moduleId =:moduleId");
 					q.setParameter("moduleId", coursemodule.getModule().getModuleId());
 
 					ModuleShdates moduleShdate = (ModuleShdates) (q.uniqueResult());
 					moduleShdate.setStartDate(null);
 					moduleShdate.setEndDate(null);
 
 					//fetch module object and update modification date
 					q = session.createQuery("select mod from Module mod where mod.moduleId =:moduleId");
 					q.setParameter("moduleId", coursemodule.getModule().getModuleId());
 
 					Module module = (Module) (q.uniqueResult());
 					User user = UserDirectoryService.getUser(userId);
 					module.setModificationDate(new java.util.Date());
 					module.setModifiedByFname(user.getFirstName());
 					module.setModifiedByLname(user.getLastName());
 					
 					// 4a. begin transaction
 					tx = session.beginTransaction();
 					// 4b save all objects
 					session.saveOrUpdate(coursemodule1);
 					session.saveOrUpdate(moduleShdate);
 					session.saveOrUpdate(module);
 					// 4c.commit transaction
 					tx.commit();
 				}
 				return;
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				// he.printStackTrace();
 				throw new MeleteException(he.toString());
 			}
 
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 			throw new MeleteException(e.toString());
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 			}
 		}
 
 	}
 
 	/**
 	 * @param hibernateUtil
 	 *        The hibernateUtil to set.
 	 */
 	public void setHibernateUtil(HibernateUtil hibernateUtil)
 	{
 		this.hibernateUtil = hibernateUtil;
 	}
 
 	/**
 	 * @param meleteCHService
 	 *        the meleteCHService to set
 	 */
 	public void setMeleteCHService(MeleteCHService meleteCHService)
 	{
 		this.meleteCHService = meleteCHService;
 	}
 
 	/**
 	 * @param meleteSecurityService
 	 *        The meleteSecurityService to set.
 	 */
 	public void setMeleteSecurityService(MeleteSecurityService meleteSecurityService)
 	{
 		this.meleteSecurityService = meleteSecurityService;
 	}
 
 	/**
 	 * @param userPrefdb
 	 *        the userPreference to set
 	 */
 	public void setMeleteUserPrefDB(MeleteUserPreferenceDB userPrefdb)
 	{
 		this.userPrefdb = userPrefdb;
 	}
 
 	/**
 	 * @param saDB
 	 *        the saDB to set
 	 */
 	public void setSaDB(SpecialAccessDB saDB)
 	{
 		this.saDB = saDB;
 	}
 
 	/**
 	 * @param sectionDB
 	 *        the sectionDB to set
 	 */
 	public void setSectionDB(SectionDB sectionDB)
 	{
 		this.sectionDB = sectionDB;
 	}
 
 	/**
 	 * Sort module up or down
 	 * 
 	 * @param module
 	 *        Module object
 	 * @param course_id
 	 *        Course id
 	 * @param Direction
 	 *        Direction to sort in
 	 * @throws MeleteException
 	 */
 	public void sortModuleItem(Module module, String course_id, String Direction) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 
 			try
 			{
 				List<CourseModule> sortModules = new ArrayList<CourseModule>();
 				List newModulesList = null;
 				Query q = session
 						.createQuery("select cm from CourseModule cm where cm.courseId =:course_id and cm.archvFlag=0 and cm.deleteFlag = 0 order by cm.seqNo");
 				q.setParameter("course_id", course_id);
 				sortModules = q.list();
 				// nothing to sort
 				if (sortModules.size() <= 1) return;
 
 				// find the to be sorted coursemodule object
 				CourseModule curr_cm = null;
 				int curr_seq = 0;
 				for (CourseModule c : sortModules)
 				{
 					if (c.getModuleId() == module.getModuleId())
 					{
 						curr_cm = c;
 						curr_seq = c.getSeqNo();
 						break;
 					}
 				}
 				// if to be sorted module not found in db then return
 				if(curr_cm == null) return;
 				
 				CourseModule change_cm = null;
 				if (Direction.equals("allUp"))
 				{
 					logger.debug("sort up module " + module.getModuleId());
 					curr_cm.setSeqNo(1);
 					newModulesList = new ArrayList();
 					newModulesList.add(curr_cm);
 					int startIdx = curr_seq - 1;
 					while (startIdx > 0)
 					{
 						CourseModule cm = (CourseModule) sortModules.get(startIdx - 1);
 						cm.setSeqNo(startIdx + 1);
 						newModulesList.add(cm);
 						startIdx--;
 					}
 
 				}
 				else if (Direction.equals("up"))
 				{
 					logger.debug("sort up module " + module.getModuleId());
 					int change_seq = curr_seq - 2;
 					change_cm = (CourseModule) sortModules.get(change_seq);
 					change_cm.setSeqNo(curr_seq);
 					curr_cm.setSeqNo(change_seq + 1);
 				}
 				else if (Direction.equals("down"))
 				{
 					logger.debug("sort down module " + module.getModuleId());
 					int change_seq = curr_seq;
 					change_cm = (CourseModule) sortModules.get(change_seq);
 					change_cm.setSeqNo(curr_seq);
 					curr_cm.setSeqNo(change_seq + 1);
 				}
 				else if (Direction.equals("allDown"))
 				{
 					logger.debug("sort all down module " + module.getModuleId());
 					int lastIndex = sortModules.size();
 					curr_cm.setSeqNo(lastIndex);
 					newModulesList = new ArrayList();
 					newModulesList.add(curr_cm);
 					int startIdx = curr_seq;
 					logger.debug("start idx :" + startIdx);
 					while (startIdx < lastIndex)
 					{
 						CourseModule cm = (CourseModule) sortModules.get(startIdx);
 						cm.setSeqNo(startIdx);
 						newModulesList.add(cm);
 						startIdx++;
 					}
 				}
 
 				// save object
 				tx = session.beginTransaction();
 				if (newModulesList == null)
 				{
 					session.saveOrUpdate(change_cm);
 					session.saveOrUpdate(curr_cm);
 				}
 				else
 				{
 					for (int i = 0; i < newModulesList.size(); i++)
 						session.saveOrUpdate(newModulesList.get(i));
 				}
 				tx.commit();
 
 				if (logger.isDebugEnabled()) logger.debug("commiting transaction and sorting module id " + module.getModuleId());
 			}
 			catch (StaleObjectStateException sose)
 			{
 				logger.error("stale object exception" + sose.toString());
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}			
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception ex)
 		{
 			// Throw application specific error
 			ex.printStackTrace();
 			throw new MeleteException("sort_fail");
 		}
 	}
 
 	/**
 	 * Sort section within the module
 	 * 
 	 * @param module
 	 *        Module object
 	 * @param section_id
 	 *        Section id
 	 * @param Direction
 	 *        Direction in which to sort
 	 * @throws MeleteException
 	 */
 	public void sortSectionItem(Module module, String section_id, String Direction) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			try
 			{
 				String sectionsSeqXML = module.getSeqXml();
 				SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 				if (Direction.equals("allUp"))
 				{
 					logger.debug("sort up section " + section_id);
 					sectionsSeqXML = SectionUtil.moveAllUpSection(sectionsSeqXML, section_id);
 				}
 				else if (Direction.equals("up"))
 				{
 					logger.debug("sort up section " + section_id);
 					sectionsSeqXML = SectionUtil.moveUpSection(sectionsSeqXML, section_id);
 				}
 				else if (Direction.equals("down"))
 				{
 					logger.debug("sort down section " + section_id);
 					sectionsSeqXML = SectionUtil.moveDownSection(sectionsSeqXML, section_id);
 				}
 				else if (Direction.equals("allDown"))
 				{
 					logger.debug("sort down section " + section_id);
 					sectionsSeqXML = SectionUtil.moveAllDownSection(sectionsSeqXML, section_id);
 				}
 				module.setSeqXml(sectionsSeqXML);
 
 				// save object
 				tx = session.beginTransaction();
 				session.saveOrUpdate(module);
 				tx.commit();
 
 				if (logger.isDebugEnabled()) logger.debug("commiting transaction and sorting section id " + section_id);
 			}
 			catch (StaleObjectStateException sose)
 			{
 				logger.error("stale object exception" + sose.toString());
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			catch (MeleteException me)
 			{
 				if (tx != null) tx.rollback();
 				throw me;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception ex)
 		{
 			throw new MeleteException("sort_fail");
 		}
 	}
 
 	/**
 	 * Update module object and use spaces in place of nulls
 	 * 
 	 * @param mod
 	 *        Module object
 	 * @throws Exception
 	 */
 	public void updateModule(Module mod) throws Exception
 	{
 		hibernateUtil.ensureModuleHasNonNulls(mod);
 		Transaction tx = null;
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 
 			tx = session.beginTransaction();
 
 			// Update module properties
 			session.update(mod);
 
 			tx.commit();
 
 			// session.flush();
 
 		}
 		catch (StaleObjectStateException sose)
 		{
 			if (tx != null) tx.rollback();
 			logger.error("stale object exception" + sose.toString());
 			throw new MeleteException("edit_module_multiple_users");
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 			throw he;
 		}
 		catch (Exception e)
 		{
 			if (tx != null) tx.rollback();
 			logger.error(e.toString());
 			throw e;
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 				throw he;
 			}
 		}
 	}
 
 	/**
 	 * Update moduledatebean objects
 	 * 
 	 * @param moduleDateBeans
 	 *        List of moduledatebeans to update
 	 * @throws Exception
 	 */
 	public void updateModuleDateBeans(List<? extends  ModuleDateBeanService> moduleDateBeans, String courseId, String userId) throws Exception
 	{
 		Transaction tx = null;
 
 		Session session = hibernateUtil.currentSession();
 		logger.debug("COMING TO UMDB");
 		if ((moduleDateBeans != null) && (moduleDateBeans.size() > 0)) {
 			for (ListIterator i = moduleDateBeans.listIterator(); i.hasNext();) {
 				tx = null;
 				logger.debug("ITERATING");
 				ModuleDateBean mdbean = (ModuleDateBean) i.next();
 				// Saving all modules (irrespective of dateFlag) as we now save
 				// modules with start date after end date also
 				try {
 					Module checkModule = (Module) mdbean.getModule();
 					ModuleShdates checkModuleDates = (ModuleShdates) mdbean
 							.getModuleShdate();
 					logger.debug("checking for " + checkModule.getTitle());
 					String queryString = "select module from Module as module where module.moduleId = :moduleId";
 					Module mod = (Module) session
 							.createQuery(queryString)
 							.setParameter("moduleId", checkModule.getModuleId())
 							.uniqueResult();
 
 					queryString = "select moduleshdate from ModuleShdates as moduleshdate where moduleshdate.module.moduleId = :moduleId";
 					ModuleShdates mDate = (ModuleShdates) session
 							.createQuery(queryString)
 							.setParameter("moduleId", checkModule.getModuleId())
 							.uniqueResult();
 
 					// If any date is > 9999, this check sets the date to the
 					// module's previous date
 					if (!checkModuleDates.isStartDateValid()) {
 						checkModuleDates.setStartDate(mDate.getStartDate());
 					}
 					if (!checkModuleDates.isEndDateValid()) {
 						checkModuleDates.setEndDate(mDate.getEndDate());
 					}
 					// compare them. If both are same then not modified
 					logger.debug("module is same " + mod.equals(checkModule));
 					logger.debug("moduleDates is same "
 							+ mDate.equals(checkModuleDates));
 					if (mod.equals(checkModule)
 							&& mDate.equals(checkModuleDates)) {
 						logger.debug("MODULE AND SH DATES BOTH ARE EQUAL SO NO DB UPDATE for:"
 								+ mod.getTitle());
 						continue;
 					}
 
 					if (checkModuleDates.getAddtoSchedule() != null) {
 						logger.debug("EVENT ids here are "+checkModuleDates.getStartEventId());
 
 						checkModuleDates = updateCalendar(
 								checkModule.getTitle(), checkModuleDates,
 								courseId);
 					}
 					else
 					{
 						logger.debug("ATS is null");
 					}
 					tx = session.beginTransaction();
 					logger.debug("update module and sh dates " + mod.getTitle());
 					// refresh object and Getting the set of show hides dates
 					// associated with this module
 					mDate.setStartDate(checkModuleDates.getStartDate());
 					mDate.setEndDate(checkModuleDates.getEndDate());
 					mDate.setAddtoSchedule(checkModuleDates.getAddtoSchedule());
 					mDate.setEndEventId(checkModuleDates.getEndEventId());
 					mDate.setStartEventId(checkModuleDates.getStartEventId());
 					session.saveOrUpdate(mDate);
 					// refresh object
 
 					mod.setCoursemodule(checkModule.getCoursemodule());
 					mod.setDescription(checkModule.getDescription());
 					mod.setKeywords(checkModule.getKeywords());
 					mod.setModificationDate(new Date());
 					User user = UserDirectoryService.getUser(userId);
 					mod.setModifiedByFname(user.getFirstName());
 					mod.setModifiedByLname(user.getLastName());
 					mod.setTitle(checkModule.getTitle());
 					mod.setModuleshdate(mDate);
 
 					// Update module properties
 					session.saveOrUpdate(mod);
 					tx.commit();
 					// session.flush();
 				} catch (StaleObjectStateException sose) {
 					if (tx != null)
 						tx.rollback();
 					logger.error("stale object exception" + sose.toString());
 					sose.printStackTrace();
 					throw new MeleteException("edit_module_multiple_users");
 				} catch (HibernateException he) {
 					logger.error(he.toString());
 					throw he;
 				} catch (Exception e) {
 					if (tx != null)
 						tx.rollback();
 					logger.error(e.toString());
 					throw e;
 				}
 			}
 		}
 		try
 		{
 			hibernateUtil.closeSession();
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 			throw he;
 		}
 	}
 
 	/**
 	 * Update moduleshdates object
 	 * 
 	 * @param modShdates
 	 *        Moduleshdates object
 	 * @throws Exception
 	 */
 	public void updateModuleShdates(ModuleShdates modShdates) throws Exception
 	{
 		// MAY NEED TO ADD NOT NULL CHECK HERE FOR ORACLE
 		Transaction tx = null;
 		try
 		{
 
 			Session session = hibernateUtil.currentSession();
 
 			tx = session.beginTransaction();
 
 			// Update module properties
 			session.saveOrUpdate(modShdates);
 
 			tx.commit();
 
 			// session.flush();
 
 		}
 		catch (StaleObjectStateException sose)
 		{
 			if (tx != null) tx.rollback();
 			logger.error("stale object exception" + sose.toString());
 			throw new MeleteException("edit_module_multiple_users");
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 			throw he;
 		}
 		catch (Exception e)
 		{
 			if (tx != null) tx.rollback();
 			logger.error(e.toString());
 			throw e;
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 				throw he;
 			}
 		}
 	}
 
 	/**
 	 * Update next steps.
 	 * 
 	 * @param moduleId
 	 *  The module Id
 	 * @param nextSteps
 	 *  What's next 
 	 * @throws Exception
 	 */
 	public void updateModuleNextSteps(Integer moduleId, String nextSteps) throws Exception
 	{
 		Transaction tx = null;
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			tx = session.beginTransaction();
 
 			String queryString = "select module from Module as module where module.moduleId = :moduleId";
 			Module mod = (Module) session.createQuery(queryString).setParameter("moduleId", moduleId).uniqueResult();
 			mod.setWhatsNext(nextSteps);
 			// Update module properties
 			session.saveOrUpdate(mod);
 
 			tx.commit();
 		}
 		catch (StaleObjectStateException sose)
 		{
 			if (tx != null) tx.rollback();
 			logger.error("stale object exception" + sose.toString());
 			throw new MeleteException("edit_module_next_steps");
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 			throw he;
 		}
 		catch (Exception e)
 		{
 			if (tx != null) tx.rollback();
 			logger.error(e.toString());
 			throw e;
 		}
 		finally
 		{
 			try
 			{
 				hibernateUtil.closeSession();
 			}
 			catch (HibernateException he)
 			{
 				logger.error(he.toString());
 				throw he;
 			}
 		}
 	}
 	
 	/**
 	 * Changes seq number of all modules
 	 * 
 	 * @param session
 	 *        Session object
 	 * @param courseModuleBeans
 	 *        List of courseModuleBeans
 	 */
 	private void assignSeqs(Session session, List courseModuleBeans)
 	{
 		int seqNo = 1;
 		for (ListIterator i = courseModuleBeans.listIterator(); i.hasNext();)
 		{
 			CourseModule cmod = (CourseModule) i.next();
 			if ((cmod.isArchvFlag() == false) && (cmod.isDeleteFlag() == false))
 			{
 				cmod.setSeqNo(seqNo);
 				session.saveOrUpdate(cmod);
 				seqNo++;
 			}
 
 		}
 	}
 
 	/**
 	 * Assign sequence number to the new module. if no sequence number is found in course module table for given courseId assume that its a first module.
 	 * 
 	 * @param session
 	 *        Session object
 	 * @param courseId
 	 *        Course id
 	 * @return sequence number
 	 */
 	private int assignSequenceNumber(Session session, String courseId)
 	{
 		int maxSeq = 1;
 		try
 		{
 			Query q = session
 					.createQuery("select max(cm.seqNo) from CourseModule cm where cm.courseId =:courseId and cm.deleteFlag=0 and cm.archvFlag=0");
 			q.setParameter("courseId", courseId);
 			Integer maxsequence = (Integer) q.uniqueResult();
 
 			// if no sequence is found then its first module.
 			if (maxsequence == null || maxsequence.intValue() <= 0)
 			{
 				return maxSeq;
 			}
 			maxSeq = maxsequence.intValue() + 1;
 
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 			// he.printStackTrace();
 		}
 		return maxSeq;
 
 	}
 
 	/**
 	 * Checks to see how special access blocks apply to modules and returns list of sequences that are blocked
 	 * 
 	 * @param accMap
 	 *        Special access map
 	 * @param dbConnection
 	 *        Database connection
 	 * @return list of sequences that are blocked
 	 */
 	private List checkAccessBlocks(Map accMap, Connection dbConnection)
 	{
 		List removeList = new ArrayList();
 		Iterator it = accMap.entrySet().iterator();
 		String sql;
 		PreparedStatement pstmt = null;
 		ResultSet rs = null;
 		// Check to see if there are any blocked entries in accMap. If so, add them to removeList
 		while (it.hasNext())
 		{
 			Map.Entry pairs = (Map.Entry) it.next();
 			Integer seq = (Integer) pairs.getKey();
 			AccessDates ad = (AccessDates) pairs.getValue();
 			java.sql.Timestamp startTimestamp = null;
 			java.sql.Timestamp endTimestamp = null;
 			if (ad.overrideStart && ad.overrideEnd)
 			{
 				startTimestamp = ad.getAccStartTimestamp();
 				endTimestamp = ad.getAccEndTimestamp();
 				if (isVisible(startTimestamp, endTimestamp))
 				{
 					continue;
 				}
 				else
 				{
 					removeList.add(seq);
 				}
 			}
 			else
 			{
 				try
 				{
 					if (ad.overrideStart)
 					{
 						startTimestamp = ad.getAccStartTimestamp();
 						pstmt = dbConnection.prepareStatement("select end_date from melete_module_shdates where module_id=?");
 						pstmt.setInt(1, ad.getModuleId());
 						rs = pstmt.executeQuery();
 						if (rs != null)
 						{
 							while (rs.next())
 							{
 								endTimestamp = rs.getTimestamp("end_date");
 							}
 						}
 						if (isVisible(startTimestamp, endTimestamp))
 						{
 							continue;
 						}
 						else
 						{
 							removeList.add(seq);
 						}
 					}
 					else
 					{
 						if (ad.overrideEnd)
 						{
 							endTimestamp = ad.getAccEndTimestamp();
 							pstmt = dbConnection.prepareStatement("select start_date from melete_module_shdates where module_id=?");
 							pstmt.setInt(1, ad.getModuleId());
 							rs = pstmt.executeQuery();
 							if (rs != null)
 							{
 								while (rs.next())
 								{
 									startTimestamp = rs.getTimestamp("start_date");
 								}
 							}
 							if (isVisible(startTimestamp, endTimestamp))
 							{
 								continue;
 							}
 							else
 							{
 								removeList.add(seq);
 							}
 						}
 					}
 					rs.close();
 					pstmt.close();
 				}
 				catch (Exception e)
 				{
 					if (logger.isErrorEnabled()) logger.error(e.toString());
 				}
 			}
 		}
 		return removeList;
 	}
 
 	/**
 	 * Correct sections (make sure sequence xml is in line with number of sections)
 	 * 
 	 * @param sectionMap
 	 *        Section map
 	 * @param moduleId
 	 *        The module id
 	 * @param xmlSecList
 	 *        Xml sequence list
 	 * @return Corrected list
 	 */
 	private List correctSections(Map sectionMap, int moduleId, List xmlSecList)
 	{
 		Module mod = null;
 		SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 		String updSeqXml = null;
 		if (sectionMap == null || sectionMap.size() == 0) return null;
 
 		if (sectionMap != null)
 		{
 
 			// Find all entries that are in sectionMap but not in
 			// xmlSecList
 			Set secKeySet = sectionMap.keySet();
 			List newSecList = new ArrayList();
 			Iterator it = secKeySet.iterator();
 			while (it.hasNext())
 			{
 				newSecList.add((Integer) it.next());
 			}
 			List xtraXmlList = new ArrayList();
 
 			// Find all entries that are in xmlSecList but not in
 			// secKeySet
 			it = xmlSecList.iterator();
 			while (it.hasNext())
 			{
 				Integer obj = new Integer(((SecLevelObj) it.next()).getSectionId());
 				if (newSecList.contains(obj))
 				{
 					newSecList.remove(obj);
 					continue;
 				}
 				else
 				{
 					xtraXmlList.add(obj);
 				}
 
 			}
 
 			// newSecList contains entries in the section list that aren't in seqXml
 			// These sections are added to seqXml at the bottom
 			// xtraXmlList contains entries in seqXml that aren't in section list
 			// These entries are deleted from seqXml
 
 			// Both lists are in sync
 			if ((newSecList.size() == 0) && (xtraXmlList.size() == 0))
 			{
 				if (secKeySet.size() == xmlSecList.size())
 				{
 					return xmlSecList;
 				}
 			}
 			else
 			{
 				logger.debug("in correct sections update time module is :" + moduleId );
 				logger.debug("in correct sections update time module is :" + newSecList.toString() );
 				updSeqXml = null;
 				mod = getModule(moduleId);
 				// Add sections to seqXml
 				if (newSecList != null)
 				{
 					if (newSecList.size() > 0)
 					{
 						it = newSecList.iterator();
 						updSeqXml = mod.getSeqXml();
 						while (it.hasNext())
 						{
 							ssuImpl.addSectiontoList(updSeqXml, ((Integer) it.next()).toString());
 							updSeqXml = ssuImpl.storeSubSections();
 						}
 					}
 
 				}// Add sections to seqXml end
 				if ((updSeqXml == null) || (updSeqXml.length() == 0))
 				{
 					updSeqXml = mod.getSeqXml();
 				}
 
 				// Remove sections from seqXml
 				if (xtraXmlList != null)
 				{
 					if (xtraXmlList.size() > 0)
 					{
 						it = xtraXmlList.iterator();
 						while (it.hasNext())
 						{
 							try
 							{
 								updSeqXml = ssuImpl.deleteSection(updSeqXml, Integer.toString((Integer) it.next()));
 							}
 							catch (Exception ex)
 							{
 								logger.error("CorrectSections - Error in deleting section " + ex.toString());
 							}
 						}
 					}
 				}// Remove sections from seqXml end
 				// Update module
 				if ((updSeqXml != null) && (updSeqXml.length() > 0))
 				{
 					mod.setSeqXml(updSeqXml);
 					try
 					{
 						updateModule(mod);
 					}
 					catch (Exception ex)
 					{
 						logger.error("CorrectSections - error in updating module " + ex.toString());
 					}
 					ssuImpl.traverseDom(mod.getSeqXml(), ((Integer) mod.getCoursemodule().getSeqNo()).toString());
 					xmlSecList = ssuImpl.getXmlSecList();
 					return xmlSecList;
 				}
 			}// end else if big condition
 
 		}// end else sectionMap!= null
 		return null;
 	}
 
 	/**
 	 * Create a new calendar event
 	 * 
 	 * @param c
 	 *        Reference to calendar api
 	 * @param eventDate
 	 *        event date
 	 * @param title
 	 *        title
 	 * @param description
 	 *        description
 	 * @return Event id after adding event to calendar
 	 * @throws Exception
 	 */
 	private String createCalendarEvent(org.sakaiproject.calendar.api.Calendar c, Date eventDate, String title, String description) throws Exception
 	{
 		String eventId = null;
 		CalendarEvent eEvent = c.addEvent(/* TimeRange */TimeService.newTimeRange(eventDate.getTime(), 0),
 		/* title */title,
 		/* description */description,
 		/* type */"Deadline",
 		/* location */"",
 		/* attachments */EntityManager.newReferenceList());
 		if (eEvent != null)
 		{
 			eventId = eEvent.getId();
 		}
 		return eventId;
 	}
 
 	/**
 	 * Delete calendar event
 	 * 
 	 * @param c
 	 *        Reference to calendar api
 	 * @param eventId
 	 *        Event id to delete
 	 * @throws Exception
 	 */
 	private void deleteCalendarEvent(org.sakaiproject.calendar.api.Calendar c, String eventId) throws Exception
 	{
 		try
 		{
 			CalendarEventEdit evEdit = c.getEditEvent(eventId, "Deadline");
 			if (evEdit != null)
 			{
 				c.removeEvent(evEdit);
 			}
 		}
 		catch (IdUnusedException idUEx)
 		{
 			logger.debug("deleteCalendarEvent - COULD Not find ID");
 		}
 	}
 
 	/**
 	 * Delete everything from Melete tables
 	 * 
 	 * @param delCourseId
 	 *        Course id to delete
 	 * @param session
 	 *        Session object
 	 * @param allModuleIds
 	 *        String of module ids
 	 * @throws Exception
 	 */
 	private void deleteEverything(String delCourseId, Session session, String allModuleIds) throws Exception
 	{
 		// logger.debug("delete everything for " + delCourseId + allModuleIds);
 		String delMeleteResourceStr = "delete MeleteResource mr where mr.resourceId like '%" + delCourseId + "%'";
 		String delSectionResourceStr = "delete SectionResource sr where sr.resource.resourceId like '%" + delCourseId + "%'";
 		// DelSectionNullResourceStr was added for ME-1300, some entries in the MELETE_SECTION_RESOURCE may have null resource ids
 		// The delSectionResourceStr query would not clear these.
 		String delSectionNullResourceStr = "delete SectionResource sr where sr.section.sectionId in (select s.sectionId from Section s where s.moduleId in "
 				+ allModuleIds + ")";
 		String delBookmarksStr = "delete Bookmark bm where bm.siteId like '%" + delCourseId + "%'";
 		String delSectionViewStr = "delete SectionTrackView stv where stv.section.sectionId in (select s.sectionId from Section s where s.moduleId in "
 				+ allModuleIds + ")";
 		String delSectionStr = "delete Section s where s.moduleId in " + allModuleIds;
 		String delCourseModuleStr = "delete CourseModule cm where cm.courseId= '" + delCourseId + "'";
 		String delModuleshDatesStr = "delete ModuleShdates msh where msh.moduleId in " + allModuleIds;
 		String delSpecialAccStr = "delete SpecialAccess sa where sa.moduleId in " + allModuleIds;
 		String delModuleStr = "delete Module m where m.moduleId in " + allModuleIds;
 
 		int deletedEntities = session.createQuery(delSectionResourceStr).executeUpdate();
 		// logger.debug("deleted sr " + deletedEntities);
 		deletedEntities = session.createQuery(delSectionNullResourceStr).executeUpdate();
 		// logger.debug("deleted sr null " + deletedEntities);
 		deletedEntities = session.createQuery(delBookmarksStr).executeUpdate();
 		// logger.debug("deleted bookmarks " + deletedEntities);
 		deletedEntities = session.createQuery(delSectionViewStr).executeUpdate();
 		// logger.debug("deleted section views " + deletedEntities);
 		deletedEntities = session.createQuery(delSectionStr).executeUpdate();
 		// logger.debug("deleted section " + deletedEntities);
 		deletedEntities = session.createQuery(delModuleshDatesStr).executeUpdate();
 		// logger.debug("deleted msh " + deletedEntities);
 		deletedEntities = session.createQuery(delSpecialAccStr).executeUpdate();
 		// logger.debug("deleted sa " + deletedEntities);
 		deletedEntities = session.createQuery(delCourseModuleStr).executeUpdate();
 		// logger.debug("deleted cm " + deletedEntities);
 		deletedEntities = session.createQuery(delModuleStr).executeUpdate();
 		// logger.debug("deleted module " + deletedEntities);
 		deletedEntities = session.createQuery(delMeleteResourceStr).executeUpdate();
 		// logger.debug("deleted mr " + deletedEntities);
 	}
 
 	/**
 	 * Get special access records for this user in this course
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @param dbConnection
 	 *        database connection
 	 * @return Map of module id and access dates object
 	 * @throws Exception
 	 */
 	private Map<Integer, AccessDates> getAccessRecords(String userId,
 			String courseId, Connection dbConnection) throws Exception {
 		Map<Integer, AccessDates> accMap = new HashMap();
 		String users;
 		if (meleteSecurityService.allowStudent(courseId)) {
 			// String sql =
 			// "select a.module_id,a.start_date,a.end_date,a.override_start,a.override_end from melete_special_access a,melete_course_module c where a.users like ? and (a.start_date is NULL or a.end_date is NULL or a.start_date < a.end_date) and a.module_id=c.module_id and c.course_id = ?";
 			String sql = "select a.users,a.module_id,a.start_date,a.end_date,a.override_start,a.override_end from melete_special_access a,melete_course_module c where (a.start_date is NULL or a.end_date is NULL or a.start_date < a.end_date) and a.module_id=c.module_id and c.course_id = ?";
 
 			PreparedStatement accPstmt = dbConnection.prepareStatement(sql);
 			accPstmt.setString(1, courseId);
 			ResultSet accRs = accPstmt.executeQuery();
 			if (accRs != null) {
 				int accModuleId;
 				while (accRs.next()) {
 					users = accRs.getString("users");
 					accModuleId = accRs.getInt("module_id");
 					if ((users != null) && (users.length() > 0)
 							&& (users.contains(userId))) {
 						AccessDates ad = new AccessDates(accModuleId,
 								accRs.getTimestamp("start_date"),
 								accRs.getTimestamp("end_date"),
 								accRs.getBoolean("override_start"),
 								accRs.getBoolean("override_end"));
 						accMap.put(accModuleId, ad);
 					}
 				}
 				accRs.close();
 				accPstmt.close();
 			}
 		}
 		return accMap;
 	}
 
 	/**
 	 * Get all section ids in a string
 	 * 
 	 * @param deletedSections
 	 *        Map of sections
 	 * @return String of section ids that are comma delimited
 	 */
 	private String getAllSectionIds(Map deletedSections)
 	{
 		StringBuffer allIds = null;
 		String a = null;
 		if (deletedSections != null)
 		{
 			allIds = new StringBuffer("(");
 			for (Iterator i = deletedSections.keySet().iterator(); i.hasNext();)
 			{
 				Object obj = i.next();
 				allIds.append(obj + ",");
 			}
 		}
 		if (allIds != null && allIds.lastIndexOf(",") != -1) a = allIds.substring(0, allIds.lastIndexOf(",")) + " )";
 		return a;
 	}
 
 	/**
 	 * Get string of section ids of sections to be deleted
 	 * 
 	 * @param session
 	 *        Session object
 	 * @param moduleId
 	 *        Module to check
 	 * @return String of section ids that are comma delimited
 	 */
 	private String getDelSectionIds(Session session, int moduleId)
 	{
 		StringBuffer delIds = null;
 		String a = null;
 		String selectDelsecStr = "select sec.sectionId from Section sec where sec.deleteFlag=1 and sec.moduleId=:moduleId";
 		List<String> deletedSections = session.createQuery(selectDelsecStr).setInteger("moduleId", moduleId).list();
 		if (deletedSections != null)
 		{
 			delIds = new StringBuffer("(");
 			for (Iterator i = deletedSections.iterator(); i.hasNext();)
 			{
 				Object obj = i.next();
 				delIds.append(obj + ",");
 			}
 		}
 		if (delIds != null && delIds.lastIndexOf(",") != -1) a = delIds.substring(0, delIds.lastIndexOf(",")) + " )";
 
 		return a;
 	}
 
 	/**
 	 * Get license information
 	 * 
 	 * @param melResource
 	 *        Melete resource object
 	 * @return License info as a string
 	 */
 	private String getLicenseInformation(MeleteResource melResource)
 	{
 		ResourceLoader rl = new ResourceLoader("melete_license");
 		String licenseStr = "";
 
 		if (melResource == null || melResource.getLicenseCode() == 0) return licenseStr;
 		int lcode = melResource.getLicenseCode();
 		switch (lcode)
 		{
 			case 1:
 				licenseStr = rl.getString("license_info_copyright");
 				break;
 			case 2:
 				licenseStr = rl.getString("license_info_dedicated_to");
 				break;
 			case 3:
 				licenseStr = rl.getString("license_info_licensed_under");
 				break;
 			case 4:
 				licenseStr = rl.getString("license_info_fairuse");
 				break;
 			default:
 				break;
 		}
 		if (melResource.getCopyrightYear() != null && melResource.getCopyrightYear().length() > 0)
 			licenseStr += " " + melResource.getCopyrightYear();
 		if (melResource.getCopyrightOwner() != null && melResource.getCopyrightOwner().length() > 0)
 			licenseStr += ", " + melResource.getCopyrightOwner();
 		return licenseStr;
 	}
 
 	/**
 	 * Determine read date of a module (max read date of all its sections)
 	 * 
 	 * @param moduleId
 	 *        Module id
 	 * @param sectionMap
 	 *        Section map
 	 * @param userId
 	 *        User id
 	 * @param dbConnection
 	 *        Db connection
 	 * @param secTrackMap
 	 *        Map that contains section id and view date
 	 * @return max read date of all the module's sections or null
 	 * @throws SQLException
 	 */
 	private Date getReadDate(int moduleId, Map sectionMap, String userId, Connection dbConnection, Map<Integer,Date> secTrackMap) throws SQLException
 	{
 		logger.debug("ModuleDB:get Read date");
 		Date viewDate = null;
 		java.sql.Timestamp viewTimestamp = null;
 
 		if (sectionMap == null || sectionMap.size() == 0)
 		{
 			return null;
 		}
 
 		if (sectionMap != null)
 		{
 			// Find all entries that are in sectionMap but not in
 			// xmlSecList
 			Set secKeySet = sectionMap.keySet();
 			List secList = new ArrayList();
 			Iterator it = secKeySet.iterator();
 			while (it.hasNext())
 			{
 				secList.add((Integer) it.next());
 			}
 			ResultSet rs = null;
 
 			String sql = "select sv.section_id,sv.view_date from melete_section_track_view sv,melete_section ms where sv.user_id = ? and sv.section_id = ms.section_id and ms.module_id = ? order by sv.view_date";
 			PreparedStatement pstmt = dbConnection.prepareStatement(sql);
 			pstmt.setString(1, userId);
 			pstmt.setInt(2, moduleId);
 			rs = pstmt.executeQuery();
 	
 			if (rs != null)
 			{
 				int sectionId;
 				while (rs.next())
 				{
 					sectionId = rs.getInt("section_id");
 					viewTimestamp = rs.getTimestamp("view_date");
 					if (viewTimestamp != null) viewDate = new java.util.Date(viewTimestamp.getTime() + (viewTimestamp.getNanos() / 1000000));
 					secTrackMap.put(new Integer(sectionId), viewDate);
 				}
 			}
 			rs.close();
 			pstmt.close();
 
 		}
 
 		return viewDate;
 	}
 	
 	private Date getReadDate(Map secViewMap) {
 		Date maxViewDate = null;
 		if ((secViewMap == null)||(secViewMap.size() == 0))
 			return null;
 		Iterator it = secViewMap.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry pairs = (Map.Entry) it.next();
 			if (pairs.getValue() != null) {
 				maxViewDate = (Date) pairs.getValue();
 				break;
 			}
 		}
 		it = null;
 		it = secViewMap.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry pairs = (Map.Entry) it.next();
 			if (pairs.getValue() != null) {
 				if (((Date) pairs.getValue()).after(maxViewDate))
 					maxViewDate = (Date) pairs.getValue();
 			}
 		}
 		return maxViewDate;
 	}
 
 	/**
 	 * This method returns next or prev seq number for students depending on how it is invoked. It takes into account blocked modules and special access
 	 * and invalid modules.
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @param currSeqNo
 	 *        Current sequence number
 	 * @param prevFlag
 	 *        true mean get previous seq number, false means get next sequence number
 	 * @return sequence number
 	 */
 	private int getStudentNavSeqNo(String userId, String courseId, int currSeqNo, boolean prevFlag)
 	{
 		Connection dbConnection = null;
 		List resList = new ArrayList();
 		java.sql.Timestamp currentTimestamp = null;
 		int navSeqNo = -1;
 		String sql;
         try
 		{
 			dbConnection = SqlService.borrowConnection();
 			ResultSet rs, accRs = null;
 			// First get all sequence numbers after this one from course module table
 			if (prevFlag)
 			{
 				sql = "select cm.seq_no, cm.module_id, count(s.module_id) as secCount from melete_course_module cm,melete_module_shdates msh,melete_section s where cm.course_id = ? and cm.delete_flag = 0 and cm.archv_flag = 0 and cm.seq_no < ? and cm.module_id = s.module_id and cm.module_id = msh.module_id and ((msh.start_date is null or msh.start_date < ?) and (msh.end_date is null or msh.end_date > ?)) group by cm.seq_no order by cm.seq_no desc";
 			}
 			else
 			{
 				sql = "select cm.seq_no, cm.module_id, count(s.module_id) as secCount from melete_course_module cm,melete_module_shdates msh,melete_section s where cm.course_id = ? and cm.delete_flag = 0 and cm.archv_flag = 0 and cm.seq_no > ? and cm.module_id = s.module_id and cm.module_id = msh.module_id and ((msh.start_date is null or msh.start_date < ?) and (msh.end_date is null or msh.end_date > ?)) group by cm.seq_no order by cm.seq_no";
 			}
 			PreparedStatement pstmt = dbConnection.prepareStatement(sql);
 			pstmt.setString(1, courseId);
 			pstmt.setInt(2, currSeqNo);
 			currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
 			pstmt.setTimestamp(3, currentTimestamp);
 			pstmt.setTimestamp(4, currentTimestamp);
 			rs = pstmt.executeQuery();
 			this.accessAdvisor = (AccessAdvisor) ComponentManager.get(AccessAdvisor.class);
 			//Check if any of these modules are blocked by CM. Add ones that are not blocked to resList
 			if (rs != null)
 			{
 				// Add them to resList
 				while (rs.next())
 				{
 					int moduleId = rs.getInt("module_id");
 					// skip modules with no sections
 					int count = rs.getInt("secCount");
 					if (count <= 0) continue;
 					// Check to see if module is blocked via coursemap, only add to resList otherwise
 					if ((this.accessAdvisor != null) && (this.accessAdvisor.denyAccess("sakai.melete", courseId, String.valueOf(moduleId), userId)))
 					{
 						continue;
 					}
 					else
 					{
 						resList.add(rs.getInt("seq_no"));
 					}
 				}
 			}
 			// Get all access entries for user after/before this seq number
 			if (prevFlag)
 			{
 				sql = "select cm.seq_no, sa.module_id, sa.start_date, sa.end_date, sa.override_start, sa.override_end from melete_course_module cm,melete_special_access sa where cm.course_id = ? and cm.delete_flag = 0 and cm.archv_flag = 0 and cm.seq_no < ? and cm.module_id = sa.module_id and sa.users like ? and (sa.start_date is null or sa.end_date is null or sa.start_date < sa.end_date) order by cm.seq_no desc";
 			}
 			else
 			{
 				sql = "select cm.seq_no, sa.module_id, sa.start_date, sa.end_date, sa.override_start, sa.override_end from melete_course_module cm,melete_special_access sa where cm.course_id = ? and cm.delete_flag = 0 and cm.archv_flag = 0 and cm.seq_no > ? and cm.module_id = sa.module_id and sa.users like ? and (sa.start_date is null or sa.end_date is null or sa.start_date < sa.end_date) order by cm.seq_no";
 			}
 			PreparedStatement accPstmt = dbConnection.prepareStatement(sql);
 			accPstmt.setString(1, courseId);
 			accPstmt.setInt(2, currSeqNo);
 			accPstmt.setString(3, "%" + userId + "%");
 			accRs = accPstmt.executeQuery();
 			Map accMap = new HashMap();
 			//Check if any of these modules are blocked by CM. Add ones that are not blocked to accMap
 			if (accRs != null)
 			{
 				// Add them to accMap
 				while (accRs.next())
 				{
 					int moduleId = accRs.getInt("module_id");
 					//Check to see if modules granted to by special access are blocked by course map ME-1377
 					if ((this.accessAdvisor != null) && (this.accessAdvisor.denyAccess("sakai.melete", courseId, String.valueOf(moduleId), userId)))
 					  continue;
 					else
 					{	
 					  AccessDates ad = new AccessDates(moduleId, accRs.getTimestamp("start_date"), accRs.getTimestamp("end_date"),
 						accRs.getBoolean("override_start"), accRs.getBoolean("override_end"));
 					  accMap.put(accRs.getInt("seq_no"), ad);
 					}  
 				}
 			}
 			accRs.close();
 			accPstmt.close();
 			// If there are no access entries, return the first entry in resList
 			if ((accMap == null) || (accMap.size() == 0))
 			{
 				if (resList.size() == 0)
 					navSeqNo = -1;
 				else
 					navSeqNo = ((Integer) resList.get(0)).intValue();
 			}
 			else
 			{
 				// Check to see if user's access blocks any modules
 				List removeList = checkAccessBlocks(accMap, dbConnection);
 
 				// If there are blocked entries, remove them from both resList and accMap
 				if (removeList.size() > 0)
 				{
 					for (Iterator itr = removeList.listIterator(); itr.hasNext();)
 					{
 						Integer seq = (Integer) itr.next();
 						if (resList.size() > 0)
 						{
 							if (resList.indexOf(seq) != -1) resList.remove(seq);
 						}
 						accMap.remove(seq);
 					}
 				}
 				// Return sequence number appropriately
 				if ((resList.size() == 0) && (accMap.size() == 0))
 				{
 					navSeqNo = -1;
 				}
 				if ((resList.size() == 0) && (accMap.size() > 0))
 					navSeqNo = ((Integer) ((Map.Entry) accMap.entrySet().iterator().next()).getKey()).intValue();
 				if ((resList.size() > 0) && (accMap.size() == 0)) navSeqNo = ((Integer) resList.get(0)).intValue();
 				if ((resList.size() > 0) && (accMap.size() > 0))
 				{
 					if (prevFlag)
 					{
 						navSeqNo = Math.max(((Integer) ((Map.Entry) accMap.entrySet().iterator().next()).getKey()).intValue(), ((Integer) resList
 								.get(0)).intValue());
 					}
 					else
 					{
 						navSeqNo = Math.min(((Integer) ((Map.Entry) accMap.entrySet().iterator().next()).getKey()).intValue(), ((Integer) resList
 								.get(0)).intValue());
 					}
 				}	
 			}
 			rs.close();
 			pstmt.close();
 		}
 		catch (Exception e)
 		{
 			if (logger.isErrorEnabled()) logger.error(e.toString());
 		}
 		finally
 		{
 			try
 			{
 				if (dbConnection != null) SqlService.returnConnection(dbConnection);
 			}
 			catch (Exception e1)
 			{
 				if (logger.isErrorEnabled()) logger.error(e1.toString());
 			}
 		}
 		return navSeqNo;
 	}
 
 	/**
 	 * Get sql timestamp value of string time
 	 * 
 	 * @param dateTime
 	 * @return sql timestamp value
 	 */
 	private java.sql.Timestamp getTime(String dateTime)
 	{
 		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
 		Date dd = null;
 		try
 		{
 			dd = sdf.parse(dateTime);
 		}
 		catch (Exception pe)
 		{
 			if (logger.isErrorEnabled()) logger.error(pe);
 		}
 		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.fffffffff");
 		String jdbcTime = sdf1.format(dd);
 		return java.sql.Timestamp.valueOf(jdbcTime);
 	}
 
 	/**
 	 * Determines if this module is currently visible
 	 * 
 	 * @param startTimestamp
 	 *        Start date
 	 * @param endTimestamp
 	 *        End date
 	 * @return true if module is visible, false if not
 	 */
 	private boolean isVisible(java.sql.Timestamp startTimestamp, java.sql.Timestamp endTimestamp)
 	{
 		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
 		if (((startTimestamp == null) || (startTimestamp.before(currentTimestamp)))
 				&& ((endTimestamp == null) || (endTimestamp.after(currentTimestamp))))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/**
 	 * Populate module date bean object
 	 * 
 	 * @param mod
 	 *        Module object
 	 * @param mdBean
 	 *        ModuleDateBean object
 	 */
 	private void populateModuleBean(Module mod, ModuleDateBean mdBean)
 	{
 		String modSeq;
 		SubSectionUtilImpl ssuImpl;
 		StringBuffer rowClassesBuf;
 		List sectionBeanList = null;
 		Map sectionMap = null;
 		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
 
 		if (mod == null) mod = new Module();
 		ModuleShdates mshdate = (ModuleShdates) mod.getModuleshdate();
 		int moduleId = mod.getModuleId().intValue();
 		mdBean.setModuleId(moduleId);
 		mdBean.setModule((Module) mod);
 		mdBean.setModuleShdate(mod.getModuleshdate());
 		mdBean.setCmod(mod.getCoursemodule());
 		mdBean.setTruncTitle(createTruncstr(mod.getTitle()));
 
 		sectionMap = mod.getSections();
 
 		if (sectionMap != null)
 		{
 			if (sectionMap.size() > 0)
 			{
 				modSeq = Integer.toString(mod.getCoursemodule().getSeqNo());
 				ssuImpl = new SubSectionUtilImpl();
 				ssuImpl.traverseDom(mod.getSeqXml(), modSeq);
 				xmlSecList = ssuImpl.getXmlSecList();
 				sectionBeanList = new ArrayList();
 				rowClassesBuf = new StringBuffer();
 
 				xmlSecList = correctSections(sectionMap, moduleId, xmlSecList);
 				processSections(sectionMap, sectionBeanList, xmlSecList, rowClassesBuf);
 				mdBean.setSectionBeans(sectionBeanList);
 				mdBean.setRowClasses(rowClassesBuf.toString());
 			}
 		}
 
 	}
 
 	/**
 	 * Process sections and assign css classes for rows depending on depth
 	 * 
 	 * @param vsBeanMap
 	 *        Section map
 	 * @param vsBeanList
 	 *        Section bean list
 	 * @param xmlSecList
 	 *        list that contains hierarchy info
 	 * @param rowClassesBuf
 	 *        css class for indentation
 	 */
 	private void processSections(Map sectionMap, List sectionBeanList, List xmlSecList, StringBuffer rowClassesBuf)
 	{
 		Section sec = null;
 		SectionBeanService secBean = null;
 
 		if ((sectionMap != null) && (xmlSecList != null))
 		{
 			if (sectionMap.size() == xmlSecList.size())
 			{
 				for (ListIterator k = xmlSecList.listIterator(); k.hasNext();)
 				{
 					SecLevelObj slObj = (SecLevelObj) k.next();
 					if (slObj != null)
 					{
 						sec = (Section) sectionMap.get(new Integer(slObj.getSectionId()));
 						if (sec != null)
 						{
 							secBean = new SectionBean(sec);
 							secBean.setTruncTitle(createTruncstr(sec.getTitle()));
 							secBean.setDisplaySequence(slObj.getDispSeq());
 							sectionBeanList.add(secBean);
 							rowClassesBuf.append("secrow" + slObj.getLevel() + ",");
 						}
 					}
 				}
 				rowClassesBuf.delete(rowClassesBuf.toString().length() - 1, rowClassesBuf.toString().length());
 			}
 		}
 	}
 
 	/**
 	 * Process sections and assign css classes for rows depending on depth
 	 * 
 	 * @param vsBeanMap
 	 *        Section map
 	 * @param vsBeanList
 	 *        Section bean list
 	 * @param xmlSecList
 	 *        list that contains hierarchy info
 	 * @param rowClassesBuf
 	 *        css class for indentation
 	 */
 	private void processViewSections(Map vsBeanMap, List vsBeanList, List xmlSecList, StringBuffer rowClassesBuf)
 	{
 		ViewSecBeanService vsBean = null;
 		// SectionBean secBean = null;
 
 		if ((vsBeanMap != null) && (xmlSecList != null))
 		{
 			if (vsBeanMap.size() == xmlSecList.size())
 			{
 				for (ListIterator k = xmlSecList.listIterator(); k.hasNext();)
 				{
 					SecLevelObj slObj = (SecLevelObj) k.next();
 					if (slObj != null)
 					{
 						vsBean = (ViewSecBeanService) vsBeanMap.get(new Integer(slObj.getSectionId()));
 						if (vsBean != null)
 						{
 							vsBean.setDisplaySequence(slObj.getDispSeq());
 							vsBean.setDisplayClass("seccol" + slObj.getLevel());
 							vsBeanList.add(vsBean);
 							rowClassesBuf.append("secrow" + slObj.getLevel() + ",");
 						}
 					}
 				}
 				rowClassesBuf.delete(rowClassesBuf.toString().length() - 1, rowClassesBuf.toString().length());
 			}
 		}
 	}
 
 	/**
 	 * Update calendar event
 	 * 
 	 * @param c
 	 *        Reference to calendar api
 	 * @param eventId
 	 *        Event id of calendar event
 	 * @param eventDate
 	 *        Event date
 	 * @param title
 	 *        title
 	 * @param description
 	 *        description
 	 * @return event id, the same one if updating, a new one if new
 	 * @throws Exception
 	 */
 	private String updateCalendarEvent(org.sakaiproject.calendar.api.Calendar c, String eventId, Date eventDate, String title, String description)
 			throws Exception
 	{
 		try
 		{
 			CalendarEventEdit evEdit = c.getEditEvent(eventId, "Deadline");
 			if (evEdit != null)
 			{
 				evEdit.setRange(TimeService.newTimeRange(eventDate.getTime(), 0));
 				evEdit.setDescription(description);
 				c.commitEvent(evEdit);
 				return eventId;
 			}
 		}
 		catch (IdUnusedException idUEx)
 		{
 			logger.debug("In updateCalendarEvent COULD Not find ID creating new event");
 			String newEventId = createCalendarEvent(c, eventDate, title, description);
 			return newEventId;
 		}
 		return eventId;
 	}
 
 	/**
 	 * Get list of resource ids belonging to modules
 	 * 
 	 * @param activenArchModules
 	 *        List of modules to check
 	 * @return List of embedded and section associated resources
 	 */
 	protected List<String> getActiveResourcesFromList(List activenArchModules)
 	{
 		List<String> secEmbed = new ArrayList();
 		try
 		{
 			Iterator<Module> i = activenArchModules.iterator();
 			while (i.hasNext())
 			{
 				Module mod = i.next();
 				Map sectionMap = mod.getSections();
 				Iterator it = sectionMap.entrySet().iterator();
 				while (it.hasNext())
 				{
 					Map.Entry pairs = (Map.Entry) it.next();
 					Section sec = (Section) pairs.getValue();
 
 					if (sec == null || sec.getContentType() == null || sec.getContentType().equals("notype") || sec.getSectionResource() == null
 							|| sec.getSectionResource().getResource() == null) continue;
 
 					if (sec.getContentType().equals("typeEditor"))
 					{
 						secEmbed.add(sec.getSectionResource().getResource().getResourceId());
 						List l = meleteCHService.findAllEmbeddedImages(sec.getSectionResource().getResource().getResourceId());
 						if (l != null) secEmbed.addAll(l);
 
 					}
 					else
 						secEmbed.add(sec.getSectionResource().getResource().getResourceId());
 				}
 			}
 			// logger.debug("before sorting and removing dups" + secEmbed.size());
 			// sort list and remove duplicates
 			SortedSet s = new TreeSet();
 			s.addAll(secEmbed);
 			secEmbed.clear();
 			secEmbed.addAll(s);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 		return secEmbed;
 	}
 
 	/**
 	 * Get list of resources in the uploads collection
 	 * 
 	 * @param toDelCourseId
 	 *        Course id
 	 * @return list of resources in the uploads collection
 	 */
 	protected List getAllMeleteResourcesOfCourse(String toDelCourseId)
 	{
 		List allres = meleteCHService.getListofMediaFromCollection("/private/meleteDocs/" + toDelCourseId + "/uploads/");
 		ArrayList<String> allresNames = new ArrayList();
 		if (allres == null) return null;
 		for (Iterator iter = allres.listIterator(); iter.hasNext();)
 		{
 			ContentResource cr = (ContentResource) iter.next();
 			allresNames.add(cr.getId());
 		}
 		SortedSet s = new TreeSet();
 		s.addAll(allresNames);
 		allresNames.clear();
 		allresNames.addAll(s);
 		return allresNames;
 	}
 
 	/**
 	 * Adds an archived module that sends it archived status via the coursemodule object
 	 * 
 	 * @param module
 	 *        module object
 	 * @param moduleshowdates
 	 *        module dates object
 	 * @param userId
 	 *        user id
 	 * @param courseId
 	 *        course id
 	 * @param coursemodule
 	 *        course module object
 	 * @throws Exception
 	 */
 	void addArchivedModule(Module module, ModuleShdates moduleshowdates, String userId, String courseId, CourseModule coursemodule) throws Exception
 	{
 
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 
 			try
 			{
 				module.setCreationDate(new java.util.Date());
 				module.setUserId(userId);
 				// module.setModificationDate(new java.util.Date());
 
 				moduleshowdates.setModule(module);
 
 				tx = session.beginTransaction();
 				// save module
 
 				session.save(module);
 
 				// save module show dates
 				session.save(moduleshowdates);
 
 				// save course module
 				session.save(coursemodule);
 
 				CourseModule cms = (CourseModule) module.getCoursemodule();
 				if (cms == null)
 				{
 					cms = coursemodule;
 				}
 				module.setCoursemodule(cms);
 
 				session.saveOrUpdate(module);
 
 				tx.commit();
 				logger.debug("add module success" + module.getModuleId() + module.getCoursemodule().getCourseId());
 				return;
 
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				// he.printStackTrace();
 				throw he;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception ex)
 		{
 			// Throw application specific error
 			logger.error("error at module db level");
 			throw new MeleteException("add_module_fail");
 		}
 
 	}
 
 	/**
 	 * Checks to see if calendar tool exists in the current site
 	 * 
 	 * @return true if calendar exists, false otherwise
 	 */
 	boolean checkCalendar(String courseId)
 	{
 		Site site = null;
 		try
 		{
 			site = SiteService.getSite(courseId);
 		}
 		catch (Exception e)
 		{
 			logger.debug("Exception thrown while getting site" + e.toString());
 		}
 		if (site.getToolForCommonId("sakai.schedule") != null)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/**
 	 * Delete a list of module and calendar events associated with them
 	 * 
 	 * @param delModules
 	 *        List of modules to delete
 	 * @param courseId
 	 *        Course id
 	 */
 	void deleteCalendar(List delModules, String courseId)
 	{
 		if (checkCalendar(courseId) == true)
 		{
 			// Delete all calendar associated events
 			CalendarService cService = org.sakaiproject.calendar.cover.CalendarService.getInstance();
 			String calendarId = cService.calendarReference(courseId, SiteService.MAIN_CONTAINER);
 			try
 			{
 				org.sakaiproject.calendar.api.Calendar c = cService.getCalendar(calendarId);
 				for (ListIterator i = delModules.listIterator(); i.hasNext();)
 				{
 					Module mod = (Module) i.next();
 					String startEventId = mod.getModuleshdate().getStartEventId();
 					String endEventId = mod.getModuleshdate().getEndEventId();
 					try
 					{
 						if (startEventId != null)
 						{
 							logger.debug("REMOVING start event for null start date");
 							deleteCalendarEvent(c, startEventId);
 							mod.getModuleshdate().setStartEventId(null);
 						}
 						if (endEventId != null)
 						{
 							logger.debug("REMOVING end event for null start date");
 							deleteCalendarEvent(c, endEventId);
 							mod.getModuleshdate().setEndEventId(null);
 						}
 						if ((startEventId != null) || (endEventId != null))
 						{
 							updateModuleShdates((ModuleShdates) mod.getModuleshdate());
 						}
 					}
 					catch (PermissionException ee)
 					{
 						logger.warn("PermissionException while adding to calendar");
 					}
 					catch (Exception ee)
 					{
 						logger.error("Some other exception while adding to calendar " + ee.getMessage());
 					}
 				}
 				// try-catch
 			}
 			catch (Exception ex)
 			{
 				logger.error("Exception thrown while getting Calendar");
 			}
 		}
 	}
 
 	/**
 	 * Updates the calendar tool. Seggregated from the other updateCalendar method.
 	 * 
 	 * @param moduleTitle
 	 * @param moduleshdates1
 	 * @param courseId
 	 * @return
 	 * @throws Exception
 	 */
 	protected ModuleShdates updateCalendar(String moduleTitle, ModuleShdates moduleshdates1, String courseId) throws Exception
 	{
 		if (checkCalendar(courseId) == true)
 		{
 			// The code below adds the start and stop dates to the Calendar
 			boolean addtoSchedule = moduleshdates1.getAddtoSchedule().booleanValue();
 			Date startDate = moduleshdates1.getStartDate();
 			Date endDate = moduleshdates1.getEndDate();
 			String startEventId = moduleshdates1.getStartEventId();
 			String endEventId = moduleshdates1.getEndEventId();
 			
 			CalendarService cService = org.sakaiproject.calendar.cover.CalendarService.getInstance();
 			String calendarId = cService.calendarReference(courseId, SiteService.MAIN_CONTAINER);
 			try
 			{
 				org.sakaiproject.calendar.api.Calendar c = cService.getCalendar(calendarId);
 
 				if (addtoSchedule == true)
 				{
 					//Fix for ME-1426, when start date is after end date, do not add events to calendar
 					if ((startDate != null)&&(endDate != null)&&(startDate.after(endDate)))
 					{
 						if (startEventId != null)
 						{
 							logger.debug("REMOVING start event for null start date");
 							deleteCalendarEvent(c, startEventId);
 							moduleshdates1.setStartEventId(null);
 						}
 						else
 						{
 							moduleshdates1.setStartEventId(null);
 						}
 						if (endEventId != null)
 						{
 							logger.debug("REMOVING end event for null end date");
 							deleteCalendarEvent(c, endEventId);
 							moduleshdates1.setEndEventId(null);
 						}
 						else
 						{
 							moduleshdates1.setEndEventId(null);
 						}
 						return moduleshdates1;
 					}
 					if (startDate == null)
 					{
 						if (startEventId != null)
 						{
 							logger.debug("REMOVING start event for null start date");
 							deleteCalendarEvent(c, startEventId);
 							moduleshdates1.setStartEventId(null);
 						}
 					}
 					else
 					{
 						if (startEventId == null)
 						{
 							logger.debug("ADDING start event for non-null start date");
 							String desc = endDate != null ? "This module opens today and closes " + endDate.toString() : "This module opens today";
 							startEventId = createCalendarEvent(c, startDate, "Opens: " + moduleTitle, desc);
 						}
 						else
 						{
 							logger.debug("UPDATING start event for non-nul start date");
 							String desc = endDate != null ? "This module opens today and closes " + endDate.toString() : "This module opens today";
 							startEventId = updateCalendarEvent(c, startEventId, startDate, "Opens: " + moduleTitle, desc);
 						}
 						moduleshdates1.setStartEventId(startEventId);
 					}
 					if (endDate == null)
 					{
 						if (endEventId != null)
 						{
 							logger.debug("REMOVING end event for null end date");
 							deleteCalendarEvent(c, endEventId);
 							moduleshdates1.setEndEventId(null);
 						}
 					}
 					if (endDate != null)
 					{
 						if (endEventId == null)
 						{
 							logger.debug("ADDING end event for non-null end date");
 							String desc = "This module closes today";
 							endEventId = createCalendarEvent(c, endDate, "Closes: " + moduleTitle, desc);
 						}
 						else
 						{
 							logger.debug("UPDATING end event for non-null end date");
 							String desc = "This module closes today";
 							endEventId = updateCalendarEvent(c, endEventId, endDate, "Closes: " + moduleTitle, desc);
 						}
 						moduleshdates1.setEndEventId(endEventId);
 					}
 				}
 				else
 				{
 					if (startEventId != null)
 					{
 						logger.debug("REMOVING start event for false flag");
 						deleteCalendarEvent(c, startEventId);
 						moduleshdates1.setStartEventId(null);
 					}
 					if (endEventId != null)
 					{
 						logger.debug("REMOVING end event for false flag");
 						deleteCalendarEvent(c, endEventId);
 						moduleshdates1.setEndEventId(null);
 					}
 				}
 			}
 			catch (PermissionException ee)
 			{
 				logger.warn("PermissionException while adding to calendar");
 			}
 			catch (Exception ee)
 			{
 				logger.error("Some other exception while adding to calendar " + ee.getMessage());
 			}
 		}
 		return moduleshdates1;
 	}
 	
 	/**
 	 * Update calendar tool with module dates
 	 * 
 	 * @param module1
 	 *        Module object
 	 * @param moduleshdates1
 	 *        Moduleshdates object
 	 * @param courseId
 	 *        Course id
 	 * @throws Exception
 	 */
 	void updateCalendar(Module module1, ModuleShdates moduleshdates1, String courseId) throws Exception
 	{
 		try
 		{
 			moduleshdates1 = updateCalendar(module1.getTitle(), moduleshdates1, courseId);
 			updateModuleShdates(moduleshdates1);
 		}
 		catch (Exception ex)
 		{
 			logger.error("Exception thrown while getting Calendar");
 		}
 	}
 
 }
 
 class AccessDates
 {
 	java.sql.Timestamp accEndTimestamp;
 	java.sql.Timestamp accStartTimestamp;
 	int moduleId;
 	boolean overrideEnd;
 	boolean overrideStart;
 
 	AccessDates(int moduleId, java.sql.Timestamp accStartTimestamp, java.sql.Timestamp accEndTimestamp, boolean overrideStart, boolean overrideEnd)
 	{
 		this.moduleId = moduleId;
 		this.accStartTimestamp = accStartTimestamp;
 		this.accEndTimestamp = accEndTimestamp;
 		this.overrideStart = overrideStart;
 		this.overrideEnd = overrideEnd;
 	}
 
 	/**
 	 * @return the access end timestamp
 	 */
 	public java.sql.Timestamp getAccEndTimestamp()
 	{
 		return this.accEndTimestamp;
 	}
 
 	/**
 	 * @return the access start timestamp
 	 */
 	public java.sql.Timestamp getAccStartTimestamp()
 	{
 		return this.accStartTimestamp;
 	}
 
 	/**
 	 * @return the moduleId
 	 */
 	public int getModuleId()
 	{
 		return moduleId;
 	}
 
 	/**
 	 * @return override end flag value
 	 */
 	public boolean isOverrideEnd()
 	{
 		return this.overrideEnd;
 	}
 
 	/**
 	 * @return override start flag value
 	 */
 	public boolean isOverrideStart()
 	{
 		return this.overrideStart;
 	}
 
 	/**
 	 * @param accEndTimestamp
 	 *        the acces end timestamp
 	 */
 	public void setAccEndTimestamp(java.sql.Timestamp accEndTimestamp)
 	{
 		this.accEndTimestamp = accEndTimestamp;
 	}
 
 	/**
 	 * @param accStartTimestamp
 	 *        the access start timestamp
 	 */
 	public void setAccStartTimestamp(java.sql.Timestamp accStartTimestamp)
 	{
 		this.accStartTimestamp = accStartTimestamp;
 	}
 
 	/**
 	 * @param moduleId
 	 *        the moduleId to set
 	 */
 	public void setModuleId(int moduleId)
 	{
 		this.moduleId = moduleId;
 	}
 
 	/**
 	 * @param overrideEnd
 	 *        override end flag value
 	 */
 	public void setOverrideEnd(boolean overrideEnd)
 	{
 		this.overrideEnd = overrideEnd;
 	}
 
 	/**
 	 * @param overrideStart
 	 *        override start flag value
 	 */
 	public void setOverrideStart(boolean overrideStart)
 	{
 		this.overrideStart = overrideStart;
 	}
 }
