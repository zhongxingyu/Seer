 package de.reneruck.tcd.ipp.database;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 import com.google.gson.Gson;
 
 import de.reneruck.tcd.ipp.database.actions.FinackAndShutdown;
 import de.reneruck.tcd.ipp.database.actions.ReceiveData;
 import de.reneruck.tcd.ipp.database.actions.SendControlSignal;
 import de.reneruck.tcd.ipp.database.actions.SendData;
 import de.reneruck.tcd.ipp.database.actions.ShutdownConnection;
 import de.reneruck.tcd.ipp.datamodel.Datagram;
 import de.reneruck.tcd.ipp.datamodel.Statics;
 import de.reneruck.tcd.ipp.datamodel.TemporalTransitionsStore;
 import de.reneruck.tcd.ipp.datamodel.TransitionExchangeBean;
 import de.reneruck.tcd.ipp.fsm.Action;
 import de.reneruck.tcd.ipp.fsm.FiniteStateMachine;
 import de.reneruck.tcd.ipp.fsm.SimpleState;
 import de.reneruck.tcd.ipp.fsm.Transition;
 import de.reneruck.tcd.ipp.fsm.TransitionEvent;
 
 public class Connection extends Thread {
 
 	private Socket connection;
 	private boolean running = false;
 	private Gson gson = new Gson();
 	private ObjectOutputStream out;
 	private ObjectInputStream in;
 	private FiniteStateMachine fsm;
 	private TemporalTransitionsStore transitionStore;
 	private TransitionExchangeBean transitionExchangeBean;
 
 	public Connection(Socket connection, TemporalTransitionsStore transitionsStore) {
 		this.connection = connection;
 		this.transitionStore = transitionsStore;
 		this.running = true;
 		this.transitionExchangeBean = new TransitionExchangeBean();
 		this.start();
 		setupFSM();
 	}
 
 	private void setupFSM() {
 		this.fsm = new FiniteStateMachine();
 		SimpleState state_start = new SimpleState("start");
 		SimpleState state_syn = new SimpleState("syn");
 		SimpleState state_waitRxMode = new SimpleState("waitRxMode");
 		SimpleState state_ReceiveData = new SimpleState("ReceiveData");
 		SimpleState state_SendData = new SimpleState("SendData");
 		SimpleState state_fin = new SimpleState("finish");
 
 		Action sendACK = new SendControlSignal(this.transitionExchangeBean, Statics.ACK);
 		Action sendRxServerAck = new SendControlSignal(this.transitionExchangeBean, Statics.RX_SERVER_ACK);
 		Action receiveData = new ReceiveData(this.transitionExchangeBean, this.transitionStore);
 		Action sendData = new SendData(this.transitionExchangeBean, this.transitionStore);
 		Action sendFIN = new SendControlSignal(this.transitionExchangeBean, Statics.FIN);
 		Action shutdownConnection = new ShutdownConnection(this);
 		Action sendFIN_ACK_and_SHUTDOWN = new FinackAndShutdown(this.transitionExchangeBean, this);
 
 		Transition rxSyn = new Transition(new TransitionEvent(Statics.SYN), state_syn, sendACK);
 		Transition rxSynAck = new Transition(new TransitionEvent(Statics.SYNACK), state_waitRxMode, null);
 		Transition rxAck = new Transition(new TransitionEvent(Statics.ACK), state_SendData, sendData);
 
 		Transition rxSendData = new Transition(new TransitionEvent(Statics.RX_HELI), state_SendData, sendData);
 		Transition rxReceiveData = new Transition(new TransitionEvent(Statics.RX_SERVER), state_ReceiveData, sendRxServerAck);
 		Transition rxData = new Transition(new TransitionEvent(Statics.DATA), state_ReceiveData, receiveData);
 
 		Transition finishedSending = new Transition(new TransitionEvent(Statics.FINISH_RX_HELI), state_fin, sendFIN);
 		Transition rxFin = new Transition(new TransitionEvent(Statics.FIN), state_fin, sendFIN_ACK_and_SHUTDOWN);
 		Transition rxFinACK = new Transition(new TransitionEvent(Statics.FINACK), null, shutdownConnection);
 
 		state_start.addTranstion(rxSyn);
 		state_syn.addTranstion(rxSyn);
 		state_syn.addTranstion(rxSynAck);
 		state_waitRxMode.addTranstion(rxReceiveData);
 		state_waitRxMode.addTranstion(rxSendData);
 		state_SendData.addTranstion(rxAck);
 		state_SendData.addTranstion(finishedSending);
 		state_ReceiveData.addTranstion(rxData);
 		state_ReceiveData.addTranstion(rxFin);
 		state_fin.addTranstion(rxFinACK);
 
 		this.fsm.setStartState(state_start);
 		this.transitionExchangeBean.setFsm(this.fsm);
 	}
 
 	@Override
 	public void run() {
 		try {
 			InputStream inputStream = this.connection.getInputStream();
 			this.out = new ObjectOutputStream(this.connection.getOutputStream());
 			this.out.flush();
 			this.in = new ObjectInputStream(inputStream);
 			this.transitionExchangeBean.setOut(this.out);
 			this.transitionExchangeBean.setIn(this.in);
 			while (this.running) {
 				handleInput(deserialize(this.in.readObject()));
 			}
 			Thread.sleep(500);
		} catch (IOException | InterruptedException | ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private Object deserialize(Object readObject) {
 		if (readObject instanceof String) {
 			String input = (String) readObject;
 			String[] split = input.split("=");
 			if (split.length > 1) {
 				try {
 					Class<?> transitionClass = Class.forName(split[0]);
 					Object fromJson = this.gson.fromJson(split[1].trim(), transitionClass);
 					System.out.println("Successfully deserialized ");
 					return fromJson;
 				} catch (ClassNotFoundException e) {
 					e.getMessage();
 					System.err.println("Cannot deserialize, discarding package");
 				}
 			} else {
 				System.err.println("No valid class identifier found, discarding package");
 			}
 		}
 		return readObject;
 	}
 
 	private void handleInput(Object input) {
 		if (input instanceof Datagram) {
 			TransitionEvent event = getTransitionEventFromDatagram((Datagram) input);
 			try {
 				this.fsm.handleEvent(event);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			System.err.println("Unknown type " + input.getClass() + " discarding package");
 		}
 	}
 
 	private TransitionEvent getTransitionEventFromDatagram(Datagram input) {
 		TransitionEvent event = new TransitionEvent(input.getType());
 		for (String key : input.getKeys()) {
 			event.addParameter(key, input.getPayload(key));
 		}
 		return event;
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public void setRunning(boolean running) {
 		this.running = running;
 	}
 
 	public void shutdown() {
 		System.out.println("Shutting down Connection");
 		try {
 			this.out.close();
 			this.in.close();
 			this.connection.close();
 			this.running = false;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
