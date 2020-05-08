 package cs444.parser.symbols.ast;
 
 import cs444.parser.symbols.ATerminal;
 
 public class StringLiteralSymbol extends ATerminal {
 
 	public final String strValue;
 
 	public StringLiteralSymbol(ATerminal terminal) {
 		super("StringLiteral", terminal.value);
 
 		StringBuilder builder = new StringBuilder();
 
 		int idx = 1;
 		while (idx < value.length() - 1) {
 			char ch = value.charAt(idx);
 			if (ch != '\\') {
 				builder.append(ch);
 				idx++;
 			} else {
 				idx++;
 				char escapeChar = value.charAt(idx);
				if (StringEscapeUtils.isOctalDigit(escapeChar)) {
 					String str = "";
 					int offset = 0;
					while (StringEscapeUtils.isOctalDigit(value.charAt(idx + offset))) {
 						str += value.charAt(idx + offset);
 						offset++;
 					}
 					builder.append(StringEscapeUtils.octalEscape(str));
 					idx += offset;
 				} else {
 					builder.append(StringEscapeUtils.simpleEscape(escapeChar));
 					idx++;
 				}
 			}
 		}
 
 		this.strValue = builder.toString();
 	}
 
 	@Override
 	public boolean empty() {
 		return false;
 	}
 }
