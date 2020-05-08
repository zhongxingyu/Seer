 package de.raion.xmppbot.hipchat;
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
 
 
 import static de.raion.xmppbot.command.util.HtmlUtils.createAnchorTag;
 import static de.raion.xmppbot.command.util.HtmlUtils.createImageTag;
 
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.filter.PacketTypeFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.LoggingFilter;
 
 import de.raion.xmppbot.AbstractPacketInterceptor;
 import de.raion.xmppbot.annotation.PacketInterceptor;
 import de.raion.xmppbot.plugin.JiraIssuePlugin;
 import de.raion.xmppbot.plugin.PluginManager;
 import de.raion.xmppbot.util.PacketUtils;
 
 /**
  * 
  * @author b.kiefer
  *
  */
 @PacketInterceptor(service = "hipchat")
 public class JiraLinkBeautifierInterceptor extends AbstractPacketInterceptor {
 
 	private static Logger log = LoggerFactory.getLogger(JiraLinkBeautifierInterceptor.class);
 	
 	private HipChatAPIConfig apiConfig;
 
 	private Client client;
 	
 	public JiraLinkBeautifierInterceptor() {
 		client = Client.create();
 		client.addFilter(new LoggingFilter());
 	}
 	
 	@Override
 	public void interceptPacket(Packet packet) {
 		
 		// packetfilter is set to MessageType
 		Message xmppMessage = (Message) packet;
 		PluginManager pluginManager = getContext().getPluginManager();
 		
 		if(apiConfig == null) {
 			apiConfig = getContext().loadConfig(HipChatAPIConfig.class);
 			
 			String authToken = apiConfig.getAuthenticationToken();
 			
 			if(authToken == null || authToken.equals("")) {
 				log.warn("no authToken configured for Hipchat, please update your hipchatapiconfig.json");
 				return;
 			}
 			
 		}
 		
 		if(pluginManager.isEnabled(JiraIssuePlugin.class) ) {
 			JiraIssuePlugin plugin = getContext().getPluginManager().get(JiraIssuePlugin.class);
 			
 			if(plugin.matches(xmppMessage.getBody())) {
 				
 				JsonNode issue = plugin.getCurrentIssue();
 				
 				
 				if(issue != null) {
 					
 					String issueKey = issue.findValue("key").textValue();
 					String status	= issue.path("fields").path("status").get("name").asText();
 					String priority	= issue.path("fields").path("priority").get("name").asText();	
 					
 					String messageText = createMessageText(xmppMessage, issue, plugin);
 										
 					// todo do better
 					// todo do better
 					String roomId = PacketUtils.getToName(xmppMessage);
 					int index = roomId.indexOf("_");
 					if(index != -1)
						roomId = roomId.substring(index+1);
 					
 					// nickname used by the bot for the configuration 'hipchat'
 					String nickName = getContext().getBot().getNickName(getClass().getAnnotation(PacketInterceptor.class).service());
 					
 					String color = getColor(status, priority);
 					
 					
 					WebResource resource = client.resource("https://api.hipchat.com/v1/rooms/message")
 							                      .queryParam("room_id", roomId)
 							                      .queryParam("from", nickName)
 							                      .queryParam("message", messageText)
 							                      .queryParam("message_format", "html")
 							                      .queryParam("color", color)
 							                      .queryParam("auth_token", apiConfig.getAuthenticationToken());
 					
 					ClientResponse response = resource.post(ClientResponse.class);
 					
 					if(response.getClientResponseStatus() == Status.OK) {
 						log.info("sent message for issue [{}] to room {}", issueKey, roomId );
 					}
 					else {
 						log.warn("sending message for {} failed, status = {}", "["+issueKey+"] to "+roomId, response.getStatus());
 						log.warn(response.getEntity(String.class));
 					}
 					
 					
 					// this is a hack :(
 					xmppMessage.setBody(null);
 					throw new IllegalArgumentException("JiraLinkBeautifier: preventing message sending via xmpp. message already sent via hipchat web api");
 				}
 			}
 		}
 		
 	}
 
 	// TODO better solution
 	private String getColor(String status, String priority) {
 		
 		if(priority.toLowerCase().equals("critical") && status.toLowerCase().equals("open"))
 			return "red";
 		
 		if(status.toLowerCase().equals("closed"))
 			return "green";
 		
 		return "yellow";
 	}
 
 	private String createMessageText(Message xmppMessage, JsonNode issue, JiraIssuePlugin plugin) {
 		String issueKey = issue.findValue("key").textValue();
 		String issueSummary = issue.findValue("summary").textValue();
 		
 		String issueIconUrl = issue.path("fields").path("issuetype").get("iconUrl").asText();
 		String issueType    = issue.path("fields").path("issuetype").get("name").asText();
 		String priorityUrl  = issue.path("fields").path("priority").get("iconUrl").asText();
 		String priority		= issue.path("fields").path("priority").get("name").asText();	
 		String statusUrl    = issue.path("fields").path("status").get("iconUrl").asText();
 		String status		= issue.path("fields").path("status").get("name").asText();
 		String reporterAcnt = issue.path("fields").path("reporter").get("name").asText();
 		String reporterName = issue.path("fields").path("reporter").get("displayName").asText();
 		String reporterIcon = issue.path("fields").path("reporter").path("avatarUrls").get("16x16").asText();
 		String assigneeAcnt = issue.path("fields").path("assignee").get("name").asText();
 		String assigneeName = issue.path("fields").path("assignee").get("displayName").asText();
 		String assigneeIcon = issue.path("fields").path("assignee").path("avatarUrls").get("16x16").asText();
 		
 		String line = xmppMessage.getBody();
 		int index = line.indexOf("http");
 		String issueUrl = line.substring(index).trim().replace("\n", "");
 		
 		
 		StringBuilder builder = new StringBuilder();
 		
 		String issueText = createIssueText(issueKey, issueSummary);
 		
 		
 		builder.append(createImageTag(issueIconUrl, issueType));
 		builder.append(" ").append(createAnchorTag(issueText, issueUrl));
 		
 		
 		// TODO do better ;)
 		if(!status.toLowerCase().equals("closed"))
 			builder.append(createImageTag(priorityUrl, priority));
 				
 		builder.append(createImageTag(statusUrl, status));
 		
 		builder.append("  Opened by ");
 	
 		builder.append(createAnchorTag(reporterName, createProfileUrl(plugin.getConfig().getJiraDomain(), reporterAcnt)));
 		builder.append(" ").append(createImageTag(reporterIcon, reporterName));
 		builder.append(". Current assignee is ");
 		builder.append(createAnchorTag(assigneeName, createProfileUrl(plugin.getConfig().getJiraDomain(), assigneeAcnt)));
 		builder.append(" ").append(createImageTag(assigneeIcon, assigneeName));
 				
 		return builder.toString();
 	}
 
 	// TODO 
 	private String createProfileUrl(String jiraDomain, String accountName) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("https://").append(jiraDomain);
 		builder.append("/secure/ViewProfile.jspa?name=");
 		builder.append(accountName);
 		
 		return builder.toString();
 	}
 
 	private String createIssueText(String issueKey, String issueSummary) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("<b>").append(issueKey).append("</b>");
 		builder.append(" - ").append(issueSummary);
 		return builder.toString();
 		
 	}
 
 	
 	
 
 	@Override
 	public PacketFilter getPacketFilter() {
 		return new PacketTypeFilter(Message.class);
 	}
 
 }
