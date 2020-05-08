 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.ui.console.TextConsole;
 
 /**
  * A hyperlink from a stack trace line of the form "(file "*.*")"
  */
 public class ScriptDebuggerConsoleToFileHyperlink extends
 		ScriptDebugConsoleGenericHyperlink {
 	static final Pattern pattern = Pattern
			.compile("\\t*#\\d+ +file:(\\S+) +line:(\\d+)");
 
 	public ScriptDebuggerConsoleToFileHyperlink(TextConsole console) {
 		super(console);
 	}
 
 	protected String getFileName(String linkText) throws CoreException {
 
 		Matcher m = pattern.matcher(linkText);
 		if (m.find()) {
 			String name = m.group(1);
 			return name;
 		}
 		IStatus status = new Status(
 				IStatus.ERROR,
 				DLTKDebugUIPlugin.PLUGIN_ID,
 				0,
 				"Error"/* ConsoleMessages.TclFileHyperlink_Unable_to_parse_type_name_from_hyperlink__5 */,
 				null);
 		throw new CoreException(status);
 	}
 
 	protected int getLineNumber(String linkText) throws CoreException {
 
 		Matcher m = pattern.matcher(linkText);
 		if (m.find()) {
 			String lineText = m.group(2);
 			try {
 				return Integer.parseInt(lineText);
 			} catch (NumberFormatException e) {
 
 			}
 		}
 		throw new CoreException(Status.CANCEL_STATUS);
 	}
 
 	public int computeOffset(int offset, int length, TextConsole console) {
 		String linkText = null;
 
 		IDocument document = console.getDocument();
 		try {
 			int lineNumber = document.getLineOfOffset(offset);
 			IRegion lineInformation = document.getLineInformation(lineNumber);
 			int lineOffset = lineInformation.getOffset();
 			linkText = document.get(lineOffset, lineInformation.getLength());
 		} catch (BadLocationException e) {
 			return length;
 		}
 		Matcher m = pattern.matcher(linkText);
 		if (m.find()) {
 			int len = m.start(1);
 			return offset + len;
 		}
 		return offset;
 	}
 
 	public int computeLength(int offset, int length, TextConsole console) {
 		String linkText = null;
 
 		IDocument document = console.getDocument();
 		try {
 			int lineNumber = document.getLineOfOffset(offset);
 			IRegion lineInformation = document.getLineInformation(lineNumber);
 			int lineOffset = lineInformation.getOffset();
 			linkText = document.get(lineOffset, lineInformation.getLength());
 		} catch (BadLocationException e) {
 			return length;
 		}
 		Matcher m = pattern.matcher(linkText);
 		if (m.find()) {
 			int len = m.end(1) - m.start(1);
 			return len;
 		}
 		return length;
 	}
 }
