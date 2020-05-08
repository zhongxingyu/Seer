 package swp_compiler_ss13.fuc.gui.text.token;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import swp_compiler_ss13.common.lexer.Token;
 import swp_compiler_ss13.common.lexer.TokenType;
 import swp_compiler_ss13.fuc.gui.text.ColorWrapper;
 import swp_compiler_ss13.fuc.gui.text.StringColourPair;
 import swp_compiler_ss13.fuc.gui.text.StringColourPair.DefaultColorWrapper;
 import swp_compiler_ss13.fuc.gui.text.Text_Controller;
 import swp_compiler_ss13.fuc.gui.text.Text_Model;
 
 /**
  * @author "Eduard Wolf"
  * 
  */
 public class TextGUITokenVisualizationModel extends Text_Model {
 
 	private final Map<TokenType, ColorWrapper> tokenColor;
 	private boolean showLineColumnInformation = true;
 
 	public TextGUITokenVisualizationModel(Text_Controller controller) {
 		super(controller, ModelType.TOKEN);
 		tokenColor = new HashMap<>();
 		ColorWrapper wrapper;
 		for (TokenType type : TokenType.values()) {
 			switch (type) {
 			case AND:
 			case OR:
 			case NOT:
 			case NOT_EQUALS:
 			case EQUALS:
 			case GREATER:
 			case GREATER_EQUAL:
 			case LESS:
 			case LESS_OR_EQUAL:
 				wrapper = DefaultColorWrapper.BLUE;
 				break;
 			case ASSIGNOP:
 				wrapper = DefaultColorWrapper.GREEN;
 				break;
 			case BOOL_SYMBOL:
 			case DOUBLE_SYMBOL:
 			case RECORD_SYMBOL:
 			case LONG_SYMBOL:
 				wrapper = DefaultColorWrapper.ORANGE;
 				break;
 			case BREAK:
 			case WHILE:
 			case DO:
 			case IF:
 			case ELSE:
 			case RETURN:
 			case PRINT:
 				wrapper = DefaultColorWrapper.YELLOW;
 				break;
 			case NUM:
 			case REAL:
 			case STRING:
 			case TRUE:
 			case FALSE:
 				wrapper = DefaultColorWrapper.RED;
 				break;
 			case COMMENT:
 			case EOF:
 			case NOT_A_TOKEN:
 				wrapper = DefaultColorWrapper.GRAY;
 				break;
 			case DIVIDE:
 			case TIMES:
 			case PLUS:
 			case MINUS:
 				wrapper = DefaultColorWrapper.CYAN;
 				break;
 			case ID:
 			case DOT:
 				wrapper = DefaultColorWrapper.ORANGE;
 				break;
 			case LEFT_BRACE:
 			case LEFT_BRACKET:
 			case RIGHT_BRACE:
 			case RIGHT_BRACKET:
 			case LEFT_PARAN:
 			case RIGHT_PARAN:
 			case SEMICOLON:
 			default:
 				wrapper = DefaultColorWrapper.BLACK;
 				break;
 			}
 			tokenColor.put(type, wrapper);
 		}
 	}
 
 	@Override
 	protected List<StringColourPair> tokenToViewInformation(List<Token> tokens) {
 		if (tokens == null) {
 			return new ArrayList<>(Arrays.asList(new StringColourPair()));
 		}
 		List<StringColourPair> result = new ArrayList<>();
 		Integer line = tokens.get(0).getLine();
 		StringBuilder text;
 		for (Token token : tokens) {
 			text = new StringBuilder();
 			text.append(token.getLine().equals(line) ? "" : "\n");
 			text.append('<');
 			line = token.getLine();
 			if (token.getTokenType() == TokenType.NUM || token.getTokenType() == TokenType.REAL
 					|| token.getTokenType() == TokenType.ID) {
 				text.append(token.getTokenType());
 				text.append(", ");
 			} else {}
 			text.append(token.getValue());
 			if (showLineColumnInformation) {
 				text.append(", ");
 				text.append(token.getLine());
 				text.append(", ");
 				text.append(token.getColumn());
 			}
 			text.append('>');
 			result.add(new StringColourPair().setText(text.toString()).setColor(
 					tokenColor.get(token.getTokenType())));
 		}
 		return result;
 	}
 
 	public void toggleLineColumnInfo() {
 		showLineColumnInformation = !showLineColumnInformation;
 	}
 
 }
