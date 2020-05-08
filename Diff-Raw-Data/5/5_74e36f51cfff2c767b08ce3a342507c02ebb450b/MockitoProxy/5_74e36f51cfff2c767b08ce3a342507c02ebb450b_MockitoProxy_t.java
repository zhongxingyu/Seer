 package org.kalibro.tests;
 
import static org.junit.Assert.assertTrue;

 import java.lang.reflect.Constructor;
 import java.lang.reflect.Modifier;
 
 import org.hamcrest.Matcher;
 import org.kalibro.core.abstractentity.AbstractEntity;
 import org.mockito.ArgumentMatcher;
 import org.mockito.Matchers;
 import org.mockito.Mockito;
 import org.mockito.verification.VerificationMode;
 import org.powermock.api.mockito.PowerMockito;
 
 public abstract class MockitoProxy extends PowerMockito {
 
 	public static <T> T mockAbstract(Class<T> type) throws Exception {
 		T mock = mock(type, Mockito.CALLS_REAL_METHODS);
 		shouldHavePublicDefaultConstructor(mock);
 		return mock;
 
 	}
 
 	private static <T> void shouldHavePublicDefaultConstructor(T mock) throws Exception {
 		Constructor<?> constructor = mock.getClass().getConstructor();
		assertTrue(Modifier.isPublic(constructor.getModifiers()));
 		constructor.newInstance();
 	}
 
 	public static <T> T verify(T mock) {
 		return Mockito.verify(mock);
 	}
 
 	public static <T> T verify(T mock, VerificationMode mode) {
 		return Mockito.verify(mock, mode);
 	}
 
 	public static VerificationMode never() {
 		return Mockito.never();
 	}
 
 	public static VerificationMode once() {
 		return Mockito.times(1);
 	}
 
 	public static VerificationMode times(int times) {
 		return Mockito.times(times);
 	}
 
 	public static <T> T any() {
 		return Matchers.any();
 	}
 
 	public static String anyString() {
 		return Matchers.anyString();
 	}
 
 	public static long anyLong() {
 		return Matchers.anyLong();
 	}
 
 	public static <T> T any(Class<T> type) {
 		return Matchers.any(type);
 	}
 
 	public static <T> T anyVararg() {
 		return Matchers.anyVararg();
 	}
 
 	public static <T> T isA(Class<T> type) {
 		return Matchers.isA(type);
 	}
 
 	public static <T> T eq(T expected) {
 		return Matchers.eq(expected);
 	}
 
 	public static <T> T same(T expected) {
 		return Matchers.same(expected);
 	}
 
 	public static <T extends AbstractEntity<? super T>> T deepEq(final T expected) {
 		return Matchers.argThat(new ArgumentMatcher<T>() {
 
 			@Override
 			public boolean matches(Object argument) {
 				return expected.deepEquals(argument);
 			}
 		});
 	}
 
 	public static <T> T argThat(Matcher<T> matcher) {
 		return Matchers.argThat(matcher);
 	}
 }
