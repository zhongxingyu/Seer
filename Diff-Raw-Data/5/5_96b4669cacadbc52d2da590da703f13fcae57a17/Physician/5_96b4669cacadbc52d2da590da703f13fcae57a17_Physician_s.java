 package puf.m2.hms.model;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import puf.m2.hms.exception.HmsException;
 import puf.m2.hms.exception.PhysicianException;
 
 public class Physician extends HmsEntity {
 
 	private static final Map<Integer, Physician> PHYSICIAN_MAP = new CacheAwareMap<Integer, Physician>();
 
     @DbProp
 	private String name;
     @DbProp
     private String role;
     @DbProp
     private boolean available;
 
     public Physician(String name, String role, boolean available) {
 
         this.name = name;
         this.role = role;
         this.available = available;
     }
 
	public static List<Physician> getDoctors() throws PhysicianException {
 		List<Physician> doctorList = new ArrayList<Physician>();
 
 		final String query = "select * from Physician where role = 'Doctor'";
 
 		DB.createConnection();
 
 		ResultSet rs = DB.executeQuery(query);
 		try {
 			while (rs.next()) {
 				int id = rs.getInt("id");
 				Physician physician = PHYSICIAN_MAP.get(id);
 
 				if (physician == null) {
 					boolean available = true;
 					if (rs.getInt("available") == 0) {
 						available = false;
 					}
 					physician = new Physician(rs.getString("name"), "Doctor",
 							available);
 					physician.id = id;
 
 					PHYSICIAN_MAP.put(id, physician);
 				}
 
 				doctorList.add(physician);
 			}
 		} catch (SQLException e) {
			e.printStackTrace();
 		}
 		DB.closeConnection();
 
 		return doctorList;
 	}
 
 	public static List<Physician> getNurses() throws PhysicianException {
 		List<Physician> doctorList = new ArrayList<Physician>();
 
 		final String query = "select * from Physician where role = 'Nurse'";
 
 		DB.createConnection();
 
 		ResultSet rs = DB.executeQuery(query);
 
 		try {
 			while (rs.next()) {
 				int id = rs.getInt("id");
 				Physician physician = PHYSICIAN_MAP.get(id);
 
 				if (physician == null) {
 					boolean available = true;
 					if (rs.getInt("available") == 0) {
 						available = false;
 					}
 					physician = new Physician(rs.getString("name"), "Nurse",
 							available);
 					physician.id = id;
 
 					PHYSICIAN_MAP.put(id, physician);
 				}
 
 				doctorList.add(physician);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		DB.closeConnection();
 		return doctorList;
 	}
 
 	public static Physician getPhysicianById(int id) throws HmsException {
 
 		Physician physician = PHYSICIAN_MAP.get(id);
 		if (physician != null) {
 			return physician;
 		}
 
 		DB.createConnection();
 
 		final String queryTempl = "SELECT * FROM Physician WHERE id = {0}";
 		ResultSet rs = DB.executeQuery(MessageFormat.format(queryTempl, id));
 
 		try {
 			if (rs.next()) {
 				boolean available = true;
 				if (rs.getInt("available") == 0) {
 					available = false;
 				}
 
 				physician = new Physician(rs.getString("name"),
 						rs.getString("role"), available);
 				physician.id = rs.getInt("id");
 				PHYSICIAN_MAP.put(physician.getId(), physician);
 			}
 		} catch (SQLException e) {
 		    throw new HmsException(e);
 		}
 
 		DB.closeConnection();
 
 		return physician;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getRole() {
 		return role;
 	}
 
 	public boolean isAvailable() {
 		return available;
 	}
 
 	public void save() throws HmsException {
 		super.save();
 		PHYSICIAN_MAP.put(id, this);
 
 	}
 
 	public void setAvailable(boolean available) {
 		this.available = available;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setRole(String role) {
 		this.role = role;
 	}
 
 	public void update() throws HmsException {
 		super.update();
 
 	}
 	
     public static void main (String[] args) throws Exception {
         Physician p = getPhysicianById(100);
         p.update();
         
     }
 
 }
