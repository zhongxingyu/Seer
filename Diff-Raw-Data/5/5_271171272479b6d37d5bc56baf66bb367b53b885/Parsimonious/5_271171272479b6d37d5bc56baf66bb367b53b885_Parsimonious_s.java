 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.Deque;
 import java.util.ArrayDeque;
 
 /**
  * Parsimonious - a mathematical parser.
 * Known bugs: Will only process one line with each run. Does not generate parse tree.
  * Should use Exceptions rather than exiting.
  * @author Wilfred Hughes
  */
 
 // invalid syntax suggestions: "1.0.1" "sin" "css" "coos" "3**3" "2 co" "sin!" "cos !"
 // test cases: "3!" "cos 3!" "~1-~1" "3.3!" "1+2*3"
 
 public class Parsimonious
 {	public static void main(String[] args) throws java.io.IOException //declaring exception because code is cleaner and I think it's never thrown
	{	System.out.printf("Operators accepted: cos ! * + - (descending priority, cos in degrees, ! rounds to integers)%n");
 		System.out.printf("Signed floating point numbers are accepted in the forms 0, 0.0 or .0 (negative numbers must use ~) %n");
 		System.out.printf("Type a mathematical expression and hit enter. All whitespace will be ignored.%n");
 
 		InputStreamReader input = new InputStreamReader(System.in);
 		String inputString = "";
 		int a = input.read();
 		//put input in string
 		while (a != -1 && a != 10) //-1 is end of stream, 10 is character return
 		{	inputString = inputString + (char)a;
 			a = input.read();
 		}
 
 		String strippedInput = Lexer.removeWhitespace(inputString);
 		//System.out.printf("Stripping whitespace: %s%n",strippedInput);
 
 		//System.out.printf("Separating String into tokens and validating operators...");
 		String[] tokenArray = Lexer.separateTokens(strippedInput);
 		//System.out.printf("OK%n");
 		//System.out.printf("Current String array: "); printArray(tokenArray);
 
 		//System.out.printf("Validating numbers and tokenising...");
 		Token[] mathsArray = Lexer.tokenise(tokenArray);
 		//System.out.printf("OK%n");
 		//System.out.printf("Current Token array: "); printArray(mathsArray);
 
 		//System.out.printf("Generating parse tree...");
 		Node parseTree = Parser.generateTree(mathsArray);
 		//System.out.printf("OK%n");
 
 		//System.out.printf("Evaluating tree...");
 		float result = Parser.evaluateTree(parseTree);
 		//System.out.printf("OK%n");
 
 		//System.out.printf("Tree has %d node(s).%n",countNodes(parseTree));
 
 		System.out.printf("Result: %f%n",result);
 	}
 
 	private static void printArray(Object[] input) //accepts strings or tokens
 	{	for (int i=0; i<input.length; i++)
 		{	System.out.printf("%s ",input[i]);
 		}
 		System.out.printf("%n");
 	}
 
 	private static int countNodes(Node root)
 	{	LinkedList<Node> children = root.getChildren();
 		if (children.size() == 0)
 		{	return 1;
 		}
 		else
 		{	int total = 1;
 			for (int i=0; i<children.size(); i++)
 			{	total += countNodes(children.get(i));
 			}
 			return total;
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
 {	public static String removeWhitespace(String input)
 	{	String returnme = "";
 		for (int i=0; i<input.length(); i++)
 		{	if((int)input.charAt(i) != 9 && (int)input.charAt(i) != 32) //not tab or space
 			{	returnme = returnme + input.charAt(i);
 			}
 		}
 		return returnme;
 	}
 
 	public static String[] separateTokens(String input)
 	{	String[] returnme = new String[0];
 		for (int i=0; i<input.length(); i++)
 		{	if (isNumeric(input.charAt(i)))
 			{	if (i != 0 && isNumeric(returnme[returnme.length-1].charAt(0))) //last token exists, is numeric, so extend with this character
 				{	returnme[returnme.length-1] = returnme[returnme.length-1] + input.charAt(i);
 				}
 				else
 				{	returnme = extendArray(returnme,input.charAt(i)+"");
 				}
 			}
 			else
 			{	try
 				{	if (Lexer.isValidOperator(input.charAt(i)+""))
 					{	returnme = extendArray(returnme,input.charAt(i)+""); //single character operator
 					}
 					else if (Lexer.isValidOperator("" + input.charAt(i) + input.charAt(i+1) + input.charAt(i+2)))
 					{	returnme = extendArray(returnme,"" + input.charAt(i) + input.charAt(i+1) + input.charAt(i+2)); //3 character operator
 						i += 2;
 					}
 					else
 					{	System.out.printf("Neither '%s' nor '%s' are valid operators.%n",input.charAt(i),""+input.charAt(i)+input.charAt(i+1)+input.charAt(i+2));
 						System.exit(1);
 					}
 				}
 				catch (StringIndexOutOfBoundsException e)
 				{	System.out.printf("Invalid operator length.%n");
 					System.exit(1);
 				}
 			}
 		}
 		return returnme;
 	}
 
 	public static Token[] tokenise(String[] tokenStrings)
 	{	Token[] returnme = new Token[tokenStrings.length];
 		for (int i=0; i<tokenStrings.length; i++)
 		{	if (isNumeric(tokenStrings[i].charAt(0)))
 			{	try
 				{	//making sure to replace ~ here so we get negative floats
 					returnme[i] = new Token(Float.parseFloat(tokenStrings[i].replace('~','-')));
 				}
 				catch (NumberFormatException e)
 				{	System.out.printf("Not a recognised number: %s%n",e.getMessage());
 					System.exit(1);
 				}
 			}
 			else
 			{	returnme[i] = new Token(tokenStrings[i]);	
 			}
 		}
 		return returnme;
 	}
 
 	//inefficient but quick and dirty
 	private static String[] extendArray(String[] input, String element)
 	{	String[] returnme = new String[input.length+1];
 		for (int i=0; i<returnme.length-1; i++)
 		{	returnme[i] = input[i];
 		}
 		returnme[returnme.length-1] = element;
 		return returnme;
 	}
 
 	private static boolean isNumeric(char input)
 	{	if (input == '0' || input == '1' || input == '2' || input == '3' || 
 		    input == '4' || input == '5' || input == '6' || input == '7' || 
 		    input == '8' || input == '9' || input == '.' || input == '~')
 		{	return true;
 		}
 		else
 		{	return false;
 		}
 	}
 
 	private static boolean isValidOperator(String input)
 	{	if (input.equals("+") || input.equals("-") || input.equals("*") || input.equals("!") || input.equals("cos"))
 		{	return true;
 		}
 		else
 		{	return false;
 		}
 	}
 }
 
 class Parser
 {	private static void printStack(Deque<Integer> s)
 	{	Object[] printMe = s.toArray();
 		for (int i=0; i<printMe.length; i++)
 		{	System.out.printf("%s ", printMe[i]);
 		}
 	}
 
 	public static Node generateTree(Token[] originalInput)
 	{	//a linkedList is easier to work with IMO, although it's refusing to convert directly from the array
 		LinkedList<Token> input = new LinkedList<Token>();
 		for (int i=0; i<originalInput.length; i++)
 		{	input.add(originalInput[i]);
 		}
 		input.addLast(new Token("end")); //eof marker
 
 		//supposed to use a stack--Java prefers using a deque
 		//we also use two stacks as we want to hold tokens and states, so we can output tokens on reductions
 		Deque<Token> symbolStack = new ArrayDeque<Token>();
 		Deque<Integer> stateStack = new ArrayDeque<Integer>();
 		stateStack.push(0); //start state
 
 		//we also need a stack to hold the pieces of our parse tree
 		Deque<Node> outputStack = new ArrayDeque<Node>();
 
 		//int i = 1;
 
 		while (true)
 		{	Token nextToken = input.peekFirst();
 
 			//System.out.printf("%n%nLoop %d.%nnextToken is: %s and reversed current state is ",i,nextToken.toString());
 			//printStack(stateStack); System.out.printf("%n");
 
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
 				int reductionRule = Table.getReduction(nextToken, stateStack.peek());
 				//System.out.printf("Reducing according to rule %d.%n",reductionRule);
 				switch (reductionRule)
 				{	case 1:
 						// A -> A - B
 						stateStack.pop();
 						stateStack.pop();
 						stateStack.pop();
 						//output - to tree
 						Node child11 = outputStack.pop();
 						Node child12 = outputStack.pop();
 						LinkedList<Node> children1 = new LinkedList<Node>();
 						children1.add(child11); children1.add(child12);
 						Node newNode1 = new Node(new Token("-"),children1);
 						outputStack.push(newNode1);
 						//System.out.printf("Popped %s and %s, pushed %s to outputStack.%n",child11,child12,newNode1);
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
 						//System.out.printf("Popped %s and %s, pushed %s to outputStack.%n",child21,child22,newNode2);
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
 						//System.out.printf("Popped %s and %s, pushed %s to outputStack.%n",child41,child42,newNode4);
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
 						//System.out.printf("Pushing node %s to outputStack.%n",newNode8);
 						break;
 					default:
 						System.out.printf("Error, could not find correct reduction.%n");
 						break;
 				}
 				//possibly output something into tree stack, or assemble tree
 
 				//ok, so a goto always follows a reduction
 				//we examine the reduction rule to see what symbol we have reduced to,
 				//so we can inspect the goto table and update the state
 				if (reductionRule == 1 || reductionRule == 2 || reductionRule == 3)
 				{	//we have reduced to A
 					//System.out.printf("Our getGotoState is %d%n",Table.getGotoState(0,stateStack.peek()));
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
 		//System.out.printf("At the end of this loop, nextToken: %s and reversed state stack: ",nextToken.toString());
 		//printStack(stateStack); System.out.printf("%n");
 		//i++;
 		}
 
 		Node parseTree = outputStack.pop();
 		return parseTree;
 	}
 
 	public static float evaluateTree(Node node)
 	{	if (node.getChildren().size() == 0) //is leaf
 		{	return node.getToken().getNumber();
 		}
 		else
 		{	//would use switch statement but its too messy with variable # of children
 			LinkedList<Node> children = node.getChildren();
 			if (!node.getToken().isOperator()) //entire tree is just a number
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
 			else
 			{	System.out.println("Operator: \"" + node.getToken().getOperator() + "\" not recognised.");
 				System.exit(1);
 				//we won't execute this but we need a return statement... not stylish but easy
 				return 0;
 			}
 		}
 	}
 
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
 {	private LinkedList<Node> children; //children
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
 	private static final int[][] stateAfterShift = {	{0,0,0,1 ,0,2,0},
 								{0,0,0,1 ,0,2,0},
 								{0,0,0,0 ,0,0,0},
 								{7,8,0,0 ,0,0,0},
 								{0,0,0,0 ,0,0,0},
 								{0,0,9,0,10,0,0},
 								{0,0,0,0 ,0,0,0},
 								{0,0,0,1 ,0,2,0},
 								{0,0,0,1 ,0,2,0},
 								{0,0,0,1 ,0,2,0},
 								{0,0,0,0 ,0,0,0},
 								{0,0,0,0 ,0,0,0},
 								{0,0,0,0 ,0,0,0},
 								{0,0,0,0 ,0,0,0} };
 
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
 	{	//dirty and hacky? you bet. array[y][x]
 		//System.out.printf("getAction received Token %s which is number %d and returned action %d.%n",t.toString(),tokenNumber(t),actionTable[state][tokenNumber(t)]);
 		return actionTable[state][tokenNumber(t)];
 	}
 	private static int tokenNumber(Token t)
 	{	int tokenSymbol = 0; //initialised to keep javac happy
 		if (!t.isOperator()) //token is num
 		{	tokenSymbol = 5;
 		}
 		else //token is an operator of some sort
 		{	if (t.getOperator().equals("-"))
 			{	tokenSymbol = 0;
 			}
 			else if (t.getOperator().equals("+"))
 			{	tokenSymbol = 1;
 			}
 			else if (t.getOperator().equals("*"))
 			{	tokenSymbol = 2;
 			}
 			else if (t.getOperator().equals("cos"))
 			{	tokenSymbol = 3;
 			}
 			else if (t.getOperator().equals("!"))
 			{	tokenSymbol = 4;
 			}
 			else if (t.getOperator().equals("end")) //eof marker, also hacky
 			{	tokenSymbol = 6;
 			}
 		}
 		return tokenSymbol;
 	}
 
 	public static int getStateAfterShift(Token t, int state)
 	{	int newState = stateAfterShift[state][tokenNumber(t)];
 		//System.out.printf("getStateAfterShift: Token was %s (or %d), state was %d. Resulting state was %d.%n",t.toString(),tokenNumber(t),state,newState);
 		return newState;
 	}
 
 	public static int getReduction(Token t, int state)
 	{	int reduction = reduceByProduction[state][tokenNumber(t)];
 		//System.out.printf("getReduction with token %s and state %d gives reduction %d.%n",t.toString(),state,reduction);
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
