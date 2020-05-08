 package com.github.argast.guice.matchers;
 
 import static com.github.argast.guice.matchers.GuiceServletMatchers.assertFilter;
 import static com.github.argast.guice.matchers.GuiceServletMatchers.assertServlet;
 import static com.github.argast.guice.matchers.GuiceServletMatchers.containsBinding;
 import static com.github.argast.guice.matchers.GuiceServletMatchers.forServlet;
 import static com.github.argast.guice.matchers.GuiceServletMatchers.serving;
 import static com.github.argast.guice.matchers.GuiceServletMatchers.servingPattern;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.servlet.GuiceFilter;
 import com.google.inject.servlet.ServletModule;
 
 @SuppressWarnings("serial")
 public class ServletModuleIntegrationTest {
 
 	private static final Map<String, String> PARAMS = new HashMap<String, String>() {{
 		put("key1", "value1");
 		put("key2", "value2");
 	}};
 	
 	private static class UriFilter implements Filter {
 		public void destroy() {}
 		public void doFilter(ServletRequest request, ServletResponse response,
 				FilterChain chain) throws IOException, ServletException {}
 		public void init(FilterConfig filterConfig) throws ServletException {}
 	}
 	
 	private static class RegexFilter implements Filter {
 		public void destroy() {}
 		public void doFilter(ServletRequest request, ServletResponse response,
 				FilterChain chain) throws IOException, ServletException {}
 		public void init(FilterConfig filterConfig) throws ServletException {}
 	}
 	
 	
 	@SuppressWarnings("serial")
 	private static class UriServlet extends HttpServlet {}
 	private static class RegexServlet extends HttpServlet {}
 	
 	private Injector injector = Guice.createInjector(new ServletModule() {
 		protected void configureServlets() {
 			bind(UriServlet.class).asEagerSingleton();
 			serve("/test/*", "/second/test/*").with(UriServlet.class, PARAMS);
 			
 			bind(UriFilter.class).asEagerSingleton();
 			filter("/to/filter/*").through(UriFilter.class, Collections.singletonMap("filterKey", "filterValue"));
 			
 			bind(RegexServlet.class).asEagerSingleton();
 			serveRegex("/regex/(test|impl)/[abc]*/.*.html").with(RegexServlet.class);
 			
 			bind(RegexFilter.class).asEagerSingleton();
 			filterRegex("/regex/(test|impl)/[def]*/.*.html").through(RegexFilter.class);
 		};
 	});
 	
 	@Before
 	public void setUp() throws Exception {
 		// init guice filter to force loading of servlets and filters
 		injector.getInstance(GuiceFilter.class).init(new DummyFilterConfig());
 	}
 	
 	@Test
 	public void testThatUriIsServed() throws Exception {
 		assertServlet(UriServlet.class).serves("/test/uri/*").on(injector);
 	}
 	
 	@Test
 	public void testThatUriIsServedUsingAssertThatSyntax() throws Exception {
 		assertThat(injector, containsBinding(forServlet(UriServlet.class), serving("/test/uri/*")));
 	}
 	
 	@Test(expected = AssertionError.class)
 	public void testThatAssertionErrorIsThrownWhenUriIsNotServed() throws Exception {
 		assertServlet(UriServlet.class).serves("/incorrect/uri/").on(injector);
 	}
 	
	@Test(expected = AssertionError.class)
	public void testThatAssertionErrorIsThrownWhenUriIsIncorrectUsingAssertThatSyntax() throws Exception {
		assertThat(injector, containsBinding(forServlet(UriServlet.class), servingPattern("/incorrect/*")));
	}
	
 	@Test
 	public void testThatSecondUriIsServed() throws Exception {
 		assertServlet(UriServlet.class).serves("/second/test/uri/*").on(injector);
 	}
 	
 	@Test
 	public void testThatSecondUriIsServedUsingAssertThatSyntax() throws Exception {
 		assertThat(injector, containsBinding(forServlet(UriServlet.class), serving("/second/test/uri/*")));
 	}	
 	
 	@Test
 	public void testThatPatternIsServed() throws Exception {
 		assertServlet(UriServlet.class).servesPattern("/test/*").on(injector);
 	}
 	
 	@Test(expected = AssertionError.class)
 	public void testThatAssertionErrorIsThrownWhenIncorrectPatternIsAsserted() throws Exception {
 		assertServlet(UriServlet.class).servesPattern("/incorrect/pattern/*").on(injector);
 	}
 
 	@Test
 	public void testThatPatternIsServedUsingAssertThatSyntax() throws Exception {
 		assertThat(injector, containsBinding(forServlet(UriServlet.class), servingPattern("/test/*")));
 	}
 	
 	@Test
 	public void testThatSecondPatternIsServed() throws Exception {
 		assertServlet(UriServlet.class).servesPattern("/second/test/*").on(injector);
 	}
 	
 	@Test
 	public void testThatSecondPatternIsServedUsingAssertThatSyntax() throws Exception {
 		assertThat(injector, containsBinding(forServlet(UriServlet.class), servingPattern("/second/test/*")));
 	}	
 	
 	@Test
 	public void testThatUriIsFiltered() throws Exception {
 		assertFilter(UriFilter.class).filters("/to/filter/*").on(injector);
 	}
 	
 	@Test(expected = AssertionError.class)
 	public void testThatAssertionErrorIsThrownWhenIncorrectUriIsAssertedForFilter() throws Exception {
 		assertFilter(UriFilter.class).filters("/incorrect/uri/*").on(injector);
 	}
 	
 	@Test
 	public void testThatServletIsInitializedWithParam() throws Exception {
 		assertServlet(UriServlet.class).hasInitParameter("key1", "value1").on(injector);
 	}
 	
 	@Test
 	public void testThatServletIsInitializedWithParams() throws Exception {
 		assertServlet(UriServlet.class).hasInitParameters(PARAMS).on(injector);
 	}
 
 	@Test(expected = AssertionError.class)
 	public void testThatServletIsNotInitializedWithParams() throws Exception {
 		assertServlet(UriServlet.class).hasInitParameters(Collections.singletonMap("key1", "value1")).on(injector);
 	}
 	
 	@Test
 	public void testThatFilterIsInitializedWithParameter() throws Exception {
 		assertFilter(UriFilter.class).hasInitParameter("filterKey", "filterValue").on(injector);
 	}
 	
 	@Test(expected = AssertionError.class)
 	public void testThatAssertionErrorIsThrownIfTestChecksForNonExistingParameter() throws Exception {
 		assertFilter(UriFilter.class).hasInitParameter("noSuchKey", "noSuchValue").on(injector);
 	}
 	
 	@Test
 	public void testThatServletServesRegex() throws Exception {
 		assertServlet(RegexServlet.class).serves("/regex/test/abcacb/index.html").on(injector);
 	}
 	
 	@Test(expected = AssertionError.class)
 	public void testThatAssertionErrorIsThrownWhenServletDoesNotServeRegexBecauseExtensionIsDifferent() throws Exception {
 		assertServlet(RegexServlet.class).serves("/regex/test/abc/index.xml").on(injector);
 	}
 	
 	@Test
 	public void testThatFilterFiltersRegex() throws Exception {
 		assertFilter(RegexFilter.class).filters("/regex/test/def/index.html").on(injector);
 	}
 	
 	@Test(expected = AssertionError.class)
 	public void testThatAssertionErrorIsThrownWhenFilterDoesNotFilterRegexBecauseExtensionIsDifferent() throws Exception {
 		assertFilter(RegexFilter.class).filters("/regex/test/def/index.xml").on(injector);
 	}	
 
 	@Test
 	public void testThatFilterFiltersPattern() throws Exception {
 		assertFilter(UriFilter.class).filtersPattern("/to/filter/*").on(injector);
 	}
 	
 	@Test(expected = AssertionError.class)
 	public void testThatAssertionErrorIsThrownWhenIfPatternIsIncorrect() throws Exception {
 		assertFilter(UriFilter.class).filtersPattern("/incorrect/path/*").on(injector);
 	}
 	
 	private final class DummyFilterConfig implements FilterConfig {
 		public ServletContext getServletContext() {
 			return null;
 		}
 
 		public Enumeration getInitParameterNames() {
 			return null;
 		}
 
 		public String getInitParameter(String name) {
 			return null;
 		}
 
 		public String getFilterName() {
 			return "guice-filter";
 		}
 	}
 }
