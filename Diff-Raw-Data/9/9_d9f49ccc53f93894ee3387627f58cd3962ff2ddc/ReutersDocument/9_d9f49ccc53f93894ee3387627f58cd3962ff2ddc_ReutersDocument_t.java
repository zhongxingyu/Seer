 import java.util.*;
 
 //Represents a reuters document.
 
 public class ReutersDocument {
 
 	//attribute values
 	private int docid;
 	private String TOPICS;	//this is more of a boolean 
 	private String LEWISSPLIT;
 	private String CGISPLIT;
 	private String OLDID;
 	private String NEWID;
 	
 	//element values
 	private String TITLE;
 	private String DATELINE;
 	private String TEXT;
 	private String[] TITLETOKENS;
 	private String[] BODYTOKENS;
 	private Boolean TESTTRAINING;
 	
 	//these are also elements, but have been separated as they are an "aftermarket addition"
 	List<String> TOPICLIST = new ArrayList<String>();
 	
 	//Constructor for initial parsing.
 	public ReutersDocument (String topics, String lewissplit, String cgisplit, String oldid, String newid, String title, String dateline, String text) {
 
 		this.TOPICS = topics;
 		this.LEWISSPLIT = lewissplit;
 		this.CGISPLIT = cgisplit;
 		this.OLDID = oldid;
 		this.NEWID = newid;
 		this.TITLE = title;
 		this.DATELINE = dateline;
 		this.TEXT = text;
 
 	}
 	
 	//Constructor for tokenized document.
 	
 	public ReutersDocument (String lewissplit, String cgisplit, String[] titleTokens, String[] bodyTokens) {
 		
 		this.TITLETOKENS = titleTokens;
 		this.BODYTOKENS = bodyTokens;
 	
 	}
 	
 	public ReutersDocument() {}
 	
 	public void settitleTokens(String[] titleTokens) {
 	
 		TITLETOKENS = titleTokens;
 	
 	}
 	
 	public void setbodyTokens(String[] bodyTokens) {
 	
 		BODYTOKENS = bodyTokens;
 	
 	}
 	
 	public void setTopics(String topics) {
 	
 		TOPICS = topics;
 		
 	}
 	
 	public void setLewis(String lewis) {
 	
 		LEWISSPLIT = lewis;
 	
 	}
 	
 	public void setCgi(String cgi) {
 	
 		CGISPLIT = cgi;
 	
 	}
 	
 	public void setoldid(String old) {
 	
 		OLDID = old;
 	
 	}
 
 	public void setnewid(String newid) {
 	
 		NEWID = newid;
 	
 	}
 	
 	public void setTitle(String title) {
 	
 		TITLE = title;
 	
 	}
 	
 	public void setDateline(String dateline) {
 	
 		DATELINE = dateline;
 	
 	}
 	
 	public void setText(String text) {
 	
 		TEXT = text;
 	
 	}
 	
 	public String getTopics() {
 	
 		return TOPICS;
 	
 	}
 	
 	public String getLewis() {
 	
 		return LEWISSPLIT;
 	
 	}
 	
 	public String getCgi() {
 	
 		return CGISPLIT;
 	
 	}
 	
 	public String getoldid() {
 	
 		return OLDID;
 	
 	}
 
 	public String getnewid() {
 	
 		return NEWID;
 	
 	}
 	
 	public String getTitle() {
 	
 		return TITLE;
 	
 	}
 	
 	public String getDateline() {
 	
 		return DATELINE;
 	
 	}
 	
 	public String getText() {
 	
 		return TEXT;
 	
 	}
 	
 	public String[] gettitleTokens() {
 	
 		return TITLETOKENS;
 	
 	}
 	
 	public String[] getbodyTokens() {
 	
 		return BODYTOKENS;
 	
 	}
 	
 	public void addTopic(String topic) {
 		TOPICLIST.add(topic);
 	}
 	
 	public String getTopicList() {
 		String ret = ""; 
 		
 		int i = 0;
 		
 		for (String topic : TOPICLIST) {
			/*if (i != 0) {
 				ret = ret + ":";
 			}
 			
 			ret = ret + topic; 
			*/
			
			//temporarily only use the first topic
			if(i==0) {
				ret = topic; 
			}
 			
 			i++;
 		}
 		
 		return ret; 
 	}
 	
 	
 	//aftermarket additions (may need to be deleted)
 	/*public void setTopicList(String topic) {
 		TOPICLIST = topic; 
 	}
 	
 	public String getTopicList() {
 		return TOPICLIST; 
 	}*/
 }
 
 
 
 
 
