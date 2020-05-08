 package de.raion.xmppbot.plugin;
 /*
  * #%L
  * XmppBot Commands
  * %%
  * Copyright (C) 2012 - 2013 Bernd Kiefer <b.kiefer@raion.de>
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 
 import java.net.URI;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.api.client.filter.LoggingFilter;
 
 import de.raion.xmppbot.XmppBot;
 import de.raion.xmppbot.XmppContext;
 import de.raion.xmppbot.command.JiraConfig;
 import de.raion.xmppbot.filter.MessageBodyMatchesFilter;
 
 /**
  * listen for messages with jira related issues and posts summary and links into chat.
  *
  * @Todo implement authentication for non public jira instances
  */
 @MessageListenerPlugin(name="jira-issues", description="provides summary and link to jira issues when mentioned in chat")
 public class JiraIssuePlugin extends AbstractMessageListenerPlugin<JiraIssuePlugin> {
 
 	private static Logger log = LoggerFactory.getLogger(JiraIssuePlugin.class);
 
 	private ObjectMapper mapper;
 
 	private MessageBodyMatchesFilter acceptFilter;;
 
 	private Pattern pattern;
 
 	private JiraConfig config;
 
 	private Client client;
 	
 	private JsonNode issueNode;
 
 
 	/**
 	 * constructor
 	 * @param aXmppBot reference
 	 */
 	public JiraIssuePlugin(XmppBot aXmppBot) {
 		super(aXmppBot);
 
 		mapper = new ObjectMapper();
 		client = Client.create();
 		acceptFilter = new MessageBodyMatchesFilter(""); // correct initialization in init
 		init();
 	}
 
 
 	/**
 	 * reloads configuration
 	 */
 	public void updateConfiguration() {
 		init();
 	}
 
 
 	@Override
 	public PacketFilter getAcceptFilter() {
 		return acceptFilter;
 	}
 
 
 	@Override
 	public void processMessage(XmppContext xmppContext, Chat chat,
 			Message message) {
 		processMessage(xmppContext, message);
 	}
 
 
 	@Override
 	public void processMessage(XmppContext xmppContext, MultiUserChat muc,
 			Message message) {
 		processMessage(xmppContext, message);
 	}
 
 
 	/**
 	 * retrieves key and name of jira projects
 	 * @param projectsUri the uri of the projects resource
 	 * @return key and name of jira projects
 	 */
 	public Map<String, String> getProjects(URI projectsUri) {
 
 		TreeMap<String, String> map = new TreeMap<String, String>();
 
 		try {
 			ClientResponse response = client.resource(projectsUri).get(ClientResponse.class);
 
 			if(response.getClientResponseStatus() == Status.OK) {
 				JsonNode rootNode = mapper.readValue(response.getEntityInputStream(), JsonNode.class);
 				List<String> keyList = rootNode.findValuesAsText("key");
 				List<String> nameList = rootNode.findValuesAsText("name");
 
 				for(int i=0; i<keyList.size(); i++) {
 					map.put(keyList.get(i), nameList.get(i));
 				}
 			}
 
 		}catch(Exception e) {
 			log.error("getProjects(URI)", e);
 			return map;
 		}
 		return map;
 	}
 
 
 	/**
 	 * creates a matching pattern
 	 * @param keySet source
 	 * @return pattern
 	 */
 	public String createMatchingPattern(Set<String> keySet) {
 
 		if(keySet.size() > 0) {
 			StringBuilder builder = new StringBuilder("(");
 
 			Iterator<String> it = keySet.iterator();
 
 			while(it.hasNext()) {
 				builder.append(it.next()).append("-\\d+");
 				if(it.hasNext()) {
 					builder.append("|");
 				}
 			}
 			builder.append(")");
 
 			Pattern aPattern = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
 			return aPattern.pattern();
 		}
 
 		return "";
 	}
 
 	public boolean matches(String aString) {
		if(pattern != null)
			return pattern.matcher(aString).find();
		return Boolean.FALSE;
 	}
 
 	/**
 	 * @return the last jsonnode fetched by the plugin, can be <code>null</code>
 	 */
 	public JsonNode getCurrentIssue() { return issueNode; }
 	
 	private void processMessage(XmppContext xmppContext, Message message) {
 
 		Matcher matcher = pattern.matcher(message.getBody());
 
 		while(matcher.find()) {
 
 			String issue = matcher.group();
 
 			try {
 
 				URI issueUri = config.getIssueURI(issue);
 
 				ClientResponse response = client.resource(issueUri).get(ClientResponse.class);
 
 				if(response.getClientResponseStatus() == Status.OK) {
 					issueNode = mapper.readValue(response.getEntityInputStream(), JsonNode.class);
 					String issueSummary = issueNode.findValue("summary").textValue();
 
 					StringBuilder builder = new StringBuilder();
 					builder.append("[").append(issue).append("] - ");
 					builder.append(issueSummary).append(" : ");
 					builder.append(config.getIssueBrowseURI(issue).toString()).append("\n");
 
 					xmppContext.println(builder.toString());
 				}
 
 
 
 			} catch (Exception e) {
 				log.error("processMessage(XmppContext, Message) - {}", e.getMessage());
 			}
 		}
 	}
 
 
 	private void init() {
 
 		config  = getContext().loadConfig(JiraConfig.class);
 
 		if(config == null)
 			return;
 		
 		String user = config.getAuthenticationUser();
 		String pwd = config.getAuthenticationPassword();
 
 		if(user != null && pwd != null) {
 			HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter(user, pwd);
 			client.addFilter(authFilter);
 			log.info("credentials for basic authentication added");
 		}
 
 		if(log.isDebugEnabled()) {
 			client.addFilter(new LoggingFilter());
 		}
 
 		String regex = config.getMatchingPattern();
 
 		if(regex != null) {
 			acceptFilter.setPattern(regex);
 			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
 			log.info("using pattern '{}' for matching", regex);
 		}
 	}
 }
