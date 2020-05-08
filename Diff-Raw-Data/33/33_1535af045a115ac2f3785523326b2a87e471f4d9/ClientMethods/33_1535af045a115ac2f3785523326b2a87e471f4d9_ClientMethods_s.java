 import java.sql.*;
 import java.util.ArrayList;
 
 class ClientMethods {
  	private String dbdriver = "org.apache.derby.jdbc.ClientDriver";
     private String dbname = "jdbc:derby://localhost:1527/CateringBuddy;user=db;password=db";
     private ParentWindow parent = new ParentWindow();
     
 	public boolean regNewCustomer() throws Exception {
 		CustomerRegistration registration = new CustomerRegistration(parent);
 		registration.setLocation(350, 350);
 		registration.setVisible(true);
 		String sql = "";
 		try {
 			sql = registration.regCustomer();
 		} catch (NullPointerException e) {
 			ConnectionManager.printMessage(e,"Some data is not present");
 		}
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 		Statement state = connection.createStatement();
     	int answer = state.executeUpdate(sql);
 		if(answer>0){
 			ConnectionManager.closeStatement(state);
 			ConnectionManager.closeConnection(connection);
 			return true;
 		} else {
 			ConnectionManager.closeStatement(state);
 			ConnectionManager.closeConnection(connection);
 			return false;
 		}
 	}	
 	public boolean editCustomer(int kid) throws Exception {
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    
 		ConnectionManager.setAutoCommit(connection, false); //turns off autocommit
 		
 		Customer customer = getCustomer(kid);
 		CustomerRegistration registration = new CustomerRegistration(parent);
 		String[] data = new String[3];
 		try {
 			data = registration.editCustomer(customer);
 		} catch (NullPointerException e) {
 			ConnectionManager.printMessage(e,"Some data is not present");
 		}
 		boolean j = false, k = false, l = false;
 		if(!data[0].equals(customer.getName())) {
     		j = customer.setName(data[0], connection);
     	}
     	if(Integer.parseInt(data[1]) != (customer.getPhone())) {
     		k = customer.setPhone(Integer.parseInt(data[1]), connection);
     	}
     	if(!data[2].equals(customer.getAdress())) {
     		l = customer.setAdress(data[2], connection);
     	}
 
 		if(j && k && l) {
 			ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 			ConnectionManager.closeConnection(connection);
 			return true;
 		} else {
 			ConnectionManager.rollback(connection); //rollback if fail
 			ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 			ConnectionManager.closeConnection(connection);
 			return false;
 		}
 	}
 	public Customer getCustomerWKid(int kid) throws Exception {
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 		String sql = "SELECT * from customer where kid = " + kid + "";
 		ResultSet res = state.executeQuery(sql);
 		String navn = "";
 		int telefonnr = 0;
 		String adresse = "";
 		int typen = 0;
 		while(res.next()){
 			kid = Integer.parseInt(res.getString("kid"));
 			navn = res.getString("name");
 			telefonnr = Integer.parseInt(res.getString("phone"));
 			adresse = res.getString("adress");
 			typen = Integer.parseInt(res.getString("type"));
 		}
 		ConnectionManager.closeResSet(res);
 		Customer hanher = new Customer(kid, navn, telefonnr, adresse, typen);
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
 		return hanher;
 	}
 	public Customer getCustomer(int phone) throws Exception {
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 		String sql = "SELECT * from customer where phone = " + phone + "";
 		ResultSet res = state.executeQuery(sql);
 		String name = "";
 		int kid = 0;
 		String adress = "";
 		int type = 0;
 		while(res.next()){
 			kid = Integer.parseInt(res.getString("kid"));
 			name = res.getString("name");
 			adress = res.getString("adress");
 			type = Integer.parseInt(res.getString("type"));
 		}
 		ConnectionManager.closeResSet(res);
 		Customer hanher = new Customer(kid, name, phone, adress, type);
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
 		return hanher;
 	}	
 	public String findOrders(int kid) {
 		String sql = "SELECT * from orders where kid = " + kid + "";
 		return sql;
 	}
 	public String findOrdersByStatus(String status) {
 		String sql = "SELECT * from orders where status = '" + status + "'";
 		return sql;
 	}
 	public boolean addOrder(int phone) throws Exception {
 		Customer customer = getCustomer(phone);
 		Order order;
 		OrderMenu orderMenu = new OrderMenu(parent);
 		orderMenu.setLocation(350, 350);
 		orderMenu.setVisible(true);
 		order = orderMenu.getOrder();
		String sql = "INSERT INTO orders(kid, status, ordertime, deliverytime, deliveryadress, price) values(" + customer.getKid() + ",'" + order.getStatus() + "', current_timestamp,'" + order.getDeliveryTime() + "', '" + order.getDeliveryAdress() + "', " + order.getPrice() + ")";
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 	    ConnectionManager.setAutoCommit(connection, false); //turns off autocommit
 	    
 	    int answer = state.executeUpdate(sql);
 	    ArrayList<Dish> dishes = order.getOrderContent();
 	    sql = "SELECT orderid FROM orders WHERE kid = " + customer.getKid() + " AND where deliverytime = '" + order.getDeliveryTime() + "' AND pirce = " + order.getPrice() + "";
 	    ResultSet res = state.executeQuery(sql);
 	    int orderid = Integer.parseInt(res.getString("orderid"));
 	    if(answer>0) {
 	    	int i = 1;
 	    	int answer2;
 	    	for(Dish aDish : dishes) {
 	    		sql = "INSERT INTO ordercontent(orderid, orderline, dishid, antall) values(" + orderid + ", " + i + ", " + aDish.getDishID() + ", 1)";
 	    		answer2 = state.executeUpdate(sql);
 	    		i++;
 	    		if(answer2<1) {
 	    			ConnectionManager.rollback(connection); //rollback if fail
 	    			ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 	    			ConnectionManager.closeResSet(res);
 	    			ConnectionManager.closeStatement(state);
 	    			ConnectionManager.closeConnection(connection);
 	    			return false;
 	    		}
 	    	}
 	    }
 	    ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 	    ConnectionManager.closeResSet(res);
 	    ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
 		return true;
 	}
 	public Order getOrder(int orderid) throws Exception {
 		Class.forName(dbdriver);
 		Connection connection = DriverManager.getConnection(dbname);
 		Statement state = connection.createStatement();
 		
 		Order order = null;
 		ArrayList<Dish> dishes = new ArrayList<Dish>();
 		ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 		
 		String sql = "SELECT * from orders where orderid = " + orderid + "";
 		ResultSet res = state.executeQuery(sql);
 		while(res.next()){
 			String status = res.getString("status");
 			int kid = Integer.parseInt(res.getString("kid"));
 			String ordertime = res.getString("ordertime");
 			String deliverytime = res.getString("deliverytime");
 			String deliveryadress = res.getString("adress");
 			Double price = Double.parseDouble(res.getString("price"));
 			
 			sql = "SELECT * FROM orderContent c, dish d, orders o WHERE o.orderid = " + orderid + " AND o.orderid = c.orderid AND c.dishid = d.dishid";
 			Statement state2 = connection.createStatement();
 			ResultSet res2 = state2.executeQuery(sql);
 			while(res2.next()) {
 				int dishID = Integer.parseInt(res2.getString("dishid"));
 				String name = res2.getString("name");
 				Double price2 = Double.parseDouble(res2.getString("price"));
 				
 				sql = "SELECT * FROM ingredients i, dishContent c where c.dishid = " + dishID + " AND i.ingredientid = c.ingredientid";
 				Statement state3 = connection.createStatement();
 				ResultSet res3 = state3.executeQuery(sql);
 				while(res3.next()) {
 					String name2 = res3.getString("name");
 					int ingredientid = Integer.parseInt(res3.getString("ingredientid"));
 					String metric = res3.getString("metric");
 					double amount = Double.parseDouble(res3.getString("amount"));
 					
 					Ingredient ingredient = new Ingredient(name2, ingredientid, metric, amount);
 					ingredients.add(ingredient);
 				}
 				Dish dish = new Dish(dishID, name, ingredients, price2);
 				dishes.add(dish);
 			}
 			order = new Order(orderid, kid, status, ordertime, deliverytime, deliveryadress, dishes, price);
 		}
 		return order;
 	}
 	public ArrayList<Order> listOrders() throws Exception{
 
 	Class.forName(dbdriver);
 	Connection connection = DriverManager.getConnection(dbname);
 	Statement state = connection.createStatement();
 
 	String status = "Bestilt"; //m sprre om status
 	String sql = findOrdersByStatus(status);
 	ResultSet res = state.executeQuery(sql);
 	int orderId;
 	int kid = 0;
 	String ordertime;
 	String deliverytime;
 	String deliveryadress;
 	int typen = 0;
 	double price = 0;
 	ArrayList<Order> orders = new ArrayList<Order>();
 	ArrayList<Dish> dishes = new ArrayList<Dish>();
 	ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 	String sql1 = "";
 	String sql2 = "";
 	while(res.next()){
 		orderId = Integer.parseInt(res.getString("orderid"));
 		kid = Integer.parseInt(res.getString("kid"));
 		ordertime = res.getString("ordertime");
 		deliverytime = res.getString("deliverytime");
 		deliveryadress = res.getString("adress");
 		status = res.getString("status");
 		sql1 = "Select * from orderContent c, dish d, orders o where where o.orderid = c.orderid AND c.dishid = d.dishid"; //m addes
 		ResultSet res2 = state.executeQuery(sql1);
 		while(res2.next()) {
 			int dishID = Integer.parseInt(res2.getString("dishid"));
 			String name = res2.getString("name");
 			price = Double.parseDouble(res2.getString("price"));
 
 			sql2 = "Select * from ingredients i, dishContent c where c.dishid = "+ dishID +" AND i.ingredientid = c.ingredientid"; //m addes
 			ResultSet res3 = state.executeQuery(sql2);
 			while(res3.next()) {
 				String name2 = res3.getString("name");
 				int ingredientid = Integer.parseInt(res3.getString("ingredientid"));
 				String metric = res3.getString("metric");
 				int amount = Integer.parseInt(res3.getString("amount"));
 
 				Ingredient ingredient = new Ingredient(name2, ingredientid, metric, amount);
 				ingredients.add(ingredient);
 			}
 			Dish dish2 = new Dish(dishID, name, ingredients, price);
 			dishes.add(dish2);
 		}
 		Order order = new Order(orderId, kid, status, ordertime, deliverytime, deliveryadress, dishes, price);
 		orders.add(order);
 	}
 	res.close();
 	/*for(Order enOrder : orders) {
 		String kundeinfo = enOrder.toString();
 		System.out.print(kundeinfo); */
 	return orders;
 	}
 	public ArrayList<Ingredient> listIngredients() throws Exception {
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 	    ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 	    String sql = "SELECT * from ingredients";
 	    ResultSet res = state.executeQuery(sql);
 	    while(res.next()) {
 	    	String name = res.getString("name");
 			int ingredientid = Integer.parseInt(res.getString("ingredientid"));
 			String metric = res.getString("metric");
 			double amount = Double.parseDouble(res.getString("amount"));
 
 			Ingredient ingredient = new Ingredient(name, ingredientid, metric, amount);
 			ingredients.add(ingredient);
 	    }
 	    ConnectionManager.closeResSet(res);
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
 		
 		return ingredients;
 	}
 	public ArrayList<Dish> listDishes() throws Exception {
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 	    ConnectionManager.setAutoCommit(connection, false); //turns off autocommit
 	    
 		ArrayList<Dish> dishes = new ArrayList<Dish>();
 		ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 		
 		String sql = "SELECT * from dishes";
 		ResultSet res = state.executeQuery(sql);
 		int i = 0;
 		while(res.next()) {
 			System.out.println(i);
 			i++;
 			int dishID = Integer.parseInt(res.getString("dishid"));
 			String name = res.getString("name");
 			double price = Double.parseDouble(res.getString("price"));
 			String type = res.getString("type");
 			String sql2 = "SELECT * from ingredients NATURAL JOIN dishcontent where dishcontent.dishid = " + dishID + " AND dishcontent.ingredientid = ingredients.ingredientid";
 			Statement state2 = connection.createStatement();
 			ResultSet res2 = state2.executeQuery(sql2);
 			
 			while(res2.next()) {
 				String name2 = res2.getString("name");
 				int ingredientid = Integer.parseInt(res2.getString("ingredientid"));
 				String metric = res2.getString("metric");
 				double amount = Double.parseDouble(res2.getString("amount"));
 
 				Ingredient ingredient = new Ingredient(name2, ingredientid, metric, amount);
 				ingredients.add(ingredient);
 			}
 			if(type.equals("Main course")) { //decides the subclass of Dish
 				MainCourse dish = new MainCourse(dishID, name, ingredients, price);
 				dishes.add(dish);
 			} else if(type.equals("Appetizer")) {
 				Appetizer dish = new Appetizer(dishID, name, ingredients, price);
 				dishes.add(dish);
 			} else if(type.equals("Dessert")) {
 				Dessert dish = new Dessert(dishID, name, ingredients, price);
 				dishes.add(dish);
 			}			
 			ConnectionManager.closeResSet(res2);
 			ConnectionManager.closeStatement(state2);
 		}
 		ConnectionManager.closeResSet(res);
 		ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
 		
 		return dishes;
 	}
 	public boolean setNewDish(String name, String ingredients, double price) throws Exception { //add dish//
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 	 	String sql = "INSERT INTO Dish(name, ingredients, price)values('" + name + "'" + "" +
 	 			"," + "'" + ingredients + "'" + "," + price + "')";
 	 	int answer = state.executeUpdate(sql);
 		if(answer>0){
 			state.close();
 			connection.close();
 			return true;
 		}else{
 			state.close();
 			connection.close();
 			return false;
 		}
 	}
 	public boolean addDish(String name, double price, String type) throws Exception { //add dish//
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 	 	String sql = "INSERT INTO Dish(name, price, type)values('" + name + "'" + "" +
 	 			"," + "'" + price + "'" + "," + type + "')";
 	 	int answer = state.executeUpdate(sql);
 		if(answer>0){
 			state.close();
 			connection.close();
 			return true;
 		}else{
 			state.close();
 			connection.close();
 			return false;
 		}
 	}
 	public Dish findDish(String name) throws Exception{
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 	    String sql = "SELECT * from dish where name = " + name + "";
 	    ResultSet res = state.executeQuery(sql);
 	    int dishID = 0;
 	    ArrayList<Integer> ingredientID = new ArrayList<>();
 	    ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 		double price = 0;
 		while(res.next()){
 			dishID = Integer.parseInt(res.getString("dishID"));
 			price = Double.parseDouble(res.getString("price"));
 			System.out.println(dishID + ":" + name + "" + "" + price);		
 		}
 		res.close();
 		sql = "SELECT ingredientid FROM dishcontent WHERE dishID = " + dishID + "";
 		res = state.executeQuery(sql);
 		while(res.next()){
 			ingredientID.add(Integer.parseInt(res.getString("IngredientID")));
 		}
 		res.close();
 		int x;
 		String ingredname;
 		String metric;
 		int amount;
 		for(int i = 0; i < ingredientID.size(); i++) {
 			x = ingredientID.get(i);
 			sql = "SELECT * FROM ingredients WHERE ingredientid = " + x + "";
 			res = state.executeQuery(sql);
 			while(res.next()) {
 				ingredname = res.getString("name");
 				metric = res.getString("metric");
 				amount = Integer.parseInt(res.getString("amount"));
 				Ingredient namm = new Ingredient(ingredname, x, metric, amount);
 				ingredients.add(namm);
 			}
 		}
 		Dish retten = new Dish(dishID, name, ingredients, price);
 		state.close();
 		connection.close();
 		return retten;
 	}
 	public boolean addIngredient() throws Exception{    
 		IngredientRegistration ingredientRegistration = new IngredientRegistration(parent);
 		ingredientRegistration.setLocation(350, 350);
 		ingredientRegistration.setVisible(true);
 		
 		String sql = "";
 		try {
 			sql = ingredientRegistration.addIngredient();
 		} catch (NullPointerException e) {
 			ConnectionManager.printMessage(e,"Some data is not present");
 		}
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 	    
 	    int answer = state.executeUpdate(sql);
 		if(answer>0){
 			ConnectionManager.closeStatement(state);
 			ConnectionManager.closeConnection(connection);
 			return true;
 		} else {
 			ConnectionManager.closeStatement(state);
 			ConnectionManager.closeConnection(connection);
 			return false;
 		}
 	}
 	public boolean executeStockpileUpdate(Ingredient ingredient, double refillAmount) throws Exception {
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    
 	    boolean check = ingredient.setAmount(refillAmount,connection);
 	
 		ConnectionManager.closeConnection(connection);	
 		return check;
 	}
 	public boolean editOrder(Order order) throws Exception {
 		OrderMenu orderMenu = new OrderMenu(parent);
 		orderMenu.setLocation(350, 350);
 		orderMenu.setVisible(true);
 		Order newOrder = orderMenu.editOrder(order);
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    ConnectionManager.setAutoCommit(connection, false); //turns off autocommit
 	    boolean j = false, k = false, l = false;
 		if(!order.getDeliveryTime().equals(newOrder.getDeliveryTime())){
 			j = order.setDeliveryTime(newOrder.getDeliveryTime(), connection);
 		} else {
 			j = true;
 		}
 		if(!order.getDeliveryAdress().equals(newOrder.getDeliveryAdress())) {
 			k = order.setDeliveryAdress(newOrder.getDeliveryAdress(), connection);
 		} else {
 			k = true;
 		}
 		if((order.getOrderContent().equals(newOrder.getOrderContent()))) { //denne m endres, trur ikke den sammenligner korrekt
 			l = order.setOrderContent(newOrder.getOrderContent(), connection);
 		} else {
 			l = true;
 		}
 		if(j && k && l) {
 			ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 			ConnectionManager.closeConnection(connection);
 			return true;
 		} else {
 			ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 			ConnectionManager.closeConnection(connection);
 			return false;
 		}
 	}
 }
