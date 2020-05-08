 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 // A Topic is the large concept of a story,
 // it is a collection of relevant keywords,
 // links to the relevant articles and the
 // dates these articles were picked up
 public class Topic {
 	
 	private HashMap<String, WordInfo> words;
 	private LinkedList<Article> articles;
 	private int numWords;
 	private Date timestamp;
 	private String recentTitle;
 	private int uid;
 	
 	
 	// Constructor, begins list with first article
 	public Topic(HashMap<String, WordInfo> words, Article article) {
 		this.words = words;
 		this.articles = new LinkedList<Article>();
 		this.articles.push(article);
 		this.numWords = 1;
 		this.timestamp = new Date();
 		this.recentTitle = article.getTitle();
 		this.uid = -1;
 	}
 
 	public Topic(Article article, int uid) {
 		this.articles = new LinkedList<Article>();
 		this.articles.push(article);
 		this.numWords = 0;
 		this.timestamp = new Date();
 		this.recentTitle = article.getTitle();
 		this.uid = uid;
 	}
 
 	// Add article only if new
 	public void addArticle(Article article) {
 	    Iterator<Article> iterator = articles.iterator();
 	    boolean repeat = false;
 	    while (iterator.hasNext()) {
 	       if (article.getURL().compareTo(iterator.next().getURL()) == 0) {
 	    	   repeat = true;
 	    	   System.out.println("Repeat URL");
 	    	   break;
 	       }
 	    }
 	    if (!repeat)
 	    	articles.push(article);
 	    timestamp = new Date();
 	    recentTitle = article.getTitle();
 	}
 
 	public void addWord(String s, Double d, Double sent) {
 		if (numWords == 0)
 			words = new HashMap<String, WordInfo>();
 		if (words.containsKey(s)) {
 			WordInfo currentInfo = words.get(s);
 			currentInfo.setRel((currentInfo.getRel() + d) / 2.0);
 			currentInfo.setSent((currentInfo.getSent() + sent) / 2.0);
 			words.remove(s);
 			words.put(s, currentInfo);
 		}
 		else
 			words.put(s, new WordInfo(d, sent));
 		numWords++;
 	}
 
 	public boolean containsWord(String s) {
 		if (words.containsKey(s))
 			return true;
 		else
 			return false;
 	}
 	
 	// Displays all the keyword information
 	public void printWordData() {
 	    Iterator<String> iterator = words.keySet().iterator();
 	    while (iterator.hasNext()) {  
 	       String key = iterator.next().toString();  
 	       String value = words.get(key).getRel().toString(); 
 	       String sent = words.get(key).getSent().toString(); 
 	       System.out.println(key + " " + value + " " + sent);  
 	    }  
 	}
 	
 	public void printTopWords() {
 		System.out.print(this.topWords());
 	}
 	
 	// Displays top 5 words
 	public String topWords() {
 		final class WordAndVal implements Comparable<WordAndVal> {
 			private final String _word;
 			private final Double _count;
 			
 			public WordAndVal(String word, double count) {
 				this._word = word;
 				this._count = count;
 			}
 			
 			public Double getCount() {
 				return _count;
 			}
 			
 			public String getWord() {
 				return _word;
 			}
 			
 			public int compareTo(WordAndVal wav) {
 				return this._count.compareTo(wav.getCount());
 			}
 		};
 	    Iterator<String> iterator = words.keySet().iterator();
 	    ArrayList<WordAndVal> wavl = new ArrayList<WordAndVal>();
 	    while (iterator.hasNext()) {  
 	       String key = iterator.next();  
 	       Double val = words.get(key).getRel();
 	       wavl.add(new WordAndVal(key, val));
 	    }  
 	    Collections.sort(wavl);
 	    int j = 5;
 	    if (wavl.size() < 5)
 	    	j = wavl.size();
 	    String rString = "";
 	    for (int i = 1; i <= j; i++) {
 	    	WordAndVal nextWord = wavl.get(wavl.size() - i);
 	    	rString = rString + nextWord.getWord() + "@" + words.get(nextWord.getWord()).getSent() + ";\n";
 	    }
 	    return rString;
 	}
 	
 	public void printTopLinks() {
 		System.out.println(this.topLinks());
 	}
 	
 	// Displays last 3 links
 	public String topLinks() {
 		String rString = "";
 		int j = articles.size();
 		if (articles.size() < 3)
 			j = articles.size();
 		for (int i = 1; i <= j; i++)
 			rString = rString + articles.get(articles.size() - i).getURL() + ";\n";
 		return rString;
 	}
 	
 	// Return the number of articles received in the last hour
 	public int artsLastHour() {
 		int result = 0;
 		Date hourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
		for (int sizeof = articles.size() - 1; sizeof >= 0; sizeof--) {
 			if (articles.get(sizeof).getDate().compareTo(hourAgo) < 0) {
 				// More than an hour old
 				break;
 			}
 			result++;
 		}
 		return result;
 	}
 
 	public LinkedList<Article> getArticles() {
 		return articles;
 	}
 
 	public String getDate() {
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		return dateFormat.format(timestamp);
 	}
 	
 	public String getRecentTitle() {
 		return recentTitle;
 	}
 	
 	public Date getTimestamp() {
 		return timestamp;
 	}
 
 	public int getUid() {
 		return uid;
 	}
 
 	public HashMap<String, WordInfo> getWords() {
 		return words;
 	}
 	
 }
