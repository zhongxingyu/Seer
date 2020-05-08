 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-shell Project
 //
 // WIGReader.java
 // Since: 2009/11/30
 //
 // $URL: http://svn.utgenome.org/utgb/trunk/utgb/utgb-shell/src/main/java/org/utgenome/shell/db/wig/WIGReader.java $ 
 // $Author: yoshimura $
 //--------------------------------------
 package org.utgenome.format.wig;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.zip.DataFormatException;
 import java.util.zip.GZIPInputStream;
 
 import org.utgenome.gwt.utgb.client.bio.WigGraphData;
 import org.xerial.util.StopWatch;
 import org.xerial.util.log.Logger;
 
 public class WIGDatabaseReader {
 	private Connection connection = null;
 	private Statement statement;
 	private static Logger _logger = Logger.getLogger(WIGDatabaseReader.class);
 
 	private static float minValue = Float.MAX_VALUE;
 	private static float maxValue = Float.MIN_VALUE;
 
 	public WIGDatabaseReader(String inputFileURL) throws ClassNotFoundException, SQLException {
 		Class.forName("org.sqlite.JDBC");
 		connection = DriverManager.getConnection("jdbc:sqlite:" + inputFileURL);
 		statement = connection.createStatement();
 	}
 
 	public void close() throws SQLException {
 		if (connection != null)
 			connection.close();
 	}
 
 	public ArrayList<String> getBrowser() throws SQLException {
 		ArrayList<String> browser = new ArrayList<String>();
 
 		ResultSet rs = statement.executeQuery(String.format("select * from browser"));
 		while (rs.next()) {
 			browser.add(rs.getString("description"));
 		}
 
 		return browser;
 	}
 
 	public ArrayList<Integer> getTrackIdList(String chrom) throws SQLException {
 		ArrayList<Integer> trackIdList = new ArrayList<Integer>();
 
 		ResultSet rs = statement.executeQuery(String.format("select distinct track_id from track where name='chrom' and value='%s'", chrom));
 		while (rs.next()) {
 			trackIdList.add(Integer.valueOf(rs.getInt("track_id")));
 		}
 
 		return trackIdList;
 	}
 
 	public ArrayList<Integer> getTrackIdList() throws SQLException {
 		ArrayList<Integer> trackIdList = new ArrayList<Integer>();
 
 		ResultSet rs = statement.executeQuery("select distinct track_id from track");
 		while (rs.next()) {
 			trackIdList.add(Integer.valueOf(rs.getInt("track_id")));
 		}
 
 		return trackIdList;
 	}
 
 	public ArrayList<String> getChromList() throws SQLException {
 		ArrayList<String> trackIdList = new ArrayList<String>();
 
 		ResultSet rs = statement.executeQuery("select distinct value from track where name='chrom'");
 		while (rs.next()) {
 			trackIdList.add(rs.getString("value"));
 		}
 
 		return trackIdList;
 	}
 
 	public HashMap<String, String> getTrack(int trackId) throws SQLException {
 		HashMap<String, String> track = new HashMap<String, String>();
 
 		ResultSet rs = statement.executeQuery(String.format("select * from track where track_id=%d", trackId));
 		while (rs.next()) {
 			track.put(rs.getString("name"), rs.getString("value"));
 		}
 
 		return track;
 	}
 
 	public HashMap<Integer, Float> getData(int trackId, int start, int end) throws SQLException, IOException, DataFormatException, NumberFormatException,
 			ClassNotFoundException {
 		return (getData((end - start), trackId, start, end));
 	}
 
 	public HashMap<Integer, Float> getData(int windowWidth, int trackId, int start, int end) throws SQLException, IOException, DataFormatException,
 			NumberFormatException, ClassNotFoundException {
 		HashMap<Integer, Float> data = new HashMap<Integer, Float>();
 		HashMap<String, String> track = getTrack(trackId);
 
 		int[] chromStarts;
 		float[] dataValues;
 
 		minValue = Float.MAX_VALUE;
 		maxValue = Float.MIN_VALUE;
 
		if (start > end) {
			int tmp = start;
			start = end;
			end = tmp;
		}

 		int rough = (int) Math.floor((end - start) / windowWidth);
 		if (rough < 1)
 			rough = 1;
 
 		StopWatch st1 = new StopWatch();
 		StopWatch st2 = new StopWatch();
 
 		ResultSet rs = statement.executeQuery(String.format("select * from data where track_id=%d and start<=%d and end>=%d order by start", trackId, end,
 				start));
 		while (rs.next()) {
 			int i;
 			ByteArrayInputStream bis;
 			GZIPInputStream in;
 			ObjectInputStream ois;
 
 			int nDatas = rs.getInt("data_num");
 
 			// read data point
 			if (track.get("stepType").equals("variableStep")) {
 				bis = new ByteArrayInputStream(rs.getBytes("chrom_starts"));
 				in = new GZIPInputStream(bis);
 				ois = new ObjectInputStream(in);
 
 				chromStarts = (int[]) ois.readObject();
 
 				ois.close();
 				in.close();
 				bis.close();
 			}
 			else if (track.get("stepType").equals("fixedStep")) {
 				int startPoint = rs.getInt("start");
 				int stepSize = Integer.parseInt(track.get("step"));
 				chromStarts = new int[nDatas];
 
 				for (i = 0; i < nDatas; i++) {
 					chromStarts[i] = startPoint + (stepSize * (int) i);
 				}
 			}
 			else {
 				throw new DataFormatException();
 			}
 
 			// read data value
 			bis = new ByteArrayInputStream(rs.getBytes("data_values"));
 			in = new GZIPInputStream(bis);
 			ois = new ObjectInputStream(in);
 
 			dataValues = (float[]) ois.readObject();
 
 			ois.close();
 			in.close();
 			bis.close();
 
 			st2.resume();
 			for (i = 0; i < nDatas; i++) {
 				if (start <= chromStarts[i] && chromStarts[i] <= end) {
 					if (dataValues[i] != 0.0f) {
 						int chromStart = chromStarts[i] - (chromStarts[i] % rough);
 						if (data.containsKey(chromStart))
 							data.put(chromStart, Math.max(dataValues[i], data.get(chromStart)));
 						else
 							data.put(chromStart, dataValues[i]);
 					}
 
 					minValue = Math.min(minValue, dataValues[i]);
 					maxValue = Math.max(maxValue, dataValues[i]);
 				}
 			}
 			st2.stop();
 		}
 		_logger.info("min: " + minValue + ", max: " + maxValue);
 		_logger.info("Time(all)    : " + st1.getElapsedTime());
 		_logger.info("Time(archive): " + st2.getElapsedTime());
 		return data;
 	}
 
 	public HashMap<Integer, Float> getData_old(long windowWidth, int trackId, long start, long end) throws SQLException, IOException, DataFormatException,
 			NumberFormatException {
 		HashMap<Integer, Float> data = new HashMap<Integer, Float>();
 		HashMap<String, String> track = getTrack(trackId);
 
 		byte[] buffer = new byte[32768];
 
 		int rough = (int) Math.floor((end - start) / windowWidth);
 		if (rough < 1)
 			rough = 1;
 
 		StopWatch st1 = new StopWatch();
 		StopWatch st2 = new StopWatch();
 
 		ResultSet rs = statement.executeQuery(String.format("select * from data where track_id=%d and start<=%d and end>=%d order by start", trackId, end,
 				start));
 		while (rs.next()) {
 			int i;
 			int temp;
 			ByteArrayInputStream bis;
 			GZIPInputStream in;
 			ByteArrayOutputStream os;
 
 			int nDatas = rs.getInt("data_num");
 			int[] chromStarts = new int[nDatas];
 			//float[] dataValues = new float[nDatas];
 			String[] dataValues;
 
 			// read data point
 			if (track.get("stepType").equals("variableStep")) {
 				bis = new ByteArrayInputStream(rs.getBytes("chrom_starts"));
 				in = new GZIPInputStream(bis);
 				os = new ByteArrayOutputStream();
 				while ((temp = in.read(buffer, 0, buffer.length)) != -1) {
 					os.write(buffer, 0, temp);
 				}
 				os.flush();
 
 				i = 0;
 				for (String tempString : os.toString().split("\t")) {
 					chromStarts[i++] = Integer.parseInt(tempString);
 				}
 
 				os.close();
 				in.close();
 				bis.close();
 			}
 			else if (track.get("stepType").equals("fixedStep")) {
 				int startPoint = rs.getInt("start");
 				int stepSize = Integer.parseInt(track.get("step"));
 
 				for (i = 0; i < nDatas; i++) {
 					chromStarts[i] = startPoint + (stepSize * (int) i);
 				}
 			}
 			else {
 				throw new DataFormatException();
 			}
 
 			// read data value
 			bis = new ByteArrayInputStream(rs.getBytes("data_values"));
 			in = new GZIPInputStream(bis);
 			os = new ByteArrayOutputStream();
 			while ((temp = in.read(buffer, 0, buffer.length)) != -1) {
 				os.write(buffer, 0, temp);
 			}
 			os.flush();
 
 			st2.resume();
 			i = 0;
 			//		for(String tempString : os.toString().split("\t"))
 			//		{
 			//			dataValues[i++] = Float.parseFloat(tempString);
 			//		}
 			dataValues = os.toString().split("\t");
 			st2.stop();
 
 			os.close();
 			in.close();
 			bis.close();
 
 			for (i = 0; i < nDatas; i++) {
 				if (chromStarts[i] >= start && chromStarts[i] <= end) {
 					int chromStart = chromStarts[i] - (chromStarts[i] % rough);
 					if (data.containsKey(chromStarts))
 						//					data.put(chromStart, Math.max(dataValues[i], data.get(chromStarts)));
 						data.put(chromStart, Math.max(Float.parseFloat(dataValues[i]), data.get(chromStarts)));
 					else
 						//					data.put(chromStart, dataValues[i]);
 						data.put(chromStart, Float.parseFloat(dataValues[i]));
 				}
 			}
 		}
 		System.out.println("st2:" + st2.getElapsedTime());
 		System.out.println("st1:" + st1.getElapsedTime());
 		return data;
 	}
 
 	public HashMap<Integer, Float> getData(int trackId) throws NumberFormatException, SQLException, IOException, DataFormatException, ClassNotFoundException {
 		return getData(trackId, 0, Integer.MAX_VALUE);
 	}
 
 	public WigGraphData getWigData(int windowWidth, int trackId, int start, int end) throws SQLException, NumberFormatException, IOException,
 			DataFormatException, ClassNotFoundException {
 		WigGraphData wigData = new WigGraphData();
 
 		wigData.setTrack_id(trackId);
 		wigData.setBrowser(getBrowser());
 		wigData.setTrack(getTrack(trackId));
 		wigData.setData(getData(windowWidth, trackId, start, end));
 		wigData.setMinValue(minValue);
 		wigData.setMaxValue(maxValue);
 
 		return wigData;
 	}
 
 	public WigGraphData getWigData(int trackId, int start, int end) throws SQLException, NumberFormatException, IOException, DataFormatException,
 			ClassNotFoundException {
 		WigGraphData wigData = new WigGraphData();
 
 		wigData.setTrack_id(trackId);
 		wigData.setBrowser(getBrowser());
 		wigData.setTrack(getTrack(trackId));
 		wigData.setData(getData(trackId, start, end));
 		wigData.setMinValue(minValue);
 		wigData.setMaxValue(maxValue);
 
 		return wigData;
 	}
 
 	public WigGraphData getWigData(int trackId) throws SQLException, NumberFormatException, IOException, DataFormatException, ClassNotFoundException {
 		return getWigData(trackId, 0, Integer.MAX_VALUE);
 	}
 
 	public ArrayList<WigGraphData> getWigDataList(int windowWidth, String chrom, int start, int end) throws SQLException, NumberFormatException, IOException,
 			DataFormatException, ClassNotFoundException {
 		ArrayList<WigGraphData> wigDataList = new ArrayList<WigGraphData>();
 
 		for (int id : getTrackIdList(chrom)) {
 			wigDataList.add(getWigData(windowWidth, id, start, end));
 		}
 
 		return wigDataList;
 	}
 
 	public ArrayList<WigGraphData> getWigDataList(String chrom, int start, int end) throws SQLException, NumberFormatException, IOException,
 			DataFormatException, ClassNotFoundException {
 		ArrayList<WigGraphData> wigDataList = new ArrayList<WigGraphData>();
 
 		for (int id : getTrackIdList(chrom)) {
 			wigDataList.add(getWigData(id, start, end));
 		}
 
 		return wigDataList;
 	}
 }
