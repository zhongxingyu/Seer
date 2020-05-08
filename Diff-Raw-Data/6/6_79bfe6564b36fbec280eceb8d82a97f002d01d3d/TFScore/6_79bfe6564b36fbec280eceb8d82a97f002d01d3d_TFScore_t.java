 package feedlosophor.scoring;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 
 public class TFScore {
 
 
 	public ArrayList<String> tfWords; // complete word list, features
 
 	private Doc[] docs;
 
 	public TFScore(String[] contents, String[] titles, String[] ids, boolean enableStemming) {
		
		if (contents == null || titles == null || ids == null 
				|| contents.length != titles.length || titles.length != ids.length)  {
			return;
		}
		
 		// 1. parse each document
 		this.docs = new Doc[contents.length];
 		for (int i = 0; i < contents.length; i++) {
 			String content = contents[i];
 			if (enableStemming) content = TextStemming.Stemming(content);
 			docs[i] = DocParser.parse(content, titles[i], ids[i]);			
 		}
 
 		// 2. build complete word list
 		tfWords = new ArrayList<String>();
 		HashMap<String, Double> map;
 		for (int i = 0; i < docs.length; i++) {
 			map = docs[i].getTfMap();
 			for (Map.Entry<String, Double> entry: map.entrySet()) {
 				if (!tfWords.contains(entry.getKey())) {
 					tfWords.add(entry.getKey()); // add to complete word list
 
 				}
 			}
 		}
 
 		// 3. build TF vector for each document from complete word list
 		for (int i = 0; i < docs.length; i++) {			
 			double[] tfVector = new double[tfWords.size()];
 			for (int j = 0; j < tfWords.size(); j++) {
 				if (docs[i].getTfMap().containsKey(tfWords.get(j))) {
 					tfVector[j] = docs[i].getTfMap().get(tfWords.get(j));					
 				} else {
 					tfVector[j] = 0;
 				}			
 			}
 			docs[i].setTfVector(tfVector);
 		}
 
 	}
 
 	public TFScore(String[] contents, String[] titles, String[] ids) {
 
 		this(contents, titles, ids, false);
 
 	}
 
 	// only for testing purpose
 	public String printResult() {
 		StringBuilder sb = new StringBuilder();
 		DecimalFormat df = new DecimalFormat("#.###");
 
 		for (int i = 0; i < docs.length; i++) {
 			System.out.print("{");
 			for (Map.Entry<String, Double> entry: docs[i].getTfMap().entrySet()) {
 				System.out.print(entry.getKey() + "=" + df.format(entry.getValue()) + " ");
 			}
 			System.out.println("}\n");
 			double[] tfVector = docs[i].getTfVector();
 			for (int j = 0; j < tfVector.length; j++) {
 				String num = (tfVector[j] == 0 ? "0    " : df.format(tfVector[j])); 
 				sb.append(num).append(", ");
 			}
 			sb.append(docs[i].getId());
 			sb.append("\n");
 		}
 		return sb.toString();
 
 	}
 
 	// output
 	// result for un-supervised learning
 	public String getResult() {
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("@relation fake\n");
 		for (int i = 1; i <= tfWords.size(); i++) {
 			sb.append("@attribute " + i + " numeric\n");			
 		}
 		sb.append("@attribute " + (tfWords.size() + 1) + " string\n");
 		sb.append("\n");
 		sb.append("@data\n");
 		for (int i = 0; i < docs.length; i++) {
 			double[] tfVector = docs[i].getTfVector();
 			for (int j = 0; j < tfVector.length; j++) {
 				sb.append(tfVector[j]).append(", ");
 			}
 			sb.append(docs[i].getId());
 			sb.append("\n");
 		}
 		return sb.toString();
 	}
 
 
 }
 
 
 
 class Doc {
 
 	private String id;
 
 	private String content;
 	private HashMap<String, Double> tfMap; // (word, tf) pair
 
 	private double[] tfVector;
 
 	public Doc(String id, String content) {
 		this.id = id;
 		this.content = content;
 	}
 
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 
 	public double[] getTfVector() {
 		return this.tfVector;
 	}
 
 	public void setTfVector(double[] tfVector) {
 		this.tfVector = tfVector;
 	}
 	public HashMap<String, Double> getTfMap() {
 		return tfMap;
 	}
 
 	public void setTfMap(HashMap<String, Double> tfMap) {
 		this.tfMap = tfMap;
 	}
 
 	public String getId() {
 		return this.id;
 	}
 
 }
 
 
 class DocParser {
 
 	public static final int TOP = 20;
 
 	public static Doc parse(String docText, String title, String id) {
 		Doc doc = new Doc(id, docText);
 		String[] words = docText.toLowerCase().split("\\s+");
 		String[] titles = title.toLowerCase().split("\\s+");
 
 		HashMap<String, Double> countMap = new HashMap<String, Double>();
 		for (int i = 0; i < titles.length; i++) {
 			if (filterWord(titles[i])) {
 				if (countMap.containsKey(words[i])) {
 					// title words has higher weight than normal text
 					countMap.put(words[i], countMap.get(words[i]) + 2);
 				} else {
 					// title words has higher weight than normal text
 					countMap.put(words[i], 3.0);
 				}
 			}		
 		}
 		for (int i = 0; i < words.length; i++) {
 			if (filterWord(words[i])) {
 				if (countMap.containsKey(words[i])) {
 					countMap.put(words[i], countMap.get(words[i]) + 1);
 				} else {
 					countMap.put(words[i], 1.0);
 				}			
 			}
 		}
 
 		ValueComparator comparator = new ValueComparator(countMap);
 		TreeMap<String, Double> sortedMap = new TreeMap<String, Double>(comparator);
 		sortedMap.putAll(countMap);
 
 
 		int count = 0;
 		HashMap<String, Double> topCountMap = new HashMap<String, Double>();
 
 		for (Map.Entry<String, Double> entry: sortedMap.entrySet()) {
 			if (count >= TOP) break;
 			topCountMap.put(entry.getKey(), entry.getValue());
 			count++;
 		}
 		long squareSum = 0;
 		for (Map.Entry<String, Double> entry: topCountMap.entrySet()) {
 			squareSum += entry.getValue() * entry.getValue();			
 		}
 		squareSum = (squareSum == 0 ? 1 : squareSum);
 		double factor = Math.sqrt(1.0 / squareSum);
 		for (Map.Entry<String, Double> entry: topCountMap.entrySet()) {
 			double value = factor * entry.getValue();
 			topCountMap.put(entry.getKey(), value);
 			//System.out.println(entry.getKey() + ":" + value);
 		}
 
 
 		doc.setTfMap(topCountMap);
 
 		return doc;
 	}
 
 	private static boolean filterWord(String word) {
 		if (word.matches("[\\d\\w\'\"]+") // only use words of digits or numbers or ' or " or .
 				&& word.length() > 1 // use words length > 1
 				&& !STOPLIST.contains(word)) {
 			return true;
 		}
 		return false;
 	}
 
 
 	private final static Set<String> STOPLIST = new HashSet<String>();
 
 	static {
 		STOPLIST.add("a");
 		STOPLIST.add("a\'s");
 		STOPLIST.add("able");
 		STOPLIST.add("about");
 		STOPLIST.add("above");
 		STOPLIST.add("according");
 		STOPLIST.add("accordingly");
 		STOPLIST.add("across");
 		STOPLIST.add("actually");
 		STOPLIST.add("after");
 		STOPLIST.add("afterwards");
 		STOPLIST.add("again");
 		STOPLIST.add("against");
 		STOPLIST.add("ain\'t");
 		STOPLIST.add("all");
 		STOPLIST.add("allow");
 		STOPLIST.add("allows");
 		STOPLIST.add("almost");
 		STOPLIST.add("alone");
 		STOPLIST.add("along");
 		STOPLIST.add("already");
 		STOPLIST.add("also");
 		STOPLIST.add("although");
 		STOPLIST.add("always");
 		STOPLIST.add("am");
 		STOPLIST.add("among");
 		STOPLIST.add("amongst");
 		STOPLIST.add("an");
 		STOPLIST.add("and");
 		STOPLIST.add("another");
 		STOPLIST.add("any");
 		STOPLIST.add("anybody");
 		STOPLIST.add("anyhow");
 		STOPLIST.add("anyone");
 		STOPLIST.add("anything");
 		STOPLIST.add("anyway");
 		STOPLIST.add("anyways");
 		STOPLIST.add("anywhere");
 		STOPLIST.add("apart");
 		STOPLIST.add("appear");
 		STOPLIST.add("appreciate");
 		STOPLIST.add("appropriate");
 		STOPLIST.add("are");
 		STOPLIST.add("aren\'t");
 		STOPLIST.add("around");
 		STOPLIST.add("as");
 		STOPLIST.add("aside");
 		STOPLIST.add("ask");
 		STOPLIST.add("asking");
 		STOPLIST.add("associated");
 		STOPLIST.add("at");
 		STOPLIST.add("available");
 		STOPLIST.add("away");
 		STOPLIST.add("awfully");
 		STOPLIST.add("b");
 		STOPLIST.add("be");
 		STOPLIST.add("became");
 		STOPLIST.add("because");
 		STOPLIST.add("become");
 		STOPLIST.add("becomes");
 		STOPLIST.add("becoming");
 		STOPLIST.add("been");
 		STOPLIST.add("before");
 		STOPLIST.add("beforehand");
 		STOPLIST.add("behind");
 		STOPLIST.add("being");
 		STOPLIST.add("believe");
 		STOPLIST.add("below");
 		STOPLIST.add("beside");
 		STOPLIST.add("besides");
 		STOPLIST.add("best");
 		STOPLIST.add("better");
 		STOPLIST.add("between");
 		STOPLIST.add("beyond");
 		STOPLIST.add("both");
 		STOPLIST.add("brief");
 		STOPLIST.add("but");
 		STOPLIST.add("by");
 		STOPLIST.add("c");
 		STOPLIST.add("c\'mon");
 		STOPLIST.add("c\'s");
 		STOPLIST.add("came");
 		STOPLIST.add("can");
 		STOPLIST.add("can\'t");
 		STOPLIST.add("cannot");
 		STOPLIST.add("cant");
 		STOPLIST.add("cause");
 		STOPLIST.add("causes");
 		STOPLIST.add("certain");
 		STOPLIST.add("certainly");
 		STOPLIST.add("changes");
 		STOPLIST.add("clearly");
 		STOPLIST.add("co");
 		STOPLIST.add("com");
 		STOPLIST.add("come");
 		STOPLIST.add("comes");
 		STOPLIST.add("concerning");
 		STOPLIST.add("consequently");
 		STOPLIST.add("consider");
 		STOPLIST.add("considering");
 		STOPLIST.add("contain");
 		STOPLIST.add("containing");
 		STOPLIST.add("contains");
 		STOPLIST.add("corresponding");
 		STOPLIST.add("could");
 		STOPLIST.add("couldn\'t");
 		STOPLIST.add("course");
 		STOPLIST.add("currently");
 		STOPLIST.add("d");
 		STOPLIST.add("definitely");
 		STOPLIST.add("described");
 		STOPLIST.add("despite");
 		STOPLIST.add("did");
 		STOPLIST.add("didn\'t");
 		STOPLIST.add("different");
 		STOPLIST.add("do");
 		STOPLIST.add("does");
 		STOPLIST.add("doesn\'t");
 		STOPLIST.add("doing");
 		STOPLIST.add("don\'t");
 		STOPLIST.add("done");
 		STOPLIST.add("down");
 		STOPLIST.add("downwards");
 		STOPLIST.add("during");
 		STOPLIST.add("e");
 		STOPLIST.add("each");
 		STOPLIST.add("edu");
 		STOPLIST.add("eg");
 		STOPLIST.add("eight");
 		STOPLIST.add("either");
 		STOPLIST.add("else");
 		STOPLIST.add("elsewhere");
 		STOPLIST.add("enough");
 		STOPLIST.add("entirely");
 		STOPLIST.add("especially");
 		STOPLIST.add("et");
 		STOPLIST.add("etc");
 		STOPLIST.add("even");
 		STOPLIST.add("ever");
 		STOPLIST.add("every");
 		STOPLIST.add("everybody");
 		STOPLIST.add("everyone");
 		STOPLIST.add("everything");
 		STOPLIST.add("everywhere");
 		STOPLIST.add("ex");
 		STOPLIST.add("exactly");
 		STOPLIST.add("example");
 		STOPLIST.add("except");
 		STOPLIST.add("f");
 		STOPLIST.add("far");
 		STOPLIST.add("few");
 		STOPLIST.add("fifth");
 		STOPLIST.add("first");
 		STOPLIST.add("five");
 		STOPLIST.add("followed");
 		STOPLIST.add("following");
 		STOPLIST.add("follows");
 		STOPLIST.add("for");
 		STOPLIST.add("former");
 		STOPLIST.add("formerly");
 		STOPLIST.add("forth");
 		STOPLIST.add("four");
 		STOPLIST.add("from");
 		STOPLIST.add("further");
 		STOPLIST.add("furthermore");
 		STOPLIST.add("g");
 		STOPLIST.add("get");
 		STOPLIST.add("gets");
 		STOPLIST.add("getting");
 		STOPLIST.add("given");
 		STOPLIST.add("gives");
 		STOPLIST.add("go");
 		STOPLIST.add("goes");
 		STOPLIST.add("going");
 		STOPLIST.add("gone");
 		STOPLIST.add("got");
 		STOPLIST.add("gotten");
 		STOPLIST.add("greetings");
 		STOPLIST.add("h");
 		STOPLIST.add("had");
 		STOPLIST.add("hadn\'t");
 		STOPLIST.add("happens");
 		STOPLIST.add("hardly");
 		STOPLIST.add("has");
 		STOPLIST.add("hasn\'t");
 		STOPLIST.add("have");
 		STOPLIST.add("haven\'t");
 		STOPLIST.add("having");
 		STOPLIST.add("he");
 		STOPLIST.add("he\'s");
 		STOPLIST.add("hello");
 		STOPLIST.add("help");
 		STOPLIST.add("hence");
 		STOPLIST.add("her");
 		STOPLIST.add("here");
 		STOPLIST.add("here\'s");
 		STOPLIST.add("hereafter");
 		STOPLIST.add("hereby");
 		STOPLIST.add("herein");
 		STOPLIST.add("hereupon");
 		STOPLIST.add("hers");
 		STOPLIST.add("herself");
 		STOPLIST.add("hi");
 		STOPLIST.add("him");
 		STOPLIST.add("himself");
 		STOPLIST.add("his");
 		STOPLIST.add("hither");
 		STOPLIST.add("hopefully");
 		STOPLIST.add("how");
 		STOPLIST.add("howbeit");
 		STOPLIST.add("however");
 		STOPLIST.add("i");
 		STOPLIST.add("i\'d");
 		STOPLIST.add("i\'ll");
 		STOPLIST.add("i\'m");
 		STOPLIST.add("i\'ve");
 		STOPLIST.add("ie");
 		STOPLIST.add("if");
 		STOPLIST.add("ignored");
 		STOPLIST.add("immediate");
 		STOPLIST.add("in");
 		STOPLIST.add("inasmuch");
 		STOPLIST.add("inc");
 		STOPLIST.add("indeed");
 		STOPLIST.add("indicate");
 		STOPLIST.add("indicated");
 		STOPLIST.add("indicates");
 		STOPLIST.add("inner");
 		STOPLIST.add("insofar");
 		STOPLIST.add("instead");
 		STOPLIST.add("into");
 		STOPLIST.add("inward");
 		STOPLIST.add("is");
 		STOPLIST.add("isn\'t");
 		STOPLIST.add("it");
 		STOPLIST.add("it\'d");
 		STOPLIST.add("it\'ll");
 		STOPLIST.add("it\'s");
 		STOPLIST.add("its");
 		STOPLIST.add("itself");
 		STOPLIST.add("j");
 		STOPLIST.add("just");
 		STOPLIST.add("k");
 		STOPLIST.add("keep");
 		STOPLIST.add("keeps");
 		STOPLIST.add("kept");
 		STOPLIST.add("know");
 		STOPLIST.add("knows");
 		STOPLIST.add("known");
 		STOPLIST.add("l");
 		STOPLIST.add("last");
 		STOPLIST.add("lately");
 		STOPLIST.add("later");
 		STOPLIST.add("latter");
 		STOPLIST.add("latterly");
 		STOPLIST.add("least");
 		STOPLIST.add("less");
 		STOPLIST.add("lest");
 		STOPLIST.add("let");
 		STOPLIST.add("let\'s");
 		STOPLIST.add("like");
 		STOPLIST.add("liked");
 		STOPLIST.add("likely");
 		STOPLIST.add("little");
 		STOPLIST.add("look");
 		STOPLIST.add("looking");
 		STOPLIST.add("looks");
 		STOPLIST.add("ltd");
 		STOPLIST.add("m");
 		STOPLIST.add("mainly");
 		STOPLIST.add("many");
 		STOPLIST.add("may");
 		STOPLIST.add("maybe");
 		STOPLIST.add("me");
 		STOPLIST.add("mean");
 		STOPLIST.add("meanwhile");
 		STOPLIST.add("merely");
 		STOPLIST.add("might");
 		STOPLIST.add("more");
 		STOPLIST.add("moreover");
 		STOPLIST.add("most");
 		STOPLIST.add("mostly");
 		STOPLIST.add("much");
 		STOPLIST.add("must");
 		STOPLIST.add("my");
 		STOPLIST.add("myself");
 		STOPLIST.add("n");
 		STOPLIST.add("name");
 		STOPLIST.add("namely");
 		STOPLIST.add("nd");
 		STOPLIST.add("near");
 		STOPLIST.add("nearly");
 		STOPLIST.add("necessary");
 		STOPLIST.add("need");
 		STOPLIST.add("needs");
 		STOPLIST.add("neither");
 		STOPLIST.add("never");
 		STOPLIST.add("nevertheless");
 		STOPLIST.add("new");
 		STOPLIST.add("next");
 		STOPLIST.add("nine");
 		STOPLIST.add("no");
 		STOPLIST.add("nobody");
 		STOPLIST.add("non");
 		STOPLIST.add("none");
 		STOPLIST.add("noone");
 		STOPLIST.add("nor");
 		STOPLIST.add("normally");
 		STOPLIST.add("not");
 		STOPLIST.add("nothing");
 		STOPLIST.add("novel");
 		STOPLIST.add("now");
 		STOPLIST.add("nowhere");
 		STOPLIST.add("o");
 		STOPLIST.add("obviously");
 		STOPLIST.add("of");
 		STOPLIST.add("off");
 		STOPLIST.add("often");
 		STOPLIST.add("oh");
 		STOPLIST.add("ok");
 		STOPLIST.add("okay");
 		STOPLIST.add("old");
 		STOPLIST.add("on");
 		STOPLIST.add("once");
 		STOPLIST.add("one");
 		STOPLIST.add("ones");
 		STOPLIST.add("only");
 		STOPLIST.add("onto");
 		STOPLIST.add("or");
 		STOPLIST.add("other");
 		STOPLIST.add("others");
 		STOPLIST.add("otherwise");
 		STOPLIST.add("ought");
 		STOPLIST.add("our");
 		STOPLIST.add("ours");
 		STOPLIST.add("ourselves");
 		STOPLIST.add("out");
 		STOPLIST.add("outside");
 		STOPLIST.add("over");
 		STOPLIST.add("overall");
 		STOPLIST.add("own");
 		STOPLIST.add("p");
 		STOPLIST.add("particular");
 		STOPLIST.add("particularly");
 		STOPLIST.add("per");
 		STOPLIST.add("perhaps");
 		STOPLIST.add("placed");
 		STOPLIST.add("please");
 		STOPLIST.add("plus");
 		STOPLIST.add("possible");
 		STOPLIST.add("presumably");
 		STOPLIST.add("probably");
 		STOPLIST.add("provides");
 		STOPLIST.add("q");
 		STOPLIST.add("que");
 		STOPLIST.add("quite");
 		STOPLIST.add("qv");
 		STOPLIST.add("r");
 		STOPLIST.add("rather");
 		STOPLIST.add("rd");
 		STOPLIST.add("re");
 		STOPLIST.add("really");
 		STOPLIST.add("reasonably");
 		STOPLIST.add("regarding");
 		STOPLIST.add("regardless");
 		STOPLIST.add("regards");
 		STOPLIST.add("relatively");
 		STOPLIST.add("respectively");
 		STOPLIST.add("right");
 		STOPLIST.add("s");
 		STOPLIST.add("said");
 		STOPLIST.add("same");
 		STOPLIST.add("saw");
 		STOPLIST.add("say");
 		STOPLIST.add("saying");
 		STOPLIST.add("says");
 		STOPLIST.add("second");
 		STOPLIST.add("secondly");
 		STOPLIST.add("see");
 		STOPLIST.add("seeing");
 		STOPLIST.add("seem");
 		STOPLIST.add("seemed");
 		STOPLIST.add("seeming");
 		STOPLIST.add("seems");
 		STOPLIST.add("seen");
 		STOPLIST.add("self");
 		STOPLIST.add("selves");
 		STOPLIST.add("sensible");
 		STOPLIST.add("sent");
 		STOPLIST.add("serious");
 		STOPLIST.add("seriously");
 		STOPLIST.add("seven");
 		STOPLIST.add("several");
 		STOPLIST.add("shall");
 		STOPLIST.add("she");
 		STOPLIST.add("should");
 		STOPLIST.add("shouldn\'t");
 		STOPLIST.add("since");
 		STOPLIST.add("six");
 		STOPLIST.add("so");
 		STOPLIST.add("some");
 		STOPLIST.add("somebody");
 		STOPLIST.add("somehow");
 		STOPLIST.add("someone");
 		STOPLIST.add("something");
 		STOPLIST.add("sometime");
 		STOPLIST.add("sometimes");
 		STOPLIST.add("somewhat");
 		STOPLIST.add("somewhere");
 		STOPLIST.add("soon");
 		STOPLIST.add("sorry");
 		STOPLIST.add("specified");
 		STOPLIST.add("specify");
 		STOPLIST.add("specifying");
 		STOPLIST.add("still");
 		STOPLIST.add("sub");
 		STOPLIST.add("such");
 		STOPLIST.add("sup");
 		STOPLIST.add("sure");
 		STOPLIST.add("t");
 		STOPLIST.add("t\'s");
 		STOPLIST.add("take");
 		STOPLIST.add("taken");
 		STOPLIST.add("tell");
 		STOPLIST.add("tends");
 		STOPLIST.add("th");
 		STOPLIST.add("than");
 		STOPLIST.add("thank");
 		STOPLIST.add("thanks");
 		STOPLIST.add("thanx");
 		STOPLIST.add("that");
 		STOPLIST.add("that\'s");
 		STOPLIST.add("thats");
 		STOPLIST.add("the");
 		STOPLIST.add("their");
 		STOPLIST.add("theirs");
 		STOPLIST.add("them");
 		STOPLIST.add("themselves");
 		STOPLIST.add("then");
 		STOPLIST.add("thence");
 		STOPLIST.add("there");
 		STOPLIST.add("there\'s");
 		STOPLIST.add("thereafter");
 		STOPLIST.add("thereby");
 		STOPLIST.add("therefore");
 		STOPLIST.add("therein");
 		STOPLIST.add("theres");
 		STOPLIST.add("thereupon");
 		STOPLIST.add("these");
 		STOPLIST.add("they");
 		STOPLIST.add("they\'d");
 		STOPLIST.add("they\'ll");
 		STOPLIST.add("they\'re");
 		STOPLIST.add("they\'ve");
 		STOPLIST.add("think");
 		STOPLIST.add("third");
 		STOPLIST.add("this");
 		STOPLIST.add("thorough");
 		STOPLIST.add("thoroughly");
 		STOPLIST.add("those");
 		STOPLIST.add("though");
 		STOPLIST.add("three");
 		STOPLIST.add("through");
 		STOPLIST.add("throughout");
 		STOPLIST.add("thru");
 		STOPLIST.add("thus");
 		STOPLIST.add("to");
 		STOPLIST.add("together");
 		STOPLIST.add("too");
 		STOPLIST.add("took");
 		STOPLIST.add("toward");
 		STOPLIST.add("towards");
 		STOPLIST.add("tried");
 		STOPLIST.add("tries");
 		STOPLIST.add("truly");
 		STOPLIST.add("try");
 		STOPLIST.add("trying");
 		STOPLIST.add("twice");
 		STOPLIST.add("two");
 		STOPLIST.add("u");
 		STOPLIST.add("un");
 		STOPLIST.add("under");
 		STOPLIST.add("unfortunately");
 		STOPLIST.add("unless");
 		STOPLIST.add("unlikely");
 		STOPLIST.add("until");
 		STOPLIST.add("unto");
 		STOPLIST.add("up");
 		STOPLIST.add("upon");
 		STOPLIST.add("us");
 		STOPLIST.add("use");
 		STOPLIST.add("used");
 		STOPLIST.add("useful");
 		STOPLIST.add("uses");
 		STOPLIST.add("using");
 		STOPLIST.add("usually");
 		STOPLIST.add("uucp");
 		STOPLIST.add("v");
 		STOPLIST.add("value");
 		STOPLIST.add("various");
 		STOPLIST.add("very");
 		STOPLIST.add("via");
 		STOPLIST.add("viz");
 		STOPLIST.add("vs");
 		STOPLIST.add("w");
 		STOPLIST.add("want");
 		STOPLIST.add("wants");
 		STOPLIST.add("was");
 		STOPLIST.add("wasn\'t");
 		STOPLIST.add("way");
 		STOPLIST.add("we");
 		STOPLIST.add("we\'d");
 		STOPLIST.add("we\'ll");
 		STOPLIST.add("we\'re");
 		STOPLIST.add("we\'ve");
 		STOPLIST.add("welcome");
 		STOPLIST.add("well");
 		STOPLIST.add("went");
 		STOPLIST.add("were");
 		STOPLIST.add("weren\'t");
 		STOPLIST.add("what");
 		STOPLIST.add("what\'s");
 		STOPLIST.add("whatever");
 		STOPLIST.add("when");
 		STOPLIST.add("whence");
 		STOPLIST.add("whenever");
 		STOPLIST.add("where");
 		STOPLIST.add("where\'s");
 		STOPLIST.add("whereafter");
 		STOPLIST.add("whereas");
 		STOPLIST.add("whereby");
 		STOPLIST.add("wherein");
 		STOPLIST.add("whereupon");
 		STOPLIST.add("wherever");
 		STOPLIST.add("whether");
 		STOPLIST.add("which");
 		STOPLIST.add("while");
 		STOPLIST.add("whither");
 		STOPLIST.add("who");
 		STOPLIST.add("who\'s");
 		STOPLIST.add("whoever");
 		STOPLIST.add("whole");
 		STOPLIST.add("whom");
 		STOPLIST.add("whose");
 		STOPLIST.add("why");
 		STOPLIST.add("will");
 		STOPLIST.add("willing");
 		STOPLIST.add("wish");
 		STOPLIST.add("with");
 		STOPLIST.add("within");
 		STOPLIST.add("without");
 		STOPLIST.add("won\'t");
 		STOPLIST.add("wonder");
 		STOPLIST.add("would");
 		STOPLIST.add("would");
 		STOPLIST.add("wouldn\'t");
 		STOPLIST.add("x");
 		STOPLIST.add("y");
 		STOPLIST.add("yes");
 		STOPLIST.add("yet");
 		STOPLIST.add("you");
 		STOPLIST.add("you\'d");
 		STOPLIST.add("you\'ll");
 		STOPLIST.add("you\'re");
 		STOPLIST.add("you\'ve");
 		STOPLIST.add("your");
 		STOPLIST.add("yours");
 		STOPLIST.add("yourself");
 		STOPLIST.add("yourselves");
 		STOPLIST.add("z");
 		STOPLIST.add("zero");
 
 	}
 
 
 }
 
 class ValueComparator implements Comparator<String> {
 
 	HashMap<String, Double> map;
 
 	ValueComparator(HashMap<String, Double> map) {
 		this.map = map;
 	}
 
 	// decending order
 	@Override
 	public int compare(String s1, String s2) {
 		if (map.get(s1) >= map.get(s2)) {
 			return -1;
 		} else {
 			return 1;
 		}
 
 	}	
 
 }
 
