 import java.io.*;
 import java.util.*;
 
 public class Main {
 	public static final int MAX_WORD_LENGTH = 16;
 
 	private PrintStream out;
 	private BufferedReader in;
 	private Map<Integer,Set<String>> dict;
 
 	public static void main (String[] args) throws Exception {
 		new Main();
 	}
 
 	public Main () throws Exception {
 		out = System.out;
 		in = new BufferedReader(new InputStreamReader(System.in));
 		dict = new HashMap<Integer,Set<String>>();
 
 		String firstLine = in.readLine();
 		int dictLength = new Scanner(firstLine).nextInt();
 
 		for (int i = 0; i < dictLength; i++) {
 			String word = in.readLine();
 			if (dict.get(word.length()) == null) {
 				dict.put(word.length(), new HashSet<String>());
 			}
 			dict.get(word.length()).add(word);
 		}
 
 		String l = in.readLine();
 		while (l != null) {
 			List<String> encodedWords = new ArrayList(new HashSet<String>(Arrays.asList(l.split(" "))));
 			Collections.sort(encodedWords, new NumBranchComparator());
			/*
			Iterator<String> it = encodedWords.iterator();
			while (it.hasNext()) {
				out.println(it.next());
			}
			out.println();
			*/
 			ImmutableTranslator translator = 
				findTranslator(encodedWords, 2, new ImmutableTranslator());
			if (translator != null) {
				out.println(translator.decode(l));
			} else {
				out.println("********************");
 			}
 			l = in.readLine();
 		}
 	}
 
 	private class NumBranchComparator implements Comparator<String> {
 		private int countBranches (String a) {
 			Set<String> branches = dict.get(a.length());
 			return (branches != null) ? branches.size() : 0;
 		}
 
 		public int compare (String a, String b) {
 			Integer numABranches = countBranches(a);
 			Integer numBBranches = countBranches(b);
 			return numABranches.compareTo(numBBranches);
 		}
 	}
 
 	private class ImmutableTranslator {
		char[] decode;
		char[] encode;
 
 		//static
 		private char[] createEmptyCharMap () {
 			char[] charMap = new char[128];
			Arrays.fill(charMap, ' ');
 			return charMap;
 		}
 
 		public ImmutableTranslator () {
 			decode = createEmptyCharMap();
 			encode = createEmptyCharMap();
 		}
 
 		public ImmutableTranslator (char[] decode, char[] encode) {
 			this.decode = decode;
 			this.encode = encode;
 		}
 
 		//invariant: from.length() == to.length(), from and to contain no spaces
 		public ImmutableTranslator addPair (String from, String to) {
 			char[] tmpDecode = Arrays.copyOf(decode, decode.length);
 			char[] tmpEncode = Arrays.copyOf(encode, encode.length);
 			
 			for (int i  = 0; i < from.length(); i++) {
 				char fromChar = from.charAt(i);
 				byte fromByte = (byte) fromChar;
 				char toChar = to.charAt(i);
 				byte toByte = (byte) toChar;
				if (tmpDecode[fromByte] == ' ') {
 					tmpDecode[fromByte] = toChar;
 				} else if (tmpDecode[fromByte] == toChar) {
 					// do nothing
 				} else {
 					return null;
 				}
				if (tmpEncode[toByte] == ' ') {
 					tmpEncode[toByte] = fromChar;
 				} else if (tmpEncode[toByte] == fromChar) {
 					// do nothing
 				} else {
 					return null;
 				}
 			}
 			return new ImmutableTranslator(tmpDecode, tmpEncode);
 		}
 
 		public String decode (String from) {
 			String to = new String();
 			for (byte b : from.getBytes()) {
 				to += decode[b];
 			}
 			return to;
 		}
 	}
 
 	private ImmutableTranslator findTranslator (List<String> encodedWords, 
 		int index, ImmutableTranslator translator) {
 		if (index == encodedWords.size()) {
 			return translator;
 		}
 		String encodedWord = encodedWords.get(index);
 		for (String dictWord : dict.get(encodedWord.length())) {
 			ImmutableTranslator newTranslator = translator.addPair(encodedWord, 
 				dictWord);
 			if (newTranslator == null) {
 				continue;
 			}
 			ImmutableTranslator result = findTranslator(encodedWords, 
 				index + 1, newTranslator);
 			if (result != null) {
 				return result;
 			}
 		}
 		return null;
 	}
 }
 
