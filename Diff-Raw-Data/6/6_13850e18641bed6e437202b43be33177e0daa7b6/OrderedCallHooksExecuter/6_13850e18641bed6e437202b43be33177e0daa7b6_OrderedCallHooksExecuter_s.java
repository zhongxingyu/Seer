 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.communication.core.factory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.riena.communication.core.hooks.CallContext;
 import org.eclipse.riena.communication.core.hooks.ICallHook;
 import org.eclipse.riena.core.util.Iter;
 import org.eclipse.riena.core.util.Orderer;
 import org.eclipse.riena.core.wire.InjectExtension;
 
 /**
  * A {@code ICallHook} that executes the ordered call hooks.
  */
 public class OrderedCallHooksExecuter implements ICallHook {
 
 	private List<ICallHook> orderedCallHooks;
 	private List<ICallHook> reversedCallHooks;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void beforeCall(final CallContext context) {
 		for (final ICallHook sHook : orderedCallHooks) {
 			sHook.beforeCall(context);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void afterCall(final CallContext context) {
 		for (final ICallHook sHook : reversedCallHooks) {
			sHook.beforeCall(context);
 		}
 	}
 
 	@InjectExtension
 	public void update(final ICallHookExtension[] callHookExtensions) {
 		final Orderer<ICallHook> orderer = new Orderer<ICallHook>();
 		for (final ICallHookExtension extension : callHookExtensions) {
			orderer.add(extension.getCallHook(), extension.getName(), extension.getPostHooks(),
					extension.getPostHooks());
 		}
 		final List<ICallHook> tempOrdered = orderer.getOrderedObjects();
 		final List<ICallHook> tempReverse = new ArrayList<ICallHook>(tempOrdered.size());
 		for (final ICallHook hook : Iter.ableReverse(tempOrdered)) {
 			tempReverse.add(hook);
 		}
 		synchronized (this) {
 			this.orderedCallHooks = tempOrdered;
 			this.reversedCallHooks = tempReverse;
 		}
 	}
 
 }
