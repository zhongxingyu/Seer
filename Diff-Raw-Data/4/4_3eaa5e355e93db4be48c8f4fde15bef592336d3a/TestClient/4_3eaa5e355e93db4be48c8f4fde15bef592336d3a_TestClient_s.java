 package clientsrc;
 
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Random;
 
 import serversrc.resImpl.InvalidTransactionException;
 import serversrc.resImpl.ServerShutdownException;
 import serversrc.resImpl.TransactionAbortedException;
 import serversrc.resInterface.ResourceManager;
 
 public class TestClient extends client implements Runnable {
 	static Random rand = new Random();
 	int transactionTime; // in milliseconds.
 	int totalResponseTime; // in milliseconds.
 	int totalTransactions;
 	int averageTransactions;
 	int lengthOfExperiment;
 	long startTime;
 	long stopTime;
 
 	TestClient(int requestRate, int numberOfClients, int lengthOfExperiment,
 			String args[]) {
 		if (numberOfClients == 1 || requestRate == 0) {
 			transactionTime = 0;
 
 		} else {
 			transactionTime = (int) (1000f * numberOfClients / requestRate);
 		}
 			String server = "localhost";
 			int port = 1099;
 			if (args.length > 0) {
 				server = args[0];
 			}
 			if (args.length > 1) {
 				port = Integer.parseInt(args[1]);
 			}
 			if (args.length > 2) {
 				System.out.println("Usage: java client [rmihost [rmiport]]");
 				System.exit(1);
 			}
 		this.lengthOfExperiment = lengthOfExperiment;
 
 		try {
 			// get a reference to the rmiregistry
 			Registry registry = LocateRegistry.getRegistry(server, port);
 			// get the proxy and the remote reference by rmiregistry lookup
 			rm = (ResourceManager) registry.lookup("Group2Middleware");
 			if (rm != null) {
 				System.out.println("Successful");
 				System.out.println("Connected to RM");
 			} else {
 				System.out.println("Unsuccessful");
 			}
 			// make call on remote method
 		} catch (Exception e) {
 			System.err.println("Client exception: " + e.toString());
 			e.printStackTrace();
 
 		}
 
 		if (System.getSecurityManager() == null) {
 			// System.setSecurityManager(new RMISecurityManager());
 		}
 
 	}
 
 	@Override
 	public void run() {
 		startTime = System.currentTimeMillis();
 		stopTime = startTime + lengthOfExperiment;
 		while (System.currentTimeMillis() < stopTime) {
 			int identifier, amount, price;
 			long delay = System.currentTimeMillis();
 
 			try {
 				identifier = rand.nextInt();
 				price = (int) (rand.nextInt(500));// random price between 0 and
 													// 500.
 				amount = (int) (rand.nextInt(250));// random number of items to
 													// be added.
 				int tid = rm.start();
 				int selector = rand.nextInt(3);
 
 				switch (selector) {
 
 				case (0):
 					flightTransaction(tid, identifier, amount, price);
 					break;
 				case (1):
 					carTransaction(tid, String.valueOf(identifier), amount,
 							price);
 					break;
 				case (2):
 					hotelTransaction(tid, String.valueOf(identifier), amount,
 							price);
 				}
 
 				rm.commit(tid);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				break;
 			} catch (Exception e) {
 
 			}
 			delay = (int) (System.currentTimeMillis() - delay);
 			totalTransactions++;
 			totalResponseTime += delay;
 			try {
				Thread.sleep(transactionTime - delay);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				return;
 			}
 
 		}
 		averageTransactions = totalResponseTime / totalTransactions;
 	}
 
 	void flightTransaction(int id, int flightNum, int flightSeats,
 			int flightPrice) throws RemoteException,
 			InvalidTransactionException, TransactionAbortedException,
 			ServerShutdownException {
 		rm.queryFlight(id, flightNum);
 		rm.addFlight(id, flightNum, flightSeats, flightPrice);
 		rm.queryFlight(id, flightNum);
 		rm.deleteFlight(id, flightNum);
 		rm.addFlight(id, flightNum, flightSeats, flightPrice);
 		rm.queryFlight(id, flightNum);
 		rm.reserveFlight(id, rm.newCustomer(id), flightNum);
 
 	}
 
 	void carTransaction(int id, String location, int numberOfCars, int carPrice)
 			throws RemoteException, InvalidTransactionException,
 			TransactionAbortedException, ServerShutdownException {
 		rm.queryCars(id, location);
 		rm.addCars(id, location, numberOfCars, carPrice);
 		rm.queryCars(id, location);
 		rm.deleteCars(id, location);
 		rm.addCars(id, location, numberOfCars, carPrice);
 		rm.queryCars(id, location);
 		rm.reserveCar(id, rm.newCustomer(id), location);
 	}
 
 	void hotelTransaction(int id, String location, int rooms, int price)
 			throws RemoteException, InvalidTransactionException,
 			TransactionAbortedException, ServerShutdownException {
 		rm.queryRooms(id, location);
 		rm.addRooms(id, location, rooms, price);
 		rm.queryRooms(id, location);
 		rm.deleteRooms(id, location);
 		rm.addRooms(id, location, rooms, price);
 		rm.reserveRoom(id, rm.newCustomer(id), location);
 	}
 }
