 package skype.shell;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import skype.ChatAdapterInterface;
 
 public class AliasProcessorImpl implements AliasProcessor {
 	final private Map<String, String> aliases = new LinkedHashMap<String, String>();	
 
 	@Override
 	public boolean understands(String message) {
 		return isAddAlias(message) || isListAlias(message);
 	}
 	
 	@Override
 	public String expandMessage(String message) {
 		return expandMessageIfNeeded(message);
 	}
 	
 	@Override
 	public ShellCommand processMessage(ChatAdapterInterface chat, String message) {
 		if (!understands(message))
 			return null;
 		
 		if (isAddAlias(message))
 			return processAliasRequestAndGenerateReply(chat, message);
 		if (isListAlias(message))
 			return processListAliasRequest(chat, message);
 		
 		return processAliasRequestAndGenerateReply(chat, message);
 	}
 
 	private boolean isAddAlias(String message) {
 		return message.trim().toLowerCase().startsWith("#alias ");
 	}
 	
 	private boolean isListAlias(String message) {
 		return message.trim().equalsIgnoreCase("#aliaslist");
 	}
 
 	private ShellCommand processListAliasRequest(ChatAdapterInterface chat, String message) {
 		StringBuilder sb = new StringBuilder();
 		for (Entry<String, String> entry : aliases.entrySet()) {
 			sb.append(entry.getKey()+" : " + entry.getValue()+"\n");
 		}
 		String reply = "Registered aliases:\n" + sb.toString().trim();
 		return new ReplyTextRequest(chat, message, reply);
 	}
 	
	private String expandMessageIfNeeded(String m) {
		String expanded = aliases.get(m);
 		if (expanded == null)
			return m;
		return expanded;
 	}
 
 	private ShellCommand processAliasRequestAndGenerateReply(ChatAdapterInterface chat, String message) {
 		String msg = message.replaceAll("#alias\\s+", "");
		String alias = msg.replaceAll("(.*)\\s.*", "$1");
 		String expanded = msg.replace(alias, "").trim();
 		String actualAlias = "#"+alias;
 		aliases.put(actualAlias, expanded);
 		
 		String reply = String.format("Alias '%s' created to expand to <%s>",actualAlias,expanded);
 		ReplyTextRequest request = new ReplyTextRequest(chat, message, reply);
 		return request;
 	}
 
 }
