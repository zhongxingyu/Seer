 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Vladimir Belov)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.parser;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.BitSet;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.IntStream;
 import org.antlr.runtime.MismatchedSetException;
 import org.antlr.runtime.MismatchedTokenException;
 import org.antlr.runtime.NoViableAltException;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.RuleReturnScope;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.TokenStream;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.parser.AbstractSourceParser;
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.compiler.problem.DefaultProblem;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.builder.ISourceLineTracker;
 import org.eclipse.dltk.internal.core.SourceRange;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.utils.TextUtils;
 
 public class JavaScriptParser extends AbstractSourceParser {
 
 	public static final String PARSER_ID = "org.eclipse.dltk.javascript.NewParser";
 
 	private boolean typeInformationEnabled = false;
 
 	public boolean isTypeInformationEnabled() {
 		return typeInformationEnabled;
 	}
 
 	public void setTypeInformationEnabled(boolean typeInformationEnabled) {
 		this.typeInformationEnabled = typeInformationEnabled;
 	}
 
 	private static class JSInternalParser extends JSParser {
 
 		private final IProblemReporter reporter;
 		private final ISourceLineTracker lineTracker;
 
 		public JSInternalParser(TokenStream input, IProblemReporter reporter,
 				ISourceLineTracker lineTracker) {
 			super(input);
 			this.reporter = reporter;
 			this.lineTracker = lineTracker;
 		}
 
 		@Override
 		protected void reportFailure(Throwable t) {
 			if (reporter != null) {
 				reporter.reportProblem(new JSProblem(t));
 			}
 		}
 
 		@Override
 		public void displayRecognitionError(String[] tokenNames,
 				RecognitionException re) {
 			if (reporter == null)
 				return;
 			String message;
 			ISourceRange range;
 			if (re instanceof NoViableAltException) {
 				range = convert(re.token);
 				message = "Unexpected " + getTokenErrorDisplay(re.token);
 			} else if (re instanceof MismatchedTokenException) {
 				MismatchedTokenException mte = (MismatchedTokenException) re;
 				if (re.token == Token.EOF_TOKEN) {
 					message = tokenNames[mte.expecting] + " expected";
 				} else {
 					message = "Mismatched input "
 							+ getTokenErrorDisplay(re.token);
 					if (mte.expecting >= 0 && mte.expecting < tokenNames.length) {
 						message += ", " + tokenNames[mte.expecting]
 								+ " expected";
 					}
 				}
 				range = convert(re.token);
 				if (range.getLength() + range.getOffset() >= inputLength()) {
 					int stop = inputLength() - 1;
 					int start = Math.min(stop - 1, range.getOffset() - 2);
 					range = new SourceRange(start, stop - start);
 				}
 			} else if (re instanceof MismatchedSetException) {
 				MismatchedSetException mse = (MismatchedSetException) re;
 				message = "Mismatched input " + getTokenErrorDisplay(re.token);
 				if (mse.expecting != null) {
 					message += " expecting set " + mse.expecting;
 				}
 				range = convert(re.token);
 			} else {
 				message = "Syntax Error:" + re.getMessage();
 				range = convert(re.token);
 				// stop = start + 1;}
 			}
 			reporter.reportProblem(new DefaultProblem(message, 0, null,
 					ProblemSeverities.Error, range != null ? range.getOffset()
 							: -1, range != null ? range.getOffset()
 							+ range.getLength() : -1, re.line - 1));
 		}
 
 		private ISourceRange convert(Token token) {
 			if (token == Token.EOF_TOKEN) {
 				final TokenStream stream = getTokenStream();
 				int index = stream.index();
 				for (;;) {
 					--index;
 					final Token prevToken = stream.get(index);
 					if (prevToken.getType() != JSParser.WhiteSpace
 							&& prevToken.getType() != JSParser.EOL) {
 						token = prevToken;
 						break;
 					}
 				}
 				if (token == Token.EOF_TOKEN) {
 					return null;
 				}
 			}
 			int offset = lineTracker.getLineOffset(token.getLine() - 1)
 					+ Math.max(token.getCharPositionInLine(), 0);
 			return new SourceRange(offset, length(token));
 		}
 
 		private int length(Token token) {
 			String sm = token.getText();
 			return sm != null ? sm.length() : 1;
 		}
 
 		private int inputLength() {
 			return lineTracker.getLength();
 		}
 
 		/*
 		 * Standard implementation contains forgotten debug System.err.println()
 		 * and we don't need it at all.
 		 */
 		@Override
 		public void recoverFromMismatchedToken(IntStream input,
 				RecognitionException e, int ttype, BitSet follow)
 				throws RecognitionException {
 			// if next token is what we are looking for then "delete" this token
 			if (input.LA(2) == ttype) {
 				reportError(e);
 				beginResync();
 				input.consume(); // simply delete extra token
 				endResync();
 				input.consume(); // move past ttype token as if all were ok
 				return;
 			}
 			// insert "}" if expected
 			if (ttype == JSParser.RBRACE) {
 				displayRecognitionError(getTokenNames(), e);
 				return;
 			}
 			if (!recoverFromMismatchedElement(input, e, follow)) {
 				throw e;
 			}
 		}
 
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public Script parse(IModuleSource input, IProblemReporter reporter) {
 		Assert.isNotNull(input);
 		char[] source = input.getContentsAsCharArray();
 		return parse(createTokenStream(source), TextUtils
 				.createLineTracker(source), reporter);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public Script parse(String source, IProblemReporter reporter) {
 		Assert.isNotNull(source);
 		return parse(createTokenStream(source), TextUtils
 				.createLineTracker(source), reporter);
 	}
 
 	private Script parse(JSTokenStream stream, ISourceLineTracker lineTracker,
 			IProblemReporter reporter) {
 		try {
 			JSInternalParser parser = new JSInternalParser(stream, reporter,
 					lineTracker);
 			parser.setTypeInformationEnabled(typeInformationEnabled);
 			RuleReturnScope root = parser.program();
 			return new JSTransformer(stream.getTokens()).transform(root);
 		} catch (Exception e) {
 			if (DLTKCore.DEBUG)
 				e.printStackTrace();
 			if (reporter != null) {
 				reporter.reportProblem(new JSProblem(e));
 			}
 			// create empty output
			Script script = new Script();
			script.setStart(0);
			script.setEnd(lineTracker.getLength());
			return script;
 		}
 	}
 
 	public static JSTokenStream createTokenStream(char[] source) {
 		CharStream charStream = new ANTLRStringStream(source, source.length);
 		return new DynamicTokenStream(new JavaScriptTokenSource(charStream));
 	}
 
 	public static JSTokenStream createTokenStream(String source) {
 		CharStream charStream = new ANTLRStringStream(source);
 		return new DynamicTokenStream(new JavaScriptTokenSource(charStream));
 	}
 
 	/**
 	 * @param input
 	 * @param encoding
 	 * @return
 	 * @throws IOException
 	 */
 	public static JSTokenStream createTokenStream(InputStream input,
 			String encoding) throws IOException {
 		CharStream charStream = new ANTLRInputStream(input, encoding);
 		return new DynamicTokenStream(new JavaScriptTokenSource(charStream));
 	}
 
 }
