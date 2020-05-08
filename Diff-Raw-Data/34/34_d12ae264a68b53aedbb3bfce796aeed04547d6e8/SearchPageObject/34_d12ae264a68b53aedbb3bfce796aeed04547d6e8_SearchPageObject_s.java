 import java.sql.*;
 import java.util.regex.*;
 import java.util.Vector;
 
 public class SearchPageObject implements Comparable<SearchPageObject>
 {
 	private String pid;
 	private Date createDate;
 	private String title;
 	private String content;
 	private String creator;
 	private int ranking;
 
 	/**
 	 * SearchPageObject constructor.
 	 *
 	 * Param:
 	 * pid - the page id from the db.
 	 * createDate - the date the page was created.
 	 * title - the title of the page.
 	 * content - the content of the page.
 	 * creator - the creator of the page.
 	 *
 	 * jnguyen1 20100307
 	 */
 	public SearchPageObject(String pid, Date createDate, String title,
 			String content, String creator)
 	{
 		this.pid = pid;
 		this.createDate = createDate;
 		this.title = title;
 		this.content = content;
 		this.creator = creator;
 	}
 
 	public String getPid()
 	{
 		return this.pid;
 	}
 
 	public Date getCreateDate()
 	{
 		return this.createDate;
 	}
 
 	public String getTitle()
 	{
 		return this.title;
 	}
 
 	public String getContent()
 	{
 		return this.content;
 	}
 
 	public String getCreator()
 	{
 		return this.creator;
 	}
 
 	/**
 	 * Function:
 	 * Getter for ranking member variable.
 	 *
 	 * Param:
 	 * None.
 	 *
 	 * Return:
 	 * The integer value of the ranking.
 	 *
 	 * jnguyen1 20100307
 	 */
 	public int getRanking()
 	{
 		return this.ranking;
 	}
 
 	/**
 	 * Function:
 	 * Calculates the ranking based on the formula T*2+C where T is the number of
 	 * matches found in title and C is the number of matches found in content.
 	 * Updates the rank member variable with the calculated value.
 	 *
 	 * Param:
 	 * keywords - the keywords used in the search and matching.
 	 *
 	 * Return:
 	 * None.
 	 *
 	 * jnguyen1 20100307
 	 */
 	public void calculateRanking(Vector<String> keywords)
 	{
 		int rankingTally = 0;
 
 		// Generate the pattern which is simply "x1|x2|x3|..."
 		String stringPattern = keywords.get(0);
 		for (int i=1; i<keywords.size(); i++)
 		{
			stringPattern.concat("|" + keywords.get(i));
 		}
 
 		Pattern pattern = Pattern.compile(stringPattern);
 
 		// Match against title and add 2 points for each match.
 		Matcher matcher = pattern.matcher(this.title);
 		while (matcher.find())
 		{
 			rankingTally += 2;
 		}
 
 		// Match against content and add 1 points for each match.
 		matcher = pattern.matcher(this.content);
 		while (matcher.find())
 		{
 			rankingTally++;
 		}
 
 		this.ranking = rankingTally;
 	}
 
 	/**
 	 * Function:
 	 * Compare algorithm that compares the rankings between two SearchPageObjects.
 	 *
 	 * Param:
 	 * spo1 - the first SearchPageObject.
 	 * spo2 - the second SearchPageObject.
 	 *
 	 * Return:
 	 * integer that is negative if the spo1 is less then spo2 in ranking, 0 if equal and positive is greater.
 	 *
 	 * jnguyen1 20100307
 	 */
 	public int compareTo(SearchPageObject spo)
 	{
 		return this.getRanking() - spo.getRanking();
 	}
 }
 
