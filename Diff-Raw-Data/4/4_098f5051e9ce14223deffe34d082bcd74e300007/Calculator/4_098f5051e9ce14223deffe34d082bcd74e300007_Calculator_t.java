 package hunternif.rpn;
 
 import hunternif.rpn.token.BasicFunction;
 import hunternif.rpn.token.Token;
 import hunternif.rpn.token.TokenComputable;
 import hunternif.rpn.token.TokenConstant;
 import hunternif.rpn.token.TokenFunction;
 import hunternif.rpn.token.TokenOperator;
 import hunternif.rpn.token.TokenValue;
 import hunternif.util.Tree;
 import hunternif.util.Tree.Node;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Calculator {
 	private static final Pattern numberPattern = Pattern.compile("\\A(\\d+(\\.\\d*)?).*");
 	
 	/** Maps to tokens first character of their notation. This provides for faster token search. */
 	private static final Map<Character, List<Token>> tokenFirstCharMap = new Hashtable<>();
 	
 	public static void registerToken(Token token) {
 		Character c = token.notation.charAt(0);
 		List<Token> list = tokenFirstCharMap.get(c);
 		if (list == null) {
 			list = new ArrayList<>();
 			tokenFirstCharMap.put(c, list);
 		}
 		list.add(token);
 	}
 	
 	static {
 		for (TokenOperator op : TokenOperator.operators) {
 			registerToken(op);
 		}
 		for (TokenFunction func : BasicFunction.functions) {
 			registerToken(func);
 		}
 		registerToken(new TokenConstant("pi", Math.PI));
 		registerToken(new TokenConstant("e", Math.E));
 		registerToken(Token.BRACKET_LEFT);
 		registerToken(Token.BRACKET_RIGHT);
 		registerToken(Token.COMMA);
 	}
 	
 	public static double calculate(String input) throws CalculationException {
 		List<Token> tokens = tokenize(input);
 		Tree<Token> tree = buildSyntaxTree(tokens);
 		List<Token> rpnSequence = tree.sequentializePostOrder(); 
 		return calculateRPN(rpnSequence);
 	}
 	
 	protected static List<Token> tokenize(String input) throws CalculationException {
 		List<Token> tokens = new ArrayList<>();
 		// Remove spaces:
 		input = input.replaceAll("\\s", "");
 		
 		// Add the argument '0' to unary operator '-':
 		input = input.replaceAll("\\A-", "0-");
 		input = input.replaceAll("\\(-", "(0-");
 		input = input.replaceAll(",-", ",0-");
 		
 		while (!input.isEmpty()) {
 			// Try number
 			Matcher numberMatcher = numberPattern.matcher(input);
 			if (numberMatcher.find()) {
 				String found = numberMatcher.group(1);
 				input = input.substring(found.length());
 				double value = Double.parseDouble(found);
 				tokens.add(new TokenValue(value));
 			}
 			if (input.isEmpty()) {
 				break;
 			}
 			
 			List<Token> list = tokenFirstCharMap.get(input.charAt(0));
 			if (list == null || list.isEmpty()) {
 				throw new CalculationException("Unknown expression: " + input); 
 			}
 			// As tokens in this list overlap, only apply the one with the longest notation
 			Token longestToken = null;
 			for (Token token : list) {
 				if (input.startsWith(token.notation)) {
 					if (longestToken == null || longestToken.notation.length() < token.notation.length()) {
 						longestToken = token;
 					}
 				}
 			}
			if (longestToken == null) {
				throw new CalculationException("Unrecognized token: " + input);
			}
 			input = input.substring(longestToken.notation.length());
 			tokens.add(longestToken);
 		}
 		return tokens;
 	}
 	
 	protected static Tree<Token> buildSyntaxTree(List<Token> list) throws CalculationException {
 		Node<Token> headNode = new Node<Token>(null, null);
 		for (Token curToken : list) {
 			if (headNode.data == null) {
 				headNode.data = curToken;
 			} else {
 				if (curToken == Token.BRACKET_LEFT) {
 					if (headNode.data instanceof TokenComputable || headNode.data == Token.BRACKET_LEFT) {
 						Node<Token> child = new Node<Token>(headNode, curToken);
 						headNode.addChild(child);
 						headNode = child;
 					} else {
 						throw new CalculationException("Unexpected opening bracket token");
 					}
 				} else if (curToken == Token.BRACKET_RIGHT) {
 					// Go up until a matching opening bracket is found:
 					while (headNode.data != Token.BRACKET_LEFT) {
 						headNode = headNode.parent;
 						if (headNode == null) {
 							throw new CalculationException("Unexpected closing bracket token");
 						}
 					}
 					if (headNode.parent != null && headNode.parent.data instanceof TokenComputable
 							&& !((TokenComputable)headNode.parent.data).isInfix()) {
 						Node<Token> parent = headNode.parent;
 						parent.absorbNode(headNode);
 						headNode = parent;
 					} else {
 						headNode.data = Token.BRACKET_COMPLETE;
 					}
 				} else if (curToken instanceof TokenValue) {
 					if (headNode.data == Token.BRACKET_LEFT) {
 						Node<Token> childNode = new Node<Token>(headNode, curToken);
 						headNode.addChild(childNode);
 						headNode = childNode;
 					} else if (headNode.data instanceof TokenComputable) {
 						Node<Token> childNode = new Node<Token>(headNode, curToken);
 						headNode.addChild(childNode);
 					} else {
 						throw new CalculationException("Unexpected value token: " + curToken.notation);
 					}
 				} else if (curToken instanceof TokenComputable) {
 					if (headNode.data instanceof TokenValue) {
 						Node<Token> parentFunctionNode = new Node<Token>(null, curToken);
 						headNode.insertParent(parentFunctionNode);
 						headNode = parentFunctionNode;
 					} else if (headNode.data == Token.BRACKET_LEFT) {
 						Node<Token> childFunctionNode = new Node<Token>(null, curToken);
 						headNode.addChild(childFunctionNode);
 						headNode = childFunctionNode;
 					} else if (headNode.data == Token.BRACKET_COMPLETE) {
 						headNode.data = curToken;
 					} else if (headNode.data instanceof TokenComputable) {
 						TokenComputable headFunction = (TokenComputable)headNode.data;
 						TokenComputable curFunction = (TokenComputable) curToken;
 						if (headFunction.precedence < curFunction.precedence) {
 							Node<Token> childFunctionNode = new Node<Token>(null, curFunction);
 							if (curFunction.isInfix()) {
 								Node<Token> childValueNode = headNode.children.get(headNode.children.size() - 1);
 								headNode.children.remove(childValueNode);
 								childFunctionNode.addChild(childValueNode);
 							}
 							headNode.addChild(childFunctionNode);
 							headNode = childFunctionNode;
 						} else {
 							Node<Token> parentFunctionNode = new Node<Token>(null, curFunction);
 							headNode.insertParent(parentFunctionNode);
 							headNode = parentFunctionNode;
 						}
 					} else {
 						throw new CalculationException("Unexpected function token: " + curToken.notation);
 					}
 				} else if (curToken == Token.COMMA) {
 					if (headNode.parent != null && headNode.parent.data == Token.BRACKET_LEFT) {
 						headNode = headNode.parent;
 					} else {
 						throw new CalculationException("Unexpected comma token");
 					}
 				} else {
 					throw new CalculationException("Unexpected token: " + curToken.notation);
 				}
 			}
 		}
 		// Find root:
 		Tree<Token> tree = new Tree<Token>(null);
 		while (headNode.parent != null) {
 			headNode = headNode.parent;
 		}
 		tree.root = headNode;
 		return tree;
 	}
 		
 	protected static double calculateRPN(List<Token> rpnSequence) throws CalculationException {
 		Stack<Token> calcStack = new Stack<>();
 		for (Token node : rpnSequence) {
 			if (node instanceof TokenValue) {
 				calcStack.push(node);
 			} else if (node instanceof TokenComputable) {
 				TokenComputable function = (TokenComputable) node;
 				if (calcStack.size() < function.args) {
 					throw new CalculationException("Wrong number of arguments " +
 							calcStack.size() + " to function " + function.notation);
 				}
 				
 				// Read params for the function from the stack
 				double[] params = new double[function.args];
 				for (int i = function.args-1; i >= 0; i--) {
 					Token paramNode = calcStack.pop();
 					if (!(paramNode instanceof TokenValue)) {
 						throw new CalculationException("Parameter node is not a value");
 					}
 					params[i] = ((TokenValue)paramNode).value;
 				}
 				
 				// Push back into the stack the result of the function call
 				double result = function.compute(params);
 				calcStack.push(new TokenValue(result));
 			} // Disregard any tokens other than Value or Computable
 		}
 		if (calcStack.size() != 1) {
 			throw new CalculationException("Stack size is " + calcStack.size());
 		}
 		Token node = calcStack.pop();
 		if (!(node instanceof TokenValue)) {
 			throw new CalculationException("Last node is not a value");
 		}
 		return ((TokenValue)node).value;
 	}
 }
