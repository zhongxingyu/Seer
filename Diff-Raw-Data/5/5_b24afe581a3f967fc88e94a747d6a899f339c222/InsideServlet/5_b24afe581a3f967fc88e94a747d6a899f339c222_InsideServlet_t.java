 package indexapp;
 
 import java.io.*;
 import java.util.*;
 import java.lang.reflect.Type;
 import javax.servlet.http.*;
 import com.google.appengine.api.memcache.*;
 import com.google.gson.*;
 import com.google.gson.reflect.*;
 
 @SuppressWarnings("serial")
 public class InsideServlet extends HttpServlet{
 	private final int[][] passwdF={{0,0}};
 	private final int[][] passwdS={{0,0}};
 	public void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException{
 		int index;
 		
 		MemcacheService ms;
 		
 		Gson gson;
 		Type pointObjType;
 		List<Point> pointList;
 		Point point;
 		
 		String key;
 		Cookie cookie;
 		
 		return;
 		
		/*ms = MemcacheServiceFactory.getMemcacheService();
 		
 		if(req.getServerPort() != 443){
 			return;
 		}
 		
 		try{
 			gson = new Gson();
 			pointObjType = new TypeToken<List<Point>>(){}.getType();
 			pointList = gson.fromJson(req.getParameter("laughtext"),pointObjType);
 			
 			for(index = 0;index < 1;index++){
 				point = pointList.get(index);
 				if(point.x < passwdF[index][0] || point.x > passwdS[index][0] || point.y < passwdF[index][1] || point.y > passwdS[index][1]){
 					resp.sendRedirect("/in.html");
 					return;
 				}
 			}
 		
 			key = String.valueOf(new Date().getTime()) + String.valueOf(Math.random());
 			ms.put(key,true,Expiration.byDeltaSeconds(3600));
 			cookie = new Cookie("inside",key);
 			cookie.setMaxAge(-1);
 			cookie.setSecure(true);
 			resp.addCookie(cookie);
 			resp.sendRedirect("/gallery.html");
 		}catch(Exception e){
 			resp.sendRedirect("/in.html");
		}*/
 	}
 }
 
 class Point{
 	public int x;
 	public int y;
 }
