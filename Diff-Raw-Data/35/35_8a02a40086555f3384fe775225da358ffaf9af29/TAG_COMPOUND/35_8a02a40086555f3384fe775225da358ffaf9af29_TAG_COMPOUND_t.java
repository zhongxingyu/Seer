 package org.mycelium.mycelium.io.nbt;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class TAG_COMPOUND extends TAG {
 	
	public ArrayList<TAG>	List = new ArrayList<TAG>();
 	
 	public TAG_COMPOUND() {
 		super("");
 	}
 	
 	public TAG_COMPOUND(String name) {
 		super(name);
 	}
 	
 	public TAG_COMPOUND(String name, ArrayList<TAG> List) {
 		super(name);
 		this.List = List;
 	}
 	
 	@Override
 	public void Read(DataInputStream stream) throws IOException {
 		this.List.clear();
 		byte id;
 		while (true) {
 			id = stream.readByte();
 			TAG tag = TAG.createTag(id, stream.readUTF());
 			tag.Read(stream);
 			this.List.add(tag);
 			if (id == 0) break;
 		}
 	}
 	
 	@Override
 	public void Write(DataOutputStream stream) throws IOException {
 		for (TAG tag : this.List) {
 			stream.writeByte(tag.getID());
 			stream.writeUTF(tag.name);
 			tag.Write(stream);
 		}
 	}
 	
 	@Override
 	public ArrayList<TAG> getData() {
 		return List;
 	}
 	
 	@Override
 	public byte getID() {
		return 0x0A;
 	}
 	
 }
