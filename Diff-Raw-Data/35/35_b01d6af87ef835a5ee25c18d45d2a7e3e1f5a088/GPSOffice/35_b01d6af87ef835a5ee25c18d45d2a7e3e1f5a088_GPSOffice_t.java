 import java.rmi.NoSuchObjectException;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import edu.rit.ds.Lease;
 import edu.rit.ds.RemoteEventFilter;
 import edu.rit.ds.RemoteEventGenerator;
 import edu.rit.ds.RemoteEventListener;
 import edu.rit.ds.registry.AlreadyBoundException;
 import edu.rit.ds.registry.NotBoundException;
 import edu.rit.ds.registry.RegistryProxy;
 
 /**
  * class GPSOffice represents an GPS Office in the Geographic Package Service
  * (GPS) system. It binds to the Registry Server. It takes the package from the
  * customer and routes it to the destination through it's neighbouring GPS
  * Offices
  * 
  * @author Punit
  * @version 04-05-2013
  * 
  */
 public class GPSOffice implements GPSOfficeRef {
 
 	private String name;
 	private double x;
 	private double y;
 	private List<Neighbor> neighbors;
 	private RegistryProxy registry;
 	private static RemoteEventGenerator<GPSOfficeEvent> eventGenerator;
 	private ScheduledExecutorService reaper;
 
 	/**
 	 * This constructor takes values from the command line and binds the office
 	 * to the Registry Server
 	 * 
 	 * @param args
 	 *            command line arguments
 	 * @throws RemoteException
 	 */
 	public GPSOffice(String[] args) throws RemoteException {
 
 		// Thread pool for sending packages simultaneously using different
 		// threads
 		reaper = Executors.newScheduledThreadPool(1000);
 
 		// Check if the number of command line arguments are correct
 		if (args.length != 5) {
 			throw new IllegalArgumentException(
 					"Usage: java Start GPSOffice <host> <port> <name> <X> <Y>"
 							+ "\n<host> is the name of the host computer where the Registry Server is running."
 							+ "\n<port> is the port number to which the Registry Server is listening"
 							+ "\n<name> is the name of the city where the GPS office is located"
 							+ "\n<X> is the GPS office's X coordinate (type double)"
 							+ "\n<Y> is the GPS office's Y coordinate (type double)");
 		}
 
 		String host = args[0];
 		name = args[2];
 		int port;
 
 		// Check if the port is an Integer
 		try {
 			port = Integer.parseInt(args[1]);
 		} catch (NumberFormatException e) {
 			throw new NumberFormatException("Port has to be an Integer value");
 		}
 
 		// Check if the X and Y co-ordinates of the GPS Office are Double
 		try {
 			x = Double.parseDouble(args[3]);
 			y = Double.parseDouble(args[4]);
 		} catch (NumberFormatException e) {
 			throw new NumberFormatException(
 					"X and Y co-ordinates should be Double value");
 		}
 
 		// Create a RemoteEventGenerator object to report GPS Office events
 		eventGenerator = new RemoteEventGenerator<GPSOfficeEvent>();
 
 		// Fetch the proxy for Registry Server
 		try {
 			registry = new RegistryProxy(host, port);
 			UnicastRemoteObject.exportObject(this, 0);
 		} catch (RemoteException e1) {
 			e1.printStackTrace();
 			throw new IllegalArgumentException("No Remote Server at host="
 					+ host + " and port=" + port);
 		}
 
 		// Bind the GPS Office to the Registry
 		try {
 			registry.bind(name, this);
 		} catch (AlreadyBoundException e) {
 			try {
 				UnicastRemoteObject.unexportObject(this, true);
 			} catch (NoSuchObjectException e2) {
 				e2.printStackTrace();
 			}
 			throw new IllegalArgumentException("GPSOffice \"" + name
 					+ "\" already exists");
 		} catch (RemoteException e) {
 			try {
 				UnicastRemoteObject.unexportObject(this, true);
 			} catch (NoSuchObjectException e2) {
 				e2.printStackTrace();
 			}
 			throw e;
 		}
 
 	}
 
 	/**
 	 * Generates the neighbors of the current node by looking up in the registry
 	 * and fetching all the {@linkplain GPSOffice} in the registry. It will
 	 * update the Neighbor list in the {@linkplain GPSOffice} class
 	 * 
 	 * @param trackingNumber
 	 *            tracking number of the package
 	 * @param x2
 	 *            x co-ordinate of the destination
 	 * @param y2
 	 *            y co-ordinate of the destination
 	 * @throws RemoteException
 	 *             exception thrown in the Remote object is not available
 	 */
 	@Override
 	public void generateNeighbors(long trackingNumber, double x2, double y2)
 			throws RemoteException {
 
 		List<String> offices = registry.list("GPSOffice");
 		List<Neighbor> gpsNeighbors = new ArrayList<Neighbor>();
 		List<GPSOfficeRef> allGPSOffices = new ArrayList<GPSOfficeRef>();
 
 		for (String office : offices) {
 			final GPSOfficeRef gpsOffice;
 			try {
 
 				gpsOffice = (GPSOfficeRef) registry.lookup(office);
 				if (gpsOffice == null)
 					continue;
 
 				allGPSOffices.add(gpsOffice);
 
 				try {
 					if (gpsOffice.getGPSOfficeName().equals(name))
 						continue;
 
 				} catch (java.rmi.ConnectException e) {
 					// When the GPSOffice is externally killed, the registry
 					// takes some time to unbind it. If a look up is made
 					// meanwhile, the unbound object is also return in the list
 					// of the lookup
 					continue;
 				}
 
 				double gpsOfficeX = gpsOffice.getGPSOfficeCoordinates()[0];
 				double gpsOfficeY = gpsOffice.getGPSOfficeCoordinates()[1];
 				final double dist = getDistance(x, y, gpsOfficeX, gpsOfficeY);
 
 				Neighbor neighbor = new Neighbor(gpsOffice, dist);
 				gpsNeighbors.add(neighbor);
 			} catch (NotBoundException e) {
 				// e.printStackTrace();
 				eventGenerator.reportEvent(new GPSOfficeEvent(this
 						.getGPSOfficeName(), trackingNumber, x2, y2, 3));
 			}
 		}
 
 		Collections.sort(gpsNeighbors, new NeighborComparator());
 
 		if (gpsNeighbors.size() == 1) {
 			neighbors = new ArrayList<Neighbor>(gpsNeighbors.subList(0, 1));
 		} else if (gpsNeighbors.size() == 2) {
 			neighbors = new ArrayList<Neighbor>(gpsNeighbors.subList(0, 2));
 		} else if (gpsNeighbors.size() >= 3) {
 			neighbors = new ArrayList<Neighbor>(gpsNeighbors.subList(0, 3));
 		}
 
 	}
 
 	/**
 	 * @param officeName
 	 *            name of the {@linkplain GPSOffice} to which the package is to
 	 *            be sent
 	 * @param trackingNumber
 	 *            tracking number of the package
 	 * @param x2
 	 *            x co-ordinate of the destination
 	 * @param y2
 	 *            y co-ordination of the destionation
 	 * @param officeListener
 	 *            {@linkplain GPSOfficeEventListener} listener to be added to
 	 *            the next GPS Office
 	 * @throws RemoteException
 	 *             exception thrown when the next GPS Office is not found during
 	 *             lookup
 	 */
 	@Override
 	public void forwardPackage(String officeName, final long trackingNumber,
 			final double x2, final double y2,
 			final RemoteEventListener<GPSOfficeEvent> officeListener)
 			throws RemoteException {
 
 		final GPSOffice currentOffice = this;
 
 		try {
 
 			GPSOfficeRef office = (GPSOfficeRef) registry.lookup(officeName);
 			if (office != null) {
 				eventGenerator.reportEvent(new GPSOfficeEvent(currentOffice
 						.getGPSOfficeName(), trackingNumber, x2, y2, 2));
 				office.examinePackage(trackingNumber, x2, y2, officeListener);
 			} else {
 				eventGenerator.reportEvent(new GPSOfficeEvent(currentOffice
 						.getGPSOfficeName(), trackingNumber, x2, y2, 3));
 			}
 
 		} catch (RemoteException e) {
			System.out.println("here");
			System.out.println(officeName);
			// e.printStackTrace();
			eventGenerator.reportEvent(new GPSOfficeEvent(officeName,
					trackingNumber, x2, y2, 3));
 		} catch (Exception e) {
			// e.printStackTrace();
			eventGenerator.reportEvent(new GPSOfficeEvent(officeName,
					trackingNumber, x2, y2, 3));
 		}
 
 	}
 
 	/**
 	 * Returns the name of the <TT>this</TT> office
 	 * 
 	 * @return name of the {@linkplain GPSOffice}
 	 * @throws RemoteException
 	 *             exception thrown in the Remote object is not available
 	 */
 	@Override
 	public String getGPSOfficeName() throws RemoteException {
 		return name;
 	}
 
 	/**
 	 * Returns the x and y (double) co-ordinates of <tt>this</tt> office
 	 * 
 	 * @return a double array with two elements
 	 * @throws RemoteException
 	 *             exception thrown in the Remote object is not available
 	 */
 	@Override
 	public double[] getGPSOfficeCoordinates() throws RemoteException {
 		return new double[] { x, y };
 	}
 
 	/**
 	 * Returns the <TT>List</TT> of {@linkplain Neighbor}s of <TT>this</TT>
 	 * office
 	 * 
 	 * @return List of {@linkplain Neighbor}s
 	 * @throws RemoteException
 	 *             exception thrown in the Remote object is not available
 	 */
 	@Override
 	public List<Neighbor> getNeighbors() throws RemoteException {
 		return neighbors;
 	}
 
 	/**
 	 * Sets the neighbors of <tt>this</tt> office
 	 * 
 	 * @param offices
 	 *            <tt>List</tt> of neighbors
 	 * @throws RemoteException
 	 *             exception thrown in the Remote object is not available
 	 */
 	@Override
 	public void setNeighbors(List<Neighbor> offices) throws RemoteException {
 		neighbors = offices;
 	}
 
 	/**
 	 * Add listener to the office
 	 * 
 	 * @param listener
 	 *            {@linkplain GPSOfficeEventListener}
 	 * @return Lease of the Listener
 	 * @throws RemoteException
 	 *             exception thrown in the Remote object is not available
 	 */
 	public Lease addListener(RemoteEventListener<GPSOfficeEvent> listener)
 			throws RemoteException {
 		return eventGenerator.addListener(listener);
 	}
 
 	/**
 	 * Add listener to the office along with the filter
 	 * 
 	 * @param listener
 	 *            {@linkplain GPSOfficeEventListener}
 	 * @param filter
 	 *            {@linkplain GPSOfficeEventFilter}
 	 * @return Lease of the listener
 	 * @throws RemoteException
 	 *             exception thrown in the Remote object is not available
 	 */
 	@Override
 	public Lease addListener(RemoteEventListener<GPSOfficeEvent> listener,
 			RemoteEventFilter<GPSOfficeEvent> filter) throws RemoteException {
 		return eventGenerator.addListener(listener, filter);
 	}
 
 	@Override
 	public long checkPackage(final double x2, final double y2,
 			final RemoteEventListener<GPSOfficeEvent> officeListener)
 			throws RemoteException, NotBoundException, InterruptedException {
 
 		final long trackingNumber = System.currentTimeMillis();
 
 		final String officeName = this.getGPSOfficeName();
 		reaper.schedule(new Runnable() {
 			public void run() {
 				try {
 					examinePackage(trackingNumber, x2, y2, officeListener);
 				} catch (RemoteException e) {
 					// e.printStackTrace();
 					eventGenerator.reportEvent(new GPSOfficeEvent(officeName,
 							trackingNumber, x2, y2, 3));
 				} catch (InterruptedException e) {
					// e.printStackTrace();
 					eventGenerator.reportEvent(new GPSOfficeEvent(officeName,
 							trackingNumber, x2, y2, 3));
 				}
 			}
 
 		}, 0l, TimeUnit.SECONDS);
 
 		return trackingNumber;
 	}
 
 	/**
 	 * Examines the package (with a 3 seconds delay), generates neighbors and
 	 * finds out where the package should be sent next.
 	 * 
 	 * @param trackingNumber
 	 *            tracking number of the package
 	 * @param x2
 	 *            x co-ordinate of the destination
 	 * @param y2
 	 *            y co-ordinate of the destionation
 	 * @param officeListener
 	 *            listener of the office
 	 * @return void
 	 * @throws RemoteException
 	 *             exception thrown in the Remote object is not available
	 * @throws InterruptedException
 	 */
 	@Override
 	public void examinePackage(long trackingNumber, final double x2,
 			final double y2, RemoteEventListener<GPSOfficeEvent> officeListener)
 			throws RemoteException, InterruptedException {
 
 		GPSOfficeEventFilter filter = new GPSOfficeEventFilter(trackingNumber);
 		addListener(officeListener, filter);
 		eventGenerator.reportEvent(new GPSOfficeEvent(this.getGPSOfficeName(),
 				trackingNumber, x2, y2, 1));
 
 		try {
 			Thread.sleep(3000);
 		} catch (InterruptedException e1) {
			// e1.printStackTrace();
 			eventGenerator.reportEvent(new GPSOfficeEvent(this
 					.getGPSOfficeName(), trackingNumber, x2, y2, 3));
 			throw e1;
 		}
 
 		GPSOfficeRef office = null;
 		try {
 
 			generateNeighbors(trackingNumber, x2, y2);
 			// printNeighbors(this);
 
 			double destDist = getDistance(x, y, x2, y2);
 			// System.out.println("dest dist: " + destDist);
 
 			double[] neigh = new double[] { Double.MAX_VALUE, Double.MAX_VALUE,
 					Double.MAX_VALUE };
 
 			if (neighbors != null) {
 				for (int i = 0; i < neighbors.size(); i++) {
 					neigh[i] = getDistance(neighbors.get(i).getGpsOffice()
 							.getGPSOfficeCoordinates()[0], neighbors.get(i)
 							.getGpsOffice().getGPSOfficeCoordinates()[1], x2,
 							y2);
 				}
 			}
 
 			if (destDist <= neigh[0] && destDist <= neigh[1]
 					&& destDist <= neigh[2]) {
 				// System.out.println("direct");
 				eventGenerator.reportEvent(new GPSOfficeEvent(this
 						.getGPSOfficeName(), trackingNumber, x2, y2, 4));
 			} else {
 
 				// System.out.println("neigh");
 				if (neigh[0] < neigh[1]) {
 					if (neigh[0] < neigh[2]) {
 						// System.out.println("neigh 1.");
 						office = (GPSOfficeRef) registry.lookup(neighbors
 								.get(0).getGpsOffice().getGPSOfficeName());
 
 					} else {
 						// System.out.println("neigh 3.");
 						office = (GPSOfficeRef) registry.lookup(neighbors
 								.get(2).getGpsOffice().getGPSOfficeName());
 					}
 				} else {
 					if (neigh[1] < neigh[2]) {
 						// System.out.println("neigh 2");
 						office = (GPSOfficeRef) registry.lookup(neighbors
 								.get(1).getGpsOffice().getGPSOfficeName());
 					} else {
 						// System.out.println("neigh 3");
 						office = (GPSOfficeRef) registry.lookup(neighbors
 								.get(2).getGpsOffice().getGPSOfficeName());
 					}
 				}
 				forwardPackage(office.getGPSOfficeName(), trackingNumber, x2,
 						y2, officeListener);
 
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			eventGenerator.reportEvent(new GPSOfficeEvent(this
 					.getGPSOfficeName(), trackingNumber, x2, y2, 3));
 		}
 
 	}
 
 	/**
 	 * Calculates the distance between two GPS offices, given their X and Y
 	 * co-ordinates
 	 * 
 	 * @param tempX1
 	 *            X co-ordinate of the first GPS Office
 	 * @param tempY1
 	 *            Y co-ordinate of the first Office
 	 * @param tempX2
 	 *            X co-ordinate of the second GPS Office
 	 * @param tempY2
 	 *            Y co-ordinate of the second GPS Office
 	 * @return distance between the two Offices, given their X and Y
 	 *         co-ordinates
 	 * @throws RemoteException
 	 *             exception thrown in the Remote object is not available
 	 */
 	@Override
 	public double getDistance(double tempX1, double tempY1, double tempX2,
 			double tempY2) {
 
 		return Math.sqrt(Math.pow((tempX1 - tempX2), 2)
 				+ Math.pow((tempY1 - tempY2), 2));
 	}
 
 }
