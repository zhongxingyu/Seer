 package wranglerView.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import wranglerView.client.jobSubmission.TemplateService;
import wranglerView.logging.WLogger;
 import wranglerView.server.template.TemplateRegistry;
 import wranglerView.shared.TemplateInfo;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * Uses the TemplateRegistry to find a list of templates that can be used 
  * for various analyses, and returns the list. 
  * @author brendan
  *
  */
 public class TemplateServiceImpl extends RemoteServiceServlet implements TemplateService {
 
 	
 	
 	@Override
 	public List<TemplateInfo> getAvailableTemplates() {
 		 
 		TemplateRegistry tReg;
 		try {
 			tReg = TemplateRegistry.getRegistry();
			
			List<TemplateInfo> templates = tReg.getAvailableTemplates();
			
			return templates;
 		} catch (IOException e) {
			WLogger.warn("Error reading templates from template registry: " + e.getMessage() );
 			e.printStackTrace();
 		}
 		
		
		WLogger.warn("Template service is returning empty template list from");
 		 
 		return new ArrayList<TemplateInfo>();
 	}
 
 }
