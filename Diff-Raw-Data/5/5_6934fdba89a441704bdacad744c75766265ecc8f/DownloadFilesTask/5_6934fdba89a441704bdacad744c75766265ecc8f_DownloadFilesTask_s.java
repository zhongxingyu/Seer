 package com.scrye.badgertunes;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.PowerManager;
 import android.widget.Toast;
 
 public class DownloadFilesTask extends AsyncTask<Void, Integer, String> {
 	PlayerActivity player;
 	Node root_node;
 	
 	DownloadFilesTask(PlayerActivity _player, Node _root_node) {
 		player = _player;
 		root_node = _root_node;
 	}
 	@Override
     protected String doInBackground(Void... inputs) {
         return downloadNode(root_node);
     }
 	
 	@Override
     protected void onProgressUpdate(Integer... progress) {
     }
 	
 	@Override
     protected void onPostExecute(String result) {
 		if(result != null) {
 			Toast.makeText(player, result, Toast.LENGTH_LONG).show();
 		} else {
 			Toast.makeText(player, "Done downloading " + root_node.filename, Toast.LENGTH_SHORT).show();
 		}
     }
 	
 	private String downloadNode(Node node) {
 		if(node.children == null) {
 			return downloadFile(node);
 		} else {
 			for(int i = 0; i < node.children.size(); i++) {
 				String error = downloadNode(node.children.get(i));
 				if(error != null) {
 					return error;
 				}
 			}
 			return null;
 		}
 	}
 	
 	private String downloadFile(Node file_node) {
 		// take CPU lock to prevent CPU from going off if the user 
         // presses the power button during download
         PowerManager pm = (PowerManager) player.getSystemService(Context.POWER_SERVICE);
         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
         wl.acquire();
 
         try {
             InputStream input = null;
             OutputStream output = null;
             HttpURLConnection connection = null;
             try {
                URL url = new URL(player.remote_address + "/get/" + file_node.filename);
                 connection = (HttpURLConnection) url.openConnection();
                 connection.connect();
 
                 // expect HTTP 200 OK, so we don't mistakenly save error report 
                 // instead of the file
                 if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                      return "Server returned HTTP " + connection.getResponseCode() 
                          + " " + connection.getResponseMessage();
 
                 // this will be useful to display download percentage
                 // might be -1: server did not report the length
                 int fileLength = connection.getContentLength();
 
                 // download the file
                 input = connection.getInputStream();
                 File dest_file = new File(player.local_root_dir, file_node.filename);
                 dest_file.getParentFile().mkdirs();
                 output = new FileOutputStream(dest_file);
 
                 byte data[] = new byte[4096];
                 long total = 0;
                 int count;
                 while ((count = input.read(data)) != -1) {
                     // allow canceling with back button
                     if (isCancelled())
                         return null;
                     total += count;
                     // publishing the progress....
                     if (fileLength > 0) // only if total length is known
                         publishProgress((int) (total * 100 / fileLength));
                     output.write(data, 0, count);
                 }
             } catch (Exception e) {
                 return e.toString();
             } finally {
                 try {
                     if (output != null)
                         output.close();
                     if (input != null)
                         input.close();
                 } 
                 catch (IOException ignored) { }
 
                 if (connection != null)
                     connection.disconnect();
             }
         } finally {
             wl.release();
         }
         return null;
 	}
 }
