 import java.util.ArrayList;
 import java.util.Comparator;
 
 
 public class Publication implements Comparable<Publication> {
 	
 	/**
 	 * author(s)
 	 */
 	private ArrayList<String> authors;
 	
 	/**
 	 * title of this paper
 	 */
 	private String titlePaper;
 	
 	/**
 	 * serial title
 	 */
 	private String titleSerial;
 	
 	/**
 	 * starting page
 	 */
 	private String pageStart;
 	
 	/**
 	 * ending page
 	 */
 	private String pageEnd;
 	
 	/**
 	 * month the publication was published
 	 */
 	private String month;
 	
 	/**
 	 * year the publication was published
 	 */
 	private String year;
 	
 	/**
 	 * hyperlink to publication (optional)
 	 */
 	private String link;
 	
 	/**
 	 * Publication constructor without link
 	 * 
 	 * @param authors list of authors
 	 * @param titlePaper paper title
 	 * @param titleSerial serial title
 	 * @param pageStart starting page
 	 * @param pageEnd ending page
 	 * @param month month of publication
 	 * @param year year of publication
 	 */
 	public Publication(ArrayList<String> authors, String titlePaper, String titleSerial, 
 			String pageStart, String pageEnd, String month, String year){
 		
 		this.authors = new ArrayList<String>();
 		
 		for(String author : authors) {
 			addAuthor(author);
 		}
 		
 		setTitlePaper(titlePaper);
 		
 		setTitleSerial(titleSerial);
 		
 		setPageStart(pageStart);
 		
 		setPageEnd(pageEnd);
 		
 		setDate(month, year);
 	}
 	
 	/**
 	 * Publication constructor with link
 	 * 
 	 * @param authors list of authors
 	 * @param titlePaper paper title
 	 * @param titleSerial serial title
 	 * @param pageStart starting page
 	 * @param pageEnd ending page
 	 * @param month month of publication
 	 * @param year year of publication
 	 * @param link hyperlink to publication
 	 */
 	public Publication(ArrayList<String> authors, String titlePaper, String titleSerial, 
 			String pageStart, String pageEnd, String month, String year, String link){
 		
 		this.authors = new ArrayList<String>();
 		
 		for(String author : authors) {
 			if(author != null)
 				addAuthor(author);
 		}
 		
 		setTitlePaper(titlePaper);
 		
 		setTitleSerial(titleSerial);
 		
 		setPageStart(pageStart);
 		
 		setPageEnd(pageEnd);
 		
 		setDate(month, year);
 		
 		setLink(link);
 	}
 
 	/**
 	 * checks if str can be cast into an int
 	 * from http://stackoverflow.com/questions/1102891/how-to-check-a-string-is-a-numeric-type-in-java
 	 * 
 	 * @param str string to check
 	 * @return true if numeric, false if not
 	 */
 	public static boolean isNumeric(String str)
 	{
 	    return str.matches("[+-]?\\d*(\\.\\d+)?");
 	}
 	
 	/**
 	 * checks if string is invalid or nonexistent
 	 * @param prop string to check
 	 * @return true of valid, false if not
 	 */
 	public static boolean isValid(String prop) {
 		if(prop.length() > 0)
 			return true;
 		return false;
 	}
 	
 	/**
 	 * checks if int is invalid or nonexistent
 	 * @param prop int to check
 	 * @return true of valid, false if not
 	 */
 	public static boolean isValid(int prop) {
 		if(prop > 0)
 			return true;
 		return false;
 	}
 	
 	/**
 	 * checks if list is invalid or nonexistent
 	 * @param prop list to check
 	 * @return true of valid, false if not
 	 */
 	public static boolean isValid(ArrayList<String> prop) {
 		if(prop != null && !prop.isEmpty() && prop.get(0) != null)
 			return true;
 		return false;
 	}
 	
 	@Override
 	public int compareTo(Publication o) {
 		return this.getTitlePaper().compareTo(o.getTitlePaper());
 	}
 	
 	public String toString() {
 		String out="";
 		
 		out += "\nPublication";
 		
 		out += "\n\tAuthors = ";
 		
 		for(String author : getAuthors())
 		{
 			out += author + "; ";
 		}
 		
 		out += "\n\tPaper Title = " + getTitlePaper();
 		
 		out += "\n\tSerial Title = " + getTitleSerial();
 		
 		out += "\n\tStarting Page = " + getPageStart();
 		
 		out += "\n\tEnding Page = " + getPageEnd();
 		
 		out += "\n\tTime of Publication = " + getDate();
 		
 		out += "\n\tHyperlink = " + getLink();
 		
 		return out;
 	}
 	
 	/**
 	 *comparator to sort by first author
 	 */
	private class PubCompAuthor implements Comparator<Publication>{
 		@Override
 		public int compare(Publication a, Publication b) {
 			if(a.getAuthors() != null && a.getAuthors().size() > 0 && 
 					b.getAuthors() != null && b.getAuthors().size() > 0)
 				return a.getAuthors().get(0).compareTo(b.getAuthors().get(0));
 			else
 				return 0;
 		}
 	}
 	
 	/**
 	 *comparator to sort by paper title
 	 */
	private class PubCompTitlePaper implements Comparator<Publication>{
 		@Override
 		public int compare(Publication a, Publication b) {
 			if(a.getTitlePaper() != null && a.getTitlePaper().length() > 0 && 
 					b.getTitlePaper() != null && b.getTitlePaper().length() > 0 )
 				return a.getTitlePaper().compareTo(b.getTitlePaper());
 			else
 				return 0;
 		}
 	}
 	
 	public ArrayList<String> getAuthors() {
 		if(isValid(authors))
 			return authors;
 		return new ArrayList<String>();
 	}
 	
 	public String getAuthorsString() {
 		String out = "";
 		
 		for(String author : getAuthors())
 		{
 			out += author + "; ";
 		}
 		
 		return out;
 	}
 	
 	
 	public boolean addAuthor(String author) {
 		return authors.add(author);
 	}
 	
 	public boolean removeAuthor(String author) {
 		return authors.remove(author);
 	}
 	
 	public String getTitleSerial() {
 		if(isValid(titleSerial))
 			return titleSerial;
 		return "Invalid Serial Title";
 	}
 	
 	public boolean setTitleSerial(String titleSerial) {
 		this.titleSerial = titleSerial;
 		return false;
 	}
 	
 	public String getTitlePaper() {
 		return titlePaper;
 	}
 	
 	public boolean setTitlePaper(String titlePaper) {
 		this.titlePaper = titlePaper;
 		return true;
 	}
 	
 	public String getPageStart() {
 		return pageStart;
 	}
 	
 	public boolean setPageStart(String pageStart) {
 		this.pageStart = pageStart;
 		return true;
 	}
 	
 	public String getPageEnd() {
 		return pageEnd;
 	}
 	
 	public boolean setPageEnd(String pageEnd) {
 		this.pageEnd = pageEnd;
 		return true;
 	}
 	
 	public String getDate() {
 		return month + year;
 	}
 	
 	public boolean setDate(String month, String year) {
 		this.month = month;
 		this.year = year;
 		return true;
 	}
 	
 	public String getLink() {
 		return link;
 	}
 	
 	public boolean setLink(String link) {
 		this.link = link;
 		return true;
 	}
 }
