 package yakitmessenging;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import server.iConServer;
 import tester.iConWeb;
 import yakitdb.YaKitDerbyDB;
 
 
 /**
  * Servlet implementation class MessengerServlet
  */
 public class TextMessengerServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public TextMessengerServlet() {
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
     
     private String getOperation(String query){
     	int indexStart = query.indexOf("?opera=");
     	int indexEnd = query.indexOf("?messg=");
     	return query.substring( (indexStart+7), indexEnd) ;
     }
     
     private String getMessage(String query){
     	int indexStart = query.indexOf("?messg=");
     	int indexEnd = query.indexOf("&sid=");//?messg
     	return query.substring( (indexStart+7), indexEnd) ;
     }
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@SuppressWarnings("unchecked")
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		MessageEngine engine = (MessageEngine) this.getServletContext().getAttribute("messageengine");
 		if(engine==null){
 			//response.getWriter().write("message engine is null");
 			this.getServletContext().setAttribute("messageengine", engine = new MessageEngine());
 			
 		}else{
 			//response.getWriter().write("message engine is avaiil");
 		}
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
 		String op = getOperation(queryString);
 		String msg = getMessage(queryString);
 		String address = request.getRemoteAddr();
 		/*
 		 * Update my position
 		 */
 		//System.out.println(queryString);
 		int userkey=dbtoicon.get(id);
 		String returnmessage="";
 		if(op.equals("sendmessage")){
 			/*
 			 * 3. if submit message
 		 * 		3a. get message body from request
 		 * 		3b. create a new TextMessage
 		 *      3c. add to MessageEngine and get msgkey back
 		 *      3d. Get List of User keys from QuadTree
 		 *      3e. Add message key to each user object
 		 *      3f. Create new Text message for sender to indicate how many users should get text
 		 *      3g. Add your message key to you object for next time you chk messages.
 			 */
 			System.out.println("Send "+msg+" at "+lat+" "+lng+" rad "+rad+" as user "+userkey);
 			TextMessage message= new TextMessage(Message.TEXT);
 			String usersspecname=YaKitDerbyDB.getInstance().getUserName(id);
 			message.setText(usersspecname+"-"+msg);
 			int msgkey=engine.addMessage(message);
 			String coverres = iconweb.getCoverage("rigi-lab-03.cs.uvic.ca", address, (new Double(lat)).doubleValue(), (new Double(lng)).doubleValue(), userkey, iconserver.metersToDegree((new Double(rad)).doubleValue())  );
 			StringTokenizer st = new StringTokenizer(coverres);
 			int msgcount=0;
 			while(st.hasMoreTokens()){
 				int key = (new Integer(st.nextToken()));
 				iconweb.addMessageKey("rigi-lab-03.cs.uvic.ca",key,msgkey,Message.TEXT);
 				msgcount++;
 			}
 			//TextMessage usrsendmsg = new TextMessage(Message.TEXT);
 			//usrsendmsg.setText(usersspecname+" success send msg to "+msgcount+" users");
 			//int slfmsgkey = engine.addMessage(usrsendmsg);
 			//iconweb.addMessageKey("rigi-lab-03.cs.uvic.ca",userkey,slfmsgkey,Message.TEXT);
 			returnmessage="message send to "+msgcount+" users";
 			//System.out.println(returnmessage);
 		}else if(op.equals("getmessages")){
 			//hopefully one at a time
 			/*
 			 *  * 4. if getting messages
 		 *      4a. use the userkey to access your object
 		 *      4b. remove all message keys in your object of type text
 		 *      4c. access MessageEngine and get text of each
 		 *      4d. send text back
 			 */
 			int ret[] =iconweb.getTextMessageKey("rigi-lab-03.cs.uvic.ca",userkey);
 			int countleft=ret[0];
 			int msgkey=ret[1];
 			TextMessage textMsg = (TextMessage) engine.getMessage(msgkey);
 			if(textMsg!=null){
 				//System.out.println("get messages "+textMsg.getText());
 				returnmessage=countleft+" "+textMsg.getText();
 			}else{
 				//System.out.println("get messages, there are no messages waiting");
 				returnmessage="";
 			}
 		}
		
		response.getWriter().write(returnmessage);
 		response.getWriter().close();
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
