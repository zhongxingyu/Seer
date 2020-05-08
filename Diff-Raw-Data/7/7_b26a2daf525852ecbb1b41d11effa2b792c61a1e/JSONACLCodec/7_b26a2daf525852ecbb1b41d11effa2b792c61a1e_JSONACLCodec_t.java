 package org.janusproject.acl.encoding;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.arakhne.vmutil.locale.Locale;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.janusproject.acl.ACLMessage;
 import org.janusproject.acl.ACLMessageContent;
 import org.janusproject.kernel.address.AgentAddress;
 import org.janusproject.kernel.crio.core.AddressUtil;
 
 public class JSONACLCodec implements ACLMessageContentEncodingService {
 
 	@Override
 	public byte[] encode(ACLMessage aMsg) {
 
 		Map<String, Object> output = new HashMap<String, Object>();
 
 		// Performative
 		output.put(Locale.getString(JSONACLCodec.class, "PERFORMATIVE"), aMsg
 				.getPerformative().ordinal());
 
 		// Display SENDER
 		AgentAddress sender = aMsg.getSender();
 		if (sender != null) {
 			Map<String, String> sender_infos = new HashMap<String, String>();
 			sender_infos.put(Locale.getString(JSONACLCodec.class, "NAME"),
 					sender.getName());
 			sender_infos.put(Locale.getString(JSONACLCodec.class, "ID"), sender
 					.getUUID().toString());
 			output.put(Locale.getString(JSONACLCodec.class, "SENDER"),
 					sender_infos);
 		}
 
 		// Display RECEIVERS
 		Collection<AgentAddress> receivers = aMsg.getReceiver();
 		Collection<Map<String, String>> receivers_infos = new ArrayList<Map<String, String>>();
 
 		if (receivers != null) {
 			for (AgentAddress receiver : receivers) {
 				Map<String, String> receiver_info = new HashMap<String, String>();
 				receiver_info.put(Locale.getString(JSONACLCodec.class, "NAME"),
 						receiver.getName());
 				receiver_info.put(Locale.getString(JSONACLCodec.class, "ID"),
 						receiver.getUUID().toString());
 				receivers_infos.add(receiver_info);
 			}
 			output.put(Locale.getString(JSONACLCodec.class, "RECEIVERS"),
 					receivers_infos);
 		}
 
 		// Display CONTENT
 		String content = aMsg.getContent().getContent().toString();
 		if (content != null && content.length() > 0) {
 			output.put(Locale.getString(JSONACLCodec.class, "CONTENT"),
 					content.trim());
 		}
 
 		// Display ENCODING
 		output.put(Locale.getString(JSONACLCodec.class, "ENCODING"),
 				aMsg.getEncoding());
 
 		// Display LANGUAGE
 		output.put(Locale.getString(JSONACLCodec.class, "LANGUAGE"),
 				aMsg.getLanguage());
 
 		// Display ONTOLOGY
 		output.put(Locale.getString(JSONACLCodec.class, "ONTOLOGY"),
 				aMsg.getOntology());
 
 		// Display PROTOCOL
 		output.put(Locale.getString(JSONACLCodec.class, "PROTOCOL"), aMsg
 				.getProtocol().getName());
 
 		// Display CONVERSATION ID
		UUID conversationId = aMsg.getConversationId();
		if (conversationId != null) {
			output.put(Locale.getString(JSONACLCodec.class, "CONVERSATIONID"),
					aMsg.getConversationId().toString());
		}
 
 		return fromMap(output);
 	}
 
 	@Override
 	public ACLMessageContent decode(byte[] byteMsg, Object... parameters) {
 		ACLMessage.Content content = new ACLMessage.Content();
 
 		Map<String, Object> json = fromBytes(byteMsg);
 
 		// PERFORMATIVE
 		content.setPerformative((Integer) json.get(Locale.getString(
 				JSONACLCodec.class, "PERFORMATIVE")));
 
 		// SENDER
 		if (json.containsKey(Locale.getString(JSONACLCodec.class, "SENDER"))) {
 			@SuppressWarnings("unchecked")
 			String sender_id = (String) ((Map<String, Object>) json.get(Locale
 					.getString(JSONACLCodec.class, "SENDER"))).get(Locale
 					.getString(JSONACLCodec.class, "ID"));
 			content.setSender(AddressUtil.createAgentAddress(UUID
 					.fromString(sender_id)));
 		}
 
 		// RECEIVERS
 		if (json.containsKey(Locale.getString(JSONACLCodec.class, "RECEIVERS"))) {
 			@SuppressWarnings("unchecked")
 			ArrayList<Map<String, Object>> json_receivers = (ArrayList<Map<String, Object>>) json
 					.get(Locale.getString(JSONACLCodec.class, "RECEIVERS"));
 			Collection<AgentAddress> receivers = new ArrayList<AgentAddress>();
 
 			for (int i = 0; i < json_receivers.size(); i++) {
 				String receiver_info = (String) json_receivers.get(i).get(
 						Locale.getString(JSONACLCodec.class, "ID"));
 				receivers.add(AddressUtil.createAgentAddress(UUID
 						.fromString(receiver_info)));
 			}
 			content.setReceiver(receivers);
 		}
 
 		// CONTENT
 		if (json.containsKey(Locale.getString(JSONACLCodec.class, "CONTENT"))) {
 			content.setContent(new StringBuffer((String) json.get(Locale
 					.getString(JSONACLCodec.class, "CONTENT"))));
 		}
 
 		// ENCODING
 		content.setEncoding((String) json.get(Locale.getString(
 				JSONACLCodec.class, "ENCODING")));
 
 		// LANGUAGE
 		if (json.containsKey(Locale.getString(JSONACLCodec.class, "LANGUAGE"))) {
 			content.setLanguage((String) json.get(Locale.getString(
 					JSONACLCodec.class, "LANGUAGE")));
 		}
 
 		// ONTOLOGY
 		if (json.containsKey(Locale.getString(JSONACLCodec.class, "ONTOLOGY"))) {
 			content.setOntology((String) json.get(Locale.getString(
 					JSONACLCodec.class, "ONTOLOGY")));
 		}
 
 		// PROTOCOL
 		content.setProtocol((String) json.get(Locale.getString(
 				JSONACLCodec.class, "PROTOCOL")));
 
 		// CONVERSATION ID
 		if (json.containsKey(Locale.getString(JSONACLCodec.class,
 				"CONVERSATIONID"))
 				&& json.get(Locale.getString(JSONACLCodec.class,
 						"CONVERSATIONID")) != null) {
 
 			try {
 				String uuid = (String) json.get(Locale.getString(
 						JSONACLCodec.class, "CONVERSATIONID"));
 				content.setConversationId(UUID.fromString(uuid));
 			} catch (IllegalArgumentException e) {
 				content.setConversationId(null);
 			}
 		}
 
 		return content;
 	}
 
 	protected ObjectMapper getMapper() {
 		return new ObjectMapper();
 	}
 
 	private byte[] fromMap(Map<String, Object> m) {
 		ObjectMapper mapper = getMapper();
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		try {
 			mapper.writeValue(baos, m);
 		} catch (JsonGenerationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(new String(baos.toByteArray()));
 		return baos.toByteArray();
 	}
 
 	private Map<String, Object> fromBytes(byte[] byteMsg) {
 		ObjectMapper mapper = getMapper();
 		try {
 			return mapper.readValue(byteMsg,
 					new TypeReference<Map<String, Object>>() {
 					});
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
