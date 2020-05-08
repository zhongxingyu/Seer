 /*
  *  This File is licensed under GPL v3.
  *  Copyright (C) 2012 Rene Peinthor.
  *
  *  This file is part of TrafficChecker.
  *
  *  BlueMouse is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  TrafficChecker is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with TrafficChecker.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.oldsch00l.TrafficChecker;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Handler;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.oldsch00l.TrafficChecker.Message.SubType;
 import com.oldsch00l.TrafficChecker.TrafficChecker.Country;
 
 public class TrafficParser extends Thread {
 	private static HashMap<String, CacheTrafficEntries> cacheEntries = new HashMap<String, CacheTrafficEntries>();
 	public static final long cacheDiff = 180000;
 	private String mStringRegionList;
 	private String mOrderBy;
 	private int mLastHours;
 	private java.util.List<Message> mResultList;
 	private ArrayList<Message.SubType> mSubTypeFilterList;
 	private Context mContext;
 	private Handler mNotifier;
 
 	public TrafficParser(Context context, Handler handle) {
 		mContext = context;
 		mNotifier = handle;
 	}
 
 	public synchronized void setRegionList(final String sRegionList) {
 		mStringRegionList = sRegionList;
 	}
 
 	public synchronized java.util.List<Message> getResultList() {
 		return mResultList;
 	}
 
 	public void setLastHourFilter(final int hours) {
 		mLastHours = hours;
 	}
 
 	protected java.util.List<Message> getTrafficNews( String regionList) {
 		java.util.List<Message> retList = new ArrayList<Message>();
 
 		for (String region : regionList.split(",")) {
 			retList.addAll( getTrafficNewsRegion( TrafficChecker.RegionMap.get( region)));
 		}
 
 		return retList;
 	}
 
 	private void sortNews(java.util.List<Message> list) {
 		LocationManager lm = (LocationManager) mContext
 				.getSystemService(Context.LOCATION_SERVICE);
 		Criteria crit = new Criteria();
 		crit.setAccuracy(Criteria.ACCURACY_FINE);
 		String provider = lm.getBestProvider(crit, true);
 		if(provider != null && mOrderBy.equals("location")) {
 			final Location loc = lm.getLastKnownLocation(provider);
 			Collections.sort(list, new Comparator<Message>() {
 				public int compare(Message mA, Message mB) {
 					GeoPoint gpA;
 					GeoPoint gpB;
 
 					if (mA.getGeoDataList() == null
 							&& mB.getGeoDataList() == null) {
 						return 0;
 					}
 
					if (mA.getGeoDataList() != null)
 						gpA = mA.getGeoDataList().get(0);
 					else
 						return -1;
 
					if (mB.getGeoDataList() != null)
 						gpB = mB.getGeoDataList().get(0);
 					else
 						return 1;
 
 					if (loc != null) {
 						float[] res = new float[2];
 						Location.distanceBetween(gpA.getLatitudeE6() / 1E6,
 								gpA.getLongitudeE6() / 1E6,
 								loc.getLatitude(), loc.getLongitude(), res);
 						Float distA = Float.valueOf(res[0]);
 
 						Location.distanceBetween(gpB.getLatitudeE6() / 1E6,
 								gpB.getLongitudeE6() / 1E6,
 								loc.getLatitude(), loc.getLongitude(), res);
 						Float distB = Float.valueOf(res[0]);
 
 						return distA.compareTo(distB);
 					}
 					return mA.compareTo(mB);
 				}
 			});
 		} else {
 			// sort by date
 			Collections.sort( list);
 		}
 	}
 
 	protected java.util.List<Message> getTrafficNewsRegion( TrafficRegion region) {
 		if( region == null )
 			return new java.util.ArrayList<Message>();
 		String url = region.getCountry().getUrl();
 		Log.d("TrafficParser", url + "/" + region.getRegionUrlAppend());
 
 		if( (region.getCountry() == Country.Austria || region.getCountry() == Country.England) && !region.getRegionUrlAppend().equals("austria") )
 			url += "/" + region.getRegionUrlAppend();
 
 		BaseFeedParser saxparser = null;
 		switch (region.getCountry()) {
 		case England:
 		case Austria:
 			saxparser = new AndroidSaxRSSParser( url);
 			break;
 
 		default:
 			saxparser = new AndroidSaxFeedParser(url);
 			break;
 		}
 
 		String state = region.getCountry() == Country.Germany ? "germany" : region.getRegionUrlAppend();
 		if( !cacheEntries.containsKey( state) )
 		{
 			CacheTrafficEntries cte = new CacheTrafficEntries();
 			cte.messages = callParse(saxparser);
 			cte.cachedTime = System.currentTimeMillis();
 			cacheEntries.put( state, cte);
 			//updateContentProvider(context, cr, cacheEntries.get(state).messages, state);
 		}
 		else
 		{
 			if( (System.currentTimeMillis() - cacheEntries.get( state).cachedTime) > cacheDiff)
 			{
 				cacheEntries.get(state).messages = callParse(saxparser);
 				cacheEntries.get(state).cachedTime = System.currentTimeMillis();
 			}
 		}
 		sortNews(cacheEntries.get(state).messages);
 		List<Message> returnList = cacheEntries.get(state).messages;
 //		Log.w("update", "set update");
 //		TrafficProvider.setSetting(cr, TrafficProvider.SET_REGION, state);
 //		TrafficProvider.setSetting(cr, TrafficProvider.SET_UPDATE_NEEDED, "1");
 
 		// prefilter german entries as it is filtered by the geo point
 		if( region.getCountry() == Country.Germany )
 		{
 			ArrayList<Message> gerFilterList = new ArrayList<Message>();
 			for( Message msg : cacheEntries.get(state).messages) {
 				if( msg.getGeoDataList() != null )
 				{
 					if(GermanyRegions.pointInPolygon( msg.getGeoDataList().get(0), region.getAreaPolygon()) )
 					{
 						gerFilterList.add(msg);
 					}
 				}
 				else if( msg.getType() == Message.Type.HEADER)
 					gerFilterList.add(msg);
 			}
 			returnList = gerFilterList;
 		}
 
 		if( returnList.size() > 0 )
 		{
 			if( returnList.get(0).getType() != Message.Type.HEADER )
 				returnList.add(0, new Message(TrafficChecker.getRegionString(mContext, region.getRegionUrlAppend() ) ) );
 		}
 		else
 		{
 			returnList.add(0, new Message(TrafficChecker.getRegionString(mContext, region.getRegionUrlAppend() ) ) );
 		}
 
 		return returnList;
 	}
 
 	private java.util.List<Message> callParse(final BaseFeedParser saxparser) {
 		java.util.List<Message> list = null;
 		try {
 			list = saxparser.parse();
 		} catch ( RuntimeException e) {
 			Log.e("trafficchecker", "parse error", e);
 			list = new ArrayList<Message>();
 			Message emptyMsg = new Message();
 			emptyMsg.setTitle( mContext.getString(R.string.NoConnection) );
 			list.add(emptyMsg);
 		}
 		return list;
 	}
 
 	protected List<Message> filterLastHours(java.util.List<Message> listToFilter) {
 		if( mLastHours > 0) {
 			GregorianCalendar gcUntil = new GregorianCalendar();
 			gcUntil.add(GregorianCalendar.HOUR, mLastHours * -1);
 			List<Message> filteredList = new ArrayList<Message>();
 			for (Message message : listToFilter) {
 				if( message.getDate().after(gcUntil.getTime())) {
 					filteredList.add(message);
 				}
 			}
 
 			return filteredList;
 		}
 		return listToFilter;
 	}
 
 	protected List<Message> filterListBySubType(java.util.List<Message> listToFilter, java.util.List<Message.SubType> filterList) {
 		List<Message> filteredList = new ArrayList<Message>();
 		for (Message message : listToFilter) {
 			if( filterList.contains( message.getSubtype() ) )
 			{
 				filteredList.add( message);
 			}
 		}
 		return filteredList;
 	}
 
 	public void clearCache() {
 		cacheEntries = new HashMap<String, CacheTrafficEntries>();
 	}
 
 	public void setFilter( boolean bTraffic, boolean bRoadWorks) {
 		mSubTypeFilterList = new ArrayList<Message.SubType>();
 		mSubTypeFilterList.add(SubType.UNDEFINED);
 		if( bTraffic )
 		{
 			mSubTypeFilterList.add(SubType.ROADCONDITION);
 			mSubTypeFilterList.add(SubType.TRAFFIC);
 		}
 		if( bRoadWorks )
 			mSubTypeFilterList.add(SubType.ROADWORKS);
 	}
 
 	public void setOrderBy( String sOrder) {
 		mOrderBy = sOrder;
 	}
 
 	public void run() {
 		List<Message> result = getTrafficNews(mStringRegionList);
 		result = filterLastHours(result);
 		mResultList = filterListBySubType(result, mSubTypeFilterList);
 		mNotifier.sendEmptyMessage(0);
 	}
 }
