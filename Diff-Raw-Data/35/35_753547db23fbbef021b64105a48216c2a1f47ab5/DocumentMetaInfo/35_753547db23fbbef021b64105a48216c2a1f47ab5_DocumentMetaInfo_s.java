 package ch.sbs.plugin.preptools;
 
 import java.net.URL;
 import java.util.Map;
 
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
 import ch.sbs.utils.preptools.Match;
 
 /**
  * Keeps meta information about a document known to the plugin.
  * One DocumentMetaInfo object is maintained per document open in oXygen.
  * 
  */
 class DocumentMetaInfo {
 
 	static class MetaInfo {
 		private boolean hasStarted;
 		private boolean isDone;
 
 		/**
 		 * True when started processing but isn't done yet.
 		 */
 		public boolean isProcessing() {
 			return hasStarted && !isDone;
 		}
 
 		/**
 		 * True when started processing.
 		 */
 		public void setHasStarted(boolean theHasStarted) {
 			hasStarted = theHasStarted;
 		}
 
 		/**
 		 * True when started processing.
 		 */
 		public boolean hasStarted() {
 			return hasStarted;
 		}
 
 		public void setDone(boolean theIsDone) {
 			isDone = theIsDone;
 		}
 
 		/**
 		 * True when done processing.
 		 */
 		public void done() {
 			setDone(true);
 		}
 
 		/**
 		 * True when done processing.
 		 */
 		public boolean isDone() {
 			return isDone;
 		}
 
 	}
 
 	final Map<String, MetaInfo> toolSpecific;
 
 	public MetaInfo getToolSpecificMetaInfo(final String label) {
 		return toolSpecific.get(label);
 	}
 
 	// general
 	private boolean isDtBook;
 	private boolean lastEditWasManual;
 	private String currentEditorPage;
 	private WSTextEditorPage page;
 	private Document document;
 	private final URL url;
 	private DocumentListener documentListener;
 	private Match.PositionMatch currentPositionMatch;
 	private PrepTool currentPrepTool;
 
 	public DocumentMetaInfo(
 			final PrepToolsPluginExtension theWorkspaceAccessPluginExtension) {
 		setPageAndDocument(theWorkspaceAccessPluginExtension);
 		toolSpecific = theWorkspaceAccessPluginExtension
 				.getToolSpecificMetaInfos(document);
 		setCurrentEditorPage(theWorkspaceAccessPluginExtension.getPageId());
 		url = theWorkspaceAccessPluginExtension.getEditorLocation();
 	}
 
 	/**
 	 * This method normally shouldn't be called from outside. The only case is
 	 * where the user accidentally closed the document which was still being
 	 * processed and he wants to take up again where he left off.
 	 * 
 	 * @param theWorkspaceAccessPluginExtension
 	 */
 	public void setPageAndDocument(
 			final PrepToolsPluginExtension theWorkspaceAccessPluginExtension) {
 		page = theWorkspaceAccessPluginExtension.getPage();
 
 		setDocument(page.getDocument());
 	}
 
 	/**
 	 * 
 	 * @return current editor page (i.e. "Text", "Grid", "Author")
 	 */
 	public String getCurrentEditorPage() {
 		return currentEditorPage;
 	}
 
 	/**
 	 * Sets current editor page.
 	 * 
 	 * @param theCurrentEditorPage
 	 */
 	public void setCurrentEditorPage(final String theCurrentEditorPage) {
 		currentEditorPage = theCurrentEditorPage;
 	}
 
 	/**
 	 * 
 	 * @return True if current document is a dtbook.
 	 */
 	public boolean isDtBook() {
 		return isDtBook;
 	}
 
 	/**
 	 * Sets document.
 	 * 
 	 * @param theDocument
 	 */
 	private void setDocument(final Document theDocument) {
 		document = theDocument;
 		documentListener = new DocumentListener() {
 
 			@Override
 			public void removeUpdate(DocumentEvent e) {
 				lastEditWasManual = true;
 			}
 
 			@Override
 			public void insertUpdate(DocumentEvent e) {
 				lastEditWasManual = true;
 			}
 
 			@Override
 			public void changedUpdate(DocumentEvent e) {
 				lastEditWasManual = true;
 			}
 		};
 		document.addDocumentListener(documentListener);
 		isDtBook = isDtBook(document);
 	}
 
 	/**
 	 * 
 	 * @param document
 	 * @return True if given document is a dtbook.
 	 */
 	private static boolean isDtBook(final Document document) {
 		try {
 			return document.getText(0, document.getLength())
 					.contains("<dtbook");
 		} catch (final BadLocationException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 * Cleans up.
 	 */
 	public void finish() {
 		document.removeDocumentListener(documentListener);
 	}
 
 	/**
 	 * accessor for url
 	 * 
 	 * @return url
 	 */
 	public URL getUrl() {
 		return url;
 	}
 
 	/**
 	 * oxygen, or rather Swing specific.
 	 * Sets current position match.
 	 * 
 	 * @param theCurrentPositionMatch
 	 */
 	public void setCurrentPositionMatch(
 			final Match.PositionMatch theCurrentPositionMatch) {
 		lastEditWasManual = false;
 		currentPositionMatch = theCurrentPositionMatch;
 	}
 
 	/**
 	 * oxygen, or rather Swing specific.
 	 * Gets current position match.
 	 * 
 	 * @return
 	 */
 	public Match.PositionMatch getCurrentPositionMatch() {
 		return currentPositionMatch;
 	}
 
 	/**
 	 * last edit was manual.
 	 * 
 	 */
 	public void setManualEdit() {
 		lastEditWasManual = true;
 	}
 
 	/**
 	 * last edit was not manual.
 	 * 
 	 */
 	public void resetManualEdit() {
 		lastEditWasManual = false;
 	}
 
 	/**
 	 * @return True if last edit was manual.
 	 */
 	public boolean manualEditOccurred() {
 		return lastEditWasManual;
 	}
 
 	/**
 	 * @return the current preptool
 	 */
 	public PrepTool getCurrentPrepTool() {
 		return currentPrepTool;
 	}
 
 	/**
 	 * Sets current preptool.
 	 * 
 	 * @param theCurrentPrepTool
 	 */
 	public void setCurrentPrepTool(final PrepTool theCurrentPrepTool) {
 		currentPrepTool = theCurrentPrepTool;
 	}
 
 	public Document getDocument() {
 		return document;
 	}
 
 	public void setCurrentState() {
 		currentPrepTool.setCurrentState(this);
 	}
 
 	public MetaInfo getCurrentToolSpecificMetaInfo() {
 		if (currentPrepTool == null) {
 			return null;
 		}
 		final String label = currentPrepTool.getLabel();
 		return getToolSpecificMetaInfo(label);
 	}
 
 	public boolean isProcessing() {
 		final MetaInfo currentToolSpecificMetaInfo = getCurrentToolSpecificMetaInfo();
 		return currentToolSpecificMetaInfo != null
 				&& currentToolSpecificMetaInfo.isProcessing();
 	}
 
 	public boolean hasStarted() {
 		final MetaInfo currentToolSpecificMetaInfo = getCurrentToolSpecificMetaInfo();
 		return currentToolSpecificMetaInfo != null
 				&& getCurrentToolSpecificMetaInfo().hasStarted();
 	}
 
 	public void setHasStarted(boolean theHasStarted) {
 		final MetaInfo currentToolSpecificMetaInfo = getCurrentToolSpecificMetaInfo();
 		if (currentToolSpecificMetaInfo != null) {
 			getCurrentToolSpecificMetaInfo().setHasStarted(theHasStarted);
 		}
 	}
 
 	public boolean isDone() {
 		final MetaInfo currentToolSpecificMetaInfo = getCurrentToolSpecificMetaInfo();
 		return currentToolSpecificMetaInfo != null
 				&& getCurrentToolSpecificMetaInfo().isDone();
 	}
 
 	public void setDone(boolean theIsDone) {
 		final MetaInfo currentToolSpecificMetaInfo = getCurrentToolSpecificMetaInfo();
 		if (currentToolSpecificMetaInfo != null) {
 			getCurrentToolSpecificMetaInfo().setDone(theIsDone);
 		}
 	}
 }
