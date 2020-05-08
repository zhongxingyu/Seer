 package us.hexcoder.asm6502.highlighter;
 
 import com.intellij.lexer.Lexer;
 import com.intellij.openapi.editor.colors.TextAttributesKey;
 import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
 import com.intellij.psi.TokenType;
 import com.intellij.psi.tree.IElementType;
 import org.jetbrains.annotations.NotNull;
 import us.hexcoder.asm6502.lexer.Asm6502LexerAdapter;
 import us.hexcoder.asm6502.psi.Asm6502Types;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * User: 67726e
  */
 
 public class Asm6502SyntaxHighlighter extends SyntaxHighlighterBase {
 	private static final Map<IElementType, TextAttributesKey[]> TOKEN_HIGHLIGHTS =
 			new HashMap<IElementType, TextAttributesKey[]>();
 
 	static {
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.MNEMONIC, Asm6502TextAttribute.MNEMONIC.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.DIRECTIVE, Asm6502TextAttribute.DIRECTIVE.toArray());
 
 		// Mnemonic operands
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.BINARY_OPERAND, Asm6502TextAttribute.NUMBER.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.DECIMAL_OPERAND, Asm6502TextAttribute.NUMBER.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.HEXADECIMAL_OPERAND, Asm6502TextAttribute.NUMBER.toArray());
 
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.ADDRESS_VALUE, Asm6502TextAttribute.NUMBER.toArray());
 
 		// Directive arguments
		TOKEN_HIGHLIGHTS.put(Asm6502Types.DIRECTIVE_STRING, Asm6502TextAttribute.STRING.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.BINARY_NUMBER, Asm6502TextAttribute.NUMBER.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.DECIMAL_NUMBER, Asm6502TextAttribute.NUMBER.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.HEXADECIMAL_NUMBER, Asm6502TextAttribute.NUMBER.toArray());
 
 		// Misc. punctuation
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.COMMENT, Asm6502TextAttribute.COMMENT.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.LABEL, Asm6502TextAttribute.LABEL.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.LABEL_OPERAND, Asm6502TextAttribute.LABEL.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.COMMA, Asm6502TextAttribute.COMMA.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.OPEN_PAREN, Asm6502TextAttribute.PARENTHESIS.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.CLOSE_PAREN, Asm6502TextAttribute.PARENTHESIS.toArray());
 
 		// Accumulator & registers
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.ACCUMULATOR_OPERAND, Asm6502TextAttribute.REGISTER.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.REGISTER_X, Asm6502TextAttribute.REGISTER.toArray());
 		TOKEN_HIGHLIGHTS.put(Asm6502Types.REGISTER_Y, Asm6502TextAttribute.REGISTER.toArray());
 
 		TOKEN_HIGHLIGHTS.put(TokenType.BAD_CHARACTER, Asm6502TextAttribute.INVALID.toArray());
 	}
 
 	@NotNull
 	@Override
 	public Lexer getHighlightingLexer() {
 		return new Asm6502LexerAdapter();
 	}
 
 	@NotNull
 	@Override
 	public TextAttributesKey[] getTokenHighlights(IElementType iElementType) {
 		TextAttributesKey[] highlights = TOKEN_HIGHLIGHTS.get(iElementType);
 
 		if (highlights == null) {
 			highlights = new TextAttributesKey[0];
 		}
 
 		return highlights;
 	}
 }
