 package pleocmd;
 
 import java.awt.Color;
 import java.util.Set;
 
 public final class StringManip {
 
 	private StringManip() {
 		// final class and only static methods
 	}
 
 	public static String safeHTML(final String s) {
 		return s.replace("<", "&lt;").replace(">", "&gt;")
 				.replace("\n", "<br>");
 	}
 
 	public static String safeTex(final String s) {
 		return safeTex(s, false);
 	}
 
 	public static String safeTex(final String s, final boolean onlyPart) {
 		final StringBuilder sb = new StringBuilder();
 		char last = '\0';
 		for (final char c : s.toCharArray()) {
 			switch (c) {
 			case '{':
 				sb.append("\\textbraceleft{}");
 				break;
 			case '}':
 				sb.append("\\textbraceright{}");
 				break;
 			case '[':
				sb.append(onlyPart ? "[" : "\\verb+[+");
 				break;
 			case ']':
				sb.append(onlyPart ? "]" : "\\verb+]+");
 				break;
 			case '\\':
 				sb.append("\\textbackslash{}");
 				break;
 			case '&':
 				sb.append(onlyPart ? "&" : "\\&");
 				break;
 			case '$':
 				sb.append("\\textdollar{}");
 				break;
 			case '\n':
 				sb.append("\\\\\n");
 				break;
 			case '\"':
 				sb.append("''");
 				break;
 			case '.':
 				sb.append("\"\".");
 				break;
 			case ' ':
 				sb.append(last == ' ' || last == '{' || // 
						last == '>' ? "\\textvisiblespace{}" : " ");
 				break;
 			default:
 				sb.append(c);
 			}
 			last = c;
 		}
 		return sb.toString();
 	}
 
 	public static String removePseudoHTML(final String text) {
 		if (!text.startsWith("<html>")) return text;
 		final StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < text.length(); ++i) {
 			final char c = text.charAt(i);
 			switch (c) {
 			case '<': {
 				while (text.charAt(++i) != '>') {
 					// ignore the whole tag
 				}
 				break;
 			}
 			case '&': {
 				final StringBuilder sb2 = new StringBuilder();
 				char c2;
 				while ((c2 = text.charAt(++i)) != ';')
 					sb2.append(c2);
 				final String id = sb2.toString();
 				if ("lt".equals(id))
 					sb.append('<');
 				else if ("gt".equals(id))
 					sb.append('>');
 				else if ("amp".equals(id))
 					sb.append('&');
 				else if ("quot".equals(id))
 					sb.append('"');
 				else
 					Log.detail("Ignoring unknown &%s;", id); // CS_IGNORE
 				break;
 			}
 			default:
 				sb.append(c);
 			}
 		}
 		return sb.toString();
 	}
 
 	public static String convertPseudoToRealHTML(final String text) {
 		if (!text.startsWith("<html>")) return safeHTML(text);
 		final StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < text.length(); ++i) {
 			final char c = text.charAt(i);
 			switch (c) {
 			case '<': {
 				final StringBuilder sb2 = new StringBuilder();
 				char c2;
 				while ((c2 = text.charAt(++i)) != '>')
 					sb2.append(c2);
 				final String tag = sb2.toString();
 				if ("html".equals(tag) || "/html".equals(tag)) {
 					// just ignore them silently
 				} else
 					sb.append("<" + tag + ">");
 				break;
 			}
 			default:
 				sb.append(c);
 			}
 		}
 		return sb.toString();
 	}
 
 	public static String convertPseudoHTMLToTex(final String s,
 			final Set<String> colorNames) {
 		if (!s.startsWith("<html>")) return safeTex(s);
 		final String text = safeTex(s, true);
 		final StringBuilder sb = new StringBuilder();
 		sb.append("{");
 		for (int i = 0; i < text.length(); ++i) {
 			final char c = text.charAt(i);
 			switch (c) {
 			case '<': {
 				final StringBuilder sb2 = new StringBuilder();
 				char c2;
 				while ((c2 = text.charAt(++i)) != '>')
 					sb2.append(c2);
 				final String tag = sb2.toString();
 				if ("/font".equals(tag) || "/b".equals(tag) || "/i".equals(tag)) {
 					// ignore
 				} else if (tag.startsWith("b"))
 					sb.append("}{\\bf ");
 				else if (tag.startsWith("i"))
 					sb.append("}{\\it ");
 				else if (tag.startsWith("font color=#")) {
 					final String clrName = "PleoCommandColor"
 							+ tag.substring(12, 18);
 					sb.append("}\\textcolor{");
 					sb.append(clrName);
 					colorNames.add(clrName);
 					sb.append("}{");
 				} else if ("html".equals(tag) || "/html".equals(tag)) {
 					// just ignore them silently
 				} else
 					Log.detail("Ignoring unknown tag '%s'", tag);
 				break;
 			}
 			case '&': {
 				final StringBuilder sb2 = new StringBuilder();
 				char c2;
 				while ((c2 = text.charAt(++i)) != ';')
 					sb2.append(c2);
 				final String id = sb2.toString();
 				if ("lt".equals(id))
 					sb.append('<');
 				else if ("gt".equals(id))
 					sb.append('>');
 				else if ("amp".equals(id))
 					sb.append("\\&");
 				else if ("quot".equals(id))
 					sb.append("''");
 				else
 					Log.detail("Ignoring unknown &%s;", id); // CS_IGNORE
 				break;
 			}
 			default:
 				sb.append(c);
 			}
 		}
 		sb.append("}");
 		return sb.toString();
 	}
 
 	public static Color hexToColor(final String hex) {
 		try {
 			return new Color(Integer.valueOf(hex.substring(0, 2), 16), Integer
 					.valueOf(hex.substring(2, 4), 16), Integer.valueOf(hex
 					.substring(4, 6), 16));
 		} catch (final NumberFormatException e) {
 			return Color.RED;
 		}
 	}
 
 }
