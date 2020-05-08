 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.editor;
 
 import org.eclipse.dltk.internal.ui.editor.BracketInserter;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
 import org.eclipse.dltk.javascript.internal.ui.text.Symbols;
 import org.eclipse.dltk.javascript.scriptdoc.JavaHeuristicScanner;
 import org.eclipse.dltk.javascript.ui.text.IJavaScriptPartitions;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.BadPositionCategoryException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.graphics.Point;
 
 public class JavaScriptBracketInserter extends BracketInserter {
 
 	JavaScriptBracketInserter(ScriptEditor scriptEditor) {
 		super(scriptEditor);
 	}
 
	@Override
 	public void verifyKey(VerifyEvent event) {
 
 		// early pruning to slow down normal typing as little as possible
 		if (!event.doit
 				|| this.editor.getInsertMode() != ScriptEditor.SMART_INSERT)
 			return;
 		switch (event.character) {
 		case '(':
 		case '<':
 		case '[':
 		case '\'':
 		case '\"':
 			break;
 		default:
 			return;
 		}
 
 		final ISourceViewer sourceViewer = this.editor.getScriptSourceViewer();
 		IDocument document = sourceViewer.getDocument();
 
 		final Point selection = sourceViewer.getSelectedRange();
 		final int offset = selection.x;
 		final int length = selection.y;
 
 		try {
 			IRegion startLine = document.getLineInformationOfOffset(offset);
 			IRegion endLine = document.getLineInformationOfOffset(offset
 					+ length);
 
 			JavaHeuristicScanner scanner = new JavaHeuristicScanner(document);
 			int nextToken = scanner.nextToken(offset + length, endLine
 					.getOffset()
 					+ endLine.getLength());
 			String next = nextToken == Symbols.TokenEOF ? null : document.get(
					offset + length, scanner.getPosition() - offset - length)
					.trim();
 			int prevToken = scanner.previousToken(offset - 1, startLine
 					.getOffset());
 			int prevTokenOffset = scanner.getPosition() + 1;
 			String previous = prevToken == Symbols.TokenEOF ? null : document
 					.get(prevTokenOffset, offset - prevTokenOffset).trim();
 
 			switch (event.character) {
 			case '(':
 				if (!fCloseBrackets || nextToken == Symbols.TokenLPAREN
 						|| nextToken == Symbols.TokenIDENT || next != null
 						&& next.length() > 1)
 					return;
 				break;
 
 			case '<':
 				if (!(fCloseAngularBrackets && fCloseBrackets)
 						|| nextToken == Symbols.TokenLESSTHAN
 						|| prevToken != Symbols.TokenLBRACE
 						&& prevToken != Symbols.TokenRBRACE
 						&& prevToken != Symbols.TokenSEMICOLON
 						&& prevToken != Symbols.TokenSYNCHRONIZED
 						&& prevToken != Symbols.TokenSTATIC
 						&& (prevToken != Symbols.TokenIDENT || !isAngularIntroducer(previous))
 						&& prevToken != Symbols.TokenEOF)
 					return;
 				break;
 
 			case '[':
 				if (!fCloseBrackets || nextToken == Symbols.TokenIDENT
 						|| next != null && next.length() > 1)
 					return;
 				break;
 
 			case '\'':
 			case '"':
 				if (!fCloseStrings || nextToken == Symbols.TokenIDENT
 						|| prevToken == Symbols.TokenIDENT || next != null
 						&& next.length() > 1 || previous != null
 						&& previous.length() > 1)
 					return;
 				break;
 
 			default:
 				return;
 			}
 
 			if (!validatePartitioning(document, offset,
 					IJavaScriptPartitions.JS_PARTITIONING)) {
 				return;
 			}
 
 			if (!this.editor.validateEditorInputState())
 				return;
 
 			insertBrackets(document, offset, length, event.character,
 					getPeerCharacter(event.character));
 
 			event.doit = false;
 
 		} catch (BadLocationException e) {
 			DLTKUIPlugin.log(e);
 		} catch (BadPositionCategoryException e) {
 			DLTKUIPlugin.log(e);
 		}
 	}
 
 }
