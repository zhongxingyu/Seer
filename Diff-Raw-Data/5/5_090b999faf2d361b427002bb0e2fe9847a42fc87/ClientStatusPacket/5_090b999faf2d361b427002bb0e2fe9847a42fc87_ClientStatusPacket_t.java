 package com.thebuerkle.mcboom.packet;
 
 import com.google.common.base.Objects;
 import com.thebuerkle.mcboom.Buffers;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 
 public class ClientStatusPacket extends Packet {
 
     public static final int ID = CLIENT_STATUS;
 
     private final int _status;
 
    public static ClientStatusPacket spawn() {
         return new ClientStatusPacket(0);
     }
 
    public static ClientStatusPacket respawn() {
         return new ClientStatusPacket(1);
     }
 
     private ClientStatusPacket(int status) {
         super(ID);
 
         _status = status;
     }
 
     @Override()
     public int size() {
         return 1;
     }
 
     @Override()
     public void write(ChannelBuffer out) {
         Buffers.mc_byte(out, _status);
     }
 
     @Override()
     public String toString() {
         return Objects.toStringHelper(this)
             .add("ID", id)
             .add("payload", _status)
             .toString();
     }
 }
