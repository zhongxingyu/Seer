 package backend.DataService;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Time;
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
 
 	public static DataService GLOBAL_DATA_INSTANCE = DataServiceImpl.create("", "", "", "");
 	
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
 			st.setLong(3, patient.getPhone()); //TODO: npe when getPhone returns null
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
 			st = connection.prepareStatement("SELECT * FROM Patient WHERE PatID=(?)");
 			st.setInt(1, PatID);
 			rs = st.executeQuery();
 			PatientDto patient = new PatientDto();
 			if (rs.next()) {
 				patient.setField(PatientDto.PATIENT_ID, rs.getInt(1));
 				patient.setField(PatientDto.FIRST, rs.getString(2));
 				patient.setField(PatientDto.LAST, rs.getString(3));
 				patient.setField(PatientDto.PHONE, rs.getLong(4));
 				patient.setField(PatientDto.NOTES, rs.getString(5));
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
 			st = connection.prepareStatement("SELECT * FROM Patient");
 			rs = st.executeQuery();
 			List<PatientDto> results = new ArrayList<PatientDto>();
 			PatientDto patient = new PatientDto();
 			while (rs.next()) {
 				patient.setField(PatientDto.PATIENT_ID, rs.getInt(PatientDto.PATIENT_ID));
 				patient.setField(PatientDto.FIRST, rs.getString(PatientDto.FIRST));
 				patient.setField(PatientDto.LAST, rs.getString(PatientDto.LAST));
 				patient.setField(PatientDto.PHONE, rs.getLong(PatientDto.PHONE));
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
 				// TODO: Will the columns always be the same order?
 				patient.setField(PatientDto.PATIENT_ID, rs.getInt(PatientDto.PATIENT_ID));
 				patient.setField(PatientDto.FIRST, rs.getString(PatientDto.FIRST));
 				patient.setField(PatientDto.LAST, rs.getString(PatientDto.LAST));
 				patient.setField(PatientDto.PHONE, rs.getLong(PatientDto.PHONE));
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
 	public boolean addNewPractitionerType(String serviceType) {
 		PreparedStatement st = null;
 
 		//TODO: have this return the ID of the this object instead if possible
 		try {
 			st = connection.prepareStatement("INSERT INTO ServiceType (TypeName) VALUES (?)");
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
	public List<TypeDto> getAllPractitionerTypers() {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT * FROM ServiceType");
 			rs = st.executeQuery();
 			List<TypeDto> results = new ArrayList<TypeDto>();
 			while (rs.next()) {
 				TypeDto type = new TypeDto();
 				type.setField(TypeDto.TYPE_NAME, rs.getString(TypeDto.TYPE_NAME));
 				type.setField(TypeDto.TYPE_ID, rs.getString(TypeDto.TYPE_ID));
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
 			st = connection.prepareStatement("SELECT * FROM Practitioner");
 			rs = st.executeQuery();
 			List<PractitionerDto> results = new ArrayList<PractitionerDto>();
 			PractitionerDto practitioner;
 			while (rs.next()) {
 				practitioner = new PractitionerDto();
 				practitioner.setField(
 						PractitionerDto.PRACT_ID, rs.getInt(PractitionerDto.PRACT_ID));
 				practitioner.setField(
 						PractitionerDto.TYPE_ID, rs.getString(PractitionerDto.TYPE_ID));
 				practitioner.setField(
 						PractitionerDto.FIRST, rs.getString(PractitionerDto.FIRST));
 				practitioner.setField(
 						PractitionerDto.LAST, rs.getString(PractitionerDto.LAST));
 				practitioner.setField(
 						PractitionerDto.APPT_LENGTH, rs.getString(PractitionerDto.APPT_LENGTH));
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
 	public boolean addPractitioner(PractitionerDto practitioner) {
 		PreparedStatement st = null;
 
 		try {
 			if (practitioner.getPractID() == null) {
 				st = connection.prepareStatement("INSERT INTO Practitioner " +
 						"(TypeID, FirstName, LastName, ApptLength, PhoneNumber, Notes) " +
 				"VALUES (?, ?, ?, ?, ?, ?)");
 			} else {
 				st = connection.prepareStatement("INSERT INTO Practitioner " +
 						"(TypeID, FirstName, LastName, ApptLength, PhoneNumber, Notes, PractID) " +
 				"VALUES (?, ?, ?, ?, ?, ?, ?)");
 				st.setInt(7, practitioner.getPractID());
 			}
 			st.setInt(1, practitioner.getTypeID());
 			st.setString(2, practitioner.getFirst());
 			st.setString(3, practitioner.getLast()); //TODO: npe when getPhone returns null
 			st.setInt(4, practitioner.getApptLength());
 			st.setString(5, practitioner.getPhone());
 			st.setString(6, practitioner.getNotes());
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
 		if (practitioner.getPractID() == null) {
 			Logger lgr = Logger.getLogger(DataServiceImpl.class.getName());
 			lgr.log(Level.SEVERE, "Tried to update practitioner without ID.\n");
 			return false;
 		}
 		// TODO: check that the ID exists in the table.
 		return addPractitioner(practitioner);
 	}
 
 	@Override
 	public List<SchedulePractitionerDto> getAllPractitionersForDay(DayDto day) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement(
 					"SELECT * FROM PractitionerScheduled WHERE ScheduleDate = ?");
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
 						this.getAllAppointments(rs.getInt(newPract.getPractSchedID())));
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
 			st.executeQuery();
 			st = connection.prepareStatement("DELETE FROM Appointment WHERE PractSchID = ?");
 			st.setInt(1, practSchedId);
 			st.executeQuery();
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
 
 			for (int i = start; i < end; i+=pract.getPractioner().getApptLength()){
 				newApt = new AppointmentDto();
 				newApt.setEnd(i + pract.getPractioner().getApptLength());
 				st.setInt(3, i + pract.getPractioner().getApptLength());
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
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean removePatientFromAppointment(AppointmentDto appointment) {
 		// TODO Auto-generated method stub
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
 	public boolean addPatientToWaitlist(PatientDto patient, TypeDto type) {
 		PreparedStatement st = null;
 
 		try {
 			st = connection.prepareStatement("INSERT INTO Waitlist " +
 					"(PatID, TypeID, DatetimeEntered) " +
 			"VALUES (?, ?, ?)");
 			st.setInt(1, patient.getPatID());
 			st.setInt(2, type.getTypeID());
 			st.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
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
 	public List<WaitlistDto> getWaitlist() {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT * FROM Waitlist");
 			rs = st.executeQuery();
 			List<WaitlistDto> results = new ArrayList<WaitlistDto>();
 			while (rs.next()) {
 				WaitlistDto entry = new WaitlistDto();
 				entry.setField(WaitlistDto.WAITLIST_ID, rs.getInt(WaitlistDto.WAITLIST_ID));
 				entry.setField(WaitlistDto.PATIENT, rs.getString(WaitlistDto.PATIENT));
 				entry.setField(WaitlistDto.TYPE_ID, rs.getString(WaitlistDto.TYPE_ID));
 				entry.setField(WaitlistDto.DATE, rs.getString(WaitlistDto.DATE));
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
 	public boolean setHoursForDay(DayDto day) {
 		PreparedStatement st = null;
 
 		try {
 			st = connection.prepareStatement("INSERT INTO Day " +
 					"(DayDate, StartTime, EndTime) " +
 			"VALUES (?, ?, ?)");
 			st.setDate(1, day.getDate());
 			st.setTime(2, new Time(day.getStart()));
 			st.setTime(3, new Time(day.getEnd()));
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
 			System.out.println(pract_id);
 
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
 		return null;    
 	}
 
 	@Override
 	public DayDto getOrCreateDay(Date date) {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		DayDto retDay = new DayDto(); 
 
 		try {
 			st = connection.prepareStatement(
 					"SELECT * WHERE DayDate = ?");
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
 				//retDfay.setStart(default start);
 				//st.setInt(2, defaut start);
 				//retDay.setEnd(default end);
 				//st.setInt(3, default end);
 				st.executeQuery();
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
 			st = connection.prepareStatement("SELECT * FROM Practitioner WHERE PractID=(?)");
 			st.setInt(1, practID);
 			rs = st.executeQuery();
 			PractitionerDto pract = new PractitionerDto();
 
 			if (rs.next()) {
 				pract.setField(PractitionerDto.FIRST, rs.getInt(PractitionerDto.FIRST));
 				pract.setField(PractitionerDto.LAST, rs.getInt(PractitionerDto.LAST));
 				pract.setField(PractitionerDto.APPT_LENGTH, 
 						rs.getInt(PractitionerDto.APPT_LENGTH));
 				pract.setField(PractitionerDto.NOTES, rs.getString(PractitionerDto.NOTES));
 				pract.setField(PractitionerDto.PHONE, rs.getString(PractitionerDto.PHONE));
 				pract.setField(PractitionerDto.PRACT_ID, rs.getString(PractitionerDto.PRACT_ID));
 				pract.setField(PractitionerDto.TYPE_ID, rs.getString(PractitionerDto.TYPE_ID));
 				//TODO:get TypeName
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
 }
 
