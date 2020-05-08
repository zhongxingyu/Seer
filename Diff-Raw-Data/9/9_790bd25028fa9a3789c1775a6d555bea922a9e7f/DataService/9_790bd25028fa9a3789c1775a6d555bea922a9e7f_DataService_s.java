 package backend.DataService;
 
 import java.util.List;
 
 import java.sql.Date;
 
 import backend.DataTransferObjects.*;
 
 /**
  * Interface for the data service backend for scheduler database.
  * The implementation of this interface will have a static create method
  * for generating instances of this data service. Methods in this interface
  * are used to query data from the database.
  * TODO: add more methods as needed and we will try to implement them.
  */
 public interface DataService {
 
     /**
      * Closes the connection. Instance is unusable afterwards.
      */
     public void close();
 
     /**
      * Adds a patient to the database.
      * Adds a new patient with a new PatID is PatID is null in the DTO,
      * otherwise, it overwrites whatever had the PatID last.
      *
      * @param patient
      * @return true if succeeded, false if failed
      */
 	public boolean addPatient(PatientDto patient);
 	
 	/**
 	 * Removes a patient from the database using the PatID in the PatientDto
 	 *
 	 * @param patient Contains the PatID to delete
 	 * @return True if succeeds false if error is encountered.
 	 */
 	public boolean removePatient(PatientDto patient);
 	
 	/**
 	 * Retrieves the patientDto by PatID. null if cannot be found.
 	 *
 	 * @param PatID
 	 */
 	public PatientDto getPatient(int PatID);
 
 	/**
 	 * Retrieves a list of all patients
 	 */
 	public List<PatientDto> getAllPatients();
 
 	/**
 	 * Retrieves a list of patients by the specified name.
 	 * 
 	 * @return List of results. null if there was an error completing the request.
 	 */
 	public List<PatientDto> queryPatientByName(String first, String last);
 	
 	/**
 	 * Returns a list no shows according patient.
 	 *
 	 * @param PatID
 	 * @return List of no shows associated with the patient
 	 */
 	public List<NoShowDto> getNoShowsByPatient(int PatID);
 	
 	/**
 	 * Returns a list of no shows in the last six months for the specified patient.
 	 */
 	public int getNoShowCountInLastSixMonths(PatientDto patient);
     
     /**
      * Adds a new Practitioner type to the database
      * 
      * @param type
      */
     public boolean addNewPractitionerType(String serviceType);
     
     /**
      * Removed a Practitioner type from the database
      * should also remove any practitioners registered only under that
      * type.
      *
      * @param type
      */
     public boolean removePractitionerType(String serviceType);
     
     /** Retrieves a list of practitioner types.
      * @return List of types in the form of strings. null if there was
      *an error completing the request.
      */
    public List<TypeDto> getAllPractitionerTypers();
     
     /** Retrieves a list of all practitioners
     */
     public List<PractitionerDto> getAllPractitioners();
     
     /** Add a practitioner to the database
     */
     public boolean addPractitioner(PractitionerDto practitioner);
     
     /**
      * Remove a practitioner from the database
      */
     public boolean removePractitioner(PractitionerDto practitioner);
     
     /** 
      * Change a practitioners info 
      */
     public boolean updatePractitionerInfo(PractitionerDto practitioner);
     
     /**
      * Retrives list of all practitioners in a day
      */
     public List<SchedulePractitionerDto> getAllPractitionersForDay(DayDto day);
     
     /**
      * remove a practitioner from a scheduled day
      * should also remove all appointments
      */
     public boolean removePractitionerFromDay(int practSchedId, DayDto day);
     
     /**
      * change hours of operation for a practitioner on a day
      * should also remove any affected appointments
      */
     public boolean changePractitionerHoursForDay(SchedulePractitionerDto practitioner, DayDto day, int start, int end);
     
     /**
      * Adds a patient to an appointment
      */
     public boolean addPatientToAppointment(int patID, AppointmentDto appointment);
     
     /**
      * removes a patient from an appointment
      */
     public boolean removePatientFromAppointment(AppointmentDto appointment);
     
     /**
      * check a patient as a no show
      */
     public boolean checkAsNoShow(AppointmentDto appointment);
     
     /**
      * uncheck a patient as a no show
      */
     public boolean uncheckAsNoShow(AppointmentDto appointment);
     
     /**
      * add patient to waitlist
      */
     public boolean addPatientToWaitlist(PatientDto patient, TypeDto type);
     
     /**
      * remove patient from waitlist
      */
     public boolean removePatientFromWaitlist(PatientDto patient, TypeDto type);
     
     /**
      * get waitlist info
      */
     public List<WaitlistDto> getWaitlist();
     
     /**
      * sets hours of operation for a day
      */
     public boolean setHoursForDay(DayDto day);
     
     /**
      * set open or closed on day
      */
     public boolean setStatus(DayDto day);
     
     /*
      * Schedules a practioner on a day
      */
     public SchedulePractitionerDto addPractitionerToDay(PractitionerDto pract, DayDto day, 
         int start, int end);
     
     /*
      * Gets a day given a date
      */
     public DayDto getOrCreateDay(Date date);
     
     /*
      * Gets a practioner from an ID
      */
     public PractitionerDto getPractitioner(int PractID);
     
     /*
      * Gets all appointments for a scheduledPract 
      */
     public List<AppointmentDto> getAllAppointments(int schedPractId);
     
 }
