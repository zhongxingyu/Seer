 package org.antstudio.rbac.action;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.antstudio.log.domain.Log;
 import org.antstudio.log.service.LogService;
 import org.antstudio.rbac.domain.Role;
 import org.antstudio.rbac.domain.User;
 import org.antstudio.rbac.domain.annotation.LoginRequired;
 import org.antstudio.rbac.domain.annotation.MenuMapping;
 import org.antstudio.rbac.domain.annotation.PermissionMapping;
 import org.antstudio.rbac.service.UserService;
 import org.antstudio.support.spring.annotation.FormParam;
 import org.antstudio.utils.MessageUtils;
 import org.antstudio.utils.ParamUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 
 /**
  * 
  * @author Gavin
  * @version 1.0
  * @date 2012-12-2
  */
 @Controller
 @RequestMapping("/user")
 public class UserAction {
 
 	@Resource 
 	private UserService userService;
 	
 	@Resource
 	private LogService logService;
 	/**
 	 * show the login page
 	 * @return
 	 */
 	@RequestMapping(value = "/login")
 	public ModelAndView login(){
 		 return new ModelAndView("pages/login");
 	}
 	 
 	/**
 	 * to do validate the user by password and userName
 	 * @param user
 	 * @param role
 	 * @return
 	 */
 	 @RequestMapping(value="/login/validate")
 	 @ResponseBody
 	 //@LogRecord(action="${method_return.result}登录系统")
 	 public Map<String,Object> userValidate(@FormParam(value="user")User user,HttpServletRequest request){
 		 if(user==null)
 			 return MessageUtils.getMapMessage(false);
		 String loginName = user.getUserName();
		 String password = user.getPassword();
 		 user.setId(null);//设置id为null，此后用id是否为空判断是否成功登录
 		 user = userService.login(user);
		 if(user==null||user.getId()==null){
			 logService.log(new Log(loginName,-1L,"登录失败","{userName:"+loginName+",password:"+password+"}"));
 			return MessageUtils.getMapMessage(false);
 		 }
 		 else{
 			 request.getSession().setAttribute(User.CURRENT_USER_ID, user.getId());
 			 logService.log(new Log(user.getUserName(),user.getId(),"成功登录系统"));
 			return MessageUtils.getMapMessage(true);
 		 }
 	 }
 	 
 	 @MenuMapping(url="/user",name="用户列表",code="100002",parentCode="100000")
 	 @RequestMapping("")
 	 @PermissionMapping(code="000008",name="用户列表")
 	 public ModelAndView userList(HttpServletRequest request){
 		 return new ModelAndView("pages/rbac/userList");
 	 }
 	 
 	 
 	 @PermissionMapping(code="00000d",name="用户列表")
 	 @RequestMapping("/getUsersData")
 	 @ResponseBody
 	 public Map<String,Object> getUsersData(HttpServletRequest request){
 		 Map<String,Object> paramsMap = ParamUtils.getParamsMap(request);
 		 paramsMap.put("uid", userService.getCurrentUserId(request));
 		 paramsMap.put("deleteFlag", false);
 		 return  userService.getUsersByCreatorForPager(paramsMap).toMap();
 	 }
 	 
 	 @RequestMapping("/addUser")
 	 @ResponseBody
 	 public Map<String,Object> addUser(@FormParam(value="user")User user,HttpServletRequest request){
 		 user.setCreateBy(userService.getCurrentUserId(request));
 		 userService.addUser(user);
 		 return MessageUtils.getMapMessage(true);
 	 }
 	 
 	 @RequestMapping("/getUser")
 	 @ResponseBody
 	 public Map<String,Object> getUser(@RequestParam("id")Long id){
 		 Map<String,Object> m = new HashMap<String,Object>();
 		 m.put("user", userService.getModel(id).toAllMap());
 		 return m;
 	 }
 	 
 	 @RequestMapping("/updateUser")
 	 @ResponseBody
 	 public Map<String,Object> updateUser(@FormParam("user") User user){
 		 userService.update(user);
 		 return MessageUtils.getMapMessage(true);
 		 
 	 }
 	 
 	 @RequestMapping("/logicDeleteUser")
 	 @ResponseBody
 	 public Map<String,Object> logicDeleteUser(@RequestParam("ids")Long[] ids){
 		 userService.delete(ids, true);
 		 return MessageUtils.getMapMessage(true);
 		 
 	 }
 	 
 	 /**
 	  * 获取用户所在的角色路径
 	  * @return
 	  */
 	 @RequestMapping("/getRolePath")
 	 @ResponseBody
 	 public Map<String,Object> getRolePath(@RequestParam("uid")Long uid){
 		Map<String,Object> path = MessageUtils.getMapMessage(true);
 		String rolePath = "";
 		Role role = userService.getModel(uid).getRole();
 		if(role!=null)
 			rolePath = role.getRolePath();
 		path.put("path", rolePath);
 		 return path;
 	 }
 	 
 	 /**
 	  * 登出操作
 	  * @return
 	  */
 	 @RequestMapping("/loginOut")
 	 public ModelAndView loginOut(HttpServletRequest request){
 		 request.getSession().setAttribute(User.CURRENT_USER_ID, null);
 		 return new ModelAndView("pages/login");
 	 }
 	 
 	 @LoginRequired
 	 @RequestMapping("/changePassword")
 	 @MenuMapping(code="100006",name="修改密码",url="/user/changePassword",parentCode="100000")
 	 public ModelAndView showChangePasswordPage(HttpServletRequest request){
 		 String info = null ;
 		 if(userService.isSysUser(userService.getCurrentUser(request)))
 			 info = "对不起，系统管理员不能修改密码.";
 		 return new ModelAndView("pages/rbac/changePassword","info",info);
 	 }
 	 
 	 /**
 	  * 密码匹配,用于密码修改
 	  * @param newPassword
 	  * @return
 	  */
 	 @LoginRequired
 	 @RequestMapping("/matchOldPassword")
 	 @ResponseBody
 	 public Map<String,Object> matchOldPassword(@RequestParam("password")String newPassword,HttpServletRequest request){
 		 if(userService.getCurrentUser(request).getPassword().equals(newPassword))
 		 return MessageUtils.getMapMessage(true);
 		 else
 			 return MessageUtils.getMapMessage(false);
 	 }
 	 /**
 	  * 修改密码
 	  * @param newPassword
 	  * @param request
 	  * @return
 	  */
 	 @LoginRequired
 	 @RequestMapping("/doChangePassword")
 	 @ResponseBody
 	 public Map<String,Object> changePassword(@RequestParam("password")String newPassword,HttpServletRequest request){
 		 User user = userService.getCurrentUser(request);
 		 if(user.isSystemUser())
 			 return MessageUtils.getMapMessage(false);
 		 user.setPassword(newPassword);
 		 user.updateUser();
 		 return MessageUtils.getMapMessage(true);
 	 }
 }
