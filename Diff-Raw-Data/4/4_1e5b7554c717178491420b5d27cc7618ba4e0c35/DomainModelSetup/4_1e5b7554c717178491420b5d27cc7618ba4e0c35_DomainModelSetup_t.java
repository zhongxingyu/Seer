 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.setup;
 
 import java.util.Calendar;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.ETypedElement;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 
 /**
  * TODO:
  * ADD: 
  *  - link label feature
  *  - more nodes
  *  - child nodes
  */
 public class DomainModelSetup implements DomainModelSource {
 	private EPackage myModelPackage;
 	private NodeData myNodeA;
 	private LinkData myLinkA2C;
 	private EReference myLinkAsRef;
 	private EClass myDiagramElement;
 	private NodeData myNodeB;
 	private NodeData myChild1OfA;
 	private NodeData myChildOfB;
 	private NodeData myChildOfChildOfB;
 	private NodeData myChild2OfA;
 	private NodeData myNodeD;
 	private LinkData myLinkA2C_Cardinalyty2;
 	private LinkData myLinkA2C_Cardinality1;
 	private EReference myLinkAsRef_Cardinality2;
 	private EReference myLinkAsRef_Cardinality1;
 	private LinkData mySelfLinkAsClass;
 	private EReference mySelfLinkAsRef;
 
 	public DomainModelSetup() {
 	}
 
 	/**
 	 * @return <code>this</code> for convenience
 	 */
 	public DomainModelSetup init() {
 		EPackage p = EcoreFactory.eINSTANCE.createEPackage();
 		p.setName("samplemodel");
 		p.setNsPrefix("gmftest");
 		Calendar c = Calendar.getInstance();
 		p.setNsURI("uri://eclipse/gmf/tests/sample/" + c.get(Calendar.HOUR_OF_DAY) + '/' + c.get(Calendar.MINUTE) + '/');
 
 		EClass superNode = EcoreFactory.eINSTANCE.createEClass();
 		superNode.setName("CommonBaseClass");
 		superNode.setAbstract(true);
 
 		EClass containmentNode = EcoreFactory.eINSTANCE.createEClass();
 		containmentNode.setName("UltimateContainer");
 		EReference r0 = EcoreFactory.eINSTANCE.createEReference();
 		r0.setContainment(true);
 		r0.setName("all");
 		r0.setEType(superNode);
 		r0.setUpperBound(-1);
 		containmentNode.getEStructuralFeatures().add(r0);
 		final EAttribute da1 = EcoreFactory.eINSTANCE.createEAttribute();
 		da1.setName("diagramAttribute");
 		da1.setEType(EcorePackage.eINSTANCE.getEString());
 		containmentNode.getEStructuralFeatures().add(da1);
 
 		EClass nodeA = EcoreFactory.eINSTANCE.createEClass();
 		nodeA.setName("NodeSrcA");
 		nodeA.getESuperTypes().add(superNode);
 		EClass nodeB = EcoreFactory.eINSTANCE.createEClass();
 		nodeB.setName("NodeTargetB");
 		nodeB.getESuperTypes().add(superNode);
 		EClass nodeC = EcoreFactory.eINSTANCE.createEClass();
 		nodeC.setName("NodeTargetC");
 		EClass nodeD = EcoreFactory.eINSTANCE.createEClass();
 		nodeD.setName("NodeTargetD");
 		EClass nodeLinkA2C = EcoreFactory.eINSTANCE.createEClass();
 		nodeLinkA2C.setName("LinkAtoC");
 		EClass nodeLinkA2C2 = EcoreFactory.eINSTANCE.createEClass();
 		nodeLinkA2C2.setName("LinkAtoC_Cardinality2");
 		EClass nodeLinkA2C3 = EcoreFactory.eINSTANCE.createEClass();
 		nodeLinkA2C3.setName("LinkAtoC_Cardinality1");
 		EClass nodeLinkA2A = EcoreFactory.eINSTANCE.createEClass();
 		nodeLinkA2A.setName("LinkAtoA");
 		EClass childNode = EcoreFactory.eINSTANCE.createEClass();
 		childNode.setName("Child");
 
 		final EAttribute a1 = EcoreFactory.eINSTANCE.createEAttribute();
 		a1.setName("label");
 		a1.setEType(EcorePackage.eINSTANCE.getEString());
 		nodeA.getEStructuralFeatures().add(a1);
 
 		final EAttribute a2 = EcoreFactory.eINSTANCE.createEAttribute();
 		a2.setName("title");
 		a2.setEType(EcorePackage.eINSTANCE.getEString());
 		nodeB.getEStructuralFeatures().add(a2);
 		nodeC.getESuperTypes().add(nodeB);
 		nodeD.getESuperTypes().add(nodeB);
 		
 		final EAttribute childLabel = EcoreFactory.eINSTANCE.createEAttribute();
 		childLabel.setName("childLabel");
 		childLabel.setEType(EcorePackage.eINSTANCE.getEString());
 		childNode.getEStructuralFeatures().add(childLabel);
 
 		EReference linkToB = EcoreFactory.eINSTANCE.createEReference();
 		linkToB.setName("refLinkToB");
 		linkToB.setEType(nodeB);
 		linkToB.setUpperBound(-1);
 		nodeA.getEStructuralFeatures().add(linkToB);
 		
 		EReference linkToB2 = EcoreFactory.eINSTANCE.createEReference();
 		linkToB2.setName("refLinkToB_Cardinality2");
 		linkToB2.setEType(nodeB);
 		linkToB2.setUpperBound(2);
 		nodeA.getEStructuralFeatures().add(linkToB2);
 		
 		EReference linkToB3 = EcoreFactory.eINSTANCE.createEReference();
 		linkToB3.setName("refLinkToB_Cardinality1");
 		linkToB3.setEType(nodeB);
 		linkToB3.setUpperBound(1);
 		nodeA.getEStructuralFeatures().add(linkToB3);
 		
 		EReference linkToARef = EcoreFactory.eINSTANCE.createEReference();
 		linkToARef.setName("refLinkToA");
 		linkToARef.setEType(nodeA);
 		linkToARef.setUpperBound(-1);
 		nodeA.getEStructuralFeatures().add(linkToARef);
 
 		EReference linkToC = EcoreFactory.eINSTANCE.createEReference();
 		linkToC.setName("classLinkToC");
 		linkToC.setEType(nodeLinkA2C);
 		linkToC.setUpperBound(-1);
 		linkToC.setContainment(true);
 		linkToC.setUnique(false);
 		nodeA.getEStructuralFeatures().add(linkToC);
 
 		EReference refCfromLink = EcoreFactory.eINSTANCE.createEReference();
 		refCfromLink.setName("trg");
 		refCfromLink.setEType(nodeC);
 		refCfromLink.setUpperBound(1);
 		refCfromLink.setUnique(false);
 		nodeLinkA2C.getEStructuralFeatures().add(refCfromLink);
 		
 		EReference linkToC2 = EcoreFactory.eINSTANCE.createEReference();
 		linkToC2.setName("classLinkToC_Cardinality2");
 		linkToC2.setEType(nodeLinkA2C2);
 		linkToC2.setUpperBound(2);
 		linkToC2.setContainment(true);
 		linkToC2.setUnique(true);
 		nodeA.getEStructuralFeatures().add(linkToC2);
 
 		EReference refCfromLink2 = EcoreFactory.eINSTANCE.createEReference();
 		refCfromLink2.setName("trg");
 		refCfromLink2.setEType(nodeC);
 		refCfromLink2.setUpperBound(-1);
 		refCfromLink2.setUnique(true);
 		nodeLinkA2C2.getEStructuralFeatures().add(refCfromLink2);
 		
 		EReference linkToC3 = EcoreFactory.eINSTANCE.createEReference();
 		linkToC3.setName("classLinkToC_Cardinality1");
 		linkToC3.setEType(nodeLinkA2C3);
 		linkToC3.setUpperBound(1);
 		linkToC3.setContainment(true);
 		linkToC3.setUnique(false);
 		nodeA.getEStructuralFeatures().add(linkToC3);
 
 		EReference refCfromLink3 = EcoreFactory.eINSTANCE.createEReference();
 		refCfromLink3.setName("trg");
 		refCfromLink3.setEType(nodeC);
 		refCfromLink3.setUpperBound(1);
 		refCfromLink3.setUnique(false);
 		nodeLinkA2C3.getEStructuralFeatures().add(refCfromLink3);
 		
 		EReference linkToAClass = EcoreFactory.eINSTANCE.createEReference();
 		linkToAClass.setName("classLinkToA");
 		linkToAClass.setEType(nodeLinkA2A);
 		linkToAClass.setUpperBound(-1);
 		linkToAClass.setContainment(true);
 		linkToAClass.setUnique(false);
 		nodeA.getEStructuralFeatures().add(linkToAClass);
 
 		EReference refAfromLink = EcoreFactory.eINSTANCE.createEReference();
 		refAfromLink.setName("trg");
 		refAfromLink.setEType(nodeA);
 		refAfromLink.setUpperBound(1);
 		refAfromLink.setUnique(false);
 		nodeLinkA2A.getEStructuralFeatures().add(refAfromLink);
 
 		EReference containment1ForA = EcoreFactory.eINSTANCE.createEReference();
 		containment1ForA.setContainment(true);
 		containment1ForA.setName("children1OfA");
 		containment1ForA.setEType(childNode);
 		containment1ForA.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
 		nodeA.getEStructuralFeatures().add(containment1ForA);
 		
 		EReference containment2ForA = EcoreFactory.eINSTANCE.createEReference();
 		containment2ForA.setContainment(true);
 		containment2ForA.setName("children2OfA");
 		containment2ForA.setEType(childNode);
 		containment2ForA.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
 		nodeA.getEStructuralFeatures().add(containment2ForA);
 		
 		EReference containmentForB = EcoreFactory.eINSTANCE.createEReference();
 		containmentForB.setContainment(true);
 		containmentForB.setName("childrenOfB");
 		containmentForB.setEType(childNode);
 		containmentForB.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
 		nodeB.getEStructuralFeatures().add(containmentForB);
 		
 		EReference selfContainment = EcoreFactory.eINSTANCE.createEReference();
 		selfContainment.setContainment(true);
 		selfContainment.setName("innerChildrenOfBChild");
 		selfContainment.setEType(childNode);
 		selfContainment.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
 		childNode.getEStructuralFeatures().add(selfContainment);		
 
 		p.getEClassifiers().add(superNode);
 		p.getEClassifiers().add(containmentNode);
 		p.getEClassifiers().add(nodeA);
 		p.getEClassifiers().add(nodeB);
 		p.getEClassifiers().add(nodeC);
 		p.getEClassifiers().add(nodeD);
 		p.getEClassifiers().add(nodeLinkA2C);
 		p.getEClassifiers().add(nodeLinkA2C2);
 		p.getEClassifiers().add(nodeLinkA2C3);
 		p.getEClassifiers().add(nodeLinkA2A);
 		p.getEClassifiers().add(childNode);
 
 		confineInResource(p);
 
 		myModelPackage = p;
 		myNodeA = new NodeData(nodeA, a1, r0);
 		myChild1OfA = new NodeData(childNode, childLabel, containment1ForA);
 		myChild2OfA = new NodeData(childNode, childLabel, containment2ForA);
 		myLinkA2C = new LinkData(nodeLinkA2C, refCfromLink, linkToC);
 		myLinkA2C_Cardinalyty2 = new LinkData(nodeLinkA2C2, refCfromLink2, linkToC2);
 		myLinkA2C_Cardinality1 = new LinkData(nodeLinkA2C3, refCfromLink3, linkToC3);
 		mySelfLinkAsClass = new LinkData(nodeLinkA2A, refAfromLink, linkToAClass);
 		myNodeB = new NodeData(nodeC, a2, r0);
 		myNodeD = new NodeData(nodeD, a2, r0);
 		myChildOfB = new NodeData(childNode, childLabel, containmentForB);
 		myChildOfChildOfB = new NodeData(childNode, childLabel, selfContainment);
 		myLinkAsRef = linkToB;
 		myLinkAsRef_Cardinality2 = linkToB2;
 		myLinkAsRef_Cardinality1 = linkToB3;
 		mySelfLinkAsRef = linkToARef;
 		myDiagramElement = containmentNode;
 		return this;
 	}
 
	private void confineInResource(EObject p) {
 		new ResourceImpl(URI.createURI("uri://org.eclipse.gmf/tests/DomainModelSetup")).getContents().add(p);
 	}
 
 	public final EPackage getModel() {
 		return myModelPackage;
 	}
 
 	public final NodeData getNodeA() {
 		return myNodeA;
 	}
 
 	public final NodeData getChildOfA() {
 		return myChild1OfA;
 	}
 	
 	public final NodeData getSecondChildOfA() {
 		return myChild2OfA;
 	}
 	
 	public NodeData getNodeB() {
 		return myNodeB;
 	}
 
 	public NodeData getNodeD() {
 		return myNodeD;
 	}
 	
 	public final NodeData getChildOfB() {
 		return myChildOfB;
 	}
 
 	/*
 	 * This is a recursive child node (able to contains itself)
 	 */
 	public final NodeData getChildOfChildOfB() {
 		return myChildOfChildOfB;
 	}
 	
 	public final LinkData getLinkAsClass() {
 		return myLinkA2C;
 	}
 
 	public final LinkData getLinkAsClass_Cardinality2() {
 		return myLinkA2C_Cardinalyty2;
 	}
 	
 	public final LinkData getLinkAsClass_Cardinality1() {
 		return myLinkA2C_Cardinality1;
 	}
 	
 	public final LinkData getSelfLinkAsClass() {
 		return mySelfLinkAsClass;
 	}
 	
 	public final EReference getLinkAsRef() {
 		return myLinkAsRef;
 	}
 	
 	public final EReference getLinkAsRef_Cardinality2() {
 		return myLinkAsRef_Cardinality2;
 	}
 	
 	public final EReference getLinkAsRef_Cardinality1() {
 		return myLinkAsRef_Cardinality1;
 	}
 
 	public final EReference getSelfLinkAsRef() {
 		return mySelfLinkAsRef;
 	}
 
 	public EClass getDiagramElement() {
 		return myDiagramElement;
 	}
 }
