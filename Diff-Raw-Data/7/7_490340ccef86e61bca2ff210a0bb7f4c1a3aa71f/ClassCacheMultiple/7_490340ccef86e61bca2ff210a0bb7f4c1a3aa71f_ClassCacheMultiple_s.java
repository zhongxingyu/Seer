 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.navigator.utils;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
 import Sirius.server.middleware.types.MetaClass;
import de.cismet.cids.utils.MetaClassUtils;
 
 import java.util.HashMap;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class ClassCacheMultiple {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static HashMap<String, HashMap> allClassCaches = new HashMap<String, HashMap>();
     private static HashMap<String, HashMap> allTableNameClassCaches = new HashMap<String, HashMap>();
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClassCacheMultiple.class);
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static HashMap getClassKeyHashtableOfClassesForOneDomain(final String domain) {
         HashMap ret = allClassCaches.get(domain);
         if (ret == null) {
             try {
                 addInstance(domain);
                 ret = allClassCaches.get(domain);
             } catch (Exception e) {
                 if (log.isDebugEnabled()) {
                     log.debug("Error in setInstance of ClassCacheMultiple", e); // NOI18N
                 }
             }
         }
         return ret;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static HashMap getTableNameHashtableOfClassesForOneDomain(final String domain) {
         HashMap ret = allTableNameClassCaches.get(domain);
         if (ret == null) {
             try {
                 addInstance(domain);
                 ret = allTableNameClassCaches.get(domain);
             } catch (Exception e) {
                 if (log.isDebugEnabled()) {
                     log.debug("Error in setInstance of ClassCacheMultiple", e); // NOI18N
                 }
             }
         }
         return ret;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain     DOCUMENT ME!
      * @param   tableName  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MetaClass getMetaClass(final String domain, final String tableName) {
         try {
             final HashMap ht = getTableNameHashtableOfClassesForOneDomain(domain);
             return (MetaClass)ht.get(tableName.toLowerCase());
         } catch (Exception e) {
             log.warn("Couldn't get Class for Table " + tableName + "@" + domain, e); // NOI18N
             return null;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain   DOCUMENT ME!
      * @param   classId  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MetaClass getMetaClass(final String domain, final int classId) {
         return (MetaClass)ClassCacheMultiple.getClassKeyHashtableOfClassesForOneDomain(domain).get(domain + classId);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  domain  DOCUMENT ME!
      */
     public static void setInstance(final String domain) {
         try {
             final MetaClass[] mcArr = SessionManager.getConnection()
                         .getClasses(SessionManager.getSession().getUser(), domain);
             allClassCaches.put(domain, MetaClassUtils.getClassHashtable(mcArr, domain));
             allTableNameClassCaches.put(domain, MetaClassUtils.getClassByTableNameHashtable(mcArr));
         } catch (ConnectionException connectionException) {
             log.error("Error in setInstance of ClassCacheMultiple", connectionException); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  domain  DOCUMENT ME!
      */
     public static void addInstance(final String domain) {
         try {
             final MetaClass[] mcArr = SessionManager.getConnection()
                         .getClasses(SessionManager.getSession().getUser(), domain);
             allClassCaches.put(domain, MetaClassUtils.getClassHashtable(mcArr, domain));
             allTableNameClassCaches.put(domain, MetaClassUtils.getClassByTableNameHashtable(mcArr));
         } catch (ConnectionException connectionException) {
             log.error("Error in setInstance of ClassCacheMultiple", connectionException); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   classes          DOCUMENT ME!
      * @param   localServerName  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
    
 }
