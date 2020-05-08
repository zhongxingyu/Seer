 /*
  * Copyright (C) 2011-2013 Kuropen.
  * 
  * This file is part of the Electricity Warning Crawler, Version 3.
  * 
  * The Electricity Warning Crawler is free software:
  * you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * The Electricity Warning Crawler is distributed in the hope that
  * it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with The Electricity Warning Crawler.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.kuropen.elecwarnv3;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.Vector;
 
 import org.kuropen.elecwarnv3.util.TwitterUtilv3;
 
 import co.akabe.common.electricusage.FiveMinDemand;
 import co.akabe.common.electricusage.PeakSupply;
 import co.akabe.elecwarn.ElectricityUsageData;
 
 public class WebSendAction implements GetInfoListener {
 
 	private String host;
 	private TwitterUtilv3 tu;
 	private static final boolean TESTFLAG = false;
 	public WebSendAction(String host, TwitterUtilv3 twUtil) {
 		this.host = host;
 		tu = twUtil;
 	}
 
 	@Override
 	public void onInfoGet(String key, FiveMinDemand demand, PeakSupply supply) {
 		int usage = demand.getDemandToday();
 		int capacity = supply.getAmount();
 
 		// 最終更新日時を取得する
 		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("JST"));
 		try {
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
 			String dateText = demand.getDate();
 			Date date = sdf.parse(dateText);
 			cal.setTime(date);
 			cal.set(Calendar.HOUR_OF_DAY, demand.getHour());
 			cal.set(Calendar.MINUTE, demand.getMinute());
 
 			String query = "http://" + host + "/dataregist?company=" + key
 					+ "&consume=" + usage + "&capacity=" + capacity
 					+ "&recorddate=" + getTime(cal);
 			
 			System.out.println(query);
 			
 			Vector<String> result = readFromURL(query);
 			for(String line : result) {
 				System.out.println(line);
 			}
 
 			ElectricityUsageData ud = new ElectricityUsageData(key, demand.getDemandToday(), supply.getAmount(), cal);
			String twMsg = ud.toString() + " http://" + host + "/" + key + "?year=" + cal.get(Calendar.YEAR) + "&month=" + cal.get(Calendar.MONTH) + "&date=" + cal.get(Calendar.DATE);
 			System.out.println(twMsg);
 			if(ud.getPercentage() >= 90 || TESTFLAG) {
 				tu.sendTweet(twMsg, TESTFLAG);
 			}
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private long getTime(Calendar cal) {
 		return cal.getTime().getTime() / 1000;
 	}
 
 	private static Vector<String> readFromURL(String addr) throws IOException {
 		System.out.println(addr);
 		URL url = new URL(addr);
 		URLConnection connection = url.openConnection();
 		connection.setDoInput(true);
 		InputStream inStream = connection.getInputStream();
 
 		try {
 			BufferedReader input = new BufferedReader(new InputStreamReader(
 					inStream));
 
 			String line = "";
 			Vector<String> ret = new Vector<String>();
 			while ((line = input.readLine()) != null)
 				ret.add(line);
 			return ret;
 		} finally {
 			inStream.close();
 		}
 	}
 
 }
