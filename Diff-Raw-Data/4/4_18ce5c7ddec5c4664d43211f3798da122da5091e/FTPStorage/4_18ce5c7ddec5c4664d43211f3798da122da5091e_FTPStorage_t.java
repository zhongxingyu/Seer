 package org.efreak.bukkitmanager.addon.ftpbackup;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPReply;
 import org.apache.commons.net.ftp.FTPSClient;
 import org.apache.commons.net.util.TrustManagerUtils;
 
 import org.efreak.bukkitmanager.util.BackupStorage;
 import org.efreak.bukkitmanager.util.FileHelper;
 import org.efreak.bukkitmanager.util.NotificationsHandler;
 
 public class FTPStorage extends BackupStorage {
 
 	private static String host, username, password, path, hostString, trustmgr, protocol;
 	private static int port;
 	private static boolean logging, ftps, implicit;
 	
 	public FTPStorage() {
 		enabled = config.getBoolean("Autobackup.FTP.Enabled");
 		tempDir = new File(FileHelper.getBackupDir(), "ftptemp");
 		if (!tempDir.exists()) tempDir.mkdir();
 	}
 	
 	static {
 		host = config.getString("Autobackup.FTP.Host");
 		port = config.getInt("Autobackup.FTP.Port");
 		username = config.getString("Autobackup.FTP.Username");
 		password = config.getString("Autobackup.FTP.Password");
 		path = config.getString("Autobackup.FTP.Path");
 		logging = config.getBoolean("Autobackup.FTP.Logging");
 		ftps = config.getBoolean("Autobackup.FTP.FTPS");
 		trustmgr = config.getString("Autobackup.FTP.TrustManager");
 		protocol = config.getString("Autobackup.FTP.Protocol");
 		implicit = config.getBoolean("Autobackup.FTP.isImplicit");
 		if (ftps) hostString = "ftps://" + host + ":" + port;
 		else hostString = "ftp://" + host + ":" + port;
 	}
 
 	@Override
 	public boolean storeFile() {
 		io.sendConsole(io.translate("FTPBackup.Start").replaceAll("%host%", hostString));
 		if (config.getBoolean("Notifications.Autobackup.FTP.Started")) NotificationsHandler.notify("Bukkitmanager", "FTPBackup Addon", "Uploading Backup to " + hostString);
 		boolean returnValue = true;
 		FTPClient ftp;
 		if (!ftps) ftp = new FTPClient();
 		else {
             FTPSClient ftps;
             if (protocol == "") ftps = new FTPSClient(implicit);
             else ftps = new FTPSClient(protocol, implicit);
             if ("all".equals(trustmgr)) {
                 ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
             } else if ("valid".equals(trustmgr)) {
                 ftps.setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
             } else if ("none".equals(trustmgr)) {
                 ftps.setTrustManager(null);
             }
             ftp = ftps;
 		}
 		try {
 			ftp.connect(host, port);
 			if (logging) io.sendConsole(io.translate("FTPBackup.Connected").replaceAll("%host%", hostString));
             int reply = ftp.getReplyCode();
             if (!FTPReply.isPositiveCompletion(reply)) {
                 ftp.disconnect();
                 io.sendConsoleError(io.translate("FTPBackup.Refused"));
                 return false;
             }
 			if (!ftp.login(username, password)) {
 				ftp.logout();
 				io.sendConsoleError(io.translate("FTPBackup.LoginError"));
 				return false;
 			}
 			ftp.setFileType(FTP.BINARY_FILE_TYPE);
 			ftp.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
 			ftp.enterLocalPassiveMode();
 			ftp.changeWorkingDirectory(path);
 			InputStream input = new FileInputStream(backupFile);
 			if (logging) {
				if (path != "") io.sendConsole(io.translate("FTPBackup.Uploading").replaceAll("%location%", path + "/" + backupFile.getName()));
				else io.sendConsole(io.translate("FTPBackup.Uploading").replaceAll("%location%", backupFile.getName()));
 			}
 			ftp.storeFile(backupFile.getName(), input);
 			input.close();
 			ftp.logout();
 		}catch (Exception e) {
             io.sendConsoleError(io.translate("FTPBackup.CantConnect").replaceAll("%host%", hostString));
             if (config.getDebug()) e.printStackTrace();
             returnValue = false;
 		}finally {
             if (ftp.isConnected()) {
                 try {
                     ftp.disconnect();
                 }catch (Exception e) {
                 	if (config.getDebug()) e.printStackTrace();
                 }
             }
 		}
 		if (returnValue) {
 			io.sendConsole(io.translate("FTPBackup.Uploaded").replaceAll("%host%", hostString));
 			if (config.getBoolean("Notifications.Autobackup.FTP.Finished")) NotificationsHandler.notify("Bukkitmanager", "FTPBackup Addon", "Uploaded Backup to " + hostString);
 		}else {
 			io.sendConsoleWarning(io.translate("FTPBackup.Error").replaceAll("%host%", hostString));
 			if (config.getBoolean("Notifications.Autobackup.FTP.Finished")) NotificationsHandler.notify("Bukkitmanager", "FTPBackup Addon", "Error uploading Backup to " + hostString);
 		}
 		return returnValue;
 	}
 }
