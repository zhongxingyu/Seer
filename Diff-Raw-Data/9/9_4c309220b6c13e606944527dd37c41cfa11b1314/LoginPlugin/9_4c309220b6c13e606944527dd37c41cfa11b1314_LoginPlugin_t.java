 /*
  * Copyright 2011 Future Systems, Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.dom.msgbus;
 
 import java.util.UUID;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.dom.api.AdminApi;
 import org.araqne.dom.api.GlobalConfigApi;
 import org.araqne.dom.model.Admin;
 import org.araqne.msgbus.Request;
 import org.araqne.msgbus.Response;
 import org.araqne.msgbus.Session;
 import org.araqne.msgbus.handler.AllowGuestAccess;
 import org.araqne.msgbus.handler.CallbackType;
 import org.araqne.msgbus.handler.MsgbusMethod;
 import org.araqne.msgbus.handler.MsgbusPlugin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "dom-login-plugin")
 @MsgbusPlugin
 public class LoginPlugin {
 	private final Logger logger = LoggerFactory.getLogger(LoginPlugin.class);
 
 	@Requires
 	private GlobalConfigApi globalConfigApi;
 
 	@Requires
 	private AdminApi adminApi;
 
 	@AllowGuestAccess
 	@MsgbusMethod
 	public void getPrincipal(Request req, Response resp) {
 		Session session = req.getSession();
 		resp.put("org_domain", session.getString("org_domain"));
 		resp.put("admin_login_name", session.getString("admin_login_name"));
 		resp.put("auth", session.getString("auth"));
 	}
 
 	@AllowGuestAccess
 	@MsgbusMethod
 	public void hello(Request req, Response resp) {
 		String nonce = UUID.randomUUID().toString();
		String locale = req.getString("locale");
		if (locale == null)
			locale = "en";
 
 		req.getSession().setProperty("nonce", nonce);
		req.getSession().setProperty("locale", locale);
 
 		resp.put("nonce", nonce);
 		resp.put("session_id", req.getSession().getGuid());
 		resp.put("message", "login please.");
 		resp.putAll(globalConfigApi.getConfigs(false));
 	}
 
 	@AllowGuestAccess
 	@MsgbusMethod
 	public void login(Request req, Response resp) {
 		Session session = req.getSession();
 		session.setProperty("org_domain", "localhost");
 		String nick = req.getString("nick");
 		String hash = req.getString("hash");
 		String nonce = session.getString("nonce");
 		boolean force = req.has("force") ? req.getBoolean("force") : false;
 
 		logger.trace("araqne dom: login attempt nick [{}] hash [{}] nonce [{}]", new Object[] { nick, hash, nonce });
 
 		Admin admin = adminApi.login(session, nick, hash, force);
 		resp.put("result", "success");
 		resp.put("use_idle_timeout", admin.isUseIdleTimeout());
 		if (admin.isUseIdleTimeout())
 			resp.put("idle_timeout", admin.getIdleTimeout());
 
 		session.unsetProperty("nonce");
 		session.setProperty("admin_login_name", admin.getUser().getLoginName());
 		session.setProperty("locale", admin.getLang());
 		session.setProperty("auth", "dom");
 	}
 
 	@MsgbusMethod
 	public void logout(Request req, Response resp) {
 		Session session = req.getSession();
 
 		String auth = session.getString("auth");
 		if (auth != null && auth.equals("dom")) {
 			session.unsetProperty("org_domain");
 			session.unsetProperty("admin_login_name");
 			session.unsetProperty("auth");
 			adminApi.logout(session);
 		}
 	}
 
 	@MsgbusMethod(type = CallbackType.SessionClosed)
 	public void onLogout(Session session) {
 		String auth = session.getString("auth");
 		String loginName = session.getString("admin_login_name");
 
 		if (loginName != null && auth != null && auth.equals("dom")) {
 			adminApi.logout(session);
 		}
 	}
 }
