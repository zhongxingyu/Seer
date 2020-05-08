 package swarm;
 
 import java.util.List;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement; 
 
 import weibo4j.Timeline;
 import weibo4j.Users;
 import weibo4j.examples.Log;
 import weibo4j.http.Response; 
 import weibo4j.model.IDs;
 import weibo4j.model.Paging;
 import weibo4j.model.Status;
 import weibo4j.model.User;
 import weibo4j.model.WeiboException;
 
 public class GetUserStatus implements Runnable{
 	 	
 	public boolean getUserStatus() 
 	{
 		boolean isStatusDone = true;
 		Connection conUser = null;
		Statement stmt = null;
 		ResultSet rset = null;
 		try {
 			conUser = PublicMethods.getConnection();
 			stmt = conUser.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
 			System.out.println(" Okay the connnection to mysql has been established..we are going to select id from users.....");
 			rset = stmt.executeQuery("select id,statusesCount from users where isStatusDone = 0 limit 50;");
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		Long userId = (long)0;
 		int statusesCount = 0;
 		Timeline tm = new Timeline();
 		boolean isUserExist = true; 
 		
 		try {
 			while(rset.next())
 			{
 				isStatusDone = false;
 				isUserExist = true;
 				userId = rset.getLong(1);
 				System.out.println("userId: "+ userId);
 				int nextCursor=1;
 				int count = 200;
 				int pageNum = 0;
 				
 				Paging pag = new Paging();
 				pag.setPage(1);
 				
 				Users um = new Users();
 				try{
 					User user = um.showUserById(userId+"");
 					//System.out.println(user.toString());
 				} catch(WeiboException e){
 					//e.printStackTrace();
 					String errorMsg = null;
 					errorMsg = e.getError();
 					//System.out.println("User: "+errorMsg);
 					if((errorMsg != null) && (errorMsg.isEmpty() == false) && errorMsg.contains("User does not exist"))
 					{
 						isUserExist = false;
 					} 
 				}
 				
 				if(isUserExist)   //if this user still exists
 				{
 					while(true)		//get the user's statuses
 					{			
 						List<Status> status = null;
 						IDs ids = null;
 						try {
 							Response res = tm.getUserTimelineRes(userId+"", "",count, pag, 0, 0);
 							ids =new IDs(res);
 							status = tm.getUserTimelineWH(res);
 							//status = tm.getUserTimeline(userId + "", "",count, pag, 0, 0);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 						if(ids != null)
 						{
 							nextCursor = (int) ids.getNextCursor();
 						}
 						else
 						{
 							nextCursor++;
 						}
 						System.out.println("nextCursor: "+nextCursor+" Pagenum: "+pageNum);
 						try {
 							Thread.sleep(1111);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						 
 						pageNum++;
 						
 						if(status == null)		//just in case that status may be null...
 						{
 							if(pageNum == 2)
 							{ 
 								break;
 							}
 							else
 							{
 								continue;
 							}
 						} 
 						else if(status.isEmpty() || (pageNum == 3))		//well stops when finished all the user's status or pageNum*count 
 						{
 							//set this user to be the finished one in terms of status
 							PublicMethods.updateUserStatus(conUser, userId); 
 							break;
 						}
 						else
 						{
 							for(Status s : status){
 								//Log.logInfo(s.toString());
 								System.out.println("status Id: "+s.getId()+" rtCounts: "+s.getRepostsCount()+" commentsCount: "+s.getCommentsCount());
 								PublicMethods.insertStatusSql(conUser, s);
 								
 								//well ,if there is a retweeted status in this status ,store the retweetedStatus and the original user
 								Status retweetedStatus = null;
 								retweetedStatus = s.getRetweetedStatus();
 								if(retweetedStatus != null)		
 								{
 									PublicMethods.insertStatusSql(conUser, retweetedStatus);
 									User user = null;
 									user = retweetedStatus.getUser();
 									if(user != null)
 									{
 										PublicMethods.insertUserSql(user, conUser);
 									}
 								}
 							}  
 						}	
 						
 						if(nextCursor > 0)		//page should be positive integer 
 						{
 							pag.setPage(nextCursor);	
 						}
 						else
 						{ 
 							//set this user to be the finished one in terms of status
 							PublicMethods.updateUserStatus(conUser, userId); 
 							break;
 						}
 					}
 					
 					//sleep for a while
 					try {
 						Thread.sleep(551);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				else		//if this user no longer exists, delete it from database
 				{
 					PublicMethods.deleteUser(conUser, userId);
 				} 
 				
 					
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
			rset.close();
			stmt.close();
 			conUser.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return isStatusDone;
 
 	}
 
 	@Override
 	public void run() {
 		while(getUserStatus() == false);
 		System.out.println("LALALALa ,getting users status is done~~"); 
 	}
 }
