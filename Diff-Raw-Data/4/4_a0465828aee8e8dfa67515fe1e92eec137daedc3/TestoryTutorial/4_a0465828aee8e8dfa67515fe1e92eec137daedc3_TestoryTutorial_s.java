 package org.testory.doc;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 import org.testory.TestoryAssertionError;
 
 /**
  * <p>
  * <ul>
  * <li><a href="#basics">Basics</a></li>
  * <li><a href="#assertionsStandalone">Assertions - standalone</a></li>
  * <li><a href="#assertionsThenReturned">Assertions - thenReturned</a></li>
  * <li><a href="#assertionsThenThrown">Assertions - thenThrown</a></li>
  * <li><a href="#matchers">Matchers</a></li>
  * <li><a href="#closures">Closures</a></li>
  * <li><a href="#macros">Macros</a></li>
  * <li><a href="#dummies">Dummies</a></li>
  * </ul>
  * </p>
  * <p>
  * <h2><a name="basics">Basics</a></h2>
  * To make <b>given</b>, <b>when</b>, <b>then</b> family of methods available add following import
  * to your test class.
  * 
  * <pre>
  * import static org.testory.Testory.*;
  * </pre>
  * 
  * The most basic purpose of <b>given</b> and <b>when</b> is decorative so instead writing comments
  * like this
  * 
  * <pre>
  * // given
  * list = new ArrayList&lt;String&gt;();
  * // when
  * list.add(&quot;element&quot;);
  * </pre>
  * 
  * you wrap lines inside methods
  * 
  * <pre>
  * given(list = new ArrayList&lt;String&gt;());
  * when(list.add(&quot;element&quot;));
  * </pre>
  * 
  * The purpose of <b>then</b> is to make an assertion and throw {@link TestoryAssertionError} if it
  * fails. The most basic assertion asserts that condition is true. This works just like junit's
  * <b>assertTrue</b>.
  * 
  * <pre>
  * given(list = new ArrayList&lt;String&gt;());
  * when(list.add(&quot;element&quot;));
  * then(!list.isEmpty());
  * </pre>
  * 
  * <b>Given/when</b> can be used in chained form. This is helpful in various situations like dealing
  * with void methods. Because void cannot be an argument
  * 
  * <pre>
  * <del>given(list.clear());</del>
  * <del>when(list.clear());</del>
  * </pre>
  * 
  * you should use chained forms.
  * 
  * <pre>
  * given(list).clear();
  * when(list).clear();
  * </pre>
  * 
  * </p>
  * 
  * <p>
  * <h2><a name="assertionsStandalone">Assertions - standalone</a></h2>
  * Few assertions can be used on their own. They are similar to junit's assertions
  * <ul>
  * <li><code>then(boolean)</code> is like <code>assertTrue(boolean)</code></li>
 * <li><code>then(Object, Object)</code> is like <code>assertEquals(Object, Object)</code> or like
 * <code>assertThat(T, Matcher&lt;T&gt;)</code></li>
  * </ul>
  * </p>
  * 
  * <p>
  * <h2><a name="assertionsThenReturned">Assertions - thenReturned</a></h2>
  * <b>ThenReturned</b> is used to make assertions about result returned by <b>when</b>. Result is
  * considered to be an object passed as an argument to <b>when</b> method. Assertion fails if result
  * is not equal to expected.
  * 
  * <pre>
  * given(list = new ArrayList&lt;String&gt;());
  * given(list.add(&quot;element&quot;));
  * when(list.get(0));
  * thenReturned(&quot;element&quot;);
  * </pre>
  * 
  * If <b>when</b> is in chained form then result is object returned by following call. (notice moved
  * parenthesis)
  * 
  * <pre>
  * given(list = new ArrayList&lt;String&gt;());
  * given(list.add(&quot;element&quot;));
  * when(list).get(0);
  * thenReturned(&quot;element&quot;);
  * </pre>
  * 
  * Matchers can be used to make custom assertions
  * 
  * <pre>
  * given(list = new ArrayList&lt;String&gt;());
  * given(list.add(&quot;element&quot;));
  * when(list.clone());
  * thenReturned(not(sameInstance(list)));
  * </pre>
  * 
  * </p>
  * 
  * <p>
  * <h2><a name="assertionsThenThrown">Assertions - thenThrown</a></h2>
  * <b>ThenThrown</b> is used to make an assertions about throwable thrown by <b>when</b>. Because of
  * java syntax <b>when</b> must be in chained form.
  * 
  * <pre>
  * given(list = new ArrayList&lt;String&gt;());
  * when(list).get(0);
  * thenThrown(IndexOutOfBoundsException.class);
  * </pre>
  * 
  * Notice that chained form of when catches any throwable preventing it from failing a test. This
  * may cause following standalone assertion to succeed even if throwable was thrown.<br/>
  * 
  * <b>ThenThrown</b> is overloaded to accept throwable instance, class or matcher.
  * 
  * </p>
  * 
  * <p>
  * <h2><a name="matchers">Matchers</a></h2>
  * Wherever api method accepts Object, but states that it accepts matcher, you are free to pass any
  * of compatible matchers
  * 
  * <ul>
  * <li>org.hamcrest.Matcher</li>
  * <li>org.fest.assertions.Condition</li>
  * <li>com.google.common.base.Predicate</li>
  * <li>com.google.common.base.Function</li>
  * <li>dynamic matcher
  * 
  * <pre>
  *   Object matcher = new Object() {
  *     public boolean matches(Object item) {
  *       return ...;
  *     }
  *   };
  * </pre>
  * 
  * </li>
  * </ul>
  * </p>
  * 
  * <p>
  * <h2><a name="closures">Closures</a></h2>
  * In some cases <b>when</b> can be difficult to write. For example you want to assert that
  * throwable was thrown, but cannot use chained form of when, because method is static. You may then
  * wrap call inside Closure.
  * 
  * <pre>
  * &#064;Test
  * public void should_fail_if_malformed() {
  *   when($parseInt(&quot;12x3&quot;));
  *   thenThrown(NumberFormatException.class);
  * }
  * 
  * private static Closure $parseInt(final String string) {
  *   return new Closure() {
  *     public Integer invoke() {
  *       return Integer.parseInt(string);
  *     }
  *   };
  * }
  * </pre>
  * 
  * </p>
  * 
  * <p>
  * <h2><a name="basics">Macros</a></h2>
  * Macros can make you code even more concise.
  * 
  * <pre>
  * given(list = new ArrayList&lt;String&gt;());
  * givenTimes(5, list).add(&quot;element&quot;);
  * when(list.size());
  * thenReturned(5);
  * </pre>
  * 
  * </p>
  * 
  * <p>
  * <h2><a name="dummies">Dummies</a></h2>
  * Testory can initialize all test's fields at once.
  * 
  * <pre>
  * &#064;Before
  * public void before() {
  *   givenTest(this);
  * }
  * </pre>
  * 
  * This will inject dummies (unstubbable mocks) into fields of <code><b>test</b></code> .<br/>
  * <br/>
  * 
  * Injected dummy has following properties
  * <ul>
  * <li>{@link Object#toString()} is stubbed to return name of declared field</li>
  * <li>{@link Object#equals(Object)} is stubbed so dummy is equal only to itself</li>
  * <li>{@link Object#hashCode()} is stubbed to obey contract</li>
  * </ul>
  * 
  * Field is skipped (nothing is injected) if
  * <ul>
  * <li>field is not null</li>
  * <li>field is of primitive type</li>
  * <li>field is not declared in <code><b>test</b></code>'s class but in it's superclass</li>
  * </ul>
  * If field is of final class - injection fails unless class is one of
  * <ul>
  * <li>array - array with single dummy element</li>
  * <li>{@link String} - string equal to field's name</li>
  * <li>{@link Boolean} - {@link Boolean#valueOf(boolean) Boolean.valueOf(0)}</li>
  * <li>{@link Character} - {@link Character#valueOf(char) Character.valueOf((char)0)}</li>
  * <li>{@link Byte} - {@link Byte#valueOf(byte) Byte.valueOf((byte)0)}</li>
  * <li>{@link Short} - {@link Short#valueOf(short) Short.valueOf((short)0)}</li>
  * <li>{@link Integer} - {@link Integer#valueOf(int) Integer.valueOf(0)}</li>
  * <li>{@link Long} - {@link Long#valueOf(long) Long.valueOf(0)}</li>
  * <li>{@link Float} - {@link Float#valueOf(float) Float.valueOf(0)}</li>
  * <li>{@link Double} - {@link Double#valueOf(double) Double.valueOf(0)}</li>
  * <li>{@link Class} - some concrete class</li>
  * <li>{@link Method} - some method declared in dummy class</li>
  * <li>{@link Field} - some field declared in dummy class</li>
  * </ul>
  * </p>
  */
 public class TestoryTutorial {}
