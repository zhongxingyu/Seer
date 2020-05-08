 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.console.ui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.dltk.console.IScriptInterpreter;
 import org.eclipse.dltk.console.ScriptConsoleHistory;
 import org.eclipse.dltk.console.ScriptConsolePrompt;
 import org.eclipse.dltk.console.ui.internal.ICommandHandler;
 import org.eclipse.dltk.console.ui.internal.ScriptConsoleInput;
 import org.eclipse.dltk.console.ui.internal.ScriptConsolePage;
 import org.eclipse.dltk.console.ui.internal.ScriptConsoleSession;
 import org.eclipse.dltk.console.ui.internal.ScriptConsoleViewer.ConsoleDocumentListener;
import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.text.ITextHover;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.console.IConsoleDocumentPartitioner;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.console.TextConsole;
 import org.eclipse.ui.part.IPageBookViewPage;
 
 public class ScriptConsole extends TextConsole implements ICommandHandler {
 
 	private ScriptConsolePage page;
 
 	private ScriptConsolePartitioner partitioner;
 
 	private IContentAssistProcessor processor;
 
 	private ITextHover hover;
 
 	private IScriptInterpreter interpreter;
 
 	private ScriptConsoleSession session;
 
 	private ListenerList consoleListeners;
 
 	private ScriptConsolePrompt prompt;
 
 	private ScriptConsoleHistory history;
 
 	private IConsoleStyleProvider styleProvider;
 
 	private Color colorBlack = new Color(Display.getCurrent(), new RGB(0, 0, 0));
 	private Color colorBlue = new Color(Display.getCurrent(),
 			new RGB(0, 0, 255));
 	private Color colorRed = new Color(Display.getCurrent(), new RGB(255, 0, 0));
 
 	protected IConsoleDocumentPartitioner getPartitioner() {
 		return partitioner;
 	}
 
 	public ScriptConsole(String consoleName, String consoleType,
 			ImageDescriptor image) {
 		super(consoleName, consoleType, image, true);
 
 		this.consoleListeners = new ListenerList(ListenerList.IDENTITY);
 		this.prompt = new ScriptConsolePrompt("=>", "->"); //$NON-NLS-1$ //$NON-NLS-2$
 		this.history = new ScriptConsoleHistory();
 
 		this.session = new ScriptConsoleSession();
 		addListener(this.session);
 
 		partitioner = new ScriptConsolePartitioner();
 		getDocument().setDocumentPartitioner(partitioner);
 		partitioner.connect(getDocument());
 
 		styleProvider = new IConsoleStyleProvider() {
 
 			protected StyleRange[] createStyles(int start, String content,
 					boolean isInput, boolean isError) {
 				List rangeList = new ArrayList();
 				if ((colorBlack.isDisposed() != true)
 						&& (colorRed.isDisposed() != true)
 						&& (colorBlue.isDisposed() != true)) {
 					// Content has to be tokenized in order for style and
 					// hyperlinks to display correctly
 					StringTokenizer tokenizer = new StringTokenizer(content,
 							" \t\n\r\f@#=|,()[]{}<>'\"", true); //$NON-NLS-1$
 					String token;
 					int tokenStart = start;
 					while (tokenizer.hasMoreTokens() == true) {
 						token = tokenizer.nextToken();
 
 						if (isInput == true) {
 							rangeList.add(new StyleRange(tokenStart, token
 									.length(), colorBlack, null, SWT.BOLD));
 						} else {
 							if (isError == true) {
 								rangeList.add(new StyleRange(tokenStart, token
 										.length(), colorRed, null, SWT.BOLD));
 							} else {
 								rangeList.add(new StyleRange(tokenStart, token
 										.length(), colorBlue, null));
 							}
 						}
 
 						tokenStart += token.length();
 					}
 				}
 				return (StyleRange[]) rangeList
 						.toArray(new StyleRange[rangeList.size()]);
 			}
 
 			public StyleRange[] createPromptStyle(ScriptConsolePrompt prompt,
 					int offset) {
 				return createStyles(offset, prompt.toString(), true, false);
 			}
 
 			public StyleRange[] createUserInputStyle(String content, int offset) {
 				return createStyles(offset, content, true, false);
 			}
 
 			public StyleRange[] createInterpreterOutputStyle(String content,
 					int offset) {
 				return createStyles(offset, content, false, false);
 			}
 
 		};
 	}
 
 	public ScriptConsole(String consoleName, String consoleType) {
 		this(consoleName, consoleType, null);
 	}
 
 	public IScriptConsoleSession getSession() {
 		return session;
 	}
 
 	public void addListener(IScriptConsoleListener listener) {
 		consoleListeners.add(listener);
 	}
 
 	public void removeListener(IScriptConsoleListener listener) {
 		consoleListeners.remove(listener);
 	}
 
 	protected void setContentAssistProcessor(IContentAssistProcessor processor) {
 		this.processor = processor;
 	}
 
 	protected void setInterpreter(IScriptInterpreter interpreter) {
 		this.interpreter = interpreter;
 		// interpreter.addInitialListenerOperation(new Runnable() {
 		// public void run() {
 		// Object[] listeners = consoleListeners.getListeners();
 		// String output = ScriptConsole.this.interpreter
 		// .getInitialOuput();
 		// if (output != null) {
 		// for (int i = 0; i < listeners.length; i++) {
 		// ((IScriptConsoleListener) listeners[i])
 		// .interpreterResponse(output);
 		// }
 		// }
 		// }
 		// });
 	}
 
 	protected void setStyleProvider(IConsoleStyleProvider provider) {
 		this.styleProvider = provider;
 	}
 
 	public void setPrompt(ScriptConsolePrompt prompt) {
 		this.prompt = prompt;
 	}
 
 	public ScriptConsolePrompt getPrompt() {
 		return prompt;
 	}
 
 	public ScriptConsoleHistory getHistory() {
 		return history;
 	}
 
 	protected void setTextHover(ITextHover hover) {
 		this.hover = hover;
 	}
 
 	private ConsoleDocumentListener documentListener;
 
 	public ConsoleDocumentListener getDocumentListener() {
 		if (documentListener == null) {
 			documentListener = new ConsoleDocumentListener(this, this
 					.getPrompt(), this.getHistory());
 			documentListener.setDocument(getDocument());
 
 		}
 
 		return documentListener;
 	}
 
 	public IPageBookViewPage createPage(IConsoleView view) {
 		SourceViewerConfiguration cfg = new ScriptConsoleSourceViewerConfiguration(
 				processor, hover);
 		page = new ScriptConsolePage(this, view, cfg);
 		if (styleProvider != null)
 			page.setStyleProviser(styleProvider);
 		return page;
 	}
 
 	public void clearConsole() {
 		page.clearConsolePage();
 	}
 
 	public IScriptConsoleInput getInput() {
 		return new ScriptConsoleInput(page);
 	}
 
 	public String handleCommand(String userInput) throws IOException {
 		Object[] listeners = consoleListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			((IScriptConsoleListener) listeners[i]).userRequest(userInput);
 		}
 
 		interpreter.exec(userInput);
 
 		String output = interpreter.getOutput();
 
 		if (interpreter.getState() == IScriptInterpreter.WAIT_NEW_COMMAND) {
 			prompt.setMode(true);
 		} else {
 			prompt.setMode(false);
 		}
 
 		for (int i = 0; i < listeners.length; i++) {
 			((IScriptConsoleListener) listeners[i]).interpreterResponse(output);
 		}
 
 		return output;
 	}
 
 	public void terminate() {
 		try {
 			interpreter.close();
 		} catch (IOException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
 		}
 	}
 
 	public void dispose() {
 		partitioner.clearRanges();
 
 		colorBlack.dispose();
 		colorBlue.dispose();
 		colorRed.dispose();
 
 		terminate();
 
 		super.dispose();
 	}
 
 }
