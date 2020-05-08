 /* OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2010, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.openremote.android.console.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.openremote.android.console.Constants;
 import org.openremote.android.console.model.AppSettingsModel;
 import org.openremote.android.console.net.ORConnection;
 import org.openremote.android.console.net.ORConnectionDelegate;
 import org.openremote.android.console.net.ORHttpMethod;
 
 import android.content.Context;
 import android.text.TextUtils;
 import android.util.Log;
 
 /**
  * It's responsible for downloading resources in backgroud and updte progress in text.
  * 
  * @author handy 2010-05-10
  * @author Dan Cong
  *
  */
 public class AsyncGroupLoader implements ORConnectionDelegate {
     private static final String LOG_CATEGORY = Constants.LOG_CATEGORY + AsyncGroupLoader.class.getName();
 	private ORConnection connection;
     private Context context;
 	
     public static void loadGroup(Context context) {
 		String server = AppSettingsModel.getSecuredServer(context);
 		if(!TextUtils.isEmpty(server)) {
 			new AsyncGroupLoader(context, server);
 		}
     }
     
 	private AsyncGroupLoader(Context context, String server) {
 		connection = new ORConnection(context, 
 							ORHttpMethod.GET, 
 							false, 
 							server + "/rest/device/group", 
 							this);
 		this.context = context;
 	}
 
 	@Override
 	public void urlConnectionDidFailWithException(Exception e) {
 		Log.e(LOG_CATEGORY + " urlConnectionDidFailWithException", e.getMessage());
 	}
 
 	@Override
 	public void urlConnectionDidReceiveResponse(HttpResponse httpResponse) {
		if(httpResponse.getStatusLine().getStatusCode() != 200) {
 			Log.e(LOG_CATEGORY + " urlConnectionDidFailWithException", 
 					httpResponse.getStatusLine().getStatusCode() + ", reason:" 
 							+ httpResponse.getStatusLine().getStatusCode());
 		}
 	}
 
 	@Override
 	public void urlConnectionDidReceiveData(InputStream data) {
 		String group = "", tmp;
 		BufferedReader in = new BufferedReader(new InputStreamReader(data));
 		try {
 			while((tmp = in.readLine()) != null) {
 				group += tmp;
 			}
 		} catch (IOException e) {
 			Log.e(LOG_CATEGORY + " urlConnectionDidReceiveData:", e.getMessage());
 		}	
 		
 		AppSettingsModel.setGroup(context, group);
 	}
    
 }
