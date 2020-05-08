 package org.eclipse.dltk.console.ui.internal;
 
 import org.eclipse.dltk.console.ScriptConsoleConstants;
 import org.eclipse.dltk.console.ui.ScriptConsole;
 import org.eclipse.dltk.console.ui.internal.actions.CloseScriptConsoleAction;
 import org.eclipse.dltk.console.ui.internal.actions.SaveConsoleSessionAction;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.console.IConsoleConstants;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.console.TextConsolePage;
 import org.eclipse.ui.console.TextConsoleViewer;
 import org.eclipse.ui.console.actions.TextViewerAction;
 
 
 public class ScriptConsolePage extends TextConsolePage implements
 		IScriptConsoleContentHandler {
 
 	protected class ContentAssistProposalsAction extends TextViewerAction {
 
 		public ContentAssistProposalsAction(ITextViewer viewer) {
 			super(viewer, ISourceViewer.CONTENTASSIST_PROPOSALS);
 		}
 	}
 
 	protected class ContentAssistContextInfoAction extends TextViewerAction {
 		public ContentAssistContextInfoAction(ITextViewer viewer) {
 			super(viewer, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
 		}
 	}
 
 	private SourceViewerConfiguration cfg;
 
 	private ScriptConsoleViewer viewer;
 
 	private TextViewerAction proposalsAction;
 
 	protected void createActions() {
 		super.createActions();
 
 		proposalsAction = new ContentAssistProposalsAction(getViewer());
 		proposalsAction = new ContentAssistProposalsAction(getViewer());
 
 		SaveConsoleSessionAction saveSessionAction = new SaveConsoleSessionAction(
 				(ScriptConsole) getConsole(),
 				ScriptConsoleMessages.SaveSessionAction,
 				ScriptConsoleMessages.SaveSessionTooltip);
 
		CloseScriptConsoleAction colseConsoleAction = new CloseScriptConsoleAction(
 				(ScriptConsole) getConsole(),
 				ScriptConsoleMessages.TerminateConsoleAction,
 				ScriptConsoleMessages.TerminateConsoleTooltip);
 
 		IActionBars bars = getSite().getActionBars();
 
 		IToolBarManager toolbarManager = bars.getToolBarManager();
 
 		toolbarManager.prependToGroup(IConsoleConstants.LAUNCH_GROUP,
 				new GroupMarker(ScriptConsoleConstants.SCRIPT_GROUP));
 		toolbarManager.appendToGroup(ScriptConsoleConstants.SCRIPT_GROUP,
 				new Separator());
 
 		toolbarManager.appendToGroup(ScriptConsoleConstants.SCRIPT_GROUP,
				colseConsoleAction);
 		
 		toolbarManager.appendToGroup(ScriptConsoleConstants.SCRIPT_GROUP,
 				saveSessionAction);
 
 		bars.updateActionBars();
 	}
 
 	protected TextConsoleViewer createViewer(Composite parent) {
 		viewer = new ScriptConsoleViewer(parent, (ScriptConsole) getConsole(),
 				this);
 		viewer.configure(cfg);
 		return viewer;
 	}
 
 	public ScriptConsolePage(ScriptConsole console, IConsoleView view,
 			SourceViewerConfiguration cfg) {
 		super(console, view);
 
 		this.cfg = cfg;
 	}
 
 	public void clearConsolePage() {
 		viewer.clear();
 	}
 
 	public void contentAssistRequired() {
 		proposalsAction.run();
 	}
 
 	public void insertText(String text) {
 		viewer.insertText(text);
 	}
 }
