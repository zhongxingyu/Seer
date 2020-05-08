 package ch.unibe.scg.cc.javaFrontend;
 
 import javax.inject.Singleton;
 
 import jregex.Pattern;
 import ch.unibe.scg.cc.ReplacerProvider;
 import ch.unibe.scg.cc.regex.Replace;
 
 import com.google.inject.Provider;
 
 @Singleton
 public class JavaType1ReplacerFactory extends ReplacerProvider implements Provider<Replace[]> {
 
 	/**
 	 * Hides deep function definitions
 	 */
 	// Replace makeHideDeepDefinitions() {
 	// Pattern ifPattern = new
 	// Pattern("(\\t{2,}|\\ {8,})([a-zA-Z \\t<>,]*\\([a-zA-Z \\t<>,]*\\)[a-zA-Z \\t<>,]*\\{|(\\n|[^\n]*[^.])class)");
 	// return new Replace(ifPattern, "; $1");
 	// }
 
 	/**
 	 * 0
 	 */
 	public Replace make00WhitespaceA() {
 		Pattern whiteSpace = new Pattern("\\s*\\n\\s*");
 		return new Replace(whiteSpace, "\n");
 	}
 
 	/**
 	 * 1
 	 */
 	public Replace make01WhitespaceB() {
 		Pattern whiteSpace = new Pattern("[ \f\r\t]+");
 		return new Replace(whiteSpace, " ");
 	}
 
 	/**
 	 * 2. removes package prefixes. <br>
 	 * <code>example 1:<br>"import java.util.Math;" gets "import Math;"</code><br>
 	 * <code>example 2:<br>
 	 * "int a = java.util.Math.sqrt(1);" gets "int a = Math.sqrt(1);"</code>
 	 */
 	public Replace make02Rj1() {
		final Pattern packagesGo = new Pattern("\\b[a-z]+[a-z.]*\\.([A-Z])");
		final String replaceWith = "$1";
 		return new Replace(packagesGo, replaceWith);
 	}
 
 	/**
 	 * 3
 	 */
 	public Replace make03Rj3a() {
 		Pattern initializationList = new Pattern("=\\s?\\{.*?\\}");
 		return new Replace(initializationList, "= { }");
 	}
 
 	/**
 	 * 4
 	 */
 	public Replace make04Rj3b() {
 		Pattern initializationList = new Pattern("\\]\\s?\\{.*?\\}");
 		return new Replace(initializationList, "] { }");
 	}
 
 	/**
 	 * 5
 	 */
 	public Replace make05Rj5() {
 		Pattern visibility = new Pattern("(\\s)(?:private\\s|public\\s|protected\\s)");
 		return new Replace(visibility, "$1");
 	}
 
 	/**
 	 * 6
 	 */
 	public Replace make06Rj6() {
 		Pattern ifPattern = new Pattern("\\sif\\s*\\((?:.*)\\)\\s*(\\n[^\\n\\{\\}]*)$");
 		return new Replace(ifPattern, " {\\n$1\\n}\\n");
 	}
 
 	/**
 	 * 7. This is necessary for tokenization. while statements look a lot like
 	 * function definitions.
 	 * 
 	 * @return
 	 */
 	public Replace make07RenameWhile() {
 		Pattern ifPattern = new Pattern("while");
 		return new Replace(ifPattern, ";while");
 	}
 
 	/**
 	 * 8. removes leading whitespace at the beginning of the string. example:
 	 * "	 public void a() {" gets "public void a() {"
 	 */
 	public Replace make08RemoveLeadingWhitespace() {
 		Pattern leadingWhiteSpace = new Pattern("^[ \f\r\t]*");
 		return new Replace(leadingWhiteSpace, "");
 	}
 }
