 /**
  *     Copyright SocialSite (C) 2009
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.socialsite.util;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public class DateUtils
 {
 	public static String relativeTime(Date date)
 	{
 		final int SECOND = 1;
 		final int MINUTE = 60 * SECOND;
 		final int HOUR = 60 * MINUTE;
 		final int DAY = 24 * HOUR;
 		final int MONTH = 30 * DAY;
 
		long delta = (new Date().getTime() - date.getTime());

 
 		Calendar ts = Calendar.getInstance();
		ts.setTime(new Date(delta));

		delta = delta / 1000;
 
 		if (delta < 1 * MINUTE)
 		{
 			return ts.get(Calendar.SECOND) == 1 ? "one second ago" : ts.get(Calendar.SECOND)
 					+ " seconds ago";
 		}
 		if (delta < 2 * MINUTE)
 		{
 			return "a minute ago";
 		}
 		if (delta < 45 * MINUTE)
 		{
 			return ts.get(Calendar.MINUTE) + " minutes ago";
 		}
 		if (delta < 90 * MINUTE)
 		{
 			return "an hour ago";
 		}
 		if (delta < 24 * HOUR)
 		{
 			return ts.get(Calendar.HOUR) + " hours ago";
 		}
 		if (delta < 48 * HOUR)
 		{
 			return "yesterday";
 		}
 		if (delta < 30 * DAY)
 		{
 			return ts.get(Calendar.DAY_OF_MONTH) + " days ago";
 		}
 		if (delta < 12 * MONTH)
 		{
 			int months = (int)Math.floor((double)ts.get(Calendar.DAY_OF_YEAR) / 30);
 			return months <= 1 ? "one month ago" : months + " months ago";
 		}
 		else
 		{
 			int years = (int)Math.floor((double)ts.get(Calendar.DAY_OF_YEAR) / 365);
 			return years <= 1 ? "one year ago" : years + " years ago";
 		}
 	}
 
 }
