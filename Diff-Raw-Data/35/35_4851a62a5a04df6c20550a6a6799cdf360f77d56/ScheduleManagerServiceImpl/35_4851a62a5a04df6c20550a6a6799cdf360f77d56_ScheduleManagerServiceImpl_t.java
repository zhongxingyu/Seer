 package edu.rpi.rocs.server.services.schedulemanager;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.portlet.PortletConfig;
 
 import org.hibernate.Session;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.rpi.rocs.Scheduler;
 import edu.rpi.rocs.client.objectmodel.Schedule;
 import edu.rpi.rocs.client.objectmodel.SchedulerManager;
 import edu.rpi.rocs.client.objectmodel.Section;
 import edu.rpi.rocs.client.objectmodel.Semester;
 import edu.rpi.rocs.server.hibernate.util.HibernateUtil;
 import edu.rpi.rocs.server.objectmodel.SchedulerManagerWriter;
 import edu.rpi.rocs.server.objectmodel.SemesterDB;
 
 public class ScheduleManagerServiceImpl extends RemoteServiceServlet implements
 		edu.rpi.rocs.client.services.schedulemanager.ScheduleManagerService {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 943641426558410281L;
 	
 	public void saveSchedule(String name, SchedulerManager state) {
 		try {
 			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 			session.beginTransaction();
 			
 			state.setName(name);
 			SchedulerManagerWriter smw = new SchedulerManagerWriter();
 			smw.setSession(session);
 			smw.visit(state);
 	
 			session.getTransaction().commit();
 		}
 		catch(Throwable ex) {
 			System.out.print("Caught exception: ");
 			ex.printStackTrace();
 		}
 	}
 	
	public SchedulerManager loadSchedule(String user, String name, int semesterid) throws Throwable {
 		try {
 			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 			session.beginTransaction();
 			
 			SchedulerManager mgr = (SchedulerManager)session.createQuery(" from SchedulerManager where UserId = '"+
 					user+"' and Name = '"+name+"' and SemesterId="+semesterid).uniqueResult();
 			Schedule s = mgr.getCurrentSchedule();
 			ArrayList<Section> sections = s.getSections();
 			ArrayList<Section> newsections = new ArrayList<Section>();
 			Semester semester = SemesterDB.getInstance(semesterid);
 			for(Section section : sections) {
 				Section newsection = semester.getSectionByCRN(section.getCrn());
 				if(newsection != null)
 					newsections.add(newsection);
 			}
 			
 			session.getTransaction().rollback();
 			
 			s.setSections(newsections);
 			return mgr;
 		}
 		catch(Throwable ex) {
 			System.out.print("Caught exception: ");
 			ex.printStackTrace();
			throw ex;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> getScheduleList(String user) {
 		ArrayList<String> temp=new ArrayList<String>();
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		
 		List<SchedulerManager> managers = session.createQuery(" from SchedulerManager where UserId = '"+user+"'").list();
 		for(SchedulerManager s : managers) {
 			temp.add(s.getName());
 		}
 		
 		session.getTransaction().rollback();
 		
 		return temp;
 	}
 	
 	public String getMOTD() {
 		URL motdFile=null;
 		String motd="";
 		try {
 			PortletConfig theConfig = Scheduler.getInstance().getPortletConfig();
 			motdFile = theConfig.getPortletContext().getResource("/WEB-INF/classes/motd.txt");
 			InputStream is = motdFile.openStream();
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 			String parts;
 			while((parts=br.readLine())!=null) {
 				motd += parts + "\r\n";
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		return motd;
 	}
 
 }
