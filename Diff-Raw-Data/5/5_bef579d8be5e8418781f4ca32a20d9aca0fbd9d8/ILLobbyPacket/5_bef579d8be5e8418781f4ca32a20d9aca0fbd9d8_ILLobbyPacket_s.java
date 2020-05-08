 package jig.ironLegends.oxide.packets;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import jig.ironLegends.oxide.client.ClientInfo;
 import jig.ironLegends.oxide.util.PacketBuffer;
 
 /**
  * @author Travis Hall
  */
 public class ILLobbyPacket extends ILPacket {
 
 	public String map;
 	public String serverName;
 	public byte numClients;
 	public Collection<ClientInfo> clients;
 
 	/**
 	 * @param headerData
 	 * @param protocolData
 	 * @throws IOException 
 	 */
 	protected ILLobbyPacket(byte[] protocolData, byte numClients, String name, String map, Collection<ClientInfo> clients) 
 			throws IOException 
 	{
 		super(ILPacket.IL_LOBBY_DATA_HEADER, protocolData);
 		this.serverName = name;
 		this.map = map;
 		this.numClients = numClients;
 		this.clients = clients;
 		this.contentData = new PacketBuffer(createContentBytes(name, map, numClients, clients));
 	}
 	
 	/**
 	 * @param protocolData
 	 * @param contentData
 	 */
 	public ILLobbyPacket(byte[] protocolData, byte[] contentData) {
 		super(ILPacket.IL_LOBBY_DATA_HEADER, protocolData);
 		this.contentData = new PacketBuffer(contentData);
 		
 		this.serverName = this.contentData.getString();
 		this.map = this.contentData.getString();
 		this.numClients = this.contentData.getByte();
 		// Reconstruct client list
 		this.clients = new ArrayList<ClientInfo>();
 		
 		for (int i = 0; i < this.numClients; i++) {
 			byte id = this.contentData.getByte();
 			String ip = this.contentData.getString();
 			String name = this.contentData.getString();
 			byte team = this.contentData.getByte();
 			this.clients.add(new ClientInfo(id, ip, name, team));
 		}
 		
 		this.contentData.rewind();
 	}
 
 	public static byte[] createContentBytes(String serverName, String map, byte numClients, Collection<ClientInfo> clients) 
 			throws IOException 
 	{
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(baos);
 		
 		dos.writeBytes(serverName);
 		dos.writeBytes(map);
 		dos.writeByte(numClients);
 		
 		for (ClientInfo c : clients) {
 			dos.write(c.id);
 			dos.writeBytes((c.name != null) ? c.name : "Nil"); //c.name
 		}
 		
 		dos.flush();
 		dos.close();
 		
 		return baos.toByteArray();
 	}
 
 }
