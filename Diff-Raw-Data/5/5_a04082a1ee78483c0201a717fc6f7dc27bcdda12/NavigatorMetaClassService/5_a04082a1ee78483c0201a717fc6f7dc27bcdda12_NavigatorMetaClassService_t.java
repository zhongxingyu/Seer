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
 
 import Sirius.server.middleware.types.MetaClass;
 
 import java.util.HashMap;
 
 import de.cismet.cids.utils.MetaClassCacheService;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
@org.openide.util.lookup.ServiceProvider(
    service = MetaClassCacheService.class,
    position = 100
)
 public class NavigatorMetaClassService implements MetaClassCacheService {
 
     //~ Instance fields --------------------------------------------------------
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new NavigatorMetaClassService object.
      */
     public NavigatorMetaClassService() {
         if (log.isDebugEnabled()) {
             log.debug("inited"); // NOI18N
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public HashMap getAllClasses(final String domain) {
         return ClassCacheMultiple.getClassKeyHashtableOfClassesForOneDomain(domain);
     }
 
     @Override
     public MetaClass getMetaClass(final String domain, final String tableName) {
         return ClassCacheMultiple.getMetaClass(domain, tableName);
     }
 
     @Override
     public MetaClass getMetaClass(final String domain, final int classId) {
         return ClassCacheMultiple.getMetaClass(domain, classId);
     }
 }
