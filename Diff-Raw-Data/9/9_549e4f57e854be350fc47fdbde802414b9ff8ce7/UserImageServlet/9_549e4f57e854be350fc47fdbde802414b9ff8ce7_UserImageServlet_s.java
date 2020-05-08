 package images;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import system.MyWeddingManager;
 import system.User;
 import system.UserAction;
 import system.UserImage;
 
 import com.mysql.jdbc.Blob;
 
 /**
  * Servlet implementation class UserImageServlet
  */
 @WebServlet(name = "UserImage", urlPatterns = { "/UserImage" })
 public class UserImageServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	MyWeddingManager mw = MyWeddingManager.getInstance();
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public UserImageServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see Servlet#init(ServletConfig)
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see Servlet#getServletConfig()
 	 */
 	public ServletConfig getServletConfig() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession(false);
 		User user = (User) session.getAttribute("user");
 		if (user == null){
 			this.getServletConfig().getServletContext().getRequestDispatcher("/LogIng.jsp").forward(request, response);
         	return;
 		}
     	Blob photo = null;
     	UserImage UserObjecphoto = new UserImage(user.getId());
     	UserAction uac = new UserAction(user);
     	UserObjecphoto = mw.GetUserPhoto(user, uac);
     	photo = (Blob)UserObjecphoto.getPhoto();
     	try {
     		ServletOutputStream out = response.getOutputStream();
     		response.setContentType("image/gif");
     		
 			InputStream in = photo.getBinaryStream();
 			int length = (int) photo.length();
 			
 			int bufferSize = 1024;
 		      byte[] buffer = new byte[bufferSize];
 		      
 		      while ((length = in.read(buffer)) != -1) {
 		    	   out.write(buffer, 0, length);
 		        }
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
