 package org.eclipse.gmf.codegen.templates.editor;
 
 import org.eclipse.gmf.codegen.gmfgen.*;
 import org.eclipse.emf.codegen.ecore.genmodel.*;
 import java.util.*;
 import org.eclipse.gmf.codegen.util.ImportUtil;
 
 public class InitDiagramFileActionGenerator {
  
   protected static String nl;
   public static synchronized InitDiagramFileActionGenerator create(String lineSeparator)
   {
     nl = lineSeparator;
     InitDiagramFileActionGenerator result = new InitDiagramFileActionGenerator();
     nl = null;
     return result;
   }
 
   protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "package ";
   protected final String TEXT_2 = ";" + NL;
   protected final String TEXT_3 = NL + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "public class ";
   protected final String TEXT_4 = " implements IObjectActionDelegate, IInputValidator {" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "    private static final String FILE_EXT = \"";
   protected final String TEXT_5 = "\";" + NL + "    " + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate IWorkbenchPart myPart;" + NL + "\t" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate IFile mySelection;" + NL + "    " + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate Map myLinkVID2EObjectMap = new HashMap();" + NL + "\t" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate Map myEObject2NodeMap = new HashMap();" + NL + "" + NL + "    /**" + NL + "     * @generated" + NL + "     */" + NL + "\tpublic void setActivePart(IAction action, IWorkbenchPart targetPart) {" + NL + "\t\tmyPart = targetPart;" + NL + "\t}" + NL + "\t" + NL + "    /**" + NL + "     * @generated" + NL + "     */" + NL + "\tprivate Shell getShell() {" + NL + "\t\treturn myPart.getSite().getShell();" + NL + "\t}" + NL + "\t" + NL + "    /**" + NL + "     * @generated" + NL + "     */" + NL + "\tpublic void selectionChanged(IAction action, ISelection selection) {" + NL + "\t\tmySelection = null;" + NL + "\t\taction.setEnabled(false);" + NL + "\t\tif (selection instanceof IStructuredSelection == false || selection.isEmpty()) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\tmySelection = (IFile) ((IStructuredSelection) selection).getFirstElement();" + NL + "\t\taction.setEnabled(true);" + NL + "\t}" + NL + "\t" + NL + "    /**" + NL + "     * @generated" + NL + "     */\t" + NL + "\tpublic String isValid(String newText) {" + NL + "\t\tIStatus status = ResourcesPlugin.getWorkspace().validateName(newText, IResource.FILE);" + NL + "\t\tif (!status.isOK()) {" + NL + "\t\t\treturn status.getMessage();" + NL + "\t\t}" + NL + "\t\tif (mySelection.getParent().getFile(new Path(newText).addFileExtension(FILE_EXT)).exists()) {" + NL + "\t\t\treturn \"File already exists, choose another name\";" + NL + "\t\t}" + NL + "\t\treturn null;" + NL + "\t}" + NL + "\t" + NL + "    /**" + NL + "     * @generated" + NL + "     */" + NL + "\tpublic void run(IAction action) {" + NL + "\t\tfinal InputDialog outputFileNameDialog = new InputDialog(getShell(), \"Diagram file name\", \"Please provide diagram file name\", mySelection.getProjectRelativePath().removeFileExtension().addFileExtension(FILE_EXT).lastSegment(), this);" + NL + "\t\tif (outputFileNameDialog.open() != InputDialog.OK) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\tfinal EObject diagramModelObject = load();" + NL + "\t\tif (diagramModelObject == null) {" + NL + "\t\t\tMessageDialog.openError(getShell(), \"Error\", \"Failed to load user model\");" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\tOperationUtil.runAsUnchecked(new MRunnable() {" + NL + "\t\t\tpublic Object run() {" + NL + "\t\t\t\tEObject diagram = create(diagramModelObject);" + NL + "\t\t\t\tif (diagram == null) {" + NL + "\t\t\t\t\tMessageDialog.openError(getShell(), \"Error\", \"Failed to create diagram object\");" + NL + "\t\t\t\t\treturn null;" + NL + "\t\t\t\t}" + NL + "\t\t\t\tIFile destFile = mySelection.getParent().getFile(new Path(outputFileNameDialog.getValue()));" + NL + "\t\t\t\tsave(destFile.getLocation().toOSString(), diagram);" + NL + "\t\t\t\ttry {" + NL + "\t\t\t\t\tIDE.openEditor(myPart.getSite().getPage(), destFile);" + NL + "\t\t\t\t} catch (PartInitException ex) {" + NL + "\t\t\t\t\tex.printStackTrace();" + NL + "\t\t\t\t}" + NL + "\t\t\t\treturn null;" + NL + "\t\t\t}" + NL + "\t\t});" + NL + "\t}" + NL + "\t" + NL + "    /**" + NL + "     * @generated" + NL + "     */" + NL + "\tprivate EObject load() {";
   protected final String TEXT_6 = NL + "\t\t";
   protected final String TEXT_7 = " resourceSet = new ";
   protected final String TEXT_8 = "();" + NL + "\t\tResource resource = resourceSet.getResource(";
   protected final String TEXT_9 = ".createPlatformResourceURI(mySelection.getFullPath().toString()), true);" + NL + "\t\ttry {" + NL + "\t\t\tresource.load(Collections.EMPTY_MAP);" + NL + "\t\t\treturn (EObject) resource.getContents().get(0);" + NL + "\t\t} catch (IOException ex) {" + NL + "\t\t\tex.printStackTrace();" + NL + "\t\t}" + NL + "\t\treturn null;";
   protected final String TEXT_10 = NL + "\t\tString resourcePath = mySelection.getLocation().toOSString();" + NL + "\t\tResource modelResource = ResourceUtil.findResource(resourcePath);" + NL + "\t\tif (modelResource == null) {" + NL + "\t\t\tmodelResource = ResourceUtil.create(resourcePath);" + NL + "\t\t}" + NL + "\t\tif (!modelResource.isLoaded()) {" + NL + "\t\t\ttry {" + NL + "\t\t\t\tResourceUtil.load(modelResource);" + NL + "\t\t\t} catch (Exception e) {" + NL + "\t\t\t\te.printStackTrace();" + NL + "\t\t\t\treturn null;" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t\treturn (EObject) modelResource.getContents().get(0);";
   protected final String TEXT_11 = NL + "\t}" + NL + "\t" + NL + "    /**" + NL + "     * @generated" + NL + "     */" + NL + "\tprivate void save(String filePath, EObject canvas) {" + NL + "\t\tResource resource = ResourceUtil.create(filePath, null);";
   protected final String TEXT_12 = NL + "\t\tresource.getContents().add(((Diagram) canvas).getElement());";
   protected final String TEXT_13 = NL + "\t\tresource.getContents().add(canvas);" + NL + "\t\ttry {" + NL + "\t\t\tresource.save(Collections.EMPTY_MAP);" + NL + "\t\t} catch (IOException ex) {" + NL + "\t\t\tex.printStackTrace();" + NL + "\t\t}" + NL + "\t}" + NL + "\t" + NL + "    /**" + NL + "     * @generated" + NL + "     */" + NL + "\tprivate EObject create(EObject diagramModel) {" + NL + "\t\tint diagramVID = ";
   protected final String TEXT_14 = ".INSTANCE.getDiagramVisualID(diagramModel);" + NL + "\t\tif (diagramVID != ";
   protected final String TEXT_15 = ") {" + NL + "\t\t\treturn null;" + NL + "\t\t}";
   protected final String TEXT_16 = NL + "\t\tmyLinkVID2EObjectMap.put(new Integer(";
   protected final String TEXT_17 = "), new ";
   protected final String TEXT_18 = "());";
   protected final String TEXT_19 = NL + "\t\tDiagram diagram = ViewService.createDiagram(diagramModel, \"";
   protected final String TEXT_20 = "\", ";
   protected final String TEXT_21 = ".DIAGRAM_PREFERENCES_HINT);" + NL + "\t\tcreate";
   protected final String TEXT_22 = "Children(diagram, diagramModel);" + NL + "\t\tcreateLinks();" + NL + "\t\tmyLinkVID2EObjectMap.clear();" + NL + "\t\tmyEObject2NodeMap.clear();\t\t" + NL + "\t    return diagram;" + NL + "\t}" + NL + "\t";
   protected final String TEXT_23 = "\t" + NL + "" + NL + "    /**" + NL + "     * @generated" + NL + "     */" + NL + "\tprivate void create";
   protected final String TEXT_24 = "Children(";
   protected final String TEXT_25 = " viewObject, EObject modelObject) {";
   protected final String TEXT_26 = NL + "\t\tEObject nextValue;" + NL + "\t\t";
  protected final String TEXT_27 = " nextNode;" + NL + "\t\tint nodeVID;";
   protected final String TEXT_28 = NL + "\t\t";
   protected final String TEXT_29 = " nextNode;";
   protected final String TEXT_30 = NL + "\t\tfor (";
   protected final String TEXT_31 = " values = ";
   protected final String TEXT_32 = ".iterator(); values.hasNext();) {" + NL + "\t\t\tnextValue = (EObject) values.next();" + NL;
   protected final String TEXT_33 = NL + "\t\tnextValue = ";
   protected final String TEXT_34 = ";";
  protected final String TEXT_35 = NL + "\t\tnodeVID = ";
   protected final String TEXT_36 = ".INSTANCE.getNodeVisualID(viewObject, nextValue, \"\");" + NL + "\t\tif (";
   protected final String TEXT_37 = " == nodeVID) {" + NL + "\t\t\tnextNode = ViewService.createNode(viewObject, nextValue, null, ";
   protected final String TEXT_38 = ".DIAGRAM_PREFERENCES_HINT);" + NL + "\t\t\tmyEObject2NodeMap.put(nextValue, nextNode);" + NL + "\t\t\tcreate";
   protected final String TEXT_39 = "Children(nextNode, nextValue);" + NL + "\t\t}";
   protected final String TEXT_40 = NL + "\t\t}";
   protected final String TEXT_41 = NL + "\t\tnextNode = getCompartment(viewObject, \"";
   protected final String TEXT_42 = "\");" + NL + "\t\tif (nextNode != null) {" + NL + "\t\t\tcreate";
   protected final String TEXT_43 = "Children(nextNode, modelObject);" + NL + "\t\t}";
   protected final String TEXT_44 = NL + "\t\tstoreLinks(modelObject);" + NL + "\t}";
   protected final String TEXT_45 = NL + "\t" + NL + "    /**" + NL + "     * @generated" + NL + "     */" + NL + "\tprivate ";
   protected final String TEXT_46 = " getCompartment(";
   protected final String TEXT_47 = " node, String name) {" + NL + "\t\tfor (";
   protected final String TEXT_48 = " it = node.getChildren().iterator(); it.hasNext();) {" + NL + "\t\t\t";
   protected final String TEXT_49 = " nextView = (";
   protected final String TEXT_50 = ") it.next();" + NL + "\t\t\tif (nextView instanceof ";
   protected final String TEXT_51 = " && name.equals(nextView.getType())) {" + NL + "\t\t\t\treturn (";
   protected final String TEXT_52 = ") nextView;" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t\treturn null;" + NL + "\t}";
   protected final String TEXT_53 = NL + NL + "\t/**" + NL + "\t *@generated" + NL + "\t */" + NL + "\tprivate void storeLinks(EObject container) {" + NL + "\t\tEClass containerMetaclass = container.eClass();" + NL + "\t\tstoreFeatureModelFacetLinks(container, containerMetaclass);" + NL + "\t\tstoreTypeModelFacetLinks(container, containerMetaclass);" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL + "     * @generated" + NL + "     */" + NL + "\tprivate void storeTypeModelFacetLinks(EObject container, EClass containerMetaclass) {\t";
   protected final String TEXT_54 = "\t\t" + NL + "\t\tif (-1 != containerMetaclass.getFeatureID(";
   protected final String TEXT_55 = ".eINSTANCE.get";
   protected final String TEXT_56 = "())) {" + NL + "\t\t\tObject featureValue = ";
   protected final String TEXT_57 = ";";
   protected final String TEXT_58 = "\t\t" + NL + "\t\t\tfor (";
   protected final String TEXT_59 = " values = ((";
   protected final String TEXT_60 = ") featureValue).iterator(); values.hasNext();) {" + NL + "\t\t\t\tEObject nextValue = ((EObject) values.next());";
   protected final String TEXT_61 = NL + "\t\t\tEObject nextValue = (EObject) featureValue;";
   protected final String TEXT_62 = NL + "\t\t\tint linkVID = ";
   protected final String TEXT_63 = ".INSTANCE.getLinkWithClassVisualID(nextValue);" + NL + "\t\t\tif (";
   protected final String TEXT_64 = " == linkVID) {" + NL + "\t\t\t\t((";
   protected final String TEXT_65 = ") myLinkVID2EObjectMap.get(new Integer(";
   protected final String TEXT_66 = "))).add(nextValue);" + NL + "\t\t\t}";
   protected final String TEXT_67 = NL + "\t\t\t}";
   protected final String TEXT_68 = NL + "\t\t}";
   protected final String TEXT_69 = NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t *@generated" + NL + "\t */" + NL + "\tprivate void storeFeatureModelFacetLinks(EObject container, EClass containerMetaclass) {";
   protected final String TEXT_70 = NL + "\t\tif (-1 != containerMetaclass.getFeatureID(";
   protected final String TEXT_71 = ".eINSTANCE.get";
   protected final String TEXT_72 = "())) {" + NL + "\t\t\t((";
   protected final String TEXT_73 = ") myLinkVID2EObjectMap.get(new Integer(";
   protected final String TEXT_74 = "))).add(container);" + NL + "\t\t}";
   protected final String TEXT_75 = NL + "\t}" + NL + "" + NL + "\t/**" + NL + "     * @generated" + NL + "     */" + NL + "\tprivate void createLinks() {";
   protected final String TEXT_76 = NL + "\t\t";
   protected final String TEXT_77 = " linkElements;";
   protected final String TEXT_78 = NL + "\t\tlinkElements = (";
   protected final String TEXT_79 = ") myLinkVID2EObjectMap.get(new Integer(";
   protected final String TEXT_80 = "));" + NL + "\t\tfor (";
   protected final String TEXT_81 = " it = linkElements.iterator(); it.hasNext();) {" + NL + "\t\t\tEObject linkElement = (EObject) it.next();";
   protected final String TEXT_82 = NL + "\t\t\tObject srcResult = ";
   protected final String TEXT_83 = ";" + NL + "\t\t\tif (srcResult instanceof EObject == false) {" + NL + "\t\t\t\tcontinue;" + NL + "\t\t\t}" + NL + "\t\t\tEObject src = (EObject) srcResult;";
   protected final String TEXT_84 = NL + "\t\t\tEObject src = linkElement.eContainer();";
   protected final String TEXT_85 = NL + "\t\t\tEObject src = linkElement;";
   protected final String TEXT_86 = NL + "\t\t\t";
   protected final String TEXT_87 = " srcNode = (";
   protected final String TEXT_88 = ") myEObject2NodeMap.get(src);" + NL + "\t\t\tif (srcNode == null) {" + NL + "\t\t\t\tcontinue;" + NL + "\t\t\t}" + NL + "\t\t\tObject structuralFeatureResult = ";
   protected final String TEXT_89 = ";";
   protected final String TEXT_90 = NL + "\t\t\tif (structuralFeatureResult instanceof EObject == false) {" + NL + "\t\t\t\tcontinue;" + NL + "\t\t\t}" + NL + "\t\t\tEObject dst = (EObject) structuralFeatureResult;";
   protected final String TEXT_91 = NL + "\t\t\tif (structuralFeatureResult instanceof ";
   protected final String TEXT_92 = " == false) {" + NL + "\t\t\t\tcontinue;" + NL + "\t\t\t}" + NL + "\t\t\tfor (";
   protected final String TEXT_93 = " destinations = ((";
   protected final String TEXT_94 = ") structuralFeatureResult).iterator(); destinations.hasNext();) {" + NL + "\t\t\t\tEObject dst = (EObject) destinations.next();";
   protected final String TEXT_95 = NL + "\t\t\tif (structuralFeatureResult instanceof EObject == false) {" + NL + "\t\t\t\tcontinue;" + NL + "\t\t\t}" + NL + "\t\t\tEObject dst = (EObject) structuralFeatureResult;";
   protected final String TEXT_96 = NL + "\t\t\t";
   protected final String TEXT_97 = " dstNode = (";
   protected final String TEXT_98 = ") myEObject2NodeMap.get(dst);" + NL + "\t\t\tif (dstNode != null) {";
   protected final String TEXT_99 = NL + "\t\t\t\tViewService.createEdge(srcNode, dstNode, linkElement, null, ";
   protected final String TEXT_100 = ".DIAGRAM_PREFERENCES_HINT);" + NL + "\t\t\t}";
   protected final String TEXT_101 = NL + "\t\t\t\t\t";
   protected final String TEXT_102 = " edge = (";
   protected final String TEXT_103 = ") ViewService.getInstance().createEdge(new ";
   protected final String TEXT_104 = "() {" + NL + "\t\t\t\t\t\tpublic Object getAdapter(Class adapter) {" + NL + "\t\t\t\t\t\t\tif (";
   protected final String TEXT_105 = ".class.equals(adapter)) {" + NL + "\t\t\t\t\t\t\t\treturn ";
   protected final String TEXT_106 = ".";
   protected final String TEXT_107 = ";" + NL + "\t\t\t\t\t\t\t}" + NL + "\t\t\t\t\t\t\treturn null;" + NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t}, srcNode.getDiagram(), \"\", ";
   protected final String TEXT_108 = ".APPEND, ";
   protected final String TEXT_109 = ".DIAGRAM_PREFERENCES_HINT);" + NL + "\t\t\t\t\tif (edge != null) {" + NL + "\t\t\t\t\t\tedge.setSource(srcNode);" + NL + "\t\t\t\t\t\tedge.setTarget(dstNode);" + NL + "\t\t\t\t\t}";
   protected final String TEXT_110 = NL + "\t\t\t\t}";
   protected final String TEXT_111 = NL + "\t\t\t}";
   protected final String TEXT_112 = NL + "\t\t}";
   protected final String TEXT_113 = NL + "\t}" + NL + "\t" + NL + "}";
   protected final String TEXT_114 = NL;
 
 	protected final String getFeatureValueGetter(String containerName, GenFeature feature, boolean isContainerEObject, ImportUtil importManager) {
 		StringBuffer result = new StringBuffer();
 		if (feature.getGenClass().isExternalInterface()) {
 // Using EMF reflective method to access feature value
 			result.append("((");
 			if (feature.isListType()) {
 				result.append(importManager.getImportedName("java.util.Collection"));
 			} else {
 				result.append(importManager.getImportedName(feature.getTypeGenClass().getQualifiedInterfaceName()));
 			}
 			result.append(")");
 			if (!isContainerEObject) {
 // Casting container to EObject - ExternalIntarfce could be not an instance of EObject
 				result.append("((");
 				result.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
 				result.append(")");
 			}
 			result.append(containerName);
 			if (!isContainerEObject) {
 				result.append(")");
 			}
 			result.append(".eGet(");
 			result.append(importManager.getImportedName(feature.getGenPackage().getQualifiedPackageInterfaceName()));
 			result.append(".eINSTANCE.get");
 			result.append(feature.getFeatureAccessorName());
 			result.append("()))");
 		} else {
 			if (isContainerEObject) {
 // Casting container to the typed interface
 				result.append("((");
 				result.append(importManager.getImportedName(feature.getGenClass().getQualifiedInterfaceName()));
 				result.append(")");
 			}
 			result.append(containerName);
 			if (isContainerEObject) {
 				result.append(")");
 			}
 			result.append(".");
 			result.append(feature.getGetAccessor());
 			result.append("()");
 		}
 		return result.toString();
 	}
 	
 	protected final String getFeatureValueSetterPrefix(String containerName, GenFeature feature, boolean isContainerEObject, ImportUtil importManager) {
 		StringBuffer result = new StringBuffer();
 		if (feature.getGenClass().isExternalInterface()) {
 // Using EMF reflective method to access feature value
 			if (!isContainerEObject) {
 // Casting container to EObject - ExternalIntarfce could be not an instance of EObject
 				result.append("((");
 				result.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
 				result.append(")");
 			}
 			result.append(containerName);
 			if (!isContainerEObject) {
 				result.append(")");
 			}
 			result.append(".eSet(");
 			result.append(importManager.getImportedName(feature.getGenPackage().getQualifiedPackageInterfaceName()));
 			result.append(".eINSTANCE.get");
 			result.append(feature.getFeatureAccessorName());
 			result.append("(), ");
 		} else {
 			if (isContainerEObject) {
 // Casting container to the typed interface
 				result.append("((");
 				result.append(importManager.getImportedName(feature.getGenClass().getQualifiedInterfaceName()));
 				result.append(")");
 			}
 			result.append(containerName);
 			if (isContainerEObject) {
 				result.append(")");
 			}
 			if (feature.isListType()) {
 				result.append(".");
 				result.append(feature.getGetAccessor());
 				result.append("().add(");
 			} else {
 				result.append(".set");
 				result.append(feature.getAccessorName());
 				result.append("(");
 			}
 		}
 		return result.toString();
 	}
  
 	protected final String getMetaClassAccessor(GenClass genClass, ImportUtil importManager) {
 		StringBuffer buf = new StringBuffer();
 		buf.append(importManager.getImportedName(genClass.getGenPackage().getQualifiedPackageInterfaceName()))
 			.append(".eINSTANCE.get") //$NON-NLS-1$
 			.append(genClass.getName())
 			.append("()"); //$NON-NLS-1$
 		return buf.toString();
 	} 
  
 	public String generate(Object argument)
   {
     StringBuffer stringBuffer = new StringBuffer();
     
 GenDiagram genDiagram = (GenDiagram) argument;
 List genLinks = genDiagram.getLinks();
 
 boolean generateGetCompartment = false;
 
     stringBuffer.append(TEXT_1);
     stringBuffer.append(genDiagram.getEditorPackageName());
     stringBuffer.append(TEXT_2);
     
 ImportUtil importManager = new ImportUtil(genDiagram.getEditorPackageName());
 
 importManager.addImport("java.io.IOException");
 importManager.addImport("java.util.Collections");
 importManager.addImport("java.util.HashMap");
 importManager.addImport("java.util.Map");
 importManager.addImport("org.eclipse.core.resources.IFile");
 importManager.addImport("org.eclipse.core.resources.IResource");
 importManager.addImport("org.eclipse.core.resources.ResourcesPlugin");
 importManager.addImport("org.eclipse.core.runtime.IStatus");
 importManager.addImport("org.eclipse.core.runtime.Path");
 importManager.addImport("org.eclipse.emf.ecore.EClass");
 importManager.addImport("org.eclipse.emf.ecore.EObject");
 importManager.addImport("org.eclipse.emf.ecore.resource.Resource");
 importManager.addImport("org.eclipse.gmf.runtime.emf.core.edit.MRunnable");
 importManager.addImport("org.eclipse.gmf.runtime.emf.core.util.OperationUtil");
 importManager.addImport("org.eclipse.gmf.runtime.emf.core.util.ResourceUtil");
 importManager.addImport("org.eclipse.gmf.runtime.notation.Diagram");
 importManager.addImport("org.eclipse.jface.action.IAction");
 importManager.addImport("org.eclipse.jface.dialogs.IInputValidator");
 importManager.addImport("org.eclipse.jface.dialogs.InputDialog");
 importManager.addImport("org.eclipse.jface.dialogs.MessageDialog");
 importManager.addImport("org.eclipse.jface.viewers.ISelection");
 importManager.addImport("org.eclipse.jface.viewers.IStructuredSelection");
 importManager.addImport("org.eclipse.swt.widgets.Shell");
 importManager.addImport("org.eclipse.ui.IObjectActionDelegate");
 importManager.addImport("org.eclipse.ui.IWorkbenchPart");
 importManager.addImport("org.eclipse.ui.PartInitException");
 importManager.addImport("org.eclipse.ui.ide.IDE");
 importManager.addImport("org.eclipse.gmf.runtime.diagram.core.services.ViewService");
 
 importManager.markImportLocation(stringBuffer);
 
     stringBuffer.append(TEXT_3);
     stringBuffer.append(genDiagram.getInitDiagramFileActionClassName());
     stringBuffer.append(TEXT_4);
     stringBuffer.append(genDiagram.getDiagramFileExtension());
     stringBuffer.append(TEXT_5);
     if (genDiagram.isSameFileForDiagramAndModel()) {
     stringBuffer.append(TEXT_6);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.resource.ResourceSet"));
     stringBuffer.append(TEXT_7);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.resource.impl.ResourceSetImpl"));
     stringBuffer.append(TEXT_8);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.util.URI"));
     stringBuffer.append(TEXT_9);
     } else {
     stringBuffer.append(TEXT_10);
     }
     stringBuffer.append(TEXT_11);
     if (genDiagram.isSameFileForDiagramAndModel()) {
     stringBuffer.append(TEXT_12);
     }
     stringBuffer.append(TEXT_13);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_14);
     stringBuffer.append(genDiagram.getVisualID());
     stringBuffer.append(TEXT_15);
     
 for (Iterator it = genLinks.iterator(); it.hasNext();) {
 	GenLink nextLink = (GenLink) it.next();
 
     stringBuffer.append(TEXT_16);
     stringBuffer.append(nextLink.getVisualID());
     stringBuffer.append(TEXT_17);
     stringBuffer.append(importManager.getImportedName("java.util.LinkedList"));
     stringBuffer.append(TEXT_18);
     
 }
 
     stringBuffer.append(TEXT_19);
     stringBuffer.append(genDiagram.getEMFGenModel().getModelName());
     stringBuffer.append(TEXT_20);
     stringBuffer.append(importManager.getImportedName(genDiagram.getPluginQualifiedClassName()));
     stringBuffer.append(TEXT_21);
     stringBuffer.append(genDiagram.getUniqueIdentifier());
     stringBuffer.append(TEXT_22);
     
 for (Iterator containers = genDiagram.getAllContainers().iterator(); containers.hasNext();) {
 	GenContainerBase nextContainer = (GenContainerBase) containers.next();
 
     stringBuffer.append(TEXT_23);
     stringBuffer.append(nextContainer.getUniqueIdentifier());
     stringBuffer.append(TEXT_24);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_25);
     
 	if (nextContainer.getContainedNodes().size() > 0) {
 
     stringBuffer.append(TEXT_26);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_27);
     
 	} else if (nextContainer instanceof GenNode && ((GenNode) nextContainer).getCompartments().size() > 0) {
 
     stringBuffer.append(TEXT_28);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_29);
     
 	}
 	
 	for (Iterator children = nextContainer.getContainedNodes().iterator(); children.hasNext();) {
 		GenNode nextChild = (GenNode) children.next();
 		TypeModelFacet typeModelFacet = nextChild.getModelFacet();
 		GenFeature childMetaFeature = typeModelFacet.getChildMetaFeature();
 		if (childMetaFeature.isListType()) {
 
     stringBuffer.append(TEXT_30);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_31);
     stringBuffer.append(getFeatureValueGetter("modelObject", childMetaFeature, true, importManager));
     stringBuffer.append(TEXT_32);
     
 		} else {
 
     stringBuffer.append(TEXT_33);
     stringBuffer.append(getFeatureValueGetter("modelObject", childMetaFeature, true, importManager));
     stringBuffer.append(TEXT_34);
     
 		}
 
     stringBuffer.append(TEXT_35);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_36);
     stringBuffer.append(nextChild.getVisualID());
     stringBuffer.append(TEXT_37);
     stringBuffer.append(importManager.getImportedName(genDiagram.getPluginQualifiedClassName()));
     stringBuffer.append(TEXT_38);
     stringBuffer.append(nextChild.getUniqueIdentifier());
     stringBuffer.append(TEXT_39);
     
 		if (childMetaFeature.isListType()) {
 
     stringBuffer.append(TEXT_40);
     
 		}
 	}
 	
 	if (nextContainer instanceof GenNode) {
 		GenNode nextNode = (GenNode) nextContainer;
 		for (Iterator compartments = nextNode.getCompartments().iterator(); compartments.hasNext();) {
 			GenCompartment nextCompartment = (GenCompartment) compartments.next();
 			generateGetCompartment = true;
 
     stringBuffer.append(TEXT_41);
     stringBuffer.append(nextCompartment.getTitle());
     stringBuffer.append(TEXT_42);
     stringBuffer.append(nextCompartment.getUniqueIdentifier());
     stringBuffer.append(TEXT_43);
     
 		}
 	}
 
     stringBuffer.append(TEXT_44);
     
 }
 
 if (generateGetCompartment) {
 
     stringBuffer.append(TEXT_45);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_46);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_47);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_48);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_49);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_50);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_51);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_52);
     
 }
 
     stringBuffer.append(TEXT_53);
     
 for (Iterator it = genLinks.iterator(); it.hasNext();) {
 	GenLink nextLink = (GenLink) it.next();
 	if (nextLink.getModelFacet() instanceof TypeLinkModelFacet) {
 		TypeLinkModelFacet typeLinkModelFacet = (TypeLinkModelFacet) nextLink.getModelFacet();
 		GenFeature childMetaFeature = typeLinkModelFacet.getChildMetaFeature();
 
     stringBuffer.append(TEXT_54);
     stringBuffer.append(importManager.getImportedName(childMetaFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_55);
     stringBuffer.append(childMetaFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_56);
     stringBuffer.append(getFeatureValueGetter("container", childMetaFeature, true, importManager));
     stringBuffer.append(TEXT_57);
     
 		if (childMetaFeature.isListType()) {
 
     stringBuffer.append(TEXT_58);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_59);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_60);
     
 		} else {
 
     stringBuffer.append(TEXT_61);
     		
 		}
 
     stringBuffer.append(TEXT_62);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_63);
     stringBuffer.append(nextLink.getVisualID());
     stringBuffer.append(TEXT_64);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_65);
     stringBuffer.append(nextLink.getVisualID());
     stringBuffer.append(TEXT_66);
     
 		if (childMetaFeature.isListType()) {
 
     stringBuffer.append(TEXT_67);
     
 		}
 
     stringBuffer.append(TEXT_68);
     
 	}
 }
 
     stringBuffer.append(TEXT_69);
     
 for (Iterator it = genLinks.iterator(); it.hasNext();) {
 	GenLink nextLink = (GenLink) it.next();
 	if (nextLink.getModelFacet() instanceof FeatureModelFacet) {
 		GenFeature genFeature = ((FeatureModelFacet) nextLink.getModelFacet()).getMetaFeature();
 
     stringBuffer.append(TEXT_70);
     stringBuffer.append(importManager.getImportedName(genFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_71);
     stringBuffer.append(genFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_72);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_73);
     stringBuffer.append(nextLink.getVisualID());
     stringBuffer.append(TEXT_74);
     
 	}
 }
 
     stringBuffer.append(TEXT_75);
     
 if (genLinks.size() > 0) {
 
     stringBuffer.append(TEXT_76);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_77);
     
 }
 
 for (Iterator it = genLinks.iterator(); it.hasNext();) {
 	GenLink nextLink = (GenLink) it.next();
 	GenFeature domainLinkTargetGenFeature;
 
     stringBuffer.append(TEXT_78);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_79);
     stringBuffer.append(nextLink.getVisualID());
     stringBuffer.append(TEXT_80);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_81);
     	
 	if (nextLink.getModelFacet() instanceof TypeLinkModelFacet) {
 		TypeLinkModelFacet typeLinkModelFacet = (TypeLinkModelFacet) nextLink.getModelFacet();
 		domainLinkTargetGenFeature = typeLinkModelFacet.getTargetMetaFeature();
 		if (typeLinkModelFacet.getSourceMetaFeature() != null) {
 
     stringBuffer.append(TEXT_82);
     stringBuffer.append(getFeatureValueGetter("linkElement", typeLinkModelFacet.getSourceMetaFeature(), true, importManager));
     stringBuffer.append(TEXT_83);
     
 		} else {
 
     stringBuffer.append(TEXT_84);
     
 		}
 	} else {
 		domainLinkTargetGenFeature = ((FeatureModelFacet) nextLink.getModelFacet()).getMetaFeature();
 
     stringBuffer.append(TEXT_85);
     	
 	}
 
     stringBuffer.append(TEXT_86);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_87);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_88);
     stringBuffer.append(getFeatureValueGetter("linkElement", domainLinkTargetGenFeature, true, importManager));
     stringBuffer.append(TEXT_89);
     
 	if (nextLink.getModelFacet() instanceof TypeLinkModelFacet) {
 
     stringBuffer.append(TEXT_90);
     
 	} else {
 		if (domainLinkTargetGenFeature.isListType()) {
 
     stringBuffer.append(TEXT_91);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_92);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_93);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_94);
     	
 		} else {
 
     stringBuffer.append(TEXT_95);
     
 		}
 	}
 
     stringBuffer.append(TEXT_96);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_97);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_98);
     
 	if (nextLink.getModelFacet() instanceof TypeLinkModelFacet) {
 
     stringBuffer.append(TEXT_99);
     stringBuffer.append(importManager.getImportedName(genDiagram.getPluginQualifiedClassName()));
     stringBuffer.append(TEXT_100);
     
 	} else {
 
     stringBuffer.append(TEXT_101);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_102);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_103);
     stringBuffer.append(importManager.getImportedName("org.eclipse.core.runtime.IAdaptable"));
     stringBuffer.append(TEXT_104);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.emf.type.core.IElementType"));
     stringBuffer.append(TEXT_105);
     stringBuffer.append(importManager.getImportedName(genDiagram.getElementTypesQualifiedClassName()));
     stringBuffer.append(TEXT_106);
     stringBuffer.append(nextLink.getUniqueIdentifier());
     stringBuffer.append(TEXT_107);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.diagram.core.util.ViewUtil"));
     stringBuffer.append(TEXT_108);
     stringBuffer.append(importManager.getImportedName(genDiagram.getPluginQualifiedClassName()));
     stringBuffer.append(TEXT_109);
     
 		if (domainLinkTargetGenFeature.isListType()) {
 
     stringBuffer.append(TEXT_110);
     
 }
 
     stringBuffer.append(TEXT_111);
     	
 	}
 
     stringBuffer.append(TEXT_112);
     
 }
 
     stringBuffer.append(TEXT_113);
     importManager.emitSortedImports();
     stringBuffer.append(TEXT_114);
     return stringBuffer.toString();
   }
 }
