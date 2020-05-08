 package com.evervoid.client;
 
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.evervoid.client.graphics.FrameUpdate;
 import com.evervoid.client.graphics.geometry.AnimatedAlpha;
 import com.evervoid.client.interfaces.EVFrameObserver;
 import com.evervoid.client.interfaces.EVGlobalMessageListener;
 import com.evervoid.client.views.EverView;
 import com.evervoid.client.views.LoadingView;
 import com.evervoid.client.views.game.GameView;
 import com.evervoid.client.views.home.MainMenuView;
 import com.evervoid.client.views.lobby.LobbyView;
 import com.evervoid.client.views.preferences.PreferencesView;
 import com.evervoid.client.views.serverlist.ServerListView;
 import com.evervoid.json.Json;
 import com.evervoid.state.Color;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.player.Player;
 import com.jme3.math.Vector2f;
 
 /**
  * Only handles switch between Game view, Main menu view, etc. Does not handle switching between subviews of the Game view.
  */
 public class EVViewManager implements EVGlobalMessageListener, EVFrameObserver
 {
 	public enum ViewType
 	{
 		GAME, LOADING, LOBBY, MAINMENU, PREFERENCES, SERVERLIST
 	}
 
 	private static EVViewManager sInstance;
 
 	public static void deregisterView(final ViewType type, final Runnable callback)
 	{
 		getInstance().deregister(type, callback);
 	}
 
 	protected static EVViewManager getInstance()
 	{
 		if (sInstance == null) {
 			sInstance = new EVViewManager();
 		}
 		return sInstance;
 	}
 
 	public static boolean onKeyPress(final KeyboardKey key, final float tpf)
 	{
 		return getInstance().aActiveView.onKeyPress(key, tpf);
 	}
 
 	public static boolean onKeyRelease(final KeyboardKey key, final float tpf)
 	{
 		return getInstance().aActiveView.onKeyRelease(key, tpf);
 	}
 
 	public static boolean onLeftClick(final Vector2f position, final float tpf)
 	{
 		return getInstance().aActiveView.onLeftClick(position, tpf);
 	}
 
 	public static boolean onLeftRelease(final Vector2f position, final float tpf)
 	{
 		return getInstance().aActiveView.onLeftRelease(position, tpf);
 	}
 
 	public static boolean onMouseMove(final Vector2f position, final float tpf)
 	{
 		return getInstance().aActiveView.onMouseMove(position, tpf);
 	}
 
 	public static boolean onMouseWheelDown(final float delta, final float tpf, final Vector2f position)
 	{
 		return getInstance().aActiveView.onMouseWheelDown(delta, tpf, position);
 	}
 
 	public static boolean onMouseWheelUp(final float delta, final float tpf, final Vector2f position)
 	{
 		return getInstance().aActiveView.onMouseWheelUp(delta, tpf, position);
 	}
 
 	public static boolean onRightClick(final Vector2f position, final float tpf)
 	{
 		return getInstance().aActiveView.onRightClick(position, tpf);
 	}
 
 	public static boolean onRightRelease(final Vector2f position, final float tpf)
 	{
 		return getInstance().aActiveView.onRightRelease(position, tpf);
 	}
 
 	public static void registerView(final ViewType type, final EverView view)
 	{
 		getInstance().register(type, view);
 	}
 
 	/**
 	 * Schedule a UI job to be executed on the next frame update
 	 * 
 	 * @param job
 	 */
 	public static void schedule(final Runnable job)
 	{
 		getInstance().aUIJobs.add(job);
 	}
 
 	public static void switchTo(final ViewType type)
 	{
 		schedule(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				getInstance().switchView(type);
 			}
 		});
 	}
 
 	private EverView aActiveView = null;
 	private ViewType aActiveViewType = null;
 	private final Map<EverView, AnimatedAlpha> aAlphaAnimations = new HashMap<EverView, AnimatedAlpha>();
 	private final BlockingQueue<Runnable> aUIJobs = new LinkedBlockingQueue<Runnable>();
 	private final Map<ViewType, EverView> aViewMap = new EnumMap<ViewType, EverView>(ViewType.class);
 
 	private EVViewManager()
 	{
 		sInstance = this;
 		EVFrameManager.register(this);
 		final MainMenuView homeView = new MainMenuView();
 		register(ViewType.MAINMENU, homeView);
 		final LoadingView loadingView = new LoadingView();
 		register(ViewType.LOADING, loadingView);
 		final ServerListView serverListView = new ServerListView();
 		register(ViewType.SERVERLIST, serverListView);
		final PreferencesView preferences = new PreferencesView();
		register(ViewType.PREFERENCES, preferences);
 		switchView(ViewType.MAINMENU);
 	}
 
 	public void deregister(final ViewType type, final Runnable callback)
 	{
 		hideView(aViewMap.get(type), callback);
 		aViewMap.remove(type);
 	}
 
 	@Override
 	public void frame(final FrameUpdate f)
 	{
 		while (!aUIJobs.isEmpty()) {
 			aUIJobs.poll().run();
 		}
 	}
 
 	private void hideView(final EverView view, final Runnable callback)
 	{
 		if (view == null) {
 			return;
 		}
 		aAlphaAnimations.get(view).setTargetAlpha(0).start(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				view.onDefocus();
 				if (callback != null) {
 					callback.run();
 				}
 				EverVoidClient.delRootNode(view);
 			}
 		});
 	}
 
 	@Override
 	public void receivedChat(final String player, final Color playerColor, final String message)
 	{
 		schedule(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if (aActiveViewType.equals(ViewType.LOBBY)) {
 					((LobbyView) aActiveView).receivedChat(player, playerColor, message);
 				}
 				else if (aActiveViewType.equals(ViewType.GAME)) {
 					((GameView) aActiveView).receivedChat(player, playerColor, message);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void receivedGameState(final EVGameState gameState)
 	{
 		// TODO - find the right name
 		final Player p = gameState.getPlayerByName(EverVoidClient.getSettings().getNickname());
 		// TODO: This shouldn't always start a game. it should only start it if it's not in progress already
 		schedule(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				final GameView gameView = new GameView(gameState, p);
 				register(ViewType.GAME, gameView);
 				switchView(ViewType.GAME);
 			}
 		});
 	}
 
 	@Override
 	public void receivedQuit(final Json quitMessage)
 	{
 		// TODO: warn user that someone has quit
 	}
 
 	public void register(final ViewType type, final EverView view)
 	{
 		aViewMap.put(type, view);
 		final AnimatedAlpha animation = view.getNewAlphaAnimation();
 		animation.setDuration(1).setAlpha(0);
 		aAlphaAnimations.put(view, animation);
 	}
 
 	private void switchView(final ViewType type)
 	{
 		if (type.equals(aActiveViewType)) {
 			return;
 		}
 		aActiveViewType = type;
 		hideView(aActiveView, null);
 		final EverView newView = aViewMap.get(type);
 		aActiveView = newView;
 		EverVoidClient.addRootNode(newView.getNodeType(), newView);
 		aActiveView.onFocus();
 		aAlphaAnimations.get(aActiveView).setTargetAlpha(1).start();
 	}
 }
