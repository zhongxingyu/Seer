 package yakitengine;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import server.iConServer;
 import tester.iConWeb;
 
 import yakitdb.YaKitDerbyDB;
 
 /**
  * Servlet implementation class LoginServlet
  */
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
     //private HashMap<String,Integer> dbtoicon = new HashMap<String,Integer>();
     /**
      * @see HttpServlet#HttpServlet()
      */
     public LoginServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
     private String getLatitude(String query){
     	int indexStart = query.indexOf("?latit=");
     	int indexEnd = query.indexOf("?logit=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     
     private String getLongitude(String query){
     	int indexStart = query.indexOf("?logit=");
     	int indexEnd = query.indexOf("?logid=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     
     private String getLoginID(String query){
     	int indexStart = query.indexOf("?logid=");
     	int indexEnd = query.indexOf("?logun=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     
     private String getLoginUN(String query){
     	int indexStart = query.indexOf("?logun=");
     	int indexEnd = query.indexOf("?logpw=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     
     private String getLoginPW(String query){
     	int indexStart = query.indexOf("?logpw=");
     	int indexEnd = query.indexOf("&sid=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     
 	/**
 	 * @see HttpServlet#doGet(HttpServletReq uest request, HttpServletResponse response)
 	 */
 	@SuppressWarnings("unchecked")
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		iConServer iconserver = (iConServer) this.getServletContext().getAttribute("iconserver");
 		if(iconserver==null){
 			//response.getWriter().write("iconserver is null");
 			this.getServletContext().setAttribute("iconserver", iconserver = iConServer.getInstance());
			iconserver.intialize(765, 6);
 			iconserver.setUrl("rigi-lab-03.cs.uvic.ca");
 		}else{
 			//response.getWriter().write("iconserver is avaiil");
 		}
 		
 		iConWeb iconweb = (iConWeb) this.getServletContext().getAttribute("iconweb");
 		if(iconweb==null){
 			//response.getWriter().write("iconweb is null");
 			this.getServletContext().setAttribute("iconweb", iconweb = iConWeb.getInstance());
 			iconweb.addServer(iconserver);
 		}else{
 			//response.getWriter().write("iconweb is avaiil");
 		}
 		
 		HashMap<String,Integer> dbtoicon = (HashMap<String, Integer>) this.getServletContext().getAttribute("dbtoicon");
 		if(dbtoicon==null){
 			this.getServletContext().setAttribute("dbtoicon", dbtoicon = new HashMap<String,Integer>() );
 		}
 		
 		// TODO Auto-generated method stub
 		String queryString = request.getQueryString();
 		queryString = queryString.replaceAll("%20", " ");
 		String id = getLoginID(queryString);
 		String un = getLoginUN(queryString);
 		String pw = getLoginPW(queryString);
 		String lat = this.getLatitude(queryString);
 		String lng = this.getLongitude(queryString);
 		String address = request.getRemoteAddr();
 		System.out.println("Login Request "+id+" un "+un+" pw "+pw+" from "+address+" lat "+lat+" lng "+lng);
 		YaKitDerbyDB database = YaKitDerbyDB.getInstance();
 		database.initialize();
 		if(id==null||id.equals("null")){
 			System.out.println("Setup Database entry for Guest");
 			//id = database.insertUser("guest","guest",1,address,""+lat,""+lng,1);
 			id = database.insertUser("guest","guest",1);
 			int userkey = iconweb.addUser("rigi-lab-03.cs.uvic.ca", address, (new Double(lat)).doubleValue(), (new Double(lng)).doubleValue());
 			iconserver.showlocationtree();
 			dbtoicon.put(id, userkey);
 		}else{
 			if(!un.equals("null")&&!pw.equals("null")){
 				System.out.println("Verify "+un+" pw "+pw);
 				boolean valid = database.verify(un,pw);
 				if(valid){
 					System.out.println(""+un+" pw "+pw+" is valid, ck to remove guest login");
 					boolean sameid = database.checkids(id,un);
 					if(sameid){
 						database.updateUserState(id,3);
 						//database.updateUserLocation((new Integer(id)).intValue(),address,""+lat,""+lng,1);
 					}else{
 						System.out.println("REmove guest login for user");
 						database.removeUserID(id);
 						int key = dbtoicon.remove(id);
 						iconweb.removeUser("rigi-lab-03.cs.uvic.ca", key);
 						id=database.getUserID(un)+"";
 						int userkey = iconweb.addUser("rigi-lab-03.cs.uvic.ca", address, (new Double(lat)).doubleValue(), (new Double(lng)).doubleValue());
 						iconserver.showlocationtree();
 						dbtoicon.put(id, userkey);
 					}
 				}else{
 					id="-1";
 				}
 			}else{
 				id="-1";
 			}
 		}
 		database.showTable("users");database.showTable("profiles");database.showTable("locations");
 		response.getWriter().write(id);
 		response.getWriter().close();
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
