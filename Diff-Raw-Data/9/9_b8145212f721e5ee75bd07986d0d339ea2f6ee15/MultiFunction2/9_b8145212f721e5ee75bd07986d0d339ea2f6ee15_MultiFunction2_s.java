 package de.deterministicarts.lib.dispatch;
 
 import java.lang.reflect.TypeVariable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 
 public class MultiFunction2<R,A1,A2> 
 implements MultiFunction {
 	
 	public static abstract class Method<R,A1,A2> 
 	implements MultiFunction.Method {
 		
 		protected final Signature signature;
 		
 		private Method() {
 			final Map<TypeVariable<?>,Class<?>> classes = ReflectHelper.resolveTypeVariables(getClass()); 
 			final Class<?> bound1 = classes.get(Method.class.getTypeParameters()[1]);
 			final Class<?> bound2 = classes.get(Method.class.getTypeParameters()[2]);
 			if( bound1 == null || bound2 == null ) throw new MultiFunctionError("no signature was supplied, and type variables remain unbound");
 			else {
 				signature = Signature.of(Criterion.isA(bound1), Criterion.isA(bound2));
 			}
 		}
 		
 		private Method(Signature s) {
 			if( s == null || s.size() != 2 ) throw new IllegalArgumentException();
 			else {
 				signature = s;
 			}
 		}
 		
 		public Signature signature() {
 			return signature;
 		}
 		
 		abstract Entry<R,A1,A2> newEntry(Entry<R,A1,A2> next);
 	};
 	
 	public static abstract class LeafMethod<R,A1,A2>
 	extends Method<R,A1,A2> {
 		
 		protected LeafMethod() { super(); }
 		protected LeafMethod(Signature s) { super(s); }
 		protected LeafMethod(Criterion s, Criterion t) { super(Signature.of(s, t)); }
 		
 		public abstract R call(A1 arg1, A2 arg2)
 		throws Exception;
 		
 		Entry<R,A1,A2> newEntry(Entry<R,A1,A2> next) {
 			return new LeafEntry<R,A1,A2>(this);
 		}
 	}
 	
 	public static abstract class InnerMethod<R,A1,A2>
 	extends Method<R,A1,A2> {
 		
 		protected InnerMethod() { super(); }
 		protected InnerMethod(Signature s) { super(s); }
 		protected InnerMethod(Criterion s, Criterion t) { super(Signature.of(s, t)); }
 		
 		public abstract R call(NextMethod<R> nextMethod, A1 arg1, A2 arg2)
 		throws Exception;
 		
 		Entry<R,A1,A2> newEntry(Entry<R,A1,A2> next) {
 			return new InnerEntry<R,A1,A2>(this, next);
 		}
 	}
 	
 	private static final Signature ANYSIG = Signature.of(Criterion.anyValue(), Criterion.anyValue()); 
 	
 	public static <R,A1,A2> MultiFunction2<R,A1,A2> newInstance(String name, Signature base) {
 		if( base == null ) throw new IllegalArgumentException();
 		return new MultiFunction2<R,A1,A2>(name, base);
 	}
 	
 	public static <R,A1,A2> MultiFunction2<R,A1,A2> newInstance(String name) {
 		return new MultiFunction2<R,A1,A2>(name, ANYSIG);
 	}
 	
 	public static <R,A1,A2> MultiFunction2<R,A1,A2> newInstance() {
 		return new MultiFunction2<R,A1,A2>(null, ANYSIG);
 	}
 	
 	private static final AtomicLong counter = new AtomicLong();
 	
 	private final Long monitor;
 	private final String name;
 	private final Signature signature;
 	private final HashSet<Method<?,?,?>> methods;
 	private final WeakPairHashMap<Class<?>,Class<?>,Entry<?,?,?>> cache;
 	private boolean sealed;
 	
 	private MultiFunction2(String name, Signature signature) {
 		this.monitor = new Long(counter.incrementAndGet());
 		this.name = name;
 		this.signature = signature;
 		this.methods = new HashSet<Method<?,?,?>>();
 		this.cache = new WeakPairHashMap<Class<?>,Class<?>,Entry<?,?,?>>();
 		this.sealed = false;
 	}
 	
 	protected MultiFunction2(String name) {
 		final Map<TypeVariable<?>,Class<?>> bindings = ReflectHelper.resolveTypeVariables(getClass());
 		final Class<?> c1 = bindings.get(MultiFunction2.class.getTypeParameters()[1]);
 		final Class<?> c2 = bindings.get(MultiFunction2.class.getTypeParameters()[2]);
 
 		if( c1 == null || c2 == null ) throw new MultiFunctionError();
 		else {
 			this.monitor = new Long(counter.incrementAndGet());
 			this.name = name;
 			this.signature = Signature.of(Criterion.isA(c1), Criterion.isA(c2));
 			this.methods = new HashSet<Method<?,?,?>>();
 			this.cache = new WeakPairHashMap<Class<?>,Class<?>,Entry<?,?,?>>();
 			this.sealed = false;
 		}
 	}
 	
 	private static Long min(Long n, Long m) {
 		return n.longValue() < m.longValue()? n : m;
 	}
 	
 	private static Long max(Long n, Long m) {
 		return n.longValue() >= m.longValue()? n : m;
 	}
 	
 	public boolean isSealed() {
 		synchronized( monitor ) {
 			return sealed;
 		}
 	}
 	
 	public void setSealed() {
 		synchronized( monitor ) {
 			this.sealed = true;
 		}
 	}
 	
 	public String toString() {
 		
 		int nm = 0;
 		
 		synchronized( monitor ) {
 			nm = methods.size();
 		}
 		
 		final String na = (name != null? name : String.format("<Anonymous@%x>", System.identityHashCode(this)));
 		return "<" + na + signature + " (" + nm + " method" + (nm == 1? "" : "s") + ")>"; 
 	}
 	
 	public <T extends A1, U extends A2, V extends A2> boolean add(Method<? extends R,? super T, ? super U> method) {
 		if( method == null ) throw new IllegalArgumentException();
 		else {
 			synchronized( monitor ) {
 				if( sealed ) throw new IllegalStateException();
 				if( !methods.add(method) ) return false;
 				else {
 					cache.clear();
 					return true;
 				}
 			}
 		}
 	}
 	
 	public <T extends A1, U extends A2> boolean addAll(Collection<? extends Method<? extends R,? super T, ? super U>> methods) {
 		if( methods == null ) throw new IllegalArgumentException();
 		else {
 			synchronized( monitor ) {
 				if( sealed ) throw new IllegalStateException();
 				if( !methods.addAll((Collection)methods) ) return false;
 				else {
 					cache.clear();
 					return true;
 				}
 			}
 		}
 	}
 	
 	public <T extends A1, U extends A2> boolean addAll(MultiFunction2<? extends R,? super T, ? super U> other) {
 		if( other == null ) throw new IllegalArgumentException();
 		else {
 			if( other == this ) return false;
 			else {
 				synchronized( min(monitor, other.monitor) ) {
 					
 					synchronized( max(monitor, other.monitor) ) {
 					
 						if( sealed ) throw new IllegalStateException();
 						
 						if( !methods.addAll(other.methods) ) return false;
 						else {
 
 							cache.clear();
 							return true;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public boolean remove(Method<?,?,?> method) {
 		if( method == null ) return false;
 		else {
 			synchronized( monitor ) {
 				if( sealed ) throw new IllegalStateException();
 				if( !methods.remove(method) ) return false;
 				else {
 					cache.clear();
 					return true;
 				}
 			}
 		}
 	}
 	
 	private Entry<R,A1,A2> lookUp(Object argument1, Object argument2) {
 		
 		synchronized( monitor ) {
 			
 			final Class cls1 = (argument1 == null? null : argument1.getClass());
 			final Class cls2 = (argument2 == null? null : argument2.getClass());
 			final Entry cached = cache.get(cls1, cls2);
 			
 			if( cached != null ) return cached;
 			else {
 				
 				final Object[] formals = new Object[] { argument1, argument2 };
 				final ArrayList<Method> applicable = new ArrayList<Method>(methods.size());
 				
 				for( Method<?,?,?> method: methods )
 					if( method.signature.matches(formals) ) 
 						applicable.add(method);
 				
 				final Implication.LayerList<Method> layers = Implication.computeAllLayers(applicable, methodImplicationTest);
 				final ListIterator<Implication.Layer<Method>> iterator = layers.listIterator(layers.size());
 				Entry seed = noApplicableMethods;
 				
 				while( iterator.hasPrevious() ) {
 					
 					final Implication.Layer<Method> layer = iterator.previous();
 					
 					if( layer.size() == 1 ) {
 						
 						seed = layer.get(0).newEntry(seed);
 						
 					} else {
 						
 						seed = new AmbigousMethods(layer);
 					}
 				}
 				
 				cache.put(cls1, cls2, seed);
 				return seed;
 			}
 		}
 	}
 	
 	private static final Implication.Test<Method> methodImplicationTest = new Implication.Test<Method>() {
 		public boolean implies(Method implicant, Method implied) {
 			return implicant.signature.implies(implied.signature);
 		}
 	};
 	
 	public R call(A1 argument1, A2 argument2) {
 		return lookUp(argument1, argument2).call(argument1, argument2);
 	}
 	
 	public String name() {
 		return name;
 	}
 	
 	public Signature signature() {
 		return signature;
 	}
 	
 	public Object invoke(Object[] arguments) {
		if( arguments == null || arguments.length == 2 ) throw new IllegalArgumentException();
 		return lookUp(arguments[0], arguments[1]).call((A1)arguments[0], (A2)arguments[1]);
 	}
 	
 	private static abstract class Entry<R,A1,A2> {
 
 		abstract R call(A1 argument, A2 argument2);
 	}
 	
 	private final Entry<R,A1,A2> noApplicableMethods = new Entry<R,A1,A2>() {
 		R call(A1 argument1, A2 argument2) {
 			throw new NoApplicableMethodsException(MultiFunction2.this, new Object[] { argument1, argument2 });
 		}
 	};
 	
 	private final class AmbigousMethods
 	extends Entry<R,A1,A2> {
 		
 		private final Collection<Method> methods;
 		
 		AmbigousMethods(Collection<? extends Method> methods) {
 			this.methods = Collections.unmodifiableCollection(new ArrayList<Method>(methods));
 		}
 
 		R call(A1 argument, A2 argument2) {
 			throw new AmbigousMethodsException(MultiFunction2.this, new Object[] { argument, argument2 }, methods);
 		}
 	}
 	
 	private static final class Chainer<R,A1,A2>
 	extends NextMethod<R> {
 		
 		private final A1 argument1;
 		private final A2 argument2;
 		private final Entry<R,A1,A2> next;
 		
 		Chainer(A1 argument1, A2 argument2, Entry<R,A1,A2> next) {
 			this.argument1 = argument1;
 			this.argument2 = argument2;
 			this.next = next;
 		}
 		
 		public R call() {
 			return next.call(argument1, argument2);
 		}
 	}
 	
 	private static final class LeafEntry<R,A1,A2>
 	extends Entry<R,A1,A2> {
 		
 		private final LeafMethod<R,A1,A2> method;
 		
 		LeafEntry(LeafMethod<R,A1,A2> method) {
 			this.method = method;
 		}
 		
 		R call(A1 argument1, A2 argument2) {
 			try {
 				return method.call(argument1, argument2);
 			} catch( Error exc ) {
 				throw exc;
 			} catch( RuntimeException exc ) {
 				throw exc;
 			} catch( Throwable exc ) {
 				throw new MultiMethodInvocationTargetException(null, new Object[] { argument1, argument2 }, method, exc);
 			}
 		}
 	}
 	
 	private static final class InnerEntry<R,A1,A2>
 	extends Entry<R,A1,A2> {
 		
 		private final InnerMethod<R,A1,A2> method;
 		private final Entry<R,A1,A2> next;
 		
 		InnerEntry(InnerMethod<R,A1,A2> method, Entry<R,A1,A2> next) {
 			this.method = method;
 			this.next = next;
 		}
 		
 		R call(A1 argument1, A2 argument2) {
 			try {
 				return method.call(new Chainer<R,A1,A2>(argument1, argument2, next), argument1, argument2);
 			} catch( Error exc ) {
 				throw exc;
 			} catch( RuntimeException exc ) {
 				throw exc;
 			} catch( Throwable exc ) {
 				throw new MultiMethodInvocationTargetException(null, new Object[] { argument1, argument2 }, method, exc);
 			}
 		}
 	}
 }
