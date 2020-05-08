 package ru.direvius.cmp;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class TLVChainBuilder {
 
     List<TLV> chain = new ArrayList<TLV>();
 
     public byte[] asByteArray() {
         int length = 0;
         for (TLV tlv : chain) {
             length += tlv.length;
         }
         ByteBuffer bb = ByteBuffer.allocate(length);
         for (TLV tlv : chain) {
             bb.put(tlv.asByteArray());
         }
         return bb.array();
     }
 
     public TLVChainBuilder putByteArray(byte tag, byte[] data) {
         chain.add(new TLV(tag, data));
         return this;
     }
 
     public TLVChainBuilder putByte(byte tag, byte id) {
         byte[] data = {id};
         chain.add(new TLV(tag, data));
         return this;
     }
 
     public TLVChainBuilder putInt(byte tag, int terminalID) {
         chain.add(new TLV(tag, ByteBuffer.allocate(4).putInt(terminalID).array()));
         return this;
     }
 
     public TLVChainBuilder putDate(byte tag, Date dt) {
        chain.add(new TLV(tag, ByteBuffer.allocate(4).putInt((int) dt.getTime() / 1000).array()));
         return this;
     }
 
     public TLVChainBuilder putBCD(byte tag, long cardNumber) {
         chain.add(new TLV(tag, Util.DecToBCDArray(cardNumber)));
         return this;
     }
 }
