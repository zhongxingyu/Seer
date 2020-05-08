 
 public enum TokenType {
 	T_BEGIN("BEGIN", "BEGIN"),
 	T_END("END", "END"),
 	T_PRINT("PRINT", "PRINT"),
	T_READ("READ", "READ"),
	T_LEFTPAR("LEFTPAR", "\\("),
	T_RIGHTPAR("RIGHTPAR", "\\)"),
 	T_SEMICOLON("SEMICOLON", ";"),
 	T_ID("ID", "[a-zA-Z][a-zA-Z0-9_]*"),
 	T_ASSIGN("ASSIGN", ":="),
 	T_COMMA("COMMA", ","),
 	T_INTNUM("INTNUM", "[0-9]+"),
 	T_PLUS("PLUS", "\\+"),
 	T_MINUS("MINUS", "-"),
 	T_MULTIPLY("MULTIPLY", "\\*"),
 	T_MODULO("MODULO", "%"),
 	T_UNKNOWN("UNKNOWN", "\\w");
 	
 	private String identifier;
 	private String regex;
 	
 	private TokenType(String identifier, String regex) {
 		this.identifier = identifier;;
 		this.regex = regex;
 	}
 	
 	public String getRegex() {
 		return regex;
 	}
 
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	public static TokenType tokenWithIdentifier(String identifier) {
 		for (TokenType type : TokenType.values()) {
 			if (type.getIdentifier().equals(identifier)) {
 				return type;
 			}
 		}
 		
 		return T_UNKNOWN;
 	}
 }
 
