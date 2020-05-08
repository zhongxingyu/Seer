 package com.test.rest.pkg.ws;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.UriInfo;
 
 import com.sun.jersey.api.json.JSONWithPadding;
 import com.test.rest.pkg.clinicaltrials.ClinicalTrials;
 import com.test.rest.pkg.clinicaltrials.ClinicalTrialsLoader;
 import com.test.rest.pkg.database.Database;
 import com.test.rest.pkg.database.Encrypt;
 import com.test.rest.pkg.database.PersistanceActions;
 import com.test.rest.pkg.misc.SendEmail;
 import com.test.rest.pkg.misc.UserInfo;
 
 
 @Path("/params")
 public class ClinicalTrialsWebServices {
 
 	// Allows to insert contextual objects into the class, 
 	// e.g. ServletContext, Request, Response, UriInfo
 	@Context
 	UriInfo uriInfo;
 	@Context
 	Request request;
 
 	@GET
 	@Path("/signout")
 	@Produces(MediaType.TEXT_XML)
 	public void signout(@Context HttpServletResponse servletResponse,@Context HttpServletRequest servletRequest) throws IOException {
 		HttpSession session = servletRequest.getSession(true);
 		session.removeAttribute("user_email");
 		servletResponse.sendRedirect("../../index.html");
 	}
 
 	@GET
 	@Path("/chart")
 	@Produces(MediaType.TEXT_XML)
 	public void showChart(@Context HttpServletResponse servletResponse,@Context HttpServletRequest servletRequest, @QueryParam("trial_id") String trial_id) throws IOException {
 		@SuppressWarnings("unused")
 		HttpSession session = servletRequest.getSession(true);
 		if(session.getAttribute("user_email") == null)
 			servletResponse.sendRedirect("../../loginFirst.html");
 	}
 
 	// Return the list of todos for applications
 	@GET
 	@Path("xmlGet")
 	@Produces({MediaType.APPLICATION_JSON})
 	public List<UserInfo> getParameters() throws Exception {
 		List<UserInfo> params = new ArrayList<UserInfo>();
 		Database database= new Database();
 		Connection connection = database.Get_Connection();
 		PersistanceActions project= new PersistanceActions();
 		project.getDBRecords(connection);
 		params.addAll(DefaultParam.instance.getModel().values());	  	
 
 		return params; 
 	}
 
 	@POST
 	@Path("/pref")
 	@Produces(MediaType.TEXT_HTML)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public void preferences(@FormParam("status") String status,
 			@FormParam("result") String result,
 			@FormParam("studyType") String studyType,
 			@FormParam("ageGroup") String ageGroup,
 			@FormParam("Phase1") String phase1,
 			@FormParam("PhaseII") String phaseII,
 			@FormParam("PhaseIII") String phaseIII,
 			@FormParam("PhaseIV") String phaseIV,
 			@FormParam("NIH") String NIH,
 			@FormParam("Industry") String industry,
 			@FormParam("federal") String federal,
 			@FormParam("University") String university,
 			@FormParam("tags") String tags,
 			@Context HttpServletResponse servletResponse,
 			@Context HttpServletRequest servletRequest) throws Exception {
 
 		HttpSession session = servletRequest.getSession(true);
 		if(session.getAttribute("user_email") == null)
 			servletResponse.sendRedirect("../../loginFirst.html");
 		else{
 			Database database= new Database();
 			Connection connection = null;
 			try{
 				connection = database.Get_Connection();
 				List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 				PersistanceActions project= new PersistanceActions();
 				System.out.println(session.getAttribute("user_email"));
 				System.out.println(studyType.toString());
 
 				project.updatePrefs(connection, status, result, studyType, ageGroup, phase1, phaseII, phaseIII, phaseIV, NIH, industry, federal, university, tags, session.getAttribute("user_email").toString(),1);
 				//Thread.sleep(1000);
 				trials = project.getSearchedTrials(connection,status,result,studyType,ageGroup,phase1,phaseII,phaseIII,phaseIV,NIH,industry,federal,university,tags);
 				session.setAttribute("advSearchedTrials", trials);
 				servletResponse.sendRedirect("../../getTrials.html");
 			}catch (Exception e) {
 				e.printStackTrace();
 			}finally{
 				if(connection!=null)
 					connection.close();
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@GET
 	@Path("advSearchRes")
 	@Produces({MediaType.APPLICATION_JSON})
 	public List<ClinicalTrials> getSearchedTrials(@Context HttpServletResponse servletResponse,@Context HttpServletRequest servletRequest) throws Exception {
 		HttpSession session = servletRequest.getSession(true);
 		if(session.getAttribute("user_email") == null)
 			servletResponse.sendRedirect("../../loginFirst.html");
 		else{
 			List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 			session = servletRequest.getSession(true);
 			trials = (List<ClinicalTrials>) session.getAttribute("advSearchedTrials");
 			return trials; 
 		}
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	@GET
 	@Path("trialRecs")
 	@Produces({MediaType.APPLICATION_JSON})
 	public List<ClinicalTrials> getSearchedTrialRecs(@Context HttpServletResponse servletResponse,@Context HttpServletRequest servletRequest) throws Exception {
 		List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 		HttpSession session = servletRequest.getSession(true);
 		if(session.getAttribute("user_email") == null)
 			servletResponse.sendRedirect("../../loginFirst.html");
 		else{
 			trials = (List<ClinicalTrials>) session.getAttribute("trialRecs");
 			return trials; 
 		}
 		return trials;
 	}
 
 	@POST
 	@Path("advSearch")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public List<ClinicalTrials> populateSearchedTrials(@FormParam("status") String status,
 			@FormParam("result") String result,
 			@FormParam("studyType") String studyType,
 			@FormParam("ageGroup") String ageGroup,
 			@FormParam("Phase1") String phase1,
 			@FormParam("PhaseII") String phaseII,
 			@FormParam("PhaseIII") String phaseIII,
 			@FormParam("PhaseIV") String phaseIV,
 			@FormParam("NIH") String NIH,
 			@FormParam("Industry") String industry,
 			@FormParam("federal") String federal,
 			@FormParam("University") String university,
 			@FormParam("tags") String tags,
 			@Context HttpServletResponse servletResponse,
 			@Context HttpServletRequest servletRequest) throws Exception {
 
 		HttpSession session = servletRequest.getSession(true);
 		if(session.getAttribute("user_email") == null)
 			servletResponse.sendRedirect("../../loginFirst.html");
 		else{
 			List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 			Database database= new Database();
 			Connection connection = null;
 			try{
 				connection = database.Get_Connection();
 				PersistanceActions project= new PersistanceActions();
 				trials = project.getSearchedTrials(connection,status,result,studyType,ageGroup,phase1,phaseII,phaseIII,phaseIV,NIH,industry,federal,university,tags);
 
 				session = servletRequest.getSession(true);
 				session.setAttribute("advSearchedTrials", trials);
 				servletResponse.sendRedirect("../../getTrials.html");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}finally{
 				if(connection!=null)
 					connection.close();
 			}
 
 			return trials; 
 		}
 		return null;
 	}
 
 	@POST
 	@Path("basicSearch")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public List<ClinicalTrials> populateBasicSearchedTrials(@FormParam("searchString") String searchString,
 			@Context HttpServletResponse servletResponse,
 			@Context HttpServletRequest servletRequest) throws Exception {
 
 		HttpSession session = servletRequest.getSession(true);
 		if(session.getAttribute("user_email") == null)
 			servletResponse.sendRedirect("../../loginFirst.html");
 		else{
 			List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 			Database database= new Database();
 			Connection connection = null;
 			try{
 				connection = database.Get_Connection();
 				PersistanceActions project= new PersistanceActions();
 				trials = project.getSearchedTrials(connection,searchString);
 
 				session = servletRequest.getSession(true);
 				session.setAttribute("advSearchedTrials", trials);
 
 				servletResponse.sendRedirect("../../getTrials.html");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}finally{
 				if(connection!=null)
 					connection.close();
 			}
 			return trials; 
 		}
 		return null;
 	}
 
 	@GET
 	@Path("getTrialRecs")
 	@Produces({MediaType.APPLICATION_JSON})
 	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public List<ClinicalTrials> getTrialRecords(@QueryParam("trialId") String trialId,
 			@Context HttpServletResponse servletResponse,
 			@Context HttpServletRequest servletRequest) throws Exception {
 
 		HttpSession session = servletRequest.getSession(true);
 		if(session.getAttribute("user_email") == null)
 			servletResponse.sendRedirect("../../loginFirst.html");
 		else{
 			List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 			Database database= new Database();
 			Connection connection = null;
 			try{
 				connection = database.Get_Connection();
 				PersistanceActions project= new PersistanceActions();
 				trials = project.getSearchedTrialRecords(connection,trialId);
 				session = servletRequest.getSession(true);
 				session.setAttribute("trialRecs", trials);
 
 				servletResponse.sendRedirect("../../getTrials.html");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}finally{
 				if(connection!=null)
 					connection.close();
 			}
 			return trials; 
 		}
 		return null;
 	}
 
 	@GET
 	@Path("load")
 	//@Produces({MediaType.APPLICATION_JSON})
 	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public void loadClinicalTrials(
 			@Context HttpServletResponse servletResponse,
 			@Context HttpServletRequest servletRequest) throws Exception {
 
 		ClinicalTrialsLoader.loadTrials();
 
 		try {
 			servletResponse.sendRedirect("../../loginPage.html");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 
 	@GET
 	@Path("xmlGetJsonp")
 	@Produces({"application/x-javascript"})
 	public JSONWithPadding getParametersjsonp(@QueryParam("callback") String callback) throws Exception{
 		List<UserInfo> params = new ArrayList<UserInfo>();
 		Database database= new Database();
 		Connection connection = null;
 		try{
 			connection = database.Get_Connection();
 			PersistanceActions project= new PersistanceActions();
 
 			project.getDBRecords(connection);
 			params.addAll(DefaultParam.instance.getModel().values());
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			if(connection!=null)
 				connection.close();
 		}
 		return new JSONWithPadding(params, callback);
 		//return (JSONWithPadding) params;
 	}
 
 	@GET
 	@Path("getTrials")
 	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
 	public List<ClinicalTrials> getClinicalTrials(@Context HttpServletResponse servletResponse,
 			@Context HttpServletRequest servletRequest) throws Exception {
 
 		HttpSession session = servletRequest.getSession(true);
 		if(session.getAttribute("user_email") == null)
 			servletResponse.sendRedirect("../../loginFirst.html");
 		else{
 			List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 			Database database= new Database();
 			Connection connection = null;
 			try{
 				connection = database.Get_Connection();
 				PersistanceActions project= new PersistanceActions();
 				trials=project.getTrialRecords(connection,null);
 			}catch(Exception e){
 				e.printStackTrace();
 			}finally{
 				if(connection!=null)
 					connection.close();
 			}
 			return trials; 
 		}
 		return null;
 	}
 
 	// to get the total number of records
 	@GET
 	@Path("/count")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String getCount() {
 		int count = DefaultParam.instance.getModel().size();
 		return String.valueOf(count);
 	}
 
 	@GET
 	@Path("/confirmRegistration")
 	@Produces(MediaType.TEXT_PLAIN)
 	public void confirmRegistration(@Context HttpServletRequest servletRequest,@Context HttpServletResponse servletResponse) throws IOException {
 		// MultivaluedMap<String,String> urlParameters = uriInfo.getQueryParameters();
 
 		HttpSession session = servletRequest.getSession(true);
 			String hashCode = uriInfo.getQueryParameters().toString();
 			boolean valid = PersistanceActions.validateConfirmationLink(hashCode); 
 			if (valid) 
 				try {
 					servletResponse.sendRedirect("../../regValid.html");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			else
 				try {
 					servletResponse.sendRedirect("../../regInvalid.html");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	}
 
 	@POST
 	@Path("/add")
 	@Produces(MediaType.TEXT_HTML)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public void register(@FormParam("id") String id,
 			@FormParam("name") String name,
 			@FormParam("dob") String dob,
 			@FormParam("email") String email,
 			@FormParam("pwd") String pwd,
 			@Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest) throws Exception {
 
 		HttpSession session = servletRequest.getSession(true);
 		pwd = Encrypt.encrypt(pwd);
 		System.out.println(name);
 		name=name.replace(" ", "_");
 		System.out.println(name);
 		//System.out.println("This is the Encrypted Password:     "+pwd);
 		UserInfo parm = new UserInfo(id,name,dob,email,"male",pwd);
 		Database database= new Database();
 		Connection connection = database.Get_Connection();
 		PersistanceActions project= new PersistanceActions();
 		project.setDBRecords(connection,parm);
 		project.setPrefs(connection, "NULL","NULL", "NULL", "NULL", "NULL", "NULL", "NULL", "NULL", "NULL", "NULL", "NULL", "NULL", "NULL", email,0);
 
 		SendEmail.send(email, "http://localhost:8080/ClinicalTrials/rest/params/confirmRegistration?hashCode="+name+";"+Encrypt.encrypt(id));
 
 		DefaultParam.instance.getModel().put(id, parm);    
 		try {
 			servletResponse.sendRedirect("../../regComplete.html");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@POST
 	@Path("/login")
 	@Produces(MediaType.TEXT_HTML)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public void login(
 			@FormParam("email") String email,
 			@FormParam("pwd") String pwd,
 			@Context HttpServletResponse servletResponse,
 			@Context HttpServletRequest servletRequest) throws Exception {
 
 		HttpSession session = servletRequest.getSession(true);
 		pwd = Encrypt.encrypt(pwd);		
 		Database database= new Database();
 		Connection connection = null;
 		try{
 			connection = database.Get_Connection();
 			PersistanceActions project= new PersistanceActions();
 			String isAuth = project.userAuthenticate(connection, email, pwd);
 			if (isAuth.equals("AUTHENTICATED") || isAuth.equals("Please Activate Your Account")){
 				session = servletRequest.getSession(true);
 				session.setAttribute("user_email", email);
 				int prefStatus = project.getPrefStatus(connection,email);
 				int regStatus = project.getRegStatus(connection,email);
 				if(regStatus == 0){
 					servletResponse.sendRedirect("../../regComplete1.html");
 				}
 				if(prefStatus == 0)
 					servletResponse.sendRedirect("../../preferences.html");
 				else{
 					Map<String, String> preferences = new HashMap<String,String>();
 					preferences = project.getPrefs(connection,email);
 					List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 					trials = project.getSearchedTrials(connection,preferences.get("status"),preferences.get("result"),preferences.get("studyType"),preferences.get("ageGroup"),preferences.get("phase1"),preferences.get("phaseII"),preferences.get("phaseIII"),preferences.get("phaseIV"),preferences.get("NIH"),preferences.get("industry"),preferences.get("federal"),preferences.get("university"),preferences.get("tags"));
 					session.setAttribute("advSearchedTrials", trials);
 					servletResponse.sendRedirect("../../getTrials.html");
 				}
 			}
 			else
 				servletResponse.sendRedirect("../../loginFailed.html");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			if(connection!=null)
 				connection.close();
 		}
 	}
 
 
 
 	// Defines that the next path parameter after todos is
 	// treated as a parameter and passed to the TodoResources
 	// Allows to type http://clinictrials.cloudapp.net/RestWS/rest/todos/1
 	// 1 will be treaded as parameter todo and passed to TodoResource
 	@Path("{todo}")
 	public ParamResource getParameters(@PathParam("todo") String id) {
 		return new ParamResource(uriInfo, request, id);
 	}
 
 } 
