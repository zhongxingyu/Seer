 package tests;
 
 import java.text.ParseException;
 
 import models.Answer;
 import models.Question;
 import models.SystemInformation;
 import models.Tag;
 import models.User;
 import models.database.Database;
 import models.helpers.Tools;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 import tests.mocks.SystemInformationMock;
 
 public class UserTest extends UnitTest {
 
 	@Before
 	public void setUp() {
 		Database.clear();
 	}
 
 	@Test
 	public void shouldCreateUser() {
 		User user = new User("Jack", "jack");
 		assertTrue(user != null);
 	}
 
 	@Test
 	public void shouldBeCalledJack() {
 		User user = new User("Jack", "jack");
 		assertEquals(user.getName(), "Jack");
 	}
 
 	@Test
 	public void checkUsernameAvailable() {
 		assertTrue(Database.get().users().isAvailable("JaneSmith"));
 		Database.get().users().register("JaneSmith", "janesmith", "jane@smith.com");
 		assertFalse(Database.get().users().isAvailable("JaneSmith"));
 		assertFalse(Database.get().users().isAvailable("janesmith"));
 		assertFalse(Database.get().users().isAvailable("jAnEsMiTh"));
 	}
 
 	@Test
 	public void shouldCheckeMailValidation() {
 		assertTrue(Tools.checkEmail("john@gmx.com"));
 		assertTrue(Tools.checkEmail("john.smith@students.unibe.ch"));
 		assertFalse(Tools.checkEmail("john@gmx.c"));
 		assertFalse(Tools.checkEmail("john@info.museum"));
 		assertFalse(Tools.checkEmail("john@...com"));
 	}
 
 	@Test
 	public void checkMailAssertion() {
 		User user = new User("Bill", "bill");
 		user.setEmail("bill@aol.com");
 		assertEquals(user.getEmail(), "bill@aol.com");
 	}
 
 	@Test
 	public void checkPassw() {
 		User user = new User("Bill", "bill");
 		assertTrue(user.checkPW("bill"));
 		assertEquals(Tools.encrypt("bill"), user.getSHA1Password());
 		user.setSHA1Password("bill2");
 		assertFalse(user.checkPW("bill"));
 	}
 
 	@Test
 	public void shouldEditProfileCorrectly() throws ParseException {
		SystemInformationMock sys = new SystemInformationMock();
		SystemInformation.mockWith(sys);
		sys.year(2010).month(12).day(3);

 		User user = new User("Jack", "jack");
 		assertEquals(user.getAge(), 0);
 
 		user.setDateOfBirth("14.9.1987");
 		user.setBiography("I lived");
 		user.setEmail("test@test.tt");
 		user.setEmployer("TestInc");
 		user.setFullname("Test Tester");
 		user.setProfession("tester");
 		user.setWebsite("http://www.test.ch");
 
 		assertEquals(user.getAge(), 23);
 		assertEquals(user.getDateOfBirth(), "14.09.1987");
 		assertEquals(user.getBiography(), "I lived");
 		assertEquals(user.getEmail(), "test@test.tt");
 		assertEquals(user.getEmployer(), "TestInc");
 		assertEquals(user.getFullname(), "Test Tester");
 		assertEquals(user.getProfession(), "tester");
 		assertEquals(user.getWebsite(), "http://www.test.ch");
 	}
 
 	@Test
 	public void checkForSpammer() {
 		User user = new User("Spammer", "spammer");
 		assertFalse(user.isBlocked());
 		assertEquals(user.getStatusMessage(), "");
 		assertTrue(user.howManyItemsPerHour() == 0);
 		new Question(user, "Why did the chicken cross the road?");
 		assertTrue(user.howManyItemsPerHour() == 1);
 		new Question(user, "Does anybody know?");
 		assertFalse(user.howManyItemsPerHour() == 1);
 		for (int i = 0; i < 57; i++) {
 			new Question(user, "This is my " + i + ". question");
 		}
 		assertTrue(!user.isSpammer());
 		assertTrue(user.howManyItemsPerHour() == 59);
 		assertTrue(!user.isCheating());
 		new Question(user, "My last possible Post");
 		assertTrue(user.isSpammer());
 		assertTrue(user.isCheating());
 		assertEquals(user.getStatusMessage(), "User is a Spammer");
 		assertTrue(user.isBlocked());
 	}
 
 	@Test
 	public void checkForCheater() {
 		User user = new User("TheSupported", "supported");
 		User user2 = new User("Cheater", "cheater");
 		assertFalse(user.isBlocked());
 		assertFalse(user2.isBlocked());
 		assertFalse(user2.isMaybeCheater());
 		assertEquals(user.getStatusMessage(), "");
 		assertEquals(user2.getStatusMessage(), "");
 		for (int i = 0; i < 5; i++) {
 			new Question(user, "This is my " + i + ". question").voteUp(user2);
 		}
 		assertTrue(user2.isMaybeCheater());
 		assertTrue(user2.isCheating());
 		assertTrue(user2.isBlocked());
 		assertEquals(user2.getStatusMessage(), "User voted up somebody");
 		assertFalse(user.isMaybeCheater());
 		assertFalse(user.isCheating());
 		assertFalse(user.isBlocked());
 		assertEquals(user.getStatusMessage(), "");
 	}
 	
 	@Test
 	public void shouldAllowVotingOften() {
 		User voter = new User("Voter", "voter");
 		User user1 = new User("User1", "user1");
 		User user2 = new User("User2", "user2");
 
 		for (int i = 0; i < 5; i++) {
 			new Question(user1, "Q1-" + i).voteUp(voter);
 			new Question(user2, "Q2-" + i).voteUp(voter);
 		}
 
 		assertFalse(voter.isMaybeCheater());
 		new Question(user1, "Q1-last").voteUp(voter);
 		assertTrue(voter.isMaybeCheater());
 	}
 
 	@Test
 	public void shouldNotBeAbleToEditForeignPosts() {
 		User user1 = new User("Jack", "jack");
 		User user2 = new User("John", "john");
 		User user3 = new User("Geronimo", "geronimo");
 		user1.setModerator(true);
 		Question q = new Question(user2, "Can you edit this post?");
 		/* moderator should be able to edit the question */
 		assertTrue(user1.canEdit(q));
 		/* owner should be able to edit the question */
 		assertTrue(user2.canEdit(q));
 		/* blocked owner should not be able to edit the question */
 		user2.block("for testing");
 		assertFalse(user2.canEdit(q));
 		/* user that is neither a moderator nor the owner of
 		   the question should NOT be able to edit the question */
 		assertFalse(user3.canEdit(q));
 	}
 
 	@Test
 	public void shouldHaveOneQuestion() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		assertEquals(1, user.getQuestions().size());
 		q.unregister();
 	}
 
 	@Test
 	public void shouldHaveNoQuestion() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.unregister();
 		assertEquals(0, user.getQuestions().size());
 	}
 
 	@Test
 	public void shouldHaveOneAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 		assertEquals(1, user.getAnswers().size());
 	}
 
 	@Test
 	public void shouldHaveNoAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 		q.answers().get(0).unregister();
 		assertEquals(0, user.getAnswers().size());
 	}
 
 	@Test
 	public void shouldHaveOneBestAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 		q.setBestAnswer(q.answers().get(0));
 		assertEquals(1, user.bestAnswers().size());
 	}
 
 	@Test
 	public void shouldHaveNoBestAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 		q.setBestAnswer(q.answers().get(0));
 		q.answers().get(0).unregister();
 		assertEquals(0, user.bestAnswers().size());
 	}
 
 	@Test
 	public void testModerator() {
 		User user = new User("Jack", "jack");
 		assertFalse(user.isModerator());
 		user.setModerator(true);
 		assertTrue(user.isModerator());
 	}
 
 	@Test
 	public void testBlock() {
 		User user = new User("Jack", "jack");
 		assertFalse(user.isBlocked());
 		assertEquals(user.getStatusMessage(), "");
 		user.block("offending comments");
 		assertTrue(user.isBlocked());
 		assertEquals(user.getStatusMessage(), "offending comments");
 		user.unblock();
 		assertFalse(user.isBlocked());
 		assertEquals(user.getStatusMessage(), "");
 
 	}
 
 	@Test
 	public void shouldHaveRecentEntries() {
 		SystemInformationMock sys = new SystemInformationMock();
 		SystemInformation.mockWith(sys);
 		sys.year(2000).month(6).day(6).hour(12).minute(0).second(0);
 
 		User user = new User("Jack", "jack");
 		assertEquals(0, user.getRecentQuestions().size());
 		assertEquals(0, user.getRecentAnswers().size());
 		assertEquals(0, user.getRecentComments().size());
 		Question question = new Question(user, "Question");
 		Answer answer = question.answer(user, "Answer");
 		question.comment(user, "Comment");
 		assertEquals(1, user.getRecentQuestions().size());
 		assertEquals(1, user.getRecentAnswers().size());
 		assertEquals(1, user.getRecentComments().size());
 
 		for (int i = 0; i < 4; i++) {
 			sys.second(i);
 			question.answer(user, "Answer " + i);
 		}
 		assertEquals(3, user.getRecentAnswers().size());
 		assertFalse(user.getRecentAnswers().contains(answer));
 	}
 
 	@Test
 	public void shouldHaveOneHighRatedAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 
 		assertEquals(0, user.highRatedAnswers().size());
 		assertTrue(Database.get().questions().countHighRatedAnswers() == 0);
 
 		User A = new User("A", "a");
 		User B = new User("B", "b");
 		User C = new User("C", "c");
 		User D = new User("D", "d");
 		User E = new User("E", "e");
 
 		q.answers().get(0).voteUp(A);
 		q.answers().get(0).voteUp(B);
 		q.answers().get(0).voteUp(C);
 		q.answers().get(0).voteUp(D);
 		q.answers().get(0).voteUp(E);
 
 		assertEquals(1, user.highRatedAnswers().size());
 		assertTrue(Database.get().questions().countHighRatedAnswers() > 0);
 
 		A.delete();
 		B.delete();
 		C.delete();
 		D.delete();
 		E.delete();
 
 		assertEquals(0, user.highRatedAnswers().size());
 	}
 
 	@Test
 	public void shouldSuggestQuestion() {
 		User user3 = new User("User3", "user3");
 		User user4 = new User("User4", "user4");
 		User user5 = new User("User5", "user5");
 		Question m = new Question(user3, "Why?");
 		Question n = new Question(user4, "Where?");
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		m.answer(user3, "Because");
 		m.answer(user4, "No idea");
 		n.answer(user5, "Therefore");
 
 		assertEquals(1, user5.getSuggestedQuestions().size());
 		assertEquals(m, user5.getSuggestedQuestions().get(0));
 
 		n.answer(user5, "and then some");
 		assertEquals(1, user5.getSuggestedQuestions().size());
 		assertEquals(m, user5.getSuggestedQuestions().get(0));
 	}
 
 	@Test
 	public void shouldSuggestThreeQuestions() {
 		User user3 = new User("User3", "user3");
 		User user4 = new User("User4", "user4");
 		User user5 = new User("User5", "user5");
 		Question m = new Question(user3, "Why?");
 		Question n = new Question(user4, "Where?");
 		Question o = new Question(user3, "Who?");
 		Question p = new Question(user4, "How old?");
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		o.setTagString("demo demo3 demo4");
 		p.setTagString("demo demo3 demo4 demo5");
 		m.answer(user3, "Because");
 		m.answer(user4, "No idea");
 		n.answer(user5, "Therefore");
 
 		assertEquals(3, user5.getSuggestedQuestions().size());
 		assertEquals(m, user5.getSuggestedQuestions().get(0));
 	}
 
 	@Test
 	public void shouldSuggestSixQuestionsMax() {
 		User user3 = new User("User3", "user3");
 		User user5 = new User("User5", "user5");
 		for (int i = 0; i < 10; i++) {
 			Question q = new Question(user3, "Hard question " + i);
 			q.setTagString("demo");
 		}
 
 		Question q = new Question(user3, "Simple question");
 		q.setTagString("demo");
 		q.answer(user5, "Simple!");
 
 		assertEquals(6, user5.getSuggestedQuestions().size());
 	}
 
 	@Test
 	public void shouldNotSuggestSameQuestionTwice() {
 		User user5 = new User("User5", "user5");
 		Question q = new Question(null, "suggest me!");
 		q.setTagString("demo");
 		Question r = new Question(null, "answer me!");
 		r.setTagString("demo");
 		r.answer(user5, "ok");
 		Question s = new Question(null, "answer me, too!");
 		s.setTagString("demo");
 		s.answer(user5, "ok");
 
 		assertEquals(1, user5.getSuggestedQuestions().size());
 		assertEquals(q, user5.getSuggestedQuestions().get(0));
 	}
 
 	@Test
 	public void shouldNotSuggestOldQuestions() {
 		SystemInformationMock sys = new SystemInformationMock();
 		SystemInformation.mockWith(sys);
 		sys.year(2000).month(6).day(6).hour(12).minute(0).second(0);
 
 		User user5 = new User("User5", "user5");
 		Question q = new Question(null, "suggest me!");
 		q.setTagString("demo");
 
 		Question r = new Question(null, "answer me!");
 		r.setTagString("demo");
 		r.answer(user5, "ok");
 
 		assertEquals(1, user5.getSuggestedQuestions().size());
 		sys.year(2001);
 		assertEquals(0, user5.getSuggestedQuestions().size());
 	}
 
 	@Test
 	public void shouldSuggestQuestionsFromBestAnswersFirst() {
 		User user3 = new User("User3", "user3");
 		User user4 = new User("User4", "user4");
 		User user5 = new User("User5", "user5");
 		Question m = new Question(user3, "Why?");
 		Question n = new Question(user4, "Where?");
 		Question o = new Question(user3, "Who?");
 		Question p = new Question(user4, "How old?");
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		o.setTagString("demo9 demo8");
 		p.setTagString("demo9 demo8");
 		m.answer(user3, "Because");
 		m.answer(user4, "No idea");
 		n.answer(user5, "Therefore");
 		o.answer(user5, "No");
 		o.setBestAnswer(user5.getAnswers().get(1));
 		assertEquals(2, user5.getSuggestedQuestions().size());
 		assertEquals(p, user5.getSuggestedQuestions().get(0));
 		assertEquals(m, user5.getSuggestedQuestions().get(1));
 
 	}
 
 	@Test
 	public void shouldSuggestQuestionsSortedByRatingOfAnswers() {
 		User user3 = new User("User3", "user3");
 		User user4 = new User("User4", "user4");
 		User user5 = new User("User5", "user5");
 		Question m = new Question(user3, "Why?");
 		Question n = new Question(user4, "Where?");
 		Question o = new Question(user3, "Who?");
 		Question p = new Question(user4, "How old?");
 		Question q = new Question(user3, "So?");
 		Question r = new Question(user4, "For ho long?");
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		o.setTagString("demo9 demo8");
 		p.setTagString("demo9 demo8");
 		q.setTagString("demo demo2 demo10");
 		r.setTagString("tag");
 		m.answer(user3, "Because");
 		m.answer(user4, "No idea");
 		n.answer(user5, "Therefore");
 		o.answer(user5, "No");
 		user5.getAnswers().get(1).voteUp(user3);
 		user5.getAnswers().get(1).voteUp(user4);
 
 		assertEquals(3, user5.getSuggestedQuestions().size());
 		assertEquals(q, user5.getSuggestedQuestions().get(0));
 		assertEquals(m, user5.getSuggestedQuestions().get(1));
 		assertEquals(p, user5.getSuggestedQuestions().get(2));
 
 	}
 
 	@Test
 	public void shouldNotSuggestQuestionsFromBadAnswers() {
 		User user6 = new User("User6", "user6");
 		User user7 = new User("User7", "user7");
 		User user8 = new User("User8", "user8");
 		Question m = new Question(user6, "Why?");
 		Question n = new Question(user7, "Where?");
 		Question o = new Question(user6, "Who?");
 		Question p = new Question(user7, "How old?");
 		m.answer(user6, "Because");
 		m.answer(user7, "No idea");
 		p.answer(user8, "Therefore");
 		user8.getAnswers().get(0).voteDown(user6);
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		o.setTagString("demo demo3 demo4");
 		p.setTagString("demo demo3 demo4 demo5");
 
 		assertEquals(0, user8.getSuggestedQuestions().size());
 	}
 
 	@Test
 	public void shouldNotSuggestOwnQuestions() {
 		User user = new User("Jack", "jack");
 		User user2 = new User("John", "john");
 		Question q = new Question(user, "Why?");
 		Question f = new Question(user2, "Where?");
 		q.setTagString("demo");
 		f.setTagString("demo");
 		q.answer(user2, "Because");
 		assertEquals(0, user2.getSuggestedQuestions().size());
 
 	}
 
 	@Test
 	public void shouldNotSuggestQuestionsWithBestAnswer() {
 		User james = new User("James", "james");
 		User john = new User("John", "john");
 		User kate = new User("Kate", "kate");
 		Question k = new Question(james, "Why?");
 		Question l = new Question(john, "Where?");
 
 		k.setTagString("demo");
 		l.setTagString("demo");
 		k.answer(james, "Because");
 		k.setBestAnswer(k.answer(john, "No idea"));
 		l.answer(kate, "Therefore");
 		assertEquals(0, kate.getSuggestedQuestions().size());
 	}
 
 	@Test
 	public void shouldNotSuggestQuestionsWithManyAnswers() {
 		User user3 = new User("User3", "user3");
 		User user5 = new User("User5", "user5");
 
 		Question p = new Question(user3, "Hard question");
 		p.setTagString("demo");
 		Question q = new Question(user3, "Simple question");
 		q.setTagString("demo");
 
 		q.answer(user5, "Simple!");
 		assertEquals(1, user5.getSuggestedQuestions().size());
 
 		for (int i = 0; i < 9; i++)
 			p.answer(null, "anonymous genious!");
 		assertEquals(1, user5.getSuggestedQuestions().size());
 		p.answer(null, "yet another anonymous genious!");
 		assertEquals(0, user5.getSuggestedQuestions().size());
 	}
 
 	@Test
 	public void shouldHaveDebugFriendly_toString() {
 		User james = new User("James", "james");
 		Question question = new Question(james, "Why?");
 		Answer answer = question.answer(james, "No idea");
 		question.setTagString("TAG");
 		Tag tag = question.getTags().get(0);
 		assertEquals(james.toString(), "U[James]");
 		assertEquals(question.toString(), "Question(Why?)");
 		assertEquals(answer.toString(), "Answer(No idea)");
 		assertEquals(tag.toString(), "Tag(tag)");
 	}
 }
