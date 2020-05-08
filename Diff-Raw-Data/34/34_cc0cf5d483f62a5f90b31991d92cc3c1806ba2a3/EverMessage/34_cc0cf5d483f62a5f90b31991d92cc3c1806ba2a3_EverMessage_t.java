 package com.evervoid.network;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.jme3.network.connection.Client;
 
 /**
  * The base class of all messages. Transparently splits itself into multiple PartialMessages. Subclasses should implement their
  * own serialization/deserialization, rather than exposing the underlying JSON structure.
  */
 public class EverMessage
 {
 	private Client aClient;
 	private final Json aJson;
 	private final String aType;
 
 	/**
 	 * Used to create new EverMessages containing lists
 	 * 
 	 * @param content
 	 *            The list of content
 	 * @param messageType
 	 *            The type of the message
 	 */
 	public EverMessage(final Collection<? extends Jsonable> content, final String messageType)
 	{
 		if (content == null) {
 			aJson = Json.getNullNode();
 		}
 		else {
 			aJson = new Json(content);
 		}
 		aType = messageType;
 	}
 
 	/**
 	 * Main constructor; used to create new EverMessages.
 	 * 
 	 * @param content
 	 *            The (Jsonable) content of the message
 	 * @param messageType
 	 *            The type of the message
 	 */
 	public EverMessage(final Jsonable content, final String messageType)
 	{
 		if (content == null) {
 			aJson = Json.getNullNode();
 		}
 		else {
 			aJson = content.toJson();
 		}
 		aType = messageType;
 	}
 
 	/**
 	 * @return The client that sent this message
 	 */
 	public Client getClient()
 	{
 		return aClient;
 	}
 
 	/**
 	 * Get the parsed Json object contained in this message. Called by subclasses only.
 	 * 
 	 * @return The parsed Json object inside this message
 	 */
 	public Json getJson()
 	{
 		return aJson;
 	}
 
 	/**
 	 * When it is time to send this message on the network, it needs to be converted to a list of partial messages
 	 * 
 	 * @return The list of sendable PartialMessages
 	 */
 	protected List<PartialMessage> getMessages()
 	{
 		String jsonString = aJson.toString();
 		final String hash = aJson.getHash();
 		final List<String> parts = new ArrayList<String>();
 		while (jsonString.length() > 0) {
 			final int partSize = Math.min(PartialMessage.sMaxPartialMessageSize, jsonString.length());
 			parts.add(jsonString.substring(0, partSize));
 			jsonString = jsonString.substring(partSize);
 		}
		final List<PartialMessage> messages = new ArrayList<PartialMessage>(parts.size());
 		int partNumber = 0;
 		for (final String part : parts) {
 			messages.add(new PartialMessage(part, aType, hash, partNumber, parts.size()));
 			partNumber++;
 		}
 		return messages;
 	}
 
 	/**
 	 * @return The type of this message
 	 */
 	public String getType()
 	{
 		return aType;
 	}
 
 	/**
 	 * Informs this EverMessage about the Client that sent it
 	 * 
 	 * @param client
 	 *            The client that sent this EverMessage
 	 * @return This (for chainability)
 	 */
 	EverMessage setClient(final Client client)
 	{
 		aClient = client;
 		return this;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "EverMessage(Type = " + getType() + "; Hash: " + aJson.getHash() + ")";
 	}
 }
