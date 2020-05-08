 package org.eclipse.uml2.diagram.component.part;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.component.conventions.ConnectorEndConvention;
 import org.eclipse.uml2.diagram.component.edit.parts.Artifact2EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.ArtifactEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.Class2EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.Class3EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.ClassEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.Component2EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.Component3EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.ComponentContents2EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.ComponentContentsEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.ComponentEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.ComponentRequiredEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.ConnectorEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.ElementImportEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.Interface2EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.InterfaceEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.InterfaceRealizationEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.Package2EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.Package3EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.Package4EditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.PackageClassifiersEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.PackageEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.PackageImportsEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.PackagePackagesEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.PortEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.PortProvidedEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.PortRequiredEditPart;
 import org.eclipse.uml2.diagram.component.edit.parts.PropertyEditPart;
 import org.eclipse.uml2.diagram.component.providers.UMLElementTypes;
 import org.eclipse.uml2.uml.BehavioredClassifier;
 import org.eclipse.uml2.uml.Class;
 import org.eclipse.uml2.uml.Component;
 import org.eclipse.uml2.uml.ConnectableElement;
 import org.eclipse.uml2.uml.Connector;
 import org.eclipse.uml2.uml.ConnectorEnd;
 import org.eclipse.uml2.uml.ElementImport;
 import org.eclipse.uml2.uml.Interface;
 import org.eclipse.uml2.uml.InterfaceRealization;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.PackageableElement;
 import org.eclipse.uml2.uml.Port;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.StructuredClassifier;
 import org.eclipse.uml2.uml.Type;
 import org.eclipse.uml2.uml.UMLPackage;
 
 /**
  * @generated
  */
 public class UMLDiagramUpdater {
 
 	/**
 	 * @generated
 	 */
 	public static boolean isShortcutOrphaned(View view) {
 		return !view.isSetElement() || view.getElement() == null || view.getElement().eIsProxy();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getSemanticChildren(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case ComponentEditPart.VISUAL_ID:
 			return getComponent_2001SemanticChildren(view);
 		case Class2EditPart.VISUAL_ID:
 			return getClass_2004SemanticChildren(view);
 		case Component2EditPart.VISUAL_ID:
 			return getComponent_3001SemanticChildren(view);
 		case ClassEditPart.VISUAL_ID:
 			return getClass_3004SemanticChildren(view);
 		case ComponentContentsEditPart.VISUAL_ID:
 			return getComponentContents_7001SemanticChildren(view);
 		case ComponentContents2EditPart.VISUAL_ID:
 			return getComponentContents_7002SemanticChildren(view);
 		case PackageImportsEditPart.VISUAL_ID:
 			return getPackageImports_7003SemanticChildren(view);
 		case PackagePackagesEditPart.VISUAL_ID:
 			return getPackagePackages_7004SemanticChildren(view);
 		case PackageClassifiersEditPart.VISUAL_ID:
 			return getPackageClassifiers_7005SemanticChildren(view);
 		case PackageEditPart.VISUAL_ID: {
 			//We have "dummy" TopLevelNode (with vid = org.eclipse.uml2.diagram.component.edit.parts.Package2EditPart.VISUAL_ID). 
 			//The only purpose for this node is to be a container for children (imports, etc)
 			//of the "main" diagram figure (that one shown as Canvas).
 			//Also we have modified the VisualIDRegistry#getNodeVisualID() to return
 			//VID = org.eclipse.uml2.diagram.component.edit.parts.Package2EditPart.VISUAL_ID, 
 			//for the case when top-level view is created for the same semantic element as the canvas view.
 
 			List resultAndHeader = new LinkedList();
 			resultAndHeader.add(new UMLNodeDescriptor(view.getElement(), Package2EditPart.VISUAL_ID));
 			resultAndHeader.addAll(getPackage_1000SemanticChildren(view));
 			return resultAndHeader;
 		}
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_2001SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Component modelElement = (Component) view.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PortEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_2004SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Class modelElement = (Class) view.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PortEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_3001SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Component modelElement = (Component) view.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PortEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_3004SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Class modelElement = (Class) view.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PortEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponentContents_7001SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.EMPTY_LIST;
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Component modelElement = (Component) containerView.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
 			PackageableElement childElement = (PackageableElement) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Component2EditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ArtifactEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ClassEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == InterfaceEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PropertyEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponentContents_7002SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.EMPTY_LIST;
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Component modelElement = (Component) containerView.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
 			PackageableElement childElement = (PackageableElement) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Component2EditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ArtifactEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == ClassEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == InterfaceEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PropertyEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackageImports_7003SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.EMPTY_LIST;
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Package modelElement = (Package) containerView.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getElementImports().iterator(); it.hasNext();) {
 			ElementImport childElement = (ElementImport) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ElementImportEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackagePackages_7004SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.EMPTY_LIST;
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Package modelElement = (Package) containerView.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getNestedPackages().iterator(); it.hasNext();) {
 			Package childElement = (Package) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Package4EditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackageClassifiers_7005SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.EMPTY_LIST;
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Package modelElement = (Package) containerView.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getOwnedTypes().iterator(); it.hasNext();) {
 			Type childElement = (Type) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Class3EditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Component3EditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_1000SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.EMPTY_LIST;
 		}
 		Package modelElement = (Package) view.getElement();
 		List result = new LinkedList();
 		for (Iterator it = modelElement.getOwnedTypes().iterator(); it.hasNext();) {
 			Type childElement = (Type) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ComponentEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Artifact2EditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Interface2EditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Class2EditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
 			PackageableElement childElement = (PackageableElement) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 		}
 		for (Iterator it = modelElement.getNestedPackages().iterator(); it.hasNext();) {
 			Package childElement = (Package) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Package3EditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getContainedLinks(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case PackageEditPart.VISUAL_ID:
 			return getPackage_1000ContainedLinks(view);
 		case ComponentEditPart.VISUAL_ID:
 			return getComponent_2001ContainedLinks(view);
 		case Artifact2EditPart.VISUAL_ID:
 			return getArtifact_2002ContainedLinks(view);
 		case Interface2EditPart.VISUAL_ID:
 			return getInterface_2003ContainedLinks(view);
 		case Class2EditPart.VISUAL_ID:
 			return getClass_2004ContainedLinks(view);
 		case Package2EditPart.VISUAL_ID:
 			return getPackage_2005ContainedLinks(view);
 		case Package3EditPart.VISUAL_ID:
 			return getPackage_2006ContainedLinks(view);
 		case Component2EditPart.VISUAL_ID:
 			return getComponent_3001ContainedLinks(view);
 		case PortEditPart.VISUAL_ID:
 			return getPort_3002ContainedLinks(view);
 		case ArtifactEditPart.VISUAL_ID:
 			return getArtifact_3003ContainedLinks(view);
 		case ClassEditPart.VISUAL_ID:
 			return getClass_3004ContainedLinks(view);
 		case InterfaceEditPart.VISUAL_ID:
 			return getInterface_3005ContainedLinks(view);
 		case PropertyEditPart.VISUAL_ID:
 			return getProperty_3006ContainedLinks(view);
 		case ElementImportEditPart.VISUAL_ID:
 			return getElementImport_3007ContainedLinks(view);
 		case Package4EditPart.VISUAL_ID:
 			return getPackage_3008ContainedLinks(view);
 		case Class3EditPart.VISUAL_ID:
 			return getClass_3009ContainedLinks(view);
 		case Component3EditPart.VISUAL_ID:
 			return getComponent_3010ContainedLinks(view);
 		case InterfaceRealizationEditPart.VISUAL_ID:
 			return getInterfaceRealization_4001ContainedLinks(view);
 		case ConnectorEditPart.VISUAL_ID:
 			return getConnector_4008ContainedLinks(view);
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getIncomingLinks(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case ComponentEditPart.VISUAL_ID:
 			return getComponent_2001IncomingLinks(view);
 		case Artifact2EditPart.VISUAL_ID:
 			return getArtifact_2002IncomingLinks(view);
 		case Interface2EditPart.VISUAL_ID:
 			return getInterface_2003IncomingLinks(view);
 		case Class2EditPart.VISUAL_ID:
 			return getClass_2004IncomingLinks(view);
 		case Package2EditPart.VISUAL_ID:
 			return getPackage_2005IncomingLinks(view);
 		case Package3EditPart.VISUAL_ID:
 			return getPackage_2006IncomingLinks(view);
 		case Component2EditPart.VISUAL_ID:
 			return getComponent_3001IncomingLinks(view);
 		case PortEditPart.VISUAL_ID:
 			return getPort_3002IncomingLinks(view);
 		case ArtifactEditPart.VISUAL_ID:
 			return getArtifact_3003IncomingLinks(view);
 		case ClassEditPart.VISUAL_ID:
 			return getClass_3004IncomingLinks(view);
 		case InterfaceEditPart.VISUAL_ID:
 			return getInterface_3005IncomingLinks(view);
 		case PropertyEditPart.VISUAL_ID:
 			return getProperty_3006IncomingLinks(view);
 		case ElementImportEditPart.VISUAL_ID:
 			return getElementImport_3007IncomingLinks(view);
 		case Package4EditPart.VISUAL_ID:
 			return getPackage_3008IncomingLinks(view);
 		case Class3EditPart.VISUAL_ID:
 			return getClass_3009IncomingLinks(view);
 		case Component3EditPart.VISUAL_ID:
 			return getComponent_3010IncomingLinks(view);
 		case InterfaceRealizationEditPart.VISUAL_ID:
 			return getInterfaceRealization_4001IncomingLinks(view);
 		case ConnectorEditPart.VISUAL_ID:
 			return getConnector_4008IncomingLinks(view);
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getOutgoingLinks(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case ComponentEditPart.VISUAL_ID:
 			return getComponent_2001OutgoingLinks(view);
 		case Artifact2EditPart.VISUAL_ID:
 			return getArtifact_2002OutgoingLinks(view);
 		case Interface2EditPart.VISUAL_ID:
 			return getInterface_2003OutgoingLinks(view);
 		case Class2EditPart.VISUAL_ID:
 			return getClass_2004OutgoingLinks(view);
 		case Package2EditPart.VISUAL_ID:
 			return getPackage_2005OutgoingLinks(view);
 		case Package3EditPart.VISUAL_ID:
 			return getPackage_2006OutgoingLinks(view);
 		case Component2EditPart.VISUAL_ID:
 			return getComponent_3001OutgoingLinks(view);
 		case PortEditPart.VISUAL_ID:
 			return getPort_3002OutgoingLinks(view);
 		case ArtifactEditPart.VISUAL_ID:
 			return getArtifact_3003OutgoingLinks(view);
 		case ClassEditPart.VISUAL_ID:
 			return getClass_3004OutgoingLinks(view);
 		case InterfaceEditPart.VISUAL_ID:
 			return getInterface_3005OutgoingLinks(view);
 		case PropertyEditPart.VISUAL_ID:
 			return getProperty_3006OutgoingLinks(view);
 		case ElementImportEditPart.VISUAL_ID:
 			return getElementImport_3007OutgoingLinks(view);
 		case Package4EditPart.VISUAL_ID:
 			return getPackage_3008OutgoingLinks(view);
 		case Class3EditPart.VISUAL_ID:
 			return getClass_3009OutgoingLinks(view);
 		case Component3EditPart.VISUAL_ID:
 			return getComponent_3010OutgoingLinks(view);
 		case InterfaceRealizationEditPart.VISUAL_ID:
 			return getInterfaceRealization_4001OutgoingLinks(view);
 		case ConnectorEditPart.VISUAL_ID:
 			return getConnector_4008OutgoingLinks(view);
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_1000ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_2001ContainedLinks(View view) {
 		Component modelElement = (Component) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Component_Required_4007(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Connector_4008(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getArtifact_2002ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getInterface_2003ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_2004ContainedLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Connector_4008(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_2005ContainedLinks(View view) {
 		//no links to, from and inside the diagram header
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_2006ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_3001ContainedLinks(View view) {
 		Component modelElement = (Component) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Component_Required_4007(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Connector_4008(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPort_3002ContainedLinks(View view) {
 		Port modelElement = (Port) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Port_Provided_4006(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Port_Required_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getArtifact_3003ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_3004ContainedLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Connector_4008(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getInterface_3005ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getProperty_3006ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getElementImport_3007ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_3008ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_3009ContainedLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Connector_4008(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_3010ContainedLinks(View view) {
 		Component modelElement = (Component) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Component_Required_4007(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Connector_4008(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getInterfaceRealization_4001ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getConnector_4008ContainedLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_2001IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getArtifact_2002IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getInterface_2003IncomingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		Map crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		List result = new LinkedList();
 		result.addAll(getIncomingTypeModelFacetLinks_InterfaceRealization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Provided_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Required_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Component_Required_4007(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_2004IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_2005IncomingLinks(View view) {
 		//no links to, from and inside the diagram header
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_2006IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_3001IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPort_3002IncomingLinks(View view) {
 		Port modelElement = (Port) view.getElement();
 		Map crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		List result = new LinkedList();
 		result.addAll(getIncomingTypeModelFacetLinks_Connector_4008(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getArtifact_3003IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_3004IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getInterface_3005IncomingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		Map crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		List result = new LinkedList();
 		result.addAll(getIncomingTypeModelFacetLinks_InterfaceRealization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Provided_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Required_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Component_Required_4007(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getProperty_3006IncomingLinks(View view) {
 		Property modelElement = (Property) view.getElement();
 		Map crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		List result = new LinkedList();
 		result.addAll(getIncomingTypeModelFacetLinks_Connector_4008(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getElementImport_3007IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_3008IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_3009IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_3010IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getInterfaceRealization_4001IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getConnector_4008IncomingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_2001OutgoingLinks(View view) {
 		Component modelElement = (Component) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Component_Required_4007(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getArtifact_2002OutgoingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getInterface_2003OutgoingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_2004OutgoingLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_2005OutgoingLinks(View view) {
 		//no links to, from and inside the diagram header
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_2006OutgoingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_3001OutgoingLinks(View view) {
 		Component modelElement = (Component) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Component_Required_4007(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPort_3002OutgoingLinks(View view) {
 		Port modelElement = (Port) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Port_Provided_4006(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Port_Required_4004(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Connector_4008(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getArtifact_3003OutgoingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_3004OutgoingLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getInterface_3005OutgoingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getProperty_3006OutgoingLinks(View view) {
 		Property modelElement = (Property) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getOutgoingTypeModelFacetLinks_Connector_4008(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getElementImport_3007OutgoingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getPackage_3008OutgoingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getClass_3009OutgoingLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getComponent_3010OutgoingLinks(View view) {
 		Component modelElement = (Component) view.getElement();
 		List result = new LinkedList();
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4001(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Component_Required_4007(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getInterfaceRealization_4001OutgoingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List getConnector_4008OutgoingLinks(View view) {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection getContainedTypeModelFacetLinks_InterfaceRealization_4001(BehavioredClassifier container) {
 		Collection result = new LinkedList();
 		for (Iterator links = container.getInterfaceRealizations().iterator(); links.hasNext();) {
 			Object linkObject = links.next();
 			if (false == linkObject instanceof InterfaceRealization) {
 				continue;
 			}
 			InterfaceRealization link = (InterfaceRealization) linkObject;
 			if (InterfaceRealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			Interface dst = link.getContract();
 			BehavioredClassifier src = link.getImplementingClassifier();
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.InterfaceRealization_4001, InterfaceRealizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection getContainedTypeModelFacetLinks_Connector_4008(StructuredClassifier container) {
 		Collection result = new LinkedList();
		StructuredClassifier sc = (StructuredClassifier) container;
		for (Iterator links = sc.getOwnedConnectors().iterator(); links.hasNext();) {
 			Object linkObject = links.next();
 			if (false == linkObject instanceof Connector) {
 				continue;
 			}
 			Connector link = (Connector) linkObject;
 			if (ConnectorEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			ConnectorEnd sourceEnd = ConnectorEndConvention.getSourceEnd(link);
 			ConnectorEnd targetEnd = ConnectorEndConvention.getTargetEnd(link);
 			if (sourceEnd == null || targetEnd == null) {
 				continue;
 			}
 
 			ConnectableElement dst = targetEnd.getRole();
 			ConnectableElement src = sourceEnd.getRole();
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Connector_4008, ConnectorEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection getIncomingTypeModelFacetLinks_InterfaceRealization_4001(Interface target, Map crossReferences) {
 		Collection result = new LinkedList();
 		Collection settings = (Collection) crossReferences.get(target);
 		for (Iterator it = settings.iterator(); it.hasNext();) {
 			EStructuralFeature.Setting setting = (EStructuralFeature.Setting) it.next();
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getInterfaceRealization_Contract() || false == setting.getEObject() instanceof InterfaceRealization) {
 				continue;
 			}
 			InterfaceRealization link = (InterfaceRealization) setting.getEObject();
 			if (InterfaceRealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			BehavioredClassifier src = link.getImplementingClassifier();
 			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.InterfaceRealization_4001, InterfaceRealizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection getIncomingFeatureModelFacetLinks_Port_Provided_4006(Interface target, Map crossReferences) {
 		Collection result = new LinkedList();
 		Collection settings = (Collection) crossReferences.get(target);
 		for (Iterator it = settings.iterator(); it.hasNext();) {
 			EStructuralFeature.Setting setting = (EStructuralFeature.Setting) it.next();
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getPort_Provided()) {
 				result.add(new UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.PortProvided_4006, PortProvidedEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection getIncomingFeatureModelFacetLinks_Port_Required_4004(Interface target, Map crossReferences) {
 		Collection result = new LinkedList();
 		Collection settings = (Collection) crossReferences.get(target);
 		for (Iterator it = settings.iterator(); it.hasNext();) {
 			EStructuralFeature.Setting setting = (EStructuralFeature.Setting) it.next();
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getPort_Required()) {
 				result.add(new UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.PortRequired_4004, PortRequiredEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection getIncomingFeatureModelFacetLinks_Component_Required_4007(Interface target, Map crossReferences) {
 		Collection result = new LinkedList();
 		Collection settings = (Collection) crossReferences.get(target);
 		for (Iterator it = settings.iterator(); it.hasNext();) {
 			EStructuralFeature.Setting setting = (EStructuralFeature.Setting) it.next();
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getComponent_Required()) {
 				result.add(new UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.ComponentRequired_4007, ComponentRequiredEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * XXX: this method is not called by GMF yet (as for 2.0 release). The
 	 * default generated version is not compiliable.
 	 * 
 	 * @generated NOT
 	 */
 	private static Collection getIncomingTypeModelFacetLinks_Connector_4008(ConnectableElement target, Map crossReferences) {
 		throw new UnsupportedOperationException("Not yet implemented");
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection getOutgoingTypeModelFacetLinks_InterfaceRealization_4001(BehavioredClassifier source) {
 		BehavioredClassifier container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof BehavioredClassifier) {
 				container = (BehavioredClassifier) element;
 			}
 		}
 		if (container == null) {
 			return Collections.EMPTY_LIST;
 		}
 		Collection result = new LinkedList();
 		for (Iterator links = container.getInterfaceRealizations().iterator(); links.hasNext();) {
 			Object linkObject = links.next();
 			if (false == linkObject instanceof InterfaceRealization) {
 				continue;
 			}
 			InterfaceRealization link = (InterfaceRealization) linkObject;
 			if (InterfaceRealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			Interface dst = link.getContract();
 			BehavioredClassifier src = link.getImplementingClassifier();
 			if (src != source) {
 				continue;
 			}
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.InterfaceRealization_4001, InterfaceRealizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection getOutgoingFeatureModelFacetLinks_Port_Provided_4006(Port source) {
 		Collection result = new LinkedList();
 		for (Iterator destinations = source.getProvideds().iterator(); destinations.hasNext();) {
 			Interface destination = (Interface) destinations.next();
 			result.add(new UMLLinkDescriptor(source, destination, UMLElementTypes.PortProvided_4006, PortProvidedEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection getOutgoingFeatureModelFacetLinks_Port_Required_4004(Port source) {
 		Collection result = new LinkedList();
 		for (Iterator destinations = source.getRequireds().iterator(); destinations.hasNext();) {
 			Interface destination = (Interface) destinations.next();
 			result.add(new UMLLinkDescriptor(source, destination, UMLElementTypes.PortRequired_4004, PortRequiredEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection getOutgoingFeatureModelFacetLinks_Component_Required_4007(Component source) {
 		Collection result = new LinkedList();
 		for (Iterator destinations = source.getRequireds().iterator(); destinations.hasNext();) {
 			Interface destination = (Interface) destinations.next();
 			result.add(new UMLLinkDescriptor(source, destination, UMLElementTypes.ComponentRequired_4007, ComponentRequiredEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * XXX: this method is not called by GMF yet (as for 2.0 release). The
 	 * default generated version is not compiliable.
 	 * 
 	 * @generated NOT
 	 */
 	private static Collection getOutgoingTypeModelFacetLinks_Connector_4008(ConnectableElement source) {
 		throw new UnsupportedOperationException("Not yet implemented");
 	}
 
 }
