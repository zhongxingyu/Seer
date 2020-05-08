 package com.johnpickup.backup;
 
 import java.io.File;
 import java.util.Date;
 
 public class BackupProgress implements BackupEventListener {
 	private long eventCount = 0;
 	private long filesCopied = 0;
 	private long filesDeleted = 0;
 	private int progressInterval = 100;
 	private Date intervalStart = new Date();
 	private long bytesCopied = 0;
 	private long totalFilesToCopy = 0;
 	private long totalFilesToDelete = 0;
 	private long totalBytesToCopy = 0;
 	private int allBytesCopied;
 	
 	@Override
 	public void onCopy(File source, File target) {
 		filesCopied++;
 		bytesCopied += source.length();
		allBytesCopied += source.length();
 
 		doEvent();
 	}
 
 	@Override
 	public void onDelete(File target) {
 		filesDeleted++;
 
 		doEvent();
 	}
 
 	@Override
 	public void onError(File source, String error) {
 	}
 
 	@Override
 	public void onStart() {
 		reset();
 	}
 
 	@Override
 	public void onScanComplete(long totalFilesToCopy, long totalFilesToDelete, long totalBytesToCopy) {
 		this.totalFilesToCopy = totalFilesToCopy;
 		this.totalFilesToDelete = totalFilesToDelete;
 		this.totalBytesToCopy = totalBytesToCopy;
 		this.allBytesCopied=0;
 		System.out.println("Starting backup...");
 		reset();
 	}
 
 	@Override
 	public void onBackupComplete() {
 		reportProgress();
 	}
 
 	private void doEvent() {
 		if (++eventCount % progressInterval == 0) {
 			reportProgress();
 		}
 	}
 
 	private void reportProgress() {
 		long seconds = ((new Date()).getTime() - intervalStart.getTime())/1000;
 		long percBytes = (allBytesCopied * 100) / totalBytesToCopy;
 		System.out.print("Copied "+ bytesCopied/1024 + "kB (" + percBytes + "%)");
 		if (seconds > 0) {
 			System.out.print(" in " + seconds + "s (" + bytesCopied/1024/seconds +"kB/s)");
 		}
 		System.out.println("  "+ filesCopied + " copied, " + filesDeleted + " deleted");
 		reset();
 	}
 
 	private void reset() {
 		bytesCopied=0;
 		filesCopied=0;
 		filesDeleted=0;
 		intervalStart=new Date();
 	}
 
 	public int getProgressInterval() {
 		return progressInterval;
 	}
 
 	public void setProgressInterval(int progressInterval) {
 		this.progressInterval = progressInterval;
 	}
 }
