 package org.nju.artemis.aejb.component;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.invocation.Interceptor;
 import org.jboss.invocation.InterceptorContext;
 import org.jboss.logging.Logger;
 
 /**
  * @author <a href="wangjue1199@gmail.com">Jason</a>
  */
 public class ContainerInvocationHandler implements InvocationHandler {
 	Logger log = Logger.getLogger(ContainerInvocationHandler.class);
 	
 	private final Map<String,Object> contextData;
     private final List<Interceptor> interceptors;
     
 	public ContainerInvocationHandler(final String appName, final String moduleName, final String distinctName, final String beanName, final Class<?> viewClass, boolean stateful, AcContainer container) {
 		final String aejbName = moduleName + "/" + beanName;
 		// Initialize context data
 		contextData = new HashMap<String,Object>();
 		contextData.put("appName", appName);
 		contextData.put("moduleName", moduleName);
 		contextData.put("distinctName", distinctName);
 		contextData.put("beanName", beanName);
 		contextData.put("viewClass", viewClass);
 		contextData.put("stateful", stateful);
 		contextData.put("aejbName", aejbName);
 		this.interceptors = container.getInterceptors();
 		container.addDepndencies(aejbName);
 	}
 	
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         final InterceptorContext context = new InterceptorContext();
         // setup interceptors
         context.setInterceptors(interceptors);
         // special location for original proxy
         context.putPrivateData(Object.class, proxy);
         context.setParameters(args);
         context.setMethod(method);
         // setup the public context data
         context.setContextData(contextData);
        return context.proceed();
 	}
 
 }
