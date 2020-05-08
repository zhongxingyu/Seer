 package aether.repl;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import aether.io.Chunk;
 
 /**
  * This is the listener service of the replication
  * manager. It continuously 
  * */
 public class ReplicationListener implements Runnable {
 	private static ReplicationListener repListener;
 	
 	public static synchronized ReplicationListener getInstance () {
 		 if (repListener == null)  {
 			 repListener = new ReplicationListener ();			 
 		 }
 		 return repListener;
 	}
 	public void run () {
 		try {
 			/*ServerSocket socketListener = new ServerSocket (Replication.REPL_PORT_LISTENER);
 			Socket s = socketListener.accept();
 			InputStream is = s.getInputStream();
 			FileOutputStream fos = new FileOutputStream (new File ("tempfile")); 
 			byte[] buffer = new byte[2048];
 			
 			int read = 0;
 			int offset = 0;
 			
 			while ((read = is.read(buffer)) != -1) {
 				fos.write(buffer, offset, read - offset);
 				offset = 0;
 			}
 			
 			fos.close();*/
 			System.out.println("Started the Replication listener thread");
 			ServerSocket socketListener = new ServerSocket (Replication.REPL_PORT_LISTENER);
 			while (!Thread.currentThread().isInterrupted()) {
 				Socket s = socketListener.accept();
 				InputStream is = s.getInputStream();
 				Chunk c;
 				ObjectInputStream ois = new ObjectInputStream (is);
 				c = (Chunk) ois.readObject();
 				FileOutputStream fos = new FileOutputStream (new File(c.getChunkName()));
 				ObjectOutputStream oos = new ObjectOutputStream (fos);
 				oos.writeObject(c);
 				oos.close();
 				ois.close();
 				
				
 				System.out.println("Got the chunk "+c.getChunkName() + "and wrote to local machine");
 				
				//Update the local data structure FileChunk Metadata structure
 				FileChunkMetadata fcm = FileChunkMetadata.getInstance();
 				fcm.addChunk(c.getFileName(), new ChunkMetadata(c));
 				
 				//Update the HtbtBuddy map
				/*HtbtBuddyMap hbm = HtbtBuddyMap.getInstance();
 				hbm.put(new Host (s.getInetAddress(), Replication.HTBT_SND_PORT_NUMBER), null);*/
 				
 			}
 		} catch (IOException e) {
 			System.out.println("IOException at Replication listener");
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			System.out.println("Class not found at Replication listener");
 			e.printStackTrace();
 		}
 	}
 }
