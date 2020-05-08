 package org.nohope.spring.app;
 
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
 import org.springframework.context.support.GenericApplicationContext;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 /**
  * Date: 30.07.12
  * Time: 17:41
  */
 public class GetImplementationsTest {
 
     private interface Iface {
 
     }
 
     private interface Marker1 {
 
     }
 
     private interface Marker2 {
 
     }
 
 
     @Test
     public void testGetImpl() {
         final List<ModuleDescriptor<Iface>> allModules = new ArrayList<>();
 
         class M1Impl implements Marker1, Iface {
         }
 
         class M2Impl implements Marker2, Iface {
         }
 
         final Properties properties = new Properties();
         final GenericApplicationContext ctx = new GenericApplicationContext();
         allModules.add(new ModuleDescriptor<Iface>("M1-1", new M1Impl() {
         }, properties, ctx));
         allModules.add(new ModuleDescriptor<Iface>("M1-2", new M1Impl() {
         }, properties, ctx));
         allModules.add(new ModuleDescriptor<Iface>("M2-1", new M2Impl() {
         }, properties, ctx));
         allModules.add(new ModuleDescriptor<Iface>("M2-2", new M2Impl() {
         }, properties, ctx));
 
         final List<Marker1> lst = HandlerWithStorage.getImplementations(Marker1.class, allModules);
         assertEquals(2, lst.size());
 
        assertThat(lst, JUnitMatchers.everyItem(new BaseMatcher<Marker1>() {
             @Override
             public boolean matches(final Object o) {
                 return o instanceof M1Impl;
             }
 
             @Override
             public void describeTo(final Description description) {
                 description.appendText("Something");
             }
         }));
     }
 
 
 }
