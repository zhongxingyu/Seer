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
 
 /**
  * This class is used to keep store information about interpretations and conversations
  * @author Jan Anders Bremer
  */
 public class UpdateDaemon {
 	private static UpdateDaemon mDaemon;
 	private JSONObject mJson;
 	private LinkedList<Interpretation> mInterpretations;
 	private LinkedList<Message> mMessages;
 	private HashMap<String, User> mUsers;
 	private String prevJson = "";
 
 	/**
 	 * Makes this class a singleton
 	 */
 	static {
 		mDaemon = new UpdateDaemon();
 	}
 
 	/**
 	 * Returns the Daemon-instance
 	 * @return
 	 */
 	public static UpdateDaemon getDaemon() {
 		return mDaemon;
 	}
 
 	/**
 	 * Private constructor initiating the default data structures
 	 */
 	private UpdateDaemon() {
 		mJson = new JSONObject();
 		mMessages = new LinkedList<Message>();
 		mInterpretations = new LinkedList<Interpretation>();
 		mUsers = new HashMap<String, User>();
 	}
 
 	/**
 	 * Retrieves the latest version of the messages and interpretations
 	 */
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
 
 	/**
 	 * Used for determining the length of a certain JSON-array
 	 * @param group array name
 	 * @return The length of the array
 	 */
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
 
 	/**
 	 * Adds Interpretation info nodes containing information about the data shown in the interpretation
 	 * @param nodes Data structure which should contain the nodes
 	 * @param id The interpretation id
 	 * @param type Interpretation type
 	 */
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
 
 	/**
 	 * Adds comments to Interpretations (and soon messages!)
 	 * @param comments Comments are added to this structure
 	 * @param category Determines which json-object to inspect
 	 * @param id The id of the object in the category-array
 	 */
 	private void addComments(LinkedList<Comment> comments, String category, int id) {
 		comments.clear();
 		JSONArray jsonComments = null;
 		try {
 			jsonComments = mJson.getJSONArray(category).
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
 
 	/**
 	 * Creates or retrieves a use given an ID.
 	 * @param id The user's id
 	 * @param name The user's name
 	 * @return The user
 	 */
 	private User addOrGetUser(String id, String name) {
 		if (mUsers.containsKey(id)) {
 			return mUsers.get(id);
 		}
 		User tmp = new User();
 		tmp.mId = id;
 		tmp.mName = name;
 		return tmp;
 	}
 
 	/**
 	 * Adds or gets a user based on a JSONObject
 	 * @param o The JSONObject to be inspected
 	 * @return The user
 	 */
 	private User addOrGetUser(JSONObject o) {
 		try {
 			return addOrGetUser(o.getString("id"), o.getString("name"));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a message with a certain index
 	 * @param id The index
 	 * @return The message
 	 */
 	public Message getMessage(int id) {
 		return mMessages.get(id);
 	}
 
 	/**
 	 * Gets a interpretation with a certain index
 	 * @param id The index
 	 * @return The interpretation
 	 */
 	public Interpretation getInterpretation(int id) {
 		return mInterpretations.get(id);
 	}
 
 	/**
 	 * @return All the messages
 	 */
 	public LinkedList<Message> getMessages() {
 		return (LinkedList<Message>) mMessages.clone();
 	}
 
 	/**
 	 * @return All the interpretations
 	 */
 	public LinkedList<Interpretation> getInterpretations() {
 		return (LinkedList<Interpretation>) mInterpretations.clone();
 	}
 
 	/**
 	 * @return Number of unread messages
 	 */
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
 
 	/**
 	 * Gets a certain property from a certain index in a certain array
 	 * @param array 
 	 * @param id
 	 * @param property
 	 * @return
 	 */
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
 
 	private Boolean getBooleanProperty(String array, int id, String property) {
 		return (Boolean) getProperty(array, id, property);
 	}
 
 	private String getStringProperty(String array, int id, String property) {
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
 
 	/**
 	 * @return The number of messages
 	 */
 	public int getNumberOfMessages() {
 		return mMessages.size();
 	}
 
 	/**
 	 * @return The number of interpretations
 	 */
 	public int getNumberOfInterpretations() {
 		return mInterpretations.size();
 	}
 
 	/**
 	 * Nasty hack for messaging
 	 * @param url
 	 * @return
 	 */
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
 
 	/**
 	 * Retrieves the latest charts.
 	 */
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
 
 	/** Fills the given TreeMap with the names and image URLs of available charts.
 	 * @param chartURLTree The TreeMap to fill. Gets cleared before filling.
 	 */
 	public void repopulateSortedChartImageHrefTree(TreeMap<String, String> chartURLTree) {
 		chartURLTree.clear();
 		for (Chart c : mCharts) {
 			chartURLTree.put(c.mName, c.mImageHref);
 		}
 	}
 }
