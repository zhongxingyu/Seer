 /*******************************************************************************
  * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet)
  * and others. All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.docmlet.tex.internal.ui.editors;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.primitives.ArrayIntList;
 import org.apache.commons.collections.primitives.IntList;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.link.LinkedModeModel;
 import org.eclipse.jface.text.link.LinkedModeUI;
 import org.eclipse.jface.text.link.LinkedPosition;
 import org.eclipse.jface.text.link.LinkedPositionGroup;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 
 import de.walware.ecommons.ltk.LTK;
 import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
 import de.walware.ecommons.ltk.ui.sourceediting.assist.CompletionProposalWithOverwrite;
 import de.walware.ecommons.ui.util.UIAccess;
 
 import de.walware.docmlet.tex.core.commands.Argument;
 import de.walware.docmlet.tex.core.commands.IEnvDefinitions;
 import de.walware.docmlet.tex.core.commands.TexCommand;
 import de.walware.docmlet.tex.core.text.LtxHeuristicTokenScanner;
 import de.walware.docmlet.tex.internal.ui.TexUIPlugin;
 import de.walware.docmlet.tex.ui.TexImages;
 
 
 public class LtxCommandCompletionProposal extends CompletionProposalWithOverwrite
 		implements ICompletionProposalExtension6 {
 	
 	
 	public static class Env extends LtxCommandCompletionProposal {
 		
 		protected Env(final AssistInvocationContext context, final int startOffset,
 				final TexCommand command, final int relevance) {
 			super(context, startOffset, command);
 			fRelevance += relevance;
 		}
 		
 		
 		@Override
 		public StyledString getStyledDisplayString() {
 			if (fDisplayString == null) {
 				final StyledString s = new StyledString(fCommand.getControlWord());
 				s.append(" – " + fCommand.getDescription(), StyledString.QUALIFIER_STYLER);
 				fDisplayString = s;
 			}
 			return fDisplayString;
 		}
 		
 	}
 	
 	
 	private static class LinkedSepMode implements IDocumentListener, VerifyKeyListener {
 		
 		private final SourceViewer fViewer;
 		private final IDocument fDocument;
 		private final int fOffset;
 		
 		private boolean fInserted;
 		private boolean fIntern;
 		
 		public LinkedSepMode(final SourceViewer viewer, final IDocument document, final int offset) {
 			fViewer = viewer;
 			fDocument = document;
 			fOffset = offset;
 		}
 		
 		public void install() {
 			if (UIAccess.isOkToUse(fViewer)) {
 				fViewer.getTextWidget().addVerifyKeyListener(this);
 				fDocument.addDocumentListener(this);
 			}
 		}
 		
 		@Override
 		public void verifyKey(final VerifyEvent event) {
 			if (fViewer.getDocument() == fDocument) {
 				final Point selection = fViewer.getSelectedRange();
 				if (!fInserted
 						&& selection.x == fOffset && selection.y == 0
						&& (event.character != 0) ) {
 					try {
 						final int currentChar = (fOffset < fDocument.getLength()) ? fDocument.getChar(fOffset) : '\n';
						final char c = event.character;
 						if (currentChar <= 0x20 && currentChar != c
 								&& c >= 0x20 && !Character.isLetterOrDigit(c) ) {
 							fIntern = true;
 							fDocument.replace(fOffset, 0, "" + c + c);
							// install linked mode?
 							fInserted = true;
 							event.doit = false;
 							fViewer.setSelection(new TextSelection(fOffset+1, 0), true);
 							return;
 						}
 					}
 					catch (final BadLocationException e) {
 					}
 					finally {
 						fIntern = false;
 					}
 				}
 				if (fInserted && event.character == SWT.BS
 						&& selection.x == fOffset + 1 && selection.y == 0) {
 					try {
 						fIntern = true;
 						fDocument.replace(fOffset, 2, "");
 						fInserted = false;
 						event.doit = false;
 						return;
 					}
 					catch (final BadLocationException e) {
 					}
 					finally {
 						fIntern = false;
 					}
 				}
 			}
 		}
 		
 		@Override
 		public void documentAboutToBeChanged(final DocumentEvent event) {
 		}
 		
 		@Override
 		public void documentChanged(final DocumentEvent event) {
 			if (!fIntern) {
 				dispose();
 			}
 		}
 		
 		private void dispose() {
 			fViewer.getTextWidget().removeVerifyKeyListener(this);
 			fDocument.removeDocumentListener(this);
 		}
 		
 	}
 	
 	
 	
 	
 	static final class ApplyData {
 		
 		private final AssistInvocationContext fContext;
 		private final SourceViewer fViewer;
 		private final IDocument fDocument;
 		
 		private LtxHeuristicTokenScanner fScanner;
 		
 		ApplyData(final AssistInvocationContext context) {
 			fContext = context;
 			fViewer = context.getSourceViewer();
 			fDocument = fViewer.getDocument();
 		}
 		
 		public SourceViewer getViewer() {
 			return fViewer;
 		}
 		
 		public IDocument getDocument() {
 			return fDocument;
 		}
 		
 		public LtxHeuristicTokenScanner getScanner() {
 			if (fScanner == null) {
 				fScanner = (LtxHeuristicTokenScanner) LTK.getModelAdapter(
 						fContext.getEditor().getModelTypeId(), LtxHeuristicTokenScanner.class );
 			}
 			return fScanner;
 		}
 		
 	}
 	
 	
 	private static final boolean isFollowedByOpeningBracket(final ApplyData util,
 			final int forwardOffset, final boolean allowSquare) {
 		final LtxHeuristicTokenScanner scanner = util.getScanner();
 		scanner.configure(util.getDocument());
 		final int idx = scanner.findAnyNonBlankForward(forwardOffset, LtxHeuristicTokenScanner.UNBOUND, false);
 		return (idx >= 0
 				&& (scanner.getChar() == '{' || (allowSquare && scanner.getChar() == '[')) );
 	}
 	
 	private static final boolean isClosedBracket(final ApplyData data, final int backwardOffset, final int forwardOffset) {
 		final int searchType = LtxHeuristicTokenScanner.CURLY_BRACKET_TYPE;
 		int[] balance = new int[3];
 		balance[searchType]++;
 		final LtxHeuristicTokenScanner scanner = data.getScanner();
 		scanner.configureDefaultParitions(data.getDocument());
 		balance = scanner.computeBracketBalance(backwardOffset, forwardOffset, balance, searchType);
 		return (balance[searchType] <= 0);
 	}
 	
 	
 	protected final TexCommand fCommand;
 	
 	protected int fRelevance;
 	
 	protected StyledString fDisplayString;
 	
 	private Point fSelection = null;
 	
 	private ApplyData fApplyData;
 	
 	
 	protected LtxCommandCompletionProposal(final AssistInvocationContext context, final int startOffset,
 			final TexCommand command) {
 		super(context, startOffset);
 		fCommand = command;
 		fRelevance = 95;
 	}
 	
 	
 	@Override
 	protected String getPluginId() {
 		return TexUIPlugin.PLUGIN_ID;
 	}
 	
 	@Override
 	public int getRelevance() {
 		return fRelevance;
 	}
 	
 	@Override
 	public String getSortingString() {
 		return fCommand.getControlWord();
 	}
 	
 	@Override
 	public String getDisplayString() {
 		return getStyledDisplayString().getString();
 	}
 	
 	@Override
 	public Image getImage() {
 		final String key = TexImages.getCommandImageKey(fCommand);
 		return (key != null) ? TexImages.getImageRegistry().get(key) : null;
 	}
 	
 	@Override
 	public StyledString getStyledDisplayString() {
 		if (fDisplayString == null) {
 			final StyledString s = new StyledString(((fCommand.getType() & TexCommand.MASK_MAIN) == TexCommand.ENV) ?
 							fCommand.getControlWord() : "\\"+fCommand.getControlWord() );
 			for (final Argument arg : fCommand.getArguments()) {
 				if ((arg.getType() & Argument.OPTIONAL) != 0) {
 					s.append("[]");
 				}
 				else {
 					s.append("{}");
 				}
 			}
 			s.append(" – " + fCommand.getDescription(), StyledString.QUALIFIER_STYLER);
 			fDisplayString = s;
 		}
 		return fDisplayString;
 	}
 	
 	protected final ApplyData getApplyData() {
 		if (fApplyData == null) {
 			fApplyData = new ApplyData(fContext);
 		}
 		return fApplyData;
 	}
 	
 	@Override
 	protected int computeReplacementLength(final int replacementOffset, final Point selection, final int caretOffset, final boolean overwrite) throws BadLocationException {
 		int end = Math.max(caretOffset, selection.x + selection.y);
 		if (overwrite) {
 			final ApplyData data = getApplyData();
 			final IDocument document = data.getDocument();
 			end--;
 			SEARCH_END: while (++end < document.getLength()) {
 				switch (document.getChar(end)) {
 				case 'a':
 				case 'b':
 				case 'c':
 				case 'd':
 				case 'e':
 				case 'f':
 				case 'g':
 				case 'h':
 				case 'i':
 				case 'j':
 				case 'k':
 				case 'l':
 				case 'm':
 				case 'n':
 				case 'o':
 				case 'p':
 				case 'q':
 				case 'r':
 				case 's':
 				case 't':
 				case 'u':
 				case 'v':
 				case 'w':
 				case 'x':
 				case 'y':
 				case 'z':
 				case 'A':
 				case 'B':
 				case 'C':
 				case 'D':
 				case 'E':
 				case 'F':
 				case 'G':
 				case 'H':
 				case 'I':
 				case 'J':
 				case 'K':
 				case 'L':
 				case 'M':
 				case 'N':
 				case 'O':
 				case 'P':
 				case 'Q':
 				case 'R':
 				case 'S':
 				case 'T':
 				case 'U':
 				case 'V':
 				case 'W':
 				case 'X':
 				case 'Y':
 				case 'Z':
 					continue SEARCH_END;
 				default:
 					break SEARCH_END;
 				}
 			}
 		}
 		return (end - replacementOffset);
 	}
 	
 	@Override
 	public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
 		try {
 			final int start = getReplacementOffset();
 			final String prefix = document.get(start, offset - start);
 			return prefix.regionMatches(true, 0, fCommand.getControlWord(), 0, prefix.length());
 		}
 		catch (final BadLocationException e) {
 			return false;
 		}
 	}
 	
 	@Override
 	public String getAdditionalProposalInfo() {
 		return null;
 	}
 	
 	@Override
 	public boolean isAutoInsertable() {
 		return true;
 	}
 	
 	@Override
 	protected void doApply(final char trigger, final int stateMask, final int caretOffset,
 			final int replacementOffset, final int replacementLength) throws BadLocationException {
 		final ApplyData data = getApplyData();
 		final IDocument document = data.getDocument();
 		
 		final StringBuilder replacement = new StringBuilder(fCommand.getControlWord());
 		if ((stateMask & 0x1) == 0x1) {
 			replacement.insert(0, '\\');
 		}
 		int cursor = replacement.length();
 		int mode = 0;
 		IntList positions = null;
 		if (fCommand == IEnvDefinitions.VERBATIM_verb_COMMAND) {
 			mode = 201;
 		}
 		else if ((fCommand.getType() & TexCommand.MASK_MAIN) != TexCommand.ENV) {
 			final List<Argument> args = fCommand.getArguments();
 			if (args != null && !args.isEmpty()) {
 				final boolean isFirstOptional = args.get(0).isOptional();
 				int idxFirstRequired = -1;
 				for (int i = (isFirstOptional) ? 1 : 0; i < args.size(); i++) {
 					final Argument arg = args.get(i);
 					if (arg.isRequired()) {
 						idxFirstRequired = i;
 						break;
 					}
 				}
 				if (idxFirstRequired >= 0) {
 					if (replacementOffset+replacementLength < document.getLength()-1
 							&& (document.getChar(replacementOffset+replacementLength) == '{'
 									|| (isFirstOptional && document.getChar(replacementOffset+replacementLength) == '[') )) {
 						cursor ++;
 						mode = 10;
 					}
 					else if (!isFollowedByOpeningBracket(data, replacementOffset+replacementLength,
 							isFirstOptional )) {
 						replacement.append('{');
 						cursor ++;
 						mode = 11;
 					}
 					if (mode >= 10) {
 						if (mode == 11
 								&& !isClosedBracket(data, replacementOffset, replacementOffset+replacementLength)) {
 							replacement.append('}');
 							
 							positions = new ArrayIntList();
 							mode = 0;
 							if (isFirstOptional) {
 								positions.add(mode);
 							}
 							mode++;
 							positions.add(mode++);
 							for (int i = idxFirstRequired+1; i < args.size(); i++) {
 								if (args.get(i).isRequired()) {
 									replacement.append("{}");
 									mode++;
 									positions.add(mode++);
 								}
 								else if (positions.get(positions.size()-1) != mode){
 									positions.add(mode);
 								}
 							}
 							if (positions.get(positions.size()-1) != mode){
 								positions.add(mode);
 							}
 							mode = 110 + 1;
 							// add multiple arguments
 						}
 					}
 				}
 			}
 		}
 		document.replace(replacementOffset, replacementLength, replacement.toString());
 		setCursorPosition(replacementOffset + cursor);
 		if (mode > 100 && mode < 200) {
 			createLinkedMode(data, replacementOffset + cursor - (mode - 110), positions).enter();
 		}
 		else if (mode > 200 && mode < 300) {
 			createLinkedVerbMode(data, replacementOffset + cursor);
 		}
 		if ((fCommand.getType() & TexCommand.MASK_MAIN) == TexCommand.GENERICENV) {
 			reinvokeAssist(data.getViewer());
 		}
 	}
 	
 	private LinkedModeUI createLinkedMode(final ApplyData data, final int offset, final IntList positions)
 			throws BadLocationException {
 		final LinkedModeModel model = new LinkedModeModel();
 		int pos = 0;
 		
 		final List<LinkedPosition> linked = new ArrayList<LinkedPosition>(positions.size());
 		for (int i = 0; i < positions.size() - 1; i++) {
 			final LinkedPositionGroup group = new LinkedPositionGroup();
 			final LinkedPosition position = (positions.get(i) % 2 == 1) ?
 					TexBracketLevel.createPosition('{', data.getDocument(),
 							offset + positions.get(i), 0, pos++ ) :
 					new LinkedPosition(data.getDocument(),
 							offset + positions.get(i), 0, pos++ );
 			group.addPosition(position);
 			linked.add(position);
 			model.addGroup(group);
 		}
 		
 		model.forceInstall();
 		
 		final TexBracketLevel level = new TexBracketLevel(data.getDocument(),
 				fContext.getEditor().getPartitioning().getPartitioning(), linked,
 				TexBracketLevel.AUTODELETE );
 		
 		/* create UI */
 		final LinkedModeUI ui = new LinkedModeUI(model, data.getViewer());
 		ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
 		ui.setExitPosition(data.getViewer(), offset + positions.get(positions.size()-1), 0, pos);
 		ui.setSimpleMode(true);
 		ui.setExitPolicy(level);
 		return ui;
 	}
 	
 	private void createLinkedVerbMode(final ApplyData data, final int offset) throws BadLocationException {
 		final LinkedSepMode mode = new LinkedSepMode(data.getViewer(), data.getDocument(), offset);
 		Display.getCurrent().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				mode.install();
 			}
 		});
 	}
 	
 	protected void setCursorPosition(final int offset) {
 		fSelection = new Point(offset, 0);
 	}
 	
 	
 	@Override
 	public Point getSelection(final IDocument document) {
 		return fSelection;
 	}
 	
 	@Override
 	public IContextInformation getContextInformation() {
 		return null;
 	}
 	
 }
