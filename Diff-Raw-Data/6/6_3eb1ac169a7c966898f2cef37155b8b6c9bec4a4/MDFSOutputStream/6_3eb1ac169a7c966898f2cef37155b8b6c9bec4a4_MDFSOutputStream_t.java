 package org.apache.hadoop.mdfs.io;
 
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.util.Progressable;
 import org.apache.hadoop.fs.permission.FsPermission;
 import org.apache.hadoop.mdfs.protocol.MDFSDataProtocol;
 import org.apache.hadoop.mdfs.protocol.MDFSNameProtocol;
 import org.apache.hadoop.mdfs.protocol.LocatedBlocks;
 import org.apache.hadoop.mdfs.utils.MountFlags;
 import org.apache.hadoop.mdfs.protocol.BlockInfo;
 import org.apache.hadoop.mdfs.protocol.LocatedBlock;
 
 
 
 
 public class MDFSOutputStream extends OutputStream {
 
 	boolean closed = false;
 	private String src;
 	final private long blockSize;
 	private short blockReplication; // replication factor of file
 	private Progressable progress;
 	private MDFSNameProtocol namesystem;
 	private MDFSDataProtocol datasystem;
 	private LocatedBlock lastBlock;
 	private byte buffer[];
 	private int bufCount;
 	private boolean addNewBlock;
 	private BlockWriter writer;
 	private long blockOffset;
 	private Configuration conf;
 
 
 
 
 	public MDFSOutputStream(MDFSNameProtocol namesystem,MDFSDataProtocol datasystem,Configuration conf,String src,int flags,FsPermission permission,boolean createParent, short replication, long blockSize,Progressable progress,int bufferSize) throws IOException{
 		this.src = src;
 		this.blockSize = blockSize;
 		this.blockReplication = replication;
 		this.progress = progress;
 		this.namesystem = namesystem;
 		this.datasystem = datasystem;
 		this.buffer= new byte[bufferSize];
 		this.bufCount=0;
 		this.blockOffset=0;
 		this.writer=null;
 		this.conf=conf;
 		boolean append = MountFlags.O_APPEND.isSet(flags);
 		lastBlock=null;
 		addNewBlock=true;
 
 		BlockInfo[] listOfBlocks=namesystem.addNewFile(src,flags,createParent,permission,replication,blockSize);
 		datasystem.removeBlocks(src,listOfBlocks);
 
 		LocatedBlocks blocks= getBlockLocations(src,0,Long.MAX_VALUE);
 		System.out.println(" Total number of blocks " + blocks.getLocatedBlocks().size());
 		System.out.println(" blockSize "+blockSize+" bufferSize "+bufferSize+ "FileLength "+blocks.getFileLength());
 		if(blocks.getLocatedBlocks().size() != 0){
 			if(!append){
 				throw new IOException(" Blocks are present for Create Operation");
 
 			}
 		}
 		else {
 			if(append){
 				throw new IOException(" No Blocks present for Append Operation");
 
 			}
 		}
 
 		if(append){
 			lastBlock= blocks.get(blocks.locatedBlockCount() - 1);
 			if(lastBlock == null)
 				throw new IOException("lastBlock is Null");
 			if(lastBlock.getBlock().getNumBytes() == blockSize){
 				addNewBlock=true;
 			}
 			else{
 				blockOffset=lastBlock.getBlock().getNumBytes();
 				BlockCopier.makeAvailableForAppend(namesystem,datasystem,src,lastBlock.getBlock().getBlockId());
 				System.out.println("Retrieved block append"+ lastBlock.getBlock().getBlockId()+ " blockOffset "+blockOffset);
 				writer=new BlockWriter(src,lastBlock.getBlock().getBlockId(),true);//TODO last block is not present locally
 				addNewBlock=false;
 			}
 		}
 
 
 	}
 
 	@Override
 	public synchronized void write(int b) throws IOException {
 		if(addNewBlock == true) {
 			if(writer != null){
 				if(lastBlock == null)
 					throw new IOException("lastBlock is Null");
 				long blockId= lastBlock.getBlock().getBlockId();
 				String blockLoc=writer.getBlockLocationInFS(src,blockId);
 				datasystem.notifyBlockAdded(src,blockLoc,blockId,blockOffset);
 				namesystem.notifyBlockAdded(src,blockId,blockOffset);
 
 				blockOffset=0;
 				writer.close();
 			}
 			lastBlock=namesystem.addNewBlock(src);
 			System.out.println(" Last Block Id "+lastBlock.getBlock().getBlockId());
 			writer=new BlockWriter(src,lastBlock.getBlock().getBlockId(),false);
 			addNewBlock=false;
 		}
 		buffer[bufCount++] = (byte)b;
 		blockOffset++;
 		if(blockOffset == blockSize ){
 			flushBuffer();
 			bufCount =0;
 			addNewBlock=true;
 		}
 
 		if(bufCount == buffer.length) {
 			flushBuffer();
 			bufCount =0;
 			//addNewBlock=true;
 		}
 
 	}
 
 	@Override
 	public void write(byte buf[]) throws IOException{
 		System.out.println(" Write called 2");
 		
 		for(byte b:buf)
 			write(b);
 
 	}	
 
 
 	@Override
 	public synchronized void write(byte buf[], int off, int len) throws IOException {
 		System.out.println(" Write called with offset "+off+" length "+len);
 		
 		byte[] array= new byte[len];
 		System.arraycopy(buf,off,array,0,len);
 		for(byte b:array)
 			write(b);
 
 
 	}
 
 
 	@Override
 	public synchronized void flush() throws IOException {
 		System.out.println("  flush ");
 		flushBuffer();
		bufCount=0;
 
 	}
 
 	@Override
 	public synchronized void close() throws IOException {
 		System.out.println("  close ");
		if(bufCount != 0 ){
 			flushBuffer();
			bufCount=0;
 		}
 
 		if(lastBlock != null){
 			long blockId= lastBlock.getBlock().getBlockId();
 			String blockLoc=writer.getBlockLocationInFS(src,blockId);
 			datasystem.notifyBlockAdded(src,blockLoc,blockId,blockOffset);
 			namesystem.notifyBlockAdded(src,blockId,blockOffset);
 
 			writer.close();
 		}
 
 	}
 
 
 	protected void finalize() throws Throwable {
 
 	}
 
 
 	public long getPos() throws IOException {
 		System.out.println(" get pos ");
 		return 0;
 	}
 
 
 	LocatedBlocks getBlockLocations(String src, long start, long length) throws IOException {
 
 		return namesystem.getBlockLocations(src, start, length);
 	}
 
 	private void flushBuffer() throws IOException {
 			if(writer== null){
 				throw new IOException(" Block writer is empty");
 			}
 			else{
 				writer.writeBuffer(buffer,0,bufCount);
 			}
 	}
 
 }
