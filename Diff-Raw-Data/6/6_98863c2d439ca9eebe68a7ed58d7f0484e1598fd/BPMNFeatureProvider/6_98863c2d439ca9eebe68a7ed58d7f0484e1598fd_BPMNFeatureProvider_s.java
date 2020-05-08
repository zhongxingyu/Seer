 /******************************************************************************* 
  * Copyright (c) 2011 Red Hat, Inc. 
  *  All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *
  * @author Innar Made
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.ui.diagram;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.eclipse.bpmn2.AdHocSubProcess;
 import org.eclipse.bpmn2.Association;
 import org.eclipse.bpmn2.BoundaryEvent;
 import org.eclipse.bpmn2.BusinessRuleTask;
 import org.eclipse.bpmn2.CallActivity;
 import org.eclipse.bpmn2.CallChoreography;
 import org.eclipse.bpmn2.CancelEventDefinition;
 import org.eclipse.bpmn2.ChoreographyTask;
 import org.eclipse.bpmn2.CompensateEventDefinition;
 import org.eclipse.bpmn2.ComplexGateway;
 import org.eclipse.bpmn2.ConditionalEventDefinition;
 import org.eclipse.bpmn2.Conversation;
 import org.eclipse.bpmn2.ConversationLink;
 import org.eclipse.bpmn2.DataInput;
 import org.eclipse.bpmn2.DataInputAssociation;
 import org.eclipse.bpmn2.DataObject;
 import org.eclipse.bpmn2.DataObjectReference;
 import org.eclipse.bpmn2.DataOutput;
 import org.eclipse.bpmn2.DataOutputAssociation;
 import org.eclipse.bpmn2.DataStoreReference;
 import org.eclipse.bpmn2.EndEvent;
 import org.eclipse.bpmn2.ErrorEventDefinition;
 import org.eclipse.bpmn2.EscalationEventDefinition;
 import org.eclipse.bpmn2.EventBasedGateway;
 import org.eclipse.bpmn2.ExclusiveGateway;
 import org.eclipse.bpmn2.Group;
 import org.eclipse.bpmn2.InclusiveGateway;
 import org.eclipse.bpmn2.IntermediateCatchEvent;
 import org.eclipse.bpmn2.IntermediateThrowEvent;
 import org.eclipse.bpmn2.Lane;
 import org.eclipse.bpmn2.LinkEventDefinition;
 import org.eclipse.bpmn2.ManualTask;
 import org.eclipse.bpmn2.Message;
 import org.eclipse.bpmn2.MessageEventDefinition;
 import org.eclipse.bpmn2.MessageFlow;
 import org.eclipse.bpmn2.ParallelGateway;
 import org.eclipse.bpmn2.Participant;
 import org.eclipse.bpmn2.ReceiveTask;
 import org.eclipse.bpmn2.ScriptTask;
 import org.eclipse.bpmn2.SendTask;
 import org.eclipse.bpmn2.SequenceFlow;
 import org.eclipse.bpmn2.ServiceTask;
 import org.eclipse.bpmn2.SignalEventDefinition;
 import org.eclipse.bpmn2.StartEvent;
 import org.eclipse.bpmn2.SubChoreography;
 import org.eclipse.bpmn2.SubProcess;
 import org.eclipse.bpmn2.Task;
 import org.eclipse.bpmn2.TerminateEventDefinition;
 import org.eclipse.bpmn2.TextAnnotation;
 import org.eclipse.bpmn2.TimerEventDefinition;
 import org.eclipse.bpmn2.Transaction;
 import org.eclipse.bpmn2.UserTask;
 import org.eclipse.bpmn2.modeler.core.features.AbstractBpmn2CreateFeature;
 import org.eclipse.bpmn2.modeler.core.features.ConnectionFeatureContainer;
 import org.eclipse.bpmn2.modeler.core.features.ContextConstants;
 import org.eclipse.bpmn2.modeler.core.features.DefaultDeleteBPMNShapeFeature;
 import org.eclipse.bpmn2.modeler.core.features.DefaultRemoveBPMNShapeFeature;
 import org.eclipse.bpmn2.modeler.core.features.FeatureContainer;
 import org.eclipse.bpmn2.modeler.core.features.activity.task.ICustomTaskFeature;
 import org.eclipse.bpmn2.modeler.core.features.bendpoint.AddBendpointFeature;
 import org.eclipse.bpmn2.modeler.core.features.bendpoint.MoveBendpointFeature;
 import org.eclipse.bpmn2.modeler.core.features.bendpoint.RemoveBendpointFeature;
 import org.eclipse.bpmn2.modeler.core.features.flow.AbstractCreateFlowFeature;
 import org.eclipse.bpmn2.modeler.core.runtime.CustomTaskDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.FeatureContainerDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
 import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
 import org.eclipse.bpmn2.modeler.ui.editor.BPMN2Editor;
 import org.eclipse.bpmn2.modeler.ui.features.activity.subprocess.AdHocSubProcessFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.subprocess.CallActivityFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.subprocess.SubProcessFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.subprocess.TransactionFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.BusinessRuleTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.CustomTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.CustomTaskFeatureContainer.CreateCustomTaskFeature;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.ManualTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.ReceiveTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.ScriptTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.SendTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.ServiceTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.TaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.UserTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.artifact.GroupFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.artifact.TextAnnotationFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.choreography.CallChoreographyFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.choreography.ChoreographyMessageLinkFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.choreography.ChoreographyTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.choreography.SubChoreographyFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.conversation.ConversationFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.conversation.ConversationLinkFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.data.DataInputFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.data.DataObjectFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.data.DataObjectReferenceFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.data.DataOutputFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.data.DataStoreReferenceFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.data.MessageFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.BoundaryEventFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.EndEventFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.IntermediateCatchEventFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.IntermediateThrowEventFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.StartEventFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.CancelEventDefinitionContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.CompensateEventDefinitionContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.ConditionalEventDefinitionContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.ErrorEventDefinitionContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.EscalationEventDefinitionContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.LinkEventDefinitionContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.MessageEventDefinitionContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.SignalEventDefinitionContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.TerminateEventDefinitionFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.event.definitions.TimerEventDefinitionContainer;
 import org.eclipse.bpmn2.modeler.ui.features.flow.AssociationFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.flow.DataInputAssociationFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.flow.DataOutputAssociationFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.flow.MessageFlowFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.flow.SequenceFlowFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.gateway.ComplexGatewayFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.gateway.EventBasedGatewayFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.gateway.ExclusiveGatewayFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.gateway.InclusiveGatewayFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.gateway.ParallelGatewayFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.label.LabelFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.lane.LaneFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.participant.ParticipantFeatureContainer;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddBendpointFeature;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IMoveBendpointFeature;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IReconnectionFeature;
 import org.eclipse.graphiti.features.IRemoveBendpointFeature;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddBendpointContext;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IMoveBendpointContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.context.IReconnectionContext;
 import org.eclipse.graphiti.features.context.IRemoveBendpointContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.ILinkService;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 
 /**
  * Determines what kinds of business objects can be added to a diagram.
  * 
  * @author imeikas
  * 
  */
 public class BPMNFeatureProvider extends DefaultFeatureProvider {
 
 	private Hashtable<Class,FeatureContainer> containers;
 	private Hashtable<String,CustomTaskFeatureContainer> customTaskContainers;
 	private ICreateFeature[] createFeatures;
 	private ICreateConnectionFeature[] createConnectionFeatures;
 	private HashMap<Class,IFeature> mapBusinessObjectClassToCreateFeature = new HashMap<Class,IFeature>();
 
 	public BPMNFeatureProvider(IDiagramTypeProvider dtp) {
 		super(dtp);
 		updateFeatureLists();
 	}
 	
 	private void initializeFeatureContainers() {
 		containers = new Hashtable<Class,FeatureContainer>();
 		containers.put(LabelFeatureContainer.class,new LabelFeatureContainer());
 		containers.put(Group.class,new GroupFeatureContainer());
 		containers.put(DataObject.class,new DataObjectFeatureContainer());
 		containers.put(DataObjectReference.class,new DataObjectReferenceFeatureContainer());
 		containers.put(DataStoreReference.class,new DataStoreReferenceFeatureContainer());
 		containers.put(DataInput.class,new DataInputFeatureContainer());
 		containers.put(DataOutput.class,new DataOutputFeatureContainer());
 		containers.put(Message.class,new MessageFeatureContainer());
 		containers.put(StartEvent.class,new StartEventFeatureContainer());
 		containers.put(EndEvent.class,new EndEventFeatureContainer());
 		containers.put(IntermediateCatchEvent.class,new IntermediateCatchEventFeatureContainer());
 		containers.put(IntermediateThrowEvent.class,new IntermediateThrowEventFeatureContainer());
 		containers.put(BoundaryEvent.class,new BoundaryEventFeatureContainer());
 		containers.put(Task.class,new TaskFeatureContainer());
 		containers.put(ScriptTask.class,new ScriptTaskFeatureContainer());
 		containers.put(ServiceTask.class,new ServiceTaskFeatureContainer());
 		containers.put(UserTask.class,new UserTaskFeatureContainer());
 		containers.put(ManualTask.class,new ManualTaskFeatureContainer());
 		containers.put(BusinessRuleTask.class,new BusinessRuleTaskFeatureContainer());
 		containers.put(SendTask.class,new SendTaskFeatureContainer());
 		containers.put(ReceiveTask.class,new ReceiveTaskFeatureContainer());
 		containers.put(ChoreographyTask.class,new ChoreographyTaskFeatureContainer());
 		containers.put(ExclusiveGateway.class,new ExclusiveGatewayFeatureContainer());
 		containers.put(InclusiveGateway.class,new InclusiveGatewayFeatureContainer());
 		containers.put(ParallelGateway.class,new ParallelGatewayFeatureContainer());
 		containers.put(EventBasedGateway.class,new EventBasedGatewayFeatureContainer());
 		containers.put(ComplexGateway.class,new ComplexGatewayFeatureContainer());
 		containers.put(AdHocSubProcess.class,new AdHocSubProcessFeatureContainer());
 		containers.put(CallActivity.class,new CallActivityFeatureContainer());
 		containers.put(Transaction.class,new TransactionFeatureContainer());
 		containers.put(SubProcess.class,new SubProcessFeatureContainer());
 		containers.put(ConditionalEventDefinition.class,new ConditionalEventDefinitionContainer());
 		containers.put(MessageEventDefinition.class,new MessageEventDefinitionContainer());
 		containers.put(TimerEventDefinition.class,new TimerEventDefinitionContainer());
 		containers.put(SignalEventDefinition.class,new SignalEventDefinitionContainer());
 		containers.put(EscalationEventDefinition.class,new EscalationEventDefinitionContainer());
 		containers.put(CompensateEventDefinition.class,new CompensateEventDefinitionContainer());
 		containers.put(LinkEventDefinition.class,new LinkEventDefinitionContainer());
 		containers.put(ErrorEventDefinition.class,new ErrorEventDefinitionContainer());
 		containers.put(CancelEventDefinition.class,new CancelEventDefinitionContainer());
 		containers.put(TerminateEventDefinition.class,new TerminateEventDefinitionFeatureContainer());
 		containers.put(SequenceFlow.class,new SequenceFlowFeatureContainer());
 		containers.put(MessageFlow.class,new MessageFlowFeatureContainer());
 		containers.put(Association.class,new AssociationFeatureContainer());
 		containers.put(Conversation.class,new ConversationFeatureContainer());
 		containers.put(ConversationLink.class,new ConversationLinkFeatureContainer());
 		containers.put(DataInputAssociation.class,new DataInputAssociationFeatureContainer());
 		containers.put(DataOutputAssociation.class,new DataOutputAssociationFeatureContainer());
 		containers.put(SubChoreography.class,new SubChoreographyFeatureContainer());
 		containers.put(CallChoreography.class,new CallChoreographyFeatureContainer());
 		containers.put(Participant.class,new ParticipantFeatureContainer());
 		containers.put(Lane.class,new LaneFeatureContainer());
 		containers.put(TextAnnotation.class,new TextAnnotationFeatureContainer());
 		// these have no BPMN2 element equivalents
 		containers.put(ChoreographyMessageLinkFeatureContainer.class,new ChoreographyMessageLinkFeatureContainer());
 		containers.put(LabelFeatureContainer.class,new LabelFeatureContainer());
 	}
 
 	public void addFeatureContainer(String id, CustomTaskFeatureContainer fc) throws Exception {
 		
 		if (customTaskContainers==null) {
 			customTaskContainers = new Hashtable<String,CustomTaskFeatureContainer>();
 		}
 		customTaskContainers.put(id,fc);
 		updateFeatureLists();
 	}
 	
 	private void updateFeatureLists() {
 
 		initializeFeatureContainers();
 		
 		// Collect all of the <featureContainer> extensions defined by the current TargetRuntime
 		// and replace the ones in our list of FeatureContainers
 		BPMN2Editor editor = BPMN2Editor.getActiveEditor(); //(BPMN2Editor)getDiagramTypeProvider().getDiagramEditor();;
 		TargetRuntime rt = editor.getTargetRuntime();
 		for (FeatureContainerDescriptor fcd : rt.getFeatureContainers()) {
 			FeatureContainer container = fcd.getFeatureContainer();
 			if (container instanceof ConnectionFeatureContainer) {
 				ICreateConnectionFeature createConnectionFeature = ((ConnectionFeatureContainer)container)
 						.getCreateConnectionFeature(this);
 				if (createConnectionFeature!=null) {
 					containers.put(fcd.getType(), container);
 				}
 			}
 			else {
 				ICreateFeature createFeature = container.getCreateFeature(this);
 				if (createFeature != null) {
 					containers.put(fcd.getType(), container);
 				}
 			}
 		}
 
 		// build the list of CreateFeatures from our new list of all FeatureContainers
 		List<ICreateFeature> createFeaturesList = new ArrayList<ICreateFeature>();
 		List<ICreateConnectionFeature> createConnectionFeatureList = new ArrayList<ICreateConnectionFeature>();
 
 		for (FeatureContainer fc : containers.values()) {
 			if (fc instanceof ConnectionFeatureContainer) {
 				ConnectionFeatureContainer connectionFeatureContainer = (ConnectionFeatureContainer) fc;
 				ICreateConnectionFeature createConnectionFeature = connectionFeatureContainer
 						.getCreateConnectionFeature(this);
 				if (createConnectionFeature != null) {
 					createConnectionFeatureList.add(createConnectionFeature);
 				}
 			}
 			else {
 				ICreateFeature createFeature = fc.getCreateFeature(this);
 				if (createFeature != null) {
 					createFeaturesList.add(createFeature);
 				}
 			}
 		}
 		if (customTaskContainers!=null) {
 			for (FeatureContainer fc : customTaskContainers.values()) {
 				ICreateFeature createFeature = fc.getCreateFeature(this);
 				if (createFeature != null) {
 					createFeaturesList.add(createFeature);
 				}
 			}
 		}
 		
 		createFeatures = createFeaturesList.toArray(new ICreateFeature[createFeaturesList.size()]);
 		createConnectionFeatures = createConnectionFeatureList
 				.toArray(new ICreateConnectionFeature[createConnectionFeatureList.size()]);
 		
 		mapBusinessObjectClassToCreateFeature.clear();
 		for (IFeature cf : createFeatures) {
 			if (cf instanceof AbstractBpmn2CreateFeature) {
 				if (cf instanceof CreateCustomTaskFeature) {
 					continue;
 				}
 				AbstractBpmn2CreateFeature acf = (AbstractBpmn2CreateFeature)cf;
 				mapBusinessObjectClassToCreateFeature.put(acf.getBusinessObjectClass().getInstanceClass(), cf);
 			}
 		}
 		for (IFeature cf : createConnectionFeatures) {
 			if (cf instanceof AbstractCreateFlowFeature) {
 				AbstractCreateFlowFeature acf = (AbstractCreateFlowFeature)cf;
 				mapBusinessObjectClassToCreateFeature.put(acf.getBusinessObjectClass().getInstanceClass(), cf);
 			}
 		}
 	}
 	
 	private EObject getApplyObject(IContext context) {
 		if (context instanceof IAddContext) {
 			Object object = ((IAddContext) context).getNewObject();
 			if (object instanceof EObject)
 				return (EObject)object;
 		} else if (context instanceof IPictogramElementContext) {
 			return BusinessObjectUtil.getFirstElementOfType(
 					(((IPictogramElementContext) context).getPictogramElement()), EObject.class);
 		}
 		return null;
 	}
 	
 	public FeatureContainer getFeatureContainer(IContext context) {
 		
 		// The special LabelFeatureContainer is used to add labels to figures that were
 		// added within the given IContext
 		Object property = context.getProperty(ContextConstants.LABEL_CONTEXT);
 		if ( property!=null && (Boolean)property )
 			return containers.get(LabelFeatureContainer.class);
 		
 		EObject object = getApplyObject(context);
 		if (object!=null) {
 			BPMN2Editor editor = (BPMN2Editor)getDiagramTypeProvider().getDiagramEditor();;
 			TargetRuntime rt = editor.getTargetRuntime();
 			FeatureContainerDescriptor fcd = rt.getFeatureContainer(object.eClass());
 			if (fcd!=null)
 				return fcd.getFeatureContainer();
 		}
 		
 		Object id = CustomTaskFeatureContainer.getId(context); 
 		for (FeatureContainer container : containers.values()) {
 			if (id!=null && !(container instanceof CustomTaskFeatureContainer))
 				continue;
 			Object o = container.getApplyObject(context);
 			if (o != null && container.canApplyTo(o)) {
 				return container;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public IAddFeature getAddFeature(IAddContext context) {
 		// only here do we need to search all of the Custom Task extensions to check if
 		// the newObject (in AddContext) is a Custom Task. This is because of a chicken/egg
 		// problem during DIImport: the Custom Task feature containers are not added to
 		// the toolpalette until AFTER the file is loaded (in DIImport) and getAddFeature()
 		// is called during file loading.
 		Object id = CustomTaskFeatureContainer.getId(context); 
 		if (id!=null) {
 			BPMN2Editor editor = (BPMN2Editor)getDiagramTypeProvider().getDiagramEditor();;
 			TargetRuntime rt = editor.getTargetRuntime();
 			for (CustomTaskDescriptor ct : rt.getCustomTasks()) {
 				if (id.equals(ct.getId())) {
 					CustomTaskFeatureContainer container = (CustomTaskFeatureContainer)ct.getFeatureContainer();
 					return container.getAddFeature(this);
 				}
 			}
 		}
 		
 		FeatureContainer container = getFeatureContainer(context);
 		if (container!=null) {
 			IAddFeature feature = container.getAddFeature(this);
 			if (feature != null)
 				return feature;
 		}
 		return super.getAddFeature(context);
 	}
 
 	@Override
 	public ICreateFeature[] getCreateFeatures() {
 		return createFeatures;
 	}
 
 	@Override
 	public IUpdateFeature getUpdateFeature(IUpdateContext context) {
 		FeatureContainer container = getFeatureContainer(context);
 		if (container!=null) {
 			IUpdateFeature feature = container.getUpdateFeature(this);
 			if (feature != null)
 				return feature;
 		}
 		return super.getUpdateFeature(context);
 	}
 
 	@Override
 	public ICreateConnectionFeature[] getCreateConnectionFeatures() {
 		return createConnectionFeatures;
 	}
 
 	@Override
 	public IDirectEditingFeature getDirectEditingFeature(IDirectEditingContext context) {
 		FeatureContainer container = getFeatureContainer(context);
 		if (container!=null) {
 			IDirectEditingFeature feature = container.getDirectEditingFeature(this);
 			if (feature != null)
 				return feature;
 		}
 		return super.getDirectEditingFeature(context);
 	}
 
 	@Override
 	public ILayoutFeature getLayoutFeature(ILayoutContext context) {
 		FeatureContainer container = getFeatureContainer(context);
 		if (container!=null) {
 			ILayoutFeature feature = container.getLayoutFeature(this);
 			if (feature != null)
 				return feature;
 		}
 		return super.getLayoutFeature(context);
 	}
 
 	@Override
 	public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
 		FeatureContainer container = getFeatureContainer(context);
 		if (container!=null) {
 			IMoveShapeFeature feature = container.getMoveFeature(this);
 			if (feature != null)
 				return feature;
 		}
 		return super.getMoveShapeFeature(context);
 	}
 
 	@Override
 	public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
 		FeatureContainer container = getFeatureContainer(context);
 		if (container!=null) {
 			IResizeShapeFeature feature = container.getResizeFeature(this);
 			if (feature != null)
 				return feature;
 		}
 		return super.getResizeShapeFeature(context);
 	}
 
 	@Override
 	public IAddBendpointFeature getAddBendpointFeature(IAddBendpointContext context) {
 		return new AddBendpointFeature(this);
 	}
 
 	@Override
 	public IMoveBendpointFeature getMoveBendpointFeature(IMoveBendpointContext context) {
 		return new MoveBendpointFeature(this);
 	}
 
 	@Override
 	public IRemoveBendpointFeature getRemoveBendpointFeature(IRemoveBendpointContext context) {
 		return new RemoveBendpointFeature(this);
 	}
 
 	@Override
 	public IReconnectionFeature getReconnectionFeature(IReconnectionContext context) {
 		for (FeatureContainer container : containers.values()) {
 			Object o = container.getApplyObject(context);
 			if (o != null && container.canApplyTo(o) && container instanceof ConnectionFeatureContainer) {
 				IReconnectionFeature feature = ((ConnectionFeatureContainer)container).getReconnectionFeature(this);
 				if (feature == null) {
 					break;
 				}
 				return feature;
 			}
 		}
 		return super.getReconnectionFeature(context);
 	}
 
 	@Override
 	public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 		FeatureContainer container = getFeatureContainer(context);
 		if (container!=null) {
 			IDeleteFeature feature = container.getDeleteFeature(this);
 			if (feature != null)
 				return feature;
 		}
 		return new DefaultDeleteBPMNShapeFeature(this);
 	}
 
 	@Override
 	public IRemoveFeature getRemoveFeature(IRemoveContext context) {
 		FeatureContainer container = getFeatureContainer(context);
 		if (container!=null) {
 			IRemoveFeature feature = container.getRemoveFeature(this);
 			if (feature != null)
 				return feature;
 		}
 		return new DefaultRemoveBPMNShapeFeature(this);
 	}
 
 	@Override
 	public ICustomFeature[] getCustomFeatures(ICustomContext context) {
 		ILinkService ls = Graphiti.getLinkService();
 		List<ICustomFeature> list = new ArrayList<ICustomFeature>();
 
 		BPMN2Editor editor = (BPMN2Editor)getDiagramTypeProvider().getDiagramEditor();;
 		TargetRuntime rt = editor.getTargetRuntime();
 		for (CustomTaskDescriptor ct : rt.getCustomTasks()) {
 			ICustomTaskFeature ctf = ct.getFeatureContainer();
 			if (ctf!=null) {
 				ICustomFeature[] cfa = ctf.getCustomFeatures(this);
 				if (cfa!=null) {
 					for (ICustomFeature cf : cfa) {
 						if (cf.isAvailable(context)) {
 							boolean found = false;
 							for (ICustomFeature cfl : list) {
 								if (cfl.getClass() == cf.getClass()) {
 									found = true;
 									break;
 								}
 							}
 							if (!found)
 								list.add(cf);
 						}
 					}
 				}
 			}
 		}
 		for (FeatureContainer fc : containers.values()) {
 			Object o = fc.getApplyObject(context);
 			if (o!=null && fc.canApplyTo(o)) {
 				ICustomFeature[] cfa = fc.getCustomFeatures(this);
 				if (cfa!=null) {
 					for (ICustomFeature cf : cfa) {
 						boolean found = false;
 						for (ICustomFeature cfl : list) {
 							if (cfl.getClass() == cf.getClass()) {
 								found = true;
 								break;
 							}
 						}
 						if (!found)
 							list.add(cf);
 					}
 				}
 			}
 		}
 		
 //		list.add(new ValidateModelFeature(this));
 		return list.toArray(new ICustomFeature[list.size()]);
 	}
 
 	// TODO: move this to the adapter registry
 	public IFeature getCreateFeatureForPictogramElement(PictogramElement pe) {
 		if (pe!=null) {
 			String id = Graphiti.getPeService().getPropertyValue(pe,ICustomTaskFeature.CUSTOM_TASK_ID);
 			if (id!=null) {
 				for (FeatureContainer container : containers.values()) {
 					if (container instanceof CustomTaskFeatureContainer) {
 						CustomTaskFeatureContainer ctf = (CustomTaskFeatureContainer)container;
 						if (id.equals(ctf.getId())) {
 							return ctf.getCreateFeature(this);
 						}
 					}
 				}
 			}
 
 			EObject be = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
 			return getCreateFeatureForBusinessObject(be);
 		}
 		return null;
 	}
 	
 	public IFeature getCreateFeatureForBusinessObject(Object be) {
 		IFeature feature = null;
 		if (be!=null) {
 			Class[] ifs = be.getClass().getInterfaces();
 			for (int i=0; i<ifs.length && feature==null; ++i) {
 				feature = mapBusinessObjectClassToCreateFeature.get(ifs[i]);
 			}
 		}
 		return feature;
 	}
 	
 	public IFeature getCreateFeatureForBusinessObject(Class clazz) {
 		return mapBusinessObjectClassToCreateFeature.get(clazz);
 	}
 }
