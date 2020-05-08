 
 package actions;
 
 import tables.Userinfo;
 import dao.UserinfoDao;
 
 public class User extends BaseAction {
     
     public static class LoginParam {
         String username;
         String password;
         String spamcode;
     }
     public String login() {
         LoginParam param = (LoginParam) getParam(LoginParam.class);
         if(session("spamcode") == null || !param.spamcode.equals(session("spamcode")) ) {
             return jsonResult("spamcode");
         }
         
         if(param.username == null || param.username.equals("")){
         	return jsonResult("username");
         }
         
         if(param.password == null || param.password.equals("")){
         	return jsonResult("password");
         }
         
         UserinfoDao ud = new UserinfoDao();
         Userinfo ui = ud.findUserinfoByname(param.username);
         if(ui == null){
         	ui = ud.findUserinfoByMail(param.username);
         }
         if(ui == null){
         	return jsonResult("username");
         }
         
         if(ui.getUpassword().equals(MD5Util.MD5(param.password))){
         	session("myUserinfo",ui);
             return jsonResult("ok");
         }
         
         return jsonResult("password");
     }
     
     
     public static class RegisterParam {
         String username;
         String password[];
         String spamcode;
         String email;
     }
     
     public String register(){
     	RegisterParam param = (RegisterParam) getParam(RegisterParam.class);
         if(session("spamcode") == null || !param.spamcode.equals(session("spamcode")) ) {
             return jsonResult("spamcode");
         }
         
         if(param.username == null || param.username.equals("")){
         	return jsonResult("username");
         }
         
         if(param.password[0] == null || param.password[0].equals("") || param.password[1] == null || param.password[1].equals("") || !param.password[0].equals(param.password[1])){
         	return jsonResult("password");
         }
         
         UserinfoDao ud = new UserinfoDao();
         Userinfo ui = ud.findUserinfoByname(param.username);
         if(ui != null){
             return jsonResult("username");
         }
         
         ui = new Userinfo();
         ui.setUname(param.username);
         ui.setUpassword(MD5Util.MD5(param.password[0]));
         ui.setUmail(param.email);
         ud.addUserinfo(ui);
         
         ui = ud.findUserinfoByname(param.username);
         session("myUserinfo",ui);
     	return jsonResult("ok");
     }
     
     
     public static class ChangePasswordParam {
         String oldPassword;
         String newPassword[];
     }
     
     public String changePassword(){
     	ChangePasswordParam param = (ChangePasswordParam) getParam(ChangePasswordParam.class);
     	if(param.oldPassword == null || param.oldPassword.equals("")){
     		return jsonResult("oldPassword");
     	}
     	
     	if(param.newPassword[0] == null || param.newPassword[0].equals("") || param.newPassword[1] == null || param.newPassword[1].equals("") || !param.newPassword[0].equals(param.newPassword[1])){
     		return jsonResult("newPassword");
     	}
     	
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo ui = (Userinfo)session("myUserinfo");
     	ui.setUpassword(MD5Util.MD5(param.newPassword[0]));
     	ud.updateUserinfo(ui);
     	
     	return jsonResult("ok");
     }
     
     
     public String getMyUserinfo(){
     	return jsonResult(session("myUserinfo"));
     }
     
     
     public static class EditMyBasicInfoParam{
     	String username;
     	String email;
    	String qq;
     	String phone;
     }
     
     public String editMyBasicInfo(){
     	EditMyBasicInfoParam param = (EditMyBasicInfoParam) getParam(EditMyBasicInfoParam.class);
     	UserinfoDao ud = new UserinfoDao();
    	Userinfo ui = ud.findUserinfoByname(param.username);
    	if(ui != null){
     		return jsonResult("username");
    	}    	
     	
    	ui = (Userinfo)session("myUserinfo");
     	ui.setUname(param.username);
    	ui.setUmail(param.email);
    	ui.setUphone(param.phone);
    	ui.setUphone(param.phone);
     	    	
     	return jsonResult("ok");
     }
     
     
     public static class EditExtraSettingParam{
     	String portrait;
     	String sketch;
     }
     
     public String editExtraSetting(){
     	EditExtraSettingParam param = (EditExtraSettingParam) getParam(EditExtraSettingParam.class);
     	
     	if(param.portrait == null || param.portrait.equals("")){
     		return jsonResult("portrait");
     	}
    	if(param.sketch == null){
    		return jsonResult("sketch");
    	}
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo ui;
     	ui = (Userinfo)session("myUserinfo");
    	ui.setUname(param.portrait);
    	ui.setUmail(param.sketch);
     	ud.updateUserinfo(ui);
     	
     	return jsonResult("ok");
     }
     
     
     public static class EditLikeLableSettingParam{
     	String label;
     }
     
     public String editLikeLableSetting(){
     	EditLikeLableSettingParam param = (EditLikeLableSettingParam) getParam(EditLikeLableSettingParam.class);
     	if(param.label == null){
     		return jsonResult("label");
     	}
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo ui;
     	ui = (Userinfo)session("myUserinfo");
    	ui.setUname(param.label);
     	ud.updateUserinfo(ui);
     	
     	return jsonResult("ok");
     }
     
     public String logout(){
     	session("myUserinfo",null);
     	return jsonResult("ok");
     }
 }
