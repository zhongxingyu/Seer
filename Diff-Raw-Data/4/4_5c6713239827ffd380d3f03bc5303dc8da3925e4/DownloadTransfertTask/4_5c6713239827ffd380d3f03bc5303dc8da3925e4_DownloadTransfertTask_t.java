 package com.quanturium.androcloud2.requests;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
import android.util.Log;

 import com.quanturium.androcloud2.listeners.TransfertTaskListener;
 
 public class DownloadTransfertTask extends AbstractTransfertTask
 {
 	private long	timestamp;
 
 	public DownloadTransfertTask(TransfertTaskListener callback)
 	{
 		super(callback);
 	}
 
 	@Override
 	protected String doInBackground(AbstractTaskQuery... params)
 	{
 		DownloadTransfertTaskQuery query = (DownloadTransfertTaskQuery) params[0];
 		name = query.file.getName();
 		
 		try
 		{
			query.file.getParentFile().mkdirs();
 			query.file.createNewFile();
 			URL url = new URL(query.url);
 			
 			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 			connection.setInstanceFollowRedirects(true);
 
 			int lenght = connection.getContentLength();
 						
 			FileOutputStream f = new FileOutputStream(query.file);
 
 			InputStream in = connection.getInputStream();
 
 			byte[] buffer = new byte[1024];
 			int len1 = 0;
 			long written = 0;
 			boolean hasCancelled = false;
 
 			while ((len1 = in.read(buffer)) > 0)
 			{
 				written += len1;																
 				f.write(buffer, 0, len1);
 				
 				if (System.currentTimeMillis() > timestamp + 500)
 				{
 					timestamp = System.currentTimeMillis();
 					int percent = (int) ((written / (float) lenght) * 100);
 					
 					if(percent > 100)
 						percent = 100;
 					
 					publishProgress(percent);
 				}
 				
 				if(isCancelled())
 				{
 					hasCancelled = true;
 					break;
 				}
 			}
 			
 			f.close();
 			
 			if(hasCancelled)
 				query.file.delete();
 			{
 				return query.file.getPath();
 			}
 		}
 		catch (MalformedURLException e)
 		{
 			cancel(true);
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			cancel(true);
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 }
