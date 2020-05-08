 package cc.warlock.core.script.internal;
 
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IStreamListener;
 import cc.warlock.core.client.IStyledString;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.internal.Command;
 import cc.warlock.core.script.IScript;
 import cc.warlock.core.script.IScriptCommands;
 import cc.warlock.core.script.Match;
 
 public class ScriptCommands implements IScriptCommands, IStreamListener
 {
 
 	protected IWarlockClient client;
 	protected Match waitForMatch;
 	protected final Lock lock = new ReentrantLock();
 	
 	protected final Condition gotTextCond = lock.newCondition();
 	protected String text;
 	protected int textWaiters = 0;
 	
 	protected final Condition nextRoom = lock.newCondition();
 	protected boolean roomWaiting = false;
 	
 	protected final Condition gotPromptCond = lock.newCondition();
 	protected int promptWaiters = 0;
 	protected boolean gotPrompt = false;
 	
 	protected boolean stopped = false;
 	
 	protected Thread pausedThread;
 	
 	public ScriptCommands (IWarlockClient client)
 	{
 		this.client = client;
 		
 		client.getDefaultStream().addStreamListener(this);
 	}
 	
 	public void echo (String text) {
 		client.getDefaultStream().echo(text);
 	}
 	
 	public void echo (IScript script, String text) {
 		client.getDefaultStream().echo("[" + script.getName() + "]: " + text);
 	}
 	
 	protected void assertPrompt() {
 		while (!client.getDefaultStream().isPrompting())
 		{
 			try {
 				Thread.sleep(200);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public Match matchWait (List<Match> matches) {
 		lock.lock();
 		try {
 			// clear out the previous line sent
 			text = null;
 			// let everyone know we're listening
 			textWaiters++;
 			
 			// run until we get a match or are told to stop
 			matchWaitLoop: while(true) {
 				System.out.println("Waiting for text");
 				// wait for some text
 				while(text == null) {
 					gotTextCond.await();
 					if(stopped)
 						break matchWaitLoop;
 				}
 				System.out.println("Got text: " + text);
 				String[] lines = text.split("\\n");
 				for(String line : lines) {
 					// try all of our matches
 					for(Match match : matches) {
 						// System.out.println("Trying a match");
 						if(match.matches(line)) {
 							// System.out.println("matched a line");
 							return match;
 						}
 					}
 				}
 				// FIXME this won't work if we have multiple text waiters.
 				// clear the line in preparation for the next one
 				text = null;
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			System.out.println("Done with matchwait");
 			// tell everyone we aren't listening
 			textWaiters--;
 			// if we were the only listener, set the line to null
 			if(textWaiters == 0) {
 				text = null;
 			}
 			lock.unlock();
 		}
 		return null;
 	}
 
 	public void move (String direction) {
 		client.send(direction);
 		client.getDefaultStream().echo(direction);
 		nextRoom();
 	}
 
 	public void nextRoom () {
 		lock.lock();
 		try {
			roomWaiting = true;
 			while (!stopped && roomWaiting) {
 				nextRoom.await();
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			roomWaiting = false;
 			lock.unlock();
 		}
 	}
 
 	public void pause (int seconds) {
 		try {
 			// FIXME need to make this work for multiple users
 			pausedThread = Thread.currentThread();
 			Thread.sleep(seconds * 1000);
 		} catch(InterruptedException e) {
 			e.printStackTrace();
 		} finally {
 			pausedThread = null;
 		}
 	}
 
 	public void put (String text) {
 		// false command so it doesn't get added to the command history
 		Command command = new Command(text, new Date());
 		command.setInHistory(true);
 		
 		client.send(command);
 		client.getDefaultStream().echo(text);
 	}
 	
 	public void put (IScript script, String text) {
 		assertPrompt();
 		
 		// false command so it doesn't get added to the command history
 		Command command = new Command(text, new Date());
 		command.setInHistory(true);
 		
 		client.send(command);
 		client.getDefaultStream().echo("[" + script.getName() + "]: " + text);
 	}
 
 	public void waitFor (Match match) {
 		waitForMatch = match;
 		lock.lock();
 		try {
 			textWaiters++;
 			while(true) {
 				while(text == null)
 					gotTextCond.await();
 				if(waitForMatch.matches(text)) {
 					break;
 				}
 				text = null;
 			}
 			// FIXME need to make this work for multiple users
 			text = null;
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			textWaiters--;
 			if(textWaiters == 0) {
 				text = null;
 			}
 			lock.unlock();
 		}
 	}
 
 	public void waitForPrompt () {
 		lock.lock();
 		try {
 			promptWaiters++;
 			while(!stopped && !gotPrompt) {
 				gotPromptCond.await();
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			promptWaiters--;
 			if(promptWaiters == 0) {
 				gotPrompt = false;
 			}
 			lock.unlock();
 		}
 	}
 	
 	public IWarlockClient getClient() {
 		return this.client;
 	}
 		
 	public void streamCleared(IStream stream) {}
 	public void streamEchoed(IStream stream, String text) {}
 	
 	public void streamPrompted(IStream stream, String prompt) {
 		if(promptWaiters > 0) {
 			lock.lock();
 			try {
 				gotPrompt = true;
 				gotPromptCond.signalAll();
 			} catch(Exception e) {
 				e.printStackTrace();
 			} finally {
 				lock.unlock();
 			}
 		}
 	}
 	
 	public void streamDonePrompting (IStream stream) { }
 	
 	public void streamReceivedText(IStream stream, IStyledString string) {
 		lock.lock();
 		try {
 			text = string.getBuffer().toString();
 			if(textWaiters > 0) {
 				System.out.println("Signaling waiters");
 				gotTextCond.signalAll();
 			}
 		} finally {
 			lock.unlock();
 		}
 	}
 	
 	public void movedToRoom() {
 		lock.lock();
 		try {
			roomWaiting = false;
 			nextRoom.notifyAll();
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	
 	public void stop() {
 		lock.lock();
 		stopped = true;
 		try {
 			gotTextCond.signalAll();
 			gotPromptCond.signalAll();
 			nextRoom.signalAll();
 			if(pausedThread != null)
 				pausedThread.interrupt();
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			lock.unlock();
 		}
 	}
 }
