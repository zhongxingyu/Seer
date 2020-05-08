 /**
  * 
  */
 package org.humanizer.rating;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.humanizer.rating.objects.Items;
 import org.humanizer.rating.objects.TasksByRater;
 import org.humanizer.rating.utils.HTTPClient;
 
 import com.google.gson.Gson;
 
 /**
  * @author sonhv
  *
  * Showing results for a keyword rating
  */
 @SuppressWarnings("serial")
 public class ListDetailServlet extends HttpServlet {
   //private static final Logger log = Logger.getLogger(AuthenServlet.class.getName());
   /**
    * @author sonhv
    * 
    * GET handling
    * Redirect to POST 
    */  
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
     doPost(req, resp);
   }
   
   /**
    * @author sonhv
    * 
    * POST handling
    * Listing details for rate 
    */
   public void doPost(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
   HttpSession sess = req.getSession(true);
   String username = (String) sess.getAttribute("username");	
 	if (username == null){
 		resp.sendRedirect("/login.jsp");
 		return;
 	}  
   String keyword = req.getParameter("keyword");
   String task = req.getParameter("task");
   String task_name = req.getParameter("task_name");
   
   //perform get rate list by keyword and task
   StringBuilder sb = new StringBuilder();
   String sURL = "";
   String sResult = "";
   //1. Get items list
   /*
   sURL = "http://humanizer.iriscouch.com/items/_design/index/_view/items_list";
   sResult = HTTPClient.request(sURL);
   Items item = new Items();
   item.initItemList(sResult);  
   */
   String username_encoded = URLEncoder.encode(username,"UTF-8");
   String keyword_encoded = URLEncoder.encode(keyword,"UTF-8");
   TasksByRater rater = new TasksByRater();
   
 
   //1. Get Rater's task
   sURL = "http://humanizer.iriscouch.com/tasks/_design/api/_view/tasks_by_rater?startkey=%22" + username_encoded + "%7C" + task + "%22&endkey=%22" + username_encoded + "%7C" + task +  "%22&include_docs=true";
   sResult = HTTPClient.request(sURL);  
   rater.init(sResult);
   
   //ArrayList lst = (ArrayList)rater.getData();
   
   ArrayList data = (ArrayList) rater.getData();
   //2.1 Get Items list 	
   sURL = "http://humanizer.iriscouch.com/items/_design/index/_view/items_in_task?startkey=%22" + task + "%22&endkey=%22" + task + "%22&include_docs=true";
   sResult = HTTPClient.request(sURL);
   Items item = new Items();
   item.initItemListForTask(sResult);
   rater.setItemList(item.getItemList());
   //2.2 Get Rater's rating
   //sURL = "http://humanizer.iriscouch.com/ratings/_design/api/_view/rating_by_rater_as_key?startkey=%22" + username_encoded + "%22&endkey=%22" + username_encoded + "%22&include_docs=true";
   sURL = "http://humanizer.iriscouch.com/ratings/_design/api/_view/rating_by_rater_task?startkey=%22" + username + "%7C" + task  + "%22&endkey=%22" + username + "%7C" + task + "%22";    
   sResult = HTTPClient.request(sURL);  
   rater.setRatingResult(sResult);    
   //newData.add(item.getItemList())
   rater.refineWithRating();
 
   /*
   List newData = new ArrayList();
   for (int i = 0; i < data.size(); i++){
   	ArrayList elem = (ArrayList) data.get(i);
   	String id = elem.get(0).toString();
   }
   
   ArrayList lst = (ArrayList)rater.getData();
   ArrayList lst1 = (ArrayList) lst.get(0);
   for (int i = 0; i <4; i ++){
 	  //remove 4 first item
 	  lst1.remove(0);
   }
   
   //lst1.remove(0);*/
   req.setAttribute("data",rater.getData());
   req.setAttribute("keyword", keyword);
   req.setAttribute("task", task);
   req.setAttribute("task_name", task_name);
   //req.setAttribute("task_status", rater.getStatus());
   
   RequestDispatcher dispatcher = req.getRequestDispatcher("/list_detail.jsp");
 
   if (dispatcher != null){
     try {
       dispatcher.forward(req, resp);
     } catch (ServletException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   } 
 
   }  
 }
