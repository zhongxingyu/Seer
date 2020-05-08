 package de.nordakademie.nakjava.server.shared.proxy.actions;
 
 import java.rmi.RemoteException;
 
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.PlayerState;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 import de.nordakademie.nakjava.server.internal.Players;
 import de.nordakademie.nakjava.server.internal.Session;
 import de.nordakademie.nakjava.server.shared.proxy.ActionAbstractImpl;
 import de.nordakademie.nakjava.server.shared.proxy.ServerAction;
 import de.nordakademie.nakjava.server.shared.serial.ActionContext;
 
 public class LeaveGameAction extends ActionContext {
 
 	public LeaveGameAction(long sessionNr) {
 		super(sessionNr);
 	}
 
 	@Override
 	protected ServerAction getAction(long sessionNr) throws RemoteException {
 		return new ActionAbstractImpl(sessionNr) {
 
 			@Override
 			protected void performImpl(Session session) {
 				if (!session.isActionInvokerCurrentPlayer()) {
 					session.getModel().changeSelfAndOpponent();
 				}
 				String name = session.getModel().getSelf().getName();
 				PlayerState opponent = session.getModel().getOpponent();
 				if (opponent == null) {
 					session.setToBeDeleted(true);
 				} else if (opponent.getState() != State.LOGIN
 						&& opponent.getState() != State.CONFIGUREGAME
 						&& opponent.getState() != State.EDITDECK) {
 					session.getModel().getOpponent()
 							.setState(State.OTHERPLAYERLEFTGAME);
 				}
 				try {
 					session.getActionInvoker().getControl().remoteClose();
 				} catch (RemoteException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				session.removeActionInvoker();
				Players.getInstance().removeLoggedInPlayerName(name);
 			}
 		};
 	}
 }
