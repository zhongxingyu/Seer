 /*
  * Copyright(C) 2011+ Woody NaDobhar
  */	
 
 package com.azuriteWeb.amtApp.Library;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 
 public class UpdateLibrary extends Activity{
 
 	//vars
 	private ProgressDialog progressDialog;
 	static final int ID_DIALOG_UPDATE = 0;
 	static ArrayList<HashMap<String, String>> onlineFileList = new ArrayList<HashMap<String, String>>();
 	int numFiles = 0;
 	int numComplete = 0;
 	int numFailed = 0;
 	
 	//methods adapter
 	ApplicationMethods AM;
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState){
 		try{
 			dismissDialog(ID_DIALOG_UPDATE);
 		}catch(Exception e){
 			Log.e("Error", "Exception: "+e);
 		}
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 
 		//the usual suspects
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.update_library);
 		
 		//app methods
 		AM = new ApplicationMethods(this);
 		
 		//if we have someplace to put it, get to it
 		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
 			
 			//progress bar
 			showDialog(ID_DIALOG_UPDATE);
 
 			//get our list of files
 			String xml = XMLfunctions.getXML(getString(R.string.library_listing_addy));
 			Document doc = XMLfunctions.XMLfromString(xml);
 
 			//our files
 			/*TODO: make this a little more distinguishing than 'all the files' */ 
 			NodeList nl = doc.getElementsByTagName("file");
 			 
 			// looping through all item nodes
 			for(int i = 0; i < nl.getLength(); i++){
 				Element e = (Element) nl.item(i);
 				String file = XMLfunctions.getValue(e, "name");
  				//this is the file to be downloaded
  				try {
  					
  					//update our number of files
  					numFiles++;
  					
  					//the URL to the file
  					URL url = new URL(getString(R.string.library_addy)+file);
 
  					//do the work
  					new getFile("", file).execute(url);
  				}catch(MalformedURLException e2){
  					Log.e("Bad URL Error:", ""+e2);
  					numFiles--;
  					e2.printStackTrace();
  				}
 			}
 			
 			
 //			NodeList fileNodes =(NodeList) doc.getElementsByTagName("file");
 //			Node child;
 //			if(fileNodes.item(0) != null){
 //				if (fileNodes.item(0).hasChildNodes()){
 //					for(child = fileNodes.item(0).getFirstChild(); child != null; child = child.getNextSibling()){
 //						if(child.getNodeType() == Node.TEXT_NODE){
 //			 				//this is the file to be downloaded
 //			 				try {
 //			 					
 //			 					//update our number of files
 //			 					numFiles++;
 //			 					
 //			 					//the URL to the file
 //			 					URL url = new URL(getString(R.string.library_addy)+child.getNodeValue());
 //
 //			 					//do the work
 //			 					new getFile("",child.getNodeValue()).execute(url);
 //			 				}catch(MalformedURLException e2){
 //			 					Log.e("Bad URL Error:", ""+e2);
 //			 					numFiles--;
 //			 					e2.printStackTrace();
 //			 				}
 //						}
 //					}
 //				}
 //			}
 		}
 	}
 	
 	private class getFile extends AsyncTask<URL, CharSequence, Boolean>{
 
 		URL taskLink;
 		private String fileName;
 		private String fileDirectory;
 
 		public getFile(String fileDirectory, String fileName){
 			super();
 			this.fileDirectory = fileDirectory;
 			this.fileName = fileName;
 		}
 		
 		@Override
 		protected void onPreExecute(){
 			//progress bar stuff
 		}
 
 		@Override
 		protected Boolean doInBackground(URL... url){
 			try{
 				
 				//set these for later
 				taskLink = url[0];
 				
 				//let them know it's begun
 				publishProgress(getString(R.string.fetching_file)+fileName);
 	
 				//create the new connection
 				HttpURLConnection urlConnection = (HttpURLConnection) url[0].openConnection();
 	
 				//set up some things on the connection
 				urlConnection.setRequestMethod("GET");
 				urlConnection.setDoOutput(true);
 	
 				//and connect!
 				urlConnection.connect();
 	
 				//data root
 				File dataRoot = new File(Environment.getExternalStorageDirectory().toString()+getString(R.string.sd_files_directory)+getString(R.string.files)+fileDirectory+"/");
 				
 				//make the directories, if needed
 				dataRoot.mkdirs();
 	
 				//this will be used in reading the data from the internet
 				InputStream inputStream = urlConnection.getInputStream();
 	
 				//this is the total size of the file
 				int totalSize = urlConnection.getContentLength();
 				
 				//check to see if we've already got this one
 				//local file
 				File fileToCheck = new File(Environment.getExternalStorageDirectory().toString()+getString(R.string.sd_files_directory)+getString(R.string.files)+fileDirectory+"/"+fileName);
 				
 				if(fileToCheck.isFile()){
					int fileSize = (int) fileToCheck.length();
 					//if so, and the size isn't the same, nuke it
					if(fileSize != totalSize){
 						fileToCheck.delete();
 					}else{
 						//already got this one, move along
 						return true;
 					}
 				}
 	
 				//create a new file, specifying the path, and the filePath
 				//which we want to save the file as.
 				File destination = new File(Environment.getExternalStorageDirectory().toString()+getString(R.string.sd_files_directory)+getString(R.string.files)+fileDirectory+"/",fileName);
 	
 				//this will be used to write the downloaded data into the file we created
 				FileOutputStream fileOutput = new FileOutputStream(destination);
 				
 				//variable to store total downloaded bytes
 				int downloadedSize = 0;
 	
 				//create a buffer...
 				byte[] buffer = new byte[1024];
 				int bufferLength = 0;
 	
 				//now, read through the input buffer and write the contents to the file
 				while((bufferLength = inputStream.read(buffer)) > 0){
 					//add the data in the buffer to the file in the file output stream (the file on the sd card
 					fileOutput.write(buffer, 0, bufferLength);
 					//add up the size so we know how much is downloaded
 					downloadedSize += bufferLength;
 				}
 				
 				//let 'em know it's done
 				publishProgress(getString(R.string.finish_fetch_file)+fileName);
 				publishProgress("incriment");
 				
 				//close the output stream when done
 				fileOutput.close();
 	
 			//catch some possible errors...
 			}catch(MalformedURLException e){
 				Log.e("Error:", ""+e);
 				e.printStackTrace();
 				return false;
 			}catch(IOException e){
 				Log.e("Error:", ""+e);
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}
 		
 		protected void onProgressUpdate(CharSequence... message){
 			if(message[0] == "incriment"){
 				progressDialog.incrementProgressBy(100/numFiles);
 				Log.i("Progress","+"+(100/numFiles));
 			}else{
 				progressDialog.setMessage(message[0]);
 			}
 		 }
 
 		@Override
 		protected void onPostExecute(Boolean isComplete){
 			Log.i("File Complete?", isComplete.toString());
 			//if it failed, try again
 			if(isComplete == false){
 				//but only three times
 				if(numFailed > 3){
 					numComplete++;
 					Log.i("NumFiles/Complete", numFiles+"/"+numComplete);
 					if(numFiles == numComplete){
 						progressDialog.setProgress(100);
 						progressDialog.dismiss();
 						//all done, go to the Library
 						finish();
 					}
 				}else{
 					publishProgress(getString(R.string.fetching_file)+fileName+getString(R.string.failed));
 					numFailed++;
 					new getFile(fileDirectory,fileName).execute(taskLink);
 				}
 			}else{
 				numComplete++;
 				Log.i("NumFiles/Complete", numFiles+"/"+numComplete);
 				if(numFiles == numComplete){
 					progressDialog.setProgress(100);
 					progressDialog.dismiss();
 					//all done, go to the Library
 					finish();
 				}
 			}
 		}
 	}
    
 	protected Dialog onCreateDialog(int id){
 		switch(id){
 		case ID_DIALOG_UPDATE:
 			progressDialog = new ProgressDialog(UpdateLibrary.this);
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressDialog.setTitle(getString(R.string.update_library));
 			progressDialog.setMessage(getString(R.string.update_library_msg));
 			progressDialog.setCancelable(false);
 			return progressDialog;
 		default:
 			return null;
 		}
 	}
 }
