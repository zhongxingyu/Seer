 
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.Product;
 
 import dao.ProductDao;
 
 /**
  * Servlet implementation class EditProduct
  */
 public class EditProduct extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public EditProduct() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		if (!Browse.isOwner(request, response)) {
 			response.sendRedirect("browse");
 		} else {
 			int id = Integer.parseInt(request.getParameter("id"));
 			ProductDao dao = new ProductDao();
 			Browse.loadProducts(request);
 			List<Product> products = dao.getProductById(id);
 			for (Product p : products) {
 				request.setAttribute("product", p);
 			}
 			getServletContext().getRequestDispatcher("/editproduct.jsp").forward(request, response);
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
		if (!Browse.isOwner(request, response)) {
 			response.sendRedirect("browse");
 		} else {
 			int id = Integer.parseInt(request.getParameter("id"));
 			String name = request.getParameter("name");
 			String sku = request.getParameter("sku");
 			Double price = Double.parseDouble(request.getParameter("price"));
 			int category = Integer.parseInt(request.getParameter("category"));
 			Product product = new Product(id, name, sku, price, category);
 			ProductDao dao = new ProductDao();
 			dao.updateProduct(id, product);
			response.sendRedirect("product");
 		}
 	}
 }
