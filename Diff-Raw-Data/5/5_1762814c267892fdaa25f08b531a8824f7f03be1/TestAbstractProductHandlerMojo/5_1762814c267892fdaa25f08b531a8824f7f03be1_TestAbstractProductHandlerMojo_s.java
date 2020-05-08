 package com.atlassian.maven.plugins.amps;
 
 import junit.framework.TestCase;
 import org.apache.maven.model.Build;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import java.lang.reflect.Field;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import static java.util.Arrays.asList;
 import static java.util.Collections.emptyList;
 
 public class TestAbstractProductHandlerMojo extends TestCase
 {
     public void testMakeProductsInheritDefaultConfiguration() throws Exception
     {
         SomeMojo mojo = new SomeMojo("foo");
 
         Product fooProd = new Product();
        fooProd.setId("foo");
         fooProd.setVersion("1.0");
 
         Product barProd = new Product();
        barProd.setId("bar");
         barProd.setVersion("2.0");
 
         Map<String,Product> prodMap = new HashMap<String, Product>();
         mojo.makeProductsInheritDefaultConfiguration(asList(fooProd, barProd), prodMap);
         assertEquals(2, prodMap.size());
         assertEquals("1.0", prodMap.get("foo").getVersion());
         assertEquals("/foo", prodMap.get("foo").getContextPath());
         assertEquals("2.0", prodMap.get("bar").getVersion());
         assertEquals("/foo", prodMap.get("bar").getContextPath());
     }
 
     public void testMakeProductsInheritDefaultConfigurationNoProducts() throws Exception
     {
         SomeMojo mojo = new SomeMojo("foo");
 
         Map<String,Product> prodMap = new HashMap<String, Product>();
         mojo.makeProductsInheritDefaultConfiguration(Collections.<Product>emptyList(), prodMap);
         assertEquals(1, prodMap.size());
         assertEquals("/foo", prodMap.get("foo").getContextPath());
     }
 
     public static class SomeMojo extends AbstractProductHandlerMojo
     {
         private final String defaultProductId;
 
         public SomeMojo(String defaultProductId)
         {
             this.defaultProductId = defaultProductId;
             contextPath = "/foo";
         }
 
         @Override
         protected String getDefaultProductId() throws MojoExecutionException
         {
             return defaultProductId;
         }
 
         @Override
         protected void doExecute() throws MojoExecutionException, MojoFailureException
         {
 
         }
     }
 }
