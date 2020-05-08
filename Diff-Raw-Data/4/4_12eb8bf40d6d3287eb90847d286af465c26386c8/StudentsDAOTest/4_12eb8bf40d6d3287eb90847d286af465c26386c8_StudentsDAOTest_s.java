 package com.kesho.datamart.repository;
 
 import com.kesho.datamart.dbtest.DatabaseSetupRule;
 import com.kesho.datamart.domain.CLASS;
 import com.kesho.datamart.domain.Gender;
 import com.kesho.datamart.entity.*;
 import com.kesho.datamart.service.DBUtil;
 import org.dbunit.dataset.DataSetException;
 import org.joda.time.LocalDate;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.TransactionDefinition;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.TransactionCallback;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import javax.inject.Inject;
 import java.sql.SQLException;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertNotNull;
 
 @ContextConfiguration(locations = {
         "classpath:repository-context.xml"})
 @RunWith(SpringJUnit4ClassRunner.class)
 public class StudentsDAOTest {
     @Rule
     public final DatabaseSetupRule dbSetup = DatabaseSetupRule.setUpDataFor("kesho", "students-it-data.xml");
 
 	@Inject
 	private StudentsDAO repo;
 
     @Inject
     private JpaTransactionManager transactionManager;
 
 	
 	@Test
 	public void shouldSaveStudent() throws DataSetException, SQLException {
         LocalDate startDate = LocalDate.now();
         Student student = new Student();
         student.setFirstName("s1");
         Family f = new Family();
         f.setId(1L);
        f.setName("name");
         student.setFamily(f);
         student.setActive(true);
         student.setGender(Gender.M);
         student.setHasDisability(true);
         student.setHomeLocation("s1home");
         student.setContactNumber("12345");
         student.setStartDate(startDate);
         student.setSponsored(true);
         student.setYearOfBirth(2000);
 
 		final Student s = repo.save(student);
 		assertNotNull("Student should have an id", s.getId());
 
         TransactionCallback<Student> callback = new TransactionCallback<Student>() {
             @Override
             public Student doInTransaction(TransactionStatus status) {
                 return repo.findOne(s.getId());
             }
         };
 
         TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
         txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
         //TODO: import jpa dialect
 //        txTemplate.setIsolationLevel(Isolation.READ_COMMITTED.value());
 
         Student saved = txTemplate.execute(callback);
 
         assertThat(saved.getFirstName(), is("s1"));
        assertThat(saved.getFamily().getName(), is("sn1"));
         assertThat(saved.isActive(), is(true));
         assertThat(saved.getGender(), is(Gender.M));
         assertThat(saved.hasDisability(), is(true));
         assertThat(saved.getHomeLocation(), is("s1home"));
         assertThat(saved.getContactNumber(), is("12345"));
         assertThat(saved.getStartDate(), is(startDate));
         assertThat(saved.isSponsored(), is(true));
         assertThat(saved.getYearOfBirth(), is(2000));
 
 		assertThat("Expected 1 row", dbSetup.getConnection().createQueryTable("students", String.format("select * from STUDENTS where id=%d", s.getId())).getRowCount(), is(1));
 	}
 
 	@Test
 	public void shouldFindStudent() {
 		Student student = repo.findOne(2L);
 		assertNotNull(student);
 		assertThat("Should match first name", student.getFirstName(), is("fn"));
 	}
 	
 	@Test
 	public void shouldDeleteStudent() throws DataSetException, SQLException {
 		repo.delete(2L);
 		assertThat("Expected no rows", dbSetup.getConnection().createQueryTable("students", "select * from STUDENTS where id=2").getRowCount(), is(0));
 	}
 //TODO: redo education history tests
 //	@Test
 //	public void shuoldCascadeInsertLog() throws DataSetException, SQLException {
 //		Student student = new Student();
 //		StudentLog log = new StudentLog();
 //		log.setComment("test log");
 //		student.addLog(log);
 //		Student s = repo.save(student);
 //		assertNotNull("Student should have an id", s.getId());
 //		assertThat("Student should have one log", s.getLogs().size(), is(1));
 //		assertThat("Expected student log row", dbSetup.getConnection().createQueryTable("STUDENT_LOG", String.format("select * from STUDENT_LOG where student_id=%d", s.getId())).getRowCount(), is(1));
 //
 //		Object logComment = dbSetup.getConnection().createQueryTable("student_log", String.format("select * from STUDENT_LOG where student_id=%d", s.getId())).getValue(0, "LOG");
 //		assertThat("Expected comment", log.getComment(), is(logComment));
 //	}
 
 //	@Test
 //	public void shuoldCascadeInsertMultipleLogs() throws DataSetException, SQLException {
 //		Student student = new Student();
 //		StudentLog log1 = new StudentLog();
 //		log1.setComment("log1");
 //		student.addLog(log1);
 //
 //		StudentLog log2 = new StudentLog();
 //		log2.setComment("log2");
 //		student.addLog(log2);
 //		Student s = repo.save(student);
 //
 //		assertNotNull("Student should have an id", s.getId());
 //		assertThat("Student should have 2 logs", s.getLogs().size(), is(2));
 //
 //		assertThat("Expected student log row", dbSetup.getConnection().createQueryTable("student_log", String.format("select * from STUDENT_LOG where student_id=%d", s.getId())).getRowCount(), is(2));
 //	}
 
 //	@Test
 //	public void shouldDeleteLogsOnUpdate() throws DataSetException, SQLException {
 //		Student student = new Student();
 //		StudentLog log = new StudentLog();
 //		log.setComment("test log");
 //		student.addLog(log);
 //		Student s = repo.save(student);
 //
 //		s.getLogs().remove(0);
 //		s = repo.save(s);
 //
 //		assertThat("Expected student log row", dbSetup.getConnection().createQueryTable("student_log", String.format("select * from STUDENT_LOG where student_id=%d", s.getId())).getRowCount(), is(0));
 //	}
 //
 //	@Test
 //	public void shouldCascadeDeleteLogs() throws DataSetException, SQLException {
 //		Student student = new Student();
 //		StudentLog log = new StudentLog();
 //		log.setComment("test log");
 //		student.addLog(log);
 //		Student s = repo.save(student);
 //
 //		repo.delete(s.getId());
 //
 //		assertThat("Expected student log row", dbSetup.getConnection().createQueryTable("student_log", String.format("select * from STUDENT_LOG where student_id=%d", s.getId())).getRowCount(), is(0));
 //	}
 //
 //	@Test
 //	public void shouldCascadeUpdateLog() throws DataSetException, SQLException {
 //		Student student = new Student();
 //		StudentLog log = new StudentLog();
 //		log.setComment("test log");
 //		student.addLog(log);
 //		Student s = repo.save(student);
 //
 //		s.getLogs().get(0).setComment("new comment");
 //		s = repo.save(s);
 //
 //		assertThat("Expected comment", dbSetup.getConnection().createQueryTable("student_log", String.format("select LOG from STUDENT_LOG where student_id=%d", s.getId())).getValue(0, "LOG").toString(), is("new comment"));
 //	}
 	
 //	@Test
 //	public void shouldCascadeInsertEducationHistory() throws DataSetException, SQLException {
 //		Student student = repo.findwithJoin(2L);
 //		School school = new School();
 //		school.setId(1L);
 //
 //		EducationHistory eh = new EducationHistory();
 //		eh.setCurrentClass(CLASS.YEAR1);
 //		eh.setLevel("Level1");
 //		eh.setPredictedEndDate(LocalDate.now());
 //		eh.setPredictedEndDate(LocalDate.now().plusYears(1));
 //		eh.setStudentId(student.getId());
 //		eh.setSchool(school);
 //
 //		EducationHistory eh1 = new EducationHistory();
 //		//eh1.setCurrentClass(CLASS.YEAR2);
 //		eh1.setLevel("Level2");
 //		//eh1.setPredictedEndDate(LocalDate.now());
 //		//eh1.setPredictedEndDate(LocalDate.now().plusYears(1));
 //		eh1.setStudentId(student.getId());
 //		//eh1.setSchool(school);
 //
 //		student.addToEducationHistory(eh);
 //		student.addToEducationHistory(eh1);
 //		student = repo.save(student);
 //
 //		assertThat("Expected EDUCATION_HISTORY", dbSetup.getConnection().createQueryTable("EDUCATION_HISTORY", String.format("select * from EDUCATION_HISTORY where student_id=%d order by level asc", student.getId())).getValue(0, "level").toString(), is("Level1"));
 //		assertThat("Expected EDUCATION_HISTORY", dbSetup.getConnection().createQueryTable("EDUCATION_HISTORY", String.format("select * from EDUCATION_HISTORY where student_id=%d order by level asc", student.getId())).getValue(0, "class").toString(), is(CLASS.YEAR1.name()));
 //		assertThat("Expected EDUCATION_HISTORY", dbSetup.getConnection().createQueryTable("EDUCATION_HISTORY", String.format("select * from EDUCATION_HISTORY where student_id=%d order by level asc", student.getId())).getValue(1, "level").toString(), is("Level2"));
 //		assertThat("Expected EDUCATION_HISTORY", dbSetup.getConnection().createQueryTable("EDUCATION_HISTORY", String.format("select * from EDUCATION_HISTORY where student_id=%d order by level asc", student.getId())).getValue(1, "class").toString(), is(CLASS.YEAR2.name()));
 //
 //		//assertThat(student.getEducationHistory().iterator().next().getSchool().getFamilyName(), is("school1"));
 //	}
 	
 //	@Test
 //	public void shouldCascadeDeleteEducationHistory() throws DataSetException, SQLException {
 //		Student student = repo.findwithJoin(2L);
 //		School school = new School();
 //		school.setId(1L);
 //
 //		EducationHistory eh = new EducationHistory();
 //		//eh.setCurrentClass(CLASS.YEAR1);
 //		eh.setLevel("Level1");
 //		//eh.setPredictedEndDate(LocalDate.now());
 //		//eh.setPredictedEndDate(LocalDate.now().plusYears(1));
 //		eh.setStudentId(student.getId());
 //		//eh.setSchool(school);
 //
 //		student.addToEducationHistory(eh);
 //		student = repo.save(student);
 //
 //		repo.delete(student);
 //
 //		assertThat("Expected EDUCATION_HISTORY", dbSetup.getConnection().createQueryTable("EDUCATION_HISTORY", String.format("select * from EDUCATION_HISTORY where student_id=%d", student.getId())).getRowCount(), is(0));
 //	}
 	
 //	@Test
 //	public void shouldNotCascadeDeleteEducationHistoryToSchool() throws DataSetException, SQLException {
 //		Student student = repo.findwithJoin(2L);
 //		School school = new School();
 //		school.setId(1L);
 //
 //		EducationHistory eh = new EducationHistory();
 //		//eh.setCurrentClass(CLASS.YEAR1);
 //		eh.setLevel("Level1");
 //		//eh.setPredictedEndDate(LocalDate.now());
 //		//eh.setPredictedEndDate(LocalDate.now().plusYears(1));
 //		eh.setStudentId(student.getId());
 //		//eh.setSchool(school);
 //		student.addToEducationHistory(eh);
 //		student = repo.save(student);
 //
 //		repo.delete(student);
 //
 //		assertThat("Expected EDUCATION_HISTORY", dbSetup.getConnection().createQueryTable("EDUCATION_HISTORY", String.format("select * from EDUCATION_HISTORY where student_id=%d", student.getId())).getRowCount(), is(0));
 //		assertThat("Expected EDUCATION_HISTORY", dbSetup.getConnection().createTable("SCHOOLS").getRowCount(), is(1));
 //
 //	}
 	
 //	@Test(expected = JpaObjectRetrievalFailureException.class)
 //	public void shouldNotCascadeInsertEducationHistoryToSchool() throws DataSetException, SQLException {
 //		Student student = repo.findwithJoin(2L);
 //		School school = new School();
 //		school.setId(Long.MAX_VALUE);
 //
 //		EducationHistory eh = new EducationHistory();
 //		//eh.setCurrentClass(CLASS.YEAR1);
 //		eh.setLevel("Level1");
 //		//eh.setPredictedEndDate(LocalDate.now());
 //		//eh.setPredictedEndDate(LocalDate.now().plusYears(1));
 //		//eh.setSchool(school);
 //		student.addToEducationHistory(eh);
 //		student = repo.save(student);
 //	}
 	
 }
