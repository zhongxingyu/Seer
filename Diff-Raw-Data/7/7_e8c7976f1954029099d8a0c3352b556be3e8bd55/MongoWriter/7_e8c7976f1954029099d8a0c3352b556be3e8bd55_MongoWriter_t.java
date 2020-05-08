 package aic.data;
 
 import java.net.UnknownHostException;
 import java.util.HashSet;
 import java.util.Set;
 
 import aic.data.dto.Tweet;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.Mongo;
 
 public class MongoWriter implements ITweetWriter {
 
 	private Mongo mongo;
 
 	private DB db;
 
 	private DBCollection tweetsCollection;
 	
 	private Set<String> noiseWords=new HashSet<String>();
 
 	public MongoWriter(String host, String db) {
 		try {
 			this.mongo = new Mongo(host);
 			this.db = mongo.getDB(db);
 			this.tweetsCollection = this.db.getCollection("tweets");
 			//removes everything from the collection
 			this.tweetsCollection.remove(new BasicDBObject());
 		} catch (UnknownHostException e) {
 			throw new RuntimeException(e);
 		}
 		
 		
 		
 		//copied from http://drupal.org/node/1202
 		String noiseList="about,after,all,also,an,and,another,any,are,as,at,be,because,been,before" + 
 						 "being,between,both,but,by,came,can,come,could,did,do,each,for,from,get" + 
 						 "got,has,had,he,have,her,here,him,himself,his,how,if,in,into,is,it,like" + 
 						 "make,many,me,might,more,most,much,must,my,never,now,of,on,only,or,other" + 
 						 "our,out,over,said,same,see,should,since,some,still,such,take,than,that" + 
 						 "the,their,them,then,there,these,they,this,those,through,to,too,under,up" + 
 						 "very,was,way,we,well,were,what,where,which,while,who,with,would,you,your,a" + 
 						 "b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,$,1,2,3,4,5,6,7,8,9,0,_";
 		for(String word : noiseList.split(",")){
 			noiseWords.add(word);
 		}
 	}
 
 	@Deprecated
 	public MongoWriter(String db) {
 		this("localhost", db);
 	}
 	
 	private String[] getKeywords(String text){
 		Set<String> out=new HashSet<String>();
 		//split on word boundary
		String[] words=text.split("\\W+");
 		for(String word : words){
			word=word.toLowerCase().trim();
			if(word.length()>2 && !"".equals(word) && !noiseWords.contains(word)){
 				out.add(word);
 			}
 		}
 		return out.toArray(new String[0]);
 	}
 
 	@Override
 	public void write(Tweet tweet) {
 		BasicDBObject dbo = new BasicDBObject();
 		dbo.put("name", tweet.getUsername());
 		dbo.put("text", tweet.getText());
 		dbo.put("keywords", getKeywords(tweet.getText()));
 		dbo.put("sentiment", tweet.getSentiment());
 		this.tweetsCollection.insert(dbo);
 		this.tweetsCollection.ensureIndex( new BasicDBObject("keywords", 1) );
 	}
 }
