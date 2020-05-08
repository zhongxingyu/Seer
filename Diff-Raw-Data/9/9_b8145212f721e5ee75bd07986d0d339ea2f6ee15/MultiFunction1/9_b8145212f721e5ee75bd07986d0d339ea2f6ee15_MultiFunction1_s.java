 package de.deterministicarts.lib.dispatch;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.ListIterator;
 import java.util.WeakHashMap;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Single-argument multi-function. 
  * 
  * <p><strong>Subclassing</strong> Client applications can create
  * subclasses of this class. Note, however, that such subclasses
  * should <em>not</em> override any of the methods defined in this
  * class. The ability to create subclasses is provided only for the
  * benefit of the signature-inferrence mechanism.
  * 
  * @author Dirk Eßer
  *
  * @param <R>		function codomain
  * @param <A1>		function domain
  */
 
 public class MultiFunction1<R,A1> 
 implements MultiFunction {
 	
 	/**
 	 * Abstract method base class. This class acts as abstract base
 	 * class for all methods, which can be added to a {@link MultiFunction1}
 	 * instance. Note, that this class itself is not intended to be 
 	 * subclassed directly. Derive your method implementations from
 	 * either {@link LeafMethod} or {@link InnerMethod}.
 	 * 
 	 * @param <R>		method codomain
 	 * @param <A1>		method domain
 	 */
 	
 	public static abstract class Method<R,A1> 
 	implements MultiFunction.Method {
 		
 		/**
 		 * This method's signature. The value of this attribute is
 		 * reported by method {@link #signature()} as the actual
 		 * signature. It is used when we test, whether the method is
 		 * applicable for an argument, and also, when we sort all
 		 * applicable methods in order to determine the order, in
 		 * which they are called. 
 		 */
 		
 		protected final Signature signature;
 		
 		private Method() {
 			final Class<?> bound = ReflectHelper.resolveTypeVariables(getClass()).get(Method.class.getTypeParameters()[1]);
 			if( bound == null ) throw new MultiFunctionError("no signature was supplied, and type variables remain unbound");
 			else {
 				final Criterion c = Criterion.isA(bound);
 				signature = Signature.of(c);
 			}
 		}
 		
 		private Method(Signature s) {
 			if( s == null || s.size() != 1 ) throw new IllegalArgumentException();
 			else {
 				signature = s;
 			}
 		}
 		
 		/**
 		 * Obtains the method's signature. This method returns the 
 		 * signature object, which describes the argument values, the
 		 * method implementation accepts as arguments.
 		 * 
 		 * @return	a signature for this method
 		 */
 		
 		public Signature signature() {
 			return signature;
 		}
 		
 		abstract Entry<R,A1> newEntry(Entry<R,A1> next);
 	};
 	
 	/**
 	 * Leaf method. This class acts as abstract base class for
 	 * leaf methods, i.e., multi-function methods, which won't
 	 * call other ("less specific") methods.
 	 * 
 	 * @param <R>		method codomain
 	 * @param <A1>		method domain
 	 */
 	
 	public static abstract class LeafMethod<R,A1>
 	extends Method<R,A1> {
 		
 		/**
 		 * Constructor, which infers the signature. Use this constructor
 		 * if the signature of the method can be inferred from the subclass'
 		 * set of bound type variables.
 		 */
 		
 		protected LeafMethod() { super(); }
 		protected LeafMethod(Signature s) { super(s); }
 		protected LeafMethod(Criterion s) { super(Signature.of(s)); }
 		
 		/**
 		 * Apply the method to some argument value. 
 		 * 
 		 * @param arg1	the actual argument value
 		 * 
 		 * @return	the result
 		 */
 		
 		public abstract R call(A1 arg1)
 		throws Exception;
 		
 		Entry<R,A1> newEntry(Entry<R,A1> next) {
 			return new LeafEntry<R,A1>(this);
 		}
 	}
 	
 	/**
 	 * Inner method. This class acts as abstract inner method, i.e.,
 	 * as base class for method implementations, which want to load
 	 * off some part of the computation to other (less specific)
 	 * methods. 
 	 *
 	 * @param <R>	type of codomain
 	 * @param <A1>	type of domain
 	 */
 	
 	public static abstract class InnerMethod<R,A1>
 	extends Method<R,A1> {
 		
 		protected InnerMethod() { super(); }
 		protected InnerMethod(Signature s) { super(s); }
 		protected InnerMethod(Criterion s) { super(Signature.of(s)); }
 		
 		public abstract R call(NextMethod<R> nextMethod, A1 arg1)
 		throws Exception;
 		
 		Entry<R,A1> newEntry(Entry<R,A1> next) {
 			return new InnerEntry<R,A1>(this, next);
 		}
 	}
 	
 	private static final Signature ANYSIG = Signature.of(Criterion.anyValue()); 
 	
 	/**
 	 * Create a new multi-function. This function returns a new multi
 	 * function with the given {@code name} and {@code signature}. 
 	 * Currently, the signature is ignored, but later versions of this
 	 * library might only ever accept methods to be added, whose 
 	 * signature is actually compatible to the one specified.
 	 * 
 	 * @param <R>		type of codomain of the new function
 	 * @param <A1>		type of domain of the new function
 	 * 
 	 * @param name		name of the new function (may be {@code null})
 	 * @param base		base signature of the function
 	 * 
 	 * @return	a new instance of this class with properties initialized
 	 * 			from the given arguments. Note, that the new function
 	 * 			has no methods added to it.
 	 */
 	
 	public static <R,A1> MultiFunction1<R,A1> newInstance(String name, Signature base) {
 		if( base == null ) throw new IllegalArgumentException();
 		return new MultiFunction1<R,A1>(name, base);
 	}
 	
 	/**
 	 * Create a new multi-function. This function returns a new multi
 	 * function with the given {@code name}. The new function will 
 	 * have a signature, which allows any value to passed to it, 
 	 * regardless of the actual static type of the new object.
 	 * 
 	 * @param <R>		type of codomain of the new function
 	 * @param <A1>		type of domain of the new function
 	 * 
 	 * @param name		name of the new function (may be {@code null})
 	 * 
 	 * @return	a new instance of this class with properties initialized
 	 * 			from the given arguments. Note, that the new function
 	 * 			has no methods added to it.
 	 */
 	
 	public static <R,A1> MultiFunction1<R,A1> newInstance(String name) {
 		return new MultiFunction1<R,A1>(name, ANYSIG);
 	}
 
 	/**
 	 * Create a new multi-function. This function returns a new anoymous
 	 * multi function. The new function will have a signature, which allows 
 	 * any value to passed to it, regardless of the actual static type of 
 	 * the new object.
 	 * 
 	 * @param <R>		type of codomain of the new function
 	 * @param <A1>		type of domain of the new function
 	 * 
 	 * @return	a new instance of this class with properties initialized
 	 * 			from the given arguments. Note, that the new function
 	 * 			has no methods added to it.
 	 */
 	
 	public static <R,A1> MultiFunction1<R,A1> newInstance() {
 		return new MultiFunction1<R,A1>(null, ANYSIG);
 	}
 	
 	private static final AtomicLong counter = new AtomicLong(0);
 	
 	/**
 	 * This object is used as monitor for thread synchronization. We use
 	 * a {@link Long} instance here, since it allows us to grab locks in
 	 * a well-defined order, and thus helps to avoid dead-locks.
 	 */
 	
 	private final Long monitor;
 	
 	/**
 	 * The function's name. The value is only used when we have to display
 	 * information about the function.
 	 */
 	
 	private final String name;
 	
 	/**
 	 * The base signature of the function. This value is currently only
 	 * used for information purposes, but later versions are likely to
 	 * check added methods, and reject those not compatible with this
 	 * signature.
 	 */
 	
 	private final Signature signature;
 	
 	/**
 	 * The set of actual methods added to this function. We use a hash
 	 * set here (could use a list as well). We do <em>not</em> key methods
 	 * on their signature (though we could -- any probably should?)
 	 */
 	
 	private final HashSet<Method<?,?>> methods;
 	
 	/**
 	 * Cache from class to actual "effective method combination". Whenever
 	 * a multi-function is invoked, this cache is consulted using the 
 	 * actual argument value's class as key. This helps to avoid the 
 	 * extremely expensive look-up of the effective method, when we have
 	 * to do it "from scratch".
 	 */
 	
 	private final WeakHashMap<Class<?>,Entry<?,?>> cache;
 	
 	/**
 	 * If true, the function has been sealed, and no new methods can be
 	 * added to it (and neither can methods be removed). Sealing is a one
 	 * way operation. Once a function is sealed, there is no way to unseal
 	 * it again.
 	 */
 	
 	private boolean sealed;
 	
 	private MultiFunction1(String name, Signature signature) {
 		this.monitor = new Long(counter.incrementAndGet());
 		this.name = name;
 		this.signature = signature;
 		this.methods = new HashSet<Method<?,?>>();
 		this.cache = new WeakHashMap<Class<?>,Entry<?,?>>();
 		this.sealed = false;
 	}
 	
 	/**
 	 * Initialize a new instance, inferring the base signature. This
 	 * constructor is provided for the convenience of client code, which
 	 * wants to use the signature-inference mechanism present for methods
 	 * in order to let the machinery infer the base signature of the 
 	 * function itself.
 	 * 
 	 * @param name	name of the new function (may be {@code null})
 	 */
 	
 	protected MultiFunction1(String name) {
 		final Class<?> cls = ReflectHelper.resolveTypeVariables(getClass()).get(MultiFunction1.class.getTypeParameters()[1]);
 		if( cls == null ) throw new MultiFunctionError();
 		else {
 			this.monitor = new Long(counter.incrementAndGet());
 			this.name = name;
 			this.signature = Signature.of(Criterion.isA(cls));
 			this.methods = new HashSet<Method<?,?>>();
 			this.cache = new WeakHashMap<Class<?>,Entry<?,?>>();
 			this.sealed = false;
 		}
 	}
 	
 	/**
 	 * Tests, whether the function is sealed.
 	 * 
 	 * @return	{@code true}, if the function has been sealed and cannot
 	 * 			be modified, {@code false}, if the function is open and
 	 * 			methods may freely be added to and removed from it.
 	 */
 	
 	public boolean isSealed() {
 		synchronized( monitor ) {
 			return sealed;
 		}
 	}
 	
 	/**
 	 * Seals the function. This method is called in order to seal the 
 	 * function, protecting it against subsequent changes. After this
 	 * method returns, any attempt to add or remove methods will cause
 	 * an {@link IllegalStateException} to be raised from the mutation
 	 * method.
 	 * 
 	 * <p>Note, that sealing is a one-way operation. You can only seal
 	 * a function once, but there are no ways to "unseal" it. Calling
 	 * this function more than once is harmless. All calls except the
 	 * first are ignored.
 	 */
 	
 	public void setSealed() {
 		synchronized( monitor ) {
 			sealed = true;
 		}
 	}
 	
 	private final Long min(Long n, Long m) {
 		return n.longValue() < m.longValue()? n : m;
 	}
 	
 	private final Long max(Long n, Long m) {
 		return n.longValue() >= m.longValue()? n : m;
 	}
 	
 	public String toString() {
 		
 		int nm = 0;
 		
 		synchronized( monitor ) {
 			nm = methods.size();
 		}
 		
 		final String na = (name != null? name : String.format("<Anonymous@%x>", System.identityHashCode(this)));
 		return "<" + na + signature + " (" + nm + " method" + (nm == 1? "" : "s") + ")>"; 
 	}
 	
 	/**
 	 * Add a new method. This method adds {@code method} as new method to
 	 * this function. When the function is invoked with some argument
 	 * value, the {@linkplain Method#signature() method's signature} is
 	 * used to determine, whether the method is applicable for being called
 	 * withn this argument, and also in order to determine the order in
 	 * case, there are multiple applicable methods. 
 	 * 
 	 * @param <T>		intermediate type
 	 * @param method	method implementation
 	 * 
 	 * @return	{@code true}, if the methods has been added to the function
 	 * 			and {@code false}, if it was already present.
 	 * 
 	 * @throws IllegalStateException	if the function has been sealed and
 	 * 									cannot be modified
 	 */
 	
 	public <T extends A1> boolean add(Method<? extends R,? super T> method) {
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
 	
 	/**
 	 * Adds new methods. This method adds all entries of collection
 	 * {@code methods} as new methods to this function. 
 	 * 
 	 * @param <T>		intermediate type
 	 * @param methods	collection of method implementation
 	 * 
 	 * @return	{@code true}, if the any method has been added to the function
 	 * 			and {@code false}, if nothing has changed
 	 * 
 	 * @throws IllegalStateException	if the function has been sealed and
 	 * 									cannot be modified
 	 */
 	
 	public <T extends A1> boolean addAll(Collection<? extends Method<? extends R,? super T>> methods) {
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
 	
 	/**
 	 * Adds new methods. This method adds all methods of function
 	 * {@code other} as new methods to this function. 
 	 * 
 	 * @param <T>		intermediate type
 	 * @param methods	other multi-function
 	 * 
 	 * @return	{@code true}, if the any method has been added to the function
 	 * 			and {@code false}, if nothing has changed
 	 * 
 	 * @throws IllegalStateException	if the function has been sealed and
 	 * 									cannot be modified
 	 */
 	
 	public <T extends A1> boolean addAll(MultiFunction1<? extends R,? super T> other) {
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
 	
 	/**
 	 * Remove a method. This method removes the {@code method} from
 	 * this function's set of defined methods. 
 	 * 
 	 * @param method	method to remove
 	 * 
 	 * @return	{@code true}, if the method was removed, and {@code false},
 	 * 			if no matching method was found
 	 * 
 	 * @throws IllegalStateException	if the function has been sealed and
 	 * 									cannot be modified
 	 */
 	
 	public boolean remove(Method<?,?> method) {
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
 	
 	private Entry<R,A1> lookUp(Object argument) {
 		
 		synchronized( monitor ) {
 			
 			final Class cls = (argument == null? null : argument.getClass());
 			final Entry cached = cache.get(cls);
 			
 			if( cached != null ) return cached;
 			else {
 				
 				final Object[] formals = new Object[] { argument };
 				final ArrayList<Method> applicable = new ArrayList<Method>(methods.size());
 				
 				for( Method<?,?> method: methods )
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
 				
 				cache.put(cls, seed);
 				return seed;
 			}
 		}
 	}
 	
 	private static final Implication.Test<Method> methodImplicationTest = new Implication.Test<MultiFunction1.Method>() {
 		public boolean implies(Method implicant, Method implied) {
 			return implicant.signature.implies(implied.signature);
 		}
 	};
 	
 	/**
 	 * Invoke this function. This method invokes the function,
 	 * calling the applicable methods for the given argument value
 	 * {@code argument}.
 	 * 
 	 * @param argument		actual argument value
 	 * 
 	 * @return	the result of the call.
 	 */
 	
 	public R call(A1 argument) {
 		return lookUp(argument).call(argument);
 	}
 	
 	/**
 	 * @return the function's defined name
 	 */
 	
 	public String name() {
 		return name;
 	}
 	
 	/**
 	 * @return the function's base signature
 	 */
 	
 	public Signature signature() {
 		return signature;
 	}
 	
 	public Object invoke(Object[] arguments) {
		if( arguments == null || arguments.length == 1 ) throw new IllegalArgumentException();
 		return lookUp(arguments[0]).call((A1)arguments[0]);
 	}
 	
 
 	
 	private static abstract class Entry<R,A1> {
 
 		abstract R call(A1 argument);
 	}
 	
 	private final Entry<R,A1> noApplicableMethods = new Entry<R,A1>() {
 		R call(A1 argument) {
 			throw new NoApplicableMethodsException(MultiFunction1.this, new Object[] { argument });
 		}
 	};
 	
 	private final class AmbigousMethods
 	extends Entry<R,A1> {
 		
 		private final Collection<Method> methods;
 		
 		AmbigousMethods(Collection<? extends Method> methods) {
 			this.methods = Collections.unmodifiableCollection(new ArrayList<Method>(methods));
 		}
 
 		R call(A1 argument) {
 			throw new AmbigousMethodsException(MultiFunction1.this, new Object[] { argument }, methods);
 		}
 	}
 	
 	private static final class Chainer<R,A1>
 	extends NextMethod<R> {
 		
 		private final A1 argument;
 		private final Entry<R,A1> next;
 		
 		Chainer(A1 argument, Entry<R,A1> next) {
 			this.argument = argument;
 			this.next = next;
 		}
 		
 		public R call() {
 			return next.call(argument);
 		}
 	}
 	
 	private static final class LeafEntry<R,A1>
 	extends Entry<R,A1> {
 		
 		private final LeafMethod<R,A1> method;
 		
 		LeafEntry(LeafMethod<R,A1> method) {
 			this.method = method;
 		}
 		
 		R call(A1 argument) {
 			try {
 				return method.call(argument);
 			} catch( Error exc ) {
 				throw exc;
 			} catch( RuntimeException exc ) {
 				throw exc;
 			} catch( Throwable exc ) {
 				throw new MultiMethodInvocationTargetException(null, new Object[] { argument }, method, exc);
 			}
 		}
 	}
 	
 	private static final class InnerEntry<R,A1>
 	extends Entry<R,A1> {
 		
 		private final InnerMethod<R,A1> method;
 		private final Entry<R,A1> next;
 		
 		InnerEntry(InnerMethod<R,A1> method, Entry<R,A1> next) {
 			this.method = method;
 			this.next = next;
 		}
 		
 		R call(A1 argument) {
 			try {
 				return method.call(new Chainer<R,A1>(argument, next), argument);
 			} catch( Error exc ) {
 				throw exc;
 			} catch( RuntimeException exc ) {
 				throw exc;
 			} catch( Throwable exc ) {
 				throw new MultiMethodInvocationTargetException(null, new Object[] { argument }, method, exc);
 			}
 		}
 	}
 }
