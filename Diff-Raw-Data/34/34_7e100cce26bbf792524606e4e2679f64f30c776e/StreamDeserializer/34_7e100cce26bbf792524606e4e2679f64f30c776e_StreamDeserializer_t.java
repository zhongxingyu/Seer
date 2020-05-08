 package com.ksoft.serialization;
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import android.os.Parcelable.Creator;
 
 public class StreamDeserializer extends Deserializer {
     private final DataInputStream _in;
     
     public StreamDeserializer(DataInputStream in) {
         _in = in;
     }
     
     public StreamDeserializer(FileInputStream in) {
         _in = new DataInputStream(new BufferedInputStream(in));
     }
     
    public final void close() throws IOException {
        _in.close();
    }
    
     @Override
     public final boolean readBoolean() throws IOException {
         return _in.readBoolean();
     }
     
     @Override
     public final byte readByte() throws IOException {
         return _in.readByte();
     }
     
     @Override
     public final char readChar() throws IOException {
         return _in.readChar();
     }
     
     @Override
     public final short readShort() throws IOException {
         return _in.readShort();
     }
     
     @Override
     public final float readFloat() throws IOException {
         return _in.readFloat();
     }
     
     @Override
     public final int readInt() throws IOException {
         return _in.readInt();
     }
     
     @Override
     public final long readLong() throws IOException {
         return _in.readLong();
     }
     
     @Override
     public final double readDouble() throws IOException {
         return _in.readDouble();
     }
     
     @Override
     public final String readString() throws IOException {
        byte[] inner = readByteArray();
        return inner == null ? null : new String(inner);
     }
     
     @Override
     public final <T> T readObject(Creator<T> creator) {
         throw new RuntimeException(
                 "Reading Parcelable objects from arbitrary output streams is not supported!");
     }
     
     // The following were taken from the Android SDK's implementations in Parcel for the
     // same functions.
     
     @Override
     public final boolean[] readBooleanArray() throws IOException {
         int N = readInt();
         if (N >= 0) {
             boolean[] val = new boolean[N];
             for (int i = 0; i < N; i++) {
                 val[i] = readInt() != 0;
             }
             return val;
         } else {
             return null;
         }
     }
     
     @Override
     public final byte[] readByteArray() throws IOException {
         int N = readInt();
         if (N >= 0) {
             byte[] val = new byte[N];
             _in.read(val);
             return val;
         } else {
             return null;
         }
     }
     @Override
     public final char[] readCharArray() throws IOException {
         int N = readInt();
         if (N >= 0) {
             char[] val = new char[N];
             for (int i = 0; i < N; i++) {
                 val[i] = (char) readInt();
             }
             return val;
         } else {
             return null;
         }
     }
     
     @Override
     public final double[] readDoubleArray() throws IOException {
         int N = readInt();
         if (N >= 0) {
             double[] val = new double[N];
             for (int i = 0; i < N; i++) {
                 val[i] = readDouble();
             }
             return val;
         } else {
             return null;
         }
     }
     
     @Override
     public final float[] readFloatArray() throws IOException {
         int N = readInt();
         if (N >= 0) {
             float[] val = new float[N];
             for (int i = 0; i < N; i++) {
                 val[i] = readFloat();
             }
             return val;
         } else {
             return null;
         }
     }
     
     @Override
     public final int[] readIntArray() throws IOException {
         int N = readInt();
         if (N >= 0) {
             int[] val = new int[N];
             for (int i = 0; i < N; i++) {
                 val[i] = readInt();
             }
             return val;
         } else {
             return null;
         }
     }
     
     @Override
     public final long[] readLongArray() throws IOException {
         int N = readInt();
         if (N >= 0) {
             long[] val = new long[N];
             for (int i = 0; i < N; i++) {
                 val[i] = readLong();
             }
             return val;
         } else {
             return null;
         }
     }
     
     @Override
     public final String[] readStringArray() throws IOException {
         int N = readInt();
         if (N >= 0) {
             String[] val = new String[N];
             for (int i = 0; i < N; i++) {
                 val[i] = readString();
             }
             return val;
         } else {
             return null;
         }
     }
     
     @Override
     public final <T> T[] readObjectArray(Creator<T> c) {
         throw new RuntimeException(
                 "Reading Parcelable objects from arbitrary output streams is not supported!");
     }
 }
