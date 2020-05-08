 package net.randomhacks.wave.gadgets.client;
 
 import java.util.ArrayList;
 
 import net.randomhacks.wave.gadgets.client.Participant.JsParticipant;
 
 /** Provides access to Wave-specific APIs. */
 class WaveImpl extends Wave {
 	
 	private State state = new StateImpl();
 	private boolean callbacksAreInitialized = false;
 	private ArrayList<StateListener> stateListeners =
 		new ArrayList<StateListener>();
 
 	public Participant getViewer() {
 		return new Participant(getJsViewer());
 	}
 	
 	private native JsParticipant getJsViewer() /*-{
 		return wave.getViewer();
 	}-*/;
 	
 	public State getState() {
 		return state;
 	}
 
 	public void addStateListener(StateListener listener) {
 		maybeInitializeCallbacks();
 		stateListeners.add(listener);
 	}
 
 	private void maybeInitializeCallbacks() {
 		if (!callbacksAreInitialized) {
 			callbacksAreInitialized = true;
 			initializeCallbacks();
 		}
 	}
 
 	private native void initializeCallbacks() /*-{
 		wave.setStateCallback(
 			this.@net.randomhacks.wave.gadgets.client.WaveImpl::notifyStateListeners(),
 			this);
 	}-*/;
 
 	@SuppressWarnings("unused")
 	private void notifyStateListeners() {
 		State state = getState();
 		for (StateListener listener : stateListeners)
 			listener.onStateChange();
 	}
 
 	public native boolean isPlayback() /*-{
		return wave.isPlayback();
 	}-*/;
 }
