 package org.marcus.weather;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class DataFetcher {
 
 	private static class LineReader {
 		private BufferedReader reader;
 		private String line;
 		private PrintWriter out;
 
 		LineReader(Reader streamReader, PrintWriter out) throws IOException {
 			this.reader = new BufferedReader(streamReader);
 			this.out = out;
 			readLine();
 		}
 
 		private void readLine() throws IOException {
 			this.line = reader.readLine();
 			if (out != null) {
 				out.println(line);
 			}
 		}
 
 		public String getStuff() {
 			int end = line.indexOf("</");
 			if (end < 0) {
 				return "";
 			}
 			String s = line.substring(0, end);
 			int pos = s.lastIndexOf(">");
 			return s.substring(pos + 1);
 		}
 
 		public void skipTo(String find) throws IOException {
 			while (!line.contains(find)) {
 				readLine();
 				if (out != null) out.println(line);
 			}
 		}
 
 		public Float getFloatStuff() {
 			String stuff = getStuff();
 			try {
 				return Float.valueOf(stuff);
 			} catch (NumberFormatException e) {
 
 			}
 			return null;
 		}
 
 		public String getStuffToWhite() {
 			String s = this.getStuff();
 			return s.split(" ")[0];
 		}
 
 		public Integer getIntStuff() {
 			String stuff = getStuff();
 			try {
 				return Integer.valueOf(stuff);
 			} catch (NumberFormatException e) {
 
 			}
 			return null;
 		}
 
 	}
 
 	int dayNumber1;
 	int dayNumber3;
 	public String zip;
 	public String date;
 	public String time;
 	public ForecastData forecast1;
 	public ForecastData forecast3;
 	public PastData past;
 	public boolean valid = false;
 
 	// temporary variables for getting the data
 	private HourlyPast[] hp = new HourlyPast[24];
 	private OverallPast op;
 	private HourlyForecast[] hf1;
 	private OverallForecast of1;
 	private HourlyForecast[] hf3;
 	private OverallForecast of3;
 	private String date1;
 	private String date3;
 	private boolean debug = false;
 
 	private static final int DAY_IN_MILLIS = 3600 * 24 * 1000;
 
 	public DataFetcher() throws Exception {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		this.date = format.format(cal.getTime());
 		cal.setTimeInMillis(cal.getTimeInMillis() + DAY_IN_MILLIS * 1);
		dayNumber1 = cal.get(Calendar.DAY_OF_YEAR)-1;
 		this.date1 = format.format(cal.getTime());
 		cal.setTimeInMillis(cal.getTimeInMillis() + DAY_IN_MILLIS * 2);
		dayNumber3 = cal.get(Calendar.DAY_OF_YEAR)-1;
 		this.date3 = format.format(cal.getTime());
 		cal = Calendar.getInstance();
 		this.time = new SimpleDateFormat("kk:mm:ss").format(cal.getTime());
 	}
 
 	public DataFetcher(boolean debug) throws Exception {
 		this();
 		this.debug = debug;
 	}
 
 	public void load(String zipCode) throws IOException, InterruptedException,
 			Exception {
 		zip = zipCode;
 		wundergroundFuture();
 		wundergroundPast();
 
 		valid = true;
 	}
 
 	public void loadPast(String zipCode, Date date) throws Exception {
 		zip = zipCode;
 		wundergroundPast(date);
 
 		valid = true;
 	}
 
 	private void wundergroundFuture() throws IOException, InterruptedException {
 		if (debug)
 			System.out.println("1");
 		getOverallForecast();
 		if (debug)
 			System.out.println("2");
 		this.hf1 = getHourlyForecast(this.dayNumber1,
 				MakeURL.hourlyURL(this.zip, 1));
 		if (debug)
 			System.out.println("3");
 		this.hf3 = getHourlyForecast(this.dayNumber3,
 				MakeURL.hourlyURL(this.zip, 3));
 		if (debug)
 			System.out.println("4");
 
 		this.forecast1 = new ForecastData(this.zip, this.date1, this.of1,
 				this.hf1);
 		this.forecast3 = new ForecastData(this.zip, this.date3, this.of3,
 				this.hf3);
 	}
 
 	private HourlyForecast[] getHourlyForecast(int dayNumber, String surl)
 			throws IOException {
 		if (debug)
 			System.out.println("2.1");
 		HourlyForecast[] hf = null;
 		for (int tries = 0; tries < 7; tries++) {
 			File outFile = null;
 			FileWriter fileWriter = null;
 			PrintWriter out = null;
 			if (debug) {
 				SimpleDateFormat format = new SimpleDateFormat(
 						"yyyyMMdd'T'kkmmssSSS");
 
 				outFile = new File("test/test"
 						+ format.format(Calendar.getInstance().getTime())
 						+ "overall.html");
 				fileWriter = new FileWriter(outFile, false);
 				out = new PrintWriter(fileWriter, true);
 				out.println(MakeURL.overallURL(this.zip));
 			}
 
 			if (debug)
 				System.out.println("2.2");
 
 			InputStreamReader webStream = null;
 			try {
 				URL url = new URL(surl);
 				webStream = new InputStreamReader(url.openStream());
 				LineReader lineReader = new LineReader(webStream, out);
 
 				if (debug)
 					System.out.println("2.3");
 
 				// get hours-store in times
 
 				lineReader.skipTo("contentTable borderTop");
 				lineReader.skipTo("taC");
 				Integer[] times = new Integer[20];
 				int timesLen = 0;
 
 				if (debug)
 					System.out.println("2.4");
 
 				while (lineReader.line.contains("taC")) {
 					String s = lineReader.getStuff();
 					try {
 						times[timesLen] = Integer.valueOf(s.split("&")[0]);
 						if (times[timesLen] == 12)
 							times[timesLen] = 0;
 						if (s.endsWith("PM"))
 							times[timesLen] = times[timesLen] + 12;
 						timesLen++;
 					} catch (NumberFormatException e) {
 
 					}
 					lineReader.readLine();
 				}
 
 				if (debug)
 					System.out.println("2.5");
 
 				hf = new HourlyForecast[timesLen];
 
 				int i = 0;
 				lineReader.skipTo("tbody");
 				lineReader.skipTo("taC");
 				Integer[] temps = new Integer[20];
 				while (lineReader.line.contains("taC")) {
 					lineReader.readLine();
 					try {
 						temps[i] = Integer.parseInt(lineReader
 								.getStuffToWhite());
 					} catch (NumberFormatException e) {
 						temps[i] = null;
 					}
 					lineReader.skipTo("/td");
 					lineReader.readLine();
 					i++;
 				}
 
 				if (debug)
 					System.out.println("2.6");
 
 				i = 0;
 				lineReader.skipTo("Probability of Precipitation");
 				lineReader.skipTo("taC");
 				Integer[] precips = new Integer[20];
 				while (lineReader.line.contains("taC")) {
 					lineReader.readLine();
 					String s = lineReader.line.trim();
 					try {
 						if (s.endsWith("%"))
 							precips[i] = Integer.parseInt(s.substring(0,
 									s.length() - 1));
 					} catch (NumberFormatException e) {
 						precips[i] = null;
 					}
 					i++;
 					lineReader.readLine();
 					lineReader.readLine();
 				}
 
 				if (debug)
 					System.out.println("2.7");
 
 				for (i = 0; i < timesLen; i++) {
 					hf[i] = new HourlyForecast(times[i], temps[i], precips[i]);
 				}
 
 				if (debug) {
 					out.close();
 					fileWriter.close();
 					outFile.delete();
 				}
 				webStream.close();
 
 				if (debug)
 					System.out.println("2.8");
 
 				return hf;
 
 			} catch (NullPointerException e) {
 				if (tries != 6) {
 					if (debug) {
 						out.close();
 						fileWriter.close();
 					}
 					webStream.close();
 					System.out.println("Reading hourly forecast failed for "
 							+ this.zip + " for day " + dayNumber + ".");
 					if (hf == null)
 						hf = new HourlyForecast[0];
 					return hf;
 				}
 			}
 		}
 		return hf;
 	}
 
 	private void getOverallForecast() throws IOException {
 		for (int tries = 0; tries < 7; tries++) {
 			if (debug)
 				System.out.println("1.1");
 			File outFile = null;
 			FileWriter fileWriter = null;
 			PrintWriter out = null;
 			String overallURL = MakeURL.overallURL(this.zip);
 			if (debug) {
 				SimpleDateFormat format = new SimpleDateFormat(
 						"yyyyMMdd'T'kkmmssSSS");
 				outFile = new File("test/test"
 						+ format.format(Calendar.getInstance().getTime())
 						+ "overall.html");
 				fileWriter = new FileWriter(outFile, false);
 				out = new PrintWriter(fileWriter, true);
 				if (debug)
 					System.out.println("1.1.1");
 				out.println(overallURL);
 			}
 
 			if (debug)
 				System.out.println("1.2");
 
 			InputStreamReader webStream = null;
 			try {
 				URL url = new URL(overallURL);
 				webStream = new InputStreamReader(url.openStream());
 				LineReader lineReader = new LineReader(webStream, out);
 
 				of1 = new OverallForecast();
 				of3 = new OverallForecast();
 
 				if (debug)
 					System.out.println("1.3");
 
 				lineReader.skipTo("10-Day Forecast");
 				lineReader.skipTo("fct_day_" + dayNumber1);
 				lineReader.skipTo("class=\"b");
 				this.of1.high = lineReader.getIntStuff();
 				lineReader.skipTo("popValue");
 				String stuff = lineReader.getStuff();
 				if (stuff.endsWith("%")) {
 					this.of1.PoP = Integer.valueOf(stuff.substring(0,
 							stuff.length() - 1));
 				}
 
 				if (debug)
 					System.out.println("1.4");
 
 				lineReader.skipTo("fct_day_" + dayNumber3);
 				lineReader.skipTo("class=\"b");
 				this.of3.high = lineReader.getIntStuff();
 				lineReader.skipTo("popValue");
 				stuff = lineReader.getStuff();
 				if (stuff.endsWith("%")) {
 					this.of3.PoP = Integer.valueOf(stuff.substring(0,
 							stuff.length() - 1));
 				}
 
 				if (debug)
 					System.out.println("1.5");
 
 				if (debug) {
 					out.close();
 					fileWriter.close();
 					outFile.delete();
 				}
 				webStream.close();
 
 				return;
 			} catch (NullPointerException e) {
 				if (tries != 6) {
 					if (debug) {
 						out.close();
 						fileWriter.close();
 					}
 					webStream.close();
 					System.out.println("Reading overall forecast failed for "
 							+ this.zip + ".");
 					if (of1 == null)
 						of1 = new OverallForecast();
 					if (of3 == null)
 						of3 = new OverallForecast();
 				}
 			}
 		}
 	}
 
 	private void wundergroundPast() throws Exception {
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(cal.getTimeInMillis() - DAY_IN_MILLIS);
 		wundergroundPast(cal.getTime());
 	}
 
 	private void wundergroundPast(Date date) throws Exception {
 		for (int tries = 0; tries < 7; tries++) {
 			File outfile = null;
 			FileWriter fileWriter = null;
 			PrintWriter out = null;
 			if (debug) {
 				SimpleDateFormat format = new SimpleDateFormat(
 						"yyyyMMdd'T'kkmmssSSS");
 				outfile = new File("test\\test"
 						+ format.format(Calendar.getInstance().getTime())
 						+ ".html");
 				fileWriter = new FileWriter(outfile, false);
 				out = new PrintWriter(fileWriter, true);
 				out.println(MakeURL.pastWundergroundURL(zip, date));
 			}
 			InputStreamReader webStream = null;
 			URL url = null;
 			try {
 
 				url = new URL(MakeURL.pastWundergroundURL(zip, date));
 
 				webStream = new InputStreamReader(url.openStream());
 				
 				//make sure there's actually data there to get
 				LineReader lr = new LineReader(webStream, out);
 				try {
 					//wunderground says "No daily or hourly history data available" or "No hourly history data available"
 					lr.skipTo("history data available");
 					System.out.println("no history data available for "+this.zip);
 					webStream.close();
 					this.op = null;
 					this.hp = null;
 					return;
 				} catch (NullPointerException e){
 					
 				}
 				
 				webStream = new InputStreamReader(url.openStream());
 				getPastWData(new LineReader(webStream, out));
 				this.past = new PastData(this.zip, new SimpleDateFormat(
 						"yyyy-MM-dd").format(date), this.op, this.hp);
 
 				webStream.close();
 				if (debug) {
 					out.close();
 					fileWriter.close();
 					outfile.delete();
 				}
 				return;
 
 			} catch (NullPointerException e) {
 				if (webStream != null)
 					webStream.close();
 				System.out.println("Reading past weather data failed for "
 						+ this.zip + ".");
 				System.out.println(url);
 				if (debug) {
 					out.close();
 					fileWriter.close();
 				}
 				Thread.sleep(1000);
 				if (this.op == null) {
 					this.op = new OverallPast();
 				}
 			}
 		}
 	}
 
 	private void getPastWData(LineReader reader) throws IOException, Exception {
 		for (int i = 0; i < 24; i++) {
 			this.hp[i] = new HourlyPast(i);
 		}
 		this.op = new OverallPast();
 
 		Float snow = null;
 
 		reader.skipTo("contentData");
 
 		reader.skipTo("<tbody>");
 		reader.readLine();
 		while (reader.line.contains("<tr")) {
 			reader.readLine();
 			if (reader.line.contains("indent")) {
 				String stuff = reader.getStuff();
 				if (stuff.equalsIgnoreCase("Max Temperature")) {
 					reader.readLine();
 					reader.readLine();
 					this.op.high = reader.getFloatStuff();
 				} else if (stuff.equalsIgnoreCase("Precipitation")) {
 					reader.readLine();
 					reader.readLine();
 					this.op.precip = reader.getFloatStuff();
 				} else if (stuff.equalsIgnoreCase("Snow")) {
 					reader.readLine();
 					reader.readLine();
 					snow = reader.getFloatStuff();
 				}
 			}
 
 			reader.skipTo("/tr");
 			reader.readLine();
 		}
 
 		if ((this.op.precip != null && snow != null)) {
 			this.op.precip = this.op.precip + snow;
 		} else if (this.op.precip != null) {
 
 		} else if (snow != null) {
 			this.op.precip = snow;
 		}
 
 		reader.skipTo("observations_details");
 
 		int colNum = 0;
 		int tempCol = -1;
 		int precipCol = -1;
 		int condCol = -1;
 		while (!reader.line.contains("/thead")) {
 			if (reader.line.contains("<th>")) {
 				colNum++;
 				if (reader.line.contains("Temp.")) {
 					tempCol = colNum;
 				} else if (reader.line.contains("Precip")) {
 					precipCol = colNum;
 				} else if (reader.line.contains("Conditions")) {
 					condCol = colNum;
 				}
 			}
 			reader.readLine();
 		}
 
 		reader.skipTo("<tbody>");
 		reader.readLine();
 		int i = 0;
 		while (reader.line.contains("<tr")) {
 			colNum = 0;
 			while (!reader.line.contains("/tr")) {
 				if (reader.line.contains("<td")) {
 					colNum++;
 					if (colNum == 1) {
 						String stuff = reader.getStuff();
 						int colonidx = stuff.indexOf(":");
 						String hourStr = stuff.substring(0, colonidx);
 						int hour = Integer.parseInt(hourStr);
 						if (hour == 12) {
 							hour = 0;
 						}
 						if (stuff.endsWith("PM")) {
 							hour = hour + 12;
 						}
 						i = hour;
 						this.hp[i].hour = hour;
 					} else if (!hp[i].conditions.equals("")) {
 					} else if (tempCol == colNum) {
 						reader.readLine();
 						this.hp[i].temp = reader.getFloatStuff();
 					} else if (precipCol == colNum) {
 						if (reader.line.contains("N/A")) {
 							this.hp[i].precip = null;
 						} else {
 							reader.readLine();
 							this.hp[i].precip = reader.getFloatStuff();
 						}
 					} else if (condCol == colNum) {
 						this.hp[i].conditions = reader.getStuff();
 					}
 				}
 				reader.readLine();
 			}
 			reader.readLine();
 		}
 	}
 }
