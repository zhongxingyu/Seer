 package com.association.controller.servlets;
 
 import com.model.bean.Article;
 import com.model.persistence.PersistenceServiceProvider;
 import com.model.persistence.services.ArticlePersistence;
 
 import javax.inject.Inject;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 @WebServlet(urlPatterns={"/List","/List/*"})
 public class ListArticle extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
     @Inject
     ServletContext context;
 
     public ListArticle() {
         super();
     }
 
 		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		if( request.getSession().getAttribute("adherent") == null ) {
 			response.sendRedirect(request.getContextPath() + "/Login");
 			return;
 		}
 
 		ArticlePersistence service = PersistenceServiceProvider.getService(ArticlePersistence.class);
 		
 		if( articleAjoute(request) ) {
 			int id = Integer.parseInt( request.getParameter("article") );
 			Article article = service.load(id);
 
 			if ( article == null ) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "La page entrée n'est pas valide ");
                 return;
 			}
             else {
                 HttpSession session = request.getSession();
                 ArrayList<Article> articles = (ArrayList<Article>) session.getAttribute("orderInProcess");
 
                 if( articles == null ) {
                     articles = new ArrayList<>();
                    session.setAttribute("orderInProcess",articles);
                 }
 
                 articles.add(article);
                 request.setAttribute("added", id);
             }
 		}
 		
 		List<Article> articles = service.loadAll();
 		RequestDispatcher rd;
 		request.setAttribute("articles", articles);
 		rd = context.getRequestDispatcher("/jsp/Articles.jsp");
 		rd.include(request, response);
 
 	}
 
     private boolean articleAjoute(HttpServletRequest request) {
         return request.getParameter("article") != null;
     }
 }
