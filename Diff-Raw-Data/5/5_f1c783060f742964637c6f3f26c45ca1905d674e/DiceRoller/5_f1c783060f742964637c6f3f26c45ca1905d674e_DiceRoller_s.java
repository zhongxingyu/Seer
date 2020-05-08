 package org.sergut.diceroller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.JFrame;
 
 import org.sergut.diceroller.adt.AdditionNode;
 import org.sergut.diceroller.adt.ComparisonNode;
 import org.sergut.diceroller.adt.ConstantNode;
 import org.sergut.diceroller.adt.ContainerNode;
 import org.sergut.diceroller.adt.DieNode;
 import org.sergut.diceroller.adt.DieType;
 import org.sergut.diceroller.adt.Node;
 import org.sergut.diceroller.adt.Sign;
 import org.sergut.diceroller.adt.ComparisonNode.Choosing;
 import org.sergut.diceroller.ui.DiceRollerFrame;
 
 public class DiceRoller {
 
/* General expression
  * G := b[L]
  * G := w[L]
  * G := E
  * 
  * List of expressions
  * L := E,L
  * L := E
  * 
  * Expression
  * E := E+E
  * E := E-E
  * E := D
  * E := n
  * 
  * Dice Expression
  * D := ndnx
  * 
  * Numeral
  * n := num
  * 
  * Descriptor
  * x := !
  * x := \    
  */
     /**
      * A cache of already parsed expressions into dice trees. 
      * 
      * As the method {@link #rollDice(String)} is called several million times, having
      * a cache saves around 95% of computation time (informal calculation) for a litle
      * price in memory. Not bad. ;-)
      */
     private final HashMap<String,ContainerNode> diceBagMap = new HashMap<String,ContainerNode>();  
     
     /**
      * Takes a string with roll20 syntax, parses it, 
      * rolls it, and returns the result.
      * 
      * param diceDescription a string like "2d10", "1d6!+1d8!", or "2d8!+1"
      * 
      * @return the result of the roll
      * 
      * @throws IllegalArgumentException if the argument is not a valid string
      */
     /*
      * This method (with the adt package) is the main reusable part of this class/project.
      */
     public int rollDice(String diceDescription) throws IllegalArgumentException {
 	ContainerNode diceTree = diceBagMap.get(diceDescription);
 	if (diceTree == null)
 	    diceTree = createNewDiceTree(diceDescription);
 	return diceTree.getResult();
     }
     
     private ContainerNode createNewDiceTree(String diceDescription) {
 	try {
 	    checkCommonErrors(diceDescription);
 	    // parse parallel rolls, e.g. b[1d6!, 1d8!] 
 	    Choosing bestOrWorst = getBestOrWorst(diceDescription);
 	    ContainerNode comparisonNode = new ComparisonNode(bestOrWorst);
 	    List<String> comparisonOperands = getComparisonOperands(diceDescription);
 	    for (String s1 : comparisonOperands) {
 		// parse addition
 		ContainerNode additionNode = new AdditionNode();
 		List<String> additionOperands = getAdditionOperands(s1);
 		for (String s2 : additionOperands) {
 		    // parse dice and constants
 			additionNode.addChild(parseDice(s2));
 		}
 		additionNode.clean();
 		comparisonNode.addChild(additionNode);
 	    }
 	    comparisonNode.clean();
 	    addToCache(diceDescription, comparisonNode);
 	    return comparisonNode;
 	} catch (RuntimeException e) {
 	    throw new IllegalDiceExpressionException(diceDescription, e);
 	}
     }
 
     private Choosing getBestOrWorst(String s) {
 	if (!s.contains("[") && !s.contains("]"))
 	    return Choosing.BEST;
 	else if (!s.contains("[") || !s.contains("]"))
 	    throw new IllegalDiceExpressionException("Illegal expression: " + s);
 
 	if (s.startsWith("b"))
 	    return Choosing.BEST;
 	else if (s.startsWith("w"))
 	    return Choosing.BEST;
 	else if (Character.isLetter(s.charAt(0)))
 	    throw new IllegalDiceExpressionException("Illegal expression: " + s);
 	else
 	    return Choosing.BEST;
     }
 
     /** 
      * Gets something like b[xxx+yyy-zzz, ttt] and returns xxx+yyy-zzz, ttt  
      */
     private List<String> getComparisonOperands(final String s) {
 	List<String> result = new ArrayList<String>();
 	if (!s.contains("[") && !s.contains("]")) {
 	    result.add(s);
 	    return result;
 	} else if (!s.contains("[") || !s.contains("]")) {
 	    throw new IllegalDiceExpressionException("Illegal expression (brackets): " + s);
 	} else if (!s.startsWith("b") && !s.startsWith("w")) {
 	    System.out.println(s.charAt(0));
 	    throw new IllegalDiceExpressionException("Illegal expression (initial letter): " + s);
 	}
 
 	// cleanString has the initial letter and brackets removed, 
 	// leaving just the comma-separated arguments
 	String cleanString = s.substring(2, s.length() - 1);
 	String currentToken = "";
 	for (int i = 0; i < cleanString.length(); i++) {
 	    char c = cleanString.charAt(i);
 	    if (c == ',') {
 		result.add(currentToken);
 		currentToken = "";
 	    } else {
 		currentToken += c;
 	    }
 	}
 	result.add(currentToken);
 	return result;
     }
 
     /** 
      * Gets something like xxx+yyy-zzz and returns xxx,yyy,-zzz
      */
     private List<String> getAdditionOperands(String s) {
 	List<String> result = new ArrayList<String>();
 	String currentToken = "";
 	for (int i = 0; i < s.length(); i++) {
 	    char c = s.charAt(i);
 	    if (c == '+') {
 		result.add(currentToken);
 		currentToken = "";
 	    } else if (c == '-') {
 		result.add(currentToken);
 		currentToken = "-";		
 	    } else {
 		currentToken += c;
 	    }
 	}
 	result.add(currentToken);
 	return result;
     }
 
     private void addToCache(String diceDescription, ContainerNode additionNode) {
 	diceBagMap.put(diceDescription, additionNode);
     }
 
     /**
      * 
      * @param dice something like "5", "3d6", "-2d6!" or "d20" 
      * @return
      */
     private static Node parseDice(String diceExpression) {
 	String dice = regulariseDiceExpression(diceExpression);
 	if (!dice.contains("d")) {
 	    if ("".equals(dice)) {
 		return new ConstantNode(0);
 	    } else {
 		return new ConstantNode(Integer.parseInt(dice));
 	    }
 	}
 	Sign sign = Sign.PLUS;
 	if (dice.charAt(0) == '-') {
 	    sign = Sign.MINUS;
 	    dice = dice.substring(1);
 	}
 	DieType dieType = DieType.REGULAR;
 	if (dice.endsWith("!")) {
 	    dice = dice.substring(0, dice.length()-1);
 	    dieType = DieType.EXPLODING;
 	}
 	if (dice.endsWith("d")) 
 	    throw new IllegalArgumentException("Wrong dice expression (no side count): " + diceExpression);
 	String[] tokens = dice.split("d");
 	if (tokens.length != 2)
 	    throw new IllegalArgumentException("Wrong dice expression: " + diceExpression);
 	int diceCount = Integer.parseInt(tokens[0]);
 	int sideCount = Integer.parseInt(tokens[1]);
 	return new DieNode(diceCount, sideCount, dieType, sign);
     }
 
     /**
      * Gets something like "D20" and returns "1d20".
      */
     private static String regulariseDiceExpression(String s) {
 	if (s.length() == 0)
 	    return "0";
 	
 	String result = new String(s);
 	result = result.replace('D', 'd');
 	if (result.charAt(0) == 'd') 
 	    result = "1" + result;
 	return result;
     }
     
     /**
      * Gets two numbers and returns a (rounded) integer percentage.
      * 
      * Examples: 
      *   (123, 345)    ->  36
      *   (908, 67765)  ->   1
      *   (34,35)       ->  97
      *   (110,111)     ->  99
      *   (11110,11111) -> 100
      * 
      * @param n numerator
      * @param d denominator
      * @return a percentage
      */
     public static int getSimpleRate(int n, int d) {
 	if (n > d)
 	    throw new IllegalArgumentException("Numerator bigger than denominator: " + n + ">" + d);
 	int result = 1000 * n/ d;
 	result = (result + 5) / 10;
 	return result;
     }
 
     public static void main(String args[]) {
 	JFrame mainFrame = new DiceRollerFrame();
 	mainFrame.setVisible(true);
     }
 
     /*
      * Convenience method that checks for several heuristics
      */
     private void checkCommonErrors(String diceDescription) {
 	if (diceDescription.contains("++") ||
 	    diceDescription.contains("--") ||
 	    diceDescription.endsWith("+")  ||
 	    diceDescription.endsWith("-")  )
 	    	throw new IllegalArgumentException("Illegal expression (operands): " + diceDescription);
 	int leftBracketCount = countChar(diceDescription, '[');
 	int rightBracketCount = countChar(diceDescription, ']');
 	if ((leftBracketCount != rightBracketCount) ||
 	    leftBracketCount > 1 ||
 	    rightBracketCount > 1)
 	    	throw new IllegalArgumentException("Illegal expression (brackets): " + diceDescription);    
     }
 
     private int countChar(String s, char c) {
 	int result = 0;
 	for (int i = 0; i < s.length(); i++) {
 	    if (s.charAt(i) == c)
 		result++;
 	}
 	return result;
     }
 }
 
