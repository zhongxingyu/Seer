 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.edp.adapters;
 
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.wazaabi.engine.edp.EDP;
 import org.eclipse.wazaabi.engine.edp.PathException;
 import org.eclipse.wazaabi.engine.edp.coderesolution.ExecutableAdapter;
 import org.eclipse.wazaabi.engine.edp.exceptions.OperationAborted;
 import org.eclipse.wazaabi.engine.edp.locationpaths.IPointersEvaluator;
 import org.eclipse.wazaabi.mm.edp.EventDispatcher;
 import org.eclipse.wazaabi.mm.edp.events.Event;
 import org.eclipse.wazaabi.mm.edp.handlers.Binding;
 import org.eclipse.wazaabi.mm.edp.handlers.EventHandler;
 import org.eclipse.wazaabi.mm.edp.handlers.Parameter;
 import org.eclipse.wazaabi.mm.edp.handlers.StringParameter;
 
 public class BindingAdapter extends EventHandlerAdapter {
 
 	private static final String BINDING_LOCK_NAME = BindingAdapter.class
 			.getName();
 	public static final String SOURCE_PARAMETER_NAME = "source"; //$NON-NLS-1$
 	public static final String TARGET_PARAMETER_NAME = "target"; //$NON-NLS-1$
 
 	private String sourceParam = null;
 	private String targetParam = null;
 
 	protected void eventPathModified(Event event, String oldPath, String newPath) {
 		// TODO Auto-generated method stub
 
 	}
 
 	protected void eventRemoved(Event event) {
 	}
 
 	protected String getSourceParam() {
 		return sourceParam;
 	}
 
 	protected String getTargetParam() {
 		return targetParam;
 	}
 
 	@Override
 	public void setTarget(Notifier newTarget) {
 		super.setTarget(newTarget);
 		sourceParam = null;
 		targetParam = null;
 		if (newTarget != null)
 			for (Parameter parameter : ((Binding) newTarget).getParameters()) {
 				if (parameter instanceof StringParameter)
 					if (SOURCE_PARAMETER_NAME.equals(parameter.getName()))
 						sourceParam = ((StringParameter) parameter).getValue();
 					else if (TARGET_PARAMETER_NAME
 							.equals(((StringParameter) parameter).getName()))
 						targetParam = ((StringParameter) parameter).getValue();
 				if (targetParam != null && sourceParam != null)
 					break;
 			}
 	}
 
 	@Override
 	public void trigger(Event event) throws OperationAborted {
 		if (getEventDispatcherAdapter() != null
 				&& getEventDispatcherAdapter().isLocked(BINDING_LOCK_NAME))
 			return;
 
 		getEventDispatcherAdapter().lock(BINDING_LOCK_NAME);
 		try {
 			// TODO : may be do we need a better error management
 			assert getTarget() instanceof EventHandler;
 			EventHandler eventHandler = (EventHandler) getTarget();
 			assert eventHandler.eContainer() instanceof EventDispatcher;
 			EventDispatcher eventDispatcher = (EventDispatcher) eventHandler
 					.eContainer();
 
 			for (ConditionAdapter condition : getConditionAdapters()) {
 				boolean canExecute = false;
 				try {
 					canExecute = condition.canExecute(eventDispatcher,
 							eventHandler, event);
 				} catch (RuntimeException e) {
 					throw (OperationAborted) e.getCause();
 				}
 				if (!canExecute) {
 					return;
 				}
 			}
 
 			if (getSourceParam() != null && !"".equals(getSourceParam())
 					&& getTargetParam() != null && !"".equals(getTargetParam())) {
 
 				final IPointersEvaluator pointersEvaluator = getPointersEvaluator();
 
 				if (pointersEvaluator == null) {
 					return;
 
 					// TODO : log this
 				}
 
 				List<?> targetPointers = pointersEvaluator.selectPointers(
 						eventDispatcher, targetParam);
 
 				if (exposeData(eventDispatcher, eventHandler, event,
 						getSourceParam(), getTargetParam(), targetPointers)) {
 					for (ExecutableAdapter executable : getExecutableAdapters()) {
 						if (executable instanceof ConverterAdapter) {
 							try {
 								executable.trigger(eventDispatcher,
 										eventHandler, event);
 							} catch (RuntimeException e) {
 								// e.printStackTrace();
 								throw (OperationAborted) e.getCause();
 							}
 
 						} else if (executable instanceof ValidatorAdapter) {
 							boolean isValid = false;
 							try {
 								isValid = ((ValidatorAdapter) executable)
 										.isValid(eventDispatcher, eventHandler);
 							} catch (RuntimeException e) {
 								throw (OperationAborted) e.getCause();
 							}
 							if (!isValid) {
 								// System.out.println("validation error");
 								throw new OperationAborted(
 										(ValidatorAdapter) executable);
 							}
 						}
 					}
 
 					try {
 						if (eventDispatcher.get(EDP.CONVERTER_RESULT_KEY) != null)
 							doSet(((EventDispatcher) ((EventHandler) getTarget())
 									.eContainer())
 									.get(EDP.CONVERTER_RESULT_KEY),
 									targetPointers);
 						else
 							doSet(eventDispatcher.get(EDP.VALUE_SOURCE_KEY),
 									targetPointers);
 					} catch (OperationAborted e) {
 						System.err
 								.println(e.getCause() + " " + eventDispatcher);
 					}
 					cleanup(eventDispatcher);
 
 				} else
 					; // NOTHING TO DO, WE KEEP THIS AS PLACEHOLDER
 			}
 		} finally {
 			getEventDispatcherAdapter().unlock(BINDING_LOCK_NAME);
 		}
 	}
 
 	protected void cleanup(EventDispatcher eventDispatcher) {
 		eventDispatcher.remove(EDP.CONVERTER_RESULT_KEY);
 		eventDispatcher.remove(EDP.VALUE_SOURCE_KEY);
 		eventDispatcher.remove(EDP.VALUE_TARGET_KEY);
 	}
 
 	protected void doSet(Object sourceValue, List<?> targetPointers) {
 
 		final IPointersEvaluator pointersEvaluator = getPointersEvaluator();
 		Object targetValue = pointersEvaluator.getValue(targetPointers.get(0));
 
 		if (targetValue == null) {
 			if (sourceValue == null)
 				return;
 		} else if (areEquals(targetValue, sourceValue))
 			return;
 		try {
 			pointersEvaluator.setValue2(targetPointers.get(0), sourceValue);
 		} catch (RuntimeException e) {
 			throw new OperationAborted("Binding aborted", e);
 			// TODO : move the catch into trigger() method in order to get more
 			// debug informations to give to users
 		}
 
 	}
 
 	public boolean areEquals(Object object1, Object object2) {
 		if (object1 == null)
 			return object2 == null;
 		else {
 			if (object1 instanceof List<?> && object2 instanceof List<?>)
 				return areEquals((List<?>) object1, (List<?>) object2);
 			else
 				return object1.equals(object2);
 		}
 	}
 
 	public boolean areEquals(List<?> list1, List<?> list2) {
 		if (list1 == null)
 			return list2 == null;
 		if (list2 == null)
 			return false;
 		if (list1.size() != list2.size())
 			return false;
 		for (int i = 0; i < list1.size(); i++) {
 			Object item1 = list1.get(i);
 			Object item2 = list2.get(i);
 
 			if (item1 == null) {
 				if (item2 != null)
 					return false;
 			} else if (!item1.equals(item2))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Puts in dispatcher's context the data required by the binding process and
 	 * potential users like converter or validators.
 	 * 
 	 * @param dispatcher
 	 * @param eventHandler
 	 * @param event
 	 * @param sourceParam
 	 * @param targetParam
 	 * @param targetPointers
 	 * @return
 	 */
 	protected boolean exposeData(EventDispatcher dispatcher,
 			EventHandler eventHandler, Event event, String sourceParam,
 			String targetParam, List<?> targetPointers) {
 
 		final IPointersEvaluator pointersEvaluator = getPointersEvaluator();
 
 		if (pointersEvaluator == null) {
 			return false;
 			// TODO : log this
 		}
 
 		try {
 			List<?> newSourcePointers = null;
 			try {
 				newSourcePointers = pointersEvaluator.selectPointers(
 						dispatcher, sourceParam);
 			} catch (Throwable t) {
 				t.printStackTrace();
 			}
 			// TODO: at the moment, we expect only one returned pointer
 			if (newSourcePointers.size() == 1 && targetPointers.size() == 1) {
 				Object newSourceValue = null;
 				try {
 					newSourceValue = pointersEvaluator
 							.getValue(newSourcePointers.get(0));
 				} catch (Throwable t) {
 					t.printStackTrace();
 				}
 
 				Object targetValue = pointersEvaluator.getValue(targetPointers
 						.get(0));
 
 				dispatcher.set(EDP.VALUE_SOURCE_KEY, newSourceValue);
 				dispatcher.set(EDP.VALUE_TARGET_KEY, targetValue);
 				return true;
 			}
 		} catch (PathException e) {
 			// TODO : log that
 			System.err.println(e.getMessage());
 		}
 		return false;
 	}
 
 }
