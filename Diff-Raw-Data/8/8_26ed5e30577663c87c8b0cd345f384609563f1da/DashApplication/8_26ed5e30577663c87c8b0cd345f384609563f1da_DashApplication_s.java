 package com.buglabs.app.bugdash2;
 
 import java.util.Map;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.app.bugdash2.servicetracker.BUGwebAdminModuleControlServiceTracker;
 import com.buglabs.app.bugdash2.web.BUGwebAdminBUGnetServlet;
 import com.buglabs.app.bugdash2.web.BUGwebAdminHardwareServlet;
 import com.buglabs.app.bugdash2.web.BUGwebAdminServlet;
 import com.buglabs.app.bugdash2.web.BUGwebAdminSoftwareServlet;
 import com.buglabs.app.bugdash2.web.BUGwebAdminSystemServlet;
 import com.buglabs.app.bugdash2.web.BUGwebAdminUtilsServlet;
 import com.buglabs.app.bugdash2.web.BUGwebFileServlet;
 import com.buglabs.bug.dragonfly.module.IModuleControl;
 import com.buglabs.osgi.sewing.pub.ISewingService;
 import com.buglabs.util.osgi.FilterUtil;
 import com.buglabs.util.osgi.ServiceTrackerUtil.ManagedRunnable;
 
 public class DashApplication implements ManagedRunnable {
 	private ISewingService service;
 	private BUGwebAdminServlet mainServlet;
 	private BUGwebAdminHardwareServlet hardwareServlet;
 	private BUGwebAdminSystemServlet systemServlet;
 	private BUGwebAdminSoftwareServlet softwareServlet;
 	private BUGwebAdminBUGnetServlet bugnetServlet;
 	private BUGwebAdminUtilsServlet utilsServlet;
 	private BUGwebFileServlet imageServlet;
 	private final BundleContext context;
 	private ServiceTracker moduleTracker;
 	
 	public DashApplication(BundleContext context) {
 		this.context = context;		
 	}
 
 	@Override
 	public void run(Map<Object, Object> services) {
 		LogManager.logInfo("BUGwebAdminServiceTracker: start");
		service = (ISewingService) services.get(ISewingService.class);
 		
 		mainServlet = new BUGwebAdminServlet();
 		hardwareServlet = new BUGwebAdminHardwareServlet(); 
 		systemServlet = new BUGwebAdminSystemServlet();
 		softwareServlet = new BUGwebAdminSoftwareServlet(); 
 		bugnetServlet = new BUGwebAdminBUGnetServlet(); 
 		utilsServlet = new BUGwebAdminUtilsServlet();
 		imageServlet = new BUGwebFileServlet();
 
 		service.register(context, "/admin", mainServlet);
 		service.register(context, "/admin_hardware", hardwareServlet); 
 		service.register(context, "/admin_system", systemServlet);
 		service.register(context, "/admin_software", softwareServlet);
 		service.register(context, "/admin_bugnet", bugnetServlet); 
 		service.register(context, "/admin_util", utilsServlet); 
 		service.register(context, "/admin_imageviewer", imageServlet);
 	
 		try {
 			moduleTracker = new ServiceTracker(context, 
 					FilterUtil.generateServiceFilter(
 							context, new String[]{IModuleControl.class.getName()}), 
 					new BUGwebAdminModuleControlServiceTracker(context));
 			moduleTracker.open(); 					
 		} catch (InvalidSyntaxException e) {
 			//This should not be thrown.
 		}
 	}
 
 	@Override
 	public void shutdown() {
 		LogManager.logInfo("BUGwebAdminServiceTracker: stop");
 		service.unregister(mainServlet);
 		service.unregister(hardwareServlet); 
 		service.unregister(systemServlet);
 		service.unregister(softwareServlet);
 		service.unregister(bugnetServlet); 
 		service.unregister(utilsServlet);
 		service.unregister(imageServlet);
 		ShellUtil.destroySession(); 
 		if (moduleTracker != null)
 			moduleTracker.close(); 
 	}
 }
