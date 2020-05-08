 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.handlers;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Caret;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import net.colar.netbeans.fan.FanLanguage;
 import net.colar.netbeans.fan.FanTokenID;
 import net.colar.netbeans.fan.antlr.FanLexer;
 import net.colar.netbeans.fan.antlr.LexerUtils;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.editor.Utilities;
 import org.netbeans.modules.csl.api.EditorOptions;
 import org.netbeans.modules.csl.api.KeystrokeHandler;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.spi.GsfUtilities;
 import org.netbeans.modules.csl.spi.ParserResult;
 import org.netbeans.modules.editor.indent.api.IndentUtils;
 
 /**
  * Impl. of keystrokeHandler
  * Provides support for closing bracket/quote insertion and the likes
  * Also deals with trying to guess/insert proper indentation.
  * @author tcolar
  */
 public class FanKeyStrokeHandler implements KeystrokeHandler
 {
 	/* Array of special patterns that will cause increased intent on the next line
 	 * if/else/while/for/try/finaly/catch without blocks({})
 	 * Switch items (case / default)
 	 * Note .*?  = match anything NON-greedily
 	 */
 
 	private static Pattern[] INDENT_AFTER_PATTERNS =
 	{
 		// single line if
 		Pattern.compile("^\\s*if\\s*\\(.*?\\)[^{]*$"),
 		// single line else
 		Pattern.compile("^\\s*else\\s*(if\\s*\\(.*?\\))?[^{]*$"),
 		// switch: case / default
 		Pattern.compile("^\\s*case\\s*[^:]*:\\s*$"),
 		Pattern.compile("^\\s*default\\s*:\\s*$"),
 		// try catch finaly
 		Pattern.compile("^\\s*try\\s*$"),
 		Pattern.compile("^\\s*finally\\s*$"),
 		Pattern.compile("^\\s*catch(\\(.*?\\))?\\s*$"),
 		// while
 		Pattern.compile("^\\s*while\\s*\\(.*?\\)[^{]*$"),
 		// for
 		Pattern.compile("^\\s*for\\s*\\(.*?\\)[^{]*$"),
 	};
 	/**
 	 * Items that should indent one level less that line above
 	 * If not immeditaly following a bracket
 	 */
 	private static Pattern[] DEINDENT_PATTERNS =
 	{
 		Pattern.compile("^\\s*else"),
 		Pattern.compile("^\\s*catch"),
 		Pattern.compile("^\\s*finally"),
 		// those could follow a bracket? unlikely 
 		Pattern.compile("^\\s*case\\s*[^:]*:\\s*"),
 		Pattern.compile("^\\s*default\\s*:\\s*"),
 	};
 	private int lastAdditions;
 
 	@Override
 	public boolean beforeCharInserted(Document document, int caretOffset, JTextComponent target, char car) throws BadLocationException
 	{
 		// TODO: typinf a """ is difficult ...
 		BaseDocument doc = (BaseDocument) document;
 		if (!isInsertMatchingEnabled(doc))
 		{
 			return false;
 		}
 		lastAdditions = 0;
 
 		String toInsert = "";
 		String prev = null;
 		if (caretOffset > 0)
 		{
 			prev = doc.getText(caretOffset - 1, 1);
 		}
 		Token<? extends FanTokenID> token = LexerUtils.getFanTokenAt(document, caretOffset);
 
 		// If User types closing item we closed automaticaly, skip it
 		if (token != null)
 		{
 			int ord = token.id().ordinal();
 			if ((car == '"' && ord == FanLexer.INC_STR) ||
 					(car == '"' && ord == FanLexer.STR) ||
 					(car == '"' && ord == FanLexer.QUOTSTR) ||
 					(car == '`' && ord == FanLexer.INC_URI) ||
 					(car == '\'' && ord == FanLexer.CHAR) ||
 					(car == '`' && ord == FanLexer.INC_URI) ||
 					(car == '`' && ord == FanLexer.URI) ||
 					(car == ']' && ord == FanLexer.SQ_BRACKET_R) ||
 					(car == ')' && ord == FanLexer.PAR_R) ||
 					(car == '}' && ord == FanLexer.BRACKET_R))
 			{
 				// just skip the existing same characters
 				target.getCaret().setDot(caretOffset + 1);
 				return true;
 			}
 		}
 		// Same but dual characters
 		if ((car == '/' && prev.equals("*") && token.id().ordinal() == FanLexer.MULTI_COMMENT) ||
 				(car == '>' && prev.equals("|") && token.id().ordinal() == FanLexer.DSL))
 		{
 			// remove previous char and then skip existing one
 			doc.remove(caretOffset - 1, 1);
 			target.getCaret().setDot(caretOffset + 1);
 			return true;
 		}
 
 		// If within those tokens, don't do anything special
 		if (token.id().ordinal() == FanLexer.STR ||
 				token.id().ordinal() == FanLexer.CHAR ||
 				token.id().ordinal() == FanLexer.DOC ||
 				token.id().ordinal() == FanLexer.DSL ||
 				token.id().ordinal() == FanLexer.EXEC_COMMENT ||
 				token.id().ordinal() == FanLexer.INC_COMMENT ||
 				token.id().ordinal() == FanLexer.INC_DSL ||
 				token.id().ordinal() == FanLexer.INC_STR ||
 				token.id().ordinal() == FanLexer.INC_URI ||
 				token.id().ordinal() == FanLexer.LINE_COMMENT ||
 				token.id().ordinal() == FanLexer.MULTI_COMMENT ||
 				token.id().ordinal() == FanLexer.QUOTSTR ||
 				token.id().ordinal() == FanLexer.STR ||
 				token.id().ordinal() == FanLexer.URI)
 		{
 			return false;
 		}
 
 		// Automatically add closing item/brace when opening one entered
 		switch (car)
 		{
 			case '{':
 				toInsert = "}";
 				break;
 			case '(':
 				toInsert = ")";
 				break;
 			case '[':
 				toInsert = "]";
 				break;
 			case '`':
 				toInsert = "`";
 				break;
 			case '\'':
 				toInsert = "'";
 				break;
 			case '"':
 				toInsert = "\"";
 				break;
 			case '|':
 				if (prev.equals("<"))
 				{
 					toInsert = "|>";
 				}
 				break;
 			case '*':
 				if (prev.equals("/"))
 				{
 					toInsert = "*/";
 				}
 				break;
 		}
 		// do the insertion job
 		if (toInsert.length() > 0)
 		{
 			doc.insertString(caretOffset, toInsert, null);
 			target.getCaret().setDot(caretOffset);
 			if (toInsert.length() > 0)
 			{
 				lastAdditions = toInsert.length() - 1;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean afterCharInserted(Document document, int caretOffset, JTextComponent target, char car) throws BadLocationException
 	{
 		// deal with fixing identation for special cases (switch, if etc...)
 		BaseDocument doc = (BaseDocument) document;
 		int indentSize = IndentUtils.indentLevelSize(document);
 		int lineBegin = Utilities.getRowStart(doc, caretOffset);
 		int lineEnd = Utilities.getRowEnd(doc, caretOffset);
 		String line = doc.getText(lineBegin, lineEnd - lineBegin);
 		int above = -1;
 		String prevLine = "";
 		if (lineBegin > 1)
 		{
 			above = Utilities.getFirstNonEmptyRow(doc, lineBegin - 1, false);
 			int prevBegin = Utilities.getRowStart(doc, above);
 			int prevEnd = Utilities.getRowEnd(doc, above);
 			prevLine = doc.getText(prevBegin, prevEnd - prevBegin);
 		}
 		int indent = GsfUtilities.getLineIndent(doc, caretOffset);
 
 
 		// If user enters { after one of the identing patterns, we need to dedent
 		if (car == '{' && line.trim().startsWith("{") && above > -1)
 		{
 			for (int i = 0; i != INDENT_AFTER_PATTERNS.length; i++)
 			{
 				if (INDENT_AFTER_PATTERNS[i].matcher(prevLine).matches())
 				{
 					// fix indent to be same as prevline
 					indent = GsfUtilities.getLineIndent(doc, above);
 					break;
 				}
 			}
 		} else
 		{
 			//Check some special patterns, decrease indent of current line (ex - deafult:)
 			// except if immediatly following a closing bracket
 			if (!prevLine.trim().endsWith("}"))
 			{
 				for (int i = 0; i != DEINDENT_PATTERNS.length; i++)
 				{
 					if (DEINDENT_PATTERNS[i].matcher(line).matches())
 					{
 						if (above >= 0)
 						{
 							// start indent as same as previous non empty line
 							indent = GsfUtilities.getLineIndent(doc, above);
 						}
 						// and decrease indent (>=0)
 						indent = indent >= indentSize ? indent - indentSize : indent;
 						break;
 					}
 				}
 			}
 		}
 		// set the indentation
 		GsfUtilities.setLineIndentation(doc, caretOffset, indent);
 		return false;
 	}
 
 	@Override
 	public boolean charBackspaced(Document document, int caretOffset, JTextComponent target, char car) throws BadLocationException
 	{
 		BaseDocument doc = (BaseDocument) document;
 		if (!isInsertMatchingEnabled(doc))
 		{
 			return false;
 		}
 
 		// If we just auto-added chars in beforeCharInserted() and the user press backspace right away,
 		// we remove them now.
 		if (lastAdditions > 0)
 		{
 			doc.remove(caretOffset, lastAdditions);
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public int beforeBreak(Document document, int caretOffset, JTextComponent target) throws BadLocationException
 	{
 		//If within DSL, STR, URI don't indent anyhting as they can be multiline
 		Token tk = LexerUtils.getFanTokenAt(document, caretOffset);
 		if (tk != null)
 		{
 			int ord = tk.id().ordinal();
 			if (ord == FanLexer.DSL |
 					ord == FanLexer.INC_DSL |
 					ord == FanLexer.INC_STR |
 					ord == FanLexer.STR |
 					ord == FanLexer.QUOTSTR |
 					ord == FanLexer.URI |
 					ord == FanLexer.INC_URI)
 			{
 				return -1;
 			}
 		}
 
 		// Deal with indentation
 		String NL =/*Character.LINE_SEPARATOR*/ "\n";
 		int indentSize = IndentUtils.indentLevelSize(document);
 		Caret caret = target.getCaret();
 		BaseDocument doc = (BaseDocument) document;
 
 		int lineBegin = Utilities.getRowStart(doc, caretOffset);
 		int lineEnd = Utilities.getRowEnd(doc, caretOffset);
 
 		int above = Utilities.getPositionAbove(target, caretOffset, 0);
 		int prevLineBegin = Utilities.getRowStart(doc, above);
 		int prevLineEnd = Utilities.getRowEnd(doc, above);
 
 		String line = null;
 		String prevLine = null;
 		if (lineBegin > -1 && lineEnd > lineBegin)
 		{
 			line = doc.getText(lineBegin, lineEnd - lineBegin);
 		}
 
 		if (prevLineBegin > -1 && prevLineEnd > prevLineBegin)
 		{
 			prevLine = doc.getText(prevLineBegin, prevLineEnd - prevLineBegin);
 		}
 
 		String lineHead = doc.getText(lineBegin, caretOffset - lineBegin);
 		String lineTail = doc.getText(caretOffset, lineEnd - caretOffset);
 
 		// standard indent (same as the line we pressed return on)
 		int indent = 0;
 		if (lineBegin > 0)
 		{
 			indent = IndentUtils.lineIndent(document, lineBegin);
 		}
 
 		int dotOffset = 0;
 		String insert = "";
 		String trimmedLine = line.trim();
 		if (line != null)
 		{
 			System.err.println("Head: " + lineHead);
 			// If within doc -> insert the ** on next line
 			// insert only if the current doc line is not empty("**") - except for first one (empty ok)
 			boolean isFirstDocLine = prevLine == null || !prevLine.trim().startsWith("**");
 			if (trimmedLine.startsWith("**") && (isFirstDocLine || trimmedLine.length() > 3))
 			{
 				insert = "** ";
 			} else if (lineHead.trim().endsWith("{"))
 			{
 				if (lineTail.trim().startsWith("}"))
 				{
 					String extraIndent = IndentUtils.createIndentString(document, indent);
 					insert = NL + extraIndent;
 					// TODO: this probably not good if using spaces instead of tabs ??
 					dotOffset = -(1 + extraIndent.length());
 				}
 
 				indent += indentSize;
 			} else
 			{
 				// increase indent of next line for special patterns, see getSpecialIndentSize()
 				indent += indentSize * getSpecialIndentSize(lineHead);
 			}
 
 		}
 
 		// Do the insertion and the indent
 		String indentStr = IndentUtils.createIndentString(document, indent);
 		doc.insertString(caretOffset, indentStr + insert, null);
 		caret.setDot(caretOffset);
 		return caretOffset + indentStr.length() + insert.length() + 1 + dotOffset;
 	}
 
 	/**
 	 * Helps finding mathcing opening/closing items (ex: {})
 	 * @param document
 	 * @param caretOffset
 	 * @return
 	 */
 	@Override
 	public OffsetRange findMatching(
 			Document document, int caretOffset)
 	{
 		TokenSequence ts = LexerUtils.getFanTokenSequence(document);
 		int searchOffset = 2; // start after rightToken
 
 		// Prefer matching the token to the right of caret
 		Token token = LexerUtils.getFanTokenAt(document, caretOffset + 1);
 		if (token == null)
 		{
 			// if rightToken is null, use left token
 			token = LexerUtils.getFanTokenAt(document, caretOffset);
 			searchOffset =
 					1; // start after leftToken
 		} else
 		{
 			int ord = token.id().ordinal();
 			// if rightToken is not 'matcheable', use left token
 			if (ord != FanLexer.PAR_L && ord != FanLexer.PAR_R &&
 					ord != FanLexer.SQ_BRACKET_L && ord != FanLexer.SQ_BRACKET_R &&
 					ord != FanLexer.BRACKET_L && ord != FanLexer.BRACKET_R)
 			{
 				token = LexerUtils.getFanTokenAt(document, caretOffset);
 				searchOffset =
 						1; // start after leftToken
 			}
 
 		}
 
 		if (token != null)
 		{
 			int ord = token.id().ordinal();
 			//Ok, now try to find the matching token
 			switch (ord)
 			{
 				case FanLexer.PAR_L:
 					ts.move(caretOffset + searchOffset);// start after opening char
 					return LexerUtils.findRangeFromOpening(document, ts, FanLexer.PAR_L, FanLexer.PAR_R);
 				case FanLexer.PAR_R:
 					ts.move(caretOffset + searchOffset - 1);// start before opening char (since going backward)
 					return LexerUtils.findRangeFromClosing(document, ts, FanLexer.PAR_L, FanLexer.PAR_R);
 				case FanLexer.BRACKET_L:
 					ts.move(caretOffset + searchOffset);
 					return LexerUtils.findRangeFromOpening(document, ts, FanLexer.BRACKET_L, FanLexer.BRACKET_R);
 				case FanLexer.BRACKET_R:
 					ts.move(caretOffset + searchOffset - 1);
 					return LexerUtils.findRangeFromClosing(document, ts, FanLexer.BRACKET_L, FanLexer.BRACKET_R);
 				case FanLexer.SQ_BRACKET_L:
 					ts.move(caretOffset + searchOffset);
 					return LexerUtils.findRangeFromOpening(document, ts, FanLexer.SQ_BRACKET_L, FanLexer.SQ_BRACKET_R);
 				case FanLexer.SQ_BRACKET_R:
 					ts.move(caretOffset + searchOffset - 1);
 					return LexerUtils.findRangeFromClosing(document, ts, FanLexer.SQ_BRACKET_L, FanLexer.SQ_BRACKET_R);
 			}
 
 		}
 		//default - no match
 		return OffsetRange.NONE;
 	}
 
 	@Override
 	public List<OffsetRange> findLogicalRanges(ParserResult arg0, int arg1)
 	{
 		// not impl yet.
 		// what is this used for ?   - provide using FanStructureAnalyzer ??
 		return Collections.emptyList();
 	}
 
 	@Override
 	public int getNextWordOffset(Document arg0, int arg1, boolean arg2)
 	{
 		// not impl, default will be fine.
 		return -1;
 	}
 
 	/**
 	 * wether brcaket matching is turned on
 	 * @param doc
 	 * @return
 	 */
 	public boolean isInsertMatchingEnabled(BaseDocument doc)
 	{
 		// Default: true
 		EditorOptions options = EditorOptions.get(FanLanguage.FAN_MIME_TYPE);
 		if (options != null)
 		{
 			return options.getMatchBrackets();
 		}
 
 		return true;
 	}
 
 	/**
 	 * Return how much extra ident we will need for special patterns
 	 * such as for / if /case etc...
 	 * @param lineText
 	 * @return
 	 */
 	private int getSpecialIndentSize(String lineText)
 	{
 		for (int i = 0; i != INDENT_AFTER_PATTERNS.length; i++)
 		{
 			if (INDENT_AFTER_PATTERNS[i].matcher(lineText).matches())
 			{
 				return 1;
 			}
 
 		}
 		return 0;
 	}
 }
