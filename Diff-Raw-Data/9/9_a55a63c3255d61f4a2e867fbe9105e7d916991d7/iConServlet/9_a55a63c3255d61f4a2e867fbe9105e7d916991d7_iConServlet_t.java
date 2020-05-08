 package icon;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import server.iConAddress;
 import server.iConServer;
 import tester.iConWeb;
 
 /**
  * Servlet implementation class iConServlet
  */
 public class iConServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public iConServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
     private String getLoginID(String query){
     	int indexStart = query.indexOf("?logid=");
     	int indexEnd = query.indexOf("?latit=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     
     private String getLatitude(String query){
     	int indexStart = query.indexOf("?latit=");
     	int indexEnd = query.indexOf("?lngit=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     
     private String getLongitude(String query){
     	int indexStart = query.indexOf("?lngit=");
     	int indexEnd = query.indexOf("?radii=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     
     private String getRadius(String query){
     	int indexStart = query.indexOf("?radii=");
     	int indexEnd = query.indexOf("?opera=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     /*
     private String getOperation(String query){
     	int indexStart = query.indexOf("?opera=");
     	int indexEnd = query.indexOf("&sid=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     */
     
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
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
 		//System.out.println(queryString);
 		String id = getLoginID(queryString);
 		String lat = this.getLatitude(queryString);
 		String lng = this.getLongitude(queryString);
 		String rad = getRadius(queryString);
 		//String op = getOperation(queryString);
 		String address = request.getRemoteAddr();
 		/*
 		 * Update my position
 		 */
 		
 		int userkey=dbtoicon.get(id);
 		//System.out.println("attempt to move user "+userkey+" to "+lat+" "+lng);
 		iconweb.moveUser("rigi-lab-03.cs.uvic.ca", address, (new Double(lat)).doubleValue(), (new Double(lng)).doubleValue(), userkey);
 		//System.out.println("Move complete");
 		//iconserver.showlocationtree();
 		/*
 		 * Get the new cover tree of this position specified
 		 */
 		//int userkey=dbtoicon.get(id);
 		String coverres = iconweb.getCoverage("rigi-lab-03.cs.uvic.ca", address, (new Double(lat)).doubleValue(), (new Double(lng)).doubleValue(), userkey, iconserver.metersToDegree((new Double(rad)).doubleValue())  );
 		//System.out.println("Cover keys are " + coverres);
 		String responsePositions = new String();
 		StringTokenizer st = new StringTokenizer(coverres);
 		while(st.hasMoreTokens()){
 			int key = (new Integer(st.nextToken()));
 			iConAddress addr = iconserver.getUserAddress(key);
 			responsePositions+=addr.getLatitude()+" "+addr.getLongitude()+" ";
 		}
 		//System.out.println(id+" got Positions from keys are "+responsePositions);
 		response.getWriter().write(responsePositions);
 		response.getWriter().close();
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
