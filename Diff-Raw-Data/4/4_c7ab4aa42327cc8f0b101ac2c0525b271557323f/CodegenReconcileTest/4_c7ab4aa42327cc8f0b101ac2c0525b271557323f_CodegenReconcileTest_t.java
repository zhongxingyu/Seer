 /*
  * Copyright (c) 2006, 2008 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Michael Golubev (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.gen;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import junit.framework.Assert;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.gmf.codegen.gmfgen.Attributes;
 import org.eclipse.gmf.codegen.gmfgen.DefaultSizeAttributes;
 import org.eclipse.gmf.codegen.gmfgen.ElementType;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenCommonBase;
 import org.eclipse.gmf.codegen.gmfgen.GenCompartment;
 import org.eclipse.gmf.codegen.gmfgen.GenContainerBase;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderBase;
 import org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.codegen.gmfgen.GenPlugin;
 import org.eclipse.gmf.codegen.gmfgen.GenTopLevelNode;
 import org.eclipse.gmf.codegen.gmfgen.ProviderPriority;
 import org.eclipse.gmf.codegen.gmfgen.Viewmap;
 import org.eclipse.gmf.internal.codegen.util.GMFGenConfig;
 import org.eclipse.gmf.internal.common.reconcile.DefaultDecision;
 import org.eclipse.gmf.internal.common.reconcile.Reconciler;
 import org.eclipse.gmf.internal.common.reconcile.ReconcilerConfigBase;
 import org.eclipse.gmf.tests.ConfiguredTestCase;
 import org.eclipse.gmf.tests.setup.DiaGenSource;
 import org.eclipse.gmf.tests.setup.SessionSetup;
 
 public class CodegenReconcileTest extends ConfiguredTestCase {
 
 	public CodegenReconcileTest(String name) {
 		super(name);
 		myDefaultSetup = SessionSetup.newInstance();
 	}
 
 	protected final GenEditorGenerator getOriginal() {
 		return getSetup().getGenModel().getGenDiagram().getEditorGen();
 	}
 
 	protected final GenEditorGenerator createCopy() {
 		return (GenEditorGenerator) EcoreUtil.copy(getOriginal());
 	}
 
 	public void testLoadGMFGen() throws Exception {
 		GenEditorGenerator original = getOriginal();
 		assertNotNull(original);
 		GenEditorGenerator copy = createCopy();
 		assertNotNull(copy);
 
 		assertFalse(original == copy);
 		assertFalse(original.equals(copy));
 
 		assertEquals(original.getCopyrightText(), copy.getCopyrightText());
 		assertEquals(original.isSameFileForDiagramAndModel(), copy.isSameFileForDiagramAndModel());
 		assertEquals(original.getPackageNamePrefix(), copy.getPackageNamePrefix());
 
 		final String NEW_VALUE = "New Value";
 		copy.setCopyrightText(NEW_VALUE);
 		assertEquals(copy.getCopyrightText(), NEW_VALUE);
 		assertFalse(copy.getCopyrightText().equals(original.getCopyrightText()));
 	}
 
 	public void testReconcileDeepElementWithAlwaysMatcher() throws Exception {
 		class GenPluginChange extends Assert implements UserChange {
 			private final String NEW_PROVIDER = "NewProviderValue";
 			private final String NEW_VERSION = "NewVersionValue";
 			private final String NEW_ID = "NewPluginID";
 			private final String NEW_ACTIVATOR = "NewActivator";
 			private final String NEW_NAME = "NewName With Space";
 			private boolean myExpectedPrintingEnabled;
 
 			public void applyChanges(GenEditorGenerator old) {
 				GenPlugin genPlugin = old.getPlugin();
 				assertNotNull(genPlugin.getProvider());
 				assertNotNull(genPlugin.getVersion());
 				assertNotNull(genPlugin.getID());
 				assertNotNull(genPlugin.getActivatorClassName());
 				assertNotNull(genPlugin.getName());
 				assertFalse(genPlugin.isPrintingEnabled());
 				
 				myExpectedPrintingEnabled = !genPlugin.isPrintingEnabled();
 
 				genPlugin.setProvider(NEW_PROVIDER);
 				genPlugin.setVersion(NEW_VERSION);
 				genPlugin.setID(NEW_ID);
 				genPlugin.setActivatorClassName(NEW_ACTIVATOR);
 				genPlugin.setPrintingEnabled(myExpectedPrintingEnabled);
 				genPlugin.setName(NEW_NAME);
 			}
 			
 			public void assertChangesPreserved(GenEditorGenerator current) {
 				GenPlugin genPlugin = current.getPlugin();
 				assertEquals(NEW_PROVIDER, genPlugin.getProvider());
 				assertEquals(NEW_VERSION, genPlugin.getVersion());
 				assertEquals(NEW_ID, genPlugin.getID());
 				assertEquals(NEW_ACTIVATOR, genPlugin.getActivatorClassName());
 				assertEquals(myExpectedPrintingEnabled, genPlugin.isPrintingEnabled());
 				assertEquals(NEW_NAME, genPlugin.getName());
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 		}
 		
 		checkUserChange(new GenPluginChange());
 	}
 	
 	public void testReconcileCompartmentIsListLayout(){
 		class CompartmentChange extends Assert implements UserChange {
 			private int myCompartmentsTotalCount;
 			private final EStructuralFeature myGenCompartmentFeature;
 			private final Boolean myExpectedValue;
 
 			public CompartmentChange(EStructuralFeature genCompartmentFeature, boolean expectedValue){
 				assertEquals(EcorePackage.eINSTANCE.getEBoolean(), genCompartmentFeature.getEType());
 				myGenCompartmentFeature = genCompartmentFeature;
 				myExpectedValue = Boolean.valueOf(expectedValue);
 			}
 			
 			public final void applyChanges(GenEditorGenerator old) {
 				GenDiagram diagram = old.getDiagram();
 				assertFalse("Precondition, we need some nodes", diagram.getChildNodes().isEmpty());
 				assertFalse("Precondition, we need some nodes", diagram.getTopLevelNodes().isEmpty());
 				
 				myCompartmentsTotalCount = 0;
 				for (GenNode next : diagram.getAllNodes()){
 					for (GenCompartment nextCompartment : next.getCompartments()){
 						myCompartmentsTotalCount++;
 						nextCompartment.eSet(myGenCompartmentFeature, myExpectedValue);
 					}
 				}
 				
 				assertTrue(myCompartmentsTotalCount > 0);
 			}
 			
 			public final void assertChangesPreserved(GenEditorGenerator current) {
 				GenDiagram diagram = current.getDiagram();
 				assertFalse(diagram.getChildNodes().isEmpty());
 				assertFalse(diagram.getTopLevelNodes().isEmpty());
 				
 				int actualCompartmentsTotalCount = 0;
 				for (GenNode next : diagram.getAllNodes()){
 					for (GenCompartment nextCompartment : next.getCompartments()){
 						actualCompartmentsTotalCount++;
 						Boolean actualValue = (Boolean)nextCompartment.eGet(myGenCompartmentFeature);
 						assertEquals(getChangeDescription(), myExpectedValue, actualValue);
 					}
 				}
 				
 				assertEquals(myCompartmentsTotalCount, actualCompartmentsTotalCount);
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 			
 			private String getChangeDescription() {
 				return "CompartmentChange: " + myGenCompartmentFeature.getName() + ":" + myExpectedValue;
 			}
 		}
 		
 		DiaGenSource diaGenSource = getSetup().getGenModel();
 		assertFalse(diaGenSource.getNodeA().getCompartments().isEmpty());
 		assertFalse(diaGenSource.getNodeB().getCompartments().isEmpty());
 		
 		final GMFGenPackage GMFGEN = GMFGenPackage.eINSTANCE;
 		
 //		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_CanCollapse(), true));
 //		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_CanCollapse(), false));
 		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_HideIfEmpty(), true));
 		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_HideIfEmpty(), false));
 //		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_NeedsTitle(), true));
 //		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_NeedsTitle(), false));
 //		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_ListLayout(), true));
 		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_ListLayout(), false));
 	}
 	
 	public void testReconcileGenNodes() throws Exception {			
 		class ListLayoutChange extends Assert implements UserChange {
 			private final String NEW_CANONICAL_EP = "MyCanonicalPolicy";
 			private final String NEW_GRAPHICAL_EP = "MyGraphicalPolicy";
 			private final String BAD_CANONICAL_EP = "MyCanonicalEditPolicy"; //changed but still follows "(.*)CanonicalEditPolicy" pattern
 			private final String BAD_GRAPHICAL_EP = "MyGraphicalNodeEditPolicy"; //changed but still follows "(.*)GraphicalNodeEditPolicy" pattern
 			
 			public void applyChanges(GenEditorGenerator old) {
 				EList<GenTopLevelNode> oldNodes = old.getDiagram().getTopLevelNodes();
 				assertTrue(oldNodes.size() > 1);
 				GenNode nodeA = oldNodes.get(0);
 				GenNode nodeB = oldNodes.get(1);
 				
 				nodeA.setCanonicalEditPolicyClassName(NEW_CANONICAL_EP);
 				nodeA.setGraphicalNodeEditPolicyClassName(NEW_GRAPHICAL_EP);
 				
 				nodeB.setCanonicalEditPolicyClassName(BAD_CANONICAL_EP);
 				nodeB.setGraphicalNodeEditPolicyClassName(BAD_GRAPHICAL_EP);
 			}
 			
 			public void assertChangesPreserved(GenEditorGenerator current) {
 				EList<GenTopLevelNode> currentNodes = current.getDiagram().getTopLevelNodes();
 				assertTrue(currentNodes.size() > 1);
 				GenNode nodeA = currentNodes.get(0);
 				GenNode nodeB = currentNodes.get(1);
 				
 				assertEquals(NEW_CANONICAL_EP, nodeA.getCanonicalEditPolicyClassName());
 				assertEquals(NEW_GRAPHICAL_EP, nodeA.getGraphicalNodeEditPolicyClassName());
 				
 				assertEquals(BAD_CANONICAL_EP, nodeB.getCanonicalEditPolicyClassName());
 				assertEquals(BAD_GRAPHICAL_EP, nodeB.getGraphicalNodeEditPolicyClassName());
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 		}
 		
 		checkUserChange(new ListLayoutChange());
 	}
 	
 	public void testReconcileGenDiagram(){
 		final class DiagramChange extends SingleChange {
 			public DiagramChange(EAttribute attribute, String valueToSet) {
 				super(attribute, valueToSet);
 			}
 
 			public DiagramChange(EAttribute attribute, boolean valueToSet) {
 				super(attribute, valueToSet);
 			}
 
 			protected EObject findChangeSubject(GenEditorGenerator root) {
 				return root.getDiagram();
 			}
 		}
 		
 		GMFGenPackage GMF = GMFGenPackage.eINSTANCE;
 		checkUserChange(new DiagramChange(GMF.getGenDiagram_Synchronized(), true));
 		checkUserChange(new DiagramChange(GMF.getGenDiagram_Synchronized(), false));
 		
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardIconPath(), null));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardIconPath(), ""));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardIconPath(), "\\..\\a\\B\\c"));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardIconPath(), "http://localhost:8080/"));
 		
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), null));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), ""));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), "   "));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), "org.eclipse.ui.Examples")); //actual default value
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), "com.myCompany.TheBestCategory"));
 	}
 
 	public void testReconcileGenDiagram_Shortcuts(){
 		final String[] PROVIDED_FOR = {"ModelA", "ModelB", "ModelC"}; 
 		final String[] CONTAINS_TO = {"txt", "mdm", "taipan"};
 		final String[] EMPTY = new String[0];
 		
 		class ShortcutChange extends Assert implements UserChange {
 			private final String[] myProvidedFor;
 			private final String[] myContainsTo;
 			
 			public ShortcutChange(String[] providedFor, String[] containsTo){
 				myProvidedFor = providedFor;
 				myContainsTo = containsTo;
 			}
 			
 			public void applyChanges(GenEditorGenerator old) {
 				GenDiagram diagram = old.getDiagram();
 				assertNotNull(diagram);
 				diagram.getShortcutsProvidedFor().addAll(Arrays.asList(myProvidedFor));
 				diagram.getContainsShortcutsTo().addAll(Arrays.asList(myContainsTo));
 				
 				assertEquals(Arrays.asList(myProvidedFor), diagram.getShortcutsProvidedFor());
 				assertEquals(Arrays.asList(myContainsTo), diagram.getContainsShortcutsTo());
 			}
 
 			public void assertChangesPreserved(GenEditorGenerator current) {
 				GenDiagram diagram = current.getDiagram();
 				assertEquals(Arrays.asList(myProvidedFor), diagram.getShortcutsProvidedFor());
 				assertEquals(Arrays.asList(myContainsTo), diagram.getContainsShortcutsTo());
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 		}
 		
 		ShortcutChange someChange = new ShortcutChange(PROVIDED_FOR, CONTAINS_TO); 
 		ShortcutChange emptyChange = new ShortcutChange(EMPTY, EMPTY);
 		
 		checkUserChange(someChange);
 		checkUserChange(emptyChange);
 	}
 
 	public void testReconcileGenEditorGenerator_LimitedConfig() throws Exception {
 		class UserChangeImpl extends Assert implements UserChange {
 			private boolean mySameFile;
 			private final boolean myExpectingCopyrightPreserved;
 			
 			public UserChangeImpl(boolean reconcileCopyright){
 				myExpectingCopyrightPreserved = reconcileCopyright;
 			}
 			
 			public void applyChanges(GenEditorGenerator old){
 				old.setCopyrightText("AAA");
 				old.setPackageNamePrefix("BBB");
 				old.setDiagramFileExtension("CCC");
 				
 				mySameFile = !old.isSameFileForDiagramAndModel();
 
 				old.setSameFileForDiagramAndModel(mySameFile);
 
 				// we do not reconcile this with limited config
 				old.setTemplateDirectory("DDD");
 				assertEquals("DDD", old.getTemplateDirectory());
 			}
 			
 			public void assertChangesPreserved(GenEditorGenerator current){
 				if (myExpectingCopyrightPreserved){
 					assertEquals("AAA", current.getCopyrightText());
 				} else {
 					assertFalse("AAA".equals(current.getCopyrightText()));
 				}
 
 				assertEquals("BBB", current.getPackageNamePrefix());
 				assertEquals("CCC", current.getDiagramFileExtension());
 				assertEquals(mySameFile, current.isSameFileForDiagramAndModel());
 				
 				//not expected to be reconciled -- limited config
 				assertFalse("DDD".equals(current.getTemplateDirectory()));
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig(){
 				return new LimitedGMFGenConfig(myExpectingCopyrightPreserved);
 			}
 		}
 		
 		checkUserChange(new UserChangeImpl(false));
 		checkUserChange(new UserChangeImpl(true));
 		
 	}
 	
 	public void testReconcileGenEditorGenerator(){
 		class GenEditorGeneratorChange extends SingleChange {
 			public GenEditorGeneratorChange(EAttribute attribute, boolean expectedValue) {
 				super(attribute, expectedValue);
 			}
 
 			public GenEditorGeneratorChange(EAttribute attribute, Object expectedValue) {
 				super(attribute, expectedValue);
 			}
 
 			protected final EObject findChangeSubject(GenEditorGenerator root) {
 				return root;
 			}
 		}
 
 		class TemplateDirectoryChange extends GenEditorGeneratorChange {
 			public TemplateDirectoryChange(String value){
 				super(GMFGenPackage.eINSTANCE.getGenEditorGenerator_TemplateDirectory(), value);
 			}
 		}
 		
 		class DynamicTemplatesChange extends GenEditorGeneratorChange {
 			public DynamicTemplatesChange(boolean value) {
 				super(GMFGenPackage.eINSTANCE.getGenEditorGenerator_DynamicTemplates(), value);
 			}
 		}
 		
 		class ModelIdChange extends GenEditorGeneratorChange {
 			public ModelIdChange(String value){
 				super(GMFGenPackage.eINSTANCE.getGenEditorGenerator_ModelID(), value);
 			}
 		}
 		
 		checkUserChange(new DynamicTemplatesChange(true));
 		checkUserChange(new DynamicTemplatesChange(false));
 		
 		checkUserChange(new TemplateDirectoryChange(null));
 		checkUserChange(new TemplateDirectoryChange(""));
 		checkUserChange(new TemplateDirectoryChange("\\a\\b\\c"));
 		checkUserChange(new TemplateDirectoryChange("c:/my-folder/my templates with space/"));
 
 		checkUserChange(new ModelIdChange("ABC"));
 		checkUserChange(new ModelIdChange("ABC   "));
 		checkUserChange(new ModelIdChange(""));
 		checkUserChange(new ModelIdChange(" "));
 		checkUserChange(new ModelIdChange(null));
 
 		checkUserChange(new GenEditorGeneratorChange(GMFGenPackage.eINSTANCE.getGenEditorGenerator_DomainFileExtension(), "xxx"));
 		
 		checkUserChange(new UserChange() {
 
 			public void applyChanges(GenEditorGenerator old) {
 				assertNotNull(old.getNavigator());
 				old.setNavigator(null);
 			}
 
 			public void assertChangesPreserved(GenEditorGenerator current) {
 				assertNull(current.getNavigator());
 			}
 
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 			
 		});
 	}
 	
 	public void testReconcileGenEditorView(){
 		final class EditorChange extends SingleChange {
 			public EditorChange(EAttribute attribute, String valueToSet) {
 				super(attribute, valueToSet);
 			}
 
 			protected EObject findChangeSubject(GenEditorGenerator root) {
 				return root.getEditor();
 			}
 		}
 		
 		GMFGenPackage GMF = GMFGenPackage.eINSTANCE;
 		checkUserChange(new EditorChange(GMF.getGenEditorView_IconPath(), null));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_IconPath(), ""));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_IconPath(), "//a//b//c"));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_IconPath(), "c:\\myIconsFolder"));
 
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ClassName(), null));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ClassName(), ""));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ClassName(), "MyClass"));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ClassName(), "org.eclipse.MyClass"));
 		
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ID(), null));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ID(), ""));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ID(), "my.editor.id"));
 	}
 	
 	public void testReconcileViewmapAttributes(){
 		abstract class AbstractAttributesChange implements UserChange {
 			private int myAffectedViewmapsCount;
 			
 			protected abstract Attributes findAttributes(Viewmap viewmap); 
 			protected abstract Attributes createUserAttributes();
 			protected abstract void assertChanges(Attributes attributes);
 			
 			public final void applyChanges(GenEditorGenerator old) {
 				myAffectedViewmapsCount = 0;
 				for (GenNode next : old.getDiagram().getAllNodes()){
 					Viewmap nextViewmap = next.getViewmap();
 					if (nextViewmap == null){
 						continue;
 					}
 					Attributes attributes = findAttributes(nextViewmap);
 					assertNull("Reconciler is intended to work with attributes that are created only by user", attributes);
 					attributes = createUserAttributes();
 					nextViewmap.getAttributes().add(attributes);
 					myAffectedViewmapsCount++;
 				}
 				assertTrue(myAffectedViewmapsCount > 0);
 			}
 			
 			public final void assertChangesPreserved(GenEditorGenerator current) {
 				int checkedViewmapsCount = 0;
 				for (GenNode next : current.getDiagram().getAllNodes()){
 					Viewmap nextViewmap = next.getViewmap();
 					if (nextViewmap == null){
 						continue;
 					}
 					Attributes attributes = findAttributes(nextViewmap);
 					assertNotNull(attributes);
 					assertChanges(attributes);
 					checkedViewmapsCount++;
 				}
 				assertEquals(myAffectedViewmapsCount, checkedViewmapsCount);
 			}
 			
 			public final ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 		}
 		
 		class DefaultSizeChange extends AbstractAttributesChange {
 			private static final int HEIGHT = 23;
 			private static final int WIDTH = 32;
 			
 			protected void assertChanges(Attributes attributes) {
 				DefaultSizeAttributes defaultSize = (DefaultSizeAttributes)attributes;
 				assertEquals(HEIGHT, defaultSize.getHeight());
 				assertEquals(WIDTH, defaultSize.getWidth());
 			}
 			
 			protected Attributes createUserAttributes() {
 				DefaultSizeAttributes defaultSize = GMFGenFactory.eINSTANCE.createDefaultSizeAttributes();
 				defaultSize.setHeight(HEIGHT);
 				defaultSize.setWidth(WIDTH);
 				return defaultSize;
 			}
 			
 			protected Attributes findAttributes(Viewmap viewmap) {
 				return viewmap.find(DefaultSizeAttributes.class);
 			}
 		}
 		
 		checkUserChange(new DefaultSizeChange());
 	}
 	
 	public void testReconcileMetamodelType(){
 		abstract class ElementTypeChange implements UserChange {
 			protected abstract void applyChange(ElementType elementType);
 			protected abstract void assertChange(ElementType elementType);
 			
 			protected Collection<GenContainerBase> collectSubjects(GenEditorGenerator editorGenerator){
 				LinkedList<GenContainerBase> allWithType = new LinkedList<GenContainerBase>();
 				GenDiagram diagram = editorGenerator.getDiagram();
 				allWithType.add(diagram);
 				allWithType.addAll(diagram.getAllChildContainers());
 				//XXX: we do not know how to match links yet 
 				//allWithType.addAll(diagram.getLinks());
 				return allWithType;
 			}
 			
 			public final void applyChanges(GenEditorGenerator old) {
 				for (GenCommonBase next : collectSubjects(old)) {
 					ElementType nextElementType = next.getElementType();
 					if (nextElementType == null){
 						continue;
 					}
 					applyChange(nextElementType);
 				}
 			}
 			
 			public final void assertChangesPreserved(GenEditorGenerator current) {
 				for (GenCommonBase next : collectSubjects(current)) {
 					ElementType nextElementType = next.getElementType();
 					if (nextElementType == null){
 						continue;
 					}
 					assertChange(nextElementType);
 				}
 			}
 			
 			public final ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 
 			protected String toString(ElementType elementType) {
 				return String.valueOf(elementType)/* + " for :" + String.valueOf(elementType.getDiagramElement())*/;
 			}
 		
 		}
 		
 		class DisplayNameChange extends ElementTypeChange {
 			private final String myValue;
 
 			public DisplayNameChange(String value){
 				myValue = value;
 			}
 			
 			protected void applyChange(ElementType elementType) {
 				elementType.setDisplayName(myValue);
 			}
 			
 			protected void assertChange(ElementType elementType) {
 				assertEquals(toString(elementType), myValue, elementType.getDisplayName());
 			}
 		}
 		
 		class DefinedExternallyChange extends ElementTypeChange {
 			private boolean myValue;
 			
 			public DefinedExternallyChange(boolean value){
 				myValue = value;
 			}
 			
 			protected void applyChange(ElementType elementType) {
 				elementType.setDefinedExternally(myValue);
 			}
 			
 			protected void assertChange(ElementType elementType) {
 				assertEquals(toString(elementType), myValue, elementType.isDefinedExternally());
 			}
 
 		}
 		
 		checkUserChange(new DisplayNameChange("ABCD"));
 		//XXX: does not work: checkUserChange(new DisplayNameChange(""));
 		checkUserChange(new DefinedExternallyChange(true));
 		checkUserChange(new DefinedExternallyChange(false));
 	}
 	
 	public void testGenNavigator() {
 		class DomainNavigatorRemovingChange implements UserChange {
 
 			public void applyChanges(GenEditorGenerator old) {
 				assertNotNull(old.getNavigator());
 				old.getNavigator().setGenerateDomainModelNavigator(false);
 			}
 
 			public void assertChangesPreserved(GenEditorGenerator current) {
 				assertFalse(current.getNavigator().isGenerateDomainModelNavigator());
 			}
 
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 			
 		}
 		checkUserChange(new DomainNavigatorRemovingChange());
 		class NavigatorChange extends SingleChange {
 			
 			public NavigatorChange(EAttribute attribute, String valueToSet) {
 				super(attribute, valueToSet);
 			}
 
 			protected EObject findChangeSubject(GenEditorGenerator root) {
 				assertNotNull(root.getNavigator());
 				return root.getNavigator();
 			}
 			
 		}
 		GMFGenPackage gmfGenPackage = GMFGenPackage.eINSTANCE;
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenDomainModelNavigator_DomainContentExtensionID(), "customId"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenDomainModelNavigator_DomainContentExtensionName(), "customName"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenDomainModelNavigator_DomainContentExtensionPriority(), "customPriorityName"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenDomainModelNavigator_DomainContentProviderClassName(), "CustomContentProvider"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenDomainModelNavigator_DomainLabelProviderClassName(), "CustomLabelProvider"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenDomainModelNavigator_DomainModelElementTesterClassName(), "CustomModelElementTester"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenDomainModelNavigator_DomainNavigatorItemClassName(), "CustomNavigatorItem"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_ContentExtensionID(), "customID"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_ContentExtensionName(), "customName"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_ContentExtensionPriority(), "customPriority"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_LinkHelperExtensionID(), "customID"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_SorterExtensionID(), "customID"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_ActionProviderID(), "customID"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_ContentProviderClassName(), "CustomContentProvider"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_LabelProviderClassName(), "CustomLabelProvider"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_LinkHelperClassName(), "CustomLinkHelper"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_SorterClassName(), "CustomSorter"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_ActionProviderClassName(), "CustomActionProvider"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_AbstractNavigatorItemClassName(), "CustomAbstractNavigator"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_NavigatorGroupClassName(), "CustomNavigatorGroup"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_NavigatorItemClassName(), "CustomItemClass"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_UriInputTesterClassName(), "CustomURITester"));
 		checkUserChange(new NavigatorChange(gmfGenPackage.getGenNavigator_PackageName(), "customPackage"));
 	}
 	
 	public void testGenJavaExpressionPovider() {
 		GenEditorGenerator editorGen = createCopy();
 		editorGen.setExpressionProviders(GMFGenFactory.eINSTANCE.createGenExpressionProviderContainer());
 		editorGen.getExpressionProviders().getProviders().add(GMFGenFactory.eINSTANCE.createGenJavaExpressionProvider());
 
 		class GenJavaExpressionProviderChange extends SingleChange {
 			public GenJavaExpressionProviderChange(EAttribute attribute, boolean valueToSet) {
 				super(attribute, valueToSet);
 			}
 			protected EObject findChangeSubject(GenEditorGenerator root) {
 				assertNotNull(root.getExpressionProviders());
 				for (GenExpressionProviderBase expressionProvider : root.getExpressionProviders().getProviders()) {
 					if (expressionProvider instanceof GenJavaExpressionProvider) {
 						return expressionProvider;
 					}
 				}
 				fail("No GenJavaExpression provider found.");
 				return null;
 			}
 		}
 		checkUserChange(new GenJavaExpressionProviderChange(GMFGenPackage.eINSTANCE.getGenJavaExpressionProvider_InjectExpressionBody(), true), editorGen, (GenEditorGenerator) EcoreUtil.copy(editorGen));
 	}
 
 	public void testGenParsers() {
		getOriginal().getLabelParsers().setProviderPriority(null); // reset to default value, as previous
		// tests may alter it to another value, and as long as preserveIfSet/DefaultDecision
		// doesn't keep old value if there's new value which is eIsSet() == true, need to make sure
		// this tests starts with a clean state ("new" model passed to reconciler has default value)
 		class GenParsersChange extends SingleChange {
 
 			public GenParsersChange(EAttribute attr, Object value) {
 				super(attr, value);
 			}
 
 			@Override
 			protected EObject findChangeSubject(GenEditorGenerator genEditor) {
 				return genEditor.getLabelParsers();
 			}
 		}
 		GMFGenPackage ePack = GMFGenPackage.eINSTANCE;
 		checkUserChange(new GenParsersChange(ePack.getGenParsers_ClassName(), "ClassNaaaame"));
 		checkUserChange(new GenParsersChange(ePack.getGenParsers_PackageName(), "org.ssaammppllee"));
 		checkUserChange(new GenParsersChange(ePack.getGenParsers_ProviderPriority(), ProviderPriority.HIGH_LITERAL));
 		assertEquals("Sanity", Boolean.FALSE, ePack.getGenParsers_ExtensibleViaService().getDefaultValue());
 		checkUserChange(new GenParsersChange(ePack.getGenParsers_ExtensibleViaService(), true));
 	}
 	
 	private void checkUserChange(UserChange userChange){
 		checkUserChange(userChange, createCopy(), createCopy());
 	}
 	
 	private void checkUserChange(UserChange userChange, GenEditorGenerator old, GenEditorGenerator current) {
 		userChange.applyChanges(old);
 		new Reconciler(userChange.getReconcilerConfig()).reconcileTree(current, old);
 		userChange.assertChangesPreserved(current);		
 	}
 	
 	private static interface UserChange {
 		public void applyChanges(GenEditorGenerator old);
 		public void assertChangesPreserved(GenEditorGenerator current);
 		public ReconcilerConfigBase getReconcilerConfig();
 	}
 
 	private static class LimitedGMFGenConfig extends ReconcilerConfigBase {
 		public LimitedGMFGenConfig(boolean reconcileCopyright){
 			final GMFGenPackage GMFGEN = GMFGenPackage.eINSTANCE;
 
 			setMatcher(GMFGEN.getGenEditorGenerator(), ALWAYS_MATCH);
 			if (reconcileCopyright){
 				preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_CopyrightText());
 			}
 			preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_PackageNamePrefix());
 			preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_DiagramFileExtension());
 			preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_SameFileForDiagramAndModel());
 			
 			setMatcher(GMFGEN.getGenPlugin(), ALWAYS_MATCH); //exactly one feature for ALWAYS_MATCH GenEditorGenerator
 			preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_Provider());
 			preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_Version());
 		}
 		
 		private void preserveIfSet(EClass eClass, EAttribute feature){
 			//FIXME: only attributes for now, allow references
 			addDecision(eClass, new DefaultDecision(feature));
 		}
 	}
 	
 	private abstract static class SingleChange implements UserChange {
 		private final EAttribute myAttribute;
 		private final Object myValueToSet;
 		private Object myExpectedValue;
 		
 		public SingleChange(EAttribute attribute, boolean valueToSet){
 			this(attribute, Boolean.valueOf(valueToSet));
 		}
 
 		public SingleChange(EAttribute attribute, Object valueToSet){
 			myAttribute = attribute;
 			myValueToSet = valueToSet;
 		}
 		
 		protected abstract EObject findChangeSubject(GenEditorGenerator root);
 		
 		public void applyChanges(GenEditorGenerator old) {
 			EObject subject = findChangeSubject(old);
 			assertNotNull(subject);
 			subject.eSet(myAttribute, myValueToSet);
 			myExpectedValue = subject.eGet(myAttribute);
 		}
 		
 		public void assertChangesPreserved(GenEditorGenerator current) {
 			EObject subject = findChangeSubject(current);
 			assertNotNull(subject);
 			assertEquals(myExpectedValue, subject.eGet(myAttribute));
 		}
 		
 		public ReconcilerConfigBase getReconcilerConfig() {
 			return new GMFGenConfig();
 		}
 	}
 	
 	
 }
