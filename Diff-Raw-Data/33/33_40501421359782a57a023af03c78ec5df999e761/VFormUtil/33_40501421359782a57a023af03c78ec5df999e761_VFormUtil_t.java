 package ch.sbs.utils.preptools.vform;
 
 public class VFormUtil {
 	private static final String[] vforms = new String[] { "Ihr(?:en|em|es|e)?",
 			"Ihn(?:en)?", "Euer", "Eure(?:n|m|s)?", "Dein(?:en|em|es|e)?",
 			"Sie", "Du", "Dir", "Dich", "Euch" };
 
	public static String replace(final String theText) {
		String result = theText;
		for (final String vform : vforms) {
			result = result.replaceAll(vform + "\\b",
					"<brl:v-form>$0</brl:v-form>");
 		}
		return result;
 	}
 
 }
