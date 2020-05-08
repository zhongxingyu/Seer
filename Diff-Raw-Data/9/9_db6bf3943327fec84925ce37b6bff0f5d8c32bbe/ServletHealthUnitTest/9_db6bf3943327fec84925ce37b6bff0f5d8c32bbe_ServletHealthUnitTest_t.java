 /*****************************************************************************************
  * *** BEGIN LICENSE BLOCK *****
  *
  * Version: MPL 2.0
  *
  * echocat JeMoni, Copyright (c) 2012-2013 echocat
  *
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  *
  * *** END LICENSE BLOCK *****
  ****************************************************************************************/
 
 package org.echocat.jemoni.jmx.support;
 
 import org.echocat.jemoni.jmx.JmxRegistry;
 import org.echocat.jemoni.jmx.support.ServletHealth.*;
 import org.echocat.jomon.runtime.util.Duration;
 import org.echocat.jomon.runtime.util.Entry;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
 import org.junit.Test;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.web.context.WebApplicationContext;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.regex.Pattern;
 
 import static org.echocat.jemoni.jmx.JmxRegistry.getLocalInstance;
 import static org.echocat.jemoni.jmx.support.ServletHealth.*;
 import static org.echocat.jomon.testing.Assert.assertThat;
 import static org.echocat.jomon.testing.BaseMatchers.*;
 import static org.echocat.jomon.testing.BaseMatchers.isNotNull;
 import static org.echocat.jomon.testing.StringMatchers.contains;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.*;
 import static org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE;
 
 public class ServletHealthUnitTest {
 
     @Test
     public void testParseMapping() throws Exception {
         final ServletHealth health = new ServletHealth();
         health.setMapping("/f.o.*>foo\n.*\\.html>bar\n\n");
         final Iterator<Entry<Pattern, ScopeMapping>> i = health.iterator();
         assertThat(i.hasNext(), is(true));
         assertThat(i.next(), isEntry("/f.o.*", "foo"));
         assertThat(i.hasNext(), is(true));
         assertThat(i.next(), isEntry(".*\\.html", "bar"));
         assertThat(i.hasNext(), is(true));
         assertThat(i.next(), isEntry(null, null));
         assertThat(i.hasNext(), is(false));
     }
 
     @Test
     public void testHandleRequest() throws Exception {
         final ServletHealth health = new ServletHealth();
         health.setMapping("/f.o.*>foo\n.*\\.html>bar\n\n");
         execute(health, "/f0o.html", "10ms");
         assertThat(health.getMapping("foo").getRequestsPerSecond("foo"), isGreaterThan(0d));
         assertThat(health.getMapping("foo").getAverageRequestDuration("foo"), isGreaterThanOrEqualTo(10d));
         assertThat(health.getMapping("foo").getAverageRequestDuration("foo"), isLessThanOrEqualTo(20d));
         assertThat(health.getMapping("bar").getRequestsPerSecond("bar"), is(0d));
         assertThat(health.getMapping("bar").getAverageRequestDuration("bar"), is(0d));
         assertThat(health.getMapping(null).getRequestsPerSecond(null), isGreaterThan(0d));
         assertThat(health.getMapping(null).getAverageRequestDuration(null), isGreaterThanOrEqualTo(10d));
         assertThat(health.getMapping(null).getAverageRequestDuration(null), isLessThanOrEqualTo(20d));
         execute(health, "/f00.html", "100ms");
         assertThat(health.getMapping("foo").getRequestsPerSecond("foo"), isGreaterThan(0d));
         assertThat(health.getMapping("foo").getAverageRequestDuration("foo"), isGreaterThanOrEqualTo(10d));
         assertThat(health.getMapping("foo").getAverageRequestDuration("foo"), isLessThanOrEqualTo(20d));
         assertThat(health.getMapping("bar").getRequestsPerSecond("bar"), isGreaterThan(0d));
         assertThat(health.getMapping("bar").getAverageRequestDuration("bar"), isGreaterThanOrEqualTo(100d));
         assertThat(health.getMapping("bar").getAverageRequestDuration("bar"), isLessThanOrEqualTo(110d));
         assertThat(health.getMapping(null).getRequestsPerSecond(null), isGreaterThan(0d));
         assertThat(health.getMapping(null).getAverageRequestDuration(null), isGreaterThanOrEqualTo(40d));
         assertThat(health.getMapping(null).getAverageRequestDuration(null), isLessThanOrEqualTo(80d));
     }
 
     protected static void execute(@Nonnull ServletHealth health, @Nonnull String requestUri, @Nonnull String blockingDuration) throws IOException, ServletException {
         health.doFilter(request(requestUri), response(), chainThatBlocksFor(blockingDuration));
     }
 
     @Nonnull
     protected static HttpServletRequest request(@Nonnull String requestUri) {
         final HttpServletRequest request = mock(HttpServletRequest.class);
         doReturn(requestUri).when(request).getRequestURI();
         return request;
     }
 
     @Nonnull
     protected static HttpServletResponse response() {
         final HttpServletResponse response = mock(HttpServletResponse.class);
         return response;
     }
 
     @Nonnull
     protected static FilterChain chainThatBlocksFor(@Nonnull String duration) throws IOException, ServletException {
         return chainThatBlocksFor(new Duration(duration));
     }
 
     @Nonnull
     protected static FilterChain chainThatBlocksFor(@Nonnull final Duration duration) throws IOException, ServletException {
         final FilterChain chain = mock(FilterChain.class);
         doAnswer(new Answer() { @Override public Object answer(InvocationOnMock invocation) throws Throwable {
             duration.sleep();
             return null;
         }}).when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
         return chain;
     }
 
     @Test
     public void testInitRegistryByFilterConfig() throws Exception {
         try {
             new ServletHealth().init(filterConfig(null, REGISTRY_REF_INIT_ATTRIBUTE, "myRegistry"));
             fail("Expected exception missing");
         } catch (ServletException expected) {
             assertThat(expected.getMessage(), is("Could not find a spring context."));
         }
         try {
             new ServletHealth().init(filterConfig(applicationContext(), REGISTRY_REF_INIT_ATTRIBUTE, "myRegistry"));
             fail("Expected exception missing");
         } catch (ServletException expected) {
             assertThat(expected.getMessage(), contains("Could not find bean"));
         }
         final ServletHealth health = new ServletHealth();
         final JmxRegistry myRegistry = new JmxRegistry();
         assertThat(health.getRegistry(), isSameAs(getLocalInstance()));
         health.init(filterConfig(applicationContext("myRegistry", myRegistry), REGISTRY_REF_INIT_ATTRIBUTE, "myRegistry"));
         health.close();
         assertThat(health.getRegistry(), isSameAs(myRegistry));
     }
 
     @Test
     public void testInitInterceptorByFilterConfig() throws Exception {
         try {
             new ServletHealth().init(filterConfig(null, INTERCEPTOR_REF_INIT_ATTRIBUTE, "myInterceptor"));
             fail("Expected exception missing");
         } catch (ServletException expected) {
             assertThat(expected.getMessage(), is("Could not find a spring context."));
         }
         try {
             new ServletHealth().init(filterConfig(applicationContext(), INTERCEPTOR_REF_INIT_ATTRIBUTE, "myInterceptor"));
             fail("Expected exception missing");
         } catch (ServletException expected) {
             assertThat(expected.getMessage(), contains("Could not find bean"));
         }
         final ServletHealth health = new ServletHealth();
         final ServletHealthInterceptor myInterceptor = mock(ServletHealthInterceptor.class);
         assertThat(health.getInterceptor(), is(null));
         health.init(filterConfig(applicationContext("myInterceptor", myInterceptor), INTERCEPTOR_REF_INIT_ATTRIBUTE, "myInterceptor"));
         health.close();
         assertThat(health.getInterceptor(), isSameAs(myInterceptor));
         health.init(filterConfig(null, INTERCEPTOR_INIT_ATTRIBUTE, MyInterceptor.class.getName()));
         health.close();
         assertThat(health.getInterceptor(), isInstanceOf(MyInterceptor.class));
     }
 
     @Test
     public void testInitMappingByFilterConfig() throws Exception {
         final ServletHealth health1 = new ServletHealth();
         health1.init(filterConfig(null, MAPPING_INIT_ATTRIBUTE, null));
         health1.close();
         assertThat(health1.getMapping(), is(null));
 
         final ServletHealth health2 = new ServletHealth();
         health2.init(filterConfig(null, MAPPING_INIT_ATTRIBUTE, "/f.o>foo"));
         health2.close();
         assertThat(health2.getMapping(), isNotNull());
         assertThat(health2.getMappingFor("/foo").getDefaultName(), is("foo"));
     }
 
     @Nonnull
     protected static WebApplicationContext applicationContext(@Nullable Object... nameToBean) {
         final WebApplicationContext applicationContext = mock(WebApplicationContext.class);
         doThrow(new NoSuchBeanDefinitionException("foo")).when(applicationContext).getBean(anyString());
         if (nameToBean != null) {
             if (nameToBean.length % 2 != 0) {
                 throw new IllegalArgumentException("nameToBean have to be divideable by 2.");
             }
             for (int i = 0; i < nameToBean.length; i+= 2) {
                 doReturn(nameToBean[i + 1]).when(applicationContext).getBean((String) nameToBean[i]);
             }
         }
         return applicationContext;
     }
 
     @Nonnull
     protected static ServletContext servletContext(@Nullable WebApplicationContext applicationContext) {
         final ServletContext servletContext = mock(ServletContext.class);
         doReturn(applicationContext).when(servletContext).getAttribute(ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
         return servletContext;
     }
 
     @Nonnull
     protected static FilterConfig filterConfig(@Nullable WebApplicationContext applicationContext, @Nullable String... initParameterKeyToValues) {
         final FilterConfig filterConfig = mock(FilterConfig.class);
         doReturn(servletContext(applicationContext)).when(filterConfig).getServletContext();
         if (initParameterKeyToValues != null) {
             if (initParameterKeyToValues.length % 2 != 0) {
                 throw new IllegalArgumentException("initParameterKeyToValues have to be divideable by 2.");
             }
             for (int i = 0; i < initParameterKeyToValues.length; i+= 2) {
                 doReturn(initParameterKeyToValues[i + 1]).when(filterConfig).getInitParameter(initParameterKeyToValues[i]);
             }
         }
         return filterConfig;
     }
 
     @Nonnull
     protected static Matcher<Entry<Pattern, ScopeMapping>> isEntry(@Nullable final String pattern, @Nullable final String defaultName) {
        return new TypeSafeMatcher<Entry<Pattern,ScopeMapping>>() {
 
             @Override
             public boolean matchesSafely(Entry<Pattern, ScopeMapping> item) {
                 final boolean result;
                 if (item != null) {
                     final boolean patternMatches;
                     if (pattern != null) {
                         patternMatches = item.getKey() != null && pattern.equals(item.getKey().pattern());
                     } else {
                         patternMatches = item.getKey() == null;
                     }
                     if (patternMatches) {
                         if (defaultName != null) {
                             result = item.getValue() != null && defaultName.equals(item.getValue().getDefaultName());
                         } else {
                             result = item.getValue() != null && item.getValue().getDefaultName() == null;
                         }
                     } else {
                         result = false;
                     }
                 } else {
                     result = false;
                 }
                 return result;
 
             }
 
             @Override
             public void describeTo(Description description) {
                 description.appendText("is entry with pattern ").appendValue(pattern).appendText(" and default name ").appendValue(defaultName);
             }
 
             @Override
            protected void describeMismatchSafely(@Nullable Entry<Pattern, ScopeMapping> actual, @Nonnull Description description) {
                 if (actual != null) {
                     final String actualDefaultName = actual.getValue() != null ? actual.getValue().getDefaultName() : null;
                     description.appendText("entry with pattern ").appendValue(actual.getKey()).appendText(" and default name ").appendValue(actualDefaultName);
                 } else {
                     description.appendValue(null);
                 }
             }
         };
     }
 
     public static class MyInterceptor implements ServletHealthInterceptor {
 
         @Override
         public boolean isRecordAllowed(@Nonnull ServletRequest request, @Nonnull ScopeMapping globalMapping, @Nullable ScopeMapping specificMapping) {
             return false;
         }
 
         @Nullable
         @Override
         public String getSpecificTargetName(@Nonnull ServletRequest request, @Nonnull ScopeMapping specificMapping) {
             return null;
         }
 
         @Nullable
         @Override
         public Collection<String> getPossibleNames(@Nonnull String defaultName) {
             return null;
         }
     }
 
 }
