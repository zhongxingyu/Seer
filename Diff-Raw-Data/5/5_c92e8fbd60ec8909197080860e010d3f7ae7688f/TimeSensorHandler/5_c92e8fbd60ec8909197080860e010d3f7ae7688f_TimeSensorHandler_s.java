 /*
  *  Copyright (C) 2012 Henry Sun
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
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package hsun324.cpsensors.sensors.handlers;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import hsun324.cpsensors.sensors.ISensorHandler;
 import hsun324.cpsensors.tile.TileBlockSensor;
 
 public class TimeSensorHandler implements ISensorHandler
 {
 	public TimeSensorHandler() { }
 	
 	@Override
 	public Map<String, Object> getData(TileBlockSensor caller)
 	{
 		Map<String, Object> dataMap = new HashMap<String, Object>();
 
		long worldTime = caller.worldObj.getTotalWorldTime();
 		
		dataMap.put("mcTotalTick", worldTime);
 		dataMap.put("mcMinute", (int)((worldTime % 1000) / 1000d * 60));
 		dataMap.put("mcHour", (int)((worldTime % 24000) / 1000d));
 		dataMap.put("mcDay", (int)((worldTime % 8760000) / 8760000d * 365));
 		dataMap.put("mcYear", (int)(worldTime / 8760000d));
 		
 		Calendar realTime = Calendar.getInstance();
 		dataMap.put("realTotalSeconds", realTime.getTimeInMillis() / 2000);
 		dataMap.put("realSecond", realTime.get(Calendar.SECOND));
 		dataMap.put("realMinute", realTime.get(Calendar.MINUTE));
 		dataMap.put("realHour", realTime.get(Calendar.HOUR));
 		dataMap.put("realIsPM", realTime.get(Calendar.AM_PM) == Calendar.PM);
 		dataMap.put("realGregorianDay", realTime.get(Calendar.DAY_OF_MONTH));
 		dataMap.put("realGregorianMonthNum", realTime.get(Calendar.MONTH) + 1);
 		dataMap.put("realGregorianMonth", realTime.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
 		dataMap.put("realDay", realTime.get(Calendar.DAY_OF_YEAR));
 		dataMap.put("realYear", realTime.get(Calendar.YEAR));
 		
 		return dataMap;
 	}
 }
