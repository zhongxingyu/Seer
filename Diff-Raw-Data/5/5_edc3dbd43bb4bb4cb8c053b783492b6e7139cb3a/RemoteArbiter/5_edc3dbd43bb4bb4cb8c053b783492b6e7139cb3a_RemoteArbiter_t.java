 //import java.io.*;
 import java.lang.System;
 
 /**
  * Remote arbiter.
  *
  * Remote arbiter from the point of view of LocalGameController.
  */
 class RemoteArbiter implements Arbiter, RemoteArbiterTransport.Parent {
 	RemoteArbiterTransport transport;
 	LocalGameController gameController;
 
 	public class CreationError extends Exception {
 		public CreationError(String s) {
 			super(s);
 		}
 	}
 
 	private long lastPong;
 	private long lastPing;
 
 	public Parent parent;
 
 	public RemoteArbiter(Parent parent, int type) throws CreationError {
 		this.parent = parent;
 
 		try {
 			transport = RemoteArbiterTransportFactory.Create(this, type);
 		} catch (InvalidArgumentException e) {
 			throw new CreationError(e.getMessage());
 		}
 		touchLastPong();
 		touchLastPing();
 		transport.start();
 	}
 
 	protected void touchLastPong() {
 		lastPong = System.currentTimeMillis();
 	}
 
 	protected void touchLastPing() {
 		lastPing = System.currentTimeMillis();
 	}
 
 	public boolean isLastPongValid() {
		return ((lastPing - lastPong) < 1000 * 3);
 	}
 
 	public boolean shouldSentNewPing() {
		return (System.currentTimeMillis() > (lastPing + 1000 * 2));
 	}
 
 	public void sentNewPing() {
 		transport.sendMsg(Protocol.PING);
 		touchLastPing();
 	}
 
 	public void shutdown() {
 		parent.handleArbiterMsg("hiere");
 		if (gameController != null) {
 		parent.handleArbiterMsg("shuting down game controller");
 			gameController.shutdown();
 		}
 		if (transport != null) {
 			transport.stop();
 			transport = null;
 		}
 	}
 
 	public void connected(LocalGameController gc) {
 		gameController = gc;
 		gc.connectedTo(this);
 	}
 
 	public void handleColor(GameController gc, int color) {
 		transport.sendMsg(
 				Protocol.HANDLE_COLOR + " "
 				+ String.valueOf(color)
 				);
 	}
 
 	public void unhandleColor(GameController gc, int color) {
 		transport.sendMsg(
 				Protocol.UNHANDLE_COLOR + " "
 				+ String.valueOf(color)
 				);
 	}
 
 	public void moveRequest(GameController gc, int x, int y) {
 		transport.sendMsg(
 				Protocol.MOVE_REQUEST + " "
 				+ String.valueOf(x) + " "
 				+ String.valueOf(y)
 				);
 	}
 
 	/**
 	 * Analyze the message that comes from transport.
 	 *
 	 * Will be called from different thread.
 	 */
 	public void receiveMsg(String msg) {
 		if (this.gameController == null) {
 			protocolFailure("null gameController on receive");
 			return;
 		}
 		Tokenizer s = new Tokenizer(msg);
 		String cmd = s.next();
 		if (cmd.equals(Protocol.PLACE_STONE)) {
 			String xs = s.next();
 			String ys = s.next();
 			String cs = s.next();
 			if (xs.equals("") || ys.equals("") || cs.equals("")) {
 				protocolFailure("no enought arguments for cmd: " + cmd);
 				return;
 			}
 			int x = Integer.parseInt(xs);
 			int y = Integer.parseInt(ys);
 			int c = Integer.parseInt(cs);
 			gameController.placeStone(x, y, c);
 			return;
 		} else if (cmd.equals(Protocol.CLEAR_BOARD)) {
 			gameController.clearBoard();
 			return;
 		} else if (cmd.equals(Protocol.GAME_INFO)) {
 			gameController.gameInfo(s.rest());
 			return;
 		} else if (cmd.equals(Protocol.PONG)) {
 			touchLastPong();
 			return;
 		}
 		protocolFailure("unknown command: '" + cmd + "'");
 	}
 
 	protected void protocolFailure(String s) {
 		// TODO: FIXME
 		parent.handleArbiterMsg("Protocol failure: " + s);
 	}
 
 	public void handleTransportConnected(RemoteArbiterTransport t, String s) {
 		parent.handleArbiterInitFinished();
 		parent.handleArbiterMsg("Connected to: " + s);
 	}
 
 	public void handleTransportDisconnected(
 			RemoteArbiterTransport t,
 			String s
 			) {
 		shutdown();
 		parent.handleArbiterMsg(s);
 	}
 
 	public void handleTransportInfo(RemoteArbiterTransport t, String s) {
 		parent.handleArbiterMsg("Info: " + s);
 	}
 }
 
