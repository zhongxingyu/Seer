 import java.lang.reflect.*;
 import java.util.HashMap;
 import java.sql.*;
 
 public class WebApp {
 	
 	public static HashMap<String, Object> cache;
 	
 	public static void main(String args[]) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
 		try
 		{
 			Class.forName("org.postgresql.Driver");
			String dbURL = "jdbc:postgresql://emotion.cs.umass.edu/test";
 			Connection dbCon = DriverManager.getConnection(dbURL, "keen", "hunter2");
 			Statement st = dbCon.createStatement();
 			ResultSet rs = st.executeQuery("SELECT VERSION()");
 			if (rs.next()) {
 				System.out.println(rs.getString(1));
 			}
 		} catch (SQLException ex) {
 			System.out.println("fail");
 		}
 		WebApp.cache = new HashMap<String, Object>();
 		
		double x = (Double) call("WebApp","add",new Object[] {(double)1000000000.0, (double)2});
 		System.out.println(x);
		double y = (Double) call("WebApp","add",new Object[] {(double)1000000000.0, (double)2});
 		System.out.println(y);
 		System.out.println(cache);
 	}
 	
 	public static Object call (String clazz, String method, Object[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 		Class<?> theClass = Class.forName(clazz);
 		Class<?>[] arguments = new Class<?>[args.length];
 		
 		String key = clazz + ".." + method;
 		for (int i = 0; i < args.length; ++i) {
 			key += ".." + args[i].toString();
 			arguments[i] = args[i].getClass();
 		}
 		
 		if(cache.containsKey(key)) {
 			return cache.get(key);
 		}
 		
 		Method toCall = theClass.getMethod(method, arguments);
 		
 		Object result;
 		if (theClass.getName() == "Statement" && toCall.getName() == "executeQuery") {
 			result = toCall.invoke(TxCache.class, args); // Connection create statement, resultset?
 		} else {
 			result = toCall.invoke(theClass, args);
 		}
 		
 		cache.put(key, result);
 		
 		return result;
 		
 	}
 	
 	public static double add (Double a,Double b) {
 		int j = 0;
 		while(j < a) j++;
 		return a+b;
 	}
 
 }
