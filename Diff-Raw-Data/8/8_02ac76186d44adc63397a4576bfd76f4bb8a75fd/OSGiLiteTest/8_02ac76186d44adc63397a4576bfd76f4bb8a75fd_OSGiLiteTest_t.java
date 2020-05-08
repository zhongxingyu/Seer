 package org.jboss.weld.environment.osgi;
 
 import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.List;
 import javax.enterprise.event.Event;
 import javax.tools.Diagnostic;
 import javax.tools.DiagnosticCollector;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaCompiler.CompilationTask;
 import javax.tools.JavaFileObject;
 import javax.tools.SimpleJavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 import org.jboss.weld.environment.osgi.api.extension.Registration;
 import org.jboss.weld.environment.osgi.api.extension.ServiceRegistry;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.ServiceRegistration;
 
 /**
  *
  * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
  */
 public class OSGiLiteTest {
 
     private WeldOSGiLite weld;
     private ClassLoader loader;
 
     @Before
     public void setUp() {
         loader = new CustomClassLoader(getClass().getClassLoader());
         weld = WeldOSGiLite.start();
     }
 
     @After
     public void stop() {
         weld.stop();
     }
 
     @Test
     public void pojoRegistryTest() throws Exception {
         Event<SayHelloEvent> eventManager = weld.event().select(SayHelloEvent.class);
         SayHelloEvent event = new SayHelloEvent("Mathieu");
         PojoServiceRegistry registry = weld.instance().select(PojoServiceRegistry.class).get();
 
         EnglishGreetingServiceImpl english = weld.instance().select(EnglishGreetingServiceImpl.class).get();
         GermanGreetingServiceImpl german = weld.instance().select(GermanGreetingServiceImpl.class).get();
         SpanishGreetingServiceImpl spanish = weld.instance().select(SpanishGreetingServiceImpl.class).get();
         GreetingBean bean = weld.instance().select(GreetingBean.class).get();
 
         Assert.assertEquals(bean.getRegistrations().size(), 1);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class.getName(), null).length, 1);
         eventManager.fire(event);
         ServiceRegistration englishReg = registry.registerService(GreetingService.class.getName(),
                 english, null);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class.getName(), null).length, 2);
         eventManager.fire(event);
         ServiceRegistration germanReg = registry.registerService(GreetingService.class.getName(),
                 german, null);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class.getName(), null).length, 3);
         eventManager.fire(event);
         ServiceRegistration spannishReg = registry.registerService(GreetingService.class.getName(),
                 spanish, null);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class.getName(), null).length, 4);
         eventManager.fire(event);
 
         germanReg.unregister();
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class.getName(), null).length, 3);
         eventManager.fire(event);
         spannishReg.unregister();
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class.getName(), null).length, 2);
         eventManager.fire(event);
         englishReg.unregister();
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class.getName(), null).length, 1);
         eventManager.fire(event);
     }
 
     @Test
     public void registryTest() throws Exception {
         Event<SayHelloEvent> eventManager = weld.event().select(SayHelloEvent.class);
         SayHelloEvent event = new SayHelloEvent("Mathieu");
         ServiceRegistry registry = weld.instance().select(ServiceRegistry.class).get();
 
         EnglishGreetingServiceImpl english = weld.instance().select(EnglishGreetingServiceImpl.class).get();
         GermanGreetingServiceImpl german = weld.instance().select(GermanGreetingServiceImpl.class).get();
         SpanishGreetingServiceImpl spanish = weld.instance().select(SpanishGreetingServiceImpl.class).get();
         GreetingBean bean = weld.instance().select(GreetingBean.class).get();
 
         Assert.assertEquals(bean.getRegistrations().size(), 1);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 1);
         eventManager.fire(event);
         Registration<GreetingService> englishReg = registry.registerService(GreetingService.class, english);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 2);
         Assert.assertEquals(bean.getRegistrations().size(), 2);
         eventManager.fire(event);
         Registration<GreetingService> germanReg = registry.registerService(GreetingService.class, german);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 3);
         Assert.assertEquals(bean.getRegistrations().size(), 3);
         eventManager.fire(event);
         Registration<GreetingService> spannishReg = registry.registerService(GreetingService.class, spanish);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 4);
         Assert.assertEquals(bean.getRegistrations().size(), 4);
         eventManager.fire(event);
 
         germanReg.unregister();
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 3);
         Assert.assertEquals(bean.getRegistrations().size(), 3);
         eventManager.fire(event);
         spannishReg.unregister();
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 2);
         Assert.assertEquals(bean.getRegistrations().size(), 2);
         eventManager.fire(event);
         englishReg.unregister();
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 1);
         Assert.assertEquals(bean.getRegistrations().size(), 1);
         eventManager.fire(event);
     }
 
     @Test
     public void registrationTest() throws Exception {
         ServiceRegistry registry = weld.instance().select(ServiceRegistry.class).get();
 
         EnglishGreetingServiceImpl english = weld.instance().select(EnglishGreetingServiceImpl.class).get();
         GermanGreetingServiceImpl german = weld.instance().select(GermanGreetingServiceImpl.class).get();
         SpanishGreetingServiceImpl spanish = weld.instance().select(SpanishGreetingServiceImpl.class).get();
         GreetingBean bean = weld.instance().select(GreetingBean.class).get();
 
         Assert.assertEquals(bean.getRegistrations().size(), 1);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 1);
 
         registry.registerService(GreetingService.class, english);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 2);
         Assert.assertEquals(bean.getRegistrations().size(), 2);
 
         registry.registerService(GreetingService.class, german);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 3);
         Assert.assertEquals(bean.getRegistrations().size(), 3);
 
         registry.registerService(GreetingService.class, spanish);
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 4);
         Assert.assertEquals(bean.getRegistrations().size(), 4);
 
         bean.unregisterAll();
         Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 0);
         Assert.assertEquals(bean.getRegistrations().size(), 0);
     }
 
     @Test
     public void dynamicTest() throws Exception {
         String code = 
             "package org.jboss.weld.environment.osgi;\n" +
             "public class ItalianGreetingServiceImpl implements GreetingService {\n" +
             "    @Override\n" +
             "    public String languageName() {\n" +
             "        return \"Italian\";\n" +
             "    }\n" +
             "    @Override\n" +
             "    public String sayHello(String name) {\n" +
             "        return \"Buongiorno \" + name +  \"!\";\n" +
             "    }\n" +
             "}\n";
         JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
         DiagnosticCollector<JavaFileObject> diagnosticsCollector =
                 new DiagnosticCollector<JavaFileObject>();
         StandardJavaFileManager fileManager =
                 compiler.getStandardFileManager(diagnosticsCollector, null, null);
         JavaFileObject javaObjectFromString = new JavaObjectFromString("ItalianGreetingServiceImpl.java", code);
         Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(javaObjectFromString);
         CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector, null, null, fileObjects);
         Boolean result = task.call();
         List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsCollector.getDiagnostics();
         for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
             System.out.println(d);
         }
         Class<GreetingService> italianClazz = (Class<GreetingService>)
                 loader.loadClass("org.jboss.weld.environment.osgi.ItalianGreetingServiceImpl");
         GreetingService italianInstance = (GreetingService) italianClazz.newInstance();
         Event<SayHelloEvent> eventManager = weld.event().select(SayHelloEvent.class);
         SayHelloEvent event = new SayHelloEvent("Mathieu");
         ServiceRegistry registry = weld.instance().select(ServiceRegistry.class).get();
        Registration<GreetingService> italianReg = registry.registerService(GreetingService.class, italianInstance);
        Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 2);
         eventManager.fire(event);
         italianReg.unregister();
        Assert.assertEquals(registry.getServiceReferences(GreetingService.class).size(), 1);
         eventManager.fire(event);
     }
 
     private static class CustomClassLoader extends ClassLoader {
 
         public CustomClassLoader(ClassLoader parent) {
             super(parent);
         }
 
         @Override
         public Class<?> loadClass(String name) throws ClassNotFoundException {
             if (name.equals("org.jboss.weld.environment.osgi.ItalianGreetingServiceImpl")) {
                 byte[] b = getClassDefinition(new File("ItalianGreetingServiceImpl.class"));
                 return defineClass(name, b, 0, b.length);
             } else {
                 return super.loadClass(name);
             }
         }
 
         public static byte[] getClassDefinition(File file) {
             InputStream is = null;
             try {
                 is = new FileInputStream(file);
             } catch (FileNotFoundException ex) {
                 ex.printStackTrace();
             }
             if (is == null) {
                 return null;
             }
             try {
                 ByteArrayOutputStream os = new ByteArrayOutputStream();
                 byte[] buffer = new byte[8192];
                 int count;
                 while ((count = is.read(buffer, 0, buffer.length)) > 0) {
                     os.write(buffer, 0, count);
                 }
                 return os.toByteArray();
             } catch (Exception e) {
                 throw new RuntimeException(e);
             } finally {
                 try {
                     is.close();
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
     }
 
     private static class JavaObjectFromString extends SimpleJavaFileObject {
 
         private String contents = null;
 
         public JavaObjectFromString(String className, String contents) throws Exception {
             super(new URI(className), Kind.SOURCE);
             this.contents = contents;
         }
 
         public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
             return contents;
         }
     }
 }
