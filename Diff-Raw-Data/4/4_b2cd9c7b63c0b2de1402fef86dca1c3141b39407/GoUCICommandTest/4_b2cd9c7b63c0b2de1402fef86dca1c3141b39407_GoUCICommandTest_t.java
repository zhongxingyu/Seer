 package de.fhb.projects.Twitchess.ucicommands;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import de.fhb.projects.Twitchess.controller.ucicommands.GoUCICommand;
 import de.fhb.projects.Twitchess.controller.ucicommands.UCICommand;
 import de.fhb.projects.Twitchess.exception.UCIException;
 
 public class GoUCICommandTest {
 
 	private GoUCICommand uciCommand;
 	
 	
 	@Before
 	public void init(){

 		uciCommand = new GoUCICommand();
 	}
 	
 	@Test
 	public void processResponseBestMoveTest(){
 		uciCommand.processResponse("bestmove g1f3 ponder g8f6");
 		assertEquals("{bestMove=g1f3}",uciCommand.getResult().toString());
 	}
 	
 	@Test
 	public void processResponseNullStringTest(){
 		uciCommand.processResponse(null);
 		assertEquals(new HashMap<String, String>(),uciCommand.getResult());
 	}
 	
 	@Test
 	public void processResponseInfoScoreMateTest(){
 		uciCommand.processResponse("info ... score mate 10 ..");
 		assertEquals("{score="+uciCommand.MATE_SCORE+"}",uciCommand.getResult().toString());
 	}
 	
 	@Test
 	public void processResponseInfoOnlyTest(){
 		uciCommand.processResponse("info");
 		assertEquals("{}",uciCommand.getResult().toString());
 		
 		uciCommand.processResponse("info dvbfuja nsdfaf nasdkl .. . .!!!");
 		assertEquals("{}",uciCommand.getResult().toString());
 	}
 	@Test
 	public void processResponseEmptyStringTest(){
 		uciCommand.processResponse("");
 		assertEquals("{}",uciCommand.getResult().toString());		
 	}
 	
 	@Test
 	public void processResponseInfoScoreCPTest(){
 		uciCommand.processResponse("info score cp 100");
 		assertEquals("{score=100}",uciCommand.getResult().toString());
 	}
 	
 	@Test
 	public void processResponseSwitchInputTest(){
 		uciCommand.processResponse("score info cp 100");
 		assertEquals("{}",uciCommand.getResult().toString());
 		uciCommand.processResponse("info cp 100 score mate");
 		assertEquals("{}",uciCommand.getResult().toString());
 	}
 	
 	@Test (expected = UCIException.class)
 	public void getBestMoveIsNotFinishedTest() throws UCIException{
 		assertNotNull(uciCommand.getBestMove());
 	}
 	@Test
 	public void getBestMoveTest() throws UCIException{
 		uciCommand.processResponse("bestmove g1f3 ponder g8f6");
 		uciCommand.getBestMove();
 	}
 
 	@Test (expected = UCIException.class)
 	public void getBestMoveNoneTest() throws UCIException{
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("bestMove", "(none)");
 		uciCommand.setFinished(true);
 		uciCommand.setResult(map);
 		uciCommand.getResult();
 		uciCommand.getBestMove();
 	}
 
 	@Test
 	public void generateFullCommandStringDepthNullTest(){
 		uciCommand.setDepth(null);
 		assertEquals("go movetime "+uciCommand.getMovetime(),uciCommand.generateFullCommandString());
 	}
 	
 	@Test
 	public void generateFullCommandStringDepthNotNullTest(){
 		uciCommand.setDepth(1);
 		assertEquals("go depth "+uciCommand.getDepth(),uciCommand.generateFullCommandString());
 	}
 	
 	@Test
 	public void generateFullDepthMaxMINIntTest(){
 		uciCommand.setDepth(Integer.MIN_VALUE);
 		assertEquals("go movetime "+uciCommand.getMovetime(),uciCommand.generateFullCommandString());
 		uciCommand.setDepth(Integer.MAX_VALUE);
 		assertEquals("go depth "+uciCommand.getDepth(),uciCommand.generateFullCommandString());
 	}
 	
 	@Test
 	public void generateFullCommandStringMovetimeNullTest(){
 		uciCommand.setMovetime(null);
 		assertEquals("go movetime "+uciCommand.getMovetime(),uciCommand.generateFullCommandString());
 	}
 	
 	@Test (expected = UCIException.class)
 	public void getScoreNoScoreTest() throws UCIException{
 		uciCommand.setFinished(true);
 		uciCommand.getScore();
 	}
 	
 	@Test 
 	public void getScoreTest() throws UCIException{
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("score", "1");
 		uciCommand.setResult(map);
 		uciCommand.setFinished(true);
 		assertEquals(1,uciCommand.getScore());
 	}
 	
 	@Test (expected = UCIException.class)
 	public void getScoreNotFinishedTest() throws UCIException{
 		uciCommand.getScore();
 	}
 	
 	@Test
 	public void setMoveTimeNullTest(){
 		uciCommand.setMovetime(null);
 		assertTrue(10000==uciCommand.getMovetime());
 	}
 	
 	 @Test 
 	 public void MoveTimeNullDepthTimeNullTest(){
 		uciCommand.processResponse("info");
 		assertEquals("{}",uciCommand.getResult().toString());
 
 	 }
 	 
 	@Test
 	public void processResponseInfoScoreTest(){
 		uciCommand.processResponse("info score fail input");
 		assertEquals("{}",uciCommand.getResult().toString());
 	}
 	 
 }
