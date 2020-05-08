 package minebot;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 
 import minebot.entities.EntityManager;
 import minebot.entities.ItemID;
 
 public final class World {
 	
 	public final class Chunk {
 		public int cx, cz;
 		
 		public byte[] blocks;
 		// extra data is expanded from nibbles to bytes for simplicity
 		public byte[] metadata;
 		public byte[] lightdata;
 		public byte[] skylightdata;
 		
 		public Chunk(int cx, int cz) {
 			this.cx = cx;
 			this.cz = cz;
 			blocks = new byte[CHUNK_SIZE];
 			metadata = new byte[CHUNK_SIZE];
 			lightdata = new byte[CHUNK_SIZE];
 			skylightdata = new byte[CHUNK_SIZE];
 		}
 		
 		public int getBlock(int x, int y, int z) {
 			return blocks[GetIndex(x, y, z)]; 
 		}
 		
 		public void setBlock(int x, int y, int z, int type) {
 			blocks[GetIndex(x, y, z)] = (byte)type;
 		}
 		
 		public int getData(int x, int y, int z) {
 			return metadata[GetIndex(x, y, z)];
 		}
 		
 		public void setData(int x, int y, int z, int data) {
 			metadata[GetIndex(x, y, z)] = (byte)(data&15);
 		}
 		
 		public int getLight(int x, int y, int z) {
 			return lightdata[GetIndex(x, y, z)];
 		}
 		
 		public void setLight(int x, int y, int z, int value) {
 			lightdata[GetIndex(x, y, z)] = (byte)(value&15);
 		}
 		
 		public int getSkyLight(int x, int y, int z) {
			return skylightdata[GetIndex(x, y, z)];
 		}
 		
 		public void setSkyLight(int x, int y, int z, int value) {
			skylightdata[GetIndex(x, y, z)] = (byte)(value&15);
 		}
 	}
 	
 	private static int GetKey(int cx, int cz) {
 		return cx + (cz << 16);
 	}
 	
 	private static int GetIndex(int x, int y, int z) {
 		return ((x&15)<<11) + ((z&15)<<7) + (y&127);
 	}
 	
 	public static int CHUNK_SIZE = 16*16*128;
 	
 	public Map<Integer, Chunk> chunks;
 	public EntityManager entities;
 	
 	public World() {
 		chunks = new HashMap<Integer, Chunk>();
 		entities = new EntityManager();
 	}
 	
 	// helper functions for readChunkData
 	private static void Inflate(Inflater inf, byte[] dest) throws DataFormatException {
 		int complete = 0;
 		do {
 			complete = inf.inflate(dest, complete, dest.length-complete);
 		} while (complete < dest.length);
 	}
 	
 	private static int GetNibble(byte[] data, int i) {
 		if ((i&1) == 0) {
 			return data[i>>1] & 15;
 		}
 		return data[i>>1] >> 4;
 	}
 	// end helper functions
 	
 	public void readChunkData(int x, int y, int z, int sx, int sy, int sz, byte[] chunkdata) throws IOException, DataFormatException {
 		
 		boolean isEntireChunk = (sx == 16 && sy == 128 && sz == 16);
 		int size = sx*sy*sz;
 		int cx = x >> 4;
 		int cz = z >> 4;
 		
 		Chunk chunk = getChunk(cx, cz);
 		
 		Inflater inf = new Inflater();
 		inf.setInput(chunkdata);
 		
 		byte[] blocks;
 		byte[] metadata = new byte[size/2];
 		byte[] lightdata = new byte[size/2];
 		byte[] skylightdata = new byte[size/2];
 		
 		if (isEntireChunk) {
 			if (chunk == null) {
 				chunk = createEmptyChunk(cx, cz);
 			}
 			blocks = chunk.blocks;
 		} else {
 			blocks = new byte[size];
 		}
 		
 		Inflate(inf, blocks);
 		Inflate(inf, metadata);
 		Inflate(inf, lightdata);
 		Inflate(inf, skylightdata);
 		
 		for (int bx = 0; bx < sx; bx++) {
 			for (int by = 0; by < sy; by++) {
 				for(int bz = 0; bz < sz; bz++) {
 					int i = bx*sy*sz + bz*sy + by;
 					if (!isEntireChunk) {
 						chunk.setBlock(x+bx, y+by, z+bz, blocks[i]);
 					}
 					chunk.setData(x+bx, y+by, z+bz, GetNibble(metadata, i));
 					chunk.setLight(x+bx, y+by, z+bz, GetNibble(lightdata, i));
 					chunk.setSkyLight(x+bx, y+by, z+bz, GetNibble(skylightdata, i));
 				}
 			}
 		}
 	}
 	
 	public void multiBlockChange(int cx, int cz, int len, byte[] coords, byte[] types, byte[] metadata) {
 		Chunk chunk = getChunk(cx, cz);
 		for (int i = 0; i < len; i++) {
 			int x = coords[i*2] >> 4;
 			int y = coords[i*2+1];
 			int z = coords[i*2]&15;
 			chunk.setBlock(x, y, z, types[i]);
 			chunk.setData(x, y, z, metadata[i]);
 		}
 	}
 	
 	public int getBlock(int x, int y, int z) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) return c.getBlock(x, y, z);
 		return 0;
 	}
 	
 	public void setBlock(int x, int y, int z, int type) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) c.setBlock(x, y, z, type);
 	}
 	
 	public int getData(int x, int y, int z) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) return c.getData(x, y, z);
 		return 0;
 	}
 	
 	public void setData(int x, int y, int z, int data) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) c.setData(x, y, z, data);
 	}
 	
 	public int getLight(int x, int y, int z) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) return c.getLight(x, y, z);
 		return 0;
 	}
 	
 	public void setLight(int x, int y, int z, int value) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) c.setLight(x, y, z, value);
 	}
 	
 	public int getSkyLight(int x, int y, int z) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) return c.getSkyLight(x, y, z);
 		return 0;
 	}
 	
 	public void setSkyLight(int x, int y, int z, int value) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) c.setSkyLight(x, y, z, value);
 	}
 	
 	public Chunk getChunk(int cx, int cz) {
 		return chunks.get(GetKey(cx, cz));
 	}
 	
 	public Chunk createEmptyChunk(int cx, int cz) {
 		Chunk c = new Chunk(cx, cz);
 		chunks.put(GetKey(cx, cz), c);
 		return c;
 	}
 	
 	public void deleteChunk(int cx, int cz) {
 		chunks.remove(GetKey(cx, cz));
 	}
 	
 	public boolean canStand(double x, double y, double z) {
 		return canStand((int)Math.floor(x), (int)y, (int)Math.floor(z));
 	}
 	
 	public boolean canStand(int x, int y, int z) {
 		int b1 = getBlock(x,y-1,z);
 		int b2 = getBlock(x,y,z);
 		int b3 = getBlock(x,y+1,z);
 
 		if (ItemID.solid[b1] && !ItemID.solid[b2] && !ItemID.solid[b3]) {
 			return true;
 		}
 		return false;
 	}
 }
