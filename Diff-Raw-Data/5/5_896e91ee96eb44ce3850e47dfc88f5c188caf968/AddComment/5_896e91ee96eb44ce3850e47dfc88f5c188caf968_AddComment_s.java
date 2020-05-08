 package es.uc3m.ctw.me_gustauto.controller;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.Date;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.NamedQuery;
 import javax.persistence.Persistence;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.transaction.Transaction;
 
 import es.uc3m.ctw.me_gustauto.model.AutoAd;
 import es.uc3m.ctw.me_gustauto.model.Comment;
 import es.uc3m.ctw.me_gustauto.model.GeneralAd;
 import es.uc3m.ctw.me_gustauto.model.User;
 
 
 
 
 public class AddComment extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
     public AddComment() {
         super();
     }
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	
 		Comment cmt = new Comment();
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("megustauto");
 		EntityManager manager = emf.createEntityManager();
 		EntityTransaction et = manager.getTransaction();
 		String username = (String) request.getSession().getAttribute(MySQLConnector.USERNAME_OF_CLIENT);		
 		int uId = ((User)(manager.createQuery("SELECT c FROM User c WHERE c.username=:userName")
 				.setParameter("userName", username)
 				.getResultList().get(0))).getUserId();
 		et.begin();
 		manager.persist(cmt);
		cmt.setAutoAd( manager.find(AutoAd.class, request.getParameter("ad_id")));
 		cmt.setContent(((String) request.getParameter("content")));
 		cmt.setUser(manager.find(User.class, uId));
		
 		
 		et.commit();
 		//	redirect to showdetails 
 		response.sendRedirect("showdetails.jsp");
 		return;
 	}
 
 }
