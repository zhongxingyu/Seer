 /**
  * 
  */
 package com.imdevice.pipe2wp.datastore;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.imdevice.pipe2wp.EMF;
 import com.imdevice.pipe2wp.Subscribe;
 
 /**
  * @author lcw601474
  *	This class demonstrates how to use JPA to access the Google AppEngine Datastore.
  */
 @SuppressWarnings("serial")
 public class JPA extends HttpServlet { 
 
 	/**
 	 * @param args
 	 */
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		resp.setContentType("text/html;charset=UTF-8");
 		PrintWriter o=resp.getWriter();
 		
 		
 		Date hireDate=new Date();
 		Employee employee=new Employee("Chunwei", "Lu",hireDate);
 		String link="http://www.ifanr.com/feed";
 		Subscribe sub=new Subscribe(link);
 		Date init=new Date();
 		init.setTime(init.getTime()-24*60*60*1000);
 		sub.setLastFetchDate(init);
 		sub.setLastPubDate(init);
 		EntityManager em = EMF.get().createEntityManager();
 		try{
 			em.getTransaction().begin();
 			em.persist(employee);
 			em.getTransaction().commit();//如果不commit，下面的查询看不到这条记录
 			em.getTransaction().begin();
 			em.persist(sub);
 			em.getTransaction().commit();
 			TypedQuery<Employee> q = em.createQuery("SELECT e FROM Employee e",Employee.class);
 			List<Employee> employees=q.getResultList();
 			if(!employees.isEmpty()){
 				for(Employee employee1:employees){	
 					o.print("<p>");
 					o.print(employee1.getKey()+" : ");
 					o.print(employee1);
 					o.print("</p>");
 				}
 			}else{
 				o.print("No result!");
 			}
 			TypedQuery<Subscribe> q1 = em.createQuery("SELECT e FROM Subscribe e",Subscribe.class);
 			List<Subscribe> subscribes=q1.getResultList();
 			if(!subscribes.isEmpty()){
 				for(Subscribe subscribe:subscribes){	
 					o.print("<hr>");
 					o.print(subscribe.getLink()+"  lastPub:  "+subscribe.getLastPubDate()+"  lastFetch:  "+subscribe.getLastFetchDate());
 				}
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			em.close();			
 		}
 	}
 
 }
