 package swarm;
 
 import java.util.List;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement; 
 
 import weibo4j.Comments;
 import weibo4j.examples.Log;
 import weibo4j.model.Comment;
 import weibo4j.model.Paging;
 
 public class GetComments implements Runnable{	
 	public boolean getStatusComments() 
 	{
 		boolean isCommentsDone = true;
 		Connection conStatus = null;
		Statement stmt = null;
 		ResultSet rset = null;
 		System.out.println(" Will connect to the database and get status......."); 
 		try {
 			conStatus = PublicMethods.getConnection();
 			System.out.println(" Okay the connnection to mysql has been established..we are going to select id from status.....");
 			stmt = conStatus.createStatement(
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_UPDATABLE);
 			rset = stmt.executeQuery("select id, commentCounts from status where isDone = false limit 100");  
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
 
 		int pageNum = 1;		
 		int commentCounts = 0;
 		long statusId = (long)0;  
 		int count = 200;
 		
 		try {
 			while(rset.next())
 			{
 				isCommentsDone = false;
 				statusId = rset.getLong(1);
 				commentCounts = rset.getInt(2);
 				if(commentCounts == 0 )		
 				{
 					//if no comment, skip this status
 					System.out.println("oops no comments here~~~");
 				}
 				else		//if there are comments of this status
 				{
 					Paging pag;
 					try {
 						pag = new Paging();
 						pag.setCount(count);
 						pag.setPage(pageNum);
 
 						Comments cm =new Comments();
 						List<Comment> comment = null;
 						try
 						{
 							comment = cm.getCommentById(statusId+"",pag,count,0);						
 						} catch (Exception e) {
 							//may be there is an exception here, but who knows ,it depends on sina
 							System.out.println("WTF, there IS an exception that actually happened!");
 						}
 						
 						
 						if(comment != null)		//just in case ,u know, to be safe is always good
 						{
 							for(Comment c : comment){
 								//Log.logInfo(c.toString());
 								PublicMethods.insertCommentsSql(conStatus, c, statusId);
 							}							
 						}
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}
 
 				//okay ,set isDone of this status = 1, which means the comments of this status have been stored
 				PublicMethods.updateStatusComments(conStatus, statusId);
 				
 			}
 		} catch (SQLException e) {
 			isCommentsDone = false;
 			e.printStackTrace();
 		}
 		
 		
 		//close the connection to mysql
 		try {
			rset.close();
			stmt.close();
 			conStatus.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return isCommentsDone;
 	}
 
 	@Override
 	public void run() {
 		while(getStatusComments() == false);
 
 		System.out.println("LALALALa ,all users' comments have been stored~~"); 
 	}
 
 }
