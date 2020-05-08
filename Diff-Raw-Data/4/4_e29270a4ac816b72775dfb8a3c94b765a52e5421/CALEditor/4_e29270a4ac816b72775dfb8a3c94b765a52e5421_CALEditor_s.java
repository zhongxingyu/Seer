 /*******************************************************************************
  * Copyright (c) 2006 Business Objects Software Limited and others.
  * All rights reserved. 
  * This file is made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Business Objects Software Limited - initial API and implementation
  *******************************************************************************/
 
 /*
  * CALEditor.java
  * Creation date: Jan 27, 2006.
  * By: Edward Lam
  */
 package org.openquark.cal.eclipse.ui.caleditor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.text.BreakIterator;
 import java.text.CharacterIterator;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Stack;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFileModificationValidator;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourceAttributes;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.internal.core.JarEntryFile;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IContributionManagerOverrides;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.BadPositionCategoryException;
 import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
 import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.DefaultLineTracker;
 import org.eclipse.jface.text.DocumentCommand;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.DocumentRewriteSession;
 import org.eclipse.jface.text.DocumentRewriteSessionType;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.ILineTracker;
 import org.eclipse.jface.text.IPositionUpdater;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextOperationTarget;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.ITextViewerExtension;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.IWidgetTokenKeeper;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.text.formatter.IFormattingContext;
 import org.eclipse.jface.text.information.IInformationPresenter;
 import org.eclipse.jface.text.information.InformationPresenter;
 import org.eclipse.jface.text.link.ILinkedModeListener;
 import org.eclipse.jface.text.link.LinkedModeModel;
 import org.eclipse.jface.text.link.LinkedModeUI;
 import org.eclipse.jface.text.link.LinkedPosition;
 import org.eclipse.jface.text.link.LinkedPositionGroup;
 import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
 import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
 import org.eclipse.jface.text.reconciler.IReconciler;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.IAnnotationAccessExtension;
 import org.eclipse.jface.text.source.IOverviewRuler;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.ISourceViewerExtension2;
 import org.eclipse.jface.text.source.IVerticalRuler;
 import org.eclipse.jface.text.source.IVerticalRulerInfo;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.jface.text.source.projection.ProjectionSupport;
 import org.eclipse.jface.text.source.projection.ProjectionViewer;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.DecoratingLabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.search.ui.ISearchPageContainer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ST;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.team.core.RepositoryProvider;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.IWindowListener;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.XMLMemento;
 import org.eclipse.ui.dialogs.SaveAsDialog;
 import org.eclipse.ui.editors.text.DefaultEncodingSupport;
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.editors.text.IEncodingSupport;
 import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
 import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
 import org.eclipse.ui.texteditor.ChainedPreferenceStore;
 import org.eclipse.ui.texteditor.ContentAssistAction;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.ui.texteditor.ITextEditorActionConstants;
 import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
 import org.eclipse.ui.texteditor.ITextEditorExtension;
 import org.eclipse.ui.texteditor.IUpdate;
 import org.eclipse.ui.texteditor.SelectMarkerRulerAction;
 import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
 import org.eclipse.ui.texteditor.TextNavigationAction;
 import org.eclipse.ui.texteditor.TextOperationAction;
 import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
 import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.openquark.cal.compiler.ClassInstance;
 import org.openquark.cal.compiler.CompilerMessageLogger;
 import org.openquark.cal.compiler.LanguageInfo;
 import org.openquark.cal.compiler.MessageLogger;
 import org.openquark.cal.compiler.ModuleContainer;
 import org.openquark.cal.compiler.ModuleName;
 import org.openquark.cal.compiler.ModuleTypeInfo;
 import org.openquark.cal.compiler.QualifiedName;
 import org.openquark.cal.compiler.ScopedEntity;
 import org.openquark.cal.compiler.SourceMetricsManager;
 import org.openquark.cal.compiler.SourcePosition;
 import org.openquark.cal.compiler.SourceRange;
 import org.openquark.cal.compiler.ModuleContainer.ISourceManager;
 import org.openquark.cal.compiler.SearchResult.Precise;
 import org.openquark.cal.compiler.SourceModel.FunctionDefn;
 import org.openquark.cal.compiler.SourceModel.FunctionTypeDeclaration;
 import org.openquark.cal.compiler.SourceModel.InstanceDefn;
 import org.openquark.cal.compiler.SourceModel.SourceElement;
 import org.openquark.cal.compiler.SourceModel.TypeClassDefn;
 import org.openquark.cal.compiler.SourceModel.TypeConstructorDefn;
 import org.openquark.cal.compiler.SourceModel.FunctionDefn.Primitive;
 import org.openquark.cal.compiler.SourceModel.TypeClassDefn.ClassMethodDefn;
 import org.openquark.cal.compiler.SourceModel.TypeConstructorDefn.AlgebraicType;
 import org.openquark.cal.compiler.SourceModel.TypeConstructorDefn.AlgebraicType.DataConsDefn;
 import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
 import org.openquark.cal.eclipse.core.CALModelManager;
 import org.openquark.cal.eclipse.core.CoreOptionIDs;
 import org.openquark.cal.eclipse.core.CALModelManager.SourceManagerFactory;
 import org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants;
 import org.openquark.cal.eclipse.core.util.Util;
 import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
 import org.openquark.cal.eclipse.ui.CALHelpContextIds;
 import org.openquark.cal.eclipse.ui.IContextMenuConstants;
 import org.openquark.cal.eclipse.ui.actions.ActionMessages;
 import org.openquark.cal.eclipse.ui.actions.ActionUtilities;
 import org.openquark.cal.eclipse.ui.actions.AddBlockCommentAction;
 import org.openquark.cal.eclipse.ui.actions.CALEditorActionDefinitionIds;
 import org.openquark.cal.eclipse.ui.actions.GeneralActionGroup;
 import org.openquark.cal.eclipse.ui.actions.GenerateActionGroup;
 import org.openquark.cal.eclipse.ui.actions.GotoElementAction;
 import org.openquark.cal.eclipse.ui.actions.IndentAction;
 import org.openquark.cal.eclipse.ui.actions.OpenDeclarationAction;
 import org.openquark.cal.eclipse.ui.actions.QuickSearchInWorkspace;
 import org.openquark.cal.eclipse.ui.actions.RemoveBlockCommentAction;
 import org.openquark.cal.eclipse.ui.actions.RenameAction;
 import org.openquark.cal.eclipse.ui.actions.ShowTooltipDescriptionAction;
 import org.openquark.cal.eclipse.ui.preferences.PreferenceConstants;
 import org.openquark.cal.eclipse.ui.text.CALHeuristicScanner;
 import org.openquark.cal.eclipse.ui.text.CALPairMatcher;
 import org.openquark.cal.eclipse.ui.text.CALPartitions;
 import org.openquark.cal.eclipse.ui.text.CALSourceViewerConfiguration;
 import org.openquark.cal.eclipse.ui.text.CALTextTools;
 import org.openquark.cal.eclipse.ui.text.CALWordIterator;
 import org.openquark.cal.eclipse.ui.text.DocumentCharacterIterator;
 import org.openquark.cal.eclipse.ui.text.HTMLTextPresenter;
 import org.openquark.cal.eclipse.ui.text.PreferencesAdapter;
 import org.openquark.cal.eclipse.ui.text.SmartBackspaceManager;
 import org.openquark.cal.eclipse.ui.text.Symbols;
 import org.openquark.cal.eclipse.ui.util.CodeFormatterUtil;
 import org.openquark.cal.eclipse.ui.util.CoreUtility;
 import org.openquark.cal.eclipse.ui.views.CALModuleContentProvider;
 import org.openquark.cal.eclipse.ui.views.CALWorkspace;
 import org.openquark.cal.eclipse.ui.views.ForeignDecorator;
 import org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider;
 import org.openquark.cal.eclipse.ui.views.ModuleTreeLabelProvider;
 import org.openquark.cal.eclipse.ui.views.OutlineTreeContentProvider;
 import org.openquark.cal.eclipse.ui.views.ProblemMarkerDecorator;
 import org.openquark.cal.eclipse.ui.views.ScopeDecorator;
 import org.openquark.util.Pair;
 import org.openquark.util.UnsafeCast;
 
 
 
 
 /**
  * A CAL-specific text editor.
  * 
  * @author Edward Lam
  */
 public class CALEditor extends AbstractDecoratedTextEditor {
     
     /** Preference key for matching brackets */
     protected final static String MATCHING_BRACKETS = PreferenceConstants.EDITOR_MATCHING_BRACKETS;
     /** Preference key for matching brackets color */
     protected final static String MATCHING_BRACKETS_COLOR = PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;
 
     protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };
 
     /** The information presenter. */
     private InformationPresenter fInformationPresenter;
     /**
      * The internal shell activation listener for updating occurrences.
      */
     private final ActivationListener fActivationListener= new ActivationListener();
 
     private OpenDeclarationAction openDeclarationAction;
     
     /**
      * The editor selection changed listener.
      */
     private EditorSelectionChangedListener fEditorSelectionChangedListener;
     
     /**
      * The encoding support for the editor.
      */
     protected DefaultEncodingSupport encodingSupport;
 
     /**
      * This editor's projection support
      */
     private ProjectionSupport fProjectionSupport;
     
     /** The editor's bracket matcher */
     protected CALPairMatcher fBracketMatcher = new CALPairMatcher(BRACKETS);
     
     /**
      * The folding runner.
      */
     private ToggleFoldingRunner fFoldingRunner;
     
     /** The bracket inserter. */
     private final BracketInserter fBracketInserter = new BracketInserter();
 
     // These variables are used for the outline view.
     private CALOutlinePage outlinePage;
     private ModuleTreeContentProvider moduleTreeContentProvider;
     
     private static final CALModelManager calModelManager = CALModelManager.getCALModelManager();
 
     /** The standard action groups added to the menu */
     private GenerateActionGroup generateActionGroup;
     private GeneralActionGroup refactorActionGroup;
     private GeneralActionGroup searchReferencesActionGroup;
     private GeneralActionGroup searchDeclarationsActionGroup;
 
     /**
      * Listeners to the changes of the current selection of the editor. 
      */
     ListenerList selectedEntitiesListeners = new ListenerList();
 
     /*
      * TODOEL:
      *   This is mostly copied from TextViewer.
      * - Change font from Colors and Fonts preference page
      */
     
     /**
      * Creates a new text editor.
      */
     public CALEditor() {
         
         // TEMP: Assign an auto-indent strategy.
         ISourceViewer sourceViewer = getSourceViewer();
         if (sourceViewer instanceof TextViewer) {
             ((TextViewer)getSourceViewer()).prependAutoEditStrategy(new DefaultIndentLineAutoEditStrategy(), "org.openquark.cal.eclipse.core.calSource");
         }
         
         setDocumentProvider(CALEclipseUIPlugin.getDefault().getCALDocumentProvider());
         setEditorContextMenuId("#CALEditorContext"); //$NON-NLS-1$
         setRulerContextMenuId("#CALRulerContext"); //$NON-NLS-1$
         setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);        // Note: the Java editor installs its own help context (CompilationUnitEditor.java).
 
     }
 
     @Override
     protected void initializeKeyBindingScopes() {
         setKeyBindingScopes(new String[] { "org.openquark.cal.eclipse.ui.calEditorScope" });  //$NON-NLS-1$
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     protected void initializeEditor() {
         IPreferenceStore store = createCombinedPreferenceStore(null);
         setPreferenceStore(store);
         CALTextTools textTools = CALEclipseUIPlugin.getDefault().getCALTextTools();
         setSourceViewerConfiguration(new CALSourceViewerConfiguration(textTools.getColorManager(), store, this, CALPartitions.CAL_PARTITIONING));
     }
 
     public final ISourceViewer getViewer() {
         return getSourceViewer();
     }
     
     /**
      * Creates and returns the preference store for this CAL editor with the given input.
      *
      * @param input The editor input for which to create the preference store
      * @return the preference store for this editor
      */
     private IPreferenceStore createCombinedPreferenceStore(IEditorInput input) {
         List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>(3);
 
         /*
          * TODOEL: Add project scoped preferences:
          */
         // From JavaEditor:
 //        IJavaProject project = EditorUtility.getJavaProject(input);
 //        if (project != null) {
 //            stores.add(new EclipsePreferencesAdapter(new ProjectScope(project.getProject()), JavaCore.PLUGIN_ID));
 //        }
 
         loadState();
         
         stores.add(CALEclipseUIPlugin.getDefault().getPreferenceStore());
         stores.add(new PreferencesAdapter(CALEclipseCorePlugin.getDefault().getPluginPreferences()));
         stores.add(EditorsUI.getPreferenceStore());
 
         return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
     }
 
     private final String section_name = "org.openquark.cal.eclipse.ui.caleditor.CALEditor"; //$NON-NLS-1$
     private final String memento_key = "memento"; //$NON-NLS-1$
     /*
      * @see IWorkbenchPart#dispose()
      */
     @Override
     public void dispose() {
         saveState();
         
         ISourceViewer sourceViewer = getSourceViewer();
         if (sourceViewer instanceof ITextViewerExtension) {
             ((ITextViewerExtension)sourceViewer).removeVerifyKeyListener(fBracketInserter);
         }
         
         if (outlinePage != null){
             outlinePage.dispose();
             selectedEntitiesListeners.remove(outlinePage);
             outlinePage = null;
         }
 
         if (encodingSupport != null) {
             encodingSupport.dispose();
             encodingSupport = null;
         }
         
         super.dispose();
         
         if (fProjectionSupport != null) {
             fProjectionSupport.dispose();
             fProjectionSupport = null;
         }
 
     }
     
     /**
      * Installs the encoding support on the given text editor.
      * <p>
      * Subclasses may override to install their own encoding
      * support or to disable the default encoding support.
      * </p>
      */
     protected void installEncodingSupport() {
         encodingSupport = new DefaultEncodingSupport();
         encodingSupport.initialize(this);
     }
     
     @Override
     protected void performSave(boolean overwrite, IProgressMonitor progressMonitor){
         super.performSave(overwrite, progressMonitor);
         {
             IDocument document = getDocumentProvider().getDocument(getEditorInput());
             if (document instanceof PartiallySynchronizedDocument){
                 PartiallySynchronizedDocument psd = (PartiallySynchronizedDocument) document;
                 psd.wasSaved();
             }
         }
     }
     
     /**
      * The <code>TextEditor</code> implementation of this  <code>AbstractTextEditor</code>
      * method asks the user for the workspace path of a file resource and saves the document there.
      *
      * can only do a saveas if not read only.
      * can only do a saveas if the input if from a file (not a jar)
      *
      * @param progressMonitor the progress monitor to be used
      */
     @Override
     protected void performSaveAs(IProgressMonitor progressMonitor) {
         if (!isEditable()) {
             return;
         }
         IEditorInput input = getEditorInput();
         if (input instanceof IFileEditorInput) {
             return;
         }
 
         Shell shell = getSite().getShell();
         
         SaveAsDialog dialog = new SaveAsDialog(shell);
         
         IFile original = ((IFileEditorInput) input).getFile();
         if (original != null) {
             dialog.setOriginalFile(original);
         }
         
         dialog.create();
         
         IDocumentProvider provider = getDocumentProvider();
         if (provider == null) {
             // editor has programmatically been  closed while the dialog was open
             return;
         }
         
         if (provider.isDeleted(input) && original != null) {
             String message= MessageFormat.format("The original file ''{0}'' has been deleted.", new Object[] { original.getName() });
 
             dialog.setErrorMessage(null);
             dialog.setMessage(message, IMessageProvider.WARNING);
         }
         
         if (dialog.open() == Window.CANCEL) {
             if (progressMonitor != null) {
                 progressMonitor.setCanceled(true);
             }
             return;
         }
         
         IPath filePath = dialog.getResult();
         if (filePath == null) {
             if (progressMonitor != null) {
                 progressMonitor.setCanceled(true);
             }
             return;
         }
         
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         IFile file = workspace.getRoot().getFile(filePath);
         final IEditorInput newInput = new FileEditorInput(file);
         
         boolean success = false;
         try {
             provider.aboutToChange(newInput);
             provider.saveDocument(progressMonitor, newInput, provider.getDocument(input), true);
             success= true;
             
         } catch (CoreException x) {
             IStatus status = x.getStatus();
             if (status == null || status.getSeverity() != IStatus.CANCEL) {
                 // TextEditorMessages.Editor_error_save_title;
                 String title = "Problems During Save As...";
                 // TextEditorMessages.Editor_error_save_message
                 String msg = MessageFormat.format("The original file ''{0}'' has been deleted.", new Object[] { x.getMessage() });
                 
                 if (status != null) {
                     switch (status.getSeverity()) {
                         case IStatus.INFO:
                             MessageDialog.openInformation(shell, title, msg);
                             break;
                         case IStatus.WARNING:
                             MessageDialog.openWarning(shell, title, msg);
                             break;
                         default:
                             MessageDialog.openError(shell, title, msg);
                     }
                 } else {
                     MessageDialog.openError(shell, title, msg);
                 }
             }
         } finally {
             provider.changed(newInput);
             if (success) {
                 setInput(newInput);
             }
         }
         
         if (progressMonitor != null) {
             progressMonitor.setCanceled(!success);
         }
     }
     
     /*
      * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
      */
     @Override
     public boolean isSaveAsAllowed() {
         return true;
     }
     
     /*
      * @see AbstractTextEditor#createActions()
      */
     @Override
     protected void createActions() {
         installEncodingSupport();
         super.createActions();
         
         Action action = new IndentAction(CALEditorMessages.getBundleForConstructedKeys(), "Indent.", this, false); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.INDENT);
         setAction("Indent", action); //$NON-NLS-1$
         markAsStateDependentAction("Indent", true); //$NON-NLS-1$
         markAsSelectionDependentAction("Indent", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.INDENT_ACTION);
         
         action = new IndentAction(CALEditorMessages.getBundleForConstructedKeys(), "Indent.", this, true); //$NON-NLS-1$
         setAction("IndentOnTab", action); //$NON-NLS-1$
         markAsStateDependentAction("IndentOnTab", true); //$NON-NLS-1$
         markAsSelectionDependentAction("IndentOnTab", true); //$NON-NLS-1$
         
         if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_TAB)) {
             // don't replace Shift Right - have to make sure their enablement is mutually exclusive
 //          removeActionActivationCode(ITextEditorActionConstants.SHIFT_RIGHT);
             setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
         }
 
         // Set up open declaration action
         {
             openDeclarationAction = new OpenDeclarationAction(CALEditorMessages.getBundleForConstructedKeys(), "OpenDeclaration.", this); //$NON-NLS-1$
             openDeclarationAction.setActionDefinitionId(CALEditorActionDefinitionIds.OPENDECLARATION_COMMAND);
             setAction("OpenDeclaration", openDeclarationAction); //$NON-NLS-1$
         }
         
         // Set up rename action
         {
             action = new RenameAction(CALEditorMessages.getBundleForConstructedKeys(), "Rename.", this); //$NON-NLS-1$
             action.setActionDefinitionId(CALEditorActionDefinitionIds.RENAMEACTION);
             setAction("Rename", action); //$NON-NLS-1$
         }
         
         {
             action = new QuickSearchInWorkspace(CALEditorMessages.getBundleForConstructedKeys(), "SearchReferencesInWorkspace.", this, ISearchPageContainer.WORKSPACE_SCOPE, QuickSearchInWorkspace.REFERENCES); //$NON-NLS-1$
             action.setActionDefinitionId(CALEditorActionDefinitionIds.SEARCH_REFERENCES_IN_WORKSPACE);
             setAction("SearchReferencesInWorkspace", action); //$NON-NLS-1$
         }
 
         {
             action = new QuickSearchInWorkspace(CALEditorMessages.getBundleForConstructedKeys(), "SearchReferencesInProject.", this, ISearchPageContainer.SELECTED_PROJECTS_SCOPE, QuickSearchInWorkspace.REFERENCES); //$NON-NLS-1$
             action.setActionDefinitionId(CALEditorActionDefinitionIds.SEARCH_REFERENCES_IN_PROJECT);
             setAction("SearchReferencesInProject", action); //$NON-NLS-1$
         }
 
         {
             action = new QuickSearchInWorkspace(CALEditorMessages.getBundleForConstructedKeys(), "SearchDeclarationsInWorkspace.", this, ISearchPageContainer.WORKSPACE_SCOPE, QuickSearchInWorkspace.DEFINITIONS); //$NON-NLS-1$
             action.setActionDefinitionId(CALEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_WORKSPACE);
             setAction("SearchDeclarationsInWorkspace", action); //$NON-NLS-1$
         }
         
         {
             action = new QuickSearchInWorkspace(CALEditorMessages.getBundleForConstructedKeys(), "SearchDeclarationsInProject.", this, ISearchPageContainer.SELECTED_PROJECTS_SCOPE, QuickSearchInWorkspace.DEFINITIONS); //$NON-NLS-1$
             action.setActionDefinitionId(CALEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_PROJECTS);
             setAction("SearchDeclarationsInProject", action); //$NON-NLS-1$
         }
 
         {
             action= new TextOperationAction(CALEditorMessages.getBundleForConstructedKeys(),"ShowOutline.", this, CALSourceViewer.SHOW_OUTLINE, true); //$NON-NLS-1$
             action.setActionDefinitionId(CALEditorActionDefinitionIds.SHOW_OUTLINE);
             setAction(CALEditorActionDefinitionIds.SHOW_OUTLINE, action);
 //            PlatformUI.getWorkbench().getHelpSystem().setHelp(action, ICALHelpContextIds.SHOW_OUTLINE_ACTION);
         }
 
         // Set up show tooltip description action
         {
             action = new ShowTooltipDescriptionAction(CALEditorMessages.getBundleForConstructedKeys(), "ShowTooltipDescription.", this); //$NON-NLS-1$
             action.setActionDefinitionId(CALEditorActionDefinitionIds.SHOWTOOLTIPDESCRIPTION);
             setAction("ShowTooltipDescription", action); //$NON-NLS-1$
         }
         
         // Set up goto next function action
         {
             action = new GotoElementAction(CALEditorMessages.getBundleForConstructedKeys(), "GotoNextFunction.", this, GotoElementAction.Direction.Next); //$NON-NLS-1$
             action.setActionDefinitionId(CALEditorActionDefinitionIds.GOTO_NEXT_ELEMENT_COMMAND);
             setAction("GotoNextFunction", action); //$NON-NLS-1$
         }
         
         // Set up goto next function action
         {
             action = new GotoElementAction(CALEditorMessages.getBundleForConstructedKeys(), "GotoPreviousFunction.", this, GotoElementAction.Direction.Previous); //$NON-NLS-1$
             action.setActionDefinitionId(CALEditorActionDefinitionIds.GOTO_PREVIOUS_ELEMENT_COMMAND);
             setAction("GotoPreviousFunction", action); //$NON-NLS-1$
         }
         
         // Set up the content assist action
         {
             action = new ContentAssistAction(CALEditorMessages.getBundleForConstructedKeys(), "ContentAssistProposal.", this); //$NON-NLS-1$
             final String id = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
             action.setActionDefinitionId(id);
             setAction("ContentAssistProposal", action);
             markAsStateDependentAction("ContentAssistProposal", true);
         }
         
         action = new TextOperationAction(CALEditorMessages.getBundleForConstructedKeys(), "Comment.", this, ITextOperationTarget.PREFIX); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.COMMENT);
         setAction("Comment", action); //$NON-NLS-1$
         markAsStateDependentAction("Comment", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.COMMENT_ACTION);
 
         action = new TextOperationAction(CALEditorMessages.getBundleForConstructedKeys(), "Uncomment.", this, ITextOperationTarget.STRIP_PREFIX); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.UNCOMMENT);
         setAction("Uncomment", action); //$NON-NLS-1$
         markAsStateDependentAction("Uncomment", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.UNCOMMENT_ACTION);
 
         action = new ToggleCommentAction(CALEditorMessages.getBundleForConstructedKeys(), "ToggleComment.", this); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.TOGGLE_COMMENT);
         setAction("ToggleComment", action); //$NON-NLS-1$
         markAsStateDependentAction("ToggleComment", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.TOGGLE_COMMENT_ACTION);
         configureToggleCommentAction();
 
         action = new GenerateElementCommentAction(CALEditorMessages.getBundleForConstructedKeys(), "GenerateElementComment.", this); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.GENERATE_ELEMENT_COMMENT);
         setAction("GenerateElementComment", action); //$NON-NLS-1$
         markAsStateDependentAction("GenerateElementComment", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.GENERATE_ELEMENT_COMMENT_ACTION);
         
         action = new CleanImportsAction(CALEditorMessages.getBundleForConstructedKeys(), "CleanImports.", this); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.CLEAN_IMPORTS);
         setAction("CleanImports", action); //$NON-NLS-1$
         markAsStateDependentAction("CleanImports", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.CLEAN_IMPORTS_ACTION);
 
         action = new TypeDeclarationInserter(CALEditorMessages.getBundleForConstructedKeys(), "TypeDeclarationInserter.", this); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.TYPE_DECLARATION_INSERTER);
         setAction("TypeDeclarationInserter", action); //$NON-NLS-1$
         markAsStateDependentAction("TypeDeclarationInserter", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.TYPE_DECLARATION_INSERTER_ACTION);
 
         action = new PrettyPrinterAction(CALEditorMessages.getBundleForConstructedKeys(), "PrettyPrinter.", this); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.PRETTY_PRINTER);
         setAction("PrettyPrinter", action); //$NON-NLS-1$
         markAsStateDependentAction("PrettyPrinter", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.PRETTY_PRINTER_ACTION);
 
         action = new AddBlockCommentAction(CALEditorMessages.getBundleForConstructedKeys(), "AddBlockComment.", this); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.ADD_BLOCK_COMMENT);
         setAction("AddBlockComment", action); //$NON-NLS-1$
         markAsStateDependentAction("AddBlockComment", true); //$NON-NLS-1$
         markAsSelectionDependentAction("AddBlockComment", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.ADD_BLOCK_COMMENT_ACTION);
 
         action = new RemoveBlockCommentAction(CALEditorMessages.getBundleForConstructedKeys(), "RemoveBlockComment.", this); //$NON-NLS-1$
         action.setActionDefinitionId(CALEditorActionDefinitionIds.REMOVE_BLOCK_COMMENT);
         setAction("RemoveBlockComment", action); //$NON-NLS-1$
         markAsStateDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
         markAsSelectionDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
         PlatformUI.getWorkbench().getHelpSystem().setHelp(action, CALHelpContextIds.REMOVE_BLOCK_COMMENT_ACTION);
         
 
         generateActionGroup = new GenerateActionGroup(this, ITextEditorActionConstants.GROUP_EDIT);
         refactorActionGroup = 
             new GeneralActionGroup(this, ITextEditorActionConstants.GROUP_EDIT, "org.eclipse.jdt.ui.refactor.menu", "org.eclipse.jdt.ui.edit.text.java.refactor.quickMenu", ActionMessages.RefactorMenu_label){
             @Override
             protected int fillSubMenu(IMenuManager source) {
                 int added = 0;
                 added += addEditorAction(source, "Rename"); //$NON-NLS-1$
                 return added;
             }      
         };
         
         searchReferencesActionGroup =
             new GeneralActionGroup(this, ITextEditorActionConstants.GROUP_FIND, "org.eclipse.jdt.ui.search.references.menu", null, ActionMessages.ReferencesMenu_label) {
 
             @Override
             protected int fillSubMenu(IMenuManager source) {
                 int added = 0;
                 added += addEditorAction(source, "SearchReferencesInWorkspace"); //$NON-NLS-1$
                 added += addEditorAction(source, "SearchReferencesInProject"); //$NON-NLS-1$
                 return added;
             }
         };
 
         searchDeclarationsActionGroup =
             new GeneralActionGroup(this, ITextEditorActionConstants.GROUP_FIND, "org.eclipse.jdt.ui.search.declarations.menu", null, ActionMessages.DeclarationsMenu_label) {
 
             @Override
             protected int fillSubMenu(IMenuManager source) {
                 int added = 0;
                 added += addEditorAction(source, "SearchDeclarationsInWorkspace"); //$NON-NLS-1$
                 added += addEditorAction(source, "SearchDeclarationsInProject"); //$NON-NLS-1$
                 return added;
             }
         };
 
         // This intercepts clicks on the lightbulb of compiler errors in order to 
         // activate quick fixes.
         {
             setAction(ITextEditorActionConstants.RULER_CLICK,
                     new CALEditorSelectAnnotationRulerAction(
                             CALEditorMessages.getBundleForConstructedKeys(), "CALEditorSelectAnnotationRulerAction.", this, getVerticalRuler()) //$NON-NLS-1$
                     );
 
         }
     }
     
     /**
      * Handle the ruler click on problem in order to show quick fixes when the lightbulb icon is clicked.
      */
     public class CALEditorSelectAnnotationRulerAction extends SelectMarkerRulerAction {
 
         private boolean fIsEditable;
         private final ITextEditor fTextEditor;
         private Position fPosition;
         private final ResourceBundle fBundle;
         private final String fPrefix;
 
         public CALEditorSelectAnnotationRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor, IVerticalRulerInfo ruler) {
             super(bundle, prefix, editor, ruler);
             fTextEditor = editor;
             fBundle = bundle;
             fPrefix = prefix;
         }
         
         @Override
         public void run() {
             runWithEvent(null);
         }
         
         /*
          * @see org.eclipse.jface.action.IAction#runWithEvent(org.eclipse.swt.widgets.Event)
          * @since 3.2
          */
         @Override
         public void runWithEvent(Event event) {
             if (fIsEditable) {
                 ITextOperationTarget operation = (ITextOperationTarget) fTextEditor.getAdapter(ITextOperationTarget.class);
                 final int opCode= ISourceViewer.QUICK_ASSIST;
                 if (operation != null && operation.canDoOperation(opCode)) {
                     fTextEditor.selectAndReveal(fPosition.getOffset(), fPosition.getLength());
                     operation.doOperation(opCode);
                 }
                 return;
             }
 
             super.run();
         }
 
         @Override
         public void update() {
             checkReadOnly();
             
             if (fIsEditable) {
                 setEnabled(true); // super.update() might change this later
                 initialize(fBundle, fPrefix + "QuickFix."); //$NON-NLS-1$
                 return;
             }
 
             super.update();
         }
 
         private void checkReadOnly() {
             fPosition = null;
             fIsEditable = false;
 
             AbstractMarkerAnnotationModel model = getAnnotationModel();
             IAnnotationAccessExtension annotationAccess = getAnnotationAccessExtension();
 
             IDocument document= getDocument();
             if (model == null) {
                 return;
             }
 
             int layer = Integer.MIN_VALUE;
 
             for (Iterator<Annotation> iter = UnsafeCast.unsafeCast(model.getAnnotationIterator()); iter.hasNext(); ) {
                 Annotation annotation = iter.next();
                 if (annotation.isMarkedDeleted()) {
                     continue;
                 }
 
                 int annotationLayer = annotationAccess.getLayer(annotation);
                 if (annotationAccess != null) {
                     if (annotationLayer < layer) {
                         continue;
                     }
                 }
 
                 Position position = model.getPosition(annotation);
                 if (!includesRulerLine(position, document)) {
                     continue;
                 }
 
                 boolean isReadOnly = fTextEditor instanceof ITextEditorExtension && ((ITextEditorExtension)fTextEditor).isEditorInputReadOnly();
                 if (!isReadOnly) {
                     fPosition = position;
                     fIsEditable = true;
                     layer = annotationLayer;
                     continue;
                 }
             }
         }
     }
     /*
      * @see StatusTextEditor#getStatusHeader(IStatus)
      */
     @Override
     protected String getStatusHeader(IStatus status) {
         if (encodingSupport != null) {
             String message = encodingSupport.getStatusHeader(status);
             if (message != null) {
                 return message;
             }
         }
         return super.getStatusHeader(status);
     }
     
     /*
      * @see StatusTextEditor#getStatusBanner(IStatus)
      */
     @Override
     protected String getStatusBanner(IStatus status) {
         if (encodingSupport != null) {
             String message = encodingSupport.getStatusBanner(status);
             if (message != null) {
                 return message;
             }
         }
         return super.getStatusBanner(status);
     }
     
     /*
      * @see StatusTextEditor#getStatusMessage(IStatus)
      */
     @Override
     protected String getStatusMessage(IStatus status) {
         if (encodingSupport != null) {
             String message = encodingSupport.getStatusMessage(status);
             if (message != null) {
                 return message;
             }
         }
         return super.getStatusMessage(status);
     }
     
     /*
      * @see AbstractTextEditor#doSetInput(IEditorInput)
      */
     @Override
     protected void doSetInput(IEditorInput input) throws CoreException {
         CALSourceViewer sourceViewer = (CALSourceViewer)getSourceViewer();
 
         if (sourceViewer == null) {
             setPreferenceStore(createCombinedPreferenceStore(input));
             internalDoSetInput(input);
             return;
         }
         
         // uninstall & unregister preference store listener
         getSourceViewerDecorationSupport(sourceViewer).uninstall();
         ((ISourceViewerExtension2)sourceViewer).unconfigure();
 
         setPreferenceStore(createCombinedPreferenceStore(input));
 
         // install & register preference store listener
         sourceViewer.configure(getSourceViewerConfiguration());
         getSourceViewerDecorationSupport(sourceViewer).install(getPreferenceStore());
 
         internalDoSetInput(input);
 
         configureTabConverter();
         configureToggleCommentAction();
 //        if (fJavaEditorErrorTickUpdater != null)
 //                fJavaEditorErrorTickUpdater.updateEditorImage(getInputJavaElement());
     }
     
     private void internalDoSetInput(IEditorInput input) throws CoreException {
         CALSourceViewer calSourceViewer = (CALSourceViewer)getSourceViewer();
         
         IPreferenceStore store = getPreferenceStore();
         if (calSourceViewer != null && isFoldingEnabled() && (store == null || !store.getBoolean(PreferenceConstants.EDITOR_SHOW_SEGMENTS))) {
             calSourceViewer.prepareDelayedProjection();
         }
         
         super.doSetInput(input);
         
         if (calSourceViewer != null && calSourceViewer.getReconciler() == null) {
             IReconciler reconciler= getSourceViewerConfiguration().getReconciler(calSourceViewer);
             if (reconciler != null) {
                 reconciler.install(calSourceViewer);
                 calSourceViewer.setReconciler(reconciler);
             }
         }
         
         if (encodingSupport != null) {
             encodingSupport.reset();
         }
     }
     
     // Save the Properties settings.
     private void saveState(){
         XMLMemento memento= XMLMemento.createWriteRoot("caleditor"); //$NON-NLS-1$
         calModuleContentProvider.saveState(memento);
         {
             IPreferenceStore preferenceStore = CALEclipseUIPlugin.getDefault().getPreferenceStore();
             memento.putInteger(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_PRIVATE_SYMBOLS, preferenceStore.getBoolean(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_PRIVATE_SYMBOLS) ? 1 : 0);
             memento.putInteger(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_ELEMENT_HIERARCHY, preferenceStore.getBoolean(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_ELEMENT_HIERARCHY) ? 1 : 0);            
         }
         StringWriter writer= new StringWriter();
         try {
             memento.save(writer);
         } catch (IOException e) {
             // ignore the failed save
         }
         IDialogSettings section= CALEclipseUIPlugin.getDefault().getDialogSettings().getSection(section_name);
         if (section == null) {
             section= CALEclipseUIPlugin.getDefault().getDialogSettings().addNewSection(section_name);
         }
         section.put(memento_key, writer.getBuffer().toString());
     }
     
     public void loadState(){
         // Load the properties state.
         XMLMemento memento = null;
         IDialogSettings section = CALEclipseUIPlugin.getDefault().getDialogSettings().getSection(section_name);
         if (section != null) {
             String settings = section.get(memento_key);
             if (settings != null) {
                 try {
                     memento = XMLMemento.createReadRoot(new StringReader(settings));
                     calModuleContentProvider.loadState(memento);
                     {
                         IPreferenceStore preferenceStore = CALEclipseUIPlugin.getDefault().getPreferenceStore();
                         
                         {
                             Integer value = memento.getInteger(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_PRIVATE_SYMBOLS);
                             if (value != null){
                                 preferenceStore.setDefault(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_PRIVATE_SYMBOLS, value == 1);
                             }
                         }
 
                         {
                             Integer value = memento.getInteger(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_ELEMENT_HIERARCHY);
                             if (value != null){
                                 preferenceStore.setDefault(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_ELEMENT_HIERARCHY, value == 1);
                             }
                         }
                     }                    
                 } catch (WorkbenchException e) {
                     // skip the restore
                 }
             }
         }
     }
 
     /**
      * This class is the adapter for the Outline view. This resuses the same content providers 
      * as the CAL Workspace.
      */
     class CALOutlinePage extends ContentOutlinePage implements SelectedEntitiesChanged {
         private ISelectionChangedListener selectionChangeListener_outlineView;
         private boolean syncEvent = false;
 
         public void selectedEntitiesChanged(LinkedList<Object> pathToEntity) {
             moduleTreeContentProvider.augmentPath(pathToEntity);
             TreeSelection treeSelection = new TreeSelection(new TreePath(pathToEntity.toArray()));
             // make sure that the selectionChanged listener does not update the current
             // position as though the user had selected an item in the tree view.
             syncEvent = true;
             getTreeViewer().setSelection(treeSelection);
         }
 
         @Override
         public void makeContributions(
                 IMenuManager menuManager,
                 IToolBarManager toolBarManager, 
                 IStatusLineManager statusLineManager) {
             moduleTreeContentProvider.fillLocalPullDown(menuManager);
         }
 
         @Override
         public void dispose(){
             saveState();
             super.dispose();
         } 
 
         @Override
         public void createControl(Composite parent) {
             super.createControl(parent);
             TreeViewer viewer  = getTreeViewer();
             DecoratingLabelProvider labelProvider =
                 new DecoratingLabelProvider(
                 new DecoratingLabelProvider(                
                 new DecoratingLabelProvider(
                         new ModuleTreeLabelProvider(calModuleContentProvider),
                         new ScopeDecorator()), 
                         new ProblemMarkerDecorator()),
                         new ForeignDecorator());
 
             // Set up the content providers
             viewer.setContentProvider(moduleTreeContentProvider = new OutlineTreeContentProvider (CALEditor.this, calModuleContentProvider, viewer, labelProvider));
 
             viewer.setLabelProvider(labelProvider);
             viewer.addSelectionChangedListener(this);
             viewer.setInput(moduleTreeContentProvider.getRoot ());
             // When an object is selected this function updated the editor to the current position.
             selectionChangeListener_outlineView = new ISelectionChangedListener() {
                 public void selectionChanged(SelectionChangedEvent event) {
                     // This was caused by the sync'ing of the editor current position so do not treat
                     // this as though the user had selected the item.
                     if (syncEvent){
                         syncEvent = false;
                         return;
                     }
                     TreeSelection treeSelection = (TreeSelection) event.getSelection();
                     // Find the element select if any
                     Object firstElement = treeSelection.getFirstElement();
                     if (firstElement != null){
                         Object entity = firstElement;
                         SourceRange position = null;
                         ModuleName moduleName = null;
                         if (entity instanceof ScopedEntity){
                             QualifiedName name = ((ScopedEntity) entity).getName();
                             Precise searchPosition = calModelManager.getSourceMetrics().getPosition(name, CoreUtility.toCategory(entity));
                             // this can happen when the user uses the outline/workspace 
                             // page for navigation during a rebuild.
                             if (searchPosition != null){
                                 position = searchPosition.getSourceRange();
                                 moduleName = name.getModuleName();
                             }
                         }
                         else if (entity instanceof ClassInstance){
                             ClassInstance classInstance = (ClassInstance) entity;
                             MessageLogger logger = new MessageLogger();
                             position = calModelManager.getSourceMetrics().getPosition(classInstance, logger);
                             moduleName = classInstance.getModuleName();
                         }
                         if (position != null){
                             // Open the editor to the correct position
                             IStorage definitionFile = calModelManager.getInputSourceFile(moduleName);
                             IEditorPart editorPart;
                             try {
                                 editorPart = CoreUtility.openInEditor(definitionFile, true);
                                 CoreUtility.showPosition(editorPart, definitionFile, position);
                                 setFocus();
                             } catch (PartInitException e) {
                                 CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                             }
                         }
                         else{
                             // Some error, since the ScopedEntity exists but source model can't find it.
                             CoreUtility.showErrorOnStatusLine(CALEditor.this, ActionMessages.OpenAction_error_messageBadSelection_CAL);
                         }           
                     }
                 }               
             };
             viewer.addSelectionChangedListener(selectionChangeListener_outlineView);
             
             // Configure the context menu.
             MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
             moduleTreeContentProvider.fillContextMenu(menuMgr);
             menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
             menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
                     + "-end")); //$NON-NLS-1$
 
 
             menuMgr.setOverrides(new IContributionManagerOverrides() {
                 public Integer getAccelerator(IContributionItem item) {
                     return null;
                 }
 
                 public String getAcceleratorText(IContributionItem item) {
                     return null;
                 }
 
                 public Boolean getEnabled(IContributionItem item) {
                     return null;
                 }
 
                 public String getText(IContributionItem item) {
                     return null;
                 }
             });
             
             Menu menu = menuMgr.createContextMenu(viewer.getTree());
             viewer.getTree().setMenu(menu);
             // Be sure to register it so that other plug-ins can add actions.
             getSite().registerContextMenu("org.openquark.cal.eclipse.ui.caleditor.outline", menuMgr, viewer); //$NON-NLS-1$
         }
     }
     
     public static final CALModuleContentProvider calModuleContentProvider = new CALModuleContentProvider() {
         @Override
         public boolean getShowModuleHierarchy() {
             return false;
         }
 
         @Override
         public boolean getShowElementHierarchy() {
             return CALEclipseUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_OUTLINE_SHOW_ELEMENT_HIERARCHY);
         }
         
         @Override
         public boolean getShowPrivateElements() {
             return CALEclipseUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_OUTLINE_SHOW_PRIVATE_SYMBOLS);
         }
 
         @Override
         public boolean getLinkWithEditor() {
             return CALEclipseCorePlugin.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.EDITOR_OUTLINE_LINK_WITH_EDITOR);
         }
 
         @Override
         public void setShowModuleHierarchy(boolean value) {
         }
 
         @Override
         public void setShowElementHierarchy(boolean value) {
             CALEclipseUIPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.EDITOR_OUTLINE_SHOW_ELEMENT_HIERARCHY, value);
         }
         
         @Override
         public void setShowPrivateElements(boolean value) {
             CALEclipseUIPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.EDITOR_OUTLINE_SHOW_PRIVATE_SYMBOLS, value);
         }
         
         @Override
         public void setLinkWithEditor(boolean value) {
             CALEclipseCorePlugin.getDefault().getPluginPreferences().setDefault(PreferenceConstants.EDITOR_OUTLINE_LINK_WITH_EDITOR, value);
         }
         
         @Override
         public CALModelManager getCALModelManager() {
             return calModelManager;
         }
     };
     
     /*
      * @see IAdaptable#getAdapter(java.lang.Class)
      */
     @Override
     public Object getAdapter(Class required) {
         if (IEncodingSupport.class.equals(required)) {
             return encodingSupport;
         }
 
         if (required.equals(IContentOutlinePage.class)) {
             if (outlinePage == null){
                 outlinePage = new CALOutlinePage();
                 selectedEntitiesListeners.add(outlinePage);
             }
             return outlinePage;
         }
         
         if (fProjectionSupport != null) {
             Object adapter = fProjectionSupport.getAdapter(getSourceViewer(), required);
             if (adapter != null) {
                 return adapter;
             }
         }
 
 //        if (required == IContextProvider.class)
 //            return JavaUIHelp.getHelpContextProvider(this, IJavaHelpContextIds.JAVA_EDITOR);
 
         if (SmartBackspaceManager.class.equals(required)) {
             if (getSourceViewer() instanceof CALSourceViewer) {
                 return ((CALSourceViewer)getSourceViewer()).getBackspaceManager();
             }
         }
         
         return super.getAdapter(required);
     }
     
     /*
      * @see org.eclipse.ui.texteditor.AbstractTextEditor#updatePropertyDependentActions()
      */
     @Override
     protected void updatePropertyDependentActions() {
         super.updatePropertyDependentActions();
         if (encodingSupport != null) {
             encodingSupport.reset();
         }
     }
     
     /*
      * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
      */
     @Override
     protected void editorContextMenuAboutToShow(IMenuManager menu) {
         super.editorContextMenuAboutToShow(menu);
         
         menu.appendToGroup(ITextEditorActionConstants.GROUP_SAVE, new Separator(IContextMenuConstants.GROUP_OPEN));
         menu.insertAfter(IContextMenuConstants.GROUP_OPEN, new GroupMarker(IContextMenuConstants.GROUP_SHOW));
 
         IAction action= getAction(CALEditorActionDefinitionIds.SHOW_OUTLINE);
         menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, action);
 
         generateActionGroup.fillContextMenu(menu);
         refactorActionGroup.fillContextMenu(menu);
         searchReferencesActionGroup.fillContextMenu(menu);
         searchDeclarationsActionGroup.fillContextMenu(menu);
     }
     
     private TabConverter tabConverter;
     interface ITextConverter {
         void customizeDocumentCommand(IDocument document, DocumentCommand command);
     }
     public class AdaptedSourceViewer extends CALSourceViewer implements MouseListener {
         
         private List<ITextConverter> fTextConverters;
         private boolean fIgnoreTextConverters= false;
 
         private IInformationPresenter fOutlinePresenter;
         
         public AdaptedSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
             super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles, store);
         }
         
         public ModuleName getModuleName(){
             return CALEditor.this.getModuleName();
         }
         
         public SourceManagerFactory getSourceManagerFactory(boolean updateDocumentIfPossible){
             return CALEditor.getSourceManagerFactory(updateDocumentIfPossible, getSite().getShell(), CALEditor.this);
         }
 
         public CALEditor getEditor(){
             return CALEditor.this;
         }
         
         /*
          * @see ITextOperationTarget#doOperation(int)
          */
         @Override
         public void doOperation(int operation) {
             
             if (getTextWidget() == null) {
                 return;
             }
 
             
             switch (operation) {
 //          case CONTENTASSIST_PROPOSALS:
 //          long time= CODE_ASSIST_DEBUG ? System.currentTimeMillis() : 0;
 //          String msg= fContentAssistant.showPossibleCompletions();
 //          if (CODE_ASSIST_DEBUG) {
 //          long delta= System.currentTimeMillis() - time;
 //          System.err.println("Code Assist (total): " + delta); //$NON-NLS-1$
 //          }
 //          setStatusLineErrorMessage(msg);
 //          return;
 //          case CORRECTIONASSIST_PROPOSALS:
 //          msg= fCorrectionAssistant.showPossibleCompletions();
 //          setStatusLineErrorMessage(msg);
 //          return;
             case SHOW_OUTLINE:
                 if (fOutlinePresenter != null)
                     fOutlinePresenter.showInformation();
                 return;            
             case UNDO:
                 fIgnoreTextConverters= true;
                 super.doOperation(operation);
                 fIgnoreTextConverters= false;
                 return;
             case REDO:
                 fIgnoreTextConverters= true;
                 super.doOperation(operation);
                 fIgnoreTextConverters= false;
                 return;
             }
             
             super.doOperation(operation);
         }
         
         /*
          * @see ITextOperationTarget#canDoOperation(int)
          */
         @Override
         public boolean canDoOperation(int operation) {
 //            if (operation == CORRECTIONASSIST_PROPOSALS)
 //                return isEditable();
             
             return super.canDoOperation(operation);
         }
         
         /*
          * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
          */
         @Override
         public void unconfigure() {
 //            if (fCorrectionAssistant != null) {
 //                fCorrectionAssistant.uninstall();
 //                fCorrectionAssistant= null;
 //            }
             // Remove the mouse listener
             {
                 StyledText text= getTextWidget();
                 if (text == null || text.isDisposed()) {
                     return;
                 }
 
                 text.removeMouseListener(this);
             }
             super.unconfigure();
         }
         
         public void insertTextConverter(ITextConverter textConverter, int index) {
             throw new UnsupportedOperationException();
         }
         
         public void addTextConverter(ITextConverter textConverter) {
             if (fTextConverters == null) {
                 fTextConverters= new ArrayList<ITextConverter>(1);
                 fTextConverters.add(textConverter);
             } else if (!fTextConverters.contains(textConverter)) {
                 fTextConverters.add(textConverter);
             }
         }
         
         public void removeTextConverter(ITextConverter textConverter) {
             if (fTextConverters != null) {
                 fTextConverters.remove(textConverter);
                 if (fTextConverters.size() == 0) {
                     fTextConverters= null;
                 }
             }
         }
         
         /*
          * @see TextViewer#customizeDocumentCommand(DocumentCommand)
          */
         @Override
         protected void customizeDocumentCommand(DocumentCommand command) {
             super.customizeDocumentCommand(command);
             if (!fIgnoreTextConverters && fTextConverters != null) {
                 for (final ITextConverter textConverter : fTextConverters) {
                     textConverter.customizeDocumentCommand(getDocument(), command);
                 }
             }
         }
         
         // http://dev.eclipse.org/bugs/show_bug.cgi?id=19270
         public void updateIndentationPrefixes() {
             SourceViewerConfiguration configuration= getSourceViewerConfiguration();
             String[] types = configuration.getConfiguredContentTypes(this);
             for (final String type : types) {
                 String[] prefixes = configuration.getIndentPrefixes(this, type);
                 if (prefixes != null && prefixes.length > 0) {
                     setIndentPrefixes(prefixes, type);
                 }
             }
         }
         
         /**
          * {@inheritDoc}
          */
         @Override
         public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
             if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed()) {
                 return false;
             }
             return super.requestWidgetToken(requester);
         }
         
         /**
          * {@inheritDoc}
          */
         @Override
         public boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority) {
             if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed()) {
                 return false;
             }
             return super.requestWidgetToken(requester, priority);
         }
         
         /**
          * {@inheritDoc}
          */
         @Override
         public void configure(SourceViewerConfiguration configuration) {
             super.configure(configuration);
 //            fCorrectionAssistant= new JavaCorrectionAssistant(CompilationUnitEditor.this);
 //            fCorrectionAssistant.install(this);
         
             if (configuration instanceof CALSourceViewerConfiguration) {
                 CALSourceViewerConfiguration calSVCconfiguration= (CALSourceViewerConfiguration)configuration;
                 fOutlinePresenter= calSVCconfiguration.getOutlinePresenter(this, false);
                 if (fOutlinePresenter != null)
                     fOutlinePresenter.install(this);
             }
             
 
             // Add the mouse listener
             {
                 StyledText text= getTextWidget();
                 if (text == null || text.isDisposed()) {
                     return;
                 }
 
                 text.addMouseListener(this);
             }
         }
         
         /**
          * {@inheritDoc}
          */
         @Override
         public IFormattingContext createFormattingContext() {
             /*
              * TODOEL
              */
             return super.createFormattingContext();
 //            IFormattingContext context= new CommentFormattingContext();
 //            
 //            Map preferences;
 //            IJavaElement inputJavaElement= getInputJavaElement();
 //            IJavaProject javaProject= inputJavaElement != null ? inputJavaElement.getJavaProject() : null;
 //            if (javaProject == null)
 //                preferences= new HashMap(JavaCore.getOptions());
 //            else
 //                preferences= new HashMap(javaProject.getOptions(true));
 //            
 //            context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, preferences);
 //            
 //            return context;
         }
 
         public void mouseDoubleClick(MouseEvent arg0) {
         }
 
         public void mouseDown(MouseEvent arg0) {
         }
 
         public void mouseUp(MouseEvent arg0) {
         }
     }
 
     /**
      * Internal activation listener.
      */
     private class ActivationListener implements IWindowListener {
         
         /*
          * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
          */
         public void windowActivated(IWorkbenchWindow window) {
 //            if (window == getEditorSite().getWorkbenchWindow() && fMarkOccurrenceAnnotations && isActivePart()) {
 //                fForcedMarkOccurrencesSelection= getSelectionProvider().getSelection();
 //                SelectionListenerWithASTManager.getDefault().forceSelectionChange(JavaEditor.this, (ITextSelection)fForcedMarkOccurrencesSelection);
 //            }
         }
         
         /*
          * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
          */
         public void windowDeactivated(IWorkbenchWindow window) {
 //            if (window == getEditorSite().getWorkbenchWindow() && fMarkOccurrenceAnnotations && isActivePart())
 //                removeOccurrenceAnnotations();
         }
         
         /*
          * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
          */
         public void windowClosed(IWorkbenchWindow window) {
         }
         
         /*
          * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
          */
         public void windowOpened(IWorkbenchWindow window) {
         }
     }
     
     /**
      * Updates the Java outline page selection and this editor's range indicator.
      */
     private class EditorSelectionChangedListener extends AbstractSelectionChangedListener {
         
         /*
          * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
          */
         public void selectionChanged(SelectionChangedEvent event) {
             // XXX: see https://bugs.eclipse.org/bugs/show_bug.cgi?id=56161
             CALEditor.this.selectionChanged(event);
         }
     }
 
     static class TabConverter implements ITextConverter {
         private int fTabRatio;
         private ILineTracker fLineTracker;
         public TabConverter() {
             //do nothing
         }
         public void setNumberOfSpacesPerTab(int ratio) {
             fTabRatio = ratio;
         }
         public void setLineTracker(ILineTracker lineTracker) {
             fLineTracker = lineTracker;
         }
         private int insertTabString(StringBuilder buffer, int offsetInLine) {
             if (fTabRatio == 0) {
                 return 0;
             }
             int remainder = offsetInLine % fTabRatio;
             remainder = fTabRatio - remainder;
             for (int i = 0; i < remainder; i++) {
                 buffer.append(' ');
             }
             return remainder;
         }
         public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
             String text = command.text;
             if (text == null) {
                 return;
             }
             int index = text.indexOf('\t');
             if (index > -1) {
                 StringBuilder buffer = new StringBuilder();
                 fLineTracker.set(command.text);
                 int lines = fLineTracker.getNumberOfLines();
                 try {
                     for (int i = 0; i < lines; i++) {
                         int offset = fLineTracker.getLineOffset(i);
                         int endOffset = offset + fLineTracker.getLineLength(i);
                         String line = text.substring(offset, endOffset);
                         int position = 0;
                         if (i == 0) {
                             IRegion firstLine = document.getLineInformationOfOffset(command.offset);
                             position = command.offset - firstLine.getOffset();
                         }
                         int length = line.length();
                         for (int j = 0; j < length; j++) {
                             char c = line.charAt(j);
                             if (c == '\t') {
                                 position += insertTabString(buffer, position);
                             } else {
                                 buffer.append(c);
                                 ++position;
                             }
                         }
                     }
                     command.text = buffer.toString();
                 } catch (BadLocationException x) {
                     //do nothing
                 }
             }
         }
     }
 
     
     /**
      * Runner that will toggle folding either instantly (if the editor is
      * visible) or the next time it becomes visible. If a runner is started when
      * there is already one registered, the registered one is canceled as
      * toggling folding twice is a no-op.
      * <p>
      * The access to the fFoldingRunner field is not thread-safe, it is assumed
      * that <code>runWhenNextVisible</code> is only called from the UI thread.
      * </p>
      */
     private final class ToggleFoldingRunner implements IPartListener2 {
 
         /**
          * The workbench page we registered the part listener with, or <code>null</code>.
          */
         private IWorkbenchPage fPage;
 
         /**
          * Does the actual toggling of projection.
          */
         private void toggleFolding() {
             ISourceViewer sourceViewer = getSourceViewer();
             if (sourceViewer instanceof ProjectionViewer) {
                 ProjectionViewer pv = (ProjectionViewer)sourceViewer;
                 if (pv.isProjectionMode() != isFoldingEnabled()) {
                     if (pv.canDoOperation(ProjectionViewer.TOGGLE)) {
                         pv.doOperation(ProjectionViewer.TOGGLE);
                     }
                 }
             }
         }
 
         /**
          * Makes sure that the editor's folding state is correct the next time it becomes visible. If it already is
          * visible, it toggles the folding state. If not, it either registers a part listener to toggle folding when the
          * editor becomes visible, or cancels an already registered runner.
          */
         public void runWhenNextVisible() {
             // if there is one already: toggling twice is the identity
             if (fFoldingRunner != null) {
                 fFoldingRunner.cancel();
                 return;
             }
             IWorkbenchPartSite site = getSite();
             if (site != null) {
                 IWorkbenchPage page = site.getPage();
                 if (!page.isPartVisible(CALEditor.this)) {
                     // if we're not visible - defer until visible
                     fPage = page;
                     fFoldingRunner = this;
                     page.addPartListener(this);
                     return;
                 }
             }
             // we're visible - run now
             toggleFolding();
         }
 
         /**
          * Remove the listener and clear the field.
          */
         private void cancel() {
             if (fPage != null) {
                 fPage.removePartListener(this);
                 fPage = null;
             }
             if (fFoldingRunner == this) {
                 fFoldingRunner = null;
             }
         }
 
         /*
          * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
          */
         public void partVisible(IWorkbenchPartReference partRef) {
             if (CALEditor.this.equals(partRef.getPart(false))) {
                 cancel();
                 toggleFolding();
             }
         }
 
         /*
          * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
          */
         public void partClosed(IWorkbenchPartReference partRef) {
             if (CALEditor.this.equals(partRef.getPart(false))) {
                 cancel();
             }
         }
 
         public void partActivated(IWorkbenchPartReference partRef) {
         }
 
         public void partBroughtToTop(IWorkbenchPartReference partRef) {
         }
 
         public void partDeactivated(IWorkbenchPartReference partRef) {
         }
 
         public void partOpened(IWorkbenchPartReference partRef) {
         }
 
         public void partHidden(IWorkbenchPartReference partRef) {
         }
 
         public void partInputChanged(IWorkbenchPartReference partRef) {
         }
     }
     
     /**
      * Configures the toggle comment action
      */
     private void configureToggleCommentAction() {
         IAction action = getAction("ToggleComment"); //$NON-NLS-1$
         if (action instanceof ToggleCommentAction) {
             ISourceViewer sourceViewer = getSourceViewer();
             SourceViewerConfiguration configuration = getSourceViewerConfiguration();
             ((ToggleCommentAction)action).configure(sourceViewer, configuration);
         }
     }
     
     private void configureTabConverter() {
         if (tabConverter != null) {
             tabConverter.setLineTracker(new DefaultLineTracker());
         }
     }
     private void startTabConversion() {
         if (tabConverter == null) {
             tabConverter = new TabConverter();
             configureTabConverter();
             tabConverter.setNumberOfSpacesPerTab(getTabSize());
             AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();
             asv.addTextConverter(tabConverter);
             // http://dev.eclipse.org/bugs/show_bug.cgi?id=19270
             asv.updateIndentationPrefixes();
         }
     }
     private void stopTabConversion() {
         if (tabConverter != null) {
             AdaptedSourceViewer asv = (AdaptedSourceViewer)getSourceViewer();
             asv.removeTextConverter(tabConverter);
             // http://dev.eclipse.org/bugs/show_bug.cgi?id=19270
             asv.updateIndentationPrefixes();
             tabConverter = null;
         }
     }
     
     public int getTabSize() {
 //        IJavaElement element= getInputJavaElement();
 //        IJavaProject project= element == null ? null : element.getJavaProject();
         return CodeFormatterUtil.getTabWidth(null);
     }
     @Override
     protected boolean affectsTextPresentation(PropertyChangeEvent event) {
         return ((CALSourceViewerConfiguration)getSourceViewerConfiguration()).affectsTextPresentation(event) || super.affectsTextPresentation(event);
     }
     @Override
     protected void setPreferenceStore(IPreferenceStore store) {
         super.setPreferenceStore(store);
         if (getSourceViewerConfiguration() instanceof CALSourceViewerConfiguration) {
             CALTextTools textTools = CALEclipseUIPlugin.getDefault().getCALTextTools();
             setSourceViewerConfiguration(new CALSourceViewerConfiguration(textTools.getColorManager(), store, this, CALPartitions.CAL_PARTITIONING));
         }
         if (getSourceViewer() instanceof CALSourceViewer) {
             ((CALSourceViewer)getSourceViewer()).setPreferenceStore(store);
         }
     }
     @Override
     protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
         
         String property = event.getProperty();
         
         if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(property)) {
             /*
              * Ignore tab setting since we rely on the formatter preferences.
              * We do this outside the try-finally block to avoid that EDITOR_TAB_WIDTH
              * is handled by the sub-class (AbstractDecoratedTextEditor).
              */
             return;
         }
         
         try {
             
             ISourceViewer asv = getSourceViewer();
             if (asv == null) {
                 return;
             }
             
             if (PreferenceConstants.EDITOR_CLOSE_BRACES.equals(property)) {
                 fBracketInserter.setCloseBracesEnabled(getPreferenceStore().getBoolean(property));
                 return;
             }
 
             if (PreferenceConstants.EDITOR_CLOSE_BRACKETS.equals(property)) {
                 fBracketInserter.setCloseBracketsEnabled(getPreferenceStore().getBoolean(property));
                 return;
             }
 
             if (PreferenceConstants.EDITOR_CLOSE_STRINGS.equals(property)) {
                 fBracketInserter.setCloseStringsEnabled(getPreferenceStore().getBoolean(property));
                 return;
             }
 
             if (DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR.equals(property)) {
                 if (isTabConversionEnabled()) {
                     startTabConversion();
                 } else {
                     stopTabConversion();
                 }
             }
             
             if (PreferenceConstants.EDITOR_SMART_TAB.equals(property)) {
                 if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_TAB)) {
                     setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
                 } else {
                     removeActionActivationCode("IndentOnTab"); //$NON-NLS-1$
                 }
             }
 
             ((CALSourceViewerConfiguration)getSourceViewerConfiguration()).handlePropertyChangeEvent(event);
             
             if (DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE.equals(property)
                     || DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE.equals(property)
                     || DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR.equals(property)) {
                 StyledText textWidget = asv.getTextWidget();
                 int tabWidth = getSourceViewerConfiguration().getTabWidth(asv);
                 if (textWidget.getTabs() != tabWidth) {
                     textWidget.setTabs(tabWidth);
                 }
                 return;
             }
             
             if (PreferenceConstants.EDITOR_FOLDING_ENABLED.equals(property)) {
                 if (asv instanceof ProjectionViewer) {
                     new ToggleFoldingRunner().runWhenNextVisible();
                 }
                 return;
             }
             
         } finally {
             super.handlePreferenceStoreChanged(event);
         }
         
         if (AbstractDecoratedTextEditorPreferenceConstants.SHOW_RANGE_INDICATOR.equals(property)) {
             // superclass already installed the range indicator
             Object newValue= event.getNewValue();
             ISourceViewer viewer= getSourceViewer();
             if (newValue != null && viewer != null) {
                 if (Boolean.valueOf(newValue.toString()).booleanValue()) {
                     // adjust the highlightrange in order to get the magnet right after changing the selection
                     Point selection= viewer.getSelectedRange();
                     adjustHighlightRange(selection.x, selection.y);
                 }
             }
             
         }
     }
     
     /**
      * Initializes the given viewer's colors.
      *
      * @param viewer the viewer to be initialized
      */
     @Override
     protected void initializeViewerColors(ISourceViewer viewer) {
         // handled by CALSourceViewer
     }
     /**
      * {@inheritDoc}
      */
     @Override
     public int getOrientation() {
         return SWT.LEFT_TO_RIGHT;       //CAL editors are always left to right by default
     }
     @Override
     public void createPartControl(Composite parent) {
         super.createPartControl(parent);
 
         IInformationControlCreator informationControlCreator = new IInformationControlCreator() {
 
             public IInformationControl createInformationControl(Shell shell) {
                 boolean cutDown = false;
                 int style = cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
                 return new DefaultInformationControl(shell, SWT.RESIZE | SWT.TOOL, style, new HTMLTextPresenter(cutDown));
             }
         };
 
         fInformationPresenter = new InformationPresenter(informationControlCreator);
         fInformationPresenter.setSizeConstraints(60, 10, true, true);
         fInformationPresenter.install(getSourceViewer());
 
         fEditorSelectionChangedListener = new EditorSelectionChangedListener();
         fEditorSelectionChangedListener.install(getSelectionProvider());
 
 //        if (fMarkOccurrenceAnnotations)
 //            installOccurrencesFinder();
 //
 //        if (isSemanticHighlightingEnabled())
 //            installSemanticHighlighting();
 
         PlatformUI.getWorkbench().addWindowListener(fActivationListener);
         
         if (isTabConversionEnabled()) {
             startTabConversion();
         }
 
         /*
          * Bracket insertion.
          */
         IPreferenceStore preferenceStore = getPreferenceStore();
         boolean closeBraces = preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACES);
         boolean closeBrackets = preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACKETS);
         boolean closeStrings = preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_STRINGS);
 //        boolean closeAngularBrackets = false;
 
         fBracketInserter.setCloseBracesEnabled(closeBraces);
         fBracketInserter.setCloseBracketsEnabled(closeBrackets);
         fBracketInserter.setCloseStringsEnabled(closeStrings);
 //        fBracketInserter.setCloseAngularBracketsEnabled(closeAngularBrackets);
 
         ISourceViewer sourceViewer = getSourceViewer();
         if (sourceViewer instanceof ITextViewerExtension) {
             ((ITextViewerExtension)sourceViewer).prependVerifyKeyListener(fBracketInserter);
         }
 
     }
     private boolean isTabConversionEnabled() {
 //        IJavaElement element = getInputJavaElement();
 //        IJavaProject project = element == null ? null : element.getJavaProject();
         String option;
 //        if (project == null)
             option = CALEclipseCorePlugin.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
 //        else
 //            option = project.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, true);
         return CoreOptionIDs.SPACE.equals(option);
     }
 
 //    private boolean isTabConversionEnabled() {
 //        IPreferenceStore store = getPreferenceStore();
 //        return store.getBoolean(PreferenceConstants.EDITOR_SPACES_FOR_TABS);
 //    }
     
     @Override
     protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler verticalRuler, int styles) {
         IPreferenceStore store = getPreferenceStore();
         AdaptedSourceViewer adaptedSourceViewer = new AdaptedSourceViewer(parent, verticalRuler, getOverviewRuler(), isOverviewRulerVisible(), styles, store);
 //      JavaUIHelp.setHelp(this, viewer.getTextWidget(), CALHelpContextIds.CAL_EDITOR);
         
         /*
          * This is a performance optimization to reduce the computation of
          * the text presentation triggered by {@link #setVisibleDocument(IDocument)}
          */
         if (adaptedSourceViewer != null && isFoldingEnabled() && (store == null || !store.getBoolean(PreferenceConstants.EDITOR_SHOW_SEGMENTS))) {
             adaptedSourceViewer.prepareDelayedProjection();
         }
 
         fProjectionSupport = new ProjectionSupport(adaptedSourceViewer, getAnnotationAccess(), getSharedColors());
         fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
         fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
         fProjectionSupport.install();
 
         // ensure source viewer decoration support has been created and configured
         getSourceViewerDecorationSupport(adaptedSourceViewer);
 
         return adaptedSourceViewer;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
 
         support.setCharacterPairMatcher(fBracketMatcher);
         support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS, MATCHING_BRACKETS_COLOR);
 
         super.configureSourceViewerDecorationSupport(support);
     }
     
     boolean isFoldingEnabled() {
         return CALEclipseUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
     }
 
     /**
      * React to changed selection.
      */
     protected void selectionChanged(SelectionChangedEvent event) {
         if (getSelectionProvider() == null) {
             return;
         }
         
         synchronizeOutlinePage(event);
 //        ISourceReference element= computeHighlightRangeSourceReference();
 //        if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE))
 //            synchronizeOutlinePage(element);
 //        setSelection(element, false);
 //        updateStatusLine();
     }
 
     /**
      * Listens to changes of the current selection of the editor.
      * 
      * @author Greg McClement
      */
     public interface SelectedEntitiesChanged{
         public void selectedEntitiesChanged(LinkedList<Object> pathToEntity);
     };
 
     void synchronizeOutlinePage(SelectionChangedEvent event){
         final Object[] workspaceListeners = CALWorkspace.getSelectedEntitiesChangedListeners();
         if (selectedEntitiesListeners.size() > 0 || workspaceListeners.length > 0){
             LinkedList<Object> pathToEntity = getPathToSelectedEntity(event);
             if (pathToEntity != null){
                 {
                     Object[] outlineListeners = selectedEntitiesListeners.getListeners();
                     for (int i = 0; i < outlineListeners.length; ++i) {
                         final SelectedEntitiesChanged listener = (SelectedEntitiesChanged) outlineListeners[i];
                         listener.selectedEntitiesChanged(pathToEntity);
                     }
                 }
 
                 {
                     for (int i = 0; i < workspaceListeners.length; ++i) {
                         final SelectedEntitiesChanged listener = (SelectedEntitiesChanged) workspaceListeners[i];
                         listener.selectedEntitiesChanged(pathToEntity);
                     }
                 }
             }
         }
     }
 
     LinkedList<Object> getPathToSelectedEntity(SelectionChangedEvent event){
         if (!calModuleContentProvider.getLinkWithEditor()){
             return null;
         }
         
         ISelection selection = event.getSelection();
         if (selection instanceof TextSelection){
             ITextSelection textSelection = (ITextSelection) selection;
             
             final CALEditor textEditor = CALEditor.this;
             final IDocument document = ActionUtilities.getDocument(textEditor);
 
             if (document != null) {
                 final IStorage storage = textEditor.getStorage();
                 ModuleName moduleName = calModelManager.getModuleName(storage); 
                 
                 final int offset = textSelection.getOffset();
                 int firstLine;
                 try {
                     firstLine = document.getLineOfOffset(offset);
                     final int column = CoreUtility.getColumn(firstLine, offset, document);
                     SourceElement[] sourceElements = calModelManager.getSourceMetrics().findContainingSourceElement(moduleName, firstLine + 1, column + 1);
                     if (sourceElements == null){
                         return null;
                     }
                     
                     ModuleTypeInfo mti = calModelManager.getModuleTypeInfo(moduleName);
                     if (mti == null){
                         return null;
                     }
                     
                     LinkedList<Object> scopedEntities = new LinkedList<Object>();  // ScopedEntity or ClassInstance
                     for(int i = 0; i < sourceElements.length; ++i){
                         SourceElement sourceElement = sourceElements[i];
                         if (sourceElement instanceof FunctionTypeDeclaration){
                             FunctionTypeDeclaration ftd = (FunctionTypeDeclaration) sourceElement;
                             scopedEntities.addLast(mti.getFunction(ftd.getFunctionName()));
                         }
                         else if (sourceElement instanceof FunctionDefn){
                             FunctionDefn ftd = (FunctionDefn) sourceElement;
                             scopedEntities.addLast(mti.getFunction(ftd.getName()));
                         }
                         else if (sourceElement instanceof AlgebraicType){
                             AlgebraicType at = (AlgebraicType) sourceElement;
                             scopedEntities.addLast(mti.getTypeConstructor(at.getTypeConsName()));
                         }
                         else if (sourceElement instanceof DataConsDefn){
                             DataConsDefn dcd = (DataConsDefn) sourceElement;
                             scopedEntities.addLast(mti.getDataConstructor(dcd.getDataConsName()));
                         }
                         else if (sourceElement instanceof TypeClassDefn){
                             TypeClassDefn tcd = (TypeClassDefn) sourceElement;
                             scopedEntities.addLast(mti.getTypeClass(tcd.getTypeClassName()));
                         }
                         else if (sourceElement instanceof Primitive){
                             Primitive p = (Primitive) sourceElement;
                             scopedEntities.addLast(mti.getFunction(p.getName()));
                         }
                         else if (sourceElement instanceof TypeConstructorDefn){
                             TypeConstructorDefn tcd = (TypeConstructorDefn) sourceElement;
                             scopedEntities.addLast(mti.getTypeConstructor(tcd.getTypeConsName()));
                         }
                         else if (sourceElement instanceof ClassMethodDefn){
                             ClassMethodDefn cmd = (ClassMethodDefn) sourceElement;
                             scopedEntities.addLast(mti.getClassMethod(cmd.getMethodName()));
                         }
                         else if (sourceElement instanceof InstanceDefn){
                             InstanceDefn instanceDefn = (InstanceDefn) sourceElement;
                             final int nClassInstances = mti.getNClassInstances();
                             for (int iClassInstance = 0; iClassInstance < nClassInstances; ++iClassInstance) {
                                 ClassInstance classInstance = mti.getNthClassInstance(iClassInstance);
                                 if (SourceMetricsManager.same(classInstance, instanceDefn, mti)){
                                     scopedEntities.addLast(classInstance);
                                     break;
                                 }
                             }
                         }
                         else{
                             throw new IllegalStateException();
                         }
                         if (scopedEntities.size() == 0 || scopedEntities.getLast() == null){
                             return null;
                         }
                     }
                     // if the module type info is out of sync with the source file then
                     // the path will contain null so just return null array since the
                     // path is not valid
                     for (final Object object : scopedEntities) {
                         if (object == null){
                             return null;
                         }                        
                     }
                     return scopedEntities;
                 } catch (BadLocationException e) {
                     // will only happen on concurrent modification
                     CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                     return null;                
                 }
             }
         }
         return null;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     protected void createNavigationActions() {
         super.createNavigationActions();
 
         final StyledText textWidget = getSourceViewer().getTextWidget();
 
         IAction action = new SmartLineStartAction(textWidget, false);
         action.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_START);
         setAction(ITextEditorActionDefinitionIds.LINE_START, action);
 
         action = new SmartLineStartAction(textWidget, true);
         action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_LINE_START);
         setAction(ITextEditorActionDefinitionIds.SELECT_LINE_START, action);
 
         action = new NavigatePreviousSubWordAction();
         action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_PREVIOUS);
         setAction(ITextEditorActionDefinitionIds.WORD_PREVIOUS, action);
         textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_LEFT, SWT.NULL);
 
         action = new NavigateNextSubWordAction();
         action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_NEXT);
         setAction(ITextEditorActionDefinitionIds.WORD_NEXT, action);
         textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_RIGHT, SWT.NULL);
 
         action = new SelectPreviousSubWordAction();
         action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS);
         setAction(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS, action);
         textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_LEFT, SWT.NULL);
 
         action = new SelectNextSubWordAction();
         action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT);
         setAction(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT, action);
         textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_RIGHT, SWT.NULL);
         
         action = new DeletePreviousSubWordAction();
         action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD);
         setAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, action);
         textWidget.setKeyBinding(SWT.CTRL | SWT.BS, SWT.NULL);
         markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, true);
 
         action = new DeleteNextSubWordAction();
         action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD);
         setAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, action);
         textWidget.setKeyBinding(SWT.CTRL | SWT.DEL, SWT.NULL);
         markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, true);
 
     }
 
     /**
      * This action implements smart home.
      * 
      * Instead of going to the start of a line it does the following:
      *  - if smart home/end is enabled and the caret is after the line's first non-whitespace then the caret is moved
      * directly before it, taking CALDoc and multi-line comments into account. - if the caret is before the line's
      * first non-whitespace the caret is moved to the beginning of the line - if the caret is at the beginning of the
      * line see first case.
      * 
      * @author Edward Lam
      */
     protected class SmartLineStartAction extends LineStartAction {
 
         /**
          * Creates a new smart line start action
          *
          * @param textWidget the styled text widget
          * @param doSelect a boolean flag which tells if the text up to the beginning of the line should be selected
          */
         public SmartLineStartAction(final StyledText textWidget, final boolean doSelect) {
             super(textWidget, doSelect);
         }
 
         /*
          * @see org.eclipse.ui.texteditor.AbstractTextEditor.LineStartAction#getLineStartPosition(java.lang.String, int, java.lang.String)
          */
         @Override
         protected int getLineStartPosition(final IDocument document, final String line, final int length, final int offset) {
 
             String type = IDocument.DEFAULT_CONTENT_TYPE;
             try {
                 type = TextUtilities.getContentType(document, CALPartitions.CAL_PARTITIONING, offset, true);
             } catch (BadLocationException exception) {
                 // Should not happen
             }
 
             int index = super.getLineStartPosition(document, line, length, offset);
             if (type.equals(CALPartitions.CAL_DOC) || type.equals(CALPartitions.CAL_MULTI_LINE_COMMENT)) {
                 if (index < length - 1 && line.charAt(index) == '*' && line.charAt(index + 1) != '/') {
                     do {
                         ++index;
                     } while (index < length && LanguageInfo.isCALWhitespace(line.charAt(index)));
                 }
             } else {
                 if (index < length - 1 && line.charAt(index) == '/' && line.charAt(index + 1) == '/') {
                     index++;
                     do {
                         ++index;
                     } while (index < length && LanguageInfo.isCALWhitespace(line.charAt(index)));
                 }
             }
             return index;
         }
     }
 
     /**
      * Text navigation action to navigate to the next sub-word.
      * 
      * @author Edward Lam
      */
     protected abstract class NextSubWordAction extends TextNavigationAction {
 
         protected CALWordIterator fIterator = new CALWordIterator();
 
         /**
          * Creates a new next sub-word action.
          *
          * @param code Action code for the default operation. Must be an action code from @see org.eclipse.swt.custom.ST.
          */
         protected NextSubWordAction(int code) {
             super(getSourceViewer().getTextWidget(), code);
         }
 
         /*
          * @see org.eclipse.jface.action.IAction#run()
          */
         @Override
         public void run() {
             // Check whether we are in a java code partition and the preference is enabled
             final IPreferenceStore store = getPreferenceStore();
             if (!store.getBoolean(PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION)) {
                 super.run();
                 return;
             }
 
             final ISourceViewer viewer = getSourceViewer();
             final IDocument document = viewer.getDocument();
             fIterator.setText((CharacterIterator)new DocumentCharacterIterator(document));
             int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
             if (position == -1) {
                 return;
             }
 
             int next = findNextPosition(position);
             if (next != BreakIterator.DONE) {
                 setCaretPosition(next);
                 getTextWidget().showSelection();
                 fireSelectionChanged();
             }
 
         }
 
         /**
          * Finds the next position after the given position.
          *
          * @param position the current position
          * @return the next position
          */
         protected int findNextPosition(int position) {
             ISourceViewer viewer = getSourceViewer();
             int widget = -1;
             while (position != BreakIterator.DONE && widget == -1) { // TODO: optimize
                 position = fIterator.following(position);
                 if (position != BreakIterator.DONE) {
                     widget = modelOffset2WidgetOffset(viewer, position);
                 }
             }
             return position;
         }
 
         /**
          * Sets the caret position to the sub-word boundary given with <code>position</code>.
          *
          * @param position Position where the action should move the caret
          */
         protected abstract void setCaretPosition(int position);
     }
 
     /**
      * Text navigation action to navigate to the next sub-word.
      * 
      * @author Edward Lam
      */
     protected class NavigateNextSubWordAction extends NextSubWordAction {
 
         /**
          * Creates a new navigate next sub-word action.
          */
         public NavigateNextSubWordAction() {
             super(ST.WORD_NEXT);
         }
 
         /*
          * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.NextSubWordAction#setCaretPosition(int)
          */
         @Override
         protected void setCaretPosition(final int position) {
             getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
         }
     }
 
     /**
      * Text operation action to delete the next sub-word.
      * 
      * @author Edward Lam
      */
     protected class DeleteNextSubWordAction extends NextSubWordAction implements IUpdate {
 
         /**
          * Creates a new delete next sub-word action.
          */
         public DeleteNextSubWordAction() {
             super(ST.DELETE_WORD_NEXT);
         }
 
         /*
          * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.NextSubWordAction#setCaretPosition(int)
          */
         @Override
         protected void setCaretPosition(final int position) {
             if (!validateEditorInputState()) {
                 return;
             }
 
             final ISourceViewer viewer = getSourceViewer();
             final int caret, length;
             Point selection = viewer.getSelectedRange();
             if (selection.y != 0) {
                 caret = selection.x;
                 length = selection.y;
             } else {
                 caret = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
                 length = position - caret;
             }
 
             try {
                 viewer.getDocument().replace(caret, length, ""); //$NON-NLS-1$
             } catch (BadLocationException exception) {
                 // Should not happen
             }
         }
 
         /*
          * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.NextSubWordAction#findNextPosition(int)
          */
         @Override
         protected int findNextPosition(int position) {
             return fIterator.following(position);
         }
 
         /*
          * @see org.eclipse.ui.texteditor.IUpdate#update()
          */
         public void update() {
             setEnabled(isEditorInputModifiable());
         }
     }
 
     /**
      * Text operation action to select the next sub-word.
      * 
      * @author Edward Lam
      */
     protected class SelectNextSubWordAction extends NextSubWordAction {
 
         /**
          * Creates a new select next sub-word action.
          */
         public SelectNextSubWordAction() {
             super(ST.SELECT_WORD_NEXT);
         }
 
         /*
          * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.NextSubWordAction#setCaretPosition(int)
          */
         @Override
         protected void setCaretPosition(final int position) {
             final ISourceViewer viewer = getSourceViewer();
 
             final StyledText text = viewer.getTextWidget();
             if (text != null && !text.isDisposed()) {
 
                 final Point selection = text.getSelection();
                 final int caret = text.getCaretOffset();
                 final int offset = modelOffset2WidgetOffset(viewer, position);
 
                 if (caret == selection.x) {
                     text.setSelectionRange(selection.y, offset - selection.y);
                 } else {
                     text.setSelectionRange(selection.x, offset - selection.x);
                 }
             }
         }
     }
 
     /**
      * Text navigation action to navigate to the previous sub-word.
      * 
      * @author Edward Lam
      */
     protected abstract class PreviousSubWordAction extends TextNavigationAction {
 
         protected CALWordIterator fIterator = new CALWordIterator();
 
         /**
          * Creates a new previous sub-word action.
          *
          * @param code Action code for the default operation. Must be an action code from @see org.eclipse.swt.custom.ST.
          */
         protected PreviousSubWordAction(final int code) {
             super(getSourceViewer().getTextWidget(), code);
         }
 
         /*
          * @see org.eclipse.jface.action.IAction#run()
          */
         @Override
         public void run() {
             // Check whether we are in a java code partition and the preference is enabled
             final IPreferenceStore store = getPreferenceStore();
             if (!store.getBoolean(PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION)) {
                 super.run();
                 return;
             }
 
             final ISourceViewer viewer = getSourceViewer();
             final IDocument document = viewer.getDocument();
             fIterator.setText((CharacterIterator)new DocumentCharacterIterator(document));
             int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
             if (position == -1) {
                 return;
             }
 
             int previous = findPreviousPosition(position);
             if (previous != BreakIterator.DONE) {
                 setCaretPosition(previous);
                 getTextWidget().showSelection();
                 fireSelectionChanged();
             }
 
         }
 
         /**
          * Finds the previous position before the given position.
          *
          * @param position the current position
          * @return the previous position
          */
         protected int findPreviousPosition(int position) {
             ISourceViewer viewer = getSourceViewer();
             int widget = -1;
             while (position != BreakIterator.DONE && widget == -1) { // TODO: optimize
                 position = fIterator.preceding(position);
                 if (position != BreakIterator.DONE) {
                     widget = modelOffset2WidgetOffset(viewer, position);
                 }
             }
             return position;
         }
 
         /**
          * Sets the caret position to the sub-word boundary given with <code>position</code>.
          *
          * @param position Position where the action should move the caret
          */
         protected abstract void setCaretPosition(int position);
     }
 
     /**
      * Text navigation action to navigate to the previous sub-word.
      * 
      * @author Edward Lam
      */
     protected class NavigatePreviousSubWordAction extends PreviousSubWordAction {
 
         /**
          * Creates a new navigate previous sub-word action.
          */
         public NavigatePreviousSubWordAction() {
             super(ST.WORD_PREVIOUS);
         }
 
         /*
          * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.PreviousSubWordAction#setCaretPosition(int)
          */
         @Override
         protected void setCaretPosition(final int position) {
             getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
         }
     }
 
     /**
      * Text operation action to delete the previous sub-word.
      * 
      * @author Edward Lam
      */
     protected class DeletePreviousSubWordAction extends PreviousSubWordAction implements IUpdate {
 
         /**
          * Creates a new delete previous sub-word action.
          */
         public DeletePreviousSubWordAction() {
             super(ST.DELETE_WORD_PREVIOUS);
         }
 
         /*
          * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.PreviousSubWordAction#setCaretPosition(int)
          */
         @Override
         protected void setCaretPosition(int position) {
             if (!validateEditorInputState()) {
                 return;
             }
 
             final int length;
             final ISourceViewer viewer = getSourceViewer();
             Point selection = viewer.getSelectedRange();
             if (selection.y != 0) {
                 position = selection.x;
                 length = selection.y;
             } else {
                 length = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset()) - position;
             }
 
             try {
                 viewer.getDocument().replace(position, length, ""); //$NON-NLS-1$
             } catch (BadLocationException exception) {
                 // Should not happen
             }
         }
 
         /*
          * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.PreviousSubWordAction#findPreviousPosition(int)
          */
         @Override
         protected int findPreviousPosition(int position) {
             return fIterator.preceding(position);
         }
 
         /*
          * @see org.eclipse.ui.texteditor.IUpdate#update()
          */
         public void update() {
             setEnabled(isEditorInputModifiable());
         }
     }
 
     /**
      * Text operation action to select the previous sub-word.
      * 
      * @author Edward Lam
      */
     protected class SelectPreviousSubWordAction extends PreviousSubWordAction {
 
         /**
          * Creates a new select previous sub-word action.
          */
         public SelectPreviousSubWordAction() {
             super(ST.SELECT_WORD_PREVIOUS);
         }
 
         /*
          * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.PreviousSubWordAction#setCaretPosition(int)
          */
         @Override
         protected void setCaretPosition(final int position) {
             final ISourceViewer viewer = getSourceViewer();
 
             final StyledText text = viewer.getTextWidget();
             if (text != null && !text.isDisposed()) {
 
                 final Point selection = text.getSelection();
                 final int caret = text.getCaretOffset();
                 final int offset = modelOffset2WidgetOffset(viewer, position);
 
                 if (caret == selection.x) {
                     text.setSelectionRange(selection.y, offset - selection.y);
                 } else {
                     text.setSelectionRange(selection.x, offset - selection.x);
                 }
             }
         }
     }
 
     /**
      * @author Edward Lam
      */
     private class ExitPolicy implements IExitPolicy {
 
         final char fExitCharacter;
         final char fEscapeCharacter;
         final Stack<BracketLevel> fStack;
         final int fSize;
 
         public ExitPolicy(char exitCharacter, char escapeCharacter, Stack<BracketLevel> stack) {
             fExitCharacter = exitCharacter;
             fEscapeCharacter = escapeCharacter;
             fStack = stack;
             fSize = fStack.size();
         }
 
         /*
          * @see org.eclipse.jdt.internal.ui.text.link.LinkedPositionUI.ExitPolicy#doExit(org.eclipse.jdt.internal.ui.text.link.LinkedPositionManager,
          *      org.eclipse.swt.events.VerifyEvent, int, int)
          */
         public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
 
             if (fSize == fStack.size() && !isMasked(offset)) {
                 if (event.character == fExitCharacter) {
                     BracketLevel level = fStack.peek();
                     if (level.fFirstPosition.offset > offset || level.fSecondPosition.offset < offset) {
                         return null;
                     }
                     if (level.fSecondPosition.offset == offset && length == 0) {
                         // don't enter the character if if its the closing peer
                         return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
                     }
                 }
                 // when entering an anonymous class between the parenthesis', we don't want
                 // to jump after the closing parenthesis when return is pressed
                 if (event.character == SWT.CR && offset > 0) {
                     IDocument document = getSourceViewer().getDocument();
                     try {
                         if (document.getChar(offset - 1) == '{') {
                             return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
                         }
                     } catch (BadLocationException e) {
                     }
                 }
             }
             return null;
         }
 
         private boolean isMasked(int offset) {
             IDocument document = getSourceViewer().getDocument();
             try {
                 return fEscapeCharacter == document.getChar(offset - 1);
             } catch (BadLocationException e) {
             }
             return false;
         }
     }
 
     /**
      * @author Edward Lam
      */
     private static class BracketLevel {
 
         int fOffset;
         int fLength;
         LinkedModeUI fUI;
         Position fFirstPosition;
         Position fSecondPosition;
     }
 
     /**
      * Position updater that takes any changes at the borders of a position to not belong to the position.
      * @author Edward Lam
      */
     private static class ExclusivePositionUpdater implements IPositionUpdater {
 
         /** The position category. */
         private final String fCategory;
 
         /**
          * Creates a new updater for the given <code>category</code>.
          * 
          * @param category the new category.
          */
         public ExclusivePositionUpdater(String category) {
             fCategory = category;
         }
 
         /*
          * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
          */
         public void update(DocumentEvent event) {
 
             int eventOffset = event.getOffset();
             int eventOldLength = event.getLength();
             int eventNewLength = event.getText() == null ? 0 : event.getText().length();
             int deltaLength = eventNewLength - eventOldLength;
 
             try {
                 Position[] positions = event.getDocument().getPositions(fCategory);
 
                 for (int i = 0; i != positions.length; i++) {
 
                     Position position = positions[i];
 
                     if (position.isDeleted()) {
                         continue;
                     }
 
                     int offset = position.getOffset();
                     int length = position.getLength();
                     int end = offset + length;
 
                     if (offset >= eventOffset + eventOldLength) {
                         // position comes
                         // after change - shift
                         position.setOffset(offset + deltaLength);
                     } else if (end <= eventOffset) {
                         // position comes way before change -
                         // leave alone
                     } else if (offset <= eventOffset && end >= eventOffset + eventOldLength) {
                         // event completely internal to the position - adjust length
                         position.setLength(length + deltaLength);
                     } else if (offset < eventOffset) {
                         // event extends over end of position - adjust length
                         int newEnd = eventOffset;
                         position.setLength(newEnd - offset);
                     } else if (end > eventOffset + eventOldLength) {
                         // event extends from before position into it - adjust offset
                         // and length
                         // offset becomes end of event, length adjusted accordingly
                         int newOffset = eventOffset + eventNewLength;
                         position.setOffset(newOffset);
                         position.setLength(end - newOffset);
                     } else {
                         // event consumes the position - delete it
                         position.delete();
                     }
                 }
             } catch (BadPositionCategoryException e) {
                 // ignore and return
             }
         }
 
         /**
          * Returns the position category.
          * 
          * @return the position category
          */
         public String getCategory() {
             return fCategory;
         }
 
     }
     
     /**
      * Closes braces, brackets, parents, string and char literals automatically, if configured.
      * @author Edward Lam
      */
     private class BracketInserter implements VerifyKeyListener, ILinkedModeListener {
 
         private boolean fCloseBraces = true;
         private boolean fCloseBrackets = true;
         private boolean fCloseStrings = true;
 //        private boolean fCloseAngularBrackets = true;
         private final String CATEGORY = toString();
         private final IPositionUpdater fUpdater = new ExclusivePositionUpdater(CATEGORY);
         private final Stack<BracketLevel> fBracketLevelStack = new Stack<BracketLevel>();
 
         public void setCloseBracesEnabled(boolean enabled) {
             fCloseBraces = enabled;
         }
         
         public void setCloseBracketsEnabled(boolean enabled) {
             fCloseBrackets = enabled;
         }
 
         public void setCloseStringsEnabled(boolean enabled) {
             fCloseStrings = enabled;
         }
 
 //        public void setCloseAngularBracketsEnabled(boolean enabled) {
 //            fCloseAngularBrackets = enabled;
 //        }
 
 //        private boolean isAngularIntroducer(String identifier) {
 //            return identifier.length() > 0 && (Character.isUpperCase(identifier.charAt(0)) || identifier.startsWith("final") //$NON-NLS-1$
 //                    || identifier.startsWith("public") //$NON-NLS-1$
 //                    || identifier.startsWith("public") //$NON-NLS-1$
 //                    || identifier.startsWith("protected") //$NON-NLS-1$
 //            || identifier.startsWith("private")); //$NON-NLS-1$
 //        }
 
         /*
          * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
          */
         public void verifyKey(VerifyEvent event) {
 
             // early pruning to slow down normal typing as little as possible
             if (!event.doit || getInsertMode() != SMART_INSERT) {
                 return;
             }
             switch (event.character) {
                 case '{':
                 case '(':
 //                case '<':
                 case '[':
                 case '\'':
                 case '\"':
                     break;
                 default:
                     return;
             }
 
             final ISourceViewer sourceViewer = getSourceViewer();
             IDocument document = sourceViewer.getDocument();
 
             final Point selection = sourceViewer.getSelectedRange();
             final int offset = selection.x;
             final int length = selection.y;
 
             try {
                 IRegion startLine = document.getLineInformationOfOffset(offset);
                 IRegion endLine = document.getLineInformationOfOffset(offset + length);
 
                 CALHeuristicScanner scanner = new CALHeuristicScanner(document);
                 int nextToken = scanner.nextToken(offset + length, endLine.getOffset() + endLine.getLength());
                 String next = nextToken == Symbols.TokenEOF ? null : document.get(offset, scanner.getPosition() - offset).trim();
                 int prevToken = scanner.previousToken(offset - 1, startLine.getOffset());
                 int prevTokenOffset = scanner.getPosition() + 1;
                 String previous = prevToken == Symbols.TokenEOF ? null : document.get(prevTokenOffset, offset - prevTokenOffset).trim();
 
                 switch (event.character) {
                     case '{':
                         if (!fCloseBraces || 
                                 nextToken == Symbols.TokenCONSIDENT || nextToken == Symbols.TokenOTHERIDENT || next != null && next.length() > 1) {
                             return;
                         }
                         break;
                         
                     case '(':
                         if (!fCloseBrackets || nextToken == Symbols.TokenLPAREN || 
                                 nextToken == Symbols.TokenCONSIDENT || nextToken == Symbols.TokenOTHERIDENT || next != null && next.length() > 1) {
                             return;
                         }
                         break;
 
 //                    case '<':
 //                        if (!(fCloseAngularBrackets && fCloseBrackets) || nextToken == Symbols.TokenLESSTHAN || prevToken != Symbols.TokenLBRACE
 //                                && prevToken != Symbols.TokenRBRACE && prevToken != Symbols.TokenSEMICOLON && prevToken != Symbols.TokenSYNCHRONIZED
 //                                && prevToken != Symbols.TokenSTATIC && (prevToken != Symbols.TokenIDENT || !isAngularIntroducer(previous)) && prevToken != Symbols.TokenEOF)
 //                            return;
 //                        break;
 
                     case '[':
                         if (!fCloseBrackets || 
                                 nextToken == Symbols.TokenCONSIDENT || nextToken == Symbols.TokenOTHERIDENT || next != null && next.length() > 1) {
                             return;
                         }
                         break;
                         
                     case '\'':
                     case '"':
                         if (!fCloseStrings || 
                                 nextToken == Symbols.TokenCONSIDENT || nextToken == Symbols.TokenOTHERIDENT || 
                                 prevToken == Symbols.TokenCONSIDENT || prevToken == Symbols.TokenOTHERIDENT ||
                                 next != null && next.length() > 1 || previous != null && previous.length() > 1) {
                             return;
                         }
                         break;
                         
                     default:
                         return;
                 }
 
                 ITypedRegion partition = TextUtilities.getPartition(document, CALPartitions.CAL_PARTITIONING, offset, true);
                 if (!IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())) {
                     return;
                 }
 
                 if (!validateEditorInputState()) {
                     return;
                 }
 
                 final char character = event.character;
                 final char closingCharacter = getPeerCharacter(character);
                 final StringBuilder buffer = new StringBuilder();
                 buffer.append(character);
                 buffer.append(closingCharacter);
 
                 document.replace(offset, length, buffer.toString());
 
                 BracketLevel level = new BracketLevel();
                 fBracketLevelStack.push(level);
 
                 LinkedPositionGroup group = new LinkedPositionGroup();
                 group.addPosition(new LinkedPosition(document, offset + 1, 0, LinkedPositionGroup.NO_STOP));
 
                 LinkedModeModel model = new LinkedModeModel();
                 model.addLinkingListener(this);
                 model.addGroup(group);
                 model.forceInstall();
 
                 level.fOffset = offset;
                 level.fLength = 2;
 
                 // set up position tracking for our magic peers
                 if (fBracketLevelStack.size() == 1) {
                     document.addPositionCategory(CATEGORY);
                     document.addPositionUpdater(fUpdater);
                 }
                 level.fFirstPosition = new Position(offset, 1);
                 level.fSecondPosition = new Position(offset + 1, 1);
                 document.addPosition(CATEGORY, level.fFirstPosition);
                 document.addPosition(CATEGORY, level.fSecondPosition);
 
                 level.fUI = new EditorLinkedModeUI(model, sourceViewer);
                 level.fUI.setSimpleMode(true);
                 level.fUI.setExitPolicy(new ExitPolicy(closingCharacter, getEscapeCharacter(closingCharacter), fBracketLevelStack));
                 level.fUI.setExitPosition(sourceViewer, offset + 2, 0, Integer.MAX_VALUE);
                 level.fUI.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
                 level.fUI.enter();
 
                 IRegion newSelection = level.fUI.getSelectedRegion();
                 sourceViewer.setSelectedRange(newSelection.getOffset(), newSelection.getLength());
 
                 event.doit = false;
 
             } catch (BadLocationException e) {
                 CALEclipseUIPlugin.log(e);
             } catch (BadPositionCategoryException e) {
                 CALEclipseUIPlugin.log(e);
             }
         }
 
         /*
          * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
          */
         public void left(LinkedModeModel environment, int flags) {
 
             final BracketLevel level = fBracketLevelStack.pop();
 
             if (flags != ILinkedModeListener.EXTERNAL_MODIFICATION) {
                 return;
             }
 
             // remove brackets
             final ISourceViewer sourceViewer = getSourceViewer();
             final IDocument document = sourceViewer.getDocument();
             if (document instanceof IDocumentExtension) {
                 IDocumentExtension extension = (IDocumentExtension)document;
                 extension.registerPostNotificationReplace(null, new IDocumentExtension.IReplace() {
 
                     public void perform(IDocument d, IDocumentListener owner) {
                         if ((level.fFirstPosition.isDeleted || level.fFirstPosition.length == 0) && !level.fSecondPosition.isDeleted
                                 && level.fSecondPosition.offset == level.fFirstPosition.offset) {
                             try {
                                 document.replace(level.fSecondPosition.offset, level.fSecondPosition.length, null);
                             } catch (BadLocationException e) {
                                 CALEclipseUIPlugin.log(e);
                             }
                         }
 
                         if (fBracketLevelStack.size() == 0) {
                             document.removePositionUpdater(fUpdater);
                             try {
                                 document.removePositionCategory(CATEGORY);
                             } catch (BadPositionCategoryException e) {
                                 CALEclipseUIPlugin.log(e);
                             }
                         }
                     }
                 });
             }
 
         }
 
         /*
          * @see org.eclipse.jface.text.link.ILinkedModeListener#suspend(org.eclipse.jface.text.link.LinkedModeModel)
          */
         public void suspend(LinkedModeModel environment) {
         }
 
         /*
          * @see org.eclipse.jface.text.link.ILinkedModeListener#resume(org.eclipse.jface.text.link.LinkedModeModel, int)
          */
         public void resume(LinkedModeModel environment, int flags) {
         }
     }
 
     /**
      * Helper for bracket inserter.
      * @param character a character which appears in a string
      * @return the character which must precede the character in order to escape it in the string.
      */
     private static char getEscapeCharacter(char character) {
         switch (character) {
             case '"':
             case '\'':
                 return '\\';
             default:
                 return 0;
         }
     }
 
     /**
      * @param character a character which has a "peer" in the cal language.
      * @return the character's peer.  eg. for an opening bracket, returns the closing bracket.
      */
     private static char getPeerCharacter(char character) {
         switch (character) {
             case '(':
                 return ')';
 
             case ')':
                 return '(';
             
             case '{':
                 return '}';
             
             case '}':
                 return '{';
 
 //            case '<':
 //                return '>';
 //
 //            case '>':
 //                return '<';
 
             case '[':
                 return ']';
 
             case ']':
                 return '[';
 
             case '"':
                 return character;
 
             case '\'':
                 return character;
 
             default:
                 throw new IllegalArgumentException();
         }
     }
 
     public ModuleName getModuleName(){
         IStorage memberFile = getStorage();
         return calModelManager.getModuleName(memberFile); 
     }
     
     public IStorage getStorage() {
         IEditorInput input = getEditorInput();
         if (input instanceof IStorageEditorInput) {
             try {
                 return ((IStorageEditorInput) input).getStorage();
             } catch (CoreException e) {
                 Util.log(e, "Failure getting Editor input storage for " + this);
             }
         }
         return null;
     }
     
     
 
     public SourceManagerFactory getSourceManagerFactory(final boolean updateDocumentIfPossible){
         return getSourceManagerFactory(updateDocumentIfPossible, getSite().getShell(), this);
     }
     
     /**
      * @return The source manager factory.
      */
     public static SourceManagerFactory getSourceManagerFactory(boolean notUsed, final Shell shell, final CALEditor calEditor){
 
         return new CALModelManager.SourceManagerFactory() {
             public ISourceManager getSourceManager(ModuleName name) {
                 return new SourceManager(shell);
             }                                
         };
     }
     
     private static class SourceManager implements ModuleContainer.ISourceManager, ModuleContainer.ISourceManager2{
         private final Map<ModuleName, IStorage> moduleNameToIStorage = new HashMap<ModuleName, IStorage>();
         private final Shell shell;
         private final CALDocumentProvider documentProvider = CALEclipseUIPlugin.getDefault().getCALDocumentProvider();
         private DocumentRewriteSession documentRewriteSession = null;
 
         SourceManager(Shell shell){
             this.shell = shell;
         }
         
         private IStorage getStorage(ModuleName name){
             IStorage storage = moduleNameToIStorage.get(name);
             if (storage == null){
                 storage = calModelManager.getInputSourceFile(name);
                 moduleNameToIStorage.put(name, storage);
             }
             return storage;
         }
         
         public boolean isWriteable(ModuleName name){
             IStorage storage = getStorage(name);
             if (storage == null){
                 return false;
             }
             
             if (storage instanceof JarEntryFile) {
                 return false;
             }
             
             IFile file = (IFile) storage;
 
             if (file.isReadOnly()){
                 // give the user an opportunity to check out the file or make it writeable
                 RepositoryProvider repositoryProvider = RepositoryProvider.getProvider(file.getProject());
                 if (repositoryProvider == null){
                     String message = MessageFormat.format(ActionMessages.ReadOnlyFileEncountered_message, new Object[] { file.getName() });
                     if (MessageDialog.openQuestion(shell, ActionMessages.ReadOnlyFileEncountered_title, message)){
                         ResourceAttributes attributes = file.getResourceAttributes();
                         if (attributes != null){
                             attributes.setReadOnly(false);
                             try {
                                 file.setResourceAttributes(attributes);
                             } catch (CoreException e) {
                             }
                         }
                     }
                 }
                 else{
                     IFileModificationValidator fmv = repositoryProvider.getFileModificationValidator();
                     IFile iFiles[] = new IFile[1];
                     iFiles[0] = file;
                     fmv.validateEdit(iFiles, shell);
                 }
             }
 
             return !file.isReadOnly();
         }
 
         private PartiallySynchronizedDocument getDocument(ModuleName name){
             final IStorage istorage = calModelManager.getInputSourceFile(name);
             if (istorage instanceof IFile){
                 final IEditorInput input = new FileEditorInput((IFile) istorage);
                 if (input != null) {
                     return (PartiallySynchronizedDocument) documentProvider.getDocument(input);
                 }
             }
             return null;
         }
 
         public void startUpdate(ModuleName moduleName){
             PartiallySynchronizedDocument psd = getDocument(moduleName);
             if (psd.getActiveRewriteSession() == null){
                 documentRewriteSession = psd.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
             }
         }
         
         public void endUpdate(ModuleName moduleName){
             if (documentRewriteSession != null){
                 PartiallySynchronizedDocument psd = getDocument(moduleName);
                 psd.stopRewriteSession(documentRewriteSession);
                 documentRewriteSession = null;
             }
         }
         
         public boolean canUpdateIncrementally(ModuleName moduleName){
             return getDocument(moduleName) != null;
         }
         
         public String getSource(ModuleName name, CompilerMessageLogger messageLogger) {
             final IDocument document = getDocument(name);
             if (document != null){
                 return document.get();
             }
             
             return CALModelManager.getCALModelManager().getModuleSource(name);
         }
         
         public boolean saveSource(final ModuleName name, final String moduleDefinition, org.openquark.cal.services.Status saveStatus){
             if (!isWriteable(name)){
                 return false;
             }
 
             final IDocument document = getDocument(name);
             if (document != null){
                 document.set(moduleDefinition);
                 return true;
             }
             
             // no document so hit the disk
             {
                 IStorage storage = getStorage(name);
                 if (storage == null){
                     return false;
                 }
                 if (! (storage instanceof IFile)) {
                     return false;
                 }
                 IFile file = (IFile) storage;
 
                 final StringReader contents = new StringReader(moduleDefinition);
                 InputStream source = 
                     new InputStream(){
                         private Reader reader = contents;
 
                         @Override
                         public int read() throws IOException {
                             return reader.read();                                  
                         }                                    
                 };
 
                 try {
                     file.setContents(source, false, true, null);
                 } catch (CoreException e) {
                     return false;
                 }
                 return true;
             }
         }
 
         public Pair<Integer, Integer> getLineAndColumn(final ModuleName name, final int offsetOneBased){
             try {
                 IDocument document = getDocument(name);
                 final int line = document.getLineOfOffset(offsetOneBased - 1);
                 final int col = CoreUtility.getColumn(line, offsetOneBased - 1, document);
                 return new Pair<Integer, Integer>(line+1, col+1);
             } catch (BadLocationException e) {
                 return null;
             }
         }
 
         public int getOffset(final ModuleName name, final SourcePosition position){
             try {
                 IDocument document = getDocument(name);
                 return CoreUtility.toOffset(position, document) + 1;
             } catch (BadLocationException e) {
                 return -1;
             }
         }
         
         public boolean saveSource(final ModuleName name, final int startIndex, final int endIndex, final String newText) {
             if (!isWriteable(name)){
                 return false;
             }
 
             final IDocument document = getDocument(name);
             if (document != null){
                 final int offset = startIndex;
                 final int length = endIndex - startIndex;
                 try {
                     document.replace(offset - 1, length, newText);
                 } catch (BadLocationException e) {
                     assert false;
                     return false;
                 }
                 return true;
             }
             
             // This only works for things open in the editor. For current use this is always true.
             assert false;
             return false;
         }
     }
     
 }
