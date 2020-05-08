 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package jp.dip.komusubi.lunch.wicket.panel;
 
 import jp.dip.komusubi.lunch.model.User;
 import jp.dip.komusubi.lunch.wicket.WicketSession;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.PropertyModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * registry panel. 
  * @author jun.ozeki
  * @since 2011/11/19
  */
 public class EmailEntry extends Panel {
 	private static final long serialVersionUID = 3071722265831129774L;
 	private static final Logger logger = LoggerFactory.getLogger(EmailEntry.class);
 	private User user = new User();
 
 	public EmailEntry(String id, Class<? extends WebPage> nextPage) {
 		super(id);
 //		this.nextPage = nextPage;
 		add(new RegistryForm("registryForm"));
 	}
 	
 	public EmailEntry(String id) {
 	    this(id, null);
 	}
 	
 	@Override
 	public boolean isVisible() {
 		return !WicketSession.get().isSignedIn();
 	}
 	
	private class RegistryForm extends Form<Void> {
 		private static final long serialVersionUID = -2271065049710327798L;
 		
 		public RegistryForm(String id) {
 			super(id);
 			add(new EmailTextField("email", new PropertyModel<String>(user, "email"))
 					.setRequired(true));
 		}
 		@Override
 		public void onSubmit() {
 		    onRegister();
 /*
 		    // confirm page の absolute URLを取得
 			// FIXME apply to hollylwood principle
 			String targetPath = getRequestCycle().urlFor(nextPage, null).toString();
 			String ownUrl = getRequestCycle().getUrlRenderer().renderFullUrl(getRequest().getClientUrl());
 			String url = RequestUtils.toAbsolutePath(ownUrl, targetPath);
 			
 			// already exist email ?  
 			if (account.readByEmail(user.getEmail()) != null) {
 				error(getLocalizer().getString("already.exist.email", this, "email address already exist."));
 			} else {
 				Nonce nonce = account.apply(user, url);
 				// session に Nonceを保持
 				WicketSession.get().setAttribute(Nonce.class.getName(), nonce);
 				info(getLocalizer().getString("send.confirm", this, "send confirm email."));
 			}
 */			
 		}
 	}
 	
 	protected void onRegister() {
 	    logger.info("RegisterForm#onRegister");
 	}
 	
 	protected User getUser() {
 	    return user;
 	}
 }
