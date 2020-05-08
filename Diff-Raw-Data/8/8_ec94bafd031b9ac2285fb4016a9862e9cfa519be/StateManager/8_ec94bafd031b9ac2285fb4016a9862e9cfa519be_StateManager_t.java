 package com.blarg.gdx.states;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Disposable;
 import com.blarg.gdx.GameApp;
 import com.blarg.gdx.Strings;
 import com.blarg.gdx.events.EventManager;
 import com.blarg.gdx.ReflectionUtils;
 import com.blarg.gdx.graphics.RenderContext;
 
 import java.util.LinkedList;
 
 public class StateManager implements Disposable {
 	public final GameApp gameApp;
 	public final EventManager eventManager;
 
 	LinkedList<StateInfo> states;
 	LinkedList<StateInfo> pushQueue;
 	LinkedList<StateInfo> swapQueue;
 
 	boolean pushQueueHasOverlay;
 	boolean swapQueueHasOverlay;
 	boolean lastCleanedStatesWereAllOverlays;
 
 	public StateManager(GameApp gameApp, EventManager eventManager) {
 		if (gameApp == null)
 			throw new IllegalArgumentException("gameApp cannot be null.");
 
 		Gdx.app.debug("StateManager", "ctor");
 
 		states = new LinkedList<StateInfo>();
 		pushQueue = new LinkedList<StateInfo>();
 		swapQueue = new LinkedList<StateInfo>();
 
 		this.gameApp = gameApp;
 		this.eventManager = eventManager;
 	}
 
 	/*** public state getters ***/
 
 	public GameState getTopState() {
 		StateInfo top = getTop();
 		return (top == null ? null : top.state);
 	}
 
 	public GameState getTopNonOverlayState() {
 		StateInfo top = getTopNonOverlay();
 		return (top == null ? null : top.state);
 	}
 
 	public boolean isTransitioning() {
 		for (int i = 0; i < states.size(); ++i) {
 			if (states.get(i).isTransitioning)
 				return true;
 		}
 		return false;
 	}
 
 	public boolean isEmpty() {
 		return (states.size() == 0 && pushQueue.size() == 0 && swapQueue.size() == 0);
 	}
 
 	public boolean isStateTransitioning(GameState state) {
 		if (state == null)
 			throw new IllegalArgumentException("state cannot be null.");
 
 		StateInfo info = getStateInfoFor(state);
 		return (info == null ? false : info.isTransitioning);
 	}
 
 	public boolean isTopState(GameState state) {
 		if (state == null)
 			throw new IllegalArgumentException("state cannot be null.");
 
 		StateInfo info = getTop();
 		return (info == null ? false : (info.state == state));
 	}
 
 	public boolean hasState(String name) {
 		for (int i = 0; i < states.size(); ++i) {
 			StateInfo info = states.get(i);
 			if (!Strings.isNullOrEmpty(info.name) && info.name.equals(name))
 				return true;
 		}
 		return false;
 	}
 
 	/** Push / Pop / Overlay / Swap / Queue ***/
 
 	public <T extends GameState> T push(Class<T> stateType) {
 		return push(stateType, null);
 	}
 
 	public <T extends GameState> T push(Class<T> stateType, String name) {
 		T newState;
 		try {
 			newState = ReflectionUtils.instantiateObject(stateType,
 			                                             new Class<?>[] { StateManager.class, EventManager.class },
 			                                             new Object[] { this, eventManager });
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Could not instantiate a GameState instance of that type.");
 		}
 
 		StateInfo stateInfo = new StateInfo(newState, name);
 		queueForPush(stateInfo);
 		return newState;
 	}
 
 	public <T extends GameState> T overlay(Class<T> stateType) {
		return overlay(stateType, null);
 	}
 
 	public <T extends GameState> T overlay(Class<T> stateType, String name) {
 		T newState;
 		try {
 			newState = ReflectionUtils.instantiateObject(stateType,
 			                                             new Class<?>[] { StateManager.class, EventManager.class },
 			                                             new Object[] { this, eventManager });
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Could not instantiate a GameState instance of that type.");
 		}
 
 		StateInfo stateInfo = new StateInfo(newState, name);
 		stateInfo.isOverlay = true;
 		queueForPush(stateInfo);
 		return newState;
 	}
 
 	public <T extends GameState> T swapTopWith(Class<T> stateType) {
		return swapTopWith(stateType, null);
 	}
 
 	public <T extends GameState> T swapTopWith(Class<T> stateType, String name) {
 		// figure out if the current top state is an overlay or not. use that
 		// same setting for the new state that is being swapped in
 		StateInfo currentTopStateInfo = getTop();
 		if (currentTopStateInfo == null)
 			throw new UnsupportedOperationException("Cannot swap, no existing states.");
 		boolean isOverlay = currentTopStateInfo.isOverlay;
 
 		T newState;
 		try {
 			newState = ReflectionUtils.instantiateObject(stateType,
 			                                             new Class<?>[] { StateManager.class, EventManager.class },
 			                                             new Object[] { this, eventManager });
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Could not instantiate a GameState instance of that type.");
 		}
 
 		StateInfo stateInfo = new StateInfo(newState, name);
 		stateInfo.isOverlay = isOverlay;
 		queueForSwap(stateInfo, false);
 		return newState;
 	}
 
 	public <T extends GameState> T swapTopNonOverlayWith(Class<T> stateType) {
		return swapTopWith(stateType, null);
 	}
 
 	public <T extends GameState> T swapTopNonOverlayWith(Class<T> stateType, String name) {
 		T newState;
 		try {
 			newState = ReflectionUtils.instantiateObject(stateType,
 			                                             new Class<?>[] { StateManager.class, EventManager.class },
 			                                             new Object[] { this, eventManager });
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Could not instantiate a GameState instance of that type.");
 		}
 
 		StateInfo stateInfo = new StateInfo(newState, name);
 		queueForSwap(stateInfo, true);
 		return newState;
 	}
 
 	public void pop() {
 		if (isTransitioning())
 			throw new UnsupportedOperationException();
 
 		Gdx.app.debug("StateManager", "Pop initiated for top-most state only.");
 		startOnlyTopStateTransitioningOut(false);
 	}
 
 	public void popTopNonOverlay() {
 		if (isTransitioning())
 			throw new UnsupportedOperationException();
 
 		Gdx.app.debug("StateManager", "Pop initiated for all top active states.");
 		startTopStatesTransitioningOut(false);
 	}
 
 	private void queueForPush(StateInfo newStateInfo) {
 		if (newStateInfo == null)
 			throw new IllegalArgumentException("newStateInfo cannot be null.");
 		if (newStateInfo.state == null)
 			throw new IllegalArgumentException("New StateInfo object has null GameState.");
 		if (pushQueueHasOverlay && !newStateInfo.isOverlay)
 			throw new UnsupportedOperationException("Cannot queue new non-overlay state while queue is active with overlay states.");
 
 		Gdx.app.debug("StateManager", String.format("Queueing state %s for pushing.", newStateInfo));
 
 		if (!newStateInfo.isOverlay)
 			startTopStatesTransitioningOut(true);
 
 		pushQueue.add(newStateInfo);
 
 		if (newStateInfo.isOverlay)
 			pushQueueHasOverlay = true;
 	}
 
 	private void queueForSwap(StateInfo newStateInfo, boolean swapTopNonOverlay) {
 		if (newStateInfo == null)
 			throw new IllegalArgumentException("newStateInfo cannot be null.");
 		if (newStateInfo.state == null)
 			throw new IllegalArgumentException("New StateInfo object has null GameState.");
 		if (swapQueueHasOverlay && !newStateInfo.isOverlay)
 			throw new UnsupportedOperationException("Cannot queue new non-overlay state while queue is active with overlay states.");
 
 		Gdx.app.debug("StateManager", String.format("Queueing state %s for swapping with %s.", newStateInfo, (swapTopNonOverlay ? "all top active states" : "only top-most active state.")));
 
 		if (swapTopNonOverlay)
 			startTopStatesTransitioningOut(false);
 		else
 			startOnlyTopStateTransitioningOut(false);
 
 		swapQueue.add(newStateInfo);
 
 		if (newStateInfo.isOverlay)
 			swapQueueHasOverlay = true;
 	}
 
 	/*** events ***/
 
 	public void onAppPause() {
 		for (int i = getTopNonOverlayIndex(); i != -1 && i < states.size(); ++i) {
 			StateInfo stateInfo = states.get(i);
 			if (!stateInfo.isInactive)
 				stateInfo.state.onAppPause();
 		}
 	}
 
 	public void onAppResume() {
 		for (int i = getTopNonOverlayIndex(); i != -1 && i < states.size(); ++i) {
 			StateInfo stateInfo = states.get(i);
 			if (!stateInfo.isInactive)
 				stateInfo.state.onAppResume();
 		}
 	}
 
 	public void onResize() {
 		for (int i = getTopNonOverlayIndex(); i != -1 && i < states.size(); ++i) {
 			StateInfo stateInfo = states.get(i);
 			if (!stateInfo.isInactive)
 				stateInfo.state.onResize();
 		}
 	}
 
 	public void onRender(float delta, RenderContext renderContext) {
 		for (int i = getTopNonOverlayIndex(); i != -1 && i < states.size(); ++i) {
 			StateInfo stateInfo = states.get(i);
 			if (!stateInfo.isInactive) {
 				stateInfo.state.onRender(delta, renderContext);
 				stateInfo.state.effectManager.onRenderGlobal(delta, renderContext);
 			}
 		}
 	}
 
 	public void onUpdate(float delta) {
 		lastCleanedStatesWereAllOverlays = false;
 
 		cleanupInactiveStates();
 		checkForFinishedStates();
 		processQueues();
 		resumeStatesIfNeeded();
 		updateTransitions(delta);
 
 		for (int i = getTopNonOverlayIndex(); i != -1 && i < states.size(); ++i) {
 			StateInfo stateInfo = states.get(i);
 			if (!stateInfo.isInactive)
 				stateInfo.state.onUpdate(delta);
 		}
 	}
 
 	/*** internal state management functions ***/
 
 	private void startTopStatesTransitioningOut(boolean pausing) {
 		int i = getTopNonOverlayIndex();
 		if (i == -1)
 			return;
 
 		for (; i < states.size(); ++i) {
 			// only look at active states, since inactive ones have already been
 			// transitioned out and will be removed on the next onUpdate()
 			if (!states.get(i).isInactive)
 				transitionOut(states.get(i), !pausing);
 		}
 	}
 
 	private void startOnlyTopStateTransitioningOut(boolean pausing) {
 		StateInfo info = getTop();
 
 		// if it's not active, then it's just been transitioned out and will be
 		// removed on the next onUpdate()
 		if (!info.isInactive)
 			transitionOut(info, !pausing);
 	}
 
 	private void cleanupInactiveStates() {
 		// we don't want to remove any states until everything is done transitioning.
 		// this is to avoid the scenario where the top non-overlay state finishes
 		// transitioning before one of the overlays. if we removed it, the overlays
 		// would then be overlayed over an inactive non-overlay (which wouldn't get
 		// resumed until the current active overlays were done being transitioned)
 		if (isTransitioning())
 			return;
 
 		boolean cleanedUpSomething = false;
 		boolean cleanedUpNonOverlay = false;
 
 		int i = 0;
 		while (i < states.size()) {
 			StateInfo stateInfo = states.get(i);
 			if (stateInfo.isInactive && stateInfo.isBeingPopped) {
 				cleanedUpSomething = true;
 				if (!stateInfo.isOverlay)
 					cleanedUpNonOverlay = true;
 
 				// remove this state and move to the next node
 				// (index doesn't change, we're removing one, so next index now equals this index)
 				states.remove(i);
 
 				Gdx.app.debug("StateManager", String.format("Deleting inactive popped state %s.", stateInfo));
 				stateInfo.state.dispose();
 
 			} else {
 				i++;
 			}
 		}
 
 		if (cleanedUpSomething && !cleanedUpNonOverlay)
 			lastCleanedStatesWereAllOverlays = true;
 	}
 
 	private void checkForFinishedStates() {
 		if (states.size() == 0)
 			return;
 
 		// don't do anything if something is currently transitioning
 		if (isTransitioning())
 			return;
 
 		boolean needToAlsoTransitionOutOverlays = false;
 
 		// check the top non-overlay state first to see if it's finished
 		// and should be transitioned out
 		StateInfo topNonOverlayStateInfo = getTopNonOverlay();
 		if (!topNonOverlayStateInfo.isInactive && topNonOverlayStateInfo.state.isFinished()) {
 			Gdx.app.debug("StateManager", String.format("State %s marked as finished.", topNonOverlayStateInfo));
 			transitionOut(topNonOverlayStateInfo, true);
 
 			needToAlsoTransitionOutOverlays = true;
 		}
 
 		// now also check the overlay states (if there were any). we force them to
 		// transition out if the non-overlay state started to transition out so that
 		// we don't end up with overlay states without a parent non-overlay state
 
 		// start the loop off 1 beyond the top non-overlay (which is where the
 		// overlays are, if any)
 		int i = getTopNonOverlayIndex();
 		if (i != -1) {
 			for (++i; i < states.size(); ++i) {
 				StateInfo stateInfo = states.get(i);
 				if (!stateInfo.isInactive && (stateInfo.state.isFinished() || needToAlsoTransitionOutOverlays)) {
 					Gdx.app.debug("StateManager", String.format("State %s marked as finished.", stateInfo));
 					transitionOut(stateInfo, true);
 				}
 			}
 		}
 	}
 
 	private void processQueues() {
 		// don't do anything if stuff is currently transitioning
 		if (isTransitioning())
 			return;
 
 		if (pushQueue.size() > 0 && swapQueue.size() > 0)
 			throw new UnsupportedOperationException("Cannot process queues when both the swap and push queues have items currently in them.");
 
 		// for each state in the queu, add it to the main list and start transitioning it in
 		// (note, only one of these queues will be processed each tick due to the above check!)
 
 		while (pushQueue.size() > 0) {
 			StateInfo stateInfo = pushQueue.removeFirst();
 
 			if (states.size() > 0) {
 				// if this new state is an overlay, and the current top state is both
 				// currently active and is not currently marked as being overlay-ed
 				// then we should pause it due to overlay
 				StateInfo currentTopStateInfo = getTop();
 				if (stateInfo.isOverlay && !currentTopStateInfo.isInactive && !currentTopStateInfo.isOverlayed) {
 					Gdx.app.debug("StateManager", String.format("Pausing %sstate %s due to overlay.", (currentTopStateInfo.isOverlay ? "overlay " : ""), currentTopStateInfo));
 					currentTopStateInfo.state.onPause(true);
 
 					// also mark the current top state as being overlay-ed
 					currentTopStateInfo.isOverlayed = true;
 				}
 			}
 
 			Gdx.app.debug("StateManager", String.format("Pushing %sstate %s from push-queue.", (stateInfo.isOverlay ? "overlay " : ""), stateInfo));
 			stateInfo.state.onPush();
 
 			transitionIn(stateInfo, false);
 
 			states.addLast(stateInfo);
 		}
 
 		while (swapQueue.size() > 0) {
 			StateInfo stateInfo = swapQueue.removeFirst();
 
 			// if this new state is an overlay, and the current top state is both
 			// currently active and is not currently marked as being overlay-ed
 			// then we should pause it due to overlay
 			StateInfo currentTopStateInfo = getTop();
 			if (stateInfo.isOverlay && !currentTopStateInfo.isInactive && !currentTopStateInfo.isOverlayed) {
 				Gdx.app.debug("StateManager", String.format("Pausing %sstate %s due to overlay.", (currentTopStateInfo.isOverlay ? "overlay " : ""), currentTopStateInfo));
 				currentTopStateInfo.state.onPause(true);
 
 				// also mark the current top state as being overlay-ed
 				currentTopStateInfo.isOverlayed = true;
 			}
 
 			Gdx.app.debug("StateManager", String.format("Pushing %sstate %s from swap-queue.", (stateInfo.isOverlay ? "overlay " : ""), stateInfo));
 			stateInfo.state.onPush();
 
 			transitionIn(stateInfo, false);
 
 			states.addLast(stateInfo);
 		}
 
 		pushQueueHasOverlay = false;
 		swapQueueHasOverlay = false;
 	}
 
 	private void resumeStatesIfNeeded() {
 		if (states.size() == 0)
 			return;
 
 		// don't do anything if stuff is currently transitioning
 		if (isTransitioning())
 			return;
 
 		// did we just clean up one or more overlay states?
 		if (lastCleanedStatesWereAllOverlays) {
 			// then we need to resume the current top state
 			// (those paused with the flag "from an overlay")
 			StateInfo stateInfo = getTop();
 			if (stateInfo.isInactive || !stateInfo.isOverlayed)
 				throw new UnsupportedOperationException();
 
 			Gdx.app.debug("StateManager", String.format("Resuming %sstate %s due to overlay removal.", (stateInfo.isOverlay ? "overlay " : ""), stateInfo));
 			stateInfo.state.onResume(true);
 
 			stateInfo.isOverlayed = false;
 
 			return;
 		}
 
 		// if the top state is no inactive, then we don't need to resume anything
 		if (!getTop().isInactive)
 			return;
 
 		Gdx.app.debug("StateManager", "Top-most state is inactive. Resuming all top states up to and including the next non-overlay.");
 
 		// top state is inactive. time to reusme one or more states...
 		// find the topmost non-overlay state and take it and all overlay states that
 		// are above it, and transition them in
 		for (int i = getTopNonOverlayIndex(); i != -1 && i < states.size(); ++i) {
 			StateInfo stateInfo = states.get(i);
 			Gdx.app.debug("StateManager", String.format("Resuming %sstate %s.", (stateInfo.isOverlay ? "overlay " : ""), stateInfo));
 			stateInfo.state.onResume(false);
 
 			transitionIn(stateInfo, true);
 		}
 	}
 
 	private void updateTransitions(float delta) {
 		for (int i = getTopNonOverlayIndex(); i != -1 && i < states.size(); ++i) {
 			StateInfo stateInfo = states.get(i);
 			if (stateInfo.isTransitioning) {
 				boolean isDone = stateInfo.state.onTransition(delta, stateInfo.isTransitioningOut, stateInfo.isTransitionStarting);
 				if (isDone) {
 					Gdx.app.debug("StateManager", String.format("Transition %s %sstate %s finished.",
 					                                          (stateInfo.isTransitioningOut ? "out of" : "into"),
 					                                          (stateInfo.isOverlay ? "overlay " : ""),
 				                                              stateInfo));
 
 					// if the state was being transitioned out, then we should mark
 					// it as inactive, and trigger it's onPop() or onPause() event now
 					if (stateInfo.isTransitioningOut) {
 						if (stateInfo.isBeingPopped) {
 							Gdx.app.debug("StateManager", String.format("Popping %sstate %s", (stateInfo.isOverlay ? "overlay " : ""), stateInfo));
 							stateInfo.state.onPop();
 
 							// TODO: do I care enough to port the return value stuff which goes here?
 						} else {
 							Gdx.app.debug("StateManager", String.format("Pausing %sstate %s.", (stateInfo.isOverlay ? "overlay " : ""), stateInfo));
 							stateInfo.state.onPause(false);
 						}
 						stateInfo.isInactive = true;
 					}
 
 					// done transitioning
 					stateInfo.isTransitioning = false;
 					stateInfo.isTransitioningOut = false;
 				}
 
 				stateInfo.isTransitionStarting = false;
 			}
 		}
 	}
 
 	private void transitionIn(StateInfo stateInfo, boolean forResuming) {
 		stateInfo.isInactive = false;
 		stateInfo.isTransitioning = true;
 		stateInfo.isTransitioningOut = false;
 		stateInfo.isTransitionStarting = true;
 		Gdx.app.debug("StateManager", String.format("Transition into %sstate %s started.", (stateInfo.isOverlay ? "overlay " : ""), stateInfo));
 
 		//if (forResuming)
 		//	stateInfo.getState().getProcessManager().onResume(false);
 	}
 
 	private void transitionOut(StateInfo stateInfo, boolean forPopping) {
 		stateInfo.isTransitioning = true;
 		stateInfo.isTransitioningOut = true;
 		stateInfo.isTransitionStarting = true;
 		stateInfo.isBeingPopped = forPopping;
 		Gdx.app.debug("StateManager", String.format("Transition out of %sstate %s started.", (stateInfo.isOverlay ? "overlay " : ""), stateInfo));
 
 		//if (forPopping)
 		//	stateInfo.getState().getProcessManager().removeAll();
 		//else
 		//	stateInfo.getState().getProcessManager().onPause(false);
 	}
 
 	/*** private state getters ***/
 
 	private StateInfo getStateInfoFor(GameState state) {
 		if (state == null)
 			throw new IllegalArgumentException("state cannot be null.");
 
 		for (int i = 0; i < states.size(); ++i) {
 			if (states.get(i).state == state)
 				return states.get(i);
 		}
 		return null;
 	}
 
 	private StateInfo getTop() {
 		return (states.isEmpty() ? null : states.getLast());
 	}
 
 	private StateInfo getTopNonOverlay() {
 		int index = getTopNonOverlayIndex();
 		return (index == -1 ? null : states.get(index));
 	}
 
 	private int getTopNonOverlayIndex() {
 		for (int i = states.size() - 1; i >= 0; i--) {
 			if (!states.get(i).isOverlay)
 				return i;
 		}
 		return (states.size() > 0 ? 0 : -1);
 	}
 
 	/*** cleanup ***/
 
 	public void dispose() {
 		if (states == null)
 			return;
 
 		Gdx.app.debug("StateManager", "dispose");
 
 		while (states.size() > 0) {
 			StateInfo stateInfo = states.getLast();
 			Gdx.app.debug("StateManager", String.format("Popping state %s as part of StateManager shutdown.", stateInfo));
 			stateInfo.state.onPop();
 			stateInfo.state.dispose();
 			states.removeLast();
 		}
 
 		// these queues will likely not have anything in them, but just in case ...
 		while (pushQueue.size() > 0) {
 			StateInfo stateInfo = pushQueue.removeFirst();
 			Gdx.app.debug("StateManager", String.format("Deleting push-queued state %s as part of StateManager shutdown.", stateInfo));
 			stateInfo.state.dispose();
 		}
 		while (swapQueue.size() > 0) {
 			StateInfo stateInfo = swapQueue.removeFirst();
 			Gdx.app.debug("StateManager", String.format("Deleting swap-queued state %s as part of StateManager shutdown.", stateInfo));
 			stateInfo.state.dispose();
 		}
 
 		states = null;
 		pushQueue = null;
 		swapQueue = null;
 	}
 }
