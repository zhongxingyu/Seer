 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.console.ui.internal;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.dltk.console.IScriptConsoleInterpreter;
 import org.eclipse.dltk.console.IScriptInterpreter;
 import org.eclipse.dltk.console.ScriptConsoleHistory;
 import org.eclipse.dltk.console.ScriptConsolePrompt;
 import org.eclipse.dltk.console.ui.AnsiColorHelper;
 import org.eclipse.dltk.console.ui.IScriptConsoleViewer;
 import org.eclipse.dltk.console.ui.ScriptConsole;
 import org.eclipse.dltk.console.ui.ScriptConsolePartitioner;
 import org.eclipse.dltk.console.ui.AnsiColorHelper.IAnsiColorHandler;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.hyperlink.HyperlinkManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ST;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.console.TextConsoleViewer;
 
 public class ScriptConsoleViewer extends TextConsoleViewer implements
 		IScriptConsoleViewer {
 	public static class ConsoleDocumentListener implements IDocumentListener {
 
 		private boolean bEnabled = true;
 		private ICommandHandler handler;
 
 		private ScriptConsolePrompt prompt;
 
 		private ScriptConsoleHistory history;
 
 		private int offset;
 
 		private IDocument doc;
 
 		private AnsiColorHelper ansiHelper = new AnsiColorHelper();
 
 		private List viewerList = new ArrayList();
 
 		private void addViewer(ScriptConsoleViewer viewer) {
 			viewerList.add(viewer);
 		}
 
 		private void removeViewer(ScriptConsoleViewer viewer) {
 			viewerList.remove(viewer);
 		}
 
 		protected void connectListener() {
 			doc.addDocumentListener(this);
 		}
 
 		protected void disconnectListener() {
 			doc.removeDocumentListener(this);
 		}
 
 		public void clear() {
 			try {
 				disconnectListener();
 				doc.set(""); //$NON-NLS-1$
 				ScriptConsoleViewer viewer;
 				for (Iterator iter = viewerList.iterator(); iter.hasNext();) {
 					viewer = (ScriptConsoleViewer) iter.next();
 					IDocumentPartitioner partitioner = viewer.getDocument()
 							.getDocumentPartitioner();
 					if (partitioner instanceof ScriptConsolePartitioner) {
 						ScriptConsolePartitioner scriptConsolePartitioner = (ScriptConsolePartitioner) partitioner;
 						scriptConsolePartitioner.clearRanges();
 					}
 				}
 				appendInvitation();
 				for (Iterator iter = viewerList.iterator(); iter.hasNext();) {
 					((ScriptConsoleViewer) iter.next()).setCaretPosition(doc
 							.getLength());
 				}
 				connectListener();
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}
 		}
 
 		public ConsoleDocumentListener(ICommandHandler handler,
 				ScriptConsolePrompt prompt, ScriptConsoleHistory history) {
 			this.prompt = prompt;
 			this.handler = handler;
 			this.history = history;
 
 			this.offset = 0;
 
 			this.doc = null;
 		}
 
 		public void setDocument(IDocument doc) {
 			if (this.doc != null) {
 				disconnectListener();
 			}
 
 			this.doc = doc;
 
 			if (this.doc != null) {
 				connectListener();
 			}
 		}
 
 		public void documentAboutToBeChanged(DocumentEvent event) {
 
 		}
 
 		protected void handleCommandLine() throws BadLocationException,
 				IOException {
 			final String command = getCommandLine();
 			ansiHelper.reset();
 			appendDelimeter();
 
 			Thread handlerThread = new Thread(new Runnable() {
 
 				public void run() {
 					try {
 						final String result = handler.handleCommand(command);
 
 						if (((ScriptConsole) handler).getState() != IScriptConsoleInterpreter.WAIT_USER_INPUT) {
 							((ScriptConsole) handler).getPage().getSite()
 									.getShell().getDisplay().asyncExec(
 											new Runnable() {
 
 												public void run() {
 													try {
 														disconnectListener();
 														processResult(result);
 														connectListener();
 													} catch (BadLocationException bxcn) {
 														bxcn.printStackTrace();
 													}
 												}
 
 											});
 						}
 					} catch (IOException ixcn) {
 						ixcn.printStackTrace();
 					}
 				}
 
 			}, Messages.ScriptConsoleViewer_scriptConsoleCommandHandler);
 			handlerThread.setDaemon(true);
 			handlerThread.setPriority(Thread.MIN_PRIORITY);
 			handlerThread.start();
 		}
 
 		protected void appendText(String text) throws BadLocationException {
 			doc.replace(doc.getLength(), 0, text);
 		}
 
 		protected void processText(int originalOffset, String content,
 				boolean isInput, boolean isError, final boolean shouldReveal,
 				final boolean shouldRedraw) throws BadLocationException {
 			if (originalOffset == -1) {
 				originalOffset = doc.getLength();
 			}
 
 			ansiHelper.processText(originalOffset, content, isInput, isError,
 					new IAnsiColorHandler() {
 
 						public void handleText(int start, String content,
 								boolean isInput, boolean isError)
 								throws BadLocationException {
 							appendText(content);
 							addToPartitioner(start, content, isInput, isError);
 						}
 
 						public void processingComplete(int start, int length) {
 							ScriptConsoleViewer viewer;
 							for (Iterator iter = viewerList.iterator(); iter
 									.hasNext();) {
 								viewer = (ScriptConsoleViewer) iter.next();
 								if (shouldReveal == true) {
 									viewer.setCaretPosition(doc.getLength());
 									viewer.revealEndOfDocument();
 								}
 
 								if (shouldRedraw == true) {
 									if (viewer.getTextWidget() != null) {
 										viewer.getTextWidget().redrawRange(
 												start, length, true);
 									}
 								}
 							}
 						}
 
 					});
 		}
 
 		protected void processResult(final String result)
 				throws BadLocationException {
 			if (result != null) {
 				processText(-1, result, false, false, false, true);
 				history.commit();
 			}
 			offset = getLastLineLength();
 			appendInvitation();
 		}
 
 		private void addToPartitioner(ScriptConsoleViewer viewer,
 				StyleRange style) {
 			IDocumentPartitioner partitioner = viewer.getDocument()
 					.getDocumentPartitioner();
 			if (partitioner instanceof ScriptConsolePartitioner) {
 				ScriptConsolePartitioner scriptConsolePartitioner = (ScriptConsolePartitioner) partitioner;
 				scriptConsolePartitioner.addRange(style);
 				viewer.getTextWidget().redraw();
 			}
 		}
 
 		protected void addToPartitioner(int start, String content,
 				boolean isInput, boolean isError) {
 			// ssanders: Content has to be tokenized in order for style and
 			// hyperlinks to display correctly
 			StringTokenizer tokenizer = new StringTokenizer(content,
 					" \t\n\r\f@#=|,()[]{}<>'\"", true); //$NON-NLS-1$
 			String token;
 			int tokenStart = start;
 			ScriptConsoleViewer viewer;
 			while (tokenizer.hasMoreTokens() == true) {
 				token = tokenizer.nextToken();
 
 				for (Iterator iter = viewerList.iterator(); iter.hasNext();) {
 					viewer = (ScriptConsoleViewer) iter.next();
 					if (isInput == true) {
 						addToPartitioner(viewer, new StyleRange(tokenStart,
 								token.length(), AnsiColorHelper.COLOR_BLACK,
 								null, SWT.BOLD));
 					} else {
 						addToPartitioner(viewer, ansiHelper.resolveStyleRange(
 								tokenStart, token.length(), isError));
 					}
 				}
 
 				tokenStart += token.length();
 			}
 		}
 
 		protected void processAddition(int offset, String text) {
 			if (!bEnabled) {
 				return;
 			}
 			try {
 				String delim = TextUtilities.getDefaultLineDelimiter(doc);
 
 				text = doc.get(offset, doc.getLength() - offset);
 
 				doc.replace(offset, text.length(), ""); //$NON-NLS-1$
 
 				text = text.replaceAll("\r\n|\n|\r", delim); //$NON-NLS-1$
 
 				int start = 0;
 				int index = -1;
 				String cmd;
 				while ((index = text.indexOf(delim, start)) != -1) {
 					cmd = text.substring(start, index);
 					processText(getCommandLineOffset(), cmd, true, false,
 							false, true);
 
 					history.update(getCommandLine());
 					start = index + delim.length();
 					handleCommandLine();
 				}
 
 				processText(-1, text.substring(start, text.length()), true,
 						false, false, true);
 			} catch (BadLocationException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			} catch (IOException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		public void documentChanged(final DocumentEvent event) {
 			ansiHelper.disableWhile(new Runnable() {
 
 				public void run() {
 					disconnectListener();
 					processAddition(event.getOffset(), event.getText());
 					connectListener();
 				}
 
 			});
 		}
 
 		public void appendInvitation() throws BadLocationException {
 			processText(-1, prompt.toString(), true, false, true, true);
 		}
 
 		public void appendDelimeter() throws BadLocationException {
 			processText(-1, TextUtilities.getDefaultLineDelimiter(doc), false,
 					false, false, true);
 		}
 
 		protected int getLastLineLength() throws BadLocationException {
 			int lastLine = doc.getNumberOfLines() - 1;
 			return doc.getLineLength(lastLine);
 		}
 
 		public int getLastLineReadOnlySize() {
 			return (((((ScriptConsole) handler).getState() == IScriptInterpreter.WAIT_USER_INPUT) ? 0
 					: prompt.toString().length()) + offset);
 		}
 
 		public int getCommandLineOffset() throws BadLocationException {
 			int lastLine = doc.getNumberOfLines() - 1;
 			return doc.getLineOffset(lastLine) + getLastLineReadOnlySize();
 		}
 
 		public int getCommandLineLength() throws BadLocationException {
 			int lastLine = doc.getNumberOfLines() - 1;
 			return doc.getLineLength(lastLine) - getLastLineReadOnlySize();
 		}
 
 		public String getCommandLine() throws BadLocationException {
 			return doc.get(getCommandLineOffset(), getCommandLineLength());
 		}
 
 		public void setCommandLine(final String command) {
 			ansiHelper.disableWhile(new Runnable() {
 
 				public void run() {
 					try {
 						doc.replace(getCommandLineOffset(),
 								getCommandLineLength(), command);
 					} catch (BadLocationException bxcn) {
 						bxcn.printStackTrace();
 					}
 				}
 
 			});
 		}
 
 	}
 
	private class ScriptConsoleStyledText extends StyledText {
 
		public ScriptConsoleStyledText(Composite parent, int style) {
 			super(parent, (style | SWT.WRAP));
 		}
 
 		public void invokeAction(int action) {
 			if (isCaretOnLastLine()) {
 				switch (action) {
 				case ST.LINE_UP:
 					history.prev();
 					console.getDocumentListener().setCommandLine(history.get());
 					setCaretOffset(getDocument().getLength());
 					return;
 
 				case ST.LINE_DOWN:
 					history.next();
 					console.getDocumentListener().setCommandLine(history.get());
 					setCaretOffset(getDocument().getLength());
 					return;
 
 				case ST.DELETE_PREVIOUS:
 					if (getCaretOffset() <= getCommandLineOffset()) {
 						return;
 					}
 					break;
 
 				case ST.DELETE_NEXT:
 					if (getCaretOffset() < getCommandLineOffset()) {
 						return;
 					}
 					break;
 
 				case ST.DELETE_WORD_PREVIOUS:
 					return;
 				}
 
 				super.invokeAction(action);
 
 				if (isCaretOnLastLine()
 						&& getCaretOffset() <= getCommandLineOffset()) {
 					setCaretOffset(getCommandLineOffset());
 				}
 			} else {
 
 				super.invokeAction(action);
 			}
 		}
 
 		public void superPaste() {
 			super.paste();
 		}
 
 		public void paste() {
 			if (isCaretOnLastLine()) {
 				console.getDocumentListener().ansiHelper
 						.disableWhile(new Runnable() {
 
 							public void run() {
 								superPaste();
 							}
 
 						});
 			}
 		}
 
 	}
 
 	private ScriptConsoleHistory history;
 
 	private ScriptConsole console;
 
 	public int getCaretPosition() {
 		return getTextWidget().getCaretOffset();
 	}
 
 	public void enableProcessing() {
 		ConsoleDocumentListener listener = console.getDocumentListener();
 		listener.bEnabled = true;
 	}
 
 	public void disableProcessing() {
 		ConsoleDocumentListener listener = console.getDocumentListener();
 		listener.bEnabled = false;
 	}
 
 	public void setCaretPosition(final int offset) {
 		if (getTextWidget() != null) {
 			getTextWidget().getDisplay().asyncExec(new Runnable() {
 
 				public void run() {
 					if (getTextWidget() != null) {
 						getTextWidget().setCaretOffset(offset);
 					}
 				}
 			});
 		}
 	}
 
 	public int beginLineOffset() throws BadLocationException {
 		IDocument doc = getDocument();
 		int offset = getCaretPosition();
 		int line = doc.getLineOfOffset(offset);
 		return offset - doc.getLineOffset(line);
 	}
 
 	protected boolean isCaretOnLastLine() {
 		try {
 			IDocument doc = getDocument();
 			int line = doc.getLineOfOffset(getCaretPosition());
 			return line == doc.getNumberOfLines() - 1;
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	protected StyledText createTextWidget(Composite parent, int styles) {
		return new ScriptConsoleStyledText(parent, styles);
 	}
 
 	public ScriptConsoleViewer(Composite parent, final ScriptConsole console,
 			final IScriptConsoleContentHandler contentHandler) {
 		super(parent, console);
 
 		this.console = console;
 
 		this.history = console.getHistory();
 
 		console.getDocumentListener().addViewer(this);
 
 		final StyledText styledText = getTextWidget();
 
 		// styledText.setEditable(false);
 
 		// Correct keyboard actions
 		styledText.addFocusListener(new FocusListener() {
 
 			public void focusGained(FocusEvent e) {
 				setCaretPosition(getDocument().getLength());
 				styledText.removeFocusListener(this);
 			}
 
 			public void focusLost(FocusEvent e) {
 
 			}
 		});
 
 		styledText.addVerifyKeyListener(new VerifyKeyListener() {
 			public void verifyKey(VerifyEvent event) {
 				try {
 					if (event.character != '\0') {
 						// Printable character
 						// ssanders: Ensure selection is on last line
 						ConsoleDocumentListener listener = console
 								.getDocumentListener();
 						int selStart = getSelectedRange().x;
 						int selEnd = (getSelectedRange().x + getSelectedRange().y);
 						int clOffset = listener.getCommandLineOffset();
 						int clLength = listener.getCommandLineLength();
 						if (selStart < clOffset) {
 							int selLength;
 
 							if (selEnd < clOffset) {
 								selStart = (clOffset + clLength);
 								selLength = 0;
 							} else {
 								selStart = clOffset;
 								selLength = (selEnd - selStart);
 							}
 
 							setSelectedRange(selStart, selLength);
 						}
 
 						if (beginLineOffset() < console.getDocumentListener()
 								.getLastLineReadOnlySize()) {
 							event.doit = false;
 							return;
 						}
 
 						if (event.character == SWT.CR) {
 							getTextWidget().setCaretOffset(
 									getDocument().getLength());
 							return;
 						}
 
 						// ssanders: Avoid outputting " " when invoking
 						// completion on Mac OS X
 						if (event.keyCode == 32
 								&& (event.stateMask & SWT.CTRL) > 0) {
 							event.doit = false;
 							return;
 						}
 
 						// ssanders: Avoid outputting "<Tab>" when invoking
 						// completion on Mac OS X
 						if (event.keyCode == 9) {
 							event.doit = false;
 							return;
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		styledText.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent e) {
 				if (e.keyCode == 9) {
 					contentHandler.contentAssistRequired();
 				}
 			}
 
 			public void keyReleased(KeyEvent e) {
 			}
 		});
 
 		if (console.getDocumentListener().viewerList.size() == 1) {
 			clear();
 		}
 	}
 
 	// IConsoleTextViewer
 	public String getCommandLine() {
 		try {
 			return console.getDocumentListener().getCommandLine();
 		} catch (BadLocationException e) {
 			return null;
 		}
 	}
 
 	public int getCommandLineOffset() {
 		try {
 			return console.getDocumentListener().getCommandLineOffset();
 		} catch (BadLocationException e) {
 			return -1;
 		}
 	}
 
 	public void clear() {
 		console.getDocumentListener().clear();
 	}
 
 	public void insertText(String text) {
 		getTextWidget().append(text);
 	}
 
 	public boolean canDoOperation(int operation) {
 		boolean canDoOperation = super.canDoOperation(operation);
 
 		if (canDoOperation) {
 			switch (operation) {
 			case CUT:
 			case DELETE:
 			case PASTE:
 			case SHIFT_LEFT:
 			case SHIFT_RIGHT:
 			case PREFIX:
 			case STRIP_PREFIX:
 				canDoOperation = isCaretOnLastLine();
 			}
 		}
 
 		return canDoOperation;
 	}
 
 	public void activatePlugins() {
 		fHyperlinkManager = new HyperlinkManager(
 				HyperlinkManager.LONGEST_REGION_FIRST);
 		fHyperlinkManager.install(this, fHyperlinkPresenter,
 				fHyperlinkDetectors, fHyperlinkStateMask);
 
 		super.activatePlugins();
 	}
 
 	public void dispose() {
 		console.getDocumentListener().removeViewer(this);
 	}
 
 }
