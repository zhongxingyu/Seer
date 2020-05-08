 package testtest;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public class StringReorderDistanceApart {
 
 	// use a greedy strategy
 	static String reorder(String str, int dist) {
 
 		Map<Character, Integer> contentMap = sortCharactors(str);
 
		FixedSizeQueue<Character> window = new FixedSizeQueue<Character>(dist);
 
 		StringBuilder sb = new StringBuilder();
 
 		while (!contentMap.isEmpty()) {
 
 			Set<Character> keys = contentMap.keySet();
 
 			for (Character key : keys) {
 
 				if (contentMap.get(key) == 0) {
 
 					contentMap.remove(key);
 
 				} else {
 
 					if (!window.contains(key)) {
 
 						sb.append(key);
 						window.addLast(key);
 						contentMap.put(key, contentMap.get(key) - 1);
 					}
 				}
 			}
 		}
 
 		return sb.toString();
 	}
 
 	static Map<Character, Integer> sortCharactors(String str) {
 
 		Map<Character, Integer> contentMap = new HashMap<Character, Integer>();
 
 		for (int i = 0; i < str.length(); i++) {
 			char c = str.charAt(i);
 
 			if (contentMap.keySet().contains(c)) {
 				contentMap.put(c, contentMap.get(c) + 1);
 			} else {
 				contentMap.put(c, 1);
 			}
 		}
 
 		System.out.println("before sorted: " + contentMap);
 
 		List<Entry<Character, Integer>> entryList = new ArrayList<Entry<Character, Integer>>(
 				contentMap.entrySet());
 
 		Collections.sort(entryList, new ValueComparator());
 
 		contentMap = new LinkedHashMap<Character, Integer>();
 
 		for (Entry<Character, Integer> entry : entryList) {
 			contentMap.put(entry.getKey(), entry.getValue());
 		}
 
 		System.out.println("after sorted: " + contentMap);
 
 		return contentMap;
 	}
 
 	public static void main(String[] args) {
 
 		final int DISTANCE = 3;
 		final String INPUT = "ddddaaaaaabbbbbccc";
 
 		System.out.println(reorder(INPUT, DISTANCE));
 	}
 }
 
 class ValueComparator implements Comparator<Entry<Character, Integer>> {
 
 	@Override
 	public int compare(Entry<Character, Integer> o1,
 			Entry<Character, Integer> o2) {
 
 		return o2.getValue().compareTo(o1.getValue());
 	}
 }
 
 class FixedSizeQueue<T> extends LinkedList<T> {
 
 	private static final long serialVersionUID = 1L;
 
 	private int maxSize;
 
 	public FixedSizeQueue(int size) {
 
 		super();
 
 		this.maxSize = size;
 	}
 
 	public void addLast(T t) {
 
 		while (super.size() >= maxSize) {
 			super.removeFirst();
 		}
 
 		super.addLast(t);
 	}
 }
