 package com.gigaspaces.hdfs;
 
 import java.util.logging.Logger;
 
 import org.openspaces.archive.ArchiveOperationHandler;
 import org.springframework.beans.factory.annotation.Required;
 
 /**
  * The archive handler for HDFS.  Supports strategies for serialization and
  * file naming.  Supports batching, which is critical to HDFS performance
  * (batch sizes should be large).
  * 
  * @author DeWayne
  *
  */
 public class HdfsArchiveHandler implements ArchiveOperationHandler{
	private static final Logger log=Logger.getLogger(HdfsArchiveHandler.class.getName());
 	private FileSystem fs;
 	private FileOutputStream fsdos;
 	private HdfsSerializer serializer;
 	private PathStrategy strategy;
 
 	public HdfsArchiveHandler(HdfsSerializer serializer){
 		this.serializer=serializer;
 	}
 	
 	/**
 	 * archive a batch.  batches are written to the same file, regardless of the strategy implementation.
 	 */
 	@Override
 	public void archive(Object... objs) {
 		fsdos=strategy.openPath(fs,fsdos,objs);
 		fsdos.write(serializer.serialize(objs));
 		fsdos.flush();
 	}
 	
 	public boolean supportsBatchArchiving(){
 		return true;
 	}
 	
 	@Required
 	public FileSystem getFs() {
 		return fs;
 	}
 	
 	public void setFs(FileSystem fs) {
 		this.fs = fs;
 	}
 	
 	@Required
 	public PathStrategy getStrategy() {
 		return strategy;
 	}
 
 	public void setStrategy(PathStrategy strategy) {
 		this.strategy = strategy;
 	}
 	
 	public static void main(String[] args){
 	}
 
 }
