 package rental;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import rental.session.ManagerSession;
 import rental.session.ReservationSession;
 
public interface Agency extends Remote {
 
 	public ReservationSession createReservationSession(String clientName)
 			throws RemoteException;
 
 	public ManagerSession createManagerSession(String managerName)
 			throws RemoteException;
 
 }
