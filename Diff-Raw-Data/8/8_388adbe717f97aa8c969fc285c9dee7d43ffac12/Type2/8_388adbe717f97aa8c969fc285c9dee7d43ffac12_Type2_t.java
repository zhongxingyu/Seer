 package truelauncher.client.auth;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import org.apache.commons.codec.Charsets;
 
 public class Type2 {
 
 	protected static void writeAuthPacket(DataOutputStream dos, final int port, final int protocolversion, final String nick, final String token, final String password) throws IOException
 	{
 		//
		//fake 1.7.2 handshake packet changes format.
		//host = authpacket(AuthConnector + nick + token + password)
 		//
 		//prepare variables to know the length (packet id + protocolversion + authpacket + port + status)
 		String authpacket = "AuthConnector|"+nick+"|"+token+"|"+password;
		int length = getVarIntSize(0x00)+getVarIntSize(protocolversion)+getStringSize(authpacket)+getVarIntSize(port)+getVarIntSize(2);
 		//write handshake packet
 		//write packet length
 		writeVarInt(dos, length);
 		//write packet id
 		writeVarInt(dos, 0x00);
 		//write protocolVersion
 		writeVarInt(dos, protocolversion);
 		//write authpacket instead of hostname
 		writeString(dos, authpacket);
 		//write port
 		writeVarInt(dos, port);
 		//write state
 		writeVarInt(dos, 2);
 	}
 	
 	
     private static void writeString(DataOutputStream dos, String string) throws IOException
     {
         writeVarInt(dos, string.getBytes(Charsets.UTF_8).length);
         dos.writeChars(string);
     }
 	private static int getStringSize(String string)
 	{
 		byte[] bytes = string.getBytes(Charsets.UTF_8);
 		return getVarIntSize(bytes.length)+bytes.length;
 	}
 	private static void writeVarInt(DataOutputStream dos, int varint) throws IOException
 	{
         int part;
         while ( true )
         {
             part = varint & 0x7F;
             varint >>>= 7;
             if (varint!= 0)
             {
                 part |= 0x80;
             }
             dos.writeByte(part);
             if (varint == 0)
             {
                 break;
             }
         }
 	}
     private static int getVarIntSize(int varint)
     {
         if ((varint&0xFFFFFF80) == 0)
         {
             return 1;
         }
         if ((varint & 0xFFFFC000) == 0)
         {
             return 2;
         }
         if ((varint & 0xFFE00000) == 0 )
         {
             return 3;
         }
         if ((varint & 0xF0000000) == 0 )
         {
             return 4;
         }
         return 5;
     }
 	
 }
