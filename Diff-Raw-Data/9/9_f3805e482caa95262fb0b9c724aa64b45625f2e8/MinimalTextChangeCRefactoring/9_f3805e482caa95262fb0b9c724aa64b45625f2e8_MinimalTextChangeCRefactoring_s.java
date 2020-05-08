 package edu.illinois.parallelism.openmp.crefactoring;
 
 import java.io.FileNotFoundException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.cdt.core.dom.ast.IASTNode;
 import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
 import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
 import org.eclipse.cdt.core.model.ICElement;
 import org.eclipse.cdt.core.model.ICProject;
 import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
 import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
 import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
 import org.eclipse.cdt.internal.core.dom.rewrite.ASTRewriteAnalyzer;
 import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
 import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.ChangeGenerator;
 import org.eclipse.cdt.internal.core.resources.ResourceLookup;
 import org.eclipse.cdt.internal.ui.refactoring.CCompositeChange;
 import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
 import org.eclipse.cdt.internal.ui.refactoring.CreateFileChange;
 import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
 import org.eclipse.cdt.ui.refactoring.CTextFileChange;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.text.edits.InsertEdit;
 import org.eclipse.text.edits.MultiTextEdit;
 
 import edu.illinois.parallelism.openmp.refactoring.utilities.Activator;
 
 @SuppressWarnings("restriction")
 public abstract class MinimalTextChangeCRefactoring extends CRefactoring {
 
 	private CCompositeChange finalChanges;
 	private ModificationCollector collector;
 
 	public MinimalTextChangeCRefactoring(IFile file, ISelection selection, ICElement element, ICProject proj) {
 		super(file, selection, element, proj);
		finalChanges = new CCompositeChange("Changes for this C/C++ Refactoring");
		// See how they mark the changes as synthetic in
		// org.eclipse.cdt.internal.ui.refactoring.ModificationCollector.createFinalChange()
		finalChanges.markAsSynthetic();
 	}
 
 	@Override
 	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
 		collector = new ModificationCollector();
 		collectModifications(pm, collector);
 		try {
 			lockIndex();
 			createModifications(pm, collector);
 		} catch (InterruptedException e) {
 			throw new OperationCanceledException();
 		} catch (FileNotFoundException e) {
 			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Trying to refactor a file that does not exist",
 					e));
 		} finally {
 			unlockIndex();
 		}
 
 		return finalChanges;
 	}
 
 	// TODO: Update the progress monitor more precisely
 	private void createModifications(IProgressMonitor pm, ModificationCollector collector) throws FileNotFoundException {
 		handleNewFileChanges(collector);
 		handleASTModificationChanges(collector);
 	}
 
 	private void handleNewFileChanges(ModificationCollector collector) {
 		Collection<CreateFileChange> fileChanges = collector.getFileChanges();
 		if (fileChanges != null)
 			finalChanges.addAll(fileChanges.toArray(new Change[fileChanges.size()]));
 	}
 
 	private void handleASTModificationChanges(ModificationCollector collector) throws FileNotFoundException {
 		Map<IASTTranslationUnit, ASTRewrite> rewriters = collector.getRewriters();
 		for (IASTTranslationUnit unit : rewriters.keySet())
 			handleEachAffectedFile(rewriters, unit);
 	}
 
 	private void handleEachAffectedFile(Map<IASTTranslationUnit, ASTRewrite> rewriters, IASTTranslationUnit unit)
 			throws FileNotFoundException {
 
 		ASTRewrite rewriter = rewriters.get(unit);
 
 		IFile affectedFile = determineAffectedFile(unit);
 		CTextFileChange change = (CTextFileChange) ASTRewriteAnalyzer.createCTextFileChange(affectedFile);
 
 		ASTModificationStore modStore = rewriter.getfModificationStore();
 		ASTModificationMap rootModifications = modStore.getNestedModifications(rewriter.getfParentMod());
 		Collection<IASTNode> modifiedNodes = rootModifications.getModifiedNodes();
 
 		for (IASTNode modifiedNode : modifiedNodes) {
 			handleEachAffectedNode(rewriter, change, modStore, rootModifications, modifiedNode);
 		}
 
 		finalChanges.add(change);
 
 	}
 
 	private void handleEachAffectedNode(ASTRewrite rewriter, CTextFileChange change, ASTModificationStore modStore,
 			ASTModificationMap rootModifications, IASTNode modifiedNode) {
 		MultiTextEdit edit = new MultiTextEdit();
 		List<ASTModification> modificationsForNode = rootModifications.getModificationsForNode(modifiedNode);
 		for (ASTModification astModification : modificationsForNode) {
 			ASTWriter writer = new ASTWriter();
 			writer.setModificationStore(modStore);
 
 			switch (astModification.getKind()) {
 			case INSERT_BEFORE:
 				String result = writer.write(astModification.getNewNode());
 				System.err.println(result);
 				ChangeGenerator gen = new ChangeGenerator(modStore, rewriter.getfCommentMap());
 				int offsetIncludingComments = gen.getOffsetIncludingComments(astModification.getTargetNode());
 				InsertEdit insertion = new InsertEdit(offsetIncludingComments, result);
 				edit.addChild(insertion);
 				break;
 			// TODO: Handle these two cases
 			case APPEND_CHILD:
 			case REPLACE:
 				break;
 			default:
 				break;
 			}
 		}
 
 		collectTextChanges(change, edit);
 	}
 
 	private void collectTextChanges(CTextFileChange change, MultiTextEdit edit) {
 		if (change.getEdit() == null)
 			change.setEdit(edit);
 		else
 			change.addEdit(edit);
 	}
 
 	private IFile determineAffectedFile(IASTTranslationUnit unit) throws FileNotFoundException {
 		String currentFile = unit.getFileLocation().getFileName();
 		IPath implPath = new Path(currentFile);
 		IFile relevantFile = ResourceLookup.selectFileForLocation(implPath, null);
 		if (relevantFile == null || !relevantFile.exists()) {
 			throw new FileNotFoundException();
 		}
 		return relevantFile;
 	}
 }
