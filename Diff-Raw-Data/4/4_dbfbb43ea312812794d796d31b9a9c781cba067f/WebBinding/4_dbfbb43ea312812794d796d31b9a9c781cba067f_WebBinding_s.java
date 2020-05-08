 package net.davidtanzer.wicket.webbinding;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 
 import net.sf.cglib.proxy.Enhancer;
 
 public class WebBinding {
 	private static class BindingInformation {
 		private final BoundTypeInterceptor<?> interceptor;
 		private final String methodName;
 		
 		public BindingInformation(final BoundTypeInterceptor<?> interceptor, final String methodName) {
 			this.interceptor = interceptor;
 			this.methodName = methodName;
 		}
 	}
 	
 	private static ThreadLocal<BindingInformation> currentBinding = new ThreadLocal<BindingInformation>();
 	private static ThreadLocal<ActionBindingTarget<?>> currentActionBindingTarget = new ThreadLocal<ActionBindingTarget<?>>();
 	
 	public static <T> T bindable(final Class<T> superClass, final Object... constructorParameters) {
 		Enhancer enhancer = new Enhancer();
 		enhancer.setSuperclass(superClass);
 		enhancer.setCallback(new BoundTypeInterceptor<T>());
 		
 		Class<?>[] argumentTypes = new Class<?>[constructorParameters.length];
 		for(int i=0; i<constructorParameters.length; i++) {
 			argumentTypes[i] = constructorParameters[i].getClass();
 		}
 		Constructor<?>[] constructors = superClass.getConstructors();
 		for(Constructor<?> c : constructors) {
 			if(argumentTypesCompatible(constructorParameters, c.getParameterTypes())) {
 				argumentTypes = c.getParameterTypes();
 				break;
 			}
 		}
 		return (T) enhancer.create(argumentTypes, constructorParameters);
 	}
 
 	private static boolean argumentTypesCompatible(final Object[] constructorParameters, final Class<?>[] parameterTypes) {
 		if(constructorParameters.length == parameterTypes.length) {
 			for(int i=0; i<constructorParameters.length; i++) {
 				if(!parameterTypes[i].isInstance(constructorParameters[i])) {
 					return false;
 				}
 			}
 		}
		return true;
 	}
 
 	public static <T> T target(final T object) {
 		Enhancer enhancer = new Enhancer();
 		enhancer.setSuperclass(object.getClass());
 		enhancer.setCallback(new TargetTypeInterceptor<T>(object));
 		
 		return createEnhancedObject(object, enhancer);
 	}
 	
 	public static <T> BindingTarget<T> bind(final T bindableReturnValue) {
 		return new BindingTarget<T>();
 	}
 	
 	public static <T> ActionBindingTarget<T> bindAction(final BindableAction<T> action) {
 		ActionBindingTarget<T> actionBindingTarget = new ActionBindingTarget<T>(action);
 		currentActionBindingTarget.set(actionBindingTarget);
 		return actionBindingTarget;
 	}
 	
 	static ActionBindingTarget<?> removeCurrentActionBindingTarget() {
 		ActionBindingTarget<?> actionBindingTarget = currentActionBindingTarget.get();
 		currentActionBindingTarget.set(null);
 		return actionBindingTarget;
 	}
 	
 	static void currentBoundType(final BoundTypeInterceptor<?> interceptor, final String methodName) {
 		currentBinding.set(new BindingInformation(interceptor, methodName));
 	}
 	
 	static void currentBindingTarget(final TargetTypeInterceptor<?> targetInterceptor, final Method method) {
 		if(currentBinding.get() == null) {
 			throw new IllegalStateException("No current binding found!");
 		}
 		
 		currentBinding.get().interceptor.setTarget(currentBinding.get().methodName, targetInterceptor.getObject(), method.getName());
 		currentBinding.set(null);
 	}
 	
 	private static <T> T createEnhancedObject(final T object, final Enhancer enhancer) {
 		Constructor<?>[] constructors = object.getClass().getConstructors();
 		Constructor<?> bestConstructor = null;
 		for(Constructor<?> constructor : constructors) {
 			if(bestConstructor == null) {
 				bestConstructor = constructor;
 			} else if(constructor.getParameterTypes().length < bestConstructor.getParameterTypes().length) {
 				bestConstructor = constructor;
 			}
 		}
 		Class<?>[] parameterTypes = bestConstructor.getParameterTypes();
 		Object[] parameters = new Object[parameterTypes.length];
 		for(int i=0; i<parameterTypes.length; i++) {
 			Class<?> type = parameterTypes[i];
 			if(String.class.isAssignableFrom(type)) {
 				parameters[i] = "dummy";
 			} else if(int.class.isAssignableFrom(type)) {
 				parameters[i] = 0;
 			} else if(long.class.isAssignableFrom(type)) {
 				parameters[i] = 0L;
 			} else if(double.class.isAssignableFrom(type)) {
 				parameters[i] = 0.0;
 			} else if(boolean.class.isAssignableFrom(type)) {
 				parameters[i] = false;
 			} else {
 				parameters[i] = null;
 			}
 		}
 		return (T) enhancer.create(parameterTypes, parameters);
 	}
 }
 
