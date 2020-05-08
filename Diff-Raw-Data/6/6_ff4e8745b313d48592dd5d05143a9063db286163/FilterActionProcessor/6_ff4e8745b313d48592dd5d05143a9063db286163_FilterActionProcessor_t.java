 package ua.ivanchenko.eman.processors;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.Collection;
 import java.util.HashMap;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import ua.ivanchenko.eman.exceptions.ConfigLoaderException;
 import ua.ivanchenko.eman.exceptions.DataAccessException;
 import ua.ivanchenko.eman.model.IDataAccessor;
 import ua.ivanchenko.eman.model.IWorker;
 
 public class FilterActionProcessor implements ActionProcessor {
 	private Logger log = Logger.getLogger("emanlogger");
 	/**
      * method processes the request from user and generate response
      * @param req it's request
      * @param resp it's response
      * @throws ConfigLoaderException  when got incorrect configs file.
      * @throws DataAccessException when can't access to data.
      */
 	public void process(HttpServletRequest req, HttpServletResponse resp,
 			IDataAccessor access) throws DataAccessException {
 		HashMap<String, String> filters = new HashMap<String, String>();
 		if (req.getParameter(FiltreConst.fname) != null && !req.getParameter(FiltreConst.fname).isEmpty()) {
 			filters.put(FiltreConst.fname,req.getParameter(FiltreConst.fname));
 		}
 		if (req.getParameter(FiltreConst.lname) != null && !req.getParameter(FiltreConst.lname).isEmpty()) {
 			filters.put(FiltreConst.lname,req.getParameter(FiltreConst.lname));
 		}
 		if (req.getParameter(FiltreConst.job) != null && !req.getParameter(FiltreConst.job).isEmpty()) {
 			filters.put(FiltreConst.job,req.getParameter(FiltreConst.job));
 		}
 		if (req.getParameter(FiltreConst.dept) != null && !req.getParameter(FiltreConst.dept).isEmpty()) {
 			filters.put(FiltreConst.dept,req.getParameter(FiltreConst.dept));
 		}		
 		if (req.getParameter(FiltreConst.office) != null && !req.getParameter(FiltreConst.office).isEmpty()) {
 			filters.put(FiltreConst.office,req.getParameter(FiltreConst.office));
 		}	
 		Collection<IWorker> workers = null;
 		if (req.getParameter("id") != null && !"null".equalsIgnoreCase(req.getParameter("id"))) {
			try {
				workers = access.filteringWorker(new BigInteger(req.getParameter("id")),filters);		
			} catch (NumberFormatException e) {
				workers = access.filteringWorker(null,filters);
			}
 		} else {
 			workers = access.filteringWorker(null,filters);
 		}
 			req.getSession().setAttribute("w_workers", workers);
 		try {
 			StringBuffer re = new StringBuffer("index.jsp?action_id=view_top_manager");
 			   if (filters.get(FiltreConst.fname) != null) 
 					re.append("&fname="+req.getParameter(FiltreConst.fname));	
 			   if (filters.get(FiltreConst.lname) != null) 
 						re.append("&lname="+req.getParameter(FiltreConst.lname));
 			 	if (filters.get(FiltreConst.job) != null) 
 					re.append("&job="+req.getParameter(FiltreConst.job));
 			 	if (filters.get(FiltreConst.office) != null) 
 					re.append("&office="+req.getParameter(FiltreConst.office));
 			 	if (filters.get(FiltreConst.dept) != null) 
 					re.append("&dept="+req.getParameter(FiltreConst.dept));
 			 	if (req.getParameter("id") != null && !"null".equalsIgnoreCase(req.getParameter("id"))) 
 			 		re.append("&id="+req.getParameter("id"));			
 			 	re.append("&filtre=true");								
 				log.info(re.toString());
 			if (req.getParameter("select") != null ) {	
 				re.append("&select=true");
 			} 
 			resp.sendRedirect(re.toString());			
 		} catch (IOException e) {
 			log.error("can't redirect from ",e);
 		}
 	}
 }
