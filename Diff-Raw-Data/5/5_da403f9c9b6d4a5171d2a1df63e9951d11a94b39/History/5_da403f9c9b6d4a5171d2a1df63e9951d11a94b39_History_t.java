 package hygeia;
 import java.util.ArrayList;
 import java.sql.*;
 
 public class History {
 
     private Database db;
     private int uid;
     
     /* Creates user's history object */
     public History(User u) {
     	this.db = u.getDb();
     	this.uid = u.getUid();
     }
     
     /* Returns an array of Meal objects */
     public Meal.List[] getHistory() {
     
         ResultSet rs = this.db.execute("select history.mid, " + 
             "history.occurrence, meals.name from history inner join meals on " +
             "history.mid=meals.mid where history.uid=" + this.uid + " order " +
             "by meals.name;");
         
         ArrayList<Meal.List> meals = new ArrayList<Meal.List>();
         
         try {
             if (rs == null) {
                 return null;
             }
             while (rs.next()) {
                 int mid = rs.getInt("mid");
                 Timestamp ocr = rs.getTimestamp("occurrence");
                 String name = rs.getString("name");
                 meals.add(new Meal.List(name, mid, ocr));
             }
         } catch (SQLException e) {
             return null;
         }
         
         return (Meal.List[])meals.toArray(new Meal.List[1]);
     }
     
     /* Returns an array of meals that are available to the user to add to
        the history or favorites. */
     public Meal.List[] getAvailableMeals(String s) {
 		s = Algorithm.Clean(s);
 		ResultSet rs = db.execute("select mid, name from meals where uid = 0" +
 		    " or uid = " + this.uid + " and name like '%" + s + "%';");
     	
     	ArrayList<Meal.List> mla = new ArrayList<Meal.List>();
 
     	try {
     	    if (rs == null) {
     	        return (Meal.List[])mla.toArray(new Meal.List[1]);
     	    }
     	    while (rs.next())
     	    {
 			    String name = rs.getString("name");
 			    int mid = rs.getInt("mid");
 			    mla.add(new Meal.List(name, mid, null));
 		    }
 		} catch (SQLException e) {
 		    return null;
 		}
 		return (Meal.List[])mla.toArray(new Meal.List[1]);
     }
     
     /* Add meal to history */
     public boolean addMeal(Meal m, Timestamp occurrence) {
     
 		int r = db.update("insert into history (mid, uid, occurrence) values (" 
			+ m.getMid() + "," + this.uid + "," + occurrence + ");");
 		
 		if (r < 1) {
 		    return false;
 		}
 		
 		return true;
     }
     
     /* Remove meal from history. */
     public boolean removeMeal(Meal m, Timestamp occurrence) {
     
 		int r = db.update("delete from history where mid = " + m.getMid() 
 			+ " and uid = " + this.uid + " and occurrence = '" + 
			occurrence + "';");
 			
 	    if (r < 1) {
 	        return false;
 	    }
 		return true;
     }
 
 }
