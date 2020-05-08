 /*
 * Copyright (c) 2008, 2009 Borland Software Corporation and others
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
 
 import java.util.LinkedList;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.RootEditPart;
 import org.eclipse.gmf.codegen.gmfgen.GenCommonBase;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
 import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DecorationEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DecorationEditPolicy.DecoratorTarget;
 import org.eclipse.gmf.runtime.diagram.ui.internal.editparts.DefaultNodeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.internal.services.decorator.DecoratorService;
 import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorProvider;
 import org.eclipse.gmf.runtime.diagram.ui.services.editpart.EditPartService;
 import org.eclipse.gmf.runtime.emf.ui.services.modelingassistant.ModelingAssistantService;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.Node;
 import org.eclipse.gmf.runtime.notation.NotationFactory;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.gmf.tests.ConfiguredTestCase;
 import org.eclipse.gmf.tests.setup.GenProjectSetup;
 import org.eclipse.gmf.tests.setup.RuntimeBasedGeneratorConfiguration;
 import org.eclipse.gmf.tests.setup.SessionSetup;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 
 /**
  * Check metainformation for GMF RT providers prevents bundle from activation
  * @author artem
  */
 public class BundleActivationTest extends ConfiguredTestCase {
 	private final PreferencesHint prefHint = new PreferencesHint("a.b.c");
 	public final static SessionSetup setup = new SessionSetup(new RuntimeBasedGeneratorConfiguration()) {
 		@Override
 		protected GenProjectSetup createGenProject() throws BundleException {
 			return new GenProjectSetup(getGeneratorConfiguration()) {
 				@Override
 				public GenProjectSetup init(GenEditorGenerator genEditor) throws BundleException {
 					try {
 						int vid = 0x10000;
 						LinkedList<GenCommonBase> vidHolders = new LinkedList<GenCommonBase>();
 						vidHolders.add(genEditor.getDiagram());
 						vidHolders.addAll(genEditor.getDiagram().getTopLevelNodes());
 						vidHolders.addAll(genEditor.getDiagram().getChildNodes());
 						vidHolders.addAll(genEditor.getDiagram().getCompartments());
 						vidHolders.addAll(genEditor.getDiagram().getLinks());
 						for (GenNode n : genEditor.getDiagram().getAllNodes()) {
 							vidHolders.addAll(n.getLabels());
 						}
 						for (GenLink l : genEditor.getDiagram().getLinks()) {
 							vidHolders.addAll(l.getLabels());
 						}
 						for (GenCommonBase cb : vidHolders) {
 							cb.setVisualID(vid++);
 						}
 						genEditor.getDiagram().getShortcutsProvidedFor().add(genEditor.getModelID());
 						generateAndCompile(genEditor);
 						getBundle().start(Bundle.START_TRANSIENT | Bundle.START_ACTIVATION_POLICY);
 					} catch (Exception ex) {
 						throw new RuntimeException(ex);
 					}
 					// !!! do not start bundle nor register it's extensions
 					return this;
 				}
 			}.init(getGenModel());
 		}
 	};
 
 
 	public BundleActivationTest(String name) {
 		super(name);
 		configure(setup);
 	}
 
 	private void assertBundleNotStarted(String msg) throws Exception {
 		assertNotSame(msg, Bundle.ACTIVE, getSetup().getGeneratedPlugin().getState());
 	}
 
 	public void testViewService() throws Exception {
 		assertBundleNotStarted("[sanity]");
 		Diagram d = NotationFactory.eINSTANCE.createDiagram();
 		ViewService.createNode(d, "a-hint", prefHint);
 		final String msg = "View creation should not trigger generated ViewProvider and plugin activation";
 		assertBundleNotStarted(msg);
 		Node n = NotationFactory.eINSTANCE.createNode();
 		@SuppressWarnings("unchecked")
 		EList<View> children = d.getTransientChildren();
 		children.add(n);
 		ViewService.createEdge(d, n, "b-hint", prefHint);
 		assertBundleNotStarted(msg);
 	}
 
 	public void testEditPartService() throws Exception {
 		assertBundleNotStarted("[sanity]");
 		Diagram d = NotationFactory.eINSTANCE.createDiagram();
 		RootEditPart p = EditPartService.getInstance().createRootEditPart(d);
 		assertNotNull(p);
 		assertBundleNotStarted("RootEditPart");
 		Node n = NotationFactory.eINSTANCE.createNode();
 		EditPart ep = EditPartService.getInstance().createGraphicEditPart(n);
 		assertNotNull(ep);
 		assertBundleNotStarted("Regular EditPart");
 	}
 
 	/**
 	 * There are two possible extensions for decoratorProviders extp: shortcut icon and validation markers
 	 */
 	public void testDecoratorService() throws Exception {
 		assertTrue("[sanity]", getSetup().getGenModel().getGenDiagram().generateShortcutIcon());
 		assertBundleNotStarted("[sanity]");
		DecorationEditPolicy decorationEditPolicy = new DecorationEditPolicy();
		decorationEditPolicy.setHost(new ShapeEditPart(null) {});
		DecoratorTarget dt = decorationEditPolicy.new DecoratorTarget();
 		@SuppressWarnings("restriction")
 		IDecoratorProvider dp = DecoratorService.getInstance();
 		dp.createDecorators(dt);
 		assertBundleNotStarted("DecoratorService");
 	}
 
 	public void testModelAssistantService() throws Exception {
 		assertBundleNotStarted("[sanity]");
 		@SuppressWarnings("restriction")
 		EditPart ep = new DefaultNodeEditPart(NotationFactory.eINSTANCE.createNode());
 		ModelingAssistantService.getInstance().getTypesForPopupBar(ep);
 		assertBundleNotStarted("ModelAssistantService#getTypesForPopupBar(EditPart)");
 	}
 }
