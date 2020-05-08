 package com.github.jmpjct.mysql.proto;
 
 import java.util.ArrayList;
 import org.apache.log4j.Logger;
 
 public class Auth_Response extends Packet {
     public long capabilityFlags = Flags.CLIENT_PROTOCOL_41;
     public long maxPacketSize = 0;
     public long characterSet = 0;
     public String username = "";
     public String authResponse = "";
     public String schema = "";
     
     public void setCapabilityFlag(long flag) {
         this.capabilityFlags |= flag;
     }
     
     public void removeCapabilityFlag(long flag) {
         this.capabilityFlags &= ~flag;
     }
     
     public void toggleCapabilityFlag(long flag) {
         this.capabilityFlags ^= flag;
     }
     
     public boolean hasCapabilityFlag(long flag) {
         return ((this.capabilityFlags & flag) == flag);
     }
     
     public ArrayList<byte[]> getPayload() {
         ArrayList<byte[]> payload = new ArrayList<byte[]>();
         
         if ((this.capabilityFlags & Flags.CLIENT_PROTOCOL_41) != 0) {
             payload.add( Proto.build_fixed_int(4, this.capabilityFlags));
             payload.add( Proto.build_fixed_int(4, this.maxPacketSize));
             payload.add( Proto.build_fixed_int(1, this.characterSet));
             payload.add( Proto.build_fixed_str(23, ""));
             payload.add( Proto.build_null_str(this.username));
             if (this.hasCapabilityFlag(Flags.CLIENT_SECURE_CONNECTION))
                 payload.add( Proto.build_lenenc_str(this.authResponse));
             else
                 payload.add( Proto.build_null_str(this.authResponse));
             payload.add( Proto.build_fixed_str(this.schema.length(), this.schema));
         }
         else {
             payload.add( Proto.build_fixed_int(2, this.capabilityFlags));
             payload.add( Proto.build_fixed_int(3, this.maxPacketSize));
             payload.add( Proto.build_null_str(this.username));
             payload.add( Proto.build_null_str(this.authResponse));
         }
         
         return payload;
     }
     
     public static Auth_Response loadFromPacket(byte[] packet) {
         Auth_Response obj = new Auth_Response();
         Proto proto = new Proto(packet, 3);
         
         obj.sequenceId = proto.get_fixed_int(1);
         obj.capabilityFlags = proto.get_fixed_int(2);
         proto.offset -= 2;
         
         if (obj.hasCapabilityFlag(Flags.CLIENT_PROTOCOL_41)) {
             obj.capabilityFlags = proto.get_fixed_int(4);
             obj.maxPacketSize = proto.get_fixed_int(4);
             obj.characterSet = proto.get_fixed_int(1);
             proto.get_fixed_str(23);
             obj.username = proto.get_null_str();
             
             if (obj.hasCapabilityFlag(Flags.CLIENT_SECURE_CONNECTION))
                 obj.authResponse = proto.get_lenenc_str();
            else
                obj.authResponse = proto.get_null_str();
             
             obj.schema = proto.get_eop_str();
         }
         else {
             obj.capabilityFlags = proto.get_fixed_int(2);
             obj.maxPacketSize = proto.get_fixed_int(3);
             obj.username = proto.get_null_str();
             obj.schema = proto.get_null_str();
         }
         
         return obj;
     }
 }
