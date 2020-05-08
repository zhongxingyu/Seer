 package org.kalibro;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.any;
 import static org.powermock.api.mockito.PowerMockito.*;
 
 import java.awt.Color;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.core.concurrent.Task;
 import org.kalibro.core.dao.DaoFactory;
 import org.kalibro.core.dao.ReadingDao;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(DaoFactory.class)
 public class ReadingTest extends TestCase {
 
 	private Reading reading;
 	private ReadingDao dao;
 
 	@Before
 	public void setUp() {
 		dao = mock(ReadingDao.class);
 		mockStatic(DaoFactory.class);
 		when(DaoFactory.getReadingDao()).thenReturn(dao);
 		reading = loadFixture("reading-excellent", Reading.class);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldSortByGrade() {
 		assertSorted(reading(Double.NEGATIVE_INFINITY), reading(0.0), reading(42.0), reading(Double.POSITIVE_INFINITY));
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void checkDefaultReading() {
 		reading = new Reading();
 		assertNull(reading.getId());
 		assertEquals("", reading.getLabel());
 		assertDoubleEquals(0.0, reading.getGrade());
 		assertEquals(Color.WHITE, reading.getColor());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void readingsWithSameLabelShouldConflict() {
 		String label = reading.getLabel();
		shouldConflictWith(reading(label), "Reading with label \"" + label + "\" already exists in the group.");
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void readingsWithSameGradeShouldConflict() {
 		Double grade = reading.getGrade();
 		shouldConflictWith(reading(grade), "Reading with grade " + grade + " already exists in the group.");
 	}
 
 	private Reading reading(String label) {
 		return new Reading(label, 42.0, Color.MAGENTA);
 	}
 
 	private Reading reading(Double grade) {
 		return new Reading("ReadingTest label", grade, Color.MAGENTA);
 	}
 
 	private void shouldConflictWith(final Reading other, String message) {
 		checkKalibroException(new Task() {
 
 			@Override
 			protected void perform() throws Throwable {
 				reading.assertNoConflictWith(other);
 			}
 		}, message);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void readingsWithSameColorShouldNotConflict() {
 		reading.assertNoConflictWith(new Reading("", 42.0, reading.getColor()));
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldNotSaveIfNotGrouped() {
 		checkKalibroException(new Task() {
 
 			@Override
 			protected void perform() throws Throwable {
 				reading.save();
 			}
 		}, "Reading is not in any group.");
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldSaveIfGrouped() {
 		reading.setGroup(mock(ReadingGroup.class));
 		reading.save();
 		verify(dao).save(reading);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldDeleteIfExists() {
 		reading.delete();
 		verify(dao, never()).delete(any(Long.class));
 
 		reading.setId(42L);
 		reading.delete();
 		verify(dao).delete(42L);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldRemoveFromGroupOnDelete() {
 		ReadingGroup group = mock(ReadingGroup.class);
 		reading.setGroup(group);
 
 		reading.delete();
 		verify(group).removeReading(reading);
 	}
 }
