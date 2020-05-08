 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Queue;
 
 /**
  * This class is used to compute the adler64 checksums of a file.
  * 
  */
 public class RollingChecksum {
 	private static long modp=4294967291l;
 	FileInputStream fis=null;
 	long a=0;
 	long b=0;
 	long mask = 0xffffffff;
 	LinkedList<Byte> data;
 	boolean initialized=false;
 	
 	/** 
 	 * Initialize the rolling checksum
 	 * @param filename The filename to use
 	 * @throws FileNotFoundException If we cant find the file...
 	 */
 	public RollingChecksum(String filename) throws FileNotFoundException {
 		fis=new FileInputStream(filename);
 		
 		data=new LinkedList<Byte>();
 		
 		// initialize the rolling checksum
 		update(OpenBox.blocksize);
 		//System.out.println("INITIALIZED WITH "+data.size());
 		initialized=true;
 	}
 	
 	/**
 	 * Returns the adler64 and MD5 checksums of consecutive non-overlapping windows of size OpenBox.blocksize
 	 * @return The checksum of consecutive data blocks
 	 */
 	public FileChecksum[] blocks() {
 		LinkedList<FileChecksum> ll=new LinkedList<FileChecksum>();
 		do {
 			long h[] = hash();
 			byte[] by = new byte[data.size()];
 			int index = 0;
 			for (byte b : data) {
 			    by[index++] = b;
 			}
 			ll.add(new FileChecksum(h[0],h[1],MD5.MD5(by)));
			//System.out.println(""+ h[0]+ " " +h[1] + " " + data.size());
 		} while (update(OpenBox.blocksize)>0);

		//System.exit(1);//TODO REMOVE THIS
 		return ll.toArray(new FileChecksum[0]);
 	}
 	
 	/**
 	 * Return the size of the current window and the hash
 	 * @return (size,adler64 hash)
 	 */
 	public long[] hash() {
 		long h[] =new long[2];
 		h[0]=data.size();
 		h[1]=((b&mask)<<32)+(a & mask );
 		//assert(h[1]==raw_checksum());
 		return h;
 	}
 	
 	/**
 	 * Return the md5 checksum of data in window
 	 * @return The MD5 checksum
 	 */
 	public String hash_md5() {
 		byte[] by = new byte[data.size()];
 		int index = 0;
 		for (byte b : data) {
 		    by[index++] = b;
 		}
 		return MD5.MD5(by);
 	}
 	
 	/**
 	 * Move the window by one byte
 	 * @return True if the window has been moved
 	 */
 	public boolean update() {
 		return update(1)>0  ? true : false;
 	}
 	
 	/**
 	 * Compute the checksum from the data in the window, not using the rolling variables.
 	 * Useful for debugging.
 	 * @return The adler64 checksum of data in the current window
 	 */
 	public long raw_checksum() {
 		long ap=0;
 		long bp=0;
 		for (Byte y : data) {
 			ap=(ap-y)%modp;
 			bp=(bp-OpenBox.blocksize*y)%modp;
 		}
 		return ((bp&mask)<<32)+(ap&mask);
 	}
 	
 	/** 
 	 * Move the rolling window by d bytes
 	 * @param d The number of bytes to move the window by
 	 * @return The number of bytes that the window has been moved by
 	 */
 	public int update(int d) {
 		try {
 			int bytes_read=0;
			byte[] z = new byte[1];
 			for (int i=0; i<d; i++) {
 
 				if (initialized && data.size()>0) {
 					// remove the old byte and update
 					//System.out.println("removing old byte");
 					Byte y = data.poll();
 					a = (a - y) % modp;
 					b = (b - OpenBox.blocksize * y) % modp;
				} else {
					//System.out.println("not popping byte " + data.size());
 				}
 				
				int read_now = (byte) fis.read(z);
				if (read_now!=-1) {
 					bytes_read++;
 					//System.out.println("read new byte");
					data.offer(z[0]);
					a=(a+z[0])%modp;
 					b=(b+a)%modp;
				} else {
					//System.out.println("error reading byte " + data.size());
 				}
 				//means we have data update
 
 			}
			
			//System.out.println("\tBytes read "+bytes_read + " " + d);
			//System.exit(1);
 			return bytes_read;
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return 0;
 	}
 	
 }
