 package ch.cern.atlas.apvs.eventbus.client;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.atmosphere.gwt.client.AtmosphereClient;
 import org.atmosphere.gwt.client.AtmosphereGWTSerializer;
 import org.atmosphere.gwt.client.AtmosphereListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.core.client.GWT;
 
 public class AtmosphereEventBus extends RemoteEventBus {
 	
 	@SuppressWarnings("unused")
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private AtmosphereClient client;
 
 	public AtmosphereEventBus(AtmosphereGWTSerializer serializer) {
 		AtmosphereEventBusListener cometListener = new AtmosphereEventBusListener();
 		
 		client = new AtmosphereClient(GWT.getModuleBaseURL() + "eventBusComet",
 				serializer, cometListener);
 		client.start();
 		
 		getServerEvent();
 	}
 
 	private void getServerEvent() {
 		// FIXME do an async call, server implements suspend, and answers eventually when a remote event needs
 		// to be sent.
 	}
 
 	/**
 	 * broadcast event and (receive it locally to distribute, below)
 	 * 
 	 */
 	@Override
 	public void fireEvent(RemoteEvent<?> event) {
 		client.broadcast(event);
 	}
 	
 	/**
 	 * broadcast event and (receive it locally to distribute, below)
 	 * FIXME source is ignored
 	 * 
 	 */
 	@Override
 	public void fireEventFromSource(RemoteEvent<?> event, int uuid) {
 		client.broadcast(event);
 	}
 
 	public class AtmosphereEventBusListener implements AtmosphereListener {
 
 		// atmosphere 1.0
 		public void onConnected(int heartbeat, int connectionID) {
 		}
 		
 		// atmosphere 1.1
 		public void onConnected(int heartbeat, String connectionUUID) {
 		}
 
 		@Override
 		public void onBeforeDisconnected() {
 		}
 
 		@Override
 		public void onDisconnected() {
 		}
 
 		@Override
 		public void onError(Throwable exception, boolean connected) {
 		}
 
 		@Override
 		public void onHeartbeat() {
 		}
 
 		@Override
 		public void onRefresh() {
 		}
 
 		/**
 		 * handle broadcasted events from other clients
 		 */
 		@Override
 		public void onMessage(List<?> messages) {
 			for (Iterator<?> i = messages.iterator(); i.hasNext(); ) {
 				Object message = i.next();
 				if (message instanceof RemoteEvent<?>) {
 					RemoteEvent<?> event = (RemoteEvent<?>)message;
 					
 					// NOTE: also my own needs to be distributed locally
 					AtmosphereEventBus.super.fireEvent(event);
 				}
 			}
 		}
 
 		// atmosphere 1.1
 		public void onAfterRefresh(String connectionUUID) {
 		}
 	}
 }
