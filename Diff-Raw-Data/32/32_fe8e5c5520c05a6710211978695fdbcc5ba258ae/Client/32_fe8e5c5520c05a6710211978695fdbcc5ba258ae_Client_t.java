 package client;
 
 import java.rmi.NotBoundException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
import rental.Agency;
 import rental.CarType;
 import rental.Company;
 import rental.Quote;
 import rental.Reservation;
 import rental.ReservationConstraints;
 
 public class Client extends AbstractScriptedSimpleTest {
 
 	private Company company;
 
 	/********
 	 * MAIN *
 	 ********/
 
 	public static void main(String[] args) throws Exception {
 		// TODO Use an RMISecurityManager for the next session
 		System.setSecurityManager(null);
 
 		// Script file
 		String scriptFile = (args.length < 0) ? "trips" : args[0];
 		// Registry host
 		String host = (args.length < 1) ? null : args[1];
 		Registry registry = LocateRegistry.getRegistry(host);
 
 		// Lookup agency
		Agency agency = null;
 		try {
			agency = (Agency) registry.lookup(Agency.class.getSimpleName());
 		} catch (NotBoundException e1) {
 			System.err.println("Agency not bound");
 			System.exit(-1);
 		}
 
 		// Run client
 		try {
 			Client client = new Client(scriptFile, agency);
 			client.run();
 		} catch (Exception e) {
 			System.err.println("Client exception: " + e.toString());
 			e.printStackTrace();
 		}
 	}
 
 	/***************
 	 * CONSTRUCTOR *
 	 ***************/
 
 	public Client(String scriptFile, Company company) {
 		super(scriptFile);
 		this.company = company;
 	}
 
 	/**
 	 * Check which car types are available in the given period and print this
 	 * list of car types.
 	 * 
 	 * @param start
 	 *            start time of the period
 	 * @param end
 	 *            end time of the period
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	@Override
 	protected void checkForAvailableCarTypes(Date start, Date end)
 			throws Exception {
 		Set<CarType> carTypes = company.getAvailableCarTypes(start, end);
 		System.out.println("Available car types between " + start + " and "
 				+ end + ":");
 		for (CarType carType : carTypes) {
 			System.out.println("* " + carType);
 		}
 	}
 
 	/**
 	 * Retrieve a quote for a given car type (tentative reservation).
 	 * 
 	 * @param clientName
 	 *            name of the client
 	 * @param start
 	 *            start time for the quote
 	 * @param end
 	 *            end time for the quote
 	 * @param carType
 	 *            type of car to be reserved
 	 * @return the newly created quote
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	@Override
 	protected Quote createQuote(String clientName, Date start, Date end,
 			String carType) throws Exception {
 		ReservationConstraints constraints = new ReservationConstraints(start,
 				end, carType);
 		return company.createQuote(constraints, clientName);
 	}
 
 	/**
 	 * Confirm the given quote to receive a final reservation of a car.
 	 * 
 	 * @param quote
 	 *            the quote to be confirmed
 	 * @return the final reservation of a car
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	@Override
 	protected Reservation confirmQuote(Quote quote) throws Exception {
 		return company.confirmQuote(quote);
 	}
 
 	/**
 	 * Get all reservations made by the given client.
 	 * 
 	 * @param clientName
 	 *            name of the client
 	 * @return the list of reservations of the given client
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	@Override
 	protected List<Reservation> getReservationsBy(String clientName)
 			throws Exception {
 		return company.getReservationsBy(clientName);
 	}
 
 	/**
 	 * Get the number of reservations for a particular car type.
 	 * 
 	 * @param carType
 	 *            name of the car type
 	 * @return number of reservations for the given car type
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	@Override
 	protected int getNumberOfReservationsForCarType(String carType)
 			throws Exception {
 		return company.getNumberOfReservationsForCarType(carType);
 	}
 }
