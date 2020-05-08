 package ru.kirillova.search.database;
 
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Kgramm {
 
 	private List<String> getWordsWithGramm(String s) {
 		// Это все нужно удалить. Для тестирования писала. Нужно забирать из базы список строк,
 		// которым соответствует данная биграмма s. Да, s - это биграмма. Не знаю как там что у
 		// тебя из базы доставать, поэтому оставлю вопрос открытым)
         List<String> result = new ArrayList<String>();
         if (s.equals("кр")) {
             result.add("кра");
             result.add("кран");
             result.add("круг");
         }
         if (s.equals("ра")) {
             result.add("кра");
             result.add("кран");
         }
         if (s.equals("ан")) {
             result.add("кран");
             result.add("ман");
 
         }
         if (s.equals("ру")) {
             result.add("круг");
 
         }
         if (s.equals("уг")) {
             result.add("круг");
 
         }
         if (s.equals("ма")) {
             result.add("ман");
         }
         if (s.equals("ак")) {
             result.add("кра");
         }
         if (s.equals("нк")) {
             result.add("кран");
         }
         if (s.equals("гк")) {
             result.add("круг");
         }
         if (s.equals("нм")) {
             result.add("ман");
         }
         // работа с бд
         // нужно вернуть список строк, в которых содержится биграмм s
         return result;
     }
 
 	private List<String> getSimilarStrings(String s) {
 		List<String> result = new ArrayList<String>();
 		Map<String, Integer> words = new TreeMap<String, Integer>();
 		for (int i = 0; i < s.length(); ++i) {
 			List<String> term = getWordsWithGramm("" + s.charAt(i % s.length())
 					+ s.charAt((i + 1) % s.length()));
 			ListIterator<String> e = term.listIterator();
 			while (e.hasNext()) {
 				String str = e.next();
 				if (words.containsKey(str)) {
 					words.put(str, words.get(str) + 1);
 				} else {
 					words.put(str, 1);
 				}
 			}
 		}
 		Iterator<Entry<String, Integer>> it = words.entrySet().iterator();
 		while (it.hasNext()) {
 			Entry<String, Integer> e = it.next();
 			double t = ((double)e.getValue())
 					/ (s.length() - 1 + e.getKey().length() - 1 - e.getValue());
 			if (t >= 0.5) {
 				result.add(e.getKey());
 			}
 		}
 		return result;
 	}
 
 	private double costExchange(char a, char b) {
 		if ((a >= 'a') && (a <= 'z')) {
 			Pattern r[] = new Pattern[9];
 			r[0] = Pattern.compile("(a|o|e|i|u|y)");
 			r[1] = Pattern.compile("(b|p)");
 			r[2] = Pattern.compile("(c|k|q)");
 			r[3] = Pattern.compile("(d|t)");
 			r[4] = Pattern.compile("(r|l)");
 			r[5] = Pattern.compile("(m|n)");
 			r[6] = Pattern.compile("(g|j)");
 			r[7] = Pattern.compile("(f|v)");
 			r[8] = Pattern.compile("(s|x|z)");
 			Matcher m1;
 			Matcher m2;
 			for (int i = 0; i < 9; ++i) {
 				m1 = r[i].matcher("" + a);
 				m2 = r[i].matcher("" + b);
 				if (m1.matches() && m2.matches()) {
 					return 0.7;
 				}
 			}
 		}
         if ((a >= 'а') && (a <= 'я')) {
             Pattern r[] = new Pattern[8];
             r[0] = Pattern.compile("(а|о|е|и|ё|я|ы|у|ю|э)");
             r[1] = Pattern.compile("(б|п)");
             r[2] = Pattern.compile("(з|с)");
             r[3] = Pattern.compile("(к|г)");
             r[4] = Pattern.compile("(ш|щ)");
             r[5] = Pattern.compile("(в|ф)");
             r[6] = Pattern.compile("(д|т)");
             r[7] = Pattern.compile("(л|м|н)");
             Matcher m1;
             Matcher m2;
             for (int i = 0; i < 8; ++i) {
                 m1 = r[i].matcher("" + a);
                 m2 = r[i].matcher("" + b);
                 if (m1.matches() && m2.matches()) {
                     return 0.7;
                 }
             }
         }
         return 1.5;
     }
 
 	private double levDist(String s1, String s2) {
 		double w_insert = 2.;
 		double w_transp = 1.;
 		int n = s1.length();
 		int m = s2.length();
 		if (n == 0) {
 			if (m == 0) {
 				return 0;
 			}
 			return m * w_insert;
 		}
 		if (m == 0) {
 			return n * w_insert;
 		}
 		double d[][] = new double[n + 2][m + 2];
 		double inf = w_insert * Math.max(n, m);
 		d[0][0] = inf;
 		for (int i = 0; i <= n; ++i) {
 			d[i + 1][1] = i;
 			d[i + 1][0] = inf;
 		}
 		for (int i = 0; i <= m; ++i) {
 			d[1][i + 1] = i;
 			d[0][i + 1] = inf;
 		}
 		Map<Character, Integer> lastPosition = new TreeMap<Character, Integer>();
 		for (int i = 0; i < n; ++i) {
 			int last = 0;
 			for (int j = 0; j < m; ++j) {
 				int i1;
 				if (lastPosition.containsKey(s2.charAt(j))) {
 					i1 = lastPosition.get(s2.charAt(j));
 				} else {
 					i1 = 0;
 				}
 				int j1 = last;
 				if (s1.charAt(i) == s2.charAt(j)) {
 					d[i + 2][j + 2] = d[i + 1][j + 1];
 					last = j;
 				} else {
 					d[i + 2][j + 2] = Math.min(
 							d[i + 1][j + 1]
 									+ costExchange(s1.charAt(i), s2.charAt(j)),
 							Math.min(d[i + 2][j + 1] + w_insert,
 									d[i + 1][j + 2] + w_insert));
 				}
 				d[i + 2][j + 2] = Math.min(d[i + 2][j + 2], d[i1 + 1][j1 + 1]
 						+ (i - i1) + (j - j1) + w_transp);
 			}
 			lastPosition.put(s1.charAt(i), i);
 		}
 		return d[n + 1][m + 1];
 	}
 
     public List<String> fixMistake(String str) {
         Normalize n = new Normalize();
         String s = n.getBasisWord(str);
         String ends = str.substring(s.length(), str.length());
         List<String> result = new ArrayList<String>();
         List<String> words = getSimilarStrings(s);
         Map<String, Double> nearestwords = new TreeMap<String, Double>();
         ListIterator<String> it = words.listIterator();
         while (it.hasNext()) {
             String term = it.next();
             double d = levDist(s, term);
             if (d <= 3.) {
                 nearestwords.put(term + ends, d);
             }
         }
         // можно добавить функцию рейтинга
         List<Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>(
                 nearestwords.entrySet());
         Collections.sort(entries, new Comparator<Entry<String, Double>>() {
             public int compare(Entry<String, Double> e1,
                                Entry<String, Double> e2) {
                 double v1 = e1.getValue();
                 double v2 = e2.getValue();
                 return (v1 > v2) ? 1 : (v1 == v2) ? 0 : -1;
             }
         });
         ListIterator<Entry<String, Double>> it2 = entries.listIterator();
         int k = 10;
         for (int i = 0; (i < k) && (it2.hasNext()); ++i) {
             result.add(it2.next().getKey());
         }
         return result;
     }
 }
