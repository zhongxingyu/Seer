 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.editors;
 
 import Sirius.server.middleware.types.MetaClass;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class EditorBeanInitializerStore {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final EditorBeanInitializerStore INSTANCE = new EditorBeanInitializerStore();
 
     //~ Instance fields --------------------------------------------------------
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditorBeanInitializerStore.class);
     private final Map<MetaClass, BeanInitializer> initializerStore;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new EditorBeanInitializerStore object.
      */
     private EditorBeanInitializerStore() {
         initializerStore = TypeSafeCollections.newHashMap();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static EditorBeanInitializerStore getInstance() {
         return INSTANCE;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  metaClass    DOCUMENT ME!
      * @param  initializer  DOCUMENT ME!
      */
     public void registerInitializer(final MetaClass metaClass, final BeanInitializer initializer) {
         initializerStore.put(metaClass, initializer);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaClass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean unregisterInitializer(final MetaClass metaClass) {
         return initializerStore.remove(metaClass) != null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaClass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public BeanInitializer getInitializer(final MetaClass metaClass) {
         return initializerStore.get(metaClass);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Collection<MetaClass> getAllRegisteredClasses() {
         return Collections.unmodifiableCollection(initializerStore.keySet());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   toInitialize  DOCUMENT ME!
      *
      * @throws  Exception                 DOCUMENT ME!
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     public void initialize(final CidsBean toInitialize) throws Exception {
         if (toInitialize != null) {
             final BeanInitializer initializer = initializerStore.get(toInitialize.getMetaObject().getMetaClass());
             if (initializer != null) {
                 initializer.initializeBean(toInitialize);
             }
        } else {
            throw new IllegalArgumentException("Bean to initialize was null!"); // NOI18N
         }
     }
 }
