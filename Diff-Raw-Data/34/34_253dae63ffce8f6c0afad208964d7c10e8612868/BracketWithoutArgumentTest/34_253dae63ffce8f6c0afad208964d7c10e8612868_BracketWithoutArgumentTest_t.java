 package jp.skypencil.pmd.slf4j;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.junit.Test;
 
 import net.sourceforge.pmd.Rule;
 
 public class BracketWithoutArgumentTest extends ExampleBasedTest {
 
 	@Override
 	protected Rule createRule() {
 		return new NumberOfPlaceholderShouldBeEqualToNumberOfArgument();
 	}
 
 	@Override
 	protected Collection<String> getExpectedExampleNames() {
 		return Arrays.asList(new String[]{
 				"UsingFormatWithoutArgument",
 				"UsingFormatWithNotEnoughArgument",
 				"UsingFormatWithTooLongArray",
 				"UsingFormatWithTooManyArgument",
 				"UsingFormatWithoutArgumentButWithException",
 				"UsingFormatWithTooLongArrayAndException",
 				"UsingSeparatedFormat"	// currently we cannot support this pattern
 		});
 	}
 
 	@Test
 	public void testCountDelimiter() {
 		assertThat(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().countDelimiter(""), is(0));
 		assertThat(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().countDelimiter("{}"), is(1));
 		assertThat(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().countDelimiter("a{}"), is(1));
 		assertThat(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().countDelimiter("{}a"), is(1));
 		assertThat(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().countDelimiter("a{}a"), is(1));
 		assertThat(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().countDelimiter("{}{}"), is(2));
 		assertThat(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().countDelimiter("\\{}"), is(0));
 		assertThat(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().countDelimiter("\\\\{}"), is(1));
 	}
 
 	@Test
 	public void testIsThrowable() {
 		assertTrue(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().isThrowable(Throwable.class));
 		assertTrue(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().isThrowable(Exception.class));
 		assertTrue(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().isThrowable(RuntimeException.class));
 		assertTrue(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().isThrowable(NullPointerException.class));
		assertTrue(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().isThrowable(null));
 		assertFalse(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().isThrowable(Object.class));
 		assertFalse(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().isThrowable(Object[].class));
 		assertFalse(new NumberOfPlaceholderShouldBeEqualToNumberOfArgument().isThrowable(String[].class));
 	}
 }
