 /*  Copyright (C) 2011  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Logger;
 
 /**
  * Class for downloading images from the Internet.
  */
 public abstract class FileLoader {
 	private static Logger logger = Logger.getLogger(FileLoader.class.getName());
 
 	protected LinkedBlockingQueue<DownloadItem> downloadList = new LinkedBlockingQueue<DownloadItem>();
 	private LinkedList<Thread> workers = new LinkedList<>();
 
 	/**Delay between downloads. This is used to limit the number of connections**/
 	protected int downloadSleep = 1000;
 	protected int imageQueueWorkers;
 
 	private GetBinary getBinary = new GetBinary();
 
 	private File workingDir;
 
 	public FileLoader(File workingDir, int imageQueueWorkers) {
 		this.workingDir = workingDir;
 		this.imageQueueWorkers = imageQueueWorkers;
 		setUp(imageQueueWorkers);
 	}
 	
 	/**
 	 * Run before a file is added to the list.
 	 * @param url URL that was added
 	 * @param fileName relative path to working directory
 	 */
	protected void beforeImageAdd(URL url,String fileName){} // code to run before adding a file to the list
 	
 	/**
 	 * Run after a file was added to the list.
 	 * @param url URL that was added
 	 * @param fileName relative path to working directory
 	 */
 	protected void afterImageAdd(URL url,String fileName){} // code to run after adding a file to the list
 
 	public void add(URL url,String fileName){
		beforeImageAdd(url, fileName);
 		
 		if(downloadList.contains(url)) // is the file already queued? 
 			return;
 		
 		downloadList.add(new DownloadItem(url, fileName));
 		
 		afterImageAdd(url, fileName);
 	}
 	
 	/**
 	 * Set the delay between file downloads. Used to limit the number of connections.
 	 * @param sleep time between downloads in milliseconds
 	 */
 	public void setDownloadSleep(int sleep){
 		this.downloadSleep = sleep;
 	}
 
 	public void clearQueue(){
 		downloadList.clear();
 	}
 	
 	/**
 	 * Called after the queue has been cleared.
 	 */
 	protected void afterClearQueue(){}
 
 	/**
 	 * Download a file, how the data is used is handled in the method afterFileDownload
 	 * 
 	 * @param url URL to save
 	 * @param savePath relative save path
 	 */
 	private void loadFile(URL url, File savePath){
 		File fullPath = new File(workingDir, savePath.toString());
 
 		try{Thread.sleep(downloadSleep);}catch(InterruptedException ie){}
 
 		byte[] data = null;
 		try{
 			data = getBinary.getViaHttp(url);
 			afterFileDownload(data,fullPath,url);
 		}catch(PageLoadException ple){
 			onPageLoadException(ple);
 		}catch(IOException ioe){
 			onIOException(ioe);
 		}
 	}
 	
 	/**
 	 * Called when a server could be contacted, but an error code was returned.
 	 * @param ple the PageLoadException that was thrown
 	 */
 	protected void onPageLoadException(PageLoadException ple){
 		logger.warning("Unable to load " + ple.getUrl() + " , response is " + ple.getResponseCode());
 	}
 	
 	/**
 	 * Called when a page / File could not be loaded due to an IO error.
 	 * @param ioe the IOException that was thrown
 	 */
 	protected void onIOException(IOException ioe){
 		logger.warning("Unable to load page " + ioe.getLocalizedMessage());
 	}
 	
 	/**
 	 * Called when the file was successfully downloaded.
 	 * @param data the downloaded file
 	 * @param fullpath the absolute filepath
 	 * @param url the url of the file
 	 */
 	abstract protected void afterFileDownload(byte[] data, File fullpath, URL url);
 	
 	private void setUp(int image){
 		for(int i=0; i <image; i++){
 			workers.add(new DownloadWorker());
 		}
 
 		for(Thread t : workers){
 			t.start();
 		}
 	}
 
 	public void shutdown(){
 		logger.info("ImageLoader shutting down...");
 		
 		clearQueue();
 
 		for(Thread t : workers){
 			t.interrupt();
 		}
 
 		for(Thread t : workers){
 			try {t.join();} catch (InterruptedException e) {}
 		}
 		
 		logger.info("ImageLoader shutdown complete");
 	}
 	
 	/**
 	 * Called after a worker has processed an item from the list.
 	 * @param di the imageitem that was processed
 	 */
 	protected void afterProcessItem(DownloadItem di){}
 
 	class DownloadWorker extends Thread{
 		public DownloadWorker() {
 			super("Download Worker");
 
 			Thread.currentThread().setPriority(2);
 		}
 
 		@Override
 		public void run() {
 			while(! isInterrupted()){
 				try{
 					DownloadItem di;
 					di = downloadList.take(); // grab some work
 					if(di == null) // check if the item is valid
 						continue;
 
 					loadFile(di.getImageUrl(), new File(di.getImageName()));
 					afterProcessItem(di);
 					
 				}catch(InterruptedException ie){interrupt();} //otherwise it will reset it's own interrupt flag
 			}
 		}
 	}
 }
