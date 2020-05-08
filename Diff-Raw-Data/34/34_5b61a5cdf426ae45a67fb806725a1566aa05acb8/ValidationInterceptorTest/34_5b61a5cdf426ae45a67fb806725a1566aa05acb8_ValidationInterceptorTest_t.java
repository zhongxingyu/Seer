 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cs.wintoosa.controller.interceptor;
 
 import cs.wintoosa.AbstractTest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author jonimake
  */
 public class ValidationInterceptorTest extends AbstractTest {
     
 
     /**
      * Test of preHandle method, of class ValidationInterceptor.
      */
     @Test
     public void testPreHandleWithValidJsonPut() throws Exception {
         System.out.println("testPreHandleWithValidJsonPut");
         
         String jsondata = "{\"lat\":1.0,\"lon\":2.0,\"alt\":0.0,\"phoneId\":\"123456789012345\",\"timestamp\":1361264436365,\"checksum\":\"2413a9ab3dc40a4a0de28316422f321c4bcd179a\"}";
         
         MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/log/gps");
         request.setContent(jsondata.getBytes());
         request.setQueryString(jsondata);
         HttpServletResponse response = new MockHttpServletResponse();
         Object handler = null;
         ValidationInterceptor instance = new ValidationInterceptor();
         
         boolean expResult = true;
         boolean result = instance.preHandle(request, response, handler);
         assertEquals(expResult, result);
     }
     
     @Test
     public void testPreHandleWithInvalidJson() throws Exception {
         System.out.println("testPreHandleWithInvalidJson");
         
         //lon is wrong now, 2.1 instead of 2.0
         String jsondata = "{\"lat\":1.0,\"lon\":2.1,\"alt\":0.0,\"phoneId\":\"123456789012345\",\"timestamp\":1361264436365,\"checksum\":\"2413a9ab3dc40a4a0de28316422f321c4bcd179a\"}";
         
         MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/log/gps");
         request.setQueryString(jsondata);
         request.setContent(jsondata.getBytes());
         HttpServletResponse response = new MockHttpServletResponse();
         Object handler = null;
         ValidationInterceptor instance = new ValidationInterceptor();
         
         boolean expResult = false;
         boolean result = instance.preHandle(request, response, handler);
         assertEquals(expResult, result);
     }
     
     @Test
     public void testPreHandleWithMissingChecksum() throws Exception {
         System.out.println("testPreHandleWithInvalidJson");
         
         //lon is wrong now, 2.1 instead of 2.0
         String jsondata = "{\"lat\":1.0,\"lon\":2.1,\"alt\":0.0,\"phoneId\":\"123456789012345\",\"timestamp\":1361264436365}";
         
         MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/log/gps");
         request.setQueryString(jsondata);
         request.setContent(jsondata.getBytes());
         HttpServletResponse response = new MockHttpServletResponse();
         Object handler = null;
         ValidationInterceptor instance = new ValidationInterceptor();
         
         boolean expResult = false;
         boolean result = instance.preHandle(request, response, handler);
         assertEquals(expResult, result);
     }
     
     @Test
     public void testPreHandleGet() throws Exception {
         System.out.println("testPreHandleWithInvalidJson");
         
         //lon is wrong now, 2.1 instead of 2.0
         String jsondata = "{\"lat\":1.0,\"lon\":2.1,\"alt\":0.0,\"phoneId\":\"123456789012345\",\"timestamp\":1361264436365}";
         
         MockHttpServletRequest request = new MockHttpServletRequest("GET", "/log/");
         request.setQueryString(jsondata);
         request.setContent(jsondata.getBytes());
         HttpServletResponse response = new MockHttpServletResponse();
         Object handler = null;
         ValidationInterceptor instance = new ValidationInterceptor();
         
         boolean expResult = true;
         boolean result = instance.preHandle(request, response, handler);
         assertEquals(expResult, result);
     }
     
     @Test
     public void testPreHandleWithMissingJson() throws Exception {
         System.out.println("testPreHandleWithMissingJson");
         
         //lon is wrong now, 2.1 instead of 2.0
         String jsondata = null;//"{\"lat\":1.0,\"lon\":2.1,\"alt\":0.0,\"phoneId\":\"123456789012345\",\"timestamp\":1361264436365,\"checksum\":\"2413a9ab3dc40a4a0de28316422f321c4bcd179a\"}";
         
         MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/log/gps");
 //        request.setQueryString(jsondata);
   //      request.setContent(jsondata.getBytes());
         HttpServletResponse response = new MockHttpServletResponse();
         Object handler = null;
         ValidationInterceptor instance = new ValidationInterceptor();
         
         boolean expResult = false;
         boolean result = instance.preHandle(request, response, handler);
         assertEquals(expResult, result);
     }
    
    @Test
    public void testPostHandleWithValidJsonPut() throws Exception {
        System.out.println("testPostHandleWithValidJsonPut");
        
        String jsondata = "{\"lat\":1.0,\"lon\":2.0,\"alt\":0.0,\"phoneId\":\"123456789012345\",\"timestamp\":1361264436365,\"checksum\":\"2413a9ab3dc40a4a0de28316422f321c4bcd179a\"}";
        
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/log/gps");
        request.setContent(jsondata.getBytes());
        request.setQueryString(jsondata);
        HttpServletResponse response = new MockHttpServletResponse();
        Object handler = null;
        ValidationInterceptor instance = new ValidationInterceptor();
        
        boolean expResult = true;
        instance.postHandle(request, response, handler, null);
    }
 }
