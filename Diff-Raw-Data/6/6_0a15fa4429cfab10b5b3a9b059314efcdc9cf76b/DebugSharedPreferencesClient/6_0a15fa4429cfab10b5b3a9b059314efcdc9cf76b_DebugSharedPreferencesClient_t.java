 package jp.knct.di.c6t.communication;
 
import java.util.LinkedList;
 import java.util.List;
 
 import jp.knct.di.c6t.model.Route;
 import jp.knct.di.c6t.model.User;
 
 public class DebugSharedPreferencesClient implements Client {
 
 	@Override
 	public List<Route> getRoutes(User user) {
 		// TODO Auto-generated method stub
		return new LinkedList<Route>();
 	}
 }
