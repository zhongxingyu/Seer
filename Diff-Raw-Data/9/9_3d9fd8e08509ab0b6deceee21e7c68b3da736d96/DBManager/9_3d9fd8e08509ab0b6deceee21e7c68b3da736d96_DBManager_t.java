 package src;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 
 public class DBManager {
 	private Connection connection = null;
 	private PreparedStatement preparedStatement = null;
 	private ResultSet resultSet = null;
 	private final static String DBNAME = "ideascale";
 	private final static String DBUSER = "ideascale";
 	private final static String DBPASS = "ideascale";
 	private final static String HOST = "localhost";
 	private final static String PORT = "8889";
 	
 	public DBManager() {
 		// This will load the MySQL driver, each DB has its own driver
 	    try {
 			Class.forName("com.mysql.jdbc.Driver");
 			String connectionStatement = "jdbc:mysql://"+HOST+":"+PORT+"/" +
 										 DBNAME + "?user=" + DBUSER +
 										 "&password=" + DBPASS;
 			// Setup the connection with the DB
 		    connection = DriverManager.getConnection(connectionStatement);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void insertCommunityFromCSV(HashMap<String,Object> community) 
 	throws SQLException 
 	{
 		preparedStatement = connection.prepareStatement("INSERT INTO communities " +
 							"(name, url, type, orientation, ideas, ideas_implemented, " +
 							"members, votes, comments, owner, purpose, language, " +
 							"status, outlier) values (?, ?, ?, ? , ?, ?, ?, ?, ?, " +
 							"?, ?, ?, ?, ?)");
 		preparedStatement.setString(1, (String) community.get("name"));
 		preparedStatement.setString(2, (String) community.get("url"));
 		preparedStatement.setString(3, (String) community.get("type"));
 		preparedStatement.setString(4, (String) community.get("orientation"));
 		preparedStatement.setInt(5, Integer.parseInt((String) community.get("ideas")));
 		if (community.get("ideas_implemented").toString().contains("---"))
 			preparedStatement.setNull(6, Types.INTEGER);
 		else
 			preparedStatement.setInt(6,Integer.parseInt((String) community.get("ideas_implemented")));
 		preparedStatement.setInt(7,Integer.parseInt((String) community.get("members")));
 		preparedStatement.setInt(8,Integer.parseInt((String) community.get("votes")));
 		preparedStatement.setInt(9, Integer.parseInt((String) community.get("comments")));
 		preparedStatement.setString(10, (String) community.get("owner"));
 		preparedStatement.setString(11, (String) community.get("purpose"));
 		preparedStatement.setString(12, (String) community.get("language"));
 		preparedStatement.setString(13, (String) community.get("status"));
 		preparedStatement.setBoolean(14,(Boolean) community.get("outlier"));
 		preparedStatement.executeUpdate();
 	    preparedStatement.close();
 	}
 	
 	public ArrayList<HashMap<String,String>> getCommunitiesURL(ArrayList<ArrayList<String>> filters)  
 	{
 		ArrayList<HashMap<String,String>> communitiesURL = new ArrayList<HashMap<String,String>>();
 		
 		try {
 			if (filters == null) {
 				preparedStatement = connection.prepareStatement("SELECT id, name, url FROM communities");
 			}
 			else {
 				String whereClause = "";
 				for (ArrayList<String> filter : filters) {
 					if (!whereClause.isEmpty())
 						whereClause += " AND ";
 					whereClause += filter.get(0) + " " + filter.get(1) + " " + filter.get(2);
 				}
 				preparedStatement = connection.prepareStatement("SELECT id, name, url FROM communities WHERE " + whereClause);
 			}
 			resultSet = preparedStatement.executeQuery();
 			while (resultSet.next()) {
 				HashMap<String,String> community = new HashMap<String,String>();
 				community.put("id", resultSet.getString("id"));
 				community.put("name", resultSet.getString("name"));
 				community.put("url", resultSet.getString("url"));
 				communitiesURL.add(community);
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				resultSet.close();
 				preparedStatement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return communitiesURL;
 	}
 	
 	public void saveCommunityDates(String idCommunity, 
 								   HashMap<String,Object> commInfo) 
 	throws SQLException 
 	{		
 			PreparedStatement preparedStatement = connection.prepareStatement(
 												  "UPDATE communities SET " +
 											      "firstidea_ts = ?, lastidea_ts = ?, " +
 											      "lifespan = ?, status = ? WHERE id = ?");
 			
 			HashMap<String,Object> dateInfo = getDatesParams(commInfo);
 			
 			if (dateInfo.get("first_idea") != null)
 				preparedStatement.setDate(1, (java.sql.Date) dateInfo.get("first_idea"));
 			else
 				preparedStatement.setNull(1, java.sql.Types.DATE);
 				
 			if (dateInfo.get("last_idea") != null)
 				preparedStatement.setDate(2, (java.sql.Date) dateInfo.get("last_idea"));
 			else
 				preparedStatement.setNull(2, java.sql.Types.DATE);
 			
 			if (dateInfo.get("lifespan") != null)
 				preparedStatement.setInt(3, (Integer) dateInfo.get("lifespan"));
 			else
 				preparedStatement.setNull(3, java.sql.Types.INTEGER);
 						
 			preparedStatement.setString(4, (String) commInfo.get("status"));
 			preparedStatement.setInt(5, Integer.parseInt(idCommunity));
 			
 			preparedStatement.executeUpdate();
 		    preparedStatement.close();
 	}
 	
 	private HashMap<String,Object> getDatesParams(HashMap<String,Object> commInfo)
 	{
 		java.sql.Date dateFirstIdea = null;
 		java.sql.Date dateLastIdea = null;
 		Integer lifespan = null;
 		HashMap<String,Object> dateInfo = new HashMap<String,Object>();
 		
 		if (commInfo.get("dateFirstIdea") != null && 
 			commInfo.get("dateLastIdea") != null) 
 		{
 			/* First, I need to convert from java.util.Date to java.sql.Date */
 			Date dateUtil = (Date) commInfo.get("dateFirstIdea");
 			dateFirstIdea = new java.sql.Date(dateUtil.getTime());
 			dateUtil = (Date) commInfo.get("dateLastIdea");
 			dateLastIdea = new java.sql.Date(dateUtil.getTime());
 			lifespan = (Integer) commInfo.get("lifespan");
 			dateInfo.put("first_idea", dateFirstIdea);
 			dateInfo.put("last_idea", dateLastIdea);
 			dateInfo.put("lifespan", lifespan);
 		}	
 		else {
 			dateInfo.put("first_idea", null);
 			dateInfo.put("last_idea", null);
 			dateInfo.put("lifespan", null);
 		}			
 		
 		return dateInfo;
 	}
 	
 	public void close() {
 		try {
 			if (connection != null) {
 				connection.close();
 			}
 		} 
 		catch (Exception e) {
 		}
 	}
 	
 	public void updateCommunityInfo(String idCommunity,
 									HashMap<String,Object> communityInfo) 
 	throws SQLException {
 		PreparedStatement preparedStatement = connection.prepareStatement(
 				  							  "UPDATE communities SET " +
 				  							  "ideas = ?, ideas_in_review = ?, " +
 				  							  "ideas_in_progress = ?, ideas_implemented = ?, " +
 				  							  "members = ?, votes = ?, " +
 				  							  "comments = ?, " +
 				  							  "firstidea_ts = ?, lastidea_ts = ?, " +
 			      							  "lifespan = ?, status = ?, updated = ?, " +
 				  							  "facebook = ?, logo = ?, description = ?, " +
 				  							  "twitter = ?, url = ? " +
 			      							  "WHERE id = ?");
 		if (communityInfo.get("ideas") != null)
 			preparedStatement.setInt(1, Integer.parseInt((String) communityInfo.get("ideas")));
 		else
 			preparedStatement.setNull(1, java.sql.Types.INTEGER);
 		if (communityInfo.get("ideas_in_review") != null)
 			preparedStatement.setInt(2, Integer.parseInt((String) communityInfo.get("ideas_in_review")));
 		else
 			preparedStatement.setNull(2, java.sql.Types.INTEGER);
 		if (communityInfo.get("ideas_in_progress") != null)
 			preparedStatement.setInt(3, Integer.parseInt((String) communityInfo.get("ideas_in_progress")));
 		else
 			preparedStatement.setNull(3, java.sql.Types.INTEGER);
 		if (communityInfo.get("ideas_completed") != null)
 			preparedStatement.setInt(4, Integer.parseInt((String) communityInfo.get("ideas_completed")));
 		else
 			preparedStatement.setNull(4, java.sql.Types.INTEGER);
 		if (communityInfo.get("members") != null)
 			preparedStatement.setInt(5, Integer.parseInt((String) communityInfo.get("members")));
 		else
 			preparedStatement.setNull(5, java.sql.Types.INTEGER);
 		if (communityInfo.get("votes") != null)
 			preparedStatement.setInt(6, Integer.parseInt((String) communityInfo.get("votes")));
 		else
 			preparedStatement.setNull(6, java.sql.Types.INTEGER);
 		if (communityInfo.get("comments") != null)
 			preparedStatement.setInt(7, Integer.parseInt((String) communityInfo.get("comments")));
 		else
 			preparedStatement.setNull(7, java.sql.Types.INTEGER);
 		
 		HashMap<String,Object> dateInfo = getDatesParams(communityInfo);
 		
 		if (dateInfo.get("first_idea") != null)
 			preparedStatement.setDate(8, (java.sql.Date) dateInfo.get("first_idea"));
 		else
 			preparedStatement.setNull(8, java.sql.Types.DATE);
 			
 		if (dateInfo.get("last_idea") != null)
 			preparedStatement.setDate(9, (java.sql.Date) dateInfo.get("last_idea"));
 		else
 			preparedStatement.setNull(9, java.sql.Types.DATE);
 		
 		if (dateInfo.get("lifespan") != null)
 			preparedStatement.setInt(10, (Integer) dateInfo.get("lifespan"));
 		else
 			preparedStatement.setNull(10, java.sql.Types.INTEGER);
 					
 		preparedStatement.setString(11, (String) communityInfo.get("status"));
 		preparedStatement.setBoolean(12, true);
 		
 		if (communityInfo.get("facebook") != null)
 			preparedStatement.setInt(13, Integer.parseInt((String) communityInfo.get("facebook")));
 		else
 			preparedStatement.setNull(13, java.sql.Types.INTEGER);
 		
 		if (communityInfo.get("logo") != null)
 			preparedStatement.setBoolean(14, true);
 		else
 			preparedStatement.setBoolean(14, false);
 		
 		if (communityInfo.get("explanation_text") != null)
 			preparedStatement.setBoolean(15, true);
 		else
 			preparedStatement.setBoolean(15, false);
 		
 		if (communityInfo.get("twitter") != null)
 			preparedStatement.setInt(16, Integer.parseInt((String) communityInfo.get("twitter")));
 		else
 			preparedStatement.setInt(16, java.sql.Types.INTEGER);
 		
 		preparedStatement.setString(17, (String) communityInfo.get("url"));
 		
 		preparedStatement.setInt(18, Integer.parseInt(idCommunity));
 		
 		preparedStatement.executeUpdate();
 	    preparedStatement.close();
 	}
 	
 	public void insertCommunityFromHash(HashMap<String,Object> community) 
 	throws SQLException 
 	{
 		preparedStatement = connection.prepareStatement("INSERT INTO communities " +
 							"(name, url, ideas, ideas_in_review, ideas_in_progress," +
 							"ideas_implemented, members, votes, comments, " +
 							"firstidea_ts, lastidea_ts, lifespan, status, " +
 							"outlier, updated, facebook, logo, description, twitter) " +
 							"values (?, ?, ?, ? , ?, ?, ?, ?, ?, " +
 							"?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 		preparedStatement.setString(1, (String) community.get("name"));
 		preparedStatement.setString(2, (String) community.get("url"));
 		if (community.get("ideas") != null)
 			preparedStatement.setInt(3, Integer.parseInt((String) community.get("ideas")));
 		else
 			preparedStatement.setNull(3, java.sql.Types.INTEGER);
 		if (community.get("ideas_in_review") != null)
 			preparedStatement.setInt(4, Integer.parseInt((String) community.get("ideas_in_review")));
 		else
 			preparedStatement.setNull(4, java.sql.Types.INTEGER);
 		if (community.get("ideas_in_progress") != null)	
 			preparedStatement.setInt(5, Integer.parseInt((String) community.get("ideas_in_progress")));
 		else
 			preparedStatement.setNull(5, java.sql.Types.INTEGER);
 		if (community.get("ideas_completed") != null)
 			preparedStatement.setInt(6, Integer.parseInt((String) community.get("ideas_completed")));
 		else
 			preparedStatement.setNull(6, java.sql.Types.INTEGER);
 		if (community.get("members") != null)
 			preparedStatement.setInt(7, Integer.parseInt((String) community.get("members")));
 		else
 			preparedStatement.setNull(7, java.sql.Types.INTEGER);
 		if (community.get("votes") != null)
 			preparedStatement.setInt(8, Integer.parseInt((String) community.get("votes")));
 		else
 			preparedStatement.setNull(8, java.sql.Types.INTEGER);
 		if (community.get("comments") != null)
 			preparedStatement.setInt(9, Integer.parseInt((String) community.get("comments")));
 		else
 			preparedStatement.setNull(9, java.sql.Types.INTEGER);
 		
 		HashMap<String,Object> dateInfo = getDatesParams(community);
 		
 		if (dateInfo.get("first_idea") != null)
 			preparedStatement.setDate(10, (java.sql.Date) dateInfo.get("first_idea"));
 		else
 			preparedStatement.setNull(10, java.sql.Types.DATE);
 			
 		if (dateInfo.get("last_idea") != null)
 			preparedStatement.setDate(11, (java.sql.Date) dateInfo.get("last_idea"));
 		else
 			preparedStatement.setNull(11, java.sql.Types.DATE);
 		
 		if (dateInfo.get("lifespan") != null)
 			preparedStatement.setInt(12, (Integer) dateInfo.get("lifespan"));
 		else
 			preparedStatement.setNull(12, java.sql.Types.INTEGER);
 				
 		preparedStatement.setString(13, (String) community.get("status"));
 		preparedStatement.setBoolean(14, false);
 		preparedStatement.setBoolean(15, true);
 		if (community.get("facebook") != null)
 			preparedStatement.setInt(16, Integer.parseInt((String) community.get("facebook")));
 		else
 			preparedStatement.setNull(16, java.sql.Types.INTEGER);
 		
 		if (community.get("logo") != null)
 			preparedStatement.setBoolean(17, true);
 		else
 			preparedStatement.setBoolean(17, false);
 		
 		if (community.get("explanation_text") != null)
 			preparedStatement.setBoolean(18, true);
 		else
 			preparedStatement.setBoolean(18, false);
 		
 		if (community.get("twitter") != null)
 			preparedStatement.setInt(19, Integer.parseInt((String) community.get("twitter")));
 		else
 			preparedStatement.setInt(19, java.sql.Types.INTEGER);
 				
 		preparedStatement.executeUpdate();
 	    preparedStatement.close();
 	}
 	
 	public String alreadyOnDB(String url) throws SQLException {
 		String idCommunity = null;
 		
 		preparedStatement = connection.prepareStatement("SELECT * FROM communities " +
 													    "WHERE url = ?");
 		preparedStatement.setString(1, url);
 		resultSet = preparedStatement.executeQuery();
 		
 		if (resultSet.first())
 			idCommunity = resultSet.getString("id");
 		
 		preparedStatement.close();
 		resultSet.close();
 		
 		return idCommunity;		
 	}
 	
 	public int removeUnexistingCommunities(String letter) throws SQLException {
 		int deletedRows = 0;
 		
 		if (letter.isEmpty()) {
 			preparedStatement = connection.prepareStatement("DELETE FROM communities " +
 															"WHERE updated = ?");
 			preparedStatement.setBoolean(1, false);
 		}
 		else {
 			preparedStatement = connection.prepareStatement("DELETE FROM communities " +
 																"WHERE updated = ? AND " +
 																"name LIKE ?");
 			preparedStatement.setBoolean(1, false);
 			preparedStatement.setString(2, letter+"%");
 		}
 		
 		deletedRows = preparedStatement.executeUpdate();
 		preparedStatement.close();
 		return deletedRows;
 	}
 	
 	public void insertCommunityTweets(HashMap<String,Object> tweet, String idCommunity) 
 	throws SQLException, ParseException 
 	{	
 		preparedStatement = connection.prepareStatement("INSERT INTO tweets_communities " +
 														"(id_community, id_tweet, author, datetime, url," +
 														"replies, favorites, retweets, text) " +
 														"values (?, ?, ?, ? , ?, ?, ?, ?, ?)");
 		   
 		preparedStatement.setString(1, (String) idCommunity);
 		preparedStatement.setString(2, (String) tweet.get("id"));
 		preparedStatement.setString(3, (String) tweet.get("author"));
 		Date dateUtil = (Date) tweet.get("datetime");
 		java.sql.Timestamp ideaDateTime = new java.sql.Timestamp(dateUtil.getTime());				
 		preparedStatement.setTimestamp(4, ideaDateTime);
 		preparedStatement.setString(5, (String) tweet.get("url"));
 		preparedStatement.setInt(6, (Integer) tweet.get("replies"));
 		preparedStatement.setLong(7, (Long) tweet.get("favorites"));
 		preparedStatement.setLong(8, (Long) tweet.get("retweets"));
 		preparedStatement.setString(9, (String) tweet.get("text"));
 				
 		preparedStatement.executeUpdate();
 	    preparedStatement.close();
 	}
 	
 	public void insertTweetsIdea(HashMap<String,Object> tweet, String idIdea) 
 	throws SQLException, ParseException 
 	{	
 		preparedStatement = connection.prepareStatement("INSERT INTO tweets_ideas " +
 							"(id_idea, id_tweet, author, datetime, url," +
 							"replies, favorites, retweets, text) " +
 							"values (?, ?, ?, ? , ?, ?, ?, ?, ?)");
 		
 		preparedStatement.setString(1, (String) idIdea);
 		preparedStatement.setString(2, (String) tweet.get("id"));
 		preparedStatement.setString(3, (String) tweet.get("author"));
 		Date dateUtil = (Date) tweet.get("datetime");
 		java.sql.Timestamp ideaDateTime = new java.sql.Timestamp(dateUtil.getTime());				
 		preparedStatement.setTimestamp(4, ideaDateTime);
 		preparedStatement.setString(5, (String) tweet.get("url"));
 		preparedStatement.setInt(6, (Integer) tweet.get("replies"));
 		preparedStatement.setLong(7, (Long) tweet.get("favorites"));
 		preparedStatement.setLong(8, (Long) tweet.get("retweets"));
 		preparedStatement.setString(9, (String) tweet.get("text"));
 		
 		preparedStatement.executeUpdate();
 	    preparedStatement.close();
 	}
 	
 	public boolean tweetAlreadyInserted(String idTweet) throws SQLException {
 		Boolean existsTweet = false;
 		preparedStatement = connection.prepareStatement("SELECT * " +
 				  										"FROM tweets_communities " +
 				  										"WHERE id_tweet = ?");
 		preparedStatement.setString(1, (String) idTweet);
 		resultSet = preparedStatement.executeQuery();
 		existsTweet = resultSet.first();
 		
 		resultSet.close();
 		preparedStatement.close();
 		
 		return existsTweet;
 	}
 	
 	public boolean commentAlreadyExisting(Integer idComment, Integer idIdea) 
 	throws SQLException {
 		Boolean existsComment = false;
 		
 		preparedStatement = connection.prepareStatement("SELECT * " +
 				  										"FROM comments " +
 				  										"WHERE idea_id = ? " +
 				  										"AND ideascale_id = ?");
 		preparedStatement.setInt(1, idIdea);
 		preparedStatement.setInt(2, idComment);
 		resultSet = preparedStatement.executeQuery();
 		existsComment = resultSet.first();
 		
 		resultSet.close();
 		preparedStatement.close();
 		
 		return existsComment; 
 	}
 	
 	public void insertComment(HashMap<String,String> comment, Integer idIdea, 
 							  Date storeDT) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement("INSERT INTO comments " +
 														"(author_id, author_name, " +
 														"creation_datetime, description, " +
 														"ideascale_id, idea_id, " +
 														"store_datetime, parent_ideascale_id) " +
 														"values (?, ?, ?, ? , ?, ?, ?, ?)");
 		preparedStatement.setInt(1, Integer.parseInt(comment.get("author-id")));
 		preparedStatement.setString(2, comment.get("author-name"));
 		preparedStatement.setString(3, comment.get("date"));
 		preparedStatement.setString(4, comment.get("description"));
 		preparedStatement.setInt(5, Integer.parseInt(comment.get("id")));
 		preparedStatement.setInt(6, idIdea);
 		java.sql.Timestamp storeDateTime = new java.sql.Timestamp(storeDT.getTime());				
 		preparedStatement.setTimestamp(7, storeDateTime);
 		preparedStatement.setInt(8, Integer.parseInt(comment.get("parent")));
 		
 		preparedStatement.executeUpdate();
 	    preparedStatement.close();
 	}
 	
 	public void insertVote(HashMap<String,String> vote, Integer idIdea, Date storeDT) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement("INSERT INTO votes " +
 														"(author_id, author_name, " +
 														"creation_datetime, value, " +
 														"idea_id, store_datetime) " +
 														"values (?, ?, ?, ? , ?, ?)");
 		preparedStatement.setInt(1, Integer.parseInt(vote.get("author-id")));
 		preparedStatement.setString(2, vote.get("author-name"));
 		preparedStatement.setString(3, vote.get("date"));
 		preparedStatement.setString(4, vote.get("value"));
 		preparedStatement.setInt(5, idIdea);
 		java.sql.Timestamp storeDateTime = new java.sql.Timestamp(storeDT.getTime());				
 		preparedStatement.setTimestamp(6, storeDateTime);
 		
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public ArrayList<HashMap<String,String>> getActiveCommunities() 
 	throws SQLException 
 	{
 		ArrayList<HashMap<String,String>> activeCommunities = new ArrayList<HashMap<String,String>>();
 		
 		preparedStatement = connection.prepareStatement("SELECT id, name, url, " +
 														"facebook, twitter, " +
 														"ideas, comments, votes, " +
 														"members " +
 														"FROM communities " +
 			    										"WHERE status = ? AND " +
 			    										"facebook != ? AND " +
 			    										"synchronized = ? " +
 			    										"ORDER BY ideas ASC");
 		preparedStatement.setString(1, "active");
 		preparedStatement.setString(2, "NULL");
 		preparedStatement.setBoolean(3, false);
 		resultSet = preparedStatement.executeQuery();
 		while (resultSet.next()) {
 			HashMap<String,String> community = new HashMap<String,String>();
 			community.put("id", resultSet.getString("id"));
 			community.put("url", resultSet.getString("url"));
 			community.put("name", resultSet.getString("name"));
 			community.put("facebook", resultSet.getString("facebook"));
 			community.put("twitter", resultSet.getString("twitter"));
 			community.put("ideas", resultSet.getString("ideas"));
 			community.put("comments", resultSet.getString("comments"));
 			community.put("votes", resultSet.getString("votes"));
 			community.put("members", resultSet.getString("members"));
 			activeCommunities.add(community);
 		}
 		
 		resultSet.close();
 		preparedStatement.close();
 		
 		return activeCommunities;
 	}
 	
 	public void updateCommunityStats(HashMap<String,Object> stats, 
 										String idCommunity) 
 	throws SQLException 
 	{
 		preparedStatement = connection.prepareStatement("UPDATE communities SET " +
 				  										"twitter = ?, facebook = ?, " +
 				  										"ideas = ?, comments = ?, " +
 				  										"votes = ?, ideas_in_review = ?, " +
 				  										"ideas_in_progress = ?, " +
 				  										"ideas_implemented = ?," +
 				  										"members = ? " +
 				  										"WHERE id = ?");
 		if (stats.get("twitter") != null)
 			preparedStatement.setInt(1, Integer.parseInt((String) stats.get("twitter")));
 		else
 			preparedStatement.setNull(1, java.sql.Types.INTEGER);
 		if (stats.get("facebook") != null)
 			preparedStatement.setInt(2, Integer.parseInt((String) stats.get("facebook")));
 		else
 			preparedStatement.setNull(2, java.sql.Types.INTEGER);
 		if (stats.get("ideas") != null)
 			preparedStatement.setInt(3, Integer.parseInt((String) stats.get("ideas")));
 		else
 			preparedStatement.setNull(3, java.sql.Types.INTEGER);
 		if (stats.get("comments") != null)
 			preparedStatement.setInt(4, Integer.parseInt((String) stats.get("comments")));
 		else
 			preparedStatement.setNull(4, java.sql.Types.INTEGER);
 		if (stats.get("votes") != null)
 			preparedStatement.setInt(5, Integer.parseInt((String) stats.get("votes")));
 		else
 			preparedStatement.setNull(5, java.sql.Types.INTEGER);
 		if (stats.get("ideas_in_review") != null)
 			preparedStatement.setInt(6, Integer.parseInt((String) stats.get("ideas_in_review")));
 		else
 			preparedStatement.setNull(6, java.sql.Types.INTEGER);
 		if (stats.get("ideas_in_progress") != null)	
 			preparedStatement.setInt(7, Integer.parseInt((String) stats.get("ideas_in_progress")));
 		else
 			preparedStatement.setNull(7, java.sql.Types.INTEGER);
 		if (stats.get("ideas_completed") != null)
 			preparedStatement.setInt(8, Integer.parseInt((String) stats.get("ideas_completed")));
 		else
 			preparedStatement.setNull(8, java.sql.Types.INTEGER);
 		if (stats.get("members") != null)
 			preparedStatement.setInt(9, Integer.parseInt((String) stats.get("members")));
 		else
 			preparedStatement.setNull(9, java.sql.Types.INTEGER);
 		preparedStatement.setString(10, (String) idCommunity);
 		
 		preparedStatement.executeUpdate();
 	    preparedStatement.close();
 	}
 	
 	public void insertCommunityIdea(HashMap<String,Object> idea, String communityId) 
 	throws SQLException, ParseException {
 		
 		
 		Integer idIdea = Integer.parseInt((String) idea.get("id"));
 
 		preparedStatement = connection.prepareStatement("INSERT INTO ideas " +
 				"(ideascale_id, title, description, " +
 				"creation_datetime, tags, author_name, " +
 				"community_id, twitter, facebook, url, score, comments, " +
 				"similar_to, author_id, status, considered) " +
 				"values (?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 		preparedStatement.setInt(1, idIdea);
 		preparedStatement.setString(2, (String) idea.get("title"));
 		if (idea.get("description") != null)
 			preparedStatement.setString(3, (String) idea.get("description"));
 		else
 			preparedStatement.setNull(3, java.sql.Types.NULL);
 		if (idea.get("datetime") != null) {
 			Date dateUtil = (Date) idea.get("datetime");
 			java.sql.Timestamp ideaDateTime = new java.sql.Timestamp(dateUtil.getTime());				
 			preparedStatement.setTimestamp(4, ideaDateTime);
 		}
 		else
 			preparedStatement.setNull(4, java.sql.Types.DATE);
 		if (idea.get("tags") != null)
 			preparedStatement.setString(5, (String) idea.get("tags"));
 		else
 			preparedStatement.setNull(5, java.sql.Types.NULL);
 		if (idea.get("author-name") != null)
 			preparedStatement.setString(6, (String) idea.get("author-name"));
 		else
 			preparedStatement.setNull(6, java.sql.Types.NULL);
 		preparedStatement.setString(7, (String) communityId);
 		if (idea.get("twitter") != null)
 			preparedStatement.setInt(8, Integer.parseInt((String) idea.get("twitter")));
 		else
 			preparedStatement.setNull(8, java.sql.Types.INTEGER);
 		if (idea.get("facebook") != null)
 			preparedStatement.setInt(9, Integer.parseInt((String) idea.get("facebook")));
 		else
 			preparedStatement.setNull(9, java.sql.Types.INTEGER);
 		preparedStatement.setString(10, (String) idea.get("url"));
 		if (idea.get("score") != null)
 			preparedStatement.setInt(11, (Integer) idea.get("score"));
 		else
 			preparedStatement.setNull(11, java.sql.Types.INTEGER);
 		if (idea.get("comments") != null)
 			preparedStatement.setInt(12, (Integer) idea.get("comments"));
 		else
 			preparedStatement.setNull(12, java.sql.Types.INTEGER);
 		if (idea.get("similar") != null)
 			preparedStatement.setInt(13, (Integer) idea.get("similar"));
 		else
 			preparedStatement.setNull(13, java.sql.Types.INTEGER);
 		if (idea.get("author-id") == "")
 			System.out.println("Idea: "+idea.get("url"));
 		if (idea.get("author-id") != null)
 			preparedStatement.setInt(14, Integer.parseInt((String) idea.get("author-id")));
 		else
 			preparedStatement.setNull(14, java.sql.Types.INTEGER);
 		if (idea.get("status") != null) {
 			String status = (String) idea.get("status");
 			preparedStatement.setString(15, status);
 			if (status.equals("completed") || status.equals("under-review") ||
 				status.equals("in-progress"))
 				preparedStatement.setBoolean(16, true);
 			else
 				preparedStatement.setBoolean(16, false);
 		}
 		else {
 			preparedStatement.setNull(15, java.sql.Types.NULL);
 			preparedStatement.setBoolean(16, false);
 		}
 		
 		preparedStatement.executeUpdate();
 		
 		preparedStatement.close();
 	}
 	
 	public void updateCommunityIdea(HashMap<String,Object> idea, Integer idIdea) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement(
 				"UPDATE ideas SET " +
 				"twitter = ?, facebook = ?, score = ?, " +
 				"comments = ?, similar_to = ?, status = ?, author_name = ?, " +
 				"considered = ? " +
 				"WHERE id = ?");
 		if (idea.get("twitter") != null)
 			preparedStatement.setInt(1, Integer.parseInt((String) idea.get("twitter")));
 		else
 			preparedStatement.setNull(1, java.sql.Types.INTEGER);
 		if (idea.get("facebook") != null)
 			preparedStatement.setInt(2, Integer.parseInt((String) idea.get("facebook")));
 		else
 			preparedStatement.setNull(2, java.sql.Types.INTEGER);
 		if (idea.get("score") != null)
 			preparedStatement.setInt(3, (Integer) idea.get("score"));
 		else
 			preparedStatement.setNull(3, java.sql.Types.INTEGER);
 		if (idea.get("comments") != null)
 			preparedStatement.setInt(4, (Integer) idea.get("comments"));
 		else
 			preparedStatement.setNull(4, java.sql.Types.INTEGER);
 		if (idea.get("similar") != null)
 			preparedStatement.setInt(5, (Integer) idea.get("similar"));
 		else
 			preparedStatement.setNull(5, java.sql.Types.INTEGER);
 		if (idea.get("status") != null) {
 			String status = (String) idea.get("status");
 			preparedStatement.setString(6, status);
 			if (status.equals("completed") || status.equals("under-review") ||
 				status.equals("in-progress"))
 				preparedStatement.setBoolean(8, true);
 			else
 				preparedStatement.setBoolean(8, false);
 		}
 		else {
 			preparedStatement.setNull(6, java.sql.Types.NULL);
 			preparedStatement.setBoolean(8, false);
 		}
 		if (idea.get("author-name") != null)
 			preparedStatement.setString(7, (String) idea.get("author-name"));
 		else
 			preparedStatement.setNull(7, java.sql.Types.NULL);
 	
 		preparedStatement.setInt(9, idIdea);
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public HashMap<String,String> ideaAlreadyInserted(Integer idIdea) throws SQLException {
 		HashMap<String,String> existingIdea = new HashMap<String,String>();
 		
 		preparedStatement = connection.prepareStatement("SELECT * " +
 				  										"FROM ideas " +
 				  										"WHERE ideascale_id = ?");
 		preparedStatement.setInt(1, idIdea);
 		resultSet = preparedStatement.executeQuery();
 		if (resultSet.first()) {
 			existingIdea.put("id", resultSet.getString("id"));
 			existingIdea.put("name", resultSet.getString("title"));
 			existingIdea.put("facebook", resultSet.getString("facebook"));
 			existingIdea.put("twitter", resultSet.getString("twitter"));
 			existingIdea.put("score", resultSet.getString("score"));
 			existingIdea.put("comments", resultSet.getString("comments"));
 			existingIdea.put("url", resultSet.getString("url"));
 		}
 		
 		resultSet.close();
 		preparedStatement.close();
 		
 		return existingIdea;
 	}
 	
 	public ArrayList<HashMap<String,String>> getCommunityIdeas(String idCommunity) 
 	throws SQLException 
 	{
 		ArrayList<HashMap<String,String>> ideas = new ArrayList<HashMap<String,String>>();
 		
 		preparedStatement = connection.prepareStatement("SELECT id, url, facebook, twitter " +
 														"FROM ideas " +
 														"WHERE community_id = ?");
 		preparedStatement.setInt(1, Integer.parseInt(idCommunity));
 		resultSet = preparedStatement.executeQuery();
 		while (resultSet.next()) {
 			HashMap<String,String> idea = new HashMap<String,String>();
 			idea.put("id", resultSet.getString("id"));
 			idea.put("url", resultSet.getString("url"));
 			idea.put("facebook", resultSet.getString("facebook"));
 			idea.put("twitter", resultSet.getString("twitter"));
 			ideas.add(idea);
 		}
 		
 		preparedStatement.close();
 		resultSet.close();
 		
 		return ideas;
 	}
 	
 	public void registerOperation(Date date, String operation, Date duration,
 								  Integer observation) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement("INSERT INTO audit " +
 														"(date, operation, duration, " +
 														"observation) " +
 														"values (?, ?, ?, ?)");
 		java.sql.Timestamp opDateTime = new java.sql.Timestamp(date.getTime());
 		preparedStatement.setTimestamp(1, opDateTime);
 		preparedStatement.setString(2, operation);
 		java.sql.Time opDuration = new java.sql.Time(duration.getTime());
 		preparedStatement.setTime(3, opDuration);
 		preparedStatement.setInt(4, observation);
 		
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public void resetUpdateFlag(String letter) throws SQLException {
 		if (letter.isEmpty()) {
 			preparedStatement = connection.prepareStatement("UPDATE communities SET " +
 															"updated = ?");
 			preparedStatement.setBoolean(1, false);
 		}
 		else {
 			preparedStatement = connection.prepareStatement("UPDATE communities SET " +
 															"updated = ? " +
 															"WHERE name LIKE ?");
 			preparedStatement.setBoolean(1, false);
 			preparedStatement.setString(2, letter+"%");
 		}
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public void saveLogCommunity(Integer observation,
 								 HashMap<String,String> community, 
 								 HashMap<String,Object> newStats) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement("INSERT INTO log_communities " +
 														"(observation, community_id, name," +
 														"old_facebook, new_facebook," +
 														"old_twitter, new_twitter," +
 														"old_members, new_members," +
 														"old_ideas, new_ideas," +
 														"old_comments, new_comments," +
 														"old_votes, new_votes) " +
 														"values (?, ?, ?, ?, ?, ?, " +
 														"?, ?, ?, ?, ?, ?, ?, ?, ?)");
 		preparedStatement.setInt(1, observation);
 		preparedStatement.setString(2, community.get("id"));
 		preparedStatement.setString(3, community.get("name"));
 		preparedStatement.setInt(4, Integer.parseInt(community.get("facebook")));
 		preparedStatement.setInt(5, Integer.parseInt((String) newStats.get("facebook")));
 		preparedStatement.setInt(6, Integer.parseInt(community.get("twitter")));
 		preparedStatement.setInt(7, Integer.parseInt((String) newStats.get("twitter")));
 		preparedStatement.setInt(8, Integer.parseInt(community.get("members")));
 		preparedStatement.setInt(9, Integer.parseInt((String) newStats.get("members")));
 		preparedStatement.setInt(10, Integer.parseInt(community.get("ideas")));
 		preparedStatement.setInt(11, Integer.parseInt((String) newStats.get("ideas")));
 		preparedStatement.setInt(12, Integer.parseInt(community.get("comments")));
 		preparedStatement.setInt(13, Integer.parseInt((String) newStats.get("comments")));
 		preparedStatement.setInt(14, Integer.parseInt(community.get("votes")));
 		preparedStatement.setInt(15, Integer.parseInt((String) newStats.get("votes")));
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public void saveLogIdea(Integer observation, HashMap<String,String> existingIdea, 
 			 				HashMap<String,Object> newIdea) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement("INSERT INTO log_ideas " +
 														"(observation, idea_id, name," +
 														"old_facebook, new_facebook," +
 														"old_twitter, new_twitter," +
 														"old_comments, new_comments) " +
 														"values (?, ?, ?, ?, ?, ?, " +
 														"?, ?, ?)");
 		preparedStatement.setInt(1, observation);
 		preparedStatement.setString(2, existingIdea.get("id"));
 		preparedStatement.setString(3, existingIdea.get("name"));
 		preparedStatement.setInt(4, Integer.parseInt(existingIdea.get("facebook")));
 		preparedStatement.setInt(5, Integer.parseInt((String) newIdea.get("facebook")));
 		preparedStatement.setInt(6, Integer.parseInt(existingIdea.get("twitter")));
 		preparedStatement.setInt(7, Integer.parseInt((String) newIdea.get("twitter")));
 		preparedStatement.setInt(8, Integer.parseInt(existingIdea.get("comments")));
 		preparedStatement.setInt(9, (Integer) newIdea.get("comments"));
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public void resetSyncFlag() throws SQLException {
 		preparedStatement = connection.prepareStatement("UPDATE communities SET " +
 														"synchronized = ?");
 		preparedStatement.setBoolean(1, false);
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public void updateSyncFlag(String idCommunity) throws SQLException {
 		preparedStatement = connection.prepareStatement("UPDATE communities SET " +
 														"synchronized = ? WHERE " +
 														"id = ?");
 		preparedStatement.setBoolean(1, true);
 		preparedStatement.setString(2, idCommunity);
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public Integer getIdeaId(Integer idIdeaScale) throws SQLException {
 		Integer idIdeaDB = -1;
 		preparedStatement = connection.prepareStatement("SELECT id " +
 														"FROM ideas " +
 														"WHERE ideascale_id = ?");
 		preparedStatement.setInt(1, idIdeaScale);
 		resultSet = preparedStatement.executeQuery();
 		if (resultSet.first()) 
 			idIdeaDB = resultSet.getInt("id");
 		
 		preparedStatement.close();
 		resultSet.close();
 		return idIdeaDB;
 	}
 	
 	public ArrayList<HashMap<String,String>> getIdeasTweets() throws SQLException {
 		ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
 		
 		preparedStatement = connection.prepareStatement("SELECT id, id_tweet " +
 														"FROM tweets_ideas");
 		resultSet = preparedStatement.executeQuery();
 		while (resultSet.next()) {
 			HashMap<String,String> tweet = new HashMap<String,String>();
 			tweet.put("id", resultSet.getString("id"));
 			tweet.put("id_tweet", resultSet.getString("id_tweet"));
 			tweets.add(tweet);
 		}
 		
 		preparedStatement.close();
 		resultSet.close();
 		return tweets;
 	}
 	
 	public void updateIdeaTweetMetric(String id, HashMap<String,Long> newMetrics) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement("UPDATE tweets_ideas SET " +
 														"retweets = ?, favorites = ?, " +
 														"replies = ? WHERE " +
 														"id = ?");
 		preparedStatement.setLong(1, newMetrics.get("retweets"));
 		preparedStatement.setLong(2, newMetrics.get("favorites"));
 		preparedStatement.setLong(3, newMetrics.get("replies"));
 		preparedStatement.setString(4, id);
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public ArrayList<HashMap<String,String>> getCommunitiesTweets() throws SQLException {
 		ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
 		
 		preparedStatement = connection.prepareStatement("SELECT id, id_tweet " +
 														"FROM tweets_communities");
 		resultSet = preparedStatement.executeQuery();
 		while (resultSet.next()) {
 			HashMap<String,String> tweet = new HashMap<String,String>();
 			tweet.put("id", resultSet.getString("id"));
 			tweet.put("id_tweet", resultSet.getString("id_tweet"));
 			tweets.add(tweet);
 		}
 		
 		preparedStatement.close();
 		resultSet.close();
 		
 		return tweets;
 	}
 	
 	public void updateCommunityTweetMetric(String id, HashMap<String,Long> newMetrics) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement("UPDATE tweets_communities SET " +
 														"retweets = ?, favorites = ?, " +
 														"replies = ? WHERE " +
 														"id = ?");
 		preparedStatement.setLong(1, newMetrics.get("retweets"));
 		preparedStatement.setLong(2, newMetrics.get("favorites"));
 		preparedStatement.setLong(3, newMetrics.get("replies"));
 		preparedStatement.setString(4, id);
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public void insertSyncProcess(String communityURL, String currentTab, 
 								  Integer currentPage, Integer communityId,
 								  Integer observation) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement("INSERT INTO sync_progress " +
 														"(community_url, current_tab, " +
 														"current_page, community_id, " +
 														"observation) " +
 														"values (?, ?, ?, ?, ?)");
 		preparedStatement.setString(1, communityURL);
 		preparedStatement.setString(2, currentTab);
 		preparedStatement.setInt(3, currentPage);
 		preparedStatement.setInt(4, communityId);
		preparedStatement.setInt(5, observation);
 		
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public HashMap<String,Object> getUnfinishedSyncProcess() throws SQLException {
 		HashMap<String,Object> unfinishedProcess = new HashMap<String,Object>();
 		
 		preparedStatement = connection.prepareStatement("SELECT * " +
 														"FROM sync_progress");
 		resultSet = preparedStatement.executeQuery();
 		if (resultSet.first()) {
 			unfinishedProcess.put("id", resultSet.getInt("id"));
 			unfinishedProcess.put("community_id", resultSet.getInt("community_id"));
 			unfinishedProcess.put("community_url", resultSet.getString("community_url"));
 			unfinishedProcess.put("current_tab", resultSet.getString("current_tab"));
 			unfinishedProcess.put("current_page", resultSet.getInt("current_page"));
 			unfinishedProcess.put("observation", resultSet.getInt("observation"));
 		}
 		
 		preparedStatement.close();
 		resultSet.close();
 		
 		return unfinishedProcess;
 	}
 	
 	public void cleanSyncProgressTable() throws SQLException {
 		//Clean the table since it should be only one progress running at
 		//the same time
 		preparedStatement = connection.prepareStatement("DELETE FROM sync_progress");
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public void updateSyncProcess(Integer currentPage) 
 	throws SQLException {
 		preparedStatement = connection.prepareStatement("UPDATE sync_progress SET " +
 														"current_page = ?");
 		preparedStatement.setInt(1, currentPage);
 		
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	public Integer getLastObservationId() throws SQLException {
 		Integer observation;
 		
 		preparedStatement = connection.prepareStatement("SELECT observation " +
 														"FROM audit");
 		resultSet = preparedStatement.executeQuery();
 		if (resultSet.first())
 			observation = resultSet.getInt("observation");
 		else
 			observation = 0;
 		
 		preparedStatement.close();
 		resultSet.close();
 		
 		return observation;
 	}
 }
