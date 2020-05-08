 /*
  *
  *  Copyright (C) 2011 GSyC/LibreSoft, Universidad Rey Juan Carlos.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see http://www.gnu.org/licenses/. 
  *
  *  Author : Raúl Román López <rroman@gsyc.es>
  *
  */
 package com.libresoft.apps.ARviewer.Utils.AsyncTasks;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.AsyncTask;
import android.util.Log;
 
 import com.libresoft.apps.ARviewer.R;
 import com.libresoft.apps.ARviewer.Location.ARLocationManager;
 import com.libresoft.apps.ARviewerPlaces.LibreGeoSocial;
 import com.libresoft.sdk.ARviewer.Types.GenericLayer;
 import com.libresoft.sdk.ARviewer.Types.GeoNode;
 
 public class AsyncLGSNodes extends AsyncTask<Void, Void, Void>{
 
 	private GenericLayer mLayer = null;
 	private OnExecutionFinishedListener onExecutionFinishedListener = null;
 	private Context mContext;
 	
 	public AsyncLGSNodes(Context mContext, GenericLayer mLayer, OnExecutionFinishedListener onExecutionFinishedListener){
 		this.mLayer = mLayer;
 		this.onExecutionFinishedListener = onExecutionFinishedListener;
 		this.mContext = mContext;
 	}
 
 	protected void onPreExecute(){
 		LibreGeoSocial.getInstance().setUrl(mContext.getString(R.string.urlServer));
 		LibreGeoSocial.getInstance().setFormat("JSON");
 	}
 	
 	@Override
 	protected Void doInBackground(Void... unused){
		try{
 		ArrayList<GeoNode> mNodeList = mLayer.getNodes();
 		if(mNodeList != null)
 			mNodeList.clear();
 		ArrayList<GenericLayer> mLayers = LibreGeoSocial.getInstance().getLayerList();
         
         for (int i=0; i<mLayers.size(); i ++)
         	mNodeList.addAll (LibreGeoSocial.getInstance().getLayerNodes(mLayers.get(i).getId(), 
         			"", 
         			"0", 
         			ARLocationManager.getInstance(mContext).getLocation().getLatitude(), 
         			ARLocationManager.getInstance(mContext).getLocation().getLongitude(), 
         			10.0, 
         			0, 
         			5));	     
		}catch(Exception e){
			Log.e("AsyncLGSNodes", "", e);
		}
		
         
 		return null;
 	}
 
 	protected void onPostExecute(Void unused) {
 		if(onExecutionFinishedListener != null)
 			onExecutionFinishedListener.onFinish();
 	}    
 	
 	
 	public interface OnExecutionFinishedListener {
 		public abstract void onFinish();
 	}
 }
