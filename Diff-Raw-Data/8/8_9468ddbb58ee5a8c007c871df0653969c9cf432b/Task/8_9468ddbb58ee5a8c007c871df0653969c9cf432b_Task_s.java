 /**
    Copyright [Shan Yin]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package com.snaker;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public abstract class Task implements Runnable{
 	private static Log logger = LogFactory.getLog(Task.class);
 	enum Status {RUNNING,PENDING,FINISHED,CANCELLED,FAILED};
 	private String id;
 	private long startTime;
 	private long endTime;
 	private Status status;
 	private Setting.Proxy proxy;
 	private String savePath;
 	private List<Downloader> downloadings = new ArrayList<Downloader>();
 	private DownloadManager downloadManager;
 	private RecognizerManager recognizerManager;
 	private Set<String> downloadedUrls = new HashSet<String>();
 	public long getStartTime() {
 		return startTime;
 	}
 	public void setStartTime(long startTime) {
 		this.startTime = startTime;
 	}
 	public long getEndTime() {
 		return endTime;
 	}
 	public void setEndTime(long endTime) {
 		this.endTime = endTime;
 	}
 	public Status getStatus() {
 		return status;
 	}
 	public void setStatus(Status status) {
 		this.status = status;
 	}
 	public void setProxy(Setting.Proxy proxy) {
 		this.proxy = proxy;
 	}
 	public Setting.Proxy getProxy() {
 		return proxy;
 	}
 	public void setSavePath(String savePath) {
 		if(!savePath.endsWith(File.separator)){
 			savePath += File.separator;
 		}
 		this.savePath = savePath;
 	}
 	public String getSavePath() {
 		return savePath;
 	}
 	public void downloadFininshed(Downloader d){
 		downloadings.remove(d);
 	}
 	
 	public void send(Downloader d) throws IOException{
 		d.setTask(this);
 		downloadings.add(d);
 		try{
 			downloadManager.start(d);
 		}
 		catch(Exception e){
 			logger.error("start download failed",e);
 			downloadings.remove(d);
 		}
 		return ;
 	}
 	public Downloader sendGet(String url) throws IOException{
 		Downloader d = new Downloader();
 		d.setUrl(url);
 		d.setGet(true);
 		send(d);
 		return d;
 	}
 	public Downloader sendPost(String url,DownloadParams parms) throws IOException{
 		Downloader d = new Downloader();
 		d.setUrl(url);
 		d.setGet(false);
 		d.setParms(parms);
 		send(d);
 		return d;
 	}
 	public String recognize(String url) throws IOException{
 		Downloader d = sendGet(url);
		byte[] image = d.getResponseBody(true);
		if(recognizerManager!=null){
			return recognizerManager.recognize(url,image);
 		}
 		return null;
 	}
 	public Downloader save(String url) throws IOException {
 		return save(url,null,null);
 	}
 	public Downloader save(String url,String subFolder) throws IOException {
 		return save(url,subFolder,null);
 	}
 	public Downloader save(String url,String subFolder,String fileName) throws IOException {
 		if(this.downloadedUrls.contains(url)){
 			return null;
 		}
 		downloadedUrls.add(url);
 		Downloader d = new Downloader();
 		d.setUrl(url);
 		d.setGet(true);
 		if(subFolder!=null){
 			d.setSubFolder(subFolder);
 		}
 		if(fileName!=null){
 			d.setFileName(fileName);
 		}
 		final String savePath = this.getSavePath();
 		d.setHandler(new DownloadHandler(){
 			FileOutputStream fos = null;
 
 			@Override
 			public void handle(byte[] buffer, int size) throws IOException {
 					fos.write(buffer, 0, size);
 			}
 
 			@Override
 			public void start(Downloader d) throws FileNotFoundException {
 				String fileName = d.getFileName();
 				String subFolder = d.getSubFolder();
 				if(subFolder == null) subFolder="";
 				String folder = savePath+subFolder;
 				File f = new File(folder + fileName);
 				String newFileName = fileName;
 				new File(folder).mkdirs();
 				if(f.exists()){
 					int tag = fileName.lastIndexOf('.');
 					String prefix = "";
 					String postfix = "";
 					if (tag > 0) {
 						prefix = fileName.substring(0, tag);
 					} else {
 						prefix = fileName;
 					}
 					if(tag!=-1 && tag<fileName.length()-1){
 						postfix = fileName.substring(tag+1);
 					}
 					while (f.exists()) {
 						newFileName = prefix + "_"
 								+ System.currentTimeMillis()+"."+postfix;
 						f = new File(folder + newFileName);
 					}
 				}
 				d.setFileName(newFileName);
 				fos = new FileOutputStream(f);
 			}
 
 			@Override
 			public void stop() {
 				if(fos!=null){
 					try {
 						fos.close();
 					} catch (IOException e) {
 						logger.error("close file failed",e);
 					}
 				}
 			}
 			
 		});
 		send(d);
 		return d;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 	public String getId() {
 		return id;
 	}
 	
 	protected abstract void execute() throws Exception;
 	
 	public final void run(){
 		this.setStatus(Status.RUNNING);
 		downloadManager.prepare(this);
 		
 		try {
 			execute();
 			do{
 				Thread.sleep(10000);
 				if(downloadings.isEmpty()){
 					break;
 				}
 			}while(true);
 			this.setStatus(Status.FINISHED);
 		} catch (InterruptedException e) {
 			logger.error("interrupted",e);
 		} catch (Exception e) {
 			logger.error("exeute failed",e);
 			this.setStatus(Status.FAILED);
 		}
 		finally{
 			downloadManager.clean(this);
 			downloadedUrls.clear();
 		}
 	}
 	
 	public void setDownloadManager(DownloadManager downloadManager) {
 		this.downloadManager = downloadManager;
 	}
 	public DownloadManager getDownloadManager() {
 		return downloadManager;
 	}
 	public RecognizerManager getRecognizerManager() {
 		return recognizerManager;
 	}
 	public void setRecognizerManager(RecognizerManager recognizerManager) {
 		this.recognizerManager = recognizerManager;
 	} 
 }
