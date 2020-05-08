 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.persistence;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.INameMapper;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.ParamNameBP;
 import org.eclipse.jubula.client.core.businessprocess.ProjectNameBP;
 import org.eclipse.jubula.client.core.businessprocess.UsedToolkitBP;
 import org.eclipse.jubula.client.core.businessprocess.progress.OperationCanceledUtil;
 import org.eclipse.jubula.client.core.businessprocess.progress.ProgressMonitorTracker;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProjectLoadedListener;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectNamePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.toolkit.common.businessprocess.ToolkitSupportBP;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.toolkit.common.utils.ToolkitUtils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.JBFatalAbortException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.jarutils.IVersion;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created 15.07.2005
  */
 public class ProjectPM extends PersistenceManager 
     implements IProjectLoadedListener {
 
     /** 
      * number of add/insert-related Persistence event types with 
      * progress listeners 
      */
     // Event types:
     // save, recreateCollection, postInsert, postUpdate
     private static final int NUM_HBM_ADD_PROGRESS_EVENT_TYPES = 4;
 
     /** standard logging */
     private static Logger log = LoggerFactory.getLogger(ProjectPM.class);
 
     /** project guid cache */
     private static Map<Long, String> guidCache = new HashMap<Long, String>(17);
     
     /** reused projects cache */
     private static Map<Long, List<IReusedProjectPO>> rpCache = 
             new HashMap<Long, List<IReusedProjectPO>>(17);
     /**
      * constructor must be hidden for class utilities (per CheckStyle)
      */
     private ProjectPM() {
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.addProjectLoadedListener(this, true);
     }
 
     // provide a base for cache clearing when projects are loaded
     static {
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ProjectPM anchor = new ProjectPM();
         ded.addProjectLoadedListener(anchor, true);      
     }
     
     /**
      * @see IProjectLoadedListener#handleProjectLoaded()
      */
     public void handleProjectLoaded() {
         clearCaches();        
     }
     
     /**
      * drop all cached data
      */
     public static void clearCaches() {
         rpCache.clear();
         guidCache.clear();
     }
     /**
      * @return list with all available projects from database the project
      *         instances are detached from their session
      * @throws JBException
      *             ...
      */
     public static synchronized List<IProjectPO> findAllProjects() 
         throws JBException {
         
         ProjectNameBP.getInstance().clearCache();
         EntityManager session = null;
         try {
             session = Persistor.instance().openSession();
             return findAllProjects(session);
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
     }
 
     /**
      * 
      * @param sess The session in which to execute the query.
      * @return list with all available projects from database the project
      *         instances are detached from their session
      * @throws PersistenceException if a Persistence error occurs.
      *             
      */
     @SuppressWarnings("unchecked")
     public static synchronized List<IProjectPO> findAllProjects(
             EntityManager sess) throws PersistenceException {
     
         Query query = sess.createQuery("select project from ProjectPO" //$NON-NLS-1$
                 + " as project where project.clientMetaDataVersion = :majorversion"); //$NON-NLS-1$
         query.setParameter(
             "majorversion", IVersion.JB_CLIENT_METADATA_VERSION); //$NON-NLS-1$
         return query.getResultList();
     }
 
     /**
      * @param guid
      *            GUID of the project to load
      * @param majorVersion
      *            Major version number of the project to load
      * @param minorVersion
      *            Minor version number of the project to load
      * @return the Project with the given attributes, or <code>null</code> if  
      *         no such Project could be found. The returned Project is not 
      *         associated with an Entity Manager.
      * @throws JBException
      *             ...
      */
     public static synchronized IProjectPO loadProjectByGuidAndVersion(
         String guid, int majorVersion, int minorVersion) throws JBException {
 
         EntityManager session = null;
         try {
             session = Persistor.instance().openSession();
             Query query = session.createQuery("select project from ProjectPO as project" //$NON-NLS-1$
                     + " inner join fetch project.properties where project.guid = :guid" //$NON-NLS-1$
                     + " and project.properties.majorNumber = :majorNumber and project.properties.minorNumber = :minorNumber"); //$NON-NLS-1$           
             query.setParameter("guid", guid); //$NON-NLS-1$
             query.setParameter("majorNumber", majorVersion); //$NON-NLS-1$
             query.setParameter("minorNumber", minorVersion); //$NON-NLS-1$
             
             try {
                 IProjectPO project = (IProjectPO)query.getSingleResult();
                 UsedToolkitBP.getInstance().readUsedToolkitsFromDB(project);
                 return project;
             } catch (NoResultException nre) {
                 // No result found. Return null as per the javadoc.
                 return null;
             }
         } catch (PersistenceException e) {
             OperationCanceledException oce = checkForCancel(e);
             if (oce != null) {
                 throw oce;
             }
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
     }
 
     /**
      * @param guid
      *            GUID of the project to load
      * @param majorVersion
      *            Major version number of the project to load
      * @param minorVersion
      *            Minor version number of the project to load
      * @return the Project with the given attributes, or <code>null</code> if  
      *         no such Project could be found. The returned Project is not 
      *         associated with an Entity Manager.
      * @throws JBException
      *             ...
      */
     public static synchronized Long findProjectIDByGuidAndVersion(
         String guid, int majorVersion, int minorVersion) throws JBException {
 
         EntityManager session = null;
         try {
             session = Persistor.instance().openSession();
             Query query = session.createQuery("select project.id from ProjectPO as project" //$NON-NLS-1$
                     + " inner join fetch project.properties where project.guid = :guid" //$NON-NLS-1$
                     + " and project.properties.majorNumber = :majorNumber and project.properties.minorNumber = :minorNumber"); //$NON-NLS-1$           
             query.setParameter("guid", guid); //$NON-NLS-1$
             query.setParameter("majorNumber", majorVersion); //$NON-NLS-1$
             query.setParameter("minorNumber", minorVersion); //$NON-NLS-1$
             
             try {
                 Long projectID = (Long)query.getSingleResult();
                 return projectID;
             } catch (NoResultException nre) {
                 // No result found. Return null as per the javadoc.
                 return null;
             }
         } catch (PersistenceException e) {
             OperationCanceledException oce = checkForCancel(e);
             if (oce != null) {
                 throw oce;
             }
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
     }
     /**
      * @param name
      *            Name of the project to load
      * @param majorVersion
      *            Major version number of the project to load
      * @param minorVersion
      *            Minor version number of the project to load
      * @return the Project with the given attributes, or <code>null</code> if  
      *         no such Project could be found. The returned Project is not 
      *         associated with an Entity Manager.
      * @throws JBException
      *             ...
      */
     public static synchronized IProjectPO loadProjectByNameAndVersion(
         String name, int majorVersion, int minorVersion) throws JBException {
 
         EntityManager session = null;
         String guid = StringConstants.EMPTY;
         try {
             session = Persistor.instance().openSession();
             Query query = session.createQuery("select name.hbmGuid " //$NON-NLS-1$
                 + "from ProjectNamePO as name" //$NON-NLS-1$
                 + " where name.hbmName = :name"); //$NON-NLS-1$           
             query.setParameter("name", name); //$NON-NLS-1$
             try {
                 guid = (String)query.getSingleResult();
             } catch (NoResultException nre) {
                 // No result found. Return null as per the javadoc.
                 return null;
             }
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
         return loadProjectByGuidAndVersion(guid, majorVersion, minorVersion);
     }
 
     /**
      * @return the project with the given name and the highest version number,
      *         or <code>null</code> if no project with the given name is found.
      *         The project instance is detached from the session.
      * @param name
      *            Name of the project to load
      * @throws JBException
      *             ...
      */
     public static synchronized IProjectPO loadLatestVersionOfProjectByName(
         String name) throws JBException {
 
         EntityManager session = null;
         String guid = StringConstants.EMPTY;
         int majorVersion = 0;
         int minorVersion = 0;
         try {
             session = Persistor.instance().openSession();
             Query query = session.createQuery("select project.hbmGuid " //$NON-NLS-1$
                 + "from ProjectNamePO as project" //$NON-NLS-1$
                 + " where project.hbmName = :name"); //$NON-NLS-1$           
             query.setParameter("name", name); //$NON-NLS-1$
             
             try {
                 guid = (String)query.getSingleResult();
             } catch (NoResultException nre) {
                 return null;
             }
 
             String versionNumber = findHighestVersionNumber(guid);
             int index = versionNumber.indexOf('.');
             majorVersion = Integer.parseInt(versionNumber.substring(0, index));
             minorVersion = Integer.parseInt(versionNumber.substring(index + 1));
         } catch (NumberFormatException nfe) {
             log.error(Messages.InvalidProjectVersionNumber, nfe);
             throw new JBException(nfe.getMessage(),
                 MessageIDs.E_INVALID_PROJECT_VERSION);
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
         return loadProjectByGuidAndVersion(guid, majorVersion, minorVersion);
     }
 
     /**
      * Loads the project in a new session, then closes the session. The returned
      * IProjectPO is therefore detached from its session.
      * @param reused
      *            The reused project information for this project.
      * @return the ProjectPO or null if no project in db 
      * @throws JBException in case of general db access errors (db disconnect, shutdown, etc)
      */
     public static synchronized IProjectPO loadProject(IReusedProjectPO reused)
         throws JBException {
 
         EntityManager session = Persistor.instance().openSession();
         try {
             return loadProjectInSession(reused, session);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
     }
     /**
      * Loads the project from the master session. The returned
      * IProjectPO MUST only be used in the master session or read only.
      * @param reused
      *            The reused project information for this project.
      * @return the ProjectPO or null if no project in db 
      * @throws JBException in case of general db access errors (db disconnect, shutdown, etc)
      */
     public static synchronized IProjectPO loadProjectFromMaster(
             IReusedProjectPO reused) throws JBException {
 
         EntityManager session = GeneralStorage.getInstance().getMasterSession();
 
         return loadProjectInSession(reused, session);
     }
 
     /**
      * Loads the project in a session. This is shared code for detached in 
      * master session loading.
      * @param reused
      *            The reused project information for this project.
      * @param session 
      *            Session context for db ops
      * @return the ProjectPO or null if no project in db 
      * @throws JBFatalAbortException
      * @throws OperationCanceledException
      * @throws JBException in case of general db access errors (db disconnect, shutdown, etc)
      */
     private static IProjectPO loadProjectInSession(IReusedProjectPO reused,
             EntityManager session) throws JBFatalAbortException,
             OperationCanceledException, JBException {
         try {
             Query query = session.createQuery("select project from ProjectPO as project" //$NON-NLS-1$
                     + " inner join fetch project.properties where project.guid = :guid" //$NON-NLS-1$
                     + " and project.properties.majorNumber = :majorNumber and project.properties.minorNumber = :minorNumber"); //$NON-NLS-1$           
             query.setParameter("guid", reused.getProjectGuid()); //$NON-NLS-1$
             query.setParameter("majorNumber", reused.getMajorNumber()); //$NON-NLS-1$
             query.setParameter("minorNumber", reused.getMinorNumber()); //$NON-NLS-1$
             
             try {
                 return (IProjectPO)query.getSingleResult();
             } catch (NoResultException nre) {
                 return null;
             }
         } catch (PersistenceException e) {
             OperationCanceledUtil.checkForOperationCanceled(e);
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } 
     }
 
     /**
      * Loads the project represented by the ReusedProjectPO into the master 
      * session.
      * 
      * @param reused The reused project information for the project.
      * @return the loaded project.
      * @throws JBException
      */
     public static synchronized IProjectPO loadReusedProjectInMasterSession(
         IReusedProjectPO reused) throws JBException {
 
         EntityManager masterSession = 
             GeneralStorage.getInstance().getMasterSession();
         
         try {
             Query query = masterSession.createQuery("select project from ProjectPO project" //$NON-NLS-1$
                         + " inner join fetch project.properties where project.guid = :guid" //$NON-NLS-1$
                         + " and project.properties.majorNumber = :majorNumber and project.properties.minorNumber = :minorNumber"); //$NON-NLS-1$           
             query.setParameter("guid", reused.getProjectGuid()); //$NON-NLS-1$
             query.setParameter("majorNumber", reused.getMajorNumber()); //$NON-NLS-1$
             query.setParameter("minorNumber", reused.getMinorNumber()); //$NON-NLS-1$
 
             IProjectPO project = null;
             try {
                 project = (IProjectPO)query.getSingleResult();
             } catch (NoResultException nre) {
                 // No result found. project remains null.
             }
 
             ParamNameBP.getInstance().initParamNamesOfReusedProject(reused);
             UsedToolkitBP.getInstance().readUsedToolkitsFromDB(project);
             return project;
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         }
     }
 
     /**
      * Loads the project in a new session and then closes the session.
      * 
      * @return the project with the given identifying information, or 
      * <code>null</code> if the project could not be found.
      * @param reusedProjectInfo 
      *      Information for finding the reused project
      * @throws JBException
      *             ...
      */
     public static synchronized IProjectPO loadReusedProject(
         IReusedProjectPO reusedProjectInfo) throws JBException {
 
         EntityManager session = null;
         try {
             session = Persistor.instance().openSession();
             return loadReusedProject(reusedProjectInfo, session);
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
     }
 
     /**
      * Loads the project in the given session.
      * 
      * @return the project with the given identifying information, or 
      * <code>null</code> if the project could not be found.
      * @param reusedProjectInfo 
      *      Information for finding the reused project
      * @param session
      *      The session into which the Project will be loaded
      * @throws JBException
      *             ...
      */
     public static synchronized IProjectPO loadReusedProject(
         IReusedProjectPO reusedProjectInfo, EntityManager session) 
         throws JBException {
 
         try {
             Query query = session.createQuery("select project from ProjectPO project" //$NON-NLS-1$
                     + " inner join fetch project.properties where project.guid = :guid and project.properties.majorNumber = :major " //$NON-NLS-1$
                     + "and project.properties.minorNumber = :minor"); //$NON-NLS-1$
             query.setParameter("guid", reusedProjectInfo.getProjectGuid()); //$NON-NLS-1$
             query.setParameter("major", reusedProjectInfo.getMajorNumber()); //$NON-NLS-1$
             query.setParameter("minor", reusedProjectInfo.getMinorNumber()); //$NON-NLS-1$
             
             try {
                 return (IProjectPO)query.getSingleResult();
             } catch (NoResultException nre) {
                 return null;
             }
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         }
     }
 
     /**
      * Finds the Project ID of the Project with the given GUID, Major Version 
      * and Minor Version.
      * @param projGuid the GUID
      * @param projMajVers the Major Version
      * @param projMinVers the Minor Version
      * @return an ID or null if not found
      * @throws JBException ...
      */
     public static final synchronized Long findProjectId(String projGuid,
             Integer projMajVers, Integer projMinVers) throws JBException {
 
         EntityManager session = null;
         try {
             session = Persistor.instance().openSession();
 
             Query query = session.createQuery("select project.id from ProjectPO project" //$NON-NLS-1$
                     + " inner join fetch project.properties where project.guid = :guid and project.properties.majorNumber = :major " //$NON-NLS-1$
                     + "and project.properties.minorNumber = :minor"); //$NON-NLS-1$
             query.setParameter("guid", projGuid); //$NON-NLS-1$
             query.setParameter("major", projMajVers); //$NON-NLS-1$
             query.setParameter("minor", projMinVers); //$NON-NLS-1$
 
             try {
                 return (Long)query.getSingleResult();
             } catch (NoResultException nre) {
                 return null;
             }
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
     }
     
     /**
      * Gets a List of IReusedProjectPO of the Project with the given GUID,
      * MajorVersion and MinorVersion.
      * @param projGuid the GUID of the Project which IReusedProjectPOs are wanted.
      * @param projMajVers the Major Version of the Project which IReusedProjectPOs are wanted.
      * @param projMinVers the Minor Version of the Project which IReusedProjectPOs are wanted.
      * @return a List of IReusedProjectPO or an empty List of nothing found. 
      * @throws JBException ...
      */
     public static final synchronized List<IReusedProjectPO> 
     loadReusedProjectsRO(
         String projGuid, Integer projMajVers, Integer projMinVers) 
         throws JBException {
         
         return loadReusedProjectsRO(
                 findProjectId(projGuid, projMajVers, projMinVers));
     }
     
     /**
      * Gets a List of IReusedProjectPO of the Project with the given ID.
      * @param projectId the ID of the Project which IReusedProjectPOs are wanted.
      * @return a List of IReusedProjectPO or an empty List of nothing found. 
      * @throws JBException ...
      */
     public static final List<IReusedProjectPO> loadReusedProjectsRO(
             Long projectId) throws JBException {
         final List<IReusedProjectPO> cachedList = rpCache.get(projectId);
         if (cachedList != null && !cachedList.isEmpty()) {
             return cachedList;
         }
         EntityManager session = GeneralStorage.getInstance().getMasterSession();
         final List<IReusedProjectPO> list = new ArrayList<IReusedProjectPO>();
         try {
             if (projectId != null) {
                 final Query query = 
                     session.createQuery(
                         "select reusedProj from ReusedProjectPO reusedProj" //$NON-NLS-1$
                         + " where reusedProj.hbmParentProjectId = :parentProjId"); //$NON-NLS-1$
                 query.setParameter("parentProjId", projectId); //$NON-NLS-1$
                 list.addAll(query.getResultList());
             }
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                     MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } 
         rpCache.put(projectId, list);
         return list;
         
     }
     
     /**
      * Load an instance of ProjectPO into the readonly session. The read
      * instance is db identical to the key supplied.
      * used for open project
      * 
      * @param project
      *            Look for this instance in the db.
      * @throws PMReadException
      *             if loading from db failed
      * 
      */
     public static void loadProjectInROSession(IProjectPO project)
         throws PMReadException {
 
         GeneralStorage.getInstance().reset();
         EntityManager s = GeneralStorage.getInstance().getMasterSession();
         s.clear(); // get rid of all session (cached) data
         try {
             preloadData(s, project);
             
             IProjectPO p = s.find(NodeMaker.getProjectPOClass(),
                     project.getId());
             GeneralStorage.getInstance().setProject(p);
             ParamNameBP.getInstance().initMap();
             ComponentNamesBP.getInstance().init();
         } catch (PersistenceException e) {
             GeneralStorage.getInstance().setProject(null);
             OperationCanceledException cancel = checkForCancel(e);
             if (cancel != null) {
                 throw cancel;
             }            
             String msg = Messages.CantReadProjectFromDatabase
                 + StringConstants.DOT;
             log.error(Messages.UnexpectedPersistenceErrorIgnored 
                     + StringConstants.DOT, e);
             throw new PMReadException(msg + e.getMessage(),
                 MessageIDs.E_CANT_READ_PROJECT);
         } catch (PMException e) {
             String msg = Messages.CouldNotReadParamNamesFromDB 
                 + StringConstants.DOT;
             log.error(msg, e); 
             throw new PMReadException(msg + e.getMessage(),
                 MessageIDs.E_CANT_READ_PROJECT);
         } catch (JBException e) {
             GeneralStorage.getInstance().setProject(null);
             String msg = Messages.CantReadProjectFromDatabase 
                 + StringConstants.DOT;
             log.error(Messages.UnexpectedPersistenceErrorIgnored, e);
             throw new PMReadException(msg + e.getMessage(),
                 MessageIDs.E_CANT_READ_PROJECT);            
         }
     }
 
     /**
      * Check if the cause of the PersistenceException was a 
      * OperationCanceledException
      * @param e origonal exception
      * @return instance of OperationCanceledException if e was cause by it or
      * null.
      */
     private static OperationCanceledException checkForCancel(
             PersistenceException e) {
         Throwable cause = e.getCause();
         while (cause != null) {
             if (cause instanceof OperationCanceledException) {
                 return (OperationCanceledException)cause;
             }
             cause = cause.getCause();
         }
         return null;
     }
 
     /**
      * iterate over a the reused list and check for reused project inside
      * @param reused A Set of accumulated IDs of reused project
      * @param check the current set of reused project under inspection
      * @throws JBException in case of DB problem
      */
     private static void findReusedProjects(Set<Long> reused,
             Set<IReusedProjectPO> check) throws JBException {
         for (IReusedProjectPO ru : check) {
             IProjectPO ruP = loadProjectFromMaster(ru);
             if (ruP != null) { // check for dangling reference
                 if (reused.add(ruP.getId())) {
                     findReusedProjects(reused, 
                             ruP.getProjectProperties().getUsedProjects());
                 }
             }
         }
     }
 
     /**
      * @see ProjectPM#getReusedProjectsForProject(EntityManager, long)
      * @param projectID
      *            Object id for the project to be used
      * @return A List of IReusedProjectPOs for the submitted projectID. The Set
      *         may be empty. Since the session is closed after this call the
      *         resulting entities are detached.
      */
     public static List<IReusedProjectPO> getReusedProjectsForProject(
             long projectID) throws PMException {
         EntityManager session = null;
         try {
             session = Persistor.instance().openSession();
             return getReusedProjectsForProject(session, projectID);
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new PMException(e.getMessage(),
                     MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
 
     }
     /**
      * @see ProjectPM#getReusedProjectsForProject(EntityManager, long)
      * @param projectID
      *            Object id for the project to be used
      * @return A List of IReusedProjectPOs for the submitted projectID. The Set
      *         may be empty. 
      */
     public static List<IReusedProjectPO> getReusedProjectsForProjectRO(
             long projectID) throws PMException {
         EntityManager session = GeneralStorage.getInstance().getMasterSession();
         try {
             return getReusedProjectsForProject(session, projectID);
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new PMException(e.getMessage(),
                     MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } 
     }
 
     /**
      * Loads a Set<IReusedProject> in a session.
      * 
      * @param s Session to be used for DB access
      * @param projectID Object id for the project to be used
      * @return A Set of IReusedProjectPOs for the submitted projectID. The Set may be empty.
      */
     public static List<IReusedProjectPO> getReusedProjectsForProject(
             EntityManager s, long projectID) {
         Query q = 
                 s.createQuery("select project from ReusedProjectPO project where project.hbmParentProjectId = :projectID"); //$NON-NLS-1$
         q.setParameter("projectID", projectID); //$NON-NLS-1$
                 
         @SuppressWarnings("unchecked")
         List<IReusedProjectPO> result = q.getResultList();
         return result;
     }
 
     /**
      * @param s Session to use
      * @param key If of Project to preload
      */
     @SuppressWarnings({ "nls", "unchecked" })
     private static void preloadData(EntityManager s, IProjectPO key)
         throws JBException {
 
         // Determine Sub Projects
         Set<Long> projectIds = new HashSet<Long>(17);
         projectIds.add(key.getId());
         // adds all project ids of reused projects to set
         findReusedProjects(projectIds,
                 key.getProjectProperties().getUsedProjects());
 
         preloadDataForClass(s, projectIds, "CompNamesPairPO");
         preloadDataForClass(s, projectIds, "CompIdentifierPO");
         preloadDataForClass(s, projectIds, "AUTConfigPO");
         preloadDataForClass(s, projectIds, "AUTMainPO");
         preloadDataForClass(s, projectIds, "ReusedProjectPO");
         preloadDataForClass(s, projectIds, "UsedToolkitPO");
         preloadDataForClass(s, projectIds, "AUTContPO");
         preloadDataForClass(s, projectIds, "ParamDescriptionPO");
 
         preloadDataForClass(s, projectIds, "TDManagerPO");
         preloadDataForClass(s, projectIds, "TestDataCubePO");
 
         preloadDataForClass(s, projectIds, "CapPO");
         List<ISpecTestCasePO> testCases =
                 preloadDataForClass(s, projectIds, "SpecTestCasePO");
         preloadDataForClass(
                 s, projectIds, "EventExecTestCasePO");
         preloadDataForClass(s, projectIds, "TestSuitePO");
         List<IExecTestCasePO> testCaseRefs =
                 preloadDataForClass(s, projectIds, "ExecTestCasePO");
         preloadDataForClass(s, projectIds, "CategoryPO");
 
         // for performance reasons, we prefill the cachedSpecTestCase
         // in ExecTestCasePOs
         Map<String, ISpecTestCasePO> sTc = 
                 new HashMap<String, ISpecTestCasePO>();
         for (ISpecTestCasePO testCase : testCases) {
             sTc.put(testCase.getGuid(), testCase);
         }
         for (IExecTestCasePO testCaseRef : testCaseRefs) {
             ISpecTestCasePO spec = sTc.get(testCaseRef.getSpecTestCaseGuid());
             if (spec != null) {
                 testCaseRef.setCachedSpecTestCase(spec);
             }
         }
     }
 
     /**
      * The Class for the given simple name must have the JPA attribute
      * "hbmParentProjectId", as this attribute will be used to identify
      * which elements should be preloaded.
      * 
      * @param s Session to use
      * @param projectIds Ids of projects
      * @param simpleClassName class name for the prefetch
      * @return List loaded data
      */
     private static List preloadDataForClass(EntityManager s, Set projectIds,
             String simpleClassName) {
         StringBuilder qString = new StringBuilder(100);
         qString.append("select e from "); //$NON-NLS-1$
         qString.append(simpleClassName);
         qString.append(" as e where e.hbmParentProjectId in :ids"); //$NON-NLS-1$
         Query q = s.createQuery(qString.toString());
         q.setParameter("ids", projectIds); //$NON-NLS-1$
         return q.getResultList();
     }
 
     /**
      * 
      * @param proj
      *            ProjectPO to be attached and saved.
      * @param newProjectName
      *            name part of the ProjectNamePO. If there is no new name, this
      *            parameter must be null (same project, different version)
      * @param mapperList mapper to resolve Parameter Names
      * @param compNameBindingList mapper to resolve Component Names
      * @param monitor The progress monitor for this potentially long-running 
      *                operation.
      * @throws PMException
      *             in case of any db error
      * @throws ProjectDeletedException if project is already deleted
      * @throws InterruptedException if the operation was canceled.
      */
     public static void attachProjectToROSession(IProjectPO proj, 
         String newProjectName, List<INameMapper> mapperList, 
         List<IWritableComponentNameMapper> compNameBindingList, 
         IProgressMonitor monitor) 
         throws PMException, ProjectDeletedException, InterruptedException {
         
         monitor.beginTask(NLS.bind(Messages.ProjectWizardCreatingProject,
                 newProjectName),
                 getTotalWorkForSave(proj));
         // Register Persistence progress listeners
         setHbmProgressMonitor(monitor);
         GeneralStorage.getInstance().reset();
         EntityManager s = GeneralStorage.getInstance().getMasterSession();
         EntityTransaction tx = null;
         try {
             tx = Persistor.instance().getTransaction(s);
             
             s.persist(proj);
             proj.setParentProjectId(proj.getId());
             if (newProjectName != null) {
                 ProjectNameBP.getInstance().setName(s, proj.getGuid(),
                         newProjectName);
             }
             ProjectNameBP.getInstance().storeTransientNames(s);
             for (INameMapper mapper : mapperList) {
                 mapper.persist(s, proj.getId());
             }
             for (IWritableComponentNameMapper compNameBinding 
                     : compNameBindingList) {
                 CompNamePM.flushCompNames(s, proj.getId(), compNameBinding);
             }
             if (!monitor.isCanceled()) {
                 Persistor.instance().commitTransaction(s, tx);
                 GeneralStorage.getInstance().setProject(proj);
                 for (INameMapper mapper : mapperList) {
                     mapper.updateStandardMapperAndCleanup(proj.getId());
                 }
                 for (IComponentNameMapper compNameBinding 
                         : compNameBindingList) {
                     compNameBinding.getCompNameCache()
                         .updateStandardMapperAndCleanup(proj.getId());
                 }
             } else {
                 Persistor.instance().rollbackTransaction(s, tx);
                 GeneralStorage.getInstance().reset();
                 for (INameMapper mapper : mapperList) {
                     mapper.clearAllNames();                    
                 }
                 for (IComponentNameMapper compNameBinding 
                         : compNameBindingList) {
                     compNameBinding.getCompNameCache().clear();
                 }
                 throw new InterruptedException();
             }
         } catch (PMException pme) {
             handleAlreadyLockedException(mapperList, s, tx);
         } catch (OperationCanceledException oce) {
             handleOperationCanceled(mapperList, s, tx);
         } catch (PersistenceException e) {
             handlePersistenceException(mapperList, s, tx, e);
         } catch (IncompatibleTypeException ite) {
             handleIncompatibleTypeException(mapperList, s, tx, ite);
         } finally {
             // Remove Persistence progress listeners
             setHbmProgressMonitor(null);
         }
         initBPs(proj); 
     }
 
     /**
      * Handles an "object locked" situation
      * 
      * @param mapperList The Parameter Name mapping list.
      * @param s The session.
      * @param tx The transaction.
      * @throws PMException if the transaction cannot be rolled back.
      * @throws PMException to indicate that the operation couldn't be
      * completed because another lock existed.
      */
 
     private static void handleAlreadyLockedException(
             List<INameMapper> mapperList, EntityManager s, 
             EntityTransaction tx) throws PMException {
         if (tx != null) {
             Persistor.instance().rollbackTransaction(s, tx);
         }
 
         GeneralStorage.getInstance().reset();
         for (INameMapper mapper : mapperList) {
             mapper.clearAllNames();
         }
         String msg = Messages.CantAttachProject + StringConstants.DOT;
         throw new PMSaveException(msg, MessageIDs.E_OBJECT_IN_USE);
 
     }
 
     /**
      * Handles an IncompatibleTypeException.
      * 
      * @param mapperList Name mappers.
      * @param s The session in which the error occurred.
      * @param tx The transaction in which the error occurred.
      * @param ite The error that occurred.
      * @throws PMException If rollback fails.
      * @throws PMSaveException If rollback does not fail.
      */
     private static void handleIncompatibleTypeException(
             List<INameMapper> mapperList, EntityManager s, 
             EntityTransaction tx,
             IncompatibleTypeException ite) throws PMException, PMSaveException {
         if (tx != null) {
             Persistor.instance().rollbackTransaction(s, tx);
         }
 
         GeneralStorage.getInstance().reset();
         for (INameMapper mapper : mapperList) {
             mapper.clearAllNames();                    
         }
 
         String msg = "Can't attach project. "; //$NON-NLS-1$
         throw new PMSaveException(msg + ite.getMessage(),
             MessageIDs.E_ATTACH_PROJECT);
     }
 
     /**
      * Handles a <code>PersistenceException</code>.
      * 
      * @param mapperList The Parameter Name mapping list.
      * @param s The session.
      * @param tx The transaction.
      * @param e The exception.
      * @throws PMException if the rollback of the transaction fails.
      * @throws InterruptedException if the cause of the given exception was 
      *                              that the operation was canceled.
      * @throws PMSaveException wrapper for the Persistence exception.
      */
     private static void handlePersistenceException(List<INameMapper> mapperList,
             EntityManager s, EntityTransaction tx, PersistenceException e)
         throws PMException, InterruptedException, PMSaveException {
         
         if (tx != null) {
             Persistor.instance().rollbackTransaction(s, tx);               
         }
         if (e.getCause() instanceof InterruptedException) {
 
             GeneralStorage.getInstance().reset();
             for (INameMapper mapper : mapperList) {
                 mapper.clearAllNames();                    
             }
             // Operation was canceled.
             throw new InterruptedException();
         }
         String msg = Messages.CantAttachProject + StringConstants.DOT;
         throw new PMSaveException(msg + e.getMessage(),
             MessageIDs.E_ATTACH_PROJECT);
     }
 
     /**
      * Handles the cancellation of an operation.
      * 
      * @param mapperList The Parameter Name mapping list.
      * @param s The session.
      * @param tx The transaction.
      * @throws PMException if the transaction cannot be rolled back.
      * @throws InterruptedException to indicate that the operation was 
      *                              successfully canceled.
      */
     private static void handleOperationCanceled(List<INameMapper> mapperList,
             EntityManager s, EntityTransaction tx) 
         throws PMException, InterruptedException {
         
         if (tx != null) {
             Persistor.instance().rollbackTransaction(s, tx);               
         }
         GeneralStorage.getInstance().reset();
         for (INameMapper mapper : mapperList) {
             mapper.clearAllNames();                    
         }
         // Operation was canceled.
         throw new InterruptedException();
     }
 
     /**
      * @param proj
      *            the Project
      * @throws PMException
      * @throws ProjectDeletedException
      * @throws PMSaveException
      */
     private static void initBPs(IProjectPO proj) throws PMException,
             ProjectDeletedException, PMSaveException {
         try {
             ComponentNamesBP.getInstance().init();
             ParamNameBP.getInstance().initMap();
         } catch (PMException e) {
             throw new PMException(
                 Messages.ReadingOfProjectNameOrParamNamesFailed
                 + StringConstants.COLON + StringConstants.SPACE
                 + e.toString(), MessageIDs.E_ATTACH_PROJECT);
         }
         try {
             UsedToolkitBP.getInstance().refreshToolkitInfo(proj);
         } catch (PMException e) {
             throw new PMSaveException(
                 Messages.PMExceptionWhileWritingUsedToolkitsInDB
                 + StringConstants.COLON + StringConstants.SPACE
                 + e.toString(), MessageIDs.E_ATTACH_PROJECT);
         }
     }
 
     /**
      * Sets the progress monitor for Persistence progress listeners/interceptors.
      * 
      * @param monitor The progress monitor to use, or <code>null</code> to clear
      *                the monitor.
      */
     private static void setHbmProgressMonitor(IProgressMonitor monitor) {
         ProgressMonitorTracker.getInstance().setProgressMonitor(monitor);
     }
 
     /**
      * Persists the given project to the DB. This is performed in a new session.
      * When this method returns, the project will not be attached to any session.
      * @param proj ProjectPO to be saved.
      * @param newProjectName
      *            name part of the ProjectNamePO. If there is no new name, this
      *            parameter must be null (same project, different version)
      * @param mapperList a List of INameMapper to persist names (Parameter).
      * @param compNameBindingList a List of Component Name mappers to persist 
      *                            names (Component).
      * @throws PMException in case of any db error
      * @throws ProjectDeletedException if project is already deleted
      * @throws InterruptedException if the operation is canceled
      */
     public static void saveProject(IProjectPO proj, String newProjectName, 
             List<INameMapper> mapperList, 
             List<IWritableComponentNameMapper> compNameBindingList) 
         throws PMException, ProjectDeletedException, 
             InterruptedException {
         
         final EntityManager saveSession = Persistor.instance().openSession();
         EntityTransaction tx = null;
         try {
             tx = Persistor.instance().getTransaction(saveSession);
 
             saveSession.persist(proj);
             proj.setParentProjectId(proj.getId());
             
             saveSession.flush();
             if (newProjectName != null) {
                 ProjectNameBP.getInstance().setName(
                     saveSession, proj.getGuid(), newProjectName);
             }
             ProjectNameBP.getInstance().storeTransientNames(saveSession);
             for (INameMapper mapper : mapperList) {
                 mapper.persist(saveSession, proj.getId());
             }
             for (IWritableComponentNameMapper compNameBinding 
                     : compNameBindingList) {
                 CompNamePM.flushCompNames(saveSession, 
                         proj.getId(), compNameBinding);
             }
             Persistor.instance().commitTransaction(saveSession, tx);
             for (INameMapper mapper : mapperList) {
                 mapper.updateStandardMapperAndCleanup(proj.getId());
             }
             for (IComponentNameMapper compNameCache : compNameBindingList) {
                 compNameCache.getCompNameCache()
                     .updateStandardMapperAndCleanup(proj.getId());
             }
         } catch (PersistenceException e) {
             if (tx != null) {
                 Persistor.instance().rollbackTransaction(saveSession, tx);
             }
             if (e.getCause() instanceof InterruptedException) {
                 // Operation was canceled.
                 throw new InterruptedException();
             }
             String msg = Messages.CantSaveProject + StringConstants.DOT;
             throw new PMSaveException(msg + e.getMessage(),
                     MessageIDs.E_ATTACH_PROJECT);
         } catch (IncompatibleTypeException ite) {
             if (tx != null) {
                 Persistor.instance().rollbackTransaction(saveSession, tx);
             }
             String msg = Messages.CantSaveProject + StringConstants.DOT;
             throw new PMSaveException(msg + ite.getMessage(),
                     MessageIDs.E_ATTACH_PROJECT);
         } finally {
             Persistor.instance().dropSession(saveSession);
         }
     } 
 
     /**
      * Check if there is a ProjectPO whith the supplied name in the DB.
      * 
      * @param name
      *            Name to check
      * @return wether the name denotes a ProjectPO in the DB
      */
     public static synchronized boolean doesProjectNameExist(String name) {
         EntityManager session = null;
         Long hits = null;
         try {
             session = Persistor.instance().openSession();
             Query q = session.createQuery("select name from ProjectNamePO as name " //$NON-NLS-1$
                             + "where name.hbmName = :name"); //$NON-NLS-1$
             q.setParameter("name", name); //$NON-NLS-1$
             
             IProjectNamePO namePO = (IProjectNamePO)q.getSingleResult();
             if (namePO != null) {
                 q = session.createQuery("select count(project.id) from ProjectPO project " //$NON-NLS-1$ 
                                 + "where project.guid = :guid"); //$NON-NLS-1$
                 q.setParameter("guid", namePO.getGuid()); //$NON-NLS-1$
                 hits = (Long)q.getSingleResult();
             }
         } catch (NoResultException nre) {
             return false;
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
         return (hits != null && hits.intValue() > 0);
     }
 
     /**
      * Check if there is a ProjectPO with the supplied guid and version in the
      * DB.
      * 
      * @param guid
      *            GUID to check
      * @param majorNumber
      *            Major version number to check
      * @param minorNumber
      *            Minor version number to check
      * @return wether the ProjectPO currently exists in the DB
      */
     public static synchronized boolean doesProjectVersionExist(String guid,
         Integer majorNumber, Integer minorNumber) {
 
         EntityManager session = null;
         Long hits = null;
         try {
             session = Persistor.instance().openSession();
             Query q = session.createQuery("select count(project) from ProjectPO as project" //$NON-NLS-1$
                     + " inner join project.properties properties where project.guid = :guid" //$NON-NLS-1$
                     + " and properties.majorNumber = :majorNumber and properties.minorNumber = :minorNumber"); //$NON-NLS-1$           
 
             q.setParameter("guid", guid); //$NON-NLS-1$
             q.setParameter("majorNumber", majorNumber); //$NON-NLS-1$
             q.setParameter("minorNumber", minorNumber); //$NON-NLS-1$
             hits = (Long)q.getSingleResult();
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
         } finally {
             Persistor.instance().
                 dropSessionWithoutLockRelease(session);            
         }
         return (hits != null && hits.intValue() > 0);
     }
 
     /**
      * Check if there is a TestSuite whith the supplied name in the DB.
      * 
      * @param name
      *            Name to check
      * @param projectId
      *            Long
      * @return wether the name denotes a ProjectPO in the DB
      */
     public static synchronized boolean doesTestSuiteExists(
         Long projectId, String name) {
         
         EntityManager session = null;
         List hits = null;
         try {
             session = Persistor.instance().openSession();
             Query q = session.createQuery("select node from TestSuitePO as node where node.hbmName = ?1 and node.hbmParentProjectId = ?2"); //$NON-NLS-1$
             q.setParameter(1, name);
             q.setParameter(2, projectId);
             hits = q.getResultList();
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
         return ((hits != null) && (hits.size() > 0));
     }
     
     /**
      * Check if there is a TestJob whith the supplied name in the DB.
      * 
      * @param name
      *            Name to check
      * @param projectId
      *            Long
      * @return wether the name denotes a ProjectPO in the DB
      */
     public static synchronized boolean doesTestJobExists(
         Long projectId, String name) {
         
         EntityManager session = null;
         List hits = null;
         try {
             session = Persistor.instance().openSession();
             Query q = session.createQuery("select node from TestJobPO as node where node.hbmName = ?1 and node.hbmParentProjectId = ?2"); //$NON-NLS-1$
             q.setParameter(1, name);
             q.setParameter(2, projectId);
             hits = q.getResultList();
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
         return ((hits != null) && (hits.size() > 0));
     }
 
     /**
      * delete a project
      * 
      * @param proj
      *            project to delete
      * @param isActProject
      *            flag to label the actual project
      * @throws PMAlreadyLockedException
      *             if project is already locked in db
      * @throws PMDirtyVersionException
      *             if project to delete is modified in the meantime
      * @throws JBException
      *             if a session cannot closed
      * @throws PMExtProjDeletedException
      *             if a project (but not the current) was deleted by another
      *             user
      * @throws ProjectDeletedException
      *             if the current project was deleted by another user
      * @throws InterruptedException
      *             if the operation was canceled
      */
     public static void deleteProject(IProjectPO proj, boolean isActProject)
         throws PMDirtyVersionException, PMAlreadyLockedException,
         PMExtProjDeletedException, ProjectDeletedException, JBException, 
         InterruptedException {
 
         Validate.notNull(proj, "Project to delete is null"); //$NON-NLS-1$
         EntityManager deleteSess = null;
         IProjectPO p = null;
         final Long projId = proj.getId();
         try {
             if (isActProject) {
                EntityManager s = GeneralStorage.getInstance()
                    .getMasterSession();
                 IProjectPO currProj = s.find(NodeMaker.getProjectPOClass(),
                    projId);
                 if (currProj == null) {
                     throw new ProjectDeletedException(
                         Messages.ProjectWasDeleted,
                         MessageIDs.E_CURRENT_PROJ_DEL);
                 }
             }
         } catch (PersistenceException e) {
             handleDBExceptionForMasterSession(proj, e);
         }
         try {
             deleteSess = Persistor.instance().openSession();
             EntityTransaction tx = 
                 Persistor.instance().getTransaction(deleteSess);
             p = deleteSess.find(NodeMaker.getProjectPOClass(), projId);
             if (p == null) {
                 if (isActProject) {
                     throw new ProjectDeletedException(
                         "Current Project was deleted", //$NON-NLS-1$
                         MessageIDs.E_CURRENT_PROJ_DEL);
                 }
                 throw new PMExtProjDeletedException(Messages.ProjectWasDeleted
                     + StringConstants.DOT,
                     MessageIDs.E_DELETED_OBJECT);
             }
             Persistor.instance().lockPO(deleteSess, p);
             deleteProjectIndependentDBObjects(deleteSess, p);
 
             // FIXME zeb Workaround for EclipseLink deleting the objects in the
             //           wrong order. Test Cases that reference Test Data Cubes
             //           were being deleted *after* the Test Data Cubes 
             //           themselves.
             List<ISpecPersistable> specObjList = 
                 new ArrayList<ISpecPersistable>(
                         p.getSpecObjCont().getSpecObjList());
             List<IExecPersistable> execObjList = 
                 new ArrayList<IExecPersistable>(
                         p.getExecObjCont().getExecObjList());
             for (ISpecPersistable po : specObjList) {
                 PersistenceUtil.removeChildNodes(po, deleteSess);
                 p.getSpecObjCont().removeSpecObject(po);
                 Persistor.instance().deletePO(deleteSess, po);
             }
             for (IExecPersistable po : execObjList) {
                 PersistenceUtil.removeChildNodes(po, deleteSess);
                 p.getExecObjCont().removeExecObject(po);
                 Persistor.instance().deletePO(deleteSess, po);
             }
             deleteSess.flush();
             // FIXME zeb end workaround
 
             
             Persistor.instance().deletePO(deleteSess, p);
             CompNamePM.deleteCompNames(deleteSess, projId);
             Persistor.instance().commitTransaction(deleteSess, tx);
             tx = null;
         } catch (PersistenceException e) {
             handleDBExceptionForAnySession(p, e, deleteSess);
         } finally {
             Persistor.instance().dropSession(deleteSess);
         }
         ProjectNameBP.getInstance().checkAndDeleteName(proj.getGuid());
     }
 
     /**
      * delete all database objects which not associated with project
      * 
      * @param s
      *            session to use for delete operation
      * @param p
      *            project, whose independent objects to be deleted
      * @throws PMException
      *             in case of failed delete operation
      * @throws ProjectDeletedException
      *             in case of already deleted exception
      * 
      */
     private static void deleteProjectIndependentDBObjects(EntityManager s,
             IProjectPO p) throws PMException, ProjectDeletedException {
         UsedToolkitBP.getInstance().deleteToolkitsFromDB(s, p.getId(), false);
         ParamNamePM.deleteParamNames(s, p.getId(), false);
     }
 
     /**
      * 
      * @param proj The project for which to find the required work
      *             amount.
      * @return the amount of work required to save the given project to the
      *         database.
      */
     private static int getTotalWorkForSave(IProjectPO proj) {
         
         // (project_node=1)
         int totalWork = 1;
         
         // (INodePO=1)
         for (IExecPersistable exec 
                 : proj.getExecObjCont().getExecObjList()) {
             
             totalWork += getWorkForNode(exec);
         }
         for (ISpecPersistable spec 
                 : proj.getSpecObjCont().getSpecObjList()) {
             
             totalWork += getWorkForNode(spec);
         }
         
         // 1 for each event type
         totalWork *= NUM_HBM_ADD_PROGRESS_EVENT_TYPES;
         return totalWork;
     }
 
     /**
      * Recursively determines the amount of work involved in saving the
      * given node to the database.
      * 
      * @param node The node for which to determine the amount of work.
      * @return the amount of work required to save the given node to the 
      *         database.
      */
     private static int getWorkForNode(INodePO node) {
         int work = 1;
         if (!(node instanceof IExecTestCasePO)) {
             Iterator<INodePO> childIter = node.getNodeListIterator();
             while (childIter.hasNext()) {
                 work += getWorkForNode(childIter.next());
             }
         }
         
         if (node instanceof ISpecTestCasePO) {
             work += ((ISpecTestCasePO)node).getAllEventEventExecTC().size();
         }
         
         return work;
     }
 
     /**
      * @param msg
      *            msg for log
      * @param s
      *            corresponding session
      * @param tx
      *            transaction to rollback
      * @throws PMException
      *             in case of failed rollback
      */
     @SuppressWarnings("unused")
     private static void rollbackTransaction(EntityManager s, String msg,
         EntityTransaction tx) throws PMException {
         try {
             Persistor.instance().rollbackTransaction(s, tx);
         } catch (PersistenceException e) {
             log.error(msg, e);
             GeneralStorage.getInstance().recoverSession();
         }
     }
 
     /**
      * @param guid
      *            The GUID to search.
      * @return a String representing the highest version number for this project
      *         GUID
      */
     public static synchronized String findHighestVersionNumber(String guid)
         throws JBException {
 
         EntityManager session = null;
         try {
             session = Persistor.instance().openSession();
             Query query = session.createQuery("select project from ProjectPO project" //$NON-NLS-1$
                     + " inner join fetch project.properties where project.guid = :guid " //$NON-NLS-1$
                     + "order by project.properties.majorNumber desc, project.properties.minorNumber desc"); //$NON-NLS-1$
             query.setParameter("guid", guid); //$NON-NLS-1$
             query.setMaxResults(1);
             final List projList = query.getResultList();
             if (projList.isEmpty()) {
                 return StringConstants.EMPTY;
             }
             IProjectPO project = (IProjectPO)projList.get(0);
             return project.getMajorProjectVersion() + StringConstants.DOT
                 + project.getMinorProjectVersion(); 
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
     }
 
     /**
      * Returns a list of all projects that can be used by a project with the 
      * given properties. This query is executed in a new session, so the 
      * objects in the returned list are detached from their session.
      * @param guid The GUID of the project that may use projects from the 
      *             returned list.
      * @param majorVersionNumber The major version number of the project 
      *                           wishing to reuse.
      * @param minorVersionNumber The minor version number of the project 
      *                           wishing to reuse.
      * @param toolkit The toolkit of the project that may use projects from the 
      *             returned list.
      * @param toolkitLevel The toolkit level of the project that may use 
      *                     projects from the returned list.
      * @return a list of all reusable projects that could be used by a project 
      *         with the given properties.
      */
     @SuppressWarnings("unchecked")
     public static synchronized List<IProjectPO> findReusableProjects(
         String guid, int majorVersionNumber, int minorVersionNumber, 
         String toolkit, String toolkitLevel) 
         throws JBException {
         EntityManager session = null;
         try {
             session = Persistor.instance().openSession();
             Query query = session.createQuery("select project from ProjectPO project" //$NON-NLS-1$
                     + " inner join fetch project.properties where project.properties.isReusable = :isReusable" //$NON-NLS-1$
                     + " and project.guid != :guid"); //$NON-NLS-1$
             query.setParameter("isReusable", true); //$NON-NLS-1$
             query.setParameter("guid", guid); //$NON-NLS-1$
             List<IProjectPO> projects = query.getResultList();
             Iterator<IProjectPO> iter = projects.iterator();
             while (iter.hasNext()) {
                 IProjectPO project = iter.next();
                 String reusedToolkit = project.getToolkit();
                 try {
                     String reusedToolkitLevel = 
                         ToolkitSupportBP.getToolkitLevel(reusedToolkit);
                     if (!(reusedToolkit.equals(toolkit) 
                         || ToolkitUtils.doesToolkitInclude(
                             toolkit, reusedToolkit)
                         || ToolkitUtils.isToolkitMoreConcrete(
                             toolkitLevel, reusedToolkitLevel))) {
                         iter.remove();
                     }
                 } catch (ToolkitPluginException tpe) {
                     StringBuilder msg = new StringBuilder();
                     msg.append(Messages.Project);
                     msg.append(StringConstants.SPACE);
                     msg.append(project.getName());
                     msg.append(StringConstants.SPACE);
                     msg.append(
                         Messages.CouldNotBeLoadedAnUnavailableToolkitPlugin);
                     msg.append(StringConstants.DOT);
                     // Plugin for toolkit could not be loaded.
                     log.error(msg.toString());
                     // Remove the project using the unavailable toolkit
                     // from the available projects list.
                     iter.remove();
                 }
             }
             // We have a list of reusable projects with compatible toolkits.
             // Now we need to remove from the list any projects that would lead 
             // to circular dependencies. 
             Set<IProjectPO> checkedProjects = new HashSet<IProjectPO>();
             Set<IProjectPO> illegalProjects = new HashSet<IProjectPO>();
 
             IProjectPO givenProject = loadProjectByGuidAndVersion(
                     guid, majorVersionNumber, minorVersionNumber);
             
             if (givenProject == null) {
                 log.debug(Messages.TriedFindProjectsForNonExistantProject);
                 return new ArrayList<IProjectPO>();
             }
             
             // Project can't reuse itself
             illegalProjects.add(givenProject);
             checkedProjects.add(givenProject);
 
             for (IProjectPO proj : projects) {
                 findIllegalProjects(proj, checkedProjects, 
                     illegalProjects, null);
             }
             
             projects.removeAll(illegalProjects);
             
             return projects;
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
     }
 
     /**
      * Recursively searches for projects that cannot be used by the given 
      * project.
      * 
      * The current rules are as follows:
      * 1. No circular dependencies. This means that a project 'A' can not use 
      *    itself nor any project that uses 'A' (this rule is, of course, 
      *    transitive).
      * 
      * @param projectToCheck The project for which reused/reusable projects are
      *                       being checked.
      * @param checkedProjects Projects that have already been examined by this
      *                        method.
      * @param illegalProjects Projects marked as not reusable by this method.
      * @param projectsToImport Projects that are currently being imported.
      *                         Reused projects will be searched for by first
      *                         looking through this collection, and, if the 
      *                         project is not found there, looking in the #
      *                         database.
      *                         May be <code>null</code>.
      */
     public static void findIllegalProjects(
             IProjectPO projectToCheck, 
             Set<IProjectPO> checkedProjects,
             Set<IProjectPO> illegalProjects,
             Set<IProjectPO> projectsToImport) {
 
         if (!checkedProjects.contains(projectToCheck)) {
             checkedProjects.add(projectToCheck);
 
             for (IReusedProjectPO reused : projectToCheck.getUsedProjects()) {
 
                 try {
                     String reusedGuid = reused.getProjectGuid();
                     Integer reusedMajorVersion = reused.getMajorNumber();
                     Integer reusedMinorVersion = reused.getMinorNumber();
                     IProjectPO reusedProject = null;
                     if (projectsToImport != null) {
                         for (IProjectPO importedProject : projectsToImport) {
                             // First, try to find the project among the imported 
                             // projects.
                             if (reusedGuid.equals(importedProject.getGuid()) 
                                 && reusedMajorVersion.equals(
                                     importedProject.getMajorProjectVersion()) 
                                 && reusedMinorVersion.equals(
                                     importedProject.getMinorProjectVersion())) {
                                 
                                 reusedProject = importedProject;
                                 break;
                             }
                         }
                     }
 
                     if (reusedProject == null) {
                         // Try to load the project from the db.
                         reusedProject = loadProjectByGuidAndVersion(
                             reused.getProjectGuid(), 
                             reused.getMajorNumber(), 
                             reused.getMinorNumber());
                     }
 
                     if (reusedProject != null) {
                         // recurse if we were able to find the project
                         findIllegalProjects(reusedProject, checkedProjects, 
                             illegalProjects, projectsToImport);
                     }
                 } catch (JBException e) {
                     // Error occurred while attempting to load the project
                     illegalProjects.add(projectToCheck);
                 }
 
                     // if the project uses an illegal project, then it is also illegal
                 for (IProjectPO project : illegalProjects) {
                     if (project.getGuid().equals(reused.getProjectGuid())
                             && project.getMajorProjectVersion().equals(
                                 reused.getMajorNumber())
                             && project.getMinorProjectVersion().equals(
                                 reused.getMinorNumber())) {
                         
                         illegalProjects.add(projectToCheck);
                         
                         // This break statement prevents a 
                         // ConcurrentModificationException from occuring
                         // because it stops iteration immediately when
                         // a project is added to the set.
                         break;
                     }
                 }
             }
         }
     }
 
     /**
      * @return The project for the given (database) ID
      * @param projectId
      *            (database) ID of the project to load
      * @param session
      *      The session to use for loading. The returned project will be 
      *      attached to this session. It is the responsibility of the
      *      caller to close the session.
      * @throws JBException
      *             if the session cannot be loaded.
      */
     public static synchronized IProjectPO loadProjectById(
         Long projectId, EntityManager session) throws JBException {
 
         if (projectId == null) {
             return null;
         }
 
         try {
             Query query = session.createQuery("select project from ProjectPO project" //$NON-NLS-1$
                     + " where project.id = :id"); //$NON-NLS-1$
             query.setParameter("id", projectId); //$NON-NLS-1$
             try {
                 return (IProjectPO)query.getSingleResult();
             } catch (NoResultException nre) {
                 return null;
             }
         } catch (PersistenceException e) {
             OperationCanceledException oce = checkForCancel(e);
             if (oce != null) {
                 throw oce;
             }
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         }
     }
 
     /**
      * Loads the project a new session and closes the session.
      * @return The  project for the given (database) ID
      * @param projectId 
      *      (database) ID of the project to load
      * @throws JBException
      *             if the session cannot be loaded or closed.
      */
     public static IProjectPO loadProjectById(Long projectId) 
         throws JBException {
 
         EntityManager session = null;
         try {
             session = Persistor.instance().openSession();
             return loadProjectById(projectId, session);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
     }
 
     /**
      * Returns a list of project IDs, each ID represents a project currently in 
      * the database that reuses the project with the given information.
      * 
      * @param guid GUID of the reused project.
      * @param majorVersion Major version number of the reused project.
      * @param minorVersion Minor version number of the reused project.
      * @return the IDs of all projects that reuse the project with the given
      *         information.
      */
     @SuppressWarnings("unchecked")
     public static synchronized List<Long> findIdsThatReuse(
         String guid, int majorVersion, int minorVersion) throws JBException {
 
         EntityManager session = null;
         List<Long> hits;
         try {
             session = Persistor.instance().openSession();
             Query query = session.createQuery("select reusedProject.hbmParentProjectId from ReusedProjectPO" //$NON-NLS-1$
                     + " as reusedProject where reusedProject.projectGuid = :projectGuid and reusedProject.majorNumber = :majorNumber" //$NON-NLS-1$
                     + " and reusedProject.minorNumber = :minorNumber"); //$NON-NLS-1$
             query.setParameter(
                 "projectGuid", guid); //$NON-NLS-1$
             query.setParameter(
                 "majorNumber", majorVersion); //$NON-NLS-1$
             query.setParameter(
                 "minorNumber", minorVersion); //$NON-NLS-1$
             hits = query.getResultList();
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
         return hits;
 
     }
     
     /**
      * Gets the GUID of the project with the given ID.
      * @param projId the ID of the project
      * @return the GUID of the project with the given ID or null if
      * no project with the given ID was found.
      * @throws JBException if the session cannot be loaded or closed.
      */
     public static final synchronized String getGuidOfProjectId(Long projId) 
         throws JBException {
         String cachedGuid = guidCache.get(projId);
         if (cachedGuid != null) {
             return cachedGuid;
         }
         EntityManager session = null;
         String projGuid = null;
         try {
             session = Persistor.instance().openSession();
             final Query query = 
                 session.createQuery("select project.guid from ProjectPO project where project.id = :projectID"); //$NON-NLS-1$
             query.setParameter("projectID", projId); //$NON-NLS-1$
             projGuid = (String)query.getSingleResult();
         } catch (NoResultException nre) {
             // No result found. Fall through to return null.
         } catch (PersistenceException e) {
             throw new JBException(e.getMessage(),
                 MessageIDs.E_PERSISTENCE_LOAD_FAILED);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
         guidCache.put(projId, projGuid);
         return projGuid;
     }
     
     /**
      * Chechs if a project with a given ID exists in the DB
      * @param projectId Object ID of project
      * @return true if the project exist, fals if not or in case of errors
      */
     public static boolean doesProjectExist(Long projectId) {
         EntityManager session = null;
         List hits = null;
         try {
             session = Persistor.instance().openSession();
             Query q = session.createQuery("select node from ProjectPO as node where node.id = ?1"); //$NON-NLS-1$
             q.setParameter(1, projectId);
             hits = q.getResultList();
         } catch (PersistenceException e) {
             log.error(Messages.PersistenceLoadFailed, e);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(session);
         }
         return ((hits != null) && (hits.size() > 0));
 
     }
 }
