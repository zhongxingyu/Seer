 /*
  * Created on Sep 18, 2005
  */
 package uk.org.ponder.rsac;
 
 import java.beans.PropertyChangeEvent;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Level;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.PropertyValue;
 import org.springframework.beans.TypeMismatchException;
 import org.springframework.beans.factory.BeanCurrentlyInCreationException;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.BeanNameAware;
 import org.springframework.beans.factory.FactoryBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.BeanDefinitionHolder;
 import org.springframework.beans.factory.config.BeanPostProcessor;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.config.ConstructorArgumentValues;
 import org.springframework.beans.factory.config.RuntimeBeanReference;
 import org.springframework.beans.factory.support.AbstractBeanDefinition;
 import org.springframework.beans.factory.support.ManagedList;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ConfigurableApplicationContext;
 
 import uk.org.ponder.beanutil.BeanLocator;
 import uk.org.ponder.beanutil.FallbackBeanLocator;
 import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.reflect.ReflectUtils;
 import uk.org.ponder.reflect.ReflectiveCache;
 import uk.org.ponder.saxalizer.AccessMethod;
 import uk.org.ponder.saxalizer.MethodAnalyser;
 import uk.org.ponder.saxalizer.SAXalizerMappingContext;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.Denumeration;
 import uk.org.ponder.util.EnumerationConverter;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * The central class managing the Request Scope Application Context. This class
  * will be due for some refactoring as soon as the next piece of functionality
  * gets added, getting on for 400 lines. Please note that this class currently
  * illegally casts BeanDefinitions received from Spring to
  * AbstractBeanDefinition, which is a potential dependency weakness. This
  * approach is known to work with Spring 1.1.2 through 1.1.5. It also calls the
  * deprecated method BeanDefinition.getBeanClass().
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  *  
  */
 
 public class RSACBeanLocator implements ApplicationContextAware {
   private static Object BEAN_IN_CREATION_OBJECT = new Object();
   private ConfigurableApplicationContext blankcontext;
   private ApplicationContext parentcontext;
   private SAXalizerMappingContext smc;
   private ReflectiveCache reflectivecache;
 
   public RSACBeanLocator(ConfigurableApplicationContext context) {
     this.blankcontext = context;
   }
 
   public void setMappingContext(SAXalizerMappingContext smc) {
     this.smc = smc;
   }
 
   public void setApplicationContext(ApplicationContext applicationContext) {
     parentcontext = applicationContext;
   }
 
   public void setReflectiveCache(ReflectiveCache reflectivecache) {
     this.reflectivecache = reflectivecache;
   }
 
   /**
    * Initialises the RSAC container if it has not already been so by
    * protoStartRequest. Enters the supplied request and response as
    * postprocessors for any RequestAware beans in the container.
    */
   public void startRequest() {
 
   }
 
   public boolean isStarted() {
     return threadlocal.get() != null;
   }
 
   /**
    * Called at the end of a request. I advise doing this in a finally block.
    */
   public void endRequest() {
     PerRequestInfo pri = getPerRequest();
     for (int i = 0; i < pri.todestroy.size(); ++i) {
       String todestroyname = pri.todestroy.stringAt(i);
       String destroymethod = (String) rbimap.get(todestroyname);
       Object todestroy = null;
       try {
         todestroy = getBean(pri, todestroyname, false);
         reflectivecache.invokeMethod(todestroy, destroymethod);
       }
       // must try to destroy as many beans as possible, cannot propagate
       // exception in a finally block in any case.
       catch (Exception e) {
         Logger.log.error("Error destroying bean " + todestroy + " with name "
             + todestroyname, e);
       }
     }
     //    System.out.println(pri.cbeans + " beans were created");
     // Give the garbage collector a head start
     pri.clear();
   }
 
   // magic evil code from AbstractBeanFactory l.443 - this is the main reason
   // I abandoned Spring Forms and the like, and it will return to plague us.
   // Just take a look at the constructor for BeanWrapperImpl - one of these
   // is created for EVERY BEAN IN A FACTORY!
 
   // protected BeanWrapper createBeanWrapper(Object beanInstance) {
   // return (beanInstance != null ? new BeanWrapperImpl(beanInstance) : new
   // BeanWrapperImpl());
   // }
 
   // this method is really
   // resolveValueIfNecessary **LITE**, we assume all other resolution
   // is done by the parent factory and are ONLY interested in propertyvalues
   // that refer to other beans IN THIS CONTAINER.
   // org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory
   // l.900:
   // protected Object resolveValueIfNecessary(
   // String beanName, RootBeanDefinition mergedBeanDefinition, String argName,
   // Object value)
   // throws BeansException {
   // Since actual values are the rarer case, make THEM the composite ones.
   private static class ValueHolder {
     public ValueHolder(String value) {
       this.value = value;
     }
 
     public String value;
   }
 
   // returns either a String or StringList of bean names, or a ValueHolder
   private static Object propertyValueToBeanName(Object value) {
     Object beanspec = null;
     if (value instanceof BeanDefinitionHolder) {
       // Resolve BeanDefinitionHolder: contains BeanDefinition with name and
       // aliases.
       BeanDefinitionHolder bdHolder = (BeanDefinitionHolder) value;
       beanspec = bdHolder.getBeanName();
     }
     else if (value instanceof BeanDefinition) {
       throw new IllegalArgumentException(
           "No idea what to do with bean definition!");
     }
     else if (value instanceof RuntimeBeanReference) {
       RuntimeBeanReference ref = (RuntimeBeanReference) value;
       beanspec = ref.getBeanName();
     }
     else if (value instanceof ManagedList) {
       List valuelist = (List) value;
       StringList togo = new StringList();
       for (int i = 0; i < valuelist.size(); ++i) {
         String thisbeanname = (String) propertyValueToBeanName(valuelist.get(i));
         togo.add(thisbeanname);
       }
       beanspec = togo;
     }
     else if (value instanceof String) {
       beanspec = new ValueHolder((String) value);
     }
     else {
       Logger.log.warn("RSACBeanLocator Got value " + value
           + " of unknown type " + value.getClass() + ": ignoring");
     }
     return beanspec;
   }
 
   // the static information stored about each bean. Constructed at startup from
   // Spring info.
   private static class RSACBeanInfo {
     // The ACTUAL class of the bean to be FIRST constructed. The class of the
     // resultant bean may differ for a factory bean.
     Class beanclass;
     boolean isfactorybean = false;
     String initmethod;
     String destroymethod;
     String factorybean;
     String factorymethod;
     // key is dependent bean name, value is property name.
     // ultimately we will cache introspection info here.
     private HashMap localdepends = new HashMap();
     public ConstructorArgumentValues constructorargvals;
 
     public boolean hasDependencies() {
       return !localdepends.isEmpty();
     }
 
     public void recordDependency(String propertyname, Object beannames) {
       localdepends.put(propertyname, beannames);
     }
 
     public Iterator dependencies() {
       return localdepends.keySet().iterator();
     }
 
     public Object beannames(String propertyname) {
       return localdepends.get(propertyname);
     }
   }
 
   // this is a map of bean names to RSACBeanInfo
   private HashMap rbimap;
   // this is a list of the beans of type RSACLazyTargetSources 
   private StringList lazysources;
   // this is a list of "fallback" beans that will have their address space shunted
   // into the root.
   private StringList fallbacks;
 
   public void init() {
     // at this point we actually expect that the "Dead" factory is FULLY
     // CREATED. This checks that all dependencies are resolvable (if this
     // has not already been checked by the IDE).
     String[] beanNames = blankcontext.getBeanDefinitionNames();
     ConfigurableListableBeanFactory factory = blankcontext.getBeanFactory();
     // prepare our list of dependencies.
 
     rbimap = new HashMap();
     lazysources = new StringList();
     fallbacks = new StringList();
 
     for (int i = 0; i < beanNames.length; i++) {
       String beanname = beanNames[i];
       try {
         RSACBeanInfo rbi = new RSACBeanInfo();
         BeanDefinition def = factory.getBeanDefinition(beanname);
         MutablePropertyValues pvs = def.getPropertyValues();
         PropertyValue[] values = pvs.getPropertyValues();
         for (int j = 0; j < values.length; ++j) {
           PropertyValue thispv = values[j];
           Object beannames = propertyValueToBeanName(thispv.getValue());
           boolean skip = false;
           // skip recording the dependency if it was unresolvable (some
           // unrecognised
           // type) or was a single-valued type referring to a static dependency.
           // NB - we now record ALL dependencies - bean-copying strategy
           // discontinued.
           if (beannames == null
           // || beannames instanceof String
           // && !blankcontext.containsBean((String) beannames)
           ) {
             skip = true;
           }
           if (!skip) {
             rbi.recordDependency(thispv.getName(), beannames);
           }
         }
         // yes, I am PERFECTLY aware this is a deprecated method, but it is the
         // only
         // visible way to determine ahead of time whether this will be a factory
         // bean.
         // Bit of a problem here with Spring flow - apparently the bean class
         // will NOT be set for a "factory-method" bean UNTIL it has been
         // instantiated
         // via the logic in AbstractAutowireCapableBeanFactory l.376:
         // protected BeanWrapper instantiateUsingFactoryMethod(
         AbstractBeanDefinition abd = (AbstractBeanDefinition) def;
         rbi.factorybean = abd.getFactoryBeanName();
         rbi.factorymethod = abd.getFactoryMethodName();
         rbi.initmethod = abd.getInitMethodName();
         rbi.destroymethod = abd.getDestroyMethodName();
         if (abd.hasConstructorArgumentValues()) {
           rbi.constructorargvals = abd.getConstructorArgumentValues();
         }
         if (rbi.factorymethod == null) {
           // all right then BE like that! We'll work out the class later.
          Class beanclass = def.getBeanClass();
           rbi.beanclass = beanclass;
           rbi.isfactorybean = FactoryBean.class.isAssignableFrom(beanclass);
           if (abd.isLazyInit()) {
             lazysources.add(beanname);
           }
           if (FallbackBeanLocator.class.isAssignableFrom(beanclass)) {
             fallbacks.add(beanname);
           }
         }
 
         rbimap.put(beanname, rbi);
       }
       catch (Exception e) {
         Logger.log.error("Error loading definition for bean " + beanname, e);
       }
     }
   }
 
   private ThreadLocal threadlocal = new ThreadLocal() {
     public Object initialValue() {
       return new PerRequestInfo(RSACBeanLocator.this, lazysources);
     }
   };
 
   private PerRequestInfo getPerRequest() {
     return (PerRequestInfo) threadlocal.get();
   }
 
   public void addPostProcessor(BeanPostProcessor beanpp) {
     getPerRequest().postprocessors.add(beanpp);
   }
 
   private Object getLocalBean(PerRequestInfo pri, String beanname, boolean nolazy) {
     Object bean = pri.beans.locateBean(beanname);
     if (bean == BEAN_IN_CREATION_OBJECT) {
       throw new BeanCurrentlyInCreationException(beanname);
     }
     else if (bean == null) {
       FactoryBean pfb = (FactoryBean) pri.lazysources.get(beanname);
       if (pfb != null && !nolazy) {
         try {
         return pfb.getObject();
         }
         catch (Exception e) {
           throw UniversalRuntimeException.accumulate(e, "Error getting proxied bean");
         }
       }
       else {
         bean = createBean(pri, beanname);
       }
     }
     return bean;
   }
 
   // package access ensures visibility from RSACLazyTargetSource
   Object getBean(PerRequestInfo pri, String beanname, boolean nolazy) {
     Object bean = null;
     if (blankcontext.containsBean(beanname)) {
       bean = getLocalBean(pri, beanname, nolazy);
     }
     else {
       bean = getFallbackBean(pri, beanname, nolazy);
       if (bean == null) {
         bean = this.parentcontext.getBean(beanname);
       }
     }
     return bean;
   }
 
   private Object getFallbackBean(PerRequestInfo pri, String beanname, boolean nolazy) {
     for (int i = 0; i < fallbacks.size(); ++ i) {
       String fallbackbean = fallbacks.stringAt(i);
       BeanLocator locator = (BeanLocator) getLocalBean(pri, fallbackbean, true);
       Object togo = locator.locateBean(beanname);
       if (togo != null) return togo;
     }
     return null;
   }
 
   private Object assembleVectorProperty(PerRequestInfo pri,
       StringList beannames, Class declaredType) {
     Object deliver = ReflectUtils.instantiateContainer(declaredType, beannames
         .size(), reflectivecache);
     Denumeration den = EnumerationConverter.getDenumeration(deliver);
     for (int i = 0; i < beannames.size(); ++i) {
       String thisbeanname = beannames.stringAt(i);
       Object bean = getBean(pri, thisbeanname, false);
       den.add(bean);
     }
     return deliver;
   }
 
   private Object createBean(PerRequestInfo pri, String beanname) {
     ++pri.cbeans;
     pri.beans.set(beanname, BEAN_IN_CREATION_OBJECT);
     RSACBeanInfo rbi = (RSACBeanInfo) rbimap.get(beanname);
     if (rbi == null) {
       throw new NoSuchBeanDefinitionException(beanname,
           "Bean definition not found");
     }
 
     Object newbean;
     // NB - isn't this odd, and in fact generally undocumented - properties
     // defined for factory-method beans are set on the PRODUCT, whereas those
     // set on FactoryBeans are set on the FACTORY!!
     if (rbi.factorybean != null) {
       Object factorybean = getBean(pri, rbi.factorybean, false);
       newbean = reflectivecache.invokeMethod(factorybean, rbi.factorymethod);
       if (newbean == null) {
         throw new IllegalArgumentException("Error: null returned from factory method " + 
             rbi.factorymethod + " of bean " + rbi.factorybean);
       }
       rbi.beanclass = newbean.getClass();
     }
     else {
       // Locate the "dead" bean from the genuine Spring context, and clone it
       // as quick as we can - bytecodes might do faster but in the meantime
       // observe that a clone typically costs 1.6 reflective calls so in general
       // this method will win over a reflective solution.
       // NB - all Copiables simply copy dependencies manually for now, no cost.
       // Copiable deadbean = (Copiable) livecontext.getBean(rbi.isfactorybean?
       // "&" +beanname : beanname);
       // All the same, the following line will cost us close to 1us - unless it
       // invokes manual code!
       newbean = reflectivecache.construct(rbi.beanclass);
     }
     if (rbi.hasDependencies()) {
       // guard this block since if it is a factory-method bean it may be
       // something
       // extremely undesirable (like an inner class) that we should not even
       // dream of reflecting over. If on the other hand the user has specified
       // some dependencies they doubtless know what they are doing.
       MethodAnalyser ma = MethodAnalyser.getMethodAnalyser(rbi.beanclass, smc);
       // Object clonebean = deadbean.copy();
       // iterate over each LOCAL dependency of the bean with given name.
       for (Iterator depit = rbi.dependencies(); depit.hasNext();) {
         String propertyname = (String) depit.next();
         try {
           AccessMethod setter = ma.getAccessMethod(propertyname);
           Object depbean = null;
           Object beanref = rbi.beannames(propertyname);
           if (beanref instanceof String) {
             depbean = getBean(pri, (String) beanref, false);
           }
           else if (beanref instanceof ValueHolder) {
             Class accezzz = setter.getAccessedType();
             String value = ((ValueHolder) beanref).value;
             if (smc.saxleafparser.isLeafType(accezzz)) {
               depbean = smc.saxleafparser.parse(accezzz, value);
             }
             else {
               // exception def copied from the beast BeanWrapperImpl!
               throw new TypeMismatchException(new PropertyChangeEvent(newbean,
                   propertyname, null, value), accezzz, null);
             }
           }
           else {
             // Really need generalised conversion of vector values here.
             // The code to do this is actually WITHIN the grotty BeanWrapperImpl
             // itself in a protected method with 5 arguments!!
             // This is a sort of 50% solution. It will deal with all 1-d array
             // types and collections
             // although clearly there is no "value" support yet and probably
             // never will be.
             depbean = assembleVectorProperty(pri, (StringList) beanref, setter
                 .getDeclaredType());
           }
           // Lose another 500ns here, until we bring on FastClass.
           setter.setChildObject(newbean, depbean);
         }
         catch (Exception e) {
           throw UniversalRuntimeException.accumulate(e,
               "Error setting dependency " + propertyname + " of bean "
                   + beanname);
         }
       }
     }
     // process it FIRST since it will be the factory that is expecting the
     // dependencies
     // set!
     processNewBean(pri, beanname, newbean);
     if (rbi.initmethod != null) {
       reflectivecache.invokeMethod(newbean, rbi.initmethod);
     }
     if (newbean instanceof InitializingBean) {
       try {
         ((InitializingBean) newbean).afterPropertiesSet();
       }
       catch (Exception e) { // Evil Rod! Bad Juergen!
         throw UniversalRuntimeException.accumulate(e);
       }
     }
     if (rbi.destroymethod != null) {
       pri.todestroy.add(beanname);
     }
     if (newbean instanceof FactoryBean) {
       FactoryBean factorybean = (FactoryBean) newbean;
       try {
         newbean = factorybean.getObject();
       }
       catch (Exception e) {
         throw UniversalRuntimeException.accumulate(e);
       }
     }
     // enter the bean into the req-specific map.
     pri.beans.set(beanname, newbean);
     // now the bean is initialised, attempt to call any init-method or InitBean.
 
     return newbean;
   }
 
   private void processNewBean(PerRequestInfo pri, String beanname,
       Object newbean) {
     for (int i = 0; i < pri.postprocessors.size(); ++i) {
       BeanPostProcessor beanpp = (BeanPostProcessor) pri.postprocessors.get(i);
       try {
         beanpp.postProcessBeforeInitialization(newbean, beanname);
         // someday we might put something in between here.
         beanpp.postProcessAfterInitialization(newbean, beanname);
       }
       catch (Exception e) {
         Logger.log.log(Level.ERROR, "Exception processing bean "
             + newbean.getClass().getName(), e);
       }
     }
     if (newbean instanceof BeanFactoryAware) {
       ((BeanFactoryAware) newbean).setBeanFactory(pri.blfactory);
     }
     if (newbean instanceof BeanNameAware) {
       ((BeanNameAware) newbean).setBeanName(beanname);
     }
     if (newbean instanceof ApplicationContextAware) {
       ((ApplicationContextAware) newbean).setApplicationContext(parentcontext);
     }
   }
 
   
   /** Returns the class of this bean, if it can be statically determined,
    * <code>null</code> if it cannot (i.e. this bean is the product of a 
    * factory-method of a class which is not yet known)
    * @param beanname
    * @return
    */
   public Class getBeanClass(String beanname) {
     RSACBeanInfo rbi = (RSACBeanInfo) rbimap.get(beanname);
     if (rbi == null) { 
       return null;
     }
     else if (rbi.beanclass == null) {
       return rbi.beanclass;
     }
     else if (rbi.factorymethod != null && rbi.factorybean != null) {
       RSACBeanInfo factoryrbi = (RSACBeanInfo) rbimap.get(rbi.factorybean);
       Class factoryclass = factoryrbi == null? null : factoryrbi.beanclass;
       Method m = ReflectiveCache.getMethod(factoryclass, rbi.factorymethod);
       if (m != null) {
         rbi.beanclass = m.getReturnType();
       }
     }
     // Noone could possibly say we didn't do our best to work out the type of this bean.
     return rbi.beanclass;
   }
   
   /**
    * This method gets a BeanLocator which is good just for the current request
    * scope. The ThreadLocal barrier has already been breached in the returned
    * object, and evaluation will proceed quickly.
    */
   public WriteableBeanLocator getBeanLocator() {
     PerRequestInfo pri = getPerRequest();
     return pri.requestwbl;
   }
 
   /**
    * Scope of this BeanLocator is the same as previous, but it will NOT
    * auto-create beans that are not present.
    */
   public WriteableBeanLocator getDeadBeanLocator() {
     PerRequestInfo pri = getPerRequest();
     return pri.beans;
   }
   
 }
