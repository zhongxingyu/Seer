 package org.eclipse.uml2.diagram.activity.part;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.activity.edit.parts.*;
 import org.eclipse.uml2.diagram.activity.providers.UMLElementTypes;
 import org.eclipse.uml2.diagram.common.genapi.IDiagramUpdater;
 import org.eclipse.uml2.uml.AcceptEventAction;
 import org.eclipse.uml2.uml.Action;
 import org.eclipse.uml2.uml.Activity;
 import org.eclipse.uml2.uml.ActivityEdge;
 import org.eclipse.uml2.uml.ActivityFinalNode;
 import org.eclipse.uml2.uml.ActivityGroup;
 import org.eclipse.uml2.uml.ActivityNode;
 import org.eclipse.uml2.uml.ActivityParameterNode;
 import org.eclipse.uml2.uml.ActivityPartition;
 import org.eclipse.uml2.uml.AddStructuralFeatureValueAction;
 import org.eclipse.uml2.uml.Behavior;
 import org.eclipse.uml2.uml.CallBehaviorAction;
 import org.eclipse.uml2.uml.CallOperationAction;
 import org.eclipse.uml2.uml.CentralBufferNode;
 import org.eclipse.uml2.uml.ConditionalNode;
 import org.eclipse.uml2.uml.Constraint;
 import org.eclipse.uml2.uml.ControlFlow;
 import org.eclipse.uml2.uml.CreateObjectAction;
 import org.eclipse.uml2.uml.DataStoreNode;
 import org.eclipse.uml2.uml.DecisionNode;
 import org.eclipse.uml2.uml.ExceptionHandler;
 import org.eclipse.uml2.uml.ExecutableNode;
 import org.eclipse.uml2.uml.ExpansionNode;
 import org.eclipse.uml2.uml.ExpansionRegion;
 import org.eclipse.uml2.uml.FlowFinalNode;
 import org.eclipse.uml2.uml.ForkNode;
 import org.eclipse.uml2.uml.InitialNode;
 import org.eclipse.uml2.uml.InputPin;
 import org.eclipse.uml2.uml.JoinNode;
 import org.eclipse.uml2.uml.LoopNode;
 import org.eclipse.uml2.uml.MergeNode;
 import org.eclipse.uml2.uml.ObjectFlow;
 import org.eclipse.uml2.uml.ObjectNode;
 import org.eclipse.uml2.uml.OpaqueAction;
 import org.eclipse.uml2.uml.OpaqueBehavior;
 import org.eclipse.uml2.uml.OutputPin;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.PackageableElement;
 import org.eclipse.uml2.uml.Parameter;
 import org.eclipse.uml2.uml.ParameterSet;
 import org.eclipse.uml2.uml.Pin;
 import org.eclipse.uml2.uml.SendSignalAction;
 import org.eclipse.uml2.uml.StructuredActivityNode;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.ValueSpecification;
 import org.eclipse.uml2.uml.ValueSpecificationAction;
 
 /**
  * @generated
  */
 public class UMLDiagramUpdater {
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getSemanticChildren(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case PackageEditPart.VISUAL_ID:
 			return getPackage_1000SemanticChildren(view);
 		case ActivityEditPart.VISUAL_ID:
 			return getActivity_2026SemanticChildren(view);
 		case OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3029SemanticChildren(view);
 		case CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3042SemanticChildren(view);
 		case AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3043SemanticChildren(view);
 		case CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3044SemanticChildren(view);
 		case CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3045SemanticChildren(view);
 		case StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3011SemanticChildren(view);
 		case StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3018SemanticChildren(view);
 		case StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3019SemanticChildren(view);
 		case StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3020SemanticChildren(view);
 		case StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3023SemanticChildren(view);
 		case ActivityPartitionEditPart.VISUAL_ID:
 			return getActivityPartition_3056SemanticChildren(view);
 		case ActivityPartition_ActivityPartitionEditPart.VISUAL_ID:
 			return getActivityPartition_3057SemanticChildren(view);
 		case ActivityPartition_OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3067SemanticChildren(view);
 		case ActivityPartition_CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3072SemanticChildren(view);
 		case ActivityPartition_AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3073SemanticChildren(view);
 		case ActivityPartition_CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3074SemanticChildren(view);
 		case ActivityPartition_CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3075SemanticChildren(view);
 		case ActivityPartition_ValueSpecificationActionEditPart.VISUAL_ID:
 			return getValueSpecificationAction_3088SemanticChildren(view);
 		case ParameterSetEditPart.VISUAL_ID:
 			return getParameterSet_3086SemanticChildren(view);
 		case ValueSpecificationActionEditPart.VISUAL_ID:
 			return getValueSpecificationAction_3089SemanticChildren(view);
 		case StructuredActivityNodeContentPaneCompartmentEditPart.VISUAL_ID:
 			return getStructuredActivityNodeStructuredActivityContentPaneCompartment_7008SemanticChildren(view);
 		case StructuredActivityNode_StructuredActivityNodeContentPaneCompartmentEditPart.VISUAL_ID:
 			return getStructuredActivityNodeStructuredActivityContentPaneCompartment_7002SemanticChildren(view);
 		case ConditionalNodeConditionalNodeCompartmentEditPart.VISUAL_ID:
 			return getConditionalNodeConditionalNodeCompartment_7023SemanticChildren(view);
 		case ActivityPartition_StructuredActivityNodeContentPaneCompartmentEditPart.VISUAL_ID:
 			return getStructuredActivityNodeStructuredActivityContentPaneCompartment_7016SemanticChildren(view);
 		case ActivityPartition_StructuredActivityNode_StructuredActivityNodeContentPaneCompartmentEditPart.VISUAL_ID:
 			return getStructuredActivityNodeStructuredActivityContentPaneCompartment_7018SemanticChildren(view);
 		case ActivityPartition_LoopNodeContentPaneCompartmentEditPart.VISUAL_ID:
 			return getLoopNodeLoopNodeContentPaneCompartment_7017SemanticChildren(view);
 		case ActivityPartition_ConditionalNodeCompartmentEditPart.VISUAL_ID:
 			return getConditionalNodeConditionalNodeCompartment_7020SemanticChildren(view);
 		case ActivityPartition_ExpansionRegionNodeCompartmentEditPart.VISUAL_ID:
 			return getExpansionRegionExpansionRegionNodeCompartment_7022SemanticChildren(view);
 		case LoopNodeContentPaneCompartmentEditPart.VISUAL_ID:
 			return getLoopNodeLoopNodeContentPaneCompartment_7015SemanticChildren(view);
 		case ConditionalNodeCompartmentEditPart.VISUAL_ID:
 			return getConditionalNodeConditionalNodeCompartment_7019SemanticChildren(view);
 		case ExpansionRegionNodeCompartmentEditPart.VISUAL_ID:
 			return getExpansionRegionExpansionRegionNodeCompartment_7021SemanticChildren(view);
 		case LocalPreconditionCompartmentEditPart.VISUAL_ID:
 			return getConstraintPrecondition_7013SemanticChildren(view);
 		case LocalPostconditionCompartmentEditPart.VISUAL_ID:
 			return getConstraintPostcondition_7014SemanticChildren(view);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getActivity_2026SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Activity modelElement = (Activity) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == MergeNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == InitialNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityParameterNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == SendSignalActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ValueSpecificationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getGroups().iterator(); it.hasNext();) {
 			ActivityGroup childElement = (ActivityGroup) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartitionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == LoopNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ConditionalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ExpansionRegionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getOwnedBehaviors().iterator(); it.hasNext();) {
 			Behavior childElement = (Behavior) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == OpaqueBehaviorEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getOwnedParameterSets().iterator(); it.hasNext();) {
 			ParameterSet childElement = (ParameterSet) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ParameterSetEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getOpaqueAction_3029SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOutputValues().iterator(); it.hasNext();) {
 			OutputPin childElement = (OutputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == OpaqueAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getInputValues().iterator(); it.hasNext();) {
 			InputPin childElement = (InputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == OpaqueAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getCreateObjectAction_3042SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			OutputPin childElement = modelElement.getResult();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CreateObjectAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getAddStructuralFeatureValueAction_3043SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			InputPin childElement = modelElement.getInsertAt();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AddStructuralFeatureValueAction_insertAt_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		{
 			InputPin childElement = modelElement.getValue();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AddStructuralFeatureValueAction_value_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		{
 			InputPin childElement = modelElement.getObject();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AddStructuralFeatureValueAction_object_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getCallBehaviorAction_3044SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getResults().iterator(); it.hasNext();) {
 			OutputPin childElement = (OutputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getArguments().iterator(); it.hasNext();) {
 			InputPin childElement = (InputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getCallOperationAction_3045SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getResults().iterator(); it.hasNext();) {
 			OutputPin childElement = (OutputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getArguments().iterator(); it.hasNext();) {
 			InputPin childElement = (InputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		{
 			InputPin childElement = modelElement.getTarget();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallOperationAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getOpaqueAction_3011SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOutputValues().iterator(); it.hasNext();) {
 			OutputPin childElement = (OutputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == OpaqueAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getInputValues().iterator(); it.hasNext();) {
 			InputPin childElement = (InputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == OpaqueAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getCreateObjectAction_3018SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			OutputPin childElement = modelElement.getResult();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CreateObjectAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getCallBehaviorAction_3019SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getResults().iterator(); it.hasNext();) {
 			OutputPin childElement = (OutputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getArguments().iterator(); it.hasNext();) {
 			InputPin childElement = (InputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getCallOperationAction_3020SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getResults().iterator(); it.hasNext();) {
 			OutputPin childElement = (OutputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getArguments().iterator(); it.hasNext();) {
 			InputPin childElement = (InputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		{
 			InputPin childElement = modelElement.getTarget();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallOperationAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getAddStructuralFeatureValueAction_3023SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			InputPin childElement = modelElement.getInsertAt();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AddStructuralFeatureValueAction_insertAt_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		{
 			InputPin childElement = modelElement.getValue();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AddStructuralFeatureValueAction_value_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		{
 			InputPin childElement = modelElement.getObject();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AddStructuralFeatureValueAction_object_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getActivityPartition_3056SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ActivityPartition modelElement = (ActivityPartition) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getSubpartitions().iterator(); it.hasNext();) {
 			ActivityPartition childElement = (ActivityPartition) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ActivityPartition_ActivityPartitionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ActivityPartition_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_MergeNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_InitialNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_SendSignalActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_LoopNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ConditionalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ExpansionRegionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ValueSpecificationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getActivityPartition_3057SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ActivityPartition modelElement = (ActivityPartition) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getSubpartitions().iterator(); it.hasNext();) {
 			ActivityPartition childElement = (ActivityPartition) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ActivityPartition_ActivityPartitionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ActivityPartition_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_MergeNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_InitialNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_SendSignalActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_LoopNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ConditionalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ExpansionRegionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ActivityPartition_ValueSpecificationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getOpaqueAction_3067SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOutputValues().iterator(); it.hasNext();) {
 			OutputPin childElement = (OutputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == OpaqueAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getCreateObjectAction_3072SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			OutputPin childElement = modelElement.getResult();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CreateObjectAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getAddStructuralFeatureValueAction_3073SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			InputPin childElement = modelElement.getInsertAt();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AddStructuralFeatureValueAction_insertAt_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		{
 			InputPin childElement = modelElement.getValue();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AddStructuralFeatureValueAction_value_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		{
 			InputPin childElement = modelElement.getObject();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == AddStructuralFeatureValueAction_object_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getCallBehaviorAction_3074SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getResults().iterator(); it.hasNext();) {
 			OutputPin childElement = (OutputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getArguments().iterator(); it.hasNext();) {
 			InputPin childElement = (InputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getCallOperationAction_3075SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getResults().iterator(); it.hasNext();) {
 			OutputPin childElement = (OutputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getArguments().iterator(); it.hasNext();) {
 			InputPin childElement = (InputPin) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		{
 			InputPin childElement = modelElement.getTarget();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CallOperationAction_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getValueSpecificationAction_3088SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ValueSpecificationAction modelElement = (ValueSpecificationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			OutputPin childElement = modelElement.getResult();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ValueSpecificationAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getParameterSet_3086SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ParameterSet modelElement = (ParameterSet) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getParameters().iterator(); it.hasNext();) {
 			Parameter childElement = (Parameter) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ParameterEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getValueSpecificationAction_3089SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ValueSpecificationAction modelElement = (ValueSpecificationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			OutputPin childElement = modelElement.getResult();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ValueSpecificationAction_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getStructuredActivityNodeStructuredActivityContentPaneCompartment_7008SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		StructuredActivityNode modelElement = (StructuredActivityNode) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ConditionalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InitialNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getStructuredActivityNodeStructuredActivityContentPaneCompartment_7002SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		StructuredActivityNode modelElement = (StructuredActivityNode) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ConditionalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InitialNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getConditionalNodeConditionalNodeCompartment_7023SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ConditionalNode modelElement = (ConditionalNode) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getStructuredActivityNodeStructuredActivityContentPaneCompartment_7016SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		StructuredActivityNode modelElement = (StructuredActivityNode) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ActivityPartition_StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getStructuredActivityNodeStructuredActivityContentPaneCompartment_7018SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		StructuredActivityNode modelElement = (StructuredActivityNode) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ActivityPartition_StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getLoopNodeLoopNodeContentPaneCompartment_7017SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		LoopNode modelElement = (LoopNode) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getConditionalNodeConditionalNodeCompartment_7020SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ConditionalNode modelElement = (ConditionalNode) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getExpansionRegionExpansionRegionNodeCompartment_7022SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ExpansionRegion modelElement = (ExpansionRegion) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getLoopNodeLoopNodeContentPaneCompartment_7015SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		LoopNode modelElement = (LoopNode) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getConditionalNodeConditionalNodeCompartment_7019SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ConditionalNode modelElement = (ConditionalNode) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getExpansionRegionExpansionRegionNodeCompartment_7021SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		ExpansionRegion modelElement = (ExpansionRegion) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNodes().iterator(); it.hasNext();) {
 			ActivityNode childElement = (ActivityNode) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_PinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_ForkNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_JoinNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_InputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == StructuredActivityNode_OutputPinEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ExpansionNodeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getConstraintPrecondition_7013SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Constraint modelElement = (Constraint) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			ValueSpecification childElement = modelElement.getSpecification();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == LocalPrecondition_LiteralStringEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getConstraintPostcondition_7014SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Constraint modelElement = (Constraint) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		{
 			ValueSpecification childElement = modelElement.getSpecification();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == LocalPostcondition_LiteralStringEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> getPackage_1000SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
 			PackageableElement childElement = (PackageableElement) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ActivityEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		Resource resource = modelElement.eResource();
		for (Iterator semanticIterator = getPhantomNodesIterator(resource); semanticIterator.hasNext();) {
			EObject childElement = (EObject) semanticIterator.next();
 			if (childElement == modelElement) {
 				continue;
 			}
 			if (UMLVisualIDRegistry.getNodeVisualID(view, childElement) == LocalPreconditionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, LocalPreconditionEditPart.VISUAL_ID));
 				continue;
 			}
 			if (UMLVisualIDRegistry.getNodeVisualID(view, childElement) == LocalPostconditionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLNodeDescriptor(childElement, LocalPostconditionEditPart.VISUAL_ID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Iterator<EObject> getPhantomNodesIterator(Resource resource) {
 		return resource.getAllContents();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getContainedLinks(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case PackageEditPart.VISUAL_ID:
 			return getPackage_1000ContainedLinks(view);
 		case ActivityEditPart.VISUAL_ID:
 			return getActivity_2026ContainedLinks(view);
 		case LocalPreconditionEditPart.VISUAL_ID:
 			return getConstraint_2027ContainedLinks(view);
 		case LocalPostconditionEditPart.VISUAL_ID:
 			return getConstraint_2028ContainedLinks(view);
 		case AcceptEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3030ContainedLinks(view);
 		case AcceptTimeEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3031ContainedLinks(view);
 		case ActivityFinalNodeEditPart.VISUAL_ID:
 			return getActivityFinalNode_3032ContainedLinks(view);
 		case DecisionNodeEditPart.VISUAL_ID:
 			return getDecisionNode_3033ContainedLinks(view);
 		case MergeNodeEditPart.VISUAL_ID:
 			return getMergeNode_3034ContainedLinks(view);
 		case InitialNodeEditPart.VISUAL_ID:
 			return getInitialNode_3035ContainedLinks(view);
 		case DataStoreNodeEditPart.VISUAL_ID:
 			return getDataStoreNode_3036ContainedLinks(view);
 		case CentralBufferNodeEditPart.VISUAL_ID:
 			return getCentralBufferNode_3037ContainedLinks(view);
 		case OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3029ContainedLinks(view);
 		case OpaqueAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3001ContainedLinks(view);
 		case OpaqueAction_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3094ContainedLinks(view);
 		case FlowFinalNodeEditPart.VISUAL_ID:
 			return getFlowFinalNode_3038ContainedLinks(view);
 		case ForkNodeEditPart.VISUAL_ID:
 			return getForkNode_3039ContainedLinks(view);
 		case JoinNodeEditPart.VISUAL_ID:
 			return getJoinNode_3040ContainedLinks(view);
 		case PinEditPart.VISUAL_ID:
 			return getPin_3041ContainedLinks(view);
 		case CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3042ContainedLinks(view);
 		case CreateObjectAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3002ContainedLinks(view);
 		case AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3043ContainedLinks(view);
 		case AddStructuralFeatureValueAction_insertAt_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3003ContainedLinks(view);
 		case AddStructuralFeatureValueAction_value_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3004ContainedLinks(view);
 		case AddStructuralFeatureValueAction_object_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3005ContainedLinks(view);
 		case CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3044ContainedLinks(view);
 		case CallAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3006ContainedLinks(view);
 		case CallAction_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3007ContainedLinks(view);
 		case CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3045ContainedLinks(view);
 		case CallOperationAction_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3008ContainedLinks(view);
 		case StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3046ContainedLinks(view);
 		case StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3009ContainedLinks(view);
 		case StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3011ContainedLinks(view);
 		case StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3012ContainedLinks(view);
 		case StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3013ContainedLinks(view);
 		case StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID:
 			return getActivityFinalNode_3014ContainedLinks(view);
 		case StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID:
 			return getDecisionNode_3015ContainedLinks(view);
 		case StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID:
 			return getFlowFinalNode_3016ContainedLinks(view);
 		case StructuredActivityNode_PinEditPart.VISUAL_ID:
 			return getPin_3017ContainedLinks(view);
 		case StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3018ContainedLinks(view);
 		case StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3019ContainedLinks(view);
 		case StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3020ContainedLinks(view);
 		case StructuredActivityNode_ForkNodeEditPart.VISUAL_ID:
 			return getForkNode_3021ContainedLinks(view);
 		case StructuredActivityNode_JoinNodeEditPart.VISUAL_ID:
 			return getJoinNode_3022ContainedLinks(view);
 		case StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3023ContainedLinks(view);
 		case StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID:
 			return getDataStoreNode_3024ContainedLinks(view);
 		case StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID:
 			return getCentralBufferNode_3025ContainedLinks(view);
 		case StructuredActivityNode_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3054ContainedLinks(view);
 		case StructuredActivityNode_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3055ContainedLinks(view);
 		case StructuredActivityNode_ConditionalNodeEditPart.VISUAL_ID:
 			return getConditionalNode_3092ContainedLinks(view);
 		case StructuredActivityNode_InitialNodeEditPart.VISUAL_ID:
 			return getInitialNode_3093ContainedLinks(view);
 		case OpaqueBehaviorEditPart.VISUAL_ID:
 			return getOpaqueBehavior_3047ContainedLinks(view);
 		case ActivityParameterNodeEditPart.VISUAL_ID:
 			return getActivityParameterNode_3052ContainedLinks(view);
 		case SendSignalActionEditPart.VISUAL_ID:
 			return getSendSignalAction_3053ContainedLinks(view);
 		case ActivityPartitionEditPart.VISUAL_ID:
 			return getActivityPartition_3056ContainedLinks(view);
 		case ActivityPartition_ActivityPartitionEditPart.VISUAL_ID:
 			return getActivityPartition_3057ContainedLinks(view);
 		case ActivityPartition_AcceptEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3059ContainedLinks(view);
 		case ActivityPartition_AcceptTimeEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3060ContainedLinks(view);
 		case ActivityPartition_ActivityFinalNodeEditPart.VISUAL_ID:
 			return getActivityFinalNode_3061ContainedLinks(view);
 		case ActivityPartition_DecisionNodeEditPart.VISUAL_ID:
 			return getDecisionNode_3062ContainedLinks(view);
 		case ActivityPartition_MergeNodeEditPart.VISUAL_ID:
 			return getMergeNode_3063ContainedLinks(view);
 		case ActivityPartition_InitialNodeEditPart.VISUAL_ID:
 			return getInitialNode_3064ContainedLinks(view);
 		case ActivityPartition_DataStoreNodeEditPart.VISUAL_ID:
 			return getDataStoreNode_3065ContainedLinks(view);
 		case ActivityPartition_CentralBufferNodeEditPart.VISUAL_ID:
 			return getCentralBufferNode_3066ContainedLinks(view);
 		case ActivityPartition_OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3067ContainedLinks(view);
 		case ActivityPartition_FlowFinalNodeEditPart.VISUAL_ID:
 			return getFlowFinalNode_3068ContainedLinks(view);
 		case ActivityPartition_ForkNodeEditPart.VISUAL_ID:
 			return getForkNode_3069ContainedLinks(view);
 		case ActivityPartition_JoinNodeEditPart.VISUAL_ID:
 			return getJoinNode_3070ContainedLinks(view);
 		case ActivityPartition_PinEditPart.VISUAL_ID:
 			return getPin_3071ContainedLinks(view);
 		case ActivityPartition_CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3072ContainedLinks(view);
 		case ActivityPartition_AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3073ContainedLinks(view);
 		case ActivityPartition_CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3074ContainedLinks(view);
 		case ActivityPartition_CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3075ContainedLinks(view);
 		case ActivityPartition_StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3076ContainedLinks(view);
 		case ActivityPartition_StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3079ContainedLinks(view);
 		case StructuredActivityNode_StructuredActivityNode_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3080ContainedLinks(view);
 		case StructuredActivityNode_StructuredActivityNode_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3081ContainedLinks(view);
 		case ActivityPartition_SendSignalActionEditPart.VISUAL_ID:
 			return getSendSignalAction_3077ContainedLinks(view);
 		case ActivityPartition_LoopNodeEditPart.VISUAL_ID:
 			return getLoopNode_3078ContainedLinks(view);
 		case ActivityPartition_ConditionalNodeEditPart.VISUAL_ID:
 			return getConditionalNode_3083ContainedLinks(view);
 		case ActivityPartition_ExpansionRegionEditPart.VISUAL_ID:
 			return getExpansionRegion_3085ContainedLinks(view);
 		case ActivityPartition_ValueSpecificationActionEditPart.VISUAL_ID:
 			return getValueSpecificationAction_3088ContainedLinks(view);
 		case ValueSpecificationAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3090ContainedLinks(view);
 		case LoopNodeEditPart.VISUAL_ID:
 			return getLoopNode_3058ContainedLinks(view);
 		case ConditionalNodeEditPart.VISUAL_ID:
 			return getConditionalNode_3082ContainedLinks(view);
 		case ExpansionRegionEditPart.VISUAL_ID:
 			return getExpansionRegion_3084ContainedLinks(view);
 		case ExpansionNodeEditPart.VISUAL_ID:
 			return getExpansionNode_3091ContainedLinks(view);
 		case ParameterSetEditPart.VISUAL_ID:
 			return getParameterSet_3086ContainedLinks(view);
 		case ParameterEditPart.VISUAL_ID:
 			return getParameter_3087ContainedLinks(view);
 		case ValueSpecificationActionEditPart.VISUAL_ID:
 			return getValueSpecificationAction_3089ContainedLinks(view);
 		case LocalPrecondition_LiteralStringEditPart.VISUAL_ID:
 			return getLiteralString_3049ContainedLinks(view);
 		case LocalPostcondition_LiteralStringEditPart.VISUAL_ID:
 			return getLiteralString_3051ContainedLinks(view);
 		case ControlFlowEditPart.VISUAL_ID:
 			return getControlFlow_4001ContainedLinks(view);
 		case ObjectFlowEditPart.VISUAL_ID:
 			return getObjectFlow_4002ContainedLinks(view);
 		case ExceptionHandlerEditPart.VISUAL_ID:
 			return getExceptionHandler_4005ContainedLinks(view);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getIncomingLinks(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case ActivityEditPart.VISUAL_ID:
 			return getActivity_2026IncomingLinks(view);
 		case LocalPreconditionEditPart.VISUAL_ID:
 			return getConstraint_2027IncomingLinks(view);
 		case LocalPostconditionEditPart.VISUAL_ID:
 			return getConstraint_2028IncomingLinks(view);
 		case AcceptEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3030IncomingLinks(view);
 		case AcceptTimeEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3031IncomingLinks(view);
 		case ActivityFinalNodeEditPart.VISUAL_ID:
 			return getActivityFinalNode_3032IncomingLinks(view);
 		case DecisionNodeEditPart.VISUAL_ID:
 			return getDecisionNode_3033IncomingLinks(view);
 		case MergeNodeEditPart.VISUAL_ID:
 			return getMergeNode_3034IncomingLinks(view);
 		case InitialNodeEditPart.VISUAL_ID:
 			return getInitialNode_3035IncomingLinks(view);
 		case DataStoreNodeEditPart.VISUAL_ID:
 			return getDataStoreNode_3036IncomingLinks(view);
 		case CentralBufferNodeEditPart.VISUAL_ID:
 			return getCentralBufferNode_3037IncomingLinks(view);
 		case OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3029IncomingLinks(view);
 		case OpaqueAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3001IncomingLinks(view);
 		case OpaqueAction_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3094IncomingLinks(view);
 		case FlowFinalNodeEditPart.VISUAL_ID:
 			return getFlowFinalNode_3038IncomingLinks(view);
 		case ForkNodeEditPart.VISUAL_ID:
 			return getForkNode_3039IncomingLinks(view);
 		case JoinNodeEditPart.VISUAL_ID:
 			return getJoinNode_3040IncomingLinks(view);
 		case PinEditPart.VISUAL_ID:
 			return getPin_3041IncomingLinks(view);
 		case CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3042IncomingLinks(view);
 		case CreateObjectAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3002IncomingLinks(view);
 		case AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3043IncomingLinks(view);
 		case AddStructuralFeatureValueAction_insertAt_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3003IncomingLinks(view);
 		case AddStructuralFeatureValueAction_value_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3004IncomingLinks(view);
 		case AddStructuralFeatureValueAction_object_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3005IncomingLinks(view);
 		case CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3044IncomingLinks(view);
 		case CallAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3006IncomingLinks(view);
 		case CallAction_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3007IncomingLinks(view);
 		case CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3045IncomingLinks(view);
 		case CallOperationAction_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3008IncomingLinks(view);
 		case StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3046IncomingLinks(view);
 		case StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3009IncomingLinks(view);
 		case StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3011IncomingLinks(view);
 		case StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3012IncomingLinks(view);
 		case StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3013IncomingLinks(view);
 		case StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID:
 			return getActivityFinalNode_3014IncomingLinks(view);
 		case StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID:
 			return getDecisionNode_3015IncomingLinks(view);
 		case StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID:
 			return getFlowFinalNode_3016IncomingLinks(view);
 		case StructuredActivityNode_PinEditPart.VISUAL_ID:
 			return getPin_3017IncomingLinks(view);
 		case StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3018IncomingLinks(view);
 		case StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3019IncomingLinks(view);
 		case StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3020IncomingLinks(view);
 		case StructuredActivityNode_ForkNodeEditPart.VISUAL_ID:
 			return getForkNode_3021IncomingLinks(view);
 		case StructuredActivityNode_JoinNodeEditPart.VISUAL_ID:
 			return getJoinNode_3022IncomingLinks(view);
 		case StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3023IncomingLinks(view);
 		case StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID:
 			return getDataStoreNode_3024IncomingLinks(view);
 		case StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID:
 			return getCentralBufferNode_3025IncomingLinks(view);
 		case StructuredActivityNode_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3054IncomingLinks(view);
 		case StructuredActivityNode_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3055IncomingLinks(view);
 		case StructuredActivityNode_ConditionalNodeEditPart.VISUAL_ID:
 			return getConditionalNode_3092IncomingLinks(view);
 		case StructuredActivityNode_InitialNodeEditPart.VISUAL_ID:
 			return getInitialNode_3093IncomingLinks(view);
 		case OpaqueBehaviorEditPart.VISUAL_ID:
 			return getOpaqueBehavior_3047IncomingLinks(view);
 		case ActivityParameterNodeEditPart.VISUAL_ID:
 			return getActivityParameterNode_3052IncomingLinks(view);
 		case SendSignalActionEditPart.VISUAL_ID:
 			return getSendSignalAction_3053IncomingLinks(view);
 		case ActivityPartitionEditPart.VISUAL_ID:
 			return getActivityPartition_3056IncomingLinks(view);
 		case ActivityPartition_ActivityPartitionEditPart.VISUAL_ID:
 			return getActivityPartition_3057IncomingLinks(view);
 		case ActivityPartition_AcceptEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3059IncomingLinks(view);
 		case ActivityPartition_AcceptTimeEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3060IncomingLinks(view);
 		case ActivityPartition_ActivityFinalNodeEditPart.VISUAL_ID:
 			return getActivityFinalNode_3061IncomingLinks(view);
 		case ActivityPartition_DecisionNodeEditPart.VISUAL_ID:
 			return getDecisionNode_3062IncomingLinks(view);
 		case ActivityPartition_MergeNodeEditPart.VISUAL_ID:
 			return getMergeNode_3063IncomingLinks(view);
 		case ActivityPartition_InitialNodeEditPart.VISUAL_ID:
 			return getInitialNode_3064IncomingLinks(view);
 		case ActivityPartition_DataStoreNodeEditPart.VISUAL_ID:
 			return getDataStoreNode_3065IncomingLinks(view);
 		case ActivityPartition_CentralBufferNodeEditPart.VISUAL_ID:
 			return getCentralBufferNode_3066IncomingLinks(view);
 		case ActivityPartition_OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3067IncomingLinks(view);
 		case ActivityPartition_FlowFinalNodeEditPart.VISUAL_ID:
 			return getFlowFinalNode_3068IncomingLinks(view);
 		case ActivityPartition_ForkNodeEditPart.VISUAL_ID:
 			return getForkNode_3069IncomingLinks(view);
 		case ActivityPartition_JoinNodeEditPart.VISUAL_ID:
 			return getJoinNode_3070IncomingLinks(view);
 		case ActivityPartition_PinEditPart.VISUAL_ID:
 			return getPin_3071IncomingLinks(view);
 		case ActivityPartition_CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3072IncomingLinks(view);
 		case ActivityPartition_AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3073IncomingLinks(view);
 		case ActivityPartition_CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3074IncomingLinks(view);
 		case ActivityPartition_CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3075IncomingLinks(view);
 		case ActivityPartition_StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3076IncomingLinks(view);
 		case ActivityPartition_StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3079IncomingLinks(view);
 		case StructuredActivityNode_StructuredActivityNode_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3080IncomingLinks(view);
 		case StructuredActivityNode_StructuredActivityNode_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3081IncomingLinks(view);
 		case ActivityPartition_SendSignalActionEditPart.VISUAL_ID:
 			return getSendSignalAction_3077IncomingLinks(view);
 		case ActivityPartition_LoopNodeEditPart.VISUAL_ID:
 			return getLoopNode_3078IncomingLinks(view);
 		case ActivityPartition_ConditionalNodeEditPart.VISUAL_ID:
 			return getConditionalNode_3083IncomingLinks(view);
 		case ActivityPartition_ExpansionRegionEditPart.VISUAL_ID:
 			return getExpansionRegion_3085IncomingLinks(view);
 		case ActivityPartition_ValueSpecificationActionEditPart.VISUAL_ID:
 			return getValueSpecificationAction_3088IncomingLinks(view);
 		case ValueSpecificationAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3090IncomingLinks(view);
 		case LoopNodeEditPart.VISUAL_ID:
 			return getLoopNode_3058IncomingLinks(view);
 		case ConditionalNodeEditPart.VISUAL_ID:
 			return getConditionalNode_3082IncomingLinks(view);
 		case ExpansionRegionEditPart.VISUAL_ID:
 			return getExpansionRegion_3084IncomingLinks(view);
 		case ExpansionNodeEditPart.VISUAL_ID:
 			return getExpansionNode_3091IncomingLinks(view);
 		case ParameterSetEditPart.VISUAL_ID:
 			return getParameterSet_3086IncomingLinks(view);
 		case ParameterEditPart.VISUAL_ID:
 			return getParameter_3087IncomingLinks(view);
 		case ValueSpecificationActionEditPart.VISUAL_ID:
 			return getValueSpecificationAction_3089IncomingLinks(view);
 		case LocalPrecondition_LiteralStringEditPart.VISUAL_ID:
 			return getLiteralString_3049IncomingLinks(view);
 		case LocalPostcondition_LiteralStringEditPart.VISUAL_ID:
 			return getLiteralString_3051IncomingLinks(view);
 		case ControlFlowEditPart.VISUAL_ID:
 			return getControlFlow_4001IncomingLinks(view);
 		case ObjectFlowEditPart.VISUAL_ID:
 			return getObjectFlow_4002IncomingLinks(view);
 		case ExceptionHandlerEditPart.VISUAL_ID:
 			return getExceptionHandler_4005IncomingLinks(view);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutgoingLinks(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case ActivityEditPart.VISUAL_ID:
 			return getActivity_2026OutgoingLinks(view);
 		case LocalPreconditionEditPart.VISUAL_ID:
 			return getConstraint_2027OutgoingLinks(view);
 		case LocalPostconditionEditPart.VISUAL_ID:
 			return getConstraint_2028OutgoingLinks(view);
 		case AcceptEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3030OutgoingLinks(view);
 		case AcceptTimeEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3031OutgoingLinks(view);
 		case ActivityFinalNodeEditPart.VISUAL_ID:
 			return getActivityFinalNode_3032OutgoingLinks(view);
 		case DecisionNodeEditPart.VISUAL_ID:
 			return getDecisionNode_3033OutgoingLinks(view);
 		case MergeNodeEditPart.VISUAL_ID:
 			return getMergeNode_3034OutgoingLinks(view);
 		case InitialNodeEditPart.VISUAL_ID:
 			return getInitialNode_3035OutgoingLinks(view);
 		case DataStoreNodeEditPart.VISUAL_ID:
 			return getDataStoreNode_3036OutgoingLinks(view);
 		case CentralBufferNodeEditPart.VISUAL_ID:
 			return getCentralBufferNode_3037OutgoingLinks(view);
 		case OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3029OutgoingLinks(view);
 		case OpaqueAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3001OutgoingLinks(view);
 		case OpaqueAction_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3094OutgoingLinks(view);
 		case FlowFinalNodeEditPart.VISUAL_ID:
 			return getFlowFinalNode_3038OutgoingLinks(view);
 		case ForkNodeEditPart.VISUAL_ID:
 			return getForkNode_3039OutgoingLinks(view);
 		case JoinNodeEditPart.VISUAL_ID:
 			return getJoinNode_3040OutgoingLinks(view);
 		case PinEditPart.VISUAL_ID:
 			return getPin_3041OutgoingLinks(view);
 		case CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3042OutgoingLinks(view);
 		case CreateObjectAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3002OutgoingLinks(view);
 		case AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3043OutgoingLinks(view);
 		case AddStructuralFeatureValueAction_insertAt_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3003OutgoingLinks(view);
 		case AddStructuralFeatureValueAction_value_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3004OutgoingLinks(view);
 		case AddStructuralFeatureValueAction_object_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3005OutgoingLinks(view);
 		case CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3044OutgoingLinks(view);
 		case CallAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3006OutgoingLinks(view);
 		case CallAction_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3007OutgoingLinks(view);
 		case CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3045OutgoingLinks(view);
 		case CallOperationAction_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3008OutgoingLinks(view);
 		case StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3046OutgoingLinks(view);
 		case StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3009OutgoingLinks(view);
 		case StructuredActivityNode_OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3011OutgoingLinks(view);
 		case StructuredActivityNode_AcceptEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3012OutgoingLinks(view);
 		case StructuredActivityNode_AcceptTimeEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3013OutgoingLinks(view);
 		case StructuredActivityNode_ActivityFinalNodeEditPart.VISUAL_ID:
 			return getActivityFinalNode_3014OutgoingLinks(view);
 		case StructuredActivityNode_DecisionNodeEditPart.VISUAL_ID:
 			return getDecisionNode_3015OutgoingLinks(view);
 		case StructuredActivityNode_FlowFinalNodeEditPart.VISUAL_ID:
 			return getFlowFinalNode_3016OutgoingLinks(view);
 		case StructuredActivityNode_PinEditPart.VISUAL_ID:
 			return getPin_3017OutgoingLinks(view);
 		case StructuredActivityNode_CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3018OutgoingLinks(view);
 		case StructuredActivityNode_CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3019OutgoingLinks(view);
 		case StructuredActivityNode_CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3020OutgoingLinks(view);
 		case StructuredActivityNode_ForkNodeEditPart.VISUAL_ID:
 			return getForkNode_3021OutgoingLinks(view);
 		case StructuredActivityNode_JoinNodeEditPart.VISUAL_ID:
 			return getJoinNode_3022OutgoingLinks(view);
 		case StructuredActivityNode_AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3023OutgoingLinks(view);
 		case StructuredActivityNode_DataStoreNodeEditPart.VISUAL_ID:
 			return getDataStoreNode_3024OutgoingLinks(view);
 		case StructuredActivityNode_CentralBufferNodeEditPart.VISUAL_ID:
 			return getCentralBufferNode_3025OutgoingLinks(view);
 		case StructuredActivityNode_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3054OutgoingLinks(view);
 		case StructuredActivityNode_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3055OutgoingLinks(view);
 		case StructuredActivityNode_ConditionalNodeEditPart.VISUAL_ID:
 			return getConditionalNode_3092OutgoingLinks(view);
 		case StructuredActivityNode_InitialNodeEditPart.VISUAL_ID:
 			return getInitialNode_3093OutgoingLinks(view);
 		case OpaqueBehaviorEditPart.VISUAL_ID:
 			return getOpaqueBehavior_3047OutgoingLinks(view);
 		case ActivityParameterNodeEditPart.VISUAL_ID:
 			return getActivityParameterNode_3052OutgoingLinks(view);
 		case SendSignalActionEditPart.VISUAL_ID:
 			return getSendSignalAction_3053OutgoingLinks(view);
 		case ActivityPartitionEditPart.VISUAL_ID:
 			return getActivityPartition_3056OutgoingLinks(view);
 		case ActivityPartition_ActivityPartitionEditPart.VISUAL_ID:
 			return getActivityPartition_3057OutgoingLinks(view);
 		case ActivityPartition_AcceptEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3059OutgoingLinks(view);
 		case ActivityPartition_AcceptTimeEventActionEditPart.VISUAL_ID:
 			return getAcceptEventAction_3060OutgoingLinks(view);
 		case ActivityPartition_ActivityFinalNodeEditPart.VISUAL_ID:
 			return getActivityFinalNode_3061OutgoingLinks(view);
 		case ActivityPartition_DecisionNodeEditPart.VISUAL_ID:
 			return getDecisionNode_3062OutgoingLinks(view);
 		case ActivityPartition_MergeNodeEditPart.VISUAL_ID:
 			return getMergeNode_3063OutgoingLinks(view);
 		case ActivityPartition_InitialNodeEditPart.VISUAL_ID:
 			return getInitialNode_3064OutgoingLinks(view);
 		case ActivityPartition_DataStoreNodeEditPart.VISUAL_ID:
 			return getDataStoreNode_3065OutgoingLinks(view);
 		case ActivityPartition_CentralBufferNodeEditPart.VISUAL_ID:
 			return getCentralBufferNode_3066OutgoingLinks(view);
 		case ActivityPartition_OpaqueActionEditPart.VISUAL_ID:
 			return getOpaqueAction_3067OutgoingLinks(view);
 		case ActivityPartition_FlowFinalNodeEditPart.VISUAL_ID:
 			return getFlowFinalNode_3068OutgoingLinks(view);
 		case ActivityPartition_ForkNodeEditPart.VISUAL_ID:
 			return getForkNode_3069OutgoingLinks(view);
 		case ActivityPartition_JoinNodeEditPart.VISUAL_ID:
 			return getJoinNode_3070OutgoingLinks(view);
 		case ActivityPartition_PinEditPart.VISUAL_ID:
 			return getPin_3071OutgoingLinks(view);
 		case ActivityPartition_CreateObjectActionEditPart.VISUAL_ID:
 			return getCreateObjectAction_3072OutgoingLinks(view);
 		case ActivityPartition_AddStructuralFeatureValueActionEditPart.VISUAL_ID:
 			return getAddStructuralFeatureValueAction_3073OutgoingLinks(view);
 		case ActivityPartition_CallBehaviorActionEditPart.VISUAL_ID:
 			return getCallBehaviorAction_3074OutgoingLinks(view);
 		case ActivityPartition_CallOperationActionEditPart.VISUAL_ID:
 			return getCallOperationAction_3075OutgoingLinks(view);
 		case ActivityPartition_StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3076OutgoingLinks(view);
 		case ActivityPartition_StructuredActivityNode_StructuredActivityNodeEditPart.VISUAL_ID:
 			return getStructuredActivityNode_3079OutgoingLinks(view);
 		case StructuredActivityNode_StructuredActivityNode_InputPinEditPart.VISUAL_ID:
 			return getInputPin_3080OutgoingLinks(view);
 		case StructuredActivityNode_StructuredActivityNode_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3081OutgoingLinks(view);
 		case ActivityPartition_SendSignalActionEditPart.VISUAL_ID:
 			return getSendSignalAction_3077OutgoingLinks(view);
 		case ActivityPartition_LoopNodeEditPart.VISUAL_ID:
 			return getLoopNode_3078OutgoingLinks(view);
 		case ActivityPartition_ConditionalNodeEditPart.VISUAL_ID:
 			return getConditionalNode_3083OutgoingLinks(view);
 		case ActivityPartition_ExpansionRegionEditPart.VISUAL_ID:
 			return getExpansionRegion_3085OutgoingLinks(view);
 		case ActivityPartition_ValueSpecificationActionEditPart.VISUAL_ID:
 			return getValueSpecificationAction_3088OutgoingLinks(view);
 		case ValueSpecificationAction_OutputPinEditPart.VISUAL_ID:
 			return getOutputPin_3090OutgoingLinks(view);
 		case LoopNodeEditPart.VISUAL_ID:
 			return getLoopNode_3058OutgoingLinks(view);
 		case ConditionalNodeEditPart.VISUAL_ID:
 			return getConditionalNode_3082OutgoingLinks(view);
 		case ExpansionRegionEditPart.VISUAL_ID:
 			return getExpansionRegion_3084OutgoingLinks(view);
 		case ExpansionNodeEditPart.VISUAL_ID:
 			return getExpansionNode_3091OutgoingLinks(view);
 		case ParameterSetEditPart.VISUAL_ID:
 			return getParameterSet_3086OutgoingLinks(view);
 		case ParameterEditPart.VISUAL_ID:
 			return getParameter_3087OutgoingLinks(view);
 		case ValueSpecificationActionEditPart.VISUAL_ID:
 			return getValueSpecificationAction_3089OutgoingLinks(view);
 		case LocalPrecondition_LiteralStringEditPart.VISUAL_ID:
 			return getLiteralString_3049OutgoingLinks(view);
 		case LocalPostcondition_LiteralStringEditPart.VISUAL_ID:
 			return getLiteralString_3051OutgoingLinks(view);
 		case ControlFlowEditPart.VISUAL_ID:
 			return getControlFlow_4001OutgoingLinks(view);
 		case ObjectFlowEditPart.VISUAL_ID:
 			return getObjectFlow_4002OutgoingLinks(view);
 		case ExceptionHandlerEditPart.VISUAL_ID:
 			return getExceptionHandler_4005OutgoingLinks(view);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPackage_1000ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivity_2026ContainedLinks(View view) {
 		Activity modelElement = (Activity) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConstraint_2027ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConstraint_2028ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3030ContainedLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3031ContainedLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityFinalNode_3032ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDecisionNode_3033ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getMergeNode_3034ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInitialNode_3035ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDataStoreNode_3036ContainedLinks(View view) {
 		DataStoreNode modelElement = (DataStoreNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCentralBufferNode_3037ContainedLinks(View view) {
 		CentralBufferNode modelElement = (CentralBufferNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueAction_3029ContainedLinks(View view) {
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3001ContainedLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3094ContainedLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getFlowFinalNode_3038ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getForkNode_3039ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getJoinNode_3040ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPin_3041ContainedLinks(View view) {
 		Pin modelElement = (Pin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCreateObjectAction_3042ContainedLinks(View view) {
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3002ContainedLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAddStructuralFeatureValueAction_3043ContainedLinks(View view) {
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3003ContainedLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3004ContainedLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3005ContainedLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallBehaviorAction_3044ContainedLinks(View view) {
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3006ContainedLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3007ContainedLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallOperationAction_3045ContainedLinks(View view) {
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3008ContainedLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3046ContainedLinksGen(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static List<UMLLinkDescriptor> getStructuredActivityNode_3046ContainedLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		List<UMLLinkDescriptor> result = getStructuredActivityNode_3046ContainedLinksGen(view);
 		result.addAll(getContainedTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3009ContainedLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueAction_3011ContainedLinks(View view) {
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3012ContainedLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3013ContainedLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityFinalNode_3014ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDecisionNode_3015ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getFlowFinalNode_3016ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPin_3017ContainedLinks(View view) {
 		Pin modelElement = (Pin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCreateObjectAction_3018ContainedLinks(View view) {
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallBehaviorAction_3019ContainedLinks(View view) {
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallOperationAction_3020ContainedLinks(View view) {
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getForkNode_3021ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getJoinNode_3022ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAddStructuralFeatureValueAction_3023ContainedLinks(View view) {
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDataStoreNode_3024ContainedLinks(View view) {
 		DataStoreNode modelElement = (DataStoreNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCentralBufferNode_3025ContainedLinks(View view) {
 		CentralBufferNode modelElement = (CentralBufferNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3054ContainedLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3055ContainedLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConditionalNode_3092ContainedLinks(View view) {
 		ConditionalNode modelElement = (ConditionalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInitialNode_3093ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueBehavior_3047ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityParameterNode_3052ContainedLinks(View view) {
 		ActivityParameterNode modelElement = (ActivityParameterNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getSendSignalAction_3053ContainedLinks(View view) {
 		SendSignalAction modelElement = (SendSignalAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityPartition_3056ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityPartition_3057ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3059ContainedLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3060ContainedLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityFinalNode_3061ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDecisionNode_3062ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getMergeNode_3063ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInitialNode_3064ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDataStoreNode_3065ContainedLinks(View view) {
 		DataStoreNode modelElement = (DataStoreNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCentralBufferNode_3066ContainedLinks(View view) {
 		CentralBufferNode modelElement = (CentralBufferNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueAction_3067ContainedLinks(View view) {
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getFlowFinalNode_3068ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getForkNode_3069ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getJoinNode_3070ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPin_3071ContainedLinks(View view) {
 		Pin modelElement = (Pin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCreateObjectAction_3072ContainedLinks(View view) {
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAddStructuralFeatureValueAction_3073ContainedLinks(View view) {
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallBehaviorAction_3074ContainedLinks(View view) {
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallOperationAction_3075ContainedLinks(View view) {
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3076ContainedLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3079ContainedLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3080ContainedLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3081ContainedLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getSendSignalAction_3077ContainedLinks(View view) {
 		SendSignalAction modelElement = (SendSignalAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLoopNode_3078ContainedLinks(View view) {
 		LoopNode modelElement = (LoopNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConditionalNode_3083ContainedLinks(View view) {
 		ConditionalNode modelElement = (ConditionalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExpansionRegion_3085ContainedLinks(View view) {
 		ExpansionRegion modelElement = (ExpansionRegion) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getValueSpecificationAction_3088ContainedLinks(View view) {
 		ValueSpecificationAction modelElement = (ValueSpecificationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3090ContainedLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLoopNode_3058ContainedLinks(View view) {
 		LoopNode modelElement = (LoopNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConditionalNode_3082ContainedLinks(View view) {
 		ConditionalNode modelElement = (ConditionalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExpansionRegion_3084ContainedLinks(View view) {
 		ExpansionRegion modelElement = (ExpansionRegion) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExpansionNode_3091ContainedLinks(View view) {
 		ExpansionNode modelElement = (ExpansionNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getParameterSet_3086ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getParameter_3087ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getValueSpecificationAction_3089ContainedLinks(View view) {
 		ValueSpecificationAction modelElement = (ValueSpecificationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLiteralString_3049ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLiteralString_3051ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getControlFlow_4001ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getObjectFlow_4002ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExceptionHandler_4005ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivity_2026IncomingLinks(View view) {
 		Activity modelElement = (Activity) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConstraint_2027IncomingLinks(View view) {
 		Constraint modelElement = (Constraint) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConstraint_2028IncomingLinks(View view) {
 		Constraint modelElement = (Constraint) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3030IncomingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3031IncomingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityFinalNode_3032IncomingLinks(View view) {
 		ActivityFinalNode modelElement = (ActivityFinalNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDecisionNode_3033IncomingLinks(View view) {
 		DecisionNode modelElement = (DecisionNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getMergeNode_3034IncomingLinks(View view) {
 		MergeNode modelElement = (MergeNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInitialNode_3035IncomingLinks(View view) {
 		InitialNode modelElement = (InitialNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDataStoreNode_3036IncomingLinks(View view) {
 		DataStoreNode modelElement = (DataStoreNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCentralBufferNode_3037IncomingLinks(View view) {
 		CentralBufferNode modelElement = (CentralBufferNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueAction_3029IncomingLinks(View view) {
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3001IncomingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3094IncomingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getFlowFinalNode_3038IncomingLinks(View view) {
 		FlowFinalNode modelElement = (FlowFinalNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getForkNode_3039IncomingLinks(View view) {
 		ForkNode modelElement = (ForkNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getJoinNode_3040IncomingLinks(View view) {
 		JoinNode modelElement = (JoinNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPin_3041IncomingLinks(View view) {
 		Pin modelElement = (Pin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCreateObjectAction_3042IncomingLinks(View view) {
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3002IncomingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAddStructuralFeatureValueAction_3043IncomingLinks(View view) {
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3003IncomingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3004IncomingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3005IncomingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallBehaviorAction_3044IncomingLinks(View view) {
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3006IncomingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3007IncomingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallOperationAction_3045IncomingLinks(View view) {
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3008IncomingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3046IncomingLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3009IncomingLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueAction_3011IncomingLinks(View view) {
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3012IncomingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3013IncomingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityFinalNode_3014IncomingLinks(View view) {
 		ActivityFinalNode modelElement = (ActivityFinalNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDecisionNode_3015IncomingLinks(View view) {
 		DecisionNode modelElement = (DecisionNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getFlowFinalNode_3016IncomingLinks(View view) {
 		FlowFinalNode modelElement = (FlowFinalNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPin_3017IncomingLinks(View view) {
 		Pin modelElement = (Pin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCreateObjectAction_3018IncomingLinks(View view) {
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallBehaviorAction_3019IncomingLinks(View view) {
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallOperationAction_3020IncomingLinks(View view) {
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getForkNode_3021IncomingLinks(View view) {
 		ForkNode modelElement = (ForkNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getJoinNode_3022IncomingLinks(View view) {
 		JoinNode modelElement = (JoinNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAddStructuralFeatureValueAction_3023IncomingLinks(View view) {
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDataStoreNode_3024IncomingLinks(View view) {
 		DataStoreNode modelElement = (DataStoreNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCentralBufferNode_3025IncomingLinks(View view) {
 		CentralBufferNode modelElement = (CentralBufferNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3054IncomingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3055IncomingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConditionalNode_3092IncomingLinks(View view) {
 		ConditionalNode modelElement = (ConditionalNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInitialNode_3093IncomingLinks(View view) {
 		InitialNode modelElement = (InitialNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueBehavior_3047IncomingLinks(View view) {
 		OpaqueBehavior modelElement = (OpaqueBehavior) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityParameterNode_3052IncomingLinks(View view) {
 		ActivityParameterNode modelElement = (ActivityParameterNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getSendSignalAction_3053IncomingLinks(View view) {
 		SendSignalAction modelElement = (SendSignalAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityPartition_3056IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityPartition_3057IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3059IncomingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3060IncomingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityFinalNode_3061IncomingLinks(View view) {
 		ActivityFinalNode modelElement = (ActivityFinalNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDecisionNode_3062IncomingLinks(View view) {
 		DecisionNode modelElement = (DecisionNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getMergeNode_3063IncomingLinks(View view) {
 		MergeNode modelElement = (MergeNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInitialNode_3064IncomingLinks(View view) {
 		InitialNode modelElement = (InitialNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDataStoreNode_3065IncomingLinks(View view) {
 		DataStoreNode modelElement = (DataStoreNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCentralBufferNode_3066IncomingLinks(View view) {
 		CentralBufferNode modelElement = (CentralBufferNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueAction_3067IncomingLinks(View view) {
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getFlowFinalNode_3068IncomingLinks(View view) {
 		FlowFinalNode modelElement = (FlowFinalNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getForkNode_3069IncomingLinks(View view) {
 		ForkNode modelElement = (ForkNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getJoinNode_3070IncomingLinks(View view) {
 		JoinNode modelElement = (JoinNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPin_3071IncomingLinks(View view) {
 		Pin modelElement = (Pin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCreateObjectAction_3072IncomingLinks(View view) {
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAddStructuralFeatureValueAction_3073IncomingLinks(View view) {
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallBehaviorAction_3074IncomingLinks(View view) {
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallOperationAction_3075IncomingLinks(View view) {
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3076IncomingLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3079IncomingLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3080IncomingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3081IncomingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getSendSignalAction_3077IncomingLinks(View view) {
 		SendSignalAction modelElement = (SendSignalAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLoopNode_3078IncomingLinks(View view) {
 		LoopNode modelElement = (LoopNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConditionalNode_3083IncomingLinks(View view) {
 		ConditionalNode modelElement = (ConditionalNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExpansionRegion_3085IncomingLinks(View view) {
 		ExpansionRegion modelElement = (ExpansionRegion) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getValueSpecificationAction_3088IncomingLinks(View view) {
 		ValueSpecificationAction modelElement = (ValueSpecificationAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3090IncomingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLoopNode_3058IncomingLinks(View view) {
 		LoopNode modelElement = (LoopNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConditionalNode_3082IncomingLinks(View view) {
 		ConditionalNode modelElement = (ConditionalNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExpansionRegion_3084IncomingLinks(View view) {
 		ExpansionRegion modelElement = (ExpansionRegion) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExpansionNode_3091IncomingLinks(View view) {
 		ExpansionNode modelElement = (ExpansionNode) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getParameterSet_3086IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getParameter_3087IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getValueSpecificationAction_3089IncomingLinks(View view) {
 		ValueSpecificationAction modelElement = (ValueSpecificationAction) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_ControlFlow_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ObjectFlow_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_ExceptionHandler_4005(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLiteralString_3049IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLiteralString_3051IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getControlFlow_4001IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getObjectFlow_4002IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExceptionHandler_4005IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivity_2026OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConstraint_2027OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConstraint_2028OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3030OutgoingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3031OutgoingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityFinalNode_3032OutgoingLinks(View view) {
 		ActivityFinalNode modelElement = (ActivityFinalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDecisionNode_3033OutgoingLinks(View view) {
 		DecisionNode modelElement = (DecisionNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getMergeNode_3034OutgoingLinks(View view) {
 		MergeNode modelElement = (MergeNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInitialNode_3035OutgoingLinks(View view) {
 		InitialNode modelElement = (InitialNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDataStoreNode_3036OutgoingLinks(View view) {
 		DataStoreNode modelElement = (DataStoreNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCentralBufferNode_3037OutgoingLinks(View view) {
 		CentralBufferNode modelElement = (CentralBufferNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueAction_3029OutgoingLinks(View view) {
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3001OutgoingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3094OutgoingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getFlowFinalNode_3038OutgoingLinks(View view) {
 		FlowFinalNode modelElement = (FlowFinalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getForkNode_3039OutgoingLinks(View view) {
 		ForkNode modelElement = (ForkNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getJoinNode_3040OutgoingLinks(View view) {
 		JoinNode modelElement = (JoinNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPin_3041OutgoingLinks(View view) {
 		Pin modelElement = (Pin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCreateObjectAction_3042OutgoingLinks(View view) {
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3002OutgoingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAddStructuralFeatureValueAction_3043OutgoingLinks(View view) {
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3003OutgoingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3004OutgoingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3005OutgoingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallBehaviorAction_3044OutgoingLinks(View view) {
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3006OutgoingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3007OutgoingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallOperationAction_3045OutgoingLinks(View view) {
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3008OutgoingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3046OutgoingLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3009OutgoingLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueAction_3011OutgoingLinks(View view) {
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3012OutgoingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3013OutgoingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityFinalNode_3014OutgoingLinks(View view) {
 		ActivityFinalNode modelElement = (ActivityFinalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDecisionNode_3015OutgoingLinks(View view) {
 		DecisionNode modelElement = (DecisionNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getFlowFinalNode_3016OutgoingLinks(View view) {
 		FlowFinalNode modelElement = (FlowFinalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPin_3017OutgoingLinks(View view) {
 		Pin modelElement = (Pin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCreateObjectAction_3018OutgoingLinks(View view) {
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallBehaviorAction_3019OutgoingLinks(View view) {
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallOperationAction_3020OutgoingLinks(View view) {
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getForkNode_3021OutgoingLinks(View view) {
 		ForkNode modelElement = (ForkNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getJoinNode_3022OutgoingLinks(View view) {
 		JoinNode modelElement = (JoinNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAddStructuralFeatureValueAction_3023OutgoingLinks(View view) {
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDataStoreNode_3024OutgoingLinks(View view) {
 		DataStoreNode modelElement = (DataStoreNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCentralBufferNode_3025OutgoingLinks(View view) {
 		CentralBufferNode modelElement = (CentralBufferNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3054OutgoingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3055OutgoingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConditionalNode_3092OutgoingLinks(View view) {
 		ConditionalNode modelElement = (ConditionalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInitialNode_3093OutgoingLinks(View view) {
 		InitialNode modelElement = (InitialNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueBehavior_3047OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityParameterNode_3052OutgoingLinks(View view) {
 		ActivityParameterNode modelElement = (ActivityParameterNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getSendSignalAction_3053OutgoingLinks(View view) {
 		SendSignalAction modelElement = (SendSignalAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityPartition_3056OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityPartition_3057OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3059OutgoingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAcceptEventAction_3060OutgoingLinks(View view) {
 		AcceptEventAction modelElement = (AcceptEventAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getActivityFinalNode_3061OutgoingLinks(View view) {
 		ActivityFinalNode modelElement = (ActivityFinalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDecisionNode_3062OutgoingLinks(View view) {
 		DecisionNode modelElement = (DecisionNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getMergeNode_3063OutgoingLinks(View view) {
 		MergeNode modelElement = (MergeNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInitialNode_3064OutgoingLinks(View view) {
 		InitialNode modelElement = (InitialNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getDataStoreNode_3065OutgoingLinks(View view) {
 		DataStoreNode modelElement = (DataStoreNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCentralBufferNode_3066OutgoingLinks(View view) {
 		CentralBufferNode modelElement = (CentralBufferNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOpaqueAction_3067OutgoingLinks(View view) {
 		OpaqueAction modelElement = (OpaqueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getFlowFinalNode_3068OutgoingLinks(View view) {
 		FlowFinalNode modelElement = (FlowFinalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getForkNode_3069OutgoingLinks(View view) {
 		ForkNode modelElement = (ForkNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getJoinNode_3070OutgoingLinks(View view) {
 		JoinNode modelElement = (JoinNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getPin_3071OutgoingLinks(View view) {
 		Pin modelElement = (Pin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCreateObjectAction_3072OutgoingLinks(View view) {
 		CreateObjectAction modelElement = (CreateObjectAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getAddStructuralFeatureValueAction_3073OutgoingLinks(View view) {
 		AddStructuralFeatureValueAction modelElement = (AddStructuralFeatureValueAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallBehaviorAction_3074OutgoingLinks(View view) {
 		CallBehaviorAction modelElement = (CallBehaviorAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getCallOperationAction_3075OutgoingLinks(View view) {
 		CallOperationAction modelElement = (CallOperationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3076OutgoingLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getStructuredActivityNode_3079OutgoingLinks(View view) {
 		StructuredActivityNode modelElement = (StructuredActivityNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getInputPin_3080OutgoingLinks(View view) {
 		InputPin modelElement = (InputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3081OutgoingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getSendSignalAction_3077OutgoingLinks(View view) {
 		SendSignalAction modelElement = (SendSignalAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLoopNode_3078OutgoingLinks(View view) {
 		LoopNode modelElement = (LoopNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConditionalNode_3083OutgoingLinks(View view) {
 		ConditionalNode modelElement = (ConditionalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExpansionRegion_3085OutgoingLinks(View view) {
 		ExpansionRegion modelElement = (ExpansionRegion) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getValueSpecificationAction_3088OutgoingLinks(View view) {
 		ValueSpecificationAction modelElement = (ValueSpecificationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutputPin_3090OutgoingLinks(View view) {
 		OutputPin modelElement = (OutputPin) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLoopNode_3058OutgoingLinks(View view) {
 		LoopNode modelElement = (LoopNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getConditionalNode_3082OutgoingLinks(View view) {
 		ConditionalNode modelElement = (ConditionalNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExpansionRegion_3084OutgoingLinks(View view) {
 		ExpansionRegion modelElement = (ExpansionRegion) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExpansionNode_3091OutgoingLinks(View view) {
 		ExpansionNode modelElement = (ExpansionNode) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getParameterSet_3086OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getParameter_3087OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getValueSpecificationAction_3089OutgoingLinks(View view) {
 		ValueSpecificationAction modelElement = (ValueSpecificationAction) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_ControlFlow_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ObjectFlow_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLiteralString_3049OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getLiteralString_3051OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getControlFlow_4001OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getObjectFlow_4002OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getExceptionHandler_4005OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getContainedTypeModelFacetLinks_ControlFlow_4001(Activity container) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getEdges().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof ControlFlow) {
 				continue;
 			}
 			ControlFlow link = (ControlFlow) linkObject;
 			if (ControlFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode dst = link.getTarget();
 			ActivityNode src = link.getSource();
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.ControlFlow_4001, ControlFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	private static Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_ControlFlow_4001(StructuredActivityNode container) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (ActivityEdge linkObject : container.getEdges()) {
 			if (false == linkObject instanceof ControlFlow) {
 				continue;
 			}
 			ControlFlow link = (ControlFlow) linkObject;
 			if (ControlFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode dst = link.getTarget();
 			ActivityNode src = link.getSource();
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.ControlFlow_4001, ControlFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getContainedTypeModelFacetLinks_ObjectFlow_4002(Activity container) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getEdges().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof ObjectFlow) {
 				continue;
 			}
 			ObjectFlow link = (ObjectFlow) linkObject;
 			if (ObjectFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode dst = link.getTarget();
 			ActivityNode src = link.getSource();
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.ObjectFlow_4002, ObjectFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	private static Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_ObjectFlow_4002(StructuredActivityNode container) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (ActivityEdge linkObject : container.getEdges()) {
 			if (false == linkObject instanceof ObjectFlow) {
 				continue;
 			}
 			ObjectFlow link = (ObjectFlow) linkObject;
 			if (ObjectFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode dst = link.getTarget();
 			ActivityNode src = link.getSource();
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.ObjectFlow_4002, ObjectFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getContainedTypeModelFacetLinks_ExceptionHandler_4005(ExecutableNode container) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getHandlers().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof ExceptionHandler) {
 				continue;
 			}
 			ExceptionHandler link = (ExceptionHandler) linkObject;
 			if (ExceptionHandlerEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ExecutableNode dst = link.getHandlerBody();
 			ExecutableNode src = link.getProtectedNode();
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.ExceptionHandler_4005, ExceptionHandlerEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_ControlFlow_4001(ActivityNode target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getActivityEdge_Target() || false == setting.getEObject() instanceof ControlFlow) {
 				continue;
 			}
 			ControlFlow link = (ControlFlow) setting.getEObject();
 			if (ControlFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode src = link.getSource();
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(src, target, link, UMLElementTypes.ControlFlow_4001, ControlFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_ObjectFlow_4002(ActivityNode target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getActivityEdge_Target() || false == setting.getEObject() instanceof ObjectFlow) {
 				continue;
 			}
 			ObjectFlow link = (ObjectFlow) setting.getEObject();
 			if (ObjectFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode src = link.getSource();
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(src, target, link, UMLElementTypes.ObjectFlow_4002, ObjectFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Action_LocalPrecondition_4003(Constraint target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getAction_LocalPrecondition()) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.ActionLocalPrecondition_4003,
 						ActionLocalPreconditionEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Action_LocalPostcondition_4006(Constraint target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getAction_LocalPostcondition()) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.ActionLocalPostcondition_4006,
 						ActionLocalPostconditionEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_ObjectNode_Selection_4004(Behavior target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getObjectNode_Selection()) {
 				result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.ObjectNodeSelection_4004, ObjectNodeSelectionEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_ExceptionHandler_4005(ExecutableNode target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getExceptionHandler_HandlerBody() || false == setting.getEObject() instanceof ExceptionHandler) {
 				continue;
 			}
 			ExceptionHandler link = (ExceptionHandler) setting.getEObject();
 			if (ExceptionHandlerEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ExecutableNode src = link.getProtectedNode();
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(src, target, link, UMLElementTypes.ExceptionHandler_4005, ExceptionHandlerEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_ControlFlow_4001Gen(ActivityNode source) {
 		Activity container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof Activity) {
 				container = (Activity) element;
 			}
 		}
 		if (container == null) {
 			return Collections.emptyList();
 		}
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getEdges().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof ControlFlow) {
 				continue;
 			}
 			ControlFlow link = (ControlFlow) linkObject;
 			if (ControlFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode dst = link.getTarget();
 			ActivityNode src = link.getSource();
 			if (src != source) {
 				continue;
 			}
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.ControlFlow_4001, ControlFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_ControlFlow_4001(ActivityNode source) {
 		StructuredActivityNode container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof StructuredActivityNode) {
 				container = (StructuredActivityNode) element;
 			}
 		}
 		if (container == null) {
 			return UMLDiagramUpdater.getOutgoingTypeModelFacetLinks_ControlFlow_4001Gen(source);
 		}
 		Collection<UMLLinkDescriptor> result = UMLDiagramUpdater.getOutgoingTypeModelFacetLinks_ControlFlow_4001Gen(source);
 		for (ActivityEdge linkObject : container.getEdges()) {
 			if (false == linkObject instanceof ControlFlow) {
 				continue;
 			}
 			ControlFlow link = (ControlFlow) linkObject;
 			if (ControlFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode dst = link.getTarget();
 			ActivityNode src = link.getSource();
 			if (src != source) {
 				continue;
 			}
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.ControlFlow_4001, ControlFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_ObjectFlow_4002Gen(ActivityNode source) {
 		Activity container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof Activity) {
 				container = (Activity) element;
 			}
 		}
 		if (container == null) {
 			return Collections.emptyList();
 		}
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getEdges().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof ObjectFlow) {
 				continue;
 			}
 			ObjectFlow link = (ObjectFlow) linkObject;
 			if (ObjectFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode dst = link.getTarget();
 			ActivityNode src = link.getSource();
 			if (src != source) {
 				continue;
 			}
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.ObjectFlow_4002, ObjectFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_ObjectFlow_4002(ActivityNode source) {
 		StructuredActivityNode container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof StructuredActivityNode) {
 				container = (StructuredActivityNode) element;
 			}
 		}
 		if (container == null) {
 			return UMLDiagramUpdater.getOutgoingTypeModelFacetLinks_ObjectFlow_4002Gen(container);
 		}
 		Collection<UMLLinkDescriptor> result = UMLDiagramUpdater.getOutgoingTypeModelFacetLinks_ObjectFlow_4002Gen(container);
 		for (ActivityEdge linkObject : container.getEdges()) {
 			if (false == linkObject instanceof ObjectFlow) {
 				continue;
 			}
 			ObjectFlow link = (ObjectFlow) linkObject;
 			if (ObjectFlowEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ActivityNode dst = link.getTarget();
 			ActivityNode src = link.getSource();
 			if (src != source) {
 				continue;
 			}
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.ObjectFlow_4002, ObjectFlowEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Action_LocalPrecondition_4003(Action source) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		for (Iterator<?> destinations = source.getLocalPreconditions().iterator(); destinations.hasNext();) {
 			Constraint destination = (Constraint) destinations.next();
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(source, destination, UMLElementTypes.ActionLocalPrecondition_4003, ActionLocalPreconditionEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Action_LocalPostcondition_4006(Action source) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		for (Iterator<?> destinations = source.getLocalPostconditions().iterator(); destinations.hasNext();) {
 			Constraint destination = (Constraint) destinations.next();
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(source, destination, UMLElementTypes.ActionLocalPostcondition_4006, ActionLocalPostconditionEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_ObjectNode_Selection_4004(ObjectNode source) {
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		Behavior destination = source.getSelection();
 		if (destination == null) {
 			return result;
 		}
 		result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(source, destination, UMLElementTypes.ObjectNodeSelection_4004, ObjectNodeSelectionEditPart.VISUAL_ID));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_ExceptionHandler_4005(ExecutableNode source) {
 		ExecutableNode container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof ExecutableNode) {
 				container = (ExecutableNode) element;
 			}
 		}
 		if (container == null) {
 			return Collections.emptyList();
 		}
 		LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getHandlers().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof ExceptionHandler) {
 				continue;
 			}
 			ExceptionHandler link = (ExceptionHandler) linkObject;
 			if (ExceptionHandlerEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ExecutableNode dst = link.getHandlerBody();
 			ExecutableNode src = link.getProtectedNode();
 			if (src != source) {
 				continue;
 			}
 			result.add(new org.eclipse.uml2.diagram.activity.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.ExceptionHandler_4005, ExceptionHandlerEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static final IDiagramUpdater TYPED_ADAPTER = new IDiagramUpdater() {
 
 		/**
 		 * @generated
 		 */
 		public List<UMLNodeDescriptor> getSemanticChildren(View view) {
 			return org.eclipse.uml2.diagram.activity.part.UMLDiagramUpdater.getSemanticChildren(view);
 		}
 
 		/**
 		 * @generated
 		 */
 		public List<UMLLinkDescriptor> getContainedLinks(View view) {
 			return org.eclipse.uml2.diagram.activity.part.UMLDiagramUpdater.getContainedLinks(view);
 		}
 
 		/**
 		 * @generated
 		 */
 		public List<UMLLinkDescriptor> getIncomingLinks(View view) {
 			return org.eclipse.uml2.diagram.activity.part.UMLDiagramUpdater.getIncomingLinks(view);
 		}
 
 		/**
 		 * @generated
 		 */
 		public List<UMLLinkDescriptor> getOutgoingLinks(View view) {
 			return org.eclipse.uml2.diagram.activity.part.UMLDiagramUpdater.getOutgoingLinks(view);
 		}
 	};
 
 }
