 package ez.dork.dbTool.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.Gson;
 
 import ez.dork.dbTool.util.SqlUtil;
 
 /**
  * Simple Hello servlet.
  */
 public final class GridViewServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		doPost(request, response);
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		response.setCharacterEncoding("utf8");
 
 		String sqls = request.getParameter("sql");
 		Map<String, Object> result = new HashMap<String, Object>();
 		result.put("msg", "");
 
 		if (sqls != null) {
 			for (String sql : sqls.split(";")) {
				sql = sql.trim();
				if ("".equals(sql)) {
 					continue;
 				}
 
				if (sql.matches("(?i)(?s)(insert |update |delete |drop |create |alter ).*")) {
 					executeBatch(sql, result);
 				} else {
 					executeQuery(sql, result);
 				}
 			}
 		}
 
 		PrintWriter writer = response.getWriter();
 		writer.print(new Gson().toJson(result));
 		writer.close();
 	}
 
 	private void executeBatch(String sql, Map<String, Object> result) {
 		String msg = (String) result.get("msg");
 		try {
 			int updateCount = SqlUtil.executeUpdate(sql);
 			msg += genOkMsg(String.format("effect %d row(s)", updateCount));
 			result.put("err", false);
 
 		} catch (Exception e) {
 			msg += genErrMsg(e.getMessage());
 			result.put("err", true);
 			e.printStackTrace();
 		}
 		result.put("msg", msg);
 	}
 
 	private void executeQuery(String sql, Map<String, Object> result) {
 		String msg = (String) result.get("msg");
 		try {
 			Map<String, Object> queryToEditablegrid = SqlUtil.queryToEditablegrid(sql);
 			result.put("result", queryToEditablegrid);
 			msg += genOkMsg(String.format("return results : %d row(s)", ((List<?>) queryToEditablegrid.get("data")).size()));
 			result.put("err", false);
 		} catch (Exception e) {
 			result.put("err", true);
 			msg += genErrMsg(e.getMessage());
 			e.printStackTrace();
 		}
 		result.put("msg", msg);
 	}
 
 	private static String genErrMsg(String msg) {
 		return String.format("<div class='err'>%s.</div>", msg);
 	}
 
 	private static String genOkMsg(String msg) {
 		return String.format("<div class='ok'>%s.</div>", msg);
 	}
 
 }
