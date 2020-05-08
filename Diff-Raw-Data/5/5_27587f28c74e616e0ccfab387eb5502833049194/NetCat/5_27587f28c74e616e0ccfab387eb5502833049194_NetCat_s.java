 package uk.co.harcourtprogramming.netcat;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Logger;
 import java.util.logging.LogRecord;
 import java.util.logging.Level;
 import java.io.IOException;
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.IrcException;
 
 public class NetCat extends PircBot implements Runnable
 {
 	public class Message
 	{
 		private final String message;
 		private final String nick;
 		private final String channel;
 		private boolean dispose = false;
 
 		private Message(String message, String nick, String channel)
 		{
 			this.message = message; // TODO: Remove escape codes
 			this.nick = nick;
 			this.channel = channel;
 		}
 
 		public String getMessage()
 		{
 			return this.message;
 		}
 
 		public String getChannel()
 		{
 			return this.channel;
 		}
 
 		public String getSender()
 		{
 			return this.nick;
 		}
 
 		public synchronized void reply(String message)
 		{
 			for (String s : message.split("\n"))
 			{
 				NetCat.this.sendMessage(this.nick, s);
 			}
 		}
 
 		public synchronized void replyToAll(String message)
 		{
 			if (this.channel == null)
 			{
 				this.reply(message);
 				return;
 			}
 			for (String s : message.split("\n"))
 			{
 				NetCat.this.sendMessage(this.channel, s);
 			}
 		}
 
 		public void dispose()
 		{
 			this.dispose = true;
 		}
 	}
 
 	private final static Logger log = Logger.getLogger("NetCat");
 	private final static Formatter form = new Formatter()
 	{
 		public String format(LogRecord l)
 		{
 			Calendar time = Calendar.getInstance();
 			time.setTimeInMillis(l.getMillis());
 
 			StringBuilder b = new StringBuilder();
 
 			b.append('[');
 			b.append(time.get(Calendar.HOUR_OF_DAY));
 			b.append(':');
 			b.append(time.get(Calendar.MINUTE));
 			b.append(' ');
 			b.append(l.getLevel().getLocalizedName());
 			b.append("] >> ");
 			b.append(formatMessage(l));
 			b.append('\n');
 
 			return b.toString();
 		}
 	};
 	static
 	{
 		Handler h = new ConsoleHandler();
 		h.setFormatter(form);
 		log.addHandler(h);
 		log.setUseParentHandlers(false);
 	}
 
 	private final String host;
 	private final String channel;
 	private final List<Service> srvs = new ArrayList<Service>();
 	private final List<MessageService> msrvs = new ArrayList<MessageService>();
 	private boolean dispose = false;
 
 	public NetCat(String host, String channel)
 	{
 		this.setName("DoCitten");
 		this.host = host;
 
 		if (channel.charAt(0) == '#')
 		{
 			this.channel = channel;
 		}
 		else
 		{
 			this.channel = '#' + channel;
 		}
 
 		this.setVerbose(false);
 	}
 
 	public void addService(Service s)
 	{
 		synchronized(srvs)
 		{
 			if (dispose) return;
 			log.log(Level.INFO, "Service Loaded: " + s.getClass().getSimpleName() + '@' + s.getId());
 			srvs.add(s);
 			if (s instanceof MessageService)
 			{
 				msrvs.add((MessageService)s);
 				log.log(Level.INFO, "Service " + s.getClass().getSimpleName() + '@' + s.getId() + " loaded as MessageService.");
 			}
 		}
 	}
 
 	public synchronized void run()
 	{
 		try
 		{
 			log.log(Level.INFO, "Connecting to '" + host + "'");
 			this.connect(host);
 			log.log(Level.INFO, "Joining '" + channel + "'");
 			this.joinChannel(channel);
 			log.log(Level.INFO, "Operations Running!");
 			this.wait();
 		}
 		catch (IOException ex)
 		{
 			log.log(Level.SEVERE, null, ex);
 		}
 		catch (IrcException ex)
 		{
 			log.log(Level.SEVERE, null, ex);
 		}
 		catch (InterruptedException ex)
 		{
 		}
 
 		// Shutdown procedure :)
 		synchronized(srvs)
 		{
 			dispose = true;
 			for (Service s : srvs) s.shutdown();
 		}
 		this.quitServer();
 		this.disconnect();
 		this.dispose();
 	}
 
 	public void onMessage(String channel, String sender, String login, String hostname, String message)
 	{
		log.log(Level.INFO, "Message received from " + sender + '/' + channel);
 		final Message m = new Message(message, sender, channel);
 		synchronized(srvs)
 		{
 			for (MessageService s : msrvs)
 			{
				log.log(Level.INFO, "Message dispatched to " + s.getClass().getSimpleName() + '@' + s.getId());
 				try
 				{
 					s.handle(m);
 				}
 				catch (Throwable ex)
 				{
 					log.log(Level.SEVERE, "Error whilst passing message to " + s.getClass().getSimpleName() + '@' + s.getId(), ex);
 				}
 				if (m.dispose) break;
 			}
 		}
 	}
 
 	public void onPrivateMessage(String sender, String login, String hostname, String message)
 	{
 		onMessage(null, sender, login, hostname, message);
 	}
 
 	public synchronized void shutdown()
 	{
 		this.notifyAll(); // run() waits to stop thread being killed; exits when notified
 	}
 
 }
 
