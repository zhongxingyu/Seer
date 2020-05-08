 package org.jcandystore.views;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.jcandystore.model.Item;
 import org.jcandystore.model.Product;
 import org.jcandystore.services.ItemService;
 import org.jcandystore.services.ProductService;
 
 public class ShoppingCartServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	public ShoppingCartServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse resp)
 			throws IOException {
 		PrintWriter out = resp.getWriter();
 		resp.setContentType("text/html;charset=UTF-8");
 
 		ProductService productService = new ProductService();
 		ItemService itemService = new ItemService();
 
 		out.println("<h1>Shopping Cart</h1>");
 
 		HttpSession session = request.getSession(true);
 		Enumeration<String> e = session.getAttributeNames();
 		if (!e.hasMoreElements()) {
 			out.println("<em>(empty cart)</em>");
 		} else {
			float grandTotal = 0;
 			String prodId, prodName = null;
 			Product zeProduct = null;
 			Item zeItem = null;
 			while (e.hasMoreElements()) {
 				// retrieve product id
 				prodId = e.nextElement();
 				// obtain real name from id
 				zeProduct = productService.find(prodId);
 				prodName = zeProduct.getProdName();
 
				out.println("<br/>" + prodName + " : ");
 				// retrieve product quantity
 				Integer quantity = (Integer) session.getAttribute(prodId);
 				out.println(quantity);
 
 				// get price and update grand total
 				zeItem = itemService.findByProdId(prodId);
 				if (zeItem == null) {
 					// Application Error: should not happen
 					out.println("(<i>Could not retrieve product price</i>)");
 				} else {
 					BigDecimal price = zeItem.getListPrice().round(new MathContext(3));
 					out.println(" @ " + price + " euros");
 					grandTotal += quantity.floatValue() * price.floatValue();
 				}
 			}
 		}
 		// TODO: round off grand total
 
 		out.println("<br/><br/><h2>Grand Total: " + grandTotal + " euros<h2>");
 		
 		// TODO: add "checkout" and "continue shopping buttons"
 	}
 
 }
