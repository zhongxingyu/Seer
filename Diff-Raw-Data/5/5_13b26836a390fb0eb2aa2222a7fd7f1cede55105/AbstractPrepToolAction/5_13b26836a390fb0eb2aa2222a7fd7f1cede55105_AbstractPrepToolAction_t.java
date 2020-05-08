 package ch.sbs.plugin.preptools;
 
 import java.awt.event.ActionEvent;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.AbstractAction;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import ro.sync.exml.workspace.api.editor.WSEditor;
 import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
 import ch.sbs.utils.preptools.DocumentUtils;
 import ch.sbs.utils.preptools.FileUtils;
 import ch.sbs.utils.preptools.MarkupUtil;
 import ch.sbs.utils.preptools.Match;
 import ch.sbs.utils.preptools.Match.PositionMatch;
 import ch.sbs.utils.preptools.MetaUtils;
 import ch.sbs.utils.preptools.RegionSkipper;
 import ch.sbs.utils.preptools.parens.ParensUtil;
 
 @SuppressWarnings("serial")
 abstract class AbstractPrepToolAction extends AbstractAction {
 
 	// common names for actions:
 	public static final String START = "Start";
 	public static final String FIND = "Find";
 	public static final String CHANGE = "Change";
 
 	protected final PrepToolsPluginExtension prepToolsPluginExtension;
 
 	/**
 	 * @param thePrepToolsPluginExtension
 	 * @param theName
 	 *            the name of this action.
 	 */
 	AbstractPrepToolAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			String theName) {
 		prepToolsPluginExtension = thePrepToolsPluginExtension;
 		NAME = theName;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(final ActionEvent arg0) {
 		if ((prepToolsPluginExtension.getPage()) != null) {
 			final DocumentMetaInfo dmi = prepToolsPluginExtension
 					.getDocumentMetaInfo();
 			final Document document = dmi.getDocument();
 
 			OxygenEditGrouper.perform(document, new OxygenEditGrouper.Edit() {
 				@Override
 				public void edit() {
 
 					try {
 						doSomething();
 					} catch (final RuntimeException e) {
 						prepToolsPluginExtension
 								.showDialog("RuntimeException occurred: " + e
 										+ " " + getStackTrace(e));
 					} catch (final BadLocationException e) {
 						prepToolsPluginExtension.showDialog(e.getMessage()
 								+ " " + getStackTrace(e));
 						throw new RuntimeException(e);
 					}
 
 				}
 			});
 			dmi.setCurrentState();
 			if (dmi.isDone()) {
 				if (!dmi.isCancelled()) {
 					wrapUp(document);
 					prepToolsPluginExtension.showDialog("You're done with "
 							+ getProcessName() + "!");
 
 					// TODO: once the schema has been upgraded, set this flag to
 					// true (or remove the if)
 					final boolean FEATURE_1205 = false;
 					if (FEATURE_1205) {
 						OxygenEditGrouper.perform(document,
 								new OxygenEditGrouper.Edit() {
 									@Override
 									public void edit() {
 
 										MetaUtils.insertPrepToolInfo(
 												prepToolsPluginExtension
 														.getDocumentMetaInfo()
 														.getDocument(),
 												getProcessName());
 
 									}
 								});
 					}
 				}
 				prepToolsPluginExtension.chooseNextUncompletedPrepTool();
 			}
 		}
 	}
 
 	private void wrapUp(final Document document) {
 
 		OxygenEditGrouper.perform(document, new OxygenEditGrouper.Edit() {
 			@Override
 			public void edit() {
 
 				doWrapUp();
 
 			}
 
 		});
 
 	}
 
 	/**
 	 * Hook thet gets called just before meta-information is inserted into the
 	 * document. This code is executed as a single edit, so it can be undone by
 	 * the user as a single edit step.
 	 */
 	protected void doWrapUp() {
 
 	}
 
 	private String getStackTrace(final Exception e) {
 		final StringWriter sw = new StringWriter();
 		e.printStackTrace(new PrintWriter(sw));
 		return sw.toString();
 	}
 
 	/**
 	 * Compulsory hook that gets called only when editor, page, document, text
 	 * have successfully been retrieved.
 	 * 
 	 * @throws BadLocationException
 	 */
 	protected abstract void doSomething() throws BadLocationException;
 
 	/**
 	 * Utility method that returns the end position of the current selection.
 	 * 
 	 * @return The end position of the current selection.
 	 */
 	protected final int getSelectionEnd() {
 		final WSEditor editorAccess = prepToolsPluginExtension.getWsEditor();
 		final WSTextEditorPage aWSTextEditorPage = PrepToolsPluginExtension
 				.getPage(editorAccess);
 		return aWSTextEditorPage.getSelectionEnd();
 	}
 
 	/**
 	 * @return
 	 */
 	protected int getStartIndex() {
 		final Document document = prepToolsPluginExtension
 				.getDocumentMetaInfo().getDocument();
 		try {
 			final String bookTag = "<book";
 			final int indexOf = document.getText(0, document.getLength())
 					.indexOf(bookTag);
 			if (indexOf < 0) {
 				final String message = "\"" + bookTag
 						+ "\" not found in document!";
 				prepToolsPluginExtension.showMessage(message);
 				throw new RuntimeException(message);
 			}
 			return indexOf;
 		} catch (final BadLocationException e) {
 			prepToolsPluginExtension.showMessage(e.getMessage());
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Subclasses call this method to notify completion of process.
 	 */
 	protected void done() {
 		prepToolsPluginExtension.getDocumentMetaInfo().setDone(true);
 	}
 
 	/**
 	 * Compulsory hook for subclasses.
 	 * 
 	 * @return
 	 */
 	protected abstract String getProcessName();
 
 	/**
 	 * @return
 	 */
 	public String getName() {
 		return NAME;
 	}
 
 	private final String NAME;
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupAction extends AbstractPrepToolAction {
 
 	AbstractMarkupAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName) {
 		super(thePrepToolsPluginExtension, theName);
 	}
 
 	/**
 	 * Utility method to search on in document starting at startAt.
 	 * 
 	 * @param aWSTextEditorPage
 	 * @param editorAccess
 	 * @param startAt
 	 * 
 	 * @throws BadLocationException
 	 */
 	protected void searchOn(final WSTextEditorPage aWSTextEditorPage,
 			final WSEditor editorAccess, final int startAt)
 			throws BadLocationException {
 		final Document document = aWSTextEditorPage.getDocument();
 		final String newText = document.getText(0, document.getLength());
 		final DocumentMetaInfo dmi = prepToolsPluginExtension
 				.getDocumentMetaInfo(editorAccess.getEditorLocation());
 		// Possibly improve this: don't create a skipper for every searchOn
 		// call.
 		// Instead save the skipper in DocumentMetaInfo. However, make sure
 		// it is properly synced with changes occurring in the document, also
 		// via Undo!
 		final RegionSkipper skipper = prepToolsPluginExtension.makeSkipper();
 		final String customPattern = getCustomSkipPatternToAdd();
 		if (customPattern != null) {
 			skipper.addPattern(customPattern);
 		}
 		final Match match = new MarkupUtil(skipper).find(newText, startAt,
 				getPattern());
 		if (match.equals(Match.NULL_MATCH)) {
 			match.startOffset = 0;
 			match.endOffset = 0;
 			done();
 		}
 		dmi.setCurrentState();
 		prepToolsPluginExtension.select(match.startOffset, match.endOffset);
 		dmi.setCurrentPositionMatch(new Match.PositionMatch(document, match));
 	}
 
 	/**
 	 * Optional hook for subclasses to add a skipper pattern.
 	 * 
 	 * @return the pattern to skip
 	 */
 	protected String getCustomSkipPatternToAdd() {
 		return null;
 	}
 
 	/**
 	 * Compulsory hook for subclasses.
 	 * 
 	 * @return pattern for which to search
 	 */
 	abstract protected Pattern getPattern();
 
 	protected DocumentMetaInfo.MetaInfo getMetaInfo() {
 		return prepToolsPluginExtension.getDocumentMetaInfo()
 				.getCurrentToolSpecificMetaInfo();
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupStartAction extends AbstractMarkupAction {
 
 	AbstractMarkupStartAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName) {
 		super(thePrepToolsPluginExtension, theName);
 	}
 
 	@Override
 	protected void doSomething() throws BadLocationException {
 		final WSEditor editorAccess = prepToolsPluginExtension.getWsEditor();
 		final WSTextEditorPage aWSTextEditorPage = prepToolsPluginExtension
 				.getPage();
 		final URL editorLocation = editorAccess.getEditorLocation();
 		final DocumentMetaInfo.MetaInfo metaInfo = getMetaInfo();
 		if (metaInfo.isDone()) {
 			if (prepToolsPluginExtension.showConfirmDialog(getProcessName()
 					+ ": Start Over?",
 					"The document " + FileUtils.basename(editorLocation)
 							+ " has already been " + getProcessName()
 							+ "ed.\n Do you want to start over?")
 
 			) {
 				metaInfo.setCancelled(false);
 				metaInfo.setDone(false);
 			}
 			else {
 				metaInfo.setCancelled(true);
 				return;
 			}
 		}
 		else if (metaInfo.hasStarted()) {
 			if (prepToolsPluginExtension.showConfirmDialog(getProcessName()
 					+ ": Start Over?",
 					"The document " + FileUtils.basename(editorLocation)
 							+ " is currently being " + getProcessName()
 							+ "ed.\n Do you want to start over?")
 
 			) {
 				metaInfo.setDone(false);
 			}
 			else {
 				return;
 			}
 		}
 
 		metaInfo.setHasStarted(true);
 		metaInfo.setDone(false);
 		searchOn(aWSTextEditorPage, editorAccess, getStartIndex());
 	}
 
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupProceedAction extends AbstractMarkupAction {
 
 	AbstractMarkupProceedAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName) {
 		super(thePrepToolsPluginExtension, theName);
 	}
 
 	/**
 	 * 
 	 * Compulsory hook to be implemented by subclasses. Handles selected text
 	 * and returns
 	 * position where to continue with search.
 	 * 
 	 * @param document
 	 *            The document.
 	 * @param selText
 	 *            The current selection.
 	 * @return The position where to continue with search.
 	 * @throws BadLocationException
 	 */
 	protected abstract int handleText(final Document document,
 			final String selText) throws BadLocationException;
 
 	/**
 	 * Optional hook to be implemented by subclasses. If true the process is
 	 * aborted.
 	 * 
 	 * @param selText
 	 * @return True if the process is to be aborted.
 	 */
 	protected boolean abortIfSelectionChanged(final String selText) {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.AbstractVFormAction#doSomething(ro.sync.exml.workspace.api.editor.WSEditor, ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage, javax.swing.text.Document, ch.sbs.plugin.preptools.DocumentMetaInfo)
 	 */
 	@Override
 	protected void doSomething() throws BadLocationException {
 		final WSTextEditorPage aWSTextEditorPage = prepToolsPluginExtension
 				.getPage();
 
 		if (abortIfSelectionChanged(aWSTextEditorPage.getSelectedText()))
 			return;
 
 		if (handleManualCursorMovement()) {
 			return;
 		}
 
 		final int continueAt = handleText(aWSTextEditorPage.getDocument(),
 				aWSTextEditorPage.getSelectedText());
 
		final int startIndex = Math.max(continueAt, getStartIndex());

 		searchOn(aWSTextEditorPage, prepToolsPluginExtension.getWsEditor(),
				startIndex);
 	}
 
 	/**
 	 * Utility method to handle user's manual cursor movement.
 	 * 
 	 * @param aWSTextEditorPage
 	 * @param dmi
 	 */
 	private boolean handleManualCursorMovement() {
 		final DocumentMetaInfo dmi = prepToolsPluginExtension
 				.getDocumentMetaInfo();
 		final WSTextEditorPage aWSTextEditorPage = prepToolsPluginExtension
 				.getPage();
 		lastMatchStart = aWSTextEditorPage.getSelectionStart();
 		lastMatchEnd = aWSTextEditorPage.getSelectionEnd();
 		final Match.PositionMatch pm = dmi.getCurrentPositionMatch();
 		if (lastMatchStart != pm.startOffset.getOffset()
 				|| lastMatchEnd != pm.endOffset.getOffset()
 				|| dmi.manualEditOccurred()) {
 			if (prepToolsPluginExtension.showConfirmDialog(getProcessName()
 					+ ": Cursor", "Cursor position has changed!\n",
 					"Take up where we left off last time", "continue anyway")) {
 				prepToolsPluginExtension.select(
 						lastMatchStart = pm.startOffset.getOffset(),
 						lastMatchEnd = pm.endOffset.getOffset());
 				return breakIfManualEditOccurred();
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Optional hook to abort process when manual edit occurred.
 	 */
 	protected boolean breakIfManualEditOccurred() {
 		return false;
 	}
 
 	protected int lastMatchStart;
 	protected int lastMatchEnd;
 
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupChangeAbortIfSelectionChangedAction extends
 		AbstractMarkupProceedAction {
 
 	AbstractMarkupChangeAbortIfSelectionChangedAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName) {
 		super(thePrepToolsPluginExtension, theName);
 	}
 
 	@Override
 	protected boolean abortIfSelectionChanged(final String selText) {
 		return (selText == null || !MarkupUtil.matches(selText, getPattern()));
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupChangeAction extends
 		AbstractMarkupChangeAbortIfSelectionChangedAction {
 
 	private final String FULL_OPENING_TAG;
 	private final String FULL_CLOSING_TAG;
 
 	AbstractMarkupChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String theTagToInsert) {
 		super(thePrepToolsPluginExtension, theName);
 		// moved initialization of these up here, to "cache" the result, since
 		// getClosingTag is potentially costly (see Bug
 		// http://redmine.sbszh.ch/issues/show/1259)
 		FULL_OPENING_TAG = "<" + theTagToInsert + ">";
 		FULL_CLOSING_TAG = "</" + MarkupUtil.getClosingTag(theTagToInsert)
 				+ ">";
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.ProceedAction#handleText(javax.swing.text.Document, java.lang.String)
 	 */
 	@Override
 	protected int handleText(final Document document, final String selText)
 			throws BadLocationException {
 		// starting with the end, so the start position doesn't shift
 		document.insertString(lastMatchEnd, FULL_CLOSING_TAG, null);
 		document.insertString(lastMatchStart, FULL_OPENING_TAG, null);
 
 		final int continueAt = lastMatchStart + FULL_OPENING_TAG.length()
 				+ FULL_CLOSING_TAG.length() + selText.length();
 		return continueAt;
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupFullRegexChangeAction extends
 		AbstractMarkupChangeAbortIfSelectionChangedAction {
 
 	private final String replaceString;
 
 	AbstractMarkupFullRegexChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, theName);
 		replaceString = theReplaceString;
 	}
 
 	protected String getReplaceString() {
 		return replaceString;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.ProceedAction#handleText(javax.swing.text.Document, java.lang.String)
 	 */
 	@Override
 	protected int handleText(final Document document, final String selText)
 			throws BadLocationException {
 		if (selText == null) {
 			return lastMatchStart;
 		}
 		final String newText = getPattern().matcher(selText).replaceAll(
 				replaceString);
 		document.remove(lastMatchStart, selText.length());
 		document.insertString(lastMatchStart, newText, null);
 
 		return lastMatchStart + newText.length();
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractMarkupFindAction extends AbstractMarkupProceedAction {
 
 	AbstractMarkupFindAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName) {
 		super(thePrepToolsPluginExtension, theName);
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.ProceedAction#handleText(javax.swing.text.Document, java.lang.String)
 	 */
 	@Override
 	protected int handleText(final Document document, final String selText)
 			throws BadLocationException {
 		return getSelectionEnd();
 	}
 }
 
 class RegexHelper {
 
 	private final Pattern pattern;
 	private final String processName;
 
 	RegexHelper(final String thePattern, final String theProcessName) {
 		pattern = Pattern.compile(thePattern);
 		processName = theProcessName;
 	}
 
 	public Pattern getPattern() {
 		return pattern;
 	}
 
 	public String getProcessName() {
 		return processName;
 	}
 
 }
 
 @SuppressWarnings("serial")
 class RegexStartAction extends AbstractMarkupStartAction {
 	private final RegexHelper helper;
 
 	RegexStartAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName) {
 		super(thePrepToolsPluginExtension, theName);
 		helper = new RegexHelper(thePattern, theProcessName);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class RegexChangeAction extends AbstractMarkupChangeAction {
 	private final RegexHelper helper;
 
 	RegexChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName, final String theTagToInsert) {
 		super(thePrepToolsPluginExtension, theName, theTagToInsert);
 		helper = new RegexHelper(thePattern, theProcessName);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class FullRegexChangeAction extends AbstractMarkupFullRegexChangeAction {
 	private final RegexHelper helper;
 
 	FullRegexChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, theName, theReplaceString);
 		helper = new RegexHelper(thePattern, theProcessName);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class AccentChangeAction extends FullRegexChangeAction {
 
 	private static final String PLACE_HOLDER = "_";
 	private static final String REGEX_SPAN_TEMPLATE = "<span\\s+brl:accents\\s*=\\s*\""
 			+ PLACE_HOLDER + "\"\\s*>(.*?)</span\\s*>";
 	public static final String REGEX_SPAN_REDUCED = REGEX_SPAN_TEMPLATE
 			.replace(PLACE_HOLDER, "reduced");
 
 	public static final String REGEX_SPAN_DETAILED = REGEX_SPAN_TEMPLATE
 			.replace(PLACE_HOLDER, "detailed");
 
 	public static final String REPLACE = "$1";
 
 	AccentChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, theName, thePattern, theProcessName,
 				theReplaceString);
 	}
 
 	@Override
 	protected int handleText(final Document document, final String selText)
 			throws BadLocationException {
 		if (selText == null) {
 			return lastMatchStart;
 		}
 		final String newText = getPattern().matcher(selText).replaceAll(
 				getReplaceString());
 
 		DocumentUtils.performReplacement(document, "\\b" + selText + "\\b",
 				newText);
 
 		return lastMatchStart + newText.length();
 	}
 
 	@Override
 	protected boolean breakIfManualEditOccurred() {
 		return true;
 	}
 
 	// Override abortIfSelectionChanged behaviour from intermediate
 	// superclasses. We don't want to silently abort the process when the cursor
 	// has been manually moved, because we deal with that case by re-setting the
 	// cursor to the last position.
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.AbstractMarkupChangeAbortIfSelectionChangedAction#abortIfSelectionChanged(java.lang.String)
 	 */
 	@Override
 	protected boolean abortIfSelectionChanged(final String selText) {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.AbstractPrepToolAction#doWrapUp()
 	 */
 	@Override
 	protected void doWrapUp() {
 		DocumentUtils.performReplacement(prepToolsPluginExtension
 				.getDocumentMetaInfo().getDocument(), REGEX_SPAN_DETAILED,
 				REPLACE);
 	}
 
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractChangeAction extends FullRegexChangeAction {
 
 	AbstractChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName) {
 		this(thePrepToolsPluginExtension, theName, thePattern, theProcessName,
 				null);
 	}
 
 	AbstractChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, theName, thePattern, theProcessName,
 				theReplaceString);
 	}
 
 	@Override
 	protected boolean breakIfManualEditOccurred() {
 		return true;
 	}
 
 	@Override
 	protected int handleText(final Document document, final String selText)
 			throws BadLocationException {
 		if (selText == null) {
 			return lastMatchStart;
 		}
 		final String newText = performReplacement(getPattern(), selText);
 
 		DocumentUtils.performSingleReplacement(document, selText, newText,
 				lastMatchStart);
 
 		return lastMatchStart + newText.length();
 	}
 
 	/**
 	 * Compulsory hook for subclasses to perform replacement of thePattern on
 	 * selText.
 	 * 
 	 * @param thePattern
 	 * @param selText
 	 * @return
 	 */
 	protected abstract String performReplacement(final Pattern thePattern,
 			final String selText);
 
 }
 
 @SuppressWarnings("serial")
 class OrdinalChangeAction extends AbstractChangeAction {
 
 	OrdinalChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName) {
 		this(thePrepToolsPluginExtension, theName, thePattern, theProcessName,
 				null);
 	}
 
 	OrdinalChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, theName, thePattern, theProcessName,
 				theReplaceString);
 	}
 
 	/**
 	 * Applies the given pattern to the given input and replaces the matches
 	 * according to http://redmine.sbszh.ch/issues/1416
 	 * 
 	 * @param pattern
 	 * @param input
 	 * @return
 	 */
 	public static String feature1416(final Pattern pattern, final String input) {
 		final Matcher matcher = pattern.matcher(input);
 		matcher.find();
 		String replacement = "<brl:num role=\"ordinal\">$1</brl:num>";
 		// We check for group(2).length() because the second capturing group in
 		// PrepToolLoader.ORDINAL_SEARCH_REGEX (\s*) matches the empty string
 		// and thus can never be null, see
 		// http://download.oracle.com/javase/1.5.0/docs/api/java/util/regex/Matcher.html#group%28%29
 		if (matcher.group(2).length() != 0) {
 			if (matcher.group(3) == null) {
 				replacement += "&nbsp;";
 			}
 			else {
 				replacement += matcher.group(2);
 			}
 		}
 		// We check for group(3) != null because the third capturing group in
 		// PrepToolLoader.ORDINAL_SEARCH_REGEX ((</|&nbsp;)?) does not match the
 		// empty string and thus can be null, see
 		// http://download.oracle.com/javase/1.5.0/docs/api/java/util/regex/Matcher.html#group%28%29
 		if (matcher.group(3) != null) {
 			replacement += matcher.group(3);
 		}
 		return matcher.replaceAll(replacement);
 	}
 
 	@Override
 	protected String performReplacement(final Pattern thePattern,
 			final String selText) {
 		return feature1416(thePattern, selText);
 	}
 }
 
 /**
  * (@see <a href="http://redmine.sbszh.ch/issues/1443">Bug 1443</a>)
  * This pattern is used for the Abbrev-Start-, -Find-, and Change-Actions to
  * allow them to plug in an additional pattern to skip, namely to avoid matching
  * text within a p element, particularly:
  * <p class="sourcePublisher">
  */
 interface Bug1443 {
 	public static final String CUSTOM_PATTERN = "<p[^>]*>";
 }
 
 @SuppressWarnings("serial")
 class AbbrevChangeAction extends AbstractChangeAction {
 
 	AbbrevChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName) {
 		this(thePrepToolsPluginExtension, theName, thePattern, theProcessName,
 				null);
 	}
 
 	AbbrevChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName, final String theReplaceString) {
 		super(thePrepToolsPluginExtension, theName, thePattern, theProcessName,
 				theReplaceString);
 	}
 
 	@Override
 	protected String performReplacement(final Pattern thePattern,
 			final String selText) {
 		return feature1414(thePattern, selText);
 	}
 
 	/**
 	 * Applies the given pattern to the given input and replaces the matches
 	 * according to http://redmine.sbszh.ch/issues/1414
 	 * 
 	 * @param pattern
 	 * @param input
 	 * @return
 	 */
 	public static String feature1414(final Pattern pattern, final String input) {
 		final Matcher matcher = pattern.matcher(input);
 		matcher.find();
 		if (matcher.group(3) != null) {
 			return pattern.matcher(input).replaceAll("<abbr>$2</abbr>$3");
 		}
 		else {
 			return pattern.matcher(input).replaceAll("<abbr>$1</abbr>");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.AbstractMarkupAction#createPattern()
 	 * (@see <a href="http://redmine.sbszh.ch/issues/1443">Bug 1443</a>)
 	 */
 	@Override
 	protected String getCustomSkipPatternToAdd() {
 		return Bug1443.CUSTOM_PATTERN;
 	}
 
 }
 
 @SuppressWarnings("serial")
 class AbbrevStartAction extends RegexStartAction {
 
 	AbbrevStartAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName) {
 		super(thePrepToolsPluginExtension, theName, thePattern, theProcessName);
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.AbstractMarkupAction#createPattern()
 	 * (@see <a href="http://redmine.sbszh.ch/issues/1443">Bug 1443</a>)
 	 */
 	@Override
 	protected String getCustomSkipPatternToAdd() {
 		return Bug1443.CUSTOM_PATTERN;
 	}
 
 }
 
 @SuppressWarnings("serial")
 class AbbrevFindAction extends RegexFindAction {
 
 	AbbrevFindAction(PrepToolsPluginExtension thePrepToolsPluginExtension,
 			String theName, String thePattern, String theProcessName) {
 		super(thePrepToolsPluginExtension, theName, thePattern, theProcessName);
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.sbs.plugin.preptools.AbstractMarkupAction#createPattern()
 	 * (@see <a href="http://redmine.sbszh.ch/issues/1443">Bug 1443</a>)
 	 */
 	@Override
 	protected String getCustomSkipPatternToAdd() {
 		return Bug1443.CUSTOM_PATTERN;
 	}
 
 }
 
 @SuppressWarnings("serial")
 class RegexFindAction extends AbstractMarkupFindAction {
 	private final RegexHelper helper;
 
 	RegexFindAction(final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName, final String thePattern,
 			final String theProcessName) {
 		super(thePrepToolsPluginExtension, theName);
 		helper = new RegexHelper(thePattern, theProcessName);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class VFormStartAction extends AbstractMarkupStartAction {
 	private final VFormActionHelper helper;
 
 	VFormStartAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName) {
 		super(thePrepToolsPluginExtension, START);
 		helper = new VFormActionHelper(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class VFormChangeAction extends AbstractMarkupChangeAction {
 	private final VFormActionHelper helper;
 
 	VFormChangeAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName) {
 		super(thePrepToolsPluginExtension, CHANGE, VFormActionHelper.VFORM_TAG);
 		helper = new VFormActionHelper(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 class VFormFindAction extends AbstractMarkupFindAction {
 	private final VFormActionHelper helper;
 
 	VFormFindAction(final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName) {
 		super(thePrepToolsPluginExtension, FIND);
 		helper = new VFormActionHelper(thePrepToolsPluginExtension);
 	}
 
 	@Override
 	protected Pattern getPattern() {
 		return helper.getPattern();
 	}
 
 	@Override
 	protected String getProcessName() {
 		return helper.getProcessName();
 	}
 }
 
 @SuppressWarnings("serial")
 abstract class AbstractOrphanParenAction extends AbstractPrepToolAction {
 
 	/**
 	 * Utility (not hook!) method to get tool specific metainfo.
 	 * 
 	 * @param dmi
 	 * @return tool specific metainfo.
 	 */
 	protected final ParensPrepTool.MetaInfo getMetaInfo(
 			final DocumentMetaInfo dmi) {
 		return (ParensPrepTool.MetaInfo) dmi
 				.getToolSpecificMetaInfo(ParensPrepTool.LABEL);
 	}
 
 	/**
 	 * Utility (not hook!) method to get tool specific metainfo.
 	 * 
 	 * @return tool specific metainfo.
 	 */
 	protected final ParensPrepTool.MetaInfo getMetaInfo() {
 		final DocumentMetaInfo dmi = prepToolsPluginExtension
 				.getDocumentMetaInfo(prepToolsPluginExtension.getWsEditor()
 						.getEditorLocation());
 		return (ParensPrepTool.MetaInfo) dmi
 				.getToolSpecificMetaInfo(ParensPrepTool.LABEL);
 	}
 
 	@Override
 	protected void doSomething() throws BadLocationException {
 		init();
 
 		if (getMetaInfo().hasNext()) {
 			final PositionMatch match = getMetaInfo().next();
 			prepToolsPluginExtension.select(match.startOffset.getOffset(),
 					match.endOffset.getOffset());
 		}
 		else {
 			done();
 		}
 	}
 
 	/**
 	 * Optional hook for subclasses: To perform something before general
 	 * processing.
 	 */
 	protected void init() {
 
 	}
 
 	AbstractOrphanParenAction(
 			PrepToolsPluginExtension thePrepToolsPluginExtension,
 			final String theName) {
 		super(thePrepToolsPluginExtension, theName);
 	}
 
 	@Override
 	public String getProcessName() {
 		return ParensPrepTool.LABEL;
 	}
 }
 
 @SuppressWarnings("serial")
 class OrphanParenStartAction extends AbstractOrphanParenAction {
 
 	OrphanParenStartAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension, START);
 	}
 
 	@Override
 	protected void init() {
 		final WSTextEditorPage aWSTextEditorPage = prepToolsPluginExtension
 				.getPage();
 		final Document document = aWSTextEditorPage.getDocument();
 		final List<Match> orphans;
 		try {
 			orphans = ParensUtil.findOrphans(
 					document.getText(0, document.getLength()), getStartIndex(),
 					prepToolsPluginExtension.makeSkipper());
 		} catch (BadLocationException e) {
 			prepToolsPluginExtension.showMessage(e.getMessage());
 			throw new RuntimeException(e);
 		}
 		final ParensPrepTool.MetaInfo parensMetaInfo = getMetaInfo();
 		parensMetaInfo.set(orphans);
 		parensMetaInfo.setHasStarted(true);
 		parensMetaInfo.setDone(false);
 	}
 }
 
 @SuppressWarnings("serial")
 class OrphanParenFindNextAction extends AbstractOrphanParenAction {
 
 	OrphanParenFindNextAction(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		super(thePrepToolsPluginExtension, FIND);
 	}
 }
