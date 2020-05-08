 /*
  * Copyright (c) 2008, 2010 Borland Software Corporation and others
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.rt;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.gmf.codegen.gmfgen.FigureViewmap;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.codegen.gmfgen.GenTopLevelNode;
 import org.eclipse.gmf.codegen.gmfgen.MetamodelType;
 import org.eclipse.gmf.codegen.gmfgen.TypeLinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.TypeModelFacet;
 import org.eclipse.gmf.internal.bridge.genmodel.GenModelMatcher;
 import org.eclipse.gmf.internal.bridge.genmodel.RuntimeGenModelAccess;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.Edge;
 import org.eclipse.gmf.runtime.notation.Node;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 import org.eclipse.gmf.tests.Utils;
 import org.eclipse.gmf.tests.gef.AbstractDiagramEditorTest;
 import org.eclipse.gmf.tests.setup.DiaGenSource;
 import org.eclipse.gmf.tests.setup.GeneratedDiagramPlugin;
 import org.eclipse.gmf.tests.setup.RuntimeBasedGeneratorConfiguration;
 import org.eclipse.gmf.tests.setup.SessionSetup;
 
 /**
  * @see https://bugs.eclipse.org/226882
  * @author artem
  */
 public class LinkChildMetaFeatureNotFromContainerTest extends AbstractDiagramEditorTest {
	protected static final class DiaGenSourceImpl implements DiaGenSource {
 
 		private final GenDiagram myDiagram;
 
 		private final GenTopLevelNode myNodeA;
 
 		private final GenTopLevelNode myNodeB;
 
 		private final GenLink myLinkL;
 		/*
 		 * C holds A, B and L, where L is a link between A and B, 
 		 * such as A has outgoing EReference to L, B knows nothing
 		 * about L, while L does (it got an EReference to B)  
 		 */
		public DiaGenSourceImpl() {
 			EcoreFactory f = EcoreFactory.eINSTANCE;
 			EClass a = f.createEClass();
 			EClass b = f.createEClass();
 			EClass c = f.createEClass();
 			EClass l = f.createEClass();
 			a.setName("ElemA");
 			b.setName("ElemB");
 			l.setName("BindAtoB");
 			c.setName("Cont");
 			l.getESuperTypes().add(a);
 
 			EReference rc1 = f.createEReference();
 			rc1.setName("containmA");
 			rc1.setUpperBound(-1);
 			rc1.setEType(a);
 			rc1.setContainment(true);
 			EReference rc2 = f.createEReference();
 			rc2.setName("containmB");
 			rc2.setUpperBound(-1);
 			rc2.setEType(b);
 			rc2.setContainment(true);
 			c.getEStructuralFeatures().add(rc1);
 			c.getEStructuralFeatures().add(rc2);
 
 			EReference rl = f.createEReference();
 			rl.setName("ls"); // link source
 			rl.setEType(l);
 			a.getEStructuralFeatures().add(rl);
 
 			EReference rb = f.createEReference();
 			rb.setName("lt"); // link target
 			rb.setEType(b);
 			l.getEStructuralFeatures().add(rb);
 			
 			EPackage p = f.createEPackage();
 			p.setName("xxx");
 			p.setNsPrefix("xxx");
 			p.setNsURI("uri:/CreateLinkTest/");
 			p.getEClassifiers().add(a);
 			p.getEClassifiers().add(b);
 			p.getEClassifiers().add(c);
 			p.getEClassifiers().add(l);
 
 			final GenModelMatcher gmm = new GenModelMatcher(Utils.createGenModel(p));
 			RuntimeGenModelAccess runtimeAccess = new RuntimeGenModelAccess();
 			runtimeAccess.ensure();
 			final GenModel runtimeModel = runtimeAccess.model();
 			GMFGenFactory gf = GMFGenFactory.eINSTANCE;
 			myDiagram = gf.createGenDiagram();
 			myDiagram.setDomainDiagramElement(gmm.findGenClass(c));
 			myDiagram.setDiagramRunTimeClass(Utils.findGenClass(runtimeModel, NotationPackage.eINSTANCE.getDiagram()));
 			myDiagram.setViewmap(gf.createFigureViewmap());
 			myDiagram.setVisualID(99);
 			MetamodelType dgmmType = gf.createMetamodelType();
 			myDiagram.setElementType(dgmmType);
 
 			myNodeA = gf.createGenTopLevelNode();
 			myNodeA.setDiagramRunTimeClass(Utils.findGenClass(runtimeModel, NotationPackage.eINSTANCE.getNode()));
 			myNodeA.setElementType(gf.createMetamodelType());
 			TypeModelFacet mf = gf.createTypeModelFacet();
 			mf.setMetaClass(gmm.findGenClass(a));
 			mf.setContainmentMetaFeature(gmm.findGenFeature(rc1));
 			myNodeA.setModelFacet(mf);
 			FigureViewmap fv = gf.createFigureViewmap();
 			fv.setFigureQualifiedClassName("org.eclipse.draw2d.RoundedRectangle");
 			myNodeA.setViewmap(fv);
 			myNodeA.setVisualID(1001);
 
 			myNodeB = gf.createGenTopLevelNode();
 			myNodeB.setDiagramRunTimeClass(myNodeA.getDiagramRunTimeClass());
 			myNodeB.setElementType(gf.createMetamodelType());
 			mf = gf.createTypeModelFacet();
 			mf.setMetaClass(gmm.findGenClass(b));
 			mf.setContainmentMetaFeature(gmm.findGenFeature(rc2));
 			myNodeB.setModelFacet(mf);
 			fv = gf.createFigureViewmap();
 			fv.setFigureQualifiedClassName("org.eclipse.draw2d.RoundedRectangle");
 			myNodeB.setViewmap(fv);
 			myNodeB.setVisualID(1002);
 
 			myLinkL = gf.createGenLink();
 			myLinkL.setDiagramRunTimeClass(Utils.findGenClass(runtimeModel, NotationPackage.eINSTANCE.getEdge()));
 			TypeLinkModelFacet lmf = gf.createTypeLinkModelFacet();
 			lmf.setMetaClass(gmm.findGenClass(l));
 			lmf.setContainmentMetaFeature(gmm.findGenFeature(rc1));
 			// >>>
 			lmf.setChildMetaFeature(gmm.findGenFeature(rl)); // HERE COMES INTERESTING PART
 			// <<<
 			lmf.setTargetMetaFeature(gmm.findGenFeature(rb));
 			myLinkL.setModelFacet(lmf);
 			fv = gf.createFigureViewmap();
 			fv.setFigureQualifiedClassName("org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx");
 			myLinkL.setViewmap(fv);
 			myLinkL.setVisualID(2001);
 			myLinkL.setElementType(gf.createMetamodelType());
 			
 			myDiagram.getTopLevelNodes().add(myNodeA);
 			myDiagram.getTopLevelNodes().add(myNodeB);
 			myDiagram.getLinks().add(myLinkL);
 			GenEditorGenerator geg = gf.createGenEditorGenerator();
 			geg.setDiagram(myDiagram);
 			geg.setDomainGenModel(myDiagram.getDomainDiagramElement().getGenModel());
 			geg.setEditor(gf.createGenEditorView());
 			geg.setPlugin(gf.createGenPlugin());
 			geg.setDiagramUpdater(gf.createGenDiagramUpdater());
 			geg.setLabelParsers(gf.createGenParsers());
 			geg.getLabelParsers().setExtensibleViaService(true);
 
 			new ResourceImpl(URI.createURI("uri://org.eclipse.gmf/tests/CreateLinkTest")).getContents().add(geg);
 		}
 
 		public GenDiagram getGenDiagram() {
 			return myDiagram;
 		}
 
 		public GenNode getNodeA() {
 			return myNodeA;
 		}
 
 		public GenNode getNodeB() {
 			return myNodeB;
 		}
 
 		public GenLink getLinkC() {
 			return myLinkL;
 		}
 
 		public GenLink getLinkD() {
 			return null;
 		}
 	}
 
 	public static final class CustomSetup extends SessionSetup {
 		public CustomSetup() {
 			super(new RuntimeBasedGeneratorConfiguration());
 		}
 
 		@Override
 		protected DiaGenSource createGenModel() {
 			return new DiaGenSourceImpl();
 		}
 	};
 
 	public LinkChildMetaFeatureNotFromContainerTest(String name) {
 		this(name, new RuntimeBasedGeneratorConfiguration());
 	}
 	
 	
 	public LinkChildMetaFeatureNotFromContainerTest(String name, RuntimeBasedGeneratorConfiguration genConfig) {
 		super(name, genConfig);
 	}
 	
 	public void testCreateLink() {
 		Node nodeA = createNode(getSetup().getGenModel().getNodeA(), getDiagram());
 		Node nodeB = createNode(getSetup().getGenModel().getNodeB(), getDiagram());
 		assertNotNull(nodeA); 
 		assertNotNull(nodeB);
 		Edge link = createLink(getSetup().getGenModel().getLinkC(), nodeA, nodeB);
 		assertNotNull(link);
 		EObject a = nodeA.getElement();
 		EObject b = nodeB.getElement();
 		EObject c = nodeA.getDiagram().getElement();
 		EObject l = link.getElement();
 		assertEquals(a.eContainer(), c);
 		assertEquals(b.eContainer(), c);
 		assertNotSame("Sanity", a.eContainmentFeature(), b.eContainmentFeature());
 		assertEquals(l.eContainer(), c);
 		// assert source feature (A to L) is set (A.ls == L),
 		// and target feature (L to B) is also set (L.lt == B);
 		// see session setup for more details
 		EStructuralFeature ls = a.eClass().getEStructuralFeature("ls");
 		assertNotNull("Sanity", ls); // just in case metamodel definition was modified
 		assertFalse("Sanity", ((EReference)ls).isContainment());
 		EStructuralFeature lt = l.eClass().getEStructuralFeature("lt");
 		assertNotNull("Sanity", lt);
 		assertFalse("Sanity", ((EReference)lt).isContainment());
 		//
 		assertEquals("Link target should be B, accessible from L.lt", l.eGet(lt), b);
 		assertEquals("Link should originate at A element, from non-containment featyre A.lt", a.eGet(ls), l);
 	}
 
 	@Override
 	protected Diagram createDiagramView(EObject domainElement, GeneratedDiagramPlugin genPlugin) {
 		return RuntimeBasedGeneratorConfiguration.createDiagram(domainElement, genPlugin);
 	}
 }
