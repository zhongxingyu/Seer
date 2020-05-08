 package servlet;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import bean.*;
 
 import dao.*;
 
 /**
  * Servlet implementation class AjaxServlet
  */
 @WebServlet("/AjaxServlet")
 public class AjaxServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AjaxServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession(true);
 		JSONObject rsobj = new JSONObject();
 		Post post = new Post();
 		
 		//get Post id
 		int postid = -1;
 		try {
 			postid = Integer.parseInt(request.getParameter("postid"));
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			postid = -1;
 		}
 		
 		// 로그인 상태이면 사용자의 정보를 받아온다
 		String loginphoto = "";
 		if(session != null) {
 			Member user = (Member) session.getAttribute("user");
 			if(user != null) {
 				loginphoto = user.getProfilephoto();
 			}
 		}
 		
 		try {
 			post = PostDAO.findByPostID(postid);
			
 			JSONArray comjsar = new JSONArray();
 			
 			//jsar.add(post.getMember().UsertoJson());
 			//jsar.add(post.getArticle().ArtitoJson());
 			
 			rsobj.put("user", post.getMember().UsertoJson());
 			rsobj.put("post", post.getArticle().ArtitoJson());
 			for(int i=0; i<post.getComment().size(); i++) {
 				comjsar.add(post.getComment().get(i).CommenttoJson());
 			}
 			rsobj.put("comment", comjsar);
 			rsobj.put("loginphoto", loginphoto);
 			/*List<Article> list = ArticleDAO.getalllist();
 			
 			for(int i=0; i<list.size(); i++) {
 				jsar.add(list.get(i).ArtitoJson());
 			}*/
 		} catch (Exception e) {
 			System.out.println(e);
 			rsobj.put("ERROR", e.getMessage());
 		}
 		response.setContentType("application/json");
 		response.setCharacterEncoding("UTF-8");
 		response.getWriter().print(rsobj.toJSONString());
		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
