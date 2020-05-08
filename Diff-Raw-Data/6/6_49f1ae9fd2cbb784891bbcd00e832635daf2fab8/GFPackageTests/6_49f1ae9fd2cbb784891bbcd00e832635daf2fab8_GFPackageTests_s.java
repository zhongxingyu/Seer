 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.bot.tests;
 
 import static org.easymock.EasyMock.createNiceMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable.syncExec;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.bot.tests.features.DefaultCopyFeature;
 import org.eclipse.graphiti.bot.tests.util.ITestConstants;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.ConfigurableFeatureProviderWrapper;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMappingProvider;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.ICopyContext;
 import org.eclipse.graphiti.features.context.IPasteContext;
 import org.eclipse.graphiti.features.context.IReconnectionContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.AreaAnchorContext;
 import org.eclipse.graphiti.features.context.impl.AreaContext;
 import org.eclipse.graphiti.features.context.impl.CopyContext;
 import org.eclipse.graphiti.features.context.impl.LayoutContext;
 import org.eclipse.graphiti.features.context.impl.PasteContext;
 import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.features.impl.DefaultMoveAnchorFeature;
 import org.eclipse.graphiti.features.impl.DefaultReconnectionFeature;
 import org.eclipse.graphiti.mm.Property;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.PictogramLink;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.pattern.mapping.data.ImageDataMapping;
 import org.eclipse.graphiti.platform.IPlatformImageConstants;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.testtool.ecore.TestDiagramTypeProvider;
 import org.eclipse.graphiti.testtool.sketch.SketchDiagramTypeProvider;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.features.AbstractPasteFeature;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.eclipse.graphiti.ui.internal.GraphitiUIPlugin;
 import org.eclipse.graphiti.ui.internal.editor.DiagramEditorInternal;
 import org.eclipse.graphiti.ui.internal.editor.GFMarqueeSelectionTool;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.graphiti.ui.internal.util.clipboard.ModelClipboard;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.graphiti.util.LocationInfo;
 import org.eclipse.swt.SWT;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.junit.Test;
 
 // Check whether this test makes sense at all...
 public class GFPackageTests extends AbstractGFTests {
 
 	public GFPackageTests() {
 		super();
 	}
 
 	@Test
	public void testGraphiti() throws Exception {
 		String[] providerIds = GraphitiUi.getExtensionManager().getDiagramTypeProviderIds(ITestConstants.DIAGRAM_TYPE_ID_SKETCH);
 		assertTrue("no dtp for sketch diagram type available", providerIds.length > 0);
 		if (providerIds.length > 0) {
			IDiagramTypeProvider dtp = GraphitiUi.getExtensionManager().createDiagramTypeProvider(null, providerIds[0]);
 			assertNotNull("dtp couldn't be instantiated", dtp);
 		}
 	}
 
 	@Test
 	public void testGraphitiUi() throws Exception {
 		String id = DiagramEditor.DIAGRAM_EDITOR_ID;
 		assertNotNull(id);
 		assertTrue(!("".equals(id)));
 		assertNotNull(GraphitiUIPlugin.getDefault());
 	}
 
 	@Test
 	public void testGraphitiUiInternal() throws Exception {
 		org.eclipse.swt.graphics.Image imageForId = GraphitiUi.getImageService().getImageForId(IPlatformImageConstants.IMG_DIAGRAM);
 		GraphitiUiInternal.getUiService().createImage(imageForId, SWT.IMAGE_GIF);
 	}
 
 	@Test
 	public void testGraphitiUiInternalEditor() throws Exception {
 		new GFMarqueeSelectionTool();
 	}
 
 	@Test
 	public void testGraphitiFeatures() throws Exception {
 
 		SketchDiagramTypeProvider mySketchDiagramTypeProvider = new SketchDiagramTypeProvider();
 		IFeatureProvider provider = mySketchDiagramTypeProvider.getFeatureProvider();
 		assertTrue(provider instanceof ConfigurableFeatureProviderWrapper);
 		ConfigurableFeatureProviderWrapper myConfigurableFeatureProviderWrapper = (ConfigurableFeatureProviderWrapper) provider;
 
 		// test canAdd
 		AreaContext areaContext = new AreaContext();
 		areaContext.setLocation(10, 20);
 		final Diagram diagram = createDiagram("testPackageOrgEclipseMiGfwFeatures");
 		Object value = new Object();
 		AddContext addContext = new AddContext(areaContext, value);
 		addContext.setTargetContainer(diagram);
 		assertTrue(myConfigurableFeatureProviderWrapper.canAdd(addContext).toBoolean());
 		addContext.setTargetContainer(null);
 		assertFalse(myConfigurableFeatureProviderWrapper.canAdd(addContext).toBoolean());
 
 		//test getAddFeature
 		addContext.setTargetContainer(diagram);
 		assertNotNull(myConfigurableFeatureProviderWrapper.getAddFeature(addContext));
 
 		//test canLayout
 		DiagramEditorInternal diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		PictogramElement pe = getPictogramElement(diagramEditor);
 		LayoutContext layoutContext = new LayoutContext(pe);
 		assertFalse(myConfigurableFeatureProviderWrapper.canLayout(layoutContext).toBoolean());
 		closeEditor(diagramEditor);
 	}
 
 	@Test
 	public void testGraphitiFeaturesContextJam() throws Exception {
 
 		final class MyPasteFeature extends AbstractPasteFeature {
 
 			private PictogramElement[] copy;
 			private PictogramElement[] fromClipboard = new PictogramElement[1];
 			private Diagram diagram;
 
 			public MyPasteFeature(IFeatureProvider fp, Diagram diagram) {
 				super(fp);
 				this.diagram = diagram;
 			}
 
 			@Override
 			public boolean canPaste(IPasteContext context) {
 				this.copy = context.getPictogramElements();
 				if (this.copy.length == 1) {
 					if (this.copy[0] instanceof ContainerShape) {
 						return getFromClipboard().length > 0;
 					}
 				}
 				return false;
 			}
 
 			@Override
 			public void paste(IPasteContext context) {
 				Object[] obs = getCopiesFromClipBoard(this.diagram);
 				assertTrue((obs != null && obs.length > 0 && obs[0] instanceof PictogramElement));
 				for (int i = 0; i < obs.length; i++) {
 					if (obs[i] instanceof PictogramElement) {
 						this.fromClipboard[i] = (PictogramElement) obs[i];
 					}
 				}
 			}
 
 			public boolean isEqual() {
 				boolean res = false;
 				if (this.copy != null && this.fromClipboard != null) {
 					res = EcoreUtil.equals(this.copy[0], this.fromClipboard[0]);
 				}
 				return res;
 			}
 		}
 
 		String s = null;
 		TestDiagramTypeProvider myDiagramTypeProvider = new TestDiagramTypeProvider();
 		final DiagramEditorInternal diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		final Diagram diagram = diagramEditor.getDiagramTypeProvider().getDiagram();
 		myDiagramTypeProvider.init(diagram, diagramEditor);
 		PictogramElement pe = getPictogramElement(diagramEditor);
 		EList<Shape> shapes = diagram.getChildren();
 		assertNotNull(pe);
 		PictogramElement[] pes = new PictogramElement[] { pe };
 
 		final DefaultCopyFeature myDefaultCopyFeature = new DefaultCopyFeature(myDiagramTypeProvider.getFeatureProvider());
 		final ICopyContext copyContext = new CopyContext(pes);
 		assertEquals(true, myDefaultCopyFeature.canExecute(copyContext));
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				myDefaultCopyFeature.execute(copyContext);
 			}
 		});
 
 		s = null;
 		s = myDefaultCopyFeature.getName();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 		final MyPasteFeature myPasteFeature = new MyPasteFeature(myDiagramTypeProvider.getFeatureProvider(), diagram);
 		final IPasteContext pasteContext = new PasteContext(pes);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				assertEquals(true, myPasteFeature.canExecute(pasteContext));
 
 				myPasteFeature.execute(pasteContext);
 				diagramEditor.doSave(null);
 				//duplicated and pasted objects are not equal with original objects
 				assertFalse(myPasteFeature.isEqual());
 			}
 		});
 
 		s = null;
 		s = myPasteFeature.getName();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test ModelClipboard to paste a root element (parent = null)
 		final Diagram dia = myDiagramTypeProvider.getDiagram();
 		final EObject[] objs = new EObject[] { dia };
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				ModelClipboard.getDefault().setContent(objs);
 				Collection<EObject> copy = ModelClipboard.getDefault().duplicateAndPaste(null, getTransactionalEditingDomain());
 				diagramEditor.doSave(null);
 				assertTrue(!copy.isEmpty() && !copy.contains(dia));
 			}
 		});
 
 		DefaultMoveAnchorFeature myDefaultMoveAnchorFeature = new DefaultMoveAnchorFeature(myDiagramTypeProvider.getFeatureProvider());
 
 		FixPointAnchor fixPointAnchorMock = createNiceMock(FixPointAnchor.class);
 		replay(fixPointAnchorMock);
 
 		AreaAnchorContext myAreaAnchorContext = new AreaAnchorContext(fixPointAnchorMock);
 		assertEquals(true, myDefaultMoveAnchorFeature.canMoveAnchor(myAreaAnchorContext));
 
 		myAreaAnchorContext.setLocation(10, 20);
 		myDefaultMoveAnchorFeature.canExecute(myAreaAnchorContext);
 
 		myDefaultMoveAnchorFeature.moveAnchor(myAreaAnchorContext);
 
 		myDefaultMoveAnchorFeature.execute(myAreaAnchorContext);
 
 		GraphicsAlgorithm graphicsAlgorithmMock = createNiceMock(GraphicsAlgorithm.class);
 		replay(graphicsAlgorithmMock);
 
 		AnchorContainer myAnchorContainer = createNiceMock(AnchorContainer.class);
 		expect(myAnchorContainer.getGraphicsAlgorithm()).andReturn(graphicsAlgorithmMock);
 		replay(myAnchorContainer);
 
 		BoxRelativeAnchor boxRelativeAnchor = createNiceMock(BoxRelativeAnchor.class);
 		expect(boxRelativeAnchor.getParent()).andReturn(myAnchorContainer);
 		replay(boxRelativeAnchor);
 
 		myAreaAnchorContext = new AreaAnchorContext(boxRelativeAnchor);
 		myAreaAnchorContext.setLocation(10, 20);
 
 		myDefaultMoveAnchorFeature.moveAnchor(myAreaAnchorContext);
 
 		s = null;
 		s = myDefaultMoveAnchorFeature.getName();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		DefaultReconnectionFeature myDefaultReconnectionFeature = new DefaultReconnectionFeature(myDiagramTypeProvider.getFeatureProvider());
 
 		Anchor anchorMock = createNiceMock(Anchor.class);
 		expect(anchorMock.getParent()).andReturn(shapes.get(0));
 		replay(anchorMock);
 
 		org.eclipse.graphiti.mm.pictograms.Connection connectionMock = createNiceMock(org.eclipse.graphiti.mm.pictograms.Connection.class);
 		expect(connectionMock.getEnd()).andReturn(boxRelativeAnchor).anyTimes();
 		expect(connectionMock.getStart()).andReturn(anchorMock).anyTimes();
 		replay(connectionMock);
 
 		IReconnectionContext myReconnectionContext = new ReconnectionContext(connectionMock, anchorMock, anchorMock);
 		myReconnectionContext.setTargetPictogramElement(pe);
 
 		assertTrue(myDefaultReconnectionFeature.canExecute(myReconnectionContext));
 
 		myDefaultReconnectionFeature.reconnect(myReconnectionContext);
 
 		s = null;
 		s = myDefaultReconnectionFeature.getName();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		DefaultFeatureProvider myDefaultJAMFeatureProvider = new DefaultFeatureProvider(myDiagramTypeProvider);
 		IUpdateContext context = new UpdateContext(null);
 		IUpdateFeature updateFeature = myDefaultJAMFeatureProvider.getUpdateFeature(context);
 
 		final PictogramLink linkForPictogramElement = Graphiti.getLinkService().getLinkForPictogramElement(pe);
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 
 				if (linkForPictogramElement != null) {
 					final EList<EObject> businessObject = linkForPictogramElement.getBusinessObjects();
 					if (businessObject != null && !businessObject.isEmpty()) {
 						final TransactionalEditingDomain edDom = getTransactionalEditingDomain();
 						edDom.getCommandStack().execute(new RecordingCommand(edDom) {
 							@Override
 							protected void doExecute() {
 								businessObject.removeAll(businessObject);
 							}
 						});
 					}
 				}
 			}
 		});
 		context = new UpdateContext(pe);
 		try {
 			updateFeature.update(context);
 		} catch (Exception e) {
 			// ignore
 		}
 
 		closeEditor(diagramEditor);
 	}
 
 	private PictogramElement getPictogramElement(DiagramEditorInternal diagramEditor) {
 
 		Diagram diagram = diagramEditor.getDiagramTypeProvider().getDiagram();
 		addClassesAndReferencesToDiagram(diagramEditor);
 		EList<Shape> shapes = diagram.getChildren();
 		PictogramLink link;
 		link = shapes.get(0).getLink();
 		PictogramElement pe;
 		pe = link.getPictogramElement();
 		return pe;
 	}
 
 	private void addClassesAndReferencesToDiagram(final DiagramEditorInternal diagramEditor) {
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 
 				TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 					@Override
 					protected void doExecute() {
 						addClassesAndReferenceToDiagram(fp, currentDiagram, -100, -100, "Connection", -700, -200, "ConnectionDecorator");
 					}
 				});
 			}
 		});
 	}
 
 	// Should be moved to a unit test plugin for the graphiti pattern plugin
 	@Test
 	public void testMappingData() throws Exception {
 		class MyImageDataMapping extends ImageDataMapping {
 			public MyImageDataMapping(IMappingProvider mappingProvider) {
 				super(mappingProvider);
 			}
 		}
 
 		IMappingProvider mappingProviderMock = createNiceMock(IMappingProvider.class);
 		replay(mappingProviderMock);
 
 		MyImageDataMapping myImageDataMapping = new MyImageDataMapping(mappingProviderMock);
 
 		PictogramLink pictogramLinkMock = createNiceMock(PictogramLink.class);
 		replay(pictogramLinkMock);
 
 		myImageDataMapping.getImageId(pictogramLinkMock);
 
 		myImageDataMapping.getUpdateWarning(pictogramLinkMock);
 	}
 
 	@Test
 	public void testGraphitiUtil() throws Exception {
 		Shape shapeMock = createNiceMock(Shape.class);
 		replay(shapeMock);
 
 		GraphicsAlgorithm graphicsAlgorithmMock = createNiceMock(GraphicsAlgorithm.class);
 		replay(graphicsAlgorithmMock);
 
 		LocationInfo myLocationInfo = new LocationInfo(shapeMock, graphicsAlgorithmMock);
 
 		assertTrue(shapeMock.equals(myLocationInfo.getShape()));
 
 		assertTrue(graphicsAlgorithmMock.equals(myLocationInfo.getGraphicsAlgorithm()));
 
 		graphicsAlgorithmMock = createNiceMock(GraphicsAlgorithm.class);
 		expect(graphicsAlgorithmMock.getX()).andReturn(10).anyTimes();
 		expect(graphicsAlgorithmMock.getY()).andReturn(10).anyTimes();
 		replay(graphicsAlgorithmMock);
 
 		PictogramElement pictogramElementMock = createNiceMock(PictogramElement.class);
 		expect(pictogramElementMock.getGraphicsAlgorithm()).andReturn(graphicsAlgorithmMock).anyTimes();
 		replay(pictogramElementMock);
 
 		shapeMock = createNiceMock(Shape.class);
 		expect(shapeMock.getGraphicsAlgorithm()).andReturn(graphicsAlgorithmMock).anyTimes();
 		replay(shapeMock);
 		EList<Shape> shapeList = new BasicEList<Shape>();
 		shapeList.add(shapeMock);
 
 		EList<org.eclipse.graphiti.mm.algorithms.styles.Point> points = new BasicEList<org.eclipse.graphiti.mm.algorithms.styles.Point>();
 
 		ConnectionDecorator connectionDecoratorMock1 = createNiceMock(ConnectionDecorator.class);
 		expect(connectionDecoratorMock1.isLocationRelative()).andReturn(new Boolean(true)).anyTimes();
 		expect(connectionDecoratorMock1.getGraphicsAlgorithm()).andReturn(graphicsAlgorithmMock).anyTimes();
 		replay(connectionDecoratorMock1);
 
 		ConnectionDecorator connectionDecoratorMock2 = createNiceMock(ConnectionDecorator.class);
 		expect(connectionDecoratorMock2.isLocationRelative()).andReturn(new Boolean(false)).anyTimes();
 		expect(connectionDecoratorMock2.getGraphicsAlgorithm()).andReturn(graphicsAlgorithmMock).anyTimes();
 		replay(connectionDecoratorMock2);
 
 		EList<ConnectionDecorator> decorators = new BasicEList<ConnectionDecorator>();
 		decorators.add(connectionDecoratorMock1);
 		decorators.add(connectionDecoratorMock2);
 
 		FreeFormConnection connectionMock = createNiceMock(FreeFormConnection.class);
 		expect(connectionMock.getBendpoints()).andReturn(points).anyTimes();
 		expect(connectionMock.getConnectionDecorators()).andReturn(decorators).anyTimes();
 		expect(connectionMock.getGraphicsAlgorithm()).andReturn(graphicsAlgorithmMock).anyTimes();
 		replay(connectionMock);
 
 		EList<org.eclipse.graphiti.mm.pictograms.Connection> connections = new BasicEList<org.eclipse.graphiti.mm.pictograms.Connection>();
 		connections.add(connectionMock);
 
 		Diagram diagramMock = createNiceMock(Diagram.class);
 		expect(diagramMock.getChildren()).andReturn(shapeList).anyTimes();
 		expect(diagramMock.getConnections()).andReturn(connections).anyTimes();
 		replay(diagramMock);
 
 		decorators = new BasicEList<ConnectionDecorator>();
 		decorators.add(connectionDecoratorMock2);
 
 		connectionMock = createNiceMock(FreeFormConnection.class);
 		expect(connectionMock.getBendpoints()).andReturn(points).anyTimes();
 		expect(connectionMock.getConnectionDecorators()).andReturn(decorators).anyTimes();
 		expect(connectionMock.getGraphicsAlgorithm()).andReturn(graphicsAlgorithmMock).anyTimes();
 		replay(connectionMock);
 
 		connections = new BasicEList<org.eclipse.graphiti.mm.pictograms.Connection>();
 		connections.add(connectionMock);
 
 		diagramMock = createNiceMock(Diagram.class);
 		expect(diagramMock.getChildren()).andReturn(shapeList).anyTimes();
 		expect(diagramMock.getConnections()).andReturn(connections).anyTimes();
 		replay(diagramMock);
 
 		final DiagramEditorInternal diagramEditor = openDiagram(ITestConstants.DIAGRAM_TYPE_ID_ECORE);
 		final Diagram diagram = diagramEditor.getDiagramTypeProvider().getDiagram();
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 
 				TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 					@Override
 					protected void doExecute() {
 						addClassesAndReferenceToDiagram(fp, currentDiagram, -100, -100, "Connection", -700, -200, "ConnectionDecorator");
 					}
 				});
 
 				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 					@Override
 					protected void doExecute() {
 						moveClassShape(fp, currentDiagram, 300, 300, "Connection");
 					}
 				});
 			}
 
 		});
 
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 
 				TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 					@Override
 					protected void doExecute() {
 						getPeService().setPropertyValue(diagram, "Test", "test");
 					}
 				});
 				Property property = getPeService().getProperty(diagram, "Test");
 				assertTrue("test".equals(property.getValue()));
 			}
 		});
 
 		pictogramElementMock = createNiceMock(PictogramElement.class);
 		replay(pictogramElementMock);
 
 		closeEditor(diagramEditor);
 	}
 
 }
