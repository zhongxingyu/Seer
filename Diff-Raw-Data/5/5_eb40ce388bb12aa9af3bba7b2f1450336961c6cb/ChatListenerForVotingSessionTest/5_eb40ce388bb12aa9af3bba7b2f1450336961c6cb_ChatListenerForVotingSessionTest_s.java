 package skype.voting;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import skype.shell.mocks.ChatBridgeMock;
 import skype.voting.mocks.VotingSessionMockAdapter;
 
 public class ChatListenerForVotingSessionTest { 
 	ChatBridgeMock chatBridgeMock = new ChatBridgeMock("autoid");
 	VotingSessionMockAdapter session = new VotingSessionMockAdapter();
 	ReplyListenerMock listener = new ReplyListenerMock();
 	VotingSessionMessageInterface messages = new VotingSessionMessages();
 	
 	@Test
 	public void onNewUserOnGivenChat_ShouldAddUserToVotingSession()
 	{
 		new ChatListenerForVotingSession(chatBridgeMock, session, listener, messages);
 		
 		assertEquals("tatu,uruca", session.getParticipants());
 		
 		chatBridgeMock.addParticipant("gamba");
 		
 		assertEquals("tatu,uruca,gamba", session.getParticipants());
 		session.getParticipants();
 
 		assertEquals(
 				"User 'gamba' added to the voting poll.\n" +
 				"Votes: foo: 2 ; baz: 3", 
 				listener.reply.get());
 		
 		assertEquals(
 				"Hey, we are having a voting poll. Come and join us. Here are the options:\n" + 
 				"Almoço!\n" + 
 				"1) foo\n" + 
 				"2) baz\n" +  
 				"Vote by using #1,#2, and so on",
 				listener.replyPrivate.get());
 	}
 	
 	@Test
 	public void onUserLeftOnGiveChat_ShouldRemoveFromVotingSession()
 	{
 		new ChatListenerForVotingSession(chatBridgeMock, session, listener, messages);
 		
 		chatBridgeMock.removeParticipant("tatu");
 		
 		assertEquals("uruca", session.getParticipants());
 		assertEquals(
 				"User 'tatu' left the voting poll.\n" +
 				"Update Votes: foo: 2 ; baz: 3", 
 				listener.reply.get());
 	}
}
