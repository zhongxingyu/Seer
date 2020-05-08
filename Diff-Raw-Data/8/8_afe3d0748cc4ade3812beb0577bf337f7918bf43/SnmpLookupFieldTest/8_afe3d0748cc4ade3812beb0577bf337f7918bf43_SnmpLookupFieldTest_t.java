 package com.sppad.jots.lookup;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.Field;
 
 import org.junit.Test;
 
 import com.sppad.jots.exceptions.SnmpBadValueException;
 
 public class SnmpLookupFieldTest
 {
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	private @interface TestAnnotation
 	{
 		String value();
 	}
 
 	private static class TestAnnotationClass
 	{
 		@TestAnnotation(value = "test")
 		public int number;
 	}
 
 	private static class TestEnumClass
 	{
 		private static enum TestEnum
 		{
 			BAR, BAZ, FOO
 		}
 
 		@SuppressWarnings("unused")
 		public TestEnum testEnum = TestEnum.FOO;
 
 		@SuppressWarnings("unused")
 		public void setTestEnum(final TestEnum value)
 		{
 			this.testEnum = value;
 		}
 	}
 
 	private static class TestSetterAvailableClass
 	{
 		@SuppressWarnings("unused")
 		public int number;
 
 		@SuppressWarnings("unused")
 		public void setNumber(int number)
 		{
 			this.number = number + 1;
 		}
 	}
 
 	private static class TestSetterNotAvailableClass
 	{
 		@SuppressWarnings("unused")
 		public int number;
 	}
 
 	private SnmpLookupField createField(Object obj, String name)
 			throws NoSuchFieldException, SecurityException
 	{
 		final Field field = obj.getClass().getDeclaredField(name);
 		return SnmpLookupField.create(null, field, obj);
 	}
 
 	@Test
 	public void testGet_enum() throws SecurityException, NoSuchFieldException,
 			NoSuchMethodException
 	{
 		final SnmpLookupField slf = createField(new TestEnumClass(), "testEnum");
 
 		final String value = slf.getValue().toString();
		final String expected = TestEnumClass.TestEnum.FOO.name();
 		assertThat(value, is(expected));
 	}
 
 	@Test
 	public void testGetAnnotation() throws NoSuchFieldException,
 			SecurityException
 	{
 		final SnmpLookupField slf = createField(new TestAnnotationClass(),
 				"number");
 
 		final TestAnnotation annotation = slf
 				.getAnnotation(TestAnnotation.class);
 
 		final String actual = annotation.value();
 		final String expected = "test";
 		assertThat(actual, is(expected));
 	}
 
 	@Test
 	public void testSet_enum() throws SecurityException, NoSuchFieldException,
 			NoSuchMethodException
 	{
 		final SnmpLookupField slf = createField(new TestEnumClass(), "testEnum");
 
		slf.set(TestEnumClass.TestEnum.BAR.name());
 
 		final String actual = slf.getValue().toString();
		final String expected = TestEnumClass.TestEnum.BAR.name();
 		assertThat(actual, is(expected));
 	}
 
 	@Test(expected = SnmpBadValueException.class)
 	public void testSet_enum_badValue() throws SecurityException,
 			NoSuchFieldException, NoSuchMethodException
 	{
 		final SnmpLookupField slf = createField(new TestEnumClass(), "testEnum");
 
 		slf.set("this is a bad value");
 	}
 
 	@Test
 	public void testSet_setterAvailable() throws NoSuchFieldException,
 			SecurityException
 	{
 		final SnmpLookupField slf = createField(new TestSetterAvailableClass(),
 				"number");
 
 		slf.set("1");
 
 		final String actual = slf.getValue().toString();
 		final String expected = "2";
 
 		assertThat(actual, is(expected));
 	}
 
 	@Test
 	public void testSet_setterNotAvailable() throws NoSuchFieldException,
 			SecurityException
 	{
 		final SnmpLookupField slf = createField(
 				new TestSetterNotAvailableClass(), "number");
 
 		slf.set("1");
 
 		final String actual = slf.getValue().toString();
 		final String expected = "1";
 
 		assertThat(actual, is(expected));
 	}
 }
