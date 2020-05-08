 package org.jboss.har2arq.containers;
 
 
 import org.jboss.arquillian.impl.DynamicServiceLoader;
 import org.jboss.arquillian.impl.XmlConfigurationBuilder;
 import org.jboss.arquillian.impl.context.ClassContext;
 import org.jboss.arquillian.impl.context.SuiteContext;
 import org.jboss.arquillian.spi.Configuration;
 import org.jboss.arquillian.spi.DeployableContainer;
 import org.jboss.arquillian.spi.LifecycleException;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.importer.ZipImporter;
 import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.testharness.api.DeploymentException;
 import org.jboss.testharness.spi.Containers;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.ServiceLoader;
 import java.util.Set;
 import java.util.zip.ZipInputStream;
 
 
 /**
  * Adaptor between an arquillian {@link DeployableContainer} and test harnesses
  * {@link Containers}
  *
  * @author Stuart Douglas
  */
 public class ArquillianContainerAdaptor implements Containers
 {
    private DeployableContainer container;
    private SuiteContext suiteContext;
    private ClassContext context;
    private Configuration configuration;
    private Archive<?> swArchive;
    private org.jboss.arquillian.spi.DeploymentException exception;
 
    public void setup() throws IOException
    {
       container = loadDeployableContainer();
       suiteContext = new SuiteContext(new DynamicServiceLoader());
       XmlConfigurationBuilder builder = new XmlConfigurationBuilder();
       configuration = builder.build();
       container.setup(suiteContext, configuration);
       try
       {
          container.start(suiteContext);
       }
       catch (LifecycleException e)
       {
          throw new RuntimeException(e);
       }
    }
 
    public void cleanup() throws IOException
    {
       try
       {
          container.stop(suiteContext);
       }
       catch (LifecycleException e)
       {
          throw new RuntimeException(e);
       }
    }
 
    public boolean deploy(InputStream archive, String name) throws IOException
    {
       exception = null;
      ClassContext context = new ClassContext(suiteContext);
      context.add(Configuration.class,configuration);
       if(name.endsWith("ear")) {
          swArchive = ShrinkWrap.create(EnterpriseArchive.class, name);
       } else if(name.endsWith("war")) {
          swArchive = ShrinkWrap.create(WebArchive.class, name);
       } else if(name.endsWith("jar")) {
          swArchive = ShrinkWrap.create(JavaArchive.class, name);
       } else {
          throw new RuntimeException("Unkown archive extension: " + name);
       }
       swArchive.as(ZipImporter.class).importZip(new ZipInputStream(archive));
       try
       {
          container.deploy(context, this.swArchive);
          return true;
       }
       catch (org.jboss.arquillian.spi.DeploymentException e)
       {
          exception = e;
          return false;
       }
    }
 
    public DeploymentException getDeploymentException()
    {
       return new DeploymentException(exception.getCause().getClass().getName(), exception.getCause());
    }
 
    public void undeploy(String name) throws IOException
    {
       try
       {
          container.undeploy(context, swArchive);
       }
       catch (org.jboss.arquillian.spi.DeploymentException e)
       {
          throw new RuntimeException(e);
       }
    }
 
    private static final DeployableContainer loadDeployableContainer()
    {
       final String arquillianContainer = System.getProperty("org.jboss.har2arq.container");
       if (arquillianContainer != null)
       {
          try
          {
             Class<?> clazz = Class.forName(arquillianContainer);
             return (DeployableContainer) clazz.newInstance();
          }
          catch (Exception e)
          {
             throw new RuntimeException(e);
          }
       }
 
       // first we need to load the deployable containers
       final ServiceLoader<DeployableContainer> container = ServiceLoader.load(DeployableContainer.class);
       final Set<DeployableContainer> containers = new HashSet<DeployableContainer>();
       for (DeployableContainer aContainer : container)
       {
          containers.add(aContainer);
       }
       if (containers.isEmpty())
       {
          throw new RuntimeException("No Arquillian DeployableContainer found on the class path.");
       }
       if (containers.size() > 1)
       {
          throw new RuntimeException("More than one DeployableContainer found on the class path. " + containers);
       }
       return containers.iterator().next();
    }
 }
