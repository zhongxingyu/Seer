 package skype.shell;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class AliasProcessorImplTest { 
 	AliasProcessorImpl subject = new AliasProcessorImpl();
 	
 	@Test
 	public void onAliasCommand_ShouldGenerateAReplyTextRequest() {
		String message = "#alias foo #understood_command";
 		
 		assertTrue(subject.understands(message));
 		
 		ReplyTextRequest aliasRequest = (ReplyTextRequest) subject.processMessage(null, message);
 		String reply = aliasRequest.getReplyText();
		assertEquals("Alias '#foo' created to expand to <#understood_command>", reply);
 	}
 	
 	@Test
 	public void onMessageThatMatchesAlias_ShouldExpandAlias(){
 		onAliasCommand_ShouldGenerateAReplyTextRequest();
 		String expandMessage = subject.expandMessage("#foo");
		assertEquals("#understood_command", expandMessage);
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
