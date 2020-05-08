 package no.uio.inf5750.assignment3.util;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.TreeMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 
 import android.util.Log;
 
 
 public class UpdateDaemon {
 	private static UpdateDaemon mDaemon;
 	private JSONObject mJson;
 	private LinkedList<Interpretation> mInterpretations;
 	private LinkedList<Message> mMessages;
 	private HashMap<String, User> mUsers;
 	private String prevJson = "";
 
 	static {
 		mDaemon = new UpdateDaemon();
 	}
 
 	public static UpdateDaemon getDaemon() {
 		return mDaemon;
 	}
 
 	private UpdateDaemon() {
 		mJson = null;
 		mMessages = new LinkedList<Message>();
 		mInterpretations = new LinkedList<Interpretation>();
 		mCharts = new LinkedList<Chart>();
 		mUsers = new HashMap<String, User>();
 	}
 
 	public void update() {
 		String jsonContent = ConnectionManager.getConnectionManager().doRequest("http://apps.dhis2.org/dev/api/currentUser/inbox");
 		if (jsonContent.equals(prevJson)) {
 			return;
 		}
 		prevJson = jsonContent;
 		try {
 			mJson = new JSONObject(jsonContent);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			mJson = null;
 			e.printStackTrace();
 			return;
 		}
 		mMessages.clear();
 		mInterpretations.clear();
 		int numMsg = getJSONLength("messageConversations");
 		for (int i = 0; i < numMsg; i++) {
 			Message msg = new Message();
 			msg.mTitle = getMessageTitle(i);
 			msg.mId = getMessageId(i);
 			msg.mLastUpdated = getMessageLastMessage(i);
 			// TODO: replace this once json is updated.
 			msg.mUser = addOrGetUser("asdasd", "Someone");
 			mMessages.add(msg);
 		}
 		
 		int numInt = getJSONLength("interpretations");
 		for (int i = 0; i < numInt; i++) {
 			Interpretation inter = new Interpretation();
 			inter.mId = getInterpretationsId(i);
 			inter.mLastUpdated = getInterpretationsLastUpdated(i);
 			inter.mUser = addOrGetUser((JSONObject) getProperty("interpretations", i, "user"));
 			inter.mText = getInterpretationsText(i);
 			inter.mCreated = getInterpretationsCreated(i);
 			addComments(inter.mCommentThread, "interpretation", i);
 			addInfoNode(inter.mInfo, i, "map");
 			addInfoNode(inter.mInfo, i, "chart");
 			mInterpretations.add(inter);
 		}
 		
 	}
 	
 	private int getJSONLength(String group) {
 		if (mJson == null) {
 			update();
 		}
 		int ret;
 		try {
 			ret = mJson.getJSONArray(group).length();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			ret = 0;
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	private void addInfoNode(LinkedList<InterpretationInfoNode> nodes, int id, String type) {
 		try {
 			JSONObject chartJson = mJson.getJSONArray("interpretations").getJSONObject(id).getJSONObject(type);
 			InterpretationInfoNode node = new InterpretationInfoNode();
 			node.mId = (String) chartJson.get("id");
 			node.mName = (String) chartJson.get("name");
 			node.mLastUpdated = (String) chartJson.get("lastUpdated");
 			node.mChart = type.equals("chart");
 			nodes.add(node);
 		} catch (JSONException e) {
 			return;
 		}
 	}
 	
 	private void addComments(LinkedList<Comment> comments, String category, int id) {
 		comments.clear();
 		JSONArray jsonComments = null;
 		try {
 			jsonComments = mJson.getJSONArray("interpretations").
 					getJSONObject(id).getJSONArray("comments");
 		} catch (JSONException e) {
 			return;
 		} 
 		try {
 			for (int i = 0; i < jsonComments.length(); i++) {
 				Comment tmp = new Comment();
 				tmp.mText = (String) jsonComments.getJSONObject(i).get("text");
 				tmp.mUser = addOrGetUser(jsonComments.getJSONObject(i).getJSONObject("user"));
 				tmp.mLastUpdated = jsonComments.getJSONObject(i).getString("lastUpdated");
 				tmp.mCreated = jsonComments.getJSONObject(i).getString("created");
 				tmp.mId = jsonComments.getJSONObject(i).getString("id");
 				comments.add(tmp);
 			}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private User addOrGetUser(String id, String name) {
 		if (mUsers.containsKey(id)) {
 			return mUsers.get(id);
 		}
 		User tmp = new User();
 		tmp.mId = id;
 		tmp.mName = name;
 		return tmp;
 	}
 	
 	private User addOrGetUser(JSONObject o) {
 		try {
 			return addOrGetUser(o.getString("id"), o.getString("name"));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public Message getMessage(int id) {
 		return mMessages.get(id);
 	}
 	
 	public Interpretation getInterpretation(int id) {
 		return mInterpretations.get(id);
 	}
 	
 	public LinkedList<Message> getMessages() {
 		return (LinkedList<Message>) mMessages.clone();
 	}
 	
 	public LinkedList<Interpretation> getInterpretations() {
 		return (LinkedList<Interpretation>) mInterpretations.clone();
 	}
 	
 	public int getUnreadMessages() {
 		if (mJson == null) {
 			update();
 		}
 		int s = 0;
 		for (Message msg : mMessages) {
 			if (!msg.mRead) {
 				s++;
 			}
 		}
 		return s;
 	}
 	
 	private Object getProperty(String array, int id, String property) {
 		
 		if (mJson == null) {
 			update();
 		}
 		Object ret;
 		try {
 			ret = mJson.getJSONArray(array).getJSONObject(id).get(property);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			ret = null;
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	public Boolean getBooleanProperty(String array, int id, String property) {
 		return (Boolean) getProperty(array, id, property);
 	}
 	
 	public String getStringProperty(String array, int id, String property) {
 		return (String) getProperty(array, id, property);
 	}
 
 	private String getMessageName(int id) {
 		return getStringProperty("messageConversations", id, "name");
 	}
 	
 	// requires updating once the latest version of the json-stream is out
 	private String getMessageTitle(int id) {
 		return getStringProperty("messageConversations", id, "name");
 	}
 	
 	private String getMessageLastMessage(int id) {
 		return getStringProperty("messageConversations", id, "lastMessage");
 	}
 	
 	private String getInterpretationsId(int id) {
 		return getStringProperty("interpretations", id, "name");
 	}
 	
 	private String getInterpretationsText(int id) {
 		return getStringProperty("interpretations", id, "text");
 	}
 	
 	private String getInterpretationsCreated(int id) {
 		return getStringProperty("interpretations", id, "created");
 	}
 	
 	private String getInterpretationCreator(int id) {
 		if (mJson == null) {
 			update();
 		}
 		String ret;
 		try {
 			ret = (String) mJson.getJSONArray("interpretations").
 					getJSONObject(id).getJSONObject("user").get("name");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			ret = "";
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	private String getInterpretationsLastUpdated(int id) {
 		return getStringProperty("interpretations", id, "lastUpdated");
 	}
 	
 	
 	private String getMessageId(int id) {
 		return getStringProperty("messageConversations", id, "id");
 	}
 
 	public int getNumberOfMessages() {
 		return mMessages.size();
 	}
 	
 	public int getNumberOfInterpretations() {
 		return mInterpretations.size();
 	}
 	
 	public LinkedHashMap<String, String> getMessage(String url) {
 		LinkedHashMap<String, String> info = new LinkedHashMap<String, String>();
 		String messageList = ConnectionManager.getConnectionManager()
 				.doRequest(url);
 		Document doc = Util.getDomElement(messageList);
 		if (doc == null)
 			return null;
 		NodeList list = doc.getChildNodes();
 		NamedNodeMap mapMessageConversation = list.item(0).getAttributes();
 		info.put("title", mapMessageConversation.getNamedItem("name")
 				.getNodeValue());
 
 		NodeList messages = list.item(0).getChildNodes();
 		for (int i = 0; i < messages.getLength(); i++) {
 			if (messages.item(i).getNodeName().equals("messages")) {
 				NodeList message = messages.item(i).getChildNodes();
 				for (int j = 0; j < message.getLength(); j++) {
 					if(message.item(j).getNodeName().equals("message")){
 						NamedNodeMap textAndDate = message.item(j).getAttributes();
 						info.put(j+"text", textAndDate.getNamedItem("name").getNodeValue());
 						info.put(j+"date", textAndDate.getNamedItem("lastUpdated").getNodeValue().substring(0, 10));
 						NodeList sender = message.item(j).getChildNodes();
 						for(int k = 0; k < sender.getLength(); k++){
 							if (sender.item(k).getNodeName().equals("sender")) {
 								info.put(j+"sender", sender.item(k).getAttributes()
 										.getNamedItem("name").getNodeValue());
 							}
 						}
 					}
 					
 				}
 
 			}
 		}
 		return info;
 	}
 
 	private LinkedList<Chart> mCharts;
 	private String prevJsonCharts = "";
 
	// Temporary fix, until user-settings are available:
 	public void updateCharts() {
		String jsonContent = ConnectionManager.getConnectionManager().doRequest("http://apps.dhis2.org/dev/api/charts.json");
 		if (jsonContent.equals(prevJsonCharts)) {
 			return;
 		}
 		prevJsonCharts = jsonContent;
 		try {
 			mJson = new JSONObject(jsonContent);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			mJson = null;
 			e.printStackTrace();
 			return;
 		}
 
 		mCharts.clear();
 
 		int numChart = getJSONLength("charts");
 		for (int i = 0; i < numChart; ++i) {
 			Chart chart = new Chart();
 			chart.mName = getChartsName(i);
 			chart.mLastUpdated = getChartsLastUpdated(i);
 			chart.mHref = getChartsHref(i);
 			chart.mId = getChartsId(i);
 			chart.mImageHref = chart.mHref.concat("/data");
			mCharts.add(chart);
 		}
 	}
 	
 	private String getChartsName(int id) {
 		return getStringProperty("charts", id, "name");
 	}
 	
 	private String getChartsLastUpdated(int id) {
 		return getStringProperty("charts", id, "lastUpdated");
 	}
 	
 	private String getChartsHref(int id) {
 		return getStringProperty("charts", id, "href");
 	}
 	
 	private String getChartsId(int id) {
 		return getStringProperty("charts", id, "id");
 	}
 	
 	public int getNumberOfCharts() {
 		return mCharts.size();
 	}
 
 	public void repopulateSortedChartImageHrefTree(TreeMap<String, String> chartURLTree) {
 		chartURLTree.clear();
 		for (Chart c : mCharts) {
 			chartURLTree.put(c.mName, c.mImageHref);
 		}
 	}
 }
