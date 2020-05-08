 package org.ukiuni.irc4j.server.command;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 import org.ukiuni.irc4j.Conf;
 import org.ukiuni.irc4j.Log;
 import org.ukiuni.irc4j.server.ClientConnection;
 import org.ukiuni.irc4j.server.IRCServer;
 import org.ukiuni.irc4j.server.ServerChannel;
 import org.ukiuni.irc4j.storage.Storage;
 import org.ukiuni.irc4j.storage.Storage.WriteHandle;
 import org.ukiuni.lighthttpserver.util.FileUtil;
 
 public class FileRecieveThread extends Thread {
 	private IRCServer ircServer;
 	private ClientConnection selfClientConnection;
 	private String parameterString;
 
 	public FileRecieveThread(IRCServer ircServer, ClientConnection selfClientConnection, String parameterString) {
 		this.ircServer = ircServer;
 		this.selfClientConnection = selfClientConnection;
 		this.parameterString = parameterString;
 	}
 
 	public void run() {
 		try {
 			final String[] param = parameterString.split(" ");
 
			Log.log("/////////////////// socket = " + selfClientConnection.getUser().getHostName() + ":" + Integer.valueOf(param[5]));
			Socket socket = new Socket(selfClientConnection.getUser().getHostName(), Integer.valueOf(param[5]));
 			long fileSize = Long.valueOf(Integer.valueOf(param[6]));
 			InputStream in = socket.getInputStream();
 			byte[] buffer = new byte[1024];
 			long totalReaded = 0;
 			int readed = in.read(buffer);
 			WriteHandle writeHandle = Storage.getInstance().createWriteHandle(param[3], FileUtil.getMimeType(param[6]));
 			OutputStream out = writeHandle.getOutputStream();
 			while (totalReaded < fileSize && readed > 0) {
 				out.write(buffer, 0, readed);
 				totalReaded += readed;
 				if (totalReaded + buffer.length > fileSize) {
 					buffer = new byte[(int) (fileSize - totalReaded)];
 				}
 				readed = in.read(buffer);
 			}
 			socket.close();
 			out.close();
 			writeHandle.save();
 			Log.log("/////////////////// upload complete");
 			ServerChannel channel = selfClientConnection.getCurrentFileUploadChannel();
 			selfClientConnection.setCurrentFileUploadChannel(null);
 			String responseMessage = "file upload to " + Conf.getHttpServerURL() + "/file/" + writeHandle.getKey();
 			channel.sendMessage("PRIVMSG", selfClientConnection, responseMessage);
 			selfClientConnection.sendMessage("PRIVMSG", selfClientConnection, channel, responseMessage);
 			selfClientConnection.sendPartCommand(ircServer.getFQSN(), channel.getName(), "upload is completed.");// TODO
 		} catch (Throwable e) {
 			Log.log(e);
 		}
 	};
 
 }
