 package com.github.spring.mvc.util.handler;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.when;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class URIMatchingHandlerInterceptorAdapterTest {
 
     private static final String URI = "/uri";
     private static final String ALL = "/*";
     private static final String NESTED_URI = "/test/uri";
     private static final String NESTED_MATCHER = "/test/*";
     private static final String INVALID_MATCHER = "invalid";
     private static final String NON_MATCHING_NESTED_PATH_URI = "/test2/uri";
    private static final String SUFFIX_URI = "/uri.html";
     private static final String SUFFIX_MATCHER = "*.html";
 
     private TestURIMatchingHandlerInterceptor handlerInterceptor;
     private TestAnnotationURIMatchingHandlerInterceptor annotationHandlerInterceptor;
 
     @Mock
     private HttpServletRequest request;
 
     @Mock
     private HttpServletResponse response;
 
     @Before
     public void setUp() throws Exception {
         handlerInterceptor = new TestURIMatchingHandlerInterceptor();
         annotationHandlerInterceptor = new TestAnnotationURIMatchingHandlerInterceptor();
     }
 
     @Test
     public void testPreHandle() throws Exception {
         when(request.getServletPath()).thenReturn(URI);
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleWithInvalidExclude() throws Exception {
         when(request.getServletPath()).thenReturn(URI);
         handlerInterceptor.setExcludes(new String[] { INVALID_MATCHER });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleWithMatchingExclude() throws Exception {
         when(request.getServletPath()).thenReturn(URI);
         handlerInterceptor.setExcludes(new String[] { URI });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertFalse(result);
     }
 
     @Test
     public void testPreHandleWithExcludeAll() throws Exception {
         when(request.getServletPath()).thenReturn(URI);
         handlerInterceptor.setExcludes(new String[] { ALL });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertFalse(result);
     }
 
     @Test
     public void testPreHandleWithPathExclude() throws Exception {
         when(request.getServletPath()).thenReturn(NESTED_URI);
         handlerInterceptor.setExcludes(new String[] { NESTED_MATCHER });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertFalse(result);
     }
 
     @Test
     public void testPreHandleWithNonMatchingPathExclude() throws Exception {
         when(request.getServletPath()).thenReturn(NON_MATCHING_NESTED_PATH_URI);
         handlerInterceptor.setExcludes(new String[] { NESTED_MATCHER });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleWithSuffixExclude() throws Exception {
         when(request.getServletPath()).thenReturn(SUFFIX_URI);
         handlerInterceptor.setExcludes(new String[] { SUFFIX_MATCHER });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
        assertFalse(result);
     }
 
     @Test
     public void testPreHandleWithInvalidInclude() throws Exception {
         when(request.getServletPath()).thenReturn(URI);
         handlerInterceptor.setIncludes(new String[] { INVALID_MATCHER });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleWithMatchingInclude() throws Exception {
         when(request.getServletPath()).thenReturn(URI);
         handlerInterceptor.setIncludes(new String[] { URI });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleWithIncludeAll() throws Exception {
         when(request.getServletPath()).thenReturn(URI);
         handlerInterceptor.setIncludes(new String[] { ALL });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleWithPathInclude() throws Exception {
         when(request.getServletPath()).thenReturn(NESTED_URI);
         handlerInterceptor.setIncludes(new String[] { NESTED_MATCHER });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleWithNonMatchingPathInclude() throws Exception {
         when(request.getServletPath()).thenReturn(NON_MATCHING_NESTED_PATH_URI);
         handlerInterceptor.setIncludes(new String[] { NESTED_MATCHER });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleWithSuffixInclude() throws Exception {
         when(request.getServletPath()).thenReturn(SUFFIX_URI);
         handlerInterceptor.setIncludes(new String[] { SUFFIX_MATCHER });
 
         boolean result = handlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleIncludeWithAnnotations() throws Exception {
         when(request.getServletPath()).thenReturn("/include");
 
         boolean result = annotationHandlerInterceptor.preHandle(request, response, this);
 
         assertTrue(result);
     }
 
     @Test
     public void testPreHandleExcludeWithAnnotations() throws Exception {
         when(request.getServletPath()).thenReturn("/exclude");
 
         boolean result = annotationHandlerInterceptor.preHandle(request, response, this);
 
         assertFalse(result);
     }
 
 }
