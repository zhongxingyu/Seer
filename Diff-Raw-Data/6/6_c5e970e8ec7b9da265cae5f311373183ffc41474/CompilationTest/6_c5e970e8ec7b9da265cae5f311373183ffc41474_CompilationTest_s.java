 /*
  * Copyright (c) 2005, 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.gen;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Hashtable;
 
 import junit.framework.TestCase;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.internal.bridge.genmodel.ViewmapProducer;
 import org.eclipse.gmf.tests.Plugin;
 import org.eclipse.gmf.tests.setup.DiaDefSetup;
 import org.eclipse.gmf.tests.setup.DiaDefSource;
 import org.eclipse.gmf.tests.setup.DiaGenSetup;
 import org.eclipse.gmf.tests.setup.DiaGenSource;
 import org.eclipse.gmf.tests.setup.DomainModelFileSetup;
 import org.eclipse.gmf.tests.setup.DomainModelSetup;
 import org.eclipse.gmf.tests.setup.DomainModelSetupInstanceClassName;
 import org.eclipse.gmf.tests.setup.DomainModelSource;
 import org.eclipse.gmf.tests.setup.GenProjectBaseSetup;
 import org.eclipse.gmf.tests.setup.GeneratorConfiguration;
 import org.eclipse.gmf.tests.setup.MapDefSource;
 import org.eclipse.gmf.tests.setup.MapSetup;
 import org.eclipse.gmf.tests.setup.MultiPackageGenSetup;
 import org.eclipse.gmf.tests.setup.MultiplePackagesDomainModelSetup;
 import org.eclipse.gmf.tests.setup.ToolDefSetup;
 import org.eclipse.gmf.tests.setup.ToolDefSource;
 import org.eclipse.gmf.tests.setup.annotated.GenASetup;
 import org.eclipse.gmf.tests.setup.annotated.GraphDefASetup;
 import org.eclipse.gmf.tests.setup.annotated.MapDefASetup;
 import org.eclipse.gmf.tests.setup.annotated.ToolDefASetup;
 import org.eclipse.jdt.core.JavaCore;
 
 /**
  * TODO add compilation check for CustomFigure(FigureAccessor(no fqn), FigureAccessor(fqn));  
  */
 public abstract class CompilationTest extends TestCase {
 	
 	public CompilationTest(String name) {
 		super(name);
 	}
 
 	protected abstract GeneratorConfiguration getGeneratorConfiguration();
 
 	protected abstract ViewmapProducer getViewmapProducer();
 
 	// TODO EditPartViewer[Source|Setup]
 
 	protected MapDefASetup getLibraryMap() throws Exception {
 		URI selected = Plugin.createURI("/models/library/library.ecore"); //$NON-NLS-1$
 		DomainModelSource dmSource =  new DomainModelFileSetup().init(selected);
 		ToolDefSource tdmSource = new ToolDefASetup(dmSource.getModel());
 		DiaDefSource gdmSource = new GraphDefASetup(dmSource.getModel());
 		return new MapDefASetup(dmSource.getModel(), tdmSource.getRegistry(), gdmSource.getCanvasDef());
 	}
 
 	protected DiaGenSource getLibraryGen(boolean rcp) throws Exception {
 		MapDefSource mmSource = getLibraryMap(); 
 		return new GenASetup(mmSource.getMapping(), rcp);
 	}
 
 	// avoid requests like #174171
 	public void testCompileWithStrictOptions() throws Exception {
 		final HashMap<String, String> options = new HashMap<String,String>();
 		options.put(JavaCore.COMPILER_PB_EMPTY_STATEMENT, JavaCore.ERROR);
 		//
 		switchJavaOptions(options);
 		try {
 			testCompileMultiPackageDomain(); // run any test
 		} finally {
 			switchJavaOptions(options);
 		}
 	}
 
 	/**
 	 * Installs java compiler options specified in the map, the map gets updated with
 	 * old values.
 	 */
 	@SuppressWarnings("unchecked")
 	private void switchJavaOptions(HashMap<String, String> options) {
 		Hashtable<Object, Object> settings = JavaCore.getOptions();
 		for (String key : options.keySet()) {
 			String originalValue = (String) settings.get(key);
 			settings.put(key, options.get(key)); // install new
 			options.put(key, originalValue); // keep old
 		}
 		JavaCore.setOptions(settings);
 	}
 
 	public void testRCPCompile() throws Exception {
 		DiaGenSource gmfGenSource = getLibraryGen(true);
 		gmfGenSource.getGenDiagram().getEditorGen().setSameFileForDiagramAndModel(false);
 		generateAndCompile(gmfGenSource, getMutatorsForRCP());
 	}
 
 	public void testCompileDiagram() throws Exception {
 		DiaGenSource gmfGenSource = getLibraryGen(false);
 		generateAndCompile(gmfGenSource, getMutators());
 	}
 
 	public void testCompilePotentialNameClashes() throws Exception {
 		DomainModelSource domainModel = new DomainModelSetup().init();
 		domainModel.getNodeA().getEClass().setName("Node"); // #142211
 		domainModel.getNodeB().getEClass().setName("ShapeNode");
 		domainModel.getLinkAsClass().getEClass().setName("ConnectionNode");
 		domainModel.getNodeA().getNameAttr().setName("attribute");
 		domainModel.getNodeB().getNameAttr().setName("class");
 		domainModel.getDiagramElement().setName("Diagram");
 		MapDefSource mapSource = new MapSetup().init(new DiaDefSetup().init(), domainModel, new ToolDefSetup());
 		DiaGenSource gmfGenSource = new DiaGenSetup(getViewmapProducer()).init(mapSource);
 		generateAndCompile(gmfGenSource, NO_MUTATORS);
 	}
 	
 	public void testCompileInstanceClassNames() throws Exception {
 		DomainModelSetup domainModelSetup = new DomainModelSetupInstanceClassName().init();
 		MapDefSource mapSource = new MapSetup().init(new DiaDefSetup().init(), domainModelSetup, new ToolDefSetup());
 		DiaGenSource gmfGenSource = new DiaGenSetup(getViewmapProducer()).init(mapSource);
 		generateAndCompile(gmfGenSource, getMutatorsForInstanceClassNames());
 	}
 	
 	public void testCompileMultiPackageDomain() throws Exception {
 		DomainModelSource ds = new MultiplePackagesDomainModelSetup().init();
 		MapDefSource ms = new MapSetup().init(new DiaDefSetup().init(), ds, new ToolDefSetup());
 
		final HashSet<EPackage> additionalPacks = new HashSet<EPackage>(8);
 		additionalPacks.add(ds.getNodeA().getEClass().getEPackage());
 		additionalPacks.add(ds.getNodeB().getEClass().getEPackage());
 		additionalPacks.add(ds.getLinkAsClass().getEClass().getEPackage());
 
 		DiaGenSource gmfGenSource = new MultiPackageGenSetup(additionalPacks).init(ms);
 
 		generateAndCompile(gmfGenSource, NO_MUTATORS);
 	}
 
 	protected void generateAndCompile(DiaGenSource genSource, final Collection<IGenDiagramMutator> mutators) throws Exception {
 		new GenProjectBaseSetup(getGeneratorConfiguration()) {
 			@Override
 			protected void generateDiagramPlugin(GenDiagram d) throws Exception {
 				super.generateDiagramPlugin(d);
 				for(IGenDiagramMutator next : mutators) {
 					next.doMutation(d);
 					try {
 						super.generateDiagramPlugin(d);
 					} finally {
 						next.undoMutation(d);
 					}
 				}
 			}
 		}.generateAndCompile(genSource);
 	}
 
 	protected Collection<IGenDiagramMutator> getMutators() {
 		Collection<IGenDiagramMutator> result = new ArrayList<IGenDiagramMutator>();
 		result.add(SAME_FILE_MUTATOR);
 		result.add(SYNCHRONIZED_MUTATOR);
 		return result;
 	}
 
 	protected Collection<IGenDiagramMutator> getMutatorsForRCP() {
 		Collection<IGenDiagramMutator> result = new ArrayList<IGenDiagramMutator>();
 		result.add(SAME_FILE_MUTATOR);
 		return result;
 	}
 
 	protected Collection<IGenDiagramMutator> getMutatorsForInstanceClassNames() {
 		Collection<IGenDiagramMutator> result = new ArrayList<IGenDiagramMutator>();
 		result.add(SYNCHRONIZED_MUTATOR);
 		return result;
 	}
 
 	protected static interface IGenDiagramMutator {
 		public void doMutation(GenDiagram d);
 		public void undoMutation(GenDiagram d);
 	}
 
 	protected static final IGenDiagramMutator SAME_FILE_MUTATOR = new IGenDiagramMutator() {
 		private boolean myIsSameFileForDiagramAndModel;
 		private String myPluginId;
 		public void doMutation(GenDiagram d) {
 			myIsSameFileForDiagramAndModel = d.getEditorGen().isSameFileForDiagramAndModel();
 			d.getEditorGen().setSameFileForDiagramAndModel(!myIsSameFileForDiagramAndModel);
 			myPluginId = d.getEditorGen().getPlugin().getID();
 			d.getEditorGen().getPlugin().setID(myPluginId + ".sameFileForDiagramAndModel");
 		}
 		public void undoMutation(GenDiagram d) {
 			d.getEditorGen().setSameFileForDiagramAndModel(myIsSameFileForDiagramAndModel);
 			d.getEditorGen().getPlugin().setID(myPluginId);
 		}
 	};
 
 	protected static final IGenDiagramMutator SYNCHRONIZED_MUTATOR = new IGenDiagramMutator() {
 		private boolean myIsSynchronized;
 		private String myPluginId;
 		public void doMutation(GenDiagram d) {
 			myIsSynchronized = d.isSynchronized();
 			d.setSynchronized(!myIsSynchronized);
 			myPluginId = d.getEditorGen().getPlugin().getID();
 			d.getEditorGen().getPlugin().setID(myPluginId + ".synchronized");
 		}
 		public void undoMutation(GenDiagram d) {
 			d.setSynchronized(myIsSynchronized);
 			d.getEditorGen().getPlugin().setID(myPluginId);
 		}
 	};
 
 	protected static final Collection<IGenDiagramMutator> NO_MUTATORS = Collections.emptyList();
 }
