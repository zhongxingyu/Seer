 package org.eclipse.uml2.diagram.clazz.action;
 
 import java.util.List;
 
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Shell;
import org.eclipse.uml2.diagram.clazz.part.NewDiagramForFixedElementsWizard;
 import org.eclipse.uml2.diagram.clazz.part.UMLDiagramEditorUtil;
 import org.eclipse.uml2.diagram.clazz.part.UMLNewDiagramFileWizard;
 import org.eclipse.uml2.diagram.common.part.NewDiagramHandlerBase;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.PackageableElement;
 
 public class NewDiagramHandler extends NewDiagramHandlerBase {
 
 	@Override
 	protected Wizard getNewDiagramWizard(Package diagramRoot) {
 		return new UMLNewDiagramFileWizard(getDiagramFileURI(diagramRoot), diagramRoot, getEditingDomain());
 	}
 
 	@Override
 	protected Wizard getNewSemiSyncDiagramWizard(Package diagramRoot, List<PackageableElement> selectedElements) {
		return new NewDiagramForFixedElementsWizard(getDiagramFileURI(diagramRoot), diagramRoot, selectedElements.toArray(new PackageableElement[selectedElements.size()]), getEditingDomain());
 	}
 
 	@Override
 	protected void runWizard(Wizard wizard, Shell sh) {
 		UMLDiagramEditorUtil.runWizard(sh, wizard, "InitDiagramFile"); //$NON-NLS-1$
 	}
 
 }
