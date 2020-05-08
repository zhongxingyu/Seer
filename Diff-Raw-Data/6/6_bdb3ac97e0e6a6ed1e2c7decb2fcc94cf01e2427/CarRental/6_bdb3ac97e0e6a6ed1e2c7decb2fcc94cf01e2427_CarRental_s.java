 package carrental;
 
 import java.util.ArrayList;
 
 /**
  * CarRental acts as the main controller of the CarRental project. It creates
  * instances of the view-controller and model-controller and communicates
  * between these.
  * @author CNN
  * @version 2011-12-02
  */
 public final class CarRental {
     public static final boolean DEBUG = true;
     private static CarRental instance;
     private Model model;
     private static View view;
     private String log;
     
     private CarRental() {
         //next line is ugly
         if(instance == null) instance = this;
         appendLog("Initializing...");
         model = new Model();
         view = new View();
     }
     
     /**
      * Get an instance of the CarRental controller. Useful for using the
      * controller's appendLog-feature.
      * @return instance of controller
      */
     public static CarRental getInstance() {
         //should never happen that there is no instance, but just in case
         if(instance == null) return new CarRental();
         return instance;
     }
     
     /**
      * Get an instance of the View main panel. Useful for changing the panel to
      * be shown.
      * @return instance of view
      */
     public static View getView() {
         if(view == null) view = new View();
         return view;
     }
     
     /**
      * Request list of vehicles from the model
      * @return list of vehicles
      */
     public ArrayList<Vehicle> requestVehicles() {
         return model.getVehicles();
     }
     
     /**
      * Request empty vehicle from the model
      * @return empty vehicle
      */
     public Vehicle requestVehicle() {
         return model.getVehicle();
     }
     
     /**
      * Request vehicle matching id from model
      * @param id id of vehicle
      * @return vehicle
      */
     public Vehicle requestVehicle(int id) {
         if(id < 1) return model.getVehicle();
         return model.getVehicle(id);
     }
     
     /**
      * Get an id for a new vehicle
      * @return id
      */
     public int requestNewVehicleId() {
         return (model.getHighestVehicleId() + 1);
     }
     
     /**
      * Request list of vehicle types from model
      * @return list of vehicle types
      */
     public ArrayList<VehicleType> requestVehicleTypes() {
         return model.getVehicleTypes();
     }
     
     /**
      * Request an empty vehicle type object
      * @return empty vehicle type
      */
     public VehicleType requestVehicleType() {
         return model.getVehicleType();
     }
     
     /**
      * Request a vehicle type matching the provided id
      * @param id id of vehicle type
      * @return vehicle type
      */
     public VehicleType requestVehicleType(int id) {
         if(id < 1) return model.getVehicleType();
         return model.getVehicleType(id);
     }
     
     /**
      * Request a vehicle type matching the provided name.
      * @param name name of vehicle type
      * @return vehicle type
      */
     public VehicleType requestVehicleType(String name) {
         return model.getVehicleType(name);
     }
     
     /**
      * Request an id for a new vehicle type
      * @return id
      */
     public int requestNewVehicleTypeId() {
         return (model.getHighestVehicleTypeId() + 1);
     }
     
     /**
      * Request a list of maintenances from model
      * @return list of maintenances
      */
     public ArrayList<Maintenance> requestMaintenances() {
         return model.getMaintenances();
     }
     
     /**
      * Request a single, empty maintenance from model
      * @return maintenance
      */
     public Maintenance requestMaintenance() {
         return model.getMaintenance();
     }
     
     /**
      * Request a maintenance from the model, matching the id
      * @param id id of maintenance
      * @return maintenance
      */
     public Maintenance requestMaintenance(int id) {
         if(id < 1) return model.getMaintenance();
         return model.getMaintenance(id);
     }
     
     /**
      * Request an id for a new maintenance.
      * @return id
      */
     public int requestNewMaintenanceId() {
         return (model.getHighestMaintenanceId() + 1);
     }
     
     /**
      * Request a list of maintenance types from the model
      * @return list of maintenance types
      */
     public ArrayList<MaintenanceType> requestMaintenanceTypes() {
         return model.getMaintenanceTypes();
     }
     
     /**
      * Request a single, empty maintenance type from the model
      * @return maintenance type
      */
     public MaintenanceType requestMaintenanceType() {
         return model.getMaintenanceType();
     }
     
     /**
      * Request a maintenance type matching the provided id
      * @param id id of maintenance type
      * @return maintenance type
      */
     public MaintenanceType requestMaintenanceType(int id) {
         return model.getMaintenanceType(id);
     }
     
     /**
      * Request a new id for a maintenance type
      * @return id
      */
     public int requestNewMaintenanceTypeId() {
         return (model.getHighestMaintenanceTypeId() + 1);
     }
     
     /**
      * Request a list of customers from the model
      * @return list of customers
      */
     public ArrayList<Customer> requestCustomers() {
         return model.getCustomers();
     }
     
     /**
      * Request a single, empty customer from the model
      * @return customer
      */
     public Customer requestCustomer() {
         return model.getCustomer();
     }
     
     /**
      * Request a customer matching the id provided
      * @param id id of customer
      * @return customer
      */
     public Customer requestCustomer(int id) {
         if(id < 1) return model.getCustomer();
         return model.getCustomer(id);
     }
     
     /**
      * Request the first customer matching the phone number provided
      * @param phone Phone number of customer
      * @return customer
      */
     public Customer requestCustomerByPhone(int phone) {
         return model.getCustomerByPhone(phone);
     }
     
     /**
      * Request a new id for a customer
      * @return id
      */
     public int requestNewCustomerId() {
         return (model.getHighestCustomerId() + 1);
     }
     
     /**
      * Request a list of reservations from the model
      * @return list of reservations
      */
     public ArrayList<Reservation> requestReservations() {
         return model.getReservations();
     }
     
     /**
      * Request a list of reservations matching the given customer id
      * @param c_id id of customer, the reservations should belong to
      * @return reservations
      */
     public ArrayList<Reservation> requestReservationsByCustomer(int c_id) {
         return model.getReservationsByCustomerId(c_id);
     }
     
     /**
      * Request an empty reservation from the model
      * @return reservation
      */
     public Reservation requestReservation() {
         return model.getReservation();
     }
     
     /**
      * Request the reservation matching the provided id from the model
      * @param id id of reservation
      * @return reservation
      */
     public Reservation requestReservation(int id) {
         if(id < 1) return model.getReservation();
         return model.getReservation(id);
     }
     
     /**
      * Request a new reservation id
      * @return id
      */
     public int requestNewReservationId() {
         return (model.getHighestReservationId() + 1);
     }
     
     /**
      * Request a list of bookings from the model. Bookings are either
      * reservations or maintenances.
      * @return list of bookings
      */
     public ArrayList<Booking> requestBookings() {
         return model.getBookings();
     }
     
     /**
      * Request a list of bookings from the model related to the vehicle with the
      * specified id. Bookings are either reservations or maintenances.
      * @return list of bookings
      */
     public ArrayList<Booking> requestBookingsByVehicle(int v_id) {
         return model.getBookingsByVehicleId(v_id);
     }
     
     /**
      * Save a vehicle to the model
      * @param v Vehicle
      */
     public void saveVehicle(Vehicle v) {
         model.saveVehicle(v);
     }
     
     /**
      * Save a vehicletype to the model
      * @param vt VehicleType
      */
     public void saveVehicleType(VehicleType vt) {
         model.saveVehicleType(vt);
     }
     
     /**
      * Save a maintenance to the model
      * @param m Maintenance
      */
     public void saveMaintenance(Maintenance m) {
         model.saveMaintenance(m);
     }
     
     /**
      * Save a maintenance type to the model
      * @param mt maintenance type
      */
     public void saveMaintenanceType(MaintenanceType mt) {
         model.saveMaintenanceType(mt);
     }
     
     /**
      * Save a customer to the model
      * @param c customer
      */
     public void saveCustomer(Customer c) {
         model.saveCustomer(c);
     }
     
     /**
      * Save a reservation to the model
      * @param r reservation
      */
     public void saveReservation(Reservation r) {
         model.saveReservation(r);
     }
     
     /**
      * Delete a vehicle from the model
      * @param id id of vehicle
      */
     public void deleteVehicle(int id) {
         model.deleteVehicle(id);
     }
     
     /**
      * Delete a vehicletype from the model
      * @param id id of vehicle type
      */
     public void deleteVehicleType(int id) {
         model.deleteVehicleType(id);
     }
     
     /**
      * Delete a maintenance from the model
      * @param id id of maintenance
      */
     public void deleteMaintenance(int id) {
         model.deleteMaintenance(id);
     }
     
     /**
      * Delete a maintenance type from the model
      * @param id id of maintenance type
      */
     public void deleteMaintenanceType(int id) {
         model.deleteMaintenanceType(id);
     }
     
     /**
      * Delete a customer from the model
      * @param id id of customer
      */
     public void deleteCustomer(int id) {
         model.deleteCustomer(id);
     }
     
     /**
      * Delete a reservation from the model
      * @param id id of reservation
      */
     public void deleteReservation(int id) {
         model.deleteReservation(id);
     }
     
     /**
      * Append a string as a new line to the log. If something goes wrong or
      * is of interest as to determine when something goes wrong, it should be
      * added to the log. Also displays the string supplied to the user.
      * @param string New line to add to the log.
      */
     public void appendLog(String string) {
         if(CarRental.DEBUG) System.out.println(string);
         log = log+"\n"+string;
        getView().displayError(string);
     }
     
     /**
      * Append a string as a new line to the log. If something goes wrong or
      * is of interest as to determine when something goes wrong, it should be
      * added to the log.
      * Additionally takes an exception e, that will be printed before the string
      * and hence there is no reason to specify exception type in the string.
      * @param string New line to add to the log.
      * @param e Exception of add as a prefix to the string.
      */
     public void appendLog(String string, Exception e) {
         if(CarRental.DEBUG) System.out.println(e+": "+string);
         log = log+"\n"+string+": "+e;
     }
     
     /**
      * Prints out the entire log to the console.
      */
     private void printLog() {
         System.out.println(log);
     }
      
     public static void main(String[] args) {
         CarRental controller = new CarRental();
     }
     
 }
