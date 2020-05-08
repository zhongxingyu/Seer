 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat Middleware LLC, and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.test.classloading.vfs.client.support.launcher;
 
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
 import org.jboss.beans.metadata.spi.ValueMetaData;
 import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
 import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
 import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
 import org.jboss.dependency.spi.Controller;
 import org.jboss.dependency.spi.ControllerMode;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.plugins.bootstrap.AbstractBootstrap;
 import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
 import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
 import org.jboss.kernel.plugins.deployment.xml.BasicXMLDeployer;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.kernel.spi.deployment.KernelDeployment;
 import org.jboss.logging.Logger;
 
 /**
  * An application client launcher mock up that uses the mc, vfs, class loaders
  * to launch the application client environment and call its main method.
  * 
  * @author Scott.Stark@jboss.org
  * @version $Revision:$
  */
 public class ClientLauncher
 {
    private static Logger log = Logger.getLogger(ClientLauncher.class);
    private static Exception exception;
    /** The kernel */
    private static Kernel kernel;
 
    /** The deployer */
    private static BasicXMLDeployer deployer;
    
    /** The default mode */
    private static ControllerMode defaultMode = ControllerMode.AUTOMATIC;
 
    private static void init() throws Throwable
    {
       // Bootstrap the kernel
       AbstractBootstrap bootstrap = getBootstrap();
       bootstrap.run();
       kernel = bootstrap.getKernel();
       
       // Create the deployer
       deployer = createDeployer();
 
    }
 
    private static BasicXMLDeployer createDeployer()
    {
       return new BasicXMLDeployer(kernel, defaultMode);
    }
 
    /**
     * Get the defaultMode.
     * 
     * @return the defaultMode.
     */
    public ControllerMode getDefaultMode()
    {
       return defaultMode;
    }
 
    /**
     * Set the defaultMode.
     * 
     * @param defaultMode the defaultMode.
     */
    public void setDefaultMode(ControllerMode defaultMode)
    {
       this.defaultMode = defaultMode;
    }
 
    /**
     * Get the kernel bootstrap
     * 
     * @return the bootstrap
     * @throws Exception for any error
     */
    private static AbstractBootstrap getBootstrap() throws Exception
    {
       return new BasicBootstrap();
    }
    
    /**
     * Get a bean
     *
     * @param name the name of the bean
     * @param state the state of the bean
     * @return the bean
     * @throws IllegalStateException when the bean does not exist at that state
     */
    private static Object getBean(final Object name, final ControllerState state)
    {
       KernelControllerContext context = getControllerContext(name, state);
       return context.getTarget();
    }
    
    /**
     * Get a bean
     *
     * @param <T> the expected type
     * @param name the name of the bean
     * @param state the state of the bean
     * @param expected the expected type
     * @return the bean
     * @throws IllegalStateException when the bean does not exist at that state
     */
    private static <T> T getBean(final Object name, final ControllerState state, final Class<T> expected)
    {
       if (expected == null)
          throw new IllegalArgumentException("Null expected");
       Object bean = getBean(name, state);
       return expected.cast(bean);
    }
    
    /**
     * Get a context
     *
     * @param name the name of the bean
     * @param state the state of the bean
     * @return the context
     * @throws IllegalStateException when the context does not exist at that state
     */
    private static KernelControllerContext getControllerContext(final Object name, final ControllerState state)
    {
       KernelController controller = kernel.getController();
       KernelControllerContext context = (KernelControllerContext) controller.getContext(name, state);
       if (context == null)
          return handleNotFoundContext(controller, name, state);
       return context;
    }
    
    /**
     * Handle not found context.
     *
     * @param controller the controller
     * @param name the name of the bean
     * @param state the state of the bean
     * @return the context
     * @throws IllegalStateException when the context does not exist at that state
     */
    private static KernelControllerContext handleNotFoundContext(Controller controller, Object name, ControllerState state)
    {
       throw new IllegalStateException("Bean not found: '" + name + "' at state " + state);
    }
 
    /**
     * Validate
     * 
     * @throws Exception for any error
     */
    private static void validate() throws Exception
    {
       try
       {
          deployer.validate();
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Exception e)
       {
          throw e;
       }
       catch (Error e)
       {
          throw e;
       }
       catch (Throwable t)
       {
          throw new RuntimeException(t);
       }
    }
    
    /**
     * Deploy a url
     *
     * @param url the deployment url
     * @return the deployment
     * @throws Exception for any error  
     */
    private static KernelDeployment deploy(URL url) throws Exception
    {
       try
       {
          log.debug("Deploying " + url);
          KernelDeployment deployment = deployer.deploy(url);
          log.trace("Deployed " + url);
          return deployment;
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Exception e)
       {
          throw e;
       }
       catch (Error e)
       {
          throw e;
       }
       catch (Throwable t)
       {
          throw new RuntimeException(t);
       }
    }
 
    /**
     * Deploy a deployment
     *
     * @param deployment the deployment
     * @throws Exception for any error  
     */
    private static void deploy(KernelDeployment deployment) throws Exception
    {
       log.debug("Deploying " + deployment);
       try
       {
          deployer.deploy(deployment);
          log.debug("Deployed " + deployment);
       }
       catch (Exception e)
       {
          throw e;
       }
       catch (Error e)
       {
          throw e;
       }
       catch (Throwable t)
       {
          throw new RuntimeException("Error deploying deployment: " + deployment, t);
       }
    }
    
    /**
     * Undeploy a deployment
     * 
     * @param deployment the deployment
     */
    private static void undeploy(KernelDeployment deployment)
    {
       log.debug("Undeploying " + deployment.getName());
       try
       {
          deployer.undeploy(deployment);
          log.trace("Undeployed " + deployment.getName());
       }
       catch (Throwable t)
       {
          log.warn("Error during undeployment: " + deployment.getName(), t);
       }
    }
    
    /**
     * Undeploy a deployment
     * 
     * @param url the url
     */
    protected void undeploy(URL url)
    {
       log.debug("Undeploying " + url);
       try
       {
          deployer.undeploy(url);
          log.trace("Undeployed " + url);
       }
       catch (Throwable t)
       {
          log.warn("Error during undeployment: " + url, t);
       }
    }
    
    @SuppressWarnings("unused")
    private static void shutdown()
    {
       kernel.getController().shutdown();
    }
 
    /**
     * Undeploy all
     */
    @SuppressWarnings("unused")
    private static void undeploy()
    {
       log.debug("Undeploying " + deployer.getDeploymentNames());
       deployer.shutdown();
    }
 
    
    public static Exception getException()
    {
       return exception;
    }
 
    /**
     * The client launcher entry point that create an mc to launch the client container.
     * @param clientClass
     * @param clientName
     * @param cp
     * @param args
     * @throws Throwable
     */
    public static void launch(String clientClass, String clientName, String[] cp, String[] args)
       throws Throwable
    {
       // Init the kernel and deployers
       init();
 
       // Deploy the common launcher beans
       String common = "/org/jboss/test/classloading/vfs/metadata/Common.xml";
       URL url = ClientLauncher.class.getResource(common);
       if (url == null)
          throw new IllegalStateException(common + " not found");
       deploy(url);
       validate();
 
       try
       {
          VFSClassLoaderFactory factory = new VFSClassLoaderFactory("ClientLauncherClassLoader");
          ArrayList<String> roots = new ArrayList<String>();
          // This will come from the client metadata
          for(String path : cp)
             roots.add(path);
          factory.setRoots(roots);
          // Do we have to export all packages? Not going to know them...
          int lastDot = clientClass.lastIndexOf('.');
          if(lastDot > 0)
          {
             String clientPackage = clientClass.substring(0, lastDot);
             ClassLoadingMetaDataFactory cfactory = ClassLoadingMetaDataFactory.getInstance();
             factory.getCapabilities().addCapability(cfactory.createModule(clientPackage));
             factory.getCapabilities().addCapability(cfactory.createPackage(clientPackage));
          }
          //factory.setIncluded(ClassFilter.JAVA_ONLY);
 
          ArrayList<BeanMetaDataFactory> beanFactories = new ArrayList<BeanMetaDataFactory>();
          beanFactories.add(factory);
          BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder("ClientContainer", ClientContainer.class.getName());
          // ClientContainer(Object metaData, String mainClass, String applicationClientName)
          Object metaData = null;
          builder.addConstructorParameter(Object.class.getName(), metaData);
          builder.addConstructorParameter(Class.class.getName(), clientClass);
          builder.addConstructorParameter(String.class.getName(), clientName);
          String classLoaderName = factory.getContextName();
          if(classLoaderName == null)
             classLoaderName = factory.getName() + ":" + factory.getVersion();
          ValueMetaData classLoader = builder.createInject(classLoaderName);
          builder.setClassLoader(classLoader);
          BeanMetaData clientContainerMD = builder.getBeanMetaData();
    
          AbstractKernelDeployment deployment = new AbstractKernelDeployment();
          deployment.setName(factory.getName() + ":" + factory.getVersion());
          if(clientContainerMD instanceof BeanMetaDataFactory)
          {
             BeanMetaDataFactory bmdf = (BeanMetaDataFactory) clientContainerMD;
             beanFactories.add(bmdf);
          }
          else
          {
             // Have to use the deprecated beans
             ArrayList<BeanMetaData> beans = new ArrayList<BeanMetaData>();
             beans.add(clientContainerMD);
             deployment.setBeans(beans);
          }
          deployment.setBeanFactories(beanFactories);
          deploy(deployment);
          validate();
 
          ClientContainer client = getBean("ClientContainer", ControllerState.INSTALLED, ClientContainer.class);
          if(client == null )
             throw new Exception("ClientContainer bean was not created");
          ClassLoader ccLoader = getBean(classLoaderName, ControllerState.INSTALLED, ClassLoader.class);
          if(ccLoader == null )
             throw new Exception(classLoaderName+" bean was not created");
          Class<?> mainClass = client.getMainClass();
          ClassLoader mainClassLoader = mainClass.getClass().getClassLoader();
          if(ccLoader != mainClassLoader)
             throw new Exception(ccLoader+" != "+mainClassLoader);
 
          // Invoke main on the underlying client main class
          Class<?> parameterTypes[] = { args.getClass() };
          Method method = client.getClass().getDeclaredMethod("invokeMain", parameterTypes);
          method.invoke(client, (Object) args);
 
          undeploy(deployment);
       }
       catch(Exception e)
       {
          exception = e;
       }
    }
 }
