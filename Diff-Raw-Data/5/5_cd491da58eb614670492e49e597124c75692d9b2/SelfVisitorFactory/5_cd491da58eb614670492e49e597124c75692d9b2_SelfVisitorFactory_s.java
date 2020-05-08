 package github.alahijani.pistachio;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
 * @author Ali Lahijani
 */
 public class SelfVisitorFactory<CC extends CaseClass<CC>, V extends CaseVisitor<CC>>
         extends CaseVisitorFactory<CC, V> {
 
     private final V postProcessor;
     private final V selfVisitor;
 
     public SelfVisitorFactory(Class<V> visitorClass, Class<CC> caseClass) {
         this(visitorClass, caseClass, null);
     }
 
     public SelfVisitorFactory(Class<V> visitorClass, Class<CC> caseClass, V postProcessor) {
         super(visitorClass);
         this.postProcessor = postProcessor;
 
         final Constructor<CC> privateConstructor;
 
         try {
             privateConstructor = caseClass.getDeclaredConstructor();
             privateConstructor.setAccessible(true);
         } catch (NoSuchMethodException e) {
             String message = "Case class " + caseClass.getName() +
                     " should declare a private no-args constructor";
             throw new IllegalStateException(message, e);
         }
 
         VisitorInvocationHandler<CC, V> handler = new VisitorInvocationHandler<CC, V>(this.visitorClass) {
             @Override
             protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                 try {
                     CC instance = privateConstructor.newInstance();
                    instance.assign0(null, method, args);
                     return instance;
                 } catch (InvocationTargetException e) {
                     throw e.getCause();
                 }
             }
         };
 
         handler = applyPostProcessor(handler);
 
         selfVisitor = handler.newVisitor(visitorConstructor);
     }
 
     private VisitorInvocationHandler<CC, V> applyPostProcessor(final VisitorInvocationHandler<CC, V> handler) {
         if (postProcessor == null)
             return handler;
 
         return new VisitorInvocationHandler<CC, V>(this.visitorClass) {
             @Override
             protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                 @SuppressWarnings("unchecked")
                 CC.Acceptor<CaseVisitor<CC>, CC> acceptor = (CC.Acceptor)
                         handler.handle(proxy, method, args).<CC>acceptor();
                 return acceptor.accept(postProcessor);
             }
         };
     }
 
     public V assign(final CC instance) {
 
         VisitorInvocationHandler<CC, V> handler = new VisitorInvocationHandler<CC, V>(visitorClass) {
             @Override
             protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                instance.assign0(null, method, args);
                 return instance;
             }
         };
 
         handler = applyPostProcessor(handler);
 
         return handler.newVisitor(visitorConstructor);
     }
 
     public V selfVisitor() {
         return selfVisitor;
     }
 
 }
