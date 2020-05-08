 package wranglerView.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import wranglerView.client.jobSubmission.TemplateService;
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
			return tReg.getAvailableTemplates();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		 
 		return new ArrayList<TemplateInfo>();
 	}
 
 }
