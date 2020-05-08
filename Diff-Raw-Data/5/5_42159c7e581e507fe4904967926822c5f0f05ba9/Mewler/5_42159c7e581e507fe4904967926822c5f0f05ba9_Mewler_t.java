 package uk.co.harcourtprogramming.mewler;
 
 import uk.co.harcourtprogramming.mewler.servermesasges.IrcMessage;
 import uk.co.harcourtprogramming.mewler.servermesasges.AbstractIrcMessage;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.Calendar;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 import uk.co.harcourtprogramming.mewler.servermesasges.IrcPingMessage;
 import uk.co.harcourtprogramming.mewler.servermesasges.IrcResponseCode;
 import uk.co.harcourtprogramming.mewler.servermesasges.User;
 
 /**
  *
  */
 public class Mewler
 {
 	private final static Logger LOG = Logger.getLogger("InternetRelatCats.Mewler");
 	static
 	{
 		Handler h = new ConsoleHandler();
 		h.setFormatter(new Formatter()
 		{
 			@Override
 			public String format(LogRecord l)
 			{
 				Calendar time = Calendar.getInstance();
 				time.setTimeInMillis(l.getMillis());
 
 				return String.format("[%2$tR %1$s] %3$s\n",
 					l.getLevel().getLocalizedName(), time, formatMessage(l));
 			}
 
 			@Override
 			public synchronized String formatMessage(LogRecord record)
 			{
 				if (record.getMessage() == null)
 				{
 					if (record.getThrown() == null)
 					{
 						return String.format("null log from <%3s>%1s::%2s", record.getSourceClassName(), record.getSourceMethodName(), Thread.currentThread().getName());
 					}
 					else
 					{
 						Throwable thrown = record.getThrown();
 						return String.format("%s <%s>%s::%s\n\t%s",
 							thrown.getClass().getName(),
 							Thread.currentThread().getName(),
 							record.getSourceClassName(),
 							record.getSourceMethodName(),
 							thrown.getLocalizedMessage()
 						);
 					}
 				}
 				if (record.getThrown() == null)
 				{
 					return super.formatMessage(record);
 				}
 				Throwable thrown = record.getThrown();
 				return String.format("%s <%s>%s::%s\n\t%s\n\t%s",
 					thrown.getClass().getName(),
 					Thread.currentThread().getName(),
 					record.getSourceClassName(),
 					record.getSourceMethodName(),
 					super.formatMessage(record),
 					thrown.getLocalizedMessage()
 				);
 			}
 		});
 		LOG.addHandler(h);
 		LOG.setUseParentHandlers(false);
 	}
 
 	private final MewlerOut outputThread;
 	private final MewlerIn inputThread;
 	private String nick = null;
 
 	public Mewler(final InputStream input, final OutputStream output, final ThreadGroup tg)
 	{
 		if (input == null) throw new IllegalArgumentException("InputStream must be a valid, active stream");
 		try
 		{
 			input.available();
 		}
 		catch (IOException ex)
 		{
 			throw new IllegalArgumentException("InputStream must be a valid, active stream");
 		}
 
 		if (input == null) throw new IllegalArgumentException("InputStream must be a valid, active stream");
 		try
 		{
 			input.available();
 		}
 		catch (IOException ex)
 		{
 			throw new IllegalArgumentException("InputStream must be a valid, active stream");
 		}
 
 		outputThread = new MewlerOut(output, tg);
 		inputThread = new MewlerIn(new BufferedReader(new InputStreamReader(input)), tg, this);
 	}
 
 	public synchronized void connect(final String nick, final String password, final String realName) throws IOException
 	{
 		if (inputThread.isAlive() && !inputThread.isDead())
 			return; // TODO: Do we want to throw an exception here
 
 		if (nick == null || !nick.matches("[\\w<\\-\\[\\]\\^{}]+"))
 		{
 			throw new IllegalArgumentException("Supplied nick is null or not a valid nickname");
 		}
 
 		if (password != null && password.length() != 0)
 		{
 			String passCommand = IrcCommands.createCommandString(
 				IrcCommands.PASS, password);
 			outputThread.send(passCommand);
 		}
 
 		String commandString;
 		int nicksTried = 0;
 
 		BufferedReader inputFromIrc = inputThread.inputStream;
 
 		// Try and register a nick name
 		commandString = IrcCommands.createCommandString(IrcCommands.NICK, nick);
 		outputThread.send(commandString);
 		String currNick = nick;
 
 		// Send the user data
 		commandString = IrcCommands.createCommandString(IrcCommands.USER,
 			nick, 0, "*", realName == null ? "Mewler-Bot" : realName);
 		outputThread.send(commandString);
 
 		while(true)
 		{
 			String line = inputFromIrc.readLine();
 
 			if (line == null)
 			{
 				dispose();
				throw new IOException("Error whilst trying to connect: null message");
 			}
 
 			AbstractIrcMessage mess = AbstractIrcMessage.parse(line, currNick);
 
 			if (mess instanceof IrcPingMessage)
 			{
 				LOG.log(Level.FINER, ">> {0}", mess.toString());
 				outputThread.send(((IrcPingMessage)mess).reply());
 			}
 			else if (mess instanceof IrcMessage)
 			{
 				final IrcMessage message = (IrcMessage)mess;
 				LOG.log(Level.FINER, ">> {0}", mess.toString());
 				if (message.getMessageType().equals("ERROR"))
 				{
 					dispose();
					throw new IOException("Error whilst trying to connect: " + message.getPayload());
 				}
 			}
 			else if (mess instanceof IrcResponseCode)
 			{
 				final IrcResponseCode message = (IrcResponseCode)mess;
 
 				switch (message.getCode())
 				{
 					case ERR_NICKNAMEINUSE:
 					case ERR_NICKCOLLISION:
 						LOG.log(Level.FINE, ">> Nick in use, trying again");
 						currNick = nick + "-" + nicksTried;
 						commandString = IrcCommands.createCommandString(IrcCommands.NICK, currNick);
 						outputThread.send(commandString);
 						break;
 					case RPL_MOTDSTART:
 					case RPL_MOTD:
 						LOG.log(Level.FINE, "Sucessfully connected!!!");
 						this.nick = currNick;
 						inputThread.start();
 						outputThread.start();
 						return;
 
 					default:
 						LOG.log(Level.FINER, ">> {0}", mess.toString());
 						// TODO: do something with messages of other codes
 				}
 			}
 			else
 			{
 				LOG.log(Level.FINER, ">> {0}", mess.toString());
 			}
 		}
 	}
 
 	public void message(final String target, final String message)
 	{
 		StringBuilder rawMessage = new StringBuilder(100 + message.length());
 
 		for (String line : message.split("[\r\n]+"))
 		{
 			rawMessage.append(IrcCommands.createCommandString(IrcCommands.MESS,
 				target, line));
 		}
 		outputThread.queue(rawMessage.toString());
 	}
 
 	public void act(final String target, final String message)
 	{
 		String mess = IrcCommands.createCommandString(IrcCommands.ACTION,
 			target, message.split("[\r\n]+")[0]);
 
 		outputThread.queue(mess);
 	}
 
 	public void join(String channel)
 	{
 		String command = IrcCommands.createCommandString(IrcCommands.JOIN, channel);
 		outputThread.queue(command);
 	}
 
 	public void part(String channel)
 	{
 		String command = IrcCommands.createCommandString(IrcCommands.PART, channel);
 		outputThread.queue(command);
 	}
 
 	public void quit()
 	{
 		String command = IrcCommands.createCommandString(IrcCommands.QUIT, "Mewler");
 		outputThread.queue(command);
 	}
 
 	@Override
 	protected void finalize() throws Throwable
 	{
 		dispose();
 		super.finalize();
 	}
 
 	public void dispose()
 	{
 		outputThread.interrupt();
 		inputThread.interrupt();
 	}
 
 	public String getNick()
 	{
 		return nick;
 	}
 
 	protected void onMessage(String nick, User sender, String channel, String message)
 	{
 		// Nothing to see here. Move along, citizen!
 	}
 
 	protected void onAction(String nick, User sender, String channel, String action)
 	{
 		// Nothing to see here. Move along, citizen!
 	}
 
 	protected void onDisconnect()
 	{
 		// Nothing to see here. Move along, citizen!
 	}
 
 	protected void onPing(IrcPingMessage ping)
 	{
 		try
 		{
 			outputThread.send(ping.reply());
 		}
 		catch (IOException ex)
 		{
 			LOG.log(Level.SEVERE, "Exception when replying to PING", ex);
 		}
 	}
 
 	protected boolean isAlive()
 	{
 		return (inputThread.isAlive() || outputThread.isAlive());
 	}
 }
