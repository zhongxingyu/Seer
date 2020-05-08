package com.snow.servlet; 
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.snow.model.Type;
 import com.snow.util.DBUtil;
 
 /**
  * Servlet implementation class TypeServlet
  */
 public class TypeServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public TypeServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String method = request.getParameter("method");
 		
 		if("query".equals(method)) {
 			query(request, response);
 		} else if ("insert".equals(method)) {
 			insert(request, response);
 		} else if ("update".equals(method)) {
 			update(request, response);
 		} else if ("delete".equals(method)) {
 			delete(request, response);
 		} else if ("preUpdate".equals(method)) {
 			preUpdate(request, response);
 		} else if ("queryChild".equals(method)) {
 			queryChild(request, response);
 		}
 	}
 
 	private void query(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Connection conn = DBUtil.getConn();
 		String sql = "select * from type where id < 100";
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ResultSet rs = ps.executeQuery();
 			List<Type> plist = new ArrayList<Type>();
 			while(rs.next()) {
 				Type t = new Type();
 				t.setId(Integer.parseInt(rs.getString("id")));
 				t.setName(rs.getString("name"));
 				t.setPid(Integer.parseInt(rs.getString("pid")));
 				plist.add(t);
 			}
 			request.setAttribute("plist", plist);
 			
 			List<List<Type>> clist = new ArrayList<List<Type>>();
 			for(int i=0; i<plist.size(); i++) {
 				sql = "select * from type where pid = ?";
 				ps = conn.prepareStatement(sql);
 				ps.setInt(1, plist.get(i).getId());
 				rs = ps.executeQuery();
 				List<Type> temp = new ArrayList<Type>();
 				while(rs.next()) {
 					Type t = new Type();
 					t.setId(Integer.parseInt(rs.getString("id")));
 					t.setName(rs.getString("name"));
 					t.setPid(Integer.parseInt(rs.getString("pid")));
 					temp.add(t);
 				}
 				clist.add(temp);
 			}
 			
 			request.setAttribute("plist", plist);
 			request.setAttribute("clist", clist);
 			String source = request.getParameter("source");
 			if(null != source && source.equals("admin")) {
 				request.getRequestDispatcher("/jsp/admin/type/list.jsp").forward(request, response);
 			}else if(null != source && source.equals("cadd")){
 				response.sendRedirect(request.getContextPath() + "/TypeServlet?method=queryChild&id=" + request.getParameter("pid"));
 			} else{
 				request.getRequestDispatcher("/home.jsp").forward(request, response);
 			}
 			//response.sendRedirect(request.getContextPath() + "/l.jsp");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}  
 	}
 	
 	
 	private void insert(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String name = request.getParameter("name");
 		name = new String(name.getBytes("ISO-8859-1"),"utf-8");
 		String pid = request.getParameter("pid");
 		String id = request.getParameter("id");
 		Connection conn = DBUtil.getConn();
 		String sql = "insert into type (id, name, pid) values (?, ?, ?)";
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, Integer.parseInt(id));
 			ps.setString(2, name);
 			ps.setInt(3, Integer.parseInt(pid));
 			int num = ps.executeUpdate();
 			System.out.println(num);
 			request.getRequestDispatcher("/jsp/admin/type/add.jsp").forward(request, response);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}  
 	}
 	
 	private void update(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String id = request.getParameter("id");
 		String name = request.getParameter("name");
 		name = new String(name.getBytes("ISO-8859-1"),"utf-8");
 		String pid = request.getParameter("pid");
 		Connection conn = DBUtil.getConn();
 		String sql = "update type set id = ?, name = ?, pid = ? where id = ?";
 		Type t = new Type();
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, Integer.parseInt(id));
 			ps.setString(2, name);
 			ps.setInt(3, Integer.parseInt(pid));
 			ps.setInt(4, Integer.parseInt(id));
 			int num = ps.executeUpdate();
 			t = findById(id);
 			System.out.println(num);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}  
 		request.setAttribute("type", t);
 		request.getRequestDispatcher("/jsp/admin/type/update.jsp").forward(request, response);
 		
 	}
 	
 	private void preUpdate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String id = request.getParameter("id");
 		Type t = findById(id);
 		request.setAttribute("type", t);
 		request.getRequestDispatcher("/jsp/admin/type/update.jsp").forward(request, response);
 	}
 	
 	private Type findById(String id) {
 		Connection conn = DBUtil.getConn();
 		String sql = "select * from type where id = ?";
 		Type t = new Type();
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, Integer.parseInt(id));
 			ResultSet rs = ps.executeQuery();
 			while(rs.next()){
 				t.setId(Integer.parseInt(rs.getString("id")));
 				t.setName(rs.getString("name"));
 				t.setPid(Integer.parseInt(rs.getString("pid")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}  
 		return t;
 	}
 
 	private void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String id = request.getParameter("id");
 		Connection conn = DBUtil.getConn();
 		String sql = "delete from type where id = ?";
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, Integer.parseInt(id));
 			int num = ps.executeUpdate();
 			System.out.println(num);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}  
 		response.sendRedirect(request.getContextPath() + "/TypeServlet?method=query&source=admin");
 	}
 	
 	
 	private void queryChild(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Connection conn = DBUtil.getConn();
 		String sql = "select * from type where pid = ?";
 		String pid = request.getParameter("id");
 		try {
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, Integer.parseInt(pid));
 			ResultSet rs = ps.executeQuery();
 			List<Type> clist = new ArrayList<Type>();
 			while(rs.next()) {
 				Type t = new Type();
 				t.setId(Integer.parseInt(rs.getString("id")));
 				t.setName(rs.getString("name"));
 				t.setPid(Integer.parseInt(rs.getString("pid")));
 				clist.add(t);
 			}
 			request.setAttribute("clist", clist);
 			request.setAttribute("pid", pid);
 			request.getRequestDispatcher("/jsp/admin/type/child.jsp").forward(request, response);
 			//response.sendRedirect(request.getContextPath() + "/l.jsp");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}  
 	}
 
 }
