 package tumblrinterface;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Iterator;
 
 import org.apache.commons.cli.*;
 
 import tumblib.Post;
 import tumblstats.PostStatistics;
 
 /**
  * The CommandLine interface class contains the main method, and parses
  * the user's input.  It utilizes Apache Common's CLI library to define the
  * options on the command line that should be considered.  In addition,
  * the user may pass a tumblr.com subdomain, or the URL of a tumblr blog to
  * query for posts.  If no arguments are given at the command line, the program
  * will print a usage message.   
  * 
  * @author Yeison Rodriguez
  */
 public class CommandLineInterface {
 //This class is long and ugly due to many of the conditions that must be
 //accounted for based on user input.
 	public static void main(String[] args){
 		Options options = new Options();
 		BasicParser parser = new BasicParser();
 
 		/* The second parameter is a boolean that specifies whether the option 
 		 * requires an argument or not.*/
 		options.addOption("h", "help", false, "Prints this usage message.");
 		options.addOption("st", "start", true, "The post offset to start from. " +
 				"The default is 0.");
 		options.addOption("n", "num", true, "The number of posts to query. " +
 				"The default is 20.  \"-num all\" may be specified, in which " +
 				"case all of the posts of a tumblr subdomain will be queried.");
 		options.addOption("t", "type", true, "The type of posts to query. If " +
 				"unspecified or empty, all types of posts are queried. Must be" +
 				" one of text, quote, photo, link, chat, video, or audio.");
 		options.addOption("i", "id", true, "A specific post ID to query. Use " +
 				"instead of start, num, or type.");
 		options.addOption("f", "filter", true, "Alternate filter to run on the " +
 				"text content. Allowed values:" +
 				"\n\t* text - Plain text only. No HTML."+
 				"\n\t* none - No post-processing. Output exactly what the " +
 				"author entered. (Note: Some authors write in Markdown, which " +
 				"will not be converted to HTML when this option is used.)");
 		options.addOption("tag", "tagged", true, "Query posts with this tag " +
 				"in reverse-chronological order (newest first).");
 		options.addOption("s", "search", true, "Search for posts containing this" +
 				" argument.");
 		options.addOption("u", "url", true, "The url of a tumblr blog. Useful " +
 				"if the blog is not a tumblr subdomain.  Cannot be used in " +
 				"conjunction with subdomains.  If num is used, it must be less " +
 				"than or equal to 50, and cannot be \"all\"");
 		options.addOption("c", "content", false, "If given, the content of all" +
 				" posts retrieved will be displayed.");
 		options.addOption("stats", false, "If given, tag and hour statistics " +
 				"will be displayed as well.");
 
 
 		//Create a commandline object.  We can parse input from this object.
 		CommandLine cl = null;
 		try {
 			//Pass the options we will consider.
 			cl = parser.parse(options, args);
 		}catch(MissingArgumentException e){
 			printHelp(cl, options);
 			System.exit(1);
 		}catch(ParseException e) {
 			e.printStackTrace();
 		}
 
 		//If no arguments, print help/usage
 		if(args.length == 0){
 			printHelp(cl, options);
 			System.exit(0);
 		}
 
 		ArrayList<Post> postList = new ArrayList<Post>();
 		try{
 			if(cl.hasOption("h") || cl.hasOption("help")){
 				printHelp(cl, options);
 				System.exit(0);
 			}
 			else{
 				//The cl.iterator returns an iterator over possible options.
 				Iterator<Option> optionIterator = cl.iterator();
 				//Linked list will consist of options and arguments.
 				LinkedList<String> optionValues = new LinkedList<String>();
 				URL url = null;
 				//Transfer the option arguments to the linked list.
 				if(cl.hasOption("u") || cl.hasOption("url")){
 					try {
 						url = new URL(cl.getOptionValue("url"));
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					}
 				}
 				//start is the post number to begin with, 0 being the newest.
 				int start = 0;
 				if(cl.hasOption("st") || cl.hasOption("start"))
 					start = new Integer(cl.getOptionValue("start"));
 				//num is the number of posts to retrieve from tumblr.
 				int num = 0;
 				if(cl.hasOption ("n") || cl.hasOption("num")){
 					if(cl.getOptionValue("num").compareToIgnoreCase("all") == 0)
 						num = -1;
 					else
 						num = new Integer(cl.getOptionValue("num"));
 				}
 				while(optionIterator.hasNext()){
 					Option option = optionIterator.next();
 					String argument = option.getValue();
 					String optString = option.getLongOpt();
 					if(argument != null && optString != "url" 
 						&& optString != "start"){
 						//Linked list will alternate between option and arg.
 						if( ( num < 0 || num > 50) & (optString == "num"))
 							;
 						else{
 							optionValues.add(optString);
 							optionValues.add(argument);
 						}
 					}
 				}
 				
 				//Join posts from different subdomains into one list.
 				String[] subdomains = {};
 				if(url == null)
 					subdomains = cl.getArgs();
 				for(int i = 0; i < subdomains.length; i++){
 					//Query a range of posts from start (default = 0) to num.
 					System.out.println("Querying " + subdomains[i] + 
 							".tumblr.com");
 					postList.addAll(queryRange(start, num, optionValues, subdomains[i]));
 					System.out.println("\nFinished query of subdomain: " + 
 							subdomains[i] +"\n");
 				}
 				if(subdomains.length == 0){
 					if(url != null){
 						postList = new TumblrQuery(url, optionValues).getPosts();
 					}
 					else
 						postList.addAll(queryRange(start, num, optionValues, "newsweek"));
 				}
 				
 			}
 		}catch(NullPointerException e){
 			e.printStackTrace();
 			printHelp(cl, options);
 			System.exit(1);
 		}
 		
 		//Create a PostStatistics class to print summaries.
 		PostStatistics stats = new PostStatistics(postList);
 		if(cl.hasOption("c") || cl.hasOption("content"))
 			stats.setAllPosts(true);
 		if(cl.hasOption("stats"))
 			stats.setAllStats(true);
 		stats.printSummary();
 
 	}
 
 	//This method prints the usage info for the program.
 	static void printHelp(CommandLine cl, Options options){
 		HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp("jtumblr [OPTION <ARGUMENT>]... [TUMBLR SUB-DOMAINS]..." +
 				"\nThe subdomains may be that of any existing tumblr blog " +
 				"located at http://<sub-domain>.tumblr.com.  If none are " +
 				"provided, newsweek is used by default.", options);
 	}
 	
 	static ArrayList<Post> queryRange(int start, int num, 
 						LinkedList<String>optionValues, String subdomain){
 		TumblrQuery tQuery;
 		ArrayList<Post> postList = new ArrayList<Post>(); 
 		LinkedList<String> tempOptionValues = new LinkedList<String>();
 		//In case num is supposed to be all (all posts).
 		if(num == -1){
 			tQuery = new TumblrQuery(subdomain, tempOptionValues);
 			num = new Integer(tQuery.totalPosts);
 			Post.resetCount();
 		}
 		//Tumblr only allows a query on 50 posts at a time.  Lets change
 		//that.
 		for(int i = 0 ; i < num/50; i++){
 			tempOptionValues.add("num");
 			tempOptionValues.add("50");
 			//Shift the start of the range to be queried by 50.
 			int rangeStart = i*50 + start;
 			tempOptionValues.add("start");
 			tempOptionValues.add(new Integer(rangeStart).toString());
 			tempOptionValues.addAll(optionValues);
 			tQuery = new TumblrQuery(subdomain, tempOptionValues);
 			//Join the new range with the previous range.
 			System.out.println("Retrieved post range: " + (i*50 + start) + "-" 
 					+ (i*50-1 + start + 50));
 			postList = tQuery.joinPosts(postList);
 		}
 		
 		//Account for the remainder posts if num is not evenly divided by 50.
 		if(num > 50 && num%50 != 0){
 			int remainder = num%50;
 			int quotient = num/50;
 			tempOptionValues.add("num");
 			tempOptionValues.add(new Integer(remainder).toString());
 			tempOptionValues.add("start");
 			tempOptionValues.add(new Integer(quotient*50).toString());
 			tempOptionValues.addAll(optionValues);
 			postList = 
 				new TumblrQuery(subdomain, tempOptionValues).joinPosts(postList);
 			System.out.println("Retrieved post range: " + (quotient*50) 
 					+ "-" + (quotient*50 + remainder-1));
 		}
		else
			return new TumblrQuery(subdomain, optionValues).getPosts();
 		
 		return postList;
 	}
 
 }	
