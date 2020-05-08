 package cz.cvut.keyczar.homework;
 
 import cz.cvut.keyczar.Verifier;
 import cz.cvut.keyczar.exceptions.KeyczarException;
 
 import java.io.*;
 import java.net.Socket;
 import java.util.List;
 import java.util.Vector;
 
 /** Verify incoming messages and HMACs in a loop.
  * The communications protocol is as follows:
  * <pre>
  * Verifier upon start: "READY"
  * loop:
  *   Client to Verifier: [message bytes][null byte][25 bytes of H-MAC]
  *   Verifier to Client: OK (when verification passes)
  *   Verifier to Client: NOT OK (when verification fails)
  *   Verifier to Client: ERROR: [error text] (when input is invalid)
  * end loop
  * Client to Verifier: (closes channel)
  * Verifier to Client: BYE (closes channel)
  * </pre>
  */
 public class MessageVerifier implements Runnable {
 
 	private Verifier keyCzarVerifier = null;
 	private PrintStream outputStream;
 	private InputStream inputStream;
     private String kudoFile;
     private Socket socket;
 
     public MessageVerifier(Socket sock, String kudoFile, String keydir) throws KeyczarException, IOException {
 		this.keyCzarVerifier = new Verifier(keydir);
         this.outputStream = new PrintStream(sock.getOutputStream());
 		this.inputStream = sock.getInputStream();
         this.outputStream.println("READY");
         this.outputStream.flush();
         this.kudoFile = kudoFile;
         this.socket = sock;
     }
 
 	/** Runs the instance
 	 *
 	 */
 	public void run() {
 		verifyMessages();
         try {
             socket.shutdownOutput();
             socket.shutdownInput();
             socket.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
 	}
 
 	private void verifyMessages()  {
 		while (true) {
 			try {
 				verifyOne();
 			}
 			catch (EOF e) {
 				System.out.println("Disconnected.");
 				return;
 			}
 			catch (Throwable t) {
 				System.err.println("ERROR: " + t.getMessage());
 			}
 		}
 	}
 
 	private void verifyOne() throws Throwable {
 		byte[] message = readNullTerminatedMessage();
 		byte[] hMAC = readHMAC();
         if (message.length > 100) return;
 		boolean result = keyCzarVerifier.verify(message, hMAC);
 		outputStream.println(result ? "OK" : "NOT OK");
         outputStream.flush();
         if (result) {
             System.out.print("Message received: ");
             System.out.write(message);
             System.out.println();
             String msg = new String(message);
             /*if ("halt".equalsIgnoreCase(msg) || "shutdown".equals(msg)) {
                 System.exit(0);
             }            */
             if (msg.startsWith("echo_")) {
                 FileOutputStream fstream = new FileOutputStream(this.kudoFile, true);
                 try {
                     PrintStream out = new PrintStream(fstream);
                     out.println(msg.substring(5));
                     out.flush();
                 } finally {
                     fstream.close();
                 }
             }
         }
 	}
 
 	private byte[] readNullTerminatedMessage() throws Throwable {
 		List<Byte> message = new Vector<Byte>();
 		while (true) {
 			byte b = byteFromInput();
 			if (b == 0) {
 				break; // don't append the null byte
 			}
 			message.add(b);
 		}
 		return listToByteArray(message);
 	}
 
 	private byte[] readHMAC() throws Throwable {
 		byte[] hMAC = new byte[25];
 		for (int i = 0; i < 25; i++) {
 			byte b = byteFromInput();
 			hMAC[i] = b;
 		}
 		return hMAC;
 	}
 
 	private static byte[] listToByteArray(List<Byte> list) {
 		byte[] array = new byte[list.size()];
 		for (int i = 0; i < array.length; i++) {
 			array[i] = list.get(i);
 		}
 		return array;
 	}
 
 	private byte byteFromInput() throws Throwable {
 		int i = inputStream.read();
 		if (i < 0) {
 			throw new EOF();
 		}
 		return (byte) i;
 	}
 
 	public class EOF extends RuntimeException {
 	}
 
 }
