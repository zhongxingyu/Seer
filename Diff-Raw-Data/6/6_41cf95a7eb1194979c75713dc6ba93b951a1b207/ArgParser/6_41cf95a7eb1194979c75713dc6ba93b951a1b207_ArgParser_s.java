 import be.ugent.twijug.jclops.annotations.*;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Pattern;
 
 /**
  * A class to parse the command line arguments for the RSSReader. 
  * 
  * Derived from code by
  * @author Amy Csizmar Dalal
  * CS 204, Spring 2013
  * date: 9 April 2013
  */
 
 @AtMostOneOf({"by-date", "by-alpha"})
 public class ArgParser {
 	// These instance variables are derived from the command line arguments passed in.
 	
 	/** Flag to display story links in order of date posted */
 	private boolean byDate;
 	
 	/** Flag to display story links with feeds ordered alphabetically */
 	private boolean byAlpha;
 	
 	/** Flag to display only stories posted or updated since the last time the feed parser was run */
 	private boolean newest;
 	
 	/** Flag to also display the description associated with a story */
 	private boolean description;
 	
 	/** The number of stories per feed to list */
 	private int number;
 
 	/** Display only story links published on or after this date */
 	private Date since;
 
 	/** A regex to find matching titles */
 	private Pattern title;
 
 	/** The file containing the list of feeds */
 	private String filename;
 
 	/**
 	 * The constructor sets default values for the instance variables, in case these are
 	 * not set by command line options.
 	 */
 	public ArgParser() {
 		byDate = false;
 		byAlpha = false;
 		newest = false;
 		description = false;
 		number = 2;
 		since = new Date(0);
 		title = null;
 		filename = null;
 	}
 
 	/**
 	 * Retrieves the current value of the "sort by date" flag.
 	 * @return true if we should sort the posts by date, false otherwise
 	 */
 	public boolean isByDate() {
 		return byDate;
 	}
 
 	/**
 	 * Turn on the "sort by date" flag.
 	 * Automatically turns off "sort by alpha" flag.
 	 */
 	@Option(description="sort by date")
 	public void setByDate() {
 		this.byDate = true;
 		this.byAlpha = false;
 	}
 
 	/**
 	 * Retrieves the current value of the "sort by alpha" flag.
 	 * @return true if we should sort the posts alphabetically, false otherwise
 	 */
 	public boolean isByAlpha() {
 		return byAlpha;
 	}
 
 	/**
 	 * Turn on the "sort by alpha" flag.
 	 * Automatically turns off the "sort by date" flag.
 	 */
 	@Option(description="sort by alpha")
 	public void setByAlpha() {
 		this.byAlpha = true;
 		this.byDate = false;
 	}
 
 	/**
 	 * Retrieves the current value of the "show newest" flag.
 	 * @return true if we should only display the stories posted or updated since the last time the feed parser was run, false otherwise
 	 */
 	public boolean isNewest() {
 		return newest;
 	}
 
 	/**
 	 * Turn on the "show newest" flag.
 	 */
 	@Option(description="show newest")
 	public void setNewest() {
 		this.newest = true;
 	}
 
 	/**
 	 * Retrieves the current value of the "show description" flag.
 	 * @return true if we should display the post's description, false otherwise
 	 */
 	public boolean isDescription() {
 		return description;
 	}
 
 	/**
 	 * Turn on the "show description" flag.
 	 */
 	@Option(description="show description")
 	public void setDescription() {
 		this.description = true;
 	}
 
 	/**
 	 * Returns the number of posts per feed to display.
 	 * @return the number of posts to display
 	 */
 	public int getNumber() {
 		return number;
 	}
 
 	/**
 	 * Sets the number of posts per feed to display
 	 * @param number the number of posts
 	 */
 	@Option(description="<N> the number of posts per feed to display")
 	public void setNumber(int number) {
 		this.number = number;
 	}
 
 	/**
 	 * Returns the date for which to display posts published on or after.
 	 * @return the date for which to display posts published on or after
 	 */
 	public Date getSince() {
 		return since;
 	}
 
 	/**
 	 * Sets the date for which to display posts published on or after
 	 * @param since the date for which to display posts published on or after
 	 */
 	@Option(description="<S> the date for which to display posts published on or after")
 	public void setSince(String since) {
		DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
 		try {
 			this.since = df.parse(since);
 		} catch (ParseException e) {
			e.printStackTrace();
 		}
 	}
 
 		/**
 	 * Returns the REGEX with which to match feed titles.
 	 * @return the REGEX for title matching
 	 */
 	public Pattern getTitle() {
 		return title;
 	}
 
 	/**
 	 * Sets the REGEX with which to match feed titles.
 	 * @param title the string to be parsed into a REGEX for title matching
 	 */
 	@Option(description="<S> the regex with which to match feed titles")
 	public void setTitle(String title) {
 		this.title = Pattern.compile(title);
 	}
 
 	/**
 	 * Sets the name of the input feed file
 	 * @param filename the input file name containing the feeds
 	 */
 	public void setFilename(String filename) {
 		this.filename = filename;
 	}
 	
 	/**
 	 * Returns the filename containing the feeds
 	 * @return the filename containing the feeds
 	 */
 	public String getFilename() {
 		return filename;
 	}
 
 }
