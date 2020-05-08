 package org.kalibro;
 
 import static org.junit.Assert.*;
 import static org.kalibro.ProcessState.*;
 
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.kalibro.core.concurrent.VoidTask;
 import org.kalibro.tests.UnitTest;
 
 public class ProcessingTest extends UnitTest {
 
 	private Repository repository;
 	private Processing processing;
 
 	@Before
 	public void setUp() {
 		repository = mock(Repository.class);
 		processing = new Processing(repository);
 	}
 
 	@Test
 	public void shouldSortByRepositoryThenDate() {
 		Processing first = withRepositoryDate("A", 3);
 		Processing second = withRepositoryDate("A", 4);
 		Processing third = withRepositoryDate("Z", 1);
 		Processing fourth = withRepositoryDate("Z", 2);
 		assertSorted(first, second, third, fourth);
 	}
 
 	private Processing withRepositoryDate(String repositoryName, long date) {
 		Processing other = new Processing(new Repository(repositoryName, null, ""));
 		other.setDate(new Date(date));
 		return other;
 	}
 
 	@Test
 	public void shouldIdentifyByRepositoryAndDate() {
 		Processing other = new Processing(null);
 		assertFalse(other.equals(processing));
 
 		other = new Processing(repository);
		other.setDate(new Date(0));
 		assertFalse(other.equals(processing));
 
 		other.setDate(processing.getDate());
 		assertEquals(processing, other);
 	}
 
 	@Test
 	public void checkConstruction() {
 		assertNull(processing.getId());
 		assertSame(repository, processing.getRepository());
 		assertEquals(new Date().getTime(), processing.getDate().getTime(), 100);
 		assertEquals(LOADING, processing.getState());
 		for (ProcessState state : ProcessState.values())
 			assertNull(processing.getStateTime(state));
 		assertNull(processing.getResultsRoot());
 	}
 
 	@Test
 	public void shouldBeInErrorStateAfterSettingError() {
 		Throwable error = mock(Throwable.class);
 		processing.setError(error);
 		assertSame(error, processing.getError());
 		assertEquals(ERROR, processing.getState());
 	}
 
 	@Test
 	public void shouldGetStateMessage() {
 		String name = "ProcessingTest repository complete name";
 		when(repository.getCompleteName()).thenReturn(name);
 
 		assertEquals(LOADING.getMessage(name), processing.getStateMessage());
 		processing.setState(ANALYZING);
 		assertEquals(ANALYZING.getMessage(name), processing.getStateMessage());
 	}
 
 	@Test
 	public void shouldGetStateWhenErrorOcurred() {
 		assertNull(processing.getStateWhenErrorOcurred());
 		processing.setState(ANALYZING);
 		processing.setError(mock(Throwable.class));
 		assertEquals(ANALYZING, processing.getStateWhenErrorOcurred());
 	}
 
 	@Test
 	public void shouldNotAllowErrorStateWithoutError() {
 		assertThat(new VoidTask() {
 
 			@Override
 			protected void perform() {
 				processing.setState(ERROR);
 			}
 		}).throwsException().withMessage("Use setError(Throwable) to put repository in error state");
 	}
 
 	@Test
 	public void shouldSetStateTimes() {
 		processing.setStateTime(LOADING, 6);
 		processing.setStateTime(COLLECTING, 28);
 		processing.setStateTime(ANALYZING, 496);
 		assertEquals(6, processing.getStateTime(LOADING).longValue());
 		assertEquals(28, processing.getStateTime(COLLECTING).longValue());
 		assertEquals(496, processing.getStateTime(ANALYZING).longValue());
 	}
 
 	@Test
 	public void shouldSetResultsRoot() {
 		ModuleResult resultsRoot = mock(ModuleResult.class);
 		processing.setResultsRoot(resultsRoot);
 		assertSame(resultsRoot, processing.getResultsRoot());
 	}
 }
