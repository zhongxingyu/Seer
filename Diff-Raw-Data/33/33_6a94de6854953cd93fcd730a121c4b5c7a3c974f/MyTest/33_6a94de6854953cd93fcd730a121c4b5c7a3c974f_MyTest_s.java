 /**
  * 
  */
 package org.jboss.solderbug.test;
 
 import java.io.FileNotFoundException;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.GenericArchive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
 import org.jboss.solderbug.MyBean;
 import org.jboss.solderbug.MyExtension;
 import org.jboss.solderbug.MyQualifier;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * @author antoine
  * 
  */
 
 @RunWith(Arquillian.class)
 public class MyTest {
     @Inject
     @MyQualifier("myValue")
     MyBean myBean;
 
    @Inject
     MyBean unqualifiedBean;
 
     @Deployment
     public static Archive<?> createTestArchive() throws FileNotFoundException {
         Archive<?> ret = ShrinkWrap
                 .create(WebArchive.class, "test.war")
                 .addClasses(MyBean.class, MyQualifier.class, MyExtension.class)
 
                 .addAsLibraries(
                        DependencyResolvers.use(MavenDependencyResolver.class).loadReposFromPom("pom.xml")
                                .artifact("org.jboss.seam.solder:seam-solder").resolveAs(GenericArchive.class));
         return ret;
     }
 
     @Test
     public void testHello() {
 
        Assert.assertTrue("Hello CDI World ".equals(myBean.saySomething()));
     }
 
     @Test
     public void testUnqualifiedHello() {
 
         Assert.assertTrue("Hello CDI World ".equals(unqualifiedBean.saySomething()));
     }
 
 }
