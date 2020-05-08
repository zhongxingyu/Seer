 package uk.co.harcourtprogramming.internetrelaycats;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.Queue;
import uk.co.harcourtprogramming.mewler.LocalBufferedSocket;
 
 public final class TestingRelayCat extends BasicRelayCat
 {
 	private Queue<Message> messageQueue = new LinkedList<Message>();
 
 	private static class TCatBot extends CatBot
 	{
 
 		private TCatBot(TestingRelayCat cat) throws IOException
 		{
			super(cat, new LocalBufferedSocket().in, new LocalBufferedSocket().out, null);
 		}
 
 		@Override
 		public String getNick()
 		{
 			return NAME;
 		}
 	}
 
 	@SuppressWarnings("PublicInnerClass")
 	public static class RelayService extends Service implements MessageService
 	{
 
 		public RelayService()
 		{
 			super();
 		}
 
 		@Override
 		public void shutdown()
 		{
 			// Nothing to see here. Move along, citizen.
 		}
 
 		@Override
 		public void handle(Message m)
 		{
 			if (m.isAction())
 			{
 				m.act(m.getSender(), m.getMessage());
 				if (m.getChannel() != null)
 					m.act(m.getChannel(), m.getMessage());
 			}
 			else
 			{
 				m.message(m.getSender(), m.getMessage());
 				if (m.getChannel() != null)
 					m.message(m.getChannel(), m.getMessage());
 			}
 		}
 
 	}
 
 	public final static String NAME = "testBost";
 
 	public TestingRelayCat() throws UnknownHostException, IOException
 	{
 		super(NAME, "", null);
 		bot = new TCatBot(this);
 	}
 
 	@Override
 	public synchronized void run()
 	{
 		throw new UnsupportedOperationException("TestingRelayCat is not for running; it is merely an input ouput queue");
 	}
 
 	@Override
 	public void message(String target, String message)
 	{
 		messageQueue.offer(new Message(message, "name", target, false, this));
 	}
 
 	@Override
 	public void act(String target, String action)
 	{
 		messageQueue.offer(new Message(action, "name", target, true, this));
 	}
 
 	public Message getOutput()
 	{
 		return messageQueue.poll();
 	}
 
 	public void inputMessage(String sender, String channel, String line)
 	{
 		bot.onMessage(sender, null, channel, line);
 	}
 
 	public void inputAction(String sender, String channel, String line)
 	{
 		bot.onAction(sender, null, (channel==null ? NAME : channel), line);
 	}
 
 }
