 package file;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.charset.Charset;
 
 import folder.FolderTree;
 import folder.MFolderListener;
 import folder.Nod;
 
 public class FileClient{
 	/***
 	 * 
 	 * @param ft - folder tree
 	 * @param n - nod
 	 * @param host - hosts ip
 	 * @param key - key of file to transfer
 	 * @param path - path of folder
 	 * @param name - name of file
 	 */
 	  public FileClient (FolderTree ft, Nod n, String host, String key, String path, String name , String usr) {
 	    long start = System.currentTimeMillis();
 	    
 	    // localhost for testing
 	    Socket sock;
 		try {
 			sock = new Socket(host,13267);
 	    System.out.println("Connecting...");
 	    OutputStream os = sock.getOutputStream();
 	    key+='\n';
 	    os.write(key.getBytes(Charset.forName("UTF-8")));
 	    
 	    InputStream is = sock.getInputStream();
 	    // receive file
 	    this.receiveFile(ft, n, is, path, name, usr);
 	       long end = System.currentTimeMillis();
 	    System.out.println(end-start);
 
 	    sock.close();
 
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	  }
 
 	  /***
 	   * 
 	   * @param ft
 	   * @param n
 	   * @param is
 	   * @param path
 	   * @param name
 	   * @throws Exception
 	   */
 	  public void receiveFile(FolderTree ft, Nod n, InputStream is, String path, String name , String usr) throws Exception{
 		  try {
 			  	String tempStamp = "^";
 				// read this file into InputStream
 		 
 				// write the inputStream to a FileOutputStream
 				FileOutputStream outputStream = new FileOutputStream(new java.io.File(path+tempStamp+name));
 		 
 				int read = 0;
 				byte[] bytes = new byte[1024];
 		 
 				while ((read = is.read(bytes)) != -1) {
 					outputStream.write(bytes, 0, read);
 				}
 				
 				outputStream.close();
 				outputStream.flush();
 		 
 				//rename file to final name without tempStamp at the beginning of filename
 				java.io.File filenameWithTempStamp = new java.io.File(path+tempStamp+name); 
 				MFolderListener.ignorowanyPlik = name;
 				java.io.File docelowyPlik = new java.io.File(path+name);
 				if (docelowyPlik.exists()){
 				    docelowyPlik.delete();
 				}
 				filenameWithTempStamp.renameTo(docelowyPlik);
 				File f = new File(n.getName(),n.getPath(), usr);
 				f.setFileId(n.getValue());
 				long data = n.getHistory().getLast().getData();//ostatnia modyfikacja
 				f.setLastModified(data);
 				f.getCurrentFileWithLatestHistoryEntry().getSingleFileHistory().getLast().setData(data);
 				if(ft!=null){
 					ft.addFile(f, usr);
				}
				filenameWithTempStamp.setLastModified(data);//to powinno ustawi date modyfikacji 
 				System.out.println("Done!");
 				if(ft!=null){
 					System.out.println("updated Tree: ");
 					System.out.println(ft.toString());
 					System.out.println(" ");
 				}
 		 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	  }
 	}
