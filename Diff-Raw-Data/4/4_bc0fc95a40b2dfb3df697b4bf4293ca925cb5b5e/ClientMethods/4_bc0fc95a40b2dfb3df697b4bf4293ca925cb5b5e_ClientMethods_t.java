 import static javax.swing.JOptionPane.*;
 import java.sql.*;
 import java.util.ArrayList;
 
 class ClientMethods {
  	private String dbdriver = "org.apache.derby.jdbc.ClientDriver";
     private String dbname = "jdbc:derby://localhost:1527/CateringBuddy;user=db;password=db";
     private ParentWindow parent = new ParentWindow();
     
 	public boolean regNewCustomer() throws Exception {
 		CustomerRegistration registration = new CustomerRegistration(parent);
 		registration.setLocation(350, 350);
 		String sql = "";
 		int answer;
 		try {
 			sql = registration.regCustomer();
 		} catch (NullPointerException e) {
 			ConnectionManager.printMessage(e,"Some data is not present");
 			return false;
 		}
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 		Statement state = connection.createStatement();
     	try {
     		answer = state.executeUpdate(sql);
     	} catch(SQLException e) {
     		return false;
     	}
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
 		registration.setLocation(350, 350);
 		String[] data = new String[3];
 		try {
 			data = registration.editCustomer(customer);
 		} catch (NullPointerException e) {
 			ConnectionManager.printMessage(e,"Some data is not present");
 		}
 		boolean j = false, k = false, l = false;
 		try {
 			if(!data[0].equals(customer.getName())) {
 				j = customer.setName(data[0], connection);
 			} else {
 				j = true;
 			}
 			if(Integer.parseInt(data[1]) != (customer.getPhone())) {
 				k = customer.setPhone(Integer.parseInt(data[1]), connection);
 			} else {
 				k = true;
 			}
 			if(!data[2].equals(customer.getAdress())) {
 				l = customer.setAdress(data[2], connection);
 			} else {
 				l = true;
 			}
 		} catch (NullPointerException e) {
 			return false;
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
 		res.next();
 		int kid;
 		try {
 			kid = Integer.parseInt(res.getString("kid"));
 		} catch (SQLException e) {
 			ConnectionManager.closeResSet(res);
 			ConnectionManager.closeStatement(state);
 			ConnectionManager.closeConnection(connection);
 			throw new NullPointerException();
 		}
 		String name = res.getString("name");
 		String adress = res.getString("adress");
 		int type = Integer.parseInt(res.getString("type"));
 
 		ConnectionManager.closeResSet(res);
 		Customer hanher = new Customer(kid, name, phone, adress, type);
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
 		return hanher;
 	}
 	public boolean addOrder() throws Exception {
 		int phone = 0;
 		Customer customer = null;
 		boolean check = true;
 		while(check) {
 			try {
 				phone = Integer.parseInt(showInputDialog(null,
 						"Input the customers phone number"));
 				check = false;
 			} catch (NumberFormatException e) {
 				int answer = showConfirmDialog(null,
 		                 "Phone number format error, try again? ",
 		                 "Error", YES_NO_OPTION);
 				if (answer == YES_OPTION) {
 					check = true;
 					continue;
 				} else {
 					return false;
 				}
 			}
 			boolean check2 = true;
 			while(check2) {
 				try {
 					customer = getCustomer(phone);
 					check2 = false;
 				} catch (NullPointerException e) {
 					int answer2 = showConfirmDialog(null,
 			                 "No customer registered with this phone number, try again? ",
 			                 "Error", YES_NO_OPTION);
 					if (answer2 == YES_OPTION) {
 						check2 = true;
 						check = true;
 						break;
 					} else {
 						return false;
 					}
 				}
 			}
 		}
 		Order order;
 		OrderMenu orderMenu = new OrderMenu(parent);
 		orderMenu.setLocation(350, 350);
 		order = orderMenu.getOrder();
 		String sql;
 		try {
 			sql = "INSERT INTO orders(kid, status, ordertime, deliverytime, deliveryadress, price, paid) values(" + customer.getKid() + ",'" + order.getStatus() + "', current_timestamp,'" + order.getDeliveryTime() + "', '" + order.getDeliveryAdress() + "', " + order.getPrice() + ", null)";
 		} catch(NullPointerException e) {
 			return false;
 		}
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
 	    ConnectionManager.setAutoCommit(connection, false); //turns off autocommit
 	    
 	    int answer = state.executeUpdate(sql);
 	    ArrayList<Dish> dishes = order.getOrderContent();
 	    sql = "SELECT orderid FROM orders WHERE kid = " + customer.getKid() + " AND deliverytime = '" + order.getDeliveryTime() + "' AND price = " + order.getPrice() + "";
 	    ResultSet res = state.executeQuery(sql);
 	    res.next();
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
 	    } else {
 	    	ConnectionManager.rollback(connection); //rollback if fail
 			ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 			ConnectionManager.closeResSet(res);
 			ConnectionManager.closeStatement(state);
 			ConnectionManager.closeConnection(connection);
 			return false;
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
 				String type = res2.getString("type");
 				
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
 				ConnectionManager.closeResSet(res3);
     			ConnectionManager.closeStatement(state3);
     			if(type.equals("MainCourse")) {
 					MainCourse dish = new MainCourse(dishID, name, ingredients, price2);
 					dishes.add(dish);
 				} else if(type.equals("Appetizer")) {
 					Appetizer dish = new Appetizer(dishID, name, ingredients, price2);
 					dishes.add(dish);
 				} else {
 					Dessert dish = new Dessert(dishID, name, ingredients, price2);
 					dishes.add(dish);
 				}
 			}
 			ConnectionManager.closeResSet(res2);
 			ConnectionManager.closeStatement(state2);
 			order = new Order(orderid, kid, status, ordertime, deliverytime, deliveryadress, dishes, price);
 		}
 		ConnectionManager.closeResSet(res);
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
 		return order;
 	}
 	public ArrayList<Order> listOrders() throws Exception{
 		Class.forName(dbdriver);
 		Connection connection = DriverManager.getConnection(dbname);
 		Statement state = connection.createStatement();
 
 		String sql = "SELECT * FROM orders";
 		ResultSet res = state.executeQuery(sql);
 		ArrayList<Order> orders = new ArrayList<Order>();
 		ArrayList<Dish> dishes = new ArrayList<Dish>();
 		ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 		while(res.next()){
 			int orderId = Integer.parseInt(res.getString("orderid"));
 			int kid = Integer.parseInt(res.getString("kid"));
 			String ordertime = res.getString("ordertime");
 			String deliverytime = res.getString("deliverytime");
 			String deliveryadress = res.getString("deliveryadress");
 			String status = res.getString("status");
 			Double price = Double.parseDouble(res.getString("price"));
 			
 			sql = "Select * from orderContent c, dishes d, orders o where o.orderid = c.orderid AND c.dishid = d.dishid"; //m addes
 			Statement state2 = connection.createStatement();
 			ResultSet res2 = state2.executeQuery(sql);
 			while(res2.next()) {
 				int dishID = Integer.parseInt(res2.getString("dishid"));
 				String name = res2.getString("name");
 				double price2 = Double.parseDouble(res2.getString("price"));
 				String type = res2.getString("type");
 				sql = "Select * from ingredients i, dishContent c where c.dishid = "+ dishID +" AND i.ingredientid = c.ingredientid"; //m addes
 				Statement state3 = connection.createStatement();
 				ResultSet res3 = state3.executeQuery(sql);
 				while(res3.next()) {
 					String name2 = res3.getString("name");
 					int ingredientid = Integer.parseInt(res3.getString("ingredientid"));
 					String metric = res3.getString("metric");
 					int amount = Integer.parseInt(res3.getString("amount"));
 
 					Ingredient ingredient = new Ingredient(name2, ingredientid, metric, amount);
 					ingredients.add(ingredient);
 				}
 				ConnectionManager.closeResSet(res3);
 				ConnectionManager.closeStatement(state3);
 				if(type.equals("MainCourse")) {
 					MainCourse dish = new MainCourse(dishID, name, ingredients, price2);
 					dishes.add(dish);
 				} else if(type.equals("Appetizer")) {
 					Appetizer dish = new Appetizer(dishID, name, ingredients, price2);
 					dishes.add(dish);
 				} else {
 					Dessert dish = new Dessert(dishID, name, ingredients, price2);
 					dishes.add(dish);
 				}
 			}
 			ConnectionManager.closeResSet(res2);
 			ConnectionManager.closeStatement(state2);
 			Order order = new Order(orderId, kid, status, ordertime, deliverytime, deliveryadress, dishes, price);
 			orders.add(order);
 		}
 		ConnectionManager.closeResSet(res);
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
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
 	public boolean addDish() throws Exception {
 		DishRegistration reg = new DishRegistration(parent);
 		reg.setLocation(350, 350);
 		String type;
 		String name;
 		Double price;
 		ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 		Dish dish = reg.newDish();
 		try {
 			if(dish instanceof MainCourse) {
 				MainCourse newDish = (MainCourse) reg.newDish();
 				type = "MainCourse";
 				name = newDish.getName();
 				price = newDish.getPrice();
 				ingredients = newDish.getIngredients();
 				
 			} else if (dish instanceof Dessert) {
 				Dessert newDish = (Dessert) reg.newDish();
 				type = "Dessert";
 				name = newDish.getName();
 				price = newDish.getPrice();
 				ingredients = newDish.getIngredients();
 			} else {
 				Appetizer newDish = (Appetizer) reg.newDish();
 				type = "Appetizer";
 				name = newDish.getName();
 				price = newDish.getPrice();
 				ingredients = newDish.getIngredients();
 			}
 		} catch (NullPointerException e) {
 			return false;
 		}
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    ConnectionManager.setAutoCommit(connection, false); //turns off autocommit
 	    Statement state = connection.createStatement();
 	 	String sql = "INSERT INTO dishes(name, price, type)values('" + name + "'," + price + ",'" + type + "')";
 	 	int answer = state.executeUpdate(sql);
 	 	ConnectionManager.closeStatement(state);
 	 	
 	 	sql = "SELECT * FROM dishes WHERE name = '" + name + "' AND price = " + price + "";
 	 	state = connection.createStatement();
 	    ResultSet res = state.executeQuery(sql);
 	    res.next();
 	    int dishid = Integer.parseInt(res.getString("dishid"));
 	    ConnectionManager.closeResSet(res);
 	 	if(answer>0){ //input dish content into db	
 	 		int i = 1;
 	    	int answer2;
 	    	for(Ingredient anIngredient : ingredients) {
 	    		sql = "INSERT INTO dishcontent(dishid, ingredientid, orderline) values(" + dishid + "," + anIngredient.getIngredientId() + "," + i + ")";
 	    		answer2 = state.executeUpdate(sql);
 	    		i++;
 	    		if(answer2<1) { //needs an errormessage maybe TODO 
 	    			ConnectionManager.rollback(connection); //rollback if fail
 	    			ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 	    			ConnectionManager.closeStatement(state);
 	    			ConnectionManager.closeConnection(connection);
 	    			return false;
 	    		}
 	    	}
 	 	} else { //needs an errormessage maybe TODO 
 	 		ConnectionManager.rollback(connection); //rollback if fail
 			ConnectionManager.closeStatement(state);
 			ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 			ConnectionManager.closeConnection(connection);
 			return false;
 	 	}
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.setAutoCommit(connection, true); //turns on autocommit
 		ConnectionManager.closeConnection(connection);
 		return true;
 	}
 	public boolean editDish()throws Exception {
 		String dishName = showInputDialog(null,"Input the name of the dish");
 		Dish dish = findDish(dishName);
 		DishRegistration reg = new DishRegistration(parent);
 		reg.setLocation(350, 350);
 		Dish newDish = reg.editDish(dish);
 		
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    ConnectionManager.setAutoCommit(connection, false); //turns off autocommit
 	    
 	    boolean j = false, k = false, l = false;
 		if(!dish.getName().equals(newDish.getName())){
 			j = dish.setName(newDish.getName(), connection);
 		} else {
 			j = true;
 		}
 		if(dish.getPrice() != newDish.getPrice()) {
 			k = dish.setName(newDish.getName(), connection);
 		} else {
 			k = true;
 		}
 		if((!dish.getIngredients().equals(newDish.getIngredients()))) { //denne m endres, trur ikke den sammenligner korrekt TODO
 			l = dish.setIngredients(newDish.getIngredients(), connection);
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
 	public Dish findDish(String name) throws Exception{
 		Class.forName(dbdriver);
 	    Connection connection = DriverManager.getConnection(dbname);
 	    Statement state = connection.createStatement();
	    String sql = "SELECT * from dishes WHERE name = '" + name + "'";
 	    ResultSet res = state.executeQuery(sql);
	    res.next();
 	    int dishID = 0;
 	    ArrayList<Integer> ingredientID = new ArrayList<>();
 	    ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 		double price = 0;
 		while(res.next()){
 			dishID = Integer.parseInt(res.getString("dishID"));
 			price = Double.parseDouble(res.getString("price"));
 			System.out.println(dishID + ":" + name + "" + "" + price);		
 		}
 		ConnectionManager.closeResSet(res);
 		
 		sql = "SELECT ingredientid FROM dishcontent WHERE dishID = " + dishID + "";
 		res = state.executeQuery(sql);
 		while(res.next()){
 			ingredientID.add(Integer.parseInt(res.getString("IngredientID")));
 		}
 		ConnectionManager.closeResSet(res);
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
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
 		return retten;
 	}
 	public boolean addIngredient() throws Exception{    
 		IngredientRegistration ingredientRegistration = new IngredientRegistration(parent);
 		ingredientRegistration.setLocation(350, 350);
 		
 		String sql = "";
 		sql = ingredientRegistration.addIngredient();
 		if(sql == null) {
 			return false;
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
 		if((!order.getOrderContent().equals(newOrder.getOrderContent()))) { //denne m endres, trur ikke den sammenligner korrekt TODO
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
 	public  ArrayList<Order> listOrdersOnCustomer(int phone) throws Exception{
 		Class.forName(dbdriver);
 		Connection connection = DriverManager.getConnection(dbname);
 		Statement state = connection.createStatement();
 		
 		Customer customer = getCustomer(phone);
 		int kid  = customer.getKid();
 		
 		String sql = "SELECT * FROM orders WHERE kid = " + kid + " ORDER BY deliverytime DESC";
 		ResultSet res = state.executeQuery(sql);
 		ArrayList<Order> orders = new ArrayList<Order>();
 		ArrayList<Dish> dishes = new ArrayList<Dish>();
 		ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 		while(res.next()){
 			int orderId = Integer.parseInt(res.getString("orderid"));
 			String ordertime = res.getString("ordertime");
 			String deliverytime = res.getString("deliverytime");
 			String deliveryadress = res.getString("adress");
 			String status = res.getString("status");
 			double price = Double.parseDouble(res.getString("price"));
 					
 			sql = "Select * from orderContent c, dish d, orders o where where o.orderid = c.orderid AND c.dishid = d.dishid";
 			Statement state2 = connection.createStatement();
 			ResultSet res2 = state2.executeQuery(sql);
 			while(res2.next()) {
 				int dishID = Integer.parseInt(res2.getString("dishid"));
 				String name = res2.getString("name");
 				Double price2 = Double.parseDouble(res2.getString("price"));
 				String type = res2.getString("type");
 				
 				sql = "Select * from ingredients i, dishContent c where c.dishid = "+ dishID +" AND i.ingredientid = c.ingredientid";
 				Statement state3 = connection.createStatement();
 				ResultSet res3 = state3.executeQuery(sql);
 				while(res3.next()) {
 					String name2 = res3.getString("name");
 					int ingredientid = Integer.parseInt(res3.getString("ingredientid"));
 					String metric = res3.getString("metric");
 					int amount = Integer.parseInt(res3.getString("amount"));
 					Ingredient ingredient = new Ingredient(name2, ingredientid, metric, amount);
 					ingredients.add(ingredient);
 				}
 				ConnectionManager.closeResSet(res3);
 				ConnectionManager.closeStatement(state3);
 				if(type.equals("MainCourse")) {
 					MainCourse dish = new MainCourse(dishID, name, ingredients, price2);
 					dishes.add(dish);
 				} else if(type.equals("Appetizer")) {
 					Appetizer dish = new Appetizer(dishID, name, ingredients, price2);
 					dishes.add(dish);
 				} else {
 					Dessert dish = new Dessert(dishID, name, ingredients, price2);
 					dishes.add(dish);
 				}
 			}
 			ConnectionManager.closeResSet(res2);
 			ConnectionManager.closeStatement(state2);
 			Order order = new Order(orderId, kid, status, ordertime, deliverytime, deliveryadress, dishes, price);				orders.add(order);
 		}
 		ConnectionManager.closeResSet(res);
 		ConnectionManager.closeStatement(state);
 		ConnectionManager.closeConnection(connection);
 		for(Order enOrder : orders) {
 			String kundeinfo = enOrder.toString();
 			System.out.print(kundeinfo); 
 		}
 		return orders;
 	}
 	/*public ArrayList<Order> getCustomerOrders(int tlf){
 		
 	}*/
 	public void showOrderList() throws Exception{
 		tableView.createAndShowGUI();
 	}
 	/*public boolean viewStockpile() {
 		stockpileView stockpile = new stockpileView(parent);
 		stockpile.setLocation(350, 350);
 		//stockpile.setOpaque(true); //content panes must be opaque
 		//parent.setContentPane(stockpile);
 		try {
 			stockpile.createAndShowGUI();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return true;
 	}*/
 }
