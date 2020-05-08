 package repository;
 
 import com.github.ldriscoll.ektorplucene.LuceneAwareCouchDbConnector;
 import domain.Address;
 import domain.Patient;
 import org.ektorp.CouchDbInstance;
 import org.ektorp.http.HttpClient;
 import org.ektorp.http.StdHttpClient;
 import org.ektorp.impl.StdCouchDbInstance;
 import org.joda.time.LocalDate;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static org.joda.time.LocalDate.now;
 
 public class PatientRepositoryTest {
 
     private LuceneAwareCouchDbConnector dbConnector;
     private PatientRepository patientRepository;
 
     @Before
     public void setUp() throws Exception {
         HttpClient httpClient = new StdHttpClient.Builder().build();
         CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
         dbConnector = new LuceneAwareCouchDbConnector("lucene-spike-db", dbInstance);
         dbConnector.createDatabaseIfNotExists();
         patientRepository = new PatientRepository(dbConnector);
     }
 
     @Test
     public void shouldAddPatient(){
         Patient patient = createPatient("1", "name", 21, now());
         patientRepository.add(patient);
 
         assertNotNull(patient.getId());
         assertEquals(patientRepository.get(patient.getId()), patient);
     }
 
 
     @Test
     public void shouldFindPatientByPatientId(){
         Patient patient = addPatient("1", "name", 21, now());
 
         Map<String, String> queryParams = new HashMap();
         queryParams.put("patientId", "1");
         List<Patient> patients = patientRepository.find(queryParams, null, null);
 
         assertEquals(1, patientRepository.count(queryParams));
         assertEquals(patients.size(), 1);
         assertEquals(patients.get(0), patient);
     }
 
     @Test
     public void shouldFindPatientByPatientName(){
         addPatient("1", "name1", 11, now());
         addPatient("2", "name2", 22, now());
         Patient patient3 = addPatient("3", "name3", 32, now());
         addPatient("4", "name4", 41, now());
         addPatient("5", "name5", 58, now());
 
         Map<String, String> queryParams = new HashMap();
         queryParams.put("name", "name3");
         List<Patient> patients = patientRepository.find(queryParams, null, null);
 
         assertEquals(patients.size(), 1);
         assertEquals(patients.get(0), patient3);
     }
 
     @Test
     public void shouldFindPatientByPatientAgeAndDateRange(){
         addPatient("1", "name1", 22, LocalDate.parse("2010-01-01"));
         Patient patient2 = addPatient("2", "name2", 22, LocalDate.parse("2010-02-01"));
         Patient patient3 = addPatient("3", "name3", 22, LocalDate.parse("2010-03-01"));
         Patient patient4 = addPatient("4", "name4", 22, LocalDate.parse("2010-04-01"));
         addPatient("5", "name5", 22, LocalDate.parse("2010-05-01"));
 
         Map<String, String> queryParams = new HashMap();
         queryParams.put("age", "22");
         queryParams.put("dob<date>", "[2010-02-01 TO 2010-04-30]");
         List<Patient> patients = patientRepository.find(queryParams, null, null);
 
         assertEquals(3, patientRepository.count(queryParams));
         assertEquals(3, patients.size());
         assertEquals(patient2, patients.get(0));
         assertEquals(patient3, patients.get(1));
         assertEquals(patient4, patients.get(2));
     }
 
     @Test
     public void shouldFindPatientByAddress(){
        Patient patient1 = createPatient("1", "name1", 22, LocalDate.parse("2010-01-01")).withAddresses(new Address("addr2", "street2", "city2", "state1"));
        Patient patient2 = createPatient("2", "name2", 22, LocalDate.parse("2010-02-01")).withAddresses(new Address("addr2", "street2", "city2", "state2"));

        addPatient(patient1);
        addPatient(patient2);
 
         Map<String, String> queryParams = new HashMap();
         queryParams.put("state", "state2");
         List<Patient> patients = patientRepository.find(queryParams, null, null);
 
         assertEquals(1, patients.size());
         assertEquals(patient2, patients.get(0));
     }
 
     @Test
     public void shouldApplyLimitAndSkip(){
         addPatient("1", "name1", 22, LocalDate.parse("2010-01-01"));
         addPatient("2", "name2", 22, LocalDate.parse("2010-02-01"));
         addPatient("3", "name3", 22, LocalDate.parse("2010-03-01"));
         addPatient("4", "name4", 22, LocalDate.parse("2010-04-01"));
         addPatient("5", "name5", 22, LocalDate.parse("2010-05-01"));
         addPatient("6", "name6", 22, LocalDate.parse("2010-06-01"));
         addPatient("7", "name7", 22, LocalDate.parse("2010-07-01"));
         addPatient("8", "name8", 22, LocalDate.parse("2010-08-01"));
         addPatient("9", "name9", 22, LocalDate.parse("2010-09-01"));
         Patient patient10 = addPatient("10", "name10", 22, LocalDate.parse("2010-10-01"));
 
         Map<String, String> queryParams = new HashMap();
         queryParams.put("age", "22");
 
         assertEquals(10, patientRepository.count(queryParams));
         assertEquals(3, patientRepository.find(queryParams, 3, 0).size());
         assertEquals(3, patientRepository.find(queryParams, 3, 3).size());
         assertEquals(3, patientRepository.find(queryParams, 3, 6).size());
         assertEquals(1, patientRepository.find(queryParams, 3, 9).size());
        assertEquals(patient10, patientRepository.find(queryParams, 3, 9).get(0));
         assertEquals(5, patientRepository.find(queryParams, 5, 5).size());
     }
 
     private Patient addPatient(String patientId, String name, int age, LocalDate dob) {
         Patient patient = createPatient(patientId, name, age, dob);
        return addPatient(patient);
    }

    private Patient addPatient(Patient patient) {
         patientRepository.add(patient);
         return patient;
     }
 
     private Patient createPatient(String patientId, String name, int age, LocalDate dob) {
         Patient patient = new Patient();
         patient.setName(name);
         patient.setPatientId(patientId);
         patient.setDob(dob);
         patient.setAge(age);
         return patient;
     }
 
     @After
     public void tearDown()  {
         List<Patient> all = patientRepository.getAll();
         for(Patient patient : all){
             patientRepository.remove(patient);
         }
     }
 }
