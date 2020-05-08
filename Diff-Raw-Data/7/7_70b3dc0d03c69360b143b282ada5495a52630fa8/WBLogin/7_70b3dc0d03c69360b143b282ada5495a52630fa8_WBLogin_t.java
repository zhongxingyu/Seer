 /**
  * 
  */
 package com.k99k.wb.acts;
 
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.k99k.khunter.Action;
 import com.k99k.khunter.ActionMsg;
 import com.k99k.khunter.HTManager;
 import com.k99k.khunter.HttpActionMsg;
 import com.k99k.khunter.KFilter;
 import com.k99k.khunter.KIoc;
 import com.k99k.khunter.KObject;
 import com.k99k.tools.JSONTool;
 import com.k99k.tools.encrypter.Base64Coder;
 
 /**
  * 微博登录与注销
  * @author keel
  *
  */
 public class WBLogin extends Action {
 
 	/**
 	 * @param name
 	 */
 	public WBLogin(String name) {
 		super(name);
 	}
 	
 	static final Logger log = Logger.getLogger(WBLogin.class);
 	
 	/**
 	 * 模板化处理后的String[]
 	 */
 	private static String[] logOutStrArr;
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
 	 */
 	@Override
 	public ActionMsg act(ActionMsg msg) {
 		String subact = KFilter.actPath(msg, 2, "show");
 		
 		//显示login入口
 		if (subact.equals("show")) {
 			msg.addData("[jsp]", "/WEB-INF/wb/login.jsp");
 			return super.act(msg);
 		}
 		//login操作
 		else if(subact.equals("login")){
 			HttpActionMsg httpmsg = (HttpActionMsg)msg;
 			login(httpmsg);
 		}
 		//logout操作
 		else if(subact.equals("logout")){
 			HttpActionMsg httpmsg = (HttpActionMsg)msg;
 			logout(httpmsg);
 		}
 		
 		return super.act(msg);
 	}
 
 	public static final void login(HttpActionMsg httpmsg){
 		String uName = httpmsg.getHttpReq().getParameter("uName");
 		String uPwd = httpmsg.getHttpReq().getParameter("uPwd");
 		if (uName != null && uPwd != null && uName.toString().trim().length()>3 && uPwd.toString().trim().length()>=6) {
 			KObject user = WBUser.findWBUser(uName);
 			if (user != null && user.getProp("pwd").toString().equals(uPwd)) {
 				//验证成功
 				JOut.txtOut("ok", httpmsg);
 				//加入cookie
 				if (httpmsg.getHttpReq().getParameter("uCookie") != null) {
 					WBUrl.setCookie("wbu", Base64Coder.encodeString(uName), httpmsg.getHttpResp());
 				}
				return;
 			}
 		}
 		JOut.err(401, httpmsg);
 	}
 
 	public static final void logout(HttpActionMsg httpmsg){
 		String uName = httpmsg.getHttpReq().getParameter("uName");
 		if (uName != null) {
 			JOut.txtOut(logOutStrArr, new String[]{uName,"Logged out."}, httpmsg);
 			WBUrl.removeCookie("wbu", httpmsg.getHttpResp());
			return;
 		}
		JOut.err(401, httpmsg);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#exit()
 	 */
 	@Override
 	public void exit() {
 
 	}
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#getIniPath()
 	 */
 	@Override
 	public String getIniPath() {
 		return "wbOutTemplet.json";
 	}
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#init()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void init() {
 		try {
 			String ini = KIoc.readTxtInUTF8(HTManager.getIniPath()+getIniPath());
 			Map<String,?> root = (Map<String,?>) JSONTool.readJsonString(ini);
 			//先定位到json的cookies属性
 			Map<String, ?> wbuMap = (Map<String, ?>) root.get("wbLogin");
 			logOutStrArr = wbuMap.get("logout").toString().split("###");
 		} catch (Exception e) {
 			log.error("WBLogin init Error!", e);
 		}
 	}
 
 }
