 package com.atlassian.sal.fisheye.appconfig;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.springframework.beans.factory.FactoryBean;
 
 import com.cenqua.fisheye.AppConfig;
 import com.cenqua.crucible.model.managers.ProjectManager;
 import com.cenqua.crucible.actions.admin.project.ProjectDataFactory;
 import com.atlassian.sal.api.component.ComponentLocator;
 import com.atlassian.crucible.spi.TxTemplate;
 
 public class FisheyeAccessorFactoryBean implements FactoryBean
 {
     private static final ClassLoader FISHEYE_HOST_CLASSLOADER = AppConfig.class.getClassLoader();
 
     public Object getObject() throws Exception
     {
         ProjectManager projectManager = ComponentLocator.getComponent(ProjectManager.class);
         ProjectDataFactory projectDataFactory = ComponentLocator.getComponent(ProjectDataFactory.class);
         TxTemplate txTemplate = ComponentLocator.getComponent(TxTemplate.class);
         return wrapService(new Class[]{FisheyeAccessor.class}, new DefaultFisheyeAccessor(projectManager,
             projectDataFactory, txTemplate), FISHEYE_HOST_CLASSLOADER);
     }
 
     public Class<?> getObjectType()
     {
         return FisheyeAccessor.class;
     }
 
     public boolean isSingleton()
     {
         return true;
     }
 
     /**
      * Copied from DefaultComponentRegistrar.java Wraps the service in a dynamic proxy that ensures all methods are
      * executed with the passed class loader as the context class loader
      *
      * @param interfaces The interfaces to proxy
      * @param service The instance to proxy
      * @param contextClassLoader
      * @return A proxy that wraps the service
      */
     private Object wrapService(final Class<?>[] interfaces, final Object service, final ClassLoader contextClassLoader)
     {
         return Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, new InvocationHandler()
         {
             public Object invoke(final Object o, final Method method, final Object[] objects) throws Throwable
             {
                 final Thread thread = Thread.currentThread();
                 final ClassLoader originalContextClassLoader = thread.getContextClassLoader();
                 try
                 {
                     thread.setContextClassLoader(contextClassLoader);
                     return method.invoke(service, objects);
                 }
                 catch (final InvocationTargetException e)
                 {
                     throw e.getTargetException();
                 }
                 finally
                 {
                     thread.setContextClassLoader(originalContextClassLoader);
                 }
             }
         });
     }
 
 }
