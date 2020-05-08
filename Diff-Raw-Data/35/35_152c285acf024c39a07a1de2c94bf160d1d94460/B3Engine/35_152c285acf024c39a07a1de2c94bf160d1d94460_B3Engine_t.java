 package org.eclipse.b3.backend.core.runtime;
 
 import java.util.List;
import java.util.Map;
 
 import org.eclipse.b3.backend.core.IB3Engine;
 import org.eclipse.b3.backend.core.exceptions.B3EngineException;
 import org.eclipse.b3.backend.core.internal.B3BackendActivator;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendFactory;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BInvocationContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BSystemContext;
 import org.eclipse.b3.backend.functions.ArithmeticFunctions;
 import org.eclipse.b3.backend.functions.RelationalFunctions;
 import org.eclipse.b3.backend.functions.StringFunctions;
 import org.eclipse.b3.backend.functions.SystemFunctions;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Guice;
 import com.google.inject.Module;
 import com.google.inject.util.Modules;
 
 public class B3Engine implements IB3Engine {
 	protected BSystemContext systemContext;
 
 	protected BInvocationContext invocationContext;
 
 	/**
 	 * Creates and initializes the engine with a default guice module consisting of
 	 * reqistered extension modules.
 	 * 
 	 * @throws B3EngineException
 	 *             - on failure to load
 	 */
 	public B3Engine() throws B3EngineException {
 		initializeContexts();
 		invocationContext.setInjector(Guice.createInjector(getExtensionModules()));
 	}
 
 	/**
 	 * Creates and initializes the engine with the same configuration as the default constructor, but with
 	 * an overriding guice module.
 	 * 
 	 * @param overridingModule
 	 *            - module with bindings that have higher precedence than the default bindings
 	 * @throws B3EngineException
 	 *             - on failure to load
 	 */
 	public B3Engine(Module overridingModule) throws B3EngineException {
 		initializeContexts();
 		invocationContext.setInjector(Guice.createInjector(Modules.override(getExtensionModules()).with(
 			overridingModule)));
 
 	}
 
	/**
	 * Sets properties in the invocation context (i.e. with the same scope as system properties automatically
	 * loaded). The properties can not override system properties. The property map keys should not be enclosed
	 * in ${ }. The defined properties are immutable but not final (i.e. may be redefined in an inner context).
	 * 
	 * @param properties
	 * @throws B3EngineException
	 *             if an immutable property is modified (or if property map contains invalid entries)
	 */
	public void setProperties(Map<String, String> properties) throws B3EngineException {
		for(Object key : properties.keySet()) {
			invocationContext.defineValue("${" + ((String) key) + "}", properties.get(key), String.class);
		}
	}

 	protected BExecutionContext getContext() {
 		return invocationContext;
 	}
 
 	protected Module getExtensionModules() throws B3EngineException {
 		IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(
 			B3BackendActivator.EXTENSION__RUNTIME_MODULE);
 		List<Module> modules = Lists.newArrayList();
 		for(IConfigurationElement e : configs) {
 			try {
 				modules.add(Module.class.cast(e.createExecutableExtension("moduleClass")));
 			}
 			catch(CoreException e1) {
 				throw new B3EngineException("Loading of RuntimeModule extension failed with exception", e1);
 			}
 		}
 		if(modules.size() < 1)
 			return Modules.EMPTY_MODULE;
 		return Modules.combine(modules);
 	}
 
 	private void initializeContexts() throws B3EngineException {
 		systemContext = B3backendFactory.eINSTANCE.createBSystemContext();
 		invocationContext = B3backendFactory.eINSTANCE.createBInvocationContext();
 		invocationContext.setParentContext(systemContext);
 
 		invocationContext.loadFunctions(ArithmeticFunctions.class);
 		invocationContext.loadFunctions(RelationalFunctions.class);
 		invocationContext.loadFunctions(StringFunctions.class);
 		invocationContext.loadFunctions(SystemFunctions.class);
 
 	}
 
 }
