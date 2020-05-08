 package jig.ironLegends.oxide.packets;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Vector;
 
 import jig.engine.util.Vector2D;
 import jig.ironLegends.EntityState;
 import jig.ironLegends.oxide.util.PacketBuffer;
 
 /**
  * @author Travis Hall
  */
 public class ILStartGamePacket extends ILPacket {
 
 	public String map;
 	public int m_countPlayers;
 	public boolean m_bSinglePlayer;
 	public boolean m_bGo; // if !m_bGo, then clients should just load map and sent client updates, but keep the "loading" text
 	public Vector<EntityState> m_entityStates = null;
 	
 	class PlayerData
 	{
 		public PlayerData(String name, int entityNumber)
 		{
 			m_name = name;
 			m_entityNumber = entityNumber;
 		}
 		
 		public PlayerData() {
 			// TODO Auto-generated constructor stub
 		}
 
 		String m_name;
 		int m_entityNumber;
 	}
 
 	public void addPlayer(String name, int entityNumber)
 	{
 		m_playerDataCol.add(new PlayerData(name, entityNumber));
 		m_countPlayers = m_playerDataCol.size();
 	}
 	public Vector<PlayerData> m_playerDataCol;
 	
 	/**
 	 * @param protocolData
 	 * @param map
 	 */
 	protected ILStartGamePacket(byte[] protocolData, String map) {
 		super(ILPacket.IL_START_GAME_HEADER, protocolData);
 		this.map = map;
 		this.m_bSinglePlayer = true;
 		this.m_bGo = false;
 		this.m_playerDataCol = new Vector<PlayerData>();
 		m_countPlayers = 0;
 		m_entityStates = new Vector<EntityState>();
 		//this.contentData = null;
 		//new PacketBuffer(map.getBytes());
 	}
 	
 	protected ILStartGamePacket(byte[] protocolData, byte[] contentData) {
 		super(ILPacket.IL_START_GAME_HEADER, protocolData);
 		this.contentData = new PacketBuffer(contentData);
 		m_playerDataCol = new Vector<PlayerData>();
 		m_entityStates = new Vector<EntityState>();
 		
 		// unpack
 		unpack(this.contentData);
 		
 		
 		// end unpack
 		this.contentData.rewind();
 	}
 	
 	public byte[] getBytes() throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(baos);
 
 		pack(dos);
 		
 		dos.flush();
 		dos.close();
 		this.contentData = new PacketBuffer(baos.toByteArray());
 		
 		return super.getBytes();
 	}
 	protected void pack(DataOutputStream dos) throws IOException
 	{
 		int test = map.lastIndexOf('\0');
 		if (map.charAt(test-1) == '\0')
 			map = map.substring(0, test);
 		//if (map.length() == 105)
 		//	map = map.substring(0, 104);
 		dos.writeBytes(map);
 		dos.writeByte(m_bSinglePlayer?1:0);
 		dos.writeByte(m_bGo?1:0);
 		dos.writeByte(this.m_playerDataCol.size());
 
 		for (PlayerData o : this.m_playerDataCol) {
 			dos.writeBytes(o.m_name);
 			dos.write(o.m_entityNumber);
 		}
 		
 		dos.writeByte(m_entityStates.size());
 		for (EntityState e : this.m_entityStates){
 			dos.writeInt(e.m_entityNumber);
 			dos.writeFloat((float) e.m_pos.getX());
 			dos.writeFloat((float) e.m_pos.getY());
 			dos.writeInt(e.m_health);
 			dos.writeFloat((float) e.m_tankRotationRad);
 			dos.writeInt(e.m_flags);
 			dos.writeFloat((float) e.m_turretRotationRad);
 		}
 	}
 	
 	private void unpack(PacketBuffer contentData) {
 		map = contentData.getString();
 		
 		m_bSinglePlayer = contentData.getByte()==1?true:false;
 		m_bGo = contentData.getByte()==1?true:false;
 
 		m_countPlayers = contentData.getByte();
 		
 		for (int i = 0; i < m_countPlayers; ++i)
 		{
 			PlayerData o = new PlayerData();
 			o.m_name = contentData.getString();
 			o.m_entityNumber = contentData.getInt();
 			m_playerDataCol.add(o);
 		}
 		
 		int iCountTanks = contentData.getByte();
 		m_entityStates.clear();
 		
 		for (int i = 0; i < iCountTanks; ++i)
 		{
 			EntityState e = new EntityState();
 			e.m_entityNumber = this.contentData.getInt();
 			e.m_pos = new Vector2D(this.contentData.getFloat(), this.contentData.getFloat());
 			e.m_health = this.contentData.getInt();
 			e.m_tankRotationRad = this.contentData.getFloat();
 			e.m_flags = this.contentData.getInt();
 			e.m_turretRotationRad = this.contentData.getFloat();
 			m_entityStates.add(e);			
 		}
 		
 		// assert (m_countPlayers == m_playerDataCol.size()) 
 	}
 
 	public void setEntityStates(Vector<EntityState> entityStates) {
 		// TODO Auto-generated method stub
 		Iterator<EntityState> iter = entityStates.iterator();
 		
 		m_entityStates.clear();
 		while (iter.hasNext())
 		{
 			m_entityStates.add(iter.next());
 			
 			/*
 			EntityState e = new EntityState();
 			e.m_entityNumber = this.contentData.getInt();
 			e.m_pos = new Vector2D(this.contentData.getFloat(), this.contentData.getFloat());
 			e.m_health = this.contentData.getInt();
 			e.m_tankRotationRad = this.contentData.getFloat();
 			e.m_flags = this.contentData.getInt();
 			e.m_turretRotationRad = this.contentData.getFloat();
 			m_entityStates.add(e);
 			*/
 		}
 	}
 }
