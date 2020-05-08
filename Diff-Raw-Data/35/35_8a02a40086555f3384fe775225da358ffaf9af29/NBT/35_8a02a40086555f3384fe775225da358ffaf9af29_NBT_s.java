 package org.mycelium.mycelium.io.nbt;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.zip.DataFormatException;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 public class NBT {
 	
 	File	file;
 	boolean	gzip;
 	
 	public NBT() {
 	}
 	
 	public NBT(String path) throws IOException {
 		file = new File(path);
 		gzip = true;
 	}
 	
 	public NBT(String path, boolean gzip) throws IOException {
 		file = new File(path);
 		this.gzip = gzip;
 	}
 	
 	public void Close() throws IOException {
 		file = null;
 		
 	}
 	
 	public TAG_COMPOUND GetTags() throws IOException, DataFormatException {
 		DataInputStream istream = gzip ? new DataInputStream(new GZIPInputStream(new FileInputStream(file))) : new DataInputStream(new FileInputStream(file));
		if (istream.readByte() == 0x10) {
 			TAG_COMPOUND tags = new TAG_COMPOUND(istream.readUTF());
 			tags.Read(istream);
 			istream.close();
 			return tags;
 		} else {
 			istream.close();
 			throw new DataFormatException("Must start with a TAG_COMPOUND");
 		}
 		
 	}
 	
 	public void WriteTags(TAG_COMPOUND tags) throws IOException {
 		DataOutputStream ostream = gzip ? new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file))) : new DataOutputStream(new FileOutputStream(file));
 		ostream.writeByte(0x10);
 		tags.Write(ostream);
 		ostream.close();
 	}
 }
