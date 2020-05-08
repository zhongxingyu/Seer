 package org.dyndns.schuschu.xmms2client.watch;
 
 import org.dyndns.schuschu.xmms2client.interfaces.FooInterfaceViewElement;
 import org.dyndns.schuschu.xmms2client.loader.FooLoader;
 
 import se.fnord.xmms2.client.Client;
 import se.fnord.xmms2.client.commands.Command;
 import se.fnord.xmms2.client.commands.Playback;
 
 public class FooWatchCurrentTrack extends Thread {
 
 	private static final boolean DEBUG = FooLoader.DEBUG;
 	private String name = "FooWatchCurrentTrack";
 
 	private void debug(String message) {
 		if (DEBUG) {
 			System.out.println("debug: " + name + " " + message);
 		}
 	}
 
 	private boolean running;
 	private Command c;
 	private Runnable r;
 	private final FooInterfaceViewElement view;
 	private int current;
 
 	public FooWatchCurrentTrack(Client client,
 			final FooInterfaceViewElement view) {
 		debug("fire");
		this.view = view;
		
 		c = Playback.currentIdBroadcast();
 		c.execute(client);
 
 		r = new Runnable() {
 			public void run() {
 				debug("fire");
 				view.getBackend().setCurrent(current);
 			}
 		};
 
 	}
 
 	public void done() {
 		running = false;
 	}
 
 	public void run() {
 
 		running = true;
 		while (running) {
 			try {
 				current = c.waitReply();
 				view.getReal().getDisplay().asyncExec(r);
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 			}
 		}
 	}
 }
