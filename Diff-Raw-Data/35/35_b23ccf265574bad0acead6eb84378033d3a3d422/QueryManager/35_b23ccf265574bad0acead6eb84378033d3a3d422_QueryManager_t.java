 package recommender.querying;
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
import org.terrier.matching.ResultSet;

 import recommender.beans.IRStory;
 import recommender.dataaccess.ConnectionManager;
 import recommender.dataaccess.RetrievalManager;
 import recommender.dataaccess.StoryDAO;
 import recommender.dataaccess.TerrierManager;
 import recommender.utils.DBUtil;
 
 public class QueryManager {
 	
 	RetrievalManager retrievalManager;
 	
 	public QueryManager() {
 		super();
 		this.retrievalManager = new RetrievalManager(
 				new TerrierManager(
 						System.getProperty(TerrierManager.TERRIER_SEARCH_INDEX_PATH),"data"));
 	}
 
 	public QueryResult search(String query, Integer offset, Integer limit) {
 		QueryResult result =  new QueryResult();
     	try
     	{
			//List<IRStory> stories = retrievalManager.searchStories(query, offset, limit);
    		ResultSet rs = retrievalManager.search(query);
    		List<IRStory> stories = retrievalManager.getStoriesFromResultSet(rs).subList(
    				offset, Math.min(offset+limit-1, rs.getExactResultSize()-1));
    		
    		result.setComplete_size(rs.getExactResultSize()-1);
     		result.setStories(stories);
     		result.setOffset(offset.intValue());
     		result.setResults_per_page(limit.intValue());
     		
     	}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		// if this is reached, then this is an error case. Should return null?
 		return result;
 	}
 	
 //	public QueryResult search(String query, Integer limit, Integer offset) {
 //		// TODO: Make the real search!!!
 //		QueryResult result =  new QueryResult();
 //		int total = 87;
 //		result.setComplete_size(total);
 //		result.setStories(new ArrayList<IRStory>());
 //		result.setOffset(offset.intValue());
 //		result.setResults_per_page(limit.intValue());
 //		
 //		for(int i = 0; i < Math.min(limit.intValue(), total); i++) {
 //			IRStory story = new IRStory();
 //			story.setId(new Random().nextLong());
 //			story.setTitle("TITLE " + story.getId());
 //			story.setText("story " + story.getId() + ", story " + story.getId() + ", story " + story.getId() + ", story " + story.getId() + ", story " + story.getId() + ", story " + story.getId() + ", story " + story.getId());
 //			result.getStories().add(story);
 //		}
 //		
 //		return result;
 //	}
 	
 //	public List<IRStory> searchWithTerrier(String query, Integer start, Integer limit) {
 //		List<IRStory> result = new ArrayList<IRStory>();
 //		
 //    	try
 //    	{
 //			RetrievalManager retManager = new RetrievalManager(
 //					new TerrierManager(
 //							System.getProperty(TerrierManager.TERRIER_SEARCH_INDEX_PATH),"data"));
 //            result = retManager.searchStories(query, start, limit);
 //    	}
 //		catch(Exception ex) {
 //			ex.printStackTrace();
 //		}
 //		
 //		// if this is reached, then this is an error case. Should return null?
 //		return result;
 //	}
 	
 //	public List<IRStory> search2(String query) {
 //		List<IRStory> result = new ArrayList<IRStory>();
 //		StoryDAO dao = new StoryDAO();
 //		
 //		// TODO: Make the real search!!!
 //		Connection connection = null;
 //		PreparedStatement stmt = null;
 //		java.sql.ResultSet rs = null;
 //		String sqlquery = "";
 //		
 //		try
 //		{
 //			//IRStory result = null;
 //			
 //			sqlquery = "select s.*, c.`Text` " +
 //					"from story as s inner join content as c on c.id = s.contentId " + 
 //					"where title LIKE ? OR text LIKE ?";
 //			connection = ConnectionManager.getInstance().getConnection();
 //			stmt = connection.prepareStatement(sqlquery);
 //			stmt.setString(1, "%" + query + "%");
 //			stmt.setString(2, "%" + query + "%");
 //			
 //			rs = stmt.executeQuery();
 //			
 //			while(rs.next()) {
 //				result.add(dao.loadStory(rs));
 //			}
 //			
 //			return result;
 //		}
 //		catch(Exception ex) {
 //			
 //			System.out.println(sqlquery);
 //			ex.printStackTrace();
 //		}
 //		finally {
 //			if(rs != null)
 //				DBUtil.closeResultSet(rs);
 //			DBUtil.closeStatement(stmt);
 //			DBUtil.closeConnection(connection);
 //		}
 //		
 //		// if this is reached, then this is an error case. Should return null?
 //		return result;
 //	}
 	
 
 	
 	
 	/**
 	 * Class to store the results and the related metadata
 	 * @author andres
 	 *
 	 */
 	public static class QueryResult implements Serializable {
 		
 		private static final long serialVersionUID = 201301031842L;
 		
 		private int complete_size;
 		private List<IRStory> stories;
 		private int offset;
 		private int results_per_page;
 		
 		/**
 		 * Default Constructor
 		 */
 		public QueryResult() {
 			
 		}
 
 		/**
 		 * @return the complete_size
 		 */
 		public int getComplete_size() {
 			return complete_size;
 		}
 
 		/**
 		 * @param complete_size the complete_size to set
 		 */
 		public void setComplete_size(int complete_size) {
 			this.complete_size = complete_size;
 		}
 
 		/**
 		 * @return the stories
 		 */
 		public List<IRStory> getStories() {
 			return stories;
 		}
 
 		/**
 		 * @param stories the stories to set
 		 */
 		public void setStories(List<IRStory> stories) {
 			this.stories = stories;
 		}
 
 		/**
 		 * @return the offset
 		 */
 		public int getOffset() {
 			return offset;
 		}
 
 		/**
 		 * @param offset the offset to set
 		 */
 		public void setOffset(int offset) {
 			this.offset = offset;
 		}
 
 		/**
 		 * @return the results_per_page
 		 */
 		public int getResults_per_page() {
 			return results_per_page;
 		}
 
 		/**
 		 * @param results_per_page the results_per_page to set
 		 */
 		public void setResults_per_page(int results_per_page) {
 			this.results_per_page = results_per_page;
 		}
 	}
 }
