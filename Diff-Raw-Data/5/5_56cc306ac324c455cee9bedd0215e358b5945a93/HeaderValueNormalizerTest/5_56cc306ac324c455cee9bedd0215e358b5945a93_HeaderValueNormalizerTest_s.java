 package com.wesabe.servlet.normalizers.tests;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 
 import com.wesabe.servlet.normalizers.HeaderValueNormalizer;
 import com.wesabe.servlet.normalizers.ValidationException;
 
 @RunWith(Enclosed.class)
 public class HeaderValueNormalizerTest {
 	public static class Normalizing_Valid_Header_Values {
 		private final HeaderValueNormalizer normalizer = new HeaderValueNormalizer();
 		
 		@Test
 		public void itPassesTheValueThrough() throws Exception {
 			assertThat(normalizer.normalize("woo"), is("woo"));
 		}
 		
 		@Test
 		public void itAllowsDoubleQuotes() throws Exception {
			System.err.println(normalizer.normalize("Basic realm=\"Wesabe API\""));
 		}
 	}
 	
 	public static class Normalizing_Invalid_Header_Values {
 		private final HeaderValueNormalizer normalizer = new HeaderValueNormalizer();
 		
 		@Test
 		public void itThrowsAValidationException() throws Exception {
 			try {
 				normalizer.normalize("wo\0o");
 				fail("should have thrown a ValidationException but didn't");
 			} catch (ValidationException e) {
 				assertThat(e.getMessage(), is("Invalid value: wo\0o (not a valid HTTP header value)"));
 			}
 		}
 	}
 	
 	public static class Normalizing_Null_Header_Values {
 		private final HeaderValueNormalizer normalizer = new HeaderValueNormalizer();
 		
 		@Test
 		public void itAllowsNullValues() throws Exception {
 			assertThat(normalizer.normalize(null), is(nullValue()));
 		}
 	}
 }
