 package backend.DataService;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import backend.DataTransferObjects.*;
 
 public class DataServiceImpl implements DataService {
 
 	// just for testing
 	public static void main(String[] args) {
 		//String url = "jdbc:mysql://localhost:3306/test";
 		String user = "testuser";
 		String password = "test623";
 
 		DataService serv = DataServiceImpl.create("test", "192.168.0.13:3306", user, password);
 
 		//		PatientDto newPatient = new PatientDto();
 		//		newPatient.setFirst("Dead").setLast("Bowie").setPhone(3215552314L).setNotes("ELE member");
 		//		serv.addPatient(newPatient);
 
 		//        serv.addNewPractitionerType("Homeopathy");
 		//        
 		//        PractitionerDto newPractitioner = new PractitionerDto();
 		//        newPractitioner.setApptLength(60);
 		//        newPractitioner.setFirst("Mitts");
 		//        newPractitioner.setLast("MaGee");
 		//        newPractitioner.setNotes("Not President");
 		//        newPractitioner.setPhone("123456789");
 		//        newPractitioner.setTypeID(1);
 		//        serv.addPractitioner(newPractitioner);
 
 		for (PatientDto patient : serv.queryPatientByName("Dead", "Bowie")) {
 			System.out.println(patient);
 		}
 		//		TypeDto type = new TypeDto();
 		//		type.setField("TypeID", 1);
 		//		serv.addPatientToWaitlist(new PatientDto().setPatID(1), type);
 		for (WaitlistDto entry : serv.getWaitlist()) {
 			System.out.println(entry);
 		}
 		//System.out.println(serv.queryPatientByName("Dead", "Bowie").get(0));
 		serv.close();
 	}
 
 	public static DataService GLOBAL_DATA_INSTANCE = DataServiceImpl.create("ifc_db", "localhost:3306", "testuser", "test623");
 	
 	private final String url;
 	private final String user;
 	private final String password;
 
 	private Connection connection;
 
 	private DataServiceImpl(String url, String user, String password) {
 		// Instantiate this class with DataService.create(String dbName, String serverAddr);
 		this.url = url;
 		this.user = user;
 		this.password = password;
 	}
 
 	/**
 	 * Creates an instance of DataService connected to the specified database service.
 	 * Returns null if a connection cannot be made.
 	 * Don't forget to close the connection after you're done!
 	 * 
 	 * @param dbName example:test
 	 * @param serverAddr example:localhost:3306
 	 * @param username example:testuser
 	 * @param password example:test623
 	 * @return An instance of DataService with an active connection. null if can't connect
 	 */
 	public static DataService create(
 			String dbName, String serverAddr, String username, String password) {
 
 		DataServiceImpl service = new DataServiceImpl(
 				"jdbc:mysql://" + serverAddr + "/" + dbName, username, password);
 
 		try {
 			service.connection =
 				DriverManager.getConnection(service.url, service.user, service.password);
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 			return null;
 		}
 
 		GLOBAL_DATA_INSTANCE = service;
 		return service;
 	}
 
 	@Override
 	public void close() {
 		try {
 			if (connection != null) {
 				connection.close();
 			}
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, "DataService.close() failed.\n" + e.getMessage(), e);
 		}
 	}
 
 	@Override
 	public boolean addPatient(PatientDto patient) {
 		PreparedStatement st = null;
 
 		try {
 			if (patient.getPatID() == null) {
 				st = connection.prepareStatement(
 				"INSERT INTO Patient (FirstName, LastName, PhoneNumber, Notes) VALUES (?, ?, ?, ?)");
 			} else {
 				st = connection.prepareStatement(
 						"INSERT INTO Patient (FirstName, LastName, PhoneNumber, Notes, PatID) " +
 				"VALUES (?, ?, ?, ?, ?)");
 				st.setInt(5, patient.getPatID());
 			}
 			st.setString(1, patient.getFirst());
 			st.setString(2, patient.getLast());
 			st.setString(3, patient.getPhone());
 			st.setString(4, patient.getNotes());
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean updatePatient(String fieldName, Object value, PatientDto patient) {
 		PreparedStatement st = null;
 
 		try {
 			patient.setField(fieldName, value);
 			st = connection.prepareStatement(
 					"UPDATE Patient SET " + fieldName + "=? WHERE PatID=?");
 			st.setObject(1, value);
 			st.setInt(2, patient.getPatID());
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean removePatient(PatientDto patient) {
 		PreparedStatement st = null;
 
 		try {
 			if (patient.getPatID() == null) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, "Tried to delete patient without ID");
 				return false;
 			} else {
 				st = connection.prepareStatement(
 				"DELETE FROM Patient WHERE PatID=?");
 				st.setInt(1, patient.getPatID());
 			}
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
         
         
 
 	@Override
 	public PatientDto getPatient(int PatID) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT Patient.PatID, Patient.`FirstName`, " +
 					"Patient.`LastName`, Patient.PhoneNumber, Patient.Notes, COUNT(NoShow.NoShowID) " +
 					"FROM Patient INNER JOIN NoShow on Patient.`PatID` = NoShow.`PatID`WHERE Patient.PatID =(?)");
 			st.setInt(1, PatID);
 			rs = st.executeQuery();
 			PatientDto patient = new PatientDto();
 			if (rs.next()) {
 				patient.setField(PatientDto.PATIENT_ID, rs.getInt(1));
 				patient.setField(PatientDto.FIRST, rs.getString(2));
 				patient.setField(PatientDto.LAST, rs.getString(3));
 				patient.setField(PatientDto.PHONE, rs.getString(4));
 				patient.setField(PatientDto.NOTES, rs.getString(5));
 				patient.setField(PatientDto.NO_SHOW, rs.getInt(6));
 				
 				return patient;
 			}
 
 			return null;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public List<PatientDto> getAllPatients() {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
                         //Todo: NOShows Claire
 			st = connection.prepareStatement("SELECT * FROM Patient");
 			rs = st.executeQuery();
 			List<PatientDto> results = new ArrayList<PatientDto>();
 			PatientDto patient = new PatientDto();
 			while (rs.next()) {
 				patient.setField(PatientDto.PATIENT_ID, rs.getInt(PatientDto.PATIENT_ID));
 				patient.setField(PatientDto.FIRST, rs.getString(PatientDto.FIRST));
 				patient.setField(PatientDto.LAST, rs.getString(PatientDto.LAST));
 				patient.setField(PatientDto.PHONE, rs.getString(PatientDto.PHONE));
 				patient.setField(PatientDto.NOTES, rs.getString(PatientDto.NOTES));
 				results.add(patient);
 				patient = new PatientDto();
 			}
 			return results;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public List<PatientDto> queryPatientByName(String first, String last) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT * FROM Patient WHERE FirstName=? AND LastName=?");
 			st.setString(1, first);
 			st.setString(2, last);
 			rs = st.executeQuery();
 			List<PatientDto> results = new ArrayList<PatientDto>();
 			PatientDto patient = new PatientDto();
 			while (rs.next()) {
 				// TODO: No shows
 				patient.setField(PatientDto.PATIENT_ID, rs.getInt(PatientDto.PATIENT_ID));
 				patient.setField(PatientDto.FIRST, rs.getString(PatientDto.FIRST));
 				patient.setField(PatientDto.LAST, rs.getString(PatientDto.LAST));
 				patient.setField(PatientDto.PHONE, rs.getString(PatientDto.PHONE));
 				patient.setField(PatientDto.NOTES, rs.getString(PatientDto.NOTES));
 				results.add(patient);
 				patient = new PatientDto();
 			}
 			return results;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public List<NoShowDto> getNoShowsByPatient(int patID) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT * FROM NoShows WHERE PatID=?");
 			st.setInt(1, patID);
 			rs = st.executeQuery();
 			List<NoShowDto> results = new ArrayList<NoShowDto>();
 			NoShowDto noShow = new NoShowDto();
 			while (rs.next()) {
 				// TODO: Will the columns always be the same order?
 				noShow.setField(NoShowDto.NOSHOW_ID, rs.getInt(NoShowDto.NOSHOW_ID));
 				noShow.setField(NoShowDto.PATIENT_ID, rs.getString(NoShowDto.PATIENT_ID));
 				noShow.setField(NoShowDto.DATE, rs.getString(NoShowDto.DATE));
 				results.add(noShow);
 				noShow = new NoShowDto();
 			}
 			return results;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public int getNoShowCountInLastSixMonths(PatientDto patient) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement(
 			"SELECT COUNT(*) FROM NoShows WHERE PatID=? AND NoShowDate>?}");
 			st.setInt(1, patient.getPatID());
 			Calendar today = Calendar.getInstance();
 			today.set(Calendar.HOUR_OF_DAY, 0);
 			today.add(Calendar.MONTH, -6);
 			st.setDate(2, new Date(today.getTime().getTime()));
 			rs = st.executeQuery();
 			// should only return single result.
 			if (rs.next()) {
 				return rs.getInt("COUNT(*)");
 			}
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return -1;
 	}
         
         
 
 	@Override
 	public TypeDto addNewPractitionerType(String serviceType) {
 		PreparedStatement st = null;
 
 		try {
 			st = connection.prepareStatement("INSERT INTO ServiceType (TypeName) VALUES (?)");
 			st.setString(1, serviceType);
 			st.executeUpdate();
 			return this.getType(serviceType);
                         
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean removePractitionerType(String serviceType) {
 		PreparedStatement st = null;
 
 		try {
 			st = connection.prepareStatement("DELETE FROM ServiceType WHERE (TypeName=?)");
 			st.setString(1, serviceType);
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public List<TypeDto> getAllPractitionerTypes() {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT * FROM ServiceType");
 			rs = st.executeQuery();
 			List<TypeDto> results = new ArrayList<TypeDto>();
 			while (rs.next()) {
 				TypeDto type = new TypeDto();
 				type.setField(TypeDto.TYPE_NAME, rs.getString(TypeDto.TYPE_NAME));
 				type.setField(TypeDto.TYPE_ID, rs.getInt(TypeDto.TYPE_ID));
 				results.add(type);
 			}
 			return results;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public List<PractitionerDto> getAllPractitioners() {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT * FROM Practitioner " +
 					"INNER JOIN ServiceType ON Practitioner.`TypeID` = ServiceType.TypeID");
 			rs = st.executeQuery();
 			List<PractitionerDto> results = new ArrayList<PractitionerDto>();
 			PractitionerDto practitioner;
 			while (rs.next()) {
 				practitioner = new PractitionerDto();
 				practitioner.setField(
 						PractitionerDto.PRACT_ID, rs.getInt(PractitionerDto.PRACT_ID));
 				TypeDto type = new TypeDto();
 				type.setField(TypeDto.TYPE_ID, rs.getInt(TypeDto.TYPE_ID));
 				type.setField(TypeDto.TYPE_NAME, rs.getString(TypeDto.TYPE_NAME));
 				practitioner.setField(
 						PractitionerDto.TYPE, type);
 				practitioner.setField(
 						PractitionerDto.FIRST, rs.getString(PractitionerDto.FIRST));
 				practitioner.setField(
 						PractitionerDto.LAST, rs.getString(PractitionerDto.LAST));
 				practitioner.setField(
 						PractitionerDto.APPT_LENGTH, rs.getInt(PractitionerDto.APPT_LENGTH));
 				practitioner.setField(
 						PractitionerDto.PHONE, rs.getString(PractitionerDto.PHONE));
 				practitioner.setField(
 						PractitionerDto.NOTES, rs.getString(PractitionerDto.NOTES));
 				results.add(practitioner);
 			}
 			return results;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public PractitionerDto addPractitioner(int typeID, String first, String last, int appLength, String phone, String notes) {
 		PreparedStatement st = null;
                 ResultSet rs = null;
 
 		try {
 			
 			st = connection.prepareStatement("INSERT INTO Practitioner " +
 					"(TypeID, FirstName, LastName, ApptLength, PhoneNumber, Notes) " +
 			"VALUES (?, ?, ?, ?, ?, ?)");
 			
 			st.setInt(1, typeID);
 			st.setString(2, first);
 			st.setString(3, last); 
 			st.setInt(4, appLength);
 			st.setString(5, phone);
 			st.setString(6, notes);
 			st.executeUpdate();
                         
                         st = connection.prepareStatement(
                         "SELECT Max(PractID) FROM Practitioner"); 
 		
                         rs = st.executeQuery();
                         
                         rs.next();
                         
                         int id = rs.getInt(1);
                         
                         System.out.println(id);
                         
                         st = connection.prepareStatement(
                         "SELECT * FROM Practitioner INNER JOIN ServiceType ON Practitioner.TypeID = " +
 					"ServiceType.TypeID WHERE Practitioner.PractID=?");
                         
                         st.setInt(1, id);
                         
                         rs = st.executeQuery();
                         
                         if(rs.next()){
                             PractitionerDto returnPract = new PractitionerDto();
 
                             returnPract.setField(PractitionerDto.APPT_LENGTH, rs.getInt(PractitionerDto.APPT_LENGTH));
                             returnPract.setField(PractitionerDto.FIRST, rs.getString(PractitionerDto.FIRST));
                             returnPract.setField(PractitionerDto.LAST, rs.getString(PractitionerDto.LAST));
                             returnPract.setField(PractitionerDto.NOTES, rs.getString(PractitionerDto.NOTES));
                             returnPract.setField(PractitionerDto.PHONE, rs.getString(PractitionerDto.PHONE));
                             returnPract.setField(PractitionerDto.PRACT_ID, rs.getInt(PractitionerDto.PRACT_ID));
                             TypeDto type = new TypeDto();
                             type.setField(TypeDto.TYPE_ID, rs.getInt(TypeDto.TYPE_ID));
                             type.setField(TypeDto.TYPE_NAME, rs.getString(TypeDto.TYPE_NAME));
                             returnPract.setField(PractitionerDto.TYPE, type);
 
                             return returnPract;
                         }
 			
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	// TODO: Remove appointments with this practitioner
 	@Override
 	public boolean removePractitioner(PractitionerDto practitioner) {
 		PreparedStatement st = null;
 
 		try {
 			if (practitioner.getPractID() == null) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, "Tried to delete practitioner without ID\n");
 				return false;
 			} else {
 				st = connection.prepareStatement(
 				"DELETE FROM Practitioner WHERE PractID	=?");
 				st.setInt(1, practitioner.getPractID());
 			}
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean updatePractitionerInfo(PractitionerDto practitioner) {
         PreparedStatement st = null;
         ResultSet rs = null;
         try {
 		
 		st = connection.prepareStatement("UPDATE Practitioner" +
 				"SET TypeID=?,FirstName=?,LastName=?,ApptLength=?,PhoneNumber=?,Notes=? " +
 				"WHERE PractID=?" );
 		st.setInt(1, practitioner.getTypeID());
 		st.setString(2, practitioner.getFirst());
 		st.setString(3,practitioner.getLast());
 		st.setInt(4,practitioner.getApptLength());
 		st.setString(5,practitioner.getPhone());
 		st.setString(6,  practitioner.getNotes());
 		st.setInt(7, practitioner.getPractID());
 		
 		
 		rs=st.executeQuery();
 		boolean updated = rs.rowUpdated();
 		return updated;
 		
 	} catch (SQLException e) {
 		Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 		lgr.log(Level.SEVERE, e.getMessage(), e);
 	} finally {
 		try {
 			if (st != null) {
 				st.close();
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 	}
 		return false;
     }
 
 	@Override
 	public List<SchedulePractitionerDto> getAllPractitionersForDay(DayDto day) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
                         //TODO: single query
 			st = connection.prepareStatement(
 					"SELECT * FROM PractitionerScheduled WHERE ScheduleDate =?");
 			st.setDate(1, day.getDate());
 			rs = st.executeQuery();
 			List<SchedulePractitionerDto> retList = new ArrayList<SchedulePractitionerDto>();
 			SchedulePractitionerDto newPract;
 
 			while(rs.next()){
 				newPract = new SchedulePractitionerDto();
 				newPract.setEnd(rs.getInt(SchedulePractitionerDto.END));
 				newPract.setStart(rs.getInt(SchedulePractitionerDto.START));
 				newPract.setField(SchedulePractitionerDto.PRACT_SCHED_ID, 
 						rs.getInt(SchedulePractitionerDto.PRACT_SCHED_ID));
 				newPract.setField(SchedulePractitionerDto.PRACT, 
 						this.getPractitioner(rs.getInt("PractID")));
 				newPract.setField(SchedulePractitionerDto.APPOINTMENTS, 
 						this.getAllAppointments(newPract.getPractSchedID()));
 				retList.add(newPract);
 			}
 			return retList;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean removePractitionerFromDay(int practSchedId, DayDto day) {
 		PreparedStatement st = null;
 		try {
 			st = connection.prepareStatement("DELETE FROM PractitionerScheduled WHERE PractSchID = ?");
 			st.setInt(1, practSchedId);
 			st.executeUpdate();
 			st = connection.prepareStatement("DELETE FROM Appointment WHERE PractSchID = ?");
 			st.setInt(1, practSchedId);
 			st.executeUpdate();
 			return true;
 		}
 		catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e + " : appointment without patient being" +
 			" checked as no show");
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 
 		}
 		return false;
 	}
 
 	@Override
 	public boolean changePractitionerHoursForDay(SchedulePractitionerDto pract,
 			DayDto day, int start, int end) {
 		PreparedStatement st = null;
 
 		try {
 			//delete previous appointments
 			st = connection.prepareStatement("DELETE FROM Appointment WHERE PractSchID = ?");
 			st.setInt(1, pract.getPractSchedID());
 			st.executeQuery();
 
 			//set hours
 			pract.setStart(start);
 			pract.setEnd(end);
 
 			AppointmentDto newApt = new AppointmentDto();
 
 			List<AppointmentDto> appointments = new ArrayList<AppointmentDto>();
 
 			pract.setField(SchedulePractitionerDto.APPOINTMENTS, appointments);;
 
 			st = connection.prepareStatement(
 					"INSERT INTO Appointment (PractSchedID, StartTime, EndTime, ApptDate) VALUES (?, ?, ?, ?)");
 
 			for (int i = start; i < end; i+=pract.getPractitioner().getApptLength()){
 				newApt = new AppointmentDto();
 				newApt.setEnd(i + pract.getPractitioner().getApptLength());
 				st.setInt(3, i + pract.getPractitioner().getApptLength());
 				newApt.setStart(i);
 				st.setInt(2, i);
 				newApt.setField(AppointmentDto.APPT_DATE, day.getDate());
 				st.setDate(4, day.getDate());
 				newApt.setField(AppointmentDto.PRACT_SCHED_ID, pract.getPractSchedID());
 				st.setInt(1, pract.getPractSchedID());
 				appointments.add(newApt);
 				st.executeQuery();
 			}
 
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;    
 	}
 
 	@Override
 	public boolean addPatientToAppointment(int patID, AppointmentDto appointment) {
         PreparedStatement st = null;
         ResultSet rs = null;
         try {
 		
 		st = connection.prepareStatement("UPDATE Appointment " +
 				"SET Appointment.PatID=? WHERE Appointment.ApptID=?" );
 		st.setInt(1, patID);
 		st.setInt(2, appointment.getApptID());
 		
 		rs=st.executeQuery();
 		boolean updated = rs.rowUpdated();
 		return updated;
 		
 	} catch (SQLException e) {
 		Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 		lgr.log(Level.SEVERE, e.getMessage(), e);
 	} finally {
 		try {
 			if (st != null) {
 				st.close();
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 	}
 		return false;
     }
 	
 
 	@Override
 	public boolean removePatientFromAppointment(AppointmentDto appointment) {
         PreparedStatement st = null;
         ResultSet rs = null;
         try {
 		
 		st = connection.prepareStatement("UPDATE Appointment " +
 				"SET Appointment.PatID=NULL WHERE Appointment.ApptID=?" );
 		st.setInt(1, appointment.getApptID());
 		
 		rs=st.executeQuery();
 		boolean updated = rs.rowUpdated();
 		return updated;
 		
 	} catch (SQLException e) {
 		Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 		lgr.log(Level.SEVERE, e.getMessage(), e);
 	} finally {
 		try {
 			if (st != null) {
 				st.close();
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 	}
 		return false;
     }
 
 	@Override
 	public boolean checkAsNoShow(AppointmentDto appointment) {
 		PreparedStatement st = null;
 
 		try {
 			int patID = appointment.getPatientID();
 			Date date = appointment.getApptDate();
 			st = connection.prepareStatement("INSERT INTO NoShow " +
 					"(PatID, NoShowDate) " +
 			"VALUES (?, ?)");
 			st.setInt(1, patID);
 			st.setDate(2, date);
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} catch (NullPointerException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e + " : appointment without patient being" +
 			" checked as no show");
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean uncheckAsNoShow(AppointmentDto appointment) {
 		PreparedStatement st = null;
 
 		try {
 			int patID = appointment.getPatientID();
 			Date date = appointment.getApptDate();
 			st = connection.prepareStatement("DELETE FROM NoShow WHERE " +
 			"patID=? AND NoShowDate=?");
 			st.setInt(1, patID);
 			st.setDate(2, date);
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} catch (NullPointerException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e + " : appointment without patient being" +
 			" checked as no show");
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public WaitlistDto addPatientToWaitlist(PatientDto patient, TypeDto type, String comments) {
 		PreparedStatement st = null;
 		try {
 			st = connection.prepareStatement("INSERT INTO Waitlist " +
 					"(PatID, TypeID, DatetimeEntered, Comments) " +
 			"VALUES (?, ?, ?, ?)");
 			st.setInt(1, patient.getPatID());
 			st.setInt(2, type.getTypeID());
 			st.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
                         st.setString(4, comments);
 			st.executeUpdate();
 			return null; //Todo: changge return type
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean removePatientFromWaitlist(PatientDto patient, TypeDto type) {
 		PreparedStatement st = null;
 
 		try {
 			st = connection.prepareStatement("DELETE FROM Waitlist WHERE " +
 			"PatID=? AND TypeID=?");
 			st.setInt(1, patient.getPatID());
 			st.setInt(2, type.getTypeID());
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean commentWaitlist(WaitlistDto entry, String comment) {
 		PreparedStatement st = null;
 
 		try {
 			st = connection.prepareStatement("UPDATE Waitlist SET Comment=? " +
 			"WHERE WaitlistID=?");
 			st.setString(1, comment);
 			st.setInt(2, entry.getWaitlistID());
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public List<WaitlistDto> getWaitlist() {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT * FROM Waitlist INNER JOIN Patient " +
 					"ON Waitlist.PatID=Patient.PatID INNER JOIN ServiceType ON" +
 					" Waitlist.TypeID = ServiceType.TypeID");
 			rs = st.executeQuery();
 			List<WaitlistDto> results = new ArrayList<WaitlistDto>();
 			while (rs.next()) {
 				WaitlistDto entry = new WaitlistDto();
 				PatientDto patient = new PatientDto();
 				entry.setField(WaitlistDto.WAITLIST_ID, rs.getInt(WaitlistDto.WAITLIST_ID));
 				TypeDto type = new TypeDto();
 				type.setField(TypeDto.TYPE_ID, rs.getInt(TypeDto.TYPE_ID));
 				type.setField(TypeDto.TYPE_NAME, rs.getString(TypeDto.TYPE_NAME));
 				entry.setField(WaitlistDto.TYPE, type);
 				entry.setField(WaitlistDto.DATE, rs.getDate(WaitlistDto.DATE));
 				entry.setField(WaitlistDto.COMMENTS, rs.getString(WaitlistDto.COMMENTS));
 				patient.setField(PatientDto.PATIENT_ID, rs.getInt(PatientDto.PATIENT_ID));
 				patient.setField(PatientDto.FIRST, rs.getString(PatientDto.FIRST));
 				patient.setField(PatientDto.LAST, rs.getString(PatientDto.LAST));
 				patient.setField(PatientDto.PHONE, rs.getString(PatientDto.PHONE));
 				patient.setField(PatientDto.NOTES, rs.getString(PatientDto.NOTES));
 				entry.setPatient(patient);
 				results.add(entry);
 			}
 			return results;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	//TODO set hours for inputs
 	public boolean setHoursForDay(DayDto day, int start, int end) {
 		PreparedStatement st = null;
 
 		try {
 			st = connection.prepareStatement("UPDATE Day " +
 					"SET StartTime=?, EndTime=? " +
 					"WHERE DayDate=?");
 			st.setInt(1, start);
 			st.setInt(2, end);
 			st.setDate(3, day.getDate());
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean setStatus(DayDto day) {
 		// TODO there's no status field for day... we could set the hours to null?
 		return false;
 	}
 
 	@Override
 	public SchedulePractitionerDto addPractitionerToDay(PractitionerDto pract, DayDto day, 
 			int start, int end){
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement(
 					"INSERT INTO PractitionerScheduled (PractID, ScheduleDate, StartTime, EndTime) VALUES (?, ?, ?, ?)");
 			st.setInt(1, pract.getPractID());
 			st.setDate(2, day.getDate());
 			st.setInt(3, start);
 			st.setInt(4, end);
 			st.executeUpdate();
                         st = connection.prepareStatement("SELECT Max(PractSchID) FROM PractitionerScheduled");
 			rs = st.executeQuery();
 			rs.next();
 
 			int pract_id = rs.getInt(1);
 
 			AppointmentDto newApt = new AppointmentDto();
 			SchedulePractitionerDto returnDto = new SchedulePractitionerDto();
 
 			List<AppointmentDto> appointments = new ArrayList<AppointmentDto>();
 
 			returnDto.setField(SchedulePractitionerDto.DATE, day.getDate());
 			returnDto.setField(SchedulePractitionerDto.APPOINTMENTS, appointments);
 			returnDto.setField(SchedulePractitionerDto.PRACT, pract);
 			returnDto.setField(SchedulePractitionerDto.END, end);
 			returnDto.setField(SchedulePractitionerDto.START, start);
 			returnDto.setField(SchedulePractitionerDto.PRACT_SCHED_ID, pract_id);
 
 			st = connection.prepareStatement(
 					"INSERT INTO Appointment (PractSchedID, StartTime, EndTime, ApptDate) VALUES (?, ?, ?, ?)");
 
 			for (int i = start; i < end; i+=pract.getApptLength()){
 				newApt = new AppointmentDto();
 				newApt.setEnd(i + pract.getApptLength());
 				st.setInt(3, i + pract.getApptLength());
 				newApt.setStart(i);
 				st.setInt(2, i);
 				newApt.setField(AppointmentDto.APPT_DATE, day.getDate());
 				st.setDate(4, day.getDate());
 				newApt.setField(AppointmentDto.PRACT_SCHED_ID, pract_id);
 				st.setInt(1, pract_id);
 				appointments.add(newApt);
 				st.executeUpdate();
 			}
                         return returnDto;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;    
 	}
 
 	@Override
 	public DayDto getOrCreateDay(Date date) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		DayDto retDay = new DayDto(); 
 
 		try {
 			st = connection.prepareStatement(
 					"SELECT * FROM Day WHERE DayDate=?");
 			st.setDate(1, date);
 			rs = st.executeQuery();
 
 			if (rs.next()){
 				retDay.setField(DayDto.DATE, date);
 				retDay.setStart(rs.getInt(DayDto.START));
 				retDay.setEnd(rs.getInt(DayDto.END));
 				return retDay;
 			}
 			else{
 				st = connection.prepareStatement("INSERT INTO Day (DayDate, StartTime, EndTime) VALUES (?, ?, ?)");
 				retDay.setField(DayDto.DATE, date);
 				st.setDate(1, date);
 				retDay.setStart(gui.Constants.DEFAULT_START_TIME);
 				st.setInt(2, gui.Constants.DEFAULT_START_TIME);
 				retDay.setEnd(gui.Constants.DEFAULT_END_TIME);
 				st.setInt(3, gui.Constants.DEFAULT_END_TIME);
 				st.executeUpdate();
 				return retDay;
 			}
 
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return retDay;
 	}
 
 	@Override
 	public PractitionerDto getPractitioner(int practID) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT * FROM Practitioner " +
 					"INNER JOIN ServiceType ON Practitioner.TypeID = " +
 					"ServiceType.TypeID WHERE Practitioner.PractID=(?)");
 			st.setInt(1, practID);
 			rs = st.executeQuery();
 			PractitionerDto pract = new PractitionerDto();
 
 			if (rs.next()) {
 				pract.setField(PractitionerDto.FIRST, rs.getString(PractitionerDto.FIRST));
 				pract.setField(PractitionerDto.LAST, rs.getString(PractitionerDto.LAST));
 				pract.setField(PractitionerDto.APPT_LENGTH, 
 						rs.getInt(PractitionerDto.APPT_LENGTH));
 				pract.setField(PractitionerDto.NOTES, rs.getString(PractitionerDto.NOTES));
 				pract.setField(PractitionerDto.PHONE, rs.getString(PractitionerDto.PHONE));
 				pract.setField(PractitionerDto.PRACT_ID, rs.getString(PractitionerDto.PRACT_ID));
 				TypeDto type = new TypeDto();
 				type.setField(TypeDto.TYPE_ID, rs.getInt(TypeDto.TYPE_ID));
 				type.setField(TypeDto.TYPE_NAME, rs.getString(TypeDto.TYPE_NAME));
 				pract.setField(PractitionerDto.TYPE, type);
                                 return pract;
 			}
 			return null;
 
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public List<AppointmentDto> getAllAppointments(int schedPractId) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement(
 					"SELECT * FROM Appointment WHERE PractSchedID = ?");
 			st.setInt(1, schedPractId);
 			rs = st.executeQuery();
 
 			List<AppointmentDto> retList = new ArrayList<AppointmentDto>();
 			AppointmentDto newAppointment;
 
 			while(rs.next()){
 				newAppointment = new AppointmentDto();
 				newAppointment.setField(AppointmentDto.APPT_DATE, 
 						rs.getDate(AppointmentDto.APPT_DATE));
 				newAppointment.setField(AppointmentDto.APPT_ID, 
 						rs.getInt(AppointmentDto.APPT_ID));
 				newAppointment.setField(AppointmentDto.END, 
 						rs.getInt(AppointmentDto.END));
 				newAppointment.setField(AppointmentDto.NOTE, 
 						rs.getString(AppointmentDto.NOTE));
 				newAppointment.setField(AppointmentDto.NO_SHOW_ID,
 						rs.getInt(AppointmentDto.NO_SHOW_ID));
 				newAppointment.setField(AppointmentDto.PAT_ID,
 						rs.getInt(AppointmentDto.PAT_ID));
 				newAppointment.setField(AppointmentDto.PRACT_SCHED_ID, 
 						rs.getInt(AppointmentDto.PRACT_SCHED_ID));
 				newAppointment.setField(AppointmentDto.START, 
 						rs.getInt(AppointmentDto.START));
 				newAppointment.setField(AppointmentDto.CONFIRMATION,
 						rs.getInt(AppointmentDto.CONFIRMATION)==1);
 				retList.add(newAppointment);
 			}
 			return retList;
 
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean confirmAppointment(AppointmentDto appointment) {
 		PreparedStatement st = null;
 
 		try {
 			appointment.setConfirmation(true);
 			st = connection.prepareStatement(
 					"UPDATE Appointment SET Confirmation=1" +
 					"WHERE ApptID=?");
 			st.setInt(1, appointment.getApptID());
 			st.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, e.getMessage(), e);
 		} finally {
 			try {
 				if (st != null) {
 					st.close();
 				}
 			} catch (SQLException ex) {
 				Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 				lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 		return false;
 	}
 
     @Override
     public PatientDto addPatient(String phone, String first, String last, String notes) {
         PreparedStatement st = null;
         ResultSet rs = null;
 	try {
 		
 		st = connection.prepareStatement(
 			"INSERT INTO Patient (FirstName, LastName, PhoneNumber, Notes) VALUES (?, ?, ?, ?)");
 		
 		st.setString(1, first);
 		st.setString(2, last);
 		st.setString(3, phone);
 		st.setString(4, notes);
 			st.executeUpdate();
                         
                 st = connection.prepareStatement(
                         "SELECT Max(PatID) FROM Patient");
 		rs = st.executeQuery();
                 rs.next();
                 
                 int newId = rs.getInt(1);
                 
                 st = connection.prepareStatement("SELECT * FROM Patient WHERE PatID=?");
                 
                 st.setInt(1, newId);
                 
                 rs = st.executeQuery();
                 
                 if(rs.next()){
                 PatientDto returnPatient = new PatientDto();
                     returnPatient.setField(PatientDto.PATIENT_ID, rs.getInt(PatientDto.PATIENT_ID));
                     returnPatient.setFirst(first);
                     returnPatient.setLast(last);
                     returnPatient.setNotes(notes);
                     returnPatient.setPhone(phone);
                     returnPatient.setField(PatientDto.NO_SHOW, 0);
 
                     return returnPatient;
                 }
 	} catch (SQLException e) {
 		Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 		lgr.log(Level.SEVERE, e.getMessage(), e);
 	} finally {
 		try {
 			if (st != null) {
 				st.close();
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 	}
 	return null;
     }
 
     @Override
     public TypeDto getType(String type) {
         PreparedStatement st = null;
         ResultSet rs = null;
         try {
 		
 		st = connection.prepareStatement("SELECT * FROM ServiceType WHERE TypeName=?");
 		
 		st.setString(1, type);
                 rs = st.executeQuery();
                         
                 if (rs.next()){
                     TypeDto returnType = new TypeDto();
                     returnType.setField(TypeDto.TYPE_ID, rs.getInt(TypeDto.TYPE_ID));
                     returnType.setField(TypeDto.TYPE_NAME, type);
                     return returnType;
                 }
                 else {
                     return null;
                 }
 	} catch (SQLException e) {
 		Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 		lgr.log(Level.SEVERE, e.getMessage(), e);
 	} finally {
 		try {
 			if (st != null) {
 				st.close();
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 	}
         return null;
     }
 
     @Override
     public boolean updatePatient(PatientDto patient) {
         PreparedStatement st = null;
         ResultSet rs = null;
         try {
 		
 		st = connection.prepareStatement("UPDATE Patient " +
 				"SET Patient.FirstName=?, Patient.LastName=?, Patient.PhoneNumber=?, Patient.Notes=?" +
 				" WHERE PatID = ?");
 		st.setString(1, patient.getFirst());
 		st.setString(2,patient.getLast());
 		st.setString(3,patient.getPhone());
 		st.setString(4,patient.getNotes());
 		st.setInt(5,patient.getPatID());
 		
 		rs=st.executeQuery();
 		boolean updated = rs.rowUpdated();
 		return updated;
 		
 	} catch (SQLException e) {
 		Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 		lgr.log(Level.SEVERE, e.getMessage(), e);
 	} finally {
 		try {
 			if (st != null) {
 				st.close();
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 	}
 		return false;
     }
 
     @Override
     public boolean updateWaitlist(WaitlistDto wl) {
         PreparedStatement st = null;
         ResultSet rs = null;
         try {
 		
 		st = connection.prepareStatement("UPDATE Waitlist " +
 				"SET Waitlist.PatID=?, Waitlist.TypeID=?, Waitlist.DatetimeEntered=?, Waitlist.Comments=?" +
 				" WHERE Waitlist.WaitlistID = ?");
 		st.setInt(1, wl.getPatientID());
 		st.setInt(2,wl.getTypeID());
 		// This seems worrisome? will date work this way?
 		st.setDate(3,wl.getDate());
 		st.setString(4,wl.getComments());
 		st.setInt(5,wl.getWaitlistID());
 		
 		rs=st.executeQuery();
 		boolean updated = rs.rowUpdated();
 		return updated;
 		
 	} catch (SQLException e) {
 		Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 		lgr.log(Level.SEVERE, e.getMessage(), e);
 	} finally {
 		try {
 			if (st != null) {
 				st.close();
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 	}
 		return false;
     }
     @Override
     public boolean removePatientFromWaitlist(WaitlistDto patient) {
         PreparedStatement st = null;
         ResultSet rs = null;
         try {
 		
 		st = connection.prepareStatement("DELETE FROM Waitlist WHERE PatID = ?");
 		
 		st.setInt(1, patient.getPatientID());
                rs = st.executeQuery();
                boolean deleted = rs.rowDeleted();
                return deleted;
                 
 	} catch (SQLException e) {
 		Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 		lgr.log(Level.SEVERE, e.getMessage(), e);
 	} finally {
 		try {
 			if (st != null) {
 				st.close();
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 	}
 		return false;
     }
 }
