 package com.github.t1.webresource.log;
 
 import static com.github.t1.webresource.log.LogLevel.*;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.lang.reflect.Method;
 
 import javax.interceptor.InvocationContext;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.*;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.Answer;
 import org.slf4j.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class LoggingInterceptorTest {
     @InjectMocks
     LoggingInterceptor interceptor = new LoggingInterceptor() {
         @Override
         Logger getLogger(java.lang.Class<?> type) {
             LoggingInterceptorTest.this.loggerType = type;
             return logger;
         };
     };
     @Mock
     InvocationContext context;
     @Mock
     Logger logger;
     Class<?> loggerType;
 
     private void whenDebugEnabled() {
         when(logger.isDebugEnabled()).thenReturn(true);
     }
 
     private void whenMethod(Method method, Object... args) throws ReflectiveOperationException {
         when(context.getTarget()).thenReturn("dummy");
         when(context.getMethod()).thenReturn(method);
         when(context.getParameters()).thenReturn(args);
     }
 
     @Test
     public void shouldLogALongMethodNameWithSpaces() throws Exception {
         class Container {
             @Logged
             public void methodWithALongName() {}
         }
         whenDebugEnabled();
         whenMethod(Container.class.getMethod("methodWithALongName"));
 
         interceptor.aroundInvoke(context);
 
         verify(logger).debug("method with a long name", new Object[0]);
     }
 
     @Test
     public void shouldLogAnAnnotatedMethod() throws Exception {
         class Container {
             @Logged("bar")
             public void foo() {}
         }
         whenDebugEnabled();
         whenMethod(Container.class.getMethod("foo"));
 
         interceptor.aroundInvoke(context);
 
         verify(logger).debug("bar", new Object[0]);
     }
 
     @Test
     public void shouldLogReturnValue() throws Exception {
         class Container {
             @Logged
             public boolean methodWithReturnType() {
                 return true;
             }
         }
         whenDebugEnabled();
         whenMethod(Container.class.getMethod("methodWithReturnType"));
         when(context.proceed()).thenReturn(true);
 
         interceptor.aroundInvoke(context);
 
         verify(logger).debug("returns {}", new Object[] { true });
     }
 
     @Test
     public void shouldLogException() throws Exception {
         class Container {
             @Logged
             public boolean methodThatMightFail() {
                 return true;
             }
         }
         whenDebugEnabled();
         whenMethod(Container.class.getMethod("methodThatMightFail"));
         RuntimeException exception = new RuntimeException("foo");
         when(context.proceed()).thenThrow(exception);
 
         try {
             interceptor.aroundInvoke(context);
             fail("RuntimeException expected");
         } catch (RuntimeException e) {
             // that's okay
         }
         verify(logger).debug("failed", exception);
     }
 
     @Test
     public void shouldNotLogVoidReturnValue() throws Exception {
         class Container {
             @Logged
             public void voidReturnType() {}
         }
         whenDebugEnabled();
         whenMethod(Container.class.getMethod("voidReturnType"));
 
         interceptor.aroundInvoke(context);
 
         verify(logger).debug("void return type", new Object[0]);
         verify(logger, atLeast(0)).isDebugEnabled();
         verifyNoMoreInteractions(logger);
     }
 
     @Test
     public void shouldLogIntParameter() throws Exception {
         class Container {
             @Logged
             public void methodWithIntArgument(int i) {}
         }
         whenDebugEnabled();
         whenMethod(Container.class.getMethod("methodWithIntArgument", int.class), 3);
 
         interceptor.aroundInvoke(context);
 
         verify(logger).debug("method with int argument", new Object[] { 3 });
     }
 
     @Test
     public void shouldLogIntegerParameter() throws Exception {
         class Container {
             @Logged
             public void methodWithIntegerArgument(Integer i) {}
         }
         whenDebugEnabled();
         whenMethod(Container.class.getMethod("methodWithIntegerArgument", Integer.class), 3);
 
         interceptor.aroundInvoke(context);
 
         verify(logger).debug("method with integer argument", new Object[] { 3 });
     }
 
     @Test
     public void shouldLogTwoParameters() throws Exception {
         class Container {
             @Logged
             public void methodWithTwoParameters(String one, String two) {}
         }
         whenDebugEnabled();
         Method method = Container.class.getMethod("methodWithTwoParameters", String.class, String.class);
         whenMethod(method, "foo", "bar");
 
         interceptor.aroundInvoke(context);
 
         verify(logger).debug("method with two parameters", new Object[] { "foo", "bar" });
     }
 
     @Test
     public void shouldNotLogWhenOff() throws Exception {
         class Container {
             @Logged(level = OFF)
             public void atOff() {}
         }
         whenDebugEnabled();
         whenMethod(Container.class.getMethod("atOff"));
 
         interceptor.aroundInvoke(context);
 
         verifyNoMoreInteractions(logger);
     }
 
     @Test
     public void shouldNotLogWhenDebugIsNotEnabled() throws Exception {
         class Container {
             @Logged(level = DEBUG)
             public void atDebug() {}
         }
         whenMethod(Container.class.getMethod("atDebug"));
 
         interceptor.aroundInvoke(context);
 
         verify(logger, atLeast(0)).isDebugEnabled();
         verifyNoMoreInteractions(logger);
     }
 
     @Test
     public void shouldLogInfoWhenInfoIsEnabled() throws Exception {
         class Container {
             @Logged(level = INFO)
             public void atInfo() {}
         }
         whenDebugEnabled();
         when(logger.isInfoEnabled()).thenReturn(true);
         whenMethod(Container.class.getMethod("atInfo"));
 
         interceptor.aroundInvoke(context);
 
         verify(logger).info("at info", new Object[0]);
     }
 
     @Test
     public void shouldLogExplicitClass() throws Exception {
         class Container {
             @Logged(logger = Integer.class)
             public void foo() {}
         }
         whenDebugEnabled();
         whenMethod(Container.class.getMethod("foo"));
 
         interceptor.aroundInvoke(context);
 
         assertEquals(Integer.class, loggerType);
     }
 
     @Test
     public void shouldLogLogContextParameter() throws Exception {
         final String KEY = "user-id";
         class Container {
             @Logged
             public void methodWithLogContextParameter(@LogContext(KEY) String one, String two) {}
         }
         whenDebugEnabled();
         Method method = Container.class.getMethod("methodWithLogContextParameter", String.class, String.class);
         whenMethod(method, "foo", "bar");
         final String[] userId = new String[1];
         when(context.proceed()).thenAnswer(new Answer<Void>() {
             @Override
             public Void answer(InvocationOnMock invocation) throws Throwable {
                 userId[0] = MDC.get(KEY);
                 return null;
             }
         });
 
        assertNull(MDC.get(KEY));
         interceptor.aroundInvoke(context);
        assertNull(MDC.get(KEY));
 
         verify(logger).debug("method with log context parameter", new Object[] { "foo", "bar" });
         assertEquals("foo", userId[0]);
     }
 }
