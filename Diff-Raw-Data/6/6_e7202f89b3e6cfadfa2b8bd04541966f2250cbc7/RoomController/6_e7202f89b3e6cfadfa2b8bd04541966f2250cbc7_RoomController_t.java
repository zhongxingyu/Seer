 package com.gemantic.killer.controller;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.gemantic.common.exception.ServiceException;
 import com.gemantic.common.util.MyListUtil;
 import com.gemantic.common.util.http.cookie.CookieUtil;
 import com.gemantic.commons.push.client.PushClient;
 import com.gemantic.kill.thread.RoomThread;
 import com.gemantic.killer.common.model.Message;
 import com.gemantic.killer.exception.ServiceErrorCode;
 import com.gemantic.killer.model.Room;
 import com.gemantic.killer.model.User;
 import com.gemantic.killer.service.MemberService;
 import com.gemantic.killer.service.MessageService;
 import com.gemantic.killer.service.RoomService;
 import com.gemantic.killer.util.MessageUtil;
 import com.gemantic.killer.util.PunchUtil;
 import com.gemantic.killer.util.UserUtil;
 import com.gemantic.labs.killer.service.UsersService;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 /**
  * 提供游戏房间的创建,删除,玩家列表等功能
  * 
  * @author xdyl
  * 
  */
 @Controller
 public class RoomController {
 	private static final Log log = LogFactory.getLog(RoomController.class);
 	@Autowired
 	private RoomService roomService;
 	@Autowired
 	private MemberService memberService;
 	@Autowired
 	private UsersService userService;
 
 	@Autowired
 	private PushClient pushClient;
 
 	@Autowired
 	private MessageService droolsGameMessageService;
 
 	@Autowired
 	private CookieUtil cookieUtil;
 
 	/**
 	 * 房间列表
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/m/list")
 	public String listRoom(HttpServletRequest request, HttpServletResponse response, ModelMap model,String oldRoom) throws Exception {
 		log.debug("start get room list ");
 
 		Long uid = cookieUtil.getID(request, response);
 		if (uid == null) {
 			// 登录不成功,重新登录
 			return "redirect:/";
 		}
 		User user = this.userService.getObjectById(uid);
 		if(user==null){
 			return "redirect:/";
 		}
 		boolean isPunch = PunchUtil.isPunched(user);
 		if(isPunch){
 			int punchCount = PunchUtil.getLatestContinueDay(PunchUtil.Punch_Time_Start, Integer.MAX_VALUE, PunchUtil.Punch_Time_Start, user.getPunch());			
 			model.addAttribute("punchCount", punchCount);
 		}else{
 		
 		}
 
 		String uname = user.getName();
 
 		/*
 		 * List<Long>
 		 * worldIDS=this.worldService.getWorldList(WorldService.Status_Running);
 		 * List<World> worlds=this.worldService.getWorlds(worldIDS);
 		 */
 
 		Map<Long, Integer> room_count = new HashMap();
 		List<Room> rooms = roomService.getList();
 		log.info("get rooms " + rooms.toString());
 		List<Long> userIDS = new ArrayList();
 		for (Room r : rooms) {
 			userIDS.add(r.getCreaterID());
 		
 			List<Long> counts = this.memberService.getMembers(r.getId());
 			room_count.put(r.getId(), counts.size());
 
 		}
 		List<User> users = this.userService.getObjectsByIds(userIDS);
 		Map id_user = MyListUtil.convert2Map(User.class.getDeclaredField("id"), users);
 		
 		
 		Room room=this.memberService.getRoomOfUser(uid);
 		if(room!=null){
 			model.addAttribute("my_room", room);
 		}
 
 		Integer count = userService.getTotalCount();
 		model.addAttribute("count", count);
 		model.addAttribute("rooms", rooms);
 		model.addAttribute("uid", uid);
 		model.addAttribute("uname", uname);
 		model.addAttribute("users", id_user);
 		model.addAttribute("room_count", room_count);
 		model.addAttribute("user", user);
 		model.addAttribute("type", "game");
 		model.addAttribute("oldRoom", oldRoom);
 		return "/room/list/all";
 	}
 
 	/**
 	 * 进入房间
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/m/play/enter")
 	public String enterRoom(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long rid) throws Exception {
 
 		Long uid = cookieUtil.getID(request, response);
 		if (uid == null) {
 			// 登录不成功,重新登录
 			return "redirect:/";
 		}
 
 		log.debug("id is " + rid + " uid " + uid);
 
 		boolean first=true;
 		List<Long> userIDS = this.memberService.getMembers(rid);
 		if(userIDS.contains(uid)){
 			first=false;
 			
 		}
 		// 进入房间,判断房间这些都是相同的
 
 		Room room = this.roomService.getRoom(rid);
 		// 告诉MembService,有新用户加入了房间
 		
 		Room oldRoom = this.memberService.getRoomOfUser(uid);
 		if (oldRoom != null&&oldRoom.getId().longValue()!=rid.longValue()) {
 			model.addAttribute("oldRoom", oldRoom.getName());
 			return "redirect:/m/list.do";
 		}else{
 			this.memberService.newUserEnterRoom(rid, uid);
 		}
 
 		// 从MemberService中获取成员列表
 	
 		List<User> users = this.userService.getObjectsByIds(userIDS);
 
 		User u=this.userService.getObjectById(uid);
 		String version = room.getVersion();
 		log.info("version is " + version);
 		String stageShow=UserUtil.getRandomStageShow(UserUtil.StageShow_Login,u);
 		log.info(u+"get stage show is "+stageShow);
 		Message loginMessage = new Message(uid.toString(), "login", "-500", "#0000FF", "78", rid.toString(), stageShow, version);
 		List<Message> messages = this.droolsGameMessageService.generate(loginMessage, room);
 		MessageUtil.sendMessage(version, messages, this.pushClient);
 
 		// 更新用户的当前房间信息,MemberService的功能是否需要?
 
 		User creater=this.userService.getObjectById(room.getCreaterID());
 		model.addAttribute("music", creater.getMusic());
 		model.addAttribute("room", room);
 		model.addAttribute("users", users);
 		model.addAttribute("type", "game");
 		model.addAttribute("uid", uid);
		model.addAttribute("stageShow", stageShow);	
 		if(first){
			model.addAttribute("first", "first");	
		}else{
			model.addAttribute("first", "notFirst");	
 		}
 	
 		return "/room/play/" + version;
 
 	}
 
 	/**
 	 * 获取房间里玩家的信息.
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/m/player/list")
 	public String getRoomPlayList(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long rid) throws Exception {
 		log.debug("id is " + rid);
 
 		Room room = this.roomService.getRoom(rid);
 		// 从MemberService中获取成员列表
 		List<Long> userIDS = this.memberService.getMembers(rid);
 		log.info(rid + " get users " + userIDS.size());
 		Set<Long> readyUserIDS = this.memberService.getMembersStatus(rid);
 		log.info(rid + " get ready users " + readyUserIDS.size());
 		// 从UserService中获取用户信息
 		List<User> users = this.userService.getObjectsByIds(userIDS);
 		// TODO performance
 
 		Map<Long, String> status = new HashMap();
 		for (Long uid : userIDS) {
 			String st = "0";
 			if (readyUserIDS.contains(uid)) {
 				log.info(rid + " : " + uid + " is ready ");
 				st = "1";
 			} else {
 				log.info(rid + " : " + uid + " is not ready ");
 			}
 			status.put(uid, st);
 		}
 
 		model.addAttribute("room", room);
 		model.addAttribute("users", users);
 		return "/room/player/list";
 
 	}
 
 	/**
 	 * g玩家准备就绪.
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/m/player/ready")
 	public String setPlayerReady(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long rid, @RequestParam Long uid)
 			throws Exception {
 		log.debug(uid + " want get ready of " + rid);
 
 		int code = 0;
 		String message = "success";
 		Room r = this.roomService.getRoom(rid);
 		if (Room.status_start == (r.getStatus())) {
 			code = ServiceErrorCode.Room_ALREADY_START;
 			model.addAttribute("code", code);
 			return "/room/play/ready";
 		}
 
 		try {
 
 			User user = userService.getObjectById(uid);
 			memberService.ready(rid, uid);
 		} catch (ServiceException e) {
 
 			code = e.getErrorCode();
 
 		}
 		model.addAttribute("code", code);
 
 		return "/room/play/ready";
 
 	}
 
 	/**
 	 * 游戏开始
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/m/start")
 	public String startRoom(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long rid, @RequestParam Long uid)
 			throws Exception {
 		log.debug(uid + " want get start of " + rid);
 
 		int code = 0;
 
 		Room r = this.roomService.getRoom(rid);
 		if (Room.status_start == (r.getStatus())) {
 			code = ServiceErrorCode.Room_ALREADY_START;
 			model.addAttribute("code", code);
 			return "/room/play/ready";
 		}
 
 		try {
 
 			this.roomService.start(rid, uid);
 		} catch (ServiceException e) {
 
 			code = e.getErrorCode();
 
 		}
 		model.addAttribute("code", code);
 
 		return "/room/play/ready";
 	}
 
 	/**
 	 * 游戏开始
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/m/create")
 	public String createRoom(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long createID, @RequestParam String name,
 			@RequestParam String version) throws Exception {
 		Long uid = cookieUtil.getID(request, response);
 		if (uid == null) {
 			// 登录不成功,重新登录
 			return "redirect:/";
 		}
 		log.debug(createID + " want create  " + name + " type " + version);
 
 		// 告诉MembService,有新用户加入了房间
 		User user = this.userService.getObjectById(uid);
 		
 		
 		
 
 		int code = 0;
 		Room room = new Room(name, createID, version);
 		// TODO 我判断不出来用Int还是用String好
 
 		Long rid = this.roomService.createRoom(room);
 		
 
 		RoomThread r = new RoomThread(rid, roomService, droolsGameMessageService, pushClient);
 		r.run();
 		
 
 		model.addAttribute("code", code);
 		model.addAttribute("room", room);
 		model.addAttribute("type", "game");
 		return "/room/form/info";
 	}
 
 	/**
 	 * 游戏准备
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/room/detail")
 	public String roomDetail(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long rid, @RequestParam Long uid)
 			throws Exception {
 
 		Room room = this.roomService.getRoom(rid);
 		Map<Long, Map<String, Set<String>>> user_info = new HashMap();
 		String version = room.getVersion();
 		Message queryMessage = new Message(uid.toString(), "query", "-500", "#0000FF", "78", rid.toString(), "我查询", version);
 		// List<Message>
 		// messages=this.droolsGameMessageService.generate(queryMessage);
 
 		String snapshots = this.droolsGameMessageService.getSnapshots(queryMessage, room);
 		log.info("get snapshot is "+snapshots);
 
 		// no world start
 		// game is not start,return room info
 
 		/*
 		 * for(Message m:messages){ Long id=new Long(m.getSubject());
 		 * Map<String,Set<String>> info=user_info.get(id); if(info==null){
 		 * info=new HashMap(); } String predict=m.getPredict(); String
 		 * content=m.getContent(); Set<String> contents=info.get(predict);
 		 * if(contents==null){ contents=new HashSet(); } contents.add(content);
 		 * info.put(predict, contents); user_info.put(id, info);
 		 * 
 		 * }
 		 */
 
 		// log.info(user_info);
 		/*
 		 * List<User> users=this.userService.getUsers(new
 		 * ArrayList(user_info.keySet())); Map<Long,User>
 		 * id_users=MyListUtil.convert2Map(User.class.getDeclaredField("id"),
 		 * users);
 		 */
 
 		model.addAttribute("room", room);
 		model.addAttribute("snapshots", snapshots);
 
 		/*
 		 * model.addAttribute("user_info", user_info);
 		 * model.addAttribute("room", room); model.addAttribute("users",
 		 * id_users);
 		 */
 		return "/room/detail/show";
 	}
 
 	/**
 	 * 进入房间
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 
 	@RequestMapping(value = "/m/expression/update", method = RequestMethod.POST)
 	public String updateExpression(HttpServletRequest request, HttpServletResponse response, ModelMap model, String[] express, @RequestParam Long rid)
 			throws Exception {
 
 		Long uid = cookieUtil.getID(request, response);
 		if (uid == null) {
 			model.addAttribute("code", "-1");
 			return "/common/success";
 		}
 		Room r = this.roomService.getRoom(rid);
 		if (r == null) {
 			model.addAttribute("code", "-1");
 			return "/common/success";
 		}
 		if (r.getCreaterID().longValue() != uid.longValue()) {
 			model.addAttribute("code", "-1");
 			return "/common/success";
 		}
 
 		if (express == null) {
 			express = new String[] {};
 		}
 		List<String> ls = Arrays.asList(express);
 		log.info("id is " + rid + " uid " + uid + " want update express " + ls);
 
 		String version = r.getVersion();
 		log.info("version is " + version);
 		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
 		String json = gson.toJson(ls);
 		Message m = new Message(uid.toString(), "expression", json, "#0000FF", "", rid.toString(), "", version);
 
 		r.getMessages().offer(m);
 		r.setExpressions(ls);
 		this.roomService.updateRoom(r);
 		User u = this.userService.getObjectById(uid);
 		u.setExpression(ls);
 		this.userService.update(u);
 
 		model.addAttribute("code", "0");
 		return "/common/success";
 
 	}
 
 	/**
 	 * 进入房间
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 
 	@RequestMapping(value = "/m/expression/show", method = RequestMethod.GET)
 	public String getExpression(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long rid) throws Exception {
 
 		Long uid = cookieUtil.getID(request, response);
 		if (uid == null) {
 			model.addAttribute("code", "-1");
 			return "/common/success";
 		}
 		Room r = this.roomService.getRoom(rid);
 		if (r == null) {
 			model.addAttribute("code", "-1");
 			return "/common/success";
 		}
 		List<String> ls = new ArrayList();
 		if (r.getCreaterID().longValue() == uid) {
 			// 从用户中取
 			if (r.getExpressions().size() == 0) {
 				User u = this.userService.getObjectById(uid);
 				r.setExpressions(u.getExpression());
 				this.roomService.updateRoom(r);
 			}
 
 			log.info(uid + " is admin so get express from use " +r.getExpressions());
 		} else {
 
 			log.info(uid + " is not admin so get express from room " + ls);
 		}
 		ls = r.getExpressions();
 
 		model.addAttribute("code", "0");
 		model.addAttribute("express", ls);
 		return "/room/express/show";
 
 	}
 
 }
