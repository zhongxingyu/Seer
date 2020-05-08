 package org.mycelium.mycelium;
 
 import java.io.IOException;
 import java.util.zip.DataFormatException;
 
 import org.mycelium.mycelium.io.nbt.NBT;
 import org.mycelium.mycelium.io.nbt.TAG;
 import org.mycelium.mycelium.io.nbt.TAG_COMPOUND;
 
 public class NBTtest {
 	
 	public static void main(String[] args) {
 		try {
			NBT nbt = new NBT("test.nbt", true);
 			TAG_COMPOUND tags = nbt.GetTags();
 			nbt.Close();
 			for(TAG tag : tags.List) {
 				switch(tag.getID()) {
 					case 0x00: System.out.print("TAG_END"); 
 					case 0x01: System.out.print("TAG_BYTE");
 					case 0x02: System.out.print("TAG_SHORT"); 
 					case 0x03: System.out.print("TAG_INT");
 					case 0x04: System.out.print("TAG_LONG");
 					case 0x05: System.out.print("TAG_FLOAT"); 
 					case 0x06: System.out.print("TAG_DOUBLE");
 					case 0x07: System.out.print("TAG_BYTE_ARRAY"); 
 					case 0x08: System.out.print("TAG_STRING"); 
 					case 0x09: System.out.print("TAG_LIST");
 					case 0x10: System.out.print("TAG_COMPOUND"); 
 					default: System.out.print("TAG_???");
 				}
 				System.out.println(" Data: " + tag.getData());
 			}
 			
 		} catch (IOException | DataFormatException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 }
