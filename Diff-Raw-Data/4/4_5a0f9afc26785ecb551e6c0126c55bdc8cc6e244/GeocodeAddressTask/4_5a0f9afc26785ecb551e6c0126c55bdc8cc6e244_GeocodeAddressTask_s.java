 package it.giacomos.android.osmer.locationUtils;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Build;
 import it.giacomos.android.osmer.locationUtils.LocationInfo;
 
 public class GeocodeAddressTask extends AsyncTask<Location, Integer, LocationInfo>
 {
 	public GeocodeAddressTask(Context ctx, GeocodeAddressUpdateListener listener)
 	{
 		mContext = ctx;
 		mUpdateListener = listener;
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public final AsyncTask<Location, Integer, LocationInfo> parallelExecute (Location... location)
 	{
 		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			return super.executeOnExecutor(THREAD_POOL_EXECUTOR, location);
 		}
 		else
 		{
 			return super.execute(location);
 		}
 	}
 	
 	protected LocationInfo doInBackground(Location... location)
 	{
 		/* get new address and locality only if lat and long are different from those 
 		 * in previous location.
 		 */
 			LocationInfo locationInfo = new LocationInfo();
 			if(location[0] != null)
 			{
 				Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
 				try{
 					locationInfo.provider = location[0].getProvider();
 					locationInfo.accuracy = location[0].getAccuracy();
 					List<Address> listAddresses = geocoder.getFromLocation(location[0].getLatitude(), location[0].getLongitude(), 1);
					if(listAddresses != null && listAddresses.get(0) != null)
 					{
 						locationInfo.address = "";
 						locationInfo.locality = "";
 						for(int i = 0; i < listAddresses.get(0).getMaxAddressLineIndex() - 1; i++)
 							locationInfo.address += listAddresses.get(0).getAddressLine(0) + "\n";
 
 						locationInfo.locality = listAddresses.get(0).getLocality();
 						locationInfo.subLocality = listAddresses.get(0).getSubLocality();
 						
 						if(locationInfo.locality == null)
 							locationInfo.locality = "";
 						if(locationInfo.subLocality == null)
 							locationInfo.subLocality = "";
 					}
 				}
 				catch (IOException e) 
 				{
 					locationInfo.locality = "-";
 					locationInfo.error = e.getLocalizedMessage();
 				}
 			}
 		
 		return locationInfo;
 	}
 	
 	protected void onPostExecute(LocationInfo result)
 	{
 		mUpdateListener.onGeocodeAddressUpdate(result);
 	}
 	
 	private Context mContext;
 	private GeocodeAddressUpdateListener mUpdateListener;
 }
