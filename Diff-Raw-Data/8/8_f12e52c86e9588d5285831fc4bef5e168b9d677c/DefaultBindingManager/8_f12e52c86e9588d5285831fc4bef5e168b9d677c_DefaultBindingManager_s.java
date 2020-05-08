 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.uibinding;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
import org.eclipse.core.runtime.Assert;

 import org.eclipse.riena.core.util.ReflectionFailure;
 import org.eclipse.riena.core.wire.Wire;
 import org.eclipse.riena.internal.ui.ridgets.Activator;
 import org.eclipse.riena.ui.common.IComplexComponent;
 import org.eclipse.riena.ui.ridgets.IComplexRidget;
 import org.eclipse.riena.ui.ridgets.ILabelRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 
 /**
  * This class manages the binding between UI-control and ridget. In contrast to
  * the {@link InjectBindingManager} which calls a setter method for each ridget
  * immediately after ridget creation and addition to the
  * {@link IRidgetContainer} managed ridget collection this
  * {@link IBindingManager} implementation only calls the method
  * {@link IInjectAllRidgets#configureRidgets()} once. Therefore the
  * {@link IRidgetContainer} is required to interface {@link IInjectAllRidgets}
  * if using this binding policy. The binding policy is configured in the view to
  * be bound.
  */
 public class DefaultBindingManager implements IBindingManager {
 
 	private final IBindingPropertyLocator propertyStrategy;
 	private final IControlRidgetMapper<Object> mapper;
 
 	/**
 	 * Creates the managers of all bindings of a view.
 	 * 
 	 * @param propertyStrategy
 	 *            strategy to get the property for the binding from the
 	 *            UI-control.
 	 * @param mapper
 	 *            mapping for UI control-classes to ridget-classes
 	 */
 	public DefaultBindingManager(final IBindingPropertyLocator propertyStrategy,
 			final IControlRidgetMapper<Object> mapper) {
 		this.propertyStrategy = propertyStrategy;
 		this.mapper = mapper;
 	}
 
 	public void injectRidgets(final IRidgetContainer ridgetContainer, final List<Object> uiControls) {
 		final CorrespondingLabelMapper ridgetMapper = new CorrespondingLabelMapper(ridgetContainer);
 		if (Activator.getDefault() != null) {
 			Wire.instance(ridgetMapper).andStart(Activator.getDefault().getContext());
 		}
 
 		final Map<String, IRidget> controls = new HashMap<String, IRidget>();
 
 		for (final Object control : uiControls) {
 			final String bindingProperty = propertyStrategy.locateBindingProperty(control);
 			if (bindingProperty != null) {
 				final IRidget ridget = createRidget(control);
 				injectRidget(ridgetContainer, bindingProperty, ridget);
 
 				//because the ridgets are not bound yet, we have to save the bindingProperty separately
 				if (!(ridget instanceof ILabelRidget)) {
 					controls.put(bindingProperty, ridget);
 				}
 
 				if (control instanceof IComplexComponent) {
 					final IComplexRidget complexRidget = (IComplexRidget) ridget;
 					final IComplexComponent complexComponent = (IComplexComponent) control;
 					injectRidgets(complexRidget, complexComponent.getUIControls());
 				}
 			}
 		}
 
 		// iterate over all controls that are not ILabelRidgets and try to connect 
 		// them with their corresponding Label
 		final Iterator<Entry<String, IRidget>> it = controls.entrySet().iterator();
 		while (it.hasNext()) {
 			final Entry<String, IRidget> entry = it.next();
 			ridgetMapper.connectCorrespondingLabel(entry.getValue(), entry.getKey());
 		}
 
 		if (Activator.getDefault() != null) {
 			// TODO This unveils a weakness of the wiring stuff because the dependency (to the wiring) is just moved the ridget containers to here :-(
 			Wire.instance(ridgetContainer).andStart(Activator.getDefault().getContext());
 		}
 
 		ridgetContainer.configureRidgets();
 	}
 
 	/**
 	 * Injects the given ridget into the given container.<br>
 	 * Adds the ridget to the container.
 	 * 
 	 * @param ridgetContainer
 	 * @param bindingProperty
 	 * @param ridget
 	 *            ridget to inject
 	 */
 	protected void injectRidget(final IRidgetContainer ridgetContainer, final String bindingProperty,
 			final IRidget ridget) {
 		ridgetContainer.addRidget(bindingProperty, ridget);
 		ridget.setController(ridgetContainer);
 	}
 
 	/**
 	 * Creates for the given UI-control the appropriate ridget.
 	 * 
 	 * @param control
 	 *            UI-control
 	 * @return ridget
 	 * @throws ReflectionFailure
 	 */
 	public IRidget createRidget(final Object control) throws ReflectionFailure {
 		final Class<? extends IRidget> ridgetClass = mapper.getRidgetClass(control);
 		try {
 			return ridgetClass.newInstance();
 		} catch (final Exception e) {
 			throw new ReflectionFailure(String.format("Could not instantiate ridget '%s' for control '%s'", //$NON-NLS-1$
 					ridgetClass, control), e);
 		}
 	}
 
 	/**
 	 * Returns form the given ridget container the ridget with the given
 	 * property value.
 	 * 
 	 * @param bindingProperty
 	 *            value of the binding property
 	 * @param controller
 	 *            ridget container
 	 * @return ridget
 	 */
 	protected <R extends IRidget> R getRidget(final String bindingProperty, final IRidgetContainer controller) {
 		return controller.getRidget(bindingProperty);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.internal.ridgets.uibinding.IBindingManager#bind(IRidgetContainer,
 	 *      java.util.List)
 	 */
 	public void bind(final IRidgetContainer controller, final List<Object> uiControls) {
 		updateBindings(controller, uiControls, false);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.internal.ridgets.uibinding.IBindingManager#unbind(IRidgetContainer,
 	 *      java.util.List)
 	 */
 	public void unbind(final IRidgetContainer controller, final List<Object> uiControls) {
 		updateBindings(controller, uiControls, true);
 	}
 
 	private void updateBindings(final IRidgetContainer controller, final List<Object> uiControls, final boolean unbind) {
 
 		for (final Object control : uiControls) {
 			final String bindingProperty = propertyStrategy.locateBindingProperty(control);
 			if (bindingProperty == null) {
 				continue;
 			}
 			if (control instanceof IComplexComponent) {
 				final IComplexComponent complexComponent = (IComplexComponent) control;
 				final IComplexRidget complexRidget = getRidget(bindingProperty, controller);
 				if (complexRidget != null) {
 					updateBindings(complexRidget, complexComponent.getUIControls(), unbind);
 					bindRidget(complexRidget, complexComponent, unbind);
 				}
 			} else {
 				final IRidget ridget = getRidget(bindingProperty, controller);
				Assert.isNotNull(ridget, "Null ridget for property: " + bindingProperty); //$NON-NLS-1$
				bindRidget(ridget, control, unbind);
 			}
 		}
 	}
 
 	private void bindRidget(final IRidget ridget, final Object uiControl, final boolean unbind) {
 
 		if (unbind) {
 			ridget.setUIControl(null);
 		} else {
 			ridget.setUIControl(uiControl);
 		}
 	}
 
 }
