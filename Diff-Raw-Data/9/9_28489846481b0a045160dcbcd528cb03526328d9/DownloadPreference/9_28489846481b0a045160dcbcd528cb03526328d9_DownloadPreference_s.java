 /* Copyright 2010 Patrick O'Leary.  All rights reserved.
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only.
  * See the file CDDL.txt in this distribution or
  * http://opensource.org/licenses/cddl1.php for details.
  */
 
 package com.olearyp.gusto;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.channels.Channels;
 import java.nio.channels.FileChannel;
 import java.nio.channels.ReadableByteChannel;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.preference.Preference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ProgressBar;
 
 public class DownloadPreference extends Preference {
 
 	private String url;
 	private String destination = "";
 	private View v;
 
 	public DownloadPreference(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	public DownloadPreference(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public DownloadPreference(Context context) {
 		super(context);
 		init();
 	}
 
 	private void init() {
 		this.setOnPreferenceClickListener(new DownloadPreferenceListener());
 		this.setWidgetLayoutResource(R.layout.download_progress);
 	}
 
 	public String getUri() {
 		return url;
 	}
 
 	public void setUri(String uri) {
 		this.url = uri;
 	}
 
 	public String getDestination() {
 		return destination;
 	}
 
 	public void setDestination(String destination) {
 		this.destination = destination;
 	}
 
 	@Override
 	protected View onCreateView(ViewGroup parent) {
 		v = super.onCreateView(parent);
 		return v;
 	}
 
 	private class DownloadPreferenceListener implements OnPreferenceClickListener {
 
 		@Override
 		public boolean onPreferenceClick(Preference preference) {
 			ProgressBar pb = (ProgressBar) v.findViewById(R.id.progress);
			new Downloader(pb).doInBackground((Void) null);
 			return true;
 		}
 	}
 	
 	private class Downloader extends AsyncTask<Void, Integer, Void> {
 
 		protected static final long dl_block_size = 4096;
 		private ProgressBar pb;
 
 		public Downloader(ProgressBar pb) {
 			super();
 			this.pb = pb;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			pb.setProgress(values[0]);
 			super.onProgressUpdate(values);
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Create an instance of HttpClient.
 			HttpClient client = new DefaultHttpClient();
 
 			try {
 				HttpGet method = new HttpGet(url);
 				ResponseHandler<Void> responseHandler = new ResponseHandler<Void>() {
 					@Override
 					public Void handleResponse(HttpResponse response)
 							throws ClientProtocolException, IOException {
 						HttpEntity entity = response.getEntity();
 						Long fileLen = entity.getContentLength();
 						if(entity != null) {
 							InputStream filecont = entity.getContent();
 							ReadableByteChannel dl_chan = Channels.newChannel(filecont);
 							File dst = new File(destination);
 							dst.createNewFile();
 							FileOutputStream fout = new FileOutputStream(dst);
 							FileChannel fout_chan = fout.getChannel();
 							
 							int bytesRead = 0;
 							while(bytesRead < fileLen) {
 								bytesRead += fout_chan.transferFrom(dl_chan, bytesRead, dl_block_size);
 								publishProgress(Math.round(bytesRead/fileLen.floatValue()*100));
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
 							}
 						}
 						return null;
 					}
 				};
 				client.execute(method, responseHandler);
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			client.getConnectionManager().shutdown();
 			return null;
 		}
 		
 	}
 }
