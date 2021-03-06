 /*
  * Gallery Remote - a File Upload Utility for Gallery 
  *
  * Gallery - a web based photo album viewer and editor
  * Copyright (C) 2000-2001 Bharat Mediratta
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or (at
  * your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package com.gallery.GalleryRemote;
 
 import HTTPClient.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import javax.swing.*;
 import com.gallery.GalleryRemote.model.*;
 
 public class GalleryComm1 implements GalleryComm {
 	private static final String MODULE = "GalleryCom";
 	
 	private static final String PROTOCAL_VERSION = "1";
 	private static final String SCRIPT_NAME = "gallery_remote.php";
 
 	/*private String mURLString = null;
 	private String mUsername = null;
 	private String mPassword = null;
 	private String mAlbum = null;
 	
 	private ArrayList mFileList;
 	private ArrayList mAlbumList;
 	
 	private String mStatus;
 	private int mUploadedCount;
 	
 	private boolean mDone = false;*/
 	
 	StatusUpdate su;
 	protected boolean isLoggedIn = false;
 	protected Gallery g = null;
 	int pId = -1;
 	
 	static {
 		//-- our policy handler accepts all cookies ---
 		CookieModule.setCookiePolicyHandler(new CookiePolicyHandler() {
 			public boolean acceptCookie(Cookie cookie, RoRequest req, RoResponse resp) {
 				return true;
 			}
 			public boolean sendCookie(Cookie cookie, RoRequest req) {
 				return true;
 			}
 		});
 	}
 	
 	public GalleryComm1(StatusUpdate su, Gallery g) {
 		this.su = su;
 		this.g = g;
 	}
 	
 	void doTask(GalleryTask task, boolean async) {
 		if (async) {
 			Thread t = new Thread(task);
 			t.start();
 		} else {
 			task.run();
 		}
 	}
 	
 	public void uploadFiles( StatusUpdate su, boolean async ) {
 		 doTask(new UploadTask(), async);
 	}
 
 	public void fetchAlbums( StatusUpdate su, boolean async ) {
 		doTask(new AlbumListTask(), async);
 	}
 	
 	/**
 	 *	Causes the GalleryComm instance to fetch the album properties
 	 *	for the given Album.
 	 *	
 	 *	@param su an instance that implements the StatusUpdate interface.
 	 */
 	public void albumInfo( StatusUpdate su, Album a, boolean async ) {
 		/* TEMPORARY */	
 		throw new RuntimeException( "This method is not implemented yet" );
 	}
 	
 	/**
 	 *	Causes the GalleryComm instance to fetch the album properties
 	 *	for the given Album.
 	 *	
 	 *	@param su an instance that implements the StatusUpdate interface.
 	 */
 	public void logOut() {
 		/* TEMPORARY */	
 		throw new RuntimeException( "This method is not implemented yet" );
 	}
 	
 	
 	
 	//-------------------------------------------------------------------------
 	//-- GalleryTask
 	//-------------------------------------------------------------------------	
 	abstract class GalleryTask implements Runnable {
 		HTTPConnection mConnection;
 		
 		boolean interrupt = false;
 		
 		public void run() {
 			su.setInProgress(true);
 			if ( ! isLoggedIn ) {
 				if ( !login() ) {
 					su.setInProgress(false);
 					return;
 				}
 				
 				isLoggedIn = true;
 			} else {
 				Log.log(Log.TRACE, MODULE, "Still logged in to " + g.toString());
 			}
 			
 			runTask();
 			su.setInProgress(false);
 		}
 		
 		public void interrupt() {
 			interrupt = true;
 		}
 		
 		abstract void runTask();
 
 		private boolean login() {
 			status("Logging in to " + g.toString());
 			
 			try	{
 				URL url = g.getUrl();
 				String urlPath = url.getFile() + SCRIPT_NAME;
 				Log.log(Log.TRACE, MODULE, "Url: " + url + SCRIPT_NAME);
 				
 				NVPair form_data[] = {
 					new NVPair("cmd", "login"),
 					new NVPair("protocal_version", PROTOCAL_VERSION),
 					new NVPair("uname", g.getUsername()),
 					new NVPair("password", g.getPassword())
 				};
 				Log.log(Log.TRACE, MODULE, "login parameters: " + Arrays.asList(form_data));
 				
 				HTTPConnection mConnection = new HTTPConnection(url);
 				HTTPResponse rsp = mConnection.Post(urlPath, form_data);
 				
 				if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
 					// retry, the library will have fixed the URL
 					status("Received redirect, following...");
 					
 					rsp = mConnection.Post(urlPath, form_data);
 				}
 				
 				if (rsp.getStatusCode() >= 300)	{
 					error("HTTP Error: "+ rsp.getStatusCode()+" "+rsp.getReasonLine());
 					return false;
 				} else {
 					String response = new String(rsp.getData()).trim();
 					Log.log(Log.TRACE, MODULE, response);
 					
 					if (response.indexOf("SUCCESS") >= 0) {
 						status("Logged in");
 						return true;
 					} else {
 						error("Login Error: " + response);
 						return false;
 					}
 				}
 			} catch (IOException ioe) {
 				Log.logException(Log.ERROR, MODULE, ioe);
 				status("Error: " + ioe.toString());
 			} catch (ModuleException me) {
 				Log.logException(Log.ERROR, MODULE, me);
 				status("Error handling request: " + me.getMessage());
 			}
 	
 			return false;
 		}
 	}
 	
 	class UploadTask extends GalleryTask {		
 		void runTask() {
 			ArrayList pictures = g.getAllPictures();
 			
 			pId = su.startProgress(0, pictures.size(), "Uploading pictures");
 			
 			// upload each file, one at a time
 			boolean allGood = true;
 			int uploadedCount = 0;
 			Iterator iter = pictures.iterator();
 			while (iter.hasNext() && allGood && !interrupt) {
 				Picture p = (Picture) iter.next();
 				
 				su.updateProgressStatus(pId, "Uploading " + p.toString()
 					+ " (" + (uploadedCount + 1) + "/" + pictures.size() + ")");
 				
 				allGood = uploadPicture(p);
 				
 				su.updateProgressValue(pId, uploadedCount++);
 				
 				p.getAlbum().removePicture(p);
 			}
 			
 			if (allGood) {
 				su.stopProgress(pId, "Upload complete");
 			} else {
 				su.stopProgress(pId, "Upload failed");
 			}
 			
 			pId = -1;
 		}
 
 		boolean uploadPicture(Picture p) {
 			try	{
 				URL url =g.getUrl();
 				String urlPath = url.getFile() + SCRIPT_NAME;
 				Log.log(Log.TRACE, MODULE, "Url: " + url + SCRIPT_NAME);
 			
 				NVPair[] opts = {
 					new NVPair("set_albumName", p.getAlbum().getName()),
 					new NVPair("cmd", "add-item"), 
 					new NVPair("protocal_version", PROTOCAL_VERSION)
 				};
 				Log.log(Log.TRACE, MODULE, "add-item parameters: " + Arrays.asList(opts));
 	
 				NVPair[] afile = { new NVPair("userfile", p.getUploadSource().getAbsolutePath()) };
 				NVPair[] hdrs = new NVPair[1];
 				byte[]   data = Codecs.mpFormDataEncode(opts, afile, hdrs);
 				HTTPConnection mConnection = new HTTPConnection(url);
 				HTTPResponse rsp = mConnection.Post(urlPath, data, hdrs);
 				
 				if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
 					// retry, the library will have fixed the URL
 					status("Received redirect, following...");
 					
 					rsp = mConnection.Post(urlPath, data, hdrs);
 				}
 								
 				if (rsp.getStatusCode() >= 300)	{
 					error("HTTP Error: "+ rsp.getStatusCode()+" "+rsp.getReasonLine());
 					return false;
 				} else {
 					String response = new String(rsp.getData()).trim();
 					Log.log(Log.TRACE, MODULE, response);
 	
 					if (response.indexOf("SUCCESS") >= 0) {
 						trace("Upload successful");
 						return true;
 					} else {
 						error("Upload Error: " + response);
 						return false;
 					}
 				}
 			} catch (IOException ioe)	{
 				Log.logException(Log.ERROR, MODULE, ioe);
 				error("Error: " + ioe.toString());
 			} catch (ModuleException me) {
 				Log.logException(Log.ERROR, MODULE, me);
 				error("Error handling request: " + me.getMessage());
 			}		
 			
 			return false;
 		}
 	}
 	
 	class AlbumListTask extends GalleryTask {
 		void runTask() {
 			status("Fetching albums from " + g.toString());
 			
 			try {
 				URL url =g.getUrl();
 				String urlPath = url.getFile() + SCRIPT_NAME;
 				Log.log(Log.TRACE, MODULE, "Url: " + url + SCRIPT_NAME);
 				
 				NVPair form_data[] = {
 					new NVPair("cmd", "fetch-albums"),
 					new NVPair("protocal_version", PROTOCAL_VERSION),
 					new NVPair("uname", g.getUsername()),
 					new NVPair("password", g.getPassword())
 				};
 				Log.log(Log.TRACE, MODULE, "fetchAlbums parameters: " + Arrays.asList(form_data));
 				
 				mConnection = new HTTPConnection(url);
 				HTTPResponse rsp = mConnection.Post(urlPath, form_data);
 				
 				if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
 					// retry, the library will have fixed the URL
 					status("Received redirect, following...");
 					
 					rsp = mConnection.Post(urlPath, form_data);
 				}
 				
 				if (rsp.getStatusCode() >= 300)	{
 					error("HTTP Error: "+ rsp.getStatusCode()+" "+rsp.getReasonLine());
 					return;
 				} else {
 					String response = new String(rsp.getData()).trim();
 					Log.log(Log.TRACE, MODULE, response);
 	
 					if (response.indexOf("SUCCESS") >= 0) {
 						ArrayList mAlbumList = new ArrayList();
 						
 						// build the list of hashtables here...
 						StringTokenizer lineT = new StringTokenizer(response, "\n");
 						while (lineT.hasMoreTokens()) {
 							StringTokenizer colT = new StringTokenizer(lineT.nextToken(), "\t");
 							
							
 							if (colT.countTokens() == 2) {
 								Album a = new Album();
 								
 								a.setName( URLDecoder.decode(colT.nextToken()) );
 								a.setTitle( URLDecoder.decode(colT.nextToken()) );
 
 								mAlbumList.add(a);
 							}
 						}
 						
 						status("Fetched albums");
 						
 						g.setAlbumList(mAlbumList);
 					} else {
 						error("Error: " + response);
 					}
 				}
 			} catch (IOException ioe) {
 				Log.logException(Log.ERROR, MODULE, ioe);
 				status("Error: " + ioe.toString());
 			} catch (ModuleException me) {
 				Log.logException(Log.ERROR, MODULE, me);
 				status("Error: " + me.toString());
 			} catch (Exception ee) {
 				Log.logException(Log.ERROR, MODULE, ee);
 				status("Error: " + ee.toString());
 			}
 		}
 	}
 
 	//-------------------------------------------------------------------------
 	//-- 
 	//-------------------------------------------------------------------------	
 	void status(String message) {
 		Log.log(Log.INFO, MODULE, message);
 		if (pId != -1) {
 			su.setStatus(message);
 		} else {
 			su.updateProgressStatus(pId, message);
 		}
 	}
 	
 	void error(String message) {
 		su.error( message );
 		status(message);
 	}
 	
 	void trace(String message) {
 		Log.log(Log.TRACE, MODULE, message);
 	}
 }
