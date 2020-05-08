 /* cPlugin
  * Copyright (C) 2013 Norbert Kawinski (norbert.kawinski@gmail.com)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package castro.base.plugin;
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Map;
 
 
 public class CUtils
 {
 	public static int getCurrentTimeInServerTicks()
 	{
 		// Some info:
 		// 0:00AM = 18000
 		// +24h = +24k ticks = 42000
 		Calendar cal	= Calendar.getInstance();
 		float hours		= cal.get(Calendar.HOUR_OF_DAY);
 		float minutes	= cal.get(Calendar.MINUTE) + hours*60;
 		float seconds	= cal.get(Calendar.SECOND) + minutes*60;
 		
 		float secondsInDay = 60*60*24;
 		float timeOfDay = seconds / secondsInDay;
 		
 		// -6000=midnight; 6000=midday; 18000 = midnight
 		// We have to forward time 6000
 		float timeOffset = 18000;
 		float currentTimeInTicks = (24000.f*timeOfDay) + timeOffset;
 		return (int)currentTimeInTicks;
 	}
 	
 	
 	public static String joinArgs(String[] array)
 	{ return joinArgs(array, 0); }
 	public static String joinArgs(String[] array, int start)
 	{
 		String ret = "";
 		if(array.length == 0)//<= start)
 			return "";
 		for(int i=start; i < array.length; ++i)
 			ret += array[i] + " ";
 		return ret.substring(0, ret.length()-1);
 	}
 	
 	
 	
 	private static long tn = 0;
 	// Reset counter for timeStep
 	public static void reset()
 	{
 		tn = System.nanoTime();
 	}
 	// Reads diff between this and last method calls
 	public static void timeStep(String msg)
 	{
 		long now = System.nanoTime();
 		float diff = now-tn;
 		CPlugin.baseinstance.log("DEBUG " + (diff/1000000.f) + "ms " + msg, false);
 	}
 	
 	
 	// Returns nearest lower value from map in comparison to searchedKey. defaultValue if not found.
 	public static <K extends Comparable<K>, V> V getNearestLowerValue(Map<K, V> map, final K searchedKey, final V defaultValue)
 	{
 		K key = getNearestLowerKey(map, searchedKey);
 		if(key != null)
 			return map.get(key);
 		return defaultValue;
 	}
 	
 	
 	// Returns nearest lower key from map in comparison to searchedKey.
 	public static <K extends Comparable<K>> K getNearestLowerKey(Map<K, ?> map, K searchedKey)
 	{
 		K maxKey = null;
 		Collection<K> keys = map.keySet();
 		for(K key : keys)
			if(searchedKey.compareTo(key) >= 0)
     			if(maxKey == null) // If not set yet
     				maxKey = key;
    			else if(key.compareTo(maxKey) >= 0)
     				maxKey = key;
 		return maxKey;
 	}
 	
 	
 	
 	public static <V> V get(Map<?, V> map, Object key, V defaultValue)
 	{
 		V value = map.get(key);
 		if(value == null)
 			return defaultValue;
 		return value;
 	}
 }
