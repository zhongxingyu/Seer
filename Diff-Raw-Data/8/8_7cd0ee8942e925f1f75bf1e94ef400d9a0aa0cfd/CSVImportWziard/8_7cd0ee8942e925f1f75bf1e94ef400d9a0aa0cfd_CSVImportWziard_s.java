 package org.eclipse.rmf.reqif10.csv.importer.ui.wizards;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.ui.util.EditUIUtil;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.rmf.reqif10.csv.importer.mapping.MappingLibrary;
 import org.eclipse.rmf.reqif10.csv.importer.ui.CSVImporterUIPlugin;
 import org.eclipse.rmf.reqif10.csv.importer.ui.wizards.pages.CSVImportWizardPage;
 import org.eclipse.rmf.reqif10.csv.importer.ui.wizards.pages.CSVMappingWizardPage;
 import org.eclipse.rmf.reqif10.csv.importer.utils.Importer;
 import org.eclipse.rmf.reqif10.csv.importer.utils.Utils;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.Reqif10Editor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 public class CSVImportWziard extends Wizard implements IImportWizard {
 
 	private CSVImportWizardPage importWizardPage;
 	private CSVMappingWizardPage mappingWizardPage;
 
 	public CSVImportWziard() {
 		setWindowTitle("CSV Import Wizard");
 		setNeedsProgressMonitor(true);
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		importWizardPage = new CSVImportWizardPage();
 		mappingWizardPage = new CSVMappingWizardPage();
 	}
 
 	@Override
 	public IWizardPage getNextPage(IWizardPage page) {
 		if (page == importWizardPage) {
 			IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
 
 				@Override
 				public void run(IProgressMonitor monitor)
 						throws InvocationTargetException, InterruptedException {
 					try {
 						final List<String> columnIDS = Utils.getColumnIds(
 								importWizardPage.getSelectedFilePath(),
 								importWizardPage.getSeparator(),
 								importWizardPage.isContainsHeader());
 						getShell().getDisplay().asyncExec(new Runnable() {
 
 							@Override
 							public void run() {
 								mappingWizardPage
 										.setColumnMappingInput(columnIDS);
 							}
 						});
 					} catch (IOException e) {
 						CSVImporterUIPlugin.INSTANCE.log(e);
 					}
 				}
 			};
 			run(runnableWithProgress);
 		}
 		return super.getNextPage(page);
 	}
 
 	@Override
 	public void addPages() {
 		super.addPages();
 		addPage(importWizardPage);
 		addPage(mappingWizardPage);
 	}
 
 	protected void run(IRunnableWithProgress runnableWithProgress) {
 		try {
 			getContainer().run(true, false, runnableWithProgress);
 		} catch (InvocationTargetException e) {
 			CSVImporterUIPlugin.INSTANCE.log(e);
 		} catch (InterruptedException e) {
 			CSVImporterUIPlugin.INSTANCE.log(e);
 		}
 	}
 
 	public static EditingDomain getEditingDomain(URI uri) {
 		return getEditorsURIMap().get(uri);
 	}
 
 	public static Map<URI, EditingDomain> getEditorsURIMap() {
 		Map<URI, EditingDomain> uriMap = new HashMap<URI, EditingDomain>();
 
 		IEditorReference[] editorReferences = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage()
 				.getEditorReferences();
 		for (IEditorReference editorReference : editorReferences) {
 			IEditorPart editorPart = editorReference.getEditor(false);
 			if (editorPart instanceof Reqif10Editor) {
 				try {
 					uriMap.put(EditUIUtil.getURI(editorReference
 							.getEditorInput()),
 							((IEditingDomainProvider) editorPart)
 									.getEditingDomain());
 				} catch (PartInitException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return uriMap;
 	}
 
 	@Override
 	public boolean performFinish() {
 		boolean valid = true;
 		final IFile selectedDestinationFile = importWizardPage
 				.getSelectedDestinationFile();
 		final String selectedFilePath = importWizardPage.getSelectedFilePath();
 		final MappingLibrary mappingLibrary = mappingWizardPage
 				.getMappingLibrary();
 		final char separator = importWizardPage.getSeparator();
 		final boolean containsHeader = importWizardPage.isContainsHeader();
 		final URI uri = URI.createPlatformResourceURI(selectedDestinationFile
 				.getFullPath().toOSString(), true);
 		final EditingDomain editingDomain = getEditingDomain(uri);
 		try {
 			Importer.importReq(editingDomain, selectedDestinationFile,
 					selectedFilePath, mappingLibrary, separator, containsHeader);
		} catch (IOException e) {
 			CSVImporterUIPlugin.getPlugin().log(e);
 			valid = false;
 		}
 
 		return valid;
 	}
 }
