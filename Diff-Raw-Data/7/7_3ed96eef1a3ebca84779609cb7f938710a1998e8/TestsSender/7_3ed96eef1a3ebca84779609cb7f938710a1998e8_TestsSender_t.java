 package za.ac.sun.cs.intlola.test.sending;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 
 import za.ac.sun.cs.intlola.file.Const;
 import za.ac.sun.cs.intlola.file.FileUtils;
 import za.ac.sun.cs.intlola.file.TestFile;
 import za.ac.sun.cs.intlola.preferences.PreferenceConstants;
 
 import com.google.gson.JsonObject;
 
 public class TestsSender {
 
 	public TestsSender(String username, String password) {
 		this.username = username;
 		this.password = password;
 	}
 
 	private Socket sock;
 	private String username;
 	private String password;
 
 	private void send(final TestFile testInfo) {
 		final byte[] readBuffer = new byte[FileUtils.BUFFER_SIZE];
 		final byte[] writeBuffer = new byte[FileUtils.BUFFER_SIZE];
 		try {
 			OutputStream snd = sock.getOutputStream();
 			InputStream rcv = sock.getInputStream();
 			snd.write(testInfo.toJSON().toString().getBytes());
 			snd.write(Const.EOF);
 			rcv.read(readBuffer);
 			String received = new String(readBuffer);
 			if (received.startsWith(Const.OK)) {
 				FileInputStream fis = new FileInputStream(testInfo.getPath());
 				int count;
 				while ((count = fis.read(writeBuffer)) >= 0) {
 					snd.write(writeBuffer, 0, count);
 				}
 				fis.close();
 				snd.write(Const.EOF);
 				snd.flush();
 				rcv.read(readBuffer);
 				received = new String(readBuffer);
 				if (!received.startsWith(Const.OK)) {
 					System.err.println(received);
 				}
 				fis = new FileInputStream(testInfo.getDataPath());
 				while ((count = fis.read(writeBuffer)) >= 0) {
 					snd.write(writeBuffer, 0, count);
 				}
 				fis.close();
 				snd.write(Const.EOF);
 				snd.flush();
 				rcv.read(readBuffer);
 				received = new String(readBuffer);
 				if (!received.startsWith(Const.OK)) {
 					System.err.println(received);
 				}
 
 			} else{
 				System.err.println(received);
 			}
 		} catch (FileNotFoundException fe) {
 			fe.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				sock.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private boolean login(String address, int port) {
 		try {
 			sock = new Socket();
 			sock.connect(new InetSocketAddress(
					address, port));
 			final JsonObject params = new JsonObject();
 			params.addProperty(Const.REQ, Const.LOGIN);
 			params.addProperty(Const.USER, username);
 			params.addProperty(Const.PASSWORD, password);
 			params.addProperty(Const.LANG, Const.JAVA);
 			final byte[] buffer = new byte[1024];
 			OutputStream snd = sock.getOutputStream();
 			snd.write(params.toString().getBytes());
 			snd.write(Const.EOF);
 			snd.flush();
 			InputStream rcv = sock.getInputStream();
 			rcv.read(buffer);
 			final String received = new String(buffer);
 			if (received.startsWith(Const.OK)) {
 				return true;
 			} else {
 				System.err.println(received);
 				return false;
 			}
 		} catch (final IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public static void main(final String argv[]) {
 		TestsSender sender = new TestsSender("username", "password");
		if (sender.login(PreferenceConstants.LOCAL_ADDRESS, 8011)) {
			TestFile testInfo = new TestFile("testing.zip", "data.zip",
 					"za.ac.sun.cs.Triangle", "testing", new String[] {
 							"EasyTests.java", "AllTests.java" });
 			sender.send(testInfo);
 		}
 	}
 
 }
