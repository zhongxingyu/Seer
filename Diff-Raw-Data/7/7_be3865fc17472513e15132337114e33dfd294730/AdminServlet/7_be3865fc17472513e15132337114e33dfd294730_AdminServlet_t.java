 package servlets;
 
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import beans.*;
 /**
  *
  * @author Alexander Fougner
  * @version 1.0
  */
 public class AdminServlet extends HttpServlet {
 
     private HttpServletRequest request;
     private HttpServletResponse response;
     private HttpSession sess;
     private RequestDispatcher rd;
     private ProductListBean productList;
     private ComponentListBean componentList;
     private String jdbcURL;
 
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
 
         jdbcURL = config.getInitParameter("JDBC_URL");
 
         try{
             componentList = new ComponentListBean(jdbcURL);
             productList = new ProductListBean(jdbcURL);
         }
         catch(Exception e){
             throw new ServletException(e);
         }
 
 		ServletContext sc = getServletContext();
 		sc.setAttribute("componentList",componentList);
         sc.setAttribute("productList",productList);
      }
 
     public void destroy() {
         
     }
     
     protected void processRequest()
     throws ServletException, java.io.IOException {
 
         sess = request.getSession();
         rd = null;
 		sess.setAttribute("currentUser", request.getRemoteUser());
         sess.setAttribute("jdbcURL",jdbcURL);
 
         try {
 
         	if(request.getParameter("action") == null || 
                    request.getParameter("action").equals("show")){
                     showAdmin();
 
         	} else if(request.getParameter("action").equals("buycomponent")) {
                 
                     this.buyComponent();
                     showAdmin();
             } else if(request.getParameter("action").equals("addproduct")) {
 
                 this.addProduct();
                 showAdmin();
 
             }
         }catch(Exception e){
             throw new ServletException("Error", e);
         }
     }
 
     protected void addProduct() throws Exception{
 
         ProductBean pb = new ProductBean(jdbcURL);
         pb.setTitle(request.getParameter("title"));
         pb.setDescription(request.getParameter("description"));
         pb.setPrice(Integer.parseInt(request.getParameter("price")));
         pb.saveProduct();
     }
 
     protected void buyComponent() throws Exception{
         
        int cid = Integer.parseInt(request.getParameter("componentid"));
        int qty = Integer.parseInt(request.getParameter("quantity"));
        componentList.increaseQuantity(cid, qty);
     }
 
     protected void showAdmin() throws Exception{
             productList.update();
             componentList.update();
             rd = request.getRequestDispatcher("/admin.jsp");
             rd.forward(request,response);
     }
 
     /** Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      */
     protected void doGet(HttpServletRequest request, 
                          HttpServletResponse response)
 	throws ServletException, java.io.IOException {
         this.request = request;
         this.response = response;
         processRequest();
     }
     
     /** Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      */
     protected void doPost(HttpServletRequest request, 
                           HttpServletResponse response)
 	throws ServletException, java.io.IOException {
         this.request = request;
         this.response = response;
         processRequest();
     }
     
     /** Returns a short description of the servlet.
      */
     public String getServletInfo() {
         return "The AdminServlet";
     }
 }
 
 
 
 
