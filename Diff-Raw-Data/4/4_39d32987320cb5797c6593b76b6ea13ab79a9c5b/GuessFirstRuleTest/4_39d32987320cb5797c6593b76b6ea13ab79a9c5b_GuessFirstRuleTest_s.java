 package org.hitzemann.mms.solver.rule.knuth;
 
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyNoMoreInteractions;
 import static org.mockito.Mockito.when;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.hitzemann.mms.model.ErgebnisKombination;
 import org.hitzemann.mms.model.SpielKombination;
 import org.hitzemann.mms.solver.rule.IRule;
 import org.junit.Test;
 
 /**
  * Tests für {@link GuessFirstRule}.
  * 
  * @author schusterc
  */
 public final class GuessFirstRuleTest {
 
 	/**
 	 * Logger für Testausgaben.
 	 */
	private static final Logger LOGGER = Logger.getLogger(GuessFirstRule.class
			.getName());
 
 	/**
 	 * Mock für die zu übergebende {@link IKnuthRuleFactory}.
 	 */
 	private final IKnuthRuleFactory ruleFactoryMock = mock(IKnuthRuleFactory.class);
 
 	/**
 	 * Testet den Fehlerfall, dass das Minimum erwarteter Kandidaten größer ist
 	 * als das Maximum.
 	 */
 	@Test
 	public void testInvalidMinMax() {
 		try {
 			new GuessFirstRule(3, 2, ruleFactoryMock);
 			fail("expected Exception not thrown");
 		} catch (IllegalArgumentException e) {
 			LOGGER.log(Level.FINEST, "expected exception", e);
 		}
 		verifyNoMoreInteractions(ruleFactoryMock);
 	}
 
 	/**
 	 * Testet den Fehlerfall, dass das Minimum erwarteter Kandidaten kleiner als
 	 * 0 ist.
 	 */
 	@Test
 	public void testInvalidMin() {
 		try {
 			new GuessFirstRule(-1, 2, ruleFactoryMock);
 			fail("expected Exception not thrown");
 		} catch (IllegalArgumentException e) {
 			LOGGER.log(Level.FINEST, "expected exception", e);
 		}
 		verifyNoMoreInteractions(ruleFactoryMock);
 	}
 
 	/**
 	 * Testet den Fehlerfall, dass zu wenige Kandidaten übergeben werden.
 	 */
 	@Test
 	public void testTooFewCandidates() {
 		final GuessFirstRule underTest = new GuessFirstRule(11, 19,
 				ruleFactoryMock);
 
 		@SuppressWarnings("unchecked")
 		final Collection<SpielKombination> candidatesMock = mock(Collection.class);
 
 		when(candidatesMock.size()).thenReturn(10);
 
 		try {
 			underTest.getGuess(candidatesMock);
 			fail("expected Exception not thrown");
 		} catch (IllegalArgumentException e) {
 			LOGGER.log(Level.FINEST, "expected exception", e);
 		}
 
 		verify(candidatesMock).size();
 		verifyNoMoreInteractions(ruleFactoryMock, candidatesMock);
 	}
 
 	/**
 	 * Testet den Fehlerfall, dass zu viele Kandidaten übergeben werden.
 	 */
 	@Test
 	public void testTooManyCandidates() {
 		final GuessFirstRule underTest = new GuessFirstRule(1, 3,
 				ruleFactoryMock);
 
 		@SuppressWarnings("unchecked")
 		final Collection<SpielKombination> candidatesMock = mock(Collection.class);
 
 		when(candidatesMock.size()).thenReturn(4);
 
 		try {
 			underTest.getGuess(candidatesMock);
 			fail("expected Exception not thrown");
 		} catch (IllegalArgumentException e) {
 			LOGGER.log(Level.FINEST, "expected exception", e);
 		}
 
 		verify(candidatesMock).size();
 		verifyNoMoreInteractions(ruleFactoryMock, candidatesMock);
 	}
 
 	/**
 	 * Testet den Fehlerfall, dass kein Kandidat vorhanden ist.
 	 */
 	@Test
 	public void testNoCandidate() {
 		final GuessFirstRule underTest = new GuessFirstRule(0, 2,
 				ruleFactoryMock);
 
 		@SuppressWarnings("unchecked")
 		final Collection<SpielKombination> candidatesMock = mock(Collection.class);
 
 		when(candidatesMock.size()).thenReturn(0);
 
 		try {
 			underTest.getGuess(candidatesMock);
 			fail("expected Exception not thrown");
 		} catch (IllegalArgumentException e) {
 			Logger.getAnonymousLogger().log(Level.FINEST, "expected exception",
 					e);
 		}
 
 		verify(candidatesMock).size();
 		verifyNoMoreInteractions(ruleFactoryMock, candidatesMock);
 	}
 
 	/**
 	 * Testet dass der erste Kandidat geraten wird.
 	 */
 	@Test
 	public void testFirstGuessed() {
 		final GuessFirstRule underTest = new GuessFirstRule(10, 20,
 				ruleFactoryMock);
 
 		@SuppressWarnings("unchecked")
 		final Collection<SpielKombination> candidatesMock = mock(Collection.class);
 		@SuppressWarnings("unchecked")
 		final Iterator<SpielKombination> iteratorMock = mock(Iterator.class);
 		final SpielKombination first = new SpielKombination(new int[0]);
 
 		when(candidatesMock.size()).thenReturn(13);
 		when(candidatesMock.iterator()).thenReturn(iteratorMock);
 		when(iteratorMock.next()).thenReturn(first);
 
 		assertSame(first, underTest.getGuess(candidatesMock));
 
 		verify(candidatesMock).size();
 		verify(candidatesMock).iterator();
 		verify(iteratorMock).next();
 		verifyNoMoreInteractions(ruleFactoryMock, candidatesMock, iteratorMock);
 	}
 
 	/**
 	 * Testet die Interaktion von mit der {@link IKnuthRuleFactory}.
 	 */
 	@Test
 	public void testNextRule() {
 		final IRule expectedRule = mock(IRule.class);
 
 		final GuessFirstRule underTest = new GuessFirstRule(2, 12,
 				ruleFactoryMock);
 
 		when(ruleFactoryMock.createGuessFirstRule(anyInt(), anyInt()))
 				.thenReturn(expectedRule);
 
 		assertSame(expectedRule,
 				underTest.getRuleForResponse(new ErgebnisKombination(0, 0)));
 
 		verify(ruleFactoryMock).createGuessFirstRule(0, 12);
 		verifyNoMoreInteractions(ruleFactoryMock, expectedRule);
 	}
 }
