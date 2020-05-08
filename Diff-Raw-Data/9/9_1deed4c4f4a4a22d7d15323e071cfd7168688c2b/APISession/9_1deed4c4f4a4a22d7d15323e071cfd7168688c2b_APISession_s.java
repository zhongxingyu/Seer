 package nl.vertinode.facepunch;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class APISession
 {
 	// State
 	private String username, bb_password;
 	private int bb_userid = -1;
 	
 	public String securityToken, lastPost, lastThread, postHash, postStartTime;
 	
 	public boolean loggedIn() { return bb_userid != -1; }
 	public int userId() { return bb_userid; }	
 	public String username() { return username;	}
 	
 	/**
 	 * The user ranks on Facepunch.
 	 * 
 	 * @author Overv
 	 */
 	public enum Rank {
 		BANNED, REGULAR, GOLD, MODERATOR
 	}
 	
 	/**
 	 * The available post ratings.
 	 * 
 	 * @author Overv
 	 */
 	public enum Rating
 	{		
 		AGREE( "Agree" ), DISAGREE( "Disagree" ), FUNNY( "Funny" ), WINNER( "Winner" ), ZING( "Zing" ), INFORMATIVE( "Informative" ), FRIENDLY( "Friendly" ),
 		USEFUL( "Useful" ), OPTIMISTIC( "Optimistic" ), ARTISTIC( "Artistic" ), LATE( "Late" ), DUMB( "Dumb" ), PROGRAMMINGKING( "Programming King" ),
 		MAPPINGKING( "Mapping King" ), LUAKING( "Lua King" ), LUAHELPER( "Lua Helper" ), SMARKED( "Smarked" ), MOUSTACHE( "Moustache" );
 		
 		String ratingName;
 		
 		Rating( String name )
 		{
 			ratingName = name;
 		}
 		
 		@Override
 		public String toString()
 		{
 			return ratingName;
 		}
 		
 		public static Rating fromString( String htmlName )
 		{
 			for( Rating r : values() )
 				if( r.name().equalsIgnoreCase( htmlName ) )
 					return r;
 			return null;
 		}
 	}
 	
 	/**
 	 * Class that holds information about a specific user on Facepunch.
 	 * The actual amount of available information depends on the function that returned the structure.
 	 * 
 	 * @author Overv
 	 */
 	public class FPUser
 	{
 		private FPUser() {}
 		
 		private int id;
 		private String name;
 		public Rank rank = Rank.REGULAR;
 		private boolean online;
 		
 		private int joinYear;
 		private String joinMonth;
 		private int postCount;
 		
 		public int getId() { return id; }
 		public String getName() { return name; }
 		public Rank getRank() { return rank; }
 		public boolean isOnline() { return online; }
 		
 		public int getJoinYear() { return joinYear; }
 		public String getJoinMonth() { return joinMonth; }
 		public int getPostCount() { return postCount; }
 	}
 	
 	/**
 	 * Class that holds information about a specific thread on Facepunch.
 	 * 
 	 * @author Overv
 	 */
 	public class FPThread
 	{
 		private FPThread() {}
 		
 		private int id;
 		private String icon, title;
 		private FPUser author;
 		private boolean read, locked, sticky;
 		private int readers;
 		private int pages;
 		private int posts, views, newPosts;
 		
 		public int getId() { return id; }
 		public String getIcon() { return icon; }
 		public String getTitle() { return title; }
 		public FPUser getAuthor() { return author; }
 		
 		public boolean isRead() { return read; }
 		public boolean isLocked() { return locked; }
 		public boolean isSticky() { return sticky; }
 		
 		public int readerCount() { return readers; }
 		public int pageCount() { return pages; }
 		public int postCount() { return posts; }
 		public int viewCount() { return views; }
 		public int unreadPostCount() { return newPosts; }
 	}
 	
 	/**
 	 * Class that holds information about a specific post on Facepunch.
 	 * 
 	 * @author Overv
 	 */
 	public class FPPost
 	{
 		private FPPost() {}
 		
 		private int id;
 		private String date;
 		private FPUser author;
 		
 		private String message;
 		
 		private String os, browser, flagdog;
 		
 		private HashMap<Rating, Integer> ratings = new HashMap<Rating, Integer>();
 		private HashMap<Rating, RateData> ratingCodes = new HashMap<Rating, RateData>();
 		
 		public int getId() { return id; }
 		public String getDate() { return date; }
 		public FPUser getAuthor() { return author; }
 		
 		public String getMessageHTML() { return message; }
 		
 		public String getOperatingSystem() { return os; }
 		public String getBrowser() { return browser; }
 		
 		public int getRatingCount( Rating rating ) { return ratings.containsKey( rating ) ? ratings.get( rating ) : 0; }
 	}
 	
 	/**
 	 * Classes for managing frontpage forums
 	 * 
 	 * @author Overv
 	 */
 	public class Forum
 	{
 		private Forum() {}
 		
 		private int id;
 		private String name;
 		
 		public int getId() { return id; }
 		public String getName() { return name; }
 	}
 	
 	public class Category
 	{
 		private Category() {}
 		
 		private int id;
 		private String name;
 		
 		private ArrayList<Forum> forums = new ArrayList<Forum>();
 		
 		public int getId() { return id; }
 		public String getName() { return name; }
 		public Forum[] getForums()
 		{
 			Forum[] arr = new Forum[forums.size()];
 			forums.toArray( arr );
 			return arr;
 		}
 	}
 	
 	// Authentication
 	public interface LoginCallback
 	{
 		public void onResult( boolean success );
 	}
 	
 	/**
 	 * Sign in as the specified user within this session.
 	 * 
 	 * @author Overv
 	 * 
 	 * @param username Username to sign in with.
 	 * @param password Associated passsword.
 	 * @param callback Function to pass the result to.
 	 */
 	public void login( String username, String password, final LoginCallback callback )
 	{
 		this.username = username;
 		String pwdHash = MD5( password );
 		
 		asyncWebRequest( "login.php?do=login", "do=login&cookieuser=1&vb_login_username=" + username + "&vb_login_md5password=" + pwdHash, new WebRequestCallback()
 		{
 			public void onResult( String source, String cookies )
 			{
 				if ( source != null && cookies.contains( "bb_password" ) )
 				{
 					bb_userid = Integer.parseInt( quickMatch( "bb_userid=([0-9]+)", cookies ) );
 					bb_password = quickMatch( "bb_password=([a-zA-Z0-9]+)", cookies );
 					
 					callback.onResult( true );
 				} else {
 					callback.onResult( false );
 				}
 			}
 		} );
 	}
 	
 	// Forum listing
 	public interface ForumCallback
 	{
 		public void onResult( boolean success, Category[] categories );
 	}
 	
 	/**
 	 * Retrieve the categories and forums visible on the frontpage.
 	 * 
 	 * @author Overv
 	 * 
 	 * @param callback Function to pass the thread list to.
 	 */
 	public void listForums( final ForumCallback callback )
 	{
 		asyncWebRequest( "", null, new WebRequestCallback()
 		{
 			public void onResult( String source, String cookies )
 			{
 				if ( source != null )
 				{
 					// Fetch relevant part of the page
					source = quickMatch( "(<td valign=top class=\"FrontPageForums\">[\\s\\S]*<center>)", source );
 					source = source.replaceAll( "(valign|width|height|color)=([a-z0-9%]+)", "$1=\"$2\"" );
 					source = source.replaceAll( "(<img.*?\")>", "$1 />" );
 					source = source.replaceAll( "(<tbody[^>]+>)", "$1<tr>" );
 					source = source.replace( "&nbsp;", " " );
 					source = source.replaceAll( "&#[0-9]+;", "" );
 					source = source.replaceAll( "<!--.*?-->", "" );
 					source = source.replaceAll( ">[\\s]*?<", "><" );
 					source = source.substring( 0, source.length() - ( "</td></tr></table><center>" ).length() );
 					
 					XmlPullParserFactory factory;
 					try
 					{
 						factory = XmlPullParserFactory.newInstance();
 						XmlPullParser parser = factory.newPullParser();
 						parser.setInput( new StringReader( source ) );
 						
 						ArrayList<Category> categories = new ArrayList<Category>();
 						Category currentCategory = null;
 						
 						int eventType = parser.getEventType();
 						while ( eventType != XmlPullParser.END_DOCUMENT )
 						{
 							if ( eventType == XmlPullParser.START_TAG )
 							{
 								if ( parser.getAttributeValue( null, "class" ) != null )
 								{
 									// Categories
 									if( parser.getName().equals( "tr" ) && parser.getAttributeValue( null, "class" ).contains( "forumhead" ) )
 									{
 										currentCategory = new Category();
 										categories.add( currentCategory );
 										
 										parser.next();
 										parser.next();
 										parser.next();
 										currentCategory.id = Integer.parseInt( quickMatch( "([0-9]+)", parser.getAttributeValue( null, "href" ) ) );
 										parser.next();
 										currentCategory.name = parser.getText();
 									} else
 									
 									// Forums
 									if ( parser.getName().equals( "h2" ) && parser.getAttributeValue( null, "class" ).equals( "forumtitle" ) )
 									{
 										Forum forum = new Forum();
 										currentCategory.forums.add( forum );
 										
 										parser.next();
 										forum.id = Integer.parseInt( quickMatch( "([0-9]+)", parser.getAttributeValue( null, "href" ) ) );
 										parser.next();
 										forum.name = parser.getText();
 									}
 								}
 							}
 							
 							eventType = parser.next();
 						}
 						
 						Category[] categoryArray = new Category[categories.size()];
 						categories.toArray( categoryArray );
 						callback.onResult( true, categoryArray );
 					} catch ( XmlPullParserException e )
 					{
 						Log.e( "XMLError", e.toString() );
 						callback.onResult( false, null );
 					} catch ( IOException e )
 					{
 						Log.e( "DEB", "It's the apocalypse!" );
 						callback.onResult( false, null );
 					}
 				} else {
 					callback.onResult( false, null );
 				}
 			}
 		} );
 	}
 	
 	// Thread listing
 	public interface ThreadCallback
 	{
 		public void onResult( boolean success, FPThread[] threads );
 	}
 	
 	/**
 	 * Retrieve the threads in the specified subforum.
 	 * 
 	 * @author Overv
 	 * 
 	 * @param forumId Forum identifier.
 	 * @param page Page of thread list.
 	 * @param callback Function to pass the thread list to.
 	 */
 	public void listThreads( int forumId, int page, final ThreadCallback callback )
 	{
 		asyncWebRequest( "forums/" + forumId + "/" + page, null, new WebRequestCallback()
 		{
 			public void onResult( String source, String cookies )
 			{
 				if ( source != null && source.contains( "<div id=\"threadlist\" class=\"threadlist\">" ) )
 				{
 					// Fetch relevant part of the page
 					source = quickMatch( "(<tbody[\\s\\S]*tbody>)", source );
 					source = source.replaceAll( "(<img.*?\")>", "$1 />" );
 					source = source.replaceAll( "<tr class=\"threadbit deleted\"[\\s\\S]*?</tr>", "" );
 					source = source.replace( "&nbsp;", " " );
 					source = source.replaceAll( "&#[0-9]+;", "" );
 					source = source.replaceAll( "<!--.*?-->", "" );
 					source = source.replaceAll( ">[\\s]*?<", "><" );
 					
 					XmlPullParserFactory factory;
 					try
 					{
 						factory = XmlPullParserFactory.newInstance();
 						XmlPullParser parser = factory.newPullParser();
 						parser.setInput( new StringReader( source ) );
 						
 						ArrayList<FPThread> threads = new ArrayList<FPThread>();
 						FPThread currentThread = null;
 						
 						int eventType = parser.getEventType();
 						while ( eventType != XmlPullParser.END_DOCUMENT )
 						{
 							if ( eventType == XmlPullParser.START_TAG )
 							{
 								if ( parser.getAttributeValue( null, "class" ) != null )
 								{
 									// Start of thread
 									if ( parser.getName().equals( "tr" ) )
 									{
 										currentThread = new FPThread();
 										threads.add( currentThread );
 										
 										currentThread.pages = 1;
 										
 										String attributes = parser.getAttributeValue( null, "class" );
 										currentThread.read = attributes.contains( "old" );
 										currentThread.locked = attributes.contains( "lock" );
 										currentThread.sticky = !attributes.contains( "nonsticky" );
 									} else
 									
 									// Title and attributes
 									if ( parser.getName().equals( "h3" ) && parser.getAttributeValue( null, "class" ).equals( "threadtitle" ) )
 									{
 										parser.next();
 										currentThread.id = Integer.parseInt( quickMatch( "([0-9]+)$", parser.getAttributeValue( null, "id" ) ) );									
 										parser.next();
 										currentThread.title = parser.getText();
 									} else
 									
 									// Icon
 									if ( parser.getName().equals( "td" ) && parser.getAttributeValue( null, "class" ).contains( "threadicon" ) )
 									{
 										parser.next();
 										currentThread.icon = parser.getAttributeValue( null, "alt" ).toLowerCase();
 									} else
 									
 									// Author
 									if ( parser.getName().equals( "div" ) && parser.getAttributeValue( null, "class" ).equals( "author" ) )
 									{
 										parser.next();
 										currentThread.author = new FPUser();
 										currentThread.author.id = Integer.parseInt( quickMatch( "([0-9]+)", parser.getAttributeValue( null, "href" ) ) );
 										parser.next();
 										currentThread.author.name = parser.getText();
 									} else
 									
 									// Viewers
 									if ( parser.getName().equals( "span" ) && parser.getAttributeValue( null, "class" ).equals( "viewers" ) )
 									{
 										parser.next();
 										currentThread.readers = Integer.parseInt( quickMatch( "([0-9]+)", parser.getText() ) );
 									} else
 									
 									// New posts
 									if ( parser.getName().equals( "div" ) && parser.getAttributeValue( null, "class" ).equals( "newposts" ) )
 									{
 										parser.next();
 										parser.next();
 										parser.next();
 										parser.next();
 										currentThread.newPosts = Integer.parseInt( quickMatch( "([0-9]+)", parser.getText() ) );
 									} else
 									
 									// Thread replies
 									if ( parser.getName().equals( "td" ) && parser.getAttributeValue( null, "class" ).equals( "threadreplies" ) )
 									{
 										parser.next();
 										parser.next();
 										currentThread.posts = Integer.parseInt( parser.getText().replace( ",", "" ) );
 									} else
 									
 									// Thread views
 									if ( parser.getName().equals( "td" ) && parser.getAttributeValue( null, "class" ).startsWith( "threadviews" ) )
 									{
 										parser.next();
 										parser.next();
 										currentThread.views = Integer.parseInt( parser.getText().replace( ",", "" ) );
 									}
 								} else {
 									// Pages
 									if ( parser.getName().equals( "a" ) && parser.getAttributeValue( null, "href" ).startsWith( "threads/" ) )	
 									{
 										parser.next();
 										currentThread.pages = Integer.parseInt( quickMatch( "([0-9]+)", parser.getText() ) );
 									}
 								}
 							}
 							
 							eventType = parser.next();
 						}
 						
 						FPThread[] threadArray = new FPThread[threads.size()];
 						threads.toArray( threadArray );
 						callback.onResult( true, threadArray );
 					} catch ( XmlPullParserException e )
 					{
 						Log.e( "XMLError", e.toString() );
 						callback.onResult( false, null );
 					} catch ( IOException e )
 					{
 						Log.e( "DEB", "It's the apocalypse!" );
 						callback.onResult( false, null );
 					}
 				} else {
 					callback.onResult( false, null );
 				}
 			}
 		} );
 	}
 	
 	// Post listing
 	public interface PostCallback
 	{
 		public void onResult( boolean success, FPPost[] posts );
 	}
 	
 	/**
 	 * Retrieve the posts in a thread on the specified page.
 	 * 
 	 * @author Overv
 	 * 
 	 * @param threadId Thread identifier.
 	 * @param page Thread page.
 	 * @param callback The function to pass the post list to.
 	 */
 	public void listPosts( final int threadId, int page, final PostCallback callback )
 	{
 		asyncWebRequest( "threads/" + threadId + "/" + page, null, new WebRequestCallback()
 		{
 			public void onResult( String source, String cookies )
 			{
 				if ( source != null && source.contains( "<ol id=\"posts\" class=\"posts\"" ) )
 				{
 					// Store state info used for replying
 					lastPost = quickMatch( "ajax_last_post = ([0-9]+)", source );
 					lastThread = String.valueOf( threadId );
 					postHash = quickMatch( "\"posthash\": \"([^\"]+)\"", source );
 					postStartTime = quickMatch( "\"poststarttime\": \"([0-9]+)\"", source );
 					
 					source = quickMatch( "(<ol id=\"posts\" class=\"posts\"[\\s\\S]*?</ol>)", source );
 					source = source.replaceAll( ">[\\s]*?<", "><" );
 					source = source.replaceAll( "<a([^/>]+)/>", "<a$1>" );
 					source = source.replaceAll( "(border|width|height|size)=([0-9]+)", "$1=\"$2\"" );
 					source = source.replaceAll( "(allowfullscreen|controls|loop|muted|autoplay)( |>)", "$1=\"1\"$2" );
 					source = source.replaceAll( "&([a-z]+=)", "&amp;$1" );
 					source = source.replaceAll( "(<img[^>]+[\"'])>", "$1 />" );
 					source = source.replaceAll( "<span class=\"usertitle\">[^<]*<span[\\s\\S]*?</span>[^<]*</span>", "" );
 					source = source.replaceAll( "<span class=\"usertitle\">[\\s\\S]*?</span>", "" );
 					source = source.replaceAll( "<li class=\"postbitdeleted[\\s\\S]*?</li>", "" );
 					source = source.replaceAll( "([a-z]\")([a-z])", "$1 $2" );
 					
 					// DEBUG
 					
 					source = source.replace( "&nbsp;", " " );
 					source = source.replaceAll( "&#[0-9]+;", "" );
 					source = source.replaceAll( "<!--.*?-->", "" );
 					source = source.replaceAll( ">[\\s]*?<", "><" );
 					
 					XmlPullParserFactory factory;
 					try
 					{
 						factory = XmlPullParserFactory.newInstance();
 						XmlPullParser parser = factory.newPullParser();
 						parser.setInput( new StringReader( source ) );
 						
 						//FPPost[] posts = new FPPost[40];
 						//int pid = 0;
 						ArrayList<FPPost> posts = new ArrayList<FPPost>();
 						FPPost currentPost = null;
 						
 						int eventType = parser.getEventType();
 						while ( eventType != XmlPullParser.END_DOCUMENT )
 						{
 							if ( eventType == XmlPullParser.START_TAG )
 							{
 								if ( parser.getAttributeValue( null, "class" ) != null )
 								{
 									// Start of post
 									if ( parser.getName().equals( "li" ) )
 									{
 										currentPost = new FPPost();
 										posts.add( currentPost );
 										
 										currentPost.id = Integer.parseInt( quickMatch( "([0-9]+)", parser.getAttributeValue( null, "id" ) ) );
 									} else
 									
 									// Time indication
 									if ( parser.getName().equals( "span" ) && parser.getAttributeValue( null, "class" ).equals( "date" ) )
 									{
 										parser.next();
 										currentPost.date = parser.getText();
 									} else
 									
 									// Author
 									if ( parser.getName().equals( "a" ) && parser.getAttributeValue( null, "class" ).startsWith( "username" ) )
 									{
 										currentPost.author = new FPUser();
 										currentPost.author.id = Integer.parseInt( quickMatch( "([0-9]+)", parser.getAttributeValue( null, "href" ) ) );
 										currentPost.author.name = quickMatch( "(.+) is (offline|invisible|online now)$", parser.getAttributeValue( null, "title" ) );
 										currentPost.author.online = quickMatch( ".+ is (offline|invisible|online now)$", parser.getAttributeValue( null, "title" ) ).equals( "online" );
 										
 										parser.next();
 										
 										if ( parser.getName() == null )
 											currentPost.author.rank = Rank.REGULAR;
 										else if ( parser.getName().equals( "font" ) )
 											currentPost.author.rank = Rank.BANNED;
 										else if ( parser.getName().equals( "strong" ) )
 											currentPost.author.rank = Rank.GOLD;
 										else if ( parser.getName().equals( "span" ) )
 											currentPost.author.rank = Rank.MODERATOR;
 									} else
 									
 									// Post contents
 									if ( parser.getName().equals( "blockquote" ) && parser.getAttributeValue( null, "class" ).contains( "postcontent" ) )
 									{
 										currentPost.message = quickMatch( "<div id=\"post_message_" + currentPost.id + "\"><blockquote[^<]+>([\\s\\S]*?)</blockquote>", source ).trim();
 									} else
 									
 									// Extra info (os, browser, flagdog)
 									if ( parser.getName().equals( "span" ) && parser.getAttributeValue( null, "class" ).contains( "postlinking" ) )
 									{
 										parser.next();
 										if ( parser.getName() != null && parser.getName().equals( "img" ) ) currentPost.os = parser.getAttributeValue( null, "alt" );
 										parser.next();
 										parser.next();
 										if ( parser.getName() != null && parser.getName().equals( "img" ) ) currentPost.browser = quickMatch( "/([^/]+)\\.(gif|png)", parser.getAttributeValue( null, "src" ) );
 										parser.next();
 										parser.next();
 										if ( parser.getName() != null && parser.getName().equals( "a" ) ) currentPost.flagdog = quickMatch( "ipe=([a-z0-9]+)&", parser.getAttributeValue( null, "href" ) );
 									} else
 									
 									// Rating results
 									if ( parser.getName().equals( "span" ) && parser.getAttributeValue( null, "class" ).equals( "rating_results" ) )
 									{
 										eventType = parser.next();
 										if ( eventType != XmlPullParser.END_TAG || !parser.getName().equals( "span" ) )
 										{
 											while ( !parser.getName().equals( "a" ) )
 											{
 												parser.next();
 												String rating = parser.getAttributeValue( null, "alt" ).toLowerCase().replace( " ", "" );
 												parser.next();
 												parser.next();
 												parser.next();
 												parser.next();
 												int count = Integer.parseInt( parser.getText() );
 												currentPost.ratings.put( Rating.fromString( rating ), count );
 												parser.next();
 												parser.next();
 												parser.next();
 											}
 										}
 									} else
 									
 									// Rating codes
 									if ( parser.getName().equals( "div" ) && parser.getAttributeValue( null, "class" ).equals( "postrating" ) )
 									{
 										parser.next();
 										
 										while ( !parser.getName().equals( "div" ) && parser.getAttributeValue( null, "onclick" ) != null )
 										{
 											RateData rate = new RateData();
 											rate.id = Integer.parseInt( quickMatch( ", '([0-9]+)',", parser.getAttributeValue( null, "onclick" ) ) );
 											rate.code = quickMatch( ", '([a-z0-9]+)' \\)", parser.getAttributeValue( null, "onclick" ) );
 											parser.next();
 											String name = parser.getAttributeValue( null, "alt" ).toLowerCase().replace( " ", "" );
 											currentPost.ratingCodes.put( Rating.fromString( name ), rate );
 											parser.next();
 											parser.next();
 											parser.next();
 										}
 									}
 								} else {
 									// Author stats
 									if ( parser.getAttributeValue( null, "id" ) != null && parser.getAttributeValue( null, "id" ).equals( "userstats" ) )
 									{
 										parser.next();
 										currentPost.author.joinYear = Integer.parseInt( quickMatch( "([0-9]+)", parser.getText() ) );
 										currentPost.author.joinMonth = quickMatch( "^([A-Za-z]+)", parser.getText() );
 										parser.next();
 										parser.next();
 										parser.next();
 										currentPost.author.postCount = Integer.parseInt( quickMatch( "([0-9]+)", parser.getText().replace( ",", "" ) ) );
 									}
 								}
 							}
 							
 							eventType = parser.next();
 						}
 						
 						FPPost[] postArray = new FPPost[posts.size()];
 						posts.toArray( postArray );
 						callback.onResult( true, postArray );
 					} catch ( XmlPullParserException e )
 					{
 						Log.e( "XMLError", e.toString() );
 						callback.onResult( false, null );
 					} catch ( IOException e )
 					{
 						Log.e( "DEB", "It's the apocalypse!" );
 						callback.onResult( false, null );
 					}
 				} else {
 					callback.onResult( false, null );
 				}
 			}
 		} );
 	}
 	
 	// Rating
 	public interface RateCallback
 	{
 		public void onResult( boolean success );
 	}
 	
 	/**
 	 * Rate a post on the last retrieved thread page.
 	 * 
 	 * @author Overv
 	 * 
 	 * @param post Post to rate.
 	 * @param rating Rating to rate.
 	 * @param callback The function to pass the result to.
 	 */
 	public void rate( FPPost post, Rating rating, final RateCallback callback )
 	{
 		if ( !post.ratingCodes.containsKey( rating ) )
 		{
 			callback.onResult( false );
 			return;
 		}
 		
 		asyncWebRequest( "ajax.php", "do=rate_post&postid=" + post.id + "&rating=" + post.ratingCodes.get( rating ).id + "&key=" + post.ratingCodes.get( rating ).code + "&securitytoken=" + securityToken, new WebRequestCallback()
 		{
 			public void onResult( String source, String cookies )
 			{
 				callback.onResult( source.startsWith( "{\"message\"" ) );
 			}
 		} );
 	}
 	
 	private class RateData
 	{
 		public int id;
 		public String code;
 	}
 	
 	// Reply to latest retrieved thread
 	public interface ReplyCallback
 	{
 		public void onResult( boolean success );
 	}
 	
 	/**
 	 * Reply to the last retrieved thread.
 	 * 
 	 * @author Overv
 	 * 
 	 * @param message Message to reply with.
 	 * @param callback The function to pass the result to.
 	 */
 	public void reply( String message, final ReplyCallback callback )
 	{
 		if ( postHash == null )
 		{
 			callback.onResult( false );
 			return;
 		}
 		
 		asyncWebRequest( "newreply.php?do=postreply&t=" + lastThread,
 				"securitytoken=" + securityToken + "&ajax=1&ajax_lastpost=" + lastPost + "&message=" + URLEncoder.encode( message ) + "&wysiwyg=0&fromquickreply=1" +
 				"&s=&do=postreply&t=" + lastThread + "&p=who%20cares&specifiedpost=0&parseurl=1&loggedinuser=" + bb_userid + "&posthash=" + postHash + "&poststarttime=" + postStartTime,
 				new WebRequestCallback()
 		{
 			public void onResult( String source, String cookies )
 			{
 				callback.onResult( source.contains( "<postbits>" ) );
 			}
 		} );
 	}
 	
 	// Retrieve avatar
 	public interface AvatarCallback
 	{
 		public void onResult( boolean success, Bitmap avatar );
 	}
 	
 	/**
 	 * Retrieve the avatar image of the specified user.
 	 * 
 	 * @author Overv
 	 * 
 	 * @param userId User identifier.
 	 * @param callback The function to pass the avatar image to.
 	 */
 	public void getAvatar( int userId, final AvatarCallback callback )
 	{
 		asyncImageRequest( "http://www.facepunch.com/avatar/" + userId + ".png", new ImageRequestCallback()
 		{
 			public void onResult( Bitmap image )
 			{
 				if ( image == null )
 					callback.onResult( false, null );
 				else
 					callback.onResult( true, image );
 			}
 		} );
 	}
 	
 	// Retrieve flag icon
 	public interface FlagCallback
 	{
 		public void onResult( boolean success, Bitmap flag );
 	}
 	
 	/**
 	 * Retrieve the flag associated with the specified flagdog code.
 	 * 
 	 * @author Overv
 	 * 
 	 * @param post The post to retrieve the flag from.
 	 * @param callback The function to pass the flag image to.
 	 */
 	public void getFlag( FPPost post, final FlagCallback callback )
 	{
 		asyncImageRequest( "http://flagdog.facepunchstudios.com/?ipe=" + post.flagdog, new ImageRequestCallback()
 		{
 			public void onResult( Bitmap image )
 			{
 				if ( image == null )
 					callback.onResult( false, null );
 				else
 					callback.onResult( true, image );
 			}
 		} );
 	}
 	
 	// Calculate MD5 hash
 	private String MD5( String str )
 	{
 		MessageDigest md;
 		try
 		{
 			md = MessageDigest.getInstance( "MD5" );
 			md.reset();
 			md.update( str.getBytes( "UTF-8" ) );
 			byte[] digest = md.digest();
 			
 			BigInteger bigInt = new BigInteger( 1, digest );
 			String hashtext = bigInt.toString( 16 );
 			
 			return hashtext;
 		}
 		catch ( NoSuchAlgorithmException e ) { return null; }
 		catch ( UnsupportedEncodingException e ) { return null; }		
 	}
 	
 	// Quick single regex matcher
 	private String quickMatch( String regex, String target )
 	{
 		Matcher m = Pattern.compile( regex ).matcher( target );
 		if ( m.find() )
 			return m.group( 1 );
 		else
 			return null;
 	}
 	
 	// Retrieve image from URL
 	private interface ImageRequestCallback
 	{
 		public void onResult( Bitmap image );
 	}
 	
 	private void asyncImageRequest( String url, ImageRequestCallback callback )
 	{
 		new ImageRequest().execute( url, callback );
 	}
 	
 	private class ImageRequest extends AsyncTask<Object, Void, Object[]>
 	{
 		protected Object[] doInBackground( Object... params )
 		{
 			Bitmap img = null;
 			
 			try
 			{
 				URL url = new URL( (String)params[0] );
 				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 				conn.setInstanceFollowRedirects( true );
 				conn.setDoInput( true );
 				conn.connect();
 				
 				img = BitmapFactory.decodeStream( conn.getInputStream() );
 			} catch ( IOException e ) {}
 			
 			return new Object[]{ img, params[1] };
 		}
 		
 		protected void onPostExecute( Object[] params )
 		{
 			ImageRequestCallback callback = (ImageRequestCallback)params[1];
 			
 			if ( params[0] == null )
 			{
 				callback.onResult( null );
 				return;
 			}
 			
 			callback.onResult( (Bitmap)params[0] );
 		}
 	}
 	
 	// Perform Facepunch web request
 	private interface WebRequestCallback
 	{
 		public void onResult( String response, String cookies );
 	}
 	
 	private void asyncWebRequest( String url, String postBody, WebRequestCallback callback )
 	{
 		new WebRequest().execute( url, postBody, callback );
 	}
 	
 	private class WebRequest extends AsyncTask<Object, Void, Object[]>
 	{
 		protected Object[] doInBackground( Object... params )
 		{			
 			String request = "http://www.facepunch.com/" + (String)params[0];
 			String postBody = (String)params[1];
 			WebRequestCallback callback = (WebRequestCallback)params[2];
 			
 			StringBuilder response = new StringBuilder();
 			String cookies = "";
 			
 			try
 			{
 				URL url = new URL( request );
 				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 				conn.setInstanceFollowRedirects( true );
 				
 				// Send login cookie there are active login details
 				if ( bb_userid != -1 )
 					conn.setRequestProperty( "Cookie", "bb_userid=" + bb_userid + "; bb_password=" + bb_password );
 				
 				// Send POST body
 				if ( postBody != null )
 				{
 					conn.setDoOutput( true );
 					OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream() );
 					out.write( postBody );
 					out.close();
 				}
 				
 				// Get any cookies
 				String headerName = null;
 				for ( int i = 1; ( headerName = conn.getHeaderFieldKey( i ) ) != null; i++ )
 				{
 					if ( headerName.toLowerCase().equals( "set-cookie" ) )
 					{
 						String cookie = conn.getHeaderField( i ).split( ";" )[0];
 						cookies += cookie + "; ";
 					}
 				}
 				if ( cookies.length() > 0 )
 					cookies = cookies.substring( 0, cookies.length() - 2 );
 				
 				// Get response text
 				InputStreamReader reader = new InputStreamReader( conn.getInputStream() );
 				char[] buffer = new char[10*1024];
 				int n = 0;
 				while ( n >= 0 )
 				{
 					n = reader.read( buffer, 0, buffer.length );
 					if ( n > 0 ) response.append( buffer, 0, n );
 				}
 				reader.close();
 				
 				return new Object[] { callback, response.toString(), cookies };
 			} catch ( IOException e )
 			{
 				return new Object[] { callback };
 			}
 		}
 		
 		protected void onPostExecute( Object[] params )
 		{
 			if ( params.length == 1 )
 			{
 				( (WebRequestCallback)params[0] ).onResult( null, null );
 				return;
 			}
 			
 			WebRequestCallback callback = (WebRequestCallback)params[0];
 			String response = (String)params[1];
 			String cookies = (String)params[2];
 			
 			if ( response.contains( "SECURITYTOKEN" ) ) securityToken = quickMatch( "SECURITYTOKEN = \"([^\"]*)\"", response );
 			
 			callback.onResult( response, cookies );
 		}
 	}
 }
