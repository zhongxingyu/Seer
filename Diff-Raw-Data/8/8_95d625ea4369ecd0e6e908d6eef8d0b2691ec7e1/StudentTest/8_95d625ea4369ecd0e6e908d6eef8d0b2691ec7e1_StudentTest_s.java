 package org.sukrupa.student;
 
 import org.junit.Test;
 
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 public class StudentTest {
 
     @Test
     public void shouldBeEqual() {
         assertThat(student("pat", null).equals(student("pat", null)), is(true));
     }
 
     @Test
     public void shouldHaveSameHashCode() {
         assertThat(student("pat", null).hashCode(), is(student("pat", null).hashCode()));
     }
 
     @Test
     public void shouldNotBeEqualIfDifferentName() {
         assertThat(student("pat", null).equals(student("mr. jones", null)), is(false));
     }
 
 	@Test
 	public void shouldBe5YearsOld() {
 		assertThat(createFakeStudent("2005/01/22", "2010/03/01").getAge(), is(5));
 	}
 
 	@Test
 	public void shouldBeOfSameAge() {
 		String currentDate = "2010/03/01";
 		assertThat(createFakeStudent("2005/04/12", currentDate).getAge() == createFakeStudent("2005/06/10", currentDate).getAge(), is(true));
 	}
 
 	@Test
 	public void shouldBe5YearOldCurrentDateMonthBeforeDOBMonth() {
 		assertThat(createFakeStudent("2005/05/22", "2010/04/01").getAge(), is(4));
 	}
 
 	@Test
 	public void shouldBe5YearOldCurrentDateDayBeforeDOBDay() {
 		assertThat(createFakeStudent("2005/05/02", "2010/05/01").getAge(), is(4));
 	}
 
 	@Test
 	public void shouldNBe5YearOldCurrentDateMonthAfterDOBMonth() {
 		assertThat(createFakeStudent("2005/05/03", "2010/06/03").getAge(), is(5));
 	}
 
 	@Test
 	public void shouldNBe5YearOldCurrentDateDayAfterDOBDay() {
 		assertThat(createFakeStudent("2005/05/02", "2010/05/03").getAge(), is(5));
 	}
 
 	@Test
 	public void shouldBeCurrentDate() {
 		GregorianCalendar currentDate = new GregorianCalendar();
		resetSecondsAnMilliseconds(currentDate);
 		Calendar studentCurrentDate = new Student() {
 			public Calendar getCurrentDateTest() {
 				return super.getCurrentDate();
 			}
 		}.getCurrentDateTest();
		resetSecondsAnMilliseconds(studentCurrentDate);
 
 		assertThat(currentDate, is(studentCurrentDate));
 	}
 
 	private FakeStudent createFakeStudent(String dateOfBirth, String currentDate) {
 		FakeStudent student = new FakeStudent(null, null, null, null, null, null, null, null, dateOfBirth);
 		student.setCurrentDate(currentDate);
 		return student;
 	}
 
	private void resetSecondsAnMilliseconds(Calendar calendar) {
 		calendar.set(Calendar.MILLISECOND, 0);
 		calendar.set(Calendar.SECOND, 0);
 	}
 
 	private Student student(String name, String dateOfBirth) {
         return new StudentBuilder().name(name).dateOfBirth(dateOfBirth).build();
     }
 
 	private class FakeStudent extends Student {
 
 		private String currentDate;
 
 		public FakeStudent(String studentId, String name, String religion, String caste, String subCaste, String area, String sex, String studentClass, String dateOfBirth) {
 			super(studentId, name, religion, caste, subCaste, area, sex, studentClass, dateOfBirth);
 		}
 
 		public void setCurrentDate(String currentDate) {
 			this.currentDate = currentDate;
 		}
 
 		@Override
 		protected Calendar getCurrentDate() {
 			try {
 				GregorianCalendar cal = new GregorianCalendar();
 				cal.setTime(super.getDateFormat().parse(currentDate));
 				return cal;
 			} catch (ParseException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 }
