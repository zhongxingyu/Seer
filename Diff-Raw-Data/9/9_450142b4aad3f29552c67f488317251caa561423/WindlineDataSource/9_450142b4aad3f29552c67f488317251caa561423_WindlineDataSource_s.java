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
 package ch.windmobile.server.windline;
 
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
 import ch.windmobile.server.windline.dataobject.Data;
 import ch.windmobile.server.windline.dataobject.Property;
 import ch.windmobile.server.windline.dataobject.PropertyValue;
 import ch.windmobile.server.windline.dataobject.Station;
 
 public class WindlineDataSource implements WindMobileDataSource {
     protected final Logger log = LoggerFactory.getLogger(getClass());
 
     // There is no timezone information stored in the JDC database
     private static final DateTimeZone defaultStationTimeZone = DateTimeZone.getDefault();
 
     private static final String STATUS_OFFLINE = "offline";
     private static final String STATUS_DEMO = "demo";
     private static float conversionFactor = 3.6f;
 
     // 1 hour by default
     private int historicDuration = 60 * 60;
     private int windTrendScale = 500000;
 
     static enum DataTypeConstant {
         windDirection(16404, "windDirection"), windAverage(16402, "windAverage"), windMax(16410, "windMax"), airTemperature(16400, "airTemperature"), airHumidity(
             16401, "airHumidity");
 
         private final int id;
         private final String name;
 
         private DataTypeConstant(int id, String name) {
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
 
     private final SessionFactory sessionFactory;
 
     public WindlineDataSource(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     private Session getSession() {
         return sessionFactory.openSession();
     }
 
     private int getWindlineStationId(String stationId) {
         return Integer.parseInt(stationId);
     }
 
     private String getServerStationId(int stationId) {
         return Integer.toString(stationId);
     }
 
     private Property getPropertyWithKey(Session session, String key) {
         Criteria criteria = session.createCriteria(Property.class);
         criteria.add(Restrictions.eq("key", key));
         criteria.setCacheable(true);
         criteria.setCacheRegion("stationInfos");
         return (Property) criteria.uniqueResult();
     }
 
     @SuppressWarnings("unchecked")
     private List<Station> getStations(Session session) {
         Criteria criteria = session.createCriteria(Station.class);
         criteria.setCacheable(true);
         criteria.setCacheRegion("stationInfos");
         return criteria.list();
     }
 
     private Station getStation(Session session, String stationId) {
         Criteria criteria = session.createCriteria(Station.class);
         criteria.add(Restrictions.eq("stationId", getWindlineStationId(stationId)));
         criteria.setCacheable(true);
         criteria.setCacheRegion("stationInfos");
         return (Station) criteria.uniqueResult();
     }
 
     private DateTime getLastUpdate(Session session, Station station, int dataTypeId) throws DataSourceException {
         Criteria criteria = session.createCriteria(Data.class);
         criteria.add(Restrictions.eq("stationId", station.getStationId()));
         criteria.add(Restrictions.eq("dataTypeId", dataTypeId));
         criteria.addOrder(Order.desc("time"));
         criteria.setMaxResults(1);
         criteria.setCacheable(true);
         criteria.setCacheRegion("dataQueries");
 
         Data lastData = (Data) criteria.uniqueResult();
         if ((lastData == null) || (lastData.getTime() == null)) {
             throw new DataSourceException(Error.INVALID_DATA, "No data found for dataTypeId '" + dataTypeId + "'");
         }
 
         return new DateTime(lastData.getTime(), defaultStationTimeZone);
     }
 
     private Status getMaintenanceStatus(Session session, Station station) {
         Property propertyStatus = getPropertyWithKey(session, "status");
 
         Set<PropertyValue> propertyValues = station.getPropertyValues();
         for (PropertyValue propertyValue : propertyValues) {
             if (propertyValue.getProperty() == propertyStatus) {
                 if (propertyValue.getValue().equalsIgnoreCase(STATUS_OFFLINE)) {
                     return Status.RED;
                 } else if (propertyValue.getValue().equalsIgnoreCase(STATUS_DEMO)) {
                     return Status.ORANGE;
                 }
             }
         }
         return Status.GREEN;
     }
 
     static protected DateTime getExpirationDate(DateTime lastUpdate) {
         return lastUpdate.plus(10 * 60 * 1000);
     }
 
     private Status getStatus(Session session, Station station, DateTime now, DateTime expirationDate) {
         // Orange > 10 minutes late
         DateTime orangeStatusLimit = expirationDate.plus(10 * 60 * 1000);
         // Red > 2h10 late
         DateTime redStatusLimit = expirationDate.plus(2 * 3600 * 1000 + 10 * 60 * 1000);
 
         Status maintenanceStatus = getMaintenanceStatus(session, station);
 
         if ((maintenanceStatus == Status.RED) || (now.isAfter(redStatusLimit))) {
             return Status.RED;
         } else if ((maintenanceStatus == Status.ORANGE) || (now.isAfter(orangeStatusLimit))) {
             return Status.ORANGE;
         } else {
             return Status.GREEN;
         }
     }
 
     /**
      * Return the last value of a sensorId
      * 
      * @param session
      * @param sensorId
      * @return
      * @throws DataSourceException
      */
     private float getData(Session session, Station station, int dataTypeId) throws DataSourceException {
         Criteria criteria = session.createCriteria(Data.class);
         criteria.add(Restrictions.eq("stationId", station.getStationId()));
         criteria.add(Restrictions.eq("dataTypeId", dataTypeId));
         criteria.addOrder(Order.desc("time"));
         criteria.setMaxResults(1);
         criteria.setCacheable(true);
         criteria.setCacheRegion("dataQueries");
 
         Data data = (Data) criteria.uniqueResult();
         return Float.parseFloat(data.getValue());
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
     private List<Data> getHistoricData(Session session, Station station, int dataTypeId, int duration) throws DataSourceException {
         DateTime lastUpdate = getLastUpdate(session, station, dataTypeId);
         long startTime = lastUpdate.getMillis() - duration * 1000;
 
         Criteria criteria = session.createCriteria(Data.class);
         criteria.add(Restrictions.eq("stationId", station.getStationId()));
         criteria.add(Restrictions.eq("dataTypeId", dataTypeId));
         criteria.add(Restrictions.ge("time", new Date(startTime)));
         criteria.setCacheable(true);
         criteria.setCacheRegion("dataQueries");
         return criteria.list();
     }
 
     private StationInfo createStationInfo(Session session, Station station) {
         StationInfo stationInfo = new StationInfo();
 
         stationInfo.setId(getServerStationId(station.getStationId()));
         stationInfo.setShortName(station.getName());
         stationInfo.setName(station.getShortDescription());
         stationInfo.setMaintenanceStatus(getMaintenanceStatus(session, station));
         stationInfo.setStationLocationType(StationLocationType.TAKEOFF);
 
         Property propertyAltitude = getPropertyWithKey(session, "altitude");
         Property propertyLatitude = getPropertyWithKey(session, "latitude");
         Property propertyLongitude = getPropertyWithKey(session, "longitude");
 
         Set<PropertyValue> propertyValues = station.getPropertyValues();
         for (PropertyValue propertyValue : propertyValues) {
             if (propertyValue.getProperty() == propertyAltitude) {
                 stationInfo.setAltitude(Integer.decode(propertyValue.getValue()));
             } else if (propertyValue.getProperty() == propertyLatitude) {
                 stationInfo.setWgs84Latitude(CoordinateHelper.parseDMS(propertyValue.getValue()));
             } else if (propertyValue.getProperty() == propertyLongitude) {
                 stationInfo.setWgs84Longitude(CoordinateHelper.parseDMS(propertyValue.getValue()));
             }
         }
 
         if (stationInfo.getAltitude() == null) {
             throw new RuntimeException("Altitude not found");
         }
         if (stationInfo.getWgs84Latitude() == null) {
             throw new RuntimeException("Latitude not found");
         }
         if (stationInfo.getWgs84Longitude() == null) {
             throw new RuntimeException("Longitude not found");
         }
 
         return stationInfo;
     }
 
     @Override
     public StationUpdateTime getLastUpdate(String stationId) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             Station station = getStation(session, stationId);
             StationUpdateTime returnObject = new StationUpdateTime();
 
             // Last update
             DateTime lastUpdate = getLastUpdate(session, station, DataTypeConstant.windAverage.getId());
             returnObject.setLastUpdate(lastUpdate);
 
             // Expiration date
             DateTime expirationDate = getExpirationDate(lastUpdate);
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
 
     @Override
     public List<StationInfo> getStationInfoList(boolean allStation) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             List<Station> stations = getStations(session);
 
             Property propertyType = getPropertyWithKey(session, "type");
             Property propertyStatus = getPropertyWithKey(session, "status");
 
             List<StationInfo> stationInfoList = new Vector<StationInfo>();
             for (Station station : stations) {
                 boolean keepIt = true;
                 Set<PropertyValue> propertyValues = station.getPropertyValues();
                 for (PropertyValue propertyValue : propertyValues) {
                     if (propertyValue.getProperty() == propertyType) {
                         if (propertyValue.getValue().equals("voice")) {
                             keepIt = false;
                             break;
                         }
                     }
                     if ((allStation == false) && (propertyValue.getProperty() == propertyStatus)) {
                         if (propertyValue.getValue().equalsIgnoreCase(STATUS_OFFLINE) || propertyValue.getValue().equalsIgnoreCase(STATUS_DEMO)) {
                             keepIt = false;
                             break;
                         }
                     }
                 }
 
                 if (keepIt) {
                     try {
                         stationInfoList.add(createStationInfo(session, station));
                     } catch (Exception e) {
                         log.warn("This station '" + station.getId() + "' was ignored because:", e);
                     }
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
 
     private StationData createStationData(Session session, Station station) throws DataSourceException {
         StationData stationData = new StationData();
 
         stationData.setStationId(getServerStationId(station.getStationId()));
 
         // Last update
         DateTime lastUpdate = getLastUpdate(session, station, DataTypeConstant.windAverage.getId());
         stationData.setLastUpdate(lastUpdate);
 
         // Expiration date
         DateTime now = new DateTime(lastUpdate.getZone());
         DateTime expirationDate = getExpirationDate(lastUpdate);
         stationData.setExpirationDate(expirationDate);
 
         // Status
         stationData.setStatus(getStatus(session, station, now, expirationDate));
 
         // Wind average
         float windAverage = getData(session, station, DataTypeConstant.windAverage.getId()) * conversionFactor;
         stationData.setWindAverage(windAverage);
 
         // Wind max
         float windMax = getData(session, station, DataTypeConstant.windMax.getId()) * conversionFactor;
         stationData.setWindMax(windMax);
 
         // Wind direction chart
         List<Data> windDirectionDatas = getHistoricData(session, station, DataTypeConstant.windDirection.getId(), getHistoricDuration());
         Serie windDirectionSerie = createSerie(windDirectionDatas, 1f);
         windDirectionSerie.setName(DataTypeConstant.windDirection.getName());
         Chart windDirectionChart = new Chart();
         windDirectionChart.setDuration(getHistoricDuration());
         windDirectionChart.getSeries().add(windDirectionSerie);
         stationData.setWindDirectionChart(windDirectionChart);
 
         // Wind history min/average
         List<Data> windAverageDatas = getHistoricData(session, station, DataTypeConstant.windAverage.getId(), getHistoricDuration());
         double minValue = Double.MAX_VALUE;
         double sum = 0;
         // double[][] windTrendAverageDatas = new
         // double[windAverageDatas.size()][2];
         for (int i = 0; i < windAverageDatas.size(); i++) {
             Data data = windAverageDatas.get(i);
             float floatValue = Float.parseFloat(data.getValue()) * conversionFactor;
             minValue = Math.min(minValue, floatValue);
             sum += floatValue;
             // windTrendAverageDatas[i][0] = data.getTime().getTime();
             // windTrendAverageDatas[i][1] = data.getValue();
         }
         stationData.setWindHistoryMin((float) minValue);
         stationData.setWindHistoryAverage((float) (sum / windAverageDatas.size()));
 
         // Wind history max
         List<Data> windMaxDatas = getHistoricData(session, station, DataTypeConstant.windMax.getId(), getHistoricDuration());
         double maxValue = Double.MIN_VALUE;
         double[][] windTrendMaxDatas = new double[windMaxDatas.size()][2];
         for (int i = 0; i < windMaxDatas.size(); i++) {
             Data data = windMaxDatas.get(i);
             float floatValue = Float.parseFloat(data.getValue()) * conversionFactor;
             maxValue = Math.max(maxValue, floatValue);
             windTrendMaxDatas[i][0] = data.getTime().getTime();
             windTrendMaxDatas[i][1] = floatValue;
         }
         stationData.setWindHistoryMax((float) maxValue);
 
         // Wind trend
         LinearRegression linearRegression = new LinearRegression(windTrendMaxDatas);
         linearRegression.compute();
         double slope = linearRegression.getBeta1();
         double angle = Math.toDegrees(Math.atan(slope * getWindTrendScale()));
        stationData.setWindTrend((int) angle);
 
         // Air temperature
         double airTemperature = getData(session, station, DataTypeConstant.airTemperature.getId());
         stationData.setAirTemperature((float) airTemperature);
 
         // Air humidity
         double airHumidity = getData(session, station, DataTypeConstant.airHumidity.getId());
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
 
     private Serie createSerie(List<Data> datas, float conversionFactor) {
         Serie serie = new Serie();
         for (Data data : datas) {
             Point newPoint = new Point();
             newPoint.setDate(data.getTime().getTime());
             newPoint.setValue(Float.parseFloat(data.getValue()) * conversionFactor);
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
             DateTime lastUpdate = getLastUpdate(session, station, DataTypeConstant.windAverage.getId());
             windChart.setLastUpdate(lastUpdate);
 
             // Wind historic chart
             List<Data> windAverageDatas = getHistoricData(session, station, DataTypeConstant.windAverage.getId(), duration);
             Serie windAverageSerie = createSerie(windAverageDatas, conversionFactor);
             windAverageSerie.setName(DataTypeConstant.windAverage.getName());
 
             List<Data> windMaxDatas = getHistoricData(session, station, DataTypeConstant.windMax.getId(), duration);
             Serie windMaxSerie = createSerie(windMaxDatas, conversionFactor);
             windMaxSerie.setName(DataTypeConstant.windMax.getName());
 
             List<Data> windDirectionDatas = getHistoricData(session, station, DataTypeConstant.windDirection.getId(), duration);
             Serie windDirectionSerie = createSerie(windDirectionDatas, 1f);
             windDirectionSerie.setName(DataTypeConstant.windDirection.getName());
 
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
