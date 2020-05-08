 package github.alahijani.pistachio;
 
 import java.lang.*;
 import java.lang.reflect.*;
 import java.util.logging.Logger;
 
 /**
  * @author Ali Lahijani
  */
 public class CaseClassFactory<CC extends CaseClass<CC>> {
 
     private static Logger logger = Logger.getLogger(CaseClassFactory.class.getName());
 
     private static ClassValue<CaseClassFactory> implCache = new ClassValue<CaseClassFactory>() {
         @Override
         protected CaseClassFactory computeValue(Class<?> type) {
             if (!CaseClass.class.isAssignableFrom(type))
                 return null;
 
             return new CaseClassFactory<>(type.asSubclass(CaseClass.class));
         }
     };
 
     @SuppressWarnings("unchecked")
     public static <CC extends CaseClass<CC>>
     CaseClassFactory<CC> get(Class<CC> caseClass) {
         return implCache.get(caseClass);
     }
 
     @SuppressWarnings("unchecked")
     public static <CC extends CaseClass<CC>, V extends CaseVisitor<CC>>
     CaseClassFactory<CC> get(Class<CC> caseClass, V instantiator) {
         return new CaseClassFactory<>(caseClass, instantiator);
     }
 
     private final CaseVisitorFactory<?, ?> caseVisitorFactory;
     private final SelfVisitorFactory<?> selfVisitorFactory;
     private final Instantiator<CC> instantiator;
 
     private <V extends CaseVisitor<CC>>
     CaseClassFactory(Class<CC> caseClass) {
         this(caseClass, null);
     }
 
     private <V extends CaseVisitor<CC>>
     CaseClassFactory(Class<CC> caseClass, final V instantiator) {
         Class<V> visitorClass = this.<CC, V>getAcceptorType(caseClass);
 
         caseVisitorFactory = new CaseVisitorFactory<>(visitorClass);
         selfVisitorFactory = new SelfVisitorFactory<>(visitorClass, caseClass);
 
         this.instantiator = getInstantiator(caseClass, instantiator);
     }
 
     private <V extends CaseVisitor<CC>>
     Instantiator<CC> getInstantiator(Class<CC> caseClass, final V instantiator) {
         if (instantiator == null) {
             final Constructor<CC> privateConstructor;
 
             try {
                 privateConstructor = caseClass.getDeclaredConstructor();
                 privateConstructor.setAccessible(true);
             } catch (NoSuchMethodException e) {
                String message = "Case class " + caseClass.getName() +
                        " should declare a private no-args constructor";
                 throw new IllegalStateException(message, e);
             }
 
             return new Instantiator<CC>() {
                 @Override
                 public CC instantiate(Method method, Object[] args) throws Throwable {
                     try {
                         CC instance = privateConstructor.newInstance();
                         instance.assign0(CaseClassFactory.this, method, args);
                         return instance;
                     } catch (InstantiationException e) {
                         throw e.getCause();
                     }
                 }
             };
         } else {
             return new Instantiator<CC>() {
                 @Override
                 @SuppressWarnings("unchecked")
                 public CC instantiate(Method method, Object[] args) throws Throwable {
                     try {
                         CC instance = (CC) method.invoke(instantiator, args);
                         instance.assign0(CaseClassFactory.this, method, args);
                         return instance;
                     } catch (InvocationTargetException e) {
                         throw e.getCause();
                     }
                 }
             };
         }
     }
 
     private interface Instantiator<CC> {
 
         CC instantiate(Method method, Object[] args) throws Throwable;
 
     }
 
     @SuppressWarnings("unchecked")
     private <R, V extends CaseVisitor<R>>
     CaseVisitorFactory<R, V> caseVisitorFactory() {
         return (CaseVisitorFactory<R, V>) caseVisitorFactory;
     }
 
     @SuppressWarnings("unchecked")
     private <V extends CaseVisitor<CC>>
     SelfVisitorFactory<V> selfVisitorFactory() {
         return (SelfVisitorFactory<V>) selfVisitorFactory;
     }
 
     public <R, V extends CaseVisitor<R>>
     Class<V> visitorClass() {
         return this.<R, V>caseVisitorFactory().visitorClass;
     }
 
     @SuppressWarnings("unchecked")
     private <R, V extends CaseVisitor<R>>
     Class<V> getAcceptorType(Class<CC> caseClass) {
         try {
 
             Method acceptor = caseClass.getDeclaredMethod("acceptor");
 
             Type returnType = acceptor.getGenericReturnType();
             if (returnType instanceof Class<?>) {
                 logger.severe("Raw return type found for method " + acceptor);
 
                 assert returnType == CaseClass.Acceptor.class;
                 return (Class) CaseVisitor.class;
             } else if (returnType instanceof ParameterizedType) {
                 ParameterizedType parameterizedType = (ParameterizedType) returnType;
 
                 assert parameterizedType.getRawType() == CaseClass.Acceptor.class;
                 Type visitorType = parameterizedType.getActualTypeArguments()[0];
 
                 while (true) {
                     if (visitorType instanceof Class<?>) {
                         return (Class<V>) ((Class) visitorType).asSubclass(CaseVisitor.class);
                     } else if (visitorType instanceof ParameterizedType) {
                         visitorType = ((ParameterizedType) visitorType).getRawType();
                     } else if (visitorType instanceof WildcardType) {
                         Type[] lowerBounds = ((WildcardType) visitorType).getLowerBounds();
                         if (lowerBounds.length ==0) {
                             throw new IllegalArgumentException("Method " + caseClass.getName() + ".acceptor() " +
                                     "must have a return type of the form Acceptor<? super Visitor<R>, R> " +
                                     "in which Visitor<R> is an interface extending " + CaseVisitor.class.getName() + "<R>");
                         }
                         visitorType = lowerBounds[0];
                     } else {
                         throw new AssertionError("Strange method signature: " + acceptor);
                     }
                 }
             } else {
                 throw new AssertionError("Strange method signature: " + acceptor);
             }
 
         } catch (NoSuchMethodException e) {
             throw new IllegalArgumentException("Case class " + caseClass.getName() + " must override acceptor()", e);
         }
     }
 
     static class CaseVisitorFactory<R, V extends CaseVisitor<R>> {
 
         protected final Class<V> visitorClass;
         protected final Constructor<? extends V> visitorConstructor;
 
         public CaseVisitorFactory(Class<V> visitorClass) {
             this.visitorClass = visitorClass;
             this.visitorConstructor = visitorConstructor(this.visitorClass);
         }
 
         /*
         public V uniformVisitor() {
             return null;
         }
         */
         private static <V extends CaseVisitor<?>>
         Constructor<? extends V> visitorConstructor(Class<V> visitorClass) {
             try {
                 Class<? extends V> proxyClass =
                         Proxy.getProxyClass(visitorClass.getClassLoader(), visitorClass).asSubclass(visitorClass);
                 return proxyClass.getConstructor(InvocationHandler.class);
             } catch (NoSuchMethodException e) {
                 // this cannot happen, unless as an internal error of the VM
                 throw new InternalError(e.toString(), e);
             }
         }
 
         @SuppressWarnings("unchecked")
         <CC extends CaseClass<CC>, W extends CaseVisitor<R>>
         CaseClass.Acceptor<V, R> cast(CaseClass.Acceptor<W, R> acceptor) {
 
             if (!acceptor.visitorClass.isAssignableFrom(this.visitorClass))
                 throw new ClassCastException(this.visitorClass.toString());
 
             return (CC.Acceptor<V, R>) acceptor;
         }
 
     }
 
     /**
      * Creates a new assigner visitor for the case represented by the given acceptor.
      *
      * @throws IllegalArgumentException if the case represented by the given acceptor is not an
      *                                  instance of {@link CaseReference}
      */
     public <V extends CaseVisitor<java.lang.Void>>
     V assign(final CaseReference<CC, V> ref) {
         CaseVisitorFactory<java.lang.Void, V> factory = caseVisitorFactory();
 
         VisitorInvocationHandler<java.lang.Void, V> handler = new VisitorInvocationHandler<java.lang.Void, V>(factory.visitorClass) {
             @SuppressWarnings("unchecked")
             @Override
             protected java.lang.Void handle(V proxy, Method method, Object[] args) throws Throwable {
                 ref.set((CC) method.invoke(values(), args));
                 return null;
             }
         };
 
         return handler.newVisitor(factory.visitorConstructor);
     }
 
     public <V extends CaseVisitor<CC>>
     V values(Class<V> visitorClass) {
         return visitorClass.cast(values());
     }
 
     public CaseVisitor<CC> values() {
         return selfVisitorFactory.selfVisitor();
     }
 
     private class SelfVisitorFactory<V extends CaseVisitor<CC>>
             extends CaseVisitorFactory<CC, V> {
 
         private final V postProcessor;
         private final V selfVisitor;
 
         public SelfVisitorFactory(Class<V> visitorClass, Class<CC> caseClass) {
             this(visitorClass, caseClass, null);
         }
 
         public SelfVisitorFactory(Class<V> visitorClass, Class<CC> caseClass, V postProcessor) {
             super(visitorClass);
             this.postProcessor = postProcessor;
 
             for (Constructor<?> constructor : caseClass.getDeclaredConstructors()) {
                 if (!Modifier.isPrivate(constructor.getModifiers()) && !constructor.isSynthetic()) {
                     logger.warning("Case class " + caseClass.getName() +
                             " should only have private constructors. But found: " + constructor);
                 }
             }
 
             VisitorInvocationHandler<CC, V> handler = new VisitorInvocationHandler<CC, V>(this.visitorClass) {
                 @Override
                 protected CC handle(V proxy, Method method, Object[] args) throws Throwable {
                     return instantiator.instantiate(method, args);
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
                     CC instance = handler.handle(proxy, method, args);
 
                     CaseClass.Acceptor<?, CC> original = instance.acceptor();
                     CaseClass.Acceptor<V, CC> acceptor = original.cast(visitorClass);
 
                     return acceptor.accept(postProcessor);
                 }
             };
         }
 
         V selfVisitor() {
             return selfVisitor;
         }
 
     }
 
     private abstract static class VisitorInvocationHandler<R, V extends CaseVisitor<R>>
             implements InvocationHandler {
 
         private final Class<V> visitorClass;
 
         public VisitorInvocationHandler(Class<V> visitorClass) {
             this.visitorClass = visitorClass;
         }
 
         @Override
         public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
             if (method.getDeclaringClass() == Object.class)
                 handleObjectMethod(this, proxy, method, args);
 
             if (method.getDeclaringClass() == CaseVisitor.class)
                 return handleCommonMethod(proxy, method, args);
 
             /**
              * throw early if visitorClass cannot handle method
              */
             visitorClass.asSubclass(method.getDeclaringClass());
 
             return handle(visitorClass.cast(proxy), method, args);
         }
 
         protected abstract R handle(V proxy, Method method, Object[] args) throws Throwable;
 
         /**
          * Actually there is nothing Visitor-specific about this method
          */
         private static Object handleObjectMethod(InvocationHandler handler, Object proxy, Method method, Object[] args) {
             switch (method.getName()) {
                 case "equals":
                     assert args.length == 1;
                     Object that = args[0];
                     return Proxy.isProxyClass(proxy.getClass()) && Proxy.getInvocationHandler(that) == handler;
                 case "hashCode":
                     return System.identityHashCode(proxy);
                 case "toString":
                     return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
                 default:
                     throw new AssertionError();
             }
         }
 
         private Object handleCommonMethod(Object proxy, Method method, Object[] args) {
             // assert CaseClass.Visitor.class.getMethod("apply", CaseClass.class).equals(method);
             return null;
         }
 
         public V newVisitor(Constructor<? extends V> visitorConstructor) {
             try {
                 return visitorConstructor.newInstance(this);
             } catch (IllegalAccessException |
                     InstantiationException |
                     InvocationTargetException e) {
                 // this cannot happen, unless as an internal error of the VM
                 throw new InternalError(e.toString(), e);
             }
         }
 
     }
 }
