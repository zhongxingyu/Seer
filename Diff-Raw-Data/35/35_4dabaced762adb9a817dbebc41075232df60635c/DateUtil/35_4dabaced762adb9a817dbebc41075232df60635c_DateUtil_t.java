 /*
  * Copyright 2010 NCHOVY
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logstorage.engine;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
import java.util.TimeZone;
 
 public class DateUtil {
 	private DateUtil() {
 	}
 
 	public static String getDayText(Date day) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		return dateFormat.format(day);
 	}
 
	private static ThreadLocal<TimeZone> timeZoneCache = new ThreadLocal<TimeZone>() {
        @Override
        protected TimeZone initialValue() {
            return TimeZone.getDefault();
        }
    };
 
    public static Date getDay(Date date) {
        long time = date.getTime();
        TimeZone timeZone = timeZoneCache.get();
        if (timeZone.inDaylightTime(date))
            return new Date(time - ((time + timeZone.getRawOffset() + 3600000) % 86400000L));
        else
            return new Date(time - ((time + timeZone.getRawOffset()) % 86400000L));
    }
    
 	public static List<Date> filt(Collection<Date> dates, Date from, Date to) {
 		List<Date> filtered = new ArrayList<Date>();
 		// canonicalize
 		Date fromDay = null;
 		Date toDay = null;
 
 		if (from != null)
 			fromDay = getDay(from);
 		if (to != null)
 			toDay = getDay(to);
 
 		for (Date day : dates) {
 			if (fromDay != null && day.before(fromDay))
 				continue;
 
 			if (toDay != null && day.after(toDay))
 				continue;
 
 			filtered.add(day);
 		}
 
 		return filtered;
 	}
 
 	public static List<Date> sortByDesc(List<Date> dates) {
 		Collections.sort(dates, new Comparator<Date>() {
 			@Override
 			public int compare(Date o1, Date o2) {
 				return (int) (o2.getTime() - o1.getTime());
 			}
 		});
 
 		return dates;
 	}
 
 }
