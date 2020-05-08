 package web_interface;
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import businessLogic_layer.Functionality;
 import db_connection.Connector;
 
 
 public class Servlet extends HttpServlet {
 
	Brugere brugerMetoder = new Brugere();
	Raavarer raavareMetoder = new Raavarer();
	Recepter receptMetoder = new Recepter();
	RaavareBatches raavareBatchMetoder = new RaavareBatches();
	ProduktBatches produktBatchMetoder = new ProduktBatches();
	Other otherMetoder = new Other();
 	private static final long serialVersionUID = 1L;
 
 	public Servlet(){
 		super();
 	}
 
 	public void init(ServletConfig config) throws ServletException{
 		try { new Connector(); } 
 		catch (InstantiationException e) { e.printStackTrace(); }
 		catch (IllegalAccessException e) { e.printStackTrace(); }
 		catch (ClassNotFoundException e) { e.printStackTrace(); }
 		catch (SQLException e) { e.printStackTrace(); }
 	}
 
 	public void destroy(){
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
 		request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
 		Functionality funktionalitetsLaget = new Functionality();
 		try{
 
			if (request.getParameter("login") != null && request.getParameter("login").equals("Log ind")) {
 				otherMetoder.handleLogIn(request, response, funktionalitetsLaget);
			}
 			if (request.getParameter("logoff") != null && request.getParameter("logoff").equals("Log af"))
 				otherMetoder.handleLogOff(request, response);
 			if (request.getParameter("hovedmenu") != null && request.getParameter("hovedmenu").equals("Tilbage til hovedmenu"))
 				otherMetoder.handleGoToHovedmenu(request, response);
 			if (request.getParameter("adminibruger") != null && request.getParameter("adminibruger").equals("Administrere brugere"))
 				request.getRequestDispatcher("/WEB-INF/admin/bruger/adminibruger.jsp").forward(request, response);
 			if (request.getParameter("createopr") != null && request.getParameter("createopr").equals("Opret bruger")) {
 				int oprID = 0;
 				oprID = funktionalitetsLaget.getOprDAO().getHighestOprID().getOprId()+1;
 				request.setAttribute("autoid", oprID);
 				request.getRequestDispatcher("/WEB-INF/admin/bruger/createopr.jsp").forward(request, response);}
 			if (request.getParameter("showopr") != null && request.getParameter("showopr").equals("Vis brugere"))
 				brugerMetoder.handleShowOpr(request, response, funktionalitetsLaget);
 			if (request.getParameter("removeopr") != null && request.getParameter("removeopr").equals("Slet bruger"))
 				brugerMetoder.handleRemoveOpr(request, response, funktionalitetsLaget);
 			if (request.getParameter("updateopr") != null && request.getParameter("updateopr").equals("Opdatér bruger"))
 				brugerMetoder.handleUpdateOpr(request, response, funktionalitetsLaget);
 			if (request.getParameter("adminivare") != null && request.getParameter("adminivare").equals("Administrere råvarer"))
 				raavareMetoder.handleAdminiVare(request, response, funktionalitetsLaget);
 			if (request.getParameter("createvare") != null && request.getParameter("createvare").equals("Opret råvare"))
 				raavareMetoder.handleCreateVare(request, response, funktionalitetsLaget);
 			if (request.getParameter("showvare") != null && request.getParameter("showvare").equals("Vis råvare"))
 				raavareMetoder.handleShowVare(request, response, funktionalitetsLaget);
 			if (request.getParameter("updatevare") != null && request.getParameter("updatevare").equals("Opdater råvare"))
 				raavareMetoder.handleUpdateVare(request, response, funktionalitetsLaget);
 			if (request.getParameter("adminirecept") != null && request.getParameter("adminirecept").equals("Administrere recepter"))
 				receptMetoder.handleAdminiRecept(request, response, funktionalitetsLaget);
 			if (request.getParameter("createrecept") != null && request.getParameter("createrecept").equals("Opret recept"))
 				receptMetoder.handleCreateRecept(request, response, funktionalitetsLaget);
 			if (request.getParameter("showrecept") != null && request.getParameter("showrecept").equals("Vis recepter"))
 				receptMetoder.handleShowRecept(request, response, funktionalitetsLaget);
 			if (request.getParameter("adminiraavarebatch") != null && request.getParameter("adminiraavarebatch").equals("Administrere råvarebatches"))
 				raavareBatchMetoder.handleAdminiRaavareBatch(request, response, funktionalitetsLaget);
 			if (request.getParameter("createraavarebatch") != null && request.getParameter("createraavarebatch").equals("Opret råvarebatch"))
 				raavareBatchMetoder.handleCreateRaavareBatch(request, response, funktionalitetsLaget);
 			if (request.getParameter("showraavarebatch") != null && request.getParameter("showraavarebatch").equals("Vis råvarebatch"))
 				raavareBatchMetoder.handleShowRaavareBatch(request, response, funktionalitetsLaget);
 			if (request.getParameter("adminiproduktbatch") != null && request.getParameter("adminiproduktbatch").equals("Administrere produktbatches"))
 				produktBatchMetoder.handleAdminiProduktBatch(request, response, funktionalitetsLaget);
 			if (request.getParameter("createproduktbatch") != null && request.getParameter("createproduktbatch").equals("Opret produktbatch"))
 				produktBatchMetoder.handleCreateProduktBatch(request, response, funktionalitetsLaget);
 			if (request.getParameter("showproduktbatch") != null && request.getParameter("showproduktbatch").equals("Vis produktbatch"))
 				produktBatchMetoder.handleShowProduktBatch(request, response, funktionalitetsLaget);
 			if (request.getParameter("createopr_submit") != null && request.getParameter("createopr_submit").equals("Opret Operatør"))
 				brugerMetoder.handleCreateOprSubmit(request, response, funktionalitetsLaget);
 			if (request.getParameter("removeopr_submit") != null && request.getParameter("removeopr_submit").equals("Slet Operatør"))
 				brugerMetoder.handleRemoveOprSubmit(request, response, funktionalitetsLaget);
 			if (request.getParameter("updateopr_submit") != null && request.getParameter("updateopr_submit").equals("Opdater operatør"))
 				brugerMetoder.handleUpdateOprSubmit(request, response, funktionalitetsLaget);
 			if (request.getParameter("createraavare_submit") != null && request.getParameter("createraavare_submit").equals("Opret råvare"))
 				raavareMetoder.handleCreateVareSubmit(request, response, funktionalitetsLaget);
 			if (request.getParameter("updateraavare_submit") != null && request.getParameter("updateraavare_submit").equals("Opdater råvare"))
 				raavareMetoder.handleUpdateVareSubmit(request, response, funktionalitetsLaget);
 			if (request.getParameter("createrecept_submit") != null && request.getParameter("createrecept_submit").equals("Opret recept"))
 				receptMetoder.handleCreateReceptSubmit(request, response, funktionalitetsLaget);
 			if (request.getParameter("createraavarebatch_submit") != null && request.getParameter("createraavarebatch_submit").equals("Opret råvarebatch"))
 				raavareBatchMetoder.handleCreateRaavareBatchSubmit(request, response, funktionalitetsLaget);
 			if (request.getParameter("createproduktbatch_submit") != null && request.getParameter("createproduktbatch_submit").equals("Opret produktbatch"))
 				produktBatchMetoder.handleCreateProduktBatchSubmit(request, response, funktionalitetsLaget);
 			if (request.getParameter("changepw") != null && request.getParameter("changepw").equals("Ændr password"))
 				request.getRequestDispatcher("/WEB-INF/admin/changepw.jsp").forward(request, response);
 			if (request.getParameter("changepw_submit") != null && request.getParameter("changepw_submit").equals("Ændr password"))
 				otherMetoder.handleChangePwSubmit(request, response, funktionalitetsLaget);
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 }
