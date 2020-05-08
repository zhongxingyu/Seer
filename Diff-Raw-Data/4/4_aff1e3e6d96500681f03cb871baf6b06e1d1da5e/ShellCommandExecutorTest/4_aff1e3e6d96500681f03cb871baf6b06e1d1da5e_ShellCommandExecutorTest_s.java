 package skype.voting;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.junit.Test;
 
 import skype.ChatAdapterInterface;
 import skype.shell.ReplyListener;
 import skype.shell.ShellCommand;
 import skype.voting.mocks.ShellCommandMock;
 import skype.voting.mocks.ShellCommandProcessorMock;
 
 public class ShellCommandExecutorTest {
 	private ShellCommand mockCommand = new ShellCommandMock();
 	ShellCommandProcessorMock oneProcessor = new ShellCommandProcessorMock(true);
 	
 	@Test
 	public void onProcessWithoutAnyProcessors_ShouldReturnFalseIndicatingCommandNotProcessed()
 	{
 		ShellCommandExecutorInterface subject = new ShellCommandExecutor(){
 			@Override
 			protected ShellCommandProcessor[] getProcessors() {
 				return new ShellCommandProcessor[0];
 			}
 		};
 		boolean isProcessed = subject.processIfPossible(mockCommand);
 		
 		assertFalse(isProcessed);
 	}
 	
 	@Test
 	public void onProcessWithAProcessorThatAccepts_ShouldReturnTrueAndDelegate()
 	{ 
 		ShellCommandExecutorInterface subject = new ShellCommandExecutor(){
 
 			@Override
 			protected ShellCommandProcessor[] getProcessors() {
 				return new ShellCommandProcessor[]{oneProcessor};
 			}
 			
 		};
 		boolean isProcessed = subject.processIfPossible(mockCommand);
 		assertTrue(isProcessed);
 		assertTrue(oneProcessor.isProcessed());
 	}
 	
 	@Test
 	public void onProcessWithTwoProcessors_ShouldDelegateToTheOneThatAcceptsAndReturnTrue(){
 		final ShellCommandProcessorMock rejectsProcessor = new ShellCommandProcessorMock(false);
 		ShellCommandExecutorInterface subject = new ShellCommandExecutor(){
 			@Override
 			protected ShellCommandProcessor[] getProcessors() {
 				return new ShellCommandProcessor[]{rejectsProcessor, oneProcessor};
 			}
 		};
 		boolean isProcessed = subject.processIfPossible(mockCommand);
 		assertTrue(isProcessed);
 		assertTrue(oneProcessor.isProcessed());
 		assertFalse(rejectsProcessor.isProcessed());
 	}
 	
 	@Test
 	public void onProcessGivenFeedbackListener_ShouldInvokeFeedbackListener() {
 		ShellCommandExecutorInterface subject = new ShellCommandExecutor(){
 			@Override
 			protected ShellCommandProcessor[] getProcessors() {
				return new ShellCommandProcessor[0];
 			}
 		};
 		oneProcessor.setPublicReplyMessage("public reply message");
 		
 		final AtomicReference<String> replyMsg = new AtomicReference<String>("");
 		
 		subject.setReplyListener(new ReplyListener() {
 			public void onReplyPrivate(ChatAdapterInterface chatAdapterInterface, String reply) {
 				throw new RuntimeException("NOT IMPLEMENTED");
 			}
 			public void onReply(ChatAdapterInterface chatAdapterInterface, String reply) {
 				replyMsg.set(reply);
 			}
 		});
 		subject.processIfPossible(mockCommand);
 		assertEquals("public reply message", replyMsg.get());
 	}
 }
