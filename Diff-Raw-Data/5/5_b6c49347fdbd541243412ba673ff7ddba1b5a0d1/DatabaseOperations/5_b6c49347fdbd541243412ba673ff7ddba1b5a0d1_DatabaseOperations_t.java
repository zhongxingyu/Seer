 package sce.finalprojects.sceprojectbackend.database;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 
 import sce.finalprojects.sceprojectbackend.datatypes.Cachable;
 import sce.finalprojects.sceprojectbackend.datatypes.CacheToken;
 import sce.finalprojects.sceprojectbackend.datatypes.ClusterRepresentationDO;
 import sce.finalprojects.sceprojectbackend.datatypes.Comment;
 import sce.finalprojects.sceprojectbackend.datatypes.CommentEntityDS;
 import sce.finalprojects.sceprojectbackend.datatypes.MapCell;
 import sce.finalprojects.sceprojectbackend.managers.DatabaseManager;
 import sce.finalprojects.sceprojectbackend.utils.MarkupUtility;
 
 
 public class DatabaseOperations {
 
 	/**
 	 * return all the comments vectors that belongs to the article
 	 * @param articleID 
 	 * @return an arrayList of comments 
 	 * @throws SQLException 
 	 */
 	public static ArrayList<Comment> getAllComentsWithoutHTML(String articleID){
 	
 		Connection conn;
 		ArrayList<Comment> ArrayOfComments = null;
 		try {
 			
 			conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement sqlQuerry = conn.prepareStatement("SELECT comment_id,vector FROM comments WHERE article_id = ? ORDER BY comment_id ASC;");
 			sqlQuerry.setString(1, articleID);
 			ResultSet rs = sqlQuerry.executeQuery();
 			ArrayOfComments = new ArrayList<Comment>();
 			
 			while(rs.next())
 			{
 				ArrayOfComments.add(new Comment(rs.getString("comment_id"),Comment.replaceStringWithVector(rs.getString("vector"))));
 			}
 			
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return ArrayOfComments;
 	}
 	
 	/**
 	 * save the article mapping
 	 * @param articleId
 	 * @param mapping
 	 * @throws SQLException
 	 */
 	public static void setArticleMapping(String articleId, ArrayList<MapCell> mapping) throws SQLException {
 		//INSERT INTO `mydb`.`hacnodesmapping` (`articleid`, `commentid`, `node_mapping`) VALUES ('1', '12', 'dsa'), ('2', '12', 'dsa');
 		
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		
 		PreparedStatement sqlQuerryDel = conn.prepareStatement("DELETE FROM HACNodesMapping WHERE article_id = ? ;");
 		sqlQuerryDel.setString(1, articleId);
 		sqlQuerryDel.execute();
 
 		StringBuffer insertQuerry = new StringBuffer();
 		for (MapCell mapCell : mapping) {
 			insertQuerry.append("('").append(mapCell.getArticle_id()).append("','").append(mapCell.getComment_id()).append("','").append(mapCell.getMapping()).append("','").append(mapCell.getDirect()).append("'),");
 		}
 		//insertQuerry = insertQuerry.substring(0, insertQuerry.length() - 1) + ";";
 		insertQuerry.replace(0, insertQuerry.length(), insertQuerry.substring(0, insertQuerry.length()-1));
 		
 		PreparedStatement sqlQuerry = conn.prepareStatement("INSERT INTO HACNodesMapping (`article_id`, `comment_id`, `node_mapping`,`direct`) VALUES "+insertQuerry);
 		//System.out.println(sqlQuerry);
 		sqlQuerry.execute();
 		
 	}
 	
 	/**
 	 * return from DB the XML representation of the HAC for a given article
 	 * @param articleID
 	 * @return
 	 * @throws SQLException 
 	 */
 	public static String getXMLRepresentation(String articleID){
 		
 		Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement sqlQuerry = conn.prepareStatement("SELECT xmlRepresentation FROM articles WHERE article_id = ?;");
 			sqlQuerry.setString(1, articleID);
 			ResultSet rs = sqlQuerry.executeQuery();
 			
 			rs.next();
 			String xmlrep= rs.getString("xmlRepresentation");
 			
 			if(xmlrep.length() == 0)
 				return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
 			
 			return xmlrep;
 			//return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Cluster id=\"1\" level=\"0\" mergeSim=\"1.0\"><Cluster id=\"6\" level=\"1\" mergeSim=\"0.9\"><Cluster id=\"10\" level=\"2\" mergeSim=\"1.0\"><Cluster id=\"10\" level=\"3\" mergeSim=\"1\"><Cluster id=\"10\" level=\"4\" mergeSim=\"1\"/></Cluster></Cluster><Cluster id=\"6\" level=\"2\" mergeSim=\"0.8\"><Cluster id=\"9\" level=\"3\" mergeSim=\"0.6\"><Cluster id=\"8\" level=\"4\" mergeSim=\"1.0\"/><Cluster id=\"9\" level=\"4\" mergeSim=\"0.6\"/></Cluster><Cluster id=\"6\" level=\"3\" mergeSim=\"0.4\"><Cluster id=\"7\" level=\"4\" mergeSim=\"1.0\"/><Cluster id=\"6\" level=\"4\" mergeSim=\"0.4\"/></Cluster></Cluster></Cluster><Cluster id=\"1\" level=\"1\" mergeSim=\"0.7\"><Cluster id=\"4\" level=\"2\" mergeSim=\"0.3\"><Cluster id=\"5\" level=\"3\" mergeSim=\"1.0\"><Cluster id=\"5\" level=\"4\" mergeSim=\"1\"/></Cluster><Cluster id=\"4\" level=\"3\" mergeSim=\"0.3\"><Cluster id=\"4\" level=\"4\" mergeSim=\"1\"/></Cluster></Cluster><Cluster id=\"1\" level=\"2\" mergeSim=\"0.5\"><Cluster id=\"3\" level=\"3\" mergeSim=\"1.0\"><Cluster id=\"3\" level=\"4\" mergeSim=\"1\"/></Cluster><Cluster id=\"1\" level=\"3\" mergeSim=\"0.2\"><Cluster id=\"2\" level=\"4\" mergeSim=\"1.0\"/><Cluster id=\"1\" level=\"4\" mergeSim=\"0.2\"/></Cluster></Cluster></Cluster></Cluster>";
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
 		}
 	}
 	
 	/**
 	 * save the xml representation
 	 * @param articleId
 	 * @param xmlHacRepresentation
 	 * @throws SQLException
 	 */
 	public static void setXmlRepresentation(String articleId, String xmlHacRepresentation) throws SQLException {
 		
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		PreparedStatement sqlQuerry = conn.prepareStatement("UPDATE articles SET xmlRepresentation = ?  WHERE  article_id = ? ;");
 		sqlQuerry.setString(1, xmlHacRepresentation);
 		sqlQuerry.setString(2, articleId);
 
 		sqlQuerry.execute();
 	}
 	
 	/**
 	 * return the article mapping
 	 * @param articleId
 	 * @return
 	 * @throws SQLException
 	 */
 	public static ArrayList<MapCell> getArticleMapping(String articleId) throws SQLException {
 		//TODO check that method
 		Connection conn = DatabaseManager.getInstance().getConnection();
 		
 		PreparedStatement sqlQuerry = conn.prepareStatement("SELECT * FROM HACNodesMapping WHERE article_id = ? ;");
 		sqlQuerry.setString(1, articleId);
 		ResultSet rs = sqlQuerry.executeQuery();
 		ArrayList<MapCell> returnarray = new ArrayList<MapCell>();
 		
 		while(rs.next()) {
 			returnarray.add(new MapCell(rs.getString("article_id"),rs.getString("comment_id"),rs.getString("node_mapping"),rs.getInt("direct")));
 		}
 		
 		return returnarray;
 
 		//		
 //		String temp = "1 10 10_3, 1 8 9_3, 1 9 9_3, 1 7 6_3, 1 6 6_3, 1 5 5_3, 1 4 4_3, 1 3 3_3, 1 2 1_3, 1 1 1_3, 1 10 10_2, 1 8 6_2, 1 9 6_2, 1 7 6_2, 1 6 6_2, 1 5 4_2, 1 4 4_2, 1 3 1_2, 1 2 1_2, 1 1 1_2, 1 10 6_1, 1 8 6_1, 1 7 6_1, 1 6 6_1, 1 9 6_1, 1 5 1_1, 1 4 1_1, 1 3 1_1, 1 2 1_1, 1 1 1_1, 1 8 1_0, 1 7 1_0, 1 6 1_0, 1 9 1_0, 1 10 1_0, 1 3 1_0, 1 5 1_0, 1 2 1_0, 1 4 1_0, 1 1 1_0";
 		
 	}
 	
 	/**
 	 * set the article words
 	 * @param articleId
 	 * @param words
 	 * @throws SQLException
 	 */
 	public static void setArticleWords(String articleId , ArrayList<String> words) {
 		Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement delStmt = conn.prepareStatement("DELETE FROM article_words WHERE article_id = ? ");
 			delStmt.setString(1, articleId);
 			delStmt.execute();
 						
 			StringBuilder insertQuerry = new StringBuilder();
 			int i=0;
 			for (String word : words) {
 				insertQuerry.append("('").append(articleId).append("','").append(word).append("',").append(i++).append(") , ");
 			}
 			insertQuerry.replace(0, insertQuerry.length()-1, insertQuerry.substring(0, insertQuerry.length()-2));
 			
 			PreparedStatement sqlQuerry = conn.prepareStatement("INSERT IGNORE INTO article_words (`article_id`,`word`,`order`) VALUES " + insertQuerry + ";");
 			
 			sqlQuerry.execute();
 		} catch (SQLException e) {e.printStackTrace();}
 }
 	
 	/**
 	 * return the word of the article ordered by order col
 	 * @param articleId
 	 * @return
 	 * @throws SQLException
 	 */
 	public static ArrayList<String> getArticleWords(String articleId) {
 		Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement sqlQuerry = conn.prepareStatement("SELECT * FROM article_words WHERE article_id = ? ORDER BY `order` ASC;");
 			sqlQuerry.setString(1, articleId);
 			ResultSet rs = sqlQuerry.executeQuery();
 			
 			ArrayList<String> arrayOfWords = new ArrayList<String>();
 			
 			while(rs.next()) {
 				arrayOfWords.add(rs.getString("word"));
 			}
 			
 			return arrayOfWords;
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return null;
 	}
 	
 	/**
 	 * creating a new Article in DB
 	 * @param articleId
 	 * @param articleUrl
 	 * @param numOfComments
 	 * @throws SQLException
 	 */
 	public static void addNewArticle(String articleId, String articleUrl, int numOfComments, String commentsAmountURL) {
 		try{
 	    	Connection conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement sqlQuerry = conn.prepareStatement("INSERT IGNORE INTO articles (article_id,url,number_of_comments,comments_amount_url,creation_time) VALUES (?,?,?,?,NOW()) ;");
 			sqlQuerry.setString(1, articleId);
 			sqlQuerry.setString(2, articleUrl);
 			sqlQuerry.setInt(3, numOfComments);
 			sqlQuerry.setString(4, commentsAmountURL);
 			
 //			java.text.SimpleDateFormat sdf = 
 //			     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 //	
 //			sqlQuerry.setString(4, sdf.format(new java.util.Date()));
 			
 			sqlQuerry.execute();
 			
 	            //set in the table: article id, article url and the number of the comment we get the first time
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
     }
 	
 	/**
 	 * return the number of comments that stored in the DB for a given article
 	 * @param articleId
 	 * @return
 	 * @throws SQLException
 	 */
     public static int getArticleNumOfComments(String articleId)
     {
 
     	Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement sqlQuerry = conn.prepareStatement("SELECT number_of_comments FROM articles WHERE article_id = ? ;");
 			sqlQuerry.setString(1, articleId);
 			ResultSet rs = sqlQuerry.executeQuery();
 			
 			while(rs.next()) {
 				return rs.getInt("number_of_comments");
 			}
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return -1;   
     }
     
     /**
      * return the URL of given Article_id
      * @param articleId
      * @return
      * @throws SQLException
      */
     public static String getNewNumberOfCommentsUrl(String articleId)
     {
     	//TODO check that method
     	Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement sqlQuerry = conn.prepareStatement("SELECT `comments_amount_url` FROM articles WHERE article_id = ? ;");
 			sqlQuerry.setString(1, articleId);
 			ResultSet rs = sqlQuerry.executeQuery();
 			
 			while(rs.next()) {
 				return rs.getString("comments_amount_url");
 			}
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return null;
     }
     
     /**
      * return the URL of given Article_id
      * @param articleId
      * @return
      * @throws SQLException
      */
     public static String getUrl(String articleId)
     {
     	//TODO check that method
     	Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement sqlQuerry = conn.prepareStatement("SELECT url FROM articles WHERE article_id = ? ;");
 			sqlQuerry.setString(1, articleId);
 			ResultSet rs = sqlQuerry.executeQuery();
 			
 			while(rs.next()) {
 				return rs.getString("url");
 			}
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return null;
     }
     
     /**
      * set the new number of comments for a given comment
      * @param articleId
      * @param numberOfComments
      * @throws SQLException
      */
     public static void setArticleNumOfComments(String articleId, int numberOfComments) throws SQLException
     {
 
     	Connection conn = DatabaseManager.getInstance().getConnection();
 		PreparedStatement sqlQuerry = conn.prepareStatement("UPDATE articles SET number_of_comments = ? WHERE article_id = ? ;");
 		sqlQuerry.setInt(1, numberOfComments);
 		sqlQuerry.setString(2, articleId);
 		sqlQuerry.execute();
     }
 
     /**
      * add comments for an article
      * @param articleId
      * @param commments
      * @throws SQLException
      */
     public static void setComments(String articleId,ArrayList<CommentEntityDS> commments)
     {
 
     	Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 			StringBuffer insertQuerry = new StringBuffer();
 	    	for (CommentEntityDS comm : commments) {   
 				insertQuerry.append("(").append(comm.getId()).append(",'").append(articleId).append("','").append(StringEscapeUtils.escapeHtml4(comm.getCommentHTML()).replace('\'', ' ')).append("',\"").append(CommentEntityDS.vectorToString(comm.getVector())).append("\") , ");
 			}
 	    	insertQuerry.replace(0, insertQuerry.length()-1, insertQuerry.substring(0, insertQuerry.length()-2));
 	    	String querry = insertQuerry.toString();
 			PreparedStatement sqlQuerry = conn.prepareStatement("INSERT IGNORE INTO comments (comment_id,article_id,html,vector) VALUES "+querry+";");
 			// 1, 123, '&lt;li class=&quot;js-item comment \&quot; data-page=\&quot;2-101\&quot; data-cmt=\&quot;1378839254408-50ba735c-6115-4b9b-add8-43b17b23c36c\&quot; data-uid=\&quot;22BLY5Z2UBYX76MGVO6XDM5HIY\&quot;&gt;\n        &lt;div class=\&quot;comment-container clearfix  \&quot;&gt;\n            &lt;div class=\&quot;actions\&quot;&gt;\n                        &lt;button tabindex=\&quot;0\&quot; class=\&quot;comments-button-small flag\&quot; title=\&quot;Report Abuse\&quot;&gt;&lt;\/button&gt;\n            &lt;\/div&gt;            &lt;div class=\&quot;img-container\&quot;&gt;\n                &lt;img src=\&quot;http:\/\/l.yimg.com\/dh\/ap\/social\/profile\/profile_b48.png\&quot; title=\&quot;P.T.Hawk\&quot; width=\&quot;48\&quot; height=\&quot;48\&quot;&gt;            &lt;\/div&gt;\n            &lt;div class=\&quot;container\&quot;&gt;\n                &lt;div class=\&quot;nickname\&quot;&gt;\n                        &lt;span tabindex=\&quot;0\&quot; class=\&quot;int profile-link \&quot; data-guid=\&quot;22BLY5Z2UBYX76MGVO6XDM5HIY\&quot;&gt;P.T.Hawk&lt;\/span&gt;\n                    &lt;span class=\&quot;comment-timestamp\&quot;&gt;21 hours ago&lt;\/span&gt;\n                    &lt;div tabindex=\&quot;0\&quot; id=\&quot;down-vote-box\&quot; class=\&quot;int vote-box down\&quot;&gt;\n                        &lt;span class=\&quot;count\&quot;&gt;15&lt;\/span&gt;\n                        &lt;i class=\&quot;comment-thumb-down\&quot;&gt;&lt;\/i&gt;\n                    &lt;\/div&gt;\n                    &lt;div tabindex=\&quot;0\&quot; id=\&quot;up-vote-box\&quot; class=\&quot;int vote-box up\&quot;&gt;\n                        &lt;span class=\&quot;count\&quot;&gt;200&lt;\/span&gt;\n                        &lt;i class=\&quot;comment-thumb-up\&quot;&gt;&lt;\/i&gt;\n                    &lt;\/div&gt;                &lt;\/div&gt;\n                &lt;div class=\&quot;text-container truncate\&quot;&gt;\n                    &lt;p class=\&quot;comment-content \&quot;&gt;\n                        If Assad&amp;#39;s declarations are sincere then this is good news no matter who get&amp;#39;s credit.\n                    &lt;\/p&gt;\n                    &lt;div class=\&quot;more-text-button \&quot;&gt;More&lt;\/div&gt;\n    \t    &lt;\/div&gt;\n                &lt;div class=\&quot;action-container clearfix\&quot;&gt;\n                            &lt;span tabindex=\&quot;0\&quot; class=\&quot;replies int\&quot; role=\&quot;button\&quot; data-state=\&quot;Collapse Replies (31)\&quot;&gt;\n                              Expand Replies (31)\n                            &lt;\/span&gt;\n                    &lt;span tabindex=\&quot;0\&quot; class=\&quot;reply int\&quot; role=\&quot;button\&quot;&gt;\n                        Reply\n                    &lt;\/span&gt;\n                    \n                &lt;\/div&gt;\n            &lt;\/div&gt;\n        &lt;\/div&gt;\n        &lt;div class=\&quot;reply-list-container\&quot;&gt;\n            &lt;ul class=\&quot;reply-list\&quot;&gt;\n                &lt;li class=\&quot;js-item reply\&quot; data-index=\&quot;1-1\&quot; data-reply=\&quot;0001ab000000000000000000000000-1aed7713-999d-4ffd-ae04-1d6922d73dee\&quot; data-uid=\&quot;LR5NS6TS2U4APYE2WKXZBPQZKQ\&quot;&gt;\n                    &lt;div class=\&quot;comment-container clearfix  \&quot;&gt;\n                        &lt;div class=\&quot;img-container\&quot;&gt;\n                &lt;img src=\&quot;http:\/\/l.yimg.com\/dg\/users\/1siARJgTDAAIBQWEo8IhtAg==.medium.png\&quot; title=\&quot;Daniel\&quot; width=\&quot;48\&quot; height=\&quot;48\&quot;&gt;                        &lt;\/div&gt;\n                        &lt;div class=\&quot;container\&quot;&gt;\n                            &lt;div class=\&quot;nickname\&quot;&gt;\n                                    &lt;span tabindex=\&quot;0\&quot; class=\&quot;int profile-link \&quot; data-guid=\&quot;LR5NS6TS2U4APYE2WKXZBPQZKQ\&quot;&gt;Daniel&lt;\/span&gt;\n                        &lt;span class=\&quot;comment-timestamp\&quot;&gt;18 hours ago&lt;\/span&gt;\n                        &lt;div tabindex=\&quot;0\&quot; id=\&quot;down-vote-box\&quot; class=\&quot;int vote-box down\&quot;&gt;\n                            &lt;span class=\&quot;count\&quot;&gt;0&lt;\/span&gt;\n                            &lt;i class=\&quot;comment-thumb-down\&quot;&gt;&lt;\/i&gt;\n                        &lt;\/div&gt;\n                        &lt;div tabindex=\&quot;0\&quot; id=\&quot;up-vote-box\&quot; class=\&quot;int vote-box up\&quot;&gt;\n                            &lt;span class=\&quot;count\&quot;&gt;0&lt;\/span&gt;\n                            &lt;i class=\&quot;comment-thumb-up\&quot;&gt;&lt;\/i&gt;\n                        &lt;\/div&gt;                            &lt;\/div&gt;\n                            &lt;div class=\&quot;text-container truncate\&quot;&gt;\n                                &lt;p class=\&quot;comment-content \&quot;&gt;\n                                    @ Jorge Lopez   Assad is in the process of creating a parliament to open up free elections in 2014. He cant have those elections until a parliament is formed .Look at the Barbara Walters interview with Assad. All of this is in the works way before this event. She also asked him about attacks on peaceful demonstrators. He claims he has no policy to attack them and there have been mistakes made by individual authorities in his country and when investigations find these things occurred people are held accountable. He is either a bigger and better liar than Obama and Bush or he tries to do the right thing in a bad situation .Watch it and you decide.\n                                &lt;\/p&gt;\n                                &lt;div class=\&quot;more-text-button \&quot;&gt;More&lt;\/div&gt;\n                \t    &lt;\/div&gt;\n                        &lt;\/div&gt;\n            &lt;div class=\&quot;actions\&quot;&gt;\n                        &lt;button tabindex=\&quot;0\&quot; class=\&quot;comments-button-small flag\&quot; title=\&quot;Report Abuse\&quot;&gt;&lt;\/button&gt;\n            &lt;\/div&gt;                    &lt;\/div&gt;\n                &lt;\/li&gt;', "1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0"
 			sqlQuerry.execute();
 		} catch (SQLException e) {e.printStackTrace();}
     	
 	
     }
     
 
     
     private static Map<String, String> getClustersChildrenIDs(String clusterID, int level, String articleID){
     	try {
 			Connection conn = DatabaseManager.getInstance().getConnection();
 			Map<String, String> childrenIDs = new HashMap<String, String>();
			String qryString = "SELECT comms.comment_id,html FROM comments comms, HACNodesMapping mapping WHERE comms.comment_id=mapping.comment_id AND comms.article_id=mapping.article_id AND mapping.article_id=? AND mapping.node_mapping = \""+clusterID+"_"+level+"\" AND direct=1";
 	    	PreparedStatement qry = conn.prepareStatement(qryString);
 			qry.setString(1, articleID);
 			ResultSet rs = qry.executeQuery();
 			
 			while(rs.next()){
 				childrenIDs.put(rs.getString("comment_id"), StringEscapeUtils.unescapeHtml4(rs.getString("html")));
 			}
 			
 			return childrenIDs;
 				
     	} catch (SQLException e) {
 			e.printStackTrace();
 		}
     	
     	return null;
     }
 
     public static Set<ClusterRepresentationDO> getClustersRepresentationByIDs(List<String> clusterIDs, int level, String articleID){
     	
     	
     	Map<String, String> children;
     	Set<String> childrenIDSet;
     	Set<ClusterRepresentationDO> repDOSet = new HashSet<ClusterRepresentationDO>();
     	String parsedLabel;
 
     		//Connection conn = DatabaseManager.getInstance().getConnection();
 /*
 	    	String qryString = "SELECT comments.* FROM comments comms, HACNodesMapping mapping WHERE comms.comment_id=mapping.comment_id AND mapping.article_id=? AND mapping.node_mapping IN (";
 	    	
 	    	for(String cid:clusterIDs){
 	    		qryString += "\""+cid+"_"+level+"\",";
 	    	}
 	    	
 	    	qryString = qryString.substring(0, qryString.lastIndexOf(","));
 	    	
 	    	qryString+=")";
 	    	
 	    
 	    	PreparedStatement qry = conn.prepareStatement(qryString);
 			qry.setString(1, articleID);
 			ResultSet rs = qry.executeQuery();
 			
 			level++;
 			*/
     		
 			for(String clusterID: clusterIDs) {
 				children = getClustersChildrenIDs(clusterID,level,articleID);
 				childrenIDSet = children.keySet();
 				parsedLabel = MarkupUtility.getCommentBodyFromMarkup(children.get(childrenIDSet.iterator().next()));
 				repDOSet.add(new ClusterRepresentationDO(clusterID, parsedLabel, childrenIDSet));
 			}
 		return repDOSet;
     }
     
     public static String getCommentsForGivenCluster(String articleID, String clusterID, int level, int from, int to){
     	
     	Connection conn;
     	StringBuilder sb = new StringBuilder();
 
     	try{
     		
     		conn = DatabaseManager.getInstance().getConnection();
 
	    	String qryString = "SELECT html FROM comments comms, HACNodesMapping mapping WHERE comms.comment_id=mapping.comment_id AND mapping.article_id=comms.article_id AND mapping.article_id=? AND mapping.node_mapping = ? LIMIT ?,?";
 	    	
 	    
 	    	PreparedStatement qry = conn.prepareStatement(qryString);
 			qry.setString(1, articleID);
 			qry.setString(2, clusterID+"_"+level);
 			qry.setInt(3, from);
 			qry.setInt(4, to);
 			ResultSet rs = qry.executeQuery();
 
 			while(rs.next()) {
 				sb.append(rs.getString("html"));
 			}
 
 		
     	}catch(SQLException e){
     		
     	}
     	
     	return StringEscapeUtils.unescapeHtml4(sb.toString());
     }
     
     /**
      * check if the article is existing in the DB
      * @param articleId
      * @return
      */
 	public static boolean checkArticleExitanceByID(String articleId){
     	
     	Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement sqlQuerry = conn.prepareStatement("SELECT article_id FROM articles WHERE article_id = ? ;");
 			sqlQuerry.setString(1, articleId);
 			ResultSet rs = sqlQuerry.executeQuery();
 			
 			while(rs.next()) {
 				return true;}
 		} catch (SQLException e) {e.printStackTrace();}
 		return false;
     }
 
     /**
      * get all the comments HTML for a given article ordered by comment id
      * @param articleId
      * @return
      */
 	public static ArrayList<String> getAllArticleCommentsHtml(String articleId) {
 		
 		Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement sqlQuerry = conn.prepareStatement("SELECT html FROM comments WHERE article_id = ? ORDER BY comment_id ASC;");
 			sqlQuerry.setString(1, articleId);
 			ResultSet rs = sqlQuerry.executeQuery();
 			ArrayList<String> returnArray = new ArrayList<String>();
 			
 			while(rs.next()) {
 				returnArray.add(StringEscapeUtils.unescapeHtml4(rs.getString("html")));
 			}
 			
 			return returnArray;
 			
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return null;
 	}
 	
 	public static CacheToken saveToCache(Cachable obj, CacheToken token){
 		return DatabaseObjectCacheImpl.save(obj, token);
 	}
 	
 	public static Cachable fetchFromCache(String cacheId){
 		return DatabaseObjectCacheImpl.fetch(cacheId);
 	}
 	
 	public static void removeFromCache(String cacheId){
 		DatabaseObjectCacheImpl.remove(cacheId);
 	}
 	
 	public static void clearCache(){
 		DatabaseObjectCacheImpl.clearCache();
 	}
 	
 	public static Set<ClusterRepresentationDO> getHACRootID(String articleID){
 		Connection conn;
 		try {
 			conn = DatabaseManager.getInstance().getConnection();
 
 	    	PreparedStatement qry = conn.prepareStatement("SELECT comment_id FROM HACNodesMapping WHERE node_mapping = CONCAT(comment_id,'_','0') AND article_id = ?");
 			qry.setString(1, articleID);
 			ResultSet rs = qry.executeQuery();
 			
 			List<String> clustersID =new  ArrayList<String>();
 			
 			while(rs.next()){
 				clustersID.add(rs.getString("comment_id"));
 				return getClustersRepresentationByIDs(clustersID, 0, articleID);
 			}
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return new HashSet<ClusterRepresentationDO>();
 		
 	}
 	
 	/**
 	 * return the number of the words that stored in the db for a given article
 	 * @param articleId
 	 * @return
 	 */
 	public static int getWordsCountForArticle(String articleId){
 		
 		try {
 			Connection conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement qry = conn.prepareStatement("SELECT * FROM article_words WHERE article_id = ?");
 			qry.setString(1, articleId);
 			
 			ResultSet rs  = qry.executeQuery();
 			
 			rs.last();
 			
 			return rs.getRow();
 			
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return 0;
 	}
 	
 	public static long getArticleCreationDate(String articleID){
 		try{
 			Connection conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement qry = conn.prepareStatement("SELECT creation_time FROM articles WHERE article_id = ?");
 			qry.setString(1, articleID);
 			ResultSet rs = qry.executeQuery();
 			while(rs.next())
 				rs.getLong("creation_time");
 		}
 		catch(SQLException e) {e.printStackTrace();}
 		return 0;
 	}
 	
 	/**
 	 * clear al the information about the article from the DB
 	 * @param articleId
 	 */
 	public static void cleaArticleFromDB(String articleId) {
 		
 		try {
 			Connection conn = DatabaseManager.getInstance().getConnection();
 			PreparedStatement stmt1 = conn.prepareStatement("DELETE FROM article_words WHERE article_id=\""+articleId+"\"");
 			stmt1.execute();
 			PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM HACNodesMapping WHERE article_id=\""+articleId+"\"");
 			stmt2.execute();
 			PreparedStatement stmt3 = conn.prepareStatement("DELETE FROM comments WHERE article_id=\""+articleId+"\"");
 			stmt3.execute();
 			PreparedStatement stmt4 = conn.prepareStatement("DELETE FROM articles WHERE article_id=\""+articleId+"\"");
 			stmt4.execute();
 			
 		} catch (SQLException e) {e.printStackTrace();}
 	}
 	
 	/**
 	 * replace the vectors for the existing comments
 	 * @param articleId
 	 * @param replacedVector
 	 */
 	public static void replaceVectorsForComments(String articleId, ArrayList<ArrayList<Double>> replacedVector) {
 		try {
 			Connection conn = DatabaseManager.getInstance().getConnection();
 			StringBuffer whenCases = new StringBuffer();
 			StringBuffer whereCase = new StringBuffer("WHERE `article_id` = ").append(articleId).append(" AND `comment_id` IN (");
 			int i=1;
 			for (ArrayList<Double> arrayList : replacedVector) {
 				whenCases.append("WHEN ").append(i).append(" THEN ").append("\"").append(CommentEntityDS.vectorToString(arrayList)).append("\"\n");
 				whereCase.append(i).append(",");
 				i++;
 			}	
 			whereCase = whereCase.replace(0, whereCase.length(), whereCase.substring(0, whereCase.length() - 1));
 			whereCase.append(")");
 			
 			PreparedStatement query = conn.prepareStatement("UPDATE comments SET `vector` = CASE `comment_id` "+whenCases+" \nEND \n "+whereCase);
 			query.execute();
 
 			//			UPDATE comments
 			//		    SET vector = CASE comment_id
 			//		        WHEN 1 THEN 'one'
 			//		        WHEN 2 THEN 'two'
 			//		        WHEN 3 THEN 'three'
 			//		    END
 			//		WHERE comment_id IN (1,2,3)
 			//		}
 
 		} catch (SQLException e) {e.printStackTrace();}
 	}
 }
