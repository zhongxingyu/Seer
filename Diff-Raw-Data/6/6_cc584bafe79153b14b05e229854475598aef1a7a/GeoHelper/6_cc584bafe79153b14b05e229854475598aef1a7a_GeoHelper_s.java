 package se.lu.nateko.edca.svc;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Scanner;
 import java.util.Set;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import se.lu.nateko.edca.BackboneSvc;
 import se.lu.nateko.edca.LayerViewer;
 import se.lu.nateko.edca.R;
 import se.lu.nateko.edca.Utilities;
 import se.lu.nateko.edca.svc.GeographyLayer.LayerField;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.provider.Settings.Secure;
 import android.util.Log;
 
 import com.google.android.gms.maps.model.LatLng;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.io.ParseException;
 import com.vividsolutions.jts.io.WKTReader;
 
 /********************************COPYRIGHT***********************************
  * This file is part of the Emergency Data Collector for Android™ (EDCA).	*
  * Copyright © 2013 Mattias Spångmyr.										*
  * 																			*
  *********************************LICENSE************************************
  * EDCA is free software: you can redistribute it and/or modify it under	*
  * the terms of the GNU General Public License as published by the Free		*
  * Software Foundation, either version 3 of the License, or (at your		*
  * option) any later version.												*
  *																			*
  * EDCA is distributed in the hope that it will be useful, but WITHOUT ANY	*
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or		*
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License	*
  * for more details.														*
  *																			*
  * You should have received a copy of the GNU General Public License along	*
  * with EDCA. If not, see "http://www.gnu.org/licenses/".					*
  * 																			*
  * The latest source for this software can be accessed at					*
  * "github.org/mattiassp/edca".												*
  * 																			*
  * EDCA also utilizes the JTS Topology Suite, Version 1.8 by Vivid			*
  * Solutions Inc. It is released under the Lesser General Public License	*
  * ("http://www.gnu.org/licenses/") and its source can be accessed at the	*
  * JTS Topology Suite website ("http://www.vividsolutions.com/jts").		*
  * 																			*
  * Android™ is a trademark of Google Inc. The Android source is released	*
  * under the Apache License 2.0												*
  * ("http://www.apache.org/licenses/LICENSE-2.0") and can be accessed at	*
  * "http://source.android.com/".											*
  * 																			*
  * For other enquiries, e-mail to: edca.contact@gmail.com					*
  * 																			*
  ****************************************************************************
  * Helper class for writing a GeographyLayer's data to the external			*
  * storage, and reading and interpreting such stored files into one. Also	*
  * handles uploading of a layer's data to its corresponding server layer.	*
  * 																			*
  * @author Mattias Spångmyr													*
 * @version 0.51, 2013-07-25												*
  * 																			*
  ****************************************************************************/
 public class GeoHelper extends AsyncTask<GeographyLayer, Void, GeoHelper> {
 	/** The error tag for this ASyncTask. */
 	public static final String TAG = "GeoHelper";
 	/** Constant defining the wait time (seconds) before an upload operation times out. */
 	private static final int TIME_OUT = 30;
 	
 	/** The mode to run, being either of "read", "write" or "overwrite". */
 	private int mRwMode = 0;
 	/** Constant identifying the run mode "read" (from external storage). */
 	public static final int RWMODE_READ = 0;
 	/** Constant identifying the run mode "write" (to external storage, without overwriting). */
 	public static final int RWMODE_WRITE = 1;
 	/** Constant identifying the run mode "overwrite" (to external storage). */
 	public static final int RWMODE_OVERWRITE = 2;
 	/** Constant identifying the run mode "upload" (to server). */
 	public static final int RWMODE_UPLOAD = 3;
 	
 	/** System class used to keep track of external storage states, e.g. if it is connected or not, and events. */
 	private BroadcastReceiver mExternalStorageReceiver;
 	/** Whether or not the external storage is available. */
 	private boolean mExternalStorageAvailable = false;
 	/** Whether or not it is possible to write to the external storage. */
 	private boolean mExternalStorageWriteable = false;
 	
 	/** A reference to the application's background Service, received in the constructor. */
 	private BackboneSvc mService;
 	/** The HttpClient to use for uploading the data. */
 	private HttpClient mHttpClient;
 	/** The active GeographyLayer from which to upload data. */
 	private GeographyLayer mGeoLayer;
 	/** The id of the layer to activate in the local SQLite database in case of running the "read" mode. */
 	private Long mRowId;
 	/** The new ID:s on the server of the features uploaded. */
 	private ArrayList<Long> mInsertedIDs;
 	/** Whether or not the helper has succeeded with the requested operation. */
 	private boolean mSuccess = false;
 	/** Whether or not a file conflict has occurred; a file with the same name as that being saved already exists. */
 	private boolean mFileConflict = false;
 	/** The XML namespace to use, the same as the name of the workspace on the geospatial server. */
 	private String mNamespace;
 	/** Constant defining what spatial reference system (srs) to use for sending geometry to the geospatial server. */
 	private static final String SRS = "srsName=\"http://www.opengis.net/gml/srs/epsg.xml#4326\"";
 		
 	/**
 	 * Basic constructor setting up the upload/write mode and the BackboneSvc reference.
 	 * @param service The database manager that issued the request and through which the UI thread can be reached.
 	 * @param rwMode The mode to run, being either of "read", "write" or "overwrite".
 	 */
 	public GeoHelper(BackboneSvc service, int rwMode) {
 //		Log.d(TAG, "GeoHelper(BackboneSvc, int) called.");
 		mService = service;
 		mRwMode = rwMode;
 	}
 	
 	/**
 	 * Read mode constructor setting up the read mode, the id of the layer to activate and the BackboneSvc reference.
 	 * @param service The database manager that issued the request and through which the UI thread can be reached.
 	 * @param rwMode The mode to run, being either of "read", "write" or "overwrite".
 	 * @param rowId The id of the layer to activate in the local SQL database.
 	 */
 	public GeoHelper(BackboneSvc service, int rwMode, long rowId) {
 //		Log.d(TAG, "GeoHelper(BackboneSvc, int, long) called.");
 		mService = service;
 		mRwMode = rwMode;
 		mRowId = rowId;
 	}
 
 	protected GeoHelper doInBackground(GeographyLayer... layers) {
 		Log.d(TAG, "doInBackground(GeographyLayer...) called.");
 		mGeoLayer = layers[0]; // Store the GeographyLayer reference.
 		
 		if(mRwMode != RWMODE_UPLOAD)
 			startWatchingExternalStorage(); // Start to listen for changes in the storage state, upon which the operations may have to cancel.		
 		
 		/* Perform the task unless the storage is not available. */
 		switch(mRwMode) {
     		case RWMODE_READ: {
     			if(!mExternalStorageAvailable)
     				mService.showAlertDialog(mService.getString(R.string.service_sdunavailable), null);
     			else
     				mSuccess = readIntoGeographyLayer();
     			break;
     		}
     		case RWMODE_WRITE: {
     			if(!mExternalStorageWriteable)
     				mService.showAlertDialog(mService.getString(R.string.service_sdunavailable), null);
     			else
     				mSuccess = writeFromGeographyLayer(false);
     			break;    			
     		}
     		case RWMODE_OVERWRITE: {
     			if(!mExternalStorageWriteable)
     				mService.showAlertDialog(mService.getString(R.string.service_sdunavailable), null);
     			else
     				mSuccess = writeFromGeographyLayer(true);
     			break;
     		}
     		case RWMODE_UPLOAD: {
     			try	{ // Get or wait for exclusive access to the HttpClient.
     				mHttpClient = mService.getHttpClient();
     				mService.startAnimation(); // Start the animation, showing that a web communicating thread is active.
     			} catch(InterruptedException e) { Log.e(TAG, "Thread " + Thread.currentThread().getId() + ": " + e.toString()); }
     			
     			mSuccess = upload();
     			mService.unlockHttpClient(); // Release the lock on the HttpClient, allowing new connections to be made.
     			break;
     		}
     		default:
     			Log.e(TAG, "Invalid read/write mode.");
     	}
 		
 		return this;
 	}
 
 	@Override
 	protected void onProgressUpdate(Void... values) {
 		Log.d(TAG, "onProgressUpdate(Void...) called.");
 		// No progress update.
 		super.onProgressUpdate(values);
 	}
 
 	@Override
 	protected void onPostExecute(GeoHelper result) {
 		Log.d(TAG, "onPostExecute(GeoHelper) called.");
 		if(mRwMode != RWMODE_UPLOAD)
 			stopWatchingExternalStorage(); // No more need to watch for storage state changes.
 		
 		if(mSuccess) {
 			if(mRwMode == RWMODE_READ) {
 				mService.deactivateLayers();
 				Cursor modeCursor = mService.getSQLhelper().fetchData(LocalSQLDBhelper.TABLE_LAYER, LocalSQLDBhelper.KEY_LAYER_COLUMNS, mRowId, null, false);
 				mService.getActiveActivity().startManagingCursor(modeCursor);
 				if(modeCursor.moveToFirst()) {
 					mService.getSQLhelper().updateData(LocalSQLDBhelper.TABLE_LAYER, modeCursor.getLong(0), LocalSQLDBhelper.KEY_LAYER_ID, new String[]{LocalSQLDBhelper.KEY_LAYER_USEMODE}, new String[]{String.valueOf(modeCursor.getLong(2) * LocalSQLDBhelper.LAYER_MODE_ACTIVE)}); // Add the "Active" mode to the current mode.
 					mService.updateLayoutOnState();
 				}
 				mService.showToast(R.string.layerviewer_uploadtoast_readsuccess);
 			}
 			else if(mRwMode == RWMODE_UPLOAD) {
 				mService.stopAnimation(); // Stop the animation, showing that a web communicating thread is no longer active.
 				mService.showToast(R.string.layerviewer_uploadtoast_uploadsuccess);
 			}
 			else { // Successful write operation.
 				Log.i(TAG, "Write operation successful.");
 				Cursor modeCursor = mService.getSQLhelper().fetchData(LocalSQLDBhelper.TABLE_LAYER, LocalSQLDBhelper.KEY_LAYER_COLUMNS, LocalSQLDBhelper.ALL_RECORDS, LocalSQLDBhelper.KEY_LAYER_NAME + " = " + "\"" + mGeoLayer.getName() + "\"", false);
 				mService.getActiveActivity().startManagingCursor(modeCursor);
 				if(modeCursor.moveToFirst()) {
 					mService.getSQLhelper().updateData(LocalSQLDBhelper.TABLE_LAYER, modeCursor.getLong(0), LocalSQLDBhelper.KEY_LAYER_ID, new String[]{LocalSQLDBhelper.KEY_LAYER_USEMODE}, new String[]{String.valueOf(modeCursor.getLong(2) * LocalSQLDBhelper.LAYER_MODE_STORE)}); // Add the "Stored" mode to the current mode.
 					mService.updateLayoutOnState();
 					mService.showToast(R.string.layerviewer_uploadtoast_writesuccess);
 				} else {Log.e(TAG, "No such layer.");}
 			}
 		}
 		else {
 			if(mRwMode == RWMODE_READ)
 				mService.showToast(R.string.layerviewer_uploadtoast_readfail);
 			else if(mRwMode == RWMODE_WRITE && mFileConflict) {
 				/* If write failed because of a file conflict, ask to overwrite the data. */
 				new AlertDialog.Builder(mService.getActiveActivity())
 		        	.setIcon(android.R.drawable.ic_dialog_alert)
 		        	.setTitle(R.string.layerviewer_overwritealert_title)
 		        	.setMessage(R.string.layerviewer_overwritealert_msg)
 		        	.setPositiveButton(R.string.layerviewer_overwritealert_confirm, new DialogInterface.OnClickListener()
 		        	{
 		        		public void onClick(DialogInterface dialog, int which) {
 		        			mService.makeSaveOperation(GeoHelper.RWMODE_OVERWRITE);
 		        			dialog.dismiss();    
 		        		}
 		        	})
 		        	.setNegativeButton(R.string.layerviewer_overwritealert_decline, null)
 		        	.show();
 			}
 			else if(mRwMode == RWMODE_UPLOAD) {
 				/* If an upload failed, ask to store the data locally. */
 				mService.stopAnimation(); // Stop the animation, showing that a web communicating thread is no longer active.
 				mService.clearConnection(false); // Report the failed connection.
 				
 				new AlertDialog.Builder(mService.getActiveActivity())
 	        	.setIcon(android.R.drawable.ic_menu_upload)
 	        	.setTitle(R.string.layerviewer_uploaddialog_title)
 	        	.setMessage(R.string.layerviewer_uploaddialog_msg)
 	        	.setPositiveButton(R.string.layerviewer_uploaddialog_confirm, new DialogInterface.OnClickListener()
 	        	{
 	        		public void onClick(DialogInterface dialog, int which) {
 	        			mService.makeSaveOperation(GeoHelper.RWMODE_WRITE);
 	        			dialog.dismiss();    
 	        		}
 	        	})
 	        	.setNegativeButton(R.string.layerviewer_uploaddialog_decline, null)
 	        	.show();
 			}
 			else
 				mService.showToast(R.string.layerviewer_uploadtoast_writefail);
 		}
 		
 		/* Reactivate the upload button and set the flag in the BackboneSvc. */
 		mService.setUploading(false);
 		if(mService.getActiveActivity().getLocalClassName().equalsIgnoreCase("LayerViewer"))
 			((LayerViewer) mService.getActiveActivity()).setLayout_EnableUploadButton(true);
 		
 		super.onPostExecute(result);
 	}
 	
 	/**
 	 * Reads the geometry and attribute data from the local storage
 	 * into the active GeographyLayer.
 	 */
 	private boolean readIntoGeographyLayer() {
 		Log.d(TAG, "readIntoGeographyLayer() called.");
 		File layerPath = new File(getPath());
 		
 		File geomFile = new File(layerPath, "geometry.txt");
 		File attFile = new File(layerPath, "attributes.txt");
 		try {
 			if(!geomFile.exists()) // If there is no geometry file.
 				Log.i(TAG, "No geometry stored.");
 			else {
 				/* Read the geometry with ID and geometry WKT. */
 			    Scanner scanner = new Scanner(new FileInputStream(geomFile));
 			    
 			    try {
 			    	while(scanner.hasNextLine()) {
 			    		String[] line = scanner.nextLine().split("[ ]{1}", 2);
 			    		WKTReader reader = new WKTReader();
 			    		switch(mGeoLayer.getTypeMode()) {
 			        		case GeographyLayer.TYPE_POINT: {
 			        			Coordinate p = ((Point) reader.read(line[1])).getCoordinate();
 			        			mGeoLayer.addGeometry(new LatLng(p.y, p.x), Long.parseLong(line[0]));
 			        			break;
 			        		}
 			        		case GeographyLayer.TYPE_LINE: {
 			        			Geometry geom = reader.read(line[1]);
 			        			Log.v(TAG, "Geom type: " + geom.getGeometryType());
 			        			if(geom.getGeometryType().equalsIgnoreCase("LineString"))
 			        				mGeoLayer.addLine((LineString) geom, Long.parseLong(line[0]), true);
 			        			else if(geom.getGeometryType().equalsIgnoreCase("Point")) {
 			        				Coordinate p = ((Point) geom).getCoordinate();
 			        				mGeoLayer.addGeometry(new LatLng(p.y, p.x));
 			        			}
 			        			break;
 			        		}
 			        		case GeographyLayer.TYPE_POLYGON: {
 			        			Geometry geom = reader.read(line[1]);
 			        			Log.v(TAG, "Geom type: " + geom.getGeometryType());
 			        			if(geom.getGeometryType().equalsIgnoreCase("Polygon"))
 			        				mGeoLayer.addPolygon((Polygon) reader.read(line[1]), Long.parseLong(line[0]), true);
 			        			else if(geom.getGeometryType().equalsIgnoreCase("Point")) {
 			        				Coordinate p = ((Point) geom).getCoordinate();
 			        				mGeoLayer.addGeometry(new LatLng(p.y, p.x));
 			        			}
 			        			break;
 			        		}
 			    		}
 			    	}
 			    } catch (ParseException e) {
 			    	Log.e(TAG, e.toString());
 			    	return false;
 			    } finally {
 			      scanner.close();
 			    }
 			}
 			if(!attFile.exists()) // If there is no attribute file.
 				Log.i(TAG, "No attributes stored.");
 			else {
 				/* Read the attributes with ID and field info. */
 				Scanner scanner = new Scanner(new FileInputStream(attFile));
 				try {
 					while(scanner.hasNextLine()) {
 				        String[] line = scanner.nextLine().split("[;]{1}",-1);
 				        for(int i=0; i < mGeoLayer.getNonGeomFields().size(); i++)
 				        	mGeoLayer.addAttribute(Long.parseLong(line[0]), mGeoLayer.getNonGeomFields().get(i).getName(), line[i+1]);
 				    }
 				} finally {
 					scanner.close();
 				}
 			}
 		} catch (IOException e) {
 		    Log.e(TAG, e.toString());
 		    return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Write the geography of the layer to files on the external
 	 * storage.
 	 * @param overwrite Determines whether or not to overwrite existing data. If false and there are already stored data on this layer, the method will have no effect.
 	 * @return True if successful.
 	 */
 	private boolean writeFromGeographyLayer(boolean overwrite) {
 		Log.d(TAG, "writeFromGeographyLayer(" + String.valueOf(overwrite) + ") called.");
 		
 		File layerPath = new File(getPath());
 		layerPath.mkdirs();
 		File geomFile = new File(layerPath, "geometry.txt");
 		File attFile = new File(layerPath, "attributes.txt");
 		
 		if(overwrite || (!geomFile.exists() && !attFile.exists())) { // If there are no files with the given names, or the mode is set to overwrite.
 			try {
 				/* Store the points with ID and geometry WKT. */
 			    FileWriter outGeom = new FileWriter(geomFile);
 			    FileWriter outAtt = new FileWriter(attFile);
 			    if(mGeoLayer.getTypeMode() == GeographyLayer.TYPE_POINT)
 			    	outGeom.write(formGeomString());
 			    else {
 			    	try {
 			    		String sequenceGeom = formSequenceGeomString();
 			    		if(!sequenceGeom.equalsIgnoreCase(""))
 			    			sequenceGeom = sequenceGeom + System.getProperty("line.separator");
 			    		outGeom.write(sequenceGeom + formUnusedPointString());
 			    	} catch (Exception e) {
 			    		Log.e(TAG, e.toString());
 			    		outGeom.close();
 			    		outAtt.close();
 			    		return false;
 			    	}
 			    }
 			    outGeom.close();
 			    outAtt.write(formAttString());
 			    outAtt.close();
 			    return true;
 			}
 			catch (IOException e) {
 			    Log.e(TAG, e.toString());
 			    return false;
 			}
 		}
 		else {
 			Log.w(TAG, "File already exists. Use overwrite mode?");
 			mFileConflict = true;
 			return false;			
 		}
 	}
 	
 	/**
 	 * Removes the Layer's data from the external storage.
 	 * @param layerName The name of the layer to delete.
 	 */
 	public static void deleteGeographyLayer(String layerName) {
 //		Log.d(TAG, "deleteGeographyLayer(layerName=" + layerName + ") called.");
 		File layerPath = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + BackboneSvc.PACKAGE_NAME + "/files/" + Utilities.dropColons(layerName, Utilities.RETURN_LAST));
 		Utilities.deleteRecursive(layerPath);
 	}
 	
 	/**
 	 * Checks if the layer with the specified name is stored
 	 * on the external storage.
 	 * @return True of the layer is stored on the device.
 	 */
 	public static boolean isStored(String layerName) {
 		Log.d(TAG, "isStored(String) called.");
 		String path = new String(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + BackboneSvc.PACKAGE_NAME + "/files/" + Utilities.dropColons(layerName, Utilities.RETURN_LAST));
 		return (new File(path)).exists();
 	}
 	
 	/**
 	 * Uploads the active layer's data to the geospatial server
 	 * and deletes the data from the device.
 	 * @return True if successful.
 	 */
 	private boolean upload() {
 		Log.d(TAG, "upload() called.");
 		/* Try to form an URI from the supplied ServerConnection info. */
 		if(mService.getActiveServer() == null) // Cannot connect unless there is an active connection.
 			return false;
 		String uriString = "http://" + mService.getActiveServer().toString() + "/wfs";
 		
 		/* Post all geometry from the active layer to that layer on the geospatial server. */
 		HttpResponse response;
 		StringEntity se;
 		boolean responseSuccessful = false;
 		StringBuilder stringTotal = new StringBuilder();
 		try {
 			final HttpParams httpParameters = mHttpClient.getParams();
 			HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT * 1000);
 			HttpConnectionParams.setSoTimeout        (httpParameters, TIME_OUT * 1000);
 
 			se = new StringEntity(formTransactXML());
 			se.setContentType("text/xml");
 			HttpPost postRequest = new HttpPost(uriString);
 			postRequest.setEntity(se);
 			
 			response = mHttpClient.execute(postRequest);
 			InputStream xmlStream = response.getEntity().getContent();
 			
 			BufferedReader r = new BufferedReader(new InputStreamReader(xmlStream));
 			String line;
 			while ((line = r.readLine()) != null)
 			    stringTotal.append(line);
 			
 			InputStream is = new ByteArrayInputStream(stringTotal.toString().getBytes("UTF-8"));
 			responseSuccessful = parseXMLResponse(is);
 			
 		} catch(UnsupportedEncodingException e) {
 			Log.e(TAG, e.toString());
 			return false;
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, e.toString());
 			return false;
 		} catch (IOException e) {
 			Log.e(TAG, e.toString());
 			return false;
 		}
 		Log.i(TAG, "Insert Response: " + stringTotal.toString());
 		
 		try { // Consume the HttpEntity.
 			se.consumeContent();
 		} catch (UnsupportedOperationException e) {
 			Log.e(TAG, "Operation unsupported by streaming entity sub-class: " + e.toString());
 		} catch (IOException e) {
 			Log.e(TAG, "Entity consumed?: " + e.toString());
 			e.printStackTrace();
 		}
 		
 		if(responseSuccessful) {
 			/* Remove all uploaded geometry from the active layer and the local storage. */
 			mGeoLayer.clearGeometry();
 			
 			deleteGeographyLayer(mGeoLayer.getName());		
 		
 			/* Update the layer table to reflect that the layer is no longer stored on the device. */
 			Cursor layerCursor = mService.getSQLhelper().fetchData(LocalSQLDBhelper.TABLE_LAYER, LocalSQLDBhelper.KEY_LAYER_COLUMNS, LocalSQLDBhelper.ALL_RECORDS, new String(LocalSQLDBhelper.KEY_LAYER_NAME + " = \"" + Utilities.dropColons(mGeoLayer.getName(), Utilities.RETURN_LAST) +"\""), false);
 			mService.getActiveActivity().startManagingCursor(layerCursor);
 			if(layerCursor.moveToFirst()) {
 				long layerid = layerCursor.getInt(0);
 				int layerMode = layerCursor.getInt(2);
 				if(layerMode % LocalSQLDBhelper.LAYER_MODE_STORE == 0) // If the layer is currently stored, remove that mode.
 					mService.getSQLhelper().updateData(LocalSQLDBhelper.TABLE_LAYER, layerid, LocalSQLDBhelper.KEY_LAYER_ID, new String[]{LocalSQLDBhelper.KEY_LAYER_USEMODE}, new String[]{String.valueOf(layerMode / LocalSQLDBhelper.LAYER_MODE_STORE)});
 			}
 			return true;
 		} else return false;
 		
 	}
 	
 	/**
 	 * Start to listen for changes in the storage state,
 	 * and report any changes to handleStorageChange.
 	 */
 	private void startWatchingExternalStorage() {
 		Log.d(TAG, "startWatchingExternalStorage() called.");
 	    mExternalStorageReceiver = new BroadcastReceiver() {
 	        @Override
 	        public void onReceive(Context context, Intent intent) {
 	            Log.i("TAG", "Storage state changed: " + intent.getData());
 	            updateExternalStorageState();
 	            handleStorageChange();
 	        }
 	    };
 	    IntentFilter filter = new IntentFilter();
 	    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
 	    filter.addAction(Intent.ACTION_MEDIA_REMOVED);
 	    mService.registerReceiver(mExternalStorageReceiver, filter);
 	    updateExternalStorageState();
 	}
 
 	/**
 	 * Stop listening for external storage state changes.
 	 */
 	private void stopWatchingExternalStorage() {
 		Log.d(TAG, "stopWatchingExternalStorage() called.");
 	    mService.unregisterReceiver(mExternalStorageReceiver);
 	}
 	
 	/**
 	 * Checks the state of the external storage to determine
 	 * what interactions can be performed.
 	 */
 	private void updateExternalStorageState() {
 //		Log.d(TAG, "updateExternalStorageState() called.");
 		String state = Environment.getExternalStorageState();
 
 		/* Check the availability of the external storage. */
 		if (Environment.MEDIA_MOUNTED.equals(state)) { // We can read and write the media.		    
 		    mExternalStorageAvailable = mExternalStorageWriteable = true;
 		    
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // We can only read the media.
 		    mExternalStorageAvailable = true;
 		    mExternalStorageWriteable = false;
 		    
 		} else // We can neither read nor write.		    
 		    mExternalStorageAvailable = mExternalStorageWriteable = false;
 	}
 	
 	/**
 	 * Pauses or resumes this AsyncTask if the external storage
 	 * becomes unavailable or is available again.
 	 */
 	synchronized private void handleStorageChange() {
 //		Log.d(TAG, "handleStorageChange() called.");
 		/* The external storage has become unavailable, pause the thread. */
 		if(!mExternalStorageAvailable) {
 			Log.w(TAG, "External storage made unavailable. Thread put on hold.");
 			try {
 				this.wait();
 			} catch (InterruptedException e) {
 				Log.e(TAG, e.toString());
 				e.printStackTrace();
 			}
 		}
 		/* The external storage cannot be written to and the mode requires this, pause the thread. */
 		else if(!mExternalStorageWriteable && (mRwMode == RWMODE_WRITE || mRwMode == RWMODE_OVERWRITE)) {
 			Log.w(TAG, "External storage made unavailable. Thread put on hold.");
 			try {
 				this.wait();
 			} catch (InterruptedException e) {
 				Log.e(TAG, e.toString());
 				e.printStackTrace();
 			}
 		}
 		/* The external storage can now be written to, resume the thread. */
 		else if(mExternalStorageWriteable) {
 			this.notify();
 			Log.w(TAG, "External storage made available. Thread resumed.");
 		}
 		/* The external storage has become available and the mode only requires reading, resume the thread. */
 		else if(mExternalStorageAvailable && mRwMode == RWMODE_READ) {
 			this.notify();
 			Log.w(TAG, "External storage made available. Thread resumed.");
 		}		
 	}
 	
 	/**
 	 * Get method for path to the geometry on the external storage.
 	 * The path does not include layer namespace.
 	 * @return The layer path.
 	 */
 	public String getPath() {
 //		Log.d(TAG, "getPath() called.");
 		return new String(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + BackboneSvc.PACKAGE_NAME + "/files/" + Utilities.dropColons(mGeoLayer.getName(), Utilities.RETURN_LAST));
 	}
 	
 	/**
 	 * Forms a single string containing all the point geometry objects in the GeographyLayer.
 	 * @return A string with the point geometry id and WKT on each row.
 	 */
 	private String formGeomString() {
 		Log.d(TAG, "formGeomString() called.");
 		if(!mGeoLayer.hasGeometry()) // Return empty string if there is no geometry to form a string from.
 			return new String("");
 		
 		Set<Long> keys;
 		String result = "";
 		
 		keys = mGeoLayer.getGeometry().keySet();
 			
 		Collection<LatLng> values = mGeoLayer.getGeometry().values();
 		Iterator<LatLng> i = values.iterator();
 					
 		for(Long id : keys) {
 			LatLng loc = i.next();
 			Point p = mService.getGeometryFactory().createPoint(new Coordinate(loc.longitude, loc.latitude));
 			result = result + String.valueOf(id) + " " + String.valueOf(p.toText());
 			if(i.hasNext())
 				result = result + System.getProperty("line.separator");
 		}
 		return result;
 	}
 	
 	/**
 	 * Forms a single string containing all the line or polygon geometry objects in the GeographyLayer.
 	 * @return A string with the line or polygon id and WKT on each row.
 	 */
 	private String formSequenceGeomString() throws Exception {
 		Log.d(TAG, "formSequenceGeomString() called.");
 		if(!mGeoLayer.hasGeometry()) // Return empty string if there is no geometry to form a string from.
 			return new String("");
 		
 		Set<Long> keys;
 		String result = "";
 		
 		keys = mGeoLayer.getPointSequence().keySet();
 		Iterator<Long> it = keys.iterator();
 			
 		while(it.hasNext()) {
 			Long id = it.next();
 			ArrayList<Long> sequence = mGeoLayer.getPointSequence().get(id);
 				
 			Coordinate[] coordinates = new Coordinate[sequence.size()];
 			for(int i=0; i < sequence.size(); i++) {
 				LatLng loc = mGeoLayer.getGeometry().get(sequence.get(i));
 				coordinates[i] = new Coordinate(loc.longitude, loc.latitude);
 			}
 
 			Geometry sequenceGeom;
 			try {
 				if(mGeoLayer.getTypeMode() == GeographyLayer.TYPE_POLYGON)
 					sequenceGeom = mService.getGeometryFactory().createPolygon(mService.getGeometryFactory().createLinearRing(coordinates), null);
 				else
 					sequenceGeom = mService.getGeometryFactory().createLineString(coordinates);
 				
 				result = result + String.valueOf(id) + " " + String.valueOf(sequenceGeom.toText());
 			} catch (Exception e) {
 				Log.e(TAG, e.toString());
 				mService.showToast(R.string.mapviewer_combinesequence_invalid); // Notice the user of invalid point sequences.
 				Exception returnExc = new Exception("Could not create geometry.");
 				throw returnExc;
 			}				
 			if(it.hasNext())
 				result = result + System.getProperty("line.separator");
 		}
 		return result;
 	}
 	
 	/**
 	 * Forms a single string containing all the point geometry objects in the GeographyLayer
 	 * that are not included in a point sequence.
 	 * @return A string with unused points represented by their id and WKT on each row.
 	 */
 	private String formUnusedPointString() { 
 		Log.d(TAG, "formUnusedPointString() called.");
 		if(!mGeoLayer.hasGeometry()) // Return empty string if there is no geometry to form a string from.
 			return new String("");
 		
 		String result = "";
 		
 		/* Go through all points and add points that are not included in a point sequence. */
 		Set<Long> keys = mGeoLayer.getGeometry().keySet();
 		Iterator<Long> it = keys.iterator();
 		boolean first = true;
 		while(it.hasNext()) {
 			Long id = it.next();
 			if(mGeoLayer.pointInSequence(id) == -1) {// If the point is not in any point sequence:
 				LatLng loc = mGeoLayer.getGeometry().get(id);
 				Point p = mService.getGeometryFactory().createPoint(new Coordinate(loc.longitude, loc.latitude));
 				if(first) { // Don't include a line separator before the first line.
 					result = String.valueOf(id) + " " + p.toText();
 					first = false;
 				}
 				else
 					result = result + System.getProperty("line.separator") + String.valueOf(id) + " " + p.toText();
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Forms a single string containing all the attributes of the objects in
 	 * the GeographyLayer.
 	 * @return A string with the geometry id and attributes on each row, separated by semi-colons.
 	 */
 	private String formAttString() {
 		Log.d(TAG, "formAttString() called.");
 		if(mGeoLayer.getAttributes().size() < 1) // Return empty string if there are no attributes to form a string from.
 			return new String("");
 		
 		Set<Long> keys = mGeoLayer.getAttributes().keySet();
 		Collection<HashMap<String,String>> values = mGeoLayer.getAttributes().values();
 		Iterator<HashMap<String,String>> i = values.iterator();
 		
 		String result = "";
 		for(Long id : keys) {
 			result = result + String.valueOf(id);			
 			HashMap<String, String> map = i.next();
 			for(int j = 0; j < mGeoLayer.getNonGeomFields().size(); j++) {
 				result = result + ";" + map.get(mGeoLayer.getNonGeomFields().get(j).getName());
 			}
 			if(i.hasNext())
 				result = result + System.getProperty("line.separator");
 		}
 		return result;
 	}
 	
 	/**
 	 * Forms a single string containing an XML of a WFS Transaction Insert request.
 	 * @return A string with the WFS-T Insert XML. 
 	 */
 	private String formTransactXML() {
 		Log.d(TAG, "formTransactXML() called.");
 		String request = "";
 		if(!mGeoLayer.hasGeometry()) // Return empty string if there is no geometry to form a string from.
 			return request;
 		
 		/* Set the namespace for the insert statements. */
 		mNamespace = mService.getActiveServer().getWorkspace();
 		if(mNamespace == null)
 			mNamespace = Utilities.dropColons(mGeoLayer.getName(), Utilities.RETURN_FIRST);
 		else if(mNamespace.equalsIgnoreCase(""))
 			mNamespace = Utilities.dropColons(mGeoLayer.getName(), Utilities.RETURN_FIRST);
 		
 		/* Create a handle for the insert operation unique to this device. */
 		String android_id = Secure.getString(mService.getActiveActivity().getContentResolver(), Secure.ANDROID_ID);
 		String insertHandle = mService.getString(R.string.app_name_short);
 		if(android_id != null)
 			insertHandle = insertHandle + "." + android_id;
 		
 		request = request + "<wfs:Transaction service=\"WFS\" version=\"1.0.0\" handle=\"" + insertHandle + "\"" +
 				" xmlns:" + mNamespace + "=\"" + mNamespace + "\"" +
 				" xmlns:wfs=\"http://www.opengis.net/wfs\"" +
 				" xmlns:gml=\"http://www.opengis.net/gml\"" +
 				" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
 				" xsi:schemaLocation=\"" +
 				"http://" + mService.getActiveServer().toString() + "/DescribeFeatureType?typename=" + mGeoLayer.getName() +
 				" http://www.opengis.net/wfs\">";
 		
 		request = request + formInsertElements();
 				
 		request = request + " </wfs:Transaction>";
 		
 		Log.i(TAG, request);
 		return request;
 	}
 	
 	/**
 	 * Forms a single string containing the Insert XML elements of a WFS Transaction Insert request.
 	 * @return A string with the Insert elements of a WFS-T Insert XML. 
 	 */
 	private String formInsertElements() {
 //		Log.d(TAG, "formInsertElements() called.");
 
 		/* The parts of the Insert statements to form. */
 		String inserts = ""; // The total, combined, result.
 		String wfsStart = "";
 		String geomFieldStart = "";
 		String geomStart = "";
 		String coords = "";
 		String geomEnd = "";
 		String geomFieldEnd = "";
 		String atts = "";
 		String wfsEnd = "";
 
 		Set<Long> keyset = (mGeoLayer.getTypeMode() == GeographyLayer.TYPE_POINT) ? mGeoLayer.getGeometry().keySet() : mGeoLayer.getPointSequence().keySet(); // Fetch the set of keys pertaining to the relevant geometry type.
 		
 		for(Long featureId : keyset) {
 			/* Form the WFS Insert element starters and enders. */
 			wfsStart = " <wfs:Insert>" +
 				" <" + mNamespace + ":" + Utilities.dropColons(mGeoLayer.getName(), Utilities.RETURN_LAST) + ">";
 			wfsEnd = " </" + mNamespace + ":" + Utilities.dropColons(mGeoLayer.getName(), Utilities.RETURN_LAST) + ">" +
 				" </wfs:Insert>";
 			
 			/* Form the Geometry field element starter and ender. */
 			geomFieldStart = " <" + mNamespace + ":" + mGeoLayer.getGeomColumnKey() + ">";
 			geomFieldEnd = " </" + mNamespace + ":" + mGeoLayer.getGeomColumnKey() + ">";
 			
 			/* Form the element starters and enders required by the corresponding geometry type before the coordinates. */
 			if(mGeoLayer.getType().equalsIgnoreCase("gml:PointPropertyType")) {
 				geomStart = " <gml:Point " + SRS + ">";
 				geomEnd = " </gml:Point>";
 			}
 			else if(mGeoLayer.getType().equalsIgnoreCase("gml:MultiPointPropertyType")) {
 				geomStart = " <gml:MultiPoint " + SRS + ">" + " <gml:pointMember>" + " <gml:Point>";
 				geomEnd = " </gml:Point>" + " </gml:pointMember>" + " </gml:MultiPoint>";
 			}
 			else if(mGeoLayer.getType().equalsIgnoreCase("gml:LineStringPropertyType")) {
 				geomStart = " <gml:LineString " + SRS + ">";
 				geomEnd = " </gml:LineString>";
 			}
 			else if(mGeoLayer.getType().equalsIgnoreCase("gml:MultiLineStringPropertyType")) {
 				geomStart = " <gml:MultiLineString " + SRS + ">" + " <gml:lineStringMember>" + " <gml:LineString>";
 				geomEnd = " </gml:LineString>" + " </gml:lineStringMember>" + " </gml:MultiLineString>";
 			}
 			else if(mGeoLayer.getType().equalsIgnoreCase("gml:PolygonPropertyType") || mGeoLayer.getType().equalsIgnoreCase("gml:SurfacePropertyType")) {
 				geomStart = " <gml:Polygon " + SRS + ">" + " <gml:outerBoundaryIs>" + " <gml:LinearRing>";
 				geomEnd = " </gml:LinearRing>" + " </gml:outerBoundaryIs>" + " </gml:Polygon>";
 			}
 			else if(mGeoLayer.getType().equalsIgnoreCase("gml:MultiPolygonPropertyType") || mGeoLayer.getType().equalsIgnoreCase("gml:MultiSurfacePropertyType")) {
 				geomStart = " <gml:MultiPolygon " + SRS + ">" + " <gml:polygonMember>" + " <gml:Polygon>" + " <gml:outerBoundaryIs>" + " <gml:LinearRing>";
 				geomEnd = " </gml:LinearRing>" + " </gml:outerBoundaryIs>" + " </gml:Polygon>" + " </gml:polygonMember>" + " </gml:MultiPolygon>";
 			}
 			
 			/* Form the coordinates elements. */
 			if(mGeoLayer.getTypeMode() == GeographyLayer.TYPE_POINT) { // Form Point Coordinates GML.
 				
 				coords = " <gml:coordinates decimal=\".\" cs=\",\" ts=\" \">" +
 						String.valueOf(mGeoLayer.getGeometry().get(featureId).longitude) +
 						"," +
 						String.valueOf(mGeoLayer.getGeometry().get(featureId).latitude) +
 						"</gml:coordinates>";
 			}
 			else { // Form the sequence Coordinates GML.
 				coords = " <gml:coordinates decimal=\".\" cs=\",\" ts=\" \">";				
 				for(int i=0; i<mGeoLayer.getPointSequence().get(featureId).size(); i++)  { // For each point in this point sequence:
 					if(i != 0)
 						coords = coords + " "; // Insert spaces between coordinates.
 					coords = coords +
 							String.valueOf(mGeoLayer.getGeometry().get(mGeoLayer.getPointSequence().get(featureId).get(i)).longitude) +
 							"," +
 							String.valueOf(mGeoLayer.getGeometry().get(mGeoLayer.getPointSequence().get(featureId).get(i)).latitude);
 				}
 				coords = coords + "</gml:coordinates>";
 			}
 			
 			/* Form the non-geometry fields and attributes, but only those with input. */
 			try {
 				for(LayerField field : mGeoLayer.getNonGeomFields()) {
 					if(!mGeoLayer.getAttributes().get(featureId).get(field.getName()).equalsIgnoreCase("")) {
 						atts = atts + " <" + mNamespace + ":" + field.getName() + ">"
 								+ mGeoLayer.getAttributes().get(featureId).get(field.getName()) +
 								"</" + mNamespace + ":" + field.getName() + ">";
 					}
 				}
 			} catch (NullPointerException e) { Log.e(TAG, "Didn't add attributes: " + e.toString()); }
 			
 			/* Combine all parts into a complete WFS Insert statement. */
 			inserts = inserts + wfsStart + geomFieldStart + geomStart + coords + geomEnd + geomFieldEnd + atts + wfsEnd;
 		}
 		return inserts;
 	}
 	
 	/**
 	 * Parses an XML response from a WFS Transaction (Insert) request and
 	 * reports if the response contains a Service Exception.
 	 * @param xmlResponse String containing the XML response from a DescribeFeatureType request.
 	 */
 	protected boolean parseXMLResponse(InputStream xmlResponse) {
 		Log.d(TAG, "parseXMLResponse(InputStream) called.");
 		
 		mInsertedIDs = new ArrayList<Long>();
 		
 		try {
 			SAXParserFactory spfactory = SAXParserFactory.newInstance(); // Make a SAXParser factory.
 			spfactory.setValidating(false); // Tell the factory not to make validating parsers.
 			SAXParser saxParser = spfactory.newSAXParser(); // Use the factory to make a SAXParser.
 			XMLReader xmlReader = saxParser.getXMLReader(); // Get an XML reader from the parser, which will send event calls to its specified event handler.
 			XMLEventHandler xmlEventHandler = new XMLEventHandler();
 			xmlReader.setContentHandler(xmlEventHandler); // Set which event handler to use.
 			xmlReader.setErrorHandler(xmlEventHandler); // Also set where to send error calls.
 			InputSource source = new InputSource(xmlResponse); // Make an InputSource from the XML input to give to the reader.
 		    xmlReader.parse(source); // Start parsing the XML.
 		} catch (Exception e) {
 			Log.e(TAG, e.toString());
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Event handler class that handles calls from an XML reader.
 	 * @author Mattias Spångmyr
 	 * @version 0.01, 2012-09-21
 	 */
 	private class XMLEventHandler extends DefaultHandler {
 		/** The error tag for this XMLEventHandler. */
 		public static final String TAG = "GeoHelper.XMLEventHandler";
 		
 		/** Constant defining the name of a "service exception" element. */
 		private static final String ELEMENT_KEY_EXCEPTION = "ServiceException";
 		/** Constant defining the name of an "exception" element. */
 		private static final String ELEMENT_KEY_EXCEPTION_B = "Exception";
 		/** Constant defining the name of a "feature id" element. */
 		private static final String ELEMENT_KEY_ID = "FeatureId";
 		/** Constant defining the name of a "failed" element. */
 		private static final String ELEMENT_KEY_FAILED = "FAILED";
 		/** Constant defining the name of a "message" element. */
 		private static final String ELEMENT_KEY_MESSAGE = "Message";
 		
 		/** Constant identifying other elements than those with specific identifiers. */
 		private static final int ELEMENT_MAPPING_DEFAULT = 0;
 		/** Constant identifying a "service exception" element. */
 		private static final int ELEMENT_MAPPING_EXCEPTION = 1;
 		/** Constant identifying an "exception" element. */
 		private static final int ELEMENT_MAPPING_EXCEPTION_B = 2;
 		/** Constant identifying a "feature id" element. */
 		private static final int ELEMENT_MAPPING_ID = 3;
 		/** Constant identifying a "failed" element. */
 		private static final int ELEMENT_MAPPING_FAILED = 4;
 		/** Constant identifying a "message" element. */
 		private static final int ELEMENT_MAPPING_MESSAGE = 5;
 		
 		/** Flag showing the type of element currently being parsed. */
 		private int mElementType = 0;
 		/** Flag indicating if the WFS-T Insert operation failed, i.e. if a forbidden (exception) element has been parsed. */
 		public boolean mFailed = false;
 		
 		@Override
 		public void startDocument() throws SAXException {
 			Log.d(TAG, "startDocument() called: Examining Insert request response...");
 			super.startDocument();
 		}
 
 		@Override
 		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 //			Log.d(TAG, "startElement(String, String, String, Attributes) called.");
 			/* Record relevant element names as integer mappings. */
 			mElementType = (localName.equalsIgnoreCase(ELEMENT_KEY_EXCEPTION))	?	ELEMENT_MAPPING_EXCEPTION	:	// Map exception as 1.
 						(localName.equalsIgnoreCase(ELEMENT_KEY_EXCEPTION_B))	?	ELEMENT_MAPPING_EXCEPTION_B	:	// Map exception as 2.
 							(localName.equalsIgnoreCase(ELEMENT_KEY_ID))		?	ELEMENT_MAPPING_ID			:	// Map id as 3.
 							(localName.equalsIgnoreCase(ELEMENT_KEY_FAILED))	?	ELEMENT_MAPPING_FAILED		:	// Map failed as 4.
 							(localName.equalsIgnoreCase(ELEMENT_KEY_MESSAGE))	?	ELEMENT_MAPPING_MESSAGE		:	// Map message as 5.
 																					ELEMENT_MAPPING_DEFAULT;		// Map any other as 0.
 			
 			switch(mElementType) {
 				case ELEMENT_MAPPING_EXCEPTION: {
 					mFailed = true;
 					Log.e(TAG, "ServiceException element found. Insert failed.");
 					break;
 				}
 				case ELEMENT_MAPPING_EXCEPTION_B: {
 					mFailed = true;
 					Log.e(TAG, "Exception element found. Insert failed.");
 					break;
 				}
 				case ELEMENT_MAPPING_FAILED: {
 					Log.e(TAG, "Failed element found. Insert failed.");
 					mFailed = true;
 					break;
 				}
 				case ELEMENT_MAPPING_ID: {
 					try {
 						String fid = attributes.getValue("", "fid");
 						String[] stringArray = fid.toString().split("[.]"); // Splits the input into sections.
 						mInsertedIDs.add(Long.parseLong(stringArray[stringArray.length-1])); // Stores the last number in the ID array.
 						
 					} catch (NullPointerException e) { Log.e(TAG, "No such element attribute."); }					
 					break;
 				}
 				default:
 					break;
 			}			
 			super.startElement(uri, localName, qName, attributes);
 		}
 
 		@Override
 		public void characters(char[] ch, int start, int length) throws SAXException {
 //			Log.d(TAG, "characters(char[], int, int) called.");
 			switch(mElementType) {
 				case ELEMENT_MAPPING_EXCEPTION: {
 					throw new SAXException("ServiceException: " + new String(Utilities.copyOfRange_char(ch, start, length)));
 				}
 				case ELEMENT_MAPPING_MESSAGE: {
 					if(mFailed)
 						throw new SAXException("ServiceException: " + new String(Utilities.copyOfRange_char(ch, start, length)));
 					else
 						Log.i(TAG, "Message: " + new String(Utilities.copyOfRange_char(ch, start, length)));
 				}
 			}
 			super.characters(ch, start, length);
 		}
 
 		@Override
 		public void endElement(String uri, String localName, String qName) throws SAXException {
 //			Log.d(TAG, "endElement(String, String, String) called.");
 			// Nothing to add.
 			super.endElement(uri, localName, qName);
 		}		
 
 		@Override
 		public void endDocument() throws SAXException {
 //			Log.d(TAG, "endDocument() called: Finished examining Insert request response...");
 			super.endDocument();
 		}		
 		
 		@Override
 		public void warning(SAXParseException e) throws SAXException {
 			Log.w(TAG, "warning(SAXParseException) called: " + e.toString());
 			super.warning(e);
 		}
 
 		@Override
 		public void error(SAXParseException e) throws SAXException {
 			Log.e(TAG, "error(SAXParseException) called: " + e.toString());
 			super.error(e);
 		}
 
 		@Override
 		public void fatalError(SAXParseException e) throws SAXException {
 			Log.e(TAG, "fatalError(SAXParseException) called: " + e.toString());
 			super.fatalError(e);
 		}
 	}
 }
