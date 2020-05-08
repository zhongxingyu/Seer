 package ch.unine.CMS.controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Restrictions;
 
 import ch.unine.CMS.model.MachineBean;
 import ch.unine.CMS.model.MachineKindBean;
 import ch.unine.CMS.model.SessionFactoryUtil;
 import ch.unine.CMS.model.UserBean;
 
 /**
  * Servlet implementation class MachineController
  */
 public class MachineController extends HttpServlet {
 	private static final long serialVersionUID 			= 1L;
 	private static final String CREATE_MACHINE_KIND_FCT = "createMachineKind";
 	private static final String CREATE_MACHINE_FCT 		= "createMachine";
 	private static final String START_MACHINE_FCT 		= "startMachine";
 	private static final String STOP_MACHINE_FCT 		= "stopMachine";
 	private static final String EDIT_MACHINE_FCT 		= "editMachine";
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public MachineController() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		//capuring operation 
 		String op = request.getParameter("fct");
 		String o;
 		if(op.equals(CREATE_MACHINE_KIND_FCT)){
 			String name 		= request.getParameter("name");
 			String description 	= request.getParameter("description");
 			
 			//Get hibernate session
 			Session sessionHibernate =  SessionFactoryUtil.getInstance().getCurrentSession();
 			//Begin transaction
 			Transaction tx = sessionHibernate.beginTransaction();
 		      
 			MachineKindBean m	= new MachineKindBean();
 			m.setName(name);
 			m.setDescription(description);
 			sessionHibernate.save(m);
 			//close transaction
 			tx.commit();
 			
 			PrintWriter out = response.getWriter();
 			
     		out.print("OK");
     		out.close();
     		
     		return;
 		}else if(op.equals(CREATE_MACHINE_FCT)){
 			Long machineKindId 	= new Long(request.getParameter("machineKind"));
 			String ip			=  request.getParameter("ip");
 			//Get hibernate session
 			Session sessionHibernate =  SessionFactoryUtil.getInstance().getCurrentSession();
 			
 			//Begin transaction
 			Transaction tx = sessionHibernate.beginTransaction();
 		      
 			MachineBean m	= new MachineBean();
 			m.setIP(ip);
 			m.setMachineKindId(machineKindId);
 			sessionHibernate.save(m);
 			//close transaction
 			tx.commit();
 			
 			PrintWriter out = response.getWriter();
 			
     		out.print("OK");
     		out.close();
 		}else if(op.equals(EDIT_MACHINE_FCT)){
 			Long machineKindId 	= new Long(request.getParameter("machineKind"));
 			String ip			=  request.getParameter("ip");
 			Long id 			= new Long(request.getParameter("id"));
 			//Get hibernate session
 			Session sessionHibernate =  SessionFactoryUtil.getInstance().getCurrentSession();
 			
 			//Begin transaction
 			Transaction tx = sessionHibernate.beginTransaction();
 			Criteria crit = sessionHibernate.createCriteria(MachineBean.class);
 			crit.add( Restrictions.eq( "id", id) );
 			crit.setMaxResults(1);
 			List cats = crit.list();
 			//Iterate answer
 			MachineBean m = null;
 	        for (Iterator it = cats.iterator(); it.hasNext();) {
 	        	m = (MachineBean) it.next();
 	        }
 	        if(m != null){
 	        	m.setId(id);
 				m.setIP(ip);
 				m.setMachineKindId(machineKindId);
 				sessionHibernate.save(m);
 	        }
 			//close transaction
 			tx.commit();
 			
 			PrintWriter out = response.getWriter();
 			
     		out.print("OK");
     		out.close();
 		}else if(op.equals(START_MACHINE_FCT)){
			System.out.println("START CMD");
 		}else if(op.equals(STOP_MACHINE_FCT)){
			System.out.println("STOP CMD");
 		}
 		
 	}
 }
