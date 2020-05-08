 package emcshop.util;
 
 import java.text.NumberFormat;
 
 /**
  * Utility class for formatting numbers.
  * @author Michael Angstadt
  */
 public class NumberFormatter {
 	private static final NumberFormat nf = NumberFormat.getNumberInstance();
 
 	/**
 	 * Formats a quantity as a string.
 	 * @param quantity the quantity
 	 * @return the quantity string (e.g. "+1,210")
 	 */
 	public static String formatQuantity(int quantity) {
 		return formatQuantity(quantity, true);
 	}
 
 	/**
 	 * Formats a quantity as a string.
 	 * @param quantity the quantity
 	 * @param addPlus true to prepend a "+" character to positive values, false
 	 * not to
 	 * @return the quantity string (e.g. "+1,210")
 	 */
 	public static String formatQuantity(int quantity, boolean addPlus) {
 		String quantityStr = nf.format(quantity);
 		if (quantity > 0 && addPlus) {
 			quantityStr = "+" + quantityStr;
 		}
 		return quantityStr;
 	}
 
 	/**
 	 * Formats a quantity in stacks.
 	 * @param quantity the quantity
 	 * @return the quantity string (e.g. "+1/30")
 	 */
 	public static String formatStacks(int quantity) {
 		return formatStacks(quantity, true);
 	}
 
 	/**
 	 * Formats a quantity in stacks.
 	 * @param quantity the quantity
 	 * @param addPlus true to prepend a "+" character to positive values, false
 	 * not to
 	 * @return the quantity string (e.g. "+1/30")
 	 */
 	public static String formatStacks(int quantity, boolean addPlus) {
 		int stacks = quantity / 64;
 		if (stacks == 0) {
			return quantity + "";
 		}
 
 		int remaining = quantity % 64;
 		if (remaining < 0) {
 			//the remaining part should not contain a "-"
 			remaining *= -1;
 		}
 
 		String quantityStr = nf.format(stacks) + "/" + remaining;
 		if (quantity > 0 && addPlus) {
 			quantityStr = "+" + quantityStr;
 		}
 		return quantityStr;
 	}
 
 	/**
 	 * Formats a rupee amount as a string.
 	 * @param rupees the amount of rupees
 	 * @return the rupee string (e.g. "+1,210r")
 	 */
 	public static String formatRupees(int rupees) {
 		return formatRupees(rupees, true);
 	}
 
 	/**
 	 * Formats a rupee amount as a string.
 	 * @param rupees the amount of rupees
 	 * @param addPlus true to prepend a "+" character to positive values, false
 	 * not to
 	 * @return the rupee string (e.g. "+1,210r")
 	 */
 	public static String formatRupees(int rupees, boolean addPlus) {
 		return formatQuantity(rupees, addPlus) + "r";
 	}
 
 	/**
 	 * Formats a rupee amount as colored HTML.
 	 * @param rupees the amount of rupees
 	 * @return the colored HTML string
 	 */
 	public static String formatRupeesWithColor(int rupees) {
 		String text = formatRupees(rupees);
 		return colorize(text, rupees);
 	}
 
 	/**
 	 * Formats a quantity as colored HTML.
 	 * @param quantity the quantity
 	 * @return the colored HTML string
 	 */
 	public static String formatQuantityWithColor(int quantity) {
 		String text = formatQuantity(quantity);
 		return colorize(text, quantity);
 	}
 
 	/**
 	 * Formats a quantity as colored HTML.
 	 * @param quantity the quantity
 	 * @return the colored HTML string
 	 */
 	public static String formatStacksWithColor(int quantity) {
 		String text = formatStacks(quantity);
 		return colorize(text, quantity);
 	}
 
 	private static String colorize(String text, int number) {
 		if (number == 0) {
 			return text;
 		}
 
 		String color = (number < 0) ? "red" : "green";
 		return "<font color=" + color + ">" + text + "</font>";
 	}
 
 	private NumberFormatter() {
 		//hide
 	}
 }
