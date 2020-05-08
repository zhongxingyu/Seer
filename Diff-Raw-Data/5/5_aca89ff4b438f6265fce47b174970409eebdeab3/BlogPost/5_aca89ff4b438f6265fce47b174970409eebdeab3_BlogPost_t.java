 package de.mactunes.schmitzkatz.blog.posts;
 
 import java.util.Date;
 import java.util.Calendar;
 import java.io.File;
 import java.text.SimpleDateFormat;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONException;
 import net.sf.json.JSONSerializer;
 import com.petebevin.markdown.MarkdownProcessor;
 
 public class BlogPost implements Comparable<BlogPost> {
 	public static final String POSTS_PATH = "blog" + File.separator + "Posts";
 	public static final String GENERATED_PATH = "blog" + File.separator + "Generated";
 	public static final String TEMPLATES_PATH = "blog" + File.separator + "Templates";
 	public static final String MEDIA_DIR = "media";
 	public static final String POSTS_DIR = "posts";
 	public static final String BLOG_POST_HTML_FILENAME = "index.html"; // TODO get from props
 	private static final String SLASH = "/";
 
 	private static final String SERIALIZED_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
 	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(SERIALIZED_DATE_FORMAT);
 
 	public static final String		JSON_KEY_DATE = "date",
 									JSON_KEY_TITLE = "title",
 									JSON_KEY_SUMMARY = "summary",
 									JSON_KEY_TEXT = "text";
 	private static final int INDENT_SPACES = 4;
 
 
 	private String title, summary, markdownText, htmlText;
 	private Date date;
 	private String identifier;
 
 
 	public BlogPost(String title, String summary, Date date, String pathToDirectory) {
 		this.title = title;
 		this.summary = summary;
 		this.date = date;
 		this.identifier = identifier;
 	}
 
 	public BlogPost(String strJSON, String strMarkdown, String identifier) {
 		this.identifier = identifier;
 		this.markdownText = strMarkdown;
 
 		try {
 			JSONObject objJSON = (JSONObject) JSONSerializer.toJSON(strJSON);
 			if (objJSON.has(JSON_KEY_TITLE)) {
 				this.title = objJSON.getString(JSON_KEY_TITLE);
 			}
 			if (objJSON.has(JSON_KEY_DATE)) {
 				try {
 					this.date = FORMATTER.parse(objJSON.getString(JSON_KEY_DATE));
 				} catch (Exception ex) {
 					System.out.println("Error: failed parsing date for post: " + title);
 				}
 			}
 			if (objJSON.has(JSON_KEY_SUMMARY)) {
 				this.summary = objJSON.getString(JSON_KEY_SUMMARY);
 			}
 		} catch (JSONException je) {
 			System.out.println("Error: could not parse some element for blog post: " + title);
 		}
 	}
 
 	public String getJSONRepresentation() {
 		JSONObject jsonObj = new JSONObject();
 
 		jsonObj.put(JSON_KEY_TITLE, title);
 		jsonObj.put(JSON_KEY_DATE, FORMATTER.format(date).toString());
 		jsonObj.put(JSON_KEY_SUMMARY, summary);
 		jsonObj.put(JSON_KEY_TEXT, markdownText);
 
 		return jsonObj.toString(INDENT_SPACES);
 	}
 
 
 
 	public String getTitle() {
 		return title;
 	}
 
 	public String getSummary() {
 		return summary;
 	}
 
 	public String getTextAsMarkdown() {
 		return markdownText;
 	}
 
 	public String getTextAsHTML() {
 		if (null == markdownText) {
 			return null;
 		}
 
 		// don't invoke markdown processor every time. 
 		// cache the results and return them when called multiple times
 		if (null != htmlText) {
 			return htmlText;
 		}
 
 		MarkdownProcessor processor = new MarkdownProcessor();
 		return processor.markdown(markdownText);
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	public String getPostDir() {
 		return POSTS_PATH + File.separator + identifier;
 	}
 
 	public String getPostMediaDir() {
 		return getPostDir() + File.separator + MEDIA_DIR;
 	}	
 
 	public String getParentDirInGeneratedDir() {
 		return GENERATED_PATH + getRelativeGeneratedPostsPath();
 	}
 
 	private String getRelativeGeneratedPostsPath() {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 
 		return File.separator + POSTS_DIR +
 					File.separator + cal.get(Calendar.YEAR) + 
					File.separator + (cal.get(Calendar.MONTH) + 1) +
 					File.separator + cal.get(Calendar.DAY_OF_MONTH) ;
 	}
 
 	public String getPostDirInGeneratedDir() {
 		return getParentDirInGeneratedDir() + 
 					File.separator + 
 					getIdentifier();
 	}
 
 	public String getLink() {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 
 		return POSTS_DIR + SLASH + 
 					cal.get(Calendar.YEAR) + SLASH +
					(cal.get(Calendar.MONTH) + 1) + SLASH +
 					cal.get(Calendar.DAY_OF_MONTH) + SLASH +
 					getIdentifier() + SLASH + 
 					BLOG_POST_HTML_FILENAME;
 	}
 
 	public String getPostMediaDirInGeneratedDir() {
 		return getParentDirInGeneratedDir() + 
 					File.separator + 
 					getIdentifier() +
 					File.separator +
 					MEDIA_DIR;
 	}
 
 	@Override
 	public int compareTo(BlogPost otherBlogPost) {
 		return date.compareTo(otherBlogPost.getDate());
 	}
 }
