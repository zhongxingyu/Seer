 package edu.usc.sirlab.db;
 
 import java.io.Serializable;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import edu.usc.sirlab.*;
 
 
 public class DatabaseQuery implements Serializable {
 	private static final long serialVersionUID = 5185656606746489628L;
 	
 	private DatabaseConnection dbConnection;
 	
 	public DatabaseQuery() throws IllegalAccessException, InstantiationException, ClassNotFoundException, SQLException {
 		dbConnection = new DatabaseConnection();
 		dbConnection.connect();
 	}
 	
 	public DatabaseQuery(DatabaseConnection dbConnection) {
 		this.dbConnection = dbConnection;
 	}
 	
 	public int getInterferogramCount() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		int count = 0;
 		ResultSet rs = dbConnection.executeQuery("SELECT COUNT(*) FROM insar_interferogram");
 		if(rs.next())
 			count = rs.getInt(1);
 		
 		
 		return count;
 	}
 	
 	public List<Interferogram> getInerferograms() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		List<Interferogram> insars = new ArrayList<Interferogram>();
 		ResultSet rs = dbConnection.executeQuery("SELECT * FROM insar_interferogram ORDER BY date_start");
 		
 		while(rs.next()) {
 			int id = rs.getInt("id");
 			String title = rs.getString("title");
 			String description = rs.getString("description");
 			GeoPoint ref1 = new GeoPoint(rs.getDouble("point1_lat"), rs.getDouble("point1_lon"));
 			GeoPoint ref2 = new GeoPoint(rs.getDouble("point2_lat"), rs.getDouble("point2_lon"));
 			GeoPoint ref3 = new GeoPoint(rs.getDouble("point3_lat"), rs.getDouble("point3_lon"));
 			GeoPoint ref4 = new GeoPoint(rs.getDouble("point4_lat"), rs.getDouble("point4_lon"));
 			Date start = rs.getDate("date_start");
 			Date end = rs.getDate("date_end");
 			double timespan = rs.getDouble("timespan");
 			int width = rs.getInt("width");
 			int length = rs.getInt("length");
 			String dataURL = rs.getString("data_file_url");
 			String metaURL = rs.getString("meta_file_url");
 			String imageURL = rs.getString("image_url");
 			String thumbnailURL = rs.getString("thumbnail_url");
 			
 			Interferogram insar = new Interferogram(id, title, description, ref1, ref2, ref3, ref4, start, end, dataURL, metaURL, imageURL, thumbnailURL, timespan, width, length);
 			insars.add(insar);
 		}
 		
 		return insars;
 	}
 	
 	public List<Interferogram> getInerferograms(int limitStart, int limitEnd) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		List<Interferogram> insars = new ArrayList<Interferogram>();
 		PreparedStatement statement = dbConnection.getPreparedStatement("SELECT * FROM insar_interferogram ORDER BY date_start LIMIT ?, ?");
 		statement.setInt(1, limitStart);
 		statement.setInt(2, limitEnd);
 		ResultSet rs = statement.executeQuery();
 		
 		while(rs.next()) {
 			int id = rs.getInt("id");
 			String title = rs.getString("title");
 			String description = rs.getString("description");
 			GeoPoint ref1 = new GeoPoint(rs.getDouble("point1_lat"), rs.getDouble("point1_lon"));
 			GeoPoint ref2 = new GeoPoint(rs.getDouble("point2_lat"), rs.getDouble("point2_lon"));
 			GeoPoint ref3 = new GeoPoint(rs.getDouble("point3_lat"), rs.getDouble("point3_lon"));
 			GeoPoint ref4 = new GeoPoint(rs.getDouble("point4_lat"), rs.getDouble("point4_lon"));
 			Date start = rs.getDate("date_start");
 			Date end = rs.getDate("date_end");
 			double timespan = rs.getDouble("timespan");
 			int width = rs.getInt("width");
 			int length = rs.getInt("length");
 			String dataURL = rs.getString("data_file_url");
 			String metaURL = rs.getString("meta_file_url");
 			String imageURL = rs.getString("image_url");
 			String thumbnailURL = rs.getString("thumbnail_url");
 			
 			Interferogram insar = new Interferogram(id, title, description, ref1, ref2, ref3, ref4, start, end, dataURL, metaURL, imageURL, thumbnailURL, timespan, width, length);
 			insars.add(insar);
 		}
 		
 		return insars;
 	}
 	
 	public Interferogram getInerferogram(String iid) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		Interferogram insar = null;
 		PreparedStatement statement = dbConnection.getPreparedStatement("SELECT * FROM insar_interferogram WHERE id = ?");
 		statement.setString(1, iid);
 		ResultSet rs = statement.executeQuery();
 		
 		if(rs.next()) {
 			int id = rs.getInt("id");
 			String title = rs.getString("title");
 			String description = rs.getString("description");
 			GeoPoint ref1 = new GeoPoint(rs.getDouble("point1_lat"), rs.getDouble("point1_lon"));
 			GeoPoint ref2 = new GeoPoint(rs.getDouble("point2_lat"), rs.getDouble("point2_lon"));
 			GeoPoint ref3 = new GeoPoint(rs.getDouble("point3_lat"), rs.getDouble("point3_lon"));
 			GeoPoint ref4 = new GeoPoint(rs.getDouble("point4_lat"), rs.getDouble("point4_lon"));
 			Date start = rs.getDate("date_start");
 			Date end = rs.getDate("date_end");
 			double timespan = rs.getDouble("timespan");
 			int width = rs.getInt("width");
 			int length = rs.getInt("length");
 			String dataURL = rs.getString("data_file_url");
 			String metaURL = rs.getString("meta_file_url");
 			String imageURL = rs.getString("image_url");
 			String thumbnailURL = rs.getString("thumbnail_url");
 			
 			insar = new Interferogram(id, title, description, ref1, ref2, ref3, ref4, start, end, dataURL, metaURL, imageURL, thumbnailURL, timespan, width, length);
 		}
 		
 		return insar;
 	}
 	
 	public int getUAVSARCount() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		int count = 0;
 		ResultSet rs = dbConnection.executeQuery("SELECT COUNT(*) AS total FROM uavsar_pri");
 		if(rs.next())
 			count = rs.getInt(1);
 		
 		return count;
 	}
 	
 	public List<UAVSAR> getUAVSAR() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		List<UAVSAR> uavsar = new ArrayList<UAVSAR>();
 		ResultSet rs = dbConnection.executeQuery("SELECT * FROM uavsar_pri");
 		
 		while(rs.next()) {
 			int id = rs.getInt("id");
 			String title = rs.getString("title");
 			String description = rs.getString("description");
 			GeoPoint ref1 = new GeoPoint(rs.getDouble("point1_lat"), rs.getDouble("point1_lon"));
 			GeoPoint ref2 = new GeoPoint(rs.getDouble("point2_lat"), rs.getDouble("point2_lon"));
 			GeoPoint ref3 = new GeoPoint(rs.getDouble("point3_lat"), rs.getDouble("point3_lon"));
 			GeoPoint ref4 = new GeoPoint(rs.getDouble("point4_lat"), rs.getDouble("point4_lon"));
 			Date date1 = rs.getTimestamp("date_1");
 			Date date2 = rs.getTimestamp("date_2");
 			String sourceURL = rs.getString("source_url");
 			String metaDataURL = rs.getString("metadata_url");
 			String imageURL = rs.getString("preview_img_url");
 			String kmlURL = rs.getString("preview_kml_url");
 			
 			UAVSAR u = new UAVSAR(id, title, description, date1, date2, sourceURL, metaDataURL, imageURL, kmlURL, ref1, ref2, ref3, ref4);
 			u.setDataCategories(getUAVSARCategories(u));
 			uavsar.add(u);
 		}
 		
 		return uavsar;
 	}
 	
 	public UAVSAR getUAVSAR(String uid) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		UAVSAR uavsar = null;
 		PreparedStatement statement = dbConnection.getPreparedStatement("SELECT * FROM uavsar_pri WHERE id = ?");
 		statement.setString(1, uid);
 		ResultSet rs = statement.executeQuery();
 		
 		if(rs.next()) {
 			int id = rs.getInt("id");
 			String title = rs.getString("title");
 			String description = rs.getString("description");
 			GeoPoint ref1 = new GeoPoint(rs.getDouble("point1_lat"), rs.getDouble("point1_lon"));
 			GeoPoint ref2 = new GeoPoint(rs.getDouble("point2_lat"), rs.getDouble("point2_lon"));
 			GeoPoint ref3 = new GeoPoint(rs.getDouble("point3_lat"), rs.getDouble("point3_lon"));
 			GeoPoint ref4 = new GeoPoint(rs.getDouble("point4_lat"), rs.getDouble("point4_lon"));
 			Date date1 = rs.getTimestamp("date_1");
 			Date date2 = rs.getTimestamp("date_2");
 			String sourceURL = rs.getString("source_url");
 			String metaDataURL = rs.getString("metadata_url");
 			String imageURL = rs.getString("preview_img_url");
 			String kmlURL = rs.getString("preview_kml_url");
 			
 			uavsar = new UAVSAR(id, title, description, date1, date2, sourceURL, metaDataURL, imageURL, kmlURL, ref1, ref2, ref3, ref4);
 			uavsar.setDataCategories(getUAVSARCategories(uavsar));		
 		}
 		
 		return uavsar;
 	}
 	
 	public List<UAVSARCategory> getUAVSARCategories(UAVSAR u) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		List<UAVSARCategory> categories = null;
 		if(u != null) {
 			categories = new ArrayList<UAVSARCategory>();
 			PreparedStatement statement = dbConnection.getPreparedStatement("SELECT DISTINCT c.id, c.name FROM uavsar_pri_data AS p, uavsar_data_category AS c WHERE p.category = c.id  AND uavsar_pri_id = ?");
 			statement.setInt(1, u.getId());
 			ResultSet rs = statement.executeQuery();
 			
 			while(rs.next()) {
 				UAVSARCategory c = new UAVSARCategory(rs.getString("id"), rs.getString("name"));
 				c.setDataItems(getUAVSARCategories(u, c));
 				
 				categories.add(c);				
 			}
 		}
 		
 		return categories;
 	}
 	
 	public List<UAVSARDataItem> getUAVSARCategories(UAVSAR u, UAVSARCategory c) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		List<UAVSARDataItem> items = null;
 		if(u != null) {
 			items = new ArrayList<UAVSARDataItem>();
 			PreparedStatement statement = dbConnection.getPreparedStatement("SELECT * FROM uavsar_pri_data WHERE uavsar_pri_id = ? AND category = ?");
 			statement.setInt(1, u.getId());
 			statement.setString(2, c.getId());
 			ResultSet rs = statement.executeQuery();
 			
 			while(rs.next()) {
 				String name = rs.getString("item_name");
 				String url = rs.getString("item_url");
 				String viz_url = rs.getString("item_viz_url");
 				String viz_url_preview = rs.getString("item_viz_preview_url");
 				
 				items.add(new UAVSARDataItem(name, url, viz_url, viz_url_preview));
 			}
 		}
 		
 		return items;
 	}
 	
 	public List<UCERFFault> getUCERFFaults(String dataSetID) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		return getUCERFFaults(dataSetID, null);
 	}
 	
 	public List<UCERFFault> getUCERFFaults(String dataSetID, String orderBy) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		List<UCERFFault> faults = new ArrayList<UCERFFault>();
 		FaultDataSet dataset = getFaultDataSet(dataSetID);
 		
 		PreparedStatement statement;
 		if(orderBy != null && orderBy.equalsIgnoreCase("fault_name"))
 			statement = dbConnection.getPreparedStatement("SELECT * FROM data_ucerf2 WHERE data_set_id = ? ORDER BY " + orderBy + ", entry_id");
 		else
 			statement = dbConnection.getPreparedStatement("SELECT * FROM data_ucerf2 WHERE data_set_id = ? ORDER BY entry_id");
 		
 		statement.setString(1, dataset.getId());		
 		ResultSet rs = statement.executeQuery();
 		while(rs.next()) {
 			UCERFFault f = new UCERFFault(dataset, rs.getString("entry_id"), rs.getString("fault_name"));
 			
 			f.setUpperDepth(rs.getDouble("upper_depth")); if(rs.wasNull()) f.setUpperDepth(null);
 			f.setLowerDepth(rs.getDouble("lower_depth")); if(rs.wasNull()) f.setLowerDepth(null);
 			f.setDipAngle(rs.getDouble("dip_angle")); if(rs.wasNull()) f.setDipAngle(null);
 			f.setDipDirection(rs.getDouble("dip_direction")); if(rs.wasNull()) f.setDipDirection(null);
 			f.setSlipRate(rs.getDouble("slip_rate")); if(rs.wasNull()) f.setSlipRate(null);
 			f.setSlipFactor(rs.getDouble("slip_factor")); if(rs.wasNull()) f.setSlipFactor(null);
 			f.setRake(rs.getDouble("rake")); if(rs.wasNull()) f.setRake(null);
 			f.setTraceLength(rs.getDouble("trace_length")); if(rs.wasNull()) f.setTraceLength(null);
 			
 			String traces = rs.getString("trace_points");
 			String[] tracePointStrings = traces.split("&");
 			
 			for(int i = 0; i < tracePointStrings.length; i++) {
 				String[] tracePoint = tracePointStrings[i].split(",");
 				FaultTracePoint p = new FaultTracePoint(Double.parseDouble(tracePoint[0].trim()), Double.parseDouble(tracePoint[1].trim()), f.getLowerDepth());
 				f.addTracePoint(p);
 			}
 			
 			faults.add(f);
 		}
 		
 		return faults;
 	}
 	
 	public UCERFFault getUCERFFault(String dataSetID, String faultID) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		UCERFFault fault = null;
 		FaultDataSet dataset = getFaultDataSet(dataSetID);
 		
 		
 		PreparedStatement statement = dbConnection.getPreparedStatement("SELECT * FROM data_ucerf2 WHERE data_set_id = ? AND entry_id = ? ORDER BY entry_id");
 		statement.setString(1, dataset.getId());
 		statement.setString(2, faultID);
 		ResultSet rs = statement.executeQuery();
 		if(rs.next()) {
 			fault = new UCERFFault(dataset, rs.getString("entry_id"), rs.getString("fault_name"));
 			fault.setUpperDepth(rs.getDouble("upper_depth")); if(rs.wasNull()) fault.setUpperDepth(null);
 			fault.setLowerDepth(rs.getDouble("lower_depth")); if(rs.wasNull()) fault.setLowerDepth(null);
 			fault.setDipAngle(rs.getDouble("dip_angle")); if(rs.wasNull()) fault.setDipAngle(null);
 			fault.setDipDirection(rs.getDouble("dip_direction")); if(rs.wasNull()) fault.setDipDirection(null);
 			fault.setSlipRate(rs.getDouble("slip_rate")); if(rs.wasNull()) fault.setSlipRate(null);
 			fault.setSlipFactor(rs.getDouble("slip_factor")); if(rs.wasNull()) fault.setSlipFactor(null);
 			fault.setRake(rs.getDouble("rake")); if(rs.wasNull()) fault.setRake(null);
 			fault.setTraceLength(rs.getDouble("trace_length")); if(rs.wasNull()) fault.setTraceLength(null);
 			
 			String traces = rs.getString("trace_points");
 			String[] tracePointStrings = traces.split("&");
 			
 			for(int i = 0; i < tracePointStrings.length; i++) {
 				String[] tracePoint = tracePointStrings[i].split(",");
 				FaultTracePoint p = new FaultTracePoint(Double.parseDouble(tracePoint[0].trim()), Double.parseDouble(tracePoint[1].trim()), fault.getLowerDepth());
 				fault.addTracePoint(p);
 			}
 		}
 		
 		return fault;
 	}
 	
 	public List<CGSFault> getCGSFaults(String dataSetID) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		return getCGSFaults(dataSetID, null);
 	}
 	
 	public List<CGSFault> getCGSFaults(String dataSetID, String orderBy) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		List<CGSFault> faults = new ArrayList<CGSFault>();
 		FaultDataSet dataset = getFaultDataSet(dataSetID);
 		
 		PreparedStatement statement;
 		if(orderBy != null && orderBy.equalsIgnoreCase("fault_name"))
 			statement = dbConnection.getPreparedStatement("SELECT * FROM data_cgs WHERE data_set_id = ? ORDER BY " + orderBy + ", entry_id");
 		else
 			statement = dbConnection.getPreparedStatement("SELECT * FROM data_cgs WHERE data_set_id = ? ORDER BY entry_id");
 		
 		statement.setString(1, dataset.getId());		
 		ResultSet rs = statement.executeQuery();
 		while(rs.next()) {
 			CGSFault f = new CGSFault(dataset, rs.getString("entry_id"), rs.getString("fault_name"));
 			f.setFaultClass(rs.getString("fault_class"));
 			f.setZone(rs.getString("fault_zone"));
 			f.setGeometry(rs.getString("geometry"));
 			f.setGeometryDirection(rs.getString("geometry_direction"));
 			f.setLength(rs.getDouble("length")); if(rs.wasNull()) f.setLength(null);
 			f.setLengthError(rs.getDouble("length_error")); if(rs.wasNull()) f.setLengthError(null);
 			f.setSlipRate(rs.getDouble("slip_rate")); if(rs.wasNull()) f.setSlipRate(null);
 			f.setSlipRateError(rs.getDouble("slip_rate_error")); if(rs.wasNull()) f.setSlipRateError(null);
 			f.setRank(rs.getString("rank"));
 			f.setmMax(rs.getDouble("mmax")); if(rs.wasNull()) f.setmMax(null);
 			f.setCharRate(rs.getDouble("char_rate")); if(rs.wasNull()) f.setCharRate(null);
 			f.setRecurrence(rs.getDouble("recurrence")); if(rs.wasNull()) f.setRecurrence(null);
 			f.setDownDipWidth(rs.getDouble("down_dip_width")); if(rs.wasNull()) f.setDownDipWidth(null);
 			f.setDownDipWidthError(rs.getDouble("down_dip_width_error")); if(rs.wasNull()) f.setDownDipWidthError(null);
 			f.setRupTop(rs.getDouble("rup_top")); if(rs.wasNull()) f.setRupTop(null);
 			f.setRupBottom(rs.getDouble("rup_bot")); if(rs.wasNull()) f.setRupBottom(null);
 			f.setRake(rs.getDouble("rake")); if(rs.wasNull()) f.setRake(null);
 			f.setDip(rs.getDouble("dip")); if(rs.wasNull()) f.setDip(null);
 			f.setDaz(rs.getDouble("daz")); if(rs.wasNull()) f.setDaz(null);
 			f.setComment(rs.getString("comment"));
 			
 			FaultTracePoint start = new FaultTracePoint(rs.getDouble("start_point_lat"), rs.getDouble("start_point_lon"), 0.0);
 			FaultTracePoint end = new FaultTracePoint(rs.getDouble("end_point_lat"), rs.getDouble("end_point_lon"), 0.0);
 			f.addTracePoint(start);
 			f.addTracePoint(end);
 			
 			faults.add(f);
 		}
 		
 		return faults;
 	}
 	
 	public CGSFault getCGSFault(String dataSetID, String faultID) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		CGSFault fault = null;
 		FaultDataSet dataset = getFaultDataSet(dataSetID);
 		
 		
 		PreparedStatement statement = dbConnection.getPreparedStatement("SELECT * FROM data_cgs WHERE data_set_id = ? AND entry_id = ? ORDER BY entry_id");
 		statement.setString(1, dataset.getId());
 		statement.setString(2, faultID);
 		ResultSet rs = statement.executeQuery();
 		if(rs.next()) {
 			fault = new CGSFault(dataset, rs.getString("entry_id"), rs.getString("fault_name"));
 			fault.setFaultClass(rs.getString("fault_class"));
 			fault.setZone(rs.getString("fault_zone"));
 			fault.setGeometry(rs.getString("geometry"));
 			fault.setGeometryDirection(rs.getString("geometry_direction"));
 			fault.setLength(rs.getDouble("length")); if(rs.wasNull()) fault.setLength(null);
 			fault.setLengthError(rs.getDouble("length_error")); if(rs.wasNull()) fault.setLengthError(null);
 			fault.setSlipRate(rs.getDouble("slip_rate")); if(rs.wasNull()) fault.setSlipRate(null);
 			fault.setSlipRateError(rs.getDouble("slip_rate_error")); if(rs.wasNull()) fault.setSlipRateError(null);
 			fault.setRank(rs.getString("rank"));
 			fault.setmMax(rs.getDouble("mmax")); if(rs.wasNull()) fault.setmMax(null);
 			fault.setCharRate(rs.getDouble("char_rate")); if(rs.wasNull()) fault.setCharRate(null);
 			fault.setRecurrence(rs.getDouble("recurrence")); if(rs.wasNull()) fault.setRecurrence(null);
 			fault.setDownDipWidth(rs.getDouble("down_dip_width")); if(rs.wasNull()) fault.setDownDipWidth(null);
 			fault.setDownDipWidthError(rs.getDouble("down_dip_width_error")); if(rs.wasNull()) fault.setDownDipWidthError(null);
 			fault.setRupTop(rs.getDouble("rup_top")); if(rs.wasNull()) fault.setRupTop(null);
 			fault.setRupBottom(rs.getDouble("rup_bot")); if(rs.wasNull()) fault.setRupBottom(null);
 			fault.setRake(rs.getDouble("rake")); if(rs.wasNull()) fault.setRake(null);
 			fault.setDip(rs.getDouble("dip")); if(rs.wasNull()) fault.setDip(null);
 			fault.setDaz(rs.getDouble("daz")); if(rs.wasNull()) fault.setDaz(null);
 			fault.setComment(rs.getString("comment"));
 			
 			FaultTracePoint start = new FaultTracePoint(rs.getDouble("start_point_lat"), rs.getDouble("start_point_lon"), 0.0);
 			FaultTracePoint end = new FaultTracePoint(rs.getDouble("end_point_lat"), rs.getDouble("end_point_lon"), 0.0);
 			fault.addTracePoint(start);
 			fault.addTracePoint(end);
 		}
 		
 		return fault;
 	}
 	
 	public List<NCALFault> getNCALFaults() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		return getNCALFaults(null);
 	}
 	
 	public List<NCALFault> getNCALFaults(String orderBy) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		List<NCALFault> faults = new ArrayList<NCALFault>();
 		FaultDataSet dataset = getFaultDataSet("NCAL");
 		
 		PreparedStatement statement;
 		String query = "SELECT * FROM data_ncal_fault ORDER BY element_number";
 		if(orderBy != null && orderBy.equalsIgnoreCase("fault_name"))
 			query = "SELECT * FROM data_ncal_fault ORDER BY fault_element, segment_element, entry_id";
 
 		ResultSet rs = dbConnection.executeQuery(query);
 		while(rs.next()) {
 			NCALFault f = new NCALFault(dataset, rs.getString("element_number"), rs.getString("name"));
 			
 			f.setFaultElement(rs.getInt("fault_element")); if(rs.wasNull()) f.setFaultElement(null);
 			f.setSegmentElement(rs.getInt("segment_element")); if(rs.wasNull()) f.setSegmentElement(null);
 			f.setSlipRate(rs.getDouble("slip_rate")); if(rs.wasNull()) f.setSlipRate(null);
 			f.setStrength(rs.getDouble("strength")); if(rs.wasNull()) f.setStrength(null);
 			
 			f.setStrike(rs.getDouble("strike")); if(rs.wasNull()) f.setStrike(null);
 			f.setDip(rs.getDouble("dip")); if(rs.wasNull()) f.setDip(null);
 			f.setRake(rs.getDouble("rake")); if(rs.wasNull()) f.setRake(null);
 			
 			FaultTracePoint p1 = new FaultTracePoint(rs.getDouble("lat1"), rs.getDouble("lon1"), rs.getDouble("depth1"));
 			FaultTracePoint p2 = new FaultTracePoint(rs.getDouble("lat2"), rs.getDouble("lon2"), rs.getDouble("depth2"));
 			FaultTracePoint p3 = new FaultTracePoint(rs.getDouble("lat3"), rs.getDouble("lon3"), rs.getDouble("depth3"));
 			FaultTracePoint p4 = new FaultTracePoint(rs.getDouble("lat4"), rs.getDouble("lon4"), rs.getDouble("depth4"));
 			
 			f.addTracePoint(p1);
 			f.addTracePoint(p2);
 			f.addTracePoint(p3);
 			f.addTracePoint(p4);
 			
 			faults.add(f);
 		}
 		
 		return faults;
 	}
 	
 	public NCALFault getNCALFault(String faultID) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
 		NCALFault fault = null;
 		FaultDataSet dataset = getFaultDataSet("NCAL");
 		
 		
 		PreparedStatement statement = dbConnection.getPreparedStatement("SELECT * FROM data_ncal_fault WHERE element_number = ?");
 		statement.setString(1, faultID);
 		ResultSet rs = statement.executeQuery();
 		if(rs.next()) {
 			fault = new NCALFault(dataset, rs.getString("element_number"), rs.getString("name"));
 			
 			fault.setFaultElement(rs.getInt("fault_element")); if(rs.wasNull()) fault.setFaultElement(null);
 			fault.setSegmentElement(rs.getInt("segment_element")); if(rs.wasNull()) fault.setSegmentElement(null);
 			fault.setSlipRate(rs.getDouble("slip_rate")); if(rs.wasNull()) fault.setSlipRate(null);
 			fault.setStrength(rs.getDouble("strength")); if(rs.wasNull()) fault.setStrength(null);
 			
 			fault.setStrike(rs.getDouble("strike")); if(rs.wasNull()) fault.setStrike(null);
 			fault.setDip(rs.getDouble("dip")); if(rs.wasNull()) fault.setDip(null);
 			fault.setRake(rs.getDouble("rake")); if(rs.wasNull()) fault.setRake(null);
 			
 			FaultTracePoint p1 = new FaultTracePoint(rs.getDouble("lat1"), rs.getDouble("lon1"), rs.getDouble("depth1"));
 			FaultTracePoint p2 = new FaultTracePoint(rs.getDouble("lat2"), rs.getDouble("lon2"), rs.getDouble("depth2"));
 			FaultTracePoint p3 = new FaultTracePoint(rs.getDouble("lat3"), rs.getDouble("lon3"), rs.getDouble("depth3"));
 			FaultTracePoint p4 = new FaultTracePoint(rs.getDouble("lat4"), rs.getDouble("lon4"), rs.getDouble("depth4"));
 			
 			fault.addTracePoint(p1);
 			fault.addTracePoint(p2);
 			fault.addTracePoint(p3);
 			fault.addTracePoint(p4);
 		}
 		
 		return fault;
 	}
 	
 	public List<FaultDataSet> getFaultDataSets() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {		
 		List<FaultDataSet> datasets = new ArrayList<FaultDataSet>();
		ResultSet rs = dbConnection.executeQuery("SELECT id, name, nick_name, title, description, download_url, entry_count, data_type FROM data_set_definition ORDER BY nick_name");
 		while(rs.next()) {
			FaultDataSet fds = new FaultDataSet(rs.getString("id"), rs.getString("name"), rs.getString("nick_name"), rs.getString("title"), rs.getString("description"), rs.getString("download_url"), rs.getInt("entry_count"), rs.getString("data_type"));
 			datasets.add(fds);
 		}
 		
 		return datasets;
 	}
 	
 	public FaultDataSet getFaultDataSet(String id) throws SQLException {
 		FaultDataSet dataset = null;
		PreparedStatement statement = dbConnection.getPreparedStatement("SELECT id, name, nick_name, title, description, download_url, entry_count, data_type FROM data_set_definition WHERE id = ? ORDER BY nick_name");
 		statement.setString(1, id);
 		ResultSet rs = statement.executeQuery();
 		if(rs.next()) {
			dataset = new FaultDataSet(rs.getString("id"), rs.getString("name"), rs.getString("nick_name"), rs.getString("title"), rs.getString("description"), rs.getString("download_url"), rs.getInt("entry_count"), rs.getString("data_type"));
 		}
 		
 		return dataset;
 	}
 }
