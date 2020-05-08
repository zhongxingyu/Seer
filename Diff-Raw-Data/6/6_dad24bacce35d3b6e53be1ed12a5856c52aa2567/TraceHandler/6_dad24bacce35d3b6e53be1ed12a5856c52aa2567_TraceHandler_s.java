 /*
  * Copyright (c) 2013 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Tanja Mayerhofer - initial API and implementation
  */
 package org.modelexecution.fumldebug.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.modelexecution.fumldebug.core.event.ActivityEntryEvent;
 import org.modelexecution.fumldebug.core.event.ActivityExitEvent;
 import org.modelexecution.fumldebug.core.event.ActivityNodeEntryEvent;
 import org.modelexecution.fumldebug.core.event.ActivityNodeExitEvent;
 import org.modelexecution.fumldebug.core.event.Event;
 import org.modelexecution.fumldebug.core.event.ExtensionalValueEvent;
 import org.modelexecution.fumldebug.core.event.ExtensionalValueEventType;
 import org.modelexecution.fumldebug.core.event.SuspendEvent;
 import org.modelexecution.fumldebug.core.event.TraceEvent;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ActionExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ActivityNodeExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.CallActionExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ControlNodeExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ControlTokenInstance;
 import org.modelexecution.fumldebug.core.trace.tracemodel.DecisionNodeExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.InitialNodeExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.Input;
 import org.modelexecution.fumldebug.core.trace.tracemodel.InputParameterSetting;
 import org.modelexecution.fumldebug.core.trace.tracemodel.InputParameterValue;
 import org.modelexecution.fumldebug.core.trace.tracemodel.InputValue;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ObjectTokenInstance;
 import org.modelexecution.fumldebug.core.trace.tracemodel.Output;
 import org.modelexecution.fumldebug.core.trace.tracemodel.OutputParameterSetting;
 import org.modelexecution.fumldebug.core.trace.tracemodel.OutputParameterValue;
 import org.modelexecution.fumldebug.core.trace.tracemodel.OutputValue;
 import org.modelexecution.fumldebug.core.trace.tracemodel.StructuredActivityNodeExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.TokenInstance;
 import org.modelexecution.fumldebug.core.trace.tracemodel.Trace;
 import org.modelexecution.fumldebug.core.trace.tracemodel.TracemodelFactory;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ValueInstance;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ValueSnapshot;
 import org.modelexecution.fumldebug.core.trace.tracemodel.impl.TraceImpl;
 
 import fUML.Semantics.Actions.BasicActions.ActionActivation;
 import fUML.Semantics.Actions.BasicActions.PinActivation;
 import fUML.Semantics.Activities.IntermediateActivities.ActivityEdgeInstance;
 import fUML.Semantics.Activities.IntermediateActivities.ActivityExecution;
 import fUML.Semantics.Activities.IntermediateActivities.ActivityNodeActivation;
 import fUML.Semantics.Activities.IntermediateActivities.ActivityParameterNodeActivation;
 import fUML.Semantics.Activities.IntermediateActivities.ControlToken;
 import fUML.Semantics.Activities.IntermediateActivities.DecisionNodeActivation;
 import fUML.Semantics.Activities.IntermediateActivities.ForkedToken;
 import fUML.Semantics.Activities.IntermediateActivities.InitialNodeActivation;
 import fUML.Semantics.Activities.IntermediateActivities.ObjectToken;
 import fUML.Semantics.Activities.IntermediateActivities.Token;
 import fUML.Semantics.Activities.IntermediateActivities.TokenList;
 import fUML.Semantics.Classes.Kernel.ExtensionalValue;
 import fUML.Semantics.Classes.Kernel.Object_;
 import fUML.Semantics.Classes.Kernel.Reference;
 import fUML.Semantics.Classes.Kernel.Value;
 import fUML.Semantics.Classes.Kernel.ValueList;
 import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValue;
 import fUML.Syntax.Actions.BasicActions.Action;
 import fUML.Syntax.Actions.BasicActions.CallAction;
 import fUML.Syntax.Actions.BasicActions.InputPin;
 import fUML.Syntax.Actions.BasicActions.OutputPin;
 import fUML.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode;
 import fUML.Syntax.Activities.IntermediateActivities.Activity;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityEdge;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityParameterNode;
 import fUML.Syntax.Activities.IntermediateActivities.ControlNode;
 import fUML.Syntax.Activities.IntermediateActivities.DecisionNode;
 import fUML.Syntax.Activities.IntermediateActivities.InitialNode;
 import fUML.Syntax.Classes.Kernel.Parameter;
 
 /**
  * @author Tanja
  *
  */
 public class TraceHandler implements ExecutionEventListener {
 
 	protected static final TracemodelFactory TRACE_FACTORY = TracemodelFactory.eINSTANCE;
 	
 	private HashMap<Integer, Trace> activityExecutionTrace = new HashMap<Integer, Trace>();
 	
 	/* (non-Javadoc)
 	 * @see org.modelexecution.fumldebug.core.ExecutionEventListener#notify(org.modelexecution.fumldebug.core.event.Event)
 	 */
 	public void notify(Event event) {
 		if(event instanceof TraceEvent) {
 			if(event instanceof ActivityEntryEvent) {
 				ActivityEntryEvent activityEntryEvent = (ActivityEntryEvent)event;
 				traceHandleActivityEntryEvent(activityEntryEvent);													
 			} else if (event instanceof ActivityExitEvent) {	
 				ActivityExitEvent activityExitEvent = (ActivityExitEvent)event;
 				traceHandleActivityExitEvent(activityExitEvent);
 			} else if (event instanceof ActivityNodeEntryEvent) {
 				ActivityNodeEntryEvent nodeEntryEvent = (ActivityNodeEntryEvent)event;
 				traceHandleActivityNodeEntryEvent(nodeEntryEvent);
 			} else if(event instanceof ActivityNodeExitEvent) {
 				ActivityNodeExitEvent nodeExitEvent = (ActivityNodeExitEvent)event;				
 				traceHandleActivityNodeExitEvent(nodeExitEvent);
 			} else if(event instanceof SuspendEvent) {
 				SuspendEvent suspendEvent = (SuspendEvent)event;				
 				traceHandleSuspendEvent(suspendEvent);				
 			}
 		} else if(event instanceof ExtensionalValueEvent){
 			traceHandleExtensionalValueEvent((ExtensionalValueEvent)event);
 		}
 	}
 	
 	public Trace getTrace(int executionID) {
 		int rootExecutionID = ExecutionContext.getInstance().executionStatus.getRootExecutionID(executionID);
 		return activityExecutionTrace.get(rootExecutionID);		 
 	}
 	
 	private void traceHandleActivityEntryEvent(ActivityEntryEvent event) {
 		int executionID = event.getActivityExecutionID();		
 		Activity activity = event.getActivity();		
 		
 		Trace trace = null; 
 		if(event.getParent() == null) {	// create new trace
 			trace = new TraceImpl();
 			initializeTraceWithObjectsAtLocus(trace);
 			activityExecutionTrace.put(executionID, trace);					
 		} else { // get existing trace
 			trace = getTrace(executionID);
 		}
 		
 		// add activity execution to trace
 		org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution activityExecution = trace.addActivityExecution(activity, executionID);
 		
 		// set caller of activity execution
 		if(event.getParent() != null && event.getParent() instanceof ActivityNodeEntryEvent) {
 			ActivityNodeEntryEvent parentevent = (ActivityNodeEntryEvent)event.getParent();
 			int parentexeID = parentevent.getActivityExecutionID();
 			org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution parentExecution = trace.getActivityExecutionByID(parentexeID);
  			
 			if(parentExecution != null && parentevent.getNode() instanceof CallAction) { 
 				//CallActionExecution callerNodeExecution = parentExecution.getActiveCallActionExecution((CallAction)parentevent.getNode());
 				ActivityNodeExecution callActionAcitivtyNodeExecution = parentExecution.getExecutionForEnabledNode(parentevent.getNode());
 				if(callActionAcitivtyNodeExecution != null && callActionAcitivtyNodeExecution instanceof CallActionExecution) {
 					CallActionExecution callerNodeExecution = (CallActionExecution)callActionAcitivtyNodeExecution;
 					activityExecution.setCaller(callerNodeExecution);
 					callerNodeExecution.setCallee(activityExecution);
 				}				
 			}			
 		}
 	}
 
 	private void traceHandleActivityExitEvent(ActivityExitEvent event) {
 		// add activity outputs to trace
 		int executionID = event.getActivityExecutionID();	
 		ActivityExecutionStatus activityExecutionStatus = ExecutionContext.getInstance().executionStatus.getActivityExecutionStatus(executionID);	
 		ActivityExecution execution = activityExecutionStatus.getActivityExecution();
 				
 		Trace trace = getTrace(executionID);
 		org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution activityExecution = trace.getActivityExecutionByID(executionID);	
 		
 		List<Parameter> outputParametersWithoutParameterNode = new ArrayList<Parameter>();
 		outputParametersWithoutParameterNode.addAll(activityExecution.getOutputParameters()); 
 					
 		List<ActivityParameterNode> outputActivityParameterNodes = activityExecution.getOutputActivityParameterNodes();
 		for(ActivityParameterNode outputActivityParameterNode : outputActivityParameterNodes) {
 			ActivityParameterNodeActivation outputActivityParameterNodeActivation = (ActivityParameterNodeActivation)execution.activationGroup.getNodeActivation(outputActivityParameterNode);
 			TokenList heldTokens = outputActivityParameterNodeActivation.heldTokens;
 
 			outputParametersWithoutParameterNode.remove(outputActivityParameterNode.parameter);
 			OutputParameterSetting outputParameterSetting = TRACE_FACTORY.createOutputParameterSetting();
 			outputParameterSetting.setParameter(outputActivityParameterNode.parameter);
 			for(Token token : heldTokens) {
 				Token originalToken = activityExecutionStatus.getOriginalToken(token);
 				TokenInstance tokenInstance = activityExecutionStatus.getTokenInstance(originalToken);
 				if(tokenInstance != null && tokenInstance instanceof ObjectTokenInstance) {
 					ObjectTokenInstance otokenInstance = (ObjectTokenInstance)tokenInstance;
 
 					List<ActivityEdge> traversedEdges = activityExecutionStatus.getTraversedActivityEdges(originalToken);
 					List<ActivityEdge> traversedEdgesForNode = getTraversedEdge(traversedEdges, outputActivityParameterNode); 
 					otokenInstance.getTraversedEdges().addAll(traversedEdgesForNode);
 
 					OutputParameterValue outputParameterValue = createOutputParameterValue(trace, otokenInstance);
 					outputParameterSetting.getParameterValues().add(outputParameterValue);
 				}
 			}				
 			activityExecution.getActivityOutputs().add(outputParameterSetting);
 
 		}
 					
 		for(Parameter inputParameter : outputParametersWithoutParameterNode) {
 			ParameterValue parameterValue = execution.getParameterValue(inputParameter);
 			if(parameterValue != null) {
 				InputParameterSetting parameterSetting = createInputParameterSetting(activityExecution, inputParameter, parameterValue.values);
 				activityExecution.getActivityInputs().add(parameterSetting);
 			}
 		}
 	}
 
 	private void traceHandleActivityNodeEntryEvent(ActivityNodeEntryEvent event) {
 		int executionID = event.getActivityExecutionID();
 		ActivityNode node = event.getNode();
 		
 		Trace trace = getTrace(executionID);
 		org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution traceActivityExecution = trace.getActivityExecutionByID(executionID);
 
 		ActivityNodeExecution traceCurrentNodeExecution = traceActivityExecution.getExecutionForEnabledNode(node);
 		
 		// There should only be one execution for one node in the trace that has not been finished yet
 		// Otherwise, the inputs have to be taken into consideration
 		if(traceCurrentNodeExecution == null) { // TODO there is an issue with expansion regions
 			return;
 		}
 		
 		// Mark activity node execution as under execution 
 		traceCurrentNodeExecution.setUnderExecution(true);
 		
 		// Set the chronological predecessor / successor relationship
 		setChronologicalRelationships(traceCurrentNodeExecution);
 		
 		// Set latest value snapshot for input values
 		if(traceCurrentNodeExecution instanceof ActionExecution) {
 			ActionExecution actionExecution = (ActionExecution)traceCurrentNodeExecution;
 			for(Input input : actionExecution.getInputs()) {
 				for(InputValue inputValue : input.getInputValues()) {
 					ObjectTokenInstance objectTokenInstance = inputValue.getInputObjectToken();
 					ValueInstance transportedValue = objectTokenInstance.getTransportedValue();
 					ValueSnapshot latestValueSnapshot = transportedValue.getLatestSnapshot();
 					inputValue.setInputValueSnapshot(latestValueSnapshot);
 				}
 			}
 		} else if(traceCurrentNodeExecution instanceof DecisionNodeExecution) {
 			ActivityExecutionStatus activityExecutionStatus = ExecutionContext.getInstance().executionStatus.getActivityExecutionStatus(executionID);	
 			ActivityExecution execution = activityExecutionStatus.getActivityExecution();
 			ActivityNodeActivation activation = execution.activationGroup.getNodeActivation(node);
 			DecisionNodeExecution decisionNodeExecution = (DecisionNodeExecution)traceCurrentNodeExecution;
 			DecisionNodeActivation decisionNodeActivation = (DecisionNodeActivation)activation;
 			
 			if(activation == null) { // TODO there is an issue with expansion regions
 				return;
 			}
 			
 			ActivityEdgeInstance decisionInputFlowInstance = decisionNodeActivation.getDecisionInputFlowInstance();				
 			
 			if(decisionInputFlowInstance != null) {
 				List<Token> decisionInputTokens = new ArrayList<Token>();
 				if(decisionInputFlowInstance.offers.size() > 0) {
 					decisionInputTokens.addAll(decisionInputFlowInstance.offers.get(0).offeredTokens);
 				}
 				if(decisionInputTokens.size() > 0) {
 					if(decisionInputTokens.get(0) instanceof ObjectToken) {
 						ObjectToken decisionInputToken = (ObjectToken)decisionInputTokens.get(0);
 						List<Token> decisionInputTokens_ = new ArrayList<Token>();
 						decisionInputTokens_.add(decisionInputToken);
 						TokenInstance decisionInputTokenInstance = getInputTokenInstances(decisionInputTokens_, node, activityExecutionStatus).get(0);
 						if(decisionInputTokenInstance instanceof ObjectTokenInstance) {
 							ObjectTokenInstance decisionInputObjectTokenInstance = (ObjectTokenInstance)decisionInputTokenInstance;
 							InputValue inputValue = TRACE_FACTORY.createInputValue();
 							inputValue.setInputObjectToken(decisionInputObjectTokenInstance);
 							decisionNodeExecution.setDecisionInputValue(inputValue);
 							ValueInstance transportedValue = decisionInputObjectTokenInstance.getTransportedValue();
 							ValueSnapshot latestValueSnapshot = transportedValue.getLatestSnapshot();
 							inputValue.setInputValueSnapshot(latestValueSnapshot);
 						}
 					}
 				}							
 			}			
 		}
 	}
 	
 	private void traceHandleActivityNodeExitEvent(ActivityNodeExitEvent event) {
 		int executionID = event.getActivityExecutionID();
 		ActivityNode node = event.getNode();
 		
 		Trace trace = getTrace(executionID);
 		org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution traceActivityExecution = trace.getActivityExecutionByID(executionID);
 		
 		ActivityNodeExecution traceCurrentNodeExecution = traceActivityExecution.getExecutionForEnabledNode(node);		
 				
 		ActivityExecutionStatus activityExecutionStatus = ExecutionContext.getInstance().executionStatus.getActivityExecutionStatus(executionID);	
 		ActivityExecution execution = activityExecutionStatus.getActivityExecution();
 		
 		
 		ActivityNodeActivation activation = execution.activationGroup.getNodeActivation(node);	
 				
 		// add output through output pins	
 		if(activation instanceof ActionActivation && traceCurrentNodeExecution instanceof ActionExecution) {
 			ActionExecution actionExecution = (ActionExecution)traceCurrentNodeExecution;
 			ActionActivation actionActivation = (ActionActivation)activation;
 			Action action = (Action)actionActivation.node;
 			for(OutputPin outputPin : action.output) {					
 				PinActivation outputPinActivation = actionActivation.getPinActivation(outputPin);
 				List<Token> sentTokens = activityExecutionStatus.removeTokenSending(outputPinActivation);
 				
 				if(sentTokens == null) { // happens if a pin has no outgoing edge
 					sentTokens = outputPinActivation.heldTokens;
 				}
 				
 				List<OutputValue> outputValues = new ArrayList<OutputValue>();
 				if(sentTokens != null) {
 					for(Token token : sentTokens) {		
 //						TokenInstance tokenInstance = executionStatus.getTokenInstance(token);
 //						if(tokenInstance == null) {	// token instance has not been added as output yet
 							OutputValue outputValue = createOutputValue(trace, token);
 							activityExecutionStatus.addTokenInstance(token, outputValue.getOutputObjectToken());
 							outputValues.add(outputValue);
 //						}
 					}
 					if(outputValues.size() > 0) {
 						Output output = TracemodelFactory.eINSTANCE.createOutput();
 						output.setOutputPin(outputPin);
 						output.getOutputValues().addAll(outputValues);
 						actionExecution.getOutputs().add(output);	
 					}
 				}
 			}
 
 
 		
 			// add output through edges
 			List<Token> sentTokens = activityExecutionStatus.removeTokenSending(activation);		
 	
 			if(sentTokens != null) {			
 				Set<Token> sentTokens_ = new HashSet<Token>(sentTokens);
 				for(Token token : sentTokens_) {
 					Token token_ = token;
 					ControlTokenInstance ctrlTokenInstance = null;
 					if(token instanceof ForkedToken) {
 						ForkedToken forkedToken = (ForkedToken)token;
 						if(sentTokens_.contains(forkedToken.baseToken)) {
 							continue;
 						}
 						token_ = forkedToken.baseToken;
 					}
 					
 					if(token_ instanceof ControlToken){					
 						ctrlTokenInstance = TracemodelFactory.eINSTANCE.createControlTokenInstance();
 						activityExecutionStatus.addTokenInstance(token, ctrlTokenInstance);
 						actionExecution.getOutgoingControl().add(ctrlTokenInstance);						
 					}/* else if (token_ instanceof ObjectToken){
 						tokenInstance = new ObjectTokenInstanceImpl();
 						ValueInstance valueInstance = new ValueInstanceImpl();
 						if(token_.getValue() instanceof Reference) {
 							valueInstance.setValue(((Reference)token_.getValue()).referent.copy());
 						} else {
 							valueInstance.setValue(token_.getValue().copy());
 						}
 						((ObjectTokenInstance)tokenInstance).setValue(valueInstance);
 					}*/
 					/*
 					if(ctrlTokenInstance != null) {																				
 						executionStatus.addTokenInstance(token, tokenInstance);
 						actionExecution.getOutgoingControl().add(tokenInstance);
 					}*/					
 				}
 /*				if(tokenInstances.size() > 0) {
 					nodeExecution.addActivityNodeOutput(null, tokenInstances);
 				}*/
 			}
 			/*
 			if(nodeExecution.getOutputs().size() == 0) {
 				nodeExecution.addActivityNodeOutput(null, null);
 			}*/
 		} else if(activation instanceof InitialNodeActivation) {
 			InitialNodeExecution initialNodeExecution = (InitialNodeExecution)traceCurrentNodeExecution;			
 			List<Token> sentTokens = activityExecutionStatus.removeTokenSending(activation);					
 			if(sentTokens != null && sentTokens.size() > 0) {			
 				Token token = sentTokens.get(0);
 				if(token instanceof ControlToken){					
 					ControlTokenInstance ctrlTokenInstance = TracemodelFactory.eINSTANCE.createControlTokenInstance();
 					activityExecutionStatus.addTokenInstance(token, ctrlTokenInstance);
 					initialNodeExecution.setOutgoingControl(ctrlTokenInstance);						
 				}	
 			}				
 		}
 		
 		// Mark node as executed
		traceCurrentNodeExecution.setUnderExecution(false);
		traceCurrentNodeExecution.setExecuted(true);
 		
 		return;
 	}
 	
 	private void traceHandleExtensionalValueEvent(ExtensionalValueEvent event) {
 		ExtensionalValue extensionalValue = event.getExtensionalValue();
 
 		if(extensionalValue instanceof Object_) {
 			Object_ object = (Object_)extensionalValue;
 			ExtensionalValueEventType eventType = event.getType();
 
 			Collection<Trace> allActiveTraces = this.activityExecutionTrace.values();
 			for(Trace trace : allActiveTraces) {
 				if(eventType == ExtensionalValueEventType.CREATION) {
 					ValueInstance valueInstance = createValueInstance(object);
 					trace.getValueInstances().add(valueInstance);
 
 				} else {
 					ValueInstance valueInstance = trace.getValueInstance(object);
 					if(valueInstance != null) {
 						if(eventType == ExtensionalValueEventType.DESTRUCTION) {							
 							valueInstance.setDestroyed(true);
 						} else {
 							ValueSnapshot valueSnapshot = createValueSnapshot(object);
 							valueInstance.getSnapshots().add(valueSnapshot);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void traceHandleSuspendEvent(SuspendEvent event) {
 		int executionID = event.getActivityExecutionID();
 		ActivityExecutionStatus activityExecutionStatus = ExecutionContext.getInstance().executionStatus.getActivityExecutionStatus(executionID);	
 		ActivityExecution execution = activityExecutionStatus.getActivityExecution();
 		
 		Trace trace = getTrace(executionID);
 		org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution activityExecution = trace.getActivityExecutionByID(executionID);
 
 		if(event.getLocation() instanceof Activity) { // add object tokens from activity input parameter node executions to trace			
 			List<Parameter> inputParametersWithoutParameterNode = new ArrayList<Parameter>();
 			inputParametersWithoutParameterNode.addAll(activityExecution.getInputParameters()); 
 						
 			List<ActivityParameterNode> inputActivityParameterNodes = activityExecution.getInputActivityParamenterNodes();
 			for(ActivityParameterNode inputActivityParameterNode : inputActivityParameterNodes) {
 				ActivityParameterNodeActivation inputActivityParameterNodeActivation = (ActivityParameterNodeActivation)execution.activationGroup.getNodeActivation(inputActivityParameterNode);
 				List<Token> sentTokens = activityExecutionStatus.removeTokenSending(inputActivityParameterNodeActivation);
 				if(sentTokens == null) { // happens if a parameter node has no outgoing edge
 					sentTokens = inputActivityParameterNodeActivation.heldTokens;
 				}
 				
 				if(sentTokens != null && sentTokens.size() > 0) {
 					inputParametersWithoutParameterNode.remove(inputActivityParameterNode.parameter);
 					InputParameterSetting inputParameterSetting = TRACE_FACTORY.createInputParameterSetting();
 					inputParameterSetting.setParameter(inputActivityParameterNode.parameter);
 					for(Token token : sentTokens) {
 						InputParameterValue inputParameterValue = createInputParameterValue(trace, token);
 						inputParameterSetting.getParameterValues().add(inputParameterValue);
 						
 						activityExecutionStatus.addTokenInstance(token, inputParameterValue.getParameterInputObjectToken());						
 					}
 					
 					activityExecution.getActivityInputs().add(inputParameterSetting);
 				}
 			}
 						
 			for(Parameter inputParameter : inputParametersWithoutParameterNode) {
 				ParameterValue parameterValue = execution.getParameterValue(inputParameter);
 				if(parameterValue != null) {
 					InputParameterSetting parameterSetting = createInputParameterSetting(activityExecution, inputParameter, parameterValue.values);
 					activityExecution.getActivityInputs().add(parameterSetting);
 				}
 			}			
 		}
 		
 		// add enabled nodes to trace
 		List<ActivityNode> enabledNodes = event.getNewEnabledNodes();
 		for(int i=0;i<enabledNodes.size();++i) {
 			ActivityNode node = enabledNodes.get(i);		
 
 			ActivityNodeExecution activityNodeExecution = createActivityNodeExecution(node);
 			activityExecution.getNodeExecutions().add(activityNodeExecution);			
 			
 			if(activityNodeExecution instanceof ActionExecution && !(activityNodeExecution instanceof StructuredActivityNodeExecution)) {
 				ActionExecution actionExecution = (ActionExecution)activityNodeExecution;
 				ActivityNodeActivation activation = execution.activationGroup.getNodeActivation(node);
 
 				if(activation == null) {
 					//there is an issue with expansion regions
 					return;
 				}
 				List<Token> tokens = getTokensForEnabledNode(activityExecutionStatus, activation, enabledNodes, i); 
 				
 				// add input through input pins
 				ActionActivation actionActivation = (ActionActivation)activation;
 				Action action = (Action)actionActivation.node;
 				for(InputPin inputPin : action.input) {
 					PinActivation inputPinActivation = actionActivation.getPinActivation(inputPin);
 					TokenList heldtokens = inputPinActivation.heldTokens;
 
 					List<InputValue> inputValues = new ArrayList<InputValue>();							
 					for(Token token : heldtokens) {
 						Token originalToken = activityExecutionStatus.getOriginalToken(token);								
 						if(tokens.contains(originalToken)) {	
 							TokenInstance tokenInstance = activityExecutionStatus.getTokenInstance(originalToken);
 							if(tokenInstance != null && tokenInstance instanceof ObjectTokenInstance) {
 								ObjectTokenInstance otokenInstance = (ObjectTokenInstance)tokenInstance;
 
 								List<ActivityEdge> traversedEdges = activityExecutionStatus.getTraversedActivityEdges(originalToken);
 								List<ActivityEdge> traversedEdgesForNode = getTraversedEdge(traversedEdges, inputPinActivation.node); 
 								otokenInstance.getTraversedEdges().addAll(traversedEdgesForNode);
 
 								InputValue inputValue = TRACE_FACTORY.createInputValue();
 								inputValue.setInputObjectToken(otokenInstance);
 								inputValues.add(inputValue);
 							}
 							tokens.remove(originalToken);
 						}
 					}							
 
 					if(inputValues.size() > 0) {
 						Input input = TRACE_FACTORY.createInput();
 						input.setInputPin(inputPin);
 						input.getInputValues().addAll(inputValues);
 						actionExecution.getInputs().add(input);
 					}
 				}
 
 				// add input through edges		
 				List<ControlTokenInstance> ctokenInstances = getInputControlTokenInstances(tokens, node, activityExecutionStatus); //control tokens remained in list TODO refactor
 				actionExecution.getIncomingControl().addAll(ctokenInstances);
 				
 			} else if(activityNodeExecution instanceof ControlNodeExecution) {
 				ControlNodeExecution controlNodeExecution = (ControlNodeExecution)activityNodeExecution;
 				ActivityNodeActivation activation = execution.activationGroup.getNodeActivation(node);
 				
 				if(activation != null) { // TODO there is an issue with expansion regions
 					List<Token> tokens = getTokensForEnabledNode(activityExecutionStatus, activation, enabledNodes, i); 
 					
 					// add input through edges		
 					List<TokenInstance> tokenInstances = getInputTokenInstances(tokens, node, activityExecutionStatus); 
 					controlNodeExecution.getRoutedTokens().addAll(tokenInstances);
 				}
 			} 			
 		}			
 	}
 	
 	private InputParameterSetting createInputParameterSetting(org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution activityExecution, Parameter parameter, ValueList values) {
 		InputParameterSetting parameterSetting = TRACE_FACTORY.createInputParameterSetting();
 		parameterSetting.setParameter(parameter);		
 		for(Value value : values) {						
 			ValueInstance valueInstance = getOrCreateValueInstance(activityExecution.getTrace(), value);						
 			InputParameterValue parameterValue = TRACE_FACTORY.createInputParameterValue();
 			parameterValue.setValueInstance(valueInstance);
 			parameterValue.setValueSnapshot(valueInstance.getLatestSnapshot());
 			parameterSetting.getParameterValues().add(parameterValue);
 		}			
 		return parameterSetting;		
 	}
 	
 	private void initializeTraceWithObjectsAtLocus(Trace trace) {
 		for(ExtensionalValue extensionalValue : ExecutionContext.getInstance().getLocus().extensionalValues) {
 			if(extensionalValue.getClass().equals(Object_.class)) {
 				ValueInstance valueInstance = createValueInstance(extensionalValue);
 				trace.getValueInstances().add(valueInstance);
 				trace.getInitialLocusValueInstances().add(valueInstance);
 			}
 		}		
 	}
 
 	private ValueInstance createValueInstance(Value value) {
 		Value value_ = value;
 		if(value instanceof Reference) {
 			value_ = ((Reference)value).referent;
 		}
 		ValueInstance valueInstance = TracemodelFactory.eINSTANCE.createValueInstance();
 		valueInstance.setRuntimeValue(value_);
 		ValueSnapshot valueSnapshot = createValueSnapshot(value_);
 		valueInstance.getSnapshots().add(valueSnapshot);
 		valueInstance.setOriginal(valueSnapshot);
 		return valueInstance;
 	}
 	
 	private ValueSnapshot createValueSnapshot(Value value) {
 		ValueSnapshot valueSnapshot = TracemodelFactory.eINSTANCE.createValueSnapshot();
 		valueSnapshot.setValue(value.copy());
 		return valueSnapshot;
 	}
 	
 	private ValueInstance getOrCreateValueInstance(Trace trace, Value value) {
 		ValueInstance valueInstance;
 		
 		Value value_ = value;
 		if(value instanceof Reference) {
 			value_ = ((Reference)value).referent;
 		}
 	
 		ValueInstance existingValueInstance = trace.getValueInstance(value_);
 		if(existingValueInstance != null) {
 			valueInstance = existingValueInstance;
 		} else {
 			valueInstance = createValueInstance(value_);
 			trace.getValueInstances().add(valueInstance);
 		}
 		
 		return valueInstance;
 	}
 	
 	private List<Token> getTokensForEnabledNode(ActivityExecutionStatus executionStatus, ActivityNodeActivation activation,
 			List<ActivityNode> enabledNodes, int i) {
 		List<TokenList> tokensets = executionStatus.getEnabledActivationTokens(activation);
 		// in one step one particular node can only be enabled once, 
 		// i.e, the tokens sent to this node in the last step (enabling the node) are added at last
 		TokenList lastTokenList = tokensets.get(tokensets.size()-1);
 		return new ArrayList<Token>(lastTokenList);
 	}
 	
 	
 	private InputParameterValue createInputParameterValue(Trace trace, Token token) {
 		ObjectTokenInstance otokenInstance = TRACE_FACTORY.createObjectTokenInstance();
 		InputParameterValue inputParameterValue = TRACE_FACTORY.createInputParameterValue();
 		inputParameterValue.setParameterInputObjectToken(otokenInstance);
 
 		if(token.getValue() != null) {
 			ValueInstance valueInstance = getOrCreateValueInstance(trace, token.getValue());
 			otokenInstance.setTransportedValue(valueInstance);									
 			inputParameterValue.setValueSnapshot(valueInstance.getLatestSnapshot());
 			inputParameterValue.setValueInstance(valueInstance);
 		}
 		
 		return inputParameterValue;
 	}
 	
 	private OutputParameterValue createOutputParameterValue(Trace trace, ObjectTokenInstance otokenInstance) {		
 		OutputParameterValue outputParameterValue = TRACE_FACTORY.createOutputParameterValue();
 		outputParameterValue.setParameterOutputObjectToken(otokenInstance);
 
 		outputParameterValue.setValueSnapshot(otokenInstance.getTransportedValue().getLatestSnapshot());
 		outputParameterValue.setValueInstance(otokenInstance.getTransportedValue());
 		
 		return outputParameterValue;				
 	}
 	
 	private OutputValue createOutputValue(Trace trace, Token token) {
 		ObjectTokenInstance otokenInstance = TRACE_FACTORY.createObjectTokenInstance();
 		OutputValue outputValue = TRACE_FACTORY.createOutputValue();
 		outputValue.setOutputObjectToken(otokenInstance);
 		
 		if(token.getValue() != null) {
 			ValueInstance valueInstance = getOrCreateValueInstance(trace, token.getValue());
 			otokenInstance.setTransportedValue(valueInstance);									
 			outputValue.setOutputValueSnapshot(valueInstance.getLatestSnapshot());
 		}
 		
 		return outputValue;
 	}
 	
 	private List<ControlTokenInstance> getInputControlTokenInstances(List<Token> tokens, ActivityNode node, ActivityExecutionStatus executionStatus) {
 		//TODO move this into ExecutionStatus?
 		List<ControlTokenInstance> ctokenInstances = new ArrayList<ControlTokenInstance>();
 		
 		List<TokenInstance> tokenInstances = getInputTokenInstances(tokens, node, executionStatus);
 		for(TokenInstance tokenInstance : tokenInstances) {
 			if(tokenInstance instanceof ControlTokenInstance) {
 				ctokenInstances.add((ControlTokenInstance)tokenInstance);
 			}
 		}
 		return ctokenInstances;
 	}
 	
 	private List<TokenInstance> getInputTokenInstances(List<Token> tokens, ActivityNode node, ActivityExecutionStatus executionStatus) {
 		//TODO move this into ExecutionStatus?
 		List<TokenInstance> tokenInstances = new ArrayList<TokenInstance>();
 		for(Token token : tokens) {
 			TokenInstance tokenInstance = executionStatus.getTokenInstance(token);
 			if(tokenInstance != null) {
 				List<ActivityEdge> traversedEdges = executionStatus.getTraversedActivityEdges(token);
 				List<ActivityEdge> traversedEdgesForNode = getTraversedEdge(traversedEdges, node);
 				
 				for(ActivityEdge e : traversedEdgesForNode) {
 					if(!tokenInstance.getTraversedEdges().contains(e)) {
 						tokenInstance.getTraversedEdges().add(e);
 					}
 				}
 				tokenInstances.add(tokenInstance);
 			}
 		}	
 		return tokenInstances;
 	}
 	
 	private ActivityNodeExecution createActivityNodeExecution(ActivityNode activityNode) {
 		ActivityNodeExecution activityNodeExecution;
 		if(activityNode instanceof DecisionNode) {
 			activityNodeExecution = TRACE_FACTORY.createDecisionNodeExecution();
 		} else if(activityNode instanceof CallAction) {
 			activityNodeExecution = TRACE_FACTORY.createCallActionExecution();
 		} else if(activityNode instanceof StructuredActivityNode) {
 			activityNodeExecution = TRACE_FACTORY.createStructuredActivityNodeExecution();
 		} else if(activityNode instanceof Action) {
 			activityNodeExecution = TRACE_FACTORY.createActionExecution();
 		} else if(activityNode instanceof InitialNode) {
 			activityNodeExecution = TRACE_FACTORY.createInitialNodeExecution();
 		} else if(activityNode instanceof ControlNode) {
 			activityNodeExecution = TRACE_FACTORY.createControlNodeExecution();
 		} else {
 			return null;
 		}
 		activityNodeExecution.setNode(activityNode);	
 		return activityNodeExecution;
 	}
 
 	private void setChronologicalRelationships(ActivityNodeExecution activityNodeExecution) {
 		Trace trace = activityNodeExecution.getActivityExecution().getTrace();
 		ActivityNodeExecution traceLastNodeExecution = trace.getLastActivityNodeExecution();
 		if(traceLastNodeExecution != null && !traceLastNodeExecution.equals(activityNodeExecution)) {
 			traceLastNodeExecution.setChronologicalSuccessor(activityNodeExecution);
 			activityNodeExecution.setChronologicalPredecessor(traceLastNodeExecution);
 		}
 	}
 	
 	private List<ActivityEdge> getTraversedEdge(List<ActivityEdge> edges, ActivityNode targetNode) {
 		List<ActivityEdge> traversedEdges = new ArrayList<ActivityEdge>();
 		if(edges != null) {
 			for(ActivityEdge edge : edges) {
 				if(edge.target.equals(targetNode)) {
 					traversedEdges.add(edge);
 				}
 			}
 		}
 		return traversedEdges;
 	}
 
 }
