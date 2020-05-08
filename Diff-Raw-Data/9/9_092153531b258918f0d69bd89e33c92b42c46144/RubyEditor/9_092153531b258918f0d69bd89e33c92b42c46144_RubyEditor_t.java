 package org.eclipse.dltk.ruby.internal.ui.editor;
 
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.internal.ui.actions.FoldingActionGroup;
 import org.eclipse.dltk.internal.ui.editor.DLTKEditorMessages;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
 import org.eclipse.dltk.internal.ui.editor.ScriptOutlinePage;
 import org.eclipse.dltk.internal.ui.editor.ToggleCommentAction;
 import org.eclipse.dltk.ruby.core.RubyLanguageToolkit;
 import org.eclipse.dltk.ruby.internal.ui.RubyUI;
 import org.eclipse.dltk.ruby.internal.ui.text.folding.RubyFoldingStructureProvider;
 import org.eclipse.dltk.ruby.ui.text.IRubyPartitions;
 import org.eclipse.dltk.ui.actions.IDLTKEditorActionDefinitionIds;
 import org.eclipse.dltk.ui.text.TextTools;
 import org.eclipse.dltk.ui.text.folding.IFoldingStructureProvider;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension3;
 import org.eclipse.jface.text.ITextOperationTarget;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.texteditor.TextOperationAction;
 
 public class RubyEditor extends ScriptEditor {
 
 	public static final String EDITOR_ID = "org.eclipse.dltk.ruby.internal.ui.editor.rubyEditor";
 
 	public static final String EDITOR_CONTEXT = "#RubyEditorContext";
 
 	protected void initializeEditor() {
 		super.initializeEditor();
 		setEditorContextMenuId(EDITOR_CONTEXT);
 	}
 
 	protected IPreferenceStore getScriptPreferenceStore() {
 		return RubyUI.getDefault().getPreferenceStore();
 	}
 
 	protected TextTools getTextTools() {
 		return RubyUI.getDefault().getTextTools();
 	}
 
 	protected ScriptOutlinePage doCreateOutlinePage() {
 		return new RubyOutlinePage(this, RubyUI
 				.getDefault().getPreferenceStore());
 	}
 
 	protected void connectPartitioningToElement(IEditorInput input,
 			IDocument document) {
 		if (document instanceof IDocumentExtension3) {
 			IDocumentExtension3 extension = (IDocumentExtension3) document;
 			if (extension
 					.getDocumentPartitioner(IRubyPartitions.RUBY_PARTITIONING) == null) {
 				RubyDocumentSetupParticipant participant = new RubyDocumentSetupParticipant();
 				participant.setup(document);
 			}
 		}
 	}
 
 	private IFoldingStructureProvider fFoldingProvider = null;
 
 	protected IFoldingStructureProvider getFoldingStructureProvider() {
 		if (fFoldingProvider == null) {
 			fFoldingProvider = new RubyFoldingStructureProvider();
 		}
 		return fFoldingProvider;
 	}
 
 	protected FoldingActionGroup createFoldingActionGroup() {
 		return new FoldingActionGroup(this, getViewer(), RubyUI.getDefault()
 				.getPreferenceStore());
 	}
 
 	public String getEditorId() {
 		return EDITOR_ID;
 	}
 
 	public IDLTKLanguageToolkit getLanguageToolkit() {		
 		return RubyLanguageToolkit.getDefault();
 	}
 
 	public String getCallHierarchyID() {
		return "org.eclipse.dltk.callhierarchy.view";
 	}
 
 	protected void initializeKeyBindingScopes() {
 		setKeyBindingScopes(new String[] { "org.eclipse.dltk.ui.rubyEditorScope" });  //$NON-NLS-1$
 	}
 	
 	protected void createActions() {
 		super.createActions();
 		
 		Action action= new TextOperationAction(DLTKEditorMessages.getBundleForConstructedKeys(), "Comment.", this, ITextOperationTarget.PREFIX); //$NON-NLS-1$
 		action.setActionDefinitionId(IDLTKEditorActionDefinitionIds.COMMENT);
 		setAction("Comment", action); //$NON-NLS-1$
 		markAsStateDependentAction("Comment", true); //$NON-NLS-1$
 		
 		action= new TextOperationAction(DLTKEditorMessages.getBundleForConstructedKeys(), "Uncomment.", this, ITextOperationTarget.STRIP_PREFIX); //$NON-NLS-1$
 		action.setActionDefinitionId(IDLTKEditorActionDefinitionIds.UNCOMMENT);
 		setAction("Uncomment", action); //$NON-NLS-1$
 		markAsStateDependentAction("Uncomment", true); //$NON-NLS-1$
 		
 		action= new ToggleCommentAction(DLTKEditorMessages.getBundleForConstructedKeys(), "ToggleComment.", this); //$NON-NLS-1$
 		action.setActionDefinitionId(IDLTKEditorActionDefinitionIds.TOGGLE_COMMENT);
 		setAction("ToggleComment", action); //$NON-NLS-1$
 		markAsStateDependentAction("ToggleComment", true); //$NON-NLS-1$
 		
 		ISourceViewer sourceViewer = getSourceViewer();
 		SourceViewerConfiguration configuration= getSourceViewerConfiguration();
 		((ToggleCommentAction)action).configure(sourceViewer, configuration);
 	}
 }
