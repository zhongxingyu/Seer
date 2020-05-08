 package za.org.opengov.common.util;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 public class StringMatcher {
 
 	private List<Element> elements;
 
 	public StringMatcher() {
 		elements = new LinkedList<StringMatcher.Element>();
 	}
 
 	public StringMatcher(List<StringMatchable> elementsToCompare) {
 		this();
 		if (elementsToCompare == null) {
 			throw new IllegalArgumentException(
 					"elementsToCompare can not be null");
 		}
 		// wrap each StringMatchable in an Element
 		for (StringMatchable stringMatchable : elementsToCompare) {
 			Element element = new Element(stringMatchable);
 			elements.add(element);
 		}
 
 	}
 
 	public void addStringMatchable(StringMatchable stringMatchable) {
 		elements.add(new Element(stringMatchable));
 	}
 
 	public StringMatchable getClosestMatch(String stringMatchableToMatch) {
 
 		if (stringMatchableToMatch == null) {
 			throw new IllegalArgumentException(
 					"stringMatchableToMatch must not be null");
 		}
 
 		Element closestMatchable = new Element(null);
 		closestMatchable.distance = Integer.MAX_VALUE;
 
 		for (Element nextElement : elements) {
 			String toCompare = nextElement.stringMatchable.getStringToMatch();
 			if (toCompare == null) {
 				// a null string is considered an empty string
 				toCompare = "";
 			}
 			nextElement.distance = computeLevenshteinDistance(
 					stringMatchableToMatch, toCompare);
 			if (nextElement.distance < closestMatchable.distance) {
 				closestMatchable = nextElement;
 			}
 		}
 
 		return closestMatchable.stringMatchable;
 	}
 
 	public List<StringMatchable> getClosestMatches(
 			StringMatchable stringMatchableToMatch, int limit) {
 
 		if (stringMatchableToMatch == null) {
 			throw new IllegalArgumentException(
 					"stringMatchableToMatch must not be null");
 		}
 		if (limit < 1) {
 			throw new IllegalArgumentException("limit must be >= 1");
 		}
 
 		for (Element nextElement : elements) {
 			String toCompare = nextElement.stringMatchable.getStringToMatch();
 			if (toCompare == null) {
 				// a null string is considered an empty string
 				toCompare = "";
 			}
 			nextElement.distance = computeLevenshteinDistance(
 					stringMatchableToMatch.getStringToMatch(), toCompare);
 
 		}
 
 		Collections.sort(elements);
 
 		List<StringMatchable> orderedResults = new LinkedList<StringMatchable>();
 
 		for (int i = 0; i < limit && i < elements.size(); i++) {
 			orderedResults.add(elements.get(i).stringMatchable);
 		}
 
 		return orderedResults;
 	}
 
 	private int computeLevenshteinDistance(String s1, String s2) {
 
 		s1 = s1.toLowerCase();
		
		s2 = s2.toLowerCase();
 		//trimming the word allows better matching to a word prefix
		if(s2.length() > s1.length()){
			s2 = s2.substring(0, s1.length());
		}
 
 		int[] costs = new int[s2.length() + 1];
 		for (int i = 0; i <= s1.length(); i++) {
 			int lastValue = i;
 			for (int j = 0; j <= s2.length(); j++) {
 				if (i == 0)
 					costs[j] = j;
 				else {
 					if (j > 0) {
 						int newValue = costs[j - 1];
 						if (s1.charAt(i - 1) != s2.charAt(j - 1))
 							newValue = Math.min(Math.min(newValue, lastValue),
 									costs[j]) + 1;
 						costs[j - 1] = lastValue;
 						lastValue = newValue;
 					}
 				}
 			}
 			if (i > 0)
 				costs[s2.length()] = lastValue;
 		}
 		return costs[s2.length()];
 	}
 
 	private class Element implements Comparable<Element> {
 
 		private int distance;
 		private StringMatchable stringMatchable;
 
 		public Element(StringMatchable stringMatchable) {
 			this.stringMatchable = stringMatchable;
 		}
 
 		@Override
 		public int compareTo(Element o) {
 			if (distance < o.distance) {
 				return -1;
 			} else if (distance == o.distance) {
 				return 0;
 			} else {
 				return 1;
 			}
 		}
 
 	}
 
 }
