 package org.hors.test;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ArchivePaths;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.jpa.persistence.PersistenceDescriptor;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(Arquillian.class)
 public class TemperatureConverterTest {
    @Inject
    private TemperatureConverter converter;
 

    @Deployment
    public static JavaArchive createTestArchive() {
       return ShrinkWrap.create(JavaArchive.class, "test.jar")
          .addClasses(TemperatureConverter.class)
          .addAsManifestResource(
             EmptyAsset.INSTANCE, 
             ArchivePaths.create("beans.xml")); 
    }
 
    @Test
    public void testConvertToCelsius() {
       Assert.assertEquals(converter.convertToCelsius(32d), 0d, 0.01d);
       Assert.assertEquals(converter.convertToCelsius(212d), 100d, 0.01d);
    }
 
    @Test
    public void testConvertToFarenheit() {
       Assert.assertEquals(converter.convertToFarenheit(0d), 32d, 0.01d);
       Assert.assertEquals(converter.convertToFarenheit(100d), 212d, 0.01d);
    }
 }
