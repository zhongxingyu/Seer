 package com.example.kindle.winningfour;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.event.KeyEvent;
 import java.util.ResourceBundle;
 
 import com.amazon.kindle.kindlet.AbstractKindlet;
 import com.amazon.kindle.kindlet.KindletContext;
 import com.amazon.kindle.kindlet.event.KindleKeyCodes;
 import com.amazon.kindle.kindlet.ui.KindleOrientation;
 import com.amazon.kindle.kindlet.util.Timer;
 import com.example.kindle.sm.KeyboardEvent;
 import com.example.kindle.utils.DialogHelper;
 import com.example.kindle.winningfour.boardgame.Board;
 import com.example.kindle.winningfour.boardgame.GameController;
 import com.example.kindle.winningfour.boardgame.GameView;
 import com.example.kindle.winningfour.gui.PageStateMachine;
 import com.example.kindle.winningfour.options.AppOptions;
 
 import org.apache.log4j.Logger;
 
 /**
  * Program life-cycle class.
  */
 public class App extends AbstractKindlet
 {
     /** {@inheritDoc} */
     public void create(final KindletContext context)
     {
     	App.log("App::create");
 		
 		this.context = context;
 		this.root = this.context.getRootContainer();
 
 		App.log("App::create done");
     }
 
     /** {@inheritDoc} */
     public void destroy()
     {
 		App.log("App::destroy");
 
 		this.root.setLayout(null);  // no need to waste time on implicit root-layout calls down from here 
 		this.root.removeAll();
 
     	KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
         fm.removeKeyEventDispatcher(this.keyEventDispatcher);
 
         App.opts.destroy();
         App.opts = null;
         
         AppResources.destroy();
         bundle = null;
         
         App.pager.destroy();
         App.pager = null;
 
         App.screenSize = null;
         App.clientSize = null;
         
         App.gamer.destroy();
         App.gamer = null;
 
         this.context = null;
         this.root = null;
         
         this.gameView.destroy();
         this.gameView = null;
 
         Board.clearHash();
 
 		App.timer.cancel();
 		App.timer = null;
 
 		System.gc();
 
 		App.log("App::destroy done.\n\nOver and out!");
 	}
 
     /** {@inheritDoc} */
     public void start()
     {
 		App.log("App::start");
 
 		synchronized (this)
 		{
 	    	App.log("App::start synchronized");
 			App.setStopped(false);
 	    	super.start();
 
 			if (!this.initialStartDone)
 			{  
 				App.log("App::start initial");
 				if (!App.isStopped())
 				{
 					Runnable runnable = 
 						new Runnable() 
 						{
 						    public void run() 
 						    {
 						    	App.this.initalStart();
 						    }
 						};
 					App.log("App::start will post runnable");
 					EventQueue.invokeLater(runnable);
 				}
 			}
 			else
 			{
 				App.log("App::start resume after pause");
 				// usually nothing much to do here, unless you did something 
 				// in stop() that needs to be rebuilt.
 			}
 			
 			/* TODO: build timers and possibly background threads (if any) ... */
 		}
 
 		App.log("App::start done");
     }
 
     /** 
      * Method that is called only for the first time the application starts.
      */
     protected void initalStart()
     {
 		App.log("App::initalStart");
 
 		if (App.isStopped())
 		{
 			App.log("App::initalStart early exit (stopped)");
 			return; 
 		}
 
 		// Setting up globals
 		App.screenSize = this.root.getSize();
 		App.clientSize = new Dimension(App.screenSize.width - 20, App.screenSize.height - 20);
 
		// Options
		this.context.getOrientationController().lockOrientation(KindleOrientation.PORTRAIT);
		
 		// Global Events
 		this.keyEventDispatcher = new KeyEventDispatcher()
 		{
             public boolean dispatchKeyEvent(final KeyEvent key)
             {
             	boolean consumed = false;
             	int code = key.getKeyCode();
                 
             	if (code == KindleKeyCodes.VK_BACK)
                 {
                 	consumed = true;
                 	App.pager.pushEvent(new KeyboardEvent(code));
                 }
 
                 if (consumed)
                 {
                 	App.log("Key pressed: " + code);
                     key.consume();
                     return true;
                 }
 
                 return false;
             }
 		};
 
 		KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
         fm.addKeyEventDispatcher(this.keyEventDispatcher);
 
 		// Options
 		App.opts = new AppOptions();
 
 		// Zobrist hash keys for board
 		Board.initHash();
 
 		// App start code
 		this.gameView = new GameView();
 		App.gamer = new GameController(this.gameView);
 		App.pager = new PageStateMachine(this.context);
 		App.pager.start();
 		
 		if (App.gamer.isSuspended())
 		{
 			App.gamer.restore();
 		}
 
 		this.initialStartDone = true;
 	
 		App.log("App::initalStart done");
 	}
 
     /** {@inheritDoc} */
 	public void stop()
     {
 		App.log("App::stop");
 		App.setStopped(true);
 		
 		synchronized (this)
 		{
 			App.opts.save();
 			App.log("App::stop synchronized");
 			// TODO: check is we need to cancel game timer
 		}
 		
 		App.log("App::stop done");
 	}
 
     /**
      * Sets the application stopped/running status.
      * 
      * @param flag Stopped flag.
      */
 	public static void setStopped(boolean flag)
 	{
 		App.stopped = flag;
 	}
 
     /**
      * Returns if the application has been stopped.
      * 
      * @return True if stopped, false otherwise.
      */
 	public static boolean isStopped()
 	{
 		return App.stopped;
 	}
 
     /**
      * Internal error handler. Will try to stop
      * the current game and exit to main menu.
      * 
      * @param msg Error message.
      */
 	public static void error(final String msg)
 	{
 		DialogHelper.error("Internal error: " + msg, new Runnable()
 		{
 			public void run()
 			{
 				App.gamer.stop();
 				App.pager.home();
 			}
 		});
 	}
 	
 	/**
 	 * Returns new Timer instance. Only one timer is present at any time.
 	 * After the first call any subsequent invocations cancel and then
 	 * destroy current timer and create a new one.
 	 * 
 	 * @return Freshly created timer.
 	 */
 	public static Timer getTimer()
 	{
 		if (App.timer != null)
 		{
 			App.timer.cancel();
 		}
 		
 		App.timer = new Timer();
 
 		return App.timer;
 	}
 
 	public static String getHomeDir()
 	{
 		return System.getProperty("kindlet.home");
 	}
 
 	public static String getHomeFilePath(final String filename)
 	{
 		return System.getProperty("kindlet.home") + "/" + filename;
 	}
 
     /** Indicates if the application has been stopped. */
     private static boolean stopped = false;
 
     /** Resource manager for centralized management and access of strings, fonts and images. */
     public static ResourceBundle bundle = ResourceBundle.getBundle("com.example.kindle.winningfour.AppResources");
     
     /** Page switching state machine.  */
     public static PageStateMachine pager;
     
     /** Current screen dimensions. */
     public static Dimension screenSize;
     
     /** Screen client area dimensions. */
     public static Dimension clientSize;
     
     /** Game state machine. */
     public static GameController gamer;
     
     /** Game options handler */
     public static AppOptions opts;
 
     /** GUI panel with game elements like board, pieces, selector, etc. */
     private GameView gameView;
 
     /** Application context. */
     private KindletContext context;
     
     /** Application root container. */
     private Container root;
     
     /** Indicates that first start routines have finished. */
     private boolean initialStartDone;
     
     /** Application start time used for logger. */
     private static long startTime = System.currentTimeMillis();
 	
     /** Application logger. */
     private static Logger logger = Logger.getLogger("App");
 	
     /** Global keys dispatcher. Used for VK_BACK handling. */
 	private KeyEventDispatcher keyEventDispatcher;
 
     /** Game clock timer. */
 	private static Timer timer;
 	
     /** Puts log message into logging stream. */
 	public static void log(String msg)
 	{
 		double timestamp = (System.currentTimeMillis() - App.startTime)/1000.0;
 		logger.info("[" + timestamp + " - " + Thread.currentThread().getName() + "]   " + msg);
 	}
 }
