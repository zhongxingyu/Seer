 package cm.util;
 
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public abstract class DiceBag {
 	/**
 	 * Random number generator to be used by the roller.
 	 */
 	private static Random numGen = new Random();
 	
 	/**
 	 * Roll 1 die of the given type.
 	 * @param dieType the die type (e.g., 4, 6, 20, etc.)
 	 * @return the roll result
 	 */
 	public static Integer roll(Integer dieType) {
 		if (dieType < 2) {
 			return 0;
 		}
 		
 		return numGen.nextInt(dieType) + 1;
 	}
 	
 	/**
 	 * Roll multiple dice of the given type.
 	 * @param dieNum the number of dice
 	 * @param dieType the type of die (e.g., 4, 6, 20)
 	 * @return the sum of the die rolls
 	 */
 	public static Integer roll(Integer dieNum, Integer dieType) {
 		Integer total = 0;
 		
 		if (dieNum < 1) {
 			return 0;
 		}
 		
 		for (int i = 0; i < dieNum; i++) {
 			total += roll(dieType);
 		}
 		
 		return total;
 	}
 	
 	private static Integer roll(Integer dieNum, Integer dieType, String desc) {
 		Integer total = 0;
 		Integer result;
 		
 		desc = dieNum + "d" + dieType + " (";
 		
 		if (dieNum < 1) {
 			return 0;
 		}
 		
 		for (int i = 0; i < dieNum; i++) {
 			result = roll(dieType);
 			total += result;
 			desc += result;
 			if (i != dieNum - 1) {
 				desc += ",";
 			}
 		}
 		
 		desc += ")";
 		
 		return total;
 	}
 
 	/**
 	 * Roll the given dice expression the requested number of times.
 	 * @param diceExpr the dice expression, e.g., 3d6+5
 	 * @param numRolls the number of times to roll the expression
 	 * @return the results in an enumerated list of rolls
 	 */
 	public static String roll(String diceExpr, int numRolls) {
 		String results = "";
 		for (int i = 1; i <= numRolls; i++) {
 			results += "Roll #" + i + ": " + roll(diceExpr) + "\n";
 		}
 		return results;
 	}
 
 	/**
 	 * Parse the given dice expression and roll it.
 	 * @param diceExpr the dice expression, e.g., 3d6+5
 	 * @return the results in expr = result format, e.g., 3d6+5 = 17
 	 */
 	public static String roll(String diceExpr) {
		Pattern p = Pattern.compile("(([0-9]+)d([0-9]+))? *[+] *([0-9]+)");
 		Matcher m = p.matcher(diceExpr);
		m.find();
 		String num = m.group(2);
 		String type = m.group(3);
 		String bonus = m.group(4);
 		
 		if (num == null || type == null) {
 			num = "1";
 			type = "20";
 		}
 		
 		if (bonus == null || bonus.isEmpty()) {
 			bonus = "0";
 		}
 		Integer dieNum = Integer.valueOf(num.trim());
 		Integer dieType = Integer.valueOf(type.trim());
 		Integer mod = Integer.valueOf(bonus.trim());
 		
		return diceExpr + " = " + (roll(dieNum, dieType) + mod);
 	}
 }
