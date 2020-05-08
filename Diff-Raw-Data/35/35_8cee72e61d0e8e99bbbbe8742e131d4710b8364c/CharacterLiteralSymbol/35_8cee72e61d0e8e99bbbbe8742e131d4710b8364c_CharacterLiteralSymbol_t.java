 package cs444.parser.symbols.ast;
 
 import cs444.parser.symbols.ATerminal;
 
 public class CharacterLiteralSymbol extends ATerminal {
 
 	public final char charVal;
 
 	public CharacterLiteralSymbol(ATerminal terminal) {
 
 	    super("CharacterLiteral", terminal.value);
 
 		char cvalue = value.charAt(1);
 
 		int idx = value.indexOf('\\');
 		if (idx != -1) {
 
 			char escapeChar = value.charAt(idx + 1);
			if (StringEscapeUtils.isOctalDigit(escapeChar)) {
				
 				String octal = value.substring(idx + 1, value.length() - 1);
 				cvalue = StringEscapeUtils.octalEscape(octal);
 			} else {
 				cvalue = StringEscapeUtils.simpleEscape(escapeChar);
 			}
 		}
 
 		this.charVal = cvalue;
 	}
 
 	@Override
 	public boolean empty() {
 		return false;
 	}
 }
