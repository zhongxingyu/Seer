 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.xmof.vm;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.modelexecution.fuml.convert.IConversionResult;
 import org.modelexecution.fuml.convert.xmof.XMOFConverter;
 import org.modelexecution.fumldebug.core.ExecutionContext;
 import org.modelexecution.fumldebug.core.ExecutionEventListener;
 import org.modelexecution.fumldebug.core.event.ActivityEntryEvent;
 import org.modelexecution.fumldebug.core.event.ActivityExitEvent;
 import org.modelexecution.fumldebug.core.event.ActivityNodeEntryEvent;
 import org.modelexecution.fumldebug.core.event.ActivityNodeExitEvent;
 import org.modelexecution.fumldebug.core.event.Event;
 import org.modelexecution.fumldebug.core.event.SuspendEvent;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.Activity;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.MainEClass;
 import org.modelexecution.xmof.vm.XMOFVirtualMachineEvent.Type;
 
import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.Classes.Kernel.Reference;
 import fUML.Semantics.Classes.Kernel.Value;
 import fUML.Semantics.Classes.Kernel.ValueList;
 import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValue;
 import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList;
 import fUML.Syntax.Actions.BasicActions.CallBehaviorAction;
 import fUML.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
 import fUML.Syntax.Activities.IntermediateActivities.DecisionNode;
 import fUML.Syntax.Classes.Kernel.Parameter;
 import fUML.Syntax.CommonBehaviors.BasicBehaviors.Behavior;
 import fUML.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior;
 
 /**
  * The virtual machine for executing {@link XMOFBasedModel xMOF-based models}.
  * 
  * @author Philip Langer (langer@big.tuwien.ac.at)
  * 
  */
 public class XMOFVirtualMachine implements ExecutionEventListener {
 
 	private final ExecutionContext executionContext = ExecutionContext
 			.getInstance();
 
 	private XMOFBasedModel model;
 	private IConversionResult xMOFConversionResult;
 	private XMOFInstanceMap instanceMap;
 
 	private Collection<IXMOFVirtualMachineListener> vmListener;
 	private Collection<ExecutionEventListener> rawListener;
 
 	private boolean isSynchronizeModel = false;
 	private boolean isRunning = false;
 
 	public int executionID = -1;
 
 	private XMOFBasedModelSynchronizer modelSynchronizer;
 
 	public XMOFVirtualMachine(XMOFBasedModel modelToBeExecuted) {
 		super();
 		this.model = modelToBeExecuted;
 		initialize();
 	}
 
 	private void initialize() {
 		initializeListeners();
 		convertMetamodel();
 		initializeInstanceMap();
 		replaceOpaqueBehaviors();
 		initializeModelSynchronizer();
 	}
 
 	private void initializeListeners() {
 		vmListener = new HashSet<IXMOFVirtualMachineListener>();
 		rawListener = new HashSet<ExecutionEventListener>();
 	}
 
 	private void initializeInstanceMap() {
 		this.instanceMap = new XMOFInstanceMap(xMOFConversionResult,
 				model.getModelElements(), executionContext.getLocus());
 	}
 
 	private void convertMetamodel() {
 		XMOFConverter xMOFConverter = new XMOFConverter();
 		if (xMOFConverter.canConvert(getMetamodelPackage())) {
 			xMOFConversionResult = xMOFConverter.convert(getMetamodelPackage());
 		}
 	}
 
 	private EPackage getMetamodelPackage() {
 		return model.getMetamodelPackages().get(0);
 	}
 
 	private void replaceOpaqueBehaviors() {
 		List<ActivityNode> nodesWithBehavior = new ArrayList<ActivityNode>();
 		for (fUML.Syntax.Activities.IntermediateActivities.Activity activity : xMOFConversionResult
 				.getAllActivities()) {
 			nodesWithBehavior.addAll(getBehaviorNodes(activity.node));
 		}
 
 		for (ActivityNode node : nodesWithBehavior) {
 			if (node instanceof CallBehaviorAction) {
 				CallBehaviorAction callBehaviorAction = (CallBehaviorAction) node;
 				Behavior behavior = callBehaviorAction.behavior;
 				OpaqueBehavior behaviorReplacement = executionContext
 						.getOpaqueBehavior(behavior.name);
 				if (behaviorReplacement != null) {
 					callBehaviorAction.behavior = behaviorReplacement;
 				}
 			} else if (node instanceof DecisionNode) {
 				DecisionNode decision = (DecisionNode) node;
 				Behavior behavior = decision.decisionInput;
 				OpaqueBehavior behaviorReplacement = executionContext
 						.getOpaqueBehavior(behavior.name);
 				if (behaviorReplacement != null) {
 					decision.decisionInput = behaviorReplacement;
 				}
 			}
 		}
 	}
 
 	private List<ActivityNode> getBehaviorNodes(List<ActivityNode> nodes) {
 		List<ActivityNode> nodesWithBehavior = new ArrayList<ActivityNode>();
 		for (ActivityNode node : nodes) {
 			if (node instanceof CallBehaviorAction) {
 				CallBehaviorAction action = (CallBehaviorAction) node;
 				nodesWithBehavior.add(action);
 			} else if (node instanceof DecisionNode) {
 				DecisionNode decision = (DecisionNode) node;
 				if (decision.decisionInput != null) {
 					nodesWithBehavior.add(decision);
 				}
 			}
 			if (node instanceof StructuredActivityNode) {
 				StructuredActivityNode structurednode = (StructuredActivityNode) node;
 				nodesWithBehavior.addAll(getBehaviorNodes(structurednode.node));
 			}
 		}
 		return nodesWithBehavior;
 	}
 
 	private void initializeModelSynchronizer() {
 		modelSynchronizer = new XMOFBasedModelSynchronizer(instanceMap,
 				model.getEditingDomain());
 		modelSynchronizer.setModelResource(model.getModelResource());
 	}
 
 	public void setSynchronizeModel(boolean isSynchronizeModel) {
 		this.isSynchronizeModel = isSynchronizeModel;
 	}
 
 	public boolean isSynchronizeModel() {
 		return isSynchronizeModel;
 	}
 
 	public void addVirtualMachineListener(IXMOFVirtualMachineListener listener) {
 		vmListener.add(listener);
 	}
 
 	public void removeVirtualMachineListener(
 			IXMOFVirtualMachineListener listener) {
 		vmListener.remove(listener);
 	}
 
 	public void addRawExecutionEventListener(ExecutionEventListener listener) {
 		rawListener.add(listener);
 	}
 
 	public void removeRawExecutionEventListener(ExecutionEventListener listener) {
 		rawListener.remove(listener);
 	}
 
 	public XMOFBasedModel getModel() {
 		return model;
 	}
 
 	public ExecutionContext getRawExecutionContext() {
 		return executionContext;
 	}
 
 	public boolean mayRun() {
 		return isXMOFConversionOK();
 	}
 
 	private boolean isXMOFConversionOK() {
 		return xMOFConversionResult != null
 				&& !xMOFConversionResult.hasErrors();
 	}
 
 	private void startListeningToRawEvents() {
 		executionContext.addEventListener(this);
 	}
 
 	private void stopListeningToRawEvents() {
 		executionContext.addEventListener(this);
 	}
 
 	public void run() {
 		prepareForExecution();
 		executeAllMainObjects();
 		cleanUpAfterExecution();
 	}
 
 	public void run(Activity activity, EObject contextObject,
 			List<ActivityParameterBinding> parameterBindings) {
 		prepareForExecution();
 		executeBehavior(activity, contextObject, parameterBindings);
 		cleanUpAfterExecution();
 	}
 
 	private void prepareForExecution() {
 		isRunning = true;
 		notifyVirtualMachineListenerStart();
 		startListeningToRawEvents();
 		installModelSynchronizerIfSet();
 	}
 
 	private void cleanUpAfterExecution() {
 		uninstallModelSynchronizer();
 		stopListeningToRawEvents();
 		notifyVirtualMachineListenerStop();
 		isRunning = false;
 	}
 
 	private void executeBehavior(Activity activity, EObject contextObject,
 			List<ActivityParameterBinding> parameterBindings) {
 		try {
 			executionContext.execute((Behavior) this.xMOFConversionResult
 					.getFUMLElement(activity), instanceMap
 					.getObject(contextObject),
 					convertToParameterValueList(parameterBindings));
 		} catch (Exception e) {
 			notifyVirtualMachineListenerError(e);
 		}
 	}
 
 	private void notifyVirtualMachineListenerError(Exception exception) {
 		XMOFVirtualMachineEvent event = new XMOFVirtualMachineEvent(this,
 				exception);
 		notifyVirtualMachineListener(event);
 	}
 
 	private ParameterValueList convertToParameterValueList(
 			List<ActivityParameterBinding> parameterBindings) {
 		ParameterValueList list = new ParameterValueList();
 		if (parameterBindings == null)
 			return list;
 		for (ActivityParameterBinding binding : parameterBindings) {
 			list.add(createParameterValue(binding));
 		}
 		return list;
 	}
 
 	private ParameterValue createParameterValue(ActivityParameterBinding binding) {
 		ParameterValue parameterValue = new ParameterValue();
 		parameterValue.parameter = (Parameter) xMOFConversionResult
 				.getFUMLElement(binding.getParameter());
 		parameterValue.values = createParameterValues(binding.getValues(), binding.getParameter().getEType());
 		return parameterValue;
 	}
 
 	private ValueList createParameterValues(List<Object> values, EClassifier parameterType) {
 		ValueList parameterValues = new ValueList();
 		if(values != null) {
 			for(Object value : values) {
 				Value parameterValue = instanceMap.getValue(value, parameterType);
 				if(parameterValue != null) {
					if(parameterValue instanceof Object_) {
						Reference reference = new Reference();
						reference.referent = (Object_)parameterValue;
						parameterValue = reference;
					}
 					parameterValues.add(parameterValue);
 				}
 			}
 		}
 		return parameterValues;
 	}
 	
 	private void notifyVirtualMachineListenerStart() {
 		XMOFVirtualMachineEvent event = new XMOFVirtualMachineEvent(Type.START,
 				this);
 		notifyVirtualMachineListener(event);
 	}
 
 	private void installModelSynchronizerIfSet() {
 		if (isSynchronizeModel) {
 			addRawExecutionEventListener(modelSynchronizer);
 		} else {
 			removeRawExecutionEventListener(modelSynchronizer);
 		}
 	}
 
 	private void uninstallModelSynchronizer() {
 		removeRawExecutionEventListener(modelSynchronizer);
 	}
 
 	private void notifyVirtualMachineListenerStop() {
 		XMOFVirtualMachineEvent event = new XMOFVirtualMachineEvent(Type.STOP,
 				this);
 		notifyVirtualMachineListener(event);
 	}
 
 	private void notifyVirtualMachineListener(XMOFVirtualMachineEvent event) {
 		for (IXMOFVirtualMachineListener listener : new ArrayList<IXMOFVirtualMachineListener>(
 				vmListener)) {
 			try {
 				listener.notify(event);
 			} catch (Exception e) {
 				// ignore exception thrown by listeners
 			}
 		}
 	}
 
 	private void executeAllMainObjects() {
 		for (EObject mainClassObject : model.getMainEClassObjects()) {
 			executeBehavior(getClassifierBehavior(mainClassObject),
 					mainClassObject, null);
 		}
 	}
 
 	private Activity getClassifierBehavior(EObject mainClassObject) {
 		EClass mainEClassInstance = mainClassObject.eClass();
 		Assert.isTrue(XMOFBasedModel.MAIN_E_CLASS
 				.isInstance(mainEClassInstance));
 		MainEClass mainEClass = (MainEClass) mainEClassInstance;
 		return (Activity) mainEClass.getClassifierBehavior();
 	}
 
 	@Override
 	public void notify(Event event) {
 		if (isRunning() && concernsCurrentExecution(event)) {
 			debugPrint(event);
 			notifyRawExecutionEventListeners(event);
 		}
 	}
 
 	private boolean concernsCurrentExecution(Event event) {
 		// TODO find out if this event concerns current execution
 		return true;
 	}
 
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	public XMOFInstanceMap getInstanceMap() {
 		return instanceMap;
 	}
 
 	private void notifyRawExecutionEventListeners(Event event) {
 		for (ExecutionEventListener listener : new ArrayList<ExecutionEventListener>(
 				rawListener)) {
 			listener.notify(event);
 		}
 	}
 
 	private void debugPrint(Event event) {
 		if (event instanceof ActivityEntryEvent) {
 			ActivityEntryEvent activityEntry = (ActivityEntryEvent) event;
 			executionID = activityEntry.getActivityExecutionID();
 			System.out.println("Activity Entry: "
 					+ activityEntry.getActivity().name);
 		} else if (event instanceof ActivityExitEvent) {
 			ActivityExitEvent activityExit = (ActivityExitEvent) event;
 			System.out.println("Activity Exit: "
 					+ activityExit.getActivity().name);
 		} else if (event instanceof ActivityNodeEntryEvent) {
 			ActivityNodeEntryEvent nodeEntry = (ActivityNodeEntryEvent) event;
 			System.out.println("Node Entry: " + nodeEntry.getNode().name + " ("
 					+ nodeEntry.getNode().getClass().getName() + ")");
 		} else if (event instanceof ActivityNodeExitEvent) {
 			ActivityNodeExitEvent nodeExit = (ActivityNodeExitEvent) event;
 			System.out.println("Node Exit: " + nodeExit.getNode().name + " ("
 					+ nodeExit.getNode().getClass().getName() + ")");
 		} else if (event instanceof SuspendEvent) {
 			SuspendEvent suspendEvent = (SuspendEvent) event;
 			if (suspendEvent.getLocation() instanceof fUML.Syntax.Activities.IntermediateActivities.Activity) {
 				System.out
 						.println("Suspend: "
 								+ ((fUML.Syntax.Activities.IntermediateActivities.Activity) suspendEvent
 										.getLocation()).name);
 			} else if (suspendEvent.getLocation() instanceof ActivityNode) {
 				System.out.println("Suspend: "
 						+ ((ActivityNode) suspendEvent.getLocation()).name
 						+ "("
 						+ ((ActivityNode) suspendEvent.getLocation())
 								.getClass().getName() + ")");
 			} else {
 				System.out.println("Suspend: " + suspendEvent.getLocation());
 			}
 		}
 	}
 
 }
