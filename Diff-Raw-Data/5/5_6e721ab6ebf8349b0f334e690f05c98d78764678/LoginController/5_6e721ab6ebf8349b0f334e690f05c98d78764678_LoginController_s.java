 package com.fl.action;
 
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.security.MessageDigest;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.util.WebUtils;
 
 import com.fl.RenRenConstant;
 import com.fl.TaobaoConstant;
 import com.taobao.api.ApiException;
 import com.taobao.api.Constants;
 import com.taobao.api.DefaultTaobaoClient;
 import com.taobao.api.TaobaoClient;
 import com.taobao.api.internal.util.StringUtils;
 import com.taobao.api.internal.util.TaobaoUtils;
 import com.taobao.api.request.UserGetRequest;
 import com.taobao.api.response.UserGetResponse;
 
 @Controller
 public class LoginController {
 
 	@RequestMapping("/login/taobao")
 	public String taobao(HttpServletResponse res) throws IOException {
 		long timestamp = (new Date()).getTime();
 		String message = TaobaoConstant.APP_SERCET + "app_key"
 				+ TaobaoConstant.APP_KEY + "timestamp" + timestamp
 				+ TaobaoConstant.APP_SERCET;
 		Cookie c = new Cookie("timestamp", String.valueOf(timestamp));
 		Cookie c1 = new Cookie("sign", byte2hex(encryptMD5(message)));
 		res.addCookie(c);
 		res.addCookie(c1);
 		return "/login/index";
 	}
 
 	@RequestMapping("/other/login/{platform}")
 	public String otherLogin(@PathVariable String platform,
 			HttpServletResponse res) {
 		if ("taobao".equals(platform)) {
 			return "redirect:https://oauth.taobao.com/authorize?response_type=code&client_id="
 					+ TaobaoConstant.APP_KEY
 					+ "&redirect_uri=http://www.fl.com/other/taobaocallback&state=1212&scope=item&view=web";
 		} else if ("renren".equals(platform)) {
 			return "redirect:https://graph.renren.com/oauth/authorize?client_id="
 					+ RenRenConstant.APP_KEY
 					+ "&redirect_uri=http://www.fl.com/other/renrencallback&response_type=code";
 		}
 		return "";
 	}
 
 	@RequestMapping("/other/taobaocallback")
 	public String loginFromTaobao(HttpServletRequest req,
 			HttpServletResponse res) {
 		Map<String, Object> map = WebUtils.getParametersStartingWith(req, "");
 		String code = (String) map.get("code");
 		String state = (String) map.get("state");
 
 		Map<String, String> param = new HashMap<String, String>();
 		param.put("grant_type", "authorization_code");
 		param.put("code", code);
 		param.put("client_id", TaobaoConstant.APP_KEY);
 		param.put("client_secret", TaobaoConstant.APP_SERCET);
 		param.put("redirect_uri", "http://www.fl.com/other/taobaocallback");
 		param.put("scope", "item");
 		param.put("view", "web");
 		param.put("state", state);
 		try {
 			String responseJson = com.taobao.api.internal.util.WebUtils.doPost(
 					"https://oauth.taobao.com/token", param, 3000, 3000);
 			Map<?, ?> resmap = TaobaoUtils.parseJson(responseJson);
 			String taobaoid = resmap.get("taobao_user_id").toString();
 			String uname = (String) resmap.get("taobao_user_nick");
 			if (!StringUtils.isEmpty(uname)) {
 				uname = com.taobao.api.internal.util.WebUtils.decode(uname,
 						"UTF-8");
 			}
 			String actoken = (String) resmap.get("access_token");
 			TaobaoClient client = new DefaultTaobaoClient(
 					TaobaoConstant.APP_URL, TaobaoConstant.APP_KEY,
 					TaobaoConstant.APP_SERCET);
 			UserGetRequest userreq = new UserGetRequest();// 实例化具体API对应的Request类
 			userreq.setFields("user_id,uid,nick,sex,buyer_credit,seller_credit,location,created,last_visit,birthday,type,status,alipay_no,alipay_account,alipay_account,email,consumer_protection,alipay_bind");
 			userreq.setNick(uname);
 			try {
 				UserGetResponse response = client.execute(userreq);
 				response.getBody();
 				res.getWriter().print(response.getBody());
 			} catch (ApiException e) {
 				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
		return "";
 	}
 
 	@RequestMapping("/other/renrencallback")
 	public String loginFromRenRen(HttpServletRequest req,
 			HttpServletResponse res) throws IOException {
 		Map<String, Object> map = WebUtils.getParametersStartingWith(req, "");
 		String code = (String) map.get("code");
 		String actoken = (String) map.get("access_token");
 		res.getOutputStream().print(code);
 		return "";
 	}
 
 	private static byte[] encryptMD5(String data) throws IOException {
 		byte[] bytes = null;
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			bytes = md.digest(data.getBytes(Constants.CHARSET_UTF8));
 		} catch (GeneralSecurityException gse) {
 			throw new IOException(gse.getMessage());
 		}
 		return bytes;
 	}
 
 	private static String byte2hex(byte[] bytes) {
 		StringBuilder sign = new StringBuilder();
 		for (int i = 0; i < bytes.length; i++) {
 			String hex = Integer.toHexString(bytes[i] & 0xFF);
 			if (hex.length() == 1) {
 				sign.append("0");
 			}
 			sign.append(hex.toUpperCase());
 		}
 		return sign.toString();
 	}
 }
