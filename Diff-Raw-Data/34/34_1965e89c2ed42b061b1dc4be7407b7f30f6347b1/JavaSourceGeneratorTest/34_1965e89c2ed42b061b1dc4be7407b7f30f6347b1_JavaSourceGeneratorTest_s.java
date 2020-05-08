 /**
  *
  */
 package com.insightfullogic.vertx.promises.codegen.java;
 
 import static com.insightfullogic.vertx.promises.codegen.java.JavaSourceGenerator.GENERATED_SOURCES;
 import static com.sun.codemodel.JMod.PUBLIC;
 import static java.nio.charset.Charset.defaultCharset;
 import static java.nio.file.Files.readAllLines;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.eventbus.EventBus;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.json.JsonObject;
 
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JTypeVar;
 
 /**
  * @author richard
  *
  */
 public class JavaSourceGeneratorTest {
 
 	@BeforeClass
 	public static void regenerate() throws Exception {
 	    Method send = EventBus.class.getMethod("send", String.class, JsonObject.class);
	    
 		JavaSourceGenerator generator = new JavaSourceGenerator();
 		generator.newClass(EventBus.class);
         generator.wrapMethod(send);
 		generator.convertMethod("registerHandler", Message.class, Arrays.<Class<?>>asList(String.class));
 		generator.generate();
 	}
 
 	@Test
 	public void generatesFile() {
 		File file = getFile();
 		assertTrue(file.exists());
 		assertTrue(file.isFile());
 	}
 
 	@Test
 	public void generatesPackage() throws IOException {
 		assertFileContains("package " + JavaSourceGenerator.DEFAULT_PKG + ";");
 	}
 
 	@Test
 	public void generatesClass() throws IOException {
 		assertFileContains("public final class PromiseEventBus");
 	}
 
 	@Test
 	public void generatesConstructor() throws IOException {
 	    assertFileContains("public PromiseEventBus(EventBus _eventbus) {");
 		assertFileContains("eventbus = _eventbus;");
 	}
 
 	@Test
 	public void generatesBinding() throws IOException {
 	    assertFileContains("Promise<Message> promise = new DefaultPromise<>();");
 		assertFileContains("eventbus.registerHandler(param0, promise);");
 		assertFileContains("return promise;");
 	}
 
     @Test
     public void wrapsMethod() throws Exception {
        assertFileContains("public EventBus send(String param0, String param1) {");
         assertFileContains("return eventbus.send(param0, param1);");
     }
 
 	// Message case
 	@Test
 	public void classesAreUsedAsBounds() {
 		JavaSourceGenerator generator = new JavaSourceGenerator();
 		JClass type = generator.convertType(Message.class, noBindings());
 		assertEquals(Message.class.getName(), type.fullName());
 	}
 
 	// ? extends Message case
 	@Test
 	public void wildcardsAreUsedAsBounds() throws Exception {
 		JavaSourceGenerator generator = new JavaSourceGenerator();
 
 		Method unregisterHandler = EventBus.class.getMethod("unregisterHandler", String.class, Handler.class);
 		ParameterizedType lastType = (ParameterizedType) unregisterHandler.getGenericParameterTypes()[1];
 		Type bound = lastType.getActualTypeArguments()[0];
 		JClass type = generator.convertType(bound, noBindings());
 
 		assertEquals(bound.toString(), type.fullName());
 	}
 
 	// <T> ... Message<T> case
 	@Test
 	public void parameterisedGenericsAreUsedAsBounds() throws Exception {
 		JavaSourceGenerator generator = new JavaSourceGenerator();
 
 		Method send = EventBus.class.getMethod("send", String.class, JsonObject.class, Handler.class);
 		ParameterizedType lastType = (ParameterizedType) send.getGenericParameterTypes()[2];
 		Type bound = lastType.getActualTypeArguments()[0];
 
 		List<TypeVariable<?>> bindings = new ArrayList<>();
 		JClass type = generator.convertType(bound, bindings);
 
 		// First operation generates the right bindings
 		assertEquals(1, bindings.size());
 		assertEquals("org.vertx.java.core.eventbus.Message<null>", type.fullName());
 
 		JMethod method = generator.code._class("bar").method(PUBLIC, type, "foo");
 		generator.rebindGenerics(type, method, bindings);
 
 		assertEquals(bound.toString(), type.fullName());
 
 		// Check generics added to method
 		JTypeVar[] typeParams = method.typeParams();
 		assertEquals(1, typeParams.length);
 		assertEquals("T", typeParams[0].name());
 	}
 
 	public List<TypeVariable<?>> noBindings() {
 		return Collections.<TypeVariable<?>>emptyList();
 	}
 
 	public File getFile() {
 		return new File("target/" + GENERATED_SOURCES + "/com/insightfullogic/vertx/promises/PromiseEventBus.java");
 	}
 
 	public void assertFileContains(String toFind) throws IOException {
 		File file = getFile();
 		for (String line : readAllLines(file.toPath(), defaultCharset())) {
 			if (line.contains(toFind)) {
 				return;
 			}
 		}
 		fail("Unable to find: " + toFind);
 	}
 
 }
