 package ch.lambdaj.function.closure;
 
 import static ch.lambdaj.function.closure.ClosuresFactory.*;
 
 import java.lang.reflect.*;
 
 import ch.lambdaj.proxy.*;
 
 /**
  * @author Mario Fusco
  */
 class ProxyClosure extends InvocationInterceptor {
 
	private boolean registered = false;
	
 	private AbstractClosure closure;
 	
 	protected ProxyClosure(AbstractClosure closure) {
 		this.closure = closure;
 	}
 
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (registered) return method.invoke(proxy, args);
		registered = true;
 		closure.registerInvocation(method, args);
 		return createProxyClosure(closure, method.getReturnType());
 	}
 }
