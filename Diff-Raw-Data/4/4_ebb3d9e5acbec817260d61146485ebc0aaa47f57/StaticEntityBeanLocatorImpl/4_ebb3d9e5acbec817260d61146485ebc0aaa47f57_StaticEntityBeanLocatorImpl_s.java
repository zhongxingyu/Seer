 /*
  * Created on 21 Aug 2007
  */
 package uk.org.ponder.rsf.state.entity.support;
 
 import org.springframework.beans.factory.BeanNameAware;
 import org.springframework.beans.factory.FactoryBean;
 
 import uk.org.ponder.beanutil.BeanModelAlterer;
 import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.reflect.ReflectiveCache;
 import uk.org.ponder.rsac.RSACBeanLocator;
 import uk.org.ponder.rsf.state.entity.EntityNameInferrer;
 
 /**
  * An "automated" implementation of BeanLocator, useful for managing "entities", 
  * addressed by a unique ID convertible to String, which are already provided with
  * some form of (probably application-scope) "DAO"-type API. This implementation
  * allows the DAO API to be expressed by means of EL method bindings to its 
  * various methods, from which the managed entities are mapped into this 
  * this request-scope cache.
  * </p>
  * Typically the user will provide a <code>fetchMethod</code> which will fetch
  * entities by ID, and (at least one of) a <code>newMethod</code> or an
  * <code>entityClass</code> which is used to instantiate new entities which
  * are not persistent. <p/> At any time, the "cache" of currently addressed
  * entities within this request may be accessed through the map
  * <code>deliveredBeans</code> 
  * <p/> If the <code>saveEL</code> is also
  * supplied, this locator will also implement a <code>saveAll()</code> method
  * which will return all the delivered entities back to persistence. If this is
  * not supplied, they are assumed to be persisted by some other means (perhaps
  * an auto-commit of some kind).
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class StaticEntityBeanLocatorImpl implements
   BeanNameAware, FactoryBean, EntityNameInferrer {
   // package level members for access from EntityBeanLocatorImpl
   String fetchEL;
   String newEL;
   Class entityClazz;
   String saveEL;
   String removeEL;
 
   BeanModelAlterer bma;
   RSACBeanLocator RSACbeanlocator;
   ReflectiveCache reflectivecache;
   private String beanName;
 
   public void setFetchMethod(String fetchEL) {
     this.fetchEL = fetchEL;
   }
 
   public void setNewMethod(String newEL) {
     this.newEL = newEL;
   }
 
   public void setEntityClass(Class entityClazz) {
     this.entityClazz = entityClazz;
   }
 
   public void setSaveMethod(String saveEL) {
     this.saveEL = saveEL;
   }
 
   public void setRemoveMethod(String removeEL) {
     this.removeEL = removeEL;
   }
   
   public void setBeanModelAlterer(BeanModelAlterer bma) {
     this.bma = bma;
   }
 
   public void setRSACBeanLocator(RSACBeanLocator RSACbeanlocator) {
     this.RSACbeanlocator = RSACbeanlocator;
   }
 
   public void setReflectiveCache(ReflectiveCache reflectivecache) {
     this.reflectivecache = reflectivecache;
   }
 
   public void init() {
     if (entityClazz == null && newEL == null) {
       throw new IllegalArgumentException(
           "At least one of entityClass and newEL must be set");
     }
   }
 
   public Object getObject() throws Exception {
     WriteableBeanLocator wbl = RSACbeanlocator.getDeadBeanLocator();
     if (wbl.locateBean(beanName) != null) {
       throw new IllegalStateException("Incorrect use of EntityBeanLocator detected. " +
       		"This implementation is designed to work only with the RSACBeanLocator, " +
       		"and must cause the delivered BeanLocator to be immediately cached " +
       		"in the RSAC on first use per request");
     }
     EntityBeanLocatorImpl togo = new EntityBeanLocatorImpl(this);
     wbl.set(beanName, togo);
     return togo;
   }
 
   public Class getObjectType() {
     return EntityBeanLocator.class;
   }
 
   public boolean isSingleton() {
     return false;
   }
 
   public void setBeanName(String beanName) {
     this.beanName = beanName;
   }
 
   public String getEntityName(Class entityClazz) {
     return (entityClazz == this.entityClazz? beanName : null); 
   }
 }
