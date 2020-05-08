 package com.gamevm.plugin.editors;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.TokenSource;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextAttribute;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.ITokenScanner;
 import org.eclipse.swt.graphics.FontData;
 
 import com.gamevm.compiler.parser.GCASTLexer;
 
 public abstract class AbstractTokenScanner implements ITokenScanner {
 
 	private TokenSource tokenSource;
 	private Token lastToken;
 
 	private int tokenOffset = 0;
 
 	protected abstract IToken convert(Token token);
 
 	@Override
 	public void setRange(IDocument document, int offset, int length) {
 		try {
 			tokenOffset = offset;
 			lastToken = null;
 			CharStream input = new ANTLRStringStream(document.get(offset, length));
 			tokenSource = new GCASTLexer(input);
 		} catch (BadLocationException e) {
 		}
 	}
 
 	private void printToken(IToken token) {
 		TextAttribute attr = (TextAttribute) token.getData();
 		if (attr != null && attr.getFont() != null) {
 			FontData fd = attr.getFont().getFontData()[0];
 		}
 	}
 
 	@Override
 	public IToken nextToken() {
 		tokenOffset = tokenOffset + getTokenLength();
 		lastToken = tokenSource.nextToken();
 		if (lastToken.getType() == GCASTLexer.EOF)
 			return org.eclipse.jface.text.rules.Token.EOF;
 
 		IToken result = convert(lastToken);
 		printToken(result);
 		return result;
 	}
 
 	@Override
 	public int getTokenOffset() {
 		return tokenOffset;
 	}
 
 	@Override
 	public int getTokenLength() {
		if (lastToken != null)
			return lastToken.getText().length();
		else
 			return 0;
 	}
 
 }
