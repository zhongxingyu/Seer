 package de.fhb.projects.Twitchess.controller.chesscommands;
 
 import static org.junit.Assert.assertEquals;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import de.fhb.projects.Twitchess.controller.UCIEngineInterface;
 import de.fhb.projects.Twitchess.data.ChessStateDAOInterface;
 import de.fhb.projects.Twitchess.data.ChessStateVO;
 import de.fhb.projects.Twitchess.exception.ChessManagerException;
 import de.fhb.projects.Twitchess.exception.UCIException;
 
 
 public class MoveChessCommandTest {
 	
 	private ChessStateDAOInterface dao;
 	private UCIEngineInterface uci;
 	private ChessStateVO chessState;
 	private List<ChessStateVO> state;
 	private MoveChessCommand mcc;
 	private List <String> parameters;
 	
 	@Before
 	public void ini(){
 		uci = EasyMock.createStrictMock(UCIEngineInterface.class);
 		dao = EasyMock.createStrictMock(ChessStateDAOInterface.class);
 		mcc = new MoveChessCommand(dao, uci);
 		parameters = new ArrayList<String>();
 		state = new ArrayList<ChessStateVO>();
 		chessState = new ChessStateVO();
 		chessState.setId(1);
 		chessState
 				.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
 		chessState.setPlayerName("player1");
 		chessState.setResult(null);
 		chessState.setDate(null);
 		state.add(chessState);
 	}
 
 	@Test (expected = ChessManagerException.class)
 	public void processInputNullParameterTest() throws ChessManagerException, SQLException{
 		mcc.processInput("player", null);
 	}
 
 	@Test (expected = ChessManagerException.class)
 	public void processInputOneParameterTest() throws ChessManagerException, SQLException{
 		parameters.add("p1");
 		mcc.processInput("player", parameters);
 	}
 
 	@Test (expected = ChessManagerException.class)
 	public void processInputTwoParameterTest() throws ChessManagerException, SQLException{
 		parameters.add("p1");
 		parameters.add("p2");
 		mcc.processInput("player", parameters);
 	}
 
 	@Test
 	public void processInputAiTest() throws Throwable{
 		parameters.add("ai");	
 		ChessStateVO vo = new ChessStateVO();
 		vo.setId(1);
 		EasyMock.expect(dao.findNotFinishedGameByPlayer("player1")).andReturn(state);
 		uci.init();
 		EasyMock.expect(uci.calculateMove("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 2000)).andReturn("a2a3");
 		uci.destroy();		
 		dao.updateTable(vo);
 		EasyMock.replay(dao);
 		EasyMock.replay(uci);
 		mcc.processInput("player1", parameters);		
 		
 		
 		EasyMock.verify(dao);
 		EasyMock.verify(uci);
 	}
 	
 	@Test
 	public void processInputPlayerTest() throws Throwable{
 		parameters.add("player");
 		parameters.add("a2a3");
 		ChessStateVO vo = new ChessStateVO();
 		vo.setId(1);
 		EasyMock.expect(dao.findNotFinishedGameByPlayer("player1")).andReturn(state);
 	
 		dao.updateTable(vo);
 		EasyMock.replay(dao);
 		EasyMock.replay(uci);
 		mcc.processInput("player1", parameters);		
 	
 		EasyMock.verify(dao);
 
 	}
 	
 
 	@Test
 	public void processInputBothTest() throws Throwable{
 		parameters.add("both");
 		parameters.add("a2a3");
 		ChessStateVO vo = new ChessStateVO();
 		vo.setId(1);
 		EasyMock.expect(dao.findNotFinishedGameByPlayer("player1")).andReturn(state);
 		uci.init();
 		EasyMock.expect(uci.calculateMove("rnbqkbnr/pppppppp/8/8/8/P7/1PPPPPPP/RNBQKBNR b KQkq - 0 1", 2000)).andReturn("a7a6");
 		uci.destroy();		
 		dao.updateTable(vo);
 		EasyMock.replay(dao);
 		EasyMock.replay(uci);
 		mcc.processInput("player1", parameters);		
 		
 		
 		EasyMock.verify(dao);
 		EasyMock.verify(uci);
 	}
 	
 	
 	@Test (expected = ChessManagerException.class)
 	public void processInputInvalidMoveTest() throws Throwable{
 		parameters.add("a2a7");	
 		ChessStateVO vo = new ChessStateVO();
 		vo.setId(1);
 		EasyMock.expect(dao.findNotFinishedGameByPlayer("player1")).andReturn(state);
 		uci.init();
 		EasyMock.expect(uci.calculateMove("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 2000)).andReturn("a2a3");
 		uci.destroy();		
 		dao.updateTable(vo);
 		EasyMock.replay(dao);
 		EasyMock.replay(uci);
 		mcc.processInput("player1", parameters);				
 		EasyMock.verify(dao);
 		EasyMock.verify(uci);
 	}
 	
 
 	@Test 
 	public void processInputValidMoveTest() throws Throwable{
 		parameters.add("c2c3");	
 		ChessStateVO vo = new ChessStateVO();
 		vo.setId(1);
 		EasyMock.expect(dao.findNotFinishedGameByPlayer("player1")).andReturn(state);
 		uci.init();
 		EasyMock.expect(uci.calculateMove("rnbqkbnr/pppppppp/8/8/8/2P5/PP1PPPPP/RNBQKBNR b KQkq - 0 1", 2000)).andReturn("a7a6");
 		uci.destroy();		
 		dao.updateTable(vo);
 		EasyMock.replay(dao);
 		EasyMock.replay(uci);
 		mcc.processInput("player1", parameters);				
 		EasyMock.verify(dao);
 		EasyMock.verify(uci);
 	}
 	
 	@Test (expected = ChessManagerException.class)
 	public void processInputUCIExeptionTest() throws Throwable{
 		parameters.add("c2c3");	
 		ChessStateVO vo = new ChessStateVO();
 		vo.setId(1);
 		EasyMock.expect(dao.findNotFinishedGameByPlayer("player1")).andReturn(state);
 		uci.init();
 		EasyMock.expect(uci.calculateMove("rnbqkbnr/pppppppp/8/8/8/2P5/PP1PPPPP/RNBQKBNR b KQkq - 0 1", 2000)).andThrow(new UCIException(""));
 		uci.destroy();		
 		dao.updateTable(vo);
 		EasyMock.replay(dao);
 		EasyMock.replay(uci);
 		mcc.processInput("player1", parameters);				
 		EasyMock.verify(dao);
 		EasyMock.verify(uci);
 	}
 	
 	@Test 
 	public void getCurrentGameTest() throws SQLException, ChessManagerException {
 		EasyMock.expect(dao.findNotFinishedGameByPlayer("player1")).andReturn(state);
 		
 		EasyMock.replay(dao);
 		assertEquals(state.get(0),mcc.getCurrentGame("player1"));
 		
 		EasyMock.verify(dao);
 	}
 	
 	@Test (expected = ChessManagerException.class)
 	public void getCurrentGameSQLExceptionTest() throws SQLException, ChessManagerException{
 		EasyMock.expect(dao.findNotFinishedGameByPlayer("player1")).andThrow(new SQLException());
 		EasyMock.replay(dao);
 		mcc.getCurrentGame("player1");
 		
 		EasyMock.verify(dao);
 	}
 	
 	@Test (expected = ChessManagerException.class)
 	public void getNoGameOpenTest() throws SQLException, ChessManagerException{
 		EasyMock.expect(dao.findNotFinishedGameByPlayer("player1")).andReturn(null);
 		EasyMock.replay(dao);
 		mcc.getCurrentGame("player1");
 		EasyMock.verify(dao);
 	}
 	
 	@Test 
 	public void parseMoveTypeTest() throws ChessManagerException{
 		parameters.add("ai");
 		assertEquals("AI",mcc.parseMoveType(parameters).toString());	
 		
 		parameters.clear();
 		parameters.add("player");
 		parameters.add("a2a3");
 		assertEquals("PLAYER", mcc.parseMoveType(parameters).toString());
 		
 		parameters.clear();
 		parameters.add("both");
 		parameters.add("a2a3");
 		assertEquals("AI_AFTER_PLAYER", mcc.parseMoveType(parameters).toString());
 	}
 	
 	@Test (expected = ChessManagerException.class)
 	public void parseMoveTypeAiFalseTest()throws ChessManagerException{
 		parameters.add("ai");
 		parameters.add("AI");
 		assertEquals("AI",mcc.parseMoveType(parameters).toString());	
 	}
 	
 	@Test (expected = ChessManagerException.class)
 	public void parseMoveTypePlayerFalseTest()throws ChessManagerException{
 		parameters.clear();
 		parameters.add("player");
 		assertEquals("PLAYER", mcc.parseMoveType(parameters).toString());
 	}
 	
 	@Test (expected = ChessManagerException.class)
 	public void parseMoveTypeBothFalseTest()throws ChessManagerException{
 		parameters.clear();
 		parameters.add("both");
 		parameters.add("a2a3");
 		parameters.add("");
 		assertEquals("AI_AFTER_PLAYER", mcc.parseMoveType(parameters).toString());
 	}
 	
 }
