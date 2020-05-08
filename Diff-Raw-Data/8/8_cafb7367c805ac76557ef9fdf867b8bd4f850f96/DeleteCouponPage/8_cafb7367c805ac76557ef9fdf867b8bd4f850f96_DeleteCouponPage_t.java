 package groupone;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class DeleteCouponPage
  */
 @WebServlet("/DeleteCouponPage")
 public class DeleteCouponPage extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public DeleteCouponPage() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		PrintWriter out = response.getWriter();
 
         ArrayList<Coupon> coupons = DBOperation.getCouponList();
         
         ArrayList<Coupon> filtered = new ArrayList<Coupon>();
         
 //        Account account = (Account) request.getAttribute("account");
         System.out.println(request.getAttribute("accountId"));
         for (Coupon c : coupons) {
        	if (c.getVendor().equalsIgnoreCase((String)session.getAttribute("accountId"))) {
         		filtered.add(c);
         		System.out.println(c.getTitle());
         	}
         		
         }
         
         request.setAttribute("coupons", filtered);
         request.getRequestDispatcher("/delete_coupon.jsp").forward(request, response);
         
 //        out.println("<html>");
 //        out.println("<head>");
 //        out.println("<title>Delete Servlet</title>");
 //        out.println("</head>");
 //        out.println("<body>");
 //        out.println("<TABLE cellpadding=\"15\" border=\"1\" style=\"background-color: #ffffcc;\">");
 //        
 //        for (Coupon c : coupons) {
 //        	out.println("<TR>");
 //        	out.println("<TD>" + c.id + "</TD>");
 //        	out.println("<TD>" + c.title + "</TD>");
 //        	out.println("<TD>" + c.createDate + "</TD>");
 //        	out.println("<TD>" + c.expireDate+ "</TD>");
 //        	out.println("<TD>" + c.quantity + "</TD>");
 //        	out.println("<TD>" + c.sold + "</TD>");
 //        	out.println("<TD>" + c.price + "</TD>");
 //        	out.println("<TD>" + c.category + "</TD>");
 //        	out.println("</TR>");
 //        }
 //        out.println("</TABLE>");
 //        out.println("</body>");
 //        out.println("</html>");
 	}
 }
