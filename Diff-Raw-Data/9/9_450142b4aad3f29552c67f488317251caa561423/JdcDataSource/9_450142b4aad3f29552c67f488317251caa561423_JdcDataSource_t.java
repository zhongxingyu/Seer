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
 package ch.windmobile.server.jdc;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
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
 import ch.windmobile.server.jdc.dataobject.Data;
 import ch.windmobile.server.jdc.dataobject.Sensor;
 import ch.windmobile.server.jdc.dataobject.Station;
 
 public class JdcDataSource implements WindMobileDataSource {
     protected final Logger log = LoggerFactory.getLogger(getClass());
 
     // There is no timezone information stored in the JDC database
     private static final DateTimeZone defaultStationTimeZone = DateTimeZone.getDefault();
 
     // 1 hour by default
     private int historicDuration = 60 * 60;
     private int windTrendScale = 500000;
 
     static enum Channel {
         windDirection(1, "windDirection"), windAverage(2, "windAverage"), windMax(3, "windMax"), airTemperature(4, "airTemperature"), airHumidity(5,
             "airHumidity");
 
         private final int id;
         private final String name;
 
         private Channel(int id, String name) {
             this.id = id;
             this.name = name;
         }
 
         public int getId() {
             return id;
         }
 
         public String getName() {
             return name;
         }
     }
 
     static enum JdcStatus {
         inactive(0), active(1), maintenance(2), test(3);
 
         private final int value;
 
         private JdcStatus(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
     }
 
     private final SessionFactory sessionFactory;
 
     public JdcDataSource(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     private Session getSession() {
         return sessionFactory.openSession();
     }
 
     private int getJdcStationId(String stationId) {
         return Integer.parseInt(stationId);
     }
 
     private String getServerStationId(int stationId) {
         return Integer.toString(stationId);
     }
 
     @SuppressWarnings("unchecked")
     private List<Station> getStations(Session session, boolean allStation) {
         Criteria criteria = session.createCriteria(Station.class);
         if (allStation) {
             criteria.add(Restrictions.ge("status", 1));
         } else {
             criteria.add(Restrictions.eq("status", 1));
         }
         criteria.setCacheable(true);
         criteria.setCacheRegion("stationInfos");
         return criteria.list();
     }
 
     private Station getStation(Session session, String stationId) {
         Criteria criteria = session.createCriteria(Station.class);
         criteria.add(Restrictions.eq("id", getJdcStationId(stationId)));
         criteria.setCacheable(true);
         criteria.setCacheRegion("stationInfos");
         return (Station) criteria.uniqueResult();
     }
 
     private DateTime getLastUpdate(Session session, Sensor sensor) throws DataSourceException {
         Criteria criteria = session.createCriteria(Data.class);
         criteria.add(Restrictions.eq("sensor", sensor));
         criteria.addOrder(Order.desc("time"));
         criteria.setMaxResults(1);
         criteria.setCacheable(true);
         criteria.setCacheRegion("dataQueries");
 
         Data lastData = (Data) criteria.uniqueResult();
         if ((lastData == null) || (lastData.getTime() == null) || (lastData.getTime().getTime() == 0)) {
             throw new DataSourceException(Error.INVALID_DATA, "No data found for sensor '" + sensor.getId() + "'");
         }
         return new DateTime(lastData.getTime(), defaultStationTimeZone);
     }
 
     private Sensor getSensorForChannel(Station station, Channel channel) throws DataSourceException {
         Set<Sensor> sensors = station.getSensors();
         for (Sensor sensor : sensors) {
             if (sensor.getChannel() == channel.getId()) {
                 return sensor;
             }
         }
         throw new DataSourceException(Error.INVALID_DATA, "No sensor found for channel '" + channel.getId() + "'");
     }
 
     /**
      * Return the last value of a sensorId
      * 
      * @param session
      * @param sensor
      * @param date
      * @return
      * @throws DataSourceException
      */
     @SuppressWarnings("unchecked")
     private double getData(Session session, Sensor sensor, DateTime lastUpdate) throws DataSourceException {
         Date date = new Date(lastUpdate.getMillis());
 
         Criteria criteria = session.createCriteria(Data.class);
         criteria.add(Restrictions.eq("sensor", sensor));
         criteria.add(Restrictions.eq("time", date));
         criteria.setCacheable(true);
         criteria.setCacheRegion("dataQueries");
 
         List<Data> datas = criteria.list();
         if (datas.size() != 1) {
             log.warn("There are multiple values for sensor '" + sensor.getId() + "' at '" + date + "'");
             // Try to return the 1st non null value
             for (Data data : datas) {
                 if (data.getValue() != 0) {
                     return data.getValue();
                 }
             }
         }
         return datas.get(0).getValue();
     }
 
     /**
      * Return a list of Data from (lastUpdate - duration) to lastUpdate
      * 
      * @param session
      * @param sensorId
      * @param duration
      *            The duration in seconds
      * @return
      * @throws DataSourceException
      */
     @SuppressWarnings("unchecked")
     private List<Data> getHistoricData(Session session, Sensor sensor, int duration) throws DataSourceException {
         DateTime lastUpdate = getLastUpdate(session, sensor);
         long startTime = lastUpdate.getMillis() - duration * 1000;
 
         Criteria criteria = session.createCriteria(Data.class);
         criteria.add(Restrictions.eq("sensor", sensor));
         criteria.add(Restrictions.ge("time", new Date(startTime)));
         criteria.setCacheable(true);
         criteria.setCacheRegion("dataQueries");
         return criteria.list();
     }
 
     @Override
     public StationUpdateTime getLastUpdate(String stationId) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             Station station = getStation(session, stationId);
             StationUpdateTime returnObject = new StationUpdateTime();
 
             // Last update, based on wind average
             Sensor sensor = getSensorForChannel(station, Channel.windAverage);
             DateTime lastUpdate = getLastUpdate(session, sensor);
             returnObject.setLastUpdate(lastUpdate);
 
             // Expiration date
             DateTime now = new DateTime(lastUpdate.getZone());
             DateTime expirationDate = getExpirationDate(now, lastUpdate);
             returnObject.setExpirationDate(expirationDate);
 
             return returnObject;
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
         }
     }
 
     // Replaced by getExpirationDate()
     @Deprecated
     private int getDataValidity() {
         DateTime now = new DateTime(defaultStationTimeZone);
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
 
     static protected Status getMaintenanceStatus(Station station) {
         if (station.getStatus() == JdcStatus.maintenance.getValue()) {
             return Status.RED;
         } else if (station.getStatus() == JdcStatus.test.getValue()) {
             return Status.ORANGE;
         } else {
             return Status.GREEN;
         }
     }
 
     static protected Status getStatus(Station station, DateTime now, DateTime expirationDate) {
         // Orange > 10 minutes late
         DateTime orangeStatusLimit = expirationDate.plus(10 * 60 * 1000);
         // Red > 2h10 late
         DateTime redStatusLimit = expirationDate.plus(2 * 3600 * 1000 + 10 * 60 * 1000);
 
         if ((station.getStatus() == JdcStatus.maintenance.getValue()) || (now.isAfter(redStatusLimit))) {
             return Status.RED;
         } else if ((station.getStatus() == JdcStatus.test.getValue()) || (now.isAfter(orangeStatusLimit))) {
             return Status.ORANGE;
         } else {
             return Status.GREEN;
         }
     }
 
     private StationInfo createStationInfo(Session session, Station station) throws NumberFormatException {
         StationInfo stationInfo = new StationInfo();
 
         stationInfo.setId(getServerStationId(station.getId()));
         stationInfo.setShortName(station.getShortName());
         stationInfo.setName(station.getName());
         stationInfo.setDataValidity(getDataValidity());
         stationInfo.setStationLocationType(StationLocationType.TAKEOFF);
         stationInfo.setWgs84Latitude(Double.parseDouble(station.getWgs84Latitude()));
         stationInfo.setWgs84Longitude(Double.parseDouble(station.getWgs84Longitude()));
         stationInfo.setAltitude(station.getAltitude());
         stationInfo.setMaintenanceStatus(getMaintenanceStatus(station));
 
         return stationInfo;
     }
 
     @Override
     public StationInfo getStationInfo(String stationId) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             Station station = getStation(session, stationId);
 
             return createStationInfo(session, station);
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
         }
     }
 
     @Override
     public List<StationInfo> getStationInfoList(boolean allStation) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             List<Station> stations = getStations(session, allStation);
 
             List<StationInfo> stationInfoList = new Vector<StationInfo>();
             for (Station station : stations) {
                 try {
                     stationInfoList.add(createStationInfo(session, station));
                 } catch (NumberFormatException e) {
                     log.warn("This station '" + station.getId() + "' was ignored because:", e);
                 }
             }
 
             return stationInfoList;
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
         }
     }
 
     private StationData createStationData(Session session, Station station) throws DataSourceException {
         StationData stationData = new StationData();
 
         stationData.setStationId(getServerStationId(station.getId()));
 
         // Last update, based on wind average
         Sensor sensor = getSensorForChannel(station, Channel.windAverage);
         DateTime lastUpdate = getLastUpdate(session, sensor);
         stationData.setLastUpdate(lastUpdate);
 
         // Expiration date
         DateTime now = new DateTime(lastUpdate.getZone());
         DateTime expirationDate = getExpirationDate(now, lastUpdate);
         stationData.setExpirationDate(expirationDate);
 
         // Status
         stationData.setStatus(getStatus(station, now, expirationDate));
 
         // Wind average
         sensor = getSensorForChannel(station, Channel.windAverage);
         double windAverage = getData(session, sensor, lastUpdate);
         stationData.setWindAverage((float) windAverage);
 
         // Wind max
         sensor = getSensorForChannel(station, Channel.windMax);
         double windMax = getData(session, sensor, lastUpdate);
         stationData.setWindMax((float) windMax);
 
         // Wind direction chart
         sensor = getSensorForChannel(station, Channel.windDirection);
         List<Data> windDirectionDatas = getHistoricData(session, sensor, getHistoricDuration());
         Serie windDirectionSerie = createSerie(windDirectionDatas);
         windDirectionSerie.setName(Channel.windDirection.getName());
         Chart windDirectionChart = new Chart();
         windDirectionChart.setDuration(getHistoricDuration());
         windDirectionChart.getSeries().add(windDirectionSerie);
         stationData.setWindDirectionChart(windDirectionChart);
 
         // Wind history min/average
         sensor = getSensorForChannel(station, Channel.windAverage);
         List<Data> windAverageDatas = getHistoricData(session, sensor, getHistoricDuration());
         double minValue = Double.MAX_VALUE;
         double sum = 0;
         // double[][] windTrendAverageDatas = new double[windAverageDatas.size()][2];
         for (int i = 0; i < windAverageDatas.size(); i++) {
             Data data = windAverageDatas.get(i);
             minValue = Math.min(minValue, data.getValue());
             sum += data.getValue();
             // windTrendAverageDatas[i][0] = data.getTime().getTime();
             // windTrendAverageDatas[i][1] = data.getValue();
         }
         stationData.setWindHistoryMin((float) minValue);
         stationData.setWindHistoryAverage((float) (sum / windAverageDatas.size()));
 
         // Wind history max
         sensor = getSensorForChannel(station, Channel.windMax);
         List<Data> windMaxDatas = getHistoricData(session, sensor, getHistoricDuration());
         double maxValue = Double.MIN_VALUE;
         double[][] windTrendMaxDatas = new double[windMaxDatas.size()][2];
         for (int i = 0; i < windMaxDatas.size(); i++) {
             Data data = windMaxDatas.get(i);
             maxValue = Math.max(maxValue, data.getValue());
             windTrendMaxDatas[i][0] = data.getTime().getTime();
             windTrendMaxDatas[i][1] = data.getValue();
         }
         stationData.setWindHistoryMax((float) maxValue);
 
         // Wind trend
         LinearRegression linearRegression = new LinearRegression(windTrendMaxDatas);
         linearRegression.compute();
         double slope = linearRegression.getBeta1();
         double angle = Math.toDegrees(Math.atan(slope * getWindTrendScale()));
        stationData.setWindTrend((int) Math.round(angle));
 
         // Air temperature
         sensor = getSensorForChannel(station, Channel.airTemperature);
         double airTemperature = getData(session, sensor, lastUpdate);
         stationData.setAirTemperature((float) airTemperature);
 
         // Air humidity
         sensor = getSensorForChannel(station, Channel.airHumidity);
         double airHumidity = getData(session, sensor, lastUpdate);
         stationData.setAirHumidity((float) airHumidity);
 
         return stationData;
     }
 
     @Override
     public StationData getStationData(String stationId) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             Station station = getStation(session, stationId);
 
             return createStationData(session, station);
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
         }
     }
 
     private Serie createSerie(List<Data> datas) {
         Serie serie = new Serie();
         for (Data data : datas) {
             Point newPoint = new Point();
             newPoint.setDate(data.getTime().getTime());
             newPoint.setValue((float) data.getValue());
             serie.getPoints().add(newPoint);
         }
         return serie;
     }
 
     @Override
     public Chart getWindChart(String stationId, int duration) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             Station station = getStation(session, stationId);
 
             Chart windChart = new Chart();
 
             // Last update
             Sensor sensor = getSensorForChannel(station, Channel.windAverage);
             DateTime lastUpdate = getLastUpdate(session, sensor);
             windChart.setLastUpdate(lastUpdate);
 
             // Wind historic chart
             sensor = getSensorForChannel(station, Channel.windAverage);
             List<Data> windAverageDatas = getHistoricData(session, sensor, duration);
             Serie windAverageSerie = createSerie(windAverageDatas);
             windAverageSerie.setName(Channel.windAverage.getName());
             sensor = getSensorForChannel(station, Channel.windMax);
             List<Data> windMaxDatas = getHistoricData(session, sensor, duration);
             Serie windMaxSerie = createSerie(windMaxDatas);
             windMaxSerie.setName(Channel.windMax.getName());
             sensor = getSensorForChannel(station, Channel.windDirection);
             List<Data> windDirectionDatas = getHistoricData(session, sensor, duration);
             Serie windDirectionSerie = createSerie(windDirectionDatas);
             windDirectionSerie.setName(Channel.windDirection.getName());
             windChart.setDuration(duration);
             windChart.getSeries().add(windAverageSerie);
             windChart.getSeries().add(windMaxSerie);
             windChart.getSeries().add(windDirectionSerie);
 
             return windChart;
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
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
