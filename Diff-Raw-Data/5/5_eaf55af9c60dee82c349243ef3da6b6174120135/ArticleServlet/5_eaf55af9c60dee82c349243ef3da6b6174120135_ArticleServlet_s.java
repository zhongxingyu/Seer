 package servlet;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.renderable.ParameterBlock;
 import java.io.File;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.media.jai.JAI;
 import javax.media.jai.RenderedOp;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import bean.Article;
 import bean.Category;
 import bean.Member;
 import bean.PageResult;
 import bean.Post;
 
 import com.oreilly.servlet.MultipartRequest;
 import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
 
 import dao.ArticleDAO;
 import dao.PostDAO;
 
 
 /**
  * Servlet implementation class ArticleServlet
  */
 @WebServlet("/article")
 public class ArticleServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ArticleServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession(true);
 
 		String op = request.getParameter("op");
 		String search = "";
 		if(request.getParameter("search") != null) {
 			search = request.getParameter("search");
 		}
 		String actionUrl = "";
 		
 		if(op == null) {
 			op = "list";
 		}
 
 		try {
 			if(op.equals("write")) { 
 				Category list = ArticleDAO.getlist(); // 카테고리 리스트를 받아옴
 
 				request.setAttribute("method", "POST");
 				request.setAttribute("article", new Article());
 				request.setAttribute("category", list);
 				actionUrl = "articlewrite.jsp";
 			} else if(op.equals("update")) {
 				
 			} else if(op.equals("list")) {
 				ArrayList<Post> posts = PostDAO.getAllPage(); // 모든 글 가져오기
 				request.setAttribute("posts", posts);
 				
 				actionUrl = "photolist_all.jsp";
 			} else if(search.equals("search")) {
 				PageResult<Post> posts = PostDAO.getPage(1, 10); // 10개를 가져오는거던가..
 				request.setAttribute("posts", posts);
 				
 				actionUrl = "photolist.jsp";
 			}
 		} catch (Exception e) {
 			System.out.println(e);
 		}
 		
 		RequestDispatcher dispatcher = request.getRequestDispatcher(actionUrl);
 		dispatcher.forward(request, response);
 
 	}
 
 	private boolean isUploadMode(MultipartRequest request) {
 		String method = request.getParameter("_method");
 		return method == null || method.equals("POST");
 	}	
 	
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		boolean ret 		= false;
 		boolean res = true;
 		String 	actionUrl = "";
 		Article post = new Article();
 		MultipartRequest multi = null;
 		
 		request.setCharacterEncoding("utf-8");
 		List<String> errorMsgs = new ArrayList<String>();
 				
 		/* MultipartRequest를 사용하면 이미 request의 값은 소멸함.. 아오 빡쳐...*/
 		String imagePath = getServletContext().getRealPath("images/photo"); //실제로 업로드 될 폴더의 경로 설정
 		int size = 5 * 1024 * 1024; //업로드 사이즈 제한. 5MB로 설정
 		
 		multi = new MultipartRequest(request, imagePath, size, "utf-8", new DefaultFileRenamePolicy());		
 		
 		/* POST로 설정된 form에서 값을 받아와서 임시로 저장함 */
 		if(!isUploadMode(multi)) { // 수정이면 게시글 id를 받아옴
 			int postid = Integer.parseInt(multi.getParameter("postid"));
 			post.setPostid(postid);
 		}
 		
 		Member user = (Member) session.getAttribute("user"); 		
 		String userid = user.getUserid();
 		String photo = UploadPhoto(multi, response, imagePath);
 		String content = multi.getParameter("content");
 		//int album;
 		String Category = multi.getParameter("category");
 
 		if (isUploadMode(multi) && photo == null) {
 			errorMsgs.add("사진을 올려주세요");
 			res = false;
 		}
 		
 		if (content == null || content.trim().length() == 0) {
 			errorMsgs.add("내용을 입력해주세요");
 			res = false;
 		}
 		
 		//post.setAlbumid();
 		post.setCategory(Category);
 		post.setContent(content);
 		post.setPostip(1);
 		post.setPhoto(photo);
 		post.setPostdate(new Timestamp(System.currentTimeMillis()));
 		post.setUserid(userid);
 		
 		try {
 			if(res) {
 				if (isUploadMode(multi)) {
 					ret = ArticleDAO.create(post);
 				} else {
 					ret = ArticleDAO.update(post);
 				}
 				if (ret != true) {
 					errorMsgs.add("글 작성이나 수정에 실패했습니다.");
 					actionUrl = "error.jsp";
 				} else {
 					actionUrl = "main.jsp";
 				}
 			}
 		} catch (Exception e) {
 			errorMsgs.add(e.getMessage());
 			System.out.println(e.getMessage());
 			actionUrl = "error.jsp";
 		}
 		
 		// 주소창을 이쁘게 보일려는 발악..
 		if(actionUrl.equals("main.jsp")) {
 			response.sendRedirect("");
 		} else {		
 			request.setAttribute("errorMsgs", errorMsgs);
 			RequestDispatcher dispatcher = request.getRequestDispatcher(actionUrl);
 			dispatcher.forward(request, response);
 		}
 	}
 
 	private String UploadPhoto(MultipartRequest multi, HttpServletResponse response, String imagePath) {
 		String photo = "";
 		String changephoto = "";
 		String curTimeStr 	= Long.toString(System.currentTimeMillis()); //use Unix Time
 	
 		try {
 			// 이미지 업로드
 			photo = multi.getParameter("images/photo");
 			
 			// 업로드 된 이미지 이름 얻어옴!
 			Enumeration files 	= multi.getFileNames();
 			String 		file 	= (String) files.nextElement();
 			
 			/* 파일을 업로드 했다면 썸네일을 만든다 */
 			if((multi.getOriginalFileName(file)) != null) {
 				// 파일을 업로드 했으면 파일명을 얻음
 				photo = multi.getOriginalFileName(file);
 				// 파일명 변경준비
 				changephoto 	= photo;
 				String fileExt 	= "";
 				int i = -1;
 				if ((i = changephoto.lastIndexOf(".")) != -1) {
 					fileExt = changephoto.substring(i); // 확장자만 추출
 					changephoto = changephoto.substring(0, i); // 파일명만 추출
 				}
 				// 사진명을 UNIXTIME_USERID로 설정
 				changephoto = curTimeStr + fileExt;
 				// 파일명 변경
 				File oldFile = new File(imagePath + System.getProperty("file.separator") + photo);
 				File newFile = new File(imagePath + System.getProperty("file.separator") + changephoto);	
 			    oldFile.renameTo(newFile);
 			    
			    //리스트에 표시할 폭 200px의 썸네일과 상세보기에 표시할 폭 700px의 썸네일을 만든다
 				// 이 클래스에 변환할 이미지를 담는다.(이미지는 ParameterBlock을 통해서만 담을수 있다.)
 				ParameterBlock pb = new ParameterBlock();
 				pb.add(imagePath + System.getProperty("file.separator") + changephoto); // 
 				
 				RenderedOp rOp = JAI.create("fileload", pb);
 		
 				// 불러온 이미지를 BuffedImage에 담는다.
 				BufferedImage bi = rOp.getAsBufferedImage();
 				// 이미지의 폭과 너비를 저장
 				int tb_width = bi.getWidth(); // 폭
 				int tb_height =  bi.getHeight(); // 너비
 				
 				// 우리 페이지의 썸네일폭은 200으로 고정이므로 그림을 200에 맞춰서 보정한다. 200보다 작으면 바꾸지 않고 200보다 크면 그림 크기를 변경한다
 				if(tb_width > 200) {
 					tb_width = 200;
					tb_height = bi.getHeight() / (bi.getWidth() / 200);
 				}
 				
 				// thumb라는 이미지 버퍼를 생성, 버퍼의 사이즈는 200*(상대값)으로 설정.
 				BufferedImage thumb = new BufferedImage(tb_width, tb_height, BufferedImage.TYPE_INT_RGB);
 		
 				// 버퍼사이즈 200*(상대값)으로  맞춰  썸네일을 그림
 				Graphics2D g = thumb.createGraphics();
 				g.drawImage(bi, 0, 0, tb_width, tb_height, null);
 		
 				//출력할 위치와 파일이름을 설정하고 섬네일 이미지를 생성한다. 저장하는 타입을 jpg로 설정.
 				File file1 = new File(imagePath + "/sm" + changephoto);
 				ImageIO.write(thumb, "jpg", file1);  
 			}
 		} catch (Exception e) {
 			// 저장에 실패하면 경로를 얻지못했기때문에 null을 리턴
 			System.out.println(e);
 			return null;
 		}		
 		return changephoto;
 	}
 	
 }
