 package se.starbox.controllers;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.openpipeline.scheduler.PipelineScheduler;
 import org.quartz.SchedulerException;
 
 import se.starbox.models.SettingsModel;
 import se.starbox.util.IndexDownloader;
 
 /**
  * A Servlet that initializes background processes, if not already running.
  * This should be called (visit url) when Starbox first starts.
  */
 @WebServlet(
 	description = "Initializes background processes", 
 	urlPatterns = { "/init" }
 )
 public class InitBackgroundProcessesServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
	private static boolean hasStartedStarboxJob = false;
	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		IndexDownloader.start();
 
 		try {
 			System.setProperty("app.home", SettingsModel.getProjectRootPath());
 			PipelineScheduler scheduler = PipelineScheduler.getInstance();
			if (!hasStartedStarboxJob) {
				System.out.println("InitBackgroundProcesses: Starting StarboxJob now");
 				scheduler.startJob("StarboxJob");
				hasStartedStarboxJob = true;
			}
 		} catch (SchedulerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 }
