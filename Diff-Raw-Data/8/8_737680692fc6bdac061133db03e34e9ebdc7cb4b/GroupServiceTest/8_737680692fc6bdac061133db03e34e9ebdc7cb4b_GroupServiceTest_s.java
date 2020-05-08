 package dbs.project.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import dbs.project.dao.TournamentGroupDao;
 import dbs.project.entity.GroupMatch;
 import dbs.project.entity.Stadium;
 import dbs.project.entity.Team;
 import dbs.project.entity.TournamentGroup;
 import dbs.project.exception.NoGroupMatchesSet;
 
 public class GroupServiceTest {
 
 	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
 
 	TournamentGroup group;
 
 	@Before
 	public void setUp() throws Exception {
 		System.setOut(new PrintStream(outContent));
 
 		Team team1 = new Team("team1", null, null, null, null);
 		Team team2 = new Team("team2", null, null, null, null);
 		Team team3 = new Team("team3", null, null, null, null);
 		Team team4 = new Team("team4", null, null, null, null);
 
 		List<Team> teams = new ArrayList<Team>();
 		teams.add(team1);
 		teams.add(team2);
 		teams.add(team3);
 		teams.add(team4);
 
 		GroupMatch match1 = new GroupMatch();
 		GroupMatch match2 = new GroupMatch();
 		GroupMatch match3 = new GroupMatch();
 		GroupMatch match4 = new GroupMatch();
 		GroupMatch match5 = new GroupMatch();
 		GroupMatch match6 = new GroupMatch();
 
 		match1.setHostTeam(team1);
 		match1.setGuestTeam(team2);
 
 		match2.setHostTeam(team3);
 		match2.setGuestTeam(team4);
 
 		match3.setHostTeam(team2);
 		match3.setGuestTeam(team3);
 
 		match4.setHostTeam(team4);
 		match4.setGuestTeam(team1);
 
 		match5.setHostTeam(team3);
 		match5.setGuestTeam(team1);
 
 		match6.setHostTeam(team2);
 		match6.setGuestTeam(team4);
 
 		Date date1 = new Date(1000);
 		Date date2 = new Date(24);
 		Date date3 = new Date(23);
 		Date date4 = new Date(42);
 		Date date5 = new Date(32);
 		Date date6 = new Date(2334);
 
 		match1.setDate(date1);
 		match2.setDate(date2);
 		match3.setDate(date3);
 		match4.setDate(date4);
 		match5.setDate(date5);
 		match6.setDate(date6);
 
 		Stadium stadium1 = new Stadium();
 		Stadium stadium2 = new Stadium();
 		Stadium stadium3 = new Stadium();
 		Stadium stadium4 = new Stadium();
 		Stadium stadium5 = new Stadium();
 		Stadium stadium6 = new Stadium();
 
 		stadium1.setName("stadium1");
 		stadium2.setName("stadium2");
 		stadium3.setName("stadium3");
 		stadium4.setName("stadium4");
 		stadium5.setName("stadium5");
 		stadium6.setName("stadium6");
 
 		match1.setStadium(stadium1);
 		match2.setStadium(stadium2);
 		match3.setStadium(stadium3);
 		match4.setStadium(stadium4);
 		match5.setStadium(stadium5);
 		match6.setStadium(stadium6);
 
 		List<GroupMatch> matchs = new ArrayList<GroupMatch>();
 		matchs.add(match1);
 		matchs.add(match2);
 		matchs.add(match3);
 		matchs.add(match4);
 		matchs.add(match5);
 		matchs.add(match6);
 
 		group = new TournamentGroup();
 
 		group.setTeams(teams);
 		group.setMatches(matchs);
 		
 		TournamentGroupDao.save(group);
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		System.setOut(null);
 		group = null;
 	}
 
 	@Test
 	public void testPrintStandings() {
 		try {
 			GroupService.printStandings(group);
 		} catch (NoGroupMatchesSet e) {
 			fail("group has no matches");
 		}
 		assertEquals(
 				"1.	          team1		0		0:0		0\n\n2.	          team2		0		0:0		0\n\n3.	          team3		0		0:0		0\n\n4.	          team4		0		0:0		0\n\n",
 				outContent.toString());
 
 	}
 
 	@Test
 	public void testGetSchedule() {
 
 		StringBuilder outPut = new StringBuilder();
 		for (GroupMatch match : group.getMatches()) {
 			outPut.append(match.toString() + "\n");
 		}
 
 		assertEquals(outPut.toString(), GroupService.getSchedule(group));
 
 	}
 
 }
