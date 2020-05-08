 package com.modelmetrics;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 public class CreateOrderServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -7130469657428131968L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doPost(req, resp);
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		try {
 			Configuration config = (Configuration)req.getSession().getAttribute("configuration");
 			if(config == null || config.getAccessToken() == null || config.getAccessToken().length() <= 10) {
 				resp.sendRedirect("index.jsp");
 			} else { 
 				String interior = req.getParameter("interior");
 				double totalPrice = Double.parseDouble(req.getParameter("totalPrice"));
 				String imageLink = req.getParameter("vehImg");
 				String options = req.getParameter("options");
 				String signature = req.getParameter("signature");
 				
 				String orderData = " {  \"Interior__c\" : \"" + interior + "\", \"Total_Price__c\" : " 
 					+ totalPrice + ", \"ImageLink__c\" : \"" + imageLink +"\", \"Options__c\" : \"" + options + "\" } ";
 				String url = config.getEndpoint() + "sobjects/Sales_Record__c";
 				//post data to salesforce
 	            String orderId = postSalesforceData(url, orderData, config.getAccessToken());
 	            
 	            //post signature to salesforce
 	            String signatureData = " {  \"Body\" : \"" + signature + "\", \"ParentId\" : \"" + orderId + "\", \"Name\" : \"Signature.png\", \"ContentType\" : \"image/png\" } ";
 	            url = config.getEndpoint() + "sobjects/Attachment";
 	            String response = postSalesforceData(url, signatureData, config.getAccessToken());
 	            resp.getWriter().write(response);
 			}
 		}
 		catch(Exception ex) {
			ex.printStackTrace();
 			ex.printStackTrace(System.err);
 			resp.sendError(404, ex.getMessage());
 		}
 	}
 
 	
 	private String postSalesforceData(String url, String data, String accessToken) throws Exception {
         HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
         connection.setRequestMethod("POST");
         connection.addRequestProperty("Authorization","OAuth " + accessToken);
         connection.addRequestProperty("X-PrettyPrint", "1");
         connection.addRequestProperty("Content-Type", "application/json");
         connection.connect();
         OutputStream output = connection.getOutputStream();
         String encodeData = URLEncoder.encode(data, "UTF-8");
         output.write(encodeData.getBytes("UTF-8"));
         
         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         String inputLine;
         String response = "";
         while ((inputLine = in.readLine()) != null) {
         	response += inputLine;
         }
 
         in.close();
         connection.disconnect();
         
         return response;
 	}
 }
