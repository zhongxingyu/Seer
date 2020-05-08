 package com.ids.controllers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.util.Streams;
 import org.json.JSONException;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 
 
 
 import com.google.appengine.api.rdbms.AppEngineDriver;
 import com.google.apphosting.api.DeadlineExceededException;
 import com.google.cloud.sql.jdbc.PreparedStatement;
 import com.ids.businessLogic.DropdownInterface;
 import com.ids.context.GetBeansFromContext;
 import com.ids.user.User;
 
 
 
 @Controller
 @Transactional
 public class SetupController {
 
 	private Connection con;
 	
 	private final static Logger logger = Logger.getLogger(SetupController.class.getName()); 
 	
 	private  boolean viaGet = false;
 	
    // static final Logger logger = LoggerFactory.getLogger(SetupController.class);
     
     
 	 @Transactional
 	 @RequestMapping(value="/setup2", method = RequestMethod.POST)
    public String save(
 		   HttpServletResponse response,  HttpServletRequest request,
                    ModelMap map) throws SQLException,  IOException  {
 
 
     	
   		 HttpSession session = request.getSession(true);
 
 	     User user =(User) session.getAttribute("myUser");
     	
 		 if (user==null || !user.getAccess().equals("a")) {
 			 return "redirect:/login";
 		 }
 		 GetBeansFromContext gcfc = new GetBeansFromContext();
 		 con = gcfc.myConnection();
 		 con.setAutoCommit(false);
 		 
 		 
 		 String access = request.getParameter("access");
 		 
 	    	String multiplier="";
 	    	if (access.equals("c")) {
 	    		multiplier="*10000";
 	    	}
 	    	if (access.equals("i")) {
 	    		multiplier="*200000";
 	    	}
 	    	
 
 	//	 Statement statement1 = con.createStatement();
    
     	
 
     	ServletFileUpload upload = new ServletFileUpload();
 
 
     	try{
     	FileItemIterator iter = upload.getItemIterator(request);
 
     	boolean fileTypeFound = false;
     	String fileType="";
     	
 
     	  
     	while (iter.hasNext()) {
 
     	    FileItemStream item = iter.next();
     	    String name = item.getFieldName();
     	    if (name.equals("submitBtn")){
     	    	fileType="none";
     	    }
     	    InputStream stream = item.openStream();
     	    if (!fileTypeFound)
     	     {
     	    //	logger.warning("Form field " + name + " with value "
     	    //        + Streams.asString(stream) + " detected.");
     	    	fileTypeFound=true;
     	    	fileType = Streams.asString(stream);
     	    	
     	    	if (fileType.equals("resetOthers")){
     	    		
     	    		PreparedStatement statement = (PreparedStatement) con.prepareStatement("DELETE FROM " +
                             "   Facts_"+access+" where companyId < 0 and access='"+request.getParameter("access")+"' ");
                     statement.executeUpdate();
                      con.commit();
     	    		
     	    		 statement = (PreparedStatement) con.prepareStatement("DELETE FROM " +
     	    				                                                       "   Company where id < 0 and access='"+request.getParameter("access")+"' ");
     	    		 
     	    		statement.executeUpdate();
     	    		con.commit();
     	    		
         	    	String sql = " Insert into Company (Name, ShortName, Id, access ) values ('Others' , 'OTH' , -1"+multiplier+" ,'"+request.getParameter("access")+"' ) ";
                     statement = (PreparedStatement) con.prepareStatement(sql);
 
                     statement.executeUpdate();
                     con.commit();
         	    	map.addAttribute("displaytype2","block");
         	    	map.addAttribute("displaytype","none");
      			   map.addAttribute("successtext","Other Companies successfully deleted - ready for load!");
      			   return  "setup";
     	    		
     	    	}
     	    	if (fileType.equals("facts")) { 
     	    		map.addAttribute("fileType","factsPARTDONE");
     	    		
     	        
     	    		
     	    		PreparedStatement statement = (PreparedStatement) con.prepareStatement("DELETE FROM Facts_"+access+"   where access='"+request.getParameter("access")+"' ");
     	    		statement.executeUpdate();
     	    		con.commit();
     	     }
     	    } else {
 
     	    	if (fileType.contains("facts")){
     	    	BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
    	    	
     	    	String sql = "INSERT INTO Facts_"+access+"  (companyId, countryid, productid, year, sales_production, quantity, flag, access) " +
     	    			" values (?"+multiplier+",?"+multiplier+",?"+multiplier+",?,?, ?, ?,?)";
     	    	logger.warning("current sql: "+sql);
     	    //	String sql = "INSERT INTO Product (Name, Shortname, Id, SortOrder ) values (? , ? , ? ,? ) ";
                 PreparedStatement statement = (PreparedStatement) con.prepareStatement(sql);
 
 
     	    	
     	    	//String query1 = item.getName();
     	    	String sCurrentLine= null;
     	    	int commitCount=0;
     	    	int totalCount= 0;
     	        int upTo10K =0;
     	        int readToTotalCount=0;
     	        int commitCount1=0;
     	    	while ((sCurrentLine = br.readLine()) != null) {
     	    		
     	    		readToTotalCount+=1;
     	    		
     	    		if ( totalCount >=readToTotalCount ) {
     	    			
     	    		}else{
     	    		
     	    		commitCount+=1;
     	    		commitCount1+=1;
     	    		String[] parms = sCurrentLine.split("\t");
    	    		logger.warning(sCurrentLine);
     	    		 statement.setString(1,  parms[0]);
     	             statement.setString(2,  parms[1]);
     	             statement.setInt(3,  Integer.parseInt(parms[2]));
     	             
     	    		 statement.setInt(4,  Integer.parseInt(parms[3]));
     	             statement.setInt(5,  Integer.parseInt(parms[4]));
     	             statement.setString(6, parms[5]);
     	             if (parms.length> 6){
     	    		    statement.setString(7,  parms[6]);
     	             } else {
     	            	 statement.setString(7,  " "); 
     	             }
     	             statement.setString(8,access);
     	             statement.addBatch();
     	             
     	    	//	statement.executeUpdate();
     	    		if (commitCount1>=5000) {
     	    			statement.executeBatch();
     	    			con.commit();
     	    			commitCount1=0;
     	    			
     	    			 upTo10K += commitCount;
 
     	    			
     	    		}
     	    		} //end of silly else
     	    		
     			 
     	    	}
     	    	map.addAttribute("displaytype2","block");
     	    	map.addAttribute("displaytype","none");
  			   map.addAttribute("done",commitCount);
  			  map.addAttribute("fileType","factsPARTDONE");
  			   map.addAttribute("successtext"," records committed. LOAD COMPLETE!");
  			  statement.executeBatch();
     	    	con.commit();
     	        // Process the input stream
     	    	}
     	    	
     	    	
     	    	
     	    	
     	    	
     	    	if (fileType.contains("otherFacts")){
     	    		
     	    		String sql = "INSERT INTO Company (name, shortname, Id, access) values ('[Others]','OTH',-1"+multiplier+",'"+access+"') ";
         	    	Statement statement11 = con.createStatement();
     	    		statement11.executeUpdate(sql);
     	    		
         	    	BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
 
         	    	
         	    	
         	    	 sql = "INSERT INTO Facts_"+access+"  (companyId, countryid, productid, year, sales_production, quantity, flag, access) " +
         	    			" values (?"+multiplier+",?"+multiplier+",?"+multiplier+",?,?, ?, ?, ?)";
         	    //	String sql = "INSERT INTO Product (Name, Shortname, Id, SortOrder ) values (? , ? , ? ,? ) ";
                     PreparedStatement statement = (PreparedStatement) con.prepareStatement(sql);
 
 
         	    	
         	    	//String query1 = item.getName();
         	    	String sCurrentLine= null;
         	    	int commitCount=0;
         	    //	int totalCount= Integer.parseInt(name.replace("myfile",""));
         	        int upTo10K =0;
         	        int readToTotalCount=0;
         	        int commitCount1=0;
         	    	while ((sCurrentLine = br.readLine()) != null) {
         	    		
         	    		readToTotalCount+=1;
         	    		
         	    	//	if ( totalCount >=readToTotalCount ) {
         	    			
         	    	//	}else{
         	    		
         	    		commitCount+=1;
         	    		commitCount1+=1;
         	    		String[] parms = sCurrentLine.split("\t");
         	    		 statement.setString(1,  parms[0]);
         	             statement.setString(2,  parms[1]);
         	             statement.setInt(3,  Integer.parseInt(parms[2]));
         	             
         	    		 statement.setInt(4,  Integer.parseInt(parms[3]));
         	             statement.setInt(5,  Integer.parseInt(parms[4]));
         	             statement.setString(6, parms[5]);
         	             if (parms.length> 6){
         	    		    statement.setString(7,  parms[6]);
         	             } else {
         	            	 statement.setString(7,  " "); 
         	             }
         	             statement.setString(8," ");
         	             statement.addBatch();
         	             
         	    	//	statement.executeUpdate();
         	    		if (commitCount1>=3000) {
         	    			statement.executeBatch();
         	    			con.commit();
         	    			commitCount1=0;
         	    			
         	    			 upTo10K += commitCount;
 
         	    			
         	    		}
         	    	//	} //end of silly else
         	    		
         			 
         	    	}
         	    	map.addAttribute("displaytype2","block");
         	    	map.addAttribute("displaytype","none");
      			   map.addAttribute("done",commitCount);
      			  map.addAttribute("fileType","factsPARTDONE");
      			   map.addAttribute("successtext"," records committed. LOAD COMPLETE!");
      			  statement.executeBatch();
         	    	con.commit();
         	    	
         	    	   return  "setup";
         	        // Process the input stream
         	    	}
         	    	
         	    	
     	    	
     	    	
     	    	
     	    	if (fileType.equals("products")){
     	    		
     	    		PreparedStatement statement = (PreparedStatement) con.prepareStatement("DELETE FROM Product where access='"+request.getParameter("access")+"' ");
     	    		statement.executeUpdate();
     	    		con.commit();
     	    		
     	    	BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
     	    	
     	    	String sql = "INSERT INTO Product (Name, Shortname, Id, SortOrder, access ) values (? , ? , ?"+multiplier+" ,? ,?) ";
                 statement = (PreparedStatement) con.prepareStatement(sql);
 
     	    	String sCurrentLine= null;
                 int count=0;
     	    	while ((sCurrentLine = br.readLine()) != null) {
 
     	    		String[] parms = sCurrentLine.split("\t");
     	    		 if (parms.length > 2) {
     	    		 statement.setString(1,  parms[0]);
     	             statement.setString(2,  parms[1]);
     	             statement.setInt(3,  Integer.parseInt(parms[2]));
     	             
     	    		 statement.setInt(4,  Integer.parseInt(parms[3]));
     	            
     	    		 statement.setString(5, request.getParameter("access"));
     	    		statement.executeUpdate();
     	    		count+=1;
     	    		 }
 
     	    	}
 
     	    	con.commit();
     	    	map.addAttribute("displaytype2","block");
     	    	map.addAttribute("displaytype","none");
  			   map.addAttribute("successtext",count+" Products committed. COMPLETE!!!");
  			   return  "setup";
     	        // Process the input stream
     	    	}
     	    	
     	    	
     	    	
     	    	
     	    	
     	    	
     	    	
     	    	if (fileType.equals("countries")){
     	    		
     	    		PreparedStatement statement = (PreparedStatement) con.prepareStatement("DELETE FROM Country where  access='"+request.getParameter("access")+"' ");
     	    		statement.executeUpdate();
     	    		con.commit();
     	    		
     	    	BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
     	    	
     	    	String sql = "INSERT INTO Country (Country, ShortName, Id, SortOrder, access ) values (? , ? , ?"+multiplier+" ,? ,?) ";
                 statement = (PreparedStatement) con.prepareStatement(sql);
 
     	    	String sCurrentLine= null;
 
     	    	int count=0;
     	    	while ((sCurrentLine = br.readLine()) != null) {
 
     	    		String[] parms = sCurrentLine.split("\t");
     	    		 statement.setString(1,  parms[0]);
     	             statement.setString(2,  parms[1]);
     	             if ( Integer.parseInt(parms[2])==0) {
     	            	 continue;
     	             }
     	             statement.setInt(3,  Integer.parseInt(parms[2]));
     	             
     	    		 statement.setInt(4,  Integer.parseInt(parms[3]));
     	    		 statement.setString(5, request.getParameter("access"));
     	            
     	    		statement.executeUpdate();
     	    		count+=1;
 
     	    	}
 
     	    	con.commit();
     	    	map.addAttribute("displaytype2","block");
     	    	map.addAttribute("displaytype","none");
  			   map.addAttribute("successtext",count+" Countries committed. COMPLETE!!!");
  			   return  "setup";
     	        // Process the input stream
     	    	}
     	    	
     	    	
     	    	
     	
     	    	
     	    	
     	    	
     	    	if (fileType.equals("companies")){
     	    		
     	    		PreparedStatement statement = (PreparedStatement) con.prepareStatement("DELETE FROM Company  where  access='"+request.getParameter("access")+"' ");
     	    		statement.executeUpdate();
     	    		con.commit();
     	    		
     	    	BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
     	    	
     	    	String sql = "INSERT INTO Company (Name, ShortName, Id, access ) values (? , ? , ?"+multiplier+" ,? ) ";
                 statement = (PreparedStatement) con.prepareStatement(sql);
 
     	    	String sCurrentLine= null;
 
     	    	int count=0;
     	    	while ((sCurrentLine = br.readLine()) != null) {
     	    		logger.warning("curr line: "+sCurrentLine);
     	    		String[] parms = sCurrentLine.split("\t");
     	    		logger.warning("length: "+parms.length);
     	    		if (parms.length==0)
     	    			continue;
     	    		 statement.setString(1,  parms[0]);
     	             statement.setString(2,  parms[1]);
     	             statement.setInt(3,  Integer.parseInt(parms[2]));
     	             statement.setString(4, request.getParameter("access"));
     	    		statement.executeUpdate();
     	    		count+=1;
 
     	    	}
     	    	
     	    	
     	    	int other=0;
     	    	con.commit();	   
     	    	map.addAttribute("displaytype2","block");
     	    	map.addAttribute("displaytype","none");
  			   map.addAttribute("successtext"," Companies committed. COMPLETE!!!");
  			   return  "setup";
     	        // Process the input stream
     	    	}
     	    	
     	    	
     	    	
     	 
     	    	
     	    	
     	    	
     	    }
     	}
     	}
     	catch(Exception e ){
     		logger.warning("FAILED");
     		e.printStackTrace();
     		 logger.log(Level.SEVERE, e.toString(), e);
  	    	map.addAttribute("displaytype2","none");
  	    	map.addAttribute("displaytype","none");
     	}
 	
     	
     
 
 
 
 
 	//   String query1 = "INSERT INTO Country (country,shortname,id,sortorder) VALUES ('SPAIN','SPA',2,14)";
 
 
 	  //    statement1.executeUpdate(query1);
 	      con.close() ; 
 	     
 
 	   //   map.addAttribute("successtext"," records committed. COMPLETE!!!");
 	   // 	map.addAttribute("displaytype2","block");
 	   // 	map.addAttribute("displaytype","none");
 
 return "setup";
 		 
 	 }
     
 	 @Transactional
 	 @RequestMapping(value="/setup2", method = RequestMethod.GET)
 	public String postMethodTwo(
            HttpServletResponse response,
 			HttpServletRequest request,
 			ModelMap model) throws IOException, JSONException, SQLException {
 		 
 		 logger.warning("Entering application via GET");
 
 
 	    	logger.warning("FIRST TIME");
 	    	model.addAttribute("rowCount",0);
 			   model.addAttribute("displaytype","none");
 			   model.addAttribute("displaytype2","none");
 			   model.addAttribute("fileType","facts");
  
 				 GetBeansFromContext gcfc = new GetBeansFromContext();
 				 con = gcfc.myConnection();
 				 con.setAutoCommit(false);
 				 
 				   model.addAttribute("ajaxPrefix",gcfc.ajaxURLprefix());
 				 
 			      Statement statement = con.createStatement();
 
 			      ResultSet resultSet = null;
 			      String query = "select userId from ids_users order by userId asc ";
 			      String options="";
 				  resultSet = statement.executeQuery(query);
 
 
 				   while (resultSet.next()) {
 					   options += "<option>"+resultSet.getString("userId")+"</option>";
 				   }
 
 				   model.addAttribute("options",options);
 				   
 
     	return "setup";
 	 }
 	
 }
 
