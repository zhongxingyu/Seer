 package skype.shell;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class AliasProcessorImplTest { 
 	AliasProcessorImpl subject = new AliasProcessorImpl();
 	
 	@Test
 	public void onAliasCommand_ShouldGenerateAReplyTextRequest() {
		String message = "#alias foo #startpoll \"some poll\"";
 		
 		assertTrue(subject.understands(message));
 		
 		ReplyTextRequest aliasRequest = (ReplyTextRequest) subject.processMessage(null, message);
 		String reply = aliasRequest.getReplyText();
		assertEquals("Alias '#foo' created to expand to <#startpoll \"some poll\">", reply);
 	}
 	
 	@Test
 	public void onMessageThatMatchesAlias_ShouldExpandAlias(){
 		onAliasCommand_ShouldGenerateAReplyTextRequest();
 		String expandMessage = subject.expandMessage("#foo");
		assertEquals("#startpoll \"some poll\"", expandMessage);
	}
	
	@Test
	public void onMessageThatMatchesAliasAndHasArguments_ShouldExpandAliasWithArguments()
	{
		onAliasCommand_ShouldGenerateAReplyTextRequest();
		String expandMessage = subject.expandMessage("#foo 123");
		assertEquals("#startpoll \"some poll\" 123", expandMessage);
 	}
 	
 	@Test
 	public void onListAliasCommand_ShouldGenerateReplyTextMessageWithListOfAliases(){
 		subject.processMessage(null, "#alias foo1 expanded1");
 		subject.processMessage(null, "#alias foo2 expanded2");
 		ReplyTextRequest aliasRequest = (ReplyTextRequest) subject.processMessage(null, "#aliaslist");
 		String reply = aliasRequest.getReplyText();
 		assertEquals(
 				"Registered aliases:\n" +
 				"#foo1 : expanded1\n" +
 				"#foo2 : expanded2", 
 				reply);
 	}
 }
