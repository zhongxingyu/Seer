 package com.nearinfinity.hbaseclient;
 
 import com.google.gson.Gson;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.io.Writable;
 
 import java.io.*;
 
 public class ColumnMetadata implements Writable {
     private ColumnType type;
     private int scale;
     private int precision;
     private boolean isNullable;
     private boolean isPrimaryKey;
     private int maxLength;
 
     public ColumnMetadata() {
         this.type = ColumnType.NONE;
         this.scale = 0;
         this.precision = 0;
        this.maxLength = 0;
         this.isNullable = false;
         this.isPrimaryKey = false;
     }
 
     public ColumnMetadata(byte[] jsonBytes) {
         ColumnMetadata meta = new Gson().fromJson(new String(jsonBytes), ColumnMetadata.class);
 
         this.copy(meta);
     }
 
     public ColumnType getType() {
         return type;
     }
 
     public void setType(ColumnType type) {
         this.type = type;
     }
 
     public int getScale() {
         return scale;
     }
 
     public void setScale(int scale) {
         this.scale = scale;
     }
 
     public int getPrecision() {
         return precision;
     }
 
     public void setPrecision(int precision) {
         this.precision = precision;
     }
 
     public boolean isNullable() {
         return isNullable;
     }
 
     public void setNullable(boolean nullable) {
         isNullable = nullable;
     }
 
     public boolean isPrimaryKey() {
         return isPrimaryKey;
     }
 
     public void setPrimaryKey(boolean primaryKey) {
         isPrimaryKey = primaryKey;
     }
 
     public int getMaxLength() {
         return this.maxLength;
     }
 
     public void setMaxLength(int maxLength) {
         this.maxLength = maxLength;
     }
 
     public byte[] getValue() {
         return this.type.getValue();
     }
 
     public byte[] toJson() throws IOException {
         return new Gson().toJson(this).getBytes();
     }
 
     @Override
     public void write(DataOutput dataOutput) throws IOException {
         dataOutput.write(new Gson().toJson(this).getBytes());
     }
 
     @Override
     public void readFields(DataInput dataInput) throws IOException {
         this.copy(new Gson().fromJson(new String(Bytes.readByteArray(dataInput)), ColumnMetadata.class));
     }
 
     private void copy(ColumnMetadata other) {
        this.type = other.getType();
        this.scale = other.getScale();
        this.precision = other.getPrecision();
        this.maxLength = other.getMaxLength();
         this.isNullable = other.isNullable();
         this.isPrimaryKey = other.isPrimaryKey();
     }
 }
