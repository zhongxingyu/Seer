 package servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import manager.*;
 import model.*;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.codec.binary.Base64;
 
 import javax.crypto.spec.SecretKeySpec;
 
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.crypto.*;
 
 import java.net.URLEncoder;
 
 public class LoginServlet extends HttpServlet {
 
 	/*********************************************************************************
 	 * change these final variable settings, and implement ur redirection in line 98
 	 ********************************************************************************/
 	private static final String SECRET_KEY = "mstest2012";
 	private static final String DOMAIN_NAME = "http://202.161.45.127";
 	// set this to true if doing local testing and your port number is not 80
 	private static final boolean INCLUDE_PORT = false;
 	
 
 	private static final String[] keys = { "oauth_callback",
 			"oauth_consumer_key", "oauth_nonce", "oauth_signature_method",
 			"oauth_timestamp", "oauth_version", "smu_domain", "smu_fullname",
 			"smu_groups", "smu_username" };
 	
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		
 		String link = "index.jsp";
 		
 		HttpSession session = request.getSession();
 		
 		//request.getSession().invalidate();
 		PrintWriter out = response.getWriter();
 		response.setContentType("text/plain");
 
 		
 		String callbackUrl = DOMAIN_NAME + (INCLUDE_PORT?":" + request.getServerPort():"")
 				+ request.getRequestURI();
 		String uri = "POST&" + encode(callbackUrl) + "&";
 
 		String pairs = "";
 		for (int i = 0; i < keys.length - 1; i++) {
 			pairs += keys[i] + "=" + encode(request.getParameter(keys[i]))
 					+ "&";
 		}
 		// the last key-value pair doesnt need the trailing & character
 		pairs += keys[keys.length - 1] + "="
 				+ encode(request.getParameter(keys[keys.length - 1]));
 
 		uri += encode(pairs);
 
 		// initialize the Mac object
 		Mac mac = null;
 		try {
 			mac = Mac.getInstance("HmacSHA1");
 		} catch (NoSuchAlgorithmException e1) {
 			e1.printStackTrace();
 		}
 
 		SecretKey secretKey = new SecretKeySpec((SECRET_KEY + "&").getBytes(),
 				mac.getAlgorithm());
 		try {
 			mac.init(secretKey);
 		} catch (InvalidKeyException e) {
 			e.printStackTrace();
 		}
 
 		// generates the signature & retrieves server signature
 		String generatedSignature = new String(Base64.encodeBase64(mac
 				.doFinal(uri.getBytes())), "UTF-8").trim();
 		String serverSignature = (request.getParameter("oauth_signature"));
 
 		// gets the time when the server generates the signature
 		long serverSignatureTime = Long.parseLong(request
 				.getParameter("oauth_timestamp"));
 
 		// gets the current time in seconds
 		long currentTime = System.currentTimeMillis() / 1000;
 
 		// ensure that the difference between your local timestamp and
 		// the incoming timestamp fall within an acceptable interval (e.g. 30
 		// seconds).
 		// You may adjust the allowance interval according to the time
 		// difference
 		// between your server and the API server. In general, it is not
 		// recommended
 		// to set the interval more than 120 seconds (i.e. +/- 120 seconds)
 		//System.out.println(serverSignature);
 		//System.out.println(generatedSignature);
 		//System.out.println(callbackUrl);
 		if (serverSignature.trim().equals(generatedSignature.trim())) {
 
 			String username = request.getParameter("smu_username");
 			/**************************************************
 			 * IF INSIDE THIS BLOCK, USER IS AUTHENTICATED
 			 * INSERT YOUR CODE TO BE PERFORMED WHEN AUTHENTICATED	
 			 *************************************************/
 			UserDataManager udm = new UserDataManager();
 			
 			
 				session.setAttribute("username", username);
 				session.setAttribute("fullname", request.getParameter("smu_fullname"));
 			
 				String loginUser = request.getParameter("smu_username");
 				
 				
 				String type = "Student";
 				try {
 					
 
 					if(udm.isSuspended(username)){
 						session.setAttribute("message","You have  been suspended. Please contact the administrator for more details");
 						session.removeAttribute("username");
 						session.removeAttribute("fullname");
 						link = "index.jsp";
 					}else{
 						
 						User u = udm.retrieve(loginUser);
 						
 						if (username.matches(".*\\d.*")) {
 							type = "Student";
 						} else {
 							type = "Faculty";
 						}
 						
 						if (u == null) {
 							link = "details.jsp";
 						} else {
 							type = u.getType();
							link = "mainPage.jsp";
 						}
 						
 						session.setAttribute("type", type);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					System.out.println("Invalid Login");
 				}
 			
 		}else{
 			System.out.println();
 		}
 		RequestDispatcher rd = request.getRequestDispatcher(link);
 		rd.forward(request, response);
 	}
 
 	public static String encode(String plain) {
 		try {
 			String encoded = URLEncoder.encode(plain, "UTF-8");
 
 			return encoded.replace("*", "%2A").replace("+", "%20")
 					.replace("%7E", "~");
 		} catch (Exception e) {
 			e.printStackTrace(); // hopefully this wont happen
 		}
 		return "";
 	}
 
 }
