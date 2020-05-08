 package com.johnpickup.backup;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.johnpickup.backup.BackupReportItem.BackupAction;
 
 public class BackupReport implements BackupEventListener {
 
 	private List<String> errors = new ArrayList<String>();
 	private List<BackupReportItem> items = new ArrayList<BackupReportItem>();
 	private Date startTime;
 	private Date startBackup;
 	private Date endBackup;
 
 	public void clear() {
 		errors.clear();
 		items.clear();
 	}
 
 	private void add(BackupReportItem backupReportItem) {
 		items.add(backupReportItem);		
 	}
 
 	@Override
 	public void onError(File source, String error) {
 		errors.add(error);
 	}
 
 	public long size() {
 		return items.size();
 	}
 
 	public BackupReportItem get(int index) {
 		return items.get(index);
 	}
 
 	public void outputTo(PrintStream output) {
 		if (errors.size() > 0) {
 			output.println("*** ERRORS OCCURRED DURING BACKUP ***");
 			for (String error : errors) {
 				output.println(error);
 			}
 			output.println("*************************************");
 		}
 		
 		long totalSize = 0;
 		if (items.size() > 0) {
 			output.println("*** The following files were backed up ***");
 			for (BackupReportItem item : items) {
 				switch (item.getAction()) {
 				case COPIED:
 					output.println("Copied  " + item.getSourceFile().getAbsolutePath());
 					totalSize += item.getSourceFile().length();
 					break;
 				case DELETED:
 					output.println("Deleted " + item.getTargetFile().getAbsolutePath());
 					break;
 				}
 			}
 			output.println("******************************************");
 		}
 		
 		output.print("Processed " + items.size()+ " files totalling ");
 		output.print(totalSize/1024 + "kB in ");
 		long scanSeconds = (startBackup.getTime() - startTime.getTime())/1000;
 		long backupSeconds = (endBackup.getTime() - startBackup.getTime())/1000;
		output.print(Utils.secondsToTimeString(backupSeconds) + "s");
 		if (backupSeconds > 0) {
 			output.println(" (" + (totalSize/1024/backupSeconds) + "kB/s)");
 		}
 		else {
 			output.println();
 		}
 		output.println("Scan took " + scanSeconds + "s");
 	}
 
 	@Override
 	public void onCopy(File source, File target) {
 		add(new BackupReportItem(source, target, BackupAction.COPIED));
 	}
 
 	@Override
 	public void onDelete(File target) {
 		add(new BackupReportItem(null, target, BackupAction.DELETED));
 	}
 
 	@Override
 	public void onStart() {
 		clear();
 		startTime = new Date();
 	}
 
 	@Override
 	public void onScanComplete(long totalFilesToCopy, long totalFilesToDelete, long totalBytesToCopy) {
 		startBackup = new Date();
 	}
 
 	@Override
 	public void onBackupComplete() {
 		endBackup = new Date();		
 	}
 }
