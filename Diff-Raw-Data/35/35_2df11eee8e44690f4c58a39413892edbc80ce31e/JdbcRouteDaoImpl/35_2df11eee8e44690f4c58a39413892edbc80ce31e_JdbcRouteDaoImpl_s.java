 package de.christianzunker.mobilecitygate.dao.impl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 
 import de.christianzunker.mobilecitygate.beans.Route;
 import de.christianzunker.mobilecitygate.dao.RouteDao;
 
 @Repository
 public class JdbcRouteDaoImpl implements RouteDao {
 	
 	private static final Logger logger = Logger.getLogger(JdbcRouteDaoImpl.class);
 	
 	private @Autowired JdbcTemplate jdbcTemplate;
 	
     public List<Route> getRouteList() {
     	logger.debug("entering method getRouteList");
        List<Route> routes = this.jdbcTemplate.query("SELECT id, name, description, length, kml_file, locale, length_unit, start_lon, start_lat, color FROM routes", new RouteMapper());
         logger.debug("leaving method getRouteList");
         return routes;
     }
     
     public List<Route> getRouteListByClientLocale(int clientId, String locale) {
     	logger.debug("entering method getRouteList");
        List<Route> routes = this.jdbcTemplate.query("SELECT id, name, description, length, kml_file, locale, length_unit, start_lon, start_lat, color FROM routes WHERE client_id = " + clientId + " AND locale = '" + locale + "'", new RouteMapper());
         logger.debug("leaving method getRouteList");
         return routes;
     }
     
     public Route getRouteById(int routeId) {
     	logger.debug("entering method getRouteById");
    	Route route = this.jdbcTemplate.queryForObject("SELECT id, name, description, length, kml_file, locale, length_unit, start_lon, start_lat, color FROM routes WHERE id = " + routeId, new RouteMapper());
     	logger.debug("leaving method getRouteById");
     	return route;
     }
     
     public List<Route> getRoutesByIds(String routeIds) {
     	logger.debug("entering method getRouteList");
        List<Route> routes = this.jdbcTemplate.query("SELECT id, name, description, length, kml_file, locale, length_unit, start_lon, start_lat, color FROM routes WHERE id IN (" + routeIds + ")", new RouteMapper());
         logger.debug("leaving method getRouteList");
         return routes;
     }
     
     private static final class RouteMapper implements RowMapper<Route> {
     	
     	private static final Logger logger = Logger.getLogger(RouteMapper.class);
 
         public Route mapRow(ResultSet rs, int rowNum) throws SQLException {
         	logger.debug("entering method mapRow");
         	Route route = new Route();
             route.setId(rs.getInt("id"));
             route.setName(rs.getString("name"));
             route.setLength(rs.getDouble("length"));
             route.setDescription(rs.getString("description"));
             route.setMapKML(rs.getString("kml_file"));
             route.setLocale(rs.getString("locale"));
             route.setLengthUnit(rs.getString("length_unit"));
             route.setStartLon(rs.getDouble("start_lon"));
             route.setStartLat(rs.getDouble("start_lat"));
             route.setColor(rs.getString("color"));
             logger.debug("leaving method mapRow");
             return route;
         }        
     }
 }
