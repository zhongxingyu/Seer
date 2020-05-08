 package org.ic.protrade.data.parsers;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 
 import org.ic.protrade.data.exceptions.NoSuchMatchException;
 import org.ic.protrade.data.fetchers.Match;
 import org.ic.protrade.data.match.PlayerEnum;
 import org.junit.Test;
 
 public class LivexScoreParserTest extends ParserTest {
 
 	@Test
 	public void testFetchScores() throws IOException, NoSuchMatchException {
 		String test = getTestString(TestManager.getScoreServe1Path());
 		Match m = LivexScoreParser.getMatch("Keothavong", "Hantuchova", test);
		assertEquals("00, 1, 0, 0, 0, 0", m.getPlayer1Score());
		assertEquals("00, 2, 0, 0, 0, 0", m.getPlayer2Score());
 		assertEquals(PlayerEnum.PLAYER1, m.getServingPlayer());
 
 		test = getTestString(TestManager.getScoreServe2Path());
 		m = LivexScoreParser.getMatch("Keothavong", "Hantuchova", test);
		assertEquals("40, 0, 0, 0, 0, 0", m.getPlayer1Score());
		assertEquals("15, 2, 0, 0, 0, 0", m.getPlayer2Score());
 		assertEquals(PlayerEnum.PLAYER2, m.getServingPlayer());
 
 	}
 }
