 package org.nohope.spring.app;
 
 import org.junit.Test;
 import org.nohope.spring.app.module.IModule;
 
 import javax.annotation.Resource;
 import java.io.File;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.atomic.AtomicReference;
 
 import static org.junit.Assert.*;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 7/27/12 5:29 PM
  */
 public class SpringAsyncModularAppTest {
 
     @Test
     public void appIsAnonymousClass() throws Exception {
         final AppWithContainer app = new AppWithContainer(null, "", "/") {
         };
         assertEquals("springAsyncModularAppTest", app.getAppName());
         assertEquals("/", app.getAppMetaInfNamespace());
         assertEquals("/", app.getModuleMetaInfNamespace());
     }
 
     @Test
     public void appDefaultContextOverriding() throws Exception {
         final AppWithContainer app = new AppWithContainer("appo", "appContextOverriding");
         probe(app);
 
         assertNotNull(app.getContext());
         assertEquals("appBeanOverridden", app.getContext().getBean("appBean"));
     }
 
     @Test
     public void moduleDefaultContextOverriding() throws Exception {
         final AppWithContainer app = new AppWithContainer("app", "", "moduleContextOverriding");
         probe(app);
 
         assertEquals(1, app.getModules().size());
         final InjectModuleWithContextValue m = getModule(app, 0);
         assertEquals("overridden", m.getValue());
         assertEquals("moduleo", m.getName());
         assertEquals("appBean", m.getContext().getBean("appBean"));
     }
 
     @Test
     public void searchPathsDetermining() throws Exception {
         final AppWithContainer app = new AppWithContainer();
         assertEquals("appWithContainer", app.getAppName());
         assertEquals("org.nohope.spring.app/", app.getAppMetaInfNamespace());
         assertEquals("org.nohope.spring.app/module/", app.getModuleMetaInfNamespace());
         assertEquals(IModule.class, app.getTargetModuleClass());
     }
 
     @Test
     public void concatTest() {
 
         assertEquals("test1" + File.separator + "test2" + File.separator +"test3" , AppWithContainer.concat("test1", "test2/", "/test3"));
     }
 
     @Test
     public void illegalModuleDescriptor() throws Exception {
         final AppWithContainer app = new AppWithContainer("app", "", "illegalDescriptor") {
         };
         probe(app);
         assertEquals(0, app.getModules().size());
     }
 
     @Test
     public void nonexistentModuleClass() throws Exception {
         final AppWithContainer app = new AppWithContainer("app", "", "nonexistentClass") {
         };
         probe(app);
 
         assertEquals(0, app.getModules().size());
     }
 
     @Test
     public void notAModuleClass() throws Exception {
         final AppWithContainer app = new AppWithContainer("app", "", "notAModule") {
         };
         probe(app);
 
         assertEquals(0, app.getModules().size());
     }
 
     /* all beans in contexts should be constructed ONCE! */
     @Test
     public void multipleConstructing() throws Exception {
         final AppWithContainer app = new AppWithContainer("app", "once", "once/module");
         probe(app);
 
         assertEquals(2, app.get(OnceConstructable.class).getId());
     }
 
     @Test
     public void legalModuleDefaultContext() throws Exception {
         final AppWithContainer app = new AppWithContainer("app", "", "legalModuleDefaultContext") {
         };
         probe(app);
 
         assertEquals(1, app.getModules().size());
         final InjectModuleWithContextValue m = getModule(app, 0);
         assertEquals("123", m.getValue());
         assertEquals("legal", m.getName());
         final Properties p = m.getProperties();
         assertEquals(2, p.size());
         assertEquals("\"fuck yeah!\"", p.getProperty("property"));
 
         // check for app beans inheritance
         assertEquals("appBean", m.getContext().getBean("appBean"));
     }
 
     public static class UtilsBean {
         // http://stackoverflow.com/a/1363435
         @Resource(name = "testList")
         private List<String> list;
 
         public List<String> getList() {
             return list;
         }
     }
 
     @Test
     public void utilsSupport() throws InterruptedException {
         final AppWithContainer app = new AppWithContainer(
                 "utils",
                 "utils",
                 "legalModuleDefaultContext") {
         };
         probe(app);
         assertNotNull(app.get("testList", List.class));
         final UtilsBean bean = app.getOrInstantiate(UtilsBean.class);
 
         final List<String> list = bean.getList();
         assertNotNull(list);
         assertEquals("one", list.get(0));
         assertEquals("two", list.get(1));
         assertEquals("three", list.get(2));
     }
 
     @SuppressWarnings("unchecked")
     private static <T extends IModule> T getModule(final AppWithContainer app,
                                                    final int index) {
         assertTrue(app.getModules().size() >= index);
         final IModule module = app.getModules().get(index);
         assertNotNull(module);
         try {
             return (T) module;
         } catch (ClassCastException e) {
             fail();
             return null;
         }
     }
 
     private static void probe(final AppWithContainer app) throws InterruptedException {
         final AtomicReference<Throwable> ref = new AtomicReference<>();
         final Thread t = new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     app.start();
                 } catch (final Exception e) {
                     ref.set(e);
                 }
             }
         });
 
         t.start();
         app.stop();
         t.join();
 
        if (ref.get() != null) {
            throw new IllegalStateException(ref.get());
         }
     }
 }
