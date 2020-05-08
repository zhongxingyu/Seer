 package com.everis.interesesdemora;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Type;
 import java.util.Collection;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 /**
  * <p>
  * Handles data sent by editable and tab navigable DataTables.
  * </p>
  * 
  * @author jagudopr
  * @version 1.0.0
  * 
  */
 public class EditableTabNavigableDataTableHandler<T> extends HttpServlet {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(EditableTabNavigableDataTableHandler.class);
 
 	public static final String DATA_PARAM_NAME = "data";
 	
 	protected TypeToken<Collection<T>> getCollectionType(){
 		return null;
 	}
 	
 	public void service(HttpServletRequest req, HttpServletResponse res)
 			throws ServletException, IOException {
 
		LOG.debug("Receiving data from DataTable. Processing...");
 		
 		PrintWriter out = res.getWriter();
 		String json = req.getParameter(DATA_PARAM_NAME);
 
 		Gson gson = new Gson();
 		Type collectionType = getCollectionType().getType();
 		Collection<T> data = gson.fromJson(json, collectionType);
 
 		if (data.size() == 0) {
 			LOG.debug("No data received");
 		} else {
 			LOG.debug("Data received from client:");
 			for (T f : data) {
 				LOG.debug(f.toString());
 			}
 		}
 		
 		processData(data);
 		
 		res.setContentType("application/json");
 		out.println(gson.toJson(new DataTableHandlerResponse()));
 		out.flush();
 	}
 
 	protected void processData(Collection<T> data) {
 		// do stuff here
 	}
 	
 	public static void main(String[] args){
 		Gson gson = new Gson();
 		String result = gson.toJson(new DataTableHandlerResponse());
 		System.out.println(result);
 	}
 
 }
