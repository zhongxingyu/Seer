 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.image.FilePattern;
 
 public class FileHeightMap extends HeightMapChunkProvider {
 	
 	private final FilePattern pattern;
 	
 	public FileHeightMap(File directory, FilePattern pattern) {
 		this.pattern = pattern;
 	}
 
 	@Override
 	protected HeightMapChunk getHeightMapChunk(Coordinate chunkLocation) {
 		HeightMapChunk chunk = null;
 		File file = pattern.getFile(chunkLocation);
 		try {
 			InputStream fstream = new FileInputStream(file);
 			InputStream bstream = new BufferedInputStream(fstream);
 			ObjectInput ostream = new ObjectInputStream(bstream);
 			try {
 				chunk = (HeightMapChunk) ostream.readObject();
 			} finally {
 				ostream.close();
 			}
		} catch (FileNotFoundException e) {
			chunk = new HeightMapChunk(chunkLocation);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return chunk;
 	}
 
 	@Override
 	protected void setHeightMapChunk(Coordinate chunkLocation, HeightMapChunk chunk) {
 		File file = pattern.getFile(chunkLocation);
 		try {
 			OutputStream fstream = new FileOutputStream(file);
 			OutputStream bstream = new BufferedOutputStream(fstream);
 			ObjectOutput ostream = new ObjectOutputStream(bstream);
 			try {
 				ostream.writeObject(chunk);
 			} finally {
 				ostream.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
