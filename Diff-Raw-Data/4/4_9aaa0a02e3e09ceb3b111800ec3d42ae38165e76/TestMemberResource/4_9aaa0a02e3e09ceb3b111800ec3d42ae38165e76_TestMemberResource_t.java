 package de.kile.zapfmaster2000.rest.api.member;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.kile.zapfmaster2000.rest.AbstractMockingTest;
 import de.kile.zapfmaster2000.rest.api.members.MemberResponse;
 import de.kile.zapfmaster2000.rest.api.members.MemberResponse.GainedUserAchievement;
 import de.kile.zapfmaster2000.rest.api.members.MembersResource;
 import de.kile.zapfmaster2000.rest.core.auth.AuthService;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Achievement;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Box;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Keg;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Sex;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.UserType;
 
 public class TestMemberResource extends AbstractMockingTest {
 
 	private Account account1;
 	private Box box1;
 	private User user1;
 	private User user2;
 	private User user3;
 	private Achievement achievement1;
 	private Achievement achievement2;
 	private Keg keg1;
 
 	@Before
 	public void setupData() {
 		// truncate at first! This way, we get rid of any achievements that are
 		// being created on start up
		truncate();
 
 		account1 = createAccount("foo-account");
 		Account account2 = createAccount("bar-account");
 		box1 = createBox("box-pp", "somewhere", "1.0", account1);
 		keg1 = createKeg("brand-1", new Date(), null, 50, box1);
 		user1 = createUser("Torsten", "img/user1", "user1-pw", 101, Sex.MALE,
 				85, UserType.USER, account1);
 		user2 = createUser("Bettina", "img/user2", "user2-pw", 202, Sex.FEMALE,
 				85, UserType.USER, account1);
 		user3 = createUser("Jutta", "img/user3", "user3-pw", 303, Sex.FEMALE,
 				85, UserType.USER, account1);
 		// user4 in account2
 		createUser("Detlef", "img/user3", "user3-pw", 303, Sex.MALE, 85,
 				UserType.USER, account2);
 
 		achievement1 = createAchievement("achievement-1", "desc1", "img/ach1");
 		achievement2 = createAchievement("achievement-2", "desc2", "img/ach2");
 
 		createGainedAchievement(createDate(2012, 1, 1), user1, achievement1);
 		createGainedAchievement(createDate(2012, 2, 1), user1, achievement2);
 		createGainedAchievement(createDate(2012, 3, 1), user2, achievement1);
 
 		createDrawing(0.3, new Date(), keg1, user1);
 		createDrawing(0.6, new Date(), keg1, user1);
 		createDrawing(0.5, new Date(), keg1, user2);
 
 		AuthService authService = mock(AuthService.class);
 		when(authService.retrieveAccount(null)).thenReturn(account1);
 		mockAuthService(authService);
 	}
 
 	@Test
 	public void testSimple() {
 		MembersResource membersResource = new MembersResource();
 		Response response = membersResource.retrieveMembers(null);
 		assertEquals(Status.OK.getStatusCode(), response.getStatus());
 
 		@SuppressWarnings("unchecked")
 		List<MemberResponse> memberResponse = (List<MemberResponse>) response
 				.getEntity();
 		// expecting 3 members: user4 ("detlef") is used in other account
 		assertEquals(3, memberResponse.size());
 
 		// order by name
 		MemberResponse m1 = memberResponse.get(0);
 		assertEquals("Bettina", m1.getUserName());
 		assertEquals("img/user2", m1.getImagePath());
 		assertEquals(0.5, m1.getTotalAmount(), 0.05);
 		assertEquals(user2.getId(), m1.getUserId());
 		assertEquals(1, m1.getAchievements().size());
 
 		GainedUserAchievement a1_1 = m1.getAchievements().get(0);
 		assertEquals(achievement1.getId(), a1_1.getAchievementId());
 		assertEquals(achievement1.getName(), a1_1.getAchievementName());
 		assertEquals(achievement1.getImagePath(), a1_1.getImagePath());
 
 		MemberResponse m2 = memberResponse.get(1);
 		assertEquals("Jutta", m2.getUserName());
 		assertEquals("img/user3", m2.getImagePath());
 		assertEquals(0, m2.getTotalAmount(), 0.05);
 		assertEquals(user3.getId(), m2.getUserId());
 		assertEquals(0, m2.getAchievements().size());
 		
 		MemberResponse m3 = memberResponse.get(2);
 		assertEquals("Torsten", m3.getUserName());
 		assertEquals("img/user1", m3.getImagePath());
 		assertEquals(0.9, m3.getTotalAmount(), 0.05);
 		assertEquals(user1.getId(), m3.getUserId());
 		assertEquals(2, m3.getAchievements().size());
 
 		// ordered by date
 		GainedUserAchievement a3_1 = m3.getAchievements().get(0);
 		assertEquals(achievement2.getId(), a3_1.getAchievementId());
 		assertEquals(achievement2.getName(), a3_1.getAchievementName());
 		assertEquals(achievement2.getImagePath(), a3_1.getImagePath());
 		
 		GainedUserAchievement a3_2 = m3.getAchievements().get(1);
 		assertEquals(achievement1.getId(), a3_2.getAchievementId());
 		assertEquals(achievement1.getName(), a3_2.getAchievementName());
 		assertEquals(achievement1.getImagePath(), a3_2.getImagePath());
 	}
 
 }
