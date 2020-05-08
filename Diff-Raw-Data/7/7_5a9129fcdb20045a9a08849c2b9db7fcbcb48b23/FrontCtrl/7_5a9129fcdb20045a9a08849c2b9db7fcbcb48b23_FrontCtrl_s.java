 package ctrl;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.CategoryBean;
 import model.FRUModel;
 
 /**
  * Servlet implementation class FontCtrl
  */
 public class FrontCtrl extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public FrontCtrl() {
         super();
         // TODO Auto-generated constructor stub
     }
 
     	
 	@Override
 	public void init() throws ServletException {
 		// TODO Auto-generated method stub
 		super.init();
 		//the singleton model need to be initialized here
 		FRUModel fru;
 		try {
 			fru = new FRUModel();
 			List<CategoryBean> cat =  fru.retrieveCategory();
 			
 			this.getServletContext().setAttribute("fru", fru);
 			// poke items
 			this.getServletContext().setAttribute("categories", cat);
 			for (CategoryBean c : cat )
 			{
 				String itemName = "item" + c.getCatID();
 				this.getServletContext().setAttribute(itemName,fru.retrieveItems(c.getCatID()));
 			}
 			//poke id
 			int id = 0;
 			this.getServletContext().setAttribute("id", id);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String info = request.getPathInfo();
 		if (info.matches("/Start")|| info.matches("/FRU")|| info.equals("/")|| info.matches("/category")|| info.matches("/index"))
 		{
 			this.getServletContext().getNamedDispatcher("FRU").forward(request, response);
 		}
 		
 		else if (info.matches("/ShoppingCart"))
 		{
 			this.getServletContext().getNamedDispatcher("ShoppingCart").forward(request, response);
 		}
 		else if (info.matches("/Login"))
 		{
 			this.getServletContext().getNamedDispatcher("Login").forward(request, response);
 		}
 		
 		else if (info.matches("/Express"))
 		{
 			this.getServletContext().getNamedDispatcher("Express").forward(request, response);
 
 		}
 		else if (info.matches("/CheckOut"))
 		{
 			this.getServletContext().getNamedDispatcher("CheckOut").forward(request, response);
 		}
 		else if (info.matches("/Statistic"))
 		{
 			this.getServletContext().getNamedDispatcher("Statistic").forward(request, response);
 
 		}
 		else
 		{
			System.out.println("!!!!Error");
 			response.sendError(404);
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 			doGet(request,response);
 	}
 
 }
