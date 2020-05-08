 package com.joelj.jcss.parse;
 
 
 import java.util.*;
 
 /**
  * Utility methods for converting CSS Selectors into Xpath expressions.
  *
  * User: joeljohnson
  * Date: 5/15/12
  * Time: 8:50 PM
  */
 public class CssUtils {
 	private static final Collection<Character> QUOTE_CHARACTERS = Arrays.asList('"', '\'');
 	public static String convertCssToXpath(String cssSelector) {
 		String selector = cssSelector.replaceAll("\\s*>\\s*", ">"); //remove padding around >
 		selector = selector.replaceAll("\\s*\\+\\s*", "+"); //remove padding around +
 		selector = selector.replaceAll("\\s+", " "); //normalize whitespace
 
 		List<String> sections = split(selector);
 
 		StringBuilder sb = new StringBuilder("//");
 		Iterator<String> iterator = sections.iterator();
 		while(iterator.hasNext()) {
 			String section = convertCssSectionToXpath(iterator.next());
 			sb.append(section);
 
 			if(iterator.hasNext()) {
 				String separator = iterator.next();
 				if(separator.equals(" ")) {
 					sb.append("//");
 				} else if(separator.equals(">")) {
 					sb.append("/");
 				} else if(separator.equals("+")) {
 					sb.append("/following-sibling::*[1]/self::");
 				} else {
 					//This is 'impossible' since the split method only allows the ' ', >, and +.
 					throw new CssParseException("Unknown separator character. Should be ' ', '+', or '>'.");
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Splits the CSS Selector by the >, +, or ' ' (space).
 	 * @param cssSelector The selector to be split.
 	 *                    It is assumed that whitespace has been normalized
 	 *                    and padding around + and > has been stripped.<br/>
 	 *                    e.g. " > " should be replaced with '>'.<br/>
 	 *                    e.g. " \n\r\t" should be replaced with ' '.<br/>
 	 * @return A list of each section. Each section is in the
 	 */
 	static List<String> split(String cssSelector) {
 		List<String> sections = new ArrayList<String>();
 		int last;
 		int next = -1;
 
 		do {
 			last = next;
 			int angleIndex = indexOfNotQuoted(cssSelector, '>', last + 1);
 			int plusIndex = indexOfNotQuoted(cssSelector, '+', last + 1);
 			int spaceIndex = indexOfNotQuoted(cssSelector, ' ', last + 1);
 
 			if(!(angleIndex == -1 && plusIndex == -1 && spaceIndex == -1)) {
 				angleIndex = angleIndex < 0 ? Integer.MAX_VALUE : angleIndex;
 				plusIndex = plusIndex < 0 ? Integer.MAX_VALUE : plusIndex;
 				spaceIndex = spaceIndex < 0 ? Integer.MAX_VALUE : spaceIndex;
 			}
 
 			next = Math.min(Math.min(angleIndex, plusIndex), spaceIndex);
 
 			int endOfSection = next;
 			if(endOfSection < 0) {
 				endOfSection = cssSelector.length();
 			}
 			String section = cssSelector.substring(last+1, endOfSection);
 			sections.add(section);
 			if(next >= 0) {
 				String divider = cssSelector.substring(next, next+1);
 				sections.add(divider);
 			}
 		} while(next >= 0);
 		return sections;
 	}
 
 	static String convertCssSectionToXpath(String cssSelector) {
 		cssSelector = replaceClassAndIDSelectors(cssSelector);
 
 		int colonIndex = indexOfNotQuoted(cssSelector, ':');
 		if(colonIndex > 0) {
 			String start = cssSelector.substring(0, colonIndex);
 			String afterColon = cssSelector.substring(colonIndex+1);
 			if(afterColon.equals("first-child")) {
 				return "*[1]/self::" + start;
 			} else {
 				throw new CssParseException("Unknown selector: \"" +cssSelector + "\"");
 			}
 		}
 
 		int openBracketIndex = indexOfNotQuoted(cssSelector, '[');
 		if(openBracketIndex < 0) {
			if(cssSelector.isEmpty()) {
				return "*";
			}
 			return cssSelector;
 		}
 
 		String tagName = cssSelector.substring(0, openBracketIndex);
		if(tagName.isEmpty()) {
			tagName = "*";
		}
 
 		int closeBracketIndex = indexOfNotQuoted(cssSelector, ']', openBracketIndex + 1);
 		if(closeBracketIndex < 0) {
 			throw new CssParseException("No close bracket. Expecting ']'");
 		}
 		String attributeSection = convertCssAttributeSectionToXpath(cssSelector.substring(openBracketIndex, closeBracketIndex+1));
 		StringBuilder sb = new StringBuilder(tagName).append(attributeSection);
 
 		//now iterate through the rest of the attributes
 		while(openBracketIndex >= 0) {
 			openBracketIndex = indexOfNotQuoted(cssSelector, '[', closeBracketIndex);
 			if(openBracketIndex >= 0) {
 				closeBracketIndex = indexOfNotQuoted(cssSelector, ']', openBracketIndex + 1);
 				if(closeBracketIndex < 0) {
 					throw new CssParseException("No close bracket. Expecting ']'");
 				}
 				attributeSection = convertCssAttributeSectionToXpath(cssSelector.substring(openBracketIndex, closeBracketIndex+1));
 				sb.append(attributeSection);
 			}
 		}
 
 		return sb.toString().replace("][", " and ");
 	}
 
 	static String replaceClassAndIDSelectors(String cssSelector) {
 		cssSelector = cssSelector.replaceAll("\\.([\\w\\d]+\\b)", "[class~=\"$1\"]");
 		cssSelector = cssSelector.replaceAll("#([\\w\\d]+\\b)", "[id=\"$1\"]");
 		return cssSelector;
 	}
 
 	static String convertCssAttributeSectionToXpath(String attributeSection) {
 		assert attributeSection.startsWith("[") && attributeSection.endsWith("]");
 
 		int index = indexOfNotQuoted(attributeSection, '=');
 		if(index != -1) {
 			index = indexOfNotQuoted(attributeSection, '~');
 			if(index != -1) {
 				String attributeName = getAttributeName(attributeSection);
 				String attributeValue = getAttributeValue(attributeSection);
 				return "[contains(concat(\" \",@"+attributeName+",\" \"),concat(\" \",\""+attributeValue+"\",\" \"))]";
 			}
 		}
 
 		return attributeSection.replaceFirst("\\[", "[@");
 	}
 
 	static String getAttributeName(String attributeSection) {
 		int start = indexOfNotQuoted(attributeSection, '[') + 1;
 		int end = indexOfNotQuoted(attributeSection, '~');
 		if(end < 0) {
 			end = indexOfNotQuoted(attributeSection, '=', start);
 			if(end < 0) {
 				end = indexOfNotQuoted(attributeSection, ']', start);
 			}
 		}
 		return attributeSection.substring(start, end);
 	}
 
 	static String getAttributeValue(String attributeSection) {
 		int openBracket = indexOfNotQuoted(attributeSection, '[') + 1;
 		int equalsIndex = indexOfNotQuoted(attributeSection, '=', openBracket);
 		if(equalsIndex < 0) {
 			return null;
 		}
 		int openQuoteIndex = 0;
 		Character openQuoteCharacter = null;
 		for(openQuoteIndex = equalsIndex; openQuoteIndex < attributeSection.length(); openQuoteIndex++) {
 			char c = attributeSection.charAt(openQuoteIndex);
 			if(QUOTE_CHARACTERS.contains(c)) {
 				openQuoteCharacter = c;
 				break;
 			}
 		}
 		if(openQuoteCharacter == null) {
 			throw new CssParseException("No open quote character. Expecting \" or '");
 		}
 
 		int close = attributeSection.indexOf(openQuoteCharacter, openQuoteIndex+1);
 		if(close < 0) {
 			throw new CssParseException("No close quote character. Expecting " + openQuoteCharacter);
 		}
 		return attributeSection.substring(openQuoteIndex + 1, close);
 	}
 
 	static int indexOfNotQuoted(String string, char contains) {
 		return indexOfNotQuoted(string, contains, 0);
 	}
 
 	static int indexOfNotQuoted(String string, char contains, int startIndex) {
 		Character currentQuoteChar = null;
 		for (int i = startIndex; i < string.length(); i++) {
 			char c = string.charAt(i);
 			if(QUOTE_CHARACTERS.contains(c)) {
 				if(currentQuoteChar == null) {
 					//this is the opening quote
 					currentQuoteChar = c;
 					continue;
 				} else if(currentQuoteChar == c) {
 					//closing quote matches opening quote
 					currentQuoteChar = null;
 					continue;
 				}
 			}
 			if(currentQuoteChar == null) {
 				if(c == contains) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 }
