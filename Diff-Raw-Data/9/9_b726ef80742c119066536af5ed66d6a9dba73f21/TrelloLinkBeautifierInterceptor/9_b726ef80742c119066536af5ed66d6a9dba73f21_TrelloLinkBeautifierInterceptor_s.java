 package de.raion.xmppbot.hipchat;
 
 
 import static de.raion.xmppbot.command.util.HtmlUtils.createAnchorTag;
 import static de.raion.xmppbot.command.util.HtmlUtils.createImageTag;
 
 import java.awt.Color;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.filter.PacketTypeFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.LoggingFilter;
 
 import de.raion.xmppbot.AbstractPacketInterceptor;
 import de.raion.xmppbot.annotation.PacketInterceptor;
 import de.raion.xmppbot.command.TrelloConfig;
 import de.raion.xmppbot.command.util.HtmlUtils;
 import de.raion.xmppbot.plugin.PluginManager;
 import de.raion.xmppbot.plugin.TrelloCardPlugin;
 import de.raion.xmppbot.util.PacketUtils;
 
 @PacketInterceptor(service = "hipchat")
 public class TrelloLinkBeautifierInterceptor  extends AbstractPacketInterceptor{
 
 	private static Logger log = LoggerFactory.getLogger(TrelloLinkBeautifierInterceptor.class);
 	
 	private static HashMap<String, Color> COLOR_MAP = new  HashMap<String, Color>();
 	
 	static {
 		COLOR_MAP.put("red", Color.decode("#C11B17"));
 		COLOR_MAP.put("blue", Color.decode("#356FF6"));
 		COLOR_MAP.put("green", Color.decode("#57E964"));
 		COLOR_MAP.put("purple", Color.decode("#800080"));
 		COLOR_MAP.put("yellow", Color.decode("#EAC117"));
 		COLOR_MAP.put("orange", Color.decode("#F88017"));
 		
 	}
 	
 	private HipChatAPIConfig apiConfig;
 	
 	private String matchingPattern = "https://trello.com/c/(\\w+)";
 	
 	private Pattern pattern;
 
 	private Client client;
 
 	private ObjectMapper mapper;
 
 	
 	public TrelloLinkBeautifierInterceptor() {
 		pattern = Pattern.compile(matchingPattern, Pattern.CASE_INSENSITIVE);
 		client = Client.create();
 		client.addFilter(new LoggingFilter());
 		mapper = new ObjectMapper();
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
 		
 		if(pluginManager.isEnabled(TrelloCardPlugin.class)) {
 			
 			if(pattern.matcher(xmppMessage.getBody()).find()) {
 				rewriteMessage(xmppMessage);
 			}
 		}
 	}
 
 	
 	@Override
 	public PacketFilter getPacketFilter() {
 		return new PacketTypeFilter(Message.class);
 	}
 
 	
 	private void rewriteMessage(Message xmppMessage) {
 		
 		TrelloConfig config = getContext().loadConfig(TrelloConfig.class);
 		
 		Matcher matcher = pattern.matcher(xmppMessage.getBody());
 		
 		while(matcher.find()) {
 			String shortUrl = matcher.group();
 			String shortId = matcher.group(1);
 			
 			WebResource resource = client.resource(config.getCardBaseUrl())
 					                     .path(shortId)
 					                     .queryParam("key", config.getApplicationKey())
 					                     .queryParam("token", config.getAccessToken())
 					                     .queryParam("members", "true");
 			
 			ClientResponse response = resource.get(ClientResponse.class);
 			
 			if(response.getClientResponseStatus() == Status.OK) {
 				try {
 					JsonNode cardNode = mapper.readValue(response.getEntityInputStream(),  JsonNode.class);
 					
 					String messageText = createMessageText(shortUrl, cardNode);
 					
 					// todo do better
 					String roomId = PacketUtils.getToName(xmppMessage);
 					int index = roomId.indexOf("_");
 					if(index != -1)
						roomId = roomId.substring(index);
 					
 					// nickname used by the bot for the configuration 'hipchat'
 					String nickName = getContext().getBot().getNickName(getClass().getAnnotation(PacketInterceptor.class).service());
 					
 					String color = getColor(cardNode);
 					
 					resource = client.resource("https://api.hipchat.com/v1/rooms/message")
 		                             .queryParam("room_id", roomId)
 		                             .queryParam("from", nickName)
 		                             .queryParam("message", messageText)
 		                             .queryParam("message_format", "html")
 		                             .queryParam("color", color)
 		                             .queryParam("auth_token", apiConfig.getAuthenticationToken());
 
 					response = resource.post(ClientResponse.class);
 
 					if(response.getClientResponseStatus() == Status.OK) {
 						log.info("sent message for issue [{}] to room {}", shortId, roomId );
 					}
 					else {
 						log.warn("sending message for {} failed, status = {}", "["+shortId+"] to "+roomId, response.getStatus());
 						log.warn(response.getEntity(String.class));
 					}
 					
 					
 				
 				} catch (Exception e) {
 					log.error(e.getMessage());
 				}finally {
 					// this is a hack :(
 					xmppMessage.setBody(null);
 					throw new IllegalArgumentException("TrelloLinkBeautifier: preventing message sending via xmpp. message already sent via hipchat web api");
 				}
 			}
 			
 		}
 		
 	}
 
 
 	private String getColor(JsonNode cardNode) {
 		
 		Boolean closed = cardNode.path("closed").asBoolean(false);
 		if(closed)
 			return "green";
 		else
 			return "yellow";
 	}
 
 
 	private String createMessageText(String shortUrl, JsonNode cardNode) {
 		
 		String idShort = cardNode.path("idShort").asText();
 		String name    = cardNode.path("name").asText();
 		
 		JsonNode labelsNode = cardNode.path("labels");
 		
 		String labels = createLables(labelsNode);
 				
 		StringBuilder messageBuilder = new StringBuilder();
 		StringBuilder builder = new StringBuilder();
 		builder.append("<b> #").append(idShort).append(": </b>").append(name);
 		
 		messageBuilder.append(createImageTag("https://trello.com/favicon.ico", "trello.com", "20", "20"));
 		messageBuilder.append(" ");
 		messageBuilder.append(createAnchorTag(builder.toString(), shortUrl));
 		messageBuilder.append(" ").append(labels);
 		
 		return messageBuilder.toString();
 		
 	}
 
 
 	private String createLables(JsonNode labelsNode) {
 		int size = labelsNode.size();
 		StringBuilder builder = new StringBuilder();
 		
 		for(int i=0; i<size; i++) {
 			String name = labelsNode.get(i).path("name").asText();
 			String colorName= labelsNode.get(i).path("color").asText();
 						
 			Color color = getColor(colorName);
 			String imageUrl = HtmlUtils.toDummyImageUrl(10, color);
 			builder.append(createImageTag(imageUrl, colorName));
 		}
 		
 		return builder.toString();
 	}
 
 
 	private static Color getColor(String colorName) {
 		colorName = colorName.toLowerCase();
 		
 		if(COLOR_MAP.containsKey(colorName)) {
 			return COLOR_MAP.get(colorName);
 		} else
 			return HtmlUtils.convertHtmlColorNameToColor(colorName);
 	}
 
 }
