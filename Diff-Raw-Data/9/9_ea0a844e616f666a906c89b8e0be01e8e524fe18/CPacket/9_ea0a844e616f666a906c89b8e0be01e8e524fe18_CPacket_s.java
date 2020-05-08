
 package ngmud.network;
 import java.io.*;
 import java.util.*;
 import ngmud.CLog;
 import ngmud.util.CHelper;
 import ngmud.network.packets.*;
 
 public class CPacket {
 	protected static final short HEADER_SIZE=6;
 	protected short Type;
 	protected short SubType;
 	protected short Size;
 	protected SubPacket Data;
 	protected boolean HeaderOK;
 	protected boolean DataOK;
 	protected CSocket Sock;
 	
 	public void Clean()
 	{
 		Data=null;
 		Type=SubType=Size=(short)-1;
 		HeaderOK=DataOK=false;
 	}
 	
 	public boolean Send(CSocket Sock)
 	{
 		if(Sock==null || !DataOK || !HeaderOK)
 		{	return false;	}
 		
 		try {
 			Sock.out().writeShort(Type);
 			Sock.out().writeShort(SubType);
 			Sock.out().writeShort(Data.GetSize());
 		}
 		catch(IOException e)
 		{
 			return false;
 		}
 		return Data.Send(Sock);
 	}
 	
 	public boolean Send()
 	{
 		return Send(Sock);
 	}
 	
 	public RECIEVED Recv(CSocket Sock)
 	{
 		if(Sock==null)
 		{	return RECIEVED.ERROR;	}
 		
 		if(DataOK && HeaderOK)
 		{	Clean();	}
 		RECIEVED Ret=RECIEVED.NOTHING;
 		if(!HeaderOK)
 		{
 			if(Sock.DataAvailable()>=HEADER_SIZE)
 			{
 				try
 				{
 					Type=Sock.in().readShort();
 					SubType=Sock.in().readShort();
 					Size=Sock.in().readShort();
 				}
 				catch(IOException e)
 				{	return RECIEVED.ERROR;	}
 				HeaderOK=true;
 				Data=GetNewPacket(Type);
 				Ret=RECIEVED.HEADER;
 			}
 		}
 		if(HeaderOK && !DataOK)
 		{
 			if(Sock.DataAvailable()>=Size)
 			{
 				if(!Data.Recv(Sock,Size))
 				{	return RECIEVED.ERROR;	}
 				DataOK=true;
 				Ret=RECIEVED.ALL;
 			}
 		}
 		return Ret;
 	}
 	
 	public RECIEVED Recv()
 	{
 		return Recv(Sock);
 	}
 	
 	public CPacket(CSocket Sock)
 	{
 		Clean();
 		this.Sock=Sock;
 	}
 	
 	public CPacket()
 	{
 		Clean();
 		Sock=null;
 	}
 	
 	public void SetSock(CSocket Sock)
 	{
 		this.Sock=Sock;
 	}
 	
 	public CSocket GetSock()
 	{
 		return Sock;
 	}
 	
 	public short GetType()
 	{
 		if(HeaderOK && Sock!=null)
 		{
 			return Type;
 		}
 		return -1;
 	}
 	
 	public short GetSubType()
 	{
 		if(HeaderOK && Sock!=null)
 		{
 			return SubType;
 		}
 		return -1;
 	}
 	
 	public short GetDateSize()
 	{
 		if(HeaderOK && Sock!=null)
 		{
 			return Size;
 		}
 		return -1;
 	}
 	
 	public SubPacket GetData()
 	{
 		if(DataOK && Sock!=null)
 		{
 			return Data;
 		}
 		return null;
 	}
 	
 	public void SetType(short Type)
 	{
 		this.Type=Type;
 		HeaderOK=HeaderComplete();
 	}
 	
 	public void SetSubType(short SubType)
 	{
 		this.SubType=SubType;
 		HeaderOK=HeaderComplete();
 	}
 	
 	protected boolean HeaderComplete()
 	{
 		return Type!=-1 && SubType!=-1;
 	}
 	
 	public void SetData(SubPacket Data)
 	{
 		this.Data=Data;
 		DataOK=true;
 	}
 	
 	public void MakePacket(short Type,short SubType,SubPacket Data)
 	{
 		this.Type=Type;
 		this.SubType=SubType;
 		this.Data=Data;
 	}
 	
 	public enum RECIEVED {
 		ERROR,NOTHING,HEADER,ALL
 	}
 	
 	protected static TreeMap<Short,Class<?> > PacketTypes=null;
 	
 	public static boolean InitPackets(LinkedList<String> Classes,LinkedList<String> Values)
 	{
 		PacketTypes=new TreeMap<Short,Class<?> >();
 		Iterator<String> CIt=Classes.iterator();
 		Iterator<String> VIt=Values.iterator();
 		while(CIt.hasNext())
 		{
 			String ClassName=CIt.next();
 			try {
 				if(Integer.parseInt(VIt.next())==1)
 				{
 					Class<?> C=CHelper.FindClass("ngmud.network.packets.",ClassName,true);
 					if(C!=null)
 					{
 						SubPacket Pack=null;
 						try {
 							Pack=(SubPacket)C.newInstance();
 						}
 						catch(Exception e)
 						{
 							CLog.Error("Can't create instance from "+C.getName()+
 							           " for unknown reason.");
 							continue;
 						}
 						if(Pack==null)
 						{
 							CLog.Error("Instance from "+C.getName()+
 							           " wasn't created for unknown reason.");
 							continue;
 						}
 						short PackNum=0;
 						try {
 							PackNum=C.getField("PACK_NUM").getShort(Pack);
 						}
 						catch(IllegalAccessException e)
 						{
 							CLog.Error("Can't access PACK_NUM in "+C.getName());
 							continue;
 						}
 						catch(NoSuchFieldException e)
 						{
 							CLog.Error("Class "+C.getName()+" hasn't a field called PACK_NUM.");
 							continue;
 						}
 						if(PackNum!=0)
 						{
 							PacketTypes.put(PackNum, C);
 						}
 					}
 				}
 			}
 			catch(NumberFormatException e) {}
 		}
 		
 		
 		return true;
 	}
 	
 	public static SubPacket GetNewPacket(int Type)
 	{
 		if(PacketTypes==null)
 		{	return null;	}
 		
 		Class<?> C=PacketTypes.get(Type);
 		if(C!=null)
 		{
 			try {
 				return (SubPacket)(C.newInstance());
 			}
 			catch(Exception e)
 			{
 				CLog.Error("Can't create instance from class "+C.getName()+".");
 				return null;
 			}
 		}
 		return null;
 	}
 }
