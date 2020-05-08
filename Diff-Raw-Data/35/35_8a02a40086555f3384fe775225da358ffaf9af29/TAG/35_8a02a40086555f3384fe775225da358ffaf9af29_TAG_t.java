 package org.mycelium.mycelium.io.nbt;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 public abstract class TAG {
 	
 	public String	name;
 	
 	public TAG(String name) {
 		this.name = name;
 	}
 	
 	public abstract void Read(DataInputStream stream) throws IOException;
 	
 	public abstract void Write(DataOutputStream stream) throws IOException;
 	
 	public abstract Object getData();
 	
 	public abstract byte getID();
 
 	public static TAG createTag(byte type, String name) {
 		switch(type) {
 			case 0x00: return new TAG_END(name); 
 			case 0x01: return new TAG_BYTE(name); 
 			case 0x02: return new TAG_SHORT(name); 
 			case 0x03: return new TAG_INT(name); 
 			case 0x04: return new TAG_LONG(name); 
 			case 0x05: return new TAG_FLOAT(name); 
 			case 0x06: return new TAG_DOUBLE(name); 
 			case 0x07: return new TAG_BYTE_ARRAY(name); 
 			case 0x08: return new TAG_STRING(name); 
 			case 0x09: return new TAG_LIST(name); 
			case 0x0A: return new TAG_COMPOUND(name); 
 			default: return new TAG_END();
 		}
 	}
 	
 }
