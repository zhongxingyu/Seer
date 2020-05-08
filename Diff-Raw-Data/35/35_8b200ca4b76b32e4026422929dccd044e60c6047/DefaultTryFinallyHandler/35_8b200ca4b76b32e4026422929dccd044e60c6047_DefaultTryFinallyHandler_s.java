 package au.net.netstorm.boost.spider.onion.layer.closure;
 
 import java.lang.reflect.Method;
 
 public final class DefaultTryFinallyHandler implements TryFinallyHandler {
     private final Object delegate;
     private final TryCatchFinally trier;
 
     public DefaultTryFinallyHandler(Object delegate, TryCatchFinally trier) {
         this.delegate = delegate;
         this.trier = trier;
     }
 
     public Object invoke(Object object, Method method, Object[] parameters) throws Throwable {
         try {
            trier.in();
             return method.invoke(delegate, parameters);
         } finally {
            trier.out();
         }
     }
 }
