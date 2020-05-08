 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /*
  * @file
  * @par File Name:
  * FunctionReflectionTest.java
  * @author Yoichiro Shimizu
  * @date Created on 2013/11/19
  * @par Copyright:
  * Ricoh IT Solutions, LTD.
  */
 
 public class FunctionReflectionTest {
 
 	private FunctionReflection targetClass;
 
 	@Before
 	public void setUp() throws Exception {
 		targetClass = new FunctionReflection();
 	}
 
 	/**
 	 * {@link FunctionReflection#FunctionReflection()} のためのテスト・メソッド。
 	 */
 	@Test
 	public void testFunctionReflection() {
 		try {
 			targetClass = new FunctionReflection();
 		} catch (Exception e) {
 			e.getCause();
 			fail("Constructor Failed.");
 		}
 
 	}
 
 	/**
 	 * {@link FunctionReflection#methodInvoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
 	 * のためのテスト・メソッド。
 	 */
 	@Test
 	public void testMethodInvoke() {
 		String targetObjectString = new String("test");
 		Method executeMethod = null;
 		Object resultObject = null;
 		/* test arg method */
 		try {
 			executeMethod = String.class.getDeclaredMethod("charAt", int.class);
 			assertNotNull(executeMethod);
 			executeMethod.setAccessible(true);
 		} catch (SecurityException e) {
 			fail();
 		} catch (NoSuchMethodException e) {
 			fail();
 		}
 		try {
 			resultObject = targetClass.methodInvoke(targetObjectString,
 					executeMethod, 1);
 		} catch (Throwable e) {
 			e.printStackTrace();
 			fail();
 		}
 
 		assertEquals(resultObject, targetObjectString.charAt(1));
 
 		/* test nothing arg method */
 		try {
 			executeMethod = String.class.getDeclaredMethod("toString");
 			assertNotNull(executeMethod);
 			executeMethod.setAccessible(true);
 		} catch (SecurityException e) {
 			fail();
 		} catch (NoSuchMethodException e) {
 			fail();
 		}
 		try {
 			resultObject = targetClass.methodInvoke(targetObjectString,
 					executeMethod);
 		} catch (Throwable e) {
 			e.printStackTrace();
 			fail();
 		}
 		assertEquals(resultObject, targetObjectString.toString());
 	}
 
 	/**
 	 * {@link FunctionReflection#setMethodMap(java.lang.reflect.Method[])}
 	 * のためのテスト・メソッド。
 	 */
 	@Test
 	public void testSetMethodMap() {
 		fail("まだ実装されていません");
 	}
 
 	/**
 	 * {@link FunctionReflection#getMethodMap(java.lang.String)} のためのテスト・メソッド。
 	 */
 	@Test
 	public void testGetMethodMap() {
 		fail("まだ実装されていません");
 	}
 
 	/**
 	 * {@link FunctionReflection#getFieldList(java.lang.Object)} のためのテスト・メソッド。
 	 */
 	@Test
 	public void testGetFieldList() {
 		String targetObjString = new String("testGetFieldList");
 		List<String> result = targetClass.getFieldList(targetObjString);
 		for (String string : result) {
 			System.out.println(string);
 		}
 
 	}
 
 	/**
 	 * {@link FunctionReflection#setField(java.lang.Object, java.lang.String, java.lang.String)}
 	 * のためのテスト・メソッド。
 	 */
 	@Test
 	public void testSetField() {
 		String targetObjString = new String("testSetField");
 		try {
 			targetClass.setField(targetObjString, "hash", "1");
 		} catch (Throwable e) {
 			e.printStackTrace();
 			fail();
 		}
 		assertThat(targetObjString.hashCode(), is(1));
 		try {
 			targetClass.setField(targetObjString, "count", "10");
 		} catch (Throwable e) {
 			e.printStackTrace();
 			fail();
 		}
 		assertThat(targetObjString.length(), is(10));
 	}
 
 	/**
 	 * {@link FunctionReflection#getField(java.lang.Object, java.lang.String)}
 	 * のためのテスト・メソッド。
 	 */
 	@Test
 	public void testGetField() {
 		fail("まだ実装されていません");
 	}
 
 	/**
 	 * {@link FunctionReflection#changeType(java.lang.reflect.Type, java.lang.String)}
 	 * のためのテスト・メソッド。
 	 */
 	@Test
 	public void testChangeType() {
 		fail("まだ実装されていません");
 	}
 
 	@Test
 	public void testMakeInstanceByConstructor() {
 		Constructor<?> targetConstructor = null;
 		Object resultObject = null;
 		try {
 			targetConstructor = String.class.getConstructor(String.class);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 		try {
 			resultObject = targetClass.makeInstanceByConstructor(
 					targetConstructor, "test");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		assertEquals(resultObject.toString(), "test");
 		resultObject = null;
 		try {
 			targetConstructor = String.class.getConstructor();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 		try {
 			resultObject = targetClass
 					.makeInstanceByConstructor(targetConstructor);
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		assertNotNull(resultObject);
 	}
 
 	@Test
 	public void testMakeArrayNewInstance() {
 		Object[] resultObject = null;
 		int dim = 4;
 		resultObject = (Object[])targetClass.makeArrayNewInstance(Integer.class, dim);
 		assertNotNull(resultObject);
 		assertEquals(resultObject.length, dim);
 		Object[][] resultDouble = null;
 		int dim2 = 3;
 		Object d = new Double(2);
 		resultDouble = targetClass
 				.makeArrayNewInstance(d, dim, dim2);
 		assertEquals(resultDouble[0].length, dim2);
 		assertEquals(Double.class, resultDouble.getClass().getComponentType().getComponentType());
 	}
 	
 	@Test
 	public void testSplitMethodName(){
 		Method inputMethod = null;
 		try {
 			inputMethod = Double.class.getDeclaredMethod("compareTo", Double.class);
 
 		} catch (SecurityException e) {
 			fail();
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail();
 			e.printStackTrace();
 		}
 		String result = targetClass.splitMethodName(inputMethod);
 		assertEquals("compareTo(java.lang.Double)", result);
 	}
 	
 	
 	
 	@Test
 	public void testGetMethodParamType(){
 		Method inputMethod = null;
 		try {
 			inputMethod = Double.class.getDeclaredMethod("compareTo", Double.class);
 
 		} catch (SecurityException e) {
 			fail();
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail();
 			e.printStackTrace();
 		}
 		Type[] result = targetClass.getMethodParamType(inputMethod);
 		assertEquals(Double.class.toString(), result[0].toString());
 	}
 	@SuppressWarnings("unchecked")
 	@Test
 	public <T> void testGetConstructorParamType(){
 		Constructor<T> inputConstructor = null;
 		try {
 			inputConstructor = (Constructor<T>) Double.class.getConstructor(String.class);
 
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 			fail();
 		}
 		Type[] result = targetClass.getConstructorParamType(inputConstructor);
 		assertEquals(String.class.toString(), result[0].toString());
 	}
 
 }
