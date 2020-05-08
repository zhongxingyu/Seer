 /*******************************************************************************
  * Copyright (c) 2011 epyx SA.
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License along
  * with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package ch.windmobile.server.mongo;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.MutableDateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.windmobile.server.datasourcemodel.DataSourceException;
 import ch.windmobile.server.datasourcemodel.DataSourceException.Error;
 import ch.windmobile.server.datasourcemodel.LinearRegression;
 import ch.windmobile.server.datasourcemodel.WindMobileDataSource;
 import ch.windmobile.server.datasourcemodel.xml.Chart;
 import ch.windmobile.server.datasourcemodel.xml.Point;
 import ch.windmobile.server.datasourcemodel.xml.Serie;
 import ch.windmobile.server.datasourcemodel.xml.StationData;
 import ch.windmobile.server.datasourcemodel.xml.StationInfo;
 import ch.windmobile.server.datasourcemodel.xml.StationLocationType;
 import ch.windmobile.server.datasourcemodel.xml.StationUpdateTime;
 import ch.windmobile.server.datasourcemodel.xml.Status;
 
import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 public abstract class MongoDataSource implements WindMobileDataSource {
     protected final Logger log = LoggerFactory.getLogger(getClass());
 
     // 1 hour by default
     private int historicDuration = 60 * 60;
     private int windTrendScale = 500000;
 
     static enum DataTypeConstant {
         windDirection("w-dir", "windDirection"), windAverage("w-avg", "windAverage"), windMax("w-max", "windMax"), airTemperature("temp",
             "airTemperature"), airHumidity("hum", "airHumidity"), airPressure("pres", "airPressure"), rain("rain", "rain");
 
         private final String jsonKey;
         private final String name;
 
         private DataTypeConstant(String jsonKey, String name) {
             this.jsonKey = jsonKey;
             this.name = name;
         }
 
         public String getJsonKey() {
             return jsonKey;
         }
 
         public String getName() {
             return name;
         }
     }
 
     protected static DataSourceException exceptionHandler(Throwable e) {
         if (e instanceof DataSourceException) {
             return (DataSourceException) e;
         } else if (e instanceof MongoException) {
             return new DataSourceException(Error.DATABASE_ERROR, e);
         }
         return new DataSourceException(Error.SERVER_ERROR, e);
     }
 
     private Mongo mongoService;
     private DB database;
 
     public MongoDataSource() {
         try {
             mongoService = new Mongo();
         } catch (UnknownHostException e) {
         }
         database = mongoService.getDB("windmobile");
     }
 
     abstract protected String getProvider();
 
     protected List<String> getStationsFilter() {
         return null;
     }
 
     private String getStationsCollectionName() {
         return "stations";
     }
 
     private BasicDBObject findStationJson(String stationId) throws DataSourceException {
         DBCollection stationsCollection = database.getCollection(getStationsCollectionName());
         BasicDBObject stationJson = (BasicDBObject) stationsCollection.findOne(BasicDBObjectBuilder.start("_id", stationId).get());
         if (stationJson != null) {
             return stationJson;
         } else {
             throw new DataSourceException(DataSourceException.Error.INVALID_DATA, "Unable to find station with id '" + stationId + "'");
         }
     }
 
     private String getDataCollectionName(String stationId) {
         return stationId;
     }
 
     private DateTime getLastUpdateDateTime(BasicDBObject lastDataJson) {
         return new DateTime(lastDataJson.getLong("_id") * 1000);
     }
 
     private List<BasicDBObject> getHistoricData(String stationId, DateTime lastUpdate, int duration) {
         DBCollection dataCollection = database.getCollection(getDataCollectionName(stationId));
         long startTime = lastUpdate.getMillis() - duration * 1000;
         DBObject query = BasicDBObjectBuilder.start("_id", BasicDBObjectBuilder.start("$gte", startTime / 1000).get()).get();
 
         List<BasicDBObject> datas = new ArrayList<BasicDBObject>();
         DBCursor cursor = dataCollection.find(query);
         while (cursor.hasNext()) {
             datas.add((BasicDBObject) cursor.next());
 
         }
         return datas;
     }
 
     @Override
     public StationUpdateTime getLastUpdate(String stationId) throws DataSourceException {
         try {
             BasicDBObject lastDataJson = (BasicDBObject) findStationJson(stationId).get("last");
             if (lastDataJson == null) {
                 throw new DataSourceException(DataSourceException.Error.INVALID_DATA, "No last data for station '" + stationId + "'");
             }
             StationUpdateTime returnObject = new StationUpdateTime();
             returnObject.setLastUpdate(getLastUpdateDateTime(lastDataJson));
 
             return returnObject;
         } catch (Exception e) {
             throw exceptionHandler(e);
         }
     }
 
     // Replaced by getExpirationDate()
     @Deprecated
     private int getDataValidity(DateTime now) {
         if (isSummerFrequency(now)) {
             // Summer frequency
             return 20 * 60;
         } else {
             // Winter frequency
             return 60 * 60;
         }
     }
 
     static protected boolean isSummerFrequency(DateTime date) {
         return (date.getMonthOfYear() >= 4) && (date.getMonthOfYear() <= 9);
     }
 
     static protected DateTime getExpirationDate(DateTime now, DateTime lastUpdate) {
         MutableDateTime expirationDate = new MutableDateTime(lastUpdate.getZone());
 
         if (isSummerFrequency(now)) {
             expirationDate.setMillis(lastUpdate.getMillis() + 20 * 60 * 1000);
             if (expirationDate.getHourOfDay() >= 20) {
                 expirationDate.addDays(1);
                 expirationDate.setHourOfDay(8);
                 expirationDate.setMinuteOfHour(0);
                 expirationDate.setSecondOfMinute(0);
             }
         } else {
             expirationDate.setMillis(lastUpdate.getMillis() + 60 * 60 * 1000);
             if (expirationDate.getHourOfDay() >= 17) {
                 expirationDate.addDays(1);
                 expirationDate.setHourOfDay(9);
                 expirationDate.setMinuteOfHour(0);
                 expirationDate.setSecondOfMinute(0);
             }
         }
         return expirationDate.toDateTime();
     }
 
     static protected Status getDataStatus(String status, DateTime now, DateTime expirationDate) {
         // Orange > 10 minutes late
         DateTime orangeStatusLimit = expirationDate.plus(10 * 60 * 1000);
         // Red > 2h10 late
         DateTime redStatusLimit = expirationDate.plus(2 * 3600 * 1000 + 10 * 60 * 1000);
 
         if (Status.RED.value().equalsIgnoreCase(status) || now.isAfter(redStatusLimit)) {
             return Status.RED;
         } else if (Status.ORANGE.value().equalsIgnoreCase(status) || now.isAfter(orangeStatusLimit)) {
             return Status.ORANGE;
         } else {
             // Handle case when data received are in the future
             return Status.GREEN;
         }
     }
 
     private StationInfo createStationInfo(BasicDBObject stationJson) {
         StationInfo stationInfo = new StationInfo();
 
         stationInfo.setId(stationJson.getString("_id"));
         stationInfo.setShortName(stationJson.getString("short"));
         stationInfo.setName(stationJson.getString("name"));
         stationInfo.setDataValidity(getDataValidity(new DateTime()));
         stationInfo.setStationLocationType(StationLocationType.TAKEOFF);
        BasicDBList locationJson = (BasicDBList) stationJson.get("loc");
        stationInfo.setWgs84Longitude((Double)locationJson.get(0));
        stationInfo.setWgs84Latitude((Double)locationJson.get(1));;
         stationInfo.setAltitude(stationJson.getInt("alt"));
         stationInfo.setMaintenanceStatus(Status.fromValue(stationJson.getString("status")));
 
         return stationInfo;
     }
 
     @Override
     public List<StationInfo> getStationInfoList(boolean allStation) throws DataSourceException {
         try {
             DBCollection stations = database.getCollection(getStationsCollectionName());
 
             List<String> list = new ArrayList<String>();
             if (allStation == true) {
                 list.add(Status.RED.value());
                 list.add(Status.ORANGE.value());
                 list.add(Status.GREEN.value());
             } else {
                 list.add(Status.GREEN.value());
             }
             DBObject query = BasicDBObjectBuilder.start("prov", getProvider()).add("status", new BasicDBObject("$in", list)).get();
             DBCursor cursor = stations.find(query);
 
             List<StationInfo> stationInfoList = new ArrayList<StationInfo>();
             while (cursor.hasNext()) {
                 try {
                     BasicDBObject stationJson = (BasicDBObject) cursor.next();
                     if (getStationsFilter() != null) {
                         String stationId = stationJson.getString("_id");
                         if (getStationsFilter().contains(stationId)) {
                             stationInfoList.add(createStationInfo(stationJson));
                         }
                     } else {
                         stationInfoList.add(createStationInfo(stationJson));
                     }
                 } catch (Exception e) {
                     log.warn("Station was ignored because:", e);
                 }
             }
 
             return stationInfoList;
         } catch (Exception e) {
             throw exceptionHandler(e);
         }
     }
 
     @Override
     public StationInfo getStationInfo(String stationId) throws DataSourceException {
         try {
             return createStationInfo(findStationJson(stationId));
         } catch (Exception e) {
             throw exceptionHandler(e);
         }
     }
 
     private StationData createStationData(String stationId) throws DataSourceException {
         BasicDBObject stationJson = findStationJson(stationId);
         BasicDBObject lastDataJson = (BasicDBObject) stationJson.get("last");
         if (lastDataJson == null) {
             throw new DataSourceException(DataSourceException.Error.INVALID_DATA, "No last data for station '" + stationId + "'");
         }
 
         StationData stationData = new StationData();
         stationData.setStationId(stationId);
 
         DateTime lastUpdate = getLastUpdateDateTime(lastDataJson);
         stationData.setLastUpdate(lastUpdate);
         DateTime now = new DateTime();
         DateTime expirationDate = getExpirationDate(now, lastUpdate);
         stationData.setExpirationDate(expirationDate);
 
         // Status
         stationData.setStatus(getDataStatus(stationJson.getString("status"), now, expirationDate));
 
         // Wind average
         stationData.setWindAverage((float) lastDataJson.getDouble(DataTypeConstant.windAverage.getJsonKey()));
 
         // Wind max
         stationData.setWindMax((float) lastDataJson.getDouble(DataTypeConstant.windMax.getJsonKey()));
 
         List<BasicDBObject> datas = getHistoricData(stationId, lastUpdate, getHistoricDuration());
         if (datas.size() > 0) {
             // Wind direction chart
             Serie windDirectionSerie = createSerie(datas, DataTypeConstant.windDirection.getJsonKey());
             windDirectionSerie.setName(DataTypeConstant.windDirection.getName());
             Chart windDirectionChart = new Chart();
             windDirectionChart.setDuration(getHistoricDuration());
             windDirectionChart.getSeries().add(windDirectionSerie);
             stationData.setWindDirectionChart(windDirectionChart);
 
             // Wind history min/average
             double minValue = Double.MAX_VALUE;
             double maxValue = Double.MIN_VALUE;
             double sum = 0;
             double[][] windTrendMaxDatas = new double[datas.size()][2];
             // double[][] windTrendAverageDatas = new double[windAverageDatas.size()][2];
             for (int i = 0; i < datas.size(); i++) {
                 BasicDBObject data = datas.get(i);
                 // JDC unix-time is in seconds, windmobile java-time in millis
                 long millis = data.getLong("_id") * 1000;
                 double windAverage = data.getDouble(DataTypeConstant.windAverage.getJsonKey());
                 double windMax = data.getDouble(DataTypeConstant.windMax.getJsonKey());
                 minValue = Math.min(minValue, windAverage);
                 maxValue = Math.max(maxValue, windMax);
                 sum += windAverage;
                 windTrendMaxDatas[i][0] = millis;
                 windTrendMaxDatas[i][1] = windMax;
             }
             stationData.setWindHistoryMin((float) minValue);
             stationData.setWindHistoryAverage((float) (sum / datas.size()));
             stationData.setWindHistoryMax((float) maxValue);
 
             // Wind trend
             LinearRegression linearRegression = new LinearRegression(windTrendMaxDatas);
             linearRegression.compute();
             double slope = linearRegression.getBeta1();
             double angle = Math.toDegrees(Math.atan(slope * getWindTrendScale()));
             stationData.setWindTrend((int) Math.round(angle));
         }
 
         // Air temperature
         stationData.setAirTemperature((float) lastDataJson.getDouble(DataTypeConstant.airTemperature.getJsonKey(), -1));
 
         // Air humidity
         stationData.setAirHumidity((float) lastDataJson.getDouble(DataTypeConstant.airHumidity.getJsonKey(), -1));
 
         // Air pressure
         String key = DataTypeConstant.airPressure.getJsonKey();
         if (lastDataJson.containsField(key)) {
             stationData.setAirPressure((float) lastDataJson.getDouble(key));
         }
 
         // Rain
         key = DataTypeConstant.rain.getJsonKey();
         if (lastDataJson.containsField(key)) {
             stationData.setRain((float) lastDataJson.getDouble(key));
         }
 
         return stationData;
     }
 
     @Override
     public StationData getStationData(String stationId) throws DataSourceException {
         try {
             return createStationData(stationId);
         } catch (Exception e) {
             throw exceptionHandler(e);
         }
     }
 
     private Serie createSerie(List<BasicDBObject> datas, String key) {
         Serie serie = new Serie();
         for (BasicDBObject data : datas) {
             Point newPoint = new Point();
             // JDC unix-time is in seconds, windmobile java-time in millis
             newPoint.setDate(data.getLong("_id") * 1000);
             newPoint.setValue(((Number) data.get(key)).floatValue());
             serie.getPoints().add(newPoint);
         }
         return serie;
     }
 
     @Override
     public Chart getWindChart(String stationId, int duration) throws DataSourceException {
         try {
             Chart windChart = new Chart();
             windChart.setStationId(stationId);
 
             BasicDBObject lastDataJson = (BasicDBObject) findStationJson(stationId).get("last");
             if (lastDataJson == null) {
                 throw new DataSourceException(DataSourceException.Error.INVALID_DATA, "No last data for station '" + stationId + "'");
             }
             DateTime lastUpdate = getLastUpdateDateTime(lastDataJson);
             windChart.setLastUpdate(lastUpdate);
             DateTime now = new DateTime();
             DateTime expirationDate = getExpirationDate(now, lastUpdate);
             windChart.setExpirationDate(expirationDate);
 
             List<BasicDBObject> datas = getHistoricData(stationId, lastUpdate, duration);
 
             // Wind historic chart
             Serie windAverageSerie = createSerie(datas, DataTypeConstant.windAverage.getJsonKey());
             windAverageSerie.setName(DataTypeConstant.windAverage.getName());
             Serie windMaxSerie = createSerie(datas, DataTypeConstant.windMax.getJsonKey());
             windMaxSerie.setName(DataTypeConstant.windMax.getName());
             Serie windDirectionSerie = createSerie(datas, DataTypeConstant.windDirection.getJsonKey());
             windDirectionSerie.setName(DataTypeConstant.windDirection.getName());
             windChart.setDuration(duration);
             windChart.getSeries().add(windAverageSerie);
             windChart.getSeries().add(windMaxSerie);
             windChart.getSeries().add(windDirectionSerie);
 
             return windChart;
         } catch (Exception e) {
             throw exceptionHandler(e);
         }
     }
 
     public int getHistoricDuration() {
         return historicDuration;
     }
 
     public void setHistoricDuration(int historicDuration) {
         this.historicDuration = historicDuration;
     }
 
     public int getWindTrendScale() {
         return windTrendScale;
     }
 
     public void setWindTrendScale(int windTrendScale) {
         this.windTrendScale = windTrendScale;
     }
 }
