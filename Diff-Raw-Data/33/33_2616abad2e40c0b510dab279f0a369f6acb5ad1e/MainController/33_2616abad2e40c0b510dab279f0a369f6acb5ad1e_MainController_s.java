 package app.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import app.dao.RegisteredLayerDao;
 import app.dao.RegisteredUserDao;
 import app.dao.RegisteredUserItemDao;
 import app.model.db.RegisteredLayer;
 import app.model.db.RegisteredUser;
 import app.model.db.RegisteredUserItem;
 import app.model.form.FormSimpleUser;
 import app.model.form.FormString;
 import app.model.local.Group;
 import app.model.local.MapItem;
 import app.model.local.Message;
 import app.model.local.Point;
 import app.model.local.User;
 import app.model.local.UserItem;
 import app.model.local.UserState;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import com.jayway.restassured.path.json.JsonPath;
 
 @Controller
 public class MainController {
 	
 	private BiMap<Long, String> sessions = HashBiMap.create();
 	private Map<String, Map<Long, MapItem>> layers = new HashMap<>();
 	private List<Message> messages = new ArrayList<>();
 	private Map<String, Group> groups = new HashMap<>();
 	
 	private Map<String, User> users = new HashMap<>();
 	private Map<String, UserItem> userItems = new HashMap<>();
 	
 	private Random random = new Random();
 	private ObjectMapper mapper = new ObjectMapper();
 	
 	@Autowired(required=true)
 	public MainController(RegisteredUserDao registeredUserDao,
 			RegisteredLayerDao registeredLayerDao, RegisteredUserItemDao registeredUserItemDao) {
 		List<RegisteredUser> rUsers = registeredUserDao.getAll();
 		for (RegisteredUser u : rUsers) {
 			users.put(u.getId(), new User(u));
 		}
 		List<RegisteredUserItem> rUserItems = registeredUserItemDao.getAll();
 		for (RegisteredUserItem i : rUserItems) {
 			userItems.put(i.getId(), new UserItem(i));
 		}
 		List<RegisteredLayer> rLayers = registeredLayerDao.getAll();
 		for (RegisteredLayer l : rLayers) {
 			layers.put(l.getId(), new HashMap<Long, MapItem>());
 		}
 	}
 
 	@RequestMapping("/index")
 	public String index(Model model) {
 		model.addAttribute("formString", new FormString());
 		model.addAttribute("formSimpleUser", new FormSimpleUser());
 		return "index";
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String login(@RequestBody String json, HttpServletResponse response) throws Exception {
 		User jsonUser = mapper.readValue(json, User.class);
 		User user = users.get(jsonUser.getId());
 		if (user == null || jsonUser.getPassword() == null ||
 				!jsonUser.getPassword().equals(user.getPassword())) {
 			return "{\"retval\": null, \"exception\": \"CouldNotLogin\"}";
 		}
 		Long sessionID = this.initializeSession(user.getId());
 		return String.format("{\"retval\": %s, \"exception\": null}", sessionID);
 	}
 	
 	@RequestMapping(value = "/logout", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String logout(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"exception\": \"InvalidSessionID\"}";
 		}
 		cleanUp(sessions.get(sessionId));
 		return "{\"exception\": null}";
 	}
 	
 	@RequestMapping(value = "/getUsers", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String getUsers(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidSessionID\"}";
 		}
 		String res = mapper.writeValueAsString(sessions.values());
 		return String.format("{\"retval\": %s, \"exception\": null}", res);
 	}
 	
 	@RequestMapping(value = "/getMapItems", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String getMapItems(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		String layer = JsonPath.with(json).getString("layer");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidSessionID\"}";
 		}
 		if (!layers.containsKey(layer)) {
 			return "{\"retval\": null, \"exception\": \"InvalidLayer\"}";
 		}
		String res = mapper.writeValueAsString(layers.get(layer));
 		return String.format("{\"retval\": %s, \"exception\": null}", res);
 	}
 	
 	@RequestMapping(value = "/getLayers", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String getLayers(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidSessionID\"}";
 		}
 		String res = mapper.writeValueAsString(layers.keySet());
 		return String.format("{\"retval\": %s, \"exception\": null}", res);
 	}
 	
 	@RequestMapping(value = "/addItemToLayer", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String addItemToLayer(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		String layer = JsonPath.with(json).getString("layer");
 		Point point = JsonPath.with(json).getObject("point", Point.class);
 		String data = JsonPath.with(json).getString("data");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidSessionID\"}";
 		}
 		if (!layers.containsKey(layer)) {
 			return "{\"retval\": null, \"exception\": \"InvalidLayer\"}";
 		}
 		MapItem item = new MapItem(point, data, layers);
 		layers.get(layer).put(item.getId(), item);
 		return String.format("{\"retval\": %s, \"exception\": null}", item.getId());
 	}
 	
 	@RequestMapping(value = "/removeMapItem", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String removeMapItem(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		Long itemId = JsonPath.with(json).getLong("item");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"exception\": \"InvalidSessionID\"}";
 		}
 		for (Map<Long, MapItem> l : layers.values()) {
 			if (l.containsKey(itemId)) {
 				return "{\"exception\": null}";
 			}
 		}
 		return "{\"exception\": \"InvalidMapItem\"}";
 	}
 	
 	@RequestMapping(value = "/updateSelfState", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String updateSelfState(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		UserState newState = JsonPath.with(json).getObject("newState", UserState.class);
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"exception\": \"InvalidSessionID\"}";
 		}
 		users.get(sessions.get(sessionId)).setState(newState);
 		return "{\"exception\": null}";
 	}
 	
 	@RequestMapping(value = "/getPossibleUserItems", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String getPossibleUserItems(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidSessionID\"}";
 		}
 		String res = mapper.writeValueAsString(userItems.values());
 		return String.format("{\"retval\": %s, \"exception\": null}", res);
 	}
 	
 	@RequestMapping(value = "/addItemToUser", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String addItemToUser(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		String userId = JsonPath.with(json).getString("user");
 		String itemId = JsonPath.with(json).getString("item");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"exception\": \"InvalidSessionID\"}";
 		}
 		if (!sessions.containsValue(userId)) {
 			return "{\"exception\": \"InvalidUser\"}";
 		}
 		if (!userItems.containsKey(itemId)) {
 			return "{\"exception\": \"InvalidUserItem\"}";
 		}
 		users.get(userId).getItems().put(itemId, userItems.get(itemId));
 		return "{\"exception\": null}";
 	}
 	
 	@RequestMapping(value = "/removeItemFromUser", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String removeItemFromUser(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		String userId = JsonPath.with(json).getString("user");
 		String itemId = JsonPath.with(json).getString("item");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"exception\": \"InvalidSessionID\"}";
 		}
 		if (!sessions.containsValue(userId)) {
 			return "{\"exception\": \"InvalidUser\"}";
 		}
 		if (!userItems.containsKey(itemId)) {
 			return "{\"exception\": \"InvalidUserItem\"}";
 		}
 		if (!users.get(userId).getItems().containsKey(itemId)) {
 			return "{\"exception\": \"CouldNotRemove\"}";
 		}
 		users.get(userId).getItems().remove(itemId);
 		return "{\"exception\": null}";
 	}
 	
 	@RequestMapping(value = "/getUserItems", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String getUserItems(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		String userId = JsonPath.with(json).getString("user");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidSessionID\"}";
 		}
 		if (!sessions.containsValue(userId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidUser\"}";
 		}
 		Set<String> items = users.get(userId).getItems().keySet();
 		String res = mapper.writeValueAsString(items);
 		return String.format("{\"retval\": %s, \"exception\": null}", res);
 	}
 	
 	@RequestMapping(value = "/sendMessage", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String sendMessage(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		String text = JsonPath.with(json).getString("message");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"exception\": \"InvalidSessionID\"}";
 		}
 		Long timestamp = System.currentTimeMillis();
 		Message m = new Message(timestamp, sessions.get(sessionId), text);
 		messages.add(m);
 		return "{\"exception\": null}";
 	}
 	
 	@RequestMapping(value = "/getMessages", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String getMessages(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidSessionID\"}";
 		}
 		User u = users.get(sessions.get(sessionId));
 		Long timestamp = u.getLastMessageCheck();
 		u.setLastMessageCheck(System.currentTimeMillis());
 		if (messages.isEmpty()) {
 			return "{\"retval\": [], \"exception\": \"null\"}";
 		}
		int index = 0;
 		for (Message m : messages) {
			if (m.getSentTime() > timestamp) {
				break;
 			}
			++index;
 		}
		List<Message> newMessages = messages.subList(index, messages.size());
 		String res = mapper.writeValueAsString(newMessages);
 		return String.format("{\"retval\": %s, \"exception\": null}", res);
 	}
 	
 	@RequestMapping(value = "/getGroups", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String getGroups(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidSessionID\"}";
 		}
 		String res = mapper.writeValueAsString(groups.values());
 		return String.format("{\"retval\": %s, \"exception\": null}", res);
 	}
 	
 	@RequestMapping(value = "/createGroup", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody
 	public synchronized String createGroup(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		Group group = JsonPath.with(json).getObject("group", Group.class);
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"exception\": \"InvalidSessionID\"}";
 		}
 		if (groups.containsKey(group.getId())) {
 			return "{\"exception\": \"CouldNotCreateGroup\"}";
 		}
 		groups.put(group.getId(), group);
 		return "{\"exception\": null}";
 	}
 	
 	@RequestMapping(value = "/addToGroup", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody
 	public synchronized String addToGroup(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		String userId = JsonPath.with(json).getString("user");
 		String groupId = JsonPath.with(json).getString("group");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"exception\": \"InvalidSessionID\"}";
 		}
 		if (!sessions.containsValue(userId)) {
 			return "{\"exception\": \"InvalidUser\"}";
 		}
 		if (!groups.containsKey(groupId)) {
 			return "{\"exception\": \"InvalidGroup\"}";
 		}
 		groups.get(groupId).getUsers().put(userId, users.get(userId));
 		return "{\"exception\": null}";
 	}
 	
 	@RequestMapping(value = "/removeFromGroup", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody
 	public synchronized String removeFromGroup(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		String userId = JsonPath.with(json).getString("user");
 		String groupId = JsonPath.with(json).getString("group");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"exception\": \"InvalidSessionID\"}";
 		}
 		if (!sessions.containsValue(userId)) {
 			return "{\"exception\": \"InvalidUser\"}";
 		}
 		if (!groups.containsKey(groupId)) {
 			return "{\"exception\": \"InvalidGroup\"}";
 		}
 		if (!groups.get(groupId).getUsers().containsKey(userId)) {
 			return "{\"exception\": \"CouldNotRemove\"}";
 		}
 		groups.get(groupId).getUsers().remove(userId);
 		return "{\"exception\": null}";
 	}
 	
 	@RequestMapping(value = "/getGroupUsers", method = RequestMethod.POST, 
 			consumes="application/json; charset=utf-8", 
 			produces="application/json; charset=utf-8")
 	@ResponseBody	
 	public synchronized String getGroupUsers(@RequestBody String json, HttpServletResponse response) throws Exception {
 		Long sessionId = JsonPath.with(json).getLong("sessionID");
 		String groupId = JsonPath.with(json).getString("group");
 		if (!sessions.containsKey(sessionId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidSessionID\"}";
 		}
 		if(!groups.containsKey(groupId)) {
 			return "{\"retval\": null, \"exception\": \"InvalidGroup\"}";
 		}
 		String res = mapper.writeValueAsString(groups.get(groupId).getUsers().keySet());
 		return String.format("{\"retval\": %s, \"exception\": null}", res);
 	}
 	
 	public BiMap<Long, String> getSessions() {
 		return sessions;
 	}
 	
 	public Map<String, Map<Long, MapItem>> getLayers() {
 		return layers;
 	}
 	
 	public Map<String, User> getUsers() {
 		return users;
 	}
 	
 	public Map<String, UserItem> getUserItems() {
 		return userItems;
 	}
 	
 	private Long initializeSession(String userId) {
 		cleanUp(userId);
 		Long sessionId = generateUniqueSessionId();
 		sessions.put(sessionId, userId);
 		users.get(userId).setLastMessageCheck(0L);
 		return sessionId;
 	}
 	
 	private Long generateUniqueSessionId() {
 		Long id = (long) random.nextInt(Integer.MAX_VALUE);
 		while (sessions.containsKey(id)) {
 			id = (long) random.nextInt(Integer.MAX_VALUE);
 		}
 		return id;
 	}
 	
 	private void cleanUp(String userId) {
 		sessions.inverse().remove(userId);
 		Iterator<Message> iter = messages.iterator();
 		while(iter.hasNext()) {
 			Message m = iter.next();
 			if (m.getId().equals(userId)) {
 				iter.remove();
 			}
 		}
 		for (Group g : groups.values()) {
 			g.getUsers().remove(userId);
 		}
 	}
 
 }
