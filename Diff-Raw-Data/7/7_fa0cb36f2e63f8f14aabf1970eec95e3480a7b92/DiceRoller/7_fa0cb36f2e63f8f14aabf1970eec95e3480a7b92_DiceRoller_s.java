 package org.sergut.diceroller;
 
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 
 import org.sergut.diceroller.adt.AdditionNode;
 import org.sergut.diceroller.adt.ConstantNode;
 import org.sergut.diceroller.adt.ContainerNode;
 import org.sergut.diceroller.adt.DieNode;
 import org.sergut.diceroller.adt.DieType;
 import org.sergut.diceroller.adt.Node;
 import org.sergut.diceroller.ui.DiceRollerFrame;
 
 public class DiceRoller {
 
     /**
      * A cache of already parsed expressions into dice trees. 
      * 
      * As the method {@link #rollDice(String)} is called several million times, having
      * a cache saves around 95% of computation time (informal calculation) for a litle
      * price in memory. Not bad. ;-)
      */
     private HashMap<String,ContainerNode> diceBagMap = new HashMap<String,ContainerNode>();  
     
     /**
      * Takes a string with roll20 syntax, parses it, 
      * rolls it, and returns the result.
      * 
      * For now, we can only parse a normal addition of regular die, exploding die, and constants.
      * 
      * @param diceDescription a string like "2d10", "1d6!+1d8!", or "2d8!+1"
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
 	// TODO: parse parallel rolls, e.g. b[1d6!, 1d8!] 
 	// ...
 	// parse addition
 	ContainerNode additionNode = new AdditionNode();
 	String[] tokens = diceDescription.split("\\+");
 	for (String s : tokens) {
 	    // parse dice and constants
 	    additionNode.addChild(parseAdditionOperands(s));
 	}
 	additionNode.clean();
 	addToCache(diceDescription, additionNode);
 	return additionNode;
     }
 
     private void addToCache(String diceDescription, ContainerNode additionNode) {
 	diceBagMap.put(diceDescription, additionNode);
     }
 
     /**
      * 
      * @param dice something like "5", "3d6", "2d6!" or "d20" 
      * @return
      */
     private static Node parseAdditionOperands(String diceExpression) {
 	String dice = regulariseDiceExpression(diceExpression);
	if (!diceExpression.contains("d")) {
	    if ("".equals(diceExpression)) {
 		return new ConstantNode(0);
 	    } else {
		return new ConstantNode(Integer.parseInt(diceExpression));
 	    }
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
 	return new DieNode(diceCount, sideCount, dieType);
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
 }
