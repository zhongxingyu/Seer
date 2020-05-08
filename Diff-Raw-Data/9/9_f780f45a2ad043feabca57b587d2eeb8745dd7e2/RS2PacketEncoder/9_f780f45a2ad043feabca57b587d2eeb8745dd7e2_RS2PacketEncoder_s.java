 package net.runecrypt.network;
 
 import net.burtleburtle.bob.rand.IsaacRandom;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Thomas Le Godais
  * Date: 3/7/13
  * Time: 11:30 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RS2PacketEncoder extends OneToOneEncoder {
 
     private final IsaacRandom encodingRandom;
 
     /**
      * Constructs a new {@code RS2PacketEncoder} instance.
      *
      * @param encodingRandom The encoding random instance.
      */
     public RS2PacketEncoder(IsaacRandom encodingRandom) {
         this.encodingRandom = encodingRandom;
     }
 
     @Override
     protected Object encode(ChannelHandlerContext ctx, Channel channel, Object message) throws Exception {
         if (message instanceof Packet) {
             Packet buf = (Packet) message;
             if (buf.isHeaderless()) {
                 return buf.getPayload();
             } else {
                 int opcode = buf.getOpcode();
                 Packet.PacketType type = buf.getType();
                 int length = buf.getLength();
                 int finalLength = length + 2 + type.getValue();
                 ChannelBuffer buffer = ChannelBuffers.buffer(finalLength);
                if (opcode < 128){
                    buffer.writeByte((opcode + encodingRandom.nextInt()) & 0xFF);
                } else {
                    int low = (opcode & 0xFF);
                    int high = (opcode >> 8) & 0xFF;
                    buffer.writeByte((high + 128) + encodingRandom.nextInt());
                    buffer.writeByte((low + encodingRandom.nextInt()) & 0xFF);
                }
                 switch (type) {
                     case BYTE:
                         buffer.writeByte(length);
                         break;
                     case SHORT:
                         buffer.writeShort(length);
                         break;
                 }
                 buffer.writeBytes(buf.getPayload());
                 return buffer;
             }
 
         } else
             return message;
     }
 }
