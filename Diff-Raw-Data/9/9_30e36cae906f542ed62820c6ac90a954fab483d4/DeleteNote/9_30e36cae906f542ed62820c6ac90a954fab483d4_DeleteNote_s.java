 package com.tongji.j2ee.sp;
 
 import hibernate.HibernateSessionFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import model.Notify;
 import model.NotifyDAO;
 
 public class DeleteNote extends HttpServlet {
 
 	/**
 	 * The doGet method of the servlet. <br>
 	 * 
 	 * This method is called when a form has its tag value method equals to get.
 	 * 
 	 * @param request
 	 *            the request send by the client to the server
 	 * @param response
 	 *            the response send by the server to the client
 	 * @throws ServletException
 	 *             if an error occurred
 	 * @throws IOException
 	 *             if an error occurred
 	 */
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		int noteid = Integer.parseInt(request.getParameter("noteid"));
 
 		NotifyDAO notifyDAO = new NotifyDAO();
 		Notify note = notifyDAO.findById(noteid);
 
 		Session s = (Session) HibernateSessionFactory.getSession();
        
         notifyDAO.delete(note);
 		// notifyDAO.save(newNote);
 		try {
 			Transaction ts = s.beginTransaction();
 			ts.commit();
 		} catch (Exception e) {
 
 		} finally {
 			//s.close();
 			HibernateSessionFactory.closeSession();
 			
 			//删除文件
 			DeleteHtml(request, noteid);
 			NotifyList lns = new NotifyList(0);
 			System.out.println("allitems" + lns.allItems);
 			HttpSession hs = request.getSession();
 			hs.setAttribute("noteli", lns);
 
 			request.setAttribute("pageNumber", 1);
 			request.getRequestDispatcher("admin.jsp")
 					.forward(request, response);
 		}
 
 	}
 	
 	private boolean DeleteHtml(HttpServletRequest request, int noteid) {
 		ServletContext sc = request.getSession().getServletContext();
 		String filename = sc.getRealPath("/") + "html/" + noteid + ".html";
 		File htmlfile = new File(filename);
 		if (htmlfile.isFile() && htmlfile.exists()) {
 			htmlfile.delete();
 			System.out.println("删除单个文件" + filename + "成功！");
 			return true;
 		} else {
 			System.out.println("删除单个文件" + filename + "失败！");
 			return false;
 		}
 	}
 
 }
