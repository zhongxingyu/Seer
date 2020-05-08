 /*******************************************************************************
  * Copyright (c) 2012 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.codeassist.ICompletionEngine;
 import org.eclipse.dltk.codeassist.ISelectionEngine;
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.builder.IBuildContext;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.internal.javascript.ti.ITypeInferenceContext;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencerVisitor;
 import org.eclipse.dltk.internal.javascript.validation.TypeInfoValidator;
 import org.eclipse.dltk.javascript.ast.JSNode;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.internal.core.CoreMessages;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserUtil;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinfo.ITypeSystem;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Bindings for the module, i.e. type information for the AST nodes.
  */
 public class JSBindings implements Map<ASTNode, IValueReference> {
 
 	private final ITypeSystem typeSystem;
 	private final Map<ASTNode, IValueReference> nodeMap;
 
 	protected JSBindings(ITypeSystem typeSystem,
 			Map<ASTNode, IValueReference> nodeMap) {
 		this.typeSystem = typeSystem;
 		this.nodeMap = nodeMap;
 	}
 
 	protected boolean isCacheable() {
 		return true;
 	}
 
 	private static class CollectingVisitor extends TypeInferencerVisitor {
 		final Map<ASTNode, IValueReference> bindings = new HashMap<ASTNode, IValueReference>();
 
 		public CollectingVisitor(ITypeInferenceContext context) {
 			super(context);
 		}
 
 		@Override
 		public IValueReference visit(ASTNode node) {
 			final IValueReference reference = super.visit(node);
 			if (reference != null && node != null) {
 				bindings.put(node, reference);
 			}
 			return reference;
 		}
 	}
 
 	/**
 	 * Returns bindings for the specified {@link Script} or <code>null</code> if
 	 * not available.
 	 */
 	public static JSBindings of(Script script) {
 		final ISourceModule module = (ISourceModule) script
 				.getAttribute(JavaScriptParserUtil.ATTR_MODULE);
 		if (module != null) {
 			return JSBindings.get(module, script);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the {@link IValueReference} describing the specified node or
 	 * <code>null</code> if not available.
 	 */
 	public static IValueReference resolveBinding(ASTNode node) {
 		if (node instanceof JSNode) {
 			final JSNode jnode = (JSNode) node;
 			final Script script = jnode.getScript();
 			if (script != null) {
 				final JSBindings bindings = of(script);
 				if (bindings != null) {
 					return bindings.get(node);
 				}
 			}
 		}
 		return null;
 	}
 
 	private static JSBindings buildBindings(IModelElement element, Script script) {
 		final JSBindings cached = TypeInfoValidator.getCachedBindings(script);
 		if (cached != null) {
 			return cached;
 		}
 		if (DLTKCore.PERFOMANCE) {
 			if (element != null) {
 				System.out.println("build bindings for " + element.getPath());
 			}
 		}
 		final TypeInferencer2 inferencer = new TypeInferencer2();
 		final CollectingVisitor collector = new CollectingVisitor(inferencer);
 		inferencer.setModelElement(element);
 		inferencer.setVisitor(collector);
 		inferencer.doInferencing(script);
 		return new JSBindings(inferencer, collector.bindings);
 	}
 
 	/**
 	 * Returns the bindings for the specified {@link IModuleSource} (input of
 	 * {@link ICompletionEngine} and {@link ISelectionEngine}). If source
 	 * implements {@link ISourceModule} then this function just delegates to the
 	 * next one which also does caching, otherwise the result is computed just
 	 * for the specified <code>source</code>.
 	 */
 	public static JSBindings of(IModuleSource source) {
 		if (source instanceof ISourceModule) {
 			return of((ISourceModule) source);
 		} else if (source instanceof IBuildContext) {
 			return of((IBuildContext) source);
 		}
 		final Script script = JavaScriptParserUtil.parse(source, null);
 		return buildBindings(source.getModelElement(), script);
 	}
 
 	/**
 	 * Returns bindings for the specified {@link ISourceModule}. The result is
 	 * cached in shared cached AST. The cache is cleared on resource change or
 	 * changes in the editor.
 	 */
 	public static JSBindings of(ISourceModule module) {
 		final Script script = JavaScriptParserUtil.parse(module, null);
 		return get(module, script);
 	}
 
 	private static final String ATTR_BINDINGS = JSBindings.class.getName();
 
 	/**
 	 * Returns bindings for the specified {@link ISourceModule} and AST. The
 	 * result is cached in AST.
 	 */
 	private static JSBindings get(ISourceModule module, Script script) {
 		JSBindings bindings = (JSBindings) script.getAttribute(ATTR_BINDINGS);
 		if (bindings != null) {
 			return bindings;
 		}
 		bindings = buildBindings(module, script);
 		if (bindings.isCacheable()) {
 			script.setAttribute(ATTR_BINDINGS, bindings);
 		}
 		return bindings;
 	}
 
 	/**
 	 * Returns bindings for the specified {@link IBuildContext}. This method
 	 * should be called only from {@link IBuildParticipant} which has dependency
 	 * on {@link TypeInfoValidator}.
 	 * 
 	 * @throws IllegalStateException
 	 *             if {@link IBuildParticipant} preconditions not met.
 	 */
 	public static JSBindings of(IBuildContext context) {
 		final ITypeSystem typeSystem = ITypeSystem.CURRENT.get();
 		if (typeSystem == null) {
 			throw new IllegalStateException(NLS.bind(
 					CoreMessages.JSBindings_not_available,
 					CoreMessages.JSBindings_currentTypeSystem,
 					TypeInfoValidator.ID));
 		}
 		@SuppressWarnings("unchecked")
 		final Map<ASTNode, IValueReference> bindings = (Map<ASTNode, IValueReference>) context
 				.get(TypeInfoValidator.ATTR_BINDINGS);
 		if (bindings == null) {
 			throw new IllegalStateException(NLS.bind(
 					CoreMessages.JSBindings_not_available,
 					CoreMessages.JSBindings_precomputedBindings,
 					TypeInfoValidator.ID));
 		}
 		return new JSBindings(typeSystem, bindings);
 	}
 
 	public ITypeSystem getTypeSystem() {
 		return typeSystem;
 	}
 
 	public IValueReference get(ASTNode node) {
 		return nodeMap.get(node);
 	}
 
 	/**
 	 * Executes the code temporary setting type system of this instance as
 	 * current.
 	 */
 	public void run(Runnable runnable) {
 		ITypeSystem.CURRENT.runWith(typeSystem, runnable);
 	}
 
 	/**
 	 * Executes the code temporary setting type system of this instance as
 	 * current.
 	 */
 	public <V> V run(Callable<V> callable) throws Exception {
 		return ITypeSystem.CURRENT.runWith(typeSystem, callable);
 	}
 
 	public int size() {
 		return nodeMap.size();
 	}
 
 	public boolean isEmpty() {
 		return nodeMap.isEmpty();
 	}
 
 	public boolean containsKey(Object key) {
 		return nodeMap.containsKey(key);
 	}
 
 	public boolean containsValue(Object value) {
 		return nodeMap.containsValue(value);
 	}
 
 	public IValueReference get(Object key) {
 		return nodeMap.get(key);
 	}
 
 	public IValueReference put(ASTNode key, IValueReference value) {
 		throw new UnsupportedOperationException();
 	}
 
 	public IValueReference remove(Object key) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void putAll(Map<? extends ASTNode, ? extends IValueReference> m) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void clear() {
 		throw new UnsupportedOperationException();
 	}
 
	private static class Views {
 		volatile Set<ASTNode> keySet;
 		volatile Collection<IValueReference> values;
 		volatile Set<Map.Entry<ASTNode, IValueReference>> entrySet;
 	}
 
 	private transient Views views;
 
 	private synchronized Views getViews() {
		if (views != null) {
 			views = new Views();
 		}
 		return views;
 	}
 
 	public Set<ASTNode> keySet() {
 		final Views v = getViews();
 		if (v.keySet == null) {
 			v.keySet = Collections.unmodifiableSet(nodeMap.keySet());
 		}
 		return v.keySet;
 	}
 
 	public Collection<IValueReference> values() {
 		final Views v = getViews();
 		if (v.values == null) {
 			v.values = Collections.unmodifiableCollection(nodeMap.values());
 		}
 		return v.values;
 	}
 
 	public Set<Map.Entry<ASTNode, IValueReference>> entrySet() {
 		final Views v = getViews();
 		if (v.entrySet == null) {
 			v.entrySet = Collections.unmodifiableSet(nodeMap.entrySet());
 		}
 		return v.entrySet;
 	}
 
 }
