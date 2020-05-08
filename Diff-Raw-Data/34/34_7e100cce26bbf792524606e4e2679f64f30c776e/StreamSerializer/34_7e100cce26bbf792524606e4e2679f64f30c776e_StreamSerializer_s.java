 package com.ksoft.serialization;
 
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.os.Parcelable;
 
 public final class StreamSerializer extends Serializer {
     private final DataOutputStream _out;
     
     public StreamSerializer(DataOutputStream out) {
         _out = out;
     }
     
     public StreamSerializer(FileOutputStream in) {
         _out = new DataOutputStream(new BufferedOutputStream(in));
     }
     
     @Override
     public final void writeBoolean(boolean val) throws IOException {
         _out.writeBoolean(val);
     }
     
     @Override
     public final void writeByte(byte val) throws IOException {
         _out.writeByte(val);
     }
     
     @Override
     public final void writeChar(char val) throws IOException {
         _out.writeChar(val);
     }
     
     @Override
     public final void writeDouble(double val) throws IOException {
         _out.writeDouble(val);
     }
     
     @Override
     public final void writeFloat(float val) throws IOException {
         _out.writeFloat(val);
     }
     
     @Override
     public final void writeInt(int val) throws IOException {
         _out.writeInt(val);
     }
     
     @Override
     public final void writeLong(long val) throws IOException {
         _out.writeLong(val);
     }
     
     @Override
     public final void writeShort(short val) throws IOException {
         _out.writeShort(val);
     }
     
     @Override
     public final void writeObject(Parcelable obj) {
         throw new RuntimeException(
                 "Writing Parcelable objects to arbitrary output streams is not supported!");
     }
     
     @Override
     public final void writeString(String obj) throws IOException {
        byte[] bs = obj.getBytes();
        writeByteArray(bs, 0, bs.length);
     }
     
     @Override
     public final void writeByteArray(byte[] vals) throws IOException {
         _out.write(vals, 0, vals != null ? vals.length : 0);
     }
     
     @Override
     public final void writeByteArray(byte[] vals, int offset, int count) throws IOException {
         if (vals == null) {
             writeInt(-1);
         } else {
             writeInt(count);
             _out.write(vals, offset, count);
         }
     }
     
     // The following were taken from the Android SDK's implementations in Parcel for the
     // same functions.
     
     @Override
     public final void writeBooleanArray(boolean[] vals) throws IOException {
         if (vals != null) {
             int N = vals.length;
             writeInt(N);
             for (int i = 0; i < N; i++) {
                 writeInt(vals[i] ? 1 : 0);
             }
         } else {
             writeInt(-1);
         }
     }
     
     @Override
     public final void writeCharArray(char[] vals) throws IOException {
         if (vals != null) {
             int N = vals.length;
             writeInt(N);
             for (int i = 0; i < N; i++) {
                 writeInt(vals[i]);
             }
         } else {
             writeInt(-1);
         }
     }
     
     @Override
     public final void writeDoubleArray(double[] vals) throws IOException {
         if (vals != null) {
             int N = vals.length;
             writeInt(N);
             for (int i = 0; i < N; i++) {
                 writeDouble(vals[i]);
             }
         } else {
             writeInt(-1);
         }
     }
     
     @Override
     public final void writeFloatArray(float[] vals) throws IOException {
         if (vals != null) {
             int N = vals.length;
             writeInt(N);
             for (int i = 0; i < N; i++) {
                 writeFloat(vals[i]);
             }
         } else {
             writeInt(-1);
         }
     }
     
     @Override
     public final void writeIntArray(int[] vals) throws IOException {
         if (vals != null) {
             int N = vals.length;
             writeInt(N);
             for (int i = 0; i < N; i++) {
                 writeInt(vals[i]);
             }
         } else {
             writeInt(-1);
         }
     }
     
     @Override
     public final void writeLongArray(long[] vals) throws IOException {
         if (vals != null) {
             int N = vals.length;
             writeInt(N);
             for (int i = 0; i < N; i++) {
                 writeLong(vals[i]);
             }
         } else {
             writeInt(-1);
         }
     }
     
     @Override
     public final void writeStringArray(String[] val) throws IOException {
         if (val != null) {
             int N = val.length;
             writeInt(N);
             for (int i = 0; i < N; i++) {
                 writeString(val[i]);
             }
         } else {
             writeInt(-1);
         }
     }
     
     @Override
     public final <T extends Parcelable> void writeObjectArray(T[] vals) {
         throw new RuntimeException(
                 "Writing Parcelable objects to arbitrary output streams is not supported!");
     }
     
    
 }
