 /**
  * 
  */
 package com.zynga.sdk.cxx;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 /**
  * @author rvergis
  * 
  */
 public class CXXTypeTest extends TestCase {
 
 	/**
 	 * @param name
 	 */
 	public CXXTypeTest(String name) {
 		super(name);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public void testTypeClass()
 			throws NoSuchMethodException
 	{
 		System.out.println("====testTypeClass=====");
 		Class[] classes = new Class[] { A.class  };
 		String[] expected = new String[] {
 			"java.util.List", //77
 			"java.util.List", //78
 			"java.util.List", //79
 			"java.lang.Number", //80
 			"java.lang.Number", //81
 			"java.lang.String", //82
 			"int", //83
 			"java.util.Map", //84
 			"java.util.Map", //85
 			"java.util.Map", //86
 			"java.util.Map", //87
 			"com.zynga.sdk.cxx.CXXType$Array", //88
 			"com.zynga.sdk.cxx.CXXType$Array", //89
 			"com.zynga.sdk.cxx.CXXType$Array", //90
 			"com.zynga.sdk.cxx.CXXType$Array", //91
 			"com.zynga.sdk.cxx.CXXType$Array", //92
 			"com.zynga.sdk.cxx.CXXType$Array", //93
 			"java.util.List", //94
 			"java.util.List", //95
 			"java.util.List", //96
 			"java.util.List", //97
 			"java.util.List", //98
 			"java.util.List", //99
 			"java.util.List" // 100
 		};
 		int testIdx = 0;
 		for (Class clazz : classes)
 		{
 			Method[] methods = clazz.getDeclaredMethods();
 			for (Method method : methods) 
 			{
 				Type[] types = method.getGenericParameterTypes();
 				for (Type type : types)
 				{
 					CXXType cxxType = new CXXType(type);
 					assertEquals("method " + method.getName(), expected[testIdx++], cxxType.getTypeClass() == null ? null : cxxType.getTypeClass().getName());
 				}
 			}
 		}
 	}	
 	
 	public void testTypePackage()
 		throws NoSuchMethodException {
 		
 		System.out.println("====testTypePackage=====");
 		Class[] classes = new Class[] { A.class  };
 		
 		String[] expected = new String[] {
 				"java.util", 	//m77
 				"java.util", 	//m78
 				"java.util", 	//m79
 				"java.lang", 	//m80
 				"java.lang", 	//m81
 				"java.lang", 	//m82
 				null,		 	//m83
 				"java.util",	//m84
 				"java.util",	//m85
 				"java.util",	//m86
 				"java.util",	//m87
 				null,			//m88
 				null,			//m89
 				null,			//m90
 				null,			//m91
 				null,			//m92
 				null,			//m93
 				"java.util",	//m94
 				"java.util",	//m95
 				"java.util",	//m96
 				"java.util",	//m97
 				"java.util",	//m98
 				"java.util",	//m99
 				"java.util"		//m100
 			};
 			int testIdx = 0;
 			for (Class clazz : classes)
 			{
 				Method[] methods = clazz.getDeclaredMethods();
 				for (Method method : methods) 
 				{
 					Type[] types = method.getGenericParameterTypes();
 					for (Type type : types)
 					{
 						CXXType cxxType = new CXXType(type);
 						assertEquals("method " + method.getName(), expected[testIdx++], cxxType.getTypePackage() == null ? null : cxxType.getTypePackage().getName());
 					}
 				}
 			}
 	}
 	
 	public void testType()
 		throws NoSuchMethodException {
 		
 		System.out.println("====testType=====");
 		Class[] classes = new Class[] { A.class  };
 		
 		String[] expected = new String[] {
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.TypeVariable",
 				"java.lang.reflect.TypeVariable",
 				"java.lang.Class",
 				"java.lang.Class",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.Class",
 				"java.lang.Class",
 				"java.lang.reflect.GenericArrayType",
 				"java.lang.reflect.GenericArrayType",
 				"java.lang.reflect.GenericArrayType",
 				"java.lang.reflect.GenericArrayType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType",
 				"java.lang.reflect.ParameterizedType"
 			};
 			int testIdx = 0;
 			for (Class clazz : classes)
 			{
 				Method[] methods = clazz.getDeclaredMethods();
 				for (Method method : methods) 
 				{
 					Type[] types = method.getGenericParameterTypes();
 					for (Type type : types)
 					{
 						CXXType cxxType = new CXXType(type);
 						assertEquals(expected[testIdx++], cxxType.getTypeType() == null ? null : cxxType.getTypeType().getName());
 					}
 				}
 			}
 	}
 	
 	@SuppressWarnings("rawtypes")
	public void testParamTypeHierarchy()
 			throws NoSuchMethodException
 		{
 			System.out.println("====testTypeHierarchy=====");
 			Class[] classes = new Class[] { A.class  };
 			String[] expected = new String[] {
 				"java.util.List<java.util.List<java.util.List<java.util.List<java.util.List<java.lang.Number>>>>>", //77
 				"java.util.List<java.util.List<java.lang.Number>>", //78
 				"java.util.List<java.util.List<java.lang.Number>>", //79
 				"java.lang.Number", //80
 				"java.lang.Number", //81
 				"java.lang.String", //82
 				"int", //83
 				"java.util.Map<java.util.Map,java.util.Map<java.lang.String,java.lang.Integer>>", //84
 				"java.util.Map<java.util.Map,java.util.Map<java.lang.String,java.lang.Object>>", //85
 				"java.util.Map<java.util.Map,java.util.Map<java.lang.String,java.lang.Integer>>", //86
 				"java.util.Map<java.lang.String,java.lang.Integer>", //87
 				"java.lang.Integer[]", //88
 				"int[]", //89
 				"java.util.List<java.lang.Number>[]", //90
 				"java.util.List<java.lang.Number>[]", //91
 				"java.util.List<java.util.List<java.lang.String>>[]", //92
 				"java.util.List<java.util.List<java.lang.String>>[]", //93
 				"java.util.List<java.util.List<java.lang.String[]>>", //94
 				"java.util.List<java.lang.String[]>", //95
 				"java.util.List<java.util.List<java.util.List<java.lang.String>>>", //96
 				"java.util.List<java.lang.Number>", //97
 				"java.util.List<java.lang.Integer>", //98
 				"java.util.List<java.lang.Double>", //99
 				"java.util.List<java.lang.String>" // 100
 			};
 			int testIdx = 0;
 			for (Class clazz : classes)
 			{
 				Method[] methods = clazz.getDeclaredMethods();
 				for (Method method : methods) 
 				{
 					Type[] types = method.getGenericParameterTypes();
 					for (Type type : types)
 					{
 						CXXType cxxType = new CXXType(type);
 						assertEquals(expected[testIdx++], cxxType.toString());
 					}
 				}
 			}
 		}	
	
	@SuppressWarnings("rawtypes")
	public void testReturnTypeHierarchy()
			throws NoSuchMethodException
		{
			System.out.println("====testTypeHierarchy=====");
			Class[] classes = new Class[] { B.class  };
			String[] expected = new String[] {
				"java.util.List<java.lang.String>", //100
			};
			int testIdx = 0;
			for (Class clazz : classes)
			{
				Method[] methods = clazz.getDeclaredMethods();
				for (Method method : methods) 
				{
					Type type = method.getGenericReturnType();
					CXXType cxxType = new CXXType(type);
					assertEquals(expected[testIdx++], cxxType.toString());
				}
			}
		}	
 
 	// ~ CLASSES
 
 	static class A<U extends Number> {
 		
 		public <T extends List<? extends U>> void m77(List<? extends List<? extends List<? extends List<T>>>> s) // java.lang.reflect.ParameterizedType
 		{
 			
 		}
 		
 		public <T extends U> void m78(List<? extends List<? extends T>> s) // java.lang.reflect.ParameterizedType
 		{
 			
 		}
 		
 		public void m79(List<? extends List<? extends U>> s) // java.lang.reflect.ParmeterizedType
 		{
 			
 		}
 		
 		public <T extends U> void m80(T s) { // java.lang.reflect.TypeVariable
 
 		}
 
 		public void m81(U s) { // java.lang.reflect.TypeVariable
 
 		}
 
 		public void m82(String s) { // java.lang.Class
 
 		}
 
 		public void m83(int s) { // java.lang.Class
 
 		}
 
 		public <T extends Integer> void m84(Map<Map, Map<String, T>> s) { // java.lang.reflect.ParameterizedType
 
 		}
 
 		public <T> void m85(Map<Map, Map<String, T>> s) { // java.lang.reflect.ParameterizedType
 
 		}
 
 		public void m86(Map<Map, Map<String, Integer>> s) { // java.lang.reflect.ParameterizedType
 
 		}
 
 		public void m87(Map<String, Integer> s) { // java.lang.ParameterizedType
 
 		}
 
 		public void m88(Integer[] s) { // java.lang.Class
 
 		}
 
 		public void m89(int[] s) { // java.lang.Class
 
 		}
 
 		public <T extends List<? extends U>> void m90(T[] s) { // java.lang.reflect.GenericTypeArray
 
 		}
 
 		public <T extends List<U>> void m91(T[] s) { // java.lang.reflect.GenericTypeArray
 
 		}
 
 		public <T extends List<String>> void m92(List<T>[] s) { // java.lang.reflect.GenericArrayType
 
 		}
 
 		public void m93(List<List<String>>[] s) { // java.lang.reflect.GenericArrayType
 
 		}
 
 		public void m94(List<List<String[]>> s) { // java.lang.reflect.ParameterizedType
 
 		}
 
 		public void m95(List<String[]> s) { // java.lang.reflect.ParameterizedType
 
 		}
 
 		public void m96(List<List<List<String>>> s) { // java.lang.reflect.ParameterizedType
 
 		}
 
 		public <T extends U> void m97(List<? extends T> s) { // java.lang.reflect.ParameterizedType
 
 		}
 
 		public <T extends Integer> void m98(List<? extends T> s) { // java.lang.reflect.ParameterizedType
 
 		}
 
 		public void m99(List<? extends Double> s) { // java.lang.reflect.ParameterizedType
 
 		}
 
 		public void m100(List<String> s) { // java.lang.reflect.ParameterizedType
 
 		}
 	}
	
	class B {
		
		public List<String> m100() { 
			return null;
		}
	}
 
 }
