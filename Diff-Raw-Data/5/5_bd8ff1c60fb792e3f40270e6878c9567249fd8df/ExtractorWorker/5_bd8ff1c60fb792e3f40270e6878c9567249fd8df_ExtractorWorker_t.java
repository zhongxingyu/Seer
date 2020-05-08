 package ca.dijital.geo.canvec;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.zip.GZIPOutputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Performs the work of an ExtractorJob in a separate thread by invoking 
  * the command-line utility.
  * 
  * @author Rob Skelly <rob@dijital.ca>
  */
 public class ExtractorWorker implements Runnable {
 
 	private static Logger logger = LoggerFactory.getLogger(ExtractorWorker.class);
 	
 	private Thread  thread;
 	private ExtractorJob job;
 	private boolean running;
 	private boolean busy;
 	
 	/**
 	 * Starts the worker with the given job. If the worker is in use, an exception is thrown.
 	 * @param job
 	 */
 	public void start(final ExtractorJob job) throws Exception {
 		if(!running){
 			busy = true;
 			running = true;
 			this.job = job;
 			this.thread = new Thread(this);
 			this.thread.start();
 		}else{
 			throw new Exception("This worker is not free.");
 		}
 	}
 	
 	/**
 	 * Stops the worker.
 	 */
 	public void stop(){
 		if(running){
 			running = false;
 			try {
 				thread.join();
 			} catch (InterruptedException e) {
 				logger.error("Error stopping worker.", e);
 			}finally{
 				job = null;
 				busy = false;
 			}
 		}
 	}
 	
 	/**
 	 * Returns true if the worker is running.
 	 * @return
 	 */
 	public boolean isBusy(){
 		return busy;
 	}
 	
 	@Override
 	public void run() {
 		while(running){
 			Set<File> shapeFiles = job.getShapeFiles();
 			Iterator<File> files = shapeFiles.iterator();
 			boolean compress = job.isCompress();
 			// Create output file.
 			String fileName = job.getOutFile();
 			if(compress)
 				fileName += ".gz";
 			File outFile = new File(fileName);
 			OutputStream out = null;
 			try{
 				// Create a file output, and GZIP it if necessary.
 				out = new BufferedOutputStream(new FileOutputStream(outFile));
 				// If compression is desired, wrap the output in a gzip stream.
 				if(compress)
 					out = new GZIPOutputStream(out);
 			}catch(IOException e){
 				logger.error("Failed to open output file {} in job {}.", job.getOutFile(), job.getName(), e);
 				break;
 			}
 			int i=0;
 			int end = shapeFiles.size() - 1;
 			while(files.hasNext()){
 				File file = files.next();
 				if(file.getName().toLowerCase().endsWith("shp")){
 					logger.info("Processing file {}.", file.getName());
 					
 					// Build shp2pgsql command.
 					StringBuffer command = new StringBuffer("shp2pgsql ");
 					if(i == 0){
 						command.append("-d ");
					}else{
						command.append("-a ");
 					}
 					if(i == end)
 						command.append("-I ");
 					command.append(file.getAbsolutePath()).append(" ");
 					command.append(job.getSchemaName()).append(".").append(job.getTableName());
 					
 					try {
 						// Start a process for the command and get the input stream.
 						Process proc = Runtime.getRuntime().exec(command.toString());
 						InputStream in = proc.getInputStream();
 						//  Write the shp2pgsql output to the output stream.
 						byte[] buf = new byte[1024];
 						int read = 0;
 						int accum = 0;
 						int maxAccum = 1024 * 1024;	
 						while((read = in.read(buf)) > -1){
 							out.write(buf, 0, read);
 							// If we've accumulated more than 1M of data, flush it.
 							accum += read;
 							if(accum > maxAccum)
 								out.flush();
 						}
 						
 						in.close();
 
 						// Wait for the process to finish, and report any problems.
 						int retval = proc.waitFor();
 						if(retval != 0)
 							logger.warn("Return value from shp2pgsql was {}.", retval);
 						logger.info("File complete", file.getName());
 					} catch (IOException e){
 						logger.error("Failed while processing job {}.", job.getName(), e);
 					} catch(InterruptedException e) {
 						logger.error("Failed while processing job {}.", job.getName(), e);
 					}
 					++i;
 				}
 			}
 			try{
 				// If gzipping, finish the archive.
 				if(compress)
 					((GZIPOutputStream) out).finish();
 				
 				// Close the stream.
 				out.close();
 			}catch(IOException e){
 				logger.error("Failed to close outputstream in job {}.", job.getName());
 			}
 			running = false;
 			busy = false;
 		}
 	}
 
 }
