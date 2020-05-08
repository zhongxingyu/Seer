 package pl.edu.agh.io.coordinator.utils.net;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import pl.edu.agh.io.coordinator.resources.Group;
 import pl.edu.agh.io.coordinator.resources.Layer;
 import pl.edu.agh.io.coordinator.resources.MapItem;
 import pl.edu.agh.io.coordinator.resources.Message;
 import pl.edu.agh.io.coordinator.resources.Point;
 import pl.edu.agh.io.coordinator.resources.User;
 import pl.edu.agh.io.coordinator.resources.UserItem;
 import pl.edu.agh.io.coordinator.resources.UserState;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.CouldNotCreateGroupException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.CouldNotLogInException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.CouldNotRemoveException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidGroupException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidLayerException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidMapItemException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidSessionIDException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidUserException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidUserItemException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.NetworkException;
 
 public class JSonProxy implements IJSonProxy {
 	
 	private static JSonProxy INSTANCE;
 	private static long SESSION_ID = -1;	
 	private static String SERVER_NAME = "http://io.wojtasskorcz.eu.cloudbees.net"; // without ending slash
 
 	private JSonProxy() {
 	}
 
 	public static synchronized JSonProxy getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new JSonProxy();
 		}
 		return INSTANCE;
 	}
 
 	private JSONObject createSessionOnlyParams() {
 		Map<String, Long> paramsInString = new HashMap<String, Long>();
 		paramsInString.put("sessionID", SESSION_ID);
 		JSONObject params = new JSONObject(paramsInString);
 		return params;
 	}
 	
 	private String getJSonString(String methodName, JSONObject params) throws NetworkException {
 		StringBuilder builder = new StringBuilder();
 		HttpClient client = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(SERVER_NAME + "/" + methodName);
 		try {
 			StringEntity stringEntity = new StringEntity(params.toString(), HTTP.UTF_8);
 			stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
 			httpPost.setEntity(stringEntity);
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 		}
 		try {
 			HttpResponse response = client.execute(httpPost);
 			StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			if (statusCode == 200) {
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 			} else {
 				throw new NetworkException();
 			}
 		} catch (Exception e) {
 			throw new NetworkException();
 		}
 		return builder.toString();
 	}
 
 	@Override
 	public synchronized void login(String userName, String password) throws CouldNotLogInException, NetworkException {
 		Map<String, String> paramsInString = new HashMap<String, String>();
 		paramsInString.put("id", userName);
 		paramsInString.put("password", password);
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("login", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("null")) {
 				SESSION_ID = jsonObject.getLong("retval");
 			} else {
 				SESSION_ID = -1;
 				throw new CouldNotLogInException();
 			}
 		} catch (JSONException e) {
 			throw new CouldNotLogInException();
 		}
 	}
 
 	@Override
 	public synchronized void logout() throws InvalidSessionIDException, NetworkException {
 		JSONObject params = createSessionOnlyParams();
 		try {
 			String jsonString = getJSonString("logout", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			SESSION_ID = -1;
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Set<MapItem> getMapItems(Layer layer) throws InvalidSessionIDException, InvalidLayerException, NetworkException {
 		Set<MapItem> toReturn = new HashSet<MapItem>();
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("layer", layer.getName());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("getMapItems", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("InvalidLayer")) {
 				throw new InvalidLayerException();
 			} else {
 				JSONArray array = jsonObject.getJSONArray("retval");
 				int limit = array.length();
 				for (int i = 0; i < limit; ++i) {
 					JSONObject jsonMapItem = array.getJSONObject(i);
 					MapItem mapItem = new MapItem(jsonMapItem);
 					toReturn.add(mapItem);
 				}
 			}			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return toReturn;
 	}
 
 	@Override
 	public Set<Layer> getLayers() throws InvalidSessionIDException, NetworkException {
 		Set<Layer> toReturn = new HashSet<Layer>();
 		JSONObject params = createSessionOnlyParams();
 		try {
 			String jsonString = getJSonString("getLayers", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else {
 				JSONArray array = jsonObject.getJSONArray("retval");
 				int limit = array.length();
 				for (int i = 0; i < limit; ++i) {
 					String layer = array.getString(i);
 					toReturn.add(new Layer(layer));
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return toReturn;
 	}
 
 	@Override
 	public MapItem addItemToLayer(Layer layer, Point point, String data) throws InvalidSessionIDException, InvalidLayerException,
 			NetworkException {
 		MapItem toReturn = null;
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("layer", layer.getName());
 		paramsInString.put("point", point.toJsonObject());
 		paramsInString.put("data", data);
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("addItemToLayer", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("InvalidLayer")) {
 				throw new InvalidLayerException();
 			} else {
 				Long retval = jsonObject.getLong("retval");
 				toReturn = new MapItem(retval, point, data);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return toReturn;
 	}
 
 	@Override
 	public void removeMapItem(MapItem item) throws InvalidSessionIDException, InvalidMapItemException, NetworkException {
 		Map<String, Long> paramsInString = new HashMap<String, Long>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("item", item.getId());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("removeMapItem", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("InvalidMapItem")) {
 				throw new InvalidMapItemException();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void updateSelfState(UserState newState) throws InvalidSessionIDException, NetworkException {
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("newState", newState.toJsonObject());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("updateSelfState", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Set<UserItem> getPossibleUserItems() throws InvalidSessionIDException, NetworkException {
 		Set<UserItem> toReturn = new HashSet<UserItem>();
 		JSONObject params = createSessionOnlyParams();
 		try {
 			String jsonString = getJSonString("getPossibleUserItems", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else {
 				JSONArray array = jsonObject.getJSONArray("retval");
 				int limit = array.length();
 				for (int i = 0; i < limit; ++i) {
 					JSONObject jsonUserItem = array.getJSONObject(i);
 					UserItem userItem = new UserItem(jsonUserItem);
 					toReturn.add(userItem);
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return toReturn;
 	}
 
 	@Override
 	public void addItemToUser(User user, UserItem item) throws InvalidSessionIDException, InvalidUserException,
 			InvalidUserItemException, NetworkException {
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("user", user.getId());
 		paramsInString.put("item", item.getId());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("addItemToUser", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("InvalidUser")) {
 				throw new InvalidUserException();
 			} else if (exception.equals("InvalidUserItem")) {
 				throw new InvalidUserItemException();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void removeItemFromUser(User user, UserItem item) throws InvalidSessionIDException, InvalidUserException,
 			InvalidUserItemException, CouldNotRemoveException, NetworkException {
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("user", user.getId());
 		paramsInString.put("item", item.getId());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("removeItemFromUser", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("InvalidUser")) {
 				throw new InvalidUserException();
 			} else if (exception.equals("InvalidUserItem")) {
 				throw new InvalidUserItemException();
 			} else if (exception.equals("CouldNotRemove")) {
 				throw new CouldNotRemoveException();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}	
 	}
 
 	@Override
 	public Set<User> getUsers() throws InvalidSessionIDException, NetworkException {
 		Set<User> ret = new HashSet<User>();
 		JSONObject params = createSessionOnlyParams();
 		try {
 			String jsonString = getJSonString("getUsers", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID"))
 				throw new InvalidSessionIDException();
 			else {
 				JSONArray retArray = jsonObject.getJSONArray("retval");
 				int max = retArray.length();
 				for (int i = 0; i < max; i++) {
 					JSONObject jsonUser = retArray.getJSONObject(i);
 					User user = new User(jsonUser);
 					ret.add(user);
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	@Override
 	public Set<String> getUserItems(User user) throws InvalidSessionIDException, InvalidUserException, NetworkException {
 		Set<String> toReturn = new HashSet<String>();
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("user", user.getId());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("getUserItems", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("InvalidUser")) {
 				throw new InvalidUserException();
 			} else {
 				JSONArray array = jsonObject.getJSONArray("retval");
 				int limit = array.length();
 				for (int i = 0; i < limit; ++i) {
 					String userItem = array.getString(i);
 					toReturn.add(userItem);
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return toReturn;
 	}
 
 	@Override
 	public Set<Group> getGroups() throws InvalidSessionIDException, NetworkException {
 		Set<Group> toReturn = new HashSet<Group>();
 		JSONObject params = createSessionOnlyParams();
 		try {
 			String jsonString = getJSonString("getGroups", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else {
 				JSONArray array = jsonObject.getJSONArray("retval");
 				int limit = array.length();
 				for (int i = 0; i < limit; ++i) {
 					JSONObject jsonGroup = array.getJSONObject(i);
 					Group group = new Group(jsonGroup);
 					toReturn.add(group);
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return toReturn;
 	}
 
 	@Override
 	public void createGroup(Group group) throws InvalidSessionIDException, CouldNotCreateGroupException, NetworkException {
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("group", group.toJsonObject());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("createGroup", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("CouldNotCreateGroup")) {
 				throw new CouldNotCreateGroupException();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void addToGroup(User user, Group group) throws InvalidSessionIDException, InvalidUserException, InvalidGroupException,
 			NetworkException {
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("user", user.getId());
 		paramsInString.put("group", group.getId());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("addToGroup", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("InvalidUser")) {
 				throw new InvalidUserException();
 			} else if (exception.equals("InvalidGroup")) {
 				throw new InvalidGroupException();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void removeFromGroup(User user, Group group) throws InvalidSessionIDException, InvalidUserException,
 			InvalidGroupException, CouldNotRemoveException, NetworkException {
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("user", user.getId());
 		paramsInString.put("group", group.getId());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("removeFromGroup", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("InvalidUser")) {
 				throw new InvalidUserException();
 			} else if (exception.equals("InvalidGroup")) {
 				throw new InvalidGroupException();
 			} else if (exception.equals("CouldNotRemove")) {
 				throw new CouldNotRemoveException();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Set<String> getGroupUsers(Group group) throws InvalidSessionIDException, InvalidGroupException, NetworkException {
 		Set<String> toReturn = new HashSet<String>();
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("group", group.getId());
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("getGroupUsers", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else if (exception.equals("InvalidGroup")) {
 				throw new InvalidGroupException();
 			} else {
 				JSONArray array = jsonObject.getJSONArray("retval");
 				int limit = array.length();
 				for (int i = 0; i < limit; ++i) {
 					String user = array.getString(i);
 					toReturn.add(user);
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
		return null;
 	}
 
 	@Override
 	public void sendMessage(String message) throws InvalidSessionIDException, NetworkException {
 		Map<String, Object> paramsInString = new HashMap<String, Object>();
 		paramsInString.put("sessionID", SESSION_ID);
 		paramsInString.put("message", message);
 		JSONObject params = new JSONObject(paramsInString);
 		try {
 			String jsonString = getJSonString("sendMessage", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Set<Message> getMessages() throws InvalidSessionIDException, NetworkException {
 		Set<Message> toReturn = new HashSet<Message>();
 		JSONObject params = createSessionOnlyParams();
 		try {
 			String jsonString = getJSonString("getMessages", params);
 			JSONObject jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("InvalidSessionID")) {
 				throw new InvalidSessionIDException();
 			} else {
 				JSONArray array = jsonObject.getJSONArray("retval");
 				int limit = array.length();
 				for (int i = 0; i < limit; ++i) {
 					JSONObject jsonMessage = array.getJSONObject(i);
 					Message message = new Message(jsonMessage);
 					toReturn.add(message);
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return toReturn;
 	}
 	
 }
