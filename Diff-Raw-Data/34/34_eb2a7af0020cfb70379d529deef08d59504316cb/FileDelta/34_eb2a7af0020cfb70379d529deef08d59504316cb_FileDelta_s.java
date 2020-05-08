 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 /**
  * 
  * The FileDelta class is used to determine how to reconstruct an updated remote file locally.
  * 
  * The FileDelta class is provided with a local filename and remote adler64 checksums upon creation.
  * From this data FileDelta constructs a linked list of Block requests (in the correct order) that describe
  * the contents of the remote file using both local and remote data.
  * 
  * If only a single byte is changed in the remote file then only one block will set to be filled remotely, 
  * the rest will be set to be filled locally.
  * 
  */
 public class FileDelta {
 	/** 
 	 * The filename with respect to the repository root
 	 */
 	String repo_filename;
 	/**
 	 * The filename on the current filesystem
 	 */
 	String local_filename;
 	/**
 	 * The linked list of Block objects, initially containing requests for all necessary data
 	 */
 	LinkedList<Block> ll;
 	
 	/** 
 	 * Creates a linked list of Block objects that are requests to be filled either remotely or locally.
 	 * Once the Block objects are filled with the data, the linear order of the data is the updated file
 	 * described by the checksums (sums)
 	 * @param fs The FileState of the local copy
 	 * @param sums The adler64 block checksums of the remote side
 	 */
 	public FileDelta(FileState fs, FileChecksum sums[]) {
 		this.local_filename=fs.local_filename;
 		this.repo_filename=fs.repo_filename;
 		//lets make a hash table from the sums
 		ll = new LinkedList<Block>();
 		
 		try {
 			//the hard case lets match these up
 			HashMap<Long,LinkedList <FileChecksum> > need_these= new HashMap<Long,LinkedList <FileChecksum> >();
 			for (int i=0; i<sums.length; i++) {
 				if (!need_these.containsKey(sums[i].adler64)) {
 					need_these.put(sums[i].adler64, new LinkedList<FileChecksum>());
 				}
 				need_these.get(sums[i].adler64).add(sums[i]);
 				//System.out.println("Check sum " + sums[i][1]);
 			}
 			
 			HashMap<FileChecksum,Block> have_these =new HashMap<FileChecksum, Block>();
 			long current_offset=0; //the 
 			RollingChecksum rl = new RollingChecksum(local_filename);
 			long h[] = rl.hash();
 			long moved=h[0];
 			while (moved>0) {
 				//System.out.println("X"+h[1]);
 				if (need_these.containsKey(h[1])) {
 					//had a adler64 collision check the md5
 					String md5_hash = rl.hash_md5(); //fairly expensive step, can optimize only compute if actually found a missing md5 from below
 					for (FileChecksum fc : need_these.get(h[1])) {
 						//first see if there is a md5 collision
 						if (md5_hash.equals(fc.md5)) {
 							//pretty sure this is the piece
 							have_these.put(fc,  Block.BlockLocalRequest(repo_filename, current_offset, -1, h[0]));
 							//have_these.put(fc, new Block(repo_filename,current_offset,true,h[1],h[0]));
 						}
 					}
 					//moved=rl.update(OpenBox.blocksize);
 					moved=rl.update(1);
 				} else {
 					moved=rl.update(1);
 				}
 				h = rl.hash();
 				current_offset+=moved;
 			}
 			
 			
 			//should know all the chunks we have, lets make a list
 			current_offset=0;
 			//System.out.println("FD building");
 			for (FileChecksum fc : sums) {
 				Block b;
 				if (have_these.containsKey(fc)) {
 					//lets just use our block
 					b = have_these.get(fc).copy();
 					//System.out.println("LOCAL\t"+fc + "\n\t"+b);
 				} else {
 					//need to request this block
 					b=Block.BlockRemoteRequest(repo_filename, current_offset, -1, fc.size);
 					//System.out.println("REMOTE\t"+fc+ "\n\t"+b);
 					//b = new Block(repo_filename,current_offset,false,other_h[1],other_h[0]);
 				}
 				b.dest_offset = current_offset;
 				ll.add(b);
 				//current_offset+=other_h[0];
 				current_offset+=fc.size;
 			}
 			
 		} catch (FileNotFoundException e) {
 			//cant find the file this means we need to request everything
 			long file_size = 0;
 			//create blocks to request
 			for (FileChecksum fc : sums) {
 				Block b = Block.BlockRemoteRequest(repo_filename, file_size, file_size, fc.size);
 				file_size+=fc.size;
 				ll.add(b);
 			}
 		}
 	}
 	
 	
 	public String toString() {
 		String r = "FD: " + repo_filename+"\n";
 		if (ll!=null) {
 			for (Block b: ll ) {
 				r+=b+"\n";
 			}
 		}
 		return r;	
 	}
 	
 	
 	//TODO should not be calling this for big files, need to call incrementally otherwise files fill up RAM!
 	/**
 	 * Assemble the Blocks into a file.
 	 * A prerequisite is that all initial Block requests have been filled
 	 * @param local_filename_out The filename to write the output to
 	 * @return The number of bytes written to file
 	 */
 	public long assemble_to(String local_filename_out ) {
 		long out_bytes=0;
 		//we have all the bytes we need!
 		try {
 			FileOutputStream fos = new FileOutputStream(local_filename_out);
 
 			for (Block b : ll) {
 				assert(b.data!=null);
 				fos.write(b.data);
 				out_bytes+=b.data.length;
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return out_bytes;
 	}
 	
 }
