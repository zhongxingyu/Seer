 package Server;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 
 /**
  * Dise Klasse kommuniziert mit einem Client. Sie wartet auf 
  * eine Nachricht vom client und fr daraufhin eine Aktion aus
  * @author Kno
  *
  */
 public class ClientConnection extends Thread {
 
 	Socket _socket;
 	Server _server;
 	BufferedReader _reader;
 	String message;
 	HashMap<String, String> _users;
 
 	public ClientConnection(Socket socket, Server server,
 			HashMap<String, String> user) {
 		_socket = socket;
 		_server = server;
 		_users = user;
 	}
 
 	public void run() {
 		System.out.println("warte auf auszufhrende Funktion!");
 		try {
 			while (true) {
 				_reader = new BufferedReader(new InputStreamReader(
 						_socket.getInputStream()));
 				String message = "";
 				message = (_reader.readLine());
 				System.out.println("Nachricht:" + message);
 				switch (message) {
 				//GPS informationen werden vom Client bentigt
 				case "GPS":
 					System.out
 							.println("Wichtige Geoinformationen werden gesendet:");
 					message = "";
 					message = (_reader.readLine());
 					System.out.println("Geoinformationen erhalten: " + message);
 					break;
 					//Der Client mchte sich einloggen
 				case "Login":
 					System.out.println("empfange daten");
 					message = (_reader.readLine());
 
 					System.out.println("empfangen: " + message);
 					String[] tempinfo = message.split(" ");
 					String returnmessage;
 					try {
 						System.out.println(tempinfo[0] + "___" + tempinfo[1]);
 						System.out.println(_users.get(tempinfo[0]));
 						if (tempinfo[1].equals(_users.get(tempinfo[0])))
 							returnmessage = "Success";
 						else
 							returnmessage = "Failure";
 					} catch (Exception e) {
 						returnmessage = "Failure";
 					}
 					System.out.println("returnmess:" + returnmessage);
 					_server.sendMessage(returnmessage, this);
 					System.out.println("login beendet ");
 					break;
 					//Der Client mchte ein Bild senden
 				case "Bild":
 					System.out.println("Bild wird empfangen");
 					saveFile(_socket.getInputStream());
 					message = "";
 					break;
 					//Der Client mchte aktuelle Informationen erhalten
 				case "Info":
					
 					_server.sendMessage(_server.getCurrentInformations(), this);
 					message = "";
 					break;
 				default:
 					System.out.println("Invalide Eingabe: " + message);
 					message = "";
 				}
 			}
 		} catch (Exception e) {
 			System.out.println("ClientDisconnected");
 			_server.killConnections(this);
 
 		}
 
 	}
 
 	/**
 	 * ungetestete Methode zur speicherung einer Datei!
 	 * 
 	 * @param filename
 	 * @param inputStr
 	 * @throws IOException
 	 */
 	private void saveFile(InputStream inputStr) throws IOException {
 		// create socket
 		int filesize = 100000; // filesize temporary hardcoded
 
 		int bytesRead;
 		int current = 0;
 
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
 				.format(new Date());
 		String fileName = "IMG_" + timeStamp + ".jpg";
 
 		byte[] mybytearray = new byte[filesize];
 		InputStream is = inputStr;
 		FileOutputStream fos = new FileOutputStream(fileName);
 		BufferedOutputStream bos = new BufferedOutputStream(fos);
 		// bytesRead = is.read(mybytearray, 0, mybytearray.length);
 		// current = bytesRead;
 		do {
 			bytesRead = is.read(mybytearray, current,
 					(mybytearray.length - current));
 			if (bytesRead >= 0)
 				current += bytesRead;
 		} while (bytesRead > -1);
 
 		bos.write(mybytearray, 0, current);
 		bos.flush();
 		bos.close();
 		System.out.println("OK bild gespeichert");
 	}
 
 	public Socket getSocket() {
 		return _socket;
 
 	}
 }
