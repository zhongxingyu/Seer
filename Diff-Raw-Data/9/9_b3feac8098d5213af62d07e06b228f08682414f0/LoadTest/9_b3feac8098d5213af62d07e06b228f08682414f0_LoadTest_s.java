 /*
 Copyright 2009 Samuel Marshall
 http://www.leafdigital.com/software/hawthorn/
 
 This file is part of Hawthorn.
 
 Hawthorn is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Hawthorn is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Hawthorn.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.leafdigital.hawthorn.loadtest;
 
 import java.io.*;
 import java.net.*;
 import java.security.NoSuchAlgorithmException;
 import java.text.*;
 import java.util.*;
 import java.util.regex.*;
 
 import com.leafdigital.hawthorn.util.Auth;
 import com.leafdigital.hawthorn.util.Auth.Permission;
 
 /**
  * Generates simulated load on a Hawthorn server. Do not use on a real server
  * while it is live to users.
  * <p>
  * To test load capacity, run this script with different values, monitoring:
  * <ul>
  * <li>Statistics on the server.</li>
  * <li>Performance reported by this script.</li>
  * </ul>
  * <p>
  * Command line arguments are all of the form name=value (no spaces).
  * <h3>Required parameters</h3>
  * <dl>
  * <dt>host</dt>
  * <dd>Server's host address</dd>
  * <dt>magicnumber</dt>
  * <dd>Server's magic number</dd>
  * <dt>drivebys</dt>
  * <dd>Number of users per minute who view pages that require a Hawthorn
  *   getRecent call</dd>
  * <dt>users</dt>
  * <dd>Number of simulated active Hawthorn chat users who simultaneously
  *   have a Hawthorn chat popup open</dd>
  * </dl>
  * <h3>Optional parameters</h3>
  * <p>When these parameters set averages for random numbers, the actual value
  * usually varies from 0 to 2 * average.</p>
  * <dl>
  * <dt>port</dt>
  * <dd>Server's port number. Default: 13370.</dd>
  * <dt>minutes</dt>
  * <dd>The test runs for this many minutes before ending and displaying
  *   summary results. (Temporary information displayed during the test is
  *   sent to System.err; the final results are sent to System.out, so you
  *   can redirect them separately.) Default: 5.</dd>
  * <dt>siteusers</dt>
  * <dd>Size of pool that the driveby and active users are picked from. Default:
  *   max ( users * 10, drivebys * 10)</dd>
  * <dt>channels</dt>
  * <dd>Number of chat channels in use. Default: users / 10 (min 1).</dd>
  * <dt>sessionminutes</dt>
  * <dd>Average length of a chat session before the window is closed. Default: 10.</dd>
  * <dt>sayseconds</dt>
  * <dd>Average number of seconds between each user saying something. Default: 60.</dd>
  * <dt>messagelength</dt>
  * <dd>Average length in characters of a message. Default: 40.</dd>
  * <dt>leavechance</dt>
  * <dd>Percentage chance that a 'leave' command is sent when session ends (this
  *   only happens if the user closes the window via the link and not the X
  *   button). Default: 30</dd>
  * <dt>threads</dt>
  * <dd>Number of work threads used by the load-tester. Default:
  *   max(drivebys/250, users/100) + 1.</dd>
  * </dl>
  * If you want to watch the simulation in action, connect to the Hawthorn server
  * in channel "loadtestchan0" (or another such number).
  */
 public class LoadTest
 {
 	private final static int REQUIRED = -1;
 
	private final static long WARMUP_TIME = 5000;
 
 	private String magicNumber, host;
 	private int drivebys, users, minutes, siteUsers, channels, sessionMinutes,
 	  saySeconds, threads, port, leaveChance, messageLength;
 
 	private long keyTime, endWarmupTime;
 
 	private int countEvents, countExceptions, countErrors;
 	private long eventTime;
 	private Object countSynch = new Object();
 
 	private SiteUser[] userPool;
 	private String[] channelPool;
 	private ActiveUser[] activeUsers;
 
 	private char[] threadStatus;
 
 	/**
 	 * Note: It appears that Random is thread-safe. There was some discussion
 	 * about possible contention-related performance issues, but even with
 	 * 100 threads each generating 500,000 nextInt calls nonstop, moving to
 	 * per-thread Random instances only saved about a third of the time. There
 	 * should be much less contention when threads are mainly doing other things
 	 * than getting random numbers...
 	 */
 	private Random random = new Random();
 
 	private TreeSet<UserEvent> userQueue = new TreeSet<UserEvent>();
 
 	/** Represents a single user in the system */
 	class SiteUser
 	{
 		private String name, parameters;
 
 		/**
 		 * @param name Username (also used as displayname).
 		 */
 		private SiteUser(String name)
 		{
 			this.name = name;
 			parameters = "&user=" + name + "&displayname=" +name
 			  + "&keytime=" + keyTime + "&extra=&permissions=rw";
 		}
 
 		/**
 		 * @param channel Channel name
 		 * @return URL parameters that describe and authenticate this user on
 		 *   the given channel, beginning with &
 		 */
 		String getParameters(String channel)
 		{
 			try
 			{
 				return parameters + "&channel=" + channel + "&key=" +
 					Auth.getKey(magicNumber, name, name, "",
 						EnumSet.of(Permission.READ, Permission.WRITE), channel, keyTime);
 			}
 			catch(NoSuchAlgorithmException e)
 			{
 				throw new Error(e);
 			}
 		}
 	}
 
 	/** A user event. Queues required user actions. */
 	static class UserEvent implements Comparable<UserEvent>
 	{
 		private EventSource u;
 		private long time;
 
 		public UserEvent(EventSource u, long time)
 		{
 			this.u = u;
 			this.time = time;
 		}
 
 		public int compareTo(UserEvent o)
 		{
 			// Doesn't return 0 unless they are identical
 			return o==this ? 0 : time < o.time ? -1 : 1;
 		}
 
 		@Override
 		public boolean equals(Object obj)
 		{
 			return obj == this;
 		}
 	}
 
 	/**
 	 * Adds this user/source to the queue of events waiting for action.
 	 * @param u Event source
 	 * @param time Time they want to be active
 	 */
 	public void queueUser(EventSource u, long time)
 	{
 		UserEvent event = new UserEvent(u, time);
 		synchronized(userQueue)
 		{
 			userQueue.add(event);
 			userQueue.notifyAll();
 		}
 	}
 
 	/**
 	 * Obtains the first user due processing from the user-event queue.
 	 * If there is no due event, blocks until one is added.
 	 * @return User to process - or null if the queue end has been reached
 	 *   and testing is over
 	 */
 	public EventSource getNextEvent()
 	{
 		EventSource u;
 		synchronized(userQueue)
 		{
 			while(true)
 			{
 				// Get first event from queue
 				Iterator<UserEvent> i = userQueue.iterator();
 				UserEvent result = i.next();
 
 				// Is it due to happen?
 				long now = System.currentTimeMillis();
 				if(result.time <= now)
 				{
 					// End of queue is a special event, leave it in there for other threads
 					if(result.u == null)
 					{
 						return null;
 					}
 
 					// Remove event and return user
 					i.remove();
 					u = result.u;
 					break;
 				}
 
 				// Not due to happen yet. Wait until it is
 				try
 				{
 					userQueue.wait(result.time - now);
 				}
 				catch(InterruptedException e)
 				{
 				}
 			}
 		}
 
 		synchronized(countSynch)
 		{
 			countEvents++;
 		}
 		return u;
 	}
 
 	/**
 	 * @return One of the site users, chosen randomly
 	 */
 	public SiteUser pickSiteUser()
 	{
 		return userPool[random.nextInt(userPool.length)];
 	}
 
 	/**
 	 * @return One of the channels, chosen randomly
 	 */
 	public String pickChannel()
 	{
 		return channelPool[random.nextInt(channelPool.length)];
 	}
 
 	/**
 	 * @return Session length in milliseconds, chosen randomly
 	 */
 	public long pickSessionLength()
 	{
 		return (long)(random.nextDouble() * 2 * sessionMinutes * 60000);
 	}
 
 	/**
 	 * @return Delay before saying something in milliseconds, chosen randomly
 	 */
 	public long pickSayPause()
 	{
 		return (long)(random.nextDouble() * 2 * saySeconds * 1000);
 	}
 
 	/**
 	 * @return True if this user bothers to send a leave command before exiting
 	 *   the channel
 	 */
 	public boolean pickSendLeave()
 	{
 		return random.nextInt(100) < leaveChance;
 	}
 
 	/**
 	 * @return Delay before next drive-by event, in microseconds (not ms)
 	 */
 	public int pickDriveByDelay()
 	{
 		return (int)(random.nextDouble() * 2 * 60000000.0 / drivebys);
 	}
 
 	/**
 	 * @return A random message to 'say'; the message will not contain any
 	 *   characters that need to be escaped, etc
 	 */
 	public String pickMessage()
 	{
 		// Make totally random characters to include at start of message
 		// (just in case there are any caching effects)
 		StringBuilder out = new StringBuilder(
 			Integer.toHexString(random.nextInt()));
 
 		// Pick a message length and make up the rest by adding X
 		int characters = random.nextInt(messageLength) * 2;
 		for(int done = out.length(); done < characters; done++)
 		{
 			out.append('X');
 		}
 
 		return out.toString();
 	}
 
 	private void test()
 	{
 		// Display initial stuff
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd,kk:mm");
 		System.out.println("Hawthorn load test,"+sdf.format(new Date()));
 		System.out.println("Host," + host);
 		System.out.println("Port," + port);
 		System.out.println("Test minutes," + minutes);
 		System.out.println();
 
 		// Important parameters
 		System.out.println("Active users," + users);
 		System.out.println("Drivebys per minute," + drivebys);
 		System.out.println();
 
 		// Other parameters
 		System.out.println("User pool," + siteUsers);
 		System.out.println("Channel pool," + channels);
 		System.out.println("User sessions (minutes)," + sessionMinutes);
 		System.out.println("Say delay (seconds)," + saySeconds);
 		System.out.println("Message length (characters)," + messageLength);
		System.out.println("Chance of clean leave (%)," + saySeconds);
 
 		System.out.println();
 
 		System.out.println("Threads," + threads);
 
 		// Initialise key time
 		long now = System.currentTimeMillis();
 		keyTime = now + (minutes+1) * 60000L;
 
 		// Initialise user pool
 		userPool = new SiteUser[siteUsers];
 		for(int i=0; i<siteUsers; i++)
 		{
 			userPool[i] = new SiteUser("loadtest"+i);
 		}
 
 		// Initialise channels
 		channelPool = new String[channels];
 		for(int i=0; i<channels; i++)
 		{
 			channelPool[i] = "loadtestchan" + i;
 		}
 
 		// Initialise active users
 		activeUsers = new ActiveUser[users];
 		for(int i=0; i<users; i++)
 		{
 			activeUsers[i] = new ActiveUser(this);
 		}
 
 		// Initialise driveby source
 		new DriveBySource(this);
 
 		// Initialise event queue
 		endWarmupTime = now + WARMUP_TIME;
 		long testEndTime = minutes * 60000L + endWarmupTime;
 		userQueue.add(new UserEvent(null, testEndTime));
 
 		// Start threads
 		threadStatus = new char[threads];
 		for(int i=0; i<threads; i++)
 		{
 			threadStatus[i] = '.';
 			new TestThread(this, i);
 		}
 
 		// Wait for warmup period
		System.err.println("Beginning test (warmup)...");
 		try
 		{
 			Thread.sleep(endWarmupTime - System.currentTimeMillis());
 		}
 		catch(InterruptedException e)
 		{
 		}
 
 		// Reset numbers after warmup
 		System.err.println("Tracking results...");
 		synchronized(countSynch)
 		{
 			countEvents = 0;
 			countErrors = 0;
 			countExceptions = 0;
 		}
 		long start = System.currentTimeMillis();
 
 		NumberFormat nf = NumberFormat.getInstance();
 		nf.setMaximumFractionDigits(2);
 		nf.setMinimumFractionDigits(2);
 		nf.setMinimumIntegerDigits(2);
 
 		// Now display information about test every 10 seconds
 		while(true)
 		{
 			now = System.currentTimeMillis();
 			String time = pad(4, "" + ((now - start) / 1000));
 
 			boolean timeToClose = false;
 			int queueDelay = 0;
 			synchronized(userQueue)
 			{
 				if(userQueue.iterator().next().u == null)
 				{
 					timeToClose = true;
 				}
 
 				for(UserEvent e : userQueue)
 				{
 					if(e.time > now || e.u==null)
 					{
 						break;
 					}
 					queueDelay++;
 				}
 			}
 
 			double meanTime = countEvents == 0 ? 0 : (double)eventTime / (double)countEvents;
 			System.err.print("@" + time + "s: Events " + pad(7, ""+countEvents) +
 				"; Errors " + pad(7, ""+countErrors) + "; Exceptions "+
 				pad(7, ""+countExceptions) + "; Queue " + pad(7, ""+queueDelay) +
 				"; Event mean " + nf.format(meanTime) + "ms; Threads: ");
 			for(int i=0; i<threads; i++)
 			{
 				System.err.print(threadStatus[i]);
 			}
 			System.err.println();
 
 			if(timeToClose)
 			{
 				break;
 			}
 
 			try
 			{
 				Thread.sleep(5000);
 			}
 			catch(InterruptedException e)
 			{
 			}
 		}
 
 		double meanTime = countEvents == 0 ? 0 : (double)eventTime / (double)countEvents;
 		System.out.println();
 		System.out.println("Events,Errors,Exceptions,Event mean");
 		System.out.println(countEvents + "," + countErrors + "," + countExceptions
 			 + "," + nf.format(meanTime));
 	}
 
 	private static String pad(int length, String string)
 	{
 		while(string.length() < length)
 		{
 			string = " " + string;
 		}
 		return string;
 	}
 
 	/**
 	 * Initialises the load test.
 	 * @param args Command-line arguments
 	 */
 	public static void main(String[] args)
 	{
 		LoadTest t = new LoadTest();
 
 		try
 		{
 			// Required parameters
 			t.host = getStringParameter(args, "host");
 			t.magicNumber = getStringParameter(args, "magicnumber");
 			t.drivebys = getIntParameter(args, "drivebys", REQUIRED, 0);
 			t.users = getIntParameter(args, "users", REQUIRED, 0);
 			if(t.drivebys == 0 && t.users == 0)
 			{
 				throw new IllegalArgumentException(
 					"You must have at least 1 active user or driveby user");
 			}
 
 			// Optional parameters
 			t.port = getIntParameter(args, "port", 13370, 1);
 			t.minutes = getIntParameter(args, "minutes", 5, 1);
 			t.siteUsers = getIntParameter(args, "siteusers",
 				Math.max(t.users*10, t.drivebys * 10), Math.max(t.users, 1));
 			t.channels = getIntParameter(args, "channels", Math.max(t.users/10, 1), 1);
 			t.sessionMinutes = getIntParameter(args, "sessionminutes", 10, 1);
 			t.saySeconds = getIntParameter(args, "sayseconds", 60, 1);
 			t.leaveChance = getIntParameter(args, "leavechance", 30, 0);
 			t.threads = getIntParameter(args, "threads", 1+Math.max(t.users/100, t.drivebys/250), 1);
 			t.messageLength = getIntParameter(args, "messagelength", 40, 10);
 		}
 		catch(IllegalArgumentException e)
 		{
 			System.err.println(e.getMessage());
 			return;
 		}
 
 		// Let's go!
 		t.test();
 	}
 
 	/**
 	 * Obtains an integer parameter from the command-line.
 	 * @param args Command-line arguments
 	 * @param name Parameter name
 	 * @param def Default value or REQUIRED if none
 	 * @param min Minimum allowed value
 	 * @return Value
 	 * @throws IllegalArgumentException If the parameter is not supplied/invalid
 	 */
 	private static int getIntParameter(String[] args, String name, int def, int min)
 	  throws IllegalArgumentException
 	{
 		for(String arg : args)
 		{
 			if(arg.startsWith(name + "="))
 			{
 				String value = arg.substring(name.length() + 1);
 				try
 				{
 					int result = Integer.parseInt(value);
 					if(result < min)
 					{
 						throw new IllegalArgumentException("Parameter " + name +
 							" must be at least " + min);
 					}
 					return result;
 				}
 				catch(NumberFormatException e)
 				{
 					throw new IllegalArgumentException("Invalid option for parameter " +
 						name + ": " + value);
 				}
 			}
 		}
 		if(def != REQUIRED)
 		{
 			return def;
 		}
 		throw new IllegalArgumentException("Missing required parameter: "+name);
 	}
 
 	/**
 	 * Obtains a string parameter from the command-line.
 	 * @param args Command-line arguments
 	 * @param name Parameter name
 	 * @return Value
 	 * @throws IllegalArgumentException If the parameter is not supplied
 	 */
 	private static String getStringParameter(String[] args, String name)
 	{
 		for(String arg : args)
 		{
 			if(arg.startsWith(name+"="))
 			{
 				return arg.substring(name.length()+1);
 			}
 		}
 		throw new IllegalArgumentException("Missing required parameter: "+name);
 	}
 
 	static class TimeResult
 	{
 		private long timeStamp, nextRequest;
 
 		public TimeResult(long timeStamp, long nextRequest)
 		{
 			this.timeStamp = timeStamp;
 			this.nextRequest = nextRequest;
 		}
 
 		/** @return Timestamp that needs to be reported to the server next time */
 		public long getTimeStamp()
 		{
 			return timeStamp;
 		}
 
 		/** @return Time that next request should be made */
 		public long getNextRequest()
 		{
 			return nextRequest;
 		}
 	}
 
 	private HashSet<String> shownExceptions = new HashSet<String>();
 
 	/**
 	 * Makes a request of the Hawthorn server.
 	 * @param command Server command path (beginning with /hawthorn)
 	 * @param parameters Extra parameters to stick on the end
 	 * @param thread Index of thread
 	 * @return Result as a string, or null in the event of any error
 	 */
 	private String getResult(String command, String parameters, int thread)
 	{
 		URL u=null;
 		try
 		{
 			long before = System.currentTimeMillis();
 			u = new URL("http://" + host + ":" + port +	command + parameters);
 			threadStatus[thread] = 'C';
 			HttpURLConnection connection = (HttpURLConnection)u.openConnection();
 			InputStream stream = connection.getInputStream();
 			threadStatus[thread] = 'R';
 
 			byte[] buffer = new byte[4096];
 			ByteArrayOutputStream extraBuffer = null;
 			int pos = 0;
 			while(true)
 			{
 				int read = stream.read(buffer, pos, buffer.length - pos);
 				if(read == -1)
 				{
 					break;
 				}
 				pos += read;
 
 				if(pos == buffer.length)
 				{
 					if(extraBuffer == null)
 					{
 						extraBuffer = new ByteArrayOutputStream();
 					}
 					extraBuffer.write(buffer);
 					pos=0;
 				}
 			}
 			stream.close();
 			connection.disconnect();
 			threadStatus[thread] = '.';
 
 			if(extraBuffer != null)
 			{
 				extraBuffer.write(buffer, 0, pos);
 				buffer = extraBuffer.toByteArray();
 				pos = buffer.length;
 			}
 
 			long time = System.currentTimeMillis() - before;
 
 			synchronized(countSynch)
 			{
 				eventTime += time;
 			}
 
 			return new String(buffer, 0, pos, "UTF-8");
 		}
 		catch(IOException e)
 		{
 			String exception = e.toString();
 			synchronized(shownExceptions)
 			{
 				if(shownExceptions.add(exception))
 				{
 					System.err.print("Exception '" + e.toString() + "' [Further instances not shown]");
 				}
 			}
 			synchronized(countSynch)
 			{
 				countExceptions++;
 			}
 			return null;
 		}
 	}
 
 	private static TimeResult getFailedResult()
 	{
 		long now = System.currentTimeMillis();
 		return new TimeResult(now, now+2000);
 	}
 
 	private final static Pattern RECENT_COMPLETE = Pattern.compile(
 		"^hawthorn\\.recentComplete.*,([0-9]+)\\);$");
 	private final static Pattern POLL_COMPLETE = Pattern.compile(
 		"^hawthorn\\.pollComplete.*,([0-9]+),([0-9]+)\\);$");
 	private final static Pattern SAY_COMPLETE = Pattern.compile(
 		"^hawthorn\\.sayComplete.*");
 	private final static Pattern LEAVE_COMPLETE = Pattern.compile(
 		"^hawthorn\\.leaveComplete.*");
 
 	/**
 	 * Executes the server 'request' command that is immediately followed
 	 * by a poll request.
 	 * @param parameters Channel, user, and authentication URL params
 	 * @param thread Index of thread
 	 * @return Result of command
 	 */
 	public TimeResult doRecent(String parameters, int thread)
 	{
 		String result = getResult(
 			"/hawthorn/recent?maxage=600000&maxnumber=10&id=1",	parameters, thread);
 		if(result == null)
 		{
 			// Handle failure gracefully so user can keep on keeping on
 			return getFailedResult();
 		}
 
 		Matcher m = RECENT_COMPLETE.matcher(result);
 		if(!m.matches())
 		{
 			synchronized(countSynch)
 			{
 				countErrors++;
 			}
 			return getFailedResult();
 		}
 
 		long
 		  now = System.currentTimeMillis(),
 			timestamp = Long.parseLong(m.group(1));
 		return new TimeResult(timestamp, now);
 	}
 
 	/**
 	 * Executes the server 'poll' command.
 	 * @param parameters Channel, user, and authentication URL params
 	 * @param lastTimeStamp Timestamp of last response from server
 	 * @param thread Index of thread
 	 * @return Result of command
 	 */
 	public TimeResult doPoll(String parameters, long lastTimeStamp, int thread)
 	{
 		String result = getResult(
 			"/hawthorn/poll?lasttime=" + lastTimeStamp + "&id=1", parameters, thread);
 		if(result == null)
 		{
 			// Handle failure gracefully so user can keep on keeping on
 			return getFailedResult();
 		}
 
 		Matcher m = POLL_COMPLETE.matcher(result);
 		if(!m.matches())
 		{
 			synchronized(countSynch)
 			{
 				countErrors++;
 			}
 			return getFailedResult();
 		}
 
 		long
 		  now = System.currentTimeMillis(),
 			timestamp = Long.parseLong(m.group(1)),
 			delay = Long.parseLong(m.group(2));
 
 		return new TimeResult(timestamp, now + delay);
 	}
 
 	/**
 	 * Executes the server 'say' command.
 	 * @param parameters Channel, user, and authentication URL params
 	 * @param thread Index of thread
 	 * @param unique Unique identifier (for this user)
 	 */
 	public void doSay(String parameters, int thread, int unique)
 	{
 		String result = getResult("/hawthorn/say?message=" + pickMessage()
 			+ "&id=1&unique="+unique, parameters, thread);
 		if(result == null)
 		{
 			// Handle failure gracefully so user can keep on keeping on
 			return;
 		}
 
 		if(!SAY_COMPLETE.matcher(result).matches())
 		{
 			synchronized(countSynch)
 			{
 				countErrors++;
 			}
 		}
 	}
 
 	/**
 	 * Executes the server 'leave' command.
 	 * @param parameters Channel, user, and authentication URL params
 	 * @param thread Index of thread
 	 */
 	public void doLeave(String parameters, int thread)
 	{
 		String result = getResult(
 			"/hawthorn/leave?id=1", parameters, thread);
 		if(result == null)
 		{
 			// Handle failure gracefully so user can keep on keeping on
 			return;
 		}
 
 		if(!LEAVE_COMPLETE.matcher(result).matches())
 		{
 			synchronized(countSynch)
 			{
 				countErrors++;
 			}
 		}
 	}
 
 }
