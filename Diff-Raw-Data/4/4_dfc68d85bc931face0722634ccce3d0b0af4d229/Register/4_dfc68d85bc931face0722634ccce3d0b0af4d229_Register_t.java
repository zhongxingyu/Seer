 package example.controller;
 
 import example.auth.Roles;
 import example.model.User;
 import example.model.UserProxy;
 import juva.Utils;
 import juva.rbac.PermissionTable.METHODS;
 
 
 public class Register extends Controller{
 	
 	public static String[] URL_PATTERN = {"/reg"};
 	
 	protected void initPermission() throws Throwable{
 		this.permissionTable.allow(Roles.Everyone, METHODS.POST);
 	}
 	
 	public Register() throws Throwable{
 		super(URL_PATTERN);
 	}
 	
 	public void post() throws Throwable{
 		String captcha = request.getParameter("captcha").toLowerCase();
 		String trueCaptcha = (String) session.getAttribute("randomString");
 		trueCaptcha = trueCaptcha.toLowerCase();
 		if (!captcha.equals(trueCaptcha)){
 			Utils.Json.json("false", "验证码错误！");
 			return ;
 		}
 		
 		String email = request.getParameter("email");
 		String passwd = request.getParameter("passwd");
 		String screen = request.getParameter("screen");
 		
 		if (email == null || passwd == null){
 			Utils.Json.json("false", "请输入帐号与密码");
 			return ;
 		}
 		
 		if (screen == null){
 			Utils.Json.json("false", "请输入您的昵称");
 			return ;
 		}
 
 		boolean isEmailExist = userProxy.isEmailExist(email);
 		if (isEmailExist){
 			Utils.Json.json("false", "该邮箱存在，请换另一个。");
 			return ;
 		}
 		boolean isScreenExist = userProxy.isScreenExist(screen);
 		if (isScreenExist){
 			Utils.Json.json("false", "该昵称已存在，请换另一个。");
 			return ;
 		}
 		String remoteIp = request.getRemoteAddr();
 		String currentTime = Utils.getCurrentTime();
 		String hashPasswd = Utils.MD5(passwd);
 		
 		
 		User user = new User();
 		user.setValue("email", email);
 		user.setValue("passwd", hashPasswd);
 		user.setValue("screen", screen);
 		user.setValue("reg_ip", remoteIp);
 		user.setValue("last_log", currentTime);
 		user.setValue("last_ip", remoteIp);
 		user.setValue("identity", "0");
 		user.setValue("is_trash", "0");
 		UserProxy newUser = new UserProxy(user);
		newUser.setDatabase(database);
 		newUser.insert();
 		
 		Utils.Json.json("true", "注册成功！");
 		
 	}
 }
