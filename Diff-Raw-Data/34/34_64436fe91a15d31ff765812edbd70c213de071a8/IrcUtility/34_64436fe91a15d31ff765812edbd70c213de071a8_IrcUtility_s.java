 package jisssea.util;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 public class IrcUtility {
 
 	private static final Set<String> prefixes = new HashSet<String>(Arrays.asList("#", "+", "!", "&"));
 
 	public static void main(String[] args) {
		String t1 = "uwcsnet:#bot";
 		System.out.println(isValidTarget(t1));
 		System.out.println(getNetwork(t1));
 		System.out.println(getCorrespondant(t1));
 	}
 
 	public static boolean isValidChannel(final String name) {
 		for (String prefix : prefixes) {
 			if (name.startsWith(prefix))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if form "uwcsnet:#bots"
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public static boolean isValidTarget(final String name) {
 		if (name == null)
 			return false;
 
 		final int indexOf = name.indexOf(':');
 		return indexOf != -1 && prefixes.contains(name.substring(indexOf + 1, indexOf + 2));
 	}
 
 	public static String getNetwork(final String name) {
 		return name.substring(0, name.indexOf(':'));
 	}
 
 	/**
 	 * The Correspondant could be the channel or the user
 	 * 
 	 * @param name
 	 *            the complete target string
 	 * @return the Correspondant of the target string
 	 */
 	public static String getCorrespondant(final String name) {
 		return name.substring(name.indexOf(':') + 1);
 	}
 
 	/**
 	 * If its a valid target then gets the correspondant, otherwise returns the
 	 * string
 	 * 
 	 * @param target
 	 * @return
 	 */
 	public static String getSafeCorrespondant(final String possibleTarget) {
 		if (isValidTarget(possibleTarget)) {
 			return getCorrespondant(possibleTarget);
 		} else {
 			return possibleTarget;
 		}
 	}
 }
