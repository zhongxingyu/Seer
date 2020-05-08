 package dbs.project.service;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import dbs.project.collections.filter.FilterSubstitutionEvent;
 import dbs.project.dao.MatchDao;
 import dbs.project.dao.event.CardEventDao;
 import dbs.project.dao.event.MatchEndEventDao;
 import dbs.project.entity.KnockoutMatch;
 import dbs.project.entity.Player;
 import dbs.project.entity.event.player.GoalEvent;
 import dbs.project.entity.event.player.SubstitutionEvent;
 import dbs.project.exception.NewPlayerHasPlayedBefore;
 import dbs.project.exception.NoMatchWhistleEvent;
 import dbs.project.exception.NoSuchCard;
 import dbs.project.exception.NotInSameTeam;
 import dbs.project.exception.PlayerDoesNotPlay;
 import dbs.project.exception.PlayerDoesNotPlayForTeam;
 import dbs.project.exception.TeamLineUpComplete;
 import dbs.project.exception.TeamNotSet;
 import dbs.project.exception.TiedMatch;
 import dbs.project.helper.TestHelper;
 import dbs.project.util.Collections;
 import dbs.project.util.Substitution;
 
 public class MatchServiceTest {
 
 	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
 
 	KnockoutMatch match;
 	
 	@Before
 	public void setUp() throws Exception {
 		match = TestHelper.match();
 		MatchDao.save(match);
 		System.setOut(new PrintStream(outContent));
 
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		MatchDao.delete(match);
 		match = null;
 		System.setOut(null);
 
 	}
 
 	@Test
 	public void testSubstitutePlayerFromSameTeamPlaying() {
 		TestHelper.matchLineUp(match);
 		MatchDao.save(match);
 		
 		try {
 			MatchService.substitutePlayers(match.getGuestTeam().getPlayers().get(0), match.getGuestTeam().getPlayers().get(11), 23, match);
 		} catch (Exception e) {
 			fail(e.getClass().toString());
 		}
 		
 		List<SubstitutionEvent> substitutions = new ArrayList<SubstitutionEvent>();
 		Collections.filterAndChangeType(match.getEvents(), new FilterSubstitutionEvent(), substitutions);
 
 		assertEquals(1, substitutions.size());
 		assertEquals(match.getGuestTeam().getPlayers().get(0),substitutions.get(0).getInvolvedPlayer());
 		assertEquals(match.getGuestTeam().getPlayers().get(11),substitutions.get(0).getNewPlayer());
 		assertEquals((Integer)23, substitutions.get(0).getMinute().getFirst());
 		assertEquals((Integer)0, substitutions.get(0).getMinute().getSecond());
 		
 		
 	}
 	
 	
 	@Test
 	public void testSubstitutePlayerFromSameTeamNotPlaying() {
 		TestHelper.matchLineUp(match);
 		MatchDao.save(match);
 		
 		try {
 			MatchService.substitutePlayers(match.getGuestTeam().getPlayers().get(11), match.getGuestTeam().getPlayers().get(12), 23, match);
 		} catch (NewPlayerHasPlayedBefore e) {
 			fail("wrong exception: " + e.getClass());
 		} catch (PlayerDoesNotPlay e) {
 			assert(true);
 		} catch (NotInSameTeam e) {
 			fail("wrong exception: "+e.getClass());
 		}
 	}
 	
 	@Test
 	public void testSubstitutePlayersFromOtherTeamPlaying() {
 		TestHelper.matchLineUp(match);
 		MatchDao.save(match);
 		
 		try {
 			MatchService.substitutePlayers(match.getHostTeam().getPlayers().get(5), match.getGuestTeam().getPlayers().get(20), 23, match);
 		} catch (NewPlayerHasPlayedBefore e) {
 			fail("wrong exception: " + e.getClass());
 		} catch (PlayerDoesNotPlay e) {
 			fail("wrong exception: " + e.getClass());
 		} catch (NotInSameTeam e) {
 			assert(true);
 		}
 	}
 	
 	@Test
 	public void testSubstitutePlayerThatPlayedBefore() {
 		TestHelper.matchLineUp(match);
 		MatchDao.save(match);
 		
 		try {
 			MatchService.substitutePlayers(match.getGuestTeam().getPlayers().get(0), match.getGuestTeam().getPlayers().get(11), 23, match);
 		} catch (Exception e) {
 			fail(e.getClass().toString());
 		}
 
 		try {
 			MatchService.substitutePlayers(match.getGuestTeam().getPlayers().get(5), match.getGuestTeam().getPlayers().get(6), 23, match);
 		} catch (NewPlayerHasPlayedBefore e) {
 			assert(true);
 		} catch (PlayerDoesNotPlay e) {
 			fail("wrong exception: " + e.getClass());
 		} catch (NotInSameTeam e) {
 			fail("wrong exception: " + e.getClass());
 		}
 	}
 	
 	@Test
 	public void testInsertPlayerToMatch() {
 		try {
 			MatchService.insertPlayerToMatch(match.getGuestTeam().getPlayers().get(0), match);
 		} catch (PlayerDoesNotPlayForTeam e) {
 			fail("exception : " + e.getClass());
 		} catch (TeamLineUpComplete e) {
 			fail("exception : " + e.getClass());
 		}
 	}
 
 	@Test
 	public void testInsertPlayerToMatchNotInTeam() {
 		Player player = new Player("first","last","nick",null,null,12,23);
 		try {
 			MatchService.insertPlayerToMatch(player, match);
 		} catch (PlayerDoesNotPlayForTeam e) {
 			assert(true);
 		} catch (TeamLineUpComplete e) {
 			fail("wrong exception : " + e.getClass());
 		}
 	}
 
 	@Test
 	public void testInsertPlayerToMatchLineUpComplete() {
 		TestHelper.matchLineUp(match);
 		try {
 			MatchService.insertPlayerToMatch(match.getGuestTeam().getPlayers().get(0), match);
 		} catch (PlayerDoesNotPlayForTeam e) {
 			fail("exception : " + e.getClass());
 		} catch (TeamLineUpComplete e) {
 			assert(true);
 		}
 	}
 
 	@Test
 	public void testGetResult() {
 		TestHelper.playMatch(match);
 		MatchDao.save(match);
 		
 		assertEquals(match.getHostTeam().getName() + " - "
 				+ match.getGuestTeam().getName() + " " + "1"
 				+ " : " + "0" + " "
 				,MatchService.getResult(match));
 	}
 
 	@Test
 	public void testInsertGoalPlayerDoesNotPlay() {
 		Player player = new Player("f", "l", "n", null, null, 12, 23);
 		GoalEvent goal = new GoalEvent(match, 11, player, match.getHostTeam());
 		try {
 			MatchService.insertGoal(goal, player, match);
 		} catch (PlayerDoesNotPlay e) {
 			assert(true);
 		} catch (TeamNotSet e) {
 			fail("wrong exception : "+e.getClass());
 		} catch (PlayerDoesNotPlayForTeam e) {
 			fail("exception : "+e.getMessage());
 		}
 		
 	}
 
 	@Test
 	public void testInsertGoal() {
 		TestHelper.matchLineUp(match);
 		MatchDao.save(match);
 		
 		GoalEvent goal = new GoalEvent();
 		goal.setScorringTeam(match.getHostTeam());
 		goal.setMinute(23);
 		goal.setMatch(match);
 		
 		try {
 			MatchService.insertGoal(goal, match.getHostTeam().getPlayers().get(5), match);
 		} catch (Exception e) {
 			fail("exception : "+e.getClass());
 		}
 		
 		assertEquals((Integer)1, MatchService.getGoalsByTeam(match.getHostTeam(), match).getFirst());
 		assertEquals((Integer)0, MatchService.getGoalsByTeam(match.getHostTeam(), match).getSecond());
 		
 	}
 	
 	@Test
 	public void testGetPointsByTeam() {
 		TestHelper.matchLineUp(match);
 		TestHelper.playMatch(match);
 		MatchDao.save(match);
 		
 		assertEquals(3,MatchService.getPointsByTeam(match.getHostTeam(), match));
 		assertEquals(0,MatchService.getPointsByTeam(match.getGuestTeam(), match));
 		
 	}
 	
 	@Test
 	public void testGetPointsByTeamDraw() {
 		TestHelper.matchLineUp(match);
 		match.setPlayed(true);
 		MatchDao.save(match);
 		
 		assertEquals(1,MatchService.getPointsByTeam(match.getHostTeam(), match));
 		assertEquals(1,MatchService.getPointsByTeam(match.getGuestTeam(), match));
 		
 	}
 	
 	@Test
 	public void testGetPointsByTeamMatchNotPlayed() {
 		TestHelper.matchLineUp(match);
 		MatchDao.save(match);
 		
 		assertEquals(0,MatchService.getPointsByTeam(match.getHostTeam(), match));
 		assertEquals(0,MatchService.getPointsByTeam(match.getGuestTeam(), match));
 		
 	}
 
 	@Test
 	public void testGetGoalsByTeam() {
 		TestHelper.matchLineUp(match);
 		TestHelper.playMatch(match);
 		MatchDao.save(match);
 		
 		assertEquals((Integer)1,MatchService.getGoalsByTeam(match.getHostTeam(), match).getFirst());
 		assertEquals((Integer)0,MatchService.getGoalsByTeam(match.getHostTeam(), match).getSecond());
 		assertEquals((Integer)1,MatchService.getGoalsByTeam(match.getGuestTeam(), match).getSecond());
 		assertEquals((Integer)0,MatchService.getGoalsByTeam(match.getGuestTeam(), match).getFirst());
 	}
 
 
 	@Test
 	public void testGetWinner() {
 		TestHelper.matchLineUp(match);
 		TestHelper.playMatch(match);
 		MatchDao.save(match);
 		
 		try {
 			assertEquals(match.getHostTeam(), MatchService.getWinner(match));
 		} catch (TiedMatch e) {
 			fail("match shouldn't be tied");
 		}
 	}
 
 	@Test
 	public void testGetWinnerTied() {
 		TestHelper.matchLineUp(match);
 		match.setPlayed(true);
 		MatchDao.save(match);
 		
 		try {
 			MatchService.getWinner(match);
 			fail("should not work");
 		} catch (TiedMatch e) {
 			assert(true);
 		}
 	}
 	
 	@Test
 	public void testGetLooser() {
 		TestHelper.matchLineUp(match);
 		TestHelper.playMatch(match);
 		MatchDao.save(match);
 		
 		try {
 			assertEquals(match.getGuestTeam(), MatchService.getLooser(match));
 		} catch (TiedMatch e) {
 			fail("match shouldn't be tied");
 		}
 	}
 
 	@Test
 	public void testGetLooserTied() {
 		TestHelper.matchLineUp(match);
 		match.setPlayed(true);
 		MatchDao.save(match);
 		
 		try {
 			MatchService.getLooser(match);
 			fail("should not work");
 		} catch (TiedMatch e) {
 			assert(true);
 		}
 	}
 
 	@Test
 	public void testGetFinalWhistleTime() {
 		TestHelper.matchLineUp(match);
 		TestHelper.playMatch(match);
 		MatchDao.save(match);
 		
 		try {
 			assertEquals((Integer)90,MatchService.getFinalWhistleTime(match).getFirst());
 		} catch (NoMatchWhistleEvent e) {
 			fail("no Match End Event");
 		}
 		try {
 			assertEquals((Integer)0,MatchService.getFinalWhistleTime(match).getSecond());
 		} catch (NoMatchWhistleEvent e) {
 		}
 		
 	}
 
 	@Test
 	public void testGetHostLineup() {
 		TestHelper.matchLineUp(match);
 		MatchDao.save(match);
 		int i = 1;
 		for(Player player : MatchService.getHostLineup(match)){
 			assertEquals("player "+i, player.getName());
 			i++;
 		}
 	}
 
 	@Test
 	public void testGetGuestLineup() {
 		TestHelper.matchLineUp(match);
 		MatchDao.save(match);
 		int i = 24;
 		for(Player player : MatchService.getGuestLineup(match)){
 			assertEquals("player "+i, player.getName());
 			i++;
 		}
 	}
 
 	@Test
 	public void testGetLineupForTeam() {
 		TestHelper.matchLineUp(match);
 		MatchDao.save(match);
 		int i = 1;
 		for(Player player : MatchService.getLineupForTeam(match.getHostTeam(), match)){
 			assertEquals("player "+i, player.getName());
 			i++;
 		}
 	}
 
 	@Test
 	public void testSetLineup() {
 
 		List<Player> players= new ArrayList<Player>();
 		players.addAll(match.getHostTeam().getPlayers().subList(0, 11));
 		players.addAll(match.getGuestTeam().getPlayers().subList(0,11));
 		try {
 			MatchService.setLineup(players, match);
 		} catch (Exception e) {
 			fail(e.getClass().toString());
 		}
 		int i=1;
 		for(Player player :MatchService.getLineupByMatch(match)){
 			assertEquals("player "+i, player.getName());
 			i++;
 			//guestplayers
 			if(i==12) i = 24;
 		}
 	}
 
 	@Test
 	public void testAddCard() {
 		try {
 			MatchService.addCard(12, match.getGuestTeam().getPlayers().get(0), "yellow", match);
 		} catch (NoSuchCard e) {
 			fail("no such card color");
 		}
 		
 		assertEquals(1,CardEventDao.findAllByMatch(match).size());
 		assertEquals(match.getGuestTeam().getPlayers().get(0), CardEventDao.findAllByMatch(match).get(0).getInvolvedPlayer());
 		assertEquals((Integer)12,CardEventDao.findAllByMatch(match).get(0).getMinute().getFirst());
 		assertEquals("yellow", CardEventDao.findAllByMatch(match).get(0).getColor());
 	
 	}
 
 	@Test
 	public void testAddCardNoSuchCard() {
 		try {
 			MatchService.addCard(12, match.getGuestTeam().getPlayers().get(0), "green", match);
 			assert(false);
 		} catch (NoSuchCard e) {
 			assert(true);
 		}
 	}
 	
 	@Test
 	public void testGetSubstitutedPlayersByTeamForMatch() {
 		TestHelper.matchLineUp(match);
 		TestHelper.playMatch(match);
 		MatchDao.save(match);
 		
 		assertEquals(1,MatchService.getSubstituedPlayersByTeamForMatch(match.getHostTeam(), match).size());
 		assertEquals((Integer)45,MatchService.getSubstituedPlayersByTeamForMatch(match.getHostTeam(), match).get(0).getMinute().getFirst());
 		assertEquals((Integer)0,MatchService.getSubstituedPlayersByTeamForMatch(match.getHostTeam(), match).get(0).getMinute().getSecond());
 		assertEquals(match.getHostTeam().getPlayers().get(11),MatchService.getSubstituedPlayersByTeamForMatch(match.getHostTeam(), match).get(0).getPlayerIn());
 		assertEquals(match.getHostTeam().getPlayers().get(1),MatchService.getSubstituedPlayersByTeamForMatch(match.getHostTeam(), match).get(0).getPlayerOut());
 			
 	}
 
 	@Test
 	public void testSetFinalWhistle() {
 		MatchService.setFinalWhistle(90, match);
 		
 		assertEquals(1,MatchEndEventDao.findAllByMatch(match).size());
 		assertEquals((Integer)90,MatchEndEventDao.findAllByMatch(match).get(0).getMinute().getFirst());
 		assertEquals((Integer)0,MatchEndEventDao.findAllByMatch(match).get(0).getMinute().getSecond());
 			
 		
 	}
 
 }
