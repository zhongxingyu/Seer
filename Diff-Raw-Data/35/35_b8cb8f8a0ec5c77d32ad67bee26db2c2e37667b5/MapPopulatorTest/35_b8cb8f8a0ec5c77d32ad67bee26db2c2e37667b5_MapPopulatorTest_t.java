 /**
  * Copyright (c) 2008, Damian Carrillo
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice, this list of 
  *     conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
  *     conditions and the following disclaimer in the documentation and/or other materials 
  *     provided with the distribution.
  *   * Neither the name of the copyright holder's organization nor the names of its contributors 
  *     may be used to endorse or promote products derived from this software without specific 
  *     prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package co.cdev.agave.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;

 import javax.servlet.http.HttpServletRequest;

 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
import co.cdev.agave.internal.HandlerMethodDescriptorImpl.ParameterDescriptor;

 /**
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  */
 public class MapPopulatorTest {
     
     private Mockery context = new Mockery();
     
     private HttpServletRequest request;
     private HandlerMethodDescriptor descriptor;
     private URIPattern pattern;
     
     private Locale locale;
     private Map<String, String> uriParams;
     private Map<String, String[]> requestParams;
     private List<ParameterDescriptor> paramDescriptors;
 
     private MapPopulator populator;
     
     @Before
     public void setup() throws Exception {        
         request = context.mock(HttpServletRequest.class);
         descriptor = context.mock(HandlerMethodDescriptor.class);
         pattern = context.mock(URIPattern.class);
         
         locale = Locale.getDefault();
         uriParams = new HashMap<String, String>();
         requestParams = new HashMap<String, String[]>();
         paramDescriptors = new ArrayList<ParameterDescriptor>();
         
         context.checking(new Expectations() {{
             allowing(request).getLocale();
             will(returnValue(locale));
             
             allowing(request).getParameterMap();
             will(returnValue(requestParams));
             
             allowing(descriptor).getPattern(); 
             will(returnValue(pattern));
             
             allowing(descriptor).getParamDescriptors();
             will(returnValue(paramDescriptors));
             
             allowing(pattern).getParameterMap(with(any(HttpServletRequest.class))); 
             will(returnValue(uriParams));
         }});
         
         // The constructor is proactive, so it must be called after the context
         // has been set up.
         
         populator = new MapPopulatorImpl(request, descriptor);
     }
     
     @Test
     public void testCollectParameters_withURIParams() throws Exception {
         paramDescriptors.add(new ParameterDescriptor(String.class, "one"));
         paramDescriptors.add(new ParameterDescriptor(String.class, "two"));
         
         uriParams.put("one", "cat");
         uriParams.put("two", "possum");
         
         Map<String, Object> namedArguments = new HashMap<String, Object>();
         namedArguments.put("one", null);
         namedArguments.put("two", null);
         
         populator.populate(namedArguments);
         
         Assert.assertEquals("cat", namedArguments.get("one"));
         Assert.assertEquals("possum", namedArguments.get("two"));
     }
     
     @Test
     public void testCollectParameters_withRequestParams() throws Exception {
         paramDescriptors.add(new ParameterDescriptor(String.class, "one"));
         paramDescriptors.add(new ParameterDescriptor(String.class, "two"));
         
         requestParams.put("one", new String[] {"cat"});
         requestParams.put("two", new String[] {"possum"});
         
         Map<String, Object> namedArguments = new HashMap<String, Object>();
         namedArguments.put("one", null);
         namedArguments.put("two", null);
         
         populator.populate(namedArguments);
         
         Assert.assertEquals("cat", namedArguments.get("one"));
         Assert.assertEquals("possum", namedArguments.get("two"));
     }
     
     @Test
     public void testCollectParameters_withURIParamsOverridingRequestParams() throws Exception {
         paramDescriptors.add(new ParameterDescriptor(String.class, "one"));
         paramDescriptors.add(new ParameterDescriptor(String.class, "two"));
         
         uriParams.put("one", "cat");
         uriParams.put("two", "possum");
         
         requestParams.put("one", new String[] {"dog"});
         requestParams.put("two", new String[] {"raccoon"});
         
         Map<String, Object> namedArguments = new HashMap<String, Object>();
         namedArguments.put("one", null);
         namedArguments.put("two", null);
         
         populator.populate(namedArguments);
         
         Assert.assertEquals("cat", namedArguments.get("one"));
         Assert.assertEquals("possum", namedArguments.get("two"));
     }
     
     @Test
     public void testCollectParameters_withImplicitConvertedURIParams() throws Exception {
         paramDescriptors.add(new ParameterDescriptor(Integer.class, "one"));
         paramDescriptors.add(new ParameterDescriptor(Double.class, "two"));
         
         uriParams.put("one", "1");
         uriParams.put("two", "2.0");
         
         Map<String, Object> namedArguments = new HashMap<String, Object>();
         namedArguments.put("one", null);
         namedArguments.put("two", null);
         
         populator.populate(namedArguments);
         
         Assert.assertEquals(Integer.valueOf(1), namedArguments.get("one"));
         Assert.assertEquals(Double.valueOf(2.0), namedArguments.get("two"));
     }
     
     @Test
     public void testCollectParameters_withImplicitConvertedRequestParams() throws Exception {
         paramDescriptors.add(new ParameterDescriptor(Integer.class, "one"));
         paramDescriptors.add(new ParameterDescriptor(Double.class, "two"));
         
         requestParams.put("one", new String[] {"1"});
         requestParams.put("two", new String[] {"2.0"});
         
         Map<String, Object> namedArguments = new HashMap<String, Object>();
         namedArguments.put("one", null);
         namedArguments.put("two", null);
         
         populator.populate(namedArguments);
         
         Assert.assertEquals(Integer.valueOf(1), namedArguments.get("one"));
         Assert.assertEquals(Double.valueOf(2.0), namedArguments.get("two"));
     }
     
    @Test
    public void testPopulate_withNullRequestParameters() throws Exception {
        paramDescriptors.add(new ParameterDescriptor(Integer.class, "one"));
        paramDescriptors.add(new ParameterDescriptor(Double.class, "two"));

        requestParams.put("one", null);
        requestParams.put("two", null);

        Map<String, Object> namedArguments = new HashMap<String, Object>();
        namedArguments.put("one", null);
        namedArguments.put("two", null);

        populator.populate(namedArguments);
    }

 }
