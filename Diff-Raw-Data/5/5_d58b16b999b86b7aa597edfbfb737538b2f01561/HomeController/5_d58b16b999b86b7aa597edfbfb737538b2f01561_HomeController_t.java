 package com.vaps.home;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import com.oreilly.servlet.MultipartRequest;
 import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
 import com.vaps.action.BoardListAction;
 import com.vaps.action.ItemsListAction;
 import com.vaps.action.MembersAction;
 import com.vaps.bean.BoardList;
 import com.vaps.bean.Items;
 import com.vaps.bean.Members;
 import com.vaps.dao.MembersDAO;
 import com.vaps.dao.ItemsDAO;
 import com.vaps.userclass.EncryptionEncoding;
 
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 @SessionAttributes("members")
 // members라는 세션영역에 저장
 // jsp에서 ${members.id} 이런식으로 세션영역의 변수를 호출할 수 있다. sessionScope.members.id 대신함
 public class HomeController {
 	static HttpSession session;
 	// mybatis-context.xml 에서 연결되었다.
 	@Resource(name = "membersDao")
 	// @Autowired 도 왼쪽과 같은 자동주입이나 권장하지 않는 방법이다. 가능하면 @Resource를 쓰라
 	private MembersDAO membersDao;
 
 	// 암호화, 자동주입 등록(mybatis-context.xml에서)
 	// private EncryptionEncoding ee = new EncryptionEncoding();
 	@Resource(name = "EncryptionEncoding")
 	// @Resource(name="EE") //네임으로 설정으로 가능하다.
 	private EncryptionEncoding ee;
 
 // URL
 // 기능별로 폴더를 나누어 작업할 것이니 리턴 경로를 확인하자!
 // --------------------------------------------------------------
 // 메뉴 관리
 	@RequestMapping(value = "/")
 	public String home() {
 		return "home";
 	}
 
 	@RequestMapping(value = "/join")
 	public String join() {
 		// join 파일 위치가 /WEB-INF/views/login/join.jsp 이다.
 		return "login/join";
 	}
 
 	@RequestMapping(value = "/login")
 	public String login() {
 		return "login/login";
 	}
 
 	@RequestMapping(value = "/what")
 	public String what() {
 		return "about/what";
 	}
 
 	@RequestMapping(value = "/developer")
 	public String developer() {
 		return "about/developer";
 	}
 	@RequestMapping(value = "/info")
 	public String info() {
 		return "member/info";
 	}
 	//No mapping found for HTTP request with URI [/favicon.ico] solution
 	@RequestMapping(value = "/favicon.ico")
	public void favicon(HttpServletResponse reponse ) {
 		try {
			reponse.sendRedirect("/images/favicon.ico");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 // --------------------------------------------------------------
 // 회원 관리
 	// 회원가입
 	@RequestMapping(value = "/memJoin")
 	public String memJoin(HttpServletRequest request, Model model) {
 		String result = "join";
 		Members mb = new Members();
 
 		try {
 			request.setCharacterEncoding("UTF-8");
 			mb.setM_id(request.getParameter("id"));
 			mb.setM_nick(request.getParameter("nick"));
 			mb.setM_pwd(ee.TripleDesEncoding(request.getParameter("pwd"))); // 패스워드 인코딩
 			mb.setM_phone(request.getParameter("phone"));
 			mb.setM_addr(request.getParameter("addr"));
 
 			MembersAction ma = new MembersAction(membersDao);
 			result = ma.memInsert(mb);
 			model.addAttribute("check", 1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	// login 성공
 	@RequestMapping(value = "/access")
 	public String mInfo(HttpServletRequest request, Model model) throws Exception {
 		String result = "home";
 
 		Members members = new Members();
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("id", request.getParameter("id"));
 		// DB에 저장된 패스워드가 암호화가 되어 있어 로그인할때 똑같이 암호화한뒤 저장된 패스워드랑 비교해야한다.
 		map.put("pwd", ee.TripleDesEncoding(request.getParameter("pwd")));
 		try {
 			MembersAction ma = new MembersAction(membersDao);
 			members = ma.accessMembers(map);
 
 			if (members != null) {
 				session = request.getSession();
 				session.setAttribute("id", members.getM_id()); // 세션에 아이디 저장, name=id
 				session.setAttribute("auth", members.getM_auth()); // 세션에 권한 저장, name=auth
 				model.addAttribute("members", members);
 				boardList(request, model); // 로그인 뒤에 게시판으로 직행하지 않게함
 			} else {
 				if (session != null) {
 					session = null;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 	
 	// 아이디 중복 
 	@RequestMapping(value="/MemberIDCheckAction")
 	public String idchck(HttpServletRequest request, Model model) throws Exception{
 		String result="./login/member_idchk";
 		//Map<String,String> map=new HashMap<String, String>();
 		//map.put("id", request.getParameter("id"));
 		int x=0;
 		x=membersDao.confirmId(request.getParameter("id"));
 	    model.addAttribute("xx", x);
 	    
 		return result;
 	}
 	// logout
 		@RequestMapping(value="/logout")
 		public String logout(){
 			return "login/logout";
 		}
 	//--------------------------------------------------------------
 // 게시판 관리
 	// 게시판(질답용도)
 	@RequestMapping(value = "/board")
 	public String boardList(HttpServletRequest request, Model model) {
 		String result = "home";
 
 		try {
 			BoardListAction ba = new BoardListAction(membersDao);
 
 			if (session != null && session.getAttribute("id") != "") {
 				session = request.getSession();
 				int pageNum = (request.getParameter("pageNum") != null) ? Integer.parseInt(request.getParameter("pageNum")) : 1;
 
 				// paging 관련 로직
 				model.addAttribute("pageNum", pageNum);
 				model.addAttribute("blist", ba.getBoardList(pageNum)); // 게시글
 				model.addAttribute("paging", ba.getPaging(pageNum)); // [1][2]...<-paging
 				result = "board/boardlist";
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	// 게시글 보기
 	@RequestMapping(value = "/contents")
 	public String listContents(HttpServletRequest request, Model model) {
 		String result = "contents";
 		session = request.getSession();
 		BoardListAction ba = new BoardListAction(membersDao);
 		try {
 			if (session != null && session.getAttribute("id") != "") {
 				int bnum = Integer.parseInt(request.getParameter("idx"));
 				ba.setUpdateCount(bnum);//조회수 증가
 				model.addAttribute("blist", ba.getContents(bnum)); // 원글 보기
 				// 세션에 게시물 번호 저장, name=idx
 				session.setAttribute("idx", Integer.parseInt(request.getParameter("idx"))); 
 				result = "board/boardContents";
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	// 게시글 쓰기 폼
 	@RequestMapping(value = "/boardWriteForm")
 	public String boardWrite() {
 		return "board/boardWrite";
 	}
 
 	// 게시글 쓰기(sql 삽입), 등록버튼 눌렀을때
 	@RequestMapping(value = "/boardWrite")
 	public void boardWR(HttpServletRequest request, Model model, HttpServletResponse res) {
 		try {
 			// DB로 한글 저장시 깨짐 해결함
 			request.setCharacterEncoding("UTF-8");
 			BoardListAction ba = new BoardListAction(membersDao);
 			if (session != null && session.getAttribute("id") != "") {
 				BoardList wr = new BoardList();
 				wr.setB_id((String) session.getAttribute("id")); // 게시자
 				wr.setB_sub(request.getParameter("sub")); // 제목
 				wr.setB_contents(request.getParameter("contents")); //내용 wr에 저장
 
 				PrintWriter out = res.getWriter();
 				res.setContentType("text/html;charset=UTF-8"); // 한글처리코드
 				if (ba.writeBoard(wr) == 1) {
 					// 글쓰기 성공하고 /board로 가기 위해서 스크립트 코드 사용
 					// String result ="/board/boardlist"; 이런식으로 가면 정상작동안됨
 					out.println("<script>");
 					out.println("location.href='/board'");
 					out.println("</script>");
 				} else {
 					// 실패시 홈으로 이동
 					out.println("<script>");
 					out.println("alert('게시글 쓰기 실패')");
 					out.println("location.href='/'");
 					out.println("</script>");
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// 게시글 삭제
 	@RequestMapping(value = "/boardDelContent")
 	public void boardDelContent(HttpServletRequest req, HttpServletResponse res, Model model) {
 		// 게시글 번호를 가져와 사용자 본인 확인or 관리자 확인 후 삭제
 		BoardListAction ba = new BoardListAction(membersDao);
 		try {
 			// sql 으로 글쓴이 확인 과정을 거쳐야 한다.
 			res.setContentType("text/html;charset=UTF-8"); // 한글처리코드
 			PrintWriter out = res.getWriter();
 			int bnum = (Integer) session.getAttribute("idx"); // 게시글 번호 가져오기
 			String id = (String) req.getParameter("id"); // 게시글 쓴 사람 id 가져오기
 
 			if (session != null && session.getAttribute("id") != "") {
 				// 현재 접속한 id와 게시글을 올린 id와 비교, 관리자 auth=1은 무조건 삭제
 				if (id.equals((String) session.getAttribute("id"))
 						|| (Integer) session.getAttribute("auth") == 1) {
 					if (ba.delContents(bnum) == 1) {// 게시글 삭제 쿼리 들어가기
 						// 페이지 이동, 성공
 						out.println("<script>");
 						out.println("location.href='/board'");
 						out.println("</script>");
 					}
 				} else {
 					// 페이지 이동, 실패
 					out.println("<script>");
 					out.println("alert('게시글 삭제 실패')");
 					out.println("location.href='/board'");
 					out.println("</script>");
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// 게시글 수정 폼
 	@RequestMapping(value = "/boardModiContentForm")
 	public String boardModiContentForm(HttpServletRequest req, HttpServletResponse res, Model model) {
 		session = req.getSession();
 
 		res.setContentType("text/html;charset=UTF-8"); // 한글처리코드
 		BoardListAction ba = new BoardListAction(membersDao);
 		try {
 			if (session != null && session.getAttribute("id") != "") {
 				int bnum = Integer.parseInt(req.getParameter("idx"));
 				// 원글 보기,함수를 따라가 보면 줄 바꿈 처리를 했음
 				model.addAttribute("blist", ba.getContentsModi(bnum));
 				// 세션에 게시물 번호 저장,name=idx
 				session.setAttribute("idx", Integer.parseInt(req.getParameter("idx")));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "board/boardModify";
 	}
 
 	// 게시글 수정
 	@RequestMapping(value = "/boardModiContent")
 	public void boardModiContent(HttpServletRequest req,HttpServletResponse res, Model model) {
 		// 해당 글번호를 쿼리로 불러와 jsp에 뿌리고 수정한 내용을 다시 update를 시킨다.
 		// 현재 로그인사람과 수정할 글쓴이를 비교하기위해 jsp페이지에서 get과 post방식을 둘다 사용하여 정보를 넘긴다.
 		// get방식으로 b_id를 전송하고 post방식으로 idx, id, sub, contents를 불러온다.
 		// 실제 update 쿼리되는 부분은 sub와 contents만 변경되고 기준 조건은 idx값을 가지고 고친다.
 		try {
 			// DB로 한글 저장시 깨짐 해결함
 			req.setCharacterEncoding("UTF-8");
 			res.setContentType("text/html;charset=UTF-8"); // 한글처리코드
 
 			BoardList wr = new BoardList();
 			BoardListAction ba = new BoardListAction(membersDao);
 			PrintWriter out = res.getWriter();
 			
 			String id = (String) req.getParameter("id"); // 게시글 쓴 사람 id 가져오기
 
 			if (session != null && session.getAttribute("id") != "") {
 				if (id.equals(session.getAttribute("id"))
 						|| (Integer) session.getAttribute("auth") == 1) {
 					wr.setB_num((Integer) session.getAttribute("idx")); // idx
 					wr.setB_id((String) session.getAttribute("id")); // id
 					wr.setB_sub(req.getParameter("sub")); // title
 					wr.setB_contents(req.getParameter("contents"));
 
 					ba.setContentsModi(wr);
 					out.println("<script>");
 					out.println("location.href='/board'");
 					out.println("</script>");
 				} else {
 					out.println("<script>");
 					out.println("alert('게시글 수정 실패')");
 					out.println("location.href='/'");
 					out.println("</script>");
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
