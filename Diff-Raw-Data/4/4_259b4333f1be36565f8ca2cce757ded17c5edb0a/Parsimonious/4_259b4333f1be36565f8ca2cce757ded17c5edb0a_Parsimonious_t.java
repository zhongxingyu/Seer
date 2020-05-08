 /*  This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 // invalid syntax suggestions: "1.0.1" "sin" "css" "coos" "3**3" "2 co" "sin!" "cos !"
 // test cases: "3!" "cos 3!" "~1-~1" "3.3!" "1+2*3" "10-2"
 
 import java.io.Console;
 import java.util.LinkedList;
 import java.util.Stack;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 /**
  * Parsimonious: a mathematical lexer and parser.
  * Known bugs: Should use Exceptions rather than exiting.
  * Does not quit.
  * @author Wilfred Hughes
  */
 
 public class Parsimonious
 {	public static void main(String[] args) throws java.io.IOException //declaring exception because code is cleaner and I think it's never thrown
 	{	Console console = System.console();
 		console.format("Operators accepted: cos ! * + - (descending priority, cos in radians, ! rounds to integers)\n");
 		console.format("Signed floating point numbers are accepted in the forms 0 or 0.0 (negative numbers must use ~)\n");
 		while (true)
 		{	console.format("Type a mathematical expression and hit enter. All whitespace will be ignored.%n");
 
 			String inputString = console.readLine();
 			//lex according to regex
 			LinkedList<Token> tokens = Lexer.lex(inputString);
 
 			//generate parse tree using LR(0) algorithm. This is the exciting bit.
 			Node parseTree = Parser.generateTree(tokens);
 
 			//bottom up traversal of tree to calculate value
 			float result = Parser.evaluateTree(parseTree);
 			console.format("%f\n",result);
 		}
 	}
 }
 
 class Token
 {	private String operatorName;
 	private boolean isOperator;
 	private float number;
 
 	public boolean isOperator()
 	{	return isOperator;
 	}
 
 	public Token(String value)
 	{	operatorName = value;
 		isOperator = true;
 	}
 
 	public Token(float value)
 	{	number = value;
 		isOperator = false;
 	}
 
 	public String toString()
 	{	if (isOperator)
 		{	return operatorName;
 		}
 		else
 		{	return "" + number;
 		}
 	}
 	public float getNumber()
 	{	return number;
 	}
 	public String getOperator()
 	{	return operatorName;
 	}
 }
 
 class Lexer
 {	public static LinkedList<Token> lex(String s)
 	{	//match any of: - + * cos ! or a number
 		Pattern validTokens = Pattern.compile("-|\\+|\\*|cos|!|~?[0-9]+(\\.[0-9]+)?");
 		Matcher matcher = validTokens.matcher(s);
 		LinkedList<String> tokenStrings = new LinkedList<String>();
 		while(matcher.find())
 		{	tokenStrings.add(matcher.group());
 		}
 		return tokenise(tokenStrings);
 	}
 
 
 	private static LinkedList<Token> tokenise(LinkedList<String> tokenStrings)
 	{	LinkedList<Token> returnme = new LinkedList<Token>();
 		for (String s : tokenStrings)
 		{	if (s.matches("~?[0-9].*")) //is a number
 			{	try
 				{	//making sure to replace ~ here so we get negative floats
 					returnme.add(new Token(Float.parseFloat(s.replace('~','-'))));
 				}
 				catch (NumberFormatException e)
 				{	System.out.printf("Not a recognised number: %s%n",e.getMessage());
 					System.exit(1);
 				}
 			}
 			else
 			{	returnme.add(new Token(s));
 			}
 		}
 		returnme.add(new Token("end"));
 		return returnme;
 	}
 }
 
 class Parser
 {	public static Node generateTree(LinkedList<Token> input)
 	{	//we also use two stacks as we want to hold tokens and states, so we can output tokens on reductions
 		Stack<Token> symbolStack = new Stack<Token>();
 		Stack<Integer> stateStack = new Stack<Integer>();
 		stateStack.push(0); //start state
 
 		//we also need a stack to hold the pieces of our parse tree
 		Stack<Node> outputStack = new Stack<Node>();
 
 		while (true)
 		{	Token nextToken = input.peek();
 
 			if (Table.getAction(nextToken, stateStack.peek()) == Table.ERROR)
 			{	System.out.printf("Invalid syntax found.%n");
 				System.exit(1);
 			}
 			else if (Table.getAction(nextToken, stateStack.peek()) == Table.SHIFT)
 			{	symbolStack.push(input.pop());
 				stateStack.push(Table.getStateAfterShift(nextToken,stateStack.peek()));
 			}
 			else if (Table.getAction(nextToken, stateStack.peek()) == Table.REDUCE)
 			{	//work out which production and act accordingly, popping the appropriate number of states
 				//if appropriate, update the outputStack
 				int reductionRule = Table.getReduction(nextToken, stateStack.peek());
 				switch (reductionRule)
 				{	case 1:
 						// A -> A - B
 						stateStack.pop();
 						stateStack.pop();
 						stateStack.pop();
 						//output - to tree
						//pop() reverses order of the arguments, so we reorder
 						Node child12 = outputStack.pop();
						Node child11 = outputStack.pop();
 						LinkedList<Node> children1 = new LinkedList<Node>();
 						children1.add(child11); children1.add(child12);
 						Node newNode1 = new Node(new Token("-"),children1);
 						outputStack.push(newNode1);
 						break;
 					case 2:
 						// A -> A + B
 						stateStack.pop();
 						stateStack.pop();
 						stateStack.pop();
 						//output + to tree
 						Node child21 = outputStack.pop();
 						Node child22 = outputStack.pop();
 						LinkedList<Node> children2 = new LinkedList<Node>();
 						children2.add(child21); children2.add(child22);
 						Node newNode2 = new Node(new Token("+"),children2);
 						outputStack.push(newNode2);
 						break;
 					case 3:
 						// A -> B
 						stateStack.pop();
 						break;
 					case 4:
 						// B -> C * B
 						stateStack.pop();
 						stateStack.pop();
 						stateStack.pop();
 						//output * to tree
 						Node child41 = outputStack.pop();
 						Node child42 = outputStack.pop();
 						LinkedList<Node> children4 = new LinkedList<Node>();
 						children4.add(child41); children4.add(child42);
 						Node newNode4 = new Node(new Token("*"),children4);
 						outputStack.push(newNode4);
 						break;
 					case 5:
 						// B -> C
 						stateStack.pop();
 						break;
 					case 6:
 						// C -> cos C
 						stateStack.pop();
 						stateStack.pop();
 						//output cos to tree
 						Node child6 = outputStack.pop();
 						Node newNode6 = new Node(new Token("cos"),child6);
 						outputStack.push(newNode6);
 						break;
 					case 7:
 						// C -> C!
 						stateStack.pop();
 						stateStack.pop();
 						//output ! to tree
 						Node child7 = outputStack.pop();
 						Node newNode7 = new Node(new Token("!"),child7);
 						outputStack.push(newNode7);
 						break;
 					case 8:
 						// C -> num
 						stateStack.pop();
 						//output this number to the tree
 						Node newNode8 = new Node(symbolStack.pop());
 						outputStack.push(newNode8);
 						break;
 					default:
 						System.out.printf("Error, could not find correct reduction.%n");
 						break;
 				}
 
 				//ok, so a goto always follows a reduction
 				//we examine the reduction rule to see what symbol we have reduced to,
 				//so we can inspect the goto table and update the state
 				if (reductionRule == 1 || reductionRule == 2 || reductionRule == 3)
 				{	//we have reduced to A
 					stateStack.push(Table.getGotoState(0,stateStack.peek()));
 				}
 				else if (reductionRule == 4 || reductionRule == 5)
 				{	//we have reduced to B
 					stateStack.push(Table.getGotoState(1,stateStack.peek()));
 				}
 				else if (reductionRule == 6 || reductionRule == 7 || reductionRule == 8)
 				{	//we have reduced to C
 					stateStack.push(Table.getGotoState(2,stateStack.peek()));
 				}
 			}
 			else if (Table.getAction(nextToken, stateStack.peek()) == Table.ACCEPT)
 			{	break; //we're done! woohoo!
 			}
 		}
 
 		//our final assembled tree is at the head of the outputStack
 		Node parseTree = outputStack.pop();
 		return parseTree;
 	}
 
 	public static float evaluateTree(Node node)
 	{	if (node.getChildren().size() == 0) //is leaf
 		{	return node.getToken().getNumber();
 		}
 		else
 		{	LinkedList<Node> children = node.getChildren();
 			if (!node.getToken().isOperator()) //node is a number so no children
 			{	return node.getToken().getNumber();
 			}
 			//can't use switch with string - would have been nice here :-(
 			if (node.getToken().getOperator().equals("cos"))
 			{	return (float)Math.cos(evaluateTree(children.get(0)));
 			}
 			else if (node.getToken().getOperator().equals("!"))
 			{	return factorial(Math.round(evaluateTree(children.get(0))),1); //round to int for factorial
 			}
 			else if (node.getToken().getOperator().equals("*"))
 			{	return evaluateTree(children.get(0))*evaluateTree(children.get(1));
 			}
 			else if (node.getToken().getOperator().equals("+"))
 			{	return evaluateTree(children.get(0))+evaluateTree(children.get(1));
 			}
 			else if (node.getToken().getOperator().equals("-"))
 			{	return evaluateTree(children.get(0))-evaluateTree(children.get(1));
 			}
 			else
 			{	System.out.println("Operator: \"" + node.getToken().getOperator() + "\" not recognised.");
 				System.exit(1);
 				//we won't execute this but we need a return statement... not stylish but easy
 				return 0;
 			}
 		}
 	}
 
 	//factorial implementation that is iterative, just in case we face big numbers
 	//would java have optimised it anyway?
 	private static int factorial(int x, int accum)
 	{	if (x < 0)
 		{	System.out.printf("Can't take factorial of: %d%n",x);
 			System.exit(1);
 			return 1;
 		}
 		else if (x == 0 || x == 1)
 		{	return accum;
 		}
 		else
 		{	return factorial(x-1,accum*x);
 		}
 	}
 }
 
 class Node //simple immutable tree
 {	private LinkedList<Node> children;
 	private Token value;
 
 	public LinkedList<Node> getChildren()
 	{	return children;
 	}
 	public Token getToken()
 	{	return value;
 	}
 	public Node(Token t)
 	{	value = t;
 		children = new LinkedList<Node>();
 	}
 	public Node(Token t, Node child)
 	{	value = t;
 		children = new LinkedList<Node>();
 		children.add(child);
 	}
 	public Node(Token t, LinkedList<Node> kids)
 	{	value = t;
 		children = kids;
 	}
 	public void update(Token t, LinkedList<Node> kids)
 	{	value = t;
 		children = kids;
 	}
 
 	public String toString()
 	{	return value.toString();
 	}
 }
 
 class Table
 {	public static final int ERROR = 0;
 	public static final int SHIFT = 1;
 	public static final int REDUCE = 2;
 	public static final int ACCEPT = 3;
 
 	//where to go to after a reduction
 	private static final int[][] gotoTable = {	{3,4, 5},
 							{0,0, 6},
 							{0,0, 0},
 							{0,0, 0},
 							{0,0, 0},
 							{0,0, 0},
 							{0,0, 0},
 							{0,11,5},
 							{0,12,5},
 							{0,13,5},
 							{0,0, 0},
 							{0,0, 0},
 							{0,0, 0},
 							{0,0, 0} };
 
 	//0 error, 1 shift, 2 reduce, 3 accept
 	private static final int[][] actionTable = {	{0,0,0,1,0,1,0},
 							{0,0,0,1,0,1,0},
 							{2,2,2,0,2,0,2},
 							{1,1,0,0,0,0,3},
 							{2,2,2,0,2,0,2},
 							{2,2,1,0,1,0,2},
 							{2,2,2,0,2,0,2},
 							{0,0,0,1,0,1,0},
 							{0,0,0,1,0,1,0},
 							{0,0,0,1,0,1,0},
 							{2,2,2,0,2,0,2},
 							{2,2,2,0,2,0,2},
 							{2,2,2,0,2,0,2},
 							{2,2,2,0,2,0,2} };
 
 	//where to go after shift
 	private static final int[][] stateAfterShift = {	{0,0,0,1,0, 2,0},
 								{0,0,0,1,0, 2,0},
 								{0,0,0,0,0, 0,0},
 								{7,8,0,0,0, 0,0},
 								{0,0,0,0,0, 0,0},
 								{0,0,9,0,10,0,0},
 								{0,0,0,0,0, 0,0},
 								{0,0,0,1,0, 2,0},
 								{0,0,0,1,0, 2,0},
 								{0,0,0,1,0, 2,0},
 								{0,0,0,0,0, 0,0},
 								{0,0,0,0,0, 0,0},
 								{0,0,0,0,0, 0,0},
 								{0,0,0,0,0, 0,0} };
 
 	//once we know we're doing a reduction, which one we're doing
 	private static final int[][] reduceByProduction = {	{0,0,0,0,0,0,0},
 								{0,0,0,0,0,0,0},
 								{8,8,8,0,8,0,8},
 								{7,8,0,0,0,0,0},
 								{3,3,3,0,3,0,3},
 								{5,5,0,0,0,0,5},
 								{6,6,6,0,6,0,6},
 								{0,0,0,0,0,0,0},
 								{0,0,0,0,0,0,0},
 								{0,0,0,0,0,0,0},
 								{7,7,7,0,7,0,7},
 								{1,1,1,0,1,0,1},
 								{2,2,2,0,2,0,2},
 								{4,4,4,0,4,0,4} };
 
 	public static int getAction(Token t, int state)
 	{	//array[y][x] obviously(!)
 		return actionTable[state][tokenNumber(t)];
 	}
 	private static int tokenNumber(Token t)
 	{	int symbolNumber = 0; //initialised to keep javac happy
 		if (!t.isOperator()) //token is num
 		{	symbolNumber = 5;
 		}
 		else //token is an operator of some sort
 		{	if (t.getOperator().equals("-"))
 			{	symbolNumber = 0;
 			}
 			else if (t.getOperator().equals("+"))
 			{	symbolNumber = 1;
 			}
 			else if (t.getOperator().equals("*"))
 			{	symbolNumber = 2;
 			}
 			else if (t.getOperator().equals("cos"))
 			{	symbolNumber = 3;
 			}
 			else if (t.getOperator().equals("!"))
 			{	symbolNumber = 4;
 			}
 			else if (t.getOperator().equals("end")) //eof marker, slightly hacky
 			{	symbolNumber = 6;
 			}
 		}
 		return symbolNumber;
 	}
 
 	public static int getStateAfterShift(Token t, int state)
 	{	int newState = stateAfterShift[state][tokenNumber(t)];
 		return newState;
 	}
 
 	public static int getReduction(Token t, int state)
 	{	int reduction = reduceByProduction[state][tokenNumber(t)];
 		if (reduction == 0)
 		{	System.out.printf("Something went wrong. Erroneous reduction.%n");
 		}
 		return reduction;
 	}
 
 	public static int getGotoState(int symbol, int state)
 	{	int newState = gotoTable[state][symbol];
 		if (newState == 0)
 		{	System.out.printf("Invalid goto requested.%n");
 		}
 		return newState;
 	}
 }
 
 /*
 augmented grammar, honouring precedence and associativity:
 A' -> A
 A -> A - B
 A -> A + B
 A -> B
 B -> C * B
 B -> C
 C -> cos C
 C -> C!
 C -> num
 
 nonterminals: A' A B C
 terminals: - + * cos ! num
 */
