 package org.mycelium.mycelium.io.nbt;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class TAG_LIST extends TAG {
 	
	public ArrayList<TAG>	List;
 	public byte				Type;
 	public int				Size;
 	
 	public TAG_LIST() {
 		super("");
 	}
 	
 	public TAG_LIST(String name) {
 		super(name);
 	}
 	
 	
 	public TAG_LIST(String name, ArrayList<TAG> List, byte Type, int Size) {
 		super(name);
 		this.List = List;
 		this.Type = Type;
 		this.Size = Size;
 	}
 	
 	@Override
 	public void Read(DataInputStream stream) throws IOException {
 		this.Type = stream.readByte();
 		this.Size = stream.readInt();
 		
 		for (int i = 0; i < this.Size; i++) {
 			TAG tag = TAG.createTag(this.Type, "");
 			tag.Read(stream);
 			this.List.add(tag);
 		}
 	}
 	
 	@Override
 	public void Write(DataOutputStream stream) throws IOException {
 		stream.writeByte(this.Type);
 		stream.writeInt(this.Size);
 		
 		for (int i = 0; i < this.Size; i++) {
 			TAG tag = this.List.get(i);
 			tag.Write(stream);
 		}
 	}
 	
 	@Override
 	public ArrayList<TAG> getData() {
 		return List;
 	}
 	
 	@Override
 	public byte getID() {
 		return 0x09;
 	}
 	
 }
