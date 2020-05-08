 package GatewayMapper;
 
 import DBConnection.ConnectionTools;
 import Resources.Order;
 import Resources.Product;
 import Resources.Truck;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  *
  * @author Adrian & Kris
  */
 public class OrderGateway {
 
    ArrayList<Truck> trucks = new ArrayList();
    ArrayList<Order> orders = new ArrayList();
     private Order currentOrder;
 
     public OrderGateway() {     //for a constructer?? Is this neccessary?
     }
     
     /*
      * Constructs an "empty" order with the unique order indentifier.
      */
     public void currentOrder() {
         int ID = getUniqueOrderID();
         currentOrder = new Order(0, 0, 0, 0, 0, ID);
     }
     
     /*
      * Methods for adding/removing products to/from the order.
      * Not using DB connection.
      */
     public boolean addItemToList(Product prod) {
         return currentOrder.addItemToList(prod);
     }
 
     public void removeFromOrder(Product prod) {
         currentOrder.removeFromOrder(prod);
     }
     
     /*
      * US 3.1
      * User can add item(s) to customer
      * order table from product table.
      */
     public boolean addToCustomerOrderTable(int customerID, String startDate, String finishDate) {
         boolean success = false;
         Connection con = ConnectionTools.getInstance().getCurrentConnection();
         String SQLString1 = "INSERT into customer_order "
                           + "VALUES (?,?,?,?)";
 
         PreparedStatement statement = null;
         try {
             statement = con.prepareStatement(SQLString1);
             statement.setInt(1, currentOrder.getOrderID());
             statement.setInt(2, customerID);
             statement.setDate(3, Date.valueOf(startDate));
             statement.setDate(4, Date.valueOf(finishDate));
             statement.executeUpdate();
             success = true;
         } catch (Exception e) {
             System.out.println("Insertion error!");
             System.out.println(e.getMessage());
         } finally {
             try {
                 statement.close();
             } catch (SQLException e) {
                 System.out.println("Statement close error!");
                 System.out.println(e.getMessage());
             }
         }
         addOrderToDB();
         return success;
     }
 
     public boolean addOrderToDB() {
         boolean success = false;
         Connection con = ConnectionTools.getInstance().getCurrentConnection();
         String SQLString1 = "INSERT into OrderTable "
                 + "VALUES (?,?,?)";
         PreparedStatement statement = null;
         for (int i = 0; i < currentOrder.getListInstance().size(); i++) {
             try {
                 statement = con.prepareStatement(SQLString1);
                 statement.setInt(1, currentOrder.getOrderID());
                 statement.setInt(2, currentOrder.getListInstance().get(i).getProductID());
                 statement.setInt(3, currentOrder.getListInstance().get(i).getQuantity());
                 statement.executeUpdate();
                 success = true;
             } catch (Exception e) {
                 System.out.println("Insertion error!");
                 System.out.println(e.getMessage());
 
             } finally {
                 try {
                     statement.close();
                 } catch (SQLException e) {
                     System.out.println("Statement close error!");
                     System.out.println(e.getMessage());
                 }
             }
         }
         return success;
     }
     
     /*
      * US 3.2
      * User can see number of trucks
      * needed for delivery from size of
      * products in customer order table,
      */
     public int getTrucksRequired(int totalVolume){
         getTrucks();
         int amountOfTrucks = 0;
         int allTrucksCapacity = 0;
         for (int i = 0; i < trucks.size(); i++) {
             allTrucksCapacity =+ trucks.get(i).getTruckCapacity();
         }
         try{
             if (totalVolume < allTrucksCapacity){
                 for (int i = 0; i < trucks.size(); i++) {
                     while (totalVolume > 0){
                         totalVolume =- trucks.get(i).getTruckCapacity();
                         amountOfTrucks++;
                     }
                 }
             }else{
                 System.out.println("problem in 'getTrucksRequired(int)'");
             }
         }
         catch(Exception e){
             System.out.println("Something went wrong trucks required method.");
         }
         return amountOfTrucks;
     }
     /*Fills the trucks arraylist from the DB. */
     public boolean getTrucks(){
         boolean success = false;
         Connection con = ConnectionTools.getInstance().getCurrentConnection();
         String SQLString1 = "SELECT *"
                             + "FROM trucks ";
         PreparedStatement statement = null;
         try {
             statement = con.prepareStatement(SQLString1);
             ResultSet rs = statement.executeQuery();
             while (rs.next()) {
                 Truck truck = new Truck();
                 int truckID=rs.getInt(1);
                 String model=rs.getString(2);
                 int capacity=rs.getInt(3);
                 truck.setTruckID(truckID);
                 truck.setTruckName(model);
                 truck.setTruckCapacity(capacity);
                 trucks.add(truck);
             }
             success = true;
         } catch (Exception e) {
             System.out.println("Retrieval error!");
             System.out.println(e.getMessage());
             success = false;
         } finally {
             try {
                 statement.close();
             } catch (SQLException e) {
                 System.out.println("Statement close error!");
                 System.out.println(e.getMessage());
             }
         }
         return success;
     }
     /*
      * US 3.4
      * User can book delivery truck(s)
      * for customer order (if available).
      * Using the trucks arraylist. No DB
      * connection.
      */
     public boolean bookTrucks(int trucksForOrder){
         boolean enoughTrucks = false;
         if (trucksForOrder < trucks.size()){
             enoughTrucks = true;
         }
         return enoughTrucks;        
     }
     /* US 3.4 Continued with DB connection. */
     public boolean commitTruckOrder(){
         boolean success = false;
         
         return success;
     }
     /*
      * Fetches a unique identifier for each order.
      */
     public int getUniqueOrderID() {
         int temp = 0;
         Connection con = ConnectionTools.getInstance().getCurrentConnection();
         String SQLString1 = "SELECT orderseq.nextval "
                 + "FROM dual";
         PreparedStatement statement = null;
         try {
             statement = con.prepareStatement(SQLString1);
             ResultSet rs = statement.executeQuery();
             if (rs.next()) {
                 temp = rs.getInt(1);
             }
 
         } catch (Exception e) {
             System.out.println("Something wrong build" + e.getMessage());
 
         } finally {
             try {
                 statement.close();
             } catch (SQLException e) {
                 System.out.println("Statment close error\n" + e.getMessage());
             }
         }
 
         return temp;
 
     }
 }// END
