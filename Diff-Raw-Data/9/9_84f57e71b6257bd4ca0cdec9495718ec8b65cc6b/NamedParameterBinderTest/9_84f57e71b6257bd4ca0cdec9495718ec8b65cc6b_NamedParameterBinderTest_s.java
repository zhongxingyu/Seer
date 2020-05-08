 package org.polyforms.repository.jpa.binder;
 
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Parameter;
 import javax.persistence.Query;
 
 import org.easymock.EasyMock;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.test.util.ReflectionTestUtils;
 
 public class NamedParameterBinderTest {
     private NamedParameterBinder parameterBinder;
 
     @Before
     public void setUp() {
         parameterBinder = new NamedParameterBinder();
     }
 
     @Test
     public void bindNamedParametersByType() throws NoSuchMethodException {
         final Method method = String.class.getMethod("startsWith", new Class<?>[] { String.class });
         final Query query = EasyMock.createMock(Query.class);
         final Parameter<?> parameter = EasyMock.createMock(Parameter.class);
         final Set<Parameter<?>> parameters = new HashSet<Parameter<?>>();
         parameters.add(parameter);
 
         parameter.getParameterType();
        EasyMock.expectLastCall().andReturn(Object.class);
         parameter.getName();
         EasyMock.expectLastCall().andReturn("name");
         query.setParameter("name", "tony");
         EasyMock.expectLastCall().andReturn(query);
         query.setParameter("name", "tong");
         EasyMock.expectLastCall().andReturn(query);
         EasyMock.replay(query, parameter);
 
         parameterBinder.bind(query, method, parameters, new Object[] { "tony" });
         // Just for testing cache
         parameterBinder.bind(query, method, parameters, new Object[] { "tong" });
         EasyMock.verify(query, parameter);
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void parameterMatchers() {
         final List<ParameterMatcher<String>> parameterMatchers = (List<ParameterMatcher<String>>) ReflectionTestUtils
                 .getField(parameterBinder, "parameterMatchers");
         Assert.assertEquals(2, parameterMatchers.size());
         Assert.assertTrue(parameterMatchers.get(0) instanceof TypedParameterMatcher);
         Assert.assertTrue(parameterMatchers.get(1) instanceof NamedParameterMatcher);
     }
 
     @Test
     public void setParameter() {
         final Query query = EasyMock.createMock(Query.class);
         query.setParameter("code", "value");
         EasyMock.expectLastCall().andReturn(query);
         EasyMock.replay(query);
 
         parameterBinder.setParameter(query, "code", "value");
         EasyMock.verify(query);
 
     }
 
     @Test
     public void getKey() {
         final Parameter<?> parameter = EasyMock.createMock(Parameter.class);
         parameter.getName();
         EasyMock.expectLastCall().andReturn("code");
         EasyMock.replay(parameter);
 
         Assert.assertEquals("code", parameterBinder.getKey(parameter));
         EasyMock.verify(parameter);
     }
 }
