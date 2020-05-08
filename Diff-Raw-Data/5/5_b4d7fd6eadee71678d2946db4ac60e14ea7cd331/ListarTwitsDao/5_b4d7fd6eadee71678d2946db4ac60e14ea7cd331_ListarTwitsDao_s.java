 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dao;
 
 import com.opensymphony.xwork2.ActionContext;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import model.Twits;
 import model.Usuarios;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 
 /**
  *
  * @author Franco
  */
 public class ListarTwitsDao {
     public static ArrayList <Twits> getTwitList() {
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                     
                         Session s = sf.openSession();
 			ArrayList<Usuarios> u = new ArrayList<Usuarios>();
                         Map auth = ActionContext.getContext().getSession();
                         Query query1= s.createQuery("from Usuarios where idu= :idu");
                         query1.setParameter("idu", ((Number)auth.get("idusuario")).longValue());
                         u=(ArrayList<Usuarios>)query1.list();
                         String nombre =  u.get(0).getNombre();
                         
 			 Query query = s.createQuery("FROM Twits where idu = :idu or idu IN (select siguiendo from Relaciones where idusuario = :idu) or string like CONCAT ('%',:nombre,'%')  order by timestam desc");
                          query.setMaxResults(10);
                          query.setParameter("idu", ((Number)auth.get("idusuario")).longValue());            
                         query.setParameter("nombre", nombre);
                    
                                                   s.disconnect();
 
                          return (ArrayList<Twits>)query.list();
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !-->" + ex.getMessage());
 			
 			return null;
 		}
   }
 
      public static Usuarios getSingleUser(Long idu) {
                 	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                       
                         Session s = sf.openSession();
 			
                         
 			 Query query = s.createQuery("FROM Usuarios where idu = :idu");
                          query.setParameter("idu", idu);            
                                                               s.disconnect();
 
                         return (Usuarios) query.list().get(0);
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !-->" + ex.getMessage());
 			
 			return null;
 		}
   }
 public static ArrayList <Twits> getPublicTwitList(Long idu) {
                 	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                     
                         Session s = sf.openSession();
 			ArrayList<Usuarios> u = new ArrayList<Usuarios>();
                         Query query1= s.createQuery("from Usuarios where idu= :idu");
                         query1.setParameter("idu", idu);
                         u=(ArrayList<Usuarios>)query1.list();
                        String nombre =  u.get(0).getNombre();
                        
                          Query query = s.createQuery("FROM Twits where idu = :idu or string like CONCAT ('%',:nombre,'%') order by timestam desc");
                          query.setMaxResults(10);
                            
                          query.setParameter("idu", idu);
                         query.setParameter("nombre", nombre);
                                                   s.disconnect();
 
                         return (ArrayList<Twits>)query.list();
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !-->" + ex.getMessage());
 			
 			return null;
 		}
   }
 
 }
