 package ch.sbs.utils.preptools.vform;
 
 public class VFormUtil {
 	private static final String[] vforms = new String[] { "Ihr(?:en|em|es|e)?",
 			"Ihn(?:en)?", "Euer", "Eure(?:n|m|s)?", "Dein(?:en|em|es|e)?",
 			"Sie", "Du", "Dir", "Dich", "Euch" };
 
	public static String replace(String text) {
		for (String vform : vforms) {
			text = text.replaceAll(vform, "<brl:v-form>$0</brl:v-form>");
 		}
		return text;
 	}
 
 }
