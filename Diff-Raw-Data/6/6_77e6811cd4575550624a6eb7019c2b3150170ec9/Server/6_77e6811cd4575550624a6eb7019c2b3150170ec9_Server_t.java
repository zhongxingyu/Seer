 package edu.nouri.photoReX;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat; 
 import java.util.*; 
 import redis.clients.jedis.*; 
 import redis.clients.jedis.exceptions.*; 
 import edu.nouri.photoReX.recommender.*; 
 
 
 public class Server {
 
 	private Jedis redis; 						//this is the redis client that receives recommendation publishes
 	private Learner learner; 
 	
 	public static void main(String[] args) {
 		
 		Server server = new Server(); 
 		server.run();  
 	}
 
 	public Server()
 	{
 		learner = new Learner(); 
 		redis = new Jedis("localhost");
 	}	
 	
 	public void run()
 	{
 		
 		System.out.println("Learning server started..."); 
 				
 		long sleepTime = 1000; 		//how many miliseconds to wait before reconnecting to redis server
 		while(true)
 		{
 	
 			try{
 				RecommendationTask task = new RecommendationTask( redis.blpop(0, "users:recommend:queue")); 
 				System.out.print("recommendingg " + task.pageCount + " pics for '" + task.username + "' ... "); 
 				long start = System.currentTimeMillis();
 				ArrayList<RecommendationPage> recs = learner.recommend(task); 
 				
 				for(int i=0; i< recs.size(); i++)
 				{
 					long page = redis.incr("counter:user:" + task.username + ":page" ); 
 					RecommendationPage rec = recs.get(i); 
 					rec.pageid = page; 
 					
 					
 					//add info to various structures in redis: 
 					Pipeline p = redis.pipelined(); 
 					p.rpush("user:"+task.username + ":queue", rec.toJson()); 	//add json rep to the end of user queu
 	
 					//add structured data to redis: 
 					String userVisitedKey = "user:" + task.username + ":visited";
 					String pageKey        = "user:" + task.username + ":page:" + rec.pageid + ":pics"; 
 					for(int j=0; j< rec.pics.size(); j++)
 					{
 						String hash = rec.pics.get(j).picture.toHash(); 
 						p.sadd(userVisitedKey, hash);						//user visited this photo
 						
 						String picVisitedKey  = "pic:" +  hash + ":visited"; 
 						p.sadd(picVisitedKey, task.username); 				//picture was visited by this user
 						
 						p.rpush(pageKey, hash); 								//page contains this picture
 						p.set("pic:" + hash, rec.pics.get(j).picture.toJson()); //remember to call toHash before calling toJson (bad design really)
 					}
 					
 					p.sync(); 				//wait for commands to execute
 				}
 				long end = System.currentTimeMillis(); 
 				System.out.println("done in " + (end-start)/1000.0 + "seconds."); 
 				
 				sleepTime = 1000; //reset sleepTime because we were successful
 				//send pic to redis both as complete json to be passed to client and detailed structured for analysis 
 			}
 			catch(JedisConnectionException jce)
 			{
 				try
 				{
 					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 					Date date = new Date();
 					redis.disconnect(); 
					System.out.println(dateFormat.format(date) + ": redis was closed. trying to reconnect in " + (int)(sleepTime/1000) + " seconds..."); 
					Thread.sleep(sleepTime);
					sleepTime = (long) Math.min(60000, sleepTime*1.5); 
					redis.connect(); 
 				}
 				catch(Exception e)
 				{
 					//wait some time 
 //					Thread.sleep(10000); 
 				}
 			}
 		}
 	}
 	
 }
