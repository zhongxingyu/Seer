 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.IndexWriterConfig.OpenMode;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 import org.jsoup.Jsoup;
 
 
 public class Crawler {
 	private LinkedList<String> URLList= new LinkedList<String>();
 	private HashMap<String, Date> VisitedUrls = new HashMap<String, Date>();
 	private String indexPath;
 	private int depth;
 	private boolean indexing;
 	private boolean first_time;
 	
 	public Crawler() {
 		//default values
 		depth = -1;
 		indexing = false;
 		first_time = true;
 	}
 	
 	public void addURL(String url)
 	{
 		URLList.add(url);
 	}
 	
 	public void printVisitedUrls()
 	{
 		for (String url : VisitedUrls.keySet())
 		{
 			System.out.println(url);
 		}
 	}
 	
 	public void enableIndexing(String indexPath) {
 		this.indexing = true;
 		this.indexPath = indexPath;
 	}
 	
 	public void setMaxDepth(int depth) {
 		this.depth = depth;
 	}
 	
 	public void start()
 	{
 		int counter = 0;
 		while (!URLList.isEmpty())
 		{
 			String url = URLList.removeFirst();
 			
 			// check if URL was visited
 			if (!VisitedUrls.containsKey(url))
 			{
 				if(depth != -1 && counter >= depth) {
 					break;
 				}
 				// debug
 				System.out.println("Visiting " + url);
 				
 				String content = downloadURL(url);
 				
 				VisitedUrls.put(url, new Date()); // record that the url was visited, at the current time
 				
 				// TODO do something with content ...
 				if(indexing){
 					try {
 						indexUrlAndContent(url, content);
 					} catch(IOException e) {
 						e.printStackTrace();
 					}
 				}
 				
 				List<String> links = extractURLs(content);
 				
 				for (String link : links)
 				{
 					URLList.add(link);	
 				}
 				
 				counter++;
 				
 			}
 			else
 			{
 				System.out.println("Already visited " + url);
 			}
 		}
 	}
 	
 	public String normalize(String content) {
 		String norm_content = content;
 		
 		/**Start of normalization**/
 		//filter html tags
 		norm_content = Jsoup.parse(norm_content).text();
 		
 		//stemming
 		//...
 		
 		return norm_content;
 	}
 	
 	public void indexUrlAndContent(String url, String content) throws IOException{
 		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
 		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
 		
 		Directory dir = FSDirectory.open(new File(indexPath));
 		
 		if(first_time) {
 			iwc.setOpenMode(OpenMode.CREATE);
 			first_time = false;
 		} else
 			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
 		
 		IndexWriter writer = new IndexWriter(dir,iwc);
 		
 		System.out.println("Indexing to directory '" + indexPath + "'...");
 		
 		Document doc = new Document();
 		Field url_field = new StringField("url", url, Field.Store.YES);
 		doc.add(url_field);
 		
		doc.add(new TextField("title", new StringReader(extractTitle(content))));
 		
 		doc.add(new TextField("content", new StringReader(normalize(content))));
 		
 		writer.addDocument(doc);
 		writer.close();
 	}
 	
 	public String extractTitle(String content) {
 		return Jsoup.parse(content).title();
 	}
 	
 	public List<String> extractURLs(String content)
 	{
 		int curIndex = 0;
 		ArrayList<String> urls = new ArrayList<String>();
 		while (true)
 		{
 			curIndex = content.indexOf("<a", curIndex);
 			if (curIndex == -1)
 				break; // No more links in this document
 			
 			// find the end tag
 			int endTagIndex = content.indexOf(">", curIndex);
 			if (endTagIndex == -1)
 				break; // End tag not found, invalid HTML
 			
 			String element = content.substring(curIndex, endTagIndex);
 			// find href attribute
 			Pattern pattern = Pattern.compile("href=\"([^\"]+)\"");
 			Matcher matcher = pattern.matcher(element);
 			if (matcher.find())
 			{
 				// url is group 1 of the regex match
 				String href = matcher.group(1);
 				
 				// add url to the list
 				if (href != null)
 				{
 					// TODO pre-process url: e.g. add domain if missing?
 					urls.add(href);
 				}
 			}
 			
 			
 			curIndex = endTagIndex;
 		}
 		
 		return urls;
 	}
 	
 	public String downloadURL(String url) 
 	{
 		URL website;
 		try {
 			website = new URL(url);
 			InputStream urlStream;
 			try {
 				urlStream = website.openStream();
 				java.util.Scanner s = new java.util.Scanner(urlStream).useDelimiter("\\A");
 			    return s.hasNext() ? s.next() : "";
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 		    
 	}
 	
 	public static void main(String[] args) {
 		Crawler crawly = new Crawler();
 		
 		//default page
 		String webpage = "https://www.udacity.com/cs101x/index.html";
 		
 		for(int i = 0; i < args.length; i++) {
 			if(args[i].equals("-index") && i <args.length ) {
 				crawly.enableIndexing(args[i+1]);
 				i++;
 			} else if(args[i].equals("-page") && i <args.length ) {
 				webpage = args[i+1];
 				i++;
 			} else if(args[i].equals("-depth") && i <args.length ) {
 				try {
 					crawly.setMaxDepth(Integer.parseInt(args[i+1]));
 					i++;
 				} catch (NumberFormatException e) {
 					System.out.println("Please use a real number as depth");
 					System.exit(1);
 				}
 			}
 		}
 		
 		crawly.addURL(webpage);
 		crawly.start();
 		crawly.printVisitedUrls();
 		
 	}
 }
