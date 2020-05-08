 package it.uniroma3.controller;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import it.uniroma3.model.*;
 
 /**
  * Servlet implementation class GestisciConferma
  */
 @WebServlet("/confermaProdotto")
 public class ConfermaProdotto extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ConfermaProdotto() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String risposta = request.getParameter("risp");
 		ServletContext application = getServletContext();
 		RequestDispatcher rd;
 		HttpSession sessione = request.getSession();
 		String destinazione = "/inserimentoProdotto.jsp";
 		FacadeProdotto facade = new FacadeProdotto();
 		
 		if (risposta.equals("si"))
		    if(facade.inserisciProdotto((String)sessione.getAttribute("nome"),(String)sessione.getAttribute("descrizione"),(double)sessione.getAttribute("prezzo")))
 		    	destinazione = "/inserimentoCompletato.jsp";
 		    else 
 		    	destinazione = "/erroreInserimento.jsp";
 		   
 		rd = application.getRequestDispatcher(destinazione);
 		rd.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
