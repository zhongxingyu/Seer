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
 package org.eclipse.riena.ui.ridgets.swt;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.uibinding.DefaultBindingManager;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingManager;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 
 /**
  * Factory creating ridgets for SWT controls.
  * 
  * @see #createRidget(Control)
  */
 public final class SwtRidgetFactory {
 
 	static {
 		final Display display = Display.getCurrent();
 		Assert.isNotNull(display);
 		new DefaultRealm(display);
 	}
 
 	private static final IBindingManager DUMMY_BINDING_MAN = new DefaultBindingManager(
 			new DummyBindingPropertyLocator(), SwtControlRidgetMapper.getInstance());
 
 	private static final DummyContainer CONTAINER = new DummyContainer();
 
 	private static final List<Object> CONTROL_LIST = new ArrayList<Object>(1);
 
 	public static IRidget createRidget(final Object control) {
 		Assert.isNotNull(control);
 		CONTAINER.clear();
 		CONTROL_LIST.clear();
 		CONTROL_LIST.add(control);
 		DUMMY_BINDING_MAN.injectRidgets(CONTAINER, CONTROL_LIST);
 		DUMMY_BINDING_MAN.bind(CONTAINER, CONTROL_LIST);
 		final IRidget result = CONTAINER.getRidget(null);
 		return result;
 	}
 
 	private SwtRidgetFactory() {
 		// prevent instantiation
 	}
 
 	// helping classes
 	// ////////////////
 
 	private static final class DummyBindingPropertyLocator implements IBindingPropertyLocator {
 
 		private static final SWTBindingPropertyLocator DELEGATE = SWTBindingPropertyLocator.getInstance();
 
 		/*
 		 * Find the binding property in the uiControl. If none is available
 		 * return 'dummy', since in that case we use the DummyContainer which
 		 * does not need an id (see DummyContainer#getRidget(...)).
 		 */
 		public String locateBindingProperty(final Object uiControl) {
 			final String bindingProp = DELEGATE.locateBindingProperty(uiControl);
 			return bindingProp != null ? bindingProp : "dummy"; //$NON-NLS-1$
 		}
 	}
 
 	private static final class DummyContainer implements IRidgetContainer {
 		private final Map<String, IRidget> ridgets = new HashMap<String, IRidget>();
 		private IRidget ridget;
 
 		public void addRidget(final String id, final IRidget ridget) {
			if (id != null && !"dummy".equals(id)) { //$NON-NLS-1$
 				ridgets.put(id, ridget);
 			}
			this.ridget = ridget;
 		}
 
 		public void configureRidgets() {
 			// nothing
 		}
 
 		@SuppressWarnings("unchecked")
 		public <R extends IRidget> R getRidget(final String id) {
			if (id == null || "dummy".equals(id)) { //$NON-NLS-1$
 				return (R) ridget;
 			}
 			return (R) ridgets.get(id);
 		}
 
 		public <R extends IRidget> R getRidget(final Class<R> ridgetClazz, final String id) {
 			return getRidget(id);
 		}
 
 		public Collection<? extends IRidget> getRidgets() {
 			return ridgets.values();
 		}
 
 		void clear() {
 			this.ridget = null;
 		}
 
 	}
 
 }
