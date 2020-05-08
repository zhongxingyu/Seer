 package streamfish;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author HJ
  */
 public class StreamFish {
 
 	/**
 	 * @param args the command line arguments
 	 */
 	private String databasedriver;
 	private Connection con;
 
 	public StreamFish() {
 		try {
 			databasedriver = "org.apache.derby.jdbc.ClientDriver";
 			Class.forName(databasedriver);  // laster inn driverklassen
 			String databasenavn = "jdbc:derby://db.stud.aitel.hist.no/streamfish;user=sfdb;password=XEnhdPy8";
 			con = DriverManager.getConnection(databasenavn);
 		} catch (SQLException ex) {
 			Logger.getLogger(StreamFish.class.getName()).log(Level.SEVERE, null, ex);
 		} catch (ClassNotFoundException ex) {
 			Logger.getLogger(StreamFish.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	public Customer getCustomer(int i) {
 		Statement stm;
 		ResultSet res;
 		Customer customer;
 
 		try {
 			stm = con.createStatement();
 
 			res = stm.executeQuery("select * from customer where customer_id = " + i);
 
 			while (res.next()) {
 				int customer_id = res.getInt("customer_id");
 				String customer_name = res.getString("customer_name");
 				String phone = res.getString("phone");
 				byte business = res.getByte("business");
 				boolean busi;
 				if (business == 0) {
 					busi = false;
 				} else {
 					busi = true;
 				}
 				customer = new Customer(customer_id, customer_name, phone, busi);
 				return customer;
 			}
 
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public Menu[] getMenus() {
 
 		Statement stm;
 		ResultSet res;
 		Menu[] menus;
 		int teller = 0;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from menu where menu.status = 1");
 			res.next();
 			int ant = res.getInt("antall");
 			menus = new Menu[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from menu where menu.status = 1");
 
 			while (res.next()) {
 				int menuId = res.getInt("menu_id");
 				String menuName = res.getString("menu_name");
 				int price = res.getInt("price");
 				String description = res.getString("description");
 				boolean busi = false;
 				menus[teller] = new Menu(menuId, menuName, price, description);
 				teller++;
 			}
 			return menus;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public Ingredient[] getIngredients(String name) {
 
 		Statement stm;
 		ResultSet res;
 		Ingredient[] ingredients;
 		String[] check = {name};
 		check = removeUnwantedSymbols(check);
 		int teller = 0;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from ingredients where (upper(ingredient_name) like '"
 					+ check[0].toUpperCase() + "%')");
 			res.next();
 			int ant = res.getInt("antall");
 			ingredients = new Ingredient[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from ingredients where (upper(ingredient_name) like '"
 					+ check[0].toUpperCase() + "%')");
 
 			while (res.next()) {
 				int ingredientID = res.getInt("ingredient_ID");
 				String ingredientName = res.getString("ingredient_name");
 				int amount = res.getInt("amount");
 				String expiryDate = res.getString("expiry_date");
 				ingredients[teller] = new Ingredient(ingredientID, ingredientName, amount, expiryDate);
 				teller++;
 			}
 			return ingredients;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public CustomerAddress[] getAddress(int custid) {
 
 		Statement stm;
 		ResultSet res;
 		CustomerAddress[] address;
 		int teller = 0;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from customer_address where customer_id = " + custid);
 			res.next();
 			int ant = res.getInt("antall");
 			address = new CustomerAddress[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from customer_address where customer_id = " + custid);
 
 			while (res.next()) {
 				String address1 = res.getString("address");
 				String zipCode = res.getString("zip_code");
 				String city = res.getString("city");
 				address[teller] = new CustomerAddress(address1, zipCode, city, custid);
 				teller++;
 			}
 			return address;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public Order[] getOrders() {
 
 		Statement stm;
 		ResultSet res;
 		Order[] orders;
 		int teller = 0;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from orders where orders.status = 1");
 			res.next();
 			int ant = res.getInt("antall");
 			orders = new Order[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from orders where orders.status = 1");
 
 			while (res.next()) {
 				int orderID = res.getInt("order_id");
 				int menuID = res.getInt("menu_id");
 				int customerID = res.getInt("customer_id");
 				int emplID = res.getInt("empl_id");
 				int nrPersons = res.getInt("nr_persons");
 				String deliveryDate = res.getString("delivery_date");
 				String deliveryTime = res.getString("deliver_time");
 				String address = res.getString("address");
 				orders[teller] = new Order(orderID, menuID, customerID, emplID, nrPersons, deliveryDate, deliveryTime, address);
 				teller++;
 			}
 			return orders;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public Order[] getOrderCustomer(Customer customer) {
 		Statement stm;
 		ResultSet res;
 		Order[] orders;
 		int counter = 0;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("SELECT COUNT (*) ordercustomer FROM ORDERS JOIN CUSTOMER ON orders.customer_id = customer.customer_id "
 					+ "WHERE orders.CUSTOMER_ID = " + customer.getCustomerID() + " AND orders.status = 1 AND customer.status = 1");
 			res.next();
 			orders = new Order[res.getInt("ordercustomer")];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("SELECT * FROM ORDERS JOIN CUSTOMER ON orders.customer_id = customer.customer_id "
 					+ "WHERE orders.CUSTOMER_ID = " + customer.getCustomerID() + " AND orders.status = 1 AND customer.status = 1");
 			while (res.next()) {
 				int order_id = res.getInt("ORDER_ID");
 				int menu_id = res.getInt("MENU_ID");
 				int customer_id = res.getInt("CUSTOMER_ID");
 				int empl_id = res.getInt("EMPL_ID");
 				int num_persons = res.getInt("NR_PERSONS");
 				String date = res.getString("DELIVERY_DATE");
 				String time = res.getString("DELIVERY_TIME");
 				String address = res.getString("ADDRESS");
 
 				orders[counter] = new Order(order_id, menu_id, customer_id, empl_id, num_persons, date, time, address);
 				counter++;
 			}
 			return orders;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public Customer[] getCustomers(String s, boolean status) {
 		Statement stm;
 		ResultSet res;
 		Customer[] customers;
 		String[] check = {s};
 		check = removeUnwantedSymbols(check);
 		int teller = 0;
                 short aktiv = 1;
                 if (status) {
                     aktiv = 0;
                 }
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from customer where (upper(customer_name) like '"
 					+ check[0].toUpperCase() + "%' or phone like '" + check[0] + "%') and customer.status = " + aktiv);
 			res.next();
 			int ant = res.getInt("antall");
 			customers = new Customer[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from customer where (upper(customer_name) like '"
 					+ check[0].toUpperCase() + "%' or phone like '" + check[0] + "%') and customer.status = " + aktiv);
 
 			while (res.next()) {
 				int customerId = res.getInt("customer_id");
 				String customerName = res.getString("customer_name");
 				String phone = res.getString("phone");
 				int business = Integer.parseInt(res.getString("business"));
 				boolean busi = false;
 				if (business == 1) {
 					busi = true;
 				}
 				customers[teller] = new Customer(customerId, customerName, phone, busi);
 				teller++;
 			}
 			return customers;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		} catch (NumberFormatException e2) {
 		}
 		return null;
 	}
 
 	public Employee userAuthorization(String username, String password) {
 		for (Employee emp : getEmployees()) {
 			if (emp.login(username, password)) {
 				return emp;
 			}
 		}
 		return null;
 	}
 
 	public Employee[] getEmployees() {
 		Statement stm;
 		ResultSet res;
 		Employee[] employees;
 		int teller = 0;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from employees where employees.status = 1");
 			res.next();
 			int ant = res.getInt("antall");
 			employees = new Employee[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select empl_id, user_type, username, password from employees where employees.status = 1");
 
 			while (res.next()) {
 				int emplID = res.getInt("empl_id");
 				byte userType = res.getByte("user_type");
 				String username = res.getString("username");
 				String password = res.getString("password");
 				employees[teller] = new Employee(emplID, userType, username, password);
 				teller++;
 			}
 			return employees;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public Orderinfo[] getTodaysTasks(String sok) {
 		Statement stm;
 		ResultSet res;
 		int count = 0;
 		Orderinfo[] info;
 
 		try {
 			stm = con.createStatement();
 
 			res = stm.executeQuery("select count(*) count from todaysTasks WHERE (UPPER(CUSTOMER_NAME) LIKE '" + sok.toUpperCase() +"%')");
 			res.next();
 			int ant = res.getInt("count");
 			info = new Orderinfo[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from todaysTasks WHERE (upper(CUSTOMER_NAME) LIKE '" + sok.toUpperCase() +"%')");
 
 			while (res.next()) {
 				int orderID = res.getInt(1);
 				String customerName = res.getString(2);
 				String address = res.getString(3);
 				String zipcode = res.getString(4);
 				String city = res.getString(5);
 				String phone = res.getString(6);
 				String menu = res.getString(7);
 				int numberOfPersons = res.getInt(8);
 				int price = res.getInt(9);
 				String salesperson = res.getString(10);
 				info[count] = new Orderinfo(orderID, customerName, address, zipcode,
 						city, phone, menu, numberOfPersons, price, salesperson);
 				count++;
 			}
 			Opprydder.lukkResSet(res);
 			return info;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		}
 		return null;
 	}
         
         public Orderinfo[] getTodaysTasks2(String sok) {
 		Statement stm;
 		ResultSet res;
 		int count = 0;
 		Orderinfo[] info;
 
 		try {
 			stm = con.createStatement();
 
 			res = stm.executeQuery("select count(*) count from todaysTasks WHERE (UPPER(MENU_NAME) LIKE '" + sok.toUpperCase() +"%')");
 			res.next();
 			int ant = res.getInt("count");
 			info = new Orderinfo[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from todaysTasks WHERE (upper(MENU_NAME) LIKE '" + sok.toUpperCase() +"%')");
 
 			while (res.next()) {
 				int orderID = res.getInt(1);
 				String customerName = res.getString(2);
 				String address = res.getString(3);
 				String zipcode = res.getString(4);
 				String city = res.getString(5);
 				String phone = res.getString(6);
 				String menu = res.getString(7);
 				int numberOfPersons = res.getInt(8);
 				int price = res.getInt(9);
 				String salesperson = res.getString(10);
 				info[count] = new Orderinfo(orderID, customerName, address, zipcode,
 						city, phone, menu, numberOfPersons, price, salesperson);
 				count++;
 			}
 			Opprydder.lukkResSet(res);
 			return info;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public Orderinfo[] getOrderinfo(int orderID) {
 		Statement stm;
 		ResultSet res;
 		int count = 0;
 		Orderinfo[] info;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("CREATE VIEW orderinfo AS SELECT orders.delivery_date, customer_name, address, "
 					+ "zip_code, city, phone, menu_name, nr_persons, price*nr_persons " + "Total" + ", username FROM customer\n"
 					+ "LEFT JOIN orders ON customer.CUSTOMER_ID = orders.CUSTOMER_ID\n"
 					+ "LEFT JOIN menu ON orders.MENU_ID = menu.MENU_ID\n"
 					+ "LEFT JOIN employees ON orders.EMPL_ID = employees.EMPL_ID\n"
 					+ "LEFT JOIN customer_address ON orders.address = customer_address.address AND orders.CUSTOMER_ID = customer_address.CUSTOMER_ID"
 					+ "WHERE orders.order_ID = '" + orderID + "' AND customer.status = 1 "
 					+ "AND orders.status = 1 ORDER BY delivery_date ASC;");
 			res.next();
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select count(*) count from orderinfo");
 			res.next();
 			int ant = res.getInt("count");
 			info = new Orderinfo[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from orderinfo");
 
 			while (res.next()) {
 				String date = res.getString(1);
 				String customerName = res.getString(2);
 				String address = res.getString(3);
 				String zipcode = res.getString(4);
 				String city = res.getString(5);
 				String phone = res.getString(6);
 				String menu = res.getString(7);
 				int numberOfPersons = res.getInt(8);
 				int price = res.getInt(9);
 				String salesperson = res.getString(10);
 				info[count] = new Orderinfo(orderID, customerName, address, zipcode, city, phone, menu, numberOfPersons, price, salesperson, date);
 				count++;
 			}
 			Opprydder.lukkResSet(res);
 			stm.executeQuery("drop view orderinfo");
 			return info;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public int addOrder(Order order) {
 		Statement stm;
 		try {
 			stm = con.createStatement();
 			String[] check = {order.getDeliveryDate(), order.getAddress().getAdress()};
  			check = removeUnwantedSymbols(check);
 			int succ;
 			if (order.getSubId() > 0) {
 				succ = stm.executeUpdate("insert into orders (DELIVERY_DATE, ADDRESS, NR_PERSONS, EMPL_ID, MENU_ID,CUSTOMER_ID,DELIVERY_TIME,SUBSCRIPTION_ID) "
 					+ "values('" + check[0] + "' , '" + check[1] + "', " + order.getNrPersons() + ", " + order.getEmplId() + ", "
 					+ order.getMenuId() + " , " + order.getCustomerId() + " , '" + order.getDeliveryTime() + "', " + order.getSubId() + ")");
 			} else {
 				succ = stm.executeUpdate("insert into orders (DELIVERY_DATE, ADDRESS, NR_PERSONS, EMPL_ID, MENU_ID,CUSTOMER_ID,DELIVERY_TIME) "
 						+ "values('" + check[0] + "' , '" + check[1] + "', " + order.getNrPersons() + ", " + order.getEmplId() + ", "
 						+ order.getMenuId() + " , " + order.getCustomerId() + " , '" + order.getDeliveryTime() + "')");
 			}
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public Subscription[] getSubscriptions(String sok){
 		Statement stm;
 		ResultSet res;
 		Subscription[] subscriptions;
 		String[] check = {sok};
 		check = removeUnwantedSymbols(check);
 		int teller = 0;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from sfdb.subscription "
 					+ "join sfdb.customer on subscription.customer_id = customer.customer_id "
 					+ "where (upper(customer_name) like '"
 					+ check[0].toUpperCase() + "%' or phone like '" + check[0] + "%')");
 			res.next();
 			int ant = res.getInt("antall");
 			subscriptions = new Subscription[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from sfdb.subscription "
 					+ "join sfdb.customer on subscription.customer_id = customer.customer_id "
 					+ "where (upper(customer_name) like '"
 					+ check[0].toUpperCase() + "%' or phone like '" + check[0] + "%')");
 
 			while (res.next()) {
 				int subscription_id = res.getInt("subscription_id");
 				int duration = res.getInt("duration");
 				String from_date = res.getString("from_date");
 				String to_date = res.getString("to_date");
 				String days = res.getString("days");
 				char status = res.getString("status").charAt(0);
 				
 				
 				subscriptions[teller] = new Subscription(subscription_id, duration, from_date, to_date, days, status);
 				teller++;
 			}
 			return subscriptions;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		} catch (NumberFormatException e2) {
 		}
 		
 		
 		return null;
 	}
 
 	public int registerSubscription(Subscription subscription, Order order) {
 		Statement stm;
 		ResultSet res;
 		int succ;
 		int subId = 0;
 
 		try {
 			stm = con.createStatement();
 			succ = stm.executeUpdate("INSERT INTO SUBSCRIPTION VALUES(DEFAULT, " 
 					+ subscription.getDuration() +", '" 
 					+ subscription.getFrom_date() + "', '" 
 					+ subscription.getTo_date() + "', '"
 					+ subscription.getDays() + "', '"
 					+ subscription.getStatus() + "', "
 					+ order.getCustomerId()+ ")" );
 			res = stm.executeQuery("select * from subscription order by subscription_ID desc");
 			res.next();
 			subId = res.getInt("subscription_id");
 			order.setSubId(subId);
 		} catch (SQLException ex) {
 			System.out.println(ex);
 		}
 		try {
 			stm = con.createStatement();
 
 			//     subscription.getDays().compareTo(TodaysDate.getDay());
 			int day = TodaysDate.getDay();
 			int duration = subscription.getDuration() * 4;
 			for (int i = 0; i < duration; i++) {
 				if (i == 0) {
 					for (int y = day; y < 7; y++) {
 						if (subscription.getDays().charAt(y) == '1') {
 							String date = TodaysDate.getADate(y - day);
 							order.setDate(date);
 							addOrder(order);
 							
 						}
 					}
 				} else if (i == (duration - 1)) {
 					for (int k = 0; k <= day; k++) {
 						if (subscription.getDays().charAt(k) == '1') {
 							String date = TodaysDate.getADate(i * 7 - day + k);
 							order.setDate(date);
 							addOrder(order);
 						}
 					}
 				} else {
 					for (int x = 0; x < 7; x++) {
 						if (subscription.getDays().charAt(x) == '1') {
 							String date = TodaysDate.getADate(i * 7 - day + x);
 							order.setDate(date);
 							addOrder(order);
 						}
 					}
 				}
 			}
 
 		} catch (SQLException exc) {
 			System.out.println(exc);
 		}
 		return -1;
 	}
         
         public int deleteSubscription(int subscription_id){
             Statement stm;
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("delete from subscription where subscription_id = " + subscription_id);
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
         }
         
         
 	public Dish[] getDishes(String name) {
 
 		Statement stm;
 		ResultSet res;
 		Dish[] dishes;
 		String[] check = {name};
 		check = removeUnwantedSymbols(check);
 		int teller = 0;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from dish where (upper(dish_name) like '"
 					+ check[0].toUpperCase() + "%')");
 			res.next();
 			int ant = res.getInt("antall");
 			dishes = new Dish[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select * from dish where (upper(dish_name) like '"
 					+ check[0].toUpperCase() + "%')");
 
 			while (res.next()) {
 				int dishID = res.getInt("DISH_ID");
 				String dishName = res.getString("dish_name");
 				dishes[teller] = new Dish(dishName, dishID);
 				teller++;
 			}
 			return dishes;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public int addDish(Dish dish) {
 		Statement stm;
 		try {
 			stm = con.createStatement();
 			String[] check = {dish.getName()};
 			check = removeUnwantedSymbols(check);
 			int succ = stm.executeUpdate("insert into dish (DISH_NAME, STATUS) "
 					+ "values('" + check[0] + "', 1)");
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int changeDishStatus(Dish dish) {
 		Statement stm;
 		ResultSet res;
 		int succ;
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select status from dish where dish_id = " + dish.getID());
 			res.next();
 			short status = res.getShort("status");
 			if (status == 1) {
 				succ = stm.executeUpdate("update dish set status =" + 0 + " where dish_id = " + dish.getID());
 			} else {
 				succ = stm.executeUpdate("update dish set status =" + 1 + " where dish_id = " + dish.getID());
 			}
 			Opprydder.lukkResSet(res);
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public String checkIngForDish(Dish dish) {
 		String result = "";
 		Statement stm;
 		ResultSet res;
 		int[] ingInStock;
 		int[] ingNeeded;
 		int[] ingDiff;
 		boolean inStock = true;
 
 		int teller = 0;
 
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from dish_ingredients where dish_id = " + dish.getID());
 			res.next();
 			int ant = res.getInt("antall");
 			ingNeeded = new int[ant];
 			ingInStock = new int[ant];
 			ingDiff = new int[ant];
 			Opprydder.lukkResSet(res);
 
 			res = stm.executeQuery("select ingredients.amount from ingredients,dish_ingredients where ingredients.ingredient_id = dish_ingredients.ingredient_id");
 
 			while (res.next()) {
 				int amount = res.getInt("amount");
 				ingInStock[teller] = amount;
 				teller++;
 			}
 			Opprydder.lukkResSet(res);
 			teller = 0;
 
 			res = stm.executeQuery("select dish_ingredients.amount from dish_ingredients, ingredients where ingredients.ingredient_id = dish_ingredients.ingredient_id");
 
 			while (res.next()) {
 				int amount = res.getInt("amount");
 				ingNeeded[teller] = amount;
 				teller++;
 			}
 			Opprydder.lukkResSet(res);
 
 			for (int i = 0; i < ant; i++) {
 				ingDiff[i] = ingInStock[i] - ingNeeded[i];
 			}
 			for (int i = 0; i < ant; i++) {
 				if (ingDiff[i] < 0) {
 					inStock = false;
 				}
 			}
 			if (inStock) {
 				result = "All ingredients are in stock.";
 			} else {
 				result = "Some of the needed ingredients are not in stock, check storage.";
 			}
 			Opprydder.lukkSetning(stm);
 			return result;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		}
 		return "feilfeil";
 	}
 
 	public int addIngredient(Ingredient ingredient) {
 		Statement stm;
 		try {
 			stm = con.createStatement();
 			String[] check = {ingredient.getName(), ingredient.getExpDate()};
 			check = removeUnwantedSymbols(check);
 			int succ = stm.executeUpdate("insert into ingredients (INGREDIENT_NAME, AMOUNT, EXPIRY_DATE) "
 					+ "values('" + check[0] + "' ," + ingredient.getAmount() + ", '" + check[1] + "')");
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
     
 	public int updateIngredient(Ingredient ingToUpdate, Ingredient ing) {
 		Statement stm;
 		String[] check = {ing.getName(), ing.getExpDate()};
 		check = removeUnwantedSymbols(check);
 
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("update ingredients set INGREDIENT_NAME ='" + check[0] + "', AMOUNT =" + ing.getAmount()
 					+ ", EXPIRY_DATE ='" + check[1] + "' where INGREDIENT_ID =" + ingToUpdate.getID());
 			Opprydder.lukkSetning(stm);
 			return succ;
 
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int deleteIngredient(Ingredient ing) {
 		Statement stm;
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("delete from INGREDIENTS where INGREDIENT_ID = " + ing.getID());
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int addCustomer(Customer customer) {
 		Statement stm;
 		String[] check = {customer.getCustomerName()};
 		check = removeUnwantedSymbols(check);
 
 		short isbusiness = 0;
 		if (customer.isBusiness()) {
 			isbusiness = 1;
 		}
 
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("insert into customer (CUSTOMER_NAME, PHONE, BUSINESS)"
 					+ " values('" + check[0] + "' , '" + customer.getPhoneNumber() + "', " + isbusiness + ")");
 			Opprydder.lukkSetning(stm);
 			return succ;
 
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int changeCustomerStatus(Customer customer) {
 		Statement stm;
 		ResultSet res;
 		int succ;
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select status from customer where customer_id = " + customer.getCustomerID());
 			res.next();
 			short status = res.getShort("status");
 			if (status == 1) {
 				succ = stm.executeUpdate("update customer set status =" + 0 + " where customer_id = " + customer.getCustomerID());
 			} else {
 				succ = stm.executeUpdate("update customer set status =" + 1 + " where customer_id = " + customer.getCustomerID());
 			}
 			Opprydder.lukkResSet(res);
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
         
         public Order getOrderfromSub(Subscription subscription){
             Statement stm;
             ResultSet res;
 			int c_id =0;
 			int m_id =0;
             try{
                 stm = con.createStatement();
                 res = stm.executeQuery("SELECT * FROM SUBSCRIPTION JOIN ORDERS ON ORDERS.SUBSCRIPTION_ID = SUBSCRIPTION.SUBSCRIPTION_ID where subscription.subscription_id = " + subscription.getSubscription_id());
                 while(res.next()){
 					c_id = res.getInt("customer_id");
 					m_id =  res.getInt("menu_id");
 				}
                  
                 
                 Order orderFsub = new Order(c_id, m_id);
                 Opprydder.lukkResSet(res);
                 Opprydder.lukkSetning(stm);
                 return orderFsub;
             }catch ( SQLException ex){
                 System.out.println(ex);
             }
             return null;
         }
 
 	public int updateCustomer(Customer customer) {
 		Statement stm;
 		String[] check = {customer.getCustomerName()};
 		check = removeUnwantedSymbols(check);
 
 		short isbusiness = 0;
 		if (customer.isBusiness()) {
 			isbusiness = 1;
 		}
 
 		try {
 			stm = con.createStatement();
			int succ = stm.executeUpdate("update customer set CUSTOMER_NAME ='" + check[0] + "', PHONE =" + customer.getPhoneNumber()
					+ ", BUSINESS =" + isbusiness + " where CUSTOMER_ID =" + customer.getCustomerID());
 			Opprydder.lukkSetning(stm);
 			return succ;
 
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int addCustomerAddress(CustomerAddress address) {
 		Statement stm;
 		String[] check = {address.getAdress(), address.getCity()};
 		check = removeUnwantedSymbols(check);
 
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("insert into CUSTOMER_ADDRESS (ADDRESS,ZIP_CODE,CITY,CUSTOMER_ID)"
 					+ " values('" + check[0] + "' , '" + address.getZipCode() + "' , '" + check[1] + "', " + address.getCustomerID() + ")");
 			Opprydder.lukkSetning(stm);
 			return succ;
 
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int addEmployee(Employee employee) {
 		Statement stm;
 		String[] check = {employee.getUsername(), employee.getPassword()};
 		check = removeUnwantedSymbols(check);
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("insert into employee (USER_TYPE, USERNAME, PASSWORD) "
 					+ "values('" + employee.getUsertype() + "', '" + check[0] + "', '" + check[1] + "'");
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int addMenu(Menu menu) {
 		Statement stm;
 		String[] check = {menu.getMenuName(), menu.getDescription()};
 		check = removeUnwantedSymbols(check);
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("insert into employee (MENU_NAME, PRICE, DESCRIPTION) "
 					+ "values('" + check[0] + "', '" + menu.getPrice() + "', '" + check[1] + "'");
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int changeMenuStatus(Menu menu) {
 		Statement stm;
 		ResultSet res;
 		int succ;
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select status from menu where menu_id = " + menu.getMenuId());
 			res.next();
 			int status = Integer.parseInt(res.getString("status"));
 			if (status == 1) {
 				succ = stm.executeUpdate("update menu set status ='" + 0 + "' where menu_id = " + menu.getMenuId());
 			} else {
 				succ = stm.executeUpdate("update menu set status ='" + 1 + "' where menu_id = " + menu.getMenuId());
 			}
 			Opprydder.lukkResSet(res);
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int changeOrderStatus(int orderID) {
 		Statement stm;
 		ResultSet res;
 		int succ;
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select status from orders where order_id = " + orderID);
 			res.next();
 			int status = Integer.parseInt(res.getString("status"));
 			if (status == 1) {
 				succ = stm.executeUpdate("update orders set status ='" + 0 + "' where order_id = " + orderID);
 			} else {
 				succ = stm.executeUpdate("update orders set status ='" + 1 + "' where order_id = " + orderID);
 			}
 			Opprydder.lukkResSet(res);
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int removeExpiredOrders() {
 		Statement stm;
 		int succ;
 		try {
 			stm = con.createStatement();
 			succ = stm.executeUpdate("UPDATE orders SET status = 0 "
 					+ "WHERE delivery_date < CURRENT DATE;\n");
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int changeEmployeeStatus(Employee employee) {
 		Statement stm;
 		ResultSet res;
 		int succ;
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select status from employees where empl_id = " + employee.getEmplID());
 			res.next();
 			short status = res.getShort("status");
 			if (status == 1) {
 				succ = stm.executeUpdate("update employees set status =" + 0 + " where empl_id = " + employee.getEmplID());
 			} else {
 				succ = stm.executeUpdate("update employees set status =" + 1 + " where empl_id = " + employee.getEmplID());
 			}
 			Opprydder.lukkResSet(res);
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public String[] removeUnwantedSymbols(String[] table) {
 		String[] checkedTable = table;
 		for (int i = 0; i < table.length; i++) {
 			checkedTable[i] = checkedTable[i].replace("'", "");
 			checkedTable[i] = checkedTable[i].replace(";", " ");
 		}
 		return checkedTable;
 	}
 
 	public void close() {
 		Opprydder.lukkForbindelse(con);
 	}
 }
